import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Cache_Simulator {
    private static String memoryTracePath = "";
    private static int cacheSize = 0;
    private static int blockSize = 0;
    private static int ways = 0;
    private static String[] instructionsArray;
    private static String[] offsetsArray;
    private static int KB = 1024;
    private static int numberOfCacheHits = 0;
    private static int numberOfCacheMisses = 0;
    private static int tagBitsLength = 0;
    private static int indexBitsLength = 0;
    private static int offsetBitsLength = 0;
    private static int setCount = 0;
    private static int[] fullyAssociativeCache;
    private static HashMap<Integer, Integer> directMappedCache;
    private static int[][] nWaySetAssociativeCache;
    private static int[][] LRUArray;
    private static final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static void main(String[] args) throws IOException {
        getInputFromCommandLine(args);
        findTagInputAndOffsetBits();
        readMemTraceFileInstructions();
        getHexadecimalAddresses();
        printOutput();
    }

    private static void getInputFromCommandLine(String[] args) {
        if (args.length == 4) {
            memoryTracePath = args[0];
            if(isNumeric(args[1])){
                cacheSize = Integer.parseInt(args[1]);
            }
            if(isNumeric(args[2])){
                blockSize = Integer.parseInt(args[2]);
            }
            if(isNumeric(args[3])){
                ways = Integer.parseInt(args[3]);
            }
        } else {
            System.out.println("Enter input command arguments properly");
        }
    }

    private static void findTagInputAndOffsetBits() {
        offsetBitsLength = (int) (Math.log(blockSize) / Math.log(2));
        if (ways == 0) {         //fully-set-associative-cache
            setCount = (int) Math.pow(2, 32 - offsetBitsLength);
            fullyAssociativeCache = new int[setCount];
        }
        else if (ways == 1) {    //direct-mapped-cache
            directMappedCache = new HashMap<>();
            setCount = (cacheSize * KB) / blockSize;
            indexBitsLength = (int) (Math.log(setCount) / Math.log(2));
        }
        else if (ways > 1) {    //nWay-set-associative-cache
            setCount = (cacheSize * KB) / (blockSize * ways);
            indexBitsLength = (int) (Math.log(setCount) / Math.log(2));
            nWaySetAssociativeCache = new int[setCount][ways];
            LRUArray = new int[setCount][ways];
        }
        tagBitsLength = (32 - indexBitsLength - offsetBitsLength);
    }

    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    private static void readMemTraceFileInstructions() throws IOException {
        BufferedReader memTraceInstructions = null;
        try {
            memTraceInstructions = new BufferedReader(new FileReader("src/" + memoryTracePath));
        } catch (Exception exception) {
            System.out.println("Exception while parsing the memTrace file instructions " + exception);
        }
        String instruction;
        List<String> instructionList = new ArrayList<String>();
        while ((instruction = memTraceInstructions.readLine()) != null) {
            instructionList.add(instruction);
        }
        instructionsArray = instructionList.toArray(new String[0]);
        offsetsArray = new String[instructionsArray.length];

        for (int i = 0; i < instructionsArray.length; i++) {
            offsetsArray[i] = instructionsArray[i].substring(2, instructionsArray[i].lastIndexOf(" "));
        }
    }

    private static void getHexadecimalAddresses(){
        long address = 0, truncatedAddressBits = 0, cacheIndex = 0, cacheTag = 0, cacheOffset = 0;
        BigInteger decimalAddress;
        String truncatedAddress = null;
        int instructionOffset = 0;

        for (int i = 0; i < instructionsArray.length; i++) {
            String hexadecimalAddress = instructionsArray[i].substring(instructionsArray[i].lastIndexOf(" ") + 1);
            decimalAddress = new BigInteger(hexadecimalAddress, 16);
            instructionOffset = Integer.parseInt(offsetsArray[i]);
            address = decimalAddress.longValue() + instructionOffset;
            String binaryAddress = Long.toBinaryString(address);
            if (binaryAddress.length() > 32)
                truncatedAddress = Long.toBinaryString(address & 4294967295L);
            else if (binaryAddress.length() < 32)
                truncatedAddress = ("0".repeat(32 - binaryAddress.length())) + binaryAddress;
            assert truncatedAddress != null;
            truncatedAddressBits = Long.parseLong(truncatedAddress, 2);

            cacheIndex = (truncatedAddressBits >> offsetBitsLength) & ((1L << indexBitsLength) - 1);
            cacheTag = truncatedAddressBits >> (offsetBitsLength + indexBitsLength);
            cacheOffset = (truncatedAddressBits) & ((1L << offsetBitsLength) - 1);
            simulateCache(cacheIndex, cacheTag, cacheOffset);
        }
    }

    private static void simulateCache(long cacheIndex, long cacheTag,long cacheOffset){
        if(ways==0)                 //fully-set-associative-cache
        {
            if(fullyAssociativeCache[(int)cacheTag]==(int)cacheOffset)  {
                numberOfCacheHits++;
            }
            else{
                numberOfCacheMisses++;
                fullyAssociativeCache[(int)cacheTag]=(int)cacheOffset;
            }
        }
        else if(ways==1) {          //direct-mapped-cache
            if(directMappedCache.get((int)cacheIndex) == null){
                numberOfCacheMisses++;
                directMappedCache.put((int)cacheIndex,(int)cacheTag);
            } else if(directMappedCache.get((int)cacheIndex) !=cacheTag) {
                numberOfCacheMisses++;
                directMappedCache.put((int)cacheIndex,(int)cacheTag);
            }
            else if(directMappedCache.get((int)cacheIndex) == cacheTag){
                numberOfCacheHits++;
            }
        }
        else if(ways>1){            //nWay-set-associative-cache
            for(int i = 0; i<ways; i++){
                if(nWaySetAssociativeCache[(int)cacheIndex][i] ==0){
                    numberOfCacheMisses++;
                    nWaySetAssociativeCache[(int)cacheIndex][i] = (int)cacheTag;
                    break;
                }
                else if(nWaySetAssociativeCache[(int)cacheIndex][i]!=(int)cacheTag){
                    if(i == ways-1){
                        numberOfCacheMisses++;
                        int minElement = LRUArray[(int) cacheIndex][0];
                        int minIndex = 0;
                        for (int j =0;j<ways;j++){
                            if(LRUArray[(int) cacheIndex][j]<minElement){
                                minIndex = j;
                            }
                        }
                        nWaySetAssociativeCache[(int) cacheIndex][minIndex]= (int) cacheTag;
                        LRUArray[(int) cacheIndex][minIndex]=0;
                    }
                }
                else if(nWaySetAssociativeCache[(int)cacheIndex][i] == cacheTag){
                    numberOfCacheHits++;
                    LRUArray[(int)cacheIndex][i]+=1;
                    break;
                }
            }
        }
    }

    private static void printOutput(){
        System.out.println("*****************************");
        System.out.println("file name: "+memoryTracePath);
        System.out.println("Cache size= "+cacheSize+" KB");
        System.out.println("Block size= "+cacheSize+" B");
        System.out.println("Cache size= "+blockSize);
        if(ways == 0) {
            System.out.println("Fully Associative Cache");
        } else if(ways ==1) {
            System.out.println("Direct Mapped Cache");
        } else if (ways>1){
            System.out.println("Number of ways= "+ ways);
        }
        System.out.println("numOfOffSetBits = " + (32 - (indexBitsLength + tagBitsLength)));
        System.out.println("numOfIndexBits = " + indexBitsLength);
        System.out.println("numOfTagBits = " + tagBitsLength);
        System.out.println();
        System.out.println("Cache hit count = " + numberOfCacheHits);
        System.out.println("Cache miss count = " + numberOfCacheMisses);
        System.out.println("Instruction count = " + instructionsArray.length);
        System.out.println("Cache hit rate = " + ((float) numberOfCacheHits / (float) instructionsArray.length) * 100.0 +" %");
        System.out.println("Cache miss rate = " + ((float) numberOfCacheMisses / (float) instructionsArray.length) * 100.0 +" %");
        System.out.println("*****************************");
    }
}

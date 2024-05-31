# Cache Simulator Implementation

## Overview
This project involves the design and implementation of a cache simulator capable of simulating direct-mapped, set-associative, and fully associative caches. The simulator is designed to track cache hits and misses, computing the cache miss rate, and processes memory instruction traces to simulate LRU cache replacement policies.

## Features
- Supports direct-mapped, set-associative, and fully associative caching strategies.
- Tracks cache hits and misses, calculating the cache miss rate.
- Processes memory instruction traces, simulating LRU cache replacement policies.
- Customizable cache configurations via command-line flags.

## Getting Started
To get started with the Cache Simulator, follow these steps:

### Prerequisites
Ensure you have Java installed on your system. You can check by running `java -version` in your terminal or command prompt.

### Installation
No installation is required. Simply clone or download the repository containing this project.

### Running the Simulator
1. Navigate to the directory containing the `Cache_Simulator.java` file.
2. Compile the program using `javac Cache_Simulator.java`.
3. Run the simulator with the appropriate command-line arguments. For example: `java Cache_Simulator gcc-10K.memtrace 2048 64 1`

## Usage
Replace `<memoryTracePath>`, `<cacheSize>`, `<blockSize>`, and `<ways>` with the desired values when running the simulator. The `<memoryTracePath>` should point to the memory trace file you wish to process.

## Output
The simulator prints out various statistics related to the cache simulation, including:
- Cache hit count
- Cache miss count
- Instruction count
- Cache hit rate (%)
- Cache miss rate (%)

## Technologies
- Java

## Future Work
Consider extending the simulator to support additional cache configurations or to visualize cache operations.

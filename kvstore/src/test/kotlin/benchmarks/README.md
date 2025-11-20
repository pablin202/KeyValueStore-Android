# KeyValueStore Benchmarks

## Overview

This directory contains performance benchmarks for the KeyValueStore implementations (Fake vs Real).

## Benchmark Types

### 1. JMH Benchmarks (`KeyValueStoreBenchmarks.kt`)

Professional-grade benchmarks using the Java Microbenchmark Harness.

**Note:** Full JMH support in Android projects requires additional configuration. These benchmarks are set up but may need a standalone JVM module to run properly.

**Features:**
- Warmup iterations to stabilize JIT
- Multiple measurement iterations
- Statistical analysis
- Various benchmark modes (throughput, average time, etc.)

**To run with JMH (requires additional setup):**
```bash
# If JMH plugin is configured:
./gradlew :kvstore:jmh

# Or generate JAR:
./gradlew :kvstore:jmhJar
java -jar kvstore/build/libs/kvstore-jmh.jar
```

### 2. Simple Benchmark Runner (`SimpleBenchmarkRunner.kt`)

Lightweight benchmarks that run as regular JUnit tests.

**To run:**
```bash
./gradlew :kvstore:test --tests "benchmarks.SimpleBenchmarkRunner"
```

**Available benchmarks:**
- Single operation benchmarks (put, get, contains, remove)
- Bulk operation benchmarks (1000+ keys)
- Large value benchmarks (1MB)
- Clear operation benchmarks

**To run comprehensive suite:**
```bash
./gradlew :kvstore:test --tests "benchmarks.SimpleBenchmarkRunner.comprehensive benchmark suite"
```

## Benchmark Categories

### Write Operations
- `fakePutSingleKey` / `realPutSingleKey` - Single key write
- `fakePut100Keys` / `realPut100Keys` - Batch writes
- `fakePutLargeValue` / `realPutLargeValue` - Large value (100KB)

### Read Operations
- `fakeGetExistingKey` / `realGetExistingKey` - Reading existing keys
- `fakeGetMissingKey` / `realGetMissingKey` - Reading missing keys
- `fakeGet100SequentialKeys` / `realGet100SequentialKeys` - Batch reads

### Mixed Operations
- `fakeMixed100Ops` / `realMixed100Ops` - Realistic workload mix

### Metadata Operations
- `fakeContainsExisting` / `realContainsExisting`
- `fakeRemoveExisting` / `realRemoveExisting`
- `fakeClear1000Keys` / `realClear1000Keys`

### Edge Cases
- Short vs Long keys
- Tiny vs Medium vs Large values
- Repeated overwrites

## Expected Results

Typical performance characteristics:

**FakeKeyValueStore (In-Memory):**
- Put: < 1 µs
- Get: < 0.5 µs
- Contains: < 0.5 µs

**KeyValueStoreImpl (File-Based):**
- Put: 10-50 µs (depends on disk I/O)
- Get: 10-50 µs (depends on disk I/O)
- Contains: 5-20 µs (file exists check)

**Expected Slowdown:** 10-100x (Real vs Fake)

## Interpretation

- **Fake store** represents the theoretical minimum (no I/O overhead)
- **Real store** includes file system overhead
- Ratio helps understand I/O impact
- Use for:
  - Performance regression detection
  - Implementation optimization
  - Capacity planning

## Tips

1. Run benchmarks multiple times for consistency
2. Close other applications to reduce noise
3. Consider disk speed (SSD vs HDD)
4. Warmup is important for JIT compilation
5. Use realistic data sizes for your use case

## Adding New Benchmarks

```kotlin
@Benchmark
fun myNewBenchmark(state: FakeStoreState) = runBlocking {
    // Your benchmark code here
    state.store.put("key", "value".toByteArray())
}
```

Or for simple benchmarks:

```kotlin
@Test
fun `benchmark - my operation`() = runBlocking {
    val time = measureNanos {
        // Operation to measure
    }
    println("Operation took: ${time / 1000.0} µs")
}
```

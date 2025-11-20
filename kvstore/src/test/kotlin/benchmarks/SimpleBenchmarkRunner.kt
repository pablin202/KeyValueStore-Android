package benchmarks

import com.pdm.kvstore.api.KVConfig
import com.pdm.kvstore.api.KeyValueStore
import kotlinx.coroutines.runBlocking
import org.junit.Test
import store.FakeKeyValueStore
import java.io.File

/**
 * Simple benchmark runner that can be executed as JUnit tests.
 * This is an alternative to full JMH when running in Android environment.
 *
 * Run these tests to get approximate performance measurements.
 */
class SimpleBenchmarkRunner {

    private fun measureNanos(iterations: Int = 1000, block: () -> Unit): Long {
        val times = mutableListOf<Long>()

        // Warmup
        repeat(100) { block() }

        // Actual measurements
        repeat(iterations) {
            val start = System.nanoTime()
            block()
            val end = System.nanoTime()
            times.add(end - start)
        }

        return times.average().toLong()
    }

    @Test
    fun `benchmark - fake vs real put operations`() = runBlocking {
        val fakeStore = FakeKeyValueStore()
        val dir = createTempDir()
        val realStore = KeyValueStore.create(KVConfig(directory = dir))

        val fakeTime = measureNanos {
            runBlocking { fakeStore.put("key", "value".toByteArray()) }
        }

        val realTime = measureNanos {
            runBlocking { realStore.put("key", "value".toByteArray()) }
        }

        println("=== PUT BENCHMARK ===")
        println("Fake store: ${fakeTime / 1000.0} µs per operation")
        println("Real store: ${realTime / 1000.0} µs per operation")
        println("Slowdown: ${realTime.toDouble() / fakeTime}x")
        println()

        fakeStore.close()
        realStore.close()
        dir.deleteRecursively()
    }

    @Test
    fun `benchmark - fake vs real get operations`() = runBlocking {
        val fakeStore = FakeKeyValueStore()
        val dir = createTempDir()
        val realStore = KeyValueStore.create(KVConfig(directory = dir))

        // Setup
        fakeStore.put("key", "value".toByteArray())
        realStore.put("key", "value".toByteArray())

        val fakeTime = measureNanos {
            runBlocking { fakeStore.get("key") }
        }

        val realTime = measureNanos {
            runBlocking { realStore.get("key") }
        }

        println("=== GET BENCHMARK ===")
        println("Fake store: ${fakeTime / 1000.0} µs per operation")
        println("Real store: ${realTime / 1000.0} µs per operation")
        println("Slowdown: ${realTime.toDouble() / fakeTime}x")
        println()

        fakeStore.close()
        realStore.close()
        dir.deleteRecursively()
    }

    @Test
    fun `benchmark - bulk writes 1000 keys`() = runBlocking {
        val fakeStore = FakeKeyValueStore()
        val dir = createTempDir()
        val realStore = KeyValueStore.create(KVConfig(directory = dir))

        val fakeStart = System.nanoTime()
        repeat(1000) { i ->
            fakeStore.put("key_$i", "value_$i".toByteArray())
        }
        val fakeTime = System.nanoTime() - fakeStart

        val realStart = System.nanoTime()
        repeat(1000) { i ->
            realStore.put("key_$i", "value_$i".toByteArray())
        }
        val realTime = System.nanoTime() - realStart

        println("=== BULK WRITE 1000 KEYS ===")
        println("Fake store: ${fakeTime / 1_000_000.0} ms total, ${fakeTime / 1000 / 1000.0} µs per key")
        println("Real store: ${realTime / 1_000_000.0} ms total, ${realTime / 1000 / 1000.0} µs per key")
        println("Slowdown: ${realTime.toDouble() / fakeTime}x")
        println()

        fakeStore.close()
        realStore.close()
        dir.deleteRecursively()
    }

    @Test
    fun `benchmark - bulk reads 1000 keys`() = runBlocking {
        val fakeStore = FakeKeyValueStore()
        val dir = createTempDir()
        val realStore = KeyValueStore.create(KVConfig(directory = dir))

        // Setup
        repeat(1000) { i ->
            fakeStore.put("key_$i", "value_$i".toByteArray())
            realStore.put("key_$i", "value_$i".toByteArray())
        }

        val fakeStart = System.nanoTime()
        repeat(1000) { i ->
            fakeStore.get("key_$i")
        }
        val fakeTime = System.nanoTime() - fakeStart

        val realStart = System.nanoTime()
        repeat(1000) { i ->
            realStore.get("key_$i")
        }
        val realTime = System.nanoTime() - realStart

        println("=== BULK READ 1000 KEYS ===")
        println("Fake store: ${fakeTime / 1_000_000.0} ms total, ${fakeTime / 1000 / 1000.0} µs per key")
        println("Real store: ${realTime / 1_000_000.0} ms total, ${realTime / 1000 / 1000.0} µs per key")
        println("Slowdown: ${realTime.toDouble() / fakeTime}x")
        println()

        fakeStore.close()
        realStore.close()
        dir.deleteRecursively()
    }

    @Test
    fun `benchmark - large value writes (1MB)`() = runBlocking {
        val fakeStore = FakeKeyValueStore()
        val dir = createTempDir()
        val realStore = KeyValueStore.create(KVConfig(directory = dir))

        val largeValue = ByteArray(1024 * 1024) { it.toByte() } // 1MB

        val fakeTime = measureNanos(iterations = 100) {
            runBlocking { fakeStore.put("large", largeValue) }
        }

        val realTime = measureNanos(iterations = 100) {
            runBlocking { realStore.put("large", largeValue) }
        }

        println("=== LARGE VALUE (1MB) WRITE ===")
        println("Fake store: ${fakeTime / 1000.0} µs per operation")
        println("Real store: ${realTime / 1000.0} µs per operation")
        println("Slowdown: ${realTime.toDouble() / fakeTime}x")
        println()

        fakeStore.close()
        realStore.close()
        dir.deleteRecursively()
    }

    @Test
    fun `benchmark - contains operation`() = runBlocking {
        val fakeStore = FakeKeyValueStore()
        val dir = createTempDir()
        val realStore = KeyValueStore.create(KVConfig(directory = dir))

        // Setup
        repeat(100) { i ->
            fakeStore.put("key_$i", "value_$i".toByteArray())
            realStore.put("key_$i", "value_$i".toByteArray())
        }

        val fakeTime = measureNanos {
            runBlocking { fakeStore.contains("key_50") }
        }

        val realTime = measureNanos {
            runBlocking { realStore.contains("key_50") }
        }

        println("=== CONTAINS BENCHMARK ===")
        println("Fake store: ${fakeTime / 1000.0} µs per operation")
        println("Real store: ${realTime / 1000.0} µs per operation")
        println("Slowdown: ${realTime.toDouble() / fakeTime}x")
        println()

        fakeStore.close()
        realStore.close()
        dir.deleteRecursively()
    }

    @Test
    fun `benchmark - remove operation`() = runBlocking {
        val fakeStore = FakeKeyValueStore()
        val dir = createTempDir()
        val realStore = KeyValueStore.create(KVConfig(directory = dir))

        val fakeTime = measureNanos {
            runBlocking {
                fakeStore.put("temp_key", "value".toByteArray())
                fakeStore.remove("temp_key")
            }
        }

        val realTime = measureNanos {
            runBlocking {
                realStore.put("temp_key", "value".toByteArray())
                realStore.remove("temp_key")
            }
        }

        println("=== REMOVE BENCHMARK ===")
        println("Fake store: ${fakeTime / 1000.0} µs per operation (put + remove)")
        println("Real store: ${realTime / 1000.0} µs per operation (put + remove)")
        println("Slowdown: ${realTime.toDouble() / fakeTime}x")
        println()

        fakeStore.close()
        realStore.close()
        dir.deleteRecursively()
    }

    @Test
    fun `benchmark - clear operation with 1000 keys`() = runBlocking {
        val fakeStore = FakeKeyValueStore()
        val dir = createTempDir()
        val realStore = KeyValueStore.create(KVConfig(directory = dir))

        // Setup
        repeat(1000) { i ->
            fakeStore.put("key_$i", "value_$i".toByteArray())
            realStore.put("key_$i", "value_$i".toByteArray())
        }

        val fakeStart = System.nanoTime()
        fakeStore.clear()
        val fakeTime = System.nanoTime() - fakeStart

        val realStart = System.nanoTime()
        realStore.clear()
        val realTime = System.nanoTime() - realStart

        println("=== CLEAR 1000 KEYS ===")
        println("Fake store: ${fakeTime / 1_000_000.0} ms")
        println("Real store: ${realTime / 1_000_000.0} ms")
        println("Slowdown: ${realTime.toDouble() / fakeTime}x")
        println()

        fakeStore.close()
        realStore.close()
        dir.deleteRecursively()
    }

    @Test
    fun `comprehensive benchmark suite`() = runBlocking {
        println("\n")
        println("=" * 60)
        println("COMPREHENSIVE BENCHMARK SUITE")
        println("=" * 60)
        println()

        `benchmark - fake vs real put operations`()
        `benchmark - fake vs real get operations`()
        `benchmark - bulk writes 1000 keys`()
        `benchmark - bulk reads 1000 keys`()
        `benchmark - large value writes (1MB)`()
        `benchmark - contains operation`()
        `benchmark - remove operation`()
        `benchmark - clear operation with 1000 keys`()

        println("=" * 60)
        println("BENCHMARK SUITE COMPLETED")
        println("=" * 60)
    }

    private operator fun String.times(n: Int): String = repeat(n)
}

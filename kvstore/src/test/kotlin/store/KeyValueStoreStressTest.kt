package store

import TestDispatcherRule
import com.pdm.kvstore.api.KVConfig
import com.pdm.kvstore.api.KeyValueStore
import com.pdm.kvstore.api.KVResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.random.Random

class KeyValueStoreStressTest {

    @get:Rule
    val dispatcher = TestDispatcherRule()

    private fun newStore(): KeyValueStore {
        val dir = createTempDir()
        return KeyValueStore.create(KVConfig(directory = dir))
    }

    // ---------------------------
    // STRESS TESTS - 10,000 OPERATIONS
    // ---------------------------

    @Test
    fun `stress test - 10000 sequential writes`() = runTest {
        val store = newStore()

        repeat(10_000) { i ->
            val result = store.put("key_$i", "value_$i".toByteArray())
            assertTrue("Write $i failed", result is KVResult.Ok)
        }

        // Verify some random entries
        val samplesToCheck = listOf(0, 100, 500, 1000, 5000, 9999)
        samplesToCheck.forEach { i ->
            val result = store.get("key_$i")
            assertTrue(result is KVResult.Ok)
            assertEquals("value_$i", String((result as KVResult.Ok).value))
        }
    }

    @Test
    fun `stress test - 10000 sequential reads on same key`() = runTest {
        val store = newStore()
        val value = "stress_value".toByteArray()

        store.put("stress_key", value)

        repeat(10_000) { i ->
            val result = store.get("stress_key")
            assertTrue("Read $i failed", result is KVResult.Ok)
            assertArrayEquals("Read $i returned wrong value", value, (result as KVResult.Ok).value)
        }
    }

    @Test
    fun `stress test - 10000 mixed operations`() = runTest {
        val store = newStore()
        val random = Random(42)
        var putCount = 0
        var getCount = 0
        var removeCount = 0

        repeat(10_000) { i ->
            when (random.nextInt(3)) {
                0 -> {
                    store.put("key_${i % 1000}", "value_$i".toByteArray())
                    putCount++
                }
                1 -> {
                    store.get("key_${i % 1000}")
                    getCount++
                }
                2 -> {
                    store.remove("key_${i % 1000}")
                    removeCount++
                }
            }
        }

        assertTrue("Expected puts", putCount > 0)
        assertTrue("Expected gets", getCount > 0)
        assertTrue("Expected removes", removeCount > 0)
    }

    @Test
    fun `stress test - 1000 concurrent writes different keys`() = runTest {
        val store = newStore()

        val jobs = (0 until 1000).map { i ->
            async {
                store.put("concurrent_$i", "value_$i".toByteArray())
            }
        }

        val results = jobs.awaitAll()
        assertTrue("All writes should succeed", results.all { it is KVResult.Ok })

        // Verify all keys exist
        repeat(1000) { i ->
            val result = store.get("concurrent_$i")
            assertTrue("Key concurrent_$i should exist", result is KVResult.Ok)
        }
    }

    @Test
    fun `stress test - 1000 concurrent writes to same key`() = runTest {
        val store = newStore()

        val jobs = (0 until 1000).map { i ->
            async {
                store.put("same_key", "value_$i".toByteArray())
            }
        }

        jobs.awaitAll()

        // Should have one of the values
        val result = store.get("same_key")
        assertTrue(result is KVResult.Ok)
        val value = String((result as KVResult.Ok).value)
        assertTrue("Value should match pattern", value.matches(Regex("value_\\d+")))
    }

    @Test
    fun `stress test - large values 1MB each`() = runTest {
        val store = newStore()
        val largeValue = ByteArray(1024 * 1024) { it.toByte() } // 1MB

        repeat(10) { i ->
            val result = store.put("large_$i", largeValue)
            assertTrue("Large write $i failed", result is KVResult.Ok)
        }

        repeat(10) { i ->
            val result = store.get("large_$i")
            assertTrue("Large read $i failed", result is KVResult.Ok)
            assertEquals("Size mismatch", largeValue.size, (result as KVResult.Ok).value.size)
        }
    }

    @Test
    fun `stress test - key churn (write then delete repeatedly)`() = runTest {
        val store = newStore()

        repeat(1000) { i ->
            val key = "churn_${i % 100}" // Reuse 100 keys

            store.put(key, "value_$i".toByteArray())
            val getResult = store.get(key)
            assertTrue(getResult is KVResult.Ok)

            store.remove(key)
            val afterRemove = store.get(key)
            assertTrue(afterRemove is KVResult.Err)
        }
    }

    @Test
    fun `stress test - many small keys`() = runTest {
        val store = newStore()

        repeat(10_000) { i ->
            store.put("k$i", "v".toByteArray())
        }

        // Spot check
        val samples = (0 until 10_000 step 500).toList()
        samples.forEach { i ->
            val result = store.get("k$i")
            assertTrue(result is KVResult.Ok)
            assertEquals("v", String((result as KVResult.Ok).value))
        }
    }

    @Test
    fun `stress test - clear with 1000 keys`() = runTest {
        val store = newStore()

        repeat(1000) { i ->
            store.put("key_$i", "value_$i".toByteArray())
        }

        val clearResult = store.clear()
        assertTrue(clearResult is KVResult.Ok)

        // Verify all keys are gone
        repeat(1000) { i ->
            val result = store.contains("key_$i")
            assertTrue(result is KVResult.Ok)
            assertFalse((result as KVResult.Ok).value)
        }
    }

    @Test
    fun `stress test - alternating put and get on 1000 keys`() = runTest {
        val store = newStore()

        repeat(1000) { i ->
            val putResult = store.put("alternate_$i", "value_$i".toByteArray())
            assertTrue(putResult is KVResult.Ok)

            val getResult = store.get("alternate_$i")
            assertTrue(getResult is KVResult.Ok)
            assertEquals("value_$i", String((getResult as KVResult.Ok).value))
        }
    }
}

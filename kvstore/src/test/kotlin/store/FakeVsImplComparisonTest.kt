package store

import TestDispatcherRule
import com.pdm.kvstore.api.KVConfig
import com.pdm.kvstore.api.KeyValueStore
import com.pdm.kvstore.api.KVResult
import com.pdm.kvstore.errors.KVError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/**
 * Tests comparing behavior between FakeKeyValueStore (in-memory)
 * and KeyValueStoreImpl (file-based).
 *
 * This ensures contract compliance and helps catch discrepancies.
 */
class FakeVsImplComparisonTest {

    @get:Rule
    val dispatcher = TestDispatcherRule()

    private fun createFakeStore(): KeyValueStore = FakeKeyValueStore()

    private fun createRealStore(): KeyValueStore {
        val dir = createTempDir()
        return KeyValueStore.create(KVConfig(directory = dir))
    }

    private fun runBothStores(test: suspend (KeyValueStore) -> Unit) = runTest {
        test(createFakeStore())
        test(createRealStore())
    }

    // ---------------------------
    // BEHAVIORAL COMPARISON TESTS
    // ---------------------------

    @Test
    fun `both stores - put and get basic value`() = runBothStores { store ->
        val value = "test_value".toByteArray()
        val putResult = store.put("key", value)
        assertTrue(putResult is KVResult.Ok)

        val getResult = store.get("key")
        assertTrue(getResult is KVResult.Ok)
        assertArrayEquals(value, (getResult as KVResult.Ok).value)
    }

    @Test
    fun `both stores - get missing key returns KeyNotFound`() = runBothStores { store ->
        val result = store.get("nonexistent")
        assertTrue(result is KVResult.Err)
        assertTrue((result as KVResult.Err).error is KVError.KeyNotFound)
    }

    @Test
    fun `both stores - put overwrites existing value`() = runBothStores { store ->
        store.put("key", "first".toByteArray())
        store.put("key", "second".toByteArray())

        val result = store.get("key")
        assertTrue(result is KVResult.Ok)
        assertEquals("second", String((result as KVResult.Ok).value))
    }

    @Test
    fun `both stores - remove existing key`() = runBothStores { store ->
        store.put("key", "value".toByteArray())

        val removeResult = store.remove("key")
        assertTrue(removeResult is KVResult.Ok)

        val getResult = store.get("key")
        assertTrue(getResult is KVResult.Err)
        assertTrue((getResult as KVResult.Err).error is KVError.KeyNotFound)
    }

    @Test
    fun `both stores - remove nonexistent key returns error`() = runBothStores { store ->
        val result = store.remove("nonexistent")
        assertTrue(result is KVResult.Err)
        assertTrue((result as KVResult.Err).error is KVError.KeyNotFound)
    }

    @Test
    fun `both stores - contains returns true for existing key`() = runBothStores { store ->
        store.put("key", "value".toByteArray())

        val result = store.contains("key")
        assertTrue(result is KVResult.Ok)
        assertTrue((result as KVResult.Ok).value)
    }

    @Test
    fun `both stores - contains returns false for missing key`() = runBothStores { store ->
        val result = store.contains("missing")
        assertTrue(result is KVResult.Ok)
        assertFalse((result as KVResult.Ok).value)
    }

    @Test
    fun `both stores - clear removes all keys`() = runBothStores { store ->
        store.put("key1", "value1".toByteArray())
        store.put("key2", "value2".toByteArray())
        store.put("key3", "value3".toByteArray())

        val clearResult = store.clear()
        assertTrue(clearResult is KVResult.Ok)

        assertTrue((store.contains("key1") as KVResult.Ok).value == false)
        assertTrue((store.contains("key2") as KVResult.Ok).value == false)
        assertTrue((store.contains("key3") as KVResult.Ok).value == false)
    }

    @Test
    fun `both stores - operations after close return ClosedStore error`() = runBothStores { store ->
        store.put("key", "value".toByteArray())
        store.close()

        assertTrue(store.isClosed())

        val getResult = store.get("key")
        assertTrue(getResult is KVResult.Err)
        assertTrue((getResult as KVResult.Err).error is KVError.ClosedStore)

        val putResult = store.put("key2", "value2".toByteArray())
        assertTrue(putResult is KVResult.Err)
        assertTrue((putResult as KVResult.Err).error is KVError.ClosedStore)
    }

    @Test
    fun `both stores - empty byte array value`() = runBothStores { store ->
        val emptyValue = ByteArray(0)
        store.put("empty", emptyValue)

        val result = store.get("empty")
        assertTrue(result is KVResult.Ok)
        assertEquals(0, (result as KVResult.Ok).value.size)
    }

    @Test
    fun `both stores - large value storage`() = runBothStores { store ->
        val largeValue = ByteArray(10000) { it.toByte() }
        store.put("large", largeValue)

        val result = store.get("large")
        assertTrue(result is KVResult.Ok)
        assertArrayEquals(largeValue, (result as KVResult.Ok).value)
    }

    @Test
    fun `both stores - multiple sequential operations`() = runBothStores { store ->
        // Put
        store.put("seq", "v1".toByteArray())
        assertEquals("v1", String((store.get("seq") as KVResult.Ok).value))

        // Update
        store.put("seq", "v2".toByteArray())
        assertEquals("v2", String((store.get("seq") as KVResult.Ok).value))

        // Contains
        assertTrue((store.contains("seq") as KVResult.Ok).value)

        // Remove
        store.remove("seq")
        assertFalse((store.contains("seq") as KVResult.Ok).value)
    }

    @Test
    fun `both stores - binary data with null bytes`() = runBothStores { store ->
        val binaryData = byteArrayOf(0x00, 0x01, 0x02, 0x00, 0xFF.toByte())
        store.put("binary", binaryData)

        val result = store.get("binary")
        assertTrue(result is KVResult.Ok)
        assertArrayEquals(binaryData, (result as KVResult.Ok).value)
    }

    @Test
    fun `both stores - unicode key support`() = runBothStores { store ->
        val unicodeKey = "emoji_🔥_key_中文"
        store.put(unicodeKey, "unicode_value".toByteArray())

        val result = store.get(unicodeKey)
        assertTrue(result is KVResult.Ok)
        assertEquals("unicode_value", String((result as KVResult.Ok).value))
    }

    @Test
    fun `both stores - isClosed state transitions`() = runBothStores { store ->
        assertFalse("Should not be closed initially", store.isClosed())

        store.put("key", "value".toByteArray())
        assertFalse("Should not be closed after operation", store.isClosed())

        store.close()
        assertTrue("Should be closed after close()", store.isClosed())
    }

    // ---------------------------
    // EDGE CASES
    // ---------------------------

    @Test
    fun `both stores - very long key names`() = runBothStores { store ->
        val longKey = "k".repeat(1000)
        store.put(longKey, "value".toByteArray())

        val result = store.get(longKey)
        assertTrue(result is KVResult.Ok)
        assertEquals("value", String((result as KVResult.Ok).value))
    }

    @Test
    fun `both stores - special characters in keys`() = runBothStores { store ->
        val specialKeys = listOf(
            "key/with/slashes",
            "key\\with\\backslashes",
            "key with spaces",
            "key\twith\ttabs",
            "key.with.dots"
        )

        specialKeys.forEach { key ->
            store.put(key, "value".toByteArray())
            val result = store.get(key)
            assertTrue("Failed for key: $key", result is KVResult.Ok)
        }
    }

    @Test
    fun `both stores - clear on empty store`() = runBothStores { store ->
        val result = store.clear()
        assertTrue(result is KVResult.Ok)
    }

    @Test
    fun `both stores - multiple puts of same key are idempotent on final value`() = runBothStores { store ->
        repeat(100) {
            store.put("same", "final".toByteArray())
        }

        val result = store.get("same")
        assertTrue(result is KVResult.Ok)
        assertEquals("final", String((result as KVResult.Ok).value))
    }

    // ---------------------------
    // PERFORMANCE COMPARISON (not strict assertions, just measurements)
    // ---------------------------

    @Test
    fun `performance comparison - 1000 writes`() = runTest {
        val fake = createFakeStore()
        val real = createRealStore()

        val fakeTime = measureTimeMillis {
            repeat(1000) { i ->
                fake.put("key_$i", "value_$i".toByteArray())
            }
        }

        val realTime = measureTimeMillis {
            repeat(1000) { i ->
                real.put("key_$i", "value_$i".toByteArray())
            }
        }

        println("Fake store 1000 writes: ${fakeTime}ms")
        println("Real store 1000 writes: ${realTime}ms")
        println("Real/Fake ratio: ${realTime.toDouble() / fakeTime}")

        // No assertions, just informational
    }

    @Test
    fun `performance comparison - 1000 reads`() = runTest {
        val fake = createFakeStore()
        val real = createRealStore()

        // Setup data
        repeat(1000) { i ->
            fake.put("key_$i", "value_$i".toByteArray())
            real.put("key_$i", "value_$i".toByteArray())
        }

        val fakeTime = measureTimeMillis {
            repeat(1000) { i ->
                fake.get("key_$i")
            }
        }

        val realTime = measureTimeMillis {
            repeat(1000) { i ->
                real.get("key_$i")
            }
        }

        println("Fake store 1000 reads: ${fakeTime}ms")
        println("Real store 1000 reads: ${realTime}ms")
        println("Real/Fake ratio: ${realTime.toDouble() / fakeTime}")
    }

    // ---------------------------
    // HELPER
    // ---------------------------

    private inline fun measureTimeMillis(block: () -> Unit): Long {
        val start = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - start
    }
}

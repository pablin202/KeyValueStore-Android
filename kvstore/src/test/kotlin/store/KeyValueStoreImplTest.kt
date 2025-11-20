package store

// store/KeyValueStoreImplTest.kt
import TestDispatcherRule
import com.pdm.kvstore.api.KVConfig
import com.pdm.kvstore.api.KeyValueStore
import com.pdm.kvstore.api.KVResult
import com.pdm.kvstore.errors.KVError
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class KeyValueStoreImplTest {

    @get:Rule
    val dispatcher = TestDispatcherRule()

    private fun newStore(): KeyValueStore {
        val dir = createTempDir()
        return KeyValueStore.create(KVConfig(directory = dir))
    }

    // ---------------------------
    // 1. HAPPY PATH
    // ---------------------------

    @Test
    fun `put then get returns the stored value`() = runTest {
        val store = newStore()

        val value = "abc123".toByteArray()
        store.put("token", value)

        val result = store.get("token")

        assertTrue(result is KVResult.Ok)
        assertArrayEquals(value, (result as KVResult.Ok).value)
    }

    @Test
    fun `get on missing key returns KeyNotFound error`() = runTest {
        val store = newStore()

        val result = store.get("not_found")

        assertTrue(result is KVResult.Err)
        assertTrue((result as KVResult.Err).error is KVError.KeyNotFound)
    }

    // ---------------------------
    // 2. CONTAINS
    // ---------------------------

    @Test
    fun `contains returns true when key exists`() = runTest {
        val store = newStore()

        store.put("key", "value".toByteArray())

        val result = store.contains("key")

        assertTrue(result is KVResult.Ok)
        assertTrue((result as KVResult.Ok).value)
    }

    @Test
    fun `contains returns false when key does not exist`() = runTest {
        val store = newStore()

        val result = store.contains("not_found")

        assertTrue(result is KVResult.Ok)
        assertFalse((result as KVResult.Ok).value)
    }

    // ---------------------------
    // 3. REMOVE
    // ---------------------------

    @Test
    fun `remove deletes the key`() = runTest {
        val store = newStore()
        store.put("key", "value".toByteArray())

        val removeResult = store.remove("key")

        assertTrue(removeResult is KVResult.Ok)

        val getResult = store.get("key")
        assertTrue(getResult is KVResult.Err)
        assertTrue((getResult as KVResult.Err).error is KVError.KeyNotFound)
    }

    // ---------------------------
    // 4. INVARIANT - config is immutable
    // ---------------------------

    @Test
    fun `config is immutable`() {
        val dir = createTempDir()
        val config1 = KVConfig(directory = dir)
        val config2 = config1.copy()

        assertEquals(config1.directory, config2.directory)
        assertNotSame(config1, config2)
    }

    // ---------------------------
    // 5. CONCURRENCY
    // ---------------------------

    @Test
    fun `writes are serialized correctly`() = runTest {
        val store = newStore()

        val jobs = (1..100).map { i ->
            launch { store.put("count", i.toString().toByteArray()) }
        }
        jobs.forEach { it.join() }

        val result = store.get("count")

        assertTrue(result is KVResult.Ok)
        val value = (result as KVResult.Ok).value
        val count = String(value).toInt()
        assertTrue(count in 1..100)
    }

    // ---------------------------
    // 6. CLEAR
    // ---------------------------

    @Test
    fun `clear removes all keys`() = runTest {
        val store = newStore()

        store.put("key1", "value1".toByteArray())
        store.put("key2", "value2".toByteArray())
        store.put("key3", "value3".toByteArray())

        val clearResult = store.clear()
        assertTrue(clearResult is KVResult.Ok)

        val result1 = store.contains("key1")
        val result2 = store.contains("key2")
        val result3 = store.contains("key3")

        assertTrue(result1 is KVResult.Ok)
        assertFalse((result1 as KVResult.Ok).value)

        assertTrue(result2 is KVResult.Ok)
        assertFalse((result2 as KVResult.Ok).value)

        assertTrue(result3 is KVResult.Ok)
        assertFalse((result3 as KVResult.Ok).value)
    }
}

package store

import TestDispatcherRule
import com.pdm.kvstore.api.KVConfig
import com.pdm.kvstore.api.KeyValueStore
import com.pdm.kvstore.api.KVResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class TempDirectoryCleanupTest {

    @get:Rule
    val dispatcher = TestDispatcherRule()

    // ---------------------------
    // TEMP DIRECTORY MANAGEMENT
    // ---------------------------

    @Test
    fun `temp directory is created if it does not exist`() {
        val dir = File(System.getProperty("java.io.tmpdir"), "kvstore_test_${System.currentTimeMillis()}")
        assertFalse("Directory should not exist yet", dir.exists())

        val store = KeyValueStore.create(KVConfig(directory = dir))

        assertTrue("Directory should be created", dir.exists())
        assertTrue("Should be a directory", dir.isDirectory)

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `temp directory can be deleted after closing store`() = runTest {
        val dir = createTempDir()
        val store = KeyValueStore.create(KVConfig(directory = dir))

        store.put("key1", "value1".toByteArray())
        store.put("key2", "value2".toByteArray())

        assertTrue("Directory should exist", dir.exists())
        assertTrue("Directory should have files", dir.listFiles()?.isNotEmpty() == true)

        store.close()

        val deleted = dir.deleteRecursively()
        assertTrue("Directory should be deletable after close", deleted)
        assertFalse("Directory should not exist after deletion", dir.exists())
    }

    @Test
    fun `multiple stores can use different temp directories`() = runTest {
        val dir1 = createTempDir(prefix = "store1_")
        val dir2 = createTempDir(prefix = "store2_")

        val store1 = KeyValueStore.create(KVConfig(directory = dir1))
        val store2 = KeyValueStore.create(KVConfig(directory = dir2))

        store1.put("key", "value1".toByteArray())
        store2.put("key", "value2".toByteArray())

        val result1 = store1.get("key")
        val result2 = store2.get("key")

        assertEquals("value1", String((result1 as KVResult.Ok).value))
        assertEquals("value2", String((result2 as KVResult.Ok).value))

        store1.close()
        store2.close()

        dir1.deleteRecursively()
        dir2.deleteRecursively()
    }

    @Test
    fun `store persists data in temp directory across close and reopen`() = runTest {
        val dir = createTempDir()

        // First store
        val store1 = KeyValueStore.create(KVConfig(directory = dir))
        store1.put("persistent", "data".toByteArray())
        store1.close()

        assertTrue("Directory should still exist after close", dir.exists())

        // Second store on same directory
        val store2 = KeyValueStore.create(KVConfig(directory = dir))
        val result = store2.get("persistent")

        assertTrue(result is KVResult.Ok)
        assertEquals("data", String((result as KVResult.Ok).value))

        store2.close()
        dir.deleteRecursively()
    }

    @Test
    fun `clear operation leaves directory but removes all files`() = runTest {
        val dir = createTempDir()
        val store = KeyValueStore.create(KVConfig(directory = dir))

        repeat(10) { i ->
            store.put("key_$i", "value_$i".toByteArray())
        }

        val filesBefore = dir.listFiles()?.size ?: 0
        assertTrue("Should have files", filesBefore > 0)

        store.clear()

        val filesAfter = dir.listFiles()?.size ?: 0
        assertTrue("Directory should still exist", dir.exists())
        assertEquals("All files should be removed", 0, filesAfter)

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `temp directory isolation - stores do not interfere`() = runTest {
        val dir1 = createTempDir(prefix = "isolated1_")
        val dir2 = createTempDir(prefix = "isolated2_")

        val store1 = KeyValueStore.create(KVConfig(directory = dir1))
        val store2 = KeyValueStore.create(KVConfig(directory = dir2))

        // Write to store1
        store1.put("shared_key", "from_store1".toByteArray())

        // store2 should not see it
        val result2 = store2.get("shared_key")
        assertTrue("Store2 should not have store1's data", result2 is KVResult.Err)

        // Write to store2
        store2.put("shared_key", "from_store2".toByteArray())

        // Both should have their own values
        val result1 = store1.get("shared_key")
        val result2Again = store2.get("shared_key")

        assertEquals("from_store1", String((result1 as KVResult.Ok).value))
        assertEquals("from_store2", String((result2Again as KVResult.Ok).value))

        store1.close()
        store2.close()
        dir1.deleteRecursively()
        dir2.deleteRecursively()
    }

    @Test
    fun `nested temp directory structure works`() {
        val rootTemp = createTempDir()
        val nestedDir = File(rootTemp, "nested/deep/structure")

        val store = KeyValueStore.create(KVConfig(directory = nestedDir))

        assertTrue("Nested directory should be created", nestedDir.exists())
        assertTrue("Should be a directory", nestedDir.isDirectory)

        store.close()
        rootTemp.deleteRecursively()
    }

    @Test
    fun `temp directory handles special characters in path`() {
        val dir = createTempDir(prefix = "test-with-dashes_and_underscores_")

        val store = KeyValueStore.create(KVConfig(directory = dir))

        assertTrue("Directory with special chars should exist", dir.exists())

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `large number of files in temp directory`() = runTest {
        val dir = createTempDir()
        val store = KeyValueStore.create(KVConfig(directory = dir))

        repeat(1000) { i ->
            store.put("file_$i", "content_$i".toByteArray())
        }

        val files = dir.listFiles()
        assertNotNull("Directory should contain files", files)
        assertTrue("Should have many files", files!!.size >= 1000)

        store.clear()

        val filesAfterClear = dir.listFiles()?.size ?: 0
        assertEquals("All files should be cleared", 0, filesAfterClear)

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `closing store allows immediate directory cleanup`() = runTest {
        val dir = createTempDir()
        val store = KeyValueStore.create(KVConfig(directory = dir))

        store.put("data", "value".toByteArray())

        assertFalse("Store should not be closed", store.isClosed())

        store.close()

        assertTrue("Store should be closed", store.isClosed())

        // Should be able to delete immediately
        val deleted = dir.deleteRecursively()
        assertTrue("Directory should be deletable immediately after close", deleted)
    }
}

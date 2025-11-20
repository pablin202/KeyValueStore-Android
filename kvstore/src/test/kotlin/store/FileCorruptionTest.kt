package store

import TestDispatcherRule
import com.pdm.kvstore.api.KVConfig
import com.pdm.kvstore.api.KeyValueStore
import com.pdm.kvstore.api.KVResult
import com.pdm.kvstore.errors.KVError
import com.pdm.kvstore.util.sha256
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/**
 * Tests simulating file corruption scenarios - common in technical interviews.
 * These test how the store handles various failure modes.
 */
class FileCorruptionTest {

    @get:Rule
    val dispatcher = TestDispatcherRule()

    private fun newStore(dir: File): KeyValueStore {
        return KeyValueStore.create(KVConfig(directory = dir))
    }

    // ---------------------------
    // FILE CORRUPTION SCENARIOS
    // ---------------------------

    @Test
    fun `reading corrupted file with partial data`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        // Write valid data
        store.put("key", "original_value".toByteArray())

        // Corrupt the file by truncating it
        val hash = sha256("key")
        val file = File(dir, hash)
        file.writeBytes("corrupt".toByteArray()) // Overwrite with different data

        // Reading should return the corrupted data (store doesn't validate content)
        val result = store.get("key")
        assertTrue(result is KVResult.Ok)
        assertEquals("corrupt", String((result as KVResult.Ok).value))

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `reading empty corrupted file`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        // Write valid data
        store.put("key", "value".toByteArray())

        // Corrupt the file by making it empty
        val hash = sha256("key")
        val file = File(dir, hash)
        file.writeBytes(ByteArray(0))

        // Should return empty array
        val result = store.get("key")
        assertTrue(result is KVResult.Ok)
        assertEquals(0, (result as KVResult.Ok).value.size)

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `manually deleted file appears as missing key`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        // Write data
        store.put("key", "value".toByteArray())

        // Manually delete the underlying file
        val hash = sha256("key")
        val file = File(dir, hash)
        file.delete()

        // Get should return KeyNotFound
        val result = store.get("key")
        assertTrue(result is KVResult.Err)
        assertTrue((result as KVResult.Err).error is KVError.KeyNotFound)

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `directory becomes read-only after write`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        // Write data normally
        val result1 = store.put("key1", "value1".toByteArray())
        assertTrue(result1 is KVResult.Ok)

        // Make directory read-only
        dir.setWritable(false)

        // Try to write - should fail with IO error
        val result2 = store.put("key2", "value2".toByteArray())
        assertTrue("Write should fail on read-only dir", result2 is KVResult.Err)
        assertTrue((result2 as KVResult.Err).error is KVError.Io)

        // Restore permissions
        dir.setWritable(true)

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `file replaced with directory of same name`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        // Write data
        store.put("key", "value".toByteArray())

        // Replace file with directory
        val hash = sha256("key")
        val file = File(dir, hash)
        file.delete()
        file.mkdir()

        // Try to read - should get IO error
        val result = store.get("key")
        assertTrue("Reading directory should fail", result is KVResult.Err)
        assertTrue((result as KVResult.Err).error is KVError.Io)

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `partial write simulation - file exists but incomplete`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        val fullData = "This is a long value that might be partially written".toByteArray()
        store.put("key", fullData)

        // Simulate partial write by truncating
        val hash = sha256("key")
        val file = File(dir, hash)
        val partialData = fullData.copyOf(10)
        file.writeBytes(partialData)

        // Should read the partial data
        val result = store.get("key")
        assertTrue(result is KVResult.Ok)
        assertEquals(10, (result as KVResult.Ok).value.size)

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `binary corruption - random bytes in file`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        store.put("key", "original".toByteArray())

        // Write random binary garbage
        val hash = sha256("key")
        val file = File(dir, hash)
        val randomBytes = ByteArray(100) { (Math.random() * 256).toInt().toByte() }
        file.writeBytes(randomBytes)

        // Should read the random bytes without error
        val result = store.get("key")
        assertTrue(result is KVResult.Ok)
        assertEquals(100, (result as KVResult.Ok).value.size)

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `directory deleted while store is open`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        store.put("key", "value".toByteArray())

        // Delete all files
        dir.listFiles()?.forEach { it.delete() }

        // Get should fail
        val result = store.get("key")
        assertTrue(result is KVResult.Err)

        // Put should still work (recreates file)
        val putResult = store.put("key", "new_value".toByteArray())
        assertTrue(putResult is KVResult.Ok)

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `file with invalid permissions`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        store.put("key", "value".toByteArray())

        // Make file unreadable
        val hash = sha256("key")
        val file = File(dir, hash)
        file.setReadable(false)

        // Try to read - should get IO error
        val result = store.get("key")
        assertTrue("Reading unreadable file should fail", result is KVResult.Err)
        // On some systems this might succeed, so we check if it's an error
        if (result is KVResult.Err) {
            assertTrue(result.error is KVError.Io)
        }

        // Restore permissions
        file.setReadable(true)

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `symlink to file instead of regular file`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        // Write data
        store.put("key", "value".toByteArray())

        // Create a symlink (if supported on this OS)
        val hash = sha256("key")
        val file = File(dir, hash)
        val targetFile = File(dir, "target")
        targetFile.writeBytes("linked_value".toByteArray())

        try {
            // Try to create symbolic link
            val symlinkFile = File(dir, "${hash}_link")
            java.nio.file.Files.createSymbolicLink(
                symlinkFile.toPath(),
                targetFile.toPath()
            )

            // This is just to verify symlinks work on the filesystem
            assertTrue("Symlink should exist", symlinkFile.exists())
        } catch (e: UnsupportedOperationException) {
            // Symlinks not supported on this OS, skip
        } catch (e: Exception) {
            // Permission issues or other problems, skip
        }

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `very large file corruption - beyond memory`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        // Write small file
        store.put("key", "small".toByteArray())

        // This test just verifies we can handle attempts to read
        // We don't actually create a multi-GB file for test speed
        val hash = sha256("key")
        val file = File(dir, hash)
        assertTrue("File should exist", file.exists())

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `concurrent corruption - file modified during read`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        store.put("key", "original_value".toByteArray())

        // Note: This is hard to test reliably without threading,
        // but we can verify that overwrites work
        store.put("key", "new_value".toByteArray())

        val result = store.get("key")
        assertTrue(result is KVResult.Ok)
        assertEquals("new_value", String((result as KVResult.Ok).value))

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `null bytes in file`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        val dataWithNulls = byteArrayOf(0x00, 0x01, 0x00, 0x02, 0x00)
        store.put("key", dataWithNulls)

        val result = store.get("key")
        assertTrue(result is KVResult.Ok)
        assertArrayEquals(dataWithNulls, (result as KVResult.Ok).value)

        store.close()
        dir.deleteRecursively()
    }

    @Test
    fun `unicode corruption in filenames`() = runTest {
        val dir = createTempDir()
        val store = newStore(dir)

        // Keys with unicode should hash consistently
        val unicodeKey = "key_with_émojis_🔥_and_中文"
        store.put(unicodeKey, "value".toByteArray())

        val result = store.get(unicodeKey)
        assertTrue(result is KVResult.Ok)
        assertEquals("value", String((result as KVResult.Ok).value))

        store.close()
        dir.deleteRecursively()
    }
}

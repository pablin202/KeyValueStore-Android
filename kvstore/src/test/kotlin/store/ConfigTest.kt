package store

// store/ConfigTest.kt
import com.pdm.kvstore.api.KVConfig
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ConfigTest {

    @Test
    fun `creating config with valid directory works`() {
        val dir = createTempDir()

        val config = KVConfig(directory = dir)

        assertEquals(dir, config.directory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `creating config with non-directory fails`() {
        val file = File.createTempFile("x", "y")

        KVConfig(directory = file) // debe explotar
    }
}

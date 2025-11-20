package com.pdm.kvstore.impl

import com.pdm.kvstore.api.KVResult
import com.pdm.kvstore.api.KeyValueStore
import com.pdm.kvstore.errors.KVError
import com.pdm.kvstore.util.KeyValidator
import com.pdm.kvstore.util.sha256
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

internal class KeyValueStoreImpl(
    private val directory: File
) : KeyValueStore {

    private val closed = AtomicBoolean(false)
    private val dispatcher = DispatcherProvider.newSingleThreadDispatcher()

    override fun isClosed(): Boolean = closed.get()

    // -----------------------------------------------------------------------
    // PUT
    // -----------------------------------------------------------------------
    override suspend fun put(key: String, value: ByteArray): KVResult<Unit> =
        withContext(dispatcher) {

            // 1. Check if store is closed
            if (closed.get()) return@withContext KVResult.Err(KVError.ClosedStore())

            // 2. Validate key
            KeyValidator.validate(key)?.let { return@withContext KVResult.Err(it) }

            // 3.
            val file = File(directory, sha256(key))

            // 4. Safe writing
            try {
                file.writeBytes(value)
                KVResult.Ok(Unit)
            } catch (io: IOException) {
                KVResult.Err(KVError.Io(io))
            }
        }

    // -----------------------------------------------------------------------
    // GET
    // -----------------------------------------------------------------------
    override suspend fun get(key: String): KVResult<ByteArray> =
        withContext(dispatcher) {

            if (closed.get()) return@withContext KVResult.Err(KVError.ClosedStore())
            KeyValidator.validate(key)?.let { return@withContext KVResult.Err(it) }

            val file = File(directory, sha256(key))

            if (!file.exists()) {
                return@withContext KVResult.Err(KVError.KeyNotFound)
            }

            return@withContext try {
                KVResult.Ok(file.readBytes())
            } catch (io: IOException) {
                KVResult.Err(KVError.Io(io))
            }
        }

    // -----------------------------------------------------------------------
    // REMOVE
    // -----------------------------------------------------------------------
    override suspend fun remove(key: String): KVResult<Unit> =
        withContext(dispatcher) {

            if (closed.get()) return@withContext KVResult.Err(KVError.ClosedStore())
            KeyValidator.validate(key)?.let { return@withContext KVResult.Err(it) }

            val file = File(directory, sha256(key))

            if (!file.exists()) {
                return@withContext KVResult.Err(KVError.KeyNotFound)
            }

            return@withContext try {
                if (file.delete()) {
                    KVResult.Ok(Unit)
                } else {
                    KVResult.Err(KVError.Io(IOException("delete failed")))
                }
            } catch (io: IOException) {
                KVResult.Err(KVError.Io(io))
            }
        }

    // -----------------------------------------------------------------------
    // CONTAINS
    // -----------------------------------------------------------------------
    override suspend fun contains(key: String): KVResult<Boolean> =
        withContext(dispatcher) {

            if (closed.get()) return@withContext KVResult.Err(KVError.ClosedStore())
            KeyValidator.validate(key)?.let { return@withContext KVResult.Err(it) }

            val file = File(directory, sha256(key))

            KVResult.Ok(file.exists())
        }

    // -----------------------------------------------------------------------
    // CLEAR ALL
    // -----------------------------------------------------------------------
    override suspend fun clear(): KVResult<Unit> =
        withContext(dispatcher) {

            if (closed.get()) return@withContext KVResult.Err(KVError.ClosedStore())

            return@withContext try {
                directory.listFiles()?.forEach { it.delete() }
                KVResult.Ok(Unit)
            } catch (io: IOException) {
                KVResult.Err(KVError.Io(io))
            }
        }

    // -----------------------------------------------------------------------
    // CLOSE
    // -----------------------------------------------------------------------
    override fun close() {
        if (closed.compareAndSet(false, true)) {
            dispatcher.close()
        }
    }
}
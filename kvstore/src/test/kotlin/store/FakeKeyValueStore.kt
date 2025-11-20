package store

import com.pdm.kvstore.api.KVResult
import com.pdm.kvstore.api.KeyValueStore
import com.pdm.kvstore.errors.KVError

// store/FakeKeyValueStore.kt
class FakeKeyValueStore : KeyValueStore {
    private val data = mutableMapOf<String, ByteArray>()
    private var closed = false

    override suspend fun put(
        key: String,
        value: ByteArray
    ): KVResult<Unit> {
        if (closed) return KVResult.Err(KVError.ClosedStore())
        data[key] = value.copyOf()
        return KVResult.Ok(Unit)
    }

    override suspend fun get(key: String): KVResult<ByteArray> {
        if (closed) return KVResult.Err(KVError.ClosedStore())
        val value = data[key] ?: return KVResult.Err(KVError.KeyNotFound)
        return KVResult.Ok(value.copyOf())
    }

    override suspend fun remove(key: String): KVResult<Unit> {
        if (closed) return KVResult.Err(KVError.ClosedStore())
        if (!data.containsKey(key)) return KVResult.Err(KVError.KeyNotFound)
        data.remove(key)
        return KVResult.Ok(Unit)
    }

    override suspend fun contains(key: String): KVResult<Boolean> {
        if (closed) return KVResult.Err(KVError.ClosedStore())
        return KVResult.Ok(data.containsKey(key))
    }

    override suspend fun clear(): KVResult<Unit> {
        if (closed) return KVResult.Err(KVError.ClosedStore())
        data.clear()
        return KVResult.Ok(Unit)
    }

    override fun isClosed(): Boolean = closed

    override fun close() {
        closed = true
        data.clear()
    }
}

package com.pdm.kvstore.api

import java.io.Closeable

interface KeyValueStore : Closeable {

    suspend fun put(key: String, value: ByteArray): KVResult<Unit>
    suspend fun get(key: String): KVResult<ByteArray>
    suspend fun remove(key: String): KVResult<Unit>
    suspend fun contains(key: String): KVResult<Boolean>
    suspend fun clear(): KVResult<Unit>

    fun isClosed(): Boolean

    companion object {
        fun create(config: KVConfig): KeyValueStore {
            val dir = config.directory
            if (!dir.exists()) dir.mkdirs()
            return com.pdm.kvstore.impl.KeyValueStoreImpl(dir)
        }
    }
}
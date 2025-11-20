package com.pdm.kvstore.util

import com.pdm.kvstore.errors.KVError

internal object KeyValidator {

    private const val MAX_LEN = 256

    fun validate(key: String): KVError.InvalidKey? {
        if (key.isBlank()) return KVError.InvalidKey("key is blank")
        if (key.length > MAX_LEN) return KVError.InvalidKey("too long")
        if (key.contains("..")) return KVError.InvalidKey("unsafe")
        return null
    }
}
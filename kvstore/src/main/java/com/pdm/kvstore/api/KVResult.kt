package com.pdm.kvstore.api

import com.pdm.kvstore.errors.KVError

sealed class KVResult<out T> {
    data class Ok<out T>(val value: T) : KVResult<T>()
    data class Err(val error: KVError) : KVResult<Nothing>()
}
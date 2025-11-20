package com.pdm.kvstore.errors

import java.io.IOException

sealed class KVError {
    object KeyNotFound : KVError()
    data class Io(val cause: IOException) : KVError()
    data class InvalidKey(val reason: String) : KVError()
    data class ClosedStore(val reason: String? = null) : KVError()
}
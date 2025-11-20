package com.pdm.kvstore.api

import java.io.File

data class KVConfig(val directory: File) {
    init {
        require(directory.isDirectory || !directory.exists()) {
            "Config directory must be a directory, not a file: ${directory.absolutePath}"
        }
    }
}
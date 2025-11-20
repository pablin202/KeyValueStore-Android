package com.pdm.kvstore.impl

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

internal object DispatcherProvider {
    fun newSingleThreadDispatcher() =
        Executors.newSingleThreadExecutor { r ->
            Thread(r, "kvstore-io")
        }.asCoroutineDispatcher()
}
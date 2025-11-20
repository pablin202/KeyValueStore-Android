package com.pdm.keyvaluestore.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdm.keyvaluestore.data.AppSettings
import com.pdm.keyvaluestore.data.CacheEntry
import com.pdm.keyvaluestore.data.User
import com.pdm.kvstore.api.KVConfig
import com.pdm.kvstore.api.KVResult
import com.pdm.kvstore.api.KeyValueStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class KVStoreUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Basic Operations
    val currentValue: String = "",
    val retrievedValue: String = "",

    // User Profile
    val currentUser: User? = null,

    // Settings
    val appSettings: AppSettings = AppSettings(),

    // Cache
    val cachedData: String = "",

    // Statistics
    val stats: Stats = Stats()
)

data class Stats(
    val totalOperations: Int = 0,
    val putOperations: Int = 0,
    val getOperations: Int = 0,
    val removeOperations: Int = 0,
    val errorCount: Int = 0
)

class KVStoreViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(KVStoreUiState())
    val uiState: StateFlow<KVStoreUiState> = _uiState.asStateFlow()

    private val store: KeyValueStore

    init {
        val dir = File(application.filesDir, "kvstore_demo")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        store = KeyValueStore.create(KVConfig(directory = dir))

        // Load initial data
        loadSettings()
        loadUserProfile()
    }

    // ---------------------------
    // BASIC OPERATIONS
    // ---------------------------

    fun putString(key: String, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = store.put(key, value.toByteArray())) {
                is KVResult.Ok -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = "Saved: $key = $value",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                putOperations = state.stats.putOperations + 1
                            )
                        )
                    }
                }
                is KVResult.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "Error: ${result.error}",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                errorCount = state.stats.errorCount + 1
                            )
                        )
                    }
                }
            }
        }
    }

    fun getString(key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = store.get(key)) {
                is KVResult.Ok -> {
                    val value = String(result.value)
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            retrievedValue = value,
                            successMessage = "Retrieved: $key = $value",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                getOperations = state.stats.getOperations + 1
                            )
                        )
                    }
                }
                is KVResult.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            retrievedValue = "",
                            errorMessage = "Key not found: $key",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                errorCount = state.stats.errorCount + 1
                            )
                        )
                    }
                }
            }
        }
    }

    fun removeKey(key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = store.remove(key)) {
                is KVResult.Ok -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = "Removed: $key",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                removeOperations = state.stats.removeOperations + 1
                            )
                        )
                    }
                }
                is KVResult.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "Error removing: ${result.error}",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                errorCount = state.stats.errorCount + 1
                            )
                        )
                    }
                }
            }
        }
    }

    fun contains(key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = store.contains(key)) {
                is KVResult.Ok -> {
                    val exists = result.value
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = "Key '$key' ${if (exists) "exists" else "does not exist"}",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1
                            )
                        )
                    }
                }
                is KVResult.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "Error: ${result.error}",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                errorCount = state.stats.errorCount + 1
                            )
                        )
                    }
                }
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = store.clear()) {
                is KVResult.Ok -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = "All data cleared",
                            currentUser = null,
                            appSettings = AppSettings(),
                            retrievedValue = "",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1
                            )
                        )
                    }
                }
                is KVResult.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "Error clearing: ${result.error}",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                errorCount = state.stats.errorCount + 1
                            )
                        )
                    }
                }
            }
        }
    }

    // ---------------------------
    // USER PROFILE
    // ---------------------------

    fun saveUserProfile(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = store.put("user_profile", user.toJson().toByteArray())) {
                is KVResult.Ok -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            currentUser = user,
                            successMessage = "User profile saved",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                putOperations = state.stats.putOperations + 1
                            )
                        )
                    }
                }
                is KVResult.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "Error saving user: ${result.error}",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                errorCount = state.stats.errorCount + 1
                            )
                        )
                    }
                }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = store.get("user_profile")) {
                is KVResult.Ok -> {
                    val json = String(result.value)
                    val user = User.fromJson(json)
                    _uiState.update { it.copy(currentUser = user) }
                }
                is KVResult.Err -> {
                    // No user saved yet
                }
            }
        }
    }

    // ---------------------------
    // SETTINGS
    // ---------------------------

    fun saveSettings(settings: AppSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = store.put("app_settings", settings.toJson().toByteArray())) {
                is KVResult.Ok -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            appSettings = settings,
                            successMessage = "Settings saved",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                putOperations = state.stats.putOperations + 1
                            )
                        )
                    }
                }
                is KVResult.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "Error saving settings: ${result.error}",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                errorCount = state.stats.errorCount + 1
                            )
                        )
                    }
                }
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = store.get("app_settings")) {
                is KVResult.Ok -> {
                    val json = String(result.value)
                    val settings = AppSettings.fromJson(json) ?: AppSettings()
                    _uiState.update { it.copy(appSettings = settings) }
                }
                is KVResult.Err -> {
                    // Use default settings
                }
            }
        }
    }

    // ---------------------------
    // CACHE
    // ---------------------------

    fun cacheData(key: String, data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cacheEntry = CacheEntry(key = key, data = data)

            when (val result = store.put("cache_$key", cacheEntry.toJson().toByteArray())) {
                is KVResult.Ok -> {
                    _uiState.update { state ->
                        state.copy(
                            successMessage = "Cached: $key",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                putOperations = state.stats.putOperations + 1
                            )
                        )
                    }
                }
                is KVResult.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            errorMessage = "Error caching: ${result.error}",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1,
                                errorCount = state.stats.errorCount + 1
                            )
                        )
                    }
                }
            }
        }
    }

    fun getCachedData(key: String, ttlMillis: Long = 60000) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = store.get("cache_$key")) {
                is KVResult.Ok -> {
                    val json = String(result.value)
                    val entry = CacheEntry.fromJson(json)

                    if (entry != null && !entry.isExpired(ttlMillis)) {
                        _uiState.update { state ->
                            state.copy(
                                cachedData = entry.data,
                                successMessage = "Cache hit: $key",
                                stats = state.stats.copy(
                                    totalOperations = state.stats.totalOperations + 1,
                                    getOperations = state.stats.getOperations + 1
                                )
                            )
                        }
                    } else {
                        _uiState.update { state ->
                            state.copy(
                                cachedData = "",
                                errorMessage = "Cache expired for: $key",
                                stats = state.stats.copy(
                                    totalOperations = state.stats.totalOperations + 1
                                )
                            )
                        }
                    }
                }
                is KVResult.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            cachedData = "",
                            errorMessage = "Cache miss: $key",
                            stats = state.stats.copy(
                                totalOperations = state.stats.totalOperations + 1
                            )
                        )
                    }
                }
            }
        }
    }

    // ---------------------------
    // UTILITIES
    // ---------------------------

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        store.close()
    }
}

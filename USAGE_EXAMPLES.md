# KeyValueStore - Complete Usage Examples

## Table of Contents
- [Initial Setup](#initial-setup)
- [Basic Operations](#basic-operations)
- [JSON Objects](#json-objects)
- [Cache System](#cache-system)
- [Compose Integration](#compose-integration)
- [Advanced Patterns](#advanced-patterns)

## Initial Setup

### 1. Add Dependency

```kotlin
// build.gradle.kts (app module)
dependencies {
    implementation(project(":kvstore"))
    implementation("com.google.code.gson:gson:2.11.0") // For JSON
}
```

### 2. Create Instance

```kotlin
// In Application or Activity
class MyApp : Application() {
    companion object {
        lateinit var kvStore: KeyValueStore
            private set
    }

    override fun onCreate() {
        super.onCreate()

        val dir = File(filesDir, "app_kvstore")
        kvStore = KeyValueStore.create(KVConfig(directory = dir))
    }

    override fun onTerminate() {
        super.onTerminate()
        kvStore.close()
    }
}

// Global usage
val result = MyApp.kvStore.put("key", "value".toByteArray())
```

## Basic Operations

### PUT - Save Data

```kotlin
suspend fun saveData() {
    // Simple string
    when (val result = store.put("username", "john_doe".toByteArray())) {
        is KVResult.Ok -> println("✓ Saved")
        is KVResult.Err -> println("✗ Error: ${result.error}")
    }

    // Number as string
    store.put("user_age", "25".toByteArray())

    // Boolean as string
    store.put("is_premium", "true".toByteArray())

    // Raw bytes
    val imageBytes = loadImageBytes()
    store.put("profile_pic", imageBytes)
}
```

### GET - Retrieve Data

```kotlin
suspend fun loadData() {
    when (val result = store.get("username")) {
        is KVResult.Ok -> {
            val username = String(result.value)
            println("Username: $username")
        }
        is KVResult.Err -> {
            when (result.error) {
                is KVError.KeyNotFound -> {
                    println("User not found")
                    // Use default value
                    val defaultUsername = "guest"
                }
                is KVError.ClosedStore -> {
                    println("Store closed, reinitialize")
                }
                is KVError.Io -> {
                    println("I/O Error: ${result.error.cause}")
                }
                else -> println("Unknown error")
            }
        }
    }
}
```

### CONTAINS - Check Existence

```kotlin
suspend fun checkUserSession(): Boolean {
    return when (val result = store.contains("auth_token")) {
        is KVResult.Ok -> result.value
        is KVResult.Err -> false
    }
}

// Usage
if (checkUserSession()) {
    navigateToHome()
} else {
    navigateToLogin()
}
```

### REMOVE - Delete

```kotlin
suspend fun logout() {
    // Remove authentication token
    store.remove("auth_token")
    store.remove("refresh_token")
    store.remove("user_profile")

    // Verify it was deleted
    val hasToken = store.contains("auth_token")
    if (hasToken is KVResult.Ok && !hasToken.value) {
        println("Session closed successfully")
    }
}
```

### CLEAR - Clear All

```kotlin
suspend fun clearAppData() {
    when (val result = store.clear()) {
        is KVResult.Ok -> {
            println("All data deleted")
            resetAppState()
        }
        is KVResult.Err -> {
            println("Error clearing: ${result.error}")
        }
    }
}
```

## JSON Objects

### Data Model

```kotlin
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class User(
    val id: String,
    val name: String,
    val email: String,
    val age: Int,
    val preferences: UserPreferences? = null
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): User? {
            return try {
                Gson().fromJson(json, User::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class UserPreferences(
    val theme: String,
    val language: String,
    val notifications: Boolean
)
```

### Save and Load Objects

```kotlin
suspend fun saveUser(user: User) {
    val json = user.toJson()
    when (val result = store.put("current_user", json.toByteArray())) {
        is KVResult.Ok -> println("User saved")
        is KVResult.Err -> println("Error: ${result.error}")
    }
}

suspend fun loadUser(): User? {
    return when (val result = store.get("current_user")) {
        is KVResult.Ok -> {
            val json = String(result.value)
            User.fromJson(json)
        }
        is KVResult.Err -> null
    }
}

// Usage
val user = User(
    id = "123",
    name = "John Doe",
    email = "john@example.com",
    age = 30,
    preferences = UserPreferences(
        theme = "dark",
        language = "en",
        notifications = true
    )
)

saveUser(user)

val loadedUser = loadUser()
println("Loaded: ${loadedUser?.name}")
```

### Lists and Collections

```kotlin
data class ShoppingList(
    val items: List<ShoppingItem>,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class ShoppingItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val completed: Boolean = false
)

suspend fun saveShoppingList(list: ShoppingList) {
    val json = Gson().toJson(list)
    store.put("shopping_list", json.toByteArray())
}

suspend fun loadShoppingList(): ShoppingList? {
    return when (val result = store.get("shopping_list")) {
        is KVResult.Ok -> {
            val json = String(result.value)
            Gson().fromJson(json, ShoppingList::class.java)
        }
        is KVResult.Err -> null
    }
}
```

## Cache System

### Basic Cache with TTL

```kotlin
data class CacheEntry<T>(
    val data: T,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun isExpired(ttlMillis: Long): Boolean {
        return System.currentTimeMillis() - timestamp > ttlMillis
    }

    fun toJson(): String = Gson().toJson(this)
}

class CacheManager(private val store: KeyValueStore) {

    suspend fun <T> cache(key: String, data: T) {
        val entry = CacheEntry(data)
        val json = Gson().toJson(entry)
        store.put("cache_$key", json.toByteArray())
    }

    suspend inline fun <reified T> getCache(
        key: String,
        ttlMillis: Long
    ): T? {
        return when (val result = store.get("cache_$key")) {
            is KVResult.Ok -> {
                try {
                    val json = String(result.value)
                    val type = object : TypeToken<CacheEntry<T>>() {}.type
                    val entry: CacheEntry<T> = Gson().fromJson(json, type)

                    if (entry.isExpired(ttlMillis)) {
                        null // Expired
                    } else {
                        entry.data
                    }
                } catch (e: Exception) {
                    null
                }
            }
            is KVResult.Err -> null
        }
    }
}

// Usage
val cacheManager = CacheManager(store)

// Cache API response
val apiResponse = fetchUsersFromApi()
cacheManager.cache("users_list", apiResponse)

// Retrieve from cache (TTL: 5 minutes)
val cachedUsers = cacheManager.getCache<List<User>>(
    "users_list",
    ttlMillis = 5 * 60 * 1000
)

if (cachedUsers != null) {
    println("Cache hit: ${cachedUsers.size} users")
} else {
    println("Cache miss or expired, fetching fresh data")
    val freshData = fetchUsersFromApi()
}
```

### Cache with Invalidation Strategy

```kotlin
class SmartCache(private val store: KeyValueStore) {

    suspend fun invalidateAll() {
        // Implement logic to remove all cache entries
        store.clear() // Or be more selective
    }

    suspend fun invalidateByPrefix(prefix: String) {
        // Note: KeyValueStore doesn't support prefix queries
        // You would need to maintain a separate index

        // Save key index
        when (val result = store.get("cache_index")) {
            is KVResult.Ok -> {
                val json = String(result.value)
                val keys: List<String> = Gson().fromJson(json,
                    object : TypeToken<List<String>>() {}.type)

                keys.filter { it.startsWith(prefix) }.forEach { key ->
                    store.remove("cache_$key")
                }
            }
            is KVResult.Err -> {}
        }
    }
}
```

## Compose Integration

### ViewModel with KeyValueStore

```kotlin
class UserViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val store: KeyValueStore

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    init {
        val dir = File(application.filesDir, "user_data")
        store = KeyValueStore.create(KVConfig(directory = dir))

        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = store.get("current_user")) {
                is KVResult.Ok -> {
                    val json = String(result.value)
                    val user = User.fromJson(json)
                    _userState.value = user
                }
                is KVResult.Err -> {
                    _userState.value = null
                }
            }
        }
    }

    fun saveUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = store.put("current_user", user.toJson().toByteArray())) {
                is KVResult.Ok -> {
                    _userState.value = user
                }
                is KVResult.Err -> {
                    // Handle error
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            store.remove("current_user")
            store.remove("auth_token")
            _userState.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        store.close()
    }
}
```

### Composable with State

```kotlin
@Composable
fun UserProfileScreen(viewModel: UserViewModel = viewModel()) {
    val user by viewModel.userState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        if (user != null) {
            Text("Welcome, ${user.name}")
            Button(onClick = { viewModel.logout() }) {
                Text("Logout")
            }
        } else {
            Text("Please login")
        }
    }
}
```

## Advanced Patterns

### Repository Pattern

```kotlin
interface UserRepository {
    suspend fun getUser(): User?
    suspend fun saveUser(user: User)
    suspend fun deleteUser()
}

class UserRepositoryImpl(
    private val store: KeyValueStore
) : UserRepository {

    override suspend fun getUser(): User? {
        return when (val result = store.get("user")) {
            is KVResult.Ok -> User.fromJson(String(result.value))
            is KVResult.Err -> null
        }
    }

    override suspend fun saveUser(user: User) {
        store.put("user", user.toJson().toByteArray())
    }

    override suspend fun deleteUser() {
        store.remove("user")
    }
}
```

### Preference Manager

```kotlin
class PreferenceManager(private val store: KeyValueStore) {

    suspend fun getString(key: String, default: String = ""): String {
        return when (val result = store.get(key)) {
            is KVResult.Ok -> String(result.value)
            is KVResult.Err -> default
        }
    }

    suspend fun putString(key: String, value: String) {
        store.put(key, value.toByteArray())
    }

    suspend fun getInt(key: String, default: Int = 0): Int {
        return when (val result = store.get(key)) {
            is KVResult.Ok -> String(result.value).toIntOrNull() ?: default
            is KVResult.Err -> default
        }
    }

    suspend fun putInt(key: String, value: Int) {
        store.put(key, value.toString().toByteArray())
    }

    suspend fun getBoolean(key: String, default: Boolean = false): Boolean {
        return when (val result = store.get(key)) {
            is KVResult.Ok -> String(result.value).toBooleanStrictOrNull() ?: default
            is KVResult.Err -> default
        }
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        store.put(key, value.toString().toByteArray())
    }
}

// Usage
val prefs = PreferenceManager(store)

prefs.putString("theme", "dark")
prefs.putBoolean("notifications", true)
prefs.putInt("last_sync", System.currentTimeMillis().toInt())

val theme = prefs.getString("theme", "light")
val notificationsEnabled = prefs.getBoolean("notifications", false)
```

### Extension Functions

```kotlin
// Useful extensions to simplify code

suspend fun KeyValueStore.putString(key: String, value: String): KVResult<Unit> {
    return put(key, value.toByteArray())
}

suspend fun KeyValueStore.getString(key: String): String? {
    return when (val result = get(key)) {
        is KVResult.Ok -> String(result.value)
        is KVResult.Err -> null
    }
}

suspend inline fun <reified T> KeyValueStore.putJson(key: String, value: T): KVResult<Unit> {
    val json = Gson().toJson(value)
    return put(key, json.toByteArray())
}

suspend inline fun <reified T> KeyValueStore.getJson(key: String): T? {
    return when (val result = get(key)) {
        is KVResult.Ok -> {
            try {
                val json = String(result.value)
                Gson().fromJson(json, T::class.java)
            } catch (e: Exception) {
                null
            }
        }
        is KVResult.Err -> null
    }
}

// Simplified usage
store.putString("name", "John")
val name = store.getString("name")

val user = User(...)
store.putJson("user", user)
val loadedUser = store.getJson<User>("user")
```

### Migrations

```kotlin
class StoreMigrations(private val store: KeyValueStore) {

    suspend fun migrateToV2() {
        // Migrate from version 1 to version 2

        // Rename key
        when (val result = store.get("old_key")) {
            is KVResult.Ok -> {
                store.put("new_key", result.value)
                store.remove("old_key")
            }
            is KVResult.Err -> {}
        }

        // Update data structure
        when (val result = store.get("user")) {
            is KVResult.Ok -> {
                val oldUser = OldUserStructure.fromJson(String(result.value))
                val newUser = oldUser?.toNewStructure()
                if (newUser != null) {
                    store.put("user", newUser.toJson().toByteArray())
                }
            }
            is KVResult.Err -> {}
        }

        // Mark migration as completed
        store.put("migration_v2", "completed".toByteArray())
    }
}
```

## Complete Use Cases

### Authentication System

```kotlin
class AuthManager(private val store: KeyValueStore) {

    suspend fun login(token: String, refreshToken: String, user: User) {
        store.put("auth_token", token.toByteArray())
        store.put("refresh_token", refreshToken.toByteArray())
        store.putJson("user", user)
        store.put("last_login", System.currentTimeMillis().toString().toByteArray())
    }

    suspend fun isAuthenticated(): Boolean {
        return when (store.contains("auth_token")) {
            is KVResult.Ok -> true
            is KVResult.Err -> false
        }
    }

    suspend fun getAuthToken(): String? {
        return store.getString("auth_token")
    }

    suspend fun logout() {
        store.remove("auth_token")
        store.remove("refresh_token")
        store.remove("user")
    }
}
```

### Offline-First App

```kotlin
class OfflineManager(private val store: KeyValueStore) {

    suspend fun saveForOffline(articles: List<Article>) {
        val cacheEntry = CacheEntry(
            data = articles,
            timestamp = System.currentTimeMillis()
        )
        store.putJson("offline_articles", cacheEntry)
    }

    suspend fun getOfflineData(): List<Article>? {
        val entry = store.getJson<CacheEntry<List<Article>>>("offline_articles")
        return entry?.data
    }

    suspend fun hasOfflineData(): Boolean {
        return when (store.contains("offline_articles")) {
            is KVResult.Ok -> true
            is KVResult.Err -> false
        }
    }
}
```

---

Need more specific examples? Open an issue in the repository!

# KeyValueStore Demo App

This application demonstrates the complete usage of the **KeyValueStore** library using Jetpack Compose.

## 📱 App Features

The app includes 5 main screens demonstrating all library use cases:

### 1. **Basic Operations** 🔧
- Basic CRUD operations (PUT, GET, REMOVE, CONTAINS)
- Simple string storage
- Preloaded quick examples (tokens, IDs, URLs)
- Clear all button

**Use cases:**
- Save authentication tokens
- Store user IDs
- Save configuration URLs

### 2. **User Profile** 👤
- Complex object storage using JSON
- Serialization/deserialization with Gson
- Custom fields (name, email, age, premium status)
- Pre-configured user examples

**Use cases:**
- Local user profile
- Session data
- Customer information

### 3. **Settings** ⚙️
- App preferences persistence
- Theme configuration (Light/Dark/System)
- Switches for notifications and auto-save
- Language selector
- Reset to default values

**Use cases:**
- User preferences
- App configuration
- Customization options

### 4. **Cache** 💾
- Cache system with TTL (Time To Live)
- Automatic timestamps
- Expiration validation
- Examples of API responses, session tokens, weather data

**Use cases:**
- HTTP response caching
- Temporary tokens
- Data with expiration

### 5. **Stats** 📊
- Real-time operation statistics
- PUT, GET, REMOVE counters
- Error tracking
- Success rate
- Library architecture information

## 🚀 How to Use the Library

### Installation

```gradle
dependencies {
    implementation(project(":kvstore"))
}
```

### Initialization

```kotlin
// In your Application or Activity
val dir = File(context.filesDir, "kvstore")
val store = KeyValueStore.create(KVConfig(directory = dir))
```

### Basic Operations

#### 1. Save Data (PUT)

```kotlin
// Simple string
val result = store.put("auth_token", "Bearer xyz123".toByteArray())

when (result) {
    is KVResult.Ok -> println("Saved successfully")
    is KVResult.Err -> println("Error: ${result.error}")
}
```

#### 2. Get Data (GET)

```kotlin
when (val result = store.get("auth_token")) {
    is KVResult.Ok -> {
        val token = String(result.value)
        println("Token: $token")
    }
    is KVResult.Err -> {
        when (result.error) {
            is KVError.KeyNotFound -> println("Key not found")
            is KVError.Io -> println("I/O Error: ${result.error}")
            is KVError.ClosedStore -> println("Store closed")
            else -> println("Error: ${result.error}")
        }
    }
}
```

#### 3. Check Existence (CONTAINS)

```kotlin
when (val result = store.contains("user_id")) {
    is KVResult.Ok -> {
        if (result.value) {
            println("Key exists")
        } else {
            println("Key doesn't exist")
        }
    }
    is KVResult.Err -> println("Error: ${result.error}")
}
```

#### 4. Remove (REMOVE)

```kotlin
when (val result = store.remove("old_token")) {
    is KVResult.Ok -> println("Removed")
    is KVResult.Err -> println("Error: ${result.error}")
}
```

#### 5. Clear All (CLEAR)

```kotlin
when (val result = store.clear()) {
    is KVResult.Ok -> println("All cleared")
    is KVResult.Err -> println("Error: ${result.error}")
}
```

### Advanced Usage

#### Store JSON Objects

```kotlin
import com.google.gson.Gson

data class User(val id: String, val name: String, val email: String) {
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

// Save
val user = User("123", "John Doe", "john@example.com")
store.put("current_user", user.toJson().toByteArray())

// Load
when (val result = store.get("current_user")) {
    is KVResult.Ok -> {
        val user = User.fromJson(String(result.value))
        println("User: ${user?.name}")
    }
    is KVResult.Err -> println("Error")
}
```

#### Cache System with TTL

```kotlin
data class CacheEntry(
    val key: String,
    val data: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun isExpired(ttlMillis: Long): Boolean {
        return System.currentTimeMillis() - timestamp > ttlMillis
    }
}

// Save to cache
val entry = CacheEntry(
    key = "api_response",
    data = """{"users": [...]}""",
    timestamp = System.currentTimeMillis()
)
store.put("cache_api_response", entry.toJson().toByteArray())

// Retrieve with TTL validation
val ttl = 5 * 60 * 1000L // 5 minutes
when (val result = store.get("cache_api_response")) {
    is KVResult.Ok -> {
        val entry = CacheEntry.fromJson(String(result.value))
        if (entry != null && !entry.isExpired(ttl)) {
            // Use cached data
            println("Cache hit: ${entry.data}")
        } else {
            // Cache expired, fetch fresh data
            println("Cache expired")
        }
    }
    is KVResult.Err -> println("Cache miss")
}
```

#### Usage with ViewModel and Coroutines

```kotlin
class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val store: KeyValueStore

    init {
        val dir = File(application.filesDir, "kvstore")
        store = KeyValueStore.create(KVConfig(directory = dir))
    }

    fun saveUserToken(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = store.put("token", token.toByteArray())) {
                is KVResult.Ok -> {
                    // Update UI
                    _uiState.update { it.copy(tokenSaved = true) }
                }
                is KVResult.Err -> {
                    // Handle error
                    _uiState.update { it.copy(error = result.error.toString()) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        store.close() // Important: close the store
    }
}
```

## 🎯 Real Use Cases

### 1. Authentication
```kotlin
// Save token after login
store.put("auth_token", token.toByteArray())
store.put("refresh_token", refreshToken.toByteArray())

// Check if there's an active session
when (val result = store.contains("auth_token")) {
    is KVResult.Ok -> {
        if (result.value) {
            // User authenticated
            navigateToHome()
        } else {
            // Go to login
            navigateToLogin()
        }
    }
}

// Logout
store.remove("auth_token")
store.remove("refresh_token")
```

### 2. Onboarding
```kotlin
// Check if it's the first time opening the app
when (val result = store.contains("onboarding_completed")) {
    is KVResult.Ok -> {
        if (!result.value) {
            showOnboarding()
        }
    }
}

// Mark onboarding as completed
store.put("onboarding_completed", "true".toByteArray())
```

### 3. User Preferences
```kotlin
// Theme
store.put("theme", "dark".toByteArray())

// Language
store.put("language", "en".toByteArray())

// Notifications
store.put("notifications_enabled", "true".toByteArray())
```

### 4. Image/Data Caching
```kotlin
// Cache profile image URL
store.put("profile_image_url", url.toByteArray())

// Cache item list with timestamp
val cacheEntry = CacheEntry(
    key = "items_list",
    data = Gson().toJson(items),
    timestamp = System.currentTimeMillis()
)
store.put("cache_items", cacheEntry.toJson().toByteArray())
```

## 🔒 Security Features

1. **Hashed filenames**: Keys are hashed with SHA-256 to avoid collisions
2. **Private storage**: Data is saved in the app's private directory
3. **Thread-safe**: All operations are serialized
4. **Explicit error handling**: Result-based API for full control

## 📝 Best Practices

1. **Always close the store**:
   ```kotlin
   override fun onCleared() {
       super.onCleared()
       store.close()
   }
   ```

2. **Use Dispatchers.IO for operations**:
   ```kotlin
   viewModelScope.launch(Dispatchers.IO) {
       store.put(...)
   }
   ```

3. **Handle all error cases**:
   ```kotlin
   when (val result = store.get(key)) {
       is KVResult.Ok -> handleSuccess(result.value)
       is KVResult.Err -> when (result.error) {
           is KVError.KeyNotFound -> handleNotFound()
           is KVError.Io -> handleIoError(result.error)
           is KVError.ClosedStore -> handleClosedStore()
           else -> handleOtherError()
       }
   }
   ```

4. **Validate keys**:
   - Don't use empty keys
   - Avoid problematic special characters
   - Use consistent naming conventions

5. **For complex objects, use JSON**:
   ```kotlin
   // Create extension functions to simplify
   fun <T> KeyValueStore.putJson(key: String, value: T): KVResult<Unit> {
       val json = Gson().toJson(value)
       return put(key, json.toByteArray())
   }

   inline fun <reified T> KeyValueStore.getJson(key: String): KVResult<T?> {
       return when (val result = get(key)) {
           is KVResult.Ok -> {
               try {
                   val value = Gson().fromJson(String(result.value), T::class.java)
                   KVResult.Ok(value)
               } catch (e: Exception) {
                   KVResult.Err(KVError.Io(e))
               }
           }
           is KVResult.Err -> KVResult.Err(result.error)
       }
   }
   ```

## 📚 Architecture

```
KeyValueStore (Interface)
    ↓
KeyValueStoreImpl (Implementation)
    ↓
[File System] - Each key → Hashed file
    ↓
[Dispatcher] - Single thread for thread-safety
```

## ⚡ Performance

- **PUT**: ~10-50 µs (depends on size and disk)
- **GET**: ~10-50 µs
- **CONTAINS**: ~5-20 µs
- **Thread-safe**: Yes, using dedicated dispatcher
- **Async**: All operations are suspend functions

## 🐛 Troubleshooting

**Error: ClosedStore**
- Cause: Trying to use the store after `close()`
- Solution: Don't call operations after closing, or create new store

**Error: KeyNotFound**
- Cause: The key doesn't exist
- Solution: Check with `contains()` first or handle the error

**Error: Io**
- Cause: Permission issues or disk full
- Solution: Verify write permissions and available space

## 📦 Build & Run

```bash
# Build the kvstore module
./gradlew :kvstore:build

# Build and run the app
./gradlew :app:installDebug

# Run tests
./gradlew :kvstore:testDebugUnitTest
```

## 📄 License

This project is an educational demonstration of the KeyValueStore library.

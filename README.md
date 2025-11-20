# KeyValueStore 🗄️

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An efficient and secure key-value storage library for Android, designed with Kotlin Coroutines and modern architecture.

## 📋 Features

- ✅ **Thread-Safe**: Serialized operations with dedicated dispatcher
- ✅ **Async/Await**: API based on suspend functions
- ✅ **Type-Safe**: Result-based API with `KVResult<T>`
- ✅ **Efficient**: File-based storage with SHA-256 hashing
- ✅ **Zero Dependencies**: Only Kotlin Coroutines
- ✅ **Comprehensive Testing**: 66+ unit tests
- ✅ **Demo App Included**: Examples with Jetpack Compose

## 🚀 Quick Start

### Installation

```gradle
// settings.gradle.kts
include(":kvstore")

// app/build.gradle.kts
dependencies {
    implementation(project(":kvstore"))
}
```

### Basic Usage

```kotlin
import com.pdm.kvstore.api.KeyValueStore
import com.pdm.kvstore.api.KVConfig
import com.pdm.kvstore.api.KVResult

// 1. Create store
val dir = File(context.filesDir, "app_data")
val store = KeyValueStore.create(KVConfig(directory = dir))

// 2. Save data
when (val result = store.put("user_token", "abc123".toByteArray())) {
    is KVResult.Ok -> println("✓ Saved")
    is KVResult.Err -> println("✗ Error: ${result.error}")
}

// 3. Retrieve data
when (val result = store.get("user_token")) {
    is KVResult.Ok -> {
        val token = String(result.value)
        println("Token: $token")
    }
    is KVResult.Err -> println("Not found")
}

// 4. Clean up
store.close()
```

## 📱 Demo App

The demo application includes 5 screens with complete examples:

1. **Basic Operations** - CRUD basics
2. **User Profile** - JSON objects
3. **Settings** - Persistent preferences
4. **Cache** - TTL-based caching system
5. **Stats** - Real-time statistics

### Run Demo

```bash
./gradlew :app:installDebug
```

## 🎯 Use Cases

### Authentication
```kotlin
// Login
store.put("auth_token", token.toByteArray())
store.put("user_id", userId.toByteArray())

// Check session
val hasToken = store.contains("auth_token")

// Logout
store.remove("auth_token")
```

### JSON Objects
```kotlin
data class User(val id: String, val name: String) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String) = Gson().fromJson(json, User::class.java)
    }
}

// Save
val user = User("123", "John")
store.put("user", user.toJson().toByteArray())

// Load
val result = store.get("user")
val user = User.fromJson(String(result.value))
```

### Caching System
```kotlin
data class CacheEntry<T>(
    val data: T,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun isExpired(ttl: Long) = System.currentTimeMillis() - timestamp > ttl
}

// Cache
val entry = CacheEntry(apiData)
store.put("cache_key", Gson().toJson(entry).toByteArray())

// Retrieve with TTL
val cached = CacheEntry.fromJson(String(store.get("cache_key").value))
if (!cached.isExpired(5 * 60 * 1000)) {
    // Use cache
}
```

## 🏗️ API Reference

### Core Operations

| Method | Description | Returns |
|--------|-------------|---------|
| `put(key, value)` | Save data | `KVResult<Unit>` |
| `get(key)` | Retrieve data | `KVResult<ByteArray>` |
| `remove(key)` | Delete entry | `KVResult<Unit>` |
| `contains(key)` | Check existence | `KVResult<Boolean>` |
| `clear()` | Clear all | `KVResult<Unit>` |
| `close()` | Close store | `Unit` |
| `isClosed()` | Store state | `Boolean` |

### Error Types

```kotlin
sealed class KVError {
    data class KeyNotFound : KVError()
    data class Io(val cause: IOException) : KVError()
    data class ClosedStore : KVError()
    data class InvalidKey(val reason: String) : KVError()
}
```

## 🧪 Testing

### Run Tests

```bash
# All tests
./gradlew :kvstore:testDebugUnitTest

# Stress tests only
./gradlew :kvstore:test --tests "store.KeyValueStoreStressTest"

# Benchmarks
./gradlew :kvstore:test --tests "benchmarks.SimpleBenchmarkRunner"
```

### Test Coverage

- **66 tests** total
- Stress tests (10,000+ operations)
- File corruption scenarios
- Concurrency tests
- Fake vs Impl comparison
- Performance benchmarks

## 📊 Performance

Approximate measurements on modern device:

- **PUT**: ~10-50 µs per operation
- **GET**: ~10-50 µs per operation
- **CONTAINS**: ~5-20 µs per operation
- **Thread-safe**: Yes ✅
- **Async**: Yes ✅

## 🏛️ Architecture

```
┌─────────────────────┐
│  KeyValueStore API  │ (Public interface)
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ KeyValueStoreImpl   │ (Implementation)
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│   File System       │ (SHA-256 hashed files)
└─────────────────────┘
```

## 📚 Complete Documentation

- [📱 Demo App Guide](app/README.md) - Demo application guide
- [💡 Usage Examples](USAGE_EXAMPLES.md) - Complete use cases
- [🧪 Benchmarks](kvstore/src/test/kotlin/benchmarks/README.md) - Benchmark guide

## 🛠️ Requirements

- **Android**: minSdk 21+ (Android 5.0+)
- **Kotlin**: 1.9.25+
- **Java**: 17
- **Gradle**: 8.11+

## 📄 License

This project is licensed under the MIT License.

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the project
2. Create a branch (`git checkout -b feature/amazing`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing`)
5. Open a Pull Request

## 📧 Contact

Questions or suggestions? Open an issue on GitHub.

---

**Made with ❤️ and Kotlin**

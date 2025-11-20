package com.pdm.keyvaluestore.data

import com.google.gson.Gson

data class User(
    val id: String,
    val name: String,
    val email: String,
    val age: Int,
    val isPremium: Boolean = false
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

data class AppSettings(
    val theme: String = "System",
    val notifications: Boolean = true,
    val autoSave: Boolean = true,
    val language: String = "en"
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): AppSettings? {
            return try {
                Gson().fromJson(json, AppSettings::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class CacheEntry(
    val key: String,
    val data: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun isExpired(ttlMillis: Long): Boolean {
        return System.currentTimeMillis() - timestamp > ttlMillis
    }

    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): CacheEntry? {
            return try {
                Gson().fromJson(json, CacheEntry::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}

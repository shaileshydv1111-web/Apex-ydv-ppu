package com.example.data

import android.content.Context

enum class UserRole {
    STUDENT,
    ADMIN
}

object UserSessionManager {
    private const val PREF_NAME = "ppu_user_session_prefs"
    private const val KEY_USER_ROLE = "user_role"

    fun getUserRole(context: Context): UserRole? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val roleStr = prefs.getString(KEY_USER_ROLE, null) ?: return null
        return try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            null
        }
    }

    fun setUserRole(context: Context, role: UserRole) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER_ROLE, role.name).apply()
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

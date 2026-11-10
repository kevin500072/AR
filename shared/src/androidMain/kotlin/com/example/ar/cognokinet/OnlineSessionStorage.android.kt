package com.example.ar.cognokinet

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class AndroidOnlineSessionStore(context: Context) : OnlineSessionStore {
    private val preferences = context.getSharedPreferences("online_session", Context.MODE_PRIVATE)

    override fun load(): String? = preferences.getString("session_json", null)

    override fun save(sessionJson: String) {
        preferences.edit().putString("session_json", sessionJson).apply()
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }
}

@Composable
actual fun rememberOnlineSessionStore(): OnlineSessionStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidOnlineSessionStore(context) }
}

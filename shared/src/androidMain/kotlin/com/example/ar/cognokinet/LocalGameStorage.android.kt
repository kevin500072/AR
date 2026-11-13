package com.example.ar.cognokinet

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class AndroidLocalGameStore(context: Context) : LocalGameStore {
    private val preferences = context.getSharedPreferences("local_game_data", Context.MODE_PRIVATE)

    override fun load(): String? = preferences.getString("snapshot_json", null)

    override fun save(snapshotJson: String) {
        preferences.edit().putString("snapshot_json", snapshotJson).apply()
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }
}

@Composable
actual fun rememberLocalGameStore(): LocalGameStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidLocalGameStore(context) }
}

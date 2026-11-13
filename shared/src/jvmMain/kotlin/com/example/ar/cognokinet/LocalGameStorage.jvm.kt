package com.example.ar.cognokinet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.prefs.Preferences

@Composable
actual fun rememberLocalGameStore(): LocalGameStore = remember {
    object : LocalGameStore {
        private val preferences = Preferences.userNodeForPackage(LocalGameStore::class.java)
        override fun load(): String? = preferences.get("snapshot_json", null)
        override fun save(snapshotJson: String) {
            preferences.put("snapshot_json", snapshotJson)
        }
        override fun clear() {
            preferences.remove("snapshot_json")
        }
    }
}

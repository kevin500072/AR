package com.example.ar.cognokinet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberLocalGameStore(): LocalGameStore = remember {
    object : LocalGameStore {
        private val defaults = NSUserDefaults.standardUserDefaults
        override fun load(): String? = defaults.stringForKey("cognokinet_local_game_data")
        override fun save(snapshotJson: String) {
            defaults.setObject(snapshotJson, forKey = "cognokinet_local_game_data")
        }
        override fun clear() {
            defaults.removeObjectForKey("cognokinet_local_game_data")
        }
    }
}

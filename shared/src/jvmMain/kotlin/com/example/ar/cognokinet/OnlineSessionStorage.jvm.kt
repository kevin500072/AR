package com.example.ar.cognokinet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberOnlineSessionStore(): OnlineSessionStore = remember {
    object : OnlineSessionStore {
        private var sessionJson: String? = null
        override fun load(): String? = sessionJson
        override fun save(sessionJson: String) { this.sessionJson = sessionJson }
        override fun clear() { sessionJson = null }
    }
}

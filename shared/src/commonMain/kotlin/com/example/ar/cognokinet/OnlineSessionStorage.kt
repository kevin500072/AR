package com.example.ar.cognokinet

import androidx.compose.runtime.Composable

interface OnlineSessionStore {
    fun load(): String?
    fun save(sessionJson: String)
    fun clear()
}

@Composable
expect fun rememberOnlineSessionStore(): OnlineSessionStore

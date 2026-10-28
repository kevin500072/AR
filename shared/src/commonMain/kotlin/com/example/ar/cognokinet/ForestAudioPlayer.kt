package com.example.ar.cognokinet

import androidx.compose.runtime.Composable

@Composable
expect fun ForestAudioPlayerController(
    isMusicEnabled: Boolean,
    isMusicPlaying: Boolean
)

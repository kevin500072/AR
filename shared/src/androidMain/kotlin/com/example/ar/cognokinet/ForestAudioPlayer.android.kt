package com.example.ar.cognokinet

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ar.shared.R

private fun createForestMediaPlayer(context: Context): MediaPlayer? {
    val packageName = context.packageName
    val resIdMain = context.resources.getIdentifier("along_the_sunny_glade", "raw", packageName)
    val resIdShared = context.resources.getIdentifier("along_the_sunny_glade", "raw", "com.example.ar.shared")

    val possibleIds = listOfNotNull(
        if (resIdMain != 0) resIdMain else null,
        if (resIdShared != 0) resIdShared else null,
        R.raw.along_the_sunny_glade
    ).distinct()

    for (resId in possibleIds) {
        try {
            val player = MediaPlayer.create(context.applicationContext, resId)
            if (player != null) {
                player.isLooping = true
                player.setVolume(1.0f, 1.0f)
                return player
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return null
}

@Composable
actual fun ForestAudioPlayerController(
    isMusicEnabled: Boolean,
    isMusicPlaying: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentMusicEnabled by rememberUpdatedState(isMusicEnabled)
    val currentMusicPlaying by rememberUpdatedState(isMusicPlaying)

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(isMusicEnabled, isMusicPlaying) {
        if (isMusicEnabled && isMusicPlaying) {
            if (mediaPlayer == null) {
                mediaPlayer = createForestMediaPlayer(context)
            }
            try {
                mediaPlayer?.let { player ->
                    if (!player.isPlaying) {
                        player.start()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val player = mediaPlayer ?: return@LifecycleEventObserver
            try {
                when (event) {
                    Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                        if (player.isPlaying) {
                            player.pause()
                        }
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        if (currentMusicEnabled && currentMusicPlaying) {
                            if (!player.isPlaying) {
                                player.start()
                            }
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.release()
                }
                mediaPlayer = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

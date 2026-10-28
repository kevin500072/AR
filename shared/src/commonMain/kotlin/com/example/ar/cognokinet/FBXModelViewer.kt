package com.example.ar.cognokinet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun FBXModelViewer(
    modelFileName: String,
    modifier: Modifier = Modifier
)

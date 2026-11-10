package com.example.ar.cognokinet

import androidx.compose.runtime.Composable

@Composable
expect fun GoogleSignInButton(
    onIdToken: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
)

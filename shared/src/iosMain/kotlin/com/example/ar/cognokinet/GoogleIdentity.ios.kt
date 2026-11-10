package com.example.ar.cognokinet

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun GoogleSignInButton(onIdToken: (String) -> Unit, onError: (String) -> Unit, modifier: Modifier) {
    Button(onClick = { onError("El inicio con Google aún está disponible solo en Android.") }, modifier = modifier) {
        Text("Continuar con Google")
    }
}

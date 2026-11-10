package com.example.ar.cognokinet

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

private const val GOOGLE_WEB_CLIENT_ID =
    "619352913012-f0avb6ir3m54v56445oajdalardqhued.apps.googleusercontent.com"

@Composable
actual fun GoogleSignInButton(
    onIdToken: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            scope.launch {
                try {
                    val googleOption = GetGoogleIdOption.Builder()
                        .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .build()
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleOption)
                        .build()
                    val result = credentialManager.getCredential(context, request)
                    val credential = result.credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        onIdToken(GoogleIdTokenCredential.createFrom(credential.data).idToken)
                    } else {
                        onError("Google no devolvió una credencial válida.")
                    }
                } catch (error: GetCredentialException) {
                    onError(error.localizedMessage ?: "No se pudo iniciar sesión con Google.")
                } catch (error: Exception) {
                    onError(error.localizedMessage ?: "Error al iniciar sesión.")
                }
            }
        },
        modifier = modifier
    ) {
        Text("Continuar con Google")
    }
}

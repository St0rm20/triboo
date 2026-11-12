package edu.uniquindio.co.tribooo.features.login

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import edu.uniquindio.co.tribooo.R

private const val TAG = "GoogleSignIn"

/** Resultado del selector de cuentas de Google. */
sealed interface GoogleIdTokenResult {
    data class Success(val idToken: String) : GoogleIdTokenResult
    data object Cancelled : GoogleIdTokenResult
    data object Failure : GoogleIdTokenResult
}

/**
 * Abre el selector de cuentas de Google con Credential Manager y entrega el ID token para Firebase Authentication.
 * [context] debe ser el de la Activity, porque el selector se muestra sobre ella.
 */
suspend fun requestGoogleIdToken(context: Context): GoogleIdTokenResult {
    // Client ID web (tipo 3) del proyecto de Firebase; se lee de local.properties (GOOGLE_WEB_CLIENT_ID)
    val serverClientId = context.getString(R.string.google_web_client_id)
    if (serverClientId.isBlank()) {
        Log.e(TAG, "Falta GOOGLE_WEB_CLIENT_ID en local.properties")
        return GoogleIdTokenResult.Failure
    }

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(GetSignInWithGoogleOption.Builder(serverClientId).build())
        .build()

    return try {
        val credential = CredentialManager.create(context).getCredential(context, request).credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            GoogleIdTokenResult.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } else {
            Log.w(TAG, "La credencial recibida no es de Google: ${credential.type}")
            GoogleIdTokenResult.Failure
        }
    } catch (e: GetCredentialCancellationException) {
        GoogleIdTokenResult.Cancelled
    } catch (e: GetCredentialException) {
        Log.w(TAG, "No se pudo obtener la credencial de Google", e)
        GoogleIdTokenResult.Failure
    } catch (e: GoogleIdTokenParsingException) {
        Log.w(TAG, "ID token de Google inválido", e)
        GoogleIdTokenResult.Failure
    }
}

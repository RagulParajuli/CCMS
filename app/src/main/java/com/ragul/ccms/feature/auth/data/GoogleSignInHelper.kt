package com.ragul.ccms.feature.auth.data

import com.ragul.ccms.feature.auth.domain.AuthException

import android.annotation.SuppressLint
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.ragul.ccms.BuildConfig

/** Result of asking Credential Manager for a Google ID token. */
sealed interface GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data class Failure(val error: Throwable) : GoogleSignInResult
}

/**
 * Wraps Credential Manager's "Sign in with Google" flow.
 *
 * The activity context is required so Credential Manager can show the account picker. A new
 * helper is created per screen; the context is not retained beyond the sign-in attempt.
 */
class GoogleSignInHelper(private val context: Context) {

    suspend fun signIn(): GoogleSignInResult {
        val clientId = resolveWebClientId()
            ?: return GoogleSignInResult.Failure(AuthException.GoogleNotConfigured())

        return try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(context = context, request = request)
            val credential = response.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleSignInResult.Success(googleCredential.idToken)
            } else {
                GoogleSignInResult.Failure(
                    IllegalStateException("Unsupported credential type: ${credential.type}"),
                )
            }
        } catch (_: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (_: NoCredentialException) {
            GoogleSignInResult.Failure(AuthException.NoGoogleAccount())
        } catch (error: GetCredentialException) {
            val detail = listOfNotNull(error.type.takeIf { it.isNotBlank() }, error.message)
                .joinToString(": ")
            GoogleSignInResult.Failure(AuthException.GoogleSignInFailed(detail))
        } catch (error: GoogleIdTokenParsingException) {
            GoogleSignInResult.Failure(
                IllegalStateException("Google returned an invalid ID token.", error),
            )
        } catch (error: Exception) {
            GoogleSignInResult.Failure(error)
        }
    }

    /** Clears the Credential Manager state after sign-out. */
    suspend fun clearCredentialState() {
        runCatching {
            CredentialManager.create(context)
                .clearCredentialState(ClearCredentialStateRequest())
        }
    }

    private fun resolveWebClientId(): String? = resolveWebClientId(context)

    companion object {
        /**
         * Firebase puts the Web OAuth client ID in google-services.json and the plugin exposes
         * it as `default_web_client_id`. The gradle property is an explicit override.
         *
         * The resource is looked up by name because it only exists after the google-services
         * plugin has run, so it cannot be referenced as `R.string.default_web_client_id` here.
         */
        @SuppressLint("DiscouragedApi")
        fun resolveWebClientId(context: Context): String? {
            if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()) {
                return BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
            }

            val resourceId = context.resources.getIdentifier(
                "default_web_client_id",
                "string",
                context.packageName,
            )
            return if (resourceId != 0) {
                context.getString(resourceId).takeIf { it.isNotBlank() }
            } else {
                null
            }
        }

        /** True when a Web client ID is available for Credential Manager. */
        fun hasWebClientId(context: Context): Boolean = resolveWebClientId(context) != null
    }
}

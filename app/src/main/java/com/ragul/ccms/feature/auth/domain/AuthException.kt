package com.ragul.ccms.feature.auth.domain

/** Errors that are specific to this app's authentication setup. */
sealed class AuthException(message: String) : Exception(message) {
    class NotConfigured : AuthException(
        "Firebase is not configured. Add app/google-services.json and rebuild the app.",
    )

    class GoogleNotConfigured : AuthException(
        "Google Sign-In is not configured. Add the Web OAuth client to " +
            "app/google-services.json (client_type 3) or set GOOGLE_WEB_CLIENT_ID, then rebuild.",
    )

    class MissingUser : AuthException(
        "The account was authenticated but its profile could not be loaded. Please try again.",
    )

    class NoGoogleAccount : AuthException(
        "No usable Google account was found. Add a Google account in Android settings and make " +
            "sure this build's SHA-1 fingerprint is registered in Firebase.",
    )

    class GoogleSignInFailed(detail: String?) : AuthException(
        detail?.takeIf { it.isNotBlank() } ?: "Google Sign-In failed. Please try again.",
    )
}

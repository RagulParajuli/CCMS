package com.ragul.ccms.feature.auth.domain

/** How the current account authenticated. */
enum class AuthProvider {
    PASSWORD,
    GOOGLE,
}

/**
 * App-level representation of a Firebase user. Keeping this separate from `FirebaseUser`
 * makes the UI and tests independent of Firebase.
 */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val provider: AuthProvider,
) {
    /** First character used for the avatar fallback. */
    val initial: String
        get() = (displayName?.trim()?.firstOrNull() ?: email?.trim()?.firstOrNull() ?: 'U')
            .uppercase()

    val friendlyName: String
        get() = displayName?.trim()?.takeIf { it.isNotEmpty() }
            ?: email?.substringBefore('@')?.takeIf { it.isNotEmpty() }
            ?: "friend"
}

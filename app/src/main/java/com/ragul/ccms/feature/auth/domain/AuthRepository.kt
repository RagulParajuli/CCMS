package com.ragul.ccms.feature.auth.domain

import kotlinx.coroutines.flow.Flow

/**
 * Authentication boundary used by the ViewModel. The Firebase implementation is the only
 * production implementation today, but the interface keeps the UI testable.
 */
interface AuthRepository {
    /** Emits the currently signed-in user, or `null` when signed out. */
    val currentUser: Flow<AuthUser?>

    /** True when Firebase has been initialised from google-services.json. */
    fun isConfigured(): Boolean

    suspend fun signIn(email: String, password: String): AuthUser

    suspend fun signUp(displayName: String, email: String, password: String): AuthUser

    suspend fun signInWithGoogle(idToken: String): AuthUser

    suspend fun sendPasswordResetEmail(email: String)

    suspend fun resendVerificationEmail()

    fun signOut()
}

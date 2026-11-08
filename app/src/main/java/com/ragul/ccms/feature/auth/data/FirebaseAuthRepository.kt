package com.ragul.ccms.feature.auth.data

import com.ragul.ccms.feature.auth.domain.*

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

/**
 * Firebase Authentication implementation covering email/password and Google ID-token sign-in.
 */
class FirebaseAuthRepository(context: Context) : AuthRepository {

    private val appContext = context.applicationContext

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val firebaseAuth = authOrNull()
        if (firebaseAuth == null) {
            trySend(null)
            awaitClose()
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override fun isConfigured(): Boolean = FirebaseApp.getApps(appContext).isNotEmpty()

    override suspend fun signIn(email: String, password: String): AuthUser {
        val result = requireAuth().signInWithEmailAndPassword(email, password).await()
        return result.user?.toAuthUser() ?: throw AuthException.MissingUser()
    }

    override suspend fun signUp(displayName: String, email: String, password: String): AuthUser {
        val firebaseAuth = requireAuth()
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw AuthException.MissingUser()

        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build(),
        ).await()

        // A verification email is helpful but should not make sign-up fail.
        runCatching { user.sendEmailVerification().await() }

        return user.toAuthUser().copy(displayName = displayName)
    }

    override suspend fun signInWithGoogle(idToken: String): AuthUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = requireAuth().signInWithCredential(credential).await()
        return result.user?.toAuthUser() ?: throw AuthException.MissingUser()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        requireAuth().sendPasswordResetEmail(email).await()
    }

    override suspend fun resendVerificationEmail() {
        val user = requireAuth().currentUser ?: throw AuthException.MissingUser()
        user.sendEmailVerification().await()
    }

    override fun signOut() {
        authOrNull()?.signOut()
    }

    private fun authOrNull(): FirebaseAuth? =
        if (isConfigured()) FirebaseAuth.getInstance() else null

    private fun requireAuth(): FirebaseAuth = authOrNull() ?: throw AuthException.NotConfigured()

    private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
        isEmailVerified = isEmailVerified,
        provider = if (providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }) {
            AuthProvider.GOOGLE
        } else {
            AuthProvider.PASSWORD
        },
    )
}

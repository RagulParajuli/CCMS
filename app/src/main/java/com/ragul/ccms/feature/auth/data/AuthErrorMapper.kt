package com.ragul.ccms.feature.auth.data

import com.ragul.ccms.feature.auth.domain.AuthException

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/** Turns Firebase's technical errors into messages that are useful in the UI. */
fun Throwable.toAuthMessage(): String = when (this) {
    is AuthException -> message ?: "Authentication failed."
    is FirebaseAuthWeakPasswordException ->
        "Choose a stronger password (at least 8 characters with a letter and a number)."
    is FirebaseAuthUserCollisionException ->
        "An account already exists with that email address."
    is FirebaseAuthInvalidUserException -> when (errorCode) {
        "ERROR_USER_DISABLED" -> "This account has been disabled."
        "ERROR_USER_NOT_FOUND" -> "No account was found with that email address."
        else -> "This account is no longer available."
    }
    is FirebaseAuthInvalidCredentialsException -> when (errorCode) {
        "ERROR_INVALID_EMAIL" -> "Enter a valid email address."
        "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
        else -> "Incorrect email or password."
    }
    is FirebaseAuthRecentLoginRequiredException ->
        "Please sign in again to continue."
    is FirebaseNetworkException ->
        "Network error. Check your connection and try again."
    is FirebaseAuthException -> when (errorCode) {
        "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
        "ERROR_OPERATION_NOT_ALLOWED" ->
            "This sign-in method is not enabled in the Firebase console."
        "ERROR_INVALID_EMAIL" -> "Enter a valid email address."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "An account already exists with that email address."
        else -> message ?: "Authentication failed (${errorCode})."
    }
    else -> message?.takeIf { it.isNotBlank() }
        ?: "Something went wrong. Please try again."
}

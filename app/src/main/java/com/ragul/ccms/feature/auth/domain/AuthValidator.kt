package com.ragul.ccms.feature.auth.domain

/**
 * Validates credentials before they are sent to Firebase. Returning `null` means valid.
 */
object AuthValidator {
    private val emailPattern = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Enter your email address."
        !emailPattern.matches(email.trim()) -> "Enter a valid email address."
        else -> null
    }

    /** Login only needs a non-empty password; older accounts may not meet today's rules. */
    fun validatePassword(password: String): String? = when {
        password.isBlank() -> "Enter your password."
        else -> null
    }

    /** New passwords must be at least 8 characters and contain a letter and a number. */
    fun validateNewPassword(password: String): String? = when {
        password.isBlank() -> "Choose a password."
        password.length < 8 -> "Password must be at least 8 characters."
        password.none { it.isLetter() } -> "Password must contain a letter."
        password.none { it.isDigit() } -> "Password must contain a number."
        else -> null
    }

    fun validateName(name: String): String? =
        if (name.isBlank()) "Enter your name." else null

    fun validatePasswordConfirmation(password: String, confirmation: String): String? = when {
        confirmation.isBlank() -> "Confirm your password."
        password != confirmation -> "Passwords do not match."
        else -> null
    }
}

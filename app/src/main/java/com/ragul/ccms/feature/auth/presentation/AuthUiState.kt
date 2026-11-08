package com.ragul.ccms.feature.auth.presentation

import com.ragul.ccms.feature.auth.domain.AuthUser

/** Single source of truth for the authentication UI. */
data class AuthUiState(
    val user: AuthUser? = null,
    val isInitializing: Boolean = true,
    val isSubmitting: Boolean = false,
    val isConfigured: Boolean = true,
    val isGoogleConfigured: Boolean = true,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

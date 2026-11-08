package com.ragul.ccms.feature.auth.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp

@Composable
fun ForgotPasswordScreen(
    state: AuthUiState,
    onSendResetLink: (email: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }

    AuthScreen(modifier = modifier) {
        AuthHeader(
            title = "Reset password",
            subtitle = "Enter your email and we'll send you a reset link.",
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!state.isConfigured) {
            FirebaseSetupBanner()
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.errorMessage?.let { message ->
            MessageBanner(message = message, isError = true)
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.infoMessage?.let { message ->
            MessageBanner(message = message, isError = false)
            Spacer(modifier = Modifier.height(12.dp))
        }

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            enabled = !state.isSubmitting,
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryAuthButton(
            text = "Send reset link",
            isLoading = state.isSubmitting,
            onClick = { onSendResetLink(email) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthFooter(
            question = "Remembered your password?",
            action = "Log in",
            onActionClick = onNavigateToLogin,
        )
    }
}

@PreviewLightDark
@Composable
private fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(
        state = AuthUiState(isInitializing = false, isConfigured = true),
        onSendResetLink = {},
        onNavigateToLogin = {},
    )
}

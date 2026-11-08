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
fun SignupScreen(
    state: AuthUiState,
    onSignUp: (displayName: String, email: String, password: String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    AuthScreen(modifier = modifier) {
        AuthHeader(
            title = "Welcome!",
            subtitle = "Sign up to get started.",
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!state.isConfigured) {
            FirebaseSetupBanner()
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.isConfigured && !state.isGoogleConfigured) {
            GoogleSetupBanner()
            Spacer(modifier = Modifier.height(12.dp))
        }

        (localError ?: state.errorMessage)?.let { message ->
            MessageBanner(message = message, isError = true)
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.infoMessage?.let { message ->
            MessageBanner(message = message, isError = false)
            Spacer(modifier = Modifier.height(12.dp))
        }

        AuthTextField(
            value = displayName,
            onValueChange = {
                displayName = it
                localError = null
            },
            label = "Name",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            enabled = !state.isSubmitting,
        )

        Spacer(modifier = Modifier.height(8.dp))

        AuthTextField(
            value = email,
            onValueChange = {
                email = it
                localError = null
            },
            label = "Email",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            enabled = !state.isSubmitting,
        )

        Spacer(modifier = Modifier.height(8.dp))

        PasswordField(
            value = password,
            onValueChange = {
                password = it
                localError = null
            },
            imeAction = ImeAction.Next,
            enabled = !state.isSubmitting,
        )

        Spacer(modifier = Modifier.height(8.dp))

        PasswordField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                localError = null
            },
            label = "Confirm password",
            imeAction = ImeAction.Done,
            enabled = !state.isSubmitting,
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryAuthButton(
            text = "Create account",
            isLoading = state.isSubmitting,
            onClick = {
                localError = when {
                    password != confirmPassword -> "Passwords do not match."
                    else -> null
                }
                if (localError == null) {
                    onSignUp(displayName, email, password)
                }
            },
        )

        Spacer(modifier = Modifier.height(20.dp))

        OrDivider()

        Spacer(modifier = Modifier.height(20.dp))

        GoogleAuthButton(
            text = "Sign up with Google",
            isLoading = state.isSubmitting,
            onClick = onGoogleSignIn,
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthFooter(
            question = "Already have an account?",
            action = "Log in",
            onActionClick = onNavigateToLogin,
        )
    }
}

@PreviewLightDark
@Composable
private fun SignupScreenPreview() {
    SignupScreen(
        state = AuthUiState(isInitializing = false, isConfigured = true),
        onSignUp = { _, _, _ -> },
        onGoogleSignIn = {},
        onNavigateToLogin = {},
    )
}

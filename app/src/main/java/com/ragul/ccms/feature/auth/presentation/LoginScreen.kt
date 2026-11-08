package com.ragul.ccms.feature.auth.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ragul.ccms.ui.theme.AuthPrimary

@Composable
fun LoginScreen(
    state: AuthUiState,
    onSignIn: (email: String, password: String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onForgotPassword: () -> Unit,
    onNavigateToSignup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthScreen(modifier = modifier) {
        AuthHeader(
            title = "Good to see you!",
            subtitle = "Let's continue the journey.",
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
            imeAction = ImeAction.Next,
            enabled = !state.isSubmitting,
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            TextButton(
                onClick = onForgotPassword,
                enabled = !state.isSubmitting,
            ) {
                Text(
                    text = "Forgot password?",
                    color = AuthPrimary,
                    fontSize = 14.sp,
                )
            }
        }

        PasswordField(
            value = password,
            onValueChange = { password = it },
            enabled = !state.isSubmitting,
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryAuthButton(
            text = "Log In",
            isLoading = state.isSubmitting,
            onClick = { onSignIn(email, password) },
        )

        Spacer(modifier = Modifier.height(20.dp))

        OrDivider()

        Spacer(modifier = Modifier.height(20.dp))

        GoogleAuthButton(
            text = "Continue with Google",
            isLoading = state.isSubmitting,
            onClick = onGoogleSignIn,
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthFooter(
            question = "Don't have an account?",
            action = "Sign up",
            onActionClick = onNavigateToSignup,
        )
    }
}

@PreviewLightDark
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        state = AuthUiState(isInitializing = false, isConfigured = true),
        onSignIn = { _, _ -> },
        onGoogleSignIn = {},
        onForgotPassword = {},
        onNavigateToSignup = {},
    )
}

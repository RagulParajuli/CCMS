package com.ragul.ccms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ragul.ccms.feature.auth.domain.AuthException
import com.ragul.ccms.feature.auth.domain.AuthProvider
import com.ragul.ccms.feature.auth.domain.AuthUser
import com.ragul.ccms.feature.auth.data.GoogleSignInHelper
import com.ragul.ccms.feature.auth.data.GoogleSignInResult
import com.ragul.ccms.feature.auth.data.toAuthMessage
import com.ragul.ccms.feature.auth.presentation.AuthUiState
import com.ragul.ccms.feature.auth.presentation.AuthViewModel
import com.ragul.ccms.feature.auth.presentation.ForgotPasswordScreen
import com.ragul.ccms.feature.auth.presentation.LoginScreen
import com.ragul.ccms.feature.auth.presentation.SignupScreen
import com.ragul.ccms.feature.records.presentation.DashboardScreen
import com.ragul.ccms.ui.theme.AuthPrimary
import com.ragul.ccms.ui.theme.CCMSTheme
import com.ragul.ccms.ui.localization.AppLanguage
import com.ragul.ccms.ui.localization.LocalAppLanguage
import com.ragul.ccms.ui.localization.LocalSetAppLanguage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val demoUser = if (intent.getBooleanExtra("demo_dashboard", false)) {
            AuthUser(
                uid = "ui-smoke-test",
                email = "demo@example.com",
                displayName = "Demo User",
                photoUrl = null,
                isEmailVerified = true,
                provider = AuthProvider.PASSWORD,
            )
        } else {
            null
        }
        setContent {
            CCMSTheme {
                CCMSApp(demoUser)
            }
        }
    }
}

@Composable
private fun CCMSApp(demoUser: AuthUser? = null) {
    val context = LocalContext.current
    val preferences = remember(context) { context.getSharedPreferences("ccms_preferences", android.content.Context.MODE_PRIVATE) }
    var language by remember {
        mutableStateOf(runCatching { AppLanguage.valueOf(preferences.getString("language", "NEPALI") ?: "NEPALI") }.getOrDefault(AppLanguage.NEPALI))
    }
    val factory = remember { AuthViewModel.factory(context.applicationContext) }
    val viewModel: AuthViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val googleSignInHelper = remember(context) { GoogleSignInHelper(context) }

    val onGoogleSignIn: () -> Unit = {
        if (!state.isConfigured) {
            viewModel.showError(AuthException.NotConfigured().toAuthMessage())
        } else if (!state.isGoogleConfigured) {
            viewModel.showError(AuthException.GoogleNotConfigured().toAuthMessage())
        } else {
            scope.launch {
                when (val result = googleSignInHelper.signIn()) {
                    is GoogleSignInResult.Success -> {
                        viewModel.signInWithGoogle(result.idToken)
                    }

                    GoogleSignInResult.Cancelled -> {
                        viewModel.clearMessages()
                    }

                    is GoogleSignInResult.Failure -> {
                        viewModel.showError(result.error.toAuthMessage())
                    }
                }
            }
        }
    }

    val user = state.user ?: demoUser
    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalSetAppLanguage provides { selected ->
            language = selected
            preferences.edit().putString("language", selected.name).apply()
        },
    ) {
        when {
        state.isInitializing -> LoadingScreen()

        user != null -> DashboardScreen(
            user = user,
            authState = state,
            onSignOut = {
                viewModel.signOut()
                scope.launch { googleSignInHelper.clearCredentialState() }
            },
            onResendVerification = viewModel::resendVerificationEmail,
        )

        else -> AuthNavHost(
            state = state,
            viewModel = viewModel,
            onGoogleSignIn = onGoogleSignIn,
        )
        }
    }
}

@Composable
private fun AuthNavHost(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onGoogleSignIn: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Router.Login,
    ) {
        composable(Router.Login) {
            LoginScreen(
                state = state,
                onSignIn = viewModel::signIn,
                onGoogleSignIn = onGoogleSignIn,
                onForgotPassword = {
                    viewModel.clearMessages()
                    navController.navigate(Router.ForgotPassword) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSignup = {
                    viewModel.clearMessages()
                    navController.navigate(Router.Signup) {
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Router.Signup) {
            SignupScreen(
                state = state,
                onSignUp = viewModel::signUp,
                onGoogleSignIn = onGoogleSignIn,
                onNavigateToLogin = {
                    viewModel.clearMessages()
                    navController.navigate(Router.Login) {
                        popUpTo(Router.Login) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Router.ForgotPassword) {
            ForgotPasswordScreen(
                state = state,
                onSendResetLink = viewModel::sendPasswordResetEmail,
                onNavigateToLogin = {
                    viewModel.clearMessages()
                    navController.navigate(Router.Login) {
                        popUpTo(Router.Login) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = AuthPrimary)
    }
}

package com.ragul.ccms.feature.auth.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ragul.ccms.feature.auth.domain.AuthRepository
import com.ragul.ccms.feature.auth.domain.AuthUser
import com.ragul.ccms.feature.auth.domain.AuthValidator
import com.ragul.ccms.feature.auth.data.FirebaseAuthRepository
import com.ragul.ccms.feature.auth.data.GoogleSignInHelper
import com.ragul.ccms.feature.auth.data.toAuthMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns all authentication actions. Screens are stateless and call into this ViewModel.
 */
class AuthViewModel(
    private val repository: AuthRepository,
    isGoogleConfigured: Boolean = true,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isInitializing = true,
            isConfigured = repository.isConfigured(),
            isGoogleConfigured = isGoogleConfigured,
        ),
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            repository.currentUser
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isInitializing = false,
                            errorMessage = error.toAuthMessage(),
                        )
                    }
                }
                .collect { user ->
                    _uiState.update {
                        it.copy(
                            user = user,
                            isInitializing = false,
                            isSubmitting = false,
                        )
                    }
                }
        }
    }

    fun signIn(email: String, password: String) {
        val validationError = AuthValidator.validateEmail(email)
            ?: AuthValidator.validatePassword(password)
        if (validationError != null) {
            showError(validationError)
            return
        }
        runAuthAction { repository.signIn(email.trim(), password) }
    }

    fun signUp(displayName: String, email: String, password: String) {
        val validationError = AuthValidator.validateName(displayName)
            ?: AuthValidator.validateEmail(email)
            ?: AuthValidator.validateNewPassword(password)
        if (validationError != null) {
            showError(validationError)
            return
        }
        runAuthAction {
            repository.signUp(displayName.trim(), email.trim(), password)
        }
    }

    fun signInWithGoogle(idToken: String) {
        runAuthAction { repository.signInWithGoogle(idToken) }
    }

    fun sendPasswordResetEmail(email: String) {
        val validationError = AuthValidator.validateEmail(email)
        if (validationError != null) {
            showError(validationError)
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, errorMessage = null, infoMessage = null)
            }
            try {
                repository.sendPasswordResetEmail(email.trim())
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        infoMessage = "Password reset link sent to ${email.trim()}.",
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.toAuthMessage(),
                    )
                }
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, errorMessage = null, infoMessage = null)
            }
            try {
                repository.resendVerificationEmail()
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        infoMessage = "Verification email sent. Check your inbox.",
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.toAuthMessage(),
                    )
                }
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _uiState.update {
            it.copy(
                user = null,
                isSubmitting = false,
                errorMessage = null,
                infoMessage = null,
            )
        }
    }

    fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message, infoMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    private fun runAuthAction(action: suspend () -> AuthUser) {
        if (_uiState.value.isSubmitting) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, errorMessage = null, infoMessage = null)
            }
            try {
                val user = action()
                _uiState.update {
                    it.copy(
                        user = user,
                        isSubmitting = false,
                        errorMessage = null,
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.toAuthMessage(),
                    )
                }
            }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AuthViewModel(
                    repository = FirebaseAuthRepository(context),
                    isGoogleConfigured = GoogleSignInHelper.hasWebClientId(context),
                )
            }
        }
    }
}

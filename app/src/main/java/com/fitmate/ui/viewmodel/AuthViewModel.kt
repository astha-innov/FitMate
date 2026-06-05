package com.fitmate.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

open class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _phoneAuthState = MutableStateFlow<PhoneAuthUiState>(PhoneAuthUiState.Idle)
    val phoneAuthState: StateFlow<PhoneAuthUiState> = _phoneAuthState.asStateFlow()

    private val _passwordResetState = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val passwordResetState: StateFlow<PasswordResetState> = _passwordResetState.asStateFlow()

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var pendingPhoneNumber: String = ""

    fun signUp(
        email: String,
        password: String
    ) {

        viewModelScope.launch {

            try {

                _isLoading.value = true

                auth.createUserWithEmailAndPassword(
                    email,
                    password
                ).await()

                _authState.value = AuthState.Success

            } catch (e: Exception) {

                _authState.value =
                    AuthState.Error(
                        e.message ?: "Signup Failed"
                    )

            } finally {

                _isLoading.value = false
            }
        }
    }

    fun signIn(
        email: String,
        password: String
    ) {

        viewModelScope.launch {

            try {

                _isLoading.value = true

                auth.signInWithEmailAndPassword(
                    email,
                    password
                ).await()

                _authState.value = AuthState.Success

            } catch (e: Exception) {

                _authState.value =
                    AuthState.Error(
                        e.message ?: "Login Failed"
                    )

            } finally {

                _isLoading.value = false
            }
        }
    }

    fun signInWithCredential(
        credential: AuthCredential,
        failureLabel: String = "Authentication failed"
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                auth.signInWithCredential(credential).await()
                _authState.value = AuthState.Success
            } catch (exception: Exception) {
                _authState.value = AuthState.Error(exception.message ?: failureLabel)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendOtp(
        activity: Activity,
        rawPhoneNumber: String,
        forceResend: Boolean = false,
    ) {
        val phoneNumber = normalizeIndianPhoneNumber(rawPhoneNumber)

        if (phoneNumber == null) {
            _phoneAuthState.value = PhoneAuthUiState.Error("Enter a valid +91 phone number.")
            return
        }

        _phoneAuthState.value = PhoneAuthUiState.Sending(phoneNumber)
        pendingPhoneNumber = phoneNumber

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(credential)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                _phoneAuthState.value = PhoneAuthUiState.Error(phoneAuthMessage(exception))
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@AuthViewModel.verificationId = verificationId
                resendToken = token
                _phoneAuthState.value = PhoneAuthUiState.CodeSent(phoneNumber)
            }
        }

        val builder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (forceResend) {
            resendToken?.let(builder::setForceResendingToken)
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }

    fun verifyOtp(code: String) {
        val currentVerificationId = verificationId
        if (currentVerificationId.isNullOrBlank()) {
            _phoneAuthState.value = PhoneAuthUiState.Error("Request a fresh OTP before verifying.")
            return
        }

        if (!code.matches(Regex("^\\d{6}$"))) {
            _phoneAuthState.value = PhoneAuthUiState.Error("Enter the 6-digit OTP.")
            return
        }

        _phoneAuthState.value = PhoneAuthUiState.Verifying(pendingPhoneNumber)
        val credential = PhoneAuthProvider.getCredential(currentVerificationId, code)
        signInWithPhoneCredential(credential)
    }

    fun resendOtp(activity: Activity) {
        if (pendingPhoneNumber.isBlank()) {
            _phoneAuthState.value = PhoneAuthUiState.Error("Enter your phone number again to resend OTP.")
            return
        }
        sendOtp(activity, pendingPhoneNumber, forceResend = true)
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                auth.signInWithCredential(credential).await()
                _phoneAuthState.value = PhoneAuthUiState.Verified
                _authState.value = AuthState.Success
            } catch (exception: Exception) {
                _phoneAuthState.value = PhoneAuthUiState.Error(phoneAuthMessage(exception))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendPasswordReset(email: String) {
        val trimmedEmail = email.trim()

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _passwordResetState.value = PasswordResetState.Error("Enter a valid email address.")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _passwordResetState.value = PasswordResetState.Sending
                auth.sendPasswordResetEmail(trimmedEmail).await()
                _passwordResetState.value =
                    PasswordResetState.Success("Password reset link sent to your email.")
            } catch (exception: Exception) {
                _passwordResetState.value = PasswordResetState.Error(passwordResetMessage(exception))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun showError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun resetPhoneState() {
        _phoneAuthState.value = PhoneAuthUiState.Idle
    }

    fun resetPasswordResetState() {
        _passwordResetState.value = PasswordResetState.Idle
    }

    private fun normalizeIndianPhoneNumber(raw: String): String? {
        val trimmed = raw.trim().replace(" ", "").replace("-", "")
        val normalized = when {
            trimmed.matches(Regex("^\\+91\\d{10}$")) -> trimmed
            trimmed.matches(Regex("^91\\d{10}$")) -> "+$trimmed"
            trimmed.matches(Regex("^\\d{10}$")) -> "+91$trimmed"
            else -> null
        }
        return normalized
    }

    private fun phoneAuthMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid or expired OTP. Please try again."
            is FirebaseTooManyRequestsException -> "Too many OTP attempts. Please wait before trying again."
            else -> exception.message ?: "Phone authentication failed. Check your network and try again."
        }
    }

    private fun passwordResetMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthInvalidUserException -> "No account was found for this email."
            is FirebaseAuthInvalidCredentialsException -> "Enter a valid email address."
            else -> exception.message ?: "Could not send reset email. Check your network and try again."
        }
    }
}

sealed class AuthState {

    object Idle : AuthState()

    object Success : AuthState()

    data class Error(
        val message: String
    ) : AuthState()
}

sealed class PhoneAuthUiState {
    data object Idle : PhoneAuthUiState()
    data class Sending(val phoneNumber: String) : PhoneAuthUiState()
    data class CodeSent(val phoneNumber: String) : PhoneAuthUiState()
    data class Verifying(val phoneNumber: String) : PhoneAuthUiState()
    data object Verified : PhoneAuthUiState()
    data class Error(val message: String) : PhoneAuthUiState()
}

sealed class PasswordResetState {
    data object Idle : PasswordResetState()
    data object Sending : PasswordResetState()
    data class Success(val message: String) : PasswordResetState()
    data class Error(val message: String) : PasswordResetState()
}

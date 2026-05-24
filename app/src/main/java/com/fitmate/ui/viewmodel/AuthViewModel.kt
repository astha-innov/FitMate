package com.fitmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

open class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

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

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {

    object Idle : AuthState()

    object Success : AuthState()

    data class Error(
        val message: String
    ) : AuthState()
}
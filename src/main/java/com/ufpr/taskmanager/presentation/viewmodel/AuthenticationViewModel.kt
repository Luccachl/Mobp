package com.ufpr.taskmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthenticationViewModel(private val firebaseAuth: FirebaseAuth) : ViewModel() {

    private val _sessionState = MutableStateFlow<UserSessionStatus>(UserSessionStatus.Undefined)
    val sessionState = _sessionState.asStateFlow()

    val currentUserEmail: String?
        get() = firebaseAuth.currentUser?.email

    val currentUserName: String?
        get() = firebaseAuth.currentUser?.displayName?.ifEmpty { currentUserEmail?.split('@')?.first() }


    init {
        verifyActiveSession()
    }

    private fun verifyActiveSession() {
        _sessionState.value = UserSessionStatus.Loading
        if (firebaseAuth.currentUser != null) {
            _sessionState.value = UserSessionStatus.Active
        } else {
            _sessionState.value = UserSessionStatus.Inactive
        }
    }

    fun attemptLogin(email: String, password: String) {
        viewModelScope.launch {
            _sessionState.value = UserSessionStatus.Loading
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                _sessionState.value = UserSessionStatus.Active
            } catch (e: Exception) {
                _sessionState.value = UserSessionStatus.AuthError(e.localizedMessage ?: "Login attempt failed")
            }
        }
    }

    fun createAccount(email: String, password: String) {
        viewModelScope.launch {
            _sessionState.value = UserSessionStatus.Loading
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                _sessionState.value = UserSessionStatus.Active
            } catch (e: Exception) {
                _sessionState.value = UserSessionStatus.AuthError(e.localizedMessage ?: "Account creation failed")
            }
        }
    }

    fun endSession() {
        firebaseAuth.signOut()
        _sessionState.value = UserSessionStatus.Inactive
    }
}

sealed class UserSessionStatus {
    object Undefined : UserSessionStatus()
    object Active : UserSessionStatus()
    object Inactive : UserSessionStatus()
    object Loading : UserSessionStatus()
    data class AuthError(val errorMessage: String) : UserSessionStatus()
}
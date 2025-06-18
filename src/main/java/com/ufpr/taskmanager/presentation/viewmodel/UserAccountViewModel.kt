package com.ufpr.taskmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class UserAccountViewModel(private val firebaseAuth: FirebaseAuth) : ViewModel() {

    val currentUserEmail: String?
        get() = firebaseAuth.currentUser?.email

    val currentUserName: String?
        get() = firebaseAuth.currentUser?.displayName

    fun performLogout() {
        firebaseAuth.signOut()

    }
}
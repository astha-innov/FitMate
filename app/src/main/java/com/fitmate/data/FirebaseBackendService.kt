package com.fitmate.data

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class FirebaseBackendService {

    fun isConfigured(): Boolean {
        return runCatching {
            FirebaseApp.getInstance()
        }.isSuccess
    }

    fun isUserLoggedIn(): Boolean {
        return auth().currentUser != null
    }

    fun currentUserId(): String? {
        return auth().currentUser?.uid
    }

    fun signOut() {
        auth().signOut()
    }

    private fun auth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
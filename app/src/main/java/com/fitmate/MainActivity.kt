package com.fitmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fitmate.data.AppStorage
import com.fitmate.ui.navigation.NavGraph
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppStorage.init(applicationContext)

        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()

        enableEdgeToEdge()

        setContent {
            NavGraph()
        }
    }
}

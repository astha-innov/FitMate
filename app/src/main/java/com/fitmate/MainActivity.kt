//package com.fitmate
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatDelegate
//import androidx.core.os.LocaleListCompat
//import com.fitmate.data.AppStorage
//import com.fitmate.ui.navigation.NavGraph
//import com.google.firebase.FirebaseApp
//import com.google.firebase.auth.FirebaseAuth
//
//class MainActivity : ComponentActivity() {
//
//    private lateinit var auth: FirebaseAuth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        AppStorage.init(applicationContext)
//
//        android.util.Log.d("LANGUAGE", "Saved = ${AppStorage.loadLanguage()}")
//
//        val language = AppStorage.loadLanguage()
//
//        if (language == "en") {
//            AppCompatDelegate.setApplicationLocales(
//                LocaleListCompat.getEmptyLocaleList()
//            )
//        } else {
//            AppCompatDelegate.setApplicationLocales(
//                LocaleListCompat.forLanguageTags(language)
//            )
//        }
//
//        FirebaseApp.initializeApp(this)
//
//        auth = FirebaseAuth.getInstance()
//
//        enableEdgeToEdge()
//
//        setContent {
//            NavGraph()
//        }
//    }
//}

package com.fitmate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fitmate.data.AppStorage
import com.fitmate.ui.navigation.NavGraph
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AppCompatDelegate has ALREADY applied the persisted per-app locale
        // by this point (it hooks attachBaseContext internally). We must NOT
        // call setApplicationLocales() here again -- doing so on every launch
        // is what caused the "Hindi needs reinstall" / race-condition bugs.
        // Locale is only ever *set* in response to a user action (see
        // LanguageRepository.setLanguage), never re-asserted at startup.
        AppStorage.init(applicationContext)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        enableEdgeToEdge()

        setContent {
            NavGraph()
        }
    }
}
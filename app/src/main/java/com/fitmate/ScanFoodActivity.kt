package com.fitmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fitmate.ui.scanfood.ScanFoodScreen
import com.fitmate.ui.theme.FitMateTheme

class ScanFoodActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            FitMateTheme {
                ScanFoodScreen()
            }
        }
    }
}
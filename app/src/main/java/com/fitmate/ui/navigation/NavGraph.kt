package com.fitmate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitmate.ui.CampusFitApp
import com.fitmate.ui.splash.SplashScreen
import com.fitmate.ui.auth.SignInScreen
import com.fitmate.ui.auth.SignUpScreen
import com.fitmate.ui.viewmodel.AuthViewModel

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,

        // SPLASH FIRST
        startDestination = Routes.Splash.route
    ) {

        // ================= SPLASH =================

        composable(
            route = Routes.Splash.route
        ) {

            SplashScreen(

                onSplashFinished = {

                    navController.navigate(
                        Routes.SignUp.route
                    ) {

                        popUpTo(
                            Routes.Splash.route
                        ) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // ================= SIGN UP =================

        composable(
            route = Routes.SignUp.route
        ) {

            SignUpScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        // ================= SIGN IN =================

        composable(
            route = Routes.SignIn.route
        ) {

            SignInScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        // ================= HOME =================

        composable(
            route = Routes.Home.route
        ) {

            CampusFitApp()
        }
    }
}
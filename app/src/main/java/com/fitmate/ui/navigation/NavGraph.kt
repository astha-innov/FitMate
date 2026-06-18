package com.fitmate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitmate.ui.CampusFitApp
import com.fitmate.ui.auth.ForgotPasswordScreen
import com.fitmate.ui.auth.OtpVerificationScreen
import com.fitmate.ui.auth.SignInScreen
import com.fitmate.ui.auth.SignUpScreen
import com.fitmate.ui.coach.CoachChatScreen
import com.fitmate.ui.splash.SplashScreen
import com.fitmate.ui.viewmodel.AuthViewModel
import com.fitmate.data.AppStorage
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,

        startDestination = Routes.Splash.route
    ) {

        composable(
            route = Routes.Splash.route
        ) {

            SplashScreen(

                onSplashFinished = {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    currentUser?.uid?.let(AppStorage::prepareForUser)
                    navController.navigate(
                        if (currentUser != null) Routes.Home.route else Routes.SignUp.route
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

        composable(
            route = Routes.SignUp.route
        ) {

            SignUpScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(
            route = Routes.SignIn.route
        ) {

            SignInScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(
            route = Routes.OtpVerification.route
        ) {

            OtpVerificationScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(
            route = Routes.ForgotPassword.route
        ) {

            ForgotPasswordScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(
            route = Routes.Home.route
        ) {

            CampusFitApp()
        }

        composable(
            route = Routes.CoachChat.route
        ) {

            CoachChatScreen()
        }
    }
}

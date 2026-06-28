//package com.fitmate.ui.navigation
//
//import androidx.compose.runtime.Composable
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.fitmate.ui.CampusFitApp
//import com.fitmate.ui.auth.ForgotPasswordScreen
//import com.fitmate.ui.auth.OtpVerificationScreen
//import com.fitmate.ui.auth.SignInScreen
//import com.fitmate.ui.auth.SignUpScreen
//import com.fitmate.ui.coach.CoachChatScreen
//import com.fitmate.ui.splash.SplashScreen
//import com.fitmate.ui.viewmodel.AuthViewModel
//import com.fitmate.data.AppStorage
//import com.google.firebase.auth.FirebaseAuth
//
//@Composable
//fun NavGraph() {
//
//    val navController = rememberNavController()
//
//    val authViewModel: AuthViewModel = viewModel()
//
//    NavHost(
//        navController = navController,
//
//        startDestination = Routes.Splash.route
//    ) {
//
//        composable(
//            route = Routes.Splash.route
//        ) {
//
//            SplashScreen(
//
//                onSplashFinished = {
//
//                    FirebaseAuth.getInstance().currentUser?.uid?.let {
//                        AppStorage.prepareForUser(it)
//                    }
//
//                    navController.navigate(Routes.SignUp.route) {
//                        popUpTo(Routes.Splash.route) {
//                            inclusive = true
//                        }
//                    }
//                }
//            )
//        }
//
//        composable(
//            route = Routes.SignUp.route
//        ) {
//
//            SignUpScreen(
//                navController = navController,
//                viewModel = authViewModel
//            )
//        }
//
//        composable(
//            route = Routes.SignIn.route
//        ) {
//
//            SignInScreen(
//                navController = navController,
//                viewModel = authViewModel
//            )
//        }
//
//        composable(
//            route = Routes.OtpVerification.route
//        ) {
//
//            OtpVerificationScreen(
//                navController = navController,
//                viewModel = authViewModel
//            )
//        }
//
//        composable(
//            route = Routes.ForgotPassword.route
//        ) {
//
//            ForgotPasswordScreen(
//                navController = navController,
//                viewModel = authViewModel
//            )
//        }
//
//        composable(
//            route = Routes.Home.route
//        ) {
//
//            CampusFitApp()
//        }
//
//        composable(
//            route = Routes.CoachChat.route
//        ) {
//
//            CoachChatScreen()
//        }
//    }
//}

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

/**
 * NOTE on localization: this file intentionally contains NO locale-specific
 * code. Once language switching goes exclusively through
 * AppCompatDelegate.setApplicationLocales() (see LanguageRepository),
 * AppCompat automatically recreates the current resumed Activity, which
 * re-runs MainActivity.onCreate() -> setContent { NavGraph() } from
 * scratch. That means rememberNavController() and the whole NavHost are
 * rebuilt fresh with the new locale's resources every time. Adding
 * locale-aware logic here would be redundant and is exactly the kind of
 * unnecessary patch to avoid.
 */
@Composable
fun NavGraph() {

    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel()

    // Decide the real start destination once, at composition time. A
    // signed-in user should never see Sign Up / Sign In again after the
    // splash screen -- they should land on Home. This was previously
    // hardcoded to always go to SignUp, which silently sent logged-in
    // users back to the sign-up flow on every cold start (including after
    // a locale-triggered Activity recreate, which made it look like the
    // language switch had "broken navigation" when it hadn't).
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

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
                    currentUser?.uid?.let {
                        AppStorage.prepareForUser(it)
                    }

                    val destination = if (currentUser != null) {
                        Routes.Home.route
                    } else {
                        Routes.SignUp.route
                    }

                    navController.navigate(destination) {
                        popUpTo(Routes.Splash.route) {
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
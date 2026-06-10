package com.fitmate.ui.navigation

sealed class Routes(
    val route: String
) {

    data object Splash : Routes(
        route = "splash"
    )

    data object SignUp : Routes(
        route = "signup"
    )

    data object SignIn : Routes(
        route = "signin"
    )

    data object Welcome : Routes(
        route = "welcome"
    )

    data object Questions : Routes(
        route = "questions"
    )

    data object Personalizing : Routes(
        route = "personalizing"
    )

    data object Home : Routes(
        route = "home"
    )

    data object OtpVerification : Routes(
        route = "otp_verification"
    )

    data object ForgotPassword : Routes(
        route = "forgot_password"
    )

    data object CoachChat : Routes(
        route = "coach_chat"
    )
}
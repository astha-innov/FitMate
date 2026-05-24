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

    data object Home : Routes(
        route = "home"
    )
}
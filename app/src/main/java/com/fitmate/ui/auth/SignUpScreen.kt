package com.fitmate.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fitmate.R
import com.fitmate.ui.navigation.Routes
import com.fitmate.ui.theme.FitMateTheme
import com.fitmate.ui.viewmodel.AuthState
import com.fitmate.ui.viewmodel.AuthViewModel
import com.fitmate.ui.viewmodel.FakeAuthViewModel

@Suppress("DEPRECATION")
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val startGoogleSignIn = rememberGoogleSignInAction(viewModel)

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val loading by viewModel.isLoading.collectAsState()

    // PREMIUM ENTRANCE ANIMATIONS
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val logoScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )

    val cardOffsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 40.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "cardOffset"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800),
        label = "cardAlpha"
    )

    LaunchedEffect(authState) {

        if (authState is AuthState.Success) {
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.SignUp.route) {
                    inclusive = true
                }
            }

            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Retained project component
    ) {



        // WHITE HAZE OVERLAY - Converts the dark theme to a premium white theme
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color.White.copy(alpha = 0.98f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            // BRANDING
            Text(
                text = "FITMATE",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                    color = Color(0xFF10B981) // Retained project component
                ),
                modifier = Modifier.graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Create Account",
                color = Color(0xFF111827), // Updated for white theme
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold, // Improved typography
                letterSpacing = (-1).sp
            )

            Text(
                text = "Join the future of fitness today.",
                color = Color(0xFF6B7280), // Updated for white theme
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // GLASSMORPHISM CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = cardOffsetY.toPx()
                        alpha = cardAlpha
                    }
                    .shadow( // Added premium shadow
                        elevation = 30.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = Color.Black.copy(alpha = 0.05f),
                        ambientColor = Color.Transparent
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(GlassWhite) // Retained project component
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE5E7EB), // Retained project component
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(24.dp)
            ) {

                Column {

                    PremiumTextField( // Retained project component
                        value = username,
                        onValueChange = {
                            username = it
                        },
                        label = "Username",
                        icon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField( // Retained project component
                        value = email,
                        onValueChange = {
                            email = it
                        },
                        label = "Email",
                        icon = Icons.Default.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField( // Retained project component
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        label = "Password",
                        icon = Icons.Default.Lock,
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    PremiumButton( // Retained project component
                        text = "Create Account",
                        loading = loading,
                        onClick = {

                            viewModel.signUp(
                                email,
                                password
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color =  Color(0xFFE5E7EB) // Retained project component
                )

                Text(
                    text = "  OR  ",
                    color = Color(0xFF6B7280), // Updated for white theme
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color =  Color(0xFFE5E7EB) // Retained project component
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            PremiumSecondaryButton( // Retained project component
                text = "Continue with Google",
                onClick = startGoogleSignIn
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = {

                    navController.navigate(
                        Routes.SignIn.route
                    )
                }
            ) {

                Text(
                    text = "Already have an account? Sign In",
                    color = Color(0xFF6B7280), // Updated for white theme
                    fontWeight = FontWeight.SemiBold
                )
            }

            // ERROR STATE
            if (authState is AuthState.Error) {

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color(0xFFEF4444), // Improved modern red color
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {

    FitMateTheme {

        SignUpScreen(
            navController = rememberNavController(),
            viewModel = FakeAuthViewModel()
        )
    }
}

package com.fitmate.ui.auth

import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.fitmate.ui.navigation.Routes
import com.fitmate.ui.theme.FitMateTheme
import com.fitmate.ui.viewmodel.AuthState
import com.fitmate.ui.viewmodel.AuthViewModel
import com.fitmate.ui.viewmodel.FakeAuthViewModel
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.fitmate.R

@Suppress("DEPRECATION")
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)

                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    viewModel.showError("Google Sign-In did not return an ID token. Check Firebase SHA and OAuth client configuration.")
                    return@rememberLauncherForActivityResult
                }

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                viewModel.signInWithCredential(credential, "Google Sign-In failed")

            } catch (e: ApiException) {
                viewModel.showError("Google Sign-In failed: ${e.statusCode}. Check SHA fingerprints and google-services.json.")
            } catch (e: Exception) {
                viewModel.showError(e.message ?: "Google Sign-In failed.")
            }
        }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val loading by viewModel.isLoading.collectAsState()

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
            .background(DeepSpace)
    ) {

        // PREMIUM ANIMATED BACKGROUND
        AnimatedAmbientBackground()

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
                    color = NeonCyan.copy(alpha = 0.8f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Create Account",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )

            Text(
                text = "Join the future of fitness today.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // GLASSMORPHISM CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(GlassWhite)
                    .border(
                        width = 1.dp,
                        color = SurfaceBorder,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(24.dp)
            ) {

                Column {

                    PremiumTextField(
                        value = username,
                        onValueChange = {
                            username = it
                        },
                        label = "Username",
                        icon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField(
                        value = email,
                        onValueChange = {
                            email = it
                        },
                        label = "Email",
                        icon = Icons.Default.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField(
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        label = "Password",
                        icon = Icons.Default.Lock,
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    PremiumButton(
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
                    color = SurfaceBorder
                )

                Text(
                    text = "  OR  ",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = SurfaceBorder
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            PremiumSecondaryButton(
                text = "Continue with Google",
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                }
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
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // ERROR STATE
            if (authState is AuthState.Error) {

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color(0xFFFF4B4B)
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

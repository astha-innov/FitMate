package com.fitmate.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fitmate.ui.navigation.Routes
import com.fitmate.ui.viewmodel.AuthState
import com.fitmate.ui.viewmodel.AuthViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.fitmate.ui.viewmodel.FakeAuthViewModel
import com.fitmate.ui.theme.FitMateTheme

// --- PREMIUM DESIGN CONSTANTS ---
val NeonCyan = Color(0xFF00E5FF)
val DeepSpace = Color(0xFF05070A)
val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.05f)
val SurfaceBorder = Color(0xFFFFFFFF).copy(alpha = 0.12f)

@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val loading by viewModel.isLoading.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.SignIn.route) { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepSpace)) {
        // 1. CINEMATIC ANIMATED BACKGROUND
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

            // 2. BRANDING SECTION
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
                text = "Welcome Back",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )

            Text(
                text = "Enter your credentials to access your lab.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 3. GLASSMORPHISM AUTH CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(GlassWhite)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                Column {
                    PremiumTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        icon = Icons.Default.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        icon = Icons.Default.Lock,
                        isPassword = true
                    )

                    TextButton(
                        onClick = { /* Handle Forgot */ },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Forgot Password?", color = NeonCyan, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    PremiumButton(
                        text = "Sign In",
                        loading = loading,
                        onClick = { viewModel.signIn(email, password) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. SOCIAL & ALTERNATIVE AUTH
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorder)
                Text("  OR  ", color = Color.Gray, fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorder)
            }

            Spacer(modifier = Modifier.height(32.dp))

            PremiumSecondaryButton(
                text = "Continue with Google",
                onClick = { /* Google logic */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PremiumTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Phone Number",
                icon = Icons.Default.Phone
            )

            Spacer(modifier = Modifier.height(12.dp))

            PremiumButton(
                text = "Send OTP",
                isSecondary = true,
                onClick = { /* Send OTP logic */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { navController.navigate(Routes.SignUp.route) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Don't have an account? Sign Up",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Error Messaging
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color(0xFFFF4B4B),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// --- REUSABLE PREMIUM COMPONENTS ---

@Composable
fun AnimatedAmbientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 400f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse), label = ""
    )

    Canvas(modifier = Modifier.fillMaxSize().blur(80.dp)) {
        drawCircle(
            brush = Brush.radialGradient(listOf(NeonCyan.copy(alpha = 0.15f), Color.Transparent)),
            radius = 600f,
            center = center.copy(x = xOffset, y = 200f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFF7000FF).copy(alpha = 0.12f), Color.Transparent)),
            radius = 500f,
            center = center.copy(x = size.width - xOffset, y = size.height - 200f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        label = { Text(label, color = Color.Gray) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp)) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
            focusedIndicatorColor = NeonCyan,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun PremiumButton(
    text: String,
    loading: Boolean = false,
    isSecondary: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .graphicsLayer {
                if (!isSecondary) {
                    shadowElevation = 20f
                    spotShadowColor = NeonCyan
                }
            },
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSecondary) Color.White.copy(alpha = 0.1f) else Color.White,
            contentColor = if (isSecondary) Color.White else Color.Black
        ),
        interactionSource = interactionSource
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
        } else {
            Text(text.uppercase(), style = TextStyle(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp))
        }
    }
}

@Composable
fun PremiumSecondaryButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignInScreenPreview() {
    FitMateTheme {
        SignInScreen(
            navController = rememberNavController(),
            viewModel = FakeAuthViewModel()
        )
    }
}
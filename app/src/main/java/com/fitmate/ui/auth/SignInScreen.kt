package com.fitmate.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.fitmate.ui.viewmodel.PhoneAuthUiState

// --- PREMIUM DESIGN CONSTANTS ---
val PrimaryGreen = Color(0xFF10B981)
val SecondaryGreen = Color(0xFF34D399)
val PrimaryText = Color(0xFF111827)
val SecondaryText = Color(0xFF6B7280)
val CardBorder = Color(0xFFE5E7EB)
val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.82f)

fun Context.findActivity(): Activity {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> throw IllegalStateException("No Activity found")
    }
}

@Suppress("DEPRECATION")
@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    val startGoogleSignIn = rememberGoogleSignInAction(viewModel)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val phoneAuthState by viewModel.phoneAuthState.collectAsState()
    val loading by viewModel.isLoading.collectAsState()

    // --- ENTRANCE ANIMATION STATES ---
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val logoScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800),
        label = "logoAlpha"
    )
    val cardOffsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 60.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "cardOffset"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(1000, delayMillis = 200),
        label = "cardAlpha"
    )

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.SignIn.route) { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    LaunchedEffect(phoneAuthState) {
        if (phoneAuthState is PhoneAuthUiState.CodeSent) {
            navController.navigate(Routes.OtpVerification.route)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // 1. BACKGROUND IMAGE WITH ZOOM ANIMATION
        val infiniteTransition = rememberInfiniteTransition(label = "bg_zoom")
        val bgScale by infiniteTransition.animateFloat(
            initialValue = 1.0f, targetValue = 1.08f,
            animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse),
            label = "bgScale"
        )

        Image(
            painter = painterResource(id = R.drawable.body_transformation),
            contentDescription = stringResource(R.string.fitness_background),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(bgScale)
        )

        // 2. FROSTED OVERLAY — stronger at bottom so UI is always readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White.copy(alpha = 0.55f),
                            0.35f to Color.White.copy(alpha = 0.80f),
                            1.0f to Color.White.copy(alpha = 0.97f)
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

            // 3. BRANDING SECTION
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                    alpha = logoAlpha
                }
            ) {
                Text(
                    text = stringResource(R.string.fitmate_brand),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 8.sp,
                        color = PrimaryGreen
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.welcome_back),
                    color = PrimaryText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.signin_subtitle),
                    color = SecondaryText,
                    fontSize = 15.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 4. AUTH CARD — NO .blur() on the card itself; blur only lives in the overlay above
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = cardOffsetY.toPx()
                        alpha = cardAlpha
                    }
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = Color.Black.copy(alpha = 0.06f),
                        ambientColor = Color.Transparent
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(GlassWhite)
                    // *** REMOVED .blur(16.dp) — that was blurring card contents ***
                    .border(1.dp, CardBorder, RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                Column {
                    PremiumTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = stringResource(R.string.email),
                        icon = Icons.Default.Email
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.password),
                        icon = Icons.Default.Lock,
                        isPassword = true
                    )
                    TextButton(
                        onClick = { navController.navigate(Routes.ForgotPassword.route) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.forgot_password), color = PrimaryGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    PremiumButton(
                        text = stringResource(R.string.sign_in),
                        loading = loading,
                        onClick = { viewModel.signIn(email, password) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 5. SOCIAL & ALTERNATIVE AUTH
            Column(
                modifier = Modifier.graphicsLayer {
                    translationY = cardOffsetY.toPx() * 0.5f
                    alpha = cardAlpha
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorder)
                    Text(stringResource(R.string.or_divider), color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorder)
                }
                Spacer(modifier = Modifier.height(32.dp))
                PremiumSecondaryButton(
                    text = stringResource(R.string.continue_with_google),
                    onClick = startGoogleSignIn
                )
                Spacer(modifier = Modifier.height(16.dp))
                PremiumTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = stringResource(R.string.phone_number),
                    icon = Icons.Default.Phone
                )
                Spacer(modifier = Modifier.height(12.dp))
                PremiumSecondaryButton(
                    text = stringResource(R.string.send_otp),
                    loading = phoneAuthState is PhoneAuthUiState.Sending,
                    onClick = { viewModel.sendOtp(activity, phoneNumber) }
                )
                Spacer(modifier = Modifier.height(32.dp))
                TextButton(
                    onClick = { navController.navigate(Routes.SignUp.route) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = stringResource(R.string.no_account), color = SecondaryText, fontSize = 14.sp)
                    Text(text = stringResource(R.string.sign_up_link), color = PrimaryGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally),
                        fontSize = 13.sp
                    )
                }
                if (phoneAuthState is PhoneAuthUiState.Error) {
                    Text(
                        text = (phoneAuthState as PhoneAuthUiState.Error).message,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally),
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// --- REUSABLE PREMIUM COMPONENTS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) PrimaryGreen else Color.Transparent,
        label = "borderColor"
    )

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .onFocusChanged { isFocused = it.isFocused },
        label = { Text(label, color = SecondaryText) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isFocused) PrimaryGreen else SecondaryText,
                modifier = Modifier.size(20.dp)
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation()
        else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = PrimaryText,
            unfocusedTextColor = PrimaryText,
            cursorColor = PrimaryGreen
        ),
        singleLine = true
    )
}

@Composable
fun PremiumButton(
    text: String,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 4.dp else 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = PrimaryGreen.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreen,
            contentColor = Color.White
        ),
        interactionSource = interactionSource
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(
                text = text,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp)
            )
        }
    }
}

@Composable
fun PremiumSecondaryButton(
    text: String,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "secondaryButtonScale"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = PrimaryText
        ),
        interactionSource = interactionSource
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryText, strokeWidth = 2.dp)
        } else {
            Text(text = text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
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
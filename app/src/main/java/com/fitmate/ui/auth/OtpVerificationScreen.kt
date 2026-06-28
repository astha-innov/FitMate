package com.fitmate.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fitmate.R
import com.fitmate.ui.navigation.Routes
import com.fitmate.ui.viewmodel.AuthState
import com.fitmate.ui.viewmodel.AuthViewModel
import com.fitmate.ui.viewmodel.PhoneAuthUiState
import kotlinx.coroutines.delay

// NOTE: PrimaryGreen, PrimaryText, SecondaryText, CardBorder, GlassWhite are declared in
// SignInScreen.kt at package level and are used directly here — NO re-declaration.
// findActivity() extension is declared in SignInScreen.kt — used directly here — NO re-declaration.

@Composable
fun OtpVerificationScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val phoneState by viewModel.phoneAuthState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val otpDigits = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    var secondsLeft by remember { mutableStateOf(30) }
    var localError by remember { mutableStateOf<String?>(null) }

    val phoneNumber = when (val state = phoneState) {
        is PhoneAuthUiState.CodeSent -> state.phoneNumber
        is PhoneAuthUiState.Sending -> state.phoneNumber
        is PhoneAuthUiState.Verifying -> state.phoneNumber
        else -> ""
    }

    // --- ENTRANCE ANIMATIONS ---
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
        focusRequesters.first().requestFocus()
    }

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

    val logoScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )

    // --- LOGIC EFFECTS ---
    LaunchedEffect(phoneState) {
        if (phoneState is PhoneAuthUiState.CodeSent) {
            secondsLeft = 30
        }
    }

    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.SignIn.route) { inclusive = true }
            }
            viewModel.resetState()
            viewModel.resetPhoneState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. PREMIUM BACKGROUND WITH ZOOM ANIMATION
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

        // 2. WHITE HAZE / FROSTED OVERLAY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.65f),
                            Color.White.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 3. TOP BAR & BRANDING
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = PrimaryText
                    )
                }

                Text(
                    text = stringResource(R.string.fitmate_brand),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp,
                        color = PrimaryGreen
                    ),
                    modifier = Modifier.graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 4. HEADER TEXT
            Text(
                text = stringResource(R.string.verify_otp),
                color = PrimaryText,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )

            Text(
                text = if (phoneNumber.isNotBlank()) stringResource(R.string.otp_sent_to, phoneNumber) else stringResource(R.string.otp_sent_to_phone),
                color = SecondaryText,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 5. GLASSMORPHISM CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = cardOffsetY.toPx()
                        alpha = cardAlpha
                    }
                    .shadow(
                        elevation = 30.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = Color.Black.copy(alpha = 0.05f),
                        ambientColor = Color.Transparent
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(GlassWhite)
                    .blur(16.dp)
                    .border(1.dp, CardBorder, RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // OTP BOXES
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        otpDigits.forEachIndexed { index, digit ->
                            OtpDigitBox(
                                value = digit,
                                focusRequester = focusRequesters[index],
                                onValueChange = { raw ->
                                    localError = null
                                    val digitsOnly = raw.filter(Char::isDigit)
                                    if (digitsOnly.length > 1) {
                                        digitsOnly.take(6).forEachIndexed { pastedIndex, char ->
                                            otpDigits[pastedIndex] = char.toString()
                                        }
                                        focusRequesters[digitsOnly.take(6).lastIndex.coerceAtLeast(0)].requestFocus()
                                    } else {
                                        otpDigits[index] = digitsOnly.take(1)
                                        if (digitsOnly.isNotEmpty() && index < 5) {
                                            focusRequesters[index + 1].requestFocus()
                                        }
                                    }
                                },
                                onBackspace = {
                                    if (otpDigits[index].isBlank() && index > 0) {
                                        otpDigits[index - 1] = ""
                                        focusRequesters[index - 1].requestFocus()
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // TIMER & RESEND
                    Text(
                        text = if (secondsLeft > 0) stringResource(R.string.resend_code_in, secondsLeft) else stringResource(R.string.request_new_code),
                        color = SecondaryText,
                        fontSize = 14.sp
                    )

                    TextButton(
                        enabled = secondsLeft == 0 && phoneState !is PhoneAuthUiState.Sending,
                        onClick = {
                            otpDigits.indices.forEach { otpDigits[it] = "" }
                            localError = null
                            viewModel.resendOtp(activity)
                            focusRequesters.first().requestFocus()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.resend_otp),
                            color = if (secondsLeft == 0) PrimaryGreen else SecondaryText.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumButton(
                        text = stringResource(R.string.verify_now),
                        loading = loading || phoneState is PhoneAuthUiState.Verifying,
                        onClick = {
                            val code = otpDigits.joinToString("")
                            if (code.length != 6) {
                                localError = context.getString(R.string.error_incomplete_otp)
                            } else {
                                viewModel.verifyOtp(code)
                            }
                        }
                    )
                }
            }

            // 6. ERROR MESSAGING
            val serverError = (phoneState as? PhoneAuthUiState.Error)?.message
            val message = localError ?: serverError
            if (message != null) {
                Text(
                    text = message,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(top = 24.dp),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OtpDigitBox(
    value: String,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onBackspace: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "boxScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) PrimaryGreen else CardBorder,
        animationSpec = tween(200),
        label = "borderColor"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .size(width = 46.dp, height = 58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { event ->
                if (event.key == Key.Backspace) {
                    onBackspace()
                    false
                } else {
                    false
                }
            },
        singleLine = true,
        textStyle = TextStyle(
            color = PrimaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
            cursorColor = PrimaryGreen
        )
    )
}
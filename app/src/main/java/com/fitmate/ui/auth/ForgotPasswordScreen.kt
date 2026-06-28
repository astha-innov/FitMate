package com.fitmate.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fitmate.R
import com.fitmate.ui.viewmodel.AuthViewModel
import com.fitmate.ui.viewmodel.PasswordResetState

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    val resetState by viewModel.passwordResetState.collectAsState()
    val loading by viewModel.isLoading.collectAsState()

    // --- ENTRANCE ANIMATION STATES ---
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. PREMIUM BACKGROUND IMAGE WITH ZOOM ANIMATION
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
                            Color.White.copy(alpha = 0.6f),
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
                    onClick = {
                        viewModel.resetPasswordResetState()
                        navController.popBackStack()
                    },
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

            Spacer(modifier = Modifier.height(48.dp))

            // 4. HEADER TEXT
            Text(
                text = stringResource(R.string.reset_password),
                color = PrimaryText,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )

            Text(
                text = stringResource(R.string.reset_password_hint),
                color = SecondaryText,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 8.dp)
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
                Column {
                    PremiumTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = stringResource(R.string.email),
                        icon = Icons.Default.Email
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PremiumButton(
                        text = stringResource(R.string.send_reset_link),
                        loading = loading || resetState is PasswordResetState.Sending,
                        onClick = { viewModel.sendPasswordReset(email) }
                    )
                }
            }

            // 6. STATE MESSAGING
            when (val state = resetState) {
                is PasswordResetState.Success -> Text(
                    text = state.message,
                    color = PrimaryGreen,
                    modifier = Modifier.padding(top = 24.dp),
                    style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                    textAlign = TextAlign.Center
                )
                is PasswordResetState.Error -> Text(
                    text = state.message,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(top = 24.dp),
                    style = TextStyle(fontSize = 14.sp),
                    textAlign = TextAlign.Center
                )
                else -> Unit
            }
        }
    }
}
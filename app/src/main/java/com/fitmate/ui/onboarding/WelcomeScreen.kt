package com.fitmate.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.fitmate.R

// --- Premium Light Color Palette ---
val PureWhite = Color(0xFFFFFFFF)
val PrimaryText = Color(0xFF111827)
val SecondaryText = Color(0xFF6B7280)
val PrimaryButton = Color(0xFF10B981)
val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.85f)
val CardBorder = Color(0xFFFFFFFF).copy(alpha = 0.6f)

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        startAnimation = true
    }

    // --- Entrance Animations ---
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "LogoScale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1200, 300),
        label = "ContentAlpha"
    )
    val slideUpOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 40f,
        animationSpec = tween(1000, 300, easing = FastOutSlowInEasing),
        label = "SlideUp"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        // 1. Full Screen Hero Image
        Image(
            painter = painterResource(id = R.drawable.model),
            contentDescription = "Welcome Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Soft White Gradient Overlay (Enhances readability for foreground content)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.7f),
                            Color.White.copy(alpha = 0.95f)
                        ),
                        startY = 0f
                    )
                )
        )

        // 3. Floating Decorative Stat Cards
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingStatCard(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 100.dp, end = 24.dp),
                icon = Icons.Default.Whatshot,
                label = "Calories",
                value = "450 kcal",
                delay = 0,
                startAnimation = startAnimation
            )

            FloatingStatCard(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp, bottom = 180.dp),
                icon = Icons.Default.Favorite,
                label = "Heart Rate",
                value = "72 BPM",
                delay = 200,
                startAnimation = startAnimation
            )
        }

        // 4. Bottom Content Section (Glassmorphism Card)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
                .offset(y = slideUpOffset.dp)
                .alpha(contentAlpha)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = PrimaryText.copy(alpha = 0.08f),
                    ambientColor = PrimaryText.copy(alpha = 0.04f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(GlassWhite)
                .border(1.dp, CardBorder, RoundedCornerShape(32.dp))
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {

                // Branding Logo
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .scale(logoScale)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Typography & Branding Text
                Text(
                    text = "FITMATE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = PrimaryText
                )

                Text(
                    text = "Ultimate Fitness Partner",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryButton,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Transform your routine into a lifestyle with AI-driven tracking.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryText,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Primary Action Button
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = PrimaryButton.copy(alpha = 0.4f)
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryButton),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "GET STARTED",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = PureWhite
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingStatCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    delay: Int,
    startAnimation: Boolean
) {
    val animationSpecToUse: AnimationSpec<Float> = if (startAnimation) {
        spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
    } else {
        tween(0)
    }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = animationSpecToUse,
        label = "CardScale"
    )

    // Independent float for each card
    val infiniteTransition = rememberInfiniteTransition(label = "CardFloat")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 3000 + delay, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "Y"
    )

    Box(
        modifier = modifier
            .offset(y = yOffset.dp)
            .scale(scale)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = PrimaryText.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryButton,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = SecondaryText,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewWelcome() {
    WelcomeScreen {}
}
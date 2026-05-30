package com.fitmate.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
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

// --- Enhanced Color Palette ---
val NeonCyan = Color(0xFF00E5FF)
val DeepSpace = Color(0xFF05070A)
val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.08f)
val TextWhite = Color(0xFFF4F4F5)

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        startAnimation = true
    }

    // --- Entrance Animations ---
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "LogoScale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000, 400),
        label = "ContentAlpha"
    )

    // --- Continuous Background Motion ---
    val infiniteTransition = rememberInfiniteTransition(label = "Background")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "Float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
    ) {
        // 1. Background Glows (Orbital Depth)
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-150).dp, y = (-100).dp)
                .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                .blur(100.dp)
        )

        // 2. Floating Decorative Cards (The "NextLevel" part)
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingStatCard(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 20.dp),
                icon = Icons.Default.Whatshot,
                label = "Calories",
                value = "450 kcal",
                delay = 0,
                startAnimation = startAnimation
            )

            FloatingStatCard(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp, bottom = 100.dp),
                icon = Icons.Default.Favorite,
                label = "Heart Rate",
                value = "72 BPM",
                delay = 200,
                startAnimation = startAnimation
            )
        }

        // 3. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Container with Glassmorphism
            Box(
                modifier = Modifier
                    .offset(y = floatAnim.dp)
                    .size(170.dp)
                    .scale(logoScale)
                    .clip(RoundedCornerShape(48.dp))
                    .background(GlassWhite)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(NeonCyan, Color.Transparent)),
                        shape = RoundedCornerShape(48.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Text Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(contentAlpha)
            ) {
                Text(
                    text = "FITMATE",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = "Ultimate Fitness Partner",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Transform your routine into a lifestyle with AI-driven tracking.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextWhite.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Action Button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .alpha(contentAlpha)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = true
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "GET STARTED",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = DeepSpace
                )
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
    // Explicitly defining the if/else fixes the Kotlin type inference error
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
        targetValue = 15f,
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
            .clip(RoundedCornerShape(24.dp))
            .background(GlassWhite)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 10.sp, color = TextWhite.copy(alpha = 0.5f))
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}

@Preview
@Composable
fun PreviewWelcome() {
    WelcomeScreen {}
}
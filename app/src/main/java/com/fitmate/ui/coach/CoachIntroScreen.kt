package com.fitmate.ui.coach

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitmate.R

// Premium color definitions matching the light theme request
private val FitGreen = Color(0xFF10B981)
private val TextPrimary = Color(0xFF111111)
private val TextSecondary = Color(0xFF6B7280)
private val BackgroundWhite = Color(0xFFFFFFFF)
private val CardBackground = Color(0xFFF9FAFB)
private val HazeOverlay = Color(0x99000000) // Semi-transparent dark overlay

@Composable
fun CoachIntroScreen(
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // --- BASE LAYER: Original UI (Blurred/Alpha-reduced) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.4f)
        ) {
            // Top 55% Image Section
            Image(
                painter = painterResource(id = R.drawable.aicoach),
                contentDescription = stringResource(R.string.coach_intro_title),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
            )

            // Bottom Content Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.BottomCenter)
                    .background(
                        color = BackgroundWhite,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Title
                    Text(
                        text = stringResource(R.string.coach_intro_title),
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Subtitle
                    Text(
                        text = stringResource(R.string.coach_intro_body),
                        color = TextSecondary,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Features Card (Alpha reduced)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardBackground)
                            .padding(16.dp)
                            .alpha(0.5f)
                    ) {
                        FeatureItem(icon = "💪", title = stringResource(R.string.coach_feature_workouts))
                        Spacer(modifier = Modifier.height(16.dp))
                        FeatureItem(icon = "🥗", title = stringResource(R.string.coach_feature_nutrition))
                        Spacer(modifier = Modifier.height(16.dp))
                        FeatureItem(icon = "📈", title = stringResource(R.string.coach_feature_progress))
                        Spacer(modifier = Modifier.height(16.dp))
                        FeatureItem(icon = "🧠", title = stringResource(R.string.coach_feature_recovery))
                    }
                }
            }
        }

        // --- MIDDLE LAYER: Dark Haze Overlay ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(HazeOverlay, Color.Black.copy(alpha = 0.8f))
                    )
                )
        )

        // --- TOP LAYER: Coming Soon Premium Card ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lock Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.label_locked),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(FitGreen.copy(alpha = 0.2f))
                    .border(1.dp, FitGreen.copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.coach_active_development),
                    color = FitGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.coach_title),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = stringResource(R.string.label_coming_soon),
                color = FitGreen,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.coach_coming_soon_description),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // --- FOOTER: Disabled Button and Support Text ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { /* No action when disabled */ },
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = FitGreen.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                )
            ) {
                Text(
                    text = stringResource(R.string.coach_next_update),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.coach_support_message),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: String,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = icon,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
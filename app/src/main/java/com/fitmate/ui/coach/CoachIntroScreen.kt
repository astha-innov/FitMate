package com.fitmate.ui.coach

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitmate.R // 1. Added the correct R import matching your project structure

// Premium color definitions matching the light theme request
private val FitGreen = Color(0xFF10B981)
private val TextPrimary = Color(0xFF111111)
private val TextSecondary = Color(0xFF6B7280)
private val BackgroundWhite = Color(0xFFFFFFFF)
private val CardBackground = Color(0xFFF9FAFB)

@Composable
fun CoachIntroScreen(
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // Top 55% Image Section
        Image(
            painter = painterResource(id = R.drawable.aicoach), // 2. Removed the .jpg extension
            contentDescription = "FitMate AI Coach",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
        )

        // Bottom Content Section (Overlaps the image slightly with rounded corners)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f) // Take up bottom half to overlap with 55% image
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
                    text = "FitMate AI Coach",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Subtitle
                Text(
                    text = "Get full fitness guidance from our AI-powered personal trainer.",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Features Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .padding(16.dp)
                ) {
                    FeatureItem(icon = "💪", title = "Personalized Workout Plans")
                    Spacer(modifier = Modifier.height(16.dp))
                    FeatureItem(icon = "🥗", title = "Smart Nutrition Guidance")
                    Spacer(modifier = Modifier.height(16.dp))
                    FeatureItem(icon = "📈", title = "Progress Analysis")
                    Spacer(modifier = Modifier.height(16.dp))
                    FeatureItem(icon = "🧠", title = "Recovery & Wellness Advice")
                }
            }

            // Bottom CTA Button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitGreen,
                    contentColor = BackgroundWhite
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
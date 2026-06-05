package com.fitmate.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
    ) {
        AnimatedAmbientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    viewModel.resetPasswordResetState()
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Reset Password",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Enter your email and we will send a secure reset link.",
                color = Color.White.copy(alpha = 0.58f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(34.dp))

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

                    Spacer(modifier = Modifier.height(22.dp))

                    PremiumButton(
                        text = "Send Reset Link",
                        loading = loading || resetState is PasswordResetState.Sending,
                        onClick = { viewModel.sendPasswordReset(email) }
                    )
                }
            }

            when (val state = resetState) {
                is PasswordResetState.Success -> Text(
                    text = state.message,
                    color = NeonCyan,
                    modifier = Modifier.padding(top = 18.dp),
                    style = TextStyle(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center
                )
                is PasswordResetState.Error -> Text(
                    text = state.message,
                    color = Color(0xFFFF4B4B),
                    modifier = Modifier.padding(top = 18.dp),
                    textAlign = TextAlign.Center
                )
                else -> Unit
            }
        }
    }
}

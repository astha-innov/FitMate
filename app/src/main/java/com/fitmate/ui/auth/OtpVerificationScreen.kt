package com.fitmate.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fitmate.ui.navigation.Routes
import com.fitmate.ui.viewmodel.AuthState
import com.fitmate.ui.viewmodel.AuthViewModel
import com.fitmate.ui.viewmodel.PhoneAuthUiState
import kotlinx.coroutines.delay

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

    LaunchedEffect(Unit) {
        focusRequesters.first().requestFocus()
    }

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
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Verify OTP",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "We sent a verification code to ${phoneNumber.ifBlank { "your phone" }}",
                color = Color.White.copy(alpha = 0.58f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(34.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (secondsLeft > 0) "Resend code in: $secondsLeft seconds" else "You can request a new code now",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 14.sp
            )

            TextButton(
                enabled = secondsLeft == 0 && phoneState !is PhoneAuthUiState.Sending,
                onClick = {
                    otpDigits.indices.forEach { otpDigits[it] = "" }
                    localError = null
                    viewModel.resendOtp(activity)
                }
            ) {
                Text(
                    text = "Resend Again?",
                    color = if (secondsLeft == 0) NeonCyan else Color.White.copy(alpha = 0.32f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            PremiumButton(
                text = "Verify Now",
                loading = loading || phoneState is PhoneAuthUiState.Verifying,
                onClick = {
                    val code = otpDigits.joinToString("")
                    if (code.length != 6) {
                        localError = "Enter the complete 6-digit OTP."
                    } else {
                        viewModel.verifyOtp(code)
                    }
                }
            )

            val serverError = (phoneState as? PhoneAuthUiState.Error)?.message
            val message = localError ?: serverError
            if (message != null) {
                Text(
                    text = message,
                    color = Color(0xFFFF4B4B),
                    modifier = Modifier.padding(top = 16.dp),
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .size(width = 48.dp, height = 62.dp)
            .focusRequester(focusRequester)
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
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = SurfaceBorder,
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.025f),
            cursorColor = NeonCyan
        )
    )
}

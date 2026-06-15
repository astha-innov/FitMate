package com.fitmate.ui.more

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Patterns
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fitmate.MainActivity
import com.fitmate.data.AppStorage
import com.fitmate.data.FirebaseBackendService
import com.fitmate.domain.model.ActivityLevel
import com.fitmate.domain.model.ExperienceLevel
import com.fitmate.domain.model.FoodPreference
import com.fitmate.domain.model.GoalType
import com.fitmate.domain.model.UserProfile
import com.fitmate.ui.viewmodel.CampusFitUiState
import com.fitmate.ui.viewmodel.CampusFitViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── Shared Profile Palette Mapping ───────────────────────────────────────────
private val FitGreen      = Color(0xFF16C47F)   // primary accent — energetic, motivating
private val FitGreenLight = Color(0xFFE8FBF3)   // soft green tint for backgrounds
private val FitGreenDim   = Color(0xFF0FA363)   // slightly deeper for pressed/secondary
private val CanvasWhite   = Color(0xFFF7F9FC)   // page background
private val CardWhite     = Color(0xFFFFFFFF)   // card surfaces
private val TextDark      = Color(0xFF111827)   // primary text
private val TextSecondary = Color(0xFF6B7280)   // secondary / muted text
private val TextHint      = Color(0xFF9CA3AF)   // placeholder / label
private val DividerColor  = Color(0xFFF0F2F5)   // subtle divider
private val WarningRed    = Color(0xFFEF4444)   // delete alert color
private val WarningRedBg  = Color(0xFFFEE2E2)   // soft background for dangerous actions

// ── Remapped Legacy Color Names (Ensures Complete Compilation Integrity) ──────
private val MoreBg = CanvasWhite
private val MoreGlass = FitGreenLight
private val MoreBorder = DividerColor
private val MoreCard = CardWhite
private val MoreText = TextDark
private val MoreMuted = TextSecondary
private val MoreCyan = FitGreen
private val MoreGreen = FitGreenDim
private val MoreGold = Color(0xFFF59E0B) // Amber/Gold warning tone tailored for light UI panels
private val MoreRed = WarningRed

private enum class MoreDialog {
    EDIT_PROFILE, ACCOUNT_SECURITY, DATA_PRIVACY, FEEDBACK, LEGAL
}

@Composable
fun MoreScreen(
    state: CampusFitUiState,
    viewModel: CampusFitViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val backendService = remember { FirebaseBackendService() }
    var activeDialog by rememberSaveable { mutableStateOf<MoreDialog?>(null) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var privacyStatus by rememberSaveable { mutableStateOf<String?>(null) }
    var entered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        entered = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MoreBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(MoreCyan.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn() + slideInVertically { -it / 5 }
            ) {
                MoreHeader()
            }

            SettingsActionCard(
                title = "Edit Profile",
                subtitle = "Update the details you gave during setup.",
                icon = Icons.Outlined.EditNote,
                accent = MoreCyan,
                onClick = { activeDialog = MoreDialog.EDIT_PROFILE }
            )

            SettingsActionCard(
                title = "Account & Security",
                subtitle = "Manage your email, password, and account identity.",
                icon = Icons.Outlined.GppGood,
                accent = MoreGreen,
                onClick = { activeDialog = MoreDialog.ACCOUNT_SECURITY }
            )

            SettingsActionCard(
                title = "Data & Privacy",
                subtitle = "Clear app data or permanently delete your account.",
                icon = Icons.Outlined.PrivacyTip,
                accent = MoreGold,
                onClick = { activeDialog = MoreDialog.DATA_PRIVACY }
            )

            SettingsActionCard(
                title = "Feedback",
                subtitle = "Share ideas, issues, and suggestions for FitMate.",
                icon = Icons.Outlined.Feedback,
                accent = MoreCyan,
                onClick = { activeDialog = MoreDialog.FEEDBACK }
            )

            SettingsActionCard(
                title = "Legal & About",
                subtitle = "Read policies and learn what FitMate is about.",
                icon = Icons.Outlined.Info,
                accent = MoreGreen,
                onClick = { activeDialog = MoreDialog.LEGAL }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    backendService.signOut()
                    restartApp(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningRedBg,
                    contentColor = MoreRed
                ),
                border = BorderStroke(1.dp, MoreRed.copy(alpha = 0.35f)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Log out",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    when (activeDialog) {
        MoreDialog.EDIT_PROFILE -> EditProfileDialog(
            state = state,
            viewModel = viewModel,
            onDismiss = { activeDialog = null }
        )
        MoreDialog.ACCOUNT_SECURITY -> AccountSecurityDialog(
            auth = auth,
            onDismiss = { activeDialog = null }
        )
        MoreDialog.DATA_PRIVACY -> DataPrivacyDialog(
            message = privacyStatus,
            onDismiss = { activeDialog = null },
            onClearData = {
                scope.launch {
                    privacyStatus = runCatching {
                        backendService.clearUserDocument()
                        AppStorage.saveSetupCompleted(false)
                        restartApp(context)
                        "Your saved FitMate data has been cleared."
                    }.getOrElse { it.message ?: "Could not clear your data right now." }
                }
            },
            onDeleteAccount = { showDeleteConfirm = true }
        )
        MoreDialog.FEEDBACK -> FeedbackDialog(
            auth = auth,
            onDismiss = { activeDialog = null }
        )
        MoreDialog.LEGAL -> LegalAboutDialog(
            onDismiss = { activeDialog = null }
        )
        null -> Unit
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            privacyStatus = runCatching {
                                val currentUser = auth.currentUser ?: error("No active account found.")
                                backendService.clearUserDocument()
                                currentUser.delete().await()
                                auth.signOut()
                                AppStorage.saveSetupCompleted(false)
                                restartApp(context)
                                "Your account has been deleted."
                            }.getOrElse {
                                it.message ?: "Account deletion failed. Please sign in again and retry."
                            }
                        }
                    }
                ) {
                    Text("Delete", color = MoreRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = MoreMuted)
                }
            },
            containerColor = MoreCard,
            title = {
                Text("Delete Account", color = MoreText, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Upon clicking this, your account will be permanently deleted along with all your progress till now. Do you wish to continue?",
                    color = MoreMuted
                )
            }
        )
    }
}

@Composable
private fun MoreHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MoreCard),
        border = BorderStroke(1.dp, MoreBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "More Options",
                color = MoreText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Keep your account, profile, privacy, and app details in one clean place.",
                color = MoreMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MoreCard),
        border = BorderStroke(1.dp, MoreBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    color = MoreText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = MoreMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = TextHint
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    state: CampusFitUiState,
    viewModel: CampusFitViewModel,
    onDismiss: () -> Unit
) {
    var age by rememberSaveable { mutableStateOf(state.profile.age.toString()) }
    var height by rememberSaveable { mutableStateOf(state.profile.heightCm.toString()) }
    var weight by rememberSaveable { mutableStateOf(state.profile.weightKg.toString()) }
    var gender by rememberSaveable { mutableStateOf(state.profile.gender) }
    var budget by rememberSaveable { mutableStateOf(state.profile.budgetInr.toString()) }
    var workoutMinutes by rememberSaveable { mutableStateOf(state.profile.workoutMinutes.toString()) }
    var equipment by rememberSaveable {
        mutableStateOf(
            state.profile.equipment
                .toList()
                .sorted()
                .joinToString(", ")
        )
    }
    var goal by rememberSaveable { mutableStateOf(state.profile.goal) }
    var foodPreference by rememberSaveable { mutableStateOf(state.profile.foodPreference) }
    var activityLevel by rememberSaveable { mutableStateOf(state.profile.activityLevel) }
    var experienceLevel by rememberSaveable { mutableStateOf(state.profile.experienceLevel) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    MoreDialogShell(
        title = "Edit Profile",
        subtitle = "Change your saved setup information here.",
        onDismiss = onDismiss
    ) {
        MoreTextField("Age", age, { age = it.filter(Char::isDigit) }, KeyboardType.Number)
        MoreTextField("Height (cm)", height, { height = it.filter(Char::isDigit) }, KeyboardType.Number)
        MoreTextField("Weight (kg)", weight, { weight = it.filter(Char::isDigit) }, KeyboardType.Number)
        MoreTextField("Gender", gender, { gender = it })
        MoreTextField("Food budget (INR)", budget, { budget = it.filter(Char::isDigit) }, KeyboardType.Number)
        MoreTextField("Workout time (minutes)", workoutMinutes, { workoutMinutes = it.filter(Char::isDigit) }, KeyboardType.Number)
        MoreTextField("Equipment (comma separated)", equipment, { equipment = it })

        EnumSelector(
            title = "Body Goal",
            items = GoalType.entries,
            selected = goal,
            labelFor = GoalType::label,
            onSelect = { goal = it }
        )
        EnumSelector(
            title = "Food Preference",
            items = FoodPreference.entries,
            selected = foodPreference,
            labelFor = FoodPreference::label,
            onSelect = { foodPreference = it }
        )
        EnumSelector(
            title = "Activity Level",
            items = ActivityLevel.entries,
            selected = activityLevel,
            labelFor = ActivityLevel::label,
            onSelect = { activityLevel = it }
        )
        EnumSelector(
            title = "Fitness Level",
            items = ExperienceLevel.entries,
            selected = experienceLevel,
            labelFor = ExperienceLevel::label,
            onSelect = { experienceLevel = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.updateProfile(
                    state.profile.copy(
                        age = age.toIntOrNull() ?: state.profile.age,
                        heightCm = height.toIntOrNull() ?: state.profile.heightCm,
                        weightKg = weight.toIntOrNull() ?: state.profile.weightKg,
                        gender = gender.ifBlank { state.profile.gender },
                        budgetInr = budget.toIntOrNull() ?: state.profile.budgetInr,
                        workoutMinutes = workoutMinutes.toIntOrNull() ?: state.profile.workoutMinutes,
                        goal = goal,
                        foodPreference = foodPreference,
                        activityLevel = activityLevel,
                        experienceLevel = experienceLevel,
                        equipment = equipment
                            .split(",")
                            .mapNotNull { value ->
                                value.trim()
                                    .takeIf(String::isNotEmpty)
                            }
                            .toSet()
                            .ifEmpty { state.profile.equipment }
                    )
                )
                saveMessage = "Profile updated successfully."
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(99.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MoreCyan,
                contentColor = Color.White
            )
        ) {
            Text("Save Changes", fontWeight = FontWeight.Bold)
        }

        saveMessage?.let {
            Text(
                text = it,
                color = MoreGreen,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun AccountSecurityDialog(
    auth: FirebaseAuth,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentUser = auth.currentUser
    var newEmail by rememberSaveable { mutableStateOf(currentUser?.email.orEmpty()) }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val providerIds = currentUser
        ?.providerData
        ?.mapNotNull { provider ->
            provider.providerId.takeIf(String::isNotBlank)
        }
        ?.distinct()
        ?: emptyList()
    val hasPasswordProvider = providerIds.contains(EmailAuthProvider.PROVIDER_ID)
    val username = currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser?.email?.substringBefore("@")
        ?: "FitMate User"

    MoreDialogShell(
        title = "Account & Security",
        subtitle = "Review your account details and update secure credentials.",
        onDismiss = onDismiss
    ) {
        StaticInfoRow("Current email", currentUser?.email ?: "No email linked")
        StaticInfoRow("Username", username)
        StaticInfoRow("Sign-in method", providerIds.joinToString(", ").ifBlank { "Unknown" })
        StaticInfoRow("Password", "********")

        Spacer(modifier = Modifier.height(4.dp))

        MoreTextField("New email", newEmail, { newEmail = it }, KeyboardType.Email)
        MoreTextField("Current password", currentPassword, { currentPassword = it }, KeyboardType.Password, true)
        MoreTextField("New password", newPassword, { newPassword = it }, KeyboardType.Password, true)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        message = runCatching {
                            val user = auth.currentUser ?: error("No active user.")
                            if (!hasPasswordProvider) {
                                error("Email updates are only available for email-password accounts in this screen.")
                            }
                            if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                                error("Enter a valid email address.")
                            }
                            user.verifyBeforeUpdateEmail(newEmail).await()
                            "Verification email sent. Confirm it to finish updating your email."
                        }.getOrElse { it.message ?: "Could not update email." }
                    }
                },
                enabled = hasPasswordProvider,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoreCyan, contentColor = Color.White)
            ) {
                Text("Update Email", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Button(
                onClick = {
                    scope.launch {
                        message = runCatching {
                            val user = auth.currentUser ?: error("No active user.")
                            if (!hasPasswordProvider) {
                                error("Password updates are only available for email-password accounts in this screen.")
                            }
                            if (newPassword.length < 6) {
                                error("Password should be at least 6 characters.")
                            }
                            val email = user.email ?: error("No email linked to this account.")
                            if (currentPassword.isBlank()) {
                                error("Enter your current password to update the password.")
                            }
                            val credential = EmailAuthProvider.getCredential(email, currentPassword)
                            user.reauthenticate(credential).await()
                            user.updatePassword(newPassword).await()
                            "Password updated successfully."
                        }.getOrElse { it.message ?: "Could not update password." }
                    }
                },
                enabled = hasPasswordProvider,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoreGreen, contentColor = Color.White)
            ) {
                Text("Update Password", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (!hasPasswordProvider) {
            InfoNote(
                icon = Icons.Outlined.Lock,
                text = "This account is managed by Google or phone sign-in, so email and password updates are not handled here."
            )
        }

        message?.let {
            Text(
                text = it,
                color = if (it.contains("success", true) || it.contains("sent", true)) MoreGreen else MoreRed,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DataPrivacyDialog(
    message: String?,
    onDismiss: () -> Unit,
    onClearData: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    MoreDialogShell(
        title = "Data & Privacy",
        subtitle = "Control your saved FitMate data with care.",
        onDismiss = onDismiss
    ) {
        MoreWarningCard(
            title = "Clear Data",
            body = "This removes your saved profile setup, workout history, and progress from this account.",
            accent = MoreGold,
            buttonLabel = "Clear Data",
            onClick = onClearData
        )
        MoreWarningCard(
            title = "Delete Account",
            body = "This permanently removes your FitMate account and all progress tied to it.",
            accent = MoreRed,
            buttonLabel = "Delete Account",
            onClick = onDeleteAccount
        )
        message?.let {
            Text(
                text = it,
                color = if (it.contains("cleared", true) || it.contains("deleted", true)) MoreGreen else MoreRed,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FeedbackDialog(
    auth: FirebaseAuth,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var feedback by rememberSaveable { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    MoreDialogShell(
        title = "Feedback",
        subtitle = "Tell us what would make FitMate better for you.",
        onDismiss = onDismiss
    ) {
        OutlinedTextField(
            value = feedback,
            onValueChange = { feedback = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            placeholder = { Text("Write your feedback here...", color = TextHint) },
            colors = moreFieldColors(),
            shape = RoundedCornerShape(18.dp)
        )

        Button(
            onClick = {
                scope.launch {
                    message = runCatching {
                        if (feedback.isBlank()) {
                            error("Write a little feedback before submitting.")
                        }
                        if (runCatching { FirebaseApp.getInstance() }.isSuccess) {
                            FirebaseFirestore.getInstance()
                                .collection("feedback")
                                .add(
                                    hashMapOf(
                                        "uid" to (auth.currentUser?.uid ?: "anonymous"),
                                        "email" to (auth.currentUser?.email ?: ""),
                                        "message" to feedback.trim(),
                                        "createdAt" to FieldValue.serverTimestamp(),
                                    )
                                )
                                .await()
                        }
                        feedback = ""
                        "Thanks. Your feedback was submitted successfully."
                    }.getOrElse { it.message ?: "Could not submit feedback." }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(99.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MoreCyan, contentColor = Color.White)
        ) {
            Text("Submit", fontWeight = FontWeight.Bold)
        }

        message?.let {
            Text(
                text = it,
                color = if (it.contains("successfully", true) || it.contains("thanks", true)) MoreGreen else MoreRed,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LegalAboutDialog(
    onDismiss: () -> Unit
) {
    MoreDialogShell(
        title = "Legal & About",
        subtitle = "Key documents and a quick note about FitMate.",
        onDismiss = onDismiss
    ) {
        LegalBlock(
            icon = Icons.Outlined.Policy,
            title = "Terms of Service",
            body = "FitMate is designed to support healthy fitness planning and tracking. Use it responsibly and stop any activity that feels unsafe for your body."
        )
        LegalBlock(
            icon = Icons.Outlined.Shield,
            title = "Privacy Policy",
            body = "Your profile, workout progress, and feedback are used only to power your FitMate experience. Sensitive credentials are never shown in plain text."
        )
        LegalBlock(
            icon = Icons.Outlined.Info,
            title = "About",
            body = "FitMate is a workout-first fitness app built to help students and young professionals stay consistent with practical, customizable training plans."
        )
    }
}

@Composable
private fun InfoNote(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MoreCyan.copy(alpha = 0.08f))
            .border(1.dp, MoreCyan.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MoreCyan,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            color = MoreMuted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MoreDialogShell(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        containerColor = MoreCard,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    color = MoreText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    color = MoreMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                content()
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = MoreGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
private fun MoreTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        colors = moreFieldColors(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun moreFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MoreCyan,
    unfocusedBorderColor = MoreBorder,
    focusedTextColor = MoreText,
    unfocusedTextColor = MoreText,
    focusedContainerColor = CanvasWhite,
    unfocusedContainerColor = CanvasWhite,
    focusedLabelColor = MoreGreen,
    unfocusedLabelColor = TextHint,
    cursorColor = MoreCyan
)

@Composable
private fun StaticInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CanvasWhite)
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = MoreMuted, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, color = MoreText, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun LegalBlock(icon: ImageVector, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MoreGlass),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MoreCyan, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, color = MoreText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(text = body, color = MoreMuted, style = MaterialTheme.typography.bodySmall, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun MoreWarningCard(
    title: String,
    body: String,
    accent: Color,
    buttonLabel: String,
    onClick: () -> Unit
) {
    val isRedAction = accent == MoreRed
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isRedAction) WarningRedBg else Color(0xFFFEF3C7)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = title, color = MoreText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(text = body, color = MoreMuted, style = MaterialTheme.typography.bodySmall, lineHeight = 16.sp)
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
                shape = RoundedCornerShape(99.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = buttonLabel, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun <T> EnumSelector(
    title: String,
    items: List<T>,
    selected: T,
    labelFor: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = MoreText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.forEach { item ->
                val isSelected = item == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MoreGlass else CanvasWhite)
                        .border(
                            1.dp,
                            if (isSelected) MoreCyan else DividerColor,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(item) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = labelFor(item),
                        color = if (isSelected) MoreGreen else MoreMuted,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MoreCyan)
                        )
                    }
                }
            }
        }
    }
}

private fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    val activity = context.findActivity()
    activity?.finish()
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

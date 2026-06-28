package com.fitmate.ui.settings

import android.util.Patterns
import androidx.compose.runtime.mutableIntStateOf
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.sp
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.fitmate.R
import com.fitmate.data.AppStorage
import com.fitmate.data.FirebaseBackendService
import com.fitmate.data.LanguageRepository
import com.fitmate.domain.model.ActivityLevel
import com.fitmate.domain.model.ExperienceLevel
import com.fitmate.domain.model.FoodPreference
import com.fitmate.domain.model.GoalType
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
private val FitGreen      = Color(0xFF16C47F)
private val FitGreenLight = Color(0xFFE8FBF3)
private val FitGreenDim   = Color(0xFF0FA363)
private val CanvasWhite   = Color(0xFFF7F9FC)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextDark      = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val TextHint      = Color(0xFF9CA3AF)
private val DividerColor  = Color(0xFFF0F2F5)
private val WarningRed    = Color(0xFFEF4444)
private val WarningRedBg  = Color(0xFFFEE2E2)

private val MoreBg = CanvasWhite
private val MoreGlass = FitGreenLight
private val MoreBorder = DividerColor
private val MoreCard = CardWhite
private val MoreText = TextDark
private val MoreMuted = TextSecondary
private val MoreCyan = FitGreen
private val MoreGreen = FitGreenDim
private val MoreGold = Color(0xFFF59E0B)
private val MoreRed = WarningRed

private enum class SettingsDialog {
    EDIT_PROFILE, ACCOUNT_SECURITY, DATA_PRIVACY, FEEDBACK, LEGAL, LANGUAGE
}

private data class LanguageOption(
    val code: String,
    val flag: String,
    @StringRes val labelResId: Int
)

private val LanguageOptions = listOf(
    LanguageOption("en", "🇬🇧", R.string.lang_english),
    LanguageOption("hi", "🇮🇳", R.string.lang_hindi),
    LanguageOption("bn", "🇮🇳", R.string.lang_bengali),
    LanguageOption("ta", "🇮🇳", R.string.lang_tamil),
    LanguageOption("te", "🇮🇳", R.string.lang_telugu),
    LanguageOption("mr", "🇮🇳", R.string.lang_marathi),
    LanguageOption("kn", "🇮🇳", R.string.lang_kannada)
)

@Composable
fun SettingsScreen(
    state: CampusFitUiState,
    viewModel: CampusFitViewModel
) {
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val backendService = remember { FirebaseBackendService() }
    var activeDialog by rememberSaveable { mutableStateOf<SettingsDialog?>(null) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var privacyStatus by rememberSaveable { mutableStateOf<String?>(null) }
    var privacyIsSuccess by rememberSaveable { mutableStateOf(true) }

    // Derived fresh on every composition straight from AppCompatDelegate.
    // Never cached in rememberSaveable -- this guarantees the selector can
    // never disagree with the locale that is actually applied, including
    // when changed via system Settings > App Info > Language.
    val currentLanguageCode = LanguageRepository.currentLanguageCode()
    android.util.Log.d(
        "LANGUAGE",
        "currentLanguageCode = $currentLanguageCode"
    )
    val selectedLanguageIndex = LanguageOptions
        .indexOfFirst { it.code == currentLanguageCode }
        .coerceAtLeast(0)

    val dataClearedMessage = stringResource(R.string.msg_data_cleared)
    val dataClearFailedMessage = stringResource(R.string.msg_data_clear_failed)
    val noActiveAccountMessage = stringResource(R.string.msg_no_active_account)
    val accountDeletedMessage = stringResource(R.string.msg_account_deleted)
    val accountDeleteFailedMessage = stringResource(R.string.msg_account_delete_failed)

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
            SettingsActionCard(
                title = stringResource(R.string.settings_edit_profile_title),
                subtitle = stringResource(R.string.settings_edit_profile_subtitle),
                icon = Icons.Outlined.EditNote,
                accent = MoreCyan,
                onClick = { activeDialog = SettingsDialog.EDIT_PROFILE }
            )

            SettingsActionCard(
                title = stringResource(R.string.settings_account_security_title),
                subtitle = stringResource(R.string.settings_account_security_subtitle),
                icon = Icons.Outlined.GppGood,
                accent = MoreGreen,
                onClick = { activeDialog = SettingsDialog.ACCOUNT_SECURITY }
            )

            SettingsActionCard(
                title = stringResource(R.string.settings_data_privacy_title),
                subtitle = stringResource(R.string.settings_data_privacy_subtitle),
                icon = Icons.Outlined.PrivacyTip,
                accent = MoreGold,
                onClick = { activeDialog = SettingsDialog.DATA_PRIVACY }
            )

            SettingsActionCard(
                title = stringResource(R.string.settings_feedback_title),
                subtitle = stringResource(R.string.settings_feedback_subtitle),
                icon = Icons.Outlined.Feedback,
                accent = MoreCyan,
                onClick = { activeDialog = SettingsDialog.FEEDBACK }
            )

            SettingsActionCard(
                title = stringResource(R.string.settings_legal_title),
                subtitle = stringResource(R.string.settings_legal_subtitle),
                icon = Icons.Outlined.Info,
                accent = MoreGreen,
                onClick = { activeDialog = SettingsDialog.LEGAL }
            )

            SettingsActionCard(
                title = stringResource(R.string.settings_language_title),
                subtitle = stringResource(R.string.settings_language_subtitle),
                icon = Icons.Outlined.Language,
                accent = MoreCyan,
                onClick = { activeDialog = SettingsDialog.LANGUAGE }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    backendService.signOut()
                    // No restart. Auth state change is observed elsewhere
                    // (NavGraph / AuthViewModel) and drives navigation back
                    // to sign-in. Locale is untouched -- it's not part of
                    // user-scoped storage and remains correctly applied.
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
                    text = stringResource(R.string.log_out),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    when (activeDialog) {
        SettingsDialog.EDIT_PROFILE -> EditProfileDialog(
            state = state,
            viewModel = viewModel,
            onDismiss = { activeDialog = null }
        )
        SettingsDialog.ACCOUNT_SECURITY -> AccountSecurityDialog(
            auth = auth,
            onDismiss = { activeDialog = null }
        )
        SettingsDialog.DATA_PRIVACY -> DataPrivacyDialog(
            message = privacyStatus,
            isSuccess = privacyIsSuccess,
            onDismiss = { activeDialog = null },
            onClearData = {
                scope.launch {
                    val result = runCatching {
                        backendService.clearUserDocument()
                        AppStorage.saveSetupCompleted(false)
                        dataClearedMessage
                        // Setup-completed flip is observed by CampusFitApp's
                        // own state flow, which naturally routes back to
                        // onboarding. No restart required.
                    }
                    privacyIsSuccess = result.isSuccess
                    privacyStatus = result.getOrElse { it.message ?: dataClearFailedMessage }
                }
            },
            onDeleteAccount = { showDeleteConfirm = true }
        )
        SettingsDialog.FEEDBACK -> FeedbackDialog(
            auth = auth,
            onDismiss = { activeDialog = null }
        )
        SettingsDialog.LEGAL -> LegalAboutDialog(
            onDismiss = { activeDialog = null }
        )
        SettingsDialog.LANGUAGE -> LanguageSelectionDialog(
            selectedIndex = selectedLanguageIndex,
            onSelect = { index ->

                android.util.Log.d(
                    "LANGUAGE",
                    "Clicked index=$index code=${LanguageOptions[index].code}"
                )

                LanguageRepository.setLanguage(
                    LanguageOptions[index].code
                )

                activeDialog = null
            },
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
                            val result = runCatching {
                                val currentUser = auth.currentUser ?: error(noActiveAccountMessage)
                                backendService.clearUserDocument()
                                currentUser.delete().await()
                                auth.signOut()
                                AppStorage.saveSetupCompleted(false)
                                accountDeletedMessage
                                // Auth state flip drives navigation back to
                                // SignIn automatically -- no restart needed.
                            }
                            privacyIsSuccess = result.isSuccess
                            privacyStatus = result.getOrElse { it.message ?: accountDeleteFailedMessage }
                        }
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MoreRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel), color = MoreMuted)
                }
            },
            containerColor = MoreCard,
            title = {
                Text(stringResource(R.string.delete_confirm_title), color = MoreText, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    stringResource(R.string.delete_confirm_body),
                    color = MoreMuted
                )
            }
        )
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
    val profileUpdatedMessage = stringResource(R.string.msg_profile_updated)

    MoreDialogShell(
        title = stringResource(R.string.edit_profile_dialog_title),
        subtitle = stringResource(R.string.edit_profile_dialog_subtitle),
        onDismiss = onDismiss
    ) {
        MoreTextField(stringResource(R.string.field_age), age, { age = it.filter(Char::isDigit) }, KeyboardType.Number)
        MoreTextField(stringResource(R.string.field_height_cm), height, { height = it.filter(Char::isDigit) }, KeyboardType.Number)
        MoreTextField(stringResource(R.string.field_weight_kg), weight, { weight = it.filter(Char::isDigit) }, KeyboardType.Number)
        MoreTextField(stringResource(R.string.field_gender), gender, { gender = it })
        MoreTextField(stringResource(R.string.field_budget_inr), budget, { budget = it.filter(Char::isDigit) }, KeyboardType.Number)
        MoreTextField(stringResource(R.string.field_workout_minutes), workoutMinutes, { workoutMinutes = it.filter(Char::isDigit) }, KeyboardType.Number)
        MoreTextField(stringResource(R.string.field_equipment), equipment, { equipment = it })

        EnumSelector(
            title = stringResource(R.string.selector_body_goal),
            items = GoalType.entries,
            selected = goal,
            labelFor = GoalType::label,
            onSelect = { goal = it }
        )
        EnumSelector(
            title = stringResource(R.string.selector_food_pref),
            items = FoodPreference.entries,
            selected = foodPreference,
            labelFor = FoodPreference::label,
            onSelect = { foodPreference = it }
        )
        EnumSelector(
            title = stringResource(R.string.selector_activity_level),
            items = ActivityLevel.entries,
            selected = activityLevel,
            labelFor = ActivityLevel::label,
            onSelect = { activityLevel = it }
        )
        EnumSelector(
            title = stringResource(R.string.selector_fitness_level),
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
                saveMessage = profileUpdatedMessage
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(99.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MoreCyan,
                contentColor = Color.White
            )
        ) {
            Text(stringResource(R.string.save_changes), fontWeight = FontWeight.Bold)
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
    var messageIsSuccess by remember { mutableStateOf(true) }
    val providerIds = currentUser
        ?.providerData
        ?.mapNotNull { provider ->
            provider.providerId.takeIf(String::isNotBlank)
        }
        ?.distinct()
        ?: emptyList()
    val hasPasswordProvider = providerIds.contains(EmailAuthProvider.PROVIDER_ID)
    val unknownLabel = stringResource(R.string.label_unknown)
    val noEmailLabel = stringResource(R.string.label_no_email)
    val fitmateUserLabel = stringResource(R.string.fitmate_user_fallback)
    val username = currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser?.email?.substringBefore("@")
        ?: fitmateUserLabel

    val noActiveUser = stringResource(R.string.msg_no_active_user)
    val emailUpdateUnsupported = stringResource(R.string.msg_email_update_unsupported)
    val invalidEmail = stringResource(R.string.msg_invalid_email)
    val emailVerificationSent = stringResource(R.string.msg_email_verification_sent)
    val emailUpdateFailed = stringResource(R.string.msg_email_update_failed)
    val passwordUpdateUnsupported = stringResource(R.string.msg_password_update_unsupported)
    val passwordTooShort = stringResource(R.string.msg_password_too_short)
    val noEmailLinked = stringResource(R.string.msg_no_email_linked)
    val currentPasswordRequired = stringResource(R.string.msg_current_password_required)
    val passwordUpdateFailed = stringResource(R.string.msg_password_update_failed)
    val passwordUpdatedMessage = stringResource(R.string.msg_password_updated)

    MoreDialogShell(
        title = stringResource(R.string.account_dialog_title),
        subtitle = stringResource(R.string.account_dialog_subtitle),
        onDismiss = onDismiss
    ) {
        StaticInfoRow(stringResource(R.string.label_current_email), currentUser?.email ?: noEmailLabel)
        StaticInfoRow(stringResource(R.string.label_username), username)
        StaticInfoRow(stringResource(R.string.label_signin_method), providerIds.joinToString(", ").ifBlank { unknownLabel })
        StaticInfoRow(stringResource(R.string.label_password), stringResource(R.string.label_password_hidden))

        Spacer(modifier = Modifier.height(4.dp))

        MoreTextField(stringResource(R.string.field_new_email), newEmail, { newEmail = it }, KeyboardType.Email)
        MoreTextField(stringResource(R.string.field_current_password), currentPassword, { currentPassword = it }, KeyboardType.Password, true)
        MoreTextField(stringResource(R.string.field_new_password), newPassword, { newPassword = it }, KeyboardType.Password, true)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        val result = runCatching {
                            val user = auth.currentUser ?: error(noActiveUser)
                            if (!hasPasswordProvider) error(emailUpdateUnsupported)
                            if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) error(invalidEmail)
                            user.verifyBeforeUpdateEmail(newEmail).await()
                            emailVerificationSent
                        }
                        messageIsSuccess = result.isSuccess
                        message = result.getOrElse { it.message ?: emailUpdateFailed }
                    }
                },
                enabled = hasPasswordProvider,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoreCyan, contentColor = Color.White)
            ) {
                Text(stringResource(R.string.update_email), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Button(
                onClick = {
                    scope.launch {
                        val result = runCatching {
                            val user = auth.currentUser ?: error(noActiveUser)
                            if (!hasPasswordProvider) error(passwordUpdateUnsupported)
                            if (newPassword.length < 6) error(passwordTooShort)
                            val email = user.email ?: error(noEmailLinked)
                            if (currentPassword.isBlank()) error(currentPasswordRequired)
                            val credential = EmailAuthProvider.getCredential(email, currentPassword)
                            user.reauthenticate(credential).await()
                            user.updatePassword(newPassword).await()
                        }
                        messageIsSuccess = result.isSuccess
                        message = if (result.isSuccess) passwordUpdatedMessage
                        else result.exceptionOrNull()?.message ?: passwordUpdateFailed
                    }
                },
                enabled = hasPasswordProvider,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoreGreen, contentColor = Color.White)
            ) {
                Text(stringResource(R.string.update_password), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (!hasPasswordProvider) {
            InfoNote(
                icon = Icons.Outlined.Lock,
                text = stringResource(R.string.google_signin_note)
            )
        }

        message?.let {
            Text(
                text = it,
                color = if (messageIsSuccess) MoreGreen else MoreRed,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DataPrivacyDialog(
    message: String?,
    isSuccess: Boolean,
    onDismiss: () -> Unit,
    onClearData: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    MoreDialogShell(
        title = stringResource(R.string.data_dialog_title),
        subtitle = stringResource(R.string.data_dialog_subtitle),
        onDismiss = onDismiss
    ) {
        MoreWarningCard(
            title = stringResource(R.string.clear_data_title),
            body = stringResource(R.string.clear_data_body),
            accent = MoreGold,
            buttonLabel = stringResource(R.string.clear_data_button),
            onClick = onClearData
        )
        MoreWarningCard(
            title = stringResource(R.string.delete_account_title),
            body = stringResource(R.string.delete_account_body),
            accent = MoreRed,
            buttonLabel = stringResource(R.string.delete_account_button),
            onClick = onDeleteAccount
        )
        message?.let {
            Text(
                text = it,
                color = if (isSuccess) MoreGreen else MoreRed,
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
    var messageIsSuccess by remember { mutableStateOf(true) }
    val feedbackEmptyMessage = stringResource(R.string.msg_feedback_empty)
    val feedbackSubmittedMessage = stringResource(R.string.msg_feedback_submitted)
    val feedbackFailedMessage = stringResource(R.string.msg_feedback_failed)

    MoreDialogShell(
        title = stringResource(R.string.feedback_dialog_title),
        subtitle = stringResource(R.string.feedback_dialog_subtitle),
        onDismiss = onDismiss
    ) {
        OutlinedTextField(
            value = feedback,
            onValueChange = { feedback = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            placeholder = { Text(stringResource(R.string.feedback_placeholder), color = TextHint) },
            colors = moreFieldColors(),
            shape = RoundedCornerShape(18.dp)
        )

        Button(
            onClick = {
                scope.launch {
                    val result = runCatching {
                        if (feedback.isBlank()) error(feedbackEmptyMessage)
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
                        feedbackSubmittedMessage
                    }
                    messageIsSuccess = result.isSuccess
                    message = result.getOrElse { it.message ?: feedbackFailedMessage }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(99.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MoreCyan, contentColor = Color.White)
        ) {
            Text(stringResource(R.string.submit), fontWeight = FontWeight.Bold)
        }

        message?.let {
            Text(
                text = it,
                color = if (messageIsSuccess) MoreGreen else MoreRed,
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
        title = stringResource(R.string.legal_dialog_title),
        subtitle = stringResource(R.string.legal_dialog_subtitle),
        onDismiss = onDismiss
    ) {
        LegalBlock(
            icon = Icons.Outlined.Policy,
            title = stringResource(R.string.legal_tos_title),
            body = stringResource(R.string.legal_tos_body)
        )
        LegalBlock(
            icon = Icons.Outlined.Shield,
            title = stringResource(R.string.legal_privacy_title),
            body = stringResource(R.string.legal_privacy_body)
        )
        LegalBlock(
            icon = Icons.Outlined.Info,
            title = stringResource(R.string.legal_about_title),
            body = stringResource(R.string.legal_about_body)
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val englishNameForResId = remember {
        mapOf(
            R.string.lang_english to "English",
            R.string.lang_hindi to "Hindi",
            R.string.lang_bengali to "Bengali",
            R.string.lang_tamil to "Tamil",
            R.string.lang_telugu to "Telugu",
            R.string.lang_marathi to "Marathi",
            R.string.lang_kannada to "Kannada"
        )
    }

    MoreDialogShell(
        title = stringResource(R.string.language_dialog_title),
        subtitle = stringResource(R.string.settings_language_subtitle),
        onDismiss = onDismiss
    ) {
        LanguageOptions.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            val isDefault = index == 0

            val containerColor by animateColorAsState(
                targetValue = if (isSelected) Color.Transparent else CardWhite,
                animationSpec = tween(durationMillis = 260),
                label = "languageCardContainerColor"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) Color.Transparent else DividerColor,
                animationSpec = tween(durationMillis = 260),
                label = "languageCardBorderColor"
            )
            val primaryTextColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else MoreText,
                animationSpec = tween(durationMillis = 260),
                label = "languageCardPrimaryTextColor"
            )
            val secondaryTextColor by animateColorAsState(
                targetValue = if (isSelected) Color.White.copy(alpha = 0.85f) else MoreMuted,
                animationSpec = tween(durationMillis = 260),
                label = "languageCardSecondaryTextColor"
            )
            val cardElevation by animateDpAsState(
                targetValue = if (isSelected) 10.dp else 1.dp,
                animationSpec = tween(durationMillis = 260),
                label = "languageCardElevation"
            )
            val cardScale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.99f,
                animationSpec = tween(durationMillis = 220),
                label = "languageCardScale"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(cardScale)
                    .shadow(
                        elevation = cardElevation,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = MoreCyan.copy(alpha = 0.25f),
                        spotColor = MoreCyan.copy(alpha = 0.35f)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        brush = if (isSelected) {
                            Brush.linearGradient(listOf(MoreCyan, MoreGreen))
                        } else {
                            Brush.linearGradient(listOf(containerColor, containerColor))
                        }
                    )
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickable { onSelect(index) },
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.18f)
                                else MoreGlass
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.flag,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(option.labelResId),
                                color = primaryTextColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (isDefault) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(
                                            if (isSelected) Color.White.copy(alpha = 0.22f)
                                            else MoreGlass
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.lang_default_badge),
                                        color = if (isSelected) Color.White else MoreGreen,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        Text(
                            text = englishNameForResId[option.labelResId].orEmpty(),
                            color = secondaryTextColor,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.22f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 0.dp else 1.5.dp,
                                color = if (isSelected) Color.Transparent else DividerColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
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
                    Text(stringResource(R.string.close), color = MoreGreen, fontWeight = FontWeight.Bold)
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
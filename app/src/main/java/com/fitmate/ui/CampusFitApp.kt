

package com.fitmate.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitmate.domain.model.AppThemeMode
import com.fitmate.ui.navigation.AppNavigation
import com.fitmate.ui.onboarding.PersonalizingScreen
import com.fitmate.ui.onboarding.QuestionsScreen
import com.fitmate.ui.onboarding.WelcomeScreen
import com.fitmate.ui.theme.FitMateTheme
import com.fitmate.ui.viewmodel.CampusFitViewModel

private enum class IntroStage {
    LOADING,
    WELCOME,
    QUESTIONS,
    PERSONALIZING,
    HOME
}

@Composable
fun CampusFitApp(
    viewModel: CampusFitViewModel =
        viewModel(factory = CampusFitViewModel.Factory)
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val personalizationState by
    viewModel.personalizationState.collectAsStateWithLifecycle()

    val sessionReady by viewModel.sessionReady.collectAsStateWithLifecycle()

    var stage by rememberSaveable {
        mutableStateOf(IntroStage.LOADING)
    }

    LaunchedEffect(sessionReady, uiState.setupCompleted) {
        if (sessionReady && stage == IntroStage.LOADING) {
            stage = IntroStage.WELCOME
        }
    }

    // Move to personalization screen
    LaunchedEffect(personalizationState.isRunning) {
        if (personalizationState.isRunning) {
            stage = IntroStage.PERSONALIZING
        }
    }

    // Move to home after setup/personalization completes
    LaunchedEffect(
        personalizationState.isRunning,
        uiState.setupCompleted
    ) {

        if (
            stage == IntroStage.PERSONALIZING &&
            !personalizationState.isRunning &&
            uiState.setupCompleted
        ) {
            stage = IntroStage.HOME
        }
    }

    FitMateTheme(
        darkTheme = uiState.themeMode == AppThemeMode.DARK
    ) {

        when (stage) {

            IntroStage.LOADING -> Unit

            IntroStage.WELCOME -> {

                WelcomeScreen(
                    onGetStarted = {
                        stage = IntroStage.QUESTIONS
                    }
                )
            }

            IntroStage.QUESTIONS -> {

                QuestionsScreen { profile ->

                    viewModel.bootstrapPersonalization(
                        profile
                    )
                }
            }

            IntroStage.PERSONALIZING -> {

                PersonalizingScreen(
                    state = personalizationState,
                    onBackToSetup = {
                        stage = IntroStage.QUESTIONS
                    }
                )
            }

            IntroStage.HOME -> {

                AppNavigation(
                    state = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

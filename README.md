# FitMate

FitMate is an Android fitness companion built for students and young professionals who want practical guidance, not generic plans. The app helps users set up a personalized routine, track meals, understand nutrition, and stay consistent with AI-assisted suggestions that adapt to their goals.

## What FitMate Does

- Guides new users through a personalized startup flow
- Builds a smart body goal system based on user details and goals
- Tracks daily calories, protein, and water progress
- Lets users describe meals in natural language for AI meal analysis
- Generates goal-aware nutrition suggestions and workout direction
- Saves progress, streaks, and personalized memory across sessions

## Core Features

- `AI-first onboarding`
  The app collects personal details and AI connection settings, then creates a personalized baseline plan.

- `Smart body goal system`
  Daily targets for calories, protein, and water are shown with progress-based feedback instead of static text only.

- `Meal analysis`
  Users can describe breakfast, lunch, snacks, or dinner in plain language and get estimated nutrition plus suggestions.

- `Diet and workout guidance`
  The app stores a persistent personalized plan so users are not starting from zero every time they open it.

- `Streak and consistency tracking`
  FitMate tracks daily goal completion and gives encouragement around long-term consistency.

- `Theme and AI settings`
  Users can switch light/dark mode and manage AI provider settings from inside the app.

## Built With

- `Kotlin`
- `Jetpack Compose`
- `Firebase Authentication`
- `Cloud Firestore`
- `StateFlow`
- Remote AI APIs or local LLM endpoints

## Project Structure

- [`app`](C:\fitness app\app)
  Main Android application module
- [`app/src/main/java/com/fitmate`](C:\fitness app\app\src\main\java\com\fitmate)
  App source code
- [`app/src/main/res`](C:\fitness app\app\src\main\res)
  Android resources, including the FitMate logo
- [`firestore.rules`](C:\fitness app\firestore.rules)
  Firestore security rules for the backend

## Notes

- Firebase is backend infrastructure for the app and is not something end users set up manually.
- The app currently supports both remote AI APIs and local LLM-style endpoints.
- For a production release, AI secrets should ideally move behind a controlled backend instead of being handled directly in the client.

# FitMate

FitMate is a workout-focused Android fitness app built with Kotlin, Jetpack Compose, Firebase, and a modern neon-dark design system.

The app helps users build a weekly training routine, customize each workout day, adjust exercises, sets, and reps, and open exercise-specific instruction pages with visual demonstrations. FitMate is designed to make workout planning feel flexible, personal, and easy to stick to.

## What FitMate Does

FitMate currently focuses on the workout side of fitness:

- user onboarding and profile setup
- Firebase authentication
- weekly workout planning
- custom workout split creation
- day-by-day workout editing
- exercise-level set and rep adjustment
- exercise difficulty indicators
- rest day assignment
- workout instruction pages with GIFs and step-by-step guidance
- progress and profile screens
- Firebase-backed persistence for saved user state

## Core Experience

After signing in, users move into the main app experience with four core sections:

- `Profile`
- `Workout`
- `Progress`
- `More`

The workout page is the main feature area and supports both a default plan and a custom plan flow.

## Workout Features

### Weekly Workout Planning
Users can choose a default plan or create a custom weekly plan. The custom flow guides users through each weekday one by one instead of forcing all 7 days at once.

### Custom Split Builder
Users can:

- assign workout focus by weekday
- repeat workout focuses across multiple days
- set rest days
- save the plan for future app launches

### Day Editing
Each workout day can be edited individually. Users can:

- change the day focus
- add or remove exercises
- change sets
- change reps or duration
- mark the day as rest

### Exercise Difficulty Feedback
Every exercise includes a difficulty pill that updates based on selected workload. This gives users a quick sense of whether the selected setup is easy, medium, or hard.

### Exercise Instructions
Each exercise has an `Instructions` action that opens a dedicated detail screen showing:

- the exercise GIF
- formatted step-by-step instructions
- a back action to return to the workout page

## Tech Stack

### Android
- Kotlin
- Jetpack Compose
- Material 3
- Coroutines
- StateFlow
- MVVM-style state handling

### Backend and Persistence
- Firebase Authentication
- Cloud Firestore
- SharedPreferences for local cached state

### Media
- Coil for GIF and image rendering

## Project Structure

```text
app/
  src/main/java/com/fitmate/
    ai/
    data/
    domain/
    ui/
      auth/
      more/
      navigation/
      onboarding/
      profile/
      progress/
      workout/
      viewmodel/
    MainActivity.kt

  src/main/assets/
    exercises/
    workout_details/
      gifs/
      instructions/


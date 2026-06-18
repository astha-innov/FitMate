# FitMate Session Migration

Last audited: 2026-06-18 (Asia/Calcutta)

This file is the authoritative handoff for continuing work on FitMate in a new Codex session. It was produced by inspecting the current worktree, not only from conversation history. Read this file and run the checks in **New Session Startup Checklist** before editing anything.

## 1. Critical Snapshot

- Repository root: `C:\fitness app`
- Active branch: `main`
- Current commit: `74ba007 Fix Gemini API integration and improve AI coach`
- Remote tracking state at audit time: `main` matched `origin/main` before considering uncommitted work.
- Android application ID and namespace: `com.fitmate`
- Firebase project in `app/google-services.json`: `fitmate-f4bad`
- The worktree is intentionally dirty. Do not reset, clean, restore, checkout, or rebase before reviewing the status described below.
- The current uncommitted application changes are a coherent feature set and must be preserved.

### Verification recorded for this exact tree

- `./gradlew.bat :app:compileDebugKotlin`: **BUILD SUCCESSFUL** on 2026-06-18.
- `./gradlew.bat :app:testDebugUnitTest`: **BUILD SUCCESSFUL** on 2026-06-18.
- Unit-test report: `GenerateWorkoutScheduleUseCaseTest`, 5 tests, 0 skipped, 0 failures, 0 errors.
- `git diff --check -- SESSION_MIGRATION.md app/src/main`: passed; only Git line-ending notices were printed.
- Search for stale `app_logo` or `DumbbellIcon` code references under `app/src/main/java/com/fitmate`: no matches.
- Emulator/device and live Firebase flows were not exercised during the migration-file audit and remain manual verification items.

### Current uncommitted application feature set

The following work is present locally but not committed at the audited commit:

1. Firebase session-aware startup and per-account local-cache ownership.
2. Shared Google sign-in launcher used by both sign-in and sign-up.
3. Sign-up success now enters the app instead of routing back to sign-in.
4. Backend bootstrap readiness prevents onboarding/home routing before Firestore state is loaded.
5. New official FitMate logo component using `R.mipmap.ic_launcher`.
6. Official logo inserted into splash, welcome, personalization, and Profile top bar.
7. Profile header subtitle changed to `EXCUSES don’t burn CALORIES`.
8. New `Diet` bottom-navigation destination after Workout.
9. Nutrition Profile and Daily Habits cards moved from Profile to Diet by reusing the existing card implementations.

Modified/untracked source files for this feature set:

```text
M  app/src/main/java/com/fitmate/data/AppStorage.kt
M  app/src/main/java/com/fitmate/data/CampusFitRepositoryImpl.kt
M  app/src/main/java/com/fitmate/domain/repository/CampusFitRepository.kt
M  app/src/main/java/com/fitmate/ui/CampusFitApp.kt
M  app/src/main/java/com/fitmate/ui/auth/SignInScreen.kt
M  app/src/main/java/com/fitmate/ui/auth/SignUpScreen.kt
M  app/src/main/java/com/fitmate/ui/diet/DietScreen.kt
M  app/src/main/java/com/fitmate/ui/navigation/AppNavigation.kt
M  app/src/main/java/com/fitmate/ui/navigation/NavGraph.kt
M  app/src/main/java/com/fitmate/ui/onboarding/PersonalizingScreen.kt
M  app/src/main/java/com/fitmate/ui/onboarding/WelcomeScreen.kt
M  app/src/main/java/com/fitmate/ui/profile/ProfileScreen.kt
M  app/src/main/java/com/fitmate/ui/splash/SplashScreen.kt
M  app/src/main/java/com/fitmate/ui/viewmodel/AuthViewModel.kt
M  app/src/main/java/com/fitmate/ui/viewmodel/CampusFitViewModel.kt
?? app/src/main/java/com/fitmate/ui/auth/GoogleSignInHandler.kt
?? app/src/main/java/com/fitmate/ui/components/FitMateLogoMark.kt
```

At the last scoped diff audit, these 15 modified source files contained approximately 191 insertions and 240 deletions, plus the two new files above.

### Other pre-existing deletions

The status also contains tracked deletions under:

- `.vendor/**`: a checked-in local PyMuPDF installation/tooling directory.
- `Workout/workout_Instructions/**`
- `Workout/workout_gifs/**`

The root `Workout` content was deliberately being removed because the same runtime workout GIFs and instructions already exist under `app/src/main/assets/workout_details`. Do not restore the root `Workout` directory unless the user explicitly reverses that decision. Do not touch the `.vendor` deletions without first determining why that tooling directory was tracked.

## 2. User Direction and Guardrails

Long-running project decisions that should be preserved:

- Product name is **FitMate**.
- Package name is `com.fitmate` everywhere, including Firebase registration.
- The official current logo is the black-background, white `FM`/bodybuilder/FITMATE mark already represented by the launcher mipmaps. Use the shared `FitMateLogoMark` component rather than introducing another logo source.
- Preserve the established visual language on existing screens. The repository currently contains both neon-dark screens and newer light green/white Profile/More styling; do not globally redesign either without an explicit request.
- Workout is the main product focus. Do not disturb workout behavior while editing unrelated areas.
- Custom workout plans are user-owned and must never be overwritten automatically.
- Recommended workout plans are deterministic, local, offline-capable templates based on profile goal and related profile fields.
- Do not restore the obsolete root `Workout` asset folder; use packaged Android assets.
- Do not expose API secrets, signing credentials, private keys, or local configuration in Git.
- Avoid broad rewrites. Inspect and reuse existing components and flows.
- The user often requests analysis-only first. If they explicitly say not to change files, honor that.
- The worktree may contain user changes. Never revert changes that were not made by the current agent.

## 3. Technology and Build Configuration

### Core stack

- Kotlin `2.0.21`
- Jetpack Compose with Compose BOM `2024.09.00`
- Material 3 and extended Material icons
- Android Gradle Plugin `8.5.2`
- Gradle wrapper `8.10.2`
- Java/JVM target `17`
- `compileSdk 34`, `targetSdk 34`, `minSdk 24`
- Coroutines and StateFlow
- Navigation Compose
- Firebase Authentication and Cloud Firestore via Firebase BOM `34.7.0`
- Google Play Services Auth `21.2.0`
- Retrofit `2.11.0`, Gson converter, OkHttp logging dependency
- Coil Compose `2.7.0` and Coil GIF `2.6.0`
- Core library desugaring enabled with `desugar_jdk_libs 2.0.4`

### Gradle files

- Root project: `settings.gradle.kts`, `build.gradle.kts`
- App module: `app/build.gradle.kts`
- Wrapper: `gradle/wrapper/gradle-wrapper.jar` and `gradle-wrapper.properties`
- There is no version catalog (`gradle/libs.versions.toml`). Dependencies are declared directly in `app/build.gradle.kts`.
- `google-services` plugin is declared at root with version `4.4.4` and applied in the app module.
- `BuildConfig.GEMINI_API_KEY` is populated from a Gradle property named `GEMINI_API_KEY`; absent property yields an empty string.

### Standard verification commands

Run from `C:\fitness app`:

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
git diff --check -- app/src/main
git status --short --branch
```

Do not run destructive Git commands to “fix” the dirty tree.

## 4. Application Startup and Navigation

### Entry point

`MainActivity.kt`:

1. Initializes `AppStorage` with application context.
2. Initializes Firebase.
3. Obtains `FirebaseAuth`.
4. Enables edge-to-edge rendering.
5. Renders `NavGraph()`.

### Root routes

Defined in `ui/navigation/Routes.kt`:

- `splash`
- `signup`
- `signin`
- `welcome`
- `questions`
- `personalizing`
- `home`
- `otp_verification`
- `forgot_password`
- `coach_chat`

Not every onboarding route is independently mounted in `NavGraph`; onboarding stages are primarily internal state inside `CampusFitApp`.

### Startup routing

`NavGraph` starts at Splash. When splash finishes:

- If `FirebaseAuth.currentUser` is null, navigate to Sign Up.
- If a Firebase user exists, call `AppStorage.prepareForUser(uid)` and navigate to Home.
- Splash is removed from the back stack.

`CampusFitApp` then waits for repository `sessionReady` before deciding whether to show onboarding or the home shell. This matters because Firestore may contain setup data not yet loaded locally.

Internal onboarding stages:

- `LOADING`
- `WELCOME`
- `QUESTIONS`
- `PERSONALIZING`
- `HOME`

When repository bootstrap is ready:

- `setupCompleted == true` -> Home shell.
- `setupCompleted == false` -> Welcome/onboarding.

### Main bottom navigation

Current `HomeTab` order in `AppNavigation.kt`:

1. Profile
2. Workout
3. Diet
4. Progress
5. AI Coach
6. More

The default selected tab is Profile. AI Coach first shows `CoachIntroScreen`; its Get Started action reveals `CoachChatScreen`. Progress can also open the Coach tab. There is also a separate root `coach_chat` route, but normal in-app access currently uses the Home tab.

## 5. Authentication

### Supported providers

- Email/password sign-up and sign-in
- Google sign-in
- Firebase phone authentication with OTP
- Password-reset email

Main files:

- `ui/viewmodel/AuthViewModel.kt`
- `ui/auth/SignUpScreen.kt`
- `ui/auth/SignInScreen.kt`
- `ui/auth/GoogleSignInHandler.kt` (new/uncommitted)
- `ui/auth/OtpVerificationScreen.kt`
- `ui/auth/ForgotPasswordScreen.kt`

### Shared auth state

`AuthViewModel` owns loading, generic auth, phone-auth, and password-reset state flows. It also stores the current phone verification ID, resend token, and pending phone number.

After successful email sign-up, email sign-in, Google credential exchange, or phone credential exchange, `prepareAuthenticatedUser()` calls `AppStorage.prepareForUser(currentUid)` before publishing success.

### Google sign-in

`rememberGoogleSignInAction()` centralizes the Google launcher for both Sign In and Sign Up:

1. Build Google sign-in options requesting the default web client ID from generated resources.
2. Launch the Google account chooser.
3. Extract ID token.
4. Exchange it for a Firebase credential.
5. Call `AuthViewModel.signInWithCredential`.

The uncommitted fix changes successful Google sign-up navigation from Sign In to Home.

Google sign-in across developers still depends on Firebase/Google Cloud configuration:

- Correct Android app registration for `com.fitmate`.
- Each developer/device signing certificate SHA-1 and preferably SHA-256 registered.
- No conflicting OAuth client using the same package/SHA pair in another Google project.
- Fresh `google-services.json` downloaded after certificate/client changes.
- Google provider enabled in Firebase Authentication.
- Emulator/device includes working Google Play Services.

Earlier screenshots showed Firebase warning that one SHA-1/package pair was already used by another OAuth 2.0 client. That is a cloud-console ownership conflict, not something Kotlin code alone can repair. Resolve/remove the duplicate OAuth client in the other Google Cloud/Firebase project, then download the updated config.

### Phone OTP

The intended implemented flow is:

1. Sign-in screen validates and submits phone number.
2. Firebase sends OTP through `PhoneAuthProvider` callbacks.
3. Navigate to `OtpVerificationScreen`.
4. Six OTP boxes support focused digit entry/backspace behavior.
5. Resend is gated by a countdown and uses Firebase resend token.
6. Successful credential verification signs the user into Firebase and enters Home.

Phone auth testing may require Firebase test phone numbers, a physical device, or an emulator with Play Services. Quotas and abuse protection apply.

### Password reset

Forgot Password calls Firebase `sendPasswordResetEmail()` and exposes success/error state through `AuthViewModel`.

### Logout/account operations

More screen contains logout, edit profile, account/security, data/privacy, feedback, and legal/about behavior. Logout signs out of Firebase and restarts/navigates through the app’s startup path; it must not be confused with account deletion.

Email updates use `verifyBeforeUpdateEmail`. Password changes require reauthentication with current password. Google/phone-managed accounts are told that email/password editing is unavailable in that dialog.

Data deletion and account deletion require care because Firebase may demand recent authentication. Firestore user state is stored separately and must be cleared consistently with the auth account.

## 6. Onboarding and Profile Data

### Eight onboarding steps

`QuestionsScreen.kt` currently collects:

1. Gender
2. Age slider (`14..80`)
3. Height slider (`120..220 cm`)
4. Weight slider (`40..150 kg`)
5. Main goal
6. Activity level
7. Food preference
8. Daily workout minutes slider (`10..120`)

Numeric slider questions include minus and plus controls for exact one-step changes. The final button reads `Generate my fitness plan`.

Goal options are driven by `GoalType.entries`:

- Fat loss
- Muscle gain
- Lean body
- Reduce stress & relax
- Cardio / Stamina
- Increasing flexibility and mobility

Current onboarding-created defaults not directly collected in the eight screens:

- `budgetInr = 0`
- `experienceLevel = BEGINNER`
- `equipment = emptySet()`

These profile fields still exist and can influence downstream generation, so adding onboarding UI for them later would improve personalization.

### Personalization behavior

Despite the “Personalizing” UI, `CampusFitViewModel.bootstrapPersonalization` currently generates a local deterministic starter plan. It does not call Gemini during onboarding.

It:

1. Saves the profile and an `AiConfig` seed.
2. Calculates goal metrics locally.
3. Creates local goal reasoning text.
4. Creates local diet recommendation and workout summary.
5. Saves `PersonalizedPlan`.
6. Marks setup completed.

Onboarding should be account-specific after the uncommitted cache-ownership changes: a returning user retains setup state, while switching Firebase UID clears user-scoped local cache before remote bootstrap.

## 7. Domain Models and State

Primary models live in `domain/model/Models.kt`.

### User and configuration

- `UserProfile`: age, height, weight, gender, activity, goal, food preference, budget, workout duration, experience, equipment.
- `AiConfig`: remote/local provider mode and endpoints/model names. This is legacy/general configuration; the live Gemini Coach uses `BuildConfig.GEMINI_API_KEY` directly.
- `AppThemeMode`: Light/Dark.

### Fitness plan

- `GoalMetrics`
- `GoalProgress`
- `GoalReasoning`
- `DietRecommendation`
- `WorkoutPlan`
- `PersonalizedPlan`

### Workout scheduling and tracking

- `WorkoutWeekday`
- `WorkoutFocus`
- `WorkoutExerciseConfig`
- `WorkoutDaySchedule`
- `WorkoutPlanType` (`DEFAULT`, `CUSTOM`)
- `WeeklyWorkoutSchedule`
- `WorkoutExerciseProgress`
- `WorkoutDayLog`
- `WorkoutDayStatus`

`WeeklyWorkoutSchedule` metadata:

- `isCustom`
- `planType`
- `generatedForGoal`
- `version`

Defaults are backward-compatible: old schedules without metadata are interpreted from `isCustom` and optional values rather than crashing.

### Meals and discipline

- `DisciplineState`
- `MealAnalysis`
- `MealLog`
- `DayProgressSummary`
- `ProfileSnapshot`
- `MealsSnapshot`

### Exercise library

`ExerciseLibraryEntry` includes name, muscle group, summary instructions, image path, detail GIF asset, instruction Markdown asset, metric type, default/min/max amount, and workload thresholds. Metric types are reps or seconds.

## 8. Data Layer and Persistence

### Repository

`CampusFitRepository` exposes StateFlows for profile, AI config, theme, setup completion, personalized plan, discipline, daily goal progress, meal logs, workout schedule, workout logs, and `sessionReady`.

`CampusFitRepositoryImpl` is the source of truth for the running app. It:

- Initializes state from `AppStorage`.
- Normalizes daily progress.
- Bootstraps Firestore state asynchronously.
- Marks `sessionReady = true` in a `finally` block even if backend load fails.
- Persists mutations locally.
- Best-effort syncs snapshots to Firestore.
- Records completed workout sets and elapsed exercise-session time.
- Calculates daily workout status and discipline streak.
- Prunes workout logs older than six months.

### Local storage

`AppStorage` uses SharedPreferences and JSON serialization for:

- setup completion
- profile
- AI config
- theme
- personalized plan
- discipline
- daily goal progress
- meal logs
- workout schedule
- workout logs

The new `prepareForUser(userId)` behavior:

- Same UID: no change.
- No prior owner UID: claim the current cache for the authenticated UID, preserving legacy data.
- Different UID: remove user-scoped keys, then set the new owner UID.

This is a migration compromise: the first authenticated user after upgrade inherits existing local cache; subsequent account switches get isolated local state. Remote Firebase state is not deleted by this method.

### Firestore

`FirebaseBackendService` stores one merged document per UID at:

```text
fitmateUsers/{firebaseUid}
```

Most complex values are stored as JSON strings inside the document. Workout logs are limited to the newest 180 when syncing; meal logs are limited to 60.

Feedback is written by More screen to a top-level `feedback` collection with UID, email, message, and server timestamp.

Firestore rules are not in this repository. They must allow authenticated users to access only their own `fitmateUsers/{uid}` document and should constrain feedback writes.

## 9. Workout System

### Core files

- `ui/workout/WorkoutScreen.kt`
- `domain/usecase/GenerateWorkoutScheduleUseCase.kt`
- `domain/workout/WorkoutExerciseCatalog.kt`
- `data/LocalExerciseDatabase.kt`
- `domain/model/Models.kt`
- `data/AppStorage.kt`
- `data/CampusFitRepositoryImpl.kt`

### Recommended plans

`GenerateWorkoutScheduleUseCase(profile)` is deterministic and offline. It generates seven days, marks `planType = DEFAULT`, records `generatedForGoal`, and uses format version 2.

Current base weekly patterns:

- Muscle gain: Push / Pull / Legs / Push / Pull / Legs / Rest (PPL split).
- Fat loss: Full Body / Conditioning / Rest / Full Body / Conditioning / Core + Conditioning / Rest.
- Lean body: Push / Pull / Rest / Legs / Conditioning / Full Body / Rest.
- Cardio/Stamina: Conditioning / Full Body / Rest / Conditioning / Core + Conditioning / Conditioning / Rest.
- Flexibility/Mobility: Mobility / Rest / Mobility / Full Body / Rest / Mobility / Mobility.
- Stress relief: Full Body / Mobility / Rest / Core + Conditioning / Mobility / Rest / Full Body.

Beginner or low-activity users receive an additional recovery day for higher-volume goals. Workout duration controls exercise count per active day: under 30 minutes -> 2, under 50 -> 3, otherwise 4. Experience changes sets/amount. A bodyweight-only equipment selection filters obviously equipment-dependent movements.

### Custom plans

The existing sequential custom-plan builder walks through weekdays, supports repeated focuses, and requires rest/recovery handling. Day editor lets users include/exclude exercises and adjust sets and reps/duration. Saved custom schedules use `planType = CUSTOM`, `isCustom = true`, and `generatedForGoal = null`.

Custom plans must not regenerate when profile goal changes.

### Goal-change handling

Workout UI compares a recommended schedule’s `generatedForGoal` with the current profile goal. It offers confirmation before replacing an outdated recommended plan. For a custom plan, the user must explicitly choose to replace it with a recommended plan.

### Exercise detail flow

Workout day cards are collapsible. Exercise cards retain their imagery, workload/difficulty pill, and an `Open` action. Opening an exercise shows:

- exercise name
- GIF
- sets and reps/seconds
- completed-set progress bar
- Start Workout / live `HH:MM:SS` timer / red Stop Workout
- post-stop questionnaire
- collapsed-by-default instructions accordion
- return action that preserves the caller’s scroll position in the workout list

Submitting `Completed my current set` increments persisted completed sets. Giving up records the elapsed session without incrementing progress. Fully completed exercises show a completion badge. Workout logs feed Progress and streak calculations.

### Exercise data and assets

The local database currently contains 29 named entries (the earlier request said 30, but Deadlift with Chains was intentionally removed):

`Push-Ups`, `Cable Chest Press`, `Bench Press`, `Chest Press`, `Butterfly`, `Incline Inner Biceps Curls`, `Barbell Rear Delt Row`, `Elevated Cable Rows`, `Deadlift with Bands`, `Dynamic Back Stretch`, `Hack Squat`, `Barbell Lunge`, `Elevated Back Lunge`, `Cable Hip Adduction`, `Shoulder Raise`, `Body Tricep Press`, `Tricep Extension`, `Bench Dips`, `Cable Crunch`, `Decline Reverse Crunch`, `Bent Knee Hip Raise`, `Battling Ropes`, `Bottoms Up`, `Mountain Climber`, `Jumping Jack`, `Plank`, `Sit-Up`, `Decline Crunch`, and `Romanian Deadlift`.

Not every local exercise necessarily has both a dedicated GIF and Markdown file. The UI falls back to summary data/assets when optional fields are blank.

## 10. Main Screens

### Profile

`ui/profile/ProfileScreen.kt` is the former dashboard rebuilt as a personal fitness identity page. It includes greeting/profile presentation, physical stats, goals/commitment information, achievement/streak elements, and motivational content.

The current uncommitted work removes Nutrition Profile and Daily Habits from the Profile render tree but keeps their reusable composables in the file as public functions for Diet.

Profile-only app-bar treatment now includes the official logo and subtitle `EXCUSES don’t burn CALORIES`.

### Diet

`ui/diet/DietScreen.kt` is newly wired into bottom navigation after Workout. It currently presents the moved `NutritionProfileCard(state)` and `DailyHabitsCard()` using Profile’s existing visual components.

This is not a complete meal scanner. The old `MealsScreen.kt` remains in the tree but is not a bottom tab. `CampusFitViewModel.analyzeMeal` currently validates input and reads profile/config/metrics but does not perform or save an analysis. Treat meal analysis as incomplete/orphaned unless deliberately revived.

### Progress

`ui/progress/ProgressScreen.kt` currently contains a richer analytics experience, not only the early simple calendar design. It consumes workout schedule, workout logs, and `AnalyticsSnapshot` to render:

- streak/consistency hero
- AI Coach entry
- fitness score
- completion ring
- weekly activity
- monthly summary
- personal records/metric cards
- workout heatmap
- navigable calendar/status views
- empty state when history does not exist

Calendar statuses are derived from the saved schedule and logs. Be aware that `AnalyticsEngine` streak logic counts any logged day, whereas repository/calendar logic distinguishes completed/partial/missed/rest days. These definitions may need consolidation later.

### More

`ui/more/MoreScreen.kt` contains:

- Edit Profile
- Account & Security
- Data & Privacy
- Feedback
- Legal & About
- Log out at the bottom

Edit Profile updates `UserProfile` through `CampusFitViewModel`. Account changes use Firebase Auth. Feedback writes to Firestore. Data clearing/account deletion use confirmation and app restart behavior.

### Habits and legacy Meals

`HabitsScreen.kt` and `MealsScreen.kt` still exist but are not current main navigation destinations. Daily Habits displayed in Diet is a Profile card implementation, not `HabitsScreen`.

## 11. AI Coach

### Access

AI Coach is a dedicated bottom tab. The user sees `CoachIntroScreen`, then taps Get Started to enter `CoachChatScreen`. Progress also contains a shortcut to the Coach tab.

### Live network path

```text
CoachChatScreen
  -> CoachViewModel
  -> GeminiRepository
  -> GeminiClient / GeminiApi
  -> Google Generative Language API
```

Endpoint:

```text
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=...
```

Timeouts are 60 seconds. The repository sends a simple system-style prompt identifying the assistant as FitMate AI Coach and appends the user question.

### API key configuration

The app reads `BuildConfig.GEMINI_API_KEY`, generated from the local Gradle property `GEMINI_API_KEY`. Example local-only setup:

```properties
GEMINI_API_KEY=your_key_here
```

`gradle.properties` is ignored and untracked. Never commit a real Gemini key. An empty key will cause API failure.

This client-side key architecture is not safe for a public production app because APK contents can be inspected. A production design should proxy Gemini requests through a protected backend/Cloud Function with authentication, quotas, and abuse controls.

### Current limitations

- The Coach prompt currently receives only the user’s text, not `UserProfile`, workout plan, or progress history.
- Daily briefing and metrics cards contain hard-coded demonstration values (for example streak/score/consistency) rather than repository state.
- Chat messages are Compose-local state and are not persisted.
- HTTP errors are returned as visible strings.
- The general `AiConfig` model and onboarding seed do not drive the live Gemini client.

Do not describe the Coach as fully personalized until these gaps are addressed.

## 12. Branding, Theme, and Assets

### Official logo

The user clarified that the desired splash logo is the black background with the white bodybuilder silhouette, geometric `FM`, `FITMATE`, and small tagline. The existing launcher mipmaps visually match it.

New shared component:

```text
app/src/main/java/com/fitmate/ui/components/FitMateLogoMark.kt
```

It renders `R.mipmap.ic_launcher`. Current uncommitted integrations:

- Splash orbital logo
- Welcome/Get Started card
- Personalizing screen
- Profile app bar

`app/src/main/res/drawable/app_logo.png` still exists but current code should not use it for the official app mark. The large body-transformation imagery on auth screens is decorative, not an app-logo replacement target.

### Theme reality

`FitMateTheme` supports light/dark mode through `AppThemeMode`, but many screens define local palettes instead of relying solely on Material theme colors. Current UI includes:

- neon/dark Workout and Coach styling
- newer light green/white Profile and More styling
- screen-specific custom palettes

Avoid assuming a single global theme implementation. Visually inspect affected screens on emulator before broad color changes.

### Packaged assets

- `app/src/main/assets/exercises/`: workout card images and decorative workout GIFs.
- `app/src/main/assets/workout_details/gifs/`: exercise-detail demonstrations.
- `app/src/main/assets/workout_details/instructions/`: Markdown instructions.
- `app/src/main/assets/profile/`: avatars, food/diet art, trophy, and body transformation imagery.
- `app/src/main/res/drawable/`: auth/onboarding/coach raster resources.
- `app/src/main/res/mipmap-*`: official launcher/logo assets.

Workout detail code loads packaged paths using `file:///android_asset/...`; the deleted root `Workout` folder is not required at runtime.

## 13. Firebase and Environment Setup

### Current Android Firebase registration

From the tracked `app/google-services.json` at audit time:

- Project ID: `fitmate-f4bad`
- Package: `com.fitmate`
- App ID: `1:898626057879:android:3e36894fc879e60e035bd2`

Firebase providers previously confirmed enabled: Email/Password, Phone, Google.

### Important credential distinction

`google-services.json` is Firebase client configuration, not a private server credential. It is currently tracked. Private service-account JSON, keystores, signing passwords, Gemini keys, and local Gradle/local.properties files must remain untracked.

### `.gitignore` coverage

The current ignore file covers build output, IDE files, keystores, signing properties, `local.properties`, `gradle.properties`, `.env*`, and common key/certificate extensions. It does not ignore `app/google-services.json`, which matches the current repository behavior.

### Manifest details

- Internet permission enabled.
- App label `FitMate`.
- Launcher icon and round icon use mipmaps.
- `android:usesCleartextTraffic="true"` remains enabled, likely for local HTTP model endpoints/legacy API configuration.
- `android:allowBackup="true"` remains enabled.
- Application uses `Theme.DeviceDefault.Light.NoActionBar`; Compose renders the actual UI.

Review cleartext traffic and backup policy before production release.

## 14. Known Risks, Incomplete Work, and Technical Debt

These are current-state observations, not instructions to rewrite everything:

1. **Uncommitted feature set still needs runtime verification.** The exact audited tree compiles and all 5 existing unit tests pass, but auth, Firestore bootstrap, logo placement, and navigation/card movement still need emulator/device testing.
2. **Google OAuth certificate conflict is external.** A duplicate package/SHA OAuth client warning was visible in Firebase; Kotlin changes cannot resolve ownership in another Cloud project.
3. **Gemini key is client-side.** Fine for development, unsuitable for public production distribution.
4. **Coach personalization is mostly visual.** It does not receive actual user profile/workout/progress context and several dashboard metrics are hard-coded.
5. **Meal analysis is incomplete.** `analyzeMeal` does not call a service or persist a result; legacy Meals UI is not in bottom navigation.
6. **Analytics definitions diverge.** `AnalyticsEngine` treats any log as a workout for streaks, while repository/calendar completion logic checks sets and scheduled rest/missed days.
7. **Profile inputs are partial.** Onboarding leaves equipment empty, budget zero, and experience beginner by default, although the recommendation engine can use them.
8. **Theme consistency is partial.** Screen-local palettes can ignore the global light/dark toggle.
9. **Data sync uses JSON strings in Firestore.** Easy to reuse locally, but hard to query and evolve server-side.
10. **Firestore failures are mostly swallowed.** Repository sync uses `runCatching` without user-visible retry/error state.
11. **Auth account deletion may require reauthentication.** Ensure UI handles Firebase recent-login errors.
12. **README is stale.** It still describes four main sections and older `Instructions` labeling; live app now has six bottom tabs and newer functionality.
13. **README formatting may be malformed.** The displayed project structure code fence appears not to close in the current file.
14. **Source comments show encoding artifacts.** Some files contain mojibake box-drawing comment text; this does not affect runtime but reduces readability.
15. **Local exercise count is 29, not 30.** Deadlift with Chains was intentionally removed; do not silently re-add it.
16. **Obsolete files remain.** `HabitsScreen.kt`, `MealsScreen.kt`, general `AiConfig`, and separate `Routes.CoachChat` may be legacy or future-facing. Confirm usage before deletion.
17. **Cleartext traffic is globally enabled.** Narrow it with Network Security Config if local HTTP support remains required.
18. **No Firestore rules are versioned here.** Backend security cannot be fully audited from this repository alone.

## 15. Recent Git History Worth Knowing

Recent commits, newest first:

```text
74ba007 Fix Gemini API integration and improve AI coach
a2faecb Add Workout recommendation engine depending on the body goal ; improved app UX
5735d89 Update auth screens, splash screen, and app logo
0102d29 FitMate onboarding, coach, workout and UI updates
e8d542a Restored AI coach
0c6b358 Restored profile UI overhaul
3d766b4 Merge branch 'main' of https://github.com/astha-innov/FitMate
4124cd6 ...
20e3fea Stop tracking gradle.properties
73757f8 Merge main
a65cce6 changed gradle properties
9400e31 Add more (settings) screen
e39ac19 Rebuilt Dashboard Page, name changed to Profile
028db20 Add Gemini AI Coach integration
700e832 add OTP verification and forgot password
fb91ec1 Added streak calender and workout timer
0232b95 Revise README page
a22776b Added instruction page for workouts
b9d7991 Restored gifs and started addition of workout instructions
865d27a UI refining
```

Important restoration history:

- Commit `20e3fea` and merge commits `73757f8` / source `9400e31` were previously inspected because AI Coach and More/Profile work had disappeared during branch integration.
- The restored AI Coach/network files are currently present under `ui/coach`.
- Avoid replaying old merge commits wholesale onto this tree; compare paths and cherry-pick only if evidence shows something is missing.

## 16. New Session Startup Checklist

The next session should do these in order:

1. Read this entire file.
2. Run `git status --short --branch` and compare it with **Critical Snapshot**.
3. Do not restore `.vendor` or root `Workout` deletions automatically.
4. Inspect the two untracked files and the 15 modified app files before editing.
5. Run `git diff --check -- app/src/main`.
6. Run `./gradlew.bat :app:compileDebugKotlin`.
7. Run `./gradlew.bat :app:testDebugUnitTest`.
8. If compilation fails, fix only the current feature set; do not broadly rewrite navigation/auth/UI.
9. Manually test on an emulator with Google Play Services:
   - signed-out launch -> Sign Up
   - email sign-up -> Home/onboarding
   - Google sign-up -> Home/onboarding, not Sign In
   - returning authenticated user -> Home without auth prompt
   - new Firebase UID -> no previous user onboarding/profile leakage
   - existing UID -> persisted setup restored after Firestore bootstrap
   - official logo on splash/welcome/personalization/Profile header
   - bottom tabs in correct order with Diet after Workout
   - Nutrition Profile and Daily Habits appear in Diet and not Profile
10. Test phone OTP and password reset separately; Firebase quotas/config may affect them.
11. Inspect `git diff --stat` and confirm only intended files are part of the commit.
12. Ask the user before committing or pushing unless they explicitly requested publication.

## 17. Recommended Immediate Continuation

The interrupted implementation should be finished before starting unrelated features:

1. Clean obvious unused imports created by extracting shared Google/logo components, without reformatting unrelated code.
2. Search for stale app-logo calls:

   ```powershell
   rg -n "app_logo|DumbbellIcon" app/src/main/java/com/fitmate
   ```

   Expected: no old app-logo code usage that should represent the official mark.
3. Compile and run unit tests.
4. Audit auth routing and per-UID storage behavior.
5. Verify Diet/Profile card placement and six-tab navigation in emulator.
6. Report any Firebase-console-only blocker separately from code status.
7. Once the user approves, commit the coherent source changes and intentional root `Workout` deletions carefully. Keep `.vendor` handling separate unless its deletion is explicitly intended.

## 18. Definition of a Safe Handoff

A new session is safely oriented only when it understands all of the following:

- Current commit and dirty-worktree boundaries.
- Which deletions are intentional and which are uncertain.
- Firebase auth -> per-user storage -> backend bootstrap -> onboarding/home routing.
- Difference between local onboarding personalization and live Gemini Coach.
- Recommended versus custom workout ownership rules.
- Workout log data feeding exercise completion, Progress, and streaks.
- Official logo source and current screen integrations.
- Current six-tab navigation and moved Diet cards.
- Known incomplete meal/Coach personalization behavior.
- Exact build/test/manual verification steps before commit.

If any of those points are unclear, inspect the referenced source files before changing code rather than relying on assumptions.

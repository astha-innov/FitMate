package com.fitmate.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import android.util.Log


/**
 * Single source of truth for app language.
 *
 * Responsibilities:
 *  - Reading the *currently applied* AppCompat locale (not a cached copy).
 *  - Persisting the user's choice (for display purposes / first-run default).
 *  - Triggering the locale change via the official AppCompat API.
 *
 * IMPORTANT: Changing the locale here does nothing else. No restartApp(),
 * no Intent juggling, no finishAffinity(). AppCompatDelegate automatically
 * recreates the current resumed Activity for you. Fighting that with a
 * manual restart is what caused the original bugs.
 */
object LanguageRepository {

    /** ISO 639-1 codes this app ships translations for. "en" = device/app default. */
    val supportedLanguageCodes = listOf("en", "hi", "bn", "ta", "te", "mr", "kn")

    /**
     * The locale code actually active right now, derived from AppCompatDelegate
     * itself rather than from SharedPreferences. This can never drift from
     * reality, including if the user changed the language from system
     * Settings > App Info > Language instead of in-app.
     */
    fun currentLanguageCode(): String {
        val applied = AppCompatDelegate.getApplicationLocales()
        if (applied.isEmpty) return "en"
        val tag = applied[0]?.language ?: return "en"
        return supportedLanguageCodes.firstOrNull { it == tag } ?: "en"
    }

    /**
     * Changes the app's language. This is the ONLY place that should call
     * AppCompatDelegate.setApplicationLocales() in the whole app.
     *
     * AppCompat will:
     *  - Persist the choice itself (survives process death / cold start).
     *  - Recreate the current resumed Activity automatically so every
     *    composable currently on screen recomposes with the new resources.
     *
     * We also mirror the choice into AppStorage purely so other parts of
     * the app (e.g. a settings list that wants to show "Hindi" as selected
     * before AppCompatDelegate finishes its async work on API 33+) have an
     * immediately-readable value. AppStorage is NEVER read to decide which
     * locale to *apply* -- only AppCompatDelegate's own state is used for that.
     */
    fun setLanguage(languageCode: String) {
        val resolved = if (languageCode in supportedLanguageCodes) languageCode else "en"
        AppStorage.saveLanguage(resolved)

        val localeList = if (resolved == "en") {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(resolved)
        }
        AppCompatDelegate.setApplicationLocales(localeList)

        Log.d(
            "LANGUAGE",
            "LocaleList object = $localeList"
        )

        Log.d(
            "LANGUAGE",
            "Applied = ${AppCompatDelegate.getApplicationLocales()}"
        )

    }
}
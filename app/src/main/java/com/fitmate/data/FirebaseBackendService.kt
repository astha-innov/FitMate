package com.fitmate.data

import com.fitmate.domain.model.AppThemeMode
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class FirebaseBackendService {

    fun isConfigured(): Boolean {
        return runCatching {
            FirebaseApp.getInstance()
        }.isSuccess
    }

    suspend fun loadState(): BackendState? {

        if (!isConfigured()) return null

        val userId = currentUserId() ?: return null

        val snapshot = firestore()
            .collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .await()

        if (!snapshot.exists()) return null

        val data = snapshot.data ?: return null

        return BackendState(

            profile =
                decodeJson(data["profile"])
                    ?.let(AppStorage::profileFromJson),

            aiConfig =
                decodeJson(data["aiConfig"])
                    ?.let(AppStorage::aiConfigFromJson),

            themeMode =
                (data["themeMode"] as? String)
                    ?.let {
                        runCatching {
                            AppThemeMode.valueOf(it)
                        }.getOrDefault(AppThemeMode.LIGHT)
                    },

            setupCompleted =
                data["setupCompleted"] as? Boolean,

            personalizedPlan =
                decodeJson(data["personalizedPlan"])
                    ?.let(AppStorage::planFromJson),

            discipline =
                decodeJson(data["discipline"])
                    ?.let(AppStorage::disciplineFromJson),

            todayProgress =
                decodeJson(data["todayProgress"])
                    ?.let(AppStorage::goalProgressFromJson),

            mealLogs =
                decodeJsonArray(data["mealLogs"])
                    ?.let { array ->

                        List(array.length()) { index ->

                            AppStorage.mealLogFromJson(
                                array.getJSONObject(index)
                            )
                        }
                    },

            workoutSchedule =
                decodeJson(data["workoutSchedule"])
                    ?.let(AppStorage::workoutScheduleFromJson),

            workoutLogs =
                decodeJsonArray(data["workoutLogs"])
                    ?.let { array ->
                        List(array.length()) { index ->
                            AppStorage.workoutDayLogFromJson(
                                array.getJSONObject(index)
                            )
                        }
                    },
        )
    }

    suspend fun saveState(state: BackendState) {

        if (!isConfigured()) return

        val userId = currentUserId() ?: return

        val payload = hashMapOf<String, Any>(

            "themeMode" to
                    (state.themeMode?.name
                        ?: AppThemeMode.LIGHT.name),

            "setupCompleted" to
                    (state.setupCompleted ?: false),

            "updatedAt" to
                    FieldValue.serverTimestamp(),
        )

        state.profile?.let {
            payload["profile"] =
                AppStorage.profileToJson(it).toString()
        }

        state.aiConfig?.let {
            payload["aiConfig"] =
                AppStorage.aiConfigToJson(it).toString()
        }

        state.personalizedPlan?.let {
            payload["personalizedPlan"] =
                AppStorage.planToJson(it).toString()
        }

        state.discipline?.let {
            payload["discipline"] =
                AppStorage.disciplineToJson(it).toString()
        }

        state.todayProgress?.let {
            payload["todayProgress"] =
                AppStorage.goalProgressToJson(it).toString()
        }

        state.mealLogs?.let {

            payload["mealLogs"] =
                JSONArray(
                    it.map(AppStorage::mealLogToJson)
                ).toString()
        }

        state.workoutSchedule?.let {
            payload["workoutSchedule"] =
                AppStorage.workoutScheduleToJson(it).toString()
        }

        state.workoutLogs?.let {
            payload["workoutLogs"] =
                JSONArray(
                    it.map(AppStorage::workoutDayLogToJson)
                ).toString()
        }

        firestore()
            .collection(USERS_COLLECTION)
            .document(userId)
            .set(payload, SetOptions.merge())
            .await()
    }

    fun isUserLoggedIn(): Boolean {
        return auth().currentUser != null
    }

    fun currentUserId(): String? {
        return auth().currentUser?.uid
    }

    fun signOut() {
        auth().signOut()
    }

    suspend fun clearUserDocument() {
        val userId = currentUserId() ?: return
        firestore()
            .collection(USERS_COLLECTION)
            .document(userId)
            .delete()
            .await()
    }

    private fun auth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    private fun firestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    private fun decodeJson(raw: Any?): JSONObject? {

        return when (raw) {

            is String ->
                runCatching {
                    JSONObject(raw)
                }.getOrNull()

            is Map<*, *> ->
                runCatching {
                    JSONObject(raw)
                }.getOrNull()

            else -> null
        }
    }

    private fun decodeJsonArray(raw: Any?): JSONArray? {

        return when (raw) {

            is String ->
                runCatching {
                    JSONArray(raw)
                }.getOrNull()

            is List<*> ->
                runCatching {
                    JSONArray(raw)
                }.getOrNull()

            else -> null
        }
    }

    private companion object {

        const val USERS_COLLECTION =
            "fitmateUsers"
    }
}

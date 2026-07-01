package com.fitmate.data

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.net.URI

data class ExerciseRemoteMedia(
    val thumbnailUrl: String?,
    val detailMediaUrl: String?,
)

internal fun exerciseRemoteMediaFromFields(
    fields: Map<String, Any?>,
): ExerciseRemoteMedia? {
    fun cloudinaryUrl(vararg keys: String): String? = keys
        .asSequence()
        .mapNotNull { key -> fields[key] as? String }
        .map(String::trim)
        .firstOrNull(::isCloudinaryHttpsUrl)

    val thumbnailUrl = cloudinaryUrl("thumbnailUrl", "imageUrl")
    val detailMediaUrl = cloudinaryUrl("detailMediaUrl", "gifUrl")
    if (thumbnailUrl == null && detailMediaUrl == null) return null

    return ExerciseRemoteMedia(
        thumbnailUrl = thumbnailUrl,
        detailMediaUrl = detailMediaUrl,
    )
}

private fun isCloudinaryHttpsUrl(value: String): Boolean = runCatching {
    val uri = URI(value)
    uri.scheme.equals("https", ignoreCase = true) &&
        uri.host.equals("res.cloudinary.com", ignoreCase = true)
}.getOrDefault(false)

object FirestoreExerciseMediaRepository {
    private val refreshMutex = Mutex()
    private val _mediaByExerciseId = MutableStateFlow<Map<String, ExerciseRemoteMedia>>(emptyMap())

    val mediaByExerciseId: StateFlow<Map<String, ExerciseRemoteMedia>> =
        _mediaByExerciseId.asStateFlow()

    suspend fun refresh() {
        refreshMutex.withLock {
            if (runCatching { FirebaseApp.getInstance() }.isFailure) return

            runCatching {
                FirebaseFirestore.getInstance()
                    .collection("exercises")
                    .get()
                    .await()
                    .documents
                    .mapNotNull { document ->
                        val canonicalExercise = LocalExerciseCatalog.findById(document.id)
                            ?: return@mapNotNull null
                        val media = exerciseRemoteMediaFromFields(document.data.orEmpty())
                            ?: return@mapNotNull null
                        canonicalExercise.id to media
                    }
                    .toMap()
            }.onSuccess { remoteMedia ->
                _mediaByExerciseId.value = remoteMedia
            }
        }
    }
}

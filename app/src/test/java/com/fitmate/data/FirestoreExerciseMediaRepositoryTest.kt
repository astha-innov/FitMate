package com.fitmate.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirestoreExerciseMediaRepositoryTest {
    @Test
    fun `parses manifest field names`() {
        val media = exerciseRemoteMediaFromFields(
            mapOf(
                "thumbnailUrl" to "https://res.cloudinary.com/demo/image/upload/workouts/images/barbell_lunge.jpg",
                "detailMediaUrl" to "https://res.cloudinary.com/demo/image/upload/workouts/gifs/barbell_lunge.gif",
            )
        )

        assertEquals(
            "https://res.cloudinary.com/demo/image/upload/workouts/images/barbell_lunge.jpg",
            media?.thumbnailUrl,
        )
        assertEquals(
            "https://res.cloudinary.com/demo/image/upload/workouts/gifs/barbell_lunge.gif",
            media?.detailMediaUrl,
        )
    }

    @Test
    fun `accepts initial gifUrl and imageUrl field names`() {
        val media = exerciseRemoteMediaFromFields(
            mapOf(
                "imageUrl" to "https://res.cloudinary.com/demo/image/upload/workouts/images/battling_ropes.jpg",
                "gifUrl" to "https://res.cloudinary.com/demo/image/upload/workouts/gifs/battling_ropes.gif",
            )
        )

        assertEquals(
            "https://res.cloudinary.com/demo/image/upload/workouts/images/battling_ropes.jpg",
            media?.thumbnailUrl,
        )
        assertEquals(
            "https://res.cloudinary.com/demo/image/upload/workouts/gifs/battling_ropes.gif",
            media?.detailMediaUrl,
        )
    }

    @Test
    fun `rejects insecure or non Cloudinary media`() {
        assertNull(
            exerciseRemoteMediaFromFields(
                mapOf(
                    "thumbnailUrl" to "http://res.cloudinary.com/demo/image/upload/a.jpg",
                    "detailMediaUrl" to "https://example.com/a.gif",
                )
            )
        )
    }
}

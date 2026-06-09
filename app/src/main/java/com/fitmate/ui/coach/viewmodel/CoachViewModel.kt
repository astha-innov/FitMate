package com.fitmate.ui.coach.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.fitmate.ui.coach.repository.GeminiRepository

class CoachViewModel : ViewModel() {

    private val repository = GeminiRepository()

    var response by mutableStateOf("")
        private set

    var loading by mutableStateOf(false)
        private set

    fun askCoach(
        apiKey: String,
        prompt: String
    ) {

        viewModelScope.launch {

            loading = true

            try {

                response =
                    repository.askCoach(
                        apiKey,
                        prompt
                    )

            } catch (e: Exception) {

                e.printStackTrace()

                response = e.toString()

            } finally {

                loading = false
            }
        }
    }
}

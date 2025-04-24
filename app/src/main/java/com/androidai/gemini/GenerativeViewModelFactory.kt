package com.androidai.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.androidai.gemini.chat.ChatViewModel
import com.androidai.gemini.multimodal.PhotoReasoningViewModel
import com.androidai.gemini.text.SummarizeViewModel
import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI

val GenerativeViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        viewModelClass: Class<T>,
        extras: CreationExtras
    ): T {
        val config = com.google.firebase.vertexai.type.generationConfig {
            temperature = 0.7f
        }

        return with(viewModelClass) {
            when {
                isAssignableFrom(SummarizeViewModel::class.java) -> {
                    // Initialize a GenerativeModel with the `gemini-flash` AI model
                    // for text generation
                    val generativeModel = Firebase.vertexAI.generativeModel(
                        modelName = "gemini-2.0-flash",
                        generationConfig = config
                    )
                    SummarizeViewModel(generativeModel)
                }

                isAssignableFrom(PhotoReasoningViewModel::class.java) -> {
                    // Initialize a GenerativeModel with the `gemini-flash` AI model
                    // for multimodal text generation
                    val generativeModel = Firebase.vertexAI.generativeModel(
                        modelName = "gemini-2.0-flash",
                        generationConfig = config
                    )
                    PhotoReasoningViewModel(generativeModel)
                }

                isAssignableFrom(ChatViewModel::class.java) -> {
                    // Initialize a GenerativeModel with the `gemini-flash` AI model for chat
                    val generativeModel = Firebase.vertexAI.generativeModel(
                        modelName = "gemini-2.0-flash",
                        generationConfig = config
                    )
                    ChatViewModel(generativeModel)
                }

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${viewModelClass.name}")
            }
        } as T
    }
}
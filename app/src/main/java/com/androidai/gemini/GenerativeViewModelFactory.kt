package com.androidai.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.androidai.gemini.api.GeminiAIService
import com.androidai.gemini.chat.ChatViewModel
import com.androidai.gemini.multimodal.PhotoReasoningViewModel
import com.androidai.gemini.text.SummarizeViewModel


/**
 * Factory for creating ViewModel instances that use the Gemini API.
 *
 * This factory has been updated to use the Gemini REST API directly via GeminiAIService
 * instead of the Google AI SDK's GenerativeModel.
 */
val GenerativeViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        viewModelClass: Class<T>,
        extras: CreationExtras
    ): T {
        // Configure the temperature for all models
        val temperature = 0.7f

        // Create the Gemini API service with the API key from BuildConfig
        val geminiService = GeminiAIService(
            model = "gemini-2.0-flash",
            apiKey = BuildConfig.GEM_API_KEY,
            temperature = temperature,
        )

        return with(viewModelClass) {
            when {
                isAssignableFrom(SummarizeViewModel::class.java) -> {
                    // Initialize SummarizeViewModel with GeminiAIService
                    SummarizeViewModel(geminiService)
                }

                isAssignableFrom(PhotoReasoningViewModel::class.java) -> {
                    // Initialize PhotoReasoningViewModel with GeminiAIService
                    // Note: For multimodal support, we'll need to extend GeminiAIService
                    // to handle image uploads
                    PhotoReasoningViewModel(geminiService)
                }

                isAssignableFrom(ChatViewModel::class.java) -> {
                    // Initialize ChatViewModel with GeminiAIService
                    ChatViewModel(geminiService)
                }

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${viewModelClass.name}")
            }
        } as T
    }
}
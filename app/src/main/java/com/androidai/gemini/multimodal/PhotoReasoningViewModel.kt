package com.androidai.gemini.multimodal

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidai.gemini.api.GeminiAIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for photo reasoning functionality using the Gemini AI service.
 *
 * This class has been updated to use the direct Gemini REST API via GeminiAIService
 * instead of the Google AI SDK's GenerativeModel.
 *
 * @param geminiService The Gemini AI service used to generate responses for image-based queries.
 */
class PhotoReasoningViewModel(
    private val geminiService: GeminiAIService
) : ViewModel() {

    private val _uiState: MutableStateFlow<PhotoReasoningUiState> =
        MutableStateFlow(PhotoReasoningUiState.Initial)
    val uiState: StateFlow<PhotoReasoningUiState> =
        _uiState.asStateFlow()

    /**
     * Analyzes images and answers a user's question about them.
     *
     * @param userInput The user's question about the images.
     * @param selectedImages The images to analyze.
     */
    fun reason(
        userInput: String,
        selectedImages: List<Bitmap>
    ) {
        _uiState.value = PhotoReasoningUiState.Loading
        val prompt = "Look at the image(s), and then answer the following question: $userInput"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Use the multimodal API to process the images and the prompt
                val response = geminiService.getMultimodalResponse(prompt, selectedImages)
                
                // Update the UI state with the response
                _uiState.value = PhotoReasoningUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = PhotoReasoningUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
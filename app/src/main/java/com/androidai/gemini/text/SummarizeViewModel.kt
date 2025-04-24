package com.androidai.gemini.text

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidai.gemini.api.GeminiAIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for text summarization functionality using the Gemini AI service.
 *
 * This class has been updated to use the direct Gemini REST API via GeminiAIService
 * instead of the Google AI SDK's GenerativeModel.
 *
 * @param geminiService The Gemini AI service used to generate summaries.
 */
class SummarizeViewModel(
    private val geminiService: GeminiAIService
) : ViewModel() {

    private val _uiState: MutableStateFlow<SummarizeUiState> =
        MutableStateFlow(SummarizeUiState.Initial)
    val uiState: StateFlow<SummarizeUiState> =
        _uiState.asStateFlow()

    /**
     * Summarizes the given text using the Gemini AI service.
     *
     * @param inputText The text to summarize.
     */
    fun summarize(inputText: String) {
        _uiState.value = SummarizeUiState.Loading

        val prompt = "Summarize the following text for me: $inputText"

        viewModelScope.launch {
            try {
                val response = geminiService.getResponse(prompt)
                _uiState.value = SummarizeUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = SummarizeUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * NOTE: Streaming is not directly supported with the basic REST API implementation.
     * This method provides a non-streaming fallback.
     *
     * @param inputText The text to summarize.
     */
    fun summarizeStreaming(inputText: String) {
        _uiState.value = SummarizeUiState.Loading

        val prompt = "Summarize the following text for me: $inputText"

        viewModelScope.launch {
            try {
                // In this implementation, we use the standard non-streaming API
                // since streaming requires more complex implementation with the REST API
                val response = geminiService.getResponse(prompt)
                _uiState.value = SummarizeUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = SummarizeUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidai.gemini.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidai.gemini.api.Content
import com.androidai.gemini.api.GeminiAIService
import com.androidai.gemini.api.Part
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for chat functionality using the Gemini AI service.
 *
 * This class has been updated to use the direct Gemini REST API via GeminiAIService
 * instead of the Google AI SDK's GenerativeModel.
 *
 * @param geminiService The Gemini AI service used to generate responses.
 */
class ChatViewModel(
    private val geminiService: GeminiAIService
) : ViewModel() {
    // Chat history that will be sent to the API with each request
    private val chatHistory = mutableListOf(
        Content(
            role = "user",
            parts = listOf(Part(text = "Hello, I have 2 dogs in my house."))
        ),
        Content(
            role = "model",
            parts = listOf(Part(text = "Great to meet you. What would you like to know?"))
        )
    )

    // UI state exposed to the UI
    private val _uiState: MutableStateFlow<ChatUiState> = MutableStateFlow(
        ChatUiState(
            messages = chatHistory.map { content ->
                ChatMessage(
                    text = content.parts.first().text,
                    participant = if (content.role == "user") Participant.USER else Participant.MODEL,
                    isPending = false
                )
            }
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /**
     * Sends a message to the Gemini AI and updates the UI with the response.
     *
     * @param userMessage The message from the user to send to the Gemini AI.
     */
    fun sendMessage(userMessage: String) {
        // Add a pending message to the UI
        _uiState.value.addMessage(
            ChatMessage(
                text = userMessage,
                participant = Participant.USER,
                isPending = true
            )
        )

        // Add the user message to the chat history
        chatHistory.add(
            Content(
                role = "user",
                parts = listOf(Part(text = userMessage))
            )
        )

        viewModelScope.launch {
            try {
                // Mark the user message as no longer pending
                _uiState.value.replaceLastPendingMessage()

                // Get response from Gemini AI
                val modelResponse = geminiService.chatCompletion(
                    prompt = userMessage
                )

                // Add the model's response to the UI
                _uiState.value.addMessage(
                    ChatMessage(
                        text = modelResponse,
                        participant = Participant.MODEL,
                        isPending = false
                    )
                )

                // Add the model's response to chat history for context in future requests
                chatHistory.add(
                    Content(
                        role = "model",
                        parts = listOf(Part(text = modelResponse))
                    )
                )
            } catch (e: Exception) {
                // Replace the pending message and show the error
                _uiState.value.replaceLastPendingMessage()
                _uiState.value.addMessage(
                    ChatMessage(
                        text = e.localizedMessage ?: "An error occurred",
                        participant = Participant.ERROR
                    )
                )
            }
        }
    }
}
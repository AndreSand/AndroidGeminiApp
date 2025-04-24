package com.androidai.gemini.api

import android.graphics.Bitmap
import android.util.Log
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Service for interacting with the Gemini AI REST API.
 * https://ai.google.dev/api/generate-content#text_gen_text_only_prompt-KOTLIN
 *
 * This class handles communication with the Gemini API for generating responses
 * from user prompts using REST calls instead of the Google AI SDK.
 *
 * @param apiKey The API key for authentication with the Gemini API.
 * @param temperature The sampling temperature for the model (controls randomness).
 * @param model The name of the Gemini model to use.
 */
class GeminiAIService(
    private val apiKey: String,
    private val temperature: Float = 0.7f,
    private val model: String = "gemini-1.5-flash"
) {
    /**
     * The HTTP client used for making network requests.
     *
     * This client is configured with:
     * - ContentNegotiation: Handles JSON serialization and deserialization.
     *   - Ignores unknown keys in JSON responses.
     *   - Pretty prints JSON for debugging.
     *   - Uses lenient parsing.
     * - Logging: Logs request and response bodies for debugging purposes.
     *   - Logs at the BODY level, capturing all details.
     *   - Uses a custom logger that prints to the console and Android's Logcat.
     * - ResponseObserver: Logs the HTTP status code of each response.
     */
    private val client = PlatformHttpClient().createHttpClient()
    
    /**
     * Base URL for the Gemini API.
     */
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    /**
     * Sends a prompt to the Gemini AI model and retrieves the generated response.
     *
     * This function sends a POST request to the Gemini API with the provided prompt.
     * It then attempts to parse the JSON response from the API into a `GeminiResponse` object.
     * If successful, it extracts the text from the first candidate's first part and returns it.
     * If the response cannot be parsed or if no text is available, it returns an error message.
     *
     * @param prompt The text prompt to send to the Gemini AI model.
     * @return The generated text response from the Gemini AI model, or an error message if the request failed or the response was malformed.
     * @throws Exception if there is an error during the network request or response parsing.
     */
    suspend fun getResponse(prompt: String): String {
        val url = "$baseUrl/$model:generateContent?key=$apiKey"
        
        // Create request with temperature parameter
        val requestBody = GeminiAIRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt)
                    )
                )
            )
        )
        
        Log.d("GeminiAIService", "Sending request to Gemini API")
        
        return try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            
            Log.d("GeminiAIService", "Received response, parsing...")
            
            val geminiResponse = response.body<GeminiResponse>()
            val responseText = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (responseText != null) {
                Log.d("GeminiAIService", "Successfully parsed response")
                responseText
            } else {
                Log.w("GeminiAIService", "No text in response")
                "No response generated"
            }
        } catch (e: Exception) {
            Log.e("GeminiAIService", "Error in API request: ${e.message}", e)
            "Error: ${e.message}"
        }
    }
    
    /**
     * Creates a chat completion with the Gemini API.
     * 
     * @param messages List of previous message contents to provide context for the chat
     * @param prompt The new prompt to send
     * @return The model's response text
     */
    suspend fun chatCompletion(prompt: String): String {
        val url = "$baseUrl/$model:generateContent?key=$apiKey"
        
        // Create combined history plus new prompt
        val requestBody = GeminiAIRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt)
                    )
                )
            )
        )
        
        return try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            
            val geminiResponse = response.body<GeminiResponse>()
            geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response generated"
        } catch (e: Exception) {
            Log.e("GeminiAIService", "Error in chat completion: ${e.message}", e)
            "Error: ${e.message}"
        }
    }
    
    /**
     * Sends a multimodal prompt (text + images) to the Gemini AI model.
     *
     * @param prompt The text prompt to send alongside the images.
     * @param images The list of images to include with the prompt.
     * @return The generated text response from the Gemini AI model.
     */
    suspend fun getMultimodalResponse(prompt: String, images: List<Bitmap>): String {
        val url = "$baseUrl/$model:generateContent?key=$apiKey"
        
        // Create multimodal parts: first the images, then the text
        val parts = mutableListOf<MultimodalPart>()
        
        // Add all images first
        for (image in images) {
            parts.add(
                MultimodalPart(
                    inlineData = image.toInlineData()
                )
            )
        }
        
        // Add the text prompt
        parts.add(MultimodalPart(text = prompt))
        
        // Create the request body with the multimodal content
        val requestBody = MultimodalRequest(
            contents = listOf(
                MultimodalContent(
                    parts = parts
                )
            )
        )
        
        Log.d("GeminiAIService", "Sending multimodal request to Gemini API")
        
        return try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            
            Log.d("GeminiAIService", "Received multimodal response, parsing...")
            
            val geminiResponse = response.body<GeminiResponse>()
            val responseText = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (responseText != null) {
                Log.d("GeminiAIService", "Successfully parsed multimodal response")
                responseText
            } else {
                Log.w("GeminiAIService", "No text in multimodal response")
                "No response generated"
            }
        } catch (e: Exception) {
            Log.e("GeminiAIService", "Error in multimodal API request: ${e.message}", e)
            "Error: ${e.message}"
        }
    }
}
package com.androidai.gemini.api

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Platform-specific HTTP client for Android.
 *
 * This class provides a configured Ktor HttpClient for making network requests
 * with appropriate logging and serialization settings for the Android platform.
 */
class PlatformHttpClient {
    
    /**
     * Creates and configures an HttpClient with Android-specific settings.
     *
     * @return A configured HttpClient ready for use.
     */
    fun createHttpClient(): HttpClient {
        return HttpClient(Android) {
            // Configure content negotiation for JSON
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            
            // Set up logging for debugging
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("GeminiAPI", message)
                    }
                }
                level = LogLevel.BODY
            }
            
            // Log response status codes
            install(ResponseObserver) {
                onResponse { response ->
                    Log.d("GeminiAPI", "Response status: ${response.status.value}")
                }
            }
            
            // Configure request timeouts
            install(HttpTimeout) {
                requestTimeoutMillis = 30000  // 30 seconds
                connectTimeoutMillis = 15000  // 15 seconds
                socketTimeoutMillis = 30000   // 30 seconds
            }
            
            engine {
                connectTimeout = 15000  // 15 seconds
                socketTimeout = 30000   // 30 seconds
            }
        }
    }
}
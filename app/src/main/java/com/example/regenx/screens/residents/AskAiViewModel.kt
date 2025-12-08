package com.example.regenx.screens.residents

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.example.regenx.BuildConfig
import kotlinx.coroutines.launch
data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class AskAiViewModel : ViewModel() {

    // 💬 ReXi's personality + domain rules
    private val systemPrompt = """
    Hey buddy! You're chatting with ReXi, the ReGenX waste assistant.

    Your job:
    - Always give practical, at-home DIY waste recycling and reuse tips.
    - For ANY "how to recycle at home" question, give ONLY step-by-step home methods.
    - Avoid generic recycling-bin instructions unless the user specifically asks for municipal recycling.

    VERY IMPORTANT:
    - DO NOT use *, -, #
    - NO formatting symbols. NO stars. NO markdown.
    - Write everything in clean, simple sentences.
    - If steps are needed, write them like:
        Step 1: Do this
        Step 2: Do that
        Step 3: Continue...
    - Keep the tone friendly, chill, and clear.

    Allowed topics:
    - Waste recycling at home
    - Waste segregation
    - Reuse / upcycling ideas
    - Composting basics
    - Garbage pickup complaints
    - Illegal dumping and reporting

    Not allowed:
    - Anything unrelated to waste, recycling, eco-living, or garbage complaints.

    Tone:
    - Friendly, short sentences, simple language. Use "buddy" or "bro" lightly.
""".trimIndent()



    var messages by mutableStateOf(listOf<ChatMessage>())
        private set

    var isLoading by mutableStateOf(false)
        private set

    // 🔑 Gemini model – adjust if you want another one
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",   // or "gemini-flash-latest"
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    // 🟦 Simple greeting detector
    private val greetingWords = setOf(
        "hi", "hello", "hey", "yo", "hii", "heyy", "sup", "hola"
    )

    private fun isGreeting(text: String): Boolean {
        val cleaned = text.lowercase().trim()
        return cleaned in greetingWords
    }

    // 👋 Welcome message for when the screen first opens
    fun addWelcomeMessage() {
        if (messages.isNotEmpty()) return   // avoid duplicate if already added
        messages = messages + ChatMessage(
            text = "Hey! 👋 I'm ReXi, your ReGenX waste assistant. Ask me anything about waste segregation, recycling tips, or any garbage collection issues — I got you! 😊",
            isUser = false
        )
    }

    fun sendPrompt(prompt: String) {
        val question = prompt.trim()
        if (question.isEmpty()) return          // 🚫 no blank calls
        if (isLoading) return                   // 🚫 avoid spamming API

        // Show user's message
        messages = messages + ChatMessage(question, isUser = true)

        // 🟢 If it's just a greeting, keep reply simple & local (no API call)
        if (isGreeting(question)) {
            messages = messages + ChatMessage(
                text = "Hey! 👋 I’m ReXi. Need help with waste tips at home or any complaint about garbage pickup?",
                isUser = false
            )
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                // Stateless: instruction + only this one question
                val fullPrompt = """
                    $systemPrompt

                    User question:
                    $question
                """.trimIndent()

                val response = model.generateContent(fullPrompt)
                val reply = response.text
                    ?: "Hmm, something went off buddy. Try asking that again?"

                messages = messages + ChatMessage(reply, isUser = false)
            } catch (e: Exception) {
                messages = messages + ChatMessage(
                    text = "Uh oh, something broke on my side bro 😅 Try again in a bit.",
                    isUser = false
                )
            } finally {
                isLoading = false
            }
        }
    }

    fun clearChat() {
        messages = emptyList()
    }
}

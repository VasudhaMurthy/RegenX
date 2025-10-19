package com.example.regenx.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse

object GeminiHelper {
    private const val API_KEY = "YOUR_API_KEY_HERE"

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY
    )

    suspend fun analyzeComplaint(complaintText: String): String {
        val prompt = """
            You are an AI assistant for a municipal waste management system.
            Read the complaint below and decide what action should be taken.
            Give output in a simple structured JSON:
            {
              "category": "...",
              "priority": "...",
              "recommended_action": "..."
            }

            Complaint: "$complaintText"
        """.trimIndent()

        val response: GenerateContentResponse = model.generateContent(prompt)
        return response.text ?: "No response"
    }
}

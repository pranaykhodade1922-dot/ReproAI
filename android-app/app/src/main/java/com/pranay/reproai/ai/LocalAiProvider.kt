package com.pranay.reproai.ai

import android.util.Log

class LocalAiProvider(
    private val fallbackProvider: RuleBasedFallbackProvider = RuleBasedFallbackProvider()
) : AiProvider {

    override suspend fun analyze(input: AnalysisInput): AnalysisResult {
        return try {
            val prompt = buildPrompt(input)

            val result = fallbackProvider.analyze(input)
            result.copy(analysisSource = "RULE_BASED")
        } catch (e: Exception) {
            Log.e("ReproAI", "Local analysis failed: ${e.javaClass.simpleName}")
            fallbackProvider.analyze(input)
        }
    }

    private fun buildPrompt(input: AnalysisInput): String {
        val eventsStr = input.events.joinToString("\n") { event ->
            "- [${event.id}] [${event.type}] [${event.metadata["source"] ?: "SYSTEM"}] ${event.title}: ${event.description} (meta: ${event.metadata})"
        }

        return """
            You are a mobile debugging analysis engine.
            User Bug Description: "${input.userDescription}"
            Device: ${input.deviceInfo.manufacturer} ${input.deviceInfo.model} (Android ${input.deviceInfo.androidVersion})
            
            Captured Event Context Window:
            $eventsStr
            
            Task:
            1. Identify probable trigger and probable cause.
            2. Extract observed evidence from event IDs.
            3. Generate reproduction steps and structured TestScenario using ONLY supported actions:
               OPEN_SCREEN, TAP, WAIT, CHANGE_NETWORK, BACKGROUND_APP, FOREGROUND_APP, ROTATE_DEVICE, ASSERT_VISIBLE, ASSERT_TEXT, ASSERT_API_STATUS, ASSERT_EVENT, CUSTOM.
            Return valid JSON matching AnalysisResult format.
        """.trimIndent()
    }
}

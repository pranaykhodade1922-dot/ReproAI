package com.pranay.reproai.ai

interface IncidentAnalysisRule : AiProvider {fun detect(input: AnalysisInput): Boolean}

class RuleBasedFallbackProvider : AiProvider {
    private val payment=PaymentNetworkFailureRule()
    private val analysisRules: List<IncidentAnalysisRule> = listOf(CheckoutRotationStateLossRule(),payment)
    override suspend fun analyze(input: AnalysisInput): AnalysisResult =
        analysisRules.firstOrNull {it.detect(input)}?.analyze(input) ?: payment.analyze(input)
}

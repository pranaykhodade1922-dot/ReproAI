package com.pranay.reproai.ai

interface AiProvider {
    suspend fun analyze(input: AnalysisInput): AnalysisResult
}

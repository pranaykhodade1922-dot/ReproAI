package com.pranay.reproai.data.remote

import com.pranay.reproai.ai.TestScenario
import com.pranay.reproai.data.remote.dto.*
import retrofit2.http.*

interface ReproRunnerApi {
    @GET("health") suspend fun health(): RunnerHealth
    @GET("devices") suspend fun devices(): List<RunnerDevice>
    @POST("preflight") suspend fun preflight(@Body scenario: TestScenario): RunnerReadiness
    @POST("preflight") suspend fun preflight(): RunnerReadiness
    @POST("demo/reset") suspend fun resetDemo(): com.google.gson.JsonObject
    @POST("execute") suspend fun execute(@Body scenario: TestScenario): ExecutionResult
    @GET("executions/{id}") suspend fun execution(@Path("id") id: String): ExecutionResult
    @POST("executions/{id}/cancel") suspend fun cancel(@Path("id") id: String): ExecutionResult
}

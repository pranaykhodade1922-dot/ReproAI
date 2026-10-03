package com.pranay.reproai.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pranay.reproai.data.remote.dto.RunnerConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RunnerApiClient {
    val gson = GsonBuilder().serializeNulls().create()
    val http = OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS).build()
    fun api(config: RunnerConfig): ReproRunnerApi = Retrofit.Builder()
        .baseUrl(config.baseUrl()).client(http).addConverterFactory(GsonConverterFactory.create(gson))
        .build().create(ReproRunnerApi::class.java)
}

package com.pranay.reproai.sdk

enum class ReproEventType {
    SCREEN_VIEW,
    USER_ACTION,
    API_REQUEST,
    API_RESPONSE,
    ERROR,
    STATE_CHANGE,
    CUSTOM
}

data class ReproSdkEvent(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: ReproEventType,
    val name: String,
    val description: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

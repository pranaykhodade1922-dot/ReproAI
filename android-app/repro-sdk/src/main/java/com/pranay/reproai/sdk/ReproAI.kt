package com.pranay.reproai.sdk

import android.content.Context
import java.util.UUID

object ReproAI {

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun trackScreen(screenName: String) {
        trackEvent(
            type = ReproEventType.SCREEN_VIEW,
            name = "Screen: $screenName",
            description = "Opened screen $screenName",
            metadata = mapOf("screen" to screenName)
        )
    }

    fun trackAction(name: String, description: String? = null, metadata: Map<String, String> = emptyMap()) {
        trackEvent(
            type = ReproEventType.USER_ACTION,
            name = name,
            description = description ?: "User action $name",
            metadata = metadata
        )
    }

    fun trackApiRequest(endpoint: String, method: String = "POST", metadata: Map<String, String> = emptyMap()) {
        val mergedMeta = metadata.toMutableMap().apply {
            put("endpoint", endpoint)
            put("method", method)
        }
        trackEvent(
            type = ReproEventType.API_REQUEST,
            name = "API Request $method $endpoint",
            description = "Initiated $method request to $endpoint",
            metadata = mergedMeta
        )
    }

    fun trackApiResponse(endpoint: String, statusCode: Int, metadata: Map<String, String> = emptyMap()) {
        val mergedMeta = metadata.toMutableMap().apply {
            put("endpoint", endpoint)
            put("statusCode", statusCode.toString())
        }
        trackEvent(
            type = ReproEventType.API_RESPONSE,
            name = "API Response $statusCode",
            description = "Received HTTP $statusCode from $endpoint",
            metadata = mergedMeta
        )
    }

    fun trackError(code: String, message: String, metadata: Map<String, String> = emptyMap()) {
        val mergedMeta = metadata.toMutableMap().apply {
            put("errorCode", code)
        }
        trackEvent(
            type = ReproEventType.ERROR,
            name = code,
            description = message,
            metadata = mergedMeta
        )
    }

    fun trackEvent(
        type: ReproEventType,
        name: String,
        description: String? = null,
        metadata: Map<String, String> = emptyMap()
    ) {
        val context = appContext ?: return
        val event = ReproSdkEvent(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            type = type,
            name = name,
            description = description,
            metadata = metadata
        )
        EventBroadcaster.broadcastEvent(context, event)
    }
}

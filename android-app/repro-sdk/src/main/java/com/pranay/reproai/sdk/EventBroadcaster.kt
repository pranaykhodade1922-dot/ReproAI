package com.pranay.reproai.sdk

import android.content.Context
import android.content.Intent
import android.util.Log
import org.json.JSONObject

internal object EventBroadcaster {

    private const val TARGET_PACKAGE = "com.pranay.reproai"
    private const val ACTION_TRACK_EVENT = "com.pranay.reproai.TRACK_EVENT"
    private const val TAG = "ReproSDK"

    fun broadcastEvent(context: Context, event: ReproSdkEvent) {
        try {
            val json = JSONObject().apply {
                put("id", event.id)
                put("timestamp", event.timestamp)
                put("type", event.type.name)
                put("name", event.name)
                put("description", event.description ?: "")

                val metaObj = JSONObject()
                event.metadata.forEach { (k, v) -> metaObj.put(k, v) }
                put("metadata", metaObj)
                put("source", "DEMOSHOP")
                put("sourcePackage", context.packageName)
            }

            val intent = Intent(ACTION_TRACK_EVENT).apply {
                setPackage(TARGET_PACKAGE)
                putExtra("event_json", json.toString())
                putExtra("event_type", event.type.name)
                putExtra("event_name", event.name)
                putExtra("source_package", context.packageName)
            }

            context.sendBroadcast(intent)
            Log.d(TAG, "Broadcasted SDK Event: ${event.name} (${event.type})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to broadcast SDK event: ${e.message}", e)
        }
    }
}

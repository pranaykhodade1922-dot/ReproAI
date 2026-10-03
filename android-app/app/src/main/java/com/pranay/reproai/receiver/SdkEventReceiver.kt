package com.pranay.reproai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pranay.reproai.data.model.DebugEventType
import com.pranay.reproai.tracking.DebugSessionManager
import org.json.JSONObject

class SdkEventReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRACK_EVENT = "com.pranay.reproai.TRACK_EVENT"
        private const val TAG = "SdkEventReceiver"

        @Volatile
        var activeSessionManager: DebugSessionManager? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TRACK_EVENT) return

        val jsonStr = intent.getStringExtra("event_json") ?: return
        val sourcePkg = intent.getStringExtra("source_package") ?: "unknown"

        try {
            val json = JSONObject(jsonStr)
            val name = json.optString("name", "SDK Event")
            val typeStr = json.optString("type", "CUSTOM")
            val description = json.optString("description", "")

            val metadataMap = mutableMapOf<String, String>()
            val metaObj = json.optJSONObject("metadata")
            if (metaObj != null) {
                val keys = metaObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    metadataMap[k] = metaObj.optString(k)
                }
            }
            metadataMap["source"] = "DEMOSHOP"
            metadataMap["sourcePackage"] = sourcePkg

            val eventType = mapType(typeStr)

            val manager = activeSessionManager
            if (manager != null && manager.hasActiveSession()) {
                manager.addEvent(
                    type = eventType,
                    title = name,
                    description = description,
                    metadata = metadataMap,
                    timestamp = json.optLong("timestamp", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Invalid SDK event: ${e.javaClass.simpleName}")
        }
    }

    private fun mapType(typeStr: String): DebugEventType {
        return when (typeStr) {
            "SCREEN_VIEW" -> DebugEventType.SCREEN_OPENED
            "USER_ACTION" -> DebugEventType.USER_ACTION
            "API_REQUEST" -> DebugEventType.API_REQUEST
            "API_RESPONSE" -> DebugEventType.API_RESPONSE
            "ERROR" -> DebugEventType.ERROR
            "STATE_CHANGE" -> DebugEventType.NETWORK_CHANGED
            else -> DebugEventType.CUSTOM
        }
    }
}

package com.pranay.reproai.tracking

import com.pranay.reproai.ai.AnalysisResult
import com.pranay.reproai.data.model.DebugSession
import org.json.JSONArray
import org.json.JSONObject

object SessionExporter {

    fun exportToJson(session: DebugSession, analysisResult: AnalysisResult? = session.analysisResult): String {
        val root = JSONObject()

        val sessionObj = JSONObject().apply {
            put("id", session.id)
            put("startedAt", session.startedAt)
            put("endedAt", session.endedAt ?: JSONObject.NULL)
            put("isActive", session.isActive)
            put("issueTitle", session.issueTitle ?: "")
            put("userDescription", EventSanitizer.sanitizeText(session.userDescription ?: ""))
            put("bugCapturedAt", session.bugCapturedAt ?: JSONObject.NULL)
        }
        root.put("session", sessionObj)

        val deviceObj = JSONObject().apply {
            put("manufacturer", session.deviceInfo.manufacturer)
            put("model", session.deviceInfo.model)
            put("androidVersion", session.deviceInfo.androidVersion)
            put("sdkVersion", session.deviceInfo.sdkVersion)
            put("appVersion", session.deviceInfo.appVersion)
        }
        root.put("device", deviceObj)

        val eventsArray = JSONArray()
        session.events.forEach { event ->
            val eventObj = JSONObject().apply {
                put("id", event.id)
                put("timestamp", event.timestamp)
                put("elapsedMs", event.elapsedMs)
                put("type", event.type.name)
                put("source", event.metadata["source"] ?: if (event.type.name.contains("NETWORK") || event.type.name.contains("ORIENTATION") || event.type.name.contains("APP_")) "DEVICE" else "REPROAI")
                put("title", EventSanitizer.sanitizeText(event.title))
                put("description", EventSanitizer.sanitizeText(event.description))

                val metaObj = JSONObject()
                val sanitizedMap = EventSanitizer.sanitizeMetadata(event.metadata)
                sanitizedMap.forEach { (k, v) -> metaObj.put(k, v) }
                put("metadata", metaObj)
            }
            eventsArray.put(eventObj)
        }
        root.put("events", eventsArray)

        if (analysisResult != null) {
            val analysisObj = JSONObject().apply {
                put("issueTitle", EventSanitizer.sanitizeText(analysisResult.issueTitle))
                put("summary", EventSanitizer.sanitizeText(analysisResult.summary))
                put("probableTrigger", EventSanitizer.sanitizeText(analysisResult.probableTrigger))
                put("probableCause", EventSanitizer.sanitizeText(analysisResult.probableCause))
                put("confidence", analysisResult.confidence)
                put("analysisSource", analysisResult.analysisSource)

                val compArray = JSONArray()
                analysisResult.relevantComponents.forEach { compArray.put(it) }
                put("relevantComponents", compArray)

                val evidenceArray = JSONArray()
                analysisResult.observedEvidence.forEach { evidenceArray.put(EventSanitizer.sanitizeText(it)) }
                put("observedEvidence", evidenceArray)
            }
            root.put("analysis", analysisObj)

            val scenarioObj = JSONObject().apply {
                put("id", analysisResult.testScenario.id)
                put("name", analysisResult.testScenario.name)
                put("description", analysisResult.testScenario.description)

                val stepsArray = JSONArray()
                analysisResult.testScenario.steps.forEach { step ->
                    val stepObj = JSONObject().apply {
                        put("action", step.action.name)
                        put("target", step.target ?: JSONObject.NULL)
                        put("value", step.value ?: JSONObject.NULL)
                        put("description", step.description ?: JSONObject.NULL)
                    }
                    stepsArray.put(stepObj)
                }
                put("steps", stepsArray)

                val assertionsArray = JSONArray()
                analysisResult.testScenario.assertions.forEach { assertion ->
                    val assertionObj = JSONObject().apply {
                        put("type", assertion.type)
                        put("target", assertion.target ?: JSONObject.NULL)
                        put("expected", assertion.expected)
                    }
                    assertionsArray.put(assertionObj)
                }
                put("assertions", assertionsArray)
            }
            root.put("testScenario", scenarioObj)
        }

        return (sanitizeJson(root) as JSONObject).toString(2)
    }

    private fun sanitizeJson(value: Any?): Any? = when(value) {
        is JSONObject -> JSONObject().apply {value.keys().forEach {key ->
            put(EventSanitizer.sanitizeText(key),if(EventSanitizer.isSensitiveKey(key)) "[REDACTED_SECRET]" else sanitizeJson(value.get(key)))
        }}
        is JSONArray -> JSONArray().apply {for(i in 0 until value.length()) put(sanitizeJson(value.get(i)))}
        is String -> EventSanitizer.sanitizeText(value)
        else -> value
    }
}

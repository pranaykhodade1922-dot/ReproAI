package com.pranay.reproai.data.local

import androidx.room.TypeConverter
import com.pranay.reproai.data.model.DebugEventType
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromEventType(value: DebugEventType): String {
        return value.name
    }

    @TypeConverter
    fun toEventType(value: String): DebugEventType {
        return try {
            DebugEventType.valueOf(value)
        } catch (e: Exception) {
            DebugEventType.CUSTOM
        }
    }

    @TypeConverter
    fun fromMetadataMap(map: Map<String, String>): String {
        return JSONObject(map as Map<*, *>).toString()
    }

    @TypeConverter
    fun toMetadataMap(jsonStr: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val jsonObject = JSONObject(jsonStr)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.optString(key)
            }
        } catch (e: Exception) {
            // Ignore parse errors and return empty map
        }
        return map
    }
}

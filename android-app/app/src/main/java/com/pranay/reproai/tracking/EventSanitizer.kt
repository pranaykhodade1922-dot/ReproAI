package com.pranay.reproai.tracking

import com.pranay.reproai.ai.AnalysisInput

object EventSanitizer {

    private val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val phoneRegex = Regex("\\+?[0-9]{10,14}")
    private val formattedPhoneRegex = Regex("(?<![A-Za-z0-9])(?:\\+\\d{1,3}[ -]?)?(?:\\(\\d{3}\\)|\\d{3})[ -]\\d{3}[ -]\\d{4}(?![A-Za-z0-9])|(?<![A-Za-z0-9])(?:\\+\\d{1,3}[ -])?\\d{5}[ -]\\d{5}(?![A-Za-z0-9])")
    private val bearerTokenRegex = Regex("Bearer\\s+[^\\s,;\\\"}]+", RegexOption.IGNORE_CASE)
    private val authHeader = Regex("(?i)(?:proxy-)?authorization\\s*[:=]\\s*(?:Basic|Bearer)\\s+[^\\s,;]+")
    private val secretAssignment = Regex("(?i)[\\\"']?(authorization|(?:access[_-]?|refresh[_-]?|auth[_-]?)?token|api[_-]?key|password|passwd|secret|cookie)[\\\"']?\\s*[:=]\\s*(?:\\\"[^\\\"]*\\\"|'[^']*'|[^\\s,;}]+)")
    fun isSensitiveKey(key: String): Boolean {
        val normalized = key.lowercase().replace(Regex("[^a-z0-9]"), "")
        return normalized in setOf("authorization", "proxyauthorization", "token", "accesstoken", "refreshtoken",
            "authtoken", "apikey", "password", "passwd", "secret", "clientsecret", "cookie", "setcookie") ||
            normalized.endsWith("password") || normalized.endsWith("secret") || normalized.endsWith("token") || normalized.endsWith("apikey")
    }

    fun sanitizeText(input: String): String {
        var sanitized = input
        sanitized = emailRegex.replace(sanitized, "[REDACTED_EMAIL]")
        sanitized = authHeader.replace(sanitized, "Authorization=[REDACTED_SECRET]")
        sanitized = bearerTokenRegex.replace(sanitized, "Bearer [REDACTED_TOKEN]")
        sanitized = secretAssignment.replace(sanitized) { "${it.groupValues[1]}=[REDACTED_SECRET]" }
        sanitized = phoneRegex.replace(sanitized, "[REDACTED_PHONE]")
        sanitized = formattedPhoneRegex.replace(sanitized,"[REDACTED_PHONE]")
        return sanitized
    }

    fun sanitizeMetadata(metadata: Map<String, String>): Map<String, String> {
        return metadata.mapValues { (key, value) ->
            if (isSensitiveKey(key)) "[REDACTED_SECRET]" else sanitizeText(value)
        }
    }

    fun sanitizeAnalysisInput(input: AnalysisInput): AnalysisInput {
        val sanitizedUserDesc = sanitizeText(input.userDescription)
        val sanitizedEvents = input.events.map { event ->
            event.copy(
                title = sanitizeText(event.title),
                description = sanitizeText(event.description),
                metadata = sanitizeMetadata(event.metadata)
            )
        }
        return input.copy(
            userDescription = sanitizedUserDesc,
            events = sanitizedEvents
        )
    }
}

package com.pranay.reproai

import com.pranay.reproai.tracking.EventSanitizer
import org.junit.Assert.*
import org.junit.Test

class PrivacySanitizerTest {
    @Test fun personalAndCredentialDataAreRemovedWithoutRemovingFailureEvidence() {
        val raw = "person@example.com +1 (415) 555-2671 +91 98765 43210 Authorization: Bearer private-value password=hidden HTTP 401 TOKEN_EXPIRED"
        val safe = EventSanitizer.sanitizeText(raw)
        listOf("person@example.com", "555-2671", "98765", "private-value", "hidden").forEach { assertFalse(safe.contains(it)) }
        assertTrue(safe.contains("HTTP 401")); assertTrue(safe.contains("TOKEN_EXPIRED"))
        val metadata = EventSanitizer.sanitizeMetadata(mapOf("apiKey" to "key-value", "clientSecret" to "secret-value", "refresh_token" to "token-value", "password" to "password-value"))
        assertTrue(metadata.values.all { it == "[REDACTED_SECRET]" })
    }
}

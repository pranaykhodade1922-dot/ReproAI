package com.pranay.reproai

import com.google.gson.JsonParser
import com.pranay.reproai.ai.*
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.report.*
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class DeveloperPackageTest {
    private val scenario=TestScenario("scenario","Payment","",steps=listOf(TestActionItem(TestAction.TAP,"PAY")))
    private val analysis=AnalysisResult("Payment","","","",90,emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),"Success","Failure",scenario)
    private val report=IncidentReportBuilder.build(DebugSession("fixture",issueTitle="Payment"),analysis,"report",1)
    @Test fun standaloneScenarioHasRunnerContractWithoutWrapper() {
        val json=JsonParser.parseString(DeveloperPackageExporter.scenarioJson(report)).asJsonObject
        assertEquals(setOf("id","name","description","preconditions","steps","assertions","verification_assertions","execution_purpose"),json.keySet())
        assertEquals("REPRODUCE",json.get("execution_purpose").asString)
        assertEquals("TAP",json.getAsJsonArray("steps")[0].asJsonObject.get("action").asString)
        assertFalse(json.has("report"))
    }
    @Test fun zipContainsOnlyAvailableSanitizedArtifactsAndSafeNames() {
        val output=ByteArrayOutputStream()
        DeveloperPackageExporter.writeZip(report.copy(description="Bearer secret-value contact@example.com"),output)
        val entries=linkedMapOf<String,String>()
        ZipInputStream(ByteArrayInputStream(output.toByteArray())).use { zip ->
            while(true) {
                val entry=zip.nextEntry ?: break
                entries[entry.name]=zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        assertEquals(setOf("incident-report.md","incident-report.json","test-scenario.json","README.txt"),entries.keys.map {it.substringAfter('/')}.toSet())
        assertTrue(entries.keys.all {it.startsWith("RPA-") && !it.contains("..")})
        assertFalse(entries.values.any {it.contains("secret-value") || it.contains("contact@example.com")})
        assertFalse(entries.keys.any {it.contains("execution-result") || it.contains("verification-result")})
    }
    @Test(expected=IllegalArgumentException::class) fun partialReportCannotInventScenario() {
        DeveloperPackageExporter.scenarioJson(report.copy(reproduction=null))
    }
}

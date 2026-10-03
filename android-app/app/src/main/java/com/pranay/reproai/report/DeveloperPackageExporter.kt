package com.pranay.reproai.report

import com.google.gson.GsonBuilder
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DeveloperPackageExporter {
    private val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
    fun scenarioJson(report: IncidentReport): String = gson.toJson(
        requireNotNull(IncidentReportExporter.sanitized(report).reproduction) { "Analyze the incident before exporting its scenario." }.scenario)
    fun files(report: IncidentReport): Map<String,String> {
        val safe = IncidentReportExporter.sanitized(report)
        return linkedMapOf<String,String>(
            "incident-report.md" to IncidentReportExporter.markdown(safe),
            "incident-report.json" to IncidentReportExporter.json(safe)
        ).apply {
            safe.reproduction?.let { put("test-scenario.json",scenarioJson(safe)) }
            safe.execution?.let { put("execution-result.json",gson.toJson(it.result)) }
            safe.verification?.let { put("verification-result.json",gson.toJson(it.execution.result)) }
            put("README.txt","ReproAI ${safe.incidentId}\nStatus: ${safe.status}\n" +
                "Only available, sanitized artifacts are included.\n" +
                "Execution mode: ${safe.environment.runnerMode ?: "not available"}\n" +
                "Execution strategy: ${safe.environment.networkStrategy ?: "not available"}\n" +
                (if(safe.environment.networkStrategy == "ANDROID_CONFIGURATION")
                    "Actual Android rotation/Activity recreation; deliberate DemoShop state-restoration bug.\n"
                 else "DemoShop DEMO_HOOK simulates network/payment behavior; ADB controls the real debug APK.\n") +
                "MOCK never proves product reproduction.\n" +
                "test-scenario.json uses the existing Repro Runner /execute schema.\n")
        }
    }
    fun writeZip(report: IncidentReport, output: OutputStream) {
        val folder = report.incidentId.replace(Regex("[^A-Za-z0-9-]"),"_")
        ZipOutputStream(output).use { zip -> files(report).forEach { (name,content) ->
            zip.putNextEntry(ZipEntry("$folder/$name").apply { time=0 })
            zip.write(content.toByteArray(Charsets.UTF_8));zip.closeEntry()
        } }
    }
}

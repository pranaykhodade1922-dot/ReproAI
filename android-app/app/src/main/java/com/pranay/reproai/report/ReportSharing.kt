package com.pranay.reproai.report

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

enum class ReportFormat(val extension: String, val mime: String) {
    JSON("json", "application/json"), MARKDOWN("md", "text/markdown"), TEXT("txt", "text/plain"),
    SCENARIO_JSON("json", "application/json"), DEVELOPER_PACKAGE("zip", "application/zip")
}
object ReportSharing {
    fun shareText(context: Context, report: IncidentReport) {
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type="text/plain"
            putExtra(Intent.EXTRA_TEXT, IncidentReportExporter.summary(report))
            putExtra(Intent.EXTRA_SUBJECT, "ReproAI ${report.incidentId}")
        }, "Share incident report"))
    }
    fun exportFile(context: Context, report: IncidentReport, format: ReportFormat): File {
        val directory = File(context.filesDir, "reports").apply { mkdirs() }
        val safeId = report.incidentId.replace(Regex("[^A-Za-z0-9-]"), "_")
        val suffix = if(format == ReportFormat.SCENARIO_JSON) "-test-scenario" else if(format == ReportFormat.DEVELOPER_PACKAGE) "-developer-package" else ""
        val file = File(directory,"reproai-$safeId$suffix.${format.extension}")
        if(format == ReportFormat.DEVELOPER_PACKAGE) {
            file.outputStream().use { DeveloperPackageExporter.writeZip(report,it) }
            return file
        }
        file.writeText(when(format) { ReportFormat.JSON -> IncidentReportExporter.json(report)
            ReportFormat.MARKDOWN -> IncidentReportExporter.markdown(report); ReportFormat.TEXT -> IncidentReportExporter.text(report)
            ReportFormat.SCENARIO_JSON -> DeveloperPackageExporter.scenarioJson(report)
            ReportFormat.DEVELOPER_PACKAGE -> error("ZIP handled above") }, Charsets.UTF_8)
        return file
    }
    fun shareFile(context: Context, file: File, format: ReportFormat) {
        val uri = FileProvider.getUriForFile(context,"${context.packageName}.reports",file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type=format.mime; putExtra(Intent.EXTRA_STREAM,uri)
            clipData=ClipData.newRawUri("Incident report",uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        },"Export incident report"))
    }
}

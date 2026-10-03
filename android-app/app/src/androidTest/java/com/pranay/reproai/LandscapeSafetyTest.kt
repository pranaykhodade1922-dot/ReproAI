package com.pranay.reproai

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.*
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.report.IncidentReportBuilder
import com.pranay.reproai.ui.screens.report.IncidentReportScreen
import com.pranay.reproai.ui.theme.ReproAITheme
import org.junit.Rule
import org.junit.Test

class LandscapeSafetyTest {
    @get:Rule val rule=createComposeRule()
    @Test fun landscapeReportScrollsAndExportChoicesRemainReachable() {
        val report=IncidentReportBuilder.build(DebugSession("landscape",issueTitle="Long incident title repeated for a narrow-height landscape viewport"),null,"report",1)
        rule.setContent {
            val pixels=LocalConfiguration.current.screenWidthDp * LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(pixels/720f,1.3f)) {
                ReproAITheme {Box(Modifier.requiredSize(720.dp,360.dp).testTag("landscape")) {IncidentReportScreen(report,{})}}
            }
        }
        rule.onNode(hasScrollToIndexAction()).performScrollToIndex(1)
        rule.onNodeWithText("Share report").assertIsDisplayed()
        rule.onNodeWithText("Export").assertIsDisplayed().performClick()
        rule.onNodeWithText("Export developer package").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithText("Copy summary").assertIsDisplayed()
    }
}

package com.pranay.reproai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pranay.reproai.navigation.ReproNavHost
import com.pranay.reproai.ui.theme.DarkBackground
import com.pranay.reproai.ui.theme.ReproAITheme
import com.pranay.reproai.viewmodel.ReproViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReproAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    color = DarkBackground
                ) {
                    val reproViewModel: ReproViewModel = viewModel()
                    ReproNavHost(viewModel = reproViewModel)
                }
            }
        }
    }
}

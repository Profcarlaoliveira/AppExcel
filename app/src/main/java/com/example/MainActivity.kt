package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UserProgress
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppMode
import com.example.ui.viewmodel.LessonViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LessonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Collect states reactively
                val progress by viewModel.userProgress.collectAsStateWithLifecycle(initialValue = UserProgress())
                val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()

                AnimatedContent(
                    targetState = currentMode,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    modifier = Modifier.fillMaxSize(),
                    label = "AppScreenTransitions"
                ) { mode ->
                    when (mode) {
                        AppMode.DASHBOARD -> {
                            MainDashboard(
                                viewModel = viewModel,
                                progress = progress,
                                onNavigate = { viewModel.setMode(it) }
                            )
                        }
                        AppMode.EXPLORE -> {
                            ExploreScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.setMode(AppMode.DASHBOARD) }
                            )
                        }
                        AppMode.PRACTICE -> {
                            PracticeScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.setMode(AppMode.DASHBOARD) }
                            )
                        }
                        AppMode.QUIZ -> {
                            QuizScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.setMode(AppMode.DASHBOARD) }
                            )
                        }
                    }
                }
            }
        }
    }
}

package inc.dimasic.taskcal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import dagger.hilt.android.AndroidEntryPoint
import inc.dimasic.taskcal.ui.AppTopBar
import inc.dimasic.taskcal.ui.BottomBar
import inc.dimasic.taskcal.ui.BottomTab
import inc.dimasic.taskcal.ui.screens.AddTaskScreen
import inc.dimasic.taskcal.ui.screens.FoodScreen
import inc.dimasic.taskcal.ui.screens.ProfileScreen
import inc.dimasic.taskcal.ui.screens.TasksScreen
import inc.dimasic.taskcal.ui.screens.TodayScreen
import inc.dimasic.taskcal.ui.screens.statistic.StatisticScreen
import inc.dimasic.taskcal.ui.theme.TaskCalTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskCalTheme {
                TaskCalApp()
            }
        }
    }
}

private const val SCREEN_TRANSITION_MS = 300

private enum class Overlay { NONE, PROFILE, ADD_TASK }

@Composable
fun TaskCalApp() {
    var selectedTab by remember { mutableStateOf(BottomTab.TODAY) }
    var overlay by remember { mutableStateOf(Overlay.NONE) }

    BackHandler(enabled = overlay != Overlay.NONE) { overlay = Overlay.NONE }

    AnimatedContent(
        targetState = overlay,
        transitionSpec = {
            val direction = if (targetState != Overlay.NONE) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            val slideSpec = tween<IntOffset>(SCREEN_TRANSITION_MS)
            val fadeSpec = tween<Float>(SCREEN_TRANSITION_MS)
            (slideIntoContainer(direction, slideSpec) + fadeIn(fadeSpec))
                .togetherWith(slideOutOfContainer(direction, slideSpec) + fadeOut(fadeSpec))
        },
        label = "screenTransition",
    ) { currentOverlay ->
        when (currentOverlay) {
            Overlay.PROFILE -> Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    AppTopBar(title = "Profile", onBackClick = { overlay = Overlay.NONE })
                },
            ) { innerPadding ->
                ProfileScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            Overlay.ADD_TASK -> Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    AppTopBar(title = "New task", onBackClick = { overlay = Overlay.NONE })
                },
            ) { innerPadding ->
                AddTaskScreen(
                    onTaskCreated = { overlay = Overlay.NONE },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            Overlay.NONE -> Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    AppTopBar(
                        title = selectedTab.label,
                        onProfileClick = { overlay = Overlay.PROFILE },
                    )
                },
                bottomBar = {
                    BottomBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                },
            ) { innerPadding ->
                TabContent(
                    tab = selectedTab,
                    onAddTask = { overlay = Overlay.ADD_TASK },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
        }
    }
}

@Composable
fun TabContent(tab: BottomTab, onAddTask: () -> Unit, modifier: Modifier = Modifier) {
    when (tab) {
        BottomTab.TODAY -> TodayScreen(modifier = modifier)
        BottomTab.TASKS -> TasksScreen(onAddTask = onAddTask, modifier = modifier)
        BottomTab.FOOD -> FoodScreen(modifier = modifier)
        BottomTab.STATISTIC -> StatisticScreen(modifier = modifier)
    }
}

@Preview(showBackground = true)
@Composable
fun TaskCalAppPreview() {
    TaskCalTheme {
        TaskCalApp()
    }
}

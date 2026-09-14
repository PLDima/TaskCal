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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import dagger.hilt.android.AndroidEntryPoint
import inc.dimasic.taskcal.ui.AppTopBar
import inc.dimasic.taskcal.ui.BottomBar
import inc.dimasic.taskcal.ui.BottomTab
import inc.dimasic.taskcal.ui.screens.tasks.TaskEditorScreen
import inc.dimasic.taskcal.ui.screens.food.FoodScreen
import inc.dimasic.taskcal.ui.screens.profile.ProfileDetailsScreen
import inc.dimasic.taskcal.ui.screens.profile.ProfileScreen
import inc.dimasic.taskcal.ui.screens.tasks.TasksScreen
import inc.dimasic.taskcal.ui.screens.today.TodayScreen
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

private enum class Overlay { NONE, PROFILE, PROFILE_DETAILS, TASK_EDITOR }

@Composable
fun TaskCalApp() {
    // Saveable, so rotation and process recreation land back on the same tab and overlay.
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.TODAY) }
    var overlay by rememberSaveable { mutableStateOf(Overlay.NONE) }
    // Which task the editor overlay is editing; null means it's creating one.
    var editingTaskId by rememberSaveable { mutableStateOf<String?>(null) }

    // Details sit on top of Profile, so backing out of them returns there rather than to the tabs.
    val closeOverlay = { overlay = if (overlay == Overlay.PROFILE_DETAILS) Overlay.PROFILE else Overlay.NONE }
    BackHandler(enabled = overlay != Overlay.NONE) { closeOverlay() }

    AnimatedContent(
        targetState = overlay,
        transitionSpec = {
            // Overlays are declared in stacking order, so a deeper one slides in, a shallower one out.
            val direction = if (targetState.ordinal > initialState.ordinal) {
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
                    onOpenDetails = { overlay = Overlay.PROFILE_DETAILS },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            Overlay.PROFILE_DETAILS -> Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    AppTopBar(title = "Your details", onBackClick = closeOverlay)
                },
            ) { innerPadding ->
                ProfileDetailsScreen(
                    onSaved = closeOverlay,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            Overlay.TASK_EDITOR -> Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    AppTopBar(
                        title = if (editingTaskId == null) "New task" else "Edit task",
                        onBackClick = { overlay = Overlay.NONE },
                    )
                },
            ) { innerPadding ->
                TaskEditorScreen(
                    taskId = editingTaskId,
                    onSaved = { overlay = Overlay.NONE },
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
                    onAddTask = {
                        editingTaskId = null
                        overlay = Overlay.TASK_EDITOR
                    },
                    onEditTask = { taskId ->
                        editingTaskId = taskId
                        overlay = Overlay.TASK_EDITOR
                    },
                    onShowTasks = { selectedTab = BottomTab.TASKS },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
        }
    }
}

@Composable
fun TabContent(
    tab: BottomTab,
    onAddTask: () -> Unit,
    onEditTask: (taskId: String) -> Unit,
    onShowTasks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (tab) {
        BottomTab.TODAY -> TodayScreen(onAddTask = onAddTask, onShowTasks = onShowTasks, modifier = modifier)
        BottomTab.TASKS -> TasksScreen(onAddTask = onAddTask, onEditTask = { onEditTask(it.taskId) }, modifier = modifier)
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

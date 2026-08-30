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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import inc.dimasic.taskcal.ui.AppTopBar
import inc.dimasic.taskcal.ui.BottomBar
import inc.dimasic.taskcal.ui.BottomTab
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

private const val PROFILE_TRANSITION_MS = 300

@Composable
fun TaskCalApp() {
    var selectedTab by remember { mutableStateOf(BottomTab.TODAY) }
    var showProfile by remember { mutableStateOf(false) }

    BackHandler(enabled = showProfile) { showProfile = false }

    AnimatedContent(
        targetState = showProfile,
        transitionSpec = {
            val direction = if (targetState) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            val slideSpec = tween<IntOffset>(PROFILE_TRANSITION_MS)
            val fadeSpec = tween<Float>(PROFILE_TRANSITION_MS)
            (slideIntoContainer(direction, slideSpec) + fadeIn(fadeSpec))
                .togetherWith(slideOutOfContainer(direction, slideSpec) + fadeOut(fadeSpec))
        },
        label = "profileTransition",
    ) { profileVisible ->
        if (profileVisible) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    AppTopBar(title = "Profile", onBackClick = { showProfile = false })
                },
            ) { innerPadding ->
                ProfileScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    AppTopBar(
                        title = selectedTab.label,
                        onProfileClick = { showProfile = true },
                    )
                },
                bottomBar = {
                    BottomBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                },
            ) { innerPadding ->
                TabContent(
                    tab = selectedTab,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
        }
    }
}

@Composable
fun TabContent(tab: BottomTab, modifier: Modifier = Modifier) {
    when (tab) {
        BottomTab.TODAY -> TodayScreen(modifier = modifier)
        BottomTab.TASKS -> TasksScreen(modifier = modifier)
        BottomTab.FOOD -> FoodScreen(modifier = modifier)
        BottomTab.STATISTIC -> StatisticScreen(modifier = modifier)
        else -> Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = tab.label, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskCalAppPreview() {
    TaskCalTheme {
        TaskCalApp()
    }
}

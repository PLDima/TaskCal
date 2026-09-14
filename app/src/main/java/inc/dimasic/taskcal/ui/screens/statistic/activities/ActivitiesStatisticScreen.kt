package inc.dimasic.taskcal.ui.screens.statistic.activities

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import inc.dimasic.taskcal.common.utils.defaultRequestHandler
import inc.dimasic.taskcal.domain.model.ActivitiesPeriod
import inc.dimasic.taskcal.domain.model.HealthConnectStatus
import inc.dimasic.taskcal.ui.screens.statistic.statistic_views.PeriodNavigator
import inc.dimasic.taskcal.ui.screens.uiViews.GoalProgressUi
import inc.dimasic.taskcal.ui.screens.uiViews.GoalRing
import inc.dimasic.taskcal.ui.screens.uiViews.WeightCard
import inc.dimasic.taskcal.ui.screens.uiViews.healthConnectPlayStoreIntent
import inc.dimasic.taskcal.ui.screens.uiViews.rememberHealthConnectPermissionLauncher
import inc.dimasic.taskcal.ui.screens.uiViews.startActivitySafely
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter

@Composable
fun ActivitiesStatisticScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivitiesStatisticViewModel = hiltViewModel(),
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val healthConnectStatus by viewModel.healthConnectStatus.collectAsState()

    // Permission is granted through the system Health Connect UI, outside the app, so re-check on resume.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshHealthConnect()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ActivitiesPeriodTabs(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = viewModel::selectPeriod,
            modifier = Modifier.fillMaxWidth(),
        )

        viewModel.getState().defaultRequestHandler(
            onSuccess = { period ->
                PeriodContent(
                    period = period,
                    healthConnectStatus = healthConnectStatus,
                    viewModel = viewModel,
                    onPreviousPeriod = viewModel::showPreviousPeriod,
                    onNextPeriod = viewModel::showNextPeriod,
                    contentModifier = Modifier.weight(1f),
                )
            },
        )
    }
}

@Composable
private fun PeriodContent(
    period: ActivitiesPeriodUi,
    healthConnectStatus: HealthConnectStatus,
    viewModel: ActivitiesStatisticViewModel,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    contentModifier: Modifier = Modifier,
) {
    PeriodNavigator(
        dateRangeLabel = period.dateRangeLabel,
        onPreviousPeriod = onPreviousPeriod,
        onNextPeriod = onNextPeriod,
        modifier = Modifier.fillMaxWidth(),
    )

    Column(
        modifier = contentModifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        if (healthConnectStatus != HealthConnectStatus.CONNECTED) {
            HealthConnectPrompt(
                status = healthConnectStatus,
                permissions = viewModel.healthConnectPermissions,
                onPermissionResult = viewModel::refreshHealthConnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }

        ActivityRingsRow(
            caloriesBurned = period.caloriesBurned,
            steps = period.steps,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )

        CharacteristicsSection(
            characteristics = period.characteristics,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        )

        WeightCard(
            weight = period.weight,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp),
        )
    }
}

@Composable
private fun HealthConnectPrompt(
    status: HealthConnectStatus,
    permissions: Set<String>,
    onPermissionResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val permissionLauncher = rememberHealthConnectPermissionLauncher(onResult = onPermissionResult)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ItemBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                // Nothing to tap through to when the device can't run Health Connect at all.
                enabled = status != HealthConnectStatus.UNAVAILABLE,
                onClick = {
                    when (status) {
                        HealthConnectStatus.NOT_CONNECTED -> permissionLauncher.launch(permissions)
                        HealthConnectStatus.NEEDS_INSTALL -> context.startActivitySafely(healthConnectPlayStoreIntent())
                        HealthConnectStatus.UNAVAILABLE, HealthConnectStatus.CONNECTED -> Unit
                    }
                },
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Connect Health Connect",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = when (status) {
                    HealthConnectStatus.NOT_CONNECTED -> "Grant permission to see your real activity data."
                    HealthConnectStatus.NEEDS_INSTALL -> "Tap to install or update Health Connect from the Play Store."
                    HealthConnectStatus.UNAVAILABLE -> "Health Connect isn't supported on this device."
                    HealthConnectStatus.CONNECTED -> ""
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


@Composable
private fun CharacteristicsSection(
    characteristics: List<ActivityCharacteristic>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ItemBackground)
            .padding(horizontal = 16.dp),
    ) {
        characteristics.forEachIndexed { index, characteristic ->
            CharacteristicRow(
                characteristic = characteristic,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
            )
            if (index != characteristics.lastIndex) {
                HorizontalDivider(color = ItemSplitter)
            }
        }
    }
}

@Composable
private fun CharacteristicRow(
    characteristic: ActivityCharacteristic,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = characteristic.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = characteristic.valueLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            characteristic.goalPercent?.let { percent ->
                Text(
                    text = "$percent% of daily goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        characteristic.fraction?.let { fraction ->
            CharacteristicProgressBar(
                fraction = fraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun CharacteristicProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun ActivityRingsRow(
    caloriesBurned: GoalProgressUi,
    steps: GoalProgressUi,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GoalRing(progress = caloriesBurned, caption = "Calories burned")
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            color = ItemSplitter,
        )
        GoalRing(progress = steps, caption = "Steps")
    }
}

@Composable
private fun ActivitiesPeriodTabs(
    selectedPeriod: ActivitiesPeriod,
    onPeriodSelected: (ActivitiesPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ActivitiesPeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                label = "periodTabBackground",
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "periodTabTextColor",
            )
            Text(
                text = period.label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .padding(vertical = 4.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = { onPeriodSelected(period) },
                    )
            )
        }
    }
}

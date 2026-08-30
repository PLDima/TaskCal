package inc.dimasic.taskcal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter
import inc.dimasic.taskcal.usecase.ProfileGoals

private enum class GoalDialog {
    CALORIES,
    MACROS,
    WEIGHT,
}

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel: ProfileViewModel = hiltViewModel()) {
    val goals by viewModel.goals.collectAsState()
    var openDialog by remember { mutableStateOf<GoalDialog?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        UserCard(
            name = "User",
            tagline = "take control of your health and stay consistent.",
            onClick = { /* TODO: open profile details */ },
            modifier = Modifier.fillMaxWidth(),
        )

        GoalsSection(
            goals = goals,
            onCaloriesGoalClick = { openDialog = GoalDialog.CALORIES },
            onMacroGoalsClick = { openDialog = GoalDialog.MACROS },
            onWeightGoalClick = { openDialog = GoalDialog.WEIGHT },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )

        HealthConnectSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )

        PreferencesSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )

        OtherSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )
    }

    when (openDialog) {
        GoalDialog.CALORIES -> CaloriesGoalDialog(
            initialValue = goals.caloriesGoal,
            onConfirm = {
                viewModel.updateCaloriesGoal(it)
                openDialog = null
            },
            onDismiss = { openDialog = null },
        )
        GoalDialog.MACROS -> MacroGoalsDialog(
            initialProtein = goals.proteinGoal,
            initialCarbs = goals.carbsGoal,
            initialFat = goals.fatGoal,
            onConfirm = { protein, carbs, fat ->
                viewModel.updateMacroGoals(protein, carbs, fat)
                openDialog = null
            },
            onDismiss = { openDialog = null },
        )
        GoalDialog.WEIGHT -> WeightGoalDialog(
            initialValue = goals.weightGoalKg,
            onConfirm = {
                viewModel.updateWeightGoal(it)
                openDialog = null
            },
            onDismiss = { openDialog = null },
        )
        null -> Unit
    }
}

@Composable
private fun UserCard(
    name: String,
    tagline: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ItemBackground)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(20.dp),
        )
    }
}

@Composable
private fun GoalsSection(
    goals: ProfileGoals,
    onCaloriesGoalClick: () -> Unit,
    onMacroGoalsClick: () -> Unit,
    onWeightGoalClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Goals",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ItemBackground)
                .padding(horizontal = 16.dp),
        ) {
            PropertyRow(
                icon = Icons.Filled.LocalFireDepartment,
                name = "Daily calories goal",
                value = "${goals.caloriesGoal} kcal",
                onClick = onCaloriesGoalClick,
            )
            HorizontalDivider(color = ItemSplitter)
            PropertyRow(
                icon = Icons.Filled.PieChart,
                name = "Macro goals",
                value = "P ${goals.proteinGoal.toInt()} · C ${goals.carbsGoal.toInt()} · F ${goals.fatGoal.toInt()} g",
                onClick = onMacroGoalsClick,
            )
            HorizontalDivider(color = ItemSplitter)
            PropertyRow(
                icon = Icons.Filled.MonitorWeight,
                name = "Weight goal",
                value = "%.1f kg".format(goals.weightGoalKg),
                onClick = onWeightGoalClick,
            )
        }
    }
}

@Composable
private fun HealthConnectSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Health Connect",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ItemBackground)
                .padding(horizontal = 16.dp),
        ) {
            PropertyRow(
                icon = Icons.Filled.MonitorHeart,
                name = "Health Connect",
                value = "Connected",
                onClick = { /* TODO: connect / disconnect Health Connect */ },
            )
        }
    }
}

@Composable
private fun PreferencesSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ItemBackground)
                .padding(horizontal = 16.dp),
        ) {
            PropertyRow(
                icon = Icons.Filled.Straighten,
                name = "Units",
                value = "Metric (kg, cm)",
                onClick = { /* TODO: change units */ },
            )
        }
    }
}

@Composable
private fun OtherSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Other",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ItemBackground)
                .padding(horizontal = 16.dp),
        ) {
            PropertyRow(
                icon = Icons.Filled.Notifications,
                name = "Notifications",
                value = "Manage reminders",
                onClick = { /* TODO: open notification settings */ },
            )
            HorizontalDivider(color = ItemSplitter)
            PropertyRow(
                icon = Icons.Filled.PrivacyTip,
                name = "Privacy",
                value = "Privacy policy and data usage",
                onClick = { /* TODO: open privacy policy */ },
            )
        }
    }
}

@Composable
private fun PropertyRow(
    icon: ImageVector,
    name: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(20.dp),
        )
    }
}

@Composable
private fun CaloriesGoalDialog(
    initialValue: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initialValue.toString()) }
    val parsedValue = text.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily calories goal") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Calories (kcal)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { parsedValue?.let(onConfirm) },
                enabled = parsedValue != null && parsedValue > 0,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun MacroGoalsDialog(
    initialProtein: Double,
    initialCarbs: Double,
    initialFat: Double,
    onConfirm: (protein: Double, carbs: Double, fat: Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var protein by remember { mutableStateOf(initialProtein.toString()) }
    var carbs by remember { mutableStateOf(initialCarbs.toString()) }
    var fat by remember { mutableStateOf(initialFat.toString()) }

    val proteinValue = protein.toDoubleOrNull()
    val carbsValue = carbs.toDoubleOrNull()
    val fatValue = fat.toDoubleOrNull()
    val isValid = proteinValue != null && carbsValue != null && fatValue != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Macro goals") },
        text = {
            Column {
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbs (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (proteinValue != null && carbsValue != null && fatValue != null) {
                        onConfirm(proteinValue, carbsValue, fatValue)
                    }
                },
                enabled = isValid,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun WeightGoalDialog(
    initialValue: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initialValue.toString()) }
    val parsedValue = text.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Weight goal") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Weight (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { parsedValue?.let(onConfirm) },
                enabled = parsedValue != null && parsedValue > 0,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

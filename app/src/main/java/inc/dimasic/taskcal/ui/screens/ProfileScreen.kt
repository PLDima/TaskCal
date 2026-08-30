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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
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
private fun GoalsSection(modifier: Modifier = Modifier) {
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
                value = "3000 kcal",
                onClick = { /* TODO: edit daily calories goal */ },
            )
            HorizontalDivider(color = ItemSplitter)
            PropertyRow(
                icon = Icons.Filled.PieChart,
                name = "Macro goals",
                value = "P 140 · C 250 · F 70 g",
                onClick = { /* TODO: edit macro goals */ },
            )
            HorizontalDivider(color = ItemSplitter)
            PropertyRow(
                icon = Icons.Filled.MonitorWeight,
                name = "Weight goal",
                value = "68 kg",
                onClick = { /* TODO: edit weight goal */ },
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

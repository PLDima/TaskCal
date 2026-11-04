package inc.dimasic.taskcal.ui.screens.uiViews

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.health.connect.client.PermissionController

/**
 * Launcher for the Health Connect permission sheet. Launch it with the permission set the
 * ViewModel exposes; [onResult] fires once the sheet closes, whatever was granted, so the caller
 * can re-check the connection status.
 */
@Composable
fun rememberHealthConnectPermissionLauncher(
    onResult: () -> Unit,
): ManagedActivityResultLauncher<Set<String>, Set<String>> {
    val contract = remember { PermissionController.createRequestPermissionResultContract() }
    return rememberLauncherForActivityResult(contract) { onResult() }
}

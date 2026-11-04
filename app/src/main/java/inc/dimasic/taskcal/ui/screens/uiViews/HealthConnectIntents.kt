package inc.dimasic.taskcal.ui.screens.uiViews

import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient

/** Where the Health Connect rows send the user, depending on what state the integration is in. */

/** The Play Store listing that installs or updates the Health Connect provider. */
fun healthConnectPlayStoreIntent(): Intent =
    Intent(Intent.ACTION_VIEW).apply {
        setPackage("com.android.vending")
        data = Uri.parse(
            "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding",
        )
    }

fun healthConnectSettingsIntent(): Intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)

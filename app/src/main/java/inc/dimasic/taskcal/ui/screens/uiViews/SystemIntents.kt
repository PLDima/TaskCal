package inc.dimasic.taskcal.ui.screens.uiViews

import android.content.Context
import android.content.Intent
import android.provider.Settings

/** The system's notification settings page for this app. */
fun appNotificationSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

/** Starts [intent] if anything can handle it; a device with nothing to open it just does nothing. */
fun Context.startActivitySafely(intent: Intent) {
    runCatching { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

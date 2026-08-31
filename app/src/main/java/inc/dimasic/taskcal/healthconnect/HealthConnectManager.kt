package inc.dimasic.taskcal.healthconnect

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Whether the Health Connect provider can be used on this device. */
enum class HealthConnectAvailability {
    /** Provider is installed and ready. */
    INSTALLED,

    /** Provider is missing or needs an update from the Play Store. */
    NEEDS_INSTALL,

    /** The device does not support Health Connect at all. */
    NOT_SUPPORTED,
}

/**
 * Thin wrapper around [HealthConnectClient] giving repositories a single place to check Health
 * Connect availability and permissions, without each of them talking to the SDK directly.
 */
@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Read permissions the app asks for: steps, active minutes, weight, distance and heart rate. */
    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
    )

    val availability: HealthConnectAvailability
        get() = when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectAvailability.NEEDS_INSTALL
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }

    /** Contract used with `rememberLauncherForActivityResult` to show the Health Connect permission sheet. */
    fun permissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    /** True only when the provider is installed and every permission in [permissions] is granted. */
    suspend fun hasAllPermissions(): Boolean {
        if (availability != HealthConnectAvailability.INSTALLED) return false
        val granted = HealthConnectClient.getOrCreate(context).permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }
}

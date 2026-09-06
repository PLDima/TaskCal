package inc.dimasic.taskcal.data.healthconnect

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import inc.dimasic.taskcal.data.model.DailyActivity
import inc.dimasic.taskcal.domain.model.DateRange
import java.time.Period
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

    /**
     * Read permissions the app asks for: steps, active minutes, weight, distance, heart rate and
     * calories burned. Calories is [TotalCaloriesBurnedRecord] (basal plus activity) rather than
     * active-only, since that's what fitness apps such as Google Fit actually write.
     */
    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
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

    /**
     * One [DailyActivity] per day in [range] that has any recorded data; days with nothing
     * recorded produce no entry. Empty, with no Health Connect call made, unless every permission
     * in [permissions] is granted.
     */
    suspend fun dailyActivity(range: DateRange): List<DailyActivity> {
        if (!hasAllPermissions()) return emptyList()

        val buckets = HealthConnectClient.getOrCreate(context).aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = AGGREGATED_METRICS,
                timeRangeFilter = TimeRangeFilter.between(
                    range.start.atStartOfDay(),
                    range.end.plusDays(1).atStartOfDay(),
                ),
                timeRangeSlicer = Period.ofDays(1),
            ),
        )
        return buckets.map { it.toDailyActivity() }
    }

    private companion object {
        val AGGREGATED_METRICS = setOf(
            StepsRecord.COUNT_TOTAL,
            DistanceRecord.DISTANCE_TOTAL,
            TotalCaloriesBurnedRecord.ENERGY_TOTAL,
            HeartRateRecord.BPM_AVG,
            ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
        )
    }
}

private fun AggregationResultGroupedByPeriod.toDailyActivity() = DailyActivity(
    date = startTime.toLocalDate(),
    caloriesBurned = (result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0).toInt(),
    steps = (result[StepsRecord.COUNT_TOTAL] ?: 0L).toInt(),
    distanceMeters = (result[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0).toInt(),
    activeMinutes = (result[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]?.toMinutes() ?: 0L).toInt(),
    avgHeartRate = (result[HeartRateRecord.BPM_AVG] ?: 0L).toInt(),
)

package inc.dimasic.taskcal.data.healthconnect

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import inc.dimasic.taskcal.data.model.RecordedActivity
import inc.dimasic.taskcal.domain.model.DateRange
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

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
     * Only touched once availability is known to be [HealthConnectAvailability.INSTALLED] —
     * [HealthConnectClient.getOrCreate] throws otherwise.
     */
    private val client: HealthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

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
        return client.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    /**
     * Everything recorded across the whole of [range], in one aggregate call.
     *
     * Asking Health Connect for the period total is deliberate: bucketing by day and summing the
     * buckets ourselves is not guaranteed to give the same figure, because records straddling
     * midnight go through Health Connect's own apportioning first.
     *
     * Returns [RecordedActivity.Empty] rather than throwing when Health Connect is unavailable or
     * the permissions have not been granted — the screens surface that state through
     * [inc.dimasic.taskcal.domain.model.HealthConnectStatus] and their connect prompt, not as an
     * error.
     */
    suspend fun activityTotals(range: DateRange): RecordedActivity {
        if (availability != HealthConnectAvailability.INSTALLED) return RecordedActivity.Empty

        return try {
            client.aggregate(
                AggregateRequest(
                    metrics = AGGREGATED_METRICS,
                    timeRangeFilter = TimeRangeFilter.between(
                        range.start.atStartOfDay(),
                        range.end.plusDays(1).atStartOfDay(),
                    ),
                ),
            ).toRecordedActivity()
        } catch (e: SecurityException) {
            RecordedActivity.Empty
        }
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

/** A metric with no records contributing to it comes back absent rather than zero. */
private fun AggregationResult.toRecordedActivity() = RecordedActivity(
    caloriesBurned = (this[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0).roundToInt(),
    steps = (this[StepsRecord.COUNT_TOTAL] ?: 0L).toInt(),
    distanceMeters = (this[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0).roundToInt(),
    activeMinutes = (this[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]?.toMinutes() ?: 0L).toInt(),
    avgHeartRate = this[HeartRateRecord.BPM_AVG]?.toInt(),
)

package inc.dimasic.taskcal.data.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import inc.dimasic.taskcal.data.model.RecordedActivity
import inc.dimasic.taskcal.data.model.RecordedWeight
import inc.dimasic.taskcal.domain.model.DateRange
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException

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
     * Returns [RecordedActivity.Empty] rather than throwing when Health Connect is unavailable,
     * the permissions have not been granted, or the read itself fails — the screens surface the
     * connection state through [inc.dimasic.taskcal.domain.model.HealthConnectStatus] and their
     * connect prompt, not as an error.
     */
    suspend fun activityTotals(range: DateRange): RecordedActivity {
        if (availability != HealthConnectAvailability.INSTALLED) return RecordedActivity.Empty

        return readOr(RecordedActivity.Empty) {
            client.aggregate(
                AggregateRequest(
                    metrics = AGGREGATED_METRICS,
                    timeRangeFilter = TimeRangeFilter.between(
                        range.start.atStartOfDay(),
                        range.end.plusDays(1).atStartOfDay(),
                    ),
                ),
            ).toRecordedActivity()
        }
    }

    /**
     * Calories burned, broken out by calendar day across [range], in one call. Unlike
     * [activityTotals], a day-by-day breakdown is unavoidable here — best/worst-day comparisons and
     * the calories chart's daily bars can't be answered from a single range total. A day with
     * nothing recorded is simply absent from the map rather than present as zero.
     */
    suspend fun dailyCaloriesBurned(range: DateRange): Map<LocalDate, Int> {
        if (availability != HealthConnectAvailability.INSTALLED) return emptyMap()

        return readOr(emptyMap()) {
            client.aggregateGroupByPeriod(
                AggregateGroupByPeriodRequest(
                    metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(
                        range.start.atStartOfDay(),
                        range.end.plusDays(1).atStartOfDay(),
                    ),
                    timeRangeSlicer = Period.ofDays(1),
                ),
            ).mapNotNull { bucket ->
                val calories = bucket.result[TotalCaloriesBurnedRecord.ENERGY_TOTAL] ?: return@mapNotNull null
                bucket.startTime.toLocalDate() to calories.inKilocalories.roundToInt()
            }.toMap()
        }
    }

    /**
     * The most recent weigh-in, looking back as far as [LATEST_WEIGHT_LOOKBACK]: a weight older
     * than that says little about the user now, so it is treated as no weight at all.
     */
    suspend fun latestWeight(): RecordedWeight? {
        if (availability != HealthConnectAvailability.INSTALLED) return null

        return readOr(null) {
            val now = LocalDateTime.now()
            client.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(now.minus(LATEST_WEIGHT_LOOKBACK), now),
                    ascendingOrder = false,
                    pageSize = 1,
                ),
            ).records.firstOrNull()?.toRecordedWeight()
        }
    }

    /** Every weigh-in across [range], oldest first. */
    suspend fun weightHistory(range: DateRange): List<RecordedWeight> {
        if (availability != HealthConnectAvailability.INSTALLED) return emptyList()

        return readOr(emptyList()) {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        range.start.atStartOfDay(),
                        range.end.plusDays(1).atStartOfDay(),
                    ),
                    ascendingOrder = true,
                ),
            ).records.map { it.toRecordedWeight() }
        }
    }

    /**
     * Runs one Health Connect read, answering [empty] instead of throwing. A [SecurityException]
     * is the expected case — the permission hasn't been granted — but the provider is a separate
     * process, and an IPC or provider failure must not take the screen down either: several
     * callers read this straight into a flow, where an exception would end the app rather than
     * the request. Cancellation is still propagated, so a superseded read stops as it should.
     */
    private suspend fun <T> readOr(empty: T, read: suspend () -> T): T =
        try {
            read()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            empty
        }

    private companion object {
        val LATEST_WEIGHT_LOOKBACK: Period = Period.ofYears(1)

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

private fun WeightRecord.toRecordedWeight() = RecordedWeight(
    date = time.atZone(zoneOffset ?: ZoneId.systemDefault()).toLocalDate(),
    kilograms = weight.inKilograms,
)

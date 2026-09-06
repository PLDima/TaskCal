package inc.dimasic.taskcal.data.model

/**
 * What the activity source recorded over one date range, as stored. Measuring these against the
 * user's goals is a domain concern.
 */
data class RecordedActivity(
    val caloriesBurned: Int,
    val steps: Int,
    val distanceMeters: Int,
    val activeMinutes: Int,
    /**
     * Null when nothing recorded a heart rate over the range. A count can honestly read zero;
     * an average cannot, so "no samples" has to stay distinct from "averaged out to nothing".
     */
    val avgHeartRate: Int?,
) {
    companion object {
        val Empty = RecordedActivity(
            caloriesBurned = 0,
            steps = 0,
            distanceMeters = 0,
            activeMinutes = 0,
            avgHeartRate = null,
        )
    }
}

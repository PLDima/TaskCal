package inc.dimasic.taskcal.ui.screens.uiViews

/** The weight card, worded: what the user weighs, where the goal is, and how far there is to go. */
data class WeightSummaryUi(
    /** e.g. "72.4 kg", or "No weigh-in yet" when nothing has been recorded. */
    val currentLabel: String,
    /** e.g. "Measured Sep 14"; null when there is no weigh-in to date. */
    val measuredLabel: String?,
    val goalLabel: String,
    /** e.g. "4.4 kg to go", "0.6 kg under goal" or "At goal"; null without a weigh-in. */
    val toGoalLabel: String?,
    /** e.g. "−0.8 kg this period"; null when the period had fewer than two weigh-ins. */
    val changeLabel: String?,
)

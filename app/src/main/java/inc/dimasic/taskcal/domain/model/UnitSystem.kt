package inc.dimasic.taskcal.domain.model

/** Which units measurements are shown and entered in. Everything is stored metric regardless. */
enum class UnitSystem(val label: String, val description: String) {
    METRIC("Metric", "Metric (kg, km, cm)"),
    IMPERIAL("Imperial", "Imperial (lb, mi, in)"),
}

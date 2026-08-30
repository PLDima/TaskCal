package inc.dimasic.taskcal.model.ui

enum class MacroType(val label: String, val unit: String, val chartMaxValue: Int) {
    PROTEIN("Protein", "g", 200),
    FAT("Fat", "g", 100),
    CARBS("Carbs", "g", 300),
}
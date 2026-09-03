package inc.dimasic.taskcal.ui.screens

/** One priority block on the Tasks screen, with its heading already worded. */
data class TaskSectionUi(
    val title: String,
    val tasks: List<TaskListItemUi>,
)

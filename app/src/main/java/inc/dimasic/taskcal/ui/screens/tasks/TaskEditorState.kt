package inc.dimasic.taskcal.ui.screens.tasks

/**
 * The form the editor should show. [session] changes every time the editor is (re)opened, so a
 * screen keyed on it starts from [form] afresh instead of resurrecting a previous visit's edits.
 */
data class TaskEditorState(
    val form: TaskFormUi,
    val session: Int,
)

package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.model.FoodEntry
import inc.dimasic.taskcal.model.Gender
import inc.dimasic.taskcal.model.MealType
import inc.dimasic.taskcal.model.NutritionGoal
import inc.dimasic.taskcal.model.Priority
import inc.dimasic.taskcal.model.RecurrenceType
import inc.dimasic.taskcal.model.Status
import inc.dimasic.taskcal.model.Task
import inc.dimasic.taskcal.model.TaskOccurrence
import inc.dimasic.taskcal.model.UserProfile
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val MOCK_CALORIES_BURNED = 2800

data class TaskListItem(
    val occurrenceId: String,
    val title: String,
    val description: String,
    val priority: Priority,
    val status: Status,
)

@HiltViewModel
class TodayViewModel @Inject constructor() : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _tasks = MutableStateFlow(mockTasks())
    private val _occurrences = MutableStateFlow(mockOccurrences(_tasks.value))
    private val _foodEntries = MutableStateFlow(mockFoodEntries())
    private val _userProfile = MutableStateFlow(mockUserProfile())

    val tasksForSelectedDate: StateFlow<List<TaskListItem>> =
        combine(_tasks, _occurrences, _selectedDate) { tasks, occurrences, date ->
            occurrences
                .filter { it.date == date }
                .mapNotNull { occurrence ->
                    val task = tasks.find { it.id == occurrence.taskId } ?: return@mapNotNull null
                    TaskListItem(
                        occurrenceId = occurrence.id,
                        title = task.title,
                        description = task.description,
                        priority = task.priority,
                        status = occurrence.status,
                    )
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val caloriesForSelectedDate: StateFlow<CaloriesSummary> =
        combine(_foodEntries, _userProfile, _selectedDate) { entries, profile, date ->
            val entriesForDate = entries.filter { it.date == date }
            val goal = profile.nutritionGoal
            CaloriesSummary(
                caloriesUsed = entriesForDate.sumOf { it.calories },
                caloriesGoal = goal.calories,
                caloriesBurned = MOCK_CALORIES_BURNED,
                proteinUsed = entriesForDate.sumOf { it.protein },
                proteinGoal = goal.protein,
                carbsUsed = entriesForDate.sumOf { it.carbs },
                carbsGoal = goal.carbs,
                fatUsed = entriesForDate.sumOf { it.fat },
                fatGoal = goal.fat,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CaloriesSummary.Empty)

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleTaskCompletion(occurrenceId: String) {
        _occurrences.value = _occurrences.value.map { occurrence ->
            if (occurrence.id != occurrenceId) {
                occurrence
            } else {
                occurrence.copy(status = if (occurrence.status == Status.DONE) Status.TODO else Status.DONE)
            }
        }
    }

    private fun mockTasks(): List<Task> = listOf(
        Task(
            title = "Finish quarterly report",
            description = "Compile Q2 numbers and send to finance",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Review PR #482",
            description = "Check the new auth flow changes",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Clean up inbox",
            description = "Archive old threads",
            priority = Priority.LOW,
            recurrenceType = RecurrenceType.DAILY,
        ),
        Task(
            title = "Schedule dentist appointment",
            description = "Call clinic before Friday",
            priority = Priority.MEDIUM,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Buy groceries",
            description = "Milk, eggs, bread, coffee",
            priority = Priority.LOW,
            recurrenceType = RecurrenceType.WEEKLY,
        ),
        Task(
            title = "Work shift",
            description = "Survive with fraps",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.DAILY,
        ),
    )

    private fun mockOccurrences(tasks: List<Task>): List<TaskOccurrence> {
        val today = LocalDate.now()
        return listOf(
            TaskOccurrence(taskId = tasks[0].id, date = today, scheduledTime = null, status = Status.TODO),
            TaskOccurrence(taskId = tasks[1].id, date = today, scheduledTime = null, status = Status.IN_PROGRESS),
            TaskOccurrence(taskId = tasks[2].id, date = today, scheduledTime = null, status = Status.DONE),
            TaskOccurrence(taskId = tasks[3].id, date = today.plusDays(1), scheduledTime = null, status = Status.TODO),
            TaskOccurrence(taskId = tasks[4].id, date = today, scheduledTime = null, status = Status.TODO),
            TaskOccurrence(taskId = tasks[5].id, date = today, scheduledTime = null, status = Status.TODO),
        )
    }

    private fun mockUserProfile(): UserProfile = UserProfile(
        name = "Alex",
        email = "alex@example.com",
        gender = Gender.OTHER,
        dateOfBirth = LocalDate.of(1995, 5, 20),
        height = 175.0,
        weight = 70.0,
        nutritionGoal = NutritionGoal(
            calories = 3000,
            protein = 140.0,
            carbs = 250.0,
            fat = 70.0,
        ),
    )

    private fun mockFoodEntries(): List<FoodEntry> {
        val today = LocalDate.now()
        return listOf(
            FoodEntry(
                date = today,
                mealType = MealType.BREAKFAST,
                calories = 420,
                protein = 28.0,
                carbs = 45.0,
                fat = 14.0,
                note = "Oatmeal with eggs",
            ),
            FoodEntry(
                date = today,
                mealType = MealType.LUNCH,
                calories = 680,
                protein = 45.0,
                carbs = 70.0,
                fat = 20.0,
                note = "Chicken bowl",
            ),
            FoodEntry(
                date = today,
                mealType = MealType.SNACK,
                calories = 210,
                protein = 12.0,
                carbs = 20.0,
                fat = 8.0,
                note = "Protein bar",
            ),
        )
    }
}

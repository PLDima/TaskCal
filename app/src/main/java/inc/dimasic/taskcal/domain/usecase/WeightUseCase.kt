package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.repository.SettingsRepository
import inc.dimasic.taskcal.data.repository.WeightRepository
import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.usecase.model.WeightSummary
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

/** Reads the user's weight against the goal they set for it. */
class WeightUseCase @Inject constructor(
    private val weightRepository: WeightRepository,
    settingsRepository: SettingsRepository,
) {

    private val userProfile = settingsRepository.userProfile

    /**
     * One-shot: the latest weigh-in against the goal, plus — when [changeOver] is given — how the
     * weight moved across that period, read from its first and last weigh-ins. The reads run
     * alongside each other; both go to Health Connect.
     */
    suspend fun summary(changeOver: DateRange? = null): WeightSummary = coroutineScope {
        val latest = async { weightRepository.latestWeight() }
        val history = async { changeOver?.let { weightRepository.weightHistory(it) }.orEmpty() }
        val goal = userProfile.first().weightGoal

        val samples = history.await()
        val change = if (samples.size >= 2) samples.last().kilograms - samples.first().kilograms else null
        val current = latest.await()
        WeightSummary(
            currentKg = current?.kilograms,
            measuredOn = current?.date,
            goalKg = goal,
            changeKg = change,
        )
    }
}

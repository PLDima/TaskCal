package inc.dimasic.taskcal.domain.usecase

import androidx.activity.result.contract.ActivityResultContract
import inc.dimasic.taskcal.data.repository.HealthConnectRepository
import inc.dimasic.taskcal.data.repository.HealthConnectStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class HealthConnectUseCase @Inject constructor(
    private val healthConnectRepository: HealthConnectRepository,
) {

    val status: Flow<HealthConnectStatus> = healthConnectRepository.status

    val permissions: Set<String> get() = healthConnectRepository.permissions

    fun permissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> =
        healthConnectRepository.permissionRequestContract()

    suspend fun refreshStatus() {
        healthConnectRepository.refreshStatus()
    }
}

package inc.dimasic.taskcal.data.repository

import androidx.activity.result.contract.ActivityResultContract
import inc.dimasic.taskcal.data.healthconnect.HealthConnectAvailability
import inc.dimasic.taskcal.data.healthconnect.HealthConnectManager
import inc.dimasic.taskcal.domain.model.HealthConnectStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class HealthConnectRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
) {

    private val _status = MutableStateFlow(HealthConnectStatus.NOT_CONNECTED)
    val status: Flow<HealthConnectStatus> = _status.asStateFlow()

    /** Permissions to hand to the request launcher. */
    val permissions: Set<String> get() = healthConnectManager.permissions

    fun permissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> =
        healthConnectManager.permissionRequestContract()

    /** Re-reads availability and granted permissions and pushes the result to [status]. */
    suspend fun refreshStatus() {
        _status.value = when {
            healthConnectManager.availability != HealthConnectAvailability.INSTALLED -> HealthConnectStatus.UNAVAILABLE
            healthConnectManager.hasAllPermissions() -> HealthConnectStatus.CONNECTED
            else -> HealthConnectStatus.NOT_CONNECTED
        }
    }
}

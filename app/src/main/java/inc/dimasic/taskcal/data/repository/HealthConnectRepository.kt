package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.local.healthconnect.HealthConnectAvailability
import inc.dimasic.taskcal.data.local.healthconnect.HealthConnectManager
import inc.dimasic.taskcal.domain.model.HealthConnectStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

@Singleton
class HealthConnectRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
) {

    private val _status = MutableStateFlow(HealthConnectStatus.NOT_CONNECTED)
    val status: Flow<HealthConnectStatus> = _status.asStateFlow()

    /** Permissions to hand to the request launcher. */
    val permissions: Set<String> get() = healthConnectManager.permissions

    /**
     * Re-reads availability and granted permissions and pushes the result to [status]. Reading the
     * granted permissions is an IPC call, and callers reach this from the main dispatcher.
     */
    suspend fun refreshStatus() {
        _status.value = withContext(Dispatchers.IO) {
            when (healthConnectManager.availability) {
                HealthConnectAvailability.NOT_SUPPORTED -> HealthConnectStatus.UNAVAILABLE
                HealthConnectAvailability.NEEDS_INSTALL -> HealthConnectStatus.NEEDS_INSTALL
                HealthConnectAvailability.INSTALLED ->
                    if (healthConnectManager.hasAllPermissions()) HealthConnectStatus.CONNECTED else HealthConnectStatus.NOT_CONNECTED
            }
        }
    }
}

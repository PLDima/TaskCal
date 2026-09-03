package inc.dimasic.taskcal.domain.model

/**
 * Whether the user's Health Connect integration is usable.
 *
 * The data layer works this out from platform availability and granted permissions, but the
 * answer itself is a domain state: [UNAVAILABLE] means Health Connect isn't installed at all,
 * [NOT_CONNECTED] means it is but hasn't been granted the permissions the app asks for.
 */
enum class HealthConnectStatus {
    NOT_CONNECTED,
    CONNECTED,
    UNAVAILABLE,
}

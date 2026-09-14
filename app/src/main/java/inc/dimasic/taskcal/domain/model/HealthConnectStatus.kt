package inc.dimasic.taskcal.domain.model

/**
 * Whether the user's Health Connect integration is usable.
 *
 * The data layer works this out from platform availability and granted permissions, but the
 * answer itself is a domain state: [NEEDS_INSTALL] means the device could run Health Connect but
 * the provider is missing or out of date, [UNAVAILABLE] means the device can't run it at all, and
 * [NOT_CONNECTED] means it's there but hasn't been granted the permissions the app asks for.
 */
enum class HealthConnectStatus {
    NOT_CONNECTED,
    CONNECTED,
    NEEDS_INSTALL,
    UNAVAILABLE,
}

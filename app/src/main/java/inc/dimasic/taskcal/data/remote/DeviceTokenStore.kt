package inc.dimasic.taskcal.data.remote

/**
 * The secret that identifies this install to the backend. The server stores only its hash, so the
 * token is effectively the password: it must never be logged.
 */
interface DeviceTokenStore {

    /** The token, created and remembered on first use so every later call sends the same one. */
    suspend fun token(): String
}
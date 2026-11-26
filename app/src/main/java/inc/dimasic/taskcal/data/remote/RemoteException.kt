package inc.dimasic.taskcal.data.remote

import inc.dimasic.taskcal.data.remote.model.ProblemDto
import com.squareup.moshi.JsonAdapter
import java.io.IOException
import retrofit2.HttpException

/** Why a call to the backend failed, in terms the rest of the app can act on. */
sealed class RemoteException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** The server couldn't be reached at all: offline, timed out, or the connection dropped. */
    class Network(cause: IOException) : RemoteException("Couldn't reach the server", cause)

    /** The token is missing, malformed or unknown to the server. */
    class Unauthorized(message: String) : RemoteException(message)

    /** The item doesn't exist for this user. The server answers the same for another user's item. */
    class NotFound(message: String) : RemoteException(message)

    /** The request clashes with what is stored, such as completing an occurrence twice. */
    class Conflict(message: String) : RemoteException(message)

    /** The server rejected the input; [errors] lists every broken rule at once. */
    class Validation(val errors: List<String>, message: String) : RemoteException(message)

    /** Anything else the server answered with, including its own failures. */
    class Server(val status: Int, message: String) : RemoteException(message)
}

/** Reads the `problem+json` body the server sends with every error, when there is one. */
internal fun HttpException.toRemoteException(problemAdapter: JsonAdapter<ProblemDto>): RemoteException {
    val problem = runCatching { response()?.errorBody()?.string()?.let(problemAdapter::fromJson) }.getOrNull()
    val message = problem?.errors?.takeIf { it.isNotEmpty() }?.joinToString()
        ?: problem?.detail
        ?: problem?.title
        ?: message()
    return when (code()) {
        400 -> RemoteException.Validation(problem?.errors.orEmpty(), message)
        401 -> RemoteException.Unauthorized(message)
        404 -> RemoteException.NotFound(message)
        409 -> RemoteException.Conflict(message)
        else -> RemoteException.Server(code(), message)
    }
}
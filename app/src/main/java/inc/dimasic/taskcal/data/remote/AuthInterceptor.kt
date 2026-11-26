package inc.dimasic.taskcal.data.remote

import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

private const val REGISTER_PATH = "/auth/device"
private const val HEALTH_PATH = "/health"

/** Sends the device token as a bearer token on every call except the two the server leaves open. */
class AuthInterceptor @Inject constructor(
    private val tokens: DeviceTokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (path.endsWith(REGISTER_PATH) || path == HEALTH_PATH) return chain.proceed(request)

        // OkHttp calls interceptors on its own worker threads, so blocking here is safe.
        val token = runBlocking { tokens.token() }
        return chain.proceed(request.newBuilder().header("Authorization", "Bearer $token").build())
    }
}
package inc.dimasic.taskcal.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthInterceptorTest {

    private val token = "a".repeat(40)

    /** Runs a request through the interceptor and returns what would have gone over the wire. */
    private fun sent(url: String): Request {
        var captured: Request? = null
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(FakeDeviceTokenStore(token)))
            .addInterceptor(
                Interceptor { chain ->
                    captured = chain.request()
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .build()
                },
            )
            .build()
        client.newCall(Request.Builder().url(url).build()).execute().close()
        return captured!!
    }

    @Test
    fun `calls carry the device token as a bearer token`() {
        assertEquals("Bearer $token", sent("http://host/api/v1/tasks").header("Authorization"))
        assertEquals("Bearer $token", sent("http://host/api/v1/me/profile").header("Authorization"))
    }

    @Test
    fun `registration and the health check go out without a token`() {
        assertNull(sent("http://host/api/v1/auth/device").header("Authorization"))
        assertNull(sent("http://host/health").header("Authorization"))
    }
}

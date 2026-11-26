package inc.dimasic.taskcal.di

import inc.dimasic.taskcal.BuildConfig
import inc.dimasic.taskcal.data.local.datastore.DataStoreDeviceTokenStore
import inc.dimasic.taskcal.data.remote.AuthInterceptor
import inc.dimasic.taskcal.data.remote.DeviceTokenStore
import inc.dimasic.taskcal.data.remote.JavaTimeAdapters
import inc.dimasic.taskcal.data.remote.TaskCalApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val API_PATH = "/api/v1/"

/** Builds the HTTP stack that [TaskCalApi] runs on. */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun moshi(): Moshi = Moshi.Builder().add(JavaTimeAdapters()).build()

    @Provides
    fun deviceTokenStore(impl: DataStoreDeviceTokenStore): DeviceTokenStore = impl

    @Provides
    @Singleton
    fun okHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val builder = OkHttpClient.Builder().addInterceptor(authInterceptor)
        if (BuildConfig.DEBUG) {
            // Headers only, with the token redacted: bodies would print the token on registration.
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    redactHeader("Authorization")
                    level = HttpLoggingInterceptor.Level.HEADERS
                },
            )
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun retrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL.trimEnd('/') + API_PATH)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun taskCalApi(retrofit: Retrofit): TaskCalApi = retrofit.create(TaskCalApi::class.java)
}
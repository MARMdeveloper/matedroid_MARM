package com.matedroid.di

import android.annotation.SuppressLint
import com.matedroid.data.api.NominatimApi
import com.matedroid.data.api.TeslamateApi
import com.matedroid.data.local.SettingsDataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideTeslamateApiFactory(
        settingsDataStore: SettingsDataStore,
        moshi: Moshi
    ): TeslamateApiFactory {
        return TeslamateApiFactory(settingsDataStore, moshi)
    }

    @Provides
    @Singleton
    fun provideNominatimApi(moshi: Moshi): NominatimApi {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NominatimApi::class.java)
    }
}

class TeslamateApiFactory(
    private val settingsDataStore: SettingsDataStore,
    private val moshi: Moshi
) {
    private var currentBaseUrl: String? = null
    private var currentAcceptInvalidCerts: Boolean? = null
    private var currentApi: TeslamateApi? = null

    fun create(baseUrl: String, acceptInvalidCerts: Boolean? = null): TeslamateApi {
        val normalizedUrl = baseUrl.trimEnd('/') + "/"
        val settings = runBlocking { settingsDataStore.settings.first() }
        val useInsecure = acceptInvalidCerts ?: settings.acceptInvalidCerts

        // Return cached API if settings haven't changed
        if (currentBaseUrl == normalizedUrl &&
            currentAcceptInvalidCerts == useInsecure &&
            currentApi != null) {
            return currentApi!!
        }

        val okHttpClient = createOkHttpClient(settings.apiToken, useInsecure)

        currentBaseUrl = normalizedUrl
        currentAcceptInvalidCerts = useInsecure
        currentApi = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TeslamateApi::class.java)

        return currentApi!!
    }

    fun invalidateCache() {
        currentBaseUrl = null
        currentAcceptInvalidCerts = null
        currentApi = null
    }

    private fun createOkHttpClient(apiToken: String, acceptInvalidCerts: Boolean): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = if (apiToken.isNotBlank()) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $apiToken")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (acceptInvalidCerts) {
            configureInsecureTls(builder)
        }

        return builder.build()
    }

    @SuppressLint("TrustAllX509TrustManager", "CustomX509TrustManager")
    private fun configureInsecureTls(builder: OkHttpClient.Builder) {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        builder.hostnameVerifier { _, _ -> true }
    }
}

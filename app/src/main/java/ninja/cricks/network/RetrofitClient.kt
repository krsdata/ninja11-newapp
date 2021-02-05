package ninja.cricks.network

import android.content.Context
import ninja.cricks.BuildConfig
import ninja.cricks.utils.BindingUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ninja.cricks.utils.BindingUtils.Companion.BASE_URL_API
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit


class RetrofitClient(val  context:Context) {
    private lateinit var interceptor: HttpLoggingInterceptor
    private lateinit var okHttpClient: OkHttpClient
    private var retrofit: Retrofit? = null

    val client: Retrofit
        get() {

            interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            if (BuildConfig.DEBUG) {
                okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(Interceptor { chain ->
                        val original = chain.request()
                        val builder = original.newBuilder()
                        builder.addHeader("Accept", "application/json")
                            .addHeader("version_code",
                                BuildConfig.VERSION_CODE.toString()
                            )
                        val request = builder.build()

                        BindingUtils.logD(
                            "ServiceGen",
                            "headrs: " + request.headers.toString()
                        )
                        chain.proceed(request)
                    })
                    .connectionSpecs(
                        Arrays.asList(
                            ConnectionSpec.MODERN_TLS,
                            ConnectionSpec.CLEARTEXT
                        )
                    )
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .retryOnConnectionFailure(true)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .cache(null)
                    .build()
            } else {
                okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(Interceptor { chain ->
                        val original = chain.request()
                        val builder = original.newBuilder()
                        builder.addHeader("Accept", "application/json")
                            .addHeader("version_code",
                                BuildConfig.VERSION_CODE.toString()
                            )
                        val request = builder.build()
                        chain.proceed(request)
                    })
                    .connectionSpecs(
                        Arrays.asList(
                            ConnectionSpec.MODERN_TLS,
                            ConnectionSpec.CLEARTEXT
                        )
                    )
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .retryOnConnectionFailure(true)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .cache(null)
                    .build()
            }
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BindingUtils.BASE_URL_API)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
            }
            return retrofit!!
        }
}
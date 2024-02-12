package ninja.cricks.network

import android.content.Context
import ninja.cricks.BuildConfig
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Arrays
import java.util.concurrent.TimeUnit

class PhonePeClient(val context: Context) {

    // Production Url
    var url = "https://api.phonepe.com/"
    // Sandbox Url
//    var url = "https://api-preprod.phonepe.com/"

    private lateinit var interceptor: HttpLoggingInterceptor
    private lateinit var okHttpClient: OkHttpClient
    private var retrofit: Retrofit? = null


    val client: Retrofit
        get() {

            interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

                okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(Interceptor { chain ->
                        val original = chain.request()
                        val request = original.newBuilder().build()
                        chain.proceed(request)
                    })
                    .connectionSpecs(
                        listOf(
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
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
            }
            return retrofit!!
        }
}
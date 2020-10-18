package ninja.cricks.network

import android.content.Context
import ninja.cricks.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ninja.cricks.utils.BindingUtils.Companion.BASE_URL_API
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitClient(val  context:Context) {
    private var retrofit: Retrofit? = null
    val service: PlugDataService
        get() {
            if (retrofit == null) {
                if(BuildConfig.DEBUG) {
                    val logging = HttpLoggingInterceptor()
                    logging.level = HttpLoggingInterceptor.Level.BODY
                    var httpClient = OkHttpClient.Builder()
                    httpClient.addInterceptor(logging);
                    retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL_API)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(httpClient.build())
                        .build()
                }else {
                    retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL_API)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
               }

            }
            return retrofit!!.create(PlugDataService::class.java)
        }
}
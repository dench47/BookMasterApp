package ru.bookmaster.app.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSerializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.bookmaster.app.BuildConfig
import java.util.concurrent.TimeUnit

object RetrofitClient {

     const val BASE_URL = "http://192.168.0.152:8080/"
//     const val BASE_URL = "http://172.25.41.231:8080/"
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Кастомный Gson с поддержкой Map
    private val gson: Gson = GsonBuilder()
        .serializeNulls()
        .registerTypeAdapter(Map::class.java, JsonSerializer<Map<*, *>> { src, _, context ->
            context.serialize(src)
        })
        .create()

    val instance: BookMasterApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(BookMasterApi::class.java)
    }
}
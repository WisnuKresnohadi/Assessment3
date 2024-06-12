package org.d3if3157.assessment3.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.d3if3157.assessment3.model.Galeri
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

private const val BASE_URL  = "https://retoolapi.dev/4EKFhN/"

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    setLevel(HttpLoggingInterceptor.Level.BODY)
}

private val client = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .build()

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .client(client)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface GaleriApiService {
    @GET("Galeri_Group")
    suspend fun getHewan(
        @Header("Authorization") userId: String
    ):List<Galeri>

    @POST("Galeri_Group")
    suspend fun postGaleri(@Body galeri: Galeri)

    @DELETE("Galeri_Group/{id}")
    suspend fun deleteGaleri(@Path("id") id: String)
}

object GaleriApi{
    val service: GaleriApiService by lazy {
        retrofit.create(GaleriApiService::class.java)
    }

    fun getHewanUrl(imageId: String):String{
        return "${BASE_URL}image.php?id=$imageId"
    }
}
enum class ApiStatus { LOADING, SUCCESS, FAILED }


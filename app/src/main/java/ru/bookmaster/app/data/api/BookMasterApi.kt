package ru.bookmaster.app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import ru.bookmaster.app.data.model.AppointmentResponse
import ru.bookmaster.app.data.model.ClientDetailResponse
import ru.bookmaster.app.data.model.ClientsResponse
import ru.bookmaster.app.data.model.LoginRequest
import ru.bookmaster.app.data.model.LoginResponse
import ru.bookmaster.app.data.model.RegisterRequest

interface BookMasterApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @GET("api/appointments/company/{companyId}")
    suspend fun getAppointments(
        @Path("companyId") companyId: Long,
        @Header("Authorization") token: String
    ): Response<List<AppointmentResponse>>

    @PUT("api/appointments/{id}/confirm")
    suspend fun confirmAppointment(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<AppointmentResponse>

    @PUT("api/appointments/{id}/cancel")
    suspend fun cancelAppointment(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<AppointmentResponse>

    @POST("api/device/register")
    suspend fun registerDeviceToken(
        @Body body: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("api/verify/send")
    suspend fun requestCallCheck(@Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("api/verify/check")
    suspend fun checkCallStatus(@Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("api/auth/login-by-phone")
    suspend fun loginByPhone(@Body body: Map<String, String>): Response<LoginResponse>

    // Добавить в конец интерфейса BookMasterApi:

    @GET("api/clients")
    suspend fun getClients(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sortBy") sortBy: String = "name",
        @Query("sortDir") sortDir: String = "asc",
        @Query("search") search: String? = null
    ): Response<ClientsResponse>

    @GET("api/clients/{id}")
    suspend fun getClientDetail(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<ClientDetailResponse>

    @PUT("api/clients/{id}/notes")
    suspend fun updateClientNotes(
        @Path("id") id: Long,
        @Body body: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    @GET("api/stats/{companyId}/today")
    suspend fun getTodayStats(
        @Path("companyId") companyId: Long,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @GET("api/stats/{companyId}/week")
    suspend fun getWeekStats(
        @Path("companyId") companyId: Long,
        @Query("weekStart") weekStart: String?,
        @Header("Authorization") token: String
    ): Response<List<Map<String, Any>>>

    @GET("api/stats/{companyId}/clients-stats")
    suspend fun getClientsStats(
        @Path("companyId") companyId: Long,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @GET("api/stats/{companyId}/masters-stats")
    suspend fun getMastersStats(
        @Path("companyId") companyId: Long,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>
}
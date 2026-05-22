package ru.bookmaster.app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import ru.bookmaster.app.data.model.AppointmentResponse
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
    ): Response<Unit>}
package ru.bookmaster.app.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
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
import ru.bookmaster.app.data.model.Master
import ru.bookmaster.app.data.model.RegisterRequest
import ru.bookmaster.app.data.model.ServiceModel
import ru.bookmaster.app.data.model.UpdateMasterRequest
import ru.bookmaster.app.data.model.UpdateServiceRequest

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

    @GET("api/appointments/company/{companyId}/pending")
    suspend fun getPendingAppointments(
        @Path("companyId") companyId: Long,
        @Header("Authorization") token: String
    ): Response<List<AppointmentResponse>>

    @GET("api/appointments/company/{companyId}/date")
    suspend fun getAppointmentsByDate(
        @Path("companyId") companyId: Long,
        @Query("date") date: String,
        @Header("Authorization") token: String
    ): Response<List<AppointmentResponse>>

    @PUT("api/appointments/company/{companyId}/mark-viewed")
    suspend fun markAppointmentsViewed(
        @Path("companyId") companyId: Long,
        @Header("Authorization") token: String
    ): Response<Unit>

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

    @PUT("api/appointments/{id}/edit")
    suspend fun editAppointment(
        @Path("id") id: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
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

    @GET("api/companies/{id}")
    suspend fun getCompany(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @PUT("api/companies/{id}")
    suspend fun updateCompany(
        @Path("id") id: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @GET("api/stats/{companyId}")
    suspend fun getStats(
        @Path("companyId") companyId: Long,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @GET("api/services/company/{companyId}")
    suspend fun getServices(
        @Path("companyId") companyId: Long,
        @Header("Authorization") token: String
    ): Response<List<ServiceModel>>

    @POST("api/services")
    suspend fun addService(
        @Body body: Map<String, Any>,
        @Header("Authorization") token: String
    ): Response<ServiceModel>

    @POST("api/services/{id}/toggle-active")
    suspend fun toggleServiceActive(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @DELETE("api/services/{id}")
    suspend fun deleteService(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("api/services/{id}")
    suspend fun getService(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<ServiceModel>

    @PUT("api/services/{id}")
    suspend fun updateService(
        @Path("id") id: Long,
        @Body body: UpdateServiceRequest,
        @Header("Authorization") token: String
    ): Response<ServiceModel>

    // ========== Мастера ==========

    @GET("api/master/company/{companyId}")
    suspend fun getMasters(
        @Path("companyId") companyId: Long,
        @Header("Authorization") token: String
    ): Response<List<Master>>

    @GET("api/master/{id}/simple")
    suspend fun getMaster(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Master>

    @POST("api/master")
    suspend fun addMaster(
        @Body body: Map<String, @JvmSuppressWildcards Any>,
        @Header("Authorization") token: String
    ): Response<Master>

    @POST("api/master/{id}/activate")
    suspend fun toggleMasterActive(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>


    @DELETE("api/master/{id}")
    suspend fun deleteMaster(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("api/master/{id}/schedule/save-all")
    suspend fun saveAllSchedule(
        @Path("id") id: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    // ========== Мастера (полные данные) ==========

    @GET("api/master/{id}")
    suspend fun getMasterDetails(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @GET("api/master/{id}/breaks")
    suspend fun getMasterBreaks(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<List<Map<String, Any>>>

    @POST("api/master/{id}/breaks")
    suspend fun addMasterBreak(
        @Path("id") id: Long,
        @Body body: Map<String, Any>,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    @DELETE("api/master/{id}/breaks/{breakId}")
    suspend fun deleteMasterBreak(
        @Path("id") id: Long,
        @Path("breakId") breakId: Long,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    @PUT("api/master/{id}/update")
    suspend fun updateMasterFull(
        @Path("id") id: Long,
        @Body body: UpdateMasterRequest,
        @Header("Authorization") token: String
    ): Response<Master>

    @PUT("api/master/{id}/services")
    suspend fun updateMasterServices(
        @Path("id") id: Long,
        @Body serviceIds: List<Long>,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    @POST("api/master/{id}/schedule/week")
    suspend fun updateWeekDay(
        @Path("id") id: Long,
        @Query("dayOfWeek") dayOfWeek: Int,
        @Query("isWorking") isWorking: Boolean,
        @Query("workStart") workStart: String,
        @Query("workEnd") workEnd: String,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    @POST("api/master/{id}/photo")
    suspend fun uploadMasterPhoto(
        @Path("id") id: Long,
        @Body body: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    @POST("api/companies/{id}/activate-premium")
    suspend fun activatePremium(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    @GET("api/appointments/company/{companyId}/cancelled-by-client")
    suspend fun getCancelledByClientAppointments(
        @Path("companyId") companyId: Long,
        @Header("Authorization") token: String
    ): Response<List<AppointmentResponse>>

    @PUT("api/appointments/company/{companyId}/mark-cancelled-viewed")
    suspend fun markCancelledViewed(
        @Path("companyId") companyId: Long,
        @Header("Authorization") token: String
    ): Response<Unit>

    @PUT("api/appointments/{id}/mark-viewed")
    suspend fun markAppointmentViewed(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Unit>
}
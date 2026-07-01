package ru.bookmaster.app.ui.masters

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.bookmaster.app.data.api.RetrofitClient
import ru.bookmaster.app.data.model.UpdateMasterRequest
import ru.bookmaster.app.util.TokenManager
import java.time.LocalDate
import com.google.gson.Gson

data class MasterService(
    val id: Long,
    val name: String,
    val price: Double,
    val durationMinutes: Int,
    val assigned: Boolean
)

data class MasterBreak(
    val id: Long,
    val dayOfWeek: Int?,
    val breakDate: String?,
    val startTime: String,
    val endTime: String,
    val description: String?,
    val label: String = ""
)

data class MasterWeekDay(
    val dayOfWeek: Int,
    val dayName: String,
    val isWorking: Boolean,
    val workStart: String,
    val workEnd: String
)

data class CalendarDay(
    val date: String,
    val label: String,
    val isWorking: Boolean?,
    val empty: Boolean = false
)

data class MasterDetailUiState(
    val masterId: Long = 0,
    val name: String = "",
    val phone: String = "",
    val description: String = "",
    val photo: String = "",
    val services: List<MasterService> = emptyList(),
    val weekDays: List<MasterWeekDay> = emptyList(),
    val breaks: List<MasterBreak> = emptyList(),
    val calendarDays: List<CalendarDay> = emptyList(),
    val calendarMonth: Int = LocalDate.now().monthValue,
    val calendarYear: Int = LocalDate.now().year,
    val timeStep: Int = 30,
    val bookingLimit: String = "none",
    val stickTime: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false
)

class MasterDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(MasterDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        generateCalendar()
    }

    fun loadMaster(masterId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.getMasterDetails(masterId, "Bearer $token")
                if (response.isSuccessful) {
                    val data = response.body()!!

                    val weekDays = (data["weekDays"] as? List<Map<String, Any>>)?.map {
                        MasterWeekDay(
                            dayOfWeek = (it["dayOfWeek"] as? Number)?.toInt() ?: 0,
                            dayName = it["dayName"] as? String ?: "",
                            isWorking = it["isWorking"] as? Boolean ?: true,
                            workStart = it["workStart"] as? String ?: "09:00",
                            workEnd = it["workEnd"] as? String ?: "18:00"
                        )
                    } ?: emptyList()

                    val services = (data["services"] as? List<Map<String, Any>>)?.map {
                        MasterService(
                            id = (it["id"] as? Number)?.toLong() ?: 0,
                            name = it["name"] as? String ?: "",
                            price = (it["price"] as? Number)?.toDouble() ?: 0.0,
                            durationMinutes = (it["durationMinutes"] as? Number)?.toInt() ?: 0,
                            assigned = it["assigned"] as? Boolean ?: false
                        )
                    } ?: emptyList()

                    _uiState.value = _uiState.value.copy(
                        masterId = (data["id"] as? Number)?.toLong() ?: masterId,
                        name = data["name"] as? String ?: "",
                        phone = data["phone"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        photo = data["photo"] as? String ?: "",
                        services = services,
                        weekDays = weekDays,
                        timeStep = (data["timeStep"] as? Number)?.toInt() ?: 30,
                        bookingLimit = data["bookingLimit"] as? String ?: "none",
                        stickTime = data["stickTime"] as? Boolean ?: false,
                        isLoading = false
                    )

                    generateCalendar()
                    loadBreaks(masterId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка загрузки: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Ошибка загрузки"
                )
            }
        }
    }

    private fun loadBreaks(masterId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.getMasterBreaks(masterId, "Bearer $token")
                if (response.isSuccessful) {
                    val breaksData = response.body() ?: emptyList()
                    val days = mapOf(1 to "Пн", 2 to "Вт", 3 to "Ср", 4 to "Чт", 5 to "Пт", 6 to "Сб", 7 to "Вс")
                    val breaks = breaksData.map {
                        val label = when {
                            it["dayOfWeek"] != null && (it["dayOfWeek"] as Number).toInt() > 0 ->
                                days[(it["dayOfWeek"] as Number).toInt()] ?: ""
                            it["breakDate"] != null -> it["breakDate"] as String
                            else -> "Каждый день"
                        }
                        MasterBreak(
                            id = (it["id"] as? Number)?.toLong() ?: 0,
                            dayOfWeek = (it["dayOfWeek"] as? Number)?.toInt(),
                            breakDate = it["breakDate"] as? String,
                            startTime = it["startTime"] as? String ?: "",
                            endTime = it["endTime"] as? String ?: "",
                            description = it["description"] as? String,
                            label = label
                        )
                    }
                    _uiState.value = _uiState.value.copy(breaks = breaks)
                }
            } catch (_: Exception) { }
        }
    }

    fun generateCalendar() {
        val year = _uiState.value.calendarYear
        val month = _uiState.value.calendarMonth
        val firstDay = LocalDate.of(year, month, 1)
        val lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth())
        val startDow = if (firstDay.dayOfWeek.value == 7) 6 else firstDay.dayOfWeek.value - 1

        val days = mutableListOf<CalendarDay>()
        repeat(startDow) { days.add(CalendarDay("", "", null, true)) }

        var d = firstDay
        while (!d.isAfter(lastDay)) {
            val dateStr = d.toString()
            val dow = d.dayOfWeek.value
            val weekDay = _uiState.value.weekDays.find { it.dayOfWeek == dow }
            val isWorking = weekDay?.isWorking ?: true
            days.add(CalendarDay(dateStr, d.dayOfMonth.toString(), isWorking))
            d = d.plusDays(1)
        }
        _uiState.value = _uiState.value.copy(calendarDays = days)
    }

    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun updatePhone(value: String) {
        _uiState.value = _uiState.value.copy(phone = value)
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun toggleServiceAssignment(serviceId: Long) {
        val current = _uiState.value.services
        val updated = current.map {
            if (it.id == serviceId) it.copy(assigned = !it.assigned) else it
        }
        _uiState.value = _uiState.value.copy(services = updated)
    }

    fun updateWeekDay(dayOfWeek: Int, field: String, value: String) {
        val current = _uiState.value.weekDays
        val updated = current.map {
            if (it.dayOfWeek == dayOfWeek) {
                when (field) {
                    "workStart" -> it.copy(workStart = value)
                    "workEnd" -> it.copy(workEnd = value)
                    else -> it
                }
            } else it
        }
        _uiState.value = _uiState.value.copy(weekDays = updated)
        generateCalendar()
    }

    fun toggleWeekDay(dayOfWeek: Int) {
        val current = _uiState.value.weekDays
        val updated = current.map {
            if (it.dayOfWeek == dayOfWeek) it.copy(isWorking = !it.isWorking) else it
        }
        _uiState.value = _uiState.value.copy(weekDays = updated)
        generateCalendar()
    }

    fun toggleDayOff(date: String) {
        val current = _uiState.value.calendarDays.find { it.date == date }
        if (current != null) {
            val newIsWorking = !(current.isWorking ?: true)
            val updated = _uiState.value.calendarDays.map {
                if (it.date == date) it.copy(isWorking = newIsWorking) else it
            }
            _uiState.value = _uiState.value.copy(calendarDays = updated)

            viewModelScope.launch {
                try {
                    val token = tokenManager.token.first() ?: ""
                    val dow = LocalDate.parse(date).dayOfWeek.value
                    api.updateWeekDay(
                        id = _uiState.value.masterId,
                        dayOfWeek = dow,
                        isWorking = newIsWorking,
                        workStart = current?.let {
                            _uiState.value.weekDays.find { w -> w.dayOfWeek == dow }?.workStart ?: "09:00"
                        } ?: "09:00",
                        workEnd = current?.let {
                            _uiState.value.weekDays.find { w -> w.dayOfWeek == dow }?.workEnd ?: "18:00"
                        } ?: "18:00",
                        token = "Bearer $token"
                    )
                } catch (_: Exception) { }
            }
        }
    }

    fun updateTimeStep(value: String) {
        value.toIntOrNull()?.let {
            _uiState.value = _uiState.value.copy(timeStep = it)
        }
    }

    fun updateBookingLimit(value: String) {
        _uiState.value = _uiState.value.copy(bookingLimit = value)
    }

    fun updateStickTime(value: Boolean) {
        _uiState.value = _uiState.value.copy(stickTime = value)
    }

    fun uploadPhoto(photoBase64: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true)
                val token = tokenManager.token.first() ?: ""

                val response = api.updateMasterFull(
                    id = _uiState.value.masterId,
                    body = UpdateMasterRequest(
                        name = _uiState.value.name,
                        photo = photoBase64,
                        phone = _uiState.value.phone,
                        description = _uiState.value.description,
                        timeStep = _uiState.value.timeStep,
                        bookingLimit = _uiState.value.bookingLimit,
                        stickTime = _uiState.value.stickTime
                    ),
                    token = "Bearer $token"
                )

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        photo = photoBase64,
                        isSaving = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка загрузки фото: ${response.code()}",
                        isSaving = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.localizedMessage ?: "Ошибка загрузки фото",
                    isSaving = false
                )
            }
        }
    }

    fun updateError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }

    fun addBreak(dayOfWeek: Int?, breakDate: String?, startTime: String, endTime: String, description: String?) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                val body = mutableMapOf<String, Any>(
                    "startTime" to startTime,
                    "endTime" to endTime
                )
                if (dayOfWeek != null) body["dayOfWeek"] = dayOfWeek
                if (breakDate != null) body["breakDate"] = breakDate
                if (description != null) body["description"] = description

                val response = api.addMasterBreak(
                    id = _uiState.value.masterId,
                    body = body,
                    token = "Bearer $token"
                )
                if (response.isSuccessful) {
                    loadBreaks(_uiState.value.masterId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage ?: "Ошибка добавления перерыва")
            }
        }
    }

    fun deleteBreak(breakId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                api.deleteMasterBreak(
                    id = _uiState.value.masterId,
                    breakId = breakId,
                    token = "Bearer $token"
                )
                val current = _uiState.value.breaks
                _uiState.value = _uiState.value.copy(breaks = current.filter { it.id != breakId })
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage ?: "Ошибка удаления перерыва")
            }
        }
    }

    fun prevMonth() {
        val m = _uiState.value.calendarMonth
        val y = _uiState.value.calendarYear
        if (m == 1) {
            _uiState.value = _uiState.value.copy(calendarMonth = 12, calendarYear = y - 1)
        } else {
            _uiState.value = _uiState.value.copy(calendarMonth = m - 1)
        }
        generateCalendar()
    }

    fun nextMonth() {
        val m = _uiState.value.calendarMonth
        val y = _uiState.value.calendarYear
        if (m == 12) {
            _uiState.value = _uiState.value.copy(calendarMonth = 1, calendarYear = y + 1)
        } else {
            _uiState.value = _uiState.value.copy(calendarMonth = m + 1)
        }
        generateCalendar()
    }

    fun getMonthLabel(): String {
        val months = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
        return "${months[_uiState.value.calendarMonth - 1]} ${_uiState.value.calendarYear}"
    }


    fun deleteMaster(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                val response = api.deleteMaster(_uiState.value.masterId, "Bearer $token")

                if (response.isSuccessful) {
                    // Если удаление прошло успешно — закрываем экран
                    onSuccess()
                } else {
                    // Если ошибка — показываем её
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка удаления: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.localizedMessage ?: "Ошибка удаления"
                )
            }
        }
    }

    fun saveAllSchedule(onSuccess: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""

                // 1. Сначала обновляем основные данные мастера
                val updateResponse = api.updateMasterFull(
                    id = state.masterId,
                    body = UpdateMasterRequest(
                        name = state.name,
                        phone = state.phone.takeIf { it.isNotBlank() },
                        description = state.description.takeIf { it.isNotBlank() },
                        timeStep = state.timeStep,
                        bookingLimit = state.bookingLimit,
                        stickTime = state.stickTime,
                        photo = state.photo.takeIf { it.isNotBlank() }
                    ),
                    token = "Bearer $token"
                )

                if (!updateResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка сохранения данных: ${updateResponse.code()}"
                    )
                    return@launch
                }

                // 2. Сохраняем услуги (галочки)
                val serviceIds = state.services.filter { it.assigned }.map { it.id }
                val servicesResponse = api.updateMasterServices(state.masterId, serviceIds, "Bearer $token")

                if (!servicesResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка сохранения услуг: ${servicesResponse.code()}"
                    )
                    return@launch
                }

                // 3. Сохраняем график (weekDays)
                val weekDaysData = state.weekDays.map { w ->
                    mapOf(
                        "dayOfWeek" to w.dayOfWeek,
                        "isWorking" to w.isWorking,
                        "workStart" to w.workStart,
                        "workEnd" to w.workEnd,
                        "timeStep" to state.timeStep
                    )
                }
                val weekDaysJson = Gson().toJson(weekDaysData)

                val datesData = emptyList<Map<String, Any>>()
                val datesJson = Gson().toJson(datesData)

                val response = api.saveAllSchedule(
                    id = state.masterId,
                    body = mapOf(
                        "weekDays" to weekDaysJson,
                        "dates" to datesJson
                    ),
                    token = "Bearer $token"
                )

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка сохранения графика: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.localizedMessage ?: "Ошибка сохранения"
                )
            }
        }
    }}
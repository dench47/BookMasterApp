package ru.bookmaster.app.data.model

data class Master(
    val id: Long,
    val name: String,
    val phone: String? = null,
    val specialization: String? = null,
    val active: Boolean = true,
    val workStart: String? = null,
    val workEnd: String? = null,
    val timeStep: Int = 30,
    val stickTime: Boolean = false,
    val bookingLimit: String? = "none"
)
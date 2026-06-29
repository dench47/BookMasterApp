package ru.bookmaster.app.data.model

data class UpdateMasterRequest(
    val name: String,
    val phone: String? = null,
    val description: String? = null,
    val timeStep: Int,
    val bookingLimit: String,
    val stickTime: Boolean,
    val photo: String? = null  // добавить

)
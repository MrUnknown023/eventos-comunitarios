package com.dsm.catedra2.eventos_comunitarios.model

import java.util.UUID

data class Event(
    val id          : String = UUID.randomUUID().toString(),
    val title       : String,
    val description : String,
    val dateTimestamp: Long,
    val timeHour    : Int = 0,   // 0-23
    val timeMinute  : Int = 0,   // 0-59
    val location    : String,
    val organizerId : String,
    val attendeesIds: List<String> = emptyList(),
    val creativeCommonsLicense: String = "CC BY"
)
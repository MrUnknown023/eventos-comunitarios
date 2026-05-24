package com.dsm.catedra2.eventos_comunitarios.model

import java.util.UUID

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val eventId: String,
    val userId: String,
    val userEmail: String,
    val rating: Int, // 1-5 stars
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
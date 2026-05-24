package com.dsm.catedra2.eventos_comunitarios.repository

import com.dsm.catedra2.eventos_comunitarios.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 1. La Interfaz (Lo que tus compañeros verán)
interface EventRepository {
    suspend fun createEvent(event: Event): Result<Unit>
    fun getAllEvents(): Flow<List<Event>>
    suspend fun getEventById(eventId: String): Result<Event>
    suspend fun updateEvent(event: Event): Result<Unit>
    suspend fun deleteEvent(eventId: String): Result<Unit>
}

// 2. Implementación Simulada (Para que el equipo trabaje desde YA)
class MockEventRepository : EventRepository {
    // Lista en memoria simulando una base de datos
    private val _eventsFlow = MutableStateFlow<List<Event>>(emptyList())

    override suspend fun createEvent(event: Event): Result<Unit> {
        _eventsFlow.update { currentList -> currentList + event }
        return Result.success(Unit)
    }

    override fun getAllEvents(): Flow<List<Event>> = _eventsFlow.asStateFlow()

    override suspend fun getEventById(eventId: String): Result<Event> {
        val event = _eventsFlow.value.find { it.id == eventId }
        return if (event != null) Result.success(event)
        else Result.failure(Exception("Evento no encontrado"))
    }

    override suspend fun updateEvent(event: Event): Result<Unit> {
        _eventsFlow.update { currentList ->
            currentList.map { if (it.id == event.id) event else it }
        }
        return Result.success(Unit)
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> {
        _eventsFlow.update { currentList ->
            currentList.filter { it.id != eventId }
        }
        return Result.success(Unit)
    }
}
package com.dsm.catedra2.eventos_comunitarios.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.catedra2.eventos_comunitarios.model.Event
import com.dsm.catedra2.eventos_comunitarios.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventViewModel(
    private val repository: EventRepository
) : ViewModel() {

    private val _events =
        MutableStateFlow<List<Event>>(emptyList())

    val events: StateFlow<List<Event>> =
        _events.asStateFlow()

    private val _uiState =
        MutableStateFlow<UiState>(UiState.Success)

    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {

        viewModelScope.launch {

            _uiState.value = UiState.Loading

            try {

                repository.getAllEvents().collect { eventList ->

                    _events.value = eventList

                    _uiState.value = UiState.Success
                }

            } catch (e: Exception) {

                _uiState.value =
                    UiState.Error(
                        e.message
                            ?: "Error al cargar eventos"
                    )
            }
        }
    }

    fun createNewEvent(
        title: String,
        description: String,
        date: Long,
        location: String,
        userId: String,
        license: String
    ) {

        viewModelScope.launch {

            _uiState.value = UiState.Loading

            val newEvent = Event(

                title = title,

                description = description,

                dateTimestamp = date,

                location = location,

                organizerId = userId,

                attendeesIds = emptyList(),

                creativeCommonsLicense = license
            )

            val result =
                repository.createEvent(newEvent)

            handleResult(result)
        }
    }

    fun updateExistingEvent(
        event: Event
    ) {

        viewModelScope.launch {

            _uiState.value = UiState.Loading

            val result =
                repository.updateEvent(event)

            handleResult(result)
        }
    }

    fun deleteEvent(
        eventId: String
    ) {

        viewModelScope.launch {

            _uiState.value = UiState.Loading

            val result =
                repository.deleteEvent(eventId)

            handleResult(result)
        }
    }

    fun confirmAttendance(
        event: Event,
        userId: String
    ) {

        viewModelScope.launch {

            try {

                if (!event.attendeesIds.contains(userId)) {

                    val updatedEvent = event.copy(

                        attendeesIds =
                            event.attendeesIds + userId
                    )

                    repository.updateEvent(updatedEvent)
                }

            } catch (e: Exception) {

                _uiState.value =
                    UiState.Error(
                        e.message
                            ?: "Error al confirmar asistencia"
                    )
            }
        }
    }

    private fun handleResult(
        result: Result<Unit>
    ) {

        _uiState.value =
            if (result.isSuccess) {

                UiState.Success

            } else {

                UiState.Error(
                    result.exceptionOrNull()?.message
                        ?: "Error desconocido"
                )
            }
    }
}

sealed class UiState {

    object Loading : UiState()

    object Success : UiState()

    data class Error(
        val message: String
    ) : UiState()
}
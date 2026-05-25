package com.dsm.catedra2.eventos_comunitarios.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.catedra2.eventos_comunitarios.R
import com.dsm.catedra2.eventos_comunitarios.model.Event
import com.dsm.catedra2.eventos_comunitarios.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EventViewModel(
    private val repository : EventRepository,
    private val context    : Context                // ← necesario para notificaciones locales
) : ViewModel() {

    companion object {
        const val CHANNEL_ID   = "event_changes_channel"
        const val CHANNEL_NAME = "Cambios en eventos"
    }

    private val _events   = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _uiState  = MutableStateFlow<UiState>(UiState.Success)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        createNotificationChannel()
        loadEvents()
    }

    // ── Crea el canal de notificaciones (Android 8+) ──────────────────────────
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando cambia la fecha u hora de un evento"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.getAllEvents().collect { eventList ->
                    _events.value  = eventList
                    _uiState.value = UiState.Success
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cargar eventos")
            }
        }
    }

    fun isDuplicateDate(dateTimestamp: Long, editingId: String? = null): Boolean {
        val cal  = Calendar.getInstance().apply { timeInMillis = dateTimestamp }
        val day  = cal.get(Calendar.DAY_OF_YEAR)
        val year = cal.get(Calendar.YEAR)

        return _events.value.any { event ->
            if (event.id == editingId) return@any false
            val eCal = Calendar.getInstance().apply { timeInMillis = event.dateTimestamp }
            eCal.get(Calendar.DAY_OF_YEAR) == day && eCal.get(Calendar.YEAR) == year
        }
    }

    fun createNewEvent(
        title       : String,
        description : String,
        date        : Long,
        timeHour    : Int,
        timeMinute  : Int,
        location    : String,
        userId      : String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val newEvent = Event(
                title         = title,
                description   = description,
                dateTimestamp = date,
                timeHour      = timeHour,
                timeMinute    = timeMinute,
                location      = location,
                organizerId   = userId,
                attendeesIds  = emptyList()
            )
            handleResult(repository.createEvent(newEvent))
        }
    }

    // ── updateExistingEvent: detecta cambio de fecha/hora y notifica ──────────
    fun updateExistingEvent(event: Event) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            // Busca el evento anterior para comparar fecha y hora
            val previous = _events.value.find { it.id == event.id }

            val dateChanged = previous != null && (
                    previous.dateTimestamp != event.dateTimestamp ||
                            previous.timeHour      != event.timeHour      ||
                            previous.timeMinute    != event.timeMinute
                    )

            handleResult(repository.updateEvent(event))

            // Notifica solo si cambió la fecha u hora Y hay asistentes
            if (dateChanged && event.attendeesIds.isNotEmpty()) {
                notifyScheduleChange(event)
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            handleResult(repository.deleteEvent(eventId))
        }
    }

    fun confirmAttendance(event: Event, userId: String) {
        viewModelScope.launch {
            try {
                if (!event.attendeesIds.contains(userId)) {
                    repository.updateEvent(event.copy(attendeesIds = event.attendeesIds + userId))
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al confirmar asistencia")
            }
        }
    }

    // ── Notificación local cuando cambia fecha/hora ───────────────────────────
    private fun notifyScheduleChange(event: Event) {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val newDate   = formatter.format(Date(event.dateTimestamp))
        val newTime   = "%02d:%02d".format(event.timeHour, event.timeMinute)

        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)   // reemplaza con tu propio ícono: R.drawable.ic_notification
                .setContentTitle("📅 Cambio en \"${event.title}\"")
                .setContentText("Nueva fecha: $newDate a las $newTime")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "El evento \"${event.title}\" ha cambiado su horario.\n" +
                                    "Nueva fecha: $newDate\n" +
                                    "Nueva hora: $newTime\n" +
                                    "Lugar: ${event.location}"
                        )
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            // Usa el id del evento como notificationId para evitar duplicados
            val notifId = event.id.hashCode()
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: SecurityException) {
            // El usuario no dio permiso POST_NOTIFICATIONS (Android 13+)
            // No es un crash crítico, simplemente no se muestra la notificación
        }
    }

    private fun handleResult(result: Result<Unit>) {
        _uiState.value = if (result.isSuccess) UiState.Success
        else UiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
    }
}

sealed class UiState {
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message : String) : UiState()
}
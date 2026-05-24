package com.dsm.catedra2.eventos_comunitarios.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp // Nuevo ícono para salir
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dsm.catedra2.eventos_comunitarios.model.Event
import com.dsm.catedra2.eventos_comunitarios.viewmodel.EventViewModel
import com.dsm.catedra2.eventos_comunitarios.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    viewModel: EventViewModel,
    currentUserId: String,
    currentUserRole: String,
    onLogoutClick: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos Comunitarios") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar Sesión", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            )
        },
        floatingActionButton = {
            // SOLO LOS ORGANIZADORES VEN EL BOTÓN DE CREAR
            if (currentUserRole == "organizer") {
                FloatingActionButton(onClick = {
                    eventToEdit = null
                    showDialog = true
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Crear Evento")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // ... (Mismo código de Loading y Error) ...
            when (uiState) {
                // ...
                is UiState.Success -> {
                    if (events.isEmpty()) {
                        Text("No hay eventos programados.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(events) { event ->
                                EventCard(
                                    event = event,
                                    currentUserRole = currentUserRole, // Pasamos el rol a la tarjeta
                                    onEditClick = {
                                        eventToEdit = event
                                        showDialog = true
                                    },
                                    onDeleteClick = { viewModel.deleteEvent(event.id) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        if (showDialog) {
            EventFormDialog(
                event = eventToEdit,
                onDismiss = { showDialog = false },
                onSave = { title, description, location ->
                    if (eventToEdit == null) {
                        viewModel.createNewEvent(
                            title = title,
                            description = description,
                            date = System.currentTimeMillis() + 86400000,
                            location = location,
                            userId = currentUserId
                        )
                    } else {
                        viewModel.updateExistingEvent(eventToEdit!!.copy(
                            title = title, description = description, location = location
                        ))
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    currentUserRole: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = event.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))

                // SOLO LOS ORGANIZADORES VEN LOS BOTONES DE EDITAR Y ELIMINAR
                if (currentUserRole == "organizer") {
                    Row {
                        IconButton(onClick = onEditClick) { Icon(Icons.Filled.Edit, "Editar", tint = MaterialTheme.colorScheme.primary) }
                        IconButton(onClick = onDeleteClick) { Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateString = dateFormat.format(Date(event.dateTimestamp))

            Text(text = "📅 Fecha: $dateString", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "📍 Lugar: ${event.location}", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun EventFormDialog(
    event: Event?,
    onDismiss: () -> Unit,
    onSave : (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(event?.title ?: "") }
    var description by remember { mutableStateOf(event?.description ?: "") }
    var location by remember { mutableStateOf(event?.location ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (event == null) "Crear Evento" else "Editar Evento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") }
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lugar") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, description, location) },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
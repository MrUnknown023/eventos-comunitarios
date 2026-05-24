package com.dsm.catedra2.eventos_comunitarios.view

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dsm.catedra2.eventos_comunitarios.model.Comment
import com.dsm.catedra2.eventos_comunitarios.model.Event
import com.dsm.catedra2.eventos_comunitarios.viewmodel.CommentViewModel
import com.dsm.catedra2.eventos_comunitarios.viewmodel.EventViewModel
import com.dsm.catedra2.eventos_comunitarios.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    viewModel: EventViewModel,
    commentViewModel: CommentViewModel,
    currentUserId: String,
    currentUserEmail: String,
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
            when (uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Success -> {
                    if (events.isEmpty()) {
                        Text("No hay eventos programados.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(events) { event ->
                                EventCard(
                                    event = event,
                                    currentUserRole = currentUserRole,
                                    currentUserId = currentUserId,
                                    currentUserEmail = currentUserEmail,
                                    commentViewModel = commentViewModel,
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
                is UiState.Error -> {
                    Text(
                        text = (uiState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
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
    currentUserId: String,
    currentUserEmail: String,
    commentViewModel: CommentViewModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val isFinished = event.dateTimestamp < System.currentTimeMillis()
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    
    val commentsMap by commentViewModel.comments.collectAsState()
    val eventComments = commentsMap[event.id] ?: emptyList()

    LaunchedEffect(event.id) {
        commentViewModel.loadCommentsForEvent(event.id)
    }

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = event.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))

                Row {
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "¡Mira este evento comunitario!\n\n${event.title}\n${event.description}\n📍 ${event.location}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Filled.Share, "Compartir", tint = MaterialTheme.colorScheme.primary)
                    }

                    if (currentUserRole == "organizer") {
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

            if (isFinished) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Feedback del Evento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showComments = !showComments }) {
                        Text(if (showComments) "Ocultar Comentarios" else "Ver Comentarios (${eventComments.size})")
                    }
                    if (currentUserRole != "organizer") {
                        Button(onClick = { showFeedbackDialog = true }) {
                            Text("Dejar Feedback")
                        }
                    }
                }

                if (showComments && eventComments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    eventComments.forEach { comment ->
                        CommentItem(comment)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                } else if (showComments) {
                    Text("No hay comentarios aún.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }

    if (showFeedbackDialog) {
        FeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onSend = { rating, content ->
                commentViewModel.addComment(event.id, currentUserId, currentUserEmail, rating, content)
                showFeedbackDialog = false
            }
        )
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = comment.userEmail, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (index < comment.rating) Color(0xFFFFC107) else Color.Gray
                    )
                }
            }
        }
        Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSend: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Califica el Evento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("¿Qué te pareció el evento?")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating ${index + 1}",
                                tint = if (index < rating) Color(0xFFFFC107) else Color.Gray
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Tu comentario") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSend(rating, content) }, enabled = content.isNotBlank()) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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
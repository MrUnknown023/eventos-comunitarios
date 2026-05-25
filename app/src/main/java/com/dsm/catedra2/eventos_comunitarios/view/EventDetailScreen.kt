package com.dsm.catedra2.eventos_comunitarios.view

import android.app.DatePickerDialog
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
import java.util.*

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

    var selectedTab by remember { mutableStateOf(0) }

    val filteredEvents = if (selectedTab == 0) {
        events.filter {
            it.dateTimestamp >= System.currentTimeMillis()
        }
    } else {
        events.filter {
            it.dateTimestamp < System.currentTimeMillis()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos Comunitarios") },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Salir"
                        )
                    }
                }
            )
        },

        floatingActionButton = {
            if (currentUserRole == "organizer") {
                FloatingActionButton(
                    onClick = {
                        eventToEdit = null
                        showDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            TabRow(selectedTabIndex = selectedTab) {

                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Eventos") }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Historial") }
                )
            }

            when (uiState) {

                is UiState.Loading -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Success -> {

                    if (filteredEvents.isEmpty()) {

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay eventos.")
                        }

                    } else {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            items(filteredEvents) { event ->

                                EventCard(
                                    event = event,
                                    currentUserRole = currentUserRole,
                                    currentUserId = currentUserId,
                                    currentUserEmail = currentUserEmail,
                                    commentViewModel = commentViewModel,

                                    onAttendClick = {
                                        viewModel.confirmAttendance(
                                            event,
                                            currentUserId
                                        )
                                    },

                                    onEditClick = {
                                        eventToEdit = event
                                        showDialog = true
                                    },

                                    onDeleteClick = {
                                        viewModel.deleteEvent(event.id)
                                    }
                                )
                            }
                        }
                    }
                }

                is UiState.Error -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = (uiState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> {}
            }
        }

        if (showDialog) {

            EventFormDialog(

                event = eventToEdit,

                onDismiss = {
                    showDialog = false
                },

                onSave = {
                        title,
                        description,
                        location,
                        selectedDate,
                        selectedLicense ->

                    if (eventToEdit == null) {

                        viewModel.createNewEvent(
                            title = title,
                            description = description,
                            date = selectedDate,
                            location = location,
                            userId = currentUserId,
                            license = selectedLicense
                        )

                    } else {

                        viewModel.updateExistingEvent(

                            eventToEdit!!.copy(
                                title = title,
                                description = description,
                                location = location,
                                dateTimestamp = selectedDate,
                                creativeCommonsLicense = selectedLicense
                            )
                        )
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
    onAttendClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

    val context = LocalContext.current

    val isFinished =
        event.dateTimestamp < System.currentTimeMillis()

    var showFeedbackDialog by remember {
        mutableStateOf(false)
    }

    var showComments by remember {
        mutableStateOf(false)
    }

    val commentsMap by commentViewModel.comments.collectAsState()

    val eventComments =
        commentsMap[event.id] ?: emptyList()

    LaunchedEffect(event.id) {
        commentViewModel.loadCommentsForEvent(event.id)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                Row {

                    IconButton(
                        onClick = {

                            val sendIntent = Intent().apply {

                                action = Intent.ACTION_SEND

                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Evento:\n${event.title}\n${event.description}"
                                )

                                type = "text/plain"
                            }

                            context.startActivity(
                                Intent.createChooser(sendIntent, null)
                            )
                        }
                    ) {

                        Icon(
                            Icons.Default.Share,
                            contentDescription = null
                        )
                    }

                    if (currentUserRole == "organizer") {

                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, null)
                        }

                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, null)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(event.description)

            Spacer(modifier = Modifier.height(8.dp))

            val formatter =
                SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                )

            Text(
                text = "📅 ${formatter.format(Date(event.dateTimestamp))}"
            )

            Text(
                text = "📍 ${event.location}"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "📄 Licencia: ${event.creativeCommonsLicense}"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "👥 Asistentes: ${event.attendeesIds.size}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!event.attendeesIds.contains(currentUserId)) {

                Button(onClick = onAttendClick) {
                    Text("Confirmar Asistencia")
                }

            } else {

                Text(
                    text = "✅ Ya asistirás",
                    color = Color.Green
                )
            }

            if (isFinished) {

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Comentarios",
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    TextButton(
                        onClick = {
                            showComments = !showComments
                        }
                    ) {

                        Text(
                            if (showComments)
                                "Ocultar"
                            else
                                "Ver (${eventComments.size})"
                        )
                    }

                    if (currentUserRole != "organizer") {

                        Button(
                            onClick = {
                                showFeedbackDialog = true
                            }
                        ) {
                            Text("Comentar")
                        }
                    }
                }

                if (showComments) {

                    eventComments.forEach { comment ->

                        CommentItem(comment)

                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }

    if (showFeedbackDialog) {

        FeedbackDialog(

            onDismiss = {
                showFeedbackDialog = false
            },

            onSend = { rating, content ->

                commentViewModel.addComment(
                    event.id,
                    currentUserId,
                    currentUserEmail,
                    rating,
                    content
                )

                showFeedbackDialog = false
            }
        )
    }
}

@Composable
fun EventFormDialog(
    event: Event?,
    onDismiss: () -> Unit,
    onSave: (
        String,
        String,
        String,
        Long,
        String
    ) -> Unit
) {

    val context = LocalContext.current

    var title by remember {
        mutableStateOf(event?.title ?: "")
    }

    var description by remember {
        mutableStateOf(event?.description ?: "")
    }

    var location by remember {
        mutableStateOf(event?.location ?: "")
    }

    var selectedDate by remember {
        mutableStateOf(
            event?.dateTimestamp ?: System.currentTimeMillis()
        )
    }

    var selectedLicense by remember {
        mutableStateOf(
            event?.creativeCommonsLicense ?: "CC BY"
        )
    }

    val licenses = listOf(
        "CC BY",
        "CC BY-SA",
        "CC BY-NC",
        "CC BY-ND"
    )

    val calendar = Calendar.getInstance()

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text(
                if (event == null)
                    "Crear Evento"
                else
                    "Editar Evento"
            )
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    label = { Text("Título") }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = { Text("Descripción") }
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        location = it
                    },
                    label = { Text("Lugar") }
                )

                Button(
                    onClick = {

                        DatePickerDialog(
                            context,

                            { _, year, month, day ->

                                calendar.set(
                                    year,
                                    month,
                                    day
                                )

                                selectedDate =
                                    calendar.timeInMillis
                            },

                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)

                        ).show()
                    }
                ) {

                    Text(
                        "Fecha: ${
                            SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            ).format(Date(selectedDate))
                        }"
                    )
                }

                Text("Licencia Creative Commons")

                licenses.forEach { license ->

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        RadioButton(
                            selected =
                                selectedLicense == license,

                            onClick = {
                                selectedLicense = license
                            }
                        )

                        Text(license)
                    }
                }
            }
        },

        confirmButton = {

            Button(

                onClick = {

                    onSave(
                        title,
                        description,
                        location,
                        selectedDate,
                        selectedLicense
                    )
                },

                enabled =
                    title.isNotBlank() &&
                            description.isNotBlank()

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
@Composable
fun CommentItem(comment: Comment) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = comment.userEmail,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row {

                repeat(5) { index ->

                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint =
                            if (index < comment.rating)
                                Color(0xFFFFC107)
                            else
                                Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSend: (Int, String) -> Unit
) {

    var rating by remember {
        mutableStateOf(5)
    }

    var content by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("Calificar Evento")
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text("¿Cómo estuvo el evento?")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    repeat(5) { index ->

                        IconButton(
                            onClick = {
                                rating = index + 1
                            }
                        ) {

                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint =
                                    if (index < rating)
                                        Color(0xFFFFC107)
                                    else
                                        Color.Gray
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                    },
                    label = {
                        Text("Comentario")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {

            Button(

                onClick = {
                    onSend(rating, content)
                },

                enabled = content.isNotBlank()

            ) {

                Text("Enviar")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("Cancelar")
            }
        }
    )
}
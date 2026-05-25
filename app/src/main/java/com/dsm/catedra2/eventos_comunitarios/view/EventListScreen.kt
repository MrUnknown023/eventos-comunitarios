package com.dsm.catedra2.eventos_comunitarios.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dsm.catedra2.eventos_comunitarios.model.Event
import com.dsm.catedra2.eventos_comunitarios.ui.components.cards.EventCard
import com.dsm.catedra2.eventos_comunitarios.ui.components.dialogs.EventFormDialog
import com.dsm.catedra2.eventos_comunitarios.ui.components.layout.EmptyState
import com.dsm.catedra2.eventos_comunitarios.ui.components.layout.ErrorState
import com.dsm.catedra2.eventos_comunitarios.ui.components.layout.LoadingState
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Spacing
import com.dsm.catedra2.eventos_comunitarios.viewmodel.CommentViewModel
import com.dsm.catedra2.eventos_comunitarios.viewmodel.EventViewModel
import com.dsm.catedra2.eventos_comunitarios.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel        : EventViewModel,
    commentViewModel : CommentViewModel,
    currentUserId    : String,
    currentUserEmail : String,
    currentUserRole  : String,
    onLogoutClick    : () -> Unit,
    onEventClick     : (String) -> Unit          // ← navega al detalle
) {
    val events  by viewModel.events.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showDialog  by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    val now = System.currentTimeMillis()
    val filteredEvents = if (selectedTab == 0)
        events.filter { it.dateTimestamp >= now }
    else
        events.filter { it.dateTimestamp < now }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Eventos",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Comunidad",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            Icons.Default.ExitToApp,
                            "Salir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (currentUserRole == "organizer") {
                ExtendedFloatingActionButton(
                    onClick = { eventToEdit = null; showDialog = true },
                    icon    = { Icon(Icons.Default.Add, null) },
                    text    = { Text("Nuevo evento") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(Modifier.fillMaxSize().padding(padding)) {

            TabRow(
                selectedTabIndex = selectedTab,
                divider = {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            ) {
                listOf("Próximos", "Historial").forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text     = {
                            Text(
                                label,
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (uiState) {
                is UiState.Loading -> LoadingState()

                is UiState.Success -> {
                    if (filteredEvents.isEmpty()) {
                        EmptyState(
                            icon     = if (selectedTab == 0) Icons.Outlined.Event else Icons.Outlined.History,
                            title    = if (selectedTab == 0) "Sin eventos próximos" else "Sin historial",
                            subtitle = if (selectedTab == 0)
                                "Vuelve pronto para ver nuevos eventos"
                            else
                                "Los eventos pasados aparecerán aquí"
                        )
                    } else {
                        LazyColumn(
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(Spacing.MD),
                            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                        ) {
                            items(filteredEvents) { event ->
                                EventCard(
                                    event            = event,
                                    currentUserRole  = currentUserRole,
                                    currentUserId    = currentUserId,
                                    currentUserEmail = currentUserEmail,
                                    commentViewModel = commentViewModel,
                                    onAttendClick    = {
                                        viewModel.confirmAttendance(event, currentUserId)
                                    },
                                    onEditClick      = { eventToEdit = event; showDialog = true },
                                    onDeleteClick    = { viewModel.deleteEvent(event.id) },
                                    onCardClick      = { onEventClick(event.id) }  // ← nuevo
                                )
                            }
                        }
                    }
                }

                is UiState.Error -> ErrorState((uiState as UiState.Error).message)

                else -> {}
            }
        }

        if (showDialog) {
            EventFormDialog(
                event         = eventToEdit,
                existingDates = events.map { it.dateTimestamp },
                onDismiss     = { showDialog = false },
                onSave        = { title, description, location, date, timeHour, timeMinute ->
                    if (eventToEdit == null) {
                        viewModel.createNewEvent(
                            title       = title,
                            description = description,
                            date        = date,
                            timeHour    = timeHour,
                            timeMinute  = timeMinute,
                            location    = location,
                            userId      = currentUserId
                        )
                    } else {
                        viewModel.updateExistingEvent(
                            eventToEdit!!.copy(
                                title         = title,
                                description   = description,
                                location      = location,
                                dateTimestamp = date,
                                timeHour      = timeHour,
                                timeMinute    = timeMinute
                            )
                        )
                    }
                    showDialog = false
                }
            )
        }
    }
}
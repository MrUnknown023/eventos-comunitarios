package com.dsm.catedra2.eventos_comunitarios.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dsm.catedra2.eventos_comunitarios.ui.components.atoms.MetaChip
import com.dsm.catedra2.eventos_comunitarios.ui.components.atoms.SectionLabel
import com.dsm.catedra2.eventos_comunitarios.ui.components.cards.CommentItem
import com.dsm.catedra2.eventos_comunitarios.ui.components.dialogs.FeedbackDialog
import com.dsm.catedra2.eventos_comunitarios.ui.theme.AppColors
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Radius
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Spacing
import com.dsm.catedra2.eventos_comunitarios.viewmodel.CommentViewModel
import com.dsm.catedra2.eventos_comunitarios.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId          : String,
    eventViewModel   : EventViewModel,
    commentViewModel : CommentViewModel,
    currentUserId    : String,
    currentUserEmail : String,
    onBack           : () -> Unit
) {
    val events      by eventViewModel.events.collectAsState()
    val commentsMap by commentViewModel.comments.collectAsState()

    val event         = events.find { it.id == eventId } ?: return
    val eventComments = commentsMap[eventId] ?: emptyList()

    val isFinished  = event.dateTimestamp < System.currentTimeMillis()
    val isAttending = event.attendeesIds.contains(currentUserId)

    val canComment = isAttending && isFinished // pueden comentar si confirmaron y si el evento finalizo

    LaunchedEffect(eventId) {
        commentViewModel.loadCommentsForEvent(eventId)
    }

    val listState = rememberLazyListState()
    val formatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    var showFeedbackDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        event.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // FAB visible siempre que el usuario haya confirmado asistencia
        floatingActionButton = {
            if (canComment) {
                ExtendedFloatingActionButton(
                    onClick = { showFeedbackDialog = true },
                    icon    = { Icon(Icons.Default.RateReview, null) },
                    text    = { Text("Comentar") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        LazyColumn(
            state          = listState,
            modifier       = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp) // espacio para el FAB
        ) {

            // ── Barra de color superior ──────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            if (isFinished)
                                Brush.horizontalGradient(listOf(
                                    MaterialTheme.colorScheme.outlineVariant,
                                    MaterialTheme.colorScheme.outlineVariant
                                ))
                            else
                                Brush.horizontalGradient(listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                ))
                        )
                )
            }

            // ── Info principal ───────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.MD, vertical = Spacing.MD)
                ) {
                    if (isFinished) {
                        SectionLabel("Finalizado")
                        Spacer(Modifier.height(Spacing.XS))
                    }

                    Text(
                        text  = event.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(Modifier.height(Spacing.SM))

                    Text(
                        text  = event.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(Spacing.MD))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                        verticalArrangement   = Arrangement.spacedBy(Spacing.XS)
                    ) {
                        MetaChip(Icons.Outlined.CalendarMonth, formatter.format(Date(event.dateTimestamp)))
                        MetaChip(Icons.Outlined.LocationOn, event.location)
                        MetaChip(Icons.Outlined.Group, "${event.attendeesIds.size} asistentes")
                        MetaChip(Icons.Outlined.Schedule, "%02d:%02d".format(event.timeHour, event.timeMinute))
                        MetaChip(Icons.Outlined.Policy, event.creativeCommonsLicense)
                    }

                    // Botón confirmar asistencia (solo si aún no confirmó y el evento no ha pasado)
                    if (!isFinished && !isAttending) {
                        Spacer(Modifier.height(Spacing.MD))
                        Button(
                            onClick  = { eventViewModel.confirmAttendance(event, currentUserId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(Radius.SM)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(Spacing.XS))
                            Text("Confirmar asistencia")
                        }
                    }

                    // Badge "ya confirmaste"
                    if (isAttending) {
                        Spacer(Modifier.height(Spacing.MD))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.SM))
                                .background(AppColors.AttendGreen.copy(alpha = 0.12f))
                                .padding(horizontal = Spacing.MD, vertical = Spacing.SM),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint     = AppColors.AttendGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(Spacing.XS))
                            Text(
                                "Asistencia confirmada",
                                color = AppColors.AttendGreen,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            // ── Divider ──────────────────────────────────────────────────────
            item {
                HorizontalDivider(
                    modifier  = Modifier.padding(horizontal = Spacing.MD),
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant
                )
            }

            // ── Cabecera sección comentarios ─────────────────────────────────
            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.MD, vertical = Spacing.SM),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    SectionLabel("Comentarios (${eventComments.size})")

                    // Botón inline también, para que sea obvio
                    if (canComment) {
                        FilledTonalButton(
                            onClick        = { showFeedbackDialog = true },
                            contentPadding = PaddingValues(horizontal = Spacing.MD, vertical = Spacing.XS),
                            shape          = RoundedCornerShape(Radius.SM)
                        ) {
                            Icon(Icons.Default.RateReview, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(Spacing.XS))
                            Text("Agregar", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // ── Banner para usuarios sin asistencia confirmada ───────────────
            if (!isAttending && isFinished) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.MD, vertical = Spacing.XS)
                            .clip(RoundedCornerShape(Radius.SM))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = Spacing.MD, vertical = Spacing.SM),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(Spacing.XS))
                        Text(
                            "No participaste en el evento para poder comentar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Lista de comentarios ─────────────────────────────────────────
            if (eventComments.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.XL),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.ChatBubbleOutline,
                                null,
                                tint     = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(Spacing.SM))
                            Text(
                                "Aún no hay comentarios",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (canComment) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "¡Sé el primero en comentar!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                items(eventComments) { comment ->
                    CommentItem(
                        comment  = comment,
                        modifier = Modifier.padding(horizontal = Spacing.MD, vertical = Spacing.XS)
                    )
                }
            }
        }
    }

    // ── FeedbackDialog — se abre desde el FAB o desde el botón "Agregar" ────
    if (showFeedbackDialog) {
        FeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onSend    = { rating, content ->
                commentViewModel.addComment(
                    eventId   = eventId,
                    userId    = currentUserId,
                    userEmail = currentUserEmail,
                    rating    = rating,
                    content   = content
                )
                showFeedbackDialog = false
            }
        )
    }
}
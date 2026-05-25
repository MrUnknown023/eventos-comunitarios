package com.dsm.catedra2.eventos_comunitarios.ui.components.cards

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dsm.catedra2.eventos_comunitarios.model.Event
import com.dsm.catedra2.eventos_comunitarios.ui.components.atoms.MetaChip
import com.dsm.catedra2.eventos_comunitarios.ui.components.atoms.SectionLabel
import com.dsm.catedra2.eventos_comunitarios.ui.theme.AppColors
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Radius
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Spacing
import com.dsm.catedra2.eventos_comunitarios.viewmodel.CommentViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventCard(
    event            : Event,
    currentUserRole  : String,
    currentUserId    : String,
    currentUserEmail : String,
    commentViewModel : CommentViewModel,
    onAttendClick    : () -> Unit,
    onEditClick      : () -> Unit,
    onDeleteClick    : () -> Unit,
    onCardClick      : () -> Unit           // ← nuevo: abre el detalle
) {
    val context     = LocalContext.current
    val isFinished  = event.dateTimestamp < System.currentTimeMillis()
    val isAttending = event.attendeesIds.contains(currentUserId)

    // Carga de comentarios solo para mostrar el contador en la card
    val commentsMap   by commentViewModel.comments.collectAsState()
    val commentCount   = (commentsMap[event.id] ?: emptyList()).size

    LaunchedEffect(event.id) {
        commentViewModel.loadCommentsForEvent(event.id)
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },          // ← toca la card para ver detalle
        shape     = RoundedCornerShape(Radius.MD),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {

            // ── Barra superior de color ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        if (isFinished)
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.outlineVariant,
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        else
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                    )
            )

            Column(modifier = Modifier.padding(Spacing.MD)) {

                // ── Header ──
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (isFinished) {
                            SectionLabel("Finalizado")
                            Spacer(Modifier.height(Spacing.XS))
                        }
                        Text(
                            text     = event.title,
                            style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    EventCardActions(
                        event           = event,
                        currentUserRole = currentUserRole,
                        onEditClick     = onEditClick,
                        onDeleteRequest = { showDeleteConfirm = true },
                        onShareClick    = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Evento:\n${event.title}\n${event.description}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        }
                    )
                }

                Spacer(Modifier.height(Spacing.SM))

                Text(
                    text     = event.description,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(Spacing.MD))

                // ── Meta chips ──
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

                Spacer(Modifier.height(Spacing.MD))

                // ── Asistencia ──
                if (!isFinished) {
                    AttendanceSection(
                        isAttending   = isAttending,
                        onAttendClick = onAttendClick
                    )
                }

                // ── Footer: hint de comentarios + flecha ──
                Spacer(Modifier.height(Spacing.SM))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(Modifier.height(Spacing.XS))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text  = if (commentCount > 0) "$commentCount comentarios" else "Ver detalles",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        "Ver detalle",
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            eventTitle = event.title,
            onConfirm  = { onDeleteClick(); showDeleteConfirm = false },
            onDismiss  = { showDeleteConfirm = false }
        )
    }
}

// ── Sub-componentes ───────────────────────────────────────────────────────────

@Composable
private fun EventCardActions(
    event           : Event,
    currentUserRole : String,
    onShareClick    : () -> Unit,
    onEditClick     : () -> Unit,
    onDeleteRequest : () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onShareClick, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Share,
                contentDescription = "Compartir",
                modifier = Modifier.size(18.dp),
                tint     = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (currentUserRole == "organizer") {
            IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    modifier = Modifier.size(18.dp),
                    tint     = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDeleteRequest, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    modifier = Modifier.size(18.dp),
                    tint     = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AttendanceSection(
    isAttending   : Boolean,
    onAttendClick : () -> Unit
) {
    if (!isAttending) {
        Button(
            onClick  = onAttendClick,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(Radius.SM)
        ) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.XS))
            Text("Confirmar asistencia")
        }
    } else {
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
                text  = "Asistencia confirmada",
                color = AppColors.AttendGreen,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    eventTitle : String,
    onConfirm  : () -> Unit,
    onDismiss  : () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon  = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Eliminar evento") },
        text  = {
            Text("¿Estás seguro de que quieres eliminar \"$eventTitle\"? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Eliminar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
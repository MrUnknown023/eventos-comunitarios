package com.dsm.catedra2.eventos_comunitarios.ui.components.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dsm.catedra2.eventos_comunitarios.model.Event
import com.dsm.catedra2.eventos_comunitarios.ui.components.atoms.SectionLabel
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Radius
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventFormDialog(
    event            : Event?,
    existingDates    : List<Long>,          // timestamps de eventos existentes para validar
    onDismiss        : () -> Unit,
    onSave           : (
        title      : String,
        description: String,
        location   : String,
        date       : Long,
        timeHour   : Int,
        timeMinute : Int
    ) -> Unit
) {
    val context = LocalContext.current

    var title           by remember { mutableStateOf(event?.title ?: "") }
    var description     by remember { mutableStateOf(event?.description ?: "") }
    var location        by remember { mutableStateOf(event?.location ?: "") }
    var selectedDate    by remember { mutableStateOf(event?.dateTimestamp ?: System.currentTimeMillis()) }
    var selectedHour    by remember { mutableStateOf(event?.timeHour ?: 9) }
    var selectedMinute  by remember { mutableStateOf(event?.timeMinute ?: 0) }

    val calendar  = Calendar.getInstance()
    val dateFmt   = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // ── Validaciones ──────────────────────────────────
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val isPastDate = selectedDate < today

    val isDuplicateDay = existingDates.any { existing ->
        if (event != null) {
            // al editar, ignorar la fecha del propio evento
            if (existing == event.dateTimestamp) return@any false
        }
        val a = Calendar.getInstance().apply { timeInMillis = existing }
        val b = Calendar.getInstance().apply { timeInMillis = selectedDate }
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR) &&
                a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
    }

    val isFormValid = title.isNotBlank()       &&
            description.isNotBlank() &&
            location.isNotBlank()    &&
            !isPastDate              &&
            !isDuplicateDay

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(Radius.LG),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = if (event == null) Icons.Default.Add else Icons.Default.Edit,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(Spacing.SM))
                Text(
                    text  = if (event == null) "Crear evento" else "Editar evento",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.MD)) {

                // ── Título ──
                item {
                    OutlinedTextField(
                        value         = title,
                        onValueChange = { title = it },
                        label         = { Text("Título") },
                        leadingIcon   = { Icon(Icons.Outlined.Title, null, modifier = Modifier.size(20.dp)) },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(Radius.SM)
                    )
                }

                // ── Descripción ──
                item {
                    OutlinedTextField(
                        value         = description,
                        onValueChange = { description = it },
                        label         = { Text("Descripción") },
                        leadingIcon   = { Icon(Icons.Outlined.Description, null, modifier = Modifier.size(20.dp)) },
                        modifier      = Modifier.fillMaxWidth(),
                        minLines      = 3,
                        maxLines      = 5,
                        shape         = RoundedCornerShape(Radius.SM)
                    )
                }

                // ── Lugar ──
                item {
                    OutlinedTextField(
                        value         = location,
                        onValueChange = { location = it },
                        label         = { Text("Lugar") },
                        leadingIcon   = { Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(20.dp)) },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(Radius.SM)
                    )
                }

                // ── Fecha ──
                item {
                    SectionLabel("Fecha y hora")
                    Spacer(Modifier.height(Spacing.XS))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.SM)
                    ) {
                        // Botón fecha
                        OutlinedButton(
                            onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(year, month, day)
                                        selectedDate = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(Radius.SM)
                        ) {
                            Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(Spacing.XS))
                            Text(
                                text  = dateFmt.format(Date(selectedDate)),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        // Botón hora
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        selectedHour   = hour
                                        selectedMinute = minute
                                    },
                                    selectedHour,
                                    selectedMinute,
                                    true   // formato 24h
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(Radius.SM)
                        ) {
                            Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(Spacing.XS))
                            Text(
                                text  = "%02d:%02d".format(selectedHour, selectedMinute),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                // ── Errores de validación ──
                item {
                    AnimatedVisibility(
                        visible = isPastDate || isDuplicateDay,
                        enter   = expandVertically() + fadeIn(),
                        exit    = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(Radius.SM),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier          = Modifier.padding(Spacing.SM),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector        = Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.error,
                                    modifier           = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(Spacing.XS))
                                Text(
                                    text  = when {
                                        isPastDate    -> "No puedes crear eventos en fechas pasadas"
                                        isDuplicateDay -> "Ya existe un evento en esa fecha"
                                        else          -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = {
                    onSave(title, description, location, selectedDate, selectedHour, selectedMinute)
                },
                enabled  = isFormValid,
                shape    = RoundedCornerShape(Radius.SM),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.XS))
                Text("Guardar evento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    )
}
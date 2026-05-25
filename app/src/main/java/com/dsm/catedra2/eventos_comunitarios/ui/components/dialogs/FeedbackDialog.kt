package com.dsm.catedra2.eventos_comunitarios.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.dsm.catedra2.eventos_comunitarios.ui.components.atoms.InteractiveStarRating
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Radius
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Spacing

@Composable
fun FeedbackDialog(
    onDismiss : () -> Unit,
    onSend    : (Int, String) -> Unit
) {
    var rating  by remember { mutableStateOf(5) }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(Radius.LG),
        icon  = { Icon(Icons.Default.RateReview, null, tint = MaterialTheme.colorScheme.primary) },
        title = {
            Text(
                "Calificar evento",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.MD)) {
                Text(
                    "¿Cómo estuvo el evento?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                InteractiveStarRating(
                    rating         = rating,
                    onRatingChange = { rating = it }
                )

                OutlinedTextField(
                    value         = content,
                    onValueChange = { content = it },
                    label         = { Text("Escribe tu comentario") },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 3,
                    maxLines      = 5,
                    shape         = RoundedCornerShape(Radius.SM)
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { onSend(rating, content) },
                enabled  = content.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(Radius.SM)
            ) {
                Icon(Icons.Default.Send, null, modifier = Modifier.size(Spacing.MD))
                Spacer(Modifier.width(Spacing.XS))
                Text("Enviar calificación")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    )
}
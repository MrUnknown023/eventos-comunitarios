package com.dsm.catedra2.eventos_comunitarios.ui.components.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dsm.catedra2.eventos_comunitarios.model.Comment
import com.dsm.catedra2.eventos_comunitarios.ui.components.atoms.StarRatingRow
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Radius
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Spacing

@Composable
fun CommentItem(
    comment  : Comment,
    modifier : Modifier = Modifier   // ← agregado con default vacío, no rompe usos anteriores
) {
    Surface(
        shape    = RoundedCornerShape(Radius.SM),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()   // ← aplica el modifier externo
    ) {
        Column(modifier = Modifier.padding(Spacing.SM)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar con inicial
                    Surface(
                        shape    = CircleShape,
                        color    = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text  = comment.userEmail.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(Modifier.width(Spacing.XS))

                    Text(
                        text     = comment.userEmail,
                        style    = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 160.dp)
                    )
                }

                StarRatingRow(rating = comment.rating)
            }

            Spacer(Modifier.height(Spacing.XS))

            Text(
                text  = comment.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
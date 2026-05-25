package com.dsm.catedra2.eventos_comunitarios.ui.components.atoms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Radius
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Spacing

@Composable
fun MetaChip(
    icon     : ImageVector,
    label    : String,
    modifier : Modifier = Modifier
) {
    Surface(
        shape    = RoundedCornerShape(Radius.SM),
        color    = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(horizontal = Spacing.SM, vertical = Spacing.XS)
        ) {
            Icon(
                imageVector    = icon,
                contentDescription = null,
                modifier       = Modifier.size(14.dp),
                tint           = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(Spacing.XS))
            Text(
                text     = label,
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
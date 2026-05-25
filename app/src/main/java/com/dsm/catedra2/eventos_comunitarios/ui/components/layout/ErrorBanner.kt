package com.dsm.catedra2.eventos_comunitarios.ui.components.layout

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Radius
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Spacing

@Composable
fun ErrorBanner(
    message  : String,
    modifier : Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message.isNotBlank(),
        enter   = expandVertically() + fadeIn(),
        exit    = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(Radius.SM),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = Spacing.MD, vertical = Spacing.SM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector        = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(Spacing.SM))
                Text(
                    text  = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
package com.dsm.catedra2.eventos_comunitarios.ui.components.atoms

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Spacing

@Composable
fun DividerWithText(text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = modifier
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text     = text,
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.SM)
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}
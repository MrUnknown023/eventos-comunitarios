package com.dsm.catedra2.eventos_comunitarios.ui.components.atoms

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dsm.catedra2.eventos_comunitarios.ui.theme.AppColors

@Composable
fun StarRatingRow(
    rating   : Int,
    starSize : Int      = 14,
    modifier : Modifier = Modifier
) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            Icon(
                imageVector        = if (index < rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                modifier           = Modifier.size(starSize.dp),
                tint               = if (index < rating) AppColors.StarActive else AppColors.StarInactive
            )
        }
    }
}
package com.dsm.catedra2.eventos_comunitarios.ui.components.atoms

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dsm.catedra2.eventos_comunitarios.ui.theme.AppColors
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Spacing

private val ratingLabels = listOf("Pésimo", "Malo", "Regular", "Bueno", "Excelente")

@Composable
fun InteractiveStarRating(
    rating    : Int,
    onRatingChange : (Int) -> Unit,
    modifier  : Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = modifier.fillMaxWidth()
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            repeat(5) { index ->
                val scale by animateFloatAsState(
                    targetValue = if (index < rating) 1.2f else 1f,
                    label       = "star_scale_$index"
                )
                IconButton(
                    onClick  = { onRatingChange(index + 1) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector        = if (index < rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = "${index + 1} estrellas",
                        tint               = if (index < rating) AppColors.StarActive else AppColors.StarInactive,
                        modifier           = Modifier.size((24 * scale).dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.XS))

        AnimatedContent(targetState = rating, label = "rating_label") { r ->
            Text(
                text  = ratingLabels.getOrElse(r - 1) { "" },
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = AppColors.StarActive
            )
        }
    }
}
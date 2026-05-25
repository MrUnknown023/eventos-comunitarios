package com.dsm.catedra2.eventos_comunitarios.ui.components.atoms

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Radius

@Composable
fun PasswordTextField(
    value         : String,
    onValueChange : (String) -> Unit,
    label         : String    = "Contraseña",
    modifier      : Modifier  = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value             = value,
        onValueChange     = onValueChange,
        label             = { Text(label) },
        modifier          = modifier,
        singleLine        = true,
        shape             = RoundedCornerShape(Radius.SM),
        leadingIcon       = {
            Icon(Icons.Outlined.Lock, contentDescription = null)
        },
        visualTransformation = if (visible)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector        = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        }
    )
}
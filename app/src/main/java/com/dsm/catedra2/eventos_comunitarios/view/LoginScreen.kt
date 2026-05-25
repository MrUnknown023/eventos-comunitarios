package com.dsm.catedra2.eventos_comunitarios.view

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dsm.catedra2.eventos_comunitarios.R
import com.dsm.catedra2.eventos_comunitarios.ui.components.atoms.DividerWithText
import com.dsm.catedra2.eventos_comunitarios.ui.components.atoms.PasswordTextField
import com.dsm.catedra2.eventos_comunitarios.ui.components.atoms.SectionLabel
import com.dsm.catedra2.eventos_comunitarios.ui.components.layout.ErrorBanner
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Radius
import com.dsm.catedra2.eventos_comunitarios.ui.theme.Spacing
import com.dsm.catedra2.eventos_comunitarios.viewmodel.AuthState
import com.dsm.catedra2.eventos_comunitarios.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    viewModel      : AuthViewModel,
    onLoginSuccess : () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val context   = LocalContext.current

    var email       by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var isOrganizer by remember { mutableStateOf(false) }

    val webClientId = stringResource(R.string.default_web_client_id)

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { viewModel.loginWithGoogle(it) }
            } catch (_: ApiException) {}
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) onLoginSuccess()
    }

    val isLoading = authState is AuthState.Loading
    val errorMsg  = (authState as? AuthState.Error)?.message ?: ""
    val isFormValid = email.isNotBlank() && password.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Acento visual superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.LG),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(Modifier.height(Spacing.XL))

            // ── Logo / Ícono ──
            Surface(
                shape    = CircleShape,
                color    = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Outlined.Group,
                        contentDescription = null,
                        modifier           = Modifier.size(36.dp),
                        tint               = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(Spacing.MD))

            Text(
                text  = "Eventos Comunitarios",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text  = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(Spacing.XL))

            // ── Formulario ──
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(Radius.MD),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier            = Modifier.padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                ) {

                    SectionLabel("Acceso")

                    Spacer(Modifier.height(Spacing.XS))

                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        label         = { Text("Correo electrónico") },
                        leadingIcon   = { Icon(Icons.Outlined.Email, null) },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(Radius.SM)
                    )

                    PasswordTextField(
                        value         = password,
                        onValueChange = { password = it },
                        modifier      = Modifier.fillMaxWidth()
                    )

                    // ── Selector de rol ──
                    Surface(
                        shape    = RoundedCornerShape(Radius.SM),
                        color    = if (isOrganizer)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.SM, vertical = Spacing.XS),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked         = isOrganizer,
                                onCheckedChange = { isOrganizer = it }
                            )
                            Spacer(Modifier.width(Spacing.XS))
                            Column {
                                Text(
                                    text       = "Registrarme como Organizador",
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isOrganizer) FontWeight.SemiBold else FontWeight.Normal,
                                    color      = if (isOrganizer)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AnimatedVisibility(visible = isOrganizer) {
                                    Text(
                                        text  = "Podrás crear y gestionar eventos",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.MD))

            // ── Error ──
            ErrorBanner(
                message  = errorMsg,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Spacing.MD))

            // ── Acciones ──
            AnimatedContent(
                targetState = isLoading,
                label       = "login_actions"
            ) { loading ->
                if (loading) {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.SM)) {

                        Button(
                            onClick  = { viewModel.login(email, password) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled  = isFormValid,
                            shape    = RoundedCornerShape(Radius.SM)
                        ) {
                            Text(
                                "Iniciar sesión",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        OutlinedButton(
                            onClick  = {
                                val role = if (isOrganizer) "organizer" else "user"
                                viewModel.register(email, password, role)
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled  = isFormValid,
                            shape    = RoundedCornerShape(Radius.SM)
                        ) {
                            Text(
                                "Crear cuenta",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        DividerWithText(
                            text     = "o continúa con",
                            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.XS)
                        )

                        GoogleSignInButton(
                            onClick  = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.XL))
        }
    }
}

// ── Botón de Google ────────────────────────────────────

@Composable
private fun GoogleSignInButton(
    onClick  : () -> Unit,
    modifier : Modifier = Modifier
) {
    ElevatedButton(
        onClick  = onClick,
        modifier = modifier,
        shape    = RoundedCornerShape(Radius.SM),
        colors   = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        GoogleLogo(modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Spacing.SM))
        Text(
            text  = "Continuar con Google",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun GoogleLogo(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Letras G con los colores de Google usando Canvas
        Canvas(modifier = Modifier.size(14.dp)) {
            val colors = listOf(
                Color(0xFF4285F4), // azul
                Color(0xFF34A853), // verde
                Color(0xFFFBBC05), // amarillo
                Color(0xFFEA4335)  // rojo
            )
            val r = size.minDimension / 2f
            colors.forEachIndexed { i, color ->
                drawArc(
                    color      = color,
                    startAngle = i * 90f,
                    sweepAngle = 88f,
                    useCenter  = false,
                    style      = Stroke(width = r * 0.45f)
                )
            }
        }
    }
}
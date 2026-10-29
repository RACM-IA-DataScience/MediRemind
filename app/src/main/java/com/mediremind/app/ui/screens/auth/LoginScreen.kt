package com.mediremind.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediremind.app.R
import com.mediremind.app.ui.theme.TealPrimary
import com.mediremind.app.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()

    var showMockDialog by remember { mutableStateOf(false) }
    var mockName by remember { mutableStateOf("Roberto González") }
    var mockEmail by remember { mutableStateOf("roberto.paciente@gmail.com") }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onLoginSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logotipo oficial de la aplicación
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "MediRemind Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp))
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "MediRemind",
                style = MaterialTheme.typography.headlineLarge,
                color = TealPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tu asistente inteligente de salud y recordatorio de medicamentos",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Beneficios clave
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeatureRow(text = "Alarmas y recordatorios precisos para cada dosis")
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureRow(text = "Base de datos médica de compuestos y presentaciones")
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureRow(text = "Alertas geolocalizadas y radar de farmacias con GPS")
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = TealPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Conectando con Google...", style = MaterialTheme.typography.bodySmall)
            } else {
                // Botón principal: Iniciar Sesión con Google
                Button(
                    onClick = {
                        val clientId = context.getString(R.string.default_web_client_id)
                        authViewModel.signInWithGoogle(clientId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Iniciar Sesión con Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón secundario: Prueba rápida con Google simulado (para Android Studio / Emulador)
                OutlinedButton(
                    onClick = { showMockDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Probar con Cuenta Google de Ejemplo")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Modo Invitado
                TextButton(
                    onClick = { authViewModel.signInAsGuest() }
                ) {
                    Text(
                        text = "Continuar como Invitado (Modo Offline)",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Mostrar error si ocurre
            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Diálogo para personalizar cuenta de prueba rápida
    if (showMockDialog) {
        AlertDialog(
            onDismissRequest = { showMockDialog = false },
            title = { Text(text = "Probar Inicio con Google") },
            text = {
                Column {
                    Text(
                        text = "Introduce los datos que deseas simular para tu sesión de Google:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = mockName,
                        onValueChange = { mockName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mockEmail,
                        onValueChange = { mockEmail = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.signInMockGoogle(mockName, mockEmail)
                        showMockDialog = false
                    }
                ) {
                    Text("Ingresar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMockDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = TealPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

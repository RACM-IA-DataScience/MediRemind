package com.mediremind.app.ui.screens.places

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mediremind.app.data.local.entities.SavedPlaceEntity
import com.mediremind.app.ui.theme.TealPrimary
import com.mediremind.app.ui.viewmodels.LocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(
    locationViewModel: LocationViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by locationViewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newPlaceName by remember { mutableStateOf("Mi Casa") }
    var notifyOnEntry by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Geovallas y Lugares", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(imageVector = Icons.Default.AddLocation, contentDescription = "Añadir lugar", tint = TealPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TealPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Las alertas por geovalla te recuerdan tomar medicamentos pendientes automáticamente cuando llegas a Casa o a tu Trabajo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tus Zonas Configuradas:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.savedPlaces) { place ->
                    PlaceCardItem(
                        place = place,
                        onToggle = { isChecked ->
                            locationViewModel.savePlace(place.copy(isEnabled = isChecked))
                        },
                        onDelete = {
                            locationViewModel.deletePlace(place)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Registrar Nueva Zona") },
            text = {
                Column {
                    Text(
                        "Se registrará tu posición GPS actual para activar recordatorios al llegar:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPlaceName,
                        onValueChange = { newPlaceName = it },
                        label = { Text("Nombre del lugar (ej. Casa, Oficina)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Alertar al llegar", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = notifyOnEntry,
                            onCheckedChange = { notifyOnEntry = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentLoc = uiState.userLocation
                        val lat = currentLoc?.latitude ?: 19.432608
                        val lon = currentLoc?.longitude ?: -99.133209

                        locationViewModel.savePlace(
                            SavedPlaceEntity(
                                name = newPlaceName,
                                address = "Coordenadas fijadas por GPS",
                                latitude = lat,
                                longitude = lon,
                                radiusMeters = 150f,
                                notifyOnEntry = notifyOnEntry,
                                notifyOnExit = !notifyOnEntry,
                                isEnabled = true
                            )
                        )
                        showAddDialog = false
                    }
                ) {
                    Text("Guardar Zona")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PlaceCardItem(
    place: SavedPlaceEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Radio: ${place.radiusMeters.toInt()}m • ${if (place.notifyOnEntry) "Al llegar" else "Al salir"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = place.isEnabled,
                    onCheckedChange = onToggle
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

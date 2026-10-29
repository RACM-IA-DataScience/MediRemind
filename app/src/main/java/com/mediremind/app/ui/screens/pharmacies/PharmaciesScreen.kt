package com.mediremind.app.ui.screens.pharmacies

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mediremind.app.location.PharmacyItem
import com.mediremind.app.ui.theme.TealPrimary
import com.mediremind.app.ui.viewmodels.LocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmaciesScreen(
    locationViewModel: LocationViewModel,
    onNavigateToPlaces: () -> Unit
) {
    val context = LocalContext.current
    val uiState by locationViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        locationViewModel.loadUserLocationAndPharmacies()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farmacias Cercanas", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToPlaces) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Configurar Geovallas")
                    }
                    IconButton(onClick = { locationViewModel.loadUserLocationAndPharmacies() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar GPS")
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
            // Tarjeta de estado de Ubicación y Radar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TealPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Radar de Farmacias con GPS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (uiState.userLocation != null)
                            "Ubicación fijada: Lat ${String.format(java.util.Locale.US, "%.4f", uiState.userLocation!!.latitude)}, Lon ${String.format(java.util.Locale.US, "%.4f", uiState.userLocation!!.longitude)}"
                        else "Buscando coordenadas GPS...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { locationViewModel.openInGoogleMaps() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = TealPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abrir Búsqueda en Google Maps", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Establecimientos en tu radio:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.pharmacies.size} encontradas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.pharmacies) { pharmacy ->
                        PharmacyCardItem(
                            pharmacy = pharmacy,
                            onNavigate = {
                                val uri = Uri.parse("google.navigation:q=${pharmacy.latitude},${pharmacy.longitude}")
                                val navIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (navIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(navIntent)
                                } else {
                                    val mapUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${pharmacy.latitude},${pharmacy.longitude}")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, mapUri))
                                }
                            },
                            onCall = {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${pharmacy.phone}"))
                                context.startActivity(dialIntent)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PharmacyCardItem(
    pharmacy: PharmacyItem,
    onNavigate: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pharmacy.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = pharmacy.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                if (pharmacy.isOpen24Hours) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TealPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "24 Horas",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cómo Llegar")
                }

                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Llamar")
                }
            }
        }
    }
}

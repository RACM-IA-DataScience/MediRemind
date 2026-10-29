package com.mediremind.app.ui.screens.medications

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
import android.widget.Toast
import com.mediremind.app.data.local.entities.MedicationEntity
import com.mediremind.app.data.repository.OnlineDrugItem
import com.mediremind.app.ui.theme.TealPrimary
import com.mediremind.app.ui.viewmodels.MedicationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugSearchScreen(
    medicationViewModel: MedicationViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by medicationViewModel.uiState.collectAsState()
    var queryText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo Oficial OpenFDA", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
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
            // Buscador OpenFDA
            OutlinedTextField(
                value = queryText,
                onValueChange = {
                    queryText = it
                    medicationViewModel.onSearchQueryChanged(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar ej. Aspirin, Ibuprofen, Metformin...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (queryText.isNotEmpty()) {
                        IconButton(onClick = {
                            queryText = ""
                            medicationViewModel.onSearchQueryChanged("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Base de datos internacional de medicamentos (FDA/NDC). Consulta principios activos, concentraciones y presentaciones certificadas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isSearchingOnline) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            } else if (uiState.onlineResults.isEmpty() && queryText.isNotBlank()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No se encontraron resultados para '$queryText'. Prueba con el nombre genérico en inglés o español.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.onlineResults) { drug ->
                        OnlineDrugCard(
                            drug = drug,
                            onImport = {
                                val newMed = MedicationEntity(
                                    name = drug.brandName,
                                    activeCompound = drug.genericName,
                                    presentation = drug.dosageForm,
                                    concentration = drug.activeIngredients,
                                    administrationRoute = drug.route,
                                    instructions = "Importado desde el catálogo oficial FDA (NDC: ${drug.productNdc})",
                                    currentStock = 30,
                                    unit = "piezas",
                                    isCustom = false
                                )
                                medicationViewModel.saveMedication(newMed) {
                                    Toast.makeText(context, "✅ Añadido a tu botiquín: ${drug.brandName}", Toast.LENGTH_SHORT).show()
                                }
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
private fun OnlineDrugCard(drug: OnlineDrugItem, onImport: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = drug.brandName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TealPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "FDA NDC",
                        style = MaterialTheme.typography.labelSmall,
                        color = TealPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Compuesto activo: ${drug.genericName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TealPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Presentación: ${drug.dosageForm} • Vía: ${drug.route}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (drug.activeIngredients.isNotBlank()) {
                Text(
                    text = "Ingredientes activos: ${drug.activeIngredients}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onImport,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Añadir a mi Botiquín")
            }
        }
    }
}

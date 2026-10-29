package com.mediremind.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediremind.app.data.local.entities.DoseHistoryEntity
import com.mediremind.app.ui.theme.PillPending
import com.mediremind.app.ui.theme.PillSkipped
import com.mediremind.app.ui.theme.PillTaken
import com.mediremind.app.ui.theme.TealPrimary
import com.mediremind.app.ui.viewmodels.ReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    reminderViewModel: ReminderViewModel
) {
    val uiState by reminderViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Tomas y Adherencia", fontWeight = FontWeight.Bold) },
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
            // Resumen de estadísticas
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatMetricItem(
                        label = "Tomadas",
                        count = "${uiState.stats.takenCount}",
                        color = PillTaken
                    )
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                    )
                    StatMetricItem(
                        label = "Omitidas",
                        count = "${uiState.stats.skippedCount}",
                        color = PillSkipped
                    )
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                    )
                    StatMetricItem(
                        label = "Efectividad",
                        count = "${uiState.stats.percentage}%",
                        color = TealPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registro Detallado de Dosis:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.fullHistory.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aún no hay registros de tomas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.fullHistory) { history ->
                        HistoryRowItem(history = history)
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
private fun StatMetricItem(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun HistoryRowItem(history: DoseHistoryEntity) {
    val (badgeText, badgeColor, icon) = when (history.status) {
        "TAKEN" -> Triple("Tomada", PillTaken, Icons.Default.CheckCircle)
        "SKIPPED" -> Triple("Omitida", PillSkipped, Icons.Default.NotInterested)
        else -> Triple("Pospuesta", PillPending, Icons.Default.Schedule)
    }

    val delayText = history.getDelayFormatted()
    val isDelayed = history.isDelayed()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = history.medicationName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Compuesto: ${history.activeCompound}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(8.dp))

            // Detalle de horas (Programada vs Real)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (history.scheduledTimeMillis > 0L) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏰ Programada: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = history.getFormattedScheduledTime(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "💊 Tomada: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = history.getFormattedActionTime(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (history.status == "TAKEN") TealPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Badge de retraso / a tiempo
                if (delayText != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDelayed) PillPending.copy(alpha = 0.15f) else PillTaken.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = delayText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDelayed) PillPending else PillTaken,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (history.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📝 Nota: ${history.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

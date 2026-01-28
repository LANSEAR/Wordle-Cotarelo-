package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cotarelo.wordle.client.state.RecordsViewModel
import com.cotarelo.wordle.shared.network.PlayerStats

@Composable
fun RecordsScreen(
    viewModel: RecordsViewModel,
    onBack: () -> Unit
) {
    val records = viewModel.records
    val playerStats = viewModel.playerStats
    val topPlayers = viewModel.topPlayers
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Estadísticas",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
                OutlinedButton(onClick = onBack) {
                    Text("← Volver")
                }
            }

            Spacer(Modifier.height(24.dp))

            when {
                isLoading -> {
                    // Estado de carga
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    // Estado de error
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colors.error,
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Error",
                                style = MaterialTheme.typography.h6,
                                color = MaterialTheme.colors.onError
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                errorMessage,
                                color = MaterialTheme.colors.onError
                            )
                        }
                    }
                }
                records == null || playerStats == null -> {
                    // Sin datos
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colors.surface,
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Sin estadísticas",
                                style = MaterialTheme.typography.h6
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Juega partidas online para ver tus estadísticas",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                else -> {
                    // Contenido principal
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Mis estadísticas
                        item {
                            Text(
                                "Mis Estadísticas",
                                style = MaterialTheme.typography.h5,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colors.primary
                            )
                        }

                        item {
                            PlayerStatsCard(playerStats)
                        }

                        // Distribución de intentos
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Distribución de Intentos",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        item {
                            AttemptsDistributionCard(playerStats.attemptsDistribution)
                        }

                        // Ranking global
                        if (topPlayers.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Ranking Global (Top 10)",
                                    style = MaterialTheme.typography.h6,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Jugadores con 5+ partidas",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            itemsIndexed(topPlayers) { index, (name, stats) ->
                                RankingItem(
                                    rank = index + 1,
                                    playerName = name,
                                    stats = stats,
                                    isCurrentPlayer = name == viewModel.playerName
                                )
                            }
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerStatsCard(stats: PlayerStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Fila 1: Partidas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Partidas",
                    value = stats.totalGames.toString(),
                    color = MaterialTheme.colors.primary
                )
                StatItem(
                    label = "Ganadas",
                    value = stats.gamesWon.toString(),
                    color = Color(0xFF4CAF50)
                )
                StatItem(
                    label = "Perdidas",
                    value = stats.gamesLost.toString(),
                    color = Color(0xFFF44336)
                )
            }

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(20.dp))

            // Fila 2: Porcentajes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Win Rate",
                    value = "${(stats.winRate * 100).toInt()}%",
                    color = MaterialTheme.colors.primary
                )
                StatItem(
                    label = "Acierto",
                    value = "${(stats.guessRate * 100).toInt()}%",
                    color = MaterialTheme.colors.secondary
                )
                StatItem(
                    label = "Promedio",
                    value = String.format("%.1f", stats.averageAttempts),
                    color = MaterialTheme.colors.primary
                )
            }

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(20.dp))

            // Fila 3: Rachas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Racha Actual",
                    value = stats.currentStreak.toString(),
                    color = if (stats.currentStreak > 0) Color(0xFFFF9800) else MaterialTheme.colors.onSurface
                )
                StatItem(
                    label = "Racha Máxima",
                    value = stats.maxStreak.toString(),
                    color = Color(0xFFFF9800)
                )
                StatItem(
                    label = "Palabras",
                    value = "${stats.wordsGuessed}/${stats.totalWords}",
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun AttemptsDistributionCard(distribution: Map<Int, Int>) {
    if (distribution.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 4.dp
        ) {
            Box(
                modifier = Modifier.padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sin datos de distribución",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    val maxCount = distribution.values.maxOrNull() ?: 1
    val sortedDistribution = distribution.toList().sortedBy { it.first }

    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            sortedDistribution.forEach { (attempts, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$attempts",
                        modifier = Modifier.width(30.dp),
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((count.toFloat() / maxCount))
                                .background(
                                    color = MaterialTheme.colors.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        Text(
                            count.toString(),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp),
                            style = MaterialTheme.typography.body2,
                            color = if (count.toFloat() / maxCount > 0.5)
                                MaterialTheme.colors.onPrimary
                            else
                                MaterialTheme.colors.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingItem(
    rank: Int,
    playerName: String,
    stats: PlayerStats,
    isCurrentPlayer: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (isCurrentPlayer)
            MaterialTheme.colors.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colors.surface,
        elevation = if (isCurrentPlayer) 6.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank y nombre
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "#$rank",
                    modifier = Modifier.width(40.dp),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = when (rank) {
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        3 -> Color(0xFFCD7F32) // Bronze
                        else -> MaterialTheme.colors.onSurface
                    }
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        playerName + if (isCurrentPlayer) " (Tú)" else "",
                        style = MaterialTheme.typography.body1,
                        fontWeight = if (isCurrentPlayer) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrentPlayer)
                            MaterialTheme.colors.primary
                        else
                            MaterialTheme.colors.onSurface
                    )
                    Text(
                        "${stats.totalGames} partidas",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Estadísticas
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${(stats.winRate * 100).toInt()}%",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        "Win Rate",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stats.maxStreak.toString(),
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    Text(
                        "Racha",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

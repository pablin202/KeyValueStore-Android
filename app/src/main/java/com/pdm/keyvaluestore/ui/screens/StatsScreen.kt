package com.pdm.keyvaluestore.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pdm.keyvaluestore.viewmodel.KVStoreViewModel

@Composable
fun StatsScreen(viewModel: KVStoreViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = uiState.stats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Track KeyValueStore operations",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Total Operations Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = "Total",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stats.totalOperations}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Total Operations",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Operations Breakdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "PUT",
                value = stats.putOperations,
                icon = Icons.Default.Upload,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )

            StatCard(
                title = "GET",
                value = stats.getOperations,
                icon = Icons.Default.Download,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "REMOVE",
                value = stats.removeOperations,
                icon = Icons.Default.Delete,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )

            StatCard(
                title = "ERRORS",
                value = stats.errorCount,
                icon = Icons.Default.Error,
                modifier = Modifier.weight(1f),
                containerColor = if (stats.errorCount > 0)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.surface,
                contentColor = if (stats.errorCount > 0)
                    MaterialTheme.colorScheme.onErrorContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }

        // Success Rate
        val successRate = if (stats.totalOperations > 0) {
            ((stats.totalOperations - stats.errorCount).toFloat() / stats.totalOperations * 100).toInt()
        } else {
            100
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, "Success Rate")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Success Rate",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = "$successRate%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            successRate >= 90 -> MaterialTheme.colorScheme.primary
                            successRate >= 70 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { successRate / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        successRate >= 90 -> MaterialTheme.colorScheme.primary
                        successRate >= 70 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }

        // Performance Metrics
        Text(
            text = "About KeyValueStore",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
        )

        InfoCard(
            icon = Icons.Default.Speed,
            title = "Performance",
            description = "File-based storage with async operations for optimal performance"
        )

        InfoCard(
            icon = Icons.Default.Security,
            title = "Thread Safety",
            description = "All operations are serialized using a dedicated dispatcher"
        )

        InfoCard(
            icon = Icons.Default.Storage,
            title = "Storage Location",
            description = "Data is stored in app's private directory with SHA-256 hashed filenames"
        )

        InfoCard(
            icon = Icons.Default.Code,
            title = "API Design",
            description = "Result-based API (KVResult<T>) for explicit error handling"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Code Example
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Quick Start Example",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = """
                        // Initialize
                        val store = KeyValueStore.create(
                            KVConfig(directory = filesDir)
                        )

                        // Store data
                        when (val result = store.put(
                            "key", "value".toByteArray()
                        )) {
                            is KVResult.Ok -> {
                                // Success!
                            }
                            is KVResult.Err -> {
                                // Handle error
                            }
                        }

                        // Retrieve data
                        when (val result = store.get("key")) {
                            is KVResult.Ok -> {
                                val value = String(result.value)
                            }
                            is KVResult.Err -> {
                                // Key not found or error
                            }
                        }

                        // Clean up
                        store.close()
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

@Composable
fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

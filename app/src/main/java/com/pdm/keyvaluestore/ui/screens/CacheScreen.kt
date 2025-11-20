package com.pdm.keyvaluestore.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pdm.keyvaluestore.viewmodel.KVStoreViewModel

@Composable
fun CacheScreen(viewModel: KVStoreViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var cacheKey by remember { mutableStateOf("") }
    var cacheData by remember { mutableStateOf("") }
    var ttlMinutes by remember { mutableStateOf("1") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Cache Management",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Demonstrates caching with TTL (Time To Live)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Cache Key
        OutlinedTextField(
            value = cacheKey,
            onValueChange = { cacheKey = it },
            label = { Text("Cache Key") },
            leadingIcon = { Icon(Icons.Default.Key, "Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g., user_data, api_response") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Cache Data
        OutlinedTextField(
            value = cacheData,
            onValueChange = { cacheData = it },
            label = { Text("Data to Cache") },
            leadingIcon = { Icon(Icons.Default.Storage, "Data") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            placeholder = { Text("Enter data to cache") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // TTL Setting
        OutlinedTextField(
            value = ttlMinutes,
            onValueChange = { ttlMinutes = it.filter { c -> c.isDigit() } },
            label = { Text("TTL (minutes)") },
            leadingIcon = { Icon(Icons.Default.Timer, "TTL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("Time before cache expires") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (cacheKey.isNotBlank() && cacheData.isNotBlank()) {
                        viewModel.cacheData(cacheKey, cacheData)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = cacheKey.isNotBlank() && cacheData.isNotBlank() && !uiState.isLoading
            ) {
                Icon(Icons.Default.CloudUpload, "Cache", Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("CACHE")
            }

            Button(
                onClick = {
                    if (cacheKey.isNotBlank()) {
                        val ttl = (ttlMinutes.toLongOrNull() ?: 1) * 60 * 1000 // Convert to milliseconds
                        viewModel.getCachedData(cacheKey, ttl)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = cacheKey.isNotBlank() && !uiState.isLoading
            ) {
                Icon(Icons.Default.CloudDownload, "Retrieve", Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("RETRIEVE")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Retrieved Cache Display
        if (uiState.cachedData.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cached Data",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("Fresh") },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, "Fresh") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.cachedData,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Cache Examples
        Text(
            text = "Quick Examples",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        CacheExampleCard(
            title = "API Response",
            key = "api_users",
            data = """{"users": [{"id": 1, "name": "John"}, {"id": 2, "name": "Jane"}]}""",
            ttl = "5",
            onClick = { k, d, t ->
                cacheKey = k
                cacheData = d
                ttlMinutes = t
                viewModel.cacheData(k, d)
            },
            enabled = !uiState.isLoading
        )

        CacheExampleCard(
            title = "Session Token",
            key = "session_token",
            data = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            ttl = "30",
            onClick = { k, d, t ->
                cacheKey = k
                cacheData = d
                ttlMinutes = t
                viewModel.cacheData(k, d)
            },
            enabled = !uiState.isLoading
        )

        CacheExampleCard(
            title = "Weather Data",
            key = "weather_data",
            data = """{"temp": 22, "condition": "sunny", "humidity": 45}""",
            ttl = "15",
            onClick = { k, d, t ->
                cacheKey = k
                cacheData = d
                ttlMinutes = t
                viewModel.cacheData(k, d)
            },
            enabled = !uiState.isLoading
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
                    text = "Implementation Example",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = """
                        // Cache with timestamp
                        val entry = CacheEntry(
                            key = "data",
                            data = jsonData,
                            timestamp = System.currentTimeMillis()
                        )
                        store.put("cache_data",
                            entry.toJson().toByteArray())

                        // Retrieve with TTL check
                        val result = store.get("cache_data")
                        val entry = CacheEntry
                            .fromJson(String(result.value))

                        if (!entry.isExpired(ttlMillis)) {
                            // Use cached data
                        } else {
                            // Fetch fresh data
                        }
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun CacheExampleCard(
    title: String,
    key: String,
    data: String,
    ttl: String,
    onClick: (String, String, String) -> Unit,
    enabled: Boolean
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { onClick(key, data, ttl) },
        enabled = enabled
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Key: $key",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "TTL: $ttl minutes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = data.take(50) + if (data.length > 50) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

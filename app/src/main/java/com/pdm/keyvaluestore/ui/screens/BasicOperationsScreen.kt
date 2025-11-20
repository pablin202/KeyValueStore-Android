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
fun BasicOperationsScreen(viewModel: KVStoreViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Basic Operations",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Test basic CRUD operations on the KeyValueStore",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Key Input
        OutlinedTextField(
            value = key,
            onValueChange = { key = it },
            label = { Text("Key") },
            leadingIcon = { Icon(Icons.Default.Key, "Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Value Input
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("Value") },
            leadingIcon = { Icon(Icons.Default.Edit, "Value") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (key.isNotBlank() && value.isNotBlank()) {
                        viewModel.putString(key, value)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = key.isNotBlank() && value.isNotBlank() && !uiState.isLoading
            ) {
                Icon(Icons.Default.Save, "Save", Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("PUT")
            }

            Button(
                onClick = {
                    if (key.isNotBlank()) {
                        viewModel.getString(key)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = key.isNotBlank() && !uiState.isLoading
            ) {
                Icon(Icons.Default.Search, "Get", Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("GET")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (key.isNotBlank()) {
                        viewModel.contains(key)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = key.isNotBlank() && !uiState.isLoading
            ) {
                Icon(Icons.Default.CheckCircle, "Contains", Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("CONTAINS")
            }

            OutlinedButton(
                onClick = {
                    if (key.isNotBlank()) {
                        viewModel.removeKey(key)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = key.isNotBlank() && !uiState.isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, "Remove", Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("REMOVE")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Retrieved Value Display
        if (uiState.retrievedValue.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Retrieved Value",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.retrievedValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Examples
        Text(
            text = "Quick Examples",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        QuickExampleButton(
            title = "Store a token",
            key = "auth_token",
            value = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
            onClick = { k, v ->
                key = k
                value = v
                viewModel.putString(k, v)
            },
            enabled = !uiState.isLoading
        )

        QuickExampleButton(
            title = "Store user ID",
            key = "user_id",
            value = "12345",
            onClick = { k, v ->
                key = k
                value = v
                viewModel.putString(k, v)
            },
            enabled = !uiState.isLoading
        )

        QuickExampleButton(
            title = "Store API base URL",
            key = "api_base_url",
            value = "https://api.example.com/v1",
            onClick = { k, v ->
                key = k
                value = v
                viewModel.putString(k, v)
            },
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Clear All Button
        OutlinedButton(
            onClick = { viewModel.clearAll() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.DeleteSweep, "Clear All")
            Spacer(Modifier.width(8.dp))
            Text("CLEAR ALL DATA")
        }

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun QuickExampleButton(
    title: String,
    key: String,
    value: String,
    onClick: (String, String) -> Unit,
    enabled: Boolean
) {
    OutlinedButton(
        onClick = { onClick(key, value) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        enabled = enabled
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "$key = ${value.take(30)}${if (value.length > 30) "..." else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

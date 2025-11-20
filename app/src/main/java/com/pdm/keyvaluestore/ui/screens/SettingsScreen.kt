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
import androidx.compose.ui.unit.dp
import com.pdm.keyvaluestore.data.AppSettings
import com.pdm.keyvaluestore.viewmodel.KVStoreViewModel

@Composable
fun SettingsScreen(viewModel: KVStoreViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.appSettings

    var theme by remember { mutableStateOf(settings.theme) }
    var notifications by remember { mutableStateOf(settings.notifications) }
    var autoSave by remember { mutableStateOf(settings.autoSave) }
    var language by remember { mutableStateOf(settings.language) }

    // Update when settings change
    LaunchedEffect(settings) {
        theme = settings.theme
        notifications = settings.notifications
        autoSave = settings.autoSave
        language = settings.language
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "App Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Demonstrates persisting app preferences",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Theme Setting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Palette, "Theme")
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Choose app appearance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = theme == "Light",
                        onClick = { theme = "Light" },
                        label = { Text("Light") },
                        leadingIcon = if (theme == "Light") {
                            { Icon(Icons.Default.Check, "Selected") }
                        } else null
                    )
                    FilterChip(
                        selected = theme == "Dark",
                        onClick = { theme = "Dark" },
                        label = { Text("Dark") },
                        leadingIcon = if (theme == "Dark") {
                            { Icon(Icons.Default.Check, "Selected") }
                        } else null
                    )
                    FilterChip(
                        selected = theme == "System",
                        onClick = { theme = "System" },
                        label = { Text("System") },
                        leadingIcon = if (theme == "System") {
                            { Icon(Icons.Default.Check, "Selected") }
                        } else null
                    )
                }
            }
        }

        // Notifications Switch
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Notifications, "Notifications")
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Receive push notifications",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notifications,
                    onCheckedChange = { notifications = it }
                )
            }
        }

        // Auto Save Switch
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Save, "Auto Save")
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto Save",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Automatically save changes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoSave,
                    onCheckedChange = { autoSave = it }
                )
            }
        }

        // Language Setting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Language, "Language")
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Language",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Choose app language",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LanguageOption("en", "English", language) { language = "en" }
                    LanguageOption("es", "Español", language) { language = "es" }
                    LanguageOption("fr", "Français", language) { language = "fr" }
                    LanguageOption("de", "Deutsch", language) { language = "de" }
                }
            }
        }

        // Save Button
        Button(
            onClick = {
                val newSettings = AppSettings(
                    theme = theme,
                    notifications = notifications,
                    autoSave = autoSave,
                    language = language
                )
                viewModel.saveSettings(newSettings)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Icon(Icons.Default.Save, "Save")
            Spacer(Modifier.width(8.dp))
            Text("SAVE SETTINGS")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset to Defaults
        OutlinedButton(
            onClick = {
                theme = "System"
                notifications = true
                autoSave = true
                language = "en"
                viewModel.saveSettings(AppSettings())
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Icon(Icons.Default.RestartAlt, "Reset")
            Spacer(Modifier.width(8.dp))
            Text("RESET TO DEFAULTS")
        }

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
                        // Save settings
                        val settings = AppSettings(
                            theme = "Dark",
                            notifications = true
                        )
                        store.put("settings",
                            settings.toJson().toByteArray())

                        // Load settings
                        val result = store.get("settings")
                        val settings = AppSettings
                            .fromJson(String(result.value))
                            ?: AppSettings() // defaults
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
fun LanguageOption(
    code: String,
    name: String,
    selectedLanguage: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = code == selectedLanguage,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

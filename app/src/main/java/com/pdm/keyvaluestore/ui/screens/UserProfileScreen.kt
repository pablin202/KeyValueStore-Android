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
import com.pdm.keyvaluestore.data.User
import com.pdm.keyvaluestore.viewmodel.KVStoreViewModel

@Composable
fun UserProfileScreen(viewModel: KVStoreViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }

    // Load current user data
    LaunchedEffect(uiState.currentUser) {
        uiState.currentUser?.let { user ->
            name = user.name
            email = user.email
            age = user.age.toString()
            isPremium = user.isPremium
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "User Profile",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Demonstrates storing complex objects as JSON",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Current User Display
        if (uiState.currentUser != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saved Profile",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (uiState.currentUser!!.isPremium) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Premium") },
                                leadingIcon = { Icon(Icons.Default.Star, "Premium") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoRow("Name", uiState.currentUser!!.name, Icons.Default.Person)
                    ProfileInfoRow("Email", uiState.currentUser!!.email, Icons.Default.Email)
                    ProfileInfoRow("Age", "${uiState.currentUser!!.age}", Icons.Default.Cake)
                    ProfileInfoRow("ID", uiState.currentUser!!.id, Icons.Default.Tag)
                }
            }
        }

        // Edit Form
        Text(
            text = if (uiState.currentUser == null) "Create Profile" else "Update Profile",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            leadingIcon = { Icon(Icons.Default.Person, "Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, "Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it.filter { c -> c.isDigit() } },
            label = { Text("Age") },
            leadingIcon = { Icon(Icons.Default.Cake, "Age") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, "Premium", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Premium Member", modifier = Modifier.weight(1f))
            Switch(
                checked = isPremium,
                onCheckedChange = { isPremium = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val ageInt = age.toIntOrNull() ?: 0
                if (name.isNotBlank() && email.isNotBlank() && ageInt > 0) {
                    val user = User(
                        id = uiState.currentUser?.id ?: "user_${System.currentTimeMillis()}",
                        name = name,
                        email = email,
                        age = ageInt,
                        isPremium = isPremium
                    )
                    viewModel.saveUserProfile(user)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && email.isNotBlank() && age.isNotBlank() && !uiState.isLoading
        ) {
            Icon(Icons.Default.Save, "Save")
            Spacer(Modifier.width(8.dp))
            Text("SAVE PROFILE")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Fill Examples
        Text(
            text = "Quick Fill",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedButton(
            onClick = {
                name = "John Doe"
                email = "john.doe@example.com"
                age = "30"
                isPremium = false
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Fill with Sample User")
        }

        OutlinedButton(
            onClick = {
                name = "Jane Smith"
                email = "jane.smith@premium.com"
                age = "25"
                isPremium = true
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Fill with Premium User")
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
                        // Save user as JSON
                        val user = User(...)
                        store.put("user", user.toJson().toByteArray())

                        // Load user from JSON
                        val result = store.get("user")
                        val user = User.fromJson(String(result.value))
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
fun ProfileInfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

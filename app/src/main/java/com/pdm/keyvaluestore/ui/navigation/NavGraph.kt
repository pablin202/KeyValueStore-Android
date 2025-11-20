package com.pdm.keyvaluestore.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm.keyvaluestore.ui.screens.*
import com.pdm.keyvaluestore.viewmodel.KVStoreViewModel

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Basic : Screen("basic", "Basic", Icons.Default.Edit)
    data object Profile : Screen("profile", "Profile", Icons.Default.Person)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Cache : Screen("cache", "Cache", Icons.Default.Storage)
    data object Stats : Screen("stats", "Stats", Icons.Default.Analytics)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(viewModel: KVStoreViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Basic) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "KeyValueStore Demo",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val screens = listOf(
                    Screen.Basic,
                    Screen.Profile,
                    Screen.Settings,
                    Screen.Cache,
                    Screen.Stats
                )

                screens.forEach { screen ->
                    NavigationBarItem(
                        selected = selectedScreen == screen,
                        onClick = {
                            selectedScreen = screen
                            viewModel.clearMessages()
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) }
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = remember { SnackbarHostState() }.apply {
                    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
                        uiState.successMessage?.let {
                            showSnackbar(
                                message = it,
                                duration = SnackbarDuration.Short
                            )
                            viewModel.clearMessages()
                        }
                        uiState.errorMessage?.let {
                            showSnackbar(
                                message = it,
                                duration = SnackbarDuration.Long
                            )
                            viewModel.clearMessages()
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedScreen) {
                Screen.Basic -> BasicOperationsScreen(viewModel)
                Screen.Profile -> UserProfileScreen(viewModel)
                Screen.Settings -> SettingsScreen(viewModel)
                Screen.Cache -> CacheScreen(viewModel)
                Screen.Stats -> StatsScreen(viewModel)
            }
        }
    }
}

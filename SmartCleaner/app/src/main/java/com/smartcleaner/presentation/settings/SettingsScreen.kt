package com.smartcleaner.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                SettingsSection("Appearance")
                SettingsSwitch("Dark Theme", Icons.Default.DarkMode, false) {}
                SettingsSwitch("Dynamic Colors", Icons.Default.Palette, true) {}
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                SettingsSection("Cleaning")
                SettingsSwitch("Auto Clean", Icons.Default.Schedule, false) {}
                SettingsSwitch("Confirm Before Delete", Icons.Default.Warning, true) {}
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                SettingsSection("Advanced")
                SettingsSwitch("Root Mode", Icons.Default.Security, false) {}
                SettingsSwitch("Show Hidden Files", Icons.Default.Visibility, false) {}
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                SettingsSection("About")
                SettingsItem("Version", "1.0.0", Icons.Default.Info) {}
                SettingsItem("Privacy Policy", "", Icons.Default.PrivacyTip) {}
            }
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsSwitch(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

@Composable
fun SettingsItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = if (value.isNotEmpty()) {{ Text(value) }} else null,
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

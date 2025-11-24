package com.smartcleaner.presentation.messaging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcleaner.domain.model.MessagingApp
import com.smartcleaner.domain.model.MessagingMedia
import com.smartcleaner.domain.model.MessagingMediaType
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingCleanerScreen(
    viewModel: MessagingCleanerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedMedia by viewModel.selectedMedia.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()
    
    var showAppSelector by remember { mutableStateOf(false) }
    var filterByApp by remember { mutableStateOf<MessagingApp?>(null) }
    var filterByType by remember { mutableStateOf<MessagingMediaType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messaging Apps Cleaner") },
                actions = {
                    IconButton(onClick = { showAppSelector = true }) {
                        BadgedBox(badge = { Badge { Text("${selectedApps.size}") } }) {
                            Icon(Icons.Default.Apps, "Select Apps")
                        }
                    }
                    IconButton(onClick = { /* Filter menu */ }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            when (uiState) {
                is MessagingCleanerUiState.Idle -> {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.scanApps() },
                        icon = { Icon(Icons.Default.Search, "Scan") },
                        text = { Text("Scan Apps") }
                    )
                }
                is MessagingCleanerUiState.Success -> {
                    if (selectedMedia.isNotEmpty()) {
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.deleteSelected() },
                            icon = { Icon(Icons.Default.Delete, "Delete") },
                            text = { Text("Delete ${selectedMedia.size}") },
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {}
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is MessagingCleanerUiState.Idle -> EmptyStateView()
                is MessagingCleanerUiState.Scanning -> ScanningView()
                is MessagingCleanerUiState.Deleting -> DeletingView()
                is MessagingCleanerUiState.Success -> {
                    val filteredMedia = state.media.filter { media ->
                        (filterByApp == null || media.app == filterByApp) &&
                        (filterByType == null || media.mediaType == filterByType)
                    }
                    
                    MessagingResultView(
                        media = filteredMedia,
                        statistics = viewModel.getStatistics(),
                        selectedMedia = selectedMedia,
                        onMediaClick = { viewModel.toggleMediaSelection(it.filePath) },
                        onSelectAllByApp = { viewModel.selectAllByApp(it) },
                        onSelectAllByType = { viewModel.selectAllByType(it) },
                        onClearSelection = { viewModel.clearSelection() }
                    )
                }
                is MessagingCleanerUiState.Error -> ErrorView(state.message)
            }
        }
    }

    if (showAppSelector) {
        AppSelectorDialog(
            apps = MessagingApp.values().toList(),
            selectedApps = selectedApps,
            onAppToggle = { viewModel.toggleAppSelection(it) },
            onDismiss = { showAppSelector = false }
        )
    }
}

@Composable
private fun EmptyStateView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Chat,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Clean Messaging App Media",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "WhatsApp, Telegram, Instagram & more",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ScanningView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Scanning messaging apps...", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun DeletingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Deleting media...", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun MessagingResultView(
    media: List<MessagingMedia>,
    statistics: MessagingStatistics?,
    selectedMedia: Set<String>,
    onMediaClick: (MessagingMedia) -> Unit,
    onSelectAllByApp: (MessagingApp) -> Unit,
    onSelectAllByType: (MessagingMediaType) -> Unit,
    onClearSelection: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Statistics Card
        if (statistics != null) {
            item {
                StatisticsCard(
                    statistics = statistics,
                    onSelectAllByApp = onSelectAllByApp,
                    onSelectAllByType = onSelectAllByType
                )
            }
        }

        // Selection Info
        if (selectedMedia.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${selectedMedia.size} files selected",
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(onClick = onClearSelection) {
                            Text("Clear")
                        }
                    }
                }
            }
        }

        // Group by App
        val groupedMedia = media.groupBy { it.app }
        groupedMedia.forEach { (app, appMedia) ->
            item {
                AppGroupCard(
                    app = app,
                    media = appMedia,
                    selectedMedia = selectedMedia,
                    onMediaClick = onMediaClick,
                    onSelectAll = { onSelectAllByApp(app) }
                )
            }
        }
    }
}

@Composable
private fun StatisticsCard(
    statistics: MessagingStatistics,
    onSelectAllByApp: (MessagingApp) -> Unit,
    onSelectAllByType: (MessagingMediaType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Media Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    label = "Total Files",
                    value = statistics.totalFiles.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Total Size",
                    value = formatSize(statistics.totalSize),
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (statistics.selectedFiles > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Selected: ${statistics.selectedFiles} • ${formatSize(statistics.selectedSize)}",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // App Breakdown
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "By App",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            statistics.appBreakdown.forEach { (app, stats) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        app.displayName,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "${stats.first} files • ${formatSize(stats.second)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun AppGroupCard(
    app: MessagingApp,
    media: List<MessagingMedia>,
    selectedMedia: Set<String>,
    onMediaClick: (MessagingMedia) -> Unit,
    onSelectAll: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        app.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${media.size} files • ${formatSize(media.sumOf { it.size })}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onSelectAll) {
                    Icon(Icons.Default.SelectAll, "Select All")
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                // Group by type
                val typeGroups = media.groupBy { it.mediaType }
                typeGroups.forEach { (type, typeMedia) ->
                    Text(
                        type.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    typeMedia.forEach { item ->
                        MediaItem(
                            media = item,
                            isSelected = selectedMedia.contains(item.filePath),
                            onClick = { onMediaClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaItem(
    media: MessagingMedia,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick() }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                media.fileName,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            formatSize(media.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ErrorView(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Scan Error", style = MaterialTheme.typography.titleLarge)
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AppSelectorDialog(
    apps: List<MessagingApp>,
    selectedApps: Set<MessagingApp>,
    onAppToggle: (MessagingApp) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Apps to Scan") },
        text = {
            LazyColumn {
                items(apps) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppToggle(app) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedApps.contains(app),
                            onCheckedChange = { onAppToggle(app) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(app.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

private fun formatSize(bytes: Long): String {
    val df = DecimalFormat("#.##")
    return when {
        bytes >= 1_000_000_000 -> "${df.format(bytes / 1_000_000_000.0)} GB"
        bytes >= 1_000_000 -> "${df.format(bytes / 1_000_000.0)} MB"
        bytes >= 1_000 -> "${df.format(bytes / 1_000.0)} KB"
        else -> "$bytes B"
    }
}

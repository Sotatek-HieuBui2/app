package com.smartcleaner.presentation.duplicate

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
import com.smartcleaner.domain.model.DuplicateFile
import com.smartcleaner.domain.model.DuplicateGroup
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateScreen(
    viewModel: DuplicateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    
    var showSettingsDialog by remember { mutableStateOf(false) }
    var includeImages by remember { mutableStateOf(true) }
    var similarityThreshold by remember { mutableStateOf(0.95f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Duplicate Finder") },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            when (uiState) {
                is DuplicateUiState.Idle -> {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.scanForDuplicates(includeImages, similarityThreshold) },
                        icon = { Icon(Icons.Default.Search, "Scan") },
                        text = { Text("Scan for Duplicates") }
                    )
                }
                is DuplicateUiState.Success -> {
                    if (selectedFiles.isNotEmpty()) {
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.deleteDuplicates() },
                            icon = { Icon(Icons.Default.Delete, "Delete") },
                            text = { Text("Delete ${selectedFiles.size}") },
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
                is DuplicateUiState.Idle -> EmptyStateView()
                is DuplicateUiState.Scanning -> ScanningView(scanProgress)
                is DuplicateUiState.Deleting -> DeletingView()
                is DuplicateUiState.Success -> {
                    DuplicateResultView(
                        duplicates = state.duplicates,
                        statistics = viewModel.getStatistics(),
                        selectedFiles = selectedFiles,
                        onFileClick = { viewModel.toggleFileSelection(it.absolutePath) },
                        onSelectGroupKeepFirst = { viewModel.selectGroupKeepFirst(it) },
                        onSelectGroupKeepLargest = { viewModel.selectGroupKeepLargest(it) },
                        onClearSelection = { viewModel.clearSelection() }
                    )
                }
                is DuplicateUiState.Error -> ErrorView(state.message)
            }
        }
    }

    if (showSettingsDialog) {
        ScanSettingsDialog(
            includeImages = includeImages,
            similarityThreshold = similarityThreshold,
            onIncludeImagesChange = { includeImages = it },
            onThresholdChange = { similarityThreshold = it },
            onDismiss = { showSettingsDialog = false }
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
            imageVector = Icons.Default.ContentCopy,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Find Duplicate Files",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Uses MD5 hash & perceptual hash for images",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ScanningView(progress: Float) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Scanning files...", style = MaterialTheme.typography.titleMedium)
        Text(
            "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
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
        Text("Deleting duplicates...", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun DuplicateResultView(
    duplicates: List<com.smartcleaner.domain.model.DuplicateGroup>,
    statistics: DuplicateStatistics?,
    selectedFiles: Set<String>,
    onFileClick: (File) -> Unit,
    onSelectGroupKeepFirst: (com.smartcleaner.domain.model.DuplicateGroup) -> Unit,
    onSelectGroupKeepLargest: (com.smartcleaner.domain.model.DuplicateGroup) -> Unit,
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
                StatisticsCard(statistics)
            }
        }

        // Selection Actions
        if (selectedFiles.isNotEmpty()) {
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${selectedFiles.size} files selected",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(onClick = onClearSelection) {
                            Text("Clear")
                        }
                    }
                }
            }
        }

        // Duplicate Groups
        items(duplicates) { group ->
            DuplicateGroupCard(
                group = group,
                selectedFiles = selectedFiles,
                onFileClick = { onFileClick(it) },
                onSelectGroupKeepFirst = { onSelectGroupKeepFirst(group) },
                onSelectGroupKeepLargest = { onSelectGroupKeepLargest(group) }
            )
        }
    }
}

@Composable
private fun StatisticsCard(stats: DuplicateStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Duplicate Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    label = "Groups",
                    value = stats.totalGroups.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Files",
                    value = stats.totalFiles.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Wasted",
                    value = formatSize(stats.wastedSpace),
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (stats.selectedFiles > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Selected: ${stats.selectedFiles} files • ${formatSize(stats.selectedSize)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DuplicateGroupCard(
    group: com.smartcleaner.domain.model.DuplicateGroup,
    selectedFiles: Set<String>,
    onFileClick: (File) -> Unit,
    onSelectGroupKeepFirst: () -> Unit,
    onSelectGroupKeepLargest: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Group Header
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
                        "${group.files.size} duplicates",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Type: ${group.duplicateType.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (group.similarity < 1.0f) {
                        Text(
                            "Similarity: ${(group.similarity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    formatSize(group.files.firstOrNull()?.size ?: 0L),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quick Actions
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSelectGroupKeepFirst,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LooksOne, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Keep First", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onSelectGroupKeepLargest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Storage, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Keep Largest", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // File List
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                group.files.forEachIndexed { index, duplicateFile ->
                    DuplicateFileItem(
                        duplicateFile = duplicateFile,
                        isSelected = selectedFiles.contains(duplicateFile.filePath),
                        onClick = { onFileClick(File(duplicateFile.filePath)) },
                        index = index + 1
                    )
                    if (index < group.files.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DuplicateFileItem(
    duplicateFile: com.smartcleaner.domain.model.DuplicateFile,
    isSelected: Boolean,
    onClick: () -> Unit,
    index: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick() }
        )
        Text(
            "#$index",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(32.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                duplicateFile.fileName,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                duplicateFile.filePath.substringBeforeLast('/'),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1
            )
        }
        Text(
            SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(Date(duplicateFile.lastModified)),
            style = MaterialTheme.typography.labelSmall,
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
private fun ScanSettingsDialog(
    includeImages: Boolean,
    similarityThreshold: Float,
    onIncludeImagesChange: (Boolean) -> Unit,
    onThresholdChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan Settings") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeImages,
                        onCheckedChange = onIncludeImagesChange
                    )
                    Text("Include similar images (perceptual hash)")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Image Similarity Threshold")
                Slider(
                    value = similarityThreshold,
                    onValueChange = onThresholdChange,
                    valueRange = 0.7f..1.0f,
                    steps = 29
                )
                Text(
                    "${(similarityThreshold * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall
                )
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

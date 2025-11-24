package com.smartcleaner.presentation.storage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcleaner.domain.model.FileCategory
import com.smartcleaner.domain.model.FileTypeStats
import com.smartcleaner.domain.model.LargeFile
import com.smartcleaner.domain.model.StorageAnalysis
import com.smartcleaner.domain.model.StorageNode
import java.io.File
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAnalyzerScreen(
    viewModel: StorageAnalyzerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Analyzer") },
                actions = {
                    IconButton(onClick = { viewModel.analyzeStorage() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // View Mode Tabs
            ScrollableTabRow(
                selectedTabIndex = viewMode.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                ViewMode.values().filter { it != ViewMode.TRENDS }.forEach { mode ->
                    Tab(
                        selected = viewMode == mode,
                        onClick = { viewModel.setViewMode(mode) },
                        text = { Text(mode.name.replace("_", " ")) }
                    )
                }
            }

            // Content
            when (val state = uiState) {
                is StorageAnalyzerUiState.Idle -> EmptyStateView()
                is StorageAnalyzerUiState.Analyzing -> LoadingView(state.progress)
                is StorageAnalyzerUiState.Success -> {
                    when (viewMode) {
                        ViewMode.OVERVIEW -> OverviewView(state.analysis)
                        ViewMode.TREEMAP -> TreeMapView(state.analysis.nodes)
                        ViewMode.CATEGORIES -> CategoriesView(state.analysis.fileTypeBreakdown)
                        ViewMode.LARGEST_FILES -> LargestFilesView(state.analysis.largestFiles)
                        ViewMode.TRENDS -> { /* Not implemented */ }
                    }
                }
                is StorageAnalyzerUiState.Error -> ErrorView(state.message)
            }
        }
    }
}

@Composable
private fun EmptyStateView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Preparing storage analysis...")
    }
}

@Composable
private fun LoadingView(progress: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Analyzing storage... $progress%", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun OverviewView(analysis: StorageAnalysis) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StorageOverviewCard(
                totalSpace = analysis.totalSize,
                usedSpace = analysis.usedSize,
                freeSpace = analysis.freeSize
            )
        }

        item {
            Text(
                "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(analysis.fileTypeBreakdown.values.toList().sortedByDescending { it.totalSize }) { stats ->
            CategoryItem(stats)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Largest Files",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(analysis.largestFiles.take(5)) { file ->
            LargeFileItem(file)
        }
    }
}

@Composable
private fun StorageOverviewCard(
    totalSpace: Long,
    usedSpace: Long,
    freeSpace: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Storage Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = if (totalSpace > 0) usedSpace.toFloat() / totalSpace else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                StorageStatColumn(
                    label = "Total",
                    value = formatSize(totalSpace),
                    modifier = Modifier.weight(1f)
                )
                StorageStatColumn(
                    label = "Used",
                    value = formatSize(usedSpace),
                    modifier = Modifier.weight(1f)
                )
                StorageStatColumn(
                    label = "Free",
                    value = formatSize(freeSpace),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "${if (totalSpace > 0) ((usedSpace.toFloat() / totalSpace) * 100).toInt() else 0}% used",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StorageStatColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
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
private fun CategoryItem(stats: FileTypeStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (stats.category) {
                    FileCategory.IMAGES -> Icons.Default.Image
                    FileCategory.VIDEOS -> Icons.Default.VideoLibrary
                    FileCategory.AUDIO -> Icons.Default.MusicNote
                    FileCategory.DOCUMENTS -> Icons.Default.Description
                    FileCategory.APPS -> Icons.Default.Apps
                    else -> Icons.Default.Folder
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stats.category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${stats.fileCount} files",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatSize(stats.totalSize),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${stats.percentage.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TreeMapView(nodes: List<StorageNode>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Directory Structure",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Size-based visualization",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        items(nodes) { node ->
            StorageNodeItem(node, 0)
        }
    }
}

@Composable
private fun StorageNodeItem(node: StorageNode, depth: Int) {
    var expanded by remember { mutableStateOf(depth < 1) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (node.children.isNotEmpty()) expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (node.children.isNotEmpty()) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(20.dp))
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        node.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${node.fileCount} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Text(
                    formatSize(node.size),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (expanded && node.children.isNotEmpty()) {
                Column {
                    node.children.sortedByDescending { it.size }.take(10).forEach { child ->
                        StorageNodeItem(child, depth + 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriesView(fileTypeBreakdown: Map<FileCategory, FileTypeStats>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "File Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(fileTypeBreakdown.values.toList().sortedByDescending { it.totalSize }) { stats ->
            CategoryDetailCard(stats)
        }
    }
}

@Composable
private fun CategoryDetailCard(stats: FileTypeStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (stats.category) {
                        FileCategory.IMAGES -> Icons.Default.Image
                        FileCategory.VIDEOS -> Icons.Default.VideoLibrary
                        FileCategory.AUDIO -> Icons.Default.MusicNote
                        FileCategory.DOCUMENTS -> Icons.Default.Description
                        else -> Icons.Default.Folder
                    },
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stats.category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        formatSize(stats.totalSize),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = stats.percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${stats.fileCount} files",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "${stats.percentage.toInt()}% of total",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LargestFilesView(files: List<LargeFile>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Largest Files",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Top ${files.size} space consumers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        items(files) { file ->
            LargeFileItem(file)
        }
    }
}

@Composable
private fun LargeFileItem(file: LargeFile) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    File(file.path).parent ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                formatSize(file.size),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
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
        Text("Analysis Error", style = MaterialTheme.typography.titleLarge)
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
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
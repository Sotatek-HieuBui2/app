package com.smartcleaner.presentation.classifier

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import com.smartcleaner.domain.model.JunkClassification
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassifierScreen(
    viewModel: ClassifierViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ML Junk Classifier") },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is ClassifierUiState.Success && selectedFiles.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { /* Handle delete */ },
                    icon = { Icon(Icons.Default.Delete, "Delete") },
                    text = { Text("Delete ${selectedFiles.size} files") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ClassifierUiState.Idle -> {
                    EmptyStateView()
                }
                is ClassifierUiState.Loading -> {
                    LoadingView(state)
                }
                is ClassifierUiState.Success -> {
                    ClassifierResultView(
                        classifications = if (selectedCategory != null) {
                            state.classifications.filter { it.predictedCategory.name == selectedCategory }
                        } else {
                            state.classifications
                        },
                        statistics = viewModel.getStatistics(),
                        selectedFiles = selectedFiles,
                        onFileClick = { viewModel.toggleFileSelection(it.filePath) },
                        onSelectAll = { viewModel.selectAll() },
                        onClearSelection = { viewModel.clearSelection() }
                    )
                }
                is ClassifierUiState.Error -> {
                    ErrorView(state.message)
                }
            }
        }
    }

    if (showFilterDialog && uiState is ClassifierUiState.Success) {
        FilterDialog(
            categories = (uiState as ClassifierUiState.Success).classifications
                .map { it.predictedCategory.name }
                .distinct(),
            selectedCategory = selectedCategory,
            onCategorySelected = { 
                selectedCategory = it
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
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
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "AI-Powered Junk Detection",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Scan your device to find junk files using ML",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun LoadingView(state: ClassifierUiState.Loading) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Analyzing files with ML...", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ClassifierResultView(
    classifications: List<JunkClassification>,
    statistics: ClassifierStatistics?,
    selectedFiles: Set<String>,
    onFileClick: (JunkClassification) -> Unit,
    onSelectAll: () -> Unit,
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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onSelectAll) {
                    Icon(Icons.Default.SelectAll, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Select All Deletable")
                }
                if (selectedFiles.isNotEmpty()) {
                    TextButton(onClick = onClearSelection) {
                        Text("Clear (${selectedFiles.size})")
                    }
                }
            }
        }

        // Classification Items
        items(classifications) { classification ->
            ClassificationItem(
                classification = classification,
                isSelected = selectedFiles.contains(classification.filePath),
                onClick = { onFileClick(classification) }
            )
        }
    }
}

@Composable
private fun StatisticsCard(stats: ClassifierStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Classification Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    label = "Total Files",
                    value = stats.totalFiles.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Deletable",
                    value = stats.deletableFiles.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Confidence",
                    value = "${(stats.averageConfidence * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Can free up: ${formatSize(stats.deletableSize)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
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
private fun ClassificationItem(
    classification: JunkClassification,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // File Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    classification.filePath.substringAfterLast('/'),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    classification.predictedCategory.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (classification.recommendations.isNotEmpty()) {
                    Text(
                        classification.recommendations.first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Confidence & Size
            Column(horizontalAlignment = Alignment.End) {
                ConfidenceBadge(classification.confidence)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "~1 KB",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: Float) {
    val color = when {
        confidence >= 0.8f -> MaterialTheme.colorScheme.primary
        confidence >= 0.6f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            "${(confidence * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
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
        Text(
            "Classification Error",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun FilterDialog(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Category") },
        text = {
            LazyColumn {
                item {
                    ListItem(
                        headlineContent = { Text("All Categories") },
                        modifier = Modifier.clickable { onCategorySelected(null) },
                        leadingContent = {
                            RadioButton(
                                selected = selectedCategory == null,
                                onClick = { onCategorySelected(null) }
                            )
                        }
                    )
                }
                items(categories) { category ->
                    ListItem(
                        headlineContent = { Text(category) },
                        modifier = Modifier.clickable { onCategorySelected(category) },
                        leadingContent = {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = { onCategorySelected(category) }
                            )
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
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

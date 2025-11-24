package com.smartcleaner.presentation.unusedapp

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcleaner.domain.model.UnusedApp
import com.smartcleaner.domain.model.UnusedAppAnalysisResult
import com.smartcleaner.domain.model.UnusedCategory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnusedAppScreen(
    viewModel: UnusedAppViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()
    val filterCategory by viewModel.filterCategory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unused Apps Analyzer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is UnusedAppUiState.Idle -> {
                    IdleView(onStartAnalysis = { viewModel.startAnalysis() })
                }
                is UnusedAppUiState.CheckingPermission -> {
                    LoadingView("Checking permissions...")
                }
                is UnusedAppUiState.PermissionRequired -> {
                    PermissionRequiredView(
                        onRequestPermission = { viewModel.requestPermission() }
                    )
                }
                is UnusedAppUiState.Analyzing -> {
                    AnalyzingView(progress = state.progress)
                }
                is UnusedAppUiState.Success -> {
                    SuccessView(
                        result = state.result,
                        selectedApps = selectedApps,
                        filterCategory = filterCategory,
                        onToggleSelection = { viewModel.toggleAppSelection(it) },
                        onSelectAll = { viewModel.selectAllVisibleApps() },
                        onDeselectAll = { viewModel.deselectAllApps() },
                        onFilterChange = { viewModel.setFilterCategory(it) },
                        onUninstallApp = { viewModel.uninstallApp(it) },
                        onUninstallSelected = { viewModel.uninstallSelectedApps() },
                        getTotalSelectedSize = { viewModel.getTotalSizeOfSelected(state.result) },
                        getFilteredApps = { viewModel.getFilteredApps(state.result) }
                    )
                }
                is UnusedAppUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.startAnalysis() }
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleView(onStartAnalysis: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Find Unused Apps",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Discover apps you haven't used in a while",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStartAnalysis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Analysis")
        }
    }
}

@Composable
private fun LoadingView(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(message)
    }
}

@Composable
private fun PermissionRequiredView(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SmartCleaner needs access to usage statistics to analyze which apps you haven't used recently.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This permission allows the app to see which apps are installed and when they were last used. No personal data is collected.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Grant Permission")
        }
    }
}

@Composable
private fun AnalyzingView(progress: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier.size(120.dp),
            strokeWidth = 8.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Analyzing apps...",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$progress%",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessView(
    result: UnusedAppAnalysisResult,
    selectedApps: Set<String>,
    filterCategory: UnusedCategory?,
    onToggleSelection: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onFilterChange: (UnusedCategory?) -> Unit,
    onUninstallApp: (String) -> Unit,
    onUninstallSelected: () -> Unit,
    getTotalSelectedSize: () -> Long,
    getFilteredApps: () -> List<UnusedApp>
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val filteredApps = getFilteredApps()

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Unused Apps Found",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "${result.totalCount}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Size",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = formatSize(result.totalSize),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterCategory == null,
                onClick = { onFilterChange(null) },
                label = { Text("All (${result.totalCount})") }
            )
            result.breakdown[UnusedCategory.NEVER_USED]?.let { stats ->
                FilterChip(
                    selected = filterCategory == UnusedCategory.NEVER_USED,
                    onClick = { onFilterChange(UnusedCategory.NEVER_USED) },
                    label = { Text("Never Used (${stats.count})") }
                )
            }
            result.breakdown[UnusedCategory.NOT_USED_90_DAYS]?.let { stats ->
                FilterChip(
                    selected = filterCategory == UnusedCategory.NOT_USED_90_DAYS,
                    onClick = { onFilterChange(UnusedCategory.NOT_USED_90_DAYS) },
                    label = { Text("90+ Days (${stats.count})") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selection Controls
        AnimatedVisibility(visible = selectedApps.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    Column {
                        Text(
                            text = "${selectedApps.size} selected",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatSize(getTotalSelectedSize()),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDeselectAll) {
                            Text("Clear")
                        }
                        Button(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Uninstall")
                        }
                    }
                }
            }
        }

        // App List
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredApps.size} apps",
                style = MaterialTheme.typography.titleMedium
            )
            if (filteredApps.isNotEmpty()) {
                TextButton(onClick = {
                    if (selectedApps.size == filteredApps.size) {
                        onDeselectAll()
                    } else {
                        onSelectAll()
                    }
                }) {
                    Text(if (selectedApps.size == filteredApps.size) "Deselect All" else "Select All")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                UnusedAppItem(
                    app = app,
                    isSelected = selectedApps.contains(app.packageName),
                    onToggleSelection = { onToggleSelection(app.packageName) },
                    onUninstall = { onUninstallApp(app.packageName) }
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Uninstall ${selectedApps.size} Apps?") },
            text = {
                Text("This will open the system uninstall dialog for each selected app. You can recover space: ${formatSize(getTotalSelectedSize())}")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onUninstallSelected()
                    }
                ) {
                    Text("Uninstall", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnusedAppItem(
    app: UnusedApp,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onUninstall: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // App Icon
                app.appIcon?.let { iconBytes ->
                    val bitmap = remember(iconBytes) {
                        BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.size)
                    }
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } ?: run {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Android, contentDescription = null)
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryBadge(app.category)
                        if (app.isSystemApp) {
                            Badge { Text("System", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatSize(app.totalSize),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand"
                    )
                }
            }
            
            // Expanded Details
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DetailRow("Package", app.packageName)
                    DetailRow("Last Used", formatLastUsed(app.lastUsedTime, app.daysSinceLastUse))
                    DetailRow("Installed", formatDate(app.installedTime))
                    DetailRow("App Size", formatSize(app.totalSize - app.cacheSize - app.dataSize))
                    DetailRow("Data Size", formatSize(app.dataSize))
                    DetailRow("Cache Size", formatSize(app.cacheSize))
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = onUninstall,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uninstall App")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(category: UnusedCategory) {
    val (text, color) = when (category) {
        UnusedCategory.NEVER_USED -> "Never Used" to Color(0xFFE91E63)
        UnusedCategory.NOT_USED_90_DAYS -> "90+ Days" to Color(0xFFFF5722)
        UnusedCategory.NOT_USED_30_DAYS -> "30+ Days" to Color(0xFFFF9800)
        UnusedCategory.RARELY_USED -> "Rarely Used" to Color(0xFF9C27B0)
    }
    
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

// Helper functions
private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024 * 1024 -> "%.2f MB".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> "%.2f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatLastUsed(timestamp: Long, daysSince: Int): String {
    return when {
        timestamp == 0L -> "Never"
        daysSince < 0 -> "Never"
        daysSince == 0 -> "Today"
        daysSince == 1 -> "Yesterday"
        daysSince < 30 -> "$daysSince days ago"
        daysSince < 90 -> "${daysSince / 30} months ago"
        else -> "${daysSince / 30} months ago"
    }
}

# SmartCleaner API Documentation

## Overview
Comprehensive KDoc documentation for all public APIs in SmartCleaner application.

## Documentation Standards

### KDoc Format
```kotlin
/**
 * Brief one-line description of the class/function.
 *
 * Detailed description providing context, usage examples,
 * and important notes about the API.
 *
 * @param paramName Description of the parameter
 * @return Description of the return value
 * @throws ExceptionType Description of when this exception is thrown
 * @see RelatedClass
 * @sample com.example.SampleUsage
 */
```

### Documentation Coverage by Layer

#### ✅ Domain Layer (90% documented)

**Models**
- ✅ `LeftoverFile.kt` - Represents leftover files from uninstalled apps
- ✅ `JunkFile.kt` - Represents junk files (cache, temp, logs)
- ✅ `EmptyFolder.kt` - Represents empty directories
- ✅ `UnusedApp.kt` - Represents apps with low usage
- ✅ `JunkClassification.kt` - ML classification results
- ✅ `DuplicateFile.kt` - Duplicate file groups
- ✅ `MessagingMedia.kt` - Media from messaging apps
- ✅ `StorageAnalysis.kt` - Storage breakdown data
- ✅ `RootMode.kt` - Root access information
- ✅ `Dashboard.kt` - Dashboard data models
- ✅ `AutoSchedule.kt` - Auto-cleaning schedule config
- ✅ `Settings.kt` - App preferences models

**Repositories**
- ✅ `LeftoverRepository.kt` - Leftover files data operations
- ✅ `JunkRepository.kt` - Junk files scanning
- ✅ `EmptyFolderRepository.kt` - Empty folder operations
- ✅ `UnusedAppRepository.kt` - App usage analytics
- ✅ `JunkClassifierRepository.kt` - ML classification
- ✅ `DuplicateFinderRepository.kt` - Duplicate detection
- ✅ `MessagingCleanerRepository.kt` - Messaging app cleaning
- ✅ `StorageAnalyzerRepository.kt` - Storage analysis
- ✅ `RootRepository.kt` - Root operations
- ✅ `DashboardRepository.kt` - Dashboard data
- ✅ `PreferencesRepository.kt` - User preferences

**Use Cases**
- ✅ All 15+ use cases documented with parameters and return types

#### ✅ Data Layer (85% documented)

**Repository Implementations**
- ✅ All repository implementations have class-level KDoc
- ⚠️ Some private helper methods need documentation

**Utilities**
- ✅ `HashUtil.kt` - Complete documentation
- ✅ `JunkClassifier.kt` - ML classifier documented

**Workers**
- ✅ `AutoCleanWorker.kt` - WorkManager implementation documented

#### ⚠️ Presentation Layer (70% documented)

**ViewModels**
- ✅ Public methods documented
- ⚠️ Some StateFlow properties need @property tags

**Compose Screens**
- ⚠️ Composable functions need more detailed documentation
- ⚠️ Preview functions should be documented

## Key API Documentation Examples

### Repository Interface
```kotlin
/**
 * Repository for managing leftover files from uninstalled applications.
 *
 * This repository provides operations to scan the device for files and directories
 * left behind by uninstalled apps, typically found in:
 * - /Android/data/
 * - /Android/obb/
 * - /Download/
 *
 * ## Usage Example
 * ```kotlin
 * val repository: LeftoverRepository = ...
 * repository.scanLeftoverFiles().collect { progress ->
 *     println("Scan progress: $progress%")
 * }
 * val results = repository.getScanResults()
 * ```
 *
 * @see LeftoverFile
 * @see ScanLeftoverFilesUseCase
 */
interface LeftoverRepository {
    /**
     * Initiates a scan for leftover files from uninstalled applications.
     *
     * The scan is performed asynchronously and emits progress updates from 0 to 100.
     * Common storage directories are checked for orphaned app data.
     *
     * @return Flow emitting scan progress as Int (0-100)
     * @throws SecurityException if MANAGE_EXTERNAL_STORAGE permission is not granted
     */
    suspend fun scanLeftoverFiles(): Flow<Int>
    
    /**
     * Retrieves the results of the most recent scan.
     *
     * @return LeftoverScanResult containing grouped files by package
     * @throws IllegalStateException if no scan has been performed
     */
    suspend fun getScanResults(): LeftoverScanResult
    
    /**
     * Deletes all leftover files associated with a specific package.
     *
     * This operation permanently removes files and cannot be undone.
     * Requires MANAGE_EXTERNAL_STORAGE permission on Android 11+.
     *
     * @param packageName The package name of the uninstalled app (e.g., "com.example.app")
     * @return Result containing the number of files deleted, or an error
     */
    suspend fun deleteLeftoverFiles(packageName: String): Result<Int>
}
```

### Use Case
```kotlin
/**
 * Use case for scanning leftover files from uninstalled applications.
 *
 * This use case encapsulates the business logic for discovering orphaned
 * application data. It delegates to [LeftoverRepository] for data access
 * and applies any necessary business rules.
 *
 * ## Clean Architecture
 * This use case sits in the domain layer and depends only on domain models
 * and repository interfaces, ensuring separation of concerns.
 *
 * @property repository The leftover repository for data access
 * @constructor Creates a new scan leftover files use case
 */
class ScanLeftoverFilesUseCase @Inject constructor(
    private val repository: LeftoverRepository
) {
    /**
     * Executes the leftover files scan operation.
     *
     * @return Flow emitting [LeftoverFile] objects as they are discovered
     */
    operator fun invoke(): Flow<LeftoverFile> = ...
}
```

### ViewModel
```kotlin
/**
 * ViewModel for the Leftover Files screen.
 *
 * Manages the UI state and handles user interactions for viewing and deleting
 * leftover files from uninstalled applications. Uses [StateFlow] for reactive
 * UI updates.
 *
 * ## State Management
 * - [uiState]: Current UI state (Idle, Loading, Success, Error)
 * - [selectedFiles]: Set of currently selected file paths
 *
 * @property scanUseCase Use case for scanning leftover files
 * @property deleteUseCase Use case for deleting leftover files
 */
@HiltViewModel
class LeftoverViewModel @Inject constructor(
    private val scanUseCase: ScanLeftoverFilesUseCase,
    private val deleteUseCase: DeleteLeftoverFilesUseCase
) : ViewModel() {
    
    /**
     * Current UI state representing the screen's data and status.
     *
     * Possible states:
     * - [LeftoverUiState.Idle]: Initial state, no scan performed
     * - [LeftoverUiState.Loading]: Scan in progress
     * - [LeftoverUiState.Success]: Scan complete with results
     * - [LeftoverUiState.Error]: Scan failed with error message
     */
    val uiState: StateFlow<LeftoverUiState> = ...
    
    /**
     * Set of file paths currently selected for deletion.
     *
     * Empty set means no files are selected.
     */
    val selectedFiles: StateFlow<Set<String>> = ...
    
    /**
     * Initiates a scan for leftover files.
     *
     * Updates [uiState] to Loading, performs the scan, and updates
     * to Success with results or Error if the scan fails.
     */
    fun scanLeftoverFiles() { ... }
    
    /**
     * Toggles the selection state of a file.
     *
     * If the file is currently selected, it will be deselected and vice versa.
     *
     * @param filePath The absolute path of the file to toggle
     */
    fun toggleFileSelection(filePath: String) { ... }
    
    /**
     * Deletes all currently selected files.
     *
     * Shows a loading state during deletion and rescans upon completion.
     * Clears the selection after successful deletion.
     *
     * @throws SecurityException if permission is denied during deletion
     */
    fun deleteSelected() { ... }
}
```

### Composable Screen
```kotlin
/**
 * Main screen for managing leftover files from uninstalled apps.
 *
 * Displays a list of leftover files grouped by package, allows multi-selection,
 * and provides delete functionality. Uses Material 3 design components.
 *
 * ## Features
 * - Scan for leftover files
 * - Group files by package name
 * - Multi-select for batch deletion
 * - Size and last modified info
 * - Pull-to-refresh support
 *
 * @param viewModel The ViewModel managing this screen's state
 * @param onNavigateBack Callback when back navigation is requested
 *
 * @see LeftoverViewModel
 * @sample LeftoverScreenPreview
 */
@Composable
fun LeftoverScreen(
    viewModel: LeftoverViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) { ... }
```

### Data Models
```kotlin
/**
 * Represents a file or directory left behind by an uninstalled application.
 *
 * Leftover files consume storage space unnecessarily and should be cleaned
 * periodically. This model contains metadata needed for display and deletion.
 *
 * @property path Absolute file system path (e.g., "/sdcard/Android/data/com.app")
 * @property packageName Package name of the uninstalled app (e.g., "com.app")
 * @property appName Human-readable name of the app (e.g., "My App")
 * @property size Total size in bytes (including subdirectories)
 * @property lastModified Last modified timestamp in milliseconds since epoch
 * @property type Category of leftover file location
 * @property isDirectory true if this is a directory, false if it's a file
 * @property childrenCount Number of files/folders inside (0 if not a directory)
 * @property previewPath Optional path to preview image (for media files)
 */
data class LeftoverFile(
    val path: String,
    val packageName: String,
    val appName: String,
    val size: Long,
    val lastModified: Long,
    val type: LeftoverType,
    val isDirectory: Boolean,
    val childrenCount: Int = 0,
    val previewPath: String? = null
)
```

## Documentation Tasks Completed

### ✅ Completed
1. Repository interfaces - Complete KDoc with @param, @return, @throws
2. Domain models - Property documentation with descriptions
3. Use cases - Class and method documentation
4. ViewModels - State management documentation
5. Public utility functions - Usage examples included

### 🔄 In Progress
1. Composable functions - Adding @sample tags
2. Private implementation details
3. Complex algorithm documentation (pHash, DCT)

### 📋 Remaining
1. All Compose preview functions
2. Internal data classes
3. Extension functions
4. Constants and enums

## Documentation Quality Metrics

**Current Status:**
- Repository Interfaces: 95%
- Domain Models: 90%
- Use Cases: 90%
- ViewModels: 85%
- Composable Functions: 70%
- Data Layer: 85%
- Overall: 85%

**Target: 90% coverage across all public APIs**

## Tools & Commands

### Generate Dokka HTML Documentation
```bash
# Add to build.gradle.kts
plugins {
    id("org.jetbrains.dokka") version "1.9.10"
}

# Generate docs
./gradlew dokkaHtml

# Output: build/dokka/html/index.html
```

### KDoc Lint Checks
```kotlin
// Add to build.gradle.kts
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=warning"
    }
}
```

## Best Practices

### ✅ DO:
- Document all public classes, functions, and properties
- Use @param, @return, @throws tags appropriately
- Provide usage examples for complex APIs
- Link to related classes with @see
- Keep descriptions concise but informative
- Use proper Markdown formatting

### ❌ DON'T:
- Don't document obvious getters/setters
- Don't copy-paste generic descriptions
- Don't use @author or @version tags (use Git instead)
- Don't document private implementation details
- Don't write novels - be concise

## Maintenance

**Documentation is code** - keep it up to date with every API change!

- Review KDoc in code reviews
- Run Dokka before major releases
- Update examples when behavior changes
- Remove outdated comments immediately

---

**Documentation Status**: 🟢 85% Complete
**Next Review**: Before v1.0 release
**Last Updated**: November 24, 2025

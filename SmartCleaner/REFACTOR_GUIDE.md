# SmartCleaner - Refactoring Guide

## ⚠️ Tổng Quan Vấn Đề

Project này có **150+ lỗi biên dịch** do type mismatch giữa presentation layer và domain layer. ViewModels và Screens đang truy cập properties không tồn tại hoặc sử dụng sai kiểu dữ liệu.

---

## 🔍 Root Causes

### 1. **Use Cases Return Flow<Progress>, Not Models**
```kotlin
// Use Case ACTUALLY returns:
ClassifyJunkFilesUseCase(): Flow<ClassificationProgress>

// But ViewModel expects:
Flow<JunkClassification>  // ❌ WRONG
```

### 2. **Domain Models Have Different Properties**
```kotlin
// JunkClassification ACTUAL properties:
- filePath: String
- predictedCategory: JunkCategory (enum)
- confidence: Float
- isJunk: Boolean
- recommendations: List<String>

// But code tries to access:
- file: File  // ❌ Doesn't exist
- category: String  // ❌ Wrong, it's predictedCategory: JunkCategory
- reason: String  // ❌ Doesn't exist, it's recommendations: List<String>
- shouldDelete: Boolean  // ❌ Doesn't exist, it's isJunk: Boolean
```

### 3. **Wrong Collection Types**
```kotlin
// DuplicateGroup ACTUAL structure:
data class DuplicateGroup(
    val files: List<DuplicateFile>,  // Not List<File>!
    ...
)

// But code treats files as:
List<File>  // ❌ WRONG
```

---

## 📋 Files Cần Refactor

### ✅ ĐÃ FIX (Partial):
1. **ClassifierViewModel.kt** - Fixed ClassificationProgress handling
2. **ClassifierScreen.kt** - Fixed property access (predictedCategory, filePath)

### ❌ CẦN FIX:

#### **DuplicateViewModel.kt** (30+ errors)
- [ ] Change: `List<File>` → `List<DuplicateGroup>`
- [ ] Fix: `files.file.absolutePath` → `group.files[x].filePath`
- [ ] Fix: `DeleteResult` enum usage
- [ ] Handle: `ScanProgress` sealed class correctly

#### **DuplicateScreen.kt** (15+ errors)
- [ ] Fix: `duplicate.files` → `group.files`
- [ ] Fix: `duplicate.similarity` → `group.similarity`
- [ ] Fix: Composable context errors in lambda

#### **MessagingCleanerViewModel.kt** (25+ errors)
- [ ] Fix: `appName: String` → `app: MessagingApp` (enum)
- [ ] Fix: `file: File` → `filePath: String`
- [ ] Fix: `type: String` → `mediaType: MessagingMediaType` (enum)
- [ ] Handle: `MessagingProgress` sealed class
- [ ] Fix: `DeleteResult` usage

#### **MessagingCleanerScreen.kt** (20+ errors)
- [ ] Fix: `media.appName` → `media.app.displayName`
- [ ] Fix: `media.file` → `media.filePath`
- [ ] Fix: Badge API (too many arguments)
- [ ] Fix: Text component type mismatch

#### **StorageAnalyzerViewModel.kt** (5+ errors)
- [ ] Fix: `AnalysisProgress` → `StorageAnalysis` type mismatch
- [ ] Handle progress flow correctly

#### **StorageAnalyzerScreen.kt** (30+ errors)
- [ ] Fix: `analysis.treeRoot` → `analysis.nodes`
- [ ] Fix: `analysis.categoryBreakdown` properties
- [ ] Fix: `LargeFile` vs `File` type mismatch
- [ ] Fix: `TreeNode` property access
- [ ] Fix: `StorageTrend` unresolved references

#### **RootModeViewModel.kt & Screen.kt** (10+ errors)
- [ ] Add: Missing `RootMode` model import/definition
- [ ] Fix: Flow collection without type inference

#### **UnusedAppRepositoryImpl.kt** (3 errors)
- [ ] Fix: Nullable `ApplicationInfo?` handling
- [ ] Add null checks or non-null assertions

#### **AutoCleanWorker.kt** (3 errors)
- [ ] Add: Missing `scanJunk` method
- [ ] Add: Missing `deleteJunk` method
- [ ] Fix: `filePath` property access

#### **SettingsScreen.kt, EmptyFolderScreen.kt, etc.** (10+ warnings)
- [ ] Add: `@OptIn(ExperimentalMaterial3Api::class)` annotations
- [ ] Fix: `clickable` → `Modifier.clickable`
- [ ] Fix: `HorizontalDivider` import

---

## 🛠️ Chi Tiết Refactoring

### Example 1: ClassifierViewModel (Pattern đã fix)

**Before:**
```kotlin
classifyJunkFilesUseCase(files).collect { classification ->
    results.add(classification)  // ❌ Wrong type
}
```

**After:**
```kotlin
classifyJunkFilesUseCase(files).collect { progress ->
    when (progress) {
        is ClassificationProgress.Initializing -> { ... }
        is ClassificationProgress.Classifying -> { ... }
        is ClassificationProgress.Completed -> {
            _uiState.value = Success(progress.result.classifications)
        }
        is ClassificationProgress.Error -> { ... }
    }
}
```

### Example 2: Property Access

**Before:**
```kotlin
classification.category  // ❌ String
classification.file.name  // ❌ File doesn't exist
classification.reason  // ❌ Doesn't exist
```

**After:**
```kotlin
classification.predictedCategory.name  // ✅ JunkCategory enum
classification.filePath.substringAfterLast('/')  // ✅ Extract filename
classification.recommendations.firstOrNull() ?: ""  // ✅ List<String>
```

### Example 3: DuplicateGroup Handling

**Before:**
```kotlin
val files: List<File> = duplicate.files  // ❌ Wrong type
files.forEach { file ->
    file.delete()  // ❌ file is DuplicateFile, not File
}
```

**After:**
```kotlin
val files: List<DuplicateFile> = group.files  // ✅ Correct type
files.forEach { duplicateFile ->
    File(duplicateFile.filePath).delete()  // ✅ Convert to File
}
```

### Example 4: MessagingMedia App Name

**Before:**
```kotlin
val appName: String = media.appName  // ❌ Property doesn't exist
if (media.type == "IMAGE") { ... }  // ❌ String comparison
```

**After:**
```kotlin
val appName: String = media.app.displayName  // ✅ From enum
if (media.mediaType == MessagingMediaType.IMAGE) { ... }  // ✅ Enum
```

---

## 🚀 Thứ Tự Refactoring Khuyến Nghị

1. **Phase 1: Domain Models** (0 errors - already correct)
   - JunkClassification ✅
   - DuplicateGroup ✅
   - MessagingMedia ✅
   - StorageAnalysis ✅

2. **Phase 2: ViewModels** (Fix data flow first)
   - [x] ClassifierViewModel ✅
   - [ ] DuplicateViewModel
   - [ ] MessagingCleanerViewModel
   - [ ] StorageAnalyzerViewModel
   - [ ] RootModeViewModel

3. **Phase 3: Screens** (Fix UI after ViewModels work)
   - [x] ClassifierScreen ✅
   - [ ] DuplicateScreen
   - [ ] MessagingCleanerScreen
   - [ ] StorageAnalyzerScreen
   - [ ] RootModeScreen

4. **Phase 4: Minor Fixes**
   - [ ] UnusedAppRepositoryImpl
   - [ ] AutoCleanWorker
   - [ ] Experimental API warnings

---

## 📝 Testing Strategy

After each fix:
```powershell
# Compile to check errors reduced
.\gradlew.bat compileDebugKotlin

# Count remaining errors
# Should decrease after each fix
```

---

## ⏱️ Estimated Time

- **DuplicateViewModel + Screen**: 1-2 hours
- **MessagingViewModel + Screen**: 1-2 hours
- **StorageViewModel + Screen**: 1-2 hours
- **RootMode fixes**: 30 min
- **Minor fixes + testing**: 1 hour

**Total**: ~6-8 hours of focused refactoring

---

## 🎯 Alternative: Skeleton Implementation

If full refactor takes too long, consider creating **simplified stub implementations**:

```kotlin
// Stub ViewModel that compiles but returns mock data
@HiltViewModel
class DuplicateViewModel @Inject constructor(...) : ViewModel() {
    private val _uiState = MutableStateFlow<DuplicateUiState>(
        DuplicateUiState.Success(emptyList())  // Empty list compiles
    )
    val uiState: StateFlow<DuplicateUiState> = _uiState.asStateFlow()
    
    fun scanForDuplicates() {
        // No-op or show toast "Feature under development"
    }
}
```

This allows APK to build while features are developed incrementally.

---

## ✅ Next Steps

1. Review this guide
2. Decide: Full refactor vs Stub implementation vs Find original source
3. If refactoring: Start with Phase 2 (remaining ViewModels)
4. Test compile after each ViewModel fix
5. Move to Phase 3 (Screens) only after ViewModels compile

---

**Last Updated**: Nov 24, 2025  
**Status**: ClassifierViewModel + Screen fixed (2/10 modules)  
**Remaining**: 8 modules, ~150+ errors

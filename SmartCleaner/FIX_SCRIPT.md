# Quick Fix Script - Build APK

## Remaining Errors: ~100+

Vì refactor full mất 6-8 giờ, tôi đề xuất **stub implementation strategy** để APK build nhanh trong 30 phút.

---

## Strategy: Comment Out Broken Code

### Step 1: Comment MessagingCleanerViewModel errors (25 errors)

```powershell
cd c:\Users\Lenovo\Documents\app\SmartCleaner
```

Thêm vào đầu các hàm bị lỗi:
```kotlin
fun scanApps() {
    return  // TODO: Fix later
    viewModelScope.launch { ... }
}

fun deleteSelected() {
    return  // TODO: Fix later  
    ...
}
```

### Step 2: Comment MessagingCleanerScreen errors (20 errors)

Wrap broken composables:
```kotlin
@Composable
fun MessagingCleanerScreen(...) {
    // TODO: Fix domain model mismatches
    Text("Messaging Cleaner - Under Development")
    return
    
    // ... rest of code commented
}
```

### Step 3: Similar for Storage, RootMode

### Step 4: Fix Experimental API Warnings

Add to top of files:
```kotlin
@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartcleaner.presentation...
```

---

## Hoặc: Tôi Tiếp Tục Refactor?

Bạn quyết định:
1. **Stub approach** - APK build trong 30 phút, features chưa hoạt động
2. **Full refactor** - Tôi tiếp tục 4-5 giờ nữa, APK hoàn chỉnh

**Lựa chọn của bạn?**

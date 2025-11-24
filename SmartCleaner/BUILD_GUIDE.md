# 📱 Hướng Dẫn Build APK - SmartCleaner

## 🎯 Mục tiêu
Build file APK từ source code để cài đặt trên điện thoại Android.

---

## ✅ Yêu Cầu Hệ Thống

### 1. Java Development Kit (JDK)
```powershell
# Kiểm tra JDK đã cài
java -version

# Cần: JDK 17 hoặc cao hơn
```

**Nếu chưa có JDK:**
- Download: https://adoptium.net/temurin/releases/
- Chọn: JDK 17 LTS, Windows x64 MSI
- Cài đặt và thêm vào PATH

### 2. Android SDK
**Cách 1: Qua Android Studio (Khuyến nghị)**
- Download: https://developer.android.com/studio
- Cài đặt sẽ tự động cài Android SDK

**Cách 2: Android Command Line Tools**
- Download: https://developer.android.com/studio#command-tools
- Giải nén vào: `C:\Android\cmdline-tools`

### 3. Biến Môi Trường
```powershell
# Thêm vào System Environment Variables:
ANDROID_HOME = C:\Users\<YourUsername>\AppData\Local\Android\Sdk
# Hoặc nơi bạn cài Android SDK

# Thêm vào PATH:
%ANDROID_HOME%\platform-tools
%ANDROID_HOME%\tools
%ANDROID_HOME%\tools\bin
```

---

## 🚀 Bước 1: Chuẩn Bị Project

### Mở PowerShell tại thư mục project:
```powershell
cd c:\Users\Lenovo\Documents\app\SmartCleaner
```

### Kiểm tra Gradle Wrapper:
```powershell
# Kiểm tra file gradlew.bat đã tồn tại
Test-Path .\gradlew.bat

# Nếu FALSE, cần tạo Gradle wrapper
```

---

## 🔧 Bước 2: Khởi Tạo Gradle Wrapper (Nếu Cần)

### Nếu chưa có Gradle wrapper, dùng Android Studio:

**Option A: Dùng Android Studio (Dễ nhất)**
1. Mở Android Studio
2. File > Open > Chọn thư mục `SmartCleaner`
3. Android Studio tự động tạo Gradle wrapper
4. Đợi sync hoàn tất

**Option B: Dùng Gradle đã cài toàn cục**
```powershell
# Nếu đã cài Gradle globally
gradle wrapper --gradle-version 8.4
```

---

## 📦 Bước 3: Build APK

### 1. Build Debug APK (Không cần signing)
```powershell
# Build debug APK
.\gradlew.bat assembleDebug

# APK output:
# app\build\outputs\apk\debug\app-debug.apk
```

**Đặc điểm Debug APK:**
- ✅ Build nhanh
- ✅ Không cần key signing
- ✅ Kích thước lớn hơn
- ⚠️ Chỉ dùng để test
- ⚠️ Hiệu năng chưa tối ưu

### 2. Build Release APK (Cần signing key)

#### Bước 2.1: Tạo Keystore (Lần đầu tiên)
```powershell
# Tạo keystore file
keytool -genkey -v `
  -keystore smartcleaner-release-key.jks `
  -alias smartcleaner `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000

# Nhập thông tin:
# - Password: [nhập password mạnh]
# - Họ tên, tổ chức, thành phố, quốc gia
# - Lưu file .jks này cẩn thận!
```

#### Bước 2.2: Cấu hình Signing
Tạo file `keystore.properties` trong thư mục project:
```properties
storeFile=smartcleaner-release-key.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=smartcleaner
keyPassword=YOUR_KEY_PASSWORD
```

⚠️ **LƯU Ý:** Thêm `keystore.properties` vào `.gitignore`!

#### Bước 2.3: Build Release
```powershell
# Build release APK
.\gradlew.bat assembleRelease

# APK output:
# app\build\outputs\apk\release\app-release.apk
```

**Đặc điểm Release APK:**
- ✅ Tối ưu hiệu năng
- ✅ Kích thước nhỏ (ProGuard/R8)
- ✅ Sẵn sàng publish
- ⚠️ Cần signing key
- ⚠️ Build lâu hơn

---

## 🎯 Bước 4: Cài Đặt APK

### Cách 1: Cài qua ADB (Điện thoại đã kết nối)
```powershell
# Kiểm tra device
adb devices

# Cài debug APK
adb install app\build\outputs\apk\debug\app-debug.apk

# Hoặc cài release APK
adb install app\build\outputs\apk\release\app-release.apk

# Gỡ cài đặt cũ và cài mới
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Cách 2: Copy APK sang điện thoại
```powershell
# Copy qua ADB
adb push app\build\outputs\apk\debug\app-debug.apk /sdcard/Download/

# Hoặc copy thủ công:
# 1. Copy file APK qua USB/Bluetooth/Email
# 2. Trên điện thoại: Mở File Manager
# 3. Tìm file APK và tap để cài
# 4. Cho phép "Install from Unknown Sources" nếu cần
```

### Cách 3: Install trực tiếp qua Gradle
```powershell
# Cài debug build lên device đã kết nối
.\gradlew.bat installDebug

# Hoặc cài release build
.\gradlew.bat installRelease
```

---

## 🧹 Các Lệnh Build Hữu Ích

### Clean Build (Xóa build cũ)
```powershell
# Clean toàn bộ
.\gradlew.bat clean

# Clean và build lại
.\gradlew.bat clean assembleDebug
```

### Build Variants
```powershell
# Xem tất cả tasks có thể chạy
.\gradlew.bat tasks

# Build tất cả variants
.\gradlew.bat assemble

# Build và test
.\gradlew.bat build
```

### Kiểm tra Dependencies
```powershell
# Xem dependency tree
.\gradlew.bat app:dependencies

# Kiểm tra version conflicts
.\gradlew.bat app:dependencyInsight --dependency androidx.core
```

---

## 🐛 Xử Lý Lỗi Thường Gặp

### Lỗi 1: "JAVA_HOME not set"
```powershell
# Set JAVA_HOME tạm thời
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Hoặc thêm vào System Environment Variables
```

### Lỗi 2: "Android SDK not found"
```powershell
# Set ANDROID_HOME
$env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"

# Hoặc tạo local.properties
echo "sdk.dir=C:\\Users\\$env:USERNAME\\AppData\\Local\\Android\\Sdk" > local.properties
```

### Lỗi 3: "Permission denied" khi chạy gradlew
```powershell
# Cấp quyền thực thi
icacls .\gradlew.bat /grant Everyone:F
```

### Lỗi 4: Build quá chậm
```powershell
# Tăng heap size cho Gradle
# Tạo/sửa file gradle.properties:
echo "org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m" >> gradle.properties
echo "org.gradle.parallel=true" >> gradle.properties
echo "org.gradle.caching=true" >> gradle.properties
```

### Lỗi 5: "Execution failed for task ':app:processDebugResources'"
```powershell
# Clean và rebuild
.\gradlew.bat clean
.\gradlew.bat assembleDebug --stacktrace
```

---

## 📊 Kiểm Tra APK Sau Build

### Xem thông tin APK:
```powershell
# Kích thước file
(Get-Item app\build\outputs\apk\debug\app-debug.apk).Length / 1MB

# Xem nội dung APK với apkanalyzer (nếu có Android SDK)
apkanalyzer apk summary app\build\outputs\apk\debug\app-debug.apk
```

### Test APK trước khi phát hành:
```powershell
# Cài đặt và chạy
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.smartcleaner/.MainActivity

# Xem logs
adb logcat | Select-String "SmartCleaner"
```

---

## 🎨 Build APK với Custom Config

### Tạo build variant riêng trong build.gradle.kts:
```kotlin
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isDebuggable = true
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-STAGING"
        }
    }
}
```

Sau đó build:
```powershell
.\gradlew.bat assembleStagingRelease
```

---

## 🚀 Quick Commands (Tóm tắt)

```powershell
# ========== BUILD ==========
# Debug APK (nhanh, không cần key)
.\gradlew.bat assembleDebug

# Release APK (tối ưu, cần key)
.\gradlew.bat assembleRelease

# Clean trước khi build
.\gradlew.bat clean assembleDebug

# ========== INSTALL ==========
# Cài trực tiếp lên device
.\gradlew.bat installDebug

# Cài qua ADB
adb install -r app\build\outputs\apk\debug\app-debug.apk

# ========== CHECK ==========
# Xem devices
adb devices

# Xem logs
adb logcat | Select-String "SmartCleaner"

# Chạy app
adb shell am start -n com.smartcleaner/.MainActivity
```

---

## 📝 Checklist Trước Khi Build Release

- [ ] Đã test đầy đủ trên debug build
- [ ] Code đã commit/backup
- [ ] Version code/name đã tăng trong build.gradle.kts
- [ ] ProGuard rules đã cấu hình đúng
- [ ] Signing key đã tạo và bảo mật
- [ ] keystore.properties đã thêm vào .gitignore
- [ ] Đã test trên nhiều API levels (26-34)
- [ ] Không còn TODO/FIXME trong code production
- [ ] Đã xóa log statements nhạy cảm
- [ ] Icons và resources đã đầy đủ

---

## 🎯 Kết Luận

**Để build APK nhanh nhất:**
1. Mở Android Studio > Open project
2. Đợi Gradle sync xong
3. Chạy: `.\gradlew.bat assembleDebug`
4. APK ở: `app\build\outputs\apk\debug\app-debug.apk`

**Cần hỗ trợ?**
- Xem logs chi tiết: `.\gradlew.bat assembleDebug --stacktrace --info`
- Hoặc mở issue trên GitHub

---

**Generated:** November 24, 2025  
**Version:** 1.0.0  
**Project:** SmartCleaner Android App

# 🎯 HƯỚNG DẪN BUILD APK - SMARTCLEANER
## Các bước cần thực hiện NGAY

---

## ⚠️ HIỆN TẠI: Thiếu Java Development Kit (JDK)

Bạn cần cài đặt JDK trước khi build được APK.

---

## 📋 BƯỚC 1: CÀI ĐẶT JDK 17

### Option A: Cài JDK qua Chocolatey (Nhanh nhất)
```powershell
# Mở PowerShell với quyền Administrator
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Sau khi cài Chocolatey xong:
choco install temurin17 -y

# Đóng và mở lại PowerShell
java -version
```

### Option B: Download và cài thủ công (Khuyến nghị)

1. **Download JDK 17:**
   - Truy cập: https://adoptium.net/temurin/releases/
   - Chọn:
     - Version: **17 - LTS**
     - Operating System: **Windows**
     - Architecture: **x64**
     - Package Type: **JDK**
     - Format: **MSI** (installer)
   
2. **Cài đặt:**
   - Chạy file `.msi` vừa download
   - Click **Next** > **Next**
   - ✅ Chọn: **Add to PATH** (Quan trọng!)
   - ✅ Chọn: **Set JAVA_HOME variable**
   - Click **Install**
   - Chờ hoàn tất

3. **Kiểm tra:**
   ```powershell
   # Đóng PowerShell cũ và mở lại
   java -version
   # Kết quả mong đợi: openjdk version "17.0.x"
   
   echo $env:JAVA_HOME
   # Kết quả mong đợi: C:\Program Files\Eclipse Adoptium\jdk-17.x.x...
   ```

### Option C: Cài qua Android Studio (Tự động)
```
1. Download Android Studio: https://developer.android.com/studio
2. Cài đặt Android Studio
3. Android Studio sẽ tự động cài JDK embedded
4. Mở project trong Android Studio > Build > Build APK(s)
```

---

## 📋 BƯỚC 2: CÀI ĐẶT ANDROID STUDIO (Nếu chưa có)

### Download và Cài đặt:
1. Truy cập: https://developer.android.com/studio
2. Click **Download Android Studio**
3. Chạy installer và làm theo hướng dẫn
4. Cài đặt mặc định (bao gồm Android SDK)

### Cấu hình lần đầu:
```
1. Mở Android Studio
2. Chọn "Standard" installation
3. Đợi download Android SDK components
4. Finish
```

---

## 📋 BƯỚC 3: MỞ PROJECT VÀ BUILD

### Cách 1: Build qua Android Studio (DỄ NHẤT)

```powershell
# 1. Mở Android Studio
# 2. Click "Open"
# 3. Chọn thư mục: c:\Users\Lenovo\Documents\app\SmartCleaner
# 4. Đợi Gradle sync (2-5 phút)
# 5. Build > Build Bundle(s) / APK(s) > Build APK(s)
# 6. Đợi build xong
# 7. Click "locate" để mở folder chứa APK
```

**File APK sẽ ở:**
```
c:\Users\Lenovo\Documents\app\SmartCleaner\app\build\outputs\apk\debug\app-debug.apk
```

### Cách 2: Build qua Command Line (SAU KHI CÀI JDK)

```powershell
cd c:\Users\Lenovo\Documents\app\SmartCleaner

# Build debug APK
.\gradlew.bat assembleDebug

# Nếu lỗi, thử:
.\gradlew.bat clean assembleDebug --stacktrace

# APK output:
# app\build\outputs\apk\debug\app-debug.apk
```

---

## 📋 BƯỚC 4: CÀI ĐẶT APK LÊN ĐIỆN THOẠI

### Cách 1: Qua USB (ADB)
```powershell
# Kết nối điện thoại qua USB
# Bật USB Debugging trên điện thoại:
#   Settings > About Phone > Tap "Build Number" 7 lần
#   Settings > Developer Options > Enable USB Debugging

# Kiểm tra kết nối
adb devices

# Cài APK
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Cách 2: Copy file APK
```powershell
# Copy file sang điện thoại:
# app\build\outputs\apk\debug\app-debug.apk

# Trên điện thoại:
# 1. Mở File Manager
# 2. Tìm file app-debug.apk
# 3. Tap để cài
# 4. Cho phép "Install from Unknown Sources" nếu hỏi
```

---

## 🎯 TÓM TẮT - CHẠY NGAY

### Nếu muốn build NHANH NHẤT:

1. **Cài Android Studio** (bao gồm JDK + Android SDK):
   ```
   Download: https://developer.android.com/studio
   ```

2. **Mở project:**
   ```
   Android Studio > Open > Chọn SmartCleaner folder
   ```

3. **Build APK:**
   ```
   Build menu > Build Bundle(s) / APK(s) > Build APK(s)
   ```

4. **Lấy file APK:**
   ```
   Sau khi build xong, click notification "locate" 
   Hoặc vào: app\build\outputs\apk\debug\app-debug.apk
   ```

---

## 🐛 XỬ LÝ LỖI

### Lỗi: "JAVA_HOME not set"
```powershell
# Cài JDK theo hướng dẫn ở Bước 1
# Sau đó kiểm tra:
java -version
```

### Lỗi: "Android SDK not found"
```powershell
# Cài Android Studio (đã bao gồm Android SDK)
# Hoặc set manually:
$env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
```

### Lỗi: "Gradle sync failed"
```powershell
# Trong Android Studio:
# File > Invalidate Caches / Restart
# Đợi restart xong rồi rebuild
```

### Build quá chậm
```powershell
# Tăng RAM cho Gradle
# Tạo file gradle.properties:
echo "org.gradle.jvmargs=-Xmx4096m" >> gradle.properties
```

---

## 📞 LIÊN HỆ HỖ TRỢ

Nếu gặp lỗi, chạy lệnh sau và gửi kết quả:

```powershell
# Kiểm tra môi trường
Write-Host "=== Java ==="
java -version

Write-Host "`n=== Android SDK ==="
$env:ANDROID_HOME
Test-Path $env:ANDROID_HOME

Write-Host "`n=== Gradle ==="
.\gradlew.bat --version

Write-Host "`n=== ADB ==="
adb version
```

---

## ✅ CHECKLIST

- [ ] Đã cài JDK 17
- [ ] Đã cài Android Studio (hoặc Android SDK)
- [ ] Đã set JAVA_HOME và ANDROID_HOME
- [ ] `java -version` chạy được
- [ ] Đã mở project trong Android Studio
- [ ] Gradle sync thành công
- [ ] Build APK thành công
- [ ] APK file tồn tại trong app\build\outputs\apk\debug\

---

**Next Step:** Sau khi cài JDK, chạy lại lệnh:
```powershell
cd c:\Users\Lenovo\Documents\app\SmartCleaner
.\gradlew.bat assembleDebug
```

Good luck! 🚀

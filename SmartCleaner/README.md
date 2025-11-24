# SmartCleaner - Ứng Dụng Dọn Dẹp Thông Minh cho Android

## 📱 Tổng Quan

SmartCleaner là ứng dụng Android chuyên nghiệp giúp tối ưu hóa bộ nhớ thiết bị bằng cách:
- Quét và dọn sạch tệp còn sót lại sau khi gỡ ứng dụng
- Dọn rác hệ thống (cache, temp files, logs)
- Phân tích ứng dụng không sử dụng
- Tìm và xóa file trùng lặp
- Phân loại rác thông minh bằng ML

## 🎯 Tính Năng Chính

### 1. Quét & Dọn Tệp Còn Sót Lại
- **Mô tả**: Phát hiện các thư mục/file của ứng dụng đã gỡ nhưng còn sót lại
- **Vị trí quét**:
  - `/Android/data/` và `/Android/obb/`
  - `/Download/`
  - `/Pictures/`, `/DCIM/`
  - Thư mục tùy chỉnh của từng ứng dụng
- **Tính năng**:
  - Xem trước file trước khi xóa
  - Backup tự động (optional)
  - Whitelist để bảo vệ dữ liệu quan trọng

### 2. Dọn Rác Hệ Thống
- Cache ứng dụng (qua `PackageManager.clearApplicationUserData()`)
- File tạm (.tmp, .temp)
- File backup (.bak, .backup)
- Log files (.log)
- APK files trong Download
- Large files (>100MB có thể cấu hình)

### 3. Dọn Thư Mục Trống
- Quét toàn bộ storage
- Hiển thị danh sách thư mục trống
- Xóa hàng loạt với xác nhận

### 4. Phân Tích Ứng Dụng Không Dùng
- Sử dụng `UsageStatsManager` (yêu cầu permission)
- Phân loại:
  - Không dùng >30 ngày
  - Không dùng >90 ngày
  - Không bao giờ dùng
- Hiển thị thông tin: kích thước, ngày cài đặt, lần cuối sử dụng

### 5. Tính Năng Nâng Cao

#### 5.1. ML Phân Loại Rác Thông Minh
- **Model**: TensorFlow Lite
- **Input**: File path, extension, size, date
- **Output**: Probability score (safe to delete/keep)
- **Categories**: 
  - Junk (90%+)
  - Maybe junk (50-90%)
  - Important (<50%)

#### 5.2. Tìm File Trùng
- **Hash-based**: MD5/SHA-256 cho tất cả file
- **Image similarity**: Perceptual hash cho ảnh
- **Grouping**: Nhóm các file giống nhau, đề xuất giữ file mới nhất

#### 5.3. Dọn Rác Messaging Apps
- **WhatsApp**: 
  - `/WhatsApp/Media/` (sent images, voice notes)
  - `.Statuses/` (stories)
- **Messenger**: 
  - `/Messenger/` cached media
- **Zalo**: 
  - `/Zalo/` cached files

#### 5.4. Storage Analyzer
- **Visualization**: Sunburst/Treemap chart
- **Breakdown by**:
  - File type
  - App ownership
  - Size categories

#### 5.5. Root Mode
- Xóa `/data/data/<package>/` của app đã gỡ
- Xóa Dalvik cache
- Yêu cầu RootTools/Magisk

### 6. UX Hiện Đại

#### 6.1. Dashboard
- Tổng dung lượng có thể giải phóng
- Biểu đồ phân bổ storage
- Nút **One Tap Clean**
- Quick stats (số file junk, app không dùng, file trùng)

#### 6.2. Lịch Dọn Tự Động
- Sử dụng WorkManager
- Frequency: Daily/Weekly/Monthly
- Tùy chỉnh thời gian
- Notification kết quả

#### 6.3. Dark Mode + Material You
- Adaptive colors theo wallpaper (Android 12+)
- Dark/Light/System theme
- Smooth animations

#### 6.4. Safe Mode
- Checklist trước khi xóa
- Backup tự động
- Undo trong 24h (Recycle Bin)

#### 6.5. Thông Báo Realtime
- Lắng nghe `ACTION_PACKAGE_REMOVED`
- Quét ngay sau khi gỡ app
- Notification với số lượng file còn sót

#### 6.6. Cloud Backup
- Google Drive integration
- Firebase Storage
- Backup trước khi xóa file quan trọng

## 🏗️ Kiến Trúc

### Tech Stack
- **Language**: Kotlin
- **Architecture**: Clean Architecture + MVVM
- **UI**: Jetpack Compose
- **DI**: Hilt/Dagger
- **Storage Access**: Storage Access Framework (SAF)
- **ML**: TensorFlow Lite
- **Charts**: Vico/MPAndroidChart
- **Root**: LibSu

### Layers
```
presentation/      # UI (Compose) + ViewModels
├── dashboard/
├── cleaner/
├── analyzer/
└── settings/

domain/           # Use Cases + Entities
├── usecases/
├── entities/
└── repositories/

data/            # Repositories Implementation + Data Sources
├── repositories/
├── local/
├── remote/
└── ml/

core/            # Utilities
├── scanner/
├── permissions/
├── ml/
└── root/
```

## 📋 Yêu Cầu Hệ Thống

- **Android Version**: 8.0 (API 26) - 15 (API 35)
- **Permissions**:
  - `READ_EXTERNAL_STORAGE` (API < 33)
  - `MANAGE_EXTERNAL_STORAGE` (API 30+, optional)
  - `PACKAGE_USAGE_STATS`
  - `REQUEST_DELETE_PACKAGES`
  - `ACCESS_MEDIA_LOCATION`
  - Root access (optional)

## 🚀 Roadmap

### Phase 1 (MVP)
- ✅ Basic file scanner
- ✅ Cache cleaner
- ✅ Empty folders
- ✅ Dashboard UI

### Phase 2
- ✅ Leftover files detection
- ✅ Usage stats analyzer
- ✅ One Tap Clean

### Phase 3
- ✅ ML classifier
- ✅ Duplicate finder
- ✅ Storage analyzer

### Phase 4
- ✅ Messaging app cleaner
- ✅ Root mode
- ✅ Cloud backup

## 📖 Documentation

Xem thêm:
- [Đặc tả kỹ thuật chi tiết](./docs/TECHNICAL_SPEC.md)
- [UX Flow & Wireframes](./docs/UX_FLOW.md)
- [API Documentation](./docs/API.md)

## 📄 License

MIT License - Copyright (c) 2025 SmartCleaner Team

---
agent: agent
---
Bạn là một Android Senior Engineer (10+ năm kinh nghiệm), chuyên thiết kế ứng dụng tối ưu hóa hệ thống. Hãy tạo toàn bộ mã nguồn và mô tả chi tiết cho một ứng dụng Android có tên: “SmartCleaner”.

Ứng dụng cần hỗ trợ Android 8 – Android 15, viết bằng Kotlin, kiến trúc Clean Architecture + MVVM, UI bằng Jetpack Compose, và cần đạt các yêu cầu sau:

Quét & dọn tệp còn sót lại của ứng dụng đã uninstall (Android/data, OBB, Download… kèm xem trước).

Dọn rác hệ thống: cache, file tạm, file log, file .tmp/.bak, large files >100MB.

Dọn thư mục trống.

Phân tích & đề xuất xoá ứng dụng không dùng lâu (dựa theo Usage Stats).

Tính năng nâng cao:

ML phân loại rác thông minh.

Tìm file trùng (hash + image similarity).

Dọn rác WhatsApp/Messenger/Zalo.

Storage analyzer (Sunburst chart).

Root mode (tùy chọn): xóa thư mục /data/data, Dalvik cache.

UX hiện đại:

Dashboard tổng quan & nút One Tap Clean.

Lịch dọn tự động (WorkManager).

Dark mode + Material You.

Safe mode (chống xoá nhầm).

Thông báo realtime khi ứng dụng bị gỡ nhưng vẫn còn rác.

Cloud backup trước khi xoá (tùy chọn).

Hãy mô tả chi tiết cách hoạt động, input/output của từng tính năng, UX flow và gợi ý kỹ thuật implement bằng Kotlin + Jetpack Compose + SAF.
# [PROJECT_CODE] - [PROJECT_NAME]

## Thành viên

| STT | MSSV | Họ và tên | Vai trò |
| 1 | |Đặng Nguyễn Bình | |
| 2 | |Nguyễn Thế Phú | |
| 3 | |Phan Ký Khôi | |
| 4 | |Nguyễn Văn Đạt  | |
| 5 | |Nguyễn Thanh Liêm | |
| 6 | |Lê Văn Minh Trí | |

## Giới thiệu

Mục tiêu của đề tài là xây dựng ứng dụng Android giúp người dùng quản lý và theo dõi quá trình tập luyện. \
Đối tượng sử dụng là những người muốn lưu trữ và quản lý thông tin các bài tập cá nhân.
Phạm vi đề tài tập trung vào quản lý workout và lưu trữ dữ liệu cục bộ trên thiết bị.

## Kiến trúc hệ thống

- Mô hình: Khác (ứng dụng độc lập-standalone với cơ sở dữ liệu cục bộ)
- Protocol: không sử dụng
- Port mặc định: không sử dụng
- Cấu trúc message: không áp dụng

## Yêu cầu môi trường

- Hệ điều hành: Windows / macOS / Linux
- Ngôn ngữ và phiên bản: Kotlin 2.2.10
- Công cụ: Android Studio, Gradle
- Dependency: Jetpack Compose, Material 3, Room Database, AndroidX Lifecycle/ViewModel, KSP

## Cài đặt

1. Clone repository về máy.
2. Mở project bằng Android Studio.
3. Cài đặt JDK 11 và Android SDK 36.
4. Đồng bộ Gradle để tự động tải các dependency cần thiết.
5. Kết nối thiết bị Android hoặc khởi động Android Emulator.
6. Build project để kiểm tra cấu hình và dependency.

## Hướng dẫn chạy

### Server

```text
Lệnh hoặc các bước chạy Server
```

### Client

```text
Lệnh hoặc các bước chạy Client
```

## Cấu hình

- Ứng dụng không sử dụng kết nối mạng hoặc server nên không cần cấu hình IP, port hay các tham số mạng.
- Các cấu hình chính được thiết lập trong project gồm SDK, JDK và Room Database. Dữ liệu được lưu trữ cục bộ trên thiết bị.

## Chức năng

1. Đăng ký tài khoản
2. Đăng nhập
3. Thêm bài tập
4. Chỉnh sửa bài tập
5. Xóa bài tập
6. Tìm kiếm bài tập
7. Lọc bài tập theo danh mục
8. Hiển thị danh sách bài tập
9. Theo dõi thời lượng và calories
10. Thống kê tổng số bài tập 
11. Lưu trữ dữ liệu bằng Room Database

## Kiểm thử

- Functional test: Kiểm tra đăng ký, đăng nhập, thêm, sửa, xóa, tìm kiếm và lọc bài tập.
- Test dữ liệu không hợp lệ: Kiểm tra thông tin đăng nhập, đăng ký và dữ liệu bài tập không hợp lệ.
- Test mất kết nối: Không áp dụng vì ứng dụng không yêu cầu kết nối mạng.
- Stress test: Kiểm tra ứng dụng khi có số lượng lớn dữ liệu bài tập.
- Performance test: Kiểm tra thời gian phản hồi khi hiển thị, tìm kiếm và thao tác với dữ liệu.

Bằng chứng kiểm thử lưu tại `Extra/`.

## Demo

- Video: [Public hoặc Unlisted URL]
- Slide: `PPTX/`
- Báo cáo: `DOCX/`

## Giới hạn

- Chưa hỗ trợ đồng bộ dữ liệu giữa nhiều thiết bị.
- Chưa có server hoặc cơ sở dữ liệu trực tuyến. 
- Chưa hỗ trợ khôi phục mật khẩu qua email. 
- Chưa có chức năng nhắc lịch tập luyện. 
- Chưa tích hợp với các thiết bị theo dõi sức khỏe. 
- Dữ liệu chỉ được lưu trữ cục bộ trên thiết bị.

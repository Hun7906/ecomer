# ECOMER Hung — Hướng dẫn từ đầu (Java + Cloud Firestore)

## 1. Bản này làm gì?
- Đăng nhập/đăng xuất bằng email và mật khẩu Firebase Authentication.
- Nhập, hiển thị, sửa, xóa sản phẩm trong Cloud Firestore, cập nhật danh sách trực tiếp.
- Chỉ tài khoản có UID quản trị trong Rules được đọc/ghi/xóa dữ liệu.
- Chỉ ba trường sản phẩm: tenSanPham (chuỗi), loaiSanPham (chuỗi), gia (số nguyên VND).
- ID là mã tài liệu Firestore, không phải một trường thêm trong sản phẩm.
- Kiểm tra dữ liệu, xác nhận xóa, xuất JSON dữ liệu thật.
- Không có ảnh, đăng ký hay màn hình user riêng. Đây là phạm vi cơ bản; không cam kết điểm và không coi là hoàn thành toàn bộ phần thưởng phân quyền 1 điểm.

Đề ghi hạn 16h ngày 23/10/2026. Cần nộp link GitHub source, link video demo và CSDL JSON.

## 2. Firebase là gì?
Firebase là dịch vụ trên mạng của Google. Cloud Firestore là CSDL: collection tương tự một nhóm dữ liệu, document tương tự một bản ghi, field là một trường. Firebase Authentication quản lý tài khoản đăng nhập, khác với nơi lưu sản phẩm. Không cần cài Firebase vào Windows hoặc cài SQL Server. Firebase SDK đã khai báo trong Gradle; Android Studio sẽ tải khi Sync.

Dùng Android Studio (không phải tên “Android Developer”). Code chính ở app/src/main/java/com/hung/ecomer/MainActivity.java. Giao diện viết bằng Java thuần nên không có activity_main.xml.

## 3. Giải nén trước, chưa cần chạy
1. Tải ZIP, chuột phải > Extract All. Đặt ở thư mục đơn giản, ví dụ C:\AndroidProjects\ECOMER_Hung.
2. Đúng thư mục dự án là thư mục chứa settings.gradle, build.gradle, app và gradlew.bat. Nếu có hai lớp ECOMER_Hung thì mở lớp bên trong có các tệp này.
3. Đừng sửa package name. Nó cố định là com.hung.ecomer.

## 4. Kết nối dự án Firebase trên trình duyệt
1. Mở https://console.firebase.google.com/ và đăng nhập Google của bạn.
2. Bạn đã có dự án ECOMER-Hung: mở dự án đó, không cần tạo lại.
3. Google Analytics là tùy chọn; có thể tắt để ít bước. Không cần Gemini.
4. Tạo dự án và vào trang quản lý.
5. Chọn biểu tượng Android hoặc Project settings > Your apps > Add app > Android.
6. Android package name: nhập chính xác com.hung.ecomer. Nickname: ECOMER Hung. SHA-1 không cần cho đăng nhập email/mật khẩu bản này.
7. Register app. Download google-services.json.
8. Copy tệp vừa tải vào ECOMER_Hung/app/google-services.json, cùng cấp với app/build.gradle, KHÔNG để ở src hoặc thư mục ngoài cùng.
9. Nếu Windows đổi tên thành google-services (1).json, đổi lại đúng google-services.json. Bật View > File name extensions để tránh .json.json.
10. Phần Add SDK trên Firebase: dự án này đã khai báo sẵn, không dán thêm Gradle từ trang hướng dẫn. Next/Continue to console.

## 5. Bật đăng nhập và tạo một admin
Tên nhóm menu Firebase có thể đổi; tìm mục Authentication (thường ở Build hoặc Security).
1. Authentication > Get started > Sign-in method / Sign-in providers.
2. Chọn Email/Password > bật Email/Password > Save. Không cần email link/passwordless.
3. Tab Users > Add user. Dùng email bạn quản lý và mật khẩu riêng đáp ứng yêu cầu Firebase (thông thường tối thiểu 6 ký tự, có thể có chính sách mạnh hơn).
4. Ghi nhớ email/mật khẩu để đăng nhập app. Không ghi mật khẩu vào code/GitHub/video.
5. Sau khi tạo user, copy User UID (chuỗi định danh tài khoản, không phải email, không phải Project ID). UID sẽ dùng ở bước kế.

## 6. Tạo Cloud Firestore và đặt quyền
1. Vào Firestore Database (thường ở Build hoặc Databases & Storage), KHÔNG chọn Realtime Database.
2. Create database. Nếu hỏi edition, chọn Standard. Database ID để (default).
3. Chọn vị trí thích hợp nếu có, ví dụ Singapore/asia-southeast1; vị trí CSDL không đổi được tùy ý sau khi tạo.
4. Chọn Production mode nếu có lựa chọn; hoàn tất tạo database. Không cần Firebase Storage cho bản không có ảnh này.
5. Mở tệp firestore.rules trong bộ code bằng Notepad/Android Studio.
6. Thay duy nhất THAY_UID_ADMIN_VAO_DAY bằng UID đã copy; giữ dấu nháy đơn hai bên.
7. Copy toàn bộ nội dung tệp vào tab Rules trên Firebase > Publish. Sửa tệp ở máy KHÔNG tự cập nhật Rules trên web, phải dán và Publish.
8. Có thể mất một lúc để Rules áp dụng. Nếu đang mở app, đăng xuất và đăng nhập lại.
9. Tab Data ban đầu trống là bình thường. App sẽ tự tạo collection products khi thêm sản phẩm đầu tiên.

Rules chỉ cho UID admin thao tác, kể cả người khác có tài khoản cũng không được đọc/ghi. Không dùng allow read, write: if true. Trong Rules Playground nên thử: chưa đăng nhập bị từ chối; UID admin được tạo đủ ba trường; UID khác bị từ chối; thêm trường thứ tư hoặc giá âm bị từ chối.

Các dịch vụ dùng trong bài có hạn mức miễn phí; bắt đầu với gói Spark và theo dõi quota trong console. Không cần bật thanh toán để cố thêm chức năng ảnh vào bài này.

## 7. Mở dự án trên Android Studio
1. Nếu chưa có, tải tại https://developer.android.com/studio, cài và dùng Standard setup để tải Android SDK.
2. Ở màn hình chào, chọn Open (hoặc File > Open). Chọn đúng thư mục ECOMER_Hung chứa settings.gradle.
3. Nếu được hỏi tin cậy source, đọc và xác nhận cho dự án vừa tải.
4. Chờ Gradle Sync; cần Internet lần đầu. Không bấm nâng cấp Gradle/AGP hàng loạt.
5. Tools > SDK Manager: cài Android SDK Platform 35 (Android 15); SDK Tools cài Android SDK Build-Tools, Platform-Tools. Chấp nhận licenses nếu được hỏi.
6. File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK: chọn JDK 17. Nếu danh sách chưa có, dùng Download JDK và chọn 17. Project dùng AGP 8.7.3 + Gradle 8.9 + Java 17.
7. File > Sync Project with Gradle Files. Chờ Sync thành công rồi mới chạy.
8. Không cần tự tạo Empty Activity hay paste code vào một dự án Kotlin khác.

## 8. Chọn một cách chạy
### Cách A — Máy ảo
Tools > Device Manager > Create device > chọn Pixel > chọn system image API 34/35 (Google APIs hoặc Google Play) > Download > Finish. Bấm nút Play trong Device Manager. Khi máy ảo vào màn hình chính, chọn thiết bị này trên thanh công cụ Android Studio, chọn cấu hình app, bấm tam giác Run.
Nếu máy ảo quá chậm, dùng điện thoại thật.

### Cách B — Điện thoại Android
Cài đặt điện thoại > Giới thiệu > chạm Build number/Số bản dựng 7 lần để bật Developer options. Bật USB debugging/Gỡ lỗi USB. Cắm USB có truyền dữ liệu, chấp nhận Allow USB debugging trên điện thoại. Chọn điện thoại trong Android Studio và Run. Windows có thể cần driver USB của hãng nếu không thấy máy. Điện thoại phải có Internet.

## 9. Thử app đúng thứ tự
1. Đăng nhập bằng tài khoản tạo ở bước 5.
2. Thêm Áo thun trắng / Áo nam / 150000. Nhập giá không có dấu chấm hoặc phẩy.
3. Thêm Quần jean xanh / Quần nam / 250000.
4. Mở Firebase > Firestore > Data > products: phải thấy hai document, mỗi document đúng ba field; gia phải là number, không phải string.
5. Bấm Sửa ở áo, đổi giá 170000 > Lưu thay đổi. Xem cả app lẫn console cập nhật.
6. Bấm Xóa quần > Hủy: sản phẩm còn. Bấm Xóa lại > Xóa: sản phẩm mất ở cả hai nơi.
7. Tắt/mở app: dữ liệu vẫn còn. Sau đó đăng xuất, đăng nhập lại.
8. Thử bỏ trống tên/loại, nhập giá trống hoặc quá 1 tỷ: app báo lỗi.
9. Giữ Internet khi demo. Firestore có cache; chỉ tin thông báo “Đã lưu trên Firestore” khi thao tác đã được server xác nhận. Mất mạng có thể khiến lệnh chờ, nút lưu bị khóa; kết nối lại để hoàn tất.

## 10. Xuất CSDL JSON để nộp
Bấm “Xuất CSDL JSON” > chọn Downloads/Tải xuống trong hộp thoại lưu > Save/Lưu. Chức năng đọc dữ liệu mới từ server rồi lưu ecomer-csdl.json. Nếu bấm hủy thì không tạo tệp.
Nếu chạy điện thoại, copy tệp bằng USB hoặc gửi cho chính bạn. Nếu chạy máy ảo, dùng Android Studio > Device Explorer > /sdcard/Download, chọn tệp > Save As/Export ra máy tính (tên menu tùy phiên bản).

csdl-mau.json trong ZIP chỉ là ví dụ cấu trúc, chưa được nạp tự động. Firestore console không có nút nhập JSON kiểu Realtime Database. Cách đơn giản là thêm dữ liệu bằng app rồi xuất JSON thật. Tệp JSON xuất ở đây là bản chụp dữ liệu cho bài nộp, không phải managed backup/import của Firestore.

## 11. Lỗi thường gặp
- File google-services.json is missing: đặt đúng ECOMER_Hung/app/google-services.json.
- No matching client found for package: Firebase Android app phải đăng ký com.hung.ecomer; tải lại JSON đúng app.
- PERMISSION_DENIED / Missing or insufficient permissions: Rules chưa Publish, sai UID hoặc tài khoản đăng nhập khác tài khoản admin. Không đổi thành public để chữa lỗi.
- Đăng nhập thất bại: bật Email/Password, tạo user, kiểm tra email/mật khẩu và mạng; xác nhận JSON thuộc cùng Firebase project.
- SDK location not found: mở qua Android Studio, chọn SDK trong SDK Manager; Studio sẽ tạo local.properties. Không copy đường dẫn SDK máy người khác.
- Android Gradle plugin requires Java 17 / Unsupported class file: chọn Gradle JDK 17 rồi Sync lại.
- Failed to find Platform SDK 35: tải Android SDK Platform 35.
- Could not resolve / timeout / Gradle download failed: kiểm tra mạng, tắt Gradle Offline mode rồi Sync lại. Có thể cần đổi mạng nếu dịch vụ Google/Gradle bị chặn.
- Chỉ thấy màn hình đang tải/không có dữ liệu: đọc thông báo trên app, kiểm tra database (default), collection products, Rules và Internet.
- Giá nhập 150.000 bị từ chối: nhập 150000, app tự hiển thị định dạng tiền.

## 12. Hiểu code để trả lời giảng viên
- showLogin(): tạo màn hình đăng nhập, gọi signInWithEmailAndPassword.
- showProducts(): tạo form và đăng ký addSnapshotListener để nhận danh sách.
- saveProduct(): kiểm tra form, thêm dùng document().set(data), sửa dùng document(id).update(data).
- renderList(): vẽ từng sản phẩm, nạp lại form khi sửa, hỏi xác nhận trước khi delete().
- firestore.rules: server kiểm tra UID admin và cấu trúc đúng ba trường; không chỉ giấu nút trên app.
- exportJson(): lấy Source.SERVER và ghi tệp qua hộp thoại Android.
- Giao diện và xử lý ở một Activity để người mới dễ theo dõi. Đây là bài cơ bản, chưa tách kiến trúc nhiều lớp.

Lời giới thiệu demo: “Em xây dựng ECOMER Hung quản lý sản phẩm ở phân hệ admin. Dữ liệu lưu trên Cloud Firestore gồm tên, loại và giá. App có thêm, xem, sửa, xóa; tài khoản quản trị đăng nhập bằng Firebase Authentication và quyền truy cập được kiểm tra bằng UID trong Security Rules.”

## 13. Nộp GitHub và quay demo
1. Tạo repository GitHub theo yêu cầu môn. Có thể dùng GitHub Desktop > Add local repository/Create repository tại thư mục ECOMER_Hung > Publish repository.
2. Source phải có app/src, các tệp Gradle, gradle/wrapper (gồm JAR), firestore.rules, hướng dẫn và JSON dữ liệu xuất thật.
3. Không nộp thư mục build, .gradle, local.properties, mật khẩu hoặc service-account key. .gitignore đã có sẵn. google-services.json được bỏ qua để người khác dùng dự án riêng; README hướng dẫn bổ sung nó. Tệp cấu hình client không phải private service-account key.
4. Nếu giảng viên cần chạy trên chính Firebase này, cung cấp cấu hình client và tài khoản demo qua kênh riêng theo hướng dẫn môn; không đăng mật khẩu lên repository công khai.
5. Video khoảng 3–5 phút: đăng nhập > thêm 2 sản phẩm > mở console chứng minh dữ liệu > sửa > xóa có xác nhận > mở lại > xuất JSON > đăng xuất. Đừng quay mật khẩu hoặc thông tin cá nhân.
6. Kiểm tra link GitHub và link video giảng viên truy cập được. Không chỉ nộp APK.

## 14. Trạng thái kiểm tra và giới hạn
Bộ code được tạo và kiểm tra cấu trúc, XML/JSON và rà soát mã Java thủ công; chưa build APK hoặc chạy máy ảo tại môi trường tạo tệp vì không có Android SDK/JDK compiler. Chưa có google-services.json thật, tài khoản hay Firebase project của bạn. Bạn cần hoàn tất cấu hình rồi thử checklist phần 9 trên Android Studio. Nếu lỗi, gửi nguyên thông báo đầu tiên trong Build Output và ảnh màn hình đang làm để xử lý tiếp.

## Nguồn chính thức
- Firebase Android setup: https://firebase.google.com/docs/android/setup
- Cloud Firestore quickstart: https://firebase.google.com/docs/firestore/quickstart
- Email/password Authentication: https://firebase.google.com/docs/auth/android/password-auth
- Firestore Security Rules: https://firebase.google.com/docs/firestore/security/get-started
- AGP 8.7 (Gradle 8.9, JDK 17): https://developer.android.com/build/releases/agp-8-7-0-release-notes

Bộ dependency được cố định ở phiên bản đã phát hành để tránh thay đổi bất ngờ, không chủ đích dùng bản mới nhất. targetSdk 34 phục vụ bài chạy cục bộ, không chuẩn bị để xuất bản Google Play.

## Lưu ý bản đổi tên Hưng
Tên ứng dụng: ECOMER Hung. Package mới: com.hung.ecomer. Nếu đã đăng ký Android app bằng package cũ, giữ nguyên Firebase project ECOMER-Hung và Add app > Android thêm app mới com.hung.ecomer, rồi tải google-services.json của app mới. Không dùng tệp JSON dành cho package cũ.

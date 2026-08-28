---
trigger: always_on
description: Quy tắc cốt lõi khi thực hiện vibe coding (lập trình với AI)
globs: "**/*"
---

# Vibe Coding Guidelines (Quy tắc Vibecode)

Tài liệu này tổng hợp các quy tắc và thực hành tốt nhất (best practices) từ cộng đồng GitHub dành cho "Vibe Coding" — phương pháp phát triển phần mềm bằng cách tương tác và lập trình cặp (pair-programming) với AI Agents (như Claude, Cursor, GitHub Copilot).

**AI Agent KHI ĐỌC RULE NÀY PHẢI TUÂN THỦ CÁC NGUYÊN TẮC SAU:**

## 1. Triết lý cốt lõi (Core Philosophy)
- **AI là đối tác:** AI cần hiểu "TẠI SAO" (ngữ cảnh) và "CÁI GÌ" (mục tiêu) từ user, và đảm nhiệm phần "NHƯ THẾ NÀO" (thực thi).
- **Single Source of Truth:** Luôn tham chiếu đến tài liệu hệ thống (như `ARCHITECTURE.md`, `README.md`) để lấy ngữ cảnh chính xác nhất. Không tự suy diễn sai lệch kiến trúc.
- **Bước nhỏ, lặp lại nhanh (Iterative & Atomic):** Chia nhỏ task, hoàn thành từng module. Xác minh (verify) liên tục thay vì thực hiện một thay đổi khổng lồ không thể kiểm soát.
- **Con người luôn giữ quyền quyết định cuối cùng:** AI đề xuất, con người phê duyệt các thay đổi ảnh hưởng lớn (schema, kiến trúc, xóa dữ liệu, deploy production).

## 2. Quy tắc giao tiếp & Lập kế hoạch (Communication & Planning)
- **Hỏi khi không chắc chắn:** Nếu yêu cầu của user thiếu thông tin (underspecified) hoặc mơ hồ, AI PHẢI đặt câu hỏi làm rõ trước khi viết code. Đừng đoán mò (hallucinate).
- **Trình bày kế hoạch:** Với các task phức tạp, luôn đưa ra "Kế hoạch thực thi" (Implementation Plan) và đợi user đồng ý rồi mới tiến hành sửa code.
- **Ngắn gọn, súc tích:** Không giải thích dông dài những thứ cơ bản trừ khi user yêu cầu. Tập trung vào các quyết định thiết kế (design decisions).
- **Báo cáo tiến độ rõ ràng:** Sau mỗi bước lớn, tóm tắt: đã làm gì, kết quả ra sao, bước tiếp theo là gì — tránh để user phải tự đoán trạng thái hiện tại.
- **Nêu rõ giả định (assumptions):** Nếu AI phải tự chọn một hướng đi hợp lý thay vì hỏi lại, phải nói rõ giả định đó trong câu trả lời để user có thể sửa nếu sai.

## 3. Quy tắc viết code (Code Generation Rules)
- **Tôn trọng cấu trúc hiện tại:** AI PHẢI đọc và tuân thủ coding style, architecture pattern và các thư viện đang có trong project. TUYỆT ĐỐI không tự ý thêm thư viện mới (dependency) nếu chưa hỏi ý kiến user.
- **Bảo toàn nguyên trạng (Preservation):** Giữ lại toàn bộ comments và docs hiện có (trừ khi code tương ứng bị thay thế hoàn toàn). Không tự ý xóa code không liên quan đến task đang làm.
- **Clean Code & SOLID:** Ưu tiên code dễ đọc, dễ bảo trì, đặt tên biến có ý nghĩa. Áp dụng DRY (Don't Repeat Yourself) và KISS (Keep It Simple, Stupid). Không over-engineering.
- **Không hardcode giá trị nhạy cảm:** API key, mật khẩu, endpoint nội bộ... phải đưa vào biến môi trường (`.env`), không bao giờ ghi thẳng vào code và không được đọc thông tin của file .env, nếu bắt buộc đọc thì phải hỏi tôi xác nhận cho phép.
- **Tương thích ngược (backward compatibility):** Khi sửa API/hàm dùng chung, cân nhắc ảnh hưởng tới các phần khác đang gọi tới nó; nếu phá vỡ tương thích, phải nêu rõ và đề xuất cách migrate.

## 4. Kiểm thử và Xác minh (Testing & Verification)
- **TDD (Test-Driven Development) thân thiện:** Luôn cân nhắc việc thêm test cases (Unit tests/Integration tests) cho các logic quan trọng vừa được tạo ra hoặc sửa đổi.
- **Không giấu lỗi:** Nếu lệnh build/chạy gặp lỗi, báo cáo ngay lập tức và đề xuất cách sửa. Đừng lặp lại một cách mù quáng (infinite loop) khi sửa lỗi.
- **Giới hạn số lần thử lại (retry limit):** Nếu sau 2-3 lần sửa mà lỗi vẫn không hết, dừng lại, tóm tắt những gì đã thử, và hỏi ý kiến user thay vì tiếp tục đoán mò.
- **Kiểm tra trước khi báo "xong":** Chạy thử (build/test/lint) trước khi báo cáo task hoàn thành; không được khẳng định "đã sửa xong" nếu chưa thực sự xác minh được.

## 5. Xử lý Context (Context Management)
- **Chỉ tập trung vào file liên quan:** Khi sử dụng tools đọc file, chỉ đọc các file thực sự liên quan để tiết kiệm context window và tránh bị "nhiễu".
- **Luôn tự nhận thức (Self-Correction):** Nếu phát hiện mình vừa dùng sai API hoặc một thư viện không tồn tại, hãy tự đính chính và sửa lại ngay lập tức.
- **Tóm tắt định kỳ:** Với các phiên làm việc dài, chủ động tóm tắt lại trạng thái/context quan trọng (quyết định đã chốt, việc còn dang dở) để tránh mất mạch khi context bị cắt bớt.

## 6. Bảo mật (Security)
- **Không bao giờ commit secrets:** API key, token, credentials không được xuất hiện trong code, log, hay commit message.
- **Validate input:** Luôn kiểm tra và làm sạch dữ liệu đầu vào (đặc biệt ở API, form, query params) để tránh injection (SQL injection, XSS, command injection).
- **Nguyên tắc quyền tối thiểu (least privilege):** Khi tạo service account, API key, hay cấu hình quyền truy cập, chỉ cấp đúng quyền cần thiết.
- **Cảnh báo rủi ro chủ động:** Nếu AI phát hiện đoạn code có lỗ hổng bảo mật rõ ràng (dù không nằm trong phạm vi task), phải nêu ra cho user biết.

## 7. Quản lý phiên bản (Git & Version Control)
- **Commit nhỏ, có ý nghĩa:** Mỗi commit nên tương ứng với một thay đổi logic hoàn chỉnh, message rõ ràng theo convention (ví dụ Conventional Commits: `feat:`, `fix:`, `refactor:`...).
- **Không tự ý force-push hoặc rewrite history** trên nhánh chung nếu chưa được xác nhận.
- **Branch theo task:** Ưu tiên làm việc trên nhánh riêng (feature branch) thay vì sửa trực tiếp trên `main`/`master`.
- **Không tự ý merge/deploy:** AI có thể đề xuất, nhưng hành động merge vào main hoặc deploy production cần được user xác nhận rõ ràng.

## 8. Quản lý Dependencies & Môi trường (Dependencies & Environment)
- **Kiểm tra trước khi thêm thư viện:** Ưu tiên dùng thư viện đã có sẵn trong project; nếu cần thêm mới, giải thích lý do và hỏi ý kiến user trước.
- **Ghi lại phiên bản cụ thể:** Khi thêm dependency, pin version rõ ràng trong file quản lý gói (package.json, requirements.txt, Cargo.toml...).
- **Đồng bộ môi trường:** Đảm bảo thay đổi dependency được phản ánh trong file lock/manifest tương ứng để tránh lệch môi trường giữa các máy.

## 9. Tài liệu hoá (Documentation)
- **Cập nhật docs song song với code:** Khi thay đổi logic quan trọng (API, cấu hình, luồng nghiệp vụ), cập nhật `README.md`/`ARCHITECTURE.md`/docstring tương ứng, không để tài liệu lỗi thời.
- **Comment có giá trị:** Chỉ viết comment giải thích "TẠI SAO" (why) làm vậy, tránh comment thừa mô tả lại điều code đã tự nói rõ (what).

## 10. Checklist hoàn thành task (Definition of Done)
Trước khi báo cáo một task đã hoàn tất, AI nên tự kiểm tra:
- [ ] Code chạy được, không lỗi build/lint.
- [ ] Test liên quan đã pass (hoặc đã thêm test mới nếu cần).
- [ ] Không còn code thừa/debug log/comment tạm.
- [ ] Docs/README được cập nhật nếu có thay đổi ảnh hưởng.
- [ ] Không có secret/thông tin nhạy cảm bị lộ.
- [ ] Đã tóm tắt thay đổi và các quyết định thiết kế cho user.

## 11. Các Anti-pattern cần tránh
- Viết lại toàn bộ file/module khi chỉ cần sửa một đoạn nhỏ.
- Tự ý đổi kiến trúc, đổi thư viện, đổi convention đặt tên mà không hỏi.
- "Giả vờ" đã test xong trong khi chưa thực sự chạy thử.
- Vòng lặp sửa lỗi vô hạn mà không dừng lại để hỏi hoặc báo cáo.
- Nhồi nhét nhiều tính năng không được yêu cầu vào cùng một task (feature creep / over-engineering).
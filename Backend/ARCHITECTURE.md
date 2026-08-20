# ARCHITECTURE.md – Auction Platform Backend

> **Mục đích:** Tài liệu bắt buộc đọc trước khi viết bất kỳ dòng code nào.  
> AI code generator và developer đều PHẢI tuân thủ 100% các quy tắc trong file này.

---

## 1. Tổng Quan Kiến Trúc

Dự án áp dụng **Domain-Driven Design (DDD)** kết hợp **Hexagonal Architecture (Ports & Adapters)**.

```
┌─────────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                         │
│         REST Controllers · Request DTOs · Response Mappers   │
├─────────────────────────────────────────────────────────────┤
│                   APPLICATION LAYER                          │
│         Use Cases / Services · Response DTOs · Mappers       │
│         Port/In (driven) · Port/Out (driving)                │
├─────────────────────────────────────────────────────────────┤
│                    DOMAIN LAYER ← TRUNG TÂM                 │
│         Entities · Value Objects · Domain Events             │
│         Domain Services · Domain Exceptions                  │
├─────────────────────────────────────────────────────────────┤
│                 INFRASTRUCTURE LAYER                         │
│         JPA Repositories · External APIs · Config            │
│         Messaging Adapters · Payment Gateways                │
└─────────────────────────────────────────────────────────────┘
```

**Quy tắc cốt lõi:** Dependencies chỉ đi từ ngoài vào trong:

```
Presentation → Application → Domain ← Infrastructure
```

Domain layer KHÔNG được import bất kỳ class nào từ Spring. JPA annotations (`@Entity`, `@Table`, `@Column`) là ngoại lệ duy nhất vì entity cần mapping.

---

## 2. Cấu Trúc Module

Dự án được tổ chức thành **8 modules nghiệp vụ** + **1 shared module**:

```
Backend/src/main/java/com/ecommerce/auctionplatform/
│
├── shared/              # Dùng chung, không thuộc module nào
├── auth/                # Authentication & Authorization
├── user/                # User profile, KYC, Reputation
├── product/             # Product, Category, Image
├── notification/        # Push notifications, WebSocket
├── auction/             # Auction lifecycle, Bidding, Scheduling
├── payment/             # Wallet, Order, Transaction, VNPay, MoMo
├── dispute/             # Dispute resolution
└── config/              # Spring Security, Redis, WebSocket config
```

### 2.1 Cấu Trúc Bên Trong Mỗi Module

```
{module}/
│
├── domain/
│   ├── model/              # JPA Entities – Pure domain objects
│   ├── enums/              # Domain enumerations
│   ├── event/              # Domain Events (implements DomainEvent)
│   ├── exception/          # Domain-specific exceptions
│   ├── repository/         # Repository INTERFACES (ports out)
│   ├── service/            # Domain Services (business invariants)
│   └── valueobject/        # Value Objects (immutable)
│
├── application/
│   ├── service/            # Application Services (use case orchestration)
│   ├── dto/
│   │   ├── response/       # Response DTOs (API output – FROZEN CONTRACT)
│   │   ├── command/        # Command objects (write operations)
│   │   └── query/          # Query objects (read operations)
│   ├── mapper/             # MapStruct mappers (Entity <-> DTO)
│   └── port/
│       ├── in/             # Input ports (use case interfaces)
│       └── out/            # Output ports (infra contracts)
│
├── infrastructure/
│   ├── persistence/
│   │   ├── repository/     # Spring Data JPA implementations
│   │   ├── entity/         # JPA-specific entities (if separate from domain)
│   │   └── mapper/         # DB entity <-> Domain model mappers
│   ├── external/           # External API adapters (Cloudinary, JWT, Payment)
│   ├── messaging/          # WebSocket, Event listeners
│   └── config/             # Module-specific configuration
│
└── presentation/
    ├── rest/               # @RestController classes
    ├── dto/
    │   ├── request/        # Request DTOs (API input – FROZEN CONTRACT)
    │   └── response/       # Presentation-layer response wrappers
    ├── mapper/             # Request DTO <-> Command/Domain mapper
    └── advice/             # Module-specific exception handlers
```

---

## 3. Shared Module

```
shared/
├── domain/event/           DomainEvent.java          # Marker interface
├── application/event/      DomainEventPublisher.java # Port for publishing events
├── infrastructure/
│   ├── event/              SpringDomainEventPublisher.java
│   └── utils/              SecurityUtils.java · PaymentUtils.java
└── presentation/
    ├── advice/             AppException.java · ErrorCode.java · GlobalExceptionHandle.java
    └── response/           APIResponse.java           # Standard API wrapper
```

**Quy tắc shared module:**
- Chỉ chứa code dùng chung THỰC SỰ (≥3 modules cùng dùng)
- Không chứa business logic của bất kỳ module nào
- Modules khác có thể import từ `shared/` nhưng `shared/` KHÔNG được import từ module nghiệp vụ

---

## 4. Luồng Xử Lý Request

### 4.1 Luồng đọc (Read – GET)

```
HTTP GET Request
    │
    ▼
[Presentation] @RestController
    │  Nhận @RequestParam / @PathVariable
    │  Validate input (@Valid)
    │
    ▼
[Application] ApplicationService
    │  Gọi repository hoặc domain service
    │  Map domain entity → ResponseDTO
    │
    ▼
[Infrastructure] JpaRepository → Database
    │
    ▼
[Application] Map Entity → ResponseDTO
    │
    ▼
[Presentation] Wrap vào APIResponse<T>
    │
    ▼
HTTP Response (JSON)
```

### 4.2 Luồng ghi (Write – POST/PUT/DELETE)

```
HTTP Request (RequestDTO)
    │
    ▼
[Presentation] Controller
    │  Validate @Valid
    │  Gọi ApplicationService với RequestDTO hoặc Command object
    │
    ▼
[Application] ApplicationService
    │  Load domain entities từ repository
    │  Gọi domain behavior method
    │  Publish DomainEvent (nếu có side effects)
    │  Persist qua repository
    │  Map → ResponseDTO
    │
    ▼
[Domain] Entity.behavior() / DomainService
    │  Thực thi business rule (KHÔNG biết gì về Spring/DB)
    │
    ▼
[Infrastructure] JpaRepository.save()
    │
    ▼
HTTP Response (ResponseDTO wrapped in APIResponse<T>)
```

### 4.3 Luồng Domain Event (Side Effects)

```
[Application] AuctionService.endAuction()
    │  event = new AuctionEndedEvent(auctionId, winnerId)
    │  domainEventPublisher.publish(event)
    │
    ▼
[Infrastructure] SpringDomainEventPublisher
    │  applicationEventPublisher.publishEvent(event)
    │
    ▼
[Notification] AuctionEventListener (@EventListener)
    │  notificationService.sendWinnerNotification(...)
    │
    ▼
[Payment] WalletEventListener (@EventListener)
    │  walletService.releaseDeposit(...)
```

---

## 5. API Contract – QUY TẮC BẤT BIẾN

> **CRITICAL:** Field names trong Request/Response DTO KHÔNG ĐƯỢC THAY ĐỔI.  
> Frontend đã map với những field này. Thay đổi = breaking change.

### Request DTOs (input)
- Vị trí: `{module}/presentation/dto/request/`
- Package: `com.ecommerce.auctionplatform.{module}.presentation.dto.request`
- **Không thêm/xóa/đổi tên field** nếu API đang production
- Thêm field mới: phải có `default value` hoặc `@JsonProperty(required = false)`

### Response DTOs (output)
- Vị trí: `{module}/application/dto/response/`
- Package: `com.ecommerce.auctionplatform.{module}.application.dto.response`
- **Không xóa/đổi tên field** đang được dùng
- Thêm field mới: OK (backward compatible)

### APIResponse wrapper – BẮT BUỘC

Tất cả API endpoint đều phải trả về `APIResponse<T>`:

```java
return APIResponse.<MyResponseDTO>builder()
    .status(HttpStatus.OK.value())
    .message("Mô tả kết quả")
    .result(data)
    .build();
```

---

## 6. Cross-Module Dependencies

| Module | Được phép import từ |
|--------|---------------------|
| `shared` | Không import từ module nào |
| `auth` | `shared`, `user` (domain model, repo) |
| `user` | `shared`, `payment` (Wallet model) |
| `product` | `shared` |
| `notification` | `shared`, `user` (domain model) |
| `auction` | `shared`, `user`, `product`, `payment`, `notification` |
| `payment` | `shared`, `user`, `auction` |
| `dispute` | `shared`, `user`, `payment`, `product`, `notification`, `auction` |

**Quy tắc import cụ thể:**
- Chỉ import từ: `domain/model/`, `domain/enums/`, `application/dto/response/`, `infrastructure/persistence/repository/`
- **KHÔNG** import `application/service/` của module khác → dùng Domain Events hoặc Port/Out interface
- **KHÔNG** import `presentation/` của module khác

---

## 7. Package Naming Convention

```
com.ecommerce.auctionplatform.{module}.{layer}.{sublayer}

Ví dụ:
  com.ecommerce.auctionplatform.auction.domain.model.Auction
  com.ecommerce.auctionplatform.auction.application.service.AuctionService
  com.ecommerce.auctionplatform.auction.infrastructure.persistence.repository.AuctionRepository
  com.ecommerce.auctionplatform.auction.presentation.rest.AuctionController
  com.ecommerce.auctionplatform.auction.presentation.dto.request.AuctionCreationRequest
  com.ecommerce.auctionplatform.auction.application.dto.response.AuctionResponse
```

---

## 8. Naming Conventions

| Loại class | Convention | Ví dụ |
|------------|-----------|-------|
| Entity (JPA) | `PascalCase` | `Auction`, `AuctionRecord` |
| Repository | `{Entity}Repository` | `AuctionRepository` |
| Application Service | `{Resource}Service` | `AuctionService`, `WalletService` |
| Domain Service | `{Resource}DomainService` | `AuctionDomainService` |
| External Adapter | `{Name}Adapter` | `CloudinaryAdapter` |
| Payment Gateway | `{Provider}GatewayAdapter` | `VNPayGatewayAdapter` |
| Request DTO | `{Action}{Resource}Request` | `AuctionCreationRequest` |
| Response DTO | `{Resource}Response` | `AuctionResponse` |
| Domain Event | `{Resource}{Verb}Event` | `AuctionEndedEvent` |
| Controller | `{Resource}Controller` | `AuctionController` |
| Mapper | `{Resource}Mapper` | `UserMapper` |
| Enum type | `PascalCase` | `AuctionStatus` |
| Enum value | `UPPER_SNAKE_CASE` | `IN_PROGRESS` |

---

## 9. Quy Tắc AI Code Generation – NGHIÊM CẤM

### Danh sách ĐỎ (DDD Violations – AI không được làm)

```java
// 1. KHÔNG đặt @Service vào domain model
@Service                     // ← VI PHẠM
public class Auction { ... }

// 2. KHÔNG để Controller gọi Repository trực tiếp
@RestController
public class AuctionController {
    @Autowired AuctionRepository repo; // ← PHẢI qua ApplicationService
}

// 3. KHÔNG để Domain entity gọi infra/Spring
public class Auction {
    public void save() {
        jpaRepository.save(this); // ← Domain không biết JPA save
    }
}

// 4. KHÔNG đặt Response DTO trong domain/model/
// domain/model/AuctionResponse.java ← VI PHẠM

// 5. KHÔNG import Spring từ domain layer
// Trong domain/model/Auction.java:
import org.springframework.data.jpa.repository.JpaRepository; // ← VI PHẠM

// 6. KHÔNG đổi tên field DTO đang production
public class AuctionResponse {
    String auctionTitle; // ← nếu field cũ là "name" phải giữ "name"
}

// 7. KHÔNG import ApplicationService của module khác
// Trong AuctionService.java:
import com.ecommerce.auctionplatform.payment.application.service.OrderService; // ← Dùng DomainEvent

// 8. KHÔNG đặt business logic trong Controller
@PostMapping("/bid")
public APIResponse<BidResponse> bid(@RequestBody BidRequest req) {
    if (req.getAmount().compareTo(BigDecimal.ZERO) <= 0) { // ← Thuộc Service/Domain
        throw new AppException(ErrorCode.INVALID_AMOUNT);
    }
}
```

### Danh sách XANH (Patterns chuẩn)

```java
// 1. Domain entity thuần túy – không phụ thuộc Spring
// File: auction/domain/model/Auction.java
@Entity
@Table(name = "auctions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Auction {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    BigDecimal currentPrice;
    AuctionStatus status;

    // Business behavior – không gọi repo, không gọi Spring
    public boolean canAcceptBid(BigDecimal bidAmount) {
        return status == AuctionStatus.ACTIVE
            && bidAmount.compareTo(currentPrice) > 0;
    }
}

// 2. Application Service orchestrate
// File: auction/application/service/AuctionService.java
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuctionService {
    AuctionRepository auctionRepository;

    @Transactional
    public BidResponse placeBid(UUID auctionId, BidRequest request) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));

        if (!auction.canAcceptBid(request.getAmount())) {   // Gọi domain behavior
            throw new AppException(ErrorCode.INVALID_BID_AMOUNT);
        }

        auction.setCurrentPrice(request.getAmount());
        auctionRepository.save(auction);

        return BidResponse.builder()
            .auctionId(auctionId)
            .amount(request.getAmount())
            .build();
    }
}

// 3. Controller chỉ delegate, không chứa logic
// File: auction/presentation/rest/AuctionController.java
@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {
    final AuctionService auctionService;

    @PostMapping("/{id}/bids")
    public APIResponse<BidResponse> placeBid(
            @PathVariable UUID id,
            @RequestBody @Valid BidRequest request) {
        return APIResponse.<BidResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Bid placed successfully")
            .result(auctionService.placeBid(id, request))
            .build();
    }
}
```

---

## 10. Thêm API / Tính Năng Mới

### Step-by-step checklist

**Ví dụ:** Thêm `GET /api/auctions/{id}/participants`

```
Step 1 – Xác định module
  → Liên quan đến Auction → module auction/

Step 2 – Xác định cần tạo gì
  → Query (read-only) → không cần Command object

Step 3 – Tạo Response DTO (nếu chưa có)
  → auction/application/dto/response/AuctionParticipantResponse.java

Step 4 – Thêm method vào ApplicationService
  → auction/application/service/AuctionService.java
  → public List<AuctionParticipantResponse> getParticipants(UUID auctionId)

Step 5 – Thêm query vào Repository (nếu cần)
  → AuctionRegistrationRepository.java
  → List<AuctionRegistration> findByAuctionId(UUID auctionId);

Step 6 – Thêm endpoint vào Controller
  → AuctionController.java
  → @GetMapping("/{id}/participants")

Step 7 – Verify
  → mvn compile → BUILD SUCCESS
```

### Template Entity mới trong module hiện có

```java
// domain/model/{NewEntity}.java
package com.ecommerce.auctionplatform.{module}.domain.model;

@Entity
@Table(name = "{table_name}")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class {NewEntity} {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    // fields...
}

// infrastructure/persistence/repository/{NewEntity}Repository.java
package com.ecommerce.auctionplatform.{module}.infrastructure.persistence.repository;

@Repository
public interface {NewEntity}Repository extends JpaRepository<{NewEntity}, UUID> { }

// application/dto/response/{NewEntity}Response.java
package com.ecommerce.auctionplatform.{module}.application.dto.response;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class {NewEntity}Response {
    // FROZEN: không thay đổi sau khi API live
}
```

---

## 11. Thêm Module Mới

**Tạo cấu trúc thư mục:**

```bash
BASE=Backend/src/main/java/com/ecommerce/auctionplatform
MODULE=review   # đổi tên module ở đây

mkdir -p $BASE/$MODULE/{domain/{model,enums,event,exception,repository,service,valueobject},application/{service,dto/{response,command,query},mapper,port/{in,out}},infrastructure/{persistence/{repository,entity,mapper},external,messaging,config},presentation/{rest,dto/{request,response},mapper,advice}}
```

**Checklist:**
- [ ] Tạo đầy đủ 4 layer directories
- [ ] Entity trong `domain/model/` với `@Entity`, `@Table`
- [ ] Repository trong `infrastructure/persistence/repository/`
- [ ] Application Service trong `application/service/`
- [ ] Request DTO trong `presentation/dto/request/` – define contract ngay từ đầu
- [ ] Response DTO trong `application/dto/response/`
- [ ] Controller trong `presentation/rest/`
- [ ] Cập nhật bảng Cross-Module Dependencies (Section 6)
- [ ] `mvn compile` → BUILD SUCCESS

---

## 12. Database Migration

### Thêm column vào table hiện có

```java
// 1. Thêm field vào Entity (nullable = true cho migration safe)
@Column(name = "new_column", nullable = true)
String newColumn;
```

```sql
-- 2. Flyway migration file: db/migration/V{N}__{description}.sql
ALTER TABLE {table_name} ADD COLUMN new_column VARCHAR(255);
```

- Nếu field xuất hiện trong Response → thêm vào Response DTO (backward compatible)  
- Nếu field là input → thêm vào Request DTO với default value

### Tạo bảng mới

```sql
-- db/migration/V{N}__create_{module}_tables.sql
CREATE TABLE {table_name} (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### Đổi tên column (Production – 2 phases)

```sql
-- Phase 1: Add new, copy, keep old
ALTER TABLE auctions ADD COLUMN new_name VARCHAR(255);
UPDATE auctions SET new_name = old_name;

-- Phase 2 (deploy sau, khi FE đã migrate): Drop old
ALTER TABLE auctions DROP COLUMN old_name;
```

---

## 13. Nâng Phiên Bản / Chuyển Framework

### Nâng Spring Boot version

```bash
# 1. Tạo nhánh riêng
git checkout -b chore/upgrade-spring-boot-{version}

# 2. Sửa pom.xml
# <version>{new-version}</version>

# 3. Kiểm tra dependency
mvn dependency:tree | grep -E "WARN|ERROR"

# 4. Fix breaking changes theo Spring Boot Migration Guide
#    javax.* → jakarta.*  (đã áp dụng trong dự án này)
#    WebSecurityConfigurerAdapter → SecurityFilterChain (đã áp dụng)

# 5. Verify
mvn compile && mvn test
```

### Thay thế thư viện (ví dụ: Cloudinary → S3)

```
1. Tạo Port interface trong application/port/out/
   → UploadImagePort.java

2. Adapter cũ implements port
   → CloudinaryAdapter implements UploadImagePort

3. Adapter mới implements cùng port
   → S3Adapter implements UploadImagePort

4. ApplicationService inject UploadImagePort (không đổi code service)

5. Config @Primary hoặc application.properties để chọn adapter
```

### Chuyển Database

```
1. Domain layer: KHÔNG thay đổi gì
2. Tạo repository implementations mới trong infrastructure/
3. Đổi dependency trong pom.xml
4. Tạo migration scripts tương thích DB mới
5. Cập nhật application.properties
```

---

## 14. Thêm Payment Gateway Mới

**Ví dụ:** Thêm ZaloPay

```
1. payment/infrastructure/external/ZaloPayGatewayAdapter.java
2. payment/infrastructure/config/ZaloPayConfig.java  (nếu cần)
3. application.properties:
   zalopay.app-id=...
   zalopay.key1=...
4. Inject vào Controller/Service
5. KHÔNG sửa Request/Response DTO – chỉ thêm routing logic
```

---

## 15. Prompt Template Cho AI

Khi yêu cầu AI generate code, LUÔN dùng template sau:

```
Thêm chức năng [MÔ TẢ] vào module [MODULE] của Auction Platform.

Kiến trúc: DDD + Hexagonal. Tuân thủ ARCHITECTURE.md.

Module đích: [module name]
Layer cần tạo/sửa:
  - domain/model: [Entity mới nếu có]
  - application/service: [method mới]
  - infrastructure/persistence/repository: [query mới]
  - presentation/rest: [endpoint mới]

Request DTO fields (FROZEN – không thay đổi):
  - [field1]: [type]
  - [field2]: [type]

Response DTO fields (FROZEN):
  - [field1]: [type]
  - [field2]: [type]

Package prefix: com.ecommerce.auctionplatform.[module]
Giữ nguyên API contract. KHÔNG import ApplicationService của module khác.
```

### AI Self-Checklist Trước Khi Output Code

- [ ] Đúng package path?
- [ ] Domain entity không import Spring?
- [ ] Controller không gọi Repository trực tiếp?
- [ ] Request/Response DTO fields không thay đổi?
- [ ] Cross-module import theo Section 6?
- [ ] Tất cả endpoint wrap `APIResponse<T>`?
- [ ] `@Transactional` ở Application Service?
- [ ] Business logic ở Service/Domain (không phải Controller)?

---

## 16. Quick Reference

### Import paths hay dùng

```java
// Shared – dùng ở mọi module
import com.ecommerce.auctionplatform.shared.presentation.advice.AppException;
import com.ecommerce.auctionplatform.shared.presentation.advice.ErrorCode;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.shared.infrastructure.utils.SecurityUtils;

// User domain
import com.ecommerce.auctionplatform.user.domain.model.User;
import com.ecommerce.auctionplatform.user.domain.enums.PredefinedRole;
import com.ecommerce.auctionplatform.user.infrastructure.persistence.repository.UserRepository;

// Auction domain
import com.ecommerce.auctionplatform.auction.domain.model.Auction;
import com.ecommerce.auctionplatform.auction.domain.enums.AuctionStatus;

// Payment domain
import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import com.ecommerce.auctionplatform.payment.domain.model.Order;
import com.ecommerce.auctionplatform.payment.domain.enums.WalletStatus;
```

### Annotation Templates

```java
// Entity
@Entity @Table(name = "{table}") @Data @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class {Entity} { ... }

// Application Service
@Service @RequiredArgsConstructor @Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class {Name}Service { ... }

// Controller
@RestController @RequestMapping("/api/{resource}")
@RequiredArgsConstructor
public class {Name}Controller { ... }
```

---

*Cập nhật lần cuối: 2026-08-20 | Phiên bản: 1.0*  
*Bất kỳ thay đổi kiến trúc nào cũng PHẢI cập nhật file này.*

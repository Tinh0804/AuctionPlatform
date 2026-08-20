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

## 2.2 Chi Tiết Từng Subfolder – Mục Đích & Quy Tắc

### `domain/model/` – JPA Entities

**Mục đích:** Trung tâm hệ thống. Chứa JPA entities đại diện cho các đối tượng nghiệp vụ.  
**Chứa gì:** `Auction.java`, `Bid.java`, `AuctionRecord.java`,...  
**Quy tắc:**
- CHỈ import: Java stdlib, Lombok, Jakarta Persistence (`@Entity`, `@Table`, `@Column`, `@ManyToOne`)
- KHÔNG import: Spring beans, ApplicationService, Repository
- Có thể chứa **behavior methods** (business rule thuần túy, không gọi DB)

```java
// auction/domain/model/Auction.java
@Entity @Table(name = "auctions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Auction {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne @JoinColumn(name = "user_id")
    User user;          // import: user.domain.model.User ✅

    BigDecimal currentPrice;
    AuctionStatus status;   // import: auction.domain.enums.AuctionStatus ✅

    // Behavior method – business rule, không gọi repo, không gọi Spring
    public boolean canAcceptBid(BigDecimal bidAmount) {
        return status == AuctionStatus.ACTIVE && bidAmount.compareTo(currentPrice) > 0;
    }
    public boolean isExpired() { return LocalDateTime.now().isAfter(endTime); }
}
```

---

### `domain/enums/` – Domain Enumerations

**Mục đích:** Định nghĩa các trạng thái, loại thuộc domain logic.  
**Chứa gì:** `AuctionStatus.java`, `AuctionRecordStatus.java`, `RegistrationStatus.java`,...  
**Quy tắc:**
- Enum thuần Java – KHÔNG có Spring annotation
- Đặt tên theo khái niệm nghiệp vụ (không phải implementation)
- Module khác import qua `{module}.domain.enums.{Name}`

```java
// auction/domain/enums/AuctionStatus.java
public enum AuctionStatus {
    PENDING,    // Chờ admin duyệt
    APPROVED,   // Đã duyệt, chờ giờ mở
    ACTIVE,     // Đang diễn ra
    EXTENDED,   // Đang gia hạn
    CLOSED,     // Kết thúc bình thường
    CANCELLED,  // Bị hủy
    FAILED      // Không có người thắng
}
```

---

### `domain/event/` – Domain Events

**Mục đích:** Ghi lại "điều gì đó đã xảy ra" trong domain để các module khác phản ứng mà không cần biết nhau.  
**Chứa gì:** `AuctionEndedEvent.java`, `BidPlacedEvent.java`,...  
**Quy tắc:**
- Implement `DomainEvent` từ `shared/domain/event/`
- Immutable: dùng Java `record` hoặc Lombok `@Value`
- KHÔNG chứa logic – chỉ chứa data
- ApplicationService `publish()`, module khác `@EventListener`

```java
// auction/domain/event/AuctionEndedEvent.java
public record AuctionEndedEvent(
    UUID auctionId, UUID winnerId, BigDecimal finalPrice, LocalDateTime occurredOn
) implements DomainEvent {
    public AuctionEndedEvent(UUID auctionId, UUID winnerId, BigDecimal finalPrice) {
        this(auctionId, winnerId, finalPrice, LocalDateTime.now());
    }
}
// Cách dùng trong service:
// domainEventPublisher.publish(new AuctionEndedEvent(id, winnerId, price));
// Cách nhận trong notification module:
// @EventListener public void onAuctionEnded(AuctionEndedEvent event) { ... }
```

---

### `domain/exception/` – Domain Exceptions

**Mục đích:** Exception đặc thù của module khi cần mang thêm context data.  
**Quy tắc:**
- Hầu hết dùng `AppException(ErrorCode.XXX)` từ `shared/` là đủ
- Chỉ tạo exception riêng khi cần embed thêm data (e.g., conflicting entity id)

```java
// Thông thường – đủ dùng:
throw new AppException(ErrorCode.AUCTION_NOT_FOUND);

// Trường hợp đặc biệt cần thêm data:
// auction/domain/exception/AuctionConflictException.java
public class AuctionConflictException extends RuntimeException {
    private final UUID conflictingAuctionId;
    public AuctionConflictException(UUID id) { this.conflictingAuctionId = id; }
}
```

---

### `domain/repository/` – Repository Port Interfaces

**Mục đích:** Domain định nghĩa **contract**, không biết cách implement (JPA, MongoDB...).  
**Chứa gì:** Interface chỉ có method signatures, không extends JpaRepository  
**Dự án hiện tại:** Đang dùng JPA Repository trực tiếp trong Service (acceptable). Thêm interface ở đây khi cần strict hexagonal hoặc đổi database.

```java
// auction/domain/repository/AuctionDomainRepository.java  (pattern đầy đủ)
public interface AuctionDomainRepository {
    Optional<Auction> findById(UUID id);
    Auction save(Auction auction);
    List<Auction> findActiveAuctions();
    List<Auction> findExpiredAuctions(LocalDateTime before);
}
// Implementation: AuctionJpaRepository extends JpaRepository + implements AuctionDomainRepository
```

---

### `domain/service/` – Domain Services

**Mục đích:** Business logic phức tạp liên quan đến **nhiều entity**, không thuộc về entity đơn nào.  
**Quy tắc:**
- KHÔNG inject Spring beans, KHÔNG gọi repository
- Nhận entity/value object làm input, trả về result thuần
- Chỉ tạo khi logic không tự nhiên thuộc về một entity method

```java
// auction/domain/service/BidValidationService.java
public class BidValidationService {  // KHÔNG có @Service
    // Logic liên quan đến cả Auction lẫn AuctionRegistration
    public void validate(Auction auction, AuctionRegistration reg, BigDecimal amount) {
        if (!auction.canAcceptBid(amount)) throw new AppException(ErrorCode.BID_TOO_LOW);
        if (reg.getStatus() != RegistrationStatus.APPROVED)
            throw new AppException(ErrorCode.NOT_REGISTERED);
    }
}
```

---

### `domain/valueobject/` – Value Objects

**Mục đích:** Object bất biến (immutable) đại diện một khái niệm domain.  
**Quy tắc:**
- Immutable: `final` fields hoặc `record`
- Equality dựa trên **giá trị** (không phải id)
- Ví dụ: `Money`, `DateRange`, `PhoneNumber`

```java
// shared hoặc auction/domain/valueobject/Money.java
public record Money(BigDecimal amount, String currency) {
    public static Money ofVND(BigDecimal amount) { return new Money(amount, "VND"); }
    public Money add(Money other) { return new Money(this.amount.add(other.amount), currency); }
    public boolean isGreaterThan(Money other) { return amount.compareTo(other.amount) > 0; }
}
```

---

### `application/service/` – Application Services (Use Cases)

**Mục đích:** Orchestrate use cases: load entities, call domain logic, save, publish events.  
**Chứa gì:** `AuctionService.java`, `AdminStatsService.java`,...  
**Quy tắc:**
- `@Service`, `@Transactional` ở đây – KHÔNG ở Controller
- KHÔNG chứa business rule (rule ở Domain)
- KHÔNG gọi ApplicationService của module khác → dùng DomainEvent
- Inject: JPA Repository, DomainEventPublisher, external adapters

```java
// auction/application/service/AuctionService.java
@Service @RequiredArgsConstructor @Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuctionService {
    AuctionRepository auctionRepository;
    DomainEventPublisher domainEventPublisher;

    @Transactional
    public BidResponse placeBid(UUID auctionId, BidRequest request) {
        // 1. Load entity
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));
        // 2. Delegate business logic to domain behavior
        if (!auction.canAcceptBid(request.getAmount()))
            throw new AppException(ErrorCode.BID_TOO_LOW);
        // 3. Persist
        auction.setCurrentPrice(request.getAmount());
        auctionRepository.save(auction);
        // 4. Notify other modules via DomainEvent (không import NotificationService)
        domainEventPublisher.publish(new BidPlacedEvent(auctionId, request.getAmount()));
        // 5. Return response DTO
        return BidResponse.builder().auctionId(auctionId).amount(request.getAmount()).build();
    }
}
```

---

### `application/scheduler/` – Scheduled Tasks

**Mục đích:** Tác vụ chạy định kỳ hoặc khi ứng dụng khởi động.  
**Chứa gì:** `AuctionScheduler.java`  
**Quy tắc:**
- `@Component`, inject ApplicationService
- KHÔNG viết business logic trực tiếp – gọi qua Service

```java
// auction/application/scheduler/AuctionScheduler.java
@Component @RequiredArgsConstructor @Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuctionScheduler {
    AuctionService auctionService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() { auctionService.checkAndStartPendingAuctions(); }

    @Scheduled(fixedDelay = 60_000)
    public void checkExpiredAuctions() { auctionService.closeExpiredAuctions(); }
}
```

---

### `application/dto/response/` – Response DTOs (**FROZEN CONTRACT**)

**Mục đích:** Dữ liệu trả về cho Frontend.  
**Chứa gì:** `AuctionResponse.java`, `BidResponse.java`, `AuctionDetailResponse.java`,...  
**Quy tắc:**
- `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
- Field names **KHÔNG ĐỔI** sau deploy (FE hardcode field name)
- Thêm field mới: OK (backward compatible)
- Xóa / đổi tên field: **FORBIDDEN**

```java
// auction/application/dto/response/AuctionResponse.java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuctionResponse {
    UUID id;              // FROZEN – không đổi thành "auctionId"
    String name;          // FROZEN – không đổi thành "title"
    BigDecimal startPrice;   // FROZEN
    BigDecimal currentPrice; // FROZEN
    String status;           // FROZEN
    LocalDateTime endTime;   // FROZEN
    // Thêm field mới ở đây là OK ↓
    // Integer totalBids;    ← backward compatible addition
}
```

---

### `application/dto/command/` và `application/dto/query/`

**Mục đích:** CQRS pattern – tách write (Command) và read (Query) operations.  
**Dự án hiện tại:** Dùng trực tiếp Request DTO (đủ đơn giản). Tạo Command khi Service cần nhận input từ nhiều nguồn (HTTP, Scheduler, Event).

```java
// auction/application/dto/command/PlaceBidCommand.java
@Value  // Lombok – immutable
public class PlaceBidCommand {
    UUID auctionId;
    UUID bidderId;
    BigDecimal amount;
}
// auction/application/dto/query/AuctionFilterQuery.java
@Data
public class AuctionFilterQuery {
    String status;
    String categoryId;
    Pageable pageable;
}
```

---

### `application/mapper/` – MapStruct Mappers

**Mục đích:** Ánh xạ tự động Entity ↔ DTO tại compile time.  
**Quy tắc:**
- `@Mapper(componentModel = "spring")`
- `@Mapping(target = "field", ignore = true)` cho lazy-loaded hoặc computed fields
- Tránh viết mapping thủ công nếu MapStruct đủ

```java
// user/application/mapper/UserMapper.java
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "wallet", ignore = true)   // lazy load → skip
    UserResponse toUserResponse(User user);
}
// user/application/mapper/AccountMapper.java
@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "roleNo", expression = "java(mapRole(account.getRoles()))")
    AccountResponse toAccountResponse(Account account);
    default String mapRole(Set<Role> roles) {
        return roles.stream().findFirst().map(r -> r.getName()).orElse("USER");
    }
}
```

---

### `application/port/in/` – Input Ports (Use Case Interfaces)

**Mục đích:** Định nghĩa "những gì hệ thống có thể làm". Controller gọi qua interface.  
**Dự án hiện tại:** Controller inject Service trực tiếp (đủ cho scale hiện tại). Thêm khi cần testability cao hơn.

```java
// auction/application/port/in/PlaceBidUseCase.java
public interface PlaceBidUseCase {
    BidResponse placeBid(UUID auctionId, BidRequest request);
}
// AuctionService implements PlaceBidUseCase { ... }
// AuctionController inject PlaceBidUseCase (không phải AuctionService)
```

---

### `application/port/out/` – Output Ports (Infra Contracts)

**Mục đích:** Định nghĩa "những gì Application cần từ Infrastructure". Cho phép swap adapter.  
**Dự án hiện tại:** Chưa implement đầy đủ. Áp dụng khi cần chuyển đổi infra provider.

```java
// product/application/port/out/UploadImagePort.java
public interface UploadImagePort {
    String upload(MultipartFile file);
    void delete(String publicId);
}
// CloudinaryAdapter implements UploadImagePort { ... }
// Nếu chuyển sang S3: S3Adapter implements UploadImagePort { ... }
// ProductService inject UploadImagePort → không cần sửa Service khi đổi provider
```

---

### `infrastructure/persistence/repository/` – JPA Repositories

**Mục đích:** Triển khai data access với Spring Data JPA.  
**Chứa gì:** `AuctionRepository.java`, `BidRepository.java`,...  
**Quy tắc:**
- Extends `JpaRepository<Entity, UUID>`, thêm `JpaSpecificationExecutor` khi cần dynamic filter
- Custom query: method naming hoặc `@Query` (JPQL)
- Dùng `@Lock(PESSIMISTIC_WRITE)` cho race-condition sensitive operations (bid, withdraw)

```java
// auction/infrastructure/persistence/repository/AuctionRepository.java
@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID>,
                                           JpaSpecificationExecutor<Auction> {
    // Method naming query
    List<Auction> findByStatus(AuctionStatus status);

    // Custom JPQL
    @Query("SELECT a FROM Auction a WHERE a.endTime < :now AND a.status = :status")
    List<Auction> findExpiredAuctions(@Param("now") LocalDateTime now,
                                      @Param("status") AuctionStatus status);

    // Pessimistic lock – tránh race condition khi bid
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :id")
    Optional<Auction> findByIdWithLock(@Param("id") UUID id);
}
// Simple repository không cần custom query:
@Repository
public interface AuctionRegistrationRepository extends JpaRepository<AuctionRegistration, UUID> {
    Optional<AuctionRegistration> findByAuctionIdAndUserId(UUID auctionId, UUID userId);
    List<AuctionRegistration> findByAuctionId(UUID auctionId);
}
```

---

### `infrastructure/external/` – External Service Adapters

**Mục đích:** Tích hợp Cloudinary, Firebase, JWT, VNPay, MoMo,...  
**Chứa gì:** `CloudinaryAdapter.java`, `JwtTokenProvider.java`, `VNPayGatewayAdapter.java`, `MoMoGatewayAdapter.java`  
**Quy tắc:**
- Tên: `{ServiceName}Adapter` hoặc `{ServiceName}Provider`
- KHÔNG chứa business logic – chỉ wrap external API call
- Bắt exception từ external và convert sang `AppException`
- Implement Port/Out interface nếu có

```java
// product/infrastructure/external/CloudinaryAdapter.java
@Component @RequiredArgsConstructor
public class CloudinaryAdapter {  // implements UploadImagePort
    private final Cloudinary cloudinary;
    public String uploadImage(MultipartFile file) {
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "auto"));
            return (String) result.get("secure_url");
        } catch (IOException e) { throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR); }
    }
}

// auth/infrastructure/external/JwtTokenProvider.java
@Component
public class JwtTokenProvider {
    public String generateToken(Account account) { /* HMAC signing */ }
    public boolean validateToken(String token) { /* verify signature */ }
    public String extractSubject(String token) { /* parse claims */ }
}

// payment/infrastructure/external/VNPayGatewayAdapter.java
@Component
public class VNPayGatewayAdapter {
    public String buildPaymentUrl(PaymentRequest req) { /* VNPay API */ }
    public boolean verifyCallback(Map<String, String> params) { /* HMAC verify */ }
}
```

---

### `infrastructure/messaging/` – WebSocket & Event Listeners

**Mục đích:** Real-time messaging và lắng nghe Domain Events từ các module khác.  
**Chứa gì:** Event listeners (`AuctionEventListener`), WebSocket message handlers  
**Quy tắc:**
- `@EventListener` hoặc `@TransactionalEventListener` để listen DomainEvent
- `SimpMessagingTemplate` để push notification qua WebSocket (STOMP)
- KHÔNG chứa business logic – delegate sang ApplicationService

```java
// notification/infrastructure/messaging/AuctionEventListener.java
@Component @RequiredArgsConstructor
public class AuctionEventListener {
    final NotificationService notificationService;

    @EventListener
    public void onBidPlaced(BidPlacedEvent event) {
        notificationService.notifyOutbidUsers(event.getAuctionId(), event.getAmount());
    }

    // AFTER_COMMIT: chỉ publish sau khi transaction commit thành công
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuctionEnded(AuctionEndedEvent event) {
        notificationService.notifyWinner(event.getWinnerId(), event.getAuctionId());
    }
}
```

---

### `infrastructure/config/` – Module-Specific Config

**Mục đích:** Spring Bean config chỉ liên quan đến module đó.  
**Quy tắc:**
- Global config (Security, Redis, CORS) → root `config/` package
- Module config chỉ khai báo bean riêng của module

```java
// payment/infrastructure/config/VNPayConfig.java
@Configuration @ConfigurationProperties(prefix = "vnpay") @Data
public class VNPayConfig {
    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;
}
```

---

### `presentation/rest/` – REST Controllers

**Mục đích:** HTTP entry point – nhận request, validate, gọi Service, wrap response.  
**Chứa gì:** `AuctionController.java`, `AdminStatsController.java`,...  
**Quy tắc:**
- `@RestController` + `@RequestMapping("/api/{resource}")`
- Chỉ inject ApplicationService – KHÔNG inject Repository
- KHÔNG chứa business logic
- Mọi response PHẢI wrap trong `APIResponse<T>`
- `@PreAuthorize` cho phân quyền

```java
// auction/presentation/rest/AuctionController.java
@RestController @RequestMapping("/api/auctions") @RequiredArgsConstructor
public class AuctionController {
    final AuctionService auctionService;

    @GetMapping
    public APIResponse<Page<AuctionResponse>> getAll(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 12) Pageable pageable) {
        return APIResponse.<Page<AuctionResponse>>builder()
            .status(HttpStatus.OK.value())
            .message("Auctions fetched successfully")
            .result(auctionService.getAllAuctions(status, null, pageable))
            .build();
    }

    @PostMapping("/create-auction")
    @PreAuthorize("hasRole('USER')")
    public APIResponse<AuctionCreationResponse> create(
            @ModelAttribute AuctionCreationRequest request) throws IOException {
        return APIResponse.<AuctionCreationResponse>builder()
            .status(HttpStatus.CREATED.value())
            .message("Auction created")
            .result(auctionService.createAuction(request))
            .build();
    }
}
```

---

### `presentation/dto/request/` – Request DTOs (**FROZEN CONTRACT**)

**Mục đích:** Nhận dữ liệu từ client.  
**Chứa gì:** `AuctionCreationRequest.java`, `BidRequest.java`,...  
**Quy tắc:**
- `@Data @FieldDefaults(level = AccessLevel.PRIVATE)`
- Dùng Jakarta Validation: `@NotNull`, `@NotBlank`, `@Min`, `@Valid`
- Field names **KHÔNG ĐỔI** sau deploy
- `MultipartFile[]` cho file upload

```java
// auction/presentation/dto/request/AuctionCreationRequest.java
@Data @FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionCreationRequest {
    String name;               // FROZEN
    String description;        // FROZEN
    String categoryId;         // FROZEN
    String condition;          // FROZEN
    BigDecimal startPrice;     // FROZEN
    BigDecimal stepPrice;      // FROZEN
    LocalDateTime startTime;   // FROZEN
    LocalDateTime endTime;     // FROZEN
    MultipartFile[] files;     // FROZEN
}
```

---

### `presentation/advice/` – Exception Handlers

**Mục đích:** `@ControllerAdvice` riêng của module (nếu cần).  
**Quy tắc:**
- Global handler: `shared/presentation/advice/GlobalExceptionHandle.java` – xử lý `AppException`
- Chỉ tạo module advice khi cần override hoặc xử lý exception đặc thù của module

```java
// Thông thường KHÔNG cần tạo riêng per module.
// GlobalExceptionHandle đã xử lý AppException + validation errors.
// Dùng:
throw new AppException(ErrorCode.AUCTION_NOT_FOUND);
```

---

### Root `config/` – Global Configuration

| File | Mục đích |
|------|---------|
| `SecurityConfig.java` | Spring Security, JWT filter chain, CORS, endpoint permissions |
| `CustomJwtDecoder.java` | Decode & validate JWT token (tích hợp với Spring Security OAuth2) |
| `JWTAuthentication.java` | Custom JWT authentication filter |
| `RedisConfig.java` | Redis connection factory, cache manager |
| `RedisKeyExpirationListener.java` | Lắng nghe Redis key TTL expired (dùng cho auction scheduling) |
| `FirebaseConfig.java` | Firebase Admin SDK initialization |
| `CloudinaryConfig.java` | Cloudinary SDK bean |
| `ApplicationInitial.java` | Seed data khi startup: tạo roles, admin account |
| `SwaggerConfig.java` | OpenAPI / Swagger UI documentation |
| `WebSocketConfig.java` | STOMP WebSocket endpoint, message broker config |
| `WebSocketEventListener.java` | WebSocket connect/disconnect session tracking |

---



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

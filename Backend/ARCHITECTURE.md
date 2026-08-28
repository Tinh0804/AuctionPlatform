# ARCHITECTURE.md – Auction Platform Backend

> **Mục đích:** Tài liệu bắt buộc đọc trước khi viết bất kỳ dòng code nào.
> AI code generator và developer đều PHẢI tuân thủ 100% các quy tắc trong file này.
>
> **v2.0 – Đã sửa để tuân thủ đúng DDD + Hexagonal (Ports & Adapters).**
> So với v1.0, 3 thay đổi cốt lõi:
> 1. Domain Model tách hoàn toàn khỏi JPA Entity (không còn Active Record).
> 2. Application Service PHẢI phụ thuộc `domain/repository` (port), không phụ thuộc JPA Repository trực tiếp.
> 3. Aggregate không tham chiếu object của Aggregate module khác — chỉ giữ ID. Đã loại bỏ circular dependency `auction ↔ payment`.


---

## 1.1. Coding Style & Conventions

- **Clean Imports:** BẮT BUỘC sử dụng lệnh `import` ở đầu file thay vì viết chuỗi tên đầy đủ (Fully Qualified Class Name - FQCN) dài dòng trong mã nguồn.
  - ❌ **Sai:** `org.springframework.data.domain.Page<com.ecommerce.auctionplatform.auction.domain.model.Auction> findAll(...)`
  - ✅ **Đúng:** Thêm `import org.springframework.data.domain.Page; import com.ecommerce.auctionplatform.auction.domain.model.Auction;` ở đầu file và viết `Page<Auction> findAll(...)`.

---

## 1. Tổng Quan Kiến Trúc

Dự án áp dụng **Domain-Driven Design (DDD)** kết hợp **Hexagonal Architecture (Ports & Adapters)**.

```
┌─────────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                         │
│         REST Controllers · Request DTOs · Response Mappers   │
├─────────────────────────────────────────────────────────────┤
│                   APPLICATION LAYER                          │
│         Use Cases / Services · Output Models · Mappers       │
│         Port/In (driven) · Port/Out (driving)                │
├─────────────────────────────────────────────────────────────┤
│                    DOMAIN LAYER ← TRUNG TÂM                 │
│         POJO Model (KHÔNG JPA) · Value Objects · Events      │
│         Domain Services · Domain Exceptions · Repo PORTS     │
├─────────────────────────────────────────────────────────────┤
│                 INFRASTRUCTURE LAYER                         │
│         JPA Entity · Repository Impl (adapter of port)       │
│         External APIs · Messaging Adapters · Payment GW      │
└─────────────────────────────────────────────────────────────┘
```

**Quy tắc cốt lõi — Dependency Rule (BẤT BIẾN):**

```
Presentation → Application → Domain ← Infrastructure
```

Mọi mũi tên đều **trỏ vào Domain**. Domain KHÔNG được biết sự tồn tại của Presentation lẫn Infrastructure.

**Hệ quả bắt buộc (đây là phần v1.0 làm sai):**
- Domain layer là **Java thuần (POJO)** — KHÔNG import bất kỳ thứ gì từ `org.springframework.*` hay `jakarta.persistence.*`. Không có ngoại lệ cho JPA annotation nữa.
- Domain định nghĩa **interface** (`domain/repository/*Repository`), Infrastructure **implement** interface đó. Application Service chỉ được inject interface này — KHÔNG BAO GIỜ inject `JpaRepository` trực tiếp.
- JPA Entity là chi tiết kỹ thuật của Infrastructure, sống ở `infrastructure/persistence/entity/`, có mapper hai chiều với Domain Model.

---

## 2. Cấu Trúc Module

Dự án được tổ chức thành **6 bounded contexts nghiệp vụ**, một `shared` kernel và một outer integration layer. Authentication là một capability của `user`, không phải bounded context độc lập:

```
Backend/src/main/java/com/ecommerce/auctionplatform/
│
├── shared/              # Dùng chung, không thuộc module nào
├── user/                # Account, Authentication, User profile, KYC, Reputation
│   └── infrastructure/security/  # JWT, token blacklist adapters
├── product/             # Product, Category, Image
├── notification/        # Push notifications, WebSocket
├── auction/             # Auction lifecycle, Bidding, Scheduling
├── payment/             # Wallet, Order, Transaction, VNPay, MoMo
├── dispute/             # Dispute resolution
├── integration/         # Cross-context adapters; chỉ nối application ports
└── config/              # Chỉ khai báo technical configuration và bean wiring
```

`integration/` không phải bounded context và không chứa business rule. Đây là outermost composition
boundary dành cho trường hợp đặt adapter vào một context sẽ tạo dependency cycle. Mỗi adapter tại đây
phải implement `port/out` của context tiêu thụ và chỉ gọi public `port/in` của context cung cấp.
Nó không được truy cập domain repository, JPA repository, entity, service implementation hoặc presentation
của bất kỳ context nào.

### 2.1 Cấu Trúc Bên Trong Mỗi Module

```
{module}/
│
├── domain/
│   ├── model/              # Domain Model – POJO THUẦN, KHÔNG @Entity, KHÔNG Spring
│   ├── enums/               # Domain enumerations
│   ├── event/                # Domain Events (implements DomainEvent)
│   ├── exception/            # Domain-specific exceptions
│   ├── repository/           # Repository INTERFACES (ports out) – BẮT BUỘC dùng, không optional
│   ├── service/               # Domain Services (business invariants)
│   └── valueobject/           # Value Objects (immutable)
│
├── application/
│   ├── service/             # Application Services (use case orchestration)
│   ├── event/               # Published integration events for other contexts
│   ├── dto/
│   │   ├── response/        # Use-case output models – transport/framework agnostic
│   │   ├── command/          # Command objects (write operations)
│   │   └── query/             # Query objects (read operations)
│   ├── mapper/               # MapStruct mappers (Domain Model <-> DTO)
│   └── port/
│       ├── in/                # Input ports (use case interfaces) – Controller phụ thuộc vào đây
│       └── out/               # Output ports (infra contracts khác ngoài persistence: upload, gateway...)
│
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/          # JPA Entity – @Entity/@Table, KHÔNG chứa business logic
│   │   ├── repository/       # Spring Data JpaRepository<Entity, UUID> (package-private, chỉ dùng nội bộ)
│   │   ├── mapper/            # Entity <-> Domain Model mapper (MapStruct hoặc thủ công)
│   │   └── {Aggregate}RepositoryImpl.java   # implements domain/repository/{Aggregate}Repository
│   ├── external/              # External API adapters (Cloudinary, JWT, Payment)
│   ├── messaging/              # WebSocket, Event listeners
│   └── config/                  # Module-specific configuration
│
└── presentation/
    ├── rest/                # @RestController classes
    ├── dto/
    │   ├── request/          # Request DTOs (API input – FROZEN CONTRACT)
    │   └── response/          # HTTP response DTOs – API contract (FROZEN)
    ├── mapper/                 # Request → Command và application output → HTTP response
    └── advice/                  # Module-specific exception handlers
```

### 2.1.1 `domain/model` hay `domain/aggregate`?

Không đổi toàn bộ `domain/model` thành `domain/aggregate` (và tên đúng là `aggregate`, không phải `aggragate`).
`model` đang chứa cả Aggregate Root lẫn entity con; đổi tên hàng loạt sẽ gắn nhãn sai cho `Bid`,
`AuctionRegistration`, `Image`, `Address`, v.v. Chỉ tạo `domain/aggregate/` khi ranh giới aggregate
đã được xác định rõ và chỉ chuyển các root vào đó. Cho đến khi hoàn tất việc thiết kế invariant/transaction
boundary này, dự án giữ `domain/model/` và thể hiện Aggregate Root bằng repository port cùng behavior nghiệp vụ.

**Điểm khác biệt quan trọng so với v1.0:** `domain/model/` không còn là nơi đặt JPA Entity. JPA Entity chuyển xuống `infrastructure/persistence/entity/`. `infrastructure/persistence/repository/` là `JpaRepository` thô, chỉ được truy cập bởi `RepositoryImpl` trong cùng package — **không ai khác được inject nó**.

---

## 2.2 Chi Tiết Từng Subfolder – Mục Đích & Quy Tắc

### `domain/model/` – Domain Model (POJO thuần)

**Mục đích:** Trung tâm hệ thống. Đại diện cho Aggregate/Entity nghiệp vụ, tự bảo vệ invariant của chính nó.
**Chứa gì:** `Auction.java`, `Bid.java`, `AuctionRecord.java`,...
**Quy tắc:**
- CHỈ import runtime dependency từ Java stdlib, class trong cùng `domain/`, hoặc `shared/domain`.
- Lombok được phép dùng như annotation processor để sinh boilerplate; domain vẫn phải chạy như POJO và không được phụ thuộc runtime vào Lombok.
- **TUYỆT ĐỐI KHÔNG** import: `jakarta.persistence.*`, bất kỳ package `org.springframework.*`, JPA annotation, Lombok `@Data`/`@Builder` không bắt buộc (khuyến khích constructor/factory method để enforce invariant thay vì builder mở toang mọi field).
- Field nên `private final` khi có thể; thay đổi state phải qua method có tên nghiệp vụ (`placeBid()`, `close()`) chứ không phải setter trần trụi.
- Tham chiếu tới Aggregate của module khác: **chỉ giữ ID (`UUID`), không giữ object reference** (xem mục 2.2.7 và mục 6).

```java
// auction/domain/model/Auction.java  – POJO thuần, không JPA, không Spring
public class Auction {
    private final UUID id;
    private final UUID sellerId;       // ← chỉ giữ ID của User, KHÔNG giữ object User
    private BigDecimal currentPrice;
    private AuctionStatus status;
    private final LocalDateTime endTime;

    public Auction(UUID id, UUID sellerId, BigDecimal startPrice,
                    LocalDateTime endTime) {
        this.id = id;
        this.sellerId = sellerId;
        this.currentPrice = startPrice;
        this.status = AuctionStatus.PENDING;
        this.endTime = endTime;
    }

    // Business behavior – method nghiệp vụ, tự enforce invariant
    public void acceptBid(BigDecimal bidAmount) {
        if (status != AuctionStatus.ACTIVE) {
            throw new AppException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        if (bidAmount.compareTo(currentPrice) <= 0) {
            throw new AppException(ErrorCode.BID_TOO_LOW);
        }
        this.currentPrice = bidAmount;
    }

    public boolean isExpired() { return LocalDateTime.now().isAfter(endTime); }

    // Getter thuần, không setter công khai cho các field cần bảo vệ invariant
    public UUID getId() { return id; }
    public UUID getSellerId() { return sellerId; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public AuctionStatus getStatus() { return status; }
}
```

---

### `domain/enums/` – Domain Enumerations

Không thay đổi so với v1.0 — đây là phần đã đúng chuẩn.

```java
// auction/domain/enums/AuctionStatus.java
public enum AuctionStatus {
    PENDING, APPROVED, ACTIVE, EXTENDED, CLOSED, CANCELLED, FAILED
}
```

---

### `domain/event/` và `application/event/`

`domain/event/` chỉ chứa sự kiện nội bộ do Aggregate phát sinh. Sự kiện trở thành hợp đồng cho bounded
context khác phải được chuyển thành published event tại `application/event/`. Consumer chỉ import
published event này, không import `domain/event` của provider. Cả hai loại sự kiện chỉ chứa
`UUID`/primitive, không chứa Domain Model hay Entity.

```java
// auction/application/event/AuctionEndedEvent.java – published contract
public record AuctionEndedEvent(
    UUID auctionId, UUID winnerId, BigDecimal finalPrice, LocalDateTime occurredOn
) implements DomainEvent {
    public AuctionEndedEvent(UUID auctionId, UUID winnerId, BigDecimal finalPrice) {
        this(auctionId, winnerId, finalPrice, LocalDateTime.now());
    }
}
```

---

### `domain/exception/`, `domain/valueobject/`

Không đổi so với v1.0 — đã đúng chuẩn (Value Object immutable, exception thuần Java).

---

### `domain/repository/` – Repository Port Interfaces (BẮT BUỘC, không còn optional)

**Thay đổi quan trọng:** v1.0 ghi "đang dùng JPA Repository trực tiếp trong Service (acceptable)" — đây chính là điểm phá vỡ Dependency Inversion. **Từ v2.0, mọi Aggregate PHẢI có một port ở `domain/repository/`, và Application Service chỉ được phụ thuộc vào interface này.**

```java
// auction/domain/repository/AuctionRepository.java
// Đây là INTERFACE THUẦN — không extends JpaRepository, không import Spring Data
public interface AuctionRepository {
    Optional<Auction> findById(UUID id);
    Auction save(Auction auction);
    List<Auction> findActiveAuctions();
    List<Auction> findExpiredAuctions(LocalDateTime before);
}
```

Việc implement nằm hoàn toàn ở Infrastructure (xem mục `infrastructure/persistence/`).

---

### `domain/service/` – Domain Services

Không đổi quy tắc so với v1.0 (đã đúng): không inject Spring bean, không gọi repository, nhận Domain Model làm input.

```java
// auction/domain/service/BidValidationService.java
public class BidValidationService {   // KHÔNG có @Service
    public void validate(Auction auction, AuctionRegistration reg, BigDecimal amount) {
        if (!auction.canAcceptBid(amount)) throw new AppException(ErrorCode.BID_TOO_LOW);
        if (reg.getStatus() != RegistrationStatus.APPROVED)
            throw new AppException(ErrorCode.NOT_REGISTERED);
    }
}
```

---

### `application/service/` – Application Services (Use Cases)

**Thay đổi quan trọng:** Service giờ inject **domain port interface**, không inject `JpaRepository`.

```java
// auction/application/service/AuctionService.java
@Service @RequiredArgsConstructor @Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuctionService implements PlaceBidUseCase {
    AuctionRepository auctionRepository;         // ← domain/repository PORT, không phải JpaRepository
    DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional
    public BidResponse placeBid(UUID auctionId, BidRequest request) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));

        auction.acceptBid(request.getAmount());     // business rule nằm trong domain model

        auctionRepository.save(auction);
        domainEventPublisher.publish(new BidPlacedEvent(auctionId, request.getAmount()));

        return BidResponse.builder().auctionId(auctionId).amount(request.getAmount()).build();
    }
}
```

`application/scheduler/`, `application/dto/*`, `application/mapper/`, `application/port/in/`, `application/port/out/` giữ nguyên quy tắc như v1.0 (các phần này đã hợp lý), ngoại trừ:
- `application/port/in/` giờ **bắt buộc** dùng, không còn optional — Controller inject Use Case interface, không inject trực tiếp `{X}Service`.
- `application/mapper/` map **Domain Model → application output model** (trước kia là Entity → DTO).
- `presentation/mapper/` map **application output model → presentation response DTO**. Controller không được trả trực tiếp application output model.

---

### `infrastructure/persistence/entity/` – JPA Entity (MỚI, tách khỏi domain/model)

**Mục đích:** Chi tiết kỹ thuật lưu trữ. Đây là nơi duy nhất được phép có `@Entity`, `@Table`, `@Column`, `@ManyToOne`.
**Quy tắc:**
- Có thể giữ quan hệ object thật (`@ManyToOne User`) vì đây là Infrastructure, không phải Domain — nhưng chỉ dùng nội bộ để build câu SQL, không leak ra khỏi lớp Infrastructure.
- KHÔNG chứa business logic/behavior method — entity này "ngu", chỉ để ORM mapping.

```java
// auction/infrastructure/persistence/entity/AuctionEntity.java
@Entity @Table(name = "auctions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "seller_id")
    UUID sellerId;             // ← lưu ID, KHÔNG @ManyToOne sang UserEntity của module khác

    BigDecimal currentPrice;

    @Enumerated(EnumType.STRING)
    AuctionStatus status;

    LocalDateTime endTime;
}
```

### `infrastructure/persistence/mapper/` – Entity ↔ Domain Model Mapper

```java
// auction/infrastructure/persistence/mapper/AuctionEntityMapper.java
@Component
public class AuctionEntityMapper {
    public Auction toDomain(AuctionEntity e) {
        Auction auction = new Auction(e.getId(), e.getSellerId(), e.getCurrentPrice(), e.getEndTime());
        // set lại status nếu constructor không cover hết field (dùng package-private setter nếu cần)
        return auction;
    }
    public AuctionEntity toEntity(Auction a) {
        return AuctionEntity.builder()
            .id(a.getId()).sellerId(a.getSellerId())
            .currentPrice(a.getCurrentPrice()).status(a.getStatus())
            .build();
    }
}
```

### `infrastructure/persistence/repository/` – Spring Data JPA (chỉ dùng nội bộ)

```java
// auction/infrastructure/persistence/repository/AuctionJpaRepository.java
// package-private — KHÔNG public, để ngăn Application Service inject nhầm
@Repository
interface AuctionJpaRepository extends JpaRepository<AuctionEntity, UUID>,
                                        JpaSpecificationExecutor<AuctionEntity> {
    List<AuctionEntity> findByStatus(AuctionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AuctionEntity a WHERE a.id = :id")
    Optional<AuctionEntity> findByIdWithLock(@Param("id") UUID id);
}
```

### `{Aggregate}RepositoryImpl` – Adapter implement Domain Port

```java
// auction/infrastructure/persistence/AuctionRepositoryImpl.java
@Repository @RequiredArgsConstructor
class AuctionRepositoryImpl implements AuctionRepository {   // domain/repository/AuctionRepository
    private final AuctionJpaRepository jpaRepository;
    private final AuctionEntityMapper mapper;

    @Override
    public Optional<Auction> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Auction save(Auction auction) {
        AuctionEntity saved = jpaRepository.save(mapper.toEntity(auction));
        return mapper.toDomain(saved);
    }

    @Override
    public List<Auction> findActiveAuctions() {
        return jpaRepository.findByStatus(AuctionStatus.ACTIVE)
            .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Auction> findExpiredAuctions(LocalDateTime before) {
        return jpaRepository.findByStatus(AuctionStatus.ACTIVE)
            .stream().filter(e -> e.getEndTime().isBefore(before))
            .map(mapper::toDomain).toList();
    }
}
```

`infrastructure/external/`, `infrastructure/messaging/`, `infrastructure/config/` giữ nguyên quy tắc như v1.0 — các phần này không vi phạm gì.

---

### `presentation/rest/`, `presentation/dto/request/`, `presentation/advice/`

Không đổi so với v1.0 (đã đúng chuẩn), chỉ thêm một quy tắc: Controller inject **Use Case interface** (`application/port/in`) thay vì `{X}Service` cụ thể, để tách rời khỏi chi tiết implementation.

```java
// auction/presentation/rest/AuctionController.java
@RestController @RequestMapping("/api/auctions") @RequiredArgsConstructor
public class AuctionController {
    final PlaceBidUseCase placeBidUseCase;   // ← Use Case interface, không phải AuctionService

    @PostMapping("/{id}/bids")
    public APIResponse<BidResponse> placeBid(@PathVariable UUID id,
                                              @RequestBody @Valid BidRequest request) {
        return APIResponse.<BidResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Bid placed successfully")
            .result(placeBidUseCase.placeBid(id, request))
            .build();
    }
}
```

---

## 3. Shared Module

```
shared/
├── domain/event/           DomainEvent.java          # Marker interface
├── domain/exception/       DomainException.java      # Pure domain error
├── domain/model/           PageResult.java           # Pure shared model
├── application/event/      DomainEventPublisher.java # Port for publishing events
├── application/exception/  AppException.java · ErrorCode.java
├── application/model/      PageQuery.java · FileContent.java
├── application/port/out/   CurrentUserProvider · FileStoragePort · PasswordCodec
├── infrastructure/
│   ├── event/              SpringDomainEventPublisher.java
│   ├── security/           Spring adapters for security ports
│   └── utils/              PaymentUtils.java
└── presentation/
    ├── advice/             GlobalExceptionHandle.java
    ├── mapper/             MultipartFile → FileContent
    └── response/           APIResponse.java           # Standard API wrapper
```

**Quy tắc shared module (không đổi):**
- Chỉ chứa code dùng chung THỰC SỰ (≥3 modules cùng dùng).
- Không chứa business logic của bất kỳ module nào.
- Modules khác import từ `shared/`, nhưng `shared/` KHÔNG import ngược từ module nghiệp vụ.

---

## 4. Luồng Xử Lý Request

### 4.1 Luồng đọc (Read – GET)

```
HTTP GET Request
    ▼
[Presentation] Controller → validate input
    ▼
[Application] Service gọi domain/repository (PORT)
    ▼
[Infrastructure] RepositoryImpl → JpaRepository → DB → Entity
    ▼
[Infrastructure Mapper] Entity → Domain Model
    ▼
[Application Mapper] Domain Model → ResponseDTO
    ▼
[Presentation] Wrap APIResponse<T> → HTTP Response
```

### 4.2 Luồng ghi (Write – POST/PUT/DELETE)

```
HTTP Request (RequestDTO)
    ▼
[Presentation] Controller validate @Valid, gọi Use Case (port/in)
    ▼
[Application] Service: load Domain Model qua domain/repository (PORT)
    ▼
[Domain] Entity/AggregateRoot.behavior() – business rule thuần Java
    ▼
[Application] Publish DomainEvent nếu có side-effect, save qua domain/repository (PORT)
    ▼
[Infrastructure] RepositoryImpl map Domain Model → Entity → JpaRepository.save()
    ▼
[Application] Map Domain Model → ResponseDTO
    ▼
HTTP Response (wrapped APIResponse<T>)
```

### 4.3 Luồng Published Event (Side Effects)

```
[Application] AuctionService.endAuction()
    │  event = new AuctionEndedEvent(auctionId, winnerId)
    │  domainEventPublisher.publish(event)
    ▼
[Infrastructure] SpringDomainEventPublisher → applicationEventPublisher.publishEvent(event)
    ▼
[Notification] AuctionEventListener (@EventListener) → notifyWinner(...)
    ▼
[Payment] WalletEventListener (@EventListener) → releaseDeposit(...)
```
> `payment` có compile-time dependency có chủ đích vào published contract
> `auction/application/event`, nhưng không phụ thuộc domain model, repository hoặc implementation của
> `auction`. Listener chỉ dịch event rồi gọi `payment/application/port/in`; toàn bộ nghiệp vụ nằm trong
> application service.

---

## 5. API Contract – QUY TẮC BẤT BIẾN

Field names trong `presentation/dto/request` và `presentation/dto/response` không được đổi tên/xóa sau
khi production; thêm field mới theo hướng backward-compatible. Application output model là contract của use case,
không phải HTTP contract. Mọi endpoint bắt buộc wrap presentation response bằng `APIResponse<T>`.

```java
return APIResponse.<MyResponseDTO>builder()
    .status(HttpStatus.OK.value())
    .message("Mô tả kết quả")
    .result(data)
    .build();
```

---

## 6. Cross-Module Dependencies (DAG bắt buộc)

**Vấn đề ở v1.0:** bảng cũ cho phép `auction → payment` VÀ `payment → auction` cùng lúc → circular dependency giữa hai module, vi phạm nguyên tắc Bounded Context phải acyclic.

**Quy tắc mới:**
1. Dependency giữa các module chỉ được đi **một chiều** (xem bảng dưới).
2. Giao tiếp bất đồng bộ giữa module đi qua published event trong `provider/application/event`; không dùng
   `provider/domain/event` làm API công khai.
3. Application của một bounded context không import Domain/Application của context khác. Nó định nghĩa
   consumer-owned `port/out` và snapshot/view thuần của chính nó; adapter trong Infrastructure thực hiện port.
4. Một Aggregate KHÔNG được giữ object reference của Aggregate thuộc module khác — chỉ giữ `UUID`.

| Module | Dependency adapter/event được phép |
|--------|------------------------------------|
| `shared` | Không import module nghiệp vụ |
| `user` | `shared` |
| `product` | `shared` |
| `auction` | `shared`, `user`, `product` |
| `payment` | `shared`, `user`, `product`, `auction` |
| `notification` | `shared`, `auction`, `payment` |
| `dispute` | `shared`, `user`, `product`, `payment`, `notification` |

DAG tương ứng: `(user, product) → auction → payment → notification → dispute`.
Top-level `integration/` là composition boundary duy nhất được phép nối nhiều context khi đặt adapter
trong infrastructure của context tiêu thụ sẽ tạo dependency cycle. `config/` không chứa adapter hoặc
logic tích hợp; mọi class trực tiếp trong package này phải là technical `@Configuration`.

**Quy tắc import cụ thể (đã siết chặt):**
- `application/**` chỉ import domain/application của chính context và `shared`; dữ liệu context khác đi qua port/view do consumer sở hữu.
- Cross-context import chỉ nằm trong infrastructure adapter/event listener theo DAG, hoặc top-level `integration/`.
  Adapter chỉ gọi `provider/application/port/in`; listener chỉ nhận `provider/application/event`.
- Behavioral adapter (`adapter`, `external`, `messaging`) không inject domain repository/JPA repository và
  không thao tác aggregate. Persistence adapter là nơi duy nhất được implement domain repository.
- Adapter trong top-level `integration/` chỉ được import application contract (`port/in`, `port/out`, DTO/view) của các context; không được import domain repository hay implementation layer.
- **KHÔNG** import `application/service/` của module khác → dùng Domain Event hoặc Port/Out interface.
- **KHÔNG** import `presentation/` của module khác.
- **KHÔNG** import `infrastructure/` của module khác (kể cả `infrastructure/persistence/entity/`) — đây là chi tiết riêng tư tuyệt đối của module đó.
- **KHÔNG** giữ object reference (`@ManyToOne`/field kiểu Entity/Domain Model) sang Aggregate của module khác — chỉ giữ `UUID`.

---

## 7. Package Naming Convention

```
com.ecommerce.auctionplatform.{module}.{layer}.{sublayer}

Ví dụ:
  com.ecommerce.auctionplatform.auction.domain.model.Auction                              # POJO thuần
  com.ecommerce.auctionplatform.auction.domain.repository.AuctionRepository                # PORT
  com.ecommerce.auctionplatform.auction.application.service.AuctionService
  com.ecommerce.auctionplatform.auction.infrastructure.persistence.entity.AuctionEntity     # JPA
  com.ecommerce.auctionplatform.auction.infrastructure.persistence.AuctionRepositoryImpl     # ADAPTER
  com.ecommerce.auctionplatform.auction.presentation.rest.AuctionController
  com.ecommerce.auctionplatform.auction.presentation.dto.request.AuctionCreationRequest
  com.ecommerce.auctionplatform.auction.application.dto.response.AuctionResponse
```

---

## 8. Naming Conventions

| Loại class | Convention | Ví dụ |
|------------|-----------|-------|
| Domain Model | `PascalCase` | `Auction`, `AuctionRecord` |
| Domain Repository (Port) | `{Aggregate}Repository` | `AuctionRepository` (interface, ở `domain/repository`) |
| JPA Entity | `{Aggregate}Entity` | `AuctionEntity` (ở `infrastructure/persistence/entity`) |
| Repository Impl (Adapter) | `{Aggregate}RepositoryImpl` | `AuctionRepositoryImpl` |
| JPA Data Repository | `{Aggregate}JpaRepository` | `AuctionJpaRepository` (package-private) |
| Application Service | `{Resource}Service` | `AuctionService`, `WalletService` |
| Use Case (Port/In) | `{Verb}{Resource}UseCase` | `PlaceBidUseCase` |
| Domain Service | `{Resource}DomainService` | `AuctionDomainService` |
| External Adapter | `{Name}Adapter` | `CloudinaryAdapter` |
| Payment Gateway | `{Provider}GatewayAdapter` | `VNPayGatewayAdapter` |
| Request DTO | `{Action}{Resource}Request` | `AuctionCreationRequest` |
| Application Output | `{Resource}Response` | `application/dto/response/AuctionResponse` |
| HTTP Response DTO | `{Resource}Response` | `presentation/dto/response/AuctionResponse` |
| Presentation Mapper | `{Resource}ResponseMapper` | `AuctionResponseMapper` |
| Domain Event | `{Resource}{Verb}Event` | `AuctionEndedEvent` |
| Controller | `{Resource}Controller` | `AuctionController` |
| Mapper | `{Resource}Mapper` / `{Resource}EntityMapper` | `UserMapper`, `AuctionEntityMapper` |
| Enum type | `PascalCase` | `AuctionStatus` |
| Enum value | `UPPER_SNAKE_CASE` | `IN_PROGRESS` |

---

## 9. Quy Tắc AI Code Generation – NGHIÊM CẤM

### Danh sách ĐỎ (Vi phạm – AI không được làm)

```java
// 1. KHÔNG đặt @Entity/@Table vào domain/model
// domain/model/Auction.java
@Entity                       // ← VI PHẠM: domain phải là POJO thuần
public class Auction { ... }

// 2. KHÔNG để Application Service inject JpaRepository trực tiếp
public class AuctionService {
    @Autowired AuctionJpaRepository jpaRepository;   // ← PHẢI inject domain/repository PORT
}

// 3. KHÔNG để Controller gọi Repository (port hay JPA) trực tiếp
@RestController
public class AuctionController {
    @Autowired AuctionRepository repo;   // ← PHẢI qua Application Service / Use Case
}

// 4. KHÔNG để Domain Model gọi infra/Spring
public class Auction {
    public void save() { jpaRepository.save(this); }   // ← Domain không biết JPA
}

// 5. KHÔNG import Spring/JPA từ domain layer
import org.springframework.data.jpa.repository.JpaRepository;   // trong domain/** ← VI PHẠM
import jakarta.persistence.Entity;                                // trong domain/** ← VI PHẠM

// 6. KHÔNG đổi tên field DTO đang production
public class AuctionResponse {
    String auctionTitle;   // ← nếu field cũ là "name" phải giữ "name"
}

// 7. KHÔNG import ApplicationService/Repository/Entity của module khác
import com.ecommerce.auctionplatform.payment.application.service.OrderService;             // ← dùng DomainEvent
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.entity.UserEntity;  // ← TUYỆT ĐỐI CẤM

// 8. KHÔNG giữ object reference sang Aggregate của module khác trong Domain Model
public class Auction {
    private User seller;   // ← VI PHẠM, phải là: private UUID sellerId;
}

// 9. KHÔNG đặt business logic trong Controller
@PostMapping("/bid")
public APIResponse<BidResponse> bid(@RequestBody BidRequest req) {
    if (req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {   // ← thuộc Domain
        throw new AppException(ErrorCode.INVALID_AMOUNT);
    }
}

// 10. KHÔNG tạo dependency hai chiều compile-time giữa 2 module
// payment module import auction.application.service.AuctionService
// VÀ auction module import payment.application.service.PaymentService
// ← CIRCULAR DEPENDENCY, một trong hai chiều PHẢI đi qua Domain Event
```

### Danh sách XANH (Patterns chuẩn)

```java
// 1. Domain Model thuần túy – POJO, tự bảo vệ invariant
// File: auction/domain/model/Auction.java
public class Auction {
    private final UUID id;
    private final UUID sellerId;          // chỉ ID, không object User
    private BigDecimal currentPrice;
    private AuctionStatus status;

    public void acceptBid(BigDecimal bidAmount) {
        if (status != AuctionStatus.ACTIVE) throw new AppException(ErrorCode.AUCTION_NOT_ACTIVE);
        if (bidAmount.compareTo(currentPrice) <= 0) throw new AppException(ErrorCode.BID_TOO_LOW);
        this.currentPrice = bidAmount;
    }
    // getters, không setter public cho field cần bảo vệ
}

// 2. Domain Repository PORT
// File: auction/domain/repository/AuctionRepository.java
public interface AuctionRepository {
    Optional<Auction> findById(UUID id);
    Auction save(Auction auction);
}

// 3. Application Service phụ thuộc PORT, không phụ thuộc JPA
// File: auction/application/service/AuctionService.java
@Service @RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuctionService implements PlaceBidUseCase {
    AuctionRepository auctionRepository;    // ← PORT interface

    @Override @Transactional
    public BidResponse placeBid(UUID auctionId, BidRequest request) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));
        auction.acceptBid(request.getAmount());
        auctionRepository.save(auction);
        return BidResponse.builder().auctionId(auctionId).amount(request.getAmount()).build();
    }
}

// 4. RepositoryImpl – Adapter, sống ở Infrastructure, implement Port
// File: auction/infrastructure/persistence/AuctionRepositoryImpl.java
@Repository @RequiredArgsConstructor
class AuctionRepositoryImpl implements AuctionRepository {
    private final AuctionJpaRepository jpaRepository;
    private final AuctionEntityMapper mapper;

    public Optional<Auction> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    public Auction save(Auction auction) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(auction)));
    }
}

// 5. Controller chỉ delegate qua Use Case, không chứa logic
// File: auction/presentation/rest/AuctionController.java
@RestController @RequestMapping("/api/auctions") @RequiredArgsConstructor
public class AuctionController {
    final PlaceBidUseCase placeBidUseCase;

    @PostMapping("/{id}/bids")
    public APIResponse<BidResponse> placeBid(@PathVariable UUID id, @RequestBody @Valid BidRequest request) {
        return APIResponse.<BidResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Bid placed successfully")
            .result(placeBidUseCase.placeBid(id, request))
            .build();
    }
}
```

---

## 10. Thêm API / Tính Năng Mới

**Ví dụ:** Thêm `GET /api/auctions/{id}/participants`

```
Step 1 – Xác định module → auction/
Step 2 – Query (read-only) → không cần Command object
Step 3 – Tạo use-case output → auction/application/dto/response/AuctionParticipantResponse.java
Step 4 – Thêm method vào domain/repository PORT nếu cần query mới
         → auction/domain/repository/AuctionRegistrationRepository.java
         → List<AuctionRegistration> findByAuctionId(UUID auctionId);
Step 5 – Implement query đó ở infrastructure/persistence/{...}RepositoryImpl.java
         (thêm method tương ứng trong {...}JpaRepository nội bộ)
Step 6 – Thêm method vào Application Service, implement Use Case interface (port/in) nếu có
         → auction/application/service/AuctionService.java
Step 7 – Tạo HTTP response + presentation mapper
         → auction/presentation/dto/response/AuctionParticipantResponse.java
         → auction/presentation/mapper/AuctionResponseMapper.java
Step 8 – Thêm endpoint vào Controller, inject Use Case interface và response mapper
         → AuctionController.java → @GetMapping("/{id}/participants")
Step 9 – Verify → mvn compile → BUILD SUCCESS
```

### Template Aggregate mới trong module hiện có

```java
// domain/model/{NewAggregate}.java – POJO thuần
package com.ecommerce.auctionplatform.{module}.domain.model;
public class {NewAggregate} {
    private final UUID id;
    // fields + behavior methods, KHÔNG JPA annotation
}

// domain/repository/{NewAggregate}Repository.java – PORT
package com.ecommerce.auctionplatform.{module}.domain.repository;
public interface {NewAggregate}Repository {
    Optional<{NewAggregate}> findById(UUID id);
    {NewAggregate} save({NewAggregate} entity);
}

// infrastructure/persistence/entity/{NewAggregate}Entity.java – JPA
package com.ecommerce.auctionplatform.{module}.infrastructure.persistence.entity;
@Entity @Table(name = "{table_name}")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class {NewAggregate}Entity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    // fields...
}

// infrastructure/persistence/{NewAggregate}RepositoryImpl.java – ADAPTER
package com.ecommerce.auctionplatform.{module}.infrastructure.persistence;
@Repository @RequiredArgsConstructor
class {NewAggregate}RepositoryImpl implements {NewAggregate}Repository {
    private final {NewAggregate}JpaRepository jpaRepository;
    private final {NewAggregate}EntityMapper mapper;
    // implement methods
}

// application/dto/response/{NewAggregate}Response.java – use-case output, không Jackson/Jakarta
package com.ecommerce.auctionplatform.{module}.application.dto.response;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class {NewAggregate}Response {
    // transport-agnostic output fields
}

// presentation/dto/response/{NewAggregate}Response.java – HTTP contract
package com.ecommerce.auctionplatform.{module}.presentation.dto.response;
public record {NewAggregate}Response(/* FROZEN API fields */) {
}
```

---

## 11. Thêm Module Mới

**Tạo cấu trúc thư mục (đã cập nhật thêm `persistence/entity`):**

```bash
BASE=Backend/src/main/java/com/ecommerce/auctionplatform
MODULE=review   # đổi tên module ở đây

mkdir -p $BASE/$MODULE/{domain/{model,enums,event,exception,repository,service,valueobject},application/{service,dto/{response,command,query},mapper,port/{in,out}},infrastructure/{persistence/{entity,repository,mapper},external,messaging,config},presentation/{rest,dto/{request,response},mapper,advice}}
```

**Checklist:**
- [ ] Tạo đầy đủ 4 layer directories
- [ ] Domain Model trong `domain/model/` — **POJO thuần, không JPA annotation**
- [ ] Domain Repository PORT trong `domain/repository/`
- [ ] JPA Entity trong `infrastructure/persistence/entity/`
- [ ] RepositoryImpl trong `infrastructure/persistence/` implement domain PORT
- [ ] Application Service trong `application/service/`, implement Use Case (`port/in`) nếu có
- [ ] Request DTO trong `presentation/dto/request/` – define contract ngay từ đầu
- [ ] Use-case output model trong `application/dto/response/`, không chứa annotation HTTP/Jackson
- [ ] HTTP Response DTO trong `presentation/dto/response/`
- [ ] Presentation mapper chuyển application output sang HTTP response
- [ ] Controller trong `presentation/rest/`, inject Use Case interface và chỉ expose presentation response
- [ ] Cập nhật bảng Cross-Module Dependencies (Section 6) — kiểm tra không tạo circular dependency
- [ ] Không có field kiểu Domain Model/Entity của module khác trong Aggregate mới — chỉ `UUID`
- [ ] `mvn compile` → BUILD SUCCESS

---

## 12. Database Migration

Khi field mới được thêm vào **JPA Entity** (`infrastructure/persistence/entity/`), cập nhật Domain Model,
application mapper/output và presentation response mapper/DTO nếu field đó thực sự thuộc API contract.

```java
// 1. Thêm field vào JPA Entity (nullable = true cho migration safe)
@Column(name = "new_column", nullable = true)
String newColumn;
```

```sql
-- 2. Flyway migration file: db/migration/V{N}__{description}.sql
ALTER TABLE {table_name} ADD COLUMN new_column VARCHAR(255);
```

- Nếu field xuất hiện trong HTTP response → cập nhật Domain Model, application output, presentation mapper và presentation response theo hướng backward-compatible.
- Nếu field là input → thêm vào Request DTO với default value.

### Tạo bảng mới / Đổi tên column

Không đổi so với v1.0 (đã đúng chuẩn — 2-phase migration cho production).

---

## 13. Nâng Phiên Bản / Chuyển Framework

Không đổi so với v1.0. Lưu ý thêm: vì Domain Model đã tách khỏi JPA Entity, việc chuyển database (mục "Chuyển Database") giờ **thực sự** chỉ cần sửa Infrastructure layer (`entity/`, `repository/`, `RepositoryImpl`) — Domain layer không đổi gì, đúng như lời hứa của Hexagonal Architecture.

```
1. Domain layer: KHÔNG thay đổi gì (giờ đã thực sự đúng vì Domain không phụ thuộc JPA)
2. Tạo Entity + JpaRepository + RepositoryImpl mới trong infrastructure/
3. Đổi dependency trong pom.xml
4. Tạo migration scripts tương thích DB mới
5. Cập nhật application.properties
```

Phần "Thay thế thư viện" và "Thêm Payment Gateway Mới" giữ nguyên như v1.0 (đã đúng chuẩn — dùng Port/Out).

---

## 14. Thêm Payment Gateway Mới

Gateway adapter tại `infrastructure/external/` chỉ được tạo/ký request, gọi API và xác minh/parse callback.
Nó không được tạo transaction, truy cập ví/order repository, đổi trạng thái aggregate hoặc publish event.
Các thao tác đó thuộc `payment/application/service` và chạy trong application transaction.

---

## 15. Prompt Template Cho AI

```
Thêm chức năng [MÔ TẢ] vào module [MODULE] của Auction Platform.

Kiến trúc: DDD + Hexagonal (v2.0). Tuân thủ ARCHITECTURE.md.

Module đích: [module name]
Layer cần tạo/sửa:
  - domain/model: [Aggregate/Domain Model mới nếu có — POJO thuần]
  - domain/repository: [Port interface mới nếu có]
  - infrastructure/persistence/entity: [JPA Entity tương ứng]
  - infrastructure/persistence: [RepositoryImpl implement port]
  - application/service: [method mới, implement Use Case]
  - presentation/rest: [endpoint mới, inject Use Case interface]

Request DTO fields (FROZEN – không thay đổi):
  - [field1]: [type]

Presentation Response DTO fields (FROZEN):
  - [field1]: [type]

Package prefix: com.ecommerce.auctionplatform.[module]
Giữ nguyên API contract.
KHÔNG import ApplicationService/Entity/Repository của module khác.
KHÔNG để Domain Model chứa JPA annotation.
KHÔNG để Application Service inject JpaRepository trực tiếp — chỉ inject domain/repository PORT.
Aggregate tham chiếu module khác CHỈ bằng UUID, không giữ object.
```

### AI Self-Checklist Trước Khi Output Code

- [ ] Đúng package path?
- [ ] Domain Model không import Spring/JPA?
- [ ] Application Service inject `domain/repository` PORT, không inject `JpaRepository`?
- [ ] Controller không gọi Repository (port hay JPA) trực tiếp, chỉ gọi Use Case/Service?
- [ ] Presentation Request/Response DTO fields không thay đổi?
- [ ] Cross-module import theo Section 6, không tạo circular dependency?
- [ ] Aggregate chỉ giữ `UUID` khi tham chiếu module khác, không giữ object?
- [ ] Tất cả endpoint wrap `APIResponse<T>`?
- [ ] `@Transactional` ở Application Service?
- [ ] Business logic ở Domain Model/Domain Service (không phải Controller, không phải Entity JPA)?

---

## 16. Quick Reference

### Import paths hay dùng

```java
// Shared – dùng ở mọi module
import com.ecommerce.auctionplatform.shared.presentation.advice.AppException;
import com.ecommerce.auctionplatform.shared.presentation.advice.ErrorCode;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.shared.infrastructure.utils.SecurityUtils;

// Identity domain (chỉ những gì được phép theo Section 6)
import com.ecommerce.auctionplatform.identity.domain.model.User;
import com.ecommerce.auctionplatform.identity.domain.enums.PredefinedRole;
// KHÔNG import identity.infrastructure.persistence.entity.UserEntity từ module khác

// Auction domain
import com.ecommerce.auctionplatform.auction.domain.model.Auction;
import com.ecommerce.auctionplatform.auction.domain.repository.AuctionRepository;
import com.ecommerce.auctionplatform.auction.domain.enums.AuctionStatus;

// Payment domain
import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import com.ecommerce.auctionplatform.payment.domain.enums.WalletStatus;
```

### Annotation Templates

```java
// Domain Model – POJO thuần
public class {Aggregate} {
    private final UUID id;
    // fields, constructor, behavior methods — KHÔNG @Entity, KHÔNG @Data/@Builder bắt buộc
}

// JPA Entity – Infrastructure
@Entity @Table(name = "{table}") @Data @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class {Aggregate}Entity { ... }

// Application Service
@Service @RequiredArgsConstructor @Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class {Name}Service implements {Name}UseCase { ... }

// Controller
@RestController @RequestMapping("/api/{resource}")
@RequiredArgsConstructor
public class {Name}Controller { ... }
```

---

*Cập nhật lần cuối: 2026-08-24 | Phiên bản: 2.0 (đã sửa để tuân thủ đúng DDD + Hexagonal)*
*Bất kỳ thay đổi kiến trúc nào cũng PHẢI cập nhật file này.*

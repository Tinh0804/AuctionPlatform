# Clean Architecture Migration – Auction Platform

## Kết quả
- ✅ `mvn compile` → BUILD SUCCESS (zero errors)
- ✅ Spring Boot context load OK (không còn BeanDefinitionConflict)
- ✅ 125 old files disabled (`.java.bak`), 129 new module files active
- ❌ DB lỗi: Neon quota exceeded (infrastructure issue, không phải code)

## Module Structure (DDD + Hexagonal)
```
{module}/
  domain/
    model/          # JPA Entities
    enums/          # Domain enums
    event/          # Domain events
    repository/     # Port interfaces (out)
    service/        # Domain services
    valueobject/
  application/
    service/        # Use cases / Application services
    dto/response/   # Response DTOs (API output)
    mapper/         # MapStruct mappers
    port/in/        # Input ports (interfaces)
    port/out/       # Output ports (interfaces)
  infrastructure/
    persistence/repository/  # Spring Data JPA implementations
    external/                # Cloudinary, JWT, Payment adapters
    config/                  # Module-specific config
    messaging/               # WebSocket, event listeners
  presentation/
    rest/           # @RestController
    dto/request/    # Request DTOs (API input)
```

## Root Cause của Compilation Errors
1. **Multiple class definitions**: Old `service/*.java` và new `module/application/service/*.java` cùng tên class → incompatible types khi assign
   - Fix: Đổi tất cả `.java` → `.java.bak` trong old packages
   
2. **Concatenated wildcard imports**: `sed -i '' '/anchor/a\importA; importB;'` → Java parser không nhận multi-import trên 1 dòng
   - Fix: Mỗi import 1 dòng riêng
   
3. **Wrong package mapping**: Enum `WalletStatus` từ `entity.enums` ≠ `payment.domain.enums`
   - Fix: Replace fully-qualified old references

## Cross-Module Dependencies
- `ReputationHistory` (user module) → cần import `Dispute` (dispute module) và `Order` (payment module)
- `AuctionService` (auction) → depends on payment, product, notification, user repos
- `DisputeService` (dispute) → depends on payment wallets, user reputation, notification
- `config/ApplicationInitial.java` → dùng user entities (Role, Account, User)
- `config/RedisKeyExpirationListener.java` → dùng AuctionRecord từ auction module

## API Contract (FROZEN)
- Request DTOs: fields giữ nguyên 100%, chỉ đổi package path
- Response DTOs: fields giữ nguyên 100%
- Không đổi endpoint URLs

## Naming Conventions
- `user` module → `identity` module (`com.ecommerce.auctionplatform.identity`)
- `CloudinaryService` → `CloudinaryAdapter` (infrastructure adapter)
- `JwtService` → `JwtTokenProvider` (infrastructure adapter)
- `MoMoService` → `MoMoGatewayAdapter`
- `VNPayService` → `VNPayGatewayAdapter`
- `BlackListService` → `identity/infrastructure/security/BlackListService.java`

## Domain Exception & ErrorCode Handling
- Domain exceptions kế thừa `DomainException(DomainErrorCode.XXX)` thay vì hardcode string.
- `DomainErrorCode` nằm trong `shared.domain.enums` (không chứa HTTP code / web framework).
- `ErrorCode` nằm trong `shared.application.exception` mapping sang HTTP status và user-facing messages.
- `GlobalExceptionHandle` map `DomainErrorCode` sang `ErrorCode` một cách type-safe qua `ErrorCode.from(...)`.


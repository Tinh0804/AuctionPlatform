# ADR-002: Integrate main features without restoring legacy layers

- Status: Accepted
- Date: 2026-08-28

## Context

`main` continued to receive admin-management and frontend changes after the
clean-architecture branch diverged. A textual merge would restore controllers,
services, repositories, entities, and DTOs in the former shared layer packages.

## Decision

Port the behavior of `main` into the existing bounded contexts instead of
retaining the legacy package tree.

- Admin endpoints depend on context-owned application input ports.
- Domain state changes stay in domain models.
- Persistence queries stay behind domain repository interfaces.
- Cross-context access uses published application contracts and integration
  adapters; contexts do not read another context's repositories directly.
- Presentation DTOs remain separate from application DTOs.
- Frontend changes use the `app`, `features`, `entities`, and `shared` folders.
- Database changes are represented in both the bootstrap SQL and a versioned
  migration.

After verification, Git records `origin/main` as merged so future merges use the
combined ancestry without replacing the clean-architecture tree.

## Consequences

The application keeps the clean modular boundaries while gaining the newer
user, auction, category, dispute, order, notification, authentication, and UI
behavior from `main`. The semantic port requires explicit contract mapping, but
prevents the legacy architecture from becoming a second implementation path.

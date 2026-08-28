# ADR-001: Published application contracts between bounded contexts

## Status

Accepted

## Context

Auction Platform is a modular monolith with six bounded contexts. Several infrastructure adapters previously
read another context's domain repositories directly. Payment gateway adapters and event listeners also changed
wallet, order, transaction and reputation state. Those shortcuts made infrastructure own business workflows
and allowed consumers to depend on provider internals.

## Options considered

| Option | Benefits | Costs |
|---|---|---|
| Direct repository access | Few classes and calls | Breaks context ownership and Dependency Inversion |
| Shared domain models | Easy reuse | Couples all contexts to one model and weakens boundaries |
| Provider input ports + published events | Explicit ownership, testable adapters, acyclic dependencies | Requires mapping small immutable snapshots |
| Separate microservices | Strong deployment isolation | Operational complexity is not justified for this project |

## Decision

Keep the modular monolith. Synchronous cross-context calls use provider-owned `application/port/in` contracts.
The consuming context owns its `port/out` and maps provider snapshots in a thin infrastructure adapter.
Asynchronous contracts live in `provider/application/event`; consumers never import provider domain events.
Top-level `integration/` is reserved for composition adapters that would otherwise create a dependency cycle.

Infrastructure gateway adapters only perform protocol work. Application services own transaction creation,
idempotency, amount validation, aggregate mutation and event publication. Root `config/` contains technical
configuration only.

## Trade-offs

- Additional immutable records and mapping code are accepted to preserve bounded-context ownership.
- Calls are still in-process; failure isolation is lower than microservices, but operations and deployment stay simple.
- Spring annotations remain in application service implementations. Domain models and all application contracts
  remain framework-neutral; extracting framework wiring can be revisited if independent deployment is required.

## Consequences

- Architecture tests reject cross-context domain/repository imports and business persistence in behavioral adapters.
- New integrations must expose a public input port or published application event.
- A move to messaging or separate services can preserve these contracts and replace only the outer adapters.

## Revisit trigger

Reconsider process boundaries when a context requires independent scaling/deployment, or when in-process event
delivery no longer meets reliability requirements and an outbox/message broker is introduced.

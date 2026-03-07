# AGENTS.md

Guidelines for making changes in this repository. These rules are derived from
`tooplox.feedbackcollector.ArchitectureSpec` and reinforced by patterns used in:

- `tooplox.feedbackcollector`

## 1. Module Structure (required)

Each feature module follows this shape:

- `...<module>.domain`
    - Public module API only:
        - `<ModuleName>Facade`
        - `<ModuleName>FacadeConfiguration`
- `...<module>.domain.queries`
    - `*Query` data classes
- `...<module>.domain.commands`
    - `*Command` data classes (if any)
- `...<module>.domain.dto`
    - `*Dto` data classes
- `...<module>.domain.events`
    - `*Event` domain events
- `...<module>.domain.impl`
    - Internal implementation types only (services, repositories interfaces, aggregates, helpers)
- `...<module>.infra...`
    - Spring controllers, async handlers, DB adapters, HTTP clients, etc.

Important:

- Top-level classes in `..domain` must be only `*Facade` and `*FacadeConfiguration` (except shared/logger-generated
  Kotlin facade artifacts).
- Internal domain implementation belongs under `..domain.impl..`, not directly in `..domain`.
- `*Event`, `*Query`, `*Command`, `*Dto` names are enforced to live in dedicated packages.

## 2. Module API Rules (required)

Facades are the module boundary.

- Name module entrypoint class `<ModuleName>Facade`.
- Put Spring wiring in `<ModuleName>FacadeConfiguration` with `@Configuration` + `@Bean`.
- Other modules should interact through facades + public commands/queries/events/dtos only.
- Do not call `..domain.impl..` classes from outside the module.

Facade method signatures should expose only module API types:

- Parameters: `..domain.events`, `..domain.commands`, `..domain.queries`
- Returns: `..domain.dto`, `void`, collections, `Either`, `Optional`, `Page`

## 3. Domain vs Infra Boundaries (required)

Domain code must stay framework-light.

- Avoid Spring dependencies in `..domain..` except:
    - `*FacadeConfiguration`
    - Facades (for initialization-level use)
    - property-mapped classes where applicable
- Domain code must not depend on `..infra..` code.
- Shared module code (`..shared..`) must not depend on other feature modules.

Pattern from samples:

- Facades orchestrate use-cases and call other facades / internal services.
- Domain logic is extracted into private functions or `domain.impl` services/data classes.
- Infra adapters implement domain interfaces from `domain.impl` (e.g. repositories, HTTP clients).

## 4. Spring Usage Patterns

### Controllers

REST controllers typically:

- live in `..infra.rest.v1`
- are annotated with:
    - `@RestController`
    - `@RequestMapping(...)`
    - `@CrossOrigin`
- authenticate first via `AuthenticationFacade`, then call module facade
- translate request DTOs to domain queries/commands inside the controller
- map domain failures to HTTP responses in infra layer (not in domain)

### Async event handlers

Async handlers typically:

- live in `..infra.async`
- are `@Component`
- expose a concrete `@AsyncEventListener` method that delegates to `super.handle(event)`

## 5. Transactions and Events

- Every module facade should be annotated with `@Transactional`.
- Facade methods handling domain events must be:
    - annotated with `@EventListener` / `@AsyncEventListener`, or
    - called only from methods/classes with the correct listener annotation (per architecture rules).
- `*Event` classes should live in `..domain.events`.
- Domain events must be deserializable and included in the `JsonSubTypes` registration on `DomainEvent` (architecture
  test enforces this).

## 6. Kotlin / Coding Style Patterns

Observed patterns in sample modules:

- Prefer constructor injection for dependencies.
- Use expression bodies when readable.
- Facades are orchestration-first:
    - keep public facade methods focused on use-case flow
    - extract repeated or shape-heavy steps into private helpers (`calculateOrderPrices`, `startPayment`,
      `publishOrderPlacedEvent`, etc.)
    - keep boundary conversions close to the called facade/client (`GetUserProfileQuery(...)`,
      `StartPaymentCommand(...)`, response DTO mapping)
- Use `with(...)`, `let`, `also`, `apply`, `run` to structure flows, especially in facades and DSL/builders.
- Chained facade style is common and preferred when readable:
    - start with `log.info { ... }`
    - continue with `.run { ... }` / `.let { ... }`
    - finish with `.also { ... }`, `.onLeft { ... }`, `.onRight { ... }` for logging/side effects
- Event handlers are typically `handle(event: SomeEvent)` methods and may use `with(event)` to reduce repetition.
- Use `data class` for DTOs/queries/events and immutable public API objects.
- Use explicit logging with `KotlinLogging` in facades for start/success/failure handling.
- Logging style is usually structured free-text with bracketed key/value fields, e.g.
  `"Action [ userId = ..., orderId = ... ]"`.
- For `Either`-returning flows, prefer explicit success/failure handling (`.onLeft { it.log() }`, `.onRight { ... }`)
  instead of silent branching.
- Prefer descriptive helper function names in tests and production code (`thereIsXXX`, `XXXWasDone`, etc.).

Naming conventions:

- Test classes:
    - shared spec base: `<Feature>Spec`
    - behavior tests: `Should<Behavior>Spec`
    - integration behavior tests: `Should<Behavior>IntegSpec`
    - controller tests: `<ControllerName>Spec`
- Test support/config:
    - `<Feature>ModuleConfig` (integration test scanning)
    - `...RequestPerformer`
- Test fixtures/DSL:
    - `*Dsl` classes + top-level builder functions (`order { ... }`)

## 7. Testing Patterns (strongly preferred)

### Test layering

Use separate packages by test type:

- `domain` for unit tests of facades/domain logic
- `integ` for integration specs
- `infra/rest/v1` for controller/web slice tests
- `testdata` for builders/DSL fixtures
- `stubs` for in-memory test doubles

### Spec style

- Kotest string specs with `init { "should ..." { ... } }`
- Use readable scenario-building code over compressed assertions; tests are expected to read like use-case
  documentation.
- Arrange/Act/Assert comments:
    - `// given`
    - `// when`
    - `// then`
- Extract reusable setup/assert helpers into abstract base specs (`<Feature>Spec`, `<Feature>IntegSpec`)
- Base specs usually:
    - extend `BaseUnitSpec` / `BaseIntegSpec`
    - keep collaborators as mutable `var`s (mocks/stubs/facade)
    - recreate the facade in `beforeEach` for clean state
    - provide domain-specific helper methods for stubbing and assertions
- Prefer constructing the SUT via `<Feature>FacadeConfiguration` in tests when practical (mirrors production wiring and
  catches config drift).
- Use `MockK` for dependencies and `MockkBean` in Spring integration/controller tests
- Use `relaxed = true` mocks for fire-and-forget collaborators, and explicit `verify(exactly = 1)` for important
  outbound interactions.
- Use `clearMocks(...)`/helper reset methods in table-driven or repeated-scenario tests.
- Use Kotest nested contexts (`"..." - { ... }`) and data-driven testing (`forAll`, `row`) when covering variants across
  markets/providers/etc.
- Prefer custom domain assertions/helpers (`failedBecauseOf`, `isSuccessful`, `shouldBaseOn`, `shouldBeUpdatedWith`)
  over repeating low-level checks inline.

### Test data

- Prefer DSL/builders from `testdata` over inline object construction for complex models
- Keep builder defaults realistic; override only scenario-specific fields
- Add scenario helper methods in spec base classes for common stubbing and verification
- DSL style in this repo:
    - `*Dsl` data classes with mutable fields for defaults
    - fluent mutator helpers returning `apply`
    - top-level builders like `order { ... }`, `user { ... }`
    - overloads for common composite setups when they reduce duplication
- Put reusable assertion extensions in `testdata/Assertions.kt` (or similarly named files) when they encode domain
  semantics.

### Integration test style

- Integration specs reuse the same DSL/builders and helper naming used in unit tests for consistency.
- Prefer module-specific request performers / spec base helpers over ad hoc request setup in each test.
- For external integrations (e.g. WireMock), extract request/response stubbing into helper methods inside the spec and
  keep test bodies scenario-focused.

## 8. When Adding a New Module

Minimum checklist:

1. Create `domain/<Module>Facade.kt` and `domain/<Module>FacadeConfiguration.kt`.
2. Put public API classes in `domain.commands|queries|dto|events`.
3. Put internals in `domain.impl`.
4. Put Spring/web/async/db/http adapters in `infra...`.
5. Annotate facade with `@Transactional`.
6. Add unit + integration tests following `<Feature>Spec` / `Should...Spec` patterns.
7. If introducing a new `DomainEvent`, register it for deserialization (`DomainEvent` `JsonSubTypes`).

## 9. Practical Guardrails for Agents

- Do not expose `domain.impl` types across module boundaries.
- Do not place extra top-level classes directly under `..domain`.
- Do not move failure-to-HTTP mapping into domain code; keep it in controllers/adapters.
- Do not introduce Spring annotations/dependencies into internal domain types unless there is a clear existing exception
  covered by architecture rules.
- Run/keep `ArchitectureSpec` green after changes touching module structure.
- You are allowed to run `./gradlew` to run Gradle tasks like build or test.
- When creating commits, use Conventional Commit messages (commitlint is enforced).
- After making code changes, run tests for the affected modules before finishing.
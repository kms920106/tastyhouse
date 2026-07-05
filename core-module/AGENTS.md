<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-06-02 -->

# core-module

## Purpose
모든 도메인의 핵심을 담는 라이브러리 모듈(`java-library`). JPA 엔티티/Aggregate Root, Value Object, DomainEvent, Repository 인터페이스 및 구현, application(Command/Query) 서비스를 포함한다. **Spring Web 의존이 없다** — 컨트롤러나 `HttpStatus`는 여기 두지 않으며, 예외는 `int httpStatusCode`로 표현한다. `web-api`/`admin-api`/`external-api`가 모두 이 모듈에 의존한다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + JPA + QueryDSL(jakarta) + Lombok. `bootJar` 비활성, 일반 `jar` 생성. Q클래스 생성 경로 설정 포함 |
| `src/main/resources/` | 모듈 공용 리소스 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/core/` | DDD 도메인 루트 — 22개 도메인 + config/shared/exception (see `src/main/java/com/tastyhouse/core/AGENTS.md`) |
| `src/test/` | 모듈 테스트 (Testcontainers 사용 가능) |

## For AI Agents

### Working In This Directory
- **Spring Web import 금지**: `org.springframework.web.*`, `HttpStatus` 사용 불가. HTTP 상태는 `ErrorCode.httpStatusCode`(int)로만 표현.
- `@Entity`는 과도기적으로 domain 레이어에 허용되나, `@OneToMany`/`@ManyToOne`/`@ElementCollection` 연관관계 매핑은 **금지** — 외부 참조는 ID VO(`MemberId` 등)로 처리하고 자식 엔티티도 별도 Repository로 분리한다.
- 새 도메인 추가 시 `domain` / `application` / `infrastructure` 3-레이어 구조를 따른다.
- **command/condition record는 원시 파라미터 정적 팩토리 `of(...)`를 둔다**: presentation의 Request 타입을 인자로 받는 팩토리는 두지 않는다(레이어 역전 방지). command 생성 책임은 command record 자신이 지고, presentation(Facade/컨트롤러)은 Request를 원시 필드로 언패킹해 `Command.of(...)`를 호출한다. Request DTO에는 `toCommand()` 같은 변환 메서드를 두지 않는다. DTO 조립 규칙 전반은 루트 [CLAUDE.md](../CLAUDE.md#dto-조립-규칙-new-직접-호출-지양) 참고.

### Testing Requirements
- `@DataJpaTest` 또는 Testcontainers 기반 통합 테스트.
- 엔티티 매핑 변경 시 루트 `create.sql`과의 정합성 확인 (`ddl-auto=validate`).

### Common Patterns
- Repository: `domain/repository/XxxRepository`(인터페이스) ← `infrastructure/persistence/XxxRepositoryImpl`(구현, JpaRepository + QueryDSL 위임).
- ID 강타입: `record XxxId(Long value)` + `infrastructure/persistence/converter/XxxIdConverter`(AttributeConverter).
- DomainEvent는 `domain/event/`에 record로 정의, application 서비스가 `ApplicationEventPublisher`로 발행.

## Dependencies

### Internal
- 의존 없음 — 가장 안쪽 레이어. 다른 모듈을 참조하지 않는다.

### External
- `spring-boot-starter-data-jpa` (api), `mysql-connector-j`
- QueryDSL 5.0.0:jakarta (api) — Q클래스는 `build/generated/sources/annotationProcessor/java/main`
- Lombok

<!-- MANUAL: -->

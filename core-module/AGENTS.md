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
- **command/condition record는 원시 파라미터 정적 팩토리 `of(...)`를 둔다**: presentation의 Request 타입을 인자로 받는 팩토리는 두지 않는다(레이어 역전 방지). command 생성 책임은 command record 자신이 지고, presentation(Facade/컨트롤러)은 Request를 원시 필드로 언패킹해 `Command.of(...)`를 호출한다. Request DTO에는 `toCommand()` 같은 변환 메서드를 두지 않는다. DTO 조립 규칙 전반은 루트 CLAUDE.md 참고.
- **`record`는 별도 파일로 분리**: application 서비스 본문 안에 결과·중간 헬퍼 record를 중첩 선언하지 않고 `application/dto/result`(command는 `application/dto/command`)에 `public record`로 둔다. 서비스 내부 전용 `private` 헬퍼 record도 분리 시 `public`으로 격상한다(reference: `product/application/dto/result/OptionInfo`). 상세는 루트 CLAUDE.md 참고.
- **admin-flavor DTO/Result/Condition 타입명에 `Admin` 마커 금지**: admin 전용 조회 결과 타입도 타입명에 `Admin`을 붙이지 않는다. 비-admin 형제가 없으면 순수명(`MemberListItemResult` 등), 형제와 충돌하면 관리 화면 용도를 나타내는 `Management` 한정어로 구별한다(`OrderManagementListItemResult` 등). 단 `admin` 도메인 자체 타입(`Admin`, `AdminId`, `AdminRepository` 등)은 애그리거트 이름이므로 예외. 상세는 루트 CLAUDE.md "admin 전용 네이밍 규칙" 참고.

### Testing Requirements
- `@DataJpaTest` 또는 Testcontainers 기반 통합 테스트.
- 엔티티 매핑 변경 시 루트 `create.sql`과의 정합성 확인 (`ddl-auto=validate`).
- **enum 필드 추가/변경 시 `@Column(columnDefinition = "VARCHAR(n)")` 병기 + `create.sql`/`alter.sql` 정합성 필수 확인**: 누락 시 Hibernate 6 `MySQLDialect`가 네이티브 `ENUM`을 기대해 부팅이 `SchemaManagementException`(`wrong column type ... expecting [enum ...]`)으로 실패한다(선례: `BUG_REPORT`가 `columnDefinition`을 빠뜨려 발생).

### Common Patterns
- Repository: `domain/repository/XxxRepository`(인터페이스) ← `infrastructure/persistence/XxxRepositoryImpl`(구현, JpaRepository + QueryDSL 위임).
- ID 강타입: `record XxxId(Long value)` + `infrastructure/persistence/converter/XxxIdConverter`(AttributeConverter).
- **엔티티 enum 매핑**: 항상 `@Enumerated(EnumType.STRING)` + `@Column(length = n, columnDefinition = "VARCHAR(n)")`. `columnDefinition`을 빼면 Hibernate 6 `MySQLDialect`가 네이티브 `ENUM`으로 기대해 `validate` 실패. `EnumType.ORDINAL` 금지. DDL은 `VARCHAR(n)` + 허용값 주석. 선례: `Order.order_status`. 상세: 루트 `CLAUDE.md` "enum ↔ DB 컬럼 매핑 규칙".
- DomainEvent는 `domain/event/`에 record로 정의, application 서비스가 `ApplicationEventPublisher`로 발행.

## Dependencies

### Internal
- 의존 없음 — 가장 안쪽 레이어. 다른 모듈을 참조하지 않는다.

### External
- `spring-boot-starter-data-jpa` (api), `mysql-connector-j`
- QueryDSL 5.0.0:jakarta (api) — Q클래스는 `build/generated/sources/annotationProcessor/java/main`
- Lombok

<!-- MANUAL: -->

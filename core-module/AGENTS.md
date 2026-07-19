<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-06-02 -->

# core-module

## Purpose
모든 도메인의 핵심을 담는 라이브러리 모듈(`java-library`). JPA 엔티티/Aggregate Root, Value Object, DomainEvent, Repository 인터페이스 및 구현, application(Command/Query) 서비스를 포함한다. **Spring Web 의존이 없다** — 컨트롤러나 `HttpStatus`는 여기 두지 않으며, 예외는 `int httpStatusCode`로 표현한다. `web-api`/`admin-api`/`external-api`가 모두 이 모듈에 의존한다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + JPA + QueryDSL(OpenFeign 포크 6.11) + Lombok. `bootJar` 비활성, 일반 `jar` 생성. Q클래스 생성 경로 설정 포함 |
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
- **도메인/JPA 엔티티 분리 패턴 (선별 적용, reference: `notice`)**: 상태전이·불변식이 실재하는 도메인은 도메인 모델을 `jakarta` 무의존 순수 POJO로 두고, JPA 엔티티(`XxxJpaEntity`)·매퍼(`XxxMapper`)·`RepositoryImpl`을 별도 `infrastructure-module`(`com.tastyhouse.infrastructure.<도메인>.persistence`)로 분리한다. 단순 CRUD 도메인은 현행(도메인 모델 = `@Entity`) 유지가 허용되며 전환은 강제가 아니다.
  - **순수 도메인 모델**: 신규 생성 `of(...)`와 DB 재구성 전용 `reconstitute(id, ..., createdAt, updatedAt)` 두 팩토리만 공개한다. `reconstitute`는 인프라만 호출(불변식 우회 방지, Javadoc 명시). `id`는 미영속이면 null.
  - **명시적 save 규칙 (더티 체킹 상실 보완)**: 분리된 도메인의 command 서비스는 도메인을 변경한 뒤 **반드시 `repository.save(domain)`를 호출**한다(`@Entity`처럼 트랜잭션 종료 시 자동 flush되지 않는다). 누락 시 변경이 조용히 유실된다 — reference: `NoticeCommandService#updateNotice`·`#deleteNotice`.
  - **저장 시맨틱은 load-copy-save**: `RepositoryImpl.save`는 id null이면 신규 insert, id 있으면 managed 엔티티를 PK로 조회 후 `Mapper.applyChanges`로 필드 복사(동일 트랜잭션 1차 캐시 히트). detached `save()`(merge)는 `@CreatedDate(updatable=false)` 감사 필드 파손 위험이 있어 금지.
- 새 도메인 추가 시 `domain` / `application` / `infrastructure` 3-레이어 구조를 따른다.
- **command/condition record는 원시 파라미터 정적 팩토리 `of(...)`를 둔다**: presentation의 Request 타입을 인자로 받는 팩토리는 두지 않는다(레이어 역전 방지). command 생성 책임은 command record 자신이 지고, presentation(Facade/컨트롤러)은 Request를 원시 필드로 언패킹해 `Command.of(...)`를 호출한다. Request DTO에는 `toCommand()` 같은 변환 메서드를 두지 않는다. DTO 조립 규칙 전반은 루트 CLAUDE.md 참고.
- **`record`는 별도 파일로 분리**: application 서비스 본문 안에 결과·중간 헬퍼 record를 중첩 선언하지 않고 `application/dto/result`(command는 `application/dto/command`)에 `public record`로 둔다. 서비스 내부 전용 `private` 헬퍼 record도 분리 시 `public`으로 격상한다(reference: `product/application/dto/result/OptionInfo`). 상세는 루트 CLAUDE.md 참고.
- **admin-flavor Result/Condition 타입명에 `Admin` 마커 금지**: admin 전용 조회 결과 타입도 타입명에 `Admin`을 붙이지 않는다. 비-admin 형제가 없으면 순수명(`MemberListItemResult` 등), 형제와 충돌하면 관리 화면 용도를 나타내는 `Management` 한정어로 구별한다(`OrderManagementListItemResult` 등). 단 `admin` 도메인 자체 타입(`Admin`, `AdminId`, `AdminRepository` 등)은 애그리거트 이름이므로 예외. 상세는 루트 CLAUDE.md "admin 전용 네이밍 규칙" 참고.
- **결과 record 접미어는 `Result`로 통일, `Dto` 금지**: application 조회 결과 record는 접미어를 `Result`로 쓰고 `Dto`는 사용하지 않는다(`Command`/`Condition`은 대상 아님). `application/dto/result/`에 둔다. 상세는 루트 CLAUDE.md "결과 DTO 접미어 규칙" 참고.

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
- QueryDSL `io.github.openfeign.querydsl:querydsl-jpa:6.11` (api, OpenFeign 포크 — CVE-2024-49203 대응. 원 `com.querydsl:5.0.0`은 패치 없음. 패키지명은 `com.querydsl.*` 그대로라 소스 무수정. 6.x부터 `:jakarta` classifier 없이 jakarta가 기본이며 apt만 `:jakarta` classifier 유지) — Q클래스는 `build/generated/sources/annotationProcessor/java/main`
- Lombok

<!-- MANUAL: -->

<!-- Generated: 2026-06-02 | Updated: 2026-06-02 -->

# tastyhouse-api

## Purpose
음식점/가게(Shop) 기반 커머스 플랫폼의 백엔드. Spring Boot 3.2.4 / Java 21 기반 Gradle 멀티모듈 프로젝트로, 회원·주문·결제·리뷰·예약·쿠폰·포인트 등 약 22개 도메인을 제공한다. 전통적 계층형에서 **DDD / Clean Architecture(점진적 Strangler Fig 전환)** 로 이행 중이며, 비즈니스 규칙을 도메인 객체에 캡슐화하는 Rich Domain Model을 지향한다.

## Key Files
| File | Description |
|------|-------------|
| `settings.gradle` | 멀티모듈 정의 (`web-api`, `admin-api`, `core-module`, `external-api`) |
| `build.gradle` | 루트 빌드 — 전 모듈 공통 설정 (Java 21, Spring Boot 플러그인, AWS BOM) |
| `gradlew` | Gradle Wrapper 실행 스크립트 |
| `CLAUDE.md` | AI 작업 규칙 (한국어 응답, 빌드 테스트 생략, 커밋/롤백 금지) |
| `create.sql` / `insert.sql` / `alter.sql` | 스키마 및 시드 데이터 (DDL은 `ddl-auto=validate` 전제) |
| `.env`, `.env-copy` | 환경 변수 (외부 연동 키 등) |
| `REAME.md` | 이미지 경로 네이밍 컨벤션 메모 (오타 파일명, README 아님) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `core-module/` | DDD 도메인 핵심 — 엔티티/VO/이벤트/레포지토리/application 서비스 (Spring Web 의존 없음). 가장 큰 모듈 (see `core-module/AGENTS.md`) |
| `web-api/` | 사용자용 REST API — 컨트롤러, 인증(JWT/OAuth), 보안, 스케줄러 (see `web-api/AGENTS.md`) |
| `external-api/` | 외부 연동 어댑터 — OAuth, 결제(Toss), 이메일/SMS, 파일(S3/Firebase), 크롤링 (see `external-api/AGENTS.md`) |
| `admin-api/` | 관리자용 REST API (현재 최소 구현, 정책 도메인 위주) (see `admin-api/AGENTS.md`) |
| `md/` | 설계 문서 — `CLEAN-ARCHITECTURE.md`(전환 가이드, 정전), 소셜 로그인 가이드, 리팩토링 노트 |
| `gradle/` | Gradle Wrapper 바이너리/설정 |
| `json/` | 외부 자격 증명 JSON (예: Firebase 서비스 계정) |

## For AI Agents

### Working In This Directory
- **응답은 한국어로** 작성한다 (프로젝트 규칙).
- **빌드/테스트 실행 금지**: 로직 구현 후 `gradle build`/test를 자동 실행하지 않는다.
- **커밋/롤백 금지** (`NO_COMMIT_OR_ROLLBACK`): 사용자가 명시적으로 요청하지 않는 한 git 커밋·롤백을 하지 않는다.
- 네이밍은 명확하고 의미 있는 이름을 선택한다.
- 변경 전 반드시 `md/CLEAN-ARCHITECTURE.md`의 레이어 의존 규칙을 따른다.
- **DTO 조립은 `new` 직접 호출을 지양**한다: 컨트롤러·Facade·서비스 등 호출부에서 command/condition/response record를 `new`로 조립하지 않고, 대상 record 자신의 정적 팩토리 `of(...)`/`from(...)`로 위임한다. Request DTO는 command 생성 책임을 지지 않는 순수 데이터 홀더로 유지하며, 컨트롤러가 Request를 원시 필드로 언패킹해 Facade/서비스에 전달한다. `new`는 팩토리 메서드 내부에만 남긴다. 상세 규칙과 reference 구현(admin-api notice)은 [CLAUDE.md](CLAUDE.md#dto-조립-규칙-new-직접-호출-지양) 참고.
- **`record`는 별도 파일로 분리**한다: 서비스·컨트롤러·Facade 등 다른 클래스 본문 안에 record를 중첩 선언하지 않고, 각 도메인 관례 위치(web-api/admin-api는 `response/`, core는 `application/dto/result`·`command`)에 독립 `.java` 파일로 둔다. 분리 시 최상위 타입이 되므로 `public record`로 선언하고, 내부 전용 헬퍼 record도 동일하게 분리한다. 상세와 reference(web-api `NoticeListPageResult`/`OrderListPageResult`, core `OptionInfo`)는 [CLAUDE.md](CLAUDE.md#record-파일-분리-규칙-중첩-record-선언-지양) 참고.
- **presentation의 core 결합 격리**: admin-api·web-api 모두 컨트롤러가 `core-module`에 직접 결합되는 것을 막기 위해 도메인별 Facade(`{도메인}Service`)를 두어 컨트롤러 ↔ core 사이를 중개한다(reference: `admin-api/notice/NoticeService`). Facade가 core 서비스 호출과 core DTO↔Request/Response 변환을 전담하며, 컨트롤러는 `com.tastyhouse.core.*`를 import하지 않는다(각 모듈 `AGENTS.md` 참조).
- **컨트롤러 `@PathVariable`은 주 리소스를 `id`로 통일**한다: 컨트롤러가 이미 `@RequestMapping`으로 그 도메인에 스코프되므로, 주 리소스를 가리키는 경로 변수는 단건·중첩 경로 모두 bare `id`로 쓰고(예: `/coupons/v1/{id}`, `/coupons/v1/{id}/issues`) 한 컨트롤러 안에서 `id`/`{도메인}Id` 혼재를 금지한다. 단, 다른 애그리거트 식별자를 함께 받는 경우만 `{도메인}Id`로 구분한다. 타입은 `Long` 유지(`@PathVariable Long id`). 상세는 [CLAUDE.md](CLAUDE.md#컨트롤러-pathvariable-식별자-명명-규칙-id로-통일) 참고.
- **import 순서** (Spring Framework 공식 컨벤션 `SpringImportOrderCheck`와 동일): `java.*` → `javax.*` → 그 외 전부(`jakarta.*` 포함, org/io/lombok/com.* 등 알파벳 혼합) → 자사(`com.tastyhouse.*`) → static import(맨 아래) 순서로 그룹을 나누고, 그룹 사이 빈 줄 1개, 그룹 내부는 알파벳 순 정렬한다. 자사(`com.tastyhouse.*`) 그룹 내부는 헥사고날 의존성 방향(안→밖) 순 — domain→application→infrastructure→external/shared→presentation — 으로 정렬하고, 같은 계층 내부만 알파벳순(프로젝트 커스텀 규칙, 공식 표준 아님). presentation(`webapi`/`adminapi`) 내부는 다시 공용 인프라(`common`·`config`·`security`·`ratelimit`·`exception`)를 위(5-a), 도메인 전용(`<도메인>.request`·`.response`)을 아래(5-b)로 서브정렬한다. 상세·근거·예시는 [CLAUDE.md](CLAUDE.md#코딩-스타일-import-순서) 참고.

### Module Dependency Graph
```
web-api ──┬─→ core-module
          └─→ external-api ─→ core-module
admin-api ─┬─→ core-module
           └─→ external-api
core-module → (no Spring Web; JPA + QueryDSL only)
```
- `core-module`은 다른 모듈에 의존하지 않는다. Spring Web/HttpStatus 사용 불가 → 예외는 `int httpStatusCode`로 표현.

### Testing Requirements
- 스키마 무변경 보장: `hibernate.ddl-auto=validate` 기준. 엔티티 변경 시 `create.sql`과 정합성 확인.
- QueryDSL Q클래스는 `build/generated/...`에 생성됨 — 경로 변경 시 `./gradlew clean compileJava` 필요.

### Common Patterns
- 도메인별 3-레이어: `domain`(model/vo/event/repository 인터페이스) / `application`(Command·Query 서비스) / `infrastructure`(JPA 구현).
- 식별자 강타입화: `record MemberId(Long value)` + `AttributeConverter`로 JPA 매핑.
- BC 간 통신은 application 레이어 호출 또는 `DomainEvent`(`@TransactionalEventListener(AFTER_COMMIT)`)로만.
- CQS: 쓰기 `@Transactional` / 읽기 `@Transactional(readOnly = true)`.

## Dependencies

### External
- Spring Boot 3.2.4 (web, webflux, security, data-jpa, data-redis, aop, mail, validation)
- Java 21, Gradle (멀티모듈)
- QueryDSL 5.0.0 (jakarta) — 동적 쿼리
- MySQL (`mysql-connector-j`), Redis
- JJWT 0.12.3 — JWT 발급/검증
- AWS SDK (SES, SNS, S3), Firebase Admin 9.2.0
- springdoc-openapi 2.3.0 — Swagger UI
- Lombok, p6spy (SQL 로깅)

<!-- MANUAL: 수동 메모는 이 라인 아래에 추가하면 재생성 시 보존됩니다 -->

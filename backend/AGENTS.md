<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# tastyhouse-api

## Purpose
음식점/가게(Shop) 기반 커머스 플랫폼의 백엔드. Spring Boot 3.2.4 / Java 21 기반 Gradle 멀티모듈 프로젝트로, 회원·주문·결제·리뷰·예약·쿠폰·포인트 등 22개 도메인을 제공한다. 전통적 계층형에서 시작해 **DDD / Clean Architecture(Strangler Fig 점진 전환)** 를 거쳐, `core-module` → `domain-module` 전환으로 **도메인 계층이 프레임워크를 전혀 모르는(production 의존 0개) 구조**에 도달했다. 비즈니스 규칙을 도메인 객체에 캡슐화하는 Rich Domain Model을 지향한다.

전환 이후 구조의 핵심 네 가지:
- **`domain-module`은 프레임워크-프리**다. Spring Web뿐 아니라 JPA·QueryDSL·`spring-tx`/`spring-orm`도 없다 — `@Transactional`/`@Service`/`@Component`가 한 곳도 없고, 도메인 서비스는 순수 POJO이며 빈 등록은 `infrastructure:persistence`의 컨텍스트별 `<ctx>/config/<Ctx>DomainConfig`가 담당한다.
- **api 모듈(web/admin/ceo/batch)은 QueryDSL도 infrastructure도 모른다**. 조회 계약(`{Ctx}QueryPort` 인터페이스 + `*Result`/`*SearchCondition`)은 패키지 `com.tastyhouse.application.<ctx>.port.out`에 있고, **소유 모듈은 소비자 수로 갈린다** — 한 앱만 쓰면 그 앱의 `{앱}-application`, 2개 이상이 쓰면 `domain-module`이다(챕터 09로 `application-common-module`이 해체되며 5개 모듈에 분산됐다. 패키지는 그대로라 소비 측 import는 바뀌지 않는다). `infrastructure:persistence`의 `{도메인}QueryDao`가 그 인터페이스를 구현한다. 각 앱의 `{도메인}QueryService`는 DAO가 아니라 포트 인터페이스를 주입하므로 `com.tastyhouse.infrastructure..` import가 **0건**이다. `com.tastyhouse.infrastructure..`(전면)·`com.querydsl..` 의존은 4개 모듈의 ArchUnit `LayerRulesTest`가 차단한다.
- **application 계층은 앱별 모듈로 물리 분리됐다 (모듈 재편 프로그램, 챕터 01~06)**. 도메인당 `{도메인}CommandService`/`{도메인}QueryService` CQRS 쌍은 이제 api 모듈이 아니라 `{web|admin|ceo|batch}-application`이 소유하고(예: `com.tastyhouse.webapplication.notice.service.NoticeQueryService`), api 모듈은 컨트롤러 + `request/`만 갖는 **thin adapter**로 축소됐다. 모듈 경계가 "계층 × 앱" 2차원이 되어, 계층 위반이 ArchUnit 사후 검출이 아니라 **컴파일 에러**가 된다.
- **`@QueryProjection`은 전 리포지토리에서 폐지**됐다. `infrastructure:persistence`의 QueryDao는 `Projections.constructor(XxxResult.class, ...)`로 Result record를 조립한다 — Result가 QueryDSL을 모르는 계약 모듈(현재는 `{앱}-application`·`domain-module`)로 이관되어 그 모듈에 apt를 붙일 수 없기 때문이다.

## Key Files
| File | Description |
|------|-------------|
| `settings.gradle` | 멀티모듈 정의 — 실행 앱 4개(`web-api`, `admin-api`, `ceo-api`, `batch-module`) + 앱별 application 4개(`web-application`, `admin-application`, `ceo-application`, `batch-application`) + 공유 모듈(`domain-module`, `infrastructure:persistence`, `infrastructure:redis`, `external-api`, `security-module`, `api-common-module`, `logging-module`) |
| `build.gradle` | 루트 빌드 — 전 모듈 공통 설정 (Java 21, Spring Boot 플러그인, AWS BOM) |
| `gradlew` | Gradle Wrapper 실행 스크립트 |
| `CLAUDE.md` | backend 고유 코딩 컨벤션 (네이밍·DTO·레이어 경계 등). AI 작업 규칙(한국어 응답, 빌드 테스트 생략, 커밋/롤백 금지)은 리포 루트 `../CLAUDE.md` |
| `schema.sql` / `insert.sql` / `alter.sql` | 스키마 및 시드 데이터 (DDL은 `ddl-auto=validate` 전제) |
| `.env`, `.env-copy` | 환경 변수 (외부 연동 키 등) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `domain-module/` | DDD 도메인 핵심 — 도메인 모델(POJO)/VO/이벤트/Repository write 포트/도메인 서비스/출력 포트 + `shared`·`exception`. **프레임워크-프리(production 의존 0개)** (see `domain-module/AGENTS.md`) |
| `infrastructure/persistence/` | domain 포트의 DB 어댑터 — `<ctx>/persistence`(write: JPA/매퍼) + `<ctx>/query`(read: QueryDSL QueryDao — `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort`를 implements) + `<ctx>/listener` + 도메인 서비스 빈 등록(`<ctx>/config/<Ctx>DomainConfig`). Gradle 좌표 `:infrastructure:persistence`, 자바 패키지는 `com.tastyhouse.infrastructure..` 불변 (see `infrastructure/persistence/AGENTS.md`) |
| `infrastructure/redis/` | Redis 연결·`StringRedisTemplate` 빈 + rate limit 카운터(`ratelimit/RedisRateLimitCounter` — `api-common-module`의 `RateLimitCounterPort` 구현). domain을 모른다(포트가 없는 순수 기술) (see `infrastructure/redis/AGENTS.md`) |
| `web-api/` | 사용자용 REST API의 **인바운드 어댑터**(컨트롤러 + `request/`) + config·security 정책·부트스트랩. application 계층은 `web-application`이 소유한다 (see `web-api/AGENTS.md`) |
| `external-api/` | 외부 연동 어댑터 — OAuth, 결제(Toss), 이메일/SMS, 파일(S3/Firebase), 크롤링 (see `external-api/AGENTS.md`) |
| `security-module/` | 공유 보안/인증 지원 라이브러리 — 공용 JWT 인증 메커니즘 + Redis 기반 JWT 세션 저장소(RefreshToken/Blacklist/소셜 임시토큰). **Redis 연결·템플릿과 Rate Limiting은 `infrastructure:redis`로 이관됐다** (see `security-module/AGENTS.md`) |
| `api-common-module/` | web-api·admin-api·ceo-api 공유 HTTP 플럼웨어 — `ApiResponse`/`PaginationResponse`/`PageRequest`/`FileService`/`GlobalExceptionHandler`(admin·ceo 전용) (see `api-common-module/AGENTS.md`) |
| `admin-api/` | 관리자용 REST API의 **인바운드 어댑터**(컨트롤러 + `request/`) + config·security 정책·부트스트랩. application 계층은 `admin-application`이 소유한다 (see `admin-api/AGENTS.md`) |
| `admin-application/` | admin-api의 **application 계층**(컨텍스트별 인바운드 포트 + CQRS 서비스 + `response/` 표현 계약). infra를 컴파일 클래스패스에 두지 않는다 (see `admin-application/AGENTS.md`) |
| `ceo-api/` | 점주(매장 오너)용 REST API의 **인바운드 어댑터**(컨트롤러 + `request/`) + config·security 정책·부트스트랩. application 계층은 `ceo-application`이 소유한다 (see `ceo-api/AGENTS.md`) |
| `ceo-application/` | ceo-api의 **application 계층**(점주 가게 설정 관심사별 CQRS 서비스 + 인바운드 포트 + `response/` + 소유권·이미지 규격 검증기). infra를 컴파일 클래스패스에 두지 않는다 (see `ceo-application/AGENTS.md`) |
| `batch-module/` | 배치 앱의 **부트스트랩 + driving adapter**(`@Scheduled` 트리거 7종) 전담 독립 실행 모듈 (see `batch-module/AGENTS.md`) |
| `batch-application/` | 배치 잡의 **application 계층**(잡 UseCase 인바운드 포트 + 서비스 + BBQ 크롤링 동기화). infra를 컴파일 클래스패스에 두지 않는다 (see `batch-application/AGENTS.md`) |
| `web-application/` | web-api의 **application 계층**(컨텍스트별 인바운드 포트 + CQRS 서비스 + `response/` 표현 계약 + auth 토큰·principal). infra를 컴파일 클래스패스에 두지 않는다 (see `web-application/AGENTS.md`) |
| `docs/` | 설계 문서 — 소셜 로그인 가이드, 결제 연동 가이드 |
| `md/` | 아키텍처 전환 기록 — `CLEAN-ARCHITECTURE.md`(Strangler Fig 도메인 분리 → `core-module` → `domain-module` 전환의 근거·검증 결과·강제 지점) |
| `tasks/` | `core-module` → `domain-module` 전환 작업지시서(공통 지침 `README.md` + 도메인별 `NN-*.md`) |
| `gradle/` | Gradle Wrapper 바이너리/설정 |
| `json/` | 외부 자격 증명 JSON (예: Firebase 서비스 계정) |

## For AI Agents

### Working In This Directory
- **응답은 한국어로** 작성한다 (프로젝트 규칙).
- **빌드/테스트 실행 금지**: 로직 구현 후 `gradle build`/test를 자동 실행하지 않는다.
- **커밋/롤백 금지** (`NO_COMMIT_OR_ROLLBACK`): 사용자가 명시적으로 요청하지 않는 한 git 커밋·롤백을 하지 않는다.
- 네이밍은 명확하고 의미 있는 이름을 선택한다.
- 변경 전 반드시 [CLAUDE.md](CLAUDE.md#도메인-모델--jpa-엔티티-분리-규칙-선별-적용-persistence는-infrastructure-module로)의 레이어 의존 규칙을 따른다. 아키텍처가 왜 현재 형태인지(전환 근거·검증 결과·빌드 그래프가 강제하는 것)는 [md/CLEAN-ARCHITECTURE.md](md/CLEAN-ARCHITECTURE.md) 참고.
- **DTO 조립은 `new` 직접 호출을 지양**한다: 컨트롤러·Service 등 호출부에서 command/condition/response record를 `new`로 조립하지 않고, 대상 record 자신의 정적 팩토리 `of(...)`/`from(...)`로 위임한다. Request DTO는 command 생성 책임을 지지 않는 순수 데이터 홀더로 유지하며, 컨트롤러가 Request를 원시 필드로 언패킹해 Service에 전달한다. `new`는 팩토리 메서드 내부에만 남긴다. 상세 규칙과 reference 구현(admin-api notice)은 [CLAUDE.md](CLAUDE.md#dto-조립-규칙-new-직접-호출-지양) 참고.
- **`record`는 별도 파일로 분리**한다: 서비스·컨트롤러 등 다른 클래스 본문 안에 record를 중첩 선언하지 않고, 각 관례 위치(web-api/admin-api/ceo-api는 도메인 폴더의 `response/`·`request/`, 조회 Result·SearchCondition은 `com.tastyhouse.application.<ctx>.port.out`(소유 모듈은 소비 앱 수에 따라 `{앱}-application` 또는 `domain-module`, 구현 DAO는 `infrastructure:persistence`의 `<ctx>/query/`))에 독립 `.java` 파일로 둔다. 분리 시 최상위 타입이 되므로 `public record`로 선언하고, 내부 전용 헬퍼 record도 동일하게 분리한다. 상세는 [CLAUDE.md](CLAUDE.md#record-파일-분리-규칙-중첩-record-선언-지양) 참고.
- **presentation의 도메인 결합 격리 — 도메인당 CQRS 서비스 쌍 (개정: 소유 모듈은 `{앱}-application`)**: 컨트롤러가 `domain-module`에 직접 결합되는 것을 막기 위해, 앱마다 도메인별 서비스를 두어 컨트롤러 ↔ 도메인 사이를 중개한다. 그 서비스는 **api 모듈이 아니라 `{web|admin|ceo|batch}-application`이 소유**하며(`com.tastyhouse.{web|admin|ceo|batch}application.<ctx>.service`), 컨트롤러는 그 짝인 UseCase 인터페이스(`<ctx>/port/in/`)만 주입한다.
  - `{도메인}CommandService`(`@Transactional`): domain write 포트(`XxxRepository`)와 도메인 서비스만 주입. 생성/수정/삭제/상태전이를 수행하고 식별자만 반환한다.
  - `{도메인}QueryService`(`@Transactional(readOnly = true)`): **`com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스**만 주입(`infrastructure:persistence`의 DAO 구현체를 직접 알지 않는다). 조회와 Response 조립(private 매퍼)을 담당한다.
  - 조회만 있는 도메인은 QueryService만 둔다. CommandService가 읽기 포트를, QueryService가 write 포트를 서로 주입하지 않는다. 컨트롤러는 `com.tastyhouse.domain.*`를 import하지 않고, command 결과 응답은 커밋 이후 QueryService로 재조회해 조립한다.
  - reference: `admin-application/notice/service/NoticeCommandService`·`NoticeQueryService`, `web-application/notice/service/NoticeQueryService`(조회 전용).
- **등록(POST) API는 생성된 `Long` id만 반환**한다: 리소스를 등록하는 POST는 `ResponseEntity<ApiResponse<Long>>`로 PK 하나만 반환하고, 생성 응답 전용 래퍼 record(`XxxCreateResponse`)를 만들거나 생성 직후 QueryService로 재조회해 상세 DTO를 반환하지 않는다(상세가 필요하면 클라이언트가 그 id로 GET 상세를 호출). 행을 생성하고도 `ApiResponse<Void>`를 반환하던 지점도 id 반환으로 통일하며, 벌크 등록은 `ApiResponse<List<Long>>`이다. 파일 업로드·인증/토큰 발급·검증 전용·토글/상태전이·POST-as-query·배치집계는 리소스 등록이 아니므로 적용 제외. 상세·적용 제외 목록·reference 구현은 [CLAUDE.md](CLAUDE.md#등록post-api-응답-본문-규칙-생성된-long-id만-반환) 참고.
- **api 모듈은 QueryDSL도 `com.tastyhouse.infrastructure..`도 모른다 (개정)**: web/admin/ceo/batch의 `src/main`에 `com.querydsl.*` import·`@QueryProjection` 선언·`com.tastyhouse.infrastructure..` import가 **0건**이며, 각 모듈 `architecture/LayerRulesTest`(ArchUnit)가 이를 차단한다. 챕터 04의 마이그레이션 임시 장치(`shouldNotDependOnInfrastructureQuery`, 구·신 패키지 이중 매칭)는 **챕터 05에서 전수 제거**됐고, 대신 `..adapter.in.web..`·`..application.port.in..`을 대상으로 하는 패키지 기준 규칙으로 승격했다. 조회는 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스만 주입한다.
- **컨트롤러 `@PathVariable`은 주 리소스를 `id`로 통일**한다: 컨트롤러가 이미 `@RequestMapping`으로 그 도메인에 스코프되므로, 주 리소스를 가리키는 경로 변수는 단건·중첩 경로 모두 bare `id`로 쓰고(예: `/coupons/v1/{id}`, `/coupons/v1/{id}/issues`) 한 컨트롤러 안에서 `id`/`{도메인}Id` 혼재를 금지한다. 단, 다른 애그리거트 식별자를 함께 받는 경우만 `{도메인}Id`로 구분한다. 타입은 `Long` 유지(`@PathVariable Long id`). 상세는 [CLAUDE.md](CLAUDE.md#컨트롤러-pathvariable-식별자-명명-규칙-id로-통일) 참고.
- **import 순서** (Spring Framework 공식 컨벤션 `SpringImportOrderCheck`와 동일): `java.*` → `javax.*` → 그 외 전부(`jakarta.*` 포함, org/io/com.* 등 알파벳 혼합) → 자사(`com.tastyhouse.*`) → static import(맨 아래) 순서로 그룹을 나누고, 그룹 사이 빈 줄 1개, 그룹 내부는 알파벳 순 정렬한다. 자사(`com.tastyhouse.*`) 그룹 내부는 헥사고날 의존성 방향(안→밖) 순 — domain(`com.tastyhouse.domain.<ctx>..`) → infrastructure(`com.tastyhouse.infrastructure..`, 그중 `..query..`만 api에서 허용) → external/shared(`com.tastyhouse.external..`·`com.tastyhouse.domain.shared..`·`com.tastyhouse.domain.exception..`) → presentation — 으로 정렬하고, 같은 계층 내부만 알파벳순(프로젝트 커스텀 규칙, 공식 표준 아님). presentation(`webapi`/`adminapi`/`ceoapi`) 내부는 다시 공용 인프라(`common`·`config`·`security`·`ratelimit`·`exception`)를 위(5-a), 도메인 전용(`<도메인>.request`·`.response`)을 아래(5-b)로 서브정렬한다. 상세·근거·예시는 [CLAUDE.md](CLAUDE.md#코딩-스타일-import-순서) 참고.

### Module Dependency Graph
```
── 실행 앱 4개 (thin adapter — 컨트롤러/트리거 + request/ + config + 부트스트랩) ──
web-api ──┬─→ web-application (implementation)          ← 컨트롤러가 컨텍스트 UseCase 포트를 주입
          ├─→ domain-module (implementation)
          ├─→ infrastructure:persistence (implementation) ← DAO 구현체는 주입하지 않지만 빈 스캔 대상이라 필요
          ├─→ external-api / security-module / api-common-module / logging-module (implementation)
admin-api  ─(동일 패턴) ─→ admin-application
ceo-api    ─(동일 패턴) ─→ ceo-application
batch-module ─(동일 패턴 — security-module·api-common-module 없음, logging-module은 p6spy exclude)
                        └→ batch-application   ← 스케줄러가 잡 UseCase 포트를 주입

── 앱별 application 계층 4개 (infra 의존 없음이 핵심) ──
web-application ─┬→ domain-module (implementation)   ← 공유 읽기 계약도 여기 있다(앱 단독 계약은 자기 모듈 소유)
                 ├→ security-module (implementation)      ← auth/token의 JwtProperties·Redis 토큰 저장소
                 ├→ external-api (implementation)         ← 소셜 로그인 SPI
                 ├→ api-common-module (implementation)    ← PaginationResponse 등 표현 계약
                 ├→ spring-boot-starter-web (implementation) ← MultipartFile 업로드 경계 타입 전용
                 └→ spring-tx (implementation)            ← @Transactional 전용
admin-application ─(동일 — external-api 없음(소셜 로그인 부재), starter-web 직접 선언 없음,
                            starter-security 추가(AuthenticationManager·PasswordEncoder))
ceo-application   ─(동일 — external-api 없음, starter-web 있음(MultipartFile), starter-security 추가)
batch-application ─┬→ domain-module (implementation)
                   ├→ external-api (implementation)        ← BbqApiClient·RemoteImageDownloader
                   └→ spring-tx (implementation)           ← @Transactional 전용, infra 제외로 드러난 의존
   ※ 4개 모두 infrastructure 의존 없음 — 계층 분리를 빌드 그래프가 강제한다
     (`import com.tastyhouse.infrastructure...` 한 줄이 컴파일 에러)

── 공유 모듈 ──
infrastructure:persistence ─┬→ domain-module (api)
                            └→ {web,admin,ceo,batch}-application (implementation) ← QueryDao가 각 앱 소유 {Ctx}QueryPort를 구현
infrastructure:redis ─→ (내부 모듈 의존 없음)   ← domain에 포트가 없는 순수 기술이라 domain조차 모른다
external-api ─→ domain-module (implementation)   ← domain <ctx>/port 구현
security-module ─┬→ domain-module (implementation) ← ErrorCode만(JwtEntryPoint·AccessDeniedHandler)
                 └→ infrastructure:redis (implementation) ← 토큰 저장소가 StringRedisTemplate 사용
api-common-module ─┬→ domain-module (api)            ← PageResult·FileUploadService가 공개 시그니처에 노출
                   └→ security-module (implementation)
domain-module → 의존 없음 (production 의존 0개)
```
- **`domain-module`은 프레임워크를 모른다**: 다른 모듈에 의존하지 않으며, Spring(Web/tx/orm)·JPA·QueryDSL 전부 의존이 없다. HTTP 상태는 `ErrorCode.httpStatusCode`(int)로, 낙관적 락 충돌은 프레임워크-프리 `OptimisticLockConflictException`으로 표현한다(스프링 예외 번역은 `infrastructure:persistence`의 `RepositoryImpl` 담당). persistence·조회·이벤트 발행·도메인 서비스 빈 등록은 전부 `infrastructure:persistence`가 전담한다.
- **읽기 계약은 소비자 수가 소유 모듈을 정한다 (챕터 09 — `application-common-module` 해체)**: 패키지는 5개 모듈이 공유하는 `com.tastyhouse.application.<ctx>.port.out`(split package)이고, 소유는 **한 앱만 쓰면 그 앱의 `{앱}-application`, 2개 이상이 쓰면 `domain-module`**이다. 패키지를 바꾸지 않았으므로 소비 측 import와 ArchUnit 패키지 규칙이 모두 무변경이다.
  - **프레임워크-프리는 유지되지만 강제 수단이 갈렸다**: `domain-module`이 소유하는 공유 계약은 루트 `build.gradle`의 spring 주입 제외 덕분에 여전히 **컴파일 게이트**(`import org.springframework...` 한 줄이 컴파일 에러)로 막힌다. 반면 `{앱}-application`은 starter를 받으므로 그 게이트가 없어, 각 모듈 `LayerRulesTest#readContractsShouldBeFrameworkFree`(ArchUnit)가 대신 강제한다(`domain-module`은 `ReadContractPurityTest`가 같은 역할 — split package라 `DomainPurityTest`의 스캔 범위 밖이다).
  - **split package의 대가는 FQCN 중복이다**: 같은 클래스가 두 모듈에 정의돼도 컴파일이 통과하고 클래스패스 순서로 한쪽이 조용히 이긴다. `infrastructure:persistence`의 `ReadContractSingleOwnerTest`가 5개 모듈이 모두 올라오는 테스트 클래스패스를 스캔해 이를 검출한다.
- **application 모듈이 읽기 계약을 보는 경로 (개정 — 과거 "infra를 컴파일 타임에 본다"는 서술의 번복)**: `{도메인}QueryService`는 이제 infra DAO 구현체가 아니라 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스를 주입한다. 앱 단독 계약은 자기 모듈에 있고 공유 계약은 이미 선언된 `implementation project(':domain-module')`로 보이므로, 이를 위한 추가 의존 선언은 없다. api 모듈은 `com.tastyhouse.infrastructure..`를 **전혀 import하지 않는다** — 각 모듈 `LayerRulesTest`가 이를 강제한다. `infrastructure:persistence`는 여전히 빈 스캔 대상(`scanBasePackages`)이라 실행 모듈의 의존 그래프에는 남아 있지만, **소스 코드 레벨의 import 대상은 아니다.**
- **`@QueryProjection` → `Projections.constructor` 전환**: Result record가 QueryDSL을 모르는 계약 모듈로 이동하며 그 record에 `@QueryProjection`을 달 수 없게 됐다. `infrastructure:persistence`의 QueryDao는 `Projections.constructor(XxxResult.class, ...)`로 리플렉션 기반 조립을 한다 — Result record가 `public`이 아니거나 생성자 시그니처가 select 절과 불일치하면 컴파일은 통과하고 **호출 시점에 500**이 나므로, 전환한 쿼리는 반드시 한 번 호출해 확인한다. 이 리플렉션 대상 일치는 `infrastructure:persistence`의 `ProjectionConstructorMatchingTest`가 소스 스캔으로 검증한다.
- `querydsl-jpa`는 `infrastructure:persistence`에서 `implementation`으로 강등되어 소비 모듈 클래스패스로 전이되지 않는다. 전 프로젝트에서 QueryDSL을 컴파일하는 모듈은 `infrastructure:persistence` 하나뿐이다.
- 실행 가능한(bootJar) 모듈은 `web-api`/`admin-api`/`ceo-api`/`batch-module` 넷뿐이며, **모듈 재편으로도 이 넷과 산출물 이름은 바뀌지 않았다**(라이브러리 모듈만 추가됐다). 나머지(`domain-module`/`{web,admin,ceo,batch}-application`/`infrastructure:persistence`/`infrastructure:redis`/`external-api`/`security-module`/`api-common-module`/`logging-module`)는 `bootJar` 비활성 + plain jar.
  - **중첩 프로젝트 컨테이너 주의**: `include 'infrastructure:persistence'`는 소스가 없는 빈 프로젝트 `:infrastructure`를 함께 만든다. 루트 `build.gradle`의 `subprojects` 일괄 설정이 이 컨테이너에까지 `bootJar`를 걸면 빌드가 깨지므로, 일괄 설정 대상에서 제외되는지 확인한다.
- **`batch-application`·`web-application`은 infrastructure를 컴파일 클래스패스에 두지 않는다**: application 계층이 infra를 모른다는 규칙을 ArchUnit이 아니라 **빌드 그래프가 1차로 강제**한다 — `import com.tastyhouse.infrastructure...` 한 줄이 실제 컴파일 에러가 된다(`domain-module`의 프레임워크-프리 게이트와 같은 방식). 그 결과 이전에 infra의 `spring-boot-starter-data-jpa`를 타고 전이로 들어오던 `spring-tx`가 드러나, `@Transactional`만을 위해 명시 선언한다. ArchUnit 규칙(`shouldNotDependOnInfrastructure`)은 누군가 build.gradle에 의존을 되돌리는 회귀를 막는 2차 방어선으로 유지한다.
- **`scanBasePackages`에 domain 엔트리 없음**: `domain-module`에 `@Component`/`@Service`/`@Configuration`이 하나도 없으므로(도메인 서비스는 POJO, 빈 등록은 infra `<ctx>/config/<Ctx>DomainConfig`), 4개 앱의 `scanBasePackages`(및 admin/ceo의 `@ComponentScan basePackages`)에서 domain 패키지 엔트리를 제거했다. 남은 엔트리는 각 앱 자신 + `com.tastyhouse.infrastructure`·`com.tastyhouse.external`·`com.tastyhouse.security`(web/admin/ceo)·`com.tastyhouse.logging`이다.
- **모듈 경계 원칙 (챕터 05 개정 — 2차원 경계)**: 모듈 경계는 이제 **계층 × 앱** 두 축이다.
  - **계층 축**: `domain-module`(순수 도메인) → `{앱}-application`(유스케이스) → api 모듈(인바운드 어댑터). `infrastructure:persistence`·`external-api`가 아웃바운드 어댑터다.
  - **앱 축**: 같은 계층이라도 web·admin·ceo·batch는 서로의 모듈을 알지 않는다(같은 이름의 서비스가 여러 모듈에 공존하는 것이 정상).
  - **infrastructure는 기술별로 나눈다**: `infrastructure:persistence`는 domain 포트의 **DB 어댑터 전용**(write `persistence` + read `query` + 이벤트 `listener`), `infrastructure:redis`는 Redis 연결·rate limiting. domain에 포트가 없는 기술이라도 **순수 인프라 기술이면 `infrastructure:{기술}`**에 두고, **여러 presentation이 공유하는 보안 관심사**일 때만 `security-module`, **HTTP 플럼빙**이면 `api-common-module`에 둔다.
  - **컨텍스트별 모듈 분할은 여전히 하지 않는다**: 컨텍스트 경계(25종)는 모듈이 아니라 `domain-module`의 ArchUnit `ContextBoundaryTest`(봉인 목록)가 담당한다.
- **api 모듈 공용 플럼빙은 `api-common-module`이 단독 소유**한다(과거 "모듈별로 각각 둠" 관례 개정): 세 모듈에 package 선언 1줄만 다르게 복제돼 있던 `ApiResponse`/`PaginationResponse`/`PageRequest`/`FileService`와 admin↔ceo 복제였던 `GlobalExceptionHandler`를 통합했다. **완전 동일한 것만** 통합하며, 내용이 다른 정책 파일(`SecurityConfig`·`PublicPaths`·`TokenService`·`AuthService`)과 계약이 다른 응답 record(`ShopDetailResponse` 등)는 복제를 유지한다 — 허용 목록은 [CLAUDE.md](CLAUDE.md#api-모듈-공용-플럼빙-소유-규칙-api-common-module) 표 참고. `GlobalExceptionHandler`는 빈이므로 **web-api는 `com.tastyhouse.apicommon.file`만 스캔**한다(자체 핸들러 유지).
- **소셜 로그인은 `external.oauth.spi` SPI로만 사용**한다: web-api는 제공자별 패키지(`..oauth.kakao..` 등)의 wire DTO·클라이언트를 직접 import하지 않고 `SocialOAuthClient`/`SocialProfile`만 안다(ArchUnit `shouldDependOnOauthSpiOnlyNotProviderPackages`가 강제). 이 SPI를 domain-module이 아니라 external-api가 소유하는 이유는 소셜 OAuth의 호출부가 전부 표현 계층이라 도메인 서비스가 쓰는 포트가 아니기 때문이다(security-module 선례와 동일 판단). 상세는 [CLAUDE.md](CLAUDE.md#소셜-로그인-spi-규칙-externaloauthspi) 참고.

### Testing Requirements
- 스키마 무변경 보장: `hibernate.ddl-auto=validate` 기준. JPA 엔티티(`infrastructure:persistence`) 변경 시 `schema.sql`과 정합성 확인.
- QueryDSL Q클래스는 `infrastructure:persistence`에서만 생성된다(`infrastructure/persistence/build/generated/...`) — 경로 변경 시 `./gradlew clean compileJava` 필요. `domain-module`에는 apt가 없어 Q타입이 생성되지 않는다.
- 도메인 불변식은 `domain-module/src/test`의 **순수 단위 테스트**로 검증한다(스프링 컨텍스트·DB 불필요).
- 레이어 경계는 각 모듈의 `architecture/LayerRulesTest`(ArchUnit)로 검증한다. 이 규칙들은 `allowEmptyShould(true)`를 쓰지 않으므로, 대상 클래스가 0건이면 **공허 통과가 아니라 실패**로 드러난다.
- **`allowEmptyShould(true)` 금지는 자동 검증된다 (신설)**: `noClasses().that()...` 형태는 대상이 0건이어도 조용히 통과하므로, 원칙을 지켰는지가 사람 눈에만 의존했다. `batch-application/src/test/.../RuleAnchorTest`가 각 규칙의 anchor 개수(`*SchedulerService` 7 · `..port.in..` 7 · response record 4)를 직접 세어, 클래스가 모듈 사이를 옮겨 다니다 대상이 통째로 사라지면 **빌드가 실패**하게 한다. 모듈을 분리·이동하는 후속 챕터에서 같은 패턴을 따른다.

### Common Patterns
- 계층 배치: `domain-module`의 `<ctx>/{model,vo,event,repository,service,port}` / `infrastructure:persistence`의 `<ctx>/{persistence,query,listener}` / api 모듈의 `<ctx>/adapter/in/web/`(컨트롤러 + `request/`) / `{앱}-application`의 `<ctx>/{port/in,service,response}`.
- 식별자 강타입화: `record MemberId(Long value)`(domain) + `AttributeConverter`(`infrastructure:persistence`의 `<ctx>/persistence/XxxIdConverter`)로 JPA 매핑.
- BC 간 통신은 도메인 서비스 호출 또는 `DomainEvent`로만. 이벤트 발행은 domain 포트 `DomainEventPublisher`(`domain/shared/event/`)를 통하고, 스프링 구현(`SpringDomainEventPublisher`)과 리스너(`<ctx>/listener/`, `@TransactionalEventListener(AFTER_COMMIT)`)는 `infrastructure:persistence`에 있다.
- CQS: 쓰기 `{도메인}CommandService`(`@Transactional`) / 읽기 `{도메인}QueryService`(`@Transactional(readOnly = true)`) — 트랜잭션 경계는 `{앱}-application`의 서비스가 소유한다(domain 서비스는 POJO라 `@Transactional`을 갖지 않는다).

## Dependencies

### External
- Spring Boot 3.2.4 (web, webflux, security, data-jpa, data-redis, aop, mail, validation)
- Java 21, Gradle (멀티모듈)
- QueryDSL `io.github.openfeign.querydsl:querydsl-jpa:6.11` (OpenFeign 포크) — 동적 쿼리. **`infrastructure:persistence`에만 `implementation`으로 의존**해 소비 모듈로 전이되지 않는다
- MySQL (`mysql-connector-j`), Redis
- JJWT 0.12.3 — JWT 발급/검증
- AWS SDK (SES, SNS, S3), Firebase Admin 9.10.0
- springdoc-openapi 2.3.0 — Swagger UI
- p6spy (SQL 로깅 — logging-module이 api로 노출하고 SQL 로그 포맷을 `application-logging.yml`에서 소유)

<!-- MANUAL: 수동 메모는 이 라인 아래에 추가하면 재생성 시 보존됩니다 -->

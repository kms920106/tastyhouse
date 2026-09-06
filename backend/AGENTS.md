<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# tastyhouse-api

## Purpose
음식점/가게(Shop) 기반 커머스 플랫폼의 백엔드. Spring Boot 3.2.4 / Java 21 기반 Gradle 멀티모듈 프로젝트로, 회원·주문·결제·리뷰·예약·쿠폰·포인트 등 22개 도메인을 제공한다. 전통적 계층형에서 시작해 **DDD / Clean Architecture(Strangler Fig 점진 전환)** 를 거쳐, `core-module` → `domain` 전환으로 **도메인 계층이 프레임워크를 전혀 모르는(production 의존 0개) 구조**에 도달했다. 비즈니스 규칙을 도메인 객체에 캡슐화하는 Rich Domain Model을 지향한다.

전환 이후 구조의 핵심 네 가지:
- **`domain`은 프레임워크-프리**다. Spring Web뿐 아니라 JPA·QueryDSL·`spring-tx`/`spring-orm`도 없다 — `@Transactional`/`@Service`/`@Component`가 한 곳도 없고, 도메인 서비스는 순수 POJO이며 빈 등록은 `infrastructure:persistence`의 컨텍스트별 `<ctx>/config/<Ctx>DomainConfig`가 담당한다.
- **api 모듈(web/admin/ceo/batch)은 QueryDSL도 infrastructure도 모른다**. 조회 계약(`{Ctx}QueryPort` 인터페이스 + `*Result`/`*SearchCondition`)은 패키지 `com.tastyhouse.application.<ctx>.port.out`에 있고, **소유 모듈은 소비자 수로 갈린다** — 한 앱만 쓰면 그 앱의 `{앱}-application`, 2개 이상이 쓰면 `domain`이다(챕터 09로 `application-common-module`이 해체되며 5개 모듈에 분산됐다. 패키지는 그대로라 소비 측 import는 바뀌지 않는다). `infrastructure:persistence`의 `{도메인}QueryDao`가 그 인터페이스를 구현한다. 각 앱의 `{도메인}QueryService`는 DAO가 아니라 포트 인터페이스를 주입하므로 `com.tastyhouse.infrastructure..` import가 **0건**이다. `com.tastyhouse.infrastructure..`(전면)·`com.querydsl..` 의존은 4개 모듈의 ArchUnit `LayerRulesTest`가 차단한다.
- **application 계층은 앱별 모듈로 물리 분리됐다 (모듈 재편 프로그램, 챕터 01~06)**. 도메인당 `{도메인}CommandService`/`{도메인}QueryService` CQRS 쌍은 이제 api 모듈이 아니라 `{web|admin|ceo|batch}-application`이 소유하고(예: `com.tastyhouse.application.notice.service.NoticeQueryService`), api 모듈은 컨트롤러 + `request/`(+ 챕터 06 적용분은 `response/`)를 갖는 **thin adapter**로 축소됐다. **`response/`의 거처는 챕터 06(admin)·09(ceo)·10(web)으로 api 모듈로 이동했다** — 3개 앱 전부 완료(admin 85개·ceo 105개·web 131개, 세 application 모듈의 `io.swagger` import 0건). 모듈 경계가 "계층 × 앱" 2차원이 되어, 계층 위반이 ArchUnit 사후 검출이 아니라 **컴파일 에러**가 된다.
- **`@QueryProjection`은 전 리포지토리에서 폐지**됐다. `infrastructure:persistence`의 QueryDao는 `Projections.constructor(XxxResult.class, ...)`로 Result record를 조립한다 — Result가 QueryDSL을 모르는 계약 모듈(현재는 `{앱}-application`·`domain`)로 이관되어 그 모듈에 apt를 붙일 수 없기 때문이다.

## Key Files
| File | Description |
|------|-------------|
| `settings.gradle` | 멀티모듈 정의 — 실행 앱 4개(`web-api`, `admin-api`, `ceo-api`, `batch-module`) + application 1개(`application` — 4개 앱 공통) + 공유 모듈(`domain`, `infrastructure:persistence`, `infrastructure:redis`, `infrastructure:{external,file-storage,firebase,aws,oauth,payment,messaging,crawling}`, `security-core`, `security-module`, `api-common-module`, `logging-module`) |
| `build.gradle` | 루트 빌드 — 전 모듈 공통 설정 (Java 21, Spring Boot 플러그인). **AWS BOM은 `infrastructure/aws/build.gradle`로 이관**됐다 — AWS SDK를 쓰는 모듈이 하나뿐이라 전 모듈 일괄 imports가 필요 없다 |
| `gradlew` | Gradle Wrapper 실행 스크립트 |
| `CLAUDE.md` | backend 고유 코딩 컨벤션 (네이밍·DTO·레이어 경계 등). AI 작업 규칙(한국어 응답, 빌드 테스트 생략, 커밋/롤백 금지)은 리포 루트 `../CLAUDE.md` |
| `schema.sql` / `insert.sql` / `alter.sql` | 스키마 및 시드 데이터 (DDL은 `ddl-auto=validate` 전제) |
| `.env`, `.env-copy` | 환경 변수 (외부 연동 키 등) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `domain/` | DDD 도메인 핵심 — 도메인 모델(POJO)/VO/이벤트/Repository write 포트/도메인 서비스/출력 포트 + `shared`·`exception`. **프레임워크-프리(production 의존 0개)** (see `domain/AGENTS.md`) |
| `infrastructure/persistence/` | domain 포트의 DB 어댑터 — `<ctx>/persistence`(write: JPA/매퍼) + `<ctx>/query`(read: QueryDSL QueryDao — `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort`를 implements) + `<ctx>/listener` + 도메인 서비스 빈 등록(`<ctx>/config/<Ctx>DomainConfig`). Gradle 좌표 `:infrastructure:persistence`, 자바 패키지는 `com.tastyhouse.infrastructure..` 불변 (see `infrastructure/persistence/AGENTS.md`) |
| `infrastructure/redis/` | Redis 연결·`StringRedisTemplate` 빈 + rate limit 카운터(`ratelimit/RedisRateLimitCounter` — `api-common-module`의 `RateLimitCounterPort` 구현). domain을 모른다(포트가 없는 순수 기술) (see `infrastructure/redis/AGENTS.md`) |
| `infrastructure/external/` | **외부 연동 코어** — `WebClientConfig`·`ExternalApiException`/`ErrorCode`·파일 저장 SPI(`FileStorageStrategy`·`FileStoragePortAdapter`). 벤더·채널 구현은 아래 6모듈로 분리됐다. **앱이 직접 의존하지 않는다** — 파일 저장은 `infrastructure/file-storage/` 스타터를 통해 전이로 실린다 (see `infrastructure/external/AGENTS.md`) |
| `infrastructure/file-storage/` | **(챕터 03 신설) 파일 저장 스타터** — 자바 코드도 auto-configuration도 없이 `infrastructure:external`(코어 SPI) + `infrastructure:firebase`(벤더 구현)를 `runtimeOnly`로 묶고 `application-file-storage.yml`이 `file.provider`와 벤더 yml import를 소유한다. **4개 앱 전부 의존** (see `infrastructure/file-storage/AGENTS.md`) |
| `infrastructure/firebase/` | Firebase Storage 파일 저장 전략. **앱이 직접 의존하지 않는다** — 스타터 `infrastructure:file-storage`가 의존하고 앱은 그 스타터만 본다 (see `infrastructure/firebase/AGENTS.md`) |
| `infrastructure/aws/` | S3·SES·SNS 어댑터. **어느 앱도 의존하지 않는다** — provider 기본값이 전부 비-AWS라 컴파일만 검증한다. 전환 절차는 (see `infrastructure/aws/AGENTS.md`) |
| `infrastructure/oauth/` | 소셜 로그인 클라이언트 4종(kakao·naver·apple·facebook) + `oauth/spi/`. **web-api만 의존** (see `infrastructure/oauth/AGENTS.md`) |
| `infrastructure/payment/` | Toss 결제 승인·취소(`PgPaymentGateway` 구현). **web-api만 의존** (see `infrastructure/payment/AGENTS.md`) |
| `infrastructure/messaging/` | 메일(JavaMail)·SMS(Solapi) 발송 + **persistence에서 이관된 `MailDomainConfig`·`SmsDomainConfig`**. **web-api만 의존** (see `infrastructure/messaging/AGENTS.md`) |
| `infrastructure/crawling/` | BBQ 메뉴 크롤링·행정동 경계 수집·원격 이미지 다운로드. **batch-module만 의존** (see `infrastructure/crawling/AGENTS.md`) |
| `web-api/` | 사용자용 REST API의 **인바운드 어댑터**(컨트롤러 + `request/`) + config·security 정책·부트스트랩. application 계층은 `application` 모듈의 `com.tastyhouse.application..`이 소유한다 (see `web-api/AGENTS.md`) |
| `security-core/` | **(챕터 03 신설)** `security-module`에서 분리된 서블릿-프리 보안 코어 — `JwtTokenProvider`(서명/파싱)와 Redis 기반 JWT 세션 저장소 6종(RefreshToken/Blacklist/소셜 임시토큰 4종). `{web,admin,ceo,batch}-application`이 이 모듈만 의존해 서블릿 스택을 컴파일 클래스패스에서 배제한다 (see `security-core/AGENTS.md`) |
| `security-module/` | 공유 보안/인증 지원 라이브러리 — `security-core`를 `api`로 재노출하고, 서블릿 결합 타입(JWT 인증 필터·EntryPoint·AccessDeniedHandler)만 잔류한다. **Redis 연결·템플릿과 Rate Limiting은 `infrastructure:redis`로 이관됐다** (see `security-module/AGENTS.md`) |
| `api-common-module/` | web-api·admin-api·ceo-api 공유 HTTP 플럼웨어 — `ApiResponse`/`PaginationResponse`/`PageRequest`/`FileService`/`GlobalExceptionHandler`(admin·ceo 전용) (see `api-common-module/AGENTS.md`) |
| `admin-api/` | 관리자용 REST API의 **인바운드 어댑터**(컨트롤러 + `request/`) + config·security 정책·부트스트랩. application 계층은 `application` 모듈의 `com.tastyhouse.application..`이 소유한다 (see `admin-api/AGENTS.md`) |
| `application/` | **4개 앱 공통의 application 계층**(컨텍스트별 인바운드 포트 + CQRS 서비스 + batch 잡 서비스·BBQ 크롤링 + auth 토큰·principal + ceo 소유권·이미지 규격 검증기) + 앱 단독 읽기 계약 271개. **자바 패키지는 챕터 03으로 `com.tastyhouse.application` 하나로 평탄화됐다** — 앱 소속은 패키지가 아니라 마커 애노테이션(`@WebApp`/`@AdminApp`/`@CeoApp`/`@BatchApp`)이 표현한다. **`response/`는 각 api 모듈로 승격됐다** — `io.swagger`·`apicommon` import가 0건이고 그 상태를 `applicationShouldNotDependOnSwagger`·`applicationShouldNotDependOnApiCommon`이 고정한다. infra를 컴파일 클래스패스에 두지 않는다 (see `application/AGENTS.md`) |
| `ceo-api/` | 점주(매장 오너)용 REST API의 **인바운드 어댑터**(컨트롤러 + `request/`) + config·security 정책·부트스트랩. application 계층은 `application` 모듈의 `com.tastyhouse.application..`이 소유한다 (see `ceo-api/AGENTS.md`) |
| `batch-module/` | 배치 앱의 **부트스트랩 + driving adapter**(`@Scheduled` 트리거 7종) 전담 독립 실행 모듈 (see `batch-module/AGENTS.md`) |
| `docs/` | 설계 문서 — 소셜 로그인 가이드, 결제 연동 가이드 |
| `gradle/` | Gradle Wrapper 바이너리/설정 |
| `json/` | 외부 자격 증명 JSON (예: Firebase 서비스 계정) |

## For AI Agents

### Working In This Directory
- **응답은 한국어로** 작성한다 (프로젝트 규칙).
- **빌드/테스트 실행 금지**: 로직 구현 후 `gradle build`/test를 자동 실행하지 않는다.
- **커밋/롤백 금지** (`NO_COMMIT_OR_ROLLBACK`): 사용자가 명시적으로 요청하지 않는 한 git 커밋·롤백을 하지 않는다.
- 네이밍은 명확하고 의미 있는 이름을 선택한다.
- 변경 전 반드시 [CLAUDE.md](CLAUDE.md#도메인-모델--jpa-엔티티-분리-규칙-선별-적용-persistence는-infrastructure-module로)의 레이어 의존 규칙을 따른다. 아키텍처가 왜 현재 형태인지(전환 근거·검증 결과·빌드 그래프가 강제하는 것)는 같은 절의 reference 구현 목록 참고.
- **DTO 조립은 `new` 직접 호출을 지양**한다: 컨트롤러·Service 등 호출부에서 command/condition/response record를 `new`로 조립하지 않고, 대상 record 자신의 정적 팩토리 `of(...)`/`from(...)`로 위임한다. Request는 `toCommand(...)`로 컨트롤러가 Command를 조립하고(완전 매핑 전략 — 과거 "원시 필드 언패킹" 규칙은 폐기), 응답 조립의 `from(...)`은 읽기 계약 `XxxResult`를 통째로 받는다(과거 "원시타입 낱개 언패킹" 규칙도 폐기 — 챕터 06). `new`는 팩토리 메서드 내부에만 남긴다. 상세 규칙과 reference 구현(admin-api notice)은 [CLAUDE.md](CLAUDE.md#dto-조립-규칙-new-직접-호출-지양) 참고.
- **`record`는 별도 파일로 분리**한다: 서비스·컨트롤러 등 다른 클래스 본문 안에 record를 중첩 선언하지 않고, 각 관례 위치(web-api/admin-api/ceo-api는 도메인 폴더의 `response/`·`request/`, 조회 Result·SearchCondition은 `com.tastyhouse.application.<ctx>.port.out`(소유 모듈은 소비 앱 수에 따라 `{앱}-application` 또는 `domain`, 구현 DAO는 `infrastructure:persistence`의 `<ctx>/query/`))에 독립 `.java` 파일로 둔다. 분리 시 최상위 타입이 되므로 `public record`로 선언하고, 내부 전용 헬퍼 record도 동일하게 분리한다. 상세는 [CLAUDE.md](CLAUDE.md#record-파일-분리-규칙-중첩-record-선언-지양) 참고.
- **presentation의 도메인 결합 격리 — 도메인당 CQRS 서비스 쌍 (개정: 소유 모듈은 `{앱}-application`)**: 컨트롤러가 `domain`에 직접 결합되는 것을 막기 위해, 앱마다 도메인별 서비스를 두어 컨트롤러 ↔ 도메인 사이를 중개한다. 그 서비스는 **api 모듈이 아니라 `{web|admin|ceo|batch}-application`이 소유**하며(`com.tastyhouse.{web|admin|ceo|batch}application.<ctx>.service`), 컨트롤러는 그 짝인 UseCase 인터페이스(`<ctx>/port/in/`)만 주입한다.
  - `{도메인}CommandService`(`@Transactional`): domain write 포트(`XxxRepository`)와 도메인 서비스만 주입. 생성/수정/삭제/상태전이를 수행하고 식별자만 반환한다.
  - `{도메인}QueryService`(`@Transactional(readOnly = true)`): **`com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스**만 주입(`infrastructure:persistence`의 DAO 구현체를 직접 알지 않는다). 조회를 담당한다. **Response 조립 주체는 api 모듈이다**(admin 챕터 06 · ceo 챕터 09 · web 챕터 10으로 3개 앱 완료) — api 모듈의 Response record가 `from(Result)`로 조립하므로 이 서비스에 매퍼가 없고 `*Result`를 그대로 반환한다.
  - 조회만 있는 도메인은 QueryService만 둔다. CommandService가 읽기 포트를, QueryService가 write 포트를 서로 주입하지 않는다. 컨트롤러는 `com.tastyhouse.domain.*`를 import하지 않고, command 결과 응답은 커밋 이후 QueryService로 재조회해 조립한다.
  - reference: `application`의 `adminapplication/notice/service/NoticeCommandService`·`NoticeQueryService`, `webapplication/notice/service/NoticeQueryService`(조회 전용).
- **등록(POST) API는 생성된 `Long` id만 반환**한다: 리소스를 등록하는 POST는 `ResponseEntity<ApiResponse<Long>>`로 PK 하나만 반환하고, 생성 응답 전용 래퍼 record(`XxxCreateResponse`)를 만들거나 생성 직후 QueryService로 재조회해 상세 DTO를 반환하지 않는다(상세가 필요하면 클라이언트가 그 id로 GET 상세를 호출). 행을 생성하고도 `ApiResponse<Void>`를 반환하던 지점도 id 반환으로 통일하며, 벌크 등록은 `ApiResponse<List<Long>>`이다. 파일 업로드·인증/토큰 발급·검증 전용·토글/상태전이·POST-as-query·배치집계는 리소스 등록이 아니므로 적용 제외. 상세·적용 제외 목록·reference 구현은 [CLAUDE.md](CLAUDE.md#등록post-api-응답-본문-규칙-생성된-long-id만-반환) 참고.
- **api 모듈은 QueryDSL도 `com.tastyhouse.infrastructure..`도 모른다 (개정)**: web/admin/ceo/batch의 `src/main`에 `com.querydsl.*` import·`@QueryProjection` 선언·`com.tastyhouse.infrastructure..` import가 **0건**이며, 각 모듈 `architecture/LayerRulesTest`(ArchUnit)가 이를 차단한다. 챕터 04의 마이그레이션 임시 장치(`shouldNotDependOnInfrastructureQuery`, 구·신 패키지 이중 매칭)는 **챕터 05에서 전수 제거**됐고, 대신 `..adapter.in.web..`·`..application.port.in..`을 대상으로 하는 패키지 기준 규칙으로 승격했다. 조회는 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스만 주입한다.
- **컨트롤러 `@PathVariable`은 주 리소스를 `id`로 통일**한다: 컨트롤러가 이미 `@RequestMapping`으로 그 도메인에 스코프되므로, 주 리소스를 가리키는 경로 변수는 단건·중첩 경로 모두 bare `id`로 쓰고(예: `/coupons/v1/{id}`, `/coupons/v1/{id}/issues`) 한 컨트롤러 안에서 `id`/`{도메인}Id` 혼재를 금지한다. 단, 다른 애그리거트 식별자를 함께 받는 경우만 `{도메인}Id`로 구분한다. 타입은 `Long` 유지(`@PathVariable Long id`). 상세는 [CLAUDE.md](CLAUDE.md#컨트롤러-pathvariable-식별자-명명-규칙-id로-통일) 참고.
- **import 순서** (Spring Framework 공식 컨벤션 `SpringImportOrderCheck`와 동일): `java.*` → `javax.*` → 그 외 전부(`jakarta.*` 포함, org/io/com.* 등 알파벳 혼합) → 자사(`com.tastyhouse.*`) → static import(맨 아래) 순서로 그룹을 나누고, 그룹 사이 빈 줄 1개, 그룹 내부는 알파벳 순 정렬한다. 자사(`com.tastyhouse.*`) 그룹 내부는 헥사고날 의존성 방향(안→밖) 순 — domain(`com.tastyhouse.domain.<ctx>..`) → infrastructure(`com.tastyhouse.infrastructure..`, 그중 `..query..`만 api에서 허용) → external/shared(`com.tastyhouse.external..`·`com.tastyhouse.domain.shared..`·`com.tastyhouse.domain.exception..`) → presentation — 으로 정렬하고, 같은 계층 내부만 알파벳순(프로젝트 커스텀 규칙, 공식 표준 아님). presentation(`webapi`/`adminapi`/`ceoapi`) 내부는 다시 공용 인프라(`common`·`config`·`security`·`ratelimit`·`exception`)를 위(5-a), 도메인 전용(`<도메인>.request`·`.response`)을 아래(5-b)로 서브정렬한다. 상세·근거·예시는 [CLAUDE.md](CLAUDE.md#코딩-스타일-import-순서) 참고.

### Module Dependency Graph
```
── 실행 앱 4개 (thin adapter — 컨트롤러/트리거 + request/ + config + 부트스트랩) ──
web-api ──┬─→ application (implementation)             ← 컨트롤러가 자기 앱의 UseCase 포트를 주입
          ├─→ domain (implementation)
          ├─→ infrastructure:persistence (runtimeOnly) ← DAO 구현체는 주입하지 않고, 챕터 02로 빈 스캔용 컴파일 참조도 필요 없어졌다(auto-configuration)
          ├─→ infrastructure:{file-storage,oauth,payment,messaging} (runtimeOnly) ← 실사용 어댑터만
          │     ※ file-storage가 external+firebase를 전이로 끌어온다 — 앱은 벤더를 모른다
          ├─→ security-module(→security-core 전이) / api-common-module (implementation) / logging-module (runtimeOnly)
admin-api  ─(동일 패턴) ─→ application
ceo-api    ─(동일 패턴) ─→ application
batch-module ─(동일 패턴 — security-module·api-common-module 없음, logging-module은 p6spy exclude)
             └→ infrastructure:{file-storage,crawling} (runtimeOnly)
   ※ admin-api·ceo-api는 infrastructure:file-storage 하나뿐이다 — 실사용이 파일 저장 하나여서
     OAuth·결제·메일·SMS·크롤링과 그 SDK를 더 이상 받지 않는다(챕터 01 분리)
                        └→ application   ← 스케줄러가 잡 UseCase 포트를 주입
   ※ 4개 api 모듈이 같은 application 모듈을 의존하므로, "자기 앱의 UseCase만 주입"은 빌드가 아니라
     각 모듈 LayerRulesTest의 adaptersShouldOnlyUseOwnAppUseCases가 강제한다(챕터 01 신설)
   ※ 챕터 02(auto-configuration 전환)로 라이브러리 모듈 의존이 `implementation` → `runtimeOnly`로 내려갔다.
     앱은 각 모듈의 `{Xxx}ModuleAutoConfiguration`을 `@Import`하지 않는다 — 모듈이
     `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록한다.
     `security-module`·`api-common-module`만 `implementation`으로 남는다 — 소비 측이 그 모듈의 구체 타입
     (`TokenService`가 쓰는 `JwtTokenProvider`, api-common의 공용 record 등)을 컴파일 타임에 직접 참조하기 때문이다

── application 계층 1개 (infra 의존 없음이 핵심) ──
application ─┬→ domain (implementation)   ← 공유 읽기 계약 55개도 여기 있다(앱 단독 271개는 이 모듈 소유)
             ├→ security-core (implementation)        ← web·admin·ceo auth/token의 JwtTokenProvider·토큰 저장소 포트 6종.
             │                                          security-module 대신 이 모듈만 의존해 서블릿 스택을 배제
             ├→ spring-security-core (implementation) ← admin·ceo AuthenticationManager·PasswordEncoder·UserDetails
             ├→ spring-web (implementation)           ← web·admin·ceo MultipartFile 업로드 경계 타입 전용(starter-web 아님)
             ├→ jackson-databind (implementation)     ← ceo ShopStorePriceVerificationCommandService의 ObjectMapper
             └→ spring-tx (implementation)            ← @Transactional 전용, infra 제외로 드러난 의존
   ※ infrastructure:* 의존 없음 — 소셜 로그인 SPI(web)·크롤링 클라이언트(batch) 계약을 이 모듈이 소유하고
     infrastructure:oauth·infrastructure:crawling이 그것을 구현한다(의존 역전). 되살리면 순환이 되어 빌드가 깨진다
   ※ api-common-module 의존 없음 — 챕터 11로 절단됐다(표현 계약 조립이 api 모듈로 승격 완료).
     빌드 그래프가 1차 방어선이고 applicationShouldNotDependOnApiCommon이 2차 방어선으로 휴면 상태로 남는다
   ※ infrastructure 의존 없음 — 계층 분리를 빌드 그래프가 강제한다
     (`import com.tastyhouse.infrastructure...` 한 줄이 컴파일 에러)
   ※ batch 유스케이스가 spring-web·spring-security-core를 컴파일 클래스패스에서 보게 되지만
     서블릿 스택(security-module·starter-web)은 여전히 없다

── 공유 모듈 ──
infrastructure:persistence ─┬→ domain (api)
                            └→ application (implementation) ← QueryDao가 앱 단독 {Ctx}QueryPort를 구현
infrastructure:redis ─┬→ security-core (implementation)     ← (챕터 01) 토큰 저장소 포트 6종을 구현하는 어댑터
                      └→ api-common-module (implementation) ← RateLimitCounterPort 구현
   ← 연결·템플릿 자체는 domain에 포트가 없는 순수 기술이라 domain을 모른다. 어댑터가 구현하는 두 계약의
     소유 모듈만 의존한다(adapter → port 방향)
infrastructure:external ─→ domain (implementation)   ← 코어: FileStoragePort 구현 + 파일 저장 SPI
   ↑ 아래 6모듈이 전부 이 코어를 implementation으로 의존한다(SPI·예외·WebClient 재사용)
infrastructure:file-storage ─→ infrastructure:external, infrastructure:firebase (둘 다 runtimeOnly)
   ← (챕터 03 신설) 자바 코드 없는 조립 전용 스타터. 앱 4개가 의존하는 유일한 파일 저장 좌표이며,
     external·firebase는 여기를 통해 앱 runtimeClasspath에 전이로 실린다(compileClasspath에는 없다)
infrastructure:firebase  ─→ infrastructure:external, domain        + firebase-admin
infrastructure:aws       ─→ infrastructure:external, infrastructure:messaging(MailProperties), domain
                                                                          + awssdk:ses/sns, spring-cloud-aws-s3(+BOM)
infrastructure:oauth     ─→ infrastructure:external, application(auth SPI), domain + jjwt
infrastructure:payment   ─→ infrastructure:external, domain        ← PgPaymentGateway 구현
infrastructure:messaging ─→ infrastructure:external, domain        + starter-mail
                            ← MailSender·SmsSender 구현 + 이 포트를 요구하는 도메인 서비스 빈 등록
infrastructure:crawling  ─→ infrastructure:external, application(배치 포트), domain
security-core ─┬→ domain (implementation)   ← ErrorCode(토큰 검증 실패 표현)
               └→ spring-security-core (api) + jjwt-api (api)/jjwt-impl·jjwt-jackson (runtimeOnly)
   ← (챕터 03 신설) security-module에서 서블릿-프리 타입(JwtTokenProvider·토큰 저장소 계약)만 분리. 서블릿 스택(starter-web·jakarta.servlet) 의존 없음
   ← (챕터 01) 토큰 저장소 6종은 여기 포트만 남았다(RefreshToken/Blacklist/소셜 임시토큰 4종). StringRedisTemplate으로
     키를 조립하는 구현은 infrastructure:redis의 token 패키지가 갖는다 — 그 결과 security-core → infrastructure:redis
     간선이 사라졌고, api-common-module을 batch까지 끌고 가던 전이 사슬도 함께 끊겼다(CLAUDE.md 감사표 참고)
security-module ─┬→ domain (implementation) ← ErrorCode만(JwtAuthenticationEntryPoint·JwtAccessDeniedHandler)
                 └→ security-core (api)             ← (챕터 03) 잔류한 서블릿 결합 타입(필터·EntryPoint·AccessDeniedHandler)이 JwtTokenProvider·토큰 저장소 포트를 쓰고, api 3모듈에도 전이로 노출. jjwt 3줄은 security-core로 이관되어 제거(전이 수신)
api-common-module ─┬→ domain (api)                  ← PageResult가 PaginationResponse.from의 공개 시그니처에 노출
                   │                                       (BusinessException·ErrorCode는 GlobalExceptionHandler 내부 사용)
                   ├→ starter-web·starter-validation (api) ← GlobalExceptionHandler·ApiResponse
                   ├→ spring-security-core (implementation)
                   ├→ starter-aop (implementation)         ← RateLimitAspect
                   └→ springdoc-openapi-starter-webmvc-ui (api)
   ← security-module 의존은 없다(과거 서술 정정). rate limit이 이 모듈로 이관되며 방향이 뒤집혔다 —
     지금은 infrastructure:redis가 이 모듈의 RateLimitCounterPort를 구현한다
domain → 의존 없음 (production 의존 0개)
```
- **`domain`은 프레임워크를 모른다**: 다른 모듈에 의존하지 않으며, Spring(Web/tx/orm)·JPA·QueryDSL 전부 의존이 없다. HTTP 상태는 `ErrorCode.httpStatusCode`(int)로, 낙관적 락 충돌은 프레임워크-프리 `OptimisticLockConflictException`으로 표현한다(스프링 예외 번역은 `infrastructure:persistence`의 `RepositoryImpl` 담당). persistence·조회·이벤트 발행·도메인 서비스 빈 등록은 전부 `infrastructure:persistence`가 전담한다.
- **읽기 계약은 전부 `application`이 소유한다 (챕터 04 — 소비자 수 판정 폐기)**: 패키지 `com.tastyhouse.application.<ctx>.port.out`을 이 한 모듈이 단독 소유한다. 한때 소유 모듈을 소비 앱 수로 갈라(한 앱이면 `{앱}-application`, 2개 이상이면 `domain`) split package가 됐으나, application 모듈 통합으로 근거였던 앱 간 수평 의존 회피가 무의미해져 공유 계약 55개를 되돌렸다. 패키지를 바꾼 적이 없으므로 소비 측 import와 ArchUnit 패키지 규칙은 그때도 지금도 무변경이다.
  - **프레임워크-프리는 ArchUnit이 강제한다**: `application`은 spring starter를 받아 컴파일 게이트가 없으므로, `LayerRulesTest#readContractsShouldBeFrameworkFree`가 계약 전체(공유분 55개 포함)를 검사한다. `domain`의 컴파일 게이트가 공유 계약을 막아 주던 시절의 `ReadContractPurityTest`와 persistence의 `ReadContractSingleOwnerTest`는 split package와 함께 삭제됐다 — 같은 모듈 안의 FQCN 중복은 컴파일 에러라 가드가 필요 없다.
- **application 모듈이 읽기 계약을 보는 경로 (개정 — 과거 "infra를 컴파일 타임에 본다"는 서술의 번복)**: `{도메인}QueryService`는 이제 infra DAO 구현체가 아니라 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스를 주입한다. 계약이 전부 자기 모듈에 있으므로 이를 위한 추가 의존 선언은 없다(챕터 04로 공유 계약까지 돌아왔다). api 모듈은 `com.tastyhouse.infrastructure..`를 **전혀 import하지 않는다** — 각 모듈 `LayerRulesTest`가 이를 강제한다. `infrastructure:persistence`는 여전히 빈 스캔 대상이라(챕터 02 이후 앱의 `scanBasePackages`가 아니라 `PersistenceModuleAutoConfiguration`이 스캔한다) 실행 모듈의 **런타임** 의존 그래프에는 남아 있지만, `runtimeOnly`로 내려가 **컴파일 클래스패스에도, 소스 코드 레벨의 import 대상에도 없다.**
- **`@QueryProjection` → `Projections.constructor` 전환**: Result record가 QueryDSL을 모르는 계약 모듈로 이동하며 그 record에 `@QueryProjection`을 달 수 없게 됐다. `infrastructure:persistence`의 QueryDao는 `Projections.constructor(XxxResult.class, ...)`로 리플렉션 기반 조립을 한다 — Result record가 `public`이 아니거나 생성자 시그니처가 select 절과 불일치하면 컴파일은 통과하고 **호출 시점에 500**이 나므로, 전환한 쿼리는 반드시 한 번 호출해 확인한다. 이 리플렉션 대상 일치는 `infrastructure:persistence`의 `ProjectionConstructorMatchingTest`가 소스 스캔으로 검증한다.
- `querydsl-jpa`는 `infrastructure:persistence`에서 `implementation`으로 강등되어 소비 모듈 클래스패스로 전이되지 않는다. 전 프로젝트에서 QueryDSL을 컴파일하는 모듈은 `infrastructure:persistence` 하나뿐이다.
- 실행 가능한(bootJar) 모듈은 `web-api`/`admin-api`/`ceo-api`/`batch-module` 넷뿐이며, **모듈 재편으로도 이 넷과 산출물 이름은 바뀌지 않았다**(라이브러리 모듈만 추가됐다). 나머지(`domain`/`application`/`infrastructure:persistence`/`infrastructure:redis`/`infrastructure:{external,firebase,aws,oauth,payment,messaging,crawling}`/`security-core`/`security-module`/`api-common-module`/`logging-module`)는 `bootJar` 비활성 + plain jar.
  - **중첩 프로젝트 컨테이너 주의**: `include 'infrastructure:persistence'`는 소스가 없는 빈 프로젝트 `:infrastructure`를 함께 만든다. 루트 `build.gradle`의 `subprojects` 일괄 설정이 이 컨테이너에까지 `bootJar`를 걸면 빌드가 깨지므로, 일괄 설정 대상에서 제외되는지 확인한다.
- **`application` 모듈은 infrastructure를 컴파일 클래스패스에 두지 않는다**: application 계층이 infra를 모른다는 규칙을 ArchUnit이 아니라 **빌드 그래프가 1차로 강제**한다 — `import com.tastyhouse.infrastructure...` 한 줄이 실제 컴파일 에러가 된다(`domain`의 프레임워크-프리 게이트와 같은 방식). 그 결과 이전에 infra의 `spring-boot-starter-data-jpa`를 타고 전이로 들어오던 `spring-tx`가 드러나, `@Transactional`만을 위해 명시 선언한다. ArchUnit 규칙(`shouldNotDependOnInfrastructure`)은 누군가 build.gradle에 의존을 되돌리는 회귀를 막는 2차 방어선으로 유지한다.
- **모듈 등록은 `scanBasePackages`/`@Import` 조합이 아니라 auto-configuration이다 (챕터 02 개정)**: 과거 4개 앱의 `{Xxx}Application.java`는 `@Import({InfrastructureModuleConfig, RedisModuleConfig, ExternalModuleConfig, ...})`로 라이브러리 모듈 설정 클래스를 일일이 나열해 조합했다. 지금은 각 라이브러리 모듈이 `{Xxx}ModuleAutoConfiguration`(`@AutoConfiguration`) + `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 **자기 자신을 등록**하고, 앱의 `@Import`는 그 앱 정체성인 `{App}ApplicationConfig` 하나만 남는다. "클래스패스 존재 = 활성화"가 새 원칙이며, 전이로 끌려온 앱에서도 안전하게 발화(또는 비발화)하도록 각 auto-configuration이 `@ConditionalOnWebApplication`·`@ConditionalOnBean`·`@ConditionalOnMissingBean` 등으로 스스로 답한다. 상세는 `backend/CLAUDE.md`의 모듈 등록 컨벤션 절 참고.
- **`scanBasePackages`는 4개 앱 전부에서 사라졌다 (챕터 02)**: 과거에는 각 앱 자신 + `com.tastyhouse.infrastructure`·`com.tastyhouse.external`·`com.tastyhouse.security`(web/admin/ceo)·`com.tastyhouse.logging`을 나열했고, `domain`에는 `@Component`/`@Service`/`@Configuration`이 하나도 없어(도메인 서비스는 POJO, 빈 등록은 infra `<ctx>/config/<Ctx>DomainConfig`) domain 엔트리만 먼저 제거된 상태였다. auto-configuration 전환으로 라이브러리 모듈이 각자 자기 패키지를 스캔하게 되면서 **나열 자체가 없어졌고**, 4개 앱 부트스트랩에는 `@SpringBootApplication`의 기본 스캔(앱 자신의 패키지)만 남는다. 도메인에 새 POJO 서비스를 추가할 때도 스캔 엔트리를 되살리지 말고 해당 컨텍스트의 `<Ctx>DomainConfig`에 `@Bean`을 추가한다.
- **모듈 경계 원칙 (챕터 05 개정 — 2차원 경계)**: 모듈 경계는 이제 **계층 × 앱** 두 축이다.
  - **계층 축**: `domain`(순수 도메인) → `{앱}-application`(유스케이스) → api 모듈(인바운드 어댑터). `infrastructure:persistence`·`infrastructure:redis`와 `infrastructure:{external,file-storage,firebase,aws,oauth,payment,messaging,crawling}` 8모듈이 아웃바운드(driven) 어댑터다(`file-storage`만은 코드 없는 조립 스타터라 어댑터를 갖지 않고 external+firebase를 묶기만 한다).
  - **앱 축**: 같은 계층이라도 web·admin·ceo·batch는 서로의 모듈을 알지 않는다(같은 이름의 서비스가 여러 모듈에 공존하는 것이 정상).
  - **infrastructure는 기술별로 나눈다**: `infrastructure:persistence`는 domain 포트의 **DB 어댑터 전용**(write `persistence` + read `query` + 이벤트 `listener`), `infrastructure:redis`는 Redis 연결·rate limiting, `infrastructure:external`과 그 벤더·채널 6모듈(`firebase`·`aws`·`oauth`·`payment`·`messaging`·`crawling`) + 조립 스타터 `file-storage`가 외부 시스템 연동 어댑터다 — **driven adapter는 DB·Redis뿐 아니라 외부 연동까지 전부 `infrastructure:{기술}` 아래에 둔다**(모듈명과 자바 패키지명은 다를 수 있다: 이 중 `file-storage`를 뺀 7모듈이 `com.tastyhouse.external..`을 나눠 소유한다 — `file-storage`는 자바 코드가 없어 소유할 패키지가 없다). **외부 연동을 벤더·채널 단위까지 쪼개는 기준은 "앱별 실사용 차이"다** — admin·ceo가 파일 저장 하나만 쓰는데 OAuth·결제·메일·SMS와 AWS·Firebase SDK를 통째로 받고 있었다. domain에 포트가 없는 기술이라도 **순수 인프라 기술이면 `infrastructure:{기술}`**에 두고, **여러 presentation이 공유하는 보안 관심사**일 때만 `security-module`, **HTTP 플럼빙**이면 `api-common-module`에 둔다.
  - **컨텍스트별 모듈 분할은 여전히 하지 않는다**: 컨텍스트 경계(25종)는 모듈이 아니라 `domain`의 ArchUnit `ContextBoundaryTest`(봉인 목록)가 담당한다.
- **api 모듈 공용 플럼빙은 `api-common-module`이 단독 소유**한다(과거 "모듈별로 각각 둠" 관례 개정): 세 모듈에 package 선언 1줄만 다르게 복제돼 있던 `ApiResponse`/`PaginationResponse`/`PageRequest`/`FileService`와 admin↔ceo 복제였던 `GlobalExceptionHandler`를 통합했다. **완전 동일한 것만** 통합하며, 내용이 다른 정책 파일(`SecurityConfig`·`PublicPaths`·`TokenService`·`AuthService`)과 계약이 다른 응답 record(`ShopDetailResponse` 등)는 복제를 유지한다 — 허용 목록은 [CLAUDE.md](CLAUDE.md#api-모듈-공용-플럼빙-소유-규칙-api-common-module) 표 참고. `GlobalExceptionHandler`는 빈이므로 web-api의 자체 핸들러와 충돌할 수 있는데, **챕터 02 이후 이것은 스캔 범위가 아니라 조건부 `@Bean`으로 해소된다** — `ApiCommonModuleAutoConfiguration`의 `@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)`가 web에서 스스로 물러난다. (`FileService`는 이후 계층 재배치로 `application`의 유스케이스가 됐고 `apicommon.file` 패키지는 없다.)
- **소셜 로그인은 `external.oauth.spi` SPI로만 사용**한다: web-api는 제공자별 패키지(`..oauth.kakao..` 등)의 wire DTO·클라이언트를 직접 import하지 않고 `SocialOAuthClient`/`SocialProfile`만 안다(ArchUnit `shouldDependOnOauthSpiOnlyNotProviderPackages`가 강제). 이 SPI를 domain이 아니라 `infrastructure:oauth`(분리 전 `infrastructure:external`)가 소유하는 이유는 소셜 OAuth의 호출부가 전부 표현 계층이라 도메인 서비스가 쓰는 포트가 아니기 때문이다(security-module 선례와 동일 판단). 상세는 [CLAUDE.md](CLAUDE.md#소셜-로그인-spi-규칙-application의-authportout) 참고.

### Testing Requirements
- 스키마 무변경 보장: `hibernate.ddl-auto=validate` 기준. JPA 엔티티(`infrastructure:persistence`) 변경 시 `schema.sql`과 정합성 확인.
- QueryDSL Q클래스는 `infrastructure:persistence`에서만 생성된다(`infrastructure/persistence/build/generated/...`) — 경로 변경 시 `./gradlew clean compileJava` 필요. `domain`에는 apt가 없어 Q타입이 생성되지 않는다.
- 도메인 불변식은 `domain/src/test`의 **순수 단위 테스트**로 검증한다(스프링 컨텍스트·DB 불필요).
- 레이어 경계는 각 모듈의 `architecture/LayerRulesTest`(ArchUnit)로 검증한다. 이 규칙들은 `allowEmptyShould(true)`를 쓰지 않으므로, 대상 클래스가 0건이면 **공허 통과가 아니라 실패**로 드러난다.
- **`allowEmptyShould(true)` 금지는 자동 검증된다 (신설)**: `noClasses().that()...` 형태는 대상이 0건이어도 조용히 통과하므로, 원칙을 지켰는지가 사람 눈에만 의존했다. `application/src/test/.../RuleAnchorTest`(앱별 하한)와 `BatchSchedulerRulesTest`(batch exact — `*SchedulerService` 7 · `..port.in..` 7 · response record 4)가 각 규칙의 anchor 개수를 직접 세어, 클래스가 모듈 사이를 옮겨 다니다 대상이 통째로 사라지면 **빌드가 실패**하게 한다. 모듈을 분리·이동하는 후속 챕터에서 같은 패턴을 따른다.

### Common Patterns
- 계층 배치: `domain`의 `<ctx>/{model,vo,event,repository,service,port}` / `infrastructure:persistence`의 `<ctx>/{persistence,query,listener}` / api 모듈의 `<ctx>/adapter/in/web/`(컨트롤러 + `request/` + **`response/`** — admin 챕터 06 · ceo 챕터 09 · web 챕터 10으로 3개 앱 전부) / `{앱}-application`의 `<ctx>/{port/in,service}`.
- 식별자 강타입화: `record MemberId(Long value)`(domain) + `AttributeConverter`(`infrastructure:persistence`의 `<ctx>/persistence/XxxIdConverter`)로 JPA 매핑.
- BC 간 통신은 도메인 서비스 호출 또는 `DomainEvent`로만. 이벤트 발행은 domain 포트 `DomainEventPublisher`(`domain/shared/event/`)를 통하고, 스프링 구현(`SpringDomainEventPublisher`)과 리스너(`<ctx>/listener/`, `@TransactionalEventListener(AFTER_COMMIT)`)는 `infrastructure:persistence`에 있다.
- CQS: 쓰기 `{도메인}CommandService`(`@Transactional`) / 읽기 `{도메인}QueryService`(`@Transactional(readOnly = true)`) — 트랜잭션 경계는 `{앱}-application`의 서비스가 소유한다(domain 서비스는 POJO라 `@Transactional`을 갖지 않는다).

## Dependencies

### 루트 `build.gradle`이 소유하는 것
- **버전 단일 관리** — `ext.springBootVersion`(BOM을 직접 import 하는 `domain` 블록용. 위 `plugins` 블록의 플러그인 버전과 **일치시킬 것**)과 `ext.springdocVersion`. springdoc 좌표는 Spring Boot BOM이 관리하지 않으므로 여기서 단일 관리하며, `web/admin/ceo-api`·`api-common-module` 4곳이 이 값을 참조한다.
- **`ext['commons-lang3.version'] = '3.18.0'`** — Spring Boot 3.2.4 BOM이 고정하는 3.13.0이 **CVE-2025-48924**에 해당해 패치 버전으로 오버라이드한 것이다(BOM 관리 프로퍼티 재정의). 보안 목적이므로 BOM 버전과 맞추려고 되돌리지 않는다.
- **`ext['netty.version'] = '4.1.137.Final'` · `ext['jackson-bom.version'] = '2.21.6'`** — 같은 기법의 보안 오버라이드다. **Spring Boot 3.2.4 BOM이 netty 4.1.107·jackson 2.15.4를 고정하는 바람에, `firebase-admin`이 요구하는 더 새 버전을 오히려 취약 버전으로 끌어내린다**(CVE-2025-58057/58056, CVE-2025-24970 등). 즉 이 프로젝트가 낡은 라이브러리를 쓰는 것이 아니라 **BOM이 downgrade한 결과**이므로, 해소는 라이브러리 좌표가 아니라 BOM 프로퍼티 재정의로 한다(`commons-lang3`와 동일한 형태).
  - **되돌리지 않는다.** "BOM이 관리하는데 왜 버전을 박아뒀나"로 보여 정리 대상처럼 읽히지만, 지우는 순간 세 CVE가 조용히 되살아난다. Boot 버전을 올릴 때는 새 BOM이 고정하는 값이 위 버전 이상인지 확인한 뒤에만 이 두 줄을 걷어낸다.
  - 확인 방법: `./gradlew :web-api:dependencies --configuration runtimeClasspath | grep -E 'netty|jackson-core'`로 해석된 실제 버전을 본다.
- **`subprojects` 일괄 설정의 제외 대상 2개** — `domain`(프레임워크-프리 컴파일 게이트)과 `:infrastructure`(소스 없는 중첩 프로젝트 컨테이너). 각각의 근거는 [CLAUDE.md](CLAUDE.md#도메인-모델--jpa-엔티티-분리-규칙-선별-적용-persistence는-infrastructure-module로)와 위 [모듈 의존 그래프](#module-dependency-graph)의 "중첩 프로젝트 컨테이너 주의"에 있다.

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

## 계층 규칙 봉인 — api 앱 3종 공통

<!-- 분류 A. web-api·admin-api·ceo-api의 architecture/LayerRulesTest.java 공통분 -->

`web-api`·`admin-api`·`ceo-api`의 `src/test/java/com/tastyhouse/{web,admin,ceo}api/architecture/LayerRulesTest.java` **세 파일은 내용이 거의 동일하다.** 공통분을 여기에 한 번만 쓰고, 앱별 차이는 각 앱 `AGENTS.md`의 "봉인·가드 목록"에 적는다. 원문 주석은 챕터 06에서 제거되므로 이 절이 그 금지 지시의 유일한 소재지다.

### `ALLOWED_DOMAIN_ENUM_ACCESSORS` — 항목을 추가하지 않는다

**대상**: 각 앱 `src/test/java/com/tastyhouse/{web,admin,ceo}api/architecture/LayerRulesTest.java`
→ `ALLOWED_DOMAIN_ENUM_ACCESSORS`

api 모듈이 도메인 enum에 호출할 수 있는 읽기 전용 accessor. 봉인 구성원 3개 — `name` · `getDescription` · `getDisplayName`.

바이트코드 그래프 실측에서 도출했다(admin-api 기준 `name` 57 · `getDescription` 8 · `getDisplayName` 1이 전부이고 `ordinal`·`toString`·`values`는 0건).

**항목을 추가하지 않는다** — 이 목록이 커지는 것은 api 모듈이 도메인 로직을 수행하기 시작했다는 신호이므로, **목록을 늘리지 말고 그 호출을 application으로 옮긴다.**

### `apiModuleShouldBeDomainModelFree` — carve-out 3종과 그 술어 형태

**대상**: 각 앱의 `LayerRulesTest.java` → `apiModuleShouldBeDomainModelFree()`

기존 `controllersShouldBeDomainFree`는 `*ApiController` 접미어로, `requestRecordsShouldBeDomainAndInfraFree`는 `..request..` 패키지로 대상을 좁히므로 `config..`·`security..`·`exception..`이 **무검사 사각지대**였다 — 이 규칙이 그 지점을 모듈 전역으로 봉인한다.

**carve-out 3종**.

1. `domain.exception..` — 계층 칸이 없는 **횡단 관심사**다(`api-common-module`이 `api project(':domain')`로 공용 에러 계약을 전 모듈에 노출한다).
2. `domain.shared.page..` — 페이징 조립을 컨트롤러로 옮기며 **정상 경로**가 됐다(application이 `PageResult`를 반환하고 컨트롤러가 `PaginationResponse.from(...)`으로 감싼다).
3. **도메인 enum** — 아래 참조.

**도메인 enum의 읽기 전용 accessor는 위반이 아니다(타입 성격 술어).** Response 조립을 컨트롤러로 올리면서 읽기 계약 `*Result`가 품은 도메인 enum을 `result.type().name()`으로 읽는 것이 **설계상 필연**이 됐다. 규칙의 원래 의도는 **승격 방향**(String·Long → 도메인 타입)을 막는 것인데 옮겨진 것은 **강등 방향**(도메인 타입 → String)이고, ArchUnit 의존 그래프는 두 방향을 구분하지 못한다. 실측 위반 67건은 전부 읽기 전용 accessor였고 도메인 객체 생성·상태 변경·리포지토리 접근은 0건이었다.

**패키지 술어를 쓸 수 없다**: 도메인 enum 76개는 전부 `com.tastyhouse.domain.<ctx>.model`에 **애그리거트 루트와 같은 자리**에 있다. carve-out을 `resideInAPackage("..model..")`로 쓰면 `Shop`·`Order`까지 함께 열려 **규칙이 무력해진다**(패키지 술어 예외는 대상이 전부 그 패키지에 살면 규칙을 삼킨다). 그래서 위치가 아니라 **타입 성격**(`JavaClass#isEnum()`)으로 좁히고, `DOMAIN_ROOT` 패키지 조건을 함께 걸어 domain 밖 enum까지 열리지 않게 한다.

**타입 수준 carve-out만으로는 이빨이 빠진다** — 도메인 enum은 무행위 값 집합이 아니다. 76개 중 13개가 비즈니스 로직을 노출하며(`MemberGrade#fromReviewCount` 등급 배정 규칙, `OrderStatus#canTransitionTo` 상태 전이 가드), 이 규칙만 두면 컨트롤러가 그것을 호출해도 빌드가 통과한다. 짝 규칙 `apiModuleShouldOnlyReadDomainEnums`가 호출 가능 메서드를 accessor로 제한해 그 구멍을 막는다.

**⚠️ 위반은 `import`로 보이지 않는다**: ArchUnit은 import 문이 아니라 바이트코드 상수 풀을 읽으므로, 이 모듈에 `import com.tastyhouse.domain..`이 0건이어도 `*Result` 컴포넌트를 통한 **전이 의존**으로 잡힌다. 그때 대상은 `java.lang.Enum`이 아니라 **구체 enum**이다(javac가 메서드 참조 소유자로 정적 수신 타입을 기록한다). **따라서 grep으로 검증하면 "위반 0건"으로 오판한다 — 검증은 반드시 이 테스트로 한다.**

### `domainEnum()` 술어 — `domain.exception..`을 제외하는 이유

**대상**: 각 앱의 `LayerRulesTest.java` → `domainEnum()`

`isEnum()`에 `DOMAIN_ROOT` 패키지 조건을 함께 거는 이유는, 그냥 `isEnum()`이면 domain 밖 enum까지 대상이 되어 술어의 의미가 흐려지기 때문이다.

**`domain.exception..`은 제외한다** — `ErrorCode`가 enum이라서 그냥 두면 짝 규칙 `apiModuleShouldOnlyReadDomainEnums`이 전역 예외 핸들러의 `getCode()`·`getDefaultMessage()` 호출을 잡는다(web-api에서 실측 2건). 에러 계약은 클래스 수준 규칙에서도 carve-out된 **횡단 관심사**이므로 **두 규칙이 같은 예외를 공유해야 한다** — 이 술어를 두 규칙이 함께 쓰는 이유이기도 하다.

### `domainBoundaryPredicatesShouldStillBite` — 규칙 무력화를 잡는 영구 증명

**대상**: 각 앱의 `LayerRulesTest.java` → `domainBoundaryPredicatesShouldStillBite()`

위 두 규칙은 현재 위반 0건이므로, carve-out을 잘못 넓혀(예: `isEnum()` 대신 `..model..` 패키지 술어로 되돌려) **규칙이 무력해져도 그대로 통과한다.** 그 무력화를 잡는 것이 이 테스트다. "일부러 위반 코드를 넣어 확인 후 되돌린다"는 한 번 확인하고 사라지므로, 동일 술어를 조립해 판별력 자체를 상시 단정한다.

네 항목을 단정하며, **(4)가 특히 중요하다** — 술어를 `isEnum()`으로 좁힌 **이유 자체**(enum과 애그리거트 루트가 같은 패키지에 산다)를 고정하므로, 전제가 바뀌면 낡은 주석이 아니라 실패로 드러난다.

1. 애그리거트 루트 `Shop`은 여전히 금지 — **carve-out을 패키지 술어로 되돌리면 여기서 실패한다.**
2. 도메인 enum `MemberGrade`는 carve-out 대상이다.
3. 짝 규칙이 막아야 할 대상(`MemberGrade#fromReviewCount` 같은 로직 메서드)이 허용 목록 밖에 실재한다.
4. enum과 애그리거트 루트가 같은 패키지에 산다는 전제가 유지된다.

### 공허 통과 금지 — `allowEmptyShould(true)`를 쓰지 않는다

**대상**: 각 앱의 `LayerRulesTest.java` (파일 전체)

**`allowEmptyShould(true)`는 이 파일 어디에도 쓰지 않는다.** 대상이 0건이 된 규칙은 공허 통과를 여는 대신 **삭제하거나 다른 모듈로 옮긴다.**

application 계층을 대상으로 하던 규칙(`commandServicesShouldNotDependOnQueryDaos` · `queryServicesShouldNotDependOnWritePorts` · `*ShouldImplementUseCase` · `commandRecords*` · `portIn*` · `commandServicesShouldNotDependOnRequestRecords` · `applicationServicesShouldNotDependOnWebLayer`)은 전부 `application` 모듈의 동명 테스트로 이동했다 — **이 모듈에 남겨 두면 대상 0건으로 공허하게 통과하기 때문이다.**

`restControllersShouldResideInWebAdapterPackage`가 `classes()` 형태인 것은 **컨트롤러 실존에 anchor** 하기 위해서다 — 컨트롤러가 0건이 되면 곧바로 실패한다. `apiModuleShouldBeDomainModelFree`는 anchor가 모듈 전체(`noClasses()`)라 클래스가 존재하는 한 **대상 0건이 될 수 없다.**

### 인바운드 어댑터의 앱 격리 — 마커로 판정한다

**대상**: 각 앱의 `LayerRulesTest.java` (앱 격리 규칙)

앱 소속은 `@WebApp`·`@AdminApp`·`@CeoApp` 등 **마커 애노테이션**이므로 규칙도 마커로 판정한다.

- 인바운드 포트(UseCase 인터페이스)는 자기 앱 마커를 직접 달고 있어야 한다.
- Command record 등은 소속 앱을 **유도**해 그 집합이 자기 앱 마커인지 본다(유도 규칙은 `AppOwnership` 참조).

각 앱은 **자기 앱 마커가 붙은 application 슬라이스만** 의존한다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

이 절의 항목은 **코드의 특정 지점을 이렇게 바꾸지 말라는 금지 지시**다. 원문 주석은 챕터 02에서 제거되므로, 이 문서가 그 지시의 유일한 소재지다.

### `ext['jackson-bom.version'] = '2.21.6'` — 내릴 때가 아니라 올릴 때만 손댄다

**대상**: `backend/build.gradle`
→ `configure(subprojects.findAll { ... })` 블록의 `ext['jackson-bom.version']`

`firebase-admin`이 끌어오는 jackson과 정렬하려고 두는 핀이다. **Spring Boot BOM이 관리하니 불필요하다고 판단해 제거하면 안 된다** — BOM이 오히려 버전을 끌어내린다.

과거 이 값을 `2.17.3`으로 두었더니 Spring Boot BOM이 `2.18.3`까지 끌어내려 **취약 버전(`WS-2026-0003`, CVSS **7.5**)이 전 모듈에 깔린 사고가 실제로 있었다.** 그래서 이 핀은 **내릴 때가 아니라 올릴 때만 손댄다.**

**그 뒤 `2.20.0`으로 올렸으나 그것도 불충분해 `2.21.6`으로 다시 올렸다**(2026-09-06). `WS-2026-0003`은 Mend 자체 ID이고, 실체는 async 파서가 `StreamReadConstraints.maxNumberLength`를 강제하지 않아 긴 숫자 리터럴로 DoS가 가능한 결함이다(CWE-770). 연결된 권고문이 **두 개**라 수정 버전을 헷갈리기 쉽다.

| 권고문 | 영향 범위 | 수정(2.21 가지) | 수정(2.18 LTS) |
|---|---|---|---|
| `GHSA-72hv-8253-57qq` (`CVE-2026-18401`) | `2.15.0`~`2.18.5`, `2.19.0`~`2.21.0` | `2.21.1` | `2.18.6` |
| `GHSA-r7wm-3cxj-wff9` (위 수정이 불완전해 나온 후속) | `2.18.8` 미만, `2.19.0`~`2.21.3` | `2.21.4` | `2.18.8` |

즉 **`2.20.x`는 어느 쪽도 고쳐지지 않았다** — 패치가 올라간 가지는 `2.18.x`와 `2.21.x`뿐이다. 둘 다 닫는 최소 버전은 **`2.21.4`**이고, 현재 값은 그 가지의 최신 패치인 `2.21.6`이다. **`2.20.x`로 되돌리면 두 권고문이 함께 되살아난다.**

검증은 OSV에 버전을 직접 질의해서 한다 — `vulns`가 빈 배열이어야 한다.

```bash
curl -s -X POST 'https://api.osv.dev/v1/query' -H 'Content-Type: application/json' \
  -d '{"package":{"name":"com.fasterxml.jackson.core:jackson-core","ecosystem":"Maven"},"version":"2.21.6"}'
```

**Boot 버전을 올릴 때의 검증 절차** — 이 핀이 오히려 버전을 낮추고 있지 않은지 확인한다.

```bash
cd backend && ./gradlew dependencies --configuration runtimeClasspath
```

**표기 주의**: `jackson-annotations`만 patch 자리를 뗀 `'2.21'`로 해석된다(2.20부터 이어진 표기다). **오타가 아니므로 `'2.21.6'`으로 "고치지" 않는다.**

**인접 핀 2건**: 같은 블록의 `ext['commons-lang3.version']`·`ext['netty.version']`은 근거가 코드에 기록된 적이 없다. 성격이 같을 가능성은 있으나 **확인되지 않았으므로 여기에 추측을 적지 않는다.**

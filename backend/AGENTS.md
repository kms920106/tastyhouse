<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# tastyhouse-api

## Purpose
음식점/가게(Shop) 기반 커머스 플랫폼의 백엔드. Spring Boot 3.2.4 / Java 21 기반 Gradle 멀티모듈 프로젝트로, 회원·주문·결제·리뷰·예약·쿠폰·포인트 등 22개 도메인을 제공한다. 전통적 계층형에서 시작해 **DDD / Clean Architecture(Strangler Fig 점진 전환)** 를 거쳐, `core-module` → `domain` 전환으로 **도메인 계층이 프레임워크를 전혀 모르는(production 의존 0개) 구조**에 도달했다. 비즈니스 규칙을 도메인 객체에 캡슐화하는 Rich Domain Model을 지향한다.

전환 이후 구조의 핵심 네 가지:
- **`domain`은 프레임워크-프리**다. Spring Web뿐 아니라 JPA·QueryDSL·`spring-tx`/`spring-orm`도 없다 — `@Transactional`/`@Service`/`@Component`가 한 곳도 없고, domain에는 포트 없는 순수 계산기·정책만 남는다. 포트를 주입받는 도메인 서비스는 `application/<ctx>/service/`에 `@Service`로 살며, 쓰는 앱이 하나면 그 앱의 `{앱}-application`, 두 앱 이상이면 코어 `application-core`에 둔다. domain의 순수 계산기·정책은 코어의 `shared/config/SharedBeanConfig`가 `@Bean`으로 등록한다.
- **api 모듈(web/admin/ceo/batch)은 QueryDSL도 infrastructure도 모른다**. 조회 계약(`{Ctx}QueryPort` 인터페이스 + `*Result`/`*SearchCondition`)은 패키지 `com.tastyhouse.application.<ctx>.port.out`에 있고 전부 코어 `application-core`가 소유한다. `infrastructure:jpa`의 `{도메인}QueryAdapter`가 그 인터페이스를 구현한다. 조회 유스케이스 서비스는 DAO가 아니라 포트 인터페이스를 주입하므로 `com.tastyhouse.infrastructure..` import가 **0건**이다. `com.tastyhouse.infrastructure..`·`com.querydsl..` 의존은 4개 모듈의 ArchUnit `LayerRulesTest`가 차단한다.
- **application 계층은 코어 + 앱 모듈 4개로 물리 분리돼 있다**. 앱 하나만 쓰는 UseCase·Command·서비스·앱 전용 SPI 포트는 `{web|admin|ceo|batch}-application`이, 2개 앱 이상이 쓰는 도메인 서비스·리스너·`@Configuration`·`port.out` 계약은 `application-core`가 소유한다([ADR-0036](../docs/adr/0036-app-marker-removal.md)). web·admin·ceo는 **유스케이스 하나 = 포트 하나 = 서비스 하나**다(예: web `com.tastyhouse.application.notice.service.NoticeListQueryService`, admin `NoticeManagementListQueryService`, ceo `com.tastyhouse.application.shop.service.ShopRequestListQueryService`). batch는 `*SchedulerService` 1:1이다. 규칙은 [CLAUDE.md](CLAUDE.md#application-서비스-cqrs-분리-규칙-도메인commandservice도메인queryservice). api 모듈은 컨트롤러 + `request/` + `response/`를 갖는 **thin adapter**이며(세 application 모듈의 `io.swagger` import 0건), 모듈 경계가 "계층 × 앱" 2차원이라 계층 위반이 ArchUnit 사후 검출이 아니라 **컴파일 에러**가 된다.
- **`@QueryProjection`은 전 리포지토리에서 폐지**됐다. `infrastructure:jpa`의 QueryAdapter는 `Projections.constructor(XxxResult.class, ...)`로 Result record를 조립한다 — Result가 QueryDSL을 모르는 `application-core`에 있어 그 모듈에 apt를 붙일 수 없기 때문이다.

## Key Files
| File | Description |
|------|-------------|
| `settings.gradle` | 멀티모듈 정의(include 38줄 + 암묵 `infrastructure` 컨테이너 = 39개) — 실행 앱 4개(`web-api`, `admin-api`, `ceo-api`, `batch-module`) + application 5개(코어 `application-core` + 앱 모듈 `web-application`·`admin-application`·`ceo-application`·`batch-application`) + 공유 모듈(`domain`, `infrastructure:{persistence,mysql,jpa,mybatis,redis,restclient,file-storage,firebase,aws-s3,aws-ses,aws-sns,oauth,kakao-oauth,naver-oauth,apple-oauth,facebook-oauth,pg,tosspayments,mail,javamail,sms,solapi,bbq,admdongkor}`, `security-core`, `security-module`, `api-common-module`, `logging-module`) |
| `build.gradle` | 루트 빌드 — 전 모듈 공통 설정 (Java 21, Spring Boot 플러그인). **spring-cloud-aws BOM은 `infrastructure/aws-s3/build.gradle`로 이관**됐다 — spring-cloud-aws SDK를 쓰는 모듈이 `aws-s3` 하나뿐이라 전 모듈 일괄 imports가 필요 없다(ses·sns는 awssdk BOM을 각자 갖는다). **라이브러리 모듈의 BOM은 `dependencyManagement { mavenBom }`이 아니라 `implementation platform(…)`으로 선언한다** — 블록 방식은 소비 앱으로 전파되지 않아 버전 없는 의존이 앱에서 FAILED가 된다(`infrastructure/aws-s3/AGENTS.md` §Dependencies) |
| `gradlew` | Gradle Wrapper 실행 스크립트 |
| `CLAUDE.md` | backend 고유 코딩 컨벤션 (네이밍·DTO·레이어 경계 등). AI 작업 규칙(한국어 응답, 빌드 테스트 생략, 커밋/롤백 금지)은 리포 루트 `../CLAUDE.md` |
| `schema.sql` / `insert.sql` / `alter.sql` | 스키마 및 시드 데이터 (DDL은 `ddl-auto=validate` 전제) |
| `.env`, `.env-copy` | 환경 변수 (외부 연동 키 등) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `domain/` | DDD 도메인 핵심 — 도메인 모델(POJO)/VO/이벤트 타입/포트 없는 순수 계산기·정책 + `shared`·`exception`. write 포트·출력 포트·포트 주입 도메인 서비스·`DomainEventPublisher`는 `application-core`에 있다. **프레임워크-프리(production 의존 0개)** (see `domain/AGENTS.md`) |
| `infrastructure/jpa/` | application `port.out`의 DB 어댑터(JPA·QueryDSL) — `<ctx>/persistence`(write: JPA 엔티티 · 엔티티↔도메인 매퍼 `XxxMapper` · `port/out/write/XxxLoadPort`·`XxxSavePort`를 함께 구현하는 `XxxPersistenceAdapter`) + `<ctx>/query`(read: QueryDSL QueryAdapter — `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort`를 implements, **domain을 모른다** — `queryShouldNotDependOnDomain`). 리스너·도메인 서비스 등록·이벤트 발행 어댑터는 없다(전부 `application-core`). Gradle 좌표 `:infrastructure:jpa`, 자바 패키지 `com.tastyhouse.infrastructure.jpa..`, 진입 설정 `JpaModuleConfig` (see `infrastructure/jpa/AGENTS.md`) |
| `infrastructure/persistence/` | **[조립·스타터]** 자바 코드 없는 DB 조립 모듈 — `runtimeOnly` `:infrastructure:jpa` + `runtimeOnly` `:infrastructure:mysql`, `application-persistence.yml`(`application-mysql.yml`·`application-jpa.yml` import만 — datasource·`spring.sql.init`은 `infrastructure:mysql` 소유). 4앱이 의존하는 DB 좌표이며 mybatis는 조립하지 않는다 (see `infrastructure/persistence/AGENTS.md`) |
| `infrastructure/mybatis/` | MyBatis 영속 어댑터 — notice 레거시 구현(쓰기 `NoticeMyBatisPersistenceAdapter` + 조회 `NoticeMyBatisQueryAdapter`). `NoticeLoadPort`·`NoticeSavePort`·`NoticeQueryPort`·`NoticeManagementQueryPort`를 jpa 모듈의 JPA 구현과 함께 구현하고 조건 없이 등록되지만, `@Primary`가 JPA 구현에만 있어 주입되지 않는다(전환은 `@Primary`를 옮기고 재빌드). web-api·admin-api가 `runtimeOnly`로 의존. 자바 패키지 `com.tastyhouse.infrastructure.mybatis..` (see `infrastructure/mybatis/AGENTS.md`) |
| `infrastructure/redis/` | Redis 연결·`StringRedisTemplate` 빈 + rate limit 카운터(`ratelimit/RedisRateLimitCounter` — `security-core`의 `RateLimitCounterPort` 구현) + 토큰 저장소 어댑터(`token/`). domain을 모른다(포트가 없는 순수 기술) (see `infrastructure/redis/AGENTS.md`) |
| `infrastructure/restclient/` | **외부 연동 HTTP 코어** — Boot `RestClient.Builder`를 꾸미는 `RestClientConfig`만 갖고, 예외·에러코드·파일 저장 SPI는 없다. 외부 HTTP 호출은 Spring `RestClient`로 통일했다(webflux 없음). **앱이 직접 의존하지 않는다** — web은 oauth(경유 kakao/naver/apple/facebook-oauth)·pg(경유 tosspayments)·sms(경유 solapi), batch는 bbq·admdongkor를 통해 전이로 실리고 admin·ceo에는 없다 (see `infrastructure/restclient/AGENTS.md`) |
| `infrastructure/file-storage/` | **[조립·스타터]** 파일 저장 스타터 — 자바 코드 없이 `infrastructure:firebase`(`FileStoragePort` 벤더 구현)를 `runtimeOnly`로 묶고 `application-file-storage.yml`이 `file.provider`와 벤더 yml import를 소유한다. **4개 앱 전부 의존** (see `infrastructure/file-storage/AGENTS.md`) |
| `infrastructure/firebase/` | Firebase Storage 파일 저장 전략. **앱이 직접 의존하지 않는다** — 스타터 `infrastructure:file-storage`가 의존하고 앱은 그 스타터만 본다 (see `infrastructure/firebase/AGENTS.md`) |
| `infrastructure/aws-s3/` | S3 파일 저장 어댑터. **어느 앱도 의존하지 않는다** — provider 기본값이 전부 비-AWS라 컴파일만 검증한다. 전환 절차는 (see `infrastructure/aws-s3/AGENTS.md`) |
| `infrastructure/aws-ses/` | SES 메일 어댑터. **어느 앱도 의존하지 않는다** — provider 기본값이 전부 비-AWS라 컴파일만 검증한다. 전환 절차는 (see `infrastructure/aws-ses/AGENTS.md`) |
| `infrastructure/aws-sns/` | SNS SMS 어댑터. **어느 앱도 의존하지 않는다** — provider 기본값이 전부 비-AWS라 컴파일만 검증한다. 전환 절차는 (see `infrastructure/aws-sns/AGENTS.md`) |
| `infrastructure/oauth/` | **[조립·스타터]** 소셜 로그인 스타터 — `file-storage`처럼 자바 코드 없이 벤더 4종을 `runtimeOnly`로 묶고 `application-oauth.yml`이 벤더 yml 4개를 import한다. 라우터는 `web-application`의 `auth/service/SocialOAuthClientRouter`(포트 비구현)다. **web-api만 의존** (see `infrastructure/oauth/AGENTS.md`) |
| `infrastructure/kakao-oauth/` | 카카오 로그인 벤더(`KakaoOAuthClient` — `SocialOAuthClientPort` 구현, `oauth.kakao.*`). 앱이 아니라 `:oauth`가 조립 (see `infrastructure/kakao-oauth/AGENTS.md`) |
| `infrastructure/naver-oauth/` | 네이버 로그인 벤더(`NaverOAuthClient`, `oauth.naver.*`). 앱이 아니라 `:oauth`가 조립 (see `infrastructure/naver-oauth/AGENTS.md`) |
| `infrastructure/apple-oauth/` | 애플 로그인 벤더(`AppleOAuthClient`, `oauth.apple.*`, jjwt). 앱이 아니라 `:oauth`가 조립 (see `infrastructure/apple-oauth/AGENTS.md`) |
| `infrastructure/facebook-oauth/` | 페이스북 로그인 벤더(`FacebookOAuthClient`, `oauth.facebook.*`). 앱이 아니라 `:oauth`가 조립 (see `infrastructure/facebook-oauth/AGENTS.md`) |
| `infrastructure/pg/` | **[조립·스타터]** 결제 PG 스타터 — 기본 벤더 tosspayments 조립(`runtimeOnly`)만 하는 자바 코드 없는 모듈. 라우터(`List<PgProviderGatewayPort>` → `PgPaymentGatewayRouter`)는 `web-application`의 `payment/service/PgPaymentGatewayRouter`(`@Service`)다. **web-api만 의존** (see `infrastructure/pg/AGENTS.md`) |
| `infrastructure/tosspayments/` | Toss 결제 승인·취소 벤더 구현(`PgProviderGatewayPort` 구현, `provider()`가 `PgProvider.TOSS`). 앱이 아니라 `:pg`가 조립 (see `infrastructure/tosspayments/AGENTS.md`) |
| `infrastructure/mail/` | **[조립·스타터]** 메일 스타터 — `mail.provider`·`mail.sender-address` + 기본 벤더 javamail 조립(`runtimeOnly`), 자바 코드 없음. `MailVerificationService`는 `web-application`의 `emailverification/service/`에 `@Service`로 있다. **web-api만 의존** (see `infrastructure/mail/AGENTS.md`) |
| `infrastructure/javamail/` | JavaMail(SMTP) 메일 발송 벤더(`JavaMailAdapter` + starter-mail). 앱이 아니라 `:mail`이 조립 (see `infrastructure/javamail/AGENTS.md`) |
| `infrastructure/sms/` | **[조립·스타터]** SMS 스타터 — `sms.provider`·`sms.sender-number` + 기본 벤더 solapi 조립(`runtimeOnly`), 자바 코드 없음. `SmsVerificationService`는 `web-application`의 `phoneverification/service/`에 `@Service`로 있다. **web-api만 의존** (see `infrastructure/sms/AGENTS.md`) |
| `infrastructure/solapi/` | Solapi SMS 발송 벤더(`SolapiSmsClient` + restclient). 앱이 아니라 `:sms`가 조립 (see `infrastructure/solapi/AGENTS.md`) |
| `infrastructure/bbq/` | BBQ 메뉴 수집·원격 이미지 다운로드. **batch-module만 의존** (see `infrastructure/bbq/AGENTS.md`) |
| `infrastructure/admdongkor/` | 행정동 경계 GeoJSON 수집(원천 `vuski/admdongkor`). **batch-module만 의존** (see `infrastructure/admdongkor/AGENTS.md`) |
| `web-api/` | 사용자용 REST API의 **인바운드 어댑터**(컨트롤러 + `request/` + `response/`) + config·security 정책·부트스트랩. application 계층은 `application-core` + `web-application`이 소유한다 (see `web-api/AGENTS.md`) |
| `security-core/` | 서블릿-프리 보안 코어 — `JwtTokenProvider`(서명/파싱), 토큰 저장소 포트(RefreshToken·Blacklist·`SocialTempTokenRepository`), rate limit 카운터 포트 `RateLimitCounterPort`(`ratelimit/`). 구현은 전부 `infrastructure:redis`. application 계층이 이 모듈만 의존해 서블릿 스택을 컴파일 클래스패스에서 배제한다 (see `security-core/AGENTS.md`) |
| `security-module/` | 공유 보안/인증 지원 라이브러리 — `security-core`를 `api`로 재노출하고, 서블릿 결합 타입(JWT 인증 필터·EntryPoint·AccessDeniedHandler)만 갖는다. Redis 연결·템플릿과 rate limit 카운터는 `infrastructure:redis` 소유다 (see `security-module/AGENTS.md`) |
| `api-common-module/` | web-api·admin-api·ceo-api 공유 HTTP 플럼빙 — `ApiResponse`/`PaginationResponse`/`PageRequest`/`ProblemDetails`/`ApiErrorCode`/`GlobalExceptionHandler`(admin·ceo 전용)/rate limit 표현 계약 (see `api-common-module/AGENTS.md`) |
| `admin-api/` | 관리자용 REST API의 **인바운드 어댑터**(컨트롤러 + `request/` + `response/`) + config·security 정책·부트스트랩. application 계층은 `application-core` + `admin-application`이 소유한다 (see `admin-api/AGENTS.md`) |
| `application-core/` | application 계층의 코어 — 2개 앱 이상이 쓰는 도메인 서비스·도메인 이벤트 리스너(`<ctx>/listener`)·`@Configuration`·`shared/**`·앱 전용 SPI 포트를 뺀 모든 `port.out` 계약. 앱 하나만 쓰는 UseCase·Command·서비스·앱 전용 SPI 포트는 앱 모듈 `web-application/`·`admin-application/`·`ceo-application/`·`batch-application/`(각 `AGENTS.md`)이 담는다. 앱 소속은 Gradle 모듈이 표현하고, 자바 패키지는 5모듈이 `com.tastyhouse.application..`을 나눠 쓴다(split package). `io.swagger`·`apicommon` import가 0건이고 그 상태를 `applicationShouldNotDependOnSwagger`·`applicationShouldNotDependOnApiCommon`이 고정한다. infra를 컴파일 클래스패스에 두지 않는다 (see `application-core/AGENTS.md`) |
| `ceo-api/` | 점주(매장 오너)용 REST API의 **인바운드 어댑터**(컨트롤러 + `request/` + `response/`) + config·security 정책·부트스트랩. application 계층은 `application-core` + `ceo-application`이 소유한다 (see `ceo-api/AGENTS.md`) |
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
- **java 주석을 쓰지 않는다**: `*.java`의 Javadoc·라인·블록 주석 전부가 금지 대상이며 main·test를 가리지 않는다. **설명할 것이 있으면 주석이 아니라 처음부터 문서에 쓴다** — 주석으로 적었다가 나중에 옮기는 것이 아니라 애초에 문서가 소재지다. 목적지는 내용의 성격으로 정한다: 금지·제약은 **그 코드를 소유한 모듈 `AGENTS.md`** 의 `## 봉인·가드 목록`, 구조·근거는 같은 파일의 `## 코드 주석에서 이관된 설계 근거`, 비즈니스 규칙은 `docs/domain/{도메인}.md`, 코드를 다시 말하는 단순 서술(`@param`/`@return` 등)은 어디에도 쓰지 않는다. 두 절은 모듈 `AGENTS.md`에 이미 신설돼 있으므로(`## 봉인·가드 목록` 21곳, `## 코드 주석에서 이관된 설계 근거` 22곳 — 후자는 패키지 단위 `src/main/.../AGENTS.md`에도 있다) 새로 만들지 말고 항목을 추가하고, **역참조 앵커 3요소**(리포 루트 기준 파일 경로 · 코드 요소명 · 내용)를 채우되 **줄 번호는 쓰지 않는다**. 주석 없는 java 파일은 문서화 누락이 아니라 **의도된 상태**이니 되살리지 않는다(잔존 주석을 발견한 경우에만 "먼저 문서에 옮기고 그다음 제거" 절차를 적용한다). ArchUnit `.because(...)`·`@DisplayName`·어노테이션·로그 메시지는 주석이 아니므로 지우지 않으며, 그래서 backend 전역 잔존 주석 기대값은 0이 아니라 **`1`**(`// noinspection BusyWait`)이다. 상세는 [CLAUDE.md](CLAUDE.md#java-주석-금지-규칙-설계-근거는-agentsmddocsdomain이-소유한다) 참고.
- 변경 전 반드시 [CLAUDE.md](CLAUDE.md#도메인-모델--jpa-엔티티-분리-규칙-선별-적용-persistence는-infrastructure-module로)의 레이어 의존 규칙을 따른다. 아키텍처가 왜 현재 형태인지(전환 근거·검증 결과·빌드 그래프가 강제하는 것)는 같은 절의 reference 구현 목록 참고.
- **DTO 조립은 `new` 직접 호출을 지양**한다: 컨트롤러·Service 등 호출부에서 command/condition/response record를 `new`로 조립하지 않고, 대상 record 자신의 정적 팩토리 `of(...)`/`from(...)`로 위임한다. Request는 `toCommand(...)`로 컨트롤러가 Command를 조립하고, 응답 조립의 `from(...)`은 읽기 계약 `XxxResult`를 통째로 받는다. `new`는 팩토리 메서드 내부에만 남긴다. 상세 규칙과 reference 구현(admin-api notice)은 [CLAUDE.md](CLAUDE.md#dto-조립-규칙-new-직접-호출-지양) 참고.
- **`record`는 별도 파일로 분리**한다: 서비스·컨트롤러 등 다른 클래스 본문 안에 record를 중첩 선언하지 않고, 각 관례 위치(web-api/admin-api/ceo-api는 도메인 폴더의 `response/`·`request/`, 조회 Result·SearchCondition은 `application-core`의 `com.tastyhouse.application.<ctx>.port.out`, 구현 DAO는 `infrastructure:jpa`의 `<ctx>/query/`)에 독립 `.java` 파일로 둔다. 분리 시 최상위 타입이 되므로 `public record`로 선언하고, 내부 전용 헬퍼 record도 동일하게 분리한다. 상세는 [CLAUDE.md](CLAUDE.md#record-파일-분리-규칙-중첩-record-선언-지양) 참고.
- **presentation의 도메인 결합 격리 — 유스케이스당 서비스 1개**: 컨트롤러가 `domain`에 직접 결합되는 것을 막기 위해, 컨트롤러 ↔ 도메인 사이를 application 서비스가 중개한다. 그 서비스는 api 모듈이 아니라 `{web|admin|ceo|batch}-application`이 소유하며, 컨트롤러는 그 짝인 UseCase 인터페이스(`<ctx>/port/in/`)만 주입한다. web·admin·ceo는 **유스케이스 하나 = 포트 하나 = 서비스 하나**다([ADR-0043](../docs/adr/0043-usecase-per-service.md)). 정본은 [CLAUDE.md](CLAUDE.md#application-서비스-cqrs-분리-규칙-도메인commandservice도메인queryservice).
  - 명령 서비스 `{도메인}{동작}Service`(`@Transactional`): domain write 포트(`XxxLoadPort`·`XxxSavePort` — 실제로 호출하는 쪽만)와 도메인 서비스만 주입. 생성/수정/삭제/상태전이를 수행하고 식별자만 반환한다.
  - 조회 서비스 `{도메인}{관점}QueryService`(`@Transactional(readOnly = true)`): **`com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스**만 주입(`infrastructure:jpa`의 DAO 구현체를 직접 알지 않는다). Response 조립 주체는 api 모듈이다 — api 모듈의 Response record가 `from(Result)`로 조립하므로 이 서비스에 매퍼가 없고 `*Result`를 그대로 반환한다.
  - 명령/조회 판별은 구현한 포트 이름이 `QueryUseCase`로 끝나는가로 한다. 명령 서비스가 읽기 포트를, 조회 서비스가 write 포트를 주입하지 않는다. 컨트롤러는 `com.tastyhouse.domain.*`를 import하지 않고, command 결과 응답은 커밋 이후 조회 서비스로 재조회해 조립한다.
  - reference: `admin-application`의 `notice/service/NoticeUpdateService`·`NoticeManagementListQueryService`, `web-application`의 `notice/service/NoticeListQueryService`(조회 전용).
- **등록(POST) API는 생성된 `Long` id만 반환**한다: 리소스를 등록하는 POST는 `ResponseEntity<ApiResponse<Long>>`로 PK 하나만 반환하고, 생성 응답 전용 래퍼 record(`XxxCreateResponse`)를 만들거나 생성 직후 조회 서비스로 재조회해 상세 DTO를 반환하지 않는다(상세가 필요하면 클라이언트가 그 id로 GET 상세를 호출). 행을 생성하는 API는 `ApiResponse<Void>`가 아니라 id를 반환하며, 벌크 등록은 `ApiResponse<List<Long>>`이다. 파일 업로드·인증/토큰 발급·검증 전용·토글/상태전이·POST-as-query·배치집계는 리소스 등록이 아니므로 적용 제외. 상세·적용 제외 목록·reference 구현은 [CLAUDE.md](CLAUDE.md#등록post-api-응답-본문-규칙-생성된-long-id만-반환) 참고.
- **api 모듈은 QueryDSL도 `com.tastyhouse.infrastructure..`도 모른다**: web/admin/ceo/batch의 `src/main`에 `com.querydsl.*` import·`@QueryProjection` 선언·`com.tastyhouse.infrastructure..` import가 **0건**이며, 각 모듈 `architecture/LayerRulesTest`(ArchUnit)가 이를 차단한다. `..adapter.in.web..`·`..application.port.in..`을 대상으로 하는 패키지 기준 규칙이 함께 지킨다. 조회는 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스만 주입한다.
- **컨트롤러 `@PathVariable`은 주 리소스를 `id`로 통일**한다: 컨트롤러가 이미 `@RequestMapping`으로 그 도메인에 스코프되므로, 주 리소스를 가리키는 경로 변수는 단건·중첩 경로 모두 bare `id`로 쓰고(예: `/coupons/v1/{id}`, `/coupons/v1/{id}/issues`) 한 컨트롤러 안에서 `id`/`{도메인}Id` 혼재를 금지한다. 단, 다른 애그리거트 식별자를 함께 받는 경우만 `{도메인}Id`로 구분한다. 타입은 `Long` 유지(`@PathVariable Long id`). 상세는 [CLAUDE.md](CLAUDE.md#컨트롤러-pathvariable-식별자-명명-규칙-id로-통일) 참고.
- **import 순서** (그룹 4개는 Spring Framework 공식 컨벤션 `SpringImportOrderCheck`와 동일): `java.*` → `javax.*` → 그 외 서드파티 전부(`jakarta.*` 포함, 알파벳 혼합) → 자사(`com.tastyhouse.*`) → static import(맨 아래) 5개 그룹으로 나누고, 그룹 사이 빈 줄 1개·그룹 안 빈 줄 0개, 그룹 내부는 알파벳순이다. 자사 그룹 내부는 클린 아키텍처 원의 안→밖 순서이며 **`com.tastyhouse.` 다음 최상위 세그먼트 하나로만** 판별한다(프로젝트 커스텀 규칙) — ① `domain`(shared·exception 포함) → ② `application` → ③ driven 어댑터 `infrastructure` → ④ 공유 횡단 `apicommon`·`architecture`·`logging`·`security`·`testsupport` → ⑤ driving 어댑터 `adminapi`·`batch`·`ceoapi`·`webapi`(내부는 공용 `common`·`config`·`exception`·`ratelimit`·`security` 먼저, 도메인 전용 나중). 같은 순위 안만 알파벳순이다. **`ImportOrderConventionTest`(`domain` 테스트)가 backend 전체 소스를 검사해 `./gradlew build`에서 강제한다** — 고칠 때는 실패 메시지의 기대·실제 줄대로 고친다. 상세·예시는 [CLAUDE.md](CLAUDE.md#코딩-스타일-import-순서) 참고.
- **타입 본문 첫 줄 빈 줄**: `class`·`interface`·`enum`·`record`·`@interface` 본문 여는 중괄호 다음 줄은 빈 줄 정확히 1개다(빈 본문·한 줄 본문·익명 클래스·enum 상수 본문 제외). **`TypeBodyBlankLineConventionTest`(`domain` 테스트)가 `./gradlew build`에서 강제한다.** 상세는 [CLAUDE.md](CLAUDE.md#타입-본문-첫-줄-빈-줄-규칙-여는-중괄호-다음-빈-줄-정확히-1개) 참고.

### Module Dependency Graph
```
── 실행 앱 4개 (thin adapter — 컨트롤러/트리거 + request/·response/ + config + 부트스트랩) ──
web-api ──┬─→ application-core (implementation)        ← 코어(공유 서비스·port.out 계약·리스너)
          ├─→ web-application (implementation)         ← 컨트롤러가 자기 앱의 UseCase 포트를 주입. api project(':application-core')
          ├─→ infrastructure:persistence (runtimeOnly) ← 코드 없는 조립 모듈 — infrastructure:jpa와 MySQL 드라이버를 전이로 끌어온다
          ├─→ infrastructure:mybatis (runtimeOnly)     ← notice 레거시 구현(web·admin만)
          ├─→ infrastructure:redis (runtimeOnly)
          ├─→ infrastructure:{file-storage,oauth,pg,mail,sms} (runtimeOnly) ← 조립 스타터만
          │     ※ file-storage가 firebase를, oauth가 kakao/naver/apple/facebook-oauth를, pg·mail·sms가 tosspayments·javamail·solapi를
          │       전이로 끌어온다 — 앱은 벤더를 모른다
          ├─→ security-module(→security-core 전이) / api-common-module (implementation) / logging-module (runtimeOnly)
admin-api  ─(동일 패턴) ─→ application-core + admin-application, runtimeOnly persistence·mybatis·redis·file-storage
ceo-api    ─(동일 패턴) ─→ application-core + ceo-application, runtimeOnly persistence·redis·file-storage
batch-module ─(동일 패턴 — security-module·api-common-module·redis 없음, logging-module은 p6spy exclude)
             ├→ application-core + batch-application   ← 스케줄러가 잡 UseCase 포트를 주입
             └→ infrastructure:{persistence,file-storage,bbq,admdongkor} (runtimeOnly)
   ※ admin-api·ceo-api가 받는 외부 연동은 infrastructure:file-storage 하나뿐이다 — 실사용이 파일 저장 하나여서
     OAuth·결제·메일·SMS·크롤링과 그 SDK를 받지 않는다
   ※ 각 앱은 자기 {앱}-application만 의존하므로 "자기 앱의 UseCase만 주입"을 빌드가 강제한다.
     다른 앱 모듈이 섞이는 것은 각 앱의 ApplicationModuleClasspathTest가 막는다
   ※ 라이브러리 모듈은 runtimeOnly다. 앱은 모듈 설정 클래스를 `@Import`하지 않는다 — 각 앱 부트스트랩의 중첩
     `ModuleScanConfig`가 라이브러리 모듈 패키지를 문자열로 스캔한다(클래스 참조가 아니라 `runtimeOnly`가 유지된다).
     `AutoConfiguration.imports` 자기 등록은 없다
     `security-module`·`api-common-module`만 `implementation`으로 남는다 — 소비 측이 그 모듈의 구체 타입
     (`TokenService`가 쓰는 `JwtTokenProvider`, api-common의 공용 record 등)을 컴파일 타임에 직접 참조하기 때문이다

── application 계층 5개 (infra 의존 없음이 핵심) ──
{web,admin,ceo,batch}-application ─→ application-core (api) + domain·security-core·spring-security-core·spring-web·jackson-databind·spring-tx (implementation)
   ※ 앱 모듈끼리는 서로를 의존하지 않고, 코어는 앱 모듈을 모른다 — 앱 간 수평 의존은 컴파일 에러다
   ※ 아래 application-core 블록이 코어다. 앱 전용 SPI 포트(web: Mail/Sms/PG/Social, batch: BBQ/행정동 경계, ceo: ReplyPhraseTextValidatorPort)는 앱 모듈이 소유한다
application-core ─┬→ domain (implementation)  ← 읽기 계약은 전부 이 모듈의 com.tastyhouse.application..port.out
             ├→ security-core (implementation)        ← web·admin·ceo auth/token의 JwtTokenProvider·토큰 저장소 포트.
             │                                          security-module 대신 이 모듈만 의존해 서블릿 스택을 배제
             ├→ spring-security-core (implementation) ← admin·ceo AuthenticationManager·PasswordEncoder·UserDetails
             ├→ spring-web (implementation)           ← web·admin·ceo MultipartFile 업로드 경계 타입 전용(starter-web 아님)
             ├→ jackson-databind (implementation)     ← ceo ShopStorePriceVerificationRequestService의 ObjectMapper
             └→ spring-tx (implementation)            ← @Transactional 전용
   ※ infrastructure:* 의존 없음 — 소셜 로그인 SPI(web-application)·크롤링 클라이언트(batch-application) 계약을 application 계층이 소유하고
     infrastructure:{kakao,naver,apple,facebook}-oauth·infrastructure:bbq·infrastructure:admdongkor가 그것을 구현한다(의존 역전). 되살리면 순환이 되어 빌드가 깨진다
   ※ api-common-module 의존 없음 — 표현 계약 조립은 api 모듈이 한다.
     빌드 그래프가 1차 방어선이고 applicationShouldNotDependOnApiCommon이 2차 방어선으로 휴면 상태로 남는다
   ※ infrastructure 의존 없음 — 계층 분리를 빌드 그래프가 강제한다
     (`import com.tastyhouse.infrastructure...` 한 줄이 컴파일 에러)
   ※ batch 유스케이스가 spring-web·spring-security-core를 컴파일 클래스패스에서 보게 되지만
     서블릿 스택(security-module·starter-web)은 여전히 없다

── 공유 모듈 ──
infrastructure:persistence ─→ infrastructure:jpa (runtimeOnly) + infrastructure:mysql (runtimeOnly)   ← [조립·스타터] 코드 없음
infrastructure:mysql ─→ mysql-connector-j·HikariCP (datasource 설정 — 자바 코드 없음)   ← DB 연결 코어
infrastructure:mybatis ─┬→ domain (implementation)
                        └→ application-core (implementation) ← notice 레거시 구현이 NoticeLoadPort·NoticeSavePort·NoticeQueryPort·NoticeManagementQueryPort를 구현(web·admin만 의존)
infrastructure:jpa ─┬→ domain (implementation)      ← XxxPersistenceAdapter·XxxMapper가 도메인 모델을 직접 다룬다
                    └→ application-core (implementation) ← QueryAdapter가 {Ctx}QueryPort를, XxxPersistenceAdapter가 port/out/write의 XxxLoadPort·XxxSavePort를 구현
   ※ domain 간선은 api가 아니라 implementation이다 — 앱 컴파일 클래스패스로 domain이 새지 않는다
   ※ 조회 DAO(..query..)와 봉인 조회 어댑터 3개는 domain을 모른다 — LayerRulesTest#queryShouldNotDependOnDomain
     ([ADR-0031](../docs/adr/0031-persistence-domain-reallowed.md))
infrastructure:redis ──→ security-core (implementation)     ← 토큰 저장소 포트 + RateLimitCounterPort를 구현하는 어댑터
   ← 연결·템플릿 자체는 domain에 포트가 없는 순수 기술이라 domain을 모른다. 어댑터가 구현하는 계약의
     소유 모듈만 의존한다(adapter → port 방향). 내부 의존은 security-core 하나
infrastructure:restclient ─→ (내부 의존 없음) + spring-web·starter-json (api)   ← HTTP 코어: RestClient.Builder customizer만(예외·에러코드 없음)
   ↑ HTTP를 쓰는 벤더(oauth 4종·tosspayments·solapi·bbq·admdongkor)가 implementation으로 의존한다(RestClient 재사용)
   ※ 이 모듈은 domain조차 의존하지 않는다 — 벤더는 실패를 결과 record로 돌려주고, 에러코드 번역은 앱의 application 모듈이 한다
   ※ 아래에서 [조립] 표시가 붙은 모듈(file-storage·oauth·pg·mail·sms, 위 persistence)은 도메인 포트를 구현하지 않는
     자바 코드 없는 컴포지션 루트 조각이다. 나머지 18모듈은 driven 어댑터다(restclient·mysql 포함)
infrastructure:file-storage ─→ infrastructure:firebase (runtimeOnly)   [조립·스타터]
   ← 자바 코드 없는 조립 전용 스타터. 앱 4개가 의존하는 유일한 파일 저장 좌표이며,
     firebase는 여기를 통해 앱 runtimeClasspath에 전이로 실린다(compileClasspath에는 없다)
infrastructure:firebase  ─→ application-core                       + firebase-admin   ← FileStoragePort(application.file.port.out) 직접 구현
infrastructure:aws-s3    ─→ application-core                       + spring-cloud-aws-starter-s3(+BOM) ← FileStoragePort 직접 구현
infrastructure:aws-ses   ─→ web-application  + awssdk:ses(+BOM)
   ※ infrastructure:restclient·채널 모듈 의존 없음(직접·전이 모두) — mail.sender-address는 @Value 키로 읽는다
infrastructure:aws-sns   ─→ web-application  + awssdk:sns(+BOM)
   ※ infrastructure:restclient·채널 모듈 의존 없음(직접·전이 모두)
infrastructure:oauth     ─→ runtimeOnly infrastructure:{kakao,naver,apple,facebook}-oauth ← [조립·스타터] 자바 코드 없음(web 전용)
   ※ web 전용 SPI를 구현하는 벤더(oauth 4종·tosspayments·javamail·solapi·aws-ses·aws-sns)는 web-application을,
     bbq·admdongkor는 batch-application을 의존한다. jpa·mybatis·firebase·aws-s3는 application-core를 의존한다
infrastructure:kakao-oauth    ─→ infrastructure:restclient, web-application(auth SPI)          ← SocialOAuthClientPort 구현(KAKAO)
infrastructure:naver-oauth    ─→ infrastructure:restclient, web-application(auth SPI)          ← SocialOAuthClientPort 구현(NAVER)
infrastructure:apple-oauth    ─→ infrastructure:restclient, web-application(auth SPI) + jjwt ← SocialOAuthClientPort 구현(APPLE)
infrastructure:facebook-oauth ─→ infrastructure:restclient, web-application(auth SPI)  ← SocialOAuthClientPort 구현(FACEBOOK)
   ※ 4벤더 전부 domain 의존이 없다 — 실패 표현은 web-application의 SocialOAuthResult/SocialOAuthFailure다.
     jjwt 선언은 apple만 — 컴파일 격리일 뿐이며 런타임에는 application → security-core 경로로 jjwt가 이미 web 전체에 실린다
infrastructure:pg        ─→ runtimeOnly infrastructure:tosspayments ← [조립·스타터] 코드 없음. 라우터는 web-application PgPaymentGatewayRouter(@Service)
infrastructure:tosspayments ─→ infrastructure:restclient, web-application ← PgProviderGatewayPort 구현(provider()는 web-application의 PgProviderCode.TOSS)
infrastructure:mail      ─→ runtimeOnly javamail                  ← [조립·스타터] 코드 없음. MailVerificationService는 web-application의 @Service
infrastructure:javamail  ─→ web-application  + starter-mail        ← MailSenderPort 구현
infrastructure:sms       ─→ runtimeOnly solapi                     ← [조립·스타터] 코드 없음. SmsVerificationService는 web-application의 @Service
infrastructure:solapi    ─→ infrastructure:restclient, web-application  ← SmsSenderPort 구현
   ※ 벤더(javamail·solapi·aws-ses·aws-sns·tosspayments·{kakao,naver,apple,facebook}-oauth)는 채널 모듈을 의존하지 않는다 — 채널이 벤더를 runtimeOnly로 조립하므로 역방향은 순환
infrastructure:bbq       ─→ infrastructure:restclient, batch-application(BBQ 포트) (동기 RestClient)
infrastructure:admdongkor ─→ infrastructure:restclient, batch-application(행정동 경계 포트) + starter-json
   ※ bbq·admdongkor도 domain 의존이 없다 — RemoteImagePort/AdminDongBoundaryPort가 각각 ImageDownloadResult·
     AdminDongBoundaryFetchResult(+BoundaryRing/BoundaryCoordinate, application 소유 좌표 타입)를 반환하므로
     벤더가 domain의 GeoRing/GeoPoint를 모른다
security-core ─┬→ (프로젝트 의존 없음)
               └→ spring-security-core (api) + jjwt-api (api)/jjwt-impl·jjwt-jackson (runtimeOnly)
   ← 서블릿-프리 타입(JwtTokenProvider·토큰 저장소 포트·RateLimitCounterPort)만 갖는다. 서블릿 스택(starter-web·jakarta.servlet) 의존 없음
   ← 토큰 저장소는 포트만 있고, StringRedisTemplate으로 키를 조립하는 구현은 infrastructure:redis의 token 패키지가 갖는다
     — 그래서 security-core → infrastructure:redis 간선이 없고, api-common-module이 batch로 전이되지 않는다(CLAUDE.md 감사표 참고)
security-module ─┬→ application-core (implementation)
                 ├→ api-common-module (implementation) ← ApiErrorCode(JwtAuthenticationEntryPoint·JwtAccessDeniedHandler). domain은 보지 않는다
                 └→ security-core (api)             ← 서블릿 결합 타입(필터·EntryPoint·AccessDeniedHandler)이 JwtTokenProvider·토큰 저장소 포트를 쓰고, api 3모듈에도 전이로 노출
api-common-module ─┬→ application-core (api)        ← PageResult가 PaginationResponse.from의 공개 시그니처에 노출
                   │                                       (에러 계약은 자기 모듈의 ApiErrorCode, 예외 판정은 application의 ErrorResponses — domain은 보지 않는다)
                   ├→ security-core (implementation)       ← RateLimitAspect가 쓰는 RateLimitCounterPort. api 노출 안 함 —
                   │                                          web·admin·ceo는 security-module의 api로 이미 받는다
                   ├→ starter-web·starter-validation (api) ← GlobalExceptionHandler·ApiResponse
                   ├→ spring-security-core (implementation)
                   ├→ starter-aop (implementation)         ← RateLimitAspect
                   └→ springdoc-openapi-starter-webmvc-ui (api)
   ← security-module 의존은 없다. rate limit 계약은 security-core 소유라
     infrastructure:redis → security-core ← api-common-module이다(구현과 표현이 서로를 모른다)
domain → 의존 없음 (production 의존 0개)
```
- **`domain`은 프레임워크를 모른다**: 다른 모듈에 의존하지 않으며, Spring(Web/tx/orm)·JPA·QueryDSL 전부 의존이 없다. HTTP 상태는 에러코드 enum의 `httpStatusCode`(int — `DomainErrorCode` 등)로, 낙관적 락 충돌은 프레임워크-프리 `OptimisticLockConflictException`(`application/shared/port/out/`)으로 표현한다(스프링 예외 번역은 `infrastructure:jpa`의 `PersistenceAdapter` 담당). 이벤트 발행·도메인 서비스 빈 등록은 `application-core`가 한다. 저장은 `application/<ctx>/port/out/write/XxxLoadPort`·`XxxSavePort` ← persistence `XxxPersistenceAdapter`(낙관적 락 번역은 `ReservationSlotPersistenceAdapter`)로 흐른다.
- **읽기 계약은 전부 `application-core`가 소유한다**: 패키지 `com.tastyhouse.application.<ctx>.port.out`의 읽기 계약을 코어 한 모듈이 갖는다. 소비 앱 수로 소유 모듈을 가르지 않는다([ADR-0015](../docs/adr/0015-shared-read-contracts-in-application.md)).
  - **프레임워크-프리는 ArchUnit이 강제한다**: `application-core`는 spring 의존을 받아 컴파일 게이트가 없으므로, `LayerRulesTest#readContractsShouldBeFrameworkFree`가 계약 전체를 검사한다. 단 write 포트 `port.out.write`(도메인 타입 `XxxLoadPort`·`XxxSavePort`)는 이 대상에서 빠지고 `#writePortsShouldOnlyDependOnDomainAndPortOut`(`java..`·`com.tastyhouse.domain..`·`application..port.out..`만 허용)이 따로 검사한다. 같은 모듈 안의 FQCN 중복은 컴파일 에러라 별도 가드가 필요 없다.
- **application 계층이 읽기 계약을 보는 경로**: 조회 유스케이스 서비스는 infra DAO 구현체가 아니라 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스를 주입한다. 계약이 전부 코어에 있으므로 이를 위한 추가 의존 선언은 없다. api 모듈은 `com.tastyhouse.infrastructure..`를 **전혀 import하지 않는다** — 각 모듈 `LayerRulesTest`가 이를 강제한다. `infrastructure:jpa`(앱은 조립 모듈 `infrastructure:persistence`를 거쳐 전이로 받는다)는 각 앱 부트스트랩의 중첩 `ModuleScanConfig`가 `com.tastyhouse.infrastructure`를 문자열로 스캔하는 대상이라 실행 모듈의 **런타임** 의존 그래프에는 있지만, `runtimeOnly`라 **컴파일 클래스패스에도, 소스 코드 레벨의 import 대상에도 없다.**
- **`Projections.constructor` 투영**: Result record가 QueryDSL을 모르는 `application-core`에 있어 `@QueryProjection`을 달 수 없다. `infrastructure:jpa`의 QueryAdapter는 `Projections.constructor(XxxResult.class, ...)`로 리플렉션 기반 조립을 한다 — Result record가 `public`이 아니거나 생성자 시그니처가 select 절과 불일치하면 컴파일은 통과하고 **호출 시점에 500**이 나므로, 새 쿼리는 반드시 한 번 호출해 확인한다. 이 리플렉션 대상 일치는 `infrastructure:jpa`의 `ProjectionConstructorMatchingTest`가 소스 스캔으로 검증한다.
- `querydsl-jpa`는 `infrastructure:jpa`에서 `implementation`으로 강등되어 소비 모듈 클래스패스로 전이되지 않는다. 전 프로젝트에서 QueryDSL을 컴파일하는 모듈은 `infrastructure:jpa` 하나뿐이다.
- 실행 가능한(bootJar) 모듈은 `web-api`/`admin-api`/`ceo-api`/`batch-module` 넷뿐이다. 나머지(`domain`/`application-core`/`{web,admin,ceo,batch}-application`/`infrastructure:*` 24개/`security-core`/`security-module`/`api-common-module`/`logging-module`)는 `bootJar` 비활성 + plain jar.
  - **중첩 프로젝트 컨테이너 주의**: `include 'infrastructure:persistence'`는 소스가 없는 빈 프로젝트 `:infrastructure`를 함께 만든다. 루트 `build.gradle`의 `subprojects` 일괄 설정이 이 컨테이너에까지 `bootJar`를 걸면 빌드가 깨지므로, 일괄 설정 대상에서 제외되는지 확인한다.
- **application 계층은 infrastructure를 컴파일 클래스패스에 두지 않는다**: application 계층이 infra를 모른다는 규칙을 ArchUnit이 아니라 **빌드 그래프가 1차로 강제**한다 — `import com.tastyhouse.infrastructure...` 한 줄이 실제 컴파일 에러가 된다(`domain`의 프레임워크-프리 게이트와 같은 방식). 그래서 `@Transactional`을 위한 `spring-tx`를 명시 선언한다. ArchUnit 규칙(`shouldNotDependOnInfrastructure`)은 누군가 build.gradle에 의존을 되돌리는 회귀를 막는 2차 방어선이다.
- **모듈 등록은 앱 부트스트랩의 문자열 스캔이다**: 라이브러리 모듈은 `AutoConfiguration.imports`로 자기 등록하지 않는다. 각 앱 부트스트랩이 static 중첩 클래스 `ApplicationLayerScanConfig`(`com.tastyhouse.application` 스캔, 필터 없음)와 `ModuleScanConfig`(`@Configuration(proxyBeanMethods = false)` + `@ComponentScan(basePackages = {...})` 문자열 패키지)를 갖는다 — `ModuleScanConfig`는 web `infrastructure`·`security`·`logging`·`apicommon.ratelimit`, admin·ceo는 여기에 `apicommon.exception` 추가, batch `infrastructure`·`logging`. 문자열 스캔이라 컴파일 클래스패스가 필요 없어 `runtimeOnly` 헥사고날 게이트가 유지된다(`@Import(Class)`는 그 이유로 불가). 앱의 `@Import`와 `scanBasePackages`는 없다. 모듈 설정 클래스는 `{Xxx}ModuleConfig`(`@Configuration`, 스캔 없음)이고, 이 설정들에 `@ConditionalOnBean`/`@ConditionalOnMissingBean`을 쓰지 않는다(일반 설정에서는 순서 의존이라 조용히 틀린다 — 환경 조건 `@ConditionalOnWebApplication`/`@ConditionalOnProperty`만 허용). imports 재유입은 4앱 `ApplicationLayerScanConfigTest`의 `assertNoTastyhouseAutoConfiguration`이 막는다. "클래스패스 존재 = 활성화" 원칙이 적용되며, 활성화 목록은 앱 스캔 목록이다([ADR-0038](../docs/adr/0038-autoconfiguration-imports-removal.md)). 상세는 `backend/CLAUDE.md`의 모듈 등록 컨벤션 절.
- **새 도메인 서비스는 스캔 엔트리가 아니라 `@Service`로 등록한다**: 쓰는 앱이 하나면 그 앱의 `{앱}-application`, 두 앱 이상이면 `application-core`의 `<ctx>/service/`에 `@Service`로 둔다. domain 모듈 클래스(순수 계산기·정책)처럼 애노테이션을 달 수 없는 빈만 `application-core`의 `shared/config/SharedBeanConfig`에 `@Bean`으로 둔다.
- **모듈 경계 원칙 — 2차원 경계**: 모듈 경계는 **계층 × 앱** 두 축이다.
  - **계층 축**: `domain`(순수 도메인) → application 계층(`application-core` + `{앱}-application`, 유스케이스) → api 모듈(인바운드 어댑터). `infrastructure/` 아래 24모듈은 **driven 18 + 조립 6**이다. `infrastructure:{jpa,mysql,mybatis,redis,restclient,firebase,aws-s3,aws-ses,aws-sns,kakao-oauth,naver-oauth,apple-oauth,facebook-oauth,tosspayments,javamail,solapi,bbq,admdongkor}` 18모듈이 아웃바운드(driven) 어댑터 쪽이다(`restclient`는 포트를 구현하지 않는 HTTP 코어, `mysql`은 DB 연결 코어지만 기술 코어라 여기에 센다). **조립 6모듈** — `persistence`(DB — jpa + mysql)·`file-storage`·`oauth`·`pg`·`mail`·`sms` — 은 전부 자바 코드가 없고 도메인 포트를 구현하지 않는 컴포지션 루트 조각으로, 앱이 DB·외부 연동을 쓸 때 직접 의존하는 좌표다(`backend/CLAUDE.md`의 "모듈 지도").
  - **앱 축**: 같은 계층이라도 web·admin·ceo·batch는 서로의 모듈을 알지 않는다(앱 모듈끼리 의존하지 않는다).
  - **infrastructure는 기술별로 나눈다**: `infrastructure:jpa`는 application 포트의 **DB 어댑터 전용**(write `persistence` + read `query`), `infrastructure:mysql`은 DB 연결, `infrastructure:mybatis`는 notice 레거시 MyBatis 구현, `infrastructure:redis`는 Redis 연결·rate limit 카운터·토큰 저장소, `infrastructure:restclient`와 벤더 13모듈(`firebase`·`aws-s3`·`aws-ses`·`aws-sns`·`kakao-oauth`·`naver-oauth`·`apple-oauth`·`facebook-oauth`·`tosspayments`·`javamail`·`solapi`·`bbq`·`admdongkor`)이 외부 시스템 연동 어댑터이고, 조립 6모듈이 그 구현을 앱에 묶어 준다 — **driven adapter는 DB·Redis뿐 아니라 외부 연동까지 전부 `infrastructure:{기술}` 아래에 둔다**. 자바 패키지 루트는 모듈명을 따른다 — 코드가 있는 17모듈 전부 `com.tastyhouse.infrastructure.{모듈명의 하이픈을 점으로}`를 루트로 쓰고, 각 모듈의 아키텍처 테스트 `shouldResideInModuleRootPackage`가 강제한다(조립 6모듈과 `mysql`은 자바 코드가 없어 패키지가 없다). **외부 연동을 벤더·채널 단위까지 쪼개는 기준은 "앱별 실사용 차이"다** — admin·ceo는 파일 저장 하나만 쓰므로 OAuth·결제·메일·SMS와 AWS SDK를 받지 않는다. domain에 포트가 없는 기술이라도 **순수 인프라 기술이면 `infrastructure:{기술}`**에 두고, **여러 presentation이 공유하는 보안 관심사**일 때만 `security-module`, **HTTP 플럼빙**이면 `api-common-module`에 둔다. 결제(`infrastructure:pg`)는 벤더 배타 선택이 아니라 **여러 PG 벤더가 공존하며 `PgProvider`로 라우팅**하고, 그 라우터(`PgPaymentGatewayRouter`)는 `web-application`의 `@Service`다 — 상세는 `backend/CLAUDE.md`의 "공존형 채널 — PG 라우터" 절. 소셜 로그인(`infrastructure:oauth`)도 제공자 4종이 공존하며, `web-application`의 `SocialOAuthClientRouter`가 `SocialProvider`로 벤더를 고른다(같은 파일의 "공존형 채널 — 소셜 로그인 스타터" 절).
  - **컨텍스트별 모듈 분할은 하지 않는다**: 컨텍스트 경계(25종)는 모듈이 아니라 `domain`의 ArchUnit `ContextBoundaryTest`(봉인 목록)가 담당한다.
- **api 모듈 공용 플럼빙은 `api-common-module`이 단독 소유**한다: `ApiResponse`/`PaginationResponse`/`PageRequest`/`ProblemDetails`와 admin·ceo 공용 `GlobalExceptionHandler`가 여기 있다. **완전 동일한 것만** 공유하며, 내용이 다른 정책 파일(`SecurityConfig`·`PublicPaths`·`TokenService`·`AuthService`)과 계약이 다른 응답 record(`ShopDetailResponse` 등)는 앱마다 둔다 — 허용 목록은 [CLAUDE.md](CLAUDE.md#api-모듈-공용-플럼빙-소유-규칙-api-common-module) 표 참고. `GlobalExceptionHandler`가 web-api의 자체 핸들러와 함께 뜨지 않는 것은 스캔 범위가 보장한다 — admin·ceo의 `ModuleScanConfig`만 `com.tastyhouse.apicommon.exception`을 스캔하고 web은 스캔하지 않는다(빈 이름 `globalExceptionHandler`).
- **소셜 로그인은 `com.tastyhouse.application.auth.port.out` SPI로만 사용**한다: web-api는 제공자별 패키지(`com.tastyhouse.infrastructure.kakao.oauth..` 등)의 wire DTO·클라이언트를 직접 import하지 않고 `SocialOAuthClientPort`/`SocialProfile`만 안다(ArchUnit `shouldDependOnOauthSpiOnlyNotProviderPackages`가 강제). 이 SPI는 domain이 아니라 `web-application`이 소유한다 — 구현은 벤더 4모듈 `infrastructure:{kakao,naver,apple,facebook}-oauth`이고 `infrastructure:oauth`는 그것을 조립하는 코드 없는 스타터다. 소셜 OAuth의 호출부가 전부 표현 계층이라 도메인 서비스가 쓰는 포트가 아니기 때문이다. 상세는 [CLAUDE.md](CLAUDE.md#소셜-로그인-spi-규칙-application의-authportout) 참고.

### Testing Requirements
- 스키마 무변경 보장: `hibernate.ddl-auto=validate` 기준. JPA 엔티티(`infrastructure:jpa`) 변경 시 `schema.sql`과 정합성 확인.
- QueryDSL Q클래스는 `infrastructure:jpa`에서만 생성된다(`infrastructure/jpa/build/generated/...`) — 경로 변경 시 `./gradlew clean compileJava` 필요. `domain`에는 apt가 없어 Q타입이 생성되지 않는다.
- 도메인 불변식은 `domain/src/test`의 **순수 단위 테스트**로 검증한다(스프링 컨텍스트·DB 불필요).
- 레이어 경계는 각 모듈의 `architecture/LayerRulesTest`(ArchUnit)로 검증한다. 이 규칙들은 `allowEmptyShould(true)`를 쓰지 않으므로, 대상 클래스가 0건이면 **공허 통과가 아니라 실패**로 드러난다.
- **`allowEmptyShould(true)` 금지는 자동 검증된다**: `noClasses().that()...` 형태는 대상이 0건이어도 조용히 통과하므로, `application-core/src/test/.../RuleAnchorTest`(앱별 하한)와 `BatchSchedulerRulesTest`(batch exact — `*SchedulerService` 7 · `..port.in..` 7)가 각 규칙의 anchor 개수를 직접 세어, 클래스가 모듈 사이를 옮겨 다니다 대상이 통째로 사라지면 **빌드가 실패**하게 한다. 모듈을 분리·이동할 때 같은 패턴을 따른다.

### Common Patterns
- 계층 배치: `domain`의 `<ctx>/{model,vo,event}` / `infrastructure:jpa`의 `<ctx>/{persistence,query}` / api 모듈의 `<ctx>/adapter/in/web/`(컨트롤러 + `request/` + `response/`) / `{앱}-application`의 `<ctx>/{port/in,service}` / `application-core`의 `<ctx>/{port/out,port/out/write,service,listener}`·`shared/**`.
- 식별자 강타입화: `record MemberId(Long value)`(domain). 엔티티 FK는 raw `Long`이고(`AttributeConverter`·`@Convert` 없음), `XxxId` 승격·언패킹은 persistence `<ctx>/persistence/XxxMapper`가 null 가드와 함께 한다(`backend/CLAUDE.md` "ID VO 경계 규칙").
- BC 간 통신은 도메인 서비스 호출 또는 `DomainEvent`로만. 이벤트 발행은 `DomainEventPublisher` 포트를 통하고, 스프링 구현(`SpringDomainEventPublisher`)과 리스너(`<ctx>/listener/`, `@Component` + `@TransactionalEventListener(AFTER_COMMIT)`)는 코어 `application-core`에만 있다(앱 모듈 금지 — `listenersAndConfigsShouldResideInCore`).
- CQS: 명령 서비스(`@Transactional`) / 조회 서비스(`@Transactional(readOnly = true)`) — 트랜잭션 경계는 application 계층의 서비스가 소유한다(domain은 POJO라 `@Transactional`을 갖지 않는다).

## Dependencies

### 루트 `build.gradle`이 소유하는 것
- **버전 단일 관리** — `ext.springBootVersion`(BOM을 직접 import 하는 `domain` 블록용. 위 `plugins` 블록의 플러그인 버전과 **일치시킬 것**)과 `ext.springdocVersion`. springdoc 좌표는 Spring Boot BOM이 관리하지 않으므로 여기서 단일 관리하며, `web/admin/ceo-api`·`api-common-module` 4곳이 이 값을 참조한다.
- **`ext['commons-lang3.version'] = '3.18.0'`** — Spring Boot 3.2.4 BOM이 고정하는 3.13.0이 **CVE-2025-48924**에 해당해 패치 버전으로 오버라이드한 것이다(BOM 관리 프로퍼티 재정의). 보안 목적이므로 BOM 버전과 맞추려고 되돌리지 않는다.
- **`ext['netty.version'] = '4.1.137.Final'` · `ext['jackson-bom.version'] = '2.21.7'`** — 같은 기법의 보안 오버라이드다. 같은 블록의 `ext['httpclient5.version'] = '5.6.3'` · `ext['httpcore5.version'] = '5.4.3'` · `ext['opentelemetry.version'] = '1.62.0'` 세 줄도 같은 이유(BOM이 `firebase-admin`이 요구하는 httpclient5 5.3.1 → 5.2.3, opentelemetry-api 1.57.0 → 1.31.0으로 끌어내림)로 두며, 아래 "jackson 핀" 절의 되돌리지 말 것 원칙이 그대로 적용된다. **Spring Boot 3.2.4 BOM이 netty 4.1.107·jackson 2.15.4를 고정하는 바람에, `firebase-admin`이 요구하는 더 새 버전을 오히려 취약 버전으로 끌어내린다**(CVE-2025-58057/58056, CVE-2025-24970 등). 즉 이 프로젝트가 낡은 라이브러리를 쓰는 것이 아니라 **BOM이 downgrade한 결과**이므로, 해소는 라이브러리 좌표가 아니라 BOM 프로퍼티 재정의로 한다(`commons-lang3`와 동일한 형태).
  - **되돌리지 않는다.** "BOM이 관리하는데 왜 버전을 박아뒀나"로 보여 정리 대상처럼 읽히지만, 지우는 순간 세 CVE가 조용히 되살아난다. Boot 버전을 올릴 때는 새 BOM이 고정하는 값이 위 버전 이상인지 확인한 뒤에만 이 두 줄을 걷어낸다.
  - 확인 방법: `./gradlew :web-api:dependencies --configuration runtimeClasspath | grep -E 'netty|jackson-core'`로 해석된 실제 버전을 본다.
- **`subprojects` 일괄 설정의 제외 대상 2개** — `domain`(프레임워크-프리 컴파일 게이트)과 `:infrastructure`(소스 없는 중첩 프로젝트 컨테이너). 각각의 근거는 [CLAUDE.md](CLAUDE.md#도메인-모델--jpa-엔티티-분리-규칙-선별-적용-persistence는-infrastructure-module로)와 위 [모듈 의존 그래프](#module-dependency-graph)의 "중첩 프로젝트 컨테이너 주의"에 있다.

### External
- Spring Boot 3.2.4 (web, security, data-jpa, data-redis, aop, mail, validation) — webflux는 리포 전체에서 제거됐다(외부 HTTP 호출은 동기 `RestClient`로 통일)
- Java 21, Gradle (멀티모듈)
- QueryDSL `io.github.openfeign.querydsl:querydsl-jpa:6.11` (OpenFeign 포크) — 동적 쿼리. **`infrastructure:jpa`에만 `implementation`으로 의존**해 소비 모듈로 전이되지 않는다
- MySQL (`mysql-connector-j`), Redis
- JJWT 0.12.3 — JWT 발급/검증
- AWS SDK (SES, SNS, S3), Firebase Admin 9.10.0
- springdoc-openapi 2.3.0 — Swagger UI
- p6spy (SQL 로깅 — logging-module이 api로 노출하고 SQL 로그 포맷을 `application-logging.yml`에서 소유)

<!-- MANUAL: 수동 메모는 이 라인 아래에 추가하면 재생성 시 보존됩니다 -->

## 계층 규칙 봉인 — api 앱 3종 공통

<!-- 분류 A. web-api·admin-api·ceo-api의 architecture/LayerRulesTest.java 공통분 -->

`web-api`·`admin-api`·`ceo-api`의 `src/test/java/com/tastyhouse/{web,admin,ceo}api/architecture/LayerRulesTest.java` **세 파일은 내용이 거의 동일하다.** 공통분을 여기에 한 번만 쓰고, 앱별 차이는 각 앱 `AGENTS.md`의 "봉인·가드 목록"에 적는다. 이 절이 그 금지 지시의 유일한 소재지다.

### `apiModuleShouldBeDomainModelFree` — 모듈 전역 domain 금지

**대상**: 각 앱의 `LayerRulesTest.java` → `apiModuleShouldBeDomainModelFree()`

모듈 전역 ✗ `com.tastyhouse.domain..`을 **예외 없이** 금지한다. `controllersShouldBeDomainFree`는 `*ApiController` 접미어로, `requestRecordsShouldBeDomainAndInfraFree`는 `..request..` 패키지로 대상을 좁히므로 `config..`·`security..`·`exception..`이 무검사 사각지대가 된다 — 이 규칙이 그 지점을 모듈 전역으로 봉인한다.

- 에러 처리는 `application`의 `ErrorResponses.resolve`와 api-common의 `ApiErrorCode`를 쓰고(`backend/CLAUDE.md` "예외·에러코드 소유 규칙"), 페이징 타입 `PageQuery`/`PageResult`는 `com.tastyhouse.application.shared.port.out.page` 소유이며, `*Result`는 도메인 enum 대신 `String`(+ `{field}Description`/`{field}DisplayName`)을 싣고 온다. 그래서 api 모듈이 domain 타입을 쓸 정상 경로가 없다([ADR-0025](../docs/adr/0025-presentation-domain-cut.md)).
- `api-common-module`·`security-module`이 `domain`이 아니라 `application-core`에 의존하므로 **presentation 컴파일 클래스패스에 `domain`이 아예 없다.** 이 규칙은 누군가 의존 한 줄을 되돌리는 회귀를 잡는 2차 방어선이다(`api-common-module`·`security-module`에도 같은 취지의 `LayerRulesTest#shouldNotDependOnDomain`이 있다).
- **⚠️ 위반은 `import`로 보이지 않는다**: ArchUnit은 import 문이 아니라 바이트코드 상수 풀을 읽으므로, 이 모듈에 `import com.tastyhouse.domain..`이 0건이어도 `*Result` 컴포넌트를 통한 **전이 의존**으로 잡힌다. **grep으로 검증하면 "위반 0건"으로 오판할 수 있다 — 검증은 반드시 이 테스트로 한다.**

### 공허 통과 금지 — `allowEmptyShould(true)`를 쓰지 않는다

**대상**: 각 앱의 `LayerRulesTest.java` (파일 전체)

**`allowEmptyShould(true)`는 이 파일 어디에도 쓰지 않는다.** 대상이 0건이 된 규칙은 공허 통과를 여는 대신 **삭제하거나 다른 모듈로 옮긴다.**

application 계층을 대상으로 하는 규칙(`commandServicesShouldNotDependOnQueryPorts` · `queryServicesShouldNotDependOnWritePorts` · 유스케이스 1:1 규칙 · `commandRecords*` · `portIn*` · `commandServicesShouldNotDependOnRequestRecords` · `applicationServicesShouldNotDependOnWebLayer`)은 `application-core` 모듈의 테스트에 있다 — **api 모듈에 두면 대상 0건으로 공허하게 통과하기 때문이다.**

`restControllersShouldResideInWebAdapterPackage`가 `classes()` 형태인 것은 **컨트롤러 실존에 anchor** 하기 위해서다 — 컨트롤러가 0건이 되면 곧바로 실패한다. `apiModuleShouldBeDomainModelFree`는 anchor가 모듈 전체(`noClasses()`)라 클래스가 존재하는 한 **대상 0건이 될 수 없다.**

### 인바운드 어댑터의 앱 격리 — 컴파일 게이트가 지킨다

**대상**: 각 앱의 `build.gradle`과 `ApplicationModuleClasspathTest`

각 앱은 `application-core`와 자기 `{앱}-application` 하나만 의존하므로, 다른 앱의 UseCase·Command는 클래스패스에 없어 **컴파일 에러**다. 다른 앱 모듈이 의존에 섞이는 사고는 각 앱의 `ApplicationModuleClasspathTest`가 막는다([ADR-0036](../docs/adr/0036-app-marker-removal.md)).

### 3층 구조 봉인 — 컨트롤러·Request의 경계 규칙

**대상**: 각 앱 `src/test/java/com/tastyhouse/{web,admin,ceo}api/architecture/LayerRulesTest.java`
→ `controllersShouldBeDomainFree()` · `requestRecordsShouldBeDomainAndInfraFree()` · `webAdaptersShouldNotDependOnApplicationServices()`

**컨트롤러는 domain-free다.** HTTP 경계는 식별자를 `Long`, 도메인 enum을 `String`으로 받고 승격은 Service가 담당하므로, 컨트롤러가 `com.tastyhouse.domain..`을 알 이유가 없다. `controllersShouldBeDomainFree`는 carve-out 없이 `com.tastyhouse.domain..`을 전면 금지한다.

**Request record는 domain-free·infra-free 순수 데이터 홀더다**(검증 + Swagger 스키마). **문자열→enum 승격을 Request에서 하지 않는다.**

이 domain-free 제약의 실무적 귀결이 하나 있다 — **multipart 문자열 파트를 컨트롤러에서 파싱할 수 없다.** 컨트롤러·Request가 도메인 타입을 모르므로, `Command`가 `String`으로 넘기고 **서비스가 파싱한다.**

**컨트롤러·인바운드 어댑터는 연산마다 UseCase 인터페이스만 주입한다.** 구체 유스케이스 서비스(`{도메인}{동작}Service`/`{도메인}{관점}QueryService`) 주입은 금지다. `webAdaptersShouldNotDependOnApplicationServices`가 **위치**(`com.tastyhouse.application..service..`) 기준으로 지킨다 — 접미어가 아니라 위치로 잡으므로 비표준 접미어 파사드까지 걸린다. 구체 서비스를 api 모듈로 되돌리는 시도는 이 규칙과 `apiModuleMustNotContainApplicationLayer`가 잡는다.

### `apiModuleMustNotContainApplicationLayer` — 물리 분리가 되돌려지지 않았음을 고정한다

**대상**: 각 앱의 `LayerRulesTest.java`
→ `apiModuleMustNotContainApplicationLayer()` · `restControllersShouldResideInWebAdapterPackage()`

**api 모듈에 `@Service` 빈을 두지 않는다** — application 계층은 `application-core`와 `{앱}-application`이 소유한다.

위의 "컨트롤러가 무엇을 주입하는가" 규칙들은 **누군가 api 모듈 안에 `@Service` 빈을 새로 만들어 application 로직을 되살리는 것은 잡지 못한다.** 그 구멍을 막는 것이 이 규칙이다.

짝 규칙 `restControllersShouldResideInWebAdapterPackage`(`@RestController`는 `..adapter.in.web..`에만)와 함께 3층 구조를 지킨다. 짝이 `classes()` 형태라 **컨트롤러 실존에 anchor** 하므로 두 규칙이 함께 공허해지지 않는다.

### 기술 스택 격리 — QueryDSL·persistence 어댑터를 알지 않는다

**대상**: 각 앱의 `LayerRulesTest.java`
→ `shouldNotDependOnQuerydsl()` · `shouldNotDependOnInfrastructurePersistence()` · `controllersShouldNotDependOnPersistencePorts()` · `controllersShouldNotDependOnQueryPorts()`

api 모듈은 `com.querydsl..`과 `com.tastyhouse.infrastructure.jpa..`(JpaEntity·Mapper·JpaRepository·PersistenceAdapter — 메서드명 `shouldNotDependOnInfrastructurePersistence`는 유지)에 **직접 의존하지 않는다.** 컨트롤러가 리포지토리·QueryDAO를 직접 주입하는 것도 같은 이유로 금지다 — 읽기·쓰기 모두 포트를 거친다.

### api 모듈에서 도메인 enum `switch`를 쓰지 않는다

api 모듈 컴파일 클래스패스에 `domain`이 없어 도메인 enum `switch`는 **컴파일부터 불가능**하다. 도메인 enum으로 분기해야 하는 판정은 **`application`에서 한다.** api 모듈은 이미 결정된 값(`*Result`의 `String`)을 읽어 표현만 한다.

### 인가는 `SecurityConfig`가 소유한다 — 컨트롤러에 `@PreAuthorize`가 없는 것은 누락이 아니다

**대상**: 각 앱 `src/main/java/com/tastyhouse/{web,admin,ceo}api/config/security/SecurityConfig.java`
→ `securityFilterChain(HttpSecurity)`, `PublicPaths.PATTERNS`

인가 계층은 세 앱 모두 같은 형태로 선언한다.

1. **공개 경로는 `PublicPaths.PATTERNS`에서 중앙 관리한다** — 컨트롤러마다 흩어 놓지 않는다. **경로를 옮기거나 바꿀 때 이 목록을 함께 고치지 않으면**, 컴파일·테스트는 그대로 통과하고 **비로그인 사용자에게 401이 나가는 형태로만** 드러난다.
2. **로그아웃(`/api/auth/v1/logout`)은 인증만 요구하고 역할을 요구하지 않는다** — 임의 토큰이 블랙리스트에 등록되는 것을 막기 위해서다.
3. **나머지 API의 게이트는 `anyRequest()` 한 줄이 소유한다.** 그래서 개별 컨트롤러에 `@PreAuthorize`가 없는 것은 **누락이 아니다.**

**단, 3번의 강도는 앱마다 다르다** — 아래를 서로 복사하지 않는다.

| 앱 | `anyRequest()` | 성격 |
|---|---|---|
| `web-api` | `.authenticated()` | 손님 앱이라 역할 구분이 없다 |
| `admin-api` | `.hasAnyRole("ADMIN", "SUPER_ADMIN")` | 심층 방어 — `authenticated()`로 낮추지 않는다 |
| `ceo-api` | `.hasRole("CEO")` | 심층 방어 — 점주 전용 |

`admin`·`ceo`의 역할 요구는 **잘못 발급·유출된 비-관리자 토큰이 `authenticated()`만으로 통과하는 것을 차단**하는 심층 방어다. "어차피 로그인했으니 충분하다"는 판단으로 `authenticated()`로 완화하지 않는다.

### 도메인 enum은 HTTP 경계에서 `String`으로 받는다 — `allowableValues`는 수동 동기화 대상이다

**대상**: 3앱의 `..adapter.in.web.request..` 검색 조건 record 다수 (`status`·`category`·`changeType`·`requestType`·`result`·`actionType` 등)

도메인 enum 경계 규칙에 따라 HTTP 경계는 enum이 아니라 `String`으로 받고, 승격은 서비스가 `from(String)`으로 수행한다. api 모듈은 domain-free여야 하기 때문이다(위 "api 모듈에서 도메인 enum `switch`를 쓰지 않는다" 참조).

**부작용이 하나 있다** — `String` 파라미터는 Swagger가 enum 스키마를 자동 생성하지 못하므로 후보값을 `@Parameter(schema = @Schema(allowableValues = ...))`로 **수동 명시**한다(3앱 합계 140여 파일). **이 수동 목록은 enum과 자동으로 동기화되지 않는다.** enum에 상수를 추가·삭제하면 이 목록도 함께 고쳐야 하며, 어긋나도 빌드는 통과하고 Swagger 문서만 조용히 거짓말을 한다.

### 조회 파라미터는 `@RequestParam` 나열이 아니라 Request record로 감싼다

**대상**: 3앱의 `..adapter.in.web.request..`의 `*SearchRequest` record

필터가 적더라도 `@RequestParam`을 나열하지 않고 Request record로 감싼다. 파라미터가 늘어날 때 시그니처가 아니라 record가 자라고, 검증·정규화·Swagger 스키마가 한자리에 모인다.

### JWT·인증 쿠키 접두어 — backend에는 이 개념이 없다

**대상**: `frontend/{web,admin,ceo}/src/lib/auth-config.ts` → `ACCESS_TOKEN`·`REFRESH_TOKEN`·`REMEMBER_ME`

세 앱은 인증 쿠키를 `th_web_`·`th_admin_`·`th_ceo_` 접두어로 분리한다. 같은 호스트에서 포트만 달리 뜨는 개발 환경에서 **브라우저 쿠키가 앱끼리 덮어쓰는 것을 막기 위한 이름 공간 분리**다.

**이 접두어는 frontend 전용이며 backend java·yml에는 등장하지 않는다**(실측 0건). 세 api 앱은 `Authorization` 헤더로 받은 토큰을 검증할 뿐 쿠키 이름을 알지 않으므로, **backend 코드에서 이 접두어를 찾지 말 것.** 접두어를 바꿔야 하면 고칠 곳은 위 frontend 3파일뿐이다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

이 절의 항목은 **코드의 특정 지점을 이렇게 바꾸지 말라는 금지 지시**다. java 주석 금지 규칙에 따라 이 문서가 그 지시의 유일한 소재지다.

### `ext['jackson-bom.version'] = '2.21.7'` — 내릴 때가 아니라 올릴 때만 손댄다

**대상**: `backend/build.gradle`
→ `configure(subprojects.findAll { ... })` 블록의 `ext['jackson-bom.version']`

`firebase-admin`이 끌어오는 jackson과 정렬하려고 두는 핀이다. **Spring Boot BOM이 관리하니 불필요하다고 판단해 제거하면 안 된다** — BOM이 오히려 버전을 끌어내린다.

과거 이 값을 `2.17.3`으로 두었더니 Spring Boot BOM이 `2.18.3`까지 끌어내려 **취약 버전(`WS-2026-0003`, CVSS **7.5**)이 전 모듈에 깔린 사고가 실제로 있었다.** 그래서 이 핀은 **내릴 때가 아니라 올릴 때만 손댄다.**

**그 뒤 `2.20.0`으로 올렸으나 그것도 불충분해 `2.21.6`으로 다시 올렸다**(2026-09-06). `WS-2026-0003`은 Mend 자체 ID이고, 실체는 async 파서가 `StreamReadConstraints.maxNumberLength`를 강제하지 않아 긴 숫자 리터럴로 DoS가 가능한 결함이다(CWE-770). 연결된 권고문이 **두 개**라 수정 버전을 헷갈리기 쉽다.

| 권고문 | 영향 범위 | 수정(2.21 가지) | 수정(2.18 LTS) |
|---|---|---|---|
| `GHSA-72hv-8253-57qq` (`CVE-2026-18401`) | `2.15.0`~`2.18.5`, `2.19.0`~`2.21.0` | `2.21.1` | `2.18.6` |
| `GHSA-r7wm-3cxj-wff9` (위 수정이 불완전해 나온 후속) | `2.18.8` 미만, `2.19.0`~`2.21.3` | `2.21.4` | `2.18.8` |

즉 **`2.20.x`는 어느 쪽도 고쳐지지 않았다** — 패치가 올라간 가지는 `2.18.x`와 `2.21.x`뿐이다. 둘 다 닫는 최소 버전은 **`2.21.4`**이다. **`2.20.x`로 되돌리면 두 권고문이 함께 되살아난다.**

**그 뒤 `2.21.6`도 불충분해 `2.21.7`로 올렸다**(2026-10-11). `2.21.6`에는 jackson-core `GHSA-7hhh-6rmp-j9qf`·`GHSA-p6pp-m3f8-5c89`(`CVE-2026-89425`·`CVE-2026-89407`)와 jackson-databind `GHSA-cxp5-3px4-pw24`·`GHSA-wv8q-qhhj-9h54`(`CVE-2026-91777`·`CVE-2026-91776`)가 걸려 있었고, 2.21 가지의 수정 버전이 모두 `2.21.7`이다(2.18 LTS는 `2.18.11`). 현재 값은 그 가지의 최소 안전 버전이다.

검증은 OSV에 버전을 직접 질의해서 한다 — `vulns`가 빈 배열이어야 한다.

```bash
curl -s -X POST 'https://api.osv.dev/v1/query' -H 'Content-Type: application/json' \
  -d '{"package":{"name":"com.fasterxml.jackson.core:jackson-core","ecosystem":"Maven"},"version":"2.21.7"}'
```

**Boot 버전을 올릴 때의 검증 절차** — 이 핀이 오히려 버전을 낮추고 있지 않은지 확인한다.

```bash
cd backend && ./gradlew dependencies --configuration runtimeClasspath
```

**표기 주의**: `jackson-annotations`만 patch 자리를 뗀 `'2.21'`로 해석된다(2.20부터 이어진 표기다). **오타가 아니므로 `'2.21.7'`으로 "고치지" 않는다.**

**인접 핀 2건**: 같은 블록의 `ext['commons-lang3.version']`·`ext['netty.version']`은 근거가 코드에 기록된 적이 없다. 성격이 같을 가능성은 있으나 **확인되지 않았으므로 여기에 추측을 적지 않는다.**

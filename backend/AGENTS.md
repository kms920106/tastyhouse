<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# tastyhouse-api

## Purpose
음식점/가게(Shop) 기반 커머스 플랫폼의 백엔드. Spring Boot 3.2.4 / Java 21 기반 Gradle 멀티모듈 프로젝트로, 회원·주문·결제·리뷰·예약·쿠폰·포인트 등 22개 도메인을 제공한다. 전통적 계층형에서 시작해 **DDD / Clean Architecture(Strangler Fig 점진 전환)** 를 거쳐, `core-module` → `domain` 전환으로 **도메인 계층이 프레임워크를 전혀 모르는(production 의존 0개) 구조**에 도달했다. 비즈니스 규칙을 도메인 객체에 캡슐화하는 Rich Domain Model을 지향한다.

전환 이후 구조의 핵심 네 가지:
- **`domain`은 프레임워크-프리**다. Spring Web뿐 아니라 JPA·QueryDSL·`spring-tx`/`spring-orm`도 없다 — `@Transactional`/`@Service`/`@Component`가 한 곳도 없고, domain에는 포트 없는 순수 계산기·정책만 남는다. 포트를 주입받는 도메인 서비스는 `application/<ctx>/service/`에 `@Service`로 살며, 쓰는 앱이 하나면 그 앱의 `{앱}-application`, 두 앱 이상이면 코어 `application-core`에 둔다. domain의 순수 계산기·정책은 코어의 `shared/config/SharedBeanConfig`가 `@Bean`으로 등록한다.
- **api 모듈(web/admin/ceo/batch)은 QueryDSL도 infrastructure도 모른다**. 조회 계약(`{Ctx}QueryPort` 인터페이스 + `*Result`/`*SearchCondition`)은 패키지 `com.tastyhouse.application.<ctx>.port.out`에 있고 전부 코어 `application-core`가 소유한다. `infrastructure:jpa`의 `{도메인}QueryAdapter`가 그 인터페이스를 구현한다. 조회 유스케이스 서비스는 DAO가 아니라 포트 인터페이스를 주입하므로 `com.tastyhouse.infrastructure..` import가 **0건**이다. `com.tastyhouse.infrastructure..`·`com.querydsl..` 의존은 4개 모듈의 ArchUnit `LayerRulesTest`가 차단한다.
- **application 계층은 코어 + 앱 모듈 4개로 물리 분리돼 있다**. 앱 하나만 쓰는 UseCase·Command·서비스·앱 전용 SPI 포트는 `{web|admin|ceo|batch}-application`이, 2개 앱 이상이 쓰는 도메인 서비스·리스너·`@Configuration`·`port.out` 계약은 `application-core`가 소유한다([결정 0036](#decision-0036)). web·admin·ceo는 **유스케이스 하나 = 포트 하나 = 서비스 하나**다(예: web `com.tastyhouse.application.notice.service.NoticeListQueryService`, admin `NoticeManagementListQueryService`, ceo `com.tastyhouse.application.shop.service.ShopRequestListQueryService`). batch는 `*SchedulerService` 1:1이다. 규칙은 [CLAUDE.md](CLAUDE.md#application-서비스-cqrs-분리-규칙-도메인commandservice도메인queryservice). api 모듈은 컨트롤러 + `request/` + `response/`를 갖는 **thin adapter**이며(세 application 모듈의 `io.swagger` import 0건), 모듈 경계가 "계층 × 앱" 2차원이라 계층 위반이 ArchUnit 사후 검출이 아니라 **컴파일 에러**가 된다.
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
| `infrastructure/restclient/` | **외부 연동 HTTP 코어** — Boot `RestClient.Builder`를 꾸미는 `RestClientConfig`만 갖고, 예외·에러코드·파일 저장 SPI는 없다. 외부 HTTP 호출은 Spring `RestClient`로 통일했다(webflux 없음). **앱이 직접 의존하지 않는다** — web은 oauth(경유 kakao/naver/apple/facebook-oauth)·pg(경유 tosspayments)·sms(경유 solapi), batch는 bbq·admdongkor·pg(경유 tosspayments)를 통해 전이로 실리고 admin·ceo에는 없다 (see `infrastructure/restclient/AGENTS.md`) |
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
| `infrastructure/pg/` | **[조립·스타터]** 결제 PG 스타터 — 기본 벤더 tosspayments 조립(`runtimeOnly`)만 하는 자바 코드 없는 모듈. 라우터(`List<PgProviderGatewayPort>` → `PgPaymentGatewayRouter`)는 `application-core`의 `payment/service/PgPaymentGatewayRouter`(`@Service`)다. **web-api(승인·취소·즉시 조회)와 batch-module(주기 대사)이 의존** (see `infrastructure/pg/AGENTS.md`) |
| `infrastructure/tosspayments/` | Toss 결제 승인·취소·주문번호 조회 벤더 구현(`application-core`의 `PgProviderGatewayPort` 구현, `provider()`가 `PgProviderCode.TOSS`). 앱이 아니라 `:pg`가 조립 (see `infrastructure/tosspayments/AGENTS.md`) |
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
| `batch-module/` | 배치 앱의 **부트스트랩 + driving adapter**(`@Scheduled` 트리거 8종) 전담 독립 실행 모듈 (see `batch-module/AGENTS.md`) |
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
- **presentation의 도메인 결합 격리 — 유스케이스당 서비스 1개**: 컨트롤러가 `domain`에 직접 결합되는 것을 막기 위해, 컨트롤러 ↔ 도메인 사이를 application 서비스가 중개한다. 그 서비스는 api 모듈이 아니라 `{web|admin|ceo|batch}-application`이 소유하며, 컨트롤러는 그 짝인 UseCase 인터페이스(`<ctx>/port/in/`)만 주입한다. web·admin·ceo는 **유스케이스 하나 = 포트 하나 = 서비스 하나**다([결정 0043](application-core/AGENTS.md#decision-0043)). 정본은 [CLAUDE.md](CLAUDE.md#application-서비스-cqrs-분리-규칙-도메인commandservice도메인queryservice).
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
             └→ infrastructure:{persistence,file-storage,bbq,admdongkor,pg} (runtimeOnly)
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
     ([결정 0031](infrastructure/jpa/AGENTS.md#decision-0031))
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
   ※ web 전용 SPI를 구현하는 벤더(oauth 4종·javamail·solapi·aws-ses·aws-sns)는 web-application을,
     bbq·admdongkor는 batch-application을 의존한다. jpa·mybatis·firebase·aws-s3·tosspayments는 application-core를 의존한다
     (tosspayments가 구현하는 PG SPI는 web·batch가 함께 쓰므로 코어 소유 — 결정 0061)
infrastructure:kakao-oauth    ─→ infrastructure:restclient, web-application(auth SPI)          ← SocialOAuthClientPort 구현(KAKAO)
infrastructure:naver-oauth    ─→ infrastructure:restclient, web-application(auth SPI)          ← SocialOAuthClientPort 구현(NAVER)
infrastructure:apple-oauth    ─→ infrastructure:restclient, web-application(auth SPI) + jjwt ← SocialOAuthClientPort 구현(APPLE)
infrastructure:facebook-oauth ─→ infrastructure:restclient, web-application(auth SPI)  ← SocialOAuthClientPort 구현(FACEBOOK)
   ※ 4벤더 전부 domain 의존이 없다 — 실패 표현은 web-application의 SocialOAuthResult/SocialOAuthFailure다.
     jjwt 선언은 apple만 — 컴파일 격리일 뿐이며 런타임에는 application → security-core 경로로 jjwt가 이미 web 전체에 실린다
infrastructure:pg        ─→ runtimeOnly infrastructure:tosspayments ← [조립·스타터] 코드 없음(web·batch). 라우터는 application-core PgPaymentGatewayRouter(@Service)
infrastructure:tosspayments ─→ infrastructure:restclient, application-core ← PgProviderGatewayPort 구현(provider()는 application-core의 PgProviderCode.TOSS)
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
- **읽기 계약은 전부 `application-core`가 소유한다**: 패키지 `com.tastyhouse.application.<ctx>.port.out`의 읽기 계약을 코어 한 모듈이 갖는다. 소비 앱 수로 소유 모듈을 가르지 않는다([결정 0015](application-core/AGENTS.md#decision-0015)).
  - **프레임워크-프리는 ArchUnit이 강제한다**: `application-core`는 spring 의존을 받아 컴파일 게이트가 없으므로, `LayerRulesTest#readContractsShouldBeFrameworkFree`가 계약 전체를 검사한다. 단 write 포트 `port.out.write`(도메인 타입 `XxxLoadPort`·`XxxSavePort`)는 이 대상에서 빠지고 `#writePortsShouldOnlyDependOnDomainAndPortOut`(`java..`·`com.tastyhouse.domain..`·`application..port.out..`만 허용)이 따로 검사한다. 같은 모듈 안의 FQCN 중복은 컴파일 에러라 별도 가드가 필요 없다.
- **application 계층이 읽기 계약을 보는 경로**: 조회 유스케이스 서비스는 infra DAO 구현체가 아니라 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스를 주입한다. 계약이 전부 코어에 있으므로 이를 위한 추가 의존 선언은 없다. api 모듈은 `com.tastyhouse.infrastructure..`를 **전혀 import하지 않는다** — 각 모듈 `LayerRulesTest`가 이를 강제한다. `infrastructure:jpa`(앱은 조립 모듈 `infrastructure:persistence`를 거쳐 전이로 받는다)는 각 앱 부트스트랩의 중첩 `ModuleScanConfig`가 `com.tastyhouse.infrastructure`를 문자열로 스캔하는 대상이라 실행 모듈의 **런타임** 의존 그래프에는 있지만, `runtimeOnly`라 **컴파일 클래스패스에도, 소스 코드 레벨의 import 대상에도 없다.**
- **`Projections.constructor` 투영**: Result record가 QueryDSL을 모르는 `application-core`에 있어 `@QueryProjection`을 달 수 없다. `infrastructure:jpa`의 QueryAdapter는 `Projections.constructor(XxxResult.class, ...)`로 리플렉션 기반 조립을 한다 — Result record가 `public`이 아니거나 생성자 시그니처가 select 절과 불일치하면 컴파일은 통과하고 **호출 시점에 500**이 나므로, 새 쿼리는 반드시 한 번 호출해 확인한다. 이 리플렉션 대상 일치는 `infrastructure:jpa`의 `ProjectionConstructorMatchingTest`가 소스 스캔으로 검증한다.
- `querydsl-jpa`는 `infrastructure:jpa`에서 `implementation`으로 강등되어 소비 모듈 클래스패스로 전이되지 않는다. 전 프로젝트에서 QueryDSL을 컴파일하는 모듈은 `infrastructure:jpa` 하나뿐이다.
- 실행 가능한(bootJar) 모듈은 `web-api`/`admin-api`/`ceo-api`/`batch-module` 넷뿐이다. 나머지(`domain`/`application-core`/`{web,admin,ceo,batch}-application`/`infrastructure:*` 24개/`security-core`/`security-module`/`api-common-module`/`logging-module`)는 `bootJar` 비활성 + plain jar.
  - **중첩 프로젝트 컨테이너 주의**: `include 'infrastructure:persistence'`는 소스가 없는 빈 프로젝트 `:infrastructure`를 함께 만든다. 루트 `build.gradle`의 `subprojects` 일괄 설정이 이 컨테이너에까지 `bootJar`를 걸면 빌드가 깨지므로, 일괄 설정 대상에서 제외되는지 확인한다.
- **application 계층은 infrastructure를 컴파일 클래스패스에 두지 않는다**: application 계층이 infra를 모른다는 규칙을 ArchUnit이 아니라 **빌드 그래프가 1차로 강제**한다 — `import com.tastyhouse.infrastructure...` 한 줄이 실제 컴파일 에러가 된다(`domain`의 프레임워크-프리 게이트와 같은 방식). 그래서 `@Transactional`을 위한 `spring-tx`를 명시 선언한다. ArchUnit 규칙(`shouldNotDependOnInfrastructure`)은 누군가 build.gradle에 의존을 되돌리는 회귀를 막는 2차 방어선이다.
- **모듈 등록은 앱 부트스트랩의 문자열 스캔이다**: 라이브러리 모듈은 `AutoConfiguration.imports`로 자기 등록하지 않는다. 각 앱 부트스트랩이 static 중첩 클래스 `ApplicationLayerScanConfig`(`com.tastyhouse.application` 스캔, 필터 없음)와 `ModuleScanConfig`(`@Configuration(proxyBeanMethods = false)` + `@ComponentScan(basePackages = {...})` 문자열 패키지)를 갖는다 — `ModuleScanConfig`는 web `infrastructure`·`security`·`logging`·`apicommon.ratelimit`, admin·ceo는 여기에 `apicommon.exception` 추가, batch `infrastructure`·`logging`. 문자열 스캔이라 컴파일 클래스패스가 필요 없어 `runtimeOnly` 헥사고날 게이트가 유지된다(`@Import(Class)`는 그 이유로 불가). 앱의 `@Import`와 `scanBasePackages`는 없다. 모듈 설정 클래스는 `{Xxx}ModuleConfig`(`@Configuration`, 스캔 없음)이고, 이 설정들에 `@ConditionalOnBean`/`@ConditionalOnMissingBean`을 쓰지 않는다(일반 설정에서는 순서 의존이라 조용히 틀린다 — 환경 조건 `@ConditionalOnWebApplication`/`@ConditionalOnProperty`만 허용). imports 재유입은 4앱 `ApplicationLayerScanConfigTest`의 `assertNoTastyhouseAutoConfiguration`이 막는다. "클래스패스 존재 = 활성화" 원칙이 적용되며, 활성화 목록은 앱 스캔 목록이다([결정 0038](#decision-0038)). 상세는 `backend/CLAUDE.md`의 모듈 등록 컨벤션 절.
- **새 도메인 서비스는 스캔 엔트리가 아니라 `@Service`로 등록한다**: 쓰는 앱이 하나면 그 앱의 `{앱}-application`, 두 앱 이상이면 `application-core`의 `<ctx>/service/`에 `@Service`로 둔다. domain 모듈 클래스(순수 계산기·정책)처럼 애노테이션을 달 수 없는 빈만 `application-core`의 `shared/config/SharedBeanConfig`에 `@Bean`으로 둔다.
- **모듈 경계 원칙 — 2차원 경계**: 모듈 경계는 **계층 × 앱** 두 축이다.
  - **계층 축**: `domain`(순수 도메인) → application 계층(`application-core` + `{앱}-application`, 유스케이스) → api 모듈(인바운드 어댑터). `infrastructure/` 아래 24모듈은 **driven 18 + 조립 6**이다. `infrastructure:{jpa,mysql,mybatis,redis,restclient,firebase,aws-s3,aws-ses,aws-sns,kakao-oauth,naver-oauth,apple-oauth,facebook-oauth,tosspayments,javamail,solapi,bbq,admdongkor}` 18모듈이 아웃바운드(driven) 어댑터 쪽이다(`restclient`는 포트를 구현하지 않는 HTTP 코어, `mysql`은 DB 연결 코어지만 기술 코어라 여기에 센다). **조립 6모듈** — `persistence`(DB — jpa + mysql)·`file-storage`·`oauth`·`pg`·`mail`·`sms` — 은 전부 자바 코드가 없고 도메인 포트를 구현하지 않는 컴포지션 루트 조각으로, 앱이 DB·외부 연동을 쓸 때 직접 의존하는 좌표다(`backend/CLAUDE.md`의 "모듈 지도").
  - **앱 축**: 같은 계층이라도 web·admin·ceo·batch는 서로의 모듈을 알지 않는다(앱 모듈끼리 의존하지 않는다).
  - **infrastructure는 기술별로 나눈다**: `infrastructure:jpa`는 application 포트의 **DB 어댑터 전용**(write `persistence` + read `query`), `infrastructure:mysql`은 DB 연결, `infrastructure:mybatis`는 notice 레거시 MyBatis 구현, `infrastructure:redis`는 Redis 연결·rate limit 카운터·토큰 저장소, `infrastructure:restclient`와 벤더 13모듈(`firebase`·`aws-s3`·`aws-ses`·`aws-sns`·`kakao-oauth`·`naver-oauth`·`apple-oauth`·`facebook-oauth`·`tosspayments`·`javamail`·`solapi`·`bbq`·`admdongkor`)이 외부 시스템 연동 어댑터이고, 조립 6모듈이 그 구현을 앱에 묶어 준다 — **driven adapter는 DB·Redis뿐 아니라 외부 연동까지 전부 `infrastructure:{기술}` 아래에 둔다**. 자바 패키지 루트는 모듈명을 따른다 — 코드가 있는 17모듈 전부 `com.tastyhouse.infrastructure.{모듈명의 하이픈을 점으로}`를 루트로 쓰고, 각 모듈의 아키텍처 테스트 `shouldResideInModuleRootPackage`가 강제한다(조립 6모듈과 `mysql`은 자바 코드가 없어 패키지가 없다). **외부 연동을 벤더·채널 단위까지 쪼개는 기준은 "앱별 실사용 차이"다** — admin·ceo는 파일 저장 하나만 쓰므로 OAuth·결제·메일·SMS와 AWS SDK를 받지 않는다. domain에 포트가 없는 기술이라도 **순수 인프라 기술이면 `infrastructure:{기술}`**에 두고, **여러 presentation이 공유하는 보안 관심사**일 때만 `security-module`, **HTTP 플럼빙**이면 `api-common-module`에 둔다. 결제(`infrastructure:pg`)는 벤더 배타 선택이 아니라 **여러 PG 벤더가 공존하며 `PgProvider`로 라우팅**하고, 그 라우터(`PgPaymentGatewayRouter`)는 web·batch가 함께 쓰므로 `application-core`의 `@Service`다 — 상세는 `backend/CLAUDE.md`의 "공존형 채널 — PG 라우터" 절. 소셜 로그인(`infrastructure:oauth`)도 제공자 4종이 공존하며, `web-application`의 `SocialOAuthClientRouter`가 `SocialProvider`로 벤더를 고른다(같은 파일의 "공존형 채널 — 소셜 로그인 스타터" 절).
  - **컨텍스트별 모듈 분할은 하지 않는다**: 컨텍스트 경계(25종)는 모듈이 아니라 `domain`의 ArchUnit `ContextBoundaryTest`(봉인 목록)가 담당한다.
- **api 모듈 공용 플럼빙은 `api-common-module`이 단독 소유**한다: `ApiResponse`/`PaginationResponse`/`PageRequest`/`ProblemDetails`와 admin·ceo 공용 `GlobalExceptionHandler`가 여기 있다. **완전 동일한 것만** 공유하며, 내용이 다른 정책 파일(`SecurityConfig`·`PublicPaths`·`TokenService`·`AuthService`)과 계약이 다른 응답 record(`ShopDetailResponse` 등)는 앱마다 둔다 — 허용 목록은 [CLAUDE.md](CLAUDE.md#api-모듈-공용-플럼빙-소유-규칙-api-common-module) 표 참고. `GlobalExceptionHandler`가 web-api의 자체 핸들러와 함께 뜨지 않는 것은 스캔 범위가 보장한다 — admin·ceo의 `ModuleScanConfig`만 `com.tastyhouse.apicommon.exception`을 스캔하고 web은 스캔하지 않는다(빈 이름 `globalExceptionHandler`).
- **소셜 로그인은 `com.tastyhouse.application.auth.port.out` SPI로만 사용**한다: web-api는 제공자별 패키지(`com.tastyhouse.infrastructure.kakao.oauth..` 등)의 wire DTO·클라이언트를 직접 import하지 않고 `SocialOAuthClientPort`/`SocialProfile`만 안다(ArchUnit `shouldDependOnOauthSpiOnlyNotProviderPackages`가 강제). 이 SPI는 domain이 아니라 `web-application`이 소유한다 — 구현은 벤더 4모듈 `infrastructure:{kakao,naver,apple,facebook}-oauth`이고 `infrastructure:oauth`는 그것을 조립하는 코드 없는 스타터다. 소셜 OAuth의 호출부가 전부 표현 계층이라 도메인 서비스가 쓰는 포트가 아니기 때문이다. 상세는 [CLAUDE.md](CLAUDE.md#소셜-로그인-spi-규칙-application의-authportout) 참고.

### Testing Requirements
- 스키마 무변경 보장: `hibernate.ddl-auto=validate` 기준. JPA 엔티티(`infrastructure:jpa`) 변경 시 `schema.sql`과 정합성 확인.
- QueryDSL Q클래스는 `infrastructure:jpa`에서만 생성된다(`infrastructure/jpa/build/generated/...`) — 경로 변경 시 `./gradlew clean compileJava` 필요. `domain`에는 apt가 없어 Q타입이 생성되지 않는다.
- 도메인 불변식은 `domain/src/test`의 **순수 단위 테스트**로 검증한다(스프링 컨텍스트·DB 불필요).
- 레이어 경계는 각 모듈의 `architecture/LayerRulesTest`(ArchUnit)로 검증한다. 이 규칙들은 `allowEmptyShould(true)`를 쓰지 않으므로, 대상 클래스가 0건이면 **공허 통과가 아니라 실패**로 드러난다.
- **`allowEmptyShould(true)` 금지는 자동 검증된다**: `noClasses().that()...` 형태는 대상이 0건이어도 조용히 통과하므로, `application-core/src/test/.../RuleAnchorTest`(앱별 하한)와 `BatchSchedulerRulesTest`(batch exact — `*SchedulerService` 8 · `..port.in..` 8)가 각 규칙의 anchor 개수를 직접 세어, 클래스가 모듈 사이를 옮겨 다니다 대상이 통째로 사라지면 **빌드가 실패**하게 한다. 모듈을 분리·이동할 때 같은 패턴을 따른다.

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

- 에러 처리는 `application`의 `ErrorResponses.resolve`와 api-common의 `ApiErrorCode`를 쓰고(`backend/CLAUDE.md` "예외·에러코드 소유 규칙"), 페이징 타입 `PageQuery`/`PageResult`는 `com.tastyhouse.application.shared.port.out.page` 소유이며, `*Result`는 도메인 enum 대신 `String`(+ `{field}Description`/`{field}DisplayName`)을 싣고 온다. 그래서 api 모듈이 domain 타입을 쓸 정상 경로가 없다([결정 0025](#decision-0025)).
- `api-common-module`·`security-module`이 `domain`이 아니라 `application-core`에 의존하므로 **presentation 컴파일 클래스패스에 `domain`이 아예 없다.** 이 규칙은 누군가 의존 한 줄을 되돌리는 회귀를 잡는 2차 방어선이다(`api-common-module`·`security-module`에도 같은 취지의 `LayerRulesTest#shouldNotDependOnDomain`이 있다).
- **⚠️ 위반은 `import`로 보이지 않는다**: ArchUnit은 import 문이 아니라 바이트코드 상수 풀을 읽으므로, 이 모듈에 `import com.tastyhouse.domain..`이 0건이어도 `*Result` 컴포넌트를 통한 **전이 의존**으로 잡힌다. **grep으로 검증하면 "위반 0건"으로 오판할 수 있다 — 검증은 반드시 이 테스트로 한다.**

### 공허 통과 금지 — `allowEmptyShould(true)`를 쓰지 않는다

**대상**: 각 앱의 `LayerRulesTest.java` (파일 전체)

**`allowEmptyShould(true)`는 이 파일 어디에도 쓰지 않는다.** 대상이 0건이 된 규칙은 공허 통과를 여는 대신 **삭제하거나 다른 모듈로 옮긴다.**

application 계층을 대상으로 하는 규칙(`commandServicesShouldNotDependOnQueryPorts` · `queryServicesShouldNotDependOnWritePorts` · 유스케이스 1:1 규칙 · `commandRecords*` · `portIn*` · `commandServicesShouldNotDependOnRequestRecords` · `applicationServicesShouldNotDependOnWebLayer`)은 `application-core` 모듈의 테스트에 있다 — **api 모듈에 두면 대상 0건으로 공허하게 통과하기 때문이다.**

`restControllersShouldResideInWebAdapterPackage`가 `classes()` 형태인 것은 **컨트롤러 실존에 anchor** 하기 위해서다 — 컨트롤러가 0건이 되면 곧바로 실패한다. `apiModuleShouldBeDomainModelFree`는 anchor가 모듈 전체(`noClasses()`)라 클래스가 존재하는 한 **대상 0건이 될 수 없다.**

### 인바운드 어댑터의 앱 격리 — 컴파일 게이트가 지킨다

**대상**: 각 앱의 `build.gradle`과 `ApplicationModuleClasspathTest`

각 앱은 `application-core`와 자기 `{앱}-application` 하나만 의존하므로, 다른 앱의 UseCase·Command는 클래스패스에 없어 **컴파일 에러**다. 다른 앱 모듈이 의존에 섞이는 사고는 각 앱의 `ApplicationModuleClasspathTest`가 막는다([결정 0036](#decision-0036)).

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

## 결정 경위

이 문서가 소유한 규칙이 **왜 지금 모양이 됐는지**를 결정 단위로 적습니다. 본문은 현재 규칙만 담고, "예전에는 어땠고 왜 바뀌었는가"는 이 절에만 둡니다. 이 절은 항상 문서의 마지막 `##` 절이며, 항목은 번호 순입니다. 작성 규칙은 루트 `CLAUDE.md`의 "규칙 문서 개정 규칙" 절에 있습니다.

<a id="decision-0005"></a>

### 0005. 인바운드 포트(UseCase 인터페이스)를 도입한다 — 완전 매핑 전략

- 상태: 채택
- 결정 커밋: `01d6d7494` (2026-08-29)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0005-inbound-port-full-mapping.md`

#### 맥락

- **인바운드 포트(UseCase 인터페이스)**: 컨트롤러가 호출하는 애플리케이션 연산을 인터페이스로 선언한 것. 컨트롤러는 구현 서비스 대신 이 인터페이스를 주입받는다.
- **완전 매핑 전략**: 『만들면서 배우는 클린 아키텍처』 그림 8.3. 계층 경계마다 그 계층 전용 모델로 매핑한다(Request → Command, Result → Response).

이전에는 인터페이스 없이 컨트롤러가 구체 서비스를 직접 주입했다. 경계 위반은 ArchUnit이 사후에 잡았다. 완전 매핑 전환(컨트롤러의 도메인 타입 유입 청산)을 시작하면서 이 결정을 다시 열었다.

#### 이전 규칙

##### `backend/CLAUDE.md` — "인바운드 포트·컨텍스트별 모듈 분할은 도입하지 않는다 (재론 방지)"

경계를 강제하는 다른 선택지를 검토한 뒤 의도적으로 채택하지 않기로 했다.

- **인바운드 포트(UseCase 인터페이스)를 도입하지 않는다**: 도메인 서비스마다 UseCase 인터페이스를 두면 인터페이스가 약 128개 생기는데, 각각의 구현체가 하나뿐이고 소비자도 모듈당 하나뿐이라 다형성·교체 가능성이라는 인터페이스의 값을 전혀 얻지 못한다. 순수 보일러플레이트이며 "구현으로 점프하려면 한 단계 더 거쳐야 한다"는 탐색 비용만 남는다. (출력 포트는 다르다 — 구현이 인프라에 있고 도메인이 그것을 몰라야 하므로 의존 역전의 실익이 있다.)
- **컨텍스트별 모듈 분할을 하지 않는다**: 경계는 ArchUnit으로 충분하며 25개 모듈로 쪼개면 빌드 그래프·`settings.gradle`·의존 선언이 늘어난다.

##### `backend/CLAUDE.md` — 인바운드 포트(UseCase 인터페이스)를 도입한다 — 완전 매핑 전략 채택 (번복 직후 문구)

- **입도 규칙(당시)**: 기본은 애그리거트 단위 `{도메인}CommandUseCase`/`{도메인}QueryUseCase` 쌍이고, 메서드 7개를 초과하거나 컨트롤러 2개 이상이 공유하는 대형 서비스만 per-operation(연산 하나당 인터페이스 하나)으로 분해한다. 처음부터 per-operation으로 쪼개면 과거 우려대로 순수 보일러플레이트가 된다는 이유였다. 이후 [결정 0043](application-core/AGENTS.md#decision-0043)으로 바뀌었다.
- **컨텍스트별 모듈 분할 금지의 근거(당시)**: 모듈 분할이 주는 강한 보장이 필요한 것은 모듈 간 방향 의존(domain ← infrastructure ← api)이고 그쪽은 이미 분리돼 있다.
- **단일 예외 `application-common-module`**: 읽기 경로 포트를 소유할 모듈 하나를 신설한 것이 이 금지의 유일한 예외였으나, 챕터 09에서 계약이 전부 소비 앱의 application 모듈로 옮겨가 모듈이 비면서 삭제됐다(공유분 55개는 잠시 `domain`을 거쳐 챕터 04에서 `application`으로 돌아왔다 — [결정 0007](application-core/AGENTS.md#decision-0007)·[결정 0015](application-core/AGENTS.md#decision-0015)).

#### 결정

인바운드 포트를 도입한다. 과거 반대 논거(다형성 실익이 0에 가깝다)는 **사실로 인정**하고, 다른 근거로 뒤집는다.

| 항목 | before | after |
|---|---|---|
| 컨트롤러가 주입하는 타입 | 구체 서비스 클래스 | UseCase 인터페이스(`<ctx>/port/in/`) |
| 경계 강제 수단 | ArchUnit(사후 검출) | 컴파일 게이트(구체 서비스에 손대는 코드가 컴파일되지 않음) + ArchUnit |
| 입도(당시) | — | 애그리거트 단위 `{도메인}CommandUseCase`/`{도메인}QueryUseCase` 쌍. 메서드 7개 초과 또는 컨트롤러 2개 이상이 공유하는 대형 서비스만 연산당 인터페이스 1개 |
| 컨텍스트별 모듈 분할 | 하지 않음 | 여전히 하지 않음 |

제목에 "번복"을 남긴 이유는 폐기된 논거가 그럴듯해서 같은 논의가 다시 열리기 쉽기 때문이다(원문).

#### 근거

1. **컴파일 게이트** — 컨트롤러가 `application/service/`의 구체 클래스를 아예 모르게 만든다. ArchUnit은 위반을 사후에 잡지만, UseCase 타입만 주입받으면 구체 서비스에 손대는 코드가 애초에 컴파일되지 않는다. 경계 강제 수단의 등급이 한 단계 올라간다.
2. **경계 계약의 문서화** — UseCase 인터페이스 한 파일이 그 애그리거트의 연산 계약(어떤 명령이 있고 무엇을 받아 무엇을 돌려주는가)을 한눈에 고정한다.

#### 결과·감수한 손실

- 이후 `935b76bc1`·`da6163964`가 전 컨텍스트에 도입했다.
- 입도 규칙("쌍이 기본")은 이후 유스케이스당 서비스 1개로 바뀌었다([결정 0043](application-core/AGENTS.md#decision-0043)). 지금은 web·admin·ceo 전부 연산 하나당 포트 하나다. application `LayerRulesTest#useCasesShouldDeclareSingleOperation`이 포트당 추상 메서드 1개를, `#useCaseServicesShouldImplementExactlyOneUseCase`가 서비스당 포트 1개를 강제한다(batch 출처는 제외 — `*SchedulerService` 7개가 이미 1:1).
- 모듈 재편 프로그램(챕터 01~06)이 앱별 application 모듈 4개와 `infrastructure:persistence`/`infrastructure:redis` 재편을 하면서 모듈 경계가 "계층 × 앱" 2차원이 됐다. 분할 대상이 "컨텍스트"(25개, 수평)가 아니라 "계층 × 앱"(8개, 수직)이어서 모듈 폭증 우려는 현실화되지 않았다 — 모듈은 25개가 아니라 6개 늘었다. 계층 위반이 ArchUnit 사후 검출이 아니라 컴파일 에러가 되어, 컴파일 게이트 논거를 모듈 수준으로 한 단계 더 밀어붙인 셈이다.
- "컨텍스트별 모듈 분할 금지"는 계층별 분할 금지가 아니다 — 이후 앱별 application 모듈 분리([결정 0008](#decision-0008))·통합([결정 0013](#decision-0013))·재분리([결정 0036](#decision-0036))가 이어졌지만 컨텍스트 경계는 여전히 domain의 `ContextBoundaryTest`가 담당한다.
- 서비스 간 협력에도 UseCase 인터페이스를 주입하게 된 것은 [결정 0033](application-core/AGENTS.md#decision-0033).

<a id="decision-0006"></a>

### 0006. Request→Command 변환은 Request의 toCommand로 컨트롤러가 소유한다

- 상태: 채택
- 결정 커밋: `01d6d7494` (2026-08-29)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0006-request-to-command-in-controller.md`

#### 맥락

- **Request**: HTTP 요청 본문을 받는 record(검증 + Swagger 스키마).
- **Command**: application 계층 유스케이스가 받는 입력 record.

이전에는 Request를 순수 데이터 홀더로 두고, 컨트롤러가 필드를 하나씩 꺼내 Service 메서드 인자로 넘겼다. 완전 매핑 전략([결정 0005](#decision-0005))을 채택하면서 "계층 경계의 매핑은 누가 하는가"를 다시 정했다. 같은 커밋에서 0005 다음으로 들어간 결정이다.

#### 이전 규칙

##### `backend/CLAUDE.md` — DTO 조립 규칙 (`d60628c43`, 2026-07-05)

원시 파라미터 → command/VO/condition 변환은 대상 record 자신의 정적 팩토리 `of(...)`로 한다. **Request DTO는 command 생성 책임을 지지 않는 순수 데이터 홀더(검증 + Swagger 스키마)로 유지하고, `toCommand()` 같은 변환 메서드를 두지 않는다.** 컨트롤러가 개별 원시 필드로 언패킹해 Service에 전달한다.

#### 결정

| 항목 | before | after |
|---|---|---|
| 변환 메서드 | Request에 `toCommand()` 금지 | Request에 `toCommand(...)`를 둔다 |
| 변환 주체 | 컨트롤러가 원시 필드를 낱개로 언패킹 → Service 다인자 호출 | 컨트롤러가 `XxxCommand command = request.toCommand(경로변수, principal 식별자, ...);`로 Command 조립 |
| 본문에 없는 값(경로 변수·인증 주체) | Service 인자로 직접 | `toCommand`의 파라미터로 주입(`request.toCommand(id, userDetails.getMemberId())`) |

#### 근거

- 계층 경계의 매핑은 그 경계를 지키는 어댑터의 책임이다(완전 매핑 전략).
- 낱개 언패킹 방식은 필드가 늘수록 Service 시그니처가 길어져 **파라미터 15개짜리 메서드**를 낳았다.
- 같은 타입(Long/String) 인자가 줄줄이 늘어선 호출부에서 **순서를 착각해 값이 조용히 뒤바뀌는 사고**(컴파일은 통과)가 반복됐다. 이름 있는 record 필드로 묶으면 그 사고 유형 자체가 사라진다.

#### 결과·감수한 손실

- wire 계약(요청/응답 JSON·에러 코드·HTTP 상태)은 바뀌지 않았다(커밋 메시지).
- Request가 application Command 타입을 알게 됐다. Request는 여전히 domain-free·infra-free이며, 문자열 → 도메인 enum 승격은 Request에서 하지 않는다.
- `toCommand` 안에서도 같은 타입 인자의 위치 기반 조립은 위험하므로 일부 Request에는 "위치 기반 조립 금지" 봉인이 붙어 있다(예: ceo-api `ProductNutritionUpdateRequest.toCommand`).

<a id="decision-0008"></a>

### 0008. 앱별 application 계층을 api 모듈에서 떼어 {앱}-application 모듈 4개로 분리한다

- 상태: 대체됨(→ [결정 0013](#decision-0013))
- 결정 커밋: `c55520733` (2026-08-30)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0008-per-app-application-modules.md`

batch(파일럿, `c55520733`) → web(`fbc301bd3`) → admin(`9612a64d3`) → ceo(`928178555`) 순으로 application 계층을 앱별 모듈로 물리 분리한다.

| 항목 | before | after |
|---|---|---|
| application 계층 위치 | 각 api 모듈 안 | `{web,admin,ceo,batch}-application` 4모듈(`com.tastyhouse.{앱}application`) |
| api 모듈 | 컨트롤러 + 서비스 | thin adapter(컨트롤러 + `request/` + 설정 + 부트스트랩) |
| application의 infra 의존 | 컴파일 클래스패스에 있음 | 없음 — `import com.tastyhouse.infrastructure...`가 컴파일 에러 |
| 모듈 경계 | 계층 1차원 | "계층 × 앱" 2차원 |
| 빈 배선(파일럿) | 앱 자체 스캔 | `BatchApplicationConfig` `@Import`(`InfrastructureModuleConfig` 선례) |

<a id="decision-0010"></a>

### 0010. api 모듈은 domain을 금지하되 domain.exception·domain.shared.page·enum 읽기 accessor 3종만 carve-out한다

- 상태: 대체됨(→ [결정 0025](#decision-0025))
- 결정 커밋: `3bf8f9171` (2026-09-01) — enum accessor carve-out은 `787dd580f` (2026-09-01)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0010-api-domain-free-carve-outs.md`

`apiModuleShouldBeDomainModelFree`(web/admin/ceo-api 3곳)를 신설해 모듈 전역에서 `com.tastyhouse.domain..`을 금지하고 아래 3종만 허용했다.

| carve-out | 허용 근거(당시) |
|---|---|
| `domain.exception..` | 계층 칸이 없는 횡단 관심사. `api-common-module`이 `api project(':domain')`로 공용 에러 계약을 전 모듈에 노출한다. 없으면 web-api `GlobalExceptionHandler`가 즉시 걸린다 |
| `domain.shared.page..` | 페이징 조립을 컨트롤러로 옮기며 정상 경로가 됐다(application이 `PageResult`를 반환하고 컨트롤러가 `PaginationResponse.from(...)`으로 감싼다) |
| 도메인 enum(`isEnum()` + `DOMAIN_ROOT`) | Response 조립을 컨트롤러로 올리며 `result.type().name()`처럼 enum을 읽는 것이 설계상 필연이 됐다. 짝 규칙 `apiModuleShouldOnlyReadDomainEnums`가 호출 가능 메서드를 `ALLOWED_DOMAIN_ENUM_ACCESSORS`(`name`·`getDescription`·`getDisplayName`)로 제한 |

| 항목 | before | after |
|---|---|---|
| 검사 범위 | `*ApiController`·`..request..`만 | api 모듈 전역 |
| 허용 | (무검사 영역 전부) | carve-out 3종 |
| `AdminSeeder` | `AdminRole.SUPER_ADMIN.name()` | 리터럴 `"SUPER_ADMIN"`(서비스가 `AdminRole.from`으로 승격·검증) |
| 시더 주입 | 구체 `AdminQueryService`/`CeoQueryService` | 인바운드 포트(`seedersShouldDependOnUseCasesOnly` 신설) |

<a id="decision-0013"></a>

### 0013. 앱별 application 모듈 4개를 application 하나로 합친다(챕터 01)

- 상태: 대체됨(→ [결정 0036](#decision-0036))
- 결정 커밋: `68d160710` (2026-09-04)
- 대체한 결정: [결정 0008](#decision-0008)
- 원문: 삭제 전 git 이력의 `docs/adr/0013-application-module-merge.md`

| 항목 | before | after |
|---|---|---|
| Gradle 모듈 | `{web,admin,ceo,batch}-application` 4개 | `application` 1개(모듈 수 17 → 14) |
| 자바 패키지 | `com.tastyhouse.{app}application` + `com.tastyhouse.application.<ctx>.port.out` | 이 챕터에서는 **불변**(평탄화는 챕터 03 — [결정 0014](#decision-0014)) |
| "자기 앱의 UseCase만 주입" 강제 | 빌드 | 각 api 모듈 `LayerRulesTest#adaptersShouldOnlyUseOwnAppUseCases`(이 챕터 신설) |

**이 챕터의 범위는 Gradle 모듈만 4 → 1이다.** 뒤 챕터에서 동명 클래스 182건 개명(02) → 패키지 평탄화 + 앱 마커 애노테이션(03) → 공유 읽기 계약 55개 복귀(04)가 이어졌다.

<a id="decision-0014"></a>

### 0014. application 패키지를 평탄화하고 앱 소속을 마커 애노테이션으로 표현한다(챕터 03)

- 상태: 대체됨(→ [결정 0036](#decision-0036))
- 결정 커밋: `9b888f1c5` (2026-09-04)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0014-package-flattening-app-markers.md`

| 항목 | before | after |
|---|---|---|
| 자바 패키지 | `com.tastyhouse.{web\|admin\|ceo\|batch}application` 4개 | `com.tastyhouse.application` 하나 |
| 앱 소속 표현 | 패키지 | 마커 4종 `com.tastyhouse.application.shared.marker.{WebApp,AdminApp,CeoApp,BatchApp}`(`@Component` 메타 없음, `@Target(TYPE)`·`@Retention(RUNTIME)`·`@Documented`) |
| 마커 부착 대상 | — | 빈 242개(`@Service` 220 + `@Component` 22)와 UseCase 인터페이스 257개에 정확히 하나씩 |
| Command record 소속 | 패키지 | 마커 없이 유도 — testFixtures `AppOwnership`이 시그니처·컴포넌트 전이 폐쇄로 계산(0개=고아, 2개 이상=경계 위반). carve-out 1건 `ShopStorePriceVerificationItemCommand`(`DESERIALIZED_COMMANDS`) |
| 빈 스캔 | 앱 패키지 | `@ComponentScan(basePackages = "com.tastyhouse.application", useDefaultFilters = false, includeFilters = @Filter(type = ANNOTATION, classes = XxxApp.class))` |
| 앱 격리 가드 | 패키지·슬라이스 술어 | 마커 술어(`AppIsolationTest` 4규칙, api 4모듈의 `adaptersShouldOnlyUseOwnAppUseCases`) |
| `<ctx>/port/out`의 의미 | 읽기 계약만 | 이 도메인의 모든 아웃바운드 계약(읽기 계약 + 아웃바운드 SPI + 명령 서비스가 반환하는 Result/View record) |

후속으로 5번째 마커 `@SharedApp`(리스너 전용, "4앱 전부에 뜬다")이 추가됐다.

<a id="decision-0016"></a>

### 0016. infrastructure:external을 벤더·채널별 모듈로 분리한다(기술별 분할 채택)

- 상태: 채택(aws 벤더 묶음은 [결정 0022](#decision-0022), 파일 저장 코어 SPI는 [결정 0021](infrastructure/file-storage/AGENTS.md#decision-0021)로 대체)
- 결정 커밋: `109fb06de` (2026-09-05)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0016-external-vendor-channel-split.md`

#### 맥락

외부 시스템 연동(OAuth 4종·토스 결제·메일 JavaMail/SES·SMS Solapi/SNS·파일 Firebase/S3·BBQ 크롤링·행정동 경계)과 무거운 벤더 SDK(AWS SES/SNS/S3, Firebase Admin)가 모듈 `infrastructure:external` 하나에 들어 있었고, **4개 앱이 그것을 통째로 받았다.**

- **벤더**: 특정 외부 서비스 구현(예: Solapi, Firebase).
- **채널**: 기능 단위(메일, SMS, 결제 등). 한 채널에 벤더가 여럿일 수 있다.

실사용은 그렇지 않았다(`backend/infrastructure/restclient/AGENTS.md` "분리 배경" 원문).

| 앱 | 실제로 쓰는 어댑터 |
|---|---|
| web-api | OAuth 4종 · Toss · Mail · SMS · File |
| admin-api | File만 |
| ceo-api | File만 |
| batch-module | File(원격 이미지) · BBQ · 행정동 경계 |

admin/ceo/batch가 메일·SMS 어댑터까지 들여와야 했던 직접 원인은 persistence의 `MailDomainConfig`·`SmsDomainConfig`가 `MailSenderPort`/`SmsSenderPort` 빈을 무조건 요구한 것이었다.

#### 이전 규칙

##### `backend/CLAUDE.md` — "external을 infrastructure 아래로 들인 이유"의 비채택 대안 (`c52280a87`, 2026-09-05 08:35)

- **비채택 대안 (1) 기술별 추가 분할**: "3~7파일짜리 모듈 6~7개와 `build.gradle` 보일러플레이트 복제, 공통 자산(`WebClientConfig`·`ExternalApiException`) 분리 비용이 이득을 넘는다."
- **비채택 대안 (3) AWS 어댑터를 `aws/` 벤더 패키지로 모으기**: "제공자 선택 축이 벤더가 아니라 채널이고 자격증명도 채널별로 따로여서 공유할 AWS 설정 코드가 없다."
- 이 대안들은 같은 날 앞선 챕터 01에서 `external-api` 모듈을 `infrastructure:external`로 편입할 때 기록됐다. `-api` 접미어는 인바운드 어댑터를 뜻하는데 `external-api`는 아웃바운드 클라이언트 모음이라 이름이 역할을 거꾸로 가리켰고, 편입 후 driven 어댑터 3형제 `persistence`·`redis`·`external`이 `infrastructure` 아래 나란히 놓였다. 이때 외부 연동 전체는 여전히 `external` 한 모듈이었다.

##### `backend/infrastructure/restclient/AGENTS.md` — Dependencies / Internal

분리 전 코어(`infrastructure:external`)는 소셜 로그인 SPI·BBQ·행정동 경계 포트를 구현하느라 `application`을 `implementation`으로 의존했다. 분리로 그 어댑터들이 oauth·crawling 모듈로 떠나 코어에는 아웃바운드 계약 소비자가 남지 않았고, `application` 의존도 없어졌다.

##### `backend/infrastructure/restclient/src/main/java/com/tastyhouse/infrastructure/restclient/AGENTS.md` — 다른 모듈로 이동한 패키지

분리 직후 코어 디렉터리의 패키지는 다음처럼 옮겨졌다(이름은 그 시점 기준이며, 이후 infrastructure 패키지 루트 통일로 전부 `com.tastyhouse.infrastructure.*`가 됐다).

| 과거 패키지 | 분리 직후 |
|---|---|
| `oauth/{kakao,naver,apple,facebook}` | `infrastructure:oauth`(패키지 불변) |
| `payment/toss` | `infrastructure:payment`(패키지 불변) |
| `mail/`, `mail/javamail`, `sms/`, `sms/solapi` | `infrastructure:messaging`(패키지 불변) |
| `mail/ses` · `sms/sns` · `file/s3` | `infrastructure:aws`(`external.aws.ses`·`external.aws.sns`·`external.aws.s3`로 변경) |
| `file/firebase` | `infrastructure:firebase`(`external.firebase`로 변경) |
| `crawling/bbq`, `region/` | `infrastructure:crawling`(패키지 불변) |
| `file/RemoteImageDownloader` | `infrastructure:crawling`(`external.crawling`으로 변경) |
| `file/ByteArrayMultipartFile` | 삭제(전략 인터페이스가 `byte[]`를 받게 되어 래퍼가 불필요) |

벤더 패키지를 `external.file` 아래에 두지 않은 이유는 코어 `external.file` 스캔이 하위 패키지를 동반 스캔하기 때문이다(아래 "결과·감수한 손실"의 형제 패키지 이동).

#### 결정

대안 (1)·(3)을 채택으로 뒤집는다. `infrastructure:external`을 7모듈로 나누고, 앱은 실사용 어댑터만 의존한다.

| 항목 | before | after |
|---|---|---|
| 모듈 | `infrastructure:external` 1개 | `external`(코어: WebClient·예외·파일 전략 SPI) / `firebase` / `aws` / `oauth` / `payment` / `messaging` / `crawling` |
| admin·ceo 의존 | external 전체 | external + firebase |
| batch 의존 | external 전체 | + crawling |
| web 의존 | external 전체 | + oauth + payment + messaging |
| aws | 4앱 클래스패스 | 어느 앱도 의존하지 않고 컴파일만 검증 |
| `MailDomainConfig`·`SmsDomainConfig` | persistence | messaging으로 이관 |
| 모듈 수 | 14 | 20 |

#### 근거

- **대안 (1) 번복 근거는 앱별 실사용 실측이다** — admin-api·ceo-api가 실제로 쓰는 어댑터는 파일 저장 하나뿐인데 OAuth·Toss·메일·SMS·크롤링과 Firebase Admin·AWS SDK 3종을 통째로 받고 있었다. 공통 자산은 코어에 남기고 6모듈이 그것을 의존하므로 "분리 비용"으로 본 것은 실재하지 않았다. `build.gradle` 보일러플레이트 복제(7벌)는 그대로지만 앱이 안 쓰는 SDK를 배포 산출물에서 빼는 이득이 더 크다.
- **대안 (3) 번복 근거는 설정 공유가 아니라 의존 무게였다** — "공유할 AWS 설정 코드가 없다"는 관찰 자체는 맞다. 다만 AWS SDK 3종은 어느 앱도 활성화하지 않는데 4앱 전부에 실려 있었다. 벤더로 묶는 목적이 "설정 재사용"에서 "쓰지 않는 SDK를 한 덩어리로 떼어내기"로 바뀌었다.
- 기준은 "앱별 실사용 차이"다 — 이 기준은 지금도 외부 연동 모듈을 쪼개는 기준이다.

#### 결과·감수한 손실

- 동작 변경 없음(빌드·4앱 기동 검증). `FileStorageStrategy`를 `byte[]` 기반으로 단순화하고 `ByteArrayMultipartFile`을 삭제했다. 코어 `external.file` 스캔이 하위 패키지를 동반 스캔하므로 firebase·s3·ses·sns·`RemoteImageDownloader` 5건은 형제 패키지로 옮겼다(커밋 메시지).
- 이후 같은 기준의 세분화가 이어졌다 — 파일 저장 스타터([결정 0018](#decision-0018)), crawling·messaging·payment·oauth의 채널·벤더 분할, 코어의 `infrastructure:http-client` → `infrastructure:restclient` 리네임과 WebClient 제거·예외 계약 해체.
- **부분 대체**: aws 단일 모듈은 `aws-s3`·`aws-ses`·`aws-sns` 3모듈로 다시 나뉘었고([결정 0022](#decision-0022)), 코어의 파일 저장 SPI(`FileStorageStrategy`·`FileStoragePortAdapter`)는 삭제돼 벤더가 `FileStoragePort`를 직접 구현한다([결정 0021](infrastructure/file-storage/AGENTS.md#decision-0021)).

<a id="decision-0017"></a>

### 0017. 라이브러리 모듈을 Spring Boot auto-configuration으로 자기 등록한다(챕터 02)

- 상태: 대체됨(→ [결정 0038](#decision-0038))
- 결정 커밋: `cad5e5880` (2026-09-05)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0017-library-auto-configuration.md`

| 항목 | before | after |
|---|---|---|
| 모듈 설정 클래스 | `{Xxx}ModuleConfig`(`persistence`는 `InfrastructureModuleConfig`) | `{Xxx}ModuleAutoConfiguration`(`@AutoConfiguration`, persistence는 `PersistenceModuleAutoConfiguration`) — 13개 |
| 등록 주체 | 앱 `@Import` 나열 | 모듈의 `AutoConfiguration.imports` 자기 등록. 앱 `@Import`는 `{App}ApplicationConfig` 하나만 |
| 앱 → 라이브러리 의존 | `implementation` | `runtimeOnly`(`security-module`·`api-common-module`만 `implementation` 유지) |
| `scanBasePackages` | 4앱에 나열 | 4앱 전부에서 사라짐 |
| 빈 발견 | — | auto-configuration 클래스가 자기 모듈 루트 패키지 하나를 `@ComponentScan` |
| 앱별 차이 | 스캔 범위·`@Import` | 조건부 `@Bean`(`@ConditionalOnWebApplication`·`@ConditionalOnBean`·`@ConditionalOnMissingBean`) |
| api-common 공용 예외 핸들러 | 부분 스캔 | `@Bean("sharedGlobalExceptionHandler")` + `@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)` — web에서 스스로 물러남 |
| `RateLimitAspect` | 컴포넌트 스캔 | `@Bean` + `@ConditionalOnBean(RateLimitCounterPort.class)` + `afterName = "…RedisModuleAutoConfiguration"` |

새 원칙은 **"클래스패스 존재 = 활성화"** 다. 모듈을 앱에 붙이는 비용은 "gradle 1줄 + (설정이 있으면) yml import 1줄"이다.

<a id="decision-0018"></a>

### 0018. 외부 연동은 코드 없는 채널 스타터 + 벤더 구현 모듈로 세분한다

- 상태: 채택
- 결정 커밋: `ae9870cd4` (2026-09-05)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0018-channel-starter-vendor-modules.md`

#### 맥락

[결정 0016](#decision-0016)으로 외부 연동이 벤더·채널별 모듈로 나뉘었지만, 파일 저장을 쓰려면 앱마다 코어 SPI(`:infrastructure:external`)와 벤더 구현(`:infrastructure:firebase`)을 **둘 다** `build.gradle`에 선언하고, `spring.config.import`에도 `application-external.yml`·`application-firebase.yml`을 둘 다 나열해야 했다. 같은 벤더 결정이 4앱 × 2곳 = 8곳에 복제됐고, 앱이 "Firebase로 저장한다"는 구현 선택을 알고 있었다.

- **스타터(starter)**: 자바 코드 없이 "무엇을 조립하는가"만 선언하는 Gradle 모듈. `spring-boot-starter-data-redis`가 Lettuce를 고르는 것과 같은 형태.

이 결정은 번복이 아니라 세분화를 묶는 우산 결정이다. 루트 `CLAUDE.md`의 모듈 수 연대기도 여기에 기록한다.

#### 이전 규칙

앱이 한 기능을 이루는 모듈(코어 SPI + 벤더 구현)과 그 yml을 직접 나열한다.

채널 모듈은 등록 코드를 가질 수 있었다 — messaging(이후 `mail`·`sms`)은 `MailDomainConfig`·`SmsDomainConfig`를, payment(이후 `pg`)는 `PgGatewayConfig`(라우터 등록)를 가졌다. 그래서 "채널(등록 코드 있음) vs 스타터(코드 없음)"로 나뉘어 있었다.

##### `backend/infrastructure/restclient/AGENTS.md` — 어디로 갔는지 (포인터) — 형제 모듈 수 연대기

당시 서술은 `infrastructure` 아래 driven 어댑터 12개에 챕터 03에서 신설된 `infrastructure:file-storage`가 더해져 13개가 됐고, 이 하나만 어댑터가 아니라 자바 코드 없는 조립 전용 스타터라고 적었다. 이후 분할로 21개가 됐고, 소셜 로그인 채널 `infrastructure:oauth`도 채널·벤더 분할로 같은 형태가 되어 스타터는 `file-storage`·`oauth` 둘이라고 적었다(지금은 조립 6모듈 — 아래 결과 참고).

##### `backend/infrastructure/redis/AGENTS.md` — 신설 배경 (챕터 05) — 형제 수 연대기

챕터 05 재편 후 `infrastructure:external`이 편입돼 driven 어댑터 3형제(`persistence`·`redis`·`external`)가 됐고, external 기술별 분할과 `aws` 3분할로 `persistence`·`redis`·`external`·`firebase`·`aws-s3`·`aws-ses`·`aws-sns`·`oauth`·`payment`·`messaging`·`bbq`·`admdongkor` 12형제가 됐다. 이후 `messaging` 4분할(2026-09-26)로 15형제(코어 `external`은 `restclient`로 리네임), `payment`가 `pg`·`tosspayments`로 나뉘어 16형제, `oauth` 채널 스타터화와 벤더 4종 신설(2026-09-27)로 19형제가 됐다. 코드 없는 스타터 `file-storage`·`oauth`는 어댑터가 아니라 형제 수에서 뺐다.

##### `backend/security-module/AGENTS.md` — Working In This Directory — 새 관심사 배치 판단 기준

판단 기준 제목에 "챕터 05 개정, 챕터 03으로 세분화" 꼬리표가 붙어 있었고, 외부 연동 어댑터 목록에서 코드 없는 조립 스타터는 `file-storage`·`oauth` 둘만 "어댑터를 갖지 않는다"고 적었다(당시 `pg`·`mail`·`sms`는 등록 코드가 있는 채널로 어댑터 목록에 들어 있었다).

#### 결정

**한 기능을 이루는 모듈 여러 개를 앱이 나열하지 않는다 — 조립 전용 스타터 모듈이 묶어서 노출하고, 앱은 그 스타터 하나만 의존한다.**

| 항목 | before | after |
|---|---|---|
| 앱 `build.gradle`(파일 저장) | external + firebase 2줄 | `runtimeOnly project(':infrastructure:file-storage')` 1줄 |
| 앱 yml import | `application-external.yml` + `application-firebase.yml` | `classpath:application-file-storage.yml` 1줄 |
| `file.provider` 키 | `application-external.yml` | 스타터의 `application-file-storage.yml` |
| 스타터의 자바 코드 | — | 없음(`compileJava` NO-SOURCE, jar에는 yml만) |
| 컴파일 클래스패스 | — | 벤더 없음 — `runtimeOnly`는 `runtimeElements`로 전이되어 `runtimeClasspath`에만 실린다 |

적용 판단 기준: 여러 앱이 같은 조합(교체 가능한 벤더 구현과 그 설정)을 반복 선언하면 스타터 후보다. 앱마다 조합이 다르거나 교체 가능한 벤더가 하나뿐이면 만들지 않는다.

같은 패턴의 후속 분할(번복이 아니라 세분화라 결정 항목을 따로 만들지 않았다):

| 커밋 | 내용 | 모듈 수 |
|---|---|---|
| `ae9870cd4` | 파일 저장 스타터 `infrastructure:file-storage` 신설 | 20 → 21 |
| (`15e76e263`, [결정 0022](#decision-0022)) | `infrastructure:aws` → `aws-s3`·`aws-ses`·`aws-sns` | 21 → 23 |
| `cd4ef5262` | `infrastructure:crawling` → `bbq`·`admdongkor` | 23 → 24 |
| `b02f83ecc` | `infrastructure:external` → `infrastructure:http-client`를 거쳐 `infrastructure:restclient` 리네임, WebClient/webflux 제거·`RestClient` 통일, `ExternalApiException`/`ExternalApiErrorCode` 해체 | 24(불변 — 리네임과 HTTP 클라이언트 전환·예외 계약 해체는 모듈 경계를 바꾸지 않는다) |
| `ed5de7d27` | `infrastructure:messaging` → 채널 `mail`·`sms` + 벤더 `javamail`·`solapi` | 24 → 27 |
| `3829c07f7` | `infrastructure:payment` → 채널 `infrastructure:pg` 리네임 + 벤더 `tosspayments` 신설, PG 라우터 | 27 → 28 |
| `33651f12b` | `infrastructure:oauth`를 코드 없는 채널 스타터로, 벤더 `kakao-oauth`·`naver-oauth`·`apple-oauth`·`facebook-oauth` 신설 | 28 → 32 |

#### 근거

- 앱은 "파일을 저장한다"까지만 알고 "Firebase로"는 모른다 — 헥사고날 경계와 맞는다.
- 벤더 전환이 앱 무수정, 스타터 두 파일(`build.gradle` 한 줄 + yml 한 줄) 수정으로 끝난다.
- 스타터에 코드를 넣으면 조립 선언과 구현이 섞여 존재 이유가 사라진다.
- 조립 대상이 하나여도 스타터를 유지한다 — 앱이 벤더 이름을 모른다는 목적은 조립 대상 개수와 무관하고, 교체 가능한 벤더(firebase·s3)가 둘이다.

#### 결과·감수한 손실

- 동작 변경 없음(커밋 메시지).
- 공존형 채널은 배타 선택(`@ConditionalOnProperty`)이 아니라 공존+라우팅을 택했다 — PG(`PgPaymentGatewayRouter`), 소셜 로그인(4종 항상 함께 조립).
- "채널 모듈은 등록 코드를 갖는다"는 부분은 이후 뒤집혔다 — 등록 코드가 application으로 옮겨가 `pg`·`mail`·`sms`도 코드 없는 스타터가 됐다([결정 0026](application-core/AGENTS.md#decision-0026)). 지금 조립 6모듈(`persistence`·`file-storage`·`oauth`·`pg`·`mail`·`sms`)은 전부 자바 코드가 없다.
- 이후 모듈 수 변화(32 → 36 → 37 → 38 → 39)는 각각 [결정 0036](#decision-0036)·[결정 0045](infrastructure/persistence/AGENTS.md#decision-0045)·[결정 0047](infrastructure/jpa/AGENTS.md#decision-0047)·[결정 0051](infrastructure/mysql/AGENTS.md#decision-0051).

<a id="decision-0020"></a>

### 0020. backend java에는 주석을 쓰지 않고 근거는 AGENTS.md·docs/domain이 소유한다

- 상태: 채택
- 결정 커밋: `104b04d74` (2026-09-22)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0020-java-comments-forbidden.md`

#### 맥락

backend java 코드에는 Javadoc(`/** */`)·라인 주석(`// `)·블록 주석(`/* */`)으로 설계 근거·금지 지시·도메인 규칙이 흩어져 있었습니다. 같은 설명이 주석과 `AGENTS.md`·`docs/domain` 양쪽에 있으면 **한쪽만 갱신돼 두 벌이 갈라지는** 일이 생깁니다. `application*.yml`·`build.gradle`에 대해서는 이미 "설명은 AGENTS.md에 둔다"는 규칙이 있었습니다.

주석 전면 이관 프로그램(챕터 01~07)이 java 주석 33,996줄을 모두 제거하고 근거를 문서로 옮겼습니다. 이 커밋은 그렇게 확립된 컨벤션을 규칙 문서에 명문화한 것입니다(코드 변경 없음).

> 이 결정을 덮인 규칙으로 표시한 꼬리표(`번복됨 — java 주석 금지`)는 나중인 `663cf89dc`(2026-10-09)에 처음 들어갔습니다. 결정이 처음 문서에 들어간 커밋을 기준으로 삼아 `104b04d74`를 결정 커밋으로 둡니다.

#### 이전 규칙

##### backend/CLAUDE.md — 조회 포트 소비자별 분할 규칙 (포트명은 반환 Result 계열을 승계 — 챕터 04)

조회 포트를 소비자별로 나눌 때(`aaec3acb6`, 2026-08-30), 두 포트에 같은 시그니처로 중복 선언된 공유 메서드에는 `/** 공유 메서드 — {@link 상대포트}에도 같은 시그니처로 선언돼 있다. */` 주석을 달았다. 이 주석으로 어느 포트와 선언이 겹치는지를 코드에서 밝혔다.

#### 결정

**backend의 `*.java`에는 주석을 쓰지 않습니다.** Javadoc·라인 주석·블록 주석 전부가 대상이고 main·test를 가리지 않습니다. 설명할 것이 있으면 처음부터 문서에 씁니다. 목적지는 내용의 성격으로 정합니다.

| 항목 | before | after |
|---|---|---|
| java 주석 | 설계 근거·금지 지시를 주석으로 남김 | 쓰지 않음(주석 없는 파일이 의도된 상태) |
| 가드(변경 금지·제약) | 코드 옆 주석 | 그 모듈 `AGENTS.md`의 `## 봉인·가드 목록` 절 |
| 아키텍처 서술 | 코드 옆 주석 | 그 모듈 `AGENTS.md`의 `## 코드 주석에서 이관된 설계 근거` 절 |
| 도메인 규칙 | 코드 옆 주석 | `docs/domain/{도메인}.md` |
| 단순 서술(`@param`/`@return`/`@throws`) | Javadoc 태그 | 어디에도 쓰지 않음 |
| 공유 메서드 중복 선언 | `/** 공유 메서드 — {@link 상대포트}… */` 주석 | 주석 없이 선언만 중복. 겹치는 포트 쌍은 규칙 문서의 reference 목록이 소유 |
| 문서 항목의 코드 지시 | — | 역참조 앵커 3요소(리포 루트 기준 파일 경로 · 코드 요소명 · 내용), 줄 번호 금지 |
| 잔존 주석 정리 | — | 먼저 문서에 옮기고 그다음 지움(신규 작성 규칙이 아니라 정리 절차) |

#### 근거

- 설명이 코드와 문서 양쪽에 있으면 한쪽만 갱신돼 두 벌이 갈라집니다. yml·gradle 주석 규칙과 같은 근거이며, java에는 단위 라벨·블록 제목처럼 남길 만한 주석 형태가 없어 예외를 두지 않습니다.
- 단순 서술은 코드에서 그대로 읽히므로 문서에 적으면 즉시 낡는 중복이 됩니다. 적는 것이 안 적는 것보다 나쁩니다.
- 주석 없는 java 파일을 문서화 누락으로 오인해 주석을 되살리지 않도록, "의도된 상태"임을 루트 `CLAUDE.md`·`backend/CLAUDE.md`·`backend/AGENTS.md` 세 문서에 모두 밝혔습니다.
- 사용자가 전면 제거를 명시적으로 선택한 결과입니다.

#### 결과·감수한 손실

- `{@link}` 상호참조 1,126건이 사라져 IDE 상호 탐색이 나빠졌습니다. javadoc jar를 만드는 gradle 태스크가 없으므로 빌드는 영향받지 않습니다. 문서에서 다른 타입을 가리킬 때는 백틱 코드 표기나 마크다운 링크를 씁니다.
- 잔존 주석 검사의 기대값은 0이 아니라 `1`입니다(`// noinspection BusyWait`는 정적분석 도구가 읽음). ArchUnit `.because("...")`·`@DisplayName`·어노테이션·로그 메시지는 주석이 아니므로 지우지 않습니다.

<a id="decision-0022"></a>

### 0022. infrastructure:aws를 aws-s3·aws-ses·aws-sns 3모듈로 나눈다

- 상태: 채택
- 결정 커밋: `15e76e263` (2026-09-26)
- 대체한 결정: [결정 0016](#decision-0016)(일부 — AWS 어댑터를 `infrastructure:aws` 한 벤더 모듈로 묶은 부분)
- 원문: 삭제 전 git 이력의 `docs/adr/0022-aws-module-three-way-split.md`

#### 맥락

AWS 어댑터 3종은 채널이 서로 다릅니다.

- S3: 파일 저장
- SES: 메일
- SNS: SMS

[결정 0016](#decision-0016)의 external 분리 때 이 셋은 **벤더 축**으로 묶여 `infrastructure:aws` 하나가 됐고, 어느 앱도 이 모듈을 의존하지 않았습니다. 그런데 S3는 `file-storage` 스타터를 통해 4앱 전부가, SES·SNS는 web만 활성화합니다. 한 모듈에 묶여 있으면 파일 저장을 S3로 바꾸는 순간 aws 모듈의 전이 의존(코어 `infrastructure:external`·webflux·`messaging`)이 admin·ceo·batch runtimeClasspath에 함께 실립니다. 그러면 `MessagingModuleAutoConfiguration`이 그 앱들에서 발화합니다. 2026-09-26에 이 위험을 실측으로 확인했습니다.

함께 잠재 결함도 하나 있었습니다. `spring-cloud-aws-s3` 라이브러리만 선언돼 autoconfigure가 없었기 때문에, `file.provider=s3`로 바꾸면 `S3Operations` 빈이 등록되지 않아 주입이 실패했습니다.

#### 이전 규칙

##### backend/CLAUDE.md — 모듈 경계 규칙 (계층 × 앱 2차원 + 기술별 infrastructure)

처음 판단은 "AWS 어댑터를 `aws/` 벤더 패키지로 모으지 않는다"(비채택 대안 3)였다. 제공자를 고르는 축이 벤더가 아니라 채널이고, 자격증명도 채널별로 따로라서 공유할 AWS 설정 코드가 없다는 이유였다. 실제로 당시 `application-aws.yml`은 `file.aws.s3.*`·`mail.aws.ses.*`·`sms.aws.sns.*` 세 채널 블록을 따로 가졌다.

external 분리 때 이 대안을 채택으로 바꿔 AWS 어댑터를 `infrastructure:aws` 하나로 묶었다. 근거는 설정 공유가 아니라 **의존 무게**였다. AWS SDK 3종은 어느 앱도 활성화하지 않는데 4앱 전부의 클래스패스에 실려 있었다. 그래서 벤더로 묶는 목적이 "설정 재사용"에서 "쓰지 않는 SDK를 한 덩어리로 떼어내기"로 바뀌었고, 그 목적에는 벤더 축이 맞았다. 그 결과 `infrastructure:aws`는 어느 앱도 의존하지 않는 모듈이 됐다(`settings.gradle` 포함으로 컴파일만 검증).

전환 절차는 채널별로 달랐다.

- 파일 저장(S3): 앱을 건드리지 않고 `infrastructure/file-storage`의 두 파일만 바꿨다. `build.gradle`의 firebase 한 줄을 aws로 바꾸고, `application-file-storage.yml`의 import를 `classpath:application-aws.yml` + `file.provider: s3`로 바꿨다.
- 메일(SES)·SMS(SNS): web 전용 채널이라 스타터를 거치지 않았다. web `build.gradle`에 aws를 추가하고, web yml에 `application-aws.yml`을 import하고, provider를 바꿨다.

##### backend/infrastructure/aws-s3/AGENTS.md — 진입 설정은 아무것도 스캔하지 않는다

과거 `AwsModuleAutoConfiguration`은 `com.tastyhouse.external.aws` 루트를 통째로 스캔했다.

#### 결정

어댑터(채널) 단위로 `aws-s3`·`aws-ses`·`aws-sns` 3모듈로 나눕니다. 각 활성화 경로는 자기 의존만 끌고 갑니다.

| 항목 | before | after |
|---|---|---|
| 모듈 | `infrastructure:aws` 1개 | `infrastructure:aws-s3` · `aws-ses` · `aws-sns` 3개 |
| S3 모듈 의존 | 코어 + `messaging`(+webflux) + SDK 전부 | domain + `spring-cloud-aws-starter-s3`만 |
| SES 모듈 의존 | (위와 공유) | domain + 코어 + `messaging`(`MailProperties` 때문) |
| SNS 모듈 의존 | (위와 공유) | domain + 코어(`messaging` 의존 없음) |
| S3 SDK 선언 | `spring-cloud-aws-s3` 라이브러리(autoconfigure 없음) | `spring-cloud-aws-starter-s3`(`S3Operations` 자동 등록) |
| spring-cloud-aws BOM | 루트 `build.gradle`의 `dependencyManagement` 블록 | `infrastructure/aws-s3/build.gradle`에 `platform()`으로 선언 |
| 설정 스캔 범위 | `com.tastyhouse.external.aws` 루트 전체 | 각 모듈 자기 패키지만 |
| S3 전환 시 admin·ceo·batch 클래스패스 | 코어·webflux·messaging 함께 실림 | S3 의존만 실림 |
| 어느 앱이 의존하나 | 없음 | 없음(그대로, 파일 저장은 firebase 유지) |

#### 근거

- 채널마다 활성화하는 앱 집합이 다릅니다(S3는 4앱, SES·SNS는 web). 한 모듈에 묶으면 가장 넓은 활성화 경로가 가장 무거운 전이 의존을 끌고 갑니다. 그 결과 쓰지 않는 메시징 auto-configuration이 admin·ceo·batch에서 발화하는 위험이 실측으로 확인됐습니다.
- 벤더로 묶은 근거(쓰지 않는 SDK 떼어내기)는 "어느 앱도 의존하지 않는 상태"에서만 성립했습니다. 한 채널이라도 활성화하면 그 근거가 오히려 불필요한 의존을 끌고 오는 원인이 됩니다.
- 3모듈로 나누면서 스캔 범위도 각자 자기 패키지로 좁혀야 합니다. 루트를 스캔하면 형제 모듈이 같은 클래스패스에 있을 때 그 빈까지 등록하게 됩니다.

#### 결과·감수한 손실

- 잠재 결함 2건을 함께 고쳤습니다. ① starter-s3로 바꿔 `S3Operations` 자동 등록이 실제로 일어납니다. ② `dependencyManagement` 블록 방식의 BOM은 소비 앱으로 전파되지 않아 앱에서 버전 미해석(FAILED)이 났으므로 `platform()`으로 바꿨습니다. "라이브러리 BOM은 `platform()`으로 선언한다"는 규칙을 `backend/AGENTS.md`에 추가했습니다.
- 동작 변경으로, S3 전환 시 자격증명은 기본 체인이 아니라 `spring.cloud.aws.credentials` 정적 키를 씁니다.
- 이 분할로 backend 모듈 수가 21개에서 23개가 됐습니다(루트 `CLAUDE.md` "리포지토리 구조"의 모듈 수 연대기).
- 세 모듈 모두 여전히 어느 앱도 의존하지 않으므로, 부패를 막는 수단은 컴파일뿐입니다.
- 같은 날 messaging 4분할로 `MailProperties`가 삭제되고 `SesConfig`가 `mail.sender-address`를 `@Value` 키로 읽게 되며 `aws-ses`의 messaging 의존도 사라졌습니다. 이후 코어 예외 계약 해체로 `aws-ses`·`aws-sns`의 코어 의존도 없어져, 지금 두 모듈의 의존은 SDK와 포트 소유 모듈뿐입니다.

<a id="decision-0025"></a>

### 0025. 표현 계층(api·api-common·security)이 domain을 모르게 절단한다(엄격 레이어드 덩어리 01)

- 상태: 채택(ErrorContracts 부분 → [결정 0042](#decision-0042), `@SharedApp` 설정 허용 부분 → [결정 0036](#decision-0036))
- 결정 커밋: `dfb8f4685` (2026-09-27)
- 대체한 결정: [결정 0010](#decision-0010)
- 원문: 삭제 전 git 이력의 `docs/adr/0025-presentation-domain-cut.md`

#### 맥락

- **표현 계층**: HTTP 요청을 받고 응답을 만드는 바깥쪽 모듈. 여기서는 `web-api`·`admin-api`·`ceo-api`, 공용 플럼빙 `api-common-module`, 보안 `security-core`·`security-module`을 말한다.
- **domain**: 애그리거트·VO·도메인 enum·도메인 예외를 담는 가장 안쪽 모듈.
- **carve-out**: 금지 규칙에 일부러 뚫어 둔 예외 구멍.

[결정 0010](#decision-0010)에서 api 모듈은 domain을 금지하되 carve-out 3종(`domain.exception..`·`domain.shared.page..`·도메인 enum 읽기 accessor)을 열어 두었다. 그 구멍 때문에 표현 계층이 여전히 domain 타입을 컴파일 시점에 볼 수 있었고, `api-common-module`이 `api project(':domain')`으로 domain을 api 3모듈에 전이 노출했다. 두 `GlobalExceptionHandler`의 `@ExceptionHandler(BusinessException.class)` 메서드는 `com.tastyhouse.domain.exception`을 import해야 했고, 이것이 presentation의 domain 의존을 끊지 못하게 한 마지막 고리였다.

#### 이전 규칙

##### backend/AGENTS.md — `apiModuleShouldBeDomainModelFree` / `ALLOWED_DOMAIN_ENUM_ACCESSORS` / `domainEnum()` / `domainBoundaryPredicatesShouldStillBite`

- `apiModuleShouldBeDomainModelFree`는 모듈 전역에서 `com.tastyhouse.domain..`을 금지하되 carve-out 3종을 두었다.
  1. `domain.exception..`: 계층 칸이 없는 횡단 관심사로 보았다. `api-common-module`이 `api project(':domain')`로 공용 에러 계약을 전 모듈에 노출했다.
  2. `domain.shared.page..`: 페이징 조립을 컨트롤러로 옮기면서 정상 경로가 됐다. application이 `PageResult`를 반환하고 컨트롤러가 `PaginationResponse.from(...)`으로 감쌌다.
  3. 도메인 enum: 타입 성격 술어 `isEnum()`에 `DOMAIN_ROOT` 패키지 조건을 걸어 열었다. 도메인 enum 76개가 애그리거트 루트와 같은 `..model` 패키지에 살아서 패키지 술어로는 `Shop`·`Order`까지 열리기 때문이다.
- 짝 규칙 `apiModuleShouldOnlyReadDomainEnums`가 도메인 enum에 호출할 수 있는 메서드를 `ALLOWED_DOMAIN_ENUM_ACCESSORS`(`name`·`getDescription`·`getDisplayName`)로 제한했다. 바이트코드 실측(admin-api 기준 `name` 57 · `getDescription` 8 · `getDisplayName` 1)에서 도출했고 "항목을 추가하지 않는다"고 봉인했다.
- `domainEnum()` 술어는 `domain.exception..`을 제외했다. `ErrorCode`가 enum이라 짝 규칙이 전역 핸들러의 `getCode()`·`getDefaultMessage()` 호출을 잡았기 때문이다.
- `domainBoundaryPredicatesShouldStillBite`는 carve-out 술어가 무력해지지 않았는지 상시 단정했다(애그리거트 루트 `Shop`은 여전히 금지, `MemberGrade`는 carve-out 대상, enum과 애그리거트 루트가 같은 패키지에 산다는 전제 등 4항목).
- `controllersShouldBeDomainFree`도 같은 carve-out을 공유했다.
- enum carve-out의 근거: 실측 위반 67건이 전부 읽기 전용 accessor였고 도메인 객체 생성·상태 변경·리포지토리 접근은 0건이었다. 규칙의 원래 의도는 승격 방향(String·Long → 도메인 타입)을 막는 것인데 옮겨진 것은 강등 방향이고, ArchUnit 의존 그래프는 두 방향을 구분하지 못한다고 보았다. 다만 도메인 enum 76개 중 13개가 비즈니스 로직을 노출하므로(`MemberGrade#fromReviewCount`·`OrderStatus#canTransitionTo`) 타입 수준 carve-out만으로는 부족해 짝 규칙을 두었다.
- `domainEnum()`이 `domain.exception..`을 제외한 것은 짝 규칙이 web-api 전역 핸들러의 `ErrorCode` 호출을 실측 2건 잡았기 때문이며, 에러 계약은 클래스 수준 규칙에서도 carve-out된 횡단 관심사라 두 규칙이 같은 예외를 공유해야 한다고 보았다.
- 짝 규칙 아래에서는 api 모듈의 도메인 enum `switch`가 금지였다. `switch`는 컴파일 시 `ordinal()`/`values()` 호출로 낮아지는데 그 둘이 `ALLOWED_DOMAIN_ENUM_ACCESSORS` 밖이라 ArchUnit 위반이 됐고, 소스에 위반 메서드명이 없어 원인을 찾기 어려웠다.

##### backend/CLAUDE.md — 도메인 enum 경계 규칙 / DTO 조립 규칙

- 아웃바운드(강등) 방향은 챕터 06·07로 정해졌다. 읽기 계약 `*Result`가 품은 도메인 enum을 api 모듈의 Response가 `.name()`으로 읽어 `String`으로 내리는 것이 정상 경로였다. 비즈니스 로직 메서드(`MemberGrade#fromReviewCount`·`OrderStatus#canTransitionTo` 등) 호출은 금지였다.
- Response record는 `com.tastyhouse.domain.*`를 import하지 않지만, `*Result`가 품은 도메인 enum의 읽기 accessor 호출(`result.type().name()`)은 accessor 3종 범위에서 허용했다.

##### backend/CLAUDE.md — 앱 마커 규칙

- `@SharedApp`은 리스너 전용이었다. `LayerRulesTest#sharedAppOnlyOnListeners`가 `..listener..`의 `@TransactionalEventListener` 보유 클래스에만 허용했다. "리스너 외에는 달지 않는다"가 규칙이었다.

##### backend/api-common-module/AGENTS.md — Key Files / Internal / `GlobalExceptionHandler`

- 내부 의존은 `domain` 하나였고 `api`로 노출했다. 이 노출을 타고 `domain.exception`(`BusinessException`·`ErrorCode`)이 api 3모듈의 공용 에러 계약이 됐다.
- `BusinessException`은 전용 메서드 `handleBusinessException`이 `getErrorCode()`의 상태·code와 `getMessage()`로 처리했다. 권한 거부는 `ErrorCode.ACCESS_DENIED`를 썼다.

##### backend/{web,admin,ceo}-api/AGENTS.md — Internal / Working In This Directory

- 각 api 모듈은 `domain`을 선언하지 않았다. domain 타입이 다시 필요해져도 `api-common-module`의 `api project(':domain')` 전이 노출로 해결됐다. web-api `GlobalExceptionHandler`가 쓰는 `domain.exception..`도 이 경로였다.
- web-api는 모듈 전체가 domain-free이되 공용 에러 계약 `domain.exception..`만 carve-out이라고 적었고, 401 처리가 그 사례였다. Response record는 도메인 enum에 carve-out된 accessor(`name`·`getDescription`·`getDisplayName`)만 호출했다.
- admin-api는 carve-out 3종을 명시했다(챕터 06 페이징, 챕터 07 enum accessor).

##### backend/batch-module/AGENTS.md — Internal

- batch는 `api-common-module`을 의존하지 않아 domain 전이 경로가 없었다. web/admin/ceo와 달랐다.

##### backend/infrastructure/jpa/AGENTS.md — `<ctx>/query/` 도입부

- 반환 페이징 타입은 domain의 `shared/page/PageResult`, 페이징 입력은 `shared/page/PageQuery`였다.

##### backend/web-api/src/main/java/com/tastyhouse/webapi/AGENTS.md — Cross-cutting Packages / Common Patterns

- 서비스가 `{Ctx}QueryPort`로부터 받는 `PageResult<T>`는 domain `shared/page` 소속이었고, 이를 `PaginationResponse.from(...)`으로 변환했다.

##### backend/application-core/AGENTS.md — 표현 계약이 만들 수 없는 값은 이 모듈이 `*View`/`*ViewResult`로 넘긴다

- 순수 표현 파생(16자리 리뷰번호 0-pad, 문구 표시명 truncate, 거리별 배달팁 비움 판정)과 함께 enum → 문자열 강등도 Response로 내렸다.

##### backend/security-core/AGENTS.md — Key Files / Working In This Directory / Internal

- `build.gradle`이 `domain`을 `implementation`으로 의존했고, 그 이유는 토큰 검증 실패를 `com.tastyhouse.domain.exception.ErrorCode`로 표현하기 위해서라고 적혀 있었다. 실측 결과 소스의 `com.tastyhouse.domain..` 참조는 0건이었다.

##### backend/security-module/AGENTS.md — Working In This Directory

- `domain` 의존의 유일한 이유는 `JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`가 401/403 응답을 조립할 때 쓰는 `com.tastyhouse.domain.exception.ErrorCode` 참조였다(그 밖의 도메인 타입 참조 0건).

#### 결정

표현 계층 전체가 domain을 컴파일 클래스패스에 두지 않는다. carve-out은 없다.

| 항목 | before | after |
|---|---|---|
| `api-common-module` 내부 의존 | `api project(':domain')` | `api project(':application-core')`(당시 `application`). `application`은 `domain`을 `implementation`으로만 가진다 |
| `security-module` / `security-core` | `security-module`은 domain 의존, `security-core`는 미사용 domain 의존 | `security-module`은 `application` 의존, `security-core`의 domain 의존 삭제 |
| presentation 컴파일 클래스패스의 `domain` | 있음(api 노출 경유) | 없음 |
| carve-out `domain.exception..` | 허용. 핸들러가 `BusinessException`·`ErrorCode`를 직접 다룸 | 소멸. 판정은 `application.shared.error.ErrorResponses#resolve`(→ `ErrorDescriptor`), 상수는 `ErrorContracts`(이후 [결정 0042](#decision-0042)로 `ApiErrorCode`) |
| `BusinessException` 처리 메서드 | 전용 `handleBusinessException` | 없음. `@ExceptionHandler(Exception.class)` 폴백이 먼저 `ErrorResponses.resolve(e)`를 호출하고, 값이 있으면 그 status·code·message로 `ProblemDetail`, 없으면 기존 500 |
| carve-out `domain.shared.page..` | 허용. 컨트롤러가 `PageResult`를 감쌈 | 소멸. `PageQuery`/`PageResult`를 `com.tastyhouse.application.shared.port.out.page`로 이동 |
| carve-out 도메인 enum | 허용 + accessor 3종 제한 | 소멸. `*Result`의 enum 필드를 `String`(상수명)으로 바꾸고, 표시 문구는 `{field}Description`/`{field}DisplayName` 컴포넌트로 싣는다. enum 카탈로그는 `CodeLabelResult` |
| 강등 위치 | api 모듈 Response(`result.type().name()`) | DAO 투영(`x.status.stringValue()`) 또는 Result를 만드는 QueryService |
| ArchUnit (api 3모듈) | `apiModuleShouldOnlyReadDomainEnums`·`domainEnum()`·`ALLOWED_DOMAIN_ENUM_ACCESSORS`·`domainBoundaryPredicatesShouldStillBite` | 전부 삭제. `apiModuleShouldBeDomainModelFree`·`controllersShouldBeDomainFree`가 carve-out 없이 전면 금지 |
| ArchUnit (api-common·security-module) | 없음 | `LayerRulesTest#shouldNotDependOnDomain` 신설 |
| `@SharedApp` 허용 대상 | 리스너만 | 리스너 또는 `..config..` + `@Configuration`. 짝 규칙 `sharedConfigsShouldOnlyDeclareUnmarkedBeans` 신설 |
| 기타 이동 | admin 이벤트 상세의 파일 URL 누락 판정이 표현 쪽 | `EventManagementQueryService`로 이동(응답 동일) |

#### 근거

- 전용 `BusinessException` 핸들러가 있는 한 표현 계층은 `com.tastyhouse.domain.exception`을 import해야 했다. 판정을 application의 정적 유틸로 옮겨야 마지막 고리가 끊긴다.
- `PageQuery`/`PageResult`는 domain 안에 사용처가 0건이었고, 소비자(persistence DAO 32곳·표현 계층)가 전부 application 쪽이라 domain이 소유할 이유가 없었다.
- enum carve-out은 "api 모듈이 도메인 로직을 수행하지 않는다"를 지키려고 accessor 목록과 짝 규칙, 술어 판별력 단정 테스트까지 필요로 했다. `*Result`가 `String`으로 도착하면 api 모듈이 도메인 enum을 호출할 일 자체가 없어지고, 지킬 술어도 사라진다.
- 컴파일 클래스패스에서 domain을 빼면 회귀는 컴파일부터 실패한다. ArchUnit 규칙은 누군가 의존 한 줄을 되돌리는 회귀를 잡는 2차 방어선으로 남는다.
- `@SharedApp` 확대: persistence `*DomainConfig`(4앱 공통)가 등록하던 빈을 application으로 옮기려면(덩어리 02/03a) 4앱 전부에 등록할 자리가 필요했다. 클래스에 마커를 직접 달면 앱 격리에 걸리고 스캔과 `@Bean`이 겹치므로, 마커 없는 POJO를 `@SharedApp @Configuration`의 `@Bean`으로만 등록하는 형태를 허용했다.

#### 결과·감수한 손실

- HTTP wire 계약은 바뀌지 않았다(변경 전/후 jar 응답 diff로 확인).
- `security-core`는 `domain` 의존을 `application`으로 바꾸지 않고 삭제만 했다. `application`이 이미 `security-core`를 의존해 순환이 되기 때문이며, 같은 이유로 `ErrorContracts`도 쓸 수 없었다(필터 단계 응답 조립은 `security-module` 소유). `security-module`은 `implementation project(':application-core')`로 `ErrorContracts`의 `authRequired()`·`accessDenied()`(`ErrorCode.AUTH_REQUIRED`·`ACCESS_DENIED`의 미러)를 썼다. 표현 계층이 쓰던 `ErrorContracts` 상수는 `rateLimit()`·`accessDenied()`·`authRequired()`였다.
- `api-common-module`의 `handleUnexpected`는 `resolve`가 값을 주면 `warn("BusinessException [{}]: {}")` 후 응답을 만든다. `shouldNotDependOnDomain`을 위해 `api-common-module`에 테스트 의존 `archunit-junit5`가 신설됐다.
- `*Result`의 enum 필드를 `String`으로 바꿀 때 record 컴포넌트 정규 순서는 바꾸지 않았고, `{field}Description`/`{field}DisplayName` 컴포넌트는 그 필드 바로 뒤에 추가했다. application이 다시 enum이 필요하면 `DayType.valueOf(...)`처럼 서비스가 승격한다. 덩어리 01 당시 라벨은 DAO가 `EnumLabelProjection.labelOf(enumPath, Enum::getDescription)`로 채웠으나, 이후 [결정 0028](infrastructure/jpa/AGENTS.md#decision-0028)(덩어리 03b)에서 `EnumLabelProjection`이 삭제되고 QueryService가 채우게 됐다.
- 함정: `Map<OrderMethod, ...>.get(dto.orderMethod())`처럼 `Object` 인자 API는 필드가 `String`이 된 뒤에도 컴파일되지만 항상 `null`을 돌려준다(검증 중 당시 `ShopQueryService`의 주문 방식 조회에서 실제 발견). `Map.get`/`containsKey`·`equals`·`Collection.contains`·AssertJ `isEqualTo`가 같은 부류이므로, Result의 문자열을 도메인 enum과 비교할 때는 먼저 `valueOf`로 승격한다.
- 예외 판정은 최상위 예외만 본다. cause를 추적하지 않으므로 기존 500은 그대로 유지된다.
- `domainBoundaryPredicatesShouldStillBite`는 대체 테스트 없이 삭제했다. carve-out 없는 전면 금지는 무력해질 여지가 없다고 보았다.
- api 모듈에서 도메인 enum `switch`는 이제 컴파일부터 불가능하다. "분기 판정은 application에서 한다"는 결론은 그대로다.
- 일부 대체: `ErrorContracts`는 [결정 0042](#decision-0042)가 api-common의 `ApiErrorCode`로 대체했다. `@SharedApp @Configuration` 허용은 [결정 0034](#decision-0034)와 [결정 0036](#decision-0036)을 거쳐 사라졌다(마커 자체가 없다). 표현 계층의 domain 절단은 현재도 유효하다.

<a id="decision-0029"></a>

### 0029. 경고를 @SuppressWarnings로 가리지 않는다(XxxCodes 복제 상수 삭제·SensitiveFieldMasker 배선)

- 상태: 채택
- 결정 커밋: `d555b0325` (2026-09-27)
- 대체한 결정: [결정 0028](infrastructure/jpa/AGENTS.md#decision-0028)(일부 — `XxxCodes` 복제 상수와 그 일치 검사)
- 원문: 삭제 전 git 이력의 `docs/adr/0029-no-suppress-warnings.md`

#### 맥락

`@SuppressWarnings`는 IDE·컴파일러 경고(미사용·타입 불안전 등)를 끄는 어노테이션입니다. 경고를 끄면 경고가 가리키던 사실이 코드에서 보이지 않게 되고, 나중에 그 상태가 바뀌어도 아무도 알아채지 못합니다. 이 저장소에서 실제로 이런 일이 있었습니다.

- **`XxxCodes` 복제 상수**: 덩어리 03b([결정 0028](infrastructure/jpa/AGENTS.md#decision-0028))에서 persistence가 도메인 enum을 모르게 하려고, `application/<ctx>/port/out/XxxCodes`에 도메인 enum을 문자열 상수로 복제한 클래스 13개를 두었습니다. DAO가 쓰지 않는 상수마다 미사용 경고가 나서 `@SuppressWarnings("unused")`가 붙었습니다. 복제본은 도메인 enum과 두 벌이라 일치 검사(`codesMatchDomainEnums`)도 따로 필요했습니다.
- **`SensitiveFieldMasker` 미배선**: 로그의 민감 필드(비밀번호·토큰)를 가리는 클래스가 어디에도 주입되지 않아 미사용 경고가 억제돼 있었습니다. 즉 마스킹이 실제로는 전혀 동작하지 않았고, `[BODY]` 로그에 민감 값이 그대로 찍혔습니다.
- `ProductScheduler`는 호출부 없는 스케줄러라 억제 2개가 붙어 있었고, 테스트에는 `unchecked`·`SameParameterValue` 억제가 있었습니다.

#### 이전 규칙

##### backend/application-core/AGENTS.md — 덩어리 03b의 enum 비교값 전달 규칙

persistence가 비교할 enum 상수는 `<ctx>/port/out/XxxCodes`(도메인 enum을 복제한 문자열 상수 클래스)에서 가져온다. DAO 리터럴 `"COMPLETED"`는 도메인 enum 상수가 개명·삭제돼도 컴파일되고 조회가 조용히 0건이 되므로, `XxxCodes` 복제본을 거친다. 스캔 기반 가드 `codesMatchDomainEnums`가 복제본과 도메인 enum의 일치를 검사한다.

##### backend/CLAUDE.md — 도메인 enum 경계 규칙

DAO가 enum 상수와 비교할 때는 `application/<ctx>/port/out/XxxCodes` 문자열 상수를 쓴다(예: `.eq(OrderStatusCodes.COMPLETED)`).

##### backend/logging-module/AGENTS.md — 규칙

`SensitiveFieldMasker`는 미배선 상태라 마스킹이 실제로는 동작하지 않는다. 마스킹이 실사용에 연결되지 않은 지금은 `SENSITIVE_FIELDS` 목록만 갱신해도 즉시 효과가 없다. 마스킹을 실제로 적용하려면 `ApiLoggingAspect` 활성화가 먼저 필요하다.

##### backend/logging-module/AGENTS.md — 패키지 구조

`SensitiveFieldMasker`는 어디서도 주입·호출되지 않았고 클래스에 `@SuppressWarnings("unused")`가 붙어 있었다. `[BODY]` 로그는 record `toString()` 형식(`LoginRequest[username=a, password=b]`)으로 찍혔고, 마스킹 로직은 배열 원소 안의 객체로 재귀하지 않아 배열 원소의 민감 필드가 그대로 남았다.

##### backend/infrastructure/jpa/AGENTS.md — getter 없는 필드를 "미사용"이라며 지우지 않는다

getter 없는 필드에 `@SuppressWarnings("unused")`가 붙어 있는 자리도 같은 이유로 남기며, 그 어노테이션을 떼지 않는다.

#### 결정

**IDE·컴파일러 경고는 `@SuppressWarnings`로 가리지 않고 원인을 고칩니다.** 새 코드에는 붙이지 않고, 기존 코드를 고칠 때도 되살리지 않습니다. 억제 말고는 방법이 없을 때만 쓰고, 그 이유는 소유 모듈 `AGENTS.md` 봉인 항목에 적습니다(주석 금지 규칙 때문).

| 경고 | before (억제) | after (원인 해소) |
|---|---|---|
| 미사용 상수 (`unused`) | `XxxCodes` 복제본 13개에 억제 + `codesMatchDomainEnums` 일치 검사 | 복제본 13개 삭제. application이 도메인 enum `name()`을 포트 인자로 넘김. 쿼리 모양이 바뀌는 분기는 스펙 record(`ReviewSortSpec`·`ShopReviewTabFilter`·`ProductExposureWindow`), 고정값은 `ShopDeliveryTipRangePolicy` |
| 옵션 유형 리터럴 | DAO의 `"NORMAL"`/`"COMMON"` 리터럴 4곳 | `ProductAvailabilityQueryPort#findProductOptionAvailability(condition, normalOptionType, commonOptionType)` 인자 |
| 복제본 가드 | `EnumCodeConstantsTest`(일치 검사) | 복제본 금지 가드 2종(상수 집합·문자열 상수) — `portOutShouldNotMirrorDomainEnums` |
| 호출부 없는 스케줄러 | `ProductScheduler`에 억제 2개 | `@Scheduled(cron = "${product.option-crawl.cron:-}")`. 기본값 `-`는 비활성 cron이라 동작은 그대로 |
| 주입되지 않은 빈 | `SensitiveFieldMasker`에 억제(마스킹 미동작) | `ApiLoggingAspect`에 주입해 `[BODY]` 로그를 마스킹(**동작 변경**) |
| 제네릭 캡처 (`unchecked`) | `ArgumentCaptor.forClass(List.class)` + 억제 | `ArgumentCaptor.captor()`(Mockito 5.x) |
| 항상 같은 인자 (`SameParameterValue`) | 테스트 fake 헬퍼에 억제 | 파라미터를 없애고 헬퍼 이름에 의미를 담음(`registerInGangnam(dongName)`) |

#### 근거

- 억제 어노테이션은 경고가 가리키던 사실(미사용·미배선·타입 불안전)을 지워 버립니다. 마스킹 미배선이 그 실례였습니다. 억제가 붙어 있는 동안 민감 필드가 로그에 그대로 찍혔습니다.
- `XxxCodes` 복제본은 도메인 enum과 두 벌이라 일치 검사가 따로 필요했습니다. 게다가 "판매 완료 주문만 센다"·"탈퇴 회원 제외" 같은 **도메인 정책이 persistence에 박혀** 있었습니다. 비교값을 application이 도메인 enum `name()`으로 넘기면 상수가 개명·삭제될 때 **application 호출부가 컴파일 에러**를 내므로, 리터럴의 "조용한 0건" 위험도 함께 막힙니다.

#### 결과·감수한 손실

- persistence가 enum 어휘를 전혀 모르게 됐습니다. HTTP 응답과 저장값은 바뀌지 않았습니다(인기상품 기준 시각을 한 번만 읽도록 통일).
- `[BODY]` 로그 형식이 record `toString()`에서 마스킹된 JSON(`{"username":"a","password":"***"}`)으로 바뀌었습니다(동작 변경 — 로그 내용만 바뀌고 요청 처리는 불변). `ApiLoggingAspect#logControllerExecution`이 각 바디를 `sensitiveFieldMasker.mask(...)`로 변환하며, 배열 원소도 재귀해 마스킹합니다. 직렬화에 실패하면 요청은 막지 않고 바디 자리에 `[직렬화 실패]`를 남깁니다(로깅이 요청을 막지 않는다는 불변 유지). 이제 `SENSITIVE_FIELDS` 목록 갱신이 즉시 반영되며, 마스킹 규칙은 `SensitiveFieldMaskerTest`가 검사합니다.
- 함께 고친 결함이 있습니다. 저장 링이 비면 NPE 대신 빈 목록을 돌려줍니다.
- 규칙 도입 시점에 정리하지 못한 억제 9곳(main 4곳·테스트 5곳)이 남아 있습니다. 일부는 각 모듈 `AGENTS.md` 봉인 항목에 근거가 있습니다.
- 이후 [결정 0031](infrastructure/jpa/AGENTS.md#decision-0031)로 쓰기 어댑터(`XxxPersistenceAdapter`)는 도메인 enum을 다시 참조할 수 있게 됐습니다. 쓰기 어댑터는 `XxxStatus.X.name()`으로 비교값을 만들고(리터럴 금지는 유지), 조회 DAO는 여전히 포트 인자로 받습니다.

<a id="decision-0030"></a>

### 0030. import 순서를 클린 아키텍처 원 기준으로 재정의한다(domain 접두어는 전부 1순위)

- 상태: 채택
- 결정 커밋: `1e3818c5b` (2026-09-27)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0030-import-order-clean-architecture.md`

#### 맥락

이 저장소는 Spring 공식 import 그룹 순서(`java` → `javax` → 서드파티 → 자사 → static) 위에, **자사 그룹(`com.tastyhouse.*`) 내부를 계층 순으로 정렬하는 커스텀 규칙**을 얹어 씁니다. 안정 의존성 원칙(의존은 안쪽 domain을 향한다)에 따라 가장 안정적인 코드를 맨 위에 둡니다.

그런데 덩어리 03a/03b로 포트·서비스·리스너가 옮겨진 뒤에도 기존 분류표는 예전 구조에 맞춰져 있었습니다.

- `domain.shared`·`domain.exception`이 도메인 모델보다 아래(4순위 "external/shared")에 분류돼 있었습니다.
- 1순위 매칭에 하위 패키지(`.repository`·`.service`·`.port`)가 나열돼 있었습니다.
- 이미 삭제된 `.converter`가 남아 있었습니다.
- `apicommon` 세그먼트가 표에 없었습니다.
- "4개 그룹"이라는 표기와 실제 5개 그룹이 모순됐습니다.

기존 코드 1,101개 파일이 규칙 위반 상태였습니다.

#### 이전 규칙

##### backend/CLAUDE.md — 코딩 스타일 (import 순서) / 자사 코드 그룹 내부 계층 정렬 (헥사고날 안→밖: domain이 위)

자사 그룹 내부를 `(계층 순위, 알파벳)` 복합 키로 정렬했다.

| 순위 | 계층 | 매칭 패키지 |
|---|---|---|
| 1 | domain | `com.tastyhouse.domain.<ctx>.model` / `.vo` / `.event` / `.repository` / `.service` / `.port` |
| 2 | application | `com.tastyhouse.application.*` |
| 3 | infrastructure | `com.tastyhouse.infrastructure.<ctx>.query` / `.persistence`(`.converter`) |
| 4 | external / shared | `com.tastyhouse.external.*`, `security.*`, `logging.*`, **`domain.shared.*`, `domain.exception.*`** |
| 5 | presentation | `webapi.*` · `adminapi.*` · `ceoapi.*` · `batch.*` |

`com.tastyhouse.external.*`는 모듈이 바뀌어도 패키지가 불변이라 4순위를 유지했고, 리네임된 코어 `com.tastyhouse.restclient.*`도 4순위에 추가했다. 문서 서두는 import를 "4개 그룹"으로 나눈다고 적었지만 실제로는 static까지 5개를 나열했다.

#### 결정

자사 import 순위를 클린 아키텍처 원(Domain 중심 → Application → 바깥 원) 기준 5단계로 재정의합니다. **판별은 `com.tastyhouse.` 바로 다음 최상위 세그먼트 하나로만** 합니다.

| 항목 | before | after |
|---|---|---|
| 판별 기준 | 최상위 + 하위 패키지(`.model`·`.repository`·`.port` 등) 혼용 | `com.tastyhouse.` 다음 최상위 세그먼트 하나 |
| `domain.shared`·`domain.exception` | 4순위(external/shared) | 1순위(접두어가 `domain`이면 전부) |
| 3순위 | `infrastructure`(`.converter` 포함) | driven 어댑터 `infrastructure`(+ 당시 `external`·`restclient`) |
| 4순위 | external·security·logging·domain.shared·domain.exception | 공유 횡단 모듈 `apicommon`·`logging`·`security`(+ 테스트 전용 세그먼트) |
| 5순위 | presentation | driving 어댑터 `adminapi`·`batch`·`ceoapi`·`webapi`(5-a 공용 → 5-b 도메인 전용) |
| 그룹 수 표기 | "4개 그룹"(실제 5개 나열) | 5개 그룹 |
| 모듈별 등장 가능 순위 | 없음 | 표로 명시(어긋나면 레이어 위반 신호) |
| 기존 위반 파일 | 서술 없음 | 1,101개 위반을 명시하고 옆 파일을 근거로 삼지 않게 함 |

#### 근거

- 판별을 최상위 세그먼트 하나로 고정하면 파일마다 사람이 판단할 여지가 사라집니다. 클래스명 접미어나 하위 패키지를 보지 않습니다.
- 03a/03b로 domain 하위 구조가 바뀌어 하위 패키지 나열이 낡았습니다. `domain.exception`을 도메인 모델보다 아래에 두던 분류는 "접두어가 domain이면 안쪽 원"이라는 원칙과 맞지 않았습니다.
- 모듈 의존 방향 때문에 모듈마다 등장할 수 있는 순위가 정해져 있으므로, 순위가 맞지 않는 import는 순서 문제가 아니라 레이어 위반일 가능성이 큽니다.

#### 결과·감수한 손실

- 이 커밋은 문서만 바꿨습니다. 후속 `f5122d837`(2026-09-27)이 위반 파일 1,101개의 import 블록만 재정렬하고, `domain` 테스트에 `ImportOrderConventionTest` 가드를 신설해 `./gradlew build`에서 강제했습니다. 순위 표에 없는 세그먼트는 실패시키고, 스캔 파일 수가 3,000개를 넘는지도 확인합니다.
- import만 바꾼 변경은 바이트코드가 같아 `:domain:test`가 UP-TO-DATE로 건너뛰어집니다. 그래서 `domain/build.gradle`에 backend `*.java`를 테스트 입력으로 선언했습니다.
- 이 절 제목은 `ImportOrderConventionTest` 메시지가 가리키므로 제목과 순위 표 구조를 유지합니다. 순위 표는 `domain/AGENTS.md` 봉인 목록의 동기화 규칙에 따라 3곳이 맞아야 합니다.
- 이후 [결정 0037](#decision-0037)로 `external`·`restclient` 세그먼트가 사라져 3순위는 `infrastructure` 하나가 됐습니다. [결정 0031](infrastructure/jpa/AGENTS.md#decision-0031)로 `infrastructure:jpa`의 쓰기 어댑터에는 1순위 import가 다시 등장할 수 있게 됐습니다.

<a id="decision-0034"></a>

### 0034. application *ServiceConfig를 지우고 도메인 서비스 클래스에 앱 마커만 단다

- 상태: 대체됨(→ [결정 0036](#decision-0036))
- 결정 커밋: `251c74c06` (2026-10-02)
- 대체한 결정: [결정 0026](application-core/AGENTS.md#decision-0026), [결정 0027](application-core/AGENTS.md#decision-0027)
- 원문: 삭제 전 git 이력의 `docs/adr/0034-service-config-removal.md`

`<ctx>/config/*ServiceConfig` 22개와 `payment/config/PgRouterConfig`를 전부 삭제합니다. 그 설정들이 만들던 도메인 서비스 78개는 **클래스에 앱 마커 하나만** 달고(`@Service`는 달지 않음) 마커 기반 컴포넌트 스캔으로 등록합니다.

```java
// before — application/bug/config/BugServiceConfig.java (삭제됨)
@Configuration(proxyBeanMethods = false)
@SharedApp
public class BugServiceConfig {
    @Bean
    public BugReportRegistrationService bugReportRegistrationService(
        BugReportPersistencePort bugReportPersistencePort,
        BugReportImagePersistencePort bugReportImagePersistencePort
    ) {
        return new BugReportRegistrationService(bugReportPersistencePort, bugReportImagePersistencePort);
    }
}
```

```java
// after — config 없음
@WebApp
public class BugReportRegistrationService { ... }
```

| 항목 | before | after |
|---|---|---|
| 등록 config | `*ServiceConfig` 22개 + `PgRouterConfig` + `SharedEventConfig`(`@Bean` 88개) | `shared/config/SharedBeanConfig` 1개(`@Bean` 10개, `SharedEventConfig`를 리네임) |
| 도메인 서비스 78개 | 마커 없는 POJO, config가 `new` | 클래스에 앱 마커만, 스캔 등록. 빈 이름 = 옛 `@Bean` 메서드명(실측 전부 일치) |
| 마커 분포 | `@SharedApp` config 20개 + `SharedEventConfig`, `@WebApp` config 3개 | `@WebApp` 17 · `@AdminApp` 3 · `@CeoApp` 21 · `@BatchApp` 2 · `@SharedApp` 35 |
| 마커 배정 기준 | "언젠가 다른 앱이 트리거해도 안전하도록" 미리 `@SharedApp` | **현재 소비 앱 집합** — 한 앱이면 그 앱 마커, 둘 이상이거나 `@SharedApp` 빈이 쓰면 `@SharedApp` |
| 한 앱 전용 빈 | 4앱 전부에 뜸 | 소비 앱에서만 뜸 |
| 애노테이션을 달 수 없는 빈 | 각 config | `SharedBeanConfig`에 잔류 — `domainEventPublisher`, domain 계산기·정책 7개, `shopDeliveryTipRangePolicy`, `prohibitedWordValidator` |

**마커 배정의 고정점**: `@SharedApp` 빈이 의존하는 빈은 전부 `@SharedApp`이거나 `SharedBeanConfig`의 마커 없는 빈이어야 합니다. 이 제약을 반복 적용해 고정점까지 올립니다. 예를 들어 `ReviewBlindRequestService`가 Shared이면 `ReviewLifecycleService`도 Shared입니다. `PaymentConfirmationService`는 `@SharedApp` 리스너 `PaymentEventListener`가 쓰므로 `@SharedApp`으로 남았고, `PaymentCancellationService`는 소비자가 web뿐이라 `@WebApp`으로 좁혀졌습니다.

ArchUnit 규칙도 바꿨습니다.

| 규칙 | before | after |
|---|---|---|
| `LayerRulesTest#sharedAppOnlyOnListeners` | 리스너 ∨ `..config..`의 `@Configuration` | 위 두 경우 ∨ 마커-only 도메인 서비스 |
| `LayerRulesTest#markerOnlyClassesShouldBeDomainServices` | 없음 | 신설 — 마커-only 클래스는 도메인 서비스 위치에만 둔다(하한 78) |
| `AppIsolationTest#appsShouldNotDependOnEachOther` | 마커 조합 전부 금지 | 앱 마커 → `@SharedApp` `..service..`만 허용(공유 커널 단방향) |
| `AppIsolationTest#constructorDependenciesShouldBeVisibleToApp` | 없음 | 신설 — 생성자 의존이 같은 앱 또는 `@SharedApp`에서 보이는지 검사(마커 누락·오배정을 둘 다 잡음) |
| `AppIsolationTest#appRestrictedPortDependentsShouldBelongToThatApp` | 없음 | 신설 — web 전용 포트(`MailSenderPort`·`SmsSenderPort`·`PgProviderGatewayPort`·`SocialOAuthClientPort`)는 `@WebApp`, batch 전용 포트(`BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort`)는 `@BatchApp`만 받는다 |
| `ServiceContextBoundaryTest` | 스테레오타입 없는 `..service..` POJO | 변경 없음 — 마커는 스테레오타입이 아니므로 계속 검사 대상 |
| `AppIsolationTest#sharedBeansShouldNotDependOnWebOnlyServices` | 이름 목록 4개 | 유지(중복 방어) |
| `AppIsolationTest#beansShouldHaveExactlyOneAppMarker` | `@Service`/`@Component`만 | 마커-only 도메인 서비스 포함(마커 2개 부착을 잡는다) |
| `AppIsolationTest#markerBeanCounts` | `@WebApp` ≥60 · `@AdminApp` ≥55 · `@CeoApp` ≥95 · `@BatchApp` ≥12 · `@SharedApp` ≥12 | 마커-only 서비스 포함, `@WebApp` ≥83 · `@AdminApp` ≥65 · `@CeoApp` ≥122 · `@BatchApp` ≥15 · `@SharedApp` ≥47 |
| `LayerRulesTest#sharedConfigsShouldOnlyDeclareUnmarkedBeans` | `@SharedApp` config 21개(`*ServiceConfig` 20 + `SharedEventConfig`) 대상 | `SharedBeanConfig` 1개 대상으로 계속 통과 |

규칙 세부(원문 기록):

- `sharedAppOnlyOnListeners`의 "마커-only 도메인 서비스"는 `..service..`에 있고, 이름이 `*CommandService`/`*QueryService`가 아니며, `port.in` UseCase 구현체가 아닌 클래스입니다. `markerOnlyClassesShouldBeDomainServices`는 리스너가 `@Component`, 설정이 `@Configuration`을 함께 단다는 전제로 마커-only 클래스의 위치를 강제했습니다.
- `appsShouldNotDependOnEachOther`의 before는 마커 5×4=20조합 전부 금지였습니다. after에도 앱 ↔ 앱, `@SharedApp` → 앱, 앱 → `@SharedApp` 리스너·설정은 계속 금지했습니다.
- `constructorDependenciesShouldBeVisibleToApp`: 마커 M 클래스(스테레오타입 유무 무관)의 생성자 파라미터 타입 T(제네릭 인자 포함)가 `com.tastyhouse.{application,domain}..`이면 (i) M/`@SharedApp` `@Configuration`의 `@Bean` 반환 타입이거나, (ii) 구체 클래스로서 M/`@SharedApp` 마커를 갖거나, (iii) 인터페이스로서 application 안 구현체 중 하나가 M/`@SharedApp`이거나, (iv) application 안에 구현체가 없어야(persistence·벤더 구현) 했습니다. 검사 의존 수 하한은 700이었습니다. 후속 보강으로 마커 `@Configuration`의 `@Bean` 메서드 파라미터도 그 설정의 마커 기준으로 같은 판정을 받게 했고(하한 ≥ 3), 컬렉션이 아닌 인터페이스 의존의 주입 후보가 2개 이상이면 모호로 실패시켰으며, 구현체 집계에서 데코레이터(그 인터페이스를 생성자로 받는 클래스)·abstract를 뺐습니다.
- 마커 분포 합계는 `@WebApp` 17 · `@AdminApp` 3 · `@CeoApp` 21 · `@BatchApp` 2 · `@SharedApp` 35 = 78이었습니다. `SharedBeanConfig`는 `SharedEventConfig`에서 `git mv`로 리네임했습니다. 잔류 `@Bean`의 domain 계산기·정책 7개는 `productExposureCalculator`·`cupDepositPolicy`·`storePriceBadgePolicy`·`shopOperatingStatusCalculator`·`shopDeliveryTipCalculator`·`shopNextOpenTimeCalculator`·`scheduledOrderSlotCalculator`였고, `domainEventPublisher`는 인터페이스 타입으로, `shopDeliveryTipRangePolicy`는 상수·람다로 조립하는 record로, `prohibitedWordValidator`는 `CachingProhibitedWordPersistencePort`로 감싸 조립했습니다.
- 고정점 예시에는 `ShopLifecycleService`(Shared) → `ShopImageApprovalService`·`ShopCeoAssignmentRecorder`도 Shared가 있었습니다. 용어상 앱 오케스트레이터는 `*CommandService`/`*QueryService`(batch는 `*SchedulerService`)로 `@Service` + 앱 마커를 달고 UseCase를 구현하며 트랜잭션 경계를 가졌고, "소비 앱 집합"은 생성자 주입을 따라 올라가 최종적으로 그 빈을 쓰는 앱의 집합이었습니다.

<a id="decision-0035"></a>

### 0035. application *ApplicationConfig를 지우고 스캔 설정을 각 앱 부트스트랩의 중첩 설정으로 옮긴다

- 상태: 채택
- 결정 커밋: `deae9a8e0` (2026-10-03)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0035-application-config-into-bootstrap.md`

#### 맥락

- **컴포넌트 스캔(`@ComponentScan`)**: "이 패키지 아래에서 빈이 될 클래스를 찾아라"는 Spring 지시.
- **마커 스캔**: 스캔에 "특정 마커 애노테이션(`@WebApp` 등)이 붙은 클래스만"이라는 include 필터를 건 것. 당시 4개 앱이 `application` 모듈 하나를 공유해서, 앱마다 뜰 빈을 마커로 갈랐다.
- **컴포지션 루트**: 앱을 조립하는 단 한 곳. "어떤 부품을 이 앱에 붙일지"는 실행 앱 모듈(`web-api`·`admin-api`·`ceo-api`·`batch-module`)만 안다는 저장소 규칙이 있다.

공유 모듈 `application` 안에 앱마다 하나씩 `WebApplicationConfig`·`AdminApplicationConfig`·`CeoApplicationConfig`·`BatchApplicationConfig`가 있었다. 마커 하나만 빼고 모양이 같았고, 각 앱이 `@Import(XxxApplicationConfig.class)`로 가져왔다. 공유 모듈이 "어느 앱이 어떤 마커를 싣는가"라는 앱 조립 지식을 갖고 있었다.

#### 이전 규칙

##### backend/CLAUDE.md — 모듈 등록 컨벤션

- 앱은 `{App}ApplicationConfig` 하나만 `@Import`했고, 그 클래스가 `application` 모듈 안에 있었다.
- `application`이 auto-configuration을 갖지 않는 예외였던 사유는 "`{App}ApplicationConfig`가 `@Import`되는 대상이라 자기 등록 대상이 아니다"였다.

##### backend/CLAUDE.md — 앱 마커 규칙 / 앱이 가질 수 있는 조립 코드의 상한 / 비채택 대안과 재고 조건

- 마커 include 필터를 건 `@ComponentScan`이 `application` 모듈의 `{Web,Admin,Ceo,Batch}ApplicationConfig`에 있었고 앱이 `@Import`했다.
- 앱 조립 코드의 상한은 `@Import({App}ApplicationConfig)` 한 줄(+ yml)이었다.
- 부트스트랩 모듈 분리를 채택하지 않은 이유를 "`@SpringBootApplication` + `@Import` 두 줄뿐"이라고 표현했다.

##### backend/AGENTS.md — Module Dependency Graph (그래프 아래 모듈 등록 항목)

- 챕터 02 auto-configuration 전환 뒤 앱의 `@Import`는 그 앱 정체성인 `{App}ApplicationConfig` 하나만 남았고, 그 클래스가 `application` 모듈에 있다고 적었다.

##### backend/application-core/AGENTS.md — 챕터 03 빈 배선

- 4개 `*ApplicationConfig`의 include 필터가 앱별 스캔 범위를 정했다.

#### 결정

| 항목 | before | after |
|---|---|---|
| 앱의 `@Import` | `@Import({App}ApplicationConfig.class)` 1줄 | 없음(0줄) |
| 스캔 `@ComponentScan`의 위치 | `application` 모듈의 `{Web,Admin,Ceo,Batch}ApplicationConfig` 4개 | 각 앱 부트스트랩 안의 `static class ApplicationLayerScanConfig` 4개 |
| `application` 모듈이 아는 것 | 앱별 마커 조합(앱 조립 지식) | 없음(당시에는 마커 애노테이션만 제공) |
| 앱 조립 코드 상한 | `@Import` 1줄 + yml | 부트스트랩 중첩 `ApplicationLayerScanConfig` 1개 + yml |
| 회귀 가드 | 없음 | 앱별 `ApplicationLayerScanConfigTest` 4개 + application `LayerRulesTest#applicationShouldNotDeclareComponentScan`. 단정은 application testFixtures `ApplicationLayerScanAssertions`로 공유 |
| 동작 | — | 변경 없음(스캔 대상·필터·빈 집합 동일) |

#### 근거

- 앱 조립 지식은 컴포지션 루트 규칙상 실행 앱 모듈의 것이다. 공유 모듈이 그것을 갖는 상태를 없앤다.
- **부트스트랩에 `@ComponentScan`을 직접 달지 않고 static 중첩 클래스로 둔 이유(Spring 6.1 함정)**: Spring Framework 6.1.5 `ConfigurationClassParser`는 설정 클래스에 직접 붙은 `@ComponentScan`을 먼저 모으고, 하나라도 있으면 메타 애노테이션(`@SpringBootApplication` 안의 `@ComponentScan`)을 무시한다. 직접 달면 앱 자기 패키지 스캔과 Boot의 `TypeExcludeFilter`·`AutoConfigurationExcludeFilter`가 조용히 사라진다. static 중첩 `@Configuration`은 별도 설정 클래스로 처리되어 이 문제가 없다.

#### 결과·감수한 손실

- 4앱 변경 전후 싱글톤 빈 집합이 설정 클래스 이름 1건을 빼고 동일함을 기동 로그 diff로 확인했다.
- 기동 시 `com.tastyhouse.application` 클래스패스 스캔이 2회 돈다(중첩 클래스 처리 경로 + 앱 기본 스캔). 두 번째는 `ClassPathBeanDefinitionScanner.isCompatible`로 건너뛰어 빈은 중복되지 않는다. 기동 비용만 미세하게 늘었다.
- 설정 클래스 자신의 빈 이름이 `webApiApplication.ApplicationLayerScanConfig`처럼 바뀌었다(주입하는 곳 없음).
- `AppOwnership`을 `com.tastyhouse.architecture` 패키지로 옮겼다.
- 이후 [결정 0036](#decision-0036)이 마커를 없애면서 `ApplicationLayerScanConfig`는 include 필터 없는 `@ComponentScan(basePackages = "com.tastyhouse.application")`가 됐다. "부트스트랩의 static 중첩 설정 클래스가 스캔을 소유한다"는 형태는 현재 규칙으로 남았다.

<a id="decision-0036"></a>

### 0036. 앱 마커 애노테이션을 제거하고 앱 소속을 Gradle 모듈(core + {앱}-application 4개)로 표현한다

- 상태: 채택(PG SPI 포트·라우터 배치 → [결정 0061](../docs/domain/payment.md#decision-0061))
- 결정 커밋: `d4688a682` (2026-10-04). 후속 `406c34ebe`(같은 날, `application-module.properties` 표식 제거 → `ModuleOrigin` 판정)도 이 결정에 포함합니다.
- 대체한 결정: [결정 0013](#decision-0013), [결정 0014](#decision-0014), [결정 0034](#decision-0034). [결정 0024](application-core/AGENTS.md#decision-0024)·[결정 0025](#decision-0025)의 `@SharedApp` 부분도 대체합니다.
- 원문: 삭제 전 git 이력의 `docs/adr/0036-app-marker-removal.md`

#### 맥락

이 결정 전까지 application 계층은 **모듈 하나**였습니다. 챕터 01이 앱별 모듈 4개를 합쳤고([결정 0013](#decision-0013)), 챕터 03이 패키지를 `com.tastyhouse.application` 하나로 평탄화했습니다([결정 0014](#decision-0014)). 이 상태에서는 패키지만 보고 클래스가 어느 앱 것인지 알 수 없었습니다. 그래서 **앱 마커**가 앱 소속을 표현했습니다.

- **앱 마커**: `com.tastyhouse.application.shared.marker`의 `@WebApp`/`@AdminApp`/`@CeoApp`/`@BatchApp`과, "앱 소속 없음 = 4앱 전부"를 뜻하는 `@SharedApp`입니다.
- 각 앱 부트스트랩의 스캔 설정은 `useDefaultFilters = false` + `includeFilters = ANNOTATION {XxxApp, SharedApp}`로 자기 마커만 골라 빈으로 올렸습니다.

마커는 **컴파일러가 모르는 약속**이었습니다. 잘못 달거나 빠뜨려도 빌드는 통과했습니다. 마커 없는 `@Service`는 어느 앱에도 뜨지 않다가, 그 빈이 처음 필요한 기동 시점에 `NoSuchBeanDefinitionException`으로 드러났습니다. 이 구멍을 `AppIsolationTest`(파일 전체), `beansShouldHaveExactlyOneAppMarker`, `adaptersShouldOnlyUseOwnAppUseCases` 같은 ArchUnit 규칙 여러 개로 메웠습니다.

#### 이전 규칙

##### backend/application-core/AGENTS.md — application(모듈 소개)·챕터 03

- 4개 앱의 application 계층을 담는 단일 모듈이었습니다. 인바운드 포트와 그 구현, 읽기 계약 326개, 도메인 이벤트 리스너 12종이 한 패키지 트리 안에 함께 있었고, 앱 소속은 마커가 표현했습니다.
- `shared/marker/{WebApp,AdminApp,CeoApp,BatchApp}`은 "앱 소속의 유일한 단서"였고, `SharedApp`은 리스너와 공유 `@Configuration`용 5번째 마커였습니다.
- 패키지 트리에서 `port/in/`의 UseCase 인터페이스는 마커를 달았고, Command record는 마커 없이 소속을 testFixtures `AppOwnership`이 유도했습니다. `shared/config/SharedBeanConfig`(구 `SharedEventConfig`)는 `@SharedApp`이었고, `listener/`의 리스너는 `@Component @SharedApp` + `@TransactionalEventListener(AFTER_COMMIT)`였습니다.
- "`com.tastyhouse.application`을 이 모듈이 단독 소유한다 — split package가 끝났다"고 적었습니다. 공유 계약을 `domain`에 두던 시기의 가드 3종(`ReadContractSingleOwnerTest`·`ReadContractPurityTest`·`RuleAnchorTest`의 소유 모듈 필터)이 챕터 04로 사라졌다는 기록입니다. 이 결정으로 5모듈 split package가 다시 생겼습니다.

##### backend/application-core/AGENTS.md — ArchUnit 클래스 구성(마커 판정 시기)

- 챕터 03 이후 `AppIsolationTest`는 슬라이스/패키지 술어에서 마커 애노테이션 술어로 전면 재작성된 클래스였습니다. 내용은 `appsShouldNotDependOnEachOther`(마커 5종 5×4=20조합 개별 검사. `@SharedApp`이 `AppOwnership.MARKERS`에 들어가 "공유 리스너는 앱 전용 빈에 의존할 수 없고, 앱 전용 빈도 공유 리스너에 의존할 수 없다"까지 강제), `beansShouldHaveExactlyOneAppMarker`(리스너는 `@SharedApp` 하나로 통과), `useCasesShouldHaveExactlyOneAppMarker`, `commandRecordsShouldBelongToExactlyOneApp`(`AppOwnership` 유도), `markerBeanCounts`·`markerUseCaseCounts`, 그리고 덩어리 02/03a 신설 `sharedBeansShouldNotDependOnWebOnlyServices`(`@SharedApp` 빈은 `MailVerificationService`·`SmsVerificationService`·`PgPaymentGatewayRouter`·`PgPaymentGatewayPort`를 의존하지 않는다)였습니다.
- `LayerRulesTest`의 공유 마커 규칙은 `listenersShouldBeShared`(`@TransactionalEventListener` 메서드를 가진 클래스는 `@SharedApp`이 붙고 `..listener..`에 있어야 한다 — 마커 누락 시 리스너가 어느 앱에도 뜨지 않아 이벤트가 조용히 유실된다), `sharedAppOnlyOnListeners`, `sharedConfigsShouldOnlyDeclareUnmarkedBeans`(`@SharedApp` 설정은 `@Component`/`@Service`를 겸하지 않고, `@Bean` 반환 타입과 그 설정이 생성자를 호출하는 클래스 전부가 앱 마커를 갖지 않아야 한다. 대상이 0개일 때 failOnEmptyShould를 피하려고 `should()`가 아니라 위반을 손으로 모으는 테스트로 썼다)였습니다. 귀결로 앱 전용 `@TransactionalEventListener`는 둘 수 없었습니다.
- 마커별 하한: `markerBeanCounts`는 처음 실측 web 66·admin 62·ceo 101·batch 13보다 낮은 `@WebApp` ≥60·`@AdminApp` ≥55·`@CeoApp` ≥95·`@BatchApp` ≥12와 리스너 12종인 `@SharedApp` ≥12였고, [결정 0034](#decision-0034) 후 `@WebApp` ≥83·`@AdminApp` ≥65·`@CeoApp` ≥122·`@BatchApp` ≥15·`@SharedApp` ≥47(리스너 12 + 공유 도메인 서비스 35)이었습니다. `markerUseCaseCounts`는 `@WebApp` ≥50·`@AdminApp` ≥100·`@CeoApp` ≥95·`@BatchApp` = 7(정확히 일치)이었습니다. 합계 하나로 두면 한 앱이 통째로 사라져도 나머지 세 앱이 하한을 떠받치므로 마커별로 유지했습니다.
- batch 선별은 마커 술어였습니다. `commandRecordsShouldBeBoundaryTyped`는 `.areNotAnnotatedWith(BatchApp.class)`로 batch를 제외했고(batch는 carve-out이 `domain.exception..` 하나뿐인 엄격판을 `BatchSchedulerRulesTest`에서 쓴다), `BatchSchedulerRulesTest`도 마커 술어로 batch만 골랐습니다.

##### backend/application-core/AGENTS.md — Dependencies — java-test-fixtures

- testFixtures에는 `ApplicationLayerScanAssertions`와 함께 `AppOwnership`이 있었습니다. `testFixturesApi`로 `archunit-junit5`를 노출한 이유는 `AppOwnership`이 마커 애노테이션(main)과 ArchUnit을 함께 보기 때문이었습니다.

##### backend/application-core/AGENTS.md — `DESERIALIZED_COMMANDS` — 고아 Command record 봉인

- `AppOwnership.DESERIALIZED_COMMANDS`는 유도가 닿지 않는 Command record의 봉인 목록이었고, 구성원은 `com.tastyhouse.application.shop.port.in.ShopStorePriceVerificationItemCommand`(`CeoApp`) 1개였습니다. 이 record는 multipart 문자열 파트로 들어와 서비스가 `ObjectMapper`로 역직렬화하므로 어느 UseCase 시그니처에도 등장하지 않습니다. "이 목록에 새 항목을 추가하지 않는다 — 추가 전에 그 record를 어디서 만드는지 먼저 찾는다"가 규칙이었습니다. 목록은 `AppOwnership`과 함께 삭제됐고, 지금은 그 record가 `ceo-application`에 있다는 사실이 소속입니다. 정적 참조가 없다고 죽은 코드로 지우면 안 된다는 점은 그대로입니다.

##### backend/application-core/AGENTS.md — 앱 클래스패스가 곧 스캔 포함 기준이다

- 새 빈과 새 UseCase 인터페이스는 반드시 마커 하나를 달고, Command record에는 붙이지 않았습니다(소속은 `AppOwnership` 유도).
- 마커는 `@Component` 메타를 얹지 않은 순수 마커로 유지했습니다. 얹으면 기존 `@Service`의 의미가 흐려지기 때문이었습니다.
- "application은 자기 등록하지 않는다"의 이유는 "4개 앱의 빈이 같은 jar에 있고 마커로만 갈려, 클래스패스 존재만으로 어느 앱인지 결정할 수 없다"였습니다. 이 결정 후 규칙은 유지되고 이유만 "스캔 범위는 앱 부트스트랩이 정한다"로 바뀌었습니다.
- 패키지 기반 include(`..listener..`를 통째로 포함)는 스캔 규칙이 "마커"와 "패키지" 두 가지로 갈리므로 채택하지 않았습니다.

##### backend/application-core/AGENTS.md — 리스너 배치·`ErrorResponses`

- 리스너는 과거 "모든 실행 모듈이 스캔하는 곳"이라는 근거로 `infrastructure:persistence`에 있었고, application으로 옮길 때 4앱 모두에 뜨게 하는 마커 `@SharedApp`을 신설해 같은 근거를 성립시켰습니다. 지금은 마커 없이 코어에 두며, 리스너별 근거는 "리스너를 앱 모듈에 두지 않는 이유"로 읽습니다(`listenersAndConfigsShouldResideInCore`).
- `ErrorResponses`를 빈이 아니라 정적 유틸로 둔 근거 중 하나는 "빈으로 만들면 앱 마커가 필요하고(`beansShouldHaveExactlyOneAppMarker`), 마커 스캔은 앱 부트스트랩이 하므로 앱 부트스트랩 없이 뜨는 컨텍스트(`ApiCommonAutoConfigurationTest`, imports 제거 후 `ratelimit/ApiCommonRateLimitConfigTest`)에서 빈을 찾지 못한다"였습니다.

##### backend/application-core/AGENTS.md — 빈 배선

- 스캔 필터가 마커 `ANNOTATION`이므로 **마커가 곧 스캔의 유일한 포함 기준**이었습니다. `@Service` 없이 마커만 단 클래스도 빈이 됐고, 도메인 서비스가 이 형태였습니다([결정 0034](#decision-0034)).
- 4개 스캔 설정 전부가 `SharedApp.class`를 포함했으므로, `@SharedApp` 빈(리스너 12종, `SharedBeanConfig`, 공유 도메인 서비스 35개)은 4앱 전부에 떴습니다.

##### backend/application-core/AGENTS.md — 도메인 서비스는 클래스에 앱 마커만 단다

- **도메인 서비스에 `@Service`를 달지 않습니다.** `ServiceContextBoundaryTest`가 스테레오타입 붙은 클래스를 대상에서 조용히 빼기 때문입니다. "마커만 = 도메인 서비스, `@Service` + 마커 = 앱 오케스트레이터"로 갈랐습니다.
- 가드는 `markerOnlyClassesShouldBeDomainServices`·`sharedAppOnlyOnListeners`·`sharedConfigsShouldOnlyDeclareUnmarkedBeans`, `AppIsolationTest`의 `constructorDependenciesShouldBeVisibleToApp`·`appRestrictedPortDependentsShouldBelongToThatApp`·`appsShouldNotDependOnEachOther`·`beansShouldHaveExactlyOneAppMarker`·`markerBeanCounts`였습니다.
- 새 도메인 서비스는 `@Bean`이 아니라 클래스에 소비 앱 마커만 달았습니다. 앱 전용 채널 포트를 받는 클래스는 그 앱 마커여야 했습니다(web 전용 `MailSenderPort`·`SmsSenderPort`·`PgProviderGatewayPort`·`SocialOAuthClientPort`는 `@WebApp`, batch 전용 `BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort`는 `@BatchApp`).
- `SharedBeanConfig`에 마커가 붙은 클래스를 `@Bean`으로 추가하면 스캔 빈과 이름이 겹쳐 기동이 실패했고, `sharedConfigsShouldOnlyDeclareUnmarkedBeans`는 이를 `@SharedApp` 설정에 한해 잡았습니다. 그래서 "`@WebApp` 등 앱 마커 설정을 새로 만들어 같은 일을 하면 이 겹침을 어떤 테스트도 잡지 못한다 — 앱 전용 config를 만들지 않는다"고 적었습니다.
- "`markerBeanCounts` 하한과 `markerOnlyClassesShouldBeDomainServices`의 하한 78을 낮추지 않는다 — 이관이 되돌려져 서비스가 마커를 잃으면 하한이 깨지는 것이 의도다"라는 규칙이 있었습니다.

##### backend/CLAUDE.md — 모듈 지도·패키지 최상위 지도

- 다이어그램은 "앱 소속은 패키지가 아니라 마커 애노테이션이 표현한다(리스너 전용 `@SharedApp` = 4앱 전부)", "ArchUnit이 막는다 — `AppIsolationTest` · `adaptersShouldOnlyUseOwnAppUseCases`"라고 적었습니다.
- `com.tastyhouse.application..`는 `application` 한 모듈이 단독 소유했습니다(챕터 04로 split package가 끝났다고 기록).
- 4앱의 `implementation`은 `:application` 하나였습니다.
- import 순서 예시(`NoticeCommandService`, 당시 이름)에는 `com.tastyhouse.application.shared.marker.AdminApp` import가 들어 있었습니다. 마커 삭제로 지금 코드에는 없고, 정렬 규칙 예시로서만 유효합니다.

##### backend/web-api·admin-api·ceo-api·batch-module AGENTS.md — Purpose / Internal

- 각 앱의 `application` (implementation) 항목은 "코어 + 마커 애노테이션 `XxxApp`·`SharedApp`(부트스트랩 중첩 `ApplicationLayerScanConfig`가 스캔 기준으로 사용. 과거에는 `{App}ApplicationConfig`를 `@Import`)"이었습니다.
- `testFixtures(project(':application-core'))`는 `ApplicationLayerScanAssertions`와 함께, `adaptersShouldOnlyUseOwnAppUseCases`가 Command record의 앱 소속 유도(`AppOwnership`)를 공유하기 위한 것이기도 했습니다(챕터 03).
- Purpose의 챕터 03 콜아웃은 "패키지만으로는 이 모듈이 주입하는 UseCase가 어느 앱 것인지 알 수 없으므로 앱 소속은 마커 애노테이션(`@WebApp`/`@AdminApp`/`@CeoApp`)이 표현한다"였습니다.

##### backend/batch-module/AGENTS.md — 패키지 구조 / 규칙 / `adaptersShouldOnlyUseOwnAppUseCases`

- batch 소속은 마커 `@BatchApp`이 표현했습니다(지금은 `batch-application` 모듈에 있다는 사실이 표현합니다).
- 이 모듈에 남은 규칙은 `shouldNotDependOnInfrastructurePersistence`·`schedulersShouldDependOnUseCasesOnly`·`adaptersShouldOnlyUseOwnAppUseCases` 3개였고, 이 결정으로 2개가 됐습니다.
- `adaptersShouldOnlyUseOwnAppUseCases`는 챕터 01이 4개 application 모듈을 합치며 사라진 컴파일 게이트를 대체하는 규칙이었습니다. 챕터 01 원본은 자기를 뺀 3개 앱 패키지를 열거해 금지했고, 챕터 03 평탄화 후에는 마커로 판정하도록 재작성됐습니다. (a) `..port.in..` UseCase 인터페이스 의존은 그것이 `BatchApp`을 달고 있어야 하고, (b) 마커가 없는 Command record는 `AppOwnership#derive`로 소속을 유도해 `BatchApp`인지 보며, (c) 구체 서비스 의존은 기존 `schedulersShouldDependOnUseCasesOnly`가 맡았습니다. 짝 규칙은 `AppIsolationTest#appsShouldNotDependOnEachOther`(application 계층끼리의 수평 의존)였습니다.

##### backend/AGENTS.md — Module Dependency Graph / 계층 규칙 봉인

- 그래프는 "4개 api 모듈이 같은 application 모듈을 의존하므로, '자기 앱의 UseCase만 주입'은 빌드가 아니라 각 모듈 `LayerRulesTest`의 `adaptersShouldOnlyUseOwnAppUseCases`가 강제한다(챕터 01 신설)"고 적었습니다.
- "인바운드 어댑터의 앱 격리 — 컴파일 게이트가 지킨다" 절은 인바운드 포트(UseCase 인터페이스)가 자기 앱 마커를 직접 달고 있어야 하고, Command record 등은 소속 앱을 `AppOwnership`으로 유도해 자기 앱 마커인지 보며, 각 앱은 자기 앱 마커가 붙은 application 슬라이스만 의존한다고 적었습니다.

##### backend/domain/AGENTS.md · backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md — 빈 등록

- domain에 남은 순수 계산기·정책 7개를 등록하는 `SharedBeanConfig`는 이 결정 전에는 `@SharedApp`이었고, application으로 옮겨 간 포트 주입 서비스는 클래스에 앱 마커만 달아 스캔으로 등록됐습니다.

##### backend/infrastructure/{mail,sms,pg}/AGENTS.md

- `MailVerificationService`·`SmsVerificationService`·`PgPaymentGatewayRouter`는 클래스에 `@WebApp` 마커만 달고 web-api의 마커 스캔으로 등록됐습니다. `MailSenderPort` 등을 받는 마커 클래스가 `@WebApp`이 아니면 `AppIsolationTest#appRestrictedPortDependentsShouldBelongToThatApp`이 빌드를 실패시켰습니다.
- `pg/AGENTS.md`의 "포트 2단 구조"는 `PgPaymentGatewayPort`·`PgProviderGatewayPort`를 둘 다 `application` 소유로 적었습니다(지금은 `web-application`). `javamail`·`solapi`의 Dependencies는 포트 소유 모듈로 `:application-core`를 의존했고, 이 결정으로 `:web-application`으로 바뀌었습니다.

##### CLAUDE.md — 리포지토리 구조

- "application 계층 1개: `application` — 4개 앱의 유스케이스를 담는 단일 모듈"이라고 적었습니다.

#### 결정

앱 마커 5종을 전부 삭제하고 앱 소속을 Gradle 모듈 경계로 옮깁니다. application은 공유 코어(`:application`, 지금의 `:application-core`)와 `{web,admin,ceo,batch}-application` 4모듈로 나뉩니다. 자바 패키지 `com.tastyhouse.application`은 5모듈이 나눠 씁니다(split package). 각 앱은 코어와 자기 앱 모듈만 의존하므로, 다른 앱 코드를 쓰면 **컴파일 에러**가 납니다.

| 항목 | before (앱 마커) | after (모듈 경계) |
|---|---|---|
| application 모듈 | `application` 1개 | 코어 + `{web,admin,ceo,batch}-application` 4개 |
| 앱 소속 표현 | 클래스의 마커 애노테이션 | 클래스가 들어 있는 Gradle 모듈 |
| 앱의 `implementation` | `:application` | `:application` + `:{앱}-application` 1줄 추가 |
| 벤더 `{kakao,naver,apple,facebook}-oauth`·`tosspayments`·`javamail`·`solapi`·`aws-ses`·`aws-sns` | `:application` | `:web-application`(구현하는 SPI 포트가 그리로 이동) |
| 벤더 `bbq`·`admdongkor` | `:application` | `:batch-application` |
| persistence·`firebase`·`aws-s3`·`security-module`·`api-common-module` | `:application` | 변경 없음 |
| 스캔 설정 | `useDefaultFilters = false` + 마커 include 필터 | `@ComponentScan(basePackages = "com.tastyhouse.application")` 기본 필터 |
| 무엇이 뜨는가를 정하는 것 | 마커 | 앱의 클래스패스(코어 + 자기 앱 모듈) — "클래스패스 존재 = 활성화" |
| 도메인 서비스 78개 | 마커만, `@Service` 없음 | `@Service` 부착. 소비 앱이 하나면 그 앱 모듈, 둘 이상이면 코어 |
| 오케스트레이터와 도메인 서비스 구분 | 마커만 = 도메인 서비스, `@Service` + 마커 = 오케스트레이터 | `port.in` UseCase를 구현하면 오케스트레이터, 아니면 도메인 서비스 |
| 리스너·`@Configuration` | `@SharedApp` | 마커 없이 코어에만 둔다 |
| 앱 전용 SPI 포트(메일·SMS·PG·소셜 로그인·BBQ·행정동 경계·답글 문구 검수) | `application` | 소비 앱 모듈 |
| 모듈 수 | 32 | 36(include 31줄 → 35줄 + 암묵 `infrastructure` 컨테이너), bootJar 4개 이름·경로 불변 |

가드도 바꿨습니다.

| 대상 | before | after |
|---|---|---|
| 앱 간 수평 의존·앱 전용 포트 소비·Command 소속 | `AppIsolationTest`, testFixtures `AppOwnership`·`DESERIALIZED_COMMANDS`, api 4모듈 `adaptersShouldOnlyUseOwnAppUseCases` | 삭제 — 컴파일 게이트로 대체 |
| 리스너·마커 위치 | `listenersShouldBeShared`·`sharedAppOnlyOnListeners`·`markerOnlyClassesShouldBeDomainServices`·`sharedConfigsShouldOnlyDeclareUnmarkedBeans` | 삭제. 신설 `listenersAndConfigsShouldResideInCore` |
| 코어에 UseCase가 새는 것 | 없음 | 신설 `coreShouldNotContainUseCasesOrOrchestrators` |
| 빈이 다른 앱에만 있는 구현을 주입 | `constructorDependenciesShouldBeVisibleToApp` | 신설 `coreBeansShouldOnlyDependOnCoreVisibleTypes` |
| `@Configuration`의 이중 등록 | `sharedConfigsShouldOnlyDeclareUnmarkedBeans` | 신설 `configurationsShouldNotRegisterStereotypedClasses` |
| 같은 FQCN이 두 모듈에 생기는 것 | 없음 | 신설 `SplitPackageUniquenessTest` |
| 다른 앱 모듈의 클래스패스 유입 | 마커 필터가 막음 | 신설 4앱 `ApplicationModuleClasspathTest` |
| `commandRecordsShouldBeBoundaryTyped`의 batch 예외 | `.areNotAnnotatedWith(BatchApp.class)` | 클래스의 출처 모듈(`batch-application`)로 판정 |
| `BatchSchedulerRulesTest` 대상 | `@BatchApp` 클래스 | `batch-application` 출처 클래스 |
| 개수 anchor | `markerBeanCounts`·`markerUseCaseCounts` | `moduleBeanCounts`(web ≥83 · admin ≥65 · ceo ≥122 · batch ≥15 · core ≥47)·`moduleUseCaseCounts`(web ≥50 · admin ≥100 · ceo ≥95 · batch =7 · core =0 — 이 결정 시점 값) |
| 스캔 형태 | `assertScansOnlyOwnAppAndSharedMarkers` | `assertScansApplicationLayerWithoutFilters` |

`ApplicationModuleClasspathTest`는 처음에 각 `{앱}-application`이 jar에 싣는 `META-INF/tastyhouse/application-module.properties`(`app=web` 등) 표식의 개수·값으로 판정했습니다. 후속 `406c34ebe`는 이 표식 4개를 삭제하고, 앱 테스트 클래스패스의 `com.tastyhouse.application` 클래스의 출처 모듈 집합이 정확히 `{application, 자기 앱 모듈}`인지를 testFixtures `ModuleOrigin`으로 판정하게 바꿨습니다.

#### 근거

- **컴파일러가 지키게 됩니다.** 마커는 컴파일러가 모르는 약속이라 ArchUnit 규칙 여러 개로 구멍을 메웠습니다. 모듈 경계에서는 다른 앱 모듈이 클래스패스에 없으므로 규칙이 아니라 컴파일러가 지킵니다.
- **챕터 01 통합의 전제가 사라졌습니다.** 챕터 01은 "persistence가 4개 앱 모듈 전부를 의존하므로, 모듈이 나뉘어 있어도 persistence를 거쳐 서로가 보여 컴파일 게이트가 실제로는 없다"는 근거로 4모듈을 합쳤습니다. 지금 persistence가 application에서 import하는 것은 `..port.out..`뿐이고 `port.out`은 전부 코어에 있으므로, persistence는 코어 하나만 의존합니다. 따라서 앱 모듈끼리는 어떤 경로로도 서로를 보지 못하고 컴파일 게이트가 진짜로 동작합니다. 모듈명은 같아도 내용은 다릅니다. 과거 4모듈은 읽기 계약까지 나눠 가졌지만, 지금 4모듈은 계약을 코어에 두고 앱 전용 빈만 갖습니다.
- **`@Service`를 달 수 있게 된 이유**: 마커가 없어지면 "스테레오타입 없는 클래스"라는 `ServiceContextBoundaryTest`의 술어를 쓸 수 없습니다. 그래서 술어를 "구조 조건 − 협력 빈 FQN 목록 `EXCLUDED_COLLABORATORS`"로 바꿔 대상 집합(94개)을 이전과 같게 유지했습니다.
- **중첩 스캔 클래스 형태는 유지합니다.** `@SpringBootApplication` 클래스에 `@ComponentScan`을 직접 달면 메타 `@ComponentScan`이 무시되는 Spring 6.1 함정은 그대로이기 때문입니다.
- **표식을 지운 이유(`406c34ebe`)**: 테스트 전용 표식이 운영 jar에 실리는 것을 없애기 위해서입니다.

#### 결과·감수한 손실

- 동작 변경은 없습니다. 4앱 jar를 띄워 싱글턴 빈 이름 집합을 비교한 결과 차이가 0이었습니다(web 1131 · admin 1079 · ceo 1155 · batch 791).
- 새 실수 지점이 생겼습니다. 다른 앱 모듈을 의존에 추가하면 그 앱의 빈이 전부 뜹니다("클래스패스 존재 = 활성화"). 각 앱의 `ApplicationModuleClasspathTest`가 이를 막습니다.
- `ServiceContextBoundaryTest`의 술어는 솔직히 "구조 + 이름 목록"입니다. 새 협력 빈을 만들면 `EXCLUDED_COLLABORATORS`에 넣을지 사람이 판단해야 합니다. 짝 테스트 `excludedCollaboratorsShouldNotBeStale`은 더 이상 존재하지 않거나 구조 조건에 맞지 않게 된 항목만 잡고, "빼도 경계 규칙을 통과할 항목"은 잡지 못합니다.
- `coreBeansShouldOnlyDependOnCoreVisibleTypes`는 `@Primary`/`@Qualifier`를 모델링하지 않습니다. infrastructure가 구현하는 포트(후보 0인 의존)는 건너뜁니다.
- 마커 스캔 시절에는 마커 없는 `@Service`가 컴파일은 통과하고 기동 시점 `NoSuchBeanDefinitionException`으로만 드러나 "이 모듈에서 가장 흔한 실수 지점"이었습니다(`beansShouldHaveExactlyOneAppMarker`·`useCasesShouldHaveExactlyOneAppMarker`가 빌드에서 잡았습니다). 지금은 `@Service`가 없으면 안 뜨는 일반 Spring과 같습니다.
- `sharedBeansShouldNotDependOnWebOnlyServices`도 `AppIsolationTest`와 함께 삭제됐습니다. 그 4개 타입이 `web-application`에 있어 코어 빈은 컴파일 단계에서 볼 수 없기 때문입니다.
- 공용 테스트 더블 20개를 코어 testFixtures `com.tastyhouse.testsupport`로 옮기며 package-private이던 9개를 public으로 바꿨습니다. import 4순위에 `testsupport`가 추가됐습니다.
- [결정 0034](#decision-0034)의 "config 삭제"와 "`SharedBeanConfig`에는 애노테이션을 달 수 없는 빈만 둔다"는 유지됐습니다. "소비 앱 집합으로 정한다"는 판단은 마커 선택 기준에서 모듈 선택 기준으로 이어졌습니다.

<a id="decision-0037"></a>

### 0037. infrastructure 모듈의 자바 패키지 루트를 com.tastyhouse.infrastructure.{모듈}로 통일한다

- 상태: 채택
- 결정 커밋: `e03a0df33` (2026-10-04)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0037-infrastructure-package-root.md`

#### 맥락

`infrastructure`는 기술별로 나뉜 중첩 Gradle 프로젝트입니다(`infrastructure:jpa`, `infrastructure:redis`, `infrastructure:kakao-oauth` 등). 모듈 이름과 자바 패키지 이름은 별개라서, 모듈을 나누거나 옮겨도 패키지는 그대로 남을 수 있습니다.

- **패키지 루트**: 한 모듈의 main 클래스가 모두 들어가는 최상위 자바 패키지입니다.
- **컴포넌트 스캔**: Spring이 지정한 패키지 아래의 `@Component`류 클래스를 찾아 빈으로 등록하는 기능입니다. 하위 패키지도 함께 스캔됩니다.

결정 전에는 infrastructure 모듈의 패키지 루트가 세 갈래였습니다. 당시 persistence 모듈이 `com.tastyhouse.infrastructure`를 **통째로** 스캔했기 때문에, 다른 모듈을 그 아래 두면 의존하지도 않은 어댑터까지 스캔돼 부팅이 깨질 수 있었습니다. 그래서 벤더 모듈은 일부러 다른 루트(`com.tastyhouse.external`)에 살았습니다. 이 분산 때문에 `DomainPurityTest`·application `LayerRulesTest`가 금지하는 `com.tastyhouse.infrastructure..` 패턴이 벤더·restclient를 덮지 못하는 구멍도 있었습니다.

#### 이전 규칙

##### backend/CLAUDE.md — 모듈 지도 · 패키지 최상위 지도 · infrastructure 패키지 규칙

- `infrastructure:persistence`·`infrastructure:redis`는 `com.tastyhouse.infrastructure..`를 썼습니다. persistence는 모듈명 세그먼트 없이 컨텍스트를 루트에 바로 펼쳤습니다(예: `com.tastyhouse.infrastructure.order.persistence`). 재편은 Gradle 좌표와 디렉터리만 바꿨습니다.
- 벤더·채널 16모듈(`firebase`·`aws-s3`·`aws-ses`·`aws-sns`·`kakao-oauth`·`naver-oauth`·`apple-oauth`·`facebook-oauth`·`pg`·`tosspayments`·`mail`·`javamail`·`sms`·`solapi`·`bbq`·`admdongkor`)은 `com.tastyhouse.external..` 하나를 하위 패키지별로 나눠 썼습니다. `PersistenceModuleAutoConfiguration`의 `@ComponentScan("com.tastyhouse.infrastructure")` 범위 **밖**에 남아 있어야 했기 때문입니다.
- 코어 `infrastructure:restclient`는 모듈 리네임 때 `com.tastyhouse.restclient..`로 옮겨 빠졌고, 클래스는 전부 `.config` 하위에 있었습니다.
- persistence 스캔은 `@ComponentScan(basePackages = "com.tastyhouse.infrastructure", excludeFilters = REGEX redis)` — redis를 REGEX 제외 필터로 겨우 빼냈습니다.
- 패키지 최상위 지도의 `com.tastyhouse.external..` 시절 이력: 16개 벤더·채널 모듈이 하위 패키지를 나눠 소유했고, 모듈마다 덩어리가 겹치지 않아 split package가 아니었습니다. 그러려고 `external.file.firebase`→`external.firebase`, `external.file.s3`→`external.aws.s3`, `external.mail.ses`→`external.aws.ses`, `external.sms.sns`→`external.aws.sns`, `external.file.RemoteImageDownloader`→`external.crawling` 5건을 옮겼습니다(코어가 `external.file`을 스캔할 때 하위 패키지가 동반 스캔되는 것도 함께 막았습니다). 이후 분할로 `external.crawling`→`external.bbq`, `external.region`→`external.admdongkor`, messaging 4분할로 `external.mail.javamail`→`external.javamail`·`external.sms.solapi`→`external.solapi`(채널 스캔에 벤더가 동반 스캔되지 않게), `external.payment.toss`→`external.tosspayments`, `external.oauth.{kakao,naver,apple,facebook}`→`external.{kakao,naver,apple,facebook}.oauth`가 됐습니다. restclient의 `.exception` 하위 패키지는 존재한 적이 없습니다.
- 루트 통일 직후 before/after 표의 벤더 예시: `com.tastyhouse.external.kakao.oauth.KakaoTokenResponse` → `com.tastyhouse.infrastructure.kakao.oauth.dto.KakaoTokenResponse`, `com.tastyhouse.external.solapi.{request,response}.*` → `com.tastyhouse.infrastructure.solapi.dto.*`(둘 다 이후 [결정 0040](#decision-0040)으로 루트 평면 package-private이 됐습니다).

##### backend/CLAUDE.md — 코딩 스타일 (import 순서) > 자사 그룹 내부 계층 정렬

- 3순위(driven 어댑터)는 `infrastructure` — `external` · `restclient` 세 세그먼트였고, 같은 순위 안 순서는 `external` → `infrastructure` → `restclient`(ASCII 오름차순)였습니다. 루트 통일로 두 세그먼트가 사라져 3순위는 `infrastructure` 하나가 됐습니다. `backend/AGENTS.md`의 "Working In This Directory" import 순서 항목도 같은 서술이었습니다.

##### CLAUDE.md · backend/AGENTS.md — 리포지토리 구조 · Module Dependency Graph

- 루트 `CLAUDE.md`는 벤더·채널 16모듈이 예외로 `com.tastyhouse.external..`을 유지하는 이유를 "`InfrastructureModuleConfig`(챕터 02 리네임 전 이름)가 `com.tastyhouse.infrastructure`를 통째로 스캔해서, 그 아래로 옮기면 admin/ceo/batch 부팅이 깨진다"고 적었습니다.
- `backend/AGENTS.md`는 "모듈명과 자바 패키지명은 다를 수 있다: 벤더 13 + 채널 3 = 16모듈이 `com.tastyhouse.external..`을 나눠 소유하고, 코어 `restclient`는 `com.tastyhouse.restclient..`를 소유한다"고 적었습니다. 루트 통일 직후 같은 자리는 "코드가 있는 16모듈(`persistence`·`redis`·`restclient` + 벤더 13)"으로 바뀌었습니다(이후 jpa 모듈 분리로 17모듈).

##### backend/infrastructure/jpa/AGENTS.md — 머리말 · 규칙 · 봉인·가드 목록 (당시 infrastructure:persistence)

- 챕터 05 경로 이동 시점에 자바 패키지 `com.tastyhouse.infrastructure..`는 불변이었고, 루트 통일로 `com.tastyhouse.infrastructure.persistence..`로 한 겹 내려갔습니다. 루트를 모듈명으로 내리면서 통째 스캔과 redis 제외 필터가 함께 사라졌습니다.
- `LayerRulesTest` 패키지 술어는 루트를 끝까지 적도록 바뀌었습니다. 루트 자체에 `persistence` 세그먼트가 생겨, 이전 술어를 두면 조회 DAO(`com.tastyhouse.infrastructure.persistence.<ctx>.query`)까지 write 어댑터로 매칭되기 때문입니다.

| 술어 | before (루트 `com.tastyhouse.infrastructure`) | after (루트 `com.tastyhouse.infrastructure.persistence`) |
|---|---|---|
| write 어댑터 | `com.tastyhouse.infrastructure..persistence..` | `com.tastyhouse.infrastructure.persistence..persistence..` |
| 조회 DAO | `com.tastyhouse.infrastructure..query..` | `com.tastyhouse.infrastructure.persistence..query..` |
| 모듈 루트 | 없음 | `com.tastyhouse.infrastructure.persistence..`(`shouldResideInModuleRootPackage`) |
| 앱 모듈 `LayerRulesTest`의 persistence 금지 술어 | `"..infrastructure..persistence.."` | `"com.tastyhouse.infrastructure.persistence.."` |

- 봉인: "중복처럼 보인다"며 `com.tastyhouse.infrastructure..persistence..`로 줄이지 않는다 — `noClasses()`는 대상이 늘든 줄든 위반이 없으면 통과하므로 컴파일·테스트 어느 쪽도 실패하지 않은 채 대상이 바뀝니다. 대상 메서드는 `queryShouldNotDependOnDomain`·`persistenceShouldNotDependOnQuery`·`sealedPersistenceToQueryShouldNotBeStale`·`shouldResideInModuleRootPackage`입니다. 이후 [결정 0047](infrastructure/jpa/AGENTS.md#decision-0047)로 루트가 `com.tastyhouse.infrastructure.jpa`가 되어 이 함정은 해소됐습니다.

##### backend/infrastructure/restclient/AGENTS.md — 패키지 봉인 절

- "자바 패키지 `com.tastyhouse.external..` 봉인" 절은 벤더 모듈을 `com.tastyhouse.infrastructure` 아래로 옮기면 persistence 스캔이 그 트리를 통째로 훑어 **admin-api·ceo-api·batch-module의 부팅이 깨진다**고 적고, 모듈 디렉터리와 패키지 이름이 어긋나 보여도 정리하지 말라고 봉인했습니다.
- 코어를 `com.tastyhouse.infrastructure.restclient`로 두지 않는 이유로, persistence 스캔이 진입 설정을 다시 등록하는 이중 등록과 `@AutoConfiguration` 순서·조건 계약 무력화를 들었습니다. Spring은 같은 클래스의 중복 스캔을 한 번만 등록하므로 기동 실패로 이어지지는 않지만 등록 주체가 persistence 쪽으로 넘어간다는 것이었고, 이 이동은 `backend/CLAUDE.md`의 "후속 프로그램 — 벤더 패키지를 `com.tastyhouse.infrastructure.*`로 정렬" 절의 stage B 대상(벤더·redis와 함께)으로 미뤄져 있었습니다. 이 결정이 그 stage A·B를 실행한 것입니다.
- "코어 패키지는 `com.tastyhouse.restclient..`, 벤더 9모듈은 `com.tastyhouse.external..` 유지" 절: 코어만 `security-core`/`security-module`이 `com.tastyhouse.security..`를 공유하는 선례(모듈명 ≠ 패키지명)를 따라 모듈 리네임과 함께 `com.tastyhouse.restclient.config`로 옮겼습니다. 코어는 persistence 스캔 트리에 걸리는 벤더 빈이 없어(설정 클래스뿐) 그 제약에서 자유로웠습니다. 7모듈 분리 전에는 persistence 스캔이 진입 설정의 OAuth REGEX 제외 필터를 우회해 admin/ceo/batch가 `Could not resolve placeholder 'apple.team-id'`로 부팅에 실패한 적이 있었습니다. 문서들은 벤더 수를 "9모듈"로 적기도 했습니다(실제 대상은 벤더·채널 16모듈).
- 하위 문서 `src/main/java/.../restclient/AGENTS.md`(루트 통일 전 `src/main/java/com/tastyhouse/restclient/AGENTS.md`)는 코어 패키지에 `config/` 하위 패키지 하나만 남는다고 적었고, 같은 봉인("벤더 패키지를 `com.tastyhouse.infrastructure` 아래로 옮기지 않는다")을 반복했습니다. 가드 `NoReactiveHttpClientTest`도 `com.tastyhouse.restclient.architecture`에서 `com.tastyhouse.infrastructure.restclient.architecture`로 옮겨지며 자기 자신을 예외로 지정하는 `GUARD_SOURCE` 경로 문자열이 함께 바뀌었습니다.
- "벤더 패키지를 `external.file` 아래에 두지 않았던 이유" 절: 7모듈 분리 당시 코어 스캔이 `com.tastyhouse.external.file`이어서 `external.file.firebase`·`external.file.s3`를 그대로 두면 코어만 import해도 벤더 빈이 딸려 올라왔습니다. 그래서 firebase·s3·`RemoteImageDownloader` 3건만 패키지를 바꿨고, 파일 저장 SPI 삭제로 그 스캔이 사라진 뒤에도 split package 회피를 위해 되돌리지 않았습니다.

##### backend/infrastructure/redis/AGENTS.md — 패키지 루트 `com.tastyhouse.infrastructure.redis`

- 이 모듈의 패키지는 루트 통일 전후로 바뀌지 않았습니다. 바뀐 것은 persistence 스캔입니다: before는 `@ComponentScan("com.tastyhouse.infrastructure")`가 이 모듈 트리까지 덮고 REGEX `excludeFilters`(`com\.tastyhouse\.infrastructure\.redis\..*`)로 되돌렸습니다. 제외 필터가 빠지면 `RedisModuleAutoConfiguration`과 중복 등록되는 구조였습니다. after는 `@ComponentScan("com.tastyhouse.infrastructure.persistence")`로 겹치지 않아 제외 필터가 삭제됐고 Redis 빈 등록 주체가 하나라는 것이 구조적으로 보장됐습니다.

##### 벤더 모듈 AGENTS.md(bbq·firebase·admdongkor·aws-s3·aws-ses·aws-sns·kakao/naver/apple/facebook-oauth·javamail·solapi·tosspayments)

각 문서에 "자바 패키지 `com.tastyhouse.external.{벤더}` 봉인" 절이 있었습니다. 외부 연동 모듈이 `com.tastyhouse.external..`을 유지하는 것은 취향이 아니라 스캔 범위 제약이라고 적었습니다.

모듈별로 덧붙은 내용은 다음과 같습니다.

- **kakao·naver·apple·facebook-oauth**: 이 패키지 이름은 web-api ArchUnit `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`가 문자열로 참조하므로, 이름을 바꾸면 규칙이 조용히 대상을 잃는다고 적었습니다. 루트 통일 때 그 목록을 `com.tastyhouse.infrastructure.{kakao,naver,facebook,apple}.oauth..`로 교체했습니다.
- **javamail·solapi·aws-ses·aws-sns·tosspayments**: 옛 위치(`external.mail.javamail`·`external.sms.solapi`·`external.mail.ses`·`external.sms.sns`·`external.pg.toss`)로 되돌리면 채널 모듈의 스캔(예: `infrastructure:mail`의 `@ComponentScan("com.tastyhouse.external.mail")`)에 동반 스캔·등록된다는 이유를 함께 적었습니다.
- **firebase·aws-s3**: 원래 위치 `external.file.firebase`·`external.file.s3`로 되돌리지 않는다고 적었습니다. 분리 당시에는 코어 `ExternalModuleAutoConfiguration`의 `com.tastyhouse.external.file` 스캔에 동반 스캔되는 것이 사유였고, 그 스캔이 없어진 뒤에도 벤더 모듈마다 겹치지 않는 하위 패키지를 소유해 split package를 피하기 위해 이동을 금지했습니다.
- **admdongkor**: 분리 때 `com.tastyhouse.external.region`에서 `com.tastyhouse.external.admdongkor`로 바뀌었고(모듈명과 맞춤 — `external.file.firebase → external.firebase` 선례), 클래스명·프로퍼티 접두사·환경변수 이름은 불변이었습니다. 옛 위치 `external.region`으로 되돌리지 않는다고 적었습니다.
- **bbq**: 옛 위치 `external.crawling`으로 되돌리지 않는다고 적었습니다. `RemoteImageDownloader`는 `external.file.RemoteImageDownloader` → `external.crawling.RemoteImageDownloader` → `external.bbq.RemoteImageDownloader` → `com.tastyhouse.infrastructure.bbq.RemoteImageDownloader`로 옮겨졌습니다. 첫 이동은 당시 코어가 `external.file`을 스캔해 admin/ceo에도 이 빈이 동반 스캔됐기 때문이고, 두 번째는 소유 모듈의 패키지에 두는 규칙을 따른 것입니다.

#### 결정

persistence를 `com.tastyhouse.infrastructure.persistence`로 한 겹 내리고 스캔을 자기 루트로 좁혔습니다. 그러자 제약이 사라져, 코드가 있는 infrastructure 모듈 전부의 루트를 `com.tastyhouse.infrastructure.{Gradle 모듈명의 하이픈을 점으로}` 하나로 맞췄습니다.

| 항목 | before | after |
|---|---|---|
| persistence 패키지 | `com.tastyhouse.infrastructure.order.persistence` | `com.tastyhouse.infrastructure.persistence.order.persistence` (이후 [결정 0047](infrastructure/jpa/AGENTS.md#decision-0047)로 `com.tastyhouse.infrastructure.jpa.order.persistence`) |
| persistence 스캔 | `@ComponentScan(basePackages = "com.tastyhouse.infrastructure", excludeFilters = REGEX redis)` | `@ComponentScan("com.tastyhouse.infrastructure.persistence")` |
| redis | `com.tastyhouse.infrastructure.redis` | 불변 |
| restclient | `com.tastyhouse.restclient.config.RestClientConfig` | `com.tastyhouse.infrastructure.restclient.RestClientConfig` (`.config` 하위 패키지 소멸) |
| 벤더 13모듈 | `com.tastyhouse.external.{벤더}` (예: `external.kakao.oauth`) | `com.tastyhouse.infrastructure.{벤더}` (예: `infrastructure.kakao.oauth`) |
| 벤더 wire DTO | 벤더마다 제각각(solapi는 `request/`·`response/`) | `{루트}.dto`로 모음 (이후 [결정 0040](#decision-0040)이 루트 평면으로 다시 옮김) |
| 가드 | 없음 | 모듈마다 `shouldResideInModuleRootPackage` (redis·restclient는 `PackageRootTest`, 벤더는 `VendorLayerRulesTest`, persistence는 `LayerRulesTest`) |

#### 근거

- 세 갈래 루트의 유일한 이유는 persistence의 통째 스캔이었습니다. 스캔을 자기 루트로 좁히면 그 이유가 사라집니다(커밋 메시지: "persistence를 infrastructure.persistence로 내려 스캔을 자기 루트로 좁히고 redis 제외 필터를 삭제했다").
- 루트를 통일하면 domain·application의 `com.tastyhouse.infrastructure..` 금지 가드가 벤더까지 덮습니다. 이전에는 벤더·restclient가 그 패턴 밖에 있어 구멍이었습니다.
- 모듈명에서 패키지를 기계적으로 유도할 수 있어, 모듈 디렉터리와 패키지가 어긋나 보이는 상태가 없어집니다.

#### 결과·감수한 손실

- **런타임 동작은 바뀌지 않았습니다.** JPQL·yml·logback·AOP pointcut·Redis 직렬화에 이 FQN 문자열이 없습니다. `@EnableConfigurationProperties`로 등록된 Properties 빈 이름(`{prefix}-{FQN}`)은 바뀌지만 문자열로 참조하는 곳이 없습니다.
- 벤더 패키지 세그먼트(`.firebase`·`.aws.s3`·`.aws.ses`·`.aws.sns`·`.bbq` 등)는 그대로이고 접두어만 `com.tastyhouse.external` → `com.tastyhouse.infrastructure`로 바뀌었습니다. 모듈마다 겹치지 않는 하위 패키지를 소유해 split package를 피하는 구조도 그대로입니다.
- 이 직후 persistence 루트에 `persistence` 세그먼트가 생겨 `persistence.order.persistence`처럼 이름이 두 번 나오는 함정이 있었습니다. 이것은 [결정 0047](infrastructure/jpa/AGENTS.md#decision-0047)이 루트를 `jpa`로 바꾸면서 해소됐습니다.
- `{루트}.dto` 배치는 같은 날 [결정 0040](#decision-0040)이 루트 평면 배치로 바꿨습니다.

<a id="decision-0038"></a>

### 0038. AutoConfiguration.imports를 제거하고 앱 ModuleScanConfig 문자열 스캔으로 조립한다

- 상태: 채택
- 결정 커밋: `a400ec42e` (2026-10-04)
- 대체한 결정: [결정 0017](#decision-0017)
- 원문: 삭제 전 git 이력의 `docs/adr/0038-autoconfiguration-imports-removal.md`

#### 맥락

- **auto-configuration**: Spring Boot가 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 파일에 적힌 설정 클래스를 클래스패스에 있다는 이유만으로 자동으로 로딩하는 장치입니다. `@AutoConfiguration(before/after)`로 순서를, `@ConditionalOnBean`·`@ConditionalOnMissingBean`으로 "다른 빈이 있을 때만" 같은 조건을 걸 수 있습니다.
- **`runtimeOnly` 의존**: 앱이 라이브러리를 실행할 때만 싣고 컴파일할 때는 보지 못하게 하는 Gradle 선언입니다. 앱이 어댑터 타입을 모르게 하는 장치입니다.

[결정 0017](#decision-0017)로 라이브러리 모듈은 imports 파일로 스스로를 등록했습니다. 그 결과 "의존 선언 = 활성화"가 되었고, 순서·빈 존재 조건 같은 auto-config 고유 기능에 기대는 설정이 늘었습니다. 앱은 infra를 `runtimeOnly`로 의존하므로 `@Import(Class)`로 되돌아갈 수도 없었습니다.

#### 이전 규칙

##### backend/CLAUDE.md — 모듈 등록 컨벤션 (auto-configuration — 챕터 02)

- 라이브러리 모듈 13곳이 imports 파일 19개로 자기 등록했고, 설정 클래스는 `{Xxx}ModuleAutoConfiguration`(`@AutoConfiguration` + 자기 패키지 `@ComponentScan`) 20개였습니다.
- 특정 모듈을 끄려면 `spring.autoconfigure.exclude`를 썼습니다.
- "클래스패스 존재 = 활성화" 원칙: imports 파일이 클래스패스에 있으면 무조건 로딩을 시도하므로, 각 auto-configuration은 전이로 끌려온 앱에서 발화해도 안전한지 `@Conditional*`로 스스로 답해야 했습니다.
- 과거 인벤토리의 조건·순서:
  - persistence `PersistenceModuleAutoConfiguration` — `@AutoConfiguration(before = JpaRepositoriesAutoConfiguration.class)`.
  - redis `RedisModuleAutoConfiguration` — `before = RedisAutoConfiguration.class`. "함정 1"은 우리 `stringRedisTemplate`이 Boot의 동명 빈과 겹쳐 `after`로 두면 4앱 전부 기동에 실패한다는 실측 기록이었습니다.
  - security-module — `@ConditionalOnWebApplication(SERVLET)` + `@ConditionalOnMissingBean(JwtAuthenticationFilter.class)`.
  - api-common-module — `ApiCommonModuleAutoConfiguration`이 `@Bean("sharedGlobalExceptionHandler")`를 `@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)`로 등록했습니다. 자체 핸들러가 있는 web-api에서는 조건이 Negative라 물러났습니다. 빈 이름을 `sharedGlobalExceptionHandler`로 구분한 이유는, 조건이 우회돼도 이름 충돌로 기동이 실패해 조용히 덮이지 않게 하려는 것이었습니다.
  - `ApiCommonRateLimitAutoConfiguration` — `@ConditionalOnBean(RateLimitCounterPort.class)` + `afterName`.
  - security-module `SecurityModuleAutoConfiguration`(챕터 02로 `SecurityModuleConfig`에서 리네임) — `@ComponentScan("com.tastyhouse.security")` + `@EnableConfigurationProperties(JwtProperties)` + `@Bean JwtAuthenticationFilter`.
  - logging-module `LoggingModuleAutoConfiguration` — `@ComponentScan("com.tastyhouse.logging")`, 조건 없음.
  - external 계열 driven 14개 — `RestClientModuleAutoConfiguration`(구 `ExternalModuleAutoConfiguration` → `HttpClientModuleAutoConfiguration`)·`FirebaseModuleAutoConfiguration`·`AwsS3ModuleAutoConfiguration`·`AwsSesModuleAutoConfiguration`·`AwsSnsModuleAutoConfiguration`·`{Kakao,Naver,Apple,Facebook}OAuthModuleAutoConfiguration`·`TossPaymentsModuleAutoConfiguration`·`JavaMailModuleAutoConfiguration`·`SolapiModuleAutoConfiguration`·`BbqModuleAutoConfiguration`·`AdmdongkorModuleAutoConfiguration`. 각자 자기 패키지 `@ComponentScan` + Properties 등록, 조건 없음.
  - 그때 `ApiCommonConfig`·`ApiCommonRateLimitConfig`는 두 auto-configuration 클래스가 대체해 삭제된 상태였습니다(imports 제거로 `ApiCommonRateLimitConfig`가 리네임으로 다시 생겼습니다).
- "클래스패스 존재 = 활성화"의 함의로 든 oauth 사례: `@Import` 시절에는 `infrastructure:oauth`를 어떤 앱 `build.gradle`에 추가해도 `OAuthModuleConfig`를 `@Import`하지 않으면 빈이 뜨지 않았으나, auto-configuration 이후에는 실수로 `admin-api`에 추가하면 `OAuthModuleAutoConfiguration`이 즉시 로딩되어 `Could not resolve placeholder 'apple.team-id'`로 기동이 실패했습니다. 이 위험은 imports 제거 후에도 같습니다(앱 스캔이 클래스패스의 infra를 전부 잡으므로).
- `ApiCommon*`의 `@ConditionalOnWebApplication(SERVLET)`이 지킨 대상: 과거 `application → security-core → infrastructure:redis → api-common-module` 전이 사슬이 batch를 포함한 4앱 runtimeClasspath에 있어, non-servlet인 batch에서 두 설정이 스스로 발화를 걸렀습니다. 챕터 01의 토큰 저장소 포트/어댑터 역전으로 사슬이 끊긴 뒤에는 재유입 방어선으로 남았습니다.

##### backend/CLAUDE.md — 모듈 등록 컨벤션 > 앱별 runtimeClasspath 감사표 · 함정 2가지

- 감사표는 "어떤 auto-config가 어느 앱에서 발화하는가"를 조건 열과 함께 기록했습니다. imports 제거 후 "발화"는 두 단계(앱 runtimeClasspath에 있는가, 그 패키지를 앱 `ModuleScanConfig`가 스캔하는가)로 읽고, 클래스패스 열(●/—)은 그대로 유효합니다.

| 행 | before | after |
|---|---|---|
| Persistence 조건 | `before = JpaRepositoriesAutoConfiguration` | 없음 — 설정 클래스 삭제, 앱 `com.tastyhouse.infrastructure` 스캔 |
| ApiCommon(예외 핸들러) | web 조건부 Negative / admin·ceo Positive / batch 전이 Negative | web 스캔 안 함 / admin·ceo `apicommon.exception` 스캔 / batch 스캔 안 함, 조건 없음 |
| ApiCommonRateLimit | `@ConditionalOnBean(RateLimitCounterPort)` + SERVLET | web·admin·ceo `apicommon.ratelimit` 스캔, batch 스캔 안 함, SERVLET만 유지 |
| Security | SERVLET 조건 | web·admin·ceo `security` 스캔, batch 스캔 안 함(클래스패스에도 없음), SERVLET 유지 |
| Logging·External·Firebase·벤더·Bbq·Admdongkor·Aws* | 각 imports 파일로 발화 | 클래스패스에 있으면 `infrastructure`/`logging` 스캔으로 등록 |

- 당시 실측(2026-09-05, `java -jar ... --debug`의 `CONDITIONS EVALUATION REPORT`): web은 `sharedGlobalExceptionHandler` Negative·`rateLimitAspect` Positive, admin/ceo는 둘 다 Positive, batch는 `ApiCommon*` 2개 Negative였습니다. Boot `JpaRepositoriesAutoConfiguration`은 4앱 전부 Negative(persistence의 `before` 순서가 이긴 결과)였고, admin·ceo·web 로그인 rate limit을 curl로 10회까지 401·11회째 429로 확인했습니다.
- 함정 1(소멸): 챕터 02 원 스펙은 `RedisModuleAutoConfiguration`을 `after = RedisAutoConfiguration.class`로 설계했으나 실측으로 `before`가 맞았습니다. `after`면 Boot가 `stringRedisTemplate`을 먼저 등록하고 스캔된 `RedisConfig`가 같은 이름으로 다시 등록하려다 `allow-bean-definition-overriding=false`(기본값)에 걸려 4앱 전부 기동에 실패했습니다. 실패 지점은 "Boot의 조건이 물러나지 않아서"가 아니라 "스캔 빈이 auto-config 빈을 덮으려다 override 금지에 걸려서"였습니다. 교훈("Boot 표준 빈과 이름이 겹치면 기본값을 가정하지 말고 실제 기동으로 검증한다")은 유효합니다.

##### backend/CLAUDE.md — 컴포지션 루트 규칙 > 앱이 가질 수 있는 조립 코드의 상한

- imports 제거 전 상한은 부트스트랩 중첩 `ApplicationLayerScanConfig` 1개였습니다(그 전에는 `@Import({App}ApplicationConfig)` 한 줄). 라이브러리 모듈의 자기 등록이 사라지며 그 활성화 목록이 앱의 두 번째 중첩 클래스 `ModuleScanConfig`로 올라와 상한이 "중첩 스캔 클래스 2개"가 됐습니다. 기준점 `batch-module`도 `config/` 없이 중첩 클래스 2개만 갖습니다.

##### backend/CLAUDE.md — api 모듈 공용 플럼빙 소유 규칙 > 통합된 것

"핸들러 중복 방지는 스캔 범위가 아니라 조건부 `@Bean`이다(챕터 02 개정)" — 이 모듈은 컴포넌트 스캔을 쓰지 않았습니다.

##### 각 모듈 AGENTS.md

api-common-module·redis·jpa(당시 persistence)·logging-module·security-module·firebase·oauth·벤더 모듈 문서가 `…ModuleAutoConfiguration`의 이름·조건·`before`/`afterName`을 서술했습니다. 예를 들어 jpa·logging 문서에는 "이 모듈이 앱의 runtimeClasspath에 있으면 자동 활성화된다"는 문장이 있었습니다.

문서별 세부는 다음과 같습니다.

- **앱 모듈(web-api·admin-api·ceo-api·batch-module AGENTS.md)**: 라이브러리 모듈이 각자의 `{Xxx}ModuleAutoConfiguration`으로 자기 자신을 등록하므로 앱은 스캔 대상을 나열하지 않는다고 적었습니다. `infrastructure:persistence`·`infrastructure:redis`는 `PersistenceModuleAutoConfiguration`·`RedisModuleAutoConfiguration`이 자기 등록했습니다. batch는 `PersistenceModuleAutoConfiguration`·`RestClientModuleAutoConfiguration`·`FirebaseModuleAutoConfiguration`·`BbqModuleAutoConfiguration`·`AdmdongkorModuleAutoConfiguration`·`LoggingModuleAutoConfiguration`이 전부 imports 파일로 발화했습니다. web의 "핸들러가 2개가 되는 것은 조건이 막는다(실측 Negative)", web·admin의 "aspect는 `ApiCommonRateLimitAutoConfiguration`이 `RateLimitCounterPort` 빈 존재를 조건으로 자동 등록한다"도 이 시절 서술입니다.
- **벤더·스타터 모듈 AGENTS.md**: 진입 설정 `{X}ModuleAutoConfiguration`이 `@ComponentScan(자기 패키지)` + `@EnableConfigurationProperties`를 가졌고, "진입 설정은 자기 (하위) 패키지만 스캔한다 — `com.tastyhouse.infrastructure`나 `com.tastyhouse.infrastructure.aws` 루트로 넓히면 같은 클래스패스의 형제 모듈 빈까지 이 설정이 등록한다"는 봉인이 있었습니다(bbq·admdongkor·aws-s3·aws-ses·aws-sns). 분리 전 `CrawlingModuleAutoConfiguration`은 `external.crawling`과 `external.region` 두 패키지를 함께 스캔했습니다. pg 문서는 벤더 설정 `TossPaymentsModuleAutoConfiguration`이 조건이 없어 `--debug` 리포트의 "Unconditional classes" 절에 나온다고 적었습니다. file-storage·firebase 문서는 firebase의 auto-configuration이 클래스패스 존재만으로 빈을 등록한다고 적었습니다.
- **application-core AGENTS.md**: write 어댑터 `XxxPersistenceAdapter`는 `PersistenceModuleAutoConfiguration`의 스캔으로 4앱에 뜬다고 적었습니다.
- **jpa AGENTS.md(당시 persistence)**: "모듈 auto-configuration의 두 설정을 건드리지 않는다" 봉인 — `PersistenceModuleAutoConfiguration`의 `infrastructure.redis` 제외(이후 [결정 0037](#decision-0037)로 삭제)와 `before = JpaRepositoriesAutoConfiguration`. `before`를 빼면 스캔 안 `InfrastructurePersistenceConfig`의 `@EnableJpaRepositories`가 Boot보다 늦게 처리돼 Boot 쪽 `@ConditionalOnMissingBean(JpaRepositoryConfigExtension)`이 물러나지 않는다는 이유였습니다. 모듈을 끄려면 `spring.autoconfigure.exclude`를 쓴다고 적었습니다. `PersistenceModuleAutoConfiguration`은 imports 파일이 FQCN으로 참조하므로 경로 변경 금지라는 봉인도 있었습니다.
- **api-common-module AGENTS.md**:
  - `ApiCommonModuleAutoConfiguration`은 컴포넌트 스캔을 쓰지 않고 앱별로 켜고 꺼야 하는 빈만 조건부 `@Bean`으로 등록했습니다. 빈 이름 `sharedGlobalExceptionHandler`를 기본 이름으로 되돌리지 않는다는 봉인이 있었습니다(조건이 우회돼도 이름 충돌로 기동이 실패해 조용히 덮이지 않게).
  - `ApiCommonRateLimitAutoConfiguration`은 `@AutoConfiguration(afterName = "com.tastyhouse.infrastructure.redis.RedisModuleAutoConfiguration")`로 순서를 문자열로 선언했습니다. api-common의 main 클래스패스에서 redis 타입을 볼 수 없었기 때문입니다(초기에는 `infrastructure:redis → api-common-module` 방향이라 순환이었습니다). 이 문자열은 리네임해도 컴파일이 깨지지 않으므로 `ApiCommonAutoConfigurationTest#rateLimitAutoConfigurationAfterNameResolvesToRealClass`가 유일한 방어선이었고, 그 테스트는 원래 redis의 `RedisModuleAutoConfigurationTest`에 있다가 `testImplementation project(':infrastructure:redis')`와 함께 api-common으로 옮겨왔습니다.
  - "`@ConditionalOnWebApplication(SERVLET)` 두 건 — 재유입 방어선" 봉인은 두 auto-configuration 클래스를 대상으로 했습니다.
- **restclient AGENTS.md**: 진입 설정 `RestClientModuleAutoConfiguration`(구 `ExternalModuleAutoConfiguration` → `HttpClientModuleAutoConfiguration`)이 `com.tastyhouse.infrastructure.restclient` 한 패키지를 스캔했고 `@EnableConfigurationProperties`는 없었습니다. 형제 모듈도 자기 auto-configuration(`FirebaseModuleAutoConfiguration` 등, 당시 `PgModuleAutoConfiguration`·`MessagingModuleAutoConfiguration` 포함)으로 자기 등록한다고 적었습니다.
- **redis AGENTS.md**: `RedisModuleAutoConfiguration`(챕터 02로 `RedisModuleConfig`에서 리네임)의 "`before`를 `after`로 바꾸지 않는다" 봉인과 "auto-config는 전이 존재 = 활성화다" 절이 있었습니다. 순서 가드는 `RedisModuleAutoConfigurationTest`였습니다. "빈 정의 순서와 생성 순서는 다르다"(`before`여도 `RedisConnectionFactory`는 생성 시점에 주입된다)는 `before` 속성을 대상으로 적혀 있었습니다.
- **security-core·security-module AGENTS.md**: `SecurityModuleAutoConfiguration`이 `AutoConfiguration.imports`에 FQCN 1줄로 자기 등록하고 `@ComponentScan("com.tastyhouse.security")`로 security-core의 같은 패키지 빈까지 잡았습니다. `jwtAuthenticationFilter`의 `@ConditionalOnMissingBean(JwtAuthenticationFilter.class)`는 앱이 자기 필터를 등록해 덮을 때의 escape hatch로 서술됐습니다(그런 앱은 없었습니다). 이 조건이 `JwtTokenProvider` 모호성은 막지 못한다(해법은 `@Primary`나 한정자뿐)는 결론은 지금도 유효합니다.
- **logging-module AGENTS.md**: "`LoggingModuleAutoConfiguration`을 끄는 방법" 절은 `spring.autoconfigure.exclude`를 안내했습니다.

#### 결정

라이브러리 모듈은 스스로를 등록하지 않습니다. 각 앱 부트스트랩의 static 중첩 `ModuleScanConfig`가 라이브러리 모듈 패키지를 **문자열로** 스캔합니다.

| 항목 | before (auto-configuration) | after (imports 제거) |
|---|---|---|
| 라이브러리 모듈을 활성화하는 주체 | 각 모듈의 imports 파일 | 각 앱 부트스트랩의 static 중첩 `ModuleScanConfig` |
| imports 파일 | 19개 | 0개 |
| 설정 클래스 | `{Xxx}ModuleAutoConfiguration` 20개 | `{Xxx}ModuleConfig`(`@Configuration(proxyBeanMethods = false)`, 스캔 없음) 13개, 7개 삭제 |
| 특정 모듈 끄기 | `spring.autoconfigure.exclude` | 그 앱 `ModuleScanConfig` 목록에서 패키지를 뺀다 |
| 빈 존재 조건(`@ConditionalOnBean`/`OnMissingBean`) | 사용 | 사용 금지 |
| 환경 조건(`@ConditionalOnWebApplication`/`OnProperty`) | 사용 | 유지 |
| 공용 예외 핸들러 중복 방지 | 조건부 `@Bean("sharedGlobalExceptionHandler")` | 스캔 범위 — admin·ceo만 `apicommon.exception`을 스캔, 빈 이름 `globalExceptionHandler` |
| Redis·JPA `before` 순서 | 필수 | 불필요 — 사용자 설정은 언제나 auto-config보다 먼저 처리된다 |

#### 근거

- 앱이 infra를 `runtimeOnly`로 의존하므로 `@Import(Class)`는 불가능합니다. 문자열 스캔은 컴파일 클래스패스가 필요 없어 헥사고날 경계("앱은 어댑터를 모른다")가 유지됩니다(커밋 메시지).
- 일반 `@Configuration`에서 빈 존재 조건은 처리 순서에 좌우돼 조용히 틀립니다. 그래서 조건 대신 앱별 스캔 범위로 대체했습니다. 환경으로 판정하는 조건은 일반 설정에서도 정확해 남겼습니다.
- 사용자 설정이 auto-config보다 항상 먼저 처리되므로, 우리 `stringRedisTemplate`이 먼저 등록되고 Boot의 `@ConditionalOnMissingBean`이 물러납니다. `before` 선언이 필요 없어졌습니다.

#### 결과·감수한 손실

- 동작(HTTP·DB)은 바뀌지 않았습니다. 4앱 빈 집합 diff에서 설정 클래스 빈과 `sharedGlobalExceptionHandler`→`globalExceptionHandler`(admin·ceo, 주입처 없음) 외 차이는 0이었습니다.
- "클래스패스 존재 = 활성화" 원칙 자체는 남았습니다. 앱 `ModuleScanConfig`가 `com.tastyhouse.infrastructure`를 통째로 스캔하므로 클래스패스에 올라온 infra 모듈은 전부 활성화됩니다. 바뀐 것은 활성화를 결정하는 주체입니다.
- 삭제한 조건: rate limit aspect의 `@ConditionalOnBean(RateLimitCounterPort)` + `afterName`(스캔하는 web·admin·ceo는 전부 Redis 카운터를 가짐), JWT 필터의 `@ConditionalOnMissingBean`(앱이 자체 필터 빈을 정의하지 않음). api-common의 `afterName` 검증용 redis `testImplementation`도 함께 삭제됐습니다.
- `@RateLimit`의 앱 배선은 0줄이 아니라 `ModuleScanConfig`의 `"com.tastyhouse.apicommon.ratelimit"` 문자열 1항목이 됐습니다.
- batch에서의 재유입 방어선은 `@ConditionalOnWebApplication(SERVLET)`보다 먼저 batch `ModuleScanConfig`가 `apicommon`·`security`를 스캔하지 않는 것이 맡습니다.
- 가드: imports 재유입·스캔 목록 정확 일치·패키지 실재를 4앱 테스트(`ApplicationLayerScanConfigTest` 등)가 검사합니다. imports 재유입은 `assertNoTastyhouseAutoConfiguration`, 스캔 목록은 `assertScansModulesWithoutFilters`가 맡습니다.
- 삭제된 설정 클래스 7개는 `PersistenceModuleAutoConfiguration`·`LoggingModuleAutoConfiguration`·`ApiCommonModuleAutoConfiguration`·`RestClientModuleAutoConfiguration`·`AwsSesModuleAutoConfiguration`·`AwsSnsModuleAutoConfiguration`·`JavaMailModuleAutoConfiguration`입니다. 나머지는 `{Xxx}ModuleConfig`로 리네임돼 `@EnableConfigurationProperties`(security는 `@Bean JwtAuthenticationFilter`까지)만 남았습니다. `RedisModuleConfig`·`SecurityModuleConfig`는 챕터 02 이전 원래 이름으로 돌아간 것입니다.
- 모듈 설정 클래스는 아무것도 스캔하지 않습니다. 앱이 `com.tastyhouse.infrastructure`를 통째로 스캔하므로 같은 클래스패스의 형제 모듈 빈이 함께 등록되는 것이 정상이 됐고, 과거 "자기 패키지만 스캔한다" 봉인은 대상이 사라졌습니다. 형제 모듈을 빼려면 의존(조립 스타터의 `runtimeOnly`)에서 빼야 하며, 모듈에 `@ComponentScan`을 되살리면 앱 스캔과 이중이 됩니다. jpa 모듈만 골라 스캔에서 빼는 수단도 없어졌습니다(4앱 전부 DB를 쓰므로 필요도 없습니다).
- 특정 모듈을 끄는 방법이 `spring.autoconfigure.exclude`에서 "그 앱 `ModuleScanConfig`에서 패키지를 빼고 그 앱 `ApplicationLayerScanConfigTest` 기대 목록도 함께 고친다"로 바뀌었습니다(logging 등).
- 벤더 설정이 일반 `@Configuration`이 되어 `--debug` 조건 리포트에 나오지 않습니다. 배선 확인은 `--logging.level.org.springframework.beans.factory.support=DEBUG`의 autowiring 로그로 합니다(pg 문서).
- 테스트 정리: redis의 `RedisModuleAutoConfigurationTest`는 `RedisModuleConfigTest`로 리네임되어 `ourStringRedisTemplateWinsOverBootDefault`(`withUserConfiguration(RedisModuleConfig.class, RedisConfig.class)` + `AutoConfigurations.of(RedisAutoConfiguration.class)`) 한 건이 순서를 지킵니다. 이 모듈의 빈을 다시 auto-configuration으로 등록하면 순서 문제가 되살아나므로, 그것도 imports를 되살리지 않는 이유입니다. api-common의 `ApiCommonAutoConfigurationTest`는 `ApiCommonRateLimitConfigTest`로 옮겨지며 `rateLimitAspectRegisteredInServletContext`·`rateLimitAspectAbsentInNonServletContext` 2건만 남고 4건(`afterName` 검증·핸들러 back-off·aspect 빈 존재 조건 등)이 삭제됐습니다. api-common `LayerRulesTest`의 클래스 수 하한도 14 → 13으로 내려갔습니다.
- JWT 필터 빈 존재 조건이 사라져, 앱이 자기 필터를 등록하면 이름이 겹쳐 기동이 실패합니다(조용히 덮이지 않음). 예외 핸들러도 같습니다 — web이 실수로 `apicommon.exception`을 스캔하면 자체 핸들러와 기본 이름 `globalExceptionHandler`가 겹쳐 기동이 실패하고, 그 전에 `ApplicationLayerScanConfigTest`가 실패합니다. 원래 `sharedGlobalExceptionHandler` 봉인의 취지("조용히 덮이지 않는다")가 이렇게 유지됩니다.
- restclient에 Properties record를 다시 추가한다면 루트 패키지에 `RestClientModuleConfig`(`@Configuration(proxyBeanMethods = false)`, 스캔 없음)를 새로 두고 그 `@EnableConfigurationProperties`에 명시합니다.

<a id="decision-0040"></a>

### 0040. 내부 구현 클래스를 package-private으로 좁힌다(벤더 wire DTO는 루트 평면 배치)

- 상태: 채택
- 결정 커밋: `151eadb88` (2026-10-04)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0040-package-private-visibility.md`

#### 맥락

- **package-private**: 자바에서 클래스 선언에 `public`을 붙이지 않은 상태입니다. 같은 패키지 안에서만 그 클래스를 이름으로 쓸 수 있고, 다른 패키지에서 import하면 컴파일 에러가 납니다.
- **wire DTO**: 외부 API(카카오·토스 등)와 주고받는 JSON을 그대로 옮긴 요청·응답 record입니다.

결정 전에는 엔티티·JPA 리포지토리·어댑터·UseCase 구현 서비스·리스너·컨트롤러·설정·스케줄러가 모두 `public`이었습니다. 원래는 이유가 있었습니다. 모듈이 자기 `@Configuration`을 `AutoConfiguration.imports`로 등록하고 다른 모듈이 구체 타입을 import해 조립했으며, 협력 서비스가 구체 클래스를 주입받았습니다. 그런데 [결정 0038](#decision-0038)(문자열 패키지 스캔 조립)과 [결정 0033](application-core/AGENTS.md#decision-0033)(UseCase 인터페이스 주입)으로 두 이유가 모두 사라졌습니다.

#### 이전 규칙

##### backend/CLAUDE.md — infrastructure 패키지 규칙 (루트 = 모듈명)

"벤더: 외부 API의 요청·응답 wire DTO는 `{루트}.dto`에 둔다." — [결정 0037](#decision-0037)이 같은 날 정한 하위 패키지 배치입니다.

##### 벤더 모듈 AGENTS.md

- kakao·naver·apple·facebook-oauth: 루트 통일 후 wire DTO는 하위 패키지 `com.tastyhouse.infrastructure.{vendor}.oauth.dto`로 모였습니다.
- solapi: `request/`·`response/` 두 하위 패키지를 `dto/` 하나로 합쳐 다른 벤더의 `dto/` 관례와 맞췄습니다(`SolapiMessageRequest`·`SolapiMessageResponse`).
- tosspayments·bbq: `dto/` 구성은 루트 통일 후에도 불변이었습니다(bbq는 `BbqMenuCategoryResponse`·`BbqMenuResponse`·`BbqMenuSubOptionResponse`).
- oauth "벤더 추가·제거 절차": 신설 벤더는 wire DTO를 `.dto` 하위에 두었습니다.

##### CLAUDE.md — 리포지토리 구조 (infrastructure 중첩 프로젝트 문단)

"벤더의 외부 API wire DTO는 `{루트}.dto`에 둡니다."

##### 클래스 선언의 접근 제어자

명시된 규칙이 없었고, 내부 구현 클래스도 관례적으로 `public`이었습니다.

#### 결정

다른 패키지가 이름으로 참조하지 않는 내부 구현 클래스는 최상위 `public`을 붙이지 않습니다. 클래스 선언의 `public`만 지우고, 생성자·메서드는 그대로 둡니다.

| 항목 | before | after |
|---|---|---|
| 엔티티·JPA 리포지토리·어댑터·UseCase 구현 서비스·리스너·컨트롤러·설정·스케줄러 | `public class`/`public interface` | package-private (854개) |
| 벤더 wire DTO 16개 | `{루트}.dto`의 `public record` | `{루트}`로 이동 + package-private |
| 벤더 모듈의 하위 패키지 | `dto/` | 없음 — Client·Adapter·Properties·Config·DTO가 루트에 평면 |
| `public` 유지 대상 | — | 다른 패키지가 참조하는 7개, `Projections.constructor` 대상 record, `BaseEntity`·Embeddable, Request/Response, 공유 도메인 서비스 |
| 가드 | 없음 | 각 모듈 ArchUnit에 가시성 규칙 + 허용 목록 낡음 검사(예: `VendorLayerRulesTest#topLevelClassesShouldNotBePublic`) |

#### 근거

- "이건 내부 구현이니 직접 쓰지 말라"는 약속을 문서·리뷰가 아니라 컴파일러가 지키게 하려는 것입니다. 다른 패키지에서 import하는 순간 컴파일이 실패합니다.
- 조립은 문자열 패키지 스캔이고 스캔은 접근 제어자를 보지 않습니다. 주입은 포트 인터페이스로만 합니다. Spring 스캔·CGLIB 프록시·Spring Data 리포지토리·`@ConfigurationProperties`·Hibernate·Jackson 모두 package-private 클래스로 동작합니다.
- package-private 타입은 같은 패키지에서만 보입니다. 그래서 wire DTO는 그것을 쓰는 `*Client`와 같은 패키지에 있어야 하고, `dto/` 하위 패키지에 둘 수 없습니다.

#### 결과·감수한 손실

- 동작은 바뀌지 않았습니다. 4앱 싱글턴 빈 이름 집합과 api-docs 경로 수가 변경 전과 같았습니다.
- "wire DTO는 `{루트}.dto`" 규칙은 [결정 0037](#decision-0037) 커밋 직후 같은 날 뒤집혔습니다.
- UseCase를 구현하지 않는 협력 서비스(약 48개는 다른 패키지에서 import)는 이 결정에서 후속 과제로 남았고, [결정 0054](application-core/AGENTS.md#decision-0054)가 마무리했습니다.
- wire DTO를 옮겨도 클래스명은 그대로라 소셜 벤더 클라이언트의 빈 이름(`kakaoOAuthClient` 등, 소비 측 `@Qualifier`)은 불변입니다. solapi의 두 record는 `SolapiSmsClient`와, tosspayments의 wire DTO는 `TossPaymentClient`와 같은 패키지의 package-private 타입이 됐습니다.
- logging-module은 이 가드 때문에 archunit 테스트 의존이 추가됐습니다.

<a id="decision-0042"></a>

### 0042. 에러코드·예외를 그것을 던지는 가장 안쪽 모듈이 소유하도록 분할한다

- 상태: 채택
- 결정 커밋: `b188384ee` (2026-10-05)
- 대체한 결정: [결정 0025](#decision-0025)(ErrorContracts 미러링 부분)
- 원문: 삭제 전 git 이력의 `docs/adr/0042-error-code-module-split.md`

#### 맥락

- **에러코드**: HTTP 상태·`errorCode` 문자열·메시지를 묶은 enum 상수입니다. 프론트는 `errorCode`로 분기하므로 이 값은 **wire 계약**(바꾸면 클라이언트가 깨지는 약속)입니다.
- **`BusinessException`**: 에러코드를 담아 던지는 예외이고, 표현 계층이 이것을 `ProblemDetail` 응답으로 바꿉니다.

결정 전에는 단일 enum `domain.exception.ErrorCode`(442개)가 모든 에러를 담았습니다. 실측해 보니 domain 모듈이 도메인 불변식이 아닌 유스케이스 실패(조회 실패·인가·중복) 289개와, 앱 하나만 쓰는 코드 180개까지 소유하고 있었습니다. 변경 이유가 다른 것이 한 파일에 모여 있었고, 앱 모듈 재분할([결정 0036](#decision-0036))로 얻은 "다른 앱 것을 쓰면 컴파일 에러" 게이트가 에러코드에만 적용되지 않았습니다.

#### 이전 규칙

##### backend/CLAUDE.md — 예외·에러코드 소유 규칙 (`ErrorCodeSpec` 공통 계약 + `BusinessException` 단일 계층)

- **카탈로그는 하나**: `ErrorCode`를 도메인(컨텍스트)별로 쪼개지 않았습니다. "상수 230여 개의 단일 카탈로그"로 서술됐습니다.
- **`ErrorCodeSpec`**: `domain.exception`의 순수 Java 인터페이스였습니다. 구현체는 `ErrorCode` 하나뿐이었고, "필요하면 domain이 모듈별 에러 카탈로그를 다시 호스트할 수 있는 확장점"으로 유지했습니다.
- **새 예외 타입을 만들지 않습니다**: 생성자를 위임만 하는 서브클래스를 금지했습니다. 근거는 과거 `ExternalApiException`이 생성자 위임뿐인 빈 서브클래스였다는 점이었습니다.
- **외부 연동 실패 코드도 단일 카탈로그 `ErrorCode`가 소유한다**: 원래는 외부 연동 전용 `ExternalApiErrorCode`(`infrastructure:http-client` 소유)가 따로 있었습니다. 그 탓에 admin-api·ceo-api가 502로 의도된 외부 연동 실패를 500으로 응답하는 결함이 있었습니다. 그래서 `ExternalApiException`·`ExternalApiErrorCode`를 삭제하고, 상수 5개를 code·상태(502)를 유지한 채 `ErrorCode`로 옮겼습니다(`b02f83ecc`).
- **표현 계층이 직접 쓰는 에러코드 상수는 `ErrorContracts`가 미러링한다**([결정 0025](#decision-0025) 덩어리 01): `application.shared.error.ErrorContracts`가 문자열 미러 3개를 제공하고 `ErrorContractsConsistencyTest`가 일치를 검사했습니다. 필터는 `ErrorContracts.authRequired()`·`accessDenied()`를 썼습니다.
- **`ErrorCodeConventionTest`**(`domain` 순수 단위 테스트)가 카탈로그 규약을 지켰습니다.
- `ResourceNotFoundException`은 domain에 있었습니다(domain은 이것을 던진 적이 없습니다).
- domain·application 코드를 함께 던지는 메서드의 당시 예: `web-application`의 `payment/service/PaymentQueryService.java`가 `validateOwnership`을 오버로드 2개로 나눠 가졌습니다.

##### backend/CLAUDE.md — 외부 연동 실패 코드 / `ErrorCodeSpec` 유지 근거 (restclient 문서에서 이관된 이력 포함)

- 외부 연동 상수 5개(`SMS_SEND_NO_RESPONSE`·`SMS_SEND_FAILED`·`SMS_SEND_API_ERROR`·`MAIL_SEND_FAILED`·`ADMIN_DONG_BOUNDARY_FETCH_FAILED`)는 도메인 `ErrorCode`(`backend/domain/src/main/java/com/tastyhouse/domain/exception/ErrorCode.java`)에 있었고, 어댑터(`SolapiSmsClient`·`JavaMailAdapter`·`SesMailSender`·`SnsSmsSender`·`AdminDongBoundaryClient`)가 `new BusinessException(ErrorCode.X[, cause])`를 직접 던졌습니다.
- `ErrorCodeSpec`을 폐지하지 않은 당시 이유: `BusinessException`의 필드·생성자·`getErrorCode()`가 `ErrorCodeSpec` 타입이어야 하고(`ErrorCode`로 좁히면 API 변경), `ErrorCodeConventionTest`의 계약 검증 케이스 1건도 필요했습니다. 두 `GlobalExceptionHandler`는 `e.getErrorCode().getCode()`/`getHttpStatusCode()`만 호출해 구체 타입을 몰랐습니다.

##### backend/CLAUDE.md — DTO 조립 규칙 (Command 가드)

Command record의 검증 가드는 `BusinessException(ErrorCode.INVALID_INPUT)`(domain)을 던졌습니다. "예외는 계층 칸이 없는 횡단 관심사"라는 이유로 `commandRecordsShouldBeBoundaryTyped`·`BatchSchedulerRulesTest#inboundPortsShouldBeBoundaryTyped`가 `domain.exception..`을 carve-out으로 허용했습니다.

##### 모듈 AGENTS.md

restclient 문서에 "예외·에러코드를 두지 않는 이유" 절이 있었습니다. bbq·firebase·admdongkor 등 벤더 문서는 실패 코드를 단일 `ErrorCode` 소유로 서술했습니다. 예를 들어 bbq의 `RemoteImageDownloader`는 과거 `BusinessException(ErrorCode.FILE_SIZE_EXCEEDED)`를 직접 던졌고, admdongkor는 `BusinessException(ErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED)`를 던진다고 서술돼 있었습니다. web-api 문서는 401도 `BusinessException`에 `ErrorCode.AUTH_*`를 담는다고 적었습니다.

- `backend/infrastructure/restclient/AGENTS.md` — 어댑터 작성 규칙: "에러는 도메인 `BusinessException(ErrorCode.X[, cause])`을 직접 던진다. 모듈마다 예외 타입을 새로 만들고 전역 핸들러에 `@ExceptionHandler`를 추가하지 않는다. `RestClientResponseException`을 catch해 이 형태로 번역한다"였습니다.
- `backend/infrastructure/bbq/AGENTS.md` — Internal: `domain`을 예외 계약(`BusinessException`·`ErrorCode`)용으로 `implementation` 의존한다고 적었습니다.
- `backend/infrastructure/firebase/AGENTS.md` — Internal: `FirebaseFileStorage#delete`가 `FILE_DELETE_FAILED`를 던지는 대신 `FileDeleteResult`를 반환한다고 서술하며, 그 상수가 단일 `ErrorCode`에 남아 있었습니다.
- `backend/infrastructure/redis/AGENTS.md` — rate limit 응답은 단일 `ErrorCode.RATE_LIMIT_EXCEEDED`(덩어리 01 이후 `ErrorContracts#rateLimit`)로 조립된다고 적었습니다.
- `backend/api-common-module/AGENTS.md` — `RateLimitException` 항목: `handleRateLimitException`이 `ErrorContracts#rateLimit`의 code·message로 응답을 조립했습니다. 필터·핸들러가 쓰는 에러 상수의 출처는 `ErrorContracts`(`rateLimit()`·`accessDenied()`·`authRequired()`)였습니다.
- `backend/security-module/AGENTS.md` — `JwtAuthenticationEntryPoint`/`JwtAccessDeniedHandler`의 401·403 출처는 `ErrorContracts.authRequired()`/`accessDenied()`였고, 이 모듈의 의존은 `application`뿐이었습니다.
- `backend/domain/AGENTS.md` — 카탈로그 가드는 `backend/domain/src/test/java/com/tastyhouse/domain/exception/ErrorCodeConventionTest.java`였습니다. 에러 카탈로그 규약 표에는 `INVALID_INPUT`(400, Command compact constructor 가드 전용)과 `ENTITY_NOT_FOUND`(fallback — 도메인별 전용 `*_NOT_FOUND`가 있으면 그쪽을 쓴다)가 domain 상수로 있었습니다.
- `backend/application-core/AGENTS.md` — `commandRecordsShouldBeBoundaryTyped`의 carve-out은 덩어리 01 이후 2건(`domain.exception..`·`MultipartFile`)이었고, `domain.exception..`은 "Command가 `application`에 살아 여전히 domain을 보고, compact constructor 가드가 `BusinessException`을 던진다"는 이유로 유지됐습니다. batch 엄격판 `BatchSchedulerRulesTest#inboundPortsShouldBeBoundaryTyped`는 `com.tastyhouse.domain.exception` 하나만 carve-out으로 허용했습니다. 앱 네임스페이스 View 표의 `product/port/out/ProductAvailabilityChangeView.java`는 domain `product/model`의 `ProductAvailabilityChangeResult`와 짝을 이뤘습니다.

##### docs/domain/ceo.md — 점주 이력 조회 보관 기간

보관 기간 위반 에러코드 4개는 단일 `ErrorCode`가 소유했습니다(지금은 ceo 전용이라 `CeoErrorCode`).

#### 결정

에러코드와 예외는 그것을 던지는 가장 안쪽 모듈이 소유합니다.

| 항목 | before | after |
|---|---|---|
| domain 에러코드 | `domain.exception.ErrorCode` 442개 | `domain.exception.DomainErrorCode` 153개 |
| core 에러코드 | 없음 | `application.shared.exception.ApplicationErrorCode` 84개 |
| 앱 에러코드 | 없음 | 같은 패키지(split package) `WebErrorCode` 86 · `AdminErrorCode` 28 · `CeoErrorCode` 65 · `BatchErrorCode` 1 — 각 `{앱}-application` 모듈 |
| api 에러코드 | `ErrorContracts`(문자열 미러 3개) | `apicommon.exception.ApiErrorCode`(`RATE_LIMIT_EXCEEDED`·`ACCESS_DENIED`·`AUTH_REQUIRED`). `ErrorContracts` 삭제 |
| infrastructure 에러코드 | 없음 | 없음 — 벤더는 실패를 결과 record로 돌려주고 application이 번역한다 |
| 기반 예외 | `BusinessException`(구체, public 생성자) | `abstract BusinessException`(protected 생성자 3개, `getErrorCode(): ErrorCodeSpec`) |
| 계층별 예외 | `BusinessException` 직접 | `DomainException`(`DomainErrorCode`만) · `ApplicationException`(`ApplicationErrorCodeSpec`만) |
| `ResourceNotFoundException` | domain | `application.shared.exception`, `extends ApplicationException` |
| 죽은 상수 | 25개(던지는 곳 0) | 23개 삭제, `ACCESS_DENIED`·`RATE_LIMIT_EXCEEDED`는 `ApiErrorCode`로 이동 |
| Command 가드 | `BusinessException(ErrorCode.INVALID_INPUT)`, `domain.exception..` carve-out | `ApplicationException(ApplicationErrorCode.INVALID_INPUT)`, carve-out 제거 — `domain..` 전면 금지 |
| 규약 가드 | `ErrorCodeConventionTest`(domain) | `ErrorCatalogConventionTest`(7개 카탈로그) + `ErrorCatalogSnapshotTest` |

#### 근거

- **변경 이유와 컴파일 게이트**: 어느 계층·어느 앱의 실패인가가 변경 이유입니다. 컨텍스트 축(주문·쿠폰별)으로 나누는 것은 여전히 금지이며, 이번 분할은 계층·모듈 축입니다.
- **계층별 예외는 "빈 서브클래스"가 아닙니다**: `DomainException`과 `ApplicationException`은 받는 코드 타입이 달라 "도메인이 앱 코드를 던짐", "web 서비스가 ceo 코드를 던짐"이 컴파일 에러가 됩니다. 과거 `ExternalApiException`은 생성자 위임뿐이고 타입 차이가 없었습니다. "새 예외 타입을 만들지 않는다" 항목은 이 차이를 근거로 뒤집혔습니다.
- `INVALID_INPUT`은 application 모듈들만 던지는 유스케이스 실패라 `ApplicationErrorCode`로 옮겨졌습니다. 그 결과 Command의 `domain.exception` carve-out을 받던 참조가 0건이 되어 규칙을 강화했습니다.

#### 결과·감수한 손실

- **wire 계약 불변**: HTTP status·`errorCode`·message는 `ErrorCatalogSnapshotTest`가 분할 전 스냅샷(`application-core/src/test/resources/error-catalog-before.tsv`)과 419행 전수 비교로 보증합니다.
- 생성자를 위임만 하고 받는 타입이 같은 서브클래스는 여전히 만들지 않습니다. 전용 `@ExceptionHandler`를 모듈마다 추가하지 않는다는 규칙도 그대로입니다.
- 필터 단계의 참조는 `ErrorContracts.authRequired()`·`accessDenied()` → `ApiErrorCode.AUTH_REQUIRED`·`ACCESS_DENIED`로 바뀌었고 값은 같습니다.
- `ProductAvailabilityFailure`·`ChangeResult`는 ceo-application으로 옮겼습니다. `ProductAvailabilityFailure`는 throw하지 않고 응답으로 변환되는 record라 `ErrorCodeSpec` 컴포넌트를 쓰는 봉인 예외 1건입니다.
- `ErrorContractsConsistencyTest`는 `ErrorResponsesTest`로, domain의 `ErrorCodeConventionTest`는 `ErrorCatalogConventionTest`로 대체됐습니다. `AUTH_REQUIRED` 미러의 일치는 `ErrorCatalogConventionTest#mirroredCodesStayIdentical`이 보증합니다.
- 대가로 `security-module`이 `ApiErrorCode`를 읽으려고 `api-common-module`을 의존하게 됐습니다(`security-module → api-common-module`). `security-module` `LayerRulesTest#shouldNotDependOnDomain`의 `.because`도 갱신됐습니다.
- `ENTITY_NOT_FOUND`는 호출부가 없어 삭제됐고, firebase의 `FILE_DELETE_FAILED`도 단일 `ErrorCode`와 함께 사라져 어느 enum에도 없습니다. bbq는 `domain` 의존이 없어졌습니다(`restclient`·`batch-application`만).
- 오버로드 당시 예였던 `PaymentQueryService#validateOwnership`은 이후 유스케이스 분리([결정 0043](application-core/AGENTS.md#decision-0043))로 `PaymentDetailQueryService`(`ApplicationErrorCode.PAYMENT_ACCESS_DENIED`)와 `PaymentByOrderQueryService`(`DomainErrorCode.ORDER_ACCESS_DENIED`)가 각자 하나씩 던지게 되어 오버로드가 필요 없어졌습니다.
- 같은 커밋에서, 문서가 안내하던 `backend/import_order.py`가 저장소에 커밋된 적이 없다는 사실을 정정하고 안내를 지웠습니다.

<a id="decision-0049"></a>

### 0049. 인증 컨텍스트 패키지를 phoneverification·emailverification으로 바꾸고 타입·식별자 어휘(mail/sms)는 유지한다

- 상태: 채택
- 결정 커밋: `456db1b13` (2026-10-09) — 커밋 메시지 3번째 줄 `refactor(verification)`
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0049-verification-context-rename.md`

#### 맥락

- **컨텍스트 패키지**: `domain`·`application`·`infrastructure.jpa`·`webapi` 아래의 1단계 하위 패키지(`order`, `shop`, `member` 등)로, 하나의 업무 영역을 뜻합니다. 같은 이름이 계층마다 반복돼 한 업무의 코드를 계층별로 찾아갈 수 있습니다.

휴대폰번호 인증과 이메일 인증만 업무 이름이 아니라 **발송 수단(기술) 이름** `sms`·`mail`을 컨텍스트 이름으로 쓰고 있었습니다. 같은 이름이 `infrastructure:sms`·`infrastructure:mail`(발송 벤더를 조립하는 채널 모듈)에도 있어, 이름만으로 업무 패키지인지 기술 모듈인지 구분되지 않았습니다(`docs/tasks/verification-context-rename/backend.md`).

#### 이전 규칙

##### backend/CLAUDE.md — 채널 도메인 어휘 통일 규칙 (`mail`/`sms`)

이 규칙은 `864b13b37`(2026-08-01)에서 시작됐습니다. "발송 채널이 곧 바운디드 컨텍스트인 도메인은 **패키지명과** 자바 식별자를 같은 어휘(`mail`/`sms`)로 통일한다"였습니다. 과거 `verification` 컨텍스트 하나가 이메일 인증과 휴대폰 인증을 함께 담았지만, 두 관심사는 애그리거트·리포지토리·출력 포트·이벤트·테이블이 처음부터 완전히 분리돼 있었고 공유하던 것은 `VerificationCode` VO 하나뿐이었습니다. 그래서 둘을 나누면서 패키지와 타입 모두 채널 어휘를 쓰게 했습니다.

- 나눈 근거에는 "`external-api`가 이미 `email/`·`sms/`로 갈라져 있어 도메인 쪽만 통합 상태로 남은 불일치"도 있었습니다.
- 당시 reference 구현의 패키지 경로는 채널 어휘였습니다: `domain`의 `mail/domain/`(`MailVerification`·`MailVerificationPurpose`·`MailVerificationMessage`·`MailSender`)·`sms/domain/`(`SmsVerification`·`SmsVerificationMessage`·`SmsSender`), `web-api`의 `mail/MailVerificationApiController`·`sms/SmsVerificationApiController`, 서비스는 `application/mail/service/MailVerificationService`·`application/sms/service/SmsVerificationService`, 발송 포트는 `web-application`의 `{mail,sms}.port.out`.
- 같은 규칙의 테이블 항목은 처음에는 "`ddl-auto=validate` 환경이라 RENAME은 DDL 마이그레이션과 앱 배포가 원자적이어야 하므로 순수 코드 리팩터에 그 운영 리스크를 들이지 않는다"는 이유로 `EMAIL_VERIFICATION`/`PHONE_VERIFICATION`을 유지했습니다. 이후 사용자 결정으로 `alter.sql`에 `RENAME TABLE`·`RENAME INDEX`를 추가해 `MAIL_VERIFICATION`/`SMS_VERIFICATION`(인덱스 `idx_mail_verification_*`/`idx_sms_verification_*`)으로 바꿨습니다. 이 테이블·인덱스명은 이번 결정에서도 불변입니다.

##### backend/CLAUDE.md — infrastructure 패키지 규칙

jpa의 인증 컨텍스트 패키지는 `com.tastyhouse.infrastructure.jpa.{mail,sms}`였습니다.

#### 결정

컨텍스트 패키지는 업무 이름을 쓰고, 기술 이름(`sms`·`mail`·`oauth`·`pg`)은 infrastructure 모듈명에만 남깁니다. 클래스명·테이블·URL·에러코드 등 **타입·식별자 어휘는 그대로** `Mail*`/`Sms*`입니다.

| 계층(모듈) | before | after |
|---|---|---|
| domain | `com.tastyhouse.domain.sms` / `.mail` | `com.tastyhouse.domain.phoneverification` / `.emailverification` |
| application(코어)·web-application | `com.tastyhouse.application.sms` / `.mail` | `com.tastyhouse.application.phoneverification` / `.emailverification` |
| infrastructure:jpa | `com.tastyhouse.infrastructure.jpa.sms` / `.mail` | `com.tastyhouse.infrastructure.jpa.phoneverification` / `.emailverification` |
| web-api | `com.tastyhouse.webapi.sms` / `.mail` | `com.tastyhouse.webapi.phoneverification` / `.emailverification` |
| 도메인 문서 | `docs/domain/sms.md` / `mail.md` | `docs/domain/phoneverification.md` / `emailverification.md` |
| 클래스명·테이블·URL·에러코드·Redis 키·infra 모듈명 | `SmsVerification`·`MAIL_VERIFICATION`·`/api/sms-verifications` 등 | 불변 |

#### 근거

- 패키지 이름이 기술 모듈명과 겹치면, 그 패키지가 업무 영역인지 기술 어댑터인지 이름만으로 알 수 없습니다.
- 타입·식별자 어휘까지 바꾸면 테이블 RENAME·URL·에러코드 문자열 같은 wire 계약과 운영 리스크가 따라옵니다. 패키지만 옮기면 import 치환으로 끝나는 리팩터링입니다.

#### 결과·감수한 손실

- 동작은 바뀌지 않았습니다. 디렉터리 18개, java 파일 60개를 옮겼고 `schema.sql`·DDL 변경은 없습니다.
- 채널 어휘 통일 규칙 중 **패키지 어휘에 관한 부분만** 바뀌었고, 타입·식별자 어휘 규칙(`Mail*`/`Sms*`, 값 이름은 `email`/`phoneNumber`, `@RateLimit keyPrefix` 개명 금지 등)은 유효합니다.
- 문자열 FQN을 쓰는 `ServiceContextBoundaryTest.SEALED_VIOLATIONS`의 `MailVerificationService` 항목도 새 패키지로 바뀌었습니다.
- jpa의 인증 컨텍스트가 `jpa.{emailverification,phoneverification}`이 되어, 예약된 스타터 이름(`mail`·`sms`)과 애초에 충돌하지 않습니다.

<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-02 -->

# api-common-module

## Purpose
`web-api`·`admin-api`·`ceo-api`가 공유하는 **HTTP 경계 공용 플럼빙 라이브러리 모듈**(`java-library`). 세 모듈에 **package 선언 1줄만 다른 완전 복제**로 존재하던 응답 래퍼·페이징 요청·전역 예외 핸들러·rate limit 표현 계약을 단독 소유한다.

복제를 방치하면 한쪽만 고쳐지는 드리프트가 생긴다. 이 모듈은 domain 어댑터가 아니라 presentation 공유 유틸이므로 `security-module` 선례대로 `implementation`으로 노출되고 `bootJar`가 비활성이다([ADR-0003](../../docs/adr/0003-api-common-shared-plumbing.md)).

**통합 판정 기준은 "완전 동일"뿐이다** — 의미 있는 차이가 한 줄이라도 있으면(응답 계약·`@Schema` 문구 포함) 통합하지 않는다. 유지되는 복제 목록과 그 사유는 루트 [CLAUDE.md](../CLAUDE.md)의 "api 모듈 공용 플럼빙 소유 규칙" 절에 표로 관리한다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + web/validation/aop starter, springdoc. 내부 의존은 `application-core`(**api** — 공개 시그니처에 `PageResult` 노출. 핸들러는 `ErrorResponses`를 쓰고, 필터·핸들러 에러 상수는 이 모듈의 `ApiErrorCode`다)와 `security-core`(implementation)다. **`domain`과 `infrastructure:redis`는 의존하지 않는다.** `bootJar` 비활성 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/apicommon/common/` | `ApiResponse<T>`(성공 응답 + `Pagination`), `PaginationResponse<T>`(표준 4필드 페이징), `PageRequest`(`@ModelAttribute` 페이징 요청), `ClientIpResolver` |
| `src/main/java/com/tastyhouse/apicommon/exception/` | `GlobalExceptionHandler`(`@RestControllerAdvice`) — admin-api·ceo-api 부트스트랩의 중첩 `ModuleScanConfig`가 이 패키지를 스캔해 직접 등록한다(빈 이름 `globalExceptionHandler`). web-api는 자체 핸들러가 있어 이 패키지를 스캔하지 않는다. `ApiErrorCode`·`ProblemDetails`도 여기 있다 |
| `src/main/java/com/tastyhouse/apicommon/ratelimit/` | rate limit **표현 관심사 전부** — `@RateLimit`·`RateLimitKeyType`·`RateLimitAspect`(키 조립: IP·요청 필드 해석)·`RateLimitException`·`ApiCommonRateLimitConfig`. 카운터 계약 `RateLimitCounterPort`는 **`security-core`(`com.tastyhouse.security.ratelimit`) 소유**(아래 [security-core에 두는 이유](#ratelimitcounterport를-security-core로-옮긴-이유)), 카운터 구현은 `infrastructure:redis`의 `RedisRateLimitCounter` |

## 등록 방식 — 앱 `ModuleScanConfig`의 패키지 스캔

이 모듈은 스스로를 등록하지 않는다(imports 파일 없음). 각 앱 부트스트랩의 중첩 `ModuleScanConfig`가 이 모듈의 패키지를 **문자열로 골라 스캔**하고, 앱별 차이는 **스캔 범위**로 표현한다. 빈 존재 조건(`@ConditionalOnMissingBean`/`@ConditionalOnBean`)은 일반 `@Configuration`에서 처리 순서에 좌우돼 믿을 수 없어 쓰지 않는다([ADR-0038](../../docs/adr/0038-autoconfiguration-imports-removal.md)).

| 대상 | 등록 방식 |
|---|---|
| 예외 핸들러 `GlobalExceptionHandler`(`@RestControllerAdvice`) | admin·ceo의 `ModuleScanConfig`가 `com.tastyhouse.apicommon.exception`을 스캔해 직접 등록한다(빈 이름 `globalExceptionHandler`, 주입처 없음). web은 그 패키지를 스캔하지 않는다 |
| rate limit aspect | **`ApiCommonRateLimitConfig`**(`@Configuration(proxyBeanMethods = false)` + `@ConditionalOnWebApplication(SERVLET)`)의 `@Bean RateLimitAspect`. web·admin·ceo의 `ModuleScanConfig`가 `com.tastyhouse.apicommon.ratelimit`을 스캔해 등록한다(스캔하는 세 앱은 전부 Redis 카운터를 갖는다) |
| 앱의 배선 | 각 앱 `ModuleScanConfig`의 패키지 문자열 1~2항목. 문자열이라 `implementation` 의존 그대로 컴파일 게이트와 무관하다 |

- **web 자체 핸들러와 공용 핸들러가 조용히 겹치지 않는다** — 둘 다 기본 빈 이름이 `globalExceptionHandler`이므로, web이 실수로 `apicommon.exception`을 스캔하면 이름이 겹쳐 스캔 시점에 기동이 실패한다.

## 앱별 등록 현황

"어느 앱이 어느 패키지를 스캔하는가"가 런타임 동작을 결정한다.

| 앱 | `globalExceptionHandler`(공용) | `rateLimitAspect` |
|---|---|---|
| `AdminApiApplication` / `CeoApiApplication` | 등록 — `apicommon.exception` 스캔 | 등록 — `apicommon.ratelimit` 스캔 |
| `WebApiApplication` | 미등록 — 스캔 안 함(같은 이름의 빈은 web 자체 `webapi.exception.GlobalExceptionHandler`) | 등록 — 스캔 |
| `BatchApplication` | 미등록 — 스캔 안 함(jar도 클래스패스에 없다) | 미등록 — 동일 |

목록은 각 앱 `ApplicationLayerScanConfigTest`의 `assertScansModulesWithoutFilters`가 정확 일치로 검사한다.

## 여기에 두면 안 되는 것
- **모듈마다 내용이 다른 정책 파일** — `SecurityConfig`(필터체인·인가 정책), `PublicPaths`(공개 경로 목록: web 18줄 vs admin/ceo 3줄), `TokenService`/`AuthService`(인증 주체 `Admin`/`Ceo`와 JWT 시크릿이 분리되어야 함).
- **필드 셋이나 `@Schema` 문구가 다른 응답 record** — 예: `ShopDetailResponse`(admin은 감사 시각, ceo는 `trademarkImageUrl`/`hidden`), `ShopAmenityResponse`("가게" vs "내 가게").
- **도메인 포트가 있는 기술 어댑터** — 그것은 외부 연동 모듈(`infrastructure:restclient` 코어 + `infrastructure:{firebase,aws-s3,aws-ses,aws-sns,kakao-oauth,naver-oauth,apple-oauth,facebook-oauth,pg,tosspayments,mail,javamail,sms,solapi,bbq,admdongkor}` 어댑터)이나 `infrastructure:jpa`(DB 어댑터) 소관이다.
- **기술 구현체** — 이 모듈은 rate limit의 표현(애노테이션·aspect·예외)만 두고, 카운터 계약(`RateLimitCounterPort`)은 `security-core`가, 구현은 인프라 모듈이 맡는다. 표현 계층이 인프라 모듈을 의존하면 표현 → 구현 역방향 의존이 생긴다.

## Dependencies

### Internal
- `application-core` (**api**). `PaginationResponse.from(PageResult<T>)`의 공개 시그니처에 `com.tastyhouse.application.shared.port.out.page.PageResult`가 노출되므로 `api`로 둔다.
  - **표현 계층은 `domain`을 보지 않는다.** 예외 판정은 `com.tastyhouse.application.shared.error.ErrorResponses#resolve`, 표현 계층이 직접 쓰는 에러 상수는 이 모듈의 `com.tastyhouse.apicommon.exception.ApiErrorCode`(`RATE_LIMIT_EXCEEDED`·`ACCESS_DENIED`·`AUTH_REQUIRED`)다. `application-core`는 `domain`을 `implementation`으로만 가지므로 presentation 컴파일 클래스패스에 `domain`이 없다([ADR-0025](../../docs/adr/0025-presentation-domain-cut.md)).
  - `domain`이 되돌아오는 회귀는 `src/test/java/com/tastyhouse/apicommon/architecture/LayerRulesTest.java` → `shouldNotDependOnDomain`이 잡는다(이 모듈 전체 ✗ `com.tastyhouse.domain..`).
- `security-core` (implementation) — `RateLimitAspect`·`ApiCommonRateLimitConfig`가 쓰는 `RateLimitCounterPort`의 소유 모듈. `api`로 노출하지 않는다 — web·admin·ceo는 `security-module`의 `api project(':security-core')`로 이미 이 모듈을 받는다. 부수적으로 compileClasspath에 `jjwt-api`가 security-core를 통해 전이로 실리는데 허용 범위다(`spring-security-core`는 원래 직접 선언돼 있다).
- **`infrastructure:redis`는 main·test 어느 클래스패스에도 없다.** 방향은 **`infrastructure:redis` → `security-core` ← `api-common-module`** 이다 — redis도 이 모듈을 모르고, 이 모듈도 redis를 모른다([ADR-0023](../../docs/adr/0023-rate-limit-port-in-security-core.md)).

### External
- `spring-boot-starter-web`·`spring-boot-starter-validation` (**api**) — `@RestControllerAdvice`. 소비 모듈도 각자 선언하지만 중복은 무해하다
- `springdoc-openapi-starter-webmvc-ui` (**api**, 버전은 루트 `ext.springdocVersion`) — 공용 응답 record가 `@Schema`를 갖는다
- `spring-security-core` (implementation) — `GlobalExceptionHandler`가 다루는 것은 core 예외 5종(`AccessDenied`/`BadCredentials`/`Disabled`/`Locked`/`Authentication`)뿐이라 starter 전체 대신 core만 선언한다
- `spring-boot-starter-aop` (**implementation** — 의도적) — `RateLimitAspect`의 `@Aspect`/`@Before`. 소비 앱이 컴파일에 필요한 것은 `@RateLimit` 애노테이션(이 모듈 소유)뿐이고, 런타임 AOP 활성화(aspectjweaver → `AopAutoConfiguration`)는 `logging-module`이 `starter-aop`를 `api`로 노출해 이미 3개 앱 클래스패스에 올려준다. **그 노출이 `implementation`으로 좁아지면 이 선언을 `api`로 승격해야 한다** — 그때 앱은 계속 컴파일되지만 aspect가 프록시되지 않아 `@RateLimit`이 조용히 무시된다

**이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar(`security-module` 선례).

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### rate limit aspect에 프로퍼티 스위치를 두지 않는다

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/ApiCommonRateLimitConfig.java`
→ 클래스 선언 / `rateLimitAspect(RateLimitCounterPort)`

web-api뿐 아니라 admin-api·ceo-api의 로그인 엔드포인트도 `@RateLimit(IP, 10회/60초)`로 이 aspect에
의존한다. **앱별 on/off 프로퍼티는 그 보호를 조용히 제거하는 보안 회귀**가 되므로 추가하지 않는다.
등록 조건은 "`apicommon.ratelimit`을 스캔 목록에 둔 서블릿 앱"뿐이다. 끄려고 그 앱 `ModuleScanConfig`에서 패키지를 빼는 것도 같은 보안 회귀다.

### `@ConditionalOnWebApplication(SERVLET)` — 재유입 방어선이므로 제거하지 않는다

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/ApiCommonRateLimitConfig.java`
→ `@ConditionalOnWebApplication(type = SERVLET)`

지금 batch의 runtimeClasspath에는 이 jar 자체가 없다. 따라서 이 조건은 잠재울 대상이 있어서가 아니라,
**재유입**(누군가 이 모듈을 non-servlet 앱의 클래스패스에 다시 올리는 경우)에 대한 방어선으로 둔 것이다.
"batch에 없으니 불필요"라는 이유로 지우지 않는다. 환경 조건이라 일반 `@Configuration`에서도 정확하게 판정된다.
예외 핸들러의 재유입 방어는 조건이 아니라 batch·web `ModuleScanConfig`가 `apicommon.exception`을 스캔하지 않는 것이 담당한다.

### `RateLimitException`을 에러코드 enum에 다시 결합하지 않는다

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/RateLimitException.java`
→ `DEFAULT_MESSAGE` / 기본 생성자

rate limiting은 domain에 대응 개념이 없는 순수 보안 관심사이므로(모듈 경계 규칙) 이 예외는 domain·application의
에러코드 enum에 결합하지 않는다. HTTP 응답은 `GlobalExceptionHandler`의 `handleRateLimitException`이
`backend/api-common-module/src/main/java/com/tastyhouse/apicommon/exception/ApiErrorCode.java`#`RATE_LIMIT_EXCEEDED`의 code·message로
직접 조립하고, 이 예외의 메시지는 읽지 않는다(wire 값 429 + `RATE_LIMIT_EXCEEDED`는 `application`의 `ErrorCatalogSnapshotTest`가 보증).
이 모듈 클래스패스에 `domain`이 없으므로 domain 에러코드와의 결합은 컴파일 수준에서도 불가능하다.

### `ClientIpResolver`와 `ApiLoggingFilter#resolveClientIp`의 중복은 의도적이다

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/common/ClientIpResolver.java`
→ `resolve(HttpServletRequest)`
(대응 사본: `backend/logging-module/src/main/java/com/tastyhouse/logging/ApiLoggingFilter.java` → `resolveClientIp`)

로직이 같지만 **통합하지 않는다** — `logging-module`은 `api-common-module`을 의존하지 않고, 의존을
추가하면 방향이 뒤집힌다(로깅은 api 계층 아래에 있는 횡단 관심사다). 중복을 감수하는 쪽을 택한 것이며,
"DRY 위반"으로 보고 합치지 않는다.

### 스캔 대상 컴포넌트에 `public`을 붙이지 않는다 (package-private 적용)

**대상**: `backend/api-common-module/src/test/java/com/tastyhouse/apicommon/architecture/LayerRulesTest.java` → `scannedComponentsShouldNotBePublic`

`RateLimitAspect`·`ApiCommonRateLimitConfig`·`exception/GlobalExceptionHandler`는 package-private이다. 가드는 `@Configuration`·`@Aspect`·`@RestControllerAdvice`의 `public`을 금지한다. **public으로 남는 것**: `ApiResponse`·`PageRequest`·`PaginationResponse`·`RateLimit`·`RateLimitKeyType`·`RateLimitException`·`ProblemDetails`·`ClientIpResolver` — 3개 api 모듈이 공용 계약으로 import한다. 근거와 전체 범주는 `backend/CLAUDE.md`의 "접근 제어자 규칙 (내부 구현은 package-private)" 절.

### `ApiErrorCode`는 `application`이 아니라 이 모듈이 소유하고, `AUTH_REQUIRED`는 미러다

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/exception/ApiErrorCode.java` → `RATE_LIMIT_EXCEEDED`(429)·`ACCESS_DENIED`(403)·`AUTH_REQUIRED`(401)

필터·핸들러가 쓰는 에러 상수는 이 모듈의 `ApiErrorCode` enum이다. wire 계약(status·errorCode·message)은 `application`의 `ErrorCatalogSnapshotTest`가 지킨다([ADR-0042](../../docs/adr/0042-error-code-module-split.md)).

- **왜 `application`이 아니라 여기인가**: 429·403·401 응답은 서블릿 필터·`@RestControllerAdvice`가 조립하는 HTTP 표현 계층 계약이고, 어떤 유스케이스도 던지지 않는다. 던지는 가장 안쪽 모듈에 둔다는 분할 규칙에 따라 표현 계층이 소유한다.
- **`ErrorCodeSpec`을 구현하지 않는다**: api 모듈은 `domain`을 볼 수 없다. 대신 `getHttpStatusCode`·`getCode`·`getDefaultMessage` 3개 getter만 같은 이름으로 둔다.
- **`AUTH_REQUIRED`는 `WebErrorCode.AUTH_REQUIRED`의 미러**다(web-application 서비스가 던지는 쪽과 필터가 조립하는 쪽이 같은 값이어야 한다). 두 값의 일치는 `backend/application-core/src/test/java/com/tastyhouse/application/shared/exception/ErrorCatalogConventionTest.java`#`mirroredCodesStayIdentical`이 보증한다 — 한쪽만 고치지 않는다.
- **대가**: `security-module`이 `JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`에서 이 enum을 읽으려고 `api-common-module`을 의존한다(`security-module → api-common-module`). 순환은 없다(`api-common-module → application-core, security-core`).
- 테스트: `backend/api-common-module/src/test/java/com/tastyhouse/apicommon/architecture/LayerRulesTest.java`의 `.because`가 "필터·핸들러 에러 계약 = `ApiErrorCode`, 예외 판정 = `ErrorResponses`"로 이 구조를 서술한다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### `com.tastyhouse.apicommon` 루트를 통째로 스캔하지 않는 이유

**대상**: 각 앱 부트스트랩의 `ModuleScanConfig` → `basePackages`

앱은 이 모듈의 패키지를 골라 스캔한다 — admin·ceo는 `apicommon.exception`·`apicommon.ratelimit`, web은 `apicommon.ratelimit`만.
나머지 공용 자산(`ApiResponse`·`PageRequest`·`ClientIpResolver` 등)은 **빈이 아니라 타입**이라 등록할 것이 없다.
앱별로 켜고 꺼야 하는 빈만 스캔 목록에 패키지 단위로 올린다.

### `GlobalExceptionHandler` — web-api가 이 핸들러를 쓰지 않는 이유

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/exception/GlobalExceptionHandler.java`
→ 클래스 선언 / `handleUnexpected`

검증 실패 메시지 형식이 다르다 — web-api는 `"필드명: 메시지"`를 `", "`로 join하는 반면 여기서는
메시지만 공백으로 join한다. 이는 우연한 차이가 아니라 **소비자별 응답 계약 차이**이므로 통합하지 않고
web-api가 자체 `com.tastyhouse.webapi.exception.GlobalExceptionHandler`를 유지한다.

**`BusinessException` 전용 처리 메서드는 없다.** `@ExceptionHandler(Exception.class)` 폴백 `handleUnexpected`가 먼저
`ErrorResponses.resolve(e)`를 호출해, 값이 있으면 `warn("BusinessException [{}]: {}")` 후 그 `status`·`code`·`message`로
`ProblemDetail`을 만들고, 값이 없으면 500을 낸다. 권한 거부(`handleAccessDenied`)는 `ApiErrorCode.ACCESS_DENIED`로 조립한다.
외부 연동 실패도 최상위가 `BusinessException`(에러코드는 그 실패를 번역하는 앱 모듈의 `WebErrorCode`·`BatchErrorCode` 등)이므로
같은 경로로 처리된다 — 전용 핸들러가 없는 것은 누락이 아니다([ADR-0025](../../docs/adr/0025-presentation-domain-cut.md)).

전용 메서드를 되살리지 않는다 — 되살리면 이 모듈이 다시 `com.tastyhouse.domain.exception`을 import해야 하고
`shouldNotDependOnDomain`이 실패한다. `resolve`가 `getCause()`를 따라가지 않는 이유(래핑 예외가 500→4xx로
바뀌는 것 방지)와 정적 유틸인 이유는 `backend/application-core/AGENTS.md`의 `ErrorResponses` 항목에 있다.

### `ProblemDetails`가 static 유틸인 이유

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/exception/ProblemDetails.java`
→ 클래스 선언 / `of(int, String, String)`

web-api와 admin·ceo-api는 응답 계약이 달라 전역 핸들러를 각자 유지하지만, **RFC7807 조립 로직만은 두
핸들러에 바이트 단위로 동일하게 복제**돼 있었다. 계약 차이는 메시지를 만드는 쪽에 있고 조립 자체에는
없으므로 이 유틸 하나로 통합했다. `@Component`가 아니라 static 유틸이므로 **컴포넌트 스캔 범위와
무관하다**(web-api는 이 패키지를 스캔하지 않아 여기의 핸들러 빈을 등록하지 않는다).

### `RateLimitCounterPort`를 security-core로 옮긴 이유

**대상**: `backend/security-core/src/main/java/com/tastyhouse/security/ratelimit/RateLimitCounterPort.java`
→ 인터페이스 선언 / `isLimitExceeded(String, int, Duration)`
(소비 지점: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/RateLimitAspect.java`,
`ApiCommonRateLimitConfig.java` → `rateLimitAspect(RateLimitCounterPort)`)

키 조립(클라이언트 IP·요청 필드 해석) 같은 웹 관심사는 이 표현 모듈에, 카운터 구현만 인프라에 둔다 — 인프라가
`HttpServletRequest`를 해석하면 서블릿 스택까지 끌어오기 때문이다. 그 사이의 **계약**은 `security-core`에 둔다.
계약을 표현 모듈에 두면 아웃바운드 어댑터 `infrastructure:redis`가 포트 인터페이스 **하나** 때문에
`starter-web`·springdoc을 노출하는 표현 모듈을 컴파일 의존하는 수평 간선이 생긴다. 포트 시그니처는
`String`·`int`·`Duration`뿐이라 서블릿·HTTP와 무관하고, 실사용처도 로그인 브루트포스 방어라는 보안
관심사다. 그래서 토큰 저장소 포트 3종(`RefreshTokenRepository`·`BlacklistRepository`·`SocialTempTokenRepository`)과 같은 "계약은 `security-core`, 구현은 `infrastructure:redis`" 구조를 따른다.
이 모듈은 `implementation project(':security-core')`로 포트를 받는다. 상세 근거와 기각한 대안(별도 초소형 모듈·`domain`)은
`security-core/AGENTS.md`의 "rate limit 카운터 계약을 여기 둔 이유"([ADR-0023](../../docs/adr/0023-rate-limit-port-in-security-core.md)).

카운팅 방식은 Fixed Window이고, 카운터를 어디에 저장하는지는 이 계약의 관심사가 아니다.

### `RateLimitAspect`의 책임 경계

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/RateLimitAspect.java`
→ 클래스 선언 / `buildKey` / `resolveFieldValue`

키 조립(클라이언트 IP·요청 필드 해석)은 **HTTP 어댑터 관심사이므로 이 표현 모듈이 소유**하고, 실제
카운팅만 `RateLimitCounterPort` 구현체(인프라)에 위임한다. `keyType=FIELD`의 필드 추출은 리플렉션으로
**레코드 컴포넌트 접근자(`phoneNumber()`) → getter(`getPhoneNumber()`)** 순으로 시도하며, 찾지 못하면
예외 대신 `"unknown"` 식별자로 폴백한다(요청을 막지 않는다).

### `ApiCommonRateLimitConfigTest`가 증명하는 것과 증명하지 못하는 것

**대상**: `backend/api-common-module/src/test/java/com/tastyhouse/apicommon/ratelimit/ApiCommonRateLimitConfigTest.java`
→ `rateLimitAspectRegisteredInServletContext` / `rateLimitAspectAbsentInNonServletContext`

두 테스트는 서블릿 컨텍스트에서 aspect가 등록되고 비-서블릿 컨텍스트에서 등록되지 않는 것을 증명한다.
`ApplicationContextRunner` 기본값이 비-웹 컨텍스트라 batch의 `web-application-type: none`에 해당한다.
핸들러가 web에 뜨지 않는 것은 조건이 아니라 스캔 목록이므로, 그 보증은 각 앱 `ApplicationLayerScanConfigTest`의
`assertScansModulesWithoutFilters`가 맡는다. 이 모듈 `LayerRulesTest`의 클래스 수 하한은 13이다.
이 테스트들은 **단위 수준 근거**일 뿐이고, 실제 회귀 방지는 4개 앱 기동 후의 등록 빈 확인·로그인 rate
limit 실측이 담당한다.

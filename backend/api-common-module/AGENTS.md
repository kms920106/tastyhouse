<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-02 -->

# api-common-module

## Purpose
`web-api`·`admin-api`·`ceo-api`가 공유하는 **HTTP 경계 공용 플럼빙 라이브러리 모듈**(`java-library`). 세 모듈에 **package 선언 1줄만 다른 완전 복제**로 존재하던 응답 래퍼·페이징 요청·파일 업로드 어댑터·전역 예외 핸들러를 단독 소유한다.

과거 루트 CLAUDE.md의 관례는 "모듈별로 각각 둠"이었으나, 전수 diff 결과 대상 파일들이 한 글자도 다르지 않아 그 관례가 보호하려던 "소비자별 계약 차이"가 실재하지 않았다. 오히려 복제를 방치한 결과 세 모듈의 `FileService`가 서로 다르게 드리프트한 선례가 있었다. 이 모듈은 domain 어댑터가 아니라 presentation 공유 유틸이므로 `security-module` 선례대로 `implementation`으로 노출되고 `bootJar`가 비활성이다.

**통합 판정 기준은 "완전 동일"뿐이다** — 의미 있는 차이가 한 줄이라도 있으면(응답 계약·`@Schema` 문구 포함) 통합하지 않는다. 유지되는 복제 목록과 그 사유는 루트 [CLAUDE.md](../CLAUDE.md)의 "api 모듈 공용 플럼빙 소유 규칙" 절에 표로 관리한다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + web/validation/aop starter, springdoc. `domain`만 내부 의존이며 `api`(공개 시그니처에 `PageResult`·`FileUploadService` 노출). **`infrastructure:redis`에 의존하지 않는다** — 챕터 02에서 방향이 역전돼 이제 redis가 이 모듈을 의존한다. `bootJar` 비활성 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/apicommon/common/` | `ApiResponse<T>`(성공 응답 + `Pagination`), `PaginationResponse<T>`(표준 4필드 페이징), `PageRequest`(`@ModelAttribute` 페이징 요청) |
| `src/main/java/com/tastyhouse/apicommon/exception/` | `GlobalExceptionHandler` — **`@Bean("sharedGlobalExceptionHandler")`로 조건부 등록**(아래 "등록 방식" 절). web-api는 자체 핸들러가 있어 이 빈이 등록되지 않는다 |
| `src/main/java/com/tastyhouse/apicommon/ratelimit/` | rate limit **표현 관심사 전부** — `@RateLimit`·`RateLimitKeyType`·`RateLimitAspect`(키 조립: IP·요청 필드 해석)·`RateLimitException`·계약 `RateLimitCounterPort`. 카운터 구현은 `infrastructure:redis`의 `RedisRateLimitCounter`(챕터 02) |
| `src/main/java/com/tastyhouse/apicommon/file/` | `FileService` — `MultipartFile`을 도메인 `FileUploadCommand`로 바꾸는 얇은 업로드 어댑터(조회·URL 변환 책임 없음) |
| `src/main/java/com/tastyhouse/apicommon/shop/response/` | admin↔ceo 바이트 동일이던 shop 응답 record 3종(`ShopBreakTimeResponse`·`ShopBusinessHourResponse`·`ShopHygieneBadgeResponse`) |

## 등록 방식 — auto-configuration (챕터 02 개정, 전면 교체)

**과거 이 절은 "부분 진입점 스캔"(`ApiCommonConfig` 전체 스캔 vs `ApiCommonFileConfig`+`ApiCommonRateLimitConfig` 부분 스캔) 구조를 다뤘다. 그 구조는 챕터 02로 완전히 사라졌다** — `ApiCommonConfig`·`ApiCommonRateLimitConfig` 두 `@Configuration` 진입점 클래스 자체가 **삭제**됐고, 대신 아래 두 `@AutoConfiguration` 클래스가 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록한다. 3개 api 모듈 어디에도 `@Import(ApiCommon...)`가 없다 — "클래스패스 존재 = 활성화"이며, 앱별 차이는 스캔 범위가 아니라 **`@Bean` 메서드의 조건**으로 표현한다.

| 클래스 | 패키지 | 등록 방식 | 조건 |
|---|---|---|---|
| `ApiCommonModuleAutoConfiguration` | `com.tastyhouse.apicommon` | `@Bean("sharedGlobalExceptionHandler") GlobalExceptionHandler` — **스캔 없이 단일 `@Bean` 메서드로 등록**(과거 `@RestControllerAdvice` 컴포넌트 스캔 방식에서 전환) | `@ConditionalOnWebApplication(type = SERVLET)` + 메서드에 `@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)` |
| `ApiCommonRateLimitAutoConfiguration` | `com.tastyhouse.apicommon.ratelimit` | `@Bean RateLimitAspect(RateLimitCounterPort, ClientIpResolver, ...)` — `RateLimitAspect`에서 `@Component`를 제거하고 `@Bean` 메서드 파라미터로 협력자를 주입 | `@ConditionalOnWebApplication(type = SERVLET)` + 메서드에 `@ConditionalOnBean(RateLimitCounterPort.class)` + `@AutoConfiguration(afterName = "com.tastyhouse.infrastructure.redis.RedisModuleAutoConfiguration")` |

- **빈 이름을 `sharedGlobalExceptionHandler`로 지정하는 이유**: web-api의 자체 `GlobalExceptionHandler`와 단순 클래스명이 같아, 기본 빈 이름(`globalExceptionHandler`)을 쓰면 두 빈이 이름 충돌한다. 이름을 다르게 지어 공존시키고, `@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)`가 실제 등록 여부를 가른다 — web-api는 자체 `@RestControllerAdvice`가 이미 있으므로 이 빈이 **등록되지 않는다**(Negative), admin/ceo는 없으므로 **등록된다**(Positive).
- **`RateLimitAspect`가 `RateLimitCounterPort` 존재를 조건으로 삼는 이유**: 카운터 빈은 `infrastructure:redis`의 `RedisModuleAutoConfiguration`이 등록한다. 클래스 리터럴(`@ConditionalOnBean(RedisRateLimitCounter.class)`)을 쓸 수 없는 이유는 의존 방향이 `infrastructure:redis → api-common`이라 이 모듈이 redis 모듈의 구체 타입을 컴파일 타임에 볼 수 없기 때문이다(순환 방지) — 그래서 도메인 포트 `RateLimitCounterPort`(이 모듈이 소유)로 조건을 건다. `afterName`으로 순서를 강제하는 이유는 스캔된 `RedisRateLimitCounter` 정의가 redis auto-configuration 처리 시점에 등록되므로, 그보다 먼저 이 조건을 평가하면 `@ConditionalOnBean`이 아직 없는 빈을 보고 거짓으로 판정하기 때문이다.
- **과거 "부분 진입점을 쓰는 앱에 패키지를 추가할 때는 그 앱의 `@Import`도 함께 늘린다"는 함정은 이제 존재하지 않는다.** admin/ceo/web 어느 쪽도 `@Import`를 갖지 않으므로 배선 누락이라는 실패 양식 자체가 사라졌다 — 대신 위 조건이 앱별 차이를 자동으로 답한다.

## 스캔 주의 (조건부 등록이 곧 동작 — 개정)
`GlobalExceptionHandler`(`@RestControllerAdvice`)와 `RateLimitAspect`는 이제 컴포넌트 스캔이 아니라 **`@Bean` 메서드 + 조건**으로 등록되므로, "어느 앱이 어느 패키지를 스캔하는가"가 아니라 "어느 앱이 어떤 조건을 만족하는가"가 런타임 동작을 결정한다.

| 앱 | `sharedGlobalExceptionHandler` | `rateLimitAspect` |
|---|---|---|
| `AdminApiApplication` / `CeoApiApplication` | **Positive** — 자체 advice 없음 | **Positive** — `RateLimitCounterPort` 빈 존재(redis) |
| `WebApiApplication` | **Negative** — 자체 `webapi.exception.GlobalExceptionHandler` 존재 | **Positive** — 동일 |
| `BatchApplication` | **Negative** — non-servlet(`spring.main.web-application-type: none`) | **Negative** — 동일. `FileService`(`@Service`, 조건 없음)는 batch도 필요 없어 원래도 안 씀 |

`FileService`는 여전히 `@Service` 컴포넌트 스캔으로 등록된다(조건부 전환 대상이 아니다 — admin/ceo/web 전부 파일 업로드가 필요해 앱별 차이가 없다).

## 여기에 두면 안 되는 것
- **모듈마다 내용이 다른 정책 파일** — `SecurityConfig`(필터체인·인가 정책), `PublicPaths`(공개 경로 목록: web 18줄 vs admin/ceo 3줄), `TokenService`/`AuthService`(인증 주체 `Admin`/`Ceo`와 JWT 시크릿이 분리되어야 함).
- **필드 셋이나 `@Schema` 문구가 다른 응답 record** — 예: `ShopDetailResponse`(admin은 감사 시각, ceo는 `trademarkImageUrl`/`hidden`), `ShopAmenityResponse`("가게" vs "내 가게").
- **도메인 포트가 있는 기술 어댑터** — 그것은 외부 연동 모듈(`infrastructure:external` 코어 + `infrastructure:{firebase,aws,oauth,payment,messaging,crawling}` 어댑터)이나 `infrastructure:persistence`(DB 어댑터) 소관이다.
- **기술 구현체** — 이 모듈은 계약(`RateLimitCounterPort`)만 두고 구현은 인프라 모듈에 맡긴다. 표현 계층이 인프라 모듈을 `implementation`으로 끌어오는 순간 챕터 02가 교정한 역방향 의존이 되살아난다.

## Dependencies

### Internal
- `domain` (**api**) — `PaginationResponse.from(PageResult<T>)`의 공개 시그니처에 domain 타입이 노출되므로 `api`로 둔다. 그 노출을 타고 `domain.exception`(`BusinessException`·`ErrorCode`)도 api 3모듈의 **공용 에러 계약**이 된다.
- **`infrastructure:redis`에 의존하지 않는다** — 챕터 02에서 rate limit의 표현 관심사(`@RateLimit`·`RateLimitAspect`·`RateLimitException`·`RateLimitCounterPort`)를 이 모듈로 올리고 Redis 카운터만 인프라에 남겨 **포트로 역전**했다. 방향은 이제 `redis → api-common`이다.

### External
- `spring-boot-starter-web`·`spring-boot-starter-validation` (**api**) — `@RestControllerAdvice`. 소비 모듈도 각자 선언하지만 중복은 무해하다
- `springdoc-openapi-starter-webmvc-ui` (**api**, 버전은 루트 `ext.springdocVersion`) — 공용 응답 record가 `@Schema`를 갖는다
- `spring-security-core` (implementation) — `GlobalExceptionHandler`가 다루는 것은 core 예외 5종(`AccessDenied`/`BadCredentials`/`Disabled`/`Locked`/`Authentication`)뿐이라 starter 전체 대신 core만 선언한다
- `spring-boot-starter-aop` (**implementation** — 의도적) — `RateLimitAspect`의 `@Aspect`/`@Before`. 소비 앱이 컴파일에 필요한 것은 `@RateLimit` 애노테이션(이 모듈 소유)뿐이고, 런타임 AOP 활성화(aspectjweaver → `AopAutoConfiguration`)는 `logging-module`이 `starter-aop`를 `api`로 노출해 이미 3개 앱 클래스패스에 올려준다. **그 노출이 `implementation`으로 좁아지면 이 선언을 `api`로 승격해야 한다** — 그때 앱은 계속 컴파일되지만 aspect가 프록시되지 않아 `@RateLimit`이 조용히 무시된다

**이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar(`security-module` 선례).

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### rate limit aspect에 프로퍼티 스위치를 두지 않는다

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/ApiCommonRateLimitAutoConfiguration.java`
→ 클래스 선언 / `rateLimitAspect(RateLimitCounterPort)`

web-api뿐 아니라 admin-api·ceo-api의 로그인 엔드포인트도 `@RateLimit(IP, 10회/60초)`로 이 aspect에
의존한다. **앱별 on/off 프로퍼티는 그 보호를 조용히 제거하는 보안 회귀**가 되므로 추가하지 않는다.
등록 조건은 "카운터 빈이 있는 서블릿 앱"뿐이다.

### `@ConditionalOnWebApplication(SERVLET)` 두 건 — 재유입 방어선이므로 제거하지 않는다

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ApiCommonModuleAutoConfiguration.java`,
`backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/ApiCommonRateLimitAutoConfiguration.java`
→ 두 클래스의 `@ConditionalOnWebApplication(type = SERVLET)`

과거 batch-module은 이 모듈을 직접 의존하지 않으면서도
`application → security-core → infrastructure:redis → api-common-module` **전이 사슬**로 클래스패스에
갖고 있었고, batch의 `spring.main.web-application-type: none`이 이 조건을 Negative로 만드는 유일한
근거였다. **토큰 저장소 포트/어댑터 역전으로 그 사슬은 끊겼다** — 지금 batch의 runtimeClasspath에는
이 jar 자체가 없다. 따라서 이 조건은 지금 잠재울 대상이 있어서가 아니라, **재유입**(누군가 이 모듈을
non-servlet 앱의 클래스패스에 다시 올리는 경우)에 대한 방어선으로 남긴 것이다. "batch에 없으니 불필요"
라는 이유로 지우지 않는다.

> 위 [스캔 주의](#스캔-주의-조건부-등록이-곧-동작--개정) 표의 `BatchApplication` 행은 이 사슬이
> 살아 있던 시점의 판정이다. 지금은 jar 자체가 없어 조건 평가에 도달하지도 않는다.

### 빈 이름 `sharedGlobalExceptionHandler`를 기본 이름으로 되돌리지 않는다

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ApiCommonModuleAutoConfiguration.java`
→ `@Bean("sharedGlobalExceptionHandler")`

web-api의 자체 핸들러와 단순명이 같아 기본 빈 이름 `globalExceptionHandler`는 충돌한다. 이름을 다르게
둔 덕분에, 조건이 어떤 이유로 우회되더라도 `allow-bean-definition-overriding=false`로 **기동이 실패해
조용히 덮이지 않는다.**

### `RateLimitException`을 `ErrorCode`에 다시 결합하지 않는다

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/RateLimitException.java`
→ `DEFAULT_MESSAGE` / 기본 생성자

rate limiting은 domain에 대응 개념이 없는 순수 보안 관심사이므로(모듈 경계 규칙) 이 예외는
`com.tastyhouse.domain.exception.ErrorCode`에 결합하지 않는다. 과거 생성자가
`ErrorCode.RATE_LIMIT_EXCEEDED.getDefaultMessage()`로 메시지를 채웠으나, 실제 HTTP 응답은
`GlobalExceptionHandler`가 `ErrorCode.RATE_LIMIT_EXCEEDED`의 code·message로 직접 조립하고 이 예외의
메시지는 읽지 않는다. 결합을 끊어도 응답 계약(429 + `RATE_LIMIT_EXCEEDED`)은 그대로다.

### `ClientIpResolver`와 `ApiLoggingFilter#resolveClientIp`의 중복은 의도적이다

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/common/ClientIpResolver.java`
→ `resolve(HttpServletRequest)`
(대응 사본: `backend/logging-module/src/main/java/com/tastyhouse/logging/ApiLoggingFilter.java` → `resolveClientIp`)

로직이 같지만 **통합하지 않는다** — `logging-module`은 `api-common-module`을 의존하지 않고, 의존을
추가하면 방향이 뒤집힌다(로깅은 api 계층 아래에 있는 횡단 관심사다). 중복을 감수하는 쪽을 택한 것이며,
"DRY 위반"으로 보고 합치지 않는다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### `ApiCommonModuleAutoConfiguration`이 컴포넌트 스캔을 쓰지 않는 이유

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ApiCommonModuleAutoConfiguration.java`
→ 클래스 선언

이 모듈의 나머지 공용 자산(`ApiResponse`·`PageRequest`·`ClientIpResolver` 등)은 **빈이 아니라 타입**이라
등록할 것이 없다. 앱별로 켜고 꺼야 하는 빈만 조건부 `@Bean`으로 등록한다.

### `afterName`에 클래스 리터럴을 쓸 수 없는 이유 (순환 회피)

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/ApiCommonRateLimitAutoConfiguration.java`
→ `@AutoConfiguration(afterName = "com.tastyhouse.infrastructure.redis.RedisModuleAutoConfiguration")`

의존 방향이 `infrastructure:redis → api-common-module`이라 api-common은 redis 모듈의 타입을 **컴파일
시점에 볼 수 없다**(참조하면 순환). 그래서 문자열 FQCN으로 순서만 선언한다. 스캔된
`RedisRateLimitCounter` 정의는 redis auto-config 처리 시점에 등록되므로, 이 순서가
`@ConditionalOnBean(RateLimitCounterPort.class)`의 가시성을 보장한다.

### `GlobalExceptionHandler` — web-api가 이 핸들러를 쓰지 않는 이유

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/exception/GlobalExceptionHandler.java`
→ 클래스 선언 / `handleBusinessException`

검증 실패 메시지 형식이 다르다 — web-api는 `"필드명: 메시지"`를 `", "`로 join하는 반면 여기서는
메시지만 공백으로 join한다. 이는 우연한 차이가 아니라 **소비자별 응답 계약 차이**이므로 통합하지 않고
web-api가 자체 `com.tastyhouse.webapi.exception.GlobalExceptionHandler`를 유지한다.

`ExternalApiException`은 `BusinessException`을 상속하므로 `handleBusinessException` 하나로 처리된다
(전용 핸들러가 없는 것은 누락이 아니다).

### `ProblemDetails`가 static 유틸인 이유

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/exception/ProblemDetails.java`
→ 클래스 선언 / `of(int, String, String)`

web-api와 admin·ceo-api는 응답 계약이 달라 전역 핸들러를 각자 유지하지만, **RFC7807 조립 로직만은 두
핸들러에 바이트 단위로 동일하게 복제**돼 있었다. 계약 차이는 메시지를 만드는 쪽에 있고 조립 자체에는
없으므로 이 유틸 하나로 통합했다. `@Component`가 아니라 static 유틸이므로 **컴포넌트 스캔 범위와
무관하다**(web-api는 이 패키지를 스캔하지 않아 여기의 핸들러 빈을 등록하지 않는다).

### `RateLimitCounterPort`를 표현 계층에 둔 이유

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/RateLimitCounterPort.java`
→ 인터페이스 선언 / `isLimitExceeded`

이전에는 api-common-module이 `RateLimitException` 처리를 위해 `infrastructure:redis`를 의존했고, 그
인프라 모듈이 `HttpServletRequest`로 클라이언트 IP를 해석하느라 **서블릿 스택까지 끌어왔다.** 웹
관심사를 표현으로 올리고 카운터만 인프라에 남기면서 의존 방향이 바로잡혔다. 카운팅 방식은 Fixed
Window이고, 카운터를 어디에 저장하는지는 이 계약의 관심사가 아니다.

### `RateLimitAspect`의 책임 경계

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/RateLimitAspect.java`
→ 클래스 선언 / `buildKey` / `resolveFieldValue`

키 조립(클라이언트 IP·요청 필드 해석)은 **HTTP 어댑터 관심사이므로 이 표현 모듈이 소유**하고, 실제
카운팅만 `RateLimitCounterPort` 구현체(인프라)에 위임한다. `keyType=FIELD`의 필드 추출은 리플렉션으로
**레코드 컴포넌트 접근자(`phoneNumber()`) → getter(`getPhoneNumber()`)** 순으로 시도하며, 찾지 못하면
예외 대신 `"unknown"` 식별자로 폴백한다(요청을 막지 않는다).

### `ApiCommonAutoConfigurationTest`가 증명하는 것과 증명하지 못하는 것

**대상**: `backend/api-common-module/src/test/java/com/tastyhouse/apicommon/ApiCommonAutoConfigurationTest.java`
→ 클래스 선언 / `NonServletApplication`

`ApplicationContextRunner` 기본값이 비-웹 컨텍스트라 batch의 `web-application-type: none`에 해당한다.
이 테스트는 **단위 수준 근거**일 뿐이고, 실제 회귀 방지는 4개 앱 기동 후의 조건 리포트·로그인 rate
limit 실측이 담당한다.

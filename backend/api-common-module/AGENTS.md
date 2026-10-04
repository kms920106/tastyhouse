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
| `build.gradle` | `java-library` + web/validation/aop starter, springdoc. ~~`domain`만 내부 의존이며 `api`~~ **(번복됨 — 덩어리 01)** 내부 의존은 `application`(**api** — 공개 시그니처에 `PageResult` 노출. 핸들러는 `ErrorResponses`·`ErrorContracts`를 쓴다)과 `security-core`(implementation)이며, `domain`은 더 이상 의존하지 않는다. **`infrastructure:redis`에 의존하지 않는다** — 챕터 02에서 방향이 역전돼 이제 redis가 이 모듈을 의존한다. `bootJar` 비활성 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/apicommon/common/` | `ApiResponse<T>`(성공 응답 + `Pagination`), `PaginationResponse<T>`(표준 4필드 페이징), `PageRequest`(`@ModelAttribute` 페이징 요청) |
| `src/main/java/com/tastyhouse/apicommon/exception/` | `GlobalExceptionHandler`(`@RestControllerAdvice`) — admin-api·ceo-api 부트스트랩의 중첩 `ModuleScanConfig`가 이 패키지를 스캔해 직접 등록한다(빈 이름 `globalExceptionHandler`). web-api는 자체 핸들러가 있어 이 패키지를 스캔하지 않는다. **(번복됨 — imports 제거)** ~~`@Bean("sharedGlobalExceptionHandler")`로 조건부 등록~~ |
| `src/main/java/com/tastyhouse/apicommon/ratelimit/` | rate limit **표현 관심사 전부** — `@RateLimit`·`RateLimitKeyType`·`RateLimitAspect`(키 조립: IP·요청 필드 해석)·`RateLimitException`. 카운터 계약 `RateLimitCounterPort`는 **`security-core`(`com.tastyhouse.security.ratelimit`) 소유**(~~이 패키지 소유~~ 번복됨 — 아래 [security-core로 옮긴 이유](#ratelimitcounterport를-security-core로-옮긴-이유)), 카운터 구현은 `infrastructure:redis`의 `RedisRateLimitCounter`(챕터 02) |
| `src/main/java/com/tastyhouse/apicommon/file/` | `FileService` — `MultipartFile`을 도메인 `FileUploadCommand`로 바꾸는 얇은 업로드 어댑터(조회·URL 변환 책임 없음) |
| `src/main/java/com/tastyhouse/apicommon/shop/response/` | admin↔ceo 바이트 동일이던 shop 응답 record 3종(`ShopBreakTimeResponse`·`ShopBusinessHourResponse`·`ShopHygieneBadgeResponse`) |

## 등록 방식 — auto-configuration (챕터 02 개정, 전면 교체)

> **(번복됨 — imports 제거)** 제목은 앵커 보존을 위해 그대로 둔다. 지금 이 모듈에는 imports 파일이 없고, 앱별 차이는 다시 **스캔 범위**로 표현한다 — 조건(`@ConditionalOnMissingBean`/`@ConditionalOnBean`)은 일반 `@Configuration`에서 처리 순서에 좌우돼 믿을 수 없어 쓰지 않는다.
>
> | 클래스 | before | after |
> |---|---|---|
> | 예외 핸들러 등록 | `ApiCommonModuleAutoConfiguration`의 `@Bean("sharedGlobalExceptionHandler")` + `@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)` | 설정 클래스 **삭제**. `GlobalExceptionHandler`(`@RestControllerAdvice`)를 admin·ceo의 `ModuleScanConfig`가 `com.tastyhouse.apicommon.exception` 스캔으로 직접 등록(빈 이름 `globalExceptionHandler`). web은 그 패키지를 스캔하지 않는다 |
> | rate limit aspect | `ApiCommonRateLimitAutoConfiguration` — `@ConditionalOnBean(RateLimitCounterPort)` + `afterName = "…RedisModuleAutoConfiguration"` | **`ApiCommonRateLimitConfig`**(`@Configuration(proxyBeanMethods = false)` + `@ConditionalOnWebApplication(SERVLET)`) — `@Bean RateLimitAspect`. web·admin·ceo의 `ModuleScanConfig`가 `com.tastyhouse.apicommon.ratelimit`을 스캔해 등록. 빈 존재 조건·`afterName` 삭제(스캔하는 세 앱은 전부 Redis 카운터를 갖는다) |
> | 앱의 배선 | 0줄(imports 파일) | 각 앱 `ModuleScanConfig`의 패키지 문자열 1~2항목. 문자열이라 `implementation` 의존 그대로 컴파일 게이트 무관 |
>
> 아래 표와 항목은 챕터 02 시점 기록이다.

**과거 이 절은 "부분 진입점 스캔"(`ApiCommonConfig` 전체 스캔 vs `ApiCommonFileConfig`+`ApiCommonRateLimitConfig` 부분 스캔) 구조를 다뤘다. 그 구조는 챕터 02로 완전히 사라졌다** — `ApiCommonConfig`·`ApiCommonRateLimitConfig` 두 `@Configuration` 진입점 클래스 자체가 **삭제**됐고, 대신 아래 두 `@AutoConfiguration` 클래스가 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록한다. 3개 api 모듈 어디에도 `@Import(ApiCommon...)`가 없다 — "클래스패스 존재 = 활성화"이며, 앱별 차이는 스캔 범위가 아니라 **`@Bean` 메서드의 조건**으로 표현한다.

| 클래스 | 패키지 | 등록 방식 | 조건 |
|---|---|---|---|
| `ApiCommonModuleAutoConfiguration` | `com.tastyhouse.apicommon` | `@Bean("sharedGlobalExceptionHandler") GlobalExceptionHandler` — **스캔 없이 단일 `@Bean` 메서드로 등록**(과거 `@RestControllerAdvice` 컴포넌트 스캔 방식에서 전환) | `@ConditionalOnWebApplication(type = SERVLET)` + 메서드에 `@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)` |
| `ApiCommonRateLimitAutoConfiguration` | `com.tastyhouse.apicommon.ratelimit` | `@Bean RateLimitAspect(RateLimitCounterPort, ClientIpResolver, ...)` — `RateLimitAspect`에서 `@Component`를 제거하고 `@Bean` 메서드 파라미터로 협력자를 주입 | `@ConditionalOnWebApplication(type = SERVLET)` + 메서드에 `@ConditionalOnBean(RateLimitCounterPort.class)` + `@AutoConfiguration(afterName = "com.tastyhouse.infrastructure.redis.RedisModuleAutoConfiguration")` |

- **빈 이름을 `sharedGlobalExceptionHandler`로 지정하는 이유**: web-api의 자체 `GlobalExceptionHandler`와 단순 클래스명이 같아, 기본 빈 이름(`globalExceptionHandler`)을 쓰면 두 빈이 이름 충돌한다. 이름을 다르게 지어 공존시키고, `@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)`가 실제 등록 여부를 가른다 — web-api는 자체 `@RestControllerAdvice`가 이미 있으므로 이 빈이 **등록되지 않는다**(Negative), admin/ceo는 없으므로 **등록된다**(Positive).
- **`RateLimitAspect`가 `RateLimitCounterPort` 존재를 조건으로 삼는 이유**: 카운터 빈은 `infrastructure:redis`의 `RedisModuleAutoConfiguration`이 등록한다. 클래스 리터럴(`@ConditionalOnBean(RedisRateLimitCounter.class)`)을 쓸 수 없는 이유는 이 모듈의 main 클래스패스에 redis 모듈이 없어 구체 타입을 컴파일 타임에 볼 수 없기 때문이다 — ~~의존 방향이 `infrastructure:redis → api-common`이라(순환 방지)~~ **(번복됨 — 지금 방향은 `infrastructure:redis → security-core ← api-common-module`이다. 순환이 아니라 "표현은 구현을 모른다"가 이유다)** — 그래서 포트 `RateLimitCounterPort`(~~이 모듈이 소유~~ **`security-core` 소유**)로 조건을 건다. `afterName`으로 순서를 강제하는 이유는 스캔된 `RedisRateLimitCounter` 정의가 redis auto-configuration 처리 시점에 등록되므로, 그보다 먼저 이 조건을 평가하면 `@ConditionalOnBean`이 아직 없는 빈을 보고 거짓으로 판정하기 때문이다.
- **과거 "부분 진입점을 쓰는 앱에 패키지를 추가할 때는 그 앱의 `@Import`도 함께 늘린다"는 함정은 이제 존재하지 않는다.** admin/ceo/web 어느 쪽도 `@Import`를 갖지 않으므로 배선 누락이라는 실패 양식 자체가 사라졌다 — 대신 위 조건이 앱별 차이를 자동으로 답한다.

## 스캔 주의 (조건부 등록이 곧 동작 — 개정)

> **(번복됨 — imports 제거)** 다시 "어느 앱이 어느 패키지를 스캔하는가"가 런타임 동작을 결정한다. 현재 판정:
>
> | 앱 | `globalExceptionHandler`(공용) | `rateLimitAspect` |
> |---|---|---|
> | `AdminApiApplication` / `CeoApiApplication` | 등록 — `apicommon.exception` 스캔 | 등록 — `apicommon.ratelimit` 스캔 |
> | `WebApiApplication` | 미등록 — 스캔 안 함(같은 이름의 빈은 web 자체 `webapi.exception.GlobalExceptionHandler`) | 등록 — 스캔 |
> | `BatchApplication` | 미등록 — 스캔 안 함(jar도 클래스패스에 없다) | 미등록 — 동일 |
>
> 목록은 각 앱 `ApplicationLayerScanConfigTest`의 `assertScansModulesWithoutFilters`가 정확 일치로 검사한다. 아래 표는 챕터 02 시점 기록이다.
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
- **도메인 포트가 있는 기술 어댑터** — 그것은 외부 연동 모듈(`infrastructure:restclient` 코어 + `infrastructure:{firebase,aws-s3,aws-ses,aws-sns,kakao-oauth,naver-oauth,apple-oauth,facebook-oauth,pg,tosspayments,mail,javamail,sms,solapi,bbq,admdongkor}` 어댑터)이나 `infrastructure:persistence`(DB 어댑터) 소관이다.
- **기술 구현체** — 이 모듈은 rate limit의 표현(애노테이션·aspect·예외)만 두고, 카운터 계약(`RateLimitCounterPort`)은 `security-core`가, 구현은 인프라 모듈이 맡는다(~~이 모듈은 계약(`RateLimitCounterPort`)만 두고~~ 번복됨 — 계약도 이 모듈을 떠났다). 표현 계층이 인프라 모듈을 `implementation`으로 끌어오는 순간 챕터 02가 교정한 역방향 의존이 되살아난다.

## Dependencies

### Internal
- `application` (**api**) — **(번복 — 덩어리 01, 과거 `domain`(api))**. `PaginationResponse.from(PageResult<T>)`의 공개 시그니처에 `com.tastyhouse.application.shared.port.out.page.PageResult`가 노출되므로 `api`로 둔다.

  | 항목 | before | after |
  |---|---|---|
  | 내부 의존 | `api project(':domain')` | `api project(':application')` |
  | 공용 에러 계약 | 이 노출을 타고 `domain.exception`(`BusinessException`·`ErrorCode`)이 api 3모듈의 **공용 에러 계약**이 된다 — **이 서술은 번복됨** | 표현 계층은 `domain.exception`을 보지 않는다. 판정은 `com.tastyhouse.application.shared.error.ErrorResponses#resolve`, 표현 계층이 직접 쓰는 상수는 `ErrorContracts`(`rateLimit()`·`accessDenied()`·`authRequired()`) |
  | presentation 컴파일 클래스패스의 `domain` | 있음(이 `api` 노출 경유) | **없음** — `application`은 `domain`을 `implementation`으로만 가진다 |

  `domain`이 되돌아오는 회귀는 `src/test/java/com/tastyhouse/apicommon/architecture/LayerRulesTest.java` → `shouldNotDependOnDomain`이 잡는다(이 모듈 전체 ✗ `com.tastyhouse.domain..`, 테스트 의존 `archunit-junit5` 신설).
- `security-core` (implementation — **신설 간선**) — `RateLimitAspect`·`ApiCommonRateLimitConfig`(당시 이름 `ApiCommonRateLimitAutoConfiguration`)가 쓰는 `RateLimitCounterPort`의 소유 모듈. `api`로 노출하지 않는다 — web·admin·ceo는 `security-module`의 `api project(':security-core')`로 이미 이 모듈을 받는다. 부수적으로 compileClasspath에 `jjwt-api`가 security-core를 통해 전이로 실리는데 허용 범위다(`spring-security-core`는 원래 직접 선언돼 있다).
- **(번복됨 — imports 제거: 이 테스트 의존은 삭제됐다. `afterName`이 사라져 검증할 문자열이 없다. 지금 이 모듈은 main·test 어느 클래스패스에서도 redis를 모른다)** ~~`infrastructure:redis` (**testImplementation**)~~ — `afterName` 문자열이 가리키는 `RedisModuleAutoConfiguration`이 실재하는지 리플렉션으로 단정하려고 테스트에서만 본다(아래 [`afterName`에 클래스 리터럴을 쓸 수 없는 이유](#aftername에-클래스-리터럴을-쓸-수-없는-이유-순환-회피)). `ApplicationContextRunner`가 `AutoConfigurations.of(...)`로 대상 클래스를 명시하므로, 테스트 클래스패스에 redis의 imports 파일이 있어도 redis auto-config가 저절로 로딩되지는 않는다.
- **main 클래스패스에서는 여전히 `infrastructure:redis`에 의존하지 않는다** — 챕터 02에서 rate limit의 표현 관심사(`@RateLimit`·`RateLimitAspect`·`RateLimitException`, 당시에는 `RateLimitCounterPort`까지)를 이 모듈로 올리고 Redis 카운터만 인프라에 남겨 **포트로 역전**했다. ~~방향은 이제 `redis → api-common`이다.~~ **(번복됨)** 그 뒤 계약을 `security-core`로 옮겨, 방향은 이제 **`infrastructure:redis` → `security-core` ← `api-common-module`** 이다 — redis도 이 모듈을 모르고, 이 모듈도 redis를 모른다.

### External
- `spring-boot-starter-web`·`spring-boot-starter-validation` (**api**) — `@RestControllerAdvice`. 소비 모듈도 각자 선언하지만 중복은 무해하다
- `springdoc-openapi-starter-webmvc-ui` (**api**, 버전은 루트 `ext.springdocVersion`) — 공용 응답 record가 `@Schema`를 갖는다
- `spring-security-core` (implementation) — `GlobalExceptionHandler`가 다루는 것은 core 예외 5종(`AccessDenied`/`BadCredentials`/`Disabled`/`Locked`/`Authentication`)뿐이라 starter 전체 대신 core만 선언한다
- `spring-boot-starter-aop` (**implementation** — 의도적) — `RateLimitAspect`의 `@Aspect`/`@Before`. 소비 앱이 컴파일에 필요한 것은 `@RateLimit` 애노테이션(이 모듈 소유)뿐이고, 런타임 AOP 활성화(aspectjweaver → `AopAutoConfiguration`)는 `logging-module`이 `starter-aop`를 `api`로 노출해 이미 3개 앱 클래스패스에 올려준다. **그 노출이 `implementation`으로 좁아지면 이 선언을 `api`로 승격해야 한다** — 그때 앱은 계속 컴파일되지만 aspect가 프록시되지 않아 `@RateLimit`이 조용히 무시된다

**이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar(`security-module` 선례).

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### rate limit aspect에 프로퍼티 스위치를 두지 않는다

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/ApiCommonRateLimitConfig.java`(imports 제거 전 `ApiCommonRateLimitAutoConfiguration.java`)
→ 클래스 선언 / `rateLimitAspect(RateLimitCounterPort)`

web-api뿐 아니라 admin-api·ceo-api의 로그인 엔드포인트도 `@RateLimit(IP, 10회/60초)`로 이 aspect에
의존한다. **앱별 on/off 프로퍼티는 그 보호를 조용히 제거하는 보안 회귀**가 되므로 추가하지 않는다.
등록 조건은 "`apicommon.ratelimit`을 스캔 목록에 둔 서블릿 앱"뿐이다(~~"카운터 빈이 있는 서블릿 앱"~~ — imports 제거로 빈 존재 조건 삭제). 끄려고 그 앱 `ModuleScanConfig`에서 패키지를 빼는 것도 같은 보안 회귀다.

### `@ConditionalOnWebApplication(SERVLET)` 두 건 — 재유입 방어선이므로 제거하지 않는다

> **(번복됨 — imports 제거)** 지금은 **한 건**이다 — `ApiCommonModuleAutoConfiguration`이 삭제돼 `ApiCommonRateLimitConfig`의 조건만 남았다(환경 조건이라 일반 `@Configuration`에서도 정확하므로 유지). 예외 핸들러의 재유입 방어는 조건이 아니라 batch·web `ModuleScanConfig`가 `apicommon.exception`을 스캔하지 않는 것이 담당한다.

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/ApiCommonRateLimitConfig.java`
→ `@ConditionalOnWebApplication(type = SERVLET)` (과거 대상: ~~`ApiCommonModuleAutoConfiguration.java`~~·`ApiCommonRateLimitAutoConfiguration.java`)

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

> **(번복됨 — imports 제거)** 이 봉인은 해제됐다. 등록 설정 클래스가 삭제되고 `GlobalExceptionHandler`가 스캔으로 직접 등록되면서 빈 이름은 기본값 **`globalExceptionHandler`**가 됐다(주입처 없음 — 영향 없음). 원래 취지인 "조용히 덮이지 않는다"는 지금도 성립한다 — web이 실수로 `apicommon.exception`을 스캔하면 web 자체 핸들러와 같은 기본 이름이 겹쳐 스캔 시점에 기동이 실패한다. 아래는 당시 기록이다.

**대상**: ~~`backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ApiCommonModuleAutoConfiguration.java`
→ `@Bean("sharedGlobalExceptionHandler")`~~ (삭제됨)

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

**(갱신 — 덩어리 01)** 지금 `handleRateLimitException`은 `ErrorCode`가 아니라 그 미러
`com.tastyhouse.application.shared.error.ErrorContracts#rateLimit`의 code·message로 조립한다(값 동일 —
`ErrorContractsConsistencyTest`가 보증). 이 모듈 클래스패스에 `domain`이 없으므로 결합은 이제 컴파일 수준에서도 불가능하다.

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

> **(번복됨 — imports 제거)** 이 클래스는 삭제됐다. 지금은 반대로 **앱이 이 모듈의 패키지를 골라 스캔**한다 — admin·ceo는 `apicommon.exception`·`apicommon.ratelimit`, web은 `apicommon.ratelimit`만. `com.tastyhouse.apicommon` 루트를 통째로 스캔하지 않는 이유는 아래 본문과 같다 — 나머지 공용 자산은 빈이 아니라 타입이다.

**대상**: ~~`backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ApiCommonModuleAutoConfiguration.java`~~ (삭제됨) → 지금은 각 앱 부트스트랩의 `ModuleScanConfig`

이 모듈의 나머지 공용 자산(`ApiResponse`·`PageRequest`·`ClientIpResolver` 등)은 **빈이 아니라 타입**이라
등록할 것이 없다. 앱별로 켜고 꺼야 하는 빈만 조건부 `@Bean`으로 등록한다.

### `afterName`에 클래스 리터럴을 쓸 수 없는 이유 (순환 회피)

> **(번복됨 — imports 제거)** `afterName`과 `@ConditionalOnBean(RateLimitCounterPort.class)`는 삭제됐고, 그것을 검증하던 `rateLimitAutoConfigurationAfterNameResolvesToRealClass` 테스트와 `testImplementation project(':infrastructure:redis')`도 함께 삭제됐다. 순서를 선언할 필요가 없어졌기 때문이다 — 일반 `@Configuration`끼리는 순서 선언 수단이 없고, 빈 존재 조건을 쓰지 않으므로 순서가 결과를 바꾸지 않는다. 아래는 당시 기록이다.

**대상**: ~~`backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/ApiCommonRateLimitAutoConfiguration.java`
→ `@AutoConfiguration(afterName = "com.tastyhouse.infrastructure.redis.RedisModuleAutoConfiguration")`~~ (삭제됨)

~~의존 방향이 `infrastructure:redis → api-common-module`이라 api-common은 redis 모듈의 타입을 **컴파일
시점에 볼 수 없다**(참조하면 순환).~~ **(번복됨 — 지금은 순환이 아니다.)** 방향이
`infrastructure:redis` → `security-core` ← `api-common-module`로 바뀌어 순환 위험은 사라졌지만, api-common의
main 클래스패스에는 여전히 redis가 없어(표현 모듈이 구현 모듈을 끌어오지 않는다) 그 타입을 **컴파일
시점에 볼 수 없다.** 그래서 문자열 FQCN으로 순서만 선언한다. 스캔된
`RedisRateLimitCounter` 정의는 redis auto-config 처리 시점에 등록되므로, 이 순서가
`@ConditionalOnBean(RateLimitCounterPort.class)`의 가시성을 보장한다.

**이 문자열의 검증 테스트는 이제 이 모듈에 있다.** 문자열이라 클래스를 리네임·이동해도 컴파일이 깨지지
않고 조건 평가만 조용히 어긋나므로, `ApiCommonAutoConfigurationTest#rateLimitAutoConfigurationAfterNameResolvesToRealClass`
가 그 FQCN이 실제 클래스로 해석되는지 단정하는 유일한 방어선이다. 과거에는 이 모듈이 redis를 볼 수 없어
`infrastructure:redis`의 `RedisModuleAutoConfigurationTest`에 있었으나, `testImplementation project(':infrastructure:redis')`
로 이 모듈이 테스트에서 redis를 볼 수 있게 되며 옮겨 왔다(~~redis 모듈에 둔다~~ 번복됨) — **소비 쪽이
구현의 등록 순서를 검증**하는 방향이 되고, redis → api-common 간선은 테스트 클래스패스에서도 사라졌다.

### `GlobalExceptionHandler` — web-api가 이 핸들러를 쓰지 않는 이유

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/exception/GlobalExceptionHandler.java`
→ 클래스 선언 / `handleUnexpected` (과거 `handleBusinessException` — 삭제됨)

검증 실패 메시지 형식이 다르다 — web-api는 `"필드명: 메시지"`를 `", "`로 join하는 반면 여기서는
메시지만 공백으로 join한다. 이는 우연한 차이가 아니라 **소비자별 응답 계약 차이**이므로 통합하지 않고
web-api가 자체 `com.tastyhouse.webapi.exception.GlobalExceptionHandler`를 유지한다.

외부 연동 실패는 도메인 `BusinessException`(에러코드는 `ErrorCode`)으로 표현되므로 `handleBusinessException` 하나로 처리된다(과거 `ExternalApiException`이 `BusinessException`을 상속하던 시절과 처리 경로는 같다 — 그 예외 타입 자체는 이후 완전히 삭제됐고 상수는 도메인 `ErrorCode`로 이관됐다)
(전용 핸들러가 없는 것은 누락이 아니다).

**번복됨 (덩어리 01) — `handleBusinessException`(`@ExceptionHandler(BusinessException.class)`)은 삭제됐다.**

| 항목 | before | after |
|---|---|---|
| `BusinessException` 처리 메서드 | `handleBusinessException` | 없음 — `@ExceptionHandler(Exception.class)` 폴백 `handleUnexpected`가 먼저 `ErrorResponses.resolve(e)`를 호출 |
| 처리 결과 | `getErrorCode()`의 상태·code + `getMessage()` | `resolve`가 값을 주면 `warn("BusinessException [{}]: {}")` 후 그 `status`·`code`·`message`로 `ProblemDetail`, 값이 없으면 기존 500 |
| 권한 거부(`handleAccessDenied`) | `ErrorCode.ACCESS_DENIED` | `ErrorContracts.accessDenied()` |
| wire 계약 | — | 불변(변경 전후 jar 응답 diff로 확인) |

전용 메서드를 되살리지 않는다 — 되살리면 이 모듈이 다시 `com.tastyhouse.domain.exception`을 import해야 하고
`shouldNotDependOnDomain`이 실패한다. `resolve`가 `getCause()`를 따라가지 않는 이유(래핑 예외가 500→4xx로
바뀌는 것 방지)와 정적 유틸인 이유는 `backend/application/AGENTS.md`의 `ErrorResponses` 항목에 있다. 외부 연동 실패도
최상위가 `BusinessException`이므로 같은 경로로 처리된다.

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
`ApiCommonRateLimitConfig.java` → `rateLimitAspect(RateLimitCounterPort)` — 과거 `ApiCommonRateLimitAutoConfiguration.java`의 `@ConditionalOnBean(RateLimitCounterPort.class)`는 imports 제거로 삭제)

> **이전 판단 — 표현 계층에 둔다 (번복됨)**: 챕터 02 당시 이 포트는 `com.tastyhouse.apicommon.ratelimit`에
> 있었다. 그 전에는 api-common-module이 `RateLimitException` 처리를 위해 `infrastructure:redis`를 의존했고,
> 그 인프라 모듈이 `HttpServletRequest`로 클라이언트 IP를 해석하느라 **서블릿 스택까지 끌어왔다.** 웹
> 관심사를 표현으로 올리고 카운터만 인프라에 남기면서 의존 방향이 바로잡혔다 — 그 교정 자체는 지금도
> 유효하다. 틀린 것은 **계약의 거처**였다.

계약을 표현 모듈에 두자 아웃바운드 어댑터 `infrastructure:redis`가 포트 인터페이스 **하나** 때문에
`starter-web`·springdoc을 노출하는 표현 모듈을 컴파일 의존하는 수평 간선이 남았다. 포트 시그니처는
`String`·`int`·`Duration`뿐이라 서블릿·HTTP와 무관하고, 실사용처도 로그인 브루트포스 방어라는 보안
관심사다. 그래서 토큰 저장소 포트 6종과 같은 "계약은 `security-core`, 구현은 `infrastructure:redis`" 구조로
옮겼다(본문 동일, `git mv`). 이 모듈은 `implementation project(':security-core')`로 포트를 받고, 키 조립 등
HTTP 관심사는 그대로 이 모듈의 `RateLimitAspect`에 남는다. 상세 근거와 기각한 대안(별도 초소형 모듈·`domain`)은
`security-core/AGENTS.md`의 "rate limit 카운터 계약을 여기 둔 이유".

카운팅 방식은 Fixed Window이고, 카운터를 어디에 저장하는지는 이 계약의 관심사가 아니다. Redis 키·Lua·
배선 조건은 이동 전후로 동일하다(동작 변경 없음).

### `RateLimitAspect`의 책임 경계

**대상**: `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/ratelimit/RateLimitAspect.java`
→ 클래스 선언 / `buildKey` / `resolveFieldValue`

키 조립(클라이언트 IP·요청 필드 해석)은 **HTTP 어댑터 관심사이므로 이 표현 모듈이 소유**하고, 실제
카운팅만 `RateLimitCounterPort` 구현체(인프라)에 위임한다. `keyType=FIELD`의 필드 추출은 리플렉션으로
**레코드 컴포넌트 접근자(`phoneNumber()`) → getter(`getPhoneNumber()`)** 순으로 시도하며, 찾지 못하면
예외 대신 `"unknown"` 식별자로 폴백한다(요청을 막지 않는다).

### `ApiCommonAutoConfigurationTest`가 증명하는 것과 증명하지 못하는 것

> **(번복됨 — imports 제거)** 이 테스트는 `src/test/java/com/tastyhouse/apicommon/ratelimit/ApiCommonRateLimitConfigTest.java`로 옮겨지며 **2건**만 남았다 — `rateLimitAspectRegisteredInServletContext`(서블릿 컨텍스트에서 등록)와 `rateLimitAspectAbsentInNonServletContext`(비-서블릿에서 미등록). afterName 검증·핸들러 back-off·aspect 빈 존재 조건 등 대상이 사라진 4건은 삭제됐다. 핸들러가 web에 뜨지 않는 것은 이제 조건이 아니라 스캔 목록이므로, 그 보증은 각 앱 `ApplicationLayerScanConfigTest`의 `assertScansModulesWithoutFilters`가 맡는다. 같은 정리로 이 모듈 `LayerRulesTest`의 클래스 수 하한이 14 → 13으로 내려갔다(설정 클래스 1개 삭제). "단위 수준 근거일 뿐, 실제 회귀 방지는 앱 기동 실측"이라는 아래 결론은 유효하다.

**대상**: `backend/api-common-module/src/test/java/com/tastyhouse/apicommon/ratelimit/ApiCommonRateLimitConfigTest.java`(과거 `ApiCommonAutoConfigurationTest.java`)
→ 클래스 선언

`ApplicationContextRunner` 기본값이 비-웹 컨텍스트라 batch의 `web-application-type: none`에 해당한다.
`rateLimitAutoConfigurationAfterNameResolvesToRealClass`는 `afterName` 문자열이 실제 클래스를 가리키는지를
증명한다(`infrastructure:redis`에서 옮겨 온 테스트 — 위 [`afterName` 절](#aftername에-클래스-리터럴을-쓸-수-없는-이유-순환-회피)).
이 테스트는 **단위 수준 근거**일 뿐이고, 실제 회귀 방지는 4개 앱 기동 후의 조건 리포트·로그인 rate
limit 실측이 담당한다.

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
| `build.gradle` | `java-library` + web/validation/aop starter, springdoc. `domain-module`만 내부 의존이며 `api`(공개 시그니처에 `PageResult`·`FileUploadService` 노출). **`infrastructure:redis`에 의존하지 않는다** — 챕터 02에서 방향이 역전돼 이제 redis가 이 모듈을 의존한다. `bootJar` 비활성 |

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
- `domain-module` (**api**) — `PaginationResponse.from(PageResult<T>)`의 공개 시그니처에 domain 타입이 노출되므로 `api`로 둔다. 그 노출을 타고 `domain.exception`(`BusinessException`·`ErrorCode`)도 api 3모듈의 **공용 에러 계약**이 된다.
- **`infrastructure:redis`에 의존하지 않는다** — 챕터 02에서 rate limit의 표현 관심사(`@RateLimit`·`RateLimitAspect`·`RateLimitException`·`RateLimitCounterPort`)를 이 모듈로 올리고 Redis 카운터만 인프라에 남겨 **포트로 역전**했다. 방향은 이제 `redis → api-common`이다.

### External
- `spring-boot-starter-web`·`spring-boot-starter-validation` (**api**) — `@RestControllerAdvice`. 소비 모듈도 각자 선언하지만 중복은 무해하다
- `springdoc-openapi-starter-webmvc-ui` (**api**, 버전은 루트 `ext.springdocVersion`) — 공용 응답 record가 `@Schema`를 갖는다
- `spring-security-core` (implementation) — `GlobalExceptionHandler`가 다루는 것은 core 예외 5종(`AccessDenied`/`BadCredentials`/`Disabled`/`Locked`/`Authentication`)뿐이라 starter 전체 대신 core만 선언한다
- `spring-boot-starter-aop` (**implementation** — 의도적) — `RateLimitAspect`의 `@Aspect`/`@Before`. 소비 앱이 컴파일에 필요한 것은 `@RateLimit` 애노테이션(이 모듈 소유)뿐이고, 런타임 AOP 활성화(aspectjweaver → `AopAutoConfiguration`)는 `logging-module`이 `starter-aop`를 `api`로 노출해 이미 3개 앱 클래스패스에 올려준다. **그 노출이 `implementation`으로 좁아지면 이 선언을 `api`로 승격해야 한다** — 그때 앱은 계속 컴파일되지만 aspect가 프록시되지 않아 `@RateLimit`이 조용히 무시된다

**이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar(`security-module` 선례).

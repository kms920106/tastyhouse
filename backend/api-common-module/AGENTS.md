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
| `src/main/java/com/tastyhouse/apicommon/exception/` | `GlobalExceptionHandler` — **admin-api·ceo-api 전용**. web-api는 자체 핸들러를 쓴다(아래 스캔 주의) |
| `src/main/java/com/tastyhouse/apicommon/ratelimit/` | rate limit **표현 관심사 전부** — `@RateLimit`·`RateLimitKeyType`·`RateLimitAspect`(키 조립: IP·요청 필드 해석)·`RateLimitException`·계약 `RateLimitCounterPort`·부분 진입점 `ApiCommonRateLimitConfig`. 카운터 구현은 `infrastructure:redis`의 `RedisRateLimitCounter`(챕터 02) |
| `src/main/java/com/tastyhouse/apicommon/file/` | `FileService` — `MultipartFile`을 도메인 `FileUploadCommand`로 바꾸는 얇은 업로드 어댑터(조회·URL 변환 책임 없음) |
| `src/main/java/com/tastyhouse/apicommon/shop/response/` | admin↔ceo 바이트 동일이던 shop 응답 record 3종(`ShopBreakTimeResponse`·`ShopBusinessHourResponse`·`ShopHygieneBadgeResponse`) |

## 스캔 주의 (빈 등록 범위가 곧 동작)
`GlobalExceptionHandler`(`@RestControllerAdvice`)와 `FileService`(`@Service`)는 빈이므로, 어느 앱이 이 모듈의 어느 패키지를 스캔하는지가 그대로 런타임 동작이 된다.

| 앱 | 스캔 범위 | 이유 |
|---|---|---|
| `AdminApiApplication` / `CeoApiApplication` | `com.tastyhouse.apicommon` 전체(`ApiCommonConfig`) | 공용 핸들러·`FileService`·`RateLimitAspect`를 모두 사용 |
| `WebApiApplication` | `.file` + `.ratelimit` **만**(`ApiCommonFileConfig` + `ApiCommonRateLimitConfig`) | web 전용 핸들러 4종(`ExternalApiException` 등)과 다른 검증 메시지 형식을 가진 자체 `GlobalExceptionHandler`를 쓴다. `exception` 패키지까지 스캔하면 핸들러가 2개가 되어 어느 쪽이 이기는지가 불확정해진다 |

**부분 진입점을 쓰는 앱에 패키지를 추가할 때는 그 앱의 `@Import`도 함께 늘린다.** 챕터 02에서 `ratelimit`을 이 모듈로 들여올 때 실제로 걸린 함정이다 — 이전에는 aspect가 `infrastructure:redis`(= 3개 앱이 모두 `@Import` 하는 `RedisModuleConfig`의 스캔 범위)에 있어 자동으로 등록됐는데, 이 모듈로 올라오면서 `ApiCommonConfig` 전체 스캔을 쓰지 않는 web-api에서만 aspect가 사라진다. 컴파일·기동 모두 통과하고 `@RateLimit`만 조용히 무시되므로 `ApiCommonRateLimitConfig`를 신설해 명시적으로 import 했다. `ApiCommonConfig`는 이 두 부분 진입점을 `excludeFilters`로 제외한다(중복 설정 빈 방지).

## 여기에 두면 안 되는 것
- **모듈마다 내용이 다른 정책 파일** — `SecurityConfig`(필터체인·인가 정책), `PublicPaths`(공개 경로 목록: web 18줄 vs admin/ceo 3줄), `TokenService`/`AuthService`(인증 주체 `Admin`/`Ceo`와 JWT 시크릿이 분리되어야 함).
- **필드 셋이나 `@Schema` 문구가 다른 응답 record** — 예: `ShopDetailResponse`(admin은 감사 시각, ceo는 `trademarkImageUrl`/`hidden`), `ShopAmenityResponse`("가게" vs "내 가게").
- **도메인 포트가 있는 기술 어댑터** — 그것은 `external-api`(도메인 포트 구현)나 `infrastructure:persistence`(DB 어댑터) 소관이다.
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

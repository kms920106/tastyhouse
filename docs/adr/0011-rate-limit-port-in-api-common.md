# ADR-0011: RateLimitCounterPort를 api-common-module로 올려 redis→api-common 방향으로 역전한다

- 상태: 대체됨(→ [ADR-0023](0023-rate-limit-port-in-security-core.md))
- 결정 커밋: `8bcf3e2f3` (2026-09-01)
- 대체한 결정: —

## 맥락

- **rate limit**: 같은 클라이언트의 요청 횟수를 제한하는 기능. `@RateLimit` 애노테이션 + aspect + Redis 카운터로 구현돼 있다.
- **포트 역전**: 구현(인프라)이 계약(인터페이스)을 의존하게 방향을 뒤집는 것.

당시 표현 공통 모듈 `api-common-module`이 `RateLimitException` 처리를 위해 인프라 모듈 `infrastructure:redis`를 의존했고, 그 인프라 모듈은 `HttpServletRequest`로 클라이언트 IP를 해석하느라 **서블릿 스택까지 끌어왔다.** 표현이 인프라를 알고, 인프라가 웹을 아는 역방향 2건이었다.

## 이전 규칙

- `api-common-module` → `infrastructure:redis` 구현 의존.
- `@RateLimit`·`RateLimitAspect`·`RateLimitException`·`RateLimitKeyType`과 IP 해석이 `infrastructure:redis`에 있었고, redis가 `starter-web`·`starter-aop`를 의존했다. 카운터 구현 이름은 `RateLimiterService`였다.

### `backend/api-common-module/AGENTS.md` — Key Files · Dependencies / Internal

이 결정 직후 두 절은 이것을 "챕터 02의 포트 역전"으로 기록했다 — 표현 관심사(`@RateLimit`·`RateLimitAspect`·`RateLimitException`, 당시에는 `RateLimitCounterPort`까지)를 이 모듈로 올리고 Redis 카운터만 인프라에 남겨, 방향이 `redis → api-common`이 됐다고 적었다. 이후 계약이 `security-core`로 옮겨지면서([ADR-0023](0023-rate-limit-port-in-security-core.md)) 이 서술은 `infrastructure:redis` → `security-core` ← `api-common-module`로 바뀌었다.

## 결정

웹 관심사를 표현으로 올리고 Redis 카운터만 인프라에 남긴다.

| 항목 | before | after |
|---|---|---|
| `@RateLimit`·`RateLimitAspect`·`RateLimitException`·`RateLimitKeyType` | `infrastructure:redis` | `api-common-module`(`com.tastyhouse.apicommon.ratelimit`) |
| 카운터 계약 | 없음 | `RateLimitCounterPort` 신설 — `api-common-module` 소유 |
| 카운터 구현 | `RateLimiterService` | `RedisRateLimitCounter`(redis 잔류, Lua 스크립트·키 불변) |
| 의존 방향 | `api-common` → `redis` | `redis` → `api-common` |
| redis의 웹 의존 | `starter-web`·`starter-aop` | 제거 |
| IP 해석 | aspect 인라인 | `apicommon.common.ClientIpResolver`로 통합 |

## 근거

- 표현 공통 모듈이 인프라 구현을 의존하는 것, 인프라 모듈이 서블릿 스택을 끌어오는 것 모두 계층 방향 위반이었다. 웹 관심사를 표현에, 카운터만 인프라에 두면 방향이 바로잡힌다.
- 이 교정 자체는 지금도 유효하다(원문 "그 교정 자체는 지금도 유효하다. 틀린 것은 계약의 거처였다").

## 결과·감수한 손실

- 동작 변경 없음 — `@RateLimit` 의미·429 응답 포맷·Redis key(keyPrefix 20건) 불변(커밋 메시지).
- web-api는 자체 `GlobalExceptionHandler` 때문에 api-common 부분 진입점만 import해서 aspect가 스캔되지 않는 구멍이 생겼다(`@RateLimit`이 조용히 무시됨). 이를 메우려고 `ApiCommonRateLimitConfig`를 신설해 명시 import했고, "부분 진입점에 패키지를 추가할 때 앱의 `@Import`도 함께 늘린다"는 규칙이 생겼다. 이 함정은 이후 등록 방식 변경([ADR-0017](0017-library-auto-configuration.md))으로 사라졌다.
- 계약이 표현 모듈에 있어 인프라(redis)가 표현 모듈을 의존하는 간선이 남았다.
- **대체**: `RateLimitCounterPort`는 `security-core`로 옮겨져 지금 방향은 `infrastructure:redis` → `security-core` ← `api-common-module`이다 — redis도 api-common을 모르고 api-common도 redis를 모른다([ADR-0023](0023-rate-limit-port-in-security-core.md)).

## 영향받은 문서

- `backend/api-common-module/AGENTS.md` · "코드 주석에서 이관된 설계 근거" > "`RateLimitCounterPort`를 security-core로 옮긴 이유" · 계약의 현재 거처와 이유
- `backend/api-common-module/AGENTS.md` · "Dependencies" > "Internal" · main 클래스패스에 redis 의존이 없다
- `backend/api-common-module/AGENTS.md` · "코드 주석에서 이관된 설계 근거" > "`RateLimitAspect`의 책임 경계" · aspect가 소유하는 것
- `backend/infrastructure/redis/AGENTS.md` · "Dependencies" > "Internal" · redis는 `security-core`만 의존
- `backend/AGENTS.md` · "Module Dependency Graph" · `infrastructure:redis` → `security-core` 간선

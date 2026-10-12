# ADR-0023: RateLimitCounterPort를 security-core로 옮겨 redis→표현 계층 의존을 끊는다

- 상태: 채택
- 결정 커밋: `8485c0e8a` (2026-09-27)
- 대체한 결정: [ADR-0011](0011-rate-limit-port-in-api-common.md)

## 맥락

**`RateLimitCounterPort`**는 rate limit(일정 시간 안의 호출 횟수 제한) 카운터 계약입니다. 시그니처는 `boolean isLimitExceeded(String key, int limit, Duration duration)`이고, 구현은 `infrastructure:redis`의 `RedisRateLimitCounter`, 소비자는 `api-common-module`의 `RateLimitAspect`입니다.

챕터 02([ADR-0011](0011-rate-limit-port-in-api-common.md))에서 rate limit의 웹 관심사(애노테이션·aspect·예외·키 조립)를 표현 모듈 `api-common-module`로 올렸습니다. 이때 계약 `RateLimitCounterPort`도 함께 `com.tastyhouse.apicommon.ratelimit`에 두었습니다. 그러자 아웃바운드 어댑터 `infrastructure:redis`가 포트 인터페이스 **하나** 때문에 `starter-web`·springdoc을 노출하는 **표현 모듈**을 컴파일 의존하는 수평 간선이 남았습니다.

## 이전 규칙

### backend/api-common-module/AGENTS.md — `RateLimitCounterPort`의 거처(챕터 02)

이 포트는 `com.tastyhouse.apicommon.ratelimit`에 둔다. 그 전에는 api-common-module이 `RateLimitException` 처리를 위해 `infrastructure:redis`를 의존했다. 그 인프라 모듈은 `HttpServletRequest`로 클라이언트 IP를 해석하느라 서블릿 스택까지 끌어왔다. 그래서 웹 관심사를 표현으로 올리고 카운터만 인프라에 남겨 의존 방향을 바로잡았다. 이 모듈은 rate limit의 표현과 함께 **계약(`RateLimitCounterPort`)도 둔다**.

### backend/infrastructure/redis/AGENTS.md — 패키지 구조 / Internal

의존 방향은 `infrastructure:redis → api-common-module`(어댑터 → 계약) 한 방향뿐이다. `api-common-module`(implementation)은 `RedisRateLimitCounter`가 구현하는 `RateLimitCounterPort`의 소유 모듈이다. 토큰 저장소 어댑터가 `security-core` 포트를 구현하는 구조([ADR-0019](0019-token-store-port-inversion.md))는 이것과 "동형"이라고 설명했다.

### backend/api-common-module/AGENTS.md — Subdirectories / 여기에 두면 안 되는 것

이 모듈의 rate limit 패키지가 카운터 계약 `RateLimitCounterPort`를 소유한다고 적었고, "여기에 두면 안 되는 것"에서도 이 모듈은 표현과 함께 계약(`RateLimitCounterPort`)만 두고 구현은 인프라 모듈이 맡는다고 했다. 이 결정으로 계약도 이 모듈을 떠났다.

### backend/infrastructure/redis/AGENTS.md — Dependencies / External

`api-common-module`을 `implementation`으로 의존했으므로 테스트 컴파일 클래스패스에도 보여, 테스트용 `api-common-module` 별도 선언은 불필요하다고 적었다. 이 결정으로 main 의존이 삭제됐고 `afterName` 문자열 가드 테스트도 `api-common-module`로 옮겨져, redis → api-common 방향은 테스트 클래스패스에서도 사라졌다.

### backend/AGENTS.md — Module Dependency Graph

`infrastructure:redis`가 `security-core`(토큰 저장소 포트) 외에 `api-common-module (implementation)`도 의존하는 간선(`RateLimitCounterPort` 구현)을 그렸고, api-common 쪽에도 "infrastructure:redis가 이 모듈의 RateLimitCounterPort를 구현한다"고 적었다. 지금은 redis의 내부 의존은 security-core 하나이고, api-common은 `security-core`를 `implementation`(api 노출 안 함)으로 받는다.

### backend/api-common-module/AGENTS.md — `afterName`에 클래스 리터럴을 쓸 수 없는 이유

aspect를 등록하는 auto-configuration이 redis 설정 뒤에 평가되도록 `afterName`을 썼다. `afterName` 순서 검증 테스트는 redis 모듈에 있었다(redis → api-common 방향 테스트).

## 결정

계약 `RateLimitCounterPort`를 서블릿-프리 보안 코어 `security-core`(`com.tastyhouse.security.ratelimit`)로 옮깁니다(본문 동일, `git mv`). 키 조립 등 HTTP 관심사는 그대로 api-common의 `RateLimitAspect`에 남깁니다.

| 항목 | before | after |
|---|---|---|
| `RateLimitCounterPort` 소유 | `api-common-module`(`com.tastyhouse.apicommon.ratelimit`) | `security-core`(`com.tastyhouse.security.ratelimit`) |
| 의존 방향 | `infrastructure:redis → api-common-module` | `infrastructure:redis → security-core ← api-common-module` |
| `infrastructure:redis` 내부 의존 | `security-core` + `api-common-module` | `security-core` 하나 |
| api-common의 포트 획득 | 자기 소유 | `implementation project(':security-core')` |
| `afterName` 순서 검증 테스트 | redis 모듈 테스트 | api-common 테스트(`testImplementation` redis) |
| Redis 키·Lua·배선 조건·응답 | — | 변경 없음 |

## 근거

- **보안 관심사입니다.** `@RateLimit`의 실사용처는 로그인 브루트포스 방어(web·admin·ceo `login`)이고, 카운터는 인증 방어 수단입니다. JWT·토큰 저장소와 같은 모듈에 있는 것이 자연스럽습니다.
- **서블릿-프리입니다.** 시그니처가 `String`·`int`·`Duration`뿐이라 `security-core`의 존재 이유(서블릿 타입 금지)를 해치지 않습니다.
- **토큰 저장소 포트와 같은 구조입니다.** redis는 이미 토큰 저장소 구현 때문에 `security-core`를 의존하므로, **새 간선 없이** redis → api-common 간선만 사라집니다.
- 챕터 02의 교정(웹 관심사를 표현으로 올리고 카운터만 인프라에 남김)은 지금도 유효합니다. 틀린 것은 **계약의 거처**였습니다.
- 기각한 대안은 다음 두 가지입니다.
  - **포트 하나만 담는 초소형 모듈**: 모듈 수만 늘고, 소비자·구현자가 이미 `security-core`를 보거나 볼 수 있어 실익이 없습니다.
  - **`domain`**: rate limit은 비즈니스 규칙이 아니라 요청 방어 기술입니다. 또 redis가 `domain`을 모르는 순수 기술 모듈이라는 성질을 깹니다.

## 결과·감수한 손실

- 구현과 표현이 main 클래스패스에서 서로를 모릅니다. 테스트에서도 redis → api-common 방향이 사라졌습니다.
- `infrastructure:redis`의 compileClasspath에 `api-common-module`·`starter-web`이 없어야 합니다. 확인 명령은 `./gradlew :infrastructure:redis:dependencies --configuration compileClasspath | grep -cE 'project :api-common-module|starter-web'`이고, 2026-09-27 실측 0건이었습니다.
- 카운팅 방식(Fixed Window)·Redis 키·Lua·배선 조건은 이동 전후로 동일합니다(동작 변경 없음).
- 이후 [ADR-0038](0038-autoconfiguration-imports-removal.md)로 aspect 등록 설정이 `ApiCommonRateLimitAutoConfiguration`에서 `ApiCommonRateLimitConfig`로 바뀌면서 빈 존재 조건(`@ConditionalOnBean(RateLimitCounterPort.class)`)과 `afterName`, 그 `afterName` 문자열 검증 테스트가 없어졌습니다.

## 영향받은 문서

- `backend/api-common-module/AGENTS.md` · "`RateLimitCounterPort`를 security-core로 옮긴 이유" · 계약을 표현 모듈에 두자 redis가 표현 모듈을 의존하는 수평 간선이 남아 security-core로 옮겼고, 키 조립은 이 모듈에 남는다
- `backend/api-common-module/AGENTS.md` · "Subdirectories" / "여기에 두면 안 되는 것" · 카운터 계약은 security-core, 구현은 인프라가 맡고 이 모듈은 표현만 둔다
- `backend/security-core/AGENTS.md` · "rate limit 카운터 계약을 여기 둔 이유" · 근거 3개와 기각한 대안 2개
- `backend/security-core/AGENTS.md` · "Subdirectories" · `ratelimit/` 패키지의 `RateLimitCounterPort` 1종
- `backend/infrastructure/redis/AGENTS.md` · "패키지 구조" / "Internal" · 의존은 `infrastructure:redis → security-core ← api-common-module`이고, 내부 의존은 security-core 하나
- `backend/AGENTS.md` · "Module Dependency Graph" · redis·api-common이 모두 security-core를 의존한다
- `backend/CLAUDE.md` · "모듈 경계 규칙 (계층 × 앱 2차원 + 기술별 infrastructure)" (도메인 포트 없는 공유 기술은 별도 공유 모듈이 소유한다) · rate limit 계약의 거처

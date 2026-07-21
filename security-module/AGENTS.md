<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-07-22 -->

# security-module

## Purpose
`web-api`·`admin-api`가 공유하는 **보안/인증 지원 라이브러리 모듈**(`java-library`). Redis 기반 JWT 세션(RefreshToken/Blacklist/소셜 임시토큰 저장소)과 Rate Limiting(순수 Redis Lua + HTTP AOP)을 캡슐화한다.

이 관심사는 core 도메인의 포트가 아니라 두 presentation 모듈의 공통 보안 관심사이므로, **DB persistence 전용인 `infrastructure-module`이 아니라 별도 공유 모듈로 분리**되어 있다(모듈 경계 근거는 루트 [AGENTS.md](../AGENTS.md#module-dependency-graph) 참고). `infrastructure-module`이 `runtimeOnly`로 은닉되는 것과 달리, 이 모듈은 core 도메인 어댑터가 아니라 presentation 공유 유틸이므로 `implementation`으로 노출된다 — `TokenService` 등이 구체 클래스를 컴파일 타임에 직접 주입하는 현재 구조를 그대로 지원하기 위함이다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + web/aop/data-redis starter, `core-module`(implementation, `RateLimitException`이 `ErrorCode` 참조). `bootJar` 비활성 |
| `src/main/resources/application-security.yml` | `spring.data.redis`(host/port/password) 소유. web-api/admin-api가 `spring.config.import`로 로딩(`application-infrastructure.yml`/`application-external.yml`과 동일 패턴) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/security/redis/` | `RedisConfig` — `StringRedisTemplate` 빈(양쪽 API 공통, key/value `StringRedisSerializer`) |
| `src/main/java/com/tastyhouse/security/ratelimit/` | `RateLimiterService`(순수 Redis Lua, INCR+PEXPIRE 원자 실행) + `RateLimitAspect`(`@annotation` AOP) + `@RateLimit`(애노테이션) + `RateLimitKeyType`(IP/FIELD) + `RateLimitException` |
| `src/main/java/com/tastyhouse/security/token/` | `RefreshTokenRedisRepository`·`BlacklistRedisRepository`(접두사 주입형 — 아래 참고) + 소셜 임시토큰 4종(`Kakao`/`Naver`/`Apple`/`Facebook`TempTokenRedisRepository, 접두사 고정) |

## For AI Agents

### Working In This Directory
- **`RefreshTokenRedisRepository`/`BlacklistRedisRepository`는 키 접두사를 생성자 파라미터로 받는 순수 POJO**다(`@Repository` 자기 등록 아님). web-api는 `"rt:"`/`"bl:"`, admin-api는 `"admin:rt:"`/`"admin:bl:"`를 각자 `@Configuration`에서 빈 등록 시 주입한다 — reference: `web-api/config/jwt/RedisRepositoryConfig`, `admin-api/config/jwt/RedisRepositoryConfig`. 새 접두사가 필요한 저장소를 추가할 때도 이 패턴(접두사 주입형 POJO + 사용처 빈 등록)을 따른다.
- **소셜 임시토큰 저장소는 접두사가 고정**이므로 `@Repository` 자기 등록으로 두고 접두사 주입을 적용하지 않는다(web-api 전용, admin-api는 소셜 로그인이 없어 빈이 떠도 무해).
- **`RateLimitAspect`는 HTTP 계층(`HttpServletRequest`)에 의존**한다. 이 모듈이 core 도메인 어댑터가 아니라 presentation 공유 유틸이기 때문에 `spring-boot-starter-web`/`-aop` 의존이 허용된다(`external-api`도 starter-web 의존 선례 있음). Redis 접근이 없는 순수 로직을 이 모듈에 추가할 때도 이 전례를 따라 web 의존을 부담 없이 사용한다.
- **`RateLimitException`이 core를 의존하는 유일한 이유**는 `ErrorCode.RATE_LIMIT_EXCEEDED`(core-module) 참조뿐이다. 이 모듈에 core 도메인 타입을 새로 끌어들이는 확장은 지양한다 — 필요하면 이 모듈 전용 `ErrorCode` 상당물을 두는 것을 우선 검토한다.
- 이 모듈에 새 기술 인프라(Redis 외)를 추가할지 판단할 때 기준: **core에 포트가 없고, web-api·admin-api가 공유하는 관심사**면 이 모듈. core 포트가 있으면 `infrastructure-module`(JPA) 또는 그에 준하는 어댑터 모듈. 특정 API 하나만 쓰면 해당 API 모듈에 잔류.

### Testing Requirements
- `RateLimiterService`의 Lua 스크립트(INCR+PEXPIRE)는 실제 Redis(테스트컨테이너 등)로 검증 권장 — mock으로는 원자성 검증이 어렵다.
- 접두사 주입형 저장소는 각 API의 빈 등록(`RedisRepositoryConfig`)이 올바른 접두사를 넘기는지 통합 테스트로 확인한다(뒤바뀌면 로그인 세션이 조용히 무효화됨).

## Dependencies

### Internal
- `core-module` (implementation) — `ErrorCode` 참조(`RateLimitException`)

### External
- `spring-boot-starter-web`, `spring-boot-starter-aop`, `spring-boot-starter-data-redis`
- Lombok

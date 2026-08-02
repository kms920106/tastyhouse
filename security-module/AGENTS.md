<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-07-22 | Updated: 2026-07-31 -->

# security-module

## Purpose
`web-api`·`admin-api`·`ceo-api`가 공유하는 **보안/인증 지원 라이브러리 모듈**(`java-library`). Redis 기반 JWT 세션(RefreshToken/Blacklist/소셜 임시토큰 저장소)과 Rate Limiting(순수 Redis Lua + HTTP AOP)을 캡슐화한다.

이 관심사는 domain 계층의 포트가 아니라 presentation 모듈들의 공통 보안 관심사이므로, **DB 어댑터 전용인 `infrastructure-module`이 아니라 별도 공유 모듈로 분리**되어 있다(모듈 경계 근거는 루트 [AGENTS.md](../AGENTS.md#module-dependency-graph) 참고). 이 모듈은 domain 어댑터가 아니라 presentation 공유 유틸이므로 `implementation`으로 노출된다 — `TokenService` 등이 구체 클래스를 컴파일 타임에 직접 주입하는 현재 구조를 그대로 지원하기 위함이다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + web/aop/data-redis starter, `domain-module`(implementation — 공용 JWT provider가 도메인 타입을 참조. `RateLimitException`의 `ErrorCode` 결합은 해소되어 `ratelimit` 패키지는 도메인 의존 0건). `bootJar` 비활성 |
| `src/main/resources/application-security.yml` | `spring.data.redis`(host/port/password) 소유. web-api/admin-api/ceo-api가 `spring.config.import`로 로딩(`application-infrastructure.yml`/`application-external.yml`/`application-logging.yml`과 동일 패턴) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/security/redis/` | `RedisConfig` — `StringRedisTemplate` 빈(양쪽 API 공통, key/value `StringRedisSerializer`) |
| `src/main/java/com/tastyhouse/security/ratelimit/` | `RateLimiterService`(순수 Redis Lua, INCR+PEXPIRE 원자 실행) + `RateLimitAspect`(`@annotation` AOP) + `@RateLimit`(애노테이션) + `RateLimitKeyType`(IP/FIELD) + `RateLimitException` |
| `src/main/java/com/tastyhouse/security/token/` | `RefreshTokenRedisRepository`·`BlacklistRedisRepository`(접두사 주입형 — 아래 참고) + 소셜 임시토큰 4종(`Kakao`/`Naver`/`Apple`/`Facebook`TempTokenRedisRepository, 접두사 고정) |
| `src/main/java/com/tastyhouse/security/jwt/` | web/admin 공유 JWT 인증 메커니즘 — `JwtTokenProvider`(파라미터형 POJO), `JwtAuthenticationFilter`(POJO), `JwtProperties`, `TokenType`, `JwtAuthenticationEntryPoint`/`JwtAccessDeniedHandler`(@Component), `JwtPrincipal`/`JwtPrincipalFactory`(앱별 principal 흡수용 계약). 아래 참고 |

## For AI Agents

### Working In This Directory
- **`RefreshTokenRedisRepository`/`BlacklistRedisRepository`는 키 접두사를 생성자 파라미터로 받는 순수 POJO**다(`@Repository` 자기 등록 아님). web-api는 `"rt:"`/`"bl:"`, admin-api는 `"admin:rt:"`/`"admin:bl:"`를 각자 `@Configuration`에서 빈 등록 시 주입한다 — reference: `web-api/config/jwt/RedisRepositoryConfig`, `admin-api/config/jwt/RedisRepositoryConfig`. 새 접두사가 필요한 저장소를 추가할 때도 이 패턴(접두사 주입형 POJO + 사용처 빈 등록)을 따른다.
- **소셜 임시토큰 저장소는 접두사가 고정**이므로 `@Repository` 자기 등록으로 두고 접두사 주입을 적용하지 않는다(web-api 전용, admin-api는 소셜 로그인이 없어 빈이 떠도 무해).
- **`RateLimitAspect`는 HTTP 계층(`HttpServletRequest`)에 의존**한다. 이 모듈이 domain 어댑터가 아니라 presentation 공유 유틸이기 때문에 `spring-boot-starter-web`/`-aop` 의존이 허용된다(`external-api`도 starter-web 의존 선례 있음). Redis 접근이 없는 순수 로직을 이 모듈에 추가할 때도 이 전례를 따라 web 의존을 부담 없이 사용한다.
- **`RateLimitException`이 `domain-module`을 의존하는 유일한 이유**는 `ErrorCode.RATE_LIMIT_EXCEEDED`(`com.tastyhouse.domain.exception.ErrorCode`) 참조뿐이다. 이 모듈에 도메인 타입을 새로 끌어들이는 확장은 지양한다 — 필요하면 이 모듈 전용 `ErrorCode` 상당물을 두는 것을 우선 검토한다.
- 이 모듈에 새 기술 인프라(Redis 외)를 추가할지 판단할 때 기준: **`domain-module`에 포트가 없고, presentation 모듈들이 공유하는 관심사**면 이 모듈. domain 포트가 있으면 `infrastructure-module`(JPA/조회) 또는 `external-api`(외부 연동). 특정 API 하나만 쓰면 해당 API 모듈에 잔류.

### `jwt/` 공용 JWT 인증 메커니즘 (web/admin 공유)
- **`JwtTokenProvider`는 `@Component`가 아닌 파라미터형 POJO**다. principal 식별자 클레임명(`memberId`/`adminId`)과 principal 재구성 팩토리(`JwtPrincipalFactory`)를 생성자로 받아 앱별 차이를 흡수한다. 각 API는 이 클래스를 상속한 얇은 `@Component` 하위 클래스로 자기 등록한다 — reference: `web-api`/`admin-api`의 `config/jwt/JwtTokenProvider`(`super(props, "memberId"|"adminId", CustomUserDetails::new)`). web은 여기에 검증용 토큰(휴대폰/이메일/개인정보/비밀번호 재설정) 발급 메서드를 **web 전용으로만** 추가한다(admin은 미사용). `key`/`parseClaims`/`jwtProperties`는 `protected`라 하위 클래스가 검증 토큰 발급에 재사용한다.
- **`JwtAuthenticationFilter`도 POJO**다(`@Component` 아님). `JwtTokenProvider` + `BlacklistRedisRepository`(접두사 주입 빈) + `ObjectMapper`를 받아 각 API의 `config/jwt/JwtConfig`가 빈 등록한다. 블랙리스트 검사는 저장소를 직접 호출한다(과거 web이 `TokenService.isBlacklisted`를 경유하던 단순 위임을 필터 안으로 흡수).
- **`JwtAuthenticationEntryPoint`/`JwtAccessDeniedHandler`는 `@Component`**다. 두 API의 `scanBasePackages`에 `com.tastyhouse.security`가 포함되어 자동 스캔되므로 별도 빈 등록이 불필요하다. `SecurityConfig`는 타입으로 주입만 받는다.
- **`CustomUserDetails`는 각 API에 잔류**하되 `JwtPrincipal`을 구현해 `getPrincipalId()`(web=memberId, admin=adminId)를 노출한다. 공용 provider가 이 계약으로 식별자를 클레임에 싣고 재구성한다.
- **시크릿은 각 API의 `application.yml`이 소유하며 web-api와 admin-api는 반드시 서로 다른 `jwt.secret`(예: `JWT_SECRET` vs `JWT_SECRET_ADMIN`)을 써야 한다.** 동일 시크릿이면 한쪽 access 토큰이 다른 쪽 인증을 통과해 권한 상승이 발생한다. 이는 `JwtProperties` Javadoc에도 명시되어 있다.
- **의존성**: 이 패키지가 Spring Security·jjwt 타입에 의존하므로 `build.gradle`에 `spring-boot-starter-security`(api)와 `jjwt-api`(api)/`jjwt-impl`·`jjwt-jackson`(runtimeOnly)을 둔다.

### Testing Requirements
- `RateLimiterService`의 Lua 스크립트(INCR+PEXPIRE)는 실제 Redis(테스트컨테이너 등)로 검증 권장 — mock으로는 원자성 검증이 어렵다.
- 접두사 주입형 저장소는 각 API의 빈 등록(`RedisRepositoryConfig`)이 올바른 접두사를 넘기는지 통합 테스트로 확인한다(뒤바뀌면 로그인 세션이 조용히 무효화됨).
- **JWT 시크릿 분리 회귀 방지**: web에서 발급한 access 토큰을 admin API에 제시하면 401(서명 불일치)이 되어야 한다. 두 앱이 같은 시크릿을 쓰지 않는지 통합 테스트/배포 체크로 확인한다.

## Dependencies

### Internal
- `domain-module` (implementation) — `com.tastyhouse.domain.exception.ErrorCode` 참조(`RateLimitException`)

### External
- `spring-boot-starter-web`, `spring-boot-starter-aop`, `spring-boot-starter-data-redis`
- `spring-boot-starter-security` (api) — 공용 JWT 필터/EntryPoint/AccessDeniedHandler/UserDetails 타입
- `jjwt-api` (api) + `jjwt-impl`·`jjwt-jackson` (runtimeOnly) — 공용 `JwtTokenProvider` 서명/파싱
- Lombok

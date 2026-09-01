<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-07-22 | Updated: 2026-07-31 -->

# security-module

## Purpose
`web-api`·`admin-api`·`ceo-api`(및 그 application 모듈)가 공유하는 **보안/인증 지원 라이브러리 모듈**(`java-library`). 공용 JWT 인증 메커니즘과 Redis 기반 JWT 세션 저장소(RefreshToken/Blacklist/소셜 임시토큰)를 캡슐화한다.

**Redis 연결·템플릿 빈과 Rate Limiting은 챕터 05에서 `infrastructure:redis`로 이관됐다** — 이 모듈은 그 저장소만 소유한다(아래 [Redis 위임](#redis-위임-챕터-05) 참고).

이 관심사는 domain 계층의 포트가 아니라 presentation 모듈들의 공통 보안 관심사이므로, **DB 어댑터 전용인 `infrastructure:persistence`가 아니라 별도 공유 모듈로 분리**되어 있다(모듈 경계 근거는 루트 [AGENTS.md](../AGENTS.md#module-dependency-graph) 참고). 이 모듈은 domain 어댑터가 아니라 presentation 공유 유틸이므로 `implementation`으로 노출된다 — `TokenService` 등이 구체 클래스를 컴파일 타임에 직접 주입하는 현재 구조를 그대로 지원하기 위함이다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + `domain-module`(implementation — 공용 JWT provider가 도메인 타입을 참조) + **`infrastructure:redis`(implementation — Redis 위임)** + starter-web + starter-security(`api`) + JJWT(`api`). `data-redis`를 직접 선언하지 않는다(`infrastructure:redis`가 `api`로 노출). `bootJar` 비활성 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/security/` | `SecurityModuleConfig` — `@ComponentScan` 진입점 |
| `src/main/java/com/tastyhouse/security/token/` | `RefreshTokenRedisRepository`·`BlacklistRedisRepository`(접두사 주입형 — 아래 참고) + 소셜 임시토큰 4종(`Kakao`/`Naver`/`Apple`/`Facebook`TempTokenRedisRepository, 접두사 고정) |
| `src/main/java/com/tastyhouse/security/jwt/` | web/admin 공유 JWT 인증 메커니즘 — `JwtTokenProvider`(파라미터형 POJO), `JwtAuthenticationFilter`(POJO), `JwtProperties`, `TokenType`, `JwtAuthenticationEntryPoint`/`JwtAccessDeniedHandler`(@Component), `JwtPrincipal`/`JwtPrincipalFactory`(앱별 principal 흡수용 계약). 아래 참고 |

## For AI Agents

### Working In This Directory
- **`RefreshTokenRedisRepository`/`BlacklistRedisRepository`는 키 접두사를 생성자 파라미터로 받는 순수 POJO**다(`@Repository` 자기 등록 아님). web-api는 `"rt:"`/`"bl:"`, admin-api는 `"admin:rt:"`/`"admin:bl:"`를 각자 `@Configuration`에서 빈 등록 시 주입한다 — reference: `web-api/config/jwt/RedisRepositoryConfig`, `admin-api/config/jwt/RedisRepositoryConfig`. 새 접두사가 필요한 저장소를 추가할 때도 이 패턴(접두사 주입형 POJO + 사용처 빈 등록)을 따른다.
- **소셜 임시토큰 저장소는 접두사가 고정**이므로 `@Repository` 자기 등록으로 두고 접두사 주입을 적용하지 않는다(web-api 전용, admin-api는 소셜 로그인이 없어 빈이 떠도 무해).
- 이 모듈이 domain 어댑터가 아니라 presentation 공유 유틸이기 때문에 `spring-boot-starter-web` 의존이 허용된다(`external-api`도 starter-web 의존 선례 있음).
- **이 모듈이 `domain-module`을 의존하는 유일한 이유**는 `JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`가 401/403 응답을 조립할 때 쓰는 `com.tastyhouse.domain.exception.ErrorCode` 참조뿐이다(그 밖의 도메인 타입 참조 0건). 이 모듈에 도메인 타입을 새로 끌어들이는 확장은 지양한다.
- 새 관심사를 어디에 둘지 판단하는 기준 (챕터 05 개정): **domain 포트가 있으면** `infrastructure:persistence`(JPA/조회) 또는 `external-api`(외부 연동). **domain 포트가 없는 순수 기술**이면 그 기술의 인프라 모듈(Redis는 `infrastructure:redis`). **domain 포트가 없고 여러 presentation이 공유하는 보안 관심사**일 때만 이 모듈. 특정 앱 하나만 쓰면 그 앱 모듈에 잔류.
- **Redis를 쓴다는 이유만으로 이 모듈에 두지 않는다** — 그것이 챕터 05에서 rate limiting을 내보낸 이유다. 이 모듈에 남는 기준은 "보안 관심사인가"이지 "Redis를 쓰는가"가 아니다.

### `jwt/` 공용 JWT 인증 메커니즘 (web/admin 공유)
- **`JwtTokenProvider`는 `@Component`가 아닌 파라미터형 POJO**다. principal 식별자 클레임명(`memberId`/`adminId`)과 principal 재구성 팩토리(`JwtPrincipalFactory`)를 생성자로 받아 앱별 차이를 흡수한다. 각 API는 이 클래스를 상속한 얇은 `@Component` 하위 클래스로 자기 등록한다 — reference: `web-api`/`admin-api`의 `config/jwt/JwtTokenProvider`(`super(props, "memberId"|"adminId", CustomUserDetails::new)`). web은 여기에 검증용 토큰(휴대폰/이메일/개인정보/비밀번호 재설정) 발급 메서드를 **web 전용으로만** 추가한다(admin은 미사용). `key`/`parseClaims`/`jwtProperties`는 `protected`라 하위 클래스가 검증 토큰 발급에 재사용한다.
- **`JwtAuthenticationFilter`도 POJO**다(`@Component` 아님). `JwtTokenProvider` + `BlacklistRedisRepository`(접두사 주입 빈) + `ObjectMapper`를 받아 각 API의 `config/jwt/JwtConfig`가 빈 등록한다. 블랙리스트 검사는 저장소를 직접 호출한다(과거 web이 `TokenService.isBlacklisted`를 경유하던 단순 위임을 필터 안으로 흡수).
- **`JwtAuthenticationEntryPoint`/`JwtAccessDeniedHandler`는 `@Component`**다. 두 API의 `scanBasePackages`에 `com.tastyhouse.security`가 포함되어 자동 스캔되므로 별도 빈 등록이 불필요하다. `SecurityConfig`는 타입으로 주입만 받는다.
- **`CustomUserDetails`는 각 API에 잔류**하되 `JwtPrincipal`을 구현해 `getPrincipalId()`(web=memberId, admin=adminId)를 노출한다. 공용 provider가 이 계약으로 식별자를 클레임에 싣고 재구성한다.
- **시크릿은 각 API의 `application.yml`이 소유하며 web-api와 admin-api는 반드시 서로 다른 `jwt.secret`(`JWT_SECRET_WEB` vs `JWT_SECRET_ADMIN`, ceo는 `JWT_SECRET_CEO`)을 써야 한다.** 동일 시크릿이면 한쪽 access 토큰이 다른 쪽 인증을 통과해 권한 상승이 발생한다. 이는 `JwtProperties` Javadoc에도 명시되어 있다.
- **의존성**: 이 패키지가 Spring Security·jjwt 타입에 의존하므로 `build.gradle`에 `spring-boot-starter-security`(api)와 `jjwt-api`(api)/`jjwt-impl`·`jjwt-jackson`(runtimeOnly)을 둔다.

### Testing Requirements
- 접두사 주입형 저장소는 각 API의 빈 등록(`RedisRepositoryConfig`)이 올바른 접두사를 넘기는지 통합 테스트로 확인한다(뒤바뀌면 로그인 세션이 조용히 무효화됨).
- **JWT 시크릿 분리 회귀 방지**: web에서 발급한 access 토큰을 admin API에 제시하면 401(서명 불일치)이 되어야 한다. 두 앱이 같은 시크릿을 쓰지 않는지 통합 테스트/배포 체크로 확인한다.

## Redis 위임 (챕터 05)

**Redis 연결 설정·`StringRedisTemplate` 빈·Rate Limiting은 이 모듈이 소유하지 않는다.** 챕터 05에서 `infrastructure:redis`로 이관됐다.

| 이관된 것 (`infrastructure:redis` 소유) | 잔류한 것 (이 모듈 소유) |
|---|---|
| `RedisConfig` — `StringRedisTemplate` 빈 | 토큰 저장소 6종(`RefreshToken`·`Blacklist`·소셜 임시토큰 4종) |
| `application-redis.yml` — `spring.data.redis`(host/port/password) | `jwt/` 공용 JWT 인증 메커니즘 |
| `ratelimit/` 전체 — 이후 챕터 02에서 다시 갈렸다: 카운터(`RateLimiterService` → `RedisRateLimitCounter`)만 `infrastructure:redis`에 남고, `RateLimitAspect`·`@RateLimit`·`RateLimitKeyType`·`RateLimitException`은 `api-common-module`로 올라갔다 | |

**나눈 기준은 "Redis를 쓰는가"가 아니라 "보안 관심사인가"다.** 토큰 저장소는 Redis를 쓰지만 보안 관심사라 남았고, rate limiting은 Redis를 쓰지만 도메인에 대응 개념이 없는 순수 인프라 관심사라 나갔다. Redis 자체(연결·템플릿)는 애초에 보안이 아니라 기술이므로, 그것을 보안 모듈이 들고 있으면 Redis를 쓰려는 다른 관심사가 전부 이 모듈을 의존해야 했다.

- 이 모듈은 `implementation project(':infrastructure:redis')`를 선언하고 **`data-redis` 타입을 직접 선언하지 않는다** — `infrastructure:redis`가 그것을 `api`로 노출하기 때문이다.
- **Redis key prefix는 불변이다**(`rt:`/`bl:`/`admin:rt:`/`admin:bl:`). 이관은 소유 모듈만 바꿨을 뿐 런타임 키 공간을 건드리지 않았다 — 바뀌면 배포 시점에 기존 로그인 세션이 전부 무효화된다.
- 자세한 내용은 `infrastructure/redis/AGENTS.md` 참고.

## Dependencies

### Internal
- `infrastructure:redis` (implementation) — Redis 연결·`StringRedisTemplate`(위 [Redis 위임](#redis-위임-챕터-05))
- `domain-module` (implementation) — `com.tastyhouse.domain.exception.ErrorCode` 참조(`JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`)

### External
- `spring-boot-starter-web` (implementation)
- `spring-boot-starter-data-redis` — 직접 선언이 아니라 `infrastructure:redis`가 `api`로 노출하는 것을 전이로 받는다
- `spring-boot-starter-security` (api) — 공용 JWT 필터/EntryPoint/AccessDeniedHandler/UserDetails 타입
- `jjwt-api` (api) + `jjwt-impl`·`jjwt-jackson` (runtimeOnly) — 공용 `JwtTokenProvider` 서명/파싱

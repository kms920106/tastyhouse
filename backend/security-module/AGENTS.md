<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-07-22 | Updated: 2026-09-01 -->

# security-module

## Purpose
`web-api`·`admin-api`·`ceo-api`가 공유하는 **서블릿 결합 보안/인증 라이브러리 모듈**(`java-library`). JWT 인증 필터(`OncePerRequestFilter`)·`JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler` 등 서블릿 스택에 결합된 타입만 소유한다.

**서블릿-프리 타입(`JwtTokenProvider`·Redis 기반 JWT 세션 저장소 6종)은 챕터 03에서 `security-core`로 분리됐다** — 이 모듈은 `api project(':security-core')`로 그 좌표를 재노출한다(아래 [security-core 분리](#security-core-분리-챕터-03) 참고). API 변경은 없다(JWT 토큰 포맷·Redis key prefix·인증 플로우 불변).

**Redis 연결·템플릿 빈과 Rate Limiting은 챕터 05에서 `infrastructure:redis`로 이관됐다** — 이 모듈은 그것도 직접 소유하지 않고 `security-core`를 통해 전이로만 접근한다(아래 [Redis 위임](#redis-위임-챕터-05) 참고).

이 관심사는 domain 계층의 포트가 아니라 presentation 모듈들의 공통 보안 관심사이므로, **DB 어댑터 전용인 `infrastructure:persistence`가 아니라 별도 공유 모듈로 분리**되어 있다(모듈 경계 근거는 루트 [AGENTS.md](../AGENTS.md#module-dependency-graph) 참고). 이 모듈은 domain 어댑터가 아니라 presentation 공유 유틸이므로 `implementation`으로 노출된다 — `TokenService` 등이 구체 클래스를 컴파일 타임에 직접 주입하는 현재 구조를 그대로 지원하기 위함이다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + `domain-module`(implementation — `JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`의 ErrorCode 참조) + **`api project(':security-core')`**(챕터 03 — 서블릿-프리 타입 재노출, jjwt는 이 좌표를 통해 전이 수신) + starter-web(implementation) + starter-security(`api`, 서블릿 결합 타입이 spring-security-web 필요). `bootJar` 비활성. **챕터 02** — `{web,admin,ceo}-api`는 이 모듈을 여전히 `implementation`으로 의존한다(`TokenService`가 `JwtTokenProvider`를 구체 타입으로 직접 주입하는 컴파일 타임 결합이 있어 `runtimeOnly`로 내릴 수 없다) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/security/` | `SecurityModuleAutoConfiguration`(챕터 02 — `SecurityModuleConfig`를 리네임 + `@AutoConfiguration(proxyBeanMethods = false)`) — `@ComponentScan` 진입점(`com.tastyhouse.security` 전체를 스캔 — 패키지 불변 덕에 `security-core`로 이동한 `@Repository` 토큰 저장소 빈도 계속 잡힌다). `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`에 FQCN 1줄로 자기 등록하며, `{web,admin,ceo}-api`는 더 이상 `@Import`하지 않는다 |
| `src/main/java/com/tastyhouse/security/jwt/` | 서블릿 결합 JWT 인증 메커니즘만 잔류 — `JwtAuthenticationFilter`(POJO, `OncePerRequestFilter` 상속), `JwtAuthenticationEntryPoint`/`JwtAccessDeniedHandler`(@Component). `JwtTokenProvider`·`JwtProperties`·`TokenType`·`JwtPrincipal`·`JwtPrincipalFactory`는 `security-core`로 이동(패키지는 동일하게 `com.tastyhouse.security.jwt`) |

## For AI Agents

### Working In This Directory
- 이 모듈이 domain 어댑터가 아니라 presentation 공유 유틸이기 때문에 `spring-boot-starter-web` 의존이 허용된다(외부 연동 모듈에는 이런 선례가 없다 — 코어 `infrastructure:external`이 한때 `MultipartFile` 1종 때문에 `spring-web` 단일 좌표를 썼으나, `FileStorageStrategy`가 `byte[]`를 받도록 바뀌며 그 의존이 사라졌다).
- **이 모듈이 `domain-module`을 의존하는 유일한 이유**는 `JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`가 401/403 응답을 조립할 때 쓰는 `com.tastyhouse.domain.exception.ErrorCode` 참조뿐이다(그 밖의 도메인 타입 참조 0건). 이 모듈에 도메인 타입을 새로 끌어들이는 확장은 지양한다.
- 새 관심사를 어디에 둘지 판단하는 기준 (챕터 05 개정, 챕터 03으로 세분화): **domain 포트가 있으면** `infrastructure:persistence`(JPA/조회) 또는 외부 연동 모듈 — 코어 계약(`WebClientConfig`·`ExternalApiException`·파일 저장 SPI)은 `infrastructure:external`, 실제 어댑터는 기술별로 `infrastructure:{firebase,aws,oauth,payment,messaging,crawling}`. **domain 포트가 없는 순수 기술**이면 그 기술의 인프라 모듈(Redis는 `infrastructure:redis`). **domain 포트가 없고 여러 presentation이 공유하는 보안 관심사**면, **서블릿 결합 여부로 다시 갈린다** — 서블릿-프리(토큰 발급/검증·저장소)면 `security-core`, 서블릿 결합(필터·EntryPoint·AccessDeniedHandler)이면 이 모듈. 특정 앱 하나만 쓰면 그 앱 모듈에 잔류.
- **Redis를 쓴다는 이유만으로 이 모듈에 두지 않는다** — 그것이 챕터 05에서 rate limiting을 내보낸 이유다. 이 모듈에 남는 기준은 "보안 관심사인가"이지 "Redis를 쓰는가"가 아니다.
- **`OncePerRequestFilter`·`jakarta.servlet`·`AuthenticationEntryPoint`/`AccessDeniedHandler` 등 서블릿 결합 타입을 새로 추가할 때만 이 모듈에 둔다.** 서블릿-프리 보안 로직(토큰 서명/파싱, 새 Redis 토큰 저장소 등)은 `security-core`로 보낸다 — application 4모듈의 컴파일 클래스패스를 서블릿 스택으로 오염시키지 않기 위해서다(아래 [security-core 분리](#security-core-분리-챕터-03) 참고).
- **`SecurityModuleAutoConfiguration`은 `@ConditionalOnWebApplication(type = SERVLET)`을 갖는다 (챕터 02)** — batch-module은 jar 자체가 없어 무관하지만, 혹시 이 모듈이 non-servlet 컨텍스트의 클래스패스에 실리는 경우에도 서블릿 필터·EntryPoint 빈이 조용히 발화하지 않도록 조건을 명시했다.

### `jwt/` JWT 인증 메커니즘 (web/admin/ceo 공유, 서블릿 결합 부분만 이 모듈)
- **`JwtTokenProvider`는 `security-core`가 소유**하는 `@Component`가 아닌 파라미터형 POJO다. principal 식별자 클레임명(`memberId`/`adminId`)과 principal 재구성 팩토리(`JwtPrincipalFactory`)를 생성자로 받아 앱별 차이를 흡수한다. 각 API는 이 클래스를 상속한 얇은 `@Component` 하위 클래스로 자기 등록한다 — reference: `web-api`/`admin-api`의 `config/jwt/JwtTokenProvider`(`super(props, "memberId"|"adminId", CustomUserDetails::new)`). web은 여기에 검증용 토큰(휴대폰/이메일/개인정보/비밀번호 재설정) 발급 메서드를 **web 전용으로만** 추가한다(admin은 미사용). `key`/`parseClaims`/`jwtProperties`는 `protected`라 하위 클래스가 검증 토큰 발급에 재사용한다.
- **`JwtAuthenticationFilter`는 이 모듈 소유 POJO**다(`@Component` 아님, 서블릿 `OncePerRequestFilter` 상속이라 이 모듈에 잔류). `JwtTokenProvider`(security-core) + `BlacklistRedisRepository`(security-core, 접두사 주입 빈) + `ObjectMapper`를 받아 각 API의 `config/jwt/JwtConfig`가 빈 등록한다. 블랙리스트 검사는 저장소를 직접 호출한다(과거 web이 `TokenService.isBlacklisted`를 경유하던 단순 위임을 필터 안으로 흡수).
- **`JwtAuthenticationEntryPoint`/`JwtAccessDeniedHandler`는 이 모듈 소유 `@Component`**다. 세 API의 `scanBasePackages`에 `com.tastyhouse.security`가 포함되어 자동 스캔되므로 별도 빈 등록이 불필요하다. `SecurityConfig`는 타입으로 주입만 받는다.
- **`CustomUserDetails`는 각 API에 잔류**하되 `JwtPrincipal`(security-core 소유 계약)을 구현해 `getPrincipalId()`(web=memberId, admin=adminId)를 노출한다. 공용 provider가 이 계약으로 식별자를 클레임에 싣고 재구성한다.
- **시크릿은 각 API의 `application.yml`이 소유하며 web-api와 admin-api는 반드시 서로 다른 `jwt.secret`(`JWT_SECRET_WEB` vs `JWT_SECRET_ADMIN`, ceo는 `JWT_SECRET_CEO`)을 써야 한다.** 동일 시크릿이면 한쪽 access 토큰이 다른 쪽 인증을 통과해 권한 상승이 발생한다. 이는 `JwtProperties` Javadoc에도 명시되어 있다.
- **의존성**: 이 모듈의 서블릿 결합 타입이 Spring Security web·jjwt(전이) 타입에 의존하므로 `build.gradle`에 `spring-boot-starter-security`(api)를 둔다. jjwt 3줄(`jjwt-api`/`jjwt-impl`/`jjwt-jackson`)은 챕터 03에서 `security-core`로 이관되어 이 모듈에서 제거됐고, `api project(':security-core')`를 통해 전이로 수신한다.

### Testing Requirements
- 접두사 주입형 저장소는 각 API의 빈 등록(`RedisRepositoryConfig`)이 올바른 접두사를 넘기는지 통합 테스트로 확인한다(뒤바뀌면 로그인 세션이 조용히 무효화됨).
- **JWT 시크릿 분리 회귀 방지**: web에서 발급한 access 토큰을 admin API에 제시하면 401(서명 불일치)이 되어야 한다. 두 앱이 같은 시크릿을 쓰지 않는지 통합 테스트/배포 체크로 확인한다.

## security-core 분리 (챕터 03)

**`application`이 이 모듈(`security-module`)을 의존하면 서블릿 필터·EntryPoint·AccessDeniedHandler까지 컴파일 클래스패스에 딸려 들어와 application 계층이 서블릿 스택으로 오염됐다.** ArchUnit `applicationMustBeServletFree`는 소스 import만 검사하므로 이 클래스패스 오염을 막지 못했다 — 빌드 그래프로 강제해야 했다.

서블릿-프리 타입(`JwtTokenProvider`·`JwtPrincipal`·`JwtPrincipalFactory`·`JwtProperties`·`TokenType`, Redis 토큰 저장소 6종)을 신설 모듈 `security-core`로 옮기고, `application`은 `security-module` 대신 `security-core`만 의존하도록 교체했다(batch 유스케이스는 원래 security 의존이 없어 대상 아님). `{web,admin,ceo}-api`는 기존대로 이 모듈(`security-module`)을 의존하며 `security-core`를 전이로 수신한다.

자바 패키지(`com.tastyhouse.security..`)는 두 모듈 모두 그대로 쓴다(split package — 모듈 재편 선례와 동일). 상세는 `security-core/AGENTS.md`와 루트 [CLAUDE.md 모듈 지도](../CLAUDE.md#모듈-지도-모듈-재편-완료--application-모듈-통합--external-분리) 참고.

## Redis 위임 (챕터 05)

**Redis 연결 설정·`StringRedisTemplate` 빈·Rate Limiting은 이 모듈이 소유하지 않는다.** 챕터 05에서 `infrastructure:redis`로 이관됐다.

| 이관된 것 (`infrastructure:redis` 소유) | 잔류한 것 (`security-core`/`security-module`이 나눠 소유, 챕터 03) |
|---|---|
| `RedisConfig` — `StringRedisTemplate` 빈 | 토큰 저장소 6종(`RefreshToken`·`Blacklist`·소셜 임시토큰 4종) — **`security-core` 소유** |
| `application-redis.yml` — `spring.data.redis`(host/port/password) | `jwt/` 인증 메커니즘 — 서블릿-프리 부분은 `security-core`, 서블릿 결합 부분(필터·EntryPoint·AccessDeniedHandler)은 이 모듈 |
| `ratelimit/` 전체 — 이후 챕터 02에서 다시 갈렸다: 카운터(`RateLimiterService` → `RedisRateLimitCounter`)만 `infrastructure:redis`에 남고, `RateLimitAspect`·`@RateLimit`·`RateLimitKeyType`·`RateLimitException`은 `api-common-module`로 올라갔다 | |

**나눈 기준은 "Redis를 쓰는가"가 아니라 "보안 관심사인가"다.** 토큰 저장소는 Redis를 쓰지만 보안 관심사라 남았고(챕터 03으로 그중 서블릿-프리 부분만 `security-core`로 재배치), rate limiting은 Redis를 쓰지만 도메인에 대응 개념이 없는 순수 인프라 관심사라 나갔다. Redis 자체(연결·템플릿)는 애초에 보안이 아니라 기술이므로, 그것을 보안 모듈이 들고 있으면 Redis를 쓰려는 다른 관심사가 전부 이 모듈을 의존해야 했다.

- 이 모듈은 이제 `infrastructure:redis`를 직접 의존하지 않는다 — Redis 토큰 저장소는 `security-core`로 이동했고, 이 모듈은 `security-core`를 통해서만 전이로 접근한다(챕터 03).
- **Redis key prefix는 불변이다**(`rt:`/`bl:`/`admin:rt:`/`admin:bl:`). 이관은 소유 모듈만 바꿨을 뿐 런타임 키 공간을 건드리지 않았다 — 바뀌면 배포 시점에 기존 로그인 세션이 전부 무효화된다.
- 자세한 내용은 `infrastructure/redis/AGENTS.md`·`security-core/AGENTS.md` 참고.

## Dependencies

### Internal
- `domain-module` (implementation) — `com.tastyhouse.domain.exception.ErrorCode` 참조(`JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`)
- `security-core` (api) — (챕터 03) 서블릿-프리 보안 코어(`JwtTokenProvider`·Redis 토큰 저장소 6종·`JwtProperties` 등) 재노출. 이 모듈에 남은 서블릿 결합 타입이 그 타입들을 쓰고, api 3모듈도 이 좌표를 통해 전이로 수신

### External
- `spring-boot-starter-web` (implementation)
- `spring-boot-starter-security` (api) — 서블릿 결합 JWT 필터/EntryPoint/AccessDeniedHandler/UserDetails 타입. `spring-security-core`는 `security-core`가 이미 `api`로 노출
- `jjwt-api`/`jjwt-impl`/`jjwt-jackson` — 직접 선언 없음(챕터 03으로 `security-core`로 이관), `api project(':security-core')`를 통해 전이로 수신

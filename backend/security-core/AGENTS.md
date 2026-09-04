<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-09-01 -->

# security-core

## Purpose
`application`·`security-module`이 공유하는 **서블릿-프리 보안 코어 라이브러리 모듈**(`java-library`, 챕터 03 신설). `JwtTokenProvider`(서명/파싱)와 Redis 기반 JWT 세션 저장소 6종(RefreshToken/Blacklist/소셜 임시토큰 4종)을 캡슐화한다.

**신설 배경**: 기존에는 이 타입들이 `security-module`에 서블릿 결합 타입(JWT 인증 필터 `OncePerRequestFilter` 상속, `JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler`)과 함께 있었는데, `application`이 `JwtTokenProvider`·토큰 저장소를 쓰려고 `security-module`을 의존하면 `starter-web`·서블릿 필터까지 컴파일 클래스패스에 딸려 들어와 **application 계층의 클래스패스가 서블릿 스택으로 오염**됐다. ArchUnit `applicationMustBeServletFree`는 소스의 import만 검사하므로 이 클래스패스 오염을 막지 못했다 — 그래서 서블릿-프리 타입만 이 모듈로 분리해 **빌드 그래프로 강제**한다. 자세한 배경은 `security-module/AGENTS.md`의 [security-core 분리](../security-module/AGENTS.md#security-core-분리-챕터-03)와 루트 [CLAUDE.md 모듈 지도](../CLAUDE.md#모듈-지도-모듈-재편-완료--application-모듈-통합-챕터-01) 참고.

**API 변경 없음** — JWT 토큰 포맷·Redis key prefix·인증 플로우는 분리 전과 완전히 동일하다. 모듈 소속만 바뀌었다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + `domain-module`(implementation — `ErrorCode`로 토큰 검증 실패 표현) + `infrastructure:redis`(implementation — 토큰 저장소 6종이 `StringRedisTemplate` 사용, `data-redis` 타입은 직접 선언하지 않고 `infrastructure:redis`가 `api`로 노출하는 것을 받는다) + `spring-security-core`(api — `JwtTokenProvider`가 `Authentication`·`UserDetails`·`GrantedAuthority`를 시그니처에 노출) + JJWT(`jjwt-api` api, `jjwt-impl`/`jjwt-jackson` runtimeOnly). **서블릿 스택(`starter-web`·`jakarta.servlet`) 의존이 없다** — 그것이 이 모듈 존재 이유다. `bootJar` 비활성 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/security/jwt/` | `JwtTokenProvider`(파라미터형 POJO, 서명/파싱), `JwtProperties`, `TokenType`, `JwtPrincipal`/`JwtPrincipalFactory`(앱별 principal 재구성 계약) |
| `src/main/java/com/tastyhouse/security/token/` | `RefreshTokenRedisRepository`·`BlacklistRedisRepository`(접두사 생성자 주입형) + 소셜 임시토큰 4종(`Kakao`/`Naver`/`Apple`/`Facebook`TempTokenRedisRepository, 접두사 고정) |

자바 패키지는 `com.tastyhouse.security..`로 **`security-module`과 동일**하다(split package — 모듈 재편 선례와 같은 방식으로, 이동 대상만 패키지를 유지한 채 모듈을 옮겼다). `SecurityModuleConfig`(`security-module` 소유)의 `@ComponentScan("com.tastyhouse.security")`가 패키지 불변 덕분에 이 모듈로 이동한 `@Repository` 빈들도 그대로 스캔한다.

## For AI Agents

### Working In This Directory
- **이 모듈에 서블릿 타입을 끌어들이지 않는다.** `jakarta.servlet.*`·`OncePerRequestFilter`·`AuthenticationEntryPoint`·`AccessDeniedHandler` 등 서블릿 결합 타입이 필요한 기능은 이 모듈이 아니라 `security-module`에 둔다 — 그것이 이 모듈이 `application`의 안전한 의존 대상으로 남는 유일한 이유다.
- **`RefreshTokenRedisRepository`/`BlacklistRedisRepository`는 키 접두사를 생성자 파라미터로 받는 순수 POJO**다(`@Repository` 자기 등록 아님). web-api는 `"rt:"`/`"bl:"`, admin-api는 `"admin:rt:"`/`"admin:bl:"`를 각자 `@Configuration`에서 빈 등록 시 주입한다 — reference: `web-api/config/jwt/RedisRepositoryConfig`, `admin-api/config/jwt/RedisRepositoryConfig`. 새 접두사가 필요한 저장소를 추가할 때도 이 패턴(접두사 주입형 POJO + 사용처 빈 등록)을 따른다.
- **소셜 임시토큰 저장소는 접두사가 고정**이므로 `@Repository` 자기 등록으로 두고 접두사 주입을 적용하지 않는다(web-api 전용, admin-api는 소셜 로그인이 없어 빈이 떠도 무해).
- **`JwtTokenProvider`는 `@Component`가 아닌 파라미터형 POJO**다. principal 식별자 클레임명(`memberId`/`adminId`)과 principal 재구성 팩토리(`JwtPrincipalFactory`)를 생성자로 받아 앱별 차이를 흡수한다. 각 API는 이 클래스를 상속한 얇은 `@Component` 하위 클래스로 자기 등록한다 — reference: `web-api`/`admin-api`의 `config/jwt/JwtTokenProvider`(`super(props, "memberId"|"adminId", CustomUserDetails::new)`). web은 검증용 토큰(휴대폰/이메일/개인정보/비밀번호 재설정) 발급 메서드를 **web 전용으로만** 추가한다(admin은 미사용). `key`/`parseClaims`/`jwtProperties`는 `protected`라 하위 클래스가 재사용한다.
- **`JwtPrincipal`/`JwtPrincipalFactory`는 앱별 principal 차이를 흡수하는 계약**이다. 각 API의 `CustomUserDetails`가 `JwtPrincipal`을 구현해 `getPrincipalId()`(web=memberId, admin=adminId)를 노출하고, `JwtPrincipalFactory`는 클레임에서 그 principal을 재구성한다.
- **시크릿은 각 API의 `application.yml`이 소유**하며 web-api와 admin-api는 반드시 서로 다른 `jwt.secret`(`JWT_SECRET_WEB` vs `JWT_SECRET_ADMIN`, ceo는 `JWT_SECRET_CEO`)을 써야 한다. 동일 시크릿이면 한쪽 access 토큰이 다른 쪽 인증을 통과해 권한 상승이 발생한다. 이는 `JwtProperties` Javadoc에도 명시되어 있다.
- **이 모듈이 `domain-module`을 의존하는 이유**는 토큰 검증 실패를 `com.tastyhouse.domain.exception.ErrorCode`로 표현하기 위해서다(그 밖의 도메인 타입 참조 없음). 도메인 타입을 새로 끌어들이는 확장은 지양한다.
- **Redis key prefix는 불변이다**(`rt:`/`bl:`/`admin:rt:`/`admin:bl:` 등). 챕터 03 이관은 소유 모듈만 바꿨을 뿐 런타임 키 공간을 건드리지 않았다 — 바뀌면 배포 시점에 기존 로그인 세션이 전부 무효화된다.

### Testing Requirements
- 접두사 주입형 저장소는 각 API의 빈 등록(`RedisRepositoryConfig`)이 올바른 접두사를 넘기는지 통합 테스트로 확인한다(뒤바뀌면 로그인 세션이 조용히 무효화됨).
- **JWT 시크릿 분리 회귀 방지**: web에서 발급한 access 토큰을 admin API에 제시하면 401(서명 불일치)이 되어야 한다. 두 앱이 같은 시크릿을 쓰지 않는지 통합 테스트/배포 체크로 확인한다.
- 기동 스모크(로그인 → 인증 필요 엔드포인트 1회 호출)로 필터+프로바이더+저장소 전 경로가 여전히 배선되는지 확인한다 — `contextLoads`류 테스트는 빈 배선을 증명하지 못한다(선례).

## Dependencies

### Internal
- `domain-module` (implementation) — `com.tastyhouse.domain.exception.ErrorCode` 참조
- `infrastructure:redis` (implementation) — Redis 연결·`StringRedisTemplate`(토큰 저장소 6종이 사용)

### External
- `spring-security-core` (api) — `JwtTokenProvider`가 `Authentication`·`UserDetails`·`GrantedAuthority`를 시그니처에 노출
- `jjwt-api` (api) + `jjwt-impl`·`jjwt-jackson` (runtimeOnly) — `JwtTokenProvider` 서명/파싱
- `spring-boot-starter-data-redis` — 직접 선언이 아니라 `infrastructure:redis`가 `api`로 노출하는 것을 전이로 받는다

### Consumers (챕터 03)
- `application` — `implementation project(':security-core')`로 직접 의존(서블릿 스택 없이 auth/token 서비스가 사용). batch 유스케이스는 원래 security를 쓰지 않으며, 모듈 통합 후 클래스패스에 보이더라도 참조하지 않는다
- `security-module` — `api project(':security-core')`로 재노출(잔류한 서블릿 결합 타입이 이 모듈의 `JwtTokenProvider`·토큰 저장소를 쓴다)
- `{web,admin,ceo}-api` — `security-module`을 통해 전이로 수신(기존 좌표 그대로, 직접 의존 선언 없음)

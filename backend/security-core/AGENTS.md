<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-09-01 -->

# security-core

## Purpose
`application`·`security-module`이 공유하는 **서블릿-프리 보안 코어 라이브러리 모듈**(`java-library`). `JwtTokenProvider`(서명/파싱)와 JWT 세션 토큰 저장소 **포트** 3종(RefreshToken/Blacklist/소셜 임시토큰)을 캡슐화하고, 구현은 `infrastructure:redis`의 `token` 패키지가 갖는다(어댑터 → 계약). **rate limit 카운터 계약 `RateLimitCounterPort`도 이 모듈이 소유한다**(근거는 아래 [rate limit 카운터 계약을 여기 둔 이유](#rate-limit-카운터-계약을-여기-둔-이유)). 구현은 역시 `infrastructure:redis`의 `RedisRateLimitCounter`다.

**신설 배경**: 기존에는 이 타입들이 `security-module`에 서블릿 결합 타입(JWT 인증 필터 `OncePerRequestFilter` 상속, `JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler`)과 함께 있었는데, `application`이 `JwtTokenProvider`·토큰 저장소를 쓰려고 `security-module`을 의존하면 `starter-web`·서블릿 필터까지 컴파일 클래스패스에 딸려 들어와 **application 계층의 클래스패스가 서블릿 스택으로 오염**됐다. ArchUnit `applicationMustBeServletFree`는 소스의 import만 검사하므로 이 클래스패스 오염을 막지 못했다 — 그래서 서블릿-프리 타입만 이 모듈로 분리해 **빌드 그래프로 강제**한다. 자세한 배경은 `security-module/AGENTS.md`의 [security-core 분리](../security-module/AGENTS.md#security-core-분리-챕터-03)와 루트 [CLAUDE.md 모듈 지도](../CLAUDE.md#모듈-지도-모듈-재편-완료--application-모듈-통합--external-분리--앱-모듈-재분리) 참고.

**API 변경 없음** — JWT 토큰 포맷·Redis key prefix·인증 플로우는 분리 전과 완전히 동일하다. 모듈 소속만 바뀌었다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + `spring-security-core`(api — `JwtTokenProvider`가 `Authentication`·`UserDetails`·`GrantedAuthority`를 시그니처에 노출) + JJWT(`jjwt-api` api, `jjwt-impl`/`jjwt-jackson` runtimeOnly). **내부 모듈 의존이 없다**(`domain`·`infrastructure:redis` 모두 의존하지 않는다 — 토큰 저장소는 포트라 Redis 타입을 보유하지 않는다). **서블릿 스택(`starter-web`·`jakarta.servlet`) 의존이 없다** — 그것이 이 모듈 존재 이유다. `bootJar` 비활성 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/security/jwt/` | `JwtTokenProvider`(파라미터형 POJO, 서명/파싱), `JwtProperties`, `TokenType`, `JwtPrincipal`/`JwtPrincipalFactory`(앱별 principal 재구성 계약) |
| `src/main/java/com/tastyhouse/security/token/` | **인터페이스 3종** — `RefreshTokenRepository`·`BlacklistRepository`·`SocialTempTokenRepository` + provider enum `SocialTempTokenProvider`. 구현·TTL 정책은 전부 `infrastructure:redis`의 `com.tastyhouse.infrastructure.redis.token`이 소유한다(소셜 임시토큰 키 접두사만 예외 — 아래 "소셜 임시토큰 저장소" 절) |
| `src/main/java/com/tastyhouse/security/ratelimit/` | **`RateLimitCounterPort` 1종** — `boolean isLimitExceeded(String key, int limit, Duration duration)`. 구현은 `infrastructure:redis`의 `com.tastyhouse.infrastructure.redis.ratelimit.RedisRateLimitCounter`, 소비자는 `api-common-module`의 `RateLimitAspect`·`ApiCommonRateLimitConfig` |

자바 패키지는 `com.tastyhouse.security..`로 **`security-module`과 동일**하다(split package — 두 모듈이 같은 패키지 트리를 나눠 쓴다). 앱 부트스트랩의 중첩 `ModuleScanConfig`가 `"com.tastyhouse.security"`를 스캔하므로 이 모듈의 빈도 그 스캔에 잡힌다. `security-module`의 `SecurityModuleConfig`는 스캔을 갖지 않는다.

## For AI Agents

### Working In This Directory
- **이 모듈에 서블릿 타입을 끌어들이지 않는다.** `jakarta.servlet.*`·`OncePerRequestFilter`·`AuthenticationEntryPoint`·`AccessDeniedHandler` 등 서블릿 결합 타입이 필요한 기능은 이 모듈이 아니라 `security-module`에 둔다 — 그것이 이 모듈이 `application`의 안전한 의존 대상으로 남는 유일한 이유다.
- **토큰 저장소 3종은 이 모듈이 계약만 갖는다.** 구현 기술을 이름에 넣지 않으므로 `Redis` 중간어를 붙이지 않고(`RefreshTokenRepository`), 구현체는 `infrastructure:redis`가 `Redis` 접두어로 갖는다(`RedisRefreshTokenRepository`). **여기에 `StringRedisTemplate`을 끌어들이지 않는다** — 그러면 이 모듈이 redis를 의존하게 되어 `application`·batch runtimeClasspath에 redis·api-common·springdoc 전이가 실린다.
- **앱별 키 접두사는 프로퍼티다**: `security.token-store.key-prefix`(web 기본 `""`, admin `"admin:"`, ceo `"ceo:"`). 앱은 Redis 타입을 직접 참조하지 않는다. 새 저장소가 접두사를 필요로 하면 `RedisTokenStoreProperties`를 주입받는 어댑터를 `infrastructure:redis`에 추가한다.
- **소셜 임시토큰 저장소는 접두사가 고정**(`kakao_temp:` 등)이라 프로퍼티를 받지 않는다. 어댑터가 `@Component`로 자기 등록한다(web-api 전용, 다른 앱은 빈이 떠도 무해).
- **`JwtTokenProvider`는 `@Component`가 아닌 파라미터형 POJO**다. principal 식별자 클레임명(`memberId`/`adminId`)과 principal 재구성 팩토리(`JwtPrincipalFactory`)를 생성자로 받아 앱별 차이를 흡수한다. 각 API는 이 클래스를 상속한 얇은 `@Component` 하위 클래스로 자기 등록한다 — reference: `web-api`/`admin-api`의 `config/jwt/JwtTokenProvider`(`super(props, "memberId"|"adminId", CustomUserDetails::new)`). web은 검증용 토큰(휴대폰/이메일/개인정보/비밀번호 재설정) 발급 메서드를 **web 전용으로만** 추가한다(admin은 미사용). `key`/`parseClaims`/`jwtProperties`는 `protected`라 하위 클래스가 재사용한다.
- **`JwtPrincipal`/`JwtPrincipalFactory`는 앱별 principal 차이를 흡수하는 계약**이다. 각 API의 `CustomUserDetails`가 `JwtPrincipal`을 구현해 `getPrincipalId()`(web=memberId, admin=adminId)를 노출하고, `JwtPrincipalFactory`는 클레임에서 그 principal을 재구성한다.
- **시크릿은 각 API의 `application.yml`이 소유**하며 web-api와 admin-api는 반드시 서로 다른 `jwt.secret`(`JWT_SECRET_WEB` vs `JWT_SECRET_ADMIN`, ceo는 `JWT_SECRET_CEO`)을 써야 한다. 동일 시크릿이면 한쪽 access 토큰이 다른 쪽 인증을 통과해 권한 상승이 발생한다. 이는 `JwtProperties` Javadoc에도 명시되어 있다.
- **이 모듈은 `domain`을 의존하지 않는다** — 소스에 `com.tastyhouse.domain..` 참조가 0건이다. `application-core`로 대신 의존할 수도 없다 — `application → security-core`가 이미 있어 **순환**이 된다. 그래서 이 모듈은 어떤 에러코드 enum도 참조하지 않는다(401/403 응답 조립은 `security-module`이 `api-common-module`의 `ApiErrorCode`로 한다). 도메인 타입을 새로 끌어들이는 확장은 지양한다([ADR-0025](../../docs/adr/0025-presentation-domain-cut.md)).
- **Redis key prefix는 불변이다**(`rt:`/`bl:`/`admin:rt:`/`admin:bl:` 등). 계약·구현의 소유 모듈을 옮기더라도 런타임 키 공간은 **바이트 단위로 건드리지 않는다** — 바뀌면 배포 시점에 기존 로그인 세션이 전부 무효화된다. 접두사 조합은 예외가 아니라 **조용한 무효화**로 드러나므로, `infrastructure:redis`의 고정값 단위 테스트가 그 유일한 자동 방어선이다.

### Testing Requirements
- 접두사 조합 검증은 이 모듈이 아니라 **구현을 가진 `infrastructure:redis`**의 단위 테스트가 담당한다(`RedisRefreshTokenRepositoryTest`·`RedisBlacklistRepositoryTest` — `""`/`"admin:"`/`"ceo:"` 고정값 단정). 이 모듈에는 인터페이스만 있어 검증할 동작이 없다.
- **JWT 시크릿 분리 회귀 방지**: web에서 발급한 access 토큰을 admin API에 제시하면 401(서명 불일치)이 되어야 한다. 두 앱이 같은 시크릿을 쓰지 않는지 통합 테스트/배포 체크로 확인한다.
- 기동 스모크(로그인 → 인증 필요 엔드포인트 1회 호출)로 필터+프로바이더+저장소 전 경로가 여전히 배선되는지 확인한다 — `contextLoads`류 테스트는 빈 배선을 증명하지 못한다(선례).

## Dependencies

### Internal
- **없음.** `domain`은 사용처가 0건이라 의존하지 않고, `application-core`는 순환(`application → security-core`)이라 의존할 수 없다. `infrastructure:redis`는 반대로 이 모듈을 의존한다(어댑터 → 계약).

### External
- `spring-security-core` (api) — `JwtTokenProvider`가 `Authentication`·`UserDetails`·`GrantedAuthority`를 시그니처에 노출
- `jjwt-api` (api) + `jjwt-impl`·`jjwt-jackson` (runtimeOnly) — `JwtTokenProvider` 서명/파싱

**`spring-boot-starter-data-redis`는 이 모듈의 클래스패스에 없다.** `./gradlew :security-core:dependencies --configuration compileClasspath | grep -c redis`가 0이어야 한다.

### Consumers
- `application-core`·`{web,admin,ceo,batch}-application` — `implementation project(':security-core')`로 직접 의존(서블릿 스택 없이 auth/token 서비스가 사용)
- `security-module` — `api project(':security-core')`로 재노출(잔류한 서블릿 결합 타입이 이 모듈의 `JwtTokenProvider`·토큰 저장소 포트를 쓴다. `JwtAuthenticationFilter`가 `BlacklistRepository`를 받는다)
- `infrastructure:redis` — `implementation project(':security-core')`. 토큰 저장소 포트 3종과 `RateLimitCounterPort`의 **구현**을 갖는다. redis가 보는 내부 모듈은 이 모듈뿐이다
- `api-common-module` — `implementation project(':security-core')`. `RateLimitAspect`가 카운터 포트를 주입받는다. `api`로 노출하지 않는다 — web·admin·ceo는 `security-module`의 `api`로 이미 이 모듈을 받는다. 이 간선으로 api-common의 compileClasspath에 `jjwt-api`가 전이로 실리는데 허용 범위다
- `{web,admin,ceo}-api` — `security-module`을 통해 전이로 수신(기존 좌표 그대로, 직접 의존 선언 없음)

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### `JwtProperties.secret` — 앱 간 동일 시크릿 금지

**대상**: `backend/security-core/src/main/java/com/tastyhouse/security/jwt/JwtProperties.java`
→ record 선언 / `secret`

값은 각 API 모듈의 `application.yml`이 소유하며, web-api와 admin-api는 반드시 서로 다른 `jwt.secret`
(`JWT_SECRET_WEB` vs `JWT_SECRET_ADMIN`)을 써야 한다. **동일 시크릿을 쓰면 한쪽 토큰이 다른 쪽 인증을
통과하는 권한 상승이 발생한다.** (같은 규칙이 위 [Working In This Directory](#working-in-this-directory)
에도 있다 — 여기서는 원 주석의 앵커를 보존한다.)

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### rate limit 카운터 계약을 여기 둔 이유

**대상**: `backend/security-core/src/main/java/com/tastyhouse/security/ratelimit/RateLimitCounterPort.java`
→ `isLimitExceeded(String, int, Duration)`

원래 이 포트는 `api-common-module`(`com.tastyhouse.apicommon.ratelimit`)에 있었고, 그 결과 아웃바운드
어댑터인 `infrastructure:redis`가 계약 하나 때문에 **표현 모듈**(`starter-web`을 노출하는 api-common)을
컴파일 의존하는 수평 간선이 생겼다. 이 모듈로 옮긴 근거는 셋이다.

- **보안 관심사다** — 현재 `@RateLimit`의 실사용처는 로그인 브루트포스 방어(web·admin·ceo `login`)이고,
  카운터는 "시도 횟수 제한"이라는 인증 방어 수단이다. JWT·토큰 저장소와 같은 모듈에 있는 것이 자연스럽다.
- **서블릿-프리다** — 시그니처가 `String`·`int`·`java.time.Duration`뿐이라 이 모듈의 존재 이유(서블릿
  타입 금지)를 해치지 않는다. 키 조립(IP·요청 필드 해석)은 HTTP 관심사라 여전히 api-common의
  `RateLimitAspect`에 남는다 — 옮긴 것은 계약뿐이다.
- **토큰 저장소 포트 3종과 같은 구조다** — "계약은 core, 구현은 `infrastructure:redis`". redis는 이미
  토큰 저장소 구현 때문에 이 모듈을 의존하므로 **새 간선 없이** redis → api-common 간선만 사라진다.

**기각한 대안**: (1) 포트 하나만 담는 별도 초소형 모듈 — 모듈 수만 늘고 소비자·구현자가 이미 이 모듈을
보거나 볼 수 있어 실익이 없다. (2) `domain` — rate limit은 비즈니스 규칙이 아니라 요청 방어 기술이며,
redis가 `domain`을 모르는 순수 기술 모듈이라는 성질(`infrastructure/redis/AGENTS.md`)을 깨게 된다.

동작 변경은 없다 — Redis 키·Lua 스크립트·`ApiCommonRateLimitAutoConfiguration`(현 `ApiCommonRateLimitConfig` — imports 제거로 리네임, 빈 존재 조건 삭제)의 배선 조건이 그대로였다.

### `JwtPrincipal` / `JwtPrincipalFactory` — 앱별 principal 차이 흡수 계약

**대상**: `backend/security-core/src/main/java/com/tastyhouse/security/jwt/JwtPrincipal.java`
→ `getPrincipalId()`
**대상**: `backend/security-core/src/main/java/com/tastyhouse/security/jwt/JwtPrincipalFactory.java`
→ `create(Long, String, Collection)`

`JwtPrincipal`은 공용 `JwtTokenProvider`가 principal 식별자(memberId/adminId 등)를 **클레임에 실을 때**
쓰는 계약이고, `JwtPrincipalFactory`는 **토큰 파싱 후 principal(UserDetails)을 재구성**하는 팩토리다.
각 API의 `CustomUserDetails`가 전자를 구현하고, 후자로는 그 생성자 참조(`CustomUserDetails::new`)를
넘긴다.

### `JwtTokenProvider`가 빈이 아닌 이유

**대상**: `backend/security-core/src/main/java/com/tastyhouse/security/jwt/JwtTokenProvider.java`
→ 클래스 선언 / 생성자

principal 식별자 클레임명과 principal 재구성 팩토리를 **생성자로 주입받아 앱별 차이를 흡수**하므로 이
클래스 자체는 빈이 아니다. 앱별 하위 클래스가 자신의 클레임명·팩토리를 주입해 `@Component`로 등록하며,
web-api는 이 클래스를 상속해 검증용 토큰(휴대폰/이메일/비밀번호 재설정 등) 발급 메서드를 추가한다.

### `TokenType` — web 전용 값을 공유해도 무해한 이유

**대상**: `backend/security-core/src/main/java/com/tastyhouse/security/jwt/TokenType.java`
→ enum 상수 목록

`ACCESS`/`REFRESH`는 양 API 공통이고, 그 외 검증용 토큰 타입(`PHONE_VERIFY`·`EMAIL_VERIFY`·
`PERSONAL_INFO_VERIFY`·`PASSWORD_RESET`)은 web-api 전용이다. admin-api는 사용하지 않지만 **상수를
공유해도 무해하므로** 앱별로 쪼개지 않는다.

### 소셜 임시토큰 저장소 — 1회용 토큰의 수명

**대상**: `backend/security-core/src/main/java/com/tastyhouse/security/token/`
→ `SocialTempTokenRepository` · `SocialTempTokenProvider`

`NEEDS_SIGN_UP` / `NEEDS_LINKING` 응답 시 발급되고, **회원가입·계정 연동 완료 시 삭제되는 1회용
토큰**이다. 계약은 `SocialTempTokenRepository`(`save(provider, tempToken, credential)`·`findCredential(provider, tempToken)`·`delete(provider, tempToken)`) 1개와 enum `SocialTempTokenProvider{KAKAO,NAVER,FACEBOOK,APPLE}`이고, 구현은 `infrastructure:redis`의 `token` 패키지에 있으며 TTL 정책은 어댑터가 소유한다([ADR-0050](../../docs/adr/0050-social-login-router.md)).

- **`SocialTempTokenProvider`는 web-application의 `SocialProvider`를 이 모듈이 볼 수 없어 둔 enum이다.** 상수명·순서가 `SocialProvider`와 같아야 한다(`backend/application-core/src/test/java/com/tastyhouse/application/architecture/EnumCodeConstantsTest.java` → `socialTempTokenProviderMatchesSocialProvider` — `SocialLoginService`가 `valueOf(provider.name())`로 변환한다).
- **Redis 키 접두사는 상수마다 명시 필드 `keyPrefix()`로 둔다**(`kakao_temp:` 등 — `name().toLowerCase()`로 만들지 않는다). 접두사 문자열이 바뀌면 배포 시점의 임시토큰이 전부 무효가 되므로 리터럴로 고정한다. 이 접두사만은 어댑터가 아니라 이 enum이 소유한다.

**Apple만 저장 대상이 다르다** — Apple은 UserInfo 엔드포인트가 없으므로 accessToken이 아닌
`id_token`을 저장한다(`SocialTempTokenRepository.save(APPLE, tempToken, idToken)` — 저장하는 값은 서비스가 넘긴 credential 그대로라 인터페이스는 provider별 값 종류를 구분하지 않는다). 이 id_token은
이미 서버에서 검증 완료된 상태이며, sub/email 재추출 시 재파싱된다.

### `RefreshTokenRepository.isInvalid`가 default 메서드인 이유

**대상**: `backend/security-core/src/main/java/com/tastyhouse/security/token/RefreshTokenRepository.java`
→ `isInvalid(String, String)`

저장된 값과의 단순 비교라 어댑터마다 다를 여지가 없어 계약 쪽에 default로 둔다. `BlacklistRepository`와
함께 앱별 키 접두사는 `security.token-store.key-prefix`가 결정한다.

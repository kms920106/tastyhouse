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
- **이 모듈은 `domain`을 의존하지 않는다** — 소스에 `com.tastyhouse.domain..` 참조가 0건이다. `application-core`로 대신 의존할 수도 없다 — `application → security-core`가 이미 있어 **순환**이 된다. 그래서 이 모듈은 어떤 에러코드 enum도 참조하지 않는다(401/403 응답 조립은 `security-module`이 `api-common-module`의 `ApiErrorCode`로 한다). 도메인 타입을 새로 끌어들이는 확장은 지양한다([결정 0025](../AGENTS.md#decision-0025)).
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
토큰**이다. 계약은 `SocialTempTokenRepository`(`save(provider, tempToken, credential)`·`findCredential(provider, tempToken)`·`delete(provider, tempToken)`) 1개와 enum `SocialTempTokenProvider{KAKAO,NAVER,FACEBOOK,APPLE}`이고, 구현은 `infrastructure:redis`의 `token` 패키지에 있으며 TTL 정책은 어댑터가 소유한다([결정 0050](../infrastructure/oauth/AGENTS.md#decision-0050)).

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

## 결정 경위

이 문서가 소유한 규칙이 **왜 지금 모양이 됐는지**를 결정 단위로 적습니다. 본문은 현재 규칙만 담고, "예전에는 어땠고 왜 바뀌었는가"는 이 절에만 둡니다. 이 절은 항상 문서의 마지막 `##` 절이며, 항목은 번호 순입니다. 작성 규칙은 루트 `CLAUDE.md`의 "규칙 문서 개정 규칙" 절에 있습니다.

<a id="decision-0011"></a>

### 0011. RateLimitCounterPort를 api-common-module로 올려 redis→api-common 방향으로 역전한다

- 상태: 대체됨(→ [결정 0023](#decision-0023))
- 결정 커밋: `8bcf3e2f3` (2026-09-01)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `backend/security-core/AGENTS.md`의 결정 0011

웹 관심사를 표현으로 올리고 Redis 카운터만 인프라에 남긴다.

| 항목 | before | after |
|---|---|---|
| `@RateLimit`·`RateLimitAspect`·`RateLimitException`·`RateLimitKeyType` | `infrastructure:redis` | `api-common-module`(`com.tastyhouse.apicommon.ratelimit`) |
| 카운터 계약 | 없음 | `RateLimitCounterPort` 신설 — `api-common-module` 소유 |
| 카운터 구현 | `RateLimiterService` | `RedisRateLimitCounter`(redis 잔류, Lua 스크립트·키 불변) |
| 의존 방향 | `api-common` → `redis` | `redis` → `api-common` |
| redis의 웹 의존 | `starter-web`·`starter-aop` | 제거 |
| IP 해석 | aspect 인라인 | `apicommon.common.ClientIpResolver`로 통합 |

<a id="decision-0019"></a>

### 0019. 토큰 저장소 6종을 security-core 포트 + infrastructure:redis 어댑터로 역전한다

- 상태: 채택
- 결정 커밋: `8cba65373` (2026-09-06)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `backend/security-core/AGENTS.md`의 결정 0019

#### 맥락

**토큰 저장소 6종**은 JWT 인증에 쓰는 Redis 저장소입니다. RefreshToken·Blacklist(로그아웃된 토큰 목록)와 소셜 로그인 임시토큰 4종(kakao·naver·apple·facebook)이 여기에 들어갑니다.

챕터 03에서 서블릿에 의존하지 않는 보안 타입을 `security-core` 모듈로 떼어냈고, 토큰 저장소 6종도 이때 함께 옮겼습니다. 다만 저장소는 `StringRedisTemplate`으로 키를 직접 조립하는 **구체 Redis 클래스**였기 때문에, `security-core`가 `implementation project(':infrastructure:redis')`를 의존해야 했습니다. 순수 계약을 담아야 할 core 모듈이 구체 인프라를 의존하는 **역방향 간선**이었습니다.

이 간선 때문에 `application → security-core → infrastructure:redis → api-common-module` 전이 사슬이 4앱 전부의 runtimeClasspath(실행 시 클래스패스)에 실렸습니다. 그래서 Redis를 쓰지 않는 batch-module에서도 `RedisModuleAutoConfiguration`이 발화했고, springdoc·starter-web이 배포 산출물에 들어갔습니다. 또 web/admin/ceo 3앱은 앱별 키 접두사(`admin:rt:` 등)를 넘기려고 `config/jwt/RedisRepositoryConfig`에서 `StringRedisTemplate`을 직접 참조했습니다. 그 결과 각 앱 `build.gradle`에 `spring-boot-starter-data-redis`를 따로 명시해야 했습니다.

#### 이전 규칙

##### backend/infrastructure/redis/AGENTS.md — security-module과의 관계

토큰 저장소 6종(`RefreshToken`·`Blacklist`·소셜 임시토큰 4종)은 `security-module`에 남긴다(이후 챕터 03에서 `security-core`로 옮겨짐). 근거는 "그것들은 보안 관심사"라는 것이었고, Redis key prefix(`rt:`/`bl:`/`admin:rt:`/`admin:bl:`)도 바꾸지 않았다. 이 모듈이 넘겨받은 것은 연결 설정·템플릿 빈·rate limiting뿐이었다. 그래서 `security-module`은 `implementation project(':infrastructure:redis')`를 선언하고 data-redis 타입은 직접 선언하지 않았다. 이 모듈이 그 타입을 `api`로 노출했기 때문이다.

##### backend/CLAUDE.md — 앱별 runtimeClasspath 감사표

`application → security-core → infrastructure:redis → api-common-module` 전이 사슬이 4앱 전부의 runtimeClasspath에 있다고 기록했다.

##### backend/CLAUDE.md — 후속 작업자가 밟기 쉬운 함정 2가지

함정 2의 사례가 이 상황이었다. 챕터 02 시점에 3앱의 `config/jwt/RedisRepositoryConfig`는 앱별 키 접두사를 넘기려고 `StringRedisTemplate`을 직접 참조했다. 그런데 `infrastructure:redis`를 `runtimeOnly`로 내리자 전이가 끊겨 컴파일이 깨졌다. 그래서 3앱 `build.gradle`에 `implementation 'org.springframework.boot:spring-boot-starter-data-redis'`를 명시해 막았다.

##### backend/CLAUDE.md — 모듈 지도 (챕터 03 — `security-core` 분리)

챕터 03 서술은 토큰 저장소 6종을 서블릿-프리 타입으로 `security-core`에 옮기면서, 그 `@Repository` 빈을 `security-module`이 스캔해 등록한다는 배선을 전제로 했다. 이 결정 이후 그 배선은 토큰 저장소에 해당하지 않는다. 어댑터는 당시 `RedisModuleAutoConfiguration`이 등록했고, 지금은 앱 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔이 등록한다.

##### backend/api-common-module/AGENTS.md — `@ConditionalOnWebApplication(SERVLET)` 두 건

batch-module은 이 모듈을 직접 의존하지 않았지만, 위 전이 사슬 때문에 클래스패스에는 갖고 있었다. batch의 `spring.main.web-application-type: none`이 이 조건을 Negative로 만드는 유일한 근거였다.

#### 결정

토큰 저장소 6종을 **포트(계약)와 어댑터(구현)로 나눕니다.** 인터페이스(포트)는 `security-core`에 남기고, Redis 구현은 `infrastructure:redis`의 `token` 패키지로 내립니다. 의존 방향은 `infrastructure:redis → security-core`(어댑터 → 계약)입니다.

| 항목 | before | after |
|---|---|---|
| 토큰 저장소 6종의 형태 | `security-core`의 구체 Redis 클래스 | `security-core`의 포트 인터페이스(`RefreshTokenRepository`·`BlacklistRepository` 등) |
| Redis 구현 위치 | `security-core` | `infrastructure:redis`의 `token` 패키지(`RedisBlacklistRepository` 등) |
| 모듈 간선 | `security-core → infrastructure:redis` | `infrastructure:redis → security-core` |
| 앱별 키 접두사 주입 | 3앱의 `config/jwt/RedisRepositoryConfig`가 `StringRedisTemplate`을 직접 받아 조립 | `security.token-store.key-prefix` 프로퍼티(`RedisRepositoryConfig` 삭제) |
| 3앱의 Redis 선언 | `implementation 'spring-boot-starter-data-redis'` + `runtimeOnly project(':infrastructure:redis')` | `runtimeOnly project(':infrastructure:redis')` 하나 |
| application·batch runtimeClasspath | redis·api-common·springdoc 포함 | 없음 |
| Redis 키 공간(`rt:`/`admin:rt:`/`ceo:rt:`/`kakao_temp:` 등) | — | 바이트 단위로 동일(동작 변경 없음) |

#### 근거

- **관심사의 귀속과 기술 구현의 귀속은 층위가 다릅니다.** "토큰 저장소는 보안 관심사"라는 옛 근거는 지금도 맞습니다. 다만 보안 관심사인 것은 *계약*(무엇을 저장하고 언제 무효화하는가)이고, `StringRedisTemplate`으로 키를 조립하는 것은 *구현*입니다. 계약을 `security-core`에, 구현을 `infrastructure:redis`에 두면 두 귀속을 모두 지키면서 역방향 간선을 없앨 수 있습니다.
- **전이 사슬이 끊깁니다.** `security-core → infrastructure:redis` 간선이 사라지면 application과 batch의 runtimeClasspath에서 redis·api-common·springdoc이 빠집니다.
- 커밋 메시지는 이 구조가 `RedisRateLimitCounter`가 외부 모듈의 `RateLimitCounterPort`를 구현하던 선례와 같은 모양이라고 밝힙니다.

#### 결과·감수한 손실

- 지금 web·admin·ceo는 `runtimeOnly project(':infrastructure:redis')`로만 Redis를 받습니다. `application`과 `batch-module`의 runtimeClasspath에는 `infrastructure:redis`·`api-common-module`·springdoc이 없습니다.
- 앱 compileClasspath(컴파일 시 클래스패스)에서도 Redis 타입이 사라졌습니다. 그래서 "함정 2" 사례와 3앱의 `spring-boot-starter-data-redis` 명시 선언도 함께 없어졌습니다.
- `security-module`은 서블릿 결합 타입만 남깁니다. `JwtAuthenticationFilter`는 `BlacklistRepository` **포트**를 받으므로 Redis 타입을 모릅니다.
- 앱별 키 접두사 프로퍼티 `security.token-store.key-prefix`의 값은 web `""`, admin `"admin:"`, ceo `"ceo:"`입니다. 접두사가 필요한 새 저장소는 `RedisTokenStoreProperties`를 주입받는 어댑터를 `infrastructure:redis`에 추가합니다.
- 키 공간이 바뀌면 배포 시점에 기존 로그인 세션이 전부 무효화되는데, 이것은 예외가 아니라 조용한 무효화로 드러납니다. 그래서 `infrastructure:redis`의 고정값 단위 테스트가 키 접두사의 유일한 자동 방어선입니다.
- `security-core`의 compileClasspath에서 `spring-boot-starter-data-redis`가 사라졌습니다. 확인 명령은 `./gradlew :security-core:dependencies --configuration compileClasspath | grep -c redis`이고 0이어야 합니다.
- 계약 소유 모듈이 다시 `infrastructure:redis`를 의존하게 되면 이 간선이 되살아납니다. 이 회귀는 컴파일과 기동이 모두 성공하고 batch 배포 산출물만 조용히 커지는 형태로 드러나므로, 문서 규칙으로 막습니다.
- 이후 [결정 0023](#decision-0023)에서 `RateLimitCounterPort`도 같은 구조(계약은 `security-core`, 구현은 `infrastructure:redis`)로 옮겨졌습니다.

<a id="decision-0023"></a>

### 0023. RateLimitCounterPort를 security-core로 옮겨 redis→표현 계층 의존을 끊는다

- 상태: 채택
- 결정 커밋: `8485c0e8a` (2026-09-27)
- 대체한 결정: [결정 0011](#decision-0011)
- 원문: 삭제 전 git 이력의 `backend/security-core/AGENTS.md`의 결정 0023

#### 맥락

**`RateLimitCounterPort`**는 rate limit(일정 시간 안의 호출 횟수 제한) 카운터 계약입니다. 시그니처는 `boolean isLimitExceeded(String key, int limit, Duration duration)`이고, 구현은 `infrastructure:redis`의 `RedisRateLimitCounter`, 소비자는 `api-common-module`의 `RateLimitAspect`입니다.

챕터 02([결정 0011](#decision-0011))에서 rate limit의 웹 관심사(애노테이션·aspect·예외·키 조립)를 표현 모듈 `api-common-module`로 올렸습니다. 이때 계약 `RateLimitCounterPort`도 함께 `com.tastyhouse.apicommon.ratelimit`에 두었습니다. 그러자 아웃바운드 어댑터 `infrastructure:redis`가 포트 인터페이스 **하나** 때문에 `starter-web`·springdoc을 노출하는 **표현 모듈**을 컴파일 의존하는 수평 간선이 남았습니다.

#### 이전 규칙

##### backend/api-common-module/AGENTS.md — `RateLimitCounterPort`의 거처(챕터 02)

이 포트는 `com.tastyhouse.apicommon.ratelimit`에 둔다. 그 전에는 api-common-module이 `RateLimitException` 처리를 위해 `infrastructure:redis`를 의존했다. 그 인프라 모듈은 `HttpServletRequest`로 클라이언트 IP를 해석하느라 서블릿 스택까지 끌어왔다. 그래서 웹 관심사를 표현으로 올리고 카운터만 인프라에 남겨 의존 방향을 바로잡았다. 이 모듈은 rate limit의 표현과 함께 **계약(`RateLimitCounterPort`)도 둔다**.

##### backend/infrastructure/redis/AGENTS.md — 패키지 구조 / Internal

의존 방향은 `infrastructure:redis → api-common-module`(어댑터 → 계약) 한 방향뿐이다. `api-common-module`(implementation)은 `RedisRateLimitCounter`가 구현하는 `RateLimitCounterPort`의 소유 모듈이다. 토큰 저장소 어댑터가 `security-core` 포트를 구현하는 구조([결정 0019](#decision-0019))는 이것과 "동형"이라고 설명했다.

##### backend/api-common-module/AGENTS.md — Subdirectories / 여기에 두면 안 되는 것

이 모듈의 rate limit 패키지가 카운터 계약 `RateLimitCounterPort`를 소유한다고 적었고, "여기에 두면 안 되는 것"에서도 이 모듈은 표현과 함께 계약(`RateLimitCounterPort`)만 두고 구현은 인프라 모듈이 맡는다고 했다. 이 결정으로 계약도 이 모듈을 떠났다.

##### backend/infrastructure/redis/AGENTS.md — Dependencies / External

`api-common-module`을 `implementation`으로 의존했으므로 테스트 컴파일 클래스패스에도 보여, 테스트용 `api-common-module` 별도 선언은 불필요하다고 적었다. 이 결정으로 main 의존이 삭제됐고 `afterName` 문자열 가드 테스트도 `api-common-module`로 옮겨져, redis → api-common 방향은 테스트 클래스패스에서도 사라졌다.

##### backend/AGENTS.md — Module Dependency Graph

`infrastructure:redis`가 `security-core`(토큰 저장소 포트) 외에 `api-common-module (implementation)`도 의존하는 간선(`RateLimitCounterPort` 구현)을 그렸고, api-common 쪽에도 "infrastructure:redis가 이 모듈의 RateLimitCounterPort를 구현한다"고 적었다. 지금은 redis의 내부 의존은 security-core 하나이고, api-common은 `security-core`를 `implementation`(api 노출 안 함)으로 받는다.

##### backend/api-common-module/AGENTS.md — `afterName`에 클래스 리터럴을 쓸 수 없는 이유

aspect를 등록하는 auto-configuration이 redis 설정 뒤에 평가되도록 `afterName`을 썼다. `afterName` 순서 검증 테스트는 redis 모듈에 있었다(redis → api-common 방향 테스트).

#### 결정

계약 `RateLimitCounterPort`를 서블릿-프리 보안 코어 `security-core`(`com.tastyhouse.security.ratelimit`)로 옮깁니다(본문 동일, `git mv`). 키 조립 등 HTTP 관심사는 그대로 api-common의 `RateLimitAspect`에 남깁니다.

| 항목 | before | after |
|---|---|---|
| `RateLimitCounterPort` 소유 | `api-common-module`(`com.tastyhouse.apicommon.ratelimit`) | `security-core`(`com.tastyhouse.security.ratelimit`) |
| 의존 방향 | `infrastructure:redis → api-common-module` | `infrastructure:redis → security-core ← api-common-module` |
| `infrastructure:redis` 내부 의존 | `security-core` + `api-common-module` | `security-core` 하나 |
| api-common의 포트 획득 | 자기 소유 | `implementation project(':security-core')` |
| `afterName` 순서 검증 테스트 | redis 모듈 테스트 | api-common 테스트(`testImplementation` redis) |
| Redis 키·Lua·배선 조건·응답 | — | 변경 없음 |

#### 근거

- **보안 관심사입니다.** `@RateLimit`의 실사용처는 로그인 브루트포스 방어(web·admin·ceo `login`)이고, 카운터는 인증 방어 수단입니다. JWT·토큰 저장소와 같은 모듈에 있는 것이 자연스럽습니다.
- **서블릿-프리입니다.** 시그니처가 `String`·`int`·`Duration`뿐이라 `security-core`의 존재 이유(서블릿 타입 금지)를 해치지 않습니다.
- **토큰 저장소 포트와 같은 구조입니다.** redis는 이미 토큰 저장소 구현 때문에 `security-core`를 의존하므로, **새 간선 없이** redis → api-common 간선만 사라집니다.
- 챕터 02의 교정(웹 관심사를 표현으로 올리고 카운터만 인프라에 남김)은 지금도 유효합니다. 틀린 것은 **계약의 거처**였습니다.
- 기각한 대안은 다음 두 가지입니다.
  - **포트 하나만 담는 초소형 모듈**: 모듈 수만 늘고, 소비자·구현자가 이미 `security-core`를 보거나 볼 수 있어 실익이 없습니다.
  - **`domain`**: rate limit은 비즈니스 규칙이 아니라 요청 방어 기술입니다. 또 redis가 `domain`을 모르는 순수 기술 모듈이라는 성질을 깹니다.

#### 결과·감수한 손실

- 구현과 표현이 main 클래스패스에서 서로를 모릅니다. 테스트에서도 redis → api-common 방향이 사라졌습니다.
- `infrastructure:redis`의 compileClasspath에 `api-common-module`·`starter-web`이 없어야 합니다. 확인 명령은 `./gradlew :infrastructure:redis:dependencies --configuration compileClasspath | grep -cE 'project :api-common-module|starter-web'`이고, 2026-09-27 실측 0건이었습니다.
- 카운팅 방식(Fixed Window)·Redis 키·Lua·배선 조건은 이동 전후로 동일합니다(동작 변경 없음).
- 이후 [결정 0038](../AGENTS.md#decision-0038)로 aspect 등록 설정이 `ApiCommonRateLimitAutoConfiguration`에서 `ApiCommonRateLimitConfig`로 바뀌면서 빈 존재 조건(`@ConditionalOnBean(RateLimitCounterPort.class)`)과 `afterName`, 그 `afterName` 문자열 검증 테스트가 없어졌습니다.

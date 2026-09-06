<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-09-01 -->

# security-core

## Purpose
`application`·`security-module`이 공유하는 **서블릿-프리 보안 코어 라이브러리 모듈**(`java-library`, 챕터 03 신설). `JwtTokenProvider`(서명/파싱)와 JWT 세션 토큰 저장소 **포트** 6종(RefreshToken/Blacklist/소셜 임시토큰 4종)을 캡슐화한다. **챕터 01에서 저장소 6종이 구체 Redis 클래스에서 인터페이스로 바뀌었고, 구현은 `infrastructure:redis`의 `token` 패키지가 갖는다**(어댑터 → 계약).

**신설 배경**: 기존에는 이 타입들이 `security-module`에 서블릿 결합 타입(JWT 인증 필터 `OncePerRequestFilter` 상속, `JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler`)과 함께 있었는데, `application`이 `JwtTokenProvider`·토큰 저장소를 쓰려고 `security-module`을 의존하면 `starter-web`·서블릿 필터까지 컴파일 클래스패스에 딸려 들어와 **application 계층의 클래스패스가 서블릿 스택으로 오염**됐다. ArchUnit `applicationMustBeServletFree`는 소스의 import만 검사하므로 이 클래스패스 오염을 막지 못했다 — 그래서 서블릿-프리 타입만 이 모듈로 분리해 **빌드 그래프로 강제**한다. 자세한 배경은 `security-module/AGENTS.md`의 [security-core 분리](../security-module/AGENTS.md#security-core-분리-챕터-03)와 루트 [CLAUDE.md 모듈 지도](../CLAUDE.md#모듈-지도-모듈-재편-완료--application-모듈-통합--external-분리) 참고.

**API 변경 없음** — JWT 토큰 포맷·Redis key prefix·인증 플로우는 분리 전과 완전히 동일하다. 모듈 소속만 바뀌었다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + `domain`(implementation — `ErrorCode`로 토큰 검증 실패 표현) (챕터 01에서 `infrastructure:redis` 의존 **삭제** — 토큰 저장소가 포트가 되어 Redis 타입을 보유하지 않는다) + `spring-security-core`(api — `JwtTokenProvider`가 `Authentication`·`UserDetails`·`GrantedAuthority`를 시그니처에 노출) + JJWT(`jjwt-api` api, `jjwt-impl`/`jjwt-jackson` runtimeOnly). **서블릿 스택(`starter-web`·`jakarta.servlet`) 의존이 없다** — 그것이 이 모듈 존재 이유다. `bootJar` 비활성 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/security/jwt/` | `JwtTokenProvider`(파라미터형 POJO, 서명/파싱), `JwtProperties`, `TokenType`, `JwtPrincipal`/`JwtPrincipalFactory`(앱별 principal 재구성 계약) |
| `src/main/java/com/tastyhouse/security/token/` | **인터페이스 6종**(챕터 01) — `RefreshTokenRepository`·`BlacklistRepository` + 소셜 임시토큰 4종(`Kakao`/`Naver`/`Apple`/`Facebook`TempTokenRepository). 구현·키 접두사·TTL 정책은 전부 `infrastructure:redis`의 `com.tastyhouse.infrastructure.redis.token`이 소유한다 |

자바 패키지는 `com.tastyhouse.security..`로 **`security-module`과 동일**하다(split package — 모듈 재편 선례와 같은 방식으로, 이동 대상만 패키지를 유지한 채 모듈을 옮겼다). `SecurityModuleAutoConfiguration`(`security-module` 소유, 챕터 02로 `SecurityModuleConfig`에서 리네임 + `@AutoConfiguration`)의 `@ComponentScan("com.tastyhouse.security")`가 패키지 불변 덕분에 이 모듈로 이동한 `@Repository` 빈들도 그대로 스캔한다.

## For AI Agents

### Working In This Directory
- **이 모듈에 서블릿 타입을 끌어들이지 않는다.** `jakarta.servlet.*`·`OncePerRequestFilter`·`AuthenticationEntryPoint`·`AccessDeniedHandler` 등 서블릿 결합 타입이 필요한 기능은 이 모듈이 아니라 `security-module`에 둔다 — 그것이 이 모듈이 `application`의 안전한 의존 대상으로 남는 유일한 이유다.
- **토큰 저장소 6종은 이 모듈이 계약만 갖는다**(챕터 01). 구현 기술을 이름에 넣지 않으므로 `Redis` 중간어를 붙이지 않고(`RefreshTokenRepository`), 구현체는 `infrastructure:redis`가 `Redis` 접두어로 갖는다(`RedisRefreshTokenRepository`). **여기에 `StringRedisTemplate`을 다시 끌어들이지 않는다** — 그 간선을 끊은 것이 이 챕터의 목적이며, 되돌리면 `application`·batch runtimeClasspath에 redis·api-common·springdoc 전이가 되살아난다.
- **앱별 키 접두사는 이제 프로퍼티다**: `security.token-store.key-prefix`(web 기본 `""`, admin `"admin:"`, ceo `"ceo:"`). 과거 3앱이 각자 갖던 `config/jwt/RedisRepositoryConfig`는 **삭제됐다** — 접두사를 생성자로 주입하려고 앱이 `StringRedisTemplate`을 직접 참조하던 구조가 사라졌기 때문이다. 새 저장소가 접두사를 필요로 하면 `RedisTokenStoreProperties`를 주입받는 어댑터를 `infrastructure:redis`에 추가한다.
- **소셜 임시토큰 저장소는 접두사가 고정**(`kakao_temp:` 등)이라 프로퍼티를 받지 않는다. 어댑터가 `@Component`로 자기 등록한다(web-api 전용, 다른 앱은 빈이 떠도 무해).
- **`JwtTokenProvider`는 `@Component`가 아닌 파라미터형 POJO**다. principal 식별자 클레임명(`memberId`/`adminId`)과 principal 재구성 팩토리(`JwtPrincipalFactory`)를 생성자로 받아 앱별 차이를 흡수한다. 각 API는 이 클래스를 상속한 얇은 `@Component` 하위 클래스로 자기 등록한다 — reference: `web-api`/`admin-api`의 `config/jwt/JwtTokenProvider`(`super(props, "memberId"|"adminId", CustomUserDetails::new)`). web은 검증용 토큰(휴대폰/이메일/개인정보/비밀번호 재설정) 발급 메서드를 **web 전용으로만** 추가한다(admin은 미사용). `key`/`parseClaims`/`jwtProperties`는 `protected`라 하위 클래스가 재사용한다.
- **`JwtPrincipal`/`JwtPrincipalFactory`는 앱별 principal 차이를 흡수하는 계약**이다. 각 API의 `CustomUserDetails`가 `JwtPrincipal`을 구현해 `getPrincipalId()`(web=memberId, admin=adminId)를 노출하고, `JwtPrincipalFactory`는 클레임에서 그 principal을 재구성한다.
- **시크릿은 각 API의 `application.yml`이 소유**하며 web-api와 admin-api는 반드시 서로 다른 `jwt.secret`(`JWT_SECRET_WEB` vs `JWT_SECRET_ADMIN`, ceo는 `JWT_SECRET_CEO`)을 써야 한다. 동일 시크릿이면 한쪽 access 토큰이 다른 쪽 인증을 통과해 권한 상승이 발생한다. 이는 `JwtProperties` Javadoc에도 명시되어 있다.
- **이 모듈이 `domain`을 의존하는 이유**는 토큰 검증 실패를 `com.tastyhouse.domain.exception.ErrorCode`로 표현하기 위해서다(그 밖의 도메인 타입 참조 없음). 도메인 타입을 새로 끌어들이는 확장은 지양한다.
- **Redis key prefix는 불변이다**(`rt:`/`bl:`/`admin:rt:`/`admin:bl:` 등). 챕터 03 이관도, 챕터 01의 포트/어댑터 역전도 소유 모듈만 바꿨을 뿐 런타임 키 공간을 **바이트 단위로 건드리지 않았다** — 바뀌면 배포 시점에 기존 로그인 세션이 전부 무효화된다. 접두사 조합은 예외가 아니라 **조용한 무효화**로 드러나므로, `infrastructure:redis`의 고정값 단위 테스트가 그 유일한 자동 방어선이다.

### Testing Requirements
- 접두사 조합 검증은 이 모듈이 아니라 **구현을 가진 `infrastructure:redis`**의 단위 테스트가 담당한다(`RedisRefreshTokenRepositoryTest`·`RedisBlacklistRepositoryTest` — `""`/`"admin:"`/`"ceo:"` 고정값 단정). 이 모듈에는 인터페이스만 있어 검증할 동작이 없다.
- **JWT 시크릿 분리 회귀 방지**: web에서 발급한 access 토큰을 admin API에 제시하면 401(서명 불일치)이 되어야 한다. 두 앱이 같은 시크릿을 쓰지 않는지 통합 테스트/배포 체크로 확인한다.
- 기동 스모크(로그인 → 인증 필요 엔드포인트 1회 호출)로 필터+프로바이더+저장소 전 경로가 여전히 배선되는지 확인한다 — `contextLoads`류 테스트는 빈 배선을 증명하지 못한다(선례).

## Dependencies

### Internal
- `domain` (implementation) — `com.tastyhouse.domain.exception.ErrorCode` 참조

**`infrastructure:redis` 의존은 챕터 01에서 삭제됐다**(방향 역전 — 이제 redis가 이 모듈을 의존한다).

### External
- `spring-security-core` (api) — `JwtTokenProvider`가 `Authentication`·`UserDetails`·`GrantedAuthority`를 시그니처에 노출
- `jjwt-api` (api) + `jjwt-impl`·`jjwt-jackson` (runtimeOnly) — `JwtTokenProvider` 서명/파싱

**`spring-boot-starter-data-redis`는 이제 이 모듈의 클래스패스에 없다**(챕터 01). `./gradlew :security-core:dependencies --configuration compileClasspath | grep -c redis`가 0이어야 한다.

### Consumers (챕터 03)
- `application` — `implementation project(':security-core')`로 직접 의존(서블릿 스택 없이 auth/token 서비스가 사용). batch 유스케이스는 원래 security를 쓰지 않으며, 모듈 통합 후 클래스패스에 보이더라도 참조하지 않는다
- `security-module` — `api project(':security-core')`로 재노출(잔류한 서블릿 결합 타입이 이 모듈의 `JwtTokenProvider`·토큰 저장소 포트를 쓴다. `JwtAuthenticationFilter`가 `BlacklistRepository`를 받는다)
- `infrastructure:redis` — `implementation project(':security-core')`(챕터 01 신설 간선). 토큰 저장소 포트 6종의 **구현**을 갖는다
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

### 소셜 임시토큰 저장소 4종 — 1회용 토큰의 수명

**대상**: `backend/security-core/src/main/java/com/tastyhouse/security/token/`
→ `KakaoTempTokenRepository` · `NaverTempTokenRepository` · `AppleTempTokenRepository` ·
`FacebookTempTokenRepository`

`NEEDS_SIGN_UP` / `NEEDS_LINKING` 응답 시 발급되고, **회원가입·계정 연동 완료 시 삭제되는 1회용
토큰**이다. 구현은 `infrastructure:redis`의 `token` 패키지에 있으며 키 접두사·TTL 정책은 어댑터가
소유한다.

**Apple만 저장 대상이 다르다** — Apple은 UserInfo 엔드포인트가 없으므로 accessToken이 아닌
`id_token`을 저장한다(`AppleTempTokenRepository.save(appleTempToken, appleIdToken)`). 이 id_token은
이미 서버에서 검증 완료된 상태이며, sub/email 재추출 시 재파싱된다.

### `RefreshTokenRepository.isInvalid`가 default 메서드인 이유

**대상**: `backend/security-core/src/main/java/com/tastyhouse/security/token/RefreshTokenRepository.java`
→ `isInvalid(String, String)`

저장된 값과의 단순 비교라 어댑터마다 다를 여지가 없어 계약 쪽에 default로 둔다. `BlacklistRepository`와
함께 앱별 키 접두사는 `security.token-store.key-prefix`가 결정한다.

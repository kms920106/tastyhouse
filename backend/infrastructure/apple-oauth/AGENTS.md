<!-- Parent: ../../AGENTS.md -->

# infrastructure:apple-oauth

애플 로그인 **벤더 모듈**(`java-library`). `web-application`(앱 마커 제거 전에는 `application`)의 SPI `SocialOAuthClient`를 `AppleOAuthClient`가 구현하고 `provider()`로 `SocialProvider.APPLE`을 알린다. 앱이 아니라 소셜 로그인 채널 스타터 `infrastructure:oauth`가 `runtimeOnly`로 조립한다.

옛 `infrastructure:oauth`의 `apple/` 패키지를 채널·벤더 분리(2026-09-27)로 옮겨 신설됐다. 패키지는 `external.oauth.apple` → `com.tastyhouse.external.apple.oauth`로 옮겼다. 이후 infrastructure 패키지 루트 통일로 `com.tastyhouse.infrastructure.apple.oauth`가 됐고, wire DTO는 하위 패키지 `com.tastyhouse.infrastructure.apple.oauth.dto`로 모였다. 클래스명은 그대로라 빈 이름 `appleOAuthClient`(소비 측 `@Qualifier`)도 불변이다. **jjwt를 직접 선언하는 유일한 소셜 벤더 모듈이다.**

## 무엇을 소유하는가

```
com.tastyhouse.infrastructure.apple.oauth/
├── AppleOAuthModuleAutoConfiguration.java  @AutoConfiguration + @ComponentScan(이 패키지) + @EnableConfigurationProperties(AppleOAuthProperties)
├── AppleOAuthProperties.java               oauth.apple.* (team-id, client-id, key-id, redirect-uri, private-key)
├── AppleOAuthClient.java                   SocialOAuthClient 구현 — ES256 client_secret 생성·토큰 교환·id_token(RS256) 검증
└── dto/
    ├── AppleTokenResponse.java             wire DTO
    └── AppleIdTokenPayload.java            id_token claim 해석
```

`AppleOAuthClient`는 `@Value`를 쓰지 않는다. 생성자에서 `AppleOAuthProperties`를 받아 같은 이름의 `final` 필드로 옮기며, **개인키만 이름이 다르다** — `privateKeyBase64 = properties.privateKey()`(값이 Base64 문자열이라는 것을 필드명이 드러낸다).

테스트: `AppleOAuthModuleAutoConfigurationTest`(`ApplicationContextRunner`, 4건) — 프로퍼티 바인딩(`bindsProperties`), 빈 이름·`provider()`(`registersClientBeanUnderQualifierName`), 키 누락 시 기동 실패(`failsStartupWhenPropertyMissing`), 환경변수 미해석 시 기동 실패(`failsStartupWhenPlaceholderUnresolved`).

## 어느 앱이 의존하는가

앱은 직접 의존하지 않는다. web-api → `infrastructure:oauth`(runtimeOnly) → 이 모듈(runtimeOnly)로 전이된다. 벤더 추가·제거 절차는 `../oauth/AGENTS.md`.

## yml — `application-apple-oauth.yml`

```yaml
oauth:
  apple:
    team-id: ${APPLE_TEAM_ID}
    client-id: ${APPLE_CLIENT_ID}
    key-id: ${APPLE_KEY_ID}
    redirect-uri: ${APPLE_REDIRECT_URI}
    private-key: ${APPLE_PRIVATE_KEY}
```

채널의 `application-oauth.yml`이 중첩 import로 로딩한다. 키 접두어는 `{채널}.{벤더}`이며 분리 전 web-api `application.yml`의 `apple.*`에서 바뀌었다. 환경변수 이름은 그대로다.

## Dependencies

- `infrastructure:restclient` (implementation) — Boot `RestClient.Builder` customizer. 토큰 교환·JWKS 조회는 **동기 `RestClient`**다.
- `web-application` (implementation) — 구현하는 SPI(`com.tastyhouse.application.auth.port.out`)의 소유 모듈. **앱 마커 제거로 `:application` → `:web-application`으로 바뀌었다** — 소셜 로그인 SPI가 web 전용이라 web 앱 모듈로 옮겨갔기 때문이다(패키지는 그대로)

**`domain` 의존은 없다.** `exchange()`/`fetchProfile()`은 id_token 검증 실패를 `BusinessException(ErrorCode.APPLE_ID_TOKEN_INVALID)`로 직접 던지지 않고, `SocialOAuthResult.failed(SocialOAuthFailure.ID_TOKEN_INVALID)`(둘 다 `application.auth.port.out` 소유)를 반환한다. 실패를 `BusinessException`으로 번역하는 책임은 이 어댑터가 아니라 `application.auth.service.SocialOAuthFailures`(소비 측 `*SocialLoginService` 4종이 `.orElseThrow(SocialOAuthFailures::toException)`으로 호출)로 옮겨갔다.
- `io.jsonwebtoken:jjwt-api:0.13.0` (implementation) + `jjwt-impl`·`jjwt-jackson` (runtimeOnly) — client_secret ES256 서명(비공개키 PKCS8, Base64 저장), id_token RS256 검증(Apple JWKS 공개키). **컴파일 시점 격리만이다** — 이 선언으로 다른 소셜 벤더 모듈의 컴파일 클래스패스에 jjwt가 없지만, 런타임에는 `application → security-core`(`api` jjwt-api, `runtimeOnly` impl·jackson) 경로로 jjwt가 web-api 전체에 이미 실려 있다. 분할 전 `infrastructure:oauth`가 카카오·네이버·페이스북까지 jjwt를 컴파일 클래스패스에 두던 것을 애플 한 모듈로 좁힌 것이 이 선언의 의미다.
- `infrastructure:oauth`를 의존하지 않는다(순환 방지)

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **`@ConditionalOnProperty`를 붙이지 않는다** — 소셜 제공자는 공존한다(`../oauth/AGENTS.md`의 봉인 항목).
- **`APPLE_TEAM_ID` 등이 없으면 기동이 실패한다.** `AppleOAuthProperties`의 compact constructor가 값마다 null·공백·`${`(해석되지 않은 placeholder)를 검사해 `IllegalStateException`을 던지고, 바인딩 실패로 컨텍스트 생성이 중단된다. 메시지는 `oauth.apple.team-id 설정값이 비어 있습니다` 또는 `... 환경변수가 해석되지 않았습니다: ${APPLE_TEAM_ID}` 형태다. `@ConfigurationProperties` 바인딩은 해석하지 못한 placeholder를 문자열 그대로 넣어 그냥 두면 첫 로그인 호출에서야 실패하므로(`../tosspayments/AGENTS.md`의 `TOSS_SECRET_KEY`가 그 상태다), 분할 전 `@Value` 시절의 기동 시 실패를 이 검사로 되살렸다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### `AppleOAuthProperties`의 기동 시 검증을 지우지 않는다

**대상**: `backend/infrastructure/apple-oauth/src/main/java/com/tastyhouse/infrastructure/apple/oauth/AppleOAuthProperties.java` → compact constructor · `requireResolved`

설정 누락을 기동 시점에 드러내는 유일한 장치다. `@Validated` + `@NotBlank`로 바꾸지 않는다 — 해석되지 않은 placeholder는 `${...}` 리터럴이라 공백이 아니어서 통과한다. 검사를 지우면 누락된 환경변수가 조용히 바인딩돼 운영 배포 후 첫 로그인에서야 드러난다. 반증 테스트 `failsStartupWhenPropertyMissing`·`failsStartupWhenPlaceholderUnresolved`가 이 동작을 고정한다.

### 자바 패키지 `com.tastyhouse.external.apple.oauth` 봉인

**대상**: `backend/infrastructure/apple-oauth/src/main/java/com/tastyhouse/infrastructure/apple/oauth/`

~~`com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 통째 스캔에 걸려 admin·ceo·batch 부팅이 깨진다.~~ **(번복됨 — infrastructure 패키지 루트 통일)** persistence가 자기 루트 `com.tastyhouse.infrastructure.persistence`만 스캔하게 되면서 이 제약이 사라졌다. 지금 이 모듈의 루트는 `com.tastyhouse.infrastructure.apple.oauth`이고 main 클래스는 전부 그 아래에 있어야 한다 — `backend/infrastructure/apple-oauth/src/test/java/com/tastyhouse/infrastructure/apple/oauth/architecture/VendorLayerRulesTest.java` → `shouldResideInModuleRootPackage`이 강제한다. 또한 이 패키지 이름은 web-api ArchUnit `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`가 문자열로 참조하므로, 이름을 바꾸면 **규칙이 조용히 대상을 잃는다**(루트 통일 때 그 목록을 `com.tastyhouse.infrastructure.{kakao,naver,facebook,apple}.oauth..`로 교체했다).

### id_token 검증 실패는 `SocialOAuthResult.failed`로 표현하고, `BusinessException` 번역은 `application`이 한다

**대상**: `backend/infrastructure/apple-oauth/src/main/java/com/tastyhouse/infrastructure/apple/oauth/AppleOAuthClient.java` → `verifyIdToken`·`exchange`·`fetchProfile`

검증 실패의 bare `RuntimeException`을 이 어댑터가 삼키고 `SocialOAuthResult.failed(SocialOAuthFailure.ID_TOKEN_INVALID)`로 표현하는 것은 이 어댑터의 책임이다(과거 web-api `AppleSocialLoginService` 3곳에 중복돼 있던 try/catch를 어댑터로 회수한 것이 그 시작이었다). 다만 그 결과를 도메인 의미의 예외(`BusinessException(ErrorCode.APPLE_ID_TOKEN_INVALID)`)로 번역하는 것은 이제 이 어댑터가 아니라 `application.auth.service.SocialOAuthFailures`가 한다 — 어댑터는 `BusinessException`/`ErrorCode`를 참조하지 않는다. 응답 계약(`APPLE_ID_TOKEN_INVALID`)은 무변경이다. 번역을 어댑터로 되돌리지 않는다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### Apple id_token payload claim의 의미

**대상**: `backend/infrastructure/apple-oauth/src/main/java/com/tastyhouse/infrastructure/apple/oauth/dto/AppleIdTokenPayload.java`

Apple id_token JWT payload의 claim 해석 규약이다.

| claim | 의미 |
|---|---|
| `sub` | Apple 사용자 고유 식별자. **앱별 고정값(pairwise)** 이므로 앱이 다르면 같은 사용자라도 값이 다르다 |
| `email` | 실제 이메일 또는 Private Relay 주소(`@privaterelay.appleid.com`) |
| `emailVerified` | 항상 `true`(Apple은 검증된 이메일만 반환). **wire 타입이 `String` 또는 `Boolean` 둘 다 올 수 있다** |
| `isPrivateEmail` | 이메일이 프라이빗 릴레이 주소인지 여부 |

### Apple 로그인이 표준 OAuth와 다른 두 지점

**대상**: `backend/infrastructure/apple-oauth/src/main/java/com/tastyhouse/infrastructure/apple/oauth/AppleOAuthClient.java`

1. **`client_secret`이 shared secret이 아니라 ES256 서명된 JWT여야 한다.** 일반 shared secret은 미지원이다. 생성 규약은 `iss` = Team ID, `sub` = Services ID(= client_id), `aud` = `https://appleid.apple.com`이며 유효기간은 최대 6개월(현재 구현은 180일).
2. **UserInfo 엔드포인트가 없다.** id_token(RS256 JWT) 자체가 유일한 프로필 소스다. 그래서 `exchange()`가 액세스 토큰이 아니라 id_token을 자격증명으로 반환하며, **그 시점에 한 번 검증해 잘못된 토큰이 Redis 임시토큰 저장소에 들어가지 않게 한다.**

`fetchProfile()`은 호출마다 Apple JWKS를 네트워크로 받아 서명을 재검증하므로(`verifyAndExtractIdToken` → `fetchApplePublicKey`) 값싼 조회가 아니다(호출 빈도에 주의).

`.p8` 개인키는 **개행을 제거한 Base64 문자열**로 설정(`oauth.apple.private-key`)에 담는다(`-----BEGIN/END PRIVATE KEY-----` 헤더 제외). 어댑터가 이것을 `ECPrivateKey`로 복원한다.

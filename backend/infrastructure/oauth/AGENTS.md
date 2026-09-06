<!-- Parent: ../../AGENTS.md -->

# infrastructure:oauth

소셜 로그인(카카오·네이버·애플·페이스북) 클라이언트를 소유하는 어댑터 모듈(`java-library`). `infrastructure:external` 7모듈 분리(챕터 01)로 코어에서 떨어져 나왔고, **자바 패키지 `com.tastyhouse.external.oauth..`는 불변**이다(코어 스캔 범위가 `external.config`·`external.file`이라 동반 스캔 위험이 없고, web-api ArchUnit 규칙이 이 패키지 이름을 직접 참조한다).

## 무엇을 소유하는가

```
com.tastyhouse.external.oauth/
├── OAuthModuleAutoConfiguration.java  진입점 — 챕터 01 ExternalOAuthConfig → OAuthModuleConfig 리네임, 챕터 02로 @AutoConfiguration 전환
├── kakao/    KakaoOAuthClient · KakaoTokenResponse · KakaoUserInfoResponse
├── naver/    NaverOAuthClient · NaverTokenResponse · NaverUserInfoResponse
├── apple/    AppleOAuthClient · AppleTokenResponse · AppleIdTokenPayload
└── facebook/ FacebookOAuthClient · FacebookTokenDebugResponse · FacebookUserInfoResponse
```

제공자별 클라이언트 4종과 그 wire DTO만 있다. **SPI(계약) 자체는 이 모듈이 아니라 `application` 모듈이 소유한다** — 아래 절 참조.

## 어느 앱이 의존하는가

**web-api 하나뿐이다.** admin-api·ceo-api·batch-module은 이 모듈을 의존하지 않으므로 OAuth 빈이 그 컨텍스트에 아예 올라오지 않는다(챕터 02 이후로는 의존 선언이 곧 활성화다).

### ⚠️ 다른 앱이 이 모듈에 의존하면 기동에 실패한다 (사고 기록 — 챕터 02로 실패 조건이 바뀜)

이 패키지의 빈들은 `kakao.*`·`naver.*`·`facebook.*`·`apple.*` 프로퍼티를 요구하고, 그 값은 **web-api의 `application.yml`에만** 있다. **챕터 02 이후 이 모듈은 auto-configuration이라 "의존 선언 = 활성화"다** — 과거처럼 다른 앱이 `OAuthModuleConfig`를 `@Import`해야 발화하는 것이 아니라, `implementation`/`runtimeOnly`로 의존을 추가하는 순간 `OAuthModuleAutoConfiguration`이 클래스패스 존재만으로 자동 등록된다. 그 상태에서 다른 앱이 이 모듈에 의존하면 컨텍스트 로딩 중 `Could not resolve placeholder 'apple.team-id'`로 **부팅이 깨진다.** 이것은 가정이 아니라 실제 이력이다 — batch-module이 이 실패를 냈다.

분리 전에는 코어 `ExternalModuleConfig`가 이 패키지를 REGEX `excludeFilters`로 제외해서 같은 효과를 냈고, admin/ceo/batch가 그 REGEX를 각자 복사해 유지해야 했다. **지금은 모듈 경계(= build.gradle 의존 선언)가 그 역할을 대신한다** — 의존하지 않는 앱에는 클래스 자체가 없으므로 제외 규칙이 필요 없다. 이 사고 기록을 남기는 이유는, 나중에 "설정을 한군데로 모으자"며 다른 앱에 이 의존을 추가하는 시도가 반복되기 쉽기 때문이다.

## SPI 규칙 — 계약은 `application`, 구현은 이 모듈

**소셜 로그인은 `com.tastyhouse.application.auth.port.out`의 SPI를 통해서만 사용한다.**

| 역할 | 위치 |
|---|---|
| 계약 — `SocialOAuthClient`(`provider()`/`exchange()`/`fetchProfile()`)와 중립 값 타입 `SocialProfile`·`SocialCredential`·`SocialAuthorization`·`SocialProvider` | **`application` 모듈**의 `com.tastyhouse.application.auth.port.out` |
| 구현 — 제공자별 클라이언트 4종 | 이 모듈의 `external.oauth.{kakao,naver,apple,facebook}` |

> **개정 이력**: 과거 이 SPI는 external 모듈 자신의 `external.oauth.spi` 패키지에 있었다(도메인 포트가 없는 공유 기술은 그 어댑터 모듈이 자기 SPI를 소유한다는 `security-module` 선례). 이후 읽기 경로 포트화·모듈 재편을 거치며 아웃바운드 계약이 전부 `application`의 `<ctx>/port/out`으로 모이면서 이 SPI도 그리로 옮겨갔고, 어댑터가 계약 소유 모듈을 의존하는 방향(adapter → port)이 됐다. **소셜 OAuth를 `domain`에 두지 않는 이유는 그대로 유효하다** — 호출부가 전부 표현·유스케이스 계층이라 도메인 서비스가 호출하는 포트가 아니므로, domain에 두면 "아무 도메인 서비스도 호출하지 않는 포트"가 된다.

**web-api는 SPI만 의존하고 제공자 패키지를 직접 import 하지 않는다.** 이것은 규율이 아니라 빌드 게이트다 — web-api의 ArchUnit `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`가 `com.tastyhouse.external.oauth.{kakao,naver,facebook,apple}..` 의존을 금지한다. **이 규칙이 패키지 문자열로 대상을 지정하므로, 이 모듈의 제공자 패키지 이름을 바꾸면 규칙이 조용히 대상을 잃는다.**

### 2단 계약이 제공자별 흐름 차이를 흡수한다
- `exchange(SocialAuthorization) → SocialCredential` — 카카오·네이버·애플의 토큰 교환, 페이스북의 app_id 검증
- `fetchProfile(SocialCredential) → SocialProfile` — 카카오·네이버·페이스북의 userinfo 조회, 애플의 id_token 검증·추출
- `state`는 네이버만 쓰며 나머지는 `null`이다.

### 제공자별 관심사는 어댑터가 갖는다
페이스북 app_id 검증(`${facebook.app-id}` + `debug_token`)과 애플 id_token 검증 예외 번역(`APPLE_ID_TOKEN_INVALID`)은 과거 web-api 서비스에 있었으나 `exchange()`/`fetchProfile()` 안으로 회수했다. 응답 계약(`SOCIAL_OAUTH_FAILED`·`APPLE_ID_TOKEN_INVALID`)은 무변경이다. **애플 `fetchProfile`은 호출마다 JWKS를 네트워크로 받아 서명을 재검증하므로 값싼 조회가 아니다.**

### 외부 응답 DTO는 도메인 타입을 반환하지 않는다 (역방향 누수 금지)
`SocialProfile`은 전 필드 `String`이며, `gender`도 도메인 enum이 아니라 상수명 문자열(`"MALE"`/`"FEMALE"`/`null`)을 담는다. 과거 `KakaoUserInfoResponse`·`NaverUserInfoResponse`가 편의 매퍼에서 도메인 enum `MemberGender`를 직접 반환해 어댑터 → domain 역결합이 있었는데, 소비 측이 곧바로 `.name()`으로 되돌리고 있어 그 결합이 아무 값도 사지 못했다. 지금은 카카오의 `"male"`/`"female"` 같은 제공자 어휘를 어댑터가 `"MALE"`/`"FEMALE"`로 정규화해 넘기고, 도메인 enum 승격은 소비 측이 `MemberGender.from(String)`으로 수행한다.

### 보존해야 하는 것 (통합 금지)
제공자별 Redis 임시토큰 저장소 4종과 **key prefix**(`kakao_temp:` 등), 제공자별 `*_TEMP_TOKEN_EXPIRED` `ErrorCode` 4종은 통합하지 않는다 — prefix를 바꾸면 배포 시점에 진행 중인 임시토큰이 전부 무효화되고, ErrorCode는 프론트가 분기할 수 있는 wire 계약이다. (저장소 자체는 이 모듈이 아니라 `security-core`에 있다.)

### 빈 주입
`SocialOAuthClient` 구현이 4개이므로 소비 측은 `@Qualifier("kakaoOAuthClient")`처럼 빈 이름을 명시한다. **`@Qualifier`는 필드가 아니라 생성자 파라미터에 단다** — 필드에만 달면 생성자 주입 경로에서 조용히 무시되고 주입이 빈 이름 우연 일치에만 의존하게 된다(Lombok 제거로 `lombok.copyableAnnotations`의 복사 효과가 사라진 뒤부터 해당).

## 진입 설정과 스캔 범위

`OAuthModuleAutoConfiguration`(챕터 02 — `@AutoConfiguration(proxyBeanMethods = false)`, `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록)이 `@ComponentScan("com.tastyhouse.external.oauth")`만 갖는다. **`@EnableConfigurationProperties`가 없다** — 이 모듈은 `@ConfigurationProperties` record를 쓰지 않고 제공자 설정값을 `@Value`로 직접 읽기 때문이다.

## yml — 없다

이 모듈에는 `src/main/resources`가 없다. OAuth 프로퍼티(`kakao.*`·`naver.*`·`facebook.*`·`apple.*`)는 **web-api의 `application.yml`에 그대로 있다.** 분리 시 별도 `application-oauth.yml`로 떼지 않은 것은 그 값들이 단일 소비 앱의 것이고, 옮기면 web-api 설정을 읽을 때 한 단계 더 따라가야 하기 때문이다.

## Dependencies

### Internal
- `infrastructure:external` (implementation) — `WebClient.Builder`, `ExternalApiException`/`ExternalApiErrorCode`
- `application` (implementation) — **구현하는 SPI(`com.tastyhouse.application.auth.port.out`)의 소유 모듈.** driven adapter가 자신이 구현하는 아웃바운드 포트를 의존하는 정상 방향이다. 반대 방향(`application → infrastructure:oauth`)은 선언돼 있지 않으므로 순환이 아니다
- `domain` (implementation) — 예외 계약과 도메인 타입

### External
- `spring-boot-starter-webflux` — 제공자 API 호출(`WebClient`)
- `io.jsonwebtoken:jjwt-api:0.13.0` + `runtimeOnly jjwt-impl`·`jjwt-jackson` — **애플 로그인 전용.** client_secret 생성은 ES256 서명(비공개키 PKCS8, Base64 저장), id_token 검증은 RS256(Apple JWKS에서 공개키 조회). **jjwt를 클래스패스에 올리는 유일한 external 계열 모듈이며, 이 모듈을 의존하는 앱은 web-api뿐이다** — 분리 전에는 4개 앱 전부가 jjwt를 받고 있었다(security 계열의 JWT 의존과는 별개 경로).

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **빈 배선**: web-api만 `implementation project(':infrastructure:oauth')`를 선언한다(챕터 02 — `runtimeOnly`). `OAuthModuleAutoConfiguration`이 클래스패스 존재만으로 자동 등록되므로 `@Import`는 없다. 다른 앱에 의존을 추가하지 않는다(위 사고 기록).
- **제공자 패키지 이름을 바꾸지 않는다** — web-api ArchUnit 규칙이 그 문자열을 참조한다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.external.oauth..` 봉인

**대상**: `backend/infrastructure/oauth/src/main/java/com/tastyhouse/external/oauth/` 이하 전 패키지

이 모듈을 포함한 외부 연동 7모듈은 자바 패키지 루트를 `com.tastyhouse.external..`로 유지한다. `com.tastyhouse.infrastructure` 아래로 옮기면 `infrastructure:persistence`의 `PersistenceModuleAutoConfiguration`이 `@ComponentScan("com.tastyhouse.infrastructure")`로 그 트리를 통째로 스캔하므로, **의존하지도 않은 어댑터까지 빈 스캔 범위에 들어와 admin-api·ceo-api·batch-module의 부팅이 깨진다.** 모듈 디렉터리 이름과 자바 패키지가 어긋나 보인다는 이유로 정리하지 않는다. 제공자 하위 패키지(`kakao`·`naver`·`apple`·`facebook`) 이름도 web-api ArchUnit `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`가 문자열로 참조하므로 함께 봉인 대상이다.

### 다른 앱에 이 모듈 의존을 추가하지 않는다

**대상**: `backend/infrastructure/oauth/src/main/java/com/tastyhouse/external/oauth/OAuthModuleAutoConfiguration.java`

이 패키지의 빈들은 `apple`·`kakao`·`naver`·`facebook` 프로퍼티를 요구하고 그 값은 web-api `application.yml`에만 있다. `@Import` 시절에는 의존 선언만으로 발화하지 않았으나 **지금은 의존 선언 자체가 활성화**이므로, 다른 앱의 `build.gradle`에 이 모듈을 추가하면 `Could not resolve placeholder 'apple.team-id'`로 기동에 실패한다(batch-module 실패 이력). 위 "다른 앱이 이 모듈에 의존하면 기동에 실패한다" 절과 같은 사실이며, 코드 주석 쪽에서도 같은 강도로 못박고 있었다.

### 외부 응답 DTO는 도메인 enum을 반환하지 않는다

**대상**: `backend/infrastructure/oauth/src/main/java/com/tastyhouse/external/oauth/kakao/KakaoUserInfoResponse.java` → `gender()` 정규화 매퍼
**대상**: `backend/infrastructure/oauth/src/main/java/com/tastyhouse/external/oauth/naver/NaverUserInfoResponse.java` → `gender()` 정규화 매퍼

두 DTO의 gender 매퍼는 도메인 enum `MemberGender`가 아니라 **그 상수명 문자열**(`"MALE"`/`"FEMALE"`/`null`)을 반환한다. 외부 응답 DTO가 도메인 타입을 보유하면 어댑터 → domain 역방향 결합이 생기기 때문이다. 도메인 enum 승격이 필요하면 소비 측(web-api 서비스)이 `MemberGender.from(String)`으로 수행한다. 편의를 이유로 여기서 enum을 반환하도록 되돌리지 않는다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### Apple id_token payload claim의 의미

**대상**: `backend/infrastructure/oauth/src/main/java/com/tastyhouse/external/oauth/apple/AppleIdTokenPayload.java`

Apple id_token JWT payload의 claim 해석 규약이다.

| claim | 의미 |
|---|---|
| `sub` | Apple 사용자 고유 식별자. **앱별 고정값(pairwise)** 이므로 앱이 다르면 같은 사용자라도 값이 다르다 |
| `email` | 실제 이메일 또는 Private Relay 주소(`@privaterelay.appleid.com`) |
| `emailVerified` | 항상 `true`(Apple은 검증된 이메일만 반환). **wire 타입이 `String` 또는 `Boolean` 둘 다 올 수 있다** |
| `isPrivateEmail` | 이메일이 프라이빗 릴레이 주소인지 여부 |

### Apple 로그인이 표준 OAuth와 다른 두 지점

**대상**: `backend/infrastructure/oauth/src/main/java/com/tastyhouse/external/oauth/apple/AppleOAuthClient.java`

1. **`client_secret`이 shared secret이 아니라 ES256 서명된 JWT여야 한다.** 일반 shared secret은 미지원이다. 생성 규약은 `iss` = Team ID, `sub` = Services ID(= client_id), `aud` = `https://appleid.apple.com`이며 유효기간은 최대 6개월(현재 구현은 180일).
2. **UserInfo 엔드포인트가 없다.** id_token(RS256 JWT) 자체가 유일한 프로필 소스다. 그래서 `exchange()`가 액세스 토큰이 아니라 id_token을 자격증명으로 반환하며, **그 시점에 한 번 검증해 잘못된 토큰이 Redis 임시토큰 저장소에 들어가지 않게 한다.**

`fetchProfile()`은 호출마다 Apple JWKS를 네트워크로 받아 서명을 재검증하므로 값싼 조회가 아니다(호출 빈도에 주의). 또한 검증 실패의 bare `RuntimeException`을 도메인 의미의 예외(`APPLE_ID_TOKEN_INVALID`)로 번역하는 것은 이 어댑터의 책임이다 — 과거 web-api `AppleSocialLoginService` 3곳에 중복돼 있던 try/catch를 어댑터로 회수한 것이며, 응답 계약은 무변경이다.

`.p8` 개인키는 **개행을 제거한 Base64 문자열**로 설정에 담는다(`-----BEGIN/END PRIVATE KEY-----` 헤더 제외). 어댑터가 이것을 `ECPrivateKey`로 복원한다.

### 페이스북은 토큰 교환 단계가 없다

**대상**: `backend/infrastructure/oauth/src/main/java/com/tastyhouse/external/oauth/facebook/FacebookOAuthClient.java` → `exchange()`

페이스북은 JS SDK가 클라이언트에서 이미 액세스 토큰을 발급하므로 교환할 것이 없다. 그래서 `exchange()`는 교환 대신 **Facebook 공식 문서가 요구하는 서버측 검증**(`debug_token`으로 토큰의 `app_id`가 우리 앱과 일치하는지 확인)을 수행하고 토큰을 그대로 돌려준다. 이 검증은 과거 web-api `FacebookSocialLoginService#validateToken`에 있었으나, `app_id` 설정값과 `debug_token` 호출은 어댑터의 관심사이므로 이 모듈로 회수했다.

### 네이버만 `state`를 쓴다 (CSRF 방어)

**대상**: `backend/infrastructure/oauth/src/main/java/com/tastyhouse/external/oauth/naver/NaverOAuthClient.java`

4개 제공자 중 네이버만 인가 요청·토큰 교환에 `state`를 함께 넘겨 CSRF를 방어한다. 나머지 3종은 `SocialAuthorization`의 `state`가 `null`이다. 이 비대칭은 제공자 사양 차이이며 통일 대상이 아니다.

### 네이버 응답의 결측·형식 처리

**대상**: `backend/infrastructure/oauth/src/main/java/com/tastyhouse/external/oauth/naver/NaverUserInfoResponse.java`

- 프로필 응답(`GET https://openapi.naver.com/v1/nid/me`)은 사용자 정보를 **최상위 `response` 객체 안에 중첩**해 돌려준다. 다른 3종과 달리 한 겹 더 벗겨야 한다.
- `response.gender()`는 **사용자가 성별 제공에 동의하지 않으면 `null`** 이므로 반드시 가드한다(카카오 형제와 동일한 이유).
- `birthday`는 `"MM-DD"` 형식이라 월·일을 각각 잘라 쓰며, **선행 0을 제거**해 반환한다(`"01"` → `"1"`, `"05"` → `"5"`).

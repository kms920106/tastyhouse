<!-- Parent: ../../AGENTS.md -->

# infrastructure:facebook-oauth

페이스북 로그인 **벤더 모듈**(`java-library`). `web-application`(앱 마커 제거 전에는 `application`)의 SPI `SocialOAuthClient`를 `FacebookOAuthClient`가 구현하고 `provider()`로 `SocialProvider.FACEBOOK`을 알린다. 앱이 아니라 소셜 로그인 채널 스타터 `infrastructure:oauth`가 `runtimeOnly`로 조립한다.

옛 `infrastructure:oauth`의 `facebook/` 패키지를 채널·벤더 분리(2026-09-27)로 옮겨 신설됐다. 패키지는 `external.oauth.facebook` → `com.tastyhouse.external.facebook.oauth`로 옮겼다. 이후 infrastructure 패키지 루트 통일로 `com.tastyhouse.infrastructure.facebook.oauth`가 됐고, wire DTO는 하위 패키지 `com.tastyhouse.infrastructure.facebook.oauth.dto`로 모였다. 클래스명은 그대로라 빈 이름 `facebookOAuthClient`(소비 측 `@Qualifier`)도 불변이다.

## 무엇을 소유하는가

```
com.tastyhouse.infrastructure.facebook.oauth/
├── FacebookOAuthModuleConfig.java  @Configuration(proxyBeanMethods = false) + @EnableConfigurationProperties(FacebookOAuthProperties) — 스캔 없음(앱 ModuleScanConfig가 com.tastyhouse.infrastructure를 스캔). imports 제거로 FacebookOAuthModuleAutoConfiguration에서 리네임
├── FacebookOAuthProperties.java               oauth.facebook.* (app-id, app-secret)
├── FacebookOAuthClient.java                   SocialOAuthClient 구현 — debug_token 검증 + /me 조회(graph.facebook.com), 동기 RestClient
└── dto/
    ├── FacebookTokenDebugResponse.java        wire DTO
    └── FacebookUserInfoResponse.java          wire DTO
```

`FacebookOAuthClient`는 `@Value`를 쓰지 않는다. 생성자에서 `FacebookOAuthProperties`를 받아 같은 이름의 `final` 필드(`appId`·`appSecret`)로 옮긴다.

테스트: `FacebookOAuthModuleConfigTest`(`ApplicationContextRunner`, 4건 — imports 제거로 `FacebookOAuthModuleAutoConfigurationTest`에서 리네임. `withUserConfiguration(FacebookOAuthModuleConfig.class, FacebookOAuthClient.class)`로 띄운다 — 설정 클래스가 더는 스캔하지 않으므로, 앱 스캔이 하던 클라이언트 등록을 테스트가 직접 한다) — 프로퍼티 바인딩(`bindsProperties`), 빈 이름·`provider()`(`registersClientBeanUnderQualifierName`), 키 누락 시 기동 실패(`failsStartupWhenPropertyMissing`), 환경변수 미해석 시 기동 실패(`failsStartupWhenPlaceholderUnresolved`).

## 어느 앱이 의존하는가

앱은 직접 의존하지 않는다. web-api → `infrastructure:oauth`(runtimeOnly) → 이 모듈(runtimeOnly)로 전이된다. 벤더 추가·제거 절차는 `../oauth/AGENTS.md`.

## yml — `application-facebook-oauth.yml`

```yaml
oauth:
  facebook:
    app-id: ${FACEBOOK_APP_ID}
    app-secret: ${FACEBOOK_APP_SECRET}
```

채널의 `application-oauth.yml`이 중첩 import로 로딩한다. 키 접두어는 `{채널}.{벤더}`이며 분리 전 web-api `application.yml`의 `facebook.*`에서 바뀌었다. 환경변수 이름은 그대로다.

## Dependencies

- `infrastructure:restclient` (implementation) — Boot `RestClient.Builder` customizer. Graph API 호출은 **동기 `RestClient`**다.
- `web-application` (implementation) — 구현하는 SPI(`com.tastyhouse.application.auth.port.out`)의 소유 모듈. **앱 마커 제거로 `:application` → `:web-application`으로 바뀌었다** — 소셜 로그인 SPI가 web 전용이라 web 앱 모듈로 옮겨갔기 때문이다(패키지는 그대로)

**`domain` 의존은 없다.** `exchange()`가 `app_id` 불일치를 `BusinessException(ErrorCode.SOCIAL_OAUTH_FAILED)`로 직접 던지지 않고, `SocialOAuthResult.failed(SocialOAuthFailure.ACCESS_TOKEN_REJECTED)`(둘 다 `application.auth.port.out` 소유)를 반환한다. 실패를 `BusinessException`으로 번역하는 책임은 이 어댑터가 아니라 `application.auth.service.SocialOAuthFailures`(소비 측 `*SocialLoginService` 4종이 `.orElseThrow(SocialOAuthFailures::toException)`으로 호출)로 옮겨갔다.
- `infrastructure:oauth`를 의존하지 않는다(순환 방지)

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **`@ConditionalOnProperty`를 붙이지 않는다** — 소셜 제공자는 공존한다(`../oauth/AGENTS.md`의 봉인 항목).
- **`FACEBOOK_APP_ID` 등이 없으면 기동이 실패한다.** `FacebookOAuthProperties`의 compact constructor가 값마다 null·공백·`${`(해석되지 않은 placeholder)를 검사해 `IllegalStateException`을 던지고, 바인딩 실패로 컨텍스트 생성이 중단된다. 메시지는 `oauth.facebook.app-id 설정값이 비어 있습니다` 또는 `... 환경변수가 해석되지 않았습니다: ${FACEBOOK_APP_ID}` 형태다. `@ConfigurationProperties` 바인딩은 해석하지 못한 placeholder를 문자열 그대로 넣어 그냥 두면 첫 로그인 호출에서야 실패하므로(`../tosspayments/AGENTS.md`의 `TOSS_SECRET_KEY`가 그 상태다), 분할 전 `@Value` 시절의 기동 시 실패를 이 검사로 되살렸다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### `FacebookOAuthProperties`의 기동 시 검증을 지우지 않는다

**대상**: `backend/infrastructure/facebook-oauth/src/main/java/com/tastyhouse/infrastructure/facebook/oauth/FacebookOAuthProperties.java` → compact constructor · `requireResolved`

설정 누락을 기동 시점에 드러내는 유일한 장치다. `@Validated` + `@NotBlank`로 바꾸지 않는다 — 해석되지 않은 placeholder는 `${...}` 리터럴이라 공백이 아니어서 통과한다. 검사를 지우면 누락된 환경변수가 조용히 바인딩돼 운영 배포 후 첫 로그인에서야 드러난다. 반증 테스트 `failsStartupWhenPropertyMissing`·`failsStartupWhenPlaceholderUnresolved`가 이 동작을 고정한다.

### 자바 패키지 `com.tastyhouse.external.facebook.oauth` 봉인

**대상**: `backend/infrastructure/facebook-oauth/src/main/java/com/tastyhouse/infrastructure/facebook/oauth/`

~~`com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 통째 스캔에 걸려 admin·ceo·batch 부팅이 깨진다.~~ **(번복됨 — infrastructure 패키지 루트 통일)** persistence가 자기 루트 `com.tastyhouse.infrastructure.persistence`만 스캔하게 되면서 이 제약이 사라졌다. 지금 이 모듈의 루트는 `com.tastyhouse.infrastructure.facebook.oauth`이고 main 클래스는 전부 그 아래에 있어야 한다 — `backend/infrastructure/facebook-oauth/src/test/java/com/tastyhouse/infrastructure/facebook/oauth/architecture/VendorLayerRulesTest.java` → `shouldResideInModuleRootPackage`이 강제한다. 또한 이 패키지 이름은 web-api ArchUnit `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`가 문자열로 참조하므로, 이름을 바꾸면 **규칙이 조용히 대상을 잃는다**(루트 통일 때 그 목록을 `com.tastyhouse.infrastructure.{kakao,naver,facebook,apple}.oauth..`로 교체했다).

### 쿼리는 문자열 연결이 아니라 URI 템플릿 변수로 조립한다

**대상**: `backend/infrastructure/facebook-oauth/src/main/java/com/tastyhouse/infrastructure/facebook/oauth/FacebookOAuthClient.java` → `debugToken`·`fetchUserInfo`

`/debug_token?input_token={inputToken}&access_token={accessToken}`, `/me?fields={fields}&access_token={accessToken}` 형태를 유지한다. 사용자가 제공하는 액세스 토큰에 `&`·`=`·`{` 같은 문자가 섞여 있으면 문자열 연결로는 파라미터 주입이나 500으로 이어질 수 있다 — WebClient 시절에도 있던 기존 결함이며, RestClient 전환 작업에서 함께 해소했다. 문자열 연결로 되돌리지 않는다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 페이스북은 토큰 교환 단계가 없다

**대상**: `backend/infrastructure/facebook-oauth/src/main/java/com/tastyhouse/infrastructure/facebook/oauth/FacebookOAuthClient.java` → `exchange()`

페이스북은 JS SDK가 클라이언트에서 이미 액세스 토큰을 발급하므로 교환할 것이 없다. 그래서 `exchange()`는 교환 대신 **Facebook 공식 문서가 요구하는 서버측 검증**(`debug_token`으로 토큰의 `app_id`가 우리 앱과 일치하는지 확인)을 수행하고 토큰을 그대로 돌려준다. 검증이 실패하거나 `app_id`가 다르면 `BusinessException(ErrorCode.SOCIAL_OAUTH_FAILED)`를 직접 던지지 않고 `SocialOAuthResult.failed(SocialOAuthFailure.ACCESS_TOKEN_REJECTED)`를 반환한다 — `BusinessException`으로의 번역은 `application.auth.service.SocialOAuthFailures`가 소비 측 `*SocialLoginService`의 `.orElseThrow(...)` 호출 지점에서 수행한다. 이 검증 자체(서버측 app_id 확인)는 과거 web-api `FacebookSocialLoginService#validateToken`에 있었으나, `app_id` 설정값과 `debug_token` 호출은 어댑터의 관심사이므로 어댑터로 회수했다. 응답 계약(`SOCIAL_OAUTH_FAILED`)은 무변경이다.

### 실패 메시지에 앱 시크릿이 남지 않는다

**대상**: `backend/infrastructure/facebook-oauth/src/main/java/com/tastyhouse/infrastructure/facebook/oauth/FacebookOAuthClient.java` → `debugToken`

`debug_token`의 `access_token`은 `appId|appSecret` 형태의 앱 액세스 토큰이다. RestClient의 `RestClientResponseException`/`ResourceAccessException` 메시지는 쿼리 문자열 이후를 잘라내므로, 호출이 실패해도 로그에 앱 시크릿이 노출되지 않는다. 과거 `WebClientResponseException`은 쿼리를 포함한 전체 URI를 메시지에 그대로 담아 로그에 시크릿이 노출될 수 있었다. 예외 메시지에 요청 URI를 직접 붙여 다시 던지는 코드를 추가하지 않는다.

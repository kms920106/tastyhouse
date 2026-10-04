<!-- Parent: ../../AGENTS.md -->

# infrastructure:naver-oauth

네이버 로그인 **벤더 모듈**(`java-library`). `web-application`(앱 마커 제거 전에는 `application`)의 SPI `SocialOAuthClient`를 `NaverOAuthClient`가 구현하고 `provider()`로 `SocialProvider.NAVER`를 알린다. 앱이 아니라 소셜 로그인 채널 스타터 `infrastructure:oauth`가 `runtimeOnly`로 조립한다.

옛 `infrastructure:oauth`의 `naver/` 패키지를 채널·벤더 분리(2026-09-27)로 옮겨 신설됐다. 패키지는 `external.oauth.naver` → `com.tastyhouse.external.naver.oauth`로 옮겼다. 이후 infrastructure 패키지 루트 통일로 `com.tastyhouse.infrastructure.naver.oauth`가 됐고, wire DTO는 하위 패키지 `com.tastyhouse.infrastructure.naver.oauth.dto`로 모였다. 클래스명은 그대로라 빈 이름 `naverOAuthClient`(소비 측 `@Qualifier`)도 불변이다.

## 무엇을 소유하는가

```
com.tastyhouse.infrastructure.naver.oauth/
├── NaverOAuthModuleConfig.java  @Configuration(proxyBeanMethods = false) + @EnableConfigurationProperties(NaverOAuthProperties) — 스캔 없음(앱 ModuleScanConfig가 com.tastyhouse.infrastructure를 스캔). imports 제거로 NaverOAuthModuleAutoConfiguration에서 리네임
├── NaverOAuthProperties.java               oauth.naver.* (client-id, client-secret, redirect-uri)
├── NaverOAuthClient.java                   SocialOAuthClient 구현 — 토큰 교환(nid.naver.com, state 포함) + userinfo(openapi.naver.com), 동기 RestClient
└── dto/
    ├── NaverTokenResponse.java             wire DTO
    └── NaverUserInfoResponse.java          wire DTO — response 중첩 해제·gender 정규화·birthday 분해
```

`NaverOAuthClient`는 `@Value`를 쓰지 않는다. 생성자에서 `NaverOAuthProperties`를 받아 같은 이름의 `final` 필드(`clientId`·`clientSecret`·`redirectUri`)로 옮긴다.

테스트: `NaverOAuthModuleConfigTest`(`ApplicationContextRunner`, 4건 — imports 제거로 `NaverOAuthModuleAutoConfigurationTest`에서 리네임. `withUserConfiguration(NaverOAuthModuleConfig.class, NaverOAuthClient.class)`로 띄운다 — 설정 클래스가 더는 스캔하지 않으므로, 앱 스캔이 하던 클라이언트 등록을 테스트가 직접 한다) — 프로퍼티 바인딩(`bindsProperties`), 빈 이름·`provider()`(`registersClientBeanUnderQualifierName`), 키 누락 시 기동 실패(`failsStartupWhenPropertyMissing`), 환경변수 미해석 시 기동 실패(`failsStartupWhenPlaceholderUnresolved`).

## 어느 앱이 의존하는가

앱은 직접 의존하지 않는다. web-api → `infrastructure:oauth`(runtimeOnly) → 이 모듈(runtimeOnly)로 전이된다. 벤더 추가·제거 절차는 `../oauth/AGENTS.md`.

## yml — `application-naver-oauth.yml`

```yaml
oauth:
  naver:
    client-id: ${NAVER_CLIENT_ID}
    client-secret: ${NAVER_CLIENT_SECRET}
    redirect-uri: ${NAVER_REDIRECT_URI}
```

채널의 `application-oauth.yml`이 중첩 import로 로딩한다. 키 접두어는 `{채널}.{벤더}`이며 분리 전 web-api `application.yml`의 `naver.*`에서 바뀌었다. 환경변수 이름은 그대로다.

## Dependencies

- `infrastructure:restclient` (implementation) — Boot `RestClient.Builder` customizer. 네이버 API 호출은 **동기 `RestClient`**다(호스트가 둘이라 baseUrl 없이 `build()`).
- `web-application` (implementation) — 구현하는 SPI(`com.tastyhouse.application.auth.port.out`)의 소유 모듈. **앱 마커 제거로 `:application` → `:web-application`으로 바뀌었다** — 소셜 로그인 SPI가 web 전용이라 web 앱 모듈로 옮겨갔기 때문이다(패키지는 그대로)
- **`domain` 의존 없음** — 실패 번역이 없고(`../kakao-oauth/AGENTS.md`의 설계 근거와 같은 상태), gender도 문자열로 넘긴다
- `infrastructure:oauth`를 의존하지 않는다(순환 방지)

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **`@ConditionalOnProperty`를 붙이지 않는다** — 소셜 제공자는 공존한다(`../oauth/AGENTS.md`의 봉인 항목).
- **`NAVER_CLIENT_ID` 등이 없으면 기동이 실패한다.** `NaverOAuthProperties`의 compact constructor가 값마다 null·공백·`${`(해석되지 않은 placeholder)를 검사해 `IllegalStateException`을 던지고, 바인딩 실패로 컨텍스트 생성이 중단된다. 메시지는 `oauth.naver.client-id 설정값이 비어 있습니다` 또는 `... 환경변수가 해석되지 않았습니다: ${NAVER_CLIENT_ID}` 형태다. `@ConfigurationProperties` 바인딩은 해석하지 못한 placeholder를 문자열 그대로 넣어 그냥 두면 첫 로그인 호출에서야 실패하므로(`../tosspayments/AGENTS.md`의 `TOSS_SECRET_KEY`가 그 상태다), 분할 전 `@Value` 시절의 기동 시 실패를 이 검사로 되살렸다.
- **실패 번역이 없다** — 네이버 4xx는 web-api catch-all을 타고 500으로 응답된다. 기존 동작이며 구조 리팩터링 중에 고치지 않는다(`../kakao-oauth/AGENTS.md`의 "카카오 경로에는 실패 번역이 없다").

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### `NaverOAuthProperties`의 기동 시 검증을 지우지 않는다

**대상**: `backend/infrastructure/naver-oauth/src/main/java/com/tastyhouse/infrastructure/naver/oauth/NaverOAuthProperties.java` → compact constructor · `requireResolved`

설정 누락을 기동 시점에 드러내는 유일한 장치다. `@Validated` + `@NotBlank`로 바꾸지 않는다 — 해석되지 않은 placeholder는 `${...}` 리터럴이라 공백이 아니어서 통과한다. 검사를 지우면 누락된 환경변수가 조용히 바인딩돼 운영 배포 후 첫 로그인에서야 드러난다. 반증 테스트 `failsStartupWhenPropertyMissing`·`failsStartupWhenPlaceholderUnresolved`가 이 동작을 고정한다.

### 자바 패키지 `com.tastyhouse.external.naver.oauth` 봉인

**대상**: `backend/infrastructure/naver-oauth/src/main/java/com/tastyhouse/infrastructure/naver/oauth/`

~~`com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 통째 스캔에 걸려 admin·ceo·batch 부팅이 깨진다.~~ **(번복됨 — infrastructure 패키지 루트 통일)** persistence가 자기 루트 `com.tastyhouse.infrastructure.persistence`만 스캔하게 되면서 이 제약이 사라졌다. 지금 이 모듈의 루트는 `com.tastyhouse.infrastructure.naver.oauth`이고 main 클래스는 전부 그 아래에 있어야 한다 — `backend/infrastructure/naver-oauth/src/test/java/com/tastyhouse/infrastructure/naver/oauth/architecture/VendorLayerRulesTest.java` → `shouldResideInModuleRootPackage`이 강제한다. 또한 이 패키지 이름은 web-api ArchUnit `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`가 문자열로 참조하므로, 이름을 바꾸면 **규칙이 조용히 대상을 잃는다**(루트 통일 때 그 목록을 `com.tastyhouse.infrastructure.{kakao,naver,facebook,apple}.oauth..`로 교체했다).

### 외부 응답 DTO는 도메인 enum을 반환하지 않는다

**대상**: `backend/infrastructure/naver-oauth/src/main/java/com/tastyhouse/infrastructure/naver/oauth/dto/NaverUserInfoResponse.java` → `getGender()` 정규화 매퍼

gender 매퍼는 도메인 enum `MemberGender`가 아니라 **그 상수명 문자열**(`"MALE"`/`"FEMALE"`/`null`)을 반환한다. 외부 응답 DTO가 도메인 타입을 보유하면 어댑터 → domain 역방향 결합이 생기기 때문이다. 도메인 enum 승격은 소비 측(web-api 서비스)이 `MemberGender.from(String)`으로 수행한다. 편의를 이유로 enum을 반환하도록 되돌리지 않는다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 네이버만 `state`를 쓴다 (CSRF 방어)

**대상**: `backend/infrastructure/naver-oauth/src/main/java/com/tastyhouse/infrastructure/naver/oauth/NaverOAuthClient.java` → `exchange()`·`fetchToken(String, String)`

4개 제공자 중 네이버만 인가 요청·토큰 교환에 `state`를 함께 넘겨 CSRF를 방어한다. 나머지 3종은 `SocialAuthorization`의 `state`가 `null`이다. 이 비대칭은 제공자 사양 차이이며 통일 대상이 아니다.

### 네이버 응답의 결측·형식 처리

**대상**: `backend/infrastructure/naver-oauth/src/main/java/com/tastyhouse/infrastructure/naver/oauth/dto/NaverUserInfoResponse.java`

- 프로필 응답(`GET https://openapi.naver.com/v1/nid/me`)은 사용자 정보를 **최상위 `response` 객체 안에 중첩**해 돌려준다. 다른 3종과 달리 한 겹 더 벗겨야 한다.
- `response.gender()`는 **사용자가 성별 제공에 동의하지 않으면 `null`** 이므로 반드시 가드한다(카카오 형제와 동일한 이유).
- `birthday`는 `"MM-DD"` 형식이라 월·일을 각각 잘라 쓰며(`getBirthMonth()`·`getBirthDay()`), **선행 0을 제거**해 반환한다(`"01"` → `"1"`, `"05"` → `"5"`).

<!-- Parent: ../../AGENTS.md -->

# infrastructure:kakao-oauth

카카오 로그인 **벤더 모듈**(`java-library`). `web-application`(앱 마커 제거 전에는 `application`)의 SPI `SocialOAuthClient`를 `KakaoOAuthClient`가 구현하고 `provider()`로 `SocialProvider.KAKAO`를 알린다. 앱이 아니라 소셜 로그인 채널 스타터 `infrastructure:oauth`가 `runtimeOnly`로 조립한다.

옛 `infrastructure:oauth`의 `kakao/` 패키지를 채널·벤더 분리(2026-09-27)로 옮겨 신설됐다. 패키지는 `external.oauth.kakao` → `com.tastyhouse.external.kakao.oauth`로 옮겼다. 이후 infrastructure 패키지 루트 통일로 `com.tastyhouse.infrastructure.kakao.oauth`가 됐고, wire DTO는 하위 패키지 `com.tastyhouse.infrastructure.kakao.oauth.dto`로 모였다. 클래스명은 그대로라 빈 이름 `kakaoOAuthClient`(소비 측 `@Qualifier`)도 불변이다.

## 무엇을 소유하는가

```
com.tastyhouse.infrastructure.kakao.oauth/
├── KakaoOAuthModuleConfig.java  @Configuration(proxyBeanMethods = false) + @EnableConfigurationProperties(KakaoOAuthProperties) — 스캔 없음(앱 ModuleScanConfig가 com.tastyhouse.infrastructure를 스캔). imports 제거로 KakaoOAuthModuleAutoConfiguration에서 리네임
├── KakaoOAuthProperties.java               oauth.kakao.* (client-id, redirect-uri)
├── KakaoOAuthClient.java                   SocialOAuthClient 구현 — 토큰 교환(kauth.kakao.com) + userinfo(kapi.kakao.com), 동기 RestClient
└── dto/
    ├── KakaoTokenResponse.java             wire DTO
    └── KakaoUserInfoResponse.java          wire DTO — gender 정규화 매퍼
```

`KakaoOAuthClient`는 `@Value`를 쓰지 않는다. 생성자에서 `KakaoOAuthProperties`를 받아 같은 이름의 `final` 필드(`clientId`·`redirectUri`)로 옮긴다.

테스트: `KakaoOAuthModuleConfigTest`(`ApplicationContextRunner`, 4건 — imports 제거로 `KakaoOAuthModuleAutoConfigurationTest`에서 리네임. `withUserConfiguration(KakaoOAuthModuleConfig.class, KakaoOAuthClient.class)`로 띄운다 — 설정 클래스가 더는 스캔하지 않으므로, 앱 스캔이 하던 클라이언트 등록을 테스트가 직접 한다) — 프로퍼티 바인딩(`bindsProperties`), 빈 이름·`provider()`(`registersClientBeanUnderQualifierName`), 키 누락 시 기동 실패(`failsStartupWhenPropertyMissing`), 환경변수 미해석 시 기동 실패(`failsStartupWhenPlaceholderUnresolved`).

## 어느 앱이 의존하는가

앱은 직접 의존하지 않는다. web-api → `infrastructure:oauth`(runtimeOnly) → 이 모듈(runtimeOnly)로 전이된다. 벤더 추가·제거 절차는 `../oauth/AGENTS.md`.

## yml — `application-kakao-oauth.yml`

```yaml
oauth:
  kakao:
    client-id: ${KAKAO_CLIENT_ID}
    redirect-uri: ${KAKAO_REDIRECT_URI}
```

채널의 `application-oauth.yml`이 중첩 import로 로딩한다. 키 접두어는 `{채널}.{벤더}`(`pg.tosspayments.*` 선례)이며 분리 전 web-api `application.yml`의 `kakao.*`에서 바뀌었다. 환경변수 이름은 그대로다.

## Dependencies

- `infrastructure:restclient` (implementation) — Boot `RestClient.Builder` customizer. 카카오 API 호출은 **동기 `RestClient`**다(호스트가 둘이라 baseUrl 없이 `build()`, 폼 전송은 `.body(MultiValueMap)`). 코어의 전역 customizer로 connect 5s / read 10s가 적용된다.
- `web-application` (implementation) — 구현하는 SPI(`com.tastyhouse.application.auth.port.out`)의 소유 모듈. **앱 마커 제거로 `:application` → `:web-application`으로 바뀌었다** — 소셜 로그인 SPI가 web 전용이라 web 앱 모듈로 옮겨갔기 때문이다(패키지는 그대로). adapter → port의 정상 방향이다
- **`domain` 의존 없음** — 실패를 `BusinessException`으로 번역하지 않고(아래 설계 근거), gender도 도메인 enum이 아니라 문자열로 넘기므로 필요가 없다
- `infrastructure:oauth`를 의존하지 않는다(순환 방지)

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **`@ConditionalOnProperty`를 붙이지 않는다** — 소셜 제공자는 배타 선택이 아니라 공존한다(`../oauth/AGENTS.md`의 봉인 항목).
- **`KAKAO_CLIENT_ID` 등이 없으면 기동이 실패한다.** `KakaoOAuthProperties`의 compact constructor가 값마다 null·공백·`${`(해석되지 않은 placeholder)를 검사해 `IllegalStateException`을 던지고, 바인딩 실패로 컨텍스트 생성이 중단된다. 메시지는 `oauth.kakao.client-id 설정값이 비어 있습니다` 또는 `... 환경변수가 해석되지 않았습니다: ${KAKAO_CLIENT_ID}` 형태다. `@ConfigurationProperties` 바인딩은 해석하지 못한 placeholder를 문자열 그대로 넣어 그냥 두면 첫 로그인 호출에서야 실패하므로(`../tosspayments/AGENTS.md`의 `TOSS_SECRET_KEY`가 그 상태다), 분할 전 `@Value` 시절의 기동 시 실패를 이 검사로 되살렸다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### `KakaoOAuthProperties`의 기동 시 검증을 지우지 않는다

**대상**: `backend/infrastructure/kakao-oauth/src/main/java/com/tastyhouse/infrastructure/kakao/oauth/KakaoOAuthProperties.java` → compact constructor · `requireResolved`

설정 누락을 기동 시점에 드러내는 유일한 장치다. `@Validated` + `@NotBlank`로 바꾸지 않는다 — 해석되지 않은 placeholder는 `${...}` 리터럴이라 공백이 아니어서 통과한다. 검사를 지우면 누락된 환경변수가 조용히 바인딩돼 운영 배포 후 첫 로그인에서야 드러난다. 반증 테스트 `failsStartupWhenPropertyMissing`·`failsStartupWhenPlaceholderUnresolved`가 이 동작을 고정한다.

### 자바 패키지 `com.tastyhouse.external.kakao.oauth` 봉인

**대상**: `backend/infrastructure/kakao-oauth/src/main/java/com/tastyhouse/infrastructure/kakao/oauth/`

~~`com.tastyhouse.infrastructure` 아래로 옮기면 `infrastructure:persistence`의 `PersistenceModuleAutoConfiguration`이 `@ComponentScan("com.tastyhouse.infrastructure")`로 그 트리를 통째로 스캔하므로, 의존하지도 않은 어댑터까지 빈 스캔 범위에 들어와 admin-api·ceo-api·batch-module의 부팅이 깨진다.~~ **(번복됨 — infrastructure 패키지 루트 통일)** persistence가 자기 루트 `com.tastyhouse.infrastructure.persistence`만 스캔하게 되면서 이 제약이 사라졌다. 지금 이 모듈의 루트는 `com.tastyhouse.infrastructure.kakao.oauth`이고 main 클래스는 전부 그 아래에 있어야 한다 — `backend/infrastructure/kakao-oauth/src/test/java/com/tastyhouse/infrastructure/kakao/oauth/architecture/VendorLayerRulesTest.java` → `shouldResideInModuleRootPackage`이 강제한다. 또한 이 패키지 이름은 web-api ArchUnit `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`가 문자열로 참조하므로, 이름을 바꾸면 **규칙이 조용히 대상을 잃는다**(바꿀 때는 그 규칙의 목록을 함께 고친다 — 루트 통일 때 목록을 `com.tastyhouse.infrastructure.{kakao,naver,facebook,apple}.oauth..`로 교체했다).

### 외부 응답 DTO는 도메인 enum을 반환하지 않는다

**대상**: `backend/infrastructure/kakao-oauth/src/main/java/com/tastyhouse/infrastructure/kakao/oauth/dto/KakaoUserInfoResponse.java` → `getGender()` 정규화 매퍼

gender 매퍼는 도메인 enum `MemberGender`가 아니라 **그 상수명 문자열**(`"MALE"`/`"FEMALE"`/`null`)을 반환한다. 카카오 어휘(`"male"`/`"female"`)를 여기서 정규화하고, 도메인 enum 승격은 소비 측(web-api 서비스)이 `MemberGender.from(String)`으로 수행한다. 외부 응답 DTO가 도메인 타입을 보유하면 어댑터 → domain 역방향 결합이 생기며, 이 모듈에 `domain` 의존이 없는 것도 이 규칙의 결과다. 편의를 이유로 enum을 반환하도록 되돌리지 않는다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 카카오 경로에는 실패 번역이 없다 (기존 동작 — 리팩터링 중에 "고치지" 않는다)

**대상**: `backend/infrastructure/kakao-oauth/src/main/java/com/tastyhouse/infrastructure/kakao/oauth/KakaoOAuthClient.java` → `fetchToken`·`fetchUserInfo`

애플(`APPLE_ID_TOKEN_INVALID`)·페이스북(`SOCIAL_OAUTH_FAILED`)과 달리 카카오 클라이언트는 `RestClient`의 `retrieve()` 예외를 잡아 `BusinessException`으로 번역하지 않는다. 그래서 카카오가 4xx(잘못된 인가 코드 등)를 돌려주면 `RestClientResponseException`이 그대로 올라가 web-api의 catch-all 핸들러를 타고 **500으로 응답된다.** 이것은 분할 이전부터의 기존 동작이며 응답 계약의 일부다. 모듈 분할·패키지 이동 같은 구조 리팩터링을 하면서 번역을 끼워 넣지 않는다 — 바꾸려면 프론트 분기(`code`)까지 포함한 별도 작업으로 결정한다. 네이버 경로도 같은 상태다(`../naver-oauth/AGENTS.md`).

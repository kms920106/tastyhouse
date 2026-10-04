<!-- Parent: ../../AGENTS.md -->

# infrastructure:oauth

소셜 로그인 **채널 스타터**(`java-library`). **자바 코드가 없다** — 의존 선언(`build.gradle`)과 설정 진입점(`application-oauth.yml`) 둘뿐이며, 형태는 `infrastructure:file-storage`와 같다. 제공자별 클라이언트는 벤더 모듈 4개(`infrastructure:kakao-oauth`·`naver-oauth`·`apple-oauth`·`facebook-oauth`)에 있고, 이 모듈은 그것들을 `runtimeOnly`로 조립해 web-api에 노출한다.

옛 `infrastructure:oauth`(제공자 4종을 `com.tastyhouse.external.oauth.{kakao,naver,apple,facebook}`로 통째로 담던 모듈)를 채널·벤더로 나누며(2026-09-27) 코드 없는 스타터가 됐다. `OAuthModuleAutoConfiguration`과 `META-INF/spring/...AutoConfiguration.imports`는 삭제됐고 `com.tastyhouse.external.oauth` 패키지는 더 이상 존재하지 않는다. 선례는 `infrastructure:payment` → `pg`(채널)·`tosspayments`(벤더) 분할이다.

## 구성

```
backend/infrastructure/oauth/
  build.gradle                               runtimeOnly kakao-oauth · naver-oauth · apple-oauth · facebook-oauth
  AGENTS.md
  src/main/resources/application-oauth.yml   벤더 yml 4개 중첩 import만
```

`compileJava`는 NO-SOURCE로 넘어가고 jar에는 yml만 실린다. 빈 등록은 web-api `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔이 벤더 4종을 잡아 수행하고, 각 벤더의 `{Kakao|Naver|Apple|Facebook}OAuthModuleConfig`는 `@EnableConfigurationProperties`만 한다(~~`…OAuthModuleAutoConfiguration`이 수행~~ — 번복됨, imports 제거).

## 채널 쪽 빈이 없다 — `pg`와 다른 점

`pg`는 여러 벤더를 `PgPaymentGatewayRouter` 하나로 묶어 앱에 단일 주입점을 준다. 소셜 로그인은 그럴 필요가 없다 — 소비 측(web-api 소셜 로그인 서비스 4종)이 **제공자를 이미 알고** `@Qualifier("kakaoOAuthClient")`처럼 빈 이름으로 주입하기 때문이다. 그래서 이 모듈은 라우터도 DomainConfig도 갖지 않고 조립만 한다. 벤더 클라이언트 클래스명(`KakaoOAuthClient` 등)은 분할 전과 같아 빈 이름도 그대로다.

## 벤더 추가·제거 절차

**web-api를 건드리지 않는다.**

1. `infrastructure:{vendor}-oauth` 신설 — 패키지 `com.tastyhouse.infrastructure.{vendor}.oauth`(wire DTO는 `.dto` 하위), 클라이언트가 `SocialOAuthClient`를 구현한다. 자기 auto-configuration(`@ComponentScan(자기 패키지)` + `@EnableConfigurationProperties`)과 `application-{vendor}-oauth.yml`(`oauth.{vendor}.*`)을 갖는다. `SocialProvider`에 상수를 추가하는 것은 `application` 쪽 일이다.
2. 이 모듈 `build.gradle`에 `runtimeOnly project(':infrastructure:{vendor}-oauth')` 한 줄.
3. `application-oauth.yml`에 `classpath:application-{vendor}-oauth.yml` import 한 줄.
4. `.env`에 벤더 키.
5. web-api ArchUnit `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`의 패키지 목록에 새 패키지를 추가한다.

**2번과 3번은 항상 한 쌍이다.** jar만 빼고 import를 남기면 import 대상 파일이 사라져 `ConfigDataResourceNotFoundException`으로 기동이 멈춘다. import만 빼고 jar를 남기면 벤더 yml 값이 없어 클라이언트가 빈(null) 설정으로 뜬다. 벤더를 제거할 때도 두 줄을 함께 지운다.

## yml — `application-oauth.yml`

벤더 yml 4개(`classpath:application-{kakao,naver,apple,facebook}-oauth.yml`)를 중첩 `spring.config.import`로 로딩하는 것이 전부다. **`oauth.provider` 같은 배타 선택 키를 두지 않는다** — 파일 저장(`file.provider`)·메일(`mail.provider`)·SMS(`sms.provider`)와 달리 4개 제공자가 동시에 떠야 하기 때문이다(결제의 `pg`와 같은 공존형). web-api `application.yml`에는 `classpath:application-oauth.yml` 한 줄만 있고, 분할 전 그 파일에 있던 `kakao.*`·`naver.*`·`facebook.*`·`apple.*` 블록은 삭제됐다.

## 어느 앱이 의존하는가

**web-api 하나뿐이다**(`runtimeOnly project(':infrastructure:oauth')` — 분할 전후로 이 한 줄은 불변). admin-api·ceo-api·batch-module은 이 모듈을 의존하지 않으므로 OAuth 빈이 그 컨텍스트에 아예 올라오지 않는다(챕터 02 이후로는 의존 선언이 곧 활성화다).

### ⚠️ 다른 앱에 이 모듈 의존을 추가하지 않는다 (사고 기록)

**과거 실패**: 벤더 클라이언트가 `@Value("${apple.team-id}")`처럼 프로퍼티를 직접 읽던 시절, 그 값은 web-api `application.yml`에만 있었다. 챕터 02 이후 "의존 선언 = 활성화"이므로 다른 앱이 이 모듈을 의존하는 순간 auto-configuration이 발화하고, 컨텍스트 로딩 중 `Could not resolve placeholder 'apple.team-id'`로 **부팅이 깨졌다.** 가정이 아니라 실제 이력이다 — batch-module이 이 실패를 냈다.

**지금 실패 조건**: 분할 후 벤더는 `@ConfigurationProperties` record로 값을 받고 yml도 벤더 모듈이 소유한다. `@ConfigurationProperties` 바인딩은 해석하지 못한 placeholder를 문자열 그대로 넣지만, 벤더 record 4개의 compact constructor가 null·공백·`${`를 검사해 `IllegalStateException`을 던지므로 **환경변수가 없는 앱에 이 모듈을 추가하면 여전히 기동이 실패한다**(`oauth.apple.team-id 설정값의 환경변수가 해석되지 않았습니다: ${APPLE_TEAM_ID}` 형태). 분할 전과 같은 강도의 조기 실패다. 소셜 로그인은 사용자 앱(web)에서만 일어난다.

분리 전에는 코어 `ExternalModuleConfig`가 이 패키지를 REGEX `excludeFilters`로 제외해 같은 효과를 냈고, admin/ceo/batch가 그 REGEX를 각자 복사해 유지해야 했다. 지금은 모듈 경계(= build.gradle 의존 선언)가 그 역할을 대신한다. 이 사고 기록을 남기는 이유는, 나중에 "설정을 한군데로 모으자"며 다른 앱에 이 의존을 추가하는 시도가 반복되기 쉽기 때문이다.

## SPI 규칙 — 계약은 `application`, 구현은 벤더 4모듈

> **(앱 마커 제거 후 갱신)** 계약(`auth.port.out`의 SPI)은 지금 코어 `application`이 아니라 **`web-application`** 모듈에 있다. 소셜 로그인은 web만 쓰므로 web 전용 SPI로 분류됐다. 패키지(`com.tastyhouse.application.auth.port.out`)는 그대로이고, 벤더 4모듈은 `:web-application`을 의존한다. 아래 "`application`"은 `web-application`으로 읽는다.

**소셜 로그인은 `com.tastyhouse.application.auth.port.out`의 SPI를 통해서만 사용한다.**

| 역할 | 위치 |
|---|---|
| 계약 — `SocialOAuthClient`(`provider()`/`exchange()`/`fetchProfile()`)와 중립 값 타입 `SocialProfile`·`SocialCredential`·`SocialAuthorization`·`SocialProvider` | **`web-application` 모듈**(앱 마커 제거 전에는 `application`)의 `com.tastyhouse.application.auth.port.out` |
| 구현 — 제공자별 클라이언트 4종 | 벤더 모듈의 `com.tastyhouse.infrastructure.{kakao,naver,apple,facebook}.oauth`(infrastructure 패키지 루트 통일 전 `com.tastyhouse.external.*`) |
| 조립 | 이 모듈(코드 없음) |

> **개정 이력**: 과거 이 SPI는 external 모듈 자신의 `external.oauth.spi` 패키지에 있었다(도메인 포트가 없는 공유 기술은 그 어댑터 모듈이 자기 SPI를 소유한다는 `security-module` 선례). 이후 읽기 경로 포트화·모듈 재편을 거치며 아웃바운드 계약이 전부 `application`의 `<ctx>/port/out`으로 모이면서 이 SPI도 그리로 옮겨갔고, 어댑터가 계약 소유 모듈을 의존하는 방향(adapter → port)이 됐다. **소셜 OAuth를 `domain`에 두지 않는 이유는 그대로 유효하다** — 호출부가 전부 표현·유스케이스 계층이라 도메인 서비스가 호출하는 포트가 아니므로, domain에 두면 "아무 도메인 서비스도 호출하지 않는 포트"가 된다.

**web-api는 SPI만 의존하고 제공자 패키지를 직접 import 하지 않는다.** 이것은 규율이 아니라 빌드 게이트다 — web-api의 ArchUnit `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`가 `com.tastyhouse.infrastructure.{kakao,naver,facebook,apple}.oauth..` 의존을 금지한다(web-api는 이 모듈을 `runtimeOnly`로 받으므로 컴파일 클래스패스에도 없다). **이 규칙이 패키지 문자열로 대상을 지정하므로, 벤더 패키지 이름을 바꾸면 규칙이 조용히 대상을 잃는다.** 실제로 infrastructure 패키지 루트 통일(`com.tastyhouse.external.*` → `com.tastyhouse.infrastructure.*`) 때 이 목록을 함께 교체했다.

### 2단 계약이 제공자별 흐름 차이를 흡수한다
- `exchange(SocialAuthorization) → SocialCredential` — 카카오·네이버·애플의 토큰 교환, 페이스북의 app_id 검증
- `fetchProfile(SocialCredential) → SocialProfile` — 카카오·네이버·페이스북의 userinfo 조회, 애플의 id_token 검증·추출
- `state`는 네이버만 쓰며 나머지는 `null`이다.

제공자별 세부(애플 id_token 검증, 페이스북 `debug_token`, 네이버 응답 중첩 등)는 각 벤더 AGENTS.md(`../kakao-oauth/AGENTS.md`·`../naver-oauth/AGENTS.md`·`../apple-oauth/AGENTS.md`·`../facebook-oauth/AGENTS.md`)가 소유한다.

### 외부 응답 DTO는 도메인 타입을 반환하지 않는다 (역방향 누수 금지)
`SocialProfile`은 전 필드 `String`이며, `gender`도 도메인 enum이 아니라 상수명 문자열(`"MALE"`/`"FEMALE"`/`null`)을 담는다. 과거 `KakaoUserInfoResponse`·`NaverUserInfoResponse`가 편의 매퍼에서 도메인 enum `MemberGender`를 직접 반환해 어댑터 → domain 역결합이 있었는데, 소비 측이 곧바로 `.name()`으로 되돌리고 있어 그 결합이 아무 값도 사지 못했다. 지금은 카카오의 `"male"`/`"female"` 같은 제공자 어휘를 어댑터가 `"MALE"`/`"FEMALE"`로 정규화해 넘기고, 도메인 enum 승격은 소비 측이 `MemberGender.from(String)`으로 수행한다. 그 결과 카카오·네이버 벤더 모듈은 `domain` 의존 자체가 없다. DTO별 봉인은 `../kakao-oauth/AGENTS.md`·`../naver-oauth/AGENTS.md`에 있다.

### 보존해야 하는 것 (통합 금지)
제공자별 Redis 임시토큰 저장소 4종과 **key prefix**(`kakao_temp:` 등), 제공자별 `*_TEMP_TOKEN_EXPIRED` `ErrorCode` 4종은 통합하지 않는다 — prefix를 바꾸면 배포 시점에 진행 중인 임시토큰이 전부 무효화되고, ErrorCode는 프론트가 분기할 수 있는 wire 계약이다. (저장소 계약은 이 모듈이 아니라 `security-core`의 포트, Redis 구현은 `infrastructure:redis`에 있다.) 채널·벤더 분할도 이 둘을 건드리지 않았다.

### 빈 주입
`SocialOAuthClient` 구현이 4개이므로 소비 측은 `@Qualifier("kakaoOAuthClient")`처럼 빈 이름을 명시한다. **`@Qualifier`는 필드가 아니라 생성자 파라미터에 단다** — 필드에만 달면 생성자 주입 경로에서 조용히 무시되고 주입이 빈 이름 우연 일치에만 의존하게 된다(Lombok 제거로 `lombok.copyableAnnotations`의 복사 효과가 사라진 뒤부터 해당). 빈 이름은 클래스 단순명에서 오므로 **벤더 클라이언트 클래스명을 바꾸면 `@Qualifier`가 조용히 어긋난다.**

## Dependencies

- `infrastructure:kakao-oauth`·`naver-oauth`·`apple-oauth`·`facebook-oauth` (runtimeOnly) — 벤더 4종. compileClasspath에는 없다
- `infrastructure:restclient`·`application`·`web-application`·`domain` 의존 없음 — 그것들은 벤더 모듈이 쓴다(벤더는 앱 마커 제거 후 `:web-application`을 의존)

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **벤더 auto-configuration 4개는 조건이 없어** `--debug` 리포트의 "Unconditional classes" 절에 나온다. 스캔된 클라이언트 빈은 리포트에 나오지 않는다.
- **환경변수 누락은 기동 시점에 실패한다** — 벤더 record의 compact constructor 검사 때문이다(위 사고 기록의 "지금 실패 조건", 각 벤더 AGENTS.md의 봉인 항목). 같은 `@ConfigurationProperties`를 쓰는 `../tosspayments/AGENTS.md`의 `TOSS_SECRET_KEY`는 이 검사가 없어 첫 승인 호출에서야 드러난다 — 소셜 로그인은 그 차이를 의도적으로 두지 않았다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 이 모듈에 코드를 넣지 않는다

**대상**: `backend/infrastructure/oauth/` 전체(`src/main/java` 부재가 정상)

이 모듈의 존재 이유는 "무엇을 조립하는가"를 두 파일에서 읽히게 하는 것이다(`../file-storage/AGENTS.md`와 같은 판단). 채널 쪽에서 등록할 빈도 없다 — 소비 측이 빈 이름으로 주입하므로 라우터가 필요 없다. 제공자 코드는 벤더 모듈, 계약은 `application`의 `auth.port.out`에 둔다. `OAuthModuleAutoConfiguration`을 되살리지 않는다.

### 다른 앱에 이 모듈 의존을 추가하지 않는다

**대상**: `backend/infrastructure/oauth/build.gradle`, 그리고 `backend/{admin-api,ceo-api,batch-module}/build.gradle`

위 "다른 앱에 이 모듈 의존을 추가하지 않는다" 절과 같은 사실이다. 분할 전에는 `Could not resolve placeholder 'apple.team-id'`(batch-module 실패 이력), 분할 후에는 벤더 record 검사의 `IllegalStateException`으로 — 형태만 바뀌었을 뿐 둘 다 기동 시점에 실패한다.

### 벤더 auto-configuration에 `@ConditionalOnProperty`를 붙이지 않는다

**대상**: `backend/infrastructure/{kakao,naver,apple,facebook}-oauth/src/main/java/com/tastyhouse/infrastructure/{kakao,naver,apple,facebook}/oauth/*OAuthModuleConfig.java`(imports 제거 전 `*OAuthModuleAutoConfiguration.java`) — 제목의 "벤더 auto-configuration"은 지금 이 설정과 스캔되는 벤더 클래스를 함께 가리킨다

메일·SMS·파일 저장 벤더처럼 provider 조건으로 배타 선택하면 제공자 하나만 뜨고 나머지 `@Qualifier` 주입이 `NoSuchBeanDefinitionException`으로 실패한다. 제공자 선택은 조건이 아니라 이 모듈의 `build.gradle` 조립으로 한다.

### 벤더는 이 모듈을 의존하지 않는다

**대상**: `backend/infrastructure/{kakao,naver,apple,facebook}-oauth/build.gradle`

이 모듈이 벤더를 `runtimeOnly`로 조립하므로, 벤더가 이 모듈을 의존하면 채널 ↔ 벤더 순환이다(`../pg/AGENTS.md`와 같은 판단).

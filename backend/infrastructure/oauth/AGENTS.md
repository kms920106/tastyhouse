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

`compileJava`는 NO-SOURCE로 넘어가고 jar에는 yml만 실린다. 빈 등록은 web-api `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔이 벤더 4종을 잡아 수행하고, 각 벤더의 `{Kakao|Naver|Apple|Facebook}OAuthModuleConfig`는 `@EnableConfigurationProperties`만 한다.

## 채널 쪽 빈이 없다 — `pg`와 다른 점

`pg`는 여러 벤더를 `PgPaymentGatewayRouter` 하나로 묶어 앱에 단일 주입점을 준다. 소셜 로그인도 라우터가 있다 — `web-application`의 `auth/service/SocialOAuthClientRouter`가 `List<SocialOAuthClientPort>`를 provider 키 Map으로 묶고, `SocialLoginService` 1개가 `resolve(provider)`로 고른다. 라우터는 포트를 구현하지 않고 web-application에 있으므로 이 모듈은 라우터도 DomainConfig도 갖지 않고 조립만 한다. 벤더 클라이언트 클래스명(`KakaoOAuthClient` 등)이 빈 이름을 정한다. ([결정 0050](#decision-0050))

## 벤더 추가·제거 절차

**web-api를 건드리지 않는다.**

1. `infrastructure:{vendor}-oauth` 신설 — 패키지 `com.tastyhouse.infrastructure.{vendor}.oauth`(wire DTO도 루트 패키지에 package-private으로 두고, 최상위 타입 전부에 `public`을 붙이지 않는다 — `VendorLayerRulesTest#topLevelClassesShouldNotBePublic`을 함께 복사한다), 클라이언트가 `SocialOAuthClientPort`를 구현한다. 자기 `{Vendor}OAuthModuleConfig`(`@EnableConfigurationProperties`)와 `application-{vendor}-oauth.yml`(`oauth.{vendor}.*`)을 갖는다. `SocialProvider`에 상수를 추가하는 것은 `application` 쪽 일이다.
2. 이 모듈 `build.gradle`에 `runtimeOnly project(':infrastructure:{vendor}-oauth')` 한 줄.
3. `application-oauth.yml`에 `classpath:application-{vendor}-oauth.yml` import 한 줄.
4. `.env`에 벤더 키.
5. web-api ArchUnit `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`의 패키지 목록에 새 패키지를 추가한다.

**2번과 3번은 항상 한 쌍이다.** jar만 빼고 import를 남기면 import 대상 파일이 사라져 `ConfigDataResourceNotFoundException`으로 기동이 멈춘다. import만 빼고 jar를 남기면 벤더 yml 값이 없어 클라이언트가 빈(null) 설정으로 뜬다. 벤더를 제거할 때도 두 줄을 함께 지운다.

## yml — `application-oauth.yml`

벤더 yml 4개(`classpath:application-{kakao,naver,apple,facebook}-oauth.yml`)를 중첩 `spring.config.import`로 로딩하는 것이 전부다. **`oauth.provider` 같은 배타 선택 키를 두지 않는다** — 파일 저장(`file.provider`)·메일(`mail.provider`)·SMS(`sms.provider`)와 달리 4개 제공자가 동시에 떠야 하기 때문이다(결제의 `pg`와 같은 공존형). web-api `application.yml`에는 `classpath:application-oauth.yml` 한 줄만 있고, 분할 전 그 파일에 있던 `kakao.*`·`naver.*`·`facebook.*`·`apple.*` 블록은 삭제됐다.

## 어느 앱이 의존하는가

**web-api 하나뿐이다**(`runtimeOnly project(':infrastructure:oauth')` — 분할 전후로 이 한 줄은 불변). admin-api·ceo-api·batch-module은 이 모듈을 의존하지 않으므로 OAuth 빈이 그 컨텍스트에 아예 올라오지 않는다(의존 선언이 곧 활성화다).

### ⚠️ 다른 앱에 이 모듈 의존을 추가하지 않는다 (사고 기록)

**과거 실패**: 벤더 클라이언트가 `@Value("${apple.team-id}")`처럼 프로퍼티를 직접 읽던 시절, 그 값은 web-api `application.yml`에만 있었다. "의존 선언 = 활성화"이므로 다른 앱이 이 모듈을 의존하는 순간 auto-configuration이 발화하고, 컨텍스트 로딩 중 `Could not resolve placeholder 'apple.team-id'`로 **부팅이 깨졌다.** 가정이 아니라 실제 이력이다 — batch-module이 이 실패를 냈다.

**지금 실패 조건**: 분할 후 벤더는 `@ConfigurationProperties` record로 값을 받고 yml도 벤더 모듈이 소유한다. `@ConfigurationProperties` 바인딩은 해석하지 못한 placeholder를 문자열 그대로 넣지만, 벤더 record 4개의 compact constructor가 null·공백·`${`를 검사해 `IllegalStateException`을 던지므로 **환경변수가 없는 앱에 이 모듈을 추가하면 여전히 기동이 실패한다**(`oauth.apple.team-id 설정값의 환경변수가 해석되지 않았습니다: ${APPLE_TEAM_ID}` 형태). 분할 전과 같은 강도의 조기 실패다. 소셜 로그인은 사용자 앱(web)에서만 일어난다.

분리 전에는 코어 `ExternalModuleConfig`가 이 패키지를 REGEX `excludeFilters`로 제외해 같은 효과를 냈고, admin/ceo/batch가 그 REGEX를 각자 복사해 유지해야 했다. 지금은 모듈 경계(= build.gradle 의존 선언)가 그 역할을 대신한다. 이 사고 기록을 남기는 이유는, 나중에 "설정을 한군데로 모으자"며 다른 앱에 이 의존을 추가하는 시도가 반복되기 쉽기 때문이다.

## SPI 규칙 — 계약은 `web-application`, 구현은 벤더 4모듈

계약(`com.tastyhouse.application.auth.port.out`의 SPI)은 **`web-application`** 모듈에 있다 — 소셜 로그인은 web만 쓰므로 web 전용 SPI다. 벤더 4모듈은 `:web-application`을 의존한다([결정 0036](../../AGENTS.md#decision-0036)).

**소셜 로그인은 `com.tastyhouse.application.auth.port.out`의 SPI를 통해서만 사용한다.**

| 역할 | 위치 |
|---|---|
| 계약 — `SocialOAuthClientPort`(`provider()`/`exchange()`/`fetchProfile()`)와 중립 값 타입 `SocialProfile`·`SocialCredential`·`SocialAuthorization`·`SocialProvider` | **`web-application` 모듈**의 `com.tastyhouse.application.auth.port.out` |
| 구현 — 제공자별 클라이언트 4종 | 벤더 모듈의 `com.tastyhouse.infrastructure.{kakao,naver,apple,facebook}.oauth` |
| 조립 | 이 모듈(코드 없음) |

어댑터가 계약 소유 모듈을 의존한다(adapter → port). **소셜 OAuth를 `domain`에 두지 않는다** — 호출부가 전부 표현·유스케이스 계층이라 도메인 서비스가 호출하는 포트가 아니므로, domain에 두면 "아무 도메인 서비스도 호출하지 않는 포트"가 된다.

**web-api는 SPI만 의존하고 제공자 패키지를 직접 import 하지 않는다.** 이것은 규율이 아니라 빌드 게이트다 — web-api의 ArchUnit `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`가 `com.tastyhouse.infrastructure.{kakao,naver,facebook,apple}.oauth..` 의존을 금지한다(web-api는 이 모듈을 `runtimeOnly`로 받으므로 컴파일 클래스패스에도 없다). **이 규칙이 패키지 문자열로 대상을 지정하므로, 벤더 패키지 이름을 바꾸면 규칙이 조용히 대상을 잃는다.** 실제로 infrastructure 패키지 루트 통일(`com.tastyhouse.external.*` → `com.tastyhouse.infrastructure.*`) 때 이 목록을 함께 교체했다.

### 2단 계약이 제공자별 흐름 차이를 흡수한다
- `exchange(SocialAuthorization) → SocialCredential` — 카카오·네이버·애플의 토큰 교환, 페이스북의 app_id 검증
- `fetchProfile(SocialCredential) → SocialProfile` — 카카오·네이버·페이스북의 userinfo 조회, 애플의 id_token 검증·추출
- `state`는 네이버만 쓰며 나머지는 `null`이다.

제공자별 세부(애플 id_token 검증, 페이스북 `debug_token`, 네이버 응답 중첩 등)는 각 벤더 AGENTS.md(`../kakao-oauth/AGENTS.md`·`../naver-oauth/AGENTS.md`·`../apple-oauth/AGENTS.md`·`../facebook-oauth/AGENTS.md`)가 소유한다.

### 외부 응답 DTO는 도메인 타입을 반환하지 않는다 (역방향 누수 금지)
`SocialProfile`은 전 필드 `String`이며, `gender`도 도메인 enum이 아니라 상수명 문자열(`"MALE"`/`"FEMALE"`/`null`)을 담는다. 과거 `KakaoUserInfoResponse`·`NaverUserInfoResponse`가 편의 매퍼에서 도메인 enum `MemberGender`를 직접 반환해 어댑터 → domain 역결합이 있었는데, 소비 측이 곧바로 `.name()`으로 되돌리고 있어 그 결합이 아무 값도 사지 못했다. 지금은 카카오의 `"male"`/`"female"` 같은 제공자 어휘를 어댑터가 `"MALE"`/`"FEMALE"`로 정규화해 넘기고, 도메인 enum 승격은 소비 측이 `MemberGender.from(String)`으로 수행한다. 그 결과 카카오·네이버 벤더 모듈은 `domain` 의존 자체가 없다. DTO별 봉인은 `../kakao-oauth/AGENTS.md`·`../naver-oauth/AGENTS.md`에 있다.

### 보존해야 하는 것 (통합 금지)
임시토큰 저장소는 `SocialTempTokenRepository`/`RedisSocialTempTokenRepository` 1벌이다(제공자별 4종이 아니다). **key prefix**(`kakao_temp:` 등), 제공자별 `*_TEMP_TOKEN_EXPIRED` 에러코드 4종(`WebErrorCode`)은 통합하지 않는다 — prefix를 바꾸면 배포 시점에 진행 중인 임시토큰이 전부 무효화되고, 에러코드는 프론트가 분기할 수 있는 wire 계약이다. (저장소 계약은 이 모듈이 아니라 `security-core`의 포트, Redis 구현은 `infrastructure:redis`에 있다.)

### 빈 주입
소비 측은 `@Qualifier`를 쓰지 않는다 — `SocialOAuthClientRouter`가 `provider()`로 고르므로 빈 이름은 계약이 아니다(java의 `@Qualifier("…OAuthClient")` 0건). 같은 provider를 반환하는 클라이언트가 둘이면 라우터 생성 시 `IllegalStateException`으로 기동이 실패하고, 하나가 빠져도(`SocialProvider.values()` 완전성 검사) 누락된 provider를 담은 `IllegalStateException`으로 기동이 실패한다.

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

메일·SMS·파일 저장 벤더처럼 provider 조건으로 배타 선택하면 제공자 하나만 뜨고, 라우터 생성 시 완전성 검사가 `IllegalStateException`으로 기동을 실패시킨다. 제공자 선택은 조건이 아니라 이 모듈의 `build.gradle` 조립으로 한다.

### 벤더는 이 모듈을 의존하지 않는다

**대상**: `backend/infrastructure/{kakao,naver,apple,facebook}-oauth/build.gradle`

이 모듈이 벤더를 `runtimeOnly`로 조립하므로, 벤더가 이 모듈을 의존하면 채널 ↔ 벤더 순환이다(`../pg/AGENTS.md`와 같은 판단).

## 결정 경위

이 문서가 소유한 규칙이 **왜 지금 모양이 됐는지**를 결정 단위로 적습니다. 본문은 현재 규칙만 담고, "예전에는 어땠고 왜 바뀌었는가"는 이 절에만 둡니다. 이 절은 항상 문서의 마지막 `##` 절이며, 항목은 번호 순입니다. 작성 규칙은 루트 `CLAUDE.md`의 "규칙 문서 개정 규칙" 절에 있습니다.

<a id="decision-0050"></a>

### 0050. 소셜 로그인 벤더를 SocialOAuthClientRouter로 묶고 서비스 4벌을 SocialLoginService 하나로 합친다

- 상태: 채택
- 결정 커밋: `456db1b13` (2026-10-09) — 커밋 메시지 4번째 줄 `refactor(auth)`
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0050-social-login-router.md`

#### 맥락

- **소셜 로그인 provider**: 카카오·네이버·애플·페이스북 4곳입니다. web-application의 `auth.port.out.SocialProvider` enum이 표현합니다.
- **`SocialOAuthClientPort`**: provider 서버와 대화하는 포트입니다. 구현은 벤더 모듈 4개(`infrastructure:{kakao,naver,apple,facebook}-oauth`)가 하나씩 갖습니다.
- **임시토큰**: 소셜 로그인은 했지만 가입·계정 연결이 끝나지 않은 사용자에게 주는 UUID입니다. 서버는 그 키로 provider credential을 Redis에 보관합니다.
- **`@Qualifier`**: 같은 타입 빈이 여럿일 때 빈 이름으로 하나를 고르는 Spring 애노테이션입니다.

결제(PG)는 `PgPaymentGatewayRouter`가 provider 키 Map으로 하나의 진입점을 줍니다. 소셜 로그인은 같은 문제를 `@Qualifier("kakaoOAuthClient")` 빈 이름 주입으로 풀면서, 거의 같은 서비스 4벌(825줄)과 임시토큰 저장소 8개(인터페이스 4 + Redis 구현 4)를 두고 있었습니다. provider가 늘 때마다 한 벌씩 복제해야 했고, 4벌 사이에 의도된 차이와 우연한 차이가 섞여 있어 어느 쪽이 맞는지 코드만 보고 알 수 없었습니다(`docs/tasks/social-login-router/backend.md`).

#### 이전 규칙

##### backend/CLAUDE.md — 모듈 등록 컨벤션 > 공존형 채널 — 소셜 로그인 스타터

"`pg`와 달리 라우터가 없다. 소비 측(소셜 로그인 서비스 4종)이 제공자를 이미 알고 `@Qualifier("kakaoOAuthClient")`처럼 빈 이름으로 주입하므로 단일 주입점이 필요 없다." 벤더에 `@ConditionalOnProperty`를 붙이지 않는 이유는 "붙이면 `@Qualifier` 주입이 깨진다"였습니다.

##### backend/CLAUDE.md — 소셜 로그인 SPI 규칙 · backend/infrastructure/oauth/AGENTS.md

- **보존해야 하는 것**: 제공자별 임시토큰 저장소 4종(계약은 `security-core`의 포트, Redis 구현은 `infrastructure:redis`)과 key prefix·`*_TEMP_TOKEN_EXPIRED` 에러코드 4종을 통합하지 않는다.
- **빈 주입**: `SocialOAuthClientPort` 구현이 4개이므로 소비 측은 빈 이름을 명시한다. `@Qualifier`는 필드가 아니라 생성자 파라미터에 단다(필드에만 달면 생성자 주입 경로에서 조용히 무시됨). 빈 이름은 클래스 단순명에서 오므로 벤더 클라이언트 클래스명을 바꾸면 `@Qualifier`가 조용히 어긋난다.
  - 파라미터에 다는 방식은 Lombok 제거 이후의 것이었습니다. 그 전에는 루트 `lombok.config`의 `lombok.copyableAnnotations`가 필드의 `@Qualifier`를 생성자 파라미터로 복사해 줬습니다.
- **`@ConditionalOnProperty` 금지 근거**: provider 조건으로 배타 선택하면 제공자 하나만 뜨고 나머지 `@Qualifier` 주입이 `NoSuchBeanDefinitionException`으로 실패한다.
- **reference 구현**: 소비 측은 "web-api의 소셜 로그인 서비스 4종"으로 적혀 있었습니다(이후 `web-application`으로 옮겨진 뒤 이 결정으로 합쳐짐).

##### backend/AGENTS.md — Subdirectories · Module Dependency Graph

- `infrastructure/oauth/` 행은 "라우터가 없다(소비 측이 `@Qualifier` 빈 이름으로 주입)"였고, 모듈 경계 원칙 항목도 "소셜 로그인(`infrastructure:oauth`)도 제공자 4종이 공존하지만 소비 측이 빈 이름으로 주입하므로 라우터 없이 코드 없는 스타터로 조립만 한다"였습니다.
- `security-core/` 행은 "Redis 기반 JWT 세션 저장소 6종(RefreshToken/Blacklist/소셜 임시토큰 4종)"이었습니다.

##### backend/web-api/AGENTS.md — Common Patterns

"소셜 서비스 4종은 `SocialOAuthClientPort`(`@Qualifier`로 제공자 지정)·`SocialProfile`만 알고", 임시토큰은 "제공자별 임시토큰 저장소(챕터 01 이후 포트는 `security-core`, Redis 구현은 `infrastructure:redis`)"였습니다.

##### backend/web-application/AGENTS.md 등

provider별 `auth/service/{kakao,naver,apple,facebook}/*SocialLoginService` 4벌이 각자 `@Qualifier("{provider}OAuthClient")`로 벤더를 주입받았습니다. apple·facebook-oauth 문서는 실패 번역을 "소비 측 `*SocialLoginService` 4종"이 한다고 적었습니다. security-core·redis 문서에는 `{Kakao,Naver,Facebook,Apple}TempTokenRepository` 4벌이 있었습니다.

- **application-core 덩어리 02/03a 서술**: `SocialOAuthResult<T>`를 "4개 `*SocialLoginService`"가 `.orElseThrow(SocialOAuthFailures::toException)`로 소비한다고 적었습니다. provider별 임시토큰 만료 코드 매핑 `SocialOAuthFailures#tempTokenExpired(SocialProvider)`는 이 결정에서 추가됐습니다.
- **security-core `token/`**: "인터페이스 6종" — `RefreshTokenRepository`·`BlacklistRepository` + `KakaoTempTokenRepository`·`NaverTempTokenRepository`·`AppleTempTokenRepository`·`FacebookTempTokenRepository`. 키 접두사·TTL 정책은 전부 어댑터가 소유했습니다. Apple만 id_token을 저장하는 것은 `AppleTempTokenRepository.save(appleTempToken, appleIdToken)` 시그니처로 표현됐습니다.
- **redis `token/`**: "토큰 저장소 어댑터 6종" — `RedisRefreshTokenRepository`·`RedisBlacklistRepository` + `RedisKakaoTempTokenRepository`(`kakao_temp:{tempToken}` → kakaoAccessToken)·`RedisNaverTempTokenRepository`(`naver_temp:`)·`RedisFacebookTempTokenRepository`(`facebook_temp:`)·`RedisAppleTempTokenRepository`(`apple_temp:` → appleIdToken), 소셜 4종 TTL은 모두 10분.
- **security-module**: Redis 위임 표와 Dependencies가 "토큰 저장소 6종(`RefreshToken`·`Blacklist`·소셜 임시토큰 4종)"으로 적었습니다.

##### backend/application-core/AGENTS.md — 앱 모듈 경계 가드

`ServiceContextBoundaryTest`의 `EXCLUDED_COLLABORATORS`는 24개였고 그중 `*SocialLoginService` 4개가 들어 있었습니다. 구성은 Executor 6 · `*SocialLoginService` 4 · Validator 7 · Reader 1 · `OwnedShopIdProvider` · `MemberAuthService` · `CredentialLoginService` · `PhoneLoginService` · `AuthPasswordResetService` · `AdminDongSyncRunner`였습니다.

#### 결정

| 항목 | before | after |
|---|---|---|
| 벤더 선택 | 서비스 4벌이 `@Qualifier("{provider}OAuthClient")`로 주입 | `web-application`의 `auth/service/SocialOAuthClientRouter`(package-private `@Component`)가 `List<SocialOAuthClientPort>`를 `SocialProvider` 키 `EnumMap`으로 묶고 `resolve(provider)` 제공 |
| 소셜 로그인 서비스 | provider별 `*SocialLoginService` 4벌 | `SocialLoginService` 1개. `Member{Kakao,Naver,Apple,Facebook}LoginService`·`MemberSocialSignUpService`·`MemberSocialAccountLinkService`는 위임만 |
| 임시토큰 저장소 | 인터페이스 4 + Redis 구현 4 | security-core `SocialTempTokenRepository` + redis `RedisSocialTempTokenRepository` 1벌(`SocialTempTokenProvider`로 구분) |
| 등록 완전성 | 빈 누락 시 `NoSuchBeanDefinitionException` | 같은 provider 중복·`SocialProvider.values()` 누락이면 라우터 생성 시 `IllegalStateException`(기동 실패) |
| `@Qualifier("…OAuthClient")` | 사용 | 0건 — 빈 이름은 더 이상 계약이 아님 |
| `EXCLUDED_COLLABORATORS` | 24개 | 21개(4벌 제거, `SocialLoginService` 추가) |

#### 근거

- provider마다 복제되던 서비스·저장소를 한 벌로 줄여, provider 추가 비용과 4벌 간 드리프트를 없앱니다.
- 4벌을 provider 이름을 정규화한 뒤 6쌍 diff로 조사해, 의도된 차이만 명시적으로 남겼습니다(네이버 `state`, 페이스북은 code 대신 access token 입력, 페이스북은 닉네임 자리에 이름 저장, provider별 만료 에러코드). 통일하면 동작이 바뀌는 차이는 보존했습니다.
- 라우터는 포트를 구현하지 않습니다. PG와 달리 소비처가 `auth.service` 하나라 라우팅 포트를 따로 두지 않았습니다.

#### 결과·감수한 손실

- API·Redis 키·TTL은 불변입니다. 임시토큰 key prefix(`kakao_temp:` 등)와 provider별 `*_TEMP_TOKEN_EXPIRED` 에러코드 4종(`WebErrorCode`)은 통합하지 않고 유지합니다 — prefix를 바꾸면 배포 시점에 진행 중인 임시토큰이 전부 무효화되고, 에러코드는 wire 계약입니다.
- 벤더에 `@ConditionalOnProperty`를 붙이지 않는 규칙은 유지됩니다. 근거만 "`@Qualifier` 주입이 깨진다"에서 "라우터 완전성 검사가 기동을 실패시킨다"로 바뀌었습니다.
- 채널 모듈 `infrastructure:oauth`는 라우터가 web-application에 있으므로 여전히 코드 없는 스타터입니다. 벤더 클라이언트 클래스명은 그대로라 빈 이름도 같습니다.
- `SocialLoginService`도 `member.service.MemberRegistrationService`·`member.port.out.write.*`를 직접 참조하므로 4벌이 받던 `EXCLUDED_COLLABORATORS` 제외를 승계했습니다. `SocialOAuthClientRouter`는 auth 타입만 참조해 경계 검사를 통과하므로 넣지 않았습니다.
- 임시토큰 Redis 키 접두사는 이제 어댑터가 아니라 security-core enum `SocialTempTokenProvider#keyPrefix()`가 리터럴로 소유합니다(`name().toLowerCase()`로 만들지 않음). 이 enum은 security-core가 `SocialProvider`를 볼 수 없어 신설했고, 상수명·순서 일치는 `EnumCodeConstantsTest#socialTempTokenProviderMatchesSocialProvider`가, 키 4개와 TTL `10, MINUTES`는 `RedisSocialTempTokenRepositoryTest`가 지킵니다.
- 이 덩어리는 `SocialOAuthClient` → `SocialOAuthClientPort` 개명(같은 커밋 2번째 줄 `refactor(port)`)을 전제로 합니다.

# ADR-0050: 소셜 로그인 벤더를 SocialOAuthClientRouter로 묶고 서비스 4벌을 SocialLoginService 하나로 합친다

- 상태: 채택
- 결정 커밋: `456db1b13` (2026-10-09) — 커밋 메시지 4번째 줄 `refactor(auth)`
- 대체한 결정: —

## 맥락

- **소셜 로그인 provider**: 카카오·네이버·애플·페이스북 4곳입니다. web-application의 `auth.port.out.SocialProvider` enum이 표현합니다.
- **`SocialOAuthClientPort`**: provider 서버와 대화하는 포트입니다. 구현은 벤더 모듈 4개(`infrastructure:{kakao,naver,apple,facebook}-oauth`)가 하나씩 갖습니다.
- **임시토큰**: 소셜 로그인은 했지만 가입·계정 연결이 끝나지 않은 사용자에게 주는 UUID입니다. 서버는 그 키로 provider credential을 Redis에 보관합니다.
- **`@Qualifier`**: 같은 타입 빈이 여럿일 때 빈 이름으로 하나를 고르는 Spring 애노테이션입니다.

결제(PG)는 `PgPaymentGatewayRouter`가 provider 키 Map으로 하나의 진입점을 줍니다. 소셜 로그인은 같은 문제를 `@Qualifier("kakaoOAuthClient")` 빈 이름 주입으로 풀면서, 거의 같은 서비스 4벌(825줄)과 임시토큰 저장소 8개(인터페이스 4 + Redis 구현 4)를 두고 있었습니다. provider가 늘 때마다 한 벌씩 복제해야 했고, 4벌 사이에 의도된 차이와 우연한 차이가 섞여 있어 어느 쪽이 맞는지 코드만 보고 알 수 없었습니다(`docs/tasks/social-login-router/backend.md`).

## 이전 규칙

### backend/CLAUDE.md — 모듈 등록 컨벤션 > 공존형 채널 — 소셜 로그인 스타터

"`pg`와 달리 라우터가 없다. 소비 측(소셜 로그인 서비스 4종)이 제공자를 이미 알고 `@Qualifier("kakaoOAuthClient")`처럼 빈 이름으로 주입하므로 단일 주입점이 필요 없다." 벤더에 `@ConditionalOnProperty`를 붙이지 않는 이유는 "붙이면 `@Qualifier` 주입이 깨진다"였습니다.

### backend/CLAUDE.md — 소셜 로그인 SPI 규칙 · backend/infrastructure/oauth/AGENTS.md

- **보존해야 하는 것**: 제공자별 임시토큰 저장소 4종(계약은 `security-core`의 포트, Redis 구현은 `infrastructure:redis`)과 key prefix·`*_TEMP_TOKEN_EXPIRED` 에러코드 4종을 통합하지 않는다.
- **빈 주입**: `SocialOAuthClientPort` 구현이 4개이므로 소비 측은 빈 이름을 명시한다. `@Qualifier`는 필드가 아니라 생성자 파라미터에 단다(필드에만 달면 생성자 주입 경로에서 조용히 무시됨). 빈 이름은 클래스 단순명에서 오므로 벤더 클라이언트 클래스명을 바꾸면 `@Qualifier`가 조용히 어긋난다.
  - 파라미터에 다는 방식은 Lombok 제거 이후의 것이었습니다. 그 전에는 루트 `lombok.config`의 `lombok.copyableAnnotations`가 필드의 `@Qualifier`를 생성자 파라미터로 복사해 줬습니다.
- **`@ConditionalOnProperty` 금지 근거**: provider 조건으로 배타 선택하면 제공자 하나만 뜨고 나머지 `@Qualifier` 주입이 `NoSuchBeanDefinitionException`으로 실패한다.
- **reference 구현**: 소비 측은 "web-api의 소셜 로그인 서비스 4종"으로 적혀 있었습니다(이후 `web-application`으로 옮겨진 뒤 이 결정으로 합쳐짐).

### backend/AGENTS.md — Subdirectories · Module Dependency Graph

- `infrastructure/oauth/` 행은 "라우터가 없다(소비 측이 `@Qualifier` 빈 이름으로 주입)"였고, 모듈 경계 원칙 항목도 "소셜 로그인(`infrastructure:oauth`)도 제공자 4종이 공존하지만 소비 측이 빈 이름으로 주입하므로 라우터 없이 코드 없는 스타터로 조립만 한다"였습니다.
- `security-core/` 행은 "Redis 기반 JWT 세션 저장소 6종(RefreshToken/Blacklist/소셜 임시토큰 4종)"이었습니다.

### backend/web-api/AGENTS.md — Common Patterns

"소셜 서비스 4종은 `SocialOAuthClientPort`(`@Qualifier`로 제공자 지정)·`SocialProfile`만 알고", 임시토큰은 "제공자별 임시토큰 저장소(챕터 01 이후 포트는 `security-core`, Redis 구현은 `infrastructure:redis`)"였습니다.

### backend/web-application/AGENTS.md 등

provider별 `auth/service/{kakao,naver,apple,facebook}/*SocialLoginService` 4벌이 각자 `@Qualifier("{provider}OAuthClient")`로 벤더를 주입받았습니다. apple·facebook-oauth 문서는 실패 번역을 "소비 측 `*SocialLoginService` 4종"이 한다고 적었습니다. security-core·redis 문서에는 `{Kakao,Naver,Facebook,Apple}TempTokenRepository` 4벌이 있었습니다.

- **application-core 덩어리 02/03a 서술**: `SocialOAuthResult<T>`를 "4개 `*SocialLoginService`"가 `.orElseThrow(SocialOAuthFailures::toException)`로 소비한다고 적었습니다. provider별 임시토큰 만료 코드 매핑 `SocialOAuthFailures#tempTokenExpired(SocialProvider)`는 이 결정에서 추가됐습니다.
- **security-core `token/`**: "인터페이스 6종" — `RefreshTokenRepository`·`BlacklistRepository` + `KakaoTempTokenRepository`·`NaverTempTokenRepository`·`AppleTempTokenRepository`·`FacebookTempTokenRepository`. 키 접두사·TTL 정책은 전부 어댑터가 소유했습니다. Apple만 id_token을 저장하는 것은 `AppleTempTokenRepository.save(appleTempToken, appleIdToken)` 시그니처로 표현됐습니다.
- **redis `token/`**: "토큰 저장소 어댑터 6종" — `RedisRefreshTokenRepository`·`RedisBlacklistRepository` + `RedisKakaoTempTokenRepository`(`kakao_temp:{tempToken}` → kakaoAccessToken)·`RedisNaverTempTokenRepository`(`naver_temp:`)·`RedisFacebookTempTokenRepository`(`facebook_temp:`)·`RedisAppleTempTokenRepository`(`apple_temp:` → appleIdToken), 소셜 4종 TTL은 모두 10분.
- **security-module**: Redis 위임 표와 Dependencies가 "토큰 저장소 6종(`RefreshToken`·`Blacklist`·소셜 임시토큰 4종)"으로 적었습니다.

### backend/application-core/AGENTS.md — 앱 모듈 경계 가드

`ServiceContextBoundaryTest`의 `EXCLUDED_COLLABORATORS`는 24개였고 그중 `*SocialLoginService` 4개가 들어 있었습니다. 구성은 Executor 6 · `*SocialLoginService` 4 · Validator 7 · Reader 1 · `OwnedShopIdProvider` · `MemberAuthService` · `CredentialLoginService` · `PhoneLoginService` · `AuthPasswordResetService` · `AdminDongSyncRunner`였습니다.

## 결정

| 항목 | before | after |
|---|---|---|
| 벤더 선택 | 서비스 4벌이 `@Qualifier("{provider}OAuthClient")`로 주입 | `web-application`의 `auth/service/SocialOAuthClientRouter`(package-private `@Component`)가 `List<SocialOAuthClientPort>`를 `SocialProvider` 키 `EnumMap`으로 묶고 `resolve(provider)` 제공 |
| 소셜 로그인 서비스 | provider별 `*SocialLoginService` 4벌 | `SocialLoginService` 1개. `Member{Kakao,Naver,Apple,Facebook}LoginService`·`MemberSocialSignUpService`·`MemberSocialAccountLinkService`는 위임만 |
| 임시토큰 저장소 | 인터페이스 4 + Redis 구현 4 | security-core `SocialTempTokenRepository` + redis `RedisSocialTempTokenRepository` 1벌(`SocialTempTokenProvider`로 구분) |
| 등록 완전성 | 빈 누락 시 `NoSuchBeanDefinitionException` | 같은 provider 중복·`SocialProvider.values()` 누락이면 라우터 생성 시 `IllegalStateException`(기동 실패) |
| `@Qualifier("…OAuthClient")` | 사용 | 0건 — 빈 이름은 더 이상 계약이 아님 |
| `EXCLUDED_COLLABORATORS` | 24개 | 21개(4벌 제거, `SocialLoginService` 추가) |

## 근거

- provider마다 복제되던 서비스·저장소를 한 벌로 줄여, provider 추가 비용과 4벌 간 드리프트를 없앱니다.
- 4벌을 provider 이름을 정규화한 뒤 6쌍 diff로 조사해, 의도된 차이만 명시적으로 남겼습니다(네이버 `state`, 페이스북은 code 대신 access token 입력, 페이스북은 닉네임 자리에 이름 저장, provider별 만료 에러코드). 통일하면 동작이 바뀌는 차이는 보존했습니다.
- 라우터는 포트를 구현하지 않습니다. PG와 달리 소비처가 `auth.service` 하나라 라우팅 포트를 따로 두지 않았습니다.

## 결과·감수한 손실

- API·Redis 키·TTL은 불변입니다. 임시토큰 key prefix(`kakao_temp:` 등)와 provider별 `*_TEMP_TOKEN_EXPIRED` 에러코드 4종(`WebErrorCode`)은 통합하지 않고 유지합니다 — prefix를 바꾸면 배포 시점에 진행 중인 임시토큰이 전부 무효화되고, 에러코드는 wire 계약입니다.
- 벤더에 `@ConditionalOnProperty`를 붙이지 않는 규칙은 유지됩니다. 근거만 "`@Qualifier` 주입이 깨진다"에서 "라우터 완전성 검사가 기동을 실패시킨다"로 바뀌었습니다.
- 채널 모듈 `infrastructure:oauth`는 라우터가 web-application에 있으므로 여전히 코드 없는 스타터입니다. 벤더 클라이언트 클래스명은 그대로라 빈 이름도 같습니다.
- `SocialLoginService`도 `member.service.MemberRegistrationService`·`member.port.out.write.*`를 직접 참조하므로 4벌이 받던 `EXCLUDED_COLLABORATORS` 제외를 승계했습니다. `SocialOAuthClientRouter`는 auth 타입만 참조해 경계 검사를 통과하므로 넣지 않았습니다.
- 임시토큰 Redis 키 접두사는 이제 어댑터가 아니라 security-core enum `SocialTempTokenProvider#keyPrefix()`가 리터럴로 소유합니다(`name().toLowerCase()`로 만들지 않음). 이 enum은 security-core가 `SocialProvider`를 볼 수 없어 신설했고, 상수명·순서 일치는 `EnumCodeConstantsTest#socialTempTokenProviderMatchesSocialProvider`가, 키 4개와 TTL `10, MINUTES`는 `RedisSocialTempTokenRepositoryTest`가 지킵니다.
- 이 덩어리는 `SocialOAuthClient` → `SocialOAuthClientPort` 개명(같은 커밋 2번째 줄 `refactor(port)`)을 전제로 합니다.

## 영향받은 문서

- `backend/CLAUDE.md` · 소셜 로그인 SPI 규칙 (`application`의 `auth.port.out`) · 보존해야 하는 것·빈 주입·reference 구현
- `backend/CLAUDE.md` · 모듈 등록 컨벤션 > 벤더 선택은 앱이 아니라 스타터 모듈이 한다 (챕터 03) · 공존형 채널 소셜 로그인 서술
- `backend/infrastructure/oauth/AGENTS.md` · 채널 쪽 빈이 없다 — `pg`와 다른 점 / SPI 규칙 > 보존해야 하는 것 (통합 금지) / 빈 주입 / 봉인·가드 목록 > 벤더 auto-configuration에 `@ConditionalOnProperty`를 붙이지 않는다
- `backend/web-application/AGENTS.md` · 무엇이 여기 사는가 · 라우터 + `SocialLoginService` 1벌
- `backend/web-api/AGENTS.md` · Common Patterns · 소셜 로그인 SPI 사용
- `backend/infrastructure/{apple,facebook}-oauth/AGENTS.md` · Dependencies / 페이스북은 토큰 교환 단계가 없다 · 번역은 `SocialLoginService`의 `.orElseThrow`
- `backend/security-core/AGENTS.md`, `backend/infrastructure/redis/AGENTS.md` · "소셜 임시토큰 저장소 — 1회용 토큰의 수명"(security-core) / "토큰 저장소 어댑터 3종의 키 형식"(redis) · 소셜 임시토큰 저장소 서술
- `backend/application-core/AGENTS.md` · "앱 모듈 경계 가드 — 마커를 되살리지 않고, 다른 앱 모듈을 클래스패스에 올리지 않는다 (앱 마커 제거)" · `EXCLUDED_COLLABORATORS` 근거

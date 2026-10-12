<!-- Parent: ../../AGENTS.md -->

# infrastructure:redis

Redis 연결·템플릿과 **rate limit 카운터**, **토큰 저장소 어댑터 3종**을 소유하는 인프라 모듈(`java-library`). 이 모듈이 구현하는 계약(토큰 저장소 포트 3종 + `RateLimitCounterPort`)은 전부 `security-core`가 소유하며, 내부 모듈 의존은 `security-core` 하나뿐이다. rate limit의 표현 관심사(`@RateLimit`·`RateLimitAspect`·`RateLimitException`)는 `api-common-module`에 있다.

## 이 모듈이 따로 있는 이유

Redis는 보안 관심사가 아니라 **인프라 기술**이고, rate limiting은 도메인에 대응 개념이 아예 없는 순수 인프라 관심사다. 보안 모듈이 연결 설정과 템플릿 빈까지 소유하면 Redis를 쓰고 싶은 다른 관심사가 전부 `security-module`을 의존해야 한다. 그래서 Redis는 `infrastructure:jpa`·`infrastructure:restclient` 등과 나란히 driven 어댑터 모듈 하나로 둔다. `infrastructure` 아래 모듈 전체 구성은 루트 `CLAUDE.md`의 "리포지토리 구조" 절이 정본이다.

**`settings.gradle`에서 `include 'infrastructure:redis'`로 선언한다.** 중첩 프로젝트 컨테이너 `:infrastructure`는 소스가 없는 빈 프로젝트이므로, 루트 `build.gradle`의 `subprojects` 일괄 설정이 이 컨테이너에 `bootJar`를 걸지 않도록 주의한다.

## 패키지 구조

```
com.tastyhouse.infrastructure.redis/
├── RedisModuleConfig.java        @EnableConfigurationProperties(RedisTokenStoreProperties)만 — 스캔 없음(앱 ModuleScanConfig가 com.tastyhouse.infrastructure를 스캔)
├── RedisConfig.java              StringRedisTemplate 빈 (key/value StringRedisSerializer)
├── ratelimit/
│   └── RedisRateLimitCounter.java  security-core의 RateLimitCounterPort 구현 — 순수 Redis Lua (INCR + PEXPIRE 원자 실행)
└── token/                          security-core의 토큰 저장소 포트 3종 구현
    ├── RedisTokenStoreProperties.java   @ConfigurationProperties("security.token-store") — keyPrefix
    ├── RedisRefreshTokenRepository.java  {keyPrefix}rt:{username}
    ├── RedisBlacklistRepository.java     {keyPrefix}bl:{accessToken}
    └── RedisSocialTempTokenRepository.java  {provider.keyPrefix()}{tempToken} (kakao_temp: 등)
```

**계약은 `security-core`, 구현은 이 모듈이다.** 토큰 저장소 포트 3종과 `RateLimitCounterPort`가 모두 같은 구조다. 계약 소유 모듈(`security-core`)이 이 모듈을 의존하면 "core가 구체 인프라를 의존하는" 역방향 간선이 되고, 그 전이 사슬을 타고 Redis를 쓰지 않는 batch-module에도 Redis 빈과 springdoc·starter-web이 실린다. 지금 batch runtimeClasspath에는 redis·api-common·springdoc이 **없다**([ADR-0019](../../../docs/adr/0019-token-store-port-inversion.md)).

### 키 접두사 프로퍼티

| 앱 | `security.token-store.key-prefix` | refresh 키 | blacklist 키 |
|---|---|---|---|
| web-api | `""` (기본값, yml 생략) | `rt:{username}` | `bl:{accessToken}` |
| admin-api | `admin:` | `admin:rt:{username}` | `admin:bl:{accessToken}` |
| ceo-api | `ceo:` | `ceo:rt:{username}` | `ceo:bl:{accessToken}` |

**"모듈이 소비하는 설정은 모듈이 소유한다"** 컨벤션에 따라 `RedisTokenStoreProperties`가 이 모듈에 있고, `RedisModuleConfig`가 `@EnableConfigurationProperties`로 등록한다. 값은 각 앱 `application.yml`이 준다.

**이 표는 불변 계약이다.** 접두사가 어긋나도 예외가 나지 않고 기존 세션만 조용히 무효화되므로(콜론 누락 `admin` → `adminrt:`), `RedisRefreshTokenRepositoryTest`·`RedisBlacklistRepositoryTest`의 고정값 단정이 유일한 자동 방어선이다. 소셜 임시토큰은 앱 간 공유되지 않아 이 프로퍼티를 받지 않고, 접두사를 `security-core` enum `SocialTempTokenProvider#keyPrefix()`가 준다.

**`ratelimit` 패키지에는 카운터 하나만 있다.** 키 조립(클라이언트 IP·요청 필드 해석)은 HTTP 어댑터 관심사라 `api-common-module`의 `RateLimitAspect`가 소유한다 — 인프라가 그것을 들면 서블릿 스택을 의존하게 된다. 계약 `RateLimitCounterPort`는 서블릿-프리 `security-core`(`com.tastyhouse.security.ratelimit`)에 있어, 방향은 **`infrastructure:redis` → `security-core` ← `api-common-module`** 이다 — 구현과 표현이 main 클래스패스에서 서로를 모른다. 근거는 `security-core/AGENTS.md`의 "rate limit 카운터 계약을 여기 둔 이유"([ADR-0023](../../../docs/adr/0023-rate-limit-port-in-security-core.md)). Redis 카운터 키 접두사는 호출부의 `@RateLimit(keyPrefix=...)`가 결정한다.

## yml — `application-redis.yml` (챕터 05 §5b)

이 모듈은 Redis 관련 **코드**뿐 아니라 **연결 설정**도 소유한다. `spring.data.redis.{host,port,password}`를 담으며 셋 다 환경변수로 덮어쓸 수 있다(`REDIS_HOST`·`REDIS_PORT`·`REDIS_PASSWORD`, 기본값 `localhost:6379`/빈 비밀번호).

**소유 근거는 소비 지점이다.** 이 설정으로 만들어지는 `RedisConnectionFactory`를 주입받는 것이 이 모듈의 `RedisConfig`(`StringRedisTemplate`)이므로, **"모듈이 소비하는 설정은 모듈이 소유한다"** 는 컨벤션(`application-persistence.yml`·`application-file-storage.yml`의 선례)이 그대로 적용된다. `security-module`에는 Redis 설정 파일이 없다.

로딩은 web/admin/ceo-api 3개 앱의 `application.yml`이 `classpath:application-redis.yml`을 `spring.config.import`로 수행한다. batch-module은 대상이 아니다.

## Dependencies

### Internal
- `security-core` (implementation) — `token` 패키지가 구현하는 토큰 저장소 포트 3종(`RefreshTokenRepository`·`BlacklistRepository`·`SocialTempTokenRepository`)과 `ratelimit` 패키지가 구현하는 `RateLimitCounterPort`(`com.tastyhouse.security.ratelimit`)의 소유 모듈. 어댑터 → 계약 방향이며, `infrastructure:jpa`가 포트 소유 모듈(`application-core`)을 의존하는 선례와 동형이다. **이 모듈의 유일한 내부 의존이다.** `./gradlew :infrastructure:redis:dependencies --configuration compileClasspath | grep -cE 'project :api-common-module|starter-web'`가 0이어야 한다(2026-09-27 실측 0).

**`domain`에 의존하지 않는다.** rate limiting은 domain에 대응 개념이 없는 순수 인프라 관심사라서 `RateLimitException`도 도메인 에러코드에 결합하지 않는다. HTTP 응답 조립은 각 api 모듈의 `GlobalExceptionHandler`가 `com.tastyhouse.apicommon.exception.ApiErrorCode#RATE_LIMIT_EXCEEDED`로 직접 수행한다.

이것이 `infrastructure:jpa`와의 결정적 차이다 — jpa는 도메인 모델을 저장하는 어댑터라 `domain`을 `implementation`으로 의존하지만, redis는 domain 포트가 없는 기술이라 domain을 아예 모른다.

### External
- `spring-boot-starter-data-redis` (**api**) — `StringRedisTemplate`·`RedisConnectionFactory`. `api`로 두는 이유는 소비 모듈의 시그니처 노출이 아니라 **이 모듈의 어댑터가 그 타입을 쓰기 때문**이다. 앱과 `security-core`의 compileClasspath에는 Redis 타입이 없고, `runtimeOnly project(':infrastructure:redis')`가 4앱 중 3앱의 **유일한** Redis 선언이다(batch는 선언 자체가 없다)
- `api-common-module`은 main·test 어느 쪽에서도 선언하지 않는다 — redis → api-common 방향은 테스트 클래스패스에도 없다.
- `archunit-junit5` (**testImplementation**) — 패키지 루트 가드 `architecture/PackageRootTest`용
- `spring-boot-starter-aop`·`spring-boot-starter-web`는 **선언하지 않는다** — `@Aspect`와 `HttpServletRequest` 기반 IP 해석은 `api-common-module` 소관이다. 이 모듈에는 Redis Lua 카운터만 있어 서블릿·AOP 스택을 알지 않는다.

## security-core / security-module과의 관계

**토큰 저장소의 계약은 `security-core`, 구현은 이 모듈의 `token` 패키지다.** 관심사의 귀속과 기술 구현의 귀속은 다른 층위다 — 보안 관심사인 것은 *계약*(무엇을 저장하고 언제 무효화하는가)이고, `StringRedisTemplate`으로 키를 조립하는 것은 *구현*이다. 계약을 `security-core`에 두고 구현을 여기 두면 두 귀속이 모두 지켜지고 `security-core → infrastructure:redis` 역방향 간선이 생기지 않는다. Redis key prefix(`rt:`/`bl:`/`admin:rt:`/`admin:bl:`/`kakao_temp:` 등)는 운영 키 공간이라 바꾸지 않는다.

- `security-core` — 이 모듈이 `implementation`으로 의존한다(어댑터 → 계약). 토큰 저장소 포트 3종과 `RateLimitCounterPort`의 소유자.
- `security-module` — `infrastructure:redis`를 선언하지 않는다. 서블릿 결합 타입만 갖고, `JwtAuthenticationFilter`는 `security-core`의 `BlacklistRepository` **포트**를 받으므로 Redis 타입을 알지 않는다.

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **빈 배선**: 이 모듈의 빈(`RedisConfig`·`RedisRateLimitCounter`·토큰 저장소 어댑터 3종)은 web·admin·ceo 부트스트랩의 중첩 `ModuleScanConfig`가 `com.tastyhouse.infrastructure`를 문자열로 스캔해 등록하고, `RedisModuleConfig`는 `@EnableConfigurationProperties`만 한다. aspect 빈은 `api-common-module`의 `ApiCommonRateLimitConfig`가 등록한다(빈 존재 조건 없음 — `apicommon.ratelimit`을 스캔하는 앱은 전부 이 모듈을 클래스패스에 갖는다). 클래스패스에 올라오면 활성화된다. 이 경로를 건드렸으면 한도 초과 호출로 429를 실제 확인한다([ADR-0038](../../../docs/adr/0038-autoconfiguration-imports-removal.md)).
- **Redis를 쓰는 새 관심사는 `security-module`이 아니라 이 모듈을 의존한다.** 그것이 이 모듈이 따로 있는 이유다.
- **다른 모듈의 계약을 구현할 때는 그 모듈을 이 모듈이 의존한다**(어댑터 → 계약). 반대로 계약 소유 모듈이 이 모듈을 의존하게 만들면 역방향 간선이 생긴다 — 그 회귀는 컴파일·기동 모두 성공하고 **batch 배포 산출물이 조용히 불어나는** 형태로만 드러난다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 패키지 루트 `com.tastyhouse.infrastructure.redis` — 다른 모듈의 스캔과 겹치지 않는다

**대상**: `backend/infrastructure/redis/src/test/java/com/tastyhouse/infrastructure/redis/architecture/PackageRootTest.java` → `shouldResideInModuleRootPackage`

이 모듈의 main 클래스는 전부 `com.tastyhouse.infrastructure.redis` 아래에 있어야 한다(`PackageRootTest`, ArchUnit은 `testImplementation`으로만 선언). 모든 infrastructure 모듈이 자기 루트 `com.tastyhouse.infrastructure.{모듈}`만 갖고, 스캔은 앱 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 한 줄이 하므로 같은 클래스는 한 번만 등록된다([ADR-0037](../../../docs/adr/0037-infrastructure-package-root.md)).

- 이 모듈의 클래스를 `com.tastyhouse.infrastructure` 바로 아래 등 루트 밖으로 옮기지 않는다 — `PackageRootTest`가 실패하고, 다른 모듈의 패키지와 겹칠 수 있다.

### `stringRedisTemplate`은 사용자 설정으로 등록해 Boot 기본 빈보다 먼저 정의되게 한다

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/RedisConfig.java` → `stringRedisTemplate` · `backend/infrastructure/redis/src/test/java/com/tastyhouse/infrastructure/redis/RedisModuleConfigTest.java` → `ourStringRedisTemplateWinsOverBootDefault`

`RedisConfig`의 `stringRedisTemplate`은 Boot `RedisAutoConfiguration`의 **동명 빈**과 이름이 겹친다. 이 모듈의 빈은 앱 스캔으로 등록되는 **사용자 설정**이라 auto-configuration보다 언제나 먼저 정의되고, Boot 쪽 `@ConditionalOnMissingBean(StringRedisTemplate.class)`가 그 정의를 보고 물러나 키·값 serializer를 명시 지정한 우리 템플릿이 이긴다. 이 순서는 `ourStringRedisTemplateWinsOverBootDefault`(`withUserConfiguration(RedisModuleConfig.class, RedisConfig.class)` + `AutoConfigurations.of(RedisAutoConfiguration.class)`)가 지킨다.

**이 모듈의 빈을 auto-configuration으로 등록하지 않는다.** auto-configuration이 되면 Boot 쪽과의 처리 순서를 따로 선언해야 하고, 순서가 뒤집히면 스캔된 `RedisConfig`가 같은 이름으로 다시 등록을 시도해 `allow-bean-definition-overriding=false`(기본값)에 걸려 **4개 앱 전부 기동이 실패한다** — 실제로 그렇게 실패한 적이 있다.

### `RedisConfig`에 `@ConditionalOnMissingBean`을 붙이지 않는다 — 의도된 선택

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/RedisConfig.java`
→ `stringRedisTemplate`

붙이면 Boot 쪽이 이겨 **serializer 명시 지정이 사라진다** — 우리가 이 빈을 소유하는 이유 자체가 없어진다. 이름 충돌은 조건이 아니라 **사용자 설정이 auto-config보다 먼저 처리되는 순서로** 해소한다. 게다가 일반 `@Configuration`에 빈 존재 조건을 다는 것은 금지 규칙이다(`backend/CLAUDE.md` 모듈 등록 컨벤션). "조건을 안 붙인 실수"로 보고 추가하지 않는다.

### `@SuppressWarnings("ConstantConditions")` — null 방어 코드를 지우지 않는다

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/ratelimit/RedisRateLimitCounter.java`
→ Lua 스크립트 실행부

`RedisTemplate#execute()`는 `@Nullable` 반환이라 Redis 계층에서 null 가능성이 있다. 방어 코드를 유지하되 IDE 경고만 억제한다. 어노테이션을 제거하거나 방어 분기를 "도달 불가"로 판단해 지우지 않는다.

### 앱별 Redis 키 접두사는 불변 계약이다

**대상**: `backend/infrastructure/redis/src/test/java/com/tastyhouse/infrastructure/redis/token/RedisRefreshTokenRepositoryTest.java` · `RedisBlacklistRepositoryTest.java`
→ 고정값 단정

이 두 테스트에 적힌 문자열은 **운영 Redis에 실재하는 키 공간**이며, 앱별 접두사(`""`/`"admin:"`/`"ceo:"`)는 불변 계약이다. 접두사 조합이 어긋나도 **컴파일·기동은 성공하고 기존 세션만 조용히 무효화**되며, 블랙리스트 쪽은 **로그아웃한 토큰이 걸리지 않아 예외 없이 계속 통과**한다. 이 고정값 단정이 그 유일한 자동 방어선이므로 "하드코딩"이라고 상수화·파라미터화하지 않는다.

### 최상위 클래스에 `public`을 붙이지 않는다 (package-private 적용, 허용 목록 1개)

**대상**: `backend/infrastructure/redis/src/test/java/com/tastyhouse/infrastructure/redis/architecture/PackageRootTest.java` → `topLevelClassesShouldNotBePublic` · `publicByNecessityShouldStillBePublic` · 상수 `PUBLIC_BY_NECESSITY`

이 모듈의 최상위 타입은 허용 목록 하나를 빼고 전부 package-private이다. 앱 `ModuleScanConfig`의 문자열 스캔으로만 조립되므로 타입 이름이 모듈 밖에 나타날 필요가 없다.

| 허용 목록(`PUBLIC_BY_NECESSITY`) | 이유 |
|---|---|
| `com.tastyhouse.infrastructure.redis.token.RedisTokenStoreProperties` | 상위 패키지의 `RedisModuleConfig`가 `@EnableConfigurationProperties`로 이 타입을 참조한다 — 패키지가 다르므로 package-private이면 컴파일되지 않는다 |

`publicByNecessityShouldStillBePublic`은 허용 목록이 낡지 않았는지(그 FQN이 아직 public인지) 검사한다. 새로 public이 필요해지면 FQN을 이 상수에 추가하고 이 표에 이유를 적는다. 근거와 전체 범주는 `backend/CLAUDE.md`의 "접근 제어자 규칙 (내부 구현은 package-private)" 절.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 클래스패스 존재 = 활성화다

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/RedisModuleConfig.java`
→ 클래스 전체

앱 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔은 클래스패스에 있는 것을 전부 잡으므로, 이 모듈은 **클래스패스 존재만으로 활성화된다.** 앱이 `runtimeOnly project(':infrastructure:redis')`로 **직접 선언할 때만** 실린다 — batch-module은 이 모듈을 클래스패스에 두지 않으므로 등록 대상 자체가 없다. 반대로 어떤 모듈이든 이 모듈을 `api`로 노출하면 그 전이 경로만으로 다른 앱에서도 활성화된다.

등록 대상은 `RedisConfig`(`StringRedisTemplate`)와 `ratelimit` 패키지의 `RedisRateLimitCounter`, `token` 패키지의 토큰 저장소 어댑터 3종이다. 토큰 저장소 계약(`RefreshTokenRepository` 등)은 `security-core`가 소유하고 이 모듈이 구현한다.

rate limit의 **표현 관심사**(`@RateLimit`·`RateLimitAspect`·`RateLimitException`)는 `api-common-module`에 있고, aspect 빈은 `ApiCommonRateLimitConfig`가 등록한다.

### 빈 **정의** 순서와 **생성** 순서는 다르다

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/RedisConfig.java` → `RedisConnectionFactory` 주입

`RedisConnectionFactory`는 Boot가 만들지만, 빈 *정의* 순서와 *생성* 순서는 다르다. 우리 `RedisConfig`는 사용자 설정이라 Boot의 `RedisConnectionFactory`보다 **먼저 정의**되지만, 주입은 **생성 시점에** 해결되므로 팩토리를 정상적으로 주입받는다. "먼저 정의되면 팩토리가 아직 없을 텐데"는 오해다.

### `RedisRateLimitCounter`의 Lua는 INCR + PEXPIRE 원자 실행이다

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/ratelimit/RedisRateLimitCounter.java`
→ Lua 스크립트

- **첫 번째 요청 시 TTL을 설정**하므로 서버 중단 시에도 키가 영구 잔류하지 않는다.
- **Fixed Window 방식** — 윈도우 시작 시점에 카운터가 초기화된다. 슬라이딩 윈도우가 아니므로 윈도우 경계에서 순간 두 배 허용이 가능하다는 점이 이 방식의 알려진 성질이다.
- Lua 스크립트와 키 취급을 바꾸면 운영 Redis에 있는 기존 카운터 키와 호환이 깨진다.

### 조건부 전략 배선은 기동 성공이 증거가 아니다

**대상**: `backend/infrastructure/redis/src/test/java/com/tastyhouse/infrastructure/redis/RedisModuleConfigTest.java`
→ 클래스 전체

이 테스트에는 `ourStringRedisTemplateWinsOverBootDefault` 한 건이 있고, 사용자 설정(`RedisModuleConfig`·`RedisConfig`) + Boot `RedisAutoConfiguration`으로 우리 템플릿이 이기는지 검증한다(위 봉인 목록의 순서 가드).

`@ConditionalOnProperty`·`@ConditionalOnBean` 류의 조건부 배선은 **기동 성공을 증거로 삼지 않는다** — 조건이 빗나가면 빈이 그냥 없을 뿐 기동은 성공한다. 반증(틀린 조건으로 실패하는지)을 확인한다.

### 토큰 저장소 어댑터 3종의 키 형식

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/token/`
→ `RedisRefreshTokenRepository` · `RedisBlacklistRepository` · `RedisSocialTempTokenRepository`

| 어댑터 | 키 | 값 | TTL |
|---|---|---|---|
| `RedisRefreshTokenRepository` | `{keyPrefix}rt:{username}` | refreshToken | `refreshTokenExpiration` |
| `RedisBlacklistRepository` | `{keyPrefix}bl:{accessToken}` | `"logout"` | 토큰 잔여 만료 시간 |
| `RedisSocialTempTokenRepository` (KAKAO) | `kakao_temp:{tempToken}` | kakaoAccessToken | 10분 |
| `RedisSocialTempTokenRepository` (NAVER) | `naver_temp:{tempToken}` | naverAccessToken | 10분 |
| `RedisSocialTempTokenRepository` (FACEBOOK) | `facebook_temp:{tempToken}` | facebookAccessToken | 10분 |
| `RedisSocialTempTokenRepository` (APPLE) | `apple_temp:{tempToken}` | appleIdToken | 10분 |

- 소셜 임시토큰 접두사는 `security-core` enum `SocialTempTokenProvider#keyPrefix()`가 주고, 4개 키·TTL은 `backend/infrastructure/redis/src/test/java/com/tastyhouse/infrastructure/redis/token/RedisSocialTempTokenRepositoryTest.java`가 단언한다([ADR-0050](../../../docs/adr/0050-social-login-router.md)).
- 블랙리스트는 **토큰이 만료되면 Redis TTL에 의해 자동 제거**되므로 별도 정리 작업이 없고 메모리 낭비도 없다.
- **소셜 임시토큰은 앱 간 공유되지 않으므로 앱별 키 접두사(`security.token-store.key-prefix`)를 붙이지 않는다.** 위 §키 접두사 프로퍼티 표의 대상은 refresh·blacklist 둘뿐이다.

### `RedisTokenStoreProperties` — 접두사는 콜론으로 끝내야 한다

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/token/RedisTokenStoreProperties.java`
→ `keyPrefix`

"모듈이 소비하는 설정은 모듈이 소유한다"에 따라 이 모듈이 갖는다. 앱마다 같은 Redis 인스턴스를 공유하므로 접두사로 키 공간을 나눈다(web-api `""`, admin-api `"admin:"`, ceo-api `"ceo:"`). **접두사에 콜론을 빠뜨려도 예외가 나지 않고 기존 세션만 조용히 무효화되므로 값은 반드시 콜론으로 끝내야 한다.**

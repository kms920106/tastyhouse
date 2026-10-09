<!-- Parent: ../../AGENTS.md -->

# infrastructure:redis

Redis 연결·템플릿과 **rate limit 카운터**, **토큰 저장소 어댑터 6종**을 소유하는 인프라 모듈(`java-library`). 챕터 05에서 `infrastructure`를 기술별로 재편하며 신설됐고, 챕터 02에서 rate limit의 표현 관심사를 `api-common-module`로 내보냈다. 이 모듈이 구현하는 계약(토큰 저장소 포트 6종 + `RateLimitCounterPort`)은 전부 `security-core`가 소유하며, 내부 모듈 의존은 `security-core` 하나뿐이다(`RateLimitCounterPort` 이동으로 `api-common-module` 의존이 삭제됐다).

## 신설 배경 (챕터 05)

Redis는 그동안 `security-module`이 들고 있었다. 그런데 Redis 자체는 보안 관심사가 아니라 **인프라 기술**이고, rate limiting은 도메인에 대응 개념이 아예 없는 순수 인프라 관심사다. 보안 모듈이 연결 설정과 템플릿 빈까지 소유하면 Redis를 쓰고 싶은 다른 관심사가 전부 `security-module`을 의존해야 한다.

동시에 이 재편은 **`infrastructure-module`을 기술별로 쪼개는** 일이기도 하다. `infrastructure:persistence`(JPA/QueryDSL — DB 어댑터)와 `infrastructure:redis`(Redis)가 형제가 되어, "infrastructure = DB"라는 암묵 전제가 모듈 이름에서 사라졌다. 이후 `infrastructure:external`(외부 시스템 연동)이 편입돼 driven 어댑터 3형제가 갖춰졌고, 그 뒤 external이 기술별로 다시 쪼개지고 `aws`가 다시 `aws-s3`·`aws-ses`·`aws-sns`로 3분할돼 지금은 `persistence`·`redis`·`external`·`firebase`·`aws-s3`·`aws-ses`·`aws-sns`·`oauth`·`payment`·`messaging`·`bbq`·`admdongkor` 12형제였고, 이후 `messaging`이 `mail`·`javamail`·`sms`·`solapi`로 4분할돼(2026-09-26) 15형제가 됐다(코어 `external`은 `restclient`로 리네임). 이어서 `payment`가 채널 `pg`·벤더 `tosspayments`로 나뉘어 16형제, `oauth`가 코드 없는 채널 스타터로 바뀌고 벤더 `kakao-oauth`·`naver-oauth`·`apple-oauth`·`facebook-oauth`가 신설돼(2026-09-27) 19형제가 됐다(코드 없는 스타터 `file-storage`·`oauth`는 어댑터가 아니라 형제 수에서 빠진다).

**`settings.gradle`에서 `include 'infrastructure:persistence'`·`include 'infrastructure:redis'`로 선언한다.** 중첩 프로젝트 컨테이너 `:infrastructure`는 소스가 없는 빈 프로젝트이므로, 루트 `build.gradle`의 `subprojects` 일괄 설정이 이 컨테이너에 `bootJar`를 걸지 않도록 주의한다(챕터 05에서 실제로 걸렸던 문제).

## 패키지 구조

```
com.tastyhouse.infrastructure.redis/
├── RedisModuleConfig.java        @EnableConfigurationProperties(RedisTokenStoreProperties)만 — 스캔 없음(앱 ModuleScanConfig가 com.tastyhouse.infrastructure를 스캔). imports 제거로 RedisModuleAutoConfiguration에서 원래 이름으로 복귀
├── RedisConfig.java              StringRedisTemplate 빈 (key/value StringRedisSerializer)
├── ratelimit/
│   └── RedisRateLimitCounter.java  security-core의 RateLimitCounterPort 구현 — 순수 Redis Lua (INCR + PEXPIRE 원자 실행)
└── token/                          챕터 01 신설 — security-core의 토큰 저장소 포트 6종 구현
    ├── RedisTokenStoreProperties.java   @ConfigurationProperties("security.token-store") — keyPrefix
    ├── RedisRefreshTokenRepository.java  {keyPrefix}rt:{username}
    ├── RedisBlacklistRepository.java     {keyPrefix}bl:{accessToken}
    └── Redis{Kakao,Naver,Facebook,Apple}TempTokenRepository.java  접두사 고정 (kakao_temp: 등)
```

**`token` 패키지는 챕터 01에서 `security-core`로부터 넘어왔다.** 과거에는 구체 Redis 저장소 6종이 `security-core`에 있어 그 모듈이 `implementation project(':infrastructure:redis')`를 의존했는데, "core가 구체 인프라를 의존하는" 역방향 간선이었다. 지금은 계약이 `security-core`, 구현이 여기 있다 — ~~`RedisRateLimitCounter`가 `api-common-module`의 `RateLimitCounterPort`를 구현하는 것과 **동형**이다~~ **(번복됨 — `RateLimitCounterPort`도 `security-core`로 옮겨졌다.)** 지금은 비유가 아니라 **같은 구조**다 — 토큰 저장소 포트 6종과 `RateLimitCounterPort` 모두 계약은 `security-core`, 구현은 이 모듈이다.

이 간선을 끊은 실익은 전이 사슬 제거다. `application → security-core → infrastructure:redis → api-common-module`이 4앱 runtimeClasspath에 실려 Redis를 쓰지 않는 batch-module에서도 `RedisModuleAutoConfiguration`(현 `RedisModuleConfig`)이 발화하고 springdoc·starter-web이 배포 산출물에 들어갔다. 지금 batch runtimeClasspath에는 redis·api-common·springdoc이 **없다**.

### 키 접두사 프로퍼티

| 앱 | `security.token-store.key-prefix` | refresh 키 | blacklist 키 |
|---|---|---|---|
| web-api | `""` (기본값, yml 생략) | `rt:{username}` | `bl:{accessToken}` |
| admin-api | `admin:` | `admin:rt:{username}` | `admin:bl:{accessToken}` |
| ceo-api | `ceo:` | `ceo:rt:{username}` | `ceo:bl:{accessToken}` |

**"모듈이 소비하는 설정은 모듈이 소유한다"** 컨벤션에 따라 `RedisTokenStoreProperties`가 이 모듈에 있고, `RedisModuleConfig`(imports 제거 전 `RedisModuleAutoConfiguration`)가 `@EnableConfigurationProperties`로 등록한다. 값은 각 앱 `application.yml`이 준다.

**이 표는 불변 계약이다.** 접두사가 어긋나도 예외가 나지 않고 기존 세션만 조용히 무효화되므로(콜론 누락 `admin` → `adminrt:`), `RedisRefreshTokenRepositoryTest`·`RedisBlacklistRepositoryTest`의 고정값 단정이 유일한 자동 방어선이다. 소셜 임시토큰 4종은 앱 간 공유되지 않아 접두사가 고정이며 프로퍼티를 받지 않는다.

**챕터 02에서 이 패키지는 카운터 하나만 남았다.** `@RateLimit`·`RateLimitAspect`·`RateLimitKeyType`·`RateLimitException`과 신설 계약 `RateLimitCounterPort`는 `com.tastyhouse.apicommon.ratelimit`로 이동했다(계약만은 이후 `security-core`로 다시 옮겨졌다 — 아래 번복 참고). 키 조립(클라이언트 IP·요청 필드 해석)은 HTTP 어댑터 관심사인데 그것을 인프라가 들고 있느라 이 모듈이 서블릿 스택을 의존했고, 반대로 표현 모듈인 `api-common-module`이 `RateLimitException` 처리를 위해 이 인프라 모듈을 의존하는 역방향이 생겼기 때문이다. ~~지금은 **`infrastructure:redis` → `api-common-module`**(어댑터 → 계약) 한 방향뿐이다.~~ **(번복됨)** 그 한 방향도 아웃바운드 어댑터가 계약 하나 때문에 **표현 모듈**(`starter-web`을 노출)을 컴파일 의존하는 수평 간선이었다. 계약 `RateLimitCounterPort`를 서블릿-프리 `security-core`(`com.tastyhouse.security.ratelimit`)로 옮겨, 지금은 **`infrastructure:redis` → `security-core` ← `api-common-module`** 이다 — 구현과 표현이 main 클래스패스에서 서로를 모른다. 이동 근거는 `security-core/AGENTS.md`의 "rate limit 카운터 계약을 여기 둔 이유".

`RedisRateLimitCounter`는 과거 `RateLimiterService`이며, Lua 스크립트와 키 취급이 그대로라 **기존 Redis 카운터 키와 호환**된다(키 접두사는 호출부의 `@RateLimit(keyPrefix=...)`가 결정한다).

## yml — `application-redis.yml` (챕터 05 §5b)

이 모듈은 Redis 관련 **코드**뿐 아니라 **연결 설정**도 소유한다. `spring.data.redis.{host,port,password}`를 담으며 셋 다 환경변수로 덮어쓸 수 있다(`REDIS_HOST`·`REDIS_PORT`·`REDIS_PASSWORD`, 기본값 `localhost:6379`/빈 비밀번호).

**소유가 이 모듈로 온 근거는 소비 지점이다.** 이 설정으로 만들어지는 `RedisConnectionFactory`를 주입받는 것이 이 모듈의 `RedisConfig`(`StringRedisTemplate`)이므로, **"모듈이 소비하는 설정은 모듈이 소유한다"** 는 기존 컨벤션(`application-infrastructure.yml`·`application-file-storage.yml`의 선례)이 그대로 적용된다. 챕터 05 §5b에서 `security-module`의 `application-security.yml`에서 옮겨왔고, **그 파일은 이 블록만 담고 있어 파일째 이관되고 삭제됐다.**

로딩은 web/admin/ceo-api 3개 앱의 `application.yml`이 `classpath:application-redis.yml`을 `spring.config.import`로 수행한다. batch-module은 대상이 아니다.

## Dependencies

### Internal
~~`api-common-module` (implementation) — `RedisRateLimitCounter`가 구현하는 `RateLimitCounterPort`의 소유 모듈(챕터 02)~~ **(삭제됨 — `RateLimitCounterPort`가 `security-core`로 이동)**. `./gradlew :infrastructure:redis:dependencies --configuration compileClasspath | grep -cE 'project :api-common-module|starter-web'`가 0이어야 한다(2026-09-27 실측 0).

**`domain`에 의존하지 않는다.** rate limiting은 domain에 대응 개념이 없는 순수 인프라 관심사라서 `RateLimitException`도 도메인 에러코드에 결합하지 않는다. HTTP 응답 조립은 각 api 모듈의 `GlobalExceptionHandler`가 `com.tastyhouse.apicommon.exception.ApiErrorCode#RATE_LIMIT_EXCEEDED`로 직접 수행한다(과거 단일 `ErrorCode.RATE_LIMIT_EXCEEDED`·`ErrorContracts#rateLimit` — 에러코드 모듈 분할로 대체).

이것이 `infrastructure:jpa`(jpa 모듈 분리 전 `infrastructure:persistence`)와의 결정적 차이다 — jpa는 domain 포트의 어댑터라 `domain`을 `api`로 노출하지만, redis는 domain 포트가 없는 기술이라 domain을 아예 모른다.

- `security-core` (implementation) — 챕터 01 신설 간선. `token` 패키지가 구현하는 토큰 저장소 포트 6종(`RefreshTokenRepository` 등)과 `ratelimit` 패키지가 구현하는 `RateLimitCounterPort`(`com.tastyhouse.security.ratelimit`)의 소유 모듈. 어댑터 → 계약 방향이며, `infrastructure:jpa`가 읽기 포트 소유 모듈(`application`)을 의존하는 선례와 동형이다. **이 모듈의 유일한 내부 의존이다.**

### External
- `spring-boot-starter-data-redis` (**api**) — `StringRedisTemplate`·`RedisConnectionFactory`. `api`로 두는 이유는 이제 소비 모듈의 시그니처 노출이 아니라 **이 모듈의 어댑터가 그 타입을 쓰기 때문**이다. 챕터 01로 앱과 `security-core`의 compileClasspath에서 Redis 타입이 사라졌고, `runtimeOnly project(':infrastructure:redis')`가 4앱 중 3앱의 **유일한** Redis 선언이 됐다(batch는 선언 자체가 없다)
- ~~**테스트용 `api-common-module` 별도 선언은 불필요하다** — 위 `implementation`이 테스트 컴파일 클래스패스에도 보이기 때문이다~~ **(번복됨)** — `api-common-module` 의존이 main에서 삭제됐고, `afterName` 문자열 가드 테스트도 `api-common-module`로 옮겨졌다(아래 §조건부 전략 배선 — 그 테스트는 이후 imports 제거로 `afterName`과 함께 삭제됐다). 이 모듈은 테스트에서도 api-common을 선언하지 않는다 — redis → api-common 방향은 테스트 클래스패스에서도 사라졌다.
- `archunit-junit5` (**testImplementation**) — 패키지 루트 가드 `architecture/PackageRootTest`용(infrastructure 패키지 루트 통일로 추가)
- `spring-boot-starter-aop`·`spring-boot-starter-web`는 **선언하지 않는다** — `@Aspect`와 `HttpServletRequest` 기반 IP 해석이 전부 `api-common-module`로 이동했다(챕터 02). 남은 것은 Redis Lua 카운터뿐이라 이 모듈은 서블릿·AOP 스택을 알지 않는다.

## security-core / security-module과의 관계

> **번복 기록 (챕터 01)**: 이 절은 원래 *"토큰 저장소 6종은 `security-module`에 잔류한다"*였다(이후 챕터 03에서 `security-core`로 이동). **그 판단은 뒤집혔다** — 저장소 6종의 **구현**은 지금 이 모듈의 `token` 패키지에 있다.
>
> 당시 근거는 "그것들은 보안 관심사다"였고 그 자체는 지금도 맞다. 뒤집힌 이유는 **관심사의 귀속과 기술 구현의 귀속이 다른 층위**이기 때문이다. 보안 관심사인 것은 *계약*(무엇을 저장하고 언제 무효화하는가)이고, `StringRedisTemplate`으로 키를 조립하는 것은 *구현*이다. 계약을 `security-core`에 남기고 구현을 여기로 내리면 두 귀속이 모두 지켜지며, 그 대가로 `security-core → infrastructure:redis` 역방향 간선이 사라진다.
>
> Redis key prefix는 이 번복에도 **바이트 단위로 불변**이다(`rt:`/`bl:`/`admin:rt:`/`admin:bl:`/`kakao_temp:` 등).

- `security-core` — 이 모듈이 `implementation`으로 의존한다(어댑터 → 계약). 토큰 저장소 포트 6종의 소유자.
- `security-module` — 여전히 `implementation project(':infrastructure:redis')`를 선언하지 않는다. 서블릿 결합 타입만 남았고, `JwtAuthenticationFilter`는 `security-core`의 `BlacklistRepository` **포트**를 받으므로 Redis 타입을 알지 않는다.

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **빈 배선 (imports 제거로 재개정)**: 이 모듈의 빈(`RedisConfig`·`RedisRateLimitCounter`·토큰 저장소 어댑터 6종)은 web·admin·ceo 부트스트랩의 중첩 `ModuleScanConfig`가 `com.tastyhouse.infrastructure`를 문자열로 스캔해 등록하고, `RedisModuleConfig`는 `@EnableConfigurationProperties`만 한다. aspect 빈은 `api-common-module`의 `ApiCommonRateLimitConfig`가 등록한다(빈 존재 조건·`afterName` 없음 — `apicommon.ratelimit`을 스캔하는 앱은 전부 이 모듈을 클래스패스에 갖는다). "클래스패스에 올라오면 활성화"는 그대로다. 아래는 챕터 02 시점 기록이다(**번복됨 — imports 제거**). ~~`RedisModuleAutoConfiguration`이 클래스패스 존재만으로 자동 등록되며, 의존하는 앱은 `@Import`하지 않는다(`build.gradle` 의존 선언 = 활성화). 이 설정은 카운터 구현체만 등록하고, **aspect 빈 등록은 `api-common-module`의 `ApiCommonRateLimitAutoConfiguration`이 담당**한다(`@ConditionalOnBean(RateLimitCounterPort.class)` + `afterName`으로 이 모듈 이후 평가). 조건부 등록으로 전환되어 "배선 누락으로 `@RateLimit`이 조용히 무시된다"는 실패 양식은 사라졌지만, 이 경로를 건드렸으면 여전히 한도 초과 호출로 429를 실제 확인한다.~~ 이 경로를 건드렸으면 지금도 한도 초과 호출로 429를 실제 확인한다.
- **Redis를 쓰는 새 관심사는 `security-module`이 아니라 이 모듈을 의존한다.** 그것이 이 모듈을 나눈 이유다.
- **다른 모듈의 계약을 구현할 때는 그 모듈을 이 모듈이 의존한다**(어댑터 → 계약). 반대로 계약 소유 모듈이 이 모듈을 의존하게 만들면 챕터 01이 끊은 역방향 간선이 되살아난다 — 그 회귀는 컴파일·기동 모두 성공하고 **batch 배포 산출물이 조용히 불어나는** 형태로만 드러난다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 패키지 루트 `com.tastyhouse.infrastructure.redis` — 다른 모듈의 스캔과 겹치지 않는다

**대상**: `backend/infrastructure/redis/src/test/java/com/tastyhouse/infrastructure/redis/architecture/PackageRootTest.java` → `shouldResideInModuleRootPackage`
→ (당시) `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/persistence/PersistenceModuleAutoConfiguration.java` → `@ComponentScan` — imports 제거로 삭제됐고 지금 스캔은 앱 `ModuleScanConfig`가 한다

이 모듈의 main 클래스는 전부 `com.tastyhouse.infrastructure.redis` 아래에 있어야 한다(`PackageRootTest`, ArchUnit은 `testImplementation`으로만 선언). 이 모듈의 패키지는 infrastructure 패키지 루트 통일 전후로 **바뀌지 않았다** — 바뀐 것은 형제 모듈 쪽이다.

| 항목 | before | after |
|---|---|---|
| persistence 스캔 | `@ComponentScan("com.tastyhouse.infrastructure")` — 이 모듈의 트리까지 덮고, REGEX `excludeFilters`(`com\.tastyhouse\.infrastructure\.redis\..*`)로 되돌렸다 | `@ComponentScan("com.tastyhouse.infrastructure.persistence")` — 이 모듈과 겹치지 않아 제외 필터가 삭제됐다 |
| Redis 빈 등록 주체 | `RedisModuleAutoConfiguration` (제외 필터가 빠지면 중복 등록) | `RedisModuleAutoConfiguration` 하나 — 구조적으로 보장된다. **(imports 제거 후)** 지금은 앱 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔 하나 — 같은 클래스는 한 번만 등록된다 |
| 동작 | — | 변경 없음 |

- 이 모듈의 클래스를 `com.tastyhouse.infrastructure` 바로 아래 등 루트 밖으로 옮기지 않는다 — `PackageRootTest`가 실패하고, 다른 모듈의 스캔 범위와 다시 겹칠 수 있다.

### `@AutoConfiguration(before = RedisAutoConfiguration.class)` — `before`를 `after`로 바꾸지 않는다

> **(번복됨 — imports 제거: 이 봉인은 대상이 사라졌다)** `RedisModuleAutoConfiguration`은 `RedisModuleConfig`(일반 `@Configuration`)가 됐고 `before` 속성도 없다. 필요 없어진 이유는 아래 본문 마지막 문단 그대로다 — **사용자 설정은 언제나 auto-configuration보다 먼저 처리되므로**, 앱 스캔으로 등록되는 `RedisConfig`의 `stringRedisTemplate`이 먼저 정의되고 Boot `RedisAutoConfiguration`의 `@ConditionalOnMissingBean`이 물러난다. 이 순서는 `RedisModuleConfigTest#ourStringRedisTemplateWinsOverBootDefault`(`withUserConfiguration(RedisModuleConfig.class, RedisConfig.class)` + `AutoConfigurations.of(RedisAutoConfiguration.class)`)가 지킨다. **이 모듈의 빈을 다시 auto-configuration으로 등록하게 되면 이 순서 문제가 되살아난다** — 그것이 imports 파일을 되살리지 않는 이유 중 하나다. 아래는 당시 기록이다.

**대상**: ~~`backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/RedisModuleAutoConfiguration.java`
→ 클래스 애노테이션의 `before` 속성~~ (삭제됨)

`RedisConfig`의 `stringRedisTemplate`은 Boot `RedisAutoConfiguration`의 **동명 빈**과 이름이 겹친다. 이 모듈이 **먼저 정의돼야** Boot 쪽 `@ConditionalOnMissingBean(StringRedisTemplate.class)`가 그 정의를 보고 물러나고, 키·값 serializer를 명시 지정한 우리 템플릿이 이긴다.

`after`로 두면 Boot 쪽이 먼저 등록되고, 그 뒤 **스캔된** `RedisConfig`(조건 없는 `@Configuration`)가 같은 이름으로 다시 등록을 시도해 `allow-bean-definition-overriding=false`(기본값)에 걸려 **기동이 실패한다** — 실제로 **4개 앱 전부 이렇게 실패한 적이 있다.** 즉 실패 지점은 "Boot의 조건이 물러나지 않아서"가 아니라 **"스캔 빈이 auto-config 빈을 덮으려다 override 금지에 걸려서"** 다. 어느 쪽이든 조용한 회귀가 아니라 기동 실패라는 점이 이 이름 충돌의 안전판이다.

전환 전에는 앱의 `@Import`가 auto-configuration보다 먼저 처리돼 자연히 이겼으나, 이 설정도 auto-configuration이 된 지금은 **순서를 명시해야 한다.**

### `RedisConfig`에 `@ConditionalOnMissingBean`을 붙이지 않는다 — 의도된 선택

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/RedisConfig.java`
→ `stringRedisTemplate`

붙이면 순서와 무관하게 Boot 쪽이 이겨 **serializer 명시 지정이 사라진다** — 우리가 이 빈을 소유하는 이유 자체가 없어진다. 이름 충돌은 조건이 아니라 ~~위 `before` 순서로~~ **사용자 설정이 auto-config보다 먼저 처리되는 순서로**(imports 제거 후) 해소한다. 게다가 일반 `@Configuration`에 빈 존재 조건을 다는 것은 이제 금지 규칙이다(`backend/CLAUDE.md` 모듈 등록 컨벤션). "조건을 안 붙인 실수"로 보고 추가하지 않는다.

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

### auto-config는 **전이 존재 = 활성화**다

> **(번복됨 — imports 제거: 주체만)** 지금 이 모듈의 빈을 활성화하는 주체는 imports 파일이 아니라 앱 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔이다. 그 스캔은 클래스패스에 있는 것을 전부 잡으므로 "전이 존재 = 활성화" 결론은 그대로이고, 아래 본문의 `ApiCommonRateLimitAutoConfiguration`은 `ApiCommonRateLimitConfig`(빈 존재 조건 없음)로 읽는다.

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/RedisModuleConfig.java`(당시 `RedisModuleAutoConfiguration.java`)
→ 클래스 전체

이 모듈은 **클래스패스 존재만으로 활성화된다.** 앱이 `runtimeOnly project(':infrastructure:redis')`로 **직접 선언할 때만** 실린다 — batch-module은 이 모듈을 클래스패스에 두지 않으므로 발화 대상 자체가 없다. 반대로 어떤 모듈이든 이 모듈을 `api`로 노출하면 그 전이 경로만으로 다른 앱에서도 발화한다(§`security-core`/`security-module`과의 관계에 기록된 과거 사슬이 그 사례다).

등록 대상은 `RedisConfig`(`StringRedisTemplate`)와 `ratelimit` 패키지의 `RedisRateLimitCounter`, `token` 패키지의 토큰 저장소 어댑터 6종이다. 토큰 저장소 계약(`RefreshTokenRepository` 등)은 `security-core`가 소유하고 이 모듈이 구현한다.

rate limit의 **표현 관심사**(`@RateLimit`·`RateLimitAspect`·`RateLimitException`)는 `api-common-module`에 있고, aspect 빈은 `ApiCommonRateLimitAutoConfiguration`이 **이 카운터 빈의 존재를 조건으로** 등록한다.

### 빈 **정의** 순서와 **생성** 순서는 다르다

> **(번복됨 — imports 제거)** `before` 속성은 사라졌지만 이 구분은 그대로 유효하다 — 지금 우리 `RedisConfig`는 사용자 설정이라 Boot의 `RedisConnectionFactory`보다 **먼저 정의**되고, 그래도 팩토리는 **생성 시점**에 주입되므로 문제없다.

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/RedisConfig.java` → `RedisConnectionFactory` 주입 (당시 대상: `RedisModuleAutoConfiguration.java`의 `before` 속성)

`RedisConnectionFactory`는 Boot가 만들지만, 빈 *정의* 순서와 *생성* 순서는 다르다 — 주입은 **생성 시점에** 해결되므로 `before`로 두어도 팩토리를 정상적으로 주입받는다. "`before`면 팩토리가 아직 없을 텐데"는 오해다.

### `RedisRateLimitCounter`의 Lua는 INCR + PEXPIRE 원자 실행이다

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/ratelimit/RedisRateLimitCounter.java`
→ Lua 스크립트

- **첫 번째 요청 시 TTL을 설정**하므로 서버 중단 시에도 키가 영구 잔류하지 않는다.
- **Fixed Window 방식** — 윈도우 시작 시점에 카운터가 초기화된다. 슬라이딩 윈도우가 아니므로 윈도우 경계에서 순간 두 배 허용이 가능하다는 점이 이 방식의 알려진 성질이다.
- 과거 `RateLimiterService`였고 챕터 02에서 포트 구현체로 전환하며 개명했다. Lua 스크립트와 키 취급은 그대로이므로 **기존 Redis 카운터 키와 호환**된다.

### 조건부 전략 배선은 기동 성공이 증거가 아니다

> **(번복됨 — imports 제거)** 테스트는 `RedisModuleConfigTest.java`로 리네임됐고 `ourStringRedisTemplateWinsOverBootDefault` 한 건이 사용자 설정(`RedisModuleConfig`·`RedisConfig`) + Boot `RedisAutoConfiguration`으로 검증한다. `afterName`과 그 단정(`rateLimitAutoConfigurationAfterNameResolvesToRealClass`)·api-common의 `testImplementation project(':infrastructure:redis')`는 함께 삭제됐다. "조건부 배선은 기동 성공을 증거로 삼지 않는다"는 아래 결론은 유효하다(`@ConditionalOnProperty` 벤더 선택 등).

**대상**: `backend/infrastructure/redis/src/test/java/com/tastyhouse/infrastructure/redis/RedisModuleConfigTest.java`(당시 `RedisModuleAutoConfigurationTest.java`)
→ 클래스 전체

~~**이 테스트가 redis 모듈에 있는 이유는 가드 대상이 두 모듈에 걸쳐 있기 때문이다** — `ApiCommonRateLimitAutoConfiguration`의 순서 선언(`afterName`)이 이 모듈의 클래스를 **문자열로** 가리키는데, api-common은 의존 방향(redis → api-common)상 redis 타입을 볼 수 없어 자기 모듈에서는 검증할 수 없다. 반대로 이 모듈은 api-common을 의존하므로 양쪽을 다 볼 수 있다.~~ **(번복됨 — `afterName` 검증은 `api-common-module`로 옮겨졌다.)** `RateLimitCounterPort` 이동으로 이 모듈의 api-common 의존이 사라졌고, 대신 api-common이 `testImplementation project(':infrastructure:redis')`로 이 모듈을 **테스트에서만** 볼 수 있게 됐다. 그래서 `afterName` 문자열 단정(`rateLimitAutoConfigurationAfterNameResolvesToRealClass`)은 `backend/api-common-module/src/test/java/com/tastyhouse/apicommon/ApiCommonAutoConfigurationTest.java`에 있다 — **소비 쪽이 구현의 등록 순서를 검증**하는 방향이다. 이 파일에 남은 것은 `ourStringRedisTemplateWinsOverBootDefault`(위 봉인 목록의 `before` 순서 가드)뿐이다.

`afterName`은 문자열 참조라 **클래스를 리네임·이동해도 컴파일이 깨지지 않고** 조건 평가만 조용히 어긋나므로, 이 리플렉션 단정이 유일한 방어선이다. 같은 이유로 `@ConditionalOnProperty`·`@ConditionalOnBean` 류의 조건부 배선은 **기동 성공을 증거로 삼지 않는다** — 조건이 빗나가면 빈이 그냥 없을 뿐 기동은 성공한다. 반증(틀린 조건으로 실패하는지)을 확인한다.

### 토큰 저장소 어댑터 6종의 키 형식

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/token/`
→ `RedisRefreshTokenRepository` · `RedisBlacklistRepository` · `Redis{Kakao,Naver,Facebook,Apple}TempTokenRepository`

| 어댑터 | 키 | 값 | TTL |
|---|---|---|---|
| `RedisRefreshTokenRepository` | `{keyPrefix}rt:{username}` | refreshToken | `refreshTokenExpiration` |
| `RedisBlacklistRepository` | `{keyPrefix}bl:{accessToken}` | `"logout"` | 토큰 잔여 만료 시간 |
| `RedisKakaoTempTokenRepository` | `kakao_temp:{tempToken}` | kakaoAccessToken | 10분 |
| `RedisNaverTempTokenRepository` | `naver_temp:{tempToken}` | naverAccessToken | 10분 |
| `RedisFacebookTempTokenRepository` | `facebook_temp:{tempToken}` | facebookAccessToken | 10분 |
| `RedisAppleTempTokenRepository` | `apple_temp:{tempToken}` | appleIdToken | 10분 |

- 블랙리스트는 **토큰이 만료되면 Redis TTL에 의해 자동 제거**되므로 별도 정리 작업이 없고 메모리 낭비도 없다.
- **소셜 임시 토큰 4종은 앱 간 공유되지 않으므로 키 접두사 설정을 받지 않는다**(접두사 고정). 위 §키 접두사 프로퍼티 표의 대상은 refresh·blacklist 둘뿐이다.

### `RedisTokenStoreProperties` — 접두사는 콜론으로 끝내야 한다

**대상**: `backend/infrastructure/redis/src/main/java/com/tastyhouse/infrastructure/redis/token/RedisTokenStoreProperties.java`
→ `keyPrefix`

"모듈이 소비하는 설정은 모듈이 소유한다"에 따라 이 모듈이 갖는다. 앱마다 같은 Redis 인스턴스를 공유하므로 접두사로 키 공간을 나눈다(web-api `""`, admin-api `"admin:"`, ceo-api `"ceo:"`). **접두사에 콜론을 빠뜨려도 예외가 나지 않고 기존 세션만 조용히 무효화되므로 값은 반드시 콜론으로 끝내야 한다.**

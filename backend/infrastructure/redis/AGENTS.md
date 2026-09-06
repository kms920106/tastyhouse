<!-- Parent: ../../AGENTS.md -->

# infrastructure:redis

Redis 연결·템플릿과 **rate limit 카운터**, **토큰 저장소 어댑터 6종**을 소유하는 인프라 모듈(`java-library`). 챕터 05에서 `infrastructure`를 기술별로 재편하며 신설됐고, 챕터 02에서 rate limit의 표현 관심사를 `api-common-module`로 내보냈다.

## 신설 배경 (챕터 05)

Redis는 그동안 `security-module`이 들고 있었다. 그런데 Redis 자체는 보안 관심사가 아니라 **인프라 기술**이고, rate limiting은 도메인에 대응 개념이 아예 없는 순수 인프라 관심사다. 보안 모듈이 연결 설정과 템플릿 빈까지 소유하면 Redis를 쓰고 싶은 다른 관심사가 전부 `security-module`을 의존해야 한다.

동시에 이 재편은 **`infrastructure-module`을 기술별로 쪼개는** 일이기도 하다. `infrastructure:persistence`(JPA/QueryDSL — DB 어댑터)와 `infrastructure:redis`(Redis)가 형제가 되어, "infrastructure = DB"라는 암묵 전제가 모듈 이름에서 사라졌다. 이후 `infrastructure:external`(외부 시스템 연동)이 편입돼 driven 어댑터 3형제가 갖춰졌고, 그 뒤 external이 기술별로 다시 쪼개져 지금은 `persistence`·`redis`·`external`·`firebase`·`aws`·`oauth`·`payment`·`messaging`·`crawling` 9형제다.

**`settings.gradle`에서 `include 'infrastructure:persistence'`·`include 'infrastructure:redis'`로 선언한다.** 중첩 프로젝트 컨테이너 `:infrastructure`는 소스가 없는 빈 프로젝트이므로, 루트 `build.gradle`의 `subprojects` 일괄 설정이 이 컨테이너에 `bootJar`를 걸지 않도록 주의한다(챕터 05에서 실제로 걸렸던 문제).

## 패키지 구조

```
com.tastyhouse.infrastructure.redis/
├── RedisModuleAutoConfiguration.java  @ComponentScan 진입점 — 챕터 02로 RedisModuleConfig에서 리네임 + @AutoConfiguration, 자기 등록
├── RedisConfig.java              StringRedisTemplate 빈 (key/value StringRedisSerializer)
├── ratelimit/
│   └── RedisRateLimitCounter.java  RateLimitCounterPort 구현 — 순수 Redis Lua (INCR + PEXPIRE 원자 실행)
└── token/                          챕터 01 신설 — security-core의 토큰 저장소 포트 6종 구현
    ├── RedisTokenStoreProperties.java   @ConfigurationProperties("security.token-store") — keyPrefix
    ├── RedisRefreshTokenRepository.java  {keyPrefix}rt:{username}
    ├── RedisBlacklistRepository.java     {keyPrefix}bl:{accessToken}
    └── Redis{Kakao,Naver,Facebook,Apple}TempTokenRepository.java  접두사 고정 (kakao_temp: 등)
```

**`token` 패키지는 챕터 01에서 `security-core`로부터 넘어왔다.** 과거에는 구체 Redis 저장소 6종이 `security-core`에 있어 그 모듈이 `implementation project(':infrastructure:redis')`를 의존했는데, "core가 구체 인프라를 의존하는" 역방향 간선이었다. 지금은 계약이 `security-core`, 구현이 여기 있다 — `RedisRateLimitCounter`가 `api-common-module`의 `RateLimitCounterPort`를 구현하는 것과 **동형**이다.

이 간선을 끊은 실익은 전이 사슬 제거다. `application → security-core → infrastructure:redis → api-common-module`이 4앱 runtimeClasspath에 실려 Redis를 쓰지 않는 batch-module에서도 `RedisModuleAutoConfiguration`이 발화하고 springdoc·starter-web이 배포 산출물에 들어갔다. 지금 batch runtimeClasspath에는 redis·api-common·springdoc이 **없다**.

### 키 접두사 프로퍼티

| 앱 | `security.token-store.key-prefix` | refresh 키 | blacklist 키 |
|---|---|---|---|
| web-api | `""` (기본값, yml 생략) | `rt:{username}` | `bl:{accessToken}` |
| admin-api | `admin:` | `admin:rt:{username}` | `admin:bl:{accessToken}` |
| ceo-api | `ceo:` | `ceo:rt:{username}` | `ceo:bl:{accessToken}` |

**"모듈이 소비하는 설정은 모듈이 소유한다"** 컨벤션에 따라 `RedisTokenStoreProperties`가 이 모듈에 있고, `RedisModuleAutoConfiguration`이 `@EnableConfigurationProperties`로 등록한다. 값은 각 앱 `application.yml`이 준다.

**이 표는 불변 계약이다.** 접두사가 어긋나도 예외가 나지 않고 기존 세션만 조용히 무효화되므로(콜론 누락 `admin` → `adminrt:`), `RedisRefreshTokenRepositoryTest`·`RedisBlacklistRepositoryTest`의 고정값 단정이 유일한 자동 방어선이다. 소셜 임시토큰 4종은 앱 간 공유되지 않아 접두사가 고정이며 프로퍼티를 받지 않는다.

**챕터 02에서 이 패키지는 카운터 하나만 남았다.** `@RateLimit`·`RateLimitAspect`·`RateLimitKeyType`·`RateLimitException`과 신설 계약 `RateLimitCounterPort`는 `com.tastyhouse.apicommon.ratelimit`로 이동했다. 키 조립(클라이언트 IP·요청 필드 해석)은 HTTP 어댑터 관심사인데 그것을 인프라가 들고 있느라 이 모듈이 서블릿 스택을 의존했고, 반대로 표현 모듈인 `api-common-module`이 `RateLimitException` 처리를 위해 이 인프라 모듈을 의존하는 역방향이 생겼기 때문이다. 지금은 **`infrastructure:redis` → `api-common-module`**(어댑터 → 계약) 한 방향뿐이다.

`RedisRateLimitCounter`는 과거 `RateLimiterService`이며, Lua 스크립트와 키 취급이 그대로라 **기존 Redis 카운터 키와 호환**된다(키 접두사는 호출부의 `@RateLimit(keyPrefix=...)`가 결정한다).

## yml — `application-redis.yml` (챕터 05 §5b)

이 모듈은 Redis 관련 **코드**뿐 아니라 **연결 설정**도 소유한다. `spring.data.redis.{host,port,password}`를 담으며 셋 다 환경변수로 덮어쓸 수 있다(`REDIS_HOST`·`REDIS_PORT`·`REDIS_PASSWORD`, 기본값 `localhost:6379`/빈 비밀번호).

**소유가 이 모듈로 온 근거는 소비 지점이다.** 이 설정으로 만들어지는 `RedisConnectionFactory`를 주입받는 것이 이 모듈의 `RedisConfig`(`StringRedisTemplate`)이므로, **"모듈이 소비하는 설정은 모듈이 소유한다"** 는 기존 컨벤션(`application-infrastructure.yml`·`application-file-storage.yml`의 선례)이 그대로 적용된다. 챕터 05 §5b에서 `security-module`의 `application-security.yml`에서 옮겨왔고, **그 파일은 이 블록만 담고 있어 파일째 이관되고 삭제됐다.**

로딩은 web/admin/ceo-api 3개 앱의 `application.yml`이 `classpath:application-redis.yml`을 `spring.config.import`로 수행한다. batch-module은 대상이 아니다.

## Dependencies

### Internal
- `api-common-module` (implementation) — `RedisRateLimitCounter`가 구현하는 `RateLimitCounterPort`의 소유 모듈(챕터 02). 어댑터가 계약을 의존하는 형태로, `infrastructure:persistence`가 읽기 포트 소유 모듈(`{앱}-application`)을 의존하는 선례와 동형이다.

**`domain`에 의존하지 않는다.** rate limiting은 domain에 대응 개념이 없는 순수 인프라 관심사라서 `RateLimitException`도 `ErrorCode`에 결합하지 않는다. HTTP 응답 조립은 각 api 모듈의 `GlobalExceptionHandler`가 `ErrorCode`로 직접 수행한다.

이것이 `infrastructure:persistence`와의 결정적 차이다 — persistence는 domain 포트의 어댑터라 `domain`을 `api`로 노출하지만, redis는 domain 포트가 없는 기술이라 domain을 아예 모른다.

- `security-core` (implementation) — 챕터 01 신설 간선. `token` 패키지가 구현하는 토큰 저장소 포트 6종(`RefreshTokenRepository` 등)의 소유 모듈. 위 `api-common-module`과 같은 어댑터 → 계약 방향이다.

### External
- `spring-boot-starter-data-redis` (**api**) — `StringRedisTemplate`·`RedisConnectionFactory`. `api`로 두는 이유는 이제 소비 모듈의 시그니처 노출이 아니라 **이 모듈의 어댑터가 그 타입을 쓰기 때문**이다. 챕터 01로 앱과 `security-core`의 compileClasspath에서 Redis 타입이 사라졌고, `runtimeOnly project(':infrastructure:redis')`가 4앱 중 3앱의 **유일한** Redis 선언이 됐다(batch는 선언 자체가 없다)
- **테스트용 `api-common-module` 별도 선언은 불필요하다** — `afterName` 문자열 가드 테스트가 `ApiCommonRateLimitAutoConfiguration`을 리플렉션으로 읽지만, 위 `implementation`은 테스트 컴파일 클래스패스에도 보이기 때문이다(`testImplementation` 중복 선언을 추가하지 않는 이유).
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
- **빈 배선 (챕터 02 개정)**: `RedisModuleAutoConfiguration`이 클래스패스 존재만으로 자동 등록되며, 의존하는 앱은 `@Import`하지 않는다(`build.gradle` 의존 선언 = 활성화). 이 설정은 카운터 구현체만 등록하고, **aspect 빈 등록은 `api-common-module`의 `ApiCommonRateLimitAutoConfiguration`이 담당**한다(`@ConditionalOnBean(RateLimitCounterPort.class)` + `afterName`으로 이 모듈 이후 평가). 조건부 등록으로 전환되어 "배선 누락으로 `@RateLimit`이 조용히 무시된다"는 실패 양식은 사라졌지만, 이 경로를 건드렸으면 여전히 한도 초과 호출로 429를 실제 확인한다.
- **Redis를 쓰는 새 관심사는 `security-module`이 아니라 이 모듈을 의존한다.** 그것이 이 모듈을 나눈 이유다.
- **다른 모듈의 계약을 구현할 때는 그 모듈을 이 모듈이 의존한다**(어댑터 → 계약). 반대로 계약 소유 모듈이 이 모듈을 의존하게 만들면 챕터 01이 끊은 역방향 간선이 되살아난다 — 그 회귀는 컴파일·기동 모두 성공하고 **batch 배포 산출물이 조용히 불어나는** 형태로만 드러난다.

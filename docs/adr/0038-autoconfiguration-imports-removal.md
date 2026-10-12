# ADR-0038: AutoConfiguration.imports를 제거하고 앱 ModuleScanConfig 문자열 스캔으로 조립한다

- 상태: 채택
- 결정 커밋: `a400ec42e` (2026-10-04)
- 대체한 결정: [ADR-0017](0017-library-auto-configuration.md)

## 맥락

- **auto-configuration**: Spring Boot가 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 파일에 적힌 설정 클래스를 클래스패스에 있다는 이유만으로 자동으로 로딩하는 장치입니다. `@AutoConfiguration(before/after)`로 순서를, `@ConditionalOnBean`·`@ConditionalOnMissingBean`으로 "다른 빈이 있을 때만" 같은 조건을 걸 수 있습니다.
- **`runtimeOnly` 의존**: 앱이 라이브러리를 실행할 때만 싣고 컴파일할 때는 보지 못하게 하는 Gradle 선언입니다. 앱이 어댑터 타입을 모르게 하는 장치입니다.

[ADR-0017](0017-library-auto-configuration.md)로 라이브러리 모듈은 imports 파일로 스스로를 등록했습니다. 그 결과 "의존 선언 = 활성화"가 되었고, 순서·빈 존재 조건 같은 auto-config 고유 기능에 기대는 설정이 늘었습니다. 앱은 infra를 `runtimeOnly`로 의존하므로 `@Import(Class)`로 되돌아갈 수도 없었습니다.

## 이전 규칙

### backend/CLAUDE.md — 모듈 등록 컨벤션 (auto-configuration — 챕터 02)

- 라이브러리 모듈 13곳이 imports 파일 19개로 자기 등록했고, 설정 클래스는 `{Xxx}ModuleAutoConfiguration`(`@AutoConfiguration` + 자기 패키지 `@ComponentScan`) 20개였습니다.
- 특정 모듈을 끄려면 `spring.autoconfigure.exclude`를 썼습니다.
- "클래스패스 존재 = 활성화" 원칙: imports 파일이 클래스패스에 있으면 무조건 로딩을 시도하므로, 각 auto-configuration은 전이로 끌려온 앱에서 발화해도 안전한지 `@Conditional*`로 스스로 답해야 했습니다.
- 과거 인벤토리의 조건·순서:
  - persistence `PersistenceModuleAutoConfiguration` — `@AutoConfiguration(before = JpaRepositoriesAutoConfiguration.class)`.
  - redis `RedisModuleAutoConfiguration` — `before = RedisAutoConfiguration.class`. "함정 1"은 우리 `stringRedisTemplate`이 Boot의 동명 빈과 겹쳐 `after`로 두면 4앱 전부 기동에 실패한다는 실측 기록이었습니다.
  - security-module — `@ConditionalOnWebApplication(SERVLET)` + `@ConditionalOnMissingBean(JwtAuthenticationFilter.class)`.
  - api-common-module — `ApiCommonModuleAutoConfiguration`이 `@Bean("sharedGlobalExceptionHandler")`를 `@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)`로 등록했습니다. 자체 핸들러가 있는 web-api에서는 조건이 Negative라 물러났습니다. 빈 이름을 `sharedGlobalExceptionHandler`로 구분한 이유는, 조건이 우회돼도 이름 충돌로 기동이 실패해 조용히 덮이지 않게 하려는 것이었습니다.
  - `ApiCommonRateLimitAutoConfiguration` — `@ConditionalOnBean(RateLimitCounterPort.class)` + `afterName`.
  - security-module `SecurityModuleAutoConfiguration`(챕터 02로 `SecurityModuleConfig`에서 리네임) — `@ComponentScan("com.tastyhouse.security")` + `@EnableConfigurationProperties(JwtProperties)` + `@Bean JwtAuthenticationFilter`.
  - logging-module `LoggingModuleAutoConfiguration` — `@ComponentScan("com.tastyhouse.logging")`, 조건 없음.
  - external 계열 driven 14개 — `RestClientModuleAutoConfiguration`(구 `ExternalModuleAutoConfiguration` → `HttpClientModuleAutoConfiguration`)·`FirebaseModuleAutoConfiguration`·`AwsS3ModuleAutoConfiguration`·`AwsSesModuleAutoConfiguration`·`AwsSnsModuleAutoConfiguration`·`{Kakao,Naver,Apple,Facebook}OAuthModuleAutoConfiguration`·`TossPaymentsModuleAutoConfiguration`·`JavaMailModuleAutoConfiguration`·`SolapiModuleAutoConfiguration`·`BbqModuleAutoConfiguration`·`AdmdongkorModuleAutoConfiguration`. 각자 자기 패키지 `@ComponentScan` + Properties 등록, 조건 없음.
  - 그때 `ApiCommonConfig`·`ApiCommonRateLimitConfig`는 두 auto-configuration 클래스가 대체해 삭제된 상태였습니다(imports 제거로 `ApiCommonRateLimitConfig`가 리네임으로 다시 생겼습니다).
- "클래스패스 존재 = 활성화"의 함의로 든 oauth 사례: `@Import` 시절에는 `infrastructure:oauth`를 어떤 앱 `build.gradle`에 추가해도 `OAuthModuleConfig`를 `@Import`하지 않으면 빈이 뜨지 않았으나, auto-configuration 이후에는 실수로 `admin-api`에 추가하면 `OAuthModuleAutoConfiguration`이 즉시 로딩되어 `Could not resolve placeholder 'apple.team-id'`로 기동이 실패했습니다. 이 위험은 imports 제거 후에도 같습니다(앱 스캔이 클래스패스의 infra를 전부 잡으므로).
- `ApiCommon*`의 `@ConditionalOnWebApplication(SERVLET)`이 지킨 대상: 과거 `application → security-core → infrastructure:redis → api-common-module` 전이 사슬이 batch를 포함한 4앱 runtimeClasspath에 있어, non-servlet인 batch에서 두 설정이 스스로 발화를 걸렀습니다. 챕터 01의 토큰 저장소 포트/어댑터 역전으로 사슬이 끊긴 뒤에는 재유입 방어선으로 남았습니다.

### backend/CLAUDE.md — 모듈 등록 컨벤션 > 앱별 runtimeClasspath 감사표 · 함정 2가지

- 감사표는 "어떤 auto-config가 어느 앱에서 발화하는가"를 조건 열과 함께 기록했습니다. imports 제거 후 "발화"는 두 단계(앱 runtimeClasspath에 있는가, 그 패키지를 앱 `ModuleScanConfig`가 스캔하는가)로 읽고, 클래스패스 열(●/—)은 그대로 유효합니다.

| 행 | before | after |
|---|---|---|
| Persistence 조건 | `before = JpaRepositoriesAutoConfiguration` | 없음 — 설정 클래스 삭제, 앱 `com.tastyhouse.infrastructure` 스캔 |
| ApiCommon(예외 핸들러) | web 조건부 Negative / admin·ceo Positive / batch 전이 Negative | web 스캔 안 함 / admin·ceo `apicommon.exception` 스캔 / batch 스캔 안 함, 조건 없음 |
| ApiCommonRateLimit | `@ConditionalOnBean(RateLimitCounterPort)` + SERVLET | web·admin·ceo `apicommon.ratelimit` 스캔, batch 스캔 안 함, SERVLET만 유지 |
| Security | SERVLET 조건 | web·admin·ceo `security` 스캔, batch 스캔 안 함(클래스패스에도 없음), SERVLET 유지 |
| Logging·External·Firebase·벤더·Bbq·Admdongkor·Aws* | 각 imports 파일로 발화 | 클래스패스에 있으면 `infrastructure`/`logging` 스캔으로 등록 |

- 당시 실측(2026-09-05, `java -jar ... --debug`의 `CONDITIONS EVALUATION REPORT`): web은 `sharedGlobalExceptionHandler` Negative·`rateLimitAspect` Positive, admin/ceo는 둘 다 Positive, batch는 `ApiCommon*` 2개 Negative였습니다. Boot `JpaRepositoriesAutoConfiguration`은 4앱 전부 Negative(persistence의 `before` 순서가 이긴 결과)였고, admin·ceo·web 로그인 rate limit을 curl로 10회까지 401·11회째 429로 확인했습니다.
- 함정 1(소멸): 챕터 02 원 스펙은 `RedisModuleAutoConfiguration`을 `after = RedisAutoConfiguration.class`로 설계했으나 실측으로 `before`가 맞았습니다. `after`면 Boot가 `stringRedisTemplate`을 먼저 등록하고 스캔된 `RedisConfig`가 같은 이름으로 다시 등록하려다 `allow-bean-definition-overriding=false`(기본값)에 걸려 4앱 전부 기동에 실패했습니다. 실패 지점은 "Boot의 조건이 물러나지 않아서"가 아니라 "스캔 빈이 auto-config 빈을 덮으려다 override 금지에 걸려서"였습니다. 교훈("Boot 표준 빈과 이름이 겹치면 기본값을 가정하지 말고 실제 기동으로 검증한다")은 유효합니다.

### backend/CLAUDE.md — 컴포지션 루트 규칙 > 앱이 가질 수 있는 조립 코드의 상한

- imports 제거 전 상한은 부트스트랩 중첩 `ApplicationLayerScanConfig` 1개였습니다(그 전에는 `@Import({App}ApplicationConfig)` 한 줄). 라이브러리 모듈의 자기 등록이 사라지며 그 활성화 목록이 앱의 두 번째 중첩 클래스 `ModuleScanConfig`로 올라와 상한이 "중첩 스캔 클래스 2개"가 됐습니다. 기준점 `batch-module`도 `config/` 없이 중첩 클래스 2개만 갖습니다.

### backend/CLAUDE.md — api 모듈 공용 플럼빙 소유 규칙 > 통합된 것

"핸들러 중복 방지는 스캔 범위가 아니라 조건부 `@Bean`이다(챕터 02 개정)" — 이 모듈은 컴포넌트 스캔을 쓰지 않았습니다.

### 각 모듈 AGENTS.md

api-common-module·redis·jpa(당시 persistence)·logging-module·security-module·firebase·oauth·벤더 모듈 문서가 `…ModuleAutoConfiguration`의 이름·조건·`before`/`afterName`을 서술했습니다. 예를 들어 jpa·logging 문서에는 "이 모듈이 앱의 runtimeClasspath에 있으면 자동 활성화된다"는 문장이 있었습니다.

문서별 세부는 다음과 같습니다.

- **앱 모듈(web-api·admin-api·ceo-api·batch-module AGENTS.md)**: 라이브러리 모듈이 각자의 `{Xxx}ModuleAutoConfiguration`으로 자기 자신을 등록하므로 앱은 스캔 대상을 나열하지 않는다고 적었습니다. `infrastructure:persistence`·`infrastructure:redis`는 `PersistenceModuleAutoConfiguration`·`RedisModuleAutoConfiguration`이 자기 등록했습니다. batch는 `PersistenceModuleAutoConfiguration`·`RestClientModuleAutoConfiguration`·`FirebaseModuleAutoConfiguration`·`BbqModuleAutoConfiguration`·`AdmdongkorModuleAutoConfiguration`·`LoggingModuleAutoConfiguration`이 전부 imports 파일로 발화했습니다. web의 "핸들러가 2개가 되는 것은 조건이 막는다(실측 Negative)", web·admin의 "aspect는 `ApiCommonRateLimitAutoConfiguration`이 `RateLimitCounterPort` 빈 존재를 조건으로 자동 등록한다"도 이 시절 서술입니다.
- **벤더·스타터 모듈 AGENTS.md**: 진입 설정 `{X}ModuleAutoConfiguration`이 `@ComponentScan(자기 패키지)` + `@EnableConfigurationProperties`를 가졌고, "진입 설정은 자기 (하위) 패키지만 스캔한다 — `com.tastyhouse.infrastructure`나 `com.tastyhouse.infrastructure.aws` 루트로 넓히면 같은 클래스패스의 형제 모듈 빈까지 이 설정이 등록한다"는 봉인이 있었습니다(bbq·admdongkor·aws-s3·aws-ses·aws-sns). 분리 전 `CrawlingModuleAutoConfiguration`은 `external.crawling`과 `external.region` 두 패키지를 함께 스캔했습니다. pg 문서는 벤더 설정 `TossPaymentsModuleAutoConfiguration`이 조건이 없어 `--debug` 리포트의 "Unconditional classes" 절에 나온다고 적었습니다. file-storage·firebase 문서는 firebase의 auto-configuration이 클래스패스 존재만으로 빈을 등록한다고 적었습니다.
- **application-core AGENTS.md**: write 어댑터 `XxxPersistenceAdapter`는 `PersistenceModuleAutoConfiguration`의 스캔으로 4앱에 뜬다고 적었습니다.
- **jpa AGENTS.md(당시 persistence)**: "모듈 auto-configuration의 두 설정을 건드리지 않는다" 봉인 — `PersistenceModuleAutoConfiguration`의 `infrastructure.redis` 제외(이후 [ADR-0037](0037-infrastructure-package-root.md)로 삭제)와 `before = JpaRepositoriesAutoConfiguration`. `before`를 빼면 스캔 안 `InfrastructurePersistenceConfig`의 `@EnableJpaRepositories`가 Boot보다 늦게 처리돼 Boot 쪽 `@ConditionalOnMissingBean(JpaRepositoryConfigExtension)`이 물러나지 않는다는 이유였습니다. 모듈을 끄려면 `spring.autoconfigure.exclude`를 쓴다고 적었습니다. `PersistenceModuleAutoConfiguration`은 imports 파일이 FQCN으로 참조하므로 경로 변경 금지라는 봉인도 있었습니다.
- **api-common-module AGENTS.md**:
  - `ApiCommonModuleAutoConfiguration`은 컴포넌트 스캔을 쓰지 않고 앱별로 켜고 꺼야 하는 빈만 조건부 `@Bean`으로 등록했습니다. 빈 이름 `sharedGlobalExceptionHandler`를 기본 이름으로 되돌리지 않는다는 봉인이 있었습니다(조건이 우회돼도 이름 충돌로 기동이 실패해 조용히 덮이지 않게).
  - `ApiCommonRateLimitAutoConfiguration`은 `@AutoConfiguration(afterName = "com.tastyhouse.infrastructure.redis.RedisModuleAutoConfiguration")`로 순서를 문자열로 선언했습니다. api-common의 main 클래스패스에서 redis 타입을 볼 수 없었기 때문입니다(초기에는 `infrastructure:redis → api-common-module` 방향이라 순환이었습니다). 이 문자열은 리네임해도 컴파일이 깨지지 않으므로 `ApiCommonAutoConfigurationTest#rateLimitAutoConfigurationAfterNameResolvesToRealClass`가 유일한 방어선이었고, 그 테스트는 원래 redis의 `RedisModuleAutoConfigurationTest`에 있다가 `testImplementation project(':infrastructure:redis')`와 함께 api-common으로 옮겨왔습니다.
  - "`@ConditionalOnWebApplication(SERVLET)` 두 건 — 재유입 방어선" 봉인은 두 auto-configuration 클래스를 대상으로 했습니다.
- **restclient AGENTS.md**: 진입 설정 `RestClientModuleAutoConfiguration`(구 `ExternalModuleAutoConfiguration` → `HttpClientModuleAutoConfiguration`)이 `com.tastyhouse.infrastructure.restclient` 한 패키지를 스캔했고 `@EnableConfigurationProperties`는 없었습니다. 형제 모듈도 자기 auto-configuration(`FirebaseModuleAutoConfiguration` 등, 당시 `PgModuleAutoConfiguration`·`MessagingModuleAutoConfiguration` 포함)으로 자기 등록한다고 적었습니다.
- **redis AGENTS.md**: `RedisModuleAutoConfiguration`(챕터 02로 `RedisModuleConfig`에서 리네임)의 "`before`를 `after`로 바꾸지 않는다" 봉인과 "auto-config는 전이 존재 = 활성화다" 절이 있었습니다. 순서 가드는 `RedisModuleAutoConfigurationTest`였습니다. "빈 정의 순서와 생성 순서는 다르다"(`before`여도 `RedisConnectionFactory`는 생성 시점에 주입된다)는 `before` 속성을 대상으로 적혀 있었습니다.
- **security-core·security-module AGENTS.md**: `SecurityModuleAutoConfiguration`이 `AutoConfiguration.imports`에 FQCN 1줄로 자기 등록하고 `@ComponentScan("com.tastyhouse.security")`로 security-core의 같은 패키지 빈까지 잡았습니다. `jwtAuthenticationFilter`의 `@ConditionalOnMissingBean(JwtAuthenticationFilter.class)`는 앱이 자기 필터를 등록해 덮을 때의 escape hatch로 서술됐습니다(그런 앱은 없었습니다). 이 조건이 `JwtTokenProvider` 모호성은 막지 못한다(해법은 `@Primary`나 한정자뿐)는 결론은 지금도 유효합니다.
- **logging-module AGENTS.md**: "`LoggingModuleAutoConfiguration`을 끄는 방법" 절은 `spring.autoconfigure.exclude`를 안내했습니다.

## 결정

라이브러리 모듈은 스스로를 등록하지 않습니다. 각 앱 부트스트랩의 static 중첩 `ModuleScanConfig`가 라이브러리 모듈 패키지를 **문자열로** 스캔합니다.

| 항목 | before (auto-configuration) | after (imports 제거) |
|---|---|---|
| 라이브러리 모듈을 활성화하는 주체 | 각 모듈의 imports 파일 | 각 앱 부트스트랩의 static 중첩 `ModuleScanConfig` |
| imports 파일 | 19개 | 0개 |
| 설정 클래스 | `{Xxx}ModuleAutoConfiguration` 20개 | `{Xxx}ModuleConfig`(`@Configuration(proxyBeanMethods = false)`, 스캔 없음) 13개, 7개 삭제 |
| 특정 모듈 끄기 | `spring.autoconfigure.exclude` | 그 앱 `ModuleScanConfig` 목록에서 패키지를 뺀다 |
| 빈 존재 조건(`@ConditionalOnBean`/`OnMissingBean`) | 사용 | 사용 금지 |
| 환경 조건(`@ConditionalOnWebApplication`/`OnProperty`) | 사용 | 유지 |
| 공용 예외 핸들러 중복 방지 | 조건부 `@Bean("sharedGlobalExceptionHandler")` | 스캔 범위 — admin·ceo만 `apicommon.exception`을 스캔, 빈 이름 `globalExceptionHandler` |
| Redis·JPA `before` 순서 | 필수 | 불필요 — 사용자 설정은 언제나 auto-config보다 먼저 처리된다 |

## 근거

- 앱이 infra를 `runtimeOnly`로 의존하므로 `@Import(Class)`는 불가능합니다. 문자열 스캔은 컴파일 클래스패스가 필요 없어 헥사고날 경계("앱은 어댑터를 모른다")가 유지됩니다(커밋 메시지).
- 일반 `@Configuration`에서 빈 존재 조건은 처리 순서에 좌우돼 조용히 틀립니다. 그래서 조건 대신 앱별 스캔 범위로 대체했습니다. 환경으로 판정하는 조건은 일반 설정에서도 정확해 남겼습니다.
- 사용자 설정이 auto-config보다 항상 먼저 처리되므로, 우리 `stringRedisTemplate`이 먼저 등록되고 Boot의 `@ConditionalOnMissingBean`이 물러납니다. `before` 선언이 필요 없어졌습니다.

## 결과·감수한 손실

- 동작(HTTP·DB)은 바뀌지 않았습니다. 4앱 빈 집합 diff에서 설정 클래스 빈과 `sharedGlobalExceptionHandler`→`globalExceptionHandler`(admin·ceo, 주입처 없음) 외 차이는 0이었습니다.
- "클래스패스 존재 = 활성화" 원칙 자체는 남았습니다. 앱 `ModuleScanConfig`가 `com.tastyhouse.infrastructure`를 통째로 스캔하므로 클래스패스에 올라온 infra 모듈은 전부 활성화됩니다. 바뀐 것은 활성화를 결정하는 주체입니다.
- 삭제한 조건: rate limit aspect의 `@ConditionalOnBean(RateLimitCounterPort)` + `afterName`(스캔하는 web·admin·ceo는 전부 Redis 카운터를 가짐), JWT 필터의 `@ConditionalOnMissingBean`(앱이 자체 필터 빈을 정의하지 않음). api-common의 `afterName` 검증용 redis `testImplementation`도 함께 삭제됐습니다.
- `@RateLimit`의 앱 배선은 0줄이 아니라 `ModuleScanConfig`의 `"com.tastyhouse.apicommon.ratelimit"` 문자열 1항목이 됐습니다.
- batch에서의 재유입 방어선은 `@ConditionalOnWebApplication(SERVLET)`보다 먼저 batch `ModuleScanConfig`가 `apicommon`·`security`를 스캔하지 않는 것이 맡습니다.
- 가드: imports 재유입·스캔 목록 정확 일치·패키지 실재를 4앱 테스트(`ApplicationLayerScanConfigTest` 등)가 검사합니다. imports 재유입은 `assertNoTastyhouseAutoConfiguration`, 스캔 목록은 `assertScansModulesWithoutFilters`가 맡습니다.
- 삭제된 설정 클래스 7개는 `PersistenceModuleAutoConfiguration`·`LoggingModuleAutoConfiguration`·`ApiCommonModuleAutoConfiguration`·`RestClientModuleAutoConfiguration`·`AwsSesModuleAutoConfiguration`·`AwsSnsModuleAutoConfiguration`·`JavaMailModuleAutoConfiguration`입니다. 나머지는 `{Xxx}ModuleConfig`로 리네임돼 `@EnableConfigurationProperties`(security는 `@Bean JwtAuthenticationFilter`까지)만 남았습니다. `RedisModuleConfig`·`SecurityModuleConfig`는 챕터 02 이전 원래 이름으로 돌아간 것입니다.
- 모듈 설정 클래스는 아무것도 스캔하지 않습니다. 앱이 `com.tastyhouse.infrastructure`를 통째로 스캔하므로 같은 클래스패스의 형제 모듈 빈이 함께 등록되는 것이 정상이 됐고, 과거 "자기 패키지만 스캔한다" 봉인은 대상이 사라졌습니다. 형제 모듈을 빼려면 의존(조립 스타터의 `runtimeOnly`)에서 빼야 하며, 모듈에 `@ComponentScan`을 되살리면 앱 스캔과 이중이 됩니다. jpa 모듈만 골라 스캔에서 빼는 수단도 없어졌습니다(4앱 전부 DB를 쓰므로 필요도 없습니다).
- 특정 모듈을 끄는 방법이 `spring.autoconfigure.exclude`에서 "그 앱 `ModuleScanConfig`에서 패키지를 빼고 그 앱 `ApplicationLayerScanConfigTest` 기대 목록도 함께 고친다"로 바뀌었습니다(logging 등).
- 벤더 설정이 일반 `@Configuration`이 되어 `--debug` 조건 리포트에 나오지 않습니다. 배선 확인은 `--logging.level.org.springframework.beans.factory.support=DEBUG`의 autowiring 로그로 합니다(pg 문서).
- 테스트 정리: redis의 `RedisModuleAutoConfigurationTest`는 `RedisModuleConfigTest`로 리네임되어 `ourStringRedisTemplateWinsOverBootDefault`(`withUserConfiguration(RedisModuleConfig.class, RedisConfig.class)` + `AutoConfigurations.of(RedisAutoConfiguration.class)`) 한 건이 순서를 지킵니다. 이 모듈의 빈을 다시 auto-configuration으로 등록하면 순서 문제가 되살아나므로, 그것도 imports를 되살리지 않는 이유입니다. api-common의 `ApiCommonAutoConfigurationTest`는 `ApiCommonRateLimitConfigTest`로 옮겨지며 `rateLimitAspectRegisteredInServletContext`·`rateLimitAspectAbsentInNonServletContext` 2건만 남고 4건(`afterName` 검증·핸들러 back-off·aspect 빈 존재 조건 등)이 삭제됐습니다. api-common `LayerRulesTest`의 클래스 수 하한도 14 → 13으로 내려갔습니다.
- JWT 필터 빈 존재 조건이 사라져, 앱이 자기 필터를 등록하면 이름이 겹쳐 기동이 실패합니다(조용히 덮이지 않음). 예외 핸들러도 같습니다 — web이 실수로 `apicommon.exception`을 스캔하면 자체 핸들러와 기본 이름 `globalExceptionHandler`가 겹쳐 기동이 실패하고, 그 전에 `ApplicationLayerScanConfigTest`가 실패합니다. 원래 `sharedGlobalExceptionHandler` 봉인의 취지("조용히 덮이지 않는다")가 이렇게 유지됩니다.
- restclient에 Properties record를 다시 추가한다면 루트 패키지에 `RestClientModuleConfig`(`@Configuration(proxyBeanMethods = false)`, 스캔 없음)를 새로 두고 그 `@EnableConfigurationProperties`에 명시합니다.

## 영향받은 문서

- `backend/CLAUDE.md` · 모듈 등록 컨벤션 (auto-configuration — 챕터 02) · 제목은 앵커 보존용, 지금은 자기 등록이 없다
- `backend/CLAUDE.md` · 모듈 등록 컨벤션 > 현재 규칙 — 새 라이브러리 모듈·패키지를 추가할 때 (imports 제거) · 등록 절차
- `backend/CLAUDE.md` · "모듈 등록 컨벤션 (auto-configuration — 챕터 02)" > `"클래스패스 존재 = 활성화" 원칙` · 주체만 앱 스캔 목록으로 바뀜
- `backend/CLAUDE.md` · 모듈 등록 컨벤션 > 모듈별 설정 클래스 인벤토리 · `{Xxx}ModuleConfig` 현행 표
- `backend/CLAUDE.md` · 모듈 등록 컨벤션 > 후속 작업자가 밟기 쉬운 함정 2가지 · Boot 표준 빈과 이름이 겹치면 실제 기동으로 검증
- `backend/CLAUDE.md` · api 모듈 공용 플럼빙 소유 규칙 > 통합된 것 · 핸들러 중복 방지는 스캔 범위
- `backend/CLAUDE.md` · 컴포지션 루트 규칙 > 앱이 가질 수 있는 조립 코드의 상한 · 부트스트랩 중첩 스캔 클래스 2개
- `backend/AGENTS.md` · Module Dependency Graph · 앱 `ModuleScanConfig` 조립
- `backend/api-common-module/AGENTS.md` · 등록 방식 — 앱 `ModuleScanConfig`의 패키지 스캔 / 봉인·가드 목록 > `@ConditionalOnWebApplication(SERVLET)` — 재유입 방어선이므로 제거하지 않는다
- `backend/{web,admin,ceo}-api/AGENTS.md`, `backend/batch-module/AGENTS.md` · "Dependencies" > "Internal"(api 3앱) / "빈 배선 — 부트스트랩 중첩 스캔 설정"(batch) · 부트스트랩 중첩 `ModuleScanConfig` 서술
- `backend/infrastructure/firebase/AGENTS.md` · "진입 설정과 스캔 범위" · 진입 설정과 스캔 범위 서술
- `backend/infrastructure/{admdongkor,aws-ses,aws-sns}/AGENTS.md` · "진입 설정은 아무것도 스캔하지 않는다" · 같은 취지
- `backend/infrastructure/pg/AGENTS.md` · "진입 설정이 벤더 클래스를 참조하지 않는다, 벤더는 이 모듈을 의존하지 않는다" · 같은 취지
- `backend/infrastructure/jpa/AGENTS.md` · "모듈 스캔·auto-configuration" · 모듈 설정 클래스와 스캔 범위 서술
- `backend/infrastructure/restclient/AGENTS.md` · "코어와 형제 모듈의 빈 등록" · 같은 취지

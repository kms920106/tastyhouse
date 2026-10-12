# ADR-0017: 라이브러리 모듈을 Spring Boot auto-configuration으로 자기 등록한다(챕터 02)

- 상태: 대체됨(→ [ADR-0038](0038-autoconfiguration-imports-removal.md))
- 결정 커밋: `cad5e5880` (2026-09-05)
- 대체한 결정: —

## 맥락

- **라이브러리 모듈**: 실행 앱이 아닌 모듈(persistence·redis·external·security·logging·api-common 등).
- **auto-configuration**: 모듈이 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 파일로 자기 설정 클래스를 선언하면, Spring Boot가 클래스패스에서 그 파일을 찾아 자동으로 로딩하는 방식.

이전에는 4개 실행 앱의 `{Xxx}Application.java`가 `@Import({InfrastructureModuleConfig, RedisModuleConfig, ExternalModuleConfig, ...})`로 라이브러리 모듈 설정 클래스를 일일이 나열했다(`a18a7af92`, 2026-08-06). `@Import(Class)`는 클래스를 컴파일 타임에 참조하므로 앱이 라이브러리 모듈을 `implementation`으로 의존해야 했다.

## 이전 규칙

### `backend/AGENTS.md` — Module Dependency Graph

모듈 등록은 `scanBasePackages`/`@Import` 조합이다. 앱의 `scanBasePackages`에는 각 앱 자신 + `com.tastyhouse.infrastructure`·`com.tastyhouse.external`·`com.tastyhouse.security`(web/admin/ceo)·`com.tastyhouse.logging`이 나열돼 있었다.

### `backend/api-common-module/AGENTS.md`

"부분 진입점 스캔" 구조 — `ApiCommonConfig` 전체 스캔 vs `ApiCommonFileConfig`+`ApiCommonRateLimitConfig` 부분 스캔. 부분 진입점을 쓰는 앱(web)에 패키지를 추가할 때는 그 앱의 `@Import`도 함께 늘려야 했다.

아래는 이 ADR이 세운 auto-configuration 시절 규칙의 요지다. [ADR-0038](0038-autoconfiguration-imports-removal.md)로 규칙 문서에서 지워졌으므로 여기에 남긴다.

### `backend/CLAUDE.md` — 모듈 등록 컨벤션 (auto-configuration — 챕터 02) > 컨벤션 본문

- **클래스 명명 `{Xxx}ModuleAutoConfiguration`**: 모듈 루트 패키지에 1개(`@AutoConfiguration(proxyBeanMethods = false)`, 필요시 `before`/`after`/`afterName`). `Module` 중간어는 Boot 자신의 `RedisAutoConfiguration`·`SecurityAutoConfiguration`·`JpaRepositoriesAutoConfiguration`과 단순명이 충돌하지 않게 하려고 붙였다. `persistence`만 `InfrastructureModuleConfig` → `PersistenceModuleAutoConfiguration`, 다른 12개는 `{Xxx}ModuleConfig` → `{Xxx}ModuleAutoConfiguration`으로 기계적으로 리네임했다.
- **imports 파일**: 각 모듈 `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`에 auto-configuration FQCN을 한 줄로 선언했다.
- **스캔 범위는 언제나 그 모듈의 루트 패키지 하나** — 다른 모듈의 패키지를 덮는 스캔과 그것을 되돌리는 `excludeFilters`를 두지 않는다(예: `PersistenceModuleAutoConfiguration`이 `@ComponentScan("com.tastyhouse.infrastructure.persistence")`). Boot 권고와 달리 auto-configuration 안에서 스캔하면 스캔된 컴포넌트의 `@Conditional*`이 `java -jar ... --debug`의 `CONDITIONS EVALUATION REPORT`에 나타나지 않는다.
- **앱별로 켜고 끄는 빈만 조건부 `@Bean`** — 그런 대상은 api-common의 2개뿐이었고, 나머지는 스캔으로 충분했다.
- **`@EnableConfigurationProperties` 명시 등록 유지**, **모듈 yml은 앱 `spring.config.import`로 로딩** — 자동 로딩은 `EnvironmentPostProcessor`가 필요해 범위 밖이었고, `@PropertySource`로는 `logging.*`이나 `optional:configtree:` 같은 특수 프로퍼티 소스가 동작하지 않는다. 이 두 항목은 지금도 유지된다.
- **escape hatch `spring.autoconfigure.exclude`**: 특정 환경에서 auto-configuration을 끄려면 이 표준 Boot 프로퍼티를 쓰고, 별도 온오프 스위치는 만들지 않는다(프로퍼티 스위치를 새로 만들면 보안 회귀를 반복하기 쉽다).
- **auto-config를 갖지 않는 예외 5건**: `application`(처음에는 `{App}ApplicationConfig`가 `@Import`되는 대상이라 자기 등록 대상이 아니었고, 이후 스캔이 앱 부트스트랩의 중첩 `ApplicationLayerScanConfig`로 옮겨져 등록 클래스 자체가 없어졌다) · `security-core`(설정 클래스가 없다 — `JwtTokenProvider`는 앱별 하위 클래스가 `@Component`로, 토큰 저장소 구현 어댑터는 `RedisModuleAutoConfiguration`이 등록하고, `JwtProperties`만 `security-module`의 `@EnableConfigurationProperties`가 등록) · `domain`(프레임워크-프리) · `infrastructure:file-storage`(코드 없는 스타터 — 빈 등록은 firebase의 auto-configuration) · `infrastructure:oauth`(코드 없는 스타터 — 옛 `OAuthModuleAutoConfiguration` 삭제, 빈 등록은 벤더 4종의 `{Kakao,Naver,Apple,Facebook}OAuthModuleAutoConfiguration`). 앱 마커 제거로 생긴 `{web,admin,ceo,batch}-application`도 같은 이유로 auto-config가 없었다. imports 제거 후에는 이 구분이 소멸했고, persistence·logging·restclient·aws-ses·aws-sns·javamail·api-common(공용 핸들러) 7곳이 설정 클래스 삭제로 합류했다.

### `backend/api-common-module/AGENTS.md` — 등록 방식 — auto-configuration (챕터 02)

- `ApiCommonConfig`·`ApiCommonRateLimitConfig` 두 `@Configuration` 진입점이 삭제되고 두 `@AutoConfiguration`이 자기 등록했다.

  | 클래스 | 패키지 | 등록 | 조건 |
  |---|---|---|---|
  | `ApiCommonModuleAutoConfiguration` | `com.tastyhouse.apicommon` | `@Bean("sharedGlobalExceptionHandler") GlobalExceptionHandler`(과거 `@RestControllerAdvice` 스캔에서 전환) | `@ConditionalOnWebApplication(type = SERVLET)` + `@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)` |
  | `ApiCommonRateLimitAutoConfiguration` | `com.tastyhouse.apicommon.ratelimit` | `@Bean RateLimitAspect(RateLimitCounterPort, ClientIpResolver, ...)` — `RateLimitAspect`의 `@Component` 제거 | `@ConditionalOnWebApplication(type = SERVLET)` + `@ConditionalOnBean(RateLimitCounterPort.class)` + `@AutoConfiguration(afterName = "com.tastyhouse.infrastructure.redis.RedisModuleAutoConfiguration")` |

- 빈 이름을 `sharedGlobalExceptionHandler`로 지정한 이유: web-api의 자체 `GlobalExceptionHandler`와 단순 클래스명이 같아 기본 이름(`globalExceptionHandler`)이면 이름 충돌한다. 이름을 달리 지어 공존시키고 조건이 등록 여부를 갈랐다.
- `RateLimitCounterPort`로 조건을 건 이유: 이 모듈 main 클래스패스에 redis 모듈이 없어 `@ConditionalOnBean(RedisRateLimitCounter.class)` 같은 클래스 리터럴을 쓸 수 없었다. `afterName`은 스캔된 `RedisRateLimitCounter` 정의가 redis auto-configuration 처리 시점에 등록되므로 그보다 먼저 평가하면 거짓으로 판정되기 때문이었다. 이 `afterName` 문자열을 검증하는 테스트 때문에 api-common은 `infrastructure:redis`를 `testImplementation`으로만 의존했다.
- **앱별 등록 현황(스캔 주의 절)**: admin/ceo는 `sharedGlobalExceptionHandler`·`rateLimitAspect` 둘 다 Positive, web은 핸들러 Negative(자체 `webapi.exception.GlobalExceptionHandler`)·aspect Positive, batch는 둘 다 Negative(`spring.main.web-application-type: none`)였다.

### `backend/AGENTS.md` — Module Dependency Graph (`implementation` 잔류 사유)

`runtimeOnly` 강등에서 `security-module`·`api-common-module`만 `implementation`으로 남긴 이유는 소비 측이 그 모듈의 구체 타입(`TokenService`가 쓰는 `JwtTokenProvider`, api-common의 공용 record 등)을 컴파일 타임에 직접 참조하기 때문이라고 적었다.

## 결정

| 항목 | before | after |
|---|---|---|
| 모듈 설정 클래스 | `{Xxx}ModuleConfig`(`persistence`는 `InfrastructureModuleConfig`) | `{Xxx}ModuleAutoConfiguration`(`@AutoConfiguration`, persistence는 `PersistenceModuleAutoConfiguration`) — 13개 |
| 등록 주체 | 앱 `@Import` 나열 | 모듈의 `AutoConfiguration.imports` 자기 등록. 앱 `@Import`는 `{App}ApplicationConfig` 하나만 |
| 앱 → 라이브러리 의존 | `implementation` | `runtimeOnly`(`security-module`·`api-common-module`만 `implementation` 유지) |
| `scanBasePackages` | 4앱에 나열 | 4앱 전부에서 사라짐 |
| 빈 발견 | — | auto-configuration 클래스가 자기 모듈 루트 패키지 하나를 `@ComponentScan` |
| 앱별 차이 | 스캔 범위·`@Import` | 조건부 `@Bean`(`@ConditionalOnWebApplication`·`@ConditionalOnBean`·`@ConditionalOnMissingBean`) |
| api-common 공용 예외 핸들러 | 부분 스캔 | `@Bean("sharedGlobalExceptionHandler")` + `@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)` — web에서 스스로 물러남 |
| `RateLimitAspect` | 컴포넌트 스캔 | `@Bean` + `@ConditionalOnBean(RateLimitCounterPort.class)` + `afterName = "…RedisModuleAutoConfiguration"` |

새 원칙은 **"클래스패스 존재 = 활성화"** 다. 모듈을 앱에 붙이는 비용은 "gradle 1줄 + (설정이 있으면) yml import 1줄"이다.

## 근거

- 앱이 모듈 설정 클래스를 몰라도 되므로 라이브러리 의존을 `runtimeOnly`로 내릴 수 있다 — 앱 컴파일 클래스패스에서 인프라가 빠진다.
- 앱의 `@Import` 누락이라는 실패 양식이 사라진다 — 과거 web이 부분 진입점만 import해 `@RateLimit`이 조용히 무시되는 구멍이 있었다([ADR-0011](0011-rate-limit-port-in-api-common.md)).
- 앱별 차이를 스캔 범위가 아니라 조건으로 표현하면, 전이로 끌려온 앱에서도 각 auto-configuration이 스스로 발화 여부를 답한다.
- Spring Boot 레퍼런스는 auto-configuration 안의 컴포넌트 스캔을 권장하지 않는다(조건 평가 리포트에 안 나타나고 범위 오버라이드가 어렵다). 내부 전용 모듈이고 `@Repository` 수백 개를 일일이 `@Bean`으로 열거하는 것이 현실적이지 않아 알고도 채택했다(원문).

## 결과·감수한 손실

- 동작 변경 없음 — 앱별 빈 집합 동일(감사표·조건 리포트·로그인 rate limit curl로 증명, 커밋 메시지).
- Redis 함정: 원 스펙의 `after = RedisAutoConfiguration`으로는 우리 `stringRedisTemplate`이 Boot 동명 빈과 겹쳐 4앱 전부 기동이 실패했다. `before`로 바꿨다. `@Import` 시절에는 사용자 설정이 항상 먼저 처리돼 없던 문제다.
- `runtimeOnly` 강등으로 앱 소유 `RedisRepositoryConfig`가 쓰던 `StringRedisTemplate` 전이 의존이 끊겨 web/admin/ceo에 `spring-boot-starter-data-redis`를 명시 선언했다.
- 일반 설정의 `@ConditionalOnBean`은 순서 의존이라 `afterName` 같은 순서 강제가 필요했다.
- **대체**: `AutoConfiguration.imports` 19개는 전부 삭제됐다. 지금은 각 앱 부트스트랩의 중첩 `ModuleScanConfig`가 라이브러리 패키지를 문자열로 스캔하고, 모듈 설정 클래스는 `{Xxx}ModuleConfig`(스캔 없음)이며 빈 존재 조건을 쓰지 않는다([ADR-0038](0038-autoconfiguration-imports-removal.md)). "클래스패스 존재 = 활성화"와 `runtimeOnly` 의존은 유지된다.

## 영향받은 문서

- `backend/CLAUDE.md` · "모듈 등록 컨벤션 (auto-configuration — 챕터 02)" > "현재 규칙 — 새 라이브러리 모듈·패키지를 추가할 때 (imports 제거)" · "컨벤션 본문" · `"클래스패스 존재 = 활성화" 원칙` · 현재 등록 방식
- `backend/CLAUDE.md` · 같은 절 > "앱별 의존 (전환 후 — `runtimeOnly`로 하향)" · "후속 작업자가 밟기 쉬운 함정 2가지" · `runtimeOnly` 의존과 남은 함정
- `backend/AGENTS.md` · "Module Dependency Graph" · 앱의 라이브러리 의존은 `runtimeOnly`, 등록은 `ModuleScanConfig`
- `backend/api-common-module/AGENTS.md` · "등록 방식 — 앱 `ModuleScanConfig`의 패키지 스캔" · "앱별 등록 현황" · 현재 등록 방식
- `backend/batch-module/AGENTS.md` · "빈 배선 — 부트스트랩 중첩 스캔 설정" > "`web-application-type: none`은 **재유입 방어선**이다" · batch의 빈 배선

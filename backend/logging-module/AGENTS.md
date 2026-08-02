# logging-module

`web-api`/`admin-api`/`ceo-api`/`batch-module` 네 실행 모듈이 공통으로 쓰는 **횡단 관심사(cross-cutting concern) 제공 모듈**. API 요청/응답 메타 로깅, 컨트롤러 진입 시 인증 사용자·요청 바디 로깅, 로그 출력 전 민감 필드 마스킹을 담당한다. `external-api`가 OAuth/결제/파일 등 외부 연동 어댑터를, `infrastructure-module`이 persistence·조회 어댑터를 캡슐화하는 것과 같은 원리로, 로깅 관련 구현체를 domain/presentation 밖으로 분리해 각 실행 모듈이 로깅 코드를 중복 작성하지 않도록 한다.

## 패키지 구조

```
com.tastyhouse.logging/
├── ApiLoggingFilter.java       OncePerRequestFilter — requestId(MDC)/Method/Path/IP/상태코드/처리시간 로깅, DEBUG 레벨에서 요청·응답 바디 로깅
├── ApiLoggingAspect.java       @Around(@RestController 전체) — 인증 사용자(username)·@RequestBody 인자 로깅
└── SensitiveFieldMasker.java   password/token/인증코드/카드정보 등 민감 필드를 마스킹 처리하는 유틸 컴포넌트
```

- `ApiLoggingFilter`는 `@Order(Ordered.HIGHEST_PRECEDENCE)`로 필터 체인 최상단에서 동작하며, `requestId`를 MDC에 등록해 같은 요청에서 발생하는 모든 로그(p6spy 포함)에 자동으로 첨부되게 한다.
- `ApiLoggingAspect`는 `@RestController`가 붙은 모든 클래스에 AOP로 적용되며, Filter 레이어에서 처리되는 401/403 등은 컨트롤러에 도달하지 않으므로 별도 로깅되지 않는다.
- `SensitiveFieldMasker`는 현재 `ApiLoggingAspect`에서 실사용이 주석 처리된 상태로 남아 있다(`SENSITIVE_FIELDS`에 정의된 필드명 기준 마스킹 로직 자체는 완성돼 있음) — 활성화하려면 `ApiLoggingAspect`의 주석 처리된 `masker` 필드·호출부를 복원한다.

## 왜 `web`/`aop`/`security` starter를 `api`로 노출하는가

`build.gradle`은 아래처럼 세 starter를 `api`(전이 의존)로 노출한다:

```gradle
dependencies {
    api 'org.springframework.boot:spring-boot-starter-web'
    api 'org.springframework.boot:spring-boot-starter-aop'
    api 'org.springframework.boot:spring-boot-starter-security'
}
```

이 모듈의 클래스들(`OncePerRequestFilter`, `@Aspect`, `SecurityContextHolder`)이 `web`/`aop`/`security`를 직접 사용하므로, 그 의존성을 `api`로 선언하면 **이 모듈을 `implementation`으로 의존하는 소비 모듈(`web-api`/`admin-api`/`ceo-api`/`batch-module`)이 같은 starter를 각자 별도로 선언하지 않아도** 전이적으로 클래스패스에 올라온다. 즉 "로깅 관련 기능에 필요한 프레임워크 의존을 이 모듈 하나로 대표한다"는 설계다. 실제로 `admin-api/build.gradle`은 `spring-boot-starter-aop`를 명시적으로도 갖고 있는데, 이는 이 모듈의 `api` 노출과 별개로 admin-api 자체 AOP 코드(예: `ratelimit/RateLimitAspect`)가 직접 선언한 것이다 — 소비 모듈이 이 모듈의 `api` 노출에만 의존하지 않고 자기 필요에 따라 동일 starter를 중복 선언하는 것 자체는 금지되지 않는다.

## 규칙

- **패키지 루트는 `com.tastyhouse.logging`** — `web-api`/`admin-api`/`ceo-api`/`batch-module`의 `scanBasePackages`(admin/ceo는 `@ComponentScan basePackages`에도)에 이 패키지가 등록되어 있어야 `ApiLoggingFilter`/`ApiLoggingAspect`가 빈으로 인식된다.
- **실행 가능한 애플리케이션이 아니다**: `bootJar`는 비활성화하고 일반 `jar`만 생성한다(`domain-module`/`infrastructure-module`/`external-api`/`security-module`과 동일한 라이브러리 모듈 패턴).
- **바디 로깅은 DEBUG 레벨에서만 활성화**된다 — `com.tastyhouse.logging` 레벨이 `DEBUG`일 때만 요청/응답 바디가 로깅된다. 운영 환경에서 바디가 로그에 그대로 남지 않도록 하는 안전장치이므로, 로그 레벨 설정을 변경할 때 이 전제를 깨지 않도록 주의한다. 이 레벨은 아래 `application-logging.yml`에서 `${API_BODY_LOG_LEVEL:DEBUG}`로 환경변수화되어 있어, 운영에서는 `API_BODY_LOG_LEVEL=INFO`만 지정하면 코드 수정·재빌드 없이 바디 로깅을 끌 수 있다(로컬 기본값은 DEBUG).
- **민감 필드 마스킹 목록(`SensitiveFieldMasker.SENSITIVE_FIELDS`)은 신규 민감 필드 추가 시 함께 갱신**한다. 마스킹이 실사용에 연결되지 않은 현재 상태에서 목록만 갱신해도 즉시 효과는 없으므로, 마스킹을 실제로 적용하려면 `ApiLoggingAspect`의 활성화가 선행되어야 한다.
- **`domain-module` 의존 없음**: 이 모듈은 순수 횡단 관심사(로깅/필터/AOP)만 다루며 도메인 모델이나 application 서비스에 의존하지 않는다(사내 모듈 의존이 0인 유일한 모듈).

## 로깅 설정 소유 (`application-logging.yml`)

이 모듈은 로깅 관련 **코드**뿐 아니라 **설정**도 소유한다. 과거 `web-api`/`admin-api`/`ceo-api` 3개 실행 모듈의 `application.yml`에 동일하게 복제돼 있던 `logging:` 블록(콘솔 패턴·root 레벨·`com.tastyhouse.logging` 레벨)과 각 모듈 `src/main/resources/spy.properties`(p6spy SQL 로그 포맷)를 이 모듈의 `src/main/resources/application-logging.yml` 하나로 통합했다. 이는 `security-module`이 `application-security.yml`을, `infrastructure-module`이 `application-infrastructure.yml`(과거 `core-module`의 `application-core.yml`이었다가 이동)을 소유하고 실행 모듈이 `spring.config.import`로 로딩하는 기존 컨벤션(루트 CLAUDE.md "모듈 경계 규칙 — 설정값도 같은 패턴")의 반복 적용이다.

- **소유 항목**: 콘솔 로그 패턴(`requestId` MDC 포함), `root: INFO`, `com.tastyhouse.logging: ${API_BODY_LOG_LEVEL:DEBUG}`, p6spy 로그 포맷(`decorator.datasource.p6spy.log-format: "%(sql)"`).
- **spy.properties 폐지**: 과거 `appender=Slf4JLogger`/`logMessageFormat=CustomLineFormat`/`customLogMessageFormat=%(sql)` 4줄을 `p6spy-spring-boot-starter`의 `decorator.datasource.p6spy.log-format` 프로퍼티로 흡수했다(appender는 starter 기본값이 Slf4JLogger라 생략, dateformat은 `%(sql)` 포맷에서 미사용). 동작은 동일하다.
- **로딩 방법**: 실행 모듈 `application.yml`의 `spring.config.import`에 `classpath:application-logging.yml`을 추가한다(현재 web/admin/ceo-api 적용). `application-infrastructure.yml`도 `logging.level`(`org.hibernate.SQL`/`p6spy` 등)을 갖지만 키가 서로 달라 병합되므로 import 순서와 무관하다.
- **batch-module은 대상 아님**: batch는 HTTP 요청이 없어 requestId 패턴·p6spy가 불필요하므로 `application-logging.yml`을 import하지 않고 자체 `logging:` 블록을 유지한다. 다만 `logging-module`을 의존하면 아래 p6spy `api` 노출이 전이되므로, `batch-module/build.gradle`은 `implementation(project(':logging-module')) { exclude ... p6spy-spring-boot-starter }`로 전이를 차단해 기존(SQL 로그 없음) 동작을 보존한다.

## Dependencies

### Internal
- 의존 없음 — 다른 사내 모듈을 참조하지 않는다.

### External
- `spring-boot-starter-web` (api) — `OncePerRequestFilter`, `ContentCachingRequestWrapper`/`ContentCachingResponseWrapper`, 전이적으로 Jackson(`ObjectMapper`/`JsonNode`)도 포함해 `SensitiveFieldMasker`의 JSON 트리 마스킹에 쓰인다(별도 Jackson 의존 선언 없음)
- `spring-boot-starter-aop` (api) — `@Aspect`/`@Around`/`@Before`
- `spring-boot-starter-security` (api) — `SecurityContextHolder`/`Authentication`
- `p6spy-spring-boot-starter` 1.12.1 (api) — datasource 데코레이션으로 SQL 로깅. 소비 모듈(web/admin/ceo-api)이 별도 선언하지 않도록 `api`로 노출하며, SQL 로그 포맷은 `application-logging.yml`이 소유한다. (batch-module은 `exclude`로 전이 차단 — 위 "로깅 설정 소유" 참고)

<!-- MANUAL: -->

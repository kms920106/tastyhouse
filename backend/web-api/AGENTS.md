<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# web-api

## Purpose
최종 사용자용 REST API 애플리케이션 (실행 가능한 Spring Boot bootJar). **챕터 02로 application 계층이 `application` 모듈로 물리 분리되어, 이 모듈은 인바운드 어댑터(컨트롤러 + `request/`)와 config·security 정책·전역 예외 핸들러·부트스트랩만 담당한다.** 컨트롤러는 `com.tastyhouse.application.<ctx>.port.in`의 UseCase 인터페이스만 주입한다.

JWT 필터체인·Spring Security 정책·Redis 캐시·요청 제한(rate limit)·로깅 설정은 계속 이 모듈에 있다. 다만 **서블릿-프리 인증 타입**(`JwtTokenProvider`·`TokenService`·`CustomUserDetails`·`CustomUserDetailsService`)은 `application`의 `auth/{token,security}`로 이동했다 — 자세한 판단 근거는 `application/AGENTS.md`의 "auth가 왜 여기까지 왔나" 참고.

> **챕터 03 — `application`의 자바 패키지가 평탄화됐다.** 과거 `com.tastyhouse.webapplication`이던 것이 `com.tastyhouse.application` 하나로 4개 앱과 합쳐졌고, 앱 소속은 마커 애노테이션(`@WebApp` 등)이 표현한다. 이 모듈이 import하는 UseCase·Command 타입의 **패키지 경로가 바뀌었으므로** 아래 예시·`adaptersShouldOnlyUseOwnAppUseCases` 설명을 그 기준으로 읽는다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `application`·`security-module`·`api-common-module`을 `implementation`으로 의존(컴파일 타임에 구체 타입을 직접 참조하는 소비처가 있어 auto-configuration 전환 후에도 남는다), `infrastructure:persistence`·`infrastructure:file-storage`(external+firebase를 묶은 파일 저장 스타터 — 챕터 03으로 2줄에서 1줄로)·`infrastructure:oauth`·`infrastructure:payment`·`infrastructure:messaging`·`infrastructure:redis`·`logging-module`은 **챕터 02로 `runtimeOnly`**(자기 등록 auto-configuration이라 앱이 진입점 설정 클래스를 컴파일 타임에 볼 필요가 없어졌다) + web, webflux, security, data-redis, aop, validation, JJWT, springdoc (p6spy는 logging-module이 api로 전이). QueryDSL 의존은 없다 |
| `src/main/resources/` | `application.yml` 등 환경 설정 (로깅 설정은 logging-module의 `application-logging.yml`을 import) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/webapi/` | 컨트롤러·인증·설정 등 프레젠테이션 코드 루트 (see `src/main/java/com/tastyhouse/webapi/AGENTS.md`) |
| `src/test/` | 컨트롤러/통합 테스트 (`spring-security-test`) |

## For AI Agents

### Working In This Directory
- **presentation(인바운드 어댑터) 레이어만 담당한다 (챕터 02 개정)**: 컨트롤러는 도메인을 직접 호출하지 않고 `application`의 UseCase 포트를 통해서만 호출한다. JPA Repository·`EntityManager`를 직접 주입하지 않는다.
  - **이 모듈에 `@Service` 빈을 두지 않는다**: application 계층은 `application`이 소유하며, `architecture/LayerRulesTest#apiModuleMustNotContainApplicationLayer`가 이를 강제한다. `@RestController`는 `..adapter.in.web..`에만 둔다(짝 규칙 `restControllersShouldResideInWebAdapterPackage`).
  - **컨텍스트 패키지는 3층 구조다**: `<ctx>/adapter/in/web/`(컨트롤러) + `<ctx>/adapter/in/web/request/`(Request record) + `<ctx>/adapter/in/web/response/`(Response record — **챕터 10으로 승격**). `application/`은 이 모듈에 없다(`application` 소유).
  - 아래 CQRS 서술은 **`application` 모듈의 규칙**이며, 컨트롤러가 어느 포트를 주입할지 판단할 때 참고한다.
  - **도메인당 CQRS 분리**가 이 모듈의 표준 구조다. 과거 `core-module`의 `application/` 계층이 하던 역할이 전환으로 이 모듈의 도메인 패키지로 내려왔고, 도메인당 두 서비스로 분해된다.
    - `{도메인}CommandService`(`@Transactional`): domain write 포트(`XxxRepository`)·도메인 서비스만 주입. 생성/수정/삭제/상태전이를 수행하고 **식별자만 반환**한다. POJO 도메인은 더티 체킹이 없으므로 변경 후 반드시 `repository.save(domain)`을 호출한다.
    - `{도메인}QueryService`(`@Transactional(readOnly = true)`): `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스만 주입(`infrastructure:persistence`의 DAO 구현체는 알지 않는다). **조회만 담당하고 `*Result`를 그대로 반환한다** — Response 조립은 챕터 10으로 이 모듈의 Response record(`from(XxxResult)`)가 맡는다.
    - 조회만 있는 도메인은 QueryService만 둔다(reference: `notice/NoticeQueryService`, `banner/BannerQueryService`, `policy/PolicyQueryService`). 명령만 있는 도메인은 CommandService만 둔다(reference: `bug/BugReportCommandService`, `partnership/PartnershipCommandService`). 둘 다 있는 도메인은 컨트롤러가 필요한 쪽을 각각 주입한다(reference: `order/OrderCommandService`+`OrderQueryService`, `payment/`, `reservation/`, `review/`, `shop/`).
    - CommandService가 `..query..`를, QueryService가 write 포트를 서로 주입하지 않는다. command 결과 응답은 커밋 이후 컨트롤러가 QueryService로 재조회해 조립한다.
    - 흐름 지향 서비스도 챕터 02에서 전부 인바운드 포트를 갖게 됐다 — `auth/AuthCommandService`(구 `AuthService`) implements `AuthCommandUseCase`, `member/MemberService` implements `MemberScreenUseCase`(화면 단위 흐름을 가진 파사드라 삭제 대신 포트를 씌웠다), `grade/GradeQueryService`·`referral/ReferralQueryService`(구 `GradeService`/`ReferralService`)가 각각 `{도메인}QueryUseCase`를 구현한다. `file/FileService`는 `api-common-module` 소유라 대상이 아니다.
  - **이 모듈 전체가 domain-free다** — 컨트롤러뿐 아니라 `config..`·`security..` 등 어디서도 `com.tastyhouse.domain.*`를 import하지 않는다(ID는 `Long`, enum 후보는 `String`으로 받아 서비스에서 승격). `apiModuleShouldBeDomainModelFree`가 강제하며, 공용 에러 계약 `domain.exception..`만 carve-out이다(아래 401 처리가 그 사례).
  - **QueryDSL도 infrastructure도 모른다 (개정)**: `src/main`에 `com.querydsl.*` import·`@QueryProjection` 선언·`com.tastyhouse.infrastructure..` import가 **전면 0건**이며, `architecture/LayerRulesTest`(ArchUnit)가 이를 차단한다(`shouldNotDependOnQuerydsl`·`shouldNotDependOnInfrastructurePersistence`, 그리고 읽기 포트 직접 주입을 막는 `controllersShouldNotDependOnQueryDaos`·`commandServicesShouldNotDependOnQueryDaos`). 챕터 04의 임시 장치였던 `shouldNotDependOnInfrastructureQuery`와 이중 패키지 매칭은 챕터 05에서 제거됐다 — infra query 패키지 자체가 api 모듈에서 참조 불가능해져 규칙이 공허해졌기 때문이다. 조회 계약(`{Ctx}QueryPort`·Result·SearchCondition)은 `com.tastyhouse.application..port.out..`에서 import한다(소유 모듈은 소비 앱 수에 따라 갈리지만 패키지는 하나다).
- 도메인별 폴더(`member/`, `order/`, `shop/` …) 안에 `adapter/in/web/request/`·`adapter/in/web/response/` DTO를 둔다. **`response/`는 챕터 10으로 이 모듈이 소유한다**(131개) — 조립 주체가 QueryService에서 Response record 자신으로 바뀌었기 때문이다.
- **import 순서 — presentation 내부 서브정렬**: 자사 import의 presentation 계층(`com.tastyhouse.webapi.*`) 안에서는 공용 인프라(`common`·`config`·`security`·`ratelimit`·`exception`)를 도메인 전용(`<도메인>.request`·`.response`)보다 **위**에 둔다. 각 서브그룹 내부는 알파벳순, 사이 빈 줄 없음. 상세·근거·예시는 루트 CLAUDE.md 참고.
- **DTO 조립 시 `new` 직접 호출 지양**: 컨트롤러·서비스에서 response/condition을 `new`로 조립하지 않고, 대상 record 자신의 정적 팩토리 `of(...)`/`from(...)`로 위임한다. **Request record가 `toCommand(...)`를 소유**하고 컨트롤러가 `XxxCommand command = request.toCommand(...);`로 조립해 UseCase에 넘긴다(완전 매핑 — 매핑은 인바운드 어댑터의 책임). 경로 변수·인증 주체처럼 본문에 없는 값은 `toCommand`의 파라미터로 주입한다. **Response record는 `from(XxxResult)` 단일 인자 팩토리로 조립한다**(챕터 10) — 도메인 모델은 import하지 않고, 도메인 enum은 carve-out된 accessor(`name`·`getDescription`·`getDisplayName`)만 호출한다. 그 밖의 계산(도메인 서비스·여러 포트 합성·시계·VO 언랩·enum 비-accessor)은 `application`이 `*View`/`*ViewResult`로 넘긴다. 예외: `PaginationResponse.from(PageResult<T>)`는 공용 페이징 계약 위임이라 그대로 둔다. 상세는 루트 CLAUDE.md 참고.
- **식별자(`XxxId.of(id)`)는 지역 변수로 추출 후 넘긴다**: CQRS 전환으로 core command 서비스 호출과 command DTO 자체는 사라졌지만, `{도메인}CommandService`가 HTTP 경계의 `Long`을 domain VO로 승격하는 지점에서 이 관례가 유지된다 — `XxxId xxxId = XxxId.of(id);`로 먼저 추출한 뒤 write 포트·도메인 서비스에 전달하고, 인자 자리에 `.of(` 호출을 인라인하지 않는다(reference: `reservation/ReservationCommandService`의 `cancel`·`confirm`·`reject`·`complete`). 대체된 인라인 한 줄을 `//` 주석으로 남기지 않는다. query(조회) 서비스 호출에 넘기는 `XxxId.of(id)`와 응답 변환 `XxxResponse.from(...)`은 인라인을 유지한다. 상세는 루트 CLAUDE.md 참고.
- **`record`는 별도 파일로 분리**: 서비스·컨트롤러 본문 안에 응답 record를 중첩 선언하지 않고 도메인 폴더의 `response/`에 `public record`로 둔다(reference: `notice/response/NoticeListItemResponse`, `order/response/OrderDetailResponse`). 표준 4필드 페이징 응답은 도메인 폴더에 만들지 않고 `common/PaginationResponse<T>` 공용 제네릭을 재사용한다. 조회 Result·SearchCondition은 `com.tastyhouse.application..port.out` 소유(소비 앱 수에 따라 `{앱}-application` 또는 `domain`)이고, **Response record는 이 모듈의 `<ctx>/adapter/in/web/response/` 소유다**(챕터 10). 상세는 루트 CLAUDE.md 참고.
- **GET 조회 파라미터는 개수와 무관하게(1개여도) `@ModelAttribute` Request record로 받는다**: 개별 `@RequestParam`을 나열하지 않고 `{도메인}SearchRequest`(`request/`, `public record`)로 묶어 `@Valid @ModelAttribute`로 받고, 페이징 `PageRequest`는 별도 인자로 병기한다. SearchRequest는 `toCondition()` 없는 순수 홀더로 두고 컨트롤러가 원시 필드로 언패킹해 `{도메인}QueryService`에 넘기며, 그 서비스가 infra `<ctx>/query/`의 `{도메인}SearchCondition.of(...)`로 조립한다(enum=`String`/`List<String>`, FK=`Long`, Swagger는 필드 `@Schema(allowableValues={...})`, 기본값은 compact constructor에서 정규화). 조회 파라미터가 없는(페이징만) GET·`@PathVariable`만 받는 단건 조회는 대상 아님. 상세는 루트 CLAUDE.md 참고.
- **`@PathVariable` 주 리소스는 `id`로 통일한다**: 컨트롤러가 `@RequestMapping`으로 이미 그 도메인에 스코프되므로 주 리소스 식별자는 단건 CRUD·중첩 하위 경로 모두 bare `id`로 쓰고 한 컨트롤러 안에서 `id`/`{도메인}Id` 혼재를 금지한다. 단, 한 경로가 서로 다른 애그리거트 식별자를 둘 이상 받을 때(예: `/members/{memberId}/orders/{orderId}`)만 각각을 `{도메인}Id`로 구분한다. 타입은 `Long` 유지(`@PathVariable Long id`) 후 `{도메인}CommandService`/`{도메인}QueryService`에서 `XxxId.of(...)`로 승격한다. 상세는 루트 CLAUDE.md 참고.
- **Request/Response record는 `@Schema`로 완전히 문서화한다**: 타입 레벨에 `@Schema(description = ...)`, 모든 필드에 `@Schema(description = ..., example = ...)`를 붙인다(컬렉션·중첩 record 필드는 example 생략 가능하나 description은 필수). Request는 Bean Validation 어노테이션 다음 줄에 `@Schema`를 두고 필수 필드는 `requiredMode = Schema.RequiredMode.REQUIRED`로 표시하며, enum 후보 `String`/`List<String>` 필드는 기존 `allowableValues = {...}`에 `description`을 병기한다(reference: `order/request/OrderCreateRequest`, `reservation/response/ReservationDetailResponse`). 상세는 루트 CLAUDE.md 참고.
- 401 처리도 비즈니스 예외와 동일하게 `domain`의 `BusinessException` 계열(`com.tastyhouse.domain.exception`)에 `ErrorCode.AUTH_*`(401)를 담아 사용 — 전역 처리는 `exception/GlobalExceptionHandler`. (web-api 로컬 `UnauthorizedException`은 제거됨: errorCode 없는 401을 내보내 다른 401과 계약이 어긋났다.)

### Testing Requirements
- `@WebMvcTest` / `@SpringBootTest` + `spring-security-test`로 인증 흐름 검증.
- **레이어 경계는 `src/test/.../architecture/LayerRulesTest`(ArchUnit)가 강제**한다 — `applicationServicesShouldNotDependOnWebLayer`(클래스명 `*CommandService`/`*QueryService`로 대상을 잡아 `org.springframework.web.bind..`/`web.servlet..`/`org.springframework.http..`/`jakarta.servlet..` 의존 차단. `MultipartFile`은 업로드 경계 타입이라 제외), `controllersShouldNotDependOnRepositories`, `controllersShouldNotDependOnQueryDaos`, `commandServicesShouldNotDependOnQueryDaos`, `shouldNotDependOnQuerydsl`, `shouldNotDependOnInfrastructurePersistence`, 그리고 챕터 05에서 패키지 기준으로 승격한 `webAdaptersShouldNotDependOnApplicationServices`(`..adapter.in.web..` → `..application.service..` 금지)·`portInShouldBeFreeOfWebDomainAndInfrastructure`. `allowEmptyShould(true)`를 쓰지 않으므로 대상 클래스가 0건이면 **공허 통과가 아니라 실패**로 드러난다(과거 `..application..` 패키지를 매칭하던 규칙이 전환 후 대상 0건으로 공허 통과하던 문제를 이렇게 해소했다).

### Common Patterns
- **JWT 인증 메커니즘(access/refresh 발급·검증·필터·EntryPoint·AccessDeniedHandler)은 `security-module`의 `com.tastyhouse.security.jwt`에 공유**된다. `application`의 `MemberJwtTokenProvider`(`@Component @WebApp`)가 그 공용 provider를 상속해 `memberId` 클레임·`MemberUserDetails` 재구성을 주입하고, **web 전용 검증 토큰(휴대폰/이메일/개인정보/비밀번호 재설정) 발급 메서드만 추가**한다. **공용 필터 빈은 `SecurityModuleAutoConfiguration`이 등록**하므로(챕터 02) 이 앱에 조립 코드가 없다 — `config/jwt/JwtConfig`는 삭제됐다.
- **부트스트랩은 `@Import(WebApplicationConfig.class)` 하나뿐이다 (챕터 02)**: `WebApiApplication`은 `@SpringBootApplication` + `@Import` 두 애노테이션만 갖는다 — `scanBasePackages`도 `@ComponentScan`도 없다. 라이브러리 모듈(`infrastructure:*`·`security-module`·`logging-module`·`api-common-module`)은 각자의 `{Xxx}ModuleAutoConfiguration`으로 **자기 자신을 등록**하므로 앱이 스캔 대상을 나열할 필요가 없다. `domain`은 `@Component`/`@Service`/`@Configuration`이 0건이라(도메인 서비스는 POJO, 빈 등록은 infra `<ctx>/config/<Ctx>DomainConfig`) 애초에 스캔 대상이 아니다. 조립의 상한은 루트 [CLAUDE.md 컴포지션 루트 규칙](../CLAUDE.md#컴포지션-루트-규칙-조립은-실행-앱-모듈의-것--챕터-03) 참고.
- **정책은 web-api에 잔류**: `config/security/`의 `SecurityConfig`(공개 경로·CORS 헤더 `X-Verify-Token` 등)·`PublicPaths`. (`config/jwt/` 디렉터리는 챕터 01~02로 소멸 — `RedisRepositoryConfig`·`JwtConfig` 모두 삭제.)
- **`jwt.secret`은 admin-api와 반드시 달라야 한다**(web=`JWT_SECRET_WEB`). 동일 시크릿이면 회원 토큰이 admin 인증을 통과한다 — 상세는 `security-module/AGENTS.md`.
- 소셜 로그인은 `auth/{kakao,naver,apple,facebook}` — 실제 외부 호출은 `infrastructure:oauth`에 위임.
- 응답은 공통 래퍼로 일관화 — `ApiResponse`/`PaginationResponse`/`PageRequest`는 이 모듈이 아니라 **`api-common-module`(`com.tastyhouse.apicommon`) 소유**다. (과거 함께 있던 `FileService`는 이후 계층 재배치로 `application`의 `FileUploadCommandService`가 됐다.)
- **`GlobalExceptionHandler`만은 이 모듈에 잔류**한다: web 전용 핸들러 4종(`ExternalApiException`·`NoHandlerFoundException`·`MissingServletRequestParameterException`·`MethodArgumentTypeMismatchException`)과 "필드명: 메시지" 형식의 검증 실패 응답이 admin/ceo와 다른 **응답 계약 차이**이기 때문이다. **핸들러가 2개가 되는 것은 스캔 범위가 아니라 조건이 막는다 (챕터 02)** — `ApiCommonModuleAutoConfiguration`이 공용 핸들러를 `@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)`로 등록하므로, 이 모듈의 자체 advice를 보고 **스스로 물러난다**(실측 Negative). 앱이 할 일은 없다.
- **소셜 로그인은 `external.oauth.spi` SPI로만 사용**한다: 소셜 서비스 4종은 `SocialOAuthClient`(`@Qualifier`로 제공자 지정)·`SocialProfile`만 알고 제공자별 wire DTO를 import하지 않는다. 제공자별 임시토큰 저장소(챕터 01 이후 포트는 `security-core`, Redis 구현은 `infrastructure:redis`)·`*_TEMP_TOKEN_EXPIRED` ErrorCode는 제공자별로 유지한다(key prefix 변경 금지). ArchUnit `shouldDependOnOauthSpiOnlyNotProviderPackages`가 강제.
- **등록(POST) API는 생성된 `Long` id만 반환**한다: `ResponseEntity<ApiResponse<Long>>`로 PK 하나만 반환하고, **생성 직후 `{도메인}QueryService`로 재조회해 상세 DTO를 반환하지 않는다**(과거 이 모듈만 등록 8종이 재조회 DTO 형태였으나 전면 전환됨). 생성 응답 전용 래퍼 record(`OrderCreateResponse` 등)를 만들지 않고, 행을 생성하고도 `ApiResponse<Void>`를 반환하지 않는다(`signUp`·`follow`도 id 반환). 상세가 필요한 클라이언트는 그 id로 GET 상세를 호출한다. 파일 업로드(`FileApiController#upload`)·인증/토큰 발급(소셜 로그인·`signUpSocialAccount`·인증코드 확인 등)·토글(`toggleBookmark`·`toggleReviewLike`)·상태전이(payment `confirm`/`cancel`/`refund`, reservation `confirm`/`reject`/`complete`)·POST-as-query(`getProductsBatch`)는 적용 제외. 상세는 루트 CLAUDE.md 참고.


## 설정 파일 (`src/main/resources/application.yml`)

서버 포트 `8080`, CORS 허용 오리진(`CORS_ALLOWED_ORIGINS`, 기본 `http://localhost:3000`), JWT 만료(access 1시간 / refresh 7일 / 로그인 상태 유지 30일), multipart 상한 10MB를 담고, 공유 모듈의 설정을 `spring.config.import`로 끌어온다.

- **Redis 연결 설정은 이 파일이 갖지 않는다** — 챕터 05 §5b에서 `infrastructure:redis` 모듈이 소유하게 됐고, 이 파일은 `classpath:application-redis.yml`을 import할 뿐이다. 그 설정만 담고 있던 `security-module`의 `application-security.yml`은 **파일째 이관되고 삭제됐다.** Redis 접속 정보를 바꿔야 하면 이 파일이 아니라 `infrastructure/redis/src/main/resources/application-redis.yml`을 본다.
- **`.env`를 `optional:file:.env[.properties]`와 `optional:file:backend/.env[.properties]` 두 경로로 선언한다.** `spring.config.import`의 `file:` 상대경로는 **JVM 작업 디렉터리(CWD) 기준으로 해석**되므로, 한 경로만 선언하면 실행 위치에 따라 `.env`가 조용히 로드되지 않는다. 두 줄을 함께 두어 **모노레포 루트에서 실행하는 경우와 `backend`에서 실행하는 경우를 모두 지원**한다. `optional:` 접두어라 없는 쪽은 건너뛴다. 그 밖의 디렉터리에서 `java -jar`를 실행하면 두 경로 모두 빗나가 DB 접속 정보 같은 필수 환경변수가 비므로, 실행 디렉터리 규칙은 루트 `CLAUDE.md`의 "실행 디렉터리(CWD) 주의"를 따른다.
- `jwt.secret`은 ``JWT_SECRET_WEB``를 읽는다(앱별로 반드시 달라야 하는 이유는 위 참고).
- 소셜 로그인 4종(kakao·naver·facebook·apple)의 client-id·secret·redirect-uri도 이 파일이 갖고, 값은 전부 환경변수 참조다.

## Dependencies

### Internal
- `application` (implementation) — 컨텍스트 UseCase 인바운드 포트(컨트롤러가 주입) + `WebApplicationConfig`
- `infrastructure:persistence` (**챕터 02로 `runtimeOnly`로 강등 — 과거 서술의 번복**): 소스 import는 0건이고, **auto-configuration 전환으로 부트스트랩의 컴파일 타임 참조 자체가 사라졌다.** 과거에는 `@Import(InfrastructureModuleConfig.class)`가 진입점 설정 클래스를 컴파일 타임에 참조해 `runtimeOnly`로 내리면 4개 모듈 전부 "package does not exist"로 깨졌으나, `InfrastructureModuleConfig` → `PersistenceModuleAutoConfiguration`으로 리네임되며 `@AutoConfiguration` + `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록하는 형태가 되어 `@Import` 자체가 사라졌다. 은닉은 여전히 의존 스코프가 아니라 ArchUnit(`LayerRulesTest`)이 담당하지만, 이제는 컴파일 타임 은닉도 `runtimeOnly`가 실제로 보장한다
- `infrastructure:file-storage` — 파일 저장 스타터(챕터 03). `infrastructure:external`(코어 — `WebClientConfig`·`ExternalApiException`·파일 저장 SPI)과 `infrastructure:firebase`(`FileStorageStrategy` 구현, 기본 provider)를 묶어 전이로 공급한다. **앱은 두 모듈을 직접 선언하지 않는다**
- `infrastructure:oauth` — 소셜 로그인 어댑터 4종(`external.oauth.spi` SPI 구현)
- `infrastructure:payment` — Toss PG 어댑터(`PgPaymentGateway` 구현)
- `infrastructure:messaging` — 메일(JavaMail)·SMS(Solapi) 어댑터
- `security-module` — 공용 JWT 메커니즘·Redis 토큰 저장소
- `infrastructure:redis` (**runtimeOnly**) — rate limit 카운터(`RedisRateLimitCounter`)와 `StringRedisTemplate` 빈. `RedisModuleAutoConfiguration`이 자기 등록하며(챕터 02) 부트스트랩은 `@Import`하지 않는다. 챕터 02에서 `@RateLimit`·aspect는 `api-common-module`로 올라갔고 이 모듈에는 카운터만 남았다
- `api-common-module` — `ApiResponse`·`PaginationResponse`·`PageRequest`·`FileService`
- `logging-module` — 요청/응답 로깅(p6spy 전이)
- **`domain`은 선언하지 않는다** — 이 모듈 소스에 `com.tastyhouse.domain..` import가 0건이고(`apiModuleShouldBeDomainModelFree`가 강제), domain 타입이 다시 필요해져도 `api-common-module`이 `api project(':domain')`로 전이 노출하므로 재선언이 필요 없다. web/admin/ceo 3모듈이 모두 같은 상태다(web-api `GlobalExceptionHandler`가 쓰는 `domain.exception..`도 이 전이 경로로 해결된다).
- `testFixtures(project(':application'))` — `adaptersShouldOnlyUseOwnAppUseCases`가 Command record의 앱 소속 유도(`AppOwnership`)를 application 모듈과 공유한다. **복제하면 두 벌이 갈라지므로** test fixture로 받는다(챕터 03).

### External — starter를 직접 선언하지 않는다
공유 모듈이 `api`로 전이 노출하므로 이 모듈은 starter 좌표를 직접 쓰지 않는다.

| 전이되는 것 | 노출 모듈 |
|---|---|
| `starter-web` · `starter-validation` · springdoc | `api-common-module` |
| `starter-security` | `security-module` |
| `starter-aop` | `logging-module` |
| jjwt-api (impl·jackson은 runtimeOnly 전이) | `security-module` → `security-core` |

"직접 쓰는 것은 직접 선언"하는 Gradle 관례와는 상충하나, 위 노출은 **의도된 계약**이라 소비 측 중복 선언을 노이즈로 판단해 걷어냈다. 공유 모듈이 노출을 `implementation`으로 좁히면 여기서 **즉시 컴파일 에러**로 드러나므로 침묵 파손은 없다.

**~~예외 — `spring-boot-starter-data-redis`는 직접 선언한다~~ (챕터 01에서 소멸).** 이 앱은 더 이상 Redis 타입을 참조하지 않으므로 그 선언이 **삭제**됐고, `runtimeOnly project(':infrastructure:redis')`가 유일한 Redis 선언이다. 근거: 접두사를 생성자로 주입하려고 `StringRedisTemplate`을 직접 참조하던 `config/jwt/RedisRepositoryConfig`가 사라졌다 — 토큰 저장소가 포트/어댑터로 역전되며 접두사가 프로퍼티가 됐기 때문이다.

**키 접두사 (불변 계약)**: 이 앱은 기본값 `""`를 쓰므로 `application.yml`에 `security.token-store.key-prefix`를 **선언하지 않는다**. 결과 키는 `rt:{username}`·`bl:{accessToken}`이다. 값을 넣으면 기존 로그인 세션이 전부 무효화된다.

**이것은 어댑터 모듈을 `implementation`으로 되돌리는 것과 다르다.** 앱이 보는 것은 `StringRedisTemplate`이라는 **라이브러리 타입**뿐이고, `infrastructure:redis`의 어댑터 클래스(`RedisRateLimitCounter` 등)는 여전히 컴파일 타임에 보이지 않는다 — 헥사고날 은닉은 그대로다. 두 판단을 섞어 "전이가 끊겼으니 모듈을 다시 `implementation`으로" 되돌리지 않는다.


<!-- MANUAL: -->

## 봉인·가드 목록

<!-- 분류 A. web-api 고유분. 3앱 공통분은 backend/AGENTS.md "계층 규칙 봉인 — api 앱 3종 공통" 참조 -->

**대상**: `backend/web-api/src/test/java/com/tastyhouse/webapi/architecture/LayerRulesTest.java`

이 파일의 규칙 대부분은 admin-api·ceo-api와 동일하며, 그 공통분은 [backend/AGENTS.md](../AGENTS.md)의 "계층 규칙 봉인 — api 앱 3종 공통"에 있다. **아래는 web-api에만 있는 것이다.**

### `shouldDependOnOauthSpiOnlyNotProviderPackages` — 3앱 중 web-api에만 있다

**대상**: `backend/web-api/src/test/java/com/tastyhouse/webapi/architecture/LayerRulesTest.java`
→ `shouldDependOnOauthSpiOnlyNotProviderPackages()`

소셜 로그인은 application의 SPI(`com.tastyhouse.application.auth.port.out`)만 통해 쓴다. 제공자별 패키지의 wire DTO·클라이언트 구현에 직접 의존하지 않는다.

금지 대상 제공자 패키지 4개를 FQN 문자열로 열거한다.

- `com.tastyhouse.external.oauth.kakao..`
- `com.tastyhouse.external.oauth.naver..`
- `com.tastyhouse.external.oauth.facebook..`
- `com.tastyhouse.external.oauth.apple..`

소유 모듈이 external-api → `infrastructure:external` → `infrastructure:oauth`로 바뀌는 동안에도 자바 패키지가 불변이라 규칙은 그대로 유효했다. 반대로 **패키지를 바꾸면 이 규칙은 실패하는 대신 조용히 대상을 잃으므로, 제공자 패키지를 옮길 때는 이 목록을 함께 고친다.**

**이 규칙을 admin-api·ceo-api에 복제하지 않는다** — 두 앱에는 소셜 로그인이 없어 대상 0건으로 **공허하게 통과**하기 때문이다.

### `seedersShouldDependOnUseCasesOnly`는 이 모듈에 두지 않는다

web-api에는 시더가 없어 대상 0건이므로(**공허 통과 회피**) admin-api·ceo-api에만 있는 규칙이다.

### 이하 — 챕터 06에서 코드 주석으로부터 이관된 가드

<!-- 분류 A. 원문 주석은 챕터 06에서 제거됐으므로 이 절이 그 금지 지시의 유일한 소재지다 -->

### `keyPrefix = "rate_limit:email_verification"` — 도메인 개명(email→mail)에 맞춰 바꾸지 않는다

**대상**: `backend/web-api/src/main/java/com/tastyhouse/webapi/mail/adapter/in/web/MailVerificationApiController.java`
→ `sendVerificationCode()`의 `@RateLimit(keyPrefix = ...)`

`keyPrefix`는 Redis 카운터 키다. 패키지·클래스가 `email`에서 `mail`로 개명됐어도 이 문자열은 **바꾸지 않는다** — 바꾸는 순간 기존 카운터가 통째로 버려져 배포 시점에 발송 한도가 전원 리셋된다(브루트포스 한도 초기화). `sms` 쪽은 원래부터 `rate_limit:sms_verification`이라 두 접두어가 대칭이 아닌 것이 **정상**이며, 대칭을 맞추려는 교정 대상이 아니다.
### `ShopOrderMethodItemResponse.code` / `.name` — wire 계약이므로 개명하지 않는다

**대상**: `backend/web-api/src/main/java/com/tastyhouse/webapi/shop/adapter/in/web/response/ShopOrderMethodItemResponse.java`
→ `code`, `name`

프론트가 이미 소비 중인 wire 계약이라 `orderMethod`/`orderMethodName`으로 **개명하지 않는다**. 주문가능 여부 3필드는 additive로 추가된 것이다(기존 필드를 건드리지 않은 확장).
### `ProductReviewsByRatingPageResponse` — 공용 `PaginationResponse<T>`로 대체하지 않는다

**대상**: `backend/web-api/src/main/java/com/tastyhouse/webapi/product/adapter/in/web/response/ProductReviewsByRatingPageResponse.java`
→ 타입 전체

4필드 표준 페이징 래퍼가 아니라 중첩 `response`와 `totalElements`만 갖는 자체 형태다. "페이징은 공용 제네릭을 쓴다"는 모듈 규칙의 **예외로 봉인**한다 — 표준 형태로 바꾸면 응답 JSON이 달라져 클라이언트가 깨진다.
### `PublicPaths` 등록을 빠뜨리면 비로그인 손님에게 401이 나간다 — 경로 변경 시 목록을 함께 고친다

**대상**: 아래 4개 컨트롤러 (공통 가드 1건으로 묶어 기재)

- `backend/web-api/src/main/java/com/tastyhouse/webapi/shop/adapter/in/web/ShopOrderNoticeApiController.java` → 타입
- `backend/web-api/src/main/java/com/tastyhouse/webapi/shop/adapter/in/web/ShopOriginInfoApiController.java` → 타입
- `backend/web-api/src/main/java/com/tastyhouse/webapi/shop/adapter/in/web/ShopPriceBadgeApiController.java` → 타입
- `backend/web-api/src/main/java/com/tastyhouse/webapi/shop/adapter/in/web/ShopMenuCollectionImageApiController.java` → 타입

주문안내·원산지·매장가격 뱃지·메뉴모음컷은 로그인 없이 가게를 둘러보는 손님도 봐야 하는 정보이므로 `PublicPaths`에 등록돼 있다(`/api/shops/v1/*/order-notice`, `/api/shops/v1/*/origin`, `/api/shops/v1/*/price-badges`). **이 컨트롤러를 옮기거나 경로를 바꿀 때는 `PublicPaths` 목록을 반드시 함께 고친다** — 등록을 빠뜨려도 컴파일·테스트는 통과하고, 비로그인 손님에게 401이 나가면서 메뉴판 최상단·원산지 영역·뱃지가 통째로 비는 형태로만 드러난다. 원산지는 법령이 요구하는 표시 정보라 특히 그렇다.
### `Request` record → `Command` 변환은 반드시 이름 기반 접근자로 짚어 넘긴다

**대상**: 아래 record들의 `toCommand(...)` (공통 가드 1건)

- `member/.../request/MemberDeliveryAddressCreateRequest.java` → `toCommand(Long)`
- `member/.../request/MemberDeliveryAddressUpdateRequest.java` → `toCommand(Long, Long)`
- `member/.../request/UpdatePasswordRequest.java` → `toCommand(Long)`
- `member/.../request/UpdatePersonalInfoRequest.java` → `toCommand(Long)`
- `order/.../request/OrderCreateRequest.java` → `toCommand(Long)`
- `payment/.../request/PaymentConfirmRequest.java` → `toCommand()`
- `payment/.../request/RefundRequest.java` → `toCommand(Long, Long)`
- `payment/.../request/TossPaymentConfirmApiRequest.java` → `toCommand(Long)`
- `review/.../request/ReplyCreateRequest.java` → `toCommand(Long, Long)`
- `review/.../request/ReviewCreateRequest.java` → `toCommand(Long)`
- `review/.../request/ReviewUpdateRequest.java` → `toCommand(Long, Long)`
- `reservation/.../request/ReservationCreateRequest.java` → `toCommand(Long)`

(경로 접두사는 모두 `backend/web-api/src/main/java/com/tastyhouse/webapi/`)

같은 타입의 값이 연달아 선언돼 있어 **위치 기반 전달은 조용히 뒤바뀐다.** 컴파일러가 잡지 못하고 런타임에도 예외가 나지 않으므로 위치 기반 조립으로 바꾸지 않는다. 구체적 피해는 각각 다르다.

- 주소 `String` 4개 + 좌표 `BigDecimal` 2개 — 위경도가 뒤바뀌면 배달팁이 엉뚱하게 산출된다
- `UpdatePasswordRequest` — 두 `String`이 뒤바뀌어도 확인값 일치 검사는 **대칭이라 그대로 통과한다**
- `ReplyCreateRequest` — `memberId`·`commentId`·`replyToMemberId` 세 `Long`이 연달아 있어 작성자와 답글 대상이 뒤바뀐다
- 평점 3종(`Integer`)·`cardCompany`/`cardNumber`/`receiptUrl`(`String`)·`paymentKey`/`pgOrderId`(`String`) 동종 연속
### `OrderCreateRequest`의 필드 선언 순서는 `OrderCreateCommand`와 다르다 (봉인)

**대상**: `backend/web-api/src/main/java/com/tastyhouse/webapi/order/adapter/in/web/request/OrderCreateRequest.java`
→ `toCommand(Long)`

이 record는 `deliveryAddressId`가 `usePoint`보다 먼저 선언돼 있어 `OrderCreateCommand`와 순서가 **일치하지 않는다.** 위치 기반 전달로 옮기면 두 값이 조용히 뒤바뀐다. 순서를 "맞추는" 리팩터링도 wire 계약(요청 JSON 필드 순서와 Swagger 스키마)에 영향을 주므로 하지 않고, 이름 기반 접근자 조립을 유지한다.
### `PaymentConfirmRequest.paymentId`는 본문 필드다 — 경로 변수로 옮기지 않는다

**대상**: `backend/web-api/src/main/java/com/tastyhouse/webapi/payment/adapter/in/web/request/PaymentConfirmRequest.java`
→ `paymentId`, `toCommand()`

이 엔드포인트는 경로에 식별자를 두지 않으므로 `toCommand()`에 주입 파라미터가 없다. 다른 결제 요청 record(`PaymentCancelRequest`·`RefundRequest`)와 시그니처가 다른 것이 정상이며, 대칭을 맞추려고 경로 변수로 승격하지 않는다.
### `TODO(보안)` 4건 — 점주 본인 검증 미구현 (실코드 결함, 제거 시 소실 주의)

**대상**: `backend/web-api/src/main/java/com/tastyhouse/webapi/reservation/adapter/in/web/ReservationApiController.java`
→ `confirm(Long)`, `reject(Long)`, `complete(Long)`, `getShopReservations(Long)`

Shop-owner 연결 후 **점주 본인 검증을 추가해야 한다.** 현재 이 4개 엔드포인트는 예약 id·가게 id만으로 상태 전이(`PENDING→CONFIRMED`/`REJECTED`, `CONFIRMED→COMPLETED`)와 목록 조회를 허용하므로, 남의 가게 예약을 조작·열람할 수 있는 **미해결 IDOR**다. 주석을 지우면 이 결함의 유일한 기록이 사라지므로 반드시 문서로 옮긴다.
### `ShopApiController`·`ReviewApiController`의 `@CurrentUser`는 `null`일 수 있다 — 비-null 가정으로 바꾸지 않는다

**대상**:
- `backend/web-api/src/main/java/com/tastyhouse/webapi/shop/adapter/in/web/ShopApiController.java` → `memberIdOrNull(MemberUserDetails)`, `getBestShops(...)`, `getDeliveryTip(...)`
- `backend/web-api/src/main/java/com/tastyhouse/webapi/review/adapter/in/web/ReviewApiController.java` → 동명 헬퍼

`/api/shops/**`·`/api/reviews/**`는 `PublicPaths`의 공개 경로라 비로그인 접근이 가능하고, 그때 principal이 `null`로 들어온다. `@CurrentUser`를 필수로 취급하거나 NPE 방어를 걷어내면 비로그인 손님 경로가 500으로 깨진다. 비로그인 시 배달지역 필터를 걸지 않고, 배달팁은 확정 계산 대신 **범위 모드**로 떨어뜨리며, 사장님만보기 리뷰는 본인 판정을 하지 않는다.
### `ProductBatchRequest.orderMethod` — 화면이 배달가/픽업가를 고르게 하지 않는다

**대상**: `backend/web-api/src/main/java/com/tastyhouse/webapi/product/adapter/in/web/request/ProductBatchRequest.java`
→ `orderMethod`

어느 채널 가격을 쓸지는 **서버가 주문유형으로 단독 결정한다**(`ProductPrice#resolvePrice`). 배치 조회도 상세 조회와 같은 파라미터를 받아 이미 해석된 단일 가격만 내려준다. **화면이 배달가/픽업가를 고르게 만들면 주문 접수의 `validateAmounts()`와 어긋나 전 주문이 거절된다.**
### `ProductPriceResponse` — 채널별 가격 세 벌·매장가를 내려주지 않는다

**대상**: `backend/web-api/src/main/java/com/tastyhouse/webapi/product/adapter/in/web/response/ProductPriceResponse.java`
→ 타입 전체

**채널별 가격 세 벌을 내려주지 않는다** — 화면이 배달가·픽업가 중에서 고르게 하면 클라이언트가 픽업가를 주장해 배달을 싸게 사는 우회가 생기고, 주문 금액 검증과 표시 가격이 갈린다. **매장가(`storePrice`)도 이 응답에 없다** — 결제에 쓰이지 않는 표시 전용 값이고 그 쓰임은 가게 단위 뱃지(`GET /api/shops/v1/{id}/price-badges`)뿐이다. 메뉴마다 매장가를 함께 내리면 계약에 없는 오프라인 가격표가 손님 앱으로 새어 나간다.
### `ShopPriceBadgeResponse` — 판정 근거를 담지 않는다 (플래그만)

**대상**: `backend/web-api/src/main/java/com/tastyhouse/webapi/shop/adapter/in/web/response/ShopPriceBadgeResponse.java`
→ 타입 전체

**플래그 2개만 내려주고 판정 근거(매장가·픽업가·커버리지 비율)는 담지 않는다.** 근거를 함께 내리면 손님 앱이 자체 판정을 시도할 수 있고, 매장가는 결제에 쓰이지 않는 표시 전용 값이라 손님 계약에 노출할 것이 아니다.

---

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 경계·앱 간 대조 (챕터 06 이관). 컨트롤러/Request/Response 작성 관례는 src/main/java/com/tastyhouse/webapi/AGENTS.md -->

### 손님 응답과 점주(ceo) 응답은 같은 경로여도 계약이 다르다 — 공용화하지 않는다 → **web-api/AGENTS.md** (앱 간 대조라 모듈 루트)

**대상**:
- `shop/adapter/in/web/ShopMenuCollectionImageApiController.java` → 타입
- `shop/adapter/in/web/response/ShopMenuCollectionImageResponse.java` → 타입
- `shop/adapter/in/web/ShopOrderNoticeApiController.java` → 타입
- `shop/adapter/in/web/response/ShopOrderNoticeResponse.java` → 타입
- `shop/adapter/in/web/ShopOriginInfoApiController.java` → 타입
- `shop/adapter/in/web/response/ShopOriginInfoResponse.java` → 타입
- `shop/adapter/in/web/response/ShopNoticeResponse.java` → 타입

ceo-api에 **URL 경로가 같은** 점주용 조회가 따로 있다. 두 앱은 서로 다른 호스트·포트로 서비스되므로 충돌하지 않으며, **응답 계약이 의도적으로 다르므로 각 모듈이 자기 버전을 소유하는 것이 맞다**("실제 쓰는 필드만" 원칙). 공용 record로 합치지 않는다.

| 응답 | 손님(web)에 없는 필드 | 이유 |
|---|---|---|
| 메뉴모음컷 | `status`, `rejectReason` | 손님에게는 승인된 것만 내려가 상태 필드가 무의미하고, 반려 사유는 점주 내부 정보 |
| 주문안내 | `hidden`, `hiddenReason`, 식별자 | 게시중단된 문구는 응답이 아예 만들어지지 않고 `data: null`. 관리자 조치 사유를 손님에게 노출하지 않는다. 가게당 1건이라 손님이 개별 조작할 대상이 아니다 |
| 원산지 | `updatedAt`, 미설정 시 빈 폼 기본값 | 손님에게 최종 수정 시각은 의미가 없고, 미설정이면 `data: null`로 화면이 영역을 통째로 감춘다(점주는 반대로 빈 폼 기본값을 받는다) |
| 가게 공지 | `exposed`, `hidden`, `updatedAt` | 사용자 화면이 쓰지 않는 내부 상태이므로 과잉 노출을 피한다 |

### 게시중단 리뷰 접근은 403이 아니라 404 — ceo 경로와 갈린다
**대상**: `review/adapter/in/web/ReviewBlindConsentApiController.java` → 타입

대상이 이미 게시중단된 비공개 리뷰이므로, 타인 리뷰 접근을 403이 아니라 **404(`REVIEW_NOT_FOUND`)로 응답해 존재 자체를 숨긴다.** ceo 경로와 응답 코드가 갈리는 것이 정상이다. 판단 근거는 도메인 서비스에 있다.

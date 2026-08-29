<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# web-api

## Purpose
최종 사용자용 REST API 애플리케이션 (실행 가능한 Spring Boot bootJar). 도메인별 컨트롤러와 Request/Response DTO를 제공하며, JWT 기반 인증·소셜 로그인(OAuth)·Spring Security·Redis 캐시·요청 제한(rate limit)·스케줄러·로깅을 담당한다. 비즈니스 로직(불변식)은 직접 구현하지 않고 `domain-module`의 도메인 모델·도메인 서비스에 위임하며, 조회는 `infrastructure-module`의 `<ctx>/query/` DAO에 위임한다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `domain-module`·`infrastructure-module`·`external-api`·`security-module`·`logging-module`을 모두 `implementation`으로 의존 + web, webflux, security, data-redis, aop, validation, JJWT, springdoc (p6spy는 logging-module이 api로 전이). QueryDSL 의존은 없다 |
| `src/main/resources/` | `application.yml` 등 환경 설정 (로깅 설정은 logging-module의 `application-logging.yml`을 import) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/webapi/` | 컨트롤러·인증·설정 등 프레젠테이션 코드 루트 (see `src/main/java/com/tastyhouse/webapi/AGENTS.md`) |
| `src/test/` | 컨트롤러/통합 테스트 (`spring-security-test`) |

## For AI Agents

### Working In This Directory
- **presentation + application 레이어를 담당**: 컨트롤러는 도메인을 직접 호출하지 않고 도메인별 서비스를 통해서만 호출한다. JPA Repository·`EntityManager`를 직접 주입하지 않는다.
  - **도메인당 CQRS 분리**가 이 모듈의 표준 구조다. 과거 `core-module`의 `application/` 계층이 하던 역할이 전환으로 이 모듈의 도메인 패키지로 내려왔고, 도메인당 두 서비스로 분해된다.
    - `{도메인}CommandService`(`@Transactional`): domain write 포트(`XxxRepository`)·도메인 서비스만 주입. 생성/수정/삭제/상태전이를 수행하고 **식별자만 반환**한다. POJO 도메인은 더티 체킹이 없으므로 변경 후 반드시 `repository.save(domain)`을 호출한다.
    - `{도메인}QueryService`(`@Transactional(readOnly = true)`): `application-common-module`의 `{Ctx}QueryPort` 인터페이스만 주입(`infrastructure-module`의 DAO 구현체는 알지 않는다). 조회와 Response 조립(private 매퍼)을 담당한다.
    - 조회만 있는 도메인은 QueryService만 둔다(reference: `notice/NoticeQueryService`, `banner/BannerQueryService`, `policy/PolicyQueryService`). 명령만 있는 도메인은 CommandService만 둔다(reference: `bug/BugReportCommandService`, `partnership/PartnershipCommandService`). 둘 다 있는 도메인은 컨트롤러가 필요한 쪽을 각각 주입한다(reference: `order/OrderCommandService`+`OrderQueryService`, `payment/`, `reservation/`, `review/`, `shop/`).
    - CommandService가 `..query..`를, QueryService가 write 포트를 서로 주입하지 않는다. command 결과 응답은 커밋 이후 컨트롤러가 QueryService로 재조회해 조립한다.
    - 인증·소셜 로그인·파일 등 애그리거트 CRUD가 아닌 흐름 지향 서비스는 CQRS 쌍이 아닌 단일 서비스로 남아 있다(`auth/AuthService`, `member/MemberService`, `file/FileService`, `grade/GradeService`, `referral/ReferralService`).
  - 컨트롤러는 `com.tastyhouse.domain.*`를 import하지 않는다(ID는 `Long`, enum 후보는 `String`으로 받아 서비스에서 승격).
  - **QueryDSL도 infrastructure도 모른다 (개정)**: `src/main`에 `com.querydsl.*` import·`@QueryProjection` 선언·`com.tastyhouse.infrastructure..` import가 **전면 0건**이며, `architecture/LayerRulesTest`(ArchUnit)가 이를 차단한다(`shouldNotDependOnQuerydsl`·`shouldNotDependOnInfrastructurePersistence`, 그리고 읽기 포트 직접 주입을 막는 `controllersShouldNotDependOnQueryDaos`·`commandServicesShouldNotDependOnQueryDaos`). 챕터 04의 임시 장치였던 `shouldNotDependOnInfrastructureQuery`와 이중 패키지 매칭은 챕터 05에서 제거됐다 — infra query 패키지 자체가 api 모듈에서 참조 불가능해져 규칙이 공허해졌기 때문이다. 조회 계약(`{Ctx}QueryPort`·Result·SearchCondition)은 `application-common-module`(`com.tastyhouse.application..port.out..`)에서 import한다.
- 도메인별 폴더(`member/`, `order/`, `shop/` …) 안에 `request/`·`response/` DTO를 둔다.
- **import 순서 — presentation 내부 서브정렬**: 자사 import의 presentation 계층(`com.tastyhouse.webapi.*`) 안에서는 공용 인프라(`common`·`config`·`security`·`ratelimit`·`exception`)를 도메인 전용(`<도메인>.request`·`.response`)보다 **위**에 둔다. 각 서브그룹 내부는 알파벳순, 사이 빈 줄 없음. 상세·근거·예시는 루트 CLAUDE.md 참고.
- **DTO 조립 시 `new` 직접 호출 지양**: 컨트롤러·서비스에서 response/condition을 `new`로 조립하지 않고, 대상 record 자신의 정적 팩토리 `of(...)`/`from(...)`로 위임한다. **Request record가 `toCommand(...)`를 소유**하고 컨트롤러가 `XxxCommand command = request.toCommand(...);`로 조립해 UseCase에 넘긴다(완전 매핑 — 매핑은 인바운드 어댑터의 책임). 경로 변수·인증 주체처럼 본문에 없는 값은 `toCommand`의 파라미터로 주입한다. Response record는 `core-free`(도메인 타입 미import)로 두고, Result → 원시 파라미터 언패킹은 서비스의 private 매퍼(`toXxxResponse(XxxResult dto)`)가 담당한다. 예외: `PaginationResponse.from(PageResult<T>)`는 공용 페이징 계약 위임이라 그대로 둔다. 상세는 루트 CLAUDE.md 참고.
- **식별자(`XxxId.of(id)`)는 지역 변수로 추출 후 넘긴다**: CQRS 전환으로 core command 서비스 호출과 command DTO 자체는 사라졌지만, `{도메인}CommandService`가 HTTP 경계의 `Long`을 domain VO로 승격하는 지점에서 이 관례가 유지된다 — `XxxId xxxId = XxxId.of(id);`로 먼저 추출한 뒤 write 포트·도메인 서비스에 전달하고, 인자 자리에 `.of(` 호출을 인라인하지 않는다(reference: `reservation/ReservationCommandService`의 `cancel`·`confirm`·`reject`·`complete`). 대체된 인라인 한 줄을 `//` 주석으로 남기지 않는다. query(조회) 서비스 호출에 넘기는 `XxxId.of(id)`와 응답 변환 `XxxResponse.from(...)`은 인라인을 유지한다. 상세는 루트 CLAUDE.md 참고.
- **`record`는 별도 파일로 분리**: 서비스·컨트롤러 본문 안에 응답 record를 중첩 선언하지 않고 도메인 폴더의 `response/`에 `public record`로 둔다(reference: `notice/response/NoticeListItemResponse`, `order/response/OrderDetailResponse`). 표준 4필드 페이징 응답은 도메인 폴더에 만들지 않고 `common/PaginationResponse<T>` 공용 제네릭을 재사용한다. 조회 Result·SearchCondition은 이 모듈이 아니라 infrastructure-module `<ctx>/query/` 소유다. 상세는 루트 CLAUDE.md 참고.
- **GET 조회 파라미터는 개수와 무관하게(1개여도) `@ModelAttribute` Request record로 받는다**: 개별 `@RequestParam`을 나열하지 않고 `{도메인}SearchRequest`(`request/`, `public record`)로 묶어 `@Valid @ModelAttribute`로 받고, 페이징 `PageRequest`는 별도 인자로 병기한다. SearchRequest는 `toCondition()` 없는 순수 홀더로 두고 컨트롤러가 원시 필드로 언패킹해 `{도메인}QueryService`에 넘기며, 그 서비스가 infra `<ctx>/query/`의 `{도메인}SearchCondition.of(...)`로 조립한다(enum=`String`/`List<String>`, FK=`Long`, Swagger는 필드 `@Schema(allowableValues={...})`, 기본값은 compact constructor에서 정규화). 조회 파라미터가 없는(페이징만) GET·`@PathVariable`만 받는 단건 조회는 대상 아님. 상세는 루트 CLAUDE.md 참고.
- **`@PathVariable` 주 리소스는 `id`로 통일한다**: 컨트롤러가 `@RequestMapping`으로 이미 그 도메인에 스코프되므로 주 리소스 식별자는 단건 CRUD·중첩 하위 경로 모두 bare `id`로 쓰고 한 컨트롤러 안에서 `id`/`{도메인}Id` 혼재를 금지한다. 단, 한 경로가 서로 다른 애그리거트 식별자를 둘 이상 받을 때(예: `/members/{memberId}/orders/{orderId}`)만 각각을 `{도메인}Id`로 구분한다. 타입은 `Long` 유지(`@PathVariable Long id`) 후 `{도메인}CommandService`/`{도메인}QueryService`에서 `XxxId.of(...)`로 승격한다. 상세는 루트 CLAUDE.md 참고.
- **Request/Response record는 `@Schema`로 완전히 문서화한다**: 타입 레벨에 `@Schema(description = ...)`, 모든 필드에 `@Schema(description = ..., example = ...)`를 붙인다(컬렉션·중첩 record 필드는 example 생략 가능하나 description은 필수). Request는 Bean Validation 어노테이션 다음 줄에 `@Schema`를 두고 필수 필드는 `requiredMode = Schema.RequiredMode.REQUIRED`로 표시하며, enum 후보 `String`/`List<String>` 필드는 기존 `allowableValues = {...}`에 `description`을 병기한다(reference: `order/request/OrderCreateRequest`, `reservation/response/ReservationDetailResponse`). 상세는 루트 CLAUDE.md 참고.
- 401 처리도 비즈니스 예외와 동일하게 `domain-module`의 `BusinessException` 계열(`com.tastyhouse.domain.exception`)에 `ErrorCode.AUTH_*`(401)를 담아 사용 — 전역 처리는 `exception/GlobalExceptionHandler`. (web-api 로컬 `UnauthorizedException`은 제거됨: errorCode 없는 401을 내보내 다른 401과 계약이 어긋났다.)

### Testing Requirements
- `@WebMvcTest` / `@SpringBootTest` + `spring-security-test`로 인증 흐름 검증.
- **레이어 경계는 `src/test/.../architecture/LayerRulesTest`(ArchUnit)가 강제**한다 — `applicationServicesShouldNotDependOnWebLayer`(클래스명 `*CommandService`/`*QueryService`로 대상을 잡아 `org.springframework.web.bind..`/`web.servlet..`/`org.springframework.http..`/`jakarta.servlet..` 의존 차단. `MultipartFile`은 업로드 경계 타입이라 제외), `controllersShouldNotDependOnRepositories`, `controllersShouldNotDependOnQueryDaos`, `commandServicesShouldNotDependOnQueryDaos`, `shouldNotDependOnQuerydsl`, `shouldNotDependOnInfrastructurePersistence`, 그리고 챕터 05에서 패키지 기준으로 승격한 `webAdaptersShouldNotDependOnApplicationServices`(`..adapter.in.web..` → `..application.service..` 금지)·`portInShouldBeFreeOfWebDomainAndInfrastructure`. `allowEmptyShould(true)`를 쓰지 않으므로 대상 클래스가 0건이면 **공허 통과가 아니라 실패**로 드러난다(과거 `..application..` 패키지를 매칭하던 규칙이 전환 후 대상 0건으로 공허 통과하던 문제를 이렇게 해소했다).

### Common Patterns
- **JWT 인증 메커니즘(access/refresh 발급·검증·필터·EntryPoint·AccessDeniedHandler)은 `security-module`의 `com.tastyhouse.security.jwt`에 공유**된다. web-api의 `config/jwt/JwtTokenProvider`는 그 공용 provider를 상속해 `memberId` 클레임·`CustomUserDetails` 재구성을 주입하고, **web 전용 검증 토큰(휴대폰/이메일/개인정보/비밀번호 재설정) 발급 메서드만 추가**한다. 공용 필터는 `config/jwt/JwtConfig`가 web 전용 블랙리스트 저장소로 빈 등록한다.
- **`scanBasePackages`에 domain 엔트리 없음**: `WebApiApplication`의 `scanBasePackages`는 `com.tastyhouse.webapi`·`com.tastyhouse.infrastructure`·`com.tastyhouse.external`·`com.tastyhouse.security`·`com.tastyhouse.logging` 다섯 개다. `domain-module`에는 `@Component`/`@Service`/`@Configuration`이 하나도 없어(도메인 서비스는 POJO, 빈 등록은 infra `<ctx>/config/<Ctx>DomainConfig`) domain 스캔 엔트리를 제거했다.
- **정책은 web-api에 잔류**: `config/security/`의 `SecurityConfig`(공개 경로·CORS 헤더 `X-Verify-Token` 등)·`PublicPaths`·`CustomUserDetails`(`JwtPrincipal` 구현)·`CustomUserDetailsService`, `config/jwt/`의 `TokenService`·`RedisRepositoryConfig`.
- **`jwt.secret`은 admin-api와 반드시 달라야 한다**(web=`JWT_SECRET_WEB`). 동일 시크릿이면 회원 토큰이 admin 인증을 통과한다 — 상세는 `security-module/AGENTS.md`.
- 소셜 로그인은 `auth/{kakao,naver,apple,facebook}` — 실제 외부 호출은 `external-api`에 위임.
- 응답은 공통 래퍼로 일관화 — `ApiResponse`/`PaginationResponse`/`PageRequest`와 `FileService`는 이 모듈이 아니라 **`api-common-module`(`com.tastyhouse.apicommon`) 소유**다.
- **`GlobalExceptionHandler`만은 이 모듈에 잔류**한다: web 전용 핸들러 4종(`ExternalApiException`·`NoHandlerFoundException`·`MissingServletRequestParameterException`·`MethodArgumentTypeMismatchException`)과 "필드명: 메시지" 형식의 검증 실패 응답이 admin/ceo와 다른 **응답 계약 차이**이기 때문이다. 그래서 `WebApiApplication`은 `com.tastyhouse.apicommon` 전체가 아니라 **`com.tastyhouse.apicommon.file`만** 스캔한다 — `exception` 패키지까지 스캔하면 `@RestControllerAdvice`가 2개가 된다.
- **소셜 로그인은 `external.oauth.spi` SPI로만 사용**한다: 소셜 서비스 4종은 `SocialOAuthClient`(`@Qualifier`로 제공자 지정)·`SocialProfile`만 알고 제공자별 wire DTO를 import하지 않는다. 제공자별 Redis 임시토큰 저장소·`*_TEMP_TOKEN_EXPIRED` ErrorCode는 제공자별로 유지한다(key prefix 변경 금지). ArchUnit `shouldDependOnOauthSpiOnlyNotProviderPackages`가 강제.
- **등록(POST) API는 생성된 `Long` id만 반환**한다: `ResponseEntity<ApiResponse<Long>>`로 PK 하나만 반환하고, **생성 직후 `{도메인}QueryService`로 재조회해 상세 DTO를 반환하지 않는다**(과거 이 모듈만 등록 8종이 재조회 DTO 형태였으나 전면 전환됨). 생성 응답 전용 래퍼 record(`OrderCreateResponse` 등)를 만들지 않고, 행을 생성하고도 `ApiResponse<Void>`를 반환하지 않는다(`signUp`·`follow`도 id 반환). 상세가 필요한 클라이언트는 그 id로 GET 상세를 호출한다. 파일 업로드(`FileApiController#upload`)·인증/토큰 발급(소셜 로그인·`signUpSocialAccount`·인증코드 확인 등)·토글(`toggleBookmark`·`toggleReviewLike`)·상태전이(payment `confirm`/`cancel`/`refund`, reservation `confirm`/`reject`/`complete`)·POST-as-query(`getProductsBatch`)는 적용 제외. 상세는 루트 CLAUDE.md 참고.

## Dependencies

### Internal
- `domain-module` (implementation) — 도메인 모델·VO·write 포트·도메인 서비스·`ErrorCode`/`BusinessException`
- `application-common-module` (implementation) — `{Ctx}QueryPort` 인터페이스·Result DTO·SearchCondition 주입용
- `infrastructure-module` (implementation) — 빈 스캔 대상(`{도메인}QueryDao`가 실제로 뜨는 곳)이지만 `com.tastyhouse.infrastructure..`는 소스 레벨에서 import하지 않는다(ArchUnit이 차단)
- `external-api` — OAuth/결제/메시징/파일 어댑터
- `security-module` — 공용 JWT 메커니즘·Redis 토큰 저장소·rate limit
- `logging-module` — 요청/응답 로깅(p6spy 전이)

### External
- Spring Web/WebFlux/Security, Redis, AOP, Validation
- JJWT 0.13.0, springdoc-openapi 2.3.0 (p6spy는 logging-module 경유 전이 의존)

<!-- MANUAL: -->

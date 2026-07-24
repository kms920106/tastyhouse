<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-21 -->

# admin-api

## Purpose
관리자용 REST API 애플리케이션 (실행 가능한 Spring Boot bootJar). `admin`(관리자 계정)·`auth`(로그인/JWT)·`banner`·`bug`(버그 제보)·`coupon`·`event`·`faq`·`member`(회원 관리)·`notice`·`policy` 등 다수 도메인 관리 API를 제공하며, `core-module`의 동일한 application 서비스를 재사용한다. 사용자 API(web-api)와 application 서비스를 공유하므로 command/query 시그니처 변경 시 동시 수정이 필요하다.

컨트롤러가 `core-module`에 직접 결합되는 것을 막기 위해, 도메인별로 admin-api 소속 `{도메인}Service`를 두어 컨트롤러와 core 사이를 중개하는 패턴을 도입한다(과거 이 계층을 "Facade"로 부르던 문서 표현은 정정되었으며, 클래스 네이밍은 예외 없이 `Service`로 통일되어 있다 — reference 구현: `notice/NoticeService`). 이 Service가 core application 서비스 호출과 core DTO↔admin-api Request/Response 변환을 전담하므로, 컨트롤러는 `com.tastyhouse.core.*`를 import하지 않고 admin-api 타입(Service·Request·Response)에만 의존한다. 신규 도메인은 이 패턴을 따르고, 기존 컨트롤러(policy 등)도 수정 시 점진적으로 전환한다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | web + springdoc 의존, `core-module`·`external-api` 참조 |
| `src/main/resources/` | 관리자 앱 환경 설정 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/adminapi/` | 관리자 컨트롤러 루트 — `common/`(공용 인프라: `ApiResponse`·`PageRequest` 등), `config/`(JWT·Security), `admin/`·`auth/`·`banner/`·`bug/`·`coupon/`·`event/`·`faq/`·`file/`·`member/`·`notice/`·`policy/`(각 도메인 폴더는 컨트롤러 + `{도메인}Service` + `request/`·`response/`) |
| `src/test/` | 관리자 API 테스트 |

## For AI Agents

### Working In This Directory
- 새 관리 기능 추가 시 web-api와 동일한 도메인-폴더 + `request/`·`response/` 컨벤션을 따른다.
- **import 순서 — presentation 내부 서브정렬**: 자사 import의 presentation 계층(`com.tastyhouse.adminapi.*`) 안에서는 공용 인프라(`common`·`config`)를 도메인 전용(`<도메인>.request`·`.response`)보다 **위**에 둔다. 각 서브그룹 내부는 알파벳순, 사이 빈 줄 없음. 상세·근거·예시는 루트 CLAUDE.md 참고.
- 도메인별로 admin-api 소속 `{도메인}Service`를 두고, 컨트롤러는 Service만 주입한다 — 컨트롤러에서 `com.tastyhouse.core.*`(core service/DTO/command)를 직접 import하지 않는다. core 호출과 DTO↔Request/Response 변환은 이 Service가 전담한다.
- **도메인 enum도 컨트롤러/Request에 core 타입으로 노출하지 않는다** — HTTP 경계는 `String`(다중값 `List<String>`)으로 받고, Service에서 core enum의 `Enum.from(String)` 정적 팩토리로 승격한다(ID를 `Long`으로 받아 `XxxId.of()`로 승격하는 것과 대칭). String 파라미터에는 `@Schema(allowableValues={...})`/`@Parameter(...)`로 Swagger 후보값을 명시하고, 변환 실패는 core enum `from()` 내부에서 `BusinessException(ErrorCode.XXX_TYPE_UNKNOWN)`으로 처리한다(reference: `banner/BannerService`, `BannerType.from`). 상세는 루트 CLAUDE.md 참고.
- 비즈니스 로직은 `core-module` application 서비스에 위임 — admin 전용 로직이라도 가능하면 core에 둔다. 이 Service는 위임·변환 계층일 뿐 비즈니스 로직을 담지 않는다.
- core의 `PageResult`도 컨트롤러에 노출하지 않는다 — 이 Service가 공용 제네릭 `common/PaginationResponse<T>`(예: `PaginationResponse<NoticeListItemResponse>`)로 변환해 반환한다. 도메인별 `XxxPageResponse` 래퍼는 만들지 않는다 — 상세는 루트 CLAUDE.md의 "페이징 응답 공용 제네릭 래퍼 규칙" 참고.
- **DTO 조립 시 `new` 직접 호출 지양**: Service에서도 command/condition/response를 `new`로 조립하지 않는다. 이 Service는 Request 타입이 아니라 개별 원시 파라미터(예: `String title, String content, boolean visible`)를 받고, 내부에서 대상 record의 정적 팩토리 `of(...)`/`from(...)`로 위임한다(reference: `NoticeService#createNotice(String, String, boolean)` → `CreateNoticeCommand.of(...)`, `NoticeSearchCondition.of(...)`, `PaginationResponse.from(...)`). Request DTO에는 `toCommand()` 같은 변환 메서드를 두지 않는다 — 컨트롤러가 Request를 원시 필드로 언패킹해 Service에 전달한다. 상세는 루트 CLAUDE.md 참고.
- **command/DTO와 식별자(`XxxId.of(id)`)는 지역 변수로 추출 후 넘긴다(command 서비스 호출 인자 인라인 조립 지양)**: `Xxx.of(...)` 팩토리 결과를 core command 서비스 호출 인자에 인라인으로 바로 넘기지 않고, 먼저 `Xxx command = Xxx.of(...);`로 추출한 뒤 전달한다. **command 서비스에 넘기는 식별자 승격 `XxxId.of(id)`도 지역 변수로 추출한다** — command와 함께 넘길 때(`XxxId xxxId = XxxId.of(id); service.method(xxxId, command);`)든 식별자만 단독으로 넘길 때(`delete`·상태전이)든 인자 자리에 인라인하지 않는다(이전 "짧은 식별자 승격은 인자 자리 허용" 예외 폐지). 조립과 호출을 문장 단위로 분리해 가독성·디버깅·도메인 간 형태 일관성을 높이기 위함이다. 예외(인라인 유지): 이미 지역 변수인 `SearchCondition`, **query(조회) 서비스 호출**에 넘기는 `XxxId.of(id)`, **`Command.of(XxxId.of(id), ...)`처럼 command 팩토리 내부에 중첩된 `Id.of`**(이때는 command만 추출), 응답 변환 `XxxResponse.from(...)`. 대체된 인라인 한 줄을 `//` 주석으로 남기지 않는다(reference: `NoticeService#updateNotice`·`#deleteNotice`, 식별자 추출 동일 적용 `banner`·`coupon`·`policy` `{도메인}Service` / `web-api`의 `reservation`·`scheduler`, command 추출 `bug`·`admin`). 상세는 루트 CLAUDE.md 참고.
- **`record`는 별도 파일로 분리**: `{도메인}Service`·컨트롤러 본문 안에 응답·결과 record를 중첩 선언하지 않고 도메인 폴더의 `response/`에 `public record`로 둔다(reference: `notice/response/NoticeListItemResponse`). 단, 표준 4필드 페이징 응답은 도메인 폴더에 만들지 않고 `common/PaginationResponse<T>`를 재사용한다. 상세는 루트 CLAUDE.md 참고.
- **GET 조회 파라미터는 개수와 무관하게(1개여도) `@ModelAttribute` Request record로 받는다**: 개별 `@RequestParam`을 나열하지 않고 `{도메인}SearchRequest`(`request/`, `public record`)로 묶어 `@Valid @ModelAttribute`로 받고, 페이징 `PageRequest`는 별도 인자로 병기한다. SearchRequest는 `toCondition()` 없는 순수 홀더로 두고 컨트롤러가 원시 필드로 언패킹해 Service에 넘기며, 이 Service는 개별 원시 파라미터를 받아 `{도메인}SearchCondition.of(...)`로 조립한다(`{도메인}Service` 시그니처 무변경). enum=`String`/`List<String>`, FK=`Long`, Swagger는 필드 `@Schema(allowableValues={...})`, 기본값은 compact constructor에서 정규화. 조회 파라미터가 없는(페이징만) GET·`@PathVariable`만 받는 단건 조회는 대상 아님. 기존 `@RequestParam` GET(`notice/getNotices` 등)은 수정 시 전환한다(reference: `NoticeSearchRequest`, 정착 선례 `common/PageRequest`). 상세는 루트 CLAUDE.md 참고.
- **`@PathVariable` 주 리소스는 `id`로 통일한다**: 컨트롤러가 `@RequestMapping`으로 이미 그 도메인에 스코프되므로 주 리소스 식별자는 단건 CRUD·중첩 하위 경로 모두 bare `id`로 쓰고(예: `/coupons/v1/{id}`, `/coupons/v1/{id}/issues`) 한 컨트롤러 안에서 `id`/`{도메인}Id` 혼재를 금지한다. 다른 애그리거트를 함께 참조하는 경우만 그 식별자를 `{도메인}Id`로 구분한다. 타입은 `Long` 유지(`@PathVariable Long id`) 후 Service에서 `XxxId.of(...)`로 승격한다(reference: `coupon/CouponApiController`). 상세는 루트 CLAUDE.md 참고.
- **중첩 경로의 부모 `@PathVariable`이 완전히 미사용이면 경로를 평탄화한다**: 하위 리소스 식별자가 전역 유니크 PK라 부모 없이 단독 식별이 가능하고, 핸들러·`{도메인}Service`·core 어디서도 부모 `id`를 쓰지 않는다면(소속 검증 로직 없음) `/v1/{id}/{하위리소스}/{하위id}` 경로에서 부모 세그먼트를 제거해 `/v1/{하위리소스}/{하위id}`로 바꾸고 미사용 `@PathVariable`을 제거한다(reference: `event/EventApiController#deleteWinner`). 부모 id를 소속 검증에 실제로 쓰는 다른 핸들러는 대상이 아니다. 상세는 루트 CLAUDE.md 참고.
- **Request/Response record는 `@Schema`로 완전히 문서화한다**: 타입 레벨에 `@Schema(description = ...)`, 모든 필드에 `@Schema(description = ..., example = ...)`를 붙인다(컬렉션·중첩 record 필드는 example 생략 가능하나 description은 필수). Request는 Bean Validation 어노테이션 다음 줄에 `@Schema`를 두고 필수 필드는 `requiredMode = Schema.RequiredMode.REQUIRED`로 표시하며, enum 후보 `String`/`List<String>` 필드는 기존 `allowableValues = {...}`에 `description`을 병기한다(reference: `notice/request/NoticeUpdateRequest`, `banner/response/BannerListItemResponse`). 상세는 루트 CLAUDE.md 참고.
- **Rate Limit은 전면 적용하지 않고 `auth` 로그인 엔드포인트에만 최소 적용한다 (결정 근거)**: admin-api는 인증된 소수 관리자만 접근하는 내부 관리용 API이므로 일반 사용자 트래픽을 겨냥한 전면적 rate limit(검색·조회 API 등)은 불필요하다고 판단했다 — web-api처럼 불특정 다수가 호출하는 공개/준공개 엔드포인트가 없고, 트래픽 규모도 본질적으로 다르다. 다만 `auth/AuthApiController#login`만은 예외로 두었다: 로그인은 인증 이전 단계라 "인증된 관리자만 접근"이라는 전제가 적용되지 않고, 무차별 대입(brute-force)으로 관리자 계정을 탈취당하면 서비스 전체에 영향을 미치는 고위험 엔드포인트이기 때문이다. 이 판단에 따라 `web-api`의 `ratelimit/` 패키지(`RateLimit`/`RateLimitAspect`/`RateLimiterService`/`RateLimitKeyType`) 구조를 그대로 참고해 `adminapi.ratelimit` 패키지에 최소 버전을 두고, `AuthApiController#login`에만 `@RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:admin_login")`을 적용했다(web-api `login`과 동일한 제한값). `RateLimitException`은 `adminapi.exception` 패키지에 두고 `GlobalExceptionHandler`에서 429로 처리한다(web-api와 동일 패턴). **web-api와 동일 코드가 두 모듈에 중복되는 것은 감수**했다 — 두 모듈 다 공용 인프라 모듈에 대한 의존이 없는 현재 구조에서 이 작업만을 위해 신규 공용 모듈을 도입하는 것은 과잉이라고 판단했다(공용화 여부는 별도 작업지시서의 common 정리 범위). `admin-api/build.gradle`은 이미 `spring-boot-starter-aop`(및 `spring-boot-starter-data-redis`)를 갖고 있어 추가 의존성 도입 없이 구현했다. 다른 도메인(banner/coupon/notice 등)의 CRUD 엔드포인트는 이 결정에 따라 rate limit 대상이 아니다.

### Testing Requirements
- `@SpringBootTest` 기반 컨트롤러 검증.

### Common Patterns
- 컨트롤러 → `{도메인}Service` → `core-module` application 서비스의 2-hop 구조. 컨트롤러는 HTTP 매핑만, 이 Service는 위임·변환만 담당한다.
- application 서비스 공유(web-api와 동일 core 서비스)로 인한 회귀 주의 — command/query 시그니처 변경 시 web-api와 동시 수정.
- **JWT 인증 메커니즘은 `security-module`의 `com.tastyhouse.security.jwt`에 공유**된다. admin-api의 `config/jwt/JwtTokenProvider`는 그 공용 provider를 상속해 `adminId` 클레임·`CustomUserDetails` 재구성만 주입한다(검증 토큰 없음). 공용 필터는 `config/jwt/JwtConfig`가 admin 전용 블랙리스트 저장소(`admin:bl:`)로 빈 등록한다. 정책은 admin-api에 잔류: `config/security/SecurityConfig`·`PublicPaths`·`CustomUserDetails`(`JwtPrincipal` 구현)·`AdminUserDetailsService`, `config/jwt/TokenService`·`RedisRepositoryConfig`.
- **인가 체인은 `.anyRequest().hasAnyRole("ADMIN", "SUPER_ADMIN")`**로 관리자 역할을 강제한다(심층 방어). `@PreAuthorize`는 더 세분화된 제어(예: `AdminApiController`의 `SUPER_ADMIN`)에만 추가로 쓴다.
- **`jwt.secret`은 web-api와 반드시 달라야 한다**(admin=`JWT_SECRET_ADMIN`). 동일 시크릿이면 회원 토큰이 admin 인증을 통과하는 권한 상승이 발생한다 — 상세는 `security-module/AGENTS.md`. 시크릿 교체 시 기존 admin 세션은 전면 무효화되므로 배포 시 관리자 재로그인 안내가 필요하다.

## Dependencies

### Internal
- `core-module`, `external-api`

### External
- Spring Web, springdoc-openapi 2.3.0, Lombok

<!-- MANUAL: -->

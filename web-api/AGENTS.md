<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-05 -->

# web-api

## Purpose
최종 사용자용 REST API 애플리케이션 (실행 가능한 Spring Boot bootJar). 도메인별 컨트롤러와 Request/Response DTO를 제공하며, JWT 기반 인증·소셜 로그인(OAuth)·Spring Security·Redis 캐시·요청 제한(rate limit)·스케줄러·로깅을 담당한다. 비즈니스 로직은 직접 구현하지 않고 `core-module`의 application(Command/Query) 서비스만 호출한다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | web, webflux, security, data-redis, aop, validation, JJWT, springdoc 의존 (p6spy는 logging-module이 api로 전이) |
| `src/main/resources/` | `application.yml` 등 환경 설정 (로깅 설정은 logging-module의 `application-logging.yml`을 import) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/webapi/` | 컨트롤러·인증·설정 등 프레젠테이션 코드 루트 (see `src/main/java/com/tastyhouse/webapi/AGENTS.md`) |
| `src/test/` | 컨트롤러/통합 테스트 (`spring-security-test`) |

## For AI Agents

### Working In This Directory
- **presentation 레이어만 담당**: 컨트롤러는 core를 직접 호출하지 않고 도메인별 `{도메인}Service`를 통해서만 호출한다(과거 "Facade"로 부르던 계층과 동일하며, 클래스 네이밍은 예외 없이 `Service`로 통일되어 있다 — 상세는 루트 CLAUDE.md 참고). JPA Repository를 직접 주입하지 않는다.
  - web-api도 admin-api와 동일하게 도메인별 `{도메인}Service`를 통해 core를 호출한다(reference: `admin-api/notice/NoticeService`, `order/OrderService`, `member/MemberService`, `auth/AuthService`). 이 Service가 core 서비스 호출과 core DTO↔Request/Response 변환을 전담하며, 컨트롤러는 `com.tastyhouse.core.*`를 import하지 않는다.
- 도메인별 폴더(`member/`, `order/`, `shop/` …) 안에 `request/`·`response/` DTO를 둔다.
- **import 순서 — presentation 내부 서브정렬**: 자사 import의 presentation 계층(`com.tastyhouse.webapi.*`) 안에서는 공용 인프라(`common`·`config`·`security`·`ratelimit`·`exception`)를 도메인 전용(`<도메인>.request`·`.response`)보다 **위**에 둔다. 각 서브그룹 내부는 알파벳순, 사이 빈 줄 없음. 상세·근거·예시는 루트 CLAUDE.md 참고.
- **DTO 조립 시 `new` 직접 호출 지양**: 컨트롤러에서 command/condition/response를 `new`로 조립하지 않고, 대상 record 자신의 정적 팩토리 `of(...)`/`from(...)`로 위임한다. Request DTO에는 `toCommand()` 같은 변환 메서드를 두지 않는다 — 단순 매핑은 컨트롤러가 Request를 원시 필드로 언패킹해 `Command.of(...)`를 직접 호출하고, 복잡한 중첩 요청은 private 변환 헬퍼로 분리할 수 있다(reference: `order/OrderApiController#toCreateOrderCommand`). 상세는 루트 CLAUDE.md 참고.
- **command/DTO와 식별자(`XxxId.of(id)`)는 지역 변수로 추출 후 넘긴다(command 서비스 호출 인자 인라인 조립 지양)**: `Xxx.of(...)` 팩토리 결과를 core command 서비스 호출 인자에 인라인으로 바로 넘기지 않고 `Xxx command = Xxx.of(...);`로 추출한 뒤 전달한다. **command 서비스에 넘기는 `XxxId.of(id)` 식별자 승격도 지역 변수로 추출한다** — command와 함께(`XxxId xxxId = XxxId.of(id); service.method(xxxId, command);`)든 식별자만 단독(`cancel`·`confirm` 등 상태전이)이든 인자 자리에 인라인하지 않는다. 예외(인라인 유지): 이미 지역 변수인 `SearchCondition`, **query(조회) 서비스 호출**의 `XxxId.of(id)`, **`Command.of(XxxId.of(id), ...)`처럼 command 팩토리 내부에 중첩된 `Id.of`**(이때는 command만 추출 — reference: `review/ReviewService`), 응답 변환 `XxxResponse.from(...)`. 대체된 인라인 한 줄을 `//` 주석으로 남기지 않는다(reference: `reservation/ReservationService`의 `cancel`·`confirm`·`reject`·`complete`, `scheduler/ProductScheduler#markBbqOptionsSynced`). 상세는 루트 CLAUDE.md 참고.
- **`record`는 별도 파일로 분리**: `{도메인}Service`/컨트롤러 본문 안에 응답·결과 record를 중첩 선언하지 않고 도메인 폴더의 `response/`에 `public record`로 둔다(reference: `notice/response/NoticeListPageResult`, `order/response/OrderListPageResult`). 상세는 루트 CLAUDE.md 참고.
- **GET 조회 파라미터는 개수와 무관하게(1개여도) `@ModelAttribute` Request record로 받는다**: 개별 `@RequestParam`을 나열하지 않고 `{도메인}SearchRequest`(`request/`, `public record`)로 묶어 `@Valid @ModelAttribute`로 받고, 페이징 `PageRequest`는 별도 인자로 병기한다. SearchRequest는 `toCondition()` 없는 순수 홀더로 두고 컨트롤러가 원시 필드로 언패킹해 `{도메인}Service`에 넘긴다(enum=`String`/`List<String>`, FK=`Long`, Swagger는 필드 `@Schema(allowableValues={...})`, 기본값은 compact constructor에서 정규화). 조회 파라미터가 없는(페이징만) GET·`@PathVariable`만 받는 단건 조회는 대상 아님. 기존 `@RequestParam` GET(다중 `shop/getLatestShops`, 단일 `search/searchMenus`의 `query` 등)은 수정 시 전환한다(reference: `ShopSearchRequest`, 정착 선례 `common/PageRequest`). 상세는 루트 CLAUDE.md 참고.
- **`@PathVariable` 주 리소스는 `id`로 통일한다**: 컨트롤러가 `@RequestMapping`으로 이미 그 도메인에 스코프되므로 주 리소스 식별자는 단건 CRUD·중첩 하위 경로 모두 bare `id`로 쓰고 한 컨트롤러 안에서 `id`/`{도메인}Id` 혼재를 금지한다. 단, 한 경로가 서로 다른 애그리거트 식별자를 둘 이상 받을 때(예: `/members/{memberId}/orders/{orderId}`)만 각각을 `{도메인}Id`로 구분한다. 타입은 `Long` 유지(`@PathVariable Long id`) 후 `{도메인}Service`에서 `XxxId.of(...)`로 승격한다. 상세는 루트 CLAUDE.md 참고.
- **Request/Response record는 `@Schema`로 완전히 문서화한다**: 타입 레벨에 `@Schema(description = ...)`, 모든 필드에 `@Schema(description = ..., example = ...)`를 붙인다(컬렉션·중첩 record 필드는 example 생략 가능하나 description은 필수). Request는 Bean Validation 어노테이션 다음 줄에 `@Schema`를 두고 필수 필드는 `requiredMode = Schema.RequiredMode.REQUIRED`로 표시하며, enum 후보 `String`/`List<String>` 필드는 기존 `allowableValues = {...}`에 `description`을 병기한다(reference: `order/request/OrderCreateRequest`, `reservation/response/ReservationDetailResponse`). 상세는 루트 CLAUDE.md 참고.
- 401 처리는 web-api의 `UnauthorizedException`을, 비즈니스 예외는 `core-module`의 `BusinessException` 계열을 사용 — 전역 처리는 `exception/GlobalExceptionHandler`.

### Testing Requirements
- `@WebMvcTest` / `@SpringBootTest` + `spring-security-test`로 인증 흐름 검증.

### Common Patterns
- **JWT 인증 메커니즘(access/refresh 발급·검증·필터·EntryPoint·AccessDeniedHandler)은 `security-module`의 `com.tastyhouse.security.jwt`에 공유**된다. web-api의 `config/jwt/JwtTokenProvider`는 그 공용 provider를 상속해 `memberId` 클레임·`CustomUserDetails` 재구성을 주입하고, **web 전용 검증 토큰(휴대폰/이메일/개인정보/비밀번호 재설정) 발급 메서드만 추가**한다. 공용 필터는 `config/jwt/JwtConfig`가 web 전용 블랙리스트 저장소로 빈 등록한다.
- **정책은 web-api에 잔류**: `config/security/`의 `SecurityConfig`(공개 경로·CORS 헤더 `X-Verify-Token` 등)·`PublicPaths`·`CustomUserDetails`(`JwtPrincipal` 구현)·`CustomUserDetailsService`, `config/jwt/`의 `TokenService`·`RedisRepositoryConfig`.
- **`jwt.secret`은 admin-api와 반드시 달라야 한다**(web=`JWT_SECRET`). 동일 시크릿이면 회원 토큰이 admin 인증을 통과한다 — 상세는 `security-module/AGENTS.md`.
- 소셜 로그인은 `auth/{kakao,naver,apple,facebook}` — 실제 외부 호출은 `external-api`에 위임.
- 응답은 공통 래퍼(`common/`)로 일관화.

## Dependencies

### Internal
- `core-module` — application 서비스, 도메인
- `external-api` — OAuth/결제/메시징/파일 어댑터

### External
- Spring Web/WebFlux/Security, Redis, AOP, Validation
- JJWT 0.13.0, springdoc-openapi 2.3.0 (p6spy는 logging-module 경유 전이 의존)

<!-- MANUAL: -->

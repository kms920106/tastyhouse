<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# webapi (presentation layer)

## Purpose
사용자 대면 REST API 계층이자, 이 앱의 **application 계층**이다. HTTP 요청을 받아 도메인당 CQRS 서비스(`{도메인}CommandService`/`{도메인}QueryService`)를 호출하고 응답 DTO로 변환하여 반환한다(과거 `core-module`의 `application/` 계층이 하던 역할이 전환으로 이 패키지로 내려왔다). JWT 기반 인증, OAuth 소셜 로그인, 요청/응답 로깅, Rate Limiting, 중앙화된 예외 처리를 제공한다. Spring Security + JWT + Redis 기반의 stateless 아키텍처를 구현하며, 외부 API 연동은 external-api 모듈로 위임한다.

## Cross-cutting Packages
| Package | Description |
|---------|-------------|
| `auth/` | JWT 발급/검증, OAuth 로그인(Apple/Facebook/Kakao/Naver), 인증 정보 추출. `service/`는 AuthService(일반 로그인), PhoneLoginService, AuthPasswordResetService 포함. OAuth 각 제공자별 하위 디렉토리. request/response 구분 저장. |
| `config/` | Spring 설정 — SecurityConfig(필터체인, CORS), AsyncConfig(비동기), RedisConfig, WebClientConfig, OpenApiConfig(Swagger). jwt/ 하위에 JwtTokenProvider(발급/검증), JwtAuthenticationFilter, JwtProperties, TokenType. security/ 하위에 JwtAuthenticationEntryPoint, JwtAccessDeniedHandler, CustomUserDetailsService(UserDetailsService 구현), CustomUserDetails(UserDetails 래퍼). PublicPaths 에서 인증 불필요 경로 관리. |
| `exception/` | 중앙화된 예외 처리. GlobalExceptionHandler가 BusinessException (domain-module `com.tastyhouse.domain.exception`, `ExternalApiException`도 이를 상속하므로 같은 핸들러가 처리), RateLimitException, Security 예외, 유효성 검사 예외를 처리하며 RFC7807 `ProblemDetail` + `errorCode` property로 응답(조립은 공용 `apicommon.exception.ProblemDetails`). 레거시 `UnauthorizedException`은 제거되고 `ErrorCode.AUTH_*`(401)로 흡수됨. |
| `logging/` | AOP 기반 요청/응답 로깅. ApiLoggingFilter (서블릿 필터로 전체 요청 추적), ApiLoggingAspect (컨트롤러 진입 로깅), SensitiveFieldMasker (민감정보 마스킹). |
| `security/` | Spring Security 보조 컴포넌트. CurrentUser (메서드 파라미터 주입 애노테이션). JwtAccessDeniedHandler, JwtAuthenticationEntryPoint, CustomUserDetailsService, CustomUserDetails는 config/security/ 에 위치. |
| `common/` | 공통 유틸. ApiResponse (모든 응답의 상위 래퍼, success/error/data), PageRequest (페이징 요청), PaginationResponse<T> (표준 4필드 페이징 응답 공용 제네릭 — 도메인별 `XxxPageResponse`를 만들지 않는다). 서비스가 `{Ctx}QueryPort`(`com.tastyhouse.application..port.out`)로부터 받는 `PageResult<T>`(domain-module `shared/page`)를 `PaginationResponse.from(...)`으로 변환한다. |

## Feature Packages
| Package | Purpose |
|---------|---------|
| `auth/` | 인증 엔드포인트 — 일반/소셜 로그인, 로그아웃, 토큰 갱신, 비밀번호 재설정 |
| `banner/` | 배너 관리 조회 API |
| `bug/` | 버그 리포트 제출 API |
| `event/` | 이벤트 조회 API |
| `faq/` | FAQ 조회 API |
| `file/` | 파일 업로드/다운로드 (S3/Firebase 연동은 external-api로 위임) |
| `follow/` | 사용자 팔로우/언팔로우 관리 API |
| `grade/` | 회원 등급 조회 API |
| `member/` | 회원 프로필 조회, 회원 정보 수정, 탈퇴 등 회원 관리 API |
| `notice/` | 공지사항 조회 API |
| `order/` | 주문 생성, 조회, 취소, 배송 추적 API |
| `partnership/` | 파트너십/제휴 관리 API |
| `payment/` | 결제 생성·승인·취소·현장완료·환불. CQRS 분리(`PaymentCommandService`/`PaymentQueryService`). PG 연동은 external-api의 `payment/toss` 어댑터가 domain 포트 `PgPaymentGateway`를 구현 |
| `policy/` | 약관/정책 조회 API |
| `product/` | 상품 조회, 검색, 필터링 API |
| `rank/` | 순위/랭킹 조회 API |
| `referral/` | 추천/레퍼럴 API |
| `reservation/` | 가게 시간 슬롯 예약 조회, 생성, 취소, 가용성 확인 API |
| `review/` | 리뷰 작성, 조회, 수정, 삭제 API |
| `search/` | 통합 검색 및 검색 로그 기록 API |
| `shop/` | 가게 조회, 상세 정보, 즐겨찾기, 사진/카테고리 조회 API (Place에서 Shop으로 재명명됨) |
| `mail/` | 메일(이메일 주소) 인증 API — 인증코드 발송·확인 |
| `sms/` | SMS(휴대폰번호) 인증 API — 인증코드 발송·확인 |

## For AI Agents

### Working In This Directory
- **요청/응답 DTO는 feature 폴더 내 request/, response/ 서브폴더에 저장** — 도메인별 응집도 향상. 모든 Request/Response record는 타입 레벨 `@Schema(description = ...)`와 필드별 `@Schema(description = ..., example = ...)`를 갖춰 Swagger 문서를 완전하게 유지한다(컬렉션·중첩 record 필드는 example 생략 가능, description은 필수). 상세는 루트 CLAUDE.md 참고.
- **response/ 폴더의 모든 응답 record는 소속 도메인명 접두어로 시작한다** — 중첩·보조 요소 record도 예외 없음(`OptionResponse`가 아니라 `ProductOptionResponse`). 접미어는 `Response`로 통일(`WithPagination` 등 임의 접미어 금지). 실제로 다른 도메인 대상을 담는 응답은 그 대상 도메인명을 따른다(예: `member/response/OrderListItemResponse`). 상세는 루트 CLAUDE.md 참고.
- **DTO 조립은 `new` 직접 호출 지양** — 컨트롤러에서 command/condition/response를 `new`로 조립하지 않고, 대상 record 자신의 정적 팩토리 `of(...)`/`from(...)`로 위임한다. **Request record가 `toCommand(...)`를 소유**하고, 컨트롤러는 `XxxCommand command = request.toCommand(...);`로 조립해 UseCase에 넘긴다(완전 매핑 — 매핑은 인바운드 어댑터의 책임). 상세는 루트 CLAUDE.md 참고.
- **`record`는 별도 파일로 분리** — 서비스/컨트롤러 본문에 응답 record를 중첩 선언하지 않고 feature 폴더의 `response/`에 `public record`로 둔다(reference: `notice/response/NoticeListItemResponse`). 단, `content`/`page`/`size`/`totalElements` 표준 페이징 응답은 도메인 폴더에 만들지 않고 `common/PaginationResponse<T>` 공용 제네릭을 재사용한다(reference: `notice`/`order`/`policy` 도메인의 페이징 조회 메서드). 상세는 루트 CLAUDE.md의 "페이징 응답 공용 제네릭 래퍼 규칙" 참고.
- **컨트롤러는 도메인별 application 서비스만 호출** — repository/JPA·QueryDSL에 직접 접근하지 않는다. 도메인당 **CQRS로 분리된 두 서비스**를 각각 주입한다(reference: `order/OrderCommandService`+`OrderQueryService`, `payment/PaymentCommandService`+`PaymentQueryService`).
  - `{도메인}CommandService`(`@Transactional`): domain write 포트·도메인 서비스만 주입. 생성/수정/삭제/상태전이를 수행하고 **식별자만 반환**한다.
  - `{도메인}QueryService`(`@Transactional(readOnly = true)`): `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스만 주입(infra DAO 구현체는 알지 않는다). **조회만 담당하고 `*Result`를 그대로 반환한다** — Response 조립은 챕터 10으로 이 모듈의 Response record(`from(XxxResult)`)가 맡는다.
  - **등록(POST)은 생성된 `Long` id만 반환**한다 — `ResponseEntity<ApiResponse<Long>>`로 PK 하나만 반환하고, 커밋 이후 QueryService로 재조회해 상세 DTO를 조립하지 않는다(과거 이 모듈만 등록 8종이 재조회 형태였으나 전면 전환됨). 생성 응답 전용 래퍼 record를 만들지 않고, 행을 생성하고도 `ApiResponse<Void>`를 반환하지 않는다. 상세가 필요한 클라이언트는 그 id로 GET 상세를 호출한다. 업로드·인증/토큰 발급·토글·상태전이·POST-as-query는 적용 제외. 상세는 루트 CLAUDE.md 참고. 수정(PUT)·상태전이(PATCH) 응답에서 상세 DTO가 필요하면 종전대로 QueryService 재조회로 조립한다.
  - 인증·회원·파일·등급·추천처럼 애그리거트 CRUD가 아닌 흐름 지향 서비스는 CQRS 쌍이 아닌 단일 서비스로 남아 있다(`auth/AuthCommandService`, `member/MemberService`, `grade/GradeQueryService`, `referral/ReferralQueryService` — 전부 `application` 소유이며 챕터 02에서 인바운드 포트를 갖게 됐다. `file/FileService`는 `api-common-module` 소유).
- **도메인 enum은 컨트롤러/Request에 core 타입으로 노출하지 않는다** — HTTP 경계는 `String`(다중값 `List<String>`)으로 받고 `{도메인}CommandService`/`{도메인}QueryService`에서 domain enum의 `Enum.from(String)`으로 승격한다(ID를 `Long`으로 받아 `XxxId.of()`로 승격하는 것과 대칭). String 파라미터에는 `@Schema(allowableValues={...})`/`@Parameter(...)`로 Swagger 후보값을 명시하고, 변환 실패는 domain enum `from()`에서 `BusinessException(ErrorCode.XXX_TYPE_UNKNOWN)`으로 처리한다(reference: `event/EventQueryService`·`EventStatus`, `shop/ShopQueryService`·`FoodType`/`Amenity`). 상세는 루트 CLAUDE.md 참고.
- **외부 API 호출은 external-api 모듈 어댑터로 위임** — OAuth, 결제, 파일 업로드, 크롤링 등.
- **Spring Security + JWT 인증 흐름**: JwtAuthenticationFilter → JwtTokenProvider.validateToken() → CustomUserDetailsService → SecurityContext 설정.
- **GlobalExceptionHandler로 모든 예외 통합 처리** — BusinessException이 담은 `ErrorCodeSpec`의 httpStatusCode로 HTTP 상태 결정. 인증 실패는 `ErrorCode.AUTH_*`(401)를 담은 BusinessException으로 던진다(전용 예외 타입을 새로 만들지 않는다).
- **Rate Limiting은 `@RateLimit(keyType=IP|FIELD, limit=N, windowSeconds=...)`로 메서드 레벨 선언** — 이 모듈에는 `ratelimit/` 패키지가 없다. 애노테이션·aspect·예외는 `api-common-module`(`com.tastyhouse.apicommon.ratelimit`), Redis 카운터는 `infrastructure:redis`가 소유한다(챕터 02). 부트스트랩이 `ApiCommonRateLimitConfig`를 `@Import` 해야 aspect가 등록된다 — 빠지면 `@RateLimit`이 조용히 무시된다.
- **API 로깅은 ApiLoggingFilter + ApiLoggingAspect로 자동 수행** — 민감정보는 SensitiveFieldMasker로 마스킹.

### Testing Requirements
- **@WebMvcTest** — 컨트롤러 단위 테스트 (mockito로 service 모킹).
- **@SpringBootTest + spring-security-test** — 통합 테스트 (JWT 토큰 생성, 보안 필터 검증).
- MockMvc로 API 엔드포인트 검증, WithMockUser 또는 커스텀 인증 헤더로 JWT 시뮬레이션.
- 예외 처리 테스트는 GlobalExceptionHandler 동작 확인.
- **레이어 경계는 `architecture/LayerRulesTest`(ArchUnit)** — CQRS 서비스의 web 플럼빙 의존 금지, 컨트롤러의 Repository·QueryDao 의존 금지, CommandService의 QueryDao 의존 금지, `com.querydsl..` 금지, `..infrastructure..persistence..` 금지, 그리고 챕터 05 승격 규칙 `webAdaptersShouldNotDependOnApplicationServices`(`..adapter.in.web..` → `..application.service..` 금지)·`portInShouldBeFreeOfWebDomainAndInfrastructure`. `allowEmptyShould(true)`를 쓰지 않아 대상 0건이면 실패로 드러난다. 챕터 04의 임시 장치 `shouldNotDependOnInfrastructureQuery`는 챕터 05에서 제거됐다.

### Common Patterns
- **Controller + Request/Response DTO**: `@RestController @RequestMapping("/api/{domain}")` → `Method(@Valid {Domain}Request) → ResponseEntity<ApiResponse<{Domain}Response>>`.
- **성공 응답은 `ApiResponse`, 에러 응답은 `ProblemDetail`**: `ApiResponse`는 `success(data)`·`success(data, page, size, totalElements)` 두 정적 팩토리만 갖는 **성공 전용** 타입이다(`error(...)`는 없다). 에러는 GlobalExceptionHandler가 RFC7807 `ProblemDetail`로 응답하며, `errorCode` property에 ErrorCode.code(예: `DUPLICATE_RESERVATION`)가 실려 프론트 분기에 사용된다.
- **페이징**: `common/PageRequest`(size/page) → `{Ctx}QueryPort`(`com.tastyhouse.application..port.out`)가 `PageResult<T>`(domain-module `shared/page`) 반환 → 서비스가 `common/PaginationResponse.from(pageResult)`로 변환.
- **@CurrentUser** 커스텀 애노테이션으로 인증된 사용자 주입 — SecurityContextHolder 간접화.
- **CQS**: 트랜잭션 경계를 이 패키지가 소유한다 — `{도메인}CommandService`는 `@Transactional`, `{도메인}QueryService`는 `@Transactional(readOnly = true)`. domain-module의 도메인 서비스는 POJO라 `@Transactional`을 갖지 않는다.

## Dependencies

### Internal
- `domain-module` — 도메인 모델·VO·write 포트·도메인 서비스, 도메인 예외 (BusinessException, ErrorCode), 페이징 계약 (PageQuery/PageResult).
- `infrastructure-module` — DAO 구현체가 뜨는 빈 스캔 대상(`com.tastyhouse.infrastructure..` 소스 import는 ArchUnit이 전면 차단).
- `external-api` — OAuth 로그인, 결제, 이메일/SMS, 파일 업로드, 크롤링 어댑터.
- `security-module` — 공용 JWT 메커니즘·Redis 토큰 저장소·rate limit.
- `logging-module` — 요청/응답 로깅.

### External
- Spring Boot 3.2.4 (web, security, aop, validation, data-redis).
- JJWT 0.12.3 — JWT 발급/검증.
- springdoc-openapi 2.3.0 — Swagger UI (/swagger-ui.html).
- spring-security-test — 통합 테스트 지원.
- Redis (distributed rate limiting, JWT 블랙리스트 등).
- p6spy — SQL 로깅.

<!-- MANUAL: -->

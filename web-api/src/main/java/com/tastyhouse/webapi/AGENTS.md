<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-06-02 -->

# webapi (presentation layer)

## Purpose
사용자 대면 REST API 계층으로, HTTP 요청을 받아 core-module의 application 서비스를 호출하고 응답 DTO로 변환하여 반환한다. JWT 기반 인증, OAuth 소셜 로그인, 요청/응답 로깅, Rate Limiting, 중앙화된 예외 처리를 제공한다. Spring Security + JWT + Redis 기반의 stateless 아키텍처를 구현하며, 외부 API 연동은 external-api 모듈로 위임한다.

## Cross-cutting Packages
| Package | Description |
|---------|-------------|
| `auth/` | JWT 발급/검증, OAuth 로그인(Apple/Facebook/Kakao/Naver), 인증 정보 추출. `service/`는 AuthService(일반 로그인), PhoneLoginService, AuthPasswordResetService 포함. OAuth 각 제공자별 하위 디렉토리. request/response 구분 저장. |
| `config/` | Spring 설정 — SecurityConfig(필터체인, CORS), AsyncConfig(비동기), RedisConfig, WebClientConfig, OpenApiConfig(Swagger). jwt/ 하위에 JwtTokenProvider(발급/검증), JwtAuthenticationFilter, JwtProperties, TokenType. security/ 하위에 JwtAuthenticationEntryPoint, JwtAccessDeniedHandler, CustomUserDetailsService(UserDetailsService 구현), CustomUserDetails(UserDetails 래퍼). PublicPaths 에서 인증 불필요 경로 관리. |
| `exception/` | 중앙화된 예외 처리. GlobalExceptionHandler가 BusinessException (core-module), ExternalApiException (external-api), RateLimitException, Security 예외, 유효성 검사 예외를 처리하며 ApiResponse 형식으로 응답. UnauthorizedException (web-api 로컬)는 401 매핑. |
| `logging/` | AOP 기반 요청/응답 로깅. ApiLoggingFilter (서블릿 필터로 전체 요청 추적), ApiLoggingAspect (컨트롤러 진입 로깅), SensitiveFieldMasker (민감정보 마스킹). |
| `ratelimit/` | 분산 Rate Limiting. RateLimit 애노테이션, RateLimitAspect(AOP), RateLimiterService(Redis 기반), RateLimitKeyType(사용자/IP/전역). |
| `scheduler/` | 스케줄된 배치 작업. GradeScheduler (회원 등급 계산), RankScheduler (순위 집계), ProductScheduler (상품 동기화), SearchKeywordScheduler (인기 검색어 집계), 각각 Service 클래스 분리. |
| `security/` | Spring Security 보조 컴포넌트. CurrentUser (메서드 파라미터 주입 애노테이션). JwtAccessDeniedHandler, JwtAuthenticationEntryPoint, CustomUserDetailsService, CustomUserDetails는 config/security/ 에 위치. |
| `common/` | 공통 유틸. ApiResponse (모든 응답의 상위 래퍼, success/error/data), PageRequest (페이징 요청). 페이징 응답은 core-module의 PageResult<T>를 그대로 사용. |

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
| `payment/` | 결제 요청, 검증, 환불. pg/ 하위에 결제 게이트웨이별 구현 (Toss 등) |
| `policy/` | 약관/정책 조회 API |
| `product/` | 상품 조회, 검색, 필터링 API |
| `rank/` | 순위/랭킹 조회 API |
| `referral/` | 추천/레퍼럴 API |
| `reservation/` | 가게 시간 슬롯 예약 조회, 생성, 취소, 가용성 확인 API |
| `review/` | 리뷰 작성, 조회, 수정, 삭제 API |
| `search/` | 통합 검색 및 검색 로그 기록 API |
| `shop/` | 가게 조회, 상세 정보, 즐겨찾기, 사진/카테고리 조회 API (Place에서 Shop으로 재명명됨) |
| `verification/` | 사용자 인증(이메일/SMS 인증) 및 검증 API |
| `crawling/bbq/` | BBQ 외부 데이터 크롤링 (외부-api 연동 지원) |

## For AI Agents

### Working In This Directory
- **요청/응답 DTO는 feature 폴더 내 request/, response/ 서브폴더에 저장** — 도메인별 응집도 향상. 모든 Request/Response record는 타입 레벨 `@Schema(description = ...)`와 필드별 `@Schema(description = ..., example = ...)`를 갖춰 Swagger 문서를 완전하게 유지한다(컬렉션·중첩 record 필드는 example 생략 가능, description은 필수). 상세는 루트 CLAUDE.md 참고.
- **response/ 폴더의 모든 응답 record는 소속 도메인명 접두어로 시작한다** — 중첩·보조 요소 record도 예외 없음(`OptionResponse`가 아니라 `ProductOptionResponse`). 접미어는 `Response`로 통일(`WithPagination` 등 임의 접미어 금지). 실제로 다른 도메인 대상을 담는 응답은 그 대상 도메인명을 따른다(예: `member/response/OrderListItemResponse`). 상세는 루트 CLAUDE.md 참고.
- **DTO 조립은 `new` 직접 호출 지양** — 컨트롤러에서 command/condition/response를 `new`로 조립하지 않고, 대상 record 자신의 정적 팩토리 `of(...)`/`from(...)`로 위임한다. Request DTO에는 `toCommand()` 변환 메서드를 두지 않고, 컨트롤러가 Request를 원시 필드로 언패킹해 `Command.of(...)`를 호출한다(복잡한 중첩 요청은 `order/OrderApiController#toCreateOrderCommand`처럼 private 헬퍼 허용). 상세는 루트 CLAUDE.md 참고.
- **`record`는 별도 파일로 분리** — 서비스(Facade)/컨트롤러 본문에 응답·결과 record를 중첩 선언하지 않고 feature 폴더의 `response/`에 `public record`로 둔다(reference: `notice/response/NoticeListPageResult`, `policy/response/PolicyListPageResult`, `order/response/OrderListPageResult`). 상세는 루트 CLAUDE.md 참고.
- **컨트롤러는 도메인별 Facade(`{도메인}Service`)만 호출** — core-module application 서비스를 직접 호출하지 않고 Facade를 경유한다(reference: `order/OrderService`, `payment/PaymentService`). repository/JPA도 직접 접근하지 않음.
- **도메인 enum은 컨트롤러/Request에 core 타입으로 노출하지 않는다** — HTTP 경계는 `String`(다중값 `List<String>`)으로 받고 Facade에서 core enum의 `Enum.from(String)`으로 승격한다(ID를 `Long`으로 받아 `XxxId.of()`로 승격하는 것과 대칭). String 파라미터에는 `@Schema(allowableValues={...})`/`@Parameter(...)`로 Swagger 후보값을 명시하고, 변환 실패는 core enum `from()`에서 `BusinessException(ErrorCode.XXX_TYPE_UNKNOWN)`으로 처리한다(전파 대상: `event/EventService`·`EventStatus`, `shop/ShopService`·`FoodType`/`Amenity`). 상세는 루트 CLAUDE.md 참고.
- **외부 API 호출은 external-api 모듈 어댑터로 위임** — OAuth, 결제, 파일 업로드, 크롤링 등.
- **Spring Security + JWT 인증 흐름**: JwtAuthenticationFilter → JwtTokenProvider.validateToken() → CustomUserDetailsService → SecurityContext 설정.
- **GlobalExceptionHandler로 모든 예외 통합 처리** — BusinessException의 httpStatusCode로 HTTP 상태 결정, UnauthorizedException → 401.
- **Rate Limiting은 @RateLimit(keyType=USER/IP, limit=N, duration=...)로 메서드 레벨 선언** — RateLimitAspect가 인터셉트.
- **API 로깅은 ApiLoggingFilter + ApiLoggingAspect로 자동 수행** — 민감정보는 SensitiveFieldMasker로 마스킹.

### Testing Requirements
- **@WebMvcTest** — 컨트롤러 단위 테스트 (mockito로 service 모킹).
- **@SpringBootTest + spring-security-test** — 통합 테스트 (JWT 토큰 생성, 보안 필터 검증).
- MockMvc로 API 엔드포인트 검증, WithMockUser 또는 커스텀 인증 헤더로 JWT 시뮬레이션.
- 예외 처리 테스트는 GlobalExceptionHandler 동작 확인.

### Common Patterns
- **Controller + Request/Response DTO**: `@RestController @RequestMapping("/api/{domain}")` → `Method(@Valid {Domain}Request) → ResponseEntity<ApiResponse<{Domain}Response>>`.
- **ApiResponse 통일**: `ApiResponse.success(data)`, `ApiResponse.error(message)`, `ApiResponse.error(errorCode, message)` — success/errorCode/message/data 필드. 에러 시 `errorCode`에 ErrorCode.code(예: `DUPLICATE_RESERVATION`)가 실려 프론트 분기에 사용(BusinessException/ExternalApiException/RateLimit 핸들러).
- **페이징**: PageRequest (size/page) → PageResult<T> (content/totalElements/totalPages/page/size, core-module/shared/page).
- **@CurrentUser** 커스텀 애노테이션으로 인증된 사용자 주입 — SecurityContextHolder 간접화.
- **CQS**: 쓰기 메서드는 `@Transactional`, 읽기는 `@Transactional(readOnly = true)` (core-module service 단).

## Dependencies

### Internal
- `core-module` — application 서비스 (CommandService/QueryService), 도메인 예외 (BusinessException, ErrorCode).
- `external-api` — OAuth 로그인, 결제, 이메일/SMS, 파일 업로드, 크롤링 어댑터.

### External
- Spring Boot 3.2.4 (web, security, aop, validation, data-redis).
- JJWT 0.12.3 — JWT 발급/검증.
- springdoc-openapi 2.3.0 — Swagger UI (/swagger-ui.html).
- spring-security-test — 통합 테스트 지원.
- Redis (distributed rate limiting, JWT 블랙리스트 등).
- p6spy — SQL 로깅.
- Lombok.

<!-- MANUAL: -->

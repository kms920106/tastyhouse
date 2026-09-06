<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# webapi (presentation layer)

## Purpose
사용자 대면 REST API 계층이자, 이 앱의 **application 계층**이다. HTTP 요청을 받아 도메인당 CQRS 서비스(`{도메인}CommandService`/`{도메인}QueryService`)를 호출하고 응답 DTO로 변환하여 반환한다(과거 `core-module`의 `application/` 계층이 하던 역할이 전환으로 이 패키지로 내려왔다). JWT 기반 인증, OAuth 소셜 로그인, 요청/응답 로깅, Rate Limiting, 중앙화된 예외 처리를 제공한다. Spring Security + JWT + Redis 기반의 stateless 아키텍처를 구현하며, 외부 API 연동은 `infrastructure:{file-storage,oauth,payment,messaging}` 모듈로 위임한다(파일 저장은 스타터 `file-storage`가 코어 `external`과 벤더 `firebase`를 묶어 공급하므로 앱이 벤더를 알지 않는다).

## Cross-cutting Packages
| Package | Description |
|---------|-------------|
| `auth/` | JWT 발급/검증, OAuth 로그인(Apple/Facebook/Kakao/Naver), 인증 정보 추출. `service/`는 AuthService(일반 로그인), PhoneLoginService, AuthPasswordResetService 포함. OAuth 각 제공자별 하위 디렉토리. request/response 구분 저장. |
| `config/` | Spring 설정 — SecurityConfig(필터체인, CORS), AsyncConfig(비동기), RedisConfig, WebClientConfig, OpenApiConfig(Swagger). jwt/ 하위에 JwtTokenProvider(발급/검증), JwtAuthenticationFilter, JwtProperties, TokenType. security/ 하위에 JwtAuthenticationEntryPoint, JwtAccessDeniedHandler, CustomUserDetailsService(UserDetailsService 구현), CustomUserDetails(UserDetails 래퍼). PublicPaths 에서 인증 불필요 경로 관리. |
| `exception/` | 중앙화된 예외 처리. GlobalExceptionHandler가 BusinessException (domain `com.tastyhouse.domain.exception`, `ExternalApiException`도 이를 상속하므로 같은 핸들러가 처리), RateLimitException, Security 예외, 유효성 검사 예외를 처리하며 RFC7807 `ProblemDetail` + `errorCode` property로 응답(조립은 공용 `apicommon.exception.ProblemDetails`). 레거시 `UnauthorizedException`은 제거되고 `ErrorCode.AUTH_*`(401)로 흡수됨. |
| `logging/` | AOP 기반 요청/응답 로깅. ApiLoggingFilter (서블릿 필터로 전체 요청 추적), ApiLoggingAspect (컨트롤러 진입 로깅), SensitiveFieldMasker (민감정보 마스킹). |
| `security/` | Spring Security 보조 컴포넌트. CurrentUser (메서드 파라미터 주입 애노테이션). JwtAccessDeniedHandler, JwtAuthenticationEntryPoint, CustomUserDetailsService, CustomUserDetails는 config/security/ 에 위치. |
| `common/` | 공통 유틸. ApiResponse (모든 응답의 상위 래퍼, success/error/data), PageRequest (페이징 요청), PaginationResponse<T> (표준 4필드 페이징 응답 공용 제네릭 — 도메인별 `XxxPageResponse`를 만들지 않는다). 서비스가 `{Ctx}QueryPort`(`com.tastyhouse.application..port.out`)로부터 받는 `PageResult<T>`(domain `shared/page`)를 `PaginationResponse.from(...)`으로 변환한다. |

## Feature Packages
| Package | Purpose |
|---------|---------|
| `auth/` | 인증 엔드포인트 — 일반/소셜 로그인, 로그아웃, 토큰 갱신, 비밀번호 재설정 |
| `banner/` | 배너 관리 조회 API |
| `bug/` | 버그 리포트 제출 API |
| `event/` | 이벤트 조회 API |
| `faq/` | FAQ 조회 API |
| `file/` | 파일 업로드/다운로드 (스타터 `infrastructure:file-storage`로 위임 — SPI는 `infrastructure:external`, Firebase 구현은 `infrastructure:firebase`가 갖고 스타터가 둘을 묶는다) |
| `follow/` | 사용자 팔로우/언팔로우 관리 API |
| `grade/` | 회원 등급 조회 API |
| `member/` | 회원 프로필 조회, 회원 정보 수정, 탈퇴 등 회원 관리 API |
| `notice/` | 공지사항 조회 API |
| `order/` | 주문 생성, 조회, 취소, 배송 추적 API |
| `partnership/` | 파트너십/제휴 관리 API |
| `payment/` | 결제 생성·승인·취소·현장완료·환불. CQRS 분리(`PaymentCommandService`/`PaymentQueryService`). PG 연동은 `infrastructure:payment`의 `payment/toss` 어댑터가 domain 포트 `PgPaymentGateway`를 구현 |
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
- **외부 API 호출은 외부 연동 모듈 어댑터로 위임** — OAuth는 `infrastructure:oauth`, 결제는 `infrastructure:payment`, 메일/SMS는 `infrastructure:messaging`, 파일 업로드는 `infrastructure:file-storage`(스타터 — SPI `infrastructure:external` + 구현 `infrastructure:firebase`를 묶는다). 크롤링(`infrastructure:crawling`)은 batch-module 전용이라 이 모듈과 무관하다.
- **Spring Security + JWT 인증 흐름**: JwtAuthenticationFilter → JwtTokenProvider.validateToken() → CustomUserDetailsService → SecurityContext 설정.
- **GlobalExceptionHandler로 모든 예외 통합 처리** — BusinessException이 담은 `ErrorCodeSpec`의 httpStatusCode로 HTTP 상태 결정. 인증 실패는 `ErrorCode.AUTH_*`(401)를 담은 BusinessException으로 던진다(전용 예외 타입을 새로 만들지 않는다).
- **Rate Limiting은 `@RateLimit(keyType=IP|FIELD, limit=N, windowSeconds=...)`로 메서드 레벨 선언** — 이 모듈에는 `ratelimit/` 패키지가 없다. 애노테이션·aspect·예외는 `api-common-module`(`com.tastyhouse.apicommon.ratelimit`), Redis 카운터는 `infrastructure:redis`가 소유한다(챕터 02). **`ApiCommonRateLimitAutoConfiguration`이 `RateLimitCounterPort` 빈 존재(= `infrastructure:redis` 활성)를 조건으로 aspect를 자동 등록한다(챕터 02 개정)** — 부트스트랩은 이 설정을 `@Import`하지 않는다. 조건부 auto-configuration이 되면서 "배선을 빠뜨려 `@RateLimit`이 조용히 무시된다"는 실패 양식 자체가 사라졌다.
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
- **페이징**: `common/PageRequest`(size/page) → `{Ctx}QueryPort`(`com.tastyhouse.application..port.out`)가 `PageResult<T>`(domain `shared/page`) 반환 → 서비스가 `common/PaginationResponse.from(pageResult)`로 변환.
- **@CurrentUser** 커스텀 애노테이션으로 인증된 사용자 주입 — SecurityContextHolder 간접화.
- **CQS**: 트랜잭션 경계를 이 패키지가 소유한다 — `{도메인}CommandService`는 `@Transactional`, `{도메인}QueryService`는 `@Transactional(readOnly = true)`. domain의 도메인 서비스는 POJO라 `@Transactional`을 갖지 않는다.

## Dependencies

### Internal
- `domain` — 도메인 모델·VO·write 포트·도메인 서비스, 도메인 예외 (BusinessException, ErrorCode), 페이징 계약 (PageQuery/PageResult).
- `infrastructure-module` — DAO 구현체가 뜨는 빈 스캔 대상(`com.tastyhouse.infrastructure..` 소스 import는 ArchUnit이 전면 차단).
- `infrastructure:file-storage` — 파일 저장 스타터(챕터 03). 외부 연동 코어 `infrastructure:external`(`WebClientConfig`·`ExternalApiException`·파일 저장 SPI)과 벤더 구현 `infrastructure:firebase`를 묶어 전이로 공급한다 — **앱 `build.gradle`에는 이 한 줄만 있다.** 코어는 나머지 어댑터 `infrastructure:oauth`(소셜 로그인)·`infrastructure:payment`(결제)·`infrastructure:messaging`(이메일/SMS)에도 전이로 딸려 온다.
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

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 컨트롤러·Request·Response 작성 관례 (챕터 06 이관). 모듈 경계·의존 이야기는 web-api/AGENTS.md -->

### `Request` record는 domain-free — enum 승격은 서비스가 한다
**대상**:
- `product/adapter/in/web/request/ProductDetailSearchRequest.java` → 타입, `orderMethod`
- `shop/adapter/in/web/request/ScheduledOrderSlotSearchRequest.java` → compact constructor
- `shop/adapter/in/web/request/ShopDeliveryTipSearchRequest.java` → compact constructor

(경로 접두사 `backend/web-api/src/main/java/com/tastyhouse/webapi/`)

도메인 enum이 아니라 `String`으로 받고 승격은 서비스가 `OrderMethod.from(...)`으로 수행한다 — **HTTP 경계에 도메인 enum을 노출하면 상수 추가가 곧 공개 스키마 변경이 된다.** compact constructor는 대소문자·공백 정규화와 기본값 채우기까지만 하고(소비 Service가 방어 분기를 두지 않도록 경계에서 끝낸다) 문자열→enum 승격은 하지 않는다(Request record는 domain-free).

### 값이 하나여도 조회 파라미터는 record로 묶는다
**대상**:
- `product/adapter/in/web/request/ProductDetailSearchRequest.java` → 타입
- `shop/adapter/in/web/request/ScheduledOrderSlotSearchRequest.java` → 타입

파라미터가 하나여도 개별 `@RequestParam`을 나열하지 않고 record로 묶는다(GET 조회 파라미터 규칙).

### 기본값 정규화는 compact constructor 한 곳에서
**대상**:
- `product/adapter/in/web/request/ProductDetailSearchRequest.java` → compact constructor, `DEFAULT_ORDER_METHOD`
- `product/adapter/in/web/request/ProductBatchRequest.java` → `DEFAULT_ORDER_METHOD`
- `shop/adapter/in/web/request/ShopDeliveryTipSearchRequest.java` → 기본값 상수

주문유형 미지정 시 기본값은 `DELIVERY`다 — 손님 화면의 기본 진입 경로가 배달이기 때문. 파라미터 없이 호출하는 **기존 클라이언트가 있으므로**(이 엔드포인트는 원래 쿼리 파라미터가 없었다) 미지정을 오류로 만들 수 없고, 서비스마다 기본값을 채우면 판단이 흩어져 한쪽만 바뀔 수 있어 compact constructor에서 한 번만 정규화한다. 배달팁 팝업도 같은 이유로 미지정을 배달로 본다(팝업이 배달 주문 화면에서만 열린다).

### 조건부 필수 유효성은 HTTP 경계가 아니라 도메인이 소유한다
**대상**: `product/adapter/in/web/request/ProductFeedbackCreateRequest.java` → `content`

`content`의 "`ETC`면 필수" 조건은 Request record에 표현하지 않는다 — 유형에 따라 달라지는 조건부 필수는 Bean Validation의 필드 단위 어노테이션으로 정확히 쓸 수 없고, 무엇보다 그 판단은 도메인 불변식이라 `ProductFeedback`이 소유한다. **HTTP 경계에만 두면 다른 호출 경로가 우회한다.**

### 응답 record 접미 규칙 — `Response` vs `Item`
**대상**: `member/adapter/in/web/response/MemberDeliveryAddressItemResponse.java` → 타입

회원 도메인 응답 record는 최상위 응답에 `Response` 접미를 쓰므로(`MyProfileResponse` 등) 목록 항목도 `ItemResponse`로 둔다. web-api `shop/response/`의 `Item` 접미는 **다른 응답 안에 중첩되는 요소** record의 관행이고, 이 record는 응답 본문의 최상위 요소다.

### Response record는 값을 다시 계산하지 않는다 — 분기 판정은 서비스가 끝냈다
**대상**:
- `auth/adapter/in/web/response/AuthSocialLoginResponse.java` → `from(...)`
- `auth/adapter/in/web/response/AuthSocialLinkResponse.java` → `from(...)`
- `shop/adapter/in/web/response/ShopDeliveryTipResponse.java` → `from(ShopDeliveryTipViewResult)`
- `shop/adapter/in/web/response/ShopDeliveryTipDistanceItem.java` → `from(...)`
- `shop/adapter/in/web/response/ShopReviewsByRatingResponse.java` → `from(ReviewsByRatingResult)`

Response record는 읽기 계약(`*Result`)을 표현 계약으로 **옮겨 담기만 한다.** 확정/범위 모드 분기, 항목별 근거 문구, 거리별 노출 판정, 평점별 묶음과 각 묶음의 개수 제한은 모두 그 `*Result`를 만든 서비스·유스케이스가 끝냈다. 여기서 어떤 금액도 다시 계산하지 않고, 필드가 채워져 있음의 보장도 서비스 판정에 의존한다.

### 판별 유니온 응답의 `null` 통과는 계약이다
**대상**:
- `auth/adapter/in/web/response/AuthSocialLoginResponse.java` → `jwt`
- `auth/adapter/in/web/response/AuthSocialLinkResponse.java` → `jwt`, `socialProfile`

`jwt`는 `LOGIN`일 때만, 링크 응답의 `jwt`·`socialProfile`은 status에 따라 한쪽만 채워진다. **`null`을 그대로 통과시키고 `@JsonInclude(NON_NULL)`이 직렬화에서 생략하는 것이 계약**이다 — 기본값으로 메우거나 빈 객체로 바꾸면 프론트의 status 분기가 무의미해진다.

### 소셜 프로필은 플랫폼마다 제공 필드가 다르다 → **webapi/AGENTS.md** (web-api 전용 사실이므로 모듈 루트에 둔다)

**대상**: `auth/adapter/in/web/response/AuthSocialProfileResponse.java` → 타입

소셜 로그인 계정 연동 시 회원가입 폼 자동 매핑용 공통 프로필. **플랫폼마다 제공 가능한 필드가 달라 미제공 항목은 `null`로 반환된다** — 이 record의 필드가 `@NotNull`이 될 수 없는 이유다.

| 플랫폼 | 제공 필드 |
|---|---|
| 카카오 | `providerId`, `email`, `nickname`, `profileImageUrl`, `name`(동의 시), `phoneNumber`(동의 시), `gender`(동의 시) |
| 네이버 | `providerId`, `email`, `nickname`, `profileImageUrl`, `name`, `phoneNumber`, `gender`, `birthYear`, `birthMonth`, `birthDay` |
| 페이스북 | `providerId`, `email`, `name`, `profileImageUrl` |
| 애플 | `providerId`, `email` |

### 컨트롤러 분리 기준 — 화면 하나만 쓰는 지연 로딩 응답은 별도 컨트롤러
**대상**:
- `product/adapter/in/web/ProductNutritionApiController.java` → 타입
- `shop/adapter/in/web/ShopOrderNoticeApiController.java` → 타입
- `review/adapter/in/web/ReviewBlindConsentApiController.java` → 타입

대형 컨트롤러(`ProductApiController`·`ReviewApiController`)에 얹지 않고 별도 컨트롤러로 둔다. 근거는 두 가지다 — (1) **수명이 다르다**: 영양성분은 "영양성분 보기"를 눌렀을 때만 조회되는 지연 로딩 대상이고 그 화면 하나만이 응답을 쓴다. (2) **관심사·인가 규칙이 다르다**: 게시중단 동의/거부는 "게시중단 생애주기"라는 다른 관심사이고 인가 규칙(작성자 본인 + 게시중단 상태)이 리뷰 CRUD와 다르다.

### 인앱 알림함은 대상 회원을 토큰에서만 얻는다 (IDOR 방어)
**대상**: `notification/adapter/in/web/NotificationApiController.java` → 타입

전 엔드포인트가 로그인 필수다 — 모든 조회·전이가 "내 알림"으로 스코프되며, 대상 회원은 경로/바디가 아니라 **토큰에서만** 얻는다. **회원 식별자를 요청으로 받으면 그 자체가 IDOR 입구가 된다.**

### 메뉴 평가는 매장 리뷰와 독립된 축이다
**대상**: `menureview/adapter/in/web/MenuReviewApiController.java` → 타입

메뉴 평가 API(`/api/menu-reviews`)는 매장 리뷰(`/api/reviews`)와 **독립된 축**이다. 매장 리뷰를 쓰지 않아도 메뉴 평가만 남길 수 있고 그 반대도 가능하다 — 두 API 사이에 호출 순서 제약이 없다.

### 메뉴 정보 제보는 리뷰와 다른 리소스다
**대상**: `product/adapter/in/web/ProductFeedbackApiController.java` → 타입

리뷰는 "음식이 어땠는지"이고 제보는 "등록된 정보가 틀렸다"는 신고다. **별점에 반영되지 않으며** 점주에게는 건별이 아니라 주간 집계로 전달된다. **로그인이 필수다** — 익명 제보를 열면 경쟁 가게의 반복 허위 제보를 막을 수 없다. 다만 저장한 회원 식별자는 중복 방지에만 쓰고 **점주에게 노출하지 않는다** — 점주가 제보자를 식별하면 보복 우려가 있고, 제보의 목적은 정보 수정이지 손님 응대가 아니다.

### 알레르기 표시는 비로그인에게도 열린다
**대상**: `product/adapter/in/web/ProductNutritionApiController.java` → 타입

`/api/products/**`가 이미 `PublicPaths`에 등록돼 있어 이 경로도 비로그인으로 열린다 — **알레르기 표시는 로그인 여부와 무관하게 보여야 하는 안전 정보**이기 때문이다.

### `ProductSummaryResponse.from` 오버로드가 두 벌인 이유
**대상**: `product/adapter/in/web/response/ProductSummaryResponse.java` → `from(SearchProductItemResult)`, `from(ShopProductItemResult)`

통합검색 메뉴 탭과 가게 상세의 메뉴 목록이 같은 응답을 쓰지만 읽기 계약은 서로 다른 포트에서 온다(`SearchProductItemResult`는 가게명을, `ShopProductItemResult`는 카테고리 그룹핑용 `productCategoryId`와 품절 여부를 각각 더 갖는다). 이 응답이 쓰는 10개 필드는 두 계약에 모두 있다. **계약을 하나로 합치면 한쪽 화면이 쓰지 않는 필드를 그 쿼리가 함께 투영해야 하므로**(읽기 계약은 소비자가 실제 쓰는 필드만 담는다) 계약은 그대로 두고 복사 지점만 둘로 나눈다.

### 결제 컨트롤러의 CQRS 두 서비스 주입
**대상**: `payment/adapter/in/web/PaymentApiController.java` → 타입

command(생성·승인·취소·현장완료·환불)와 조회를 CQRS로 분리한 두 서비스를 각각 주입한다(공통 지침 패턴 2). **command 서비스는 식별자만 돌려주므로, 커밋 이후 조회 서비스로 재조회해 응답을 조립한다.**

### 서버가 표시 문구를 완성해 내려준다 — 프론트가 규칙을 복제하지 않게
**대상**:
- `shop/adapter/in/web/response/ShopDeliveryTipBreakdownItem.java` → `label`
- `shop/adapter/in/web/response/ScheduledOrderSlotItemResponse.java` → `label`, `dayLabel`
- `shop/adapter/in/web/response/ShopDeliveryTipRegionItem.java` → `regionName`

어느 설정이 이 금액을 만들었는지는 **계산 근거라 read model에 속한다** — 프론트가 구간·거리·시간대 규칙을 다시 구현하면 서버와 문구가 갈린다. 슬롯도 배달은 범위(`"오후 6:00~오후 6:30"`), 포장은 단일 시각(`"오후 6:00"`)이라 프론트가 조립하면 그 분기 규칙이 클라이언트마다 복제된다. `regionName`은 서버가 행정동 마스터를 조인해 완성한 전체 이름이다(프론트가 시도·시군구·동을 조립하지 않는다).

**경계**: 금액의 **표기 포맷**(천 단위 콤마·"원" 단위)은 프론트가 담당하므로 `amount`는 포맷하지 않은 정수다.

### 시각은 `"HH:mm"` 문자열 한 형태로 통일한다
**대상**:
- `shop/adapter/in/web/response/ShopBusinessHourItem.java` → 시각 필드
- `shop/adapter/in/web/response/ShopBreakTimeItem.java` → 시각 필드
- `shop/adapter/in/web/response/ShopDeliveryTipScheduleItem.java` → 시각 필드

영업시간·휴게시간·시간대별 배달팁 응답이 같은 형태여야 **프론트가 시간 파싱을 한 벌만 갖는다.** 한 응답만 형태를 바꾸지 않는다.

### 안내 문구 상수는 backend가 아니라 화면이 갖는다
**대상**: `product/adapter/in/web/response/ProductNutritionResponse.java` → `setMenu`

`setMenu`가 `true`면 화면이 "메뉴구성에 따라 영양성분이 다르므로 각각의 메뉴에 대한 영양성분을 확인해 주시기 바랍니다" 안내문구를 함께 노출한다. **그 문구는 backend가 아니라 화면 상수다** — 문구 자체는 표시 정책이라 배포 없이 바꿀 이유가 없고, API가 문장을 내려주면 화면 레이아웃과 결합된다.

### 손님용 알레르기는 코드가 아니라 한글 라벨 배열
**대상**: `product/adapter/in/web/response/ProductNutritionResponse.java` → `allergens`

`allergens`는 코드가 아니라 **한글 라벨 배열**(`["우유","땅콩"]`)이다 — 손님 화면이 코드→라벨 매핑표를 들고 있지 않게 하려는 것이다. 점주 응답은 반대로 체크박스 상태 복원을 위해 코드 배열을 받는다.

### 두 모드를 한 스키마로 표현하고 엔드포인트로 쪼개지 않는다
**대상**:
- `shop/adapter/in/web/response/ShopDeliveryTipResponse.java` → 타입
- `shop/adapter/in/web/request/ShopDeliveryTipSearchRequest.java` → 타입

배달 주소와 주문금액이 **모두** 주어지면 `deliveryTip`에 확정 금액, `breakdown`에 근거가 담긴다(확정 모드). 하나라도 없으면 `deliveryTip`이 `null`, `breakdown`이 빈 배열이고 프론트는 `minDeliveryTip`~`maxDeliveryTip` 범위를 보여준다(범위 모드). 요청 세 값이 모두 선택인 것도 같은 이유다 — 배달팁 팝업은 주소를 아직 고르지 않은 상태에서도 열려야 한다. **두 모드를 엔드포인트로 쪼개지 않은 것은, 팝업이 주소를 고르는 순간 같은 화면에서 범위 → 확정으로 넘어가기 때문이다.**

### 조회 불가 상태는 404가 아니라 `200 + available:false`
**대상**:
- `shop/adapter/in/web/response/ScheduledOrderSlotsResponse.java` → 타입, `available`
- `shop/adapter/in/web/ShopApiController.java` → `getScheduledOrderSlots(...)`

예약주문 미운영·미지원 주문방식·영업 종료·영업시간 미등록은 모두 오류가 아니라 **"지금은 예약할 수 없다"는 정상적인 조회 결과**이므로 404가 아니라 `200 + available:false`로 응답한다(배달팁 통합 조회의 선례를 따른다). 프론트는 같은 분기 하나로 안내 문구를 띄운다. 시각 의존 응답이라 캐시하지 않는다.

### 상세 초기 렌더 비용을 0으로 유지한다 — 팝업 데이터는 별도 엔드포인트
**대상**: `shop/adapter/in/web/ShopApiController.java` → `getDeliveryTip(...)`

배달팁 표·지역 목록·시간대 목록은 팝업을 열 때만 필요하므로 가게 상세(`/v1/{id}`)에 싣지 않고 이 엔드포인트로 분리했다. **상세는 하한/상한 2필드만 갖는다.**

### 범위 표기는 목록·카드·상세와 같은 산출 규칙을 쓴다
**대상**: `shop/adapter/in/web/response/ShopDeliveryTipResponse.java` → `minDeliveryTip`, `maxDeliveryTip`

목록·카드·상세가 쓰는 값과 **같은 산출 규칙**(현재 시각·거리에 의존하지 않는 설정값 전체의 하한/상한)이므로, 같은 가게를 목록에서 보다가 팝업을 열어도 범위 표기가 달라지지 않는다.

### 슬롯 응답은 상수를 함께 내려 프론트 복제를 막는다
**대상**: `shop/adapter/in/web/response/ScheduledOrderSlotsResponse.java` → `leadTimeMinutes`, `slotUnitMinutes`, `rangeSlot`

안내 문구와 표시 형태를 위한 값이다("2시간 이후부터 예약 가능"). **슬롯이 없어도 내려가므로 프론트가 상수를 복제하지 않는다.**

### 배달 주소의 좌표는 필수, 행정동은 서버가 채운다
**대상**:
- `member/adapter/in/web/request/MemberDeliveryAddressCreateRequest.java` → 타입
- `member/adapter/in/web/request/MemberDeliveryAddressUpdateRequest.java` → 타입

좌표는 클라이언트가 주소 검색 API에서 받은 값을 그대로 보낸다. **좌표가 없으면 거리별 배달팁을 산출할 수 없어 할증이 0원이 되므로 필수다.** 행정동은 서버가 주소 문자열로 매칭해 채우므로 요청 필드에 없다. 수정 요청도 좌표가 동일하게 필수다 — **생성만 막고 수정을 열어두면 좌표 없는 주소가 뒷문으로 들어온다.**

### 기본 배송지 지정은 전용 엔드포인트가 담당한다
**대상**: `member/adapter/in/web/request/MemberDeliveryAddressUpdateRequest.java` → 타입

수정 요청에 `isDefault`가 없는 것은 기본 배송지 지정을 `PATCH /v1/me/delivery-addresses/{id}/default` 전용 엔드포인트가 담당하기 때문이다.

### `null` 컬렉션은 빈 배열로 정규화한다
**대상**: `shop/adapter/in/web/response/ShopEditorChoiceResponse.java` → `from(...)`

`products`가 `null`이면 빈 배열로 내린다 — **추천 상품이 없는 초이스도 목록에 나와야 하므로 응답 계약이 빈 배열**이고, 이는 값을 만들지 않는 순수 `null` 기본값이라 "Response는 계산하지 않는다"(B-6)와 충돌하지 않는다.

### 시큐리티 필터체인의 3단 인가 규칙
**대상**: `config/security/SecurityConfig.java` → `securityFilterChain(HttpSecurity)`

인가 규칙은 세 줄이고 순서가 의미를 갖는다.

1. 공개 경로 — `PublicPaths.PATTERNS`에서 **중앙 관리**한다(컨트롤러마다 흩어 두지 않는다)
2. `/api/auth/logout`은 공개 경로가 아니라 **인증 필요** — 임의 토큰을 블랙리스트에 등록하는 것을 막기 위함
3. 나머지 API는 인증 필요(`anyRequest().authenticated()`)

### Swagger 진입점
**대상**: `config/OpenApiConfig.java` → 타입

Swagger UI는 `http://localhost:8080/swagger-ui.html`, OpenAPI JSON은 `http://localhost:8080/v3/api-docs`. (포트 8080은 web-api 기본값.)

---

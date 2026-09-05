<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# admin-api

## Purpose
관리자용 REST API 애플리케이션 (실행 가능한 Spring Boot bootJar). **챕터 03으로 application 계층이 `application` 모듈로 물리 분리되어, 이 모듈은 인바운드 어댑터(컨트롤러 + `request/`)와 config·security 정책·부트스트랩만 담당한다.** 컨트롤러는 `com.tastyhouse.application.<ctx>.port.in`의 UseCase 인터페이스만 주입한다.

`admin`(관리자 계정)·`auth`(로그인/JWT)·`banner`·`bug`(버그 제보)·`ceo`(점주 계정)·`coupon`·`event`·`faq`·`file`·`member`(회원 관리)·`notice`·`order`·`partnership`·`point`·`policy`·`product`·`rank`·`review`·`shop` 도메인 관리 API를 제공한다.

**관리자 측 application 계층은 `application`이 소유한다 (챕터 03 개정 — 과거 "이 모듈이 관리자 측 application 계층이다"의 번복).** 유스케이스가 관리자 전용이라는 사실은 그대로이고, 그것을 담는 **모듈만 바뀌었다**. web-api와 application 서비스를 공유하지 않는다는 성질도 그대로다(공유되는 것은 `domain-module`의 도메인 모델·write 포트·도메인 서비스와 `domain-module`이 소유하는 다중 앱 공유 `{Ctx}QueryPort` 계약이며, 이쪽 변경은 여전히 소비 모듈 전체를 함께 확인해야 한다).

> **챕터 03 — `application`의 자바 패키지가 평탄화됐다.** 과거 `com.tastyhouse.adminapplication`이던 것이 `com.tastyhouse.application` 하나로 4개 앱과 합쳐졌다. 패키지만으로는 이 모듈이 주입하는 UseCase가 admin 것인지 알 수 없으므로, 앱 소속은 마커 애노테이션(`@AdminApp`)이 표현한다 — 마커·유도 규칙 상세는 `application/AGENTS.md` 참고.

**도메인당 CQRS 분리 (서비스는 `application` 소유)**: 도메인마다 두 서비스를 둔다 — `{도메인}CommandService`(`@Transactional`, domain write 포트·도메인 서비스만 주입, 생성/수정/삭제/상태전이)와 `{도메인}QueryService`(`@Transactional(readOnly = true)`, `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스만 주입, 조회 전담 — **챕터 06 이후 Response 조립을 하지 않고 `*Result`를 반환한다**). **컨트롤러는 서비스 구현이 아니라 그 짝인 UseCase 인터페이스(`..port.in..`)를 주입한다** — 구체 서비스 클래스는 이 모듈의 컴파일 클래스패스에 보이지 않는다(`webAdaptersShouldNotDependOnApplicationServices`). 조회만 있는 도메인은 QueryService만(`ceo/CeoQueryService`), 명령만 있는 도메인은 CommandService만(`policy/PolicyCommandService`) 둔다. CommandService는 읽기 포트를, QueryService는 write 포트를 서로 주입하지 않고, command 결과 응답은 커밋 이후 컨트롤러가 QueryService로 재조회해 조립한다. 컨트롤러는 `com.tastyhouse.domain.*`를 import하지 않는다. 대형 도메인(`shop`)은 관심사별로 서비스를 더 쪼갠다(`ShopContentBoard*`/`ShopHygieneBadge*`/`ShopImageChange*`). **QueryDSL도 infrastructure도 절대 쓰지 않는다 (개정)** — `src/main`에 `com.querydsl.*` import·`@QueryProjection` 선언·`com.tastyhouse.infrastructure..` import가 **전면 0건**이며 `architecture/LayerRulesTest`(ArchUnit)가 이를 차단한다. 챕터 04의 임시 장치였던 `shouldNotDependOnInfrastructureQuery`와 이중 패키지 매칭은 챕터 05에서 제거됐다. reference 구현: `notice/NoticeCommandService`·`notice/NoticeQueryService`.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | web + springdoc 의존. **`application`**·`security-module`·`api-common-module`을 `implementation`으로 참조하고, `infrastructure:persistence`·`infrastructure:file-storage`·`infrastructure:redis`·`logging-module`은 **챕터 02로 `runtimeOnly`**로 내려갔다(자기 등록 auto-configuration이라 앱이 진입점 설정 클래스를 컴파일 타임에 볼 필요가 없다). 외부 연동은 **파일 저장 스타터 `infrastructure:file-storage` 하나뿐**이며(챕터 03 — 이전에는 코어 `external`과 구현 `firebase` 2줄이었다), OAuth·결제·메일/SMS 어댑터는 web-api 전용이라 이 모듈에 오지 않는다. QueryDSL 의존은 없다 |
| `src/main/resources/` | 관리자 앱 환경 설정 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/adminapi/` | 관리자 인바운드 어댑터 루트 — `config/`(JWT·Security), 각 도메인 폴더는 **`<ctx>/adapter/in/web/`(컨트롤러) + `request/` + `response/`**. **`response/`는 챕터 06으로 이 모듈이 소유한다**(85개) — 서비스·`port/in/`은 `application`이 소유한다. **공용 플럼빙(`ApiResponse`·`PageRequest`·`PaginationResponse`·`FileService`·`GlobalExceptionHandler`)은 이 모듈이 아니라 `api-common-module`(`com.tastyhouse.apicommon`) 소유**이며, `AdminApiApplication`이 그 패키지를 스캔한다 |
| `src/test/` | 관리자 API 테스트 |

## For AI Agents

### Working In This Directory
- **presentation(인바운드 어댑터) 레이어만 담당한다 (챕터 03 개정)**: 컨트롤러는 도메인을 직접 호출하지 않고 `application`의 UseCase 포트를 통해서만 호출한다. JPA Repository·`EntityManager`를 직접 주입하지 않는다.
  - **이 모듈에 `@Service` 빈을 두지 않는다**: application 계층은 `application`이 소유하며, `architecture/LayerRulesTest#apiModuleMustNotContainApplicationLayer`가 이를 강제한다. `@RestController`는 `..adapter.in.web..`에만 둔다(짝 규칙 `restControllersShouldResideInWebAdapterPackage`).
  - **컨텍스트 패키지는 3층 구조다 (챕터 06 개정)**: `<ctx>/adapter/in/web/`(컨트롤러) + `.../request/`(Request record) + `.../response/`(Response record). **`response/`는 챕터 06으로 `application`에서 이 모듈로 이동했다** — 유스케이스 계층에서 Swagger·HTTP 표현을 걷어내기 위함이다. `service/`·`port/in/`은 여전히 이 모듈에 없다(`application` 소유). ceo-api는 챕터 09, web-api는 챕터 10으로 같은 3층이 됐다 — **3개 앱 전부 완료다.**
  - 아래 CQRS 서술은 **`application` 모듈의 규칙**이며, 컨트롤러가 어느 포트를 주입할지 판단할 때 참고한다.
- 새 관리 기능 추가 시 도메인-폴더 + `request/`·`response/` 컨벤션을 따르고(둘 다 이 모듈), 서비스·포트는 `application`의 같은 컨텍스트 아래에 만든다. **Response는 `from(XxxResult)`로 읽기 계약을 통째로 받아 조립한다**(루트 `backend/CLAUDE.md`의 DTO 조립 규칙 — 챕터 06으로 개정됨).
- **import 순서 — presentation 내부 서브정렬**: 자사 import의 presentation 계층(`com.tastyhouse.adminapi.*`) 안에서는 공용 인프라(`common`·`config`)를 도메인 전용(`<도메인>.request`·`.response`)보다 **위**에 둔다. 각 서브그룹 내부는 알파벳순, 사이 빈 줄 없음. 상세·근거·예시는 루트 CLAUDE.md 참고.
- 도메인별 CQRS 서비스 쌍은 `application`에 두고, **컨트롤러는 그 짝인 UseCase 인터페이스(`..port.in..`)만 주입한다**(구체 서비스 클래스는 주입하지 않는다 — `webAdaptersShouldNotDependOnApplicationServices`). **이 모듈은 도메인 모델을 알지 않는다** — 컨트롤러뿐 아니라 `config..`·`security..` 등 어디서도 애그리거트·VO·리포지토리·도메인 서비스를 참조하지 않는다(`apiModuleShouldBeDomainModelFree`). **carve-out 3종**: 공용 에러 계약 `domain.exception..` · 페이징 계약 `domain.shared.page..`(챕터 06 — 컨트롤러가 `PaginationResponse.from(PageResult)`로 조립) · **도메인 enum의 읽기 accessor**(챕터 07 — `result.type().name()` 등 3종, 짝 규칙 `apiModuleShouldOnlyReadDomainEnums`가 그 범위를 강제). 승격(String → 도메인 enum)은 여전히 application 서비스가 담당한다.
- **도메인 호출은 `application`의 서비스가 전담하고, 매핑은 양방향 모두 이 모듈의 책임이다** — Request → Command(컨트롤러의 `request.toCommand(...)`)와 Result → Response(Response record의 `from(XxxResult)`) 둘 다 인바운드 어댑터가 한다(챕터 06). ceo-api는 챕터 09, web-api는 챕터 10으로 같아졌다 — **3개 앱 전부 양방향 매핑이 인바운드 어댑터의 책임이다.**
- **`scanBasePackages`에 domain 엔트리 없음**: `AdminApiApplication`의 `scanBasePackages`(및 `@ComponentScan basePackages`)는 `com.tastyhouse.adminapi`·`com.tastyhouse.infrastructure`·`com.tastyhouse.external`·`com.tastyhouse.security`·`com.tastyhouse.logging` 다섯 개이며, **`application`은 스캔 문자열이 아니라 `AdminApplicationConfig`를 `@Import`해 배선한다**(타입 세이프 조합이 이 저장소의 표준). `domain-module`에 `@Component`/`@Service`/`@Configuration`이 하나도 없어(도메인 서비스는 POJO, 빈 등록은 infra `<ctx>/config/<Ctx>DomainConfig`) domain 스캔 엔트리를 제거했다. 기존 `excludeFilters`(`com.tastyhouse.external.oauth.*` 제외 — admin은 소셜 로그인이 없다)는 그대로 유지된다.
- **도메인 enum도 컨트롤러/Request에 domain 타입으로 노출하지 않는다** — HTTP 경계는 `String`(다중값 `List<String>`)으로 받고, 서비스에서 domain enum의 `Enum.from(String)` 정적 팩토리로 승격한다(ID를 `Long`으로 받아 `XxxId.of()`로 승격하는 것과 대칭). String 파라미터에는 `@Schema(allowableValues={...})`/`@Parameter(...)`로 Swagger 후보값을 명시하고, 변환 실패는 domain enum `from()` 내부에서 `BusinessException(ErrorCode.XXX_TYPE_UNKNOWN)`으로 처리한다(reference: `banner/BannerCommandService`, `BannerType.from`). 상세는 루트 CLAUDE.md 참고.
- **불변식은 `domain-module`에 둔다** — 한 트랜잭션에서 2개 이상 애그리거트 타입을 load & save하는 오케스트레이션과 무상태 정책·검증기는 `<ctx>/service/` POJO로 내리고, `application`의 CommandService는 트랜잭션 경계·VO 승격·명시적 `save` 호출·응답 조립만 담당한다. admin 전용 로직이라도 불변식이면 domain에 두어, web/ceo가 같은 유스케이스를 실행할 때 우회되지 않게 한다.
- **명시적 save 필수** (`application` 규칙): 도메인 모델은 순수 POJO라 JPA 더티 체킹으로 자동 flush되지 않는다. CommandService에서 도메인을 변경한 뒤 반드시 `repository.save(domain)`을 호출한다(누락 시 변경이 조용히 유실된다).
- **`PageResult` → `PaginationResponse<T>` 변환은 컨트롤러가 한다 (챕터 06 개정)**: application이 `PageResult<XxxResult>`를 반환하고 컨트롤러가 `PaginationResponse.from(pageResult.map(XxxResponse::from))`으로 감싼다. 그래서 이 모듈은 `domain.shared.page..`를 정당하게 참조한다(위 carve-out). 과거에는 QueryService가 `PaginationResponse<T>`를 조립해 반환했으나, 그것이 유스케이스 계층의 HTTP 표현 의존이라 06이 걷어냈다. 도메인별 `XxxPageResponse` 래퍼는 만들지 않는다 — 상세는 루트 CLAUDE.md의 "페이징 응답 공용 제네릭 래퍼 규칙" 참고.
- **DTO 조립 시 `new` 직접 호출 지양**: Service에서도 command/condition/response를 `new`로 조립하지 않는다. 이 Service는 Request 타입이 아니라 개별 원시 파라미터(예: `String title, String content, boolean visible`)를 받고, 내부에서 대상 record의 정적 팩토리 `of(...)`/`from(...)`로 위임한다(reference: `NoticeCommandService#createNotice`, `NoticeQueryService#getNotices` → `NoticeSearchCondition.of(...)`. **`PaginationResponse.from(...)`은 챕터 06으로 컨트롤러로 옮겨졌다**). **Request record가 `toCommand(...)`를 소유**하고 컨트롤러가 그 결과를 지역 변수로 추출해 UseCase에 넘긴다(완전 매핑 — 매핑은 인바운드 어댑터의 책임). 상세는 루트 CLAUDE.md 참고.
- **command/DTO와 식별자(`XxxId.of(id)`)는 지역 변수로 추출 후 넘긴다(command 서비스 호출 인자 인라인 조립 지양)**: `Xxx.of(...)` 팩토리 결과를 core command 서비스 호출 인자에 인라인으로 바로 넘기지 않고, 먼저 `Xxx command = Xxx.of(...);`로 추출한 뒤 전달한다. **command 서비스에 넘기는 식별자 승격 `XxxId.of(id)`도 지역 변수로 추출한다** — command와 함께 넘길 때(`XxxId xxxId = XxxId.of(id); service.method(xxxId, command);`)든 식별자만 단독으로 넘길 때(`delete`·상태전이)든 인자 자리에 인라인하지 않는다(이전 "짧은 식별자 승격은 인자 자리 허용" 예외 폐지). 조립과 호출을 문장 단위로 분리해 가독성·디버깅·도메인 간 형태 일관성을 높이기 위함이다. 예외(인라인 유지): 이미 지역 변수인 `SearchCondition`, **query(조회) 서비스 호출**에 넘기는 `XxxId.of(id)`, **`Command.of(XxxId.of(id), ...)`처럼 command 팩토리 내부에 중첩된 `Id.of`**(이때는 command만 추출), 응답 변환 `XxxResponse.from(...)`. 대체된 인라인 한 줄을 `//` 주석으로 남기지 않는다. CQRS 전환으로 core command 서비스 호출과 command DTO 자체는 사라졌지만, CommandService가 HTTP 경계의 `Long`을 domain VO로 승격하는 지점에서 이 관례가 유지된다(reference: `NoticeCommandService#updateNotice`·`#deleteNotice`, 동일 적용 `banner`·`coupon`·`policy`의 CommandService). 상세는 루트 CLAUDE.md 참고.
- **`record`는 별도 파일로 분리**: 도메인 application 서비스·컨트롤러 본문 안에 응답·결과 record를 중첩 선언하지 않고 도메인 폴더의 `response/`에 `public record`로 둔다(reference: `notice/response/NoticeListItemResponse`). 단, 표준 4필드 페이징 응답은 도메인 폴더에 만들지 않고 `common/PaginationResponse<T>`를 재사용한다. 상세는 루트 CLAUDE.md 참고.
- **GET 조회 파라미터는 개수와 무관하게(1개여도) `@ModelAttribute` Request record로 받는다**: 개별 `@RequestParam`을 나열하지 않고 `{도메인}SearchRequest`(`request/`, `public record`)로 묶어 `@Valid @ModelAttribute`로 받고, 페이징 `PageRequest`는 별도 인자로 병기한다. SearchRequest는 `toCondition()` 없는 순수 홀더로 두고 컨트롤러가 원시 필드로 언패킹해 Service에 넘기며, 이 Service는 개별 원시 파라미터를 받아 `{도메인}SearchCondition.of(...)`로 조립한다(`{도메인}Service` 시그니처 무변경). enum=`String`/`List<String>`, FK=`Long`, Swagger는 필드 `@Schema(allowableValues={...})`, 기본값은 compact constructor에서 정규화. 조회 파라미터가 없는(페이징만) GET·`@PathVariable`만 받는 단건 조회는 대상 아님. 기존 `@RequestParam` GET(`notice/getNotices` 등)은 수정 시 전환한다(reference: `NoticeSearchRequest`, 정착 선례 `common/PageRequest`). 상세는 루트 CLAUDE.md 참고.
- **`@PathVariable` 주 리소스는 `id`로 통일한다**: 컨트롤러가 `@RequestMapping`으로 이미 그 도메인에 스코프되므로 주 리소스 식별자는 단건 CRUD·중첩 하위 경로 모두 bare `id`로 쓰고(예: `/coupons/v1/{id}`, `/coupons/v1/{id}/issues`) 한 컨트롤러 안에서 `id`/`{도메인}Id` 혼재를 금지한다. 다른 애그리거트를 함께 참조하는 경우만 그 식별자를 `{도메인}Id`로 구분한다. 타입은 `Long` 유지(`@PathVariable Long id`) 후 Service에서 `XxxId.of(...)`로 승격한다(reference: `coupon/CouponApiController`). 상세는 루트 CLAUDE.md 참고.
- **중첩 경로의 부모 `@PathVariable`이 완전히 미사용이면 경로를 평탄화한다**: 하위 리소스 식별자가 전역 유니크 PK라 부모 없이 단독 식별이 가능하고, 핸들러·`{도메인}Service`·core 어디서도 부모 `id`를 쓰지 않는다면(소속 검증 로직 없음) `/v1/{id}/{하위리소스}/{하위id}` 경로에서 부모 세그먼트를 제거해 `/v1/{하위리소스}/{하위id}`로 바꾸고 미사용 `@PathVariable`을 제거한다(reference: `event/EventApiController#deleteWinner`). 부모 id를 소속 검증에 실제로 쓰는 다른 핸들러는 대상이 아니다. 상세는 루트 CLAUDE.md 참고.
- **Request/Response record는 `@Schema`로 완전히 문서화한다**: 타입 레벨에 `@Schema(description = ...)`, 모든 필드에 `@Schema(description = ..., example = ...)`를 붙인다(컬렉션·중첩 record 필드는 example 생략 가능하나 description은 필수). Request는 Bean Validation 어노테이션 다음 줄에 `@Schema`를 두고 필수 필드는 `requiredMode = Schema.RequiredMode.REQUIRED`로 표시하며, enum 후보 `String`/`List<String>` 필드는 기존 `allowableValues = {...}`에 `description`을 병기한다(reference: `notice/request/NoticeUpdateRequest`, `banner/response/BannerListItemResponse`). 상세는 루트 CLAUDE.md 참고.
- **Rate Limit은 전면 적용하지 않고 `auth` 로그인 엔드포인트에만 최소 적용한다 (결정 근거)**: admin-api는 인증된 소수 관리자만 접근하는 내부 관리용 API이므로 일반 사용자 트래픽을 겨냥한 전면적 rate limit(검색·조회 API 등)은 불필요하다고 판단했다 — web-api처럼 불특정 다수가 호출하는 공개/준공개 엔드포인트가 없고, 트래픽 규모도 본질적으로 다르다. 다만 `auth/AuthApiController#login`만은 예외로 두었다: 로그인은 인증 이전 단계라 "인증된 관리자만 접근"이라는 전제가 적용되지 않고, 무차별 대입(brute-force)으로 관리자 계정을 탈취당하면 서비스 전체에 영향을 미치는 고위험 엔드포인트이기 때문이다. 이 판단에 따라 `web-api`의 `ratelimit/` 패키지(`RateLimit`/`RateLimitAspect`/`RateLimiterService`(현 `RedisRateLimitCounter`)/`RateLimitKeyType`) 구조를 그대로 참고해 `adminapi.ratelimit` 패키지에 최소 버전을 두고, `AuthApiController#login`에만 `@RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:admin_login")`을 적용했다(web-api `login`과 동일한 제한값). `RateLimitException`은 `adminapi.exception` 패키지에 두고 `GlobalExceptionHandler`에서 429로 처리한다(web-api와 동일 패턴). ~~**web-api와 동일 코드가 두 모듈에 중복되는 것은 감수**했다~~ **(개정됨)** — 당시에는 공용 인프라 모듈이 없어 중복을 감수했으나, 이후 `security-module`이 rate limit(`com.tastyhouse.security.ratelimit`)을 통합했고 `api-common-module`이 `GlobalExceptionHandler`를 통합했다. 지금 이 모듈에는 `ratelimit/`·`exception/` 패키지가 없으며, **챕터 02 이후** `@RateLimit`·`RateLimitAspect`·`RateLimitException`과 429 변환(공용 `GlobalExceptionHandler`)은 모두 `api-common-module`, Redis 카운터만 `infrastructure:redis`가 담당한다. **챕터 02 이후** aspect 빈은 스캔이 아니라 `ApiCommonRateLimitAutoConfiguration`이 `RateLimitCounterPort` 빈 존재를 조건으로 자동 등록하며, 이 모듈은 그 설정을 `@Import`하지 않는다. **다만 `keyPrefix`(`rate_limit:admin_login`)는 Redis 카운터 키이므로 개명하지 않는다.** `admin-api/build.gradle`은 이미 `spring-boot-starter-aop`(및 `spring-boot-starter-data-redis`)를 갖고 있어 추가 의존성 도입 없이 구현했다. 다른 도메인(banner/coupon/notice 등)의 CRUD 엔드포인트는 이 결정에 따라 rate limit 대상이 아니다.

### Testing Requirements
- `@SpringBootTest` 기반 컨트롤러 검증.
- **레이어 경계는 `src/test/.../architecture/LayerRulesTest`(ArchUnit)가 강제**한다 — `applicationServicesShouldNotDependOnWebLayer`(클래스명 `*CommandService`/`*QueryService`로 대상을 잡아 `org.springframework.web.bind..`/`web.servlet..`/`org.springframework.http..`/`jakarta.servlet..` 차단. `MultipartFile`은 업로드 경계 타입이라 제외), `controllersShouldNotDependOnRepositories`, `controllersShouldNotDependOnQueryDaos`, `commandServicesShouldNotDependOnQueryDaos`, `shouldNotDependOnQuerydsl`, `shouldNotDependOnInfrastructurePersistence`, 그리고 챕터 05에서 패키지 기준으로 승격한 `webAdaptersShouldNotDependOnApplicationServices`·`portInShouldBeFreeOfWebDomainAndInfrastructure`. `allowEmptyShould(true)`를 쓰지 않으므로 대상 클래스가 0건이면 **공허 통과가 아니라 실패**로 드러난다(과거 `..application..` 패키지 매칭 규칙이 전환 후 대상 0건으로 공허 통과하던 문제를 이렇게 해소했다).

### Common Patterns
- 컨트롤러 → 이 모듈의 application 서비스(`{도메인}CommandService`/`{도메인}QueryService`) → domain write 포트·도메인 서비스 또는 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort`. 컨트롤러는 HTTP 매핑만, 서비스는 트랜잭션 경계·승격·위임·변환을 담당한다(과거 `{도메인}Service` → core application 서비스의 2-hop 구조는 전환으로 제거되어 1-hop이다).
- application 서비스는 더 이상 web-api와 공유되지 않는다. 대신 `domain-module`의 write 포트·도메인 서비스 시그니처나 `{Ctx}QueryPort`의 메서드를 바꿀 때는 web-api·ceo-api·batch-module의 소비 지점을 함께 확인한다.
- **JWT 인증 메커니즘은 `security-module`의 `com.tastyhouse.security.jwt`에 공유**된다. admin-api의 `config/jwt/JwtTokenProvider`는 그 공용 provider를 상속해 `adminId` 클레임·`CustomUserDetails` 재구성만 주입한다(검증 토큰 없음). 공용 필터는 `config/jwt/JwtConfig`가 admin 전용 블랙리스트 저장소(`admin:bl:`)로 빈 등록한다. 정책은 admin-api에 잔류: `config/security/SecurityConfig`·`PublicPaths`·`CustomUserDetails`(`JwtPrincipal` 구현)·`AdminUserDetailsService`, `config/jwt/TokenService`·`RedisRepositoryConfig`.
- **인가 체인은 `.anyRequest().hasAnyRole("ADMIN", "SUPER_ADMIN")`**로 관리자 역할을 강제한다(심층 방어). `@PreAuthorize`는 더 세분화된 제어(예: `AdminApiController`의 `SUPER_ADMIN`)에만 추가로 쓴다.
- **`jwt.secret`은 web-api와 반드시 달라야 한다**(admin=`JWT_SECRET_ADMIN`). 동일 시크릿이면 회원 토큰이 admin 인증을 통과하는 권한 상승이 발생한다 — 상세는 `security-module/AGENTS.md`. 시크릿 교체 시 기존 admin 세션은 전면 무효화되므로 배포 시 관리자 재로그인 안내가 필요하다.
- **등록(POST) API는 생성된 `Long` id만 반환**한다: `ResponseEntity<ApiResponse<Long>>`로 PK 하나만 반환하고, 생성 응답 전용 래퍼 record를 만들거나 생성 직후 QueryService로 재조회해 상세 DTO를 반환하지 않는다. 행을 생성하고도 `ApiResponse<Void>`를 반환하지 않는다. 파일 업로드·인증/토큰 발급·토글/상태전이·POST-as-query·배치집계(`RankApiController#aggregate`, point `earn`/`deduct`)는 리소스 등록이 아니므로 적용 제외. reference: `ShopApiController`(등록 13개 전부 `Long`)·`NoticeApiController#createNotice`. 상세는 루트 CLAUDE.md 참고.


## 설정 파일 (`src/main/resources/application.yml`)

서버 포트 `8090`, CORS 허용 오리진(`CORS_ALLOWED_ORIGINS`, 기본 `http://localhost:3010`), JWT 만료(access 1시간 / refresh 7일 / 로그인 상태 유지 30일), multipart 상한 10MB를 담고, 공유 모듈의 설정을 `spring.config.import`로 끌어온다.

- **Redis 연결 설정은 이 파일이 갖지 않는다** — 챕터 05 §5b에서 `infrastructure:redis` 모듈이 소유하게 됐고, 이 파일은 `classpath:application-redis.yml`을 import할 뿐이다. 그 설정만 담고 있던 `security-module`의 `application-security.yml`은 **파일째 이관되고 삭제됐다.** Redis 접속 정보를 바꿔야 하면 이 파일이 아니라 `infrastructure/redis/src/main/resources/application-redis.yml`을 본다.
- **`.env`를 `optional:file:.env[.properties]`와 `optional:file:backend/.env[.properties]` 두 경로로 선언한다.** `spring.config.import`의 `file:` 상대경로는 **JVM 작업 디렉터리(CWD) 기준으로 해석**되므로, 한 경로만 선언하면 실행 위치에 따라 `.env`가 조용히 로드되지 않는다. 두 줄을 함께 두어 **모노레포 루트에서 실행하는 경우와 `backend`에서 실행하는 경우를 모두 지원**한다. `optional:` 접두어라 없는 쪽은 건너뛴다. 그 밖의 디렉터리에서 `java -jar`를 실행하면 두 경로 모두 빗나가 DB 접속 정보 같은 필수 환경변수가 비므로, 실행 디렉터리 규칙은 루트 `CLAUDE.md`의 "실행 디렉터리(CWD) 주의"를 따른다.
- `jwt.secret`은 ``JWT_SECRET_ADMIN``를 읽는다(앱별로 반드시 달라야 하는 이유는 위 참고).
- **최초 SUPER_ADMIN 시드(`admin.seed.*`)** — 이 서비스에는 관리자 공개 가입이 없으므로 **첫 관리자는 부팅 시 주입**한다. `admin.seed.password`가 기본 센티넬 `__UNSET__`이면 **신규 시드를 거부**하므로, 운영·최초 기동 시 `ADMIN_SEED_PASSWORD` 환경변수가 필수다(비밀번호 없는 관리자 계정이 조용히 생기는 것을 막는 fail-fast). `ADMIN_SEED_USERNAME`(기본 `admin`)·`ADMIN_SEED_NAME`(기본 `최고관리자`)은 선택이다.

## Dependencies

### Internal
- `application` (implementation) — 컨텍스트 UseCase 인바운드 포트(컨트롤러가 주입) + `AdminApplicationConfig`
- `infrastructure:persistence` (**챕터 02로 `runtimeOnly`로 강등 — 과거 서술의 번복**): 소스 import는 0건이고, **auto-configuration 전환으로 부트스트랩의 컴파일 타임 참조 자체가 사라졌다.** 과거에는 `@Import(InfrastructureModuleConfig.class)`가 진입점 설정 클래스를 컴파일 타임에 참조해 `runtimeOnly`로 내리면 4개 모듈 전부 "package does not exist"로 깨졌으나, `InfrastructureModuleConfig` → `PersistenceModuleAutoConfiguration`으로 리네임되며 `@AutoConfiguration` + `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록하는 형태가 되어 `@Import` 자체가 사라졌다. 은닉은 여전히 의존 스코프가 아니라 ArchUnit(`LayerRulesTest`)이 담당하지만, 이제는 컴파일 타임 은닉도 `runtimeOnly`가 실제로 보장한다
- `infrastructure:file-storage` — 파일 저장 스타터(챕터 03). `infrastructure:external`(코어 — `WebClientConfig`·`ExternalApiException`·파일 저장 SPI)과 `infrastructure:firebase`(`FileStorageStrategy` 구현 — 파일 업로드)를 묶어 전이로 공급하므로 **앱은 두 모듈을 직접 선언하지 않는다**. **OAuth·결제·메시징 모듈은 의존하지 않는다** — 관리자 화면에는 소셜 로그인·PG 결제·메일/SMS 발송 유스케이스가 없다
- `logging-module`, `security-module`
- `infrastructure:redis` (**runtimeOnly**) — rate limit 카운터와 `StringRedisTemplate` 빈. `RedisModuleAutoConfiguration`이 자기 등록하며(챕터 02) 부트스트랩은 `@Import`하지 않는다
- `api-common-module` — `ApiResponse`·`PaginationResponse`·`PageRequest`·`FileService`·공용 `GlobalExceptionHandler`
- **`domain-module`은 선언하지 않는다** — 이 모듈 소스에 `com.tastyhouse.domain..` import가 0건이고(`apiModuleShouldBeDomainModelFree`가 강제), domain 타입이 다시 필요해져도 `api-common-module`이 `api project(':domain-module')`로 전이 노출하므로 재선언이 필요 없다. web/admin/ceo 3모듈이 모두 같은 상태다(web-api `GlobalExceptionHandler`가 쓰는 `domain.exception..`도 이 전이 경로로 해결된다).
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

**예외 — `spring-boot-starter-data-redis`는 직접 선언한다.** 이 앱이 소유한 `config/jwt/RedisRepositoryConfig`가 `StringRedisTemplate`을 **직접 참조**하기 때문이다(앱별 키 접두사를 주입한다). 챕터 02에서 `infrastructure:redis`를 `runtimeOnly`로 내리면서 그동안 전이로 받아 쓰던 이 라이브러리 의존이 드러났고, 그래서 명시 선언으로 메웠다.

**이것은 어댑터 모듈을 `implementation`으로 되돌리는 것과 다르다.** 앱이 보는 것은 `StringRedisTemplate`이라는 **라이브러리 타입**뿐이고, `infrastructure:redis`의 어댑터 클래스(`RedisRateLimitCounter` 등)는 여전히 컴파일 타임에 보이지 않는다 — 헥사고날 은닉은 그대로다. 두 판단을 섞어 "전이가 끊겼으니 모듈을 다시 `implementation`으로" 되돌리지 않는다.

<!-- MANUAL: -->

> AI 규칙(한국어 답변·빌드 미실행·체크리스트 질문), GIT 규칙(`NO_COMMIT_OR_ROLLBACK`·추천 커밋 메시지), 플랜 작성 규칙, 일반 네이밍 규칙 등 리포 전체 공통 규칙은 **리포지토리 루트의 `CLAUDE.md`** 를 참조합니다. 이 파일은 backend 고유 컨벤션만 다룹니다.


## 모듈 지도 (모듈 재편 프로그램 완료 — 챕터 01~06 + 챕터 03 security-core 분리)

**모듈은 18개이고, 경계는 "계층 × 앱" 2차원이다.** 어느 파일을 어디에 둘지 헷갈리면 여기서 시작한다(배치 기준의 근거는 아래 [모듈 경계 규칙](#모듈-경계-규칙-계층--앱-2차원--기술별-infrastructure)).

```
실행 앱 4 (bootJar)      web-api      admin-api      ceo-api      batch-module
   │  컨트롤러/@Scheduled 트리거 + request/ + config·security 정책 + 부트스트랩
   ↓
application 4            web-         admin-         ceo-         batch-
                         application  application    application  application
   │  <ctx>/port/in/(UseCase + Command) + <ctx>/service/(CQRS)
   │  ※ <ctx>/response/ 는 api 모듈로 이동 완료 — 3개 앱 전부(admin 챕터 06 · ceo 챕터 09 · web 챕터 10)
   ↓                                    ※ infrastructure를 컴파일 클래스패스에 두지 않는다
도메인                   domain-module               모델·VO·write 포트·도메인 서비스 (프레임워크-프리)
                                                     + 다중 앱 공유 읽기 계약(챕터 05)
   ※ 앱 전용 읽기 계약({Ctx}QueryPort · *Result · *SearchCondition)은 위 application 4개가 각자 소유한다
     (챕터 09로 application-common-module 해체 — 패키지는 com.tastyhouse.application..port.out 그대로)
   ↑ 구현
아웃바운드 어댑터        infrastructure:persistence  JPA 어댑터 + QueryPort 구현 DAO + listener
                         infrastructure:redis        Redis 연결·템플릿 + rate limiting
                         external-api                OAuth·PG·메일·SMS·파일·크롤링
공유                     security-core  security-module  api-common-module  logging-module
```

- **실행 단위는 여전히 4개다.** 재편으로 늘어난 것은 라이브러리 모듈뿐이라 **bootJar 산출물 이름·경로는 불변**이다(`{web-api,admin-api,ceo-api,batch-module}/build/libs/{모듈}-0.0.1-SNAPSHOT.jar`). 배포 스크립트는 영향받지 않는다.
- **자바 패키지는 모듈명과 다르다**: `infrastructure:persistence`·`infrastructure:redis` 둘 다 `com.tastyhouse.infrastructure..`를 쓴다(재편은 Gradle 좌표와 디렉터리만 바꿨다). application 모듈은 `com.tastyhouse.{web|admin|ceo|batch}application..`이다. **`security-core`와 `security-module`도 같은 선례를 따라 둘 다 `com.tastyhouse.security..`를 쓴다**(챕터 03 — split package. 이동 대상만 패키지를 유지한 채 모듈을 옮겼다).
- **어느 모듈의 AGENTS.md를 읽어야 하나**: 컨트롤러·인증 필터를 고치면 `{앱}-api/AGENTS.md`, 유스케이스·서비스를 고치면 `{앱}-application/AGENTS.md`, 쿼리·엔티티는 `infrastructure/persistence/AGENTS.md`, 불변식은 `domain-module/AGENTS.md`, JWT 토큰 발급/검증·Redis 토큰 저장소는 `security-core/AGENTS.md`, 서블릿 인증 필터·EntryPoint는 `security-module/AGENTS.md`.
- **챕터 03 — `security-core` 분리 (application의 서블릿 스택 오염 절단)**: `security-module`이 서블릿 결합 타입(JWT 인증 필터 `OncePerRequestFilter` 상속·`JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`, `starter-web` 의존)과 서블릿-프리 타입(`JwtTokenProvider`·Redis 토큰 저장소 6종)을 함께 갖고 있어, `{web,admin,ceo,batch}-application`이 `security-module`을 의존하면 application 계층의 컴파일 클래스패스가 서블릿 스택으로 오염됐다(ArchUnit `applicationMustBeServletFree`는 소스 import만 검사해 이 클래스패스 오염을 막지 못한다). 서블릿-프리 타입(`JwtTokenProvider`·`JwtPrincipal`·`JwtPrincipalFactory`·`JwtProperties`·`TokenType`, Redis 토큰 저장소 6종 — RefreshToken/Blacklist/소셜 임시토큰 4종)을 신설 모듈 `security-core`로 이동하고, `security-module`은 서블릿 결합 타입(`SecurityModuleConfig`·`JwtAuthenticationFilter`·`JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`)만 남긴 채 `api project(':security-core')`로 재노출한다. `{web,admin,ceo}-application`은 `security-module` 대신 `security-core`만 의존해 서블릿 스택을 컴파일 클래스패스에서 배제하고(batch-application은 원래 security 의존이 없어 대상 아님), `{admin,ceo}-application`은 `spring-boot-starter-security`를 `spring-security-core`로 축소했다. `{web,admin,ceo}-api`는 기존대로 `security-module`을 의존하며 `security-core`를 전이로 받는다. 자바 패키지(`com.tastyhouse.security..`)·Redis key prefix(`rt:`/`bl:`/`admin:rt:`/`admin:bl:` 등)·빈 배선(`SecurityModuleConfig`의 `@ComponentScan("com.tastyhouse.security")`가 패키지 불변 덕에 이동한 `@Repository` 빈을 그대로 스캔)은 전부 불변이다. API 계약(JWT 토큰 포맷·인증 플로우)도 변경 없음. 상세는 [모듈 경계 규칙](#모듈-경계-규칙-계층--앱-2차원--기술별-infrastructure) 아래 의존 그래프와 `security-core/AGENTS.md`·`security-module/AGENTS.md` 참고.

## 패키지 최상위 지도

| 최상위 패키지 | 소유 모듈 | 계층 |
|---|---|---|
| `com.tastyhouse.domain..` | `domain-module` | 도메인 |
| `com.tastyhouse.application..` | **5개 모듈이 나눠 소유** — `{web,admin,ceo,batch}-application`(각 앱 단독 계약) / `domain-module`(다중 앱 공유) | 읽기 계약(`port.out`) — 한 최상위 패키지를 5개 모듈이 나눠 갖는 **split package**다(챕터 09). 모듈명과 패키지명이 어긋나는 것은 `infrastructure:persistence`가 `com.tastyhouse.infrastructure..`를 쓰는 것과 같은 선례이며, 그 덕분에 계약이 어느 모듈로 가든 소비 측 import가 바뀌지 않는다. FQCN 중복은 `ReadContractSingleOwnerTest`가 검출한다. 판정은 [소유 규칙](#query-daoqueryportresult-dtosearchcondition-소유-규칙-개정--읽기-계약은-소비-앱이-소유-구현은-infrastructurepersistence) 참고 |
| `com.tastyhouse.webapplication..` | `web-application` | application |
| `com.tastyhouse.adminapplication..` | `admin-application` | application |
| `com.tastyhouse.ceoapplication..` | `ceo-application` | application |
| `com.tastyhouse.batchapplication..` | `batch-application` | application |
| `com.tastyhouse.webapi..` | `web-api` | 인바운드 어댑터 |
| `com.tastyhouse.adminapi..` | `admin-api` | 인바운드 어댑터 |
| `com.tastyhouse.ceoapi..` | `ceo-api` | 인바운드 어댑터 |
| `com.tastyhouse.batch..` | `batch-module` | 인바운드 어댑터(트리거) |
| `com.tastyhouse.infrastructure..` | `infrastructure:persistence` | 아웃바운드 어댑터(DB) |
| `com.tastyhouse.infrastructure.redis..` | `infrastructure:redis` | 아웃바운드 어댑터(Redis) |
| `com.tastyhouse.external..` | `external-api` | 아웃바운드 어댑터(외부) |
| `com.tastyhouse.security..` | **2개 모듈이 나눠 소유** — `security-core`(서블릿-프리: `JwtTokenProvider`·Redis 토큰 저장소) / `security-module`(서블릿 결합: 인증 필터·EntryPoint·AccessDeniedHandler) | 공유(보안) — 챕터 03으로 split package(모듈명과 패키지명이 어긋나는 것은 `infrastructure:persistence`가 `com.tastyhouse.infrastructure..`를 쓰는 것과 같은 선례) |
| `com.tastyhouse.apicommon..` | `api-common-module` | 공유(HTTP 플럼빙) |
| `com.tastyhouse.logging..` | `logging-module` | 공유(횡단) |

**`webapi`와 `webapplication`은 접두어가 겹치므로**(`com.tastyhouse.webap...`) import 정렬·grep 시 세그먼트 전체를 비교한다 — 아래 [import 순서](#코딩-스타일-import-순서)의 계층 판별도 이 패키지 기준이다.

## admin 전용 네이밍 규칙 (메서드·타입명에 admin-flavor `Admin` 접두·접미·중간어 금지)

**admin 전용 조회·명령 메서드도 `ForAdmin`/`Admin` 접미어·접두어 없이 순수 도메인 동작명만 씁니다.** 같은 도메인 안에서 admin 전용 메서드만 이런 접미·접두를 붙이면, 일반 메서드(`findOrders`, `findAllEvents`, `findAllNotices` 등)와 이름 짓는 방식이 갈려 일관성이 깨집니다. **이 원칙은 메서드명뿐 아니라 반환 DTO/Result/Condition 등 타입명에도 동일하게 적용합니다** — admin 전용 타입이라고 해서 타입명에 역할 마커 `Admin`을 붙이지 않습니다.

- **admin/일반 구분 방법**: 메서드명·타입명의 `Admin` 마커가 아니라 아래 기준으로 구분합니다.
  - **메서드**: 시그니처(파라미터 차이: `memberId` 유무·소유권 검증 유무 등)로 구분하고, **비-admin 형제와 이름이 충돌할 때만** `ById`처럼 의미 있는 한정어를 붙입니다(예: `findOrderDetail(memberId, orderId)` vs `findOrderDetailById(orderId)`).
  - **타입(Result/Condition)**: 비-admin 형제가 없으면 `Admin`을 뗀 순수명을 쓰고(예: `MemberListItemResult`, `BugReportListItemResult`, `FaqListItemResult`), **비-admin 형제와 타입명이 충돌할 때만** 관리 화면 용도를 나타내는 한정어 `Management`를 붙여 구별합니다(`{도메인}Management{용도}Result`, 예: `OrderManagementListItemResult` vs `OrderListItemResult`). `Management`는 "누가(관리자)"가 아니라 "무엇을 위한 것인가(관리 화면 목록/상세)"를 표현하므로 역할 기반 마커를 쓰지 않으면서도 admin 성격을 이름에 담을 수 있습니다.
- 이미 존재하던 `ForAdmin` 접미어(`findOrderDetailForAdmin`, `findCategoriesForAdmin`, `findAllForAdmin`, `findPageForAdmin`)는 각각 `findOrderDetailById`, `findAllCategories`, `findAllCategories`, `findFaqPage`로 리네이밍하여 정리했습니다. 이후 타입명에 남아 있던 `XxxAdminDto`/`XxxAdminListItemResult`류(`OrderAdminListItemResult`, `EventAdminListItemDto`, `EventAdminDetailDto`, `BannerAdminListItemDto`, `BannerAdminSearchCondition`, `MemberAdminListItemResult`, `BugReportAdminListItemDto`, `BugReportAdminSearchCondition`, `CouponAdminListItemDto`, `MemberCouponAdminItemDto`, `FaqCategoryAdminDto`)도 위 기준으로 전부 리네이밍했습니다. (이후 [결과 DTO 접미어 규칙](#결과-dto-접미어-규칙-result로-통일-dto-금지)에 따라 `Dto` 접미어였던 타입들도 전부 `Result`로 추가 리네이밍되었습니다.)
- **예외**: `admin` 도메인 자체의 타입(`Admin` 엔티티, `AdminId`, `AdminRepository`, `AdminCreateCommand`, `AdminCommandService`/`AdminQueryService`, `AdminJpaRepository` 등)은 이 규칙의 대상이 아닙니다. 여기서 `Admin`은 역할 마커가 아니라 "관리자 계정"이라는 애그리거트 본래 이름입니다.

reference 구현: `order` 도메인의 `OrderQueryService#findOrderDetailById`(비-admin `findOrderDetail`과 시그니처로 구분)·`OrderManagementListItemResult`(비-admin `OrderListItemResult`와 충돌 → `Management` 한정어로 구별), `faq` 도메인의 `FaqQueryService#findAllCategories`/`FaqRepository#findFaqPage`(반환 타입 `FaqCategoryManagementResult`/`FaqListItemResult` — admin 전용 `FaqCategoryManagementResult`는 web-api용 `FaqCategoryResult`와 충돌해 `Management` 한정어 적용), `event` 도메인의 `findAllEvents`/`EventManagementListItemResult`/`EventManagementDetailResult`(비-admin `EventListItemResult`/`EventDetailResult`와 충돌 → `Management` 한정어), `member`/`bug`/`coupon` 도메인의 `MemberListItemResult`/`BugReportListItemResult`/`CouponListItemResult`(형제 없어 순수 strip).

## 결과 DTO 접미어 규칙 (`Result`로 통일, `Dto` 금지)

**조회 결과 record는 접미어를 `Result`로 통일합니다. `Dto` 접미어는 사용하지 않습니다.** 과거 `*Result`(다수)와 `*Dto`(소수)가 도메인마다, 심지어 **같은 도메인 안에서도**(예: 과거 `bug`의 `BugReportResult` vs `BugReportListItemDto`, `faq`의 `FaqResult` vs `FaqDetailDto`) 혼재해 같은 목적(조회 결과 반환)의 타입이 파일마다 다른 방식으로 이름 지어져 검색·리뷰·패턴 일치가 어려웠습니다. 이미 확립된 다수파이자 "CQRS Query 결과"라는 의미가 더 분명한 `Result`로 전 도메인을 통일합니다.

- **적용 대상**: `com.tastyhouse.application.<ctx>.port.out` 이하의 조회 결과 record(JPA 엔티티에서 표현 목적으로 직접 투영한 반환 타입). 과거(챕터 03까지)는 QueryDSL이 `@QueryProjection`으로 직접 투영했으나, 읽기 경로 포트화(챕터 04) 이후 이 record는 QueryDSL을 모르는 모듈에 있으므로 `infrastructure:persistence`의 DAO가 `Projections.constructor(XxxResult.class, ...)`로 투영한다. DAO가 `from(...)` 정적 팩토리로 조합하는 비투영 record도 동일하게 `Result`로 접미합니다.
- **적용 제외**: `*Condition`(검색 조건 — 같은 `port.out` 패키지에 두지만 접미어는 `SearchCondition` 유지), web-api/admin-api/ceo-api의 `*Request`/`*Response`(HTTP 경계 DTO)는 이 규칙 대상이 아니며 기존 네이밍 규칙을 그대로 따릅니다.
- **폴더 위치 (개정됨 — 읽기 경로 포트화로 재개정)**: 결과 record는 [record 파일 분리 규칙](#record-파일-분리-규칙-중첩-record-선언-지양)에 따라 **`com.tastyhouse.application.<ctx>.port.out`** 에 독립 파일로 둡니다. 과거 core-module `application/dto/result/`에 있던 결과 DTO는 application 계층 해체로 `infrastructure:persistence`의 `<ctx>/query/`로 이관됐고(챕터 03까지), 읽기 경로 포트화(챕터 04)로 다시 이 신설 모듈로 이관됐습니다 — 상세는 아래 [query DAO·QueryPort·Result DTO·SearchCondition 소유 규칙](#query-daoqueryportresult-dtosearchcondition-소유-규칙-개정--읽기-계약은-소비-앱이-소유-구현은-infrastructurepersistence)을 참고합니다.
- **admin 충돌 시 처리**: admin 전용 결과 record가 비-admin 형제와 이름이 충돌하면 위 [admin 전용 네이밍 규칙](#admin-전용-네이밍-규칙-메서드타입명에-admin-flavor-admin-접두접미중간어-금지)과 동일하게 `Management` 한정어로 구별합니다(예: `faq` 도메인의 admin 전용 `FaqCategoryManagementResult` vs web-api용 `FaqCategoryResult` — 단순 `Dto`→`Result` 치환 시 이름이 충돌해 `Management`를 적용한 사례).

reference 구현: `com.tastyhouse.application.shop.port.out`(`BestShopItemResult`, `ShopBookmarkedItemResult` 등 — 과거 `*Dto`에서 전환), `com.tastyhouse.application.event.port.out`(`EventManagementListItemResult`), `com.tastyhouse.application.faq.port.out`(`FaqCategoryResult`/`FaqCategoryManagementResult`/`FaqDetailResult` — 폴더 이동과 admin 충돌 해결이 함께 발생한 사례로, admin/web Result가 지금은 같은 `faq.port.out` 패키지에 공존한다).

## Command/DTO 네이밍 순서 규칙 (`{도메인}{동작}` 형태)

command·result 등 도메인 DTO의 이름은 **`{도메인}` 접두어 + `{동작}` + 접미어** 순서로 짓습니다. 동작(Create/Update/Delete 등)을 접두어로 두는 `{동작}{도메인}` 형태(예: `CreateBannerCommand`)는 사용하지 않습니다. 도메인명을 앞에 두면 같은 애그리거트의 DTO들이 IDE·파일 탐색기·import 목록에서 이름순으로 인접하게 모여 응집도가 드러나고, 도메인 단위로 일괄 검색·정렬하기 쉽습니다.

- **command record**: `{도메인}Create Command` / `{도메인}Update Command` / `{도메인}Delete Command` → 예: `NoticeCreateCommand`, `NoticeUpdateCommand`, `BannerCreateCommand`, `BannerUpdateCommand` (공백은 표기상 구분일 뿐 실제 타입명은 붙여 씀).
- **condition record**: `{도메인}SearchCondition` → 예: `NoticeSearchCondition`.
- **response/result record**: `{도메인}{용도}Response` / `{도메인}{용도}Result` → 예: `NoticeListItemResponse`, `OrderDetailResponse`. (페이징 응답은 도메인별 래퍼 대신 공용 제네릭 `PaginationResponse<T>`를 씁니다 — 아래 [페이징 응답 공용 제네릭 래퍼 규칙](#페이징-응답-공용-제네릭-래퍼-규칙-paginationresponset) 참고.)
- 한 도메인 안에서 일부만 이 규칙을 따르고 나머지는 `{동작}{도메인}` 형태로 남기는 **혼재 상태를 금지**합니다(예: `BannerUpdateCommand`와 `CreateBannerCommand`가 공존하는 것). 신규 작성·기존 수정 모두 이 순서로 통일합니다.

reference 구현: `notice` 도메인 — `NoticeCreateCommand`, `NoticeUpdateCommand`, `NoticeSearchCondition`, `NoticeListItemResponse`. (과거 `CreateNoticeCommand`였다가 `NoticeCreateCommand`로 리네이밍하여 이 순서로 확정한 전례가 있습니다.)

## web-api/admin-api response record 도메인 접두어 규칙 (`{도메인}` 접두어 누락 금지)

**`{도메인}/response/` 폴더 안의 모든 응답 record는 예외 없이 그 폴더가 속한 도메인명 접두어로 시작합니다.** 폴더 경로 자체가 이미 도메인을 나타내더라도, 타입명만 보고도 소속 도메인을 알 수 있어야 IDE 전역 타입 검색·import 목록에서 같은 도메인의 응답 DTO들이 이름순으로 인접하게 모이고, 다른 도메인의 동명·유사명 타입과 혼동되지 않습니다. 위 [Command/DTO 네이밍 순서 규칙](#commanddto-네이밍-순서-규칙-도메인동작-형태)이 "접두어 순서"를 다룬다면, 이 규칙은 그보다 앞선 전제인 "접두어 자체가 반드시 있어야 한다"를 명시합니다.

- **접미어 통일**: 응답 record는 `Response`로 접미합니다(`Result`는 `PageResult` 등 기존 페이지 래퍼 관례를 그대로 따름). `WithPagination`처럼 `Response`/`Result` 접미어 없이 임의 명사로 끝내는 이름은 금지합니다.
- **예외 (페이징 공용 제네릭 래퍼)**: `content`/`page`/`size`/`totalElements` 4필드 표준 페이징 응답은 도메인 접두어를 붙인 `XxxPageResponse`를 도메인마다 새로 만들지 않고, `common/PaginationResponse.java`의 공용 제네릭 `PaginationResponse<T>` 하나를 재사용합니다. 상세는 아래 [페이징 응답 공용 제네릭 래퍼 규칙](#페이징-응답-공용-제네릭-래퍼-규칙-paginationresponset) 참고. (형태가 다른 변종 — 예: 리뷰 평점별 조회처럼 중첩 `response` 필드 + `totalElements`만 갖는 경우 — 는 이 예외 대상이 아니며 기존처럼 도메인 접두어 규칙을 따릅니다.)
- **중첩·보조 요소 record도 대상**: 다른 응답 record 안에 리스트·필드로 포함되는 하위 record(옵션·아이템 등 보조 개념)도 소속 도메인 접두어를 생략하지 않습니다(예: 상품 옵션 응답은 `OptionResponse`가 아니라 `ProductOptionResponse`).
- **수식어보다 도메인이 먼저**: `{도메인}{수식어}{용도}Response` 순서를 지킵니다. 수식어(예: `TodayDiscount`)를 도메인명보다 앞에 두지 않습니다(`TodayDiscountProductListItemResponse`(X) → `ProductTodayDiscountListItemResponse`(O)).
- **예외 (다른 애그리거트를 담는 응답)**: 그 record가 실제로 표현하는 대상이 폴더의 소속 도메인이 아니라 다른 도메인이면, 접두어는 "폴더가 속한 도메인"이 아니라 "담고 있는 대상 도메인"을 따릅니다(예: `member/response/OrderListItemResponse`는 회원 폴더 안에 있지만 담는 내용이 주문 목록이므로 `MemberOrderListItemResponse`로 바꾸지 않고 `OrderListItemResponse`를 유지). 판단 기준은 "이 record가 무엇을 담는가"이며, "어느 폴더에 있는가"가 아닙니다.
- **적용 시점**: 신규 작성 및 수정 시 이 규칙을 따르며, 기존에 접두어가 없던 파일들은 해당 파일을 다음에 수정할 때 함께 리네이밍합니다. 이 규칙만을 위한 전 도메인 일괄 재작성은 하지 않습니다.

reference 구현: `product` 도메인 — `ProductOptionResponse`/`ProductOptionGroupResponse`(중첩 옵션 요소도 접두어 부여), `ProductTodayDiscountListItemResponse`(수식어보다 도메인 우선; 페이징 래퍼는 공용 `PaginationResponse<ProductTodayDiscountListItemResponse>`로 대체), `ProductReviewsByRatingPageResponse`(과거 `ProductReviewsByRatingWithPagination`에서 `Response` 계열 접미어로 통일 — 표준 4필드가 아닌 변종이라 공용 래퍼 예외 대상이 아님). 예외 사례: `member` 도메인의 `OrderListItemResponse`(담는 대상인 `order` 도메인명 유지).

## 페이징 응답 공용 제네릭 래퍼 규칙 (`PaginationResponse<T>`)

**`content`/`page`/`size`/`totalElements` 4필드로 구성된 표준 페이징 응답은 도메인마다 `XxxPageResponse` record를 새로 만들지 않고, 각 모듈 `common/PaginationResponse.java`의 공용 제네릭 `PaginationResponse<T>` 하나를 재사용합니다.** 과거 `admin-api`/`web-api`에 도메인별 `*PageResponse`(`EventPageResponse`, `CouponPageResponse`, `NoticePageResponse` 등) 약 18개가 존재했는데, 전부 `content/page/size/totalElements` 4필드 + `from(PageResult<T>)` 팩토리로 도메인마다 이름과 `@Schema` 문구만 다르고 구조는 동일했습니다. 게다가 이 래퍼들은 컨트롤러가 곧바로 `pageResponse.content()`/`page()`/`size()`/`totalElements()`로 해체해 `ApiResponse.success(...)`에 전달하므로 **JSON으로 직렬화되지 않고 어떤 컨트롤러 반환 시그니처에도 등장하지 않아 Swagger 스키마에도 나타나지 않는**, 서비스→컨트롤러 사이의 순수 중간 배관 타입이었습니다. 도메인 접두어를 강제하는 이유(IDE 타입 검색 시 소속 도메인 식별)가 애초에 적용되지 않는 타입이므로, 공용 `ApiResponse<T>`와 동일하게 `common/`에 제네릭 하나로 통합합니다.

- **타입 정의**: 각 모듈 `common/PaginationResponse.java`에 아래 형태로 둡니다.

```java
@Schema(description = "페이지 목록 응답")
public record PaginationResponse<T>(
    @Schema(description = "목록")
    List<T> content,

    @Schema(description = "페이지 번호(0부터 시작)", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 요소 수", example = "42")
    long totalElements
) {

    public static <T> PaginationResponse<T> from(PageResult<T> pageResult) {
        return new PaginationResponse<>(
            pageResult.content(),
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements()
        );
    }
}
```

- **서비스 사용법**: 페이징 조회 메서드는 `PaginationResponse<XxxListItemResponse>`를 반환하고, 마지막 줄에서 `PaginationResponse.from(pageResult)`로 조립합니다(`PageResult<T>` 위임 예외는 [DTO 조립 규칙](#dto-조립-규칙-new-직접-호출-지양)과 동일).
- **컨트롤러 사용법**: 지역 변수 타입만 `PaginationResponse<XxxListItemResponse>`로 두고, `ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements())` 4-인자 호출은 **그대로 유지**합니다. `ApiResponse`에 `PaginationResponse<T>`를 받는 오버로드를 추가하지 않습니다 — `ApiResponse.java`는 이 리팩터링과 무관하게 기존 형태를 유지합니다.
- **~~모듈별로 각각 둠~~ → `api-common-module`이 단독 소유 (개정됨)**: 과거에는 `ApiResponse<T>`/`PageRequest`가 모듈별로 중복 배치된 선례를 따라 `PaginationResponse<T>`도 각 모듈에 뒀으나, 세 모듈의 파일이 **package 선언 1줄만 다른 완전 복제**로 확인되어 신설 공유 모듈 `api-common-module`(`com.tastyhouse.apicommon.common`)이 단독 소유하도록 통합했습니다. 상세 기준은 아래 [api 모듈 공용 플럼빙 소유 규칙](#api-모듈-공용-플럼빙-소유-규칙-api-common-module)을 참고합니다.
- **적용 제외 (변종)**: `response`(중첩 객체) + `totalElements`만 갖는 형태(예: `ProductReviewsByRatingPageResponse`, `ShopReviewsByRatingPageResponse`)처럼 표준 4필드가 아닌 페이징 응답은 이 규칙 대상이 아니며 기존 도메인 접두어 규칙을 그대로 따릅니다.

reference 구현: `admin-api`/`web-api` 공통 — `common/PaginationResponse.java` + 이를 사용하는 전 도메인의 페이징 조회 메서드(`NoticeQueryService#getNotices`, `EventService#getEvents`, `OrderService#getOrderList` 등).

**적용 확인 (위치·네이밍 정렬, 개정됨)**: `ApiResponse`/`PageRequest`/`PaginationResponse`는 위 개정에 따라 `api-common-module`이 단독 소유합니다. 반면 `SecurityConfig`(인증 주체·필터체인·인가 정책이 모듈마다 다름)와 `PublicPaths.PATTERNS`(공개 경로 목록이 모듈마다 전혀 다름 — web은 18줄, admin/ceo는 3줄)는 **내용이 실제로 다른 정책 파일**이므로 계속 모듈별로 각각 둡니다. 위치·패키지 이름만 세 모듈에서 통일합니다(`config/security/`).

## api 모듈 공용 플럼빙 소유 규칙 (`api-common-module`)

**web-api/admin-api/ceo-api에서 내용이 완전히 동일한(= package 선언 줄만 다른) 비도메인 플럼빙 타입은 모듈별로 복제하지 않고 `api-common-module`이 단독 소유합니다.** 과거 관례는 "모듈별로 각각 둠"이었고 그 근거는 모듈 독립성이었으나, 전수 diff 결과 대상 파일들이 **정말로 한 글자도 다르지 않아** 그 관례가 보호하던 "소비자별 계약 차이"가 실재하지 않았습니다. 계약이 같은 것을 복제해 두면 한쪽만 고쳐지는 드리프트(실제로 세 모듈 `FileService`가 서로 다르게 갈라졌던 선례)가 생기므로, **동일함이 확인된 것만** 통합합니다.

- **모듈 형태**: `java-library` + `bootJar` 비활성 + `implementation` 의존. `security-module` 선례를 그대로 따릅니다(도메인 포트가 없는 공유 기술은 별도 공유 모듈이 소유).
- **통합 판정 기준 — "완전 동일"만**: `diff` 결과가 package 선언 1줄뿐인 것만 대상입니다. 한 줄이라도 **의미 있는** 차이가 있으면 그 차이가 소비자별 계약 차이인지 우연인지 먼저 판정하고, 계약 차이면 통합하지 않고 각 모듈에 남깁니다.
- **Swagger `@Schema` 문구 차이는 통합하지 않는 사유입니다**: `description`이 "가게" vs "내 가게"처럼 다르면 필드 구조가 같아도 **API 문서에 노출되는 값이 달라지므로** 통합 대상에서 제외합니다(그 문구가 소비자별로 의도된 설명이기 때문).

### 통합된 것 (`api-common-module` 소유)

| 타입 | 패키지 | 통합 전 |
|---|---|---|
| `ApiResponse<T>` | `apicommon.common` | 3모듈 복제(diff 1줄) |
| `PaginationResponse<T>` | `apicommon.common` | 3모듈 복제(diff 1줄) |
| `PageRequest` | `apicommon.common` | 3모듈 복제(diff 1줄) |
| `FileService` | `apicommon.file` | 3모듈 복제(diff 1줄) |
| `GlobalExceptionHandler` | `apicommon.exception` | admin↔ceo 복제(diff 1줄) — **web-api는 제외** |
| `ProblemDetails` | `apicommon.exception` | web-api↔공용 핸들러의 `problemDetail(...)` private 헬퍼 복제(바이트 동일) — **web-api도 이것만은 공유** |
| ~~`ShopBreakTimeResponse`·`ShopBusinessHourResponse`·`ShopHygieneBadgeResponse`~~ | ~~`apicommon.shop.response`~~ | **문서 드리프트 — 실제로는 통합되지 않았다**(챕터 09에서 확인). 세 record는 `api-common-module`에 존재한 적이 없고 admin·ceo가 각자 소유한다(admin은 챕터 06, ceo는 챕터 09로 각 api 모듈의 `adapter/in/web/response/`). 필드 구성이 같은 것은 중복이 아니라 **우연히 일치한 앱별 응답 계약**이며, 한쪽 화면 요구가 바뀌면 다른 쪽을 건드리지 않고 갈라져야 한다 |

**스캔 주의**: `GlobalExceptionHandler`(`@RestControllerAdvice`)와 `FileService`(`@Service`)는 빈이므로 어느 앱이 무엇을 스캔하는지가 곧 동작입니다. admin/ceo는 `com.tastyhouse.apicommon` 전체를, **web-api는 `com.tastyhouse.apicommon.file`만** 스캔합니다(자체 핸들러를 쓰므로 `exception` 패키지를 스캔하면 핸들러가 2개가 됩니다). 반면 `ProblemDetails`는 **`@Component`가 아닌 static 유틸**이라 스캔 범위와 무관하며, web-api가 `exception` 패키지를 스캔하지 않고도 import해서 쓸 수 있습니다(핸들러 빈은 여전히 1개).

### 복제를 유지하는 것 (허용 목록 — 통합 금지)

아래는 이름이 같아도 **내용이 실제로 달라** 각 모듈이 각자 소유합니다. 통합하려는 시도가 반복되지 않도록 사유를 명시합니다.

| 타입 | 사유 |
|---|---|
| `GlobalExceptionHandler` (web-api) | 검증 실패 메시지 형식이 `"필드명: 메시지"`를 `", "`로 join(공용은 메시지만 공백 join)하고 인증 예외(`BadCredentials`/`Disabled`/`Locked`)를 각각 분리해 개별 문구를 내려주는 등 → **응답 계약 차이**. 조립 로직인 `ProblemDetails`는 공용을 쓴다 |
| `TokenService` (admin/ceo) | 인증 주체(`Admin`/`Ceo`)·`ErrorCode`(`ADMIN_*`/`CEO_*`)·조회 서비스가 전부 다름. **JWT 시크릿도 분리**(`JWT_SECRET_ADMIN` 등)되어야 하므로 통합 시 권한 상승 위험 |
| `AuthService` (admin/ceo) | 위와 동일(인증 주체 차이) |
| `SecurityConfig` (3모듈) | 필터체인·인가 정책이 모듈마다 다름 |
| `PublicPaths` (3모듈) | 공개 경로 목록이 모듈마다 전혀 다름(web 18줄 vs admin/ceo 3줄). admin↔ceo가 우연히 같을 뿐 **정책 파일**이므로 통합하지 않음 |
| `ShopDetailResponse` (admin/ceo) | 필드 셋이 다름 — admin은 `createdAt`/`updatedAt`, ceo는 `trademarkImageUrl`/`hidden` → **계약 차이** |
| `ShopAmenityResponse`·`ShopListItemResponse` (admin/ceo) | 필드는 같으나 `@Schema(description)`이 "가게" vs "내 가게"로 달라 Swagger 문서가 바뀜 |
| `OrderDetailResponse` (web/admin) | `@Schema` example이 `APPROVED` vs `COMPLETED`로 다름(소비자별 대표값) |
| `ApiResponse`/`PageRequest`/`PaginationResponse`의 **모듈별 사본** | 없음 — 위 표대로 통합 완료 |

reference 구현: `api-common-module/` 전체와 이를 `implementation`으로 의존하는 3개 api 모듈, 스캔 범위를 좁힌 `WebApiApplication`.

## 예외·에러코드 소유 규칙 (`ErrorCodeSpec` 공통 계약 + `BusinessException` 단일 계층)

**HTTP 응답으로 나가는 모든 에러는 `ErrorCodeSpec`을 구현한 에러코드를 담은 `BusinessException`(또는 그 하위)으로 표현합니다.** 예외 타입을 새로 만들어 전역 핸들러에 전용 `@ExceptionHandler`를 추가하는 방식은 쓰지 않습니다. 과거 `ExternalApiErrorCode`가 `ErrorCode`와 구조(`httpStatusCode`/`code`/`defaultMessage`)가 완전히 같은데도 공통 추상이 없어, 각 api 모듈 핸들러가 예외 타입마다 같은 변환 코드를 복제해야 했고 **admin-api·ceo-api가 그 복제를 갖지 않아 502로 의도된 외부 연동 실패(SMS·메일 발송)가 `Exception` 폴백을 타고 500으로 응답되는 결함**이 실제로 있었습니다. 계약을 하나로 모으면 이런 누락이 구조적으로 불가능해집니다.

- **`ErrorCodeSpec`** (`domain-module`의 `com.tastyhouse.domain.exception`): `int getHttpStatusCode()`·`String getCode()`·`String getDefaultMessage()` 세 메서드만 갖는 순수 Java 인터페이스입니다. `httpStatusCode`가 `HttpStatus`가 아니라 `int`인 이유는 domain-module이 프레임워크-프리(production 의존 0)이기 때문이며, HTTP 상태 해석은 api 모듈 핸들러가 담당합니다.
- **에러코드 enum은 이 인터페이스를 구현합니다**: 비즈니스 에러 카탈로그 `ErrorCode`와 외부 연동 에러 `ExternalApiErrorCode`(external-api 소유)가 각각 구현합니다. **`BusinessException`의 필드·생성자·`getErrorCode()`는 `ErrorCodeSpec` 타입**이므로 호출부는 어느 계열이든 그대로 넘길 수 있습니다.
- **새 예외 타입 대신 상속을 씁니다**: 외부 연동처럼 별도 예외 타입이 필요하면 `ExternalApiException extends BusinessException`처럼 **`BusinessException`을 상속**해, 기존 `BusinessException` 핸들러가 그대로 처리하게 합니다. 전용 핸들러를 모듈마다 추가하지 않습니다.
- **`ErrorCode`는 도메인별로 쪼개지 않습니다**: 상수 230여 개의 단일 카탈로그이지만 이는 "비즈니스 에러 카탈로그"라는 단일 책임이며, 도메인별 enum으로 분리하면 이 enum을 import하는 167개 파일을 전면 수정해야 하는 데 비해 얻는 것이 파일 분할뿐입니다. 대신 아래 가드 테스트로 규약을 강제합니다.
- **`ErrorCodeConventionTest`가 카탈로그 규약을 지킵니다** (`domain-module` 순수 단위 테스트): `code` 유일성, 상수명↔`code` 일치, `*_NOT_FOUND` 이름은 404, `httpStatusCode`는 4xx·5xx, `defaultMessage` 비어 있지 않음. **기존 위반은 wire 계약이라 고치지 않고 봉인 목록(`EnumSet`)으로 통과시키며, 그 목록이 낡으면(고쳐졌으면) 별도 테스트가 실패해 알려줍니다.** 봉인 목록에 새 항목을 추가하지 말고 신규 상수는 규약을 지킵니다.
- **응답 `code` 문자열과 상태코드는 wire 계약입니다**: 프론트가 `code`로 분기하므로 기존 값을 바꾸지 않습니다. 신규 상수 추가는 additive라 안전합니다. 상수명과 `code`가 의도적으로 다른 경우(채널 어휘 통일로 상수명만 `SMS_`/`MAIL_`로 바꾼 6개)도 봉인 목록에 있습니다.
- **`ResourceNotFoundException`은 존치, `AccessDeniedException`은 폐지했습니다**: 전자는 catch 구분·의도 표현 가치가 있어 남깁니다. 후자는 Spring Security의 `org.springframework.security.access.AccessDeniedException`과 **이름이 같아** 두 타입의 처리 경로가 다른데도(도메인판은 ErrorCode의 상태·code로, Spring판은 전용 핸들러로) 이름만으로 구분되지 않아 import 한 줄 실수로 응답 계약이 조용히 달라지는 위험이 있었고, 서브클래스가 HTTP 상태에 아무 영향을 주지 않아(상태는 ErrorCode에서만 옴) 존재 의의도 없었습니다. 사용처 14곳을 `BusinessException`으로 전환하고 파일을 삭제했습니다. **권한 예외는 `BusinessException`에 403 `ErrorCode`(`ACCESS_DENIED` 또는 도메인별 `*_ACCESS_DENIED`)를 직접 넘깁니다.**
- **필터 단계와 advice 단계는 같은 `errorCode`를 냅니다**: 서블릿 필터(`JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`)는 advice를 타지 않아 `ProblemDetail`을 직접 직렬화하지만, `ErrorCode.AUTH_REQUIRED`(401)·`ErrorCode.ACCESS_DENIED`(403)의 `code`를 `setProperty("errorCode", ...)`로 담아 **클라이언트가 보는 계약을 advice 단계와 일치**시킵니다.
- **`ProblemDetails`(api-common-module)가 조립을 담당합니다**: `HttpStatus.resolve()` null 폴백과 `errorCode` property 부착 로직은 두 전역 핸들러에 바이트 동일하게 복제돼 있었으므로 static 유틸 하나로 통합했습니다. **응답 계약 차이(검증 실패 메시지 형식 등)는 메시지를 만드는 쪽에 있고 조립에는 없으므로** 핸들러는 계속 모듈별로 유지합니다.
- **batch-module은 이 체계를 쓰지 않습니다**: HTTP 경계가 없어 응답 계약이 존재하지 않으므로 `ErrorCode`를 강제하지 않고, raw `RuntimeException` 대신 `BatchJobException`(batch-module 로컬)을 던져 배치 실패를 식별합니다. 스케줄러가 이를 잡아 로그로 남기고 다음 주기에 재실행하는 잡 단위 격리는 정상 설계이므로 재던지도록 바꾸지 않습니다.

reference 구현: `domain-module`의 `exception/ErrorCodeSpec`·`ErrorCode`·`BusinessException`·`ResourceNotFoundException`과 가드 테스트 `ErrorCodeConventionTest`, `external-api`의 `ExternalApiErrorCode`·`ExternalApiException`(BusinessException 상속), `api-common-module`의 `ProblemDetails`·`GlobalExceptionHandler`(admin/ceo 공용), `web-api`의 `GlobalExceptionHandler`(계약 차이로 자체 유지), `security-module`의 `JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`, `batch-module`의 `exception/BatchJobException`.

## 소셜 로그인 SPI 규칙 (`external.oauth.spi`)

**소셜 로그인은 external-api가 소유한 SPI(`com.tastyhouse.external.oauth.spi`)를 통해서만 사용합니다.** web-api는 제공자별 패키지(`..oauth.kakao..` 등)의 wire DTO·클라이언트 구현을 직접 import하지 않습니다.

- **왜 domain-module이 아닌가**: domain-module의 출력 포트(`MailSender`·`FileStoragePort` 등)는 전부 **도메인 서비스가** 불변식을 만족시키려고 호출하는 것들입니다. 소셜 OAuth는 호출부가 전부 web-api(표현 계층)이고 도메인 서비스가 쓰는 곳이 없어, domain-module에 두면 "아무 도메인 서비스도 호출하지 않는 포트"가 됩니다. 도메인에 대응 개념이 없는 공유 기술은 별도 모듈이 소유한다는 [모듈 경계 규칙](#모듈-경계-규칙-계층--앱-2차원--기술별-infrastructure)(security-module의 Redis JWT·rate limit 선례)에 따라 **어댑터와 같은 모듈(external-api)이 자신의 SPI를 소유**합니다.
- **2단 계약으로 제공자별 흐름 차이를 흡수**: `exchange(SocialAuthorization) → SocialCredential`(카카오·네이버·애플의 토큰 교환, 페이스북의 app_id 검증)과 `fetchProfile(SocialCredential) → SocialProfile`(카카오·네이버·페이스북의 userinfo 조회, 애플의 id_token 검증·추출). `state`는 네이버만 쓰며 나머지는 `null`입니다.
- **`SocialProfile`은 전 필드 `String`**: `gender`도 도메인 enum이 아니라 상수명 문자열(`"MALE"`/`"FEMALE"`/`null`)을 담습니다 — 외부 연동 타입이 도메인 타입을 보유하면 external-api → domain-module 역방향 결합이 됩니다(과거 `KakaoUserInfoResponse`/`NaverUserInfoResponse`가 `MemberGender`를 import하던 문제). 도메인 enum 승격은 소비 측이 `MemberGender.from(String)`으로 수행합니다.
- **제공자별 관심사는 어댑터가 회수**: 페이스북 app_id 검증(`${facebook.app-id}` 설정값 포함)과 애플 id_token 검증 예외 번역(`APPLE_ID_TOKEN_INVALID`)은 과거 web-api 서비스에 있었으나 어댑터로 옮겼습니다. 응답 계약(`SOCIAL_OAUTH_FAILED`·`APPLE_ID_TOKEN_INVALID`)은 그대로입니다.
- **보존해야 하는 것**: 제공자별 Redis 임시토큰 저장소 4종과 **key prefix**(`kakao_temp:` 등), 제공자별 `*_TEMP_TOKEN_EXPIRED` `ErrorCode` 4종은 통합하지 않습니다 — prefix를 바꾸면 배포 시점에 진행 중인 임시토큰이 전부 무효화되고, ErrorCode는 프론트가 분기할 수 있는 wire 계약입니다.
- **빈 주입**: `SocialOAuthClient` 구현이 4개이므로 소비 측은 `@Qualifier("kakaoOAuthClient")`처럼 빈 이름을 명시합니다. **`@Qualifier`는 필드가 아니라 생성자 파라미터에 답니다** — 필드에만 달면 생성자 주입 경로에서 조용히 무시되고 주입이 빈 이름 우연 일치에만 의존하게 됩니다. (과거 Lombok을 쓰던 시절에는 루트 `lombok.config`의 `lombok.copyableAnnotations`가 필드의 `@Qualifier`를 생성자 파라미터로 복사해 줬으나, Lombok 제거로 생성자를 직접 작성하게 되면서 파라미터에 명시하는 방식으로 바뀌었습니다.)
- **ArchUnit으로 강제**: `web-api`의 `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`가 제공자별 패키지 의존을 금지합니다.

reference 구현: `external-api`의 `oauth/spi/`(`SocialOAuthClient`·`SocialProfile`·`SocialCredential`·`SocialAuthorization`·`SocialProvider`)와 이를 구현한 `KakaoOAuthClient`·`NaverOAuthClient`·`FacebookOAuthClient`·`AppleOAuthClient`, 소비 측 `web-api`의 소셜 로그인 서비스 4종.

## DTO 조립 규칙 (`new` 직접 호출 지양)

컨트롤러·Service 등 **호출부에서 DTO(command / condition / response record)를 `new`로 직접 조립하지 않습니다.** 대신 변환 책임을 해당 타입 또는 소스 타입으로 위임합니다. 이는 필드 추가 시 호출부 연쇄 수정을 막고, 조립 로직을 한 곳에 모아 가독성과 응집도를 높입니다.

- **Request → Command 변환은 인바운드 어댑터(컨트롤러)가 소유합니다 (개정 — 과거 `toCommand` 금지 규칙의 번복)**: Request record에 `toCommand(...)`를 두고, 컨트롤러가 `XxxCommand command = request.toCommand(경로변수, principal 식별자, ...);`로 application Command record를 조립합니다. 『만들면서 배우는 클린 아키텍처』 그림 8.3의 **완전(Full) 매핑 전략**을 채택한 결과이며, 계층 경계의 매핑은 그 경계를 지키는 어댑터의 책임이라는 것이 근거입니다. 경로 변수·인증 주체처럼 **요청 본문에 없는 값은 `toCommand`의 파라미터로 주입**합니다(`request.toCommand(id, userDetails.getMemberId())`).
  - **과거 규칙(폐기)**: "Request DTO는 `toCommand()` 같은 변환 메서드를 두지 않고, 컨트롤러가 개별 원시 필드로 언패킹해 Service에 전달한다"는 규칙은 **폐기**합니다. 그 방식은 필드가 늘어날수록 Service 시그니처가 그대로 길어져 **파라미터 15개짜리 메서드**를 낳았고, 같은 타입(Long/String) 인자가 줄줄이 늘어선 호출부에서 **순서를 착각해 값이 조용히 뒤바뀌는 사고**(컴파일은 통과)를 반복적으로 만들어냈습니다. 이름 있는 record 필드로 묶으면 그 사고 유형 자체가 사라집니다.
  - **Request·Command 모두 domain-free를 유지합니다**: Command 필드는 경계 타입(`Long`/`String`/`Integer`)을 그대로 씁니다. 도메인 타입 승격(`XxxId.of(...)`·`XxxEnum.from(...)`)은 **서비스 내부**에서 수행하므로, Request record에 `toCommand`가 생겨도 Request가 `com.tastyhouse.domain..`를 import하지 않습니다(기존 Request/Response domain-free 규칙과 충돌하지 않음).
  - **Command의 구조적 가드는 `BusinessException(ErrorCode.INVALID_INPUT)`을 던지며, 이것이 `domain..` 금지의 유일한 예외입니다**: Command record의 compact constructor에는 **필수값 누락 같은 구조적 가드만** 두고(형식·범위 검증은 Request의 jakarta.validation에 그대로 남겨 400 계약·한국어 메시지를 보존), 위반 시 `com.tastyhouse.domain.exception`의 `BusinessException`/`ErrorCode`를 던집니다. 즉 Command가 import할 수 있는 유일한 `com.tastyhouse.domain..` 타입은 **`domain.exception..`뿐**이며, `model`·`vo`·`repository`·`service`·`event`·`port`는 예외 없이 금지입니다.
    - **근거(그림 8.3)**: 완전 매핑에서 계층 칸으로 격리되는 것은 **도메인 모델**(그림의 `Account`)이지 예외 타입이 아닙니다 — 예외는 계층 칸이 아예 없는 **횡단 관심사**이고, 원서의 `SendMoneyCommand`도 `SelfValidating`으로 스스로 검증해 던집니다. 이 저장소에서도 `api-common-module`이 `api project(':domain-module')`로 `domain.exception`을 전 모듈에 노출해 사실상 공용 에러 계약 위치입니다. 이 carve-out이 없으면 가드 자체를 쓸 수 없습니다(스펙 §2가 "예외 없음"과 "BusinessException으로 던져라"를 동시에 요구해 모순이던 지점).
    - **`IllegalArgumentException`으로 대체하지 않습니다**: 전역 핸들러의 변환 경로가 달라 에러 wire 계약(`code`·상태)이 조용히 바뀝니다.
    - **`ErrorCode.INVALID_INPUT`(400)은 이 가드 전용으로 신설한 공통 상수입니다**: 인바운드 어댑터를 우회해 Command가 직접 조립된 경우의 구조적 위반에만 씁니다. 도메인별 구체 에러가 있으면 그쪽을 쓰고, 이 상수를 형식 검증 용도로 확대하지 않습니다.
  - **`MultipartFile`은 Command 필드로 두지 않습니다**: 업로드 경계 타입이므로 서비스 파라미터로는 허용하되(아래 [CQRS 분리 규칙](#api-모듈-application-서비스-cqrs-분리-규칙-도메인commandservice도메인queryservice) 참조), Command에는 업로드 **결과 참조**(파일 식별자·URL)만 담습니다.
- **command/VO/condition 자체의 생성**: 대상 record에 정적 팩토리 `of(...)`를 두고 `Xxx.of(a, b, c)`로 생성하는 관례는 그대로입니다. `toCommand` 본문이 `XxxCommand.of(...)`를 호출하는 형태가 표준형이며, `new`는 팩토리 내부에만 남습니다.
- **도메인/DTO → 응답 변환 (개정 — 읽기 계약 `XxxResult`를 통째로 수신. 과거 "원시타입 낱개 언패킹" 규칙의 명시적 번복)**: 응답 record의 정적 팩토리 `from(...)`은 읽기 계약 `XxxResult`를 **통째로 받습니다**(`XxxResponse.from(XxxResult result)`). 매퍼는 Service가 아니라 **Response record 자신**이며, 중첩 조립(리스트 필드·하위 Response)도 그 record의 private 헬퍼가 담당합니다.
  - **과거 규칙(폐기)**: "result의 각 필드를 원시타입으로 낱개 언패킹해 받고(`from(id, title, ...)`), 언패킹 책임은 이 Response를 호출하는 **Service**가 지며 Service에 private 매퍼(`toXxxResponse(XxxResult dto)`)를 둔다"는 규칙은 **폐기**합니다. 챕터 06이 Response를 api 모듈로 승격하면서 두 가지가 성립하지 않게 됐습니다.
    1. **문언이 지정한 언패킹 주체가 사라졌습니다** — 규칙은 그 책임을 "Service"에 맡기는데, 챕터 06이 api 모듈의 Service를 삭제했고 `apiModuleMustNotContainApplicationLayer`가 재생성을 막습니다. 남는 후보는 컨트롤러뿐이라, 규칙을 지켜도 언패킹이 api 모듈에 그대로 남습니다(위반이 이동만 합니다).
    2. **`PageResult.map`이 arity 1을 요구합니다** — `PaginationResponse.from(pageResult.map(XxxResponse::from))` 형태의 메서드 레퍼런스가 admin-api에 **56곳**이고 `PageResult#map`은 `Function<T,R>`입니다. 다인자 `from(Long, String, ...)`은 `Function`의 메서드 레퍼런스가 될 수 없어, 문언을 지키려면 56곳을 전부 인라인 람다로 풀어야 하고 **그러면 바로 아래가 경고하는 위치 착오 위험이 응답 타입 85개에 되살아납니다.**
  - **폐기된 근거**: 과거 규칙의 근거였던 *"result 객체를 그대로 받으면 Response record가 infra query result의 필드 구조를 알아야 해 infrastructure에 결합된다"*는 **더 이상 성립하지 않습니다.** `from`이 받는 타입은 `com.tastyhouse.infrastructure..`의 result가 아니라 `com.tastyhouse.application..port.out`의 **읽기 계약**이고(챕터 04 읽기 경로 포트화·챕터 05 소유 규칙의 결과), 그 계약은 프레임워크-프리라 api 모듈이 정당하게 아는 대상입니다. 즉 결합 대상이 "infra 구현"에서 "계약"으로 바뀌었으므로 회피할 이유가 없어졌습니다.
  - **위치 착오 주의는 그대로 유효합니다**: `return new XxxResponse(...)`에 값을 넘길 때 같은 타입(String/Long 등) 필드가 여러 개면 순서를 착각해도 컴파일되고 값만 조용히 뒤바뀝니다. record 필드 선언 순서와 생성자 인자 순서를 하나씩 대조합니다.
  - **Response record는 여전히 `com.tastyhouse.domain.*`·`com.tastyhouse.infrastructure.*`를 import하지 않습니다** — `apiModuleShouldBeDomainModelFree`가 모듈 전역으로 강제합니다. 다만 `*Result`가 품은 **도메인 enum의 읽기 accessor 호출**(`result.type().name()`)은 챕터 07로 정상 경로가 됐고, 그 범위는 accessor 3종으로 제한됩니다(아래 [도메인 enum 경계 규칙](#도메인-enum-경계-규칙) 참고). ArchUnit은 import가 아니라 바이트코드를 보므로 이 호출은 import 없이도 의존으로 잡힙니다.
  - **허용되는 `from` 형태는 셋입니다** (admin-api의 `from` 팩토리 84건 전수 분류 — 아래 세 형태 + 잔존 1건):
    1. **`from(XxxResult)`** — 기본형. 단일 읽기 계약 하나로 응답이 완성되는 경우(**78건**).
    2. **`from(XxxResult, List<OtherResult>)`** — 상세 + 별도 조회한 자식 목록. 두 조회 결과를 합치는 것이 아니라 **하나의 화면 계약을 두 쿼리로 채우는** 형태이며, 자식 목록의 중첩 조립은 이 record의 private 헬퍼가 담당합니다(**3건** — `ShopRiderGuideDetailResponse`(이력)·`StorePriceVerificationDetailResponse`(검수 항목)·`ReviewCommentListItemResponse`(대댓글)).
    3. **`from(..., 경계값, ...)` — Result에 없는 경계값을 함께 받는 형태**(**2건**). 인자 순서는 고정하지 않습니다.
       - `ShopDetailResponse.from(ShopManagementDetailResult result, String thumbnailImageUrl)` — 가게 상세와 이미지가 **서로 다른 읽기 포트**에 있어 유스케이스가 두 번 조회해 넘긴다.
       - `PointBalanceResponse.from(Long memberId, PointBalanceResult result)` — 응답이 요청 식별자를 되돌려줘야 하고, 호출부(`PointApiController:49`)가 `Optional`이 비면 `zero(memberId)`로 폴백하므로 `memberId`가 Result 밖에서 와야 한다.
       - **이 형태를 확대하지 않습니다** — 컨트롤러가 **계산·가공한** 값을 응답에 끼워 넣는 통로가 되면 조립 책임이 다시 컨트롤러로 새어 나갑니다. 허용 범위는 (a) 다른 읽기 포트에서 조회된 값과 (b) 요청 식별자 에코뿐이고, 그 밖의 값이 필요하면 Result에 담습니다.
  - **`from(원시타입 낱개)` 잔존 1건**: `ProductImagesResponse.from(List<String> imageUrls)`. 대응 유스케이스가 애초에 `List<String>`을 반환해 풀 Result가 없는 경우라, 이것은 폐기 대상이 아니라 **Result가 없는 조회의 정상 형태**입니다.
  - **참고 — response record 85개 중 `from`을 가진 것은 84개입니다.** 나머지 하나는 `common/response/FileResponse`로, 파일 3필드(id·name·url)를 상위 Response가 조립해 넘기는 중첩 DTO라 `of(Long, String, String)`을 씁니다(`from`이 받을 Result가 없습니다).
  - **예외 — `PageResult<T>` 변환은 그대로 `from(pageResult)`**: `PaginationResponse.from(PageResult<T> pageResult)`처럼 `PageResult<T>`(`com.tastyhouse.domain.shared.page.PageResult` — domain-module의 공용 페이징 타입)를 받아 `content()`/`page()`/`size()`/`totalElements()`를 그대로 위임하는 경우는 이 규칙의 대상이 아닙니다. `PageResult<T>` 자체는 도메인 result가 아니라 공용 페이징 계약이므로 원시타입 언패킹 대상이 아니며, 이 경우 페이징 응답(`PaginationResponse<T>`)이 `PageResult<T>`를 import하는 것은 허용합니다.
- `new`는 이러한 팩토리 메서드 **내부**에만 남깁니다(각 record가 자기 자신을 생성). 호출부에는 `new`가 남지 않는 것을 목표로 합니다.

reference 구현 (챕터 06 적용분 = admin): `admin-api`의 `notice` 도메인 — `notice/adapter/in/web/response/NoticeListItemResponse.from(NoticeManagementListItemResult result)`(기본형) + 컨트롤러의 `PaginationResponse.from(pageResult.map(NoticeListItemResponse::from))`(PageResult 위임은 예외 그대로). 형태 2 사례: `ShopRiderGuideDetailResponse.from(ShopRiderGuideResult, List<ShopRiderGuideHistoryResult>)`. 중첩 조립 사례: `admin-api`의 `order` 도메인 — `OrderDetailResponse.from(OrderDetailResult)`가 `OrderProductResponse`/`PaymentSummaryResponse` 중첩 리스트·필드를 각 record의 `from`으로 위임 조립한다(`.map(OrderProductResponse::from)`). **ceo(챕터 09)·web(챕터 10)에도 같은 설계가 적용됐다**(reference: `ceo-api`의 `shop`·`product` 도메인 — `ShopBusinessHourResponse.from(ShopBusinessHourResult)`·`ProductOptionGroupResponse.from(ProductOptionGroupViewResult)`; `web-api`의 `order`·`auth` 도메인 — `OrderDetailResponse.from(OrderDetailViewResult)`·`AuthSocialLoginResponse.from(SocialLoginResult)`). **3개 앱이 같은 규칙을 따르므로 앱별 예외가 없다.**

## command/DTO 지역 변수 추출 규칙 (호출 인자로 인라인 조립 지양)

위 [DTO 조립 규칙](#dto-조립-규칙-new-직접-호출-지양)으로 Command를 조립하더라도, **그 조립 결과를 UseCase 메서드의 인자 자리에 인라인으로 바로 넘기지 않고, 먼저 지역 변수(`command`)로 추출한 뒤 그 변수를 전달합니다.** 무엇을 만드는지와 어디에 넘기는지는 별개이며, 이 규칙은 후자를 다룹니다.

**컨트롤러 표준형**은 아래 두 줄입니다(조립 주체가 Service에서 컨트롤러로 이동한 A-1 개정 반영).

```java
XxxCommand command = request.toCommand(...);
xxxCommandUseCase.method(command);
```

- **왜 지역 변수로 추출하는가**: (1) 조립(무엇을 만드는가)과 호출(어디에 넘기는가)이 한 줄에 겹쳐 있으면, 인자가 많은 command일수록 한 줄이 길어지고 어떤 값이 어느 필드로 가는지 읽기 어렵습니다. 이름 붙은 지역 변수로 분리하면 "이 줄은 command를 만든다 / 이 줄은 그것을 서비스에 넘긴다"가 문장 단위로 드러납니다. (2) 디버깅 시 조립된 command에 중단점·로그를 걸거나 값을 들여다보기 쉽습니다. (3) 호출부 형태가 도메인 간 동일해져(항상 `Xxx command = Xxx.of(...); service.method(id, command);`) 리뷰·검색·패턴 일치가 단순해집니다.
- **적용 대상**: command 서비스 호출에 넘기는 **command DTO**와, 그 **command 서비스 호출의 인자로 넘기는 `XxxId.of(id)` 식별자 승격**이 대상입니다. command 변수명은 `command`로 통일하고(한 메서드에 command가 하나인 것이 일반적), 식별자 VO는 `XxxId xxxId = XxxId.of(id);`로 추출합니다.
- **컨트롤러의 `XxxId.of()` 식별자 승격 추출 규칙은 소멸했습니다 (개정)**: Command가 식별자를 `Long`으로 보유하므로 컨트롤러에 `XxxId.of(id)`가 아예 등장하지 않습니다. `update`/`delete` 계열은 **경로 변수 `id`를 `toCommand(id)`/`Command.of(id, ...)`의 파라미터로 흡수**하며, 컨트롤러가 식별자를 UseCase 메서드의 별도 인자로 넘기지 않습니다(`useCase.updateXxx(command)` — `useCase.updateXxx(xxxId, command)`가 아닙니다). 식별자만 필요한 `delete`·상태전이도 식별자 하나를 담은 Command를 넘깁니다.
- **서비스 내부의 도메인 승격 추출 관례는 유지합니다**: `{도메인}CommandService`가 Command를 받아 `XxxId xxxId = XxxId.of(command.id());`처럼 지역 변수로 승격한 뒤 도메인에 넘기는 형태는 그대로입니다 — 사라진 것은 *컨트롤러의* 승격이지 승격 자체가 아닙니다.
- **적용 예외 (인라인 유지)**: (1) 이미 지역 변수로 조립되고 있던 **`SearchCondition`은 그대로 둡니다**(조회 메서드에서 관례적으로 `XxxSearchCondition condition = ...of(...)`로 이미 분리 — reference: `NoticeQueryService#getNotices`의 `condition`). (2) 조회 UseCase 호출은 Command를 쓰지 않고 경계 타입 인자를 그대로 넘기므로 이 규칙의 대상이 아닙니다. (3) **command 팩토리 내부로 들어가는 식별자**(`XxxCommand.of(XxxId.of(id), ...)`처럼 `Command.of(...)`의 인자로 중첩된 `Id.of`)는 command 조립의 일부이므로 그대로 둡니다 — 이때는 command 자체를 지역 변수로 추출하는 것이 규칙이며 내부 `Id.of`는 건드리지 않습니다(reference: `ReviewService`의 `ReviewDeleteCommand.of(ReviewId.of(reviewId), ...)`). (4) 응답 변환 `XxxResponse.from(...)`도 대상이 아닙니다(반환식에 바로 쓰는 것이 자연스러움).
- **죽은 코드 금지**: 추출로 대체된 기존 인라인 한 줄을 `//` 주석으로 남기지 않습니다. 추출한 형태만 남깁니다.

```java
// 지양 — 컨트롤러가 Request를 원시 필드로 언패킹하고 도메인 enum까지 승격 (A-1에서 폐기된 형태)
Long bannerId = bannerCommandService.createBanner(
    BannerType.from(request.type()), request.title(), request.imageFileId(),
    request.linkUrl(), request.startDate(), request.endDate(), request.sort(), request.visible()
);

// 권장 — Request가 Command로 변환하고, 컨트롤러는 지역 변수로 추출해 UseCase에 전달
BannerCreateCommand command = request.toCommand();
Long bannerId = bannerCommandUseCase.createBanner(command);
```

update 계열은 **경로 변수 `id`를 `toCommand`의 파라미터로 흡수**합니다 — 컨트롤러가 식별자를 별도 인자로 넘기지 않습니다:

```java
// 지양 — 식별자를 UseCase 인자 자리에 따로 넘김(+ 컨트롤러의 XxxId 승격)
BannerUpdateCommand command = request.toCommand();
bannerCommandUseCase.updateBanner(BannerId.of(id), command);

// 권장 — 경로 변수를 Command 안으로 흡수
BannerUpdateCommand command = request.toCommand(id);
bannerCommandUseCase.updateBanner(command);
```

본문이 없는 `delete`·상태전이 계열은 식별자만 담은 Command를 정적 팩토리로 조립합니다:

```java
// 지양
bannerCommandUseCase.deleteBanner(BannerId.of(id));

// 권장
BannerDeleteCommand command = BannerDeleteCommand.of(id);
bannerCommandUseCase.deleteBanner(command);
```

인증 주체 식별자처럼 요청 본문에 없는 값도 같은 방식으로 주입합니다:

```java
ReviewCreateCommand command = request.toCommand(userDetails.getMemberId());
Long reviewId = reviewCommandUseCase.createReview(command);
```

reference 구현: `admin-api`의 `notice` 도메인 — `NoticeCommandService#updateNotice`·`#deleteNotice`(`NoticeId noticeId = NoticeId.of(id);`로 식별자를 지역 변수 추출한 뒤 도메인 로드·변경·`save`). CQRS 분리로 core command 서비스 호출이 사라져 command DTO 자체는 없어졌고, 식별자 추출 관례는 그대로 유지된다. 식별자 추출 동일 적용: `banner`(`updateBanner`·`deleteBanner`), `coupon`(`updateCoupon`·`deleteCoupon`·`issueCoupon`), `policy`(`updatePolicy`·`activateCurrentPolicy`), `web-api`의 `reservation`(`cancel`·`confirm`·`reject`·`complete`), `scheduler`(`ProductScheduler#markBbqOptionsSynced`). command 추출 동일 적용: `admin`(`AdminAccountService#create`), `bug`(`changeStatus`·`classify`·`assign`), `createBanner`·`createCoupon`·`createPolicy`.

## record 파일 분리 규칙 (중첩 record 선언 지양)

**`record`는 서비스·컨트롤러 등 다른 클래스 본문 안에 중첩 선언하지 않고, 각 도메인의 관례 위치에 독립된 `.java` 파일로 둡니다.** 클래스 안에 DTO record를 함께 두면 파일 응집도가 떨어지고, 다른 클래스에서 참조할 때 `Outer.Inner` 접두사가 붙어 결합도가 커지며, DTO 조립 규칙(정적 팩토리 위임)을 적용하기도 불리합니다.

- **분리 위치 (개정됨 — 읽기 경로 포트화로 재개정)**: 각 도메인의 기존 관례를 따릅니다. api 모듈(web-api/admin-api/ceo-api)의 응답성 record는 해당 도메인 패키지의 `response/` 하위(요청은 `request/`)에, 조회 결과 Result·`SearchCondition`은 **`com.tastyhouse.application.<ctx>.port.out`** 에 둡니다(과거 `infrastructure:persistence`의 `<ctx>/query/`였으나 챕터 04 읽기 경로 포트화로 이관).
- **접근제어자**: 별도 파일로 빼면 최상위 타입이 되므로 `public record`로 선언합니다. 한 클래스 내부에서만 쓰이던 `private`/`package-private` 헬퍼 record도 분리 시 `public`으로 격상합니다.
  - **⚠️ `port.out`의 Result record는 이 규칙이 특히 강제됩니다 — package-private이면 런타임에만 깨집니다.** QueryDSL `Projections.constructor(Xxx.class, ...)`가 만드는 `ConstructorExpression`은 대상 생성자를 `Class#getConstructors()`로 탐색하는데, 이 메서드는 **public 생성자만** 반환합니다. package-private record의 canonical 생성자는 package-private이므로 **같은 패키지의 DAO가 투영하더라도 리플렉션에서는 보이지 않아** `ExpressionException: No constructor found for class ...`로 실패합니다. `Projections.constructor`는 `Class<?>`를 받으므로 **컴파일은 통과하고 그 쿼리가 실행되는 순간에만 500**이 납니다.
  - "DAO 내부에서만 쓰는 중간 투영이니 노출을 좁힌다"는 판단이 이 함정에 빠지기 쉽습니다. 실제 장애 선례: `ShopRiderGuidePickupPresenceResult`가 그 의도로 package-private으로 선언돼 admin "라이더 안내 검수" 목록 조회가 전부 500이 났고, 같은 패키지의 다른 Result record 30여 개는 모두 `public`이라 이 한 건만 어긋난 상태였습니다.
  - **가드 테스트가 강제합니다**: `QueryResultRecordVisibilityTest`가 infra 내부 투영(`<ctx>/query/`)과 읽기 계약 패키지(`com.tastyhouse.application..port.out`) **양쪽**의 최상위 record를 클래스패스 스캔해 `public` 여부를 검증합니다(목록 수동 관리 불필요). **이 문서는 챕터 04부터 "port.out을 스캔한다"고 서술해 왔으나 실제 코드는 infra 패턴만 스캔하고 있었고, 챕터 09에서 계약 패턴을 추가해 문서와 코드를 일치시켰습니다** — 그동안 정작 주된 투영 대상인 계약 Result가 이 가드의 사각지대에 있었습니다. `infrastructure:persistence`의 신설 `ProjectionConstructorMatchingTest`는 select 절 인자 개수·대상 record 생성자 파라미터 개수 일치를 소스 스캔으로 검증해, `@QueryProjection`이 주던 컴파일 게이트 상실을 보완합니다. DAO 본문에 중첩된 `private` 헬퍼 record는 `new`로 직접 조립하는 내부 계산용이라 검사 대상이 아닙니다 — 투영에 쓰려면 애초에 이 규칙대로 독립 파일로 분리해야 하고, 그 시점에 가드 대상이 됩니다.
- **적용 대상**: 응답/결과 DTO뿐 아니라 서비스 내부 전용 헬퍼 record(예: 조회 중간 계산용)도 동일하게 분리합니다.
- 이미 자기 파일 하나에 정의된 최상위 record(도메인 이벤트, ID VO 등)는 그대로 두며, 이 규칙은 "다른 클래스 본문 안에 중첩된 record"를 제거하는 것을 목표로 합니다.
- **적용 대상은 다른 클래스가 참조하는 DTO record입니다**: 같은 패키지 내부에서만 쓰이는 헬퍼 유틸 클래스는 `public` 격상 대상이 아니며 package-private을 유지해 노출을 좁힙니다(reference: `MailVerificationMessage`·`SmsVerificationMessage`(도메인 서비스 전용 문구), `MailVerificationMapper`(infrastructure)).

reference 구현: `web-api`의 `NoticeListItemResponse`(`notice/response/`)와 `com.tastyhouse.application.product.port.out` 결과 record들(과거 core `application/dto/result/`의 `OptionInfo`처럼 서비스 본문에 중첩돼 있던 헬퍼 record를 `private` → `public` 최상위 파일로 격상한 전례). (과거 도메인별 페이징 래퍼 `NoticePageResponse`/`PolicyPageResponse`/`OrderPageResponse`는 공용 `common/PaginationResponse.java` 하나로 통합되어 삭제되었습니다 — [페이징 응답 공용 제네릭 래퍼 규칙](#페이징-응답-공용-제네릭-래퍼-규칙-paginationresponset) 참고.)

## ID VO(식별자 값 객체) 경계 규칙

도메인 식별자는 계층에 따라 `Long`과 `XxxId`(record VO)를 구분해서 사용합니다. 같은 코드베이스에서 도메인마다 이 규칙이 갈리면(예: `getMemberId()`가 도메인에 따라 `MemberId`를 반환하기도, `Long` FK를 반환하기도 하는 것처럼) 같은 메서드 이름이 다른 타입을 의미하게 되어 혼동과 버그를 유발합니다. 아래 표의 경계선을 기준으로 전 도메인에 동일하게 적용합니다.

| 계층 | ID 타입 | 비고 |
|---|---|---|
| HTTP 경계 (컨트롤러 `@PathVariable`/요청·응답 필드) | `Long` | `XxxId`는 api 모듈 밖(HTTP)으로 노출하지 않습니다 |
| api 모듈 Service (`{도메인}CommandService`/`{도메인}QueryService`) | 입력은 `Long`, 여기서 `XxxId`로 승격 | `XxxId.of(long)` 정적 팩토리로 승격(`new` 직접 호출 금지) |
| domain-module 도메인 서비스(`<ctx>/service/`) public 시그니처 | `XxxId` | 도메인 서비스에 넘기는 파라미터·반환도 동일 |
| domain repository(write 포트) 인터페이스 (`findById` 등) | `XxxId` | |
| 읽기 포트 계약(읽기 계약(`com.tastyhouse.application..port.out`)의 Result·`SearchCondition`) + `infrastructure:persistence` query DAO 구현 | `Long` | 표현 목적 조회는 도메인 모델을 거치지 않아 VO를 쓰지 않습니다(`SearchCondition` 필드도 `Long`). **ID가 아닌 단일값 VO(`Amount` 등)도 같습니다** — 챕터 07 참조 |
| 도메인 모델 내부 / 도메인 이벤트 | `XxxId` | **모든 애그리거트 간 FK에 예외 없이 적용**(정책 A) — 자기 자신의 PK(`Long id`, 재구성 전 신규 상태를 null로 표현하는 필드)는 대상이 아니며, `getXxxId()`가 이를 VO로 래핑해 노출합니다 |
| 엔티티 `@Id` 필드 | `Long` + `@GeneratedValue(IDENTITY)` | 유지 — VO로 바꾸지 않습니다(자기 PK는 이 규칙의 예외이며 혼동 방지 대상) |
| 엔티티 자기 ID getter | `getXxxId(): XxxId` | 내부 `Long id`를 VO로 래핑해 노출 |
| **엔티티 FK 필드(다른 애그리거트 참조)** | **raw `Long`** (`@Convert` 미사용) | 정책 B(개정) — 아래 "엔티티 FK는 raw `Long`" 절 참고 |
| 결과 DTO(result record)에서 id 추출 | `entity.getXxxId()` | `getId()`(Long)를 응답에 직접 노출하지 않습니다 |

### 엔티티 FK는 raw `Long`, 매퍼에서 `IdMapping`으로 승격/언패킹 (정책 B)

**JPA 엔티티의 크로스-애그리거트 FK 필드는 `@Convert`로 VO에 매핑하지 않고 raw `Long`으로 둡니다.** 도메인 모델·domain repository·도메인 서비스는 이 규칙과 무관하게 그대로 `XxxId` VO를 유지합니다(위 표의 나머지 행은 변경 없음) — 변경 대상은 JPA 엔티티와 매퍼뿐입니다.

**왜 정책 A(엔티티 FK도 `@Convert`로 VO)에서 정책 B로 전환했는가**: 엔티티 FK를 VO로 매핑하면 QueryDSL이 그 필드에 대해 `NumberPath<Long>`이 아닌 VO 타입 path를 생성합니다. query DAO 계층은 항상 raw `Long`을 쓰므로(위 표), 그 컬럼을 다른 엔티티의 raw `Long` PK와 조인하거나 `Long`-typed Result 필드로 투영하는 코드가 컴파일되지 않아 문자열 필드명 기반 우회 유틸(`ConvertedIdPaths`, 폐지됨)이 필수였습니다. 이 우회는 (1) 파라미터 바인딩 시 VO/Long 타입 불일치로 `QueryArgumentException`, (2) 투영 시 컨버터가 VO 인스턴스를 돌려줘 `Long`-typed Result 생성자와 불일치 — 이 두 번째 결함은 **해당 FK의 행이 실제로 존재할 때만** 터져 빈 테이블 테스트로는 잡히지 않고 오래 숨어 있었습니다. 한 도메인의 FK를 VO로 전환하면 그 컬럼을 조인하던 **다른 도메인**의 query DAO가 함께 깨지는 크로스 도메인 회귀도 반복됐습니다. 엔티티 FK를 raw `Long`으로 두면 이 세 결함 유형이 구조적으로 사라집니다 — QueryDSL이 애초에 VO path를 만들 일이 없기 때문입니다.

**매퍼(`<ctx>/persistence/XxxMapper.java`)의 VO↔raw 변환은 `infrastructure/shared/persistence/IdMapping`으로 통일**합니다. `toDomain`에서는 `IdMapping.vo(entity.getXxxId(), XxxId::of)`로 승격하고, `toEntity`/`applyChanges`에서는 `IdMapping.raw(domain.getXxxId(), XxxId::value)`로 언패킹합니다. `CeoId.of(entity.getCeoId())`처럼 직접 호출하지 않습니다.

- **이유**: 모든 `XxxId` VO는 compact constructor에서 null을 거부하는데, 일부 FK 컬럼은 nullable입니다(예: `Shop.ceo_id` — 점주 미배정, `BugReport.assignee_admin_id` — 미배정). 직접 호출하면 컴파일은 통과하고 **해당 FK가 실제로 null인 행을 읽을 때만** `IllegalArgumentException`으로 실패합니다 — 위와 같은 "행이 존재할 때만 터지는" 결함 유형입니다. `IdMapping`은 이 판단을 호출부에서 없애 nullable 여부를 몰라도 항상 안전한 형태를 기본값으로 만듭니다.
- **NOT NULL 컬럼에서도 예외 없이 통일**합니다. "NOT NULL이면 직접 호출, nullable이면 헬퍼만"처럼 컬럼별로 형태를 나누지 않습니다 — 혼재하면 작성자·리뷰어가 매번 엔티티의 nullable 여부를 확인해야 하고, 위험한 직접 호출 형태가 흔해 보여 눈에 띄지 않게 됩니다.
- **`raw(...)`는 NOT NULL 컬럼에서도 죽은 코드가 아닙니다** — 도메인 모델이 아직 배정되지 않은 상태(예: 점주 미배정 `Shop`)를 null VO로 들고 있을 수 있으므로, `domain.getCeoId().value()`를 직접 호출하면 VO가 null일 때 NPE로 실패합니다.
- **`Xxx.of(...)` 위임 규칙의 예외가 아닙니다**: `IdMapping.vo(raw, CeoId::of)`의 메서드 레퍼런스가 `of()` 팩토리를 경유하므로 `new`는 여전히 팩토리 내부에만 남습니다.

**단일값 VO도 읽기 계약에서는 경계 타입으로 내립니다 (챕터 07).** `OrderPaymentResult.amount`가 `Amount` VO였던 것은 `PAYMENT.amount`가 `@Convert` 매핑이라 QueryDSL이 `SimplePath<Amount>`를 생성한다는 **영속 매핑의 산물**이었고, 그 결과 api 모듈이 `result.amount().value()`로 도메인 VO를 만져 `apiModuleShouldBeDomainModelFree`의 유일한 비-enum 위반이 됐습니다. 컴포넌트를 `Integer`로 바꾸고, VO → 원시타입 언랩은 DAO가 **fetch 직후 재조립**으로 수행합니다(`Projections.constructor`가 생성자 직접 투영이라 변환을 투영식에 넣을 수 없으므로 — `withResolvedShopThumbnailImageUrl`과 같은 형태). 투영 중간 타입은 `PaymentProjection`(infra 내부, `public record`)이 받습니다. **`@Convert`/`AmountConverter` 자체는 write 엔티티에서 그대로 유지합니다** — 바뀐 것은 읽기 계약의 타입뿐입니다.

**query DAO의 Result record·SearchCondition도 raw `Long`을 예외 없이 씁니다.** 정책 A 시절 일부 Result record가 `@Convert`가 투영해 주는 VO를 그대로 필드 타입으로 삼은 사례가 있었으나(예: `MemberReferralResult.referrerId : MemberId`), 이는 "query DAO 계층은 항상 raw Long" 규칙 위반이었고 정책 B 전환으로 전부 `Long`으로 교정했습니다. 도메인/write 포트로 값을 넘겨야 하는 소비 지점(예: 랭킹 도메인 포트 `MemberReviewCount.of(MemberId, ...)`)은 그 호출부에서 `MemberId.of(result.memberId())`로 승격합니다 — 승격 책임은 query 결과가 아니라 그 결과를 도메인 포트에 넘기는 어댑터가 집니다.

**`XxxId` VO 표준 형태** (`domain-module/src/main/java/com/tastyhouse/domain/<ctx>/vo/XxxId.java`, 변경 없음):

```java
public record XxxId(Long value) {

    public XxxId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("XxxId는 양수여야 합니다: " + value);
        }
    }

    public static XxxId of(Long value) {
        return new XxxId(value);
    }
}
```

- `Long → XxxId` 승격도 위 DTO 조립 규칙과 동일하게 `new XxxId(id)` 대신 `XxxId.of(id)`를 사용합니다. `new`는 `of()` 팩토리 내부에만 남습니다.
- **엔티티 FK에는 더 이상 `AttributeConverter`/`@Convert`를 두지 않습니다** — `AttributeConverter<XxxId, Long>` 컨버터 클래스(`*IdConverter`)는 정책 B 전환으로 전부 삭제되었습니다. `@Convert`는 ID가 아닌 값 객체(예: `AmountConverter`)에만 계속 사용합니다.

reference 구현: `infrastructure:persistence`의 `shared/persistence/IdMapping`(vo/raw 헬퍼), `shop/persistence/ShopMapper`(nullable FK 3개 — `ceoId`/`thumbnailImageFileId`/`trademarkImageFileId` — 를 포함한 대표 사례), `payment` 도메인의 `PaymentJpaEntity.orderId`(raw `Long`, `AmountConverter`만 계속 `@Convert` 사용). VO 승격이 query 결과 소비 지점에서 이뤄지는 사례: `rank` 도메인의 `MemberReviewCountAdapter`(`MemberId.of(result.memberId())`).

## 도메인 enum 경계 규칙

도메인 enum(`com.tastyhouse.domain.<ctx>.model`의 `BannerType`·`EventStatus`·`FoodType` 등)은 **ID VO와 동일한 경계 원칙**을 따릅니다. HTTP 경계(컨트롤러 `@RequestParam`/Request 필드)는 `String`(다중값은 `List<String>`)으로 받고, web-api/admin-api Service에서 core enum으로 **승격**합니다. 이는 ID를 `Long`으로 받아 `XxxId.of()`로 승격하는 것과 대칭이며, "컨트롤러·Request record는 `com.tastyhouse.domain.*`(및 `com.tastyhouse.infrastructure.*`)를 import하지 않는다"는 상위 규칙(각 모듈 `AGENTS.md`)의 enum 케이스 구체화입니다. 컨트롤러가 도메인 enum을 직접 노출하면 API 계약이 도메인 모델에 결합되어, enum 상수 추가가 곧 공개 스키마 변경이 되고 어댑터가 도메인을 알게 되는 레이어 위반이 발생합니다.

**이 규칙은 인바운드(승격) 방향입니다. 아웃바운드(강등) 방향은 챕터 06·07로 정해졌습니다.** 챕터 06이 Response 조립을 컨트롤러로 올린 뒤, 읽기 계약 `*Result`가 품은 도메인 enum을 api 모듈의 `..response..` record가 `.name()`으로 읽어 `String`으로 내리는 것이 **정상 경로**입니다 — 규칙이 막으려던 것은 "api 모듈이 도메인 로직을 수행하는 것"이고, 값 집합에서 상수명을 꺼내는 것은 표현 조립이기 때문입니다. **단 읽기 accessor 3종에 한정되며**, 도메인 enum이 가진 비즈니스 로직(`MemberGrade#fromReviewCount`·`OrderStatus#canTransitionTo` 등 13개 enum)의 호출은 여전히 금지입니다(`apiModuleShouldOnlyReadDomainEnums`가 강제).

| 계층 | enum 타입 | 비고 |
|---|---|---|
| HTTP 경계 (컨트롤러 `@RequestParam`/요청 필드) | `String` / `List<String>` | 도메인 enum을 api 모듈 밖(HTTP)으로 노출하지 않습니다 |
| web-api/admin-api Service | 입력은 `String`, 여기서 도메인 enum으로 승격 | `Enum.from(String)` 정적 팩토리로 승격(`valueOf` 산재·`new` 금지) |
| domain-module 도메인 서비스 public 시그니처 | 도메인 enum | String이 domain-module로 내려가지 않습니다 |
| 도메인 모델 내부 / 도메인 이벤트 | 도메인 enum | |
| **아웃바운드 — 응답 조립(api 모듈 `..response..`)** | 도메인 enum을 **읽어** `String`으로 강등 | **챕터 07** — `result.type().name()`. 읽기 accessor 3종(`name`·`getDescription`·`getDisplayName`)만 허용하며 `from(String)` 승격·상태 전이 판정·등급 계산은 금지(`apiModuleShouldOnlyReadDomainEnums`) |

- **변환 팩토리 위치**: 도메인 enum 자신에 `static Xxx from(String code)`를 두고, 실패 시 프로젝트 공통 `BusinessException(ErrorCode.XXX_TYPE_UNKNOWN)`(400)으로 변환합니다. 생짜 `IllegalArgumentException`(`No enum constant …`)을 노출하지 않습니다. 이는 DTO 조립 규칙("변환 책임을 대상 타입에 위임")과 일관됩니다.

```java
public enum BannerType {

    HOME, SIDEBAR;

    public static BannerType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BANNER_TYPE_UNKNOWN,
                ErrorCode.BANNER_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
```

- **null 허용 파라미터**(`required = false`)는 Service에서 `s == null ? null : Xxx.from(s)`로 승격해 기존 동작을 보존합니다.
- **Swagger**: String 파라미터는 자동 enum 스키마가 생성되지 않으므로 후보값을 수동 명시합니다 — Request 필드는 `@Schema(allowableValues = {...})`, `@RequestParam`은 `@Parameter(schema = @Schema(allowableValues = {...}))`. 필수 enum 필드의 `@NotNull`은 String 전환 시 `@NotBlank`로 바꿉니다.

reference 구현: `banner` 도메인 — `BannerType.from(String)`, `BannerService`(admin-api, `String` 수신 후 승격 담당), `BannerApiController`/`BannerCreateRequest`/`BannerUpdateRequest`(`String type` + `allowableValues`). 변환 실패 `BusinessException` 선례: `rank` 도메인의 `ErrorCode.RANK_TYPE_UNKNOWN`.

## enum ↔ DB 컬럼 매핑 규칙 (엔티티 enum은 `EnumType.STRING` + `@Column(columnDefinition = "VARCHAR(n)")` 필수)

도메인 enum을 엔티티 필드로 영속화할 때는 **반드시 `@Enumerated(EnumType.STRING)`로 매핑하고, `@Column`에 `length`와 함께 `columnDefinition = "VARCHAR(n)"`를 명시**합니다. **`@Enumerated(EnumType.ORDINAL)`은 전 도메인 금지**합니다. DB 컬럼(`schema.sql`/`alter.sql`)도 네이티브 MySQL `ENUM(...)`이 아닌 `VARCHAR(n)`로 정의합니다.

**왜 `columnDefinition`이 필수인가 (핵심)**: 이 프로젝트는 Spring Boot 3.2.x(**Hibernate ORM 6.4.x**) + MySQL이며 `hibernate.dialect`를 명시하지 않아 `MySQLDialect`가 자동 감지됩니다. **Hibernate 6.2+ 부터 `MySQLDialect`는 `@Enumerated(EnumType.STRING)` enum을 기본으로 네이티브 `ENUM('a','b',...)` 컬럼 타입으로 매핑**합니다. 따라서 `columnDefinition` 없이 `@Column(length = 20)`만 두면, DB 컬럼이 `VARCHAR(20)`로 올바르게 되어 있어도 **Hibernate는 `ENUM(...)`을 기대**하게 되어 `ddl-auto=validate`에서 다음처럼 부팅이 실패합니다:

```
SchemaManagementException: Schema-validation: wrong column type encountered in column [category] in table [BUG_REPORT];
found [varchar (Types#VARCHAR)], but expecting [enum ('payment',...) (Types#ENUM)]
```

`columnDefinition = "VARCHAR(20)"`를 명시하면 Hibernate가 dialect 기본 enum 매핑 대신 그 타입을 그대로 기대하므로, `VARCHAR` DB 컬럼과 일치해 검증을 통과합니다. (동등한 대안으로 `@JdbcTypeCode(SqlTypes.VARCHAR)`가 있으나, **이 프로젝트의 확립된 관례는 `columnDefinition`**이므로 그것으로 통일합니다.)

- **엔티티 표준 형태** (`length` + `columnDefinition` 병기):

```java
@Enumerated(EnumType.STRING)
@Column(name = "category", length = 20, columnDefinition = "VARCHAR(20)")
private BugReportCategory category;
```

- **`length` / `columnDefinition`의 `n` 값**: 가장 긴 enum 상수 이름이 들어갈 길이로 정합니다(대부분 `20`으로 충분, 더 긴 상수는 `50`). **일괄 고정값이 아니라 실제 상수 길이에 맞춥니다** — 예: `Order.order_status`는 `20`, `Order.order_method`는 값이 길어 `50`. `length`와 `columnDefinition`의 숫자는 서로 일치시킵니다.
- **`EnumType.ORDINAL` 금지 이유**: enum 선언 순서(0,1,2…)를 저장하므로 상수를 추가·재배열하면 기존 데이터 의미가 조용히 바뀝니다. 항상 `EnumType.STRING`(상수 이름 문자열 저장)을 씁니다.
- **DDL 표준 형태** (`schema.sql`/`alter.sql`): 네이티브 `ENUM(...)`이 아니라 `VARCHAR(n)` + **주석으로 허용값 나열**. 엔티티의 `columnDefinition` 숫자와 일치시킵니다.

```sql
-- 올바름: VARCHAR + 허용값 주석
category VARCHAR(20)  COMMENT '분류 (PAYMENT, LOGIN, ORDER, RESERVATION, UI, PERFORMANCE, ETC / 미분류 시 NULL)',

-- 금지: 네이티브 ENUM 타입
category ENUM('payment','login','order','reservation','ui','performance','etc'),
```

- **정합성 책임**: 엔티티 enum 필드를 추가/변경할 때 `@Column`에 `columnDefinition = "VARCHAR(n)"`가 있는지 반드시 확인하고, `schema.sql`/`alter.sql`의 컬럼 타입·`n`·nullable을 엔티티와 일치시킵니다.
- **`ddl-auto=validate`를 낮추지 않습니다**: 이 오류를 피하려고 `validate`를 `none`/`update`로 바꾸지 않습니다 — 검증 게이트는 그대로 두고 `columnDefinition`으로 매핑을 정렬합니다.

이 규칙 위반이 실제로 유발한 오류 선례: `BugReport`의 enum 필드들만 `columnDefinition`을 누락(`@Column(length = 20)`만)해, `MySQLDialect`가 `ENUM(...)`을 기대 → DB의 `VARCHAR(20)` 컬럼과 불일치로 `wrong column type ... found [varchar], but expecting [enum (...)]` 발생 → SessionFactory 빌드 실패. 다른 도메인은 `columnDefinition`을 병기해 이 문제가 없었음.

reference 구현: `order` 도메인 — `Order`(`@Enumerated(EnumType.STRING)` + `@Column(columnDefinition = "VARCHAR(20)")` 형태의 `order_status`, `VARCHAR(50)`의 `order_method`), `referral`의 `MemberReferral`, `payment`의 `PaymentRefund`. 수정 후 `bug` 도메인의 `BugReport`(`status`/`category`/`priority`/`platform` 모두 `columnDefinition = "VARCHAR(20)"` 병기)도 동일 형태.

## 컨트롤러 조회 파라미터 수신 규칙 (조회 파라미터는 `@ModelAttribute` Request record)

**GET 조회/검색 API의 조회 파라미터는 개수와 무관하게(1개여도)** 개별 `@RequestParam`으로 나열하지 않고 `{도메인}SearchRequest` record로 묶어 `@Valid @ModelAttribute`로 받습니다. `@ModelAttribute`는 **쿼리스트링을 포함한 request parameter를 model 객체에 바인딩**합니다(Servlet의 request parameter는 form data + query string 모두 포함). "필터 2개 이상일 때만 record" 같은 개수 기반 조건을 두지 않는 이유는, **"조회 파라미터 = 항상 SearchRequest"라는 예외 없는 단일 규칙이 예측가능성·리뷰 단순성 면에서 낫고**, 필터가 하나 추가되는 순간 시그니처를 고치지 않아도 되기 때문입니다. 이 결정은 실무상 개수 기준(Spring 레퍼런스는 단일 값에 `@RequestParam`도 허용)을 **일관성 우선으로 상향 적용**한 프로젝트 커스텀 컨벤션입니다.

| 상황 | 수신 방식 | 비고 |
|---|---|---|
| 조회 파라미터(필터/검색어) **1개 이상** | `{도메인}SearchRequest` record + `@Valid @ModelAttribute` | 값이 1개여도 record로 감쌉니다 |
| 페이징(`page`/`size`) | 기존 `@ModelAttribute PageRequest` 별도 병기 | SearchRequest에 흡수하지 않고 인자로 나란히 둡니다 |
| `@PathVariable` id | `Long` 유지 | ID VO 경계 규칙과 동일 |

- **적용 예외**: 조회 파라미터가 **아예 없는**(페이징만 있는) GET은 `@ModelAttribute PageRequest`만 두고 SearchRequest를 만들지 않습니다. `@PathVariable`만 받는 단건 조회도 대상이 아닙니다.
- **순수 데이터 홀더 유지 (DTO 조립 규칙과 일관)**: `{도메인}SearchRequest`는 검증 + Swagger 스키마만 갖는 순수 record로 두고, `toCondition()`/`toCommand()` 같은 변환 메서드를 두지 않습니다. 컨트롤러가 `request.title()` 등 **개별 원시 필드로 언패킹**해 Service에 전달하고, Service가 `{도메인}SearchCondition.of(...)`로 조립합니다. → Service 시그니처(개별 파라미터 수신)는 **변경하지 않습니다**.
- **기본값·필수 표현**: `@RequestParam(defaultValue = ...)`로 표현하던 기본값은 record의 **compact constructor에서 정규화**하거나(`type == null ? "ALL" : type`) 필드 초기화로 대체합니다. 필수 파라미터는 필드에 Bean Validation(`@NotBlank`/`@NotNull` 등)을 부착하고 `@Valid`로 강제합니다.
- **경계 타입 (ID VO·enum 경계 규칙과 일관)**: record 필드에서도 enum 후보는 `String`/`List<String>`, FK/식별자는 `Long`으로 받습니다. `com.tastyhouse.domain.*`(도메인 enum·VO)를 Request가 import하지 않습니다. 승격은 기존대로 Service에서 `Enum.from(String)`·`XxxId.of(Long)`으로 수행합니다.
- **파일 분리·명명 (record 파일 분리·명명 순서 규칙과 일관)**: 컨트롤러 본문 중첩이 아니라 도메인 폴더 `request/`에 `public record {도메인}SearchRequest`로 둡니다. 이름은 `{도메인}` 접두 순서(예: `ShopSearchRequest`, `NoticeSearchRequest`, `RankSearchRequest`)로 기존 `{도메인}CreateRequest`/`{도메인}UpdateRequest`와 이름순 인접시킵니다.
- **Swagger**: 기존에 `@RequestParam`에 붙던 `@Parameter(schema = @Schema(allowableValues = {...}))`는 record 필드의 `@Schema(allowableValues = {...})`로 이전합니다. `required = false` 옵션 동작(미바인딩 시 null/빈 리스트)은 그대로 보존합니다.
- **적용 대상**: 신규 조회 API는 이 규칙을 따르고, 기존 `@RequestParam` GET(다중: `shop/getLatestShops`·admin `notice/getNotices`, 단일: `search/searchMenus`의 `query`, `rank/getMemberRankList`의 `type`/`limit` 등)도 수정 시 전환합니다.

reference 구현(전환 예정): `web-api`의 `ShopSearchRequest`(`shop/request/`, `getLatestShops`), `admin-api`의 `NoticeSearchRequest`(`notice/request/`, `getNotices`). 이미 정착된 `@ModelAttribute` record 선례: 공통 `PageRequest`(`web-api`/`admin-api`의 `common/PageRequest`).

**참고 자료 (Spring 공식)**: Spring Framework Reference — `@ModelAttribute` method arguments: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/modelattrib-method-args.html ("binds request parameters ... onto a model object"; 보안상 web 바인딩 전용 객체/생성자 바인딩 권장 — setter 없는 record가 이에 부합). 단일 값 `@RequestParam` 대신 record로 통일하는 것은 위 공식 최소 요건을 넘어서는 이 프로젝트의 일관성 우선 컨벤션입니다.

## 컨트롤러 `@PathVariable` 식별자 명명 규칙 (`{id}`로 통일)

**한 컨트롤러 안에서 그 컨트롤러의 주(主) 리소스를 가리키는 `@PathVariable` 식별자는 경로상의 위치·핸들러 종류와 무관하게 bare `id`로 통일합니다.** 단건 CRUD(`GET/PUT/DELETE /coupons/v1/{id}`)든 그 리소스에 속한 중첩 하위 경로(`POST /coupons/v1/{id}/issues`)든, 결국 "쿠폰"을 가리키는 경로 변수는 모두 `id` 하나로 씁니다. 같은 대상을 어떤 메서드에서는 `id`, 어떤 메서드에서는 `{도메인}Id`로 **혼재하는 것을 금지**합니다.

- **왜 `{id}`인가**: `@RequestMapping("/api/coupons")`로 컨트롤러 전체가 이미 쿠폰 리소스에 스코프되어 있으므로, 경로 변수 `id`는 문맥상 "이 컨트롤러가 다루는 쿠폰의 id"임이 자명합니다. `couponId`처럼 도메인 접두어를 반복하면 클래스 스코프와 중복되어 장황하고, 오히려 한 파일 안에서 `id`/`couponId`가 섞이는 혼재를 부릅니다. 짧고 일관된 `id`가 REST 관례(리소스 컬렉션 `/coupons/{id}`)에도 부합합니다.
- **경계 타입은 그대로**: 타입은 [ID VO 경계 규칙](#id-vo식별자-값-객체-경계-규칙)대로 HTTP 경계에서 `Long`을 유지합니다(`@PathVariable Long id`). Service에서 `XxxId.of(id)`로 승격하는 흐름은 변경하지 않습니다.
- **다른 애그리거트를 참조하는 경우는 예외**: 어떤 컨트롤러가 **자기 주 리소스가 아닌 다른 도메인의 식별자**를 경로로 받는 경우(예: 회원 컨트롤러가 `/members/{memberId}/orders/{orderId}`처럼 두 종류 이상의 식별자를 동시에 받는 경우)에는 각 식별자를 `{도메인}Id`로 구분해 모호함을 없앱니다. 즉 "그 컨트롤러의 주 리소스 = `id`, 그 외 참조 식별자 = `{도메인}Id`"가 기준입니다.
- **적용 대상**: 신규 컨트롤러는 이 규칙을 따르고, 기존에 주 리소스를 `{도메인}Id`로 받던 컨트롤러는 **해당 파일을 수정할 때 함께 `id`로 전환**합니다. 이 규칙만을 위해 전 컨트롤러를 일괄 재작성하지는 않습니다.

reference 구현: `admin-api/coupon/CouponApiController` — 단건 CRUD·중첩 발급/현황 API 모두 주 리소스인 쿠폰을 `@PathVariable Long id`로 받음(`/v1/{id}`, `/v1/{id}/issues`).

## 컨트롤러 미사용 `@PathVariable` 경로 평탄화 규칙

**중첩 리소스 경로(`/v1/{id}/{하위리소스}/{하위id}`)의 부모 `@PathVariable`(`id`)이 핸들러·Service·도메인 계층 어디에서도 실제로 사용되지 않는다면(단순 전달조차 없이 완전히 죽은 파라미터), 그 경로를 부모 세그먼트 없이 평탄화합니다.** 하위 리소스의 식별자(`{하위id}`)가 전역 유니크 PK라 부모 없이도 단독으로 대상을 특정할 수 있는 경우가 이에 해당합니다. "경로가 리소스 계층을 반영해야 한다"는 REST 중첩 관례보다, **실제로 쓰이지 않는 경로 변수를 남겨 두지 않는 것**을 우선합니다 — 사용하지 않는 파라미터는 호출자에게 "이 값이 삭제 대상을 좁히는 데 쓰인다"는 잘못된 인상을 주고, 나중에 검증 로직이 있는 줄 착각하게 만들기 쉽습니다.

- **⚠️ 평탄화는 소유권 검증 면제가 아닙니다**: 경로에서 부모 `id`를 없앴다고 해서 소유권 검증까지 생략해도 된다는 뜻이 아닙니다. **대상 행에서 소유자를 역조회할 수 있으면 반드시 검증합니다.** 판단 순서는 (1) 대상 행을 `findById`로 읽어 `getShopId()` 같은 소유자 참조를 얻을 수 있는가 → 얻을 수 있으면 검증한다, (2) 정말로 역조회 수단이 없을 때만 생략하고 그 한계를 Javadoc에 남긴다. 실제 사고 사례: 배달가능지역 삭제(`DELETE /v1/delivery-areas/{deliveryAreaId}`)가 "shopId가 경로에 없다"는 이유로 검증을 생략했는데, `ShopDeliveryAreaRepository#findById`와 `ShopDeliveryArea#getShopId()`가 **둘 다 존재**해 역조회가 가능했습니다. 그 결과 아무 점주나 순번을 훑어 **남의 가게 배달가능지역을 삭제**할 수 있는 IDOR이 되었고(피해 가게는 배달 범위를 잃거나, 등록 건수가 0이 되면 주문 접수의 지역 검사 자체가 비활성화됨), 리뷰에서 발견해 `ShopDeliveryAreaCommandService#removeDeliveryArea(ceoId, deliveryAreaId)`로 교정했습니다. 기존에 검증을 생략한 하위 리소스 삭제 경로도 이 기준으로 재점검 대상입니다(`ShopClosedDayCommandService#deleteClosedDay` 등).
- **판별 기준**: 핸들러 메서드 본문에서 부모 `id`를 Service 호출 인자로 전달조차 하지 않는 경우(완전 미사용)에만 적용합니다. Service나 도메인 서비스에서 `id`를 소속 검증(예: "이 하위 리소스가 이 부모에 속하는지")에 사용한다면 평탄화 대상이 아니며 기존 중첩 경로를 그대로 유지합니다.
- **적용 방법**: `@DeleteMapping("/v1/{id}/{하위리소스}/{하위id}")` → `@DeleteMapping("/v1/{하위리소스}/{하위id}")`로 변경하고, 미사용 `@PathVariable Long id` 파라미터를 제거합니다. Service/core 시그니처가 이미 하위 식별자만 받고 있다면 추가 변경이 필요 없습니다.
- **타입·명명은 그대로**: 남는 하위 식별자는 [`@PathVariable` 식별자 명명 규칙](#컨트롤러-pathvariable-식별자-명명-규칙-id로-통일)에 따라 `Long` 타입을 유지하며, 이름은 기존 하위 식별자명(`winnerId` 등)을 그대로 씁니다.
- **적용 대상**: 같은 컨트롤러의 다른 CRUD/중첩 API(예: 생성·목록 조회)는 부모 `id`를 실제로 사용하므로 그대로 둡니다 — 평탄화는 미사용이 확인된 해당 핸들러 하나에만 적용하고, 컨트롤러 전체 경로 스타일을 바꾸지 않습니다.

reference 구현: `admin-api/event/EventApiController#deleteWinner` — `EventCommandService.deleteWinner(Long winnerId)`가 winnerId(전역 유니크 PK)만으로 조회·삭제하고 eventId 소속 검증이 없어, 경로를 `/v1/{id}/winners/{winnerId}`에서 `/v1/winners/{winnerId}`로 평탄화하고 미사용 `@PathVariable Long id`를 제거함. 같은 컨트롤러의 `createWinner`·`getWinners`는 `id`(eventId)를 실제로 사용하므로 `/v1/{id}/winners` 형태를 그대로 유지.

## Request/Response record `@Schema` 문서화 규칙

HTTP 경계에 노출되는 **모든 Request/Response record는 Swagger(springdoc-openapi) 스키마를 완전히 문서화**합니다. 타입 선언부에 `@Schema(description = ...)`를 붙이고, 모든 필드에 `@Schema(description = ..., example = ...)`를 붙입니다. 필드 설명·예시가 비어 있으면 Swagger UI에서 해당 필드의 용도를 추론할 수 없어 프론트엔드·QA가 API 문서만으로 연동하기 어려워지므로, 신규 Request/Response record는 이 문서화를 스키마의 일부로 간주하고 누락 없이 작성합니다.

- **타입 레벨**: record 선언 바로 위에 `@Schema(description = "…")`로 이 DTO가 무엇을 나타내는지 한 줄로 명시합니다.
- **필드 레벨**: 모든 필드에 `@Schema(description = ..., example = ...)`를 답니다. **컬렉션 필드(`List<...>`)나 중첩 record 필드는 example을 생략할 수 있으나 description은 필수**입니다 — 예시 값을 만들기 어렵거나 하위 record가 자체적으로 필드 설명을 가지므로 상위에서 example로 중복 표현할 필요가 없기 때문입니다.
- **Request record**: Bean Validation 어노테이션(`@NotBlank`/`@NotNull`/`@Min` 등)을 필드 위에 먼저 두고, **그 다음 줄에 `@Schema`** 를 둡니다. 필수 필드는 `requiredMode = Schema.RequiredMode.REQUIRED`를 명시해 Validation 제약과 Swagger 스키마상 필수 표시를 일치시킵니다.
- **enum 후보 필드**: 도메인 enum 경계 규칙에 따라 `String`/`List<String>`으로 받는 필드는 기존처럼 `allowableValues = {...}`를 유지하면서, 같은 `@Schema` 안에 `description`을 병기합니다(`allowableValues`만 있고 `description`이 없는 상태로 남기지 않습니다).

```java
@Schema(description = "공지사항 수정 요청")
public record NoticeUpdateRequest(
    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "제목", example = "서비스 점검 안내", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean visible
) {
}
```

reference 구현: `admin-api`의 `notice/request/NoticeUpdateRequest`(Bean Validation 다음 줄 `@Schema` + `requiredMode`), `web-api`의 `order/request/OrderCreateRequest`(선택 필드는 `requiredMode` 생략, 컬렉션 필드 `orderProducts`는 example 생략), `web-api`의 `reservation/response/ReservationDetailResponse`(응답 record 필드 전수 `description`+`example`), `admin-api`의 `banner/response/BannerListItemResponse`(타입 레벨 `@Schema(description = ...)` + 모든 필드 문서화).

## Response record 정적 팩토리 시그니처 줄바꿈 규칙

`*Response.java`의 정적 팩토리 메서드(`from(...)` / `of(...)` 등, `ofLogin`류 변형·`zero`·`success` 등 이름과 무관한 모든 정적 팩토리 포함)는 **시그니처 파라미터와 `return new Xxx(...)` 생성자 호출 인자 모두, 2개 이상이면 여는 괄호 다음 줄부터 한 줄에 하나씩 줄바꿈**하고, **0~1개면 한 줄로 유지**합니다. 처음에는 시그니처 파라미터만 이 규칙 대상이고 `return new` 본문은 적용 제외였으나, 전수 조사 결과 시그니처는 이미 통일됐음에도 `return new` 본문만 파일마다 한 줄에 몰리거나(예: 과거 `JwtResponse`) 여러 개씩 묶여(예: 과거 `BugReportDetailResponse`) 있어 같은 목적(응답 DTO 생성)의 코드가 반쪽만 통일된 상태였습니다. 시그니처와 동일한 임계값(2개 이상 줄바꿈, 0~1개 한 줄)을 `return new` 본문에도 적용해 완전히 통일합니다.

- **적용 대상**: 정적 팩토리 메서드의 **시그니처 파라미터**와 그 본문의 **`return new Xxx(...)` / `return new Xxx<>(...)` 생성자 호출 인자** 둘 다.
- **적용 제외**: record 컴포넌트 선언부(생성자 파라미터 목록), `@Schema` 등은 이 규칙과 무관하며 기존 형태를 그대로 둡니다. 제네릭 `<>`(예: `new ApiResponse<>(`)는 그대로 보존합니다.

```java
// Before (파라미터 2개 이상을 한 줄에, return new도 한 줄)
public static JwtResponse of(String accessToken, String refreshToken, String tokenType) {
    return new JwtResponse(accessToken, refreshToken, tokenType);
}

// After
public static JwtResponse of(
    String accessToken,
    String refreshToken,
    String tokenType
) {
    return new JwtResponse(
        accessToken,
        refreshToken,
        tokenType
    );
}

// 파라미터 0~1개는 한 줄 유지 (그대로)
public static EmailVerifyTokenResponse from(String emailVerifyToken) {
    return new EmailVerifyTokenResponse(emailVerifyToken);
}
```

자동 강제 도구(spotless 등)는 도입하지 않으며, 신규 작성·기존 파일 수정 시 이 규칙을 수동으로 따릅니다.

reference 구현: `admin-api`의 `auth/response/JwtResponse`, `coupon/response/CouponDetailResponse`(파라미터 15개, `return new`도 한 줄에 하나씩 — 시그니처·본문 통일의 기준 예시), `event/response/EventWinnerResponse`(파라미터 6개), `bug/response/BugReportDetailResponse`(과거 `return new` 인자가 여러 개씩 묶여 있던 것을 전환), `common/ApiResponse`(제네릭 `ApiResponse<>`도 동일 적용), `web-api`의 `notice/response/NoticeListItemResponse`(이미 줄바꿈된 다수파 예시), `member/response/MyReviewListItemResponse`(파라미터 2개로 줄바꿈 전환).

## 코딩 스타일 (import 순서)

Spring Framework가 자기 코드베이스에 강제하는 공식 컨벤션(`spring-javaformat`의 `SpringImportOrderCheck`)과 동일한 규칙을 따릅니다. 모든 Java 파일의 import는 아래 4개 그룹 순서로 배치합니다. **그룹 사이에는 빈 줄 1개**, 그룹 내부는 **알파벳(ASCII) 오름차순** 정렬, 그룹 내부에는 빈 줄을 넣지 않습니다.

1. 자바 표준 라이브러리 — `java.*`
2. `javax.*` (예: `javax.crypto.*`)
3. 그 외 전부 (자사 제외 — `jakarta.*` 포함) — `com.querydsl.*`, `io.swagger.*`, `jakarta.persistence.*`, `jakarta.validation.*`, `org.slf4j.*`, `org.springframework.*`, `software.amazon.*` 등을 **한 그룹으로 알파벳 혼합 정렬**
4. 자사 코드 (projectRootPackage) — `com.tastyhouse.*`
5. static import — 그룹 구분 없이 맨 아래 한 블록 (QueryDSL Q타입, `assertThat` 등), 내부는 알파벳 순

```java
package com.tastyhouse.infrastructure.order.query;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
```

위 예시처럼 `jakarta`는 별도 그룹이 아니라 `com.querydsl` → `io.swagger` → `jakarta` → `org.slf4j` → `org.springframework` 순으로 서드파티 그룹 안에서 알파벳 정렬됩니다.

**자사 코드(`com.tastyhouse.*`)만 맨 뒤로 분리하는 이유**: DDD 레이어드 아키텍처에서 "내 도메인 코드"와 "외부 프레임워크 의존"을 한눈에 구분하기 위함입니다. 특히 `domain-module`은 프레임워크 의존 자체가 금지되어 있고(production 의존 0개), api 모듈은 QueryDSL·infra `..persistence..` 의존이 금지되어 있으므로, import 그룹 분리가 레이어 위반을 리뷰 시점에 즉시 드러내는 역할을 합니다.

### 자사 코드 그룹 내부 계층 정렬 (헥사고날 안→밖: domain이 위)

**이 규칙은 공식 표준이 아니라 프로젝트 커스텀 컨벤션입니다.** Spring `spring-javaformat`(`SpringImportOrderCheck`), Google Java Style, Checkstyle `ImportOrder` 등 어떤 공식 컨벤션도 "자사 패키지 그룹 **내부**를 계층 순으로 정렬"하지 않습니다(공식은 그룹 내부 순수 알파벳순). 이 프로젝트는 헥사고날/클린 아키텍처의 **"의존성은 항상 안쪽(domain)을 향한다"는 안정 의존성 원칙**을 근거로, 위 그룹4(`com.tastyhouse.*`) 내부의 정렬 순서를 **`(계층 순위, 알파벳)` 복합 키**로 세분합니다. 가장 안정적·핵심인 domain을 맨 위에, 가장 바깥·휘발적인 presentation을 맨 아래에 둡니다.

| 계층 순위 | 계층 | 매칭 패키지 세그먼트 |
|---|---|---|
| 1 | **domain** (가장 안쪽·핵심) | `com.tastyhouse.domain.<ctx>.model` / `.vo` / `.event` / `.repository`(write 포트) / `.service`(순수 POJO 도메인 서비스) / `.port`(출력 포트) |
| 2 | **application** | `com.tastyhouse.{web\|admin\|ceo\|batch}application.*` — 인바운드 포트(`<ctx>.port.in`)·CQRS 서비스(`<ctx>.service`)·**표현 계약(`<ctx>.response`)은 여기 없다 — 3개 앱 전부 api 모듈로 이동했다**(admin 챕터 06 · ceo 챕터 09 · web 챕터 10) |
| 3 | **infrastructure** | `com.tastyhouse.infrastructure.<ctx>.query`(query DAO·Result DTO·SearchCondition) / `.persistence`(`.converter`) / `.listener` |
| 4 | **external / shared** (어댑터·횡단 공용) | `com.tastyhouse.external.*`, `com.tastyhouse.security.*`, `com.tastyhouse.logging.*`, `com.tastyhouse.domain.shared.*`, `com.tastyhouse.domain.exception.*` |
| 5 | **presentation** (가장 바깥) | `com.tastyhouse.webapi.*`, `com.tastyhouse.adminapi.*`, `com.tastyhouse.ceoapi.*`, `com.tastyhouse.batch.*` — 내부는 아래 "presentation 내부 서브정렬"로 5-a → 5-b 세분 |

- **2순위 판별은 이제 패키지로 합니다 (챕터 06 개정 — 과거 "클래스명 접미어로 판별"의 대체)**: 모듈 재편 전에는 application 서비스가 api 모듈 안에 있어 패키지 접두어가 presentation과 같았고(`com.tastyhouse.webapi..`), 그래서 `*CommandService`/`*QueryService` **접미어**로 계층을 판별할 수밖에 없었습니다. 이제 application 계층이 `com.tastyhouse.*application..`이라는 **자기 최상위 패키지**를 가지므로, 접두어만 보고 2순위와 5순위를 가릅니다.
  - **이 전환의 실익**: 접미어 판별은 `*Executor`·`*Validator`·`*Runner`처럼 이름이 다른 협력 빈을 놓쳤고, 그때마다 "이것도 사실상 application"이라는 사람 판단이 필요했습니다. 패키지 판별에는 그 예외가 없습니다 — `com.tastyhouse.ceoapplication.shop.service.ShopOwnershipValidator`는 이름과 무관하게 2순위입니다.
  - `webapi`/`adminapi`/`ceoapi`(5순위)와 `webapplication`/`adminapplication`/`ceoapplication`(2순위)은 **접두어가 겹치므로**(`com.tastyhouse.webap...`) 정렬 시 세그먼트 전체를 비교합니다.
- **api 모듈은 3순위(infrastructure)에 등장하지 않습니다 (개정)** — `com.tastyhouse.infrastructure..`(과거 허용되던 `..query..` 포함) 의존이 전면 금지되어 있으므로(아래 [api 모듈 QueryDSL·infra 전면 금지 규칙](#api-모듈-querydslinfra-전면-금지-규칙-archunit-강제--챕터-04로-완료)), api 모듈 파일의 자사 import 3순위는 이제 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort`·Result·SearchCondition(`com.tastyhouse.application..port.out..`)이 대신합니다.

- **같은 계층 순위 내부는 기존대로 알파벳(ASCII) 오름차순**으로 정렬합니다.
- **그룹4 내부에는 여전히 빈 줄을 넣지 않습니다** — 계층 사이도 빈 줄로 구분하지 않고 순서만 바꿉니다(상위 그룹 간 빈 줄 규칙은 그대로 유지).
- static import는 이 규칙과 무관하게 맨 아래 블록 그대로입니다.

**Before (단일 알파벳순 — 계층 혼재)**:
```java
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.service.OrderPlacementService;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.infrastructure.order.query.OrderDetailResult;
import com.tastyhouse.infrastructure.order.query.OrderQueryDao;
import com.tastyhouse.webapi.member.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.request.OrderProductRequest;
import com.tastyhouse.webapi.order.response.OrderDetailResponse;
```

**After (domain → application → infrastructure → external/shared → presentation, 계층 내부는 알파벳순)**:
```java
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.service.OrderPlacementService;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.infrastructure.order.query.OrderDetailResult;
import com.tastyhouse.infrastructure.order.query.OrderQueryDao;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.request.OrderProductRequest;
import com.tastyhouse.webapi.order.response.OrderDetailResponse;
```

**domain-module 전용 파일 예시** (presentation·external·infrastructure 없이 domain → shared만 존재):
```java
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
```

**`infrastructure:persistence` query DAO 파일 예시** (domain(shared) → infrastructure 순, Q타입은 static import):
```java
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.order.query.OrderListItemResult;

import static com.tastyhouse.infrastructure.order.persistence.QOrderJpaEntity.orderJpaEntity;
```

#### presentation(5순위) 내부 서브정렬 (공용 인프라 위 → 도메인 전용 아래)

presentation 계층(`com.tastyhouse.webapi.*` / `adminapi.*` / `ceoapi.*`)은 한 계층 안에 성격이 다른 두 종류가 섞여 있습니다. 이를 **위 계층 정렬과 같은 안정 의존성 원칙**으로 한 단계 더 세분합니다. 여러 도메인이 공유하는 **공용 인프라**(비도메인 프레임워크 유틸)는 특정 도메인 전용 어댑터 DTO보다 안정적이므로 **위(5-a)**, 도메인 전용은 **아래(5-b)** 에 둡니다. 이는 4순위에서 `com.tastyhouse.domain.shared.*`를 presentation보다 위에 두는 방향("공용은 도메인 전용보다 위")과 대칭입니다.

| 서브순위 | 대상 | 매칭 패키지 세그먼트 |
|---|---|---|
| **5-a** (공용 인프라, 위) | 여러 도메인이 공유하는 비도메인 프레임워크 유틸 | `com.tastyhouse.{webapi\|adminapi\|ceoapi}.common.*` · `.config.*` · `.security.*` · `.ratelimit.*` · `.exception.*` |
| **5-b** (도메인 전용, 아래) | 특정 도메인 전용 컨트롤러 협력 타입 | `com.tastyhouse.{webapi\|adminapi\|ceoapi}.<도메인>.request.*` · `.response.*` (및 도메인 하위 기타) |

- **판별 기준**: "여러 도메인이 공유하는 비도메인(=`request`/`response` 같은 도메인 DTO가 아닌) 프레임워크·횡단 유틸"이면 5-a입니다. 위 세그먼트 목록에 없는 새 공용 패키지가 생겨도 이 기준으로 5-a에 편입합니다. 특정 도메인 패키지(`banner`·`notice`·`order` 등) 하위는 5-b입니다.
- **같은 서브순위 내부는 알파벳(ASCII) 오름차순** — 5-a 내부(`common` → `config` → `exception` → `ratelimit` → `security`)도, 5-b 내부(도메인 이름순)도 알파벳순입니다.
- **5-a/5-b 사이에도 빈 줄을 넣지 않습니다** — 그룹4 내부 무(無)빈줄 규칙 그대로, 순서만 구분합니다.

**Before (`common`이 도메인 request/response 아래로 빠짐 — 알파벳순의 우연):**
```java
import com.tastyhouse.adminapi.banner.request.BannerCreateRequest;
import com.tastyhouse.adminapi.banner.response.BannerDetailResponse;
import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.common.PageRequest;
```

**After (공용 인프라 5-a 먼저 → 도메인 전용 5-b):**
```java
import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.common.PageRequest;
import com.tastyhouse.adminapi.banner.request.BannerCreateRequest;
import com.tastyhouse.adminapi.banner.response.BannerDetailResponse;
```

> 이 서브규칙 도입 전에도 `common`이 도메인보다 위에 오던 파일(예: `NoticeApiController` — `common` < `notice`)은 알파벳순의 우연으로 이미 부합 상태였고, 규칙화로 전 도메인에서 위치가 일관됩니다.

**신규 작성·기존 파일 수정 시 수동 적용**하며, 기존 코드를 이 규칙(및 위 5-a/5-b 서브규칙)만을 위해 일괄 재정렬하지 않습니다.

자동 강제 도구(spotless 등)는 도입하지 않으며, 신규/수정 코드 작성 시 이 규칙을 수동으로 따릅니다.

**참고 자료 (Spring 공식 소스)**:
- Spring Java Format `SpringImportOrderCheck` 구현: https://github.com/spring-io/spring-javaformat/blob/main/spring-javaformat/spring-javaformat-checkstyle/src/main/java/io/spring/javaformat/checkstyle/check/SpringImportOrderCheck.java
- Spring Java Format checkstyle 설정: https://github.com/spring-io/spring-javaformat/blob/main/spring-javaformat/spring-javaformat-checkstyle/src/main/resources/io/spring/javaformat/checkstyle/spring-checkstyle.xml
- Checkstyle `ImportOrder` 규칙 문서: https://checkstyle.sourceforge.io/checks/imports/importorder.html

### 미사용 import 제거 규칙 (파일을 건드리면 그 파일의 미사용 import까지 정리)

**AI가 Java 파일을 수정할 때 리네이밍·삭제·리팩터링으로 더 이상 쓰이지 않게 된 import를 남겨두는 사고가 반복됩니다.** 메서드를 지우거나 타입을 바꿔치기하면서 그 메서드/타입이 쓰던 import를 정리하지 않고 넘어가는 경우가 대부분입니다. import 정리는 별도 작업으로 미루지 말고, **그 파일을 수정하는 바로 그 편집의 일부**로 취급합니다.

- **자신이 손댄 파일은 편집 직후 스스로 확인합니다**: 코드 변경으로 특정 타입·메서드·상수 참조가 사라졌다면, 그 참조에 쓰였던 import 문도 같은 편집에서 제거합니다. "나중에 한 번에 정리" 방식으로 미루지 않습니다.
- **완료 보고 전 최종 확인 수단**: IDE 진단(`mcp__ide__getDiagnostics`가 연결돼 있으면 우선 사용) 또는 `./gradlew :{모듈}:compileJava`의 `-Xlint:all` 경고, 혹은 변경된 파일에서 `grep`으로 import된 심볼이 본문에 실제로 등장하는지 대조하는 방법으로 미사용 import 유무를 점검합니다. 여러 파일을 수정한 작업에서는 **변경된 파일 전부**를 대상으로 이 확인을 수행하고, 일부만 확인하고 넘어가지 않습니다.
- **미사용 import는 컴파일을 막지 않아 놓치기 쉽습니다**: `./gradlew build`는 미사용 import만으로는 실패하지 않으므로(경고 수준), 빌드 성공을 "정리 완료"의 증거로 삼지 않습니다.
- **적용 범위**: 이번 작업으로 새로 만든 파일뿐 아니라, **기존 로직을 고치며 함께 수정한 기존 파일**도 동일하게 적용합니다. 작업과 무관한 파일까지 찾아가 정리하는 전수 청소는 하지 않습니다 — 손댄 파일에 한정합니다.

## QueryDSL 동적 where 조건 조립 규칙 (`BooleanBuilder` 대신 `BooleanExpression` varargs 헬퍼)

`infrastructure:persistence`의 `*RepositoryImpl.java`·`*QueryDao.java`에서 **동적 검색(필터가 null이면 조건 무시)을 하는 where 조건은 `BooleanBuilder` + `if`문이 아니라, `private BooleanExpression xxxEq(arg)` 헬퍼(arg가 null이면 null 반환) + `.where(가변인자)`로 조립합니다.** QueryDSL이 `.where(...)`에 전달된 null 인자를 자동으로 무시하는 것을 이용한 동적 쿼리 관용구입니다.

- **왜 통일하는가**: 전수 조사 결과 동적 검색 리포지토리 11개 중 9개가 이미 `BooleanExpression` varargs 헬퍼 패턴이었고, `OrderRepositoryImpl`·`ShopRepositoryImpl` 2개만 `BooleanBuilder` + `if`문(명령형·장황)을 쓰고 있었습니다. 같은 목적(동적 where)을 서로 다른 스타일로 구현하면 파일마다 읽는 방식이 달라지므로, 다수파 패턴으로 통일합니다.
- **정적 고정 조건**(필터링 대상이 아닌 조건, 예: `deleted.isFalse()`)은 헬퍼 없이 `.where(...)`에 인라인으로 둡니다.
- **`BooleanBuilder` 예외 허용 범위**: OR 조합·복잡한 그룹핑처럼 varargs `.where(...)`(AND만 지원)로 표현 불가능한 경우에만 예외적으로 쓰고, 이유를 주석으로 남깁니다.
- **선행 데이터 계산은 대상 아님**: 서브쿼리로 ID 집합을 먼저 계산해 교집합하는 등 where 조립이 아닌 로직은 이 규칙과 무관합니다. 계산된 집합을 최종 where에 넣을 때만 `xxxIn(Set<Long>)` 헬퍼를 씁니다.

```java
// 권장 — BooleanExpression 헬퍼 + varargs where
.where(
    noticeJpaEntity.deleted.isFalse(),  // 정적 고정 조건은 인라인
    titleContains(condition.title()),   // 동적 조건은 헬퍼로
    visibleEq(condition.visible())
)
...
private BooleanExpression titleContains(String title) {
    return StringUtils.hasText(title) ? noticeJpaEntity.title.containsIgnoreCase(title) : null;
}
```

```java
// 지양 — BooleanBuilder + if
BooleanBuilder where = new BooleanBuilder();
if (condition.title() != null) { where.and(noticeJpaEntity.title.containsIgnoreCase(condition.title())); }
```

reference 구현: `infrastructure:persistence`의 `notice/query/NoticeQueryDao`(`titleContains`/`contentContains`/`visibleEq` 헬퍼 + varargs `.where(...)`), `order/query/OrderQueryDao`, `shop/query/ShopQueryDao`·`ShopSearchQueryDao`(과거 `ShopRepositoryImpl`의 `BooleanBuilder`였다가 통일). 상세 예시는 `infrastructure/persistence/AGENTS.md` 참고.

## 도메인 모델 / JPA 엔티티 분리 규칙 (선별 적용, persistence는 `infrastructure-module`로)

> **모듈명 주석 (챕터 05)**: 이 제목의 `infrastructure-module`은 현재 **`infrastructure:persistence`**(경로 `infrastructure/persistence/`)다. 제목을 그대로 두는 이유는 다른 문서들이 이 앵커(`#도메인-모델--jpa-엔티티-분리-규칙-선별-적용-persistence는-infrastructure-module로`)를 링크하고 있기 때문이다. 자바 패키지 `com.tastyhouse.infrastructure..`는 불변이므로 본문의 패키지 경로는 그대로 유효하다.

**도메인 모델은 순수 POJO로 두고 JPA 엔티티와 분리하며, JPA 어댑터는 `infrastructure:persistence`이 소유한다.** 과거에는 "상태전이·불변식이 실재하는 도메인만 선별 전환하고 단순 CRUD 도메인은 현행(도메인 모델 = `@Entity`, persistence가 core-module 내부) 유지"가 허용되는 점진 전환(Strangler Fig, `md/CLEAN-ARCHITECTURE.md`)이었으나, **전 도메인 전환이 완료되어 지금은 선별이 아니라 전면 적용**이다. 클래스 수준(POJO화)과 모듈 수준(어댑터 분리)을 함께 적용해 "도메인은 JPA 구현을 모른다"를 빌드 그래프로 강제한다.

- **모듈 경계 (개정됨 — 읽기 경로 포트화로 재개정)**: `web-api`/`admin-api`/`ceo-api`/`batch-module` → `domain-module`(도메인 POJO + write 포트 + 순수 POJO 도메인 서비스) `implementation`, `infrastructure:persistence`(JPA 어댑터 + query DAO 구현체) `implementation`. 읽기 계약(`{Ctx}QueryPort` + Result + SearchCondition)은 별도 모듈이 아니라 소비 앱의 `{앱}-application`(단독분)과 `domain-module`(공유분)이 소유하므로 **추가 의존 선언이 없다**. `infrastructure:persistence` → `domain-module` `implementation` + `{web,admin,ceo,batch}-application` `implementation`(DAO가 각 앱 소유 포트를 구현). api 모듈은 이제 **`com.tastyhouse.infrastructure..`를 전혀 import하지 않는다** — `{도메인}QueryService`는 DAO 구현체가 아니라 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스를 주입한다. `..persistence..` 직접 의존과 QueryDSL 의존은 처음부터 ArchUnit으로 금지돼 있었고, 챕터 04로 `com.tastyhouse.infrastructure..` 전체(과거 허용되던 `..query..` 포함)가 금지 대상에 추가됐다(아래 [api 모듈 QueryDSL·infra 전면 금지 규칙](#api-모듈-querydslinfra-전면-금지-규칙-archunit-강제--챕터-04로-완료)).
- **domain-module은 JPA뿐 아니라 QueryDSL·spring-tx까지 없는 완전 프레임워크-프리 모듈이다**: production 의존이 **하나도 없으며**(Lombok까지 제거되어 접근자·생성자를 수기로 작성한다), `@Entity`/`JpaRepository`/`RepositoryImpl`/`AttributeConverter`/`BaseEntity`/`QueryDslConfig`/`EntityManager`는 전부 `infrastructure:persistence`에 있다. `domain-module/build.gradle`에서 `spring-boot-starter-data-jpa`·`mysql-connector-j`·`querydsl-jpa`뿐 아니라 **`querydsl-core`·`querydsl-apt`(및 querydsl sourceSets/generated 블록)·`spring-tx`·`spring-orm`도 제거**됐다 — `@QueryProjection` Result DTO와 query DAO가 전부 infrastructure `<ctx>/query/` 소유가 되어 `com.querydsl.*`을 컴파일할 필요가 없어졌고, 도메인 서비스는 전부 순수 POJO(`@Service`/`@Transactional` 미사용)라 스프링 트랜잭션 API도 필요 없어졌다. 낙관적 락 충돌은 프레임워크-프리 `com.tastyhouse.domain.shared.exception.OptimisticLockConflictException`으로 표현하고, 스프링 예외(`ObjectOptimisticLockingFailureException`) 번역은 `infrastructure:persistence`의 `RepositoryImpl`이 담당한다.
  - **이 순수성은 빌드로 강제된다 (컴파일 게이트)**: 루트 `build.gradle`의 spring 주입 블록은 `configure(subprojects.findAll { it.name != 'domain-module' })`로 **domain-module을 제외**하고, domain-module은 바로 아래 `project(':domain-module')` 블록에서 `java` + `io.spring.dependency-management`(버전 고정만 하고 의존은 추가하지 않음)만 적용받는다. 그 결과 domain-module의 컴파일 클래스패스에 `org.springframework.*`가 아예 없어 **`import org.springframework.stereotype.Service;` 한 줄이 컴파일 에러**가 된다 — 순수성이 리뷰 규율이 아니라 빌드 게이트로 보장된다. `org.springframework.boot` 플러그인을 적용하지 않으므로 이 모듈에는 `bootJar` 태스크가 없고, 따라서 `bootJar { enabled = false }`를 쓰면 스크립트 평가 에러가 난다(다른 `java-library` 모듈과 다른 점).
  - **테스트도 spring-free**: `spring-boot-starter-test` 통째 대신 실제로 쓰는 `junit-jupiter`·`assertj-core`·`archunit-junit5`만 선언한다. domain-module 테스트는 전부 순수 단위 테스트(`@SpringBootTest`류 0건)라 스프링 테스트 컨텍스트가 필요 없고, starter를 두면 테스트 클래스패스로 spring이 되돌아와 순수성 검증이 무뎌진다. **domain-module에 `@SpringBootTest`를 추가해야 할 상황이면 그 테스트가 이 모듈에 있어야 하는지를 먼저 의심한다**(대개 `infrastructure:persistence` 소속이다).
  - **Lombok은 전 모듈에서 제거됐다**: 과거 이 모듈은 Lombok을 `compileOnly` + `annotationProcessor`로(다른 8개 모듈은 `implementation`으로 — lombok jar가 런타임 산출물·전이 의존에 새는 형태로) 선언하고 있었으나, 지금은 어느 모듈에도 lombok 의존이 없고 루트 `lombok.config`도 삭제됐다. getter·생성자·`Logger` 필드는 전부 수기로 작성한다.
  - **ArchUnit 규칙(P2)과 상보적이다**: 컴파일 게이트는 "클래스패스에 없어서 못 쓴다"를, ArchUnit은 "클래스패스에 있어도 쓰면 안 된다"(예: 테스트 의존으로 들어온 타입, 모듈 간 의존 방향)를 각각 막는다. 둘 다 있어야 완전하다.
- **순수 도메인 모델**(domain-module `<ctx>/model/`): `jakarta`/`@Entity` 무의존. 신규 생성 `of(...)`와 DB 재구성 전용 `reconstitute(id, ..., createdAt, updatedAt)` 두 정적 팩토리만 공개하고, `reconstitute`는 인프라만 호출(불변식 우회 방지, Javadoc 명시). `id`는 미영속이면 null. getter는 수기로 작성한다(Lombok 제거 — `boolean`은 `isXxx()`, 그 외는 `getXxx()`). **필드 가시성(mutable vs final)**: JPA `@Entity`는 프록시·리플렉션이 필드를 세팅해야 해 `final`을 못 붙이지만, 순수 도메인 모델은 그 제약이 없다. 따라서 **생성자(팩토리) 이후 재대입되지 않는 필드는 반드시 `final`로 선언**한다 — `id`뿐 아니라 `username`·`status` 같은 상태 필드도 상태전이 메서드로 실제 재대입되지 않으면 `final`로 둔다. 이는 (1) POJO화로 얻은 불변성 표현력을 실제로 살리고, (2) "이 필드는 생성 시점에 확정되고 이후 안 바뀐다"를 컴파일러가 강제하며, (3) IntelliJ의 `Field 'xxx' may be 'final'` 경고를 원천 차단한다. 상태전이가 있는 도메인이라도 전이 대상이 아닌 필드는 개별적으로 `final`을 적용한다(전이되는 필드만 non-final). reference: `admin` 도메인 `Admin`(update 경로가 없어 `id`+`username`·`password`·`name`·`role`·`status` 전 필드 `final`).
- **JPA 엔티티**(`infrastructure:persistence`, `com.tastyhouse.infrastructure.<도메인>.persistence.XxxJpaEntity`): DB 매핑 전용, 행위 없음. `BaseEntity`(`@MappedSuperclass`, `com.tastyhouse.infrastructure.shared.persistence.BaseEntity`) 상속. 신규 생성 `create(...)`·update 복사용 `applyChanges(...)`만 둔다. **DDL·`ddl-auto=validate` 무변경**(테이블/컬럼 동일 매핑).
- **공유/비공유 `@Embeddable` VO 경계 (개정됨)**: `PhoneNumber`·`ProductDiscountInfo`·`VerificationCode` 등 `@Embedded`로 쓰이는 VO는 여러 애그리거트가 공유하든 하지 않든 **domain-module에서 순수 POJO(어노테이션 無)로 둔다.** `@Embeddable`/`@Column`/`jakarta.persistence` import는 VO에서 완전히 제거하고, 컬럼 매핑은 이를 사용하는 각 `infrastructure:persistence` JpaEntity 쪽에서 `@Embedded` + `@AttributeOverride`(복수 필드는 `@AttributeOverrides`)로 재선언한다. (과거 "공유 VO는 core에 어노테이션을 유지한다"는 규칙이었으나, domain-module을 완전 프레임워크-프리로 만들며 역전되었다.) reference: `MemberJpaEntity`/`EventWinnerJpaEntity`/`SmsVerificationJpaEntity`의 `PhoneNumber` `@AttributeOverride(name="value", column=@Column(name="phone_number", nullable=false, length=11))`, `ProductJpaEntity`의 `ProductDiscountInfo` `@AttributeOverrides`.
- **`@Embedded` 대상 순수 POJO VO는 반드시 Java `record`로 선언한다 (검증은 compact constructor)**: Hibernate 6의 `EmbeddableInstantiatorPojoStandard`는 `@Embedded` 값 객체를 인스턴스화할 때 (1) no-arg 생성자, (2) `@org.hibernate.annotations.Instantiator` 지정 생성자, (3) record의 canonical 생성자 중 하나를 요구한다. 일반 `class`로 두고 검증 로직이 든 단일/복수 인자 생성자만 제공하면 셋 다 해당이 없어 런타임에 `org.hibernate.InstantiationException: Unable to locate constructor for embeddable`이 발생한다(실제 장애 선례: `PhoneNumber`가 순수 POJO class로 전환되며 no-arg 생성자가 없어 카카오 로그인의 `MemberRepositoryImpl.findById`에서 이 예외로 500 발생). `@Instantiator`는 `org.hibernate.annotations` import가 필요해 domain-module을 완전 프레임워크-프리로 두는 이 프로젝트 규칙과 상충하므로 채택하지 않는다. 대신 JPA 3.1·Hibernate 6.0부터 공식 지원되는 **record를 canonical 생성자로 인스턴스화**하는 방식을 쓴다 — record는 벤더 의존 없이 이 요건을 만족하는 유일한 선택지다. VO 필드는 record 컴포넌트로 선언하고, 값 검증은 compact constructor(`public XxxVo { ... 검증 ... }`)에 둔다. `@AttributeOverride(name = "...")`의 `name`은 record 컴포넌트명과 정확히 일치해야 한다(예: `PhoneNumber(String value)` → `@AttributeOverride(name = "value", ...)`). 접근자·`equals`/`hashCode`는 record가 자동 생성하므로 따로 작성하지 않고, 접근자는 `getValue()`가 아닌 `value()`(record accessor)로 호출부를 통일한다(당시엔 lombok `@Getter`/`@EqualsAndHashCode`를 떼는 작업이었다).
  - **`toString()` 오버라이드를 남기지 않는다**: 이 프로젝트는 nullability 애노테이션(`@NotNull`/`@NonNull` 등)을 코드에 전혀 쓰지 않으므로, `Object.toString()`에 JetBrains 외부 애노테이션으로 걸린 `@NotNull` 계약을 애노테이션 없는 오버라이드로 덮으면 IntelliJ가 `Not annotated method overrides method annotated with @NotNull` 경고를 낸다(실제 선례: `PhoneNumber`를 record로 전환하며 값만 반환하던 `toString()`을 남겨 이 경고 발생). 경고를 없애려 `@NotNull`을 붙여 새 의존성(`org.jetbrains:annotations`)을 들이지 말고, **오버라이드 자체를 제거**한다 — record 기본 `toString()`(`Xxx[value=...]`)으로 충분하며, 값 추출은 아래대로 accessor로 한다.
  - **값 추출은 반드시 accessor(`value()`)로 한다 — `toString()`으로 값을 뽑던 지점도 전환**: 위 "`getValue()` → `value()`"는 기존 lombok getter만이 아니라, **class 시절 값만 반환하던 `toString()`에 의존해 값을 뽑던 호출부**(예: `getPhoneNumber().toString()`)에도 적용한다. record 기본 `toString()`은 값이 아니라 `Xxx[value=...]`를 반환하므로 그대로 두면 컴파일은 되지만 결과 문자열이 바뀌는 조용한 버그가 된다(실제 선례: `admin-api`의 `MemberService#getMember`가 `member.getPhoneNumber().toString()`으로 응답 DTO에 번호를 넣고 있었음 → `.value()`로 교정). VO를 record로 전환할 때 `.getValue()`와 `.toString()` 두 패턴을 모두 grep해 `.value()`로 통일한다.
  - **컴포넌트 선언 순서는 반드시 이름 알파벳 오름차순으로 한다 (다중 컴포넌트 VO 필수)**: Hibernate 6의 `Component#sortProperties()`는 embeddable 프로퍼티를 이름순으로 정렬하며, `isSimpleRecord()`(= 정렬 결과가 record 컴포넌트 순서와 일치)일 때만 정렬을 건너뛴다. 선언 순서가 알파벳순이 아니면 `ComponentType#deepCopy`가 **정렬된 순서**로 읽은 값 배열을 record canonical 생성자에 **선언 순서**대로 위치 기반 전달하므로 값이 엉뚱한 컴포넌트로 들어간다. 결과는 둘 중 하나이며 **후자가 더 위험**하다: (1) 타입이 다르면 런타임에 `Could not instantiate entity ... argument type mismatch`(`Cannot cast java.lang.String to java.lang.Integer` 등)로 500 — 실제 장애 선례: `OrderDeliveryDestination`의 선언 순서가 `roadAddress, lotAddress, detailAddress, adminDongId, latitude, longitude, distanceMeters`라 정렬 후 `lotAddress`(String)가 `distanceMeters`(Integer) 자리에 들어가 주문 생성(`POST /api/orders/v1`)이 전부 실패했다. (2) **타입이 같으면 예외 없이 값만 조용히 뒤바뀐다**(도로명↔지번 주소가 서로 바뀌어 저장되는 식). `@AttributeOverride(name = ...)`는 컴포넌트명으로 매칭되므로 순서를 바꿔도 **컬럼 매핑·DDL은 영향받지 않는다** — 다만 정적 팩토리의 `new` 호출 인자 순서는 반드시 함께 맞춘다(위치 기반이라 어긋나도 컴파일된다). 컴포넌트를 추가할 때도 알파벳 위치에 삽입한다. 이 규약은 `infrastructure:persistence`의 가드 테스트 `EmbeddedRecordComponentOrderTest`가 `@Entity`를 클래스패스 스캔해 전 대상을 자동 검증하므로 대상 목록을 수동 관리하지 않는다(1필드 VO는 순서 문제가 없어 자동 통과).
  reference 구현: `PhoneNumber`(1필드, `shared/vo/`, `toString()` 오버라이드 제거 + `admin-api/MemberService`의 `.toString()`→`.value()` 교정)·`VerificationCode`(1필드, `shared/vo/` — `mail`·`sms` 두 도메인이 공유하므로 승격, 정적 팩토리 `of`/`generate` 유지)·`ProductDiscountInfo`(2필드, `product/vo/`, 이미 `discountPrice()`/`discountRate()` record식 접근자를 쓰고 있어 전환 시 호출부 무변경 — `discountPrice` < `discountRate`로 우연히 알파벳순이라 순서 문제가 드러나지 않았다)·`OrderDeliveryDestination`(7필드 혼합 타입, `order/vo/` — 알파벳순 규약이 실제 장애로 확립된 사례).
- **매퍼**(`XxxMapper`, package-private): `toDomain`(재구성)·`toEntity`(신규)·`applyChanges`(managed 엔티티 필드 복사). 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
- **저장 시맨틱 — load-copy-save (merge 금지)**: `RepositoryImpl.save`는 id null이면 insert, id 있으면 managed 엔티티를 PK로 조회 후 `applyChanges` 복사(동일 트랜잭션 1차 캐시 히트 — 추가 쿼리 없음). detached `save()`(merge)는 `@CreatedDate(updatable=false)` 감사 필드 파손·전 필드 UPDATE 문제로 금지.
- **명시적 save 규칙 (더티 체킹 상실 보완)**: 도메인 변경 후 **반드시 `repository.save(domain)`를 호출**한다(`@Entity`처럼 자동 flush되지 않음). 누락 시 변경이 조용히 유실된다. 호출 책임은 트랜잭션을 여는 쪽에 있으므로, 변경을 수행한 지점이 api 모듈의 `{도메인}CommandService`든 domain-module의 순수 POJO 도메인 서비스(`<ctx>/service/`)든 그 안에서 write 포트의 `save`를 명시적으로 부른다.
- **Q타입 생성 위치 (개정됨 — 읽기 경로 포트화로 재개정)**: `QXxxJpaEntity`는 여전히 `infrastructure:persistence`에서 생성된다. **`QXxxResult`(Result DTO의 Q타입)는 더 이상 생성되지 않는다** — 챕터 04(읽기 경로 포트화)로 Result record가 QueryDSL을 모르는 `application-common-module`로 이관되며 `@QueryProjection`을 뗐고, DAO는 `Projections.constructor(XxxResult.class, ...)`로 조립한다(리포 전체 `@QueryProjection` 선언 0건, `QXxxResult` 생성물 0건). 따라서 **QueryDSL 의존(`querydsl-jpa`·apt)과 querydsl sourceSets/generated 블록은 여전히 `infrastructure:persistence`에만 남고, domain-module과 application-common-module 둘 다 `querydsl-core`·`querydsl-apt`를 완전히 제거**했다(과거엔 core가 `@QueryProjection` DTO 컴파일용으로 `querydsl-core`만 잠정 유지하던 상태였다). 이 배치가 api 모듈로의 QueryDSL 전이 노출을 원천 차단하는 지점이기도 하다.
- **Spring 조립 — 스캔·전역 설정은 소유 모듈(infrastructure)이 선언 (개정됨)**: `com.tastyhouse.infrastructure`는 `WebApiApplication`/`AdminApiApplication`/`CeoApiApplication`/`BatchApplication`의 `scanBasePackages`에 등록되어 있고, JPA 스캔(`@EnableJpaRepositories`/`@EntityScan`, `basePackageClasses` 타입 세이프 방식)뿐 아니라 **JPA Auditing(`@EnableJpaAuditing`)·트랜잭션 관리(`@EnableTransactionManagement`) 전역 설정도 전부 `infrastructure:persistence` 자신의 `InfrastructurePersistenceConfig`(패키지 루트)로 병합됐다.** domain-module은 완전 프레임워크-프리가 되며 과거 core의 `config/DatabaseConfig.java`를 폐지했다(그 파일이 `@EnableJpaRepositories(basePackages="com.tastyhouse.core.domain")`·`@EntityScan`·`@EnableJpaAuditing`·`@EnableTransactionManagement`를 core에서 선언했으나, 도메인 패키지에 더 이상 JPA가 없어 무의미해졌고 auditing/tx는 엔티티·리포지토리를 소유한 infrastructure로 옮기는 것이 응집도상 자연스럽다). domain-module은 infrastructure를 의존하지 않아 컴파일 타임에 그 패키지를 볼 수 없으므로(IDE "Cannot resolve package" 에러), 엔티티를 소유한 모듈이 스스로 스캔·전역 설정을 선언하는 것이 Spring Boot 공식 권장(`basePackageClasses`)과 일치한다.
- **scanBasePackages에서 domain 스캔 엔트리를 제거한다 (개정됨)**: domain-module에는 `@Component`/`@Service`/`@Configuration`이 **0건**이므로(도메인 서비스는 순수 POJO이고 빈 등록은 `infrastructure:persistence`의 컨텍스트별 `<ctx>/config/<Ctx>DomainConfig`가 `@Bean` 팩토리 메서드로 수행) 스캔할 대상이 아예 없다. 따라서 4개 앱(`WebApiApplication`/`AdminApiApplication`/`CeoApiApplication`/`BatchApplication`)의 `scanBasePackages`(및 admin/ceo의 `@ComponentScan basePackages`)에서 과거의 `"com.tastyhouse.core"` 항목을 삭제했다. 남는 엔트리는 각 앱 자신 + `com.tastyhouse.infrastructure`·`com.tastyhouse.external`·`com.tastyhouse.security`(web/admin/ceo)·`com.tastyhouse.logging`이다. 도메인에 새 순수 POJO 서비스를 추가할 때도 스캔 엔트리를 되살리지 말고 **해당 컨텍스트의 `<Ctx>DomainConfig`에 `@Bean`을 추가한다(없으면 신설)**.

## query DAO·QueryPort·Result DTO·SearchCondition 소유 규칙 (개정 — 읽기 계약은 소비 앱이 소유, 구현은 `infrastructure:persistence`)

**표현 목적 조회(read)의 계약(인터페이스 + 입출력 DTO)은 패키지 `com.tastyhouse.application.<ctx>.port.out`에 두고, 그 구현(QueryDSL DAO)은 `infrastructure:persistence`의 `<ctx>/query/` 패키지가 소유한다.** 계약의 **소유 모듈은 소비 앱 수가 정한다** — 한 앱만 쓰면 그 앱의 `{앱}-application`, 2개 이상이 쓰면 `domain-module`이다(챕터 09로 `application-common-module`이 해체되며 5개 모듈로 분산됐다. 패키지는 그대로다). 과거(챕터 03까지)는 `infrastructure:persistence`의 `<ctx>/query/` 패키지 하나가 `{도메인}QueryDao` + `@QueryProjection` Result DTO + `{도메인}SearchCondition`을 전부 소유했고, api 모듈이 그 DAO를 직접 주입했다. 챕터 04(읽기 경로 포트화)에서 이 구조를 완전 매핑 전략으로 역전했다 — **infra가 구현해야 하는 인터페이스는 api 모듈이 컴파일 타임에 봐야 하는데, infra → api 방향 의존은 만들 수 없고(`LayerRulesTest#shouldNotDependOnApiModules`) domain-module에 두면 표현용 투영 184개가 순수 도메인에 섞이므로**, 계약 전용 신규 모듈 `application-common-module`이 필요했다.

과거 core-module `application/` 계층이 조회 서비스와 `application/dto/result/`를 소유했던 문제(도메인에 QueryDSL 의존 유입·그 의존의 api 전이·CQRS 경계 흐려짐)는 챕터 03까지의 구조로 이미 해소돼 있었다. 이번 전환의 동기는 그와 다르다 — **api 모듈이 인프라 구현 상세(DAO 클래스)를 컴파일 타임에 직접 알아야 했던 마지막 결합**을 인터페이스 경계로 끊는 것이다.

### 다중 앱 공유 계약은 `domain-module`이 소유한다 (챕터 05)

**위 규칙의 절반이다 — 2개 이상의 앱이 함께 쓰는 읽기 계약은 소비 앱 모듈이 아니라 `domain-module`에 있다.** 패키지는 `com.tastyhouse.application.<ctx>.port.out` **그대로**이므로, 한 최상위 패키지를 두 모듈이 나눠 소유하는 구조다.

**왜.** 포트를 소비자별로 쪼개도(챕터 04) 여러 앱이 함께 쓰는 메서드와 `*Result`가 남는다. 그것을 어느 한 앱의 application 모듈에 주면 나머지 앱이 그 모듈을 의존해야 한다 — **앱 간 수평 의존**이다. `domain-module`은 이미 4개 앱이 전부 의존하므로 소유자를 여기로 옮기면 **새 의존 간선이 하나도 생기지 않는다.** 이것이 수평 의존을 0으로 만드는 조치다.

**대상.** `event`·`member`·`order`·`point`·`product`·`rank`·`review`·`shop` 8개 컨텍스트 50파일(shop 32개). 포트는 `ShopBasicInfoQueryPort`·`ShopDeliveryTipQueryPort`·`ShopOrderNoticeManagementQueryPort`·`ShopSearchManagementQueryPort`·`ReviewTagQueryPort` 5개다.

**패키지를 바꾸지 않은 이유 셋.**

1. **소비 측 import 수정이 0건**이다. 개명하면 50개 타입의 소비 파일을 전부 고쳐야 하고, 그 과정에서 `Projections.constructor` 인자 회귀(런타임에만 터지는)가 생긴다.
2. **ArchUnit 규칙이 무변경으로 통과**한다. `queryDaosShouldImplementQueryPorts`·`controllersShouldNotDependOnQueryDaos`·`commandServicesShouldNotDependOnQueryDaos`가 전부 `com.tastyhouse.application..port.out..` 패키지로 대상을 잡는다.
3. **`DomainPurityTest`·`ContextBoundaryTest`의 스캔 범위 밖에 남는다.** 두 테스트는 `importPackages("com.tastyhouse.domain")`으로 대상을 모은다. 읽기 계약은 도메인 모델이 아니라 표현용 투영이므로 컨텍스트 경계 규칙(`model`/`repository`/`service` 상호 참조 금지)의 대상이어서는 안 된다 — **회피가 아니라 정확한 구분**이다. 개명했다면 50개 Result가 갑자기 경계 검사 대상이 되어 봉인 목록이 늘어났을 것이다.

모듈명과 패키지명이 어긋나는 것은 `infrastructure:persistence`가 `com.tastyhouse.infrastructure..`를 쓰는 것과 같은 선례다. **`domain-module`은 물리적 거처만 제공하고, 논리적 계층(패키지)은 읽기 계약 그대로다.**

**새 계약을 어디에 둘지 판정.** 소비 앱이 **하나면 그 앱의 `{앱}-application`**, **둘 이상이면 `domain-module`**이다. 다중 앱이 쓰더라도 **애매하면 각 앱이 선언 중복으로 갖고 `domain-module`로 보내지 않는다** — `domain-module`이 표현 투영의 쓰레기통이 되면 위 문단의 기각 사유("표현용 투영 184개가 순수 도메인에 섞인다")가 되살아난다. 기준은 "점주가 설정하고 회원이 보고 관리자가 검수하는 도메인 개념 자체인가"이다. 옮긴 50개는 전수 검사 결과 `java..`·`com.tastyhouse.domain..`·`com.tastyhouse.application..` 밖 import가 0건이고 참조 도메인 타입이 enum과 `Amount` VO뿐이라(애그리거트·리포지토리·서비스 참조 0건) 이 기준을 만족한다.

**프레임워크-프리 게이트가 자동으로 강제한다.** `domain-module`은 루트 `build.gradle`의 spring 주입 제외 대상이라 `import org.springframework...` 한 줄이 **실제 컴파일 에러**다. 옮길 계약에 프레임워크 import가 섞이면 빌드가 즉시 실패한다.

- **패키지 구성 (계약 — 소유 모듈은 소비 앱 수에 따라 갈린다)**: `com.tastyhouse.application.<ctx>.port.out`에 아래 셋을 둔다. Result record는 [record 파일 분리 규칙](#record-파일-분리-규칙-중첩-record-선언-지양)대로 각각 독립 `.java` 파일이며, 접미어는 [결과 DTO 접미어 규칙](#결과-dto-접미어-규칙-result로-통일-dto-금지)대로 `Result`다. 이름·`Management` 한정어 등 기존 명명 규칙은 그대로 승계했다 — `View`로의 개명은 하지 않았다(184개 개명 + 소비 99파일 연쇄 수정 비용 대비 이득 없음).
  - `{Ctx}QueryPort` — 인터페이스(DAO당 1개, 48개+). 해당 `{Ctx}QueryDao`의 public 메서드를 전사한다.
  - `{용도}Result` — `public record`(`@QueryProjection` 없음 — 이 모듈은 QueryDSL을 모른다). DAO가 `Projections.constructor`로 투영한다.
  - `{도메인}SearchCondition` — 포트 메서드 파라미터인 동적 검색 조건 record. 정적 팩토리 `of(...)`로 조립하며([DTO 조립 규칙](#dto-조립-규칙-new-직접-호출-지양)) 필드는 HTTP 경계에서 넘어온 원시타입(`String`/`Long`/`Boolean`)이다.
- **패키지 구성 (구현 — `infrastructure:persistence`)**: `com.tastyhouse.infrastructure.<ctx>.query`에는 이제 `{도메인}QueryDao`(`@Repository`, `{Ctx}QueryPort`를 **implements**) 하나만 남는다. Result·SearchCondition은 더 이상 이 패키지에 없다.
- **도메인당 DAO 1개가 기본**: 소비 모듈이 여러 개(web-api·admin-api·ceo-api)여도 DAO를 모듈별로 쪼개지 않고 한 클래스에 **소비자별 메서드로 분리**한다. 같은 테이블을 읽는 QueryDSL 코드가 여러 파일로 흩어지면 인덱스·조인 전략을 한눈에 검토할 수 없기 때문이다.
- **메서드명에 admin 마커를 붙이지 않는다**: [admin 전용 네이밍 규칙](#admin-전용-네이밍-규칙-메서드타입명에-admin-flavor-admin-접두접미중간어-금지)을 그대로 따라 순수 동작명을 쓴다. 소비자 구분은 이름이 아니라 **시그니처(파라미터 차이)와 동작 의미**로 하고, 비-admin 형제와 이름이 충돌할 때만 `ById`처럼 의미 있는 한정어를 붙인다(예: `findOrderDetail(memberId, orderId)` vs `findOrderDetailById(orderId)`). 노출 범위 차이는 `findAllNotices`(비노출 포함 전체) vs `findVisibleNotices`(노출분만)처럼 **동작 자체를 이름에 담아** 구별한다.
- **대형 도메인만 용도별 DAO 분리 허용**: 한 도메인의 조회가 너무 많아 DAO 하나가 비대해지면 용도별로 나눌 수 있다. 나눌 때도 접미어는 `QueryDao`로 유지하고 용도를 접두·중간어로 표현한다(예: `shop`의 `ShopQueryDao`/`ShopSearchQueryDao`/`ShopChoiceQueryDao`). 분리 기준은 "소비 모듈"이 아니라 **조회 용도(검색 vs 상세 vs 서브 애그리거트)** 다. 여러 DAO로 나뉘어도 대응 `{Ctx}QueryPort`는 도메인 용도별로 함께 나뉜다(예: `ShopQueryPort`/`ShopSearchQueryPort`/`ShopChoiceQueryPort`).
- **소비 모듈이 실제 쓰는 메서드·필드만 이관한다**: 전환 시 과거 조회 서비스의 모든 메서드를 기계적으로 옮기지 않고, 호출부가 실제로 존재하는 것만 포트에 만든다. Result record의 필드도 소비하는 Response가 실제로 쓰는 것만 남긴다 — 쓰이지 않는 조회·필드를 함께 옮기면 그 순간부터 "누가 쓰는지 모르지만 지울 수도 없는" 코드가 되고, 불필요한 컬럼·조인이 쿼리에 남는다.
- **api 모듈에서의 사용법**: 소비 모듈의 `{도메인}QueryService`가 **`{Ctx}QueryPort` 인터페이스**를 주입해 쓰고(DAO 구현체를 직접 알지 않는다), 유스케이스는 `*Result`를 그대로 반환한다. **Result → Response 변환 주체는 api 모듈이다**(admin 챕터 06 · ceo 챕터 09 · web 챕터 10으로 3개 앱 완료) — api 모듈의 Response record가 `from(Result)`로 조립하며, `{앱}-application`의 QueryService에는 매퍼가 없다([DTO 조립 규칙](#dto-조립-규칙-new-직접-호출-지양)). 그 덕분에 api 모듈은 QueryDSL도 `com.tastyhouse.infrastructure..`도 알지 않는다(아래 [api 모듈 QueryDSL·infra 전면 금지 규칙](#api-모듈-querydslinfra-전면-금지-규칙-archunit-강제--챕터-04로-완료)).
- **DAO는 표현에 필요한 완성 형태로 투영한다**: Result는 소비 Service가 추가 조회나 파생 계산 없이 그대로 응답에 옮길 수 있는 값을 담는다. 대표 사례가 **파일 URL**로, DAO가 `uploaded_file`을 join한 뒤 `FileUrlResolver`로 표시용 URL까지 완성해 담는다(경로나 fileId를 넘겨 Service가 변환하게 하지 않는다). 이를 위해 DAO가 도메인 출력 포트(`FileStoragePort`)를 경유하는 `@Component`를 주입받는 것은 허용된다 — driven 어댑터가 도메인 포트를 쓰는 정상 형태이고 변환 자체가 순수 연산이라 쿼리를 늘리지 않는다. 상세는 [파일 URL 조립 위치 규칙](#파일-url-조립-위치-규칙-query-dao가-fileurlresolver로-완성) 참고.
- **`@QueryProjection` → `Projections.constructor` 전환**: Result record가 QueryDSL을 모르는 계약 모듈로 옮겨지며 그 record에 `@QueryProjection`을 달 수 없다. DAO의 select 절은 `Projections.constructor(XxxResult.class, notice.id, notice.title, ...)` 형태로 조립한다. **`Projections.constructor`는 리플렉션으로 런타임에 생성자를 찾으므로**, Result record가 `public`이 아니거나 select 절 인자 개수·타입·순서가 생성자와 불일치하면 컴파일은 통과하고 **호출 시점에만 500**이 난다(`ShopRiderGuidePickupPresenceResult` 선례). 전환한 쿼리는 반드시 한 번 호출해 확인한다.
- **가드 테스트 위치가 함께 이동했다**: `QueryResultRecordVisibilityTest`(public record 강제)는 이제 읽기 계약 패키지의 Result를 스캔 대상으로 삼는다. 신설 `ProjectionConstructorMatchingTest`(`infrastructure:persistence`)는 select 절 인자 개수가 대상 record의 public 생성자 파라미터 개수와 일치하는지 소스 스캔으로 검증해, `@QueryProjection`이 주던 컴파일 게이트를 대체한다.

reference 구현: `notice` 도메인 — `com.tastyhouse.application.notice.port.out`(`NoticeQueryPort` + `NoticeManagementListItemResult`/`NoticeListItemResult`/`NoticeDetailResult`/`NoticeSearchCondition`), `infrastructure-module/.../notice/query/NoticeQueryDao`(`findAllNotices`(비노출 포함 관리 목록)·`findVisibleNotices`(web 노출분)로 소비자별 메서드를 admin 마커 없이 분리하며 `NoticeQueryPort`를 implements), 소비 측은 `admin-api/notice/NoticeQueryService`·`web-api/notice/NoticeQueryService`(둘 다 `NoticeQueryPort` 주입). 용도별 DAO/포트 분리 사례: `shop`(`ShopQueryDao`/`ShopSearchQueryDao`/`ShopChoiceQueryDao`, 조회 수가 많아 검색·상세·`ShopChoice`로 나눈 대형 도메인).

## write 포트 잔류 판정 기준 (domain repository에 남길 조회의 경계)

**어떤 조회를 domain repository(write 포트)에 남기고 어떤 조회를 infra query DAO로 내릴지는 "그 조회가 불변식 검증·상태 전이에 필요한가"로 가른다.** 판정 질문 하나로 정리하면: **"이 조회가 없으면 불변식 검증이나 상태 전이가 불가능한가?"** — 그렇다면 write 포트에 남기고, 아니라면(= 화면에 보여주기 위한 조회라면) infra query DAO로 내린다. 이 기준이 없으면 리포지토리 인터페이스가 "도메인이 필요한 것"과 "화면이 필요한 것"을 함께 담게 되어, CQRS 분리가 이름만 남고 실제로는 read/write가 같은 포트에 다시 뒤섞인다.

| 구분 | 두는 위치 | 해당하는 것 |
|---|---|---|
| 도메인 모델·VO·원시값을 반환하고 command 경로/도메인 서비스 트랜잭션 안에서 소비 | **domain-module `<ctx>/repository/`** (write 포트) | `findById`, `save`, `delete`, `existsByX`(중복 방지 불변식), `findByNaturalKey`(자연키 단건 로드), 검증용 `countByX`, 락 획득 조회(`findByIdForUpdate` 등) |
| Result DTO·`PageResult<T>` 반환, 조인 투영, 목록/검색/페이징 | **`com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 계약** + **infrastructure-module `<ctx>/query/`** (query DAO 구현) | `findXxxPage`, `findAllXxx`, `searchXxx`, 상세 화면용 조인 투영 `findXxxDetail` |

- **반환 타입이 1차 신호다**: 도메인 모델(`Optional<Notice>`)·VO·`boolean`/`long` 같은 원시값을 반환하면 write 포트 후보이고, `XxxResult`·`PageResult<T>`를 반환하면 query DAO다. 도메인 모델을 반환하더라도 그것을 **화면에 뿌리기 위해** 로드하는 목록 조회는 query DAO로 내린다(도메인 모델 컬렉션을 표현 목적으로 로드하는 것은 불변식과 무관하다).
- **`existsByX`·`countByX`는 용도로 갈린다**: "이미 존재하면 생성 불가" 같은 **불변식 검증**이면 write 포트에 남기고(예: 같은 가게·같은 이미지 타입에 PENDING 요청이 2건 생기지 않게 막는 검사), 화면에 개수를 표시하려는 집계라면 query DAO로 내린다.
- **락 획득 조회는 반드시 write 포트**: 낙관적 락 `@Version` 검증·`saveAndFlush`로 충돌을 커밋 전에 노출시키는 경로는 상태 전이의 일부이므로 write 포트에 남긴다([낙관적 락 재시도 배치 규칙](#낙관적-락-재시도-배치-규칙-재시도-루프는-트랜잭션-경계-밖-별도-executor-빈) 참고).
- **양쪽에 같은 데이터를 읽는 메서드가 생기는 것을 허용한다**: write 포트의 `findById`(도메인 모델 로드)와 query DAO의 `findXxxDetail`(투영)이 같은 행을 읽어도 중복이 아니다 — 목적(불변식 vs 표현)과 반환 타입이 다르므로 통합하지 않는다.

reference 구현: `domain-module/.../notice/repository/NoticeRepository`(`findById`/`save` 둘만 노출 — 목록·검색·페이징은 전부 `com.tastyhouse.application.notice.port.out.NoticeQueryPort`(구현은 `infrastructure-module/.../notice/query/NoticeQueryDao`)가 담당하며, 그 의도를 인터페이스 Javadoc에 명시). 락 획득 조회 사례: `reservation` 도메인의 `ReservationSlotRepository`(`saveAndFlush`로 `@Version` 충돌을 커밋 전에 노출). 기준 위반을 사후 교정한 사례: `file` 도메인의 `UploadedFileRepository`가 응답 URL 변환용으로 `findFilePath`(단건 default)·`findFilePaths`(배치)를 갖고 있었으나, 둘 다 "화면에 뿌릴 값"을 얻는 조회여서 이 기준에 맞지 않았고 조회를 DAO join으로 옮긴 뒤 호출부가 0이 되어 제거했다(현재는 `save`/`findById`만 노출). 애그리거트를 로드해 그 fileId를 표현용으로만 쓰던 5개 경로도 같은 기준으로 `ShopQueryDao#findShopImageUrls`·`MemberQueryDao#findProfileImageUrl` 투영으로 이관했다 — 다만 그 경로들은 응답의 다른 필드나 소유권 검증(ceo-api `validateOwnership`) 때문에 애그리거트 로드 자체는 계속 필요하므로, **이미지 URL만** 투영으로 분리했다.

## 도메인 컨텍스트 경계 규칙 (ArchUnit 강제 — 봉인 목록 방식)

**domain-module의 한 바운디드 컨텍스트(`com.tastyhouse.domain.<ctx>`)는 다른 컨텍스트를 ID VO·도메인 이벤트·출력 포트로만 참조하고, 타 컨텍스트의 `model`/`repository`/`service`를 직접 import하지 않는다.** domain-module에는 25개 컨텍스트가 한 모듈에 공존하는데 컨텍스트 간 의존을 막는 규칙이 하나도 없었고(`DomainPurityTest`는 프레임워크 순수성과 모듈 방향만 검사한다), 그 결과 한 애그리거트의 내부 구현이 다른 컨텍스트에 그대로 노출돼 있었다. 모듈을 컨텍스트별로 쪼개는 대신 ArchUnit으로 경계를 강제한다.

| 대상 | 타 컨텍스트에서 | 이유 |
|---|---|---|
| `<ctx>.vo..` | **허용** | ID VO 참조 — 애그리거트 간 FK 표현의 정상 형태([ID VO 경계 규칙](#id-vo식별자-값-객체-경계-규칙)) |
| `<ctx>.event..` | **허용** | 도메인 이벤트 타입 참조(구독 자체는 infra 리스너 몫) |
| `<ctx>.port..` | **허용** | 출력 포트 |
| `<ctx>.model..` / `.repository..` / `.service..` | **금지** | 애그리거트 내부 구현 |
| `shared..` · `exception..` | **전면 허용** | 컨텍스트가 아니라 전 컨텍스트 공용 |

**신규 컨텍스트 `domain/menureview/`(메뉴 평가)** — 매장 리뷰(`domain/review/`)와 **독립된 축**이라 review 안이 아니라 별도 컨텍스트로 뒀다. 애그리거트 경계(FK 없음·생애주기 무관)와 변경 이유("매장 리뷰와 그 소셜 기능" vs "상품 평점의 근거")가 모두 갈리며, 무엇보다 **경계 규칙이 이 설계의 최상위 불변식을 지켜준다** — 같은 컨텍스트에 두면 나중에 누군가 `review_id`를 붙일 때 아무 규칙도 막지 못하지만, 나눠 두면 그 참조가 즉시 `ContextBoundaryTest` 위반으로 드러난다("어느 쪽을 먼저 하든, 하나만 하든 성립한다"를 빌드가 강제). 타 컨텍스트를 ID VO(`MemberId`·`ShopId`·`ProductId`·`OrderId`·`OrderProductId`)와 이벤트로만 참조하므로 **`SEALED_VIOLATIONS`에 항목이 늘지 않는다**. 도메인 문서는 `docs/domain/menu-review.md`.

- **기존 위반은 고치지 않고 봉인했다**: 규칙 도입 시점의 위반 클래스 21개를 `ContextBoundaryTest.SEALED_VIOLATIONS`에, 컨텍스트 간 순환 성분 2개(`member,point`와 `order,product,review,shop`)를 `SEALED_CYCLES`에 등재해 통과시켰다. 이 단계의 목표는 전면 재설계가 아니라 **현상 동결 + 신규 위반 차단**이며, 실제 결합 해소는 후속 단계가 담당한다. 방식은 `ErrorCodeConventionTest`의 봉인 목록(EnumSet) 선례를 그대로 따른 수동 `Set`이다.
- **후속 해소 진행 상황(step 7)**: 위반 21개 → **16개**, 순환 성분 2개 → **1개**로 줄었다. 해소된 것은 `ReferralRegistrationService`(추천 보상 적립을 point 컨텍스트 서비스로 이관 → `member,point` 순환 소멸), `Order`·`OrderPlacement`·`OrderDeliveryDestination`·`OrderSchedule`(아래 두 규칙으로 order VO·모델의 타 컨텍스트 참조 제거)이다. `OrderPlacementService`는 타 컨텍스트를 **서비스로만** 참조하도록 정리했으나 서비스 간 의존은 규칙상 여전히 위반이라 봉인에 남는다.
- **⚠️ 순환은 쌍이 아니라 강결합 성분(SCC) 단위로 봉인한다**: `SliceRule#beFreeOfCycles`는 2노드 상호 참조뿐 아니라 `order → product → shop → order` 같은 **전이 순환**까지 잡으므로, 봉인 목록과 그 짝 테스트도 같은 단위여야 한다. 쌍으로 적으면 두 모델이 어긋나 **짝 테스트가 "이 쌍을 지우라"고 지시했는데 그대로 따르면 정작 전이 순환이 드러나 규칙이 깨지는** 모순이 생긴다. 실제로 이 저장소의 순환은 `order↔shop`·`review↔shop` 두 쌍이 아니라 **`product`까지 포함한 4-노드 성분 하나**이며, 쌍 표기로는 `product`가 봉인 목록에 이름조차 등장하지 않아 감사(audit)가 불가능했다(리뷰에서 발견해 SCC 단위로 교정).
- **⚠️ 봉인 목록에 새 항목을 추가하지 말 것.** 목록은 **줄어들기만 해야 한다** — 항목 추가는 새 위반을 승인하는 것이다. 신규 코드는 규칙을 지키고, 기존 위반을 해소했으면 그 클래스를 목록에서 지운다.
- **짝 테스트가 봉인의 낡음을 잡는다**: `sealedViolationsShouldNotBeStale()`·`sealedCyclesShouldNotBeStale()`이 "목록에 있는데 실제로는 더 이상 위반하지 않는"(순환은 "성분이 사라졌거나 더 작아진") 항목을 찾아 실패시킨다(수동 목록을 택할 때의 필수 조건). 봉인이 전부 해소되면 `sealedViolationListShouldNotBeEmpty()`가 실패해 **봉인 장치 자체를 제거하고 순수 강제로 전환하라**고 알려준다.
- **순환 제외는 성분 "안쪽"으로만 한다**: `SliceRule#ignoreDependency(from, to)`의 양쪽에 **같은 성분**을 걸어, 출발·도착이 모두 그 성분에 속할 때만 무시한다. "봉인 컨텍스트가 관여하는 의존을 통째로 무시"하면 예컨대 `order→member` 같은 성분 밖 신규 순환까지 함께 가려진다.
- **하위 패키지 판정은 클래스명을 빼고 패키지 세그먼트만 스캔한다**: 마지막 세그먼트(클래스명)까지 훑으면 컨텍스트 루트에 놓인 클래스명이 우연히 세그먼트 목록과 겹칠 때(`<ctx>.Event`·`<ctx>.Model` 등) 하위 패키지로 오인된다. **`event`는 실제 컨텍스트명이자 허용 세그먼트**라 이 충돌이 한 걸음 거리에 있다.
- **판정 근거는 import가 아니라 ArchUnit 의존 그래프다**: 필드·시그니처·제네릭 파라미터 같은 간접 참조까지 잡힌다. 봉인 목록은 파일 단위(최상위 클래스)로 관리하므로 중첩·익명 클래스(`Xxx$1`)를 별도 등재하지 않는다.
- **`member.follow`·`member.referral`처럼 컨텍스트 아래 한 겹이 더 있어도 동작한다**: 하위 패키지 판정이 "허용/금지 목록에 있는 세그먼트를 앞에서부터 찾는" 방식이라 중첩 깊이에 무관하다.
- **`allowEmptyShould(true)`를 쓰지 않는다**(공허 통과 금지 — `DomainPurityTest`·`LayerRulesTest` 개정 선례).

### 크로스 컨텍스트 결합 해소 패턴 (step 7에서 확립)

경계 위반을 실제로 걷어낼 때 쓰는 세 가지 형태다. 어느 것을 쓸지는 **그 규칙을 누가 소유해야 하는가**로 정한다.

- **검증·조회를 소유 컨텍스트의 서비스로 밀어낸다 (기본형)**: 소비 컨텍스트가 타 컨텍스트의 모델·리포지토리를 직접 주입해 검증하고 있으면, 그 검증을 **소유 컨텍스트의 도메인 서비스**로 옮기고 소비 측은 결과 record만 받는다. 소유 컨텍스트가 정책을 바꿔도 소비 경로가 낡은 규칙으로 남지 않는다. reference: `OrderProductValidationService`(product 소유 — 상품 존재·판매중지·옵션 검증 + `OrderProductSnapshot`), `ShopOrderContextService`(shop 소유 — 가게 로드·주문가능·최소주문금액·배달지역·배달팁·예약슬롯 파사드), `OrdererLookupService`(member 소유 — `OrdererSnapshot`).
  - **애그리거트를 밖으로 내보내지 않는다**: 여러 단계가 같은 애그리거트를 재사용해야 하면, 값 접근자가 package-private인 **핸들**(`ShopOrderContextService.OrderableShop`)로 감싸 돌려준다. 소비 측은 그 핸들을 이후 단계에 되돌려주는 토큰으로만 쓰므로, 애그리거트를 한 번만 읽으면서도 타 컨텍스트가 모델을 import하지 않는다.
  - **입력도 소유 컨텍스트의 타입으로 받는다**: 소비 컨텍스트의 요청 record를 그대로 넘기면 방향만 뒤집힌 위반이 된다(`OrderPlacementItem` → product). 검증에 필요한 값만 담은 소유 컨텍스트 입력 record를 두고 호출부가 변환한다(`OrderLineSelection`).
  - **⚠️ 파사드가 또 다른 컨텍스트를 끌어들이지 않는지 확인한다**: 이관 대상 코드가 제3 컨텍스트를 쓰고 있으면 파사드가 그 위반을 물려받는다. 이미 해석된 값으로 받아 판정을 호출부에 남긴다 — `ShopOrderContextService`는 `holiday`의 `PublicHolidayCalendar`를 주입받지 않고 `boolean publicHoliday`를 받는다(순수 계산기가 해석된 값을 받는 것과 같은 형태).
- **VO 팩토리는 타 컨텍스트 타입 대신 원시값을 받는다**: `@Embedded` 스냅샷 VO가 다른 컨텍스트의 애그리거트·모델을 팩토리 파라미터로 받으면 그 VO 파일 자체가 경계를 위반한다. 값을 낱개로 받고, 꺼내 넘기는 책임은 VO를 만드는 서비스가 진다. reference: `OrderDeliveryDestination.of(...)`(과거 `MemberDeliveryAddress` 수신 → 7개 원시값), `OrderSchedule.of(...)`(과거 `ScheduledOrderSlot` 수신 → 시작·종료 시각 2개). **같은 타입 파라미터가 연속하므로 자리를 바꿔도 컴파일되고 값만 조용히 뒤바뀐다** — 호출부에서 한 인자씩 대조한다([DTO 조립 규칙](#dto-조립-규칙-new-직접-호출-지양)의 동일 경고).
  - 서비스 반환 타입도 같다 — `ScheduledOrderSlotService#resolveSlot`은 order의 `OrderSchedule`이 아니라 shop 소유 `ScheduledOrderSlot`을 돌려준다.
- **양쪽 컨텍스트의 어휘인 enum은 `shared`로 옮긴다**: 어느 한쪽의 애그리거트 내부 구현이 아니라 두 컨텍스트가 대등하게 쓰는 개념이면 `shared`가 소유한다(경계 규칙이 `shared`를 전면 허용하므로 양방향이 정상 참조가 된다). reference: `OrderMethod`(과거 `shop.model` → `shared.model`. `Order`·`OrderSchedule`이 필드로 보유해 order → shop 위반이었는데, **`order.model`로 옮기면 방향만 뒤집혀 shop 쪽 18개 클래스가 새로 위반**하므로 `shared`가 유일한 해답이었다. `ApprovalStatus` 선례).
  - **상수명을 바꾸지 않으면 DB·API 계약은 무변경이다**: `EnumType.STRING` + `VARCHAR` 저장이라 저장값과 HTTP 문자열이 상수명에 묶여 있다. 패키지 이동은 순수 컴파일타임 재배치이므로 마이그레이션이 필요 없다.
  - **⚠️ 이동 방향을 정하기 전에 양쪽 참조 수를 센다**: 위반을 없애려다 더 많은 위반을 만드는 것이 이 작업의 주된 실패 형태다. 봉인 목록은 줄어들기만 해야 하므로, 이동 후 위반이 늘면 그 방향은 오답이다.

### 인바운드 포트(UseCase 인터페이스)를 도입한다 — 완전 매핑 전략 채택 (과거 결정의 명시적 번복)

**과거 이 절은 "인바운드 포트를 도입하지 않는다"였다. 그 결정을 번복한다.** 번복임을 제목에 남기는 이유는, 폐기된 논거가 그럴듯해서 같은 논의가 다시 열리기 쉽기 때문이다.

- **과거 반대 논거는 지금도 사실이다**: UseCase 인터페이스는 구현체가 하나뿐이고 소비자도 모듈당 하나뿐이라 **다형성·교체 가능성의 실익은 여전히 0에 가깝다.** 이 점을 부정해서 뒤집는 것이 아니다.
- **도입 근거는 다형성이 아니라 다음 두 가지다**:
  1. **컴파일 게이트** — 컨트롤러가 `application/service/`의 구체 클래스를 아예 모르게 만든다. ArchUnit은 위반을 *사후에* 잡지만, 컨트롤러가 UseCase 타입만 주입받으면 구체 서비스에 손대는 코드가 **애초에 컴파일되지 않는다**. 경계 강제 수단의 등급이 한 단계 올라간다.
  2. **경계 계약의 문서화** — UseCase 인터페이스 한 파일이 그 애그리거트의 연산 계약(어떤 명령이 있고 무엇을 받아 무엇을 돌려주는가)을 한눈에 고정한다. 구현 본문에 섞여 있던 계약이 별도 파일로 드러난다.
- **입도 규칙**: 기본은 애그리거트 단위 `{도메인}CommandUseCase`/`{도메인}QueryUseCase` **쌍**이다. **메서드 7개를 초과하거나 컨트롤러 2개 이상이 공유하는 대형 서비스만** per-operation(연산 하나당 인터페이스 하나)으로 분해한다. 처음부터 per-operation으로 쪼개면 과거 우려대로 순수 보일러플레이트가 된다.
- **컨텍스트별 모듈 분할은 여전히 하지 않는다**: 경계는 ArchUnit + 위 컴파일 게이트로 충분하며, 25개 모듈로 쪼개면 빌드 그래프·`settings.gradle`·의존 선언이 그만큼 늘어난다. 모듈 분할이 주는 강한 보장이 필요한 것은 **모듈 간 방향 의존**(domain ← infrastructure ← api)이고 그쪽은 이미 분리돼 있다.
  - **개정 이력 (모듈 재편 프로그램, 챕터 01~06) — 이 항목은 "컨텍스트별" 분할 금지이지 "계층별" 분할 금지가 아니다.** 이 절을 쓸 당시에는 계층 분할도 하지 않는 전제였으나, 챕터 01~04로 **앱별 application 모듈 4개**(`web-application`·`admin-application`·`ceo-application`·`batch-application`)를, 챕터 05로 **`infrastructure:persistence`/`infrastructure:redis`** 재편을 완료했다. 그 결과:
    - **모듈 경계가 "계층 × 앱" 2차원이 됐다.** 계층 위반(서비스가 컨트롤러를 참조, application이 infra를 참조)이 ArchUnit 사후 검출이 아니라 **컴파일 에러**가 된다 — 위 컴파일 게이트 논거를 모듈 수준으로 한 단계 더 밀어붙인 것이다.
    - **컨텍스트 경계는 여전히 모듈이 아니다.** 25개 컨텍스트의 수평 경계는 `domain-module`의 ArchUnit `ContextBoundaryTest`(봉인 목록)가 계속 담당한다. 이 항목의 금지는 그대로 유효하다.
    - 분할 대상이 "컨텍스트"(25개, 수평)가 아니라 "계층 × 앱"(8개, 수직)이었기 때문에 위 우려(모듈 폭증)가 현실화되지 않았다 — 모듈은 25개가 아니라 6개 늘었다.
  - **~~단일 예외 — `application-common-module`~~ (챕터 09에서 소멸)**: 읽기 경로 포트를 소유할 모듈 하나를 신설했던 예외는 **모듈 자체가 삭제되며 사라졌다**. 계약이 전부 소비 앱의 `{앱}-application`(단독분)과 `domain-module`(공유분)으로 옮겨가 그 모듈이 비었기 때문이다. 지금은 **읽기 계약 전용 모듈이 없다** — 계약을 어디 둘지는 위 [소유 규칙](#query-daoqueryportresult-dtosearchcondition-소유-규칙-개정--읽기-계약은-소비-앱이-소유-구현은-infrastructurepersistence)이 소비 앱 수로 판정한다.

reference 구현: `domain-module/src/test/.../architecture/ContextBoundaryTest`(경계 규칙 1 + 순환 규칙 1 + 봉인 짝 테스트 3). 같은 모듈의 `DomainPurityTest`(프레임워크 순수성·모듈 방향)와 역할이 겹치지 않는다 — 이쪽은 **컨텍스트 간 수평 경계**만 본다.

## api 모듈 QueryDSL·infra 전면 금지 규칙 (ArchUnit 강제 — 챕터 04로 완료)

**`web-api`/`admin-api`/`ceo-api`/`batch-module`의 `src`에는 `com.querydsl.*` import가 0건이고, `@QueryProjection` 선언이 0건이며, `com.tastyhouse.infrastructure..` import가 전면 0건이다.** 읽기 경로 포트화(챕터 04) 완료로, 과거 "infra 중 `..query..`만 허용"이던 예외는 **완전히 소멸했다** — 4개 모듈 어디에도 봉인 목록이 남아 있지 않다.

- **읽기 포트 인터페이스 `{Ctx}QueryPort`와 그 입출력 타입(`*Result`·`*SearchCondition`)은 `com.tastyhouse.application.<ctx>.port.out`이 담고(소유 모듈은 소비 앱 수에 따라 갈린다)**하고, `infrastructure:persistence`의 DAO가 그 인터페이스를 `implements`한다. api 모듈의 `{도메인}QueryService`는 DAO 구현이 아니라 포트 인터페이스를 주입하므로 **`com.tastyhouse.infrastructure..`를 전혀 import하지 않는다.** 강제 규칙은 각 모듈 `LayerRulesTest`의 `shouldNotDependOnQuerydsl`·`shouldNotDependOnInfrastructurePersistence`와, 읽기 포트 직접 주입을 막는 `controllersShouldNotDependOnQueryDaos`·`commandServicesShouldNotDependOnQueryDaos`다. **챕터 04의 마이그레이션 임시 장치(`shouldNotDependOnInfrastructureQuery`, 구 `infrastructure..query..` + 신 `application..port.out..` 이중 패키지 매칭)는 챕터 05에서 전수 제거**됐다 — api 모듈이 `infrastructure:persistence`을 소스 레벨에서 참조할 수 없게 되어 그 규칙이 대상 0건으로 공허해졌기 때문이며, 대신 패키지 기준 규칙으로 승격했다(아래 §승격 규칙).
  - `..persistence..`(JpaEntity/Mapper/JpaRepository/RepositoryImpl) 금지는 처음부터 예외 없이 유지된다. `..listener..`는 스프링이 이벤트로 간접 연결하므로 api 모듈이 import할 일이 없다.
- **`@QueryProjection` → `Projections.constructor` 전환 (완료)**: Result record가 QueryDSL을 모르는 계약 모듈로 이동해 그 record에 `@QueryProjection`을 달 수 없으므로, DAO 쿼리는 `@QueryProjection` 생성자 대신 `Projections.constructor(XxxResult.class, ...)`로 조립한다. 리포 전체에 `@QueryProjection` 선언 0건, `QXxxResult` Q타입 생성물 0건이다.
  - **Result record는 반드시 `public`이어야 한다**: `Projections.constructor`는 **리플렉션으로 런타임에** 생성자를 찾으므로, record가 package-private이면 컴파일은 통과하고 **호출 시점에 500이 난다.** 생성자 시그니처(파라미터 개수·타입·순서) 불일치도 마찬가지로 컴파일에 걸리지 않는다. 이 저장소에는 `ShopRiderGuidePickupPresenceResult`로 실제 이 사고를 낸 선례가 있다. 전환한 쿼리는 **반드시 한 번 호출해 확인**한다. `infrastructure:persistence`의 `ProjectionConstructorMatchingTest`가 select 절 인자 개수와 대상 record의 public 생성자 파라미터 개수 일치를 소스 스캔으로 검증해 이 컴파일 게이트 상실을 보완한다.
- **강제 수단은 ArchUnit + grep 이중**: 4개 모듈 각각의 `architecture/LayerRulesTest`(`shouldNotDependOnQuerydsl`·`shouldNotDependOnInfrastructurePersistence`)로 빌드 게이트를 두고, 리뷰 시 `com.querydsl`·`@QueryProjection`·`com.tastyhouse.infrastructure` grep으로 교차 확인한다.
- **`allowEmptyShould(true)`를 쓰지 않는다 (공허 통과 제거)**: 과거 규칙들은 대상 클래스가 0건이어도 통과하도록 `allowEmptyShould(true)`가 붙어 있어, **규칙이 아무것도 검사하지 않는 상태를 성공으로 보고**하고 있었다. 전환 완료로 모든 규칙이 실제 대상을 갖게 되었으므로 이 옵션을 제거했다 — 대상 0건이면 그 자체가 실패로 드러나야 한다. **챕터 05 기준 리포 전체에 `allowEmptyShould(true)`가 0건**이다 — 마지막까지 남아 있던 `batch-module`의 CQRS 서비스 규칙도 매칭 대상에 실재하는 `*SchedulerService` 7개를 포함시켜 공허성을 없앴으므로 옵션 자체를 제거했다. 즉 모든 규칙이 실제 대상을 가지며, 대상이 0건이 되면 곧바로 실패한다.
- **application 서비스 web 의존 금지 규칙의 개정**: 과거 `applicationShouldNotDependOnWebLayer`는 `..application..` 패키지를 매칭했는데, application 계층 해체로 그런 패키지가 사라져 **대상 0건으로 공허하게 통과**하고 있었다. 이를 `applicationServicesShouldNotDependOnWebLayer`로 개정해 **클래스명(`*CommandService`/`*QueryService`)으로 대상을 잡고**, 차단 대상을 web *플럼빙*(`org.springframework.web.bind..`·`org.springframework.web.servlet..`·`org.springframework.http..`·`jakarta.servlet..`)으로 한정했다. 서비스가 요청 바인딩·서블릿·`HttpStatus`를 알 이유가 없다는 것이 규칙의 취지다.
- **`MultipartFile`은 예외로 허용한다**: `org.springframework.web.multipart.MultipartFile`은 차단 목록에 넣지 않는다 — 파일 업로드에서 업로드 자체를 받는 경계 타입이라 `ceo-api`의 이미지 변경·콘텐츠보드 서비스가 정당하게 파라미터로 사용하며, 이를 금지하려면 업로드 흐름 자체를 재설계해야 한다.

- **컨트롤러의 DAO 직접 주입도 금지한다 (`controllersShouldNotDependOnQueryDaos`, 챕터 04로 확장)**: 조회는 `*QueryService`가 읽기 포트를 주입해 수행하고 Result → Response 변환까지 담당하므로, 컨트롤러가 `com.tastyhouse.infrastructure..query..`나 `com.tastyhouse.application..port.out..`를 알 이유가 없다. 이 규칙이 없으면 **구조적 구멍**이 남는다 — `controllersShouldNotDependOnRepositories`는 이름 접미어 `*Repository`만 매칭해 `*QueryDao`를 놓치고, `shouldNotDependOnInfrastructurePersistence`는 `..persistence..`만 차단해 `..query..`에 있는 DAO를 놓친다. 즉 컨트롤러가 DAO를 직접 주입해도 어느 규칙에도 걸리지 않았다. **챕터 04에서 차단 패키지가 `com.tastyhouse.infrastructure..query..`와 `com.tastyhouse.application..port.out..` 양쪽으로 확장**됐다 — 컨트롤러·CommandService는 읽기 포트도 직접 주입할 수 없다(CQRS 유지). 도입 시점 위반은 0건이며 규칙은 그 상태를 고정한다(기존 두 규칙은 이중 방어로 유지). 같은 확장은 `commandServicesShouldNotDependOnQueryDaos`에도 적용됐다.
- **`batch-module`에 남은 공허 통과 규칙은 0건이다 (개정)**: 과거 `applicationServicesShouldNotDependOnWebLayer`가 `allowEmptyShould(true)`로 통과하던 것은 batch가 CQRS 분리를 쓰지 않아 `*CommandService`/`*QueryService`가 0개이고 잡 본문을 `*SchedulerService`에 담기 때문이었다. 매칭 대상에 **`*SchedulerService`를 포함**시켜 실재하는 잡 서비스들(`region`·`grade`·`product`·`productsoldout`·`rank`·`reviewblind`·`search` 7개 잡)이 규칙 대상이 되게 하고 `allowEmptyShould(true)`를 제거했다. 나머지 `requestResponseRecordsShouldBeDomainAndInfraFree`는 `crawling/bbq/response/`에 record 4종이 실재해 애초에 공허하지 않다. **따라서 이제 4개 api 모듈 어디에도 `allowEmptyShould(true)`가 없다.**

reference 구현: `web-api/src/test/.../architecture/LayerRulesTest`(및 `admin-api`/`ceo-api`/`batch-module`의 동명 테스트) — `applicationServicesShouldNotDependOnWebLayer`·`controllersShouldNotDependOnRepositories`·`controllersShouldNotDependOnQueryDaos`·`commandServicesShouldNotDependOnQueryDaos`·`shouldNotDependOnQuerydsl`·`shouldNotDependOnInfrastructurePersistence`, 그리고 챕터 05에서 패키지 기준으로 승격한 `webAdaptersShouldNotDependOnApplicationServices`(`..adapter.in.web..`는 `..application.service..` 구체 클래스 의존 금지 — 접미어가 다른 파사드까지 잡는다)·`portInShouldBeFreeOfWebDomainAndInfrastructure`(Command 규칙과 web 플럼빙 규칙의 패키지 기준 통합) 규칙(batch는 컨트롤러가 없어 controller 규칙 2개와 승격 규칙을 두지 않는다). **`applicationServicesShouldNotDependOnWebLayer`는 패키지 전면 금지로 상향하지 않는다** — `request/`·`response/`가 `..adapter.in.web..` 하위로 이동했고 QueryService가 Response를 조립하는 구조가 확정이라, 상향하면 그 확정 구조가 곧바로 위반이 된다. 근거는 해당 규칙 Javadoc에 남겼다. `infrastructure:persistence`의 `architecture/LayerRulesTest`에는 신설 `queryDaosShouldImplementQueryPorts`(`*QueryDao`는 `..application..port.out..` 인터페이스를 최소 1개 구현 — 봉인 없이 순수 강제)가 짝을 이룬다.

## infrastructure-module 계층 방향 규칙 (ArchUnit — `LayerRulesTest`)

**`infrastructure:persistence`에도 ArchUnit 계층 방향 규칙을 둔다.** 그동안 이 모듈에는 ArchUnit 의존 자체가 없었고, 기존 가드 2종(`QueryResultRecordVisibilityTest`·`EmbeddedRecordComponentOrderTest`)은 수제 리플렉션 클래스패스 스캔으로 **런타임 규약**(record 가시성·`@Embedded` 컴포넌트 순서)만 지키고 있어, 계층 방향 규칙을 둘 곳이 없었다. **기존 가드 2종은 그대로 둔다** — 스캔 방식이 이미 잘 동작하므로 ArchUnit으로 재작성하지 않고, 새 테스트에는 그 방식으로 표현할 수 없는 방향 규칙만 둔다.

- **`shouldNotDependOnApiModules`** — infra는 `com.tastyhouse.{webapi,adminapi,ceoapi,batch}..`를 의존하지 않는다. 빌드 그래프상 이미 막혀 있지만(infra는 api 모듈을 의존하지 않음) 테스트로 명시해 향후 의존 추가 시 즉시 드러나게 한다. `..listener..`의 api 모듈 의존 금지도 이 규칙이 함께 커버한다(`@TransactionalEventListener` 규약 자체는 강제하지 않는다).
- **`persistenceShouldNotDependOnQuery`** — read→write 단방향. **반대 방향(`..query..` → `..persistence..`)은 정상**이다(DAO가 `QXxxJpaEntity`를 static import해 조인하는 것이 조회 구현의 기본 형태). 금지하는 것은 역방향으로, write 경로가 표현용 투영에 결합되면 api 모듈에서 막아 둔 CQRS 교차 주입 금지(`commandServicesShouldNotDependOnQueryDaos`)가 infra 안쪽에서 우회된다.
- **봉인 목록 3건**: 도입 시점 위반은 전부 **도메인 출력 포트 어댑터**다(`ProductReviewStatisticsAdapter`·`MemberReviewCountAdapter`·`KeywordCountAdapter`). 도메인이 선언한 포트를 구현하면서 그 데이터의 소유 도메인이 이미 가진 read model을 재사용하는 형태로(예: 랭킹 집계용 리뷰 수는 리뷰 도메인 소유라 `review/query/`에 있고 랭킹 포트 어댑터가 도메인 값 타입으로 옮겨 담는다), write 경로가 아니라 **포트 구현**이라 위 위험에 해당하지 않지만 패키지 위치(`..persistence..`)가 규칙 표현과 어긋나 잡힌다. `ErrorCodeConventionTest`·`ContextBoundaryTest` 선례대로 클래스명으로 명시 제외하며 **목록은 줄어들기만 해야 한다** — 새 항목 추가는 새 위반을 승인하는 것이다. 짝 테스트 `sealedPersistenceToQueryShouldNotBeStale`(더 이상 위반하지 않는 낡은 항목 검출)·`sealedPersistenceToQueryListShouldNotBeEmpty`(전부 해소되면 봉인 장치 제거 지시)가 함께 붙는다.
- **`allowEmptyShould(true)`를 쓰지 않는다** — api 모듈 4개와 동일하게 공허 통과를 허용하지 않는다.

reference 구현: `infrastructure-module/src/test/.../architecture/LayerRulesTest`. 같은 모듈의 수제 가드 `QueryResultRecordVisibilityTest`·`EmbeddedRecordComponentOrderTest`와 역할이 겹치지 않는다 — 이쪽은 **계층 방향**만 본다.

## 인바운드 포트 ArchUnit 규칙 6종 (완전 매핑 컴파일 게이트)

3개 api 모듈(`web-api`/`admin-api`/`ceo-api`)의 `LayerRulesTest`에 아래 6종을 둡니다. 전 컨텍스트 전환이 끝나 `..application.port.in..`에 실제 대상(437개)이 있으므로 **`allowEmptyShould(true)`를 쓰지 않습니다**.

| 규칙 | 강제 내용 |
|---|---|
| `controllersShouldDependOnUseCasesOnly` | `*ApiController`는 `*CommandService` 구체 클래스에 의존 금지. **이것이 완전 매핑의 컴파일 게이트** — 인터페이스만 만들어 두고 컨트롤러가 구체 클래스를 계속 주입하면 경계는 이름만 남습니다. (`*QueryService` 구체 주입 금지는 챕터 03) |
| `commandServicesShouldImplementUseCase` | `*CommandService`는 `..application.port.in..`의 인터페이스를 최소 1개 구현. 위 규칙의 짝으로, 그것만 있으면 "포트 없이 빈 주입만 우회"가 통과합니다 |
| `commandServicesShouldNotDependOnRequestRecords` | `*CommandService`는 `..request..` 의존 금지 |
| `commandRecordsShouldBeBoundaryTyped` | `..port.in..`은 `com.tastyhouse.domain..`·`infrastructure..`·`org.springframework.web..` 의존 금지 |
| `commandRecordsShouldNotHoldMultipartFile` | `..port.in..`의 **필드**로 `MultipartFile` 금지 |
| `portInShouldNotDependOnWebPlumbing` | `..port.in..`은 `web.bind`·`web.servlet`·`http`·`jakarta.servlet` 의존 금지 |
| `webAdaptersShouldNotDependOnApplicationServices` | (챕터 05 승격) `..adapter.in.web..`는 `..application.service..` 구체 클래스 의존 금지 — 접미어가 아닌 **패키지**로 잡아, `*Service` 같은 비표준 접미어 파사드까지 걸린다 |
| `portInShouldBeFreeOfWebDomainAndInfrastructure` | (챕터 05 승격) `..application.port.in..`의 web·domain·infra 금지를 패키지 기준 하나로 통합(예외 2건은 원본 규칙과 동일) |

### 규칙의 현재 위치 — 모듈 재편 후 인벤토리 (챕터 06 갱신)

**위 6종을 포함해 모든 레이어 규칙은 모듈 분리를 따라 이사했다.** 규칙을 찾을 때는 "무엇을 검사하는가"가 아니라 **"검사 대상 클래스가 어느 모듈에 사는가"**로 위치를 판단한다 — ArchUnit은 자기 모듈의 클래스만 볼 수 있기 때문이다.

| 테스트 위치 | 규칙 수 | 대상 |
|---|---|---|
| `{web,admin,ceo}-application/.../architecture/LayerRulesTest` | 각 12 | CQRS 서비스·Command record·`port/in` (api 모듈에서 **이동한 10종** + 신설 2종) |
| `batch-application/.../architecture/LayerRulesTest` | 8 | `*SchedulerService`·잡 UseCase·response record |
| `{web,admin,ceo}-api/.../architecture/LayerRulesTest` | web 12 / admin·ceo 각 12 | 컨트롤러·Request record + **모듈 전역 domain-free·부트스트랩 포트 주입** (어댑터가 지킬 것만 **잔류**) |
| `batch-module/.../architecture/LayerRulesTest` | 2 | `@Scheduled` 트리거 |
| `infrastructure/persistence/.../architecture/LayerRulesTest` | 5 | infra 계층 방향(아래 절) |
| `domain-module/.../architecture/{DomainPurityTest,ContextBoundaryTest}` | — | 프레임워크 순수성 · 컨텍스트 수평 경계 |
| `{web,admin,ceo,batch}-application/.../architecture/RuleAnchorTest` | 각 4 | 공허 통과 검출(아래) |

**분리로 비로소 표현 가능해진 신설 규칙 3종** — 한 모듈에 두 계층이 함께 있을 때는 컨트롤러가 정당하게 서블릿 타입을 쓰므로 모듈 전역 금지를 걸 수 없었다:

| 신설 규칙 | 위치 | 강제 내용 |
|---|---|---|
| `applicationMustBeServletFree` | `{앱}-application` 4곳 | 모듈 전체 ✗ `jakarta.servlet..`·`org.springframework.web..`. **batch만 `MultipartFile` carve-out 없이 완전 servlet-free**(HTTP 경계·업로드가 없어서) |
| `applicationMustNotDependOnAdapters` | `{앱}-application` 4곳 | ✗ `com.tastyhouse.{webapi,adminapi,ceoapi,batch}..` (역참조 금지) |
| `apiModuleMustNotContainApplicationLayer` | `{web,admin,ceo}-api` 3곳 | api 모듈에 `@Service` 빈 재등장 금지. 짝 규칙 `restControllersShouldResideInWebAdapterPackage`가 `@RestController` 위치를 `..adapter.in.web..`으로 고정 |
| `apiModuleShouldBeDomainModelFree` | `{web,admin,ceo}-api` 3곳 | 모듈 전역 ✗ `com.tastyhouse.domain..` — **carve-out 3종**: `domain.exception..`(횡단 관심사이며 `api-common-module`이 `api` 스코프로 공용 노출) · `domain.shared.page..`(챕터 06 — 컨트롤러가 `PaginationResponse.from(PageResult)`로 조립하는 것이 정상 경로) · **도메인 enum**(챕터 07 — `JavaClass#isEnum()` **타입 성격 술어**. `..model..` 패키지 술어는 금지 — enum 76개가 애그리거트 루트와 같은 패키지에 살아 `Shop`·`Order`까지 열린다). enum carve-out은 짝 규칙 `apiModuleShouldOnlyReadDomainEnums`가 호출 메서드를 accessor로 제한하는 것이 **채택 조건**이다. 기존 규칙들이 `*ApiController` 접미어·`..request..`로 좁혀 무검사였던 `config..`·`security..`·`exception..` 사각지대를 봉인한다. **batch-module에는 두지 않는다** — 컨트롤러·`config..`가 없고 클래스가 `@Scheduled` 트리거 7개 + 부트스트랩뿐이라 기존 `schedulersShouldDependOnUseCasesOnly`가 이미 포트 주입을 강제하며, 그 모듈은 "대상을 잃은 규칙은 공허 통과를 열지 말고 삭제한다"는 방침으로 규칙 4개를 이미 지운 곳이다 |
| `apiModuleShouldOnlyReadDomainEnums` | `{web,admin,ceo}-api` 3곳 | **챕터 07 신설 짝 규칙** — 도메인 enum에 호출 가능한 메서드를 읽기 accessor 3종(`name`·`getDescription`·`getDisplayName`)으로 제한한다. 위 규칙이 **타입 수준**에서 뚫은 구멍을 **메서드 수준**에서 막는다(`commandRecordsShouldNotHoldMultipartFile` 선례와 같은 구조). 도메인 enum 76개 중 **13개가 비즈니스 로직을 노출**하므로(`MemberGrade#fromReviewCount`·`OrderStatus#canTransitionTo` 등) 타입 성격 술어만으로는 컨트롤러가 등급 계산·전이 인가를 해도 통과한다. **허용 목록은 바이트코드 그래프 실측에서 도출했고 늘리지 않는다** |
| `domainBoundaryPredicatesShouldStillBite` | `{web,admin,ceo}-api` 3곳 | **챕터 07 신설** — 위 두 규칙이 현재 위반 0건이라 carve-out을 잘못 넓혀도 조용히 통과하므로, 동일 술어를 조립해 애그리거트 루트가 여전히 금지 대상인지와 **설계 전제**(enum이 `..model` 패키지에 애그리거트와 공존)를 상시 단정한다 |
| `seedersShouldDependOnUseCasesOnly` | `{admin,ceo}-api` 2곳 | `..config..`는 `{앱}application..service..` 구체 클래스 의존 금지. `webAdaptersShouldNotDependOnApplicationServices`(`..adapter.in.web..` 한정)의 사각지대 보완. 시더가 없는 web-api·`config..`가 없는 batch-module에는 두지 않는다(공허 통과 회피) |
| `applicationShouldNotDependOnSwagger` | **`{admin,ceo,web}-application` 3곳** | **챕터 06 신설(admin) → 09(ceo)·10(web) 확대 완료** — 모듈 전체 ✗ `io.swagger..`. "유스케이스 계층은 API 문서화 도구를 알지 않는다(Response 조립은 api 모듈 담당)". 세 모듈 모두 `io.swagger` import 0건이며 이 규칙이 그 상태를 고정한다 |
| `applicationShouldNotDependOnApiCommon` | **`{admin,ceo,web}-application` 3곳** | **챕터 06 신설(admin) → 09(ceo)·10(web) 확대 완료** — 모듈 전체 ✗ `com.tastyhouse.apicommon..`. `PaginationResponse`·`ApiResponse` 같은 HTTP 래퍼는 표현 계약이므로 유스케이스 계층이 조립하지 않는다(application은 `PageResult`를 반환하고 컨트롤러가 감싼다). **ceo·web은 `build.gradle`의 api-common 의존 제거가 챕터 11의 몫이라 지금은 이 규칙이 유일한 방어선이다**(admin은 의존 제거가 1차 방어선인 이중화 상태) |

**`allowEmptyShould(true)`는 리포 전체에서 0건이며, 새로 도입하지 않는다.** `noClasses().that()...`은 대상이 0건이어도 조용히 통과하므로, 규칙이 대상을 잃으면 공허 통과를 여는 대신 **규칙을 지우거나 anchor를 고친다** — 이번 재편에서 batch-module 쪽 규칙 4개를 삭제한 것이 그 선례다(대상 클래스가 전부 `batch-application`으로 떠났다).

- **`RuleAnchorTest`가 이 원칙을 자동 검증한다**: 각 규칙의 anchor 개수(`*CommandService`·`*QueryService`·`..port.in..`·모듈 전체)를 직접 세어, 클래스가 모듈 사이를 옮겨 다니다 대상이 통째로 사라지면 빌드를 실패시킨다. web/admin/ceo는 컨텍스트가 늘어나는 것이 정상이라 **하한**(`isGreaterThanOrEqualTo`)으로, batch는 규모가 작아 일치로 검사한다.
- **다만 anchor는 falsifiability를 보증하지 않는다**: 그것이 세는 것은 "규칙이 대상 클래스를 갖는가"이지 "규칙이 실패할 수 있는가"가 아니다. 모듈 분리 후 상당수 규칙은 **금지 대상 패키지가 클래스패스에 아예 없어** 위반 코드가 애초에 컴파일되지 않는 **휴면** 상태다 — 계층을 실제로 강제하는 것은 ArchUnit이 아니라 build.gradle이고(그것이 모듈 분리의 요점이다), 휴면 규칙은 누군가 의존 한 줄을 되돌리는 **회귀를 잡는 2차 방어선**이다. 이 구분은 `batch-application/AGENTS.md`가 규칙별 상태표로 기록해 두었다.

**규칙 4와 5가 나뉜 이유**: ArchUnit 의존 그래프는 같은 패키지의 **UseCase 인터페이스 메서드 파라미터**까지 잡습니다. 업로드 연산은 `method(XxxCommand, MultipartFile)`이 규정된 형태이므로(§6) 규칙 4에서 `org.springframework.web.multipart..`를 예외로 빼고, §6이 실제로 금지하는 것(Command **필드**로 싣기)은 규칙 5가 따로 막습니다.

### 세 규칙이 동시에 걸리는 지점 — multipart 문자열 파트의 파싱 위치

`ShopStorePriceVerificationApiController`의 `items`(JSON 배열 문자열 파트)처럼 **전송 형식을 풀어야 하는 값**은 파싱 위치가 규칙 셋에 동시에 걸립니다.

- 컨트롤러에서 파싱 → `controllersShouldBeDomainFree` 위반(파싱 실패를 `BusinessException`으로 번역해야 함)
- Request record에서 파싱 → `requestResponseRecordsShouldBeDomainAndInfraFree` 위반(같은 이유)
- 서비스에서 Request를 받아 파싱 → `commandServicesShouldNotDependOnRequestRecords` 위반(§5)

**셋을 모두 만족하는 유일한 형태는 Command가 원문 문자열을 경계 타입 `String`으로 담아 넘기고, 서비스가 그것을 중첩 Command로 파싱하는 것입니다.** 파싱 실패와 빈 목록이 같은 `ErrorCode`로 나가던 계약도 이때 그대로 보존됩니다(둘 다 서비스가 던짐). 비슷한 multipart 파트를 새로 만들 때 이 형태를 따릅니다.

## application 서비스 CQRS 분리 규칙 (`{도메인}CommandService`/`{도메인}QueryService`)

**`{앱}-application` 모듈의 서비스는 도메인당 `{도메인}CommandService`(쓰기)와 `{도메인}QueryService`(읽기) 두 클래스로 분리한다.** 과거 core-module `application/`의 단일 서비스는 명령과 조회를 한 클래스에 담아 트랜잭션 속성(`readOnly` 여부)이 메서드마다 갈리고, 쓰기 경로가 필요 없는 조회에도 write 포트가 주입돼 의존이 과했다. 클래스 단위로 나누면 트랜잭션 속성이 클래스 하나에 일관되게 걸리고, 주입 대상이 곧 그 클래스의 역할을 증명한다.

- **위치 — 완전 매핑 패키지 배치 (개정)**: 과거 이 절은 "`..application..` 패키지를 만들지 않는다"였다. **그 규칙을 폐기하고** 각 api 모듈의 컨텍스트 아래를 아래 3층으로 배치한다. 계층 판별을 클래스명 접미어에만 의존하던 것을 패키지 구조로 끌어올려, 인바운드 어댑터와 application을 눈으로 구분할 수 있게 한다.

  ```
  {앱}-api/         <ctx>/adapter/in/web/   컨트롤러 + request/
  {앱}-application/ <ctx>/port/in/          UseCase 인터페이스 + Command record
  {앱}-application/ <ctx>/service/          서비스 구현
  {앱}-api/         <ctx>/adapter/in/web/response/  표현 계약 — 3개 앱 전부(admin 챕터 06 · ceo 챕터 09 · web 챕터 10)
  ```

  **모듈 재편(챕터 01~05)으로 계층 경계가 패키지가 아니라 모듈이 됐다.** 과거 이 3층은 한 api 모듈 안의 `<ctx>/application/..` 하위 패키지였으나, 지금은 api 모듈과 `{앱}-application` 모듈로 물리 분리되어 있다 — 그래서 경로에 `application` 세그먼트가 없다(모듈명이 그 역할을 한다). `request/`는 매핑이 어댑터 책임이므로 api 쪽에 있다. **`response/`의 거처는 챕터 06(admin)·09(ceo)·10(web)으로 바뀌었다** — 원래는 QueryService가 조립하므로 application 쪽이었으나, 유스케이스 계층에서 Swagger·HTTP 표현을 걷어내려고 api 모듈로 올렸다(3개 앱 완료). 그 결과 admin·ceo·web-application 세 모듈 모두 `io.swagger` import가 0건이다.

  **worked example — `ceo-api`의 shop 컨텍스트(영업시간)**: 한 연산이 네 파일에 어떻게 흩어지는지의 최종형이다.

  | 역할 | 실제 경로 |
  |---|---|
  | 인바운드 어댑터 | `ceo-api/.../ceoapi/shop/adapter/in/web/ShopBusinessHourApiController.java` |
  | 인바운드 포트 + Command | `ceo-application/.../ceoapplication/shop/port/in/ShopBusinessHourCommandUseCase.java`·`ShopBusinessHourCreateCommand.java` |
  | 서비스 구현 | `ceo-application/.../ceoapplication/shop/service/ShopBusinessHourCommandService.java` |
  | 읽기 포트(아웃바운드) | `{web,ceo}-application/.../application/shop/port/out/ShopQueryPort.java`(소비 앱 수에 따라 소유 모듈이 갈린다) |

  컨트롤러는 `ShopBusinessHourCommandUseCase`만 주입하고 `..ceoapplication..service..`를 알지 않으며(`webAdaptersShouldNotDependOnApplicationServices`, 그리고 모듈 분리 후에는 `apiModuleMustNotContainApplicationLayer`가 이 모듈에 서비스가 다시 생기는 것 자체를 막는다), 조회 측 `ShopBusinessHourQueryService`는 `infrastructure:persistence`의 DAO가 아니라 `ShopQueryPort` 인터페이스를 주입한다.

  **클래스명은 그대로 `{도메인}CommandService`/`{도메인}QueryService`를 유지한다** — 기존 ArchUnit 규칙들이 접미어로 대상을 매칭하므로 이름을 바꾸면 규칙이 조용히 대상을 잃는다. 이번 전환에서 바뀌는 것은 **패키지 위치와 `implements` 추가**뿐이다.
- **역할과 주입 대상**:
  - `{도메인}CommandService` — `@Transactional`. 해당 **`{도메인}CommandUseCase`를 implements**한다. **Command를 수신**해 도메인 타입으로 승격(`XxxId.of`/`Enum.from`)하고, domain-module의 **write 포트**(`{도메인}Repository`)와 **순수 POJO 도메인 서비스**(`<ctx>/service/`)를 주입한다. 도메인 변경 후 [명시적 save](#도메인-모델-jpa-엔티티-분리-규칙-선별-적용-persistence는-infrastructure-module로)를 호출한다.
  - `{도메인}QueryService` — `@Transactional(readOnly = true)`. 해당 **`{도메인}QueryUseCase`를 implements**한다. infrastructure DAO가 아니라 **`{Ctx}QueryPort`를 주입**하며(위 [읽기 경로 포트화](#api-모듈-querydslinfra-전면-금지-규칙-archunit-강제--챕터-04로-완료) 참조), **`*Result`를 그대로 반환하고 Response 조립은 api 모듈의 Response record가 한다**(이 서비스에 매퍼가 없다 — admin 챕터 06 · ceo 챕터 09 · web 챕터 10으로 3개 앱 동일).
- **컨트롤러는 UseCase 인터페이스만 주입한다**: 컨트롤러 생성자에 `application/service/`의 구체 클래스가 등장하지 않는다. 이것이 인바운드 포트 도입의 실익인 컴파일 게이트가 실제로 작동하는 지점이다.
- **`MultipartFile`은 서비스 파라미터로만 허용하고 Command 필드로는 금지한다**: 업로드 자체를 받는 경계 타입이라 서비스 시그니처에 남기는 것은 존치하되(파일 업로드 흐름을 재설계하지 않기 위함), **Command에는 업로드 결과 참조**(파일 식별자·URL)만 담는다. Command가 서블릿 업로드 타입을 보유하면 application 계층이 web 플럼빙에 결합되고, 직렬화·재실행이 불가능해진다.
- **서로의 의존을 교차 주입하지 않는다**: **CommandService는 `..query..`를 주입하지 않고, QueryService는 write 포트를 주입하지 않는다.** 이 두 금지가 CQRS 분리를 실제로 지탱하는 지점이다 — 한쪽이라도 허용하면 클래스는 둘로 나뉘었지만 의존 그래프는 여전히 하나로 뭉쳐 있어, 조회 트랜잭션에서 쓰기가 일어나거나 명령 경로가 표현용 투영에 결합되는 것을 막을 수 없다. 명령 처리 후 응답이 필요하면 **명령은 식별자만 반환하고 컨트롤러가 QueryService로 재조회**한다.
- **조회만 있는 도메인은 QueryService만 둔다**: 쓰기 경로가 없는 도메인(공개 조회 전용 등)에 빈 `CommandService`를 만들지 않는다. 반대로 쓰기만 있는 경로도 `CommandService` 하나만 둔다 — "도메인당 2개"는 상한이 아니라 **역할이 존재할 때의 이름 규칙**이다.
- **모듈 간 같은 이름이 공존하는 것은 정상이다**: `web-api`와 `admin-api`가 각각 `NoticeQueryService`를 갖는다(패키지가 달라 충돌하지 않음). 소비자가 다르면 조회 범위·응답 형태가 다르므로 통합하지 않는다.

reference 구현: `admin-api`의 `notice/NoticeCommandService`(write 포트 `NoticeRepository` 주입, 변경 후 명시적 `save`)·`admin-application`의 `notice/NoticeQueryService`(`NoticeQueryPort` 주입. **챕터 06 이후 Response 조립을 하지 않고 `*Result`를 반환한다** — private 매퍼가 남아 있는 것은 ceo·web뿐이다), `web-api`의 `notice/NoticeQueryService`(조회 전용이라 CommandService 없음)·`faq/FaqQueryService`(같은 이유), `web-api`의 `reservation/ReservationCommandService`(도메인 서비스 `ReservationBookingService`를 경유하는 명령 — 재시도 루프 때문에 트랜잭션 경계를 별도 `ReservationBookingExecutor`가 갖는 예외 형태).

## admin/web Result 충돌 시 `Management` 한정어 상시 적용 규칙

**admin 전용 Result와 web(비-admin) Result가 같은 `port.out` 패키지에서 이름이 충돌하면, `Management` 한정어를 붙여 구별한다 — 이는 상황에 따른 선택이 아니라 상시 적용 규칙이다.** 조회 계약이 `com.tastyhouse.application.<ctx>.port.out` 한 패키지로 모였으므로(과거 `infrastructure:persistence`의 `<ctx>/query/`, 챕터 04로 이관) **admin/web Result는 이제 항상 같은 패키지에 공존**한다. 따라서 과거에 있었던 "모듈이 분리되면 충돌이 사라지니 한정어를 제거할 수 있다"는 여지는 **폐지한다** — 패키지가 같아지는 방향으로 구조가 확정됐으므로 한정어는 영구적이다.

- **네이밍 형태**: `{도메인}Management{용도}Result`. `Management`는 "누가(관리자)"가 아니라 **"무엇을 위한 것인가(관리 화면 목록/상세)"** 를 표현하므로, [admin 전용 네이밍 규칙](#admin-전용-네이밍-규칙-메서드타입명에-admin-flavor-admin-접두접미중간어-금지)이 금지하는 역할 마커 `Admin`을 쓰지 않으면서 admin 성격을 이름에 담을 수 있다.
- **충돌하지 않으면 순수명을 쓴다**: 비-admin 형제가 없으면 한정어 없이 순수 도메인명을 쓴다(예: `MemberListItemResult`, `BugReportListItemResult`). 한정어는 충돌 해소 수단이며 admin 여부의 표식이 아니다.
- **필드 셋이 다른 admin/web Result는 통합하지 않는다**: 이름이 비슷하다고 두 Result를 하나로 합쳐 상위집합 필드를 갖게 만들지 않는다. admin 목록에만 필요한 필드(비노출 여부·내부 상태·감사 시각 등)를 web 응답 경로에도 흘려보내면 **과잉 노출**이 되고, 어느 필드가 어느 화면 계약인지 추적할 수 없게 된다. 필드 셋이 다르면 별도 record로 유지하고, 각각 자기 소비자가 실제 쓰는 필드만 갖는다([query DAO·QueryPort 소유 규칙](#query-daoqueryportresult-dtosearchcondition-소유-규칙-개정--읽기-계약은-소비-앱이-소유-구현은-infrastructurepersistence)의 "실제 쓰는 필드만 이관"과 동일 취지).
- **`SearchCondition`도 같은 원칙**: admin/web 검색 조건이 충돌하면 동일하게 `Management`로 구별하고, 조건 필드가 다르면 통합하지 않는다.

reference 구현: `com.tastyhouse.application.faq.port.out`(`FaqCategoryManagementResult` vs web용 `FaqCategoryResult`, `FaqManagementListItemResult` — 같은 패키지 공존이 확정된 사례), `com.tastyhouse.application.event.port.out`(`EventManagementListItemResult`/`EventManagementDetailResult` vs `EventListItemResult`/`EventDetailResult`), `com.tastyhouse.application.order.port.out`(`OrderManagementListItemResult` vs `OrderListItemResult`), `com.tastyhouse.application.notice.port.out`(`NoticeManagementListItemResult` vs `NoticeListItemResult`). 충돌 없어 순수명을 쓴 사례: `member`/`bug`/`coupon` 도메인의 `MemberListItemResult`/`BugReportListItemResult`/`CouponListItemResult`.

## 조회 포트 소비자별 분할 규칙 (포트명은 반환 Result 계열을 승계 — 챕터 04)

**한 조회 포트에 여러 앱의 조회가 섞여 있으면 앱별 인터페이스로 쪼개고, 각 앱이 자기 계약만 소유한다.** 챕터 04에서 공유 포트 22개를 48개로 분할했다(전체 47 → 73개). "포트를 공유한다"는 것은 대부분 **타입만 같을 뿐 서로 다른 메서드를 쓰고 있다는 뜻**이었다 — `EventQueryPort`·`FaqQueryPort`·`BannerQueryPort`·`NoticeQueryPort`는 공유 메서드가 0개였고, `ProductQueryPort`는 35개 중 회원 12·관리자 5·점주 15가 각자 다른 메서드를 쓰고 있었다.

- **포트명은 그 포트가 반환하는 `Result` 계열의 이름을 승계한다.** Result는 이미 위 [`Management` 한정어 규칙](#adminweb-result-충돌-시-management-한정어-상시-적용-규칙)으로 앱별 충돌이 해소돼 있으므로, 포트가 그것을 따르면 **새 규칙 없이 충돌이 재발하지 않는다**. 예: `findAllBanners`가 `BannerManagementListItemResult`를 반환하므로 그 포트는 `BannerManagementQueryPort`다.
- **`Management`는 여기서도 "무엇을 위한 것인가(관리 화면)"** 이지 "누가"가 아니다. 따라서 admin과 ceo 어느 쪽이든 관리 화면 계약이면 쓸 수 있다(선례: ceo의 `ShopReviewManagementQueryPort` vs admin의 `ReviewManagementQueryPort` — 도메인 접두로 구분).
- **형제가 이미 `Management`를 점유했으면 `Owner`로 구별한다.** admin 계약이 `*ManagementResult`를 반환해 `Management`를 쓰고 있는데 ceo(점주 관리 화면) 계약도 필요하면, 소유 주체를 담은 `Owner`를 쓴다(예: `ShopNoticeManagementQueryPort`(admin) vs `ShopNoticeOwnerQueryPort`(ceo), `ProductManagementQueryPort`(admin) vs `ProductOwnerQueryPort`(ceo)). **`Admin`·`Ceo` 같은 역할 마커는 금지**가 그대로 유지된다.
- **공유 메서드는 양쪽 인터페이스에 선언만 중복한다.** DAO 구현은 하나를 공유하므로 투영 코드가 복제되지 않으며, 유지비는 선언에 한정된다. 중복 선언에는 `/** 공유 메서드 — {@link 상대포트}에도 같은 시그니처로 선언돼 있다. */` 주석을 단다.
- **겹침이 압도적이면 쪼개지 않는다.** 두 앱이 쓰는 메서드가 거의 같으면 쪼갠 인터페이스가 사실상 같아져 조회 하나를 고칠 때 두 파일을 고쳐야 한다. 이때는 **소유자를 앱이 아니게** 만든다 — 성격을 담은 이름의 공용 포트 하나로 두고 여러 앱이 함께 의존한다(예: `ShopBasicInfoQueryPort`, `ShopSearchManagementQueryPort`). 이런 공용 포트는 챕터 05에서 `domain-module`로 이동했다.
- **구현이 서로 다른 DAO면 선언 중복 대신 공유분을 별도 포트로 뗀다.** 선언을 중복하면 그 계약을 구현하는 DAO마다 본문이 필요한데, 두 DAO가 서로 다른 빈이면 투영 코드가 복제된다(사례: `ReviewTagQueryPort` — admin이 태그 2건 때문에 회원 화면용 `ReviewQueryPort`를 통째로 주입하던 것을 해소).
- **DAO는 분할된 포트를 전부 `implements`하며 본문은 바뀌지 않는다.** 분할 전후로 `@Override` 개수가 같아야 한다(`grep -c '@Override' {DAO}`로 대조). 메서드 누락은 `implements` 미구현으로 컴파일 에러가 나므로 기계적으로 드러난다.
- **application 소비자가 없는 조회는 포트에 두지 않는다.** infra 내부에서만 쓰는 조회는 DAO의 평범한 public 메서드로 남긴다(사례: `ShopQueryDao#findShopName` — 유일한 소비처인 `ReviewOwnerReplyEventListener`가 같은 모듈에서 구체 타입으로 주입한다. `MemberReviewCountQueryPort` 선례와 같은 취급).

**챕터 09 이후 — 이 규칙이 곧 계약의 소유 모듈을 정한다.** `application-common-module`이 해체되며 "포트를 쪼갠다"는 판정이 파일의 물리적 위치까지 결정하게 됐다.

| 이 규칙의 판정 | 계약이 사는 곳 |
|---|---|
| 앱별로 쪼갠 단독 포트 | 그 앱의 `{앱}-application` |
| "겹침이 압도적"이라 쪼개지 않은 공용 포트 | `domain-module` |

따라서 **포트를 쪼갤지 말지는 이제 이름 문제가 아니라 모듈 배치 문제다.** 잘못 판정해 단독 계약을 `domain-module`에 두면 그 모듈이 표현 투영의 쓰레기통이 되고, 공유 계약을 한 앱 모듈에 두면 다른 앱이 그 모듈을 의존하는 **앱 간 수평 의존**이 생긴다. 새 계약을 추가하기 전에 **소비 앱 수를 먼저 센다**.

**분할 후 남은 공유 포트는 의도적으로 남긴 것뿐이다** — `ShopBasicInfoQueryPort`(3앱)·`ShopDeliveryTipQueryPort`(web+ceo)·`ReviewTagQueryPort`·`ShopOrderNoticeManagementQueryPort`·`ShopSearchManagementQueryPort`(각 2앱) 5개이며, 전부 위 "겹침이 압도적" 판정을 거쳤고 챕터 05에서 `domain-module`로 간다. **그 밖에 어떤 앱도 다른 앱의 계약을 주입하지 않는다.**

reference 구현: `com.tastyhouse.application.shop.port.out`(`ShopQueryPort`/`ShopBasicInfoQueryPort`/`ShopManagementQueryPort`/`ShopOwnerQueryPort` — 35개 메서드를 4분할한 최대 사례), `com.tastyhouse.application.product.port.out`(`ProductQueryPort`/`ProductManagementQueryPort`/`ProductOwnerQueryPort`), `com.tastyhouse.application.review.port.out`(`ReviewTagQueryPort` — 구현 DAO가 달라 별도 포트로 뗀 사례).

## 모듈 경계 규칙 (계층 × 앱 2차원 + 기술별 infrastructure)

**모듈 경계는 챕터 01~05를 거쳐 "계층 × 앱" 2차원이 됐고, `infrastructure`는 기술별로 나뉘었다.** 아래가 현재 배치 기준이다.

| 두어야 할 곳 | 기준 |
|---|---|
| `domain-module` | 도메인 모델·VO·write 포트·불변식(순수 POJO, 프레임워크-프리) |
| `{web,admin,ceo,batch}-application` | 유스케이스 — `<ctx>/port/in/` + CQRS 서비스 (**`response/`는 없다 — 3개 앱 전부 api 모듈 소유**). **infra를 컴파일 클래스패스에 두지 않는다** |
| `{web,admin,ceo}-api` · `batch-module` | 인바운드 어댑터 — 컨트롤러/`@Scheduled` 트리거 + `request/` + config·security 정책 + 부트스트랩 |
| `infrastructure:persistence` | domain write 포트의 JPA 어댑터 + `{Ctx}QueryPort` 구현 DAO + `<ctx>/listener` + 도메인 서비스 빈 등록 |
| `infrastructure:redis` | Redis 연결·`StringRedisTemplate` + rate limiting. **domain조차 모른다**(포트가 없는 순수 기술) |
| `external-api` | domain `<ctx>/port/` 구현(OAuth·PG·메일·SMS·파일·크롤링) |
| `security-core` | (챕터 03) 여러 앱이 공유하는 **서블릿-프리 보안 코어** — `JwtTokenProvider`(서명/파싱) + Redis 토큰 저장소 6종. `{web,admin,ceo,batch}-application`이 서블릿 스택 없이 의존할 수 있는 대상 |
| `security-module` | 여러 앱이 공유하는 **서블릿 결합 보안** 관심사 — JWT 인증 필터·`JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`. `security-core`를 `api`로 재노출 |
| `api-common-module` | 여러 앱이 공유하는 **HTTP 플럼빙** — `ApiResponse`·`PaginationResponse`·`PageRequest`·`FileService` |

- **infrastructure를 기술별로 나눈 이유 (챕터 05)**: `infrastructure:persistence` 하나가 "infrastructure = DB"라는 암묵 전제를 만들고 있었다. Redis는 보안 관심사가 아니라 인프라 기술인데 `security-module`이 연결·템플릿까지 들고 있어서, Redis를 쓰려는 다른 관심사가 전부 보안 모듈을 의존해야 했다. 이제 **순수 인프라 기술이면 `infrastructure:{기술}`**에 두고, `security-module`에 남는 기준은 "Redis를 쓰는가"가 아니라 **"보안 관심사인가"**다(토큰 저장소는 잔류, rate limiting은 이관).
- **컨텍스트별 분할은 하지 않는다**: 25개 컨텍스트의 수평 경계는 모듈이 아니라 `domain-module`의 ArchUnit `ContextBoundaryTest`(봉인 목록)가 담당한다. 위 [인바운드 포트 절의 개정 이력](#인바운드-포트usecase-인터페이스를-도입한다--완전-매핑-전략-채택-과거-결정의-명시적-번복) 참고.

### 역사적 근거 — 도메인 포트 없는 공유 기술을 왜 infrastructure에 두지 않았나

**`infrastructure:persistence`는 domain-module write 포트의 DB persistence 어댑터와 CQRS read 측(`<ctx>/query/` DAO — 읽기 계약 패키지가 선언한 `{Ctx}QueryPort`의 구현체)으로 역할이 한정된다.** api 모듈들이 공유하지만 domain-module에 대응 포트가 없는 기술 인프라(Redis 기반 인증 세션·rate limiting 등)를 이 모듈에 넣지 않는다. 대신 그 관심사만을 위한 별도 공유 모듈을 신설한다.

- **판별 기준**: 어떤 공유 코드를 "인프라 어댑터"로 볼지는 **domain-module에 대응 포트(인터페이스)가 있는가**로 가른다. `external-api`가 domain-module의 `MailSender`(`mail/port/`)·`SmsSender`(`sms/port/`)·`FileStoragePort`(`file/port/`)를 구현하듯, 어댑터는 도메인 포트를 구현할 때만 성립한다. Redis 기반 JWT 세션·rate limit은 domain-module에 아무 개념도 없다(순수 presentation/보안 관심사) — 이런 코드를 `infrastructure:persistence`에 넣으면 "DB persistence 어댑터 + query DAO"라는 모듈 정체성이 흐려진다. **다만 챕터 05 이후 결론이 한 단계 갱신됐다** — 도메인 포트가 없는 기술이라고 무조건 `security-module`류 공유 모듈로 가는 것이 아니라, **순수 인프라 기술이면 `infrastructure:{기술}`**에 둔다(rate limiting이 실제로 그렇게 이동했다).
- **은닉 원칙과의 충돌 방지**: 이 분리를 결정할 당시 `infrastructure:persistence`(현 `infrastructure:persistence`)은 API 모듈에 `runtimeOnly`로 의존되어 컴파일 타임 은닉이 강제되고 있었다. 그런데 `TokenService`류가 Redis 저장소를 **구체 클래스로 컴파일 타임 직접 주입**하는 구조이므로, 이런 코드를 `runtimeOnly` 모듈에 두면 컴파일이 깨진다. 이걸 억지로 맞추려면 (a) domain-module에 해당 포트를 선언(도메인 오염) 하거나 (b) API 의존을 `implementation`으로 승격(은닉 포기)해야 하는데, 둘 다 당시 규칙을 깨는 선택이었다 — 그래서 별도 공유 모듈(`security-module`)로 뺐다.
  - **(현행 실태 — 읽기 경로 포트화로 재개정)** 챕터 03까지는 `{도메인}QueryService`가 infra `<ctx>/query/`의 `{도메인}QueryDao`를 컴파일 타임에 직접 주입했고, 4개 api 모듈(web/admin/ceo/batch)이 모두 `implementation project(':infrastructure:persistence')`(현 `:infrastructure:persistence`)로 의존했다. 챕터 04(읽기 경로 포트화)로 그 직접 주입이 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스 주입으로 바뀌면서, **api 모듈은 `com.tastyhouse.infrastructure..`를 전혀 import하지 않는다.** 은닉은 의존 스코프가 아니라 **ArchUnit 규칙**이 담당한다: `..infrastructure..persistence..`(write 어댑터)·`com.querydsl..`·`com.tastyhouse.infrastructure..query..` 전부 금지(위 [api 모듈 QueryDSL·infra 전면 금지 규칙](#api-모듈-querydslinfra-전면-금지-규칙-archunit-강제--챕터-04로-완료)). 따라서 위 문단은 security-module 분리의 **역사적 근거**로만 읽고, 현재의 의존 스코프 판단 기준으로 삼지 않는다. 다만 결론(도메인 포트가 없는 공유 기술은 별도 모듈로 뺀다)은 스코프와 무관하게 그대로 유효하다.
- **해결책**: 도메인 포트가 없는 api 모듈 공유 기술은 `implementation`으로 의존되는 별도 공유 모듈(`java-library`, bootJar 비활성)로 분리한다. 이 모듈은 도메인 어댑터가 아니라 presentation 공유 유틸이므로 `implementation` 노출이 자연스럽고, `TokenService`의 구체 클래스 직접 주입 구조를 그대로 유지할 수 있다.
- **설정값도 같은 패턴**: 이 공유 모듈이 실제로 소비하는 설정값(예: `spring.data.redis`)은 모듈 자신의 `application-{모듈}.yml`이 소유하고, web-api/admin-api가 `spring.config.import`로 로딩한다(`application-infrastructure.yml`/`application-external.yml`과 동일한 기존 패턴). **로깅 설정도 이 패턴을 그대로 따른다** — `logging-module`이 `application-logging.yml`(콘솔 패턴·root/`com.tastyhouse.logging` 레벨·p6spy 로그 포맷)을 소유하고, web/admin/ceo-api가 `classpath:application-logging.yml`을 import한다. 과거 3개 실행 모듈 `application.yml`에 복제돼 있던 `logging:` 블록과 모듈별 `spy.properties`(p6spy)를 이 하나로 통합한 것으로, `com.tastyhouse.logging` 레벨은 `${API_BODY_LOG_LEVEL:DEBUG}`로 환경변수화(운영에서 코드 수정 없이 INFO 전환)했고 p6spy 의존은 `logging-module`이 `api`로 노출한다(batch-module은 HTTP 요청이 없어 `exclude`로 전이 차단, `application-logging.yml`도 import하지 않고 자체 `logging:` 유지). 상세는 `logging-module/AGENTS.md` 참고.

reference 구현: `security-module` — Redis 기반 `RedisConfig`(StringRedisTemplate 빈)·rate limiting(**챕터 02 이후 재배치됨** — `@RateLimit`·`RateLimitAspect`·`RateLimitException`·계약 `RateLimitCounterPort`는 `api-common-module`, Redis 카운터 `RedisRateLimitCounter`는 `infrastructure:redis` 소유)·`RefreshTokenRedisRepository`/`BlacklistRedisRepository`(접두사 생성자 주입형 — web은 `"rt:"`/`"bl:"`, admin은 `"admin:rt:"`/`"admin:bl:"`를 각자 `RedisRepositoryConfig`에서 주입)·소셜 임시토큰 저장소 4종을 web-api·admin-api 중복 없이 통합했다. `implementation project(':security-module')`로 의존해 `TokenService`의 구체 클래스 직접 주입이 그대로 컴파일된다. 설정값은 `security-module/application-security.yml`이 소유. 상세는 `security-module/AGENTS.md` 참고.

이후 **JWT 인증 메커니즘도 이 모듈의 `com.tastyhouse.security.jwt`로 통합**했다(과거 web-api/admin-api `config/jwt`·`config/security`에 사실상 동일하게 복제돼 있던 `JwtTokenProvider`/`JwtAuthenticationFilter`/`JwtProperties`/`TokenType`/`JwtAuthenticationEntryPoint`/`JwtAccessDeniedHandler` 12개 파일 제거). 공용 `JwtTokenProvider`는 `@Component`가 아닌 파라미터형 POJO로, principal 식별자 클레임명(`memberId`/`adminId`)과 principal 재구성 팩토리(`JwtPrincipalFactory`)를 생성자로 받아 앱별 차이를 흡수한다 — 각 API는 이를 상속한 얇은 `@Component` 하위 클래스로 자기 등록하고(web은 검증 토큰 발급 메서드를 web 전용으로 추가), `JwtAuthenticationFilter`(POJO)는 각 API의 `config/jwt/JwtConfig`가 블랙리스트 저장소와 함께 빈 등록한다. 정책(`SecurityConfig`/`PublicPaths`/`CustomUserDetails`/`UserDetailsService`/`TokenService`/`RedisRepositoryConfig`)은 각 API에 잔류하며, `CustomUserDetails`는 `JwtPrincipal`을 구현해 식별자를 노출한다. **web-api와 admin-api의 `jwt.secret`은 반드시 서로 다른 환경변수(`JWT_SECRET_WEB` vs `JWT_SECRET_ADMIN`, ceo는 `JWT_SECRET_CEO`)를 써야 한다** — 동일 시크릿이면 회원 access 토큰이 admin 인증을 통과하는 권한 상승이 발생하므로, admin은 시크릿을 분리하고 인가 체인도 `.anyRequest().hasAnyRole("ADMIN","SUPER_ADMIN")`로 강화했다. 이 통합으로 `security-module`에 `spring-boot-starter-security`(api)·`jjwt`(api/runtimeOnly) 의존이 추가됐다.

**아래 reference 목록의 읽는 법**: 이 목록은 도메인별 전환을 진행한 순서대로 누적 기록된 것으로, **각 항목의 서술은 그 전환이 이뤄진 시점의 상태**를 담고 있다(어떤 도메인이 아직 미분리였고, 그래서 어떤 우회가 필요했는지 등). 경로 표기는 현재 기준(`domain-module/...`·`com.tastyhouse.domain.*`)으로 정정했으나, "당시 미분리였다"·"당시엔 core에 어노테이션을 유지했다" 같은 시점 서술은 역사적 기록으로서 그대로 유지한다. 또한 이 목록이 언급하는 `application` 계층 서비스·`application/dto`는 이후 [application 계층 해체](#api-모듈-application-서비스-cqrs-분리-규칙-도메인commandservice도메인queryservice)로 각 api 모듈의 CQRS 서비스와 infrastructure `<ctx>/query/`로 이관됐다.

reference 구현: `notice` 도메인 — 순수 모델 `domain-module/.../notice/model/Notice`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../notice/persistence/`(`NoticeJpaEntity`/`NoticeMapper`/`NoticeJpaRepository`/`NoticeRepositoryImpl`), 명시적 save `NoticeCommandService#updateNotice`·`#deleteNotice`, 순수 단위 테스트 `domain-module/src/test/.../notice/model/NoticeTest`. 상세는 `infrastructure/persistence/AGENTS.md`·`md/CLEAN-ARCHITECTURE.md` 참고. `admin` 도메인 — 연관관계·QueryDSL·update/soft delete가 없는 최소 CRUD(create+read) 변형 사례: 순수 모델 `domain-module/.../admin/model/Admin`(`create`/`reconstitute`, 감사 필드 미소비로 생략), 어댑터 `infrastructure-module/.../admin/persistence/`(`AdminJpaEntity`/`AdminMapper`/`AdminJpaRepository`/`AdminRepositoryImpl` — QueryDSL 없이 순수 pass-through, `save`는 update 경로가 없어 insert 전용), 순수 단위 테스트 `domain-module/src/test/.../admin/model/AdminTest`. (명시적 save 규칙은 대상 없음 — `AdminCommandService#createAdmin`이 이미 저장 시 `save` 호출.) `banner` 도메인 — enum 필드(`BannerType`, `EnumType.STRING`+`columnDefinition`)와 (당시) 미분리 도메인(`file`)의 Q타입(`QUploadedFile`)을 그대로 조인하는 크로스 도메인 QueryDSL 조회가 있는 사례: 순수 모델 `domain-module/.../banner/model/Banner`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../banner/persistence/`(`BannerJpaEntity`/`BannerMapper`/`BannerJpaRepository`/`BannerRepositoryImpl` — `QBannerJpaEntity`는 infra 생성, 당시엔 `QUploadedFile`을 core-module 생성 그대로 import했으나 이후 `file` 전환으로 `QUploadedFileJpaEntity`(infra 생성)로 치환됨), 명시적 save `BannerCommandService#updateBanner`·`#deleteBanner`, 순수 단위 테스트 `domain-module/src/test/.../banner/model/BannerTest`. `bug` 도메인 — JPA 연관관계는 없지만 **한 도메인에 두 애그리거트**(`BugReport`+`BugReportImage`, plain FK `bug_report_id`+QueryDSL 서브쿼리로만 연결)가 있는 사례, `@Convert` FK VO(`memberId`)와 enum 4개(`columnDefinition` VARCHAR)를 그대로 JpaEntity로 이관: 순수 모델 `domain-module/.../bug/model/BugReport`·`BugReportImage`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../bug/persistence/`(`BugReportJpaEntity`/`BugReportImageJpaEntity`/`BugReportMapper`/`BugReportImageMapper`/`BugReportJpaRepository`/`BugReportImageJpaRepository`/`BugReportRepositoryImpl`/`BugReportImageRepositoryImpl` — 서브쿼리 Q타입도 `QBugReportImageJpaEntity`로 치환), 더티 체킹에 의존하던 `changeStatus`/`classify`/`assign`에 명시적 save 추가(`BugReportCommandService`), 순수 단위 테스트 `domain-module/src/test/.../bug/model/BugReportTest`·`BugReportImageTest`. `faq` 도메인 — JPA 연관관계 없이 raw FK(`Long faqCategoryId`)로만 연결된 **두 애그리셋**(`Faq`+`FaqCategory`)이 있고, 한쪽 리포지토리 구현이 다른 쪽 JPA 엔티티 Q타입을 직접 조회하는 크로스 엔티티 QueryDSL 사례(`FaqCategoryRepositoryImpl#existsActiveItemsByCategoryId`가 `QFaqJpaEntity`를 조회): 순수 모델 `domain-module/.../faq/model/Faq`·`FaqCategory`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../faq/persistence/`(`FaqJpaEntity`/`FaqCategoryJpaEntity`/`FaqMapper`/`FaqCategoryMapper`/`FaqJpaRepository`/`FaqCategoryJpaRepository`/`FaqRepositoryImpl`/`FaqCategoryRepositoryImpl`), 더티 체킹에 의존하던 `updateFaq`/`deleteFaq`(`FaqCommandService`)·`updateCategory`/`deleteCategory`(`FaqCategoryCommandService`)에 명시적 save 추가, 순수 단위 테스트 `domain-module/src/test/.../faq/model/FaqTest`·`FaqCategoryTest`. `coupon` 도메인 — JPA 연관관계 없이 raw FK(`Long couponId`)와 `@Convert` FK VO(`memberId`)로만 연결된 **두 애그리거트**(`Coupon`+`MemberCoupon`)가 있고, `Coupon`만 조회 결과(`CouponDetailResult.from`)가 감사 시각을 직접 소비해 감사 필드를 보유하는 반면 `MemberCoupon`은 QueryDSL이 엔티티에서 직접 투영해 감사 필드가 불필요한 **비대칭 사례**, 두 엔티티를 join하는 `MemberCouponRepositoryImpl`이 Q타입 2개(`QCouponJpaEntity`+`QMemberCouponJpaEntity`)를 함께 치환: 순수 모델 `domain-module/.../coupon/model/Coupon`·`MemberCoupon`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../coupon/persistence/`(`CouponJpaEntity`/`MemberCouponJpaEntity`/`CouponMapper`/`MemberCouponMapper`/`CouponJpaRepository`/`MemberCouponJpaRepository`/`CouponRepositoryImpl`/`MemberCouponRepositoryImpl`), 더티 체킹에 의존하던 `updateCoupon`/`deleteCoupon`에 명시적 save 추가(`CouponCommandService`), 순수 단위 테스트 `domain-module/src/test/.../coupon/model/CouponTest`·`MemberCouponTest`. `event` 도메인 — JPA 연관관계 없이 raw FK(`Long eventId`)로만 연결된 **세 애그리거트**(`Event`+`EventWinner`+`EventAnnouncement`, 각각 독립 Repository/JpaRepository/RepositoryImpl)가 있고, `EventWinner`는 공유 VO `PhoneNumber`를 도메인 모델 필드로 그대로 유지하며(당시엔 `@Embeddable`을 core에 유지했으나, 이후 core-module 100% JPA-free 전환으로 `PhoneNumber`는 순수 POJO가 되고 컬럼 매핑은 `EventWinnerJpaEntity`의 `@AttributeOverride`로 이전됨) `Event`만 조회 결과(`EventManagementDetailResult.from`)가 감사 시각을 직접 소비해 감사 필드를 보유하는 반면 `EventWinner`/`EventAnnouncement`는 감사 필드가 불필요한 **비대칭 사례**, `EventRepositoryImpl`이 미분리 `file` 도메인의 `QUploadedFile`을 그대로 조인: 순수 모델 `domain-module/.../event/model/Event`·`EventWinner`·`EventAnnouncement`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../event/persistence/`(`EventJpaEntity`/`EventWinnerJpaEntity`/`EventAnnouncementJpaEntity`/`EventMapper`/`EventWinnerMapper`/`EventAnnouncementMapper`/`EventJpaRepository`/`EventWinnerJpaRepository`/`EventAnnouncementJpaRepository`/`EventRepositoryImpl`/`EventWinnerRepositoryImpl`/`EventAnnouncementRepositoryImpl`), 더티 체킹에 의존하던 `updateEvent`/`deleteEvent`/`updateAnnouncement`/`deleteWinner`에 명시적 save 추가(`EventCommandService`), 순수 단위 테스트 `domain-module/src/test/.../event/model/EventTest`·`EventWinnerTest`·`EventAnnouncementTest`. `member` 도메인 — **member 코어 3개 애그리거트만 전환**(`Member`/`MemberSocialAccount`/`MemberWithdrawal`; 같은 폴더의 `follow`/`referral`은 이번 범위 제외), JPA 연관관계 없이 `MemberSocialAccount`/`MemberWithdrawal`은 `@Convert` FK VO(`memberId`, `MemberIdConverter`는 `bug` 선례대로 core-module 잔류)로만 연결되고 `Member`는 공유 VO `PhoneNumber`를 도메인 모델 필드로 그대로 유지하는 사례(당시엔 `@Embeddable`을 core에 유지했으나, 이후 core-module 100% JPA-free 전환으로 `PhoneNumber`는 순수 POJO가 되고 컬럼 매핑은 `MemberJpaEntity`의 `@AttributeOverride`로 이전됨): 순수 모델 `domain-module/.../member/model/Member`·`MemberSocialAccount`·`MemberWithdrawal`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../member/persistence/`(`MemberJpaEntity`/`MemberSocialAccountJpaEntity`/`MemberWithdrawalJpaEntity`/`MemberMapper`/`MemberSocialAccountMapper`/`MemberWithdrawalMapper`/`MemberJpaRepository`/`MemberSocialAccountJpaRepository`/`MemberWithdrawalJpaRepository`/`MemberRepositoryImpl`/`MemberSocialAccountRepositoryImpl`/`MemberWithdrawalRepositoryImpl`). **크로스 도메인 참조 신규 패턴**: `Member`가 POJO로 전환되며 core-module에 `QMember`가 더 이상 생성되지 않아, 아직 미분리인 `follow`/`review`/`rank` 도메인의 `FollowRepositoryImpl`/`ReviewRepositoryImpl`/`MemberReviewRankRepositoryImpl`이 깨졌다 — core는 infrastructure를 의존할 수 없어 이동한 `QMemberJpaEntity`를 import할 수 없으므로, 세 파일 모두 `com.querydsl.core.types.dsl.PathBuilder<Object>`로 JPA 엔티티명 문자열(`"MemberJpaEntity"`)을 참조해 필요한 컬럼만 `NumberPath`/`StringPath`/`EnumPath`로 노출하는 방식으로 전환(이후 유사 상황의 재사용 패턴). 명시적 save `MemberCommandService#updateProfile`·`#updatePersonalInfo`·`#updatePassword`·`#suspend`·`#activate`, 그리고 도메인 분리로 실제 회귀가 드러난 `web-api`의 `KakaoSocialLoginService`·`NaverSocialLoginService`·`FacebookSocialLoginService`·`AppleSocialLoginService`(`updateProviderInfo` 호출 후 `saveSocialAccount` 미호출 지점 수정), 순수 단위 테스트 `domain-module/src/test/.../member/model/MemberTest`·`MemberSocialAccountTest`·`MemberWithdrawalTest`. `member/follow`·`member/referral` 도메인 — member 코어 전환 시 제외됐던 두 하위 도메인 후속 전환, 둘 다 JPA 연관관계 없이 `@Convert` FK VO(`MemberId`)만 사용: `follow`는 상태전이 없는 insert+delete-only 애그리거트(`MemberFollow`, 감사 필드 미소비로 생략) — 순수 모델 `domain-module/.../member/follow/model/MemberFollow`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../member/follow/persistence/`(`MemberFollowJpaEntity`/`MemberFollowMapper`/`MemberFollowJpaRepository`/`MemberFollowRepositoryImpl` — member 코어 전환 때 생긴 `PathBuilder<Object>("MemberJpaEntity")` 우회는 그대로 유지, 자신의 `QMemberFollow`만 `QMemberFollowJpaEntity`로 치환, `delete`는 `deleteById`로 구현), 명시적 save 규칙은 대상 없음(이미 생성 시 save·삭제 시 delete 호출 중), 순수 단위 테스트 `domain-module/src/test/.../member/follow/model/MemberFollowTest`. `referral`은 상태전이(`reward`/`cancel`) 있는 애그리거트(`MemberReferral`, enum `MemberReferralStatus`) — 순수 모델 `domain-module/.../member/referral/model/MemberReferral`(`register`/`reconstitute`, `createdAt`만 포함 — coupon/point/reservation과 동형의 비대칭 사례), 어댑터 `infrastructure-module/.../member/referral/persistence/`(`MemberReferralJpaEntity`/`MemberReferralMapper`/`MemberReferralJpaRepository`/`MemberReferralRepositoryImpl`), load-copy-save로 `applyChanges`가 상태 전이를 복사. **`ReferralCommandService#register`에서 실제 회귀 발견 및 수정**: `save`가 이제 새 도메인 인스턴스를 반환하므로 첫 저장 후 `referral = memberReferralRepository.save(referral);`로 재할당하지 않으면 이어지는 `reward()` 저장이 중복 insert가 되는 문제를 이번 전환에서 고쳤다. 순수 단위 테스트 `domain-module/src/test/.../member/referral/model/MemberReferralTest`. `partnership` 도메인 — JPA 연관관계 없는 **단일 애그리거트**(`PartnershipRequest`)에 enum 1개(`PartnershipStatus`, `EnumType.STRING`+`columnDefinition`)만 있는 banner 유사 사례이면서, 기존 `save`가 **detached merge**(`jpaRepository.save(request)` 통째 저장)였던 것을 load-copy-save로 교정한 사례: 순수 모델 `domain-module/.../partnership/model/PartnershipRequest`(`of`/`reconstitute`, 재대입되지 않는 필드는 `final`), 어댑터 `infrastructure-module/.../partnership/persistence/`(`PartnershipRequestJpaEntity`/`PartnershipRequestMapper`/`PartnershipRequestJpaRepository`/`PartnershipRepositoryImpl`), 더티 체킹에 의존하던 `changeStatus`/`delete`에 명시적 save 추가(`PartnershipCommandService`), 순수 단위 테스트 `domain-module/src/test/.../partnership/model/PartnershipRequestTest`. `policy` 도메인 — JPA 연관관계 없는 **단일 애그리거트**(`PolicyDocument`)에 enum 1개(`PolicyType`, `EnumType.STRING`+`columnDefinition`)만 있는 banner/partnership 유사 사례이면서, 기존 `save`가 **detached merge**(`entityManager.merge(policyDocument)`)였던 것을 load-copy-save로 교정하고, 더티 체킹에 의존하던 지점이 **한 커맨드 메서드 안에 2곳**(`updatePolicy`의 무저장 업데이트, `activatePolicy`에서 `findCurrentEntityByType(...).ifPresent(PolicyDocument::deactivate)`로 비활성화되는 기존 정책의 무저장 변경) 있던 사례: 순수 모델 `domain-module/.../policy/model/PolicyDocument`(`of`/`reconstitute`, 재대입되지 않는 필드는 `final`), 어댑터 `infrastructure-module/.../policy/persistence/`(`PolicyDocumentJpaEntity`/`PolicyDocumentMapper`/`PolicyDocumentJpaRepository`/`PolicyDocumentRepositoryImpl`), 명시적 save `PolicyCommandService#updatePolicy`·`#activatePolicy`(비활성화되는 기존 정책도 별도 save), 순수 단위 테스트 `domain-module/src/test/.../policy/model/PolicyDocumentTest`. `point` 도메인 — JPA 연관관계 없이 `@Convert` FK VO(`memberId`)로만 연결된 **두 애그리거트**(`Point`+`PointHistory`)가 있고, `PointHistory`만 조회 결과(`PointHistoryResult.from`)가 감사 시각(`createdAt`)을 직접 소비해 감사 필드를 보유하는 반면 상태전이(`addPoints`/`deductPoints`)가 있는 `Point`는 어떤 result도 감사 시각을 소비하지 않아 감사 필드가 불필요한 **coupon과 동형의 비대칭 사례**(`updatedAt`은 두 엔티티 모두 미소비): 순수 모델 `domain-module/.../point/model/Point`·`PointHistory`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../point/persistence/`(`PointJpaEntity`/`PointHistoryJpaEntity`/`PointMapper`/`PointHistoryMapper`/`PointJpaRepository`/`PointHistoryJpaRepository`/`PointRepositoryImpl`/`PointHistoryRepositoryImpl`), 더티 체킹에 의존하던 `usePoints`/`earnPoints`/`refundPoints`/`reclaimEarnedPoints`/`deductPoints` 5개 지점에 명시적 save 추가(`PointCommandService`), 순수 단위 테스트 `domain-module/src/test/.../point/model/PointTest`·`PointHistoryTest`. **후속 네이밍 통일(별도 리팩터링)**: 애그리거트/VO/Repository/Result/JPA 엔티티만 `MemberPoint`/`MEMBER_POINT`로 남아 애플리케이션·어댑터 계층(`PointCommandService` 등)과 혼재하던 것을 `Point`/`POINT`로 통일했고, DB 테이블 `MEMBER_POINT`→`POINT`·`MEMBER_POINT_HISTORY`→`POINT_HISTORY`(컬럼명은 유지, 인덱스명만 `idx_point_*`로 변경, `alter.sql`에 `RENAME TABLE` 마이그레이션 추가)도 함께 반영했다. web-api의 point 응답 DTO·서비스도 `member` 패키지에서 `webapi/point/`로 완전 분리하고, `/v1/me/point*` 핸들러를 전담하는 `PointApiController`(admin-api `PointApiController`와 이름 대칭, URL 경로의 `/me`는 유지)를 신설했다. `rank` 도메인 — JPA 연관관계 없이 raw FK(`Long rankId`)와 `@Convert` FK VO(`memberId`)로 연결된 **세 애그리거트**(`RankPeriod`+`RankPrize`+`MemberReviewRank`, `MemberReviewRank`는 insert-only로 상태전이·삭제 없음)가 있고, **분리 리팩터링과 별개로 사용자 결정에 따라 `RankPeriod`/`RankPrize`의 하드 삭제를 소프트 삭제로 전환**(DDL에 `is_deleted` 컬럼 신설, `RepositoryImpl`의 `delete(도메인)`이 내부적으로 managed 엔티티의 `deleted` 플래그만 갱신하도록 재구현, 모든 조회 경로에 `deleted.isFalse()` 필터 추가)한 사례, `RankInfoRepositoryImpl`/`MemberReviewRankRepositoryImpl`이 미분리 `file`/`member` 도메인의 Q타입을 함께 쓰는 크로스 도메인 조회: 순수 모델 `domain-module/.../rank/model/RankPeriod`·`RankPrize`·`MemberReviewRank`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../rank/persistence/`(`RankPeriodJpaEntity`/`RankPrizeJpaEntity`/`MemberReviewRankJpaEntity`/`RankPeriodMapper`/`RankPrizeMapper`/`MemberReviewRankMapper`/`RankPeriodJpaRepository`/`RankPrizeJpaRepository`/`MemberReviewRankJpaRepository`/`RankPeriodRepositoryImpl`/`RankPrizeRepositoryImpl`/`MemberReviewRankRepositoryImpl`/`RankInfoRepositoryImpl` — `RankInfoRepositoryImpl`은 JPA 리포지토리 없이 순수 QueryDSL 조회만 담당). `MemberReviewRankRepositoryImpl`은 `member` 도메인이 이미 POJO로 전환되어 `QMember`가 core-module에 더 이상 생성되지 않으므로, `follow`/`review` 선례와 동일하게 `PathBuilder<Object>`로 `"MemberJpaEntity"`를 문자열 참조해 필요한 컬럼만 노출(신규 재사용이 아니라 기존 크로스 도메인 참조 패턴의 반복 적용). 더티 체킹에 의존하던 `updatePeriod`/`updatePrize`에 명시적 save 추가(`RankCommandService`), 순수 단위 테스트 `domain-module/src/test/.../rank/model/RankPeriodTest`·`RankPrizeTest`·`MemberReviewRankTest`. `reservation` 도메인 — JPA 연관관계 없이 raw FK(`Long shopId`)와 `@Convert` FK VO(`memberId`)로 연결된 **두 애그리거트**(`Reservation`+`ReservationSlot`)가 있고, **레퍼런스 최초로 `@Version` 낙관적 락 애그리거트**(`ReservationSlot`)를 분리한 사례: POJO에 `version` 필드를 유지해 재구성 시 주입하고, `save`의 load-copy-save가 managed 엔티티의 `@Version`을 그대로 검증·증가시켜 `ReservationCreator`의 재시도 루프(낙관적 락/유니크 충돌 감지) 동작을 보존함. `Reservation`만 조회 결과(`ReservationResult.from`)가 감사 시각(`createdAt`)을 직접 소비해 감사 필드를 보유하는 반면 `ReservationSlot`은 어떤 result도 감사 시각을 소비하지 않아 감사 필드가 불필요한 **coupon/point와 동형의 비대칭 사례**(`updatedAt`은 둘 다 미소비): 순수 모델 `domain-module/.../reservation/model/Reservation`·`ReservationSlot`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../reservation/persistence/`(`ReservationJpaEntity`/`ReservationSlotJpaEntity`/`ReservationMapper`/`ReservationSlotMapper`/`ReservationJpaRepository`/`ReservationSlotJpaRepository`/`ReservationRepositoryImpl`/`ReservationSlotRepositoryImpl`), 더티 체킹에 의존하던 `confirm`/`reject`/`complete`/`cancel`과 슬롯 반납 `releaseSlot`에 명시적 save 추가(`ReservationCommandService`), 순수 단위 테스트 `domain-module/src/test/.../reservation/model/ReservationTest`·`ReservationSlotTest`. (슬롯 시간·정원 상수 유틸은 이 애그리거트와 이름이 겹치지 않도록 `SlotPolicy`로 별도 유지 — [Command/DTO 네이밍 순서 규칙](#commanddto-네이밍-순서-규칙-도메인동작-형태) 등과 무관한 순수 리네이밍 사례.) `search` 도메인 — JPA 연관관계·ID VO 없이 독립된 **세 애그리거트**(`PopularKeyword`+`RecommendedKeyword`+`SearchKeywordLog`)가 있고, 그중 `RecommendedKeyword`는 **Java 애플리케이션 계층에 생성/변경 경로가 전혀 없는 읽기 전용 애그리거트(SQL/수동 시드)라 `of` 없이 `reconstitute`만 공개하는 이 프로젝트 최초의 read-only 분리 사례**: 순수 모델 `domain-module/.../search/model/PopularKeyword`·`RecommendedKeyword`·`SearchKeywordLog`(전 필드 `final`; `PopularKeyword`/`SearchKeywordLog`는 `of`/`reconstitute` 둘 다, `RecommendedKeyword`는 `reconstitute`만), 어댑터 `infrastructure-module/.../search/persistence/`(`PopularKeywordJpaEntity`/`RecommendedKeywordJpaEntity`/`SearchKeywordLogJpaEntity`/`PopularKeywordMapper`/`RecommendedKeywordMapper`/`SearchKeywordLogMapper`/`PopularKeywordJpaRepository`/`RecommendedKeywordJpaRepository`/`SearchKeywordLogJpaRepository`/`PopularKeywordRepositoryImpl`/`RecommendedKeywordRepositoryImpl`/`SearchKeywordLogRepositoryImpl` — `PopularKeywordRepositoryImpl`만 QueryDSL로 벌크 `deleteAll()`, `SearchKeywordLogJpaRepository`의 `@Modifying` JPQL은 엔티티명을 `SearchKeywordLogJpaEntity`로 갱신). **update 경로 자체가 없어 세 애그리거트 모두 신규 insert만 수행**(`PopularKeywordRepositoryImpl#saveAll`은 도메인↔엔티티 리스트를 매핑만 하고 load-copy-save 불필요), 명시적 save 규칙은 `admin` 선례와 동일하게 대상 없음(`SearchKeywordCommandService`가 `deleteAll`+`saveAll`+`deleteOlderThan`만 수행). 자체 애그리거트 없이 다른 도메인에 위임하는 `SearchResultQueryService`는 전환 범위에서 제외. 순수 단위 테스트 `domain-module/src/test/.../search/model/PopularKeywordTest`·`RecommendedKeywordTest`·`SearchKeywordLogTest`. `review` 도메인 — JPA 연관관계 없이 raw FK(`Long reviewId`/`commentId` 등)와 `@Convert` FK VO(`memberId`, `ReviewReply`는 `memberId`+`replyToMemberId` 2개)로만 연결된 **여섯 애그리거트**(`Review`+`ReviewComment`+`ReviewReply`+`ReviewImage`+`ReviewLike`+`ReviewTag`)가 있고, 감사 시각 소비 여부로 셋씩 갈리는 사례(`Review`/`ReviewComment`/`ReviewReply`는 `createdAt` 보유 — 각각 응답·`ReviewQueryService`의 `findCommentsIncludingHidden`/`findRepliesIncludingHidden`이 소비, `ReviewImage`/`ReviewLike`/`ReviewTag`는 불변 애그리거트라 감사 필드 생략), **전환과 별개로 죽은 코드였던 일곱 번째 애그리거트 `ReviewProduct`(전 코드베이스에 실제 호출부 없음)를 이번 PR에서 완전 삭제**(모델·리포지토리 인터페이스·JpaRepository·RepositoryImpl 전부 제거)한 사례: 순수 모델 `domain-module/.../review/model/Review`·`ReviewComment`·`ReviewReply`·`ReviewImage`·`ReviewLike`·`ReviewTag`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../review/persistence/`(6개 애그리거트 × `JpaEntity`/`Mapper`/`JpaRepository`/`RepositoryImpl`) — `ReviewRepositoryImpl`이 자신의 서브쿼리 Q타입(`QReviewImageJpaEntity`/`QReviewLikeJpaEntity`/`QReviewCommentJpaEntity`, 별칭 인스턴스 `subReviewImage`/`subReviewLike`/`subReviewComment`/`sortReviewLike` 포함)을 모두 치환하면서, 미분리 도메인의 `QUploadedFile`/`QOrderProduct`/`QProduct`/`QStation`(당시 `shop`은 미분리라 `QShop`도 포함— 이후 `shop` 전환 시 `PathBuilder`로 대체됨, 아래 `shop` 항목 참고)과 `member` 도메인의 `MemberJpaEntity` `PathBuilder` 문자열 참조는 그대로 유지한 크로스 도메인 QueryDSL 사례. 이 `review.domain.model.QReview`가 사라지면서 core-module에 남아 있던 다른 도메인(`shop`)의 `ShopRepositoryImpl`이 `QReview`를 직접 조인하던 지점도 함께 깨져, `member`/`follow`/`rank` 선례와 동일하게 `PathBuilder<Object>`로 `"ReviewJpaEntity"`를 문자열 참조하도록 전환(기존 크로스 도메인 참조 패턴의 반복 적용). `Review`/`ReviewComment`/`ReviewReply`의 `hide`/`unhide`/`updateContent`가 더티 체킹에 의존하던 지점에 명시적 save 추가(`ReviewCommandService#changeReviewHidden`·`#changeCommentHidden`·`#changeReplyHidden`·`#updateReview`), 순수 단위 테스트 `domain-module/src/test/.../review/model/ReviewTest`·`ReviewCommentTest`·`ReviewReplyTest`·`ReviewImageTest`·`ReviewLikeTest`·`ReviewTagTest`. `shop` 도메인 — **@Entity 17개(+enum 5개) 중 핵심 `Shop` 애그리거트 1개만 우선 전환**하고 나머지 16개 자식 엔티티(`ShopAmenity`/`ShopBusinessHour`/`ShopChoice` 등)와 그 JpaRepository는 현행(core-module 잔류) 유지한 최초의 **부분 전환** 사례 — JPA 연관관계는 없으나 `Shop`이 다른 다섯 파일(`order`/`product`/`review` 및 `shop` 자신의 `ShopChoiceRepositoryImpl`, 그리고 자기 자신인 이동 대상 `ShopRepositoryImpl`)에서 `QShop`으로 참조되고 있어 회귀 범위가 컸다: 순수 모델 `domain-module/.../shop/model/Shop`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../shop/persistence/`(`ShopJpaEntity`/`ShopMapper`/`ShopJpaRepository`/`ShopRepositoryImpl` — 자신의 자식 Q타입 `QShopAmenity`/`QShopFoodType`/`QShopFoodTypeCategory`/`QShopBookmark`/`QShopAmenityCategory`/`QStation`은 core에 잔류해 그대로 import, `review`가 이미 POJO라 `QReview` 대신 `PathBuilder<Object>`로 `"ReviewJpaEntity"`를 문자열 참조). **`QShop` 소멸로 core에 남은 4개 파일이 함께 깨진 크로스 도메인 팬아웃**: `order/.../OrderRepositoryImpl`·`product/.../ProductRepositoryImpl`·`shop/.../ShopChoiceRepositoryImpl`(모두 core 잔류)과 `infrastructure:persistence`로 이미 이동한 `review/.../ReviewRepositoryImpl`까지 4곳 전부 `member`/`follow`/`rank` 선례와 동일하게 `PathBuilder<Object>`로 `"ShopJpaEntity"`를 문자열 참조하도록 전환(기존 크로스 도메인 참조 패턴의 반복 적용, 이번이 가장 넓은 팬아웃 사례). `ShopQueryService#findShopById`가 core에서 곧 사라질 `ShopJpaRepository`를 직접 주입받아 쓰던 지점을 `ShopRepository#findById`로 교체. `ShopCommandService`의 `createShop`/`updateShop`/`closeShop`은 이미 명시적 `save` 호출 중이라 추가 조치 불필요(`save()`만 detached merge → load-copy-save로 교정), 순수 단위 테스트 `domain-module/src/test/.../shop/model/ShopTest`. shop 자식 16개 엔티티 후속 전환 완료(부분 전환을 완결한 사례 — `ShopAmenity`/`ShopAmenityCategory`/`ShopBannerImage`/`ShopBookmark`/`ShopBreakTime`/`ShopBusinessHour`/`ShopChoice`/`ShopClosedDay`/`ShopFoodType`/`ShopFoodTypeCategory`/`ShopOrderMethod`/`ShopOwnerMessageHistory`/`ShopPhotoCategory`/`ShopPhotoCategoryImage`/`Station`/`Tag`를 순수 POJO + `infrastructure/shop/persistence`의 JpaEntity/Mapper로 분리하고 JpaRepository 15개·RepositoryImpl 4개(`ShopDetailRepositoryImpl`/`ShopChoiceRepositoryImpl`/`ShopBookmarkRepositoryImpl`/`TagRepositoryImpl`)를 infrastructure-module로 이동; `ShopChoiceRepositoryImpl`은 이동 후 기존 `PathBuilder<Object>("ShopJpaEntity")` 우회를 같은 패키지의 정식 `QShopJpaEntity` 조인으로 복원(order 선례와 동일 패턴); `ShopRepositoryImpl`(자식 Q-type 6종 import)과 `ReviewRepositoryImpl`(`QStation` import)은 infra→infra 참조로 전환; core 애플리케이션 계층이 직접 주입하던 `ShopBookmarkJpaRepository`/`StationJpaRepository`와 `new ShopBookmark(...)` 생성을 신설 `StationRepository`(`existsById`)·확장된 `ShopBookmarkRepository`(`existsById`/`save`)·`ShopBookmark.of(...)` 팩토리로 대체해 core의 JPA 직접 의존을 제거; JPA 연관관계는 처음부터 전무(raw FK + QueryDSL join)했고 `ShopBookmark`만 `@Convert` FK VO(`MemberId`) 보유). `order` 도메인 — JPA 연관관계 없이 plain FK `Long`(`orderId`/`orderProductId`)로만 연결된 **세 애그리거트**(`Order`+`OrderProduct`+`OrderProductOption`, `bug` 2-애그리거트 패턴을 3개로 확장)가 있고, **미전환 `payment` 도메인이 core에 잔류한 `OrderIdConverter`(`Payment.orderId : OrderId @Convert`)를 그대로 사용해 컨버터를 core에 남긴 채 도메인·JPA 엔티티만 분리한 사례**(같은 폴더의 미사용 `OrderProductIdConverter`/`OrderProductOptionIdConverter`는 이번에 삭제): 순수 모델 `domain-module/.../order/model/Order`·`OrderProduct`·`OrderProductOption`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../order/persistence/`(`OrderJpaEntity`/`OrderProductJpaEntity`/`OrderProductOptionJpaEntity`/`OrderMapper`/`OrderProductMapper`/`OrderProductOptionMapper`/`OrderJpaRepository`/`OrderProductJpaRepository`/`OrderProductOptionJpaRepository`/`OrderRepositoryImpl`/`OrderProductRepositoryImpl`/`OrderProductOptionRepositoryImpl` — `OrderJpaEntity.memberId`는 `bug`/`member` 선례대로 `member` 도메인의 `MemberIdConverter`를 그대로 `@Convert` import). **`shop` 전환으로 생겼던 `PathBuilder` 우회를 정식 Q타입 조인으로 복원한 최초 사례**: `shop`이 먼저 POJO로 전환되며 core에 남아 있던 (구)`OrderRepositoryImpl`이 한시적으로 `PathBuilder<Object>`("ShopJpaEntity")로 우회하고 있었는데, order 자신이 infrastructure-module로 이동하면서 `QShopJpaEntity`를 정식 import할 수 있게 되어 우회를 제거했다(크로스 도메인 참조 패턴이 "이동 후에는 정식 Q타입으로 되돌아간다"는 것을 보여준 첫 사례). 옵션은 update 행위가 없어 `save`가 insert 전용(JpaEntity에 `applyChanges` 미생성). `OrderCommandService`의 `createOrder`(금액 갱신·상품 가격 갱신 2곳)·`changeOrderStatus`·`deleteOrder`에 명시적 save 추가. 순수 단위 테스트 `domain-module/src/test/.../order/model/OrderTest`. **크로스 도메인 영향**: 미전환 `review`(이미 infrastructure-module로 이동됨)의 `ReviewRepositoryImpl#findBestReviews`가 조인하던 `QOrderProduct`(도메인모델)를 `QOrderProductJpaEntity`(infra)로 치환(review가 이미 infra에 있어 infra→infra import로 문제 없이 해결). `payment` 도메인 — JPA 연관관계 없이 plain FK `Long`(`payment_id`)로만 연결된 **세 애그리거트**(`Payment`+`PaymentRefund`+`TossPaymentRecord`, `TossPaymentRecord`는 필드 50여 개의 PG raw 원장으로 insert-only)가 있고, **order가 먼저 분리되며 생겼던 core→infra 역참조 blocker가 해소된 뒤 진행한 후행 전환 사례**: order 미분리 시점에는 `OrderRepositoryImpl`(당시 core 잔류)이 `QPayment`(도메인 Q타입)를 조인해 payment를 먼저 분리하면 core가 infra의 `QPaymentJpaEntity`를 import할 수 없어 컴파일이 깨졌으나, order를 먼저 infrastructure-module로 옮겨 `OrderRepositoryImpl` 자체가 infra로 이동한 뒤에는 그 import를 `QPaymentJpaEntity`로 바꾸는 것이 infra→infra 참조가 되어 문제가 사라졌다(order/payment 분리 순서가 강제된 최초 사례). 순수 모델 `domain-module/.../payment/model/Payment`·`PaymentRefund`·`TossPaymentRecord`(`create`/`reconstitute`), 어댑터 `infrastructure-module/.../payment/persistence/`(`PaymentJpaEntity`/`PaymentRefundJpaEntity`/`TossPaymentRecordJpaEntity`/`PaymentMapper`/`PaymentRefundMapper`/`TossPaymentRecordMapper`/`PaymentJpaRepository`/`PaymentRefundJpaRepository`/`TossPaymentRecordJpaRepository`/`PaymentRepositoryImpl`/`PaymentRefundRepositoryImpl`/`TossPaymentRecordRepositoryImpl` — `PaymentJpaEntity`는 `order`의 `OrderIdConverter`와 자신의 `AmountConverter`를, `PaymentRefundJpaEntity`는 `PaymentIdConverter`+`AmountConverter`를 core 잔류 위치 그대로 import). `TossPaymentRecordMapper`는 파라미터 50여 개라 도메인 필드 선언 순서·`reconstitute`/`create` 파라미터 순서·매퍼 인자 순서를 스크립트로 3중 대조. `PaymentRefund`/`TossPaymentRecord`는 update 경로가 없어 `save`가 insert 전용(JpaEntity에 `applyChanges` 미생성), `Payment`만 기존 detached merge(`jpaRepository.save`)를 load-copy-save로 교정. **`OrderRepositoryImpl`(infra)의 `QPayment`→`QPaymentJpaEntity` import 치환이 blocker 해소의 실제 지점**. 더티 체킹에 의존하던 `confirmPayment`/`confirmTossPayment`/`cancelPayment`/`completeOnSitePayment`에 명시적 save 추가(`PaymentCommandService`) — 이 네 메서드는 같은 트랜잭션에서 order 상태도 함께 바꾸므로 `PaymentCommandService`에 `OrderRepository`를 신규 주입해 `orderRepository.save(order)`도 함께 호출. 순수 단위 테스트 `domain-module/src/test/.../payment/model/PaymentTest`·`PaymentRefundTest`. `verification` 도메인 — JPA 연관관계 없이 독립된 **두 애그리거트**(`EmailVerification`+`PhoneVerification`)가 있고, **`@Embedded` 값 객체(VO)를 그대로 유지한 사례**(양쪽 모두 자체 VO `VerificationCode`, `PhoneVerification`은 추가로 `member`/`event`와 공유하는 VO `core.shared.vo.PhoneNumber`도 도메인(둘 다 당시엔 `@Embeddable`을 core에 유지했으나, 이후 core-module 100% JPA-free 전환으로 순수 POJO가 되고 컬럼 매핑은 `PhoneVerificationJpaEntity`/`EmailVerificationJpaEntity`의 `@AttributeOverride`로 이전됨) 필드로 유지), **이 프로젝트 최초로 `BaseEntity`를 상속하지 않는 사례**(감사 필드가 수동 `@Column created_at`뿐이고 `updated_at`이 없음 — `createdAt`은 `@CreatedDate` 감사가 아니라 도메인 생성자가 `LocalDateTime.now()`로 직접 채움): 순수 모델 `domain-module/.../verification/model/EmailVerification`·`PhoneVerification`(`create`/`reconstitute`, `updatedAt` 없음), 어댑터 `infrastructure-module/.../verification/persistence/`(`EmailVerificationJpaEntity`/`PhoneVerificationJpaEntity`/`EmailVerificationMapper`/`PhoneVerificationMapper`/`EmailVerificationJpaRepository`/`PhoneVerificationJpaRepository`/`EmailVerificationRepositoryImpl`/`PhoneVerificationRepositoryImpl` — JpaEntity도 `BaseEntity` 미상속), 기존 detached merge(`jpaRepository.save(entity)` 통째 저장)였던 `save`를 load-copy-save로 교정. **파사드가 도메인 모델을 직접 다루는 사례**: `web-api`의 `AuthPasswordResetService`가 `EmailVerification`/`EmailVerificationRepository`/`VerificationCode`를 command 서비스 밖에서 직접 호출하므로, 더티 체킹에 의존하던 `verifyPasswordResetCode`에도 명시적 save를 추가(코어 `EmailVerificationCommandService#confirmVerificationCode`·`PhoneVerificationCommandService#confirmVerificationCode`와 함께 총 3곳). 순수 단위 테스트 `domain-module/src/test/.../verification/model/EmailVerificationTest`·`PhoneVerificationTest`. **(이후 개정)** 이 `verification` 컨텍스트는 뒤에 `mail`·`sms` 두 독립 도메인으로 분리되었고 타입명도 `MailVerification`·`SmsVerification`으로 통일되었다 — 위 경로·타입명은 전환 당시의 기록이며 현재 구조는 [채널 도메인 어휘 통일 규칙](#채널-도메인-어휘-통일-규칙-mailsms--패키지타입-모두)을 따른다. `product` 도메인 — JPA 연관관계 없이 raw FK(`Long shopId`/`productId`/`optionGroupId` 등)로만 연결된 **8개 애그리거트**(`Product`+`ProductBbq`+`ProductCategory`+`ProductCommonOption`+`ProductCommonOptionGroup`+`ProductImage`+`ProductOption`+`ProductOptionGroup`)가 있고, **`shop`·`review`가 먼저 분리되어야만 풀리는 core→infra 역참조 blocker의 두 번째 사례**(payment 이후) — 분리 전 조사 시점에 미전환이던 `shop`의 `ShopChoiceRepositoryImpl`과 `review`의 `ReviewRepositoryImpl`이 core의 `QProduct`/`QProductImage`(도메인 모델 Q타입)를 직접 join하고 있어 product를 먼저 옮기면 두 파일이 컴파일 실패하는 구조였다. `shop`·`review`가 각각 먼저 infrastructure-module로 전환되며 두 파일 자체가 infra로 이동한 뒤, product 전환 시점에 그 두 파일의 `QProduct`/`QProductImage` import를 `QProductJpaEntity`/`QProductImageJpaEntity`로 치환하는 것이 infra→infra 참조가 되어 해소됨(`shop`→`review`→`product` 순서 강제, order→payment 선례와 동일 패턴). `Product`만 보유한 `@Embedded ProductDiscountInfo`(공유 아님)는 `member`의 `PhoneNumber` 선례처럼 `@Embeddable`/`@Column` 어노테이션을 유지한 채 순수 POJO `Product`와 `ProductJpaEntity` 양쪽이 재사용했으나(당시엔 비공유 VO도 실제 JPA 임베디드로 쓰이면 어노테이션을 유지한다는 원칙이었음), 이후 core-module 100% JPA-free 전환으로 `ProductDiscountInfo`도 어노테이션이 완전히 제거된 순수 POJO가 되고 컬럼 매핑은 `ProductJpaEntity`의 `@AttributeOverrides`로 이전됨. 8개 애그리거트 전부 감사 시각 미소비로 `createdAt`/`updatedAt` 생략(admin/search 선례와 동형). 순수 모델 `domain-module/.../product/model/Product`·`ProductBbq`·`ProductCategory`·`ProductCommonOption`·`ProductCommonOptionGroup`·`ProductImage`·`ProductOption`·`ProductOptionGroup`(`of`/`reconstitute`; `ProductImage`는 update 없는 불변 애그리거트라 전 필드 `final`), 어댑터 `infrastructure-module/.../product/persistence/`(8개 애그리거트 × `JpaEntity`/`Mapper`/`JpaRepository`, `RepositoryImpl`은 `Product`+7개 하위 애그리거트로 9개 — `ProductRepositoryImpl`은 (당시) 미분리 `file`의 `QUploadedFile`과 이미 이동한 `shop`의 `QShopJpaEntity`를 조인, 이후 `file` 전환으로 `QUploadedFileJpaEntity`(infra 생성)로 치환됨), 명시적 save는 `ProductCommandService`의 8개 커맨드 메서드와 `ProductReviewEventListener#updateProductReviewStats`가 분리 이전부터 이미 전부 호출 중이라 신규 추가 없음(admin 선례와 동일), 순수 단위 테스트 `domain-module/src/test/.../product/model/ProductTest`·`ProductBbqTest`·`ProductCategoryTest`·`ProductCommonOptionTest`·`ProductCommonOptionGroupTest`·`ProductImageTest`·`ProductOptionTest`·`ProductOptionGroupTest`. `file` 도메인 — 연관관계·QueryDSL·update/soft delete가 없는 최소 CRUD(create+read) 변형이면서, 동시에 코드베이스에서 QueryDSL 조인으로 가장 많이 참조되는 hub 도메인이었던 사례(참조자 14개 — order/shop×3/member/member-follow/product×2/review/rank×3/event/banner의 `RepositoryImpl`이 `QUploadedFile`을 조인): 순수 모델 `domain-module/.../file/model/UploadedFile`(`of`/`reconstitute`, 감사 필드 `createdAt`/`updatedAt` 포함 — 테이블에 존재하는 컬럼과의 매핑 일관성을 위해 admin과 달리 유지), 어댑터 `infrastructure-module/.../file/persistence/`(`UploadedFileJpaEntity`/`UploadedFileMapper`/`UploadedFileJpaRepository`/`UploadedFileRepositoryImpl` — QueryDSL 없이 순수 pass-through, `save`는 update 경로가 없어 insert 전용). hub 도메인 특성상 참조자 14개 `RepositoryImpl`의 `QUploadedFile`(core 생성) import를 `QUploadedFileJpaEntity`(infra 생성)로 전부 치환(모두 infra→infra 참조라 정식 Q타입 직접 사용, PathBuilder 우회 불필요) — 이 전환은 order/shop/member/follow/product/review/rank/event가 먼저 infra로 이동해 core의 `QUploadedFile` 참조가 0건이 된 뒤에야 안전하게 수행할 수 있었다(hub 도메인은 참조자를 먼저 옮기고 마지막에 전환하는 순서가 강제됨). 명시적 save 규칙은 대상 없음(`FileCommandService#save`가 이미 저장 시 `save` 호출). 순수 단위 테스트 `domain-module/src/test/.../file/model/UploadedFileTest`. **PathBuilder 문자열 우회 정식 복원(후속)**: 전 도메인(22개) JPA 분리 완료로 core-module이 100% JPA-free가 되어, 남아 있던 5곳의 `PathBuilder<Object>` 문자열 우회(`shop`의 `ShopRepositoryImpl`이 참조하던 `"ReviewJpaEntity"`, `member/follow`의 `MemberFollowRepositoryImpl`·`review`의 `ReviewRepositoryImpl`(2곳)·`rank`의 `MemberReviewRankRepositoryImpl`이 참조하던 `"MemberJpaEntity"`/`"ShopJpaEntity"`)가 모두 infra→infra 참조로 바뀌어 더 이상 필요 없어졌으므로, 각각 정식 `QReviewJpaEntity`/`QMemberJpaEntity`/`QShopJpaEntity` 조인으로 복원함(order/product 선례와 동일 패턴, 동작 변경 없는 순수 리팩터링).

## 채널 도메인 어휘 통일 규칙 (`mail`/`sms` — 패키지·타입 모두)

**발송 채널이 곧 바운디드 컨텍스트인 도메인은 패키지명과 자바 식별자를 같은 어휘(`mail`/`sms`)로 통일합니다.** 과거 `verification` 컨텍스트 하나가 이메일 인증과 휴대폰 인증을 함께 담고 있었으나, 두 관심사는 애그리거트·리포지토리·출력 포트·이벤트·테이블이 처음부터 완전히 분리되어 있었고 공유하던 것은 `VerificationCode` VO 하나뿐이어서 한 컨텍스트에 묶여 있을 응집 근거가 없었습니다. `external-api`가 이미 `email/`·`sms/`로 갈라져 있어 도메인 쪽만 통합 상태로 남은 불일치이기도 했습니다.

- **패키지**: `domain/mail/`, `domain/sms/` (flat — `verification/mail` 같은 중첩이나 `verification-mail` 같은 복합명을 쓰지 않습니다).
- **타입·메서드·변수**: `Mail*`/`Sms*`로 통일합니다(`MailVerification`, `SmsVerification`, `MailVerificationId`, `createMailVerifyToken()`, `smsVerifyToken` 등). `Email*`/`Phone*` 접두어를 남기지 않습니다.
- **`Mail`과 `Email`이 중복될 때는 `Mail`만 씁니다** — 단 Spring 타입과 이름이 충돌하면 역할 접미어로 구분합니다(예: Spring `JavaMailSender`를 주입받는 어댑터는 `JavaMailSender`가 아니라 `JavaMailAdapter`).
- **예외 — 값의 이름은 유지합니다**: `email`(이메일 주소 값), `phoneNumber`(번호 값)는 채널이 아니라 검증 대상 데이터의 이름이므로 그대로 둡니다. 따라서 `MailVerification.getEmail()`, `SmsVerification.getPhoneNumber()`가 정상이며, 요청 JSON 필드 `email`·공용 VO `PhoneNumber`(`shared/vo/`)도 유지합니다. **타입명은 채널, 속성명은 실체** — 이 경계가 "메일 인증"의 모호함을 없앱니다.
- **HTTP 경로는 복수형 리소스 컬렉션**: `/api/mail-verifications`, `/api/sms-verifications`. web-api 25개 경로 중 24개가 복수형 리소스 컬렉션(`/api/shops`·`/api/bug-reports` 등)이고 단수형은 `/api/event` 하나뿐이므로, `/api/mail`·`/api/sms` 같은 단수 채널명은 쓰지 않습니다(RPC 스타일이 되어 나머지 경로와 결이 갈립니다).
- **메서드명에 채널과 값 이름이 함께 등장하는 것은 정상입니다**: `getEmailFromMailVerifyToken()`은 "메일 인증 토큰에서 이메일 주소를 꺼낸다"로 두 어휘가 각각 다른 대상을 정확히 가리킵니다(`getPhoneNumberFromSmsVerifyToken()`도 동일). `getMailFromMailVerifyToken()`으로 바꾸면 반환값을 오해하게 되므로 이런 혼재는 교정 대상이 아닙니다.
- **`@RateLimit keyPrefix`는 개명하지 않습니다**: Redis 카운터 키이므로 바꾸는 순간 기존 카운터가 버려져 배포 시점에 발송 한도가 전원 리셋됩니다(인증코드 API에서는 브루트포스 한도 초기화). 그 결과 `mail` 도메인이 `rate_limit:email_verification`을, `sms`가 `rate_limit:sms_verification`을 쓰는 비대칭이 남는데 이는 정상이며, 컨트롤러에 그 이유를 주석으로 남깁니다.
- **DB 테이블·인덱스명도 채널 어휘로 통일합니다 (개정됨)**: 테이블은 `MAIL_VERIFICATION`/`SMS_VERIFICATION`, 인덱스는 `idx_mail_verification_*`/`idx_sms_verification_*`를 씁니다. 과거에는 "`ddl-auto=validate` 환경이라 RENAME은 DDL 마이그레이션과 앱 배포가 원자적이어야 하므로 순수 코드 리팩터에 그 운영 리스크를 들이지 않는다"는 이유로 `EMAIL_VERIFICATION`/`PHONE_VERIFICATION`을 유지했으나, 이후 사용자 결정으로 `alter.sql`에 `RENAME TABLE`·`RENAME INDEX` 마이그레이션을 추가하고 엔티티 `@Table`/`@Index`를 함께 전환했습니다. **배포 시 이 마이그레이션과 앱 배포는 반드시 원자적으로 수행해야 합니다**(선후가 갈리면 `validate`가 실패합니다).
- **컬럼명과 응답 `code` 문자열은 여전히 불일치를 허용합니다**: 컬럼 `email`/`phone_number`는 채널이 아니라 검증 대상 데이터의 이름이므로 그대로 두는 것이 정확합니다(위 "값의 이름은 유지" 예외와 동일). `ErrorCode`의 응답 `code` 문자열도 프론트가 `code`로 분기할 경우를 대비해 기존 값(`VERIFICATION_CODE_*`/`EMAIL_VERIFICATION_CODE_*`)을 유지하고 **상수명만** `SMS_`/`MAIL_` 접두어로 대칭화합니다. **이 불일치들은 버그가 아니므로 엔티티 Javadoc·ErrorCode 주석에 그 의도를 명시하고 임의로 RENAME하지 않습니다.**

reference 구현: `domain-module`의 `mail/domain/`(`MailVerification`·`MailVerificationPurpose`·`MailVerificationMessage`·`MailSender`)·`sms/domain/`(`SmsVerification`·`SmsVerificationMessage`·`SmsSender`), 공유 VO `shared/vo/VerificationCode`, `infrastructure:persistence`의 `mail/persistence`·`sms/persistence`(테이블·인덱스명을 채널 어휘로 통일, 컬럼명 유지 의도를 Javadoc에 명시), `external-api`의 `JavaMailAdapter`·`AwsSesMailSender`, `web-api`의 `mail/MailVerificationApiController`(`/api/mail-verifications`)·`sms/SmsVerificationApiController`(`/api/sms-verifications`).

## 인증코드 발송은 발급과 원자적으로 수행하는 규칙 (도메인 서비스에 Sender 포트 주입)

**인증코드 발급 도메인 서비스는 출력 포트(`MailSender`/`SmsSender`)를 주입받아 `issue()` 안에서 저장과 발송을 함께 수행합니다.** 발송 호출을 api 계층 command 서비스나 이벤트 리스너로 미루지 않습니다.

- **이 규칙이 생긴 이유(실제 장애)**: 과거에는 발송 책임이 호출부에 흩어져 있어 `AuthPasswordResetService`(비밀번호 재설정 파사드)만 `issue()` 후 `mailSender.send()`를 직접 호출했고, **인증코드 발송 API 경로 두 개는 그 호출을 빠뜨려 코드를 DB에 저장만 하고 발송하지 않았습니다**(사용자는 200을 받지만 메일/SMS를 받지 못함). 그 결과 `SmsSender`는 프로덕션에서 한 번도 호출되지 않는 죽은 포트였습니다. 발급의 정의에 발송을 포함시키면 이 누락이 구조적으로 불가능해집니다.
- **롤백 의미가 이 도메인에서는 올바릅니다**: 발송이 실패하면 예외가 전파되어 인증 레코드 저장도 롤백됩니다. 인증코드는 발송되지 않으면 존재 가치가 0이므로, DB에 PENDING 코드만 남는 유령 레코드보다 502를 반환하는 것이 옳습니다. (일반적으로 "외부 I/O를 트랜잭션에 넣지 말라"가 정석이지만 그 규칙은 외부 호출 실패 시 로컬 커밋을 살려야 할 때 적용됩니다 — 여기는 반대입니다.)
- **발송 문구는 도메인이 소유합니다**: `{도메인}VerificationMessage`(package-private)를 도메인 서비스와 같은 패키지에 두고, 목적별 분기가 필요하면 `{도메인}VerificationPurpose` enum으로 나눕니다. 문구에 노출되는 유효시간이 애그리거트의 만료 정책과 같은 값이어야 하고, 소비 모듈이 늘어날 때 문구가 복제되어 갈라지는 것을 막습니다. Spring `MessageSource`는 domain-module 프레임워크-프리 원칙에 어긋나므로 쓰지 않으며, i18n이 필요해지면 도메인 포트로 승격합니다.
- **이벤트 리스너에 발송을 두지 않습니다**: 도메인 이벤트(`MailVerifiedEvent`/`SmsVerifiedEvent`)는 인증 **완료** 시점이고 발송은 **발급** 시점에 필요해 시점이 다릅니다. 리스너는 크로스커팅 관찰(로깅·향후 퍼널 집계) 역할만 맡고 도메인별로 분리해 둡니다.
- **일반화**: 출력 포트 호출(발송·업로드 등)이 불변식의 구성요소이면 도메인 서비스가 그 호출을 소유합니다 — SRP는 "메서드가 한 동작만"이 아니라 "클래스가 변경될 이유가 하나"이며, 여기서 변경 이유는 "인증코드 발급·검증 정책" 하나입니다. 의존 방향도 안쪽을 향합니다(포트는 도메인 선언, 구현은 external-api).
- **유효시간 상수는 도메인 모델이 단독 소유합니다**: 발송 문구에 노출되는 유효시간을 문구 유틸에 리터럴로 복제하지 않고 `{애그리거트}.EXPIRATION_MINUTES`를 참조합니다 — 복제하면 한쪽만 바뀌어 "실제 만료 10분 / 안내 문구 5분" 같은 불일치가 테스트에도 걸리지 않고 생깁니다.
- **회귀 방어 테스트가 필수입니다**: `{도메인}VerificationServiceTest`에 fake Sender를 주입해 "`issue`가 발급한 코드를 담아 발송한다"를 검증합니다. 이 테스트가 있으면 위 누락이 CI에서 잡힙니다(발송 호출을 제거하면 실제로 실패하는 것까지 확인).

reference 구현: `MailVerificationService#issue`(`MailSender` 주입 + `MailVerificationMessage`로 목적별 문구)·`SmsVerificationService#issue`, 빈 등록은 `infrastructure:persistence`의 `mail/config/MailDomainConfig`·`sms/config/SmsDomainConfig`(`@Bean` 파라미터로 포트 주입), 테스트는 `MailVerificationServiceTest`·`SmsVerificationServiceTest`. `AuthPasswordResetService`는 이 전환으로 `MailSender` 직접 의존과 문구 상수가 사라져 `mailVerificationService.issue(username, PASSWORD_RESET)` 한 줄이 되었습니다.

## 크로스 컨텍스트 후처리를 AFTER_COMMIT 리스너로 이관할 때의 규칙

**위 "인증코드는 발급과 발송을 원자적으로"와 반대 방향의 선택을 하는 경우가 있습니다.** 두 규칙이 모순이 아니라는 것과, 어느 쪽을 택할지의 판단 기준을 남깁니다.

| 판단 질문 | 예 → 동기(같은 트랜잭션) | 아니오 → AFTER_COMMIT 리스너 |
|---|---|---|
| **후처리가 실패하면 원본도 없던 일이 되어야 하는가?** | 인증코드 발급 — 발송 안 된 코드는 존재 가치가 0 | 추천 등록 — 보상이 늦어도 추천 관계 자체는 유효 |
| 원본과 후처리가 같은 컨텍스트의 불변식인가? | 발급·발송이 곧 "인증코드 정책" 하나 | 추천 관계는 member, 적립은 point |

- **이관의 이득은 컨텍스트 결합 제거입니다**: `ReferralRegistrationService`가 point의 애그리거트·리포지토리를 직접 주입해 적립 불변식(잔액 증가 + EARNED 이력 + 적립 이벤트)을 재구현하던 것을, `PointLedgerService` 단일 원천으로 되돌렸습니다. 그 결과 `member,point` 컨텍스트 순환도 함께 사라졌습니다.
- **⚠️ 트레이드오프를 반드시 상태로 식별 가능하게 만듭니다**: AFTER_COMMIT 리스너가 실패하면 원본은 커밋됐는데 후처리만 유실됩니다. **원본의 "완료" 상태 전이를 후처리와 같은 리스너 안에, 후처리 뒤에 두십시오** — 그러면 실패 건이 `PENDING`에 남아 재처리 대상으로 식별됩니다. 전이를 원본 트랜잭션에 남겨 두면 "완료로 표시됐지만 후처리는 안 된" 건이 되어 사후 추적이 불가능해집니다(`ReferralRegistrationService`는 `reward()` 전이를 리스너로 함께 옮겼습니다).
- **상태 전이 자체는 소유 컨텍스트의 도메인 서비스가 갖습니다**: 리스너가 write 포트를 직접 주입해 `findById → 전이 → save`를 조립하면 그 절차가 infrastructure에 흩어져, 다른 경로(관리자 수동 보정 등)가 생길 때 규칙이 갈립니다. reference: `ReferralRewardCompletionService`.
- **리스너는 `@Transactional(propagation = REQUIRES_NEW)`를 함께 답니다**(`PaymentEventListener` 선례) — `AFTER_COMMIT`은 원본 트랜잭션이 끝난 뒤이므로 새 트랜잭션이 없으면 쓰기가 커밋되지 않습니다.
- **회귀 방어 테스트가 필수입니다**: 리스너 단위 테스트로 (1) 후처리가 실제로 수행되는지, (2) **후처리 실패 시 완료 전이가 일어나지 않는지**를 함께 검증합니다. reference: `ReferralRegisteredEventListenerTest`.

reference 구현: `ReferralRegistrationService`(등록 + 이벤트 발행만) → `ReferralRegisteredEventListener`(적립 2건 → 보상 완료 전이) → `PointLedgerService`·`ReferralRewardCompletionService`.

## 점주 가게 관리(ceo-api) 소유권 검증 규칙

**ceo-api의 모든 가게 관리 엔드포인트는 도메인 계층 호출 전에 소유권을 검증한다.** 점주(`ceoId`)는 자기 소유 가게(`shop.ceoId == ceoId`)에만 접근할 수 있어야 하므로, `ceoapplication/shop/service/ShopOwnershipValidator`(@Component)의 `Shop validateOwnership(Long ceoId, Long shopId)`를 컨트롤러→Service 경로에서 먼저 호출하고, 불일치·미배정 시 `BusinessException(ErrorCode.SHOP_ACCESS_DENIED)`(403)을 던진다. 이 검증기는 domain-module이 아니라 **점주 앱의 application 계층(`ceo-application`)에 둔다** — admin(무제한)·web(회원 관점)과 구분되는 ceo 고유의 인가 관심사이기 때문이다(모듈 경계 규칙의 "도메인 포트 없는 관심사는 presentation에" 원칙과 일관). `CustomUserDetails`는 `ceoId`만 노출하므로 `shopId`는 경로/바디로 받아 이 검증기로 확인한다.

- **점주-가게 연결**: `Shop`에 `ceoId`(nullable, `@Convert` 없이 raw `Long` FK) 컬럼을 두어 1점주 N가게를 표현한다. 배정은 admin-api의 `ShopCreateCommand.ceoId`로 관리자가 수행하고, `Shop` 도메인은 `assignCeo(Long)`로 배정한다. `ShopSearchCondition.ceoId`로 "내 가게" 목록을 필터링한다.
- **개별 리소스 삭제/변경 시 소유권 한계**: 하위 리소스 식별자만 경로에 있고(예: `/v1/phone-numbers/{phoneNumberId}`) infra query DAO에 해당 단건→shopId 역조회 메서드가 없으면 소유권 검증을 생략하고 그대로 위임한다. 이런 지점은 Service에 한계를 주석으로 명시하며, 향후 `shop/query/ShopQueryDao`에 `findXxxById`(shopId 포함) 조회를 추가해 검증을 강화할 수 있다. 역조회가 가능한 경우(영업시간/휴게시간)는 `ShopQueryService.findShopBusinessHourById`/`findShopBreakTimeById`로 대상의 shopId를 얻어 검증한다.

## 이미지 변경 승인 워크플로 규칙 (공용 `ApprovalStatus`)

**"점주 이미지 변경요청 → 관리자 검수 → 승인 시 반영"이 반복되는 상표·대표이미지는 도메인마다 status를 두지 않고 공용 애그리거트 하나로 통합한다.** `shop/model/ShopImageChangeRequest`(imageType: `ShopImageType`[TRADEMARK/THUMBNAIL], imageFileId, status, rejectReason)가 `core/shared/model/ApprovalStatus`(PENDING/APPROVED/REJECTED)를 상태로 갖고, `approve()`/`reject(reason)` 상태전이(PENDING이 아니면 `SHOP_IMAGE_CHANGE_REQUEST_NOT_PENDING`)를 수행한다. `approveImageChange`는 승인과 동시에 `Shop.changeTrademarkImage`/`changeThumbnailImage`로 반영하고 명시적 save한다. 같은 가게·같은 imageType에 PENDING이 2건 생기지 않도록 `requestImageChange`가 `existsByShopIdAndImageTypeAndStatus(..., PENDING)`으로 중복을 막는다(`SHOP_IMAGE_CHANGE_REQUEST_ALREADY_PENDING`). 가게 노출정지 변경은 PENDING 요청이 있으면 `SHOP_STATUS_CHANGE_BLOCKED_BY_PENDING_REQUEST`로 차단한다.

- **승인 vs 즉시반영 구분**: 검수 대상이 명확한(이미지) 것만 승인 워크플로를 쓴다. 가게소개·찾아오는길은 금칙어 검수 통과 시 **즉시 반영**, 콘텐츠보드는 **즉시 노출 + 관리자 사후 숨김(`hide()`)/삭제**로 처리한다(배민 PDF 원문 동작과 일치).
- **이미지 규격 검증**: 형식/용량/해상도/비율 검증은 presentation(`ceoapi/shop/ShopImageSpecValidator`)에서 `ImageIO`로 업로드 전 수행하고, 통과분만 `FileService`로 업로드한다(도메인 계층은 fileId만 받음). 검증 실패는 `SHOP_IMAGE_SPEC_INVALID`로 통일한다.

## 금칙어 검수 규칙 (read-only `ProhibitedWord` + 공용 Validator)

**가게소개(500자)·찾아오는길(200자) 등 점주 입력 텍스트는 저장 전 공용 금칙어 검증을 통과해야 한다.** `shop/model/ProhibitedWord`는 Java 계층에 생성 경로가 없는 **read-only 애그리거트**(`reconstitute`만 공개, SQL 시드로 `PROHIBITED_WORD` 테이블 관리 — search 도메인 `RecommendedKeyword` 선례)이고, `shop/application/ProhibitedWordValidator`(@Component)의 `List<String> findViolations(String)`·`void validate(String)`(위반 시 `SHOP_TEXT_PROHIBITED_WORD`)를 command 서비스가 저장 직전 호출한다. ceo-api는 사전 검증 엔드포인트(`.../introduction/validate`)로 위반 단어 목록을 미리 반환한다.

reference 구현: `shop` 도메인의 점주 관리 기능 전반 — 소유권 `ShopOwnershipValidator`, 승인 워크플로 `ShopImageChangeRequest`/`ApprovalStatus`, 금칙어 `ProhibitedWordValidator`/`ProhibitedWord`, 그리고 이 규칙을 따르는 ceo-api `shop/` 하위 도메인별 컨트롤러·Service(영업시간·휴무일·전화번호·상태·소개·편의정보·상표·콘텐츠보드·임시중지·위생) 및 admin-api의 검수 API(`ShopImageChangeAdminApiController` 등).

## 요청 인덱스 동기화 규칙 (파생 읽기모델은 원본 전이와 같은 트랜잭션에서 Recorder 경유)

**"관리자 처리를 기다리는" 성격의 애그리거트를 새로 만들면, 그 도메인 서비스의 <b>모든 상태 전이</b>에 `ShopRequestIndexRecorder`를 배선한다.** 요청처리 현황(`SHOP_REQUEST_INDEX`)은 유형별 원본 테이블이 분리돼 있는데도 통합 목록·상세·취소·문의를 단일 식별자로 제공하기 위한 파생 읽기모델이며, 배선이 빠지면 그 유형의 요청이 <b>목록에서 아예 보이지 않거나</b> 상태가 영구히 어긋난다.

- **인덱스는 파생 읽기모델이고 진실원은 원본 애그리거트다.** 상세 조회는 인덱스에서 `requestType`/`sourceRequestId`만 얻어 유형별 원본을 투영하고, `status`·`rejectReason`도 원본 값으로 응답한다. 이렇게 두면 drift가 생겨도 영향 범위가 "목록 배지" 하나로 좁혀진다. 반대로 상세까지 인덱스 값으로 응답하면 drift가 곧 오답이 된다.
- **도메인 이벤트·`@TransactionalEventListener(AFTER_COMMIT)`를 쓰지 않는다.** 기록 유실이 곧 "요청이 목록에서 사라짐"이므로 원본 상태 전이와 같은 트랜잭션에서 동기 기록한다(`ShopChangeHistoryRecorder`가 동기 기록을 택한 것과 같은 이유).
- **배선 지점은 domain-module 도메인 서비스로만 한정한다.** api 모듈 CommandService에 배선하면 같은 전이가 ceo/admin 두 모듈에 흩어져 한쪽이 반드시 빠진다. 도메인 서비스는 이미 원본 애그리거트를 손에 들고 있어 추가 조회가 0회다. Recorder를 **생성자 필수 의존**으로 받아, 새 전이 메서드를 만들 때 배선 필요성이 컴파일 단계에서 드러나게 한다.
- **`UNIQUE (request_type, source_request_id)`가 멱등성의 구조적 보증이다.** 배선 중복은 즉시 드러나고 조용한 중복행이 생기지 않는다.
- **`syncStatus`가 인덱스 행을 못 찾으면 `SHOP_REQUEST_NOT_FOUND`로 실패시켜 원본 트랜잭션을 롤백한다.** 조용히 무시하면 원본만 전이돼 목록이 영구히 어긋난다(백필 누락이 이 경로로 드러난다).
- **원본 → 통합 상태 매핑은 Recorder의 private static 메서드가 소유한다.** 공용 `ApprovalStatus`에 shop 요청 전용 변환 메서드를 넣지 않는다(공용 enum이 특정 컨텍스트를 알게 되는 역방향 의존). 매핑은 `valueOf`가 아니라 **switch**로 쓴다 — 어느 한쪽에 상수가 추가되면 컴파일이 깨져 누락이 드러난다.
- **전이 메서드 목록에 대해 항목별 테스트를 쓴다.** 전이를 추가하고 배선을 잊는 실수는 리뷰가 아니라 테스트가 잡아야 한다.
- **변경이력과 기록 범위가 다르다.** `ShopChangeHistory`는 점주의 **요청 시점만** 남긴다(검수는 "가게 설정 변경"이 아니다). 인덱스는 **모든 전이**를 남긴다("내가 낸 요청이 어떻게 처리됐는가"에서는 검수 결과가 곧 본문이다). 한쪽 규칙을 다른 쪽에 복사하지 말 것.
- **조회 기간 상한을 두지 않는다.** 변경이력의 6개월 제한(`SHOP_CHANGE_HISTORY_DATE_OUT_OF_RANGE`)을 대칭성을 이유로 복제하지 않는다 — 요청처리 현황은 오래된 건도 반려 사유·재요청 근거로 열람해야 한다. 이 판단은 `ShopRequestQueryService` Javadoc에 남아 있다.

reference 구현: `domain-module`의 `shop/service/ShopRequestIndexRecorder`(매핑 2개 + `syncCanceled` + 가게 일치 재검증), 배선 지점 `ShopImageApprovalService`(요청·승인·반려 3곳)·`ShopDeliveryAreaAdjustmentService`(접수·개시·완료·반려 4곳), 읽기 측 `infrastructure:persistence`의 `shop/query/ShopRequestQueryDao`, 봉인 테스트 `ShopRequestIndexRecorderTest`·`ShopImageApprovalServiceTest`·`ShopDeliveryAreaAdjustmentServiceTest`, fake `RecordingShopRequestIndexRepository`.

## 요청 취소 규칙 (CANCELED는 원본 enum에 두고, 대기중만 취소)

**점주의 요청 취소는 인덱스가 아니라 <b>원본 애그리거트의 상태 전이</b>다.** 인덱스에만 `CANCELED`를 두면 진실원이 실제로 갈라진다 — 원본이 `PENDING`으로 남아 (1) `existsByShopIdAndImageTypeAndStatus(PENDING)` 중복 차단이 **취소 후에도 재요청을 막고**, (2) 관리자가 이미 취소된 요청을 승인·반려할 수 있어 인덱스는 CANCELED인데 가게 이미지가 실제로 교체된다.

- **취소 가능 조건은 `PENDING`만이다.** `IN_PROGRESS`(배달지역 조정)는 이미 외부(가맹본부)에 자료가 전달된 뒤라 플랫폼이 일방 취소하면 외부 절차와 시스템 상태가 어긋난다. 시도는 409 `SHOP_REQUEST_NOT_CANCELABLE`.
- **에러코드는 유형별이 아니라 통합 코드 하나를 쓴다.** 기존 `SHOP_IMAGE_CHANGE_REQUEST_NOT_PENDING`류를 재사용하지 않는다 — 취소는 통합 화면의 단일 동작이므로 프론트가 유형별 에러코드 N종을 알 필요가 없어야 한다.
- **상태값을 추가할 때는 그 enum의 종결 판정 가드를 전수 재점검한다.** `ShopDeliveryAreaAdjustmentRequest#reject()`의 종결 조건이 `COMPLETED`/`REJECTED`뿐이어서 `CANCELED`를 빠뜨리면 **관리자가 취소된 신청을 반려할 수 있다.** `!= PENDING` 형태의 가드는 값 추가에 자동으로 안전하지만, **값을 열거하는 가드는 그렇지 않다** — enum에 상수를 추가하면 `grep`으로 그 enum의 사용처를 전수 확인하고 exhaustive switch·열거형 가드를 함께 고친다.
- **부수효과 해제는 코드 추가 없이 자동이다**(이 설계의 이점) — 중복 차단·노출정지 차단·재신청 차단이 모두 "PENDING/OPEN 상태 조회"에 기반하므로 CANCELED가 되면 함께 풀린다.
- **취소 주체는 점주만이다.** 관리자 종결은 "반려"(사유 필수)로 표현해 사유 없는 취소와 섞지 않는다. 취소 동기화는 `rejectReason`을 비운다.
- **취소된 요청의 업로드 파일은 삭제하지 않는다**(첨부 이력 보존, 상세에서 계속 열람).
- **유형 분기는 domain-module의 취소 전용 도메인 서비스가 갖는다.** 취소 가능 조건이 애그리거트 불변식이므로 판정이 도메인에 있어야 하고, api 모듈에 두면 CommandService가 write 포트 2개를 알게 된다.
- **`VARCHAR(n)` status 컬럼에 상수를 추가할 때 길이를 확인한다.** `CANCELED`(8자)는 기존 `VARCHAR(20)`에 들어가 `ALTER`가 불필요했고 `schema.sql`의 허용값 주석만 갱신했다. 길이를 넘기면 DDL이 필요하며, 그때는 `docs/tasks/*.sql`로 작성해 사용자에게 적용을 요청한다.

reference 구현: `ApprovalStatus.CANCELED`·`DeliveryAreaAdjustmentStatus.CANCELED`, `ShopImageChangeRequest#cancel`·`ShopDeliveryAreaAdjustmentRequest#cancel`(+`reject` 종결 조건에 CANCELED 추가), `ShopRequestCancelService`(유형 분기), 봉인 테스트 `ShopRequestCancelServiceTest`(PENDING만 취소 / IN_PROGRESS 409 / 취소 후 재요청 가능 / 취소된 신청 반려 불가).

## 응답 record 파일/이미지 필드 URL 규칙 (`~FileId` 노출 금지, 표시용 URL만)

**HTTP 응답 record는 파일 식별자(`~FileId`/`List<Long> ~FileIds`)를 그대로 노출하지 않고, 서버가 만든 표시용 URL 필드(`~ImageUrl`·`~Url`/`List<String> imageUrls`)로 대체합니다.** 프론트엔드가 fileId만 받으면 표시용 URL을 얻을 공식 경로가 없어 `${API_URL}/api/files/v1/{fileId}` 같은 **존재하지 않는 엔드포인트를 추측 조립**하게 됩니다(파일 API는 `POST /api/files/v1/upload` 업로드 전용이고 GET 단건 조회가 없음). 파일 URL은 Firebase Storage 경로 인코딩(`?alt=media`)이 필요해 **서버만 생성**할 수 있으므로, 응답 계약이 URL을 직접 내려주는 것이 유일하게 올바른 방식입니다. 이 규칙은 [Request/Response record `@Schema` 문서화 규칙](#requestresponse-record-schema-문서화-규칙)·[DTO 조립 규칙](#dto-조립-규칙-new-직접-호출-지양)의 파일 필드 케이스 구체화입니다.

- **적용 대상**: web-api/admin-api/ceo-api의 조회(GET) 응답 record 및 파일 식별자를 담던 생성/수정 응답 record. `~FileId`(Long)는 `~ImageUrl`/`~Url`(String)로, `List<Long> ~FileIds`는 `List<String> imageUrls`로 바꿉니다. **fileId는 응답에서 완전히 제거**합니다(url과 병기하지 않습니다).
- **변환 방법 (개정됨 — query DAO가 URL을 완성해 투영)**: Response record 자체는 `domain-free`·`infra-free`(`com.tastyhouse.domain.*`·`com.tastyhouse.infrastructure.*` 미import)를 유지하되, **filePath→url 변환은 Service가 아니라 infrastructure query DAO가 조회 시점에 끝냅니다.** Result record가 이미 `~Url`(String)을 담은 채 나오고, Service는 그 값을 그대로 응답에 전달만 합니다(파일에 대해 아무것도 알지 않음). 상세는 아래 [파일 URL 조립 위치 규칙](#파일-url-조립-위치-규칙-query-dao가-fileurlresolver로-완성)을 참고합니다.
  - **Service에 fileId→url 변환 매퍼를 두지 않습니다.** 과거 이 자리에 있던 `fileService.getUrlByPath(...)`·`getUrlByFileId(...)`·`getUrlsByFileIds(...)`·`findFileResponse(...)`는 전부 제거됐고, 각 api 모듈 `FileService`는 업로드 전용으로 축소됐습니다.
  - Result에 URL 필드가 없다면 그것이 신호입니다 — Service에서 변환하지 말고 **DAO에 파일 join을 추가**합니다.
  - `null` 가드는 DAO 쪽 `FileUrlResolver.resolve`가 담당합니다(경로 없으면 `null`). 리스트는 `List.of()`로 정규화하고 변환 실패분은 걸러냅니다.
- **적용 제외**: Request/Command(업로드 시 fileId 수신은 그대로), 이미 `FileResponse(id, name, url)`처럼 url을 동반하는 타입(이때도 url은 DAO가 투영한 값을 씁니다).
- **적용 시점**: 신규 작성은 이 규칙을 따르고, 기존에 fileId를 노출하던 응답은 해당 파일을 수정할 때 함께 전환합니다.

reference 구현: ceo-api — `ShopImageStatusResponse.currentImageUrl`·`ShopImageChangeRequestItemResponse.imageUrl`·`ShopContentBoardResponse.imageUrl`·`ShopDetailResponse.thumbnailImageUrl`/`trademarkImageUrl`(전부 `ShopQueryDao`가 join으로 URL까지 완성해 투영). admin-api — `ShopAmenityCategoryResponse`/`ShopFoodTypeCategoryResponse`/`ShopBannerImageItemResponse`/`ShopPhotoCategoryImageItemResponse`/`ShopDetailResponse`, `ShopContentBoardListItemResponse`/`ShopImageChangeRequestItemResponse`(fileId 제거). web-api — `BugReportResponse.imageUrls`/`ReviewResponse.imageUrls`. `FileResponse(id, name, url)`를 유지하는 사례: admin-api의 `BannerQueryService`·`EventQueryService`·`RankQueryService`·`BugReportQueryService`(DAO가 `~FileId`·`~FileName`·`~Url` 3필드를 함께 투영하고 Service는 `FileResponse.of(...)`로 묶기만 함).

## 파일 URL 조립 위치 규칙 (query DAO가 `FileUrlResolver`로 완성)

**파일 저장 경로(`filePath`)를 표시용 URL로 바꾸는 변환은 api 모듈 Service가 아니라 infrastructure query DAO가 조회 시점에 수행합니다.** Result record는 `~FilePath`가 아니라 **`~Url`을 담은 채** 나오고, 소비 Service는 그 값을 그대로 응답에 전달합니다.

- **왜 DAO인가**: 전환 전에는 같은 변환(`fileService.getUrlByPath(dto.xxxFilePath())`)이 60여 개 호출부에 흩어져 있었고, 그 결과 세 모듈의 `FileService`가 서로 다르게 드리프트했습니다(web엔 배치 변환이 없고, ceo엔 경로 변환이 없고, `findFileResponse`는 admin에만 존재). 더 나쁜 것은 Result가 `Long fileId`만 담던 11개 경로로, 응답 조립 중에 파일을 **다시 조회**해 추가 DB 왕복이 발생했습니다. 표현 목적 read model을 화면이 필요로 하는 형태로 완성해 내려보내는 것은 CQRS read 측의 정상 책임이므로(Microsoft Learn의 CQRS·Materialized View 가이드가 계산·변환된 값을 read view에 포함하도록 권장), 변환 지점을 read 어댑터 한 곳으로 모읍니다.
- **변환기**: `infrastructure:persistence`의 `file/query/FileUrlResolver`(`@Component`) 하나만 씁니다. 도메인 출력 포트 `FileStoragePort`를 주입받아 `resolve(String filePath)`·`resolveAll(Map<Long,String>)`·`resolveAll(Collection<String>)`를 제공합니다. driven 어댑터가 도메인 포트를 사용하는 형태라 의존 방향(안쪽)이 유지되며, infrastructure는 이미 domain-module을 `api`로 의존하므로 새 모듈 의존이 생기지 않습니다.
- **SQL로 URL을 만들지 않습니다**: Firebase는 `URLEncoder.encode(path).replace("+","%20") + "?alt=media"`, S3는 `baseUrl + "/" + path`로 규칙이 다르고 `baseUrl`은 환경 설정값입니다. `CONCAT`으로 재현하면 인코딩이 깨지고 설정이 하드코딩되므로, **조회 직후 Java에서 매핑**합니다.
- **`@QueryProjection`과의 관계**: `@QueryProjection`은 record 생성자로 직접 투영하므로 변환을 투영식에 끼울 수 없습니다. 따라서 DAO는 `uploadedFileJpaEntity.filePath`를 그대로 투영한 뒤 **fetch 직후 재조립**합니다 — Result 타입마다 private `withResolvedXxx(Result row)` 헬퍼를 두고 `new XxxResult(...)`로 URL 슬롯만 `fileUrlResolver.resolve(...)`로 바꿔 다시 만듭니다.

```java
List<BannerListItemResult> banners = queryFactory
    .select(new QBannerListItemResult(..., uploadedFileJpaEntity.filePath, ...))
    .from(bannerJpaEntity)
    .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(bannerImageFileId()))
    .fetch()
    .stream()
    .map(this::withResolvedImageUrl)   // filePath → URL
    .toList();
```

- **record 재조립은 위치 기반이라 주의합니다**: 같은 `String` 필드가 여러 개면(썸네일 URL vs 상표 URL, active 아이콘 vs inactive 아이콘) 순서를 바꿔도 컴파일은 되고 값만 조용히 뒤바뀝니다. record 선언 순서와 `new` 호출 인자 순서를 한 필드씩 대조합니다([DTO 조립 규칙](#dto-조립-규칙-new-직접-호출-지양)의 동일 경고).
- **캐싱하지 않습니다**: `FileStoragePort.getFileUrl`은 네트워크·SDK·DB 접근이 없는 순수 문자열 연산이라 행 단위 반복 호출에 비용이 없습니다. 캐싱은 값비싼 연산에 쓰는 수단이며, 여기 도입하면 `baseUrl` 변경 시 무효화 책임만 새로 생깁니다.
- **DB에는 계속 경로를 저장합니다**: 절대 URL을 저장하면 `baseUrl` 변경·Firebase 토큰 무효화 시 저장된 값이 통째로 썩습니다. 스키마(`UPLOADED_FILE.file_path`)는 그대로 두고 읽기 시점에만 URL을 만듭니다.
- **fileId만 있고 경로가 없으면 join을 추가합니다**: Service에서 파일을 재조회하지 않습니다. 애그리거트(`Shop`·`Member`)에서 fileId를 꺼내 변환하던 경로도, 그 로드가 표현 목적뿐이면 [write 포트 잔류 판정 기준](#write-포트-잔류-판정-기준-domain-repository에-남길-조회의-경계)에 따라 DAO 투영으로 옮깁니다(예: `ShopQueryDao#findShopImageUrls`, `MemberQueryDao#findProfileImageUrl`).
- **left join 미스와 예외 의미**: 파일이 필수 자산인 화면에서는 join이 비어 URL이 `null`이 되는 경우를 Service가 검사해 기존 예외를 유지합니다(reference: admin-api `EventQueryService`가 `fileId != null && url == null`이면 `FILE_NOT_FOUND`를 던져 과거 `findFileResponse`의 의미를 보존).
- **api 모듈 `FileService`는 업로드 전용입니다**: 세 모듈(`webapi`/`adminapi`/`ceoapi`)의 `file/FileService`는 `upload(MultipartFile)` + `readBytes` 만 갖는 **완전히 동일한 파일**입니다(패키지 선언만 다름). `MultipartFile`이 spring-web 타입이라 프레임워크-프리인 domain-module에 둘 수 없어 이 얇은 어댑터만 모듈별로 남으며, 이는 `ApiResponse`/`PageRequest`/`PaginationResponse`의 모듈별 중복 관례와 같습니다. 업로드 규칙 본체는 domain-module의 `FileUploadService` 한 곳이 소유합니다.
- **write 포트에 표현용 조회를 두지 않습니다**: `UploadedFileRepository`는 `save`·`findById`만 노출합니다. 과거의 `findFilePath`(default)·`findFilePaths`(배치)는 화면에 뿌릴 값을 얻기 위한 조회여서 잔류 기준에 맞지 않았고, 전환 후 호출부가 0이 되어 제거했습니다. `FileUploadService.getUrlByPath`도 같은 이유로 제거됐습니다(읽기 변환은 `FileUrlResolver` 소유).

reference 구현: `infrastructure:persistence`의 `file/query/FileUrlResolver` + 이를 주입하는 14개 query DAO(`BannerQueryDao`가 가장 단순한 기준 예시 — 단건·목록·상세 3개 메서드 전부 이 형태). 파일 join이 새로 추가된 사례: `ShopQueryDao`(콘텐츠보드·이미지변경요청·`findShopImageUrls`), `EventQueryDao#findEventDetailById`(썸네일·배너 2개 alias join), `BugReportQueryDao#findImages`(`BugReportImageResult`로 분리), `MemberQueryDao#findProfileImageUrl`. **이 규칙에는 예외가 없습니다.** 과거 `OrderProductResult.imageUrl`이 "주문 시점에 이미 URL로 스냅샷된 값"이라는 이유로 유일한 예외로 기재돼 있었으나, `OrderPlacementService`가 그 컬럼(`ORDER_PRODUCT.image_url`)에 넣던 값은 `UPLOADED_FILE.file_path`인 **저장 경로**여서 전제가 사실과 달랐습니다. 그 결과 주문 상세 응답이 호스트 없는 경로(`2026/04/....png`)를 그대로 내려보내 프론트엔드 `next/image`가 크래시하는 장애가 발생했습니다.

**교훈: 파일 참조는 경로 문자열이 아니라 `UPLOADED_FILE.id`로 스냅샷하십시오.** 이 사고 후 `ORDER_PRODUCT`는 `image_url`(경로 문자열) → `image_file_id`(파일 ID) 로 전환했습니다. 파일 ID를 들고 있으면 조회 시 `UPLOADED_FILE`을 join해 경로를 얻고 resolver를 거치는 것이 **유일하게 가능한 형태**가 되어, "이 컬럼에 URL이 들었나 경로가 들었나"라는 혼동 자체가 성립하지 않습니다. 값 스냅샷(`name`·`original_price`처럼 주문 시점 값을 박제)이 필요한 경우에도 이미지만은 ID 참조가 맞습니다 — `UPLOADED_FILE` 행은 불변이라 과거 주문이 당시 파일을 계속 가리키므로 **이력 보존도 함께 만족**합니다.

새 컬럼을 설계할 때: 파일을 가리키는 컬럼명은 `~_file_id`로 짓고 타입은 `BIGINT`를 씁니다. `~_url` 이름의 컬럼을 만나면 저장값이 URL이라고 가정하지 말고 **그 컬럼에 값을 넣는 write 경로를 확인**하십시오.

## 낙관적 락 재시도 배치 규칙 (재시도 루프는 트랜잭션 경계 **밖**, 별도 Executor 빈)

**동시성 경합(낙관적 락 `@Version`·유니크 제약 충돌)을 재시도해야 하는 명령은, 재시도 루프를 트랜잭션 안에 두지 않고 "재시도 루프(비트랜잭션) → 트랜잭션 경계 빈 → 도메인 서비스" 3단으로 분리합니다.** 재시도는 매 시도가 **새 트랜잭션**이어야 의미가 있는데, 같은 빈의 메서드를 호출하면 Spring 프록시를 거치지 않아(self-invocation) `@Transactional`이 적용되지 않고, 첫 시도에서 rollback-only로 표시된 트랜잭션을 그대로 재사용해 재시도가 무의미해집니다. 또한 도메인 서비스는 순수 POJO(패턴 1)라 `@Transactional`을 가질 수 없으므로, 트랜잭션 경계를 담당하는 얇은 빈이 별도로 필요합니다.

- **3단 구조와 각 층의 책임**:
  1. **`{도메인}CommandService`** (소비 모듈, `@Transactional` **없음**) — 재시도 루프(`MAX_RETRY`)와 경합 예외 판별만. 재시도 소진 시 도메인 의미의 실패로 변환(예: `RESERVATION_SLOT_FULL`).
  2. **`{도메인}{동작}Executor`** (소비 모듈, `@Component` + `@Transactional`) — 한 번의 시도를 독립 트랜잭션으로 감싸는 얇은 위임. 비즈니스 로직을 갖지 않습니다.
  3. **도메인 서비스** (domain, 순수 POJO) — 불변식 본체. 경합을 **재시도하지 않고**, 충돌이 커밋 전에 드러나도록 `saveAndFlush`로 노출만 시킵니다.
- **경합 예외 판별**: 프레임워크-프리 `OptimisticLockConflictException`(기존 행 동시 update — infra 어댑터가 `ObjectOptimisticLockingFailureException`을 catch해 번역)과 `DataIntegrityViolationException`(신규 행 동시 insert 시 유니크 충돌) **두 가지만** 재시도합니다. 비즈니스 예외(정원 마감·중복·약관 미동의 등)는 재시도하지 않고 즉시 전파합니다 — 재시도해도 결과가 같으므로 지연만 늘어납니다.
- **커밋 전 노출이 필수**: 충돌이 트랜잭션 커밋 시점에야 터지면 이미 루프를 벗어나 재시도 루프가 잡을 수 없습니다. 그래서 경합 지점의 write 포트에 `saveAndFlush`를 두어 **메서드 내부에서** 충돌을 유발합니다(일반 `save`와 구분되는 존재 이유).
- **응답 조립은 재조회로**: CommandService가 트랜잭션을 열지 않으므로 명령 결과를 그대로 응답에 쓸 수 없습니다. 명령은 식별자(`Long`)만 반환하고, 컨트롤러가 커밋 완료 후 `{도메인}QueryService`로 재조회해 Response를 조립합니다(CQRS 분리와도 일관 — 응답 조립은 QueryService 책임).

reference 구현: `reservation` 도메인 — `webapi/reservation/ReservationCommandService#createReservation`(재시도 루프, 비트랜잭션) → `webapi/reservation/ReservationBookingExecutor#bookInNewTx`(`@Transactional`) → `core/.../reservation/service/ReservationBookingService#book`(정원 차감 + 예약 저장 원자 연산, `slotRepository.saveAndFlush`로 충돌 노출), 예외 번역은 `infrastructure/reservation/persistence/ReservationSlotRepositoryImpl`. 이 프로젝트에서 재시도 루프를 가진 유일한 경로입니다.

## 등록(POST) API 응답 본문 규칙 (생성된 `Long` id만 반환)

**리소스를 등록하는 POST 엔드포인트는 예외 없이 `ResponseEntity<ApiResponse<Long>>`로 생성된 PK 하나만 반환합니다.** 생성 결과를 담는 래퍼 record(`XxxCreateResponse`)를 만들지 않고, 생성 직후 `{도메인}QueryService`로 재조회해 전체 상세 DTO를 반환하지도 않습니다. 상세가 필요한 클라이언트는 반환받은 id로 별도 상세 조회(GET)를 호출합니다.

과거에는 모듈마다 등록 응답 형태가 갈려 있었습니다 — `ceo-api`는 11개 등록 API 전부가 `Long`, `admin-api`는 37개 중 30개가 `Long`이었던 반면, **`web-api`만 등록 8종이 전부 "생성 → QueryService 재조회 → 전체 DTO 반환" 형태**였고 그 외에 `Void`(본문 없음)나 id 한 개짜리 래퍼 record(`AdminCreateResponse`·`OrderCreateResponse`)를 반환하는 지점이 섞여 있었습니다. 같은 코드베이스에서 "등록하면 무엇이 돌아오는가"가 모듈마다 다르면 프론트엔드 연동·리뷰·패턴 일치가 모두 어려워지므로, 이미 다수파(43개 중 41개)였던 `Long` id 형태로 전면 통일했습니다.

- **왜 재조회 DTO가 아닌가**: (1) 커밋 직후 **추가 SELECT 라운드트립**이 발생합니다. (2) 그 재조회만을 위해 존재하는 QueryService 메서드(`getBugReportResponse`·`getPartnershipRequestResponse`·`getCommentResponse` 등, javadoc에 "생성 응답 재조립용"이라 명시돼 있던 것들)가 생겨 조회 API가 실제로 노출하는 계약과 무관한 메서드가 쌓입니다. (3) 등록 응답이 상세 DTO를 그대로 물면, 상세 응답 스키마가 바뀔 때 등록 API 계약까지 함께 깨집니다. (4) [CQRS 분리 규칙](#api-모듈-application-서비스-cqrs-분리-규칙-도메인commandservice도메인queryservice)이 이미 "명령은 식별자만 반환한다"를 요구하는데, 컨트롤러에서 다시 DTO로 부풀리면 그 경계가 HTTP 계층에서 무효화됩니다. 이 규칙은 그 원칙을 **컨트롤러 반환 타입까지** 일관되게 확장한 것입니다.
- **`Void` 반환도 금지**: 행을 생성하고도 본문 없이 `ApiResponse<Void>`를 반환하던 등록 API(회원가입·팔로우·상품 옵션/이미지 등록 등)도 전부 id를 반환합니다. 대부분 `repository.save(...)`의 반환값을 버리고 있었을 뿐이라, 도메인 서비스에서 저장 결과를 받아 식별자를 반환하도록 시그니처를 바꾸면 됩니다.
- **벌크 등록은 `ApiResponse<List<Long>>`**: 한 번에 여러 건을 등록하는 API는 생성된 id 목록을 반환합니다(reference: `ceo-api`의 `ShopSuspensionApiController#createSuspension`).
- **HTTP 상태코드는 이 규칙의 대상이 아닙니다**: 기존 값(대부분 200, 일부 201)을 그대로 유지합니다. 이 규칙은 응답 **본문의 형태**만 정합니다.
- **적용 제외 (리소스 등록이 아닌 POST)**: 아래는 POST이지만 "리소스 등록"이 아니므로 기존 반환 타입을 유지합니다.
  - **파일 업로드**: `FileApiController#upload` — 이미 `Long fileId`를 반환해 규칙에 부합합니다.
  - **인증·토큰 발급**: `login`·`refresh`·소셜 로그인·`signUpSocialAccount`·`phoneLogin`·`verifyPasswordReset`·`verifyPassword`·인증코드 확인(`SmsVerificationTokenResponse` 등) — 발급된 토큰이 응답의 본질입니다.
  - **검증 전용**: `ceo-api`의 `ShopIntroductionApiController#validateIntroduction`(금칙어 위반 목록 반환).
  - **토글·상태전이**: `toggleBookmark`·`toggleReviewLike`·payment `confirm`/`cancel`/`refund`·reservation `confirm`/`reject`/`complete`·`withdraw` 등.
  - **POST-as-query**: 요청 본문으로 조건을 받는 조회(`ProductApiController#getProductsBatch`).
  - **배치·집계·원장 기록**: `RankApiController#aggregate`, point `earn`/`deduct`(적립·차감은 원장 기록이지 리소스 등록이 아니므로 `Void` 유지).
- **적용 시점**: 신규 등록 API는 이 규칙을 따르고, 기존 등록 API도 이 규칙에 맞춰 전면 전환을 완료했습니다. 등록 응답 전용 래퍼 record와 재조회 전용 QueryService 메서드는 함께 삭제합니다(단, 그 record가 GET·PUT 등 다른 경로에서도 쓰이면 남깁니다 — 예: `ReviewCommentResponse`/`ReviewReplyResponse`는 댓글 목록 조회의 중첩 요소로 계속 사용되므로 유지).

reference 구현: `ceo-api`의 shop 하위 컨트롤러 전체(등록 API 11개가 전부 `ApiResponse<Long>`, 벌크는 `List<Long>`), `admin-api`의 `ShopApiController`(등록 13개 전부 `Long`)·`NoticeApiController#createNotice`. 전환 사례: `web-api`의 `OrderApiController#createOrder`(`OrderCreateResponse` 삭제)·`BugReportApiController#createBugReport`(재조회 제거 + `BugReportQueryService` 주입 제거)·`AuthApiController#signUp`(`Void`→`Long`, 도메인 `MemberRegistrationService#signUp`까지 4개 계층 시그니처 변경), `admin-api`의 `AdminApiController#createAdmin`(`AdminCreateResponse` 삭제, HTTP 201은 유지).

## 집합 불변식 설정 컬렉션은 replace-all PUT으로 교체하는 규칙

**설정 컬렉션의 유효성이 "행 하나"가 아니라 "집합 전체"를 봐야 판정된다면, 개별 행 CRUD를 열지 않고 파트별 `PUT` replace-all 하나로 교체합니다.** 판정 기준은 아래 한 질문입니다.

| 질문 | 답 | 수신 방식 | 사례 |
|---|---|---|---|
| **행 하나가 스스로 유효한가?** | 예 | 행 단위 CRUD (`POST`/`PUT {subId}`/`DELETE {subId}`) | 영업시간·휴게시간·전화번호·배달가능지역 |
| **집합 전체가 함께 유효해야 하는가?** | 예 | **파트별 `PUT` replace-all** (컬렉션 통째 교체) | 배달팁 구간·지역별·시간별 |

- **왜 행 단위 CRUD면 안 되는가**: 배달팁 구간의 규칙은 "3개 이하 **+** 주문금액 오름차순 **+** 팁 내림차순"이라 행 하나만 보고는 판정할 수 없습니다. 행 단위로 열면 어떤 순서로 조작해도 **중간 상태가 규칙을 위반**합니다 — 예를 들어 2구간(5,000원/2,000원, 10,000원/1,500원)을 (8,000원/1,800원)으로 바꾸려면 삭제→추가든 추가→삭제든 한 시점에 반드시 규칙 위반 상태를 거칩니다. 서버가 그 중간 상태를 거부하면 정상적인 변경이 불가능해지고, 허용하면 불변식이 사실상 없는 것이 됩니다. `ShopBusinessHour`가 개별 CRUD인 것은 요일 간에 이런 관계가 **없기** 때문이며, 규칙의 차이지 취향의 차이가 아닙니다.
- **여러 리소스에 걸친 불변식도 같은 이유로 한 트랜잭션·한 컨트롤러**: 배달팁의 거리별↔지역별 상호 배타는 두 리소스에 걸친 규칙이라, 컨트롤러를 파트별로 쪼개면 배타성 검증이 두 컨트롤러에 흩어집니다. 배달팁 엔드포인트 8개를 컨트롤러 하나가 소유하는 이유입니다.
- **불변식의 물리적 소유자를 행 하나에 둡니다**: 배타성이 어느 행에도 소유자 없이 서비스 코드에만 떠 있으면 동시 요청에 뚫립니다. `SHOP_DELIVERY_TIP_SETTING`은 `UNIQUE(shop_id)` 행 하나가 `extra_tip_type`을 들고 있어 그 행이 배타성의 단일 소유자가 됩니다(그래서 거리별 설정을 별도 테이블로 쪼개지 않고 이 헤더에 인라인했습니다).
- **빈 배열은 "전부 삭제"입니다**: `PUT`에 빈 목록을 보내면 해당 파트가 비워집니다. 그래서 "지역별을 전부 삭제해야 거리별을 설정할 수 있다"는 규격이 별도 분기 없이 자동 성립합니다.
- **개수 규칙은 도메인이 판정하게 두고 Bean Validation으로 가로채지 않습니다**: 구간 목록에 `@NotEmpty`를 붙이면 "0개"만 400 검증 오류로 가로채이고 "4개"는 도메인 에러코드로 내려가, **같은 개수 위반이 입력값에 따라 다른 code로 응답**되어 프론트 분기가 갈립니다. `@NotNull`만 두고 개수 판정은 도메인 한 곳(`SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED`)에 맡깁니다.
- **부수 이점**: 모든 엔드포인트가 `{id}`(shopId)를 갖게 되므로, `ShopPhoneNumberApiController`가 겪은 "하위 리소스 id만 있어 소유권 검증을 생략할 수밖에 없는" 한계가 구조적으로 발생하지 않습니다.

reference 구현: `ceo-api`의 `ShopDeliveryTipApiController`(파트별 replace-all PUT 8개, 한 컨트롤러 소유) + 도메인 측 `ShopDeliveryTipService#replaceTiers`·`#replaceRegionTips`·`#replaceScheduleTips`. **대비 사례**: `ShopBusinessHourApiController`·`ShopDeliveryAreaApiController`(행 하나가 스스로 유효 → 행 단위 CRUD 유지).

## 리포지토리 없는 순수 계산기의 입력은 `{도메인}Context` record로 묶는 규칙

**리포지토리를 주입받지 않고 넘겨받은 값만으로 판정하는 순수 계산기(`{도메인}Calculator`)의 입력은, 파라미터를 나열하지 않고 `{도메인}Context` record 하나로 묶어 받습니다.**

- **왜인가**: 순수 계산기는 입력이 늘어나는 방향으로만 자라는데(판정 근거가 추가될 때마다 파라미터가 하나씩 붙는다), 파라미터 나열은 개수가 늘수록 **위치 착오가 컴파일을 통과하는** 위험이 커집니다. 특히 `int`·`boolean`·`List` 같은 같은 타입이 여러 개면 순서를 바꿔도 컴파일되고 값만 조용히 뒤바뀝니다. record로 묶으면 호출부가 필드명으로 조립하게 되어 이 실수가 구조적으로 사라지고, 입력이 늘어도 시그니처는 하나만 바뀝니다.
- **적용 대상**: `@Service`/`@Transactional` 없이 리포지토리 주입 0개·인스턴스 상태 0개인 계산기. 리포지토리를 주입받는 도메인 서비스(`ShopDeliveryTipService` 등)는 대상이 아닙니다 — 그쪽은 입력이 식별자 몇 개로 좁습니다.
- **Context는 이미 해석된 값을 담습니다**: 좌표→거리 변환(`GeoDistance`), 날짜→공휴일 판정(`PublicHolidayCalendar`) 같은 조회·변환은 **호출부가 끝내고** 결과값만 담습니다. 이것이 계산기가 리포지토리도 시계도 갖지 않는 순수 함수로 남는 지점이며, Spring·DB 없이 단위 테스트할 수 있는 이유입니다.
- **`of(...)` 정적 팩토리를 함께 둡니다**(DTO 조립 규칙과 동일). 컬렉션 필드의 `null`은 compact constructor에서 빈 목록으로 정규화해, 계산기 본문에 null 분기를 남기지 않습니다.
- **출력도 항목별로 쪼갠 record로 돌려줍니다**: 총액만 반환하면 (1) 화면이 "기본 2,000 + 거리 1,500"처럼 근거를 보여줄 수 없고, (2) 금액 불일치 CS 때 어느 항목이 갈렸는지 추적할 수 없습니다.
- **기존 계산기에 소급 적용하지 않습니다**: 이 규칙만을 위해 재작성하지 않고, 그 계산기의 입력이 다음에 늘어날 때 함께 전환합니다. `ShopOperatingStatusCalculator`가 이 방식으로 전환된 선례입니다 — 파라미터 8개를 나열하던 상태로 두었다가, 주문유형별 주문가능 판정을 위해 `orderMethod`가 9번째 입력으로 추가되는 시점에 `ShopOperatingStatusContext`로 전환했습니다.

reference 구현: `ShopDeliveryTipCalculator#calculate(ShopDeliveryTipContext)` — 입력 11개를 record 하나로 묶고, 출력은 항목별 `ShopDeliveryTipBreakdown`(base/distance/region/schedule/holiday + total)으로 돌려줍니다. `ShopOperatingStatusCalculator#calculate(ShopOperatingStatusContext)` — 입력 9개(`List` 5개 연속)를 묶고, 출력은 상태에 사유를 동반한 `ShopOperatingStatusResult`(status + unavailableReason)로 돌려줍니다. 상태만 돌려주면 화면이 "왜 준비중인지"를 보여줄 수 없고 주문 거절 시 어느 조건에 걸렸는지 추적할 수 없으므로, 출력을 쪼개는 원칙이 여기에도 그대로 적용됩니다. `ScheduledOrderSlotCalculator#calculate(ScheduledOrderSlotContext)`도 같은 형태입니다.

> **전환 시 주의(실제 사례)**: `ShopOperatingStatusCalculator` 전환에서 진짜 위험은 파라미터 순서가 아니라 **재사용 지점의 입력 누락**이었습니다. `ScheduledOrderSlotCalculator`가 이 계산기를 재사용하면서 `orderMethod`를 넘기지 않으면 컴파일은 통과하지만(그 자리에 `null`을 넣으면 됨) 배달만 중지한 가게에서 포장 예약 슬롯까지 사라집니다. Context에 필드를 추가할 때는 **그 계산기를 재사용하는 모든 호출부가 새 필드를 채우는지** 확인하고, 각 호출부마다 회귀 테스트를 둡니다.

## 시크릿 파일 로딩 규칙 (configtree — 경로 주입 금지, 내용 주입)

**서비스 계정 키 등 자격증명 "파일"은 경로를 환경변수로 주입하지 않고, Spring Boot `configtree:`로 파일 내용을 프로퍼티로 흡수합니다.** 과거 `FIREBASE_SERVICE_ACCOUNT_PATH=file:backend/json/...`(리포 루트 기준 상대경로)를 `ResourceLoader.getResource()`로 읽었는데, `file:` 접두어의 상대경로는 `UrlResource`가 **JVM 작업 디렉터리(CWD) 기준**으로 해석하므로 실행 방식(`gradlew -p backend` vs `cd backend && gradlew`, `java -jar`의 실행 위치, systemd `WorkingDirectory`)마다 성패가 갈렸습니다 — 실제로 `-p backend`로 기동한 admin-api/web-api가 `backend/backend/json/...`을 찾다 부팅 실패한 장애 선례가 있습니다. `java -jar`의 CWD는 표준화되어 있지 않으므로(공식 문서상 실행 방식마다 다름), **CWD에 의존하는 경로 설계 자체를 금지**합니다.

- **메커니즘**: `external-api`의 `config/application-file.yml`이 `spring.config.import: optional:configtree:${SECRETS_DIR:/etc/tastyhouse/secrets}/`를 선언합니다. 디렉터리 트리의 각 파일이 "상대경로 = 프로퍼티 키, 파일 내용 = 값"으로 Environment에 흡수됩니다(Docker/K8s secret 마운트와 동일한 공식 패턴).
- **파일 배치 규약**: `{SECRETS_DIR}/firebase/service-account` (확장자 없음) → `firebase.service-account` 프로퍼티에 JSON 원문 전체가 담깁니다. 새 시크릿 파일도 같은 방식으로 `{도메인}/{용도}` 경로에 둡니다.
- **로컬 개발**: 시크릿은 `backend/secrets/`(gitignore 대상)에 두고, `backend/.env`의 `SECRETS_DIR`에 그 **절대경로**를 지정합니다. 운영은 배포 환경변수 `SECRETS_DIR`(기본값 `/etc/tastyhouse/secrets`)로 주입합니다.
- **소비 코드는 경로가 아니라 내용을 받습니다**: `FirebaseStorageProperties.serviceAccountJson`이 JSON 문자열을 바인딩받고, `FirebaseStorageConfig`가 `ByteArrayInputStream`으로 SDK에 전달합니다. `ResourceLoader`/`FileInputStream`으로 자격증명 파일을 직접 여는 코드를 새로 만들지 않습니다.
- **`optional:` + 명확한 실패**: configtree import는 `optional:`이라 디렉터리가 없어도 부팅이 진행되며, 값 부재는 소비 지점(`FirebaseStorageConfig`)이 SECRETS_DIR 안내를 담은 `IllegalStateException`으로 실패시킵니다 — placeholder 해석 오류 같은 불친절한 실패를 남기지 않습니다.
- **`classpath:` 동봉 금지**: 자격증명을 리소스로 옮기면 jar·이미지에 키가 박히므로 쓰지 않습니다.

reference 구현: `external-api`의 `config/application-file.yml`(configtree import), `file/firebase/FirebaseStorageProperties`(`serviceAccountJson`)·`FirebaseStorageConfig`(내용 기반 초기화 + 부재 시 명확한 실패), `backend/.gitignore`의 `secrets/`.

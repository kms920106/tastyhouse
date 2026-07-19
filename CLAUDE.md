## AI 규칙

명령어에 대한 답변은 한국어로 하도록 합니다.

명령된 로직을 구현 후, gradle build 테스트는 진행하지 않도록 합니다.

나에게 무언가를 되물어 확인해야 할 때는, 자유 서술형으로 답을 요구하지 말고 **항상 선택지를 체크리스트(선택 가능한 항목 목록) 형태로 제시**하여 내가 키보드로 타이핑하지 않고 마우스 클릭만으로 답할 수 있게 합니다. 즉, 질문이 발생하면 가능한 답안들을 명확한 항목으로 나열해 그중에서 고르도록 하고, 여러 개를 동시에 고를 수 있는 질문이면 다중 선택이 가능함을 함께 알려줍니다.

작업 플랜을 작성할 때는, 이번 작업으로 인해 새로운 컨벤션이 생기거나 기존 규칙이 바뀌는지 확인하여 루트 및 각 모듈의 `CLAUDE.md`/`AGENTS.md` 문서도 갱신이 필요한지 함께 검토합니다.

## GIT 규칙

NO_COMMIT_OR_ROLLBACK

명령된 작업(리팩터링/기능 구현 등)이 끝나면, 커밋은 직접 실행하지 않되(NO_COMMIT_OR_ROLLBACK) **추천 커밋 메시지를 항상 함께 제시**합니다. 최근 커밋 로그(`git log`)의 컨벤션(`{type}({scope}): {한글 요약}` 형태, 예: `refactor(naming): ...`, `feat(point): ...`, `style(response): ...`)을 따르고, 본문은 무엇을 왜 바꿨는지(동작 변경 여부 포함) 한국어로 서술합니다. 신설 규칙이 있어 CLAUDE.md/AGENTS.md를 갱신한 경우 그 사실도 본문에 언급합니다.

## 네이밍 규칙

파일명, 변수명, 함수명 등 모든 네이밍은 최적의 이름을 선택하도록 합니다. 명확하고 의미 있는 이름을 사용하여 코드의 가독성과 유지보수성을 높입니다.

## admin 전용 네이밍 규칙 (메서드·타입명에 admin-flavor `Admin` 접두·접미·중간어 금지)

**admin 전용 조회·명령 메서드도 `ForAdmin`/`Admin` 접미어·접두어 없이 순수 도메인 동작명만 씁니다.** 같은 도메인 안에서 admin 전용 메서드만 이런 접미·접두를 붙이면, 일반 메서드(`findOrders`, `findAllEvents`, `findAllNotices` 등)와 이름 짓는 방식이 갈려 일관성이 깨집니다. **이 원칙은 메서드명뿐 아니라 반환 DTO/Result/Condition 등 타입명에도 동일하게 적용합니다** — admin 전용 타입이라고 해서 타입명에 역할 마커 `Admin`을 붙이지 않습니다.

- **admin/일반 구분 방법**: 메서드명·타입명의 `Admin` 마커가 아니라 아래 기준으로 구분합니다.
  - **메서드**: 시그니처(파라미터 차이: `memberId` 유무·소유권 검증 유무 등)로 구분하고, **비-admin 형제와 이름이 충돌할 때만** `ById`처럼 의미 있는 한정어를 붙입니다(예: `findOrderDetail(memberId, orderId)` vs `findOrderDetailById(orderId)`).
  - **타입(Result/Condition)**: 비-admin 형제가 없으면 `Admin`을 뗀 순수명을 쓰고(예: `MemberListItemResult`, `BugReportListItemResult`, `FaqListItemResult`), **비-admin 형제와 타입명이 충돌할 때만** 관리 화면 용도를 나타내는 한정어 `Management`를 붙여 구별합니다(`{도메인}Management{용도}Result`, 예: `OrderManagementListItemResult` vs `OrderListItemResult`). `Management`는 "누가(관리자)"가 아니라 "무엇을 위한 것인가(관리 화면 목록/상세)"를 표현하므로 역할 기반 마커를 쓰지 않으면서도 admin 성격을 이름에 담을 수 있습니다.
- 이미 존재하던 `ForAdmin` 접미어(`findOrderDetailForAdmin`, `findCategoriesForAdmin`, `findAllForAdmin`, `findPageForAdmin`)는 각각 `findOrderDetailById`, `findAllCategories`, `findAllCategories`, `findFaqPage`로 리네이밍하여 정리했습니다. 이후 타입명에 남아 있던 `XxxAdminDto`/`XxxAdminListItemResult`류(`OrderAdminListItemResult`, `EventAdminListItemDto`, `EventAdminDetailDto`, `BannerAdminListItemDto`, `BannerAdminSearchCondition`, `MemberAdminListItemResult`, `BugReportAdminListItemDto`, `BugReportAdminSearchCondition`, `CouponAdminListItemDto`, `MemberCouponAdminItemDto`, `FaqCategoryAdminDto`)도 위 기준으로 전부 리네이밍했습니다. (이후 [결과 DTO 접미어 규칙](#결과-dto-접미어-규칙-result로-통일-dto-금지)에 따라 `Dto` 접미어였던 타입들도 전부 `Result`로 추가 리네이밍되었습니다.)
- **예외**: `admin` 도메인 자체의 타입(`Admin` 엔티티, `AdminId`, `AdminRepository`, `AdminCreateCommand`, `AdminCommandService`/`AdminQueryService`, `AdminJpaRepository` 등)은 이 규칙의 대상이 아닙니다. 여기서 `Admin`은 역할 마커가 아니라 "관리자 계정"이라는 애그리거트 본래 이름입니다.

reference 구현: `order` 도메인의 `OrderQueryService#findOrderDetailById`(비-admin `findOrderDetail`과 시그니처로 구분)·`OrderManagementListItemResult`(비-admin `OrderListItemResult`와 충돌 → `Management` 한정어로 구별), `faq` 도메인의 `FaqQueryService#findAllCategories`/`FaqRepository#findFaqPage`(반환 타입 `FaqCategoryManagementResult`/`FaqListItemResult` — admin 전용 `FaqCategoryManagementResult`는 web-api용 `FaqCategoryResult`와 충돌해 `Management` 한정어 적용), `event` 도메인의 `findAllEvents`/`EventManagementListItemResult`/`EventManagementDetailResult`(비-admin `EventListItemResult`/`EventDetailResult`와 충돌 → `Management` 한정어), `member`/`bug`/`coupon` 도메인의 `MemberListItemResult`/`BugReportListItemResult`/`CouponListItemResult`(형제 없어 순수 strip).

## 결과 DTO 접미어 규칙 (`Result`로 통일, `Dto` 금지)

**core-module application 계층의 조회 결과 record는 접미어를 `Result`로 통일합니다. `Dto` 접미어는 사용하지 않습니다.** 과거 `*Result`(다수)와 `*Dto`(소수)가 도메인마다, 심지어 **같은 도메인 안에서도**(예: 과거 `bug`의 `BugReportResult` vs `BugReportListItemDto`, `faq`의 `FaqResult` vs `FaqDetailDto`) 혼재해 같은 목적(조회 결과 반환)의 타입이 파일마다 다른 방식으로 이름 지어져 검색·리뷰·패턴 일치가 어려웠습니다. 이미 확립된 다수파이자 "CQRS Query 결과"라는 의미가 더 분명한 `Result`로 전 도메인을 통일합니다.

- **적용 대상**: core-module `application/dto/` 이하의 조회 결과 record(엔티티/도메인을 application 계층에서 조합·투영한 반환 타입). `@QueryProjection`으로 QueryDSL이 직접 투영하는 record와, 서비스에서 `from(...)` 정적 팩토리로 조합하는 record 모두 대상입니다.
- **적용 제외**: `*Command`(입력 명령), `*Condition`(검색 조건), web-api/admin-api의 `*Request`/`*Response`(HTTP 경계 DTO)는 이 규칙 대상이 아니며 기존 네이밍 규칙을 그대로 따릅니다.
- **폴더 위치**: 결과 record는 [record 파일 분리 규칙](#record-파일-분리-규칙-중첩-record-선언-지양)에 따라 도메인의 `application/dto/result/`에 둡니다. 과거 `application/dto/` 바로 아래 있던 결과 DTO(`banner`/`bug`/`coupon`/`event`/`faq`/`notice`/`search` 등)도 전부 `result/` 하위로 이동했습니다.
- **admin 충돌 시 처리**: admin 전용 결과 record가 비-admin 형제와 이름이 충돌하면 위 [admin 전용 네이밍 규칙](#admin-전용-네이밍-규칙-메서드타입명에-admin-flavor-admin-접두접미중간어-금지)과 동일하게 `Management` 한정어로 구별합니다(예: `faq` 도메인의 admin 전용 `FaqCategoryManagementResult` vs web-api용 `FaqCategoryResult` — 단순 `Dto`→`Result` 치환 시 이름이 충돌해 `Management`를 적용한 사례).

reference 구현: `shop` 도메인(`BestShopItemResult`, `ShopBookmarkedItemResult` 등 — 과거 `*Dto`에서 전환), `event` 도메인(`EventManagementListItemResult`), `faq` 도메인(`FaqCategoryResult`/`FaqCategoryManagementResult`/`FaqListItemResult`/`FaqDetailResult` — 폴더 이동과 admin 충돌 해결이 함께 발생한 사례).

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
- **모듈별로 각각 둠**: `ApiResponse<T>`/`PageRequest`가 이미 `adminapi.common`·`webapi.common`에 모듈별로 중복 배치된 선례를 그대로 따라, `PaginationResponse<T>`도 두 모듈에 각각 둡니다(모듈 간 공유 모듈 없음).
- **적용 제외 (변종)**: `response`(중첩 객체) + `totalElements`만 갖는 형태(예: `ProductReviewsByRatingPageResponse`, `ShopReviewsByRatingPageResponse`)처럼 표준 4필드가 아닌 페이징 응답은 이 규칙 대상이 아니며 기존 도메인 접두어 규칙을 그대로 따릅니다.

reference 구현: `admin-api`/`web-api` 공통 — `common/PaginationResponse.java` + 이를 사용하는 전 도메인의 페이징 조회 메서드(`NoticeService#getNotices`, `EventService#getEvents`, `OrderService#getOrderList` 등).

## DTO 조립 규칙 (`new` 직접 호출 지양)

컨트롤러·Facade·서비스 등 **호출부에서 DTO(command / condition / response record)를 `new`로 직접 조립하지 않습니다.** 대신 변환 책임을 해당 타입 또는 소스 타입으로 위임합니다. 이는 필드 추가 시 호출부 연쇄 수정을 막고, 조립 로직을 한 곳에 모아 가독성과 응집도를 높입니다.

- **원시 파라미터 → command/VO/condition 변환**: command·condition 등 대상 record 자신에 정적 팩토리 `of(...)`를 두고 `Xxx.of(a, b, c)`로 생성합니다. Request DTO는 command 생성 책임을 지지 않는 순수 데이터 홀더(검증 + Swagger 스키마)로 유지하고, `toCommand()` 같은 변환 메서드를 두지 않습니다.
- **호출 경로**: 컨트롤러가 Request를 개별 원시 필드로 언패킹(`request.title()` 등)해 Facade/서비스에 전달하고, Facade/서비스는 그 원시 파라미터로 `Command.of(...)`를 호출합니다. Facade가 원시 파라미터만 받으므로 admin-api Request 타입에 의존하지 않습니다.
- **도메인/DTO → 응답 변환 (result 객체가 아니라 개별 원시타입으로 수신)**: 응답 record의 정적 팩토리 `from(...)`은 core-module의 result 객체(`XxxResult`)를 통째로 받지 않고, **result의 각 필드를 원시타입(String/Long/Integer/LocalDateTime 등)으로 낱개 언패킹해서 받습니다.** `XxxResponse.from(id, title, content, ...)` 형태이며 `XxxResponse` 파일 자체는 `com.tastyhouse.core.*`를 import하지 않습니다(컨트롤러·Request record와 동일하게 Response record도 core-free). result를 낱개로 풀어 넘기는 책임은 이 Response를 호출하는 Facade/Service가 지며, Facade/Service에 private 매퍼 메서드(`toXxxResponse(XxxResult dto)`)를 두어 `dto.id()`, `dto.title()`처럼 이름 기반으로 안전하게 꺼낸 뒤 `XxxResponse.from(...)`에 위치 기반으로 전달합니다. 중첩 조립(리스트 필드, 하위 Response 필드)이 있으면 그 조립도 Facade/Service의 private 매퍼로 나눕니다.
  - **예외 — `PageResult<T>` 변환은 그대로 `from(pageResult)`**: `PaginationResponse.from(PageResult<T> pageResult)`처럼 `PageResult<T>`(core-module의 공용 페이징 타입)를 받아 `content()`/`page()`/`size()`/`totalElements()`를 그대로 위임하는 경우는 이 규칙의 대상이 아닙니다. `PageResult<T>` 자체는 도메인 result가 아니라 공용 페이징 계약이므로 원시타입 언패킹 대상이 아니며, 이 경우 페이징 응답(`PaginationResponse<T>`)이 `PageResult<T>`를 import하는 것은 허용합니다.
  - **왜 result 객체 통째 수신이 아닌가**: result 객체를 그대로 받으면 Response record가 core result의 필드 구조를 알아야 해 core에 결합되고, 필드 접근이 이름 기반(`result.title()`)이라 안전하지만 그 안전함을 Facade로 옮겨도 잃지 않습니다 — 대신 Response는 Request와 대칭적으로 순수 데이터 홀더가 되고 core 의존은 Facade 한 곳에 집중됩니다. 다만 같은 타입(String/Long 등) 파라미터가 여러 개면 위치 기반 전달이라 순서를 착각하면 컴파일은 되지만 값이 뒤바뀌는 조용한 버그가 날 수 있으므로, 신규 작성 시 record 필드 선언 순서·`from` 파라미터 순서·호출부에서 넘기는 인자 순서를 반드시 하나씩 대조합니다.
- `new`는 이러한 팩토리 메서드 **내부**에만 남깁니다(각 record가 자기 자신을 생성). 호출부에는 `new`가 남지 않는 것을 목표로 합니다.

reference 구현: `admin-api`의 `notice` 도메인 — `CreateNoticeCommand.of(...)`, `NoticeUpdateCommand.of(...)`, `NoticeSearchCondition.of(...)`, `NoticeListItemResponse.from(Long id, String title, String content, boolean visible, LocalDateTime createdAt)`(core-free) + `NoticeService#toNoticeListItemResponse(NoticeListItemResult dto)`(private 매퍼가 언패킹), `PaginationResponse.from(pageResult)`(PageResult 위임은 예외 그대로), `NoticeService#createNotice(String, String, boolean)`. 중첩 조립 사례: `admin-api`의 `order` 도메인 — `OrderService#toOrderDetailResponse`가 `OrderProductResponse`/`PaymentSummaryResponse` 중첩 리스트·필드를 각각의 private 매퍼(`toOrderProductResponse`, `toPaymentSummaryResponse`)로 분리 조립.

## command/DTO 지역 변수 추출 규칙 (호출 인자로 인라인 조립 지양)

위 [DTO 조립 규칙](#dto-조립-규칙-new-직접-호출-지양)으로 `Xxx.of(...)` 팩토리를 쓰더라도, **그 팩토리 호출 결과를 다른 메서드(주로 core application command 서비스)의 인자 자리에 인라인으로 바로 넘기지 않고, 먼저 지역 변수(`command`)로 추출한 뒤 그 변수를 전달합니다.** `of(...)`로 조립하는 것과 조립 결과를 어떻게 넘기는지는 별개이며, 이 규칙은 후자를 다룹니다.

- **왜 지역 변수로 추출하는가**: (1) 조립(무엇을 만드는가)과 호출(어디에 넘기는가)이 한 줄에 겹쳐 있으면, 인자가 많은 command일수록 한 줄이 길어지고 어떤 값이 어느 필드로 가는지 읽기 어렵습니다. 이름 붙은 지역 변수로 분리하면 "이 줄은 command를 만든다 / 이 줄은 그것을 서비스에 넘긴다"가 문장 단위로 드러납니다. (2) 디버깅 시 조립된 command에 중단점·로그를 걸거나 값을 들여다보기 쉽습니다. (3) 호출부 형태가 도메인 간 동일해져(항상 `Xxx command = Xxx.of(...); service.method(id, command);`) 리뷰·검색·패턴 일치가 단순해집니다.
- **적용 대상**: command 서비스 호출에 넘기는 **command DTO**와, 그 **command 서비스 호출의 인자로 넘기는 `XxxId.of(id)` 식별자 승격**이 대상입니다. command 변수명은 `command`로 통일하고(한 메서드에 command가 하나인 것이 일반적), 식별자 VO는 `XxxId xxxId = XxxId.of(id);`로 추출합니다.
- **식별자(`XxxId.of(id)`)도 지역 변수로 추출합니다**: command 서비스에 넘기는 식별자 승격은, command와 함께 넘기는 경우(`update`/`delete`)든 식별자만 단독으로 넘기는 경우(`deleteNotice`·`activatePolicy`·`cancel` 등)든 모두 먼저 지역 변수로 추출한 뒤 전달합니다(`XxxId xxxId = XxxId.of(id); service.method(xxxId, command);`). "짧으니 인자 자리에 인라인으로 둔다"는 이전 예외를 폐지하고, command 서비스 호출부 형태를 `XxxId xxxId = XxxId.of(id); XxxCommand command = XxxCommand.of(...); service.method(xxxId, command);`로 전 도메인 통일합니다 — 호출부에 `.of(` 호출이 인자 자리에 남지 않는 것을 목표로 합니다. (메서드 파라미터명과 VO 변수명이 겹치면 VO 쪽을 `id`/`targetXxxId` 등으로 구분합니다.)
- **적용 예외 (인라인 유지)**: (1) 이미 지역 변수로 조립되고 있던 **`SearchCondition`은 그대로 둡니다**(조회 메서드에서 관례적으로 `XxxSearchCondition condition = ...of(...)`로 이미 분리 — reference: `NoticeService#getNotices`의 `condition`). (2) **query(조회) 서비스 호출**에 넘기는 `XxxId.of(id)`는 인라인으로 둡니다(`queryService.findDetailById(XxxId.of(id))` — reference: `NoticeService#getNotice`). 이 규칙은 command 서비스 호출부에만 적용합니다. (3) **command 팩토리 내부로 들어가는 식별자**(`XxxCommand.of(XxxId.of(id), ...)`처럼 `Command.of(...)`의 인자로 중첩된 `Id.of`)는 command 조립의 일부이므로 그대로 둡니다 — 이때는 command 자체를 지역 변수로 추출하는 것이 규칙이며 내부 `Id.of`는 건드리지 않습니다(reference: `ReviewService`의 `ReviewDeleteCommand.of(ReviewId.of(reviewId), ...)`). (4) 응답 변환 `XxxResponse.from(...)`도 대상이 아닙니다(반환식에 바로 쓰는 것이 자연스러움).
- **죽은 코드 금지**: 추출로 대체된 기존 인라인 한 줄을 `//` 주석으로 남기지 않습니다. 추출한 형태만 남깁니다.

```java
// 지양 — 팩토리 결과를 호출 인자 자리에 인라인 조립
BannerId bannerId = bannerCommandService.createBanner(
    BannerCreateCommand.of(BannerType.from(type), title, imageFileId, linkUrl, startDate, endDate, sort, visible)
);

// 권장 — command 지역 변수로 추출 후 전달
BannerCreateCommand command = BannerCreateCommand.of(
    BannerType.from(type), title, imageFileId, linkUrl, startDate, endDate, sort, visible
);
BannerId bannerId = bannerCommandService.createBanner(command);
```

update 계열처럼 식별자와 command를 함께 넘길 때는 **식별자 VO와 command를 각각 지역 변수로 추출**한 뒤 전달합니다(`XxxId.of(id)`를 인자 자리에 인라인하지 않습니다):

```java
// 지양 — 식별자 승격을 인자 자리에 인라인
BannerUpdateCommand command = BannerUpdateCommand.of(BannerType.from(type), title, imageFileId, linkUrl, startDate, endDate, sort, visible);
bannerCommandService.updateBanner(BannerId.of(id), command);

// 권장 — 식별자 VO도 지역 변수로 추출
BannerId bannerId = BannerId.of(id);
BannerUpdateCommand command = BannerUpdateCommand.of(BannerType.from(type), title, imageFileId, linkUrl, startDate, endDate, sort, visible);
bannerCommandService.updateBanner(bannerId, command);
```

식별자만 단독으로 넘기는 `delete`·상태전이 계열도 동일하게 추출합니다:

```java
// 지양
bannerCommandService.deleteBanner(BannerId.of(id));

// 권장
BannerId bannerId = BannerId.of(id);
bannerCommandService.deleteBanner(bannerId);
```

reference 구현: `admin-api`의 `notice` 도메인 — `NoticeService#updateNotice`·`#deleteNotice`(`NoticeId noticeId = NoticeId.of(id); ... noticeCommandService.updateNotice(noticeId, command);` / `deleteNotice(noticeId)`)로 command·식별자 모두 지역 변수 추출을 확정. 식별자 추출 동일 적용: `banner`(`updateBanner`·`deleteBanner`), `coupon`(`updateCoupon`·`deleteCoupon`·`issueCoupon`), `policy`(`updatePolicy`·`activateCurrentPolicy`), `web-api`의 `reservation`(`cancel`·`confirm`·`reject`·`complete`), `scheduler`(`ProductScheduler#markBbqOptionsSynced`). command 추출 동일 적용: `admin`(`AdminAccountService#create`), `bug`(`changeStatus`·`classify`·`assign`), `createBanner`·`createCoupon`·`createPolicy`.

## record 파일 분리 규칙 (중첩 record 선언 지양)

**`record`는 서비스·컨트롤러·Facade 등 다른 클래스 본문 안에 중첩 선언하지 않고, 각 도메인의 관례 위치에 독립된 `.java` 파일로 둡니다.** 클래스 안에 DTO record를 함께 두면 파일 응집도가 떨어지고, 다른 클래스에서 참조할 때 `Outer.Inner` 접두사가 붙어 결합도가 커지며, DTO 조립 규칙(정적 팩토리 위임)을 적용하기도 불리합니다.

- **분리 위치**: 각 도메인의 기존 관례를 따릅니다. web-api/admin-api의 응답성 record는 해당 패키지의 `response/` 하위에, core-module의 결과 DTO는 `application/dto/result/`(command는 `application/dto/command/`)에 둡니다.
- **접근제어자**: 별도 파일로 빼면 최상위 타입이 되므로 `public record`로 선언합니다. 한 클래스 내부에서만 쓰이던 `private`/`package-private` 헬퍼 record도 분리 시 `public`으로 격상합니다.
- **적용 대상**: 응답/결과 DTO뿐 아니라 서비스 내부 전용 헬퍼 record(예: 조회 중간 계산용)도 동일하게 분리합니다.
- 이미 자기 파일 하나에 정의된 최상위 record(도메인 이벤트, ID VO 등)는 그대로 두며, 이 규칙은 "다른 클래스 본문 안에 중첩된 record"를 제거하는 것을 목표로 합니다.

reference 구현: `web-api`의 `NoticeListItemResponse`(`notice/response/`)와 `core-module`의 `OptionInfo`(`product/application/dto/result/`, `private` → `public` 격상). (과거 도메인별 페이징 래퍼 `NoticePageResponse`/`PolicyPageResponse`/`OrderPageResponse`는 공용 `common/PaginationResponse.java` 하나로 통합되어 삭제되었습니다 — [페이징 응답 공용 제네릭 래퍼 규칙](#페이징-응답-공용-제네릭-래퍼-규칙-paginationresponset) 참고.)

## ID VO(식별자 값 객체) 경계 규칙

도메인 식별자는 계층에 따라 `Long`과 `XxxId`(record VO)를 구분해서 사용합니다. 같은 코드베이스에서 도메인마다 이 규칙이 갈리면(예: `getMemberId()`가 도메인에 따라 `MemberId`를 반환하기도, `Long` FK를 반환하기도 하는 것처럼) 같은 메서드 이름이 다른 타입을 의미하게 되어 혼동과 버그를 유발합니다. 아래 표의 경계선을 기준으로 전 도메인에 동일하게 적용합니다.

| 계층 | ID 타입 | 비고 |
|---|---|---|
| HTTP 경계 (컨트롤러 `@PathVariable`/요청·응답 필드) | `Long` | `XxxId`는 web-api/admin-api 밖으로 노출하지 않습니다 |
| web-api/admin-api Service | 입력은 `Long`, 여기서 `XxxId`로 승격 | `XxxId.of(long)` 정적 팩토리로 승격(`new` 직접 호출 금지) |
| core-module application 서비스 public 시그니처 | `XxxId` | Command/Condition DTO 필드도 동일 |
| Repository 인터페이스 (`findById` 등) | `XxxId` | |
| 도메인 모델 내부 / 도메인 이벤트 | `XxxId` | |
| 엔티티 `@Id` 필드 | `Long` + `@GeneratedValue(IDENTITY)` | 유지 — VO로 바꾸지 않습니다 |
| 엔티티 자기 ID getter | `getXxxId(): XxxId` | 내부 `Long id`를 VO로 래핑해 노출 |
| 엔티티 FK 필드(다른 애그리거트 참조) | 가능하면 `@Convert`로 `XxxId` | 일괄 강제 아님, 점진적으로 전환 |
| 결과 DTO(result record)에서 id 추출 | `entity.getXxxId()` | `getId()`(Long)를 응답에 직접 노출하지 않습니다 |

**`XxxId` VO 표준 형태** (`core-module/.../<domain>/domain/vo/XxxId.java`):

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
- FK를 VO로 매핑할 때는 `AttributeConverter<XxxId, Long>` 컨버터를 두고 엔티티 필드에 `@Convert`를 적용합니다.

reference 구현: `policy` 도메인 — `PolicyDocumentId`, `PolicyCommandService`(파라미터/반환이 `PolicyDocumentId`), `PolicyDocumentRepository.findById(PolicyDocumentId)`, `PolicyService`(admin-api, `Long↔VO` 승격 담당). FK `@Convert` 레퍼런스: `payment` 도메인의 `Payment.orderId : OrderId`.

## 도메인 enum 경계 규칙

도메인 enum(`core.domain.<도메인>.domain.model`의 `BannerType`·`EventStatus`·`FoodType` 등)은 **ID VO와 동일한 경계 원칙**을 따릅니다. HTTP 경계(컨트롤러 `@RequestParam`/Request 필드)는 `String`(다중값은 `List<String>`)으로 받고, web-api/admin-api Service(Facade)에서 core enum으로 **승격**합니다. 이는 ID를 `Long`으로 받아 `XxxId.of()`로 승격하는 것과 대칭이며, "컨트롤러·Request record는 `com.tastyhouse.core.*`를 import하지 않는다"는 상위 규칙(각 모듈 `AGENTS.md`)의 enum 케이스 구체화입니다. 컨트롤러가 도메인 enum을 직접 노출하면 API 계약이 도메인 모델에 결합되어, enum 상수 추가가 곧 공개 스키마 변경이 되고 어댑터가 도메인을 알게 되는 레이어 위반이 발생합니다.

| 계층 | enum 타입 | 비고 |
|---|---|---|
| HTTP 경계 (컨트롤러 `@RequestParam`/요청 필드) | `String` / `List<String>` | 도메인 enum을 web-api/admin-api 밖(HTTP)으로 노출하지 않습니다 |
| web-api/admin-api Service(Facade) | 입력은 `String`, 여기서 core enum으로 승격 | `Enum.from(String)` 정적 팩토리로 승격(`valueOf` 산재·`new` 금지) |
| core-module application 서비스 public 시그니처 | core enum | Command/Condition DTO 필드도 동일. String이 core로 내려가지 않습니다 |
| 도메인 모델 내부 / 도메인 이벤트 | core enum | |

- **변환 팩토리 위치**: core enum 자신에 `static Xxx from(String code)`를 두고, 실패 시 프로젝트 공통 `BusinessException(ErrorCode.XXX_TYPE_UNKNOWN)`(400)으로 변환합니다. 생짜 `IllegalArgumentException`(`No enum constant …`)을 노출하지 않습니다. 이는 DTO 조립 규칙("변환 책임을 대상 타입에 위임")과 일관됩니다.

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

도메인 enum을 엔티티 필드로 영속화할 때는 **반드시 `@Enumerated(EnumType.STRING)`로 매핑하고, `@Column`에 `length`와 함께 `columnDefinition = "VARCHAR(n)"`를 명시**합니다. **`@Enumerated(EnumType.ORDINAL)`은 전 도메인 금지**합니다. DB 컬럼(`create.sql`/`alter.sql`)도 네이티브 MySQL `ENUM(...)`이 아닌 `VARCHAR(n)`로 정의합니다.

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
- **DDL 표준 형태** (`create.sql`/`alter.sql`): 네이티브 `ENUM(...)`이 아니라 `VARCHAR(n)` + **주석으로 허용값 나열**. 엔티티의 `columnDefinition` 숫자와 일치시킵니다.

```sql
-- 올바름: VARCHAR + 허용값 주석
category VARCHAR(20)  COMMENT '분류 (PAYMENT, LOGIN, ORDER, RESERVATION, UI, PERFORMANCE, ETC / 미분류 시 NULL)',

-- 금지: 네이티브 ENUM 타입
category ENUM('payment','login','order','reservation','ui','performance','etc'),
```

- **정합성 책임**: 엔티티 enum 필드를 추가/변경할 때 `@Column`에 `columnDefinition = "VARCHAR(n)"`가 있는지 반드시 확인하고, `create.sql`/`alter.sql`의 컬럼 타입·`n`·nullable을 엔티티와 일치시킵니다.
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
- **순수 데이터 홀더 유지 (DTO 조립 규칙과 일관)**: `{도메인}SearchRequest`는 검증 + Swagger 스키마만 갖는 순수 record로 두고, `toCondition()`/`toCommand()` 같은 변환 메서드를 두지 않습니다. 컨트롤러가 `request.title()` 등 **개별 원시 필드로 언패킹**해 Facade/Service에 전달하고, Facade/Service가 `{도메인}SearchCondition.of(...)`로 조립합니다. → Facade/Service 시그니처(개별 파라미터 수신)는 **변경하지 않습니다**.
- **기본값·필수 표현**: `@RequestParam(defaultValue = ...)`로 표현하던 기본값은 record의 **compact constructor에서 정규화**하거나(`type == null ? "ALL" : type`) 필드 초기화로 대체합니다. 필수 파라미터는 필드에 Bean Validation(`@NotBlank`/`@NotNull` 등)을 부착하고 `@Valid`로 강제합니다.
- **경계 타입 (ID VO·enum 경계 규칙과 일관)**: record 필드에서도 enum 후보는 `String`/`List<String>`, FK/식별자는 `Long`으로 받습니다. `com.tastyhouse.core.*`(core enum·VO)를 Request가 import하지 않습니다. 승격은 기존대로 Facade/Service에서 `Enum.from(String)`·`XxxId.of(Long)`으로 수행합니다.
- **파일 분리·명명 (record 파일 분리·명명 순서 규칙과 일관)**: 컨트롤러 본문 중첩이 아니라 도메인 폴더 `request/`에 `public record {도메인}SearchRequest`로 둡니다. 이름은 `{도메인}` 접두 순서(예: `ShopSearchRequest`, `NoticeSearchRequest`, `RankSearchRequest`)로 기존 `{도메인}CreateRequest`/`{도메인}UpdateRequest`와 이름순 인접시킵니다.
- **Swagger**: 기존에 `@RequestParam`에 붙던 `@Parameter(schema = @Schema(allowableValues = {...}))`는 record 필드의 `@Schema(allowableValues = {...})`로 이전합니다. `required = false` 옵션 동작(미바인딩 시 null/빈 리스트)은 그대로 보존합니다.
- **적용 대상**: 신규 조회 API는 이 규칙을 따르고, 기존 `@RequestParam` GET(다중: `shop/getLatestShops`·admin `notice/getNotices`, 단일: `search/searchMenus`의 `query`, `rank/getMemberRankList`의 `type`/`limit` 등)도 수정 시 전환합니다.

reference 구현(전환 예정): `web-api`의 `ShopSearchRequest`(`shop/request/`, `getLatestShops`), `admin-api`의 `NoticeSearchRequest`(`notice/request/`, `getNotices`). 이미 정착된 `@ModelAttribute` record 선례: 공통 `PageRequest`(`web-api`/`admin-api`의 `common/PageRequest`).

**참고 자료 (Spring 공식)**: Spring Framework Reference — `@ModelAttribute` method arguments: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/modelattrib-method-args.html ("binds request parameters ... onto a model object"; 보안상 web 바인딩 전용 객체/생성자 바인딩 권장 — setter 없는 record가 이에 부합). 단일 값 `@RequestParam` 대신 record로 통일하는 것은 위 공식 최소 요건을 넘어서는 이 프로젝트의 일관성 우선 컨벤션입니다.

## 컨트롤러 `@PathVariable` 식별자 명명 규칙 (`{id}`로 통일)

**한 컨트롤러 안에서 그 컨트롤러의 주(主) 리소스를 가리키는 `@PathVariable` 식별자는 경로상의 위치·핸들러 종류와 무관하게 bare `id`로 통일합니다.** 단건 CRUD(`GET/PUT/DELETE /coupons/v1/{id}`)든 그 리소스에 속한 중첩 하위 경로(`POST /coupons/v1/{id}/issues`)든, 결국 "쿠폰"을 가리키는 경로 변수는 모두 `id` 하나로 씁니다. 같은 대상을 어떤 메서드에서는 `id`, 어떤 메서드에서는 `{도메인}Id`로 **혼재하는 것을 금지**합니다.

- **왜 `{id}`인가**: `@RequestMapping("/api/coupons")`로 컨트롤러 전체가 이미 쿠폰 리소스에 스코프되어 있으므로, 경로 변수 `id`는 문맥상 "이 컨트롤러가 다루는 쿠폰의 id"임이 자명합니다. `couponId`처럼 도메인 접두어를 반복하면 클래스 스코프와 중복되어 장황하고, 오히려 한 파일 안에서 `id`/`couponId`가 섞이는 혼재를 부릅니다. 짧고 일관된 `id`가 REST 관례(리소스 컬렉션 `/coupons/{id}`)에도 부합합니다.
- **경계 타입은 그대로**: 타입은 [ID VO 경계 규칙](#id-vo식별자-값-객체-경계-규칙)대로 HTTP 경계에서 `Long`을 유지합니다(`@PathVariable Long id`). Facade/Service에서 `XxxId.of(id)`로 승격하는 흐름은 변경하지 않습니다.
- **다른 애그리거트를 참조하는 경우는 예외**: 어떤 컨트롤러가 **자기 주 리소스가 아닌 다른 도메인의 식별자**를 경로로 받는 경우(예: 회원 컨트롤러가 `/members/{memberId}/orders/{orderId}`처럼 두 종류 이상의 식별자를 동시에 받는 경우)에는 각 식별자를 `{도메인}Id`로 구분해 모호함을 없앱니다. 즉 "그 컨트롤러의 주 리소스 = `id`, 그 외 참조 식별자 = `{도메인}Id`"가 기준입니다.
- **적용 대상**: 신규 컨트롤러는 이 규칙을 따르고, 기존에 주 리소스를 `{도메인}Id`로 받던 컨트롤러는 **해당 파일을 수정할 때 함께 `id`로 전환**합니다. 이 규칙만을 위해 전 컨트롤러를 일괄 재작성하지는 않습니다.

reference 구현: `admin-api/coupon/CouponApiController` — 단건 CRUD·중첩 발급/현황 API 모두 주 리소스인 쿠폰을 `@PathVariable Long id`로 받음(`/v1/{id}`, `/v1/{id}/issues`).

## 컨트롤러 미사용 `@PathVariable` 경로 평탄화 규칙

**중첩 리소스 경로(`/v1/{id}/{하위리소스}/{하위id}`)의 부모 `@PathVariable`(`id`)이 핸들러·Facade·core 어디에서도 실제로 사용되지 않는다면(단순 전달조차 없이 완전히 죽은 파라미터), 그 경로를 부모 세그먼트 없이 평탄화합니다.** 하위 리소스의 식별자(`{하위id}`)가 전역 유니크 PK라 부모 없이도 단독으로 대상을 특정할 수 있는 경우가 이에 해당합니다. "경로가 리소스 계층을 반영해야 한다"는 REST 중첩 관례보다, **실제로 쓰이지 않는 경로 변수를 남겨 두지 않는 것**을 우선합니다 — 사용하지 않는 파라미터는 호출자에게 "이 값이 삭제 대상을 좁히는 데 쓰인다"는 잘못된 인상을 주고, 나중에 검증 로직이 있는 줄 착각하게 만들기 쉽습니다.

- **판별 기준**: 핸들러 메서드 본문에서 부모 `id`를 Facade/Service 호출 인자로 전달조차 하지 않는 경우(완전 미사용)에만 적용합니다. Facade나 core에서 `id`를 소속 검증(예: "이 하위 리소스가 이 부모에 속하는지")에 사용한다면 평탄화 대상이 아니며 기존 중첩 경로를 그대로 유지합니다.
- **적용 방법**: `@DeleteMapping("/v1/{id}/{하위리소스}/{하위id}")` → `@DeleteMapping("/v1/{하위리소스}/{하위id}")`로 변경하고, 미사용 `@PathVariable Long id` 파라미터를 제거합니다. Facade/core 시그니처가 이미 하위 식별자만 받고 있다면 추가 변경이 필요 없습니다.
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
3. 그 외 전부 (자사 제외 — `jakarta.*` 포함) — `com.querydsl.*`, `io.swagger.*`, `jakarta.persistence.*`, `jakarta.validation.*`, `lombok.*`, `org.springframework.*`, `software.amazon.*` 등을 **한 그룹으로 알파벳 혼합 정렬**
4. 자사 코드 (projectRootPackage) — `com.tastyhouse.*`
5. static import — 그룹 구분 없이 맨 아래 한 블록 (QueryDSL Q타입, `assertThat` 등), 내부는 알파벳 순

```java
package com.tastyhouse.core.domain.order.domain.model;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.shared.entity.BaseEntity;

import static com.tastyhouse.core.domain.order.domain.model.QOrderProduct.orderProduct;
```

위 예시처럼 `jakarta`는 별도 그룹이 아니라 `com.querydsl` → `io.swagger` → `jakarta` → `lombok` → `org.springframework` 순으로 서드파티 그룹 안에서 알파벳 정렬됩니다.

**자사 코드(`com.tastyhouse.*`)만 맨 뒤로 분리하는 이유**: DDD 레이어드 아키텍처에서 "내 도메인 코드"와 "외부 프레임워크 의존"을 한눈에 구분하기 위함입니다. 특히 `core-module`은 Spring Web 의존이 금지되어 있으므로, import 그룹 분리가 레이어 위반을 리뷰 시점에 즉시 드러내는 역할을 합니다.

### 자사 코드 그룹 내부 계층 정렬 (헥사고날 안→밖: domain이 위)

**이 규칙은 공식 표준이 아니라 프로젝트 커스텀 컨벤션입니다.** Spring `spring-javaformat`(`SpringImportOrderCheck`), Google Java Style, Checkstyle `ImportOrder` 등 어떤 공식 컨벤션도 "자사 패키지 그룹 **내부**를 계층 순으로 정렬"하지 않습니다(공식은 그룹 내부 순수 알파벳순). 이 프로젝트는 헥사고날/클린 아키텍처의 **"의존성은 항상 안쪽(domain)을 향한다"는 안정 의존성 원칙**을 근거로, 위 그룹4(`com.tastyhouse.*`) 내부의 정렬 순서를 **`(계층 순위, 알파벳)` 복합 키**로 세분합니다. 가장 안정적·핵심인 domain을 맨 위에, 가장 바깥·휘발적인 presentation을 맨 아래에 둡니다.

| 계층 순위 | 계층 | 매칭 패키지 세그먼트 |
|---|---|---|
| 1 | **domain** (가장 안쪽·핵심) | `...domain.model` / `.vo` / `.event` / `.repository` |
| 2 | **application** | `...application`, `...application.dto.command`, `...application.dto.result`, `...application.dto` |
| 3 | **infrastructure** | `...infrastructure.persistence`(`.converter`) |
| 4 | **external / shared** (어댑터·횡단 공용) | `com.tastyhouse.external.*`, `com.tastyhouse.core.shared.*`, `com.tastyhouse.core.config.*`, `com.tastyhouse.core.exception.*` |
| 5 | **presentation** (가장 바깥) | `com.tastyhouse.webapi.*`, `com.tastyhouse.adminapi.*` — 내부는 아래 "presentation 내부 서브정렬"로 5-a → 5-b 세분 |

- **같은 계층 순위 내부는 기존대로 알파벳(ASCII) 오름차순**으로 정렬합니다.
- **그룹4 내부에는 여전히 빈 줄을 넣지 않습니다** — 계층 사이도 빈 줄로 구분하지 않고 순서만 바꿉니다(상위 그룹 간 빈 줄 규칙은 그대로 유지).
- static import는 이 규칙과 무관하게 맨 아래 블록 그대로입니다.

**Before (단일 알파벳순 — 계층 혼재)**:
```java
import com.tastyhouse.core.domain.order.application.OrderCommandService;
import com.tastyhouse.core.domain.order.application.dto.command.OrderCreateCommand;
import com.tastyhouse.core.domain.order.application.dto.result.OrderResult;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.request.OrderProductRequest;
import com.tastyhouse.webapi.order.response.OrderDetailResponse;
```

**After (domain → application → infrastructure → external/shared → presentation, 계층 내부는 알파벳순)**:
```java
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.domain.order.application.OrderCommandService;
import com.tastyhouse.core.domain.order.application.dto.command.OrderCreateCommand;
import com.tastyhouse.core.domain.order.application.dto.result.OrderResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.request.OrderProductRequest;
import com.tastyhouse.webapi.order.response.OrderDetailResponse;
```

**core-module 전용 파일 예시** (presentation·external 없이 domain → application → shared만 존재):
```java
import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.repository.OrderRepository;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.QOrderListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
```

#### presentation(5순위) 내부 서브정렬 (공용 인프라 위 → 도메인 전용 아래)

presentation 계층(`com.tastyhouse.webapi.*` / `adminapi.*`)은 한 계층 안에 성격이 다른 두 종류가 섞여 있습니다. 이를 **위 계층 정렬과 같은 안정 의존성 원칙**으로 한 단계 더 세분합니다. 여러 도메인이 공유하는 **공용 인프라**(비도메인 프레임워크 유틸)는 특정 도메인 전용 어댑터 DTO보다 안정적이므로 **위(5-a)**, 도메인 전용은 **아래(5-b)** 에 둡니다. 이는 4순위에서 `core.shared.*`를 presentation보다 위에 두는 방향("공용은 도메인 전용보다 위")과 대칭입니다.

| 서브순위 | 대상 | 매칭 패키지 세그먼트 |
|---|---|---|
| **5-a** (공용 인프라, 위) | 여러 도메인이 공유하는 비도메인 프레임워크 유틸 | `com.tastyhouse.{webapi\|adminapi}.common.*` · `.config.*` · `.security.*` · `.ratelimit.*` · `.exception.*` |
| **5-b** (도메인 전용, 아래) | 특정 도메인 전용 컨트롤러 협력 타입 | `com.tastyhouse.{webapi\|adminapi}.<도메인>.request.*` · `.response.*` (및 도메인 하위 기타) |

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

## QueryDSL 동적 where 조건 조립 규칙 (`BooleanBuilder` 대신 `BooleanExpression` varargs 헬퍼)

`core-module`의 `*RepositoryImpl.java`에서 **동적 검색(필터가 null이면 조건 무시)을 하는 where 조건은 `BooleanBuilder` + `if`문이 아니라, `private BooleanExpression xxxEq(arg)` 헬퍼(arg가 null이면 null 반환) + `.where(가변인자)`로 조립합니다.** QueryDSL이 `.where(...)`에 전달된 null 인자를 자동으로 무시하는 것을 이용한 동적 쿼리 관용구입니다.

- **왜 통일하는가**: 전수 조사 결과 동적 검색 리포지토리 11개 중 9개가 이미 `BooleanExpression` varargs 헬퍼 패턴이었고, `OrderRepositoryImpl`·`ShopRepositoryImpl` 2개만 `BooleanBuilder` + `if`문(명령형·장황)을 쓰고 있었습니다. 같은 목적(동적 where)을 서로 다른 스타일로 구현하면 파일마다 읽는 방식이 달라지므로, 다수파 패턴으로 통일합니다.
- **정적 고정 조건**(필터링 대상이 아닌 조건, 예: `deleted.isFalse()`)은 헬퍼 없이 `.where(...)`에 인라인으로 둡니다.
- **`BooleanBuilder` 예외 허용 범위**: OR 조합·복잡한 그룹핑처럼 varargs `.where(...)`(AND만 지원)로 표현 불가능한 경우에만 예외적으로 쓰고, 이유를 주석으로 남깁니다.
- **선행 데이터 계산은 대상 아님**: 서브쿼리로 ID 집합을 먼저 계산해 교집합하는 등 where 조립이 아닌 로직은 이 규칙과 무관합니다. 계산된 집합을 최종 where에 넣을 때만 `xxxIn(Set<Long>)` 헬퍼를 씁니다.

```java
// 권장 — BooleanExpression 헬퍼 + varargs where
.where(
    notice.deleted.isFalse(),          // 정적 고정 조건은 인라인
    titleContains(condition.title()),  // 동적 조건은 헬퍼로
    visibleEq(condition.visible())
)
...
private BooleanExpression titleContains(String title) {
    return StringUtils.hasText(title) ? notice.title.containsIgnoreCase(title) : null;
}
```

```java
// 지양 — BooleanBuilder + if
BooleanBuilder where = new BooleanBuilder();
if (condition.title() != null) { where.and(notice.title.containsIgnoreCase(condition.title())); }
```

reference 구현: `notice` 도메인 `NoticeRepositoryImpl`(도메인/JPA 분리로 `infrastructure-module`로 이동), `order` 도메인 `OrderRepositoryImpl#findOrders`, `shop` 도메인 `ShopRepositoryImpl#findLatestShops`/`#searchByKeywordWithBookmark`(과거 `BooleanBuilder`였다가 통일). 상세 예시는 `core-module/src/main/java/com/tastyhouse/core/AGENTS.md` 참고.

## 도메인 모델 / JPA 엔티티 분리 규칙 (선별 적용, persistence는 `infrastructure-module`로)

**상태전이·불변식이 실재하는 도메인은 순수 도메인 모델(POJO)과 JPA 엔티티를 분리하고, JPA 어댑터를 별도 `infrastructure-module`로 옮긴다.** 단순 CRUD 도메인은 현행(도메인 모델 = `@Entity`, persistence가 core-module 내부) 유지가 허용되며 전환은 강제가 아니다. 이는 `md/CLEAN-ARCHITECTURE.md`의 Strangler Fig 점진 전환과 일관되며, 클래스 수준(POJO화)과 모듈 수준(어댑터 분리)을 함께 적용해 "core는 JPA 구현을 모른다"를 빌드 그래프로 강제한다.

- **모듈 경계**: `web-api`/`admin-api` → `core-module`(도메인 POJO + application + Repository 인터페이스) `implementation`, `infrastructure-module`(JPA 어댑터) `runtimeOnly`. `infrastructure-module` → `core-module` `implementation`. 미분리 도메인 20개의 persistence는 당분간 core-module에 잔류(모듈 Strangler Fig).
- **순수 도메인 모델**(core-module, 위치 불변): `jakarta`/`@Entity` 무의존. 신규 생성 `of(...)`와 DB 재구성 전용 `reconstitute(id, ..., createdAt, updatedAt)` 두 정적 팩토리만 공개하고, `reconstitute`는 인프라만 호출(불변식 우회 방지, Javadoc 명시). `id`는 미영속이면 null. getter는 lombok `@Getter` 허용(JPA 아님). **필드 가시성(mutable vs final)**: JPA `@Entity`는 프록시·리플렉션이 필드를 세팅해야 해 `final`을 못 붙이지만, 순수 도메인 모델은 그 제약이 없다. 따라서 **생성자(팩토리) 이후 재대입되지 않는 필드는 반드시 `final`로 선언**한다 — `id`뿐 아니라 `username`·`status` 같은 상태 필드도 상태전이 메서드로 실제 재대입되지 않으면 `final`로 둔다. 이는 (1) POJO화로 얻은 불변성 표현력을 실제로 살리고, (2) "이 필드는 생성 시점에 확정되고 이후 안 바뀐다"를 컴파일러가 강제하며, (3) IntelliJ의 `Field 'xxx' may be 'final'` 경고를 원천 차단한다. 상태전이가 있는 도메인이라도 전이 대상이 아닌 필드는 개별적으로 `final`을 적용한다(전이되는 필드만 non-final). reference: `admin` 도메인 `Admin`(update 경로가 없어 `id`+`username`·`password`·`name`·`role`·`status` 전 필드 `final`).
- **JPA 엔티티**(`infrastructure-module`, `com.tastyhouse.infrastructure.<도메인>.persistence.XxxJpaEntity`): DB 매핑 전용, 행위 없음. `BaseEntity`(`@MappedSuperclass`) 상속. 신규 생성 `create(...)`·update 복사용 `applyChanges(...)`만 둔다. **DDL·`ddl-auto=validate` 무변경**(테이블/컬럼 동일 매핑).
- **매퍼**(`XxxMapper`, package-private): `toDomain`(재구성)·`toEntity`(신규)·`applyChanges`(managed 엔티티 필드 복사). 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
- **저장 시맨틱 — load-copy-save (merge 금지)**: `RepositoryImpl.save`는 id null이면 insert, id 있으면 managed 엔티티를 PK로 조회 후 `applyChanges` 복사(동일 트랜잭션 1차 캐시 히트 — 추가 쿼리 없음). detached `save()`(merge)는 `@CreatedDate(updatable=false)` 감사 필드 파손·전 필드 UPDATE 문제로 금지.
- **명시적 save 규칙 (더티 체킹 상실 보완)**: 분리된 도메인의 command 서비스는 도메인 변경 후 **반드시 `repository.save(domain)`를 호출**한다(`@Entity`처럼 자동 flush되지 않음). 누락 시 변경이 조용히 유실된다.
- **Q타입 생성 위치**: `QXxxJpaEntity`는 infrastructure-module에서, `@QueryProjection` result DTO의 `QXxxResult`는 core-module에서 생성(DTO가 core `application/dto`에 잔류). 양쪽 build.gradle에 QueryDSL apt 유지. core-module의 `spring-boot-starter-data-jpa`·mysql 의존은 미분리 도메인이 남는 동안 유지하고, 전 도메인 이동 완료 시 제거하는 것이 최종 목표.
- **Spring 조립 — 스캔 설정은 소유 모듈이 선언**: `com.tastyhouse.infrastructure`는 (1) `WebApiApplication`/`AdminApiApplication`의 `scanBasePackages`에 등록하고, (2) JPA 스캔(`@EnableJpaRepositories`/`@EntityScan`)은 infrastructure-module 자신의 `InfrastructurePersistenceConfig`(패키지 루트, `basePackageClasses` 타입 세이프 방식)가 선언한다. core의 `DatabaseConfig`에 infrastructure 패키지 문자열을 넣지 않는다 — core는 infrastructure를 의존하지 않아 컴파일 타임에 그 패키지를 볼 수 없고(IDE "Cannot resolve package" 에러), 엔티티를 소유한 모듈이 스스로 스캔을 선언하는 것이 Spring Boot 공식 권장(`basePackageClasses`)과 일치한다. `@EntityScan`은 여러 설정에서 누적 병합되므로 두 선언이 공존한다. JPA Auditing 등 전역 설정은 core `DatabaseConfig` 한 곳만 유지.

reference 구현: `notice` 도메인 — 순수 모델 `core-module/.../notice/domain/model/Notice`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../notice/persistence/`(`NoticeJpaEntity`/`NoticeMapper`/`NoticeJpaRepository`/`NoticeRepositoryImpl`), 명시적 save `NoticeCommandService#updateNotice`·`#deleteNotice`, 순수 단위 테스트 `core-module/src/test/.../notice/domain/model/NoticeTest`. 상세는 `infrastructure-module/AGENTS.md`·`md/CLEAN-ARCHITECTURE.md` 참고. `admin` 도메인 — 연관관계·QueryDSL·update/soft delete가 없는 최소 CRUD(create+read) 변형 사례: 순수 모델 `core-module/.../admin/domain/model/Admin`(`create`/`reconstitute`, 감사 필드 미소비로 생략), 어댑터 `infrastructure-module/.../admin/persistence/`(`AdminJpaEntity`/`AdminMapper`/`AdminJpaRepository`/`AdminRepositoryImpl` — QueryDSL 없이 순수 pass-through, `save`는 update 경로가 없어 insert 전용), 순수 단위 테스트 `core-module/src/test/.../admin/domain/model/AdminTest`. (명시적 save 규칙은 대상 없음 — `AdminCommandService#createAdmin`이 이미 저장 시 `save` 호출.) `banner` 도메인 — enum 필드(`BannerType`, `EnumType.STRING`+`columnDefinition`)와 미분리 도메인(`file`)의 Q타입(`QUploadedFile`)을 그대로 조인하는 크로스 도메인 QueryDSL 조회가 있는 사례: 순수 모델 `core-module/.../banner/domain/model/Banner`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../banner/persistence/`(`BannerJpaEntity`/`BannerMapper`/`BannerJpaRepository`/`BannerRepositoryImpl` — `QBannerJpaEntity`는 infra 생성, `QUploadedFile`은 core-module 생성을 그대로 import), 명시적 save `BannerCommandService#updateBanner`·`#deleteBanner`, 순수 단위 테스트 `core-module/src/test/.../banner/domain/model/BannerTest`. `bug` 도메인 — JPA 연관관계는 없지만 **한 도메인에 두 애그리거트**(`BugReport`+`BugReportImage`, plain FK `bug_report_id`+QueryDSL 서브쿼리로만 연결)가 있는 사례, `@Convert` FK VO(`memberId`)와 enum 4개(`columnDefinition` VARCHAR)를 그대로 JpaEntity로 이관: 순수 모델 `core-module/.../bug/domain/model/BugReport`·`BugReportImage`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../bug/persistence/`(`BugReportJpaEntity`/`BugReportImageJpaEntity`/`BugReportMapper`/`BugReportImageMapper`/`BugReportJpaRepository`/`BugReportImageJpaRepository`/`BugReportRepositoryImpl`/`BugReportImageRepositoryImpl` — 서브쿼리 Q타입도 `QBugReportImageJpaEntity`로 치환), 더티 체킹에 의존하던 `changeStatus`/`classify`/`assign`에 명시적 save 추가(`BugReportCommandService`), 순수 단위 테스트 `core-module/src/test/.../bug/domain/model/BugReportTest`·`BugReportImageTest`. `faq` 도메인 — JPA 연관관계 없이 raw FK(`Long faqCategoryId`)로만 연결된 **두 애그리셋**(`Faq`+`FaqCategory`)이 있고, 한쪽 리포지토리 구현이 다른 쪽 JPA 엔티티 Q타입을 직접 조회하는 크로스 엔티티 QueryDSL 사례(`FaqCategoryRepositoryImpl#existsActiveItemsByCategoryId`가 `QFaqJpaEntity`를 조회): 순수 모델 `core-module/.../faq/domain/model/Faq`·`FaqCategory`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../faq/persistence/`(`FaqJpaEntity`/`FaqCategoryJpaEntity`/`FaqMapper`/`FaqCategoryMapper`/`FaqJpaRepository`/`FaqCategoryJpaRepository`/`FaqRepositoryImpl`/`FaqCategoryRepositoryImpl`), 더티 체킹에 의존하던 `updateFaq`/`deleteFaq`(`FaqCommandService`)·`updateCategory`/`deleteCategory`(`FaqCategoryCommandService`)에 명시적 save 추가, 순수 단위 테스트 `core-module/src/test/.../faq/domain/model/FaqTest`·`FaqCategoryTest`. `coupon` 도메인 — JPA 연관관계 없이 raw FK(`Long couponId`)와 `@Convert` FK VO(`memberId`)로만 연결된 **두 애그리거트**(`Coupon`+`MemberCoupon`)가 있고, `Coupon`만 조회 결과(`CouponDetailResult.from`)가 감사 시각을 직접 소비해 감사 필드를 보유하는 반면 `MemberCoupon`은 QueryDSL이 엔티티에서 직접 투영해 감사 필드가 불필요한 **비대칭 사례**, 두 엔티티를 join하는 `MemberCouponRepositoryImpl`이 Q타입 2개(`QCouponJpaEntity`+`QMemberCouponJpaEntity`)를 함께 치환: 순수 모델 `core-module/.../coupon/domain/model/Coupon`·`MemberCoupon`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../coupon/persistence/`(`CouponJpaEntity`/`MemberCouponJpaEntity`/`CouponMapper`/`MemberCouponMapper`/`CouponJpaRepository`/`MemberCouponJpaRepository`/`CouponRepositoryImpl`/`MemberCouponRepositoryImpl`), 더티 체킹에 의존하던 `updateCoupon`/`deleteCoupon`에 명시적 save 추가(`CouponCommandService`), 순수 단위 테스트 `core-module/src/test/.../coupon/domain/model/CouponTest`·`MemberCouponTest`. `event` 도메인 — JPA 연관관계 없이 raw FK(`Long eventId`)로만 연결된 **세 애그리거트**(`Event`+`EventWinner`+`EventAnnouncement`, 각각 독립 Repository/JpaRepository/RepositoryImpl)가 있고, `EventWinner`는 공유 `@Embeddable` VO `PhoneNumber`를 도메인 모델 필드로 그대로 유지하며 `Event`만 조회 결과(`EventManagementDetailResult.from`)가 감사 시각을 직접 소비해 감사 필드를 보유하는 반면 `EventWinner`/`EventAnnouncement`는 감사 필드가 불필요한 **비대칭 사례**, `EventRepositoryImpl`이 미분리 `file` 도메인의 `QUploadedFile`을 그대로 조인: 순수 모델 `core-module/.../event/domain/model/Event`·`EventWinner`·`EventAnnouncement`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../event/persistence/`(`EventJpaEntity`/`EventWinnerJpaEntity`/`EventAnnouncementJpaEntity`/`EventMapper`/`EventWinnerMapper`/`EventAnnouncementMapper`/`EventJpaRepository`/`EventWinnerJpaRepository`/`EventAnnouncementJpaRepository`/`EventRepositoryImpl`/`EventWinnerRepositoryImpl`/`EventAnnouncementRepositoryImpl`), 더티 체킹에 의존하던 `updateEvent`/`deleteEvent`/`updateAnnouncement`/`deleteWinner`에 명시적 save 추가(`EventCommandService`), 순수 단위 테스트 `core-module/src/test/.../event/domain/model/EventTest`·`EventWinnerTest`·`EventAnnouncementTest`. `member` 도메인 — **member 코어 3개 애그리거트만 전환**(`Member`/`MemberSocialAccount`/`MemberWithdrawal`; 같은 폴더의 `follow`/`referral`은 이번 범위 제외), JPA 연관관계 없이 `MemberSocialAccount`/`MemberWithdrawal`은 `@Convert` FK VO(`memberId`, `MemberIdConverter`는 `bug` 선례대로 core-module 잔류)로만 연결되고 `Member`는 공유 `@Embeddable` VO `PhoneNumber`를 도메인 모델 필드로 그대로 유지하는 사례: 순수 모델 `core-module/.../member/domain/model/Member`·`MemberSocialAccount`·`MemberWithdrawal`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../member/persistence/`(`MemberJpaEntity`/`MemberSocialAccountJpaEntity`/`MemberWithdrawalJpaEntity`/`MemberMapper`/`MemberSocialAccountMapper`/`MemberWithdrawalMapper`/`MemberJpaRepository`/`MemberSocialAccountJpaRepository`/`MemberWithdrawalJpaRepository`/`MemberRepositoryImpl`/`MemberSocialAccountRepositoryImpl`/`MemberWithdrawalRepositoryImpl`). **크로스 도메인 참조 신규 패턴**: `Member`가 POJO로 전환되며 core-module에 `QMember`가 더 이상 생성되지 않아, 아직 미분리인 `follow`/`review`/`rank` 도메인의 `FollowRepositoryImpl`/`ReviewRepositoryImpl`/`MemberReviewRankRepositoryImpl`이 깨졌다 — core는 infrastructure를 의존할 수 없어 이동한 `QMemberJpaEntity`를 import할 수 없으므로, 세 파일 모두 `com.querydsl.core.types.dsl.PathBuilder<Object>`로 JPA 엔티티명 문자열(`"MemberJpaEntity"`)을 참조해 필요한 컬럼만 `NumberPath`/`StringPath`/`EnumPath`로 노출하는 방식으로 전환(이후 유사 상황의 재사용 패턴). 명시적 save `MemberCommandService#updateProfile`·`#updatePersonalInfo`·`#updatePassword`·`#suspend`·`#activate`, 그리고 도메인 분리로 실제 회귀가 드러난 `web-api`의 `KakaoSocialLoginService`·`NaverSocialLoginService`·`FacebookSocialLoginService`·`AppleSocialLoginService`(`updateProviderInfo` 호출 후 `saveSocialAccount` 미호출 지점 수정), 순수 단위 테스트 `core-module/src/test/.../member/domain/model/MemberTest`·`MemberSocialAccountTest`·`MemberWithdrawalTest`. `partnership` 도메인 — JPA 연관관계 없는 **단일 애그리거트**(`PartnershipRequest`)에 enum 1개(`PartnershipStatus`, `EnumType.STRING`+`columnDefinition`)만 있는 banner 유사 사례이면서, 기존 `save`가 **detached merge**(`jpaRepository.save(request)` 통째 저장)였던 것을 load-copy-save로 교정한 사례: 순수 모델 `core-module/.../partnership/domain/model/PartnershipRequest`(`of`/`reconstitute`, 재대입되지 않는 필드는 `final`), 어댑터 `infrastructure-module/.../partnership/persistence/`(`PartnershipRequestJpaEntity`/`PartnershipRequestMapper`/`PartnershipRequestJpaRepository`/`PartnershipRepositoryImpl`), 더티 체킹에 의존하던 `changeStatus`/`delete`에 명시적 save 추가(`PartnershipCommandService`), 순수 단위 테스트 `core-module/src/test/.../partnership/domain/model/PartnershipRequestTest`. `policy` 도메인 — JPA 연관관계 없는 **단일 애그리거트**(`PolicyDocument`)에 enum 1개(`PolicyType`, `EnumType.STRING`+`columnDefinition`)만 있는 banner/partnership 유사 사례이면서, 기존 `save`가 **detached merge**(`entityManager.merge(policyDocument)`)였던 것을 load-copy-save로 교정하고, 더티 체킹에 의존하던 지점이 **한 커맨드 메서드 안에 2곳**(`updatePolicy`의 무저장 업데이트, `activatePolicy`에서 `findCurrentEntityByType(...).ifPresent(PolicyDocument::deactivate)`로 비활성화되는 기존 정책의 무저장 변경) 있던 사례: 순수 모델 `core-module/.../policy/domain/model/PolicyDocument`(`of`/`reconstitute`, 재대입되지 않는 필드는 `final`), 어댑터 `infrastructure-module/.../policy/persistence/`(`PolicyDocumentJpaEntity`/`PolicyDocumentMapper`/`PolicyDocumentJpaRepository`/`PolicyDocumentRepositoryImpl`), 명시적 save `PolicyCommandService#updatePolicy`·`#activatePolicy`(비활성화되는 기존 정책도 별도 save), 순수 단위 테스트 `core-module/src/test/.../policy/domain/model/PolicyDocumentTest`. `point` 도메인 — JPA 연관관계 없이 `@Convert` FK VO(`memberId`)로만 연결된 **두 애그리거트**(`MemberPoint`+`MemberPointHistory`)가 있고, `MemberPointHistory`만 조회 결과(`MemberPointHistoryResult.from`)가 감사 시각(`createdAt`)을 직접 소비해 감사 필드를 보유하는 반면 상태전이(`addPoints`/`deductPoints`)가 있는 `MemberPoint`는 어떤 result도 감사 시각을 소비하지 않아 감사 필드가 불필요한 **coupon과 동형의 비대칭 사례**(`updatedAt`은 두 엔티티 모두 미소비): 순수 모델 `core-module/.../point/domain/model/MemberPoint`·`MemberPointHistory`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../point/persistence/`(`MemberPointJpaEntity`/`MemberPointHistoryJpaEntity`/`MemberPointMapper`/`MemberPointHistoryMapper`/`MemberPointJpaRepository`/`MemberPointHistoryJpaRepository`/`MemberPointRepositoryImpl`/`MemberPointHistoryRepositoryImpl`), 더티 체킹에 의존하던 `usePoints`/`earnPoints`/`refundPoints`/`reclaimEarnedPoints`/`deductPoints` 5개 지점에 명시적 save 추가(`PointCommandService`), 순수 단위 테스트 `core-module/src/test/.../point/domain/model/MemberPointTest`·`MemberPointHistoryTest`. `rank` 도메인 — JPA 연관관계 없이 raw FK(`Long rankId`)와 `@Convert` FK VO(`memberId`)로 연결된 **세 애그리거트**(`RankPeriod`+`RankPrize`+`MemberReviewRank`, `MemberReviewRank`는 insert-only로 상태전이·삭제 없음)가 있고, **분리 리팩터링과 별개로 사용자 결정에 따라 `RankPeriod`/`RankPrize`의 하드 삭제를 소프트 삭제로 전환**(DDL에 `is_deleted` 컬럼 신설, `RepositoryImpl`의 `delete(도메인)`이 내부적으로 managed 엔티티의 `deleted` 플래그만 갱신하도록 재구현, 모든 조회 경로에 `deleted.isFalse()` 필터 추가)한 사례, `RankInfoRepositoryImpl`/`MemberReviewRankRepositoryImpl`이 미분리 `file`/`member` 도메인의 Q타입을 함께 쓰는 크로스 도메인 조회: 순수 모델 `core-module/.../rank/domain/model/RankPeriod`·`RankPrize`·`MemberReviewRank`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../rank/persistence/`(`RankPeriodJpaEntity`/`RankPrizeJpaEntity`/`MemberReviewRankJpaEntity`/`RankPeriodMapper`/`RankPrizeMapper`/`MemberReviewRankMapper`/`RankPeriodJpaRepository`/`RankPrizeJpaRepository`/`MemberReviewRankJpaRepository`/`RankPeriodRepositoryImpl`/`RankPrizeRepositoryImpl`/`MemberReviewRankRepositoryImpl`/`RankInfoRepositoryImpl` — `RankInfoRepositoryImpl`은 JPA 리포지토리 없이 순수 QueryDSL 조회만 담당). `MemberReviewRankRepositoryImpl`은 `member` 도메인이 이미 POJO로 전환되어 `QMember`가 core-module에 더 이상 생성되지 않으므로, `follow`/`review` 선례와 동일하게 `PathBuilder<Object>`로 `"MemberJpaEntity"`를 문자열 참조해 필요한 컬럼만 노출(신규 재사용이 아니라 기존 크로스 도메인 참조 패턴의 반복 적용). 더티 체킹에 의존하던 `updatePeriod`/`updatePrize`에 명시적 save 추가(`RankCommandService`), 순수 단위 테스트 `core-module/src/test/.../rank/domain/model/RankPeriodTest`·`RankPrizeTest`·`MemberReviewRankTest`. `reservation` 도메인 — JPA 연관관계 없이 raw FK(`Long shopId`)와 `@Convert` FK VO(`memberId`)로 연결된 **두 애그리거트**(`Reservation`+`ReservationSlot`)가 있고, **레퍼런스 최초로 `@Version` 낙관적 락 애그리거트**(`ReservationSlot`)를 분리한 사례: POJO에 `version` 필드를 유지해 재구성 시 주입하고, `save`의 load-copy-save가 managed 엔티티의 `@Version`을 그대로 검증·증가시켜 `ReservationCreator`의 재시도 루프(낙관적 락/유니크 충돌 감지) 동작을 보존함. `Reservation`만 조회 결과(`ReservationResult.from`)가 감사 시각(`createdAt`)을 직접 소비해 감사 필드를 보유하는 반면 `ReservationSlot`은 어떤 result도 감사 시각을 소비하지 않아 감사 필드가 불필요한 **coupon/point와 동형의 비대칭 사례**(`updatedAt`은 둘 다 미소비): 순수 모델 `core-module/.../reservation/domain/model/Reservation`·`ReservationSlot`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../reservation/persistence/`(`ReservationJpaEntity`/`ReservationSlotJpaEntity`/`ReservationMapper`/`ReservationSlotMapper`/`ReservationJpaRepository`/`ReservationSlotJpaRepository`/`ReservationRepositoryImpl`/`ReservationSlotRepositoryImpl`), 더티 체킹에 의존하던 `confirm`/`reject`/`complete`/`cancel`과 슬롯 반납 `releaseSlot`에 명시적 save 추가(`ReservationCommandService`), 순수 단위 테스트 `core-module/src/test/.../reservation/domain/model/ReservationTest`·`ReservationSlotTest`. (슬롯 시간·정원 상수 유틸은 이 애그리거트와 이름이 겹치지 않도록 `SlotPolicy`로 별도 유지 — [Command/DTO 네이밍 순서 규칙](#commanddto-네이밍-순서-규칙-도메인동작-형태) 등과 무관한 순수 리네이밍 사례.) `search` 도메인 — JPA 연관관계·ID VO 없이 독립된 **세 애그리거트**(`PopularKeyword`+`RecommendedKeyword`+`SearchKeywordLog`)가 있고, 그중 `RecommendedKeyword`는 **Java 애플리케이션 계층에 생성/변경 경로가 전혀 없는 읽기 전용 애그리거트(SQL/수동 시드)라 `of` 없이 `reconstitute`만 공개하는 이 프로젝트 최초의 read-only 분리 사례**: 순수 모델 `core-module/.../search/domain/model/PopularKeyword`·`RecommendedKeyword`·`SearchKeywordLog`(전 필드 `final`; `PopularKeyword`/`SearchKeywordLog`는 `of`/`reconstitute` 둘 다, `RecommendedKeyword`는 `reconstitute`만), 어댑터 `infrastructure-module/.../search/persistence/`(`PopularKeywordJpaEntity`/`RecommendedKeywordJpaEntity`/`SearchKeywordLogJpaEntity`/`PopularKeywordMapper`/`RecommendedKeywordMapper`/`SearchKeywordLogMapper`/`PopularKeywordJpaRepository`/`RecommendedKeywordJpaRepository`/`SearchKeywordLogJpaRepository`/`PopularKeywordRepositoryImpl`/`RecommendedKeywordRepositoryImpl`/`SearchKeywordLogRepositoryImpl` — `PopularKeywordRepositoryImpl`만 QueryDSL로 벌크 `deleteAll()`, `SearchKeywordLogJpaRepository`의 `@Modifying` JPQL은 엔티티명을 `SearchKeywordLogJpaEntity`로 갱신). **update 경로 자체가 없어 세 애그리거트 모두 신규 insert만 수행**(`PopularKeywordRepositoryImpl#saveAll`은 도메인↔엔티티 리스트를 매핑만 하고 load-copy-save 불필요), 명시적 save 규칙은 `admin` 선례와 동일하게 대상 없음(`SearchKeywordCommandService`가 `deleteAll`+`saveAll`+`deleteOlderThan`만 수행). 자체 애그리거트 없이 다른 도메인에 위임하는 `SearchResultQueryService`는 전환 범위에서 제외. 순수 단위 테스트 `core-module/src/test/.../search/domain/model/PopularKeywordTest`·`RecommendedKeywordTest`·`SearchKeywordLogTest`. `review` 도메인 — JPA 연관관계 없이 raw FK(`Long reviewId`/`commentId` 등)와 `@Convert` FK VO(`memberId`, `ReviewReply`는 `memberId`+`replyToMemberId` 2개)로만 연결된 **여섯 애그리거트**(`Review`+`ReviewComment`+`ReviewReply`+`ReviewImage`+`ReviewLike`+`ReviewTag`)가 있고, 감사 시각 소비 여부로 셋씩 갈리는 사례(`Review`/`ReviewComment`/`ReviewReply`는 `createdAt` 보유 — 각각 응답·`ReviewQueryService`의 `findCommentsIncludingHidden`/`findRepliesIncludingHidden`이 소비, `ReviewImage`/`ReviewLike`/`ReviewTag`는 불변 애그리거트라 감사 필드 생략), **전환과 별개로 죽은 코드였던 일곱 번째 애그리거트 `ReviewProduct`(전 코드베이스에 실제 호출부 없음)를 이번 PR에서 완전 삭제**(모델·리포지토리 인터페이스·JpaRepository·RepositoryImpl 전부 제거)한 사례: 순수 모델 `core-module/.../review/domain/model/Review`·`ReviewComment`·`ReviewReply`·`ReviewImage`·`ReviewLike`·`ReviewTag`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../review/persistence/`(6개 애그리거트 × `JpaEntity`/`Mapper`/`JpaRepository`/`RepositoryImpl`) — `ReviewRepositoryImpl`이 자신의 서브쿼리 Q타입(`QReviewImageJpaEntity`/`QReviewLikeJpaEntity`/`QReviewCommentJpaEntity`, 별칭 인스턴스 `subReviewImage`/`subReviewLike`/`subReviewComment`/`sortReviewLike` 포함)을 모두 치환하면서, 미분리 도메인의 `QUploadedFile`/`QOrderProduct`/`QProduct`/`QStation`(당시 `shop`은 미분리라 `QShop`도 포함— 이후 `shop` 전환 시 `PathBuilder`로 대체됨, 아래 `shop` 항목 참고)과 `member` 도메인의 `MemberJpaEntity` `PathBuilder` 문자열 참조는 그대로 유지한 크로스 도메인 QueryDSL 사례. 이 `review.domain.model.QReview`가 사라지면서 core-module에 남아 있던 다른 도메인(`shop`)의 `ShopRepositoryImpl`이 `QReview`를 직접 조인하던 지점도 함께 깨져, `member`/`follow`/`rank` 선례와 동일하게 `PathBuilder<Object>`로 `"ReviewJpaEntity"`를 문자열 참조하도록 전환(기존 크로스 도메인 참조 패턴의 반복 적용). `Review`/`ReviewComment`/`ReviewReply`의 `hide`/`unhide`/`updateContent`가 더티 체킹에 의존하던 지점에 명시적 save 추가(`ReviewCommandService#changeReviewHidden`·`#changeCommentHidden`·`#changeReplyHidden`·`#updateReview`), 순수 단위 테스트 `core-module/src/test/.../review/domain/model/ReviewTest`·`ReviewCommentTest`·`ReviewReplyTest`·`ReviewImageTest`·`ReviewLikeTest`·`ReviewTagTest`. `shop` 도메인 — **@Entity 17개(+enum 5개) 중 핵심 `Shop` 애그리거트 1개만 우선 전환**하고 나머지 16개 자식 엔티티(`ShopAmenity`/`ShopBusinessHour`/`ShopChoice` 등)와 그 JpaRepository는 현행(core-module 잔류) 유지한 최초의 **부분 전환** 사례 — JPA 연관관계는 없으나 `Shop`이 다른 다섯 파일(`order`/`product`/`review` 및 `shop` 자신의 `ShopChoiceRepositoryImpl`, 그리고 자기 자신인 이동 대상 `ShopRepositoryImpl`)에서 `QShop`으로 참조되고 있어 회귀 범위가 컸다: 순수 모델 `core-module/.../shop/domain/model/Shop`(`of`/`reconstitute`), 어댑터 `infrastructure-module/.../shop/persistence/`(`ShopJpaEntity`/`ShopMapper`/`ShopJpaRepository`/`ShopRepositoryImpl` — 자신의 자식 Q타입 `QShopAmenity`/`QShopFoodType`/`QShopFoodTypeCategory`/`QShopBookmark`/`QShopAmenityCategory`/`QStation`은 core에 잔류해 그대로 import, `review`가 이미 POJO라 `QReview` 대신 `PathBuilder<Object>`로 `"ReviewJpaEntity"`를 문자열 참조). **`QShop` 소멸로 core에 남은 4개 파일이 함께 깨진 크로스 도메인 팬아웃**: `order/.../OrderRepositoryImpl`·`product/.../ProductRepositoryImpl`·`shop/.../ShopChoiceRepositoryImpl`(모두 core 잔류)과 `infrastructure-module`로 이미 이동한 `review/.../ReviewRepositoryImpl`까지 4곳 전부 `member`/`follow`/`rank` 선례와 동일하게 `PathBuilder<Object>`로 `"ShopJpaEntity"`를 문자열 참조하도록 전환(기존 크로스 도메인 참조 패턴의 반복 적용, 이번이 가장 넓은 팬아웃 사례). `ShopQueryService#findShopById`가 core에서 곧 사라질 `ShopJpaRepository`를 직접 주입받아 쓰던 지점을 `ShopRepository#findById`로 교체. `ShopCommandService`의 `createShop`/`updateShop`/`closeShop`은 이미 명시적 `save` 호출 중이라 추가 조치 불필요(`save()`만 detached merge → load-copy-save로 교정), 순수 단위 테스트 `core-module/src/test/.../shop/domain/model/ShopTest`.

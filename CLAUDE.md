## AI 규칙

명령어에 대한 답변은 한국어로 하도록 합니다.

명령된 로직을 구현 후, gradle build 테스트는 진행하지 않도록 합니다.

## GIT 규칙

NO_COMMIT_OR_ROLLBACK

## 네이밍 규칙

파일명, 변수명, 함수명 등 모든 네이밍은 최적의 이름을 선택하도록 합니다. 명확하고 의미 있는 이름을 사용하여 코드의 가독성과 유지보수성을 높입니다.

## Command/DTO 네이밍 순서 규칙 (`{도메인}{동작}` 형태)

command·result 등 도메인 DTO의 이름은 **`{도메인}` 접두어 + `{동작}` + 접미어** 순서로 짓습니다. 동작(Create/Update/Delete 등)을 접두어로 두는 `{동작}{도메인}` 형태(예: `CreateBannerCommand`)는 사용하지 않습니다. 도메인명을 앞에 두면 같은 애그리거트의 DTO들이 IDE·파일 탐색기·import 목록에서 이름순으로 인접하게 모여 응집도가 드러나고, 도메인 단위로 일괄 검색·정렬하기 쉽습니다.

- **command record**: `{도메인}Create Command` / `{도메인}Update Command` / `{도메인}Delete Command` → 예: `NoticeCreateCommand`, `NoticeUpdateCommand`, `BannerCreateCommand`, `BannerUpdateCommand` (공백은 표기상 구분일 뿐 실제 타입명은 붙여 씀).
- **condition record**: `{도메인}SearchCondition` → 예: `NoticeSearchCondition`.
- **response/result record**: `{도메인}{용도}Response` / `{도메인}{용도}Result` → 예: `NoticePageResponse`, `NoticeListPageResult`.
- 한 도메인 안에서 일부만 이 규칙을 따르고 나머지는 `{동작}{도메인}` 형태로 남기는 **혼재 상태를 금지**합니다(예: `BannerUpdateCommand`와 `CreateBannerCommand`가 공존하는 것). 신규 작성·기존 수정 모두 이 순서로 통일합니다.

reference 구현: `notice` 도메인 — `NoticeCreateCommand`, `NoticeUpdateCommand`, `NoticeSearchCondition`, `NoticePageResponse`. (과거 `CreateNoticeCommand`였다가 `NoticeCreateCommand`로 리네이밍하여 이 순서로 확정한 전례가 있습니다.)

## DTO 조립 규칙 (`new` 직접 호출 지양)

컨트롤러·Facade·서비스 등 **호출부에서 DTO(command / condition / response record)를 `new`로 직접 조립하지 않습니다.** 대신 변환 책임을 해당 타입 또는 소스 타입으로 위임합니다. 이는 필드 추가 시 호출부 연쇄 수정을 막고, 조립 로직을 한 곳에 모아 가독성과 응집도를 높입니다.

- **원시 파라미터 → command/VO/condition 변환**: command·condition 등 대상 record 자신에 정적 팩토리 `of(...)`를 두고 `Xxx.of(a, b, c)`로 생성합니다. Request DTO는 command 생성 책임을 지지 않는 순수 데이터 홀더(검증 + Swagger 스키마)로 유지하고, `toCommand()` 같은 변환 메서드를 두지 않습니다.
- **호출 경로**: 컨트롤러가 Request를 개별 원시 필드로 언패킹(`request.title()` 등)해 Facade/서비스에 전달하고, Facade/서비스는 그 원시 파라미터로 `Command.of(...)`를 호출합니다. Facade가 원시 파라미터만 받으므로 admin-api Request 타입에 의존하지 않습니다.
- **도메인/DTO → 응답 변환**: 응답 record에 정적 팩토리 `from(...)`을 두고 `XxxResponse.from(source)`로 생성합니다. `PageResult<T>` → 응답 페이지 타입 변환도 `from(pageResult)`로 위임합니다.
- `new`는 이러한 팩토리 메서드 **내부**에만 남깁니다(각 record가 자기 자신을 생성). 호출부에는 `new`가 남지 않는 것을 목표로 합니다.

reference 구현: `admin-api`의 `notice` 도메인 — `CreateNoticeCommand.of(...)`, `NoticeUpdateCommand.of(...)`, `NoticeSearchCondition.of(...)`, `NoticePageResponse.from(...)`, `NoticeService#createNotice(String, String, boolean)`.

## command/DTO 지역 변수 추출 규칙 (호출 인자로 인라인 조립 지양)

위 [DTO 조립 규칙](#dto-조립-규칙-new-직접-호출-지양)으로 `Xxx.of(...)` 팩토리를 쓰더라도, **그 팩토리 호출 결과를 다른 메서드(주로 core application command 서비스)의 인자 자리에 인라인으로 바로 넘기지 않고, 먼저 지역 변수(`command`)로 추출한 뒤 그 변수를 전달합니다.** `of(...)`로 조립하는 것과 조립 결과를 어떻게 넘기는지는 별개이며, 이 규칙은 후자를 다룹니다.

- **왜 지역 변수로 추출하는가**: (1) 조립(무엇을 만드는가)과 호출(어디에 넘기는가)이 한 줄에 겹쳐 있으면, 인자가 많은 command일수록 한 줄이 길어지고 어떤 값이 어느 필드로 가는지 읽기 어렵습니다. 이름 붙은 지역 변수로 분리하면 "이 줄은 command를 만든다 / 이 줄은 그것을 서비스에 넘긴다"가 문장 단위로 드러납니다. (2) 디버깅 시 조립된 command에 중단점·로그를 걸거나 값을 들여다보기 쉽습니다. (3) 호출부 형태가 도메인 간 동일해져(항상 `Xxx command = Xxx.of(...); service.method(id, command);`) 리뷰·검색·패턴 일치가 단순해집니다.
- **적용 대상**: command 서비스 호출에 넘기는 **command DTO**가 주 대상입니다. 변수명은 `command`로 통일합니다(한 메서드에 command가 하나인 것이 일반적).
- **적용 예외**: 이미 지역 변수로 조립되고 있던 **`SearchCondition`은 그대로 둡니다**(조회 메서드에서 관례적으로 `XxxSearchCondition condition = ...of(...)`로 이미 분리되어 있음 — reference: `NoticeService#getNotices`의 `condition`). `XxxId.of(id)`처럼 **식별자 승격 한 줄**은 인자 자리에 인라인으로 두어도 됩니다(짧고 의미가 자명하며, command와 나란히 `service.method(XxxId.of(id), command)` 형태가 관례). 응답 변환 `XxxResponse.from(...)`도 대상이 아닙니다(반환식에 바로 쓰는 것이 자연스러움).
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

update 계열처럼 식별자와 command를 함께 넘길 때도 command만 추출하고 `XxxId.of(id)`는 인자 자리에 둡니다:

```java
BannerUpdateCommand command = BannerUpdateCommand.of(BannerType.from(type), title, imageFileId, linkUrl, startDate, endDate, sort, visible);
bannerCommandService.updateBanner(BannerId.of(id), command);
```

reference 구현: `admin-api`의 `notice` 도메인 — `NoticeService#createNotice`·`#updateNotice`(`NoticeCreateCommand command = ...; noticeCommandService.createNotice(command);`). 동일 적용: `admin`(`AdminAccountService#create`), `banner`(`createBanner`·`updateBanner`), `bug`(`changeStatus`·`classify`·`assign`), `coupon`(`createCoupon`·`updateCoupon`), `policy`(`createPolicy`·`updatePolicy`).

## record 파일 분리 규칙 (중첩 record 선언 지양)

**`record`는 서비스·컨트롤러·Facade 등 다른 클래스 본문 안에 중첩 선언하지 않고, 각 도메인의 관례 위치에 독립된 `.java` 파일로 둡니다.** 클래스 안에 DTO record를 함께 두면 파일 응집도가 떨어지고, 다른 클래스에서 참조할 때 `Outer.Inner` 접두사가 붙어 결합도가 커지며, DTO 조립 규칙(정적 팩토리 위임)을 적용하기도 불리합니다.

- **분리 위치**: 각 도메인의 기존 관례를 따릅니다. web-api/admin-api의 응답성 record는 해당 패키지의 `response/` 하위에, core-module의 결과 DTO는 `application/dto/result/`(command는 `application/dto/command/`)에 둡니다.
- **접근제어자**: 별도 파일로 빼면 최상위 타입이 되므로 `public record`로 선언합니다. 한 클래스 내부에서만 쓰이던 `private`/`package-private` 헬퍼 record도 분리 시 `public`으로 격상합니다.
- **적용 대상**: 응답/결과 DTO뿐 아니라 서비스 내부 전용 헬퍼 record(예: 조회 중간 계산용)도 동일하게 분리합니다.
- 이미 자기 파일 하나에 정의된 최상위 record(도메인 이벤트, ID VO 등)는 그대로 두며, 이 규칙은 "다른 클래스 본문 안에 중첩된 record"를 제거하는 것을 목표로 합니다.

reference 구현: `web-api`의 `NoticeListPageResult`(`notice/response/`), `PolicyListPageResult`(`policy/response/`), `OrderListPageResult`(`order/response/`)와 `core-module`의 `OptionInfo`(`product/application/dto/result/`, `private` → `public` 격상).

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

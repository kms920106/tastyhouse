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
| 5 | **presentation** (가장 바깥) | `com.tastyhouse.webapi.*`, `com.tastyhouse.adminapi.*` |

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

**신규 작성·기존 파일 수정 시 수동 적용**하며, 기존 코드를 이 규칙만을 위해 일괄 재정렬하지 않습니다.

자동 강제 도구(spotless 등)는 도입하지 않으며, 신규/수정 코드 작성 시 이 규칙을 수동으로 따릅니다.

**참고 자료 (Spring 공식 소스)**:
- Spring Java Format `SpringImportOrderCheck` 구현: https://github.com/spring-io/spring-javaformat/blob/main/spring-javaformat/spring-javaformat-checkstyle/src/main/java/io/spring/javaformat/checkstyle/check/SpringImportOrderCheck.java
- Spring Java Format checkstyle 설정: https://github.com/spring-io/spring-javaformat/blob/main/spring-javaformat/spring-javaformat-checkstyle/src/main/resources/io/spring/javaformat/checkstyle/spring-checkstyle.xml
- Checkstyle `ImportOrder` 규칙 문서: https://checkstyle.sourceforge.io/checks/imports/importorder.html

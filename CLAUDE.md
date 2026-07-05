## AI 규칙

명령어에 대한 답변은 한국어로 하도록 합니다.

명령된 로직을 구현 후, gradle build 테스트는 진행하지 않도록 합니다.

## GIT 규칙

NO_COMMIT_OR_ROLLBACK

## 네이밍 규칙

파일명, 변수명, 함수명 등 모든 네이밍은 최적의 이름을 선택하도록 합니다. 명확하고 의미 있는 이름을 사용하여 코드의 가독성과 유지보수성을 높입니다.

## DTO 조립 규칙 (`new` 직접 호출 지양)

컨트롤러·Facade·서비스 등 **호출부에서 DTO(command / condition / response record)를 `new`로 직접 조립하지 않습니다.** 대신 변환 책임을 해당 타입 또는 소스 타입으로 위임합니다. 이는 필드 추가 시 호출부 연쇄 수정을 막고, 조립 로직을 한 곳에 모아 가독성과 응집도를 높입니다.

- **원시 파라미터 → command/VO/condition 변환**: command·condition 등 대상 record 자신에 정적 팩토리 `of(...)`를 두고 `Xxx.of(a, b, c)`로 생성합니다. Request DTO는 command 생성 책임을 지지 않는 순수 데이터 홀더(검증 + Swagger 스키마)로 유지하고, `toCommand()` 같은 변환 메서드를 두지 않습니다.
- **호출 경로**: 컨트롤러가 Request를 개별 원시 필드로 언패킹(`request.title()` 등)해 Facade/서비스에 전달하고, Facade/서비스는 그 원시 파라미터로 `Command.of(...)`를 호출합니다. Facade가 원시 파라미터만 받으므로 admin-api Request 타입에 의존하지 않습니다.
- **도메인/DTO → 응답 변환**: 응답 record에 정적 팩토리 `from(...)`을 두고 `XxxResponse.from(source)`로 생성합니다. `PageResult<T>` → 응답 페이지 타입 변환도 `from(pageResult)`로 위임합니다.
- `new`는 이러한 팩토리 메서드 **내부**에만 남깁니다(각 record가 자기 자신을 생성). 호출부에는 `new`가 남지 않는 것을 목표로 합니다.

reference 구현: `admin-api`의 `notice` 도메인 — `CreateNoticeCommand.of(...)`, `NoticeUpdateCommand.of(...)`, `NoticeSearchCondition.of(...)`, `NoticePageResponse.from(...)`, `NoticeService#createNotice(String, String, boolean)`.

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

자동 강제 도구(spotless 등)는 도입하지 않으며, 신규/수정 코드 작성 시 이 규칙을 수동으로 따릅니다.

**참고 자료 (Spring 공식 소스)**:
- Spring Java Format `SpringImportOrderCheck` 구현: https://github.com/spring-io/spring-javaformat/blob/main/spring-javaformat/spring-javaformat-checkstyle/src/main/java/io/spring/javaformat/checkstyle/check/SpringImportOrderCheck.java
- Spring Java Format checkstyle 설정: https://github.com/spring-io/spring-javaformat/blob/main/spring-javaformat/spring-javaformat-checkstyle/src/main/resources/io/spring/javaformat/checkstyle/spring-checkstyle.xml
- Checkstyle `ImportOrder` 규칙 문서: https://checkstyle.sourceforge.io/checks/imports/importorder.html

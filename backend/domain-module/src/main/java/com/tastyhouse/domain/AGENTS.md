<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# domain (DDD domain root)

## Purpose

DDD(Domain-Driven Design) 패턴으로 설계된 모든 Bounded Context가 거주하는 핵심 계층입니다. **프레임워크 의존이 전혀 없습니다** — Spring(Web/tx/orm)·JPA(`jakarta.persistence`)·QueryDSL(`com.querydsl`)을 import하지 않으며, 이 모듈의 production 의존은 **하나도 없습니다**(Lombok까지 제거되어 접근자·생성자를 수기로 작성합니다). 각 Bounded Context는 `<ctx>/{model,vo,event,repository,service,port}` 구조를 가지며, 여기에 공유 커널(`shared/`)과 공통 예외(`exception/`)가 더해집니다.

> 과거 이 패키지는 `com.tastyhouse.core`였고 도메인마다 `application/`(서비스·DTO)과 `infrastructure/`(JPA 구현)를 함께 갖고 있었습니다. `core-module` → `domain-module` 전환으로 **`application/`은 해체**(조회는 infrastructure-module `<ctx>/query/`, 액터 특화 command는 각 소비 모듈의 CQRS 서비스, 불변식 오케스트레이션은 `domain/service/`로 하강)되고 **`infrastructure/`는 `infrastructure-module`로 이동**했습니다. 이 패키지에는 이제 domain·shared·exception만 남습니다.

## Key Files / Cross-cutting packages

| 패키지/파일 | 설명 |
|---|---|
| `shared/vo/PhoneNumber.java` | 공유 커널 Value Object. `record`(compact constructor 검증) — `@Embeddable` 어노테이션 없음, 컬럼 매핑은 각 JpaEntity의 `@AttributeOverride`가 소유 |
| `shared/model/ApprovalStatus.java` | 승인 워크플로 공용 enum(PENDING/APPROVED/REJECTED). 상표·대표이미지 변경요청 등에서 재사용 |
| `shared/page/PageQuery.java` / `PageResult.java` | 프레임워크-프리 페이징 계약. infrastructure-module의 `<ctx>/query/` DAO가 반환 타입으로 사용 |
| `shared/event/DomainEventPublisher.java` | 이벤트 발행 **출력 포트**. 스프링 구현체(`SpringDomainEventPublisher`)는 infrastructure-module 소유 |
| `shared/exception/OptimisticLockConflictException.java` | 낙관적 락 충돌의 프레임워크-프리 표현. 스프링 `ObjectOptimisticLockingFailureException` → 이 예외 번역은 infrastructure-module의 `RepositoryImpl` 담당 |
| `exception/ErrorCode.java` | 도메인 에러 코드 enum. `httpStatusCode`(int)/`code`(String)/`defaultMessage`(String). Spring Web 비의존이므로 `HttpStatus` 대신 int 사용 |
| `exception/BusinessException.java` | 기본 비즈니스 예외. 모든 도메인 예외의 부모 |
| `exception/ResourceNotFoundException.java` | 리소스(애그리거트) 미존재 예외 (BusinessException 상속). 과거 `EntityNotFoundException`이었으나 `jakarta.persistence.EntityNotFoundException`과 동명이라 JPA 관심사로 오해될 수 있어 리네이밍 |
| `exception/ErrorCodeSpec.java` | 에러코드 공통 계약 인터페이스(`getHttpStatusCode`/`getCode`/`getDefaultMessage`). `ErrorCode`와 `infrastructure:external`의 `ExternalApiErrorCode`(`com.tastyhouse.external.exception`)가 구현하며, `BusinessException`이 이 타입을 보유해 전역 핸들러 하나가 두 계열을 모두 처리한다 |

> JPA 설정(`@EnableJpaRepositories`/`@EntityScan`/`@EnableJpaAuditing`/`@EnableTransactionManagement`)·`QueryDslConfig`·`BaseEntity`는 이 패키지에 없습니다. 전부 `infrastructure-module`(`InfrastructurePersistenceConfig`·`config/QueryDslConfig`·`shared/persistence/BaseEntity`)이 소유합니다. 도메인 서비스 빈 등록도 이 패키지가 아니라 infrastructure-module의 컨텍스트별 `<ctx>/config/<Ctx>DomainConfig` 소관입니다.

## Bounded Contexts

각 컨텍스트는 `<ctx>/domain/` 아래에 `model`(애그리거트, 순수 POJO) / `vo`(ID VO 등) / `event`(DomainEvent record) / `repository`(write 포트) / `service`(불변식 오케스트레이션·무상태 정책 POJO) / `port`(외부 어댑터 출력 포트)를 갖습니다. 아래 표의 숫자는 해당 하위 패키지의 파일 수입니다.

| 도메인 | 목적 | model | vo | event | repo | service | port |
|---|---|---|---|---|---|---|---|
| admin | 관리자 계정 | 3 | 1 | - | 1 | - | - |
| banner | 배너 관리 | 2 | 1 | - | 1 | - | - |
| bug | 버그 리포팅 | 6 | 1 | - | 2 | 1 | - |
| ceo | 점주 계정 (`admin`과 동형 최소 CRUD, `role` 없이 `status`만) | 2 | 1 | - | 1 | - | - |
| coupon | 쿠폰 발급/사용 | 3 | 2 | 2 | 2 | 2 | - |
| event | 이벤트/프로모션 (Event/Winner/Announcement 3 애그리거트) | 4 | 1 | - | 3 | - | - |
| faq | FAQ (Faq/FaqCategory) | 2 | 2 | - | 2 | 1 | - |
| file | 파일 업로드/관리 | 1 | 1 | 1 | 1 | 2 | 1 (`FileStoragePort`) |
| member | 회원 관리 (하위 `follow`·`referral` 포함). 다른 모든 BC가 `MemberId`로 참조하는 핵심 도메인 | 11 | 4 | 3 | 5 | 4 | - |
| notice | 공지사항 (분리 패턴 reference 도메인) | 1 | 1 | - | 1 | - | - |
| mail | 메일(이메일 주소) 인증 — 발급 시 발송까지 원자적 수행 | 3 | 2 | 1 | 1 | 1 | 1 (`MailSender`) |
| order | 주문 (Order/OrderProduct/OrderProductOption) | 4 | 3 | 2 | 3 | 5 | - |
| partnership | 제휴 신청 | 2 | 1 | - | 1 | - | - |
| payment | 결제 (Payment/PaymentRefund/TossPaymentRecord) | 8 | 4 | 3 | 3 | 3 | 4 (`PgPaymentGateway` + dto) |
| point | 포인트 (Point/PointHistory) | 3 | - | 3 | 2 | 1 | - |
| policy | 정책/약관 버전 관리 | 2 | 1 | 1 | 1 | 1 | - |
| product | 상품·옵션 (8 애그리거트) | 8 | 5 | 3 | 8 | 2 | 1 (`ProductReviewStatisticsPort`) |
| rank | 리뷰 랭킹/기간·상품 | 4 | 2 | - | 3 | 1 | 2 (`MemberReviewCountPort`) |
| reservation | 시간대 예약 — `@Version` 낙관적 락 정원 관리 | 3 | 1 | - | 2 | 2 | - |
| review | 리뷰/댓글/답글/이미지/좋아요/태그 (6 애그리거트) | 7 | 4 | 3 | 6 | 2 | - |
| search | 검색어 (PopularKeyword/SearchKeywordLog) | 2 | - | - | 2 | 1 | - |
| shop | 가게/식당 + 자식 애그리거트 다수 — 최대 도메인 | 36 | 1 | - | 14 | 8 | - |
| sms | SMS(휴대폰번호) 인증 — 발급 시 발송까지 원자적 수행 | 3 | 2 | 1 | 1 | 1 | 1 (`SmsSender`) |

## For AI Agents

### Working In This Directory

**계층 의존 방향 규칙**:

```
presentation + application (web-api / admin-api / ceo-api / batch-module)
   · {도메인}CommandService(@Transactional) / {도메인}QueryService(@Transactional(readOnly))
        ↓                                              ↓
   domain (이 패키지)                       infrastructure-module
   · model/vo/event/repository(write 포트)     · <ctx>/persistence (write 어댑터)
   · domain/service (POJO 불변식·정책)  ←DIP─  · <ctx>/query (read: QueryDao + Result)
   · domain/port (출력 포트)            ←DIP─  · <ctx>/listener, <ctx>/config/<Ctx>DomainConfig
        ↑                                     ↑
   shared (kernel), exception            infrastructure:{external,firebase,aws,
                                          oauth,payment,messaging,crawling}
                                          (외부 연동 port 구현)
```

- **domain 계층에 프레임워크 import 금지**: `org.springframework.*`·`jakarta.persistence.*`·`com.querydsl.*`를 넣지 않는다. build.gradle에 해당 의존이 없으므로 시도하면 컴파일이 깨진다 — 그 관심사는 `infrastructure-module` 소관이다.
- **`@Entity`는 이 패키지에 없다**: 도메인 모델은 전 도메인 순수 POJO다. `@OneToMany`/`@ManyToOne`/`@ElementCollection`은 애초에 표현할 수 없으며, 외부 애그리거트 참조는 ID VO로만 한다.
- **BC 간 통신**: 도메인 서비스 호출 또는 `DomainEventPublisher` 포트를 통한 DomainEvent로만 한다(다른 BC의 model 직접 조작 금지). 리스너는 infrastructure-module의 `<ctx>/listener/`에 둔다.
- **표현 목적 조회는 이 패키지에 두지 않는다**: Repository 인터페이스에는 write 포트만 남긴다(`findById`/`save`/`saveAndFlush`/`delete`/`existsByX`/`findByNaturalKey`/검증용 `countByX`/락 획득용 조회). Result DTO·`PageResult` 반환·조인 투영·목록·검색·페이징은 infrastructure-module의 `<ctx>/query/{도메인}QueryDao`가 소유한다.

**ID 참조 규칙**:
- 외부 BC의 애그리거트는 ID VO로만 참조한다(예: `Order.memberId : MemberId`, `Payment.orderId : OrderId`).
- `XxxId`는 `record XxxId(Long value)` + compact constructor 검증 + 정적 팩토리 `of(Long)`. `new`는 `of()` 내부에만 남긴다. JPA 매핑용 `AttributeConverter`는 infrastructure-module(`<ctx>/persistence/XxxIdConverter`)에 있다.

**도메인 서비스 규칙 (`<ctx>/service/`)**:
- `@Service`/`@Component`/`@Transactional`을 붙이지 않는 **순수 POJO**다. 빈 등록은 infrastructure-module의 해당 컨텍스트 `<ctx>/config/<Ctx>DomainConfig`가 `@Bean` 팩토리로 수행하고(없으면 신설), 트랜잭션 경계는 이를 호출하는 api 모듈의 `{도메인}CommandService`가 소유한다.
- 여기 두는 것: (C) 한 트랜잭션에서 2개 이상 애그리거트 타입을 load & save하는 **불변식 오케스트레이션**(reference: `order/service/OrderPlacementService`, `payment/service/PaymentConfirmationService`·`PaymentCancellationService`, `point/service/PointLedgerService`, `reservation/service/ReservationBookingService`), (D) **무상태 정책·검증기**(reference: `faq/service/FaqCategoryDeletionPolicy`, `shop/service/ProhibitedWordValidator`).
- 소비 모듈로 복제하지 않는다 — 특정 api 모듈에 두면 다른 모듈이 같은 유스케이스를 실행할 때 불변식이 우회된다.

**Service 분할 규칙 (CQS)** — api 모듈 측:
- `{도메인}CommandService`(`@Transactional`): domain write 포트·도메인 서비스만 주입. 식별자만 반환.
- `{도메인}QueryService`(`@Transactional(readOnly = true)`): infra `{도메인}QueryDao`만 주입. Response 조립은 private 매퍼.

**DTO 규칙**:
- 이 패키지는 조회 결과 DTO를 갖지 않는다. Result record(`@QueryProjection` 포함)와 `SearchCondition`은 infrastructure-module `<ctx>/query/` 소유이며, 접미어 `Result` 통일(`Dto` 금지)·admin 충돌 시 `Management` 한정어 규칙이 그 위치에서 적용된다.
- 도메인 서비스·모델은 presentation의 Request 타입을 파라미터로 받지 않는다(레이어 역전 방지). HTTP 경계에서 `String`/`Long`으로 받아 api 모듈 서비스가 `Enum.from(String)`·`XxxId.of(Long)`으로 승격한 뒤 넘긴다.
- `record`는 다른 클래스 본문 안에 중첩 선언하지 않고 독립 `.java`로 둔다(`vo/`, `event/`, `port/dto/`).

### Testing Requirements

- **순수 단위 테스트**(`domain-module/src/test`)로 불변식·상태전이를 검증한다 — 스프링 컨텍스트·DB 불필요. 현재 도메인 단위 테스트 368개가 전부 통과 상태다.
- 새 애그리거트·상태전이 추가 시 대응 `XxxTest`를 함께 추가한다(reference: `notice/model/NoticeTest`).
- `ddl-auto=validate` 스키마 정합성·enum `columnDefinition` 검증은 JPA 엔티티를 소유한 `infrastructure-module` 책임이다.
- 레이어 경계 회귀는 api 4개 모듈의 `architecture/LayerRulesTest`(ArchUnit)가 지킨다.

### Common Patterns

**Repository write 포트 + load-copy-save**:
```java
// domain/<ctx>/repository/NoticeRepository.java (이 패키지 — 인터페이스만)
public interface NoticeRepository {
    Optional<Notice> findById(NoticeId noticeId);
    Notice save(Notice notice);
}

// infrastructure-module: <ctx>/persistence/NoticeRepositoryImpl (@Repository)
//  - save: id null이면 insert, 있으면 managed 엔티티 조회 후 Mapper.applyChanges 복사
//  - detached merge 금지(@CreatedDate(updatable=false) 감사 필드 파손 방지)
```

**순수 도메인 모델 (팩토리 2종 + final 필드)**:
```java
public class Notice {

    private final Long id;          // 미영속이면 null
    private String title;           // 상태전이로 재대입되므로 non-final

    // 신규 생성 — 불변식 검증
    public static Notice of(String title, String content, boolean visible) { ... }

    /** DB 재구성 전용 — 인프라(매퍼)만 호출한다. 불변식 우회 방지. */
    public static Notice reconstitute(Long id, ..., LocalDateTime createdAt, LocalDateTime updatedAt) { ... }
}
```
- 상태전이로 재대입되지 않는 필드는 `final`로 선언한다(reference: `admin/model/Admin` — update 경로가 없어 전 필드 `final`).
- **명시적 save 필수**: POJO는 JPA 더티 체킹으로 자동 flush되지 않으므로 변경 후 반드시 `repository.save(domain)`을 호출한다. 누락하면 변경이 조용히 유실된다.

**`@Embedded` 대상 VO는 `record`**:
```java
public record PhoneNumber(String value) {

    public PhoneNumber {
        // 검증은 compact constructor에서
    }
}
```
- Hibernate 6이 `@Embedded` 값 객체를 canonical 생성자로 인스턴스화하므로, 일반 class + 검증 생성자는 런타임 `InstantiationException`을 유발한다. 접근자는 `value()`(record accessor)로 통일하고 `toString()` 오버라이드는 남기지 않는다.
- `@Embeddable`/`@Column` 어노테이션은 이 패키지에 없다 — 컬럼 매핑은 각 JpaEntity의 `@AttributeOverride`(복수 필드는 `@AttributeOverrides`)가 소유한다.

**ID VO**:
```java
public record OrderId(Long value) {

    public OrderId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("OrderId는 양수여야 합니다: " + value);
        }
    }

    public static OrderId of(Long value) {
        return new OrderId(value);
    }
}
```
JPA 매핑용 `AttributeConverter`(`OrderIdConverter`)는 infrastructure-module의 `order/persistence/`에 있다.

**DomainEvent (발행은 포트 경유, 리스너는 infra)**:
```java
// domain/<ctx>/event/XxxEvent.java (이 패키지 — record)
public record MemberRegisteredEvent(MemberId memberId, LocalDateTime registeredAt) { }

// domain/shared/event/DomainEventPublisher.java (이 패키지 — 출력 포트)
public interface DomainEventPublisher {
    void publish(Object event);
}

// infrastructure-module: shared/event/SpringDomainEventPublisher — ApplicationEventPublisher 위임
// infrastructure-module: <ctx>/listener/XxxListener — @TransactionalEventListener(AFTER_COMMIT)
```
리스너를 특정 api 모듈에 두면 다른 모듈이 같은 이벤트를 트리거할 때 누락되므로 반드시 infrastructure-module에 둔다.

**출력 포트 (외부 연동 모듈이 기술별로 나눠 구현)**:
```java
// domain/mail/port/MailSender.java        — infrastructure:messaging (JavaMailAdapter) / infrastructure:aws (SesMailSender)
// domain/sms/port/SmsSender.java          — infrastructure:messaging (SolapiSmsClient) / infrastructure:aws (SnsSmsSender)
// domain/file/port/FileStoragePort.java   — infrastructure:external (FileStoragePortAdapter → FileStorageStrategy 위임)
//                                           구현체는 infrastructure:firebase / infrastructure:aws
// domain/payment/port/PgPaymentGateway.java (+ port/dto/PgConfirmResult 등) — infrastructure:payment
```

**QueryDSL 동적 where 조립은 이 패키지 소관이 아니다**: `BooleanExpression` varargs 헬퍼 패턴은 QueryDSL을 소유한 `infrastructure-module`의 `<ctx>/query/{도메인}QueryDao` 규칙이다 — 상세와 reference(`notice/query/NoticeQueryDao`)는 `infrastructure-module/AGENTS.md` 참고.

## Dependencies

### Internal

없음 (innermost 계층)

### External

**production 의존 없음.** getter·생성자는 Lombok이 아니라 수기로 작성한다.

> JPA/Hibernate·QueryDSL·MySQL·Spring Data 의존은 이 모듈에 없다. 전부 `infrastructure-module`이 소유한다.

<!-- MANUAL: -->

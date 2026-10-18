<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# domain (DDD domain root)

## Purpose

DDD(Domain-Driven Design) 패턴으로 설계된 모든 Bounded Context가 거주하는 핵심 계층입니다. **프레임워크 의존이 전혀 없습니다** — Spring(Web/tx/orm)·JPA(`jakarta.persistence`)·QueryDSL(`com.querydsl`)을 import하지 않으며, 이 모듈의 production 의존은 **하나도 없습니다**(Lombok까지 제거되어 접근자·생성자를 수기로 작성합니다). 각 Bounded Context는 `<ctx>/{model,vo,event}` 구조를 가지며(write 포트·출력 포트·포트 주입 서비스는 `application`에 있고, 순수 계산기·정책은 `model`에 있다), 여기에 공유 커널(`shared/`)과 공통 예외(`exception/`)가 더해집니다.

## Key Files / Cross-cutting packages

| 패키지/파일 | 설명 |
|---|---|
| `shared/vo/PhoneNumber.java` | 공유 커널 Value Object. `record`(compact constructor 검증) — `@Embeddable` 어노테이션 없음, 컬럼 매핑은 jpa의 `PhoneNumberEmbeddable`·`@AttributeOverride`가 소유 |
| `shared/model/ApprovalStatus.java` | 승인 워크플로 공용 enum(PENDING/APPROVED/REJECTED). 상표·대표이미지 변경요청 등에서 재사용 |
| `shared/geo/` | `GeoPoint`·`GeoRing`·`GeoPolygon`·`GeoBoundingBox`·`InteriorPoint`·`PointInPolygon`·`GeoDistance`·`GeoPolygonTextCodec` 등 순수 도형 계산 |
| `exception/DomainErrorCode.java` | 도메인이 던지는 에러 코드 enum(153개, application도 함께 던지는 22개 포함). `httpStatusCode`(int)/`code`(String)/`defaultMessage`(String). Spring Web 비의존이므로 `HttpStatus` 대신 int 사용. 앱 계층 코드는 `application.shared.exception`의 `ApplicationErrorCode`·`{Web,Admin,Ceo,Batch}ErrorCode`가 소유한다 |
| `exception/DomainException.java` | `DomainErrorCode`를 받는 구체 예외(`BusinessException` 상속). domain 코드는 이것만 던진다 |
| `exception/BusinessException.java` | **abstract** 기본 비즈니스 예외(protected 생성자가 `ErrorCodeSpec`을 받는다). 직접 `new` 불가 — domain은 `DomainException`, application은 `ApplicationException`을 던진다. 전역 핸들러(`ErrorResponses.resolve`)는 `instanceof BusinessException`으로 판정한다 |
| `exception/ErrorCodeSpec.java` | 에러코드 공통 계약 인터페이스(`getHttpStatusCode`/`getCode`/`getDefaultMessage`). 구현체는 `DomainErrorCode`와 application 계층 5개 enum(`ApplicationErrorCodeSpec` 경유)이다. 외부 연동 실패 코드(SMS 발송·메일 발송·행정동 경계 조회 실패 등)는 던지는 계층의 enum(`WebErrorCode`·`BatchErrorCode` 등)이 소유한다 |

이 패키지에 없는 것: 페이징 계약 `PageQuery`·`PageResult`(→ `application`의 `shared/port/out/page/`), 이벤트 발행 포트 `DomainEventPublisher`와 구현 `SpringDomainEventPublisher`(→ `application`의 `shared/event/`), `OptimisticLockConflictException`(→ `application`의 `shared/port/out/`), `ResourceNotFoundException`(→ `application`의 `shared/exception/`, `ApplicationException` 상속).

> JPA 설정(`@EnableJpaRepositories`/`@EntityScan`/`@EnableJpaAuditing`/`@EnableTransactionManagement`)·`QueryDslConfig`·`BaseEntity`는 이 패키지에 없습니다. 전부 `infrastructure:jpa`(`JpaModuleConfig`·`config/QueryDslConfig`·`shared/persistence/BaseEntity`)가 소유합니다. 빈 등록도 이 패키지 소관이 아닙니다 — 이 패키지의 순수 계산기·정책은 `application-core`의 `shared/config/SharedBeanConfig`가 `@Bean`으로 등록하고, 포트를 주입받는 서비스는 `application`에서 `@Service`로 스캔 등록됩니다.

## Bounded Contexts

각 컨텍스트는 `<ctx>/` 아래에 `model`(애그리거트 순수 POJO + **포트를 주입받지 않는** 순수 계산기·정책·검증기와 그 입출력 record) / `vo`(ID VO 등) / `event`(DomainEvent record)를 갖습니다. 아래 표의 숫자는 해당 하위 패키지의 파일 수입니다(`member`는 `follow`·`referral` 하위 포함, 2026-10-13 실측).

| 도메인 | 목적 | model | vo | event |
|---|---|---|---|---|
| admin | 관리자 계정 | 3 | 1 | - |
| banner | 배너 관리 | 2 | 1 | - |
| bug | 버그 리포팅 | 6 | 1 | - |
| ceo | 점주 계정 (`admin`과 동형 최소 CRUD, `role` 없이 `status`만) | 6 | 2 | - |
| coupon | 쿠폰 발급/사용 | 4 | 2 | 2 |
| event | 이벤트/프로모션 (Event/Winner/Announcement 3 애그리거트) | 4 | 1 | - |
| faq | FAQ (Faq/FaqCategory) | 2 | 2 | - |
| file | 파일 업로드/관리 | 1 | 1 | 1 |
| holiday | 공휴일 마스터 | 1 | - | - |
| emailverification | 메일(이메일 주소) 인증 — 발급 시 발송까지 원자적 수행 | 3 | 1 | 1 |
| member | 회원 관리 (하위 `follow`·`referral` 포함). 다른 모든 BC가 `MemberId`로 참조하는 핵심 도메인 | 13 | 2 | 3 |
| menureview | 메뉴 평가(매장 리뷰와 독립된 축) | 1 | 1 | 3 |
| notice | 공지사항 (분리 패턴 reference 도메인) | 1 | 1 | - |
| notification | 알림 | 3 | 1 | - |
| order | 주문 (Order/OrderProduct/OrderProductOption) | 8 | 4 | - |
| partnership | 제휴 신청 | 2 | 1 | - |
| payment | 결제 (Payment/PaymentRefund/TossPaymentRecord) | 9 | 4 | 3 |
| point | 포인트 (Point/PointHistory) | 3 | - | 3 |
| policy | 정책/약관 버전 관리 | 2 | 1 | 1 |
| product | 상품·옵션 (8 애그리거트) | 50 | 13 | - |
| rank | 리뷰 랭킹/기간·상품 | 4 | 2 | - |
| region | 행정동 마스터·경계 | 1 | 1 | - |
| reservation | 시간대 예약 — `@Version` 낙관적 락 정원 관리 | 4 | 1 | - |
| review | 리뷰/댓글/답글/이미지/좋아요/태그 (6 애그리거트) | 15 | 5 | 4 |
| search | 검색어 (PopularKeyword/SearchKeywordLog) | 2 | - | - |
| shop | 가게/식당 + 자식 애그리거트 다수 — 최대 도메인 | 93 | 8 | - |
| phoneverification | SMS(휴대폰번호) 인증 — 발급 시 발송까지 원자적 수행 | 2 | 1 | 1 |

## For AI Agents

### Working In This Directory

**계층 의존 방향 규칙**:

```
presentation (web-api / admin-api / ceo-api / batch-module)
   · 컨트롤러·스케줄러 → port.in UseCase만 주입
        ↓
   application — <ctx>/port/in (UseCase) · <ctx>/service (유스케이스 서비스 · 포트 주입 도메인 서비스, @Service)
               · <ctx>/port/out/write (write 포트) · <ctx>/port/out (읽기 계약 · 출력 포트)
               · shared/config/SharedBeanConfig (domain 계산기 등록)
        ↓                                              ↑ 구현
   domain (이 패키지)                       infrastructure:jpa
   · model/vo/event                            · <ctx>/persistence (write 어댑터)
   · (순수 계산기·정책도 <ctx>/model)           · <ctx>/query (read: QueryAdapter — domain-free)
        ↑                                     ↑
   shared (kernel), exception            infrastructure:{firebase,aws-s3,aws-ses,aws-sns,
                                          kakao-oauth,naver-oauth,apple-oauth,facebook-oauth,
                                          tosspayments,javamail,solapi,bbq,admdongkor}
                                          (외부 연동 port 구현 — domain을 의존하지 않는다)
```

- **domain 계층에 프레임워크 import 금지**: `org.springframework.*`·`jakarta.persistence.*`·`com.querydsl.*`를 넣지 않는다. build.gradle에 해당 의존이 없으므로 시도하면 컴파일이 깨진다 — 그 관심사는 `infrastructure:jpa` 소관이다.
- **`@Entity`는 이 패키지에 없다**: 도메인 모델은 전 도메인 순수 POJO다. `@OneToMany`/`@ManyToOne`/`@ElementCollection`은 애초에 표현할 수 없으며, 외부 애그리거트 참조는 ID VO로만 한다.
- **BC 간 통신**: application 서비스 호출 또는 `DomainEventPublisher` 포트를 통한 DomainEvent로만 한다(다른 BC의 model 직접 조작 금지). 리스너는 `application-core`의 `<ctx>/listener/`에 둔다. 이 패키지 안에서는 컨텍스트 간 참조가 ID VO·이벤트 타입으로만 허용된다(`ContextBoundaryTest` — 서비스 간 경계는 `application`의 `ServiceContextBoundaryTest`).
- **포트를 주입받는 로직은 이 패키지에 두지 않는다**: write 포트·출력 포트·`DomainEventPublisher`를 주입받으면 `application/<ctx>/service/`, 아니면 이 패키지의 `<ctx>/model/`이다. `DomainPurityTest#domainShouldNotHaveServicePackage`가 이 패키지에 `service` 패키지가 생기는 것을 막는다. 근거는 `backend/domain/AGENTS.md`의 "컨텍스트 하위 패키지는 `model`·`vo`·`event` 셋이다" 절.
- **표현 목적 조회는 이 패키지에 두지 않는다**: write 포트(`application/<ctx>/port/out/write/`)에는 `findById`/`save`/`saveImmediately`/`delete`/`existsByX`/`findByNaturalKey`/검증용 `countByX`/락 획득용 조회만 둔다. Result DTO·`PageResult` 반환·조인 투영·목록·검색·페이징은 `application`의 `{Ctx}QueryPort`와 그 구현 `infrastructure:jpa`의 `<ctx>/query/{도메인}QueryAdapter`가 소유한다.

**ID 참조 규칙**:
- 외부 BC의 애그리거트는 ID VO로만 참조한다(예: `Order.memberId : MemberId`, `Payment.orderId : OrderId`).
- `XxxId`는 `record XxxId(Long value)` + compact constructor 검증 + 정적 팩토리 `of(Long)`. `new`는 `of()` 내부에만 남긴다. JPA 엔티티 FK는 raw `Long`이고 `AttributeConverter`는 없다 — `XxxId.of` 승격은 jpa `<ctx>/persistence/XxxMapper`가 한다.

**순수 계산기·정책 규칙 (`<ctx>/model/`)**:
- `@Service`/`@Component`/`@Transactional`을 붙이지 않는 **순수 POJO**다. 빈 등록은 `application-core`의 `shared/config/SharedBeanConfig`가 `@Bean` 팩토리로 수행하고, 트랜잭션 경계는 이를 호출하는 유스케이스 서비스가 소유한다.
- 소비 모듈로 복제하지 않는다 — 특정 앱 모듈에 두면 다른 앱이 같은 계산을 할 때 규칙이 갈린다.

**DTO 규칙**:
- 이 패키지는 조회 결과 DTO를 갖지 않는다. Result record와 `SearchCondition`은 `application`의 `com.tastyhouse.application.<ctx>.port.out` 소유이며, 접미어 `Result` 통일(`Dto` 금지)·admin 충돌 시 `Management` 한정어 규칙이 그 위치에서 적용된다.
- 도메인 모델은 presentation의 Request 타입을 파라미터로 받지 않는다(레이어 역전 방지). HTTP 경계에서 `String`/`Long`으로 받아 application 서비스가 `Enum.from(String)`·`XxxId.of(Long)`으로 승격한 뒤 넘긴다.
- `record`는 다른 클래스 본문 안에 중첩 선언하지 않고 독립 `.java`로 둔다(`vo/`, `event/`, `model/`).

### Testing Requirements

- **순수 단위 테스트**(`domain/src/test`)로 불변식·상태전이를 검증한다 — 스프링 컨텍스트·DB 불필요.
- 새 애그리거트·상태전이 추가 시 대응 `XxxTest`를 함께 추가한다(reference: `notice/model/NoticeTest`).
- `ddl-auto=validate` 스키마 정합성·enum `columnDefinition` 검증은 JPA 엔티티를 소유한 `infrastructure:jpa` 책임이다.
- 레이어 경계 회귀는 각 모듈의 `architecture/LayerRulesTest`(ArchUnit)와 이 모듈의 `DomainPurityTest`·`ContextBoundaryTest`가 지킨다.

### Common Patterns

**write 포트 + load-copy-save**:
```java
// application/banner/port/out/write/BannerLoadPort.java (조회 — 인터페이스만)
public interface BannerLoadPort {
    Optional<Banner> findActiveById(BannerId id);
}

// application/banner/port/out/write/BannerSavePort.java (변경 — 삭제도 Save에 둔다)
public interface BannerSavePort {
    Banner save(Banner banner);
}

// infrastructure:jpa: <ctx>/persistence/BannerPersistenceAdapter (@Repository, BannerLoadPort·BannerSavePort 구현)
//  - save: id null이면 insert, 있으면 managed 엔티티 조회 후 Mapper.applyChanges 복사
//  - detached merge 금지(@CreatedDate(updatable=false) 감사 필드 파손 방지)
```

같은 포트를 두 기술(JPA·MyBatis)이 구현하는 예외 사례(notice)의 이름 규칙은 `backend/CLAUDE.md`의 "아웃바운드 포트·어댑터 네이밍 규칙" 예외 항목에 있다.

**순수 도메인 모델 (팩토리 2종 + final 필드)**:
```java
public class Notice {

    private final Long id;          // 미영속이면 null
    private String title;           // 상태전이로 재대입되므로 non-final

    // 신규 생성 — 불변식 검증
    public static Notice of(String title, String content, boolean visible) { ... }

    // DB 재구성 전용 — 인프라(매퍼)만 호출한다. 불변식 우회 방지.
    public static Notice reconstitute(Long id, ..., LocalDateTime createdAt, LocalDateTime updatedAt) { ... }
}
```
- 상태전이로 재대입되지 않는 필드는 `final`로 선언한다(reference: `admin/model/Admin` — update 경로가 없어 전 필드 `final`).
- **명시적 save 필수**: POJO는 JPA 더티 체킹으로 자동 flush되지 않으므로 변경 후 반드시 write 포트의 `save(domain)`을 호출한다. 누락하면 변경이 조용히 유실된다.

**VO는 `record`**:
```java
public record PhoneNumber(String value) {

    public PhoneNumber {
        // 검증은 compact constructor에서
    }
}
```
- 접근자는 `value()`(record accessor)로 통일하고 `toString()` 오버라이드는 남기지 않는다.
- `@Embeddable`/`@Column` 어노테이션은 이 패키지에 없다 — jpa 엔티티는 자기 소유 `XxxEmbeddable` record를 매핑하고 컬럼 매핑(`@AttributeOverride`)도 jpa가 소유한다. 상세는 `backend/domain/AGENTS.md`의 "도메인 모델 규칙".

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

**DomainEvent (발행은 포트 경유, 리스너는 application-core)**:
```java
// domain/<ctx>/event/XxxEvent.java (이 패키지 — record)
public record MemberRegisteredEvent(MemberId memberId, LocalDateTime registeredAt) { }

// application/shared/event/DomainEventPublisher.java (발행 포트)
public interface DomainEventPublisher {
    void publish(Object event);
}

// application-core: shared/event/SpringDomainEventPublisher — ApplicationEventPublisher 위임, SharedBeanConfig가 @Bean 등록
// application-core: <ctx>/listener/XxxListener — @Component + @TransactionalEventListener(AFTER_COMMIT)
```
리스너를 특정 api 모듈이나 앱 모듈(`{앱}-application`)에 두면 다른 앱이 같은 이벤트를 트리거할 때 누락되므로 4앱 전부의 클래스패스에 있는 `application-core`에 둔다.

**출력 포트는 이 패키지에 없다**: 아웃바운드 SPI는 `com.tastyhouse.application.<ctx>.port.out`이 소유한다.

```java
// application/emailverification/port/out/MailSenderPort.java        — infrastructure:javamail (JavaMailAdapter) / infrastructure:aws-ses (SesMailSender), 조립은 infrastructure:mail
// application/phoneverification/port/out/SmsSenderPort.java          — infrastructure:solapi (SolapiSmsClient) / infrastructure:aws-sns (SnsSmsSender), 조립은 infrastructure:sms
// application/file/port/out/FileStoragePort.java   — infrastructure:firebase (FirebaseFileStorage) / infrastructure:aws-s3 (S3FileStorage) — file.provider 배타 선택
// application/payment/port/out/PgPaymentGatewayPort.java — application-core의 라우터 PgPaymentGatewayRouter(@Service)가 구현
// application/payment/port/out/PgProviderGatewayPort.java (벤더 SPI) — infrastructure:tosspayments의 TossPaymentGatewayAdapter(provider() = PgProviderCode.TOSS)가 구현, 조립은 infrastructure:pg
```

`mail`/`sms`/`file`/`payment`의 포트 주입 서비스도 `application`에 있다 — `MailVerificationService`·`SmsVerificationService`는 `web-application`, `FileUploadService`·`PaymentConfirmationService`는 `application-core`(`backend/application-core/AGENTS.md` 참고). 벤더 SPI `PgProviderGatewayPort`는 domain `PgProvider`가 아니라 application enum `PgProviderCode`를 쓰고, 라우터가 `name()` 기반으로 domain `PgProvider`로 변환한다(`application`의 `EnumCodeConstantsTest`가 검증).

**QueryDSL 동적 where 조립은 이 패키지 소관이 아니다**: `BooleanExpression` varargs 헬퍼 패턴은 QueryDSL을 소유한 `infrastructure:jpa`의 `<ctx>/query/{도메인}QueryAdapter` 규칙이다 — 상세와 reference(`notice/query/NoticeJpaQueryAdapter`)는 `infrastructure/jpa/AGENTS.md` 참고.

## Dependencies

### Internal

없음 (innermost 계층)

### External

**production 의존 없음.** getter·생성자는 Lombok이 아니라 수기로 작성한다.

> JPA/Hibernate·QueryDSL·MySQL·Spring Data 의존은 이 모듈에 없다. 전부 `infrastructure:jpa`·`infrastructure:mysql`이 소유한다.

<!-- MANUAL: -->

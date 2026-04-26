# DDD 패턴 전환 가이드

> 작성일: 2026-04-26
> 대상 모듈: core-module, web-api, admin-api, external-api
> 전환 방식: 점진적 전환 (Strangler Fig)

---

## 목차

- [1. 배경 및 목표](#1-배경-및-목표)
- [2. 확정 시나리오 요약](#2-확정-시나리오-요약)
- [3. 새 패키지 구조](#3-새-패키지-구조)
- [4. 도메인 전환 우선순위](#4-도메인-전환-우선순위)
- [5. 도메인 내부 레이어 규칙](#5-도메인-내부-레이어-규칙)
- [6. DDD 빌딩블록 도입 가이드](#6-ddd-빌딩블록-도입-가이드)
- [7. Verification 파일럿 단계별 작업](#7-verification-파일럿-단계별-작업)
- [8. *CoreService 재배치 매핑](#8-coreservice-재배치-매핑)
- [9. ArchUnit 의존성 검증](#9-archunit-의존성-검증)
- [10. 위험 요소 및 마이그레이션 주의사항](#10-위험-요소-및-마이그레이션-주의사항)
- [11. 검증 체크리스트](#11-검증-체크리스트)

---

## 1. 배경 및 목표

### 현재 상태
- Spring Boot 3.2.4 / Java 21 기반 Gradle 멀티모듈
- `core-module`에 27개 도메인 엔티티, 19개 Repository, 24개 `*CoreService` 집중
- 전통적 계층형 아키텍처 (entity → repository → service → controller)
- 비즈니스 로직이 service 레이어에 응집된 **Anemic Domain Model**
- DDD 빌딩블록은 `PhoneNumber` ValueObject 하나만 존재

### 전환 목표
- **Rich Domain Model**: 비즈니스 규칙을 도메인 객체에 캡슐화
- **Bounded Context 명확화**: 도메인 간 결합 추적/제어
- **DIP 적용**: 결제·외부연동처럼 변경이 잦은 영역 의존 역전
- **점진적 전환**: 빅뱅 없이 회귀 위험 최소화

---

## 2. 확정 시나리오 요약

| 축              | 결정                                                              | 이유                                          |
| --------------- | ----------------------------------------------------------------- | --------------------------------------------- |
| 모듈 구조       | 멀티모듈 유지 + core-module 내부 패키지 재편                      | 빌드/CI 무변경, 작업량 최소                   |
| 전환 전략       | Strangler Fig (도메인 1개씩 점진 전환)                            | 매 단계 배포 가능, 회귀 분산                  |
| 첫 파일럿       | **Verification** 도메인                                           | 외부 의존 명확, 결합도 낮음                   |
| AR 외부 참조    | **ID 참조** (`Order.memberId: MemberId`)                          | 정통 DDD, BC 경계 명확                        |
| 내부 레이어     | `domain` / `application` / `infrastructure` (3-layer)             | presentation은 web-api/admin-api 유지         |
| 부분 Hexagonal  | Payment, File, Verification (외부 어댑터 명확한 BC)               | OutPort + Adapter로 외부 의존 격리            |

---

## 3. 새 패키지 구조

```
core-module/com/tastyhouse/core
├── common/                  (BaseEntity, 공용 예외 - 그대로 유지)
├── shared/                  (kernel: Money, Address 등 BC 횡단 VO)
│   └── vo/
│       └── PhoneNumber      (기존 entity/common/vo 에서 승격)
└── domain/
    ├── verification/        ← 1차 파일럿
    │   ├── domain/
    │   │   ├── model/       EmailVerification, PhoneVerification
    │   │   ├── vo/          VerificationCode, VerificationToken
    │   │   ├── event/       EmailVerifiedEvent, PhoneVerifiedEvent
    │   │   └── repository/  EmailVerificationRepository (interface)
    │   ├── application/
    │   │   ├── EmailVerificationCommandService
    │   │   ├── EmailVerificationQueryService
    │   │   ├── port/out/    MailSender, SmsSender (interface)
    │   │   └── dto/         command, result
    │   └── infrastructure/
    │       └── persistence/ EmailVerificationJpaRepository, EmailVerificationRepositoryImpl
    │
    ├── member/
    │   ├── domain/
    │   │   ├── model/       Member(AggregateRoot), MemberSocialAccount, MemberWithdrawal
    │   │   ├── vo/          MemberId, Email, Nickname, Birthday
    │   │   ├── event/       MemberRegisteredEvent, MemberWithdrawnEvent
    │   │   └── repository/  MemberRepository (interface)
    │   ├── application/     MemberCommandService, MemberQueryService
    │   └── infrastructure/  persistence (JPA + QueryDSL)
    │
    ├── place/   (17 엔티티 → Place AR + 내부 엔티티/VO 정리)
    ├── product/
    ├── order/
    ├── payment/             ← Hexagonal 적용 (port/in, port/out, adapter/in, adapter/out)
    ├── review/
    ├── coupon/  point/  rank/  file/
    └── support/             (notice, event, banner, policy, faq, follow, referral, report, partnership)

web-api/com/tastyhouse/webapi
├── verification/controller, dto         (presentation - 도메인별 폴더 정리)
├── member/controller, dto
├── ...
└── config, auth, scheduler, ratelimit, logging   (그대로 유지)

external-api/com/tastyhouse/external
├── email/                   ← MailSender 어댑터 구현
├── sms/                     ← SmsSender 어댑터 구현
├── payment/toss/            ← PaymentGateway 어댑터 구현
└── file/                    ← FileStorage 어댑터 구현
```

---

## 4. 도메인 전환 우선순위

| 순서 | 도메인       | 선정 이유                                              |
| ---: | ------------ | ------------------------------------------------------ |
|    1 | Verification | 가장 단순, 외부 의존 명확 → 패턴 정착 파일럿           |
|    2 | Member       | 다른 도메인이 모두 참조 → 안정화 먼저                  |
|    3 | Place        | 17개 엔티티 → AR 분리 연습                             |
|    4 | Product      | Order 의존 정착                                        |
|    5 | Order        | Member·Place·Product 정착 후                           |
|    6 | Payment      | Hexagonal 본격 적용, Order 안정화 후                   |
|    7 | Review / Coupon / Point·Rank | 부가 BC                                |
|    8 | support 통합 | Notice/Event/Banner/Policy/Faq/Follow/Referral/Report/Partnership |
|    9 | File         | CDN/스토리지 추상화                                    |

**1 BC = 3개 PR 분할 원칙** (PR 거대화 방지):
- **PR-1**: 패키지 이동 (mechanical move only — 동작 변경 없음)
- **PR-2**: AR / VO 도입 (캡슐화 메서드, 식별자 강타입, ID 참조 전환)
- **PR-3**: DomainEvent 도입 + ArchUnit 룰 강화

---

## 5. 도메인 내부 레이어 규칙

### 5.1 의존 방향

```
presentation(web-api, admin-api)
        ↓
   application
        ↓                    ↑
     domain  ←──── infrastructure (구현체 주입, DIP)
        ↑
     shared (kernel)
```

### 5.2 레이어별 책임

| 레이어              | 위치                                       | 책임                                      | 금지사항                                          |
| ------------------- | ------------------------------------------ | ----------------------------------------- | ------------------------------------------------- |
| **domain**          | `core/domain/<bc>/domain/`                 | AR, Entity, VO, DomainEvent, Repository(interface) | Spring/JPA import 금지 (단, `@Entity`는 과도기 허용) |
| **application**     | `core/domain/<bc>/application/`            | UseCase 조율, 트랜잭션 경계, DTO 변환     | 다른 BC의 infrastructure 직접 참조 금지            |
| **infrastructure**  | `core/domain/<bc>/infrastructure/`         | Repository 구현, 외부 어댑터              | 다른 BC의 application/infrastructure 참조 금지    |
| **presentation**    | `web-api/`, `admin-api/`                   | Controller, Request/Response DTO          | application 레이어만 호출                         |

### 5.3 BC 횡단 규칙

```
✅ allowed: domain.member.application → domain.verification.application (out port)
❌ forbidden: domain.member.domain → domain.verification.infrastructure
❌ forbidden: domain.member.application → domain.verification.infrastructure
```

**원칙**: BC 간 통신은 application 레이어 또는 DomainEvent로만.

---

## 6. DDD 빌딩블록 도입 가이드

### 6.1 ValueObject (VO)

#### 식별자 강타입화 (1단계)

**Before**
```java
public Member findById(Long memberId) { ... }
public void deleteOrder(Long orderId, Long memberId) { ... }
// 호출 시: deleteOrder(memberId, orderId);  ← 실수 가능
```

**After**
```java
public record MemberId(Long value) {
    public MemberId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid MemberId");
        }
    }
}

public Member findById(MemberId memberId) { ... }
public void deleteOrder(OrderId orderId, MemberId memberId) { ... }
// 호출 시: deleteOrder(memberId, orderId);  ← 컴파일 에러
```

#### 비즈니스 의미가 있는 VO (2단계)

```java
@Embeddable
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount.signum() < 0) throw new IllegalArgumentException("Money cannot be negative");
    }

    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(amount.add(other.amount), currency);
    }
}
```

JPA 매핑이 필요한 VO는 `@Embeddable` + `AttributeConverter` 활용.

### 6.2 Aggregate Root (AR)

#### AR 후보

| AR        | 포함 엔티티/VO                                  |
| --------- | ----------------------------------------------- |
| `Member`  | MemberSocialAccount, Email, Nickname            |
| `Place`   | PlaceBusinessHour, PlaceBreakTime, PlacePhoto, PlaceMenu |
| `Order`   | OrderItem, OrderItemOption                      |
| `Payment` | PaymentRefund, TossPaymentRecord                |
| `Review`  | ReviewComment, ReviewLike, ReviewImage          |

#### 외부 참조는 ID로

**Before (양방향 연관관계)**
```java
@Entity
public class Order {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  // ← 객체 참조

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;
}
```

**After (ID 참조)**
```java
@Entity
public class Order {
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "member_id"))
    private MemberId memberId;  // ← ID만 참조

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "place_id"))
    private PlaceId placeId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();  // ← AR 내부 엔티티는 객체 참조 OK
}
```

#### AR 캡슐화 메서드

**Before (Anemic)**
```java
// Service에서 setter로 상태 변경
member.setStatus(MemberStatus.WITHDRAWN);
member.setWithdrawnAt(LocalDateTime.now());
member.setNickname(null);
member.setEmail(null);
memberRepository.save(member);
```

**After (Rich)**
```java
// Member AR 내부에 메서드
public class Member {
    public void withdraw(WithdrawalReason reason, LocalDateTime now) {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.ALREADY_WITHDRAWN);
        }
        this.status = MemberStatus.WITHDRAWN;
        this.withdrawnAt = now;
        this.email = null;
        this.nickname = null;
        registerEvent(new MemberWithdrawnEvent(this.id, reason, now));
    }
}

// Application 서비스
public void withdraw(MemberId memberId, WithdrawalReason reason) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    member.withdraw(reason, LocalDateTime.now());
    memberRepository.save(member);
}
```

### 6.3 DomainEvent

#### Spring AbstractAggregateRoot 활용

```java
@Entity
public class Member extends AbstractAggregateRoot<Member> {

    public void withdraw(WithdrawalReason reason, LocalDateTime now) {
        // ... 상태 변경 ...
        registerEvent(new MemberWithdrawnEvent(this.id, reason, now));
    }
}
```

#### 이벤트 리스너

```java
@Component
@RequiredArgsConstructor
public class MemberWithdrawnEventListener {

    private final PointApplicationService pointService;
    private final CouponApplicationService couponService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MemberWithdrawnEvent event) {
        pointService.expireAll(event.memberId());
        couponService.expireAll(event.memberId());
    }
}
```

**원칙**:
- 초기엔 동기 `@TransactionalEventListener(AFTER_COMMIT)`
- 점진적으로 비동기 + Outbox 패턴 도입 (특히 Payment)

---

## 7. Verification 파일럿 단계별 작업

### 7.1 대상 코드

| 분류            | 현재 위치                                                         |
| --------------- | ----------------------------------------------------------------- |
| Entity          | `core/entity/verification/EmailVerification`, `PhoneVerification` |
| Status enum     | `core/entity/verification/EmailVerificationStatus`, `PhoneVerificationStatus` |
| Repository      | `core/repository/verification/`                                   |
| Service         | `core/service/EmailVerificationCoreService`, `PhoneVerificationCoreService` |
| 외부 어댑터     | `external-api/email/ses`, `external-api/email/javamail`, `external-api/sms/sns`, `external-api/sms/solapi` |
| Controller      | `web-api/controller/verification/VerificationApiController`       |

### 7.2 PR-1: 패키지 이동 (mechanical)

**목표**: 동작 변경 없이 신규 패키지 구조로 이동.

**작업 단계**:

1. 신규 패키지 생성
   ```
   core-module/src/main/java/com/tastyhouse/core/domain/verification/
   ├── domain/model/         ← 기존 entity 이동
   ├── domain/repository/    ← 인터페이스 추출
   ├── application/          ← *CoreService 분할 후 이동
   ├── application/port/out/ ← MailSender, SmsSender 인터페이스 신규
   └── infrastructure/persistence/  ← JPA Repository 구현
   ```

2. Repository 인터페이스/구현 분리
   ```java
   // domain/repository/EmailVerificationRepository.java (interface)
   public interface EmailVerificationRepository {
       Optional<EmailVerification> findById(EmailVerificationId id);
       Optional<EmailVerification> findLatestByEmail(Email email);
       EmailVerification save(EmailVerification verification);
   }

   // infrastructure/persistence/EmailVerificationJpaRepository.java
   public interface EmailVerificationJpaRepository
       extends JpaRepository<EmailVerification, Long> {
       Optional<EmailVerification> findFirstByEmailOrderByCreatedAtDesc(String email);
   }

   // infrastructure/persistence/EmailVerificationRepositoryImpl.java
   @Repository
   @RequiredArgsConstructor
   public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {
       private final EmailVerificationJpaRepository jpaRepository;

       @Override
       public Optional<EmailVerification> findById(EmailVerificationId id) {
           return jpaRepository.findById(id.value());
       }
       // ...
   }
   ```

3. CoreService → CommandService/QueryService 분할
   ```java
   // application/EmailVerificationCommandService.java
   @Service
   @Transactional
   @RequiredArgsConstructor
   public class EmailVerificationCommandService {
       private final EmailVerificationRepository repository;
       private final MailSender mailSender;  // out port

       public EmailVerificationResult requestVerification(RequestVerificationCommand cmd) {
           EmailVerification verification = EmailVerification.create(cmd.email());
           repository.save(verification);
           mailSender.send(verification.getEmail(), verification.getCode());
           return EmailVerificationResult.from(verification);
       }
   }

   // application/EmailVerificationQueryService.java
   @Service
   @Transactional(readOnly = true)
   @RequiredArgsConstructor
   public class EmailVerificationQueryService {
       private final EmailVerificationRepository repository;

       public EmailVerificationResult findById(EmailVerificationId id) {
           return repository.findById(id)
               .map(EmailVerificationResult::from)
               .orElseThrow(() -> new EntityNotFoundException(ErrorCode.VERIFICATION_NOT_FOUND));
       }
   }
   ```

4. 외부 어댑터 (out port 구현)
   ```java
   // core-module: application/port/out/MailSender.java (interface)
   public interface MailSender {
       void send(Email to, VerificationCode code);
   }

   // external-api: email/ses/SesMailSenderAdapter.java
   @Component
   @RequiredArgsConstructor
   public class SesMailSenderAdapter implements MailSender {
       private final AmazonSimpleEmailService sesClient;

       @Override
       public void send(Email to, VerificationCode code) {
           // SES 호출
       }
   }
   ```

5. Controller 호출 변경
   ```java
   // VerificationApiController
   // Before
   private final EmailVerificationCoreService emailVerificationCoreService;

   // After
   private final EmailVerificationCommandService emailVerificationCommandService;
   private final EmailVerificationQueryService emailVerificationQueryService;
   ```

6. 기존 `*CoreService` 삭제 (위임 후 안전 확인 시).

### 7.3 PR-2: AR / VO 도입

```java
// domain/vo/VerificationCode.java
public record VerificationCode(String value) {
    private static final Pattern PATTERN = Pattern.compile("^\\d{6}$");

    public VerificationCode {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Verification code must be 6 digits");
        }
    }

    public static VerificationCode generate() {
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return new VerificationCode(String.valueOf(code));
    }
}

// domain/vo/EmailVerificationId.java
public record EmailVerificationId(Long value) { }

// domain/model/EmailVerification.java (AR)
@Entity
public class EmailVerification extends AbstractAggregateRoot<EmailVerification> {

    @Id @GeneratedValue
    private Long id;

    @Embedded
    private Email email;

    @Embedded
    private VerificationCode code;

    @Enumerated(EnumType.STRING)
    private EmailVerificationStatus status;

    private LocalDateTime expiresAt;

    public static EmailVerification create(Email email) {
        EmailVerification v = new EmailVerification();
        v.email = email;
        v.code = VerificationCode.generate();
        v.status = EmailVerificationStatus.PENDING;
        v.expiresAt = LocalDateTime.now().plusMinutes(5);
        return v;
    }

    public void verify(VerificationCode input, LocalDateTime now) {
        if (this.status != EmailVerificationStatus.PENDING) {
            throw new BusinessException(ErrorCode.VERIFICATION_ALREADY_USED);
        }
        if (now.isAfter(this.expiresAt)) {
            this.status = EmailVerificationStatus.EXPIRED;
            throw new BusinessException(ErrorCode.VERIFICATION_EXPIRED);
        }
        if (!this.code.equals(input)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }
        this.status = EmailVerificationStatus.VERIFIED;
        registerEvent(new EmailVerifiedEvent(new EmailVerificationId(this.id), this.email, now));
    }
}
```

### 7.4 PR-3: DomainEvent + ArchUnit

```java
// domain/event/EmailVerifiedEvent.java
public record EmailVerifiedEvent(
    EmailVerificationId verificationId,
    Email email,
    LocalDateTime verifiedAt
) {}

// 다른 BC(Member)에서 리스너 등록 (signup 흐름과 디커플링)
@Component
@RequiredArgsConstructor
public class MemberSignupEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(EmailVerifiedEvent event) {
        // signup 흐름에서 활용
    }
}
```

ArchUnit 룰 추가:
```java
// core-module/src/test/java/com/tastyhouse/core/architecture/VerificationArchitectureTest.java
@AnalyzeClasses(packagesOf = TastyhouseCoreApplication.class)
public class VerificationArchitectureTest {

    @ArchTest
    static final ArchRule domainShouldNotDependOnSpring = noClasses()
        .that().resideInAPackage("..domain.verification.domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("org.springframework..");

    @ArchTest
    static final ArchRule verificationShouldNotDependOnOtherBcInfrastructure = noClasses()
        .that().resideInAPackage("..domain.verification..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..domain.member.infrastructure..", "..domain.payment.infrastructure..");
}
```

---

## 8. *CoreService 재배치 매핑

| 기존                                                           | 이동 후                                                                                                |
| -------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| `EmailVerificationCoreService` / `PhoneVerificationCoreService` | `application/EmailVerificationCommandService` + `QueryService` + out port `MailSender`/`SmsSender`     |
| `MemberCoreService` (write+read 혼재)                           | `application/MemberCommandService` + `MemberQueryService` 분리 (CQS). 단순 위임은 AR 메서드로 흡수      |
| `MemberWithdrawalCoreService`                                   | `MemberWithdrawalUseCase` (application) + `MemberWithdrawnEvent` 발행                                   |
| `MemberSocialAccountCoreService`                                | `Member` AR 내부 메서드 + application 협력                                                              |
| `RankAggregationService`                                        | application 레이어 도메인 서비스 (여러 AR 협력 — Domain Service)                                        |
| `PaymentCoreService`                                            | `application/port/in/PaymentUseCase` + `out/PaymentGateway` + `out/PaymentRepository`. Toss 어댑터는 external-api로 |
| `FileCoreService`                                               | application + out port `FileStorage` (S3/Firebase 어댑터는 external-api)                                |
| 그 외 *CoreService                                              | `<bc>/application/<Bc>CommandService` + `<Bc>QueryService` 패턴 적용                                    |

---

## 9. ArchUnit 의존성 검증

### 9.1 의존성 추가

```gradle
// core-module/build.gradle
dependencies {
    testImplementation 'com.tngtech.archunit:archunit-junit5:1.2.1'
}
```

### 9.2 공통 룰 (점진 강화)

```java
@AnalyzeClasses(packagesOf = TastyhouseCoreApplication.class)
public class CommonArchitectureTest {

    // 도메인 레이어는 인프라/프레임워크에 의존하지 않는다
    @ArchTest
    static final ArchRule domainShouldNotDependOnInfrastructure =
        noClasses()
            .that().resideInAPackage("..core.domain.(*).domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..core.domain.(*).infrastructure..");

    // application은 다른 BC의 infrastructure에 의존하지 않는다
    @ArchTest
    static final ArchRule applicationShouldNotCrossBcInfrastructure =
        slices().matching("..core.domain.(*)..")
            .should().notDependOnEachOther()
            .ignoreDependency(
                resideInAPackage("..core.domain.(*).application.."),
                resideInAPackage("..core.shared..")
            );

    // shared는 도메인에 의존하지 않는다
    @ArchTest
    static final ArchRule sharedShouldNotDependOnDomain =
        noClasses()
            .that().resideInAPackage("..core.shared..")
            .should().dependOnClassesThat()
            .resideInAPackage("..core.domain..");
}
```

### 9.3 BC 단위 강화 (전환 완료 BC부터)

```java
// VerificationArchitectureTest, MemberArchitectureTest 등 BC별 추가
@ArchTest
static final ArchRule verificationDomainShouldBePure =
    noClasses()
        .that().resideInAPackage("..core.domain.verification.domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage(
            "jakarta.persistence..",   // @Entity 분리 후
            "org.springframework..",
            "com.querydsl.."
        );
```

---

## 10. 위험 요소 및 마이그레이션 주의사항

| #   | 위험                                                         | 대응                                                                  |
| --- | ------------------------------------------------------------ | --------------------------------------------------------------------- |
| 1   | JPA 양방향 연관관계 절단 시 N+1, 페치 조인 쿼리 영향         | PR-1은 패키지 이동만, PR-2에서 ID 참조로 단계적 전환                  |
| 2   | QueryDSL Q클래스 경로 변경                                   | gradle querydsl generated 경로 확인, 빌드 캐시 무효화 (`./gradlew clean`) |
| 3   | 순환 의존 (Member↔Review, Place↔Review)                      | Domain Service(application 레이어)에서 조율, AR 직접 참조 금지        |
| 4   | 트랜잭션 경계 변화로 데이터 일관성 깨짐                      | DomainEvent + Outbox 패턴 (Payment부터). 초기엔 `AFTER_COMMIT` 동기   |
| 5   | DTO 위치 혼재 (`entity/place/dto/`)                          | read model로 분류해 `application/dto/`로 이동, native projection은 infrastructure |
| 6   | admin-api 호환 깨짐                                           | admin도 동일 application 서비스 사용 → command/query 변경 시 동시 수정 PR |
| 7   | 스키마 무변경 보장                                           | `@Table(name=...)` 명시 점검, `hibernate.ddl-auto=validate` 적용     |
| 8   | `@Entity` 클래스 패키지 이동 시 직렬화/캐시 충돌             | Redis 캐시 키에 FQCN 포함 여부 확인, 배포 전 캐시 비우기              |
| 9   | 외부 어댑터 이동 시 빈 충돌                                  | 동일 인터페이스 빈이 여러 개일 경우 `@Primary` 또는 `@Qualifier` 명시 |
| 10  | 트랜잭션 전파 변경으로 readOnly 누락                          | `QueryService`는 항상 `@Transactional(readOnly = true)`               |

---

## 11. 검증 체크리스트

각 BC 전환 PR마다 아래 항목을 모두 통과해야 머지.

### 11.1 정적 검증

- [ ] 컴파일 그린
- [ ] ArchUnit 룰 통과 (해당 BC 룰 + 공통 룰)
- [ ] `hibernate.ddl-auto=validate`로 스키마 무변경 확인
- [ ] QueryDSL Q클래스 정상 생성 (`./gradlew clean compileJava`)

### 11.2 회귀 테스트

- [ ] 기존 단위 테스트 그린
- [ ] `@SpringBootTest` 통합 테스트 그린
- [ ] OpenAPI 응답 JSON 스냅샷 동일성 (필드 추가/삭제 없음)
- [ ] 핵심 시나리오 e2e (회원가입/주문/결제/리뷰 흐름)

### 11.3 성능 회귀

- [ ] QueryDSL 핵심 쿼리 explain plan 비교 (인덱스 사용 동일)
- [ ] 응답시간 측정 (주요 API: 회원조회, 주문조회, 가게리스트)

### 11.4 운영 안정성

- [ ] 로컬 도커 컴포즈로 실행 후 주요 시나리오 수동 확인
- [ ] Redis 캐시 키 변경 여부 확인 (FQCN 포함 시 비우기)
- [ ] 배포 후 1시간 모니터링 (에러율, 응답시간)

---

## 부록 A: Verification BC PR-1 체크리스트 (실행용)

신규 패키지 생성:
- [ ] `core/domain/verification/domain/model/`
- [ ] `core/domain/verification/domain/vo/`
- [ ] `core/domain/verification/domain/event/`
- [ ] `core/domain/verification/domain/repository/`
- [ ] `core/domain/verification/application/`
- [ ] `core/domain/verification/application/port/out/`
- [ ] `core/domain/verification/infrastructure/persistence/`

파일 이동:
- [ ] `entity/verification/EmailVerification` → `domain/verification/domain/model/`
- [ ] `entity/verification/PhoneVerification` → `domain/verification/domain/model/`
- [ ] `entity/verification/*VerificationStatus` → `domain/verification/domain/model/`
- [ ] `repository/verification/*JpaRepository` → `domain/verification/infrastructure/persistence/`

신규 작성:
- [ ] `MailSender`, `SmsSender` (out port 인터페이스)
- [ ] `EmailVerificationRepository`, `PhoneVerificationRepository` (도메인 인터페이스)
- [ ] `*RepositoryImpl` (infrastructure 구현)
- [ ] `EmailVerificationCommandService`, `EmailVerificationQueryService`
- [ ] `PhoneVerificationCommandService`, `PhoneVerificationQueryService`
- [ ] `external-api`에 `SesMailSenderAdapter`, `JavaMailSenderAdapter`, `SnsSmsAdapter`, `SolapiSmsAdapter`

호출 변경:
- [ ] `VerificationApiController` → application 서비스 호출로 변경
- [ ] 기타 *CoreService 사용처 grep 후 일괄 변경

정리:
- [ ] 기존 `EmailVerificationCoreService`, `PhoneVerificationCoreService` 삭제
- [ ] 기존 `entity/verification/`, `repository/verification/` 빈 디렉토리 정리
- [ ] 기존 `external-api/email/`, `external-api/sms/`의 직접 호출 코드 정리

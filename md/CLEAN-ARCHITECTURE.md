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
- [9. 위험 요소 및 마이그레이션 주의사항](#9-위험-요소-및-마이그레이션-주의사항)
- [10. 검증 체크리스트](#10-검증-체크리스트)

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
    │   │   ├── service/     MailSender, SmsSender (interface)
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
    ├── place/   (17 엔티티 → Place AR + 연관 엔티티 별도 Repository 분리 / VO 정리)
    ├── product/
    ├── order/
    ├── payment/
    ├── review/
    ├── coupon/  point/  rank/  file/
    └── support/             (notice, event, banner, policy, faq, follow, referral, report, partnership)

web-api/com/tastyhouse/webapi
├── verification/controller, dto         (presentation - 도메인별 폴더 정리)
├── member/controller, dto
├── ...
└── config, auth, scheduler, ratelimit, logging   (그대로 유지)
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
|    6 | Payment      | Order 안정화 후                                        |
|    7 | Review / Coupon / Point·Rank | 부가 BC                                |
|    8 | support 통합 | Notice/Event/Banner/Policy/Faq/Follow/Referral/Report/Partnership |
|    9 | File         | CDN/스토리지 추상화                                    |

**1 BC = 3개 PR 분할 원칙** (PR 거대화 방지):
- **PR-1**: 패키지 이동 (mechanical move only — 동작 변경 없음)
- **PR-2**: AR / VO 도입 (캡슐화 메서드, 식별자 강타입, ID 참조 전환)
- **PR-3**: DomainEvent 도입

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
| **infrastructure**  | `core/domain/<bc>/infrastructure/`         | Repository 구현                           | 다른 BC의 application/infrastructure 참조 금지    |
| **presentation**    | `web-api/`, `admin-api/`                   | Controller, Request/Response DTO          | application 레이어만 호출                         |

### 5.3 페이징 타입 경계 규칙

```
✅ domain/application 레이어: PageResult<T> (반환), PageQuery (입력) — core-module/shared/page/
❌ domain/application 레이어: org.springframework.data.domain.{Page, Pageable, PageRequest, PageImpl} 사용 금지

✅ infrastructure/persistence 레이어: Spring Data 페이징 타입 내부 사용 허용
   (PageRequest.of(), PageImpl, PageableExecutionUtils → PageResult.of()로 변환 후 반환)

✅ presentation 레이어: PageResult<T>를 그대로 사용하여 ApiResponse로 응답 (별도 변환 클래스 없음)
❌ presentation 레이어: Spring Data 페이징 타입 직접 참조 금지
```

**검증 기준**: `grep -rln "org.springframework.data.domain" core-module/src/main/java` 결과가 `infrastructure/persistence` 경로에만 존재해야 한다.

---

### 5.4 BC 횡단 규칙

```
✅ allowed: domain.member.application → domain.verification.application
❌ forbidden: domain.member.domain → domain.verification.infrastructure
❌ forbidden: domain.member.application → domain.verification.infrastructure
```

**원칙**: BC 간 통신은 application 레이어 또는 DomainEvent로만.

---

### 5.5 파사드(Facade) 도입 기준

presentation(`web-api`, `admin-api`)의 기본 규칙은 §5.2와 같이 **컨트롤러가 application 레이어
(`XxxCommandService`/`XxxQueryService`)를 직접 주입**하는 것이다. 다만 아래 조건 중 하나라도
해당하면 컨트롤러와 application 서비스 사이에 모듈 전용 파사드(예: `web-api`의 `AuthFacade`,
`MemberFacade`)를 두는 것을 허용한다.

```
✅ 파사드 도입 조건 (하나 이상 해당 시)
   1. 하나의 유스케이스가 2개 이상의 application service(또는 BC)를 오케스트레이션해야 함
      예) MemberFacade가 memberAccountService + memberAuthService + couponQueryService 등을 조율
   2. 여러 서비스의 결과를 조합하거나 응답 DTO로 변환하는 로직이 필요함
   3. 컨트롤러가 내부 서비스 분할(Command/Query, 다중 도메인 서비스)을 알 필요가 없도록 캡슐화해야 함

❌ 파사드 불필요 (단일 서비스에 대한 단순 위임)
   - 하나의 CommandService 또는 QueryService만 호출하고 끝나는 CRUD 유스케이스
   - 예) PolicyAdminApiController → PolicyCommandService 직접 주입, NoticeApiController → NoticeCommandService/NoticeQueryService 직접 주입
```

**원칙(Fowler, Service Layer)**: "필요하다면, 가능한 가장 얇은 Service Layer를 두라." 파사드는
필수 계층이 아니라 위 조건을 만족할 때만 선택적으로 추가하는 계층이다. `admin-api`처럼 단순 CRUD
위주 모듈은 파사드 없이 컨트롤러가 application 서비스를 직접 주입하는 것이 표준이며, `web-api`의
`AuthFacade`/`MemberFacade`처럼 다중 서비스 조율이 실제로 필요한 경우에만 파사드를 도입한다.

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

| AR        | 연관 엔티티(별도 Repository) / VO               |
| --------- | ----------------------------------------------- |
| `Member`  | MemberSocialAccount, Email, Nickname            |
| `Place`   | PlaceBusinessHour, PlaceBreakTime, PlacePhoto, PlaceMenu |
| `Order`   | OrderItem, OrderItemOption                      |
| `Payment` | PaymentRefund, TossPaymentRecord                |
| `Review`  | ReviewComment, ReviewLike, ReviewImage          |

> `@OneToMany`, `@ManyToOne`, `@ElementCollection` 사용 금지.  
> AR 내부 자식 엔티티도 별도 Repository로 분리하고, 외부 BC 참조는 ID VO로만 처리한다.

#### 외부 참조는 ID로

```java
@Entity
@Table(name = "orders")
public class Order {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "member_id"))
    private MemberId memberId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "place_id"))
    private PlaceId placeId;
    // OrderItem은 OrderItemRepository로 분리 — AR 내부라도 @OneToMany 미사용
}
```

```java
// domain/repository/OrderItemRepository.java
public interface OrderItemRepository {
    List<OrderItem> findByOrderId(OrderId orderId);
    OrderItem save(OrderItem item);
    void deleteByOrderId(OrderId orderId);
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
public class Member extends BaseEntity {
    public void withdraw(WithdrawalReason reason, LocalDateTime now) {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.ALREADY_WITHDRAWN);
        }
        this.status = MemberStatus.WITHDRAWN;
        this.withdrawnAt = now;
        this.email = null;
        this.nickname = null;
    }
}

// Application 서비스 (이벤트 발행은 CommandService에서 담당)
@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandService {
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void withdraw(MemberId memberId, WithdrawalReason reason) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        member.withdraw(reason, LocalDateTime.now());
        memberRepository.save(member);
        eventPublisher.publishEvent(new MemberWithdrawnEvent(memberId, reason, LocalDateTime.now()));
    }
}
```

### 6.3 DomainEvent

#### ApplicationEventPublisher 직접 주입 방식

```java
@Entity
public class Member extends BaseEntity {

    public void withdraw(WithdrawalReason reason, LocalDateTime now) {
        // ... 상태 변경 ...
    }
}

// MemberCommandService
@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandService {
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void withdraw(MemberId memberId, WithdrawalReason reason) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        member.withdraw(reason, LocalDateTime.now());
        memberRepository.save(member);
        eventPublisher.publishEvent(new MemberWithdrawnEvent(memberId, reason, LocalDateTime.now()));
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

---

## 7. Verification 파일럿 단계별 작업

### 7.1 대상 코드

| 분류            | 현재 위치                                                         |
| --------------- | ----------------------------------------------------------------- |
| Entity          | `core/entity/verification/EmailVerification`, `PhoneVerification` |
| Status enum     | `core/entity/verification/EmailVerificationStatus`, `PhoneVerificationStatus` |
| Repository      | `core/repository/verification/`                                   |
| Service         | `core/service/EmailVerificationCoreService`, `PhoneVerificationCoreService` |
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
       private final MailSender mailSender;

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

4. Controller 호출 변경
   ```java
   // VerificationApiController
   // Before
   private final EmailVerificationCoreService emailVerificationCoreService;

   // After
   private final EmailVerificationCommandService emailVerificationCommandService;
   private final EmailVerificationQueryService emailVerificationQueryService;
   ```

5. 기존 `*CoreService` 삭제 (위임 후 안전 확인 시).

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
public class EmailVerification extends BaseEntity {

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
        // 이벤트 발행은 CommandService에서 담당
    }
}

// EmailVerificationCommandService
@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationCommandService {
    private final EmailVerificationRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public void verify(EmailVerificationId id, VerificationCode input) {
        EmailVerification verification = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.VERIFICATION_NOT_FOUND));
        verification.verify(input, LocalDateTime.now());
        repository.save(verification);
        eventPublisher.publishEvent(new EmailVerifiedEvent(id, verification.getEmail(), LocalDateTime.now()));
    }
}
```

### 7.4 PR-3: DomainEvent

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

---

## 8. *CoreService 재배치 매핑

| 기존                                                           | 이동 후                                                                                                |
| -------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| `EmailVerificationCoreService` / `PhoneVerificationCoreService` | `application/EmailVerificationCommandService` + `EmailVerificationQueryService`                         |
| `MemberCoreService` (write+read 혼재)                           | `application/MemberCommandService` + `MemberQueryService` 분리 (CQS). 단순 위임은 AR 메서드로 흡수      |
| `MemberWithdrawalCoreService`                                   | `MemberWithdrawalUseCase` (application) + `MemberWithdrawnEvent` 발행                                   |
| `MemberSocialAccountCoreService`                                | `Member` AR 내부 메서드 + application 협력                                                              |
| `RankAggregationService`                                        | application 레이어 도메인 서비스 (여러 AR 협력 — Domain Service)                                        |
| `PaymentCoreService`                                            | `application/PaymentCommandService` + `PaymentQueryService`. Toss 연동은 infrastructure 레이어로          |
| `FileCoreService`                                               | `application/FileCommandService` + `FileQueryService`                                                    |
| 그 외 *CoreService                                              | `<bc>/application/<Bc>CommandService` + `<Bc>QueryService` 패턴 적용                                    |

---

## 9. 위험 요소 및 마이그레이션 주의사항

| #   | 위험                                                         | 대응                                                                  |
| --- | ------------------------------------------------------------ | --------------------------------------------------------------------- |
| 1   | 연관관계 제거 후 N+1 쿼리 발생                               | Repository 분리 시 필요한 조회는 명시적 쿼리로 대체, QueryDSL fetch join 활용 |
| 2   | QueryDSL Q클래스 경로 변경                                   | gradle querydsl generated 경로 확인, 빌드 캐시 무효화 (`./gradlew clean`) |
| 3   | 순환 의존 (Member↔Review, Place↔Review)                      | Domain Service(application 레이어)에서 조율, AR 직접 참조 금지        |
| 4   | 트랜잭션 경계 변화로 데이터 일관성 깨짐                      | DomainEvent 도입. 초기엔 `AFTER_COMMIT` 동기                          |
| 5   | DTO 위치 혼재 (`entity/place/dto/`)                          | read model로 분류해 `application/dto/`로 이동, native projection은 infrastructure |
| 6   | admin-api 호환 깨짐                                           | admin도 동일 application 서비스 사용 → command/query 변경 시 동시 수정 PR |
| 7   | 스키마 무변경 보장                                           | `@Table(name=...)` 명시 점검, `hibernate.ddl-auto=validate` 적용     |
| 8   | `@Entity` 클래스 패키지 이동 시 직렬화/캐시 충돌             | Redis 캐시 키에 FQCN 포함 여부 확인, 배포 전 캐시 비우기              |
| 9   | 트랜잭션 전파 변경으로 readOnly 누락                          | `QueryService`는 항상 `@Transactional(readOnly = true)`               |
| 10  | 패키지 이동 후 `DatabaseConfig` 스캔 범위 미반영으로 빈 등록 실패 | `@EnableJpaRepositories`, `@EntityScan`의 `basePackages`에 `com.tastyhouse.core.domain` 추가 |

---

## 10. 검증 체크리스트

각 BC 전환 PR마다 아래 항목을 모두 통과해야 머지.

### 10.1 정적 검증

- [ ] 컴파일 그린
- [ ] `hibernate.ddl-auto=validate`로 스키마 무변경 확인
- [ ] QueryDSL Q클래스 정상 생성 (`./gradlew clean compileJava`)

### 10.2 회귀 테스트

- [ ] 기존 단위 테스트 그린
- [ ] `@SpringBootTest` 통합 테스트 그린
- [ ] OpenAPI 응답 JSON 스냅샷 동일성 (필드 추가/삭제 없음)
- [ ] 핵심 시나리오 e2e (회원가입/주문/결제/리뷰 흐름)

### 10.3 성능 회귀

- [ ] QueryDSL 핵심 쿼리 explain plan 비교 (인덱스 사용 동일)
- [ ] 응답시간 측정 (주요 API: 회원조회, 주문조회, 가게리스트)

### 10.4 운영 안정성

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
- [ ] `core/domain/verification/infrastructure/persistence/`

파일 이동:
- [ ] `entity/verification/EmailVerification` → `domain/verification/domain/model/`
- [ ] `entity/verification/PhoneVerification` → `domain/verification/domain/model/`
- [ ] `entity/verification/*VerificationStatus` → `domain/verification/domain/model/`
- [ ] `repository/verification/*JpaRepository` → `domain/verification/infrastructure/persistence/`

신규 작성:
- [ ] `MailSender`, `SmsSender` (인터페이스)
- [ ] `EmailVerificationRepository`, `PhoneVerificationRepository` (도메인 인터페이스)
- [ ] `*RepositoryImpl` (infrastructure 구현)
- [ ] `EmailVerificationCommandService`, `EmailVerificationQueryService`
- [ ] `PhoneVerificationCommandService`, `PhoneVerificationQueryService`

호출 변경:
- [ ] `VerificationApiController` → application 서비스 호출로 변경
- [ ] 기타 *CoreService 사용처 grep 후 일괄 변경

정리:
- [ ] 기존 `EmailVerificationCoreService`, `PhoneVerificationCoreService` 삭제
- [ ] 기존 `entity/verification/`, `repository/verification/` 빈 디렉토리 정리

---

## 11. 도메인 모델 / JPA 엔티티 분리 (선별 적용, `infrastructure-module`)

> 추가일: 2026-07-19
> 상태: **notice 파일럿 완료, admin 전환 완료, banner 전환 완료, bug 전환 완료, faq 전환 완료, coupon 전환 완료, event 전환 완료, member 전환 완료(코어 3개 애그리거트만; follow/referral 제외), partnership 전환 완료, policy 전환 완료, point 전환 완료, rank 전환 완료(하드→소프트 삭제 전환 포함), reservation 전환 완료(@Version 낙관적 락 애그리거트 분리 최초 사례), search 전환 완료(3개 애그리거트, 읽기 전용 애그리거트 reconstitute-only 최초 사례), review 전환 완료(6개 애그리거트 + 죽은 코드 애그리거트 ReviewProduct 삭제), shop 전환 완료(@Entity 17개 중 핵심 `Shop` 애그리거트 1개만 우선 전환 — 최초의 부분 전환 사례, 나머지 16개 자식 엔티티는 core 잔류; `QShop` 소멸로 `order`/`product`/`shop`(`ShopChoiceRepositoryImpl`) 및 이미 이동한 `review`까지 4개 파일이 함께 깨져 `PathBuilder`로 전환한 이번 프로젝트 최대 팬아웃 사례), order 전환 완료(연관관계 없는 3개 애그리거트 `Order`+`OrderProduct`+`OrderProductOption`; 미전환 `payment`가 쓰는 `OrderIdConverter`는 core 잔류, 미사용 컨버터 2개는 삭제; order 이동으로 `shop` 전환 때 생겼던 `PathBuilder` 우회를 `QShopJpaEntity` 정식 조인으로 복원; 이미 이동한 `review`의 `QOrderProduct`→`QOrderProductJpaEntity` 치환), payment 전환 완료(연관관계 없는 3개 애그리거트 `Payment`+`PaymentRefund`+`TossPaymentRecord`; order를 먼저 분리해야만 풀리는 core→infra 역참조 blocker가 있었던 최초 사례 — order 미분리 시 `OrderRepositoryImpl`이 core의 `QPayment`를 조인 중이라 payment를 먼저 옮기면 컴파일이 깨져 order→payment 순서로 진행, order 이동 후 `OrderRepositoryImpl`의 `QPayment`→`QPaymentJpaEntity` import 치환으로 해소; `TossPaymentRecordMapper`는 파라미터 50여 개라 3중 대조 필요)** — 이후 도메인은 아래 롤아웃 절차로 점진 적용

### 배경

당초 이 문서는 "도메인 모델 = JPA 엔티티(`@Entity`가 domain 레이어에 잔류)"를 과도기적으로 허용했다. 상태전이·불변식이 실재하는 도메인에서는 JPA 제약(`protected` 기본 생성자, 프록시, 더티 체킹 암묵 저장)이 도메인에 새고 순수 단위 테스트가 불가하므로, **순수 도메인 모델(POJO)과 JPA 엔티티를 분리하고 JPA 어댑터를 별도 `infrastructure-module`로 옮기는 패턴**을 도입했다. 단순 CRUD 도메인은 현행 유지가 허용되며 전환은 강제가 아니다(클래스·모듈 모두 Strangler Fig).

### 모듈 의존 구조

```
web-api / admin-api ──implementation──→ core-module          (도메인 POJO + application + Repository 인터페이스)
        └──runtimeOnly──→ infrastructure-module (com.tastyhouse.infrastructure.*)
                              └──implementation──→ core-module
```

- 분리된 도메인: 도메인 모델·application·Repository 인터페이스는 core-module에 유지, `XxxJpaEntity`/`XxxMapper`/`XxxJpaRepository`/`XxxRepositoryImpl`은 `infrastructure-module`.
- 미분리 도메인: persistence가 core-module 내부에 잔류(현행).

### 핵심 규칙 (상세는 루트 `CLAUDE.md` "도메인 모델 / JPA 엔티티 분리 규칙", `infrastructure-module/AGENTS.md`)

- 순수 도메인 모델: `of(...)`(신규) + `reconstitute(...)`(DB 재구성 전용) 두 팩토리, `id` 미영속 시 null, `jakarta` 무의존.
- 저장: **load-copy-save** (id null=insert, id 존재=managed 조회 후 `applyChanges` 복사, merge 금지).
- command 서비스는 변경 후 **명시적 `repository.save`** 호출(더티 체킹 상실 보완).
- DDL·`ddl-auto=validate` 무변경. Q타입은 엔티티=infra / result DTO=core 각각 생성.

### 도메인별 롤아웃 절차

1. 대상 선정: 상태전이/불변식이 코드에 실재하는 도메인 우선 (order·payment·coupon·point·reservation)
2. 도메인 모델 POJO화 (`of`/`reconstitute`) → `XxxJpaEntity`/`XxxMapper` 신설 → persistence를 `infrastructure-module`로 이동 → command 서비스 명시적 save → 순수 단위 테스트 → 문서 reference 갱신
3. 전 도메인 이동 완료 시: core-module에서 `spring-boot-starter-data-jpa`·mysql 의존 제거(QueryDSL 어노테이션은 result DTO Q타입용 잔류), `DatabaseConfig` 이관 검토

### notice 파일럿 결과물 (reference)

- 순수 모델: `core-module/.../notice/domain/model/Notice`
- 어댑터: `infrastructure-module/.../notice/persistence/{NoticeJpaEntity, NoticeMapper, NoticeJpaRepository, NoticeRepositoryImpl}`
- 명시적 save: `NoticeCommandService#updateNotice`·`#deleteNotice`
- 순수 단위 테스트: `core-module/src/test/.../notice/domain/model/NoticeTest`

### admin 전환 결과물 (reference — 연관관계·QueryDSL·update 없는 최소 CRUD 변형)

- 순수 모델: `core-module/.../admin/domain/model/Admin` (`create`/`reconstitute`; 감사 필드 미소비로 `createdAt`/`updatedAt` 생략)
- 어댑터: `infrastructure-module/.../admin/persistence/{AdminJpaEntity, AdminMapper, AdminJpaRepository, AdminRepositoryImpl}` — QueryDSL 없이 순수 pass-through, `applyChanges`/load-copy-save 분기 없음(update 경로 자체가 없어 `save`는 insert 전용)
- 명시적 save: 대상 없음 — `AdminCommandService#createAdmin`이 이미 저장 시 `save` 호출(더티 체킹 의존 지점 0건)
- 순수 단위 테스트: `core-module/src/test/.../admin/domain/model/AdminTest`

### banner 전환 결과물 (reference — enum 필드 + 크로스 도메인 QueryDSL 조인 변형)

- 순수 모델: `core-module/.../banner/domain/model/Banner` (`of`/`reconstitute`; `BannerType` enum, `createdAt`/`updatedAt` 포함 — `BannerDetailResult`가 둘 다 소비)
- 어댑터: `infrastructure-module/.../banner/persistence/{BannerJpaEntity, BannerMapper, BannerJpaRepository, BannerRepositoryImpl}` — `BannerJpaEntity.type`은 `@Enumerated(EnumType.STRING)`+`columnDefinition="VARCHAR(20)"` 원본 유지. `BannerRepositoryImpl`은 미분리 `file` 도메인의 `QUploadedFile`(core-module 생성)을 그대로 조인 — `QBannerJpaEntity`(infra 생성)만 치환하고 크로스 도메인 Q타입 import는 변경하지 않음
- 명시적 save: `BannerCommandService#updateBanner`·`#deleteBanner`
- 순수 단위 테스트: `core-module/src/test/.../banner/domain/model/BannerTest`

### faq 전환 결과물 (reference — 2개 애그리거트, 크로스 엔티티 QueryDSL)

- 순수 모델: `core-module/.../faq/domain/model/Faq`·`FaqCategory` (`of`/`reconstitute`; 둘 다 JPA 연관관계 없이 raw FK `Long faqCategoryId`로만 연결, `createdAt`/`updatedAt` 포함 — `FaqDetailResult`/`FaqCategoryManagementResult`가 소비)
- 어댑터: `infrastructure-module/.../faq/persistence/{FaqJpaEntity, FaqCategoryJpaEntity, FaqMapper, FaqCategoryMapper, FaqJpaRepository, FaqCategoryJpaRepository, FaqRepositoryImpl, FaqCategoryRepositoryImpl}` — `FaqCategoryRepositoryImpl#existsActiveItemsByCategoryId`가 자신의 Q타입이 아닌 `QFaqJpaEntity`(다른 애그리거트의 infra Q타입)를 직접 조회하는 크로스 엔티티 QueryDSL 사례
- 명시적 save: `FaqCommandService#updateFaq`·`#deleteFaq`, `FaqCategoryCommandService#updateCategory`·`#deleteCategory`
- 순수 단위 테스트: `core-module/src/test/.../faq/domain/model/FaqTest`·`FaqCategoryTest`

### coupon 전환 결과물 (reference — 2개 애그리거트, 감사 필드 보유 여부가 갈리는 변형)

- 순수 모델: `core-module/.../coupon/domain/model/Coupon`·`MemberCoupon` (`of`/`reconstitute`; 둘 다 JPA 연관관계 없이 `Coupon`은 raw FK 없음, `MemberCoupon`은 raw FK `Long couponId` + `@Convert` VO `MemberId memberId`로만 연결). `Coupon`은 `createdAt`/`updatedAt` 포함(`CouponDetailResult.from`이 둘 다 소비), `MemberCoupon`은 감사 필드 생략(어떤 result도 도메인 경유로 감사 시각을 쓰지 않고 QueryDSL이 엔티티에서 직접 투영)
- 어댑터: `infrastructure-module/.../coupon/persistence/{CouponJpaEntity, MemberCouponJpaEntity, CouponMapper, MemberCouponMapper, CouponJpaRepository, MemberCouponJpaRepository, CouponRepositoryImpl, MemberCouponRepositoryImpl}` — `MemberCouponRepositoryImpl`이 `QCouponJpaEntity`+`QMemberCouponJpaEntity` 두 엔티티를 join하는 크로스 엔티티 QueryDSL 사례
- 명시적 save: `CouponCommandService#updateCoupon`·`#deleteCoupon` (`useCoupon`은 기존에 이미 `memberCouponRepository.save` 호출 중이라 추가 불필요)
- 순수 단위 테스트: `core-module/src/test/.../coupon/domain/model/CouponTest`·`MemberCouponTest`

### event 전환 결과물 (reference — 3개 애그리거트, 감사 필드 보유 여부가 갈리는 변형 + 공유 `@Embeddable` VO)

- 순수 모델: `core-module/.../event/domain/model/Event`·`EventWinner`·`EventAnnouncement` (`of`/`reconstitute`; 셋 다 JPA 연관관계 없이 raw FK `Long eventId`로만 연결). `Event`만 `createdAt`/`updatedAt` 포함(`EventManagementDetailResult.from`이 둘 다 소비), `EventWinner`/`EventAnnouncement`는 감사 필드 생략(어떤 result도 도메인 경유로 감사 시각을 쓰지 않음). `EventWinner`는 공유 `@Embeddable` VO `PhoneNumber`를 도메인 모델 필드로 그대로 유지(`EventWinnerResult.from`의 `getPhoneNumber().getValue()` 호출·검증 로직 무변경)
- 어댑터: `infrastructure-module/.../event/persistence/{EventJpaEntity, EventWinnerJpaEntity, EventAnnouncementJpaEntity, EventMapper, EventWinnerMapper, EventAnnouncementMapper, EventJpaRepository, EventWinnerJpaRepository, EventAnnouncementJpaRepository, EventRepositoryImpl, EventWinnerRepositoryImpl, EventAnnouncementRepositoryImpl}` — `EventRepositoryImpl`이 미분리 `file` 도메인의 `QUploadedFile`(core-module 생성)을 그대로 조인 — `QEventJpaEntity`(infra 생성)만 치환하고 크로스 도메인 Q타입 import는 변경하지 않음
- 명시적 save: `EventCommandService#updateEvent`·`#deleteEvent`·`#updateAnnouncement`·`#deleteWinner` (`createEvent`/`createAnnouncement`/`createWinner`는 기존에 이미 `save` 호출 중이라 추가 불필요)
- 순수 단위 테스트: `core-module/src/test/.../event/domain/model/EventTest`·`EventWinnerTest`·`EventAnnouncementTest`

### member 전환 결과물 (reference — 코어 3개 애그리거트, `@Embedded` VO 공유 + `@Convert` FK VO + 크로스 도메인 `PathBuilder` 참조 변형)

- **범위**: member 코어 3개 애그리거트(`Member`/`MemberSocialAccount`/`MemberWithdrawal`)만 전환. 같은 `member` 폴더 하위의 `follow`(`Follow`)·`referral`(`MemberReferral`)는 이번 범위 제외(현행 유지, 별도 전환 대상).
- 순수 모델: `core-module/.../member/domain/model/Member`·`MemberSocialAccount`·`MemberWithdrawal` (`of`/`reconstitute`; 셋 다 JPA 연관관계 없이 `MemberSocialAccount`/`MemberWithdrawal`은 `@Convert` FK VO `MemberId`로만 연결). `Member`는 공유 `@Embeddable` VO `PhoneNumber`를 도메인 모델 필드로 그대로 유지(파사드의 `getPhoneNumber().getValue()`/`.toString()` 호출 무변경). `Member`/`MemberSocialAccount`만 `createdAt`/`updatedAt` 포함(각각 admin `MemberService#getCreatedAt()` 소비·엔티티 자체 감사 관례 유지), `MemberWithdrawal`은 감사 필드 생략(어떤 result도 도메인 경유로 감사 시각을 쓰지 않고 insert 전용).
- 어댑터: `infrastructure-module/.../member/persistence/{MemberJpaEntity, MemberSocialAccountJpaEntity, MemberWithdrawalJpaEntity, MemberMapper, MemberSocialAccountMapper, MemberWithdrawalMapper, MemberJpaRepository, MemberSocialAccountJpaRepository, MemberWithdrawalJpaRepository, MemberRepositoryImpl, MemberSocialAccountRepositoryImpl, MemberWithdrawalRepositoryImpl}` — `MemberSocialAccountJpaEntity`/`MemberWithdrawalJpaEntity`는 `@Convert(MemberIdConverter.class) MemberId memberId`를 그대로 유지(`bug` 선례와 동일하게 `MemberIdConverter`는 core-module에 잔류, `bug`의 `BugReportJpaEntity`가 이미 cross-module import 중이라 이동 불가). `MemberRepositoryImpl`은 미분리 `file` 도메인의 `QUploadedFile`(core-module 생성)을 그대로 조인.
- **크로스 도메인 참조 신규 패턴 — `PathBuilder`**: `Member`가 POJO로 전환되며 core-module에는 더 이상 `QMember`(도메인 모델 Q타입)가 생성되지 않는다. 그런데 아직 미분리 상태로 core-module에 남아 있는 `follow`(`FollowRepositoryImpl`)·`review`(`ReviewRepositoryImpl`)·`rank`(`MemberReviewRankRepositoryImpl`) 세 리포지토리가 `QMember`를 join해 `nickname`/`memberGrade`/`profileImageFileId`를 읽고 있었다. core-module은 `infrastructure-module`을 의존할 수 없어(의존 방향: infrastructure → core) 이동한 `QMemberJpaEntity`를 import할 수 없으므로, 세 파일 모두 `com.querydsl.core.types.dsl.PathBuilder<Object>`로 JPA 엔티티명 문자열(`"MemberJpaEntity"`, `@Entity(name=...)` 미지정 시 기본값인 단순 클래스명)을 참조해 필요한 컬럼(`id`/`nickname`/`memberGrade`/`profileImageFileId`)만 `NumberPath`/`StringPath`/`EnumPath`로 타입 세이프하게 노출하는 방식으로 전환했다. 이는 이 프로젝트에서 "이미 분리된 도메인의 엔티티를, 아직 미분리인 다른 도메인이 Q타입 없이 참조"하는 첫 사례이며, 이후 다른 미분리 도메인이 이미 분리된 엔티티를 참조해야 할 때도 동일 패턴(`PathBuilder` + 엔티티명 문자열)을 재사용한다.
- 명시적 save: `MemberCommandService#updateProfile`·`#updatePersonalInfo`·`#updatePassword`·`#suspend`·`#activate`에 추가(`signUp`/`signUpSocial`/`withdraw`는 기존에 이미 `save` 호출 중이라 추가 불필요). 더불어 `web-api`의 `KakaoSocialLoginService`·`NaverSocialLoginService`·`FacebookSocialLoginService`·`AppleSocialLoginService`가 기존 소셜 계정 로그인 시 `socialAccount.updateProviderInfo(...)`만 호출하고 `save`를 부르지 않은 채 더티 체킹에 의존하고 있던 지점을 발견해 `memberCommandService.saveSocialAccount(socialAccount)` 호출을 추가했다(도메인 분리로 이 네 서비스가 즉시 깨졌을 실제 회귀 지점).
- 순수 단위 테스트: `core-module/src/test/.../member/domain/model/MemberTest`·`MemberSocialAccountTest`·`MemberWithdrawalTest`

### partnership 전환 결과물 (reference — 단일 애그리거트, enum 1개, detached merge 교정 사례)

- 순수 모델: `core-module/.../partnership/domain/model/PartnershipRequest` (`of`/`reconstitute`; JPA 연관관계 없이 단일 애그리거트, enum 필드 `PartnershipStatus` 1개, `createdAt`/`updatedAt` 둘 다 포함 — `PartnershipRequestResult.from`이 둘 다 소비). 재대입되지 않는 필드(`businessName`/`address`/`addressDetail`/`contactName`/`contactPhone`/`consultationRequestedAt`)는 `final`로 선언.
- 어댑터: `infrastructure-module/.../partnership/persistence/{PartnershipRequestJpaEntity, PartnershipRequestMapper, PartnershipRequestJpaRepository, PartnershipRepositoryImpl}` — `QPartnershipRequestJpaEntity`(infra 생성)만 치환, result projection(`QPartnershipRequestListItemResult`, core 생성)은 무변경.
- **detached merge → load-copy-save 교정**: 전환 전 `PartnershipRepositoryImpl#save`가 `partnershipRequestJpaRepository.save(request)`로 detached 엔티티를 통째 저장(merge)하고 있었다. 감사 필드 파손 위험을 없애기 위해 다른 도메인과 동일하게 신규는 insert, 기존은 필터 없는 PK 조회 → `applyChanges` → 반환으로 교정했다.
- 명시적 save: `PartnershipCommandService#changeStatus`·`#delete`에 추가(`create`는 기존에 이미 `save` 호출 중이라 추가 불필요).
- 순수 단위 테스트: `core-module/src/test/.../partnership/domain/model/PartnershipRequestTest`

### policy 전환 결과물 (reference — 단일 애그리거트, enum 1개, detached merge 교정 + 한 커맨드 메서드 내 더티 체킹 의존 2곳)

- 순수 모델: `core-module/.../policy/domain/model/PolicyDocument` (`of`/`reconstitute`; JPA 연관관계 없이 단일 애그리거트, enum 필드 `PolicyType` 1개, `createdAt`/`updatedAt` 둘 다 포함 — `PolicyDocumentResult.from`이 둘 다 소비, `PolicyListItemResult`는 `createdAt`만 소비). 재대입되지 않는 필드(`type`/`version`/`createdBy`)는 `final`로 선언.
- 어댑터: `infrastructure-module/.../policy/persistence/{PolicyDocumentJpaEntity, PolicyDocumentMapper, PolicyDocumentJpaRepository, PolicyDocumentRepositoryImpl}` — `QPolicyDocumentJpaEntity`(infra 생성)만 치환, result projection(`QPolicyDocumentResult`/`QPolicyListItemResult`, core 생성)은 무변경.
- **detached merge → load-copy-save 교정**: 전환 전 `PolicyDocumentRepositoryImpl#save`가 `entityManager.merge(policyDocument)`로 detached 엔티티를 통째 저장(merge)하고 있었다. 다른 도메인과 동일하게 신규는 insert, 기존은 필터 없는 PK 조회 → `applyChanges` → 반환으로 교정했다.
- **더티 체킹 의존 2곳(한 메서드 안에 공존)**: `updatePolicy`는 `policyDocument.update(...)` 후 `save` 미호출, `activatePolicy`는 신규 정책(`newPolicy.activate()` 후 `save`)은 이미 저장하면서도 `findCurrentEntityByType(...).ifPresent(PolicyDocument::deactivate)`로 비활성화되는 **기존** 정책은 save 없이 더티 체킹에만 의존하고 있었다. 두 지점 모두 명시적 save를 추가했다.
- 명시적 save: `PolicyCommandService#updatePolicy`·`#activatePolicy`(비활성화되는 기존 정책도 별도 save)에 추가(`createPolicy`는 기존에 이미 `save` 호출 중이라 추가 불필요).
- 순수 단위 테스트: `core-module/src/test/.../policy/domain/model/PolicyDocumentTest`

### point 전환 결과물 (reference — 2개 애그리거트, coupon과 동형: 상태전이+감사 생략 / insert 전용+createdAt만)

- 순수 모델: `core-module/.../point/domain/model/MemberPoint`·`MemberPointHistory` (`of`/`reconstitute`; 둘 다 JPA 연관관계 없이 `@Convert` FK VO `MemberId memberId`로만 연결). `MemberPoint`는 `addPoints`/`deductPoints` 상태전이가 있으나 어떤 result도 감사 시각을 소비하지 않아 감사 필드 생략, `MemberPointHistory`는 insert 전용(변경 없음)이며 `MemberPointHistoryResult.from`이 `createdAt`만 소비해 `createdAt`만 포함(`updatedAt`은 둘 다 미소비) — coupon(`Coupon`/`MemberCoupon`)과 동일한 감사 필드 비대칭 구조.
- 어댑터: `infrastructure-module/.../point/persistence/{MemberPointJpaEntity, MemberPointHistoryJpaEntity, MemberPointMapper, MemberPointHistoryMapper, MemberPointJpaRepository, MemberPointHistoryJpaRepository, MemberPointRepositoryImpl, MemberPointHistoryRepositoryImpl}` — `QMemberPointJpaEntity`/`QMemberPointHistoryJpaEntity`(infra 생성)만 치환. `MemberPointRepositoryImpl#save`는 load-copy-save, `MemberPointHistoryRepositoryImpl#save`는 insert 전용.
- **더티 체킹 의존 5곳**: `PointCommandService`의 `usePoints`/`earnPoints`/`refundPoints`/`reclaimEarnedPoints`/`deductPoints` 전부 `MemberPoint.addPoints`/`deductPoints` 변경 후 `save` 미호출 상태였다(`earnPoints`는 신규 생성 경로만 저장하고 `addPoints` 이후 재저장이 없었음). 5곳 모두 명시적 save를 추가했다.
- 명시적 save: `PointCommandService#usePoints`·`#earnPoints`·`#refundPoints`·`#reclaimEarnedPoints`·`#deductPoints`.
- 순수 단위 테스트: `core-module/src/test/.../point/domain/model/MemberPointTest`·`MemberPointHistoryTest`

### rank 전환 결과물 (reference — 3개 애그리거트, 분리와 별개로 하드→소프트 삭제 전환을 함께 수행한 사례)

- 순수 모델: `core-module/.../rank/domain/model/RankPeriod`·`RankPrize`·`MemberReviewRank` (`of`/`reconstitute`; 셋 다 JPA 연관관계 없음). `RankPrize`는 raw FK `Long rankId`로 `RankPeriod`를 참조하고, `MemberReviewRank`는 `@Convert` FK VO `MemberId memberId` + enum `RankType`(`EnumType.STRING`+`columnDefinition`)을 갖되 상태전이·삭제가 없는 insert-only 애그리거트다.
- **스코프 확대(사용자 결정)**: 원 가이드 원칙("DB 스키마·API 동작 무변경")과 별개로, `RankPeriod`/`RankPrize`의 기존 하드 삭제(`jpaRepository.delete(entity)`)를 소프트 삭제로 전환했다. `create.sql`의 `RANK_PERIOD`/`RANK_PRIZE`에 `is_deleted TINYINT(1) NOT NULL DEFAULT 0` 컬럼을 추가하고, `alter.sql`에 동일 컬럼을 추가하는 마이그레이션을 작성했다(부팅 전 DB에 먼저 적용해야 `ddl-auto=validate` 통과). 도메인 모델에 `deleted` 필드·`delete()` 메서드를 추가하고, `RankPeriodRepositoryImpl`/`RankPrizeRepositoryImpl`의 `delete(도메인)`은 **인터페이스 시그니처를 그대로 유지**하되 내부적으로 필터 없는 순수 PK 조회(managed) 후 `deleted` 플래그만 갱신하도록 재구현했다. `findAllPeriods`/`findPeriodById`/`findByPeriodId`/`findPrizeById`/`RankInfoRepositoryImpl`의 모든 조회 경로에 `deleted.isFalse()` 필터를 추가했다.
- 어댑터: `infrastructure-module/.../rank/persistence/{RankPeriodJpaEntity, RankPrizeJpaEntity, MemberReviewRankJpaEntity, RankPeriodMapper, RankPrizeMapper, MemberReviewRankMapper, RankPeriodJpaRepository, RankPrizeJpaRepository, MemberReviewRankJpaRepository, RankPeriodRepositoryImpl, RankPrizeRepositoryImpl, MemberReviewRankRepositoryImpl, RankInfoRepositoryImpl}` — `RankInfoRepositoryImpl`은 `RankPeriodJpaRepository`/`RankPrizeJpaRepository` 같은 JPA 리포지토리 없이 순수 QueryDSL 조회(`findActiveDuration`/`findActivePrizes`)만 담당하는 사례.
- **크로스 도메인 참조(기존 패턴 반복 적용)**: `MemberReviewRankRepositoryImpl`이 조회에 회원 정보(닉네임·프로필 이미지·등급)를 조인하는데, `member` 도메인이 이미 POJO로 전환되어 core-module에 `QMember`가 더 이상 생성되지 않는다. `follow`/`review` 도메인 전환 시 확립된 패턴과 동일하게 `com.querydsl.core.types.dsl.PathBuilder<Object>`로 JPA 엔티티명 문자열(`"MemberJpaEntity"`)을 참조해 `id`/`nickname`/`profileImageFileId`/`memberGrade`만 `NumberPath`/`StringPath`/`EnumPath`로 타입 세이프하게 노출했다(신규 패턴이 아니라 기존 재사용 패턴의 반복 적용).
- **더티 체킹 의존 2곳**: `RankCommandService`의 `updatePeriod`/`updatePrize`가 각각 `RankPeriod.update`/`RankPrize.update` 변경 후 `save` 미호출 상태였다. 2곳 모두 명시적 save를 추가했다. `deletePeriod`/`deletePrize`는 기존대로 `repository.delete(도메인)` 호출을 유지한다(소프트 삭제 로직은 `RepositoryImpl` 내부로 캡슐화).
- 명시적 save: `RankCommandService#updatePeriod`·`#updatePrize`.
- 순수 단위 테스트: `core-module/src/test/.../rank/domain/model/RankPeriodTest`·`RankPrizeTest`·`MemberReviewRankTest`

### reservation 전환 결과물 (reference — 2개 애그리거트, `@Version` 낙관적 락 애그리거트 분리 최초 사례)

- 순수 모델: `core-module/.../reservation/domain/model/Reservation`·`ReservationSlot` (`of`/`reconstitute`; 둘 다 JPA 연관관계 없음). `Reservation`은 `@Convert` FK VO `MemberId memberId` + raw FK `Long shopId` + enum `ReservationStatus`(`EnumType.STRING`+`columnDefinition`)를 갖고 `confirm`/`reject`/`cancel`/`complete` 상태전이가 있다. `ReservationSlot`은 raw FK `Long shopId`로만 연결되며 `reserve`/`release`로 점유수(`reservedCount`)를 증감하는 정원 카운터 애그리거트다. (네이밍 정리: 이 애그리거트는 원래 `ShopReservationSlot`이었으나 `Shop-` 접두어를 제거해 `ReservationSlot`으로 개명했고, 같은 패키지의 슬롯 시간·정원 상수 유틸은 이름 충돌을 피해 `SlotPolicy`로 개명했다. DB 테이블도 `SHOP_RESERVATION_SLOT` → `RESERVATION_SLOT`으로 함께 변경.)
- **`@Version` 낙관적 락 보존(신규 패턴)**: `ReservationSlot`은 이 프로젝트에서 분리한 첫 `@Version` 애그리거트다. POJO에 `private final Long version` 필드를 두어 `reconstitute` 시 마지막으로 읽은 버전을 보관하되, 도메인이 직접 증가시키지 않는다. `ReservationSlotJpaEntity`의 `@Version` 필드가 flush 시 JPA에 의해 자동 검증·증가되며, `ReservationSlotRepositoryImpl#save`의 load-copy-save(managed 엔티티 조회 → `applyChanges(reservedCount)` → 반환)가 이 메커니즘을 그대로 통과시키므로 기존 낙관적 락 동작이 100% 보존된다. `applyChanges`는 `version`을 인자로 받지 않고 건드리지도 않는다.
- **동시성 계약 보존 확인**: `ReservationCreator#createInNewTx`(별도 트랜잭션, self-invocation 회피용 분리 빈)가 슬롯 get-or-create → `slot.reserve()` → `slotRepository.save(slot)` → `entityManager.flush()` 순으로 명시 호출해, 신규 슬롯 동시 insert는 유니크 제약 위반(`DataIntegrityViolationException`)을, 기존 슬롯 동시 update는 `@Version` 충돌(`ObjectOptimisticLockingFailureException`)을 flush 시점에 유발한다. 두 예외는 호출자 `ReservationCommandService#create`의 최대 3회 재시도 루프가 처리하며, 분리 전후로 이 흐름은 변경되지 않았다(코드 리뷰 + 순수 단위테스트로만 검증; 동시성 통합테스트는 신설하지 않음 — 사용자 결정).
- 감사 필드 비대칭: `Reservation`만 조회 결과(`ReservationResult.from`)가 `createdAt`을 직접 소비해 감사 필드를 보유하고(`updatedAt`은 미사용이라 생략), `ReservationSlot`은 어떤 result도 감사 시각을 소비하지 않아 감사 필드가 전혀 없다 — coupon/point와 동형의 비대칭 구조.
- 어댑터: `infrastructure-module/.../reservation/persistence/{ReservationJpaEntity, ReservationSlotJpaEntity, ReservationMapper, ReservationSlotMapper, ReservationJpaRepository, ReservationSlotJpaRepository, ReservationRepositoryImpl, ReservationSlotRepositoryImpl}` — `QReservationJpaEntity`/`QReservationSlotJpaEntity`(infra 생성)만 치환. `ReservationSlotJpaRepository`의 파생 쿼리(`findByShopIdAndSlotDateAndSlotTime`/`findByShopIdAndSlotDate`)는 `ReservationSlotJpaEntity`를 반환하고 `RepositoryImpl`이 매퍼로 도메인 변환한다.
- **더티 체킹 의존 5곳**: `ReservationCommandService`의 `confirm`/`reject`/`complete`/`cancel`이 각각 상태전이 후 `save` 미호출 상태였고, `reject`/`cancel`이 호출하는 `releaseSlot`도 `slot.release()`만 하고 `save` 미호출 상태였다(더티 체킹 의존). 5곳 모두 명시적 save를 추가했다(`create`가 호출하는 `ReservationCreator#createInNewTx`는 기존에 이미 `save`+`flush`를 명시 호출 중이라 추가 불필요).
- 명시적 save: `ReservationCommandService#confirm`·`#reject`·`#complete`·`#cancel`·`#releaseSlot`(private).
- 순수 단위 테스트: `core-module/src/test/.../reservation/domain/model/ReservationTest`·`ReservationSlotTest`

### search 전환 결과물 (reference — 3개 애그리거트, 읽기 전용 애그리거트 reconstitute-only 최초 사례)

- 순수 모델: `core-module/.../search/domain/model/PopularKeyword`·`RecommendedKeyword`·`SearchKeywordLog` (셋 다 JPA 연관관계·ID VO 없음, 전 필드 `final`). `PopularKeyword`(``` `rank` ```예약어 컬럼 + 복합 인덱스)·`SearchKeywordLog`는 `of`/`reconstitute` 둘 다 공개하나, `RecommendedKeyword`는 **Java 애플리케이션 계층에 생성/변경 경로가 전혀 없는 읽기 전용 애그리거트(SQL/수동 시드)**라 `of` 없이 `reconstitute`만 공개한다(이 프로젝트에서 읽기 전용 애그리거트를 분리한 첫 사례). 세 모델 모두 어떤 result도 감사 시각을 소비하지 않아 감사 필드 생략(`PopularKeyword`/`RecommendedKeyword`는 JpaEntity만 `BaseEntity` 유지, `SearchKeywordLog`는 원본부터 `BaseEntity` 미상속).
- 어댑터: `infrastructure-module/.../search/persistence/{PopularKeywordJpaEntity, RecommendedKeywordJpaEntity, SearchKeywordLogJpaEntity, PopularKeywordMapper, RecommendedKeywordMapper, SearchKeywordLogMapper, PopularKeywordJpaRepository, RecommendedKeywordJpaRepository, SearchKeywordLogJpaRepository, PopularKeywordRepositoryImpl, RecommendedKeywordRepositoryImpl, SearchKeywordLogRepositoryImpl}` — `PopularKeywordRepositoryImpl`만 QueryDSL 사용(`QPopularKeywordJpaEntity`로 벌크 `deleteAll()`), 나머지 둘은 pass-through. `SearchKeywordLogJpaRepository`의 `@Modifying` JPQL(`DELETE FROM SearchKeywordLogJpaEntity`)은 엔티티명만 갱신, native `@Query` top10(`List<Object[]>`)은 테이블명 문자열이라 무변경.
- **update 경로 없음 → load-copy-save 불필요**: `PopularKeywordRepositoryImpl#saveAll`은 도메인 리스트를 엔티티로 매핑해 `jpaRepository.saveAll` 후 다시 도메인으로 매핑하는 **전량 신규 insert**만 수행한다(`SearchKeywordCommandService#aggregatePopularKeywords`가 매번 `deleteAll()` 후 재생성). `SearchKeywordLogRepositoryImpl#save`도 항상 신규 insert. update 경로 자체가 없어 managed 조회 후 `applyChanges` 분기가 필요 없다(admin 선례와 동형).
- 명시적 save: **대상 없음** — `SearchKeywordCommandService`가 `deleteAll`+`saveAll`(신규 insert)과 `deleteOlderThan`만 수행해 더티 체킹 의존 지점이 0건이다(admin 선례와 동일한 무-대상 사례).
- **범위 제외**: `SearchResultQueryService`는 자체 애그리거트 없이 product/review/shop 도메인에 위임하는 순수 오케스트레이션이라 이번 전환 대상이 아니다.
- 순수 단위 테스트: `core-module/src/test/.../search/domain/model/PopularKeywordTest`·`RecommendedKeywordTest`·`SearchKeywordLogTest`

### review 전환 결과물 (reference — 6개 애그리거트 + 죽은 코드 애그리거트 삭제 + 크로스 도메인 QueryDSL 3중 사례)

- 순수 모델: `core-module/.../review/domain/model/Review`·`ReviewComment`·`ReviewReply`·`ReviewImage`·`ReviewLike`·`ReviewTag`(`of`/`reconstitute`, 전부 JPA 연관관계 없이 raw FK(`Long reviewId`/`commentId` 등)와 `@Convert` FK VO(`memberId`, `ReviewReply`는 `memberId`+`replyToMemberId` 2개)로만 연결). 감사 시각 소비 여부로 셋씩 갈리는 사례 — `Review`/`ReviewComment`/`ReviewReply`는 `createdAt` 보유(각각 `ReviewCommandService`의 생성/수정 응답, `ReviewQueryService`의 `findCommentsIncludingHidden`/`findRepliesIncludingHidden`이 소비), `ReviewImage`/`ReviewLike`/`ReviewTag`는 불변 애그리거트라 감사 필드 생략. `updatedAt`은 여섯 애그리거트 모두 어떤 result도 소비하지 않아 전부 미보유.
- **죽은 코드 애그리거트 삭제**: 조사 결과 `ReviewProduct`(원래 7번째 애그리거트)는 전 코드베이스에 실제 호출부가 없는 죽은 코드였다. 분리 대상에서 제외하는 대신 이번 PR에서 **완전 삭제**했다 — `domain/model/ReviewProduct.java`, `domain/repository/ReviewProductRepository.java`, 및 core-module `infrastructure/persistence`의 `ReviewProductJpaRepository`·`ReviewProductRepositoryImpl` 전부 제거. (web-api의 `ReviewProductResponse`는 이름이 비슷할 뿐 리뷰+상품 조회용 별개 응답 DTO로, 이 삭제와 무관하게 그대로 유지.)
- 어댑터: `infrastructure-module/.../review/persistence/`(6개 애그리거트 × `JpaEntity`/`Mapper`/`JpaRepository`/`RepositoryImpl`, 총 24개 파일). `ReviewRepositoryImpl`이 자신의 서브쿼리 Q타입(`QReviewImageJpaEntity`/`QReviewLikeJpaEntity`/`QReviewCommentJpaEntity`, 별칭 인스턴스 `subReviewImage`/`subReviewLike`/`subReviewComment`/`sortReviewLike` 포함)을 모두 infra Q타입으로 치환하면서, 미분리 도메인의 `QUploadedFile`/`QOrderProduct`/`QProduct`/`QShop`/`QStation`과 `member` 도메인의 `MemberJpaEntity` `PathBuilder` 문자열 참조는 그대로 유지(banner/event 선례와 동일).
- **크로스 도메인 참조 파급(신규 관찰 지점)**: `review.domain.model.QReview`가 core-module에서 더 이상 생성되지 않게 되면서, 아직 미분리인 `shop` 도메인의 `ShopRepositoryImpl`이 리뷰 개수 집계에 `QReview`를 직접 조인하던 지점이 함께 깨졌다. `member`/`follow`/`rank` 선례와 동일하게 `PathBuilder<Object>`로 `"ReviewJpaEntity"`를 문자열 참조해 필요한 컬럼(`shopId`, `hidden`)만 노출하도록 전환했다(신규 패턴이 아니라 기존 크로스 도메인 참조 패턴의 반복 적용이며, `review` 도메인 자신의 분리 범위 밖인 `shop` 도메인 파일을 함께 고쳐야 했던 첫 사례).
- 더티 체킹에 의존하던 `Review`/`ReviewComment`/`ReviewReply`의 `hide`/`unhide`/`updateContent`에 명시적 save 추가: `ReviewCommandService#changeReviewHidden`·`#changeCommentHidden`·`#changeReplyHidden`·`#updateReview`. `ReviewImage`/`ReviewLike`/`ReviewTag`는 update 경로 자체가 없어(insert-only) load-copy-save 불필요(search 선례와 동형).
- 순수 단위 테스트: `core-module/src/test/.../review/domain/model/ReviewTest`·`ReviewCommentTest`·`ReviewReplyTest`·`ReviewImageTest`·`ReviewLikeTest`·`ReviewTagTest`

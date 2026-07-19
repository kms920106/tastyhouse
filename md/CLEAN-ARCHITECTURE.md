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
> 상태: **notice 파일럿 완료, admin 전환 완료, banner 전환 완료, bug 전환 완료** — 이후 도메인은 아래 롤아웃 절차로 점진 적용

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

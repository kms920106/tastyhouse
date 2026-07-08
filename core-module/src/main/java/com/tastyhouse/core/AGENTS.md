<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-06-02 -->

# core (DDD domain root)

## Purpose

DDD(Domain-Driven Design) 패턴으로 설계된 모든 Bounded Context가 거주하는 핵심 계층입니다. Spring Web 의존성이 없으며, 각 도메인은 3계층 Clean Architecture(domain/application/infrastructure)를 따릅니다. 공유 커널(PhoneNumber VO), 공통 예외 처리, JPA 설정을 포함합니다.

## Key Files / Cross-cutting packages

| 패키지/파일 | 설명 |
|---|---|
| `config/DatabaseConfig.java` | JPA Repository와 Entity 스캔 범위를 명시 (`com.tastyhouse.core.domain` 하위). @EnableJpaAuditing으로 createdAt/updatedAt 자동 관리 활성화 |
| `config/QueryDslConfig.java` | JPAQueryFactory 빈 제공 (QueryDSL 쿼리 작성 지원) |
| `shared/entity/BaseEntity.java` | 모든 Entity의 부모 클래스. @CreatedDate/@LastModifiedDate로 생성일시/수정일시 자동 관리 |
| `shared/vo/PhoneNumber.java` | 공유 커널 Value Object. 도메인 간 사용 가능한 휴대폰번호 추상화 |
| `exception/ErrorCode.java` | 도메인 에러 코드 enum. httpStatusCode(int), code(String), defaultMessage(String) 필드 포함. Spring Web 비의존성을 위해 HttpStatus 대신 int 사용 |
| `exception/BusinessException.java` | 기본 비즈니스 예외. errorCode 필드 포함. 모든 도메인 예외의 부모 클래스 |
| `exception/EntityNotFoundException.java` | 엔티티 미존재 예외 (BusinessException 상속) |
| `exception/AccessDeniedException.java` | 권한 없는 접근 예외 (BusinessException 상속) |

## Bounded Contexts (domain/)

| 도메인 | 목적 | VO | Event | Converter | 비고 |
|---|---|---|---|---|---|
| banner | 배너 관리 | - | - | - | 단순 조회 도메인 |
| bug | 버그 리포팅 | - | - | - | 버그 티켓 관리 |
| coupon | 쿠폰 발급/사용 | - | ✓ | - | 쿠폰 상태 이벤트 발행 |
| event | 이벤트/프로모션 관리 | - | - | - | 배너와 유사한 프로모션 |
| faq | FAQ 관리 | - | - | - | 조회 전용 도메인 |
| file | 파일 업로드/관리 | - | ✓ | - | CDN 추상화, 파일 삭제 이벤트 |
| follow | 사용자 팔로우 관계 | ✓ | - | - | 팔로우 상태 관리 |
| member | 회원 관리 | ✓ | ✓ | - | 가장 핵심 도메인. 다른 모든 BC가 MemberId로 참조 |
| notice | 공지사항 | - | - | - | 조회 전용 도메인 |
| order | 주문 관리 | ✓ | ✓ | ✓ | OrderId, OrderItemId VO. 시간대 슬롯 예약 지원 |
| partnership | 제휴사 관리 | - | - | - | B2B 파트너십 계약 정보 |
| payment | 결제 처리 | ✓ | ✓ | ✓ | 외부 결제 API(Toss) 의존. PaymentId VO |
| point | 포인트 관리 | ✓ | ✓ | - | 포인트 적립/차감 이벤트 |
| policy | 정책/약관 | ✓ | ✓ | - | 약관 버전 관리, 정책 변경 이벤트 |
| product | 상품 | ✓ | ✓ | - | 상품/옵션 그룹 정보 |
| rank | 등급 시스템 | - | - | - | 회원 등급 계산 |
| referral | 추천인 | ✓ | ✓ | - | 추천 보상 이벤트 |
| reservation | 시간대 예약 | - | - | - | 최신 추가 도메인. 낙관적 락 기반 정원 관리 |
| review | 리뷰/평점 | ✓ | ✓ | - | 리뷰 작성/수정/삭제 이벤트 |
| search | 검색 | - | - | - | 검색어 입력/조회 |
| shop | 가게/식당 | - | - | - | 2025-05-27 "Place"에서 "Shop"으로 도메인 리네이밍 완료 |
| verification | 이메일/SMS 인증 | ✓ | ✓ | - | DDD 파일럿 도메인. EmailVerification, PhoneVerification AR |

## For AI Agents

### Working In This Directory

**계층 의존 방향 규칙** (CLEAN-ARCHITECTURE.md 참조):

```
presentation(web-api, admin-api)
        ↓
   application
        ↓                    ↑
     domain  ←──── infrastructure (DIP)
        ↑
     shared (kernel)
```

- **domain 계층**: AR, Entity, VO, Repository(interface), DomainEvent만 포함. Spring/JPA 임포트 금지 (단, @Entity는 과도기 허용)
- **application 계층**: UseCase 조율, @Transactional 경계, DTO 변환. 다른 BC의 infrastructure 직접 참조 금지
- **infrastructure 계층**: Repository 구현, JPA/QueryDSL 쿼리. 다른 BC의 application/infrastructure 참조 금지
- **BC 간 통신**: application 레이어 또는 DomainEvent로만 허용 (domain 레이어 직접 참조 금지)

**ID 참조 규칙**:
- 외부 BC의 엔티티는 ID VO로만 참조 (예: Order.memberId: MemberId, Order.shopId: ShopId)
- @OneToMany/@ManyToOne 사용 금지. 자식 엔티티도 별도 Repository로 분리

**Service 분할 규칙** (CQS):
- CommandService: write 작업 (@Transactional)
- QueryService: read 작업 (@Transactional(readOnly = true))

**DTO 조립 규칙**:
- command/condition record에 원시 파라미터용 정적 팩토리 `of(...)`를 둔다. presentation(web-api/admin-api)의 Request 타입을 인자로 받는 팩토리는 금지(레이어 역전 방지). command 생성은 command record 자신의 `of(...)`가 담당하고, presentation의 Facade/컨트롤러는 Request를 원시 필드로 언패킹해 넘긴다 — Request DTO에 `toCommand()` 같은 변환 메서드를 두지 않는다.
- 호출부는 `new`로 DTO를 직접 조립하지 않고 정적 팩토리로 위임한다. 상세는 루트 CLAUDE.md 참고.
- `record`는 application 서비스 본문 안에 중첩 선언하지 않고 `application/dto/result`(command는 `application/dto/command`)에 `public record`로 분리한다. 서비스 내부 전용 `private` 헬퍼 record도 분리 시 `public`으로 격상한다(reference: `product/application/dto/result/OptionInfo`). 상세는 루트 CLAUDE.md 참고.

### Testing Requirements

- `hibernate.ddl-auto=validate` 적용하여 스키마 무변경 확인
- QueryDSL Q-class 재생성 필요 시: `./gradlew clean compileJava`
- 기존 테스트 회귀 검증 필수
- 주요 시나리오 e2e 테스트 (회원가입/주문/결제/리뷰/예약)

### Common Patterns

**Repository 패턴**:
```java
// domain/<bc>/domain/repository/XxxRepository.java (interface)
public interface OrderRepository {
    Optional<Order> findById(OrderId orderId);
    Order save(Order order);
}

// infrastructure/persistence/OrderJpaRepository.java (JPA interface)
public interface OrderJpaRepository extends JpaRepository<Order, Long> { }

// infrastructure/persistence/OrderRepositoryImpl.java (@Repository 구현체)
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    @Override
    public Optional<Order> findById(OrderId orderId) {
        return jpaRepository.findById(orderId.value());
    }
}
```

**ID VO + Converter 패턴**:
```java
// domain/vo/OrderId.java
public record OrderId(Long value) { }

// infrastructure/persistence/converter/OrderIdConverter.java
@Converter(autoApply = true)
public class OrderIdConverter implements AttributeConverter<OrderId, Long> {
    @Override
    public Long convertToDatabaseColumn(OrderId attribute) {
        return attribute == null ? null : attribute.value();
    }
    
    @Override
    public OrderId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new OrderId(dbData);
    }
}
```

**DomainEvent 패턴**:
```java
// domain/<bc>/domain/event/XxxEvent.java (record)
public record MemberRegisteredEvent(MemberId memberId, LocalDateTime registeredAt) { }

// application/<bc>CommandService.java
@Service
@Transactional
public class MemberCommandService {
    private final ApplicationEventPublisher eventPublisher;
    
    public void register(RegisterCommand cmd) {
        Member member = Member.create(cmd);
        memberRepository.save(member);
        eventPublisher.publishEvent(
            new MemberRegisteredEvent(member.getId(), LocalDateTime.now())
        );
    }
}

// 다른 BC의 이벤트 리스너
@Component
public class PointAllocationListener {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MemberRegisteredEvent event) {
        // 포인트 적립
    }
}
```

## Dependencies

### Internal

없음 (innermost 계층)

### External

| 의존성 | 용도 |
|---|---|
| **JPA/Hibernate** | 엔티티 영속화, 트랜잭션 관리 |
| **QueryDSL** | 타입안전 동적 쿼리 작성 |
| **MySQL** | 데이터베이스 |
| **Lombok** | @Getter/@RequiredArgsConstructor 등 보일러플레이트 제거 |
| **Spring Data** | Repository, JpaAuditing, EntityManager 지원 |

<!-- MANUAL: -->

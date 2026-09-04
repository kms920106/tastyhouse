<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# domain-module

## Purpose
모든 도메인의 핵심을 담는 라이브러리 모듈(`java-library`). 순수 POJO 도메인 모델(Aggregate Root), Value Object, DomainEvent, Repository **write 포트**, 도메인 서비스(불변식 오케스트레이션·무상태 정책), 외부 어댑터용 **출력 포트**를 포함한다.

**프레임워크를 전혀 모른다** — production 의존이 **하나도 없다**(Lombok까지 제거됨). Spring Web뿐 아니라 JPA·QueryDSL·`spring-tx`/`spring-orm`도 없으므로, `@Entity`/`@Transactional`/`@Service`/`@Component`/`com.querydsl.*`가 이 모듈에 단 한 곳도 없다. 예외의 HTTP 상태는 `int httpStatusCode`로, 낙관적 락 충돌은 `OptimisticLockConflictException`으로 표현한다. `web-api`/`admin-api`/`ceo-api`/`batch-module`/`infrastructure:persistence`/`infrastructure:external`/`security-module`이 이 모듈에 의존한다(역방향 의존은 없다).

> 과거 `core-module`(패키지 `com.tastyhouse.core`)이었으며, `application/` 계층(서비스·DTO)을 소비 모듈과 infrastructure-module로 해체하면서 `domain-module`(패키지 `com.tastyhouse.domain`)로 리네이밍되었다. 전환 기록은 루트 `AGENTS.md`와 `tasks/README.md` 참고.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library`, **production 의존 0개**(Lombok까지 제거 — 접근자·생성자는 전부 수기 작성). QueryDSL(`querydsl-core`/`querydsl-apt`·sourceSets/generated 블록)·`spring-tx`·`spring-orm` 의존 **전부 제거됨** — api 모듈로의 `com.querydsl.*` 전이를 원천 차단하는 지점이다. **루트 `build.gradle`의 spring 주입 `subprojects` 블록에서도 제외**되어 컴파일 클래스패스에 `org.springframework.*`가 없다(순수성 컴파일 게이트). `org.springframework.boot` 플러그인 미적용 → `bootJar` 태스크 자체가 없으므로 `bootJar { enabled = false }`를 쓰면 스크립트 평가 에러, 일반 `jar`만 생성 |
| `src/main/resources/` | 모듈 공용 리소스 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/domain/` | DDD 도메인 루트 — 22개 Bounded Context + `shared`/`exception` (see `src/main/java/com/tastyhouse/domain/AGENTS.md`) |
| `src/test/` | 도메인 순수 단위 테스트(스프링 컨텍스트·DB 불필요) |

## For AI Agents

### Working In This Directory
- **프레임워크 import 금지**: `org.springframework.*`(`@Transactional`/`@Service`/`@Component` 포함)·`jakarta.persistence.*`·`com.querydsl.*`를 이 모듈에 추가하지 않는다. build.gradle에 해당 의존이 아예 없으므로 추가하려면 컴파일이 깨진다 — 필요한 관심사는 `infrastructure:persistence`로 보낸다. HTTP 상태는 `exception/ErrorCode`의 `httpStatusCode`(int)로만 표현한다.
- **`@Entity`는 이 모듈에 없다**: 도메인 모델은 전 도메인(22개) 순수 POJO이며, JPA 엔티티(`XxxJpaEntity`)·매퍼(`XxxMapper`)·`XxxRepositoryImpl`·`AttributeConverter`·`BaseEntity`는 전부 `infrastructure:persistence`(`com.tastyhouse.infrastructure.<ctx>.persistence`)에 있다. 외부 애그리거트 참조는 ID VO(`MemberId` 등)로 하고, 자식 애그리거트도 별도 Repository로 분리한다(JPA 연관관계 매핑 자체가 이 모듈에 존재할 수 없다).
- **도메인 모델 규칙**:
  - 신규 생성 `of(...)`(또는 `create(...)`/`register(...)`)와 DB 재구성 전용 `reconstitute(id, ..., createdAt, updatedAt)` 두 팩토리만 공개한다. `reconstitute`는 인프라(매퍼)만 호출하며(불변식 우회 방지, Javadoc 명시), `id`는 미영속이면 null이다. Java 계층에 생성 경로가 없는 read-only 애그리거트는 `reconstitute`만 둔다(reference: `shop/model/ProhibitedWord`). 조회 전용이고 도메인 불변식도 없는 데이터는 애그리거트를 두지 않고 infra `<ctx>/query/`의 Result DTO로만 노출한다(reference: `search`의 추천 검색어 — 도메인 모델 없이 `infrastructure/search/query/RecommendedKeywordResult`만 존재).
  - **재대입되지 않는 필드는 `final`로 선언**한다. `@Entity`와 달리 순수 POJO는 JPA 프록시/리플렉션 제약이 없으므로, 생성자(팩토리) 이후 상태전이로 바뀌지 않는 필드는 `id`뿐 아니라 상태 필드까지 모두 `final`로 둔다(전이되는 필드만 non-final). 불변성을 컴파일러가 강제하고 IntelliJ `may be 'final'` 경고를 차단한다. reference: `admin`의 `Admin`(update 경로 없어 전 필드 `final`).
  - **`@Embedded` 대상 VO는 Java `record`로 선언**한다(검증은 compact constructor). Hibernate 6이 `@Embedded` 값 객체를 canonical 생성자로 인스턴스화할 수 있어야 하므로 일반 class + 검증 생성자는 런타임 `InstantiationException`을 유발한다. 접근자는 record accessor(`value()`)로 통일하고 `toString()` 오버라이드는 남기지 않는다. 컬럼 매핑은 이 모듈이 아니라 각 `XxxJpaEntity`의 `@AttributeOverride`가 소유한다. reference: `shared/vo/PhoneNumber`, `shared/vo/VerificationCode`, `product/vo/ProductDiscountInfo`.
- **Repository 인터페이스는 write 포트만 둔다**: `findById`/`save`/`saveAndFlush`/`delete`/`existsByX`(중복 검증)/`findByNaturalKey`/검증용 `countByX`/락 획득용 조회처럼 **불변식 검증·상태 전이에 필요한** 조회만 남긴다. Result DTO·`PageResult` 반환, 조인 투영, 목록·검색·페이징 등 **표현 목적 조회는 이 모듈에 두지 않고** `infrastructure:persistence`의 `<ctx>/query/`(`{도메인}QueryDao`)가 소유한다. 판정 기준: "이 조회가 없으면 불변식 검증이나 상태 전이가 불가능한가?"
- **도메인 서비스(`<ctx>/service/`)는 순수 POJO**다: `@Service`/`@Component`/`@Transactional`을 붙이지 않고, 빈 등록은 `infrastructure:persistence`의 컨텍스트별 `<ctx>/config/<Ctx>DomainConfig`가 `@Bean` 팩토리로 수행한다(없으면 신설). 트랜잭션 경계는 이를 호출하는 api 모듈의 `{도메인}CommandService`(`@Transactional`)가 소유한다. 한 트랜잭션에서 2개 이상 애그리거트 타입을 load & save하는 불변식 오케스트레이션(reference: `order/service/OrderPlacementService`, `payment/service/PaymentConfirmationService`, `point/service/PointLedgerService`)과 무상태 정책·검증기(reference: `faq/service/FaqCategoryDeletionPolicy`, `shop/service/ProhibitedWordValidator`)가 여기 산다 — 소비 모듈로 복제하지 않는다.
- **명시적 save 규칙**: 도메인 모델은 POJO이므로 JPA 더티 체킹으로 자동 flush되지 않는다. 도메인을 변경한 뒤 **반드시 `repository.save(domain)`을 호출**한다(누락 시 변경이 조용히 유실된다). 이 책임은 도메인 서비스와 api 모듈의 `{도메인}CommandService` 양쪽에 있다.
- **출력 포트는 `<ctx>/port/`에 둔다**: 외부 시스템을 도메인이 인터페이스로 선언하고 `infrastructure:external`이 구현한다(`file/port/FileStoragePort`, `payment/port/PgPaymentGateway`, `mail/port/MailSender`, `sms/port/SmsSender`, `product/port/ProductReviewStatisticsPort`, `rank/port/MemberReviewCountPort`). 이벤트 발행 포트는 `shared/event/DomainEventPublisher`이며 스프링 구현은 infrastructure-module의 `SpringDomainEventPublisher`다.
- **낙관적 락 충돌은 `shared/exception/OptimisticLockConflictException`으로 표현**한다. 스프링의 `ObjectOptimisticLockingFailureException`을 이 예외로 번역하는 책임은 `infrastructure:persistence`의 `RepositoryImpl`에 있고, 재시도 루프는 소비 모듈에 둔다(상세는 루트 CLAUDE.md "낙관적 락 재시도 배치 규칙").
- **command 파라미터는 원시 타입 또는 도메인 타입으로 받는다**: presentation의 Request 타입을 인자로 받는 팩토리·메서드를 두지 않는다(레이어 역전 방지). HTTP 경계는 `String`/`Long`으로 받고 api 모듈 서비스에서 `Enum.from(String)`·`XxxId.of(Long)`으로 승격한 뒤 이 모듈에 전달한다.
- **조회 결과 DTO를 `com.tastyhouse.domain..` 안에 두지 않는다**: Result record와 `SearchCondition`은 도메인 모델이 아니다. **읽기 계약은 전부 `application` 모듈의 `com.tastyhouse.application.<ctx>.port.out`이 소유한다** — 이 모듈에는 두지 않는다. 한때 다중 앱 공유분 55개를 이 모듈이 갖고 있었으나(모듈 재편 챕터 05), application 모듈이 하나로 통합되며 근거였던 앱 간 수평 의존 회피가 무의미해져 의존성 정리 챕터 04에서 되돌렸다. 접미어 `Result` 통일·`Dto` 금지·admin 충돌 시 `Management` 한정어 규칙은 위치와 무관하게 적용된다.
- **QueryDSL 동적 where 조립 규칙은 이 모듈 소관이 아니다**: `BooleanExpression` varargs 헬퍼 패턴은 QueryDSL을 소유한 `infrastructure:persistence`(`<ctx>/query/`의 QueryDao)의 규칙이다 — `infrastructure-module/AGENTS.md` 참고.

### Testing Requirements
- **순수 단위 테스트**가 원칙이다: 도메인 모델·도메인 서비스는 프레임워크 의존이 없으므로 스프링 컨텍스트나 DB 없이 JUnit만으로 불변식·상태전이를 검증한다(reference: `notice/model/NoticeTest` 등 도메인별 `XxxTest`).
- 새 애그리거트·상태전이를 추가하면 대응 단위 테스트를 함께 추가한다.
- JPA 매핑 정합성(`ddl-auto=validate`)·enum `columnDefinition` 검증은 엔티티를 소유한 `infrastructure:persistence`의 책임이다.

### Common Patterns
- Repository write 포트: `<ctx>/repository/XxxRepository`(인터페이스) ← `infrastructure:persistence`의 `<ctx>/persistence/XxxRepositoryImpl`(구현). 저장 시맨틱은 load-copy-save(id null이면 insert, 있으면 managed 엔티티 조회 후 `Mapper.applyChanges` 복사 — detached merge 금지).
- ID 강타입: `<ctx>/vo/XxxId`(`record XxxId(Long value)` + compact constructor 검증 + 정적 팩토리 `of`). JPA 매핑용 `AttributeConverter`는 `infrastructure:persistence`에 있다.
- DomainEvent는 `<ctx>/event/`에 record로 정의하고, 발행은 `DomainEventPublisher` 포트를 통한다. 리스너는 `infrastructure:persistence`의 `<ctx>/listener/`에 둔다(특정 api 모듈에 두면 다른 모듈이 트리거할 때 누락된다).
- 공유 커널: `shared/vo/PhoneNumber`, `shared/model/ApprovalStatus`, `shared/page/PageQuery`·`PageResult`, `shared/event/DomainEventPublisher`, `shared/exception/OptimisticLockConflictException`.

## Dependencies

### Internal
- 의존 없음 — 가장 안쪽 레이어. 다른 모듈을 참조하지 않는다.

### External
- **없음** — production 의존이 0개다. getter·생성자는 Lombok이 아니라 수기로 작성한다(전 모듈 Lombok 제거 완료). `dependencyManagement`(BOM) 블록은 아래 테스트 의존의 버전 고정 용도로만 남아 있다
- 테스트: `junit-jupiter`, `assertj-core`, `archunit-junit5` — 실제로 쓰는 것만 선언한다. `spring-boot-starter-test`는 **의도적으로 제외**(도메인 테스트는 전부 순수 단위 테스트라 스프링 컨텍스트가 필요 없고, starter를 두면 테스트 클래스패스로 spring이 되돌아와 순수성 검증이 무뎌진다)

<!-- MANUAL: -->


<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# domain

## Purpose
모든 도메인의 핵심을 담는 라이브러리 모듈(`java-library`). 순수 POJO 도메인 모델(Aggregate Root), Value Object, DomainEvent, Repository **write 포트**, 도메인 서비스(불변식 오케스트레이션·무상태 정책), 외부 어댑터용 **출력 포트**를 포함한다.

**프레임워크를 전혀 모른다** — production 의존이 **하나도 없다**(Lombok까지 제거됨). Spring Web뿐 아니라 JPA·QueryDSL·`spring-tx`/`spring-orm`도 없으므로, `@Entity`/`@Transactional`/`@Service`/`@Component`/`com.querydsl.*`가 이 모듈에 단 한 곳도 없다. 예외의 HTTP 상태는 `int httpStatusCode`로, 낙관적 락 충돌은 `OptimisticLockConflictException`으로 표현한다. `web-api`/`admin-api`/`ceo-api`/`batch-module`/`infrastructure:persistence`/외부 연동 모듈(`infrastructure:{external,firebase,aws,oauth,payment,messaging,crawling}`)/`security-module`이 이 모듈에 의존한다(역방향 의존은 없다).

> 과거 `core-module`(패키지 `com.tastyhouse.core`)이었으며, `application/` 계층(서비스·DTO)을 소비 모듈과 infrastructure-module로 해체하면서 `domain`(패키지 `com.tastyhouse.domain`)로 리네이밍되었다. 전환 기록은 루트 `AGENTS.md`와 `tasks/README.md` 참고.

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
- **출력 포트는 `<ctx>/port/`에 둔다**: 외부 시스템을 도메인이 인터페이스로 선언하고 외부 연동 모듈이 기술별로 나눠 구현한다 — `file/port/FileStoragePort`는 `infrastructure:external`(코어의 `FileStoragePortAdapter`가 `FileStorageStrategy` 구현체에 위임), `payment/port/PgPaymentGateway`는 `infrastructure:payment`, `mail/port/MailSender`·`sms/port/SmsSender`는 `infrastructure:messaging`(S3/SES/SNS 대체 구현은 `infrastructure:aws`)이 구현한다. `product/port/ProductReviewStatisticsPort`·`rank/port/MemberReviewCountPort`는 `infrastructure:persistence` 소관이다. 이벤트 발행 포트는 `shared/event/DomainEventPublisher`이며 스프링 구현은 infrastructure-module의 `SpringDomainEventPublisher`다.
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


## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

이 절의 항목은 **코드의 특정 지점을 이렇게 바꾸지 말라는 금지 지시**다. 원문 주석은 챕터 03에서 제거되므로, 이 문서가 그 지시의 유일한 소재지다.

### `ErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND` 외 3건 — 이름과 상태코드 불일치 봉인

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/exception/ErrorCodeConventionTest.java`
→ `NOT_FOUND_NAME_WITH_NON_404_STATUS`

이름이 `*_NOT_FOUND`인데 404가 아닌 기존 상수들. 이미 프론트엔드가 분기하는 wire 계약(응답 status + code)이므로 지금 고치면 클라이언트가 깨진다. 교정 대상이 아니라 **봉인 대상**이다. **여기에 새 항목을 추가하지 말고, 신규 상수는 규약을 지킨다.**

봉인 구성원 4개 — 코드를 열지 않고 대조할 수 있도록 전부 열거한다.

- `ErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND`
- `ErrorCode.MAIL_VERIFICATION_CODE_NOT_FOUND`
- `ErrorCode.REFERRAL_REFERRER_NOT_FOUND`
- `ErrorCode.FOLLOW_NOT_FOUND`

**짝 테스트(노후 감지)**: `ErrorCodeConventionTest.whitelistIsNotStale()` — 봉인 목록의 상수가 404로 고쳐졌으면 실패해서 목록에서 지우라고 알린다. 봉인이 영구 면죄부가 되지 않게 하는 장치다.

### `ErrorCode` code 문자열이 상수명과 의도적으로 다른 6건 — 봉인

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/exception/ErrorCodeConventionTest.java`
→ `CODE_INTENTIONALLY_DIFFERS_FROM_NAME`

채널 도메인 어휘 통일(mail/sms)로 상수명은 `SMS_`·`MAIL_` 접두어로 대칭화했지만, 응답 `code` 문자열은 프론트가 분기하는 wire 계약이라 예전 값(`VERIFICATION_CODE_*`·`EMAIL_VERIFICATION_CODE_*`)을 유지했다. 루트 `CLAUDE.md`의 "채널 도메인 어휘 통일 규칙"에 명시된 **의도적 불일치이므로 교정 대상이 아니다.**

봉인 구성원 6개.

- `ErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND`
- `ErrorCode.SMS_VERIFICATION_CODE_EXPIRED`
- `ErrorCode.SMS_VERIFICATION_CODE_MISMATCH`
- `ErrorCode.MAIL_VERIFICATION_CODE_NOT_FOUND`
- `ErrorCode.MAIL_VERIFICATION_CODE_EXPIRED`
- `ErrorCode.MAIL_VERIFICATION_CODE_MISMATCH`

### 컨텍스트 경계 위반 16건 — 현상 동결 봉인

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/architecture/ContextBoundaryTest.java`
→ `SEALED_VIOLATIONS`

domain에는 25개 바운디드 컨텍스트가 한 모듈에 공존한다. 컨텍스트 간 참조는 **ID VO(`<ctx>.vo..`)·도메인 이벤트(`<ctx>.event..`)·출력 포트(`<ctx>.port..`)** 셋으로만 허용하고, 타 컨텍스트의 `model..`/`repository..`/`service..` 직접 import는 금지한다. `shared..`·`exception..`은 컨텍스트가 아니라 전 컨텍스트 공용이므로 전면 허용한다.

**기존 위반은 고치지 않고 봉인한다.** 이 단계의 목표는 전면 재설계가 아니라 "현상 동결 + 신규 위반 차단"이며, 실제 결합 해소는 후속 단계가 담당한다. **이 목록은 줄어들기만 해야 한다 — 항목을 추가하는 것은 새 위반을 승인하는 것이므로 금지한다.** 위반을 해소했다면 그 클래스를 목록에서 지운다.

봉인 구성원 16개.

- `com.tastyhouse.domain.mail.service.MailVerificationService`
- `com.tastyhouse.domain.member.service.MemberDeliveryAddressService`
- `com.tastyhouse.domain.order.service.OrderPlacementService`
- `com.tastyhouse.domain.payment.service.PaymentCancellationService`
- `com.tastyhouse.domain.payment.service.PaymentConfirmationService`
- `com.tastyhouse.domain.reservation.service.ReservationBookingService`
- `com.tastyhouse.domain.review.service.ReviewBlindRequestService`
- `com.tastyhouse.domain.review.service.ReviewLifecycleService`
- `com.tastyhouse.domain.review.service.ReviewOwnerReplyService`
- `com.tastyhouse.domain.shop.service.DeliveryAreaProjection`
- `com.tastyhouse.domain.shop.service.ShopCeoAssignmentService`
- `com.tastyhouse.domain.shop.service.ShopDeliveryAreaPolygonService`
- `com.tastyhouse.domain.shop.service.ShopDeliveryAreaRadiusService`
- `com.tastyhouse.domain.shop.service.ShopDeliveryAreaService`
- `com.tastyhouse.domain.shop.service.ShopDeliveryTipService`
- `com.tastyhouse.domain.shop.service.ShopRequestCancelService`

**짝 테스트 2종**.

- `ContextBoundaryTest.sealedViolationsShouldNotBeStale()` — 목록에 있으나 더 이상 위반하지 않는 클래스가 있으면 실패해 지우라고 알린다.
- `ContextBoundaryTest.sealedViolationListShouldNotBeEmpty()` — 목록이 비면 실패해서 **봉인 장치 자체(`SEALED_VIOLATIONS`·짝 테스트)를 제거하고 규칙을 순수 강제로 전환하라**고 알린다. 목록이 비면 위 짝 테스트가 검사 대상을 잃어 공허하게 통과하기 때문이다.

**모든 규칙은 `allowEmptyShould(true)` 없이 선언한다**(공허 통과 금지 — `DomainPurityTest`·`LayerRulesTest` 개정 선례).

### 컨텍스트 간 순환 성분 1건 — 현상 동결 봉인

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/architecture/ContextBoundaryTest.java`
→ `SEALED_CYCLES`

봉인 구성원은 `"order,product,review,shop"` 1건이다.

**쌍이 아니라 강결합 성분(SCC) 단위로 봉인한다.** `SliceRule#beFreeOfCycles`는 2노드 상호 참조뿐 아니라 `order → product → shop → order` 같은 *전이 순환*까지 잡으므로, 봉인 목록도 같은 단위여야 한다. 쌍으로 적으면 두 모델이 어긋나 "쌍 하나를 지우라"는 짝 테스트의 지시를 따랐을 때 정작 전이 순환이 드러나 규칙이 깨진다(실제로 `product`는 이 4-노드 성분에 속하는데 쌍 표기로는 이름이 등장하지 않았다). 항목 형태는 성분에 속한 컨텍스트를 알파벳 오름차순으로 이은 `"a,b,c"`다.

**봉인 성분 "안쪽" 의존만 제외한다** — 양 끝이 모두 같은 성분에 속할 때만 무시하므로, 봉인 컨텍스트가 관여하더라도 성분 밖으로 나가는 의존(예: `order→member`)은 그대로 검사된다. 성분에 걸린 컨텍스트를 통째로 무시하면 무관한 신규 순환까지 함께 가려진다.

**짝 테스트**: `ContextBoundaryTest.sealedCyclesShouldNotBeStale()` — 비교 단위가 `SliceRule`과 동일한 **SCC**여야 한다. 2노드 쌍으로 비교하면 전이 순환을 놓쳐, 봉인을 지우라고 지시해 놓고 정작 규칙은 깨지는 모순이 생긴다.

### `ReplyPhraseTextValidator` — 포트 carve-out (금칙어 검증기를 직접 부르지 않는다)

**대상**: `backend/domain/src/main/java/com/tastyhouse/domain/ceo/port/ReplyPhraseTextValidator.java`

실제 검수 규칙(금칙어 목록 대조)은 shop 컨텍스트의 `ProhibitedWordValidator`가 소유하고, 이 포트의 어댑터가 그것을 그대로 호출한다 — **규칙을 복제하지 않는다.**

**ceo 도메인이 `ProhibitedWordValidator`를 직접 부르지 않는 이유는 컨텍스트 경계다.** 컨텍스트 간 참조는 ID VO·도메인 이벤트·출력 포트로만 허용되고 타 컨텍스트의 `service` 직접 import는 금지되어 있다(`ContextBoundaryTest`). **기존에 같은 검증기를 직접 import하는 도메인 서비스들이 있으나 그것은 규칙 도입 이전 코드로 봉인된 것이라 선례로 삼지 않는다.**

위반 시 `BusinessException(SHOP_TEXT_PROHIBITED_WORD)`(400)을 던진다.

### `StorePriceVerificationPort` — CQRS write 포트 잔류 carve-out

**대상**: `backend/domain/src/main/java/com/tastyhouse/domain/product/port/StorePriceVerificationPort.java`

메뉴 가격 저장은 product 컨텍스트의 규칙이지만, "매장가·픽업가를 설정할 수 있는가"와 "배달가가 매장가를 넘어 인증을 내려야 하는가"는 **가게 단위 상태**다. 컨텍스트 경계 규칙(`ContextBoundaryTest`)이 타 컨텍스트의 `model`·`repository` 직접 import를 금지하므로, product는 이 포트로만 그 상태를 다룬다 — **`ShopRepository`를 직접 주입하면 신규 위반이 되고 봉인 목록은 늘릴 수 없다.**

구현은 `StorePriceVerificationAdapter`가 `ShopRepository`에 위임한다.

### `StorePriceVerificationService` — 애그리거트를 product가 소유하는 배치 (위 포트와 짝)

**대상**: `backend/domain/src/main/java/com/tastyhouse/domain/product/service/StorePriceVerificationService.java`

**왜 shop이 아니라 product 컨텍스트가 소유하는가**: 승인이 하는 일의 본체는 `PRODUCT_PRICE`의 매장가·픽업가를 채우는 것이다. 요청 애그리거트를 shop에 두면 그 승인 경로가 `product.model`·`product.repository`를 import해야 해 컨텍스트 경계 규칙(`ContextBoundaryTest`)을 위반하는데, **그 봉인 목록은 늘릴 수 없다.** 그래서 인증 요청 애그리거트 자체를 product가 소유하고, 가게 단위 상태인 인증 ON/OFF 플래그만 `StorePriceVerificationPort`로 다룬다 — **이 방향이 경계 위반 없이 성립하는 유일한 배치다.**

함께 고정되는 제약.

- 테이블명이 `SHOP_STORE_PRICE_VERIFICATION`인 것은 요청이 **가게 단위**로 접수되기 때문이며, 소유 컨텍스트와는 별개다.
- **승인은 요청 시점의 매장가를 쓴다** — 항목(`StorePriceVerificationItem`)에 박제된 값이며, 승인 시점에 현재 가격을 다시 읽지 않는다. 그러지 않으면 검수자가 보지 않은 값이 승인된다.
- **인증을 켜기 전에 항목을 먼저 반영한다.** 순서를 뒤집으면 반영 도중 실패했을 때 인증만 켜진 채 매장가가 비어 있는 상태가 남는다.
- 상태 전이는 원본 전이와 **같은 트랜잭션**에서 인덱스에 동기 기록한다. **전이 메서드마다 이 호출을 넣는다** — 한 곳이라도 빠지면 점주 화면의 요청처리 현황이 원본과 영구히 어긋난다. **배선이 api 모듈이 아니라 이 도메인 서비스에 있는 것이 중요하다** — 승인·반려는 admin이, 취소는 ceo가 호출하므로 api 모듈에 두면 같은 전이가 두 모듈로 흩어져 한쪽이 반드시 빠진다.
- 인증 상태 → 통합 상태 매핑을 product가 소유하는 이유도 컨텍스트 경계다 — shop이 `StorePriceVerificationStatus`(product 소유)를 알면 `ContextBoundaryTest`를 위반한다. 다섯 상태가 이름까지 대응하지만 `name()`을 그대로 흘려보내지 않고 **명시 매핑**을 두는 이유는, 어느 한쪽 enum에 상수가 추가되면 **컴파일 단계에서** 대응을 결정하도록 강제하기 위해서다.
- 할인 중인 메뉴는 인증 요청 대상이 아니다(승인 시 매장가가 할인가와 뒤엉킨다). 판정식은 `ProductPriceService`와 같다 — 이 저장소에 할인 스케줄링이 없어 "할인가 존재"로 본다.
- 가격 행이 정말 그 메뉴의 것인지 확인한다. **확인하지 않으면 남의 메뉴 가격 행에 매장가를 심을 수 있다.**

### `ReviewBlindStatus` — 공용 `ApprovalStatus`에 상수를 추가하지 않는다

**대상**: `backend/domain/src/main/java/com/tastyhouse/domain/review/model/ReviewBlindStatus.java`

**공용 `ApprovalStatus`에 상수를 추가하지 않고 별도 enum을 둔다.** 그 enum은 `ShopImageChangeRequest`·`ShopDeliveryAreaAdjustmentRequest` 등이 공유하므로, 리뷰에만 의미가 있는 `EXPIRED`/`DELETED`를 넣으면 이미지 검수 코드가 도달 불가능한 분기를 갖게 된다. `ApprovalStatus` 문서의 *"도메인 특화 승인상태가 필요하면 그 도메인 enum에서 이 enum을 감싸거나 별도로 정의한다"* 가 이 경우다.

앞의 네 상수는 `ApprovalStatus`와 이름·의미가 그대로 대응하고, 뒤의 둘은 승인 이후의 생애주기(30일 경과 재노출 / 고객 동의 삭제)를 나타낸다.

### `ShopReviewDisplaySetting` — `Shop`에 컬럼을 추가하지 않는다

**대상**: `backend/domain/src/main/java/com/tastyhouse/domain/review/model/ShopReviewDisplaySetting.java`

**`Shop`에 컬럼을 추가하지 않는다** — 리뷰 표시 설정이 앞으로 늘어날 여지가 있고 `Shop`은 이미 필드가 19개다.

- 설정 행이 없으면 `ReviewSortType.LATEST`로 간주한다. 행을 미리 만들지 않으므로 기존 가게에 대한 백필이 필요 없다.
- 이 설정은 고객이 정렬을 직접 지정하지 **않았을 때만** 적용된다 — **점주 설정이 고객의 명시적 선택을 덮어써서는 안 된다.**
- DB 재구성 팩토리는 영속 계층 전용이며, **불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만** 식별자·감사 시각을 주입한다.

### 도메인 서비스 단위 테스트 10종 — 불변식 봉인 (테스트 스텁 carve-out)

아래 테스트들은 **현재 동작을 봉인**하는 것이 목적이다. 리팩터링으로 테스트가 깨지면 테스트를 고치기 전에 **봉인된 불변식을 깼는지 먼저 확인한다.** 전부 write 포트·이벤트 발행 포트를 fake로 대체해 Spring/DB 없이 판정 로직만 검증하는 순수 단위 테스트다.

| 대상 (`backend/domain/src/test/java/com/tastyhouse/domain/...`) | 봉인하는 불변식 |
|---|---|
| `menureview/service/MenuReviewLifecycleServiceTest.java` → `register_succeedsWithoutStoreReview` | **매장 리뷰가 없어도 메뉴 평가가 등록된다**(설계 원칙 1의 회귀 방어). 이 서비스가 `ReviewRepository`를 **아예 주입받지 않는 것 자체가** 그 원칙의 구조적 보증이며, 테스트는 그 상태를 봉인한다 |
| `product/service/OrderProductValidationServiceTest.java` | 필수 옵션그룹을 비운 주문, 숨긴·품절 옵션을 실은 주문 차단. 전부 "프론트만 막고 서버는 통과시키던" 결함이다. 3단계 보증금이 도입되면 후자는 "보증금 옵션을 숨겨 보증금 없이 주문"하는 경로가 되므로, 이 테스트가 그 우회를 **영구히 봉인한다** |
| `product/service/ProductRepresentativeApprovalServiceTest.java` | 세 제약(최대 6개 · 이미지 필수 · 최소 1개 유지). 특히 **최소 1개 유지가 기존 `PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE`를 재사용**하는 것이 핵심 — 새 코드로 갈라지면 같은 불변식에 프론트가 두 갈래를 분기해야 하고 일괄 숨김 경로와 하한이 어긋난다 |
| `review/service/ReviewBlindRequestServiceTest.java` | 스펙의 세 규칙 — **1회 제한**(단 `CANCELED`는 예외) · **고객 동의 삭제** · **타인 리뷰 접근 차단**. 추가로 `IndexSync` 중첩 클래스가 신규 전이 2종(`EXPIRED`/`DELETED`)이 종결(`APPROVED`)로 접히는지 봉인한다 — 목록에 "재노출"·"삭제"라는 없는 통합 상태가 새어 나가면 안 된다. 원본→통합 상태 매핑은 컨텍스트 경계 때문에 recorder가 아니라 이 서비스가 소유한다 |
| `review/service/ReviewOwnerReplyServiceTest.java` | **30일 작성 제한이 등록에만 걸리는지**를 봉인한다. 기한 판정 기준일을 파라미터로 받는 설계 덕에 시계 조작 없이 29·30·31일차를 지정할 수 있다 — **도메인이 `LocalDate.now()`를 직접 부르면 이 테스트 자체가 불가능하다** |
| `shop/service/ShopCeoAssignmentServiceTest.java` | `ShopCeoAssignmentService`의 상태 규칙 표 전체. 특히 **재배정이 `REVOKE`+`GRANT` 2행**인 것을 봉인한다 — 한 행에 before/after를 담는 형태로 되돌아가면 "언제부터 언제까지 권한이 있었는가"를 읽을 수 없게 된다 |
| `shop/service/ShopLifecycleServiceTest.java` | 가게 등록 시 접근권한 이력 기록. 등록에서 점주를 함께 배정하는 것도 접근권한 부여이므로 나중에 배정한 경우와 **구별 없이 `GRANT` 이력이 남아야** 하고, 반대로 점주 없이 등록하면 아무 행도 남지 않아야 한다 |
| `shop/service/ShopMenuCollectionImageServiceTest.java` | 규칙이 전부 **행 하나만 보고는 판정할 수 없는 집합 차원**이라 애그리거트 단위 테스트로는 한 줄도 검증되지 않는다. **정원(최대 6개)은 상태를 가리지 않는다**(대기·반려 건도 슬롯을 차지한다), **순서 변경은 replace-all**(부분·초과·미지의 id 목록은 전부 거절 — 부분 목록을 받아주면 낡은 화면의 요청이 빠진 이미지를 목록 끝으로 밀어낸다). 가게 소유가 아닌 id는 존재를 알리지 않고 `SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND`로 합쳐 **IDOR을 막는 경로도 함께 봉인**한다 |
| `shop/service/ShopRequestCancelServiceTest.java` | 세 규칙 — (1) `PENDING`만 취소된다, (2) `IN_PROGRESS`는 409로 거부된다(가맹본부에 자료가 전달된 뒤라 플랫폼이 일방 취소할 수 없다), (3) **취소는 원본 애그리거트의 상태를 바꾼다**. (3)이 핵심으로, 인덱스에만 `CANCELED`를 두면 원본이 `PENDING`으로 남아 중복 차단이 재요청을 계속 막고 관리자가 취소된 요청을 승인·반려할 수 있다 |
| `shop/service/ShopRequestIndexRecorderTest.java` | 원본 → 통합 상태 **매핑 표를 전수** 봉인한다. 특히 조정 신청의 `COMPLETED → APPROVED`는 유일하게 값 이름이 어긋나는 매핑이라, 고정하지 않으면 목록에 "완료"라는 없는 상태가 새어 나가거나 매핑이 조용히 뒤집힌다. 게시중단만은 **통합 상태를 그대로 받는다** — 컨텍스트 경계 때문에 recorder가 `review.model.ReviewBlindStatus`를 import할 수 없어 매핑을 `ReviewBlindRequestService`가 소유하기 때문이다 |

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

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거. 챕터 03(domain 모듈) 이관분 -->

이 절은 `domain` 각 컨텍스트의 java 주석에 있던 **설계 근거**를 옮긴 것이다. 비즈니스 규칙(분류 C)은 `docs/domain/{도메인}.md`에 있으므로 여기 중복 기술하지 않고 링크한다.

### 모듈 전역 — 프레임워크-프리와 명시적 저장

**대상**: `domain` 전 컨텍스트의 model·service

도메인 모델은 JPA/Spring에 의존하지 않는 POJO다. 영속화는 `infrastructure:persistence`의 `{X}JpaEntity` + `{X}Mapper`가 담당한다. **POJO라 더티 체킹이 없으므로, 변경 후에는 command 서비스가 명시적으로 `{X}Repository#save`를 호출해야 한다** — 이것을 빠뜨리면 변경이 조용히 유실된다.

도메인 서비스는 `@Service`/`@Transactional` 없는 순수 POJO이며, **빈 등록은 `ShopDomainConfig` 등 config가 담당하고 트랜잭션 경계는 각 api 앱의 `CommandService`가 선언한다.**

도메인은 인증을 모르므로, **변경 주체(`ShopChangeActor` 등)는 마지막 파라미터로 명시 전달받는다.** 도메인이 `LocalDate.now()`를 직접 부르지 않는 것도 같은 이유다 — 기준일을 파라미터로 받아야 시계 조작 없이 경계값을 테스트할 수 있다.

### 배달팁·최소주문금액 규격은 외부 가이드가 강제한다 — 내부 판단으로 완화 금지

**대상**: `shop/service/ShopDeliveryTipService.java`, `shop/model/ShopDeliveryTipTier.java`·`ShopDeliveryTipHoliday.java`·`Shop.java` → `validateMinOrderAmount`, `shared/geo/GeoDistance.java` → `distanceMeters`

아래 규격은 이 저장소가 정한 비즈니스 정책이 아니라 **외부 배달 플랫폼 가이드가 강제하는 요건**이다. 원문 주석이 "배민 가이드 강제 규격"·"배민 가이드 원문"으로 출처를 밝히고 있었으므로, **불합리해 보여도 내부 판단으로 완화·재협상하지 말고 그 가이드가 실제로 바뀌었는지부터 확인한다.**

- 구간별 배달팁 상한 **5,000원 미만**(5,000원 자체 불가)
- 구간 단조성 — **주문금액이 오르면 배달팁은 내려가야 한다**
- 거리별 ↔ 지역별 **상호 배타**
- 공휴일 팁은 날짜별이 아니라 **가게당 단일 금액**("법정 공휴일에 일괄 부과")
- 거리 기준은 도로 경로가 아니라 **직선거리**
- 최소주문금액은 **픽업(포장)에 적용하지 않는다**

규칙 본문은 [`docs/domain/shop.md` § 배달팁 · § 최소주문금액](../../docs/domain/shop.md)에 있고, 이 절은 **그 출처가 외부라는 사실**만 담는다 — 그 사실이 사라지면 후임자가 평범한 내부 규칙으로 오인해 바꾸게 된다.

### 도메인 서비스에 남는 것과 애그리거트에 남는 것의 경계

**대상**: `shop/service/ShopDeliveryTipService.java`, `shop/service/ShopBusinessHourService.java`

도메인 서비스에 두는 것은 **행 하나만 보고는 판정할 수 없는 규칙**뿐이다.

- 집합의 개수·정렬·단조성 (구간별 배달팁의 "3개 이하 + 주문금액 오름차순 + 팁 내림차순")
- 두 리소스에 걸친 상호 배타 (거리별 ↔ 지역별)
- 다른 애그리거트 컬렉션을 읽어야 판정되는 것 (지역별 팁의 행정동이 가게 배달가능지역에 속하는지)
- 집합 관계 (같은 요일 시간대 겹침)

**행 하나의 값 불변식(금액 범위·시각 유효성)은 각 애그리거트의 `of`·`update`가 강제한다.** 서비스에 두면 팩토리를 직접 부르는 경로(배치·마이그레이션)가 규격을 우회할 수 있기 때문이다.

### replace-all 교체와 변경이력 1행 규칙

**대상**: `shop/service/ShopDeliveryTipService.java` → `replaceTiers`·`replaceRegionTips`·`replaceScheduleTips`

**배달팁 컬렉션은 전부 replace-all로 교체한다.** 위 규칙들이 집합 전체를 봐야 판정되므로, 행 단위 CRUD면 어떤 순서로 조작해도 중간 상태가 규칙을 위반한다. (`ShopBusinessHour`가 개별 CRUD인 것은 요일 간에 이런 관계가 없기 때문이다.)

**변경이력 기록도 이 서비스가 소유한다**(`DELIVERY_TIP_TIER`·`DELIVERY_TIP_DISTANCE`·`DELIVERY_TIP_REGION`·`DELIVERY_TIP_SCHEDULE`·`DELIVERY_TIP_HOLIDAY`). 이 서비스가 replace-all을 하려고 **삭제 전에** 컬렉션을 읽을 수 있는 유일한 지점이고, ceo-api의 `CommandService`는 CQRS 교차 주입 금지로 QueryDao를 주입할 수 없어 변경 전 값을 구조적으로 볼 수 없다.

**replace-all은 컬렉션 1행당 이력을 남기지 않고 저장 1회당 1행만 남긴다.** `deleteAll + saveAll`로 교체하므로 PK 기반 diff가 불가능하고, 행 단위로 남기면 이력 목록이 "점주가 저장한 횟수"가 아니라 "바뀐 행 수"로 페이징되어 읽을 수 없게 된다. 그래서 변경 전·후 컬렉션 전체를 `ShopChangeValueFormatter#snapshot`으로 요약해 한 행에 담는다.

이 규칙에서 파생되는 개별 판단들:

- **`tier_order`는 호출부가 보낸 순서가 아니라 정렬 후 재부여한다** — 그래야 저장된 순서와 금액 정렬이 어긋나지 않고, 화면에 보이는 순서와 이력의 순서가 일치한다.
- **`clearDistanceTip`은 설정 헤더가 없으면 이력도 남기지 않는다** — 애초에 거리별을 쓰지 않던 가게에 "해제했다"고 기록하면 일어나지 않은 변경이 이력에 남는다.
- **`clearRegionTips`는 전용 `DELETE` 행을 만들지 않고 빈 컬렉션으로의 교체로 기록한다** — 이 경로가 빈 배열 PUT과 완전히 같은 연산이라, 구분하면 같은 결과가 두 형태로 기록된다.
- **`changeHolidayTip`은 0원(삭제)도 `UPDATE` 한 행으로 남긴다** — 이 엔드포인트는 스칼라 하나를 설정하는 경로이고 0원은 그 스칼라의 유효한 값(미설정)이라, 같은 저장 버튼이 금액에 따라 `UPDATE`/`DELETE`로 갈리면 이력 목록에서 같은 조작이 두 종류로 보인다.
- **지역별 팁 스냅샷에서 마스터에 없는 행정동(폐지 동 등)은 식별자를 그대로 노출한다** — 행을 통째로 빠뜨리면 "그때 무엇이 설정돼 있었는가"가 부정확해진다.
- **`loadOrCreateSetting`은 그 시점에 저장하지 않는다** — 호출부가 전환을 마친 뒤 한 번만 저장해야 "만들었지만 전환에 실패한" 빈 헤더가 남지 않는다.
- **빈 목록으로 지역별을 교체할 때, 거리별을 쓰던 가게의 설정은 건드리지 않는다** — 지역별을 비우는 요청이 거리별 설정을 조용히 지우면 안 된다.

### 순수 계산기 형태 (리포지토리 0개·상태 0개)

**대상**: `shop/service/ShopDeliveryTipCalculator.java`, `shop/service/ShopOperatingStatusCalculator.java`

두 계산기는 **리포지토리 주입 0개, 인스턴스 상태 0개**다. 좌표→거리 변환과 날짜→공휴일 판정은 호출부가 끝내고 `ShopDeliveryTipContext`에 **이미 해석된 값**으로 담아 넘기므로, 계산기는 Spring·DB·시계 없이 단위 테스트할 수 있다. 이 형태를 깨고 리포지토리를 주입하면 그 테스트 가능성이 사라진다.

배달팁 항목 간 우선순위(거리별↔지역별 배타, 공휴일 > 시간별 **대체**, 시간별은 구체성 우선으로 하나만)는 비즈니스 규칙이므로 [`docs/domain/shop.md` § 배달팁](../../docs/domain/shop.md)에 있다.

### 시간 구간 겹침 판정 — 자정 넘김 분할

**대상**: `shop/service/ShopDeliveryTipService.java` → `validateScheduleOverlap`·`toSegments`, `shop/service/ShopBusinessHourService.java`

자정을 넘기는 구간은 `[start, 24:00)`과 `[00:00, end)` **두 조각으로 나눠 판정한다.** 그러지 않으면 22:00~02:00과 01:00~03:00처럼 실제로 겹치는 쌍을 놓친다. 두 서비스가 같은 기법을 쓴다.

**서로 다른 요일 구분끼리는 겹침을 검사하지 않는다** — 겹쳐도 적용 시점에 구체성 우선으로 하나만 선택되므로 이중 부과가 생기지 않고, 오히려 DAILY 기본값 위에 특정 요일을 덧씌우는 정상적인 설정 방식이기 때문이다.

### 점주가 켤 수 없고 admin·시스템만 바꾸는 플래그

**대상**: `shop/model/Shop.java` → `cupDepositEnabled`, `storePriceVerified`

**`cupDepositEnabled`는 점주의 영업 설정이 아니라 외부 규제 사실이다.** 환경부·자원순환보증금관리센터가 지역(제주·세종)과 사업자 규모로 지정하며, 지정·해제가 운영 이벤트로 발생한다. 그래서 점주가 스스로 켤 수 없고 **admin만 토글한다.** 주소 문자열로 지역을 파싱해 판정하지 않는 이유도 같다 — 같은 지역이어도 지정 사업자가 아닐 수 있고, 지정 여부는 주소에서 도출되는 값이 아니다.

**`storePriceVerified`는 '매장과 같은 가격' 뱃지 노출과 매장가·픽업가 설정 가능 여부를 함께 가른다.** 관리자가 가격표 이미지와 실제 매장을 대조해 승인할 때만 켜지며(`StorePriceVerificationService`), 메뉴 가격이 바뀌어 **배달가 > 매장가**가 되면 가격 저장 시점에 **동기**로 내려간다 — 배치로 미루면 그 사이 손님이 잘못된 뱃지를 본다.

### 부분실패 판정은 "요청 전체를 반영한 뒤의 최종 상태" 기준

**대상**: `product/service/ProductAvailabilityService.java`

메뉴·옵션의 품절·숨김 전이에서 **하나씩 순차로 검사하면 요청 배열의 순서에 따라 결과가 갈린다.** 노출 메뉴가 2개일 때 둘 다 숨김 요청하면 순차 검사는 첫 건을 통과시키고 두 번째만 실패시키는데, 어느 것이 통과할지가 배열 순서에 좌우된다. **최종 상태 기준이면 "노출 메뉴가 0개가 되므로 마지막 1개는 남긴다"는 판정이 결정적이다.**

이 제약들(노출 메뉴 ≥1 · 추천 메뉴 ≥1 · 옵션 `minSelect` 잔여 개수)이 api 모듈이 아니라 도메인에 있는 이유는 **애그리거트 불변식이고, ceo/admin 두 모듈에 흩어지면 한쪽만 고쳐지기 때문**이다.

**이 서비스는 shop 컨텍스트를 참조하지 않는다.** 품절 기간 기본값("익일 가게 오픈 시간") 산출은 `ShopNextOpenTimeCalculator`(shop 컨텍스트)가 담당하고, ceo-api의 command service가 두 서비스를 각각 주입해 조립한다 — `ShopBusinessHour`를 직접 참조하면 컨텍스트 경계 위반이다.

### `ProductPrice` — 별도 애그리거트인 이유와 `original_price` 이중화

**대상**: `product/model/ProductPrice.java`, `product/service/ProductPriceService.java`

**`PRODUCT`에 컬럼을 붙이지 않고 별도 애그리거트인 이유**: 컬럼 방식으로는 가격명(보통/곱빼기)을 표현할 수 없다. 한 메뉴가 가격명을 가진 여러 가격 행을 갖는 구조가 요구사항이므로 행으로 분리한다.

**기존 `PRODUCT.original_price`는 지우지 않는다.** 주문·검색·오늘의할인·목록 등 수십 곳이 그 컬럼을 읽고 있어 한 번에 걷어내면 회귀 범위가 통제 불가능하다. `sort=0` 행의 배달가를 그 컬럼에 동기화해 유지하므로, **가격 행이 1개뿐인 메뉴(대부분)는 기존 동작이 완전히 그대로다** — 이 이중화가 이 설계의 안전장치이며, 단일화는 후속 과제로 남긴다.

**채널별 가격은 의미가 서로 다르다** — `deliveryPrice`는 배달·테이블·예약 주문의 실제 결제 가격(상시 변경 가능), `storePrice`는 '매장과 같은 가격' 뱃지의 근거일 뿐 **결제에 쓰이지 않는 표시 전용**, `pickupPrice`는 포장(`TAKEOUT`) 주문의 결제 가격(미설정이면 배달가를 쓴다).

`storePrice`·`pickupPrice`는 **매장 가격 인증 승인 후에만** 설정할 수 있다. 그 판정은 가게 애그리거트를 함께 읽어야 하므로 모델이 아니라 `ProductPriceService`가 소유한다.

### 옵션그룹 합치기 — 기준 그룹 불변·흡수 그룹은 숨김

**대상**: `product/service/ProductOptionGroupMergeService.java`

- **기준 그룹은 손대지 않는다.** "기준 옵션그룹"이 곧 살아남는 정의다. 기준을 덮어쓰면 멱등성이 깨지고, 무엇보다 **과거 주문에 박제된 옵션을 조용히 바꾸게 된다.**
- **흡수 그룹은 행을 남긴 채 감춘다**(`hide()`) — `ORDER_PRODUCT_OPTION`이 `option_group_id`로 이 행을 참조하므로 **하드 삭제는 주문 이력을 끊는다.**
- **흡수 그룹의 옵션을 기준 그룹으로 재부모화(union)하지 않는다.** 합치기 확인 화면은 기준 그룹의 옵션 목록 **하나만** 보여주므로 union이면 중복된 합집합이 나와 화면이 약속한 것과 결과가 달라진다. 또한 추천 합치기는 옵션명·가격이 전부 같은 그룹만 제안하므로 union은 순수 중복 생성이다.
- **링크만 기준 그룹으로 옮긴다**(sort 보존 + 재정규화).

### 주문 접수 — 타 컨텍스트는 전부 소유 컨텍스트의 서비스를 경유한다

**대상**: `order/service/OrderPlacementService.java`

주문 한 건의 접수는 `Order` 헤더 · 상품 라인(`OrderProduct`) · 라인 옵션(`OrderProductOption`) 세 애그리거트를 한 트랜잭션에서 함께 만들고, 그 과정에서 계산한 금액을 헤더에 되반영해야 하는 **원자 연산**이다. 세 애그리거트 중 하나라도 빠지면 주문이 반쪽으로 저장되고, **금액 되반영이 빠지면 결제 금액이 0원인 주문이 남는다.** 쿠폰 사용·포인트 차감까지 같은 트랜잭션에 묶이는 크로스 애그리거트 불변식 오케스트레이션이라 도메인 계층에 둔다.

**타 컨텍스트는 전부 그 컨텍스트의 서비스를 경유한다** — 상품·옵션 검증은 `OrderProductValidationService`(product 소유), 가게 로드·주문가능·최소주문금액·배달지역·배달팁·예약슬롯은 `ShopOrderContextService`(shop 소유), 주문자 조회는 `OrdererLookupService`, 배달 주소 로드·소유권 검증은 `MemberDeliveryAddressService`(둘 다 member 소유)가 담당한다.

과거 이 서비스는 외부 컨텍스트 6개에서 모델·리포지토리를 직접 주입해 26개를 import했는데, 그러면 **각 컨텍스트의 규칙(판매중지 판정·배달지역 미등록 처리·주소 소유권)이 주문 안에서 재구현되어, 소유 컨텍스트가 정책을 바꿀 때 주문 경로만 낡은 규칙으로 남는다.** 지금 남은 타 컨텍스트 의존은 **서비스와 그 결과 record**뿐이며 모델·리포지토리 직접 import는 0건이다.

**이관해도 트랜잭션 경계는 그대로다** — 전부 같은 트랜잭션 안의 동기 호출이며(이벤트로 바꾸지 않는다), 검증 순서·에러코드·응답 계약도 이관 전과 동일하다.

주요 협력자의 존재 이유(생성자 `@param`에 있던 것):

- **`memberDeliveryAddressService`** — 배달 주소 로드와 소유권 검증. **좌표는 저장된 주소에서만 읽는다 — 위조 방지.**
- **`publicHolidayCalendar`** — 접수 시각의 공휴일 여부 판정. **shop이 이 캘린더를 직접 부르면 컨텍스트 경계를 위반하므로, 판정 결과만 배달팁 산출에 넘긴다.**

**주문 접수는 도메인 이벤트를 발행하지 않는다** — 과거 `OrderCreatedEvent`를 발행했으나 수신 리스너가 없는 no-op이어서 제거했다. 접수 이후 비동기 후처리(알림·집계)가 필요해지면 이 메서드 말미에 발행을 다시 추가하면 된다.

**저장 횟수**: 더티 체킹이 없으므로 헤더는 신규 저장 후 금액 갱신으로 **2회**, 상품 라인은 신규 저장 후 가격 갱신으로 **2회** 저장한다.

**반환은 생성된 주문의 식별자(`OrderId`)만이다** — 응답 조립(가게명·상품 라인·결제 요약)은 커밋 이후 소비 모듈의 `OrderQueryService`가 재조회해 담당한다(CQRS 분리).

### 리뷰 게시중단 — 상태 전이와 반영은 한 트랜잭션

**대상**: `review/service/ReviewBlindRequestService.java`

"점주 요청 → 관리자 심사 → 승인 시 리뷰 숨김 → 30일 뒤 재노출 또는 고객 동의 시 삭제" 워크플로의 규칙은 요청자(ceo)·심사자(admin)·고객(web)·배치가 서로 다른 액터임에도 동일하게 유지되어야 한다.

**승인은 요청 애그리거트의 상태 전이와 리뷰의 숨김 반영이 한 트랜잭션에서 반드시 함께** 일어나야 하는 원자 연산이다 — 둘 중 하나만 반영되면 **"승인됐는데 리뷰가 계속 노출되는"** 상태가 남는다(`ShopImageApprovalService`가 이미지 교체에 대해 갖는 것과 같은 성질).

**`ShopRequestIndexRecorder`를 생성자 필수 의존으로 받는다** — 요청처리 현황(`SHOP_REQUEST_INDEX`)은 파생 읽기모델이고 기록이 누락되면 그 요청이 통합 목록에서 아예 보이지 않는다. 필수 의존으로 두면 **새 상태 전이 메서드를 추가할 때 동기화 배선이 필요하다는 사실이 컴파일 단계에서 드러난다.**

**취소는 원본 애그리거트의 상태 전이**이며, 취소 후에는 같은 리뷰에 재요청이 가능해진다 — PENDING 중복 차단과 1회 제한이 모두 상태 조회에 기반하므로 **코드 추가 없이 자동으로 풀린다.**

**대상 리뷰가 그 가게의 것인지 역조회로 재검증한다** — 경로의 `shopId`만 믿으면 남의 가게 리뷰에 게시중단을 걸 수 있다.

### `ErrorCode` 카탈로그 규약 — 그룹 주석에서 이관

**대상**: `domain/src/main/java/com/tastyhouse/domain/exception/ErrorCode.java`

상수 그룹의 단순 분류 라벨(`// 주문`·`// 쿠폰`)은 상수명 접두어(`ORDER_`·`COUPON_`)가 같은 정보를 담으므로 이관하지 않고 삭제했다. **아래는 코드만 읽어서는 알 수 없는 규약**이다. 봉인 목록 관련 서술(이름·상태코드 불일치 4건, code 문자열 상이 6건)은 위 [봉인·가드 목록](#봉인가드-목록)에 있다.

| 상수 | 용도 한정 |
|---|---|
| `INVALID_INPUT` (400) | **Command record의 compact constructor 가드 전용.** 형식·범위 검증은 Request의 `jakarta.validation`이 담당하므로, 이 코드는 인바운드 어댑터를 우회해 Command가 직접 조립된 경우의 **필수값 누락 같은 구조적 위반**에만 쓴다 |
| `ENTITY_NOT_FOUND` | **fallback이다.** 도메인별 전용 `*_NOT_FOUND` 코드가 있으면 그쪽을 쓴다 |
| `AUTH_REQUIRED`·`ACCESS_DENIED` | **필터 단계와 advice 단계가 같은 코드를 쓰도록 공용으로 둔다** — 서블릿 필터는 advice를 타지 않지만 클라이언트가 보는 계약은 일치해야 한다 |

**응답 `code` 문자열은 wire 계약이다** — 프론트가 `code`로 분기하므로 기존 값을 바꾸지 않는다.

- **SMS·메일 인증**: 상수명은 `SMS_`/`MAIL_` 접두어로 대칭화했으나 **응답 code 문자열은 기존 값을 유지한다** — 프론트가 code로 분기하는 경우 구버전 클라이언트가 unknown으로 처리해 안내 문구가 퇴화하기 때문이다. 프론트 마이그레이션 완료 후 code도 통일 예정.
- **주문 배달팁 3종**은 프론트가 code로 분기하는 wire 계약이다. 문자열을 바꾸면 프론트 매핑도 함께 고쳐야 한다.

**의도적으로 분리해 둔 유사 코드쌍** — 합치지 말 것:

- **쿠폰 최소주문금액 미달 vs `ORDER_MINIMUM_AMOUNT_NOT_MET`** — 다른 검증이다. 프론트가 code로 원인을 구분하므로 별도 코드로 둔다.
- **`SHOP_ORDER_METHOD_NOT_SUPPORTED`(400) vs `SHOP_ORDER_METHOD_NOT_FOUND`(404)** — 후자는 admin이 배정 행을 찾을 때, 전자는 **주문을 거절할 때** 쓴다.

**상태 전이 가드 코드는 승인요청 구조마다 필요하다.** 스펙의 3종에는 없지만 이미지·채식 승인요청과 같은 구조라 같은 형태의 코드가 필요하다 — **이 코드가 없으면 이미 승인·반려된 요청을 다시 승인할 수 있고, 그때 `Product` 컬럼이 두 번 켜진다.**

**중복 제보 차단**: 같은 회원이 같은 메뉴에 같은 유형으로 **7일 내 재제보**하는 것을 막는다. 없으면 한 사람이 반복 제보해 목록을 채울 수 있다. (메뉴 정보 제보는 리뷰(맛 평가)가 아니라 "등록된 정보가 틀렸다"는 제보다.)

**즉시 반영 vs 검수 대상의 구분**(가게 콘텐츠):

- **주문안내** — 메뉴판 최상단 안내 문구. **승인 절차 없이 즉시 반영**되므로 등록 시점 검증은 본문 길이뿐이고, 규정 위반은 관리자 게시중단(`is_hidden`)으로 사후 조치한다(금지어 자동 판정은 하지 않는다).
- **메뉴모음컷** — 손님이 가게를 열었을 때 가장 먼저 보는 이미지. **등록만 검수하고 순서 변경·삭제는 즉시 반영**된다.

**메뉴-가게 연결(N:M)**: `PRODUCT.shop_id`(원본 소유 가게)는 유지하고, 링크가 "어느 가게 메뉴판에 노출되는가"만 담는다. **링크가 1개인 메뉴는 동작이 완전히 그대로다.**

### 결제 승인 — PG HTTP 왕복은 트랜잭션 밖

**대상**: `payment/service/PaymentConfirmationService.java`

결제 승인은 결제 애그리거트의 상태 전이와 주문 애그리거트의 확정 전이를 한 트랜잭션에서 반드시 함께 수행해야 하는 원자 연산이다. 한쪽만 반영되면 **"결제는 됐지만 주문은 대기"** 이거나 **"주문은 확정인데 결제는 미승인"** 인 정합성 붕괴가 남는다. 승인 경로는 세 가지(PG 콜백 · 토스 승인 · 현장결제 완료)인데 규칙은 하나여야 하므로 도메인 계층에 둔다.

주문 상태 전이는 직접 `order.confirm()`을 호출하지 않고 `OrderTransitionService`에 위임한다 — **전이와 저장을 항상 함께 수행한다는 규칙의 단일 원천을 주문 도메인에 유지하기 위함**이다.

**PG HTTP 왕복은 이 서비스 안에서 하지 않는다.** 토스 승인은 (1) 금액·상태·소유권을 검증하는 `prepareTossConfirmation`(DB 읽기, 트랜잭션 안)과 (2) PG 응답을 반영하는 `applyTossConfirmation`/`failTossConfirmation`(DB 쓰기, 별도 트랜잭션)으로 쪼개져 있고, **그 사이의 PG 호출은 소비 모듈이 트랜잭션 밖에서 수행한다.**

과거에는 PG 왕복 전체가 DB 트랜잭션 안에 있어 커넥션·행 락을 네트워크 지연만큼 점유했고, **PG 승인 성공 후 커밋이 실패하면 "PG는 승인, DB는 미승인"이 되어 보상이 불가능했다.** 그래서 이 서비스는 `PgPaymentGateway`를 주입받지 않는다.

이벤트 발행은 Spring `ApplicationEventPublisher`가 아니라 프레임워크-프리 포트 `DomainEventPublisher`를 쓴다.

### 예약 — 정원 차감은 낙관적 락, 재시도는 트랜잭션 밖

**대상**: `reservation/service/ReservationBookingService.java`

예약 생성은 "슬롯 정원 검증 → 정원 차감 → 예약 저장"이, 취소·거절은 "예약 상태 전이 → 슬롯 정원 반납"이 반드시 함께 일어나야 하는 원자 연산이다. 도메인 계층에 두어 **트리거 액터(회원 취소 · 점주 거절)가 달라도 "예약 건수와 슬롯 점유 수는 항상 함께 움직인다"는 규칙이 갈리지 않게** 한다.

**동시성**: 정원 차감은 슬롯의 낙관적 락(`@Version`)으로 보호한다. 이 서비스는 차감 직후 `saveAndFlush`로 **충돌을 트랜잭션 커밋 전에 노출시키기만 하고 재시도는 하지 않는다** — 재시도는 매 시도마다 새 트랜잭션이 필요하므로 트랜잭션 경계 **바깥**(소비 모듈의 command 서비스)에서 수행해야 한다. 충돌 예외는 프레임워크-프리 `OptimisticLockConflictException`으로 번역되어 올라간다(infrastructure 어댑터가 번역).

### `MenuReview` — `Review`와 독립된 애그리거트

**대상**: `menureview/model/MenuReview.java`

**`reviewId`를 필드로 갖지 않으며, 두 평가의 유일한 연결고리는 `orderId`다.** 이 설계는 "매장 평가와 메뉴 평가 중 어느 것을 먼저 하든, 또는 하나만 하든 성립해야 한다"는 요구에서 나왔다 — **`reviewId`를 두는 순간 매장 리뷰 없이 메뉴 평가만 남기는 것이 구조적으로 불가능해진다.** 이 불변식은 컨텍스트 경계(`ContextBoundaryTest`)가 빌드로 강제한다.

**의도적으로 얇다.** 댓글·대댓글·좋아요·사장님답변·사장님만보기가 없는 것은 누락이 아니라 **"리뷰가 아니라 평가(rating)"라는 성격 규정**이다. 소셜 기능은 전부 매장 리뷰 쪽에만 남는다.

주문 항목당 1건 제약은 `UNIQUE(order_product_id)`가 물리적으로 보증한다 — 애플리케이션의 `existsByOrderProductId` 검사는 사용자에게 409를 돌려주기 위한 것이고, **동시 요청의 최종 방어선은 그 유니크 제약이다.**

### 자주 쓰는 문구 — 5개 상한은 완전하지 않다 (의도된 감수)

**대상**: `ceo/service/CeoReplyPhraseService.java`

**5개 상한은 DB가 아니라 애플리케이션 코드가 강제한다 — 그래서 완전하지 않다.** MySQL에는 "한 점주당 행 5개 이하" 같은 행 수 제약을 걸 수단이 없으므로, 건수 조회와 삽입 사이의 경합을 막을 최종 방어선이 존재하지 않는다. **같은 점주가 동시에 등록 요청을 보내면 6개가 될 수 있다.**

이를 감수하는 이유는 (1) 이 목록이 답변 작성 시 골라 쓰는 **표시용**이라 6개가 되어도 데이터 정합성이나 금전에 피해가 없고, (2) 이를 막으려면 점주 행에 비관적 잠금을 걸어야 하는데 **그 비용이 피해에 비해 과하기** 때문이다. 초과 상태가 발견되면 점주가 하나 지우면 그만이다.

소유권은 `shopId`가 아니라 **문구의 `ceoId`와 요청 점주의 일치**로 검증한다 — 문구는 가게가 아니라 점주 계정에 귀속되므로 `ShopOwnershipValidator`가 개입할 자리가 없다. 불일치는 404가 아니라 `CEO_REPLY_PHRASE_ACCESS_DENIED`(403)다.

**등록·수정 시점에 금칙어를 검수**하므로 실제 답변에 넣을 때 `ReviewOwnerReplyService`가 한 번 더 검수하는 것과 중복되지만 **그것이 의도다** — 문구 등록 후 금칙어 목록이 늘어났을 수 있다.

### 행정동 마스터와 경계 도형 — 대표점은 centroid가 아니다

**대상**: `region/model/AdminDong.java`, `shared/geo/InteriorPoint.java`·`PointInPolygon.java`·`GeoRing.java`

`AdminDong`은 **일반 요청 경로에 생성·변경이 없는 마스터**다 — web/admin/ceo 어느 api 모듈도 만들지 않고 조회만 한다. 유일한 생성 경로는 batch-module의 행정동 경계 동기화 배치이며, 외부 원천(통계청 SGIS 파생 행정동 경계)을 읽어 마스터 전체를 교체한다. (과거에는 시드 SQL이 소유해 `reconstitute`만 공개했으나, 좌표·경계까지 3,500여 건을 사람이 관리할 수 없어 배치로 전환했다.) `ADMIN_DONG`은 감사 컬럼이 없는 마스터 테이블이라 감사 시각을 필드로 두지 않는다.

**좌표·경계는 전부 선택 값이다.** 시드가 단계적으로 투입되므로(코드·좌표 먼저, 경계는 나중에) 둘 다 없는 행이 정상적으로 존재한다. 환산은 `hasCenter()`·`hasBoundary()`로 보유 여부를 먼저 확인하고, **둘 다 없으면 판정 불가로 분류해 조용히 포함시키지 않는다.**

**대표점은 centroid가 아니라 경계 내부가 보장되는 점이다.** 오목하거나 초승달 모양인 도형은 무게중심이 도형 밖에 떨어진다 — 실제 행정동 경계 원천 3,558건 중 **47건**의 centroid가 자기 경계 밖이었다. 대표점이 경계 밖이면 그 동은 좌표 기준 포함 판정이 뒤집혀, **배달지역에 들어와야 할 동이 빠지거나 그 반대가 된다.**

`InteriorPoint`의 알고리즘(PostGIS `ST_PointOnSurface`·JTS `InteriorPoint`와 같은 계열):

1. 도형의 위도 범위 한가운데에 수평 스캔라인을 긋는다
2. 모든 변과의 교차점 경도를 모은다 — **구멍(hole) 링의 변도 포함해야** 내부·외부가 올바로 토글된다
3. 경도로 정렬해 짝을 지으면 각 쌍이 도형 내부 구간이 된다(even-odd 규칙)
4. **가장 넓은** 구간의 중점을 대표점으로 삼는다 — 가장 좁은 목을 피해 경계에서 멀어진다

스캔라인이 꼭짓점을 정확히 지나면 같은 교차점이 두 번 잡혀 짝이 어긋날 수 있는데, 2단계의 부등호를 `(y1 > y) != (y2 > y)`로 두어 **한쪽 끝점만** 세도록 해 이 경우를 배제한다(ray casting의 표준 처리).

### 리뷰 부가 리포지토리 Fake 2종 — 보관하지 않는 것이 의도다

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/review/service/FakeReviewTagRepository.java`
→ 클래스 선언 / `saveAll(List<ReviewTag>)` · `deleteByReviewId(ReviewId)`
**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/review/service/FakeReviewImageRepository.java`
→ 클래스 선언 / `saveAll(List<ReviewImage>)` · `deleteByReviewId(ReviewId)`

`ReviewTagRepository`·`ReviewImageRepository`에는 **조회 메서드가 없다.** 저장한 태그·이미지를
되읽어 검증할 수단이 계약에 없으므로, 이 Fake들은 보관용 컬렉션을 두지 않고 호출을 삼키기만 한다 —
협력 객체를 채우는 용도의 스텁이다.

**빈 메서드 본문을 "미구현"으로 오인해 채우지 않는다.** 보관 컬렉션을 추가해도 그것을 읽어 단언할
포트 메서드가 없어 검증에 쓰이지 못하며, 조회 계약이 실제로 생기면 그때 함께 채운다.

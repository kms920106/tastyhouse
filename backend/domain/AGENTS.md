<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# domain

## Purpose
모든 도메인의 핵심을 담는 라이브러리 모듈(`java-library`). 순수 POJO 도메인 모델(Aggregate Root), Value Object, DomainEvent 타입, 포트를 주입받지 않는 순수 계산기·정책·검증기(`*Calculator`·`*Policy`·`*Validator`)와 그 입출력 record, 공유 커널(`shared/{vo,geo,model}`), 예외 카탈로그(`exception/`)를 포함한다.

**(번복됨 — 덩어리 03a) write 포트·출력 포트·포트를 주입받는 도메인 서비스는 더 이상 이 모듈에 없다.** 과거에는 Repository **write 포트**(`<ctx>/repository/`), 외부 어댑터용 **출력 포트**(`<ctx>/port/`), 그 포트를 주입받는 도메인 서비스(`<ctx>/service/`의 오케스트레이션), 이벤트 발행 포트 `shared/event/DomainEventPublisher`, `shared/exception/OptimisticLockConflictException`까지 이 모듈이 소유했으나, 엄격 레이어드 전환(infrastructure는 application만 본다)을 위해 전부 `application`으로 옮겼다. (그 뒤 persistence domain 재허용으로 persistence가 다시 이 모듈을 `implementation`으로 보게 됐지만, write 포트는 `application/<ctx>/port/out/write/`에 그대로 둔다.) 옮긴 위치·등록 방식·경계 테스트는 `application/AGENTS.md`의 "덩어리 03a" 절이 정본이다.

| 이 모듈에 남는 것 | `application`으로 간 것 (03a) |
|---|---|
| `<ctx>/model/`·`<ctx>/vo/`·`<ctx>/event/`(이벤트 타입) | `<ctx>/repository/` write 포트 106개 + 보조 타입 2개 → `application/<ctx>/port/out/write/` |
| 포트를 주입받지 않는 순수 서비스(`CupDepositPolicy`·`EditorChoicePolicy`·`ShopDeliveryTipCalculator` 등)와 그 입출력 record | `<ctx>/port/` 출력 포트 10개 → `application/<ctx>/port/out/` |
| `shared/{vo,geo,model}` | 포트를 주입받는 서비스 71개 + `NotificationMessage` → `application/<ctx>/service/` |
| `exception/`(`BusinessException`·`ErrorCode` 등) | `DomainEventPublisher` → `application/shared/event/`, `OptimisticLockConflictException` → `application/shared/port/out/` |

**프레임워크를 전혀 모른다** — production 의존이 **하나도 없다**(Lombok까지 제거됨). Spring Web뿐 아니라 JPA·QueryDSL·`spring-tx`/`spring-orm`도 없으므로, `@Entity`/`@Transactional`/`@Service`/`@Component`/`com.querydsl.*`가 이 모듈에 단 한 곳도 없다. 예외의 HTTP 상태는 `int httpStatusCode`로, 낙관적 락 충돌은 `OptimisticLockConflictException`으로 표현한다. ~~`web-api`/`admin-api`/`ceo-api`/`batch-module`/`infrastructure:persistence`/외부 연동 모듈~~ **(번복됨 — 엄격 레이어드 01·02·03b)** 지금 이 모듈을 의존하는 것은 `application`과 `infrastructure:persistence` 둘이다(둘 다 `implementation`, 전이 노출 없음). presentation은 덩어리 01로, 벤더 16모듈은 02로 domain 의존이 끊겼다. `infrastructure:persistence`는 03b로 끊겼다가 **(번복됨 — persistence domain 재허용)** 다시 `implementation`으로 의존한다 — 쓰기 어댑터 `XxxPersistenceAdapter`이 도메인 모델을 직접 저장하고, 엔티티 ↔ 도메인 변환은 persistence `<ctx>/persistence/XxxMapper`가 한다. 따라서 `reconstitute`를 부르는 "인프라"는 다시 persistence 매퍼다(03b 동안은 `application/<ctx>/store/XxxStateMapper`였다). persistence의 조회 DAO(`..query..`)는 여전히 이 모듈을 모른다(`queryShouldNotDependOnDomain`). 아래는 과거 서술이다: `web-api`/`admin-api`/`ceo-api`/`batch-module`/`infrastructure:persistence`/외부 연동 모듈(`infrastructure:{firebase,aws-s3,aws-ses,aws-sns,apple-oauth,facebook-oauth,pg,tosspayments,mail,javamail,sms,solapi,bbq,admdongkor}`)/`security-module`이 이 모듈에 의존한다(역방향 의존은 없다). 코어 `infrastructure:restclient`, 코드 없는 스타터 `file-storage`·`oauth`, 도메인 타입을 쓰지 않는 `kakao-oauth`·`naver-oauth`는 이 모듈을 의존하지 않는다.

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
- **`@Entity`는 이 모듈에 없다**: 도메인 모델은 전 도메인(22개) 순수 POJO이며, JPA 엔티티(`XxxJpaEntity`)·매퍼(`XxxMapper`)·`XxxPersistenceAdapter`·`AttributeConverter`·`BaseEntity`는 전부 `infrastructure:persistence`(`com.tastyhouse.infrastructure.<ctx>.persistence`)에 있다. `AttributeConverter`는 0건이고, 이 모듈의 모델을 `reconstitute`로 재구성하는 것은 persistence `XxxMapper`다(엔티티 필드는 `String`·`XxxEmbeddable`이므로 enum `valueOf`·VO `of`도 그 매퍼가 한다). ~~**(03b)** 그 persistence는 이제 이 모듈을 모른다 — 엔티티·매퍼는 `application`의 `XxxState` record만 다루고(`XxxPersistenceAdapter`은 `XxxStatePortImpl`로 개명), 재구성은 `application/<ctx>/store/XxxStateMapper`다.~~ **(번복됨 — persistence domain 재허용)** 외부 애그리거트 참조는 ID VO(`MemberId` 등)로 하고, 자식 애그리거트도 별도 Repository로 분리한다(JPA 연관관계 매핑 자체가 이 모듈에 존재할 수 없다).
- **도메인 모델 규칙**:
  - 신규 생성 `of(...)`(또는 `create(...)`/`register(...)`)와 DB 재구성 전용 `reconstitute(id, ..., createdAt, updatedAt)` 두 팩토리만 공개한다. `reconstitute`는 인프라(매퍼)만 호출하며(불변식 우회 방지, Javadoc 명시), `id`는 미영속이면 null이다. Java 계층에 생성 경로가 없는 read-only 애그리거트는 `reconstitute`만 둔다(reference: `shop/model/ProhibitedWord`). 조회 전용이고 도메인 불변식도 없는 데이터는 애그리거트를 두지 않고 infra `<ctx>/query/`의 Result DTO로만 노출한다(reference: `search`의 추천 검색어 — 도메인 모델 없이 `infrastructure/search/query/RecommendedKeywordResult`만 존재).
  - **재대입되지 않는 필드는 `final`로 선언**한다. `@Entity`와 달리 순수 POJO는 JPA 프록시/리플렉션 제약이 없으므로, 생성자(팩토리) 이후 상태전이로 바뀌지 않는 필드는 `id`뿐 아니라 상태 필드까지 모두 `final`로 둔다(전이되는 필드만 non-final). 불변성을 컴파일러가 강제하고 IntelliJ `may be 'final'` 경고를 차단한다. reference: `admin`의 `Admin`(update 경로 없어 전 필드 `final`).
  - **`@Embedded` 대상 VO는 Java `record`로 선언**한다(검증은 compact constructor). Hibernate 6이 `@Embedded` 값 객체를 canonical 생성자로 인스턴스화할 수 있어야 하므로 일반 class + 검증 생성자는 런타임 `InstantiationException`을 유발한다. 접근자는 record accessor(`value()`)로 통일하고 `toString()` 오버라이드는 남기지 않는다. 컬럼 매핑은 이 모듈이 아니라 각 `XxxJpaEntity`의 `@AttributeOverride`가 소유한다. reference: `shared/vo/PhoneNumber`, `shared/vo/VerificationCode`, `product/vo/ProductDiscountInfo`. **(번복됨 — 03b, 근거만)** 이 모듈의 VO는 더 이상 `@Embedded` 대상이 아니다 — persistence가 domain을 모르게 되면서 엔티티는 자기 소유 `@Embeddable` record(`PhoneNumberEmbeddable`·`VerificationCodeEmbeddable`·`ProductDiscountInfoEmbeddable`·`OrderDeliveryDestinationEmbeddable`·`OrderScheduleEmbeddable`)를 매핑한다. 따라서 위 "Hibernate가 canonical 생성자로 인스턴스화" 근거와 알파벳순 컴포넌트 규약(`EmbeddedRecordComponentOrderTest`)은 **그 Embeddable record에 걸리고**, 이 모듈의 VO에는 걸리지 않는다. VO를 record로 두는 것(불변 값·compact constructor 검증·`value()` accessor)은 그대로 유지한다 — 되돌릴 이유가 없고, 컴포넌트 순서를 바꾸면 매퍼(03b 당시 StateMapper, 지금은 persistence `XxxMapper`)의 위치 기반 `new` 호출이 조용히 어긋날 수 있으므로 순서도 함부로 바꾸지 않는다. persistence domain 재허용 후에도 엔티티는 domain VO가 아니라 `XxxEmbeddable`을 매핑한다(Embeddable 5종 유지).
- **(번복됨 — 03a, 위치만) write 포트는 이 모듈에 없다** — `application/<ctx>/port/out/write/`로 옮겨졌고 판정 기준은 아래 그대로다. **Repository 인터페이스는 write 포트만 둔다**: `findById`/`save`/`saveAndFlush`/`delete`/`existsByX`(중복 검증)/`findByNaturalKey`/검증용 `countByX`/락 획득용 조회처럼 **불변식 검증·상태 전이에 필요한** 조회만 남긴다. Result DTO·`PageResult` 반환, 조인 투영, 목록·검색·페이징 등 **표현 목적 조회는 이 모듈에 두지 않고** `infrastructure:persistence`의 `<ctx>/query/`(`{도메인}QueryAdapter`)가 소유한다. 판정 기준: "이 조회가 없으면 불변식 검증이나 상태 전이가 불가능한가?"
- **(번복됨 — application `*ServiceConfig` 삭제)** 아래 03a 항목의 `<Ctx>ServiceConfig`는 전부 삭제됐다. 지금 포트를 주입받는 도메인 서비스는 `application/<ctx>/service/`에서 **클래스에 앱 마커 하나만**(`@Service` 없이) 달아 스캔으로 등록되고, 이 모듈에 남은 순수 계산기·정책 7개(`ProductExposureCalculator`·`CupDepositPolicy`·`StorePriceBadgePolicy`·`ShopOperatingStatusCalculator`·`ShopDeliveryTipCalculator`·`ShopNextOpenTimeCalculator`·`ScheduledOrderSlotCalculator`)는 이 모듈이 spring-free라 애노테이션을 달 수 없으므로 `application/shared/config/SharedBeanConfig`(`@SharedApp`)의 `@Bean`으로 등록된다. **이 모듈에 새 순수 서비스를 추가해 빈이 필요하면 `SharedBeanConfig`에 `@Bean`을 추가한다.**
- **(번복됨 — 03a) 포트를 주입받는 도메인 서비스는 `application/<ctx>/service/`의 마커 없는 POJO가 됐고, 빈 등록은 `application/<ctx>/config/<Ctx>ServiceConfig`(`@SharedApp`)가 한다** — 과거 `infrastructure:persistence`의 `<ctx>/config/<Ctx>DomainConfig` 18개는 삭제됐다. 이 모듈에 남는 `<ctx>/service/`는 **포트를 주입받지 않는 순수 계산기·정책과 그 입출력 record**뿐이며, 그 순수 서비스의 `@Bean` 메서드도 같은 `<Ctx>ServiceConfig`에 있다(클래스는 domain, 등록은 application). 판정은 기계적이다 — 포트(write 포트·출력 포트·이벤트 발행 포트)나 그것을 주입받는 서비스를 생성자로 받으면 application, 아니면 domain이다(domain은 application을 볼 수 없으므로 순수 서비스가 이동 서비스를 주입하면 그 서비스도 옮긴다 — `ShopRiderGuideValidator`가 그 사례). 아래는 과거 서술이다. **도메인 서비스(`<ctx>/service/`)는 순수 POJO**다: `@Service`/`@Component`/`@Transactional`을 붙이지 않고, 빈 등록은 ~~`infrastructure:persistence`의 컨텍스트별 `<ctx>/config/<Ctx>DomainConfig`~~ `application`의 `<Ctx>ServiceConfig`가 `@Bean` 팩토리로 수행한다. 트랜잭션 경계는 이를 호출하는 api 모듈의 `{도메인}CommandService`(`@Transactional`)가 소유한다. 한 트랜잭션에서 2개 이상 애그리거트 타입을 load & save하는 불변식 오케스트레이션(reference: `order/service/OrderPlacementService`, `payment/service/PaymentCancellationService`, `point/service/PointLedgerService`)과 무상태 정책·검증기(reference: `faq/service/FaqCategoryDeletionPolicy`, `shop/service/ProhibitedWordValidator`)가 여기 산다 — 소비 모듈로 복제하지 않는다. (`PaymentConfirmationService`는 같은 성격의 오케스트레이션이었으나 `application`으로 이동했다 — 아래 "결제 승인" 절 참고.)
- **명시적 save 규칙**: 도메인 모델은 POJO이므로 JPA 더티 체킹으로 자동 flush되지 않는다. 도메인을 변경한 뒤 **반드시 `repository.save(domain)`을 호출**한다(누락 시 변경이 조용히 유실된다). 이 책임은 도메인 서비스와 api 모듈의 `{도메인}CommandService` 양쪽에 있다.
- **(번복됨 — 03a) 출력 포트는 이 모듈에 없다** — 남아 있던 포트 인터페이스 7종(`ProductReviewStatisticsPort`·`StorePriceVerificationPort`·`ShopRequestIndexSyncPort`·`member`/`rank`의 `MemberReviewCountPort`·`KeywordCountPort`·`ReplyPhraseTextValidator`, 입출력 record 3개 별도)과 이벤트 발행 포트 `DomainEventPublisher`까지 전부 `application`으로 갔다. 아래는 과거 서술이다. **출력 포트는 `<ctx>/port/`에 둔다**: 외부 시스템을 도메인이 인터페이스로 선언하고 외부 연동 모듈이 기술별로 나눠 구현한다. **(번복됨 — 덩어리 02/03a)** 파일·메일·SMS·결제 4개 컨텍스트의 출력 포트는 `domain`을 떠나 `application`의 `<ctx>/port/out`으로 이관됐다 — `application/file/port/out/FileStoragePort`는 `infrastructure:firebase`의 `FirebaseFileStorage`(기본)·`infrastructure:aws-s3`의 `S3FileStorage`가 직접 구현(`file.provider`로 배타 선택 — 중간 위임 어댑터 없음), `application/payment/port/out/PgPaymentGateway`는 라우터 `application/payment/service/PgPaymentGatewayRouter`(POJO+`@SharedApp` 등록)가 구현하고 벤더는 `application/payment/port/out/PgProviderGateway`를 구현한다(`infrastructure:tosspayments`, 조립은 채널 모듈 `infrastructure:pg`), `application/mail/port/out/MailSender`는 `infrastructure:javamail`(SES 대체 구현은 `infrastructure:aws-ses`의 `SesMailSender` — 벤더 선택은 채널 모듈 `infrastructure:mail`이 조립한다)·`application/sms/port/out/SmsSender`는 `infrastructure:solapi`(벤더 선택은 채널 모듈 `infrastructure:sms`, SNS 대체 구현은 `infrastructure:aws-sns`의 `SnsSmsSender`)이 구현한다. **domain에 남은 출력 포트**는 `product/port/ProductReviewStatisticsPort`·`rank/port/MemberReviewCountPort`(`infrastructure:persistence` 소관)뿐이다. 이벤트 발행 포트는 `shared/event/DomainEventPublisher`이며 스프링 구현은 infrastructure-module의 `SpringDomainEventPublisher`다 — ~~이 포트는 이동 대상이 아니다~~ **(번복됨 — 03a)** 이 포트와 구현 모두 `application/shared/event/`로 이동했다.
- **낙관적 락 충돌은 `OptimisticLockConflictException`으로 표현**한다(**03a로 위치 이동**: `application/shared/port/out/` — persistence가 던지고 application이 잡으므로 `port/out` 아래여야 03b에서 persistence가 domain 없이 던질 수 있다). 스프링의 `ObjectOptimisticLockingFailureException`을 이 예외로 번역하는 책임은 `infrastructure:persistence`의 `ReservationSlotPersistenceAdapter`(03b 동안은 `ReservationSlotStatePortImpl` — **(번복됨 — persistence domain 재허용)** 이름만 되돌아왔고 예외 위치는 `port/out` 그대로)에 있고, 재시도 루프는 소비 모듈에 둔다(상세는 루트 CLAUDE.md "낙관적 락 재시도 배치 규칙").
- **command 파라미터는 원시 타입 또는 도메인 타입으로 받는다**: presentation의 Request 타입을 인자로 받는 팩토리·메서드를 두지 않는다(레이어 역전 방지). HTTP 경계는 `String`/`Long`으로 받고 api 모듈 서비스에서 `Enum.from(String)`·`XxxId.of(Long)`으로 승격한 뒤 이 모듈에 전달한다.
- **조회 결과 DTO를 `com.tastyhouse.domain..` 안에 두지 않는다**: Result record와 `SearchCondition`은 도메인 모델이 아니다. **읽기 계약은 전부 `application` 모듈의 `com.tastyhouse.application.<ctx>.port.out`이 소유한다** — 이 모듈에는 두지 않는다. 한때 다중 앱 공유분 55개를 이 모듈이 갖고 있었으나(모듈 재편 챕터 05), application 모듈이 하나로 통합되며 근거였던 앱 간 수평 의존 회피가 무의미해져 의존성 정리 챕터 04에서 되돌렸다. 접미어 `Result` 통일·`Dto` 금지·admin 충돌 시 `Management` 한정어 규칙은 위치와 무관하게 적용된다.
- **QueryDSL 동적 where 조립 규칙은 이 모듈 소관이 아니다**: `BooleanExpression` varargs 헬퍼 패턴은 QueryDSL을 소유한 `infrastructure:persistence`(`<ctx>/query/`의 QueryAdapter)의 규칙이다 — `infrastructure-module/AGENTS.md` 참고.

### Testing Requirements
- **순수 단위 테스트**가 원칙이다: 도메인 모델·도메인 서비스는 프레임워크 의존이 없으므로 스프링 컨텍스트나 DB 없이 JUnit만으로 불변식·상태전이를 검증한다(reference: `notice/model/NoticeTest` 등 도메인별 `XxxTest`).
- 새 애그리거트·상태전이를 추가하면 대응 단위 테스트를 함께 추가한다.
- JPA 매핑 정합성(`ddl-auto=validate`)·enum `columnDefinition` 검증은 엔티티를 소유한 `infrastructure:persistence`의 책임이다.

### Common Patterns
- Repository write 포트(**03a로 `application/<ctx>/port/out/write/`로 이동**): `XxxPersistencePort`(인터페이스) ← `infrastructure:persistence`의 `<ctx>/persistence/XxxPersistenceAdapter`(구현). ~~**(03b)** `application/<ctx>/store/XxxPersistencePort` ← `application/<ctx>/store/XxxStore`(도메인 ↔ `XxxState` 변환) → `application/<ctx>/port/out/write/XxxStatePort` ← persistence `XxxStatePortImpl` 순서다.~~ **(번복됨 — persistence domain 재허용)** 다시 `port/out/write/XxxPersistencePort` ← persistence `XxxPersistenceAdapter`이며 load-copy-save도 `XxxPersistenceAdapter`에 있다. 저장 시맨틱은 load-copy-save(id null이면 insert, 있으면 managed 엔티티 조회 후 `Mapper.applyChanges` 복사 — detached merge 금지).
- ID 강타입: `<ctx>/vo/XxxId`(`record XxxId(Long value)` + compact constructor 검증 + 정적 팩토리 `of`). ~~JPA 매핑용 `AttributeConverter`는 `infrastructure:persistence`에 있다.~~ **(번복됨 — 정책 B·03b)** `*IdConverter`는 정책 B로, 마지막 `AmountConverter`는 03b로 삭제돼 persistence에 컨버터가 없다. 엔티티는 raw `Long`이고 `XxxId.of`로의 승격은 persistence `<ctx>/persistence/XxxMapper`가 null 가드와 함께 한다(03b 동안은 `XxxState`·`application/<ctx>/store/XxxStateMapper` — **(번복됨 — persistence domain 재허용: 위치만)**).
- DomainEvent는 `<ctx>/event/`에 record로 정의하고, 발행은 `DomainEventPublisher` 포트를 통한다. 리스너는 `application`의 `<ctx>/listener/`에 `@Component @SharedApp`으로 둔다(4앱 전부가 스캔한다 — 특정 앱에만 두면 다른 앱이 트리거할 때 누락된다. 과거 위치 `infrastructure:persistence`는 번복됨, 근거는 `application/AGENTS.md`).
- 공유 커널: `shared/vo/PhoneNumber`, `shared/model/ApprovalStatus`, `shared/geo/`(`GeoPoint`·`GeoRing`·`GeoPolygon`·`GeoBoundingBox`·`InteriorPoint`·`PointInPolygon` 등 + **03b로 persistence에서 옮겨 온 `GeoPolygonTextCodec`** — 도형 ↔ `LONGTEXT` 문자열 형식, 아래 설계 근거 절). (`shared/event/DomainEventPublisher`·`shared/exception/OptimisticLockConflictException`은 **03a로 `application`의 `shared/event/`·`shared/port/out/`으로 이동**) (페이징 계약 `shared/page/PageQuery`·`PageResult`는 **덩어리 01로 `application`의 `shared/port/out/page/`로 이동** — domain 내 사용처 0건)

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

### 컨텍스트 경계 위반 1건 — 현상 동결 봉인

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/architecture/ContextBoundaryTest.java`
→ `SEALED_VIOLATIONS`

domain에는 25개 바운디드 컨텍스트가 한 모듈에 공존한다. 컨텍스트 간 참조는 **ID VO(`<ctx>.vo..`)·도메인 이벤트(`<ctx>.event..`)** 둘로만 허용하고(03a 이전에는 출력 포트 `<ctx>.port..`도 허용했으나 포트가 전부 `application`으로 떠나 허용 목록에서 뺐다), 타 컨텍스트의 `model..`/`repository..`/`service..` 직접 import는 금지한다. `shared..`·`exception..`은 컨텍스트가 아니라 전 컨텍스트 공용이므로 전면 허용한다.

**기존 위반은 고치지 않고 봉인한다.** **이 목록은 줄어들기만 해야 한다 — 항목을 추가하는 것은 새 위반을 승인하는 것이므로 금지한다.** 위반을 해소했다면 그 클래스를 목록에서 지운다.

봉인 구성원 1개.

- `com.tastyhouse.domain.shop.service.DeliveryAreaProjection`

**봉인 목록 이력**: 16개 → 14개(덩어리 02 — `MailVerificationService`·`PaymentConfirmationService`가 `application`으로 이동) → **1개(덩어리 03a)**. 03a에서 빠진 13개(`MemberDeliveryAddressService`·`OrderPlacementService`·`PaymentCancellationService`·`ReservationBookingService`·`ReviewBlindRequestService`·`ReviewLifecycleService`·`ReviewOwnerReplyService`·`ShopCeoAssignmentService`·`ShopDeliveryAreaPolygonService`·`ShopDeliveryAreaRadiusService`·`ShopDeliveryAreaService`·`ShopDeliveryTipService`·`ShopRequestCancelService`)는 **위반을 해소한 것이 아니라 대상이 `application`으로 옮겨간 것**이다. 02에서 빠진 2개와 함께 15개 전부가 `application`의 `ServiceContextBoundaryTest.SEALED_VIOLATIONS`로 옮겨가 같은 규칙(봉인 + 짝 테스트)을 계속 받는다 — `application/AGENTS.md` 참고.

**짝 테스트 2종**.

- `ContextBoundaryTest.sealedViolationsShouldNotBeStale()` — 목록에 있으나 더 이상 위반하지 않는 클래스가 있으면 실패해 지우라고 알린다.
- `ContextBoundaryTest.sealedViolationListShouldNotBeEmpty()` — 목록이 비면 실패해서 **봉인 장치 자체(`SEALED_VIOLATIONS`·짝 테스트)를 제거하고 규칙을 순수 강제로 전환하라**고 알린다. 목록이 비면 위 짝 테스트가 검사 대상을 잃어 공허하게 통과하기 때문이다.

**모든 규칙은 `allowEmptyShould(true)` 없이 선언한다**(공허 통과 금지 — `DomainPurityTest`·`LayerRulesTest` 개정 선례).

### 컨텍스트 간 순환 — 봉인 해제, 순수 강제 (03a)

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/architecture/ContextBoundaryTest.java`
→ `contextsShouldBeFreeOfCycles()`

**번복됨 — 03a.** 과거에는 강결합 성분 `"order,product,review,shop"` 1건을 `SEALED_CYCLES`로 봉인하고 짝 테스트 `sealedCyclesShouldNotBeStale()`을 두었다. 그 순환을 만들던 간선이 전부 포트 주입 도메인 서비스에서 나왔기 때문에, 서비스가 `application`으로 떠나자 domain 컨텍스트 그래프의 순환 성분이 **0개**가 됐다. 짝 테스트가 봉인의 낡음을 알려 준 대로 `SEALED_CYCLES`·짝 테스트·성분 안쪽 제외 로직을 삭제하고 **`beFreeOfCycles()`를 예외 없이 강제**한다. 새 순환이 생기면 그대로 빌드가 실패한다 — 봉인 목록을 되살리지 말 것.

같은 성분 `"order,product,review,shop"`은 이제 `application`의 `ServiceContextBoundaryTest.SEALED_CYCLES`가 봉인한다(성분 단위 봉인 원칙 — 쌍이 아니라 SCC — 도 그쪽이 그대로 승계했다).

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

### `GeoPolygonTextCodec`의 실패를 조용히 넘기지 않는다 (덩어리 03b로 `infrastructure:persistence`에서 이관)

**대상**: `backend/domain/src/main/java/com/tastyhouse/domain/shared/geo/GeoPolygonTextCodec.java` → `decode` · `decodeRings` · `COORDINATE_SCALE`

- **형식이 깨진 입력은 `IllegalArgumentException`으로 실패시킨다** — 조용히 건너뛰면 도형의 일부가 사라진 채 복원되어, 점주가 그린 것과 다른 배달지역이 저장된 것처럼 보인다.
- 저장 형식은 "경도 위도" 순서이고 `GeoPoint`는 (위도, 경도) 순서이므로 **복원 시 뒤집는 자리를 지우지 않는다.**
- `decodeRings`가 값 없음(`null`·빈 문자열)에 빈 목록을 돌려주는 분기는 지우지 않는다 — 행정동 경계는 단계적으로 투입되므로 미보유가 정상 상태다.
- `MySQL GEOMETRY`·JSON으로 되돌리지 않는다 — 근거는 아래 "`GeoPolygonTextCodec` — 폴리곤·경계를 `LONGTEXT` 문자열로 담는 형식" 절.
- 회귀 테스트는 `backend/domain/src/test/java/com/tastyhouse/domain/shared/geo/GeoPolygonTextCodecTest.java`(persistence에서 함께 이동)다.

### import 순서 가드 — `ImportOrderConventionTest`

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/architecture/ImportOrderConventionTest.java`
→ `importsFollowConvention` · `TOP_SEGMENT_RANK` · `PRESENTATION_SHARED_SEGMENTS` · `MINIMUM_SCANNED_FILES`
짝 설정: `backend/domain/build.gradle` → `tasks.named('test')`의 `importOrderSources` 입력

backend 전체 `*.java`를 **소스 파일로** 읽어(`build`·`bin`·`.gradle` 제외) import 블록이 `backend/CLAUDE.md` "코딩 스타일 (import 순서)" 규칙대로인지 검사한다. 클래스패스가 아니라 소스를 읽는 이유는 import 순서가 바이트코드에 남지 않기 때문이다. 규칙은 `backend/import_order.py`의 `key`/`render`와 같다.

- **순위 표는 세 곳에 있다** — `backend/CLAUDE.md`의 표, 이 테스트의 `TOP_SEGMENT_RANK`·`PRESENTATION_SHARED_SEGMENTS`, `backend/import_order.py`의 `RANK`·`PRESENTATION_SHARED`. **표를 바꾸면 세 곳을 함께 바꾼다.**
- **표에 없는 최상위 세그먼트는 실패시킨다.** 새 모듈이 생겼는데 순위가 정해지지 않은 상태를 드러내기 위해서다. 테스트를 느슨하게 바꾸지 말고 순위를 정해 세 곳에 추가한다.
- **`MINIMUM_SCANNED_FILES`(3000) 단정을 지우지 않는다.** backend 루트(`settings.gradle`이 있는 디렉터리)를 잘못 찾아 0개를 스캔하고 공허하게 통과하는 것을 막는다(`allowEmptyShould(true)` 금지와 같은 취지).
- **`build.gradle`의 `importOrderSources` 입력 선언을 지우지 않는다.** import 순서만 바꾼 변경은 바이트코드가 같아서, 이 선언이 없으면 Gradle이 `:domain:test`를 UP-TO-DATE로 건너뛰어 위반이 통과한다(실측 확인).
- **모듈 하나만 빌드하면(`./gradlew :ceo-api:build`) 이 테스트는 돌지 않는다.** 전체 `./gradlew build`나 `:domain:test`에서만 잡힌다. 모듈마다 복사하는 방식은 유지비가 커서 채택하지 않았다.

### 타입 본문 첫 줄 빈 줄 가드 — `TypeBodyBlankLineConventionTest`

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/architecture/TypeBodyBlankLineConventionTest.java`
→ `typeBodiesStartWithSingleBlankLine` · `violatingLines` · `typeBodyBraces` · `isViolation` · `MINIMUM_SCANNED_FILES`
짝 설정: `backend/domain/build.gradle` → `tasks.named('test')`의 `importOrderSources` 입력 (이름은 import 가드 때 지었지만 `:domain:test` 태스크 전체의 입력이라 이 가드도 보호한다)

backend 전체 `*.java`를 소스 파일로 읽어(`build`·`bin`·`.gradle` 제외) 타입 본문 여는 중괄호 다음 줄이 빈 줄 정확히 1개인지 검사한다. 규칙과 제외 대상은 `backend/CLAUDE.md` "타입 본문 첫 줄 빈 줄 규칙" 절이 정본이다.

- **판정을 정규식 한 줄로 단순화하지 않는다.** 단순 매칭은 `public void record(...)` 메서드, `(record, markers) -> {` 람다, `for (JavaClass record : ...)` 반복문을 타입으로 오인해 메서드·람다 본문에 빈 줄을 요구한다(도입 당시 실측 오탐 5건 — `ShopRequestIndexRecorder`·`AppIsolationTest` 등). 그래서 스캐너는 ① 리터럴·텍스트 블록·주석을 건너뛰고 ② 키워드를 완전한 식별자로만 인정하며(직전 토큰 `.` 제외) ③ 바로 다음 토큰이 식별자(타입 이름)일 때만 선언으로 보고 ④ 괄호·제네릭 깊이가 음수가 되거나 `;`를 만나면 판정을 취소한다.
- **반증 테스트 7개를 지우지 않는다.** 위 오탐 사례와 `@Target({...})`·`sealed … permits`·`>>>` 제네릭·텍스트 블록·`'{'` 문자 리터럴이 각각 단정돼 있다. 스캐너를 고치면 이 테스트가 먼저 깨져야 한다.
- **`MINIMUM_SCANNED_FILES`(3000) 단정을 지우지 않는다.** backend 루트를 잘못 찾아 0개를 스캔하고 공허하게 통과하는 것을 막는다.
- **실패 메시지는 원소를 최대 1,000개까지만 보여 준다**(AssertJ 기본 표시 한도). 위반이 그보다 많아도 목록이 잘린 것일 뿐이다.
- **모듈 하나만 빌드하면 이 테스트는 돌지 않는다.** import 순서 가드와 같다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거. 챕터 03(domain 모듈) 이관분 -->

이 절은 `domain` 각 컨텍스트의 java 주석에 있던 **설계 근거**를 옮긴 것이다. 비즈니스 규칙(분류 C)은 `docs/domain/{도메인}.md`에 있으므로 여기 중복 기술하지 않고 링크한다.

### 모듈 전역 — 프레임워크-프리와 명시적 저장

**대상**: `domain` 전 컨텍스트의 model·service

도메인 모델은 JPA/Spring에 의존하지 않는 POJO다. 영속화는 `infrastructure:persistence`의 `{X}JpaEntity` + `{X}Mapper`가 담당한다. **POJO라 더티 체킹이 없으므로, 변경 후에는 command 서비스가 명시적으로 `{X}Repository#save`를 호출해야 한다** — 이것을 빠뜨리면 변경이 조용히 유실된다.

도메인 서비스는 `@Service`/`@Transactional` 없는 순수 POJO이며, **빈 등록은 ~~`application`의 `ShopServiceConfig` 등 `<Ctx>ServiceConfig`(`@SharedApp`)가 담당하고~~ **(번복됨 — application `*ServiceConfig` 삭제)** `application/shared/config/SharedBeanConfig`(`@SharedApp`)의 `@Bean`이 담당하고 트랜잭션 경계는 각 앱의 `CommandService`가 선언한다.** (03a 이후 포트를 주입받는 서비스는 `application`에 살고, 이 모듈에는 포트 없는 순수 서비스만 남는다.)

도메인은 인증을 모르므로, **변경 주체(`ShopChangeActor` 등)는 마지막 파라미터로 명시 전달받는다.** 도메인이 `LocalDate.now()`를 직접 부르지 않는 것도 같은 이유다 — 기준일을 파라미터로 받아야 시계 조작 없이 경계값을 테스트할 수 있다.

### 배달팁·최소주문금액 규격은 외부 가이드가 강제한다 — 내부 판단으로 완화 금지

**대상**: `shop/service/ShopDeliveryTipService.java`(**03a로 `application/.../shop/service/`로 이동**), `shop/model/ShopDeliveryTipTier.java`·`ShopDeliveryTipHoliday.java`·`Shop.java` → `validateMinOrderAmount`, `shared/geo/GeoDistance.java` → `distanceMeters`

아래 규격은 이 저장소가 정한 비즈니스 정책이 아니라 **외부 배달 플랫폼 가이드가 강제하는 요건**이다. 원문 주석이 "배민 가이드 강제 규격"·"배민 가이드 원문"으로 출처를 밝히고 있었으므로, **불합리해 보여도 내부 판단으로 완화·재협상하지 말고 그 가이드가 실제로 바뀌었는지부터 확인한다.**

- 구간별 배달팁 상한 **5,000원 미만**(5,000원 자체 불가)
- 구간 단조성 — **주문금액이 오르면 배달팁은 내려가야 한다**
- 거리별 ↔ 지역별 **상호 배타**
- 공휴일 팁은 날짜별이 아니라 **가게당 단일 금액**("법정 공휴일에 일괄 부과")
- 거리 기준은 도로 경로가 아니라 **직선거리**
- 최소주문금액은 **픽업(포장)에 적용하지 않는다**

규칙 본문은 [`docs/domain/shop.md` § 배달팁 · § 최소주문금액](../../docs/domain/shop.md)에 있고, 이 절은 **그 출처가 외부라는 사실**만 담는다 — 그 사실이 사라지면 후임자가 평범한 내부 규칙으로 오인해 바꾸게 된다.

### 순수 계산기 형태 (리포지토리 0개·상태 0개)

**대상**: `shop/service/ShopDeliveryTipCalculator.java`, `shop/service/ShopOperatingStatusCalculator.java`

두 계산기는 **리포지토리 주입 0개, 인스턴스 상태 0개**다. 좌표→거리 변환과 날짜→공휴일 판정은 호출부가 끝내고 `ShopDeliveryTipContext`에 **이미 해석된 값**으로 담아 넘기므로, 계산기는 Spring·DB·시계 없이 단위 테스트할 수 있다. 이 형태를 깨고 리포지토리를 주입하면 그 테스트 가능성이 사라진다.

배달팁 항목 간 우선순위(거리별↔지역별 배타, 공휴일 > 시간별 **대체**, 시간별은 구체성 우선으로 하나만)는 비즈니스 규칙이므로 [`docs/domain/shop.md` § 배달팁](../../docs/domain/shop.md)에 있다.

### 점주가 켤 수 없고 admin·시스템만 바꾸는 플래그

**대상**: `shop/model/Shop.java` → `cupDepositEnabled`, `storePriceVerified`

**`cupDepositEnabled`는 점주의 영업 설정이 아니라 외부 규제 사실이다.** 환경부·자원순환보증금관리센터가 지역(제주·세종)과 사업자 규모로 지정하며, 지정·해제가 운영 이벤트로 발생한다. 그래서 점주가 스스로 켤 수 없고 **admin만 토글한다.** 주소 문자열로 지역을 파싱해 판정하지 않는 이유도 같다 — 같은 지역이어도 지정 사업자가 아닐 수 있고, 지정 여부는 주소에서 도출되는 값이 아니다.

**`storePriceVerified`는 '매장과 같은 가격' 뱃지 노출과 매장가·픽업가 설정 가능 여부를 함께 가른다.** 관리자가 가격표 이미지와 실제 매장을 대조해 승인할 때만 켜지며(`StorePriceVerificationService`), 메뉴 가격이 바뀌어 **배달가 > 매장가**가 되면 가격 저장 시점에 **동기**로 내려간다 — 배치로 미루면 그 사이 손님이 잘못된 뱃지를 본다.

### `ProductPrice` — 별도 애그리거트인 이유와 `original_price` 이중화

**대상**: `product/model/ProductPrice.java`, `product/service/ProductPriceService.java`(**03a로 `application/.../product/service/`로 이동**)

**`PRODUCT`에 컬럼을 붙이지 않고 별도 애그리거트인 이유**: 컬럼 방식으로는 가격명(보통/곱빼기)을 표현할 수 없다. 한 메뉴가 가격명을 가진 여러 가격 행을 갖는 구조가 요구사항이므로 행으로 분리한다.

**기존 `PRODUCT.original_price`는 지우지 않는다.** 주문·검색·오늘의할인·목록 등 수십 곳이 그 컬럼을 읽고 있어 한 번에 걷어내면 회귀 범위가 통제 불가능하다. `sort=0` 행의 배달가를 그 컬럼에 동기화해 유지하므로, **가격 행이 1개뿐인 메뉴(대부분)는 기존 동작이 완전히 그대로다** — 이 이중화가 이 설계의 안전장치이며, 단일화는 후속 과제로 남긴다.

**채널별 가격은 의미가 서로 다르다** — `deliveryPrice`는 배달·테이블·예약 주문의 실제 결제 가격(상시 변경 가능), `storePrice`는 '매장과 같은 가격' 뱃지의 근거일 뿐 **결제에 쓰이지 않는 표시 전용**, `pickupPrice`는 포장(`TAKEOUT`) 주문의 결제 가격(미설정이면 배달가를 쓴다).

`storePrice`·`pickupPrice`는 **매장 가격 인증 승인 후에만** 설정할 수 있다. 그 판정은 가게 애그리거트를 함께 읽어야 하므로 모델이 아니라 `ProductPriceService`가 소유한다.

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

### `MenuReview` — `Review`와 독립된 애그리거트

**대상**: `menureview/model/MenuReview.java`

**`reviewId`를 필드로 갖지 않으며, 두 평가의 유일한 연결고리는 `orderId`다.** 이 설계는 "매장 평가와 메뉴 평가 중 어느 것을 먼저 하든, 또는 하나만 하든 성립해야 한다"는 요구에서 나왔다 — **`reviewId`를 두는 순간 매장 리뷰 없이 메뉴 평가만 남기는 것이 구조적으로 불가능해진다.** 이 불변식은 컨텍스트 경계(`ContextBoundaryTest`)가 빌드로 강제한다.

**의도적으로 얇다.** 댓글·대댓글·좋아요·사장님답변·사장님만보기가 없는 것은 누락이 아니라 **"리뷰가 아니라 평가(rating)"라는 성격 규정**이다. 소셜 기능은 전부 매장 리뷰 쪽에만 남는다.

주문 항목당 1건 제약은 `UNIQUE(order_product_id)`가 물리적으로 보증한다 — 애플리케이션의 `existsByOrderProductId` 검사는 사용자에게 409를 돌려주기 위한 것이고, **동시 요청의 최종 방어선은 그 유니크 제약이다.**

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


### `GeoPolygonTextCodec` — 폴리곤·경계를 `LONGTEXT` 문자열로 담는 형식

**대상**: `shared/geo/GeoPolygonTextCodec.java` → `encode` · `encodeRings` · `decode` · `decodeRings` · `COORDINATE_SCALE`

> **덩어리 03b로 `infrastructure:persistence`의 `shared/persistence/GeoPolygonTextCodec`에서 이 모듈로 옮겼다(테스트 포함).** 과거 판단은 "좌표 인코딩 형식은 영속 계층의 지식이므로 코덱을 `..persistence..`에 가두고 매퍼(`ShopDeliveryAreaPolygonMapper`·`AdminDongMapper`)가 위임한다"였다 — **이 판단은 번복됐다.** persistence가 `domain`을 모르게 되면서(엄격 레이어드) `GeoRing`·`GeoPolygon`을 문자열로 바꾸는 쪽은 그 타입을 아는 계층이어야 했고, 형식은 `String.split`만 쓰는 순수 규칙이라 프레임워크-프리인 이 모듈에 두어도 잃는 것이 없다. ~~지금 코덱을 부르는 곳은 전부 `application`이다 — write 경로 `application/shop/store/ShopDeliveryAreaPolygonStateMapper`·`application/region/store/AdminDongStateMapper`. persistence는 인코딩된 문자열(`XxxSnapshot.encodedRings`)을 컬럼에 옮기기만 한다.~~ **(번복됨 — persistence domain 재허용: write 경로 호출부만)** 지금 write 경로는 persistence 매퍼 `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/persistence/ShopDeliveryAreaPolygonMapper.java`·`backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/region/persistence/AdminDongMapper.java`가 도메인 도형 ↔ 평탄 컬럼으로 직접 부른다(`AdminDongMapper`는 bbox min/max 계산도 한다). read 경로는 그대로 `application/shop/service/ShopDeliveryAreaPolygonQueryService`·`application/region/service/AdminDongQueryService`다 — 조회 DAO는 domain-free여야 하므로(`queryShouldNotDependOnDomain`) 문자열을 그대로 넘긴다. **코덱은 이 모듈에 그대로 둔다** — persistence가 domain을 다시 보게 됐지만 쓰는 쪽이 write 매퍼와 application 두 곳이라 공통 조상인 domain이 여전히 맞는 자리다. 같은 이동으로 persistence의 read 측 변환기 `GeoRingsResolver`와 계약 `GeoRingsQueryPort`는 필요가 없어져 삭제됐다.

인코딩 형식은 **링 구분 `;` · 점 구분 `,` · 점 내부는 `"경도 위도"`(공백 1칸) · 소수점 6자리 고정**이다. 정밀도는 위경도 저장 컬럼(`DECIMAL(9,6)`)과 맞췄다.

**MySQL `GEOMETRY`를 쓰지 않는 이유**: 공간 인덱스가 이득을 주는 질의가 설계상 없다(폴리곤 조회는 항상 `WHERE shop_id = ?` 단건이다). 반면 `hibernate-spatial`+JTS 의존, dialect 교체, SRID 4326 축순서 함정, 자기교차 도형에서의 `ST_Contains` 미정의 동작을 모두 떠안게 된다. 이 모듈은 production 의존이 0개로 강제되어 JTS `Geometry`를 둘 수도 없다.

**JSON을 쓰지 않는 이유**: 리포에 JSON 컬럼 선례가 없고, 이 모듈에는 Jackson이 없다(production 의존 0개). `String.split`만으로 끝나는 형식이라 의존을 늘릴 이유가 없다.

**좌표 순서가 "경도 위도"인 이유**: GeoJSON·WKT 등 공간 데이터 표준이 `(x, y) = (경도, 위도)` 순서를 쓰므로 저장 형식이 그 관례를 따른다. 다만 **API 경계에서는 `{latitude, longitude}` 객체로 주고받아** 순서 혼동을 없앤다 — 이 코덱은 API에 노출되지 않고, `GeoPoint` 복원 시 순서를 뒤집는다.

**실패 정책**: 형식이 깨진 입력은 `IllegalArgumentException`으로 실패시키고, `decodeRings`는 값이 없으면 빈 목록을 반환한다(행정동 경계 미보유가 정상 상태이므로). `decode`(단일 폴리곤)는 빈 결과를 허용하지 않는다. 금지 항목은 위 [봉인·가드 목록](#봉인가드-목록)의 해당 절.

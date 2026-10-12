<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# domain

## Purpose
모든 도메인의 핵심을 담는 라이브러리 모듈(`java-library`). 순수 POJO 도메인 모델(Aggregate Root), Value Object, DomainEvent 타입, 포트를 주입받지 않는 순수 계산기·정책·검증기(`*Calculator`·`*Policy`·`*Validator`)와 그 입출력 record, 공유 커널(`shared/{vo,geo,model}`), 예외 카탈로그(`exception/`)를 포함한다.

**write 포트·출력 포트·포트를 주입받는 도메인 서비스는 이 모듈에 없다** — 전부 `application`에 있다([ADR-0036](../../docs/adr/0036-app-marker-removal.md)). 위치·등록 방식·경계 테스트는 `application-core/AGENTS.md`가 정본이다.

| 이 모듈에 있는 것 | `application`에 있는 것 |
|---|---|
| `<ctx>/model/`·`<ctx>/vo/`·`<ctx>/event/`(이벤트 타입) | write 포트 `XxxLoadPort`·`XxxSavePort` → `application/<ctx>/port/out/write/` |
| 포트를 주입받지 않는 순수 계산기·정책(`CupDepositPolicy`·`EditorChoicePolicy`·`ShopDeliveryTipCalculator` 등)과 그 입출력 record | 출력 포트 → `application/<ctx>/port/out/` |
| `shared/{vo,geo,model}` | 포트를 주입받는 서비스 → `application/<ctx>/service/`(코어 `application-core` 또는 `{앱}-application`) |
| `exception/`(`ErrorCodeSpec`·`BusinessException`·`DomainErrorCode`·`DomainException`) | `DomainEventPublisher` → `application/shared/event/`, `OptimisticLockConflictException` → `application/shared/port/out/` |

### 컨텍스트 하위 패키지는 `model`·`vo`·`event` 셋이다 (domain `service` → `model` 흡수)

**이 모듈에는 `<ctx>/service/` 패키지가 없다.** 포트를 주입받지 않는 순수 계산기·정책·검증기와 그 입출력 record는 각 컨텍스트의 `<ctx>/model/`에 둔다(대상 8개 컨텍스트 — `coupon`·`member`·`order`·`payment`·`product`·`reservation`·`review`·`shop`, 테스트도 같은 경로).

| 항목 | before | after |
|---|---|---|
| 순수 계산·정책 위치 | `<ctx>/service/` (예: `product/service/CupDepositPolicy`, `shop/service/ShopDeliveryTipCalculator`) | `<ctx>/model/` (예: `product/model/CupDepositPolicy`, `shop/model/ShopDeliveryTipCalculator`) |
| 컨텍스트 하위 패키지 | `model` · `vo` · `service` · `event` | `model` · `vo` · `event` |
| 어디에 둘지 판정 | 포트를 받으면 `application/<ctx>/service/`, 아니면 domain `<ctx>/service/` | 포트를 받으면 `application/<ctx>/service/`, 아니면 domain `<ctx>/model/` |
| 동작 | — | 변경 없음. FQN만 바뀌었다. `SharedBeanConfig`의 `@Bean` 메서드 이름 기준이라 빈 이름도 그대로다 |

- **왜 `service` 패키지를 두지 않나**: domain에 사는 로직은 포트가 없는 순수 로직뿐이고, 컨텍스트 간 참조 규칙(`ContextBoundaryTest`)에서 그것은 `model`과 똑같이 "타 컨텍스트 import 금지" 대상이다. 패키지를 나눠도 경계 규칙상 달라지는 것이 없고 남는 차이는 역할 이름뿐이라 `model` 하나로 둔다. `event`는 타 컨텍스트에 허용된 공개 계약이라 분리해 둔다([ADR-0041](../../docs/adr/0041-domain-service-into-model.md)).
- **되살리지 않는다**: `DomainPurityTest#domainShouldNotHaveServicePackage`가 `com.tastyhouse.domain..service..`에 클래스가 생기면 빌드를 실패시킨다(main 클래스만 검사한다 — 테스트 소스는 대상이 아니다). 새 순수 계산기·정책도 `<ctx>/model/`에 두고, 빈이 필요하면 지금처럼 `application/shared/config/SharedBeanConfig`에 `@Bean`을 추가한다.
- **함께 바뀐 가드**: `ContextBoundaryTest.FORBIDDEN_CROSS_CONTEXT_SUBPACKAGES`에서 `"service"`를 뺐고, `SEALED_VIOLATIONS`의 FQN은 `com.tastyhouse.domain.shop.model.DeliveryAreaProjection`이 됐다. `application`의 `ServiceContextBoundaryTest#forbiddenSubpackageOf`도 domain 쪽 판정에서 `service`를 뺐다(application 쪽 `service` 판정은 그대로다).

**프레임워크를 전혀 모른다** — production 의존이 **하나도 없다**(Lombok까지 제거됨). Spring Web뿐 아니라 JPA·QueryDSL·`spring-tx`/`spring-orm`도 없으므로, `@Entity`/`@Transactional`/`@Service`/`@Component`/`com.querydsl.*`가 이 모듈에 단 한 곳도 없다. 예외의 HTTP 상태는 `int httpStatusCode`로 표현한다. 이 모듈을 의존하는 것은 application 계층 5모듈(`application-core`·`web-application`·`admin-application`·`ceo-application`·`batch-application`)과 `infrastructure:jpa`·`infrastructure:mybatis`이며, 전부 `implementation`이라 전이 노출이 없다. presentation(api 모듈·`api-common-module`·`security-*`)과 벤더 모듈은 이 모듈을 의존하지 않는다. jpa의 쓰기 어댑터 `XxxPersistenceAdapter`가 도메인 모델을 직접 저장하고, 엔티티 ↔ 도메인 변환과 `reconstitute` 호출은 jpa `<ctx>/persistence/XxxMapper`가 한다. jpa의 조회 DAO(`..query..`)는 이 모듈을 모른다(`queryShouldNotDependOnDomain`) ([ADR-0031](../../docs/adr/0031-persistence-domain-reallowed.md)).

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
- **프레임워크 import 금지**: `org.springframework.*`(`@Transactional`/`@Service`/`@Component` 포함)·`jakarta.persistence.*`·`com.querydsl.*`를 이 모듈에 추가하지 않는다. build.gradle에 해당 의존이 아예 없으므로 추가하려면 컴파일이 깨진다 — 필요한 관심사는 `infrastructure:jpa`로 보낸다. HTTP 상태는 `exception/DomainErrorCode`의 `httpStatusCode`(int)로만 표현한다. 도메인 코드는 `DomainException(DomainErrorCode)`로만 던진다 — `BusinessException`은 abstract라 직접 `new`할 수 없다.
- **`@Entity`는 이 모듈에 없다**: 도메인 모델은 전 도메인 순수 POJO이며, JPA 엔티티(`XxxJpaEntity`)·매퍼(`XxxMapper`)·`XxxPersistenceAdapter`·`BaseEntity`는 전부 `infrastructure:jpa`(`com.tastyhouse.infrastructure.jpa.<ctx>.persistence`)에 있다. `AttributeConverter`는 0건이고, 이 모듈의 모델을 `reconstitute`로 재구성하는 것은 jpa `XxxMapper`다(엔티티 필드는 `String`·`XxxEmbeddable`이므로 enum `valueOf`·VO `of`도 그 매퍼가 한다). 외부 애그리거트 참조는 ID VO(`MemberId` 등)로 하고, 자식 애그리거트도 별도 write 포트로 분리한다(JPA 연관관계 매핑 자체가 이 모듈에 존재할 수 없다).
- **도메인 모델 규칙**:
  - 신규 생성 `of(...)`(또는 `create(...)`/`register(...)`)와 DB 재구성 전용 `reconstitute(id, ..., createdAt, updatedAt)` 두 팩토리만 공개한다. `reconstitute`는 인프라(매퍼)만 호출하며(불변식 우회 방지, Javadoc 명시), `id`는 미영속이면 null이다. Java 계층에 생성 경로가 없는 read-only 애그리거트는 `reconstitute`만 둔다(reference: `shop/model/ProhibitedWord`). 조회 전용이고 도메인 불변식도 없는 데이터는 애그리거트를 두지 않고 infra `<ctx>/query/`의 Result DTO로만 노출한다(reference: `search`의 추천 검색어 — 도메인 모델 없이 `infrastructure/search/query/RecommendedKeywordResult`만 존재).
  - **재대입되지 않는 필드는 `final`로 선언**한다. `@Entity`와 달리 순수 POJO는 JPA 프록시/리플렉션 제약이 없으므로, 생성자(팩토리) 이후 상태전이로 바뀌지 않는 필드는 `id`뿐 아니라 상태 필드까지 모두 `final`로 둔다(전이되는 필드만 non-final). 불변성을 컴파일러가 강제하고 IntelliJ `may be 'final'` 경고를 차단한다. reference: `admin`의 `Admin`(update 경로 없어 전 필드 `final`).
  - **VO는 Java `record`로 선언**한다(검증은 compact constructor). 접근자는 record accessor(`value()`)로 통일하고 `toString()` 오버라이드는 남기지 않는다. reference: `shared/vo/PhoneNumber`, `shared/vo/VerificationCode`, `product/vo/ProductDiscountInfo`. 이 모듈의 VO는 JPA `@Embedded` 대상이 아니다 — jpa 엔티티는 자기 소유 `@Embeddable` record(`PhoneNumberEmbeddable`·`VerificationCodeEmbeddable`·`ProductDiscountInfoEmbeddable`·`OrderDeliveryDestinationEmbeddable`·`OrderScheduleEmbeddable`)를 매핑하고 컬럼 매핑(`@AttributeOverride`)도 jpa가 소유한다. 그래서 Hibernate canonical 생성자 요건과 알파벳순 컴포넌트 규약(`backend/infrastructure/jpa/src/test/java/com/tastyhouse/infrastructure/jpa/shared/persistence/EmbeddedRecordComponentOrderTest.java`)은 그 Embeddable record에 걸린다. 그래도 VO 컴포넌트 순서는 함부로 바꾸지 않는다 — jpa `XxxMapper`의 위치 기반 `new` 호출이 같은 타입끼리 조용히 어긋날 수 있다.
- **write 포트는 `application/<ctx>/port/out/write/`에 있다**(`XxxLoadPort`·`XxxSavePort`). write 포트에는 `findById`/`save`/`saveImmediately`/`delete`/`existsByX`(중복 검증)/`findByNaturalKey`/검증용 `countByX`/락 획득용 조회처럼 **불변식 검증·상태 전이에 필요한** 조회만 둔다. Result DTO·`PageResult` 반환, 조인 투영, 목록·검색·페이징 등 **표현 목적 조회는 write 포트에 두지 않고** `application`의 `{Ctx}QueryPort`(구현은 `infrastructure:jpa`의 `<ctx>/query/`)가 소유한다. 판정 기준: "이 조회가 없으면 불변식 검증이나 상태 전이가 불가능한가?"
- **포트를 주입받는 도메인 서비스는 `application/<ctx>/service/`에 있다** — `@Service`를 달고, 쓰는 앱 수에 따라 코어 `application-core` 또는 `{앱}-application` 모듈에 놓여 스캔으로 등록된다([ADR-0036](../../docs/adr/0036-app-marker-removal.md)). 이 모듈에는 **포트를 주입받지 않는 순수 계산기·정책과 그 입출력 record**(`<ctx>/model/`)만 둔다. 판정은 기계적이다 — 포트(write 포트·출력 포트·이벤트 발행 포트)나 그것을 주입받는 서비스를 생성자로 받으면 application, 아니면 domain이다(domain은 application을 볼 수 없으므로 순수 로직이 application 서비스를 주입해야 하면 그 로직도 application으로 간다 — `application-core`의 `shop/service/ShopRiderGuideValidator`가 그 사례).
- **순수 계산기·정책은 `@Service`/`@Component`/`@Transactional`을 붙이지 않는다.** 이 모듈이 spring-free라 애노테이션을 달 수 없으므로, 빈이 필요한 7개(`ProductExposureCalculator`·`CupDepositPolicy`·`StorePriceBadgePolicy`·`ShopOperatingStatusCalculator`·`ShopDeliveryTipCalculator`·`ShopNextOpenTimeCalculator`·`ScheduledOrderSlotCalculator`)는 `application-core`의 `shared/config/SharedBeanConfig` `@Bean`으로 등록된다. **새 순수 로직에 빈이 필요하면 `SharedBeanConfig`에 `@Bean`을 추가한다.** 트랜잭션 경계는 이를 호출하는 유스케이스 서비스가 소유한다.
- **명시적 save 규칙**: 도메인 모델은 POJO이므로 JPA 더티 체킹으로 자동 flush되지 않는다. 도메인을 변경한 뒤 **반드시 write 포트의 `save(domain)`을 호출**한다(누락 시 변경이 조용히 유실된다). 이 책임은 변경을 수행한 application 서비스(도메인 서비스·유스케이스 서비스)에 있다.
- **출력 포트도 이 모듈에 없다** — 파일·메일·SMS·결제·리뷰 통계 등 외부 시스템 출력 포트와 이벤트 발행 포트 `DomainEventPublisher`(구현 `SpringDomainEventPublisher`)는 전부 `application`의 `<ctx>/port/out/`·`shared/event/`에 있다. 각 포트의 구현 모듈은 `application-core/AGENTS.md`와 각 infrastructure 모듈 문서가 서술한다.
- **낙관적 락 충돌은 `OptimisticLockConflictException`(`application/shared/port/out/`)으로 표현**한다 — persistence가 던지고 application이 잡으므로 `port/out`에 있다. 스프링의 `ObjectOptimisticLockingFailureException`을 이 예외로 번역하는 책임은 `infrastructure:jpa`의 `ReservationSlotPersistenceAdapter`에 있고, 재시도 루프는 소비 모듈에 둔다(상세는 루트 CLAUDE.md "낙관적 락 재시도 배치 규칙").
- **command 파라미터는 원시 타입 또는 도메인 타입으로 받는다**: presentation의 Request 타입을 인자로 받는 팩토리·메서드를 두지 않는다(레이어 역전 방지). HTTP 경계는 `String`/`Long`으로 받고 api 모듈 서비스에서 `Enum.from(String)`·`XxxId.of(Long)`으로 승격한 뒤 이 모듈에 전달한다.
- **조회 결과 DTO를 `com.tastyhouse.domain..` 안에 두지 않는다**: Result record와 `SearchCondition`은 도메인 모델이 아니다. **읽기 계약은 전부 `application` 모듈의 `com.tastyhouse.application.<ctx>.port.out`이 소유한다** — 이 모듈에는 두지 않는다. 한때 다중 앱 공유분 55개를 이 모듈이 갖고 있었으나(모듈 재편 챕터 05), application 모듈이 하나로 통합되며 근거였던 앱 간 수평 의존 회피가 무의미해져 의존성 정리 챕터 04에서 되돌렸다. 접미어 `Result` 통일·`Dto` 금지·admin 충돌 시 `Management` 한정어 규칙은 위치와 무관하게 적용된다.
- **QueryDSL 동적 where 조립 규칙은 이 모듈 소관이 아니다**: `BooleanExpression` varargs 헬퍼 패턴은 QueryDSL을 소유한 `infrastructure:jpa`(`<ctx>/query/`의 QueryAdapter)의 규칙이다 — `infrastructure-module/AGENTS.md` 참고.

### Testing Requirements
- **순수 단위 테스트**가 원칙이다: 도메인 모델·도메인 서비스는 프레임워크 의존이 없으므로 스프링 컨텍스트나 DB 없이 JUnit만으로 불변식·상태전이를 검증한다(reference: `notice/model/NoticeTest` 등 도메인별 `XxxTest`).
- 새 애그리거트·상태전이를 추가하면 대응 단위 테스트를 함께 추가한다.
- JPA 매핑 정합성(`ddl-auto=validate`)·enum `columnDefinition` 검증은 엔티티를 소유한 `infrastructure:jpa`의 책임이다.

### Common Patterns
- write 포트: `application/<ctx>/port/out/write/`의 `XxxLoadPort`(조회)·`XxxSavePort`(변경, 삭제 포함) ← `infrastructure:jpa`의 `<ctx>/persistence/XxxPersistenceAdapter`(구현). 저장 시맨틱은 load-copy-save(id null이면 insert, 있으면 managed 엔티티 조회 후 `Mapper.applyChanges` 복사 — detached merge 금지).
- ID 강타입: `<ctx>/vo/XxxId`(`record XxxId(Long value)` + compact constructor 검증 + 정적 팩토리 `of`). 엔티티 FK는 raw `Long`이고 `AttributeConverter`는 없다. `XxxId.of`로의 승격은 jpa `<ctx>/persistence/XxxMapper`가 null 가드와 함께 한다.
- DomainEvent는 `<ctx>/event/`에 record로 정의하고, 발행은 `DomainEventPublisher` 포트를 통한다. 리스너는 `application-core`의 `<ctx>/listener/`에 `@Component`로 둔다 — 코어는 4앱 전부의 클래스패스에 있어 4앱 전부에 뜬다. 특정 앱 모듈에 두면 다른 앱이 트리거할 때 누락된다(근거는 `application-core/AGENTS.md`).
- 공유 커널: `shared/vo/PhoneNumber`, `shared/model/ApprovalStatus`, `shared/geo/`(`GeoPoint`·`GeoRing`·`GeoPolygon`·`GeoBoundingBox`·`InteriorPoint`·`PointInPolygon`·`GeoPolygonTextCodec`(도형 ↔ `LONGTEXT` 문자열 형식, 아래 설계 근거 절) 등). 페이징 계약 `PageQuery`·`PageResult`는 이 모듈이 아니라 `application`의 `shared/port/out/page/`에 있다.

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

### `SMS_VERIFICATION_CODE_NOT_FOUND` 외 3건 — 이름과 상태코드 불일치 봉인

> 가드 테스트는 `backend/application-core/src/test/java/com/tastyhouse/application/shared/exception/ErrorCatalogConventionTest.java`다. 이 테스트는 7개 카탈로그(`DomainErrorCode`·`ApplicationErrorCode`·`WebErrorCode`·`AdminErrorCode`·`CeoErrorCode`·`BatchErrorCode`·`ApiErrorCode`)를 한꺼번에 검사한다. 상수가 사는 enum은 소유 모듈 규칙(던지는 가장 안쪽 모듈)에 따라 갈린다 — 정본은 `backend/application-core/AGENTS.md`의 봉인·가드 목록 "에러 카탈로그 가드" 항목이다.

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/shared/exception/ErrorCatalogConventionTest.java`
→ `NOT_FOUND_NAME_WITH_NON_404_STATUS`

이름이 `*_NOT_FOUND`인데 404가 아닌 기존 상수들. 이미 프론트엔드가 분기하는 wire 계약(응답 status + code)이므로 지금 고치면 클라이언트가 깨진다. 교정 대상이 아니라 **봉인 대상**이다. **여기에 새 항목을 추가하지 말고, 신규 상수는 규약을 지킨다.**

봉인 구성원 4개 — 코드를 열지 않고 대조할 수 있도록 전부 열거한다.

- `SMS_VERIFICATION_CODE_NOT_FOUND`
- `MAIL_VERIFICATION_CODE_NOT_FOUND`
- `REFERRAL_REFERRER_NOT_FOUND`
- `FOLLOW_NOT_FOUND`

**짝 테스트(노후 감지)**: `ErrorCatalogConventionTest.sealedListsAreNotStale()` — 봉인 목록의 상수가 404로 고쳐졌으면 실패해서 목록에서 지우라고 알린다. 봉인이 영구 면죄부가 되지 않게 하는 장치다.

### 에러코드 `code` 문자열이 상수명과 의도적으로 다른 6건 — 봉인

**대상**: `backend/application-core/src/test/java/com/tastyhouse/application/shared/exception/ErrorCatalogConventionTest.java`
→ `CODE_INTENTIONALLY_DIFFERS_FROM_NAME`

채널 도메인 어휘 통일(mail/sms)로 상수명은 `SMS_`·`MAIL_` 접두어로 대칭화했지만, 응답 `code` 문자열은 프론트가 분기하는 wire 계약이라 예전 값(`VERIFICATION_CODE_*`·`EMAIL_VERIFICATION_CODE_*`)을 유지했다. 루트 `CLAUDE.md`의 "채널 도메인 어휘 통일 규칙"에 명시된 **의도적 불일치이므로 교정 대상이 아니다.**

봉인 구성원 6개.

- `SMS_VERIFICATION_CODE_NOT_FOUND`
- `SMS_VERIFICATION_CODE_EXPIRED`
- `SMS_VERIFICATION_CODE_MISMATCH`
- `MAIL_VERIFICATION_CODE_NOT_FOUND`
- `MAIL_VERIFICATION_CODE_EXPIRED`
- `MAIL_VERIFICATION_CODE_MISMATCH`

### 컨텍스트 경계 위반 1건 — 현상 동결 봉인

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/architecture/ContextBoundaryTest.java`
→ `SEALED_VIOLATIONS`

domain에는 25개 바운디드 컨텍스트가 한 모듈에 공존한다. 컨텍스트 간 참조는 **ID VO(`<ctx>.vo..`)·도메인 이벤트(`<ctx>.event..`)** 둘로만 허용하고, 타 컨텍스트의 `model..`/`repository..` 직접 import는 금지한다(`ContextBoundaryTest.FORBIDDEN_CROSS_CONTEXT_SUBPACKAGES`). `shared..`·`exception..`은 컨텍스트가 아니라 전 컨텍스트 공용이므로 전면 허용한다.

**기존 위반은 고치지 않고 봉인한다.** **이 목록은 줄어들기만 해야 한다 — 항목을 추가하는 것은 새 위반을 승인하는 것이므로 금지한다.** 위반을 해소했다면 그 클래스를 목록에서 지운다.

봉인 구성원 1개.

- `com.tastyhouse.domain.shop.model.DeliveryAreaProjection`

포트를 주입받는 도메인 서비스 사이의 컨텍스트 경계는 서비스가 사는 `application`의 `ServiceContextBoundaryTest.SEALED_VIOLATIONS`가 같은 규칙(봉인 + 짝 테스트)으로 받는다 — `application-core/AGENTS.md` 참고.

**짝 테스트 2종**.

- `ContextBoundaryTest.sealedViolationsShouldNotBeStale()` — 목록에 있으나 더 이상 위반하지 않는 클래스가 있으면 실패해 지우라고 알린다.
- `ContextBoundaryTest.sealedViolationListShouldNotBeEmpty()` — 목록이 비면 실패해서 **봉인 장치 자체(`SEALED_VIOLATIONS`·짝 테스트)를 제거하고 규칙을 순수 강제로 전환하라**고 알린다. 목록이 비면 위 짝 테스트가 검사 대상을 잃어 공허하게 통과하기 때문이다.

**모든 규칙은 `allowEmptyShould(true)` 없이 선언한다**(공허 통과 금지 — `DomainPurityTest`·`LayerRulesTest` 개정 선례).

### 컨텍스트 간 순환 — 예외 없이 강제

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/architecture/ContextBoundaryTest.java`
→ `contextsShouldBeFreeOfCycles()`

**`beFreeOfCycles()`를 예외 없이 강제한다.** 봉인 목록(`SEALED_CYCLES`)을 두지 않으므로 새 순환이 생기면 그대로 빌드가 실패한다 — 봉인 목록을 만들지 말 것. 컨텍스트 순환을 만들던 간선은 포트 주입 도메인 서비스에서 나왔고 그 서비스는 `application`에 있으므로, 서비스 사이 순환 성분 `"order,product,review,shop"`은 `application`의 `ServiceContextBoundaryTest.SEALED_CYCLES`가 봉인한다(쌍이 아니라 SCC 단위).

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

### `GeoPolygonTextCodec`의 실패를 조용히 넘기지 않는다

**대상**: `backend/domain/src/main/java/com/tastyhouse/domain/shared/geo/GeoPolygonTextCodec.java` → `decode` · `decodeRings` · `COORDINATE_SCALE`

- **형식이 깨진 입력은 `IllegalArgumentException`으로 실패시킨다** — 조용히 건너뛰면 도형의 일부가 사라진 채 복원되어, 점주가 그린 것과 다른 배달지역이 저장된 것처럼 보인다.
- 저장 형식은 "경도 위도" 순서이고 `GeoPoint`는 (위도, 경도) 순서이므로 **복원 시 뒤집는 자리를 지우지 않는다.**
- `decodeRings`가 값 없음(`null`·빈 문자열)에 빈 목록을 돌려주는 분기는 지우지 않는다 — 행정동 경계는 단계적으로 투입되므로 미보유가 정상 상태다.
- `MySQL GEOMETRY`·JSON으로 되돌리지 않는다 — 근거는 아래 "`GeoPolygonTextCodec` — 폴리곤·경계를 `LONGTEXT` 문자열로 담는 형식" 절.
- 회귀 테스트는 `backend/domain/src/test/java/com/tastyhouse/domain/shared/geo/GeoPolygonTextCodecTest.java`다.

### import 순서 가드 — `ImportOrderConventionTest`

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/architecture/ImportOrderConventionTest.java`
→ `importsFollowConvention` · `TOP_SEGMENT_RANK` · `PRESENTATION_SHARED_SEGMENTS` · `MINIMUM_SCANNED_FILES`
짝 설정: `backend/domain/build.gradle` → `tasks.named('test')`의 `importOrderSources` 입력

backend 전체 `*.java`를 **소스 파일로** 읽어(`build`·`bin`·`.gradle` 제외) import 블록이 `backend/CLAUDE.md` "코딩 스타일 (import 순서)" 규칙대로인지 검사한다. 클래스패스가 아니라 소스를 읽는 이유는 import 순서가 바이트코드에 남지 않기 때문이다.

- **순위 표는 두 곳에 있다** — `backend/CLAUDE.md`의 표와 이 테스트의 `TOP_SEGMENT_RANK`·`PRESENTATION_SHARED_SEGMENTS`. **표를 바꾸면 두 곳을 함께 바꾼다.**
- **3순위(driven 어댑터)는 `infrastructure` 하나다.** infrastructure 모듈의 패키지는 전부 `com.tastyhouse.infrastructure.{모듈}` 아래에 있으므로 `TOP_SEGMENT_RANK`에 `external`·`restclient` 같은 다른 세그먼트를 두지 않는다 — 그런 세그먼트가 생기면 아래 규칙대로 실패한다.
- **표에 없는 최상위 세그먼트는 실패시킨다.** 새 모듈이 생겼는데 순위가 정해지지 않은 상태를 드러내기 위해서다. 테스트를 느슨하게 바꾸지 말고 순위를 정해 두 곳에 추가한다.
- **`MINIMUM_SCANNED_FILES`(3000) 단정을 지우지 않는다.** backend 루트(`settings.gradle`이 있는 디렉터리)를 잘못 찾아 0개를 스캔하고 공허하게 통과하는 것을 막는다(`allowEmptyShould(true)` 금지와 같은 취지).
- **`build.gradle`의 `importOrderSources` 입력 선언을 지우지 않는다.** import 순서만 바꾼 변경은 바이트코드가 같아서, 이 선언이 없으면 Gradle이 `:domain:test`를 UP-TO-DATE로 건너뛰어 위반이 통과한다(실측 확인).
- **모듈 하나만 빌드하면(`./gradlew :ceo-api:build`) 이 테스트는 돌지 않는다.** 전체 `./gradlew build`나 `:domain:test`에서만 잡힌다. 모듈마다 복사하는 방식은 유지비가 커서 채택하지 않았다.

### 타입 본문 첫 줄 빈 줄 가드 — `TypeBodyBlankLineConventionTest`

**대상**: `backend/domain/src/test/java/com/tastyhouse/domain/architecture/TypeBodyBlankLineConventionTest.java`
→ `typeBodiesStartWithSingleBlankLine` · `violatingLines` · `typeBodyBraces` · `isViolation` · `MINIMUM_SCANNED_FILES`
짝 설정: `backend/domain/build.gradle` → `tasks.named('test')`의 `importOrderSources` 입력 (이름은 import 가드 때 지었지만 `:domain:test` 태스크 전체의 입력이라 이 가드도 보호한다)

backend 전체 `*.java`를 소스 파일로 읽어(`build`·`bin`·`.gradle` 제외) 타입 본문 여는 중괄호 다음 줄이 빈 줄 정확히 1개인지 검사한다. 규칙과 제외 대상은 `backend/CLAUDE.md` "타입 본문 첫 줄 빈 줄 규칙" 절이 정본이다.

- **판정을 정규식 한 줄로 단순화하지 않는다.** 단순 매칭은 `public void record(...)` 메서드, `(record, markers) -> {` 람다, `for (JavaClass record : ...)` 반복문을 타입으로 오인해 메서드·람다 본문에 빈 줄을 요구한다(도입 당시 실측 오탐 5건 — `ShopRequestIndexRecorder` 등). 그래서 스캐너는 ① 리터럴·텍스트 블록·주석을 건너뛰고 ② 키워드를 완전한 식별자로만 인정하며(직전 토큰 `.` 제외) ③ 바로 다음 토큰이 식별자(타입 이름)일 때만 선언으로 보고 ④ 괄호·제네릭 깊이가 음수가 되거나 `;`를 만나면 판정을 취소한다.
- **반증 테스트 7개를 지우지 않는다.** 위 오탐 사례와 `@Target({...})`·`sealed … permits`·`>>>` 제네릭·텍스트 블록·`'{'` 문자 리터럴이 각각 단정돼 있다. 스캐너를 고치면 이 테스트가 먼저 깨져야 한다.
- **`MINIMUM_SCANNED_FILES`(3000) 단정을 지우지 않는다.** backend 루트를 잘못 찾아 0개를 스캔하고 공허하게 통과하는 것을 막는다.
- **실패 메시지는 원소를 최대 1,000개까지만 보여 준다**(AssertJ 기본 표시 한도). 위반이 그보다 많아도 목록이 잘린 것일 뿐이다.
- **모듈 하나만 빌드하면 이 테스트는 돌지 않는다.** import 순서 가드와 같다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거. 챕터 03(domain 모듈) 이관분 -->

이 절은 `domain` 각 컨텍스트의 java 주석에 있던 **설계 근거**를 옮긴 것이다. 비즈니스 규칙(분류 C)은 `docs/domain/{도메인}.md`에 있으므로 여기 중복 기술하지 않고 링크한다.

### 모듈 전역 — 프레임워크-프리와 명시적 저장

**대상**: `domain` 전 컨텍스트의 model

도메인 모델은 JPA/Spring에 의존하지 않는 POJO다. 영속화는 `infrastructure:jpa`의 `{X}JpaEntity` + `{X}Mapper`가 담당한다. **POJO라 더티 체킹이 없으므로, 변경 후에는 application 서비스가 write 포트의 `save`를 명시적으로 호출해야 한다** — 이것을 빠뜨리면 변경이 조용히 유실된다.

이 모듈의 순수 계산기·정책은 `@Service`/`@Transactional` 없는 POJO이며, **빈 등록은 `application-core`의 `shared/config/SharedBeanConfig` `@Bean`이 담당하고 트랜잭션 경계는 호출하는 유스케이스 서비스가 선언한다.** 포트를 주입받는 서비스는 `application`에 산다.

도메인은 인증을 모르므로, **변경 주체(`ShopChangeActor` 등)는 마지막 파라미터로 명시 전달받는다.** 도메인이 `LocalDate.now()`를 직접 부르지 않는 것도 같은 이유다 — 기준일을 파라미터로 받아야 시계 조작 없이 경계값을 테스트할 수 있다.

### 배달팁·최소주문금액 규격은 외부 가이드가 강제한다 — 내부 판단으로 완화 금지

**대상**: `backend/ceo-application/src/main/java/com/tastyhouse/application/shop/service/ShopDeliveryTipService.java`, `shop/model/ShopDeliveryTipTier.java`·`ShopDeliveryTipHoliday.java`·`Shop.java` → `validateMinOrderAmount`, `shared/geo/GeoDistance.java` → `distanceMeters`

아래 규격은 이 저장소가 정한 비즈니스 정책이 아니라 **외부 배달 플랫폼 가이드가 강제하는 요건**이다. 원문 주석이 "배민 가이드 강제 규격"·"배민 가이드 원문"으로 출처를 밝히고 있었으므로, **불합리해 보여도 내부 판단으로 완화·재협상하지 말고 그 가이드가 실제로 바뀌었는지부터 확인한다.**

- 구간별 배달팁 상한 **5,000원 미만**(5,000원 자체 불가)
- 구간 단조성 — **주문금액이 오르면 배달팁은 내려가야 한다**
- 거리별 ↔ 지역별 **상호 배타**
- 공휴일 팁은 날짜별이 아니라 **가게당 단일 금액**("법정 공휴일에 일괄 부과")
- 거리 기준은 도로 경로가 아니라 **직선거리**
- 최소주문금액은 **픽업(포장)에 적용하지 않는다**

규칙 본문은 [`docs/domain/shop.md` § 배달팁 · § 최소주문금액](../../docs/domain/shop.md)에 있고, 이 절은 **그 출처가 외부라는 사실**만 담는다 — 그 사실이 사라지면 후임자가 평범한 내부 규칙으로 오인해 바꾸게 된다.

### 순수 계산기 형태 (리포지토리 0개·상태 0개)

**대상**: `backend/domain/src/main/java/com/tastyhouse/domain/shop/model/ShopDeliveryTipCalculator.java`, `backend/domain/src/main/java/com/tastyhouse/domain/shop/model/ShopOperatingStatusCalculator.java`

두 계산기는 **리포지토리 주입 0개, 인스턴스 상태 0개**다. 좌표→거리 변환과 날짜→공휴일 판정은 호출부가 끝내고 `ShopDeliveryTipContext`에 **이미 해석된 값**으로 담아 넘기므로, 계산기는 Spring·DB·시계 없이 단위 테스트할 수 있다. 이 형태를 깨고 리포지토리를 주입하면 그 테스트 가능성이 사라진다.

배달팁 항목 간 우선순위(거리별↔지역별 배타, 공휴일 > 시간별 **대체**, 시간별은 구체성 우선으로 하나만)는 비즈니스 규칙이므로 [`docs/domain/shop.md` § 배달팁](../../docs/domain/shop.md)에 있다.

### 점주가 켤 수 없고 admin·시스템만 바꾸는 플래그

**대상**: `shop/model/Shop.java` → `cupDepositEnabled`, `storePriceVerified`

**`cupDepositEnabled`는 점주의 영업 설정이 아니라 외부 규제 사실이다.** 환경부·자원순환보증금관리센터가 지역(제주·세종)과 사업자 규모로 지정하며, 지정·해제가 운영 이벤트로 발생한다. 그래서 점주가 스스로 켤 수 없고 **admin만 토글한다.** 주소 문자열로 지역을 파싱해 판정하지 않는 이유도 같다 — 같은 지역이어도 지정 사업자가 아닐 수 있고, 지정 여부는 주소에서 도출되는 값이 아니다.

**`storePriceVerified`는 '매장과 같은 가격' 뱃지 노출과 매장가·픽업가 설정 가능 여부를 함께 가른다.** 관리자가 가격표 이미지와 실제 매장을 대조해 승인할 때만 켜지며(`StorePriceVerificationService`), 메뉴 가격이 바뀌어 **배달가 > 매장가**가 되면 가격 저장 시점에 **동기**로 내려간다 — 배치로 미루면 그 사이 손님이 잘못된 뱃지를 본다.

### `ProductPrice` — 별도 애그리거트인 이유와 `original_price` 이중화

**대상**: `product/model/ProductPrice.java`, `backend/ceo-application/src/main/java/com/tastyhouse/application/product/service/ProductPriceService.java`

**`PRODUCT`에 컬럼을 붙이지 않고 별도 애그리거트인 이유**: 컬럼 방식으로는 가격명(보통/곱빼기)을 표현할 수 없다. 한 메뉴가 가격명을 가진 여러 가격 행을 갖는 구조가 요구사항이므로 행으로 분리한다.

**기존 `PRODUCT.original_price`는 지우지 않는다.** 주문·검색·오늘의할인·목록 등 수십 곳이 그 컬럼을 읽고 있어 한 번에 걷어내면 회귀 범위가 통제 불가능하다. `sort=0` 행의 배달가를 그 컬럼에 동기화해 유지하므로, **가격 행이 1개뿐인 메뉴(대부분)는 기존 동작이 완전히 그대로다** — 이 이중화가 이 설계의 안전장치이며, 단일화는 후속 과제로 남긴다.

**채널별 가격은 의미가 서로 다르다** — `deliveryPrice`는 배달·테이블·예약 주문의 실제 결제 가격(상시 변경 가능), `storePrice`는 '매장과 같은 가격' 뱃지의 근거일 뿐 **결제에 쓰이지 않는 표시 전용**, `pickupPrice`는 포장(`TAKEOUT`) 주문의 결제 가격(미설정이면 배달가를 쓴다).

`storePrice`·`pickupPrice`는 **매장 가격 인증 승인 후에만** 설정할 수 있다. 그 판정은 가게 애그리거트를 함께 읽어야 하므로 모델이 아니라 `ProductPriceService`가 소유한다.

### 에러 카탈로그 규약 — 그룹 주석에서 이관

**대상**: `domain/src/main/java/com/tastyhouse/domain/exception/DomainErrorCode.java`(이 모듈 소유 153개). 아래 표의 `INVALID_INPUT`은 `backend/application-core/src/main/java/com/tastyhouse/application/shared/exception/ApplicationErrorCode.java`, `AUTH_REQUIRED`는 `WebErrorCode`(미러 `ApiErrorCode`), `ACCESS_DENIED`는 `ApiErrorCode`에 있다.

에러코드는 그것을 던지는 가장 안쪽 모듈의 카탈로그가 소유한다 — 이 모듈은 `DomainErrorCode`, 나머지는 `ApplicationErrorCode`·`{Web,Admin,Ceo,Batch}ErrorCode`·`ApiErrorCode`. `BusinessException`은 abstract(protected 생성자, `ErrorCodeSpec` 수신)이고 domain은 `DomainException(DomainErrorCode)`만 던진다. wire 계약(HTTP 상태·code 문자열·메시지)은 `ErrorCatalogSnapshotTest`가 지킨다([ADR-0042](../../docs/adr/0042-error-code-module-split.md)).

상수 그룹의 단순 분류 라벨(`// 주문`·`// 쿠폰`)은 상수명 접두어(`ORDER_`·`COUPON_`)가 같은 정보를 담으므로 이관하지 않고 삭제했다. **아래는 코드만 읽어서는 알 수 없는 규약**이다. 봉인 목록 관련 서술(이름·상태코드 불일치 4건, code 문자열 상이 6건)은 위 [봉인·가드 목록](#봉인가드-목록)에 있다.

| 상수 | 용도 한정 |
|---|---|
| `INVALID_INPUT` (400) | **Command record의 compact constructor 가드 전용.** 형식·범위 검증은 Request의 `jakarta.validation`이 담당하므로, 이 코드는 인바운드 어댑터를 우회해 Command가 직접 조립된 경우의 **필수값 누락 같은 구조적 위반**에만 쓴다 |
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

`AdminDong`은 **일반 요청 경로에 생성·변경이 없는 마스터**다 — web/admin/ceo 어느 api 모듈도 만들지 않고 조회만 한다. 유일한 생성 경로는 batch-module의 행정동 경계 동기화 배치이며, 외부 원천(통계청 SGIS 파생 행정동 경계)을 읽어 마스터 전체를 교체한다(좌표·경계까지 3,500여 건이라 사람이 시드로 관리할 수 없다). `ADMIN_DONG`은 감사 컬럼이 없는 마스터 테이블이라 감사 시각을 필드로 두지 않는다.

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

> **코덱이 이 모듈에 있는 이유**: `GeoRing`·`GeoPolygon`을 문자열로 바꾸는 쪽은 그 타입을 아는 계층이어야 하고, 형식은 `String.split`만 쓰는 순수 규칙이라 프레임워크-프리인 이 모듈에 두어도 잃는 것이 없다. 코덱을 쓰는 곳은 두 계층이다 — write 경로는 jpa 매퍼 `backend/infrastructure/jpa/src/main/java/com/tastyhouse/infrastructure/jpa/shop/persistence/ShopDeliveryAreaPolygonMapper.java`·`backend/infrastructure/jpa/src/main/java/com/tastyhouse/infrastructure/jpa/region/persistence/AdminDongMapper.java`(도메인 도형 ↔ 평탄 컬럼, `AdminDongMapper`는 bbox min/max 계산도 한다), read 경로는 ceo-application `shop/service/ShopDeliveryAreaPolygonDetailQueryService`·`ShopDeliveryAreaPolygonPreviewQueryService`·`region/service/AdminDongBoundaryQueryService`다. 조회 DAO는 domain-free여야 하므로(`queryShouldNotDependOnDomain`) 문자열을 그대로 넘긴다. 쓰는 쪽이 둘이라 공통 조상인 이 모듈이 맞는 자리다.

인코딩 형식은 **링 구분 `;` · 점 구분 `,` · 점 내부는 `"경도 위도"`(공백 1칸) · 소수점 6자리 고정**이다. 정밀도는 위경도 저장 컬럼(`DECIMAL(9,6)`)과 맞췄다.

**MySQL `GEOMETRY`를 쓰지 않는 이유**: 공간 인덱스가 이득을 주는 질의가 설계상 없다(폴리곤 조회는 항상 `WHERE shop_id = ?` 단건이다). 반면 `hibernate-spatial`+JTS 의존, dialect 교체, SRID 4326 축순서 함정, 자기교차 도형에서의 `ST_Contains` 미정의 동작을 모두 떠안게 된다. 이 모듈은 production 의존이 0개로 강제되어 JTS `Geometry`를 둘 수도 없다.

**JSON을 쓰지 않는 이유**: 리포에 JSON 컬럼 선례가 없고, 이 모듈에는 Jackson이 없다(production 의존 0개). `String.split`만으로 끝나는 형식이라 의존을 늘릴 이유가 없다.

**좌표 순서가 "경도 위도"인 이유**: GeoJSON·WKT 등 공간 데이터 표준이 `(x, y) = (경도, 위도)` 순서를 쓰므로 저장 형식이 그 관례를 따른다. 다만 **API 경계에서는 `{latitude, longitude}` 객체로 주고받아** 순서 혼동을 없앤다 — 이 코덱은 API에 노출되지 않고, `GeoPoint` 복원 시 순서를 뒤집는다.

**실패 정책**: 형식이 깨진 입력은 `IllegalArgumentException`으로 실패시키고, `decodeRings`는 값이 없으면 빈 목록을 반환한다(행정동 경계 미보유가 정상 상태이므로). `decode`(단일 폴리곤)는 빈 결과를 허용하지 않는다. 금지 항목은 위 [봉인·가드 목록](#봉인가드-목록)의 해당 절.

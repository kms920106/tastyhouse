# ADR-0028: persistence가 domain을 모르게 State·StatePort·Store 계층을 둔다(덩어리 03b)

- 상태: 대체됨(→ [ADR-0031](0031-persistence-domain-reallowed.md))
- 결정 커밋: `cdd6c7727` (2026-09-27)
- 대체한 결정: [ADR-0004](0004-entity-fk-raw-long-idmapping.md)

## 맥락

**엄격 레이어드 프로그램**은 바깥 계층이 안쪽 계층의 내부 모델을 모르게 의존을 자르는 작업이었습니다. 덩어리 01은 표현 계층(api·api-common·security)을([ADR-0025](0025-presentation-domain-cut.md)), 덩어리 02는 벤더 16모듈을([ADR-0026](0026-vendor-ports-to-application.md)) domain에서 끊었습니다. 덩어리 03a는 포트와 포트를 주입받는 도메인 서비스를 application으로 옮겼습니다([ADR-0027](0027-domain-ports-to-application.md)).

03a가 끝나도 `infrastructure:persistence`(지금의 `infrastructure:jpa`)는 `:domain`을 의존했습니다. 서비스가 주입하는 write 포트 `XxxPersistencePort`의 시그니처가 도메인 모델(`Optional<Notice> findById(NoticeId)` 등)이었고, persistence가 그 인터페이스를 직접 구현했기 때문입니다. 게다가 의존이 `api`라서 domain이 persistence를 거쳐 앱 쪽으로 전이됐습니다.

- **State record**: 도메인 모델의 필드를 원시 타입(`String`·`Long`·`boolean` 등)으로만 담은 record입니다.
- **Store**: application 안에서 도메인 모델 ↔ State 변환을 맡는 어댑터입니다.

## 이전 규칙

### backend/infrastructure/jpa/AGENTS.md (당시 `infrastructure/persistence/AGENTS.md`)

- persistence는 `api project(':domain')`을 의존하고, `<ctx>/persistence/XxxPersistenceAdapter`가 도메인 모델 시그니처의 write 포트를 직접 구현했습니다. 매퍼 `XxxMapper`는 `toDomain(entity)`·`toEntity(domain)`·`applyChanges(entity, domain)`를 가졌습니다.
- 엔티티 enum 필드는 도메인 enum + `@Enumerated(EnumType.STRING)`이었습니다. `columnDefinition = "VARCHAR(n)"`을 지우면 Hibernate 6 `MySQLDialect`가 네이티브 `ENUM(...)`을 기대해 `ddl-auto: validate`가 부팅을 거부했습니다.
- `@Embedded` 대상은 domain VO record(`PhoneNumber`·`VerificationCode`·`ProductDiscountInfo`·`OrderDeliveryDestination` 등)였습니다.
- 매퍼는 FK의 VO 승격·언패킹을 `shared/persistence/IdMapping`의 `vo`·`raw` 헬퍼로만 했습니다. 직접 `XxxId.of(...)`를 부르면 FK가 실제로 null인 행에서만 예외가 나므로, nullable 여부와 무관하게 모든 매퍼가 헬퍼를 썼습니다([ADR-0004](0004-entity-fk-raw-long-idmapping.md)).
- 조회 DAO는 enum 라벨을 `shared/query/EnumLabelProjection.labelOf(x.status, Status::getDescription)`로 채웠고, enum 비교는 `.eq(OrderStatus.COMPLETED)`처럼 도메인 상수를 썼습니다.
  - 덩어리 01 당시 enum 컬럼은 `x.status.stringValue()`로 투영했습니다. `EnumLabelProjection`은 `FileUrlProjection`과 같은 `MappingProjection`으로 null-safe였고, 정적 호출부가 0개여도 `map(Tuple)`을 지우지 않았습니다.
  - 상관 서브쿼리가 enum 식을 돌려줄 때는 `.stringValue()` 대신 `labelOf(..., ReviewBlindStatus::name)`으로 Java 쪽에서 상수명을 꺼냈습니다(`ShopReviewManagementQueryAdapter#latestBlindRequestStatus`).
  - `ProjectionConstructorMatchingTest`는 `labelOf(...)` 래퍼 인자를 이름 판정에서 제외했습니다.
- `ReservationQueryAdapter#existsBlockingReservation`은 차단 대상 상태로 `ReservationStatus.blockingStatuses()`를 직접 참조했습니다(목록 복제 금지).
- `OrderQueryAdapter#withUnwrappedAmount(PaymentProjection)`가 `Amount` VO를 `Integer`로 언랩해, api 모듈이 `Amount.value()`를 부르지 않게 했습니다. `payment/persistence/AmountConverter`가 그 VO 컬럼을 매핑했습니다.
  - `PAYMENT.amount`가 `@Convert` 매핑이라 QueryDSL이 `SimplePath<Amount>`를 생성했고, `Projections.constructor`는 투영식 안에서 변환할 수 없어 infra 내부 중간 투영 `PaymentProjection`(`public record`)으로 받은 뒤 fetch 직후 언랩했습니다. 이 자리는 `apiModuleShouldBeDomainModelFree`의 유일한 비-enum 위반이었습니다(챕터 07).
  - `@Convert`/`AmountConverter` 자체는 write 엔티티에 유지했고, 바뀐 것은 읽기 계약의 타입뿐이었습니다. FK용 `*IdConverter`는 정책 B 전환으로 이미 삭제됐고 `@Convert`는 ID가 아닌 값 객체(`AmountConverter`)에만 남아 있었습니다.
- 폴리곤 인코딩 코덱은 `shared/persistence/GeoPolygonTextCodec`에 있었습니다. 근거는 "좌표 인코딩 형식은 영속 계층의 지식이므로 `..persistence..`에 가두고 매퍼가 위임한다"였습니다. read 측 변환기 `shared/query/GeoRingsResolver`와 계약 `GeoRingsQueryPort`도 있었습니다.
  - 코덱이 "그 경계 안쪽(영속 계층)에만 존재"하고 `ShopDeliveryAreaPolygonMapper`·`AdminDongMapper`가 위임한다고 적었습니다.
  - `GeoRingsResolver`를 별도 빈으로 둔 이유는 api 모듈이 `..persistence..`에 의존할 수 없고(`shouldNotDependOnInfrastructurePersistence`) api가 인코딩 형식을 알면 저장 형식 변경 때 api까지 고쳐야 하기 때문이었습니다. `FileUrlResolver`와 같은 형태로 read 측이 완성형을 내려보냈고, 소비 모듈은 계약 `GeoRingsQueryPort`를 주입했습니다.
- `AdminDongMapper`는 도메인 경계에서 바운딩박스를 파생했고, `toEntity`와 `applyChanges`가 같은 파생 헬퍼를 썼습니다. `ShopDeliveryAreaMapper`는 `source`가 null인 행을 `MANUAL`로 보았고, `ReviewMapper`는 `product_id`의 `null`·`0` 이하를 정규화한 뒤 `ProductId` 승격을 건너뛰었습니다.
- 컬럼 길이 봉인 대상은 `@Enumerated` 엔티티 12개(`CeoLoginHistoryJpaEntity`·`NotificationJpaEntity`·`ProductFeedbackJpaEntity`·`ProductOptionGroupJpaEntity`·`ProductOptionGroupMergeHistoryJpaEntity`·`ShopCeoAssignmentHistoryJpaEntity`·`ShopChangeHistoryJpaEntity`·`ShopDeliveryAreaAdjustmentRequestJpaEntity`·`ShopDeliveryTipSettingJpaEntity`·`ShopRequestIndexJpaEntity`·`ShopRequestCommentJpaEntity`·`ShopRiderGuideHistoryJpaEntity`)였습니다.
- 투영 중간 record 선례 목록에 `PaymentProjection`이 있었습니다.

### backend/CLAUDE.md — enum ↔ DB 컬럼 매핑 규칙

- 도메인 enum을 엔티티 필드로 영속화할 때는 반드시 `@Enumerated(EnumType.STRING)`로 매핑하고 `@Column`에 `length`와 `columnDefinition = "VARCHAR(n)"`을 함께 명시했습니다. `@Enumerated(EnumType.ORDINAL)`은 선언 순서를 저장해 상수 추가·재배열이 기존 데이터 의미를 바꾸므로 전 도메인 금지였습니다.
- 근거는 Spring Boot 3.2.x(Hibernate ORM 6.4.x)가 dialect 미지정으로 `MySQLDialect`를 자동 감지하고, Hibernate 6.2+의 `MySQLDialect`가 `@Enumerated(STRING)`을 네이티브 `ENUM(...)`으로 기대한다는 것이었습니다. 동등한 대안 `@JdbcTypeCode(SqlTypes.VARCHAR)`가 있었으나 확립된 관례인 `columnDefinition`으로 통일했습니다.
- 선례: `BugReport`의 enum 필드만 `columnDefinition`을 누락해 `wrong column type ... found [varchar], but expecting [enum (...)]`로 SessionFactory 빌드가 실패했습니다. reference 구현은 `Order`(`order_status` `VARCHAR(20)`, `order_method` `VARCHAR(50)`)·`MemberReferral`·`PaymentRefund`, 수정 후 `BugReport`였습니다.

### backend/CLAUDE.md — ID VO(식별자 값 객체) 경계 규칙 — 엔티티 FK는 raw `Long`, 매퍼가 삼항 null 가드로 승격/언패킹 (정책 B)

- reference 구현은 `shared/persistence/IdMapping`(vo/raw 헬퍼), nullable FK 3개(`ceoId`/`thumbnailImageFileId`/`trademarkImageFileId`)를 포함한 `shop/persistence/ShopMapper`, raw `Long` `orderId`에 `AmountConverter`만 `@Convert`로 쓰던 `PaymentJpaEntity`였습니다.

### backend/application-core/AGENTS.md (당시 `application/AGENTS.md`)

- `LayerRulesTest#readContractsShouldBeFrameworkFree`는 `port.out` 계약이 `java..`·`com.tastyhouse.domain..`·자기 자신을 참조하도록 허용했습니다.
- `*SearchCondition`·`{Ctx}QueryPort` 파라미터에 도메인 enum·`XxxId`를 실을 수 있었습니다.

### backend/domain/AGENTS.md

- `@Embedded` 대상 VO는 record로 선언했습니다. Hibernate 6이 canonical 생성자로 인스턴스화하기 때문이며, 컬럼 매핑은 각 `XxxJpaEntity`의 `@AttributeOverride`가 소유했습니다.

## 결정

write 경로에 상태 record 한 겹을 끼워, persistence가 domain을 몰라도 저장할 수 있게 합니다. 서비스가 주입하던 도메인 타입 인터페이스는 `application/<ctx>/store/`로 옮기고 이름·시그니처는 그대로 둡니다. 그 인터페이스를 application의 `XxxStore`가 구현해 도메인 모델을 `XxxState`로 바꾼 뒤, 원시 타입 포트 `XxxStatePort`에 넘깁니다. persistence는 `XxxStatePort`만 구현합니다.

```
03a : Service → XxxPersistencePort(도메인) ← persistence XxxPersistenceAdapter
03b : Service → store/XxxPersistencePort(도메인) ← XxxStore ─(XxxStateMapper)→ XxxStatePort(State) ← persistence XxxStatePortImpl
```

| 항목 | before (03a까지) | after (03b) |
|---|---|---|
| persistence의 프로젝트 의존 | `api project(':domain')` + application | `implementation project(':application')` 하나, `com.tastyhouse.domain..` import 0건 |
| persistence가 구현하는 write 포트 | `port/out/write/XxxPersistencePort`(도메인 시그니처) | `port/out/write/XxxStatePort`(State·`Long`·`String` 시그니처, 메서드 이름은 같음) |
| 구현 클래스 | `XxxPersistenceAdapter` | `XxxStatePortImpl`(`git mv` 개명) 105개. 도메인 타입을 쓰지 않는 `StationPersistenceAdapter`만 그대로 |
| 도메인 ↔ State 변환 | 없음 | application `store/XxxStore` + `XxxStateMapper`(`final`, package-private, `toDomain`·`toState`) |
| Store 등록 | — | 마커 없는 POJO + `<Ctx>ServiceConfig`(`@SharedApp`)의 `@Bean` |
| 엔티티 enum 필드 | 도메인 enum + `@Enumerated(STRING)` | `String`, `@Enumerated` 0건. `@Column(columnDefinition = "VARCHAR(n)")`은 글자 하나 바꾸지 않음(DDL 불변) |
| `@Embedded` 대상 | domain VO record | persistence 소유 `@Embeddable` record `XxxEmbeddable` 5종 |
| FK의 VO 승격 | persistence 매퍼의 `IdMapping` | application `XxxStateMapper#toDomain`의 삼항 null 가드(모든 FK에 예외 없이) |
| DAO의 enum 비교 | `.eq(OrderStatus.COMPLETED)` | `port/out`의 `XxxCodes` 복제 상수 |
| enum 라벨 | DAO의 `EnumLabelProjection` | DAO는 `Expressions.nullExpression(String.class)`로 자리만 두고 QueryService가 Result wither로 채움 |
| 도메인 예외·정책 호출 | 일부 어댑터·DAO | 0건 — `Optional`/`boolean`/원자료를 돌려주고 Store·QueryService가 판정·예외화 |
| `readContractsShouldBeFrameworkFree` | domain 허용 | `java..`·`port.out..`만 허용 |
| 읽기 계약의 enum·ID | 도메인 enum·`XxxId` | `String`·`Long`. QueryService가 검증 후 강등해 넘김 |
| 삭제 | — | `IdMapping`(+테스트), `EnumLabelProjection`, `GeoRingsResolver`·`GeoRingsQueryPort`, `PaymentProjection`, `AmountConverter` |
| 이동 | — | `GeoPolygonTextCodec` → `domain/shared/geo`, `StorePriceVerificationAdapter` → `application/shop/service` |

규모는 State 122(자식 State 포함), StatePort 105, Store 105, persistence `XxxStatePortImpl` 105입니다.

부속 규칙도 함께 정했습니다.

- State 컴포넌트는 도메인 `reconstitute` 파라미터와 이름·순서·개수가 같습니다. `StateRecordArityTest`가 최상위 컴포넌트 수를 검사하고, 순서·이름 뒤바뀜은 `*StateMapperTest`(81개)의 왕복 비교가 잡습니다.
- 복합 VO는 `XxxSnapshot` record로 펼칩니다. `*State` 접미사를 쓰면 `StateRecordArityTest`가 애그리거트로 보고 `reconstitute`를 찾기 때문입니다.
- State·StatePort는 `port/out/write/`에 둡니다. persistence가 application 중 `..port.out..`만 볼 수 있기 때문입니다.
- 도메인 타입을 쓰지 않는 write 포트는 State로 바꾸지 않습니다.
- `queryServicesShouldNotDependOnWritePorts`의 대상에 `..store..`를 추가하고, `RuleAnchorTest#storesExist`(≥ 105)를 새로 둡니다. persistence에는 `LayerRulesTest#infrastructureShouldNotDependOnDomain`을 새로 둡니다.

## 근거

- `port.out`은 infrastructure가 보는 유일한 application 표면입니다. 여기에 domain 타입이 실리면 persistence가 다시 domain을 알게 되므로, 계약은 `java..`와 `port.out`만 참조하게 했습니다.
- persistence가 domain enum 타입을 선언할 수 없게 되므로 엔티티 enum 필드는 `String`이 되고, `@Enumerated` 규칙은 쓸 수 없습니다.
- `GeoRing`·`GeoPolygon`을 문자열로 바꾸는 쪽은 그 타입을 아는 계층이어야 합니다. 인코딩 형식은 `String.split`만 쓰는 순수 규칙이라, 프레임워크-프리인 domain에 두어도 잃는 것이 없습니다.
- 커밋 메시지는 HTTP 응답·DDL·저장값이 불변이고 동작 변경이 없다고 밝힙니다. 서비스 코드는 import 한 줄만 바뀌었습니다.

## 결과·감수한 손실

- **enum 문자열의 앞뒤 공백 trim 차이를 감수했습니다.** 03a까지는 Hibernate `@Enumerated(STRING)`이 `Enum.valueOf(cls, value.trim())`으로 복원해 `'ACTIVE '`도 읽었습니다. 03b부터는 `StateMapper.toDomain`이 trim 없이 `Enum.valueOf`를 불러 같은 값이 `IllegalArgumentException`(500)이 됩니다. 앱이 쓰는 값은 전부 `name()`이라 수동 입력·레거시 데이터에만 해당하므로 감수했고, 그런 데이터는 매퍼가 아니라 데이터를 정정하기로 했습니다.
- `columnDefinition`을 지워도 더는 부팅이 실패하지 않지만, 떼지 않는 금지는 유지했습니다. `@Column`을 바꾸지 않는 것이 03b의 불변식이고, `n`은 `schema.sql` 길이와의 대응을 코드에 남기는 유일한 자리이기 때문입니다.
- `withUnwrappedAmount` 봉인은 대상이 사라졌습니다. `PaymentJpaEntity.amount`가 `Integer` 필드가 되어 DAO가 `OrderPaymentResult`에 직접 투영합니다.
- domain의 VO는 더 이상 `@Embedded` 대상이 아닙니다. Hibernate canonical 생성자 근거와 알파벳순 컴포넌트 규약(`EmbeddedRecordComponentOrderTest`)은 `XxxEmbeddable`에 걸립니다.
- 커밋 리뷰에서 배달팁 거리 단위 400 응답, 정산 `memberId` 전 행 검증, `bulkUpdateGrade` null 허용, 리뷰 정렬 default 분기를 되살려 기존 동작을 맞췄습니다.
- `XxxCodes` 복제 상수는 [ADR-0029](0029-no-suppress-warnings.md)에서 삭제됐고, 지금 DAO는 비교값을 포트 인자로 받습니다.
- 03b 당시 부속 사항: 파일럿은 `notice`였습니다(`store/NoticePersistencePort`·`port/out/write/NoticeState`·`NoticeStatePort`·`store/NoticeStore`·`NoticeStateMapper`·`config/NoticeServiceConfig`·persistence `NoticeStatePortImpl`·`NoticeStateMapperTest`). `StateRecordArityTest`의 예외 목록 `NON_AGGREGATE_STATES`는 비어 있었고 짝 테스트 `nonAggregateStatesShouldNotBeStale`이 붙었습니다. 복합 VO는 `OrderDeliveryDestinationSnapshot`·`OrderScheduleSnapshot`·`ProductDiscountInfoSnapshot`·`AdminDongBoundarySnapshot`·`AdminDongCenterSnapshot`·`ShopDeliveryAreaPolygonShapeSnapshot`·`ShopDeliveryAreaPolygonCenterSnapshot`으로 펼쳤습니다. `writePortsExist`(≥ 106)는 `port.out.write` + `store`의 `*Repository` 합계를 셌습니다.
- Store를 스캔으로 등록하지 않은 이유는 `@Component`를 붙이면 앱 격리 규칙 `beansShouldHaveExactlyOneAppMarker`에 걸리고 스캔과 `@Bean`이 겹치면 기동이 실패하기 때문이었습니다. persistence 구현이 4앱 전부에 있어 `@SharedApp`으로 등록했습니다. 새 `<ctx>/store/`는 `port.out` 밖이라 `ServiceContextBoundaryTest`의 금지 대상에 자동으로 들어갔습니다.
- 도메인 예외와 정책 호출은 Store가 맡았습니다(예: `ReservationStore`가 `ReservationStatus.blockingStatuses()`를 원시값으로 풀어 `ReservationStatePort#existsBlockingByMemberShopDate(..., Collection<String> blockingStatuses)`에 넘김). 조회 DAO `existsBlockingReservation`도 같은 목록을 파라미터로 받게 됐습니다. 도메인 타입을 쓰는 보조 포트 `ShopDeliveryTipRegionLookupPort`는 `shop/store/`로 갔고, 원시 필드뿐인 `AdminDongSyncResult`는 `region/port/out/write/`에 남았습니다.
- QueryService는 `OrderStatus.from(orderStatus).name()`·`OrderId.of(id).value()`로 검증 후 강등해 넘겨 잘못된 입력의 400 응답 경로를 유지했습니다. 라벨 참고 구현은 당시 `ShopChangeHistoryQueryService` → `ShopChangeHistoryResult#withDescriptions`, `CeoLoginHistoryQueryService` → `CeoLoginHistoryResult`였습니다. 라벨 슬롯이 `null`로 도착하는 것이 정상이라, 라벨 컴포넌트를 추가할 때는 DAO의 `nullExpression`과 QueryService의 채우기를 한 벌로 넣습니다.
- 경계 인코딩·바운딩박스 파생은 `AdminDongStateMapper#toBoundarySnapshot`으로 옮겨 `AdminDongBoundarySnapshot(encodedRings, minLatitude, maxLatitude, minLongitude, maxLongitude)` 한 record로 넘겼고, `ShopDeliveryAreaMapper`의 null → `MANUAL` 판정은 `ShopDeliveryAreaStateMapper#toDomain`으로, `ReviewMapper`는 정규화만 남기고 승격 건너뛰기는 `ReviewStateMapper#toDomain`으로 나뉘었습니다. 코덱 테스트 `GeoPolygonTextCodecTest`도 domain으로 함께 옮겼습니다.
- 위치 기반 매퍼 대조가 한 겹 늘었습니다(도메인 `reconstitute` 순서 = State 컴포넌트 순서 · `XxxStateMapper` 인자 순서 · persistence `XxxMapper` 인자 순서). 도메인 ↔ State 쪽은 round-trip 테스트가, State ↔ 엔티티 쪽은 3중 대조가 유일한 방어선이었습니다.
- `SEALED_PERSISTENCE_TO_QUERY` 3건은 재판정 후 유지했습니다. 세 어댑터가 채우는 값 타입이 application `port.out`(예: `application/rank/port/out/MemberReviewCount`)으로 바뀌었지만 여전히 `..persistence..`에서 `..query..`를 부르기 때문입니다.
- 쓰기 경로는 다음 날 [ADR-0031](0031-persistence-domain-reallowed.md)이 되돌렸습니다. 엔티티의 `String` 컬럼·`XxxEmbeddable` 5종, 읽기 계약·조회 DAO의 domain-free, `GeoPolygonTextCodec`의 domain 배치, QueryService의 라벨 채우기는 그대로 남았습니다.

## 영향받은 문서

- `backend/infrastructure/jpa/AGENTS.md` · `enum 필드는 문자열로 투영한다 — 라벨은 QueryService가 채운다` · `EnumLabelProjection`은 없고, DAO는 `nullExpression` 슬롯만 두며 QueryService가 라벨을 채운다
- `backend/infrastructure/jpa/AGENTS.md` · `enum 컬럼의 \`columnDefinition\`을 떼지 않는다` · 부팅 실패가 아니라 DDL 불변식과 `schema.sql` 대응 때문에 떼지 않는다
- `backend/infrastructure/jpa/AGENTS.md` · `VO 승격을 null 가드 없이 직접 호출하지 않는다` · `IdMapping` 대신 매퍼의 삼항 null 가드가 승격한다
- `backend/infrastructure/jpa/AGENTS.md` · `\`@Embedded\` record VO — 컴포넌트 선언 순서는 이름 알파벳 오름차순이다` · 대상은 이 모듈의 `XxxEmbeddable` 5종이다
- `backend/infrastructure/jpa/AGENTS.md` · `영속 모델을 도메인 모델과 분리하는 이유` / `크로스 애그리거트 FK는 raw \`Long\`이고, VO 변환은 매퍼의 삼항 null 가드가 한다` / `enum 컬럼은 \`String\` 필드 + \`columnDefinition\` 병기다` / `\`GeoPolygonTextCodec\` — 폴리곤을 \`LONGTEXT\`에 담는다` · 03b가 만든 엔티티 형태가 현행으로 남은 자리
- `backend/infrastructure/jpa/AGENTS.md` · `결제 금액은 \`Integer\` 컬럼을 그대로 투영한다` · `AmountConverter`·`withUnwrappedAmount` 없이 직접 투영한다
- `backend/CLAUDE.md` · `엔티티 FK는 raw \`Long\`, 매퍼가 삼항 null 가드로 승격/언패킹 (정책 B)` · 제목은 링크 때문에 유지하고, `IdMapping`은 되살리지 않았다
- `backend/CLAUDE.md` · "enum ↔ DB 컬럼 매핑 규칙 (엔티티 enum 컬럼은 `String` 필드 + `@Column(columnDefinition = "VARCHAR(n)")`)" · 제목이 현재 규칙(String 필드)으로 바뀌었고, 엔티티는 도메인 enum을 필드로 갖지 않는다
- `backend/CLAUDE.md` · `도메인 enum 경계 규칙` · enum 강등·승격의 경계
- `backend/application-core/AGENTS.md` · `덩어리 03b — 읽기 계약은 domain을 모른다` / `읽기 계약 평탄화 — \`port/out\`은 domain을 모른다` / `persistence에서 이 모듈로 올라온 도메인 판단` · 03b 중 현행으로 남은 읽기 경로 규칙
- `backend/application-core/AGENTS.md` · `매퍼·enum 비교값 봉인 항목` · 매퍼 테스트 값·null 가드·enum 비교값 규칙
- `backend/domain/AGENTS.md` · `\`GeoPolygonTextCodec\` — 폴리곤·경계를 \`LONGTEXT\` 문자열로 담는 형식` · 코덱이 domain에 있는 이유
- `backend/domain/AGENTS.md` · `Working In This Directory` · VO를 record로 두는 근거

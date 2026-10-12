# ADR-0031: persistence가 도메인 모델을 직접 쓰도록 State 계열을 제거한다(쓰기 경로만, 조회 DAO는 domain-free 유지)

- 상태: 채택
- 결정 커밋: `9d6f4a1ba` (2026-09-28)
- 대체한 결정: [ADR-0028](0028-persistence-state-layer.md)

## 맥락

[ADR-0028](0028-persistence-state-layer.md)(덩어리 03b)은 persistence가 domain을 모르게 하려고 쓰기 경로에 번역 계층을 끼웠습니다. 그 결과 애그리거트 하나를 저장하려면 파일 5개를 거쳐야 했습니다.

- **State**: 도메인 모델을 원시 타입으로 펼친 record
- **StatePort**: State를 주고받는 원시 타입 포트
- **Store**: 도메인 ↔ State 변환 어댑터(application)
- **StateMapper**: Store가 쓰는 변환기
- **StatePortImpl**: persistence 구현

변환도 Entity → State → Domain으로 두 번 일어났습니다. 규모는 State 122 · Snapshot 7 · StatePort 105 · Store 105 · StateMapper 121 · StateMapper 테스트 81개였습니다.

## 이전 규칙

### backend/application-core/AGENTS.md — 덩어리 03b

- 서비스가 주입하는 도메인 타입 인터페이스는 `store/XxxPersistencePort`였고, 마커 없는 POJO `store/XxxStore`가 이를 구현했습니다. Store는 `XxxStatePort`를 생성자로 받았고, `<Ctx>ServiceConfig`(`@SharedApp`)의 `@Bean`으로 등록됐습니다.
- State 컴포넌트는 `reconstitute` 파라미터와 1:1이어야 했고, `StateRecordArityTest`가 개수를, `*StateMapperTest`가 왕복 비교(`toDomain(toState(original))`)로 순서를 검사했습니다.
- 도메인 예외와 도메인 정책 호출(예: `ReservationStatus.blockingStatuses()`)은 Store가 맡았고, StatePort는 `Optional`/`boolean`만 돌려줬습니다.
- State record 모양: 컴포넌트는 도메인 `reconstitute` 파라미터와 같은 순서·같은 개수였고, enum → `String`, ID·값 VO → 원시값, 복합 VO → `XxxSnapshot` record(예: `OrderDeliveryDestinationSnapshot` — `XxxState` 접미사는 애그리거트 전용), 자식 컬렉션 → `List<ChildState>`로 펼쳤습니다. 도메인 타입을 쓰지 않는 write 포트 `StationPersistencePort`(`existsById(Long)` 하나)는 State화하지 않고 `port/out/write/`에 남긴 예외였습니다.
- "DAO는 원자료만 돌려주고, 판정·계산·예외화는 QueryService·Store가 한다." enum 비교값은 Store가 만들어 StatePort 인자로 넘겼습니다. 예: `ReviewBlindRequestStore`의 종결 상태 상수 → `ReviewBlindRequestStatePort#existsByReviewIdAndStatusIn(reviewId, statuses)`, `MailVerificationStore#expireAllPendingByEmail` → `MailVerificationStatePort#changeStatusByEmail(email, PENDING, EXPIRED)`, 예약 차단 상태는 `ReservationStore`가 `ReservationStatus.blockingStatuses()`의 `name()` 목록으로 만들었습니다. 도메인 타입 `XxxPersistencePort` 시그니처는 그대로여서 도메인 타입 fake(`FakeMailVerificationPersistencePort` 등)는 영향을 받지 않았습니다.
- 매퍼 테스트 봉인 "같은 타입의 연속 필드(`String`·`Long`·`boolean`·`LocalDateTime`)는 반드시 서로 다른 값으로 채운다"의 대상은 `backend/application-core/src/test/java/com/tastyhouse/application/<ctx>/store/*StateMapperTest.java` 81개였습니다. `StateRecordArityTest`는 개수만 보므로 같은 타입 순서 뒤바뀜을 잡는 것은 이 테스트뿐이었습니다.
- `readContractsShouldBeFrameworkFree`는 `port.out..` 전체(`write/` 포함)가 `java..`·`port.out..`만 의존하게 했습니다. `queryServicesShouldNotDependOnWritePorts`의 대상은 `..port.out.write..` + `..store..`였고, `RuleAnchorTest#storesExist`(≥ 105)가 Store 수를 지켰습니다.

### backend/infrastructure/jpa/AGENTS.md (당시 `infrastructure/persistence/AGENTS.md`)

- 이 모듈은 domain을 몰랐습니다. 프로젝트 의존은 `implementation project(':application')` 하나였고, `LayerRulesTest#infrastructureShouldNotDependOnDomain`이 모듈 전체의 domain 참조를 금지했습니다.
- 구현 클래스는 `XxxStatePortImpl`, 매퍼는 `toState(entity)`·`toEntity(state)`·`applyChanges(entity, state)`였습니다.
- Dependencies 절: `application`(implementation) 한 줄이 유일한 프로젝트 의존이었고, `port/out/write`에는 `XxxState`·`XxxSnapshot`·`XxxStatePort`가 있었습니다. 이 모듈의 매퍼 테스트는 `*MapperTest` 5개뿐이었습니다.
- 낙관적 락 번역은 `ReservationSlotStatePortImpl`이 했습니다(`OptimisticLockConflictException`은 03a로 `application/shared/port/out/`에 있어 persistence가 domain 없이 던질 수 있었습니다).

### backend/domain/AGENTS.md

- 이 모듈을 의존하는 것은 `application` 하나였습니다. `reconstitute`로 모델을 재구성하는 것은 `application/<ctx>/store/XxxStateMapper`였습니다.
- `GeoPolygonTextCodec`의 write 경로 호출부는 `application/shop/store/ShopDeliveryAreaPolygonStateMapper`·`application/region/store/AdminDongStateMapper`였습니다.

### backend/CLAUDE.md — 모듈 지도

- "`infrastructure:persistence`는 `domain`을 모른다 — application만 의존한다"고 적었습니다.

### backend/CLAUDE.md — ID VO 경계 규칙 > 엔티티 FK는 raw `Long`, 매퍼가 삼항 null 가드로 승격/언패킹 (정책 B)

- 03b 동안 VO ↔ raw 변환은 application `<ctx>/store/XxxStateMapper`(`toDomain(state)`/`toState(domain)`, package-private)가 삼항 null 가드 인라인(`state.ceoId() == null ? null : CeoId.of(state.ceoId())`)으로 했고, persistence 매퍼는 VO를 만지지 않았습니다. 참고 구현은 `backend/application-core/src/main/java/com/tastyhouse/application/shop/store/ShopStateMapper.java`였습니다.
- ID VO 표에는 `XxxStatePort` · `XxxState` record · persistence `XxxStatePortImpl`이 `Long`을 쓰는 행이 있었습니다.

### backend/CLAUDE.md — 코딩 스타일 (import 순서) > 자사 그룹 내부 계층 정렬

03b 시점 persistence 행은 `2 · 3`이었고 "1은 나올 수 없습니다 — persistence는 domain을 모릅니다"라고 적었습니다.

## 결정

쓰기 경로의 State 계열을 전부 걷어내고, persistence가 도메인 모델을 직접 쓰도록 되돌립니다. 읽기 경로(읽기 계약과 조회 DAO)는 03b의 domain-free 상태로 둡니다.

```
03b  : Service → XxxPersistencePort(도메인) ← XxxStore ─(XxxStateMapper)→ XxxStatePort(State) ← persistence XxxStatePortImpl
after: Service → XxxPersistencePort(도메인) ← persistence XxxPersistenceAdapter (XxxMapper: JpaEntity ↔ Domain)
```

| 항목 | before (03b) | after |
|---|---|---|
| persistence의 domain 의존 | 없음 | `implementation project(':domain')`(`api`가 아님) |
| 도메인 타입 write 포트 위치 | `store/XxxPersistencePort` | `port/out/write/`로 복귀(이름·시그니처 불변) |
| State · Snapshot · StatePort · Store · StateMapper | 있음 | 삭제(`store` 패키지 전체) |
| persistence 구현 | `XxxStatePortImpl`(State 반환) | `XxxPersistenceAdapter`(`@Repository`, 도메인 반환) 106개 |
| persistence 매퍼 | JpaEntity ↔ State | JpaEntity ↔ Domain. StateMapper의 표현식(null 가드·`valueOf`·`Xxx.of`)을 그대로 흡수 |
| Store가 하던 write 쪽 판단 | Store | persistence `XxxPersistenceAdapter`(빈 컬렉션 조기 반환·`LinkedHashSet` 수집·도메인 정책 상수 호출) |
| 매퍼 테스트 | application `store/*StateMapperTest` 81개(왕복 비교) | persistence `XxxMapperTest` — Domain→Entity / Entity→Domain 두 방향을 따로 검증 |
| Store 전용 `*ServiceConfig` | Admin·Banner·Event·Notice·Partnership·Region | 파일째 삭제 |
| 엔티티 `String` enum 컬럼 · `XxxEmbeddable` 5종 | 있음 | 유지 |
| persistence domain 가드 | `infrastructureShouldNotDependOnDomain`(모듈 전체) | `queryShouldNotDependOnDomain`(`..query..` + 봉인 조회 어댑터 3개 `SEALED_PERSISTENCE_TO_QUERY`만) |
| `readContractsShouldBeFrameworkFree` | `port.out..` 전체 | `..port.out.write..`만 제외 |
| `writePortsShouldOnlyDependOnDomainAndPortOut` | 없음 | 신설 — `port.out.write`는 `java..`·`com.tastyhouse.domain..`·`application..port.out..`만 |
| `queryServicesShouldNotDependOnWritePorts` 대상 | `..port.out.write..` + `..store..` | `..port.out.write..`만 |
| `StateRecordArityTest` · `RuleAnchorTest#storesExist` | 있음 | 삭제 |

## 근거

- **비용이 격리 이득보다 컸습니다.** 애그리거트 하나를 저장하려고 파일 5개를 거치고 변환이 두 번 일어나는 비용에 비해, 얻는 것은 "persistence가 domain을 모른다"는 격리뿐이었습니다.
- **의존 방향이 규칙에 맞습니다.** persistence는 원래 도메인 포트의 어댑터이므로, domain을 아는 방향이 의존 규칙(안쪽을 향함)에 맞습니다.
- **presentation 절단은 유지됩니다.** `implementation` 의존이면 domain이 앱 컴파일 클래스패스로 새지 않으므로, 덩어리 01([ADR-0025](0025-presentation-domain-cut.md))의 절단이 그대로입니다.
- **엔티티 String 컬럼을 되돌리지 않은 이유**: 조회 DAO 31개 파일이 String 컬럼을 193곳에서 씁니다. enum으로 되돌리면 `Projections.constructor` 투영이 컴파일은 통과하고 런타임에만 깨집니다.
- **매퍼 테스트를 두 방향으로 나눈 이유**: Entity를 거치면 원본 값 일부가 사라집니다. `XxxJpaEntity.create(...)`는 id를 받지 않고, `BaseEntity`의 `createdAt`/`updatedAt`은 auditing이 채우며 setter가 없습니다.

## 결과·감수한 손실

- 커밋 메시지는 HTTP 응답·DDL·저장값·null 처리·load-copy-save가 불변이고 동작 변경이 없다고 밝힙니다.
- `MailVerificationPersistencePort`·`SmsVerificationPersistencePort` 구현 빈이 web 전용에서 전 앱으로 넓어졌습니다. 03b 이전과 같은 상태이고, admin·ceo·batch에는 주입처가 없습니다.
- 03b가 만든 enum trim 차이는 남았습니다. 그 표현식을 persistence `XxxMapper#toDomain`이 그대로 옮겨 받았기 때문입니다.
- `GeoPolygonTextCodec`은 domain에 그대로 둡니다. 쓰는 쪽이 persistence write 매퍼와 application 조회 서비스 두 곳이라, 공통 조상인 domain이 여전히 맞는 자리입니다.
- `EnumLabelProjection`은 되살리지 않았습니다. 조회 DAO가 domain-free를 유지하므로 라벨은 계속 QueryService가 채웁니다.
- `RuleAnchorTest` 하한을 재조정했습니다(441·1500).
- `port/out/write/`에는 옮겨 온 `XxxPersistencePort` 105개와 `ShopDeliveryTipRegionLookupPort`, 원래 있던 `StationPersistencePort`·`AdminDongSyncResult`가 남았습니다. Store 하나가 인터페이스 둘을 구현하던 경우(`ShopDeliveryTipRegionLookupPort`)는 PersistenceAdapter도 둘을 구현합니다. `RuleAnchorTest#writePortsExist`는 `port.out.write` + `store`의 `*Repository` 합계 ≥ 106에서 `port.out.write`의 `*Repository` ≥ 106으로 바뀌었습니다(이후 [ADR-0046](0046-write-port-load-save-split.md)로 ≥192).
- Store에 있던 enum 비교값·도메인 정책 상수 호출은 persistence로 내려갔습니다(예: `ReviewBlindRequestPersistenceAdapter`, `MailVerificationPersistenceAdapter`, `ReservationPersistenceAdapter`가 차단 상태 목록을 직접 만든다). 조회 DAO 쪽의 "DAO는 원자료만" 원칙은 그대로입니다.
- ID 변환은 persistence `<ctx>/persistence/XxxMapper`(package-private) 한 단계로 돌아왔지만 형태는 03b의 삼항 null 가드를 그대로 씁니다. `IdMapping`은 복원하지 않았습니다 — 표현식을 바꾸면 동작이 달라질 수 있어서입니다. 참고 구현은 `ShopMapper`(`ceoId` 등 nullable FK)입니다.
- persistence `XxxMapperTest`의 Entity→Domain 방향은 `ReflectionTestUtils.setField`로 id·감사 필드를 채운 엔티티로 검증합니다. 왕복 비교를 남긴다면 `ignoringFields("id", "createdAt", "updatedAt")`만 허용합니다. "같은 타입 연속 필드는 서로 다른 값으로 채운다" 봉인은 규칙 그대로 대상만 `backend/infrastructure/jpa/src/test/java/com/tastyhouse/infrastructure/jpa/<ctx>/persistence/*MapperTest.java`로 바뀌었습니다.
- Embeddable 없이 평탄 컬럼으로 저장되는 VO(`AdminDong` 경계·중심, 배달 폴리곤 모양·중심)는 매퍼가 평탄 컬럼(`GeoPolygonTextCodec` 포함)으로 직접 매핑하고, `AdminDongMapper`가 bbox min/max를 계산합니다. 기존 매퍼가 복사하지 않던 필드(`version`·`createdAt`·`updatedAt` 등)는 `applyChanges`로 계속 복사하지 않습니다.
- 존재하지 않는 id 저장 같은 프로그래밍 오류는 `IllegalStateException`이고, 낙관적 락 번역은 `ReservationSlotPersistenceAdapter`로 이름만 되돌아왔습니다(예외 위치 `port/out`은 그대로).
- import 순서 표에서 persistence 쓰기 어댑터에 1순위(domain)가 돌아왔습니다.
- 당시 참고 구현은 notice였으나, notice가 JPA·MyBatis 공존 예외가 되면서([ADR-0053](0053-notice-mybatis-legacy.md)) 구현 1개 기본형인 banner(`BannerPersistenceAdapter`·`BannerMapper`)로 바뀌었습니다.

## 영향받은 문서

- `backend/application-core/AGENTS.md` · `persistence domain 재허용 — persistence가 도메인 모델을 직접 쓴다` (하위 `한 컨텍스트의 파일 구성 — \`notice\`` · `write 어댑터 작성 규칙` · `매퍼 테스트는 두 방향으로 나눠 검증한다` · `가드`) · write 경로에 번역 계층을 다시 끼우지 않는다
- `backend/application-core/AGENTS.md` · `덩어리 03b — 읽기 계약은 domain을 모른다` · 읽기 경로는 domain-free 그대로
- `backend/application-core/AGENTS.md` · `앱 간 동명 클래스는 정상이다` / `읽기 계약을 이 모듈이 소유한다` · write 포트는 `port/out/write/`에 있다
- `backend/infrastructure/jpa/AGENTS.md` · `infrastructure:jpa` / `규칙` / `Dependencies` > `Internal` · domain을 `implementation`으로 의존하고, 조회 DAO는 `queryShouldNotDependOnDomain`으로 막는다
- `backend/infrastructure/jpa/AGENTS.md` · `\`SEALED_PERSISTENCE_TO_QUERY\` 3건 — read→write 단방향 위반 봉인` · domain 금지 대상에 포함된 봉인 조회 어댑터
- `backend/CLAUDE.md` · `모듈 지도 (모듈 재편 완료 + application 모듈 통합 + external 분리 + 앱 모듈 재분리)` · jpa가 domain을 `implementation`으로 의존한다
- `backend/CLAUDE.md` · `도메인 모델 / JPA 엔티티 분리 규칙 (선별 적용, persistence는 \`infrastructure-module\`로)` · 쓰기 어댑터가 도메인 모델을 직접 다룬다
- `backend/CLAUDE.md` · `ID VO(식별자 값 객체) 경계 규칙` / `엔티티 FK는 raw \`Long\`, 매퍼가 삼항 null 가드로 승격/언패킹 (정책 B)` · 변환 위치가 persistence 매퍼로 돌아왔다
- `backend/CLAUDE.md` · `infrastructure-module 계층 방향 규칙 (ArchUnit — \`LayerRulesTest\`)` · persistence의 domain 가드 범위
- `backend/AGENTS.md` · `Module Dependency Graph` / `Common Patterns` · persistence → domain 간선
- `backend/domain/AGENTS.md` · `Purpose` / `Working In This Directory` / `Common Patterns` · 이 모듈을 의존하는 쪽과 `reconstitute` 호출 위치
- `backend/domain/AGENTS.md` · `\`GeoPolygonTextCodec\` — 폴리곤·경계를 \`LONGTEXT\` 문자열로 담는 형식` · write 경로 호출부가 persistence 매퍼다

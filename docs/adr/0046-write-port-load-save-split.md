# ADR-0046: 쓰기 포트를 LoadPort·SavePort로 목적별 분리한다

- 상태: 채택
- 결정 커밋: `085fa5559` (2026-10-09)
- 대체한 결정: [ADR-0032](0032-outbound-port-adapter-naming.md)(일부 — `XxxPersistencePort` 단일 포트)

## 맥락

- **쓰기 포트**: `application/<ctx>/port/out/write/`에 있는 아웃바운드 포트로, 명령 경로가 애그리거트를 읽고 저장할 때 씁니다.
- **ISP(인터페이스 분리 원칙)**: 클라이언트가 쓰지 않는 메서드에 의존하지 않도록 인터페이스를 작게 나누라는 원칙입니다.

[ADR-0032](0032-outbound-port-adapter-naming.md)로 쓰기 포트는 컨텍스트마다 `{Ctx}PersistencePort` 하나였습니다(106개). 한 포트에 조회와 변경이 섞여 있어, 서비스가 쓰지 않는 메서드까지 의존했고 가드가 "이 서비스는 읽기만 한다"를 표현할 수 없었습니다.

## 이전 규칙

### backend/CLAUDE.md — 아웃바운드 포트·어댑터 네이밍 규칙

- 대칭 규칙은 `XxxPersistencePort` ↔ `XxxPersistenceAdapter`였습니다. 예: `BannerPersistenceAdapter implements BannerPersistencePort`(조회+변경 한 인터페이스).
- 서비스 필드는 `bannerPersistencePort` 하나였습니다.
- 데코레이터는 `CachingProhibitedWordPersistencePort implements ProhibitedWordPersistencePort`였습니다.
- 테스트 더블은 `FakeXxxPersistencePort`였습니다.
- 가드 `writePortsShouldBeNamedPort`가 `port.out.write` 인터페이스 이름을 검사했고, api 3모듈의 `controllersShouldNotDependOnPersistencePorts`는 이름 조건이 `PersistencePort`였습니다.
- 예외 포트 `ShopDeliveryTipRegionLookupPort`가 있었습니다.

- 기술 공존 어댑터 `BannerJpaPersistenceAdapter`·`BannerMyBatisPersistenceAdapter`는 둘 다 `BannerPersistencePort`를 구현하고 `@ConditionalOnProperty`로 하나만 등록됐습니다([ADR-0045](0045-banner-mybatis-provider-pilot.md)).

### backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md — Common Patterns

write 포트 예시는 분리 전 단일 포트 하나였습니다. 분리 후 예시는 `application/banner/port/out/write/BannerLoadPort.java`(03a로 domain 패키지에서 이동, 이후 Load/Save 분리)로 바뀌었습니다.

### backend/application-core/AGENTS.md — anchor 하한

`RuleAnchorTest#writePortsExist`의 하한은 107이었습니다.

## 결정

쓰기 포트를 조회 전용 `{Ctx}LoadPort`와 변경 전용 `{Ctx}SavePort`(삭제 포함)로 나눕니다. 어댑터는 이름을 그대로 두고 두 포트를 함께 구현하며, 서비스는 실제로 호출하는 포트만 주입합니다.

| 항목 | before | after |
|---|---|---|
| 쓰기 포트 | `BannerPersistencePort`(조회+변경) | `BannerLoadPort`(상태를 바꾸지 않는 조회) + `BannerSavePort`(상태를 바꾸는 모든 것) |
| 개수 | 106개 + `ShopDeliveryTipRegionLookupPort` = 107 | 192개(Load·Save 85쌍 = 170 + Save만 18 + Load만 4) |
| 예외 포트 | `ShopDeliveryTipRegionLookupPort` | `ShopDeliveryTipRegionLoadPort`(구현 어댑터는 `ShopDeliveryTipPersistenceAdapter` — 대칭 규칙의 예외) |
| 기술 공존 어댑터 | `BannerJpaPersistenceAdapter`·`BannerMyBatisPersistenceAdapter` implements `BannerPersistencePort` | 이름 불변 — 둘 다 `implements BannerLoadPort, BannerSavePort`(당시 선택은 `@ConditionalOnProperty`, 이후 [ADR-0052](0052-banner-write-primary.md)로 `@Primary`) |
| 영속 어댑터 | `BannerPersistenceAdapter implements BannerPersistencePort` | 이름 불변 — `implements BannerLoadPort, BannerSavePort` |
| 서비스 필드 | `bannerPersistencePort` | 호출하는 쪽만: `bannerLoadPort`·`bannerSavePort` |
| 데코레이터 | `CachingProhibitedWordPersistencePort` | `CachingProhibitedWordLoadPort` |
| 테스트 더블 | `FakeXxxPersistencePort` | 두 포트면 `FakeXxxPersistence`, 한 포트면 `FakeXxxLoadPort`/`FakeXxxSavePort` |
| 가드 | `writePortsShouldBeNamedPort` | `writePortsShouldBeLoadOrSavePort` + 신설 `loadPortsShouldOnlyQuery`(`LOAD_PREFIXES`)·`savePortsShouldOnlyMutate`(`SAVE_PREFIXES`) |
| anchor | `writePortsExist` ≥107 | ≥192 |

## 근거

- 원문의 분리 이유: "한 포트에 조회와 변경이 섞이면 서비스가 쓰지 않는 메서드까지 의존하고, 가드가 '이 서비스는 읽기만 한다'를 표현할 수 없습니다."
- 포트는 목적(조회·변경)으로, 어댑터는 역할로 이름을 짓는다는 헥사고날 원칙에 맞춥니다. 개수 규칙이 아니라 이름 짝 규칙이라 어댑터 1개가 포트 N개를 구현합니다.

## 결과·감수한 손실

- 동작·빈 이름·HTTP·DB 변경은 없습니다. 어댑터 이름이 그대로라 어댑터 빈 이름도 그대로입니다.
- 생성자를 바꾼 클래스가 많았습니다(작업 스펙 기준 약 230개 클래스, 테스트 더블 개명 포함).
- 이 결정 이전 시점의 이력 서술에 나오는 `XxxPersistencePort`는 분리 전 이름입니다. 현재 코드를 찾을 때는 `XxxLoadPort`/`XxxSavePort`로 바꿔 검색합니다.
- 컨트롤러 가드 `controllersShouldNotDependOnPersistencePorts`(api 3모듈)는 규칙 이름을 유지하고, 이름 조건만 `LoadPort`·`SavePort`로 바꿨습니다. `port.out.write` 패키지 조건과 패키지 기반 규칙(`queryServicesShouldNotDependOnWritePorts` 등)은 그대로입니다.
- 과거 이관 기록의 이름은 다음 기준으로 처리했습니다. "03a 시점의 `XxxPersistencePort`"는 당시 이름 `XxxRepository`를 가리킵니다. 이미 사라진 클래스(`FollowRepositoryImpl`·`EmailVerificationRepositoryImpl` 등)는 새 이름으로 바꾸면 존재한 적 없는 이름이 되므로 옛 이름을 유지합니다. 다른 문서가 링크하는 일부 제목도 앵커를 보존하려고 옛 표현을 유지합니다.
- banner의 기술 공존 어댑터는 이후 [ADR-0053](0053-notice-mybatis-legacy.md)에서 MyBatis 구현이 삭제되고 JPA 쪽이 `BannerPersistenceAdapter`로 이름을 되돌렸습니다. 지금 공존 어댑터는 notice의 `NoticeJpaPersistenceAdapter`·`NoticeMyBatisPersistenceAdapter`(둘 다 `implements NoticeLoadPort, NoticeSavePort`, JPA 쪽만 `@Primary`)입니다.

## 영향받은 문서

- `backend/CLAUDE.md` · 아웃바운드 포트·어댑터 네이밍 규칙 (`LoadPort`·`SavePort` ↔ `PersistenceAdapter`, `QueryPort` ↔ `QueryAdapter`) · 대칭 규칙·before/after 표·가드
- `backend/infrastructure/jpa/AGENTS.md` · 규칙 / 패키지 구조 · 어댑터가 Load·Save 포트를 함께 구현
- `backend/application-core/AGENTS.md` · anchor 하한 서술 · `writePortsExist` ≥192
- `backend/{web,admin,ceo}-api/AGENTS.md` · "Testing Requirements" · `controllersShouldNotDependOnPersistencePorts` 이름 조건
- `backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md` · Common Patterns · write 포트 예시

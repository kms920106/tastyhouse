# ADR-0032: 쓰기 포트·구현 이름을 PersistencePort·PersistenceAdapter·QueryAdapter로 통일한다

- 상태: 채택(`XxxPersistencePort`는 [ADR-0046](0046-write-port-load-save-split.md)에서 `XxxLoadPort`·`XxxSavePort`로 세분)
- 결정 커밋: `64b985bba` (2026-09-30)
- 대체한 결정: —

## 맥락

**아웃바운드 포트**는 application이 바깥(DB 등)에 요구하는 인터페이스이고, **어댑터**는 그 포트를 구현하는 infrastructure 클래스입니다. 헥사고날 원칙에서는 포트를 목적으로, 어댑터를 역할로 이름 짓고, 둘이 이름 짝으로 대응하는 것이 좋습니다.

전환 전 이름은 이 대칭을 표현하지 못했습니다.

- 쓰기 포트: `XxxRepository`(106개)
- 쓰기 구현: `XxxRepositoryImpl`(106개)
- 조회 구현: `XxxQueryDao`(48개)

`Impl`과 `Dao`는 이 클래스가 어떤 포트를 구현하는지 말해 주지 않습니다. 또 인바운드 어댑터나 읽기 포트(`XxxQueryPort`)와 어휘가 갈렸습니다. 쓰기 포트 이름 `XxxRepository`는 Spring Data 인터페이스 `XxxJpaRepository`와도 헷갈렸습니다.

## 이전 규칙

### backend/application-core/AGENTS.md — ArchUnit — 5클래스 … / anchor 하한

write 포트는 `port.out.write`의 `*Repository` 인터페이스로 셌다. 덩어리 03a 시점 `RuleAnchorTest#writePortsExist` 하한은 `*Repository` ≥ 106이었다. 구현은 `XxxRepositoryImpl`, 조회 구현은 `XxxQueryDao`라고 불렀다(03b 동안에는 State 계열 `XxxStatePortImpl`도 있었다).

## 결정

쓰기 포트와 영속·조회 구현을 이름 짝 규칙으로 통일합니다.

| 역할 | before | after |
|---|---|---|
| 쓰기 포트(`application/<ctx>/port/out/write/`) | `XxxRepository`(106) | `XxxPersistencePort` |
| 예외 포트 | `ShopDeliveryTipRegionLookup` | `ShopDeliveryTipRegionLookupPort` |
| 영속 어댑터(`infrastructure/<ctx>/persistence/`) | `XxxRepositoryImpl`(106) | `XxxPersistenceAdapter` |
| 조회 어댑터(`infrastructure/<ctx>/query/`) | `XxxQueryDao`(48) | `XxxQueryAdapter` |
| 데코레이터·테스트 더블·포트 타입 필드명 | `CachingProhibitedWordRepository` 등 | 같은 어휘(`…PersistencePort`)로 변경 |
| `RuleAnchorTest#writePortsExist` | `*Repository` ≥ 106 | `*Port` ≥ 107(`ShopDeliveryTipRegionLookupPort` 포함) |
| 범위 밖(유지) | — | Spring Data `XxxJpaRepository`·`@Repository`·security-core 토큰 저장소 |

## 근거

- 포트와 구현이 이름 짝으로 대응해야 어떤 어댑터가 어떤 포트를 구현하는지 이름만 보고 알 수 있습니다. `Impl`·`Dao`는 그 정보를 담지 못했습니다.
- 읽기 쪽 `XxxQueryPort` ↔ `XxxQueryAdapter`와 쓰기 쪽 `XxxPersistencePort` ↔ `XxxPersistenceAdapter`로 어휘가 맞춰집니다.

## 결과·감수한 손실

- 동작 변경은 없습니다. 패키지·DB·HTTP 계약은 그대로이고, 스프링 빈 이름만 클래스명을 따라 바뀌었습니다.
- 이름 기반 ArchUnit 규칙과 `RuleAnchorTest` 하한(107)을 갱신했습니다. 옛 접미어가 되살아나지 않게 막는 가드 2종을 신설했고(그중 `infrastructure/jpa`의 `LayerRulesTest#adaptersShouldNotUseRetiredSuffixes`가 `*RepositoryImpl`·`*QueryDao`를 금지한다), 컨트롤러 가드에 `port.out.write` 패키지 차단을 추가했습니다.
- `backend/CLAUDE.md`에 아웃바운드 포트·어댑터 네이밍 규칙 절을 신설했습니다.
- 이후 [ADR-0046](0046-write-port-load-save-split.md)이 `XxxPersistencePort` 106개를 목적별 `XxxLoadPort`·`XxxSavePort`(192개)로 나눴습니다. 어댑터 이름 `XxxPersistenceAdapter`는 그대로이고, 하한은 107 → 192가 됐습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · "아웃바운드 포트·어댑터 네이밍 규칙 (`LoadPort`·`SavePort` ↔ `PersistenceAdapter`, `QueryPort` ↔ `QueryAdapter`)" · 포트는 목적, 어댑터는 역할로 이름 짓고 이름 짝으로 대응하며, 전환 전 이름은 대칭을 표현하지 못했다
- `backend/application-core/AGENTS.md` · "ArchUnit — 아키텍처 테스트 클래스" 표의 `RuleAnchorTest` 행 / "anchor 하한" · write 포트 하한은 `port.out.write`의 `*LoadPort`·`*SavePort` 인터페이스 기준
- `backend/infrastructure/jpa/AGENTS.md` · "패키지 구조" · 영속 어댑터 `XxxPersistenceAdapter`, 조회 어댑터 `XxxQueryAdapter`

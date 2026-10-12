# ADR-0039: 조회 DAO에서 QueryDSL Tuple을 전면 제거하고 public Row record로 투영한다

- 상태: 채택
- 결정 커밋: `a373e0a4d` (2026-10-04)
- 대체한 결정: —

## 맥락

- **QueryDSL `Tuple`**: 여러 컬럼을 select할 때 결과 한 행을 담는 타입 없는 컨테이너입니다. 값은 `tuple.get(path)`(표현식으로 찾기)나 `row.get(0, X.class)`(위치로 찾기)로 꺼냅니다.
- **Row record 투영**: `Projections.constructor(XxxRow.class, ...)`로 select 결과를 이름과 타입이 있는 record 생성자에 바로 넣는 방식입니다.

`Tuple`은 타입이 없는 행이라 `row.get(0, X.class)` 위치 접근이 순서가 어긋나도 컴파일됩니다. `toSummary(Tuple)`·`Map<Long, Tuple>`처럼 메서드 경계를 넘으면 무엇이 담겼는지 시그니처로 알 수 없었습니다(커밋 메시지).

## 이전 규칙

### backend/infrastructure/jpa/AGENTS.md(당시 infrastructure/persistence/AGENTS.md) — 봉인·가드 목록

- 1차 결정(`b67401c26`, 2026-09-23)은 위치 인덱스 Tuple만 record로 바꾸고, 절 제목을 "남긴 `Tuple`은 key→value 룩업 빌더다 — record로 바꾸지 않는다"로 두었습니다.
- 2컬럼 key→value 수집을 Tuple의 정당한 용법으로 보고 `ShopReviewManagementQueryAdapter`·`ReviewQueryAdapter`·`ProductFeedbackQueryAdapter`·`ReviewStatisticsQueryAdapter`·`ShopChoiceQueryAdapter`·`ShopDeliveryTipQueryAdapter`·`ShopSearchQueryAdapter`·`ProductQueryAdapter`의 Tuple 39건을 의도적으로 남겼습니다.

### 같은 문서 — 코드 주석 이관 근거

- `ProductQueryAdapter` 절 제목은 "`select`와 `Tuple.get`은 같은 표현식 인스턴스를 참조해야 한다"였습니다. `Tuple.get(expr)`은 `equals`로 select 목록에서 값을 찾으므로, 새로 만든 동등 표현식을 넘기면 값을 못 찾았습니다.
- `FileUrlProjection`은 `MappingProjection`을 상속해 `map(Tuple)` 하나만 구현했습니다.

## 결정

조회 DAO의 다중 컬럼 select를 전부 Row record 투영으로 바꾸고, Tuple을 ArchUnit으로 금지합니다.

| 항목 | before | after |
|---|---|---|
| 다중 컬럼 select 결과 | `List<Tuple>` + `tuple.get(path)` / `row.get(0, X.class)` | `Projections.constructor(XxxRow.class, ...)` → `List<XxxRow>` + `row.xxx()` |
| 메서드 경계 | `toSummary(Tuple row)`, `Map<Long, Tuple>` 등 타입 없는 행이 오감 | Row 타입이 시그니처에 드러난다 |
| Row record | — | public `XxxRow` 20개, 공용 `IdStringRow`·`IdCountRow`는 `shared/query` |
| `FileUrlProjection` | `MappingProjection#map(Tuple)` | `FactoryExpressionBase`를 직접 상속 |
| 가드 | 없음 | `LayerRulesTest#queryAdaptersShouldNotUseTuple`(Tuple·QTuple·MappingProjection 금지) |
| `ProjectionConstructorMatchingTest` | import한 투영 대상만 해석 | 같은 패키지 투영 대상도 해석해 import 없는 Row의 인자 개수·순서도 검사 |

## 근거

- 위치 접근은 순서가 어긋나도 컴파일되고, 타입 없는 행이 메서드 경계를 넘으면 시그니처가 아무것도 말하지 않습니다. 원문은 key→value 수집용 Tuple 39건까지 "사용자 결정으로 전부 Row record로 바꿨다"고 적고 있습니다.
- `MappingProjection`은 내부 `QTuple`로 행을 `Tuple`로 감싸 넘기므로 Tuple 금지 가드와 양립할 수 없어 상위 타입을 바꿨습니다. `equals`는 둘 다 `FactoryExpressionBase#equals`라 의미가 같습니다.

## 결과·감수한 손실

- 동작은 바뀌지 않았습니다(변경 전후 jar 응답 63건 동일, HTTP·SQL·DB 동일).
- 값이 생성자 위치로 들어가므로 "select와 `Tuple.get`은 같은 표현식 인스턴스를 참조해야 한다"는 제약이 사라졌습니다.
- `Projections.constructor`는 컴파일 타임 검증이 없어, 인자 개수·순서는 `ProjectionConstructorMatchingTest`가 유일한 방어선입니다.

## 영향받은 문서

- `backend/infrastructure/jpa/AGENTS.md` · `Tuple`을 쓰지 않는다 — 다중 컬럼 select는 public `XxxRow` record로 받는다 (`LayerRulesTest#queryAdaptersShouldNotUseTuple`) · Row record 투영 규칙
- `backend/infrastructure/jpa/AGENTS.md` · 봉인·가드 목록 > `ProjectionConstructorMatchingTest`는 같은 패키지 투영 대상도 해석한다 — `resolve`의 package 폴백
- `backend/infrastructure/jpa/AGENTS.md` · "코드 주석에서 이관된 설계 근거" > "`FileUrlProjection` · `FileUrlResolver#urlOf` — 경로→URL 변환을 투영식 안으로" · `FactoryExpressionBase` 직접 상속
- `backend/CLAUDE.md` · infrastructure-module 계층 방향 규칙 (ArchUnit — `LayerRulesTest`) · `queryAdaptersShouldNotUseTuple`
- `backend/CLAUDE.md` · 파일 URL 조립 위치 규칙 (query DAO가 `FileUrlResolver`로 완성) · `FileUrlProjection` 서술

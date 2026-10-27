# ADR-0056: 신규 에러코드는 스냅샷 tsv에 행을 추가하고 개수 단정을 올린다

- 상태: 채택
- 결정 커밋: `03d806112` (2026-10-15)
- 대체한 결정: —

## 맥락

- **에러 카탈로그 스냅샷**: `backend/application-core/src/test/resources/error-catalog-before.tsv`는 HTTP 응답으로 나가는 에러의 `(이름, status, code, message)`를 한 줄씩 적은 파일입니다. `ErrorCatalogSnapshotTest`가 7개 카탈로그 enum의 실제 값이 이 파일과 정확히 같은지 비교합니다.
- **wire 계약**: 프론트가 응답의 `code`·status로 분기하므로, 이미 나가고 있는 값은 바꾸면 안 되는 계약입니다.

[ADR-0042](0042-error-code-module-split.md)는 에러코드를 모듈별 카탈로그로 나누면서, 분할이 wire 계약을 바꾸지 않았다는 증거로 이 스냅샷을 만들었습니다. 그 뒤로 "스냅샷 파일을 갱신해 테스트를 맞추지 않는다"가 규칙이었는데, 이 규칙대로라면 새 에러코드를 추가하는 것 자체가 테스트 실패로 막힙니다. 분할 이후 처음 추가된 신규 코드(`WebErrorCode.PAYMENT_REFUND_ORDER_STATUS_INVALID`, [ADR-0055](0055-payment-refund-pessimistic-lock.md))에서 이 공백이 드러났습니다.

## 이전 규칙

`backend/application-core/AGENTS.md`의 "에러 카탈로그 가드 — 코드는 던지는 가장 안쪽 모듈에 두고, 봉인 집합을 늘리지 않는다 (에러코드 모듈 분할)" 절, `ErrorCatalogSnapshotTest` 항목: 스냅샷 파일을 갱신해 테스트를 맞추지 않는다. 코드·상태·메시지를 바꾸면 이 테스트가 실패해야 정상이며, 의도한 wire 변경이면 그 사실을 문서에 먼저 적는다. 신규 코드에 대한 예외는 없었습니다.

## 결정

| 항목 | before | after |
|---|---|---|
| 기존 행의 code·status·message 변경 | 금지 | 금지(변경 없음) |
| 신규 상수 추가 | 규칙 없음(테스트 실패로 사실상 불가) | 허용 — 아래 4단계 |
| 개수 단정 `hasSize` | 419 고정 | 신규 코드 수만큼 올림 |

신규 상수 추가 절차:

1. 소유 카탈로그 enum에 상수를 추가한다(배치는 `backend/CLAUDE.md`의 예외·에러코드 소유 규칙).
2. tsv에 `NAME⇥status⇥CODE⇥message` 한 행을 추가한다(⇥ = 탭).
3. `ErrorCatalogSnapshotTest`의 `hasSize`를 1 올린다.
4. `backend/CLAUDE.md`와 `backend/application-core/AGENTS.md`의 카탈로그 개수 표기를 함께 갱신한다.

## 근거

- 신규 코드는 기존 클라이언트가 분기하던 값을 하나도 바꾸지 않으므로(additive) 스냅샷이 지키려던 wire 계약을 깨지 않습니다.
- 스냅샷을 신규 코드에도 동결하면 카탈로그를 넓히는 정상 작업이 막히고, 우회하려고 기존 코드를 재사용해 의미가 다른 에러가 같은 `code`로 나가게 됩니다.
- 기존 행 변경은 여전히 테스트 실패로 드러나므로, 이 테스트의 원래 목적(조용한 계약 변경 차단)은 그대로 남습니다.

## 결과·감수한 손실

- tsv 파일명 `error-catalog-before.tsv`의 "before"(분할 전)는 더 이상 내용과 정확히 맞지 않습니다. 테스트가 이 경로를 참조하므로 파일명은 바꾸지 않았습니다.
- 개수 표기가 문서 두 곳과 테스트 한 곳에 흩어져 있어, 신규 코드를 추가할 때마다 함께 고쳐야 합니다. 병행 작업이 각자 코드를 추가하면 개수가 충돌할 수 있으므로 커밋 직전에 tsv 행 수와 `hasSize`를 다시 확인합니다.

## 영향받은 문서

- `backend/application-core/AGENTS.md` · "에러 카탈로그 가드 — 코드는 던지는 가장 안쪽 모듈에 두고, 봉인 집합을 늘리지 않는다 (에러코드 모듈 분할)" · `ErrorCatalogSnapshotTest` 항목의 신규 코드 추가 4단계 절차
- `backend/CLAUDE.md` · "현재 규칙 — 에러코드·예외는 소유 모듈별로 나뉜다" · 가드 항목의 "신규 에러코드는 tsv에 행을 추가하고 `hasSize`를 함께 올린다" 안내

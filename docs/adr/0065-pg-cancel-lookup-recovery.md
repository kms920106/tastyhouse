# ADR-0065: PG 취소의 "이미 취소됨"·결과 불명은 조회로 확정하고, 취소 실패 원인을 분류한다

- 상태: 채택
- 결정 커밋: `93aa93994` (2026-10-27)
- 대체한 결정: ADR-0063(`PG_API_ERROR`를 취소 전용으로 남기던 부분)

## 맥락

결제 취소는 세 단계다 — 취소 가능 판정, 토스 취소 호출, 결제 행을 잠그고 재판정한 뒤 반영. 두 가지 문제가 있었다.

1. **토스는 취소했는데 DB가 모르는 상태가 자동으로 맞춰지지 않았다.** web은 취소 응답을 20초까지 기다린다. 서버가 토스 취소를 끝냈는데 응답이 늦으면 사용자는 다시 누르고, 서버는 토스에 취소를 한 번 더 보내 "이미 취소된 결제"로 거절당해 `CANCEL_FAILED`가 됐다. 토스 취소 호출이 타임아웃·5xx로 끝났는데 토스가 실제로는 취소한 경우, DB는 `COMPLETED`인데 토스는 이미 취소된 데이터 불일치도 같은 경로로 `CANCEL_FAILED`로 끝났다.
2. **취소 실패 원인이 하나로 합쳐졌다.** `TossPaymentClient#cancelPayment`는 HTTP 오류 응답을 전부 `PG_API_ERROR`로 덮어써서, 404 `NOT_FOUND_PAYMENT`(DB에는 완료 결제가 있는데 PG에는 없음 — 데이터 불일치 신호)와 400 `ALREADY_CANCELED_PAYMENT`를 구분할 수 없었다.

승인 쪽은 같은 문제를 이미 풀었다 — "이미 처리됨"·결과 불명이면 토스에 한 번 조회해 확정한다([ADR-0060](0060-pg-confirm-unknown-lookup.md), [ADR-0063](0063-pg-confirm-unreadable-4xx-unknown.md)).

## 이전 규칙

- 취소 HTTP 오류는 `PG_API_ERROR` 하나, 그 밖의 예외는 `SYSTEM_ERROR`였다. `PgCancelResult`는 `success`·`errorCode`·`errorMessage`만 실었다.
- `PaymentCancelService`는 토스 취소가 실패하면 원인과 관계없이 `CANCEL_FAILED`를 돌려줬다.
- 토스 취소 성공 후 재판정이 `SUCCESS`가 아니면 원인과 관계없이 `PG_DB_MISMATCH`를 남겼다.

## 결정

| 항목 | before | after |
|---|---|---|
| 취소 HTTP 오류 변환 | 전부 `PG_API_ERROR` | 승인·조회와 같은 `translateResponseError` — 4xx 토스 code 보존, 해석 불가 4xx `PG_UNREADABLE_RESPONSE`, 5xx `PG_SERVER_ERROR`. `PG_API_ERROR` 상수 삭제 |
| 취소 실패 계약 | `errorCode` 문자열뿐 | `PgCancelFailure` 4종(`ALREADY_CANCELED`·`NOT_FOUND`·`REJECTED`·`UNKNOWN`)을 어댑터가 분류해 `PgCancelResult.failure`로 싣는다 |
| `ALREADY_CANCELED`·`UNKNOWN` | `CANCEL_FAILED` | 새 포트 메서드 `lookupCancellation(pgOrderId, paymentKey)`로 한 번 조회해 `CANCELED`면 기존 반영 경로(`applyInNewTx`)로 취소를 반영하고 `SUCCESS`. 아니면(조회 `NOT_CANCELED`·`UNKNOWN`·예외, 조회 키 `pgOrderId` 없음) `CANCEL_FAILED` + ERROR 로그 마커 `PG_CANCEL_UNKNOWN` |
| `NOT_FOUND` | `CANCEL_FAILED` | `CANCEL_FAILED` + ERROR 로그 마커 `PG_CANCEL_NOT_FOUND` |
| PG 취소 확인 후 재판정 `ALREADY_CANCELLED` | `PG_DB_MISMATCH` | 결제도 `CANCELLED`면 마커 없음, 결제가 `CANCELLED`가 아니면(주문만 취소) `PG_DB_MISMATCH` 유지 |
| 사용자 응답 코드·HTTP 상태·에러코드 | — | 변경 없음 |

## 근거

- **조회는 읽기뿐이라 중복 환불을 만들지 않는다.** 토스에 보내는 취소 요청 횟수는 그대로이고, 반영은 성공·조회 확인 두 경로 모두 같은 `applyInNewTx`(결제 행 잠금 + 주문 상태 재판정)를 지난다.
- **조회 결과가 우리 결제의 것인지 확인한다.** 응답의 `orderId`·`paymentKey`가 요청과 같고 `status = CANCELED`일 때만 반영 근거로 쓴다. `PARTIAL_CANCELED`는 전액 취소가 아니므로 확정하지 않는다.
- **조회로도 확정하지 못하면 `PG_CANCEL_UNKNOWN` 마커를 남긴다.** 이 상태는 PG가 이미 환불했는데 DB는 `COMPLETED`일 수 있다. 사용자가 다시 취소하면 "이미 취소됨" → 조회로 맞춰지지만, 그사이 가게가 조리를 시작하면 재취소가 `ALREADY_PREPARING`으로 막히고 취소 대사 배치도 없다. 이 저장소는 알림을 로그 레벨이 아니라 마커 문자열로 걸므로, 마커 없는 ERROR로는 운영자가 이 건을 찾을 수 없다. 조회로 정상 복구된 경우에는 ERROR를 남기지 않으므로 거짓 경보는 늘지 않는다.
- **`NOT_FOUND`는 조회하지 않는다.** 실측에서 취소가 404인 테스트 결제도 조회는 `DONE`이었다. 조회로는 확정할 수 없고, 운영자가 찾을 표식만 필요하다.
- **기존 `lookupPayment`를 재사용하지 않았다.** 그 메서드는 `CANCELED`를 `ABORTED`·`EXPIRED`·404와 함께 `NOT_APPROVED`로 묶는다. 구분하려면 `PgPaymentLookupStatus`에 값을 추가해야 하고, 그러면 승인·batch 대사의 `switch`가 함께 바뀐다. 토스 HTTP 호출(`lookupPaymentByOrderId`)은 그대로 재사용한다.
- **`ALREADY_CANCELLED`에서 결제 상태를 따로 읽는다.** 재판정은 주문 상태로만 `ALREADY_CANCELLED`를 판정한다. 관리자 주문 상태 변경 API는 결제 취소 없이 주문만 `CANCELLED`로 바꿀 수 있으므로, 주문 취소만 보고 마커를 빼면 "토스 환불 완료·결제 `COMPLETED`·포인트 미복원" 불일치를 놓친다. 반대로 재클릭한 다른 요청이 먼저 반영한 경우(결제도 `CANCELLED`)에는 불일치가 아니므로 거짓 경보를 없앤다.

### 고려한 대안

- **취소 요청에 `Idempotency-Key`를 붙인다**: 토스는 실패 응답도 키별로 캐시해 재생한다는 실측이 있다(`backend/infrastructure/tosspayments/AGENTS.md`의 "주문번호로 결제 조회" 절). 첫 시도가 일시 실패하면 같은 키의 재시도가 유효기간 동안 같은 실패를 재생한다. 또 이미 생긴 불일치는 키로 고칠 수 없다. 기각.
- **`PgPaymentLookupStatus`에 `CANCELED` 추가**: 위 근거대로 기각.
- **결과 불명은 조회 없이 둔다(이미 취소됨만 조회)**: 타임아웃 뒤 토스가 실제로 취소한 경우를 사용자가 다시 누를 때까지 맞추지 못한다. 승인 쪽 결정과도 대칭이 깨진다. 기각.

## 결과·감수한 손실

- 결정 시점에는 토스 취소 API가 재취소에 `ALREADY_CANCELED_PAYMENT`를 돌려주는지 문서(전체 에러 목록에만 400으로 있음)로 확인하지 못했다. 구현 직후 테스트 환경에서 전액 취소된 결제에 취소를 한 번 더 보내 400 `ALREADY_CANCELED_PAYMENT`임을 확인했다(2026-10-25). `NOT_CANCELABLE_PAYMENT` 등 그 밖의 취소 거절 코드의 발생 조건은 여전히 미확정이며 `REJECTED`로 분류돼 이전과 같이 `CANCEL_FAILED`다. 돈이 어긋나지는 않는다. 카드 결제의 잔액 초과·면세 금액·부분 취소 조건으로는 `NOT_CANCELABLE_AMOUNT`(403)·`INVALID_REQUEST`(400)만 나왔고 위 세 코드는 만들어지지 않았다(2026-10-27).
- 부분 취소 뒤 남은 금액을 전액 취소한 결제는 잔액이 0원인데도 토스 상태가 `CANCELED`가 아니라 `PARTIAL_CANCELED`로 남는다(2026-10-27 실측, 취소 2건 모두 `DONE`). 이 결정은 `PARTIAL_CANCELED`를 전액 취소가 아닌 것으로 보고 확정하지 않으므로, 그런 결제는 응답이 유실돼도 조회로 복구되지 않고 `CANCEL_FAILED` + `PG_CANCEL_UNKNOWN`으로 남는다. 우리 앱은 부분 취소를 하지 않아 운영자가 토스 어드민에서 먼저 일부 환불한 결제에서만 생긴다. 후속 과제로 `balanceAmount`가 0인 `PARTIAL_CANCELED`를 `CANCELED`로 보는 변경이 있다.
- 결과 불명(최악 15초) 뒤 조회(최악 15초)가 붙으면 서버 처리가 최대 약 30초다. web의 결과 확인 창(약 35초) 안이지만, 넘기면 사용자는 주문 내역을 다시 열어야 결과를 본다.
- 결과 불명 직후 조회하면 토스가 아직 처리 중이라 `DONE`이 올 수 있다. 이때는 이전과 같이 `CANCEL_FAILED`(+ `PG_CANCEL_UNKNOWN`)이고, 사용자가 다시 취소하면 "이미 취소됨" → 조회로 맞춰진다. 그사이 조리가 시작돼 재취소가 막히면 운영자가 마커로 찾아 맞춘다. 토스가 실제로는 취소하지 않은 건에도 마커가 남으므로, 운영자는 토스 쪽 상태를 확인한 뒤 판단한다.
- 이미 생긴 불일치(DB `COMPLETED`·토스 `CANCELED`)는 누군가 취소를 다시 눌러야 맞춰진다. 취소 대사 배치는 없다.

## 영향받은 문서

- `backend/infrastructure/tosspayments/AGENTS.md` · 무엇을 소유하는가 — wire DTO 반환 타입 목록에 `PgCancelLookupResult` 추가
- `backend/infrastructure/tosspayments/AGENTS.md` · Dependencies — 계약 타입 목록에 신규 3종 추가, 테스트 건수 갱신
- `backend/infrastructure/tosspayments/AGENTS.md` · 주의 — 오래된 테스트 결제는 조회는 되지만 취소가 404일 수 있다는 실측 bullet 신설
- `backend/infrastructure/tosspayments/AGENTS.md` · 승인 4xx 응답은 토스 본문의 code·message를 보존한다 — "`PG_API_ERROR`는 취소 전용" 항목 삭제, 취소도 같은 `translateResponseError`를 쓴다로 정정
- `backend/infrastructure/tosspayments/AGENTS.md` · 취소 실패 분류와 취소 확인 조회(신설)
- `backend/application-core/AGENTS.md` · 앱 모듈 재분리 — 앱 소속은 Gradle 모듈이 표현한다 — "PG SPI는 앱 전용이 아니다" 문단의 계약 타입 목록에 신규 3종 추가
- `backend/application-core/AGENTS.md` · 덩어리 02/03a — 벤더 포트·서비스 4종을 `domain`에서 이관 — payment 행의 계약 타입 목록에 신규 3종 추가
- `backend/application-core/AGENTS.md` · PG 승인·실패 반영은 결제 행을 잠그고… — 취소 복구 항목, `PG_CANCEL_NOT_FOUND`·`PG_CANCEL_UNKNOWN` 마커
- `backend/application-core/AGENTS.md` · PG·외부 왕복은 트랜잭션 밖에 둔다 — 3단 구조와 보상 불가 지점 — 취소 흐름 블록(트랜잭션 밖 취소 확인 조회, 결제 상태 읽기 트랜잭션) 추가, 보상 장치 항목의 ERROR 마커 목록 갱신
- `backend/web-application/AGENTS.md` · 의존 — 코어 PG 계약 타입 목록에 신규 3종 추가, 취소 마커 상수(`PG_CANCEL_NOT_FOUND`·`PG_CANCEL_UNKNOWN`) 참조 위치
- `backend/infrastructure/pg/AGENTS.md` · 라우터 등록은 어디인가 — 코어 소유 계약 타입 목록에 신규 3종 추가
- `backend/infrastructure/pg/AGENTS.md` · 포트 2단 구조 — 벤더는 공존하고 앱은 하나만 주입한다 — `PgProviderGatewayPort` 메서드 목록에 `lookupCancellation` 추가
- `backend/CLAUDE.md` · 앱 모듈 경계 규칙 → 무엇이 어디에 사는가 — 코어 PG SPI 목록에 `PgCancelFailure`·`PgCancelLookupResult`·`PgCancelLookupStatus` 추가
- `docs/domain/payment.md` · 결제 취소 — 2단계(확정 실패 시 운영자용 기록 포함), 웹 재확인 단락, 비즈니스 규칙 표

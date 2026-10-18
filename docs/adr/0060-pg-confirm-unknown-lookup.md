# ADR-0060: PG 승인 결과 불명은 실패가 아니라 대기로 두고, 즉시 조회로 대사한다

- 상태: 채택(부분 → 0063)
- 결정 커밋: 커밋 후 기입
- 대체한 결정: ADR-0059(응답 없음·통신 오류를 FAILED로 두던 부분, "이미 처리됨"을 실패 응답으로 돌려주던 부분)

## 맥락

[ADR-0059](0059-payment-confirm-race-lock.md)는 같은 결제의 승인·실패 반영을 결제 행 잠금으로 직렬화하고 "이미 처리됨" 거절은 상태를 바꾸지 않게 했습니다. 그러나 두 가지를 후속 과제로 남겼습니다.

- **응답 없음·통신 오류는 FAILED였습니다.** `TossPaymentClient`가 5xx를 `PG_API_ERROR`, 예외를 `SYSTEM_ERROR`, 빈 본문을 `UNKNOWN_ERROR`로 바꾸고 `TossPaymentGatewayAdapter`가 이를 `PgConfirmFailure.REJECTED`로 분류해 `failPgConfirmation`이 `payment.fail()`을 호출했습니다. 토스가 실제로 승인했다면 돈은 나갔는데 결제는 FAILED이고, 사전 확인이 PENDING만 통과시켜 재시도로도 복구되지 않았습니다.
- **"이미 처리됨"의 두 번째 요청은 400을 받았습니다.** 결제는 첫 요청 결과대로 완료인데 사용자는 실패 화면을 봤습니다.

여기에 연관 결함이 하나 더 있었습니다. 결과 확정 전의 PENDING 결제를 사용자가 취소하면 결제가 CANCELLED가 되어, 나중에 승인이 확인돼도 대사 대상에서 빠지고 돈은 환불되지 않은 채 남습니다.

## 이전 규칙

- `PgConfirmFailure`는 `ALREADY_PROCESSED`·`REJECTED` 둘. `REJECTED`는 "`ALREADY_PROCESSED`가 아닌 모든 실패"(통신 오류 포함).
- 결과 불명·"이미 처리됨"이면 토스에 결제 상태를 묻지 않음.
- 결제 취소는 주문 상태로만 판정하고 PENDING 결제도 취소.

## 결정

| 항목 | before | after |
|---|---|---|
| 결과 불명 분류 | `REJECTED` | `PgConfirmFailure.UNKNOWN` — 5xx(`PG_SERVER_ERROR`, `PG_API_ERROR`에서 분리)·예외(`SYSTEM_ERROR`)·빈 응답(`UNKNOWN_ERROR`) |
| 승인 전 기록 | 없음 | `Payment#recordPgAttempt(PgProvider)` — PENDING일 때만 `pg_provider`를 채움. "PENDING + `pg_provider` 있음" = 돈이 나갔을 수 있는 결제. 서비스(`PaymentConfirmationService#recordPgAttempt`)는 결제를 잠금 조회한 뒤 PENDING이 아니면 `PAYMENT_NOT_PENDING_APPROVAL`로 **PG 호출 전에 중단**한다. 미지원 PG는 기록 전에 `PG_PROVIDER_UNSUPPORTED`로 거절한다 |
| `UNKNOWN`·`ALREADY_PROCESSED` 이후 | 실패 반영(각각 FAILED / 상태 불변) → 400 | 토스 "주문번호로 결제 조회" 1회 → `APPROVED`면 대사 반영, `NOT_APPROVED`면 FAILED → 400(원래 결과가 `UNKNOWN`이면 기본 메시지, `ALREADY_PROCESSED`면 토스 원문), `UNKNOWN`이면 PENDING 유지 → **503 `PAYMENT_APPROVAL_UNCONFIRMED`** |
| 토스 조회 상태 매핑 | 없음 | `DONE` → APPROVED, `ABORTED`·`EXPIRED`·`CANCELED`·404 `NOT_FOUND_PAYMENT` → NOT_APPROVED, `READY`·`IN_PROGRESS`·`PARTIAL_CANCELED`·그 밖의 오류 → UNKNOWN |
| 조회 후 실패 반영에 넘기는 결과 | — | 원래 승인 결과가 아니라 **조회 결과**만 |
| 승인 반영 | COMPLETED면 거절 | COMPLETED이고 `pgTid == paymentKey`면 그 결제를 반환(멱등) — 원장 기록보다 먼저 판정 |
| 대사 반영 결과 | 없음 | `PgReconcileOutcome` 4종: `COMPLETED`·`ALREADY_COMPLETED`(200), `AMOUNT_MISMATCH`(상태 불변·원장 미기록 — 금액 검사가 원장 기록보다 먼저)·`ORDER_NOT_CONFIRMABLE`(결제만 완료, 주문 확정·이벤트 생략)(503 + `PG_DB_MISMATCH` ERROR) |
| 미확정 결제 취소 | 취소 | 취소 반영(`PaymentCancellationService#applyCancellation`)이 트랜잭션 첫 조회로 결제 행을 잠그고(`findByIdForUpdate`), PENDING + `pg_provider` 있음이면 **409 `PAYMENT_CANCEL_UNCONFIRMED`**. 승인 시도 기록도 같은 행을 잠가 두 쓰기가 직렬화된다. 잠그지 않는 사전 판정이 PENDING(PG 취소 불필요)으로 본 결제가 반영 시점에 COMPLETED면 같은 409로 거절한다(환불 없는 취소 방지) |

## 근거

| 대안 | 채택하지 않은 이유 |
|---|---|
| `pgTid`에 시도 표시를 남김 | `pgTid`는 PG 취소에 쓰는 값이라 오염되면 취소가 엉뚱한 거래로 갑니다. 기존 `pg_provider` 컬럼은 PENDING 동안 비어 있어 DDL 없이 쓸 수 있습니다 |
| 새 결제 상태(APPROVING·UNKNOWN) | 상태값·DDL·전이 표·모든 상태 분기를 함께 바꿔야 합니다. "PENDING + `pg_provider`"만으로 같은 판별이 됩니다 |
| 결과 불명이면 조회 없이 503 | 대부분의 결과 불명은 조회 한 번으로 확정됩니다. 조회 없이 미루면 모든 건이 수동·batch 대사로 넘어갑니다 |
| 조회 후에도 원래 결과를 실패 반영에 넘김 | 버튼 연타의 정상 경합(`ALREADY_PROCESSED` → 조회 `IN_PROGRESS`)에서 `PG_DB_MISMATCH` 경고가 나 알럿 노이즈가 됩니다 |
| `ORDER_NOT_CONFIRMABLE`에서 결제를 PENDING으로 둠 | 재시도·batch가 같은 실패를 끝없이 반복합니다. 돈이 나간 사실을 결제에 반영하고, 환불은 `PG_DB_MISMATCH` ERROR로 운영자에게 넘깁니다 |
| 미확정 결제 취소를 200 `PaymentCancelCode`로 거절 | 기존 코드는 확정된 거절 결과입니다. 이번 차단은 일시적 상태라 409 예외로 구분합니다 |
| 취소 반영을 잠그지 않고 `pg_provider`만 판정 | 취소의 일반 조회와 시도 기록이 겹치면 취소가 옛 값(`pg_provider` 없음)을 보고 결제를 CANCELLED로 만든 뒤 PG 승인이 나갈 수 있습니다. 시도 기록도 도메인 no-op만으로는 "취소된 결제에 PG 호출"을 막지 못합니다. 그래서 취소는 잠금 조회, 시도 기록은 PENDING 아니면 예외로 PG 호출 전에 끊습니다 |
| 토스 `READY`를 미승인으로 봄 | 승인 요청 직후 토스 상태가 아직 `READY`일 수 있어, 돈이 나간 결제를 FAILED로 만들 수 있습니다. 끝내 승인되지 않으면 토스가 `EXPIRED`로 확정하므로 그때 미승인으로 정리합니다. 승인 가능 시한은 인증 후 약 10분이고 그 뒤 승인은 거부되지만, 조회 상태가 `EXPIRED`로 바뀌는 것은 테스트 환경 실측으로 결제 생성 후 약 33분입니다(라이브 미확인) |
| 토스 `CANCELED`를 UNKNOWN으로 둠 | 전액 취소는 돈이 돌아간 상태라 FAILED로 정리해도 돈이 어긋나지 않습니다. UNKNOWN으로 두면 7일 동안 취소 차단이 이어집니다 |

## 결과·감수한 손실

- **신규 에러코드 2개**(`WebErrorCode`): `PAYMENT_APPROVAL_UNCONFIRMED`(503), `PAYMENT_CANCEL_UNCONFIRMED`(409). 스냅샷 tsv에 2행 추가([ADR-0056](0056-error-catalog-additive-snapshot.md) 절차).
- **"결제 완료와 주문 확정은 함께" 불변식에 예외가 생겼습니다.** 주문이 취소된 뒤 승인이 확인되면 결제만 완료이고 주문은 취소 상태입니다. 이 건은 환불 대상이며 `PG_DB_MISMATCH` ERROR로 드러납니다.
- **응답이 최대 약 30초 걸릴 수 있습니다**(승인 + 조회가 모두 실패하는 경우). web의 승인 호출 타임아웃을 35초로 늘렸습니다.
- **wire 변화**: 미확정·실패 결제의 `PaymentResponse.pgProvider`가 null 대신 `"TOSS"`입니다. 이 필드를 쓰는 프론트 코드는 없습니다.
- **승인 1건마다 쓰기 트랜잭션이 1회 늘었습니다**(`recordPgAttempt`).
- **토스 조회의 "결제 없음" 응답은 404 `NOT_FOUND_PAYMENT`입니다**(토스 에러 코드 문서 "결제 조회" 절). 승인 전에 이탈한 주문을 조회했을 때 404가 오는지 200 `READY`·`IN_PROGRESS`가 오는지는 문서로 확정하지 못했지만, 앞의 것은 미승인으로, 뒤의 것은 `UNKNOWN`으로 처리하므로 어느 쪽이든 돈 손실 방향이 아닙니다.
- **조회 응답의 `orderId`가 요청과 다르면 `UNKNOWN`으로 둡니다**(토스 보안 체크리스트의 교차 검증). 금액은 `reconcileApproved`가 대조합니다.
- **조회로도 확정하지 못한 건은 01만으로는 자동 해소되지 않습니다.** 취소 차단도 그동안 유지됩니다. batch 주기 대사는 덩어리 02([ADR-0061](0061-pg-ports-to-core-batch-reconcile.md))가 다룹니다.
- **01 배포 전 데이터**(이미 FAILED가 된 결과 불명 건, `pg_provider`가 빈 PENDING 건)는 수동 대사 대상입니다.
- **같은 결제의 취소와 승인 시도가 서로 기다립니다.** 둘 다 결제 행 잠금을 잡으므로 짧게 직렬화됩니다(PG 호출은 잠금 밖).
- **금액 불일치 건은 PENDING으로 남아** 02 batch가 10분마다 `PG_DB_MISMATCH` ERROR를 반복합니다. 수동 대사가 필요하다는 신호로 감수합니다.
- **`pg_order_id` 인덱스는 제안만 하고 적용하지 않았습니다**(`docs/tasks/pg-confirm-recovery/pg-order-id-index.sql`, `schema.sql` 무변경 — 적용 여부는 사용자 결정). 적용하면 결제를 찾는 1단계 조회가 빨라지지만, 잠금 조회를 `pg_order_id` 직접 `FOR UPDATE`로 바꾸면 인덱스 next-key 잠금으로 **잠금 경합이 늘 수 있습니다**. 또 jpa `PaymentConcurrencyIntegrationTest`의 반증 시나리오 B("직접 `FOR UPDATE`면 전체 스캔 잠금이라 실패한다")는 인덱스 추가 후 성립하지 않습니다.

## 영향받은 문서

- `docs/domain/payment.md` · 카드 결제 3단계 · 실패 결과 표 · 결제 상태 표 · 결제 취소 규칙 — 결과 불명은 대기 유지·조회 대사, 미확정 결제 취소 차단, 주문 확정 불변식 예외
- `backend/application-core/AGENTS.md` · 코드 주석에서 이관된 설계 근거 · "PG 승인·실패 반영은 결제 행을 잠그고 …" 항목과 "PG·외부 왕복은 트랜잭션 밖에 둔다" 항목 — `UNKNOWN`·PG 시도 기록·멱등 반영·대사 결과
- `backend/infrastructure/tosspayments/AGENTS.md` · 승인 4xx 응답 code 보존 · 결제 조회 — `PG_SERVER_ERROR` 분리와 조회 매핑(`READY`→UNKNOWN, `CANCELED`→NOT_APPROVED)
- `backend/infrastructure/jpa/AGENTS.md` · 비관적 락 항목 reference · `PaymentPersistenceAdapter#findByPgOrderIdForUpdate` 절 — 취소 반영의 잠금 조회, 취소 vs 시도 기록 통합 시나리오, `pg_order_id` 인덱스 제안
- `backend/CLAUDE.md` · 예외·에러코드 소유 규칙 — `WebErrorCode` 개수·스냅샷 행 수

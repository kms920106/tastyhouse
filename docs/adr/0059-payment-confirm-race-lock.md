# ADR-0059: PG 승인·실패 반영은 결제 행을 잠그고, PG 승인 성공을 사실로 삼는다

- 상태: 채택(부분 → 0060)
- 결정 커밋: `fff56ac2b` (2026-10-18)
- 대체한 결정: —

## 맥락

같은 기법을 결제 행에 먼저 적용한 결정은 [ADR-0055](0055-payment-refund-pessimistic-lock.md)(환불 요청)입니다. 이 결정은 그것을 대체하지 않고, 대상을 토스 승인 반영 경로로 넓힙니다.

- **승인 흐름**: 사전 확인(`PaymentConfirmationService#preparePgConfirmation`, 잠금 없는 readOnly 트랜잭션) → PG 호출(트랜잭션 밖) → 성공이면 `applyPgConfirmation`, 실패면 `failPgConfirmation`(각각 별도 트랜잭션, `PaymentConfirmationExecutor`).
- **load-copy-save**: `PaymentPersistenceAdapter#save`가 관리 엔티티에 도메인 값을 복사하고 커밋 때 UPDATE합니다. `PAYMENT`에는 `@Version`이 없어 늦게 커밋한 쪽이 이깁니다.

같은 결제에 승인 요청이 두 번 오면(버튼 연타·성공 리다이렉트 재요청) 두 요청이 모두 사전 확인을 통과해 PG를 두 번 부릅니다. 첫 요청은 성공하고, 두 번째는 토스가 `ALREADY_PROCESSED_PAYMENT`(HTTP 400) 등으로 거절합니다. 실패 반영은 결제를 잠금 없이 읽고 PENDING이면 `fail()`을 저장했으므로, 돈은 결제됐는데 결제 기록이 FAILED가 되는 경우가 두 방향으로 생겼습니다.

| 커밋 순서 | 결과 |
|---|---|
| 승인 반영 → 실패 반영 | 실패 반영이 PENDING으로 읽은 뒤 늦게 커밋 → 결제 FAILED, 주문 CONFIRMED |
| 실패 반영 → 승인 반영 | 승인 반영이 FAILED를 보고 `PAYMENT_NOT_PENDING_APPROVAL`로 롤백 → 결제 FAILED, 주문 PENDING, 로그 `PG_DB_MISMATCH` |

게다가 `TossPaymentClient`가 4xx 응답의 code를 늘 `PG_API_ERROR`로 덮어써, "이미 처리됨"을 알아볼 수도 없었습니다.

## 이전 규칙

- 결제 상태 규칙(`docs/domain/payment.md`): 승인 반영은 결제 대기에서만, 실패 반영은 무조건 결제 실패로.
- `Payment#complete`만 상태 가드(PENDING)가 있었고 `Payment#fail`에는 없었습니다.

## 결정

세 겹으로 막습니다.

| 항목 | before | after |
|---|---|---|
| 승인·실패 반영의 결제 조회 | `PaymentLoadPort#findByPgOrderId`(잠금 없음) | `PaymentLoadPort#findByPgOrderIdForUpdate` — id를 스칼라로 찾은 뒤 PK 한 행에 `PESSIMISTIC_WRITE` |
| 토스 4xx 응답 code | 항상 `PG_API_ERROR` | 토스 본문의 code·message(해석 실패 시 `PG_API_ERROR`). 5xx는 그대로 `PG_API_ERROR`. 4xx 로그는 ERROR(스택 포함) → WARN(스택 없음) |
| 벤더 중립 실패 사유 | 없음(`errorCode` 문자열뿐) | `PgConfirmResult.failure` — `PgConfirmFailure.ALREADY_PROCESSED` / `REJECTED` |
| "이미 처리됨" 거절의 실패 반영 | 결제 FAILED | 결제 상태를 바꾸지 않음(원장은 남김) |
| 승인 반영이 허용하는 상태 | PENDING | PENDING·FAILED(PG 승인 성공 우선). COMPLETED·CANCELLED는 계속 거절 |
| `Payment#fail` | 상태 무관하게 FAILED | PENDING이 아니면 `DomainErrorCode.PAYMENT_NOT_PENDING` |
| 두 번째 요청의 HTTP 응답 | `PAYMENT_APPROVAL_FAILED`(400) + "결제 승인하기 API 호출에 실패했습니다: 400 …" | `PAYMENT_APPROVAL_FAILED`(400) + 토스 원문 메시지 |

## 근거

| 대안 | 채택하지 않은 이유 |
|---|---|
| `@Version` 낙관적 락 | `PAYMENT.version` 컬럼 DDL이 필요합니다. 충돌이 커밋 시점에 나면 실패 반영은 버려도 되지만 승인 반영은 재시도 루프(예약 슬롯의 3단 구조)가 있어야 합니다 |
| 사전 확인에서 APPROVING 상태로 선점(조건부 UPDATE) | 두 번째 요청이 PG를 아예 부르지 않게 되는 가장 강한 형태지만, 새 상태값과 "APPROVING에서 멈춘 결제" 복구 경로가 필요해 범위가 큽니다 |
| `pg_order_id`에 인덱스를 추가하고 직접 잠금 | DDL이 필요합니다. 인덱스 없이 `WHERE pg_order_id = ? FOR UPDATE`를 실행하면 테이블 전체가 잠기므로, 지금은 id를 먼저 찾고 PK로 잠급니다 |
| 잠금만 적용 | 실패 반영이 승인 반영보다 먼저 커밋하는 순서는 막지 못합니다. 그래서 "이미 처리됨" 무시와 PG 성공 우선을 함께 둡니다 |
| "이미 처리됨" 무시만 적용 | 토스가 동시 중복 승인에 다른 코드를 줄 수 있고, 잠금 없이는 늦게 커밋한 쪽이 여전히 이깁니다 |

FAILED→COMPLETED 허용은 사용자가 정한 규칙입니다. PG가 승인 성공을 돌려줬다면 돈은 이미 결제된 것이므로, 우리 기록이 실패여도 승인 쪽으로 바로잡는 것이 사실에 맞습니다. 사전 확인은 여전히 PENDING만 통과시키므로, 실패한 결제로 새 승인을 시작하는 경로는 생기지 않습니다.

## 결과·감수한 손실

- **새 에러코드가 없습니다.** `Payment#fail` 가드는 `PAYMENT_NOT_PENDING`을 재사용합니다. 메시지("대기 중인 결제만 완료 처리할 수 있습니다.")가 FAILED 완료 허용·실패 가드와 맞지 않지만, 바꾸면 wire 계약이 바뀝니다.
- **실패 메시지 문구가 바뀝니다.** 토스 4xx 전부에서 사용자 메시지가 토스 원문이 됩니다. 에러코드와 상태는 같습니다.
- **두 번째 요청은 여전히 실패 응답을 받습니다.** 결제·주문은 첫 요청 결과대로 완료·확정입니다. "이미 처리됨"이면 기존 paymentId를 성공으로 돌려주는 멱등 응답은 프론트 계약 변경이라 후속 과제입니다.
- **잠깐 FAILED가 보일 수 있습니다.** "이미 처리됨"이 아닌 코드로 거절된 실패 반영이 먼저 커밋되면, 승인 반영이 도착할 때까지 결제가 FAILED입니다.
- **응답 없음·통신 오류는 지금도 FAILED입니다.** 이 경우 승인 성공이 나중에 도착하는 경로가 없어 자동 복구되지 않습니다. `PgConfirmFailure.UNKNOWN`(PENDING 유지)과 대사(reconciliation)는 후속 과제입니다. `docs/domain/payment.md`가 이 경우를 "결제 대기 유지"로 적고 있던 서술은 실제 동작에 맞게 고쳤습니다.
- **`cancel()` 상태 가드와 동시 취소 직렬화는 이번 범위가 아닙니다.** 취소 경로는 별도 작업(M-5, 결제 취소 경로의 상태 가드·동시 취소 직렬화 — 이 ADR이 다루는 M-1에 이어지는 후속 결제 안전성 작업 번호이며 아직 스펙 문서가 없음)이 다룹니다.
- **잠금은 어댑터 단위로만 실제 DB에서 검증합니다.** 단위 테스트 `PaymentPersistenceAdapterTest`가 "id 조회 → PK 잠금" 순서를, Testcontainers 통합 테스트 `PaymentConcurrencyIntegrationTest`가 실제 MySQL에서 대기·행 단위 잠금을 검사합니다(`./gradlew build`에는 포함되지 않음). 승인·실패 반영 서비스 전체의 경합 재현은 하지 않았습니다.
- **"이미 처리됨" + PENDING은 경합과 반영 실패를 구분하지 못합니다.** 실패 반영은 `PG_DB_MISMATCH`를 WARN으로만 남깁니다(정상 연타 경합에서도 나므로 ERROR가 아님). 반영 실패 자체는 원 요청이 ERROR로 남기므로, WARN만 남는 것은 원 요청이 커밋 전에 프로세스 종료·`Error`로 로그 없이 끝난 경우입니다. 대사가 이 경우를 덮어야 합니다.
- **후속 과제**: `pg_order_id` 인덱스 추가(DDL), 응답 유실 대사(PG 승인인데 일정 시간 이상 PENDING인 결제 포함).

## 영향받은 문서

- `docs/domain/payment.md` · 카드 결제 3단계 · 실패 결과 표 · 결제 상태 표 · 온라인 카드 결제 승인 규칙
- `backend/application-core/AGENTS.md` · 코드 주석에서 이관된 설계 근거 · PG 승인·실패 반영 항목
- `backend/infrastructure/jpa/AGENTS.md` · 비관적 락 reference · `findByPgOrderIdForUpdate` 설계 근거
- `backend/infrastructure/tosspayments/AGENTS.md` · 승인 4xx 응답 code 보존

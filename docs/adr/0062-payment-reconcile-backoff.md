# ADR-0062: batch 미확정 결제 대사에 결제별 재시도 백오프를 둔다

- 상태: 채택(부분 → 0064)
- 결정 커밋: `fff56ac2b` (2026-10-18)
- 대체한 결정: ADR-0061(대사 대상이 매 주기 같은 결제를 다시 고르고, 금액 불일치 ERROR 반복을 감수하던 부분)

## 맥락

[ADR-0061](0061-pg-ports-to-core-batch-reconcile.md)의 주기 대사 잡은 10분마다 "PENDING + `pg_provider` 있음" 결제를 **생성 순으로 100건** 골라 PG에 다시 묻습니다. 그런데 PG 조회 결과가 `UNKNOWN`이거나, `APPROVED`인데 반영 결과가 `AMOUNT_MISMATCH`인 결제는 상태가 PENDING 그대로라 7일 동안 매 주기 다시 뽑힙니다. 그 결과 두 가지가 생깁니다.

- **알럿 반복**: `AMOUNT_MISMATCH` 한 건이 `PG_DB_MISMATCH` ERROR를 7일 × 144회 ≈ 1,000회 남깁니다. 진짜 신호가 노이즈에 묻힙니다.
- **앞줄 막힘**: 그런 결제가 100건 쌓이면 limit 100을 전부 차지해, 그보다 새 결제는 대사되지 않습니다(그 결제들의 취소 409 차단도 풀리지 않습니다).

## 이전 규칙

- 대상 조회는 `PaymentLoadPort#findAllPgAttemptedByStatusCreatedBetween(status, from, to, limit)`였고, 이미 시도한 결제를 거르는 조건이 없었습니다.
- 금액 불일치 ERROR의 10분 반복은 "수동 대사가 필요하다는 신호"로 감수했습니다.

## 결정

| 항목 | before | after |
|---|---|---|
| `PAYMENT` 컬럼 | 없음 | `reconcile_attempt_count INT NOT NULL DEFAULT 0`, `reconcile_next_at DATETIME NULL` |
| 대상 조회 | `findAllPgAttemptedByStatusCreatedBetween(status, from, to, limit)` | `findAllPgAttemptedReconcileDueByStatusCreatedBetween(status, from, to, dueAt, limit)` — 기존 조건 + `reconcile_next_at IS NULL OR reconcile_next_at <= dueAt` |
| 건별 처리 | PG 조회부터 | PG 조회 **전에** `PaymentReconcileExecutor#scheduleNextAttempt`(트랜잭션)가 횟수를 1 올리고 다음 시각을 저장. 기록이 실패해도 조회·반영은 계속 |
| 다음 시각 | — | `now + min(10분 × 2^이전 시도 횟수, 24시간) − 1분`. 1분은 cron 실행 시각의 밀리초 차이로 한 주기씩 밀리지 않게 두는 여유 |
| web 승인 시도(`PaymentConfirmationService#recordPgAttempt`) | 결제 저장만 | + 대사 일정 초기화: 횟수 0, 다음 시각 = 시도 시각 + 10분 |
| 엔티티 매핑 | — | `PaymentJpaEntity`의 두 필드는 `insertable = false, updatable = false`. 쓰기는 어댑터의 전용 QueryDSL UPDATE(`PaymentSavePort#saveReconcileSchedule`)만 한다 |
| 도메인 모델 `Payment` | — | 변경 없음 |

## 근거

| 대안 | 채택하지 않은 이유 |
|---|---|
| 배치 서비스의 메모리에 시도 횟수를 둔다 | DDL이 필요 없지만 batch를 재시작할 때마다 초기화돼 반복 알럿이 다시 시작되고, 인스턴스가 늘면 공유되지 않습니다 |
| `updated_at`을 마지막 시도 시각으로 쓴다 | 다른 저장도 `updated_at`을 바꾸므로 "대사를 시도했다"는 뜻이 되지 못합니다 |
| 생성 시각에 비례하는 간격(다음 시각 = now + 결제 나이) | 횟수 컬럼이 필요 없지만, 생성 후 한참 지나 승인을 시도한 결제는 첫 재조회부터 수 시간 뒤로 밀려 취소 409가 오래 풀리지 않습니다 |
| 시도 횟수 상한을 넘으면 대상에서 아예 뺀다 | 알럿은 완전히 멎지만, 사람이 맞추기 전까지 자동 확정 기회도 사라집니다. 24시간 상한 백오프면 7일 동안 약 14회로 충분히 줄어듭니다 |
| 두 필드를 `Payment` 도메인 모델에 둔다 | 결제 상태 규칙이 아니라 batch 대사의 운영 일정이고, `reconstitute` 호출부·매퍼·`applyChanges`까지 파급됩니다 |
| 엔티티를 일반 매핑으로 두고 `applyChanges`에서 빼기만 한다 | Hibernate는 엔티티 flush 때 모든 컬럼을 UPDATE하므로, 다른 저장이 batch가 방금 쓴 값을 옛 값으로 덮을 수 있습니다 |
| web 승인 시도 때 다음 시각을 NULL로 초기화 | NULL은 "즉시 대상"이라, 생성 후 10분이 지난 결제는 재승인 직후 다음 batch 주기에 바로 대사돼 잠금 밖에서 진행 중인 web PG 호출과 겹칩니다 |

## 결과·감수한 손실

- **DDL이 배포 선행 조건입니다.** 엔티티가 두 컬럼을 매핑하므로 컬럼이 없으면 4개 앱(web-api·admin-api·ceo-api·batch-module) 모두 `ddl-auto: validate`로 부팅에 실패합니다(`infrastructure:persistence`를 4앱이 모두 싣고 `JpaModuleConfig`가 jpa 엔티티 전체를 스캔합니다). 로컬 반영 DDL은 `docs/tasks/pg-confirm-recovery/03-reconcile-backoff/payment-reconcile-schedule.sql`입니다.
- **확정이 늦어질 수 있습니다.** 시도가 쌓인 결제는 최대 24시간 간격으로만 다시 확인하므로, 취소 409의 자동 해제와 `PG_DB_MISMATCH` 반영 실패의 자동 교정도 그만큼 늦어집니다. 사용자가 승인을 다시 시도하면 즉시 조회와 일정 초기화가 함께 일어납니다.
- **금액 불일치 ERROR는 7일 동안 약 14회로 줄어듭니다**(before 약 1,000회). 사라지지는 않으므로 수동 대사 신호로서는 그대로 남습니다.
- **횟수 읽기·쓰기가 원자적이지 않습니다.** batch를 여러 대 띄우거나 같은 순간 web 초기화가 끼면 횟수가 한 번 덜 세거나 초기화가 덮일 수 있습니다. 결과는 한 번 일찍 또는 한 백오프 간격 늦게 재시도하는 것뿐이지만, 그 간격은 최대 24시간입니다. 같은 순간 사용자의 재승인이 겹치는 드문 경우라 원자화(`count = count + 1`·잠금 조회)는 하지 않았습니다(기존 단일 인스턴스 전제와 같음).
- 첫 재조회 간격은 10분으로 이전과 같습니다. 다만 테스트 환경 실측으로 토스의 `EXPIRED` 확정은 결제 생성 후 약 33분이라(라이브 미확인), 첫 재조회에서는 대개 아직 `IN_PROGRESS`입니다.

## 영향받은 문서

- `docs/domain/payment.md` · 10분 주기 점검 절 — 다음 확인 간격, 재시도 시 초기화
- `backend/application-core/AGENTS.md` · batch 주기 대사 항목 — 새 포트 이름, 백오프, `recordPgAttempt` 초기화
- `backend/infrastructure/jpa/AGENTS.md` · `PaymentPersistenceAdapter#findAllPgAttemptedReconcileDueByStatusCreatedBetween` 절 — 다음 시각 조건, 두 컬럼의 `insertable/updatable = false` 근거
- `backend/batch-module/AGENTS.md` · 잡 주기가 다른 이유 — 백오프
- `backend/batch-application/AGENTS.md` · 무엇이 여기 사는가 — 대사 일정 기록

# ADR-0061: PG 포트·라우터를 코어로 옮기고, batch가 미확정 결제를 주기적으로 대사한다

- 상태: 채택(부분 → 0062)
- 결정 커밋: `fff56ac2b` (2026-10-18)
- 대체한 결정: ADR-0036(PG SPI 포트·라우터를 `web-application`에 두던 부분)

## 맥락

[ADR-0060](0060-pg-confirm-unknown-lookup.md)으로 PG 승인 결과를 모르는 결제는 FAILED가 아니라 "PENDING + `pg_provider` 있음"으로 남고, web 요청 안에서 토스에 한 번 조회해 대부분 그 자리에서 확정됩니다. 그러나 조회로도 확정하지 못한 건(토스 `IN_PROGRESS`, 조회 실패)과 승인 반영이 커밋 전에 끊긴 건은 사용자가 승인을 다시 시도하지 않는 한 PENDING으로 남고, 그동안 사용자의 결제 취소도 409로 막힙니다. 이것을 사람 손 없이 풀려면 web 요청과 무관하게 주기적으로 PG에 다시 묻는 잡이 필요합니다.

잡은 `batch-module`에 두는 것이 자연스럽지만, PG 포트(`PgProviderGatewayPort`·`PgPaymentGatewayPort`)와 계약 타입(`PgCancelResult`·`PgProviderCode`), 라우터(`PgPaymentGatewayRouter`)가 모두 `web-application`에 있었고, 벤더 `infrastructure:tosspayments`가 `:web-application`을 의존했습니다. 이 상태로 batch에 `infrastructure:pg`를 붙이면 web-application jar가 batch 클래스패스에 실리고, 필터 없는 `com.tastyhouse.application` 스캔 때문에 **web 빈 전체가 batch에 뜹니다**(`ApplicationModuleClasspathTest`가 실패합니다).

## 이전 규칙

- PG SPI 포트 4종과 라우터는 "web 전용 SPI"로 `web-application` 소유였습니다([ADR-0036](0036-app-marker-removal.md)).
- `infrastructure:tosspayments`는 `:web-application`을, PG 스타터 `infrastructure:pg`는 web-api만 의존했습니다.
- 미등록 PG 거절 코드 `PG_PROVIDER_UNSUPPORTED`는 `WebErrorCode`였습니다.
- 결과를 확정하지 못한 결제는 사용자의 재시도나 운영자의 수동 대사로만 확정됐습니다.

## 결정

| 항목 | before | after |
|---|---|---|
| PG 포트 2종·`PgCancelResult`·`PgProviderCode`·라우터(+`PgPaymentGatewayRouterTest`) | `web-application` | `application-core`(패키지 동일 — import 불변) |
| `PG_PROVIDER_UNSUPPORTED` | `WebErrorCode` | `ApplicationErrorCode`(code·status·message 동일) |
| `infrastructure:tosspayments` 의존 | `:web-application` | `:application-core` |
| `batch-module` | PG 없음 | `runtimeOnly ':infrastructure:pg'` + `classpath:application-pg.yml`, 환경 변수 `TOSS_SECRET_KEY` |
| 라우터 빈 | web에만 | 4앱 모두. admin·ceo는 벤더가 없어 빈 목록으로 생성 |
| 대상 조회 | 없음 | `PaymentLoadPort#findAllPgAttemptedByStatusCreatedBetween(PaymentStatus, from, to, limit)` — `payment_status = ?`, `pg_provider IS NOT NULL`, 결제 **생성 시각** 반개구간 `from <= created_at < to`(승인 시도 시각 아님), `created_at, id` 오름차순, `LIMIT`, 잠금 없음 |
| 주기 대사 잡 | 없음 | `PaymentReconcileScheduler`(cron `${payment.reconcile.cron:0 */10 * * * *}`) → `ReconcileUnconfirmedPaymentsUseCase` → `PaymentReconcileSchedulerService`(대상 PENDING, now−7일 ~ now−10분, 100건) → 건별 `PgPaymentGatewayPort#lookupPayment` → `APPROVED`면 `reconcileApproved`, `NOT_APPROVED`면 조회 결과로 `failPgConfirmation`(FAILED), `UNKNOWN`이면 건너뜀(`PG_CONFIRM_UNKNOWN` WARN). `APPROVED` 반영이 예외면 `PG_DB_MISMATCH` ERROR. 대상 0건은 DEBUG. 반영은 `PaymentReconcileExecutor`(`@Transactional`) |

## 근거

| 대안 | 채택하지 않은 이유 |
|---|---|
| batch-application에 PG 포트를 복제 | 같은 FQCN이 두 모듈에 생겨 `SplitPackageUniquenessTest`가 실패하고, 이름을 바꿔 복제하면 벤더가 두 포트를 함께 구현해야 합니다 |
| batch가 `web-application`을 의존 | web 빈 전체가 batch에 뜹니다. 앱 간 수평 의존을 막는 모듈 경계의 존재 이유와 정면으로 충돌합니다 |
| 대사를 web-api의 `@Scheduled`로 | web은 여러 인스턴스로 뜰 수 있어 중복 실행되고, 스케줄러는 batch-module로 일원화돼 있습니다 |
| 라우터를 batch·web 각 앱 모듈에 하나씩 | 같은 로직 두 벌입니다. 2개 앱 이상이 쓰는 것은 코어에 둔다는 규칙에 맞지 않습니다 |
| 대상 조회의 상태 비교값을 어댑터에 고정(`findAllPgAttemptedPending…`) | 쓰기 포트는 상태를 도메인 enum 인자로 받는 것이 기존 형태(`existsByProductIdAndStatus(…, ApprovalStatus)` 등)이고, 도메인 정책(PENDING만 대사)을 영속 어댑터에 박지 않습니다 |
| 대상 창을 생성 직후부터 | web 요청 안의 즉시 조회와 같은 결제를 동시에 다룰 수 있습니다. 결제 행 잠금으로 결과는 맞지만 쓸데없는 PG 호출이 늘어 10분을 둡니다 |
| `UNKNOWN`이면 실패 반영(`failPgConfirmation(UNKNOWN)`)으로 원장·WARN | 10분마다 같은 원장 행과 `PG_CONFIRM_UNKNOWN` WARN이 쌓입니다. 잡 자체의 건너뜀 WARN과 건수 로그로 충분합니다 |

## 결과·감수한 손실

- **API 변경 없음.** wire 계약(`PG_PROVIDER_UNSUPPORTED`의 code·status·message)이 같아 `error-catalog-before.tsv`는 바뀌지 않았습니다(스냅샷은 카탈로그 enum 이름을 담지 않습니다). 카탈로그 개수는 `ApplicationErrorCode` 84 → 85, `WebErrorCode` 89 → 88입니다.
- **라우터 빈이 admin·ceo에도 뜹니다.** 빈 목록이라 동작은 없고, 주입하는 빈도 없습니다. `coreBeansShouldOnlyDependOnCoreVisibleTypes`는 `List` 주입을 검사하지 않으므로 이 상태를 가드가 따로 막지 않습니다.
- **batch-module 기동에 `TOSS_SECRET_KEY`가 필요합니다.** 없으면 토스 호출이 인증 실패로 끝나 모든 건이 `UNKNOWN`으로 건너뛰어집니다(돈 손실 방향 아님).
- **처리 상한**: 한 주기 100건, 생성 순. `UNKNOWN`이 계속 쌓이면 오래된 건이 슬롯을 차지할 수 있고, 건너뜀 건수 로그로 관측합니다. 7일을 넘긴 건(토스 `PARTIAL_CANCELED` 같은 예상 밖 상태 포함 — `CANCELED`는 NOT_APPROVED로 FAILED 처리)은 대상에서 빠져 수동 대사로 넘어갑니다.
- **확정까지 걸리는 시간**: 토스가 결제를 `EXPIRED`로 확정하는 시점에 좌우됩니다. 테스트 환경 실측으로는 결제 생성 후 약 33분이므로(라이브 미확인), 승인 전 상태로 남은 `IN_PROGRESS` 결제는 10분 대기 + 10분 주기의 첫 점검들에서는 정리되지 않고 만료 이후 점검에서 정리됩니다. 그동안 결제 취소 차단(409)이 유지됩니다.
- **금액 불일치는 10분마다 ERROR를 반복합니다.** `AMOUNT_MISMATCH`는 결제를 PENDING으로 두므로 다음 주기에 다시 조회돼 `PG_DB_MISMATCH` ERROR가 7일 동안 반복됩니다. 수동 대사가 필요하다는 신호로 감수합니다.
- **단일 인스턴스 전제**: batch-module을 여러 대 띄우면 같은 결제를 동시에 조회합니다. 반영은 결제 행 잠금과 멱등 판정으로 맞지만 PG 호출이 중복됩니다(기존 batch 운영 전제와 같음).
- `pg/AGENTS.md`의 절 제목 "라우터 등록은 어디인가 — `web-application`의 `PgPaymentGatewayRouter`"는 ADR-0026·0034·0036이 인용하므로 바꾸지 않고, 제목 아래에 라우터가 지금 `application-core`에 있다는 안내를 둡니다(참조되는 제목 유지 규칙).

## 영향받은 문서

- `CLAUDE.md` · 리포지토리 구조 · "앱별 사용" 문단 — `pg`는 web·batch, PG 포트·라우터는 코어
- `backend/CLAUDE.md` · 모듈 지도 · 앱별 runtimeClasspath 감사표 · 앱별 의존 · 공존형 채널 — PG 라우터 · 앱 모듈 경계 규칙(무엇이 어디에 사는가·실행 앱·벤더의 의존·가드 anchor) · 예외·에러코드 소유 규칙(카탈로그 개수) — PG SPI 코어 이동, batch의 `:pg`, `ApplicationErrorCode.PG_PROVIDER_UNSUPPORTED`, batch UseCase 8
- `backend/application-core/AGENTS.md` · 앱 전용 SPI 포트 · "PG 승인·실패 반영은 결제 행을 잠그고 …" 항목 · Executor를 별도 빈으로 분리하는 이유 · 결제 승인 — PG HTTP 왕복은 트랜잭션 밖 — PG SPI는 앱 전용이 아님, batch 주기 대사
- `backend/infrastructure/pg/AGENTS.md` · 라우터 등록은 어디인가 · 포트 2단 구조 · 어느 앱이 의존하는가 — 코어 라우터, `supports(String)` 정정, web·batch 의존
- `backend/infrastructure/tosspayments/AGENTS.md` · 포트 반환 타입 · 어느 앱이 의존하는가 · Dependencies — `:application-core` 의존
- `backend/AGENTS.md` · 모듈 표 · Module Dependency Graph · 계층·앱 축 · Testing Requirements — 라우터·포트 코어 위치, batch의 `:pg`, `*SchedulerService` 8
- `backend/web-api/AGENTS.md` · `infrastructure:pg` 의존 항목 — 라우터·포트가 코어
- `backend/infrastructure/jpa/AGENTS.md` · `PaymentPersistenceAdapter#findAllPgAttemptedReconcileDueByStatusCreatedBetween` 절 — 반개구간·정렬·`pg_provider IS NOT NULL`
- `backend/batch-module/AGENTS.md` · 스케줄 주기 근거 — 생성 시각 기준 대상 창, 로그 레벨
- `backend/web-application/AGENTS.md` · 무엇이 여기 사는가 · 의존 — PG 포트·라우터가 코어로 이동
- `backend/batch-module/AGENTS.md` · 잡 표 · Dependencies · 설정 파일 · 스케줄러 활성 상태 · 잡 주기가 다른 이유 — `payment` 잡, `infrastructure:pg`
- `backend/batch-application/AGENTS.md` · 무엇이 여기 사는가 — 잡 UseCase 8개, PG 포트는 코어
- `docs/domain/payment.md` · 카드 결제 3단계 — 10분 주기 점검

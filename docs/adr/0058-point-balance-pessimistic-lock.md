# ADR-0058: 포인트 잔액 변경은 POINT 행 비관적 락으로 직렬화한다

- 상태: 채택
- 결정 커밋: `18112f232` (2026-10-17)
- 대체한 결정: —

## 맥락

같은 기법을 결제 행에 먼저 적용한 결정은 [ADR-0055](0055-payment-refund-pessimistic-lock.md)입니다. 이 결정은 그것을 대체하지 않고, 대상을 포인트 잔액으로 넓힙니다.

- **lost update(갱신 손실)**: 두 트랜잭션이 같은 값을 읽고 각자 계산한 결과를 덮어써서, 한쪽 변경이 사라지는 현상입니다.
- **포인트 잔액**: `POINT` 테이블에 회원당 한 행이 있고(`member_id` UNIQUE), `available_points` 컬럼에 사용 가능 잔액을 담습니다. 잔액을 바꿀 때마다 `POINT_HISTORY`에 이력 한 줄이 함께 남습니다.

잔액을 바꾸는 경로는 다섯 개이고, 전부 `application-core`의 `PointLedgerService`를 거칩니다.

| 경로 | 호출부 |
|---|---|
| 주문 포인트 사용 | `OrderPlacementService#place` → `usePoints` |
| 관리자 차감·적립 | `PointDeductService`·`PointEarnService`(admin-application) |
| 결제 취소 환급·적립분 회수 | `PaymentEventListener#onPaymentCancelled` |
| 현장결제 적립 | `PaymentEventListener#onPaymentCompleted` |
| 추천 보상 | `ReferralRegisteredEventListener#handle` |

`PointPersistenceAdapter`는 잔액을 잠금 없이 읽었습니다. 도메인 `Point`가 계산을 마치면, 그 절대값을 엔티티에 복사하고 커밋 때 `UPDATE POINT SET available_points = <계산값>`을 실행했습니다. 그래서 잔액 1,000P인 회원이 1,000P를 쓰는 주문 두 건을 동시에 넣으면 이렇게 됩니다.

1. 두 트랜잭션이 둘 다 1,000을 읽습니다.
2. 둘 다 잔액 검사를 통과합니다.
3. 둘 다 0을 씁니다.

결과적으로 할인은 2,000P가 나가는데 잔액은 0이 되고, 이력에는 −1,000이 두 줄 남아 잔액과 이력이 어긋났습니다.

## 이전 규칙

포인트 경로에는 동시성 제어가 없었습니다. 저장소의 잠금 규칙은 `backend/infrastructure/jpa/AGENTS.md` "규칙" 절의 비관적 락 항목(ADR-0055)이었고, 이름은 `findByIdForUpdate`, 락 로드는 "그 트랜잭션의 첫 조회"로 정해져 있었습니다.

## 결정

`PointLoadPort`의 잔액 로드를 비관적 락(PESSIMISTIC_WRITE)으로 바꾸고, 이름에 그 사실을 드러냅니다.

| 항목 | before | after |
|---|---|---|
| 포트 메서드 | `PointLoadPort#findByMemberId` | `PointLoadPort#findByMemberIdForUpdate` |
| 어댑터 쿼리 | `selectFrom … where … fetchOne()` | 같은 쿼리에 `.setLockMode(LockModeType.PESSIMISTIC_WRITE)` 추가 |
| 같은 회원 동시 요청 | 둘 다 같은 잔액으로 판정·덮어쓰기 | 두 번째 요청이 첫 요청 커밋까지 대기한 뒤 갱신된 잔액으로 판정 |
| 잔액 부족 응답 | 동시 요청이면 발생하지 않음 | 뒤 요청이 기존 `DomainErrorCode.POINT_INSUFFICIENT`를 받음(새 에러코드 없음) |
| 적립(`earnPoints`)의 행 확보 | 잠금 없는 로드, 없으면 새 행 저장 | `PointSavePort#saveIfAbsent`(원자적 upsert — `INSERT … ON DUPLICATE KEY UPDATE`)로 행을 보장한 뒤 잠금 로드 |
| `POINT`의 중복 인덱스 | `member_id` UNIQUE와 비유니크 `idx_point_member_id` 공존(`schema.sql`·`PointJpaEntity` 양쪽 선언) | `idx_point_member_id` 제거. 로컬·운영 DB는 `ALTER TABLE POINT DROP INDEX idx_point_member_id` 적용 필요 |
| 저장소 규칙 | 이름 `findByIdForUpdate`, 락은 언제나 첫 조회 | 이름 `find…ForUpdate`. "첫 조회" 조건은 락 뒤에 일반 조회로 다시 읽는 판정 값이 있을 때만 적용. "없으면 만든다" 경로는 원자적 upsert 후 잠금 로드. 여러 행을 한 트랜잭션에서 잠글 때는 키 오름차순 |

## 근거

| 대안 | 채택하지 않은 이유 |
|---|---|
| 조건부 원자 UPDATE(`UPDATE … SET available = available - ? WHERE member_id = ? AND available >= ?` 후 영향 행 수 확인) | 잔액 검사가 도메인 `Point#deductPoints`에서 SQL로 새어 나갑니다. 또 회수(남은 잔액만큼만 차감 — `LEAST`), 적립, 최초 행 생성마다 전용 포트 메서드를 따로 만들어야 하고, 도메인 모델이 쓰이지 않는 경로가 생깁니다 |
| `@Version` 낙관적 락 | `POINT`에 `version` 컬럼을 추가하는 DDL이 필요합니다. 충돌하면 잔액이 충분한 요청도 실패하므로 재시도 루프와 에러 매핑을 새로 설계해야 합니다 |

비관적 락이 가장 단순합니다.

- 컬럼 변경과 도메인 로직 변경이 없고, 어댑터·포트와 적립의 행 확보 방식만 바뀝니다. 스키마 변경은 중복 인덱스 제거 하나입니다.
- 회원당 행이 하나이고 `member_id`가 UNIQUE라 InnoDB 레코드 락 하나만 잡히므로, 잠금 범위가 좁습니다.

## 결과·감수한 손실

- **"첫 조회" 조건을 정확히 했습니다.** ADR-0055의 결제 경로는 락을 얻은 뒤 누적 합계를 일반 조회로 다시 읽으므로, 그 앞에 일반 조회가 있으면 스냅샷 때문에 경합이 통과합니다. 포인트는 판정 값(잔액)을 락 로드 자체가 읽습니다. `FOR UPDATE`는 locking read라 최신 커밋 값을 읽으므로, 주문 경로처럼 상품·쿠폰을 먼저 일반 조회한 뒤 잠가도 안전합니다. 이 차이를 규칙 문구에 반영했습니다.
- **적립은 원자적 upsert로 행을 보장한 뒤 잠급니다.** 가입 시 `POINT` 행을 만드는 코드가 없어, 모든 신규 회원의 첫 적립(추천 보상의 피추천인, 첫 현장결제)이 "행 없음" 경로를 탑니다. 이 경로의 방식을 네 가지 검토했습니다.
  - **곧장 잠금 로드**(처음 구현): 행이 없는 UNIQUE 키에 `FOR UPDATE`를 걸면 InnoDB(REPEATABLE READ)는 gap lock을 잡습니다. 신규 회원은 member_id가 가장 커서 그 gap이 대개 같은 `(최대 member_id, supremum)` 구간입니다. gap lock끼리는 호환되므로, **서로 다른** 신규 회원 두 명의 첫 적립이 겹치면 둘 다 gap lock을 잡은 뒤 INSERT의 insert-intention lock에서 상대를 기다려 데드락이 납니다. 추천 보상 리스너(`AFTER_COMMIT`)에서 나면 추천인·피추천인 보상이 함께 롤백되고 조용히 사라집니다. 리뷰에서 드러나 폐기했습니다.
  - **존재 확인 → 있으면 잠금 로드 / 없으면 INSERT**(두 번째 구현): 위 데드락은 피하지만, 행이 없는 **같은** 회원에게 적립 2건이 동시에 오면 둘 다 "없음"을 보고 INSERT해 한쪽이 `member_id` 유니크 위반으로 롤백됩니다. 변경 전부터 있던 유실이 그대로 남아 폐기했습니다.
  - **가입 시 행 생성 + 기존 회원 백필**: "회원당 POINT 행이 항상 있다" 불변식이 생기지만, 백필 DML 적용과 백필→배포 순서가 필요합니다. 또 member 컨텍스트 서비스는 point의 쓰기 포트를 직접 쓸 수 없어(`ServiceContextBoundaryTest`) 이벤트 리스너를 거쳐야 합니다. 채택하지 않았습니다.
  - **채택 — `saveIfAbsent` upsert 후 잠금 로드**: `INSERT INTO POINT … ON DUPLICATE KEY UPDATE member_id = member_id` 한 문장입니다. 키가 없으면 insert intention lock만 잡아 서로 다른 회원의 동시 생성이 막히지 않습니다. 같은 회원이 동시에 오면 뒤 트랜잭션이 앞의 미커밋 INSERT 행에서 기다렸다가 중복으로 처리되고, 이어지는 `FOR UPDATE`가 앞이 커밋한 값을 읽어 두 적립이 모두 반영됩니다. `ON DUPLICATE KEY UPDATE`는 중복 행에 exclusive next-key lock을 잡으므로 S→X 업그레이드 교착도 없습니다. 대가는 다음과 같습니다.
    - 어댑터의 MySQL 네이티브 문장 1개.
    - 행이 이미 있어도 적립마다 이 문장이 한 번 실행되는 비용(인덱스 조회 1회).
    - 중복으로 끝나도 AUTO_INCREMENT 값을 소비해 `POINT.id`에 빈 번호가 생깁니다(`BIGINT`라 고갈 위험은 없습니다).
    - next-key lock이 중복 행 바로 앞 gap도 잠그므로, 그 gap으로 들어오는 다른 회원의 첫 적립 INSERT가 커밋까지 기다릴 수 있습니다. 교착이 아니라 대기입니다.
- **사용·차감·환급·회수를 행이 없는 회원에게 하면 gap lock을 잠깐 잡습니다.** 바로 `POINT_NOT_FOUND`로 롤백되며 INSERT를 하지 않으므로 데드락을 만들지 않습니다. 그 사이 같은 gap에 들어오는 첫 적립 INSERT가 롤백까지 잠깐 기다릴 수 있습니다.
- **락 대기 타임아웃은 번역하지 않습니다.** `innodb_lock_wait_timeout`(기본 50초)을 넘기면 `CannotAcquireLockException`이 500으로 나갑니다. ADR-0055와 같은 상태입니다.
- **여러 회원의 행은 member_id 오름차순으로 잠급니다.** 한 트랜잭션에서 두 회원의 행을 잠그는 경로는 추천 보상 하나뿐입니다. 추천인은 기존 회원이고 피추천인은 방금 가입한 회원이라 추천인 → 피추천인 순서가 곧 오름차순입니다. 이 순서를 규칙으로 고정했습니다.
- **실제 DB 동시성은 Testcontainers 통합 테스트로 지킵니다.** `PointConcurrencyIntegrationTest`(MySQL 8.0 컨테이너, `backend/schema.sql` 전체 적재)가 다섯 가지를 검사합니다 — 동시 사용 직렬화, 잠금 대기 후 최신 값 읽기, 같은 회원 동시 첫 적립, 서로 다른 회원 동시 첫 적립, 잠금 조회의 UNIQUE 인덱스 사용과 중복 인덱스 부재. 락을 떼거나 upsert를 일반 INSERT로 바꾸면 앞의 세 시나리오가 실패하는 것을 확인했습니다.
  - Docker가 없는 환경에서 `./gradlew build`가 깨지지 않도록 `@Tag("integration")`으로 분리했고, `./gradlew :infrastructure:jpa:integrationTest`로 실행합니다. 대가로 이 태스크를 따로 돌리지 않으면 회귀를 잡지 못합니다.
  - 단위 테스트도 남깁니다. `PointPersistenceAdapterTest`는 `setLockMode(PESSIMISTIC_WRITE)` 호출을, `PointLedgerServiceTest`는 각 경로의 호출 순서(적립은 `saveIfAbsent` → 잠금 로드)를 검사합니다.
- **중복 인덱스 `idx_point_member_id`를 제거했습니다.** 옵티마이저가 비유니크 인덱스를 고르면 next-key·gap lock이 잡혀 잠금 범위가 넓어지기 때문입니다. `schema.sql`과 `PointJpaEntity`의 선언을 함께 지웠습니다. `ddl-auto: validate`는 인덱스를 검사하지 않으므로 부팅에는 영향이 없지만, 로컬·운영 DB에는 `ALTER TABLE POINT DROP INDEX idx_point_member_id`를 따로 적용해야 합니다.

- **후속 과제 — 쿠폰 사용**: `CouponIssueService#useCoupon`에도 같은 계열의 read-modify-write 경합이 있는지 점검합니다.

## 영향받은 문서

- `backend/infrastructure/jpa/AGENTS.md` · 규칙 · 비관적 락 항목(이름 규칙 일반화, "첫 조회" 조건, "없으면 만든다" upsert, 잠금 순서, 통합 테스트, 포인트 reference)
- `CLAUDE.md`(루트) · backend는 `java -jar`로 실행합니다 · `integrationTest`가 `build`에 포함되지 않는다는 안내
- `docs/domain/point.md` · "비즈니스 규칙" · 동시 요청 직렬화 행 추가

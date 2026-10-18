# batch-application

**batch(`batch-module`) 하나만 쓰는 application 계층을 담는 앱 모듈.** 앱 마커 제거로 신설됐다. 과거에는 이 코드가 `application` 한 모듈 안에서 `@BatchApp` 마커로 구분됐으나, 지금은 **이 모듈에 있다는 사실 자체가 "batch 소속"** 이다. 5모듈 공통 규칙의 정본은 `backend/application-core/AGENTS.md`(코어)이고, 모듈 지도와 배치 기준은 `backend/CLAUDE.md`의 "앱 모듈 경계 규칙" 절이다. 잡별 UseCase·트리거 대응표는 `backend/batch-module/AGENTS.md`에 있다. 이 문서는 요약만 둔다.

## 무엇이 여기 사는가

- batch 전용 `@Service`/`@Component` 빈 — `*SchedulerService`(잡 오케스트레이터)와 batch만 쓰는 도메인 서비스·러너(예: `AdminDongSyncRunner`)
- 잡 UseCase 인터페이스 8개와 Command record(`<ctx>/port/in/`)
- **batch 전용 SPI 포트**(구현 벤더가 batch에만 조립되는 포트):
  - `crawling.bbq.port.out.*` — `BbqMenuPort`·`RemoteImagePort`와 DTO. 구현 `infrastructure:bbq`
  - `region.port.out.{AdminDongBoundaryPort,AdminDongBoundaryFetchResult,AdminDongBoundarySource,BoundaryCoordinate,BoundaryRing}` — 구현 `infrastructure:admdongkor`
- **PG 결제 조회 포트는 이 모듈이 아니라 코어에 있다** — 주기 대사 잡(`payment/service/PaymentReconcileSchedulerService`)이 쓰는 `PgPaymentGatewayPort`와 라우터 `PgPaymentGatewayRouter`는 web도 함께 쓰므로 `application-core` 소유다. 구현 벤더는 `batch-module`이 `runtimeOnly ':infrastructure:pg'`로 조립한다([ADR-0061](../../docs/adr/0061-pg-ports-to-core-batch-reconcile.md)).
- **대사 재시도 일정은 `payment/service/PaymentReconcileExecutor`가 기록하고, 다음 시각 계산은 `payment/service/PaymentReconcileBackoffPolicy`가 한다** — `claimNextAttemptInNewTx`(트랜잭션)가 `PaymentLoadPort#findReconcileAttemptCount`로 이전 시도 횟수를 읽고 `PaymentSavePort#saveReconcileScheduleIfDue`로 "횟수가 읽은 값 그대로이고 다음 시각이 실행 시작 시각 기준으로 비었거나 지났을 때만" 횟수+1과 다음 시각(`PaymentReconcileBackoffPolicy#nextAttemptAt` — `max(실행 시작 시각 + min(10분 × 2^이전 횟수, 24시간) − 1분, 선점 시각 + 2분)`)을 저장한다. 앞항은 cron 격자에 맞춰 같은 주기에 선점된 결제가 선점 순서와 무관하게 같은 다음 실행에서 재시도되게 하고, 뒤항(`CLAIM_GUARD` 2분)은 주기가 길어져 늦게 선점했을 때 그 사이 시작하는 다른 실행이 처리 중인 결제를 다시 선점하지 못하게 한다. `CLAIM_GUARD`는 선점 뒤 한 결제의 최악 처리 시간(토스 조회 소켓 타임아웃 연결 5초 + 읽기 10초(`infrastructure:restclient`의 `RestClientConfig`), 반영의 행 잠금 대기 — `innodb_lock_wait_timeout` 미설정이라 MySQL 기본 50초, 커넥션 획득 3초(`application-mysql.yml`) ≈ 68초)보다 길어야 하므로, 이 타임아웃 중 하나를 늘리면 `CLAIM_GUARD`도 함께 검토한다. 대가로 주기 시작 8분 뒤 이후 선점분은 한 주기 늦게 재시도된다. 저장이 0건이면 다른 실행(다른 batch 인스턴스)이 먼저 차례를 가져갔거나 web 승인 시도가 일정을 초기화한 것이라 빈 값을 돌려주고, 1건이면 `PaymentReconcileClaim(attemptCount, nextAttemptAt)`을 돌려준다([ADR-0064](../../docs/adr/0064-payment-reconcile-claim.md)). 계산을 Executor 밖에 두는 이유는 Executor가 트랜잭션 경계만 갖는 얇은 위임이어야 하기 때문이다(`backend/CLAUDE.md`의 "낙관적 락 재시도 배치 규칙" 절). `PaymentReconcileSchedulerService`는 건마다 PG 조회 **전에** 이것을 부르고, 차례를 얻은 결제만 PG에 묻는다(선점 예외면 그 결제를 건너뛰고, 3건 연속이면 주기를 중단한다. Executor의 세 메서드는 모두 `*InNewTx`라 `application-core`의 `ExecutorCallerTransactionRulesTest`가 이 서비스의 `@Transactional`을 금지한다), 대상 조회는 다음 시각이 비었거나 지난 결제만 고른다. 간격 표는 `PaymentReconcileBackoffPolicyTest`가 고정한다([ADR-0062](../../docs/adr/0062-payment-reconcile-backoff.md)).
- 자바 패키지는 코어와 같은 `com.tastyhouse.application.<ctx>..`다(split package). 클래스를 코어와 이 모듈 사이로 옮겨도 import는 바뀌지 않는다.

## 의존

- `api project(':application-core')` — 코어(공유 도메인 서비스·`port.out` 계약·리스너·`shared/**`)를 이 모듈의 소비자에게 함께 노출한다.
- `implementation project(':domain')`, `implementation project(':security-core')`, 그리고 `spring-security-core`·`spring-web`·`jackson-databind`·`spring-tx`.
- **이 모듈을 의존하는 쪽**: `batch-module`(`implementation`), 그리고 위 SPI 포트를 구현하는 벤더 모듈 `infrastructure:bbq`·`infrastructure:admdongkor`.
- **다른 앱 모듈(`web`·`admin`·`ceo-application`)을 의존하지 않는다.** 앱 간 수평 의존을 컴파일러가 막는 것이 이 모듈이 존재하는 이유다.

## 규칙

- **`@Configuration`과 도메인 이벤트 리스너(`@TransactionalEventListener`)를 여기 두지 않는다** — 코어에만 둔다(`backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `listenersAndConfigsShouldResideInCore`). 리스너가 앱 모듈에 있으면 다른 앱이 같은 이벤트를 발행할 때 후속 처리가 조용히 사라진다.
- **빈이면 `@Service`/`@Component`를 단다.** 앱 마커는 없다.
- **이 모듈 전용 에러코드는 `BatchErrorCode`(`backend/batch-application/src/main/java/com/tastyhouse/application/shared/exception/BatchErrorCode.java`)에 둔다; 두 번째 앱이 쓰면 코어 `ApplicationErrorCode`로 올린다.** 예외는 `ApplicationException(BatchErrorCode.X)`로 던진다. 규칙 정본은 `backend/application-core/AGENTS.md`의 봉인·가드 목록 "에러 카탈로그 가드" 항목이다.
- 현재 `BatchErrorCode`는 `ADMIN_DONG_BOUNDARY_FETCH_FAILED` 1건뿐이다.
- **두 번째 앱이 쓰게 되면 코어로 옮긴다.** 이 모듈에는 batch 하나만 쓰는 것만 둔다.
- **batch 전용 SPI 포트를 코어로 옮기지 않는다.** 코어로 가면 web·admin·ceo의 코어 빈이 그 포트를 주입해도 컴파일이 통과하고, 구현이 없는 앱에서 기동 시점에야 실패한다.
- **batch 고유 ArchUnit 규칙은 "이 모듈에서 온 클래스"를 대상으로 한다** — 코어 `application`의 `BatchSchedulerRulesTest`와 `LayerRulesTest#commandRecordsShouldBeBoundaryTyped`(batch 제외)는 과거 `@BatchApp` 마커 대신 testFixtures `com.tastyhouse.architecture.ModuleOrigin.from("batch-application")` 술어로 대상을 고른다. 이 술어는 main 출력(`build/classes/java/main`, main jar)만 인정하므로 아키텍처 테스트는 Gradle로 실행해야 한다(IntelliJ 자체 빌드 출력 `out/production/...`은 예외). 이 모듈의 이름을 바꾸면 그 판정이 함께 바뀐다.
- **이 모듈에는 앱 소속 표식 리소스가 없다.** `batch-module`의 스캔(`ApplicationLayerScanConfig`)은 필터 없이 `com.tastyhouse.application`을 훑으므로, 클래스패스에 다른 앱 모듈이 섞이면 그 앱의 빈이 전부 뜬다("클래스패스 존재 = 활성화"). `batch-module`의 `ApplicationModuleClasspathTest`가 `com.tastyhouse.application` 클래스의 출처 모듈 집합이 `{application, batch-application}`인지를 `ModuleOrigin`으로 판정해 그 사고를 막는다. 과거에는 `src/main/resources/META-INF/tastyhouse/application-module.properties`(`app=batch`) 표식으로 판정했으나 삭제했다.
- 아키텍처 테스트(ArchUnit)는 이 모듈이 아니라 코어 `application`의 테스트에 있다. 코어가 `testImplementation project(':batch-application')`로 이 모듈을 본다.
- 공유 테스트 더블은 코어 testFixtures의 `com.tastyhouse.testsupport.<ctx>..`에 있다(`testImplementation(testFixtures(project(':application-core')))`).
- **UseCase 구현 서비스·협력 서비스(`..service..`의 모든 class)·리스너는 package-private이다**(`public` 금지 — 다른 패키지가 import해야 하는 협력 서비스만 예외로 허용 목록 `PUBLIC_BY_NECESSITY`에 등재). 가드는 코어 `backend/application-core/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`의 `useCaseImplementationsShouldNotBePublic`·`serviceClassesShouldNotBePublic`·`publicByNecessityShouldStillBePublic`·`listenersAndConfigsShouldNotBePublic`이고, 정본은 `backend/application-core/AGENTS.md`의 봉인·가드 목록 "UseCase 구현 서비스·리스너·설정에 `public`을 붙이지 않는다" 항목이다.

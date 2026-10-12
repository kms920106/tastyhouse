# batch-module

배치 앱의 **부트스트랩과 driving adapter**를 전담하는 **독립 실행 모듈**. `web-api`/`admin-api`/`ceo-api`와 동일하게 자체 `main()`(`BatchApplication`)을 가진 bootJar 실행 단위이며, HTTP 요청과 무관하게 `@Scheduled` cron 트리거로만 동작한다.

> **application 계층은 이 모듈에 없다.** 잡 UseCase 인바운드 포트·`*SchedulerService`·`*Executor`·BBQ 크롤링 동기화는 `batch-application` 모듈에, `BatchJobException`은 코어 `application-core`에 있다(규칙 정본은 `application-core/AGENTS.md`). 이 모듈에 남은 것은 `@Scheduled` 트리거 7종과 `BatchApplication`뿐이다. 잡 **로직**을 고치러 왔다면 이 문서가 아니라 `application-core/AGENTS.md`를 본다.

## 신설 배경

기존에는 스케줄러 5개(`RankScheduler`/`ProductScheduler`/`GradeScheduler`+`GradeSchedulerService`/`SearchKeywordScheduler`)가 프레젠테이션 모듈인 `web-api`에 있었다. 웹 서버와 배치 서버를 분리 배포하기 위해 이 모듈로 이동했다(작업지시서 `docs/tasks/06-scheduler-relocation.md` 선택지 B).

## 패키지 구조

```
com.tastyhouse.batch/                  ← 이 모듈 (부트스트랩 + driving adapter)
├── BatchApplication.java          @SpringBootApplication + @EnableScheduling + 중첩 ApplicationLayerScanConfig(@ComponentScan, 필터 없음)·ModuleScanConfig — @Import 없음
└── <job>/adapter/in/scheduler/     @Scheduled 트리거 클래스(로직 없음, UseCase 호출만)
                                    잡 슬러그 7종 — region · grade · product · productsoldout · rank · reviewblind · search

com.tastyhouse.application/       ← application 계층 (batch-application + application-core — split package)
│   (루트에는 클래스가 없다 — application 계층 스캔은 BatchApplication의 중첩 ApplicationLayerScanConfig가 소유한다)
├── <job>/port/in/                 잡 UseCase 인터페이스(batch-application, 입력이 없어 Command record 불필요)
├── <job>/service/                 *SchedulerService(batch-application) implements {Job}UseCase + *Executor/*Runner
├── crawling/bbq/                  BBQ 크롤링 동기화(batch-application)
└── shared/exception/BatchJobException    BbqService가 던지는 배치 실패 예외(application-core)
```

**패키지만으로는 이 서비스가 batch 것인지 알 수 없다** — `com.tastyhouse.application`은 코어와 앱 모듈 4개가 나눠 쓰는 패키지 루트이고, batch 소속은 `batch-application` 모듈에 있다는 사실이 표현한다. 의존 방향은 `batch-module(adapter) → batch-application → application(코어) → domain` 한 방향이다. 트리거는 `..port.in..`의 UseCase 인터페이스만 주입하며, 구체 서비스 주입은 ArchUnit `schedulersShouldDependOnUseCasesOnly`가 막는다. ([ADR-0036](../../docs/adr/0036-app-marker-removal.md))

아래 표에서 **트리거만 이 모듈**에 있고, UseCase·서비스 열은 전부 `batch-application` 소속이다.

| 잡 슬러그 | UseCase (batch-application) | 트리거 (이 모듈) | 비고 (batch-application) |
|---|---|---|---|
| `region` | `SynchronizeAdminDongsUseCase` | `AdminDongScheduler`(매월 1일 04시) | `AdminDongSchedulerService`(다운로드, 트랜잭션 밖) + `AdminDongSyncExecutor`(저장, 트랜잭션) + `AdminDongSyncRunner`(수동 1회 실행, 기본 비활성) |
| `grade` | `SettleMemberGradesUseCase` | `GradeScheduler` | `GradeSchedulerService`가 등급 계산·확정 전담 |
| `product` | `SyncProductOptionsUseCase` | `ProductScheduler`(**비활성 — cron 기본값 `-`, 자동 실행되지 않는다.** 아래 §스케줄러 활성 상태 참조) | `ProductSchedulerService`가 BBQ 옵션 크롤링 저장 |
| `productsoldout` | `ReleaseExpiredSoldOutUseCase` | `ProductSoldOutReleaseScheduler` | `ProductSoldOutReleaseSchedulerService` + `ProductSoldOutReleaseExecutor`(트랜잭션 경계 분리) |
| `rank` | `AggregateRanksUseCase` | `RankScheduler` | `RankSchedulerService`가 랭킹 집계 로직 전담 |
| `reviewblind` | `ExpireBlindedReviewsUseCase` | `ReviewBlindScheduler` | `ReviewBlindSchedulerService` + `ReviewBlindExpirationExecutor`(트랜잭션 경계 분리) |
| `search` | `AggregatePopularKeywordsUseCase` | `SearchKeywordScheduler` | `SearchKeywordSchedulerService`가 인기 검색어 집계 전담 |

## 규칙

- **유스케이스 분리(web·admin·ceo의 `{도메인}CommandService`/`{도메인}QueryService` → 유스케이스당 서비스 1개)에서 batch는 제외했다**: `*SchedulerService` 7개가 이미 잡 UseCase 1개 = 서비스 1개이고, `*SchedulerService` 명명은 `backend/application-core/src/test/java/com/tastyhouse/application/architecture/BatchSchedulerRulesTest.java`가 지킨다(포트명 기반 `useCaseServiceNameShouldMatchPort`와 맞지 않아 1:1 규칙 4종은 batch 출처를 뺀다). `SearchKeywordSchedulerService`가 UseCase 하나에 메서드 2개인 것은 이번 범위 밖이라 손대지 않았다.
- **Scheduler(트리거) + Service(로직) 이분 구조는 모듈 경계와 일치한다**: `adapter/in/scheduler/`의 `@Scheduled` 클래스는 cron 트리거와 try/catch 로깅만 담당하고, 실제 로직은 `batch-application`의 `*SchedulerService`(`@Transactional` 경계 소유)로 위임한다. 이 분리는 규율이 아니라 **빌드 그래프가 강제**한다 — 트리거에 잡 로직 한 줄을 적으려 하면 `batch-module`의 컴파일 클래스패스에 없는 domain 타입을 import해야 해서 컴파일이 깨진다.
- **잡 UseCase 인터페이스(`batch-application`의 `<job>/port/in/`)**: 잡마다 1개(`SettleMemberGradesUseCase`·`AggregateRanksUseCase`·`SyncProductOptionsUseCase`·`ReleaseExpiredSoldOutUseCase`·`ExpireBlindedReviewsUseCase`·`SynchronizeAdminDongsUseCase`·`AggregatePopularKeywordsUseCase`). **배치 잡은 입력이 없으므로 Command record를 두지 않는다** — web/admin/ceo의 `{도메인}CommandUseCase`와 달리 파라미터 없는 메서드 하나만 선언한다. `*SchedulerService`가 이를 implements하고(ArchUnit `application-core`의 `BatchSchedulerRulesTest#schedulerServicesShouldImplementUseCase`), 트리거는 구체 클래스가 아니라 이 인터페이스만 주입한다(이 모듈의 `schedulersShouldDependOnUseCasesOnly`가 강제). 후자는 **모듈 경계를 넘는** 구체 클래스를 막는 규칙이라, 클래스명(`*SchedulerService`)이 아니라 패키지(`com.tastyhouse.application..service..`)로 대상을 잡는다 — 그래야 `*Executor`처럼 이름이 다른 내부 구현까지 함께 막힌다.
- **도메인 모델은 POJO — 명시적 save 필수** (해당 코드는 `batch-application`): 스케줄러 Service에서 도메인을 변경한 뒤 반드시 `repository.save(domain)`을 호출한다(JPA 더티 체킹이 없어 누락 시 변경이 조용히 유실된다).
- **QueryDSL·infra 직접 호출 금지**: 잡 서비스를 대상으로 하는 규칙은 대상 클래스가 있는 `application-core`의 테스트가 갖는다 — `BatchSchedulerRulesTest`(대상 `batch-application` 출처)의 `applicationServicesShouldNotDependOnWebLayer`·`inboundPortsShouldBeBoundaryTyped`·`schedulerServicesShouldImplementUseCase`, 그리고 application 5모듈 전체를 보는 `LayerRulesTest`의 `shouldNotDependOnQuerydsl`·`applicationMustBeServletFree`. 대상 클래스가 이 모듈 밖에 있어, 여기에 두면 대상 0건으로 공허하게 통과하기 때문이다. 이 모듈의 `LayerRulesTest`는 어댑터가 지킬 것 5개(아래 [이 모듈의 규칙이 어댑터 규칙 5개뿐인 이유](#이-모듈의-규칙이-어댑터-규칙-5개뿐인-이유))만 갖는다. 다른 앱의 UseCase·Command는 이 모듈의 클래스패스에 없어 컴파일 게이트가 막는다. 조회는 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스를 주입해 쓴다(reference: `ProductBbqSyncQueryPort#findFirstBbqSyncTarget`). **리포 전체에 `allowEmptyShould(true)`는 0건이며, 새로 도입하지 않는다** — 규칙이 대상을 잃으면 공허 통과를 여는 대신 규칙을 지우거나 anchor를 고친다. ([ADR-0036](../../docs/adr/0036-app-marker-removal.md))
- **cron 표현식은 순수 구조 리팩터링 대상이 아니다**: 스케줄 주기를 바꾸는 변경은 이 모듈이 아니라 별도 운영 결정으로 다룬다.
- **외부 다운로드는 트랜잭션 밖에서 수행한다** (해당 코드는 `batch-application`): 네트워크 구간을 트랜잭션 안에 넣으면 그동안 DB 커넥션이 묶인다. 다운로드 → (트랜잭션) 저장 순으로 나누되, **같은 빈의 메서드를 자기 자신이 호출하면 Spring 프록시를 거치지 않아 `@Transactional`이 적용되지 않으므로**(self-invocation) 저장 구간은 별도 빈(`XxxExecutor`)이 소유한다. reference: `AdminDongSchedulerService`(다운로드) + `AdminDongSyncExecutor`(저장), `ProductSoldOutReleaseExecutor`, `ReviewBlindExpirationExecutor`.
- **마스터 동기화는 삭제·재삽입이 아니라 id 보존 갱신이다**: 다른 테이블이 마스터의 `id`를 참조하고 있으면(행정동의 경우 배달가능지역·지역별 배달팁·주문 스냅샷) 전량 교체 시 그 참조가 **말없이 다른 행을 가리키거나 끊어진다.** 자연키(행정동은 `code`)로 매칭해 제자리 갱신하고, 원천에서 사라진 행은 삭제 대신 `is_active = 0`으로 내린다. reference: `AdminDongSavePort#synchronize`.
- **단일 인스턴스 배포 전제**: `@EnableScheduling` 기반 cron은 인스턴스마다 독립 실행된다. batch-module을 여러 인스턴스로 배포하면 동일 작업이 중복 실행되므로, 운영 시 배치 인스턴스는 1대로 유지한다(분산 락 등 중복 방지 로직은 아직 없음).

## admin-api 비대칭 (의도된 설계)

`admin-api`에는 배치 스케줄러가 없다. `admin-api`는 관리자용 CRUD/조회 API만 제공하며, 시간 기반으로 자동 실행되어야 하는 배치 유스케이스(랭킹 집계, 등급 갱신, 인기 검색어 집계, 상품 옵션 동기화)가 전부 사용자(web) 도메인에 속하기 때문이다. `ceo-api`도 같은 이유로 스케줄러를 갖지 않는다. 배치 책임은 web-api가 아니라 이 `batch-module`로 일원화되어 있다.

## Dependencies

### Internal
- `application` (implementation) — 코어(공유 도메인 서비스·`port.out` 계약·리스너).
- `batch-application` (implementation) — 잡 UseCase 인바운드 포트(트리거가 주입)·`*SchedulerService`·batch 전용 SPI 포트. **다른 앱의 application 모듈을 추가하지 않는다** — 필터 없는 스캔이라 그 앱의 빈이 전부 뜬다(`ApplicationModuleClasspathTest`가 막는다)
- `infrastructure:persistence` (**runtimeOnly**) — DAO 구현체가 뜨는 빈 스캔 대상. `com.tastyhouse.infrastructure..`·`com.querydsl..` 소스 import는 ArchUnit이 전면 차단. 부트스트랩 `ModuleScanConfig`가 패키지 문자열로 스캔하므로 컴파일 타임 참조가 필요 없어 `runtimeOnly`다
- `infrastructure:file-storage` (**runtimeOnly**) — 파일 저장 스타터. 자바 코드 없이 `infrastructure:firebase`(`application`의 포트 `FileStoragePort`(`application.file.port.out`) 구현 — 크롤링 이미지 저장)를 묶어 노출하므로, 이 앱은 **벤더 모듈을 직접 선언하지 않고 이 한 줄만** 갖는다(코어 `infrastructure:restclient`는 스타터가 아니라 아래 `infrastructure:bbq`·`infrastructure:admdongkor`를 통해 전이로 실린다). firebase는 전이로 `runtimeClasspath`에 실려 빈 스캔·설정(`application-file-storage.yml` → `application-firebase.yml`)이 그대로 동작한다
- `infrastructure:bbq` (**runtimeOnly**) — `com.tastyhouse.infrastructure.bbq.BbqApiClient`(BBQ 메뉴 HTTP 클라이언트)·`com.tastyhouse.infrastructure.bbq.RemoteImageDownloader`. 설정은 `application-bbq.yml`
- `infrastructure:admdongkor` (**runtimeOnly**) — 행정동 경계 클라이언트 `com.tastyhouse.infrastructure.admdongkor.AdminDongBoundaryClient`. 설정은 `application-admdongkor.yml`
- 위 두 모듈은 2026-09-26에 옛 `infrastructure:crawling` 한 모듈을 나눈 것이다(근거는 `../infrastructure/admdongkor/AGENTS.md`). **소스 참조는 `batch-application`에 있고**, 두 모듈은 빈 스캔·설정 때문에 유지한다
- `logging-module` (**runtimeOnly**) — **p6spy를 `exclude`한다**: `logging-module`이 그것을 `api`로 노출하지만 batch는 HTTP 요청이 없어 쓰지 않으므로, 전이 의존을 끊어 datasource 자동 데코레이션(SQL 로그 신규 발생)을 막는다. `runtimeOnly`에 걸린 `exclude`도 동일하게 적용된다(Gradle의 `exclude`는 의존 스코프와 무관하게 동작)
- **`domain`은 선언하지 않는다** — 이 모듈 소스에 `com.tastyhouse.domain..` 참조가 0건이다. 전이 경로도 없다(`application`이 `domain`을 `api`가 아닌 `implementation`으로 물고 있고, 이 모듈은 `api-common-module`을 의존하지 않는다). web/admin/ceo도 같은 상태다(`api-common-module`이 `api project(':application-core')`이라 전이 경로가 없다). 도메인 타입이 필요해 보이면 선언하지 말고 그 판단을 application으로 옮긴다 — presentation 전체가 domain을 컴파일 클래스패스에 두지 않는 것이 현재 규칙이다
- `testFixtures(project(':application-core'))` — `ApplicationLayerScanAssertions`(스캔 단정·클래스패스 단정)를 application 모듈과 공유한다. **복제하면 두 벌이 갈라지므로** test fixture로 받는다. 다른 앱의 UseCase·Command는 이 모듈의 클래스패스에 없어 컴파일 에러다.

### External
- Spring Boot Starter(루트 `subprojects`가 부여) — `@Scheduled`/`@Transactional` 지원

## 빈 배선 — 부트스트랩 중첩 스캔 설정

**`BatchApplication`에는 `@Import`가 하나도 없다.** 배선은 `BatchApplication`의 두 번째 중첩 클래스 `ModuleScanConfig`(`@ComponentScan(basePackages = {"com.tastyhouse.infrastructure", "com.tastyhouse.logging"})`)가 한다 — 클래스패스에 있는 persistence·file-storage(firebase)·bbq·admdongkor·logging 빈이 이 문자열 스캔으로 등록되고, `FirebaseModuleConfig`·`BbqModuleConfig`·`AdmdongkorModuleConfig`가 각자 `@EnableConfigurationProperties`만 갖는다. imports 파일은 0개다. batch **앱 자신의 정체성**(`@ComponentScan`, 필터 없음)은 `BatchApplication`의 static 중첩 `ApplicationLayerScanConfig`가 소유한다. `application` 모듈은 설정 클래스도 스캔 선언도 갖지 않는다 — 위 `backend/CLAUDE.md`의 모듈 등록 컨벤션 참고. **왜 중첩 클래스인가**: Spring Framework 6.1.5는 설정 클래스에 직접 붙은 `@ComponentScan`이 있으면 `@SpringBootApplication`의 메타 `@ComponentScan`을 무시해 앱 기본 스캔이 사라지므로 직접 선언은 금지이고, 중첩 `@Configuration`은 별도 설정 클래스로 처리돼 안전하다. 부작용으로 application 클래스패스 스캔이 기동 시 2회 돌지만 빈 집합은 같다. ([ADR-0038](../../docs/adr/0038-autoconfiguration-imports-removal.md))

**batch가 non-servlet인 이유는 이 문서가 유일하게 담보한다** — 아래 절.

### `web-application-type: none`은 **재유입 방어선**이다

이 모듈의 runtimeClasspath에는 `api-common-module`도, `infrastructure:redis`도, springdoc도, 서블릿 스택도 **없다** — `security-core`는 토큰 저장소 포트만 갖고 Redis 구현을 의존하지 않으므로 그것들을 batch로 끌고 오는 전이 사슬이 없다([ADR-0019](../../docs/adr/0019-token-store-port-inversion.md)). 발화할 대상이 아예 없으므로 조건이 잠재울 것도 없다.

`application.yml`의 `spring.main.web-application-type: none`은 그래서 **일차 방어선이 아니라 재유입 방어선**이다. 일차 방어선은 **빌드 그래프**다 — 어떤 모듈도 `api-common-module`을 batch로 끌고 오지 않는다.

**두 방어선의 역할이 다르다.** 누군가 `api-common-module`(또는 그것을 `api`로 노출하는 모듈)을 이 모듈의 의존에 다시 추가하면 서블릿 스택이 클래스패스로 돌아오고, 그 순간 `@ConditionalOnWebApplication(type = SERVLET)`이 다시 실질적인 판정을 하게 된다. 그때 이 yml 한 줄이 없으면 rate limit aspect(`rateLimitAspect`)가 조용히 올라온다. 그보다 앞선 방어선이 하나 더 있다 — batch `ModuleScanConfig`가 `com.tastyhouse.apicommon`을 **스캔하지 않으므로**, 의존이 되돌아와도 공용 예외 핸들러(빈 이름 `globalExceptionHandler`, 조건 없음)와 `ApiCommonRateLimitConfig`는 스캔 목록에 그 패키지를 넣기 전에는 등록되지 않는다. yml 한 줄은 `ApiCommonRateLimitConfig`·`SecurityModuleConfig`의 SERVLET 조건을 위한 3중 방어선으로 남긴다. **따라서 이 줄을 "이제 불필요하다"며 지우지 않는다** — 지금 무해한 이유는 그 줄이 필요 없어서가 아니라 방어 대상이 일시적으로 부재해서다.

확인 방법은 그대로다: 배선을 건드렸으면 `java -jar --debug` 기동의 `CONDITIONS EVALUATION REPORT`를 본다. 두 설정이 Negative로 **평가되는 것이 아니라 보고서에 아예 등장하지 않는 것**이 정상이다(클래스패스에 없고 스캔 대상도 아니므로 후보에도 오르지 않는다 — imports 후보 목록에도 `com.tastyhouse.` 항목이 없다).

외부 수집(`BbqModuleConfig`·`AdmdongkorModuleConfig`)은 이 모듈이 실제로 쓰는 의도된 등록이다. **Redis(`RedisModuleConfig`)는 이 모듈에서 등록되지 않는다** — `infrastructure:redis`가 클래스패스에 없다.

> **이 모듈에는 `contextLoads` 테스트가 없다.** web/admin/ceo와 달리 `BatchApplicationTests`가 없어서, 스캔 설정(`ApplicationLayerScanConfig`)이 빠지거나 틀려도 **빌드는 green이고 jar만 조용히 깨진다**(빈을 못 찾아 부팅 실패. 스캔 설정 오기입은 `ApplicationLayerScanConfigTest`가, 다른 앱 모듈 유입은 `ApplicationModuleClasspathTest`가 잡는다). 배선을 건드렸으면 빌드만 믿지 말고 실제로 띄워 `Started BatchApplication` 마커를 확인한다.
>
> ```bash
> pkill -f 'batch-module-.*\.jar'
> cd backend && ./gradlew :batch-module:build
> nohup java -jar batch-module/build/libs/batch-module-0.0.1-SNAPSHOT.jar > /tmp/batch.log 2>&1 &
> grep 'Started BatchApplication' /tmp/batch.log
> ```

## 설정 파일

`src/main/resources/application.yml`이 `application-persistence.yml`(DB/JPA, `infrastructure:persistence` 소유)과 외부 연동 설정 세 벌 — `application-file-storage.yml`(파일 저장 스타터 `infrastructure:file-storage` 소유. `file.provider`를 갖고 벤더 yml `application-firebase.yml`을 중첩 import한다)·`application-bbq.yml`(BBQ 메뉴 수집, `infrastructure:bbq` 소유)·`application-admdongkor.yml`(행정동 경계 수집, `infrastructure:admdongkor` 소유) — 을 `classpath:` import한다 — web-api와 동일한 패턴. 웹 전용 설정(서버 포트/CORS/JWT/OAuth/Redis/multipart)은 없다.

**DB 커넥션 풀은 다른 앱과 같은 `infrastructure:mysql`의 `application-mysql.yml`을 받는다**(persistence yml이 import). batch는 기본값(`DB_POOL_MAX_SIZE` 5, `DB_SOCKET_TIMEOUT_MS` 0 = 무제한)을 그대로 쓴다. 행정동 경계·BBQ 메뉴 적재처럼 오래 걸리는 쿼리가 있으므로 **`DB_SOCKET_TIMEOUT_MS`를 켜지 않는다** — 켜면 그 시간을 넘는 쿼리가 끊긴다. 이 앱 `application.yml`에 `spring.datasource.hikari.*`를 적어도 import된 mysql yml이 이기므로 반영되지 않는다. 조정은 기동 시 환경변수로 한다(`../infrastructure/mysql/AGENTS.md`의 "앱별 조정").

## 스케줄러 활성 상태 (트리거 7종 중 1종이 비활성)

**이 절은 각 트리거가 운영에서 실제로 도는지를 한눈에 보여 주는 표다.** `ProductScheduler`도 `@Scheduled`를 갖되 cron 기본값 `-`(비활성)로 꺼져 있어, 코드가 비활성 상태를 스스로 드러낸다.

| 트리거 | 상태 | 운영 cron |
|---|---|---|
| `AdminDongScheduler` | 활성 | `0 0 4 1 * *` — 매월 1일 04시 |
| `GradeScheduler` | 활성 | `0 30 3 * * *` — 매일 새벽 3시 30분 |
| `ProductScheduler` | **비활성** | `${product.option-crawl.cron:-}` — 기본값 `-`(비활성 cron), 프로퍼티를 주면 활성 |
| `ProductSoldOutReleaseScheduler` | 활성 | `${product.sold-out-release.cron:0 */10 * * * *}` — 기본 10분 주기(프로퍼티로 조정 가능) |
| `RankScheduler` | 활성 | `0 0 3 * * *` — 매일 새벽 3시 |
| `ReviewBlindScheduler` | 활성 | `0 0 4 * * *` — 매일 새벽 4시 |
| `SearchKeywordScheduler` | 활성 | `0 0 3 * * *` + `0 30 3 * * *` — 트리거 메서드 2개 |

### `ProductScheduler`는 비활성이다 — 자동 실행되지 않는다

**대상**: `backend/batch-module/src/main/java/com/tastyhouse/batch/product/adapter/in/scheduler/ProductScheduler.java`
→ `crawlAndSaveProductOptions()`

`crawlAndSaveProductOptions()`(BBQ 상품 옵션 크롤링 저장)는 배치 앱이 떠 있어도 **자동으로 실행되지 않는다.** 트리거는 `@Scheduled(cron = "${product.option-crawl.cron:-}")`이고, 프로퍼티를 주지 않으면 기본값 `-`가 쓰이는데 이것은 Spring의 **비활성 cron**(`Scheduled.CRON_DISABLED`)이라 스케줄이 등록되지 않는다.

| 항목 | before | after |
|---|---|---|
| 비활성 표현 | `@Scheduled` 없음 + 클래스·메서드에 `@SuppressWarnings("unused")` 2개 | `@Scheduled(cron = "${product.option-crawl.cron:-}")` — 기본값 `-`로 비활성 |
| 동작 | 자동 실행 안 됨 | **동일** — 자동 실행 안 됨 |
| 켜는 방법 | 코드에 `@Scheduled(fixedDelay = 10000)`과 import를 되살리고 억제 2개 제거 | `product.option-crawl.cron` 프로퍼티에 cron 식을 준다(코드 변경 없음) |

억제 어노테이션을 쓰지 않게 된 이유는 `backend/CLAUDE.md`의 "`@SuppressWarnings` 지양 규칙"이다. 과거 주석으로 보존돼 있던 주기는 10초 고정 지연(`fixedDelay = 10000`)이었다 — cron으로 켜려면 `*/10 * * * * *`에 해당한다. 크롤링 대상이 남의 서비스이므로(`../infrastructure/bbq/AGENTS.md`), 켜기 전에 그 주기가 상대 서비스에 과한 부하인지부터 판단한다.

### `GradeScheduler`·`RankScheduler`는 활성이다 — "비활성"으로 오해하지 말 것

**대상**: `backend/batch-module/src/main/java/com/tastyhouse/batch/{grade,rank}/adapter/in/scheduler/{GradeScheduler,RankScheduler}.java`
→ `settleMemberGrades()` · 랭킹 집계 트리거

두 트리거에는 운영 cron이 **살아 있다** — `GradeScheduler`는 `@Scheduled(cron = "0 30 3 * * *")`(매일 새벽 3시 30분, 랭킹 집계 이후에 도는 순서), `RankScheduler`는 `@Scheduled(cron = "0 0 3 * * *")`(매일 새벽 3시)다.

과거 이 두 파일에는 `//    @Scheduled(cron = "0 * * * * *") // 1분마다 실행 (테스트용)`이 각각 한 줄씩 주석으로 남아 있었다. **그것은 비활성 상태를 나타내는 정보가 아니라 버려진 테스트용 cron 변형**(1분 주기)이며, 되살릴 대상이 아니다. 운영에서 1분마다 등급 정산·랭킹 집계를 돌리면 안 된다. 챕터 07에서 그 두 줄을 삭제했고, 이 문단이 "삭제된 것이 무엇이었는지"의 기록이다.

`GradeScheduler`의 등급 산정 규칙(리뷰 개수 기준)은 `docs/domain/`의 등급 문서가 소유하며, 이 트리거는 시각만 정한다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->


### `ApplicationModuleClasspathTest` — 다른 앱의 application 모듈이 클래스패스에 섞이지 않게 막는다 (앱 마커 제거)

**대상**: `backend/batch-module/src/test/java/com/tastyhouse/batch/ApplicationModuleClasspathTest.java` → `loadsOnlyOwnApplicationModule`
· 단정 본문 `backend/application-core/src/testFixtures/java/com/tastyhouse/architecture/ApplicationLayerScanAssertions.java` → `assertLoadsOnlyOwnApplicationModule(ModuleOrigin.BATCH)`
· 출처 판정 `backend/application-core/src/testFixtures/java/com/tastyhouse/architecture/ModuleOrigin.java` → `of(JavaClass)`

**무엇을 확인하나**: 이 앱의 테스트 클래스패스에서 `com.tastyhouse.application` 패키지 클래스(main 출력만)를 ArchUnit으로 읽고, 각 클래스의 출처 모듈(`ModuleOrigin.of`)을 모은 집합이 **정확히 `{application-core, batch-application}`**인지 본다. 클래스가 어느 모듈의 jar·클래스 디렉터리에서 왔는지로 판정하므로 모듈에 별도 표식 파일을 두지 않는다. `ModuleOrigin`은 Gradle 출력만 인정하므로 이 테스트는 Gradle로 실행한다(IntelliJ 자체 빌드 출력 `out/production`이면 예외로 실패).

**왜 막는가**: `ApplicationLayerScanConfig`는 **필터 없이** `com.tastyhouse.application`을 스캔한다. 무엇이 뜰지는 클래스패스가 정한다("클래스패스 존재 = 활성화"). 그래서 누가 이 모듈 `build.gradle`에 다른 앱의 application 모듈(예: `:web-application`)을 실수로 추가하면, 컴파일도 기동도 성공한 채 **그 앱의 빈·권한 경계가 이 앱에 통째로 실린다.** 이 테스트가 그것을 막는다. 집합에 `batch-application`이 없으면 자기 앱 모듈이 빠진 것이고, 다른 `*-application`이 있으면 다른 앱 모듈이 섞인 것이다.

| 항목 | 현재 |
|---|---|
| 다른 앱 빈이 뜨는 것을 막는 수단 | 클래스패스에 다른 앱 모듈이 없음 + 이 테스트 |
| application 계층 의존 | `implementation project(':application-core')` + `implementation project(':batch-application')` |

### `ApplicationLayerScanConfigTest` — 부트스트랩 스캔 설정의 오기입·직접 선언을 막는다

**대상**: `backend/batch-module/src/test/java/com/tastyhouse/batch/ApplicationLayerScanConfigTest.java`
→ `BatchApplication`의 static 중첩 클래스 `ApplicationLayerScanConfig`·`ModuleScanConfig`(`backend/batch-module/src/main/java/com/tastyhouse/batch/BatchApplication.java`)의 `@ComponentScan`
· 단정 본문 `backend/application-core/src/testFixtures/java/com/tastyhouse/architecture/ApplicationLayerScanAssertions.java`

**무엇을 확인하나**: 테스트 4건이 fixture의 단정 메서드를 하나씩 부른다. 앱 테스트에는 앱마다 다른 값(부트스트랩 클래스·스캔 패키지 목록)만 두고, **단정을 바꿀 때는 fixture 하나만 고친다** — 4개 앱이 같은 단정을 복사해 두면 한 앱만 고쳐져 가드가 갈라지므로 `testFixtures(project(':application-core'))`로 공유한다.

| 테스트 | 호출 | 단정 |
|---|---|---|
| `scansApplicationLayerWithoutFilters` | `assertScansApplicationLayerWithoutFilters(BatchApplication.class)` | 부트스트랩의 중첩 클래스 중 `@ComponentScan`/`@ComponentScans`를 가진 것이 정확히 `ApplicationLayerScanConfig`·`ModuleScanConfig` 2개 · `ApplicationLayerScanConfig`는 static `@Configuration`이고 `basePackages == {"com.tastyhouse.application"}`, `value`·`basePackageClasses` 비어 있음, `useDefaultFilters == true`, include·exclude 필터 없음 |
| `bootstrapShouldNotDeclareScanOrImportDirectly` | `assertBootstrapDoesNotDeclareScanOrImport(BatchApplication.class)` | 부트스트랩 자체에 직접 선언된 `@ComponentScan`·`@ComponentScans`·`@Import`가 없음 · `@SpringBootApplication`의 `scanBasePackages`·`scanBasePackageClasses`가 비어 있음 |
| `scansLibraryModulesByPackageName` | `assertScansModulesWithoutFilters(BatchApplication.class, …)` | `ModuleScanConfig`(static `@Configuration`)의 `basePackages`가 정확히 `infrastructure`·`logging`(전부 `com.tastyhouse.` 접두, 순서 포함)이고 필터 없음 · 각 패키지에서 컴포넌트가 1개 이상 발견됨 |
| `libraryModulesShouldNotSelfRegisterAsAutoConfiguration` | `assertNoTastyhouseAutoConfiguration()` | Boot `ImportCandidates`의 `AutoConfiguration.imports` 후보에 `com.tastyhouse.` 항목이 없음(imports 파일 재유입 차단) |

중첩 클래스는 단순명으로 찾으므로 이름을 바꾸거나 지우면 "must declare nested ApplicationLayerScanConfig"(또는 `ModuleScanConfig`)로 실패한다. 단정은 순수 자바 리플렉션 `getDeclaredAnnotation`으로 읽는다 — Spring `MergedAnnotations`는 `@SpringBootApplication` → `@EnableAutoConfiguration` → `@Import`를 따라가 "`@Import` 없음" 단정이 항상 실패하므로 쓰지 않는다. **스캔 목록을 바꾸면 이 테스트의 기대 목록도 함께 고친다.**

**왜 막는가**: application 계층 스캔에 필터가 생기거나 `basePackages`가 바뀌면 자기 앱 빈이 조용히 빠지거나 엉뚱한 패키지가 섞이고, `ModuleScanConfig`의 패키지 이름 하나가 틀리면 그 라이브러리 모듈의 빈이 예외 없이 사라진다. 어느 쪽도 컴파일은 통과한다. 다른 앱 모듈이 클래스패스에 섞이는 사고는 위 `ApplicationModuleClasspathTest`가 막는다.

**왜 중첩 클래스인가**: Spring Framework 6.1.5 `ConfigurationClassParser`는 직접 붙은 `@ComponentScan`을 먼저 모으고 하나라도 있으면 메타 애노테이션(`@SpringBootApplication` 안의 스캔)을 무시한다. `@SpringBootApplication` 클래스에 직접 달면 앱 자기 패키지 스캔과 Boot의 `TypeExcludeFilter`·`AutoConfigurationExcludeFilter`가 조용히 사라지므로 금지다(위 마지막 단정이 이를 막는다). 중첩 `@Configuration`은 별도 설정 클래스로 처리돼 안전하며, 부작용으로 application 클래스패스 스캔이 기동 시 2회 돌지만 빈 집합은 같다.

### `ProductScheduler`의 cron 기본값 `-`를 지우지 않는다

**대상**: `backend/batch-module/src/main/java/com/tastyhouse/batch/product/adapter/in/scheduler/ProductScheduler.java`
→ `crawlAndSaveProductOptions()`의 `@Scheduled(cron = "${product.option-crawl.cron:-}")`

기본값 `-`가 비활성 상태를 표현하는 유일한 코드상 흔적이다. `${product.option-crawl.cron}`처럼 기본값을 지우면 프로퍼티가 없는 환경에서 **부팅이 실패**하고, 다른 cron 기본값을 넣으면 **남의 서비스를 크롤링하는 작업이 모든 환경에서 켜진다.** 과거 이 자리의 봉인 대상이던 `@SuppressWarnings("unused")` 2개는 이 cron 트리거로 대체돼 삭제됐다.

### `allowEmptyShould(true)`를 쓰지 않는다 — 규칙이 대상을 잃으면 지우거나 anchor를 고친다

**대상**: `backend/batch-module/src/test/java/com/tastyhouse/batch/architecture/LayerRulesTest.java`
→ 클래스 전체

규칙이 대상을 잃으면 **공허 통과를 여는 대신 규칙을 지우거나 anchor를 고친다.** 잡 서비스를 검사하는 규칙(`applicationServicesShouldNotDependOnWebLayer`·`schedulerServicesShouldImplementUseCase` 등)이 이 모듈이 아니라 `application-core`의 `BatchSchedulerRulesTest`에 있는 것이 그 적용이다 — 대상 클래스가 이 모듈에 없으므로 여기에 두면 공허하게 통과한다. `useCaseFieldsShouldBeNamedUseCase`는 검사한 UseCase 필드가 5개 미만이면 실패하는 하한을 스스로 갖는다.

### `schedulersShouldDependOnUseCasesOnly`의 대상은 **클래스명이 아니라 패키지**다

**대상**: `backend/batch-module/src/test/java/com/tastyhouse/batch/architecture/LayerRulesTest.java`
→ `schedulersShouldDependOnUseCasesOnly`

잡 서비스가 `batch-application`에 있으므로, 이 규칙은 "모듈 안의 구체 클래스"가 아니라 **모듈 경계를 넘는 구체 클래스**를 막는다. 그래서 클래스 이름(`*SchedulerService`)이 아니라 **패키지**(`com.tastyhouse.application..service..`)로 대상을 잡는다 — 이렇게 해야 `*Executor`(예: `ProductSoldOutReleaseExecutor`)처럼 `SchedulerService`로 끝나지 않는 내부 구현까지 함께 막힌다. **이름 기준으로 되돌리지 않는다.** 정방향인 `..port.in..`의 UseCase 인터페이스 주입은 이 규칙에 걸리지 않으며, 실존 스케줄러 7종에 anchor하므로 공허하지 않다.

### 스케줄러에 `public`을 붙이지 않는다 (package-private 적용)

**대상**: `backend/batch-module/src/test/java/com/tastyhouse/batch/architecture/LayerRulesTest.java` → `schedulersShouldNotBePublic`

`*Scheduler` 7개를 package-private으로 좁혔다. 스캔으로만 등록되고 각자 `batch-application`의 UseCase(`*SchedulerService`, 역시 package-private)를 인터페이스로 주입받으므로 이름이 패키지 밖에 나타날 필요가 없다. `@Scheduled` 메서드의 가시성은 손대지 않는다. 근거와 전체 범주는 `backend/CLAUDE.md`의 "접근 제어자 규칙 (내부 구현은 package-private)" 절.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### 트리거는 로깅·예외격리만 담당한다 (전 스케줄러 공통형)

**대상**: `backend/batch-module/src/main/java/com/tastyhouse/batch/{productsoldout,reviewblind}/adapter/in/scheduler/`
→ `ProductSoldOutReleaseScheduler` · `ReviewBlindScheduler`

두 트리거의 클래스 주석이 명시하던 것으로, 이 모듈 트리거 7종 전부의 공통형이다 — **스케줄러는 로깅·예외격리만** 담당하고 잡 본문은 `application`의 UseCase(`ReleaseExpiredSoldOutUseCase`·`ExpireBlindedReviewsUseCase` 등)에 둔다. `RankScheduler`가 그 최초 형태이고 나머지가 그 패턴을 그대로 따랐다.

### 잡 주기가 다른 이유 (cron 값의 근거)

**대상**: `backend/batch-module/src/main/java/com/tastyhouse/batch/{productsoldout,reviewblind,region}/adapter/in/scheduler/`
→ `ProductSoldOutReleaseScheduler` · `ReviewBlindScheduler` · `AdminDongScheduler`

세 트리거의 메서드·클래스 주석이 담고 있던 판단 근거다. **cron 값을 바꾸기 전에 이 근거를 확인한다.**

- **`ProductSoldOutReleaseScheduler` — 10분 주기.** 다른 배치가 하루 1회 새벽에 도는 것과 성격이 다르다. **"익일 가게 오픈 시간까지 품절"이 오픈 직후에 풀려야** 의미가 있고, 품절 기간 입력 단위가 10분이라 그보다 촘촘하게 돌 필요가 없다. 하루 1회로 두면 오전에 오픈한 가게가 다음 날 새벽까지 품절로 남는다.
- **`ReviewBlindScheduler` — 매일 새벽 4시.** 랭킹 집계(3시)와 시간대를 분리해 두 잡이 겹치지 않게 한다.
- **`AdminDongScheduler` — 매월 1일 04시.** 행정구역 개편은 연 몇 회 수준이라 잦은 실행이 의미 없고, **원천도 그 주기로만 갱신된다**(`../infrastructure/admdongkor/AGENTS.md` §수집 방식). 매월 1일 새벽에 한 번만 돌려 개편을 뒤늦게라도 따라잡게 하며, 다른 배치와 겹치지 않는 04시대를 쓴다.

### 이 모듈의 규칙이 어댑터 규칙 5개뿐인 이유

**대상**: `backend/batch-module/src/test/java/com/tastyhouse/batch/architecture/LayerRulesTest.java`
→ 클래스 전체

이 모듈에 있는 것은 **driving adapter**(`<job>/adapter/in/scheduler`의 `@Scheduled` 트리거 7종)와 부트스트랩(`BatchApplication`)뿐이다. 그래서 규칙도 "어댑터가 지켜야 할 것"만 둔다 — `@Test` 5개다.

| 규칙 | 지키는 것 |
|---|---|
| `appModuleShouldNotEnableAsync` | `@EnableAsync`를 앱 모듈에 두지 않는다 — 코어 `application-core`의 `AsyncConfig`가 4앱 공통으로 소유한다([ADR-0048](../../docs/adr/0048-async-config-core.md)) |
| `shouldNotDependOnInfrastructurePersistence` | `com.tastyhouse.infrastructure.jpa..` 의존 금지(아래 문단) |
| `schedulersShouldDependOnUseCasesOnly` | `*Scheduler`는 `com.tastyhouse.application..service..` 구체 클래스를 의존하지 않는다(위 절) |
| `useCaseFieldsShouldBeNamedUseCase` | `port.in` 인터페이스 타입 필드는 이름이 `UseCase`로 끝난다(검사 대상 5개 이상 하한) |
| `schedulersShouldNotBePublic` | 최상위 `*Scheduler`는 public이 아니다(위 절) |

`shouldNotDependOnInfrastructurePersistence`는 **스케줄러가 잡 UseCase만 주입하므로 현재 위반이 없고**, 트리거가 `JpaRepository`를 직접 주입해 "잡 로직 한 줄"을 어댑터에 적는 회귀를 막는다. 대상은 이 모듈의 전 클래스라 공허하지 않다.

<!-- MANUAL: -->

# batch-module

배치 앱의 **부트스트랩과 driving adapter**를 전담하는 **독립 실행 모듈**. `web-api`/`admin-api`/`ceo-api`와 동일하게 자체 `main()`(`BatchApplication`)을 가진 bootJar 실행 단위이며, HTTP 요청과 무관하게 `@Scheduled` cron 트리거로만 동작한다.

> **챕터 01로 application 계층이 떠났다.** 잡 UseCase 인바운드 포트·`*SchedulerService`·`*Executor`·BBQ 크롤링 동기화는 이제 `application` 모듈에 있다(`application/AGENTS.md`). 이 모듈에 남은 것은 `@Scheduled` 트리거 7종과 `BatchApplication`뿐이다. 잡 **로직**을 고치러 왔다면 이 문서가 아니라 `application/AGENTS.md`를 본다.

## 신설 배경

기존에는 스케줄러 5개(`RankScheduler`/`ProductScheduler`/`GradeScheduler`+`GradeSchedulerService`/`SearchKeywordScheduler`)가 프레젠테이션 모듈인 `web-api`에 있었다. 웹 서버와 배치 서버를 분리 배포하기 위해 이 모듈로 이동했다(작업지시서 `docs/tasks/06-scheduler-relocation.md` 선택지 B).

## 패키지 구조 (개정됨 — 챕터 01 application 계층 물리 분리)

```
com.tastyhouse.batch/                  ← 이 모듈 (부트스트랩 + driving adapter)
├── BatchApplication.java          @SpringBootApplication + @EnableScheduling + @Import(BatchApplicationConfig)
└── <job>/adapter/in/scheduler/     @Scheduled 트리거 클래스(로직 없음, UseCase 호출만)
                                    잡 슬러그 7종 — region · grade · product · productsoldout · rank · reviewblind · search

com.tastyhouse.batchapplication/       ← application 모듈 모듈 (application 계층)
├── BatchApplicationConfig.java    @ComponentScan 진입점 — 이 모듈을 쓰는 앱이 @Import 한다
├── <job>/port/in/                 잡 UseCase 인터페이스(입력이 없어 Command record 불필요)
├── <job>/service/                 *SchedulerService implements {Job}UseCase + *Executor/*Runner
├── crawling/bbq/                  BBQ 크롤링 동기화(application 서비스 — 아래 "왜 함께 옮겼나" 참고)
└── exception/BatchJobException    BbqService만 던지는 예외라 함께 이동
```

의존 방향은 `batch-module(adapter) → application 모듈(application) → domain` 한 방향이다. 트리거는 `..port.in..`의 UseCase 인터페이스만 주입하며, 구체 서비스 주입은 ArchUnit `schedulersShouldDependOnUseCasesOnly`가 막는다.

아래 표에서 **트리거만 이 모듈**에 있고, UseCase·서비스 열은 전부 `application` 소속이다.

| 잡 슬러그 | UseCase (application 모듈) | 트리거 (이 모듈) | 비고 (application 모듈) |
|---|---|---|---|
| `region` | `SynchronizeAdminDongsUseCase` | `AdminDongScheduler`(매월 1일 04시) | `AdminDongSchedulerService`(다운로드, 트랜잭션 밖) + `AdminDongSyncExecutor`(저장, 트랜잭션) + `AdminDongSyncRunner`(수동 1회 실행, 기본 비활성) |
| `grade` | `SettleMemberGradesUseCase` | `GradeScheduler` | `GradeSchedulerService`가 등급 계산·확정 전담 |
| `product` | `SyncProductOptionsUseCase` | `ProductScheduler`(`@Scheduled` 주석 처리된 비활성 상태 유지) | `ProductSchedulerService`가 BBQ 옵션 크롤링 저장 |
| `productsoldout` | `ReleaseExpiredSoldOutUseCase` | `ProductSoldOutReleaseScheduler` | `ProductSoldOutReleaseSchedulerService` + `ProductSoldOutReleaseExecutor`(트랜잭션 경계 분리) |
| `rank` | `AggregateRanksUseCase` | `RankScheduler` | `RankSchedulerService`가 랭킹 집계 로직 전담 |
| `reviewblind` | `ExpireBlindedReviewsUseCase` | `ReviewBlindScheduler` | `ReviewBlindSchedulerService` + `ReviewBlindExpirationExecutor`(트랜잭션 경계 분리) |
| `search` | `AggregatePopularKeywordsUseCase` | `SearchKeywordScheduler` | `SearchKeywordSchedulerService`가 인기 검색어 집계 전담 |

## 규칙

- **Scheduler(트리거) + Service(로직) 이분 구조는 이제 모듈 경계와 일치한다 (개정)**: `adapter/in/scheduler/`의 `@Scheduled` 클래스는 cron 트리거와 try/catch 로깅만 담당하고, 실제 로직은 `application`의 `*SchedulerService`(`@Transactional` 경계 소유)로 위임한다. 챕터 01 전에는 두 계층이 같은 모듈 안 다른 패키지였을 뿐이라 규율로만 유지됐으나, 이제 **빌드 그래프가 강제**한다 — 트리거에 잡 로직 한 줄을 적으려 하면 `batch-module`에 없는 domain write 포트·`{Ctx}QueryPort`를 import해야 해서 컴파일이 깨진다.
- **잡 UseCase 인터페이스(`application`의 `<job>/port/in/`)**: 잡마다 1개(`SettleMemberGradesUseCase`·`AggregateRanksUseCase`·`SyncProductOptionsUseCase`·`ReleaseExpiredSoldOutUseCase`·`ExpireBlindedReviewsUseCase`·`SynchronizeAdminDongsUseCase`·`AggregatePopularKeywordsUseCase`). **배치 잡은 입력이 없으므로 Command record를 두지 않는다** — web/admin/ceo의 `{도메인}CommandUseCase`와 달리 파라미터 없는 메서드 하나만 선언한다. `*SchedulerService`가 이를 implements하고(ArchUnit `schedulerServicesShouldImplementUseCase` — application 모듈 소유), 트리거는 구체 클래스가 아니라 이 인터페이스만 주입한다(이 모듈의 `schedulersShouldDependOnUseCasesOnly`가 강제). 후자는 챕터 01로 **모듈 경계를 넘는** 구체 클래스를 막는 규칙이 되어, 클래스명(`*SchedulerService`)이 아니라 패키지(`com.tastyhouse.batchapplication..service..`)로 대상을 잡는다 — 그래야 `*Executor`처럼 이름이 다른 내부 구현까지 함께 막힌다.
- **도메인 모델은 POJO — 명시적 save 필수** (해당 코드는 `application`): 스케줄러 Service에서 도메인을 변경한 뒤 반드시 `repository.save(domain)`을 호출한다(JPA 더티 체킹이 없어 누락 시 변경이 조용히 유실된다).
- **QueryDSL·infra 직접 호출 금지 (개정 — 규칙 대부분이 application 모듈로 이동)**: 잡 서비스를 대상으로 하던 규칙들(`applicationServicesShouldNotDependOnWebLayer`·`shouldNotDependOnQuerydsl`·response record 규칙·`schedulerServicesShouldImplementUseCase`)은 대상 클래스가 전부 이 모듈을 떠났으므로 **여기서 삭제하고 application 모듈의 `BatchSchedulerRulesTest`로 옮겼다** — 남겨 두면 대상 0건으로 공허하게 통과한다. 이 모듈에 남은 규칙은 어댑터가 지킬 것 3개(`shouldNotDependOnInfrastructurePersistence`·`schedulersShouldDependOnUseCasesOnly`·`adaptersShouldOnlyUseOwnAppUseCases`)뿐이다 — 마지막 하나는 application 모듈 통합(챕터 01)으로 4개 앱 패키지가 모두 컴파일 클래스패스에 들어오면서 사라진 게이트를 대체하는 신설 규칙이다. 조회는 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스를 주입해 쓴다(reference: `ProductQueryPort#findFirstBbqSyncTarget`). **리포 전체에 `allowEmptyShould(true)`는 여전히 0건이며, 새로 도입하지 않는다** — 규칙이 대상을 잃으면 공허 통과를 여는 대신 규칙을 지우거나 anchor를 고친다(이번 이동이 그 선례다).
- **cron 표현식은 순수 구조 리팩터링 대상이 아니다**: 스케줄 주기를 바꾸는 변경은 이 모듈이 아니라 별도 운영 결정으로 다룬다.
- **외부 다운로드는 트랜잭션 밖에서 수행한다** (해당 코드는 `application`): 네트워크 구간을 트랜잭션 안에 넣으면 그동안 DB 커넥션이 묶인다. 다운로드 → (트랜잭션) 저장 순으로 나누되, **같은 빈의 메서드를 자기 자신이 호출하면 Spring 프록시를 거치지 않아 `@Transactional`이 적용되지 않으므로**(self-invocation) 저장 구간은 별도 빈(`XxxExecutor`)이 소유한다. reference: `AdminDongSchedulerService`(다운로드) + `AdminDongSyncExecutor`(저장), `ProductSoldOutReleaseExecutor`, `ReviewBlindExpirationExecutor`.
- **마스터 동기화는 삭제·재삽입이 아니라 id 보존 갱신이다**: 다른 테이블이 마스터의 `id`를 참조하고 있으면(행정동의 경우 배달가능지역·지역별 배달팁·주문 스냅샷) 전량 교체 시 그 참조가 **말없이 다른 행을 가리키거나 끊어진다.** 자연키(행정동은 `code`)로 매칭해 제자리 갱신하고, 원천에서 사라진 행은 삭제 대신 `is_active = 0`으로 내린다. reference: `AdminDongRepository#synchronize`.
- **단일 인스턴스 배포 전제**: `@EnableScheduling` 기반 cron은 인스턴스마다 독립 실행된다. batch-module을 여러 인스턴스로 배포하면 동일 작업이 중복 실행되므로, 운영 시 배치 인스턴스는 1대로 유지한다(분산 락 등 중복 방지 로직은 아직 없음).

## admin-api 비대칭 (의도된 설계)

`admin-api`에는 배치 스케줄러가 없다. `admin-api`는 관리자용 CRUD/조회 API만 제공하며, 시간 기반으로 자동 실행되어야 하는 배치 유스케이스(랭킹 집계, 등급 갱신, 인기 검색어 집계, 상품 옵션 동기화)가 전부 사용자(web) 도메인에 속하기 때문이다. `ceo-api`도 같은 이유로 스케줄러를 갖지 않는다. 배치 책임은 web-api가 아니라 이 `batch-module`로 일원화되어 있다.

## Dependencies

### Internal
- `domain-module` (implementation) — 도메인 모델·VO·write 포트·도메인 서비스·`ErrorCode`/`BusinessException`·`shared/page`
- `application` (implementation) — 잡 UseCase 인바운드 포트(트리거가 주입) + `BatchApplicationConfig`(`BatchApplication`이 `@Import`)
- `infrastructure:persistence` (implementation) — DAO 구현체가 뜨는 빈 스캔 대상. `com.tastyhouse.infrastructure..`·`com.querydsl..` 소스 import는 ArchUnit이 전면 차단
- `external-api` (implementation) — `BbqApiClient`(크롤링 HTTP 클라이언트), `RemoteImageDownloader`. **소스 참조는 `application`으로 옮겨갔고**, 이 모듈은 빈 스캔·설정(`application-external.yml`) 때문에 유지한다
- `logging-module` (implementation)

### External
- Spring Boot Starter(루트 `subprojects`가 부여) — `@Scheduled`/`@Transactional` 지원

## 빈 배선

`BatchApplication`은 `@Import({InfrastructureModuleConfig, ExternalApiConfig, LoggingModuleConfig, BatchApplicationConfig})`로 각 모듈의 진입점 설정을 조합한다(`scanBasePackages` 문자열 나열 대신 타입 세이프 조합 — `InfrastructureModuleConfig` 선례).

> **이 모듈에는 `contextLoads` 테스트가 없다.** web/admin/ceo와 달리 `BatchApplicationTests`가 없어서, `@Import`에서 모듈 하나를 빠뜨려도 **빌드는 green이고 jar만 조용히 깨진다**(빈을 못 찾아 부팅 실패). 배선을 건드렸으면 빌드만 믿지 말고 실제로 띄워 `Started BatchApplication` 마커를 확인한다.
>
> ```bash
> pkill -f 'batch-module-.*\.jar'
> cd backend && ./gradlew :batch-module:build
> nohup java -jar batch-module/build/libs/batch-module-0.0.1-SNAPSHOT.jar > /tmp/batch.log 2>&1 &
> grep 'Started BatchApplication' /tmp/batch.log
> ```

## 설정 파일

`src/main/resources/application.yml`이 `application-infrastructure.yml`(DB/JPA, `infrastructure:persistence` 소유)과 `application-external.yml`(크롤링/S3, external-api 소유)을 `classpath:` import한다 — web-api와 동일한 패턴. 웹 전용 설정(서버 포트/CORS/JWT/OAuth/Redis/multipart)은 없다.

<!-- MANUAL: -->

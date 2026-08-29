# batch-module

시간 기반 배치 유스케이스(스케줄러)를 전담하는 **독립 실행 모듈**. `web-api`/`admin-api`/`ceo-api`와 동일하게 자체 `main()`(`BatchApplication`)을 가진 bootJar 실행 단위이며, HTTP 요청과 무관하게 `@Scheduled` cron 트리거로만 동작한다.

## 신설 배경

기존에는 스케줄러 5개(`RankScheduler`/`ProductScheduler`/`GradeScheduler`+`GradeSchedulerService`/`SearchKeywordScheduler`)가 프레젠테이션 모듈인 `web-api`에 있었다. 웹 서버와 배치 서버를 분리 배포하기 위해 이 모듈로 이동했다(작업지시서 `docs/tasks/06-scheduler-relocation.md` 선택지 B).

## 패키지 구조 (개정됨 — 챕터 04 잡별 인바운드 포트 도입)

과거에는 모든 스케줄러·서비스가 `com.tastyhouse.batch.scheduler` 한 패키지에 평면적으로 있었으나, 인바운드 포트(UseCase 인터페이스) 도입과 함께 잡(job)마다 아래 3층 패키지로 재편했다.

```
com.tastyhouse.batch/
├── BatchApplication.java          @SpringBootApplication + @EnableScheduling (진입점)
├── <job>/                         잡 슬러그별 패키지 — region · grade · product · productsoldout · rank · reviewblind · search
│   ├── adapter/in/scheduler/          @Scheduled 트리거 클래스(로직 없음, UseCase 호출만)
│   ├── application/port/in/           잡 UseCase 인터페이스(입력이 없어 Command record 불필요)
│   └── application/service/           `*SchedulerService implements {Job}UseCase`(@Transactional 경계) +
│                                       필요 시 `*Executor`/`*Runner` 협력 빈(self-invocation 회피용 별도 트랜잭션 경계)
└── crawling/bbq/                  product 잡의 ProductSchedulerService가 의존하는 BBQ 크롤링 어댑터
    ├── BbqService.java
    └── response/*.java
```

| 잡 슬러그 | UseCase | 트리거 | 비고 |
|---|---|---|---|
| `region` | `SynchronizeAdminDongsUseCase` | `AdminDongScheduler`(매월 1일 04시) | `AdminDongSchedulerService`(다운로드, 트랜잭션 밖) + `AdminDongSyncExecutor`(저장, 트랜잭션) + `AdminDongSyncRunner`(수동 1회 실행, 기본 비활성) |
| `grade` | `SettleMemberGradesUseCase` | `GradeScheduler` | `GradeSchedulerService`가 등급 계산·확정 전담 |
| `product` | `SyncProductOptionsUseCase` | `ProductScheduler`(`@Scheduled` 주석 처리된 비활성 상태 유지) | `ProductSchedulerService`가 BBQ 옵션 크롤링 저장 |
| `productsoldout` | `ReleaseExpiredSoldOutUseCase` | `ProductSoldOutReleaseScheduler` | `ProductSoldOutReleaseSchedulerService` + `ProductSoldOutReleaseExecutor`(트랜잭션 경계 분리) |
| `rank` | `AggregateRanksUseCase` | `RankScheduler` | `RankSchedulerService`가 랭킹 집계 로직 전담 |
| `reviewblind` | `ExpireBlindedReviewsUseCase` | `ReviewBlindScheduler` | `ReviewBlindSchedulerService` + `ReviewBlindExpirationExecutor`(트랜잭션 경계 분리) |
| `search` | `AggregatePopularKeywordsUseCase` | `SearchKeywordScheduler` | `SearchKeywordSchedulerService`가 인기 검색어 집계 전담 |

## 규칙

- **Scheduler(트리거) + Service(로직) 이분 구조는 유지, 패키지만 3층으로 재편**: `adapter/in/scheduler/`의 `@Scheduled` 클래스는 cron 트리거와 try/catch 로깅만 담당하고, 실제 로직은 `application/service/`의 `*SchedulerService`(`@Transactional` 경계 소유)로 위임한다. `core-module` → `domain-module` 전환으로 core의 application 계층이 해체되었으므로, **이 `XxxSchedulerService`가 배치의 application 계층**이다 — domain write 포트·도메인 서비스와 `application-common-module`의 `{Ctx}QueryPort`를 직접 주입해 조합한다.
- **잡 UseCase 인터페이스(`application/port/in/`)**: 잡마다 1개(`SettleMemberGradesUseCase`·`AggregateRanksUseCase`·`SyncProductOptionsUseCase`·`ReleaseExpiredSoldOutUseCase`·`ExpireBlindedReviewsUseCase`·`SynchronizeAdminDongsUseCase`·`AggregatePopularKeywordsUseCase`). **배치 잡은 입력이 없으므로 Command record를 두지 않는다** — web/admin/ceo의 `{도메인}CommandUseCase`와 달리 파라미터 없는 메서드 하나만 선언한다. `*SchedulerService`가 이를 implements하고, 트리거는 구체 클래스가 아니라 이 인터페이스만 주입한다(신설 ArchUnit `schedulerServicesShouldImplementUseCase`·`schedulersShouldDependOnUseCasesOnly`가 강제).
- **도메인 모델은 POJO — 명시적 save 필수**: 스케줄러 Service에서 도메인을 변경한 뒤 반드시 `repository.save(domain)`을 호출한다(JPA 더티 체킹이 없어 누락 시 변경이 조용히 유실된다).
- **QueryDSL·infra 직접 호출 금지 (개정)**: `src/main`에 `com.querydsl.*` import·`@QueryProjection` 선언·`com.tastyhouse.infrastructure..` import가 **전면 0건**이며, `src/test/.../architecture/LayerRulesTest`(ArchUnit)가 이를 차단한다(챕터 04의 임시 장치 `shouldNotDependOnInfrastructureQuery`는 챕터 05에서 제거됐다). 조회는 `application-common-module`의 `{Ctx}QueryPort` 인터페이스를 주입해 쓴다(reference: `product` 도메인의 `ProductQueryPort#findFirstBbqSyncTarget` — BBQ 옵션 동기화 대상 조회). 이 모듈만 클래스명 `*CommandService`/`*QueryService`인 CQRS 서비스가 0개(`XxxSchedulerService` 네이밍)라, `applicationServicesShouldNotDependOnWebLayer` 규칙은 매칭 대상에 `*SchedulerService`를 포함시켜 공허 통과를 없앴다(대상이 실재하게 되어 `allowEmptyShould(true)`를 제거했다 — 챕터 05 기준 이 모듈을 포함해 리포 전체에 `allowEmptyShould(true)`가 0건이다).
- **cron 표현식은 순수 구조 리팩터링 대상이 아니다**: 스케줄 주기를 바꾸는 변경은 이 모듈이 아니라 별도 운영 결정으로 다룬다.
- **외부 다운로드는 트랜잭션 밖에서 수행한다**: 네트워크 구간을 트랜잭션 안에 넣으면 그동안 DB 커넥션이 묶인다. 다운로드 → (트랜잭션) 저장 순으로 나누되, **같은 빈의 메서드를 자기 자신이 호출하면 Spring 프록시를 거치지 않아 `@Transactional`이 적용되지 않으므로**(self-invocation) 저장 구간은 별도 빈(`XxxExecutor`)이 소유한다. reference: `AdminDongSchedulerService`(다운로드) + `AdminDongSyncExecutor`(저장), `ProductSoldOutReleaseExecutor`, `ReviewBlindExpirationExecutor`.
- **마스터 동기화는 삭제·재삽입이 아니라 id 보존 갱신이다**: 다른 테이블이 마스터의 `id`를 참조하고 있으면(행정동의 경우 배달가능지역·지역별 배달팁·주문 스냅샷) 전량 교체 시 그 참조가 **말없이 다른 행을 가리키거나 끊어진다.** 자연키(행정동은 `code`)로 매칭해 제자리 갱신하고, 원천에서 사라진 행은 삭제 대신 `is_active = 0`으로 내린다. reference: `AdminDongRepository#synchronize`.
- **단일 인스턴스 배포 전제**: `@EnableScheduling` 기반 cron은 인스턴스마다 독립 실행된다. batch-module을 여러 인스턴스로 배포하면 동일 작업이 중복 실행되므로, 운영 시 배치 인스턴스는 1대로 유지한다(분산 락 등 중복 방지 로직은 아직 없음).

## admin-api 비대칭 (의도된 설계)

`admin-api`에는 배치 스케줄러가 없다. `admin-api`는 관리자용 CRUD/조회 API만 제공하며, 시간 기반으로 자동 실행되어야 하는 배치 유스케이스(랭킹 집계, 등급 갱신, 인기 검색어 집계, 상품 옵션 동기화)가 전부 사용자(web) 도메인에 속하기 때문이다. `ceo-api`도 같은 이유로 스케줄러를 갖지 않는다. 배치 책임은 web-api가 아니라 이 `batch-module`로 일원화되어 있다.

## Dependencies

### Internal
- `domain-module` (implementation) — 도메인 모델·VO·write 포트·도메인 서비스·`ErrorCode`/`BusinessException`·`shared/page`
- `application-common-module` (implementation) — `{Ctx}QueryPort` 인터페이스·Result DTO를 컴파일 타임에 주입
- `infrastructure-module` (implementation) — DAO 구현체가 뜨는 빈 스캔 대상. `com.tastyhouse.infrastructure..`(과거 허용되던 `..query..` 포함)·`com.querydsl..` 소스 import는 ArchUnit이 전면 차단
- `external-api` (implementation) — `BbqApiClient`(크롤링 HTTP 클라이언트), `RemoteImageDownloader`
- `logging-module` (implementation)

### External
- Spring Boot Starter(루트 `subprojects`가 부여) — `@Scheduled`/`@Transactional` 지원

## 설정 파일

`src/main/resources/application.yml`이 `application-infrastructure.yml`(DB/JPA, infrastructure-module 소유)과 `application-external.yml`(크롤링/S3, external-api 소유)을 `classpath:` import한다 — web-api와 동일한 패턴. 웹 전용 설정(서버 포트/CORS/JWT/OAuth/Redis/multipart)은 없다.

<!-- MANUAL: -->

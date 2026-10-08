# application

**application 계층의 코어 모듈.** application 계층은 앱 마커 제거 이후 **5개 Gradle 모듈**로 나뉜다 — 이 코어 `application`과 앱 모듈 `web-application`·`admin-application`·`ceo-application`·`batch-application`(각 모듈의 `AGENTS.md` 참고). 자바 패키지는 5모듈 모두 `com.tastyhouse.application.<도메인>.{port.in, port.out, service, listener}` 하나를 나눠 쓴다(split package). **이 코어에는 "2개 앱 이상이 쓰는 것"만 산다** — 공유 도메인 서비스(과거 `@SharedApp` 35개), 도메인 이벤트 리스너 12종(`<ctx>/listener/`), `shared/**`(`SharedBeanConfig` 포함), **모든 `port.out` 계약**(읽기 계약·write 포트·Command 반환 Result), `PgConfirmResult`·`TossPaymentDetail`. UseCase 인터페이스·Command record·유스케이스 서비스(유스케이스당 서비스 1개 — 분리 전에는 `*CommandService`/`*QueryService` 쌍)·앱 전용 도메인 서비스·앱 전용 SPI 포트는 앱 모듈에 있다. **앱 소속은 마커 애노테이션이 아니라 Gradle 모듈이 표현한다** — 상세는 아래 [앱 마커 제거 — 앱 모듈 재분리](#앱-마커-제거--앱-모듈-재분리-챕터-01-통합챕터-03-마커-번복). 이 문서는 5모듈 공통 규칙의 정본이기도 하다(앱 모듈 `AGENTS.md`는 요약만 둔다).

> **(번복됨 — 앱 마커 제거)** 과거 소개: "4개 앱(web · admin · ceo · batch)의 application 계층을 담는 단일 모듈. … 컨텍스트별 인바운드 포트(`<ctx>/port/in/`)와 그 구현인 `*CommandService`/`*QueryService`(batch는 `*SchedulerService`), 읽기 계약 326개, 그리고 도메인 이벤트 리스너 12종(`<ctx>/listener/`)이 이 한 패키지 트리 안에 함께 있다. **앱 소속은 패키지가 아니라 마커 애노테이션**(`@WebApp`/`@AdminApp`/`@CeoApp`/`@BatchApp`, 그리고 리스너 전용 "앱 소속 없음 = 4앱 전부" 마커 `@SharedApp`)이 표현한다 — 상세는 아래 [챕터 03 — 패키지 평탄화 + 앱 마커](#챕터-03--패키지-평탄화--앱-마커-애노테이션-과거-판단의-번복)."

컨트롤러(`<ctx>/adapter/in/web/`)·`request/`·`response/`·config·security 정책·전역 예외 핸들러와 부트스트랩은 각 api 모듈(`web-api`·`admin-api`·`ceo-api`·`batch-module`)에 남아 있다.

## 앱 마커 제거 — 앱 모듈 재분리 (챕터 01 통합·챕터 03 마커 번복)

**앱 마커 5종(`com.tastyhouse.application.shared.marker.{WebApp,AdminApp,CeoApp,BatchApp,SharedApp}`)을 전부 삭제하고, 앱 소속을 Gradle 모듈 경계로 옮겼다.** 아래 "과거 판단의 번복 — 앱 축을 접은 이유(챕터 01)"와 "챕터 03 — 패키지 평탄화 + 앱 마커 애노테이션"은 이 절로 번복됐다(패키지 평탄화 자체는 유지된다).

**용어 풀이**

- **코어**: 모듈명 `application`(이름 유지). 2개 앱 이상이 쓰는 것과 리스너·`@Configuration`·`port.out` 계약을 담는다.
- **앱 모듈**: `{web,admin,ceo,batch}-application`. 평면 이름의 `java-library`이고 `api project(':application')`으로 코어를 노출한다. 앱 하나만 쓰는 빈·UseCase·Command·도메인 서비스·SPI 포트를 담는다.
- **컴파일 게이트**: "클래스패스에 없어서 아예 import할 수 없다"로 막는 것. ArchUnit은 위반을 테스트 시점에 잡지만 컴파일 게이트는 코드가 애초에 컴파일되지 않는다.
- **split package**: 같은 자바 패키지를 여러 모듈이 나눠 갖는 것. `security-core`/`security-module`(`com.tastyhouse.security..`) 선례가 있다.

**왜 바꿨나**

- 마커는 **컴파일러가 모르는 약속**이었다. 잘못 달거나 빠뜨려도 빌드가 통과했고, 그 구멍을 `AppIsolationTest`(앱 간 의존·마커 누락·앱 전용 포트 소비자), `AppOwnership`(Command 소속 유도), api 4모듈 `adaptersShouldOnlyUseOwnAppUseCases` 같은 규칙으로 메웠다. 규칙이 많을수록 술어의 사각지대도 많았다.
- 챕터 01이 앱 모듈 4개를 하나로 합친 근거("persistence가 4개 앱 모듈 전부를 의존해 컴파일 게이트가 사실상 없었다")가 **지금은 성립하지 않는다.** persistence가 application에서 import하는 것은 `..port.out..`뿐이고 `port.out`을 전부 코어에 두면 **persistence는 코어 하나만 의존**한다. 따라서 앱 모듈끼리는 어떤 경로로도 서로를 보지 못하고, 컴파일 게이트가 진짜로 동작한다.

**무엇이 바뀌었나 (before / after)**

| 항목 | before | after |
|---|---|---|
| application 계층 모듈 | `application` 1개 | `application`(코어) + `{web,admin,ceo,batch}-application` 4개 — 전체 모듈 32 → 36 |
| 앱 소속 표현 | 마커 애노테이션 5종 | 클래스가 들어 있는 모듈 |
| 자바 패키지 | `com.tastyhouse.application..` | **변경 없음**(5모듈 split package) — 클래스를 옮겨도 import가 안 바뀐다 |
| 빈 선언 | 오케스트레이터 `@Service` + 마커, 도메인 서비스 마커만(78개) | 전부 `@Service`(또는 `@Component`) |
| 오케스트레이터 vs 도메인 서비스 구분 | "마커만 = 도메인 서비스, `@Service` + 마커 = 오케스트레이터" | **`port.in` UseCase를 구현하면 오케스트레이터, 아니면 도메인 서비스**(구조로 판정) |
| 앱 스캔(`ApplicationLayerScanConfig`) | `useDefaultFilters = false` + 마커 `ANNOTATION` include 필터 | `@Configuration(proxyBeanMethods = false) @ComponentScan(basePackages = "com.tastyhouse.application")` — 기본 필터, 필터 없음. 클래스패스(코어 + 자기 앱 모듈)가 스캔 범위를 정한다 |
| 실행 앱 의존 | `implementation project(':application')` | 그대로 + `implementation project(':{앱}-application')` |
| 벤더 의존 | 전부 `:application` | `{kakao,naver,apple,facebook}-oauth`·`tosspayments`·`javamail`·`solapi`·`aws-ses`·`aws-sns` → `:web-application`, `bbq`·`admdongkor` → `:batch-application`. `persistence`·`firebase`·`aws-s3`·`security-module`·`api-common-module`은 `:application` 그대로 |
| 앱별 빈 집합 | — | **동일** — 4앱 jar를 띄워 싱글턴 빈 이름을 비교해 차이 0(web 1131 · admin 1079 · ceo 1155 · batch 791) |

**앱 전용 SPI 포트(앱 모듈 소유)** — 구현(벤더)이 그 앱에만 조립되는 포트는 그 앱 모듈이 갖는다. 코어가 이 포트를 import하면 **컴파일 에러**다(반증 확인).

- `web-application`: `mail.port.out.{MailSender,MailSendResult}`, `sms.port.out.{SmsSender,SmsSendResult,SmsSendFailure}`, `payment.port.out.{PgProviderGateway,PgPaymentGateway,PgCancelResult,PgProviderCode}`, `auth.port.out.{SocialOAuthClient,SocialAuthorization,SocialCredential,SocialOAuthResult,SocialOAuthFailure,SocialProfile,SocialProvider}`
- `ceo-application`: `ceo.port.out.ReplyPhraseTextValidator` — 구현(`shop/service/ReplyPhraseProhibitedWordValidatorAdapter`)과 유일한 소비자가 모두 ceo라 코어에서 옮겨왔다. 이 포트는 구현이 infrastructure가 아니라 같은 앱 모듈에 있다는 점이 다른 SPI와 다르다
- `batch-application`: `crawling.bbq.port.out.*`(`BbqMenuPort`·`RemoteImagePort`·DTO), `region.port.out.{AdminDongBoundaryPort,AdminDongBoundaryFetchResult,AdminDongBoundarySource,BoundaryCoordinate,BoundaryRing}`

**새 클래스를 어디에 두나**

1. 쓰는 앱이 하나 → 그 `{앱}-application`. UseCase·Command·유스케이스 서비스(`{도메인}{동작}Service`/`{도메인}{관점}QueryService`, batch는 `*SchedulerService`)는 언제나 앱 모듈이다(코어의 UseCase는 0개). 새 연산은 포트 1개(추상 메서드 1개) + 서비스 1개(포트명의 `UseCase`→`Service`)로 만든다 — 아래 "봉인·가드 목록"의 "유스케이스 서비스 1:1 규칙 4종".
2. 2개 앱 이상이 쓰는 도메인 서비스 → 코어. 두 번째 앱이 생기는 시점에 코어로 옮긴다(패키지가 같아 import 불변).
3. 빈이면 언제나 `@Service`/`@Component`를 단다.
4. 리스너(`@TransactionalEventListener`)와 `@Configuration`은 **코어에만**. `@Configuration`이 스테레오타입 클래스를 `@Bean`으로 다시 등록하지 않는다(스캔과 이중 등록).
5. `port.out` 계약은 코어. 예외는 위 앱 전용 SPI 포트.
6. 코어 빈은 구현체가 앱 모듈에만 있는 application 인터페이스를 주입받지 않는다.

**가드 (before / after)** — 정본 목록은 아래 [봉인·가드 목록](#봉인가드-목록)의 "앱 모듈 경계 가드" 항목.

| 대상 | before | after |
|---|---|---|
| 앱 간 수평 의존 · 앱 전용 포트 소비자 · Command 소속 | `AppIsolationTest`(파일 전체), testFixtures `AppOwnership` + `DESERIALIZED_COMMANDS`, api 4모듈 `LayerRulesTest#adaptersShouldOnlyUseOwnAppUseCases` | **전부 삭제 — 컴파일 게이트로 대체** |
| 코어 `LayerRulesTest` 마커 규칙 | `#listenersShouldBeShared`·`#sharedAppOnlyOnListeners`·`#markerOnlyClassesShouldBeDomainServices`·`#sharedConfigsShouldOnlyDeclareUnmarkedBeans` | 삭제. 신설 `#listenersAndConfigsShouldResideInCore`·`#coreShouldNotContainUseCasesOrOrchestrators`·`#coreBeansShouldOnlyDependOnCoreVisibleTypes`·`#configurationsShouldNotRegisterStereotypedClasses` |
| `commandRecordsShouldBeBoundaryTyped` batch 예외 | `.areNotAnnotatedWith(BatchApp.class)` | 출처 모듈(`batch-application`)로 판정 |
| `BatchSchedulerRulesTest` 대상 | `@BatchApp` 클래스 | `batch-application` 출처 클래스 |
| `RuleAnchorTest` 개수 anchor | `markerBeanCounts`·`markerUseCaseCounts` | `#moduleBeanCounts`(web ≥83 · admin ≥65 · ceo ≥122 · batch ≥15 · core ≥47)·`#moduleUseCaseCounts`(web ≥50 · admin ≥100 · ceo ≥95 · batch =7 · core =0 — 앱 마커 제거 시점 값. 유스케이스 분리 후 web ≥187 · admin ≥207 · ceo ≥188) |
| `ServiceContextBoundaryTest#domainServices()` | 스테레오타입 없는 `..service..` POJO | 구조 조건 − `EXCLUDED_COLLABORATORS` 29개(대상 94개 불변) + 짝 `excludedCollaboratorsShouldNotBeStale` |
| testFixtures `ApplicationLayerScanAssertions` | `assertScansOnlyOwnAppAndSharedMarkers` | `assertScansApplicationLayerWithoutFilters` + 신설 `assertLoadsOnlyOwnApplicationModule(appModule)`(출처 모듈 집합 판정 — 처음엔 `application-module.properties` 표식으로 판정했으나 표식을 지우고 `ModuleOrigin`으로 교체) |
| 출처 모듈 판정 | 없음 | 신설 testFixtures `com.tastyhouse.architecture.ModuleOrigin`(`ModuleOrigin.from(module)` 술어 — 규칙별 `FROM_BATCH_APPLICATION` 같은 지역 술어를 대체) + `ModuleOriginTest` |

- **아키텍처 테스트는 이 코어 모듈의 테스트에 둔다.** `application/build.gradle`이 `testImplementation project(':{web,admin,ceo,batch}-application')` 4줄을 가져 ArchUnit이 5모듈을 모두 본다. test → main 방향이라 Gradle 순환이 아니다.
- **공유 테스트 더블 20개**(`Fake*`/`Stub*`/`Recording*`/`ListenerLogCapture`)는 코어 testFixtures의 `com.tastyhouse.testsupport.<ctx>..`로 옮겼다. 앱 모듈의 테스트도 이 더블을 써야 하기 때문이다. package-private이던 9개는 public이 됐다. 새 최상위 import 세그먼트 `testsupport`는 4순위다(`ImportOrderConventionTest.TOP_SEGMENT_RANK`, `backend/CLAUDE.md`의 import 순서 표).
- 새 규칙은 전부 위반 probe로 실패를 확인했다(코어가 `MailSender`를 import하면 컴파일 에러인 것도 확인).

## 과거 판단의 번복 — 앱 축을 접은 이유 (챕터 01)

> **(번복됨 — 앱 마커 제거)** 이 절의 4 → 1 통합은 다시 번복돼 `{web,admin,ceo,batch}-application` 4모듈이 돌아왔다. 첫째 근거("persistence가 4개 application 모듈을 전부 의존해 컴파일 게이트가 사실상 없었다")가 지금은 성립하지 않기 때문이다 — `port.out`을 전부 코어에 두자 persistence는 코어만 의존하게 됐다. 둘째 근거(소유권 연쇄)는 계약을 코어가 전부 가지므로 재발하지 않고, 셋째 근거(규칙 중복)는 아키텍처 테스트를 코어 한 곳에 두어(`testImplementation`으로 앱 모듈 4개를 봄) 피했다. 현재 규칙은 [앱 마커 제거 — 앱 모듈 재분리](#앱-마커-제거--앱-모듈-재분리-챕터-01-통합챕터-03-마커-번복).

챕터 01~04(각각 batch·web·admin·ceo)로 앱마다 `{app}-application` 모듈을 하나씩 세웠던 것을, **이 챕터가 되돌려 하나로 합쳤다.** 앱 축 분리가 값을 못 했다는 판단이며 근거는 셋이다.

- **컴파일 게이트가 사실상 없었다.** `infrastructure/persistence/build.gradle`이 4개 application 모듈을 전부 `implementation`으로 의존하므로 **모든 실행 jar에 4개 jar가 이미 들어 있었다**(admin-api fat jar `BOOT-INF/lib/` 실측). 앱 분할이 실제로 준 게이트는 application → application 한 방향뿐이었고, 그것은 ArchUnit이 패키지로 이미 막고 있었다.
- **소유권 연쇄가 부채를 낳았다.** 읽기 계약을 앱 모듈이 소유하게 하면서 "한 앱이 소유하면 다른 앱이 그 모듈을 의존해야 한다"를 피하려고, 공유 계약을 `domain`로 올리고 `application-common-module`을 해체했다(챕터 05·07·09). 그 결과가 split package 5모듈과 가드 3종(`ReadContractSingleOwnerTest`·`ReadContractPurityTest`·`RuleAnchorTest`의 소유 모듈 필터)이다. **챕터 04에서 그 55개를 이 모듈로 되돌려 이 부채가 통째로 사라졌다.**
- **중복이 컸고 이득이 없었다.** web·admin·ceo의 `LayerRulesTest`는 규칙 16종이 이름·본문까지 동일했다(diff는 carve-out 이름과 `because` 문구뿐). `gradle.properties`가 비어 있어 병렬 빌드 이득도 없었다.

**이 챕터의 범위는 Gradle 모듈만 4 → 1이다.** 자바 패키지는 그대로였다(`com.tastyhouse.{app}application` + `com.tastyhouse.application.<ctx>.port.out`). 뒤 챕터에서 동명 클래스 182건 개명(02) → **패키지 평탄화 + 앱 마커 애노테이션(03, 완료 — 아래 절)** → 공유 읽기 계약 55개 복귀(04, 완료)가 이어진다.

## 챕터 03 — 패키지 평탄화 + 앱 마커 애노테이션 (과거 판단의 번복)

> **(번복됨 — 앱 마커 제거)** 패키지 평탄화(`com.tastyhouse.application` 하나)는 **유지**되지만, 그 대체 수단이던 마커 애노테이션 5종·`AppOwnership` 유도·`AppIsolationTest`·`adaptersShouldOnlyUseOwnAppUseCases`·마커 include 필터 스캔은 **전부 삭제**됐다. 앱 소속은 이제 Gradle 모듈이 표현한다. 아래는 그 시점 기록이다. 현재 규칙은 [앱 마커 제거 — 앱 모듈 재분리](#앱-마커-제거--앱-모듈-재분리-챕터-01-통합챕터-03-마커-번복).

**챕터 01 직후에는 Gradle 모듈만 합쳐졌고 앱별 패키지(`com.tastyhouse.{web|admin|ceo|batch}application`)는 그대로 남아 있었다. 이 챕터가 그 4개 패키지를 `com.tastyhouse.application` 하나로 평탄화했다.**

- **왜 평탄화했나**: 챕터 01의 판단 근거 중 하나였던 "중복이 컸고 이득이 없었다"가 패키지 수준에서도 반복되고 있었다 — 앱별 패키지가 남아 있는 한 `ArchUnit` 슬라이스 규칙·import 정렬 규칙 모두 "접두어가 겹치는 4개 패키지"를 특별 취급해야 했고, 그 특별 취급 자체가 문서·규칙의 복잡도였다. 패키지를 하나로 합치면 그 특별 취급이 사라진다.
- **잃는 것**: 패키지 자체가 앱 소속을 말해주던 유일한 단서가 사라진다. `NoticeQueryService`(당시 이름)가 `com.tastyhouse.adminapplication.notice.service`에 있다는 사실만으로 "이건 admin 것"임을 알 수 있었는데, 평탄화 후에는 `com.tastyhouse.application.notice.service`가 되어 그 정보가 없다.
- **대체 수단 — 마커 애노테이션 4종**: `com.tastyhouse.application.shared.marker.{WebApp,AdminApp,CeoApp,BatchApp}`. 순수 마커(`@Component` 메타 없음, `@Target(TYPE)` + `@Retention(RUNTIME)` + `@Documented`)이며, 빈 242개(`@Service` 220 + `@Component` 22)와 UseCase 인터페이스 257개에 정확히 하나씩 붙는다(ServiceConfig 삭제 후에는 `@Service` 없이 마커만 단 도메인 서비스 78개도 더해진다 — 아래 "application `*ServiceConfig` 전면 삭제" 절). **Command record에는 붙이지 않는다** — 소속은 유도한다(아래).
- **스캔이 패키지에서 애노테이션으로 바뀌었다**: 마커 스캔이 `com.tastyhouse.application` 루트를 대상으로 하며 `@ComponentScan(basePackages = "com.tastyhouse.application", useDefaultFilters = false, includeFilters = @Filter(type = ANNOTATION, classes = XxxApp.class))` 형태다. **`useDefaultFilters = false`이므로 마커 없는 `@Service`는 컴파일은 통과하지만 어느 앱에도 뜨지 않는다** — 그 실패는 그 빈이 처음 필요해지는 기동 시점에야 `NoSuchBeanDefinitionException`으로 드러난다. jar 이름·경로는 불변이다. **(번복됨 — application `*ApplicationConfig` 삭제)** 이 `@ComponentScan`은 과거 이 모듈의 `{Web,Admin,Ceo,Batch}ApplicationConfig`에 있었고 api 4모듈이 `@Import`했으나, 지금은 이 모듈에 스캔 선언이 없고 각 앱 부트스트랩의 static 중첩 `ApplicationLayerScanConfig`가 소유한다(아래 [빈 배선](#빈-배선-챕터-03-개정--패키지-스캔에서-마커-스캔으로) 절 참고).
- **(후속 추가) 5번째 마커 `@SharedApp` — 리스너 전용**: 도메인 이벤트 리스너 12종을 `infrastructure:persistence`에서 이 모듈의 `<ctx>/listener/`로 옮기면서 신설했다. 의미는 "앱 소속 없음 = 4앱 전부에 뜬다"이고, 4앱 부트스트랩 중첩 `ApplicationLayerScanConfig`의 필터가 `classes = {XxxApp.class, SharedApp.class}`로 넓어졌다(당시에는 4개 `*ApplicationConfig`의 필터였다). ~~**리스너 외에는 붙이지 않는다**~~ **(번복됨 — 덩어리 01: 리스너 + `..config..`의 `@Configuration`까지 허용, 아래 [ArchUnit](#archunit--4클래스-챕터-03으로-importer판별-기준이-패키지에서-마커로-전환) 절)** — 일반 `@Service`에 붙이면 앱 격리를 우회하므로 `LayerRulesTest#sharedAppOnlyOnListeners`가 막는다(이것은 여전히 금지). 상세는 아래 [`<ctx>/listener/` — 도메인 이벤트 리스너](#ctxlistener--도메인-이벤트-리스너).
- **파일 이동 2건**: `batchapplication/exception/BatchJobException` → `application/shared/exception/`, `batchapplication/crawling/bbq/response/*.java` 4개(`BbqProductResponse`·`BbqProductCategoryResponse`·`BbqProductSubOptionResponse`·`SubOptionItemDetailResponse`) → `application/crawling/bbq/port/out/`.
- **`<ctx>/port/out`의 의미가 넓어졌다** — 이제 "이 도메인의 **모든 아웃바운드 계약**"이다. 읽기 계약(`QueryPort`·`Result`·`SearchCondition`) + 아웃바운드 SPI(`SocialOAuthClient`·`BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort`) + **명령 유스케이스 서비스(당시 CommandService)가 반환하는 Result/View record**가 함께 산다.
- **Command record는 마커 없이 유도한다**: `AppOwnership`(`application/src/testFixtures/java/com/tastyhouse/architecture/AppOwnership.java`)이 `apps(R) = R을 시그니처에 쓰는 마커 UseCase의 마커 집합 ∪ R을 컴포넌트로 품는 record의 apps`(전이 폐쇄)로 소속을 계산한다. 0개=고아(죽은 코드), 2개 이상=앱 간 공유(경계 위반) 둘 다 위반. **carve-out 1건**: `ShopStorePriceVerificationItemCommand`는 multipart 문자열 파트를 서비스가 `ObjectMapper`로 역직렬화해 만들어 정적 참조가 없으므로 `AppOwnership.DESERIALIZED_COMMANDS`에 소속(`CeoApp`)을 명시했다 — 유도가 닿을 수 없는 정상 형태다.
- **`AppOwnership`은 `testFixtures`에 있고 api 4모듈이 재사용한다**(`java-test-fixtures` 플러그인, `testImplementation(testFixtures(project(':application')))`) — api 모듈의 `adaptersShouldOnlyUseOwnAppUseCases`도 같은 유도가 필요하기 때문이다.
- **ArchUnit 규칙 전환**: `commandServicesShouldNotDependOnQueryPorts`가 패키지 술어 → **이름 기준**(`haveSimpleNameEndingWith("QueryPort")` / `"QueryService"`)으로 바뀌었다 — `port.out`에 Command 반환 record가 함께 살게 되어, 패키지 술어를 두면 그 record를 import하는 CommandService 7개가 정당한 반환 타입인데도 위반으로 잡히기 때문이다. 같은 이유로 api 3모듈의 `controllersShouldNotDependOnQueryPorts`도 이름 기준이다. `AppIsolationTest`는 슬라이스/패키지 술어에서 **마커 술어**로 전면 재작성됐다(아래 [ArchUnit — 4클래스](#archunit--4클래스-챕터-03으로-importer판별-기준이-패키지에서-마커로-전환) 절 반영). 상세 규칙 목록·근거는 루트 `backend/CLAUDE.md`의 "앱 마커 규칙" 절 참고.

### 잃어버린 컴파일 게이트를 무엇이 대체했나

> **(번복됨 — 앱 마커 제거)** 아래 표의 두 대체 규칙은 삭제됐다. 앱 모듈이 다시 나뉘어 **잃었던 컴파일 게이트 자체가 돌아왔기** 때문이다 — 다른 앱의 application 타입은 클래스패스에 없어 import가 컴파일 에러다.

모듈이 하나가 되면서 **앱 간 수평 의존을 빌드가 막지 못하게 됐다.** 이 챕터는 그 자리에 ArchUnit 규칙 두 개를 같은 커밋에 세웠다 — 나중에 넣으면 그 사이에 들어온 교차 의존이 정상으로 굳는다.

| 잃은 게이트 | 대체 규칙 | 위치 |
|---|---|---|
| application → 다른 앱 application | `AppIsolationTest#appsShouldNotDependOnEachOther` | 이 모듈 |
| api 어댑터 → 다른 앱 application | `adaptersShouldOnlyUseOwnAppUseCases` | api 4모듈 각각 |

## 덩어리 02/03a — 벤더 포트·서비스 4종을 `domain`에서 이관

**파일·메일·SMS·결제 4개 컨텍스트의 아웃바운드 포트와, 그것을 쓰는 도메인 서비스 4개가 `domain`을 떠나 이 모듈로 옮겨왔다.** 소셜 로그인 SPI(`auth.port.out`)·BBQ 크롤링 포트가 이미 이 모듈에 있던 것과 같은 형태로, "벤더 무관 계약을 `domain`이 갖고 외부 연동 모듈이 구현한다"는 배치를 "계약을 유스케이스 계층이 갖고 외부 연동 모듈이 구현한다"로 확장한 것이다.

| 컨텍스트 | 이관된 포트 | 이관된 서비스 | 이전 위치 |
|---|---|---|---|
| file | `file/port/out/FileStoragePort`(+`FileDeleteResult`) | `file/service/FileUploadService`(+`FileUploadCommand`) | `domain.file.port.FileStoragePort`, `domain.file.service.FileUploadService` |
| mail | `mail/port/out/MailSender`(+`MailSendResult`) | `mail/service/MailVerificationService`(+package-private `MailVerificationMessage`) | `domain.mail.port.MailSender`, `domain.mail.service.MailVerificationService` |
| sms | `sms/port/out/SmsSender`(+`SmsSendResult`, enum `SmsSendFailure{NO_RESPONSE,FAILED,API_ERROR}`) | `sms/service/SmsVerificationService` | `domain.sms.port.SmsSender`, `domain.sms.service.SmsVerificationService` |
| payment | `payment/port/out/{PgPaymentGateway,PgProviderGateway}` + `PgConfirmResult`·`PgCancelResult`·`TossPaymentDetail` + 신설 enum `PgProviderCode` | `payment/service/{PaymentConfirmationService,PgPaymentGatewayRouter}`(+`PgConfirmation`·`PgConfirmationTarget`) | `domain.payment.port.{PgPaymentGateway,PgProviderGateway}`, `domain.payment.service.{PaymentConfirmationService,PgPaymentGatewayRouter}` |

**왜 서비스까지 옮겼나 — 포트만으로는 끝나지 않는다.** 이 네 서비스는 순수 POJO라는 점에서 기존 도메인 서비스와 다르지 않지만, 이관된 포트를 생성자로 주입받는다. 포트가 `application`으로 옮겨간 채 서비스만 `domain`에 남으면 `domain`이 `application`의 포트 인터페이스를 참조해야 해 **의존 방향이 뒤집힌다**(안쪽이 바깥쪽을 아는 상태). 그래서 포트를 쓰는 서비스 자체도 함께 옮겼다 — "이 서비스가 도메인 불변식을 오케스트레이션하는가"라는 기존 배치 기준(`domain/AGENTS.md`의 "도메인 서비스(`<ctx>/service/`)는 순수 POJO다" 절)은 여전히 참이지만, **아웃바운드 포트가 domain 밖에 있으면 그 포트를 쓰는 오케스트레이션도 domain 밖에 있어야 한다**는 조건이 우선한다.

- **`file`·`payment`는 `@SharedApp`로, `mail`·`sms`는 `@WebApp`으로 등록한다** — 발송 포트 구현이 web에만 있는 기존 배치 기준(`persistence/AGENTS.md`의 "구현이 일부 앱에만 있는가" 판정)을 그대로 승계했다. `file`은 4앱 전부가 `FileStoragePort` 구현(firebase/aws-s3)을 가지므로 공유, `payment`(`PaymentConfirmationService`)는 PG 결제 승인이 web에서만 일어나지만 ~~**읽기 계약과 도메인 이벤트 리스너처럼 "언젠가 다른 앱이 같은 유스케이스를 트리거해도 안전해야 한다"는 4개 리스너 배치 원칙과 같은 이유로 `@SharedApp`을 유지한다** — `PgPaymentGatewayRouter`만 web 전용 채널(`infrastructure:pg`가 web-api에만 조립)이라 별도로 `@WebApp`인 `PgRouterConfig`가 등록한다.~~ **(번복됨 — application `*ServiceConfig` 삭제)** "다른 앱이 트리거해도 안전하도록 미리 `@SharedApp`"이라는 판단은 버렸다. 지금 마커는 **현재 소비 앱 집합**으로 정한다 — `PaymentConfirmationService`는 `@SharedApp` 리스너 `PaymentEventListener`가 쓰므로 `@SharedApp`으로 남았지만, `PaymentCancellationService`는 소비자가 web(당시 `PaymentCommandService` — 지금은 `PaymentRefundRequestService` — ·`PaymentCancellationExecutor`)뿐이라 `@WebApp`으로 좁혀졌다. 다른 앱이 필요해지면 그때 마커를 `@SharedApp`으로 올리면 되고, 올리지 않고 주입하면 `AppIsolationTest#constructorDependenciesShouldBeVisibleToApp`이 빌드에서 잡는다(미리 넓혀 두는 것은 쓰지 않는 앱에 빈을 띄우는 비용만 있다). `PgPaymentGatewayRouter`도 config 없이 클래스의 `@WebApp` 마커로 등록된다.
- **등록 클래스 5종은 모두 `@Configuration` + 마커, `@Bean` 팩토리 하나(또는 관련 빈 여러 개)**: `file/config/FileServiceConfig`(`@SharedApp`, `fileUploadService`) · `mail/config/MailServiceConfig`(`@WebApp`, `mailVerificationService`) · `sms/config/SmsServiceConfig`(`@WebApp`, `smsVerificationService`) · `payment/config/PaymentServiceConfig`(`@SharedApp`, `paymentConfirmationService`) · `payment/config/PgRouterConfig`(`@WebApp`, `pgPaymentGatewayRouter` — `List<PgProviderGateway>`를 주입받아 라우터를 조립). 이 다섯이 위 "(번복) `@SharedApp` 허용 대상 확대" 절이 예고한 **"리스너 외 첫 사용처"**다 — `LayerRulesTest#sharedConfigsShouldOnlyDeclareUnmarkedBeans`가 처음으로 실제 대상(`file/payment`의 두 `@SharedApp` 설정)을 갖게 됐다. **(번복됨 — application `*ServiceConfig` 삭제)** 이 다섯 설정은 전부 삭제됐고, 각 서비스 클래스에 마커만 붙는다(`FileUploadService`·`PaymentConfirmationService` `@SharedApp`, `MailVerificationService`·`SmsVerificationService`·`PgPaymentGatewayRouter`·`PaymentCancellationService` `@WebApp`).
- **`FileDomainConfig`·`PaymentDomainConfig`의 관련 빈은 `infrastructure:persistence`에서 삭제됐다.** ~~`PaymentDomainConfig`는 `paymentCancellationService`(도메인에 남은 `PaymentCancellationService`용) 하나만 남았다.~~ **(03a로 소멸)** 남은 `paymentCancellationService`도 서비스와 함께 이 모듈로 와 `PaymentServiceConfig`에 합쳐졌고, persistence의 `*DomainConfig`는 0개가 됐다(아래 "덩어리 03a" 절). `MailDomainConfig`/`SmsDomainConfig`가 이미 채널 모듈(`infrastructure:mail`/`infrastructure:sms`)로 옮겨가 있던 선례와 마찬가지로, 판정 기준은 "외부 연동 포트인가"가 아니라 "이 서비스가 지금 어디 있는가"다.
- **`PgProviderGateway.provider()`는 domain `PgProvider`가 아니라 이 모듈 신설 enum `PgProviderCode`를 반환한다.** `PgPaymentGateway`(라우터가 구현하는, 소비 측이 호출하는 계약)는 여전히 domain `PgProvider`를 쓴다 — 라우터(`PgPaymentGatewayRouter`)가 `PgProviderCode.name()` → `PgProvider.valueOf(...)`로 두 enum을 **상수명으로만** 연결한다. 벤더(`infrastructure:tosspayments`)가 `PgProviderGateway`를 구현하며 domain을 몰라도 되게 하려는 것이 이 우회의 목적이다 — `PgProviderCode`가 `domain`을 참조하지 않으므로 벤더 모듈도 `domain` 의존 없이 채널 어댑터를 만들 수 있다. **두 enum은 상수명·순서가 항상 같아야 하며**, `application/src/test/.../architecture/EnumCodeConstantsTest#pgProviderCodeMatchesPgProvider`가 `Enum::name` 배열을 대조해 어긋남을 잡는다. 한쪽에만 상수를 추가하면 이 테스트가 즉시 실패한다(라우터의 `PgProvider.valueOf(code.name())`이 매핑되지 않는 상수에서 `IllegalArgumentException`을 내는 런타임 위험의 컴파일 타임 방어선).
- **`SocialOAuthClient` SPI가 예외 없는 `Optional`형 결과로 바뀌었다** — `exchange`/`fetchProfile`이 이제 예외를 던지지 않고 `SocialOAuthResult<T>`(`value` XOR `failure`인 record, compact constructor가 강제)를 반환한다. 실패는 enum `SocialOAuthFailure{APPLE_ID_TOKEN_INVALID,ACCESS_TOKEN_REJECTED}`로 표현하고, 4개 `*SocialLoginService`는 `.orElseThrow(SocialOAuthFailures::toException)`로 소비한다. 신설 `auth/service/SocialOAuthFailures`(정적 유틸)가 `APPLE_ID_TOKEN_INVALID → WebErrorCode.APPLE_ID_TOKEN_INVALID`·`ACCESS_TOKEN_REJECTED → WebErrorCode.SOCIAL_OAUTH_FAILED`(에러코드 모듈 분할 후 — 과거 `ErrorCode`)로 매핑한다 — 카카오·네이버는 항상 `success(...)`로 감싸 던지던 예외를 값으로 옮겼을 뿐이고, 응답 계약(`ErrorCode` 문자열·HTTP 상태)은 이전과 동일하다.
- **`RemoteImagePort.download`도 같은 형태로 전환됐다** — `ImageDownloadResult(image, failure)` record(`image` XOR `failure`)를 반환하고, 실패는 enum `ImageDownloadFailure{EMPTY,SIZE_EXCEEDED}`로 표현한다. `BbqService`가 `success()`를 확인해 각각 `ApplicationErrorCode.FILE_EMPTY`/`FILE_SIZE_EXCEEDED`로 번역한다(에러코드 모듈 분할 후 — 과거 `ErrorCode`) — 과거 `RemoteImagePort`가 던지던 예외를 값으로 옮긴 것으로, 위 소셜 OAuth SPI 전환과 동일한 패턴이다.
- **`AdminDongBoundaryPort.fetchAll()`도 결과 record로 전환됐다** — `AdminDongBoundaryFetchResult(sources, failed)`를 반환하고, `AdminDongBoundarySource`는 더 이상 domain `GeoPoint`/`GeoRing`을 담지 않는다. 대신 `List<BoundaryRing>`(`BoundaryRing(List<BoundaryCoordinate>)`, `BoundaryCoordinate(double latitude, double longitude)`)라는 이 모듈 소유의 좌표 전용 타입을 담는다 — 어댑터(`infrastructure:admdongkor`)는 원시 좌표만 돌려주고, `GeoRing` 조립(퇴화 링 스킵)·중심점 계산(`InteriorPoint`)·중심점 없는 행 스킵·전량 실패시 `ADMIN_DONG_BOUNDARY_FETCH_FAILED`는 전부 `region/service/AdminDongSchedulerService`(이 모듈)가 수행한다. 어댑터가 domain 기하 타입을 몰라도 되게 하려는 것이 이 분리의 목적이며, 위 `PgProviderCode`·`SocialOAuthFailure`와 같은 "어댑터는 벤더 무관 원시 타입만, 판단은 유스케이스 계층"이라는 원칙의 반복 적용이다.
- **ArchUnit 신설 2종**: `AppIsolationTest#sharedBeansShouldNotDependOnWebOnlyServices`(`@SharedApp` 빈은 `MailVerificationService`·`SmsVerificationService`·`PgPaymentGatewayRouter`·`PgPaymentGateway`를 의존하지 않는다 — web 전용 설정만 등록하는 서비스를 공통 빈이 주입하면 admin·ceo·batch가 그 빈을 못 찾아 기동하지 못한다는 것을 빌드 시점에 잡는다) · `architecture/EnumCodeConstantsTest`(위 `PgProviderCode`↔`PgProvider` 대조, 현재 1개 케이스). **(번복됨 — 앱 마커 제거)** `sharedBeansShouldNotDependOnWebOnlyServices`는 `AppIsolationTest`와 함께 삭제됐다 — 그 4개 타입이 지금 `web-application`에 있어 코어 빈은 컴파일 단계에서 볼 수 없다.
- **domain 쪽 정리**: `domain/AGENTS.md`의 `ContextBoundaryTest.SEALED_VIOLATIONS`에서 `MailVerificationService`·`PaymentConfirmationService` 2건이 빠졌다(대상이 domain 밖으로 나갔으므로 봉인 목록도 그 파일의 관할이 아니게 됐다) — 16개 → 14개.

## 덩어리 03a — 나머지 포트·도메인 서비스·이벤트 발행기를 `domain`에서 이관

**02가 확립한 "마커 없는 POJO + 마커 붙은 `@Configuration`" 등록 방식을 나머지 전부에 반복했다.** **(번복됨 — application `*ServiceConfig` 삭제)** 이 등록 방식은 이후 걷어냈다 — 아래 표의 "빈 등록 18개"·"서비스 연결 어댑터"·"구현 `SpringDomainEventPublisher`" 행은 03a 시점 기록이고, 현행은 위 "application `*ServiceConfig` 전면 삭제" 절이다. 그 결과 `domain`에는 모델·VO·이벤트 타입·포트 없는 순수 계산기/정책·공유 커널·예외만 남고, 포트와 포트를 주입받는 서비스는 전부 이 모듈에 있다. **클래스 위치와 빈 등록 위치만 바꿨고 로직은 한 줄도 바꾸지 않았다**(HTTP·DB 계약 불변). 이 덩어리가 끝나도 persistence는 아직 `:domain`을 의존한다 — write 포트 시그니처가 여전히 domain 모델(`Optional<Notice> findById(NoticeId)`)이기 때문이며, 시그니처를 State record로 바꾸는 것은 03b다. **(03b로 해소 — 아래 "덩어리 03b" 절)** **(다시 번복됨 — persistence domain 재허용)** 지금 persistence는 다시 `:domain`을 `implementation`으로 의존하고, write 포트 시그니처는 03a 그대로 domain 모델이다.

| 항목 | before | after |
|---|---|---|
| write 포트 106개 + 보조 타입 2개(`AdminDongSyncResult`·`ShopDeliveryTipRegionLookupPort` — 이후 네이밍 전환에서 `ShopDeliveryTipRegionLookupPort`는 쓰기 포트로 분류돼 `writePortsExist`에 포함된다) | `domain/<ctx>/repository/` | **`<ctx>/port/out/write/`** (`member/follow`·`member/referral`처럼 한 겹 더 있으면 경로를 그대로 따른다 — `member/follow/port/out/write/`) |
| 출력 포트 7개 + record 3개 | `domain/<ctx>/port/` | `<ctx>/port/out/` (`product`·`member`·`rank`·`search`·`ceo`) |
| 포트를 주입받는 서비스 71개 | `domain/<ctx>/service/` | `<ctx>/service/` (마커 없는 POJO). 순수 서비스가 이동 서비스를 주입하면 함께 옮겼다(`ShopRiderGuideValidator` — domain은 application을 볼 수 없다). package-private 문구 유틸 `NotificationMessage`도 유일한 사용처 `NotificationService`와 함께 옮겼다(02의 `MailVerificationMessage` 선례) |
| 빈 등록 18개 | `infrastructure:persistence`의 `<ctx>/config/<Ctx>DomainConfig`(마커 없음 — 4앱 전부 스캔) | **`<ctx>/config/<Ctx>ServiceConfig`** (`@Configuration(proxyBeanMethods = false) @SharedApp` — 등록 앱 불변). 17개는 `git mv` + 개명, `PaymentDomainConfig`의 `paymentCancellationService`는 이미 있던 `payment/config/PaymentServiceConfig`에 합쳤다. **`@Bean` 메서드 이름은 그대로**(빈 이름이 바뀌면 이름 기반 주입이 깨진다). 예외는 발행기 하나다 — `@Component` 기본 이름 `springDomainEventPublisher`가 `@Bean domainEventPublisher`로 바뀌었고, 그 이름을 `@Qualifier`로 참조하는 곳은 0건이다. domain에 남은 순수 서비스(`CupDepositPolicy`·`ProductExposureCalculator`·`ShopDeliveryTipCalculator` 등)의 `@Bean`도 이 설정들이 그대로 갖는다 — 클래스는 domain, 등록은 application |
| 이벤트 발행 포트 `DomainEventPublisher` | `domain/shared/event/` | `shared/event/` |
| 구현 `SpringDomainEventPublisher` | persistence `shared/event/`, 마커 없는 `@Component` | `shared/event/`, `@Component` 제거, **`shared/config/SharedEventConfig`(`@SharedApp`)의 `@Bean domainEventPublisher`**. persistence에 두면 persistence가 `port.out` 밖의 application 타입을 봐야 해 `shouldNotDependOnApiModules`에 걸리고, application 설정이 persistence 구현을 `new`할 수는 없다(모듈 의존 없음). 구현이 `ApplicationEventPublisher`에 위임하는 한 줄이라 이 모듈(이미 spring-context 보유)이 자연스럽다 |
| `OptimisticLockConflictException` | `domain/shared/exception/` | **`shared/port/out/`** — persistence `ReservationSlotPersistenceAdapter`이 던지고 web-application `ReservationCreateService`(당시 `ReservationCommandService`)가 잡는다. `port/out` 아래여야 03b에서 persistence가 domain 없이 던질 수 있다. `BusinessException`이 아니므로 응답은 여전히 500이고, 재시도 소진 시 `RESERVATION_SLOT_FULL` 409로 번역되는 흐름도 그대로다 |
| 서비스 연결 어댑터 2개(`ShopRequestIndexSyncAdapter`·`ReplyPhraseProhibitedWordValidatorAdapter`) | persistence `product/persistence`·`ceo/persistence`, `@Component` | **`shop/service/`** POJO, `ShopServiceConfig`가 `@Bean`(같은 빈 이름) 등록. DB 기술이 아니라 서비스 연결부라 persistence에 두면 infra가 application 서비스를 부르게 된다. **포트를 소유한 쪽(product·ceo)이 아니라 구현 대상 서비스가 사는 `shop`에 둔 이유**: product·ceo 쪽에 두면 어댑터가 shop의 `service`를 직접 참조해 `ServiceContextBoundaryTest` 위반이 되고 봉인 목록은 늘릴 수 없다. shop에 두면 "shop이 product·ceo의 포트(`port.out`)를 구현"하는 허용된 방향만 남는다 |
| 금칙어 캐시 데코레이터 `CachingProhibitedWordPersistencePort` | persistence `shop/persistence` | `shop/service/` (순수 자바 TTL 캐시, 빈 아님). `ShopServiceConfig#prohibitedWordValidator`가 `new`로 감싸는데 그 설정이 이 모듈로 왔으므로 함께 옮겼다 |

**ArchUnit 변경**

| 규칙 | before | after |
|---|---|---|
| `LayerRulesTest#queryServicesShouldNotDependOnWritePorts` | 대상 `com.tastyhouse.domain..repository..` | `com.tastyhouse.application..port.out.write..` — 바꾸지 않으면 대상 패키지가 사라져 **아무것도 검사하지 않고 통과**한다 |
| `RuleAnchorTest#writePortsExist` | 없음 | **신설** — `..port.out.write..`의 `*Repository` 인터페이스 ≥ 106. 위 규칙이 대상을 잃으면 빌드 실패 |
| `LayerRulesTest#readContractsShouldBeFrameworkFree` | `port.out`은 `java`·`domain`·`port.out`만 참조 | **그대로**(write 포트가 domain 모델을 참조하므로 domain 허용 유지). 03b에서 domain 허용을 제거한다 — **(03b로 완료)** `java..`·`port.out..`만 허용. **(persistence domain 재허용)** 읽기 계약은 그대로 두고 `port.out.write`만 대상에서 빼, 신설 `writePortsShouldOnlyDependOnDomainAndPortOut`이 따로 검사한다 |
| `ServiceContextBoundaryTest` | 없음 | **신설** — 아래 절 |
| domain `ContextBoundaryTest` | 봉인 위반 14 · 순환 성분 1 | 봉인 위반 **1**(`DeliveryAreaProjection`) · 순환 봉인 **해제**(성분 0개 → `beFreeOfCycles()` 순수 강제) — `domain/AGENTS.md` |
| persistence `LayerRulesTest#sealedPersistenceToQuery` | 봉인 3건 | **3건 유지** — 포트가 application으로 옮겨가도 어댑터(`*Adapter`)는 persistence에 남아 여전히 `..query..`를 본다 |

### `ServiceContextBoundaryTest` — 도메인 서비스 사이의 컨텍스트 경계

**대상**: `backend/application/src/test/java/com/tastyhouse/application/architecture/ServiceContextBoundaryTest.java`

domain `ContextBoundaryTest`가 도메인 서비스에 걸던 컨텍스트 경계를, 서비스가 옮겨온 이 모듈에서 **같은 구조(봉인 목록 + 짝 테스트)**로 이어서 강제한다. 옮기기만 하고 규칙을 따라 옮기지 않으면 경계가 조용히 사라진다.

- **검사 대상**: `com.tastyhouse.application.<ctx>..service..`에 있는 최상위 클래스 중 **`@Service`/`@Component`/`@Configuration`이 없고 이름이 `*CommandService`/`*QueryService`로 끝나지 않는 것**(03a 시점 판정 — 지금은 아래 "앱 모듈 경계 가드"의 `EXCLUDED_COLLABORATORS` 항목대로 "구조 조건(`port.in` 구현 아님 등) − 협력 빈 FQN 목록"이고, 이름 접미어 절은 유스케이스 분리로 삭제됐다) — 즉 ~~마커 없는 POJO~~ 스테레오타입 없는 도메인 서비스(ServiceConfig 삭제 후에는 앱 마커만 단다 — 마커는 스테레오타입이 아니라 이 판정에 영향이 없다)와 그 협력 record·유틸. 유스케이스 서비스(`*CommandService`·`*QueryService`, 마커 부착)는 원래 컨텍스트를 가로질러 조립하는 계층이라 대상이 아니다. 하한 `domainServicesShouldExist` ≥ 71.
- **컨텍스트 판정**: 패키지 두 번째 세그먼트(`com.tastyhouse.{domain|application}.<ctx>.`). `shared`·`exception`·`architecture`는 컨텍스트가 아니다. `application.member.follow`처럼 03a가 domain 경로를 그대로 옮긴 곳은 `member` 컨텍스트이고, 원래부터 이 모듈에 있던 `application.follow`는 별개 컨텍스트다.
- **금지**: 타 컨텍스트의 domain `model`·`service`, application의 `port.out` 밖 전부(`port.out.write`(구 `repository`)·`service`·`port.in`·`listener`·`config` 등). **허용**: domain `vo`·`event`, application `port.out`(write 제외). 대상 서비스의 **중첩 클래스도 검사**하며 위반은 최상위 클래스 이름으로 모아 봉인 목록과 대조한다(domain `ContextBoundaryTest`의 `topLevelNameOf` 선례). 의존 대상도 최상위 클래스로 접어서 센다. 한계: 컴파일 타임 상수(`static final` 원시값·문자열)는 javac가 인라인해 바이트코드 의존이 남지 않으므로 잡히지 않는다(`NotificationMessage` → `ReviewBlindRequest.BLIND_PERIOD_DAYS`) — domain 테스트와 같은 한계다.
- **봉인 위반 15개** — domain에서 옮겨 온 위반만 담는다: 03a 직전 domain 봉인 14개에서 domain에 남은 `DeliveryAreaProjection`을 뺀 13개 + 덩어리 02가 옮기며 domain 봉인에서만 빠지고 새 규칙이 없던 `MailVerificationService`·`PaymentConfirmationService`. 그중 ceo `ShopRequestCancelService`는 유스케이스 분리로 `ShopRequestCancellationService`로 개명돼 FQN만 바뀌었다(항목 수 불변). **목록은 줄어들기만 한다 — 항목을 추가하지 않는다.** 짝 테스트 `sealedViolationsShouldNotBeStale`(해소된 항목 검출)·`sealedViolationListShouldNotBeEmpty`(다 비면 봉인 장치 제거 지시).
- **봉인 순환 성분 1개** `"order,product,review,shop"` — domain에서 봉인되던 바로 그 성분이 서비스와 함께 옮겨왔다(순환을 만들던 간선이 전부 서비스에서 나왔다). 간선은 대상 서비스에서 나가는 의존만으로 계산하고, 비교 단위는 domain 선례대로 **SCC**다. 짝 테스트 `sealedCyclesShouldNotBeStale`.
- `allowEmptyShould(true)`를 쓰지 않는다 — 위반을 손으로 모으는 테스트라 ArchUnit의 빈 `should()` 문제도 없다.
- ~~**(03b)** 새로 생긴 `<ctx>/store/`도 `port.out` 밖이므로 이 규칙의 금지 대상에 자동으로 들어간다 — 타 컨텍스트의 `XxxPersistencePort`는 03a 이전과 마찬가지로 직접 주입할 수 없다(패키지가 `port.out.write` → `store`로 바뀌었을 뿐 판정은 같다).~~ **(번복됨 — persistence domain 재허용)** `store`는 없어지고 `XxxPersistencePort`가 `port.out.write`로 돌아왔다. 위 "금지"의 `port.out.write`가 그대로 적용되므로 판정은 03a와 같다.

## persistence domain 재허용 — State 계열 제거 (03b 쓰기 경로 번복)

**03b가 쓰기 경로에 끼운 번역 계층(State·Snapshot·StatePort·Store·StateMapper)을 전부 걷어내고, persistence가 도메인 모델을 직접 쓰도록 되돌렸다.** 읽기 경로(읽기 계약 `port/out`의 `*Result`·`*SearchCondition`, persistence 조회 DAO)는 03b의 domain-free 상태 그대로다. **동작 변경은 없다** — HTTP 응답, DDL, 저장값, null 처리, load-copy-save가 모두 그대로이고, Entity → State → Domain 두 단계 변환을 Entity → Domain 한 단계로 합쳤을 뿐이다.

```
03b  : Service → XxxPersistencePort(도메인) ← XxxStore ─(XxxStateMapper)→ XxxStatePort(State) ← persistence XxxStatePortImpl
현행 : Service → XxxPersistencePort(도메인) ← persistence XxxPersistenceAdapter (XxxMapper: JpaEntity ↔ Domain)
```

**왜 되돌렸나**: 03b 구조에서는 애그리거트 하나를 저장하려고 파일 5개(State·StatePort·Store·StateMapper·StatePortImpl)를 거쳤다(State 122 · Snapshot 7 · StatePort 105 · Store 105 · StateMapper 121 · StateMapper 테스트 81). 이 비용으로 얻는 것은 "persistence가 domain을 모른다"는 격리였지만, persistence는 원래 도메인 포트의 어댑터라 domain을 아는 방향이 의존 규칙에 맞고, `implementation` 의존이면 domain이 앱 컴파일 클래스패스로 새지 않아 덩어리 01의 presentation 절단도 유지된다.

**엔티티의 `String` 컬럼과 `*Embeddable` 5종은 유지한다**: 조회 DAO 31개 파일이 String 컬럼을 193곳에서 쓴다. enum으로 되돌리면 `Projections.constructor` 투영이 **컴파일은 통과하고 런타임에만** 깨진다. 그래서 String ↔ enum(`valueOf`/`name()`) 변환은 persistence `XxxMapper`의 몫이다.

### 한 컨텍스트의 파일 구성 — `notice`

| 역할 | 파일 | before (03b) | after |
|---|---|---|---|
| 서비스가 주입하는 도메인 타입 인터페이스 | `backend/application/src/main/java/com/tastyhouse/application/notice/port/out/write/NoticePersistencePort.java` | `store/NoticePersistencePort` | **`port/out/write/`로 복귀** — 이름·시그니처 불변 |
| 상태 record · 원시 타입 포트 | `port/out/write/NoticeState`·`NoticeStatePort` | 있음 | **삭제** |
| Store · StateMapper | `store/NoticeStore`·`NoticeStateMapper` | 있음 | **삭제**(`store` 패키지 전체 삭제) |
| 등록 | `config/NoticeServiceConfig` | `@SharedApp` 설정의 `@Bean`으로 Store 등록 | **삭제** — Store 빈만 있던 파일은 파일째 삭제 |
| persistence 구현 | `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/persistence/notice/persistence/NoticePersistenceAdapter.java` | `NoticeStatePortImpl`(State 반환) | `NoticePersistenceAdapter`(`@Repository`, **`NoticePersistencePort` 구현, 도메인 반환**) |
| persistence 매퍼 | `.../notice/persistence/NoticeMapper.java` | JpaEntity ↔ `NoticeState` | **JpaEntity ↔ `Notice`** — StateMapper의 표현식을 흡수 |
| 매퍼 테스트 | `backend/infrastructure/persistence/src/test/java/com/tastyhouse/infrastructure/persistence/<ctx>/persistence/XxxMapperTest.java` | application `store/*StateMapperTest`(왕복 비교) | persistence로 이관 — Domain→Entity / Entity→Domain **두 방향 따로**(아래) |

규모: 삭제 State 122 · Snapshot 7 · StatePort 105 · Store 105 · StateMapper 121. `port/out/write/`에는 옮겨 온 `XxxPersistencePort` 105개와 `ShopDeliveryTipRegionLookupPort`, 원래 있던 `StationPersistencePort`·`AdminDongSyncResult`가 남는다. persistence `XxxPersistenceAdapter`은 106개(`StationPersistenceAdapter` 포함, 03b 이전과 같음).

### write 어댑터 작성 규칙 (현행)

- **`XxxPersistencePort`는 `<ctx>/port/out/write/`에 둔다.** 시그니처는 도메인 타입(`Optional<Notice> findById(NoticeId)`)이고, 구현은 persistence `<ctx>/persistence/XxxPersistenceAdapter`(`@Repository`, 4앱 부트스트랩 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔으로 4앱 전부에 뜬다 — ~~`PersistenceModuleAutoConfiguration`의 스캔~~ 번복됨, imports 제거)이다. 이 모듈에는 구현을 두지 않는다.
- **write 포트는 `java..`·`com.tastyhouse.domain..`·`com.tastyhouse.application..port.out..`만 의존한다** — `LayerRulesTest#writePortsShouldOnlyDependOnDomainAndPortOut`. 서비스·UseCase·설정을 참조하면 persistence가 그 타입까지 봐야 해 `shouldNotDependOnApiModules`에 걸린다.
- **변환은 persistence `XxxMapper`가 한다** — `toDomain(entity)`(`reconstitute` 호출)·`toEntity(domain)`·`applyChanges(entity, domain)`. enum은 `valueOf`/`name()`, ID·단일값 VO는 `Xxx.of(...)`/`.value()`, 복합 VO는 persistence 소유 `*Embeddable` 또는 평탄 컬럼으로 바꾼다. nullable enum·VO·FK는 `x == null ? null : ...` 삼항 가드를 **모든 FK에 예외 없이** 둔다(`backend/CLAUDE.md` "ID VO 경계 규칙").
- **Store에 있던 로직은 PersistenceAdapter로 옮겼다** — 빈 컬렉션 조기 반환, `LinkedHashSet` 수집, 도메인 정책 상수 호출(예: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/persistence/reservation/persistence/ReservationPersistenceAdapter.java`가 `ReservationStatus.blockingStatuses()`를 `name()` 목록으로 풀어 쿼리에 넘긴다). 인터페이스 둘을 구현하던 Store(`ShopDeliveryTipStore`)는 PersistenceAdapter도 `ShopDeliveryTipPersistencePort`·`ShopDeliveryTipRegionLookupPort` 둘을 구현한다.
- **Store 빈 등록 설정은 없다** — ~~`<Ctx>ServiceConfig`에는 Store가 아닌 빈(도메인 서비스·어댑터·정책 record)만 남는다.~~ **(번복됨 — application `*ServiceConfig` 삭제)** 남아 있던 `<Ctx>ServiceConfig`도 전부 삭제됐다. Store 빈만 갖던 `Admin`·`Banner`·`Event`·`Notice`·`Partnership`·`Region`의 `*ServiceConfig`는 파일째 삭제됐다. `@WebApp`이던 `MailServiceConfig`·`SmsServiceConfig`의 `MailVerificationPersistencePort`·`SmsVerificationPersistencePort` 구현은 이제 `@Repository` 스캔으로 전 앱에 뜬다 — 03b 이전과 같은 상태이고 admin·ceo·batch에는 주입처가 없다.
- **서비스 테스트의 Repository fake는 import만 바뀌었다** — 도메인 타입 시그니처가 불변이므로 `FakeMailVerificationPersistencePort` 등은 그대로다.

### 매퍼 테스트는 두 방향으로 나눠 검증한다

03b의 `*StateMapperTest`는 `toDomain(toState(original))`을 `usingRecursiveComparison()`으로 원본과 비교하는 왕복 방식이었다. Entity를 거치면 원본의 일부 값이 사라진다 — `XxxJpaEntity.create(...)`는 id를 받지 않고, `BaseEntity`의 `createdAt`/`updatedAt`은 auditing이 채우며 setter가 없다. 그래서 persistence `XxxMapperTest`는 다음처럼 검증한다.

- (a) Domain → Entity: `toEntity` 결과의 컬럼 값(enum `name()`, VO `value()`, Embeddable 필드)을 필드별로 단언한다.
- (b) Entity → Domain: `ReflectionTestUtils.setField`로 id·`createdAt`·`updatedAt`을 채운 엔티티를 만들고 `toDomain` 결과를 단언한다.
- 왕복 비교를 남긴다면 제외 필드는 `ignoringFields("id", "createdAt", "updatedAt")`만 허용한다. 다른 필드를 제외해 테스트를 통과시키지 않는다.
- null 가드 케이스(enum·VO가 null)는 두 방향 모두 유지하고, **같은 타입의 연속 필드는 서로 다른 값으로 채운다**(아래 봉인 항목).

### ArchUnit·테스트 변경 (persistence domain 재허용)

| 규칙·테스트 | before (03b) | after |
|---|---|---|
| persistence `LayerRulesTest#infrastructureShouldNotDependOnDomain` | persistence 전체 → domain 금지 | **대상 축소·개명** `queryShouldNotDependOnDomain` — `..query..` + 봉인 조회 어댑터 3개(`SEALED_PERSISTENCE_TO_QUERY`)만 domain 금지(`../infrastructure/persistence/AGENTS.md`) |
| `LayerRulesTest#readContractsShouldBeFrameworkFree` | `port.out..` 전체가 `java..`·`port.out..`만 의존 | 대상에서 `..port.out.write..`만 제외(읽기 계약은 domain-free 그대로) |
| `LayerRulesTest#writePortsShouldOnlyDependOnDomainAndPortOut` | 없음 | **신설** — `port.out.write`는 `java..`·`com.tastyhouse.domain..`·`application..port.out..`만 |
| `LayerRulesTest#queryServicesShouldNotDependOnWritePorts` | 대상 `..port.out.write..` + `..store..` | `..port.out.write..`만 |
| `RuleAnchorTest#storesExist` | Store ≥ 105 | **삭제** |
| `RuleAnchorTest#writePortsExist` | `port.out.write` + `store`의 `*Repository` 합계 ≥ 106 | `port.out.write`의 `*Repository` ≥ 106 |
| `StateRecordArityTest` | State record 컴포넌트 수 검사 | **파일 삭제** |
| 컨텍스트별 `store/*StateMapperTest` 81개 | application | **persistence `XxxMapperTest`로 이관**(위 두 방향 검증) |

## application `*ServiceConfig` 전면 삭제 — 도메인 서비스는 클래스에 앱 마커만 (02/03a 등록 방식 번복)

> **(번복됨 — 앱 마커 제거)** "config 삭제"는 유지되지만 "클래스에 앱 마커만, `@Service` 없이"는 번복됐다. 도메인 서비스 78개는 지금 **`@Service`를 달고**, 마커 대신 **모듈 위치**로 소속을 표현한다(소비 앱이 하나면 그 `{앱}-application`, 둘 이상이면 코어 — 과거 `@SharedApp` 35개가 코어에 남았다). `@Service`를 달아도 `ServiceContextBoundaryTest`가 대상을 잃지 않도록 술어를 "구조 조건 − `EXCLUDED_COLLABORATORS`"로 바꿨다. 아래 ArchUnit 표의 `AppIsolationTest`·마커 규칙은 전부 삭제됐다. **(유스케이스 분리)** 아래 용어 풀이의 "도메인 서비스 = `*CommandService`/`*QueryService`가 아닌 서비스"·"앱 오케스트레이터 = `*CommandService`/`*QueryService`"는 당시 기준이다 — 지금은 `port.in`을 구현하면 유스케이스 서비스, 아니면 도메인 서비스다. 현재 규칙은 [앱 마커 제거 — 앱 모듈 재분리](#앱-마커-제거--앱-모듈-재분리-챕터-01-통합챕터-03-마커-번복).

**02/03a가 확립한 "마커 없는 POJO + 마커 붙은 `@Configuration`의 `@Bean`" 등록 방식을 걷어냈다.** `<ctx>/config/*ServiceConfig` 22개와 `payment/config/PgRouterConfig`를 전부 삭제했고, 그 설정들이 `new`로 만들던 도메인 서비스 78개는 이제 **클래스에 앱 마커 하나만**(`@WebApp`/`@AdminApp`/`@CeoApp`/`@BatchApp`/`@SharedApp`, `@Service`는 달지 않는다) 달고 마커 기반 컴포넌트 스캔(아래 "빈 배선" 절)으로 등록된다. HTTP·DB 계약과 빈 이름은 바뀌지 않았다.

**용어 풀이**

- **도메인 서비스**: `<ctx>/service/`에 있으면서 `*CommandService`/`*QueryService`가 아닌 서비스. 트랜잭션을 열지 않고(`@Transactional` 없음) 여러 애그리거트·포트를 묶어 도메인 불변식을 지킨다. 03a로 domain에서 옮겨 온 것들이다.
- **앱 오케스트레이터**: `*CommandService`/`*QueryService`(batch는 `*SchedulerService`). `@Service` + 앱 마커를 달고 UseCase를 구현하며 트랜잭션 경계를 갖는다.
- **마커-only 클래스**: 스테레오타입(`@Service`/`@Component`/`@Configuration`) 없이 앱 마커만 단 클래스. 스캔 필터가 `useDefaultFilters = false` + 마커 `ANNOTATION`이라 마커 하나만으로 빈이 된다.
- **소비 앱 집합**: 생성자 주입을 따라 올라가 "최종적으로 이 빈을 쓰는 앱"의 집합. 마커를 정하는 기준이다.

**왜 바꿨나**

- 서비스 하나를 추가할 때 파일 두 곳(클래스 + config의 `@Bean`)을 고쳐야 했다.
- `@SharedApp` config가 등록하던 탓에 **한 앱만 쓰는 빈도 4앱 전부에 떴다**(web·admin·ceo·batch 어디에도 주입처가 없는 빈이 컨텍스트에 상주).
- config를 못 없앤 원인은 규칙 3개였다 — `sharedAppOnlyOnListeners`(`@SharedApp`은 리스너·config 전용), `appsShouldNotDependOnEachOther`(앱 → `@SharedApp` 의존 금지), `ServiceContextBoundaryTest`(스테레오타입이 붙으면 검사 대상에서 조용히 빠짐). 앞의 둘을 "앱 → 공유 도메인 서비스 단방향 허용(공유 커널)"으로 개정하고, 셋째는 `@Service`를 달지 않는 것으로 피했다.

**등록 방식 before / after**

```java
// before — application/bug/config/BugServiceConfig.java (삭제됨)
@Configuration(proxyBeanMethods = false)
@SharedApp
public class BugServiceConfig {
    @Bean
    public BugReportRegistrationService bugReportRegistrationService(
        BugReportPersistencePort bugReportPersistencePort,
        BugReportImagePersistencePort bugReportImagePersistencePort
    ) {
        return new BugReportRegistrationService(bugReportPersistencePort, bugReportImagePersistencePort);
    }
}
// application/bug/service/BugReportRegistrationService.java
public class BugReportRegistrationService { ... }
```

```java
// after — config 없음
// application/bug/service/BugReportRegistrationService.java
@WebApp
public class BugReportRegistrationService { ... }
```

| 항목 | before | after |
|---|---|---|
| 등록 config | `*ServiceConfig` 22개 + `PgRouterConfig` + `SharedEventConfig`(`@Bean` 88개) | `shared/config/SharedBeanConfig` 1개(`@Bean` 10개, `SharedEventConfig`에서 `git mv` 리네임) |
| 도메인 서비스 78개 | 마커 없는 POJO, config가 `new` | 클래스에 앱 마커만, 스캔 등록. 빈 이름 = 클래스명 첫 글자 소문자 = 옛 `@Bean` 메서드명(실측 전부 일치) |
| 마커 분포 | `@SharedApp` config 20개(+`SharedEventConfig`) + `@WebApp` config 3개(Mail·Sms·PgRouter) | `@WebApp` 17 · `@AdminApp` 3 · `@CeoApp` 21 · `@BatchApp` 2 · `@SharedApp` 35 = 78 |
| 잔류 `@Bean` | — | `domainEventPublisher`(인터페이스 타입으로 등록) · domain 계산기·정책 7개(`productExposureCalculator`·`cupDepositPolicy`·`storePriceBadgePolicy`·`shopOperatingStatusCalculator`·`shopDeliveryTipCalculator`·`shopNextOpenTimeCalculator`·`scheduledOrderSlotCalculator` — domain이 spring-free라 애노테이션 불가) · `shopDeliveryTipRangePolicy`(상수·람다로 조립하는 record) · `prohibitedWordValidator`(`CachingProhibitedWordPersistencePort`로 감싸 조립) |
| 한 앱 전용 빈 | 4앱 전부에 뜸 | **소비 앱에서만 뜸** — 동작 변경이지만 주입처가 없는 앱에서 사라질 뿐이라 기능 차이는 없다 |

**마커 배정 규칙(고정점)**: 소비 앱 집합이 한 앱이면 그 앱 마커, 두 앱 이상이거나 `@SharedApp` 리스너·빈이 쓰면 `@SharedApp`. `@SharedApp` 빈이 의존하는 빈은 전부 `@SharedApp`이거나 `SharedBeanConfig`의 마커 없는 빈이어야 하므로, 이 제약을 반복 적용해 고정점까지 올린다(예: `ReviewBlindRequestService`(Shared) → `ReviewLifecycleService`도 Shared, `ShopLifecycleService`(Shared) → `ShopImageApprovalService`·`ShopCeoAssignmentRecorder`도 Shared). 배정 근거는 아래 "코드 주석에서 이관된 설계 근거"의 "도메인 서비스 마커는 소비 앱 집합으로 정한다" 절.

**ArchUnit 규칙 before / after**

| 규칙 | before | after |
|---|---|---|
| `LayerRulesTest#sharedAppOnlyOnListeners` | 리스너 ∨ `..config..`의 `@Configuration` | 위 두 경우 ∨ **마커-only 도메인 서비스**(`..service..`, 이름이 `*CommandService`/`*QueryService`가 아님, `port.in` UseCase 구현체가 아님). 규칙명 유지 |
| `LayerRulesTest#markerOnlyClassesShouldBeDomainServices` | 없음 | **신설** — 마커-only 클래스는 위 도메인 서비스 위치에만 있어야 한다(하한 78개). 리스너는 `@Component`, 설정은 `@Configuration`을 함께 단다 |
| `AppIsolationTest#appsShouldNotDependOnEachOther` | 마커 5×4=20조합 전부 금지 | 앱 마커 → `@SharedApp`이면서 대상이 `..service..`인 경우만 허용. 앱 ↔ 앱, `@SharedApp` → 앱, 앱 → `@SharedApp` 리스너·설정은 계속 금지 |
| `AppIsolationTest#constructorDependenciesShouldBeVisibleToApp` | 없음 | **신설** — 마커 M 클래스(스테레오타입 유무 무관)의 생성자 파라미터 타입 T(제네릭 인자 포함)가 `com.tastyhouse.{application,domain}..`이면: (i) M/`@SharedApp` `@Configuration`의 `@Bean` 반환 타입이거나, (ii) 구체 클래스로서 M/`@SharedApp` 마커를 갖거나, (iii) 인터페이스로서 application 안 구현체 중 하나가 M/`@SharedApp`이거나, (iv) application 안에 구현체가 없어야(persistence·벤더 구현) 한다. 검사 의존 수 하한 700. **마커 누락과 오배정을 둘 다 잡는다** 후속 보강: 마커 `@Configuration`의 `@Bean` 메서드 파라미터도 그 설정의 마커 기준으로 같은 판정을 받고(하한 ≥ 3), 컬렉션이 아닌 인터페이스 의존의 주입 후보가 2개 이상이면 모호로 실패하며, 구현체 집계에서 데코레이터(그 인터페이스를 생성자로 받는 클래스)·abstract를 뺀다. 공유 도메인 서비스 판정에서는 `port.in` UseCase 구현 클래스를 제외한다(`appsShouldNotDependOnEachOther` 완화·`sharedAppOnlyOnListeners`·`markerOnlyClassesShouldBeDomainServices` 공통). |
| `AppIsolationTest#appRestrictedPortDependentsShouldBelongToThatApp` | 없음 | **신설** — 앱 마커가 붙은 클래스가 앱 전용 채널 포트를 생성자로 받으면(제네릭 인자 포함) 그 앱 마커여야 한다. web 전용: `MailSender`·`SmsSender`·`PgProviderGateway`·`SocialOAuthClient` → `@WebApp`. batch 전용: `BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort` → `@BatchApp`. 포트 구현이 그 앱에만 조립되기 때문이다 |
| `AppIsolationTest#sharedBeansShouldNotDependOnWebOnlyServices` | 이름 목록 4개 | 유지(중복 방어) |
| `AppIsolationTest#beansShouldHaveExactlyOneAppMarker` | `@Service`/`@Component`만 | 마커-only 도메인 서비스 포함(마커 2개 부착을 잡는다) |
| `AppIsolationTest#markerBeanCounts` | `@WebApp` ≥60 · `@AdminApp` ≥55 · `@CeoApp` ≥95 · `@BatchApp` ≥12 · `@SharedApp` ≥12 | 마커-only 서비스 포함, `@WebApp` ≥83 · `@AdminApp` ≥65 · `@CeoApp` ≥122 · `@BatchApp` ≥15 · `@SharedApp` ≥47 |
| `ServiceContextBoundaryTest` | 스테레오타입 없는 `..service..` POJO | **변경 없음** — 마커-only 서비스는 스테레오타입이 없으므로 계속 검사된다(`domainServicesShouldExist` ≥71, 봉인 15, 순환 봉인 1 불변) |
| `LayerRulesTest#sharedConfigsShouldOnlyDeclareUnmarkedBeans` | `@SharedApp` config 21개(`*ServiceConfig` 20 + `SharedEventConfig`) 대상 | `SharedBeanConfig` 1개 대상으로 계속 통과(반환 타입이 전부 마커 없는 클래스) |

**검증**: `backend`에서 `./gradlew build` 통과. 반증 probe 7종을 임시 클래스로 만들어 각각 실패하는 것을 확인하고 지웠다 — (a) `@SharedApp` 서비스가 `@WebApp` 서비스 주입, (b) 마커-only 클래스를 `..service..` 밖에 둠, (c) `@WebApp` 클래스가 구현체가 `@CeoApp`뿐인 인터페이스 주입, (d) 마커 2개 부착, (e) 마커 없는 `..service..` POJO를 `@WebApp` 서비스에 주입(마커 누락), (f) `MailSender`를 받는 클래스에 `@SharedApp`, (g) `List<PgProviderGateway>`를 받는 클래스에 `@SharedApp`. `grep -rl '@Bean' application/src/main/java`는 `SharedBeanConfig` 1개만 반환한다.

**남은 것 / 사각지대**: 4앱 `java -jar` 기동(`Started *ApiApplication` 마커) 확인은 별도 검증 세션 몫이다 — `contextLoads`는 빈 껍데기라 대신할 수 없다. `List<I>` 주입(`PgPaymentGatewayRouter`)은 구현체가 0개여도 기동되므로 규칙이 잡지 못한다(벤더가 application 밖이라 이번 변경과 무관).

## (번복됨 — persistence domain 재허용) 덩어리 03b — persistence가 도메인 모델을 모르게 (`store/`·`XxxState`·`XxxStatePort`)

> **이 절의 쓰기 경로 규칙(파일 구성·State record 작성 규칙·Store/StatePortImpl 분담·Store 등록·`storesExist`·`StateRecordArityTest`·`*StateMapperTest`)은 위 "persistence domain 재허용" 절로 번복됐다.** 기록으로 남긴다. **읽기 경로 규칙은 현행이다** — 아래 "읽기 계약 평탄화", "enum 비교값 전달 규칙", "persistence에서 이 모듈로 올라온 도메인 판단" 중 조회 DAO·QueryService에 관한 내용은 그대로 유효하다. 그 안의 "Store"는 persistence `XxxPersistenceAdapter`로 읽는다.

**write 경로에 상태 record 한 겹을 끼워 넣어, `infrastructure:persistence`가 `domain`을 몰라도 저장할 수 있게 했다.** 03a까지 서비스가 주입하던 `XxxPersistencePort`(도메인 모델 시그니처)를 persistence가 직접 구현했으므로 persistence는 domain을 알아야 했다. 지금은 그 인터페이스를 이 모듈의 `XxxStore`가 구현하고, Store가 도메인 모델을 `XxxState`로 바꿔 원시 타입 포트 `XxxStatePort`에 넘긴다. **서비스 코드는 import 한 줄만 바뀌었고 로직·HTTP 응답·DDL·저장값은 불변이다.** 대상은 도메인 타입을 쓰던 write 포트가 있는 전 컨텍스트(`notice`·`admin`·`banner`·`policy`·`partnership`·`faq`·`coupon`·`event`·`search`·`member`(+`follow`·`referral`)·`point`·`rank`·`reservation`·`review`·`menureview`·`notification`·`file`·`mail`·`sms`·`bug`·`ceo`·`holiday`·`region`·`shop`·`product`·`order`·`payment`)다.

### (번복됨) 한 컨텍스트의 파일 구성 — `notice` 파일럿

| 역할 | 파일 (`backend/application/src/main/java/com/tastyhouse/application/notice/...`) | 규칙 |
|---|---|---|
| 서비스가 주입하는 도메인 타입 인터페이스 | `store/NoticePersistencePort.java` | 03a의 `port/out/write/NoticePersistencePort`를 `git mv` — **이름·시그니처 불변, 패키지만 변경** |
| 상태 record | `port/out/write/NoticeState.java` | 컴포넌트 = 도메인 `Notice.reconstitute` 파라미터와 **같은 이름·같은 순서·같은 개수**. 원시 타입만 |
| 원시 타입 포트 | `port/out/write/NoticeStatePort.java` | 메서드 이름은 `NoticePersistencePort`와 1:1로 같고 인자·반환만 `NoticeState`·`Long`·`String`·`boolean`·`Optional` |
| 구현 (Store) | `store/NoticeStore.java` | **마커 없는 POJO**, `implements NoticePersistencePort`, `NoticeStatePort`를 생성자로 받는다 |
| 변환기 | `store/NoticeStateMapper.java` | `final class`, package-private, `toDomain(state)`(`reconstitute` 호출) · `toState(domain)` |
| 등록 | `config/NoticeServiceConfig.java` | `@Configuration(proxyBeanMethods = false) @SharedApp`, `@Bean NoticePersistencePort noticeRepository(NoticeStatePort)` — 컨텍스트에 이미 `<Ctx>ServiceConfig`가 있으면 거기에 `@Bean`을 추가한다 |
| persistence 구현 | `backend/infrastructure/persistence/.../notice/persistence/NoticeStatePortImpl.java` | 구 `NoticePersistenceAdapter`을 `git mv`. load-copy-save 그대로 |
| round-trip 테스트 | `backend/application/src/test/java/com/tastyhouse/application/notice/store/NoticeStateMapperTest.java` | `reconstitute`(모든 필드를 **서로 다른 값**으로) → `toState` → `toDomain` → `usingRecursiveComparison().isEqualTo(original)`. 컨텍스트마다 애그리거트별로 둔다 |

규모: `XxxState` 122(자식 State 포함) · `XxxStatePort` 105 · `XxxStore` 105 · `store/*Repository` 105 · persistence `XxxStatePortImpl` 105.

### (번복됨) State record 작성 규칙

- **컴포넌트는 `reconstitute` 파라미터와 1:1이다.** `backend/application/src/test/java/com/tastyhouse/application/architecture/StateRecordArityTest.java`가 각 `XxxState`의 **최상위** 컴포넌트 수를 같은 이름의 도메인 클래스 `reconstitute` 파라미터 수와 비교한다(개수만 본다 — 순서·이름 뒤바뀜은 round-trip 테스트가 잡는다). 예외 목록 `NON_AGGREGATE_STATES`는 비어 있고 짝 테스트 `nonAggregateStatesShouldNotBeStale`이 붙어 있다.
- **타입 강등**: enum → `String`(`name()` / 복원은 `Enum.valueOf`), ID VO·단일값 VO → 원시값(`value()`), 복합 VO → **`XxxSnapshot` record**(예: `order/port/out/write/OrderDeliveryDestinationSnapshot`·`OrderScheduleSnapshot`, `product/port/out/write/ProductDiscountInfoSnapshot`, `region/port/out/write/AdminDongBoundarySnapshot`·`AdminDongCenterSnapshot`, `shop/port/out/write/ShopDeliveryAreaPolygonShapeSnapshot`·`ShopDeliveryAreaPolygonCenterSnapshot`) 또는 원시 컴포넌트로 펼친다. 자식 컬렉션 → `List<ChildState>`(자식에 `reconstitute`가 있으면 그 `ChildState`도 arity 검사를 받는다).
- **복합 VO용 record에 `State` 접미사를 쓰지 않는다** — `StateRecordArityTest`가 `*State`를 전부 애그리거트로 보고 `reconstitute`를 찾으므로, `PhoneNumberState` 같은 이름은 테스트를 깨거나 잘못된 대응을 만든다. 접미사는 `Snapshot`이다.
- **null 안전**: nullable enum·VO·FK는 `x == null ? null : ...` 삼항으로 강등·승격한다. **모든 FK에 예외 없이** 둔다(삭제된 `IdMapping`이 강제하던 규칙의 승계 — `backend/CLAUDE.md` "ID VO 경계 규칙"의 번복 표기).
- **`port/out/write/` 아래에 둔다.** persistence의 `LayerRulesTest#shouldNotDependOnApiModules`가 application 중 `..port.out..`만 허용하고, 이 모듈의 `readContractsShouldBeFrameworkFree`가 그 아래 전부를 `java..`와 `port.out`만 참조하게 강제한다 — State·StatePort에 domain 타입이 한 개라도 들어가면 빌드가 실패한다.

### (번복됨) Store가 맡는 것 / StatePortImpl이 맡는 것

| 일 | 위치 |
|---|---|
| 도메인 ↔ State 변환 | Store(`XxxStateMapper`) |
| 도메인 예외(`BusinessException`·`ResourceNotFoundException`) | **Store**. StatePort는 `Optional`/`boolean`을 돌려준다. 응답 코드·HTTP 상태는 불변 |
| 도메인 정책·상수 호출(예: `ReservationStatus.blockingStatuses()`) | **Store**가 원시값으로 풀어 StatePort에 넘긴다(`application/reservation/store/ReservationStore` → `ReservationStatePort#existsBlockingByMemberShopDate(..., Collection<String> blockingStatuses)`) |
| JPA 조작·load-copy-save·낙관적 락 번역·비도메인 예외(`IllegalStateException`·`IllegalArgumentException`) | persistence `XxxStatePortImpl` |

- **도메인 타입을 쓰지 않는 write 포트는 State화하지 않는다** — `shop/port/out/write/StationPersistencePort`(`existsById(Long)`)는 03a 위치에 그대로 있고 persistence `StationPersistenceAdapter`이 직접 구현한다. 반대로 Repository가 아닌 보조 포트라도 도메인 타입을 쓰면 같은 방식으로 나눴다 — `ShopDeliveryTipRegionLookupPort`은 `shop/store/`로 가서 `ShopDeliveryTipStore`가 구현하고, `AdminDongSyncResult`(원시 필드뿐)는 `region/port/out/write/`에 남았다.
- **Store를 스캔으로 등록하지 않는다** — 02/03a와 같은 "마커 없는 POJO + 마커 붙은 `@Configuration`의 `@Bean`" 방식이다. `@Component`를 붙이면 앱 격리 규칙(`beansShouldHaveExactlyOneAppMarker`)에 걸리고, 스캔과 `@Bean`이 겹치면 기동이 실패한다. 등록 앱은 4앱 전부(`@SharedApp`) — persistence 구현이 4앱 전부에 있기 때문이다.
- **Store는 CQRS상 write 쪽이다** — `LayerRulesTest#queryServicesShouldNotDependOnWritePorts`의 대상에 `..store..`를 추가했다. 추가하지 않으면 서비스가 이제 `store`를 주입하므로 규칙이 **공허하게 통과**한다. `RuleAnchorTest#storesExist`(`..store..`의 `*Store` 구현 ≥ 105)가 대상이 사라지는 것을 막고, `writePortsExist`(≥ 106)는 `port.out.write` + `store`의 `*Repository` 인터페이스 합계를 센다.

### 읽기 계약 평탄화 — `port/out`은 domain을 모른다

| 항목 | before (01·03a) | after (03b) |
|---|---|---|
| `readContractsShouldBeFrameworkFree` | `port.out`은 `java..`·`com.tastyhouse.domain..`·`port.out` 참조 허용 | **`java..`·`port.out`만**(domain 허용 제거) |
| `*SearchCondition`·`{Ctx}QueryPort` 파라미터의 enum·ID | 도메인 enum·`XxxId` 허용 | `String`·`Long`. QueryService가 `OrderStatus.from(orderStatus).name()`·`OrderId.of(id).value()`로 **검증 후 강등**해 넘긴다(잘못된 입력의 400 응답 경로 불변) |
| `*Result`의 enum 라벨(`{field}Description`/`{field}DisplayName`) | persistence DAO가 `EnumLabelProjection`으로 채움 | **QueryService가 채운다** — DAO는 `Expressions.nullExpression(String.class)`로 자리만 두고, QueryService가 `XxxEnum.valueOf(result.status()).getDescription()`을 Result wither(`withDescriptions(...)` 등)에 넘긴다. 참고: ceo-application `shop/service/ShopChangeHistoryListQueryService`(당시 `ShopChangeHistoryQueryService`) → `shop/port/out/ShopChangeHistoryResult#withDescriptions`, ceo-application `ceo/service/CeoLoginHistoryListQueryService`(당시 `CeoLoginHistoryQueryService`) → `ceo/port/out/CeoLoginHistoryResult` |
| DAO가 enum 상수와 비교 | `.eq(OrderStatus.COMPLETED)` | `.eq(status)` — 비교값을 **포트 인자**로 받고, application(Store·QueryService)이 도메인 enum의 `name()`으로 넘긴다(아래 "enum 비교값 전달 규칙") |

**enum 비교값 전달 규칙** — ~~persistence가 비교할 enum 상수는 `<ctx>/port/out/XxxCodes`(도메인 enum을 복제한 문자열 상수 클래스)에서 가져온다~~ **(번복됨 — `@SuppressWarnings` 지양 규칙)** 복제본은 도메인 enum과 두 벌이 돼 일치 검사가 따로 필요했고, DAO가 쓰지 않는 상수마다 IDE 미사용 경고가 나서 억제 어노테이션이 붙어야 했다. 게다가 "판매 완료 주문만 센다"·"탈퇴 회원 제외" 같은 **도메인 정책이 persistence에 박혀 있었다.** 그래서 복제본 13개를 전부 지우고, **persistence는 enum 어휘를 전혀 모르게** 했다.

| 형태 | 언제 | 예 |
|---|---|---|
| `String`/`Collection<String>` 포트 인자 | 비교·필터 값 | `MemberQueryPort#existsByPhoneNumberAndStatusNot(phoneNumber, excludedStatus)` ← `MemberPhoneAvailabilityQueryService`(당시 `MemberQueryService`)가 `MemberStatus.DELETED.name()`. ~~`ReviewBlindRequestStatePort#existsByReviewIdAndStatusIn(reviewId, statuses)` ← `ReviewBlindRequestStore`의 종결 상태 상수~~ (번복됨 — 이 판단은 이제 persistence `ReviewBlindRequestPersistenceAdapter` 안에 있다) |
| 스펙 record 포트 인자 | DAO가 값에 따라 **쿼리 모양을 바꾸는** 곳 | `review/port/out/ReviewSortSpec(byLikeCount, createdAtAscending)` ← `review/service/ReviewSortSpecs.of(ReviewSortType)`, `review/port/out/ShopReviewTabFilter` ← `ShopReviewTabFilters.of(ReviewListTab)`, `product/port/out/ProductExposureWindow(now, todayDayTypes, previousDayDayTypes)` ← `product/service/ProductExposureWindows.now()`/`at(LocalDateTime)`(`DayType#appliesTo(dow, false)` — 공휴일 미판정은 과거 DAO 동작 그대로) |
| 기존 정책 record 컴포넌트 | 요청마다 달라지지 않는 고정값 | `shop/port/out/ShopDeliveryTipRangePolicy`의 `distanceExtraTipType`·`regionExtraTipType` ← `shared/config/SharedBeanConfig#shopDeliveryTipRangePolicy`(구 `ShopServiceConfig`, `DeliveryTipExtraType.DISTANCE/REGION.name()`) |

- **스펙 record를 만드는 유틸은 `<ctx>/service/`의 final class이며, 도메인 enum → 스펙 매핑은 exhaustive switch로 쓴다.** 도메인 상수가 추가되면 컴파일 에러로 드러난다.
- **그 유틸은 자기 컨텍스트(또는 `domain.shared`) 타입만 import한다.** `ServiceContextBoundaryTest`는 `..service..`의 `port.in`을 구현하지 않는 클래스(당시 판정은 비-`*QueryService`/`*CommandService`)를 도메인 서비스로 보고 타 컨텍스트 `model` 참조를 막는다. 그래서 인기상품 판매 집계의 `OrderStatus.COMPLETED.name()`은 `ProductExposureWindows`가 아니라 `ProductPopularQueryService#findPopularProducts`(당시 `ProductQueryService`)에서 만든다.
- **도메인 타입 `XxxPersistencePort` 시그니처는 그대로 둔다.** ~~바뀌는 것은 `XxxStatePort`와 Store 본문뿐이다(예: `MailVerificationStore#expireAllPendingByEmail` → `MailVerificationStatePort#changeStatusByEmail(email, PENDING, EXPIRED)`).~~ **(번복됨 — persistence domain 재허용)** 지금은 그 변환이 persistence `MailVerificationPersistenceAdapter` 안에 있다. 그래서 도메인 타입 fake(`FakeMailVerificationPersistencePort` 등)가 영향을 받지 않는다.
- **`port.out`에 도메인 enum의 복제본을 두지 않는다.** `EnumCodeConstantsTest#portOutShouldNotMirrorDomainEnums`가 `port.out`의 모든 enum을 도메인 enum과 **상수 집합**으로 대조해 막는다. 이름과 무관하게 잡는다. 허용 목록은 값 자체가 벤더 계약인 `PgProviderCode` 하나다. 과거 형태(`public static final String X = "X"` 상수 클래스)의 재발은 `#portOutShouldNotDeclareDomainEnumConstantStrings`가 막는다(도메인 enum 상수명과 같은 이름의 `static final String` 필드 금지).
- **`ShopReviewTabFilter`는 조건을 하나만 켤 수 있다.** compact constructor가 둘 이상이면 `IllegalArgumentException`으로 거부한다. DAO가 앞선 조건만 조용히 적용하는 것을 막는다.
- 리뷰 정렬 fallback이 DB에서 읽은 문자열이면 `ReviewSortType.valueOf`로 승격한다. 알 수 없는 값이면 `IllegalArgumentException`(500)이 나는데, 과거 DAO의 `IllegalStateException`(500)과 응답이 같다.

**함정은 01과 같다** — `Map<Enum, X>.get(string)`·`Set<Enum>.contains(string)`·`Objects.equals(enum, string)`은 컴파일되고 항상 `null`/`false`다. 파라미터를 `String`으로 바꾼 뒤에는 그 값을 소비하는 모든 곳을 읽는다(아래 "enum → `String` 강등 후 Object 타입 API" 절).

### persistence에서 이 모듈로 올라온 도메인 판단

persistence가 domain을 볼 수 없게 되면서, DAO·어댑터 안에 있던 도메인 정책 호출과 도메인 예외를 이 모듈이 맡게 됐다. **DAO는 원자료만 돌려주고, 판정·계산·예외화는 QueryService·Store가 한다.** **(persistence domain 재허용 후)** 조회 DAO 쪽은 그대로 유효하다(`queryShouldNotDependOnDomain`). Store가 하던 write 쪽 판단(예: 예약 차단 상태)은 persistence `XxxPersistenceAdapter`로 옮겨졌다.

| 판단 | before (persistence) | after (이 모듈) |
|---|---|---|
| 컵 보증금 금액 `CupDepositPolicy#depositAmountOf(cupCount)` | `product/query/ProductQueryAdapter`가 주입받아 투영 중 계산 | DAO는 `cupCount`만 싣고, `product/service/ProductOptionDepositAmounts`(package-private 유틸)가 web `ProductOptionsQueryService`·`ProductBatchQueryService`(당시 `ProductQueryService`)·admin `ProductOptionManagementListQueryService`(당시 `ProductManagementQueryService`)에서 채운다 |
| 에디터 추천 가게당 상품 수 `EditorChoicePolicy.PRODUCT_LIMIT` | `shop/query/ShopChoiceQueryAdapter`가 상수 직접 참조 | `ShopChoiceQueryPort#findEditorChoices(PageQuery, int productLimit)` 파라미터 — web `ShopEditorChoiceQueryService`(당시 `ShopQueryService`)·admin `ShopChoiceListManagementQueryService`(당시 `ShopManagementQueryService`)가 상수를 넘긴다 |
| 배달팁 표기 상한·거리 단위 `DeliveryTipPolicy.EXTRA_TIP_UPPER_BOUND`·`DeliveryTipDistanceUnit#getUnitMeters` | `shop/query/ShopDeliveryTipQueryAdapter`가 domain 상수·enum 직접 참조 | **값 record `shop/port/out/ShopDeliveryTipRangePolicy`**(상한 + 단위명→미터 맵)를 `SharedBeanConfig#shopDeliveryTipRangePolicy`(구 `ShopServiceConfig`)가 domain 값으로 만들어 `@Bean` 등록하고, DAO가 그 빈을 주입받는다 — DAO가 domain 없이 같은 값을 쓰는 형태 |
| 예약 차단 상태 `ReservationStatus.blockingStatuses()` | `reservation/query/ReservationQueryAdapter`가 직접 참조 | `ReservationAvailabilityQueryService`(당시 `ReservationQueryService`)·~~`ReservationStore`~~가 `name()` 목록으로 만들어 파라미터로 넘긴다(write 쪽은 지금 persistence `ReservationPersistenceAdapter`이 직접 만든다) |
| 가게 위치 조회 실패 `SHOP_ACCESS_DENIED`·좌표 미등록 `SHOP_DELIVERY_AREA_RADIUS_EXCEEDED` | `shop/query/ShopDeliveryAreaQueryAdapter#findShopLocation`이 `BusinessException` | DAO는 `Optional<ShopLocationResult>`를 돌려주고, 위치 없음(`SHOP_ACCESS_DENIED`)은 호출하는 `ShopDeliveryAreaPolygonDetailQueryService`·`ShopDeliveryAreaPolygonPreviewQueryService`(당시 `ShopDeliveryAreaPolygonQueryService`)·`ShopDeliveryAreaRadiusQueryService`가 `orElseThrow`로, 좌표 미등록은 `shop/service/ShopDeliveryAreaGeoMapper#requireCoordinates`가 같은 코드·문구로 던진다(응답 불변) |
| 폴리곤·행정동 경계 디코딩 | persistence `shared/query/GeoRingsResolver`(`GeoRingsQueryPort` 구현) | QueryService가 `domain/shared/geo/GeoPolygonTextCodec.decodeRings`를 직접 호출(ceo-application `region/service/AdminDongBoundaryQueryService`(당시 `AdminDongQueryService`)·`shop/service/ShopDeliveryAreaPolygonDetailQueryService`·`ShopDeliveryAreaPolygonPreviewQueryService`(당시 `ShopDeliveryAreaPolygonQueryService`)). `GeoRingsQueryPort`·`GeoRingsResolver`는 삭제 |
| 가게 매장가 인증 플래그 어댑터 `StorePriceVerificationAdapter` | persistence `@Component` | **`shop/service/StorePriceVerificationAdapter`**(POJO, ~~`ShopServiceConfig`가 `@Bean`~~ 지금은 클래스에 `@SharedApp` 마커만) — `ShopPersistencePort`(도메인 `Shop`)와 `ResourceNotFoundException`을 쓰므로 |

**DAO에 도메인 상수가 필요해 보이면** ① 호출부가 파라미터로 넘기거나(`productLimit`·`blockingStatuses`), ② 값 record를 `port/out`에 두고 `shared/config/SharedBeanConfig`(구 `<Ctx>ServiceConfig`)가 domain 값으로 `@Bean` 등록한다(`ShopDeliveryTipRangePolicy`). 조회 DAO에 domain을 들이지 않는다(persistence 모듈 자체는 domain을 다시 의존하지만 `..query..`는 `queryShouldNotDependOnDomain`이 막는다).

### ArchUnit·테스트 변경 (03b 시점 — 현행은 위 "persistence domain 재허용" 절의 표)

| 규칙·테스트 | before | after |
|---|---|---|
| `LayerRulesTest#readContractsShouldBeFrameworkFree` | domain 허용 | domain 허용 제거 |
| `LayerRulesTest#queryServicesShouldNotDependOnWritePorts` | 대상 `..port.out.write..` | `..port.out.write..` + `..store..` |
| `RuleAnchorTest#storesExist` | 없음 | **신설** — Store ≥ 105 |
| `RuleAnchorTest#writePortsExist` | `port.out.write`의 `*Repository` ≥ 106 | `port.out.write` + `store`의 `*Repository` 합계 ≥ 106 |
| `StateRecordArityTest` | 없음 | **신설** — 위 "State record 작성 규칙" |
| `EnumCodeConstantsTest` | `pgProviderCodeMatchesPgProvider` 1케이스 | + ~~`codesMatchDomainEnums`~~ **`portOutShouldNotMirrorDomainEnums`**(복제본 금지, 상수 집합 대조) |
| 컨텍스트별 `store/*StateMapperTest` | 없음 | **신설** 81개 — round-trip |
| persistence `LayerRulesTest#infrastructureShouldNotDependOnDomain` | 없음 | **신설**(`../infrastructure/persistence/AGENTS.md`) |

## 패키지 구조 (챕터 03으로 평탄화 — 도메인 아래에 앱별 폴더가 없다)

> **(앱 마커 제거 후 갱신)** 아래 트리는 **5모듈을 합친 패키지 모양**이다(패키지는 그대로). 달라진 점: `shared/marker/`는 **삭제**됐다. `port/in/`·유스케이스 서비스(유스케이스 분리 전 `*CommandService`/`*QueryService`)·앱 전용 도메인 서비스·앱 전용 SPI 포트(`SocialOAuthClient`·`MailSender`·`SmsSender`·`PgProviderGateway`·`BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort` 등)는 각 `{앱}-application` 모듈에 있고, 나머지(`shared/**`·공유 도메인 서비스·`port/out` 계약·`listener/`)는 코어 `application`에 있다. 트리 안의 "마커 부착"·"@SharedApp"·"AppOwnership 유도" 표기는 과거 기록이다. 상세는 [앱 마커 제거 — 앱 모듈 재분리](#앱-마커-제거--앱-모듈-재분리-챕터-01-통합챕터-03-마커-번복).

```
com.tastyhouse.application/
  │   (루트에는 클래스가 없다 — 과거 {App}ApplicationConfig.java 4개는 삭제됐다. 마커 스캔은 각 앱 부트스트랩의 중첩 ApplicationLayerScanConfig가 소유한다)
  ├── shared/marker/{WebApp,AdminApp,CeoApp,BatchApp}.java   순수 마커 애노테이션 4종 — 앱 소속의 유일한 단서
  ├── shared/marker/SharedApp.java   5번째 마커 — 리스너 + 공유 @Configuration(덩어리 01로 확대), "앱 소속 없음 = 4앱 전부"
  ├── shared/exception/BatchJobException.java   (챕터 03 이동 — 과거 batchapplication/exception/)
  ├── shared/exception/{ApplicationErrorCode,ApplicationErrorCodeSpec,ApplicationException,ResourceNotFoundException}.java   (에러코드 모듈 분할 신설·이동 — 코어 소유 84개 코드·앱 계층 예외. split package: `{Web,Admin,Ceo,Batch}ErrorCode`는 각 앱 모듈이 같은 패키지에 둔다)
  ├── shared/error/                 (덩어리 01 신설) 표현 계층용 에러 판정 — 빈 아님, 정적 유틸
  │     ├── ErrorDescriptor.java    record(int status, String code, String message)
  │     └── ErrorResponses.java     resolve(Throwable) → Optional<ErrorDescriptor> (최상위가 BusinessException일 때만)
  │     (~~ErrorContracts.java~~ 삭제 — 에러코드 모듈 분할. 미러는 `api-common-module`의 `apicommon.exception.ApiErrorCode`가 대신한다)
  ├── shared/port/out/CodeLabelResult.java   (덩어리 01 신설) record(String code, String label) — enum 카탈로그 응답용
  ├── shared/port/out/page/{PageQuery,PageResult}.java   (덩어리 01 이동 — 과거 domain의 shared/page/)
  ├── shared/port/out/OptimisticLockConflictException.java   (덩어리 03a 이동 — 과거 domain의 shared/exception/)
  ├── shared/event/{DomainEventPublisher,SpringDomainEventPublisher}.java   (덩어리 03a — 포트는 domain, 구현은 persistence에서)
  ├── shared/config/SharedBeanConfig.java    (구 SharedEventConfig, @SharedApp) — domainEventPublisher + domain 계산기 7 + shopDeliveryTipRangePolicy + prohibitedWordValidator. 이 모듈의 유일한 @Bean 보유 클래스
  └── <ctx>/
      ├── port/in/                UseCase 인터페이스(마커 부착) + Command record(마커 없음 — AppOwnership 유도)
      ├── service/                유스케이스 서비스 {도메인}{동작}Service / {도메인}{관점}QueryService(batch는 *SchedulerService),
      │                           implements 포트 정확히 1개 (유스케이스 분리 — 과거 *CommandService/*QueryService 쌍, 마커 부착)
      │                           + 도메인 서비스(과거 domain의 포트 주입 서비스) — @Service 없이 앱 마커만 (ServiceConfig 삭제 후)
      ├── port/out/               이 도메인의 모든 아웃바운드 계약(챕터 03으로 의미 확장) —
      │                           읽기 계약({Ctx}QueryPort·*Result·*SearchCondition, 마커 없음) +
      │                           아웃바운드 SPI(SocialOAuthClient 등) + Command 경로 반환 Result/View(마커 없음)
      │   │                       + persistence가 쿼리 모양을 바꿀 때 받는 스펙 record(ReviewSortSpec 등 — 도메인 enum 복제본은 두지 않는다)
      │   └── write/              (덩어리 03a) write 포트 XxxPersistencePort — 과거 domain의 <ctx>/repository/.
      │                           시그니처는 domain 타입, 구현은 persistence XxxPersistenceAdapter
      │                           ※ (번복됨 — persistence domain 재허용) 03b 동안은 XxxPersistencePort가 store/에 있었고
      │                             여기엔 XxxState · XxxSnapshot · XxxStatePort만 있었다 — 전부 삭제
  │                           — Result의 도메인 enum 필드는 덩어리 01로 String(+ {field}Description/DisplayName)
      ├── ~~config/~~             (번복됨 — ServiceConfig 삭제) <Ctx>ServiceConfig는 전부 삭제됐다. 컨텍스트 아래 config/ 폴더는 없다
      └── listener/               도메인 이벤트 리스너(@Component @SharedApp + @TransactionalEventListener(AFTER_COMMIT))
                                  — persistence에서 이동, 10개 컨텍스트에 12종
```

패키지만 봐서는 어느 앱 것인지 알 수 없다 — **(번복됨 — 앱 마커 제거)** 지금은 클래스가 들어 있는 모듈이 소속을 정한다. ~~빈·UseCase는 마커 애노테이션이, Command record는 `AppOwnership`의 유도가 소속을 정한다~~(아래 [챕터 03](#챕터-03--패키지-평탄화--앱-마커-애노테이션-과거-판단의-번복) 참고). 컨텍스트별 규모는 앱마다 다르다.

- **web** 컨텍스트 27종: `auth` · `banner` · `bug` · `coupon` · `event` · `faq` · `follow` · `grade` · `mail` · `member` · `menureview` · `notice` · `notification` · `order` · `partnership` · `payment` · `point` · `policy` · `product` · `rank` · `referral` · `reservation` · `review` · `search` · `shop` · `sms`.
- **admin** 컨텍스트 19종: `admin` · `auth` · `banner` · `bug` · `ceo` · `coupon` · `event` · `faq` · `file` · `member` · `notice` · `order` · `partnership` · `point` · `policy` · `product` · `rank` · `review` · `shop`.
- **ceo** 컨텍스트 6종: `auth` · `ceo` · `product` · `region` · `review` · `shop`. **컨텍스트 수는 가장 적은데 쌍 시절 서비스 수는 가장 많았다**(당시 `*CommandService` 44 · `*QueryService` 43 — 유스케이스 분리 후 명령 120 · 조회 68 = 188개) — 점주 셀프서비스가 `shop` 하나에 설정 관심사를 대량으로 갖기 때문이다(`ShopBusinessHour*`/`ShopClosedDay*`/`ShopStatus*`/`ShopDeliveryTip*` 등).
- **batch** 잡 슬러그 7종: `grade` · `product` · `productsoldout` · `rank` · `region` · `reviewblind` · `search`. 추가로 `crawling/bbq/`(BBQ 크롤링 동기화 + `port/out`에 포트 2종 `BbqMenuPort`·`RemoteImagePort`, 응답 record 4종, 다운로드 결과 record `DownloadedImage`)와 `exception/BatchJobException`이 있다. 잡별 UseCase·트리거 대응표는 `batch-module/AGENTS.md`에 있다.

대형 컨텍스트는 관심사 단위로 서비스를 더 쪼갠다 — 이 관례는 모듈 통합 전과 동일하다.

### 앱 간 동명 클래스는 정상이다

**앱별로 같은 역할의 타입이 따로 존재하는 것은 의도된 중복이다** — 소비자가 다르면 조회 범위·응답 형태가 다르고, 인증은 주체(`Member`·`Admin`·`Ceo`)·ErrorCode·`JWT_SECRET_*`가 앱별로 분리돼 있다. 통합하지 않는다.

**다만 이름까지 같게 두지는 않는다(챕터 02에서 개명 완료).** 챕터 03 평탄화로 세 앱의 타입이 같은 패키지에 공존하므로 simple name이 앱 간에도 유일해야 한다. `NoticeQueryService`(web) / `NoticeManagementQueryService`(admin), `ShopQueryService`(web) / `ShopManagementQueryService`(admin) / `ShopOwnerQueryService`(ceo, 당시 이름 — 지금은 `ShopOwnerListQueryService`·`ShopOwnerDetailQueryService`), `MemberTokenService` / `AdminTokenService` / `CeoTokenService`처럼 **web은 순수명, admin은 `Management`, ceo는 `Owner`**(인증 타입은 주체명 접두)로 구별한다. (web의 `NoticeQueryService`·`ShopQueryService`와 admin의 `NoticeManagementQueryService`·`ShopManagementQueryService`는 당시 이름이다 — 이후 유스케이스 분리로 `NoticeListQueryService`·`ShopDetailQueryService`·`NoticeManagementListQueryService`·`ShopListManagementQueryService` 같은 per-op 서비스로 나뉘었고, 순수명/`Management`/`Owner` 구별은 per-op 이름에도 그대로 적용한다.)

공유되는 것은 `domain`의 도메인 모델·write 포트·도메인 서비스와, 이 모듈 안에서 여러 앱이 함께 쓰는 `{Ctx}QueryPort` 계약이다(03a 이후 write 포트는 이 모듈의 `port/out/write/XxxPersistencePort`, 도메인 서비스는 `<ctx>/service/`에 있다 — 03b 동안 `store/`에 있었던 것은 번복됐다). 그 시그니처를 바꿀 때는 소비 앱 전체를 함께 확인한다.

**앱 간 타입명 충돌 시 `Management`/`Owner` 한정어**를 상시 적용한다 — `Result`·`QueryPort`뿐 아니라 `*UseCase`·`*Service`·`*Command`·협력 빈(`*Reader`·`*View`)까지가 대상이다(규칙 전문과 한정어 삽입 위치는 루트 `backend/CLAUDE.md` 참고). 동명 클래스 **182건의 일괄 개명은 챕터 02에서 완료**했다.

### 읽기 계약을 이 모듈이 소유한다

**읽기 계약(`{Ctx}QueryPort`·`*Result`·`*SearchCondition`)은 전부 이 모듈에 있다** — `src/main/java/com/tastyhouse/application/<ctx>/port/out/`이다. 통합 전 4개 모듈이 나눠 갖던 271개를 같은 트리로 합쳤고(챕터 01, 파일명 충돌 0건), 챕터 04에서 `domain`이 갖고 있던 다중 앱 공유 계약 55개까지 돌아왔다.

**`com.tastyhouse.application`을 이 모듈이 단독 소유한다 — split package가 끝났다.** 공유 계약 55개를 `domain`에 두던 시기에는 한 패키지를 두 모듈이 나눠 가졌고, 그것을 지키는 가드가 3종 필요했다(`ReadContractSingleOwnerTest`·`ReadContractPurityTest`·`RuleAnchorTest`의 소유 모듈 필터). 챕터 04로 셋 다 사라졌다 — 같은 모듈 안의 FQCN 중복은 컴파일 에러이기 때문이다. 이동은 패키지 경로가 같아 `git mv`뿐이었고 소비 측 import는 0건 바뀌었다.

- 구현은 `infrastructure:persistence`의 `<ctx>/query/` DAO다. 그 모듈이 `implementation project(':application')`으로 이 계약들을 본다.
- **새 읽기 계약은 소비 앱 수를 따지지 않고 이 모듈에 둔다.** 소비 앱이 하나든 셋이든 자리가 같다 — 소유 모듈을 판정하던 절차는 챕터 04와 함께 폐기됐다.
- **프레임워크-프리를 `LayerRulesTest#readContractsShouldBeFrameworkFree`가 지킨다**: 이 모듈은 spring starter를 받으므로 `application-common-module` 시절의 컴파일 게이트가 없다. ~~계약이 참조해도 되는 것은 `java..`·`com.tastyhouse.domain..`과 자기 자신뿐이다.~~ **(번복됨 — 덩어리 03b)** 계약이 참조해도 되는 것은 `java..`와 `com.tastyhouse.application..port.out..`뿐이다 — domain 허용이 제거됐다. `port.out`(읽기 계약 + `write/`의 `XxxState`·`XxxStatePort` + 스펙 record)은 infrastructure가 보는 유일한 application 표면이라, 여기에 domain 타입이 실리면 persistence가 domain을 다시 알게 된다. enum은 `String`, ID는 `Long`으로 싣고 승격·강등은 QueryService·Store가 한다(위 "덩어리 03b" 절). **(persistence domain 재허용 후)** 이 규칙은 **읽기 계약에만** 걸린다 — `port.out.write`(도메인 타입 `XxxPersistencePort`)는 대상에서 빠졌고, 대신 `LayerRulesTest#writePortsShouldOnlyDependOnDomainAndPortOut`이 `java..`·`com.tastyhouse.domain..`·`application..port.out..`만 허용한다. `XxxState`·`XxxStatePort`·Store는 삭제됐다.

## `response/`는 각 api 모듈로 승격됐다 (챕터 06 · 09 · 10)

**분리 당시에는 `response/`가 application 모듈에 함께 있었다.** 그때의 규칙이 "`{도메인}QueryService`가 Result → Response 변환을 담당"이었으므로, `response/`를 api에 남기면 서비스가 api 패키지를 역참조해 `applicationMustNotDependOnAdapters`가 곧바로 위반됐기 때문이다.

**Response 승격 챕터들이 그 전제를 바꿨다** — 조립 주체를 QueryService에서 **Response record 자신**(`from(XxxResult)`)으로 옮기고 Response를 api 모듈로 올렸다(admin 85 · ceo 105 · web 131, 총 **321개**). 유스케이스는 이제 프레임워크-프리 `*Result`·`PageResult`를 반환하므로 역참조가 생기지 않는다. 그 결과 이 모듈의 `io.swagger` import와 `com.tastyhouse.apicommon` 참조는 **0건**이며, `applicationShouldNotDependOnSwagger`·`applicationShouldNotDependOnApiCommon`이 그 상태를 고정한다.

**`request/`는 원래부터 api 모듈에 있었고 그대로다** — Request → Command 매핑은 인바운드 어댑터의 책임이며(완전 매핑 전략), 컨트롤러가 `request.toCommand(...)`로 조립해 넘긴다.

### 표현 계약이 만들 수 없는 값은 이 모듈이 `*View`/`*ViewResult`로 넘긴다

승격 후에도 **application에 남아야 하는 조립**이 있다. 표현 계약(api 모듈)은 도메인 모델·도메인 서비스·아웃바운드 포트를 알 수 없고 시계도 읽지 않아야 하므로, 아래는 이 모듈이 계산해 결과만 넘긴다.

| 남는 이유 | 예 |
|---|---|
| 읽기 포트가 아예 없는 파생(도메인 enum 상수에서 생성) | `GradeInfoResult`(`MemberGrade.values()`) |
| 여러 읽기 포트를 합친 결과 | `PointHistoryViewResult`·`ShopInfoViewResult`(2포트 6쿼리)·`ShopImageStatusResult`·`ProductNutritionViewResult` |
| 도메인 서비스·정책 판정이 필요한 값 | `ShopDetailViewResult`(`ShopOperatingStatusService`)·`ShopPriceBadgeViewResult`(`StorePriceBadgePolicy`)·`ReservationSlotAvailabilityResult`(`SlotPolicy`+시계) |
| 도메인 enum의 **비-accessor 호출**이 필요한 값 | `PaymentCancelResult`(`getMessage()`)·`ProductNutritionView`(`AllergenType.from`)·`ShopRequestListItemViewResult`(`isContractAmending`) |
| 금액 VO 언랩 | `PaymentViewResult`·`PaymentRefundViewResult`(`Money#value()`) |
| 시계 의존 파생 | `MyCouponListItemResult`(`daysRemaining`·`expired`)·`ShopReviewReplyWindow` |
| 다른 컨텍스트에 물어본 값 | `OrderProductViewResult`(`reviewed` — 리뷰 배치 조회로 N+1 회피) |
| 판별 유니온(분기 판정이 도메인 규칙) | `SocialLoginResult`·`SocialLinkResult`·`PhoneLoginResult`(web auth) |
| 도메인 enum `switch` | `ShopReviewSortTypeView` — api 모듈에서 enum을 `switch`하면 바이트코드가 `ordinal()`·`values()`를 호출해 `apiModuleShouldOnlyReadDomainEnums`에 걸린다 *(그 규칙은 덩어리 01로 삭제됐지만 — 지금은 api 모듈 클래스패스에 domain이 없어 `switch` 자체가 컴파일되지 않는다 — 분기를 이 모듈이 맡는다는 결론과 View는 그대로 유효하다)* |

**중첩 `Status` enum은 Result로 함께 복제하되 상수명을 바꾸지 않는다** — 상수 이름이 그대로 JSON 값이라 이름을 바꾸면 API가 바뀐다(`SocialLoginResult.Status`).

**`@JsonInclude`·`@JsonFormat` 같은 jackson 직렬화 어노테이션은 Response 쪽에만 둔다** — 직렬화는 api 모듈에서 일어나므로 Result로 옮기면 무의미해지고, `@JsonFormat` 소실은 날짜 포맷이 조용히 바뀌어 프론트 파싱을 깬다(챕터 10 실측 8인스턴스/6파일).

**반대로 순수 표현 파생은 Response로 내렸다** — 16자리 리뷰번호 0-pad, 문구 표시명 truncate(`CeoReplyPhraseResponse`), 거리별 배달팁 비움 판정, ~~enum → 문자열 강등~~ **(번복됨 — 덩어리 01: 강등은 Result를 채우는 DAO 투영(`x.status.stringValue()`) 또는 QueryService가 하고, Response는 이미 문자열인 값을 쓴다)**.

### Command 경로의 반환 Result는 앱 네임스페이스(`{app}application.<ctx>.port.out`)에 둔다

`ShopDeliveryAreaBulkResult`·`ProductAvailabilityChangeView`·admin `JwtResult`는 **읽기 계약 패키지(`com.tastyhouse.application..port.out`)가 아니라** 앱 네임스페이스에 있다. 읽기 계약 패키지에 두면 `commandServicesShouldNotDependOnQueryPorts`(CQRS 교차 주입 금지)가 CommandService의 **반환 타입**을 위반으로 잡는다.

## 앱별 auth 처리

| 앱 | 인증 방식 | 이 모듈에 있는 것 | api 모듈에 남은 것 |
|---|---|---|---|
| web | 소셜 로그인 SPI + JWT | `JwtTokenProvider` · `TokenService` · `CustomUserDetails(Service)` · `AuthCommandService` | `SecurityConfig` · `PublicPaths` (`JwtConfig`는 챕터 02에서 삭제 — 필터 빈은 `SecurityModuleConfig`(당시 이름 `SecurityModuleAutoConfiguration`)가 등록) |
| admin | `spring-security-core` + JWT | 위 + `AdminUserDetailsService` | 위 (`RedisRepositoryConfig`는 챕터 01에서 삭제 — 키 접두사는 `security.token-store.key-prefix` 프로퍼티) |
| ceo | `spring-security-core` + JWT | 위 + `CeoUserDetailsService` | 위 (동일) |
| batch | 없음 | — | — |

**결합의 실체는 서블릿이 아니라 Spring Security core였다**(챕터 02 판단 기록). auth 컨텍스트 전체에 `jakarta.servlet`·`org.springframework.web` import가 **0건**이었고 — 컨트롤러가 이미 원시값(Bearer 토큰 문자열·인가 코드)만 넘기고 있었다 — 실제 blocker이던 `JwtTokenProvider`·`TokenService`·`CustomUserDetails(Service)`는 `AuthenticationManager`·`SecurityContextHolder`·`UserDetails`·JWT만 쓰는 **서블릿-프리** 타입이라 함께 이동할 수 있었다. 서블릿 결합 타입만 밖에 남았고 — 필터·EntryPoint는 `security-module`이, `SecurityConfig`·`PublicPaths`는 각 api 모듈이 갖는다 — `applicationMustBeServletFree`가 그 경계를 강제한다.

**소셜 로그인은 web에만 있다** — admin·ceo에는 없다.

## ceo 고유 — 소유권·규격 검증이 이 모듈에 있다

`ShopOwnershipValidator`(`shop.ceoId == 로그인 ceoId` 확인, 불일치 시 `ApplicationErrorCode.SHOP_ACCESS_DENIED` 403)와 `ShopImageSpecValidator`/`ProductImageSpecValidator`(이미지 규격)는 **application 계층 협력자**라 이 모듈에 있다. 서블릿 타입을 쓰지 않으므로 `applicationMustBeServletFree`에 걸리지 않으며, 검증기가 쓰는 `javax.imageio.ImageIO`는 java 표준이라 규칙 대상이 아니다.

규격 검증기가 `MultipartFile`을 파라미터로 받는 것은 **업로드 경계 파라미터**로 허용된 형태다(`applicationMustBeServletFree`의 유일한 carve-out). Command record에 담는 것은 `commandRecordsShouldNotHoldMultipartFile`이 별도로 금지한다 — Command에는 업로드 결과 참조(파일 식별자·URL)만 담는다.

## batch 고유 — 왜 `crawling/bbq`가 여기 있나 (챕터 01 §2 판단 기록)

스펙은 "driven 클라이언트면 batch-module 잔류 + 인터페이스 분리"를 원칙으로 했으나, 확인 결과 **`crawling/bbq`는 driven 클라이언트가 아니라 application 계층 코드**였다.

- `BbqProductSyncService`는 `@Service @Transactional`로 **트랜잭션 경계를 소유**하고, 저장 불변식은 도메인 서비스 `ProductRegistrationService`에 위임하며, 동기화 대상 탐색은 `ProductQueryPort`(읽기 포트)로 한다.
- `BbqService`는 오케스트레이션이고, **진짜 driven 클라이언트는 `infrastructure:bbq`에 있다**(`com.tastyhouse.infrastructure.bbq.BbqApiClient`·`com.tastyhouse.infrastructure.bbq.RemoteImageDownloader`). 원격 이미지는 어댑터가 받아오기만 하고(`RemoteImagePort.download` → `DownloadedImage`), 파일 등록(`FileUploadService.upload`)은 `BbqService`가 조율한다.
- `BatchJobException`은 `BbqService`만 던지므로 함께 이동했다.

batch는 CQRS 분리를 쓰지 않는다 — `*CommandService`/`*QueryService`가 0개이고 잡 본문이 `*SchedulerService`에 담기며, 스케줄이 유일한 입력이라 Command record가 없고 인바운드 포트가 전부 `void foo()`다.

## ArchUnit — 5클래스 (챕터 03으로 importer·판별 기준이 패키지에서 마커로 전환, 03a로 `ServiceContextBoundaryTest` 추가)

> **(앱 마커 제거 후 갱신)** `AppIsolationTest`는 **파일째 삭제**됐다. `LayerRulesTest`에서 마커 규칙 4개(`listenersShouldBeShared`·`sharedAppOnlyOnListeners`·`markerOnlyClassesShouldBeDomainServices`·`sharedConfigsShouldOnlyDeclareUnmarkedBeans`)가 빠지고 모듈 경계 규칙 4개(`listenersAndConfigsShouldResideInCore`·`coreShouldNotContainUseCasesOrOrchestrators`·`coreBeansShouldOnlyDependOnCoreVisibleTypes`·`configurationsShouldNotRegisterStereotypedClasses`)가 생겼다. importer는 여전히 `com.tastyhouse.application`이지만, 이 모듈의 테스트 클래스패스에 앱 모듈 4개가 있어(`testImplementation project(':{앱}-application')`) **5모듈을 모두 본다.** batch 선별(`commandRecordsShouldBeBoundaryTyped`의 batch 제외, `BatchSchedulerRulesTest`)은 마커 대신 testFixtures `ModuleOrigin`으로 판정한 **출처 모듈**(`batch-application`)을 쓴다. 마커별 anchor는 `RuleAnchorTest#moduleBeanCounts`·`#moduleUseCaseCounts`가 승계했다. 아래 표와 문단의 마커 서술은 과거 기록이다.

> **(유스케이스 분리 후 갱신)** `LayerRulesTest`는 지금 **28종**이다. 표의 "UseCase 구현 강제 2"(`commandServicesShouldImplementUseCase`·`queryServicesShouldImplementUseCase`)는 **삭제**됐고, 그 자리를 **1:1 규칙 4종** `useCaseServicesShouldImplementExactlyOneUseCase`·`useCasesShouldDeclareSingleOperation`·`useCaseServiceNameShouldMatchPort`·`useCaseServicesShouldHaveSinglePublicOperation`이 채웠다(근거는 아래 "봉인·가드 목록"의 "유스케이스 서비스 1:1 규칙 4종"). "CQRS 교차 주입 2(이름 기준)"와 `commandServicesShouldNotDependOnRequestRecords`는 대상을 서비스 이름 접미어가 아니라 testFixtures `com.tastyhouse.architecture.UseCaseServices#commands()`·`#queries()`로 고른다 — 금지 대상(`*QueryPort`·`*QueryService`·`*QueryUseCase`·`port.out.write`·`..request..`) 쪽 판정은 그대로다. `coreShouldNotContainUseCasesOrOrchestrators`의 접미어 절과 `ServiceContextBoundaryTest#isStructuralDomainService`의 접미어 절도 삭제돼 `port.in` 구현 여부로만 가른다.

**챕터 01 직후에는 아래 4클래스의 importer가 "4개 앱 패키지"(`com.tastyhouse.{web|admin|ceo|batch}application`)였다.** 챕터 03의 패키지 평탄화로 그 패키지 접두어가 사라지자 이 표현 자체가 성립하지 않게 됐고, 특히 `AppIsolationTest`는 슬라이스/패키지 술어에서 **마커 애노테이션 술어**로 전면 재작성됐다(`application/src/test/.../architecture/AppIsolationTest.java`).

| 클래스 | importer | 내용 |
|---|---|---|
| `LayerRulesTest` | `com.tastyhouse.application`(단일) | **공통 18종**(+ persistence domain 재허용으로 신설된 `writePortsShouldOnlyDependOnDomainAndPortOut` — write 포트는 `java..`·`com.tastyhouse.domain..`·`application..port.out..`만 의존. `readContractsShouldBeFrameworkFree`는 대상에서 `port.out.write`를 뺐다). CQRS 교차 주입 2(이름 기준 — 아래 참고) · UseCase 구현 강제 2 · Command 경계 타입 2 · portIn/request 2 · QueryDSL·infra 차단 2 · servlet-free · adapter 역참조 금지 · 읽기 계약 프레임워크-프리 · swagger·api-common 차단 2 · **공유 마커 규칙 3** (덩어리 01로 리스너 마커 양방향 2 + 공유 설정 1) — `listenersShouldBeShared`(`@TransactionalEventListener` 메서드를 가진 클래스는 `@SharedApp`이 붙고 `..listener..` 패키지에 있어야 한다 — 마커 누락 시 리스너가 어느 앱에도 뜨지 않아 이벤트가 조용히 유실된다) · `sharedAppOnlyOnListeners`(~~`@SharedApp`은 `..listener..` 패키지에 있고 `@TransactionalEventListener` 메서드를 실제로 가진 클래스에만 허용~~ **번복됨(덩어리 01)**: 이제 허용 대상은 (`..listener..` + `@TransactionalEventListener` 보유) **또는** (`..config..` + `@Configuration`) — `..listener..`에 일반 빈을 두고 마커를 붙여 앱 격리를 우회하는 것은 여전히 막는다) · **`sharedConfigsShouldOnlyDeclareUnmarkedBeans`**(덩어리 01 신설 — `@SharedApp` 설정은 `@Component`/`@Service`를 겸하지 않고, `@Bean` 반환 타입과 그 설정이 생성자를 호출하는 클래스 전부가 앱 마커를 갖지 않아야 한다. 생성자 호출 검사는 구체 빈을 인터페이스 타입으로 반환해 반환 타입 검사를 피하는 경우까지 잡는다. **`should()`가 아니라 위반을 손으로 모으는 테스트**다 — 현재 `@SharedApp` 설정이 0개라 `should()`로 쓰면 ArchUnit failOnEmptyShould에 걸리고, `allowEmptyShould(true)`는 쓰지 않는 방침이기 때문이다. ~~첫 사용처는 덩어리 02/03a이며 그때까지 `RuleAnchorTest`에 anchor가 없다~~ **(실현됨 — 덩어리 02/03a)**: `file/config/FileServiceConfig`·`payment/config/PaymentServiceConfig` 2개가 이제 실제 대상이다. **(번복됨 — application `*ServiceConfig` 삭제)** 지금 대상은 `shared/config/SharedBeanConfig` 1개다. `sharedAppOnlyOnListeners`의 허용 대상에는 마커-only 도메인 서비스가 추가됐고, `markerOnlyClassesShouldBeDomainServices`가 신설됐다(위 "application `*ServiceConfig` 전면 삭제" 절의 표). 두 규칙 모두 임시 probe 클래스로 반증했다). **귀결: 앱 전용 `@TransactionalEventListener`는 둘 수 없다** — 의도된 제약이며, 필요해지면 이 두 규칙부터 개정한다. `@EventListener`(비트랜잭션)는 현재 0건이라 판정 대상에 넣지 않았다 |
| `AppIsolationTest` | `com.tastyhouse.application`(단일, 마커로 앱 구분) | **챕터 03 전면 재작성.** `appsShouldNotDependOnEachOther`(마커 5종 5×4=20조합 개별 검사 — 슬라이스가 아니다. `@SharedApp`이 `AppOwnership.MARKERS`에 들어가 "공유 리스너는 앱 전용 빈에 의존할 수 없고, 앱 전용 빈도 공유 리스너에 의존할 수 없다"까지 강제한다. `because`는 "공유는 domain과 읽기 계약 + `@SharedApp` 리스너뿐") · `beansShouldHaveExactlyOneAppMarker`(리스너는 `@SharedApp` 하나로 통과) · `useCasesShouldHaveExactlyOneAppMarker` · `commandRecordsShouldBelongToExactlyOneApp`(`AppOwnership` 유도) · `markerBeanCounts`·`markerUseCaseCounts`(마커별 하한 — 앱별 anchor 승계, ~~`@SharedApp` 빈 ≥ 12 포함~~ 마커-only 서비스 포함 `@SharedApp` ≥ 47) · **(번복됨 — application `*ServiceConfig` 삭제)** `appsShouldNotDependOnEachOther`는 앱 → `..service..`의 `@SharedApp` 도메인 서비스를 허용하고, `constructorDependenciesShouldBeVisibleToApp`·`appRestrictedPortDependentsShouldBelongToThatApp`이 신설됐다 · **`sharedBeansShouldNotDependOnWebOnlyServices`**(덩어리 02/03a 신설 — `@SharedApp` 빈은 `MailVerificationService`·`SmsVerificationService`·`PgPaymentGatewayRouter`·`PgPaymentGateway`를 의존하지 않는다. 이 넷은 `@WebApp` 설정만 등록하는 web 전용 서비스이므로, 공유 빈이 이들을 주입받으면 admin·ceo·batch에서 그 빈을 찾지 못해 기동이 실패한다) |
| `EnumCodeConstantsTest` | `com.tastyhouse.application.payment.port.out`·`com.tastyhouse.domain.payment.model`(단일 케이스) | **덩어리 02/03a 신설.** `pgProviderCodeMatchesPgProvider` — `PgProviderCode.values()`와 domain `PgProvider.values()`의 상수명·순서가 같은지 `Enum::name` 배열로 대조한다. 라우터(`PgPaymentGatewayRouter`)가 `PgProvider.valueOf(code.name())`으로 변환하므로, 두 enum이 어긋나면 이 테스트가 아니라 런타임 `IllegalArgumentException`으로 드러났을 결함을 컴파일 타임 대신 빌드 타임에 잡는다. ~~**(03b 확장 — 스캔 기반)** `codesMatchDomainEnums`가 `port.out`의 `*Codes`를 같은 이름 도메인 enum과 대조했다.~~ **(번복됨)** 복제본을 전부 지웠으므로 대체 규칙 **`portOutShouldNotMirrorDomainEnums`**가 `port.out`의 모든 enum(현재 6개, 중첩 `SocialLoginResult.Status`·`SocialLinkResult.Status` 포함)을 도메인 enum과 상수 집합으로 대조해 복제본이 다시 생기는 것을 막는다(`isNotEmpty()`로 공허 통과 방지, 허용 목록 `PgProviderCode`). **알려진 취약점**: `SocialProvider`는 도메인 `MemberSocialProvider`와 `GOOGLE` 하나만 다르다 — 둘이 같아져 이 테스트가 실패하면 `SocialProvider`를 허용 목록에 추가하는 것이 올바른 조치다(벤더 계약 enum이지 복제본이 아니다) |
| `BatchSchedulerRulesTest` | `com.tastyhouse.application`(단일, `.areNotAnnotatedWith(BatchApp.class)` 등 마커 술어로 batch만 선별) | batch 고유 4종 + exact anchor 3종(`*SchedulerService` 7 · `..port.in..` 7 · response record 4) |
| `RuleAnchorTest` | `com.tastyhouse.application`(단일) + 계약 | 공허 통과 자동 검출. 마커별 하한은 `AppIsolationTest`가 승계했으므로 이 클래스는 계약(읽기 계약) 하한과 **write 포트 하한(`writePortsExist` ≥ 107 — `port.out.write`의 `*Port` 인터페이스, `ShopDeliveryTipRegionLookupPort` 포함. ~~덩어리 03a 시점은 `*Repository` ≥ 106~~ (번복됨 — 아웃바운드 포트·어댑터 네이밍 전환))**을 담당. ~~03b로 `port.out.write` + `store` 합계, Store 하한 `storesExist` ≥ 105~~ **(번복됨 — persistence domain 재허용: `store` 조건 제거, `storesExist` 삭제)** |
| `ServiceContextBoundaryTest` | `com.tastyhouse.application` + `com.tastyhouse.domain` | **덩어리 03a 신설.** 스테레오타입 없는(ServiceConfig 삭제 후 앱 마커만 단) 도메인 서비스 사이의 컨텍스트 경계(봉인 위반 15 · 봉인 순환 1 + 짝 테스트) — 위 "덩어리 03a" 절 |
| ~~`StateRecordArityTest`~~ | — | **(번복됨 — persistence domain 재허용: 파일 삭제)** 아래는 03b 시점 기록. **덩어리 03b 신설.** 각 `XxxState`의 최상위 컴포넌트 수 = 같은 이름 도메인 클래스의 static `reconstitute` 파라미터 수(동명 클래스가 여럿이면 컨텍스트로 좁힘). 예외 목록 `NON_AGGREGATE_STATES`(현재 0) + 짝 테스트 `nonAggregateStatesShouldNotBeStale` — 위 "덩어리 03b" 절 |

**챕터 01 시점에 통합으로 의미가 달라져 손본 곳 두 군데는(carve-out FQN화, `applicationMustNotDependOnAdapters` 4패키지 확대) 챕터 03 이후에도 그대로 유효하다** — carve-out 대상 클래스와 api 패키지 이름 자체는 이번 평탄화로 바뀌지 않았다.

- `queryServicesShouldNotDependOnWritePorts`의 carve-out은 simple name이 아니라 **FQN**이다. 당시 `ShopQueryService`가 web·admin·ceo에 각각 있어 simple name으로 두면 의도한 1개가 아니라 3개 전부가 면제됐기 때문이다. 확정 carve-out은 web `ShopDeliveryTipViewQueryService`(유스케이스 분리 전 `ShopQueryService`, 도메인 계산 입력) **1건**이며, **이 목록에 새 항목을 추가하지 않는다.** ~~admin `AdminUsernameExistsQueryService`·ceo `CeoOwnerUsernameExistsQueryService` 인증 조회도 이관 대상이 아닌 carve-out이었다~~ **(번복됨 — JpaRepository 메서드 선언 금지)** 두 서비스는 `AdminQueryPort`·`CeoOwnerQueryPort`로 옮겨 carve-out에서 빠졌다.
- `applicationMustNotDependOnAdapters`의 금지 대상은 **4개 api 패키지 전부**다. 어느 앱의 서비스든 어느 api 모듈도 역참조할 수 없다.

**분리해 둔 이유가 있는 곳도 둘이다.**

- `commandRecordsShouldBeBoundaryTyped`는 (챕터 03 이후) `.areNotAnnotatedWith(BatchApp.class)`로 batch를 제외한다. batch는 carve-out이 `domain.exception..` 하나뿐인 **엄격판**을 `BatchSchedulerRulesTest`에서 쓸 수 있는데, 한 규칙으로 합치면 batch가 느슨한 3-carve-out 규칙에 얹혀 엄격함을 잃는다.
- `AppIsolationTest`의 마커별 anchor(`markerBeanCounts`·`markerUseCaseCounts`)는 **마커별로 유지**한다. 합계 하나로 두면 한 앱의 빈·UseCase가 통째로 사라져도 나머지 세 앱이 하한을 떠받쳐 anchor가 조용히 통과한다.

`allowEmptyShould(true)`는 어느 파일에도 쓰지 않는다 — 규칙이 대상을 잃으면 공허하게 통과시키지 말고 규칙을 지우거나 anchor를 고친다.

### anchor 하한

> **(번복됨 — 앱 마커 제거)** 빈·UseCase 하한은 지금 `RuleAnchorTest`가 **모듈별로** 갖는다 — `#moduleBeanCounts`(web ≥83 · admin ≥65 · ceo ≥122 · batch ≥15 · core ≥47), `#moduleUseCaseCounts`(~~web ≥50 · admin ≥100 · ceo ≥95~~ **유스케이스 분리 후 web ≥187 · admin ≥207 · ceo ≥188** · batch =7 · core =0). 유스케이스 서비스 하한은 `#commandServicesExist` ≥314 · `#queryServicesExist` ≥268(`UseCaseServices` 술어로 센다 — 과거 `countSuffix("CommandService")` ≥91 · `countSuffix("QueryService")` ≥100은 삭제), `..port.in..` 하한은 `#inboundPortsExist` ≥891(과거 ≥556). 모듈별로 두는 이유는 마커별로 두던 이유와 같다(합계 하나면 한 앱이 통째로 사라져도 통과한다). core UseCase =0은 "코어에 UseCase가 새지 않는다"의 anchor다.

**마커별 하한(빈·UseCase)은 `AppIsolationTest`가 갖는다** — `markerBeanCounts`(~~실측 web 66·admin 62·ceo 101·batch 13보다 낮은 하한: `@WebApp` ≥60·`@AdminApp` ≥55·`@CeoApp` ≥95·`@BatchApp` ≥12, 그리고 리스너 12종인 `@SharedApp` ≥12 — 리스너 하나가 마커를 잃으면 어느 앱에도 뜨지 않으므로 하한이 곧 리스너 수다~~ **(번복됨 — application `*ServiceConfig` 삭제)** 마커-only 도메인 서비스 78개를 포함해 `@WebApp` ≥83·`@AdminApp` ≥65·`@CeoApp` ≥122·`@BatchApp` ≥15·`@SharedApp` ≥47 — 이관이 되돌려져 서비스가 다시 마커를 잃으면 하한이 깨진다. `@SharedApp`은 리스너 12종 + 공유 도메인 서비스 35개 기준이다)와 `markerUseCaseCounts`(`@WebApp` ≥50·`@AdminApp` ≥100·`@CeoApp` ≥95·`@BatchApp` = 7 정확히 일치 — batch는 잡 7개로 규모가 작아 늘거나 줄면 의식적으로 고치는 것이 의도).

write 포트는 **≥ 107**(`RuleAnchorTest#writePortsExist` — `port.out.write`의 `*Port` 인터페이스: `*PersistencePort` 106 + `ShopDeliveryTipRegionLookupPort` 1. 보조 record `AdminDongSyncResult`는 인터페이스가 아니라 세지 않는다). ~~덩어리 03a 시점은 `*Repository` 인터페이스 ≥ 106~~ **(번복됨 — 아웃바운드 포트·어댑터 네이밍 전환)**. ~~03b부터 `port.out.write`와 `store` 두 패키지의 합계. Store는 ≥ 105(`RuleAnchorTest#storesExist`)~~ **(번복됨 — persistence domain 재허용)** `store` 패키지가 사라져 `storesExist`는 삭제됐고, `queryServicesShouldNotDependOnWritePorts`의 대상도 `port.out.write` 하나로 돌아왔다. 읽기 계약은 ~~합계 **≥ 282**(통합 전 4개 앱 합 227 + 챕터 04로 돌아온 공유 계약 55, `RuleAnchorTest` 소유)~~ **(번복됨 — persistence domain 재허용)** `port.out.write`를 뺀 `port.out` 클래스 **≥ 441**(`RuleAnchorTest#readContractsExist`)이다 — `readContractsShouldBeFrameworkFree`가 write 포트를 대상에서 뺐으므로 anchor 집계 범위도 맞췄다. 모듈 전체 하한(`RuleAnchorTest#moduleIsNotEmpty`)은 State 계열 약 460개 삭제 후 실측(중첩 클래스 포함 1,531)에 맞춰 **≥ 1,500**으로 올렸다. 소유 모듈을 가리던 소스-URI 필터는 챕터 04에서 제거했다 — 테스트 클래스패스에 남의 모듈 계약이 더는 없다.

하한으로 두는 이유는 컨텍스트가 늘어나는 것이 정상이기 때문이다. 정확히 일치를 요구하면 기능 추가마다 이 파일을 고쳐야 해 anchor가 규칙이 아니라 잡음이 된다(batch UseCase는 규모가 작아 예외적으로 정확히 일치를 쓴다).

## Dependencies

### 빌드 스크립트 형태
- **(앱 마커 제거)** `testImplementation project(':{web,admin,ceo,batch}-application')` 4줄 — 이 모듈의 ArchUnit 테스트가 5모듈 전부를 보게 한다. test → main 방향이라 Gradle 순환이 아니다(앱 모듈은 `api project(':application')`로 이 모듈을 main에서 의존한다).
- `java-test-fixtures` 플러그인 — ~~`AppOwnership`과~~(앱 마커 제거로 삭제) `ApplicationLayerScanAssertions`(앱 부트스트랩 스캔 단정 — 지금은 필터 없는 스캔·자기 앱 모듈만 로딩 단정 + imports 제거로 신설된 `assertScansModulesWithoutFilters`(중첩 `ModuleScanConfig`의 패키지 목록 정확 일치 + 패키지마다 스테레오타입 클래스가 1개 이상 실재 — 문자열 오타·패키지 리네임으로 모듈 빈이 조용히 사라지는 것을 막는다. `@ConditionalOnWebApplication`이 붙은 설정이 비-웹 스캐너에서 걸러지지 않도록 조건을 평가하지 않고 `@Component` 메타 애노테이션만 본다)·`assertNoTastyhouseAutoConfiguration`(`ImportCandidates`에 `com.tastyhouse.` 항목 0건). 중첩 스캔 클래스는 `ApplicationLayerScanConfig`·`ModuleScanConfig` 2개만 허용, 각 앱의 `ApplicationLayerScanConfigTest`가 호출)를 api 4모듈 테스트가 재사용하기 위한 것이다. `testFixturesImplementation`으로 `spring-boot-autoconfigure`(`@SpringBootApplication`·`@ComponentScan` 애노테이션 읽기)와 `assertj-core`를 선언한 것은 `ApplicationLayerScanAssertions` 때문이다. **같은 파일을 각 모듈에 복제하면 두 벌이 갈라지므로** test fixture로 공유한다(위 [챕터 03](#챕터-03--패키지-평탄화--앱-마커-애노테이션-과거-판단의-번복) 참고). `testFixturesApi`로 `archunit-junit5`를 노출하는 이유는 `AppOwnership`이 마커 애노테이션(main)과 ArchUnit을 함께 보기 때문이다.
- **실행 모듈이 아니므로 `bootJar { enabled = false }` + `jar { enabled = true; archiveClassifier = '' }`** — plain jar만 만든다(`security-module` 선례). 아래 [주의](#주의) 참고.

### Internal
- **(앱 마커 제거) 이 모듈은 앱 모듈을 main에서 의존하지 않는다** — 반대로 `{web,admin,ceo,batch}-application`이 `api project(':application')`로 이 모듈을 의존한다. 코어가 앱 모듈 타입(예: `MailSender`)을 import하면 컴파일 에러인 것이 앱 경계의 1차 방어선이다.
- `domain` (implementation) — 도메인 모델·VO·write 포트·도메인 서비스. **`implementation`이어서 이 모듈을 의존하는 쪽에 전이 노출되지 않는다** — 덩어리 01로 `api-common-module`(`api project(':application')`)·`security-module`(`implementation project(':application')`)이 `domain` 대신 이 모듈을 의존하게 되면서, presentation(web·admin·ceo-api, batch-module, api-common, security-module)의 컴파일 클래스패스에 `domain`이 사라졌다. **이 줄을 `api`로 바꾸지 않는다** — 바꾸는 순간 표현 계층에 domain이 되돌아오고 `shouldNotDependOnDomain`·`apiModuleShouldBeDomainModelFree`가 휴면 방어선에서 실제 실패로 바뀐다
- **표현 계층이 이 모듈에서 보는 domain 대체물 (덩어리 01)**: 에러 판정 `shared/error/`(`ErrorResponses`·`ErrorDescriptor`. ~~`ErrorContracts`~~ 는 삭제되고 `api-common-module`의 `ApiErrorCode`로 대체), 페이징 `shared/port/out/page/`(`PageQuery`·`PageResult`), enum 카탈로그 `shared/port/out/CodeLabelResult`, 그리고 enum 필드를 `String`으로 강등한 `*Result`
- `security-core` (implementation) — `JwtTokenProvider`·토큰 저장소 **포트**. **web·admin·ceo auth가 쓰는 서블릿-프리 타입 한정**. 챕터 01로 `security-core → infrastructure:redis` 간선이 끊겨, 이 모듈의 runtimeClasspath에서 `infrastructure:redis`·`api-common-module`이 사라졌다(전이 수신 0)
- **외부 연동 모듈(`infrastructure:{restclient,file-storage,firebase,aws-s3,aws-ses,aws-sns,oauth,kakao-oauth,naver-oauth,apple-oauth,facebook-oauth,pg,tosspayments,mail,javamail,sms,solapi,bbq,admdongkor}`) 의존은 두지 않는다** — 소셜 로그인 SPI(web)·크롤링 클라이언트(batch) 계약은 이 모듈이 소유하고 어댑터가 그것을 구현한다(**의존 역전**). 실제로 이 모듈의 계약을 구현하는 쪽은 `infrastructure:{kakao,naver,apple,facebook}-oauth`(소셜 SPI — 스타터 `infrastructure:oauth`는 코드가 없어 조립만 한다)와 `infrastructure:bbq`·`infrastructure:admdongkor`(배치 포트)이며, 이 줄을 되살리면 그 모듈들과 `application` 사이가 순환이 되어 빌드가 깨진다
- **`security-module`·`api-common-module`을 추가하지 않는다** — 서블릿 스택이 유입된다

### External
- `spring-security-core` — admin·ceo `AuthenticationManager`·`SecurityContextHolder`·`PasswordEncoder`·`UserDetails`
- `spring-web` — web·admin·ceo `MultipartFile`(업로드 경계 파라미터). 실사용이 1종뿐이라 starter-web 전체 대신 이 좌표만 선언한다
- `jackson-databind` — ceo `ShopStorePriceVerificationRequestService`(당시 이름 `ShopStorePriceVerificationCommandService`)의 `ObjectMapper`
- `spring-tx` — `@Transactional`만을 위한 최소 의존
- `spring-boot-autoconfigure`는 **재선언하지 않는다** — batch `AdminDongSyncRunner`의 `@ConditionalOnProperty`가 쓰지만 루트 `build.gradle`의 `subprojects` 블록이 넣는 `spring-boot-starter`로 전이 충족된다

batch 유스케이스가 `spring-web`·`spring-security-core`를 컴파일 클래스패스에서 보게 되지만, **서블릿 스택(`security-module`·`starter-web`)은 여전히 없다.**

### infrastructure 의존 없음 — 이 모듈의 핵심

application 계층이 infra를 모른다는 규칙을 ArchUnit이 아니라 **빌드 그래프가 1차로 강제**한다. `import com.tastyhouse.infrastructure...` 한 줄이 실제 컴파일 에러가 된다. `shouldNotDependOnInfrastructure`는 누군가 build.gradle에 의존을 되돌리는 회귀를 막는 2차 방어선이다.

## 빈 배선 (챕터 03 개정 — 패키지 스캔에서 마커 스캔으로)

> **(번복됨 — 앱 마커 제거)** 지금 각 앱 부트스트랩의 중첩 설정은 마커 필터가 없다.
>
> ```java
> @Configuration(proxyBeanMethods = false)
> @ComponentScan(basePackages = "com.tastyhouse.application")
> static class ApplicationLayerScanConfig {
> }
> ```
>
> | 항목 | before | after |
> |---|---|---|
> | 필터 | `useDefaultFilters = false` + `includeFilters = ANNOTATION {XxxApp, SharedApp}` | 기본 필터(`@Component` 계열), include/exclude 없음 |
> | 무엇이 뜨나 | 자기 앱 마커 + `@SharedApp` 클래스 | 앱 클래스패스에 있는 `com.tastyhouse.application..`의 스테레오타입 클래스 전부 = 코어 + 자기 앱 모듈 |
> | 흔한 실수 | 마커 누락 → 어느 앱에도 안 뜸 | 다른 앱 모듈을 의존에 추가 → 그 앱의 빈이 전부 뜸(**"클래스패스 존재 = 활성화"**). 각 앱 `ApplicationModuleClasspathTest`가 막는다 |
> | 빈 집합 | — | 동일(4앱 jar 기동 후 싱글턴 빈 이름 diff 0) |
>
> 중첩 클래스여야 하는 이유(아래 "왜 중첩 클래스인가")는 그대로 유효하다. 아래 본문의 마커 서술은 과거 기록이다.

**챕터 01 직후에는 앱마다 `{App}ApplicationConfig`가 자기 패키지만 스캔했다**(`@ComponentScan(basePackages = "com.tastyhouse.{app}application")`). 챕터 03의 평탄화로 그 앱별 패키지 자체가 사라졌으므로 4앱이 **같은 루트 패키지(`com.tastyhouse.application`)를 스캔하되 마커로 걸러낸다.** **(번복됨 — application `*ApplicationConfig` 삭제)** 그 스캔 선언은 과거 이 모듈의 `*ApplicationConfig` 4개에 있었으나 삭제됐고, 지금은 각 앱 부트스트랩의 static 중첩 클래스 `ApplicationLayerScanConfig`가 소유한다(`WebApiApplication` 예시):

```java
@Configuration(proxyBeanMethods = false)
@ComponentScan(
    basePackages = "com.tastyhouse.application",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {WebApp.class, SharedApp.class}))
static class ApplicationLayerScanConfig {
}
```

| 항목 | before | after |
|---|---|---|
| 스캔 선언 위치 | 이 모듈의 `WebApplicationConfig`(루트 패키지, `public class`) | `WebApiApplication` 안의 `static class ApplicationLayerScanConfig` |
| 앱의 `@Import` | `@Import(WebApplicationConfig.class)` | 없음 |
| 이 모듈이 아는 것 | 앱별 마커 조합(조립 지식) | 없음 — 마커 애노테이션만 제공 |
| 동작 | — | 변경 없음 |

**왜 중첩 클래스인가**: Spring Framework 6.1.5 `ConfigurationClassParser`는 설정 클래스에 직접 붙은 `@ComponentScan`을 먼저 모으고, 하나라도 있으면 메타 애노테이션(`@SpringBootApplication` 안의 `@ComponentScan`)을 무시한다. `@SpringBootApplication` 클래스에 직접 달면 앱 자기 패키지 스캔과 Boot의 `TypeExcludeFilter`·`AutoConfigurationExcludeFilter`가 조용히 사라지므로 금지다. 중첩 `@Configuration`은 별도 설정 클래스로 처리돼 안전하다. 부작용으로 `com.tastyhouse.application` 클래스패스 스캔이 기동 시 2회 돌지만(두 번째는 `ClassPathBeanDefinitionScanner.isCompatible`로 건너뜀) 빈 집합은 같다. 이 모듈은 이제 어느 앱이 어떤 마커를 싣는지 모른다.
4개 설정 전부가 자기 앱 마커와 함께 **`SharedApp.class`를 포함**한다(`classes = {XxxApp.class, SharedApp.class}`). 그래서 `@SharedApp` 빈(도메인 이벤트 리스너 12종, `SharedBeanConfig`, ServiceConfig 삭제 후에는 공유 도메인 서비스 35개까지)은 4앱 전부에 뜬다. 스캔 필터가 마커 `ANNOTATION`이므로 **`@Service` 없이 마커만 단 클래스도 빈이 된다** — 도메인 서비스가 이 형태다. 패키지 기반 include(`..listener..`를 통째로 포함)는 스캔 규칙이 "마커"와 "패키지" 두 가지로 갈리므로 채택하지 않았다.

`useDefaultFilters = false`이므로 **마커가 곧 스캔의 유일한 포함 기준**이다 — `@WebApp` 없는 `@Service`는 컴파일은 통과하지만 웹 앱의 `ApplicationLayerScanConfig`가 스캔해도 빈으로 뜨지 않는다. 이 실패는 그 빈이 처음 필요해지는 기동 시점에야 `NoSuchBeanDefinitionException`으로 드러나므로, 새 빈·UseCase를 추가할 때 마커를 빠뜨리지 않는 것이 이 모듈에서 가장 흔한 실수 지점이다(ArchUnit `beansShouldHaveExactlyOneAppMarker`·`useCasesShouldHaveExactlyOneAppMarker`가 이를 빌드 시점에 잡는다).

챕터 03 시점에는 각 부트스트랩의 `@Import` 대상 클래스가 챕터 01 이후 그대로여서 부트스트랩(api 모듈) 소스 변경이 **0건**이었고, auto-configuration 전환(챕터 02) 이후로는 `scanBasePackages` 나열이 사라져 `@Import({App}ApplicationConfig)` 한 줄만 남았다. **(번복됨 — application `*ApplicationConfig` 삭제)** 그 한 줄도 사라졌다 — 앱의 `@Import`는 0줄이고 스캔은 부트스트랩 중첩 `ApplicationLayerScanConfig`가 맡는다.

## `<ctx>/listener/` — 도메인 이벤트 리스너

domain이 `shared/event/DomainEventPublisher` 포트로 발행한 도메인 이벤트를 구독하는 크로스커팅 리스너를 둔다. 전부 `@TransactionalEventListener(phase = AFTER_COMMIT)`이며, 발행 구현 `shared/event/SpringDomainEventPublisher`가 `ApplicationEventPublisher`로 위임한다(**덩어리 03a로 포트와 구현 모두 이 모듈로 이동** — 과거 포트는 domain, 구현은 `infrastructure:persistence`에 있었다. 등록은 `shared/config/SharedBeanConfig`(구 `SharedEventConfig`)).

**위치는 `com.tastyhouse.application.<ctx>.listener`이고, 리스너는 전부 `@Component`이며 코어 `application` 모듈에만 둔다**(앱 마커 제거 전에는 `@Component` + `@SharedApp`. 지금은 코어가 4앱 전부의 클래스패스에 있으므로 코어에 두는 것만으로 4앱 전부에 뜬다 — `LayerRulesTest#listenersAndConfigsShouldResideInCore`가 강제). 현재 12종 — `coupon`·`file`·`mail`·`member`(`MemberEventListener`·`ReferralRegisteredEventListener`)·`notification`(`ReviewOwnerReplyEventListener`·`ReviewBlindApprovedEventListener`)·`payment`·`point`·`policy`·`product`·`sms`. 과거에는 `infrastructure:persistence`의 `com.tastyhouse.infrastructure.<ctx>.listener`에 있었다(번복됨 — 이벤트를 받아 도메인 서비스를 오케스트레이션하는 것은 유스케이스 계층의 일이다). `@SharedApp` 마커를 빠뜨리면 리스너가 어느 앱에도 뜨지 않아 이벤트가 **예외도 로그도 없이** 유실되므로, `LayerRulesTest#listenersShouldBeShared`가 빌드 시점에 막는다(아래 [빈 배선](#빈-배선-챕터-03-개정--패키지-스캔에서-마커-스캔으로) 참고). 리스너가 infra DAO를 직접 주입하지 않는다 — 이 모듈은 infra를 컴파일 클래스패스에 두지 않으므로 필요한 조회는 `port/out` 읽기 포트로 받는다(`ReviewOwnerReplyEventListener` → `ShopBasicInfoQueryPort#findShopName`).

**미소비 이벤트를 남기지 않는다.** 모든 `*Event` record에는 대응 리스너가 있어야 한다. 리스너 없는 이벤트는 "누군가 처리하고 있겠지"라는 착각을 낳고, 발행 지점만 보고는 그 착각이 드러나지 않는다. 소비 수요가 없다고 판단되면 리스너를 만드는 대신 **이벤트 record와 발행 호출을 함께 삭제**한다 — 둘 중 하나를 고르되 "발행만 하고 두는" 상태는 허용하지 않는다.

### ⚠️ AFTER_COMMIT은 실패하면 조용히 유실된다 — 금전 처리를 리스너에 두지 말 것

**이 프로젝트에는 재시도도 outbox도 없다.** AFTER_COMMIT 리스너가 예외로 죽으면 원본 트랜잭션은 이미 커밋된 뒤이므로 롤백되지 않고, 후속 처리만 소리 없이 사라진다. 로그 한 줄이 남을 뿐 실패를 감지하는 장치가 없다.

payment·point·coupon 리스너는 **금전에 직접 영향을 준다**(포인트 적립·환급·회수). 지금 이 처리들이 리스너에 있는 것은 유실이 허용돼서가 아니라 기존 구조가 그렇기 때문이며, **새로 추가하는 후속 처리에는 이 배치를 선례로 삼지 않는다.**

**판단 기준 — 유실되면 곤란한가?**

| 유실 시 결과 | 두는 곳 |
|---|---|
| 관측성만 손해(로그·통계 누락) | `<ctx>/listener/`의 `@TransactionalEventListener(AFTER_COMMIT)` |
| **데이터가 어긋남**(금전 정산, 목록에서 사라짐, 상태 불일치) | **동기 Recorder 패턴** — 원본 상태 전이와 **같은 트랜잭션**에서 도메인 서비스가 직접 호출 |

동기 Recorder의 선례는 `ShopChangeHistoryRecorder`·`ShopRequestIndexRecorder`다. 특히 후자는 이 판단을 명시적으로 기록해 두었다 — 요청처리 현황은 기록 유실이 곧 "요청이 목록에서 사라짐"이라 이벤트를 쓰지 않고 동기 기록을 택했고, Recorder를 도메인 서비스의 **생성자 필수 의존**으로 받아 새 상태 전이를 추가할 때 배선 필요성이 컴파일 단계에서 드러나게 했다. 상세는 루트 `CLAUDE.md`의 "요청 인덱스 동기화 규칙".

**outbox 도입은 현재 범위 밖이다.** 도입을 검토해야 할 시점의 근거만 남긴다 — (1) 유실 시 데이터가 어긋나는 후속 처리인데 동기 트랜잭션에 넣을 수 없는 경우(외부 API 호출처럼 원본 트랜잭션을 길게 잡으면 안 되는 것), (2) 그런 처리가 여러 컨텍스트에 생겨 Recorder 패턴만으로 감당되지 않는 경우. 그 전까지는 위 표의 두 선택지로 충분하다.

### 리스너 작성 규칙

- **도메인별로 분리한다**: 한 리스너가 여러 도메인 이벤트를 구독하면 한 도메인의 변경이 다른 도메인의 리스너 파일을 건드리게 된다. 같은 도메인의 이벤트 여러 개를 한 리스너가 받는 것은 정상이다(`CouponEventListener`가 발급·사용을 함께 받는 형태).
- **규칙 본체를 리스너에 두지 않는다**: 리스너는 이벤트 수신과 트랜잭션 경계만 담당하고, 판단·계산은 도메인 서비스가 갖는다(`PaymentEventListener` → `PointLedgerService`·`PaymentConfirmationService`, `ProductMenuReviewEventListener` → `ProductReviewStatsService`).
- **DB를 쓰면 `@Transactional(propagation = REQUIRES_NEW)`를 붙인다**: AFTER_COMMIT 시점에는 원본 트랜잭션이 이미 끝나 있다. 기록만 하는 핸들러는 붙이지 않는다.

### 리스너 단위 테스트

**리스너 파일마다 `<ctx>/listener/` 아래 대응 테스트를 둔다.** 스프링 컨텍스트 없이 리스너를 직접 생성해 핸들러를 이벤트 객체로 호출하는 순수 단위 테스트이며, AFTER_COMMIT 발화·`@Async`·트랜잭션 전파 같은 배선 자체는 프레임워크 몫이라 검증하지 않는다.

- **협력자가 있는 리스너**(payment·product)는 mock으로 **무엇을 호출/미호출하는지**를 검증한다. 조건 분기(현장 결제만 적립, `usedPoint > 0`일 때만 환급, `productId == null`이면 통계 미갱신)가 이 리스너들의 실질이고, 잘못되면 이중 정산·환급 누락으로 이어진다.
- **기록만 하는 리스너**(coupon·file·mail·member×2·point·policy·sms)는 `ListenerLogCapture`(testFixtures `com.tastyhouse.testsupport.shared.listener`)로 Logback appender를 붙여 **무엇이 기록되는지**까지 확인한다. 로그를 관측하지 않으면 핸들러 본문을 통째로 지워도 통과하는 공허한 테스트가 된다.
- **같은 타입 파라미터가 여러 개면 서로 다른 값을 넣는다**: `ReferralRegisteredEvent`의 추천인·피추천인은 둘 다 `MemberId`라 순서를 바꿔도 컴파일된다 — 값이 뒤바뀌면 "누가 누구를 추천했는지"가 반대로 기록되므로 각각이 제 자리에 들어가는지 확인한다.

reference 구현: `PaymentEventListenerTest`(협력자 mock + 조건 분기 3종 + 환불 접수의 "포인트 미개입" 계약), `ProductMenuReviewEventListenerTest`(null 가드), `CouponEventListenerTest`(로그 캡처 기준 예시), 공용 유틸 `ListenerLogCapture`(앱 마커 제거로 testFixtures로 이동 — `backend/application/src/testFixtures/java/com/tastyhouse/testsupport/shared/listener/ListenerLogCapture.java`, 과거 `backend/application/src/test/java/com/tastyhouse/application/shared/listener/ListenerLogCapture.java`).

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar(`security-module` 선례). 앱을 띄우는 것은 각 api 모듈의 fat jar 4개이며, 그 **이름·경로·포트는 통합 후에도 불변**이다. jar 내용만 application jar 4개 → `application-0.0.1-SNAPSHOT.jar` 1개로 바뀐다. **(앱 마커 제거 후)** 지금 각 fat jar에는 `application-0.0.1-SNAPSHOT.jar`와 자기 앱의 `{앱}-application-0.0.1-SNAPSHOT.jar` 2개가 들어간다(앱 모듈 4개도 같은 형태 — `bootJar` 비활성 + plain jar).
- **빈 배선 실수는 빌드로 드러나지 않는다** — `contextLoads` 테스트가 `@SpringBootTest` 없이 빈 껍데기라 스캔 설정(부트스트랩 중첩 `ApplicationLayerScanConfig` — 앱 마커 제거 후 필터 없는 스캔)이 누락·오기입돼도 빌드는 green이고 jar만 조용히 깨진다(오기입은 각 앱의 `ApplicationLayerScanConfigTest`가, 다른 앱 모듈 유입은 `ApplicationModuleClasspathTest`가 잡는다). 배선을 건드렸으면 실제로 띄워 `Started {Xxx}Application` 마커를 확인한다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

원문 주석은 챕터 04에서 제거되므로, 이 문서가 그 금지 지시의 유일한 소재지다.

### 앱 모듈 경계 가드 — 마커를 되살리지 않고, 다른 앱 모듈을 클래스패스에 올리지 않는다 (앱 마커 제거)

**대상**:
- `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `listenersAndConfigsShouldResideInCore` · `coreShouldNotContainUseCasesOrOrchestrators` · `coreBeansShouldOnlyDependOnCoreVisibleTypes` · `configurationsShouldNotRegisterStereotypedClasses` · `commandRecordsShouldBeBoundaryTyped`(batch 제외를 출처 모듈로 판정)
- `backend/application/src/test/java/com/tastyhouse/application/architecture/BatchSchedulerRulesTest.java` → 대상 선별(`batch-application` 출처 클래스)
- `backend/application/src/test/java/com/tastyhouse/application/architecture/RuleAnchorTest.java` → `moduleBeanCounts` · `moduleUseCaseCounts` · `testFixturesShouldNotResideInApplicationPackage`
- `backend/application/src/test/java/com/tastyhouse/application/architecture/ServiceContextBoundaryTest.java` → `domainServices()` · `EXCLUDED_COLLABORATORS` · `excludedCollaboratorsShouldNotBeStale`
- `backend/application/src/testFixtures/java/com/tastyhouse/architecture/ModuleOrigin.java` → 클래스 전체 · 짝 `backend/application/src/test/java/com/tastyhouse/application/architecture/ModuleOriginTest.java`
- `backend/application/src/testFixtures/java/com/tastyhouse/architecture/ApplicationLayerScanAssertions.java` → `assertScansApplicationLayerWithoutFilters` · `assertLoadsOnlyOwnApplicationModule`
- `backend/application/src/test/java/com/tastyhouse/application/architecture/SplitPackageUniquenessTest.java` → `classNamesShouldBeUniqueAcrossApplicationModules`

원문 취지:
- **마커 애노테이션을 되살리지 않는다.** 앱 소속은 모듈 위치가 표현한다. 빈은 언제나 `@Service`/`@Component`를 단다.
- **리스너와 `@Configuration`은 코어에만 둔다**(`listenersAndConfigsShouldResideInCore`). 리스너가 앱 모듈에 있으면 다른 앱이 같은 이벤트를 발행할 때 후속 처리가 조용히 사라진다(AFTER_COMMIT 실패는 예외도 남지 않는다). `@Configuration`이 스테레오타입 클래스를 `@Bean`으로 다시 등록하면 스캔 빈과 이름이 겹쳐 기동이 실패한다(`configurationsShouldNotRegisterStereotypedClasses`).
- **코어에 UseCase·오케스트레이터를 두지 않는다**(`coreShouldNotContainUseCasesOrOrchestrators`, `moduleUseCaseCounts`의 core =0). 코어는 4앱 전부에 뜨므로 거기 둔 UseCase는 쓰지 않는 앱에도 뜬다.
- **빈은 자기가 뜨는 앱 컨텍스트에서 보이지 않는 구현에 의존하지 않는다**(`coreBeansShouldOnlyDependOnCoreVisibleTypes`). 컴파일은 통과해도(인터페이스가 코어에 있으면) 그 구현이 없는 앱에서 기동이 실패한다. 판정: 모든 `@Service`/`@Component` 빈(코어·앱 모듈 모두)의 생성자 파라미터 중 `com.tastyhouse.application.` 인터페이스마다 **후보** = 그 인터페이스의 추상이 아닌 스테레오타입 구현체(같은 인터페이스를 생성자로 받는 데코레이터는 제외) + 그 타입을 반환하는 `@Bean` 메서드. 후보가 1개 이상이면, 그 빈이 뜨는 **각 앱 컨텍스트**(코어 빈 → 4앱 전부, 앱 빈 → 자기 앱. 컨텍스트 = 코어 + 그 앱 모듈)에서 보이는 후보가 0개면 위반(구현이 다른 앱 모듈에만 있어 그 앱이 기동하지 못한다), 2개 이상이면 모호 위반이다. 위반은 Set으로 모아 한 번에 보고한다. anchor: 검사한 의존 ≥ 300, 후보가 1개 이상인 의존(`RESOLVED_FLOOR`) ≥ 43. **알려진 한계**: `@Primary`/`@Qualifier`는 모델링하지 않는다. infrastructure 구현체는 import 대상이 아니어서 후보 0인 의존(persistence·벤더가 구현하는 포트)은 건너뛴다. **anchor 하한(300 / `RESOLVED_FLOOR` 43)을 낮추지 않는다** — 후보 계산이 깨지면 모든 의존이 "후보 0, 건너뜀"이 되어 규칙이 공허 통과한다.
- **`moduleBeanCounts`·`moduleUseCaseCounts` 하한을 낮추지 않는다**(빈: web ≥83 · admin ≥65 · ceo ≥122 · batch ≥15 · core ≥47 / UseCase: web ≥187 · admin ≥207 · ceo ≥188 · batch =7 · core =0 — 유스케이스 분리 전에는 web ≥50 · admin ≥100 · ceo ≥95). 한 모듈의 클래스가 통째로 사라지거나 엉뚱한 모듈로 옮겨지면 깨지는 것이 의도다.
- **`ModuleOrigin`은 main 출력만 인정하고, 나머지는 예외를 던져야 한다.** 규칙은 `ModuleOrigin.from(module)`(`DescribedPredicate`)로 대상을 고른다. 인정하는 것은 클래스 디렉터리 `.../{module}/build/classes/java/main/...`와 main jar `{module}-<버전>.jar`뿐이다. testFixtures 출력(`build/classes/java/testFixtures`, `*-test-fixtures.jar`), IntelliJ 자체 빌드 출력(`out/production/...`), opaque·형식이 깨진 URI는 전부 `IllegalStateException`이다. 모르는 형태에서 빈 값이나 기본값을 돌려주면, 출처로 고르는 규칙(batch 선별 등)이 대상을 잃고 공허하게 통과한다. **그래서 아키텍처 테스트는 Gradle로 실행한다** — IntelliJ 자체 빌드로 돌리면 `out/production/...` 출력 때문에 예외로 실패한다. IntelliJ에서는 Settings → Build Tools → Gradle → "Build and run using: Gradle"(테스트 실행도 Gradle)로 둔다. 이 예외를 피하려고 `out/production`을 인정하도록 넓히지 않는다.
- **`EXCLUDED_COLLABORATORS` 29개의 근거** — 모든 서비스가 `@Service`를 달게 되어 "스테레오타입이 없으면 도메인 서비스"라는 과거 술어를 쓸 수 없다. 그래서 구조 조건(`..service..`, 인터페이스 아님, `@Configuration` 아님, `port.in` 구현 아님 — ~~`*CommandService`/`*QueryService` 아님~~ 접미어 절은 유스케이스 분리로 삭제됐다. 유스케이스 서비스는 전부 `port.in`을 구현하므로 그 절이 하던 일은 마지막 조건이 한다)으로 고르고, **원래부터 스테레오타입이 있어 검사 대상이 아니던 협력 빈 29개**(Executor 6 · `*SocialLoginService` 4 · Validator 7 · Reader 3 · `OwnedShopIdProvider` · `Member{Auth,Grade,Review,Shop}Service` · `CredentialLoginService` · `PhoneLoginService` · `AuthPasswordResetService` · `AdminDongSyncRunner`)를 FQN으로 뺐다. 결과 대상 집합은 (앱 마커 제거 시점에) 이전과 같은 94개였고 `SEALED_VIOLATIONS`는 불변이다. 유스케이스 분리로 새로 생긴 `{도메인}{명사}Reader`·`Validator`(예: ceo `ShopNoticeOwnerReader`·`ShopContentBoardOwnerReader`·`ShopBusinessHourOwnerValidator`)는 이 목록에 넣지 않았다 — 그래서 구조적 도메인 서비스로서 경계 검사를 받는다. `SEALED_VIOLATIONS`의 항목 수는 그대로이고, ceo 도메인 서비스 `ShopRequestCancelService`가 `ShopRequestCancellationService`로 개명되며 FQN만 바뀌었다(아래 "유스케이스 서비스 1:1 규칙 4종"). 이 목록에서 항목을 빼면 그 협력 빈이 처음으로 경계 검사를 받아 봉인 밖 위반이 드러날 수 있다 — 빼려면 위반부터 확인한다. 새 협력 빈을 만들 때는 도메인 서비스인지 협력 빈인지 판단해 넣을지 정한다. 낡은 항목은 `excludedCollaboratorsShouldNotBeStale`이 잡는다 — **알려진 한계**: 이 짝 테스트는 더 이상 존재하지 않거나 구조 조건에 맞지 않게 된 항목만 잡고, "목록에서 빼도 경계 규칙을 통과할 항목"은 잡지 못한다. Reader 3개는 `ShopFoodTypeCategoryReader`·`StorePriceVerificationReader`·`StorePriceVerificationOwnerReader`, Validator는 7개다(합계 29). **이 술어는 사실상 "구조 + 이름 목록"이다.**
- **앱 테스트 클래스패스의 `com.tastyhouse.application` 클래스는 코어와 자기 앱 모듈에서만 와야 한다**(각 앱 `ApplicationModuleClasspathTest` → `assertLoadsOnlyOwnApplicationModule(ModuleOrigin.{WEB,ADMIN,CEO,BATCH})`, 출처 모듈 집합 == `{application, 자기 앱 모듈}`). 스캔에 필터가 없으므로 다른 앱 모듈이 의존에 섞이면 그 앱의 빈이 전부 뜬다("클래스패스 존재 = 활성화"). 판정은 클래스 출처(`ModuleOrigin`)로 하며, 앱 모듈에 표식 리소스(과거 `META-INF/tastyhouse/application-module.properties`)를 되살리지 않는다. 판정 대상에서 자기 앱 모듈이 빠져도 실패하므로 공허 통과가 없다.
- **같은 FQCN이 application 계층 5모듈 중 두 곳 이상에 있으면 안 된다**(`SplitPackageUniquenessTest`). 패키지를 유지한 split package라서 같은 FQCN이 core와 앱 모듈에 함께 생겨도 컴파일은 통과하고, 실행 시 클래스패스 순서로 한쪽이 조용히 가려진다. ArchUnit은 같은 이름의 클래스를 하나로 합쳐 보므로 잡지 못한다. 그래서 이 검사는 5모듈의 소스 경로로 한다(스캔 소스 ≥1400 anchor).
- 각 규칙은 위반 probe로 실패를 확인했다. 코어가 `MailSender`를 import하면 컴파일 에러인 것도 확인했다.

### 도메인 서비스는 클래스에 앱 마커만 단다 — `@Service`도 `@Bean`도 되살리지 않는다

> **(번복됨 — 앱 마커 제거)** 이 항목의 마커 규칙(`markerOnlyClassesShouldBeDomainServices`·`sharedAppOnlyOnListeners`·`sharedConfigsShouldOnlyDeclareUnmarkedBeans`, `AppIsolationTest` 전체, `markerBeanCounts`)은 삭제됐고, "도메인 서비스에 `@Service`를 달지 않는다"는 반대로 바뀌었다 — 지금은 **전부 `@Service`를 단다.** 유효하게 남은 것은 "새 도메인 서비스를 `@Bean`으로 등록하지 않는다·`*ServiceConfig`를 되살리지 않는다"와 "`SharedBeanConfig`에는 애노테이션을 달 수 없는 빈만 둔다"뿐이다. 현재 가드는 바로 위 "앱 모듈 경계 가드" 항목.

**대상**:
- `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `markerOnlyClassesShouldBeDomainServices` · `sharedAppOnlyOnListeners` · `sharedConfigsShouldOnlyDeclareUnmarkedBeans`
- `backend/application/src/test/java/com/tastyhouse/application/architecture/AppIsolationTest.java` → `constructorDependenciesShouldBeVisibleToApp` · `appRestrictedPortDependentsShouldBelongToThatApp` · `appsShouldNotDependOnEachOther` · `beansShouldHaveExactlyOneAppMarker` · `markerBeanCounts`
- `backend/application/src/main/java/com/tastyhouse/application/shared/config/SharedBeanConfig.java` → 클래스 전체(`@Bean` 10개)
- `backend/application/src/main/java/com/tastyhouse/application/*/service/` 의 마커-only 도메인 서비스 78개(예: `bug/service/BugReportRegistrationService` → 클래스 애노테이션 `@WebApp`)

원문 취지:
- **도메인 서비스에 `@Service`를 달지 않는다.** `ServiceContextBoundaryTest`는 `@Service`/`@Component`가 붙은 클래스를 검사 대상에서 조용히 뺀다 — 달면 컨텍스트 경계 검사가 빌드 실패 없이 사라진다. 반대로 필터를 넓혀 `@Service`까지 검사하면 기존 오케스트레이터 37개 중 13개 이상이 봉인 목록에 없는 위반이라 실패한다(그래서 "마커만 = 도메인 서비스, `@Service` + 마커 = 앱 오케스트레이터"로 갈랐다).
- **새 도메인 서비스를 `@Bean`으로 등록하지 않는다.** 클래스에 소비 앱 마커만 단다. `*ServiceConfig`를 되살리지 않는다.
- **`SharedBeanConfig`에는 애노테이션을 달 수 없는 빈만 둔다** — domain 모듈 클래스(spring-free), 인터페이스 타입으로 등록해야 하는 `domainEventPublisher`, 상수·람다·데코레이터로 조립해야 하는 `shopDeliveryTipRangePolicy`·`prohibitedWordValidator`. 마커가 붙은 클래스를 여기 `@Bean`으로 추가하면 스캔 빈과 이름이 겹쳐 기동이 실패한다(`sharedConfigsShouldOnlyDeclareUnmarkedBeans`가 `@SharedApp` 설정에 한해 잡는다). **`@WebApp` 등 앱 마커 설정을 새로 만들어 같은 일을 하면 이 겹침을 어떤 테스트도 잡지 못한다** — 앱 전용 config를 만들지 않는다.
- **마커는 소비 앱 집합으로 정한다** — 한 앱이면 그 앱 마커, 두 앱 이상이거나 `@SharedApp` 빈·리스너가 쓰면 `@SharedApp`. `@SharedApp` → 앱 마커 의존은 금지다(`appsShouldNotDependOnEachOther`). 오배정·누락은 `constructorDependenciesShouldBeVisibleToApp`이 잡는다.
- **앱 전용 채널 포트를 받는 클래스는 그 앱 마커여야 한다** — web 전용 `MailSender`·`SmsSender`·`PgProviderGateway`·`SocialOAuthClient`는 `@WebApp`, batch 전용 `BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort`는 `@BatchApp`. 포트 구현이 그 앱에만 조립돼 있어 다른 앱(특히 `@SharedApp`)에 걸면 그 앱이 기동하지 못한다.
- **`markerBeanCounts` 하한을 낮추지 않는다**(`@WebApp` ≥83 · `@AdminApp` ≥65 · `@CeoApp` ≥122 · `@BatchApp` ≥15 · `@SharedApp` ≥47). 이관이 되돌려져 서비스가 마커를 잃으면 이 하한이 깨지는 것이 의도다. `markerOnlyClassesShouldBeDomainServices`의 하한 78도 같은 이유다.
- 규칙 통과는 빈 누락이 없다는 증거가 아니다 — 배정을 바꿨으면 4앱을 `java -jar`로 띄워 `Started *ApiApplication`을 확인한다(`contextLoads`는 빈 껍데기). `List<I>` 주입은 구현체 0개여도 기동되므로 규칙이 잡지 못한다.

### testFixtures 클래스는 `com.tastyhouse.application` 밖에 둔다 — `com.tastyhouse.architecture`

> **(앱 마커 제거 후 갱신)** testFixtures에는 지금 두 패키지가 있다 — `com.tastyhouse.architecture`(`ApplicationLayerScanAssertions`·`ModuleOrigin`. `AppOwnership`은 삭제)와 **`com.tastyhouse.testsupport.<ctx>..`**(공유 테스트 더블 20개 — `Fake*`/`Stub*`/`Recording*`/`ListenerLogCapture`). 테스트 더블을 testFixtures로 옮긴 이유는 앱 모듈의 테스트도 같은 더블을 써야 하기 때문이고, `com.tastyhouse.application` 아래에 두지 않은 이유는 아래 취지 그대로다. 옮기며 package-private이던 9개를 public으로 바꿨다. `testsupport`도 `ImportOrderConventionTest.TOP_SEGMENT_RANK`에서 `architecture`와 같은 4순위다.

**대상**:
- `backend/application/src/testFixtures/java/com/tastyhouse/architecture/` → ~~`AppOwnership`~~(앱 마커 제거로 삭제) · `ApplicationLayerScanAssertions` · `ModuleOrigin`
- `backend/application/src/testFixtures/java/com/tastyhouse/testsupport/` → 공유 테스트 더블 20개(앱 마커 제거로 신설)
- `backend/application/src/test/java/com/tastyhouse/application/architecture/RuleAnchorTest.java` → `testFixturesShouldNotResideInApplicationPackage`
- `backend/domain/src/test/java/com/tastyhouse/domain/architecture/ImportOrderConventionTest.java` → `TOP_SEGMENT_RANK`의 `architecture`(4순위)

원문 취지:
- **testFixtures에 `com.tastyhouse.application..` 패키지를 만들지 않는다.** `ImportOption.Predefined.DO_NOT_INCLUDE_TESTS`는 `build/classes/*/test/` 경로만 걸러 낸다. 그래서 `-test-fixtures.jar`와 `build/classes/java/testFixtures/`는 통과한다. fixture가 `com.tastyhouse.application` 아래 있으면 `importPackages("com.tastyhouse.application")`를 쓰는 모든 규칙(이 모듈의 `LayerRulesTest`·`AppIsolationTest`·`RuleAnchorTest` 등, api 4모듈의 `adaptersShouldOnlyUseOwnAppUseCases`)이 fixture를 **프로덕션 클래스로 보고 검사한다**.
- 그 상태로는 "application의 모든 클래스는 마커를 가진다"처럼 모듈 전역을 대상으로 하는 규칙을 새로 만들 때, 마커가 없는 fixture부터 위반으로 걸린다. `moduleIsNotEmpty` 같은 개수 anchor도 fixture 수만큼 부풀어 실제 개수보다 크게 나온다.
- `com.tastyhouse.architecture`는 어떤 ArchUnit importer의 대상 패키지도 아니다(리포 전체에 `importPackages("com.tastyhouse")`가 없다). 새 fixture도 이 패키지에 둔다. 이 규칙을 지키는 장치가 `testFixturesShouldNotResideInApplicationPackage`다. 소스 URI가 `test-fixtures`/`/testFixtures/`인 클래스가 application 대상 import에 섞이면 실패한다. 2026-10-03에 패키지 이동 전 상태로 두 클래스를 잡아 실패하는 것을 확인했다.
- 새 최상위 세그먼트라 import 순위가 필요하다. 테스트 공용 지원 코드라서 공유 횡단 모듈과 같은 4순위로 정했다.

### UseCase 구현 구체 주입 금지 — `servicesShouldDependOnUseCasesNotImplementations`

**대상**: `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`
→ `servicesShouldDependOnUseCasesNotImplementations`

- **검사 내용**: `port.in` 인터페이스를 구현한 클래스(구현 집합 `I`)에 다른 클래스가 구체 타입으로 의존하면 실패한다. `getDirectDependenciesFromSelf()`로 판정하므로 필드·생성자 파라미터·메서드 파라미터·호출이 모두 잡힌다.
- **제외 대상**:
  - 자기 자신과 자기 중첩 클래스. 최상위 클래스가 대상과 같으면 건너뛴다. non-static inner 클래스는 바깥 클래스를 암묵적으로 참조하기 때문에 이 제외가 없으면 오탐이 난다.
  - 봉인 목록은 두지 않는다. 기대 위반은 0건이다.
- **공허 통과 방지**: `I.size() >= 200`을 함께 단정한다(도입 시점 실측 202). 패키지 필터가 틀려 `I`가 비면 이 규칙은 아무것도 검사하지 않고 통과하기 때문이다.
- **위반이 나면**: `@SuppressWarnings`나 예외 목록을 두지 않는다. 호출부가 UseCase 인터페이스를 주입하게 바꾼다. 필요한 메서드가 도메인 타입을 쓴다면 write 포트나 도메인 서비스를 직접 주입한다(아래 "QueryUseCase에는 컨트롤러 표면만 올린다" 절의 번복 표기).
- **반증**: 구체 `MemberCommandService`(당시 이름)를 주입하는 probe 클래스를 넣었을 때 실패하는 것을 확인했다.

### UseCase 필드명 — `useCaseFieldsShouldBeNamedUseCase`

**대상**: `useCaseFieldsShouldBeNamedUseCase`. 아래 모든 모듈의 `LayerRulesTest`에 있다.
- `application/src/test/.../architecture/LayerRulesTest`
- `{web,admin,ceo}-api/src/test/.../architecture/LayerRulesTest`
- `batch-module/src/test/.../architecture/LayerRulesTest`

- **검사 내용**: `port.in` 인터페이스 타입 필드는 이름이 `UseCase`로 끝나야 한다. 변수명이 타입을 따르게 하려는 것이다(backend/CLAUDE.md 포트·어댑터 네이밍 규칙).
- **도입 배경**: 도입 전에는 컨트롤러 69곳이 `MemberAuthCommandUseCase authCommandService`처럼 UseCase를 주입하면서 필드 이름은 `*Service`였다. 코드만 보면 구체 서비스를 주입한 것으로 읽혔다.
- **공허 통과 방지**: 모듈별로 대상 필드 수의 하한을 단정한다(도입 시점 실측의 약 80%: application 15 · web 40 · admin 80 · ceo 80 · batch 5). 필드 타입 해석이 일부만 깨져도 공허 통과하지 않게 하려는 것이다. 필드가 크게 줄어드는 리팩터링을 하면 하한을 함께 조정한다.
- **판정식은 5개 파일이 같아야 한다**: `isPortInInterface`와 테스트 본문은 모듈별 테스트에 복제돼 있다(공유 testFixtures를 두지 않는 관행). 판정식을 바꿀 때는 5곳을 함께 고친다.
- **검사 범위의 한계**: 필드만 검사하고 생성자 파라미터명은 검사하지 않는다. javac `-parameters` 설정 유무에 따라 파라미터명을 읽을 수 없기 때문이다. 파라미터명은 리네임할 때 필드와 함께 맞춘다.

### CQRS 규칙의 `*QueryUseCase` 확장 — `commandServicesShouldNotDependOnQueryPorts`

**대상**: `LayerRulesTest#commandServicesShouldNotDependOnQueryPorts`

- **변경 내용**: 금지 대상에 이름 접미어 `QueryUseCase`를 추가했다. 기존 금지 대상은 `QueryPort`·`QueryService`였다.
- **변경 이유**: 조회 협력 메서드가 `*QueryUseCase`에 올라가자, CommandService가 구체 `*QueryService` 대신 `*QueryUseCase`를 주입하면 이름 기준 규칙을 우회할 수 있게 됐다.
- **(유스케이스 분리) 대상 쪽 판정이 바뀌었다**: 규칙의 대상은 이름 접미어 `*CommandService`가 아니라 `UseCaseServices.commands()`(구현 포트가 `…QueryUseCase`가 아닌 유스케이스 서비스)다. 명령 서비스 이름에서 `Command`가 사라져(`PaymentConfirmService` 등) 접미어로는 대상을 고를 수 없기 때문이다. 금지 대상 쪽(`QueryPort`·`QueryService`·`QueryUseCase` 접미어)은 그대로다.
- **위반 현황**: 도입 시점 위반은 0건이고, probe로 규칙이 실패하는 것을 확인했다. 이 확장을 되돌리지 않는다.

### 유스케이스 서비스 1:1 규칙 4종 — 포트 하나 = 연산 하나 = 서비스 하나 (유스케이스 분리)

**대상**:
- `backend/application/src/testFixtures/java/com/tastyhouse/architecture/UseCaseServices.java` → `isUseCaseService` · `isQueryService` · `isCommandService` · `commands()` · `queries()`
- `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `useCaseServicesShouldImplementExactlyOneUseCase` · `useCasesShouldDeclareSingleOperation` · `useCaseServiceNameShouldMatchPort` · `useCaseServicesShouldHaveSinglePublicOperation` · `useCaseServices()`(하한 582)
- `backend/application/src/test/java/com/tastyhouse/application/architecture/RuleAnchorTest.java` → `commandServicesExist` · `queryServicesExist` · `moduleUseCaseCounts` · `inboundPortsExist`

| 항목 | before | after |
|---|---|---|
| 명령/조회 서비스 판별 | 클래스명 접미어 `*CommandService`/`*QueryService` | `UseCaseServices` 술어 — 유스케이스 서비스 = `..service..`의 비인터페이스 + `port.in` 인터페이스 구현 + batch 출처 아님. 그중 구현 포트명이 `QueryUseCase`로 끝나면 조회, 나머지는 명령 |
| 포트 구현 개수 | "최소 1개"(`commandServicesShouldImplementUseCase`·`queryServicesShouldImplementUseCase`) | **정확히 1개**(`useCaseServicesShouldImplementExactlyOneUseCase`) — 두 옛 규칙은 삭제 |
| 포트의 연산 수 | 제한 없음(`PaymentCommandUseCase` 메서드 6개 등) | web·admin·ceo 출처 `port.in` `*UseCase`는 추상 메서드 1개(`useCasesShouldDeclareSingleOperation`) — 의미가 다른 오버로드도 이름을 나눈 별도 포트로 둔다 |
| 서비스 이름 | 도메인당 `{도메인}CommandService`/`{도메인}QueryService` | 포트명의 `UseCase`를 `Service`로 바꾼 이름(`useCaseServiceNameShouldMatchPort`) — `PaymentConfirmUseCase` → `PaymentConfirmService` |
| 서비스의 public 메서드 | 제한 없음 | 생성자를 뺀 public 메서드 1개(`useCaseServicesShouldHaveSinglePublicOperation`, static·synthetic·bridge 제외) |
| anchor | `countSuffix` 기반 `commandServicesExist` ≥91 · `queryServicesExist` ≥100 | `countSuffix` 삭제. 술어 기반 ≥314 · ≥268, `moduleUseCaseCounts` web ≥187 · admin ≥207 · ceo ≥188 · batch =7 · core =0, `inboundPortsExist` ≥891 |

원문 취지:
- **왜 1:1인가**: 도메인당 한 쌍이던 시절 서비스 하나가 연산 수십 개를 떠안았다(web `ShopQueryService` 714줄·public 메서드 20개, admin `ShopManagementCommandService`는 UseCase 37개 구현 — 둘 다 당시 이름). 그러면 트랜잭션 속성·주입 의존이 그 클래스의 모든 연산에 묶이고, 한 연산을 고칠 때 무관한 연산이 같은 diff에 섞인다. 『만들면서 배우는 클린 아키텍처』의 `SendMoneyService implements SendMoneyUseCase` 형태로 바꿔, 주입 목록이 곧 그 연산 하나의 의존을 증명하게 했다. 네 규칙 중 하나만 빠져도 1:1이 무너진다 — 포트 2개를 구현하는 서비스, 연산 2개짜리 포트, 포트와 이름이 어긋난 서비스, public 헬퍼를 노출한 서비스가 각각 다른 규칙에서 잡힌다.
- **왜 판별을 서비스 이름이 아니라 구현 포트 이름으로 하나**: 명령 서비스 이름에서 `Command`가 빠졌다(`PaymentConfirmService`). 접미어로 고르던 CQRS 교차 주입 규칙(`commandServicesShouldNotDependOnQueryPorts`·`queryServicesShouldNotDependOnWritePorts`·`commandServicesShouldNotDependOnRequestRecords`)이 대상을 통째로 잃고 공허 통과하므로, 판별자를 포트 이름 끝의 `QueryUseCase` 하나로 옮겼다.
- **왜 batch는 제외하나**: `*SchedulerService` 7개는 이미 UseCase 1개 = 서비스 1개다. 이름은 `BatchSchedulerRulesTest`가 `*SchedulerService`로 지키므로 `useCaseServiceNameShouldMatchPort`(포트명 기반)와 충돌한다. `SearchKeywordSchedulerService`는 UseCase 하나에 메서드 2개라 `useCaseServicesShouldHaveSinglePublicOperation`·`useCasesShouldDeclareSingleOperation`에 걸리는데, 이번 분할의 범위 밖이라 손대지 않았다. 그래서 `isUseCaseService`와 `useCasesShouldDeclareSingleOperation`이 `ModuleOrigin.BATCH` 출처를 뺀다.
- **하한 582를 낮추지 않는다**: 출처 판정이나 `port.in` 판정이 깨지면 대상이 줄어 네 규칙이 공허 통과한다. 하한은 분할 직후 실측(web 187 · admin 207 · ceo 188)이다.
- **`allowEmptyShould`를 쓰지 않고, 위반은 손으로 모아 한 번에 보고한다**(`assertThat(violations).isEmpty()`).
- **반증**: 네 규칙 모두 위반 probe(포트 2개를 구현하는 임시 클래스, 추상 메서드 2개짜리 포트, 포트와 이름이 다른 서비스, public 메서드 2개짜리 서비스)로 실패를 확인하고 probe를 지웠다.
- **이 규칙이 생긴 김에 함께 바뀐 이름**: ceo 도메인 서비스 `ShopRequestCancelService`(요청 유형별 취소 분기)를 `ShopRequestCancellationService`로 개명했다(`backend/ceo-application/src/main/java/com/tastyhouse/application/shop/service/ShopRequestCancellationService.java`, 테스트 `ShopRequestCancellationServiceTest`). 유스케이스 `cancelRequest`의 서비스 이름이 `ShopRequestCancelService`가 되어 완전히 같은 이름이 생기기 때문이다 — **도메인 서비스는 명사형, 유스케이스 서비스는 동사형**으로 구분한다. `ServiceContextBoundaryTest.SEALED_VIOLATIONS`의 해당 FQN도 함께 바뀌었고 항목 수는 그대로다.
- **적용 범위 — 인바운드에만**: 유스케이스 1:1 분리(포트당 연산 1개·서비스 1개)는 `port.in` UseCase와 그 구현 서비스에만 적용한다. 네 규칙이 판정하는 것도 `port.in`뿐이다. 아웃바운드 `port.out` 포트와 persistence `*QueryAdapter`는 대상이 아니다 — 포트는 쓰는 쪽 기준(ISP)으로, 어댑터는 응집도로 크기를 정하고, 어댑터 1개가 포트 N개를 구현하는 형태를 유지한다. 근거는 `../infrastructure/persistence/AGENTS.md`의 [`<ctx>/query/` 절](../infrastructure/persistence/AGENTS.md#ctxquery--read-어댑터-cqrs-query-측-개정됨--읽기-경로-포트화) "유스케이스 1:1 분리는 read 어댑터에 적용하지 않는다" 항목에 있다.

### `queryServicesShouldNotDependOnWritePorts` carve-out 3건 — 목록에 새 항목을 추가하지 않는다

> **갱신 (JpaRepository 메서드 선언 금지) — carve-out은 이제 1건(`ShopDeliveryTipViewQueryService`)이다.** 제목의 "3건"은 앵커 호환을 위해 그대로 둔다. admin·ceo 아이디 중복확인 서비스는 원시값을 돌려주는 조회 유스케이스라 write 포트가 아니라 QueryPort(`AdminQueryPort`·`CeoOwnerQueryPort`, 구현은 `..query..`의 QueryAdapter)로 옮겼다. 같은 `existsByUsername`을 `AdminCreateService`·`CeoCreateService`(CommandService)가 생성 중복검사에 쓰므로 write 포트 메서드는 남는다 — 같은 행을 읽는 메서드가 두 포트에 있는 것은 목적이 달라 허용된다.

**대상**: `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`
→ `queryServicesShouldNotDependOnWritePorts()`

조회 유스케이스 서비스(`UseCaseServices.queries()` — 구현 포트명이 `QueryUseCase`로 끝나는 서비스. 유스케이스 분리 전에는 이름 접미어 `*QueryService`로 골랐다)는 write 포트(03a 이후 `..port.out.write..`, 과거 domain `..repository..`)를 주입하지 않는다 — 조회 트랜잭션(`readOnly = true`)에서 쓰기 경로가 열리는 것을 구조적으로 막는다.

~~**carve-out 3건은 각 앱에서 그대로 승계한 확정 판정이며, 이관 대상이 아니다.**~~ **(번복됨 — 위 갱신)** 아래 표의 admin·ceo 2행은 이력이다.

| FQN | 근거 |
|---|---|
| `com.tastyhouse.application.shop.service.ShopDeliveryTipViewQueryService` (web) | 유스케이스 분리로 carve-out이 `ShopQueryService`에서 write 포트를 실제로 쓰는 이 연산의 서비스로 1:1 이전됐다. write 포트를 배달팁 계산 경로가 도메인 서비스에 넘길 애그리거트 로드에 쓴다. 표현용 투영이 아니라 **도메인 계산 입력**이다 |
| `com.tastyhouse.application.admin.service.AdminUsernameExistsQueryService` | 유스케이스 분리로 carve-out이 `AdminQueryService`에서 write 포트 `AdminPersistencePort`를 실제로 쓰는 이 연산의 서비스로 1:1 이전됐다. 인증 계정의 존재 확인(`existsByUsername`, 시드 멱등성 확인)에 쓰이며 표현 목적 read model이 없다. 원시값 반환 경로다 |
| `com.tastyhouse.application.ceo.service.CeoOwnerUsernameExistsQueryService` | 유스케이스 분리로 carve-out이 `CeoOwnerQueryService`(당시 이름)에서 `existsByUsername`을 구현한 이 서비스로 1:1 이전됐다. 위 admin과 같은 인증 조회 경로다 |

**판정 기준은 simple name이 아니라 FQN이다.** 4개 모듈이 하나로 합쳐지면서 동명 클래스가 한 importer에 들어왔기 때문이다 — 예컨대 `ShopQueryService`는 web·admin·ceo에 각각 존재했으므로 `haveSimpleNameNotEndingWith("ShopQueryService")`를 그대로 두면 **의도한 web 1개가 아니라 3개 전부가 면제**되어 admin·ceo의 위반이 조용히 통과했다. 이후 개명·평탄화로 simple name이 다시 유일해졌지만 **FQN을 유지한다** — 나중에 같은 접미어의 형제가 생겨도 면제 범위가 넓어지지 않기 때문이다.

**이 목록에 새 항목을 추가하지 않는다.**

### `commandRecordsShouldBeBoundaryTyped` carve-out 3건 — 느슨한 판을 batch에 적용하지 않는다

> **갱신 (에러코드 모듈 분할) — carve-out은 이제 1건(`MultipartFile`)이다.** 아래 1번 `domain.exception..`도 소멸했다.

> **갱신 (덩어리 01) — carve-out은 이제 2건이다.** 3번 `domain.shared.page..`는 **소멸**했다 — `PageQuery`/`PageResult`가 `com.tastyhouse.application.shared.port.out.page`로 옮겨가 더 이상 domain 타입이 아니므로 제외할 대상이 없다. 1번 `domain.exception..`은 **유지**한다(표현 계층은 domain을 끊었지만 Command는 `application`에 살아 여전히 domain을 보고, compact constructor 가드가 `BusinessException`을 던진다). 제목의 "3건"은 앵커 호환을 위해 그대로 둔다.

**대상**: `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`
→ `commandRecordsShouldBeBoundaryTyped()`

Command record는 경계 타입만 싣는다. carve-out 3건을 **그대로 유지**한다.

1. ~~`com.tastyhouse.domain.exception..` — `BusinessException`·`ErrorCode`는 애그리거트가 아니라 전 계층이 공유하는 **횡단 관심사(에러 계약)**이고, compact constructor의 구조적 가드가 이를 던져야 응답 코드가 나머지 경로와 같은 형태로 나간다.~~ **(소멸 — 에러코드 모듈 분할, 아래 "에러 카탈로그 가드" 항목 참고)** Command의 구조적 가드는 이제 `ApplicationException(ApplicationErrorCode.INVALID_INPUT)`을 던지고 domain 타입을 import하지 않으므로 제외할 대상이 없다. 같은 carve-out이 `BatchSchedulerRulesTest#inboundPortsShouldBeBoundaryTyped`에서도 제거됐다.
2. `org.springframework.web.multipart..`(`MultipartFile`) — 업로드를 받는 연산은 `method(XxxCommand, MultipartFile)`처럼 별도 파라미터로 두는 것이 규정된 형태이고, ArchUnit 의존 그래프는 같은 패키지 UseCase 인터페이스의 메서드 파라미터까지 함께 잡는다. Command **필드**로 실리는 것은 `commandRecordsShouldNotHoldMultipartFile`이 따로 막는다.
3. ~~`com.tastyhouse.domain.shared.page..`~~ **(소멸 — 덩어리 01, 위 갱신 참고)** — 근거는 `MultipartFile` carve-out과 **동일한 구조**다. 이 규칙이 겨냥하는 것은 Command record가 **필드로** 도메인 모델을 싣는 것인데, ArchUnit은 같은 `..port.in..` 패키지에 사는 **QueryUseCase의 메서드 시그니처**까지 함께 잡는다. 목록 반환 타입이 `PaginationResponse`에서 `PageResult`로 바뀌면서 걸린 건들은 **전부 반환 타입이며 Command 필드는 한 건도 없다**(실측 확인).

즉 이것은 규칙을 무르게 하는 것이 아니라, 규칙이 애초에 겨냥하지 않던 대상을 제외하는 것이다. 도메인 **모델**(`domain.{shop,order,member}.model..` 등)은 그대로 금지이며, Command가 실제로 도메인 타입을 필드로 실으면 여전히 걸린다.

**batch 제외는 importer가 아니라 `@BatchApp` 마커로 표현한다.** batch에는 Command record가 없고 인바운드 포트가 `void foo()`뿐이라 carve-out이 `domain.exception..` 하나인 **엄격판**을 쓸 수 있으며, 그쪽은 `BatchSchedulerRulesTest.inboundPortsShouldBeBoundaryTyped()`가 맡는다. **batch를 이 규칙에 함께 넣으면 느슨한 판(당시 3-carve-out, 지금 2-carve-out)에 얹혀 엄격함을 잃는다.**

**`allowEmptyShould(true)`는 이 파일 어디에도 쓰지 않는다.** 규칙이 대상을 잃으면 공허하게 통과시키지 말고 규칙을 지우거나 anchor를 고친다(`RuleAnchorTest`가 자동 검증).

### `inboundPortsShouldBeBoundaryTyped` — carve-out 1건뿐인 엄격판, 죽은 코드로 보고 지우지 말 것

**대상**: `backend/application/src/test/java/com/tastyhouse/application/architecture/BatchSchedulerRulesTest.java`
→ `inboundPortsShouldBeBoundaryTyped()`

`com.tastyhouse.domain.exception`만 carve-out으로 허용한다 — 예외는 횡단 관심사라 계층 칸이 없다. **web·admin·ceo가 쓰는 페이징·업로드 carve-out 2건은 여기 없다**: batch에는 목록 조회도 파일 업로드도 없어 느슨하게 할 이유가 없다. 이것이 이 규칙을 마커로 좁혀 둔 이유다.

**주의 — 지금은 검사할 표면이 없다.** UseCase 7개가 전부 파라미터·반환값 없는 `void foo()` 하나뿐이라 의존 그래프에 잡힐 타입 자체가 0건이다(`inboundPortsExist`가 세는 것은 "인터페이스가 존재함"이지 "검사 대상이 있음"이 아니다). **규칙과 carve-out은 UseCase가 처음으로 파라미터를 갖는 시점을 위해 미리 세워 둔 것이므로, 지금 아무것도 걸리지 않는다는 이유로 carve-out을 죽은 코드로 보고 지우지 말 것.**

같은 파일의 다른 고정 사항.

- **대상은 importer가 아니라 `@BatchApp` 마커로 좁힌다.** 평탄화로 앱별 패키지가 사라졌기 때문이다. importer는 모듈 전체를 훑고 각 규칙이 마커로 대상을 좁힌다 — 다른 앱까지 대상에 들어오면 그 앱들이 정당하게 쓰는 페이징·업로드 타입에 걸려 실패한다.
- **대상이 0건이 된 규칙은 `allowEmptyShould(true)`로 공허 통과를 열지 않고 삭제한다**는 것이 이 저장소의 방침이다(`responseRecordsShouldBeDomainAndInfraFree`와 그 anchor `responseRecordsExist`를 실제로 삭제한 선례).
- anchor 2종은 batch가 규모가 작아 **정확히 일치**로 둔다(다른 앱은 하한). 잡이 늘거나 줄면 **이 숫자를 의식적으로 고치게 되는 것이 의도다.**

### `DESERIALIZED_COMMANDS` — 고아 Command record 봉인, 새 항목을 추가하지 않는다

> **(번복됨 — 앱 마커 제거: 목록 삭제)** `AppOwnership`과 함께 `DESERIALIZED_COMMANDS`도 삭제됐다. Command record의 소속을 유도할 필요가 없어졌기 때문이다 — `ShopStorePriceVerificationItemCommand`는 지금 `ceo-application` 모듈에 있고, 그 사실 자체가 소속이다. 아래 "multipart 문자열 파트를 서비스가 `ObjectMapper`로 역직렬화한다"는 설명은 여전히 사실이며, 이 record를 정적 참조가 없다는 이유로 죽은 코드로 보고 지우면 안 된다는 점도 그대로다.

**대상**: `backend/application/src/testFixtures/java/com/tastyhouse/architecture/AppOwnership.java`
→ `DESERIALIZED_COMMANDS`

봉인 구성원 1개 — `com.tastyhouse.application.shop.port.in.ShopStorePriceVerificationItemCommand` (`CeoApp`).

`ShopStorePriceVerificationItemCommand`는 multipart의 **문자열 파트**로 들어온다. 컨트롤러도 Request record도 domain-free여야 해 파싱을 할 수 없으므로, Command가 원문을 `String items`로 담아 넘기고 `ShopStorePriceVerificationRequestService`(당시 이름 `ShopStorePriceVerificationCommandService`)가 `ObjectMapper`로 이 record 목록으로 역직렬화한다. 그래서 이 record는 어느 UseCase 시그니처에도, 어느 부모 Command의 컴포넌트로도 등장하지 않는다 — **유도가 닿을 수 없는 정상 형태이지 죽은 코드가 아니다.**

**이 목록에 새 항목을 추가하지 않는다.** 고아로 잡히는 record는 대개 진짜 죽은 코드이므로, 추가하기 전에 그 record를 **어디서 만드는지**를 먼저 찾는다. 여기 담을 수 있는 것은 "런타임 역직렬화로만 생성되어 정적 참조가 존재할 수 없는" 경우뿐이다.

### `CeoAuthCommandService` — 기록 실패 정책의 의도적 비대칭 (인증 조회 carve-out)

**대상**: `backend/ceo-application/src/main/java/com/tastyhouse/application/auth/service/CeoLoginService.java` → `login` · `recordFailureQuietly`

> **현재 이름(유스케이스 분리)**: 제목의 `CeoAuthCommandService`는 당시 이름이다(당시 위치 `backend/application/.../auth/service/CeoAuthCommandService.java`). login·refresh·logout 세 연산이 `CeoLoginService`·`CeoTokenRefreshService`·`CeoLogoutService`로 나뉘었고, 아래 정책은 로그인 연산을 맡은 `CeoLoginService`에 그대로 있다. 세 서비스 모두 트랜잭션을 열지 않는다(원본에 클래스 레벨 `@Transactional`이 없었다). 기록 호출도 `CeoLoginHistoryCommandUseCase`(당시 이름) 하나에서 `CeoLoginSuccessRecordUseCase#recordSuccess`·`CeoLoginFailureRecordUseCase#recordFailure` 두 포트로 나뉘었다.

**기록 실패 시 정책은 성공·실패 경로가 의도적으로 비대칭이다.**

- **성공 경로**: 기록 실패를 그대로 전파한다. 접속기록 없이 토큰이 발급되는 상태를 만들지 않는다 — 개인정보처리시스템 접속기록은 법적 요구사항이므로, 남기지 못했다면 접속도 허용하지 않는 편이 옳다.
- **실패 경로**: 기록 실패를 catch·로깅하고 원래 인증 예외를 rethrow한다. **감사 쓰기 실패가 인증 실패 응답 계약(401 `CEO_AUTHENTICATION_FAILED` 등)을 500으로 바꾸면 안 된다.**

이 비대칭은 `CeoLoginServiceTest`(당시 이름 `AuthCommandServiceTest`)가 봉인한다.

### `AuthCommandServiceTest` — 점주 로그인 접속기록 배선 봉인 (소셜 4종 분기 carve-out)

**대상**: `backend/ceo-application/src/test/java/com/tastyhouse/application/auth/service/CeoLoginServiceTest.java`

> **현재 이름(유스케이스 분리)**: 제목의 `AuthCommandServiceTest`(`new CeoAuthCommandService(`로 대상을 만들던 테스트)는 당시 이름이다. 로그인 연산의 새 서비스 `CeoLoginService`를 대상으로 바꾸며 클래스명도 `CeoLoginServiceTest`로 바꿨다. 기록 포트는 `CeoLoginSuccessRecordUseCase`·`CeoLoginFailureRecordUseCase` mock이다. 봉인 내용은 아래 그대로다.

이 테스트가 지키는 것은 네 가지다.

- 성공·실패 양쪽 모두 이력을 남긴다(실패 이력이 인증 예외와 함께 사라지지 않는다).
- 실패 시 **원래 인증 예외가 그대로 rethrow**된다 — 응답 계약이 바뀌지 않는다.
- 존재하지 않는 username은 기록하지 않는다(**계정 존재 여부 탐색 표면 방지**).
- 기록 실패 시 정책이 성공·실패 경로에서 **의도적으로 비대칭**이다.

### 읽기 계약 carve-out 5종 — 도메인 타입을 강등해 나르는 이유

아래 record들은 전부 **api 모듈이 도메인 타입을 알 수 없다는 경계** 때문에 존재한다. "중복 DTO"로 보고 합치거나 도메인 타입을 그대로 실으면 ArchUnit 규칙이 깨진다.

| 대상 (`backend/application/src/main/java/com/tastyhouse/application/...`) | 봉인 취지 |
|---|---|
| `product/port/out/ProductAvailabilityChangeView.java` | **거처는 앱 네임스페이스이고 읽기 계약 패키지(`com.tastyhouse.application..port.out`)가 아니다.** 판매상태 변경은 **Command 경로**의 반환값이라 조회 계약이 아니며, 읽기 계약 패키지에 두면 `commandServicesShouldNotDependOnQueryPorts`(CQRS 교차 주입 금지)가 CommandService의 반환 타입을 위반으로 잡는다. ~~`ErrorCode`는 그대로 담는다 — 에러 계약은 **횡단 관심사**라 api 모듈에서도 참조가 허용된 carve-out(`domain.exception..`)이다~~ **번복됨(덩어리 01)**: `Failure`는 `ErrorCode errorCode` 대신 `String code, String message`를 싣는다 — api 모듈의 `domain.exception..` carve-out이 사라졌기 때문이다. **(번복됨 — 에러코드 모듈 분할)** 이 타입은 `ProductAvailabilityChangeResult`(domain `product/model`에서 이동)와 함께 `backend/ceo-application/src/main/java/com/tastyhouse/application/product/service/`로 옮겨져 `ProductAvailabilityFailure`가 됐다 — ceo-application만 만들고 쓰기 때문이다. 상세는 `backend/ceo-application/AGENTS.md` |
| `region/port/out/AdminDongBoundaryViewResult.java` | `AdminDongBoundaryResult`는 DAO가 읽어 온 **인코딩된** `boundary` 문자열을 그대로 들고 있어 그 자체로는 응답을 만들 수 없다. 디코딩은 ~~`GeoRingsPort`가~~ **(03b 번복 — `GeoRingsQueryPort`·persistence `GeoRingsResolver`는 삭제됐고, ceo-application `region/service/AdminDongBoundaryQueryService`(당시 이름 `AdminDongQueryService`)가 `domain/shared/geo/GeoPolygonTextCodec.decodeRings`를 직접 호출해)** 수행하므로 **application에 남아야 하고**, 표현 계약이 `from(Result)` 한 번으로 끝낼 수 있도록 디코딩을 마친 이 타입을 따로 둔다. 좌표를 `GeoRing`·`GeoPoint`가 아니라 낱개 `BigDecimal` 쌍(`Point`)으로 내리는 이유는 **`controllersShouldBeDomainFree`의 carve-out이 `domain.shared.page..`와 도메인 enum뿐이고 `domain.shared.geo..`는 포함되지 않기** 때문이다(덩어리 01 이후로는 carve-out 자체가 없어 더 분명하다). 리포 전체에서 api 모듈이 geo 타입을 참조하는 곳은 한 곳도 없으며, **그 경계를 깨지 않는다** |
| `review/port/out/ReviewBlindReasonView.java` | 카탈로그는 도메인 enum의 `values()`를 훑어 만드는데 그 메서드는 api 모듈에 허용된 accessor가 아니므로(`apiModuleShouldOnlyReadDomainEnums` — 덩어리 01로 삭제됐고, 지금은 api 모듈이 domain을 아예 못 봐서 같은 결론) 목록 구성이 application에 남는다. **도메인 enum을 그대로 담지 않고 문자열로 강등해 나른다** — 인바운드 포트의 반환 타입에 `com.tastyhouse.domain..`이 실리면 `commandRecordsShouldBeBoundaryTyped`(carve-out은 예외·페이징 계약뿐)에 걸린다. **목록 요소는 제네릭 타입 인자로도 잡힌다** |
| `shop/port/out/GeoPointView.java` | 도형 계산은 도메인 기하 타입으로 수행하는데 api 모듈은 그 타입을 알 수 없다 — `apiModuleShouldBeDomainModelFree`의 carve-out은 `domain.exception..`·`domain.shared.page..`·도메인 enum뿐이고 **`domain.shared.geo..`는 포함되지 않는다**(덩어리 01로 carve-out이 전부 사라졌다). 추가로 **컴포넌트 선언 순서는 알파벳순(`latitude` → `longitude`)이다** — 둘 다 `BigDecimal`이라 순서가 어긋나면 컴파일은 통과하고 **값만 조용히 뒤바뀐다** |
| `shop/port/out/ShopStorePriceVerificationViewResult.java` | 세 출처를 합친다 — 최신 인증 요청(애그리거트), 인증 여부 플래그, 미충족 메뉴 목록(도메인 서비스). 앞의 둘은 애그리거트에서, 마지막은 도메인 서비스에서 나오므로 표현 계약이 직접 받을 수 없다(`apiModuleShouldBeDomainModelFree`). 미충족 사유는 `domain.product.model`의 `StorePriceUnverifiedItem`을 그대로 넘기지 않고 `UnverifiedItem`으로 옮겨 담는다 — 그 타입은 domain 타입이고 api 모듈은 `com.tastyhouse.domain..`을 carve-out 없이 전면 금지하기 때문이다(~~`domain.product.service`에 있어 api 모듈의 carve-out 어디에도 들어가지 않는다~~ **번복됨 — domain service→model 흡수**로 패키지가 `model`로 바뀌었다). ~~사유 enum 자체는 carve-out 대상이라 그대로 나르고, 문자열 강등은 표현 계약이 수행한다~~ **번복됨(덩어리 01)**: `status`·`UnverifiedItem.reason`도 `String`으로 강등해 나르고, 강등은 이 View를 만드는 서비스가 한다 |

### `ShopStorePriceVerificationCommandService` — 인덱스 기록이 도메인이 아니라 이 서비스에 있는 이유

**대상**: `backend/ceo-application/src/main/java/com/tastyhouse/application/shop/service/ShopStorePriceVerificationRequestService.java` → `requestVerification` · `toItemSpecs`

> **현재 이름(유스케이스 분리)**: 제목의 `ShopStorePriceVerificationCommandService`는 당시 이름이다(당시 위치 `backend/application/.../shop/service/`). 이 서비스의 유일한 연산 `requestVerification`이 `ShopStorePriceVerificationRequestUseCase`를 구현하는 `ShopStorePriceVerificationRequestService`가 됐고, 아래 판단은 그대로 이 서비스에 있다.

- **`items`가 JSON 문자열인 것은 요청 형식이 multipart이기 때문이다.** 가격표 이미지와 대상 목록은 한 트랜잭션에 함께 들어와야 한다 — 2단 요청으로 쪼개면 중간에서 끊긴 요청이 첨부만 있고 대상이 없는 고아 상태로 남고, 관리자 검수 큐에 검수할 수 없는 건이 쌓인다. multipart는 JSON 바디를 함께 실을 수 없으므로 목록만 문자열 파트로 받아 여기서 파싱한다.
- **인덱스 기록이 도메인이 아니라 이 서비스에 있는 것은 컨텍스트 경계 때문이다.** 다른 요청 유형(`ShopImageApprovalService`·`ShopDeliveryAreaAdjustmentService`)은 shop 컨텍스트 소유라 도메인 서비스가 직접 `ShopRequestIndexRecorder`를 호출한다. 그러나 인증 요청 애그리거트는 **product** 컨텍스트 소유여서, 그 도메인 서비스가 `shop.service`를 호출하면 `ContextBoundaryTest` 위반이 되고 **봉인 목록은 늘릴 수 없다.** 두 컨텍스트를 한 트랜잭션에서 잇는 일은 표현 계층의 몫이다.
- `MultipartFile`을 파라미터로 받는 것은 **파일 업로드 경계의 문서화된 예외**다 — 규격 검증이 업로드보다 앞서야 하고, 도메인은 통과분의 `fileId`만 받는다.

### QueryDSL 투영 전용 생성자 3건 — "never used" 경고를 근거로 삭제하지 말 것

**대상** (`backend/application/src/main/java/com/tastyhouse/application/review/port/out/`)

| record | 좁은 시그니처가 제외하는 것 | 투영 호출부 |
|---|---|---|
| `ReviewDetailResult` | 1:N인 이미지·태그 | `ReviewQueryAdapter` |
| `LatestReviewListItemResult` | 1:N인 이미지(`imageUrls`를 빈 목록으로 채운다) | `ReviewQueryAdapter`(6개 쿼리) |
| `ReviewManagementDetailResult` | 1:N인 이미지·태그 | `ReviewManagementQueryAdapter#findReviewManagementDetail` |

세 record는 canonical 생성자 외에 **QueryDSL 투영 전용 생성자**를 하나 더 갖는다. DAO가
`Projections.constructor`로 **리플렉션 호출**하므로 정적 호출부가 0개이고, 그래서 **IDE가
"never used"로 표시한다.** 그 경고를 근거로 지우면 **컴파일은 통과하고 그 쿼리가 실행되는 순간에만
500이 난다.**

- **파라미터 개수·타입·순서가 DAO의 select 인자와 정확히 일치해야 한다.** `Projections.constructor`는
  `Class<?>`를 받아 런타임에 생성자를 찾으므로 불일치도 컴파일에 걸리지 않는다. `@QueryProjection`에서
  전환하며 **컴파일 게이트가 사라졌고, 인자 개수 가드 테스트
  (`infrastructure:persistence`의 `ProjectionConstructorMatchingTest`)가 유일한 방어선**이다.
- record는 반드시 `public`이어야 한다 — package-private이면 `getConstructors()`가 찾지 못해
  `ExpressionException: No constructor found`로 실패한다(`ShopRiderGuidePickupPresenceResult` 장애 선례).
- **원문 주석 1건은 낡아 있었으므로 여기 옮기며 교정했다** — `ReviewManagementDetailResult`의 주석은
  "제거하면 Q타입이 생성되지 않아 빌드가 깨진다"고 적혀 있었으나, `@QueryProjection` → `Projections.constructor`
  전환으로 **`QReviewManagementDetailResult` Q타입은 더 이상 생성되지 않는다**(실측 0건). 실제 위험은
  빌드 실패가 아니라 **런타임 투영 실패**다.

### `ResultWitherComponentOrderTest` — `port/out` Result wither의 컴포넌트 순서 가드 (이 모듈의 유일한 방어선)

**대상**: `backend/application/src/test/java/com/tastyhouse/application/architecture/ResultWitherComponentOrderTest.java`
→ `witherArgumentsShouldFollowComponentOrder`, `detectWitherReordering`, 짝 테스트 `detectorShouldCatchSwappedSlots`

**검사 대상**: (앱 마커 제거 후) 소스 루트 5개 — `backend/{application,web-application,admin-application,ceo-application,batch-application}/src/main/java` — 아래 `com/tastyhouse/application/**/port/out/*.java`의 record가 가진(과거에는 `backend/application/src/main/java`만 훑었다. 앱 모듈의 Command 반환 Result도 빠지지 않게 하려고 넓혔다)
`public {자기 record명} with\w+(...)` 메서드 전부. 도입 시점 15개였고, 02 롤아웃(URL 투영)으로 URL 변환용
wither 3개가 빠져 **현재 13개**다 — `MenuReviewWritableItemResult#withProductImageUrl`·
`MenuReviewListItemResult#withMemberProfileImageUrl`은 호출부가 0이 되어 삭제했고,
`OrderProductResult#withResolvedImageUrl(url, options)`는 URL 인자를 떼고 `#withOptions(options)`로 축소했다.
`OrderProductResult#withOptions` · `OrderDetailResult#withOrderProducts`/`#withPayment` ·
`ReviewBlindNoticeResult#withImageUrls` · `ReviewBlindRequestDetailResult#withUrls` ·
`ReviewManagementDetailResult#withImageUrls`/`#withTagNames` · `ReviewDetailResult#withImageUrls`/`#withTagNames` ·
`ShopReviewManagementDetailResult#withCollections` ·
`ShopReviewManagementListItemResult#withImageUrls`/`#withProductNames` · `LatestReviewListItemResult#withImageUrls`

- **wither는 전 컴포넌트를 위치 기반으로 재나열한다.** 인접한 같은 타입 컴포넌트를 바꿔 써도 컴파일되고 어떤
  테스트도 잡지 못한다. 가장 위험한 것은 `ShopReviewManagementListItemResult`(13개 컴포넌트를 **두 번**
  재나열하며 `imageUrls`·`productNames`가 인접한 `List<String>`)와 `OrderDetailResult`(24개 컴포넌트 중
  금액·포인트 `Integer`가 9개, 가게·주문자 `String`이 5개 연속)다.
- **`infrastructure:persistence`의 가드 2종은 이 모듈을 스캔하지 않는다.** `ProjectionConstructorMatchingTest`는
  자기 모듈 소스(`Projections.constructor` 인자)만 보고, 재조립 헬퍼 봉인은 개수만 센다. 그래서 이 테스트가
  wither 순서에 대한 **유일한 방어선**이다. 지우거나 `@Disabled`하지 않는다.
- **wither는 제거 대상이 아니다.** 별도 쿼리로 얻는 컬렉션(이미지·태그·상품명·주문 옵션)과 서브 애그리거트(주문 상품·결제)
  보강은 컬럼 표현식이 될 수 없어 post-fetch가 정상 형태다. 반대로 **URL 슬롯만 바꿔 끼우는 wither는 만들지 않는다** —
  URL 변환은 `infrastructure:persistence`가 투영식의 `fileUrlResolver.urlOf(...)`로 끝내고, 그쪽의 `withResolved*`
  재조립 헬퍼는 02 롤아웃으로 0개가 됐다.

**판정 방식** — 인자마다 아래 셋 중 하나로 본다. 어느 것에도 해당하지 않는 인자(`List.of()`·메서드 호출 등)는
건너뛴다.

| 인자 형태 | 의미 | 실패 조건 |
|---|---|---|
| `this.foo`(인자 전체가 이 형태일 때만) | 기존 필드 복사 | `foo`가 그 자리의 컴포넌트명과 다르면 |
| `foo`(wither 파라미터가 아님) | 기존 필드 복사(`this.` 생략형) | `foo`가 컴포넌트명인데 그 자리와 다르면 |
| `foo`(wither 파라미터) | 교체 슬롯 | 파라미터명이 **컴포넌트명과 같은데** 그 자리와 다르면. 이름이 다른 파라미터(`resolvedImageUrl`)는 건너뛴다 |

인자 개수가 컴포넌트 개수와 다르면 그것만으로 실패다. 이와 별개로 두 가지를 더 단정한다.

- **wither 파라미터는 전부 인자로 쓰여야 한다.** `withProductNames(productNames)`의 본문이 `this.productNames`를
  그대로 쓰면 모든 인자가 제자리라 순서 검사는 통과하지만, wither가 **기존 값을 그대로 반환**한다. 두 wither가
  같은 13개 인자를 복붙하는 `ShopReviewManagementListItemResult`에서 가장 일어나기 쉬운 실수다.
- **리플렉션으로 센 wither 수 = 소스에서 파싱한 wither 수.** 레코드마다 `getDeclaredMethods()` 중 비정적·비합성이고
  이름이 `with[A-Z]…`이며 반환 타입이 자기 record인 메서드를 세어, 시그니처 패턴(`public {Record} with\w+(`)이
  놓친 wither(`final`·제네릭 메서드·package-private 등)를 조용히 건너뛰지 않게 한다. 같은 이유로 `port/out`에
  record 선언이 있는데 클래스를 로드하지 못하면 건너뛰지 않고 실패한다.

- **`this.` 접두 인자만 보면 안 된다.** 원 스펙은 `this.` 인자만 검사하도록 설계했으나, 도입 시점 전수 확인에서
  15개 중 **8개가 `this.` 없이 필드명을 그대로** 쓰는 것으로 드러났다(`OrderProductResult` 1 ·
  `OrderDetailResult` 2 · `ReviewManagementDetailResult` 2 · `ReviewDetailResult` 2 · `LatestReviewListItemResult` 1).
  `this.`만 보면 이 8개는 검사 인자가 0개가 되어 공허하게 통과한다 — 그중 `OrderDetailResult`가 금액·포인트
  `Integer` 9개가 연속한 가장 위험한 record다. 그래서 파라미터가 아닌 bare 식별자도 필드 참조로 판정한다.
- **오탐이 없는 근거**: record 메서드 본문에서 파라미터가 아닌 bare 식별자는 컴포넌트 필드를 가리킬 수밖에 없고,
  현존 wither는 교체 슬롯에 파라미터명을 그대로 쓴다(`imageUrls` 파라미터 → `imageUrls` 자리). 교체 슬롯에
  이름이 다른 파라미터를 쓰는 형태(`resolvedImageUrl`)는 판정하지 않을 뿐 실패시키지 않는다.
- **시그니처 파싱만 `<`·`>`를 괄호 깊이로 센다.** `List<String> a, Map<K, V> b` 같은 제네릭 쉼표 때문이다.
  `new` 인자 파싱에서는 세지 않는다 — 인자 안의 `->`·비교 연산자가 깊이를 깨뜨린다.
- **공허 통과를 막는 장치 2겹**: `checked` 카운터는 `isPositive()`로 스캔 경로가 살아 있음을 단정하고
  (도입 시점 실측 15), 짝 테스트 `detectorShouldCatchSwappedSlots`는 합성 record `SwapProbe`에 대해 세 형태의
  교차를 **실제로 잡아내는지** 단정한다. 현존 wither가 전부 통과하는 상태에서는 검출기가 고장 나도 알 수 없으므로,
  짝 테스트를 지우지 않는다. `checked`를 15로 고정하지 않는 것은 wither 추가가 정상 변경이기 때문이다 — 누락은
  위 리플렉션 대조가 record 단위로 잡는다.
- **알려진 한계**: `new {Record}(` 탐색은 시그니처 뒤 **첫 번째** 등장을 쓰므로, 다른 wither에 위임하거나
  (`return withBoth(x, this.y);`) 본문에서 두 번 생성하는(if/else) wither는 정확히 검사되지 않는다. 현존 15개에는
  없는 형태다. 보조 생성자의 `this(...)` 위임 인자 순서(`OrderProductResult`의 축약 생성자)도 이 가드의 대상이 아니다.
- **이 테스트가 실패하면 그것은 가드의 오작동이 아니라 발견된 프로덕션 버그다.** 테스트를 고치거나 완화하지 말고,
  리팩터링과 분리한 별도 커밋(`fix(query): ...`)으로 해당 record의 wither 인자 순서를 바로잡은 뒤, 그 Result를
  쓰는 엔드포인트를 실제로 호출해 값이 교차돼 있었는지 확인한다.

### `//noinspection BusyWait` — 이 억제 마커는 정당하며 제거 대상이 아니다

**대상**: `backend/application/src/main/java/com/tastyhouse/application/crawling/bbq/BbqService.java`
→ 카테고리 루프의 `Thread.sleep(10000)`

**이 모듈에서 유일하게 남아 있는 주석 형태의 코드**다(챕터 04의 주석 전량 제거에서 의도적으로 제외).
정적분석 도구가 읽는 억제 마커이므로 주석이 아니라 **코드로 취급한다.**

억제가 정당한 이유는 **busy-wait가 아니라 외부 BBQ 서버 부하 방지를 위한 의도적인 요청 간
지연**이기 때문이다. 루프 안의 `Thread.sleep`이라는 형태만 보고 "폴링을 이벤트 대기로 바꾸라"는
지적으로 오인해 지연 자체를 없애면, 크롤링이 외부 서버를 연속 타격한다. 마커와 지연 둘 다 유지한다.

### 이벤트 리스너 단위 테스트 12종 — 현재 동작 봉인 (공통 규칙)

리스너 테스트는 **리스너의 현재 동작을 봉인하는 순수 단위 테스트**다. 아래가 12개 파일 전부에 적용되는 공통 규칙이며, 개별 항목은 이 규칙에서 벗어나는 것만 아래에 따로 적는다.

- **스프링 컨텍스트 없이 리스너를 직접 생성해 핸들러를 호출한다** — `AFTER_COMMIT` 발화 자체는 프레임워크 몫이라 검증 대상이 아니다. 스프링 배선을 검증하려고 `@SpringBootTest`를 붙이지 않는다.
- 협력자 없이 기록만 하는 리스너는 **무엇이 기록되는지**를 `ListenerLogCapture`로 확인한다.

**공통 유틸**: `backend/application/src/test/java/com/tastyhouse/application/shared/listener/ListenerLogCapture.java`

리스너 12개 중 7개는 협력자 없이 `log.info(...)`만 수행한다. 이런 리스너에서 "무엇을 하는지"는 곧 "무엇을 기록하는지"이므로, **로그를 관측하지 않으면 핸들러 본문을 통째로 지워도 통과하는 공허한 테스트만 남는다.** 그래서 Logback `ListAppender`를 대상 로거에 직접 붙여, 이벤트의 어떤 값이 기록에 반영되는지까지 봉인한다. **사용 후에는 반드시 `detach()`를 호출한다**(JUnit `@AfterEach`) — 떼지 않으면 같은 로거를 쓰는 다른 테스트가 실행될 때 이벤트가 계속 쌓인다.

공통 규칙만 적용되는 파일 — `coupon/listener/CouponEventListenerTest.java` · `member/listener/MemberEventListenerTest.java`(가입·탈퇴는 web-api와 admin-api 양쪽에서 트리거되지만 리스너 자체는 발행 경로를 알지 않는다) · `policy/listener/PolicyActivatedEventListenerTest.java`.

#### 개별 예외 — 공통 규칙 위에 추가로 봉인하는 것

| 대상 (`backend/application/src/test/java/com/tastyhouse/application/...`) | 추가로 봉인하는 것 |
|---|---|
| `file/listener/FileUploadedEventListenerTest.java` | 기록되는 것은 **저장 경로**이지 표시용 URL이 아니다 — URL 변환은 조회 시점에 query DAO가 `FileUrlResolver`로 수행하므로 리스너가 경로를 그대로 남기는 것이 정상이다 |
| `mail/listener/MailVerificationEventListenerTest.java` | **이 리스너가 메일을 발송하지 않는 것이 정상**이라는 점을 함께 고정한다 — 이 이벤트는 인증 **완료** 시점이고 발송은 **발급** 시점에 필요하므로, 발송은 `MailVerificationService#issue`가 발급과 원자적으로 수행한다 |
| `sms/listener/SmsVerificationEventListenerTest.java` | 위와 동일한 이유로 **발송하지 않음**을 고정한다 — 발송은 `SmsVerificationService#issue`가 발급과 원자적으로 수행한다 |
| `point/listener/PointEventListenerTest.java` | **이 리스너가 잔액을 건드리지 않는 것이 정상**임을 고정한다 — 포인트 증감은 `PointLedgerService`가 이벤트 발행 **이전에** 이미 수행했고 리스너는 기록만 한다. **협력자를 주입받지 않는 생성자가 그 증거이며, 여기에 원장 서비스가 추가되면 이중 정산이 된다** |
| `member/listener/ReferralRegisteredEventListenerTest.java` | referral↔point 두 컨텍스트를 잇는 지점이라 검증 대상이 로깅이 아니라 **적립 2건과 보상 완료 전이가 모두, 그리고 그 순서대로 일어나는가**이다 |
| `notification/listener/ReviewOwnerReplyEventListenerTest.java` | review↔notification을 잇는 지점이라 검증 대상은 **답변 등록 이벤트가 리뷰 작성자 앞으로 알림을 적재하는가**이다. 수신자가 `reviewerMemberId`(작성자)여야 하고 이동 대상이 그 리뷰여야 한다. 가게명은 `ShopBasicInfoQueryPort#findShopName`으로 조회하므로(테스트는 `mock(ShopBasicInfoQueryPort.class)`) **조회가 비어 있는 경우까지 함께 봉인한다** — 알림 본문에 "null 사장님"이 새는 것을 막기 위함이다 |
| `notification/listener/ReviewBlindApprovedEventListenerTest.java` | 게시중단 승인 이벤트가 **리뷰 작성자 앞으로 게시중단 기한을 담은 알림을 적재하는가**를 `NotificationService` mock의 `notifyReviewBlindApproved` 호출 인자로 검증한다. 가게명 조회가 없는 것이 이 리스너의 의도이므로(아래 배치 근거 참고) 조회 협력자를 추가하지 않는다 |
| `payment/listener/PaymentEventListenerTest.java` | 로그만 남기는 다른 리스너와 달리 **실제 금전 효과**(포인트 증감)를 낸다. 따라서 "무엇을 기록하는가"가 아니라 **"어떤 조건에서 원장 서비스를 호출/미호출하는가"**를 검증한다 — 조건 분기가 잘못되면 적립이 이중으로 되거나 환급이 누락되며, `AFTER_COMMIT`이라 실패해도 재시도가 없다. 특히 **환불 요청 접수 시점에는 아무것도 하지 않는 것이 이 핸들러의 계약이다** — 접수 시점에 포인트가 움직이면 이후 취소가 확정될 때 `PaymentCancelledEvent`가 같은 금액을 다시 반영해 **이중 정산**이 된다 |
| `product/listener/ProductMenuReviewEventListenerTest.java` | 상품 평점·평가 수라는 **영속 상태**를 갱신하므로 "어떤 상품 id로 통계 갱신을 호출하는가"를 검증한다. **이벤트 3종 모두가 같은 재집계를 트리거해야 한다** — 하나라도 빠지면 `PRODUCT.rating`이 조용히 낡는다 |

### 이벤트 리스너 — 지우거나 되돌리면 안 되는 것

`infrastructure:persistence`에서 리스너와 함께 옮겨 온 금지 항목이다. 아래 `.../` 경로의 루트는 `backend/application/src/main/java/com/tastyhouse/application/`다.

#### 알림 리스너의 `@Async` + `AFTER_COMMIT` + `REQUIRES_NEW` 3종 세트를 부분적으로 떼지 않는다

**대상**: `.../notification/listener/ReviewBlindApprovedEventListener.java` · `.../notification/listener/ReviewOwnerReplyEventListener.java` · `.../product/listener/ProductMenuReviewEventListener.java`

- `@Async`가 빠지면 호출 스레드에서 동기 실행되어 **알림 실패가 원본 API로 전파된다** — DB에는 반영됐는데 화면은 실패로 뜬다.
- `REQUIRES_NEW`가 빠지면 커밋될 트랜잭션이 없어 **리스너가 조용히 아무것도 남기지 않는다.**

`ProductMenuReviewEventListener`의 **`productId == null` 가드도 유지한다** — 컬럼이 NOT NULL이라는 이유로 지우면, 향후 발행 경로가 늘어 null이 실릴 때 그 예외가 `AFTER_COMMIT`에서 조용히 유실된다.

#### `PaymentEventListener#onRefundRequested`는 포인트를 건드리지 않는 것이 계약이다

**대상**: `.../payment/listener/PaymentEventListener.java` → `onRefundRequested`

이 이벤트는 환불 **접수** 시점이며 실제 금전 정산은 결제가 취소로 확정될 때 `PaymentCancelledEvent`가 수행한다. **여기서 포인트를 함께 움직이면 승인 전 요청만으로 잔액이 바뀌고, 이후 취소 확정 시 같은 금액이 두 번 반영된다.** 이 핸들러는 접수 사실만 남기며, DB를 쓰지 않으므로 `REQUIRES_NEW`도 열지 않는다.

`onPaymentCompleted`의 **현장 결제 한정 적립**도 계약이다 — PG 결제는 주문 접수 시점에 이미 처리됐다.

#### `ReferralRegisteredEventListener`의 처리 순서를 바꾸지 않는다

**대상**: `.../member/listener/ReferralRegisteredEventListener.java`

**적립 먼저, 보상 완료 전이는 그 다음이다.** 전이가 먼저 커밋되면 "완료로 표시됐지만 포인트는 없는" 추천 관계가 남아 적립 실패 건을 상태로 식별할 수 없게 된다. 지금 순서라면 적립 실패 시 추천 관계가 `PENDING`에 머물러 재처리 대상으로 남는다.

적립을 이 리스너에서 도메인 서비스로 되돌리지 않는다 — 적립 시맨틱(잔액 증가 + EARNED 이력 + 적립 이벤트)의 단일 원천은 `PointLedgerService`다.

#### `ProductMenuReviewEventListener`가 REVIEW 이벤트를 다시 구독하게 하지 않는다

**대상**: `.../product/listener/ProductMenuReviewEventListener.java`

`PRODUCT.rating`의 근거가 MENU_REVIEW로 완전히 옮겨갔다. **두 리스너가 같은 `ProductReviewStatsService`를 호출하면 재집계가 두 번 돌고 "어느 쪽이 진짜 근거인가"가 코드에서 사라진다.**

#### 인증 리스너에 발송을 추가하지 않는다

**대상**: `.../mail/listener/MailVerificationEventListener.java` · `.../sms/listener/SmsVerificationEventListener.java`

이 이벤트는 인증 **완료** 시점이고 발송은 **발급** 시점에 필요하므로 시점이 다르다. 발송은 `MailVerificationService#issue`·`SmsVerificationService#issue`가 발급과 원자적으로 수행한다.


### enum → `String` 강등 후 Object 타입 API는 컴파일러가 잡지 못한다 — 먼저 `valueOf`로 승격한다

**대상**: `backend/application/src/main/java/com/tastyhouse/application/**/service/*.java` 중 `*Result`의 강등된 문자열 필드를 도메인 enum과 비교·조회하는 곳 (발견 사례: `backend/web-application/src/main/java/com/tastyhouse/application/shop/service/ShopOrderMethodQueryService.java`(당시 `ShopQueryService`)의 주문 방식(order-methods) 조회 — `OrderMethod`)

덩어리 01로 `*Result`의 도메인 enum 필드가 `String`이 됐다. 필드 타입을 바꾸면 대부분의 사용처는 컴파일 에러로 드러나지만, **`Object`를 받는 API는 문자열을 그대로 받아들여 컴파일이 통과한다.** 검증 중 실제 사례: `Map<OrderMethod, ...>.get(dto.orderMethod())`가 `Map.get(Object)`라 컴파일은 됐지만 키 타입이 달라 **항상 `null`**을 돌려줬다.

| 같은 부류 | 증상 |
|---|---|
| `Map.get` / `Map.containsKey` | 항상 `null` / `false` |
| `Object#equals` (`enumValue.equals(result.status())`) | 항상 `false` |
| `Collection.contains` / `remove` | 항상 `false` |
| AssertJ `isEqualTo` (enum 기대값 vs 문자열 실제값) | 테스트가 실패하거나, 반대쪽으로 짜면 잘못된 단정이 통과 |

**규칙**: Result의 문자열을 도메인 enum과 비교·조회하려면 **먼저 승격한다**(`OrderMethod.valueOf(dto.orderMethod())`, 같은 방식의 `DayType.valueOf(...)`·`ClosedDayType.valueOf(...)`). 반대로 enum 쪽을 `.name()`으로 내려 문자열끼리 비교해도 되지만, 한 파일 안에서 두 방식을 섞지 않는다. 문자열 값은 DAO의 `x.status.stringValue()` 투영에서 오며 엔티티 enum이 전부 `EnumType.STRING`이라 `name()`과 같다(`valueOf` 승격이 안전한 근거).

**enum 필드를 또 강등할 때**: 그 필드를 읽는 모든 곳을 `grep`으로 찾아 `Map`·`equals`·`contains`·`isEqualTo` 사용처를 **눈으로** 확인한다 — 빌드 성공은 증거가 아니다.

### 03a로 domain에서 옮겨 온 봉인 항목 (도메인 서비스·포트·테스트)

아래 항목은 원래 `domain/AGENTS.md`에 있었다. 대상 코드(포트 주입 도메인 서비스·그 포트·그 단위 테스트)가 03a로 이 모듈로 옮겨오면서 **소유 모듈의 문서로 함께 옮겼다**(규칙: 설명은 그 코드를 소유한 모듈의 `AGENTS.md`에 산다). 본문은 그대로이고 앵커 경로와 경계 테스트 이름만 새 위치로 고쳤다 — 서비스 사이의 컨텍스트 경계는 이제 domain `ContextBoundaryTest`가 아니라 이 모듈의 `ServiceContextBoundaryTest`가 강제한다.

#### `ReplyPhraseTextValidator` — 포트 carve-out (금칙어 검증기를 직접 부르지 않는다)

**대상**: `backend/ceo-application/src/main/java/com/tastyhouse/application/ceo/port/out/ReplyPhraseTextValidator.java`(앱 마커 제거로 `backend/application/...`에서 이동 — 구현 `ReplyPhraseProhibitedWordValidatorAdapter`와 유일한 소비자가 ceo라 ceo 전용 SPI가 됐다)

실제 검수 규칙(금칙어 목록 대조)은 shop 컨텍스트의 `ProhibitedWordValidator`가 소유하고, 이 포트의 어댑터가 그것을 그대로 호출한다 — **규칙을 복제하지 않는다.**

**ceo 도메인이 `ProhibitedWordValidator`를 직접 부르지 않는 이유는 컨텍스트 경계다.** 컨텍스트 간 참조는 ID VO·도메인 이벤트·출력 포트로만 허용되고 타 컨텍스트의 `service` 직접 import는 금지되어 있다(`ServiceContextBoundaryTest`). **기존에 같은 검증기를 직접 import하는 도메인 서비스들이 있으나 그것은 규칙 도입 이전 코드로 봉인된 것이라 선례로 삼지 않는다.**

위반 시 `BusinessException(SHOP_TEXT_PROHIBITED_WORD)`(400)을 던진다.

#### `StorePriceVerificationPort` — CQRS write 포트 잔류 carve-out

**대상**: `backend/application/src/main/java/com/tastyhouse/application/product/port/out/StorePriceVerificationPort.java`

메뉴 가격 저장은 product 컨텍스트의 규칙이지만, "매장가·픽업가를 설정할 수 있는가"와 "배달가가 매장가를 넘어 인증을 내려야 하는가"는 **가게 단위 상태**다. 컨텍스트 경계 규칙(`ServiceContextBoundaryTest`)이 타 컨텍스트의 `model`·write 포트(`port.out.write`, 03a 이전 `repository`, 03b 동안 `store` — **persistence domain 재허용으로 다시 `port.out.write`**)·`service` 직접 참조를 금지하므로, product는 이 포트로만 그 상태를 다룬다 — **`ShopPersistencePort`를 직접 주입하면 신규 위반이 되고 봉인 목록은 늘릴 수 없다.**

구현은 `StorePriceVerificationAdapter`가 `ShopPersistencePort`에 위임한다. **(03b — 위치 이동)** 이 어댑터는 `infrastructure:persistence`의 `@Component`였으나 지금은 **`backend/application/src/main/java/com/tastyhouse/application/shop/service/StorePriceVerificationAdapter.java`**(~~마커 없는 POJO)이고 `shop/config/ShopServiceConfig#storePriceVerificationAdapter`가 `@Bean`으로 등록한다~~ **(번복됨 — application `*ServiceConfig` 삭제)** 지금은 클래스에 `@SharedApp` 마커만 단 도메인 서비스로 스캔 등록된다). 도메인 모델 `Shop`을 로드해 `verifyStorePrice()`/`clearStorePriceVerification()`을 호출하고 `ResourceNotFoundException(SHOP_NOT_FOUND)`를 던지므로 domain을 모르는 persistence에 둘 수 없었다. **shop에 둔 이유는 03a의 `ShopRequestIndexSyncAdapter`와 같다** — product 쪽에 두면 어댑터가 shop의 `store/ShopPersistencePort`를 참조해 `ServiceContextBoundaryTest` 위반이 되고, shop에 두면 "shop이 product의 포트(`port.out`)를 구현"하는 허용된 방향만 남는다.

#### `StorePriceVerificationService` — 애그리거트를 product가 소유하는 배치 (위 포트와 짝)

**대상**: `backend/application/src/main/java/com/tastyhouse/application/product/service/StorePriceVerificationService.java`

**왜 shop이 아니라 product 컨텍스트가 소유하는가**: 승인이 하는 일의 본체는 `PRODUCT_PRICE`의 매장가·픽업가를 채우는 것이다. 요청 애그리거트를 shop에 두면 그 승인 경로가 `product.model`·product의 write 포트를 import해야 해 컨텍스트 경계 규칙(`ServiceContextBoundaryTest`)을 위반하는데, **그 봉인 목록은 늘릴 수 없다.** 그래서 인증 요청 애그리거트 자체를 product가 소유하고, 가게 단위 상태인 인증 ON/OFF 플래그만 `StorePriceVerificationPort`로 다룬다 — **이 방향이 경계 위반 없이 성립하는 유일한 배치다.**

함께 고정되는 제약.

- 테이블명이 `SHOP_STORE_PRICE_VERIFICATION`인 것은 요청이 **가게 단위**로 접수되기 때문이며, 소유 컨텍스트와는 별개다.
- **승인은 요청 시점의 매장가를 쓴다** — 항목(`StorePriceVerificationItem`)에 박제된 값이며, 승인 시점에 현재 가격을 다시 읽지 않는다. 그러지 않으면 검수자가 보지 않은 값이 승인된다.
- **인증을 켜기 전에 항목을 먼저 반영한다.** 순서를 뒤집으면 반영 도중 실패했을 때 인증만 켜진 채 매장가가 비어 있는 상태가 남는다.
- 상태 전이는 원본 전이와 **같은 트랜잭션**에서 인덱스에 동기 기록한다. **전이 메서드마다 이 호출을 넣는다** — 한 곳이라도 빠지면 점주 화면의 요청처리 현황이 원본과 영구히 어긋난다. **배선이 api 모듈이 아니라 이 도메인 서비스에 있는 것이 중요하다** — 승인·반려는 admin이, 취소는 ceo가 호출하므로 api 모듈에 두면 같은 전이가 두 모듈로 흩어져 한쪽이 반드시 빠진다.
- 인증 상태 → 통합 상태 매핑을 product가 소유하는 이유도 컨텍스트 경계다 — shop이 `StorePriceVerificationStatus`(product 소유)를 알면 `ServiceContextBoundaryTest`를 위반한다. 다섯 상태가 이름까지 대응하지만 `name()`을 그대로 흘려보내지 않고 **명시 매핑**을 두는 이유는, 어느 한쪽 enum에 상수가 추가되면 **컴파일 단계에서** 대응을 결정하도록 강제하기 위해서다.
- 할인 중인 메뉴는 인증 요청 대상이 아니다(승인 시 매장가가 할인가와 뒤엉킨다). 판정식은 `ProductPriceService`와 같다 — 이 저장소에 할인 스케줄링이 없어 "할인가 존재"로 본다.
- 가격 행이 정말 그 메뉴의 것인지 확인한다. **확인하지 않으면 남의 메뉴 가격 행에 매장가를 심을 수 있다.**

#### 도메인 서비스 단위 테스트 10종 — 불변식 봉인 (테스트 스텁 carve-out)

아래 테스트들은 **현재 동작을 봉인**하는 것이 목적이다. 리팩터링으로 테스트가 깨지면 테스트를 고치기 전에 **봉인된 불변식을 깼는지 먼저 확인한다.** 전부 write 포트·이벤트 발행 포트를 fake로 대체해 Spring/DB 없이 판정 로직만 검증하는 순수 단위 테스트다.

| 대상 (`backend/application/src/test/java/com/tastyhouse/application/...`) | 봉인하는 불변식 |
|---|---|
| `menureview/service/MenuReviewLifecycleServiceTest.java` → `register_succeedsWithoutStoreReview` | **매장 리뷰가 없어도 메뉴 평가가 등록된다**(설계 원칙 1의 회귀 방어). 이 서비스가 `ReviewPersistencePort`를 **아예 주입받지 않는 것 자체가** 그 원칙의 구조적 보증이며, 테스트는 그 상태를 봉인한다 |
| `product/service/OrderProductValidationServiceTest.java` | 필수 옵션그룹을 비운 주문, 숨긴·품절 옵션을 실은 주문 차단. 전부 "프론트만 막고 서버는 통과시키던" 결함이다. 3단계 보증금이 도입되면 후자는 "보증금 옵션을 숨겨 보증금 없이 주문"하는 경로가 되므로, 이 테스트가 그 우회를 **영구히 봉인한다** |
| `product/service/ProductRepresentativeApprovalServiceTest.java` | 세 제약(최대 6개 · 이미지 필수 · 최소 1개 유지). 특히 **최소 1개 유지가 기존 `PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE`를 재사용**하는 것이 핵심 — 새 코드로 갈라지면 같은 불변식에 프론트가 두 갈래를 분기해야 하고 일괄 숨김 경로와 하한이 어긋난다 |
| `review/service/ReviewBlindRequestServiceTest.java` | 스펙의 세 규칙 — **1회 제한**(단 `CANCELED`는 예외) · **고객 동의 삭제** · **타인 리뷰 접근 차단**. 추가로 `IndexSync` 중첩 클래스가 신규 전이 2종(`EXPIRED`/`DELETED`)이 종결(`APPROVED`)로 접히는지 봉인한다 — 목록에 "재노출"·"삭제"라는 없는 통합 상태가 새어 나가면 안 된다. 원본→통합 상태 매핑은 컨텍스트 경계 때문에 recorder가 아니라 이 서비스가 소유한다 |
| `review/service/ReviewOwnerReplyServiceTest.java` | **30일 작성 제한이 등록에만 걸리는지**를 봉인한다. 기한 판정 기준일을 파라미터로 받는 설계 덕에 시계 조작 없이 29·30·31일차를 지정할 수 있다 — **도메인이 `LocalDate.now()`를 직접 부르면 이 테스트 자체가 불가능하다** |
| `shop/service/ShopCeoAssignmentServiceTest.java` | `ShopCeoAssignmentService`의 상태 규칙 표 전체. 특히 **재배정이 `REVOKE`+`GRANT` 2행**인 것을 봉인한다 — 한 행에 before/after를 담는 형태로 되돌아가면 "언제부터 언제까지 권한이 있었는가"를 읽을 수 없게 된다 |
| `shop/service/ShopLifecycleServiceTest.java` | 가게 등록 시 접근권한 이력 기록. 등록에서 점주를 함께 배정하는 것도 접근권한 부여이므로 나중에 배정한 경우와 **구별 없이 `GRANT` 이력이 남아야** 하고, 반대로 점주 없이 등록하면 아무 행도 남지 않아야 한다 |
| `shop/service/ShopMenuCollectionImageServiceTest.java` | 규칙이 전부 **행 하나만 보고는 판정할 수 없는 집합 차원**이라 애그리거트 단위 테스트로는 한 줄도 검증되지 않는다. **정원(최대 6개)은 상태를 가리지 않는다**(대기·반려 건도 슬롯을 차지한다), **순서 변경은 replace-all**(부분·초과·미지의 id 목록은 전부 거절 — 부분 목록을 받아주면 낡은 화면의 요청이 빠진 이미지를 목록 끝으로 밀어낸다). 가게 소유가 아닌 id는 존재를 알리지 않고 `SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND`로 합쳐 **IDOR을 막는 경로도 함께 봉인**한다 |
| `shop/service/ShopRequestCancellationServiceTest.java`(지금 위치는 `backend/ceo-application/src/test/java/com/tastyhouse/application/shop/service/` — 대상 도메인 서비스 `ShopRequestCancellationService`가 ceo-application에 있다. 유스케이스 분리 전 이름은 `ShopRequestCancelServiceTest`·`ShopRequestCancelService`) | 세 규칙 — (1) `PENDING`만 취소된다, (2) `IN_PROGRESS`는 409로 거부된다(가맹본부에 자료가 전달된 뒤라 플랫폼이 일방 취소할 수 없다), (3) **취소는 원본 애그리거트의 상태를 바꾼다**. (3)이 핵심으로, 인덱스에만 `CANCELED`를 두면 원본이 `PENDING`으로 남아 중복 차단이 재요청을 계속 막고 관리자가 취소된 요청을 승인·반려할 수 있다 |
| `shop/service/ShopRequestIndexRecorderTest.java` | 원본 → 통합 상태 **매핑 표를 전수** 봉인한다. 특히 조정 신청의 `COMPLETED → APPROVED`는 유일하게 값 이름이 어긋나는 매핑이라, 고정하지 않으면 목록에 "완료"라는 없는 상태가 새어 나가거나 매핑이 조용히 뒤집힌다. 게시중단만은 **통합 상태를 그대로 받는다** — 컨텍스트 경계 때문에 recorder가 `review.model.ReviewBlindStatus`를 import할 수 없어 매핑을 `ReviewBlindRequestService`가 소유하기 때문이다 |

#### 리뷰 부가 리포지토리 Fake 2종 — 보관하지 않는 것이 의도다

**대상**: `backend/application/src/test/java/com/tastyhouse/application/review/service/FakeReviewTagPersistencePort.java`
→ 클래스 선언 / `saveAll(List<ReviewTag>)` · `deleteByReviewId(ReviewId)`
**대상**: `backend/application/src/test/java/com/tastyhouse/application/review/service/FakeReviewImagePersistencePort.java`
→ 클래스 선언 / `saveAll(List<ReviewImage>)` · `deleteByReviewId(ReviewId)`

`ReviewTagPersistencePort`·`ReviewImagePersistencePort`에는 **조회 메서드가 없다.** 저장한 태그·이미지를
되읽어 검증할 수단이 계약에 없으므로, 이 Fake들은 보관용 컬렉션을 두지 않고 호출을 삼키기만 한다 —
협력 객체를 채우는 용도의 스텁이다.

**빈 메서드 본문을 "미구현"으로 오인해 채우지 않는다.** 보관 컬렉션을 추가해도 그것을 읽어 단언할
포트 메서드가 없어 검증에 쓰이지 못하며, 조회 계약이 실제로 생기면 그때 함께 채운다.

#### 03a로 `infrastructure:persistence`에서 옮겨 온 봉인 항목

아래 두 항목의 대상 클래스(`ShopRequestIndexSyncAdapter`·`CachingProhibitedWordPersistencePort`)는 03a로 persistence에서 이 모듈의 `shop/service/`로 옮겨왔다. 본문은 `infrastructure/persistence/AGENTS.md`에 있던 그대로다.

##### `ShopRequestIndexSyncAdapter`의 enum 승격 실패를 삼키지 않는다

**대상**: `backend/application/src/main/java/com/tastyhouse/application/shop/service/ShopRequestIndexSyncAdapter.java`

포트 시그니처가 `String`인 것은 통합 상태 `ShopRequestStatus`가 shop 소유라 product 쪽 포트에 등장할 수 없기 때문이다. **승격 실패는 프로그래밍 오류(양쪽 enum이 어긋난 상태)이므로 `from(String)`의 400 변환에 맡기지 않고 그대로 전파시킨다** — 조용히 넘기면 인덱스가 원본과 어긋난 채 남는다.

이 기록은 **이벤트·`AFTER_COMMIT`이 아니라 원본 상태 전이와 같은 트랜잭션에서 동기 수행한다** — 기록 유실이 곧 "요청이 목록에서 사라짐"이기 때문이다. 리스너로 옮기지 않는다.


##### `CachingProhibitedWordPersistencePort`에 락을 추가하지 않는다

**대상**: `backend/application/src/main/java/com/tastyhouse/application/shop/service/CachingProhibitedWordPersistencePort.java`

`AtomicReference`에 스냅샷을 통째로 담아 교체하므로 락이 필요 없다. 만료 직후 동시 호출이 겹치면 적재가 중복될 수 있으나 결과가 같은 read-only 조회라 무해하며, **중복 적재를 막는 락이 주는 이득보다 락 경합 비용이 크다.** TTL을 제거해 무기한 캐싱으로 바꾸지도 않는다 — 시드 갱신이 재기동 전까지 반영되지 않는다.

### 03b로 생긴 봉인 항목 (Store·State·Code)

> **(번복됨 — persistence domain 재허용: 대상 파일만)** 아래 두 매퍼 항목의 대상이던 application `store/*StateMapper`·`*StateMapperTest`는 삭제됐고, 변환과 테스트가 persistence로 옮겨졌다. **규칙 자체는 그대로 유효하며 대상만 바뀌었다** — 아래 각 항목의 "대상"을 현행 경로로 고쳐 두었다. 이관 테스트의 봉인 규칙은 `../infrastructure/persistence/AGENTS.md`에도 같은 내용이 있다.

#### 매퍼 테스트의 필드 값을 같은 값으로 채우지 않는다 (구 `*StateMapperTest`)

**대상**: `backend/infrastructure/persistence/src/test/java/com/tastyhouse/infrastructure/persistence/<ctx>/persistence/*MapperTest.java` → 각 `reconstitute`·`XxxJpaEntity.create` 호출 인자 (03b 동안은 `backend/application/src/test/java/com/tastyhouse/application/<ctx>/store/*StateMapperTest.java` 81개)

**같은 타입의 연속 필드(`String`·`Long`·`boolean`·`LocalDateTime`)는 반드시 서로 다른 값으로 채운다.** `title`과 `content`에 같은 문자열을 넣으면 매퍼가 둘을 뒤바꿔도 단언이 통과한다. ~~`StateRecordArityTest`는 개수만 본다~~(삭제됨) — 같은 타입 컴포넌트의 순서 뒤바뀜을 잡는 것은 이 테스트뿐이다. 자식 컬렉션·Embeddable이 있으면 비우지 말고 채워서 검사한다.

#### 매퍼의 null 가드를 "NOT NULL 컬럼이라 불필요"하다며 지우지 않는다 (구 `XxxStateMapper`)

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/persistence/<ctx>/persistence/*Mapper.java` → `x == null ? null : XxxId.of(x)` 형태 전부 (03b 동안은 `backend/application/src/main/java/com/tastyhouse/application/<ctx>/store/*StateMapper.java` — 표현식은 한 글자도 바꾸지 않고 옮겼다)

삭제된 `IdMapping`이 강제하던 규칙을 삼항 가드가 승계했다. 컬럼이 NOT NULL이어도 도메인 모델이 미배정 상태를 `null` VO로 들 수 있고(`toState` 방향 NPE), nullable FK는 **그 행이 실제로 있을 때만** `XxxId.of(null)`로 터진다. 컬럼별로 가드 유무를 나누지 않는다.

#### DAO에 enum 상수명을 리터럴로도, 복제 상수로도 쓰지 않는다 — 비교값은 포트 인자로 받는다

**대상**: `backend/infrastructure/persistence/**/*.java` 전체 · `backend/application/src/main/java/com/tastyhouse/application/**/port/out/`

리터럴 `"COMPLETED"`는 도메인 enum 상수가 개명·삭제돼도 컴파일되고 조회가 **조용히 0건**이 된다. ~~그래서 `XxxCodes` 복제본을 거쳤다~~ **(번복됨)** 지금은 application이 도메인 enum의 `name()`을 포트 인자로 넘기므로, 상수가 개명·삭제되면 **application 호출부가 컴파일 에러**가 난다(위 "enum 비교값 전달 규칙"). persistence에 enum 상수 리터럴이나 복제 enum을 다시 들이지 않는다. **(persistence domain 재허용 후)** 쓰기 어댑터 `XxxPersistenceAdapter`은 도메인 enum을 참조할 수 있으므로 비교값을 `XxxStatus.X.name()`으로 만든다(리터럴 금지는 그대로). 조회 DAO는 여전히 포트 인자로 받는다. 옵션 가용성 결과의 `"NORMAL"`/`"COMMON"` 리터럴 4곳도 같은 이유로 `ProductOwnerQueryPort#findProductOptionAvailability(condition, normalOptionType, commonOptionType)` 인자로 바꿨다. 잔존 검사: `grep -rnE '"[A-Z][A-Z_]{2,}"' --include='*.java' infrastructure/persistence/src/main | grep -vE '@Table|@Column|@Index|name = "|columnDefinition|columnList'` → 0건.

#### 정산이 limit 밖 행의 `memberId`까지 검증하는 것을 "불필요한 변환"이라며 줄이지 않는다

**대상**: `backend/application/src/main/java/com/tastyhouse/application/rank/service/RankSettlementService.java` → `settle`의 `memberIds`

03b 이전에는 `MemberReviewCount.memberId`가 `MemberId`여서 persistence가 **조회된 모든 행**을 `MemberId.of`로 변환했고, 0 이하·null id가 한 행이라도 있으면 정산 전체가 실패했다. `Long`으로 강등된 뒤에도 그 동작을 지키려고 `settle`은 상위 `limit`개가 아니라 **전 행**을 `MemberId`로 변환해 두고, `buildRanks`는 그중 앞 `limit`개만 쓴다. 변환 대상을 `limit`개로 좁히면 잘못된 id가 조용히 통과한다.

### UseCase 구현 서비스·리스너·설정에 `public`을 붙이지 않는다 (package-private 적용, 5모듈 공통 정본)

**대상**: `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `useCaseImplementationsShouldNotBePublic` · `listenersAndConfigsShouldNotBePublic`

5모듈(`application`·`{web,admin,ceo,batch}-application`)에서 215개를 package-private으로 좁혔다 — `..service..`에서 `port.in` UseCase를 구현하는 서비스 202개(web 48 · admin 59 · ceo 88 · batch 7 `*SchedulerService`), 리스너(`<ctx>/listener`) 12개, `SharedBeanConfig` 1개. 컨트롤러·협력 서비스는 UseCase 인터페이스로만 주입받고, 빈은 앱의 `ApplicationLayerScanConfig` 문자열 스캔으로 등록되므로 구현 클래스 이름이 패키지 밖에 나타날 필요가 없다. `useCaseImplementationsShouldNotBePublic`은 대상이 200개 이상인지(하한) 함께 확인해, 술어가 어긋나 대상을 잃고 공허하게 통과하는 것을 막는다. 생성자·메서드의 `public`은 유지한다(`@Transactional`은 public 메서드에만 적용된다).

**public으로 남는 것**:

| 대상 | 이유 |
|---|---|
| `port.in`·`port.out`·`*Result`·`*Command` | 모듈 간 계약 |
| UseCase 없는 도메인 서비스 | 약 48개가 다른 패키지에서 import된다(리스너→서비스, api 모듈→`CeoUserDetails`·`ShopOwnershipValidator` 등). 같은 패키지에서만 쓰이는 나머지는 **아직 좁히지 않았다**(후속 과제 — 좁히기 전에 다른 패키지 참조가 없는지 확인한다) |
| `SpringDomainEventPublisher`·`ProhibitedWordValidator`·`CachingProhibitedWordPersistencePort` | 다른 패키지의 `SharedBeanConfig`가 참조한다 |

새 UseCase 구현 서비스·리스너·`@Configuration`은 `public` 없이 만든다. 근거와 전체 범주는 `backend/CLAUDE.md`의 "접근 제어자 규칙 (내부 구현은 package-private)" 절.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

챕터 04에서 이 모듈의 java 주석 11,312줄을 전부 제거하며, 코드만 읽어서는 도달할 수 없는
설계 근거를 여기로 옮겼다. 각 절은 **어느 코드 요소에 붙어 있던 서술인지**를 앵커로 밝힌다.

### 도메인 서비스 마커는 소비 앱 집합으로 정한다 — 공유 커널 단방향

> **(앱 마커 제거 후 갱신)** 마커는 사라졌지만 **"소비 앱 집합으로 정한다"는 판단은 모듈 선택 기준으로 그대로 이어진다** — 소비 앱이 하나면 그 `{앱}-application`, 둘 이상이면 코어 `application`. "공유 커널 단방향"도 구조로 바뀌었을 뿐 같다 — 앱 모듈은 코어를 의존하고(`api project(':application')`) 코어는 앱 모듈을 모르므로, 공유(코어) → 앱 의존은 **컴파일 에러**다. 고정점 계산(코어 빈이 의존하는 빈도 코어여야 함)도 같고, 놓치면 컴파일 에러 또는 `coreBeansShouldOnlyDependOnCoreVisibleTypes`가 잡는다. 아래 서술의 `@WebApp`/`@SharedApp`은 각각 `web-application`/코어로 읽는다(예: `PaymentCancellationService`는 `web-application`, `PaymentConfirmationService`·`ShopLifecycleService`는 코어). `AppIsolationTest`는 삭제됐다.

**대상**: `backend/application/src/main/java/com/tastyhouse/application/*/service/` 의 마커-only 도메인 서비스(예: `payment/service/PaymentCancellationService` → `@WebApp`, `payment/service/PaymentConfirmationService` → `@SharedApp`, `shop/service/ShopLifecycleService` → `@SharedApp`) · `backend/application/src/test/java/com/tastyhouse/application/architecture/AppIsolationTest.java` → `appsShouldNotDependOnEachOther`·`constructorDependenciesShouldBeVisibleToApp`

원문 취지(ServiceConfig 삭제 때 정한 근거):

- **왜 4앱 공통 등록을 버렸나.** `*ServiceConfig`는 03a에서 persistence `*DomainConfig`의 등록 범위(4앱 전부)를 지키려고 만든 것이었다. 범위를 지킨 대가로 한 앱 전용 빈 44개가 쓰지 않는 앱에도 떴고, 서비스 추가마다 config를 함께 고쳐야 했다. 앱 마커는 이미 "어느 앱에 뜨는가"를 표현하는 이 모듈의 유일한 수단이므로, 서비스 자신이 마커를 갖는 것이 정보가 한 곳에 사는 형태다.
- **왜 "현재 소비자" 기준인가 (번복 — 과거 "언젠가 다른 앱이 트리거해도 안전하도록 `@SharedApp`").** 과거 `PaymentServiceConfig`는 결제 승인·취소가 web에서만 일어나도 리스너 배치 원칙을 빌려 `@SharedApp`으로 두었다. 지금은 그 판단을 버렸다 — 미리 넓혀 두면 쓰지 않는 앱에 빈을 띄우는 비용만 있고, 좁혀 둔 마커가 틀리면 `constructorDependenciesShouldBeVisibleToApp`이 **빌드에서** 잡으므로 "나중에 필요할 때 `@SharedApp`으로 올린다"가 안전하다. 그래서 `PaymentCancellationService`(소비자 `PaymentRefundRequestService`(당시 `PaymentCommandService`)·`PaymentCancellationExecutor`, 모두 web)는 `@WebApp`, `PaymentConfirmationService`(`@SharedApp` 리스너 `PaymentEventListener`가 소비)는 `@SharedApp`이다. 리스너 자체가 4앱 공통인 이유(이벤트는 어느 앱이 발행하든 처리돼야 한다)는 리스너에만 적용된다.
- **왜 앱 → 공유만 허용하고 공유 → 앱은 막나.** `@SharedApp` 빈은 4앱 전부에 뜨는데, 그것이 앱 전용 빈을 주입하면 나머지 세 앱에서 그 빈을 찾지 못해 기동이 실패한다. 반대 방향(앱 → 공유)은 공유 빈이 어느 앱에나 있으므로 항상 안전하다 — 이것이 "공유 커널" 단방향이다. 허용 대상은 `..service..`의 공유 도메인 서비스로 한정한다. 앱 → `@SharedApp` 리스너·설정 클래스 직접 의존은 여전히 금지다(허용 범위를 공유 도메인 서비스로만 열었다).
- **고정점 계산이 필요한 이유.** 공유 빈이 의존하는 빈도 공유여야 하므로, 한 서비스를 `@SharedApp`으로 올리면 그 의존 서비스도 따라 올라간다(`ReviewBlindRequestService` → `ReviewLifecycleService`, `ShopLifecycleService` → `ShopImageApprovalService`·`ShopCeoAssignmentRecorder`). 새 서비스를 추가하거나 소비자가 바뀌면 이 전파를 다시 따라간다 — 누락하면 `constructorDependenciesShouldBeVisibleToApp`이 위반 경로를 이름으로 보여준다.
- **왜 domain 계산기 7개는 마커를 못 다나.** domain 모듈은 production 의존이 0개(spring-free)이고 마커 애노테이션은 application 소유라 domain이 볼 수 없다. 그래서 `SharedBeanConfig`의 `@Bean`이 남는다. `cupDepositPolicy`가 단일 빈이어야 하는 근거(점주 설정·손님 메뉴판·주문 금액 확정이 같은 인스턴스를 주입)는 등록 위치가 `SharedBeanConfig`로 바뀌어도 그대로다.

### 트랜잭션 경계를 파사드가 아니라 하위 서비스가 갖는 이유 — read-then-write 판정

**대상**: 조립 서비스(트랜잭션 없음) `backend/web-application/src/main/java/com/tastyhouse/application/member/service/MemberVerifiedPasswordUpdateService.java` ·
`.../member/service/MemberVerifiedPersonalInfoUpdateService.java` · `.../member/service/MemberWithdrawWithLogoutService.java` ·
`.../member/service/MemberPasswordVerifyService.java`, 내부 per-op 서비스(클래스 `@Transactional`)
`.../member/service/MemberPasswordUpdateService.java` · `.../member/service/MemberPersonalInfoUpdateService.java` ·
`.../member/service/MemberWithdrawService.java`, 그리고 `backend/web-application/src/main/java/com/tastyhouse/application/auth/service/AuthPasswordResetService.java`

화면 단위 흐름을 엮는 **조립 서비스는 `@Transactional`을 갖지 않는다.** 조립 서비스가 트랜잭션을 열면
DB 원자성이 필요 없는 단계(JWT 서명 검증·Redis 접근)까지 DB 커넥션을 네트워크 지연만큼
점유하게 되므로, 원자성이 실제로 필요한 구간만 하위 per-op 서비스가 단일 트랜잭션으로 갖는다.
(유스케이스 분리 전에는 이 조립을 파사드 `MemberService`(당시 `MemberScreenUseCase` 구현) 한 클래스가,
하위 쓰기를 `MemberCommandService`가 맡았다. 파사드는 해체됐고 판정 기준은 그대로다.)
**바깥 단계를 내부 서비스로 합치지 않는다** — 합치면 토큰 무효화가 DB 트랜잭션 안으로 들어가고,
토큰 없이 내부 `MemberPasswordUpdateUseCase`를 부르는 `AuthPasswordResetService` 경로가 깨진다.

판정 기준은 하나다 — **"이 단계가 DB에서 읽은 값에 근거해 DB를 쓰는가(read-then-write)?"**
그렇다면 검증과 쓰기가 같은 트랜잭션·같은 로드 안에 있어야 하고(그렇지 않으면 검증 후 쓰기
사이에 상태가 바뀌어 검사를 우회할 수 있다), 아니라면 묶지 않는다.

| 유스케이스 | 판정 | 근거 |
|---|---|---|
| 개인정보 변경 | 묶지 않는다 | 두 토큰 검증이 **JWT 서명·클레임 검증만** 수행하고 DB를 읽지 않는다(토큰이 발급 시점의 인증 사실을 서명으로 담고 있다). read-then-write 경합이 성립하지 않으며, 실제 DB write는 `MemberPersonalInfoUpdateService#updatePersonalInfo` 한 번뿐이라 이미 단일 트랜잭션이다 |
| 비밀번호 변경 | 묶었다(하강) | "새 비밀번호가 기존과 같은지" 검사가 **DB에서 읽은 현재 비밀번호**에 근거해 DB를 쓰는 read-then-write다. 과거에는 이 검사가 별도 readOnly 트랜잭션에 있어 검사와 변경이 두 트랜잭션·두 번의 회원 로드로 쪼개져 **검사 후 변경 사이에 비밀번호가 바뀌면 우회 가능**했다. `MemberPasswordUpdateService#updatePassword`(당시 `MemberCommandService#updatePassword`) 안으로 내려 단일 트랜잭션·단일 로드로 원자화했다 |
| 회원 탈퇴 | 묶지 않는다(묶으면 틀린다) | 탈퇴는 DB 변경이지만 토큰 무효화는 **Redis 블랙리스트 등록**이라 DB 트랜잭션과 무관하다. 오히려 **순서가 중요**하다 — 탈퇴(`MemberWithdrawService`)가 커밋된 뒤 무효화해야 하며(`MemberWithdrawWithLogoutService`가 이 순서를 지킨다), 한 트랜잭션에 넣으면 Redis 등록이 커밋 전에 일어나 **탈퇴가 롤백돼도 토큰만 죽는** 불일치가 남는다 |
| 인증코드 발송 | 묶었다 | "기존 미완료 인증 만료 + 새 인증 저장 + 발송"이 함께 성립해야 한다 |

**비밀번호 변경의 검사 순서를 뒤집지 않는다** — 동일 여부(`MEMBER_PASSWORD_SAME_AS_OLD`) →
확인값 불일치(`MEMBER_PASSWORD_CONFIRM_MISMATCH`) 순서를 유지해야 하며, 뒤집으면 두 조건을
동시에 위반한 요청의 **응답 코드가 바뀐다**.

### PG·외부 왕복은 트랜잭션 밖에 둔다 — 3단 구조와 보상 불가 지점

**대상**: `backend/web-application/src/main/java/com/tastyhouse/application/payment/service/PgPaymentConfirmService.java` · `.../payment/service/PaymentCancelService.java` · `.../payment/service/PaymentConfirmationExecutor.java` · `.../payment/service/PaymentCancellationExecutor.java`

**PG 왕복이 있는 두 유스케이스 서비스(`PgPaymentConfirmService`·`PaymentCancelService`)에 `@Transactional`이 없는 것은 의도다.** 같은 결제 컨텍스트의 나머지 네 서비스(`PaymentCreateService`·`PaymentConfirmService`·`PaymentOnSiteCompleteService`·`PaymentRefundRequestService`)는 PG 왕복이 없어 클래스 레벨 `@Transactional`을 단다. (유스케이스 분리 전에는 이 여섯 연산이 `PaymentCommandService` 한 클래스에 있었고, 그래서 클래스 레벨 대신 메서드 레벨로 트랜잭션을 나눠 달았다.) PG 승인·결제 취소는 PG사와의 HTTP 왕복을
포함하는데, 그 왕복이 DB 트랜잭션 안에 있으면 (1) 커넥션과 결제·주문 행 락을 네트워크 지연만큼
점유하고, (2) PG 처리가 성공한 뒤 커밋이 실패하면 **"PG는 승인/취소, DB는 미반영"이라는 보상 불가
불일치**가 남는다. **취소는 `pgCancelRequired && pgPaymentGateway.supports(pgProvider)`일 때만 PG를 호출한다** —
`PaymentCancelService#cancelPayment`가 판정하는 `pgCancelAttempted`이며, 라우터가 해당 벤더를
지원하지 않으면(`PgPaymentGatewayRouter#supports`) PG 왕복 없이 DB 취소만 반영한다.

```
① 사전 검증  : PaymentConfirmationExecutor#prepareInNewTx  (트랜잭션, readOnly)
② PG 호출    : PgPaymentGateway                            (트랜잭션 없음)  ← 서비스가 직접
③ 결과 반영  : PaymentConfirmationExecutor#applyInNewTx    (트랜잭션)
```

- **보상 장치**: ③이 실패하면 PG는 이미 처리됐으므로 자동 보상이 불가능하다. `PG_DB_MISMATCH`
  마커와 PG 거래 식별자를 담은 `log.error`를 **수동 개입·대조 배치의 진입점**으로 삼는다(운영에서
  이 마커로 알럿을 건다). 사용자에게는 실패를 그대로 전파해 "성공했지만 반영되지 않은" 상태를
  성공으로 오인하게 하지 않는다.
- **PG 호출 자체가 예외(타임아웃 등)면 상태를 바꾸지 않고 그대로 전파한다** — 승인 여부가 불확실한
  상태에서 `FAILED`로 단정하면 PG는 승인인데 DB는 실패인 **반대 방향 불일치**를 만든다.
- `failInNewTx`의 트랜잭션은 **커밋되어야 한다** — 실패 사실과 PG 응답 원본을 남기는 것이 목적이라
  예외 변환은 커밋 이후 호출자가 수행한다.
- PG 왕복을 포함하지 않는 명령(결제 개시·PG 콜백 반영·현장결제 완료·환불 요청)은 DB만 다루므로
  메서드 단위 `@Transactional` 하나로 충분하다.

### Executor를 별도 빈으로 분리하는 이유 — self-invocation은 프록시를 거치지 않는다

**대상**: `payment/service/PaymentConfirmationExecutor.java` ·
`reservation/service/ReservationBookingExecutor.java` ·
`reviewblind/service/ReviewBlindExpirationExecutor.java` ·
`productsoldout/service/ProductSoldOutReleaseExecutor.java` · `region/service/AdminDongSyncExecutor.java`

**같은 빈의 메서드를 호출하면 Spring 프록시를 거치지 않아(self-invocation) `@Transactional`이
적용되지 않는다.** 그래서 "재시도 루프·반복 처리는 트랜잭션 밖, 각 시도는 독립 트랜잭션"을
표현하려면 두 구간이 **서로 다른 빈**에 있어야 한다. 오케스트레이션하는 쪽은 트랜잭션을 가질 수
없고(외부 호출을 밖에 둬야 하므로), 도메인 서비스는 순수 POJO라 가질 수 없다 — 그 사이를 메우는
얇은 위임 빈이 Executor다.

- **낙관적 락 재시도**(`ReservationBookingExecutor`): 매 시도가 새 트랜잭션이어야 한다. 한 빈에
  두면 첫 시도에서 **rollback-only로 표시된 트랜잭션을 그대로 재사용**해 재시도가 무의미해진다.
- **건별 격리**(`ReviewBlindExpirationExecutor`·`ProductSoldOutReleaseExecutor`): 한 건이 실패해도
  앞서 성공한 건들이 함께 말려 들어가지 않아야 한다. 그래서 **스케줄러 서비스에는 `@Transactional`을
  붙이지 않는다** — 붙이면 전체가 한 트랜잭션이 되어 한 건의 실패가 전체를 되돌린다.
  실패 요약은 예외가 아니라 **로그**로 남긴다(예외를 던지면 스케줄러가 삼켜 성공 건수까지 잃는다).
- Executor는 `REQUIRES_NEW`가 아니라 기본 `REQUIRED`를 쓰되, **상위 트랜잭션이 없는 상태를 전제한
  설계**임을 밝히려 전파 속성을 명시적으로 남긴다.

### CQRS 교차 주입 금지가 실제로 강제하는 것

**대상**: `{web,admin,ceo}-application`의 `**/service/` 유스케이스 서비스 — `backend/application/src/testFixtures/java/com/tastyhouse/architecture/UseCaseServices.java` → `commands()` · `queries()`, 강제 규칙 `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `commandServicesShouldNotDependOnQueryPorts` · `queryServicesShouldNotDependOnWritePorts`
(유스케이스 분리 전 대상 표기는 `**/service/*CommandService.java` · `**/service/*QueryService.java` — 지금은 이름이 아니라 구현 포트로 명령/조회를 가른다)

명령 유스케이스 서비스(당시 `*CommandService`)는 infra query DAO도 `*QueryService`·`*QueryUseCase`도 주입하지 않고, 조회 유스케이스 서비스(당시 `*QueryService`)는
domain의 write 포트를 주입하지 않는다. 그 결과 아래가 **구조로 강제**된다.

- **모든 명령은 식별자만 반환하고, 응답 조립은 커밋 이후 컨트롤러가 QueryService로 재조회해 담당한다.**
- 명령 경로에서 다른 애그리거트를 참조해야 하면 표현용 투영이 아니라 **write 포트의 단건 로드**를
  쓴다 — 그 값이 화면 표시용이 아니라 **불변식 입력**이기 때문이다(예: 리뷰 등록 시 상품 → 가게
  역조회는 "리뷰가 어느 가게에 속하는가"를 확정한다).
- 조회 경로에서 소유권·인가 판정이 필요하면 write 포트 대신 **읽기 포트로 식별자만 조회해 대조**한다
  (상태를 바꾸지 않는 화면 접근 판정이라 표현 목적 조회다).
- 규칙을 우회하지 않으면서 인가 관심사를 다루려면 **write 포트를 감싼 협력 빈**에 가둔다
  (`ShopOwnershipValidator`·`OwnedShopIdProvider`·`StorePriceVerificationReader`). 규칙의 의도는
  "쓰기 경로가 표현용 조회를 끌어다 쓰는 것"을 막는 데 있고 소유권 판정은 그 범주가 아니다.

**빈 순환 참조 회피**: 한 화면이 다른 컨텍스트의 데이터를 곁들여 보여줄 때 그쪽 QueryService를
경유하지 않고 **QueryPort를 직접 주입**한다 — 서비스를 경유하면 상대 쪽이 이 서비스를 다시 주입해야
해 순환이 생긴다. 표현 목적 조회는 DAO 계층에서 교차하는 것이 옳다(`ProductReviewsByRatingQueryService`가 `ReviewQueryPort`를,
`ReviewProductQueryService`가 `ProductQueryPort`를 직접 주입하는 것이 실제 사례 — 유스케이스 분리 전에는 `ProductQueryService` ↔ `ReviewQueryService`).

### 도메인 계산 입력은 표현용 투영으로 대체하지 않는다

**대상**: `backend/web-application/src/main/java/com/tastyhouse/application/shop/service/ShopDeliveryTipViewQueryService.java` → `findVisibleShopAggregate`

표현용 단건 조회와 달리 **도메인 서비스에 넘길 도메인 모델이 필요한 조회는 write 포트를 쓴다.**
계산기가 도메인 모델을 받으므로 표현용 Result를 도메인으로 되돌리는 역변환을 두지 않기 위함이며,
이것이 `queryServicesShouldNotDependOnWritePorts` carve-out의 실질적 근거다(위 봉인 목록 참조).
화면 표기용 목록(지역 이름 조립 등)만 infra query DAO에서 받는다.

같은 이유로 **read model을 `reconstitute`로 도메인 모델까지 되짚어 올려** 도메인 정책의 술어를
재사용하는 경로가 있다(`ShopPriceBadgeQueryService`·`ProductDetailQueryService`·`ProductBatchQueryService`). 규칙을 복제하면 표시
가격과 결제 금액이 갈리거나, 요일 구분을 추가할 때 한쪽만 고쳐진다. `reconstitute`(검증 미수행)를
쓰는 것은 **기존 데이터가 현행 규격을 위반해도 조회는 되어야 하기 때문**이다.

### 시각·시계에 의존하는 계산은 application에 남는다

**대상**: `coupon/port/out/MyCouponListItemResult.java` · ceo-application `review/service/ShopReviewListQueryService.java`·`review/service/ShopReviewDetailQueryService.java`
→ `toReplyWindow` · `review/service/ReviewOwnerReplyCreateService.java` → `register`
(유스케이스 분리 전 이름은 `ShopReviewQueryService`·`ReviewOwnerReplyCommandService`. `toReplyWindow`는 목록·상세 두 서비스에 각각 복제됐다)

"오늘"을 읽어야 하는 판정은 표현 계약이 대신할 수 없다 — 표현 계약이 시계를 읽으면 **응답 조립이
시점에 따라 값이 달라지는 순수하지 않은 함수**가 된다. 마감일 상수는 도메인 모델이 소유하므로
api 모듈이 참조할 수 없다는 것(`apiModuleShouldBeDomainModelFree`)도 함께 작용한다.

반대 방향으로, **domain은 프레임워크-프리라 시계를 주입받을 수 없고** 도메인이 직접 `now()`를
부르면 단위 테스트에서 기한을 고정할 수 없다. 그래서 기준 시각은 **이 계층이 해석해 도메인 서비스에
넘긴다**.

### 도메인 enum에 대한 `switch`를 api 모듈로 내리지 않는다

**대상**: ceo-application `review/service/ShopReviewSortTypeQueryService.java` → `describeSortType` ·
`shop/service/ShopRequestDetailQueryService.java` → `toRequestStatus`
(유스케이스 분리 전 이름은 `ShopReviewQueryService`·`ShopRequestQueryService`)

도메인 enum에 대한 `switch`는 바이트코드에서 `ordinal()`·`values()` 호출이 되어 api 모듈에서는
`apiModuleShouldOnlyReadDomainEnums`(읽기 accessor 3종만 허용)에 걸린다. 그래서 분기·표시 문구
매핑은 이 계층에 남는다.

*(갱신 — 덩어리 01)* 그 규칙은 삭제됐다. 지금은 api 모듈 컴파일 클래스패스에 `domain`이 없어 도메인 enum
`switch`가 **컴파일되지 않으므로** 결론(분기·표시 문구 매핑은 이 계층)은 더 강하게 유지된다.

**`valueOf`가 아니라 `switch`를 쓰는 것도 의도다** — 어느 한쪽에 상수가 추가되면 컴파일이 깨져
매핑 누락이 드러난다. 값 이름이 그대로 대응하더라도 마찬가지다.

### 표시 문구를 서버가 완성하는 기준

**대상**: `backend/web-application/src/main/java/com/tastyhouse/application/shop/service/ShopDeliveryTipViewQueryService.java` → `toShopDeliveryTipBreakdownItems` ·
`.../shop/service/ShopScheduledOrderSlotQueryService.java` → `toScheduledOrderSlotItemResult`·`toDayLabel`

프론트가 분기·상수를 복제하지 않도록 서버가 문구를 완성한다. **문구 안의 숫자는 천 단위 콤마까지
서버가 넣는다** — 그 값은 응답의 금액 필드가 아니라 **이미 완성된 문장의 일부**라 프론트가 문자열을
뜯어 다시 포맷할 수 없기 때문이다(금액 필드 자체의 표기 포맷은 그대로 프론트 담당이다).

도메인 enum 승격이 필요한 표기(요일 표시명 등)도 여기서 끝낸다 — api 모듈이 호출할 수 없는
도메인 enum 메서드이기 때문이다.

### 컨텍스트 경계를 잇는 조립은 이 계층의 몫이다

**대상**: ceo-application `product/service/ProductSoldOutOwnerService.java`·`ProductOptionSoldOutService.java`(그 밖의 판매상태 변경 서비스 6개 포함) ·
`product/service/ProductVegetarianRequestService.java`·`ProductVegetarianClearService.java` ·
`menureview/service/MenuReviewCreateService.java` ·
`shop/service/ShopStorePriceVerificationRequestService.java`
(유스케이스 분리 전 이름은 `ProductAvailabilityCommandService`(연산 8개)·`ProductVegetarianCommandService`·`ShopStorePriceVerificationCommandService`)

한 유스케이스가 두 컨텍스트의 값을 함께 필요로 하면, 도메인 서비스가 상대 컨텍스트를 직접 참조하는
대신 **이 계층이 각각 주입해 연결한다** — 도메인에서 참조하면 `ContextBoundaryTest` 위반이 되고
**봉인 목록은 늘릴 수 없다.**

정책과 계산을 가르는 기준도 함께 기록한다 — "오픈 시각을 정할 수 없다"(계산기)와 "그러면 얼마로
할까"(정책)는 서로 다른 판단이므로, **순수 계산기가 정책을 삼키지 않도록** 폴백 정책은 이 계층에 둔다.

### 소유권 역조회를 생략하지 않는다 — 실제 IDOR 사고의 근거

**대상**: ceo-application `product/service/ProductImageDeleteService.java` → `deleteImage` ·
`product/service/ProductOptionGroupOwnershipValidator.java` ·
`shop/service/ShopDeliveryAreaDeleteService.java` → `removeDeliveryArea`
(유스케이스 분리 전 이름은 `ProductImageCommandService`·`ShopDeliveryAreaCommandService`)

경로에 소유자 식별자가 없더라도 **대상 행에서 소유자를 역조회할 수 있으면 반드시 검증한다.**
이 저장소는 배달가능지역 삭제에서 정확히 이 역조회를 빠뜨려 **아무 점주나 순번을 훑어 남의 가게
배달가능지역을 삭제**할 수 있는 IDOR을 낸 전례가 있다(피해 가게는 배달 범위를 잃거나, 등록 건수가
0이 되면 주문 접수의 지역 검사 자체가 비활성화됐다).

- **"없음"과 "남의 것"은 같은 404로 합친다** — 코드가 갈리면 존재 여부가 새어 식별자 열거에 쓰인다.
  403을 쓰면 그 리소스의 존재 자체가 드러난다.
- **연결이 0건이면 소유자를 판정할 수 없으므로 접근 불가로 다룬다** — `null`을 "허용"으로 읽으면
  곧 인가 우회다.
- **N:M 전환 이후 소유권 판정은 동등 비교가 아니라 포함 관계다** — 한 메뉴가 여러 가게에 걸리므로
  원본 가게만 인정하면 연결된 가게의 점주가 자기 메뉴판의 메뉴를 열지 못한다.

### 집합 규칙이 있는 교체는 전량 검증 후 업로드한다

**대상**: ceo-application `shop/service/ShopNoticeOwnerCreateService.java`·`ShopNoticeOwnerUpdateService.java` → `saveImages`
(유스케이스 분리 전에는 `ShopNoticeOwnerCommandService` 한 클래스의 private 헬퍼였다. 지금은 두 서비스가 각자 갖는다)

파일 단위로 검증·업로드를 교차하면 뒤쪽 파일이 규격 위반일 때 앞쪽은 **이미 외부 스토리지에 올라간**
상태가 된다. 트랜잭션 롤백은 `UPLOADED_FILE` 행만 되돌릴 뿐 **스토리지 바이트는 되돌리지 못해**
실패 시도마다 고아 파일이 누적된다. 그래서 전량 검증을 먼저 끝낸 뒤 업로드한다.

변경 전 요약은 `updateContent` **호출 전에** 확정해야 한다 — 같은 인스턴스를 제자리에서 갱신하므로
나중에 읽으면 이미 변경 후 값이다.

### multipart 문자열 파트의 파싱 위치

**대상**: ceo-application `shop/service/ShopStorePriceVerificationRequestService.java` → `toItemSpecs`(유스케이스 분리 전 이름은 `ShopStorePriceVerificationCommandService`)

컨트롤러·Request record는 domain-free라 `BusinessException`을 던질 수 없고, 서비스는 `..request..`를
알 수 없다(`commandServicesShouldNotDependOnRequestRecords`). 세 규칙을 모두 만족하는 유일한 형태는
**Command가 원문을 경계 타입 `String`으로 담아 넘기고 서비스가 파싱하는 것**이다. 파싱 실패와 빈
목록이 같은 `ErrorCode`로 나가던 계약도 이때 그대로 보존된다(둘 다 서비스가 던진다).

실행 순서에도 의도가 있다 — **파싱을 업로드보다 앞에 둬야** 목록이 깨진 요청 때문에 쓸모없는
파일이 업로드되지 않는다.

### 조회 기간 상한을 이 계층에서 강제하는 이유

**대상**: ceo-application `ceo/service/CeoLoginHistoryListQueryService.java`(90일) ·
`ceo/service/CeoShopAccessHistoryListQueryService.java`(5년) ·
`shop/service/ShopChangeHistoryListQueryService.java`(6개월)
(유스케이스 분리 전 이름은 `CeoLoginHistoryQueryService`·`CeoShopAccessHistoryQueryService`·`ShopChangeHistoryQueryService`)

- **domain이 아닌 이유**: 기간 제한은 도메인 불변식이 아니라 **조회 화면 정책**이다. 기간이 지난
  행도 삭제하지 않고 계속 보관하며(고객센터 요청 시 장기 조회가 원 요구사항), 기록·저장은 제한하지
  않는다.
- **Bean Validation만으로 불가능한 이유**: `@PastOrPresent`는 상한만 막고 **"오늘 기준 -N일"이라는
  상대 하한**을 어노테이션으로 표현할 수 없다.
- **DAO 단독이 아닌 이유**: DAO가 조용히 잘라내면 사용자에게 "왜 비었는지"가 보이지 않는다.
- **기본값으로 파생된 경우에도 동일하게 검증한다** — 한쪽만 범위 밖으로 지정하면 나머지가 파생되어
  함께 밖으로 나가므로 그 조합도 거부되어야 한다.

**요청처리 현황에는 상한을 두지 않는다** — 변경이력의 6개월 제한을 대칭성을 이유로 복제하지 않는다.
"내가 낸 요청의 결과"는 반려 사유 확인·재요청 시 과거 제출물 참조를 위해 오래된 건도 열람돼야 한다.

### 접속기록은 인증 실패 경로에서도 남아야 한다

**대상**: ceo-application `auth/service/CeoLoginService.java` · `ceo/service/CeoLoginSuccessRecordService.java` · `ceo/service/CeoLoginFailureRecordService.java`
(유스케이스 분리 전 이름은 `CeoAuthCommandService`·`CeoLoginHistoryCommandService`. 기록 서비스가 성공·실패 두 개로 나뉘었고, 둘 다 같은 패키지의 `CeoLoginHistoryRecorder`로 저장한다)

**호출부 `CeoLoginService`(당시 `CeoAuthCommandService`)에 `@Transactional`을 붙이지 않는다.** 로그인 실패는 Spring Security 예외로 전파되는데,
트랜잭션이 걸려 있으면 **실패 이력이 예외와 함께 롤백되어 영구히 남지 않는다.** 호출부가 비트랜잭션이므로
기록 서비스(`CeoLoginSuccessRecordService`·`CeoLoginFailureRecordService` — 클래스 레벨 `@Transactional`)의 매 호출이 프록시를 거쳐 **독립 트랜잭션으로 즉시 커밋**되고, 따라서 `REQUIRES_NEW`가
필요 없다.

**기록 실패 시 정책은 성공·실패 경로가 의도적으로 비대칭이다**(봉인 목록의 carve-out과 짝).

- 성공 경로는 기록 실패를 **그대로 전파한다** — 접속기록 없이 토큰이 발급되는 상태를 만들지 않으며,
  개인정보처리시스템 접속기록은 법적 요구사항이라 남기지 못했다면 접속도 허용하지 않는 편이 옳다.
- 실패 경로는 기록 실패를 catch·로깅하고 **원래 인증 예외를 rethrow한다** — 감사 쓰기 실패가 인증
  실패 응답 계약(401)을 500으로 바꾸면 안 된다.

`refresh`(`CeoTokenRefreshService`)는 접속기록을 남기지 않는다 — 토큰 갱신은 새로운 개인정보 접속이 아니라 기존 세션의 연장이다.
존재하지 않는 아이디도 기록하지 않는다 — 임의 username을 쌓으면 **계정 존재 여부를 탐색하는 표면**이 된다.

### 인증 타입의 앱별 중복은 의도된 것이다

**대상**: `auth/service/{Member,Admin,Ceo}LoginService.java`(와 같은 앱의 로그아웃·토큰 갱신 서비스) · `auth/token/*TokenService.java`
(유스케이스 분리 전 이름은 `{Member,Admin,Ceo}AuthCommandService`)

인증 주체(`Member`·`Admin`·`Ceo`), 앱별 `ErrorCode`, `JWT_SECRET_*` 분리 때문에 **통합하면 앱별 인증
경계가 무너진다**(동일 시크릿이면 회원 토큰이 admin 인증을 통과하는 권한 상승). backend/CLAUDE.md의
앱별 중복 허용 목록에 있는 항목이며, "중복 제거" 대상으로 보지 않는다.

### 인바운드 포트의 도입 근거는 다형성이 아니다

**대상**: `**/port/in/*UseCase.java`

구현체가 하나뿐이고 소비자도 하나뿐이라 다형성·교체 가능성의 실익은 0에 가깝다. 도입 근거는
**컴파일 게이트**(컨트롤러가 구체 서비스에 손대는 코드가 애초에 컴파일되지 않는다)와 **경계 계약의
문서화**(그 애그리거트의 연산 계약을 한 파일이 고정한다)다.

- **인자가 하나뿐인 연산은 Command로 묶지 않는다** — 이름 있는 record로 얻는 이득(같은 타입 인자
  순서 착각 방지)이 인자 1개에는 존재하지 않는다.
- **배치 잡 UseCase에는 Command record가 없다** — 스케줄이 유일한 입력이라 경계에서 받을 값이 없고,
  파라미터 없는 연산에 빈 Command를 만드는 것은 형식만 맞추는 껍데기다.
- `@Scheduled` 트리거가 이 인터페이스만 주입하고 구현을 알지 않아야 **잡 본문 교체·테스트 대역
  주입**이 가능하다.

### 앱 네임스페이스 Result와 공용 읽기 계약을 가르는 기준

**대상**: `point/port/out/PointHistoryItemViewResult.java` · `coupon/port/out/MyCouponListItemResult.java` ·
`payment/port/out/PaymentViewResult.java` · `order/port/out/OrderDetailViewResult.java` ·
`grade/port/out/GradeInfoResult.java`

공용 읽기 계약 패키지는 **포트 하나의 산출물**을 담는 자리다. 아래에 해당하면 그 자리에 형제로 둘 수
없어 앱 네임스페이스에 별도 Result를 둔다.

- **DB 값이 아니라 계산 결과인 필드**가 있다(포인트 사용 내역의 부호 반전 — 저장된 양수를 음수로
  뒤집는 이 규칙은 표현 규칙이 아니라 회원 화면의 도메인 규칙이라 컨트롤러가 흉내낼 수 없다).
- **조회 시각 기준 파생값**이 있다(남은 일수·만료 여부).
- **VO 언랩·enum 강등**이 필요하다(금액이 `Money` VO라 `.value()`를 꺼내야 하는데 그것은 도메인
  타입을 아는 일이다).
- **다른 컨텍스트에 물어본 값이 합쳐진다**(주문상품의 리뷰 작성 여부).
- **포트 자체가 없다**(등급 정책은 도메인 enum 상수에서 파생되는 정적 목록이라 DB를 읽지 않는다).

**이 Result들은 어떤 금액도 계산하지 않는다** — 합계·할인 분해·최종금액 산출은 도메인과 DAO 투영이
이미 끝냈고, 이 계약은 거처만 옮긴다.

### 필드 셋이 다른 Result를 통합하지 않는다

**대상**: `coupon/port/out/MemberCouponItemResult.java` · `order/port/out/OrderListItemResult.java` ·
`product/port/out/ProductOptionGroupManagementResult.java` ·
`review/port/out/ShopReviewManagementDetailResult.java`

이름이 비슷하다고 상위집합 필드를 갖는 하나로 합치지 않는다 — 관리 화면에만 필요한 필드를 손님
응답 경로로 흘리면 **과잉 노출**이 되고, 어느 필드가 어느 화면 계약인지 추적할 수 없게 된다.
Result가 소비자별로 분리돼 있어 **실수로 새기 어렵다**는 것 자체가 이 배치의 이득이다(배달 평가가
ceo 전용 Result에만 있는 것이 그 사례).

### 관리 전용 조회가 별도 경로를 갖는 이유

**대상**: `review/port/out/ReviewBlindNoticeResult.java` ·
`review/service/ReviewBlindConsentQueryService.java`

일반 리뷰 상세 조회는 `hidden.isFalse()` 필터에 걸려 게시중단 리뷰에 404를 낸다. 그 필터는
**"게시중단은 정책 위반 제재"라는 판단이라 완화할 수 없으므로**, 작성자 본인에게만 열리는 전용
경로를 따로 둔다. 인가가 핵심이라 투영 결과의 작성자와 인증 주체 일치를 **재검증**하고, 불일치·부재를
모두 404로 응답한다.

### 스냅샷은 재조회하지 않는다

**대상**: `order/port/out/OrderProductResult.java`

주문 시점 가격·가격명은 스냅샷이므로 상품에서 재조회하지 않는다 — 가격명을 바꿔도 **과거 주문
전표가 변하지 않아야** 하기 때문이다.

### 인덱스는 파생 읽기모델이고 진실원은 원본이다

**대상**: ceo-application `shop/service/ShopRequestDetailQueryService.java` → `toStorePriceVerificationDetailResult`(유스케이스 분리 전 이름은 `ShopRequestQueryService`)

상세는 원칙적으로 원본 애그리거트를 다시 읽는다. 다만 인증 요청 유형은 원본이 **product 컨텍스트
소유**(승인의 본체가 `PRODUCT_PRICE` 갱신)여서 상세를 투영하는 shop 조회 DAO가 없다. 인덱스 상태는
접수·전이 시점마다 `ShopRequestIndexRecorder`가 동기화하므로 목록과 같은 값이고, 화면이 이 유형에서
필요한 것은 진행 상태와 반려 사유뿐이라 인덱스 값을 그대로 쓴다.

### 빈 상태·판정 불가를 예외로 만들지 않는다

**대상**: `shop/service/ShopPriceBadgeQueryService.java` → `getPriceBadges` ·
`shop/service/ShopNoticeQueryService.java` → `getShopNotice` ·
`product/service/ProductDetailQueryService.java` → `findProductById` ·
ceo-application `region/service/AdminDongBoundaryQueryService.java` → `getAdminDongBoundaries`(유스케이스 분리 전 이름은 `AdminDongQueryService`)

부가 표시(뱃지)는 판정 불가가 **가게 화면 전체를 깨서는 안 되므로** 예외 대신 `false`를 준다.
공지가 없는 것은 에러가 아니라 `null`이다 — 대부분의 가게에 공지가 없으므로 404를 쓰면 프론트가
정상 상태를 에러로 처리하게 된다. 가격 행이 없는 이관 이전 메뉴도 예외 대신 빈 목록을 준다(상세가
500으로 막히면 그 메뉴는 아예 팔 수 없다). 지도 축소도 정상 조작이라 **400이 아니라 빈 배열 +
`truncated: true`**로 응답한다.

### N+1을 부르는 반복 조회는 호출부가 한 번에 읽는다

**대상**: `review/service/ReviewWrittenProductIdsQueryService.java` → `findReviewedProductIds` ·
`product/service/ProductBatchQueryService.java` → `findProductsBatch`

주문 상세처럼 항목이 여러 건인 화면이 항목마다 단건 조회를 부르면 항목 수만큼 쿼리가 나간다.
호출부가 **루프 전에 1회 조회**한 뒤 메모리에서 판정하거나, 가격 행을 한 번에 읽어 그룹핑한다.

### 서버가 판정해 가리는 필드는 표현 계층에 맡길 수 없다

**대상**: `review/port/out/ReviewDetailView.java` · `review/service/ReviewDetailQueryService.java`
→ `toReviewDetailView`

배달 평가 3필드는 **뷰어가 작성자 본인일 때만** 채워진다(규격상 다른 고객에게 노출 금지, 본인은
수정 폼 초깃값으로 필요). 판정이 컨트롤러로 새면 **다른 호출부가 그 가림을 빠뜨릴 수 있으므로**
이 계약에 담긴 시점에 세 필드는 이미 "보여도 되는 값"이고 `null`이면 가려진 것이다.

**수정 폼은 받은 값을 그대로 되돌려 보내야 한다** — 수정 API는 PUT(전체 교체) 의미라 받은 값을 조건
없이 덮어쓴다. "값이 없으면 유지"를 서버에 넣지 않은 것은 그 순간 `null`이 "안 보냄"과 "지워줘" 두
뜻을 갖게 되어 **배달 평가를 지울 방법이 사라지기** 때문이다.

### 가시성 가드의 위치가 조회와 등록에서 다른 것은 의도다

**대상**: `review/service/ReviewDetailReader.java` → `requireVisibleReview` ·
`review/service/ReviewVisibilityQueryService.java` → `requireVisibleReview`

조회(GET)는 조회 서비스가 `ReviewDetailReader`로 직접 가드를 걸지만(예: `ReviewCommentListQueryService`), 등록(POST)은 컨트롤러가 가드를 호출한 뒤 command
서비스를 부른다 — command 서비스가 query 서비스를 주입받는 것이 **CQRS 교차 주입 금지 위반**이기
때문이다. **한쪽으로 통일하려다 중복 쿼리를 만들지 않는다.**

### 하위 호환을 위한 정규화

**대상**: `review/service/ReviewCreateService.java` → `createReview`·`validateDeliveryRating` ·
`review/service/ReviewUpdateService.java` → `validateDeliveryRating`

기존 클라이언트가 보내지 않는 필드는 `null`로 오므로 박싱 타입으로 받아 정규화한다(미전송 시 공개).
배달 평가도 **둘 다 null이면 검증 자체를 건너뛴다**. 새 필드를 추가할 때 이 형태를 따른다.

### 앱 마커가 곧 스캔 포함 기준이다

> **(번복됨 — 앱 마커 제거)** 마커 5종과 마커 include 필터는 삭제됐다. 지금 스캔 포함 기준은 **"앱의 클래스패스에 있는가"**(코어 + 자기 앱 모듈)와 일반 스테레오타입(`@Service`/`@Component`)이다. "application은 자기 등록하지 않는다(auto-config 없음)"는 그대로 유효하다 — 다만 이유가 "4개 앱의 빈이 같은 jar에 있고 마커로만 갈린다"에서 "스캔 범위는 앱 부트스트랩이 정한다"로 바뀌었다. 가드는 위 "앱 모듈 경계 가드" 항목과 각 앱 `ApplicationModuleClasspathTest`.

**대상**: `shared/marker/{WebApp,AdminApp,CeoApp,BatchApp,SharedApp}.java` · 각 앱 부트스트랩의 중첩 `ApplicationLayerScanConfig`(과거 `{App}ApplicationConfig.java`)

`useDefaultFilters = false` 스캔의 **유일한 포함 기준**이자 ArchUnit 앱 격리 규칙의 술어다. 새 빈과
새 UseCase 인터페이스는 **반드시 마커 하나를 단다** — 마커가 없으면 어느 앱에도 뜨지 않고, **컴파일은
통과하므로 실패는 기동 시점 `NoSuchBeanDefinitionException`으로만 드러난다.**

- Command record에는 붙이지 않는다(소속은 `AppOwnership`이 유도한다).
- ~~`@SharedApp`은 **리스너 전용**이다~~ **(번복됨 — 덩어리 01)** `@SharedApp`("앱 소속 없음 = 4앱 전부")은 **리스너와 공유 `@Configuration`**에만 붙는다 — 그 밖(`..listener..`/`..config..` 밖, 또는 일반 `@Service`)에 붙이면 앱 격리를 우회하므로 `LayerRulesTest#sharedAppOnlyOnListeners`가 막고, 반대로 리스너가 이 마커를 빠뜨리면 `listenersShouldBeShared`가 잡는다. 공유 설정이 등록하는 빈은 **마커 없는 POJO**여야 하며 `sharedConfigsShouldOnlyDeclareUnmarkedBeans`가 강제한다(클래스에 마커를 달면 앱 격리에 걸리고, 스캔과 `@Bean`이 겹치면 기동이 실패한다). **(번복됨 — application `*ServiceConfig` 삭제)** 허용 대상에 **`..service..`의 마커-only 도메인 서비스**가 추가됐다(`port.in` UseCase 구현체·`*CommandService`/`*QueryService` 제외). 공유 설정은 `SharedBeanConfig` 하나뿐이고 "마커 없는 POJO만 `@Bean`" 규칙은 그 설정에 그대로 적용된다 — 마커를 단 서비스를 거기 `@Bean`으로 추가하면 스캔 빈과 이름이 겹쳐 기동이 실패한다.
- **마커만 = 도메인 서비스, `@Service` + 마커 = 앱 오케스트레이터.** 새 도메인 서비스는 `@Bean`이 아니라 클래스에 소비 앱 마커만 단다(`markerOnlyClassesShouldBeDomainServices`가 위치를 강제한다).
- `@Component` 메타를 얹지 않은 **순수 마커**로 유지한다 — 얹으면 기존 `@Service`의 의미가 흐려진다.
- ~~라이브러리 모듈은 auto-configuration으로 자기 등록하지만~~ **(번복됨 — imports 제거: 이제 라이브러리 모듈도 자기 등록하지 않는다 — 각 앱 부트스트랩의 중첩 `ModuleScanConfig`가 문자열 스캔으로 조립한다)** **application은 자기 등록하지 않는다** —
  application 계층은 **앱 정체성 그 자체**라 클래스패스 존재만으로 어느 앱인지 결정할 수 없다
  (4개 앱의 빈이 같은 jar에 있고 마커로만 갈린다). 그 선택은 각 앱 부트스트랩의 중첩 `ApplicationLayerScanConfig`가 한다.
  **(번복됨 — application `*ApplicationConfig` 삭제)** 과거에는 "이 설정만은 앱이 `@Import` 한다"였고 그 설정이 이 모듈의 `{App}ApplicationConfig`였다.

### `applicationShouldNotDeclareComponentScan` — application에 `@ComponentScan`을 되살리지 않는다

**대상**: `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`
→ `applicationShouldNotDeclareComponentScan` (`noClasses().should().beMetaAnnotatedWith(ComponentScan.class).orShould().beAnnotatedWith(ComponentScans.class)`)

application 모듈의 어떤 클래스도 `@ComponentScan`(직접·메타)이나 `@ComponentScans`를 선언할 수 없다. "어느 앱이 어떤 마커를 싣는가"는 앱 조립 지식이라 컴포지션 루트(각 앱 부트스트랩의 중첩 `ApplicationLayerScanConfig`)의 것이고, 이 모듈에 스캔 선언이 돌아오면 과거의 `{App}ApplicationConfig` 구조가 되살아난다. 앱 쪽 오기입은 각 앱 모듈의 `ApplicationLayerScanConfigTest`가 막는다(`backend/{web-api,admin-api,ceo-api,batch-module}/AGENTS.md`의 봉인·가드 목록). `allowEmptyShould`는 쓰지 않는다 — 대상이 모듈 전체라 비지 않는다.

### 앱마다 얇은 래퍼를 두는 이유 — 트랜잭션 경계는 앱의 관심사다

**대상**: web-application `file/service/MemberFileUploadService.java` · admin-application `file/service/FileManagementUploadService.java` · ceo-application `file/service/FileOwnerUploadService.java`
(유스케이스 분리 전 이름은 `FileUpload*CommandService` — 예: ceo `FileUploadOwnerCommandService`·포트 `FileUploadOwnerCommandUseCase` → 지금 `FileOwnerUploadService`·`FileOwnerUploadUseCase`)

업로드 규칙 본체(허용 확장자·용량 한도·저장 경로·이벤트 발행)는 도메인 서비스가 단독으로 갖고,
이 클래스들은 `MultipartFile` 어댑팅과 `@Transactional` 경계 선언 둘만 한다. **세 벌은 로직 중복이
아니라 경계 선언 3개다** — 과거 api-common의 `FileService` 한 벌이 겸했는데, 표현 모듈이
`@Transactional` 유스케이스를 갖는 데다 application이 그것을 주입받아 **application → 표현 역방향
의존**이 생겼다.

### 조회 전용 컨텍스트에는 CommandService를 두지 않는다

**대상**: ceo-application `region/service/AdminDongListQueryService.java`·`AdminDongTreeQueryService.java`·`AdminDongBoundaryQueryService.java`(유스케이스 분리 전 `AdminDongQueryService` 한 클래스) · admin-application `ceo/service/CeoManagementQueryService.java`

`ADMIN_DONG`은 시드 SQL로만 관리하는 read-only 마스터다. 점주 계정의 생성·수정은 ceo-api가 담당하므로
관리 조회 쪽에는 명령 유스케이스 서비스(쌍 시절의 CommandService)를 두지 않는다. **빈 명령 서비스·포트를 형식으로 만들지 않는다** — 유스케이스 분리 후에는 연산이 없으면 포트도 서비스도 없다.

### `QueryUseCase`에는 컨트롤러 표면만 올린다 — 협력용 public 메서드를 전사하지 않는다

> **(번복됨 — 서비스 간 구체 주입 제거)** 이 절의 결론 "협력 서비스는 인터페이스가 아니라 구체 클래스를 주입해 쓴다"는 **폐기됐다.** 지금은 UseCase를 구현한 클래스를 누구도 구체 타입으로 주입하지 않는다. 아래 본문은 번복 전의 기록이다(제목은 앵커 보존을 위해 유지). 본문의 `ReviewQueryService`·`ProductQueryService`·`ShopQueryService`·`SearchQueryService`·`MemberQueryService`·`AdminQueryService`·`CeoOwnerQueryService`·`MemberCommandService`는 당시 이름이다 — 이후 유스케이스 분리로 per-op 서비스로 나뉘었다.
>
> | 항목 | before | after |
> |---|---|---|
> | 서비스 간 협력 주입 | 구체 `XxxService`를 주입한다(28건, 24개 파일) | `port.in` UseCase 인터페이스를 주입한다(0건) |
> | 반환 타입이 domain-free인 협력 메서드(`ReviewQueryService`의 `findShopReviewsByRating`·`findShopReviewStatistics`·`countVisibleReviewsByMemberId`·`findReviewedProductIds`·`findMyReviews`, `ProductQueryService`의 `searchByKeyword`·`findShopProducts`·`findPopularProducts`·`findShopProductCategories`) | 구체 클래스에만 있다 | 연산마다 per-op 포트에 하나씩 선언한다 — `ReviewShopByRatingQueryUseCase`·`ReviewShopStatisticsQueryUseCase`·`ReviewMemberCountQueryUseCase`·`ReviewWrittenProductIdsQueryUseCase`·`ReviewMyListQueryUseCase`, `ProductKeywordSearchQueryUseCase`·`ProductByShopQueryUseCase`·`ProductPopularQueryUseCase`·`ProductCategoryByShopQueryUseCase`(유스케이스 분리 전에는 `ReviewQueryUseCase`·`ProductQueryUseCase` 두 포트에 모아 선언했다). 반환 타입이 전부 `port.out` `*Result`라 `commandRecordsShouldBeBoundaryTyped`를 통과한다 |
> | 도메인 타입을 주고받는 단순 위임(`AdminQueryService`/`CeoOwnerQueryService#findByUsername`, `MemberCommandService#signUp`·`signUpSocial`·`saveSocialAccount`) | 구체 클래스에 두고 협력 서비스가 호출한다 | **삭제**했다. 호출부가 실제 협력자를 직접 주입한다 — `AdminPersistencePort`/`CeoPersistencePort`, 마커 없는 도메인 서비스 `MemberRegistrationService`, `PasswordEncoder`, `MemberSocialAccountPersistencePort`. UseCase에 올리지 않는 이유는 둘이다. 도메인 타입이라 `port.in` 규칙에 걸리고, 비밀번호 해시를 담은 `Admin`/`Ceo`를 컨트롤러가 닿는 인터페이스로 노출하게 되기 때문이다 |
> | 트랜잭션 경계 | 위임 대상 서비스의 클래스 `@Transactional`이 제공했다 | 위임을 걷어낸 호출부에 **같은 속성을 옮겨 붙였다**. `CredentialLoginService#signUp`은 `@Transactional`(회원 저장과 추천 등록의 원자성), `AdminTokenService#refresh`·`CeoTokenService#refresh`는 `@Transactional(readOnly = true)`다. refresh의 범위가 조회 한 번에서 메서드 전체로 넓어지지만, 나머지 작업이 Redis 호출이라 동작은 같다 |
>
> **새 원칙**
>
> - UseCase를 구현한 클래스는 구체 타입으로 주입하지 않는다.
> - 협력에 필요한 것이 domain-free 타입이면 UseCase에 선언한다. 도메인 타입이면 그 타입을 소유한 write 포트나 마커 없는 도메인 서비스를 직접 주입한다.
> - UseCase에 올린 협력 메서드는 컨트롤러 표면이 아니다. 예를 들어 `ReviewMyListQueryUseCase#findMyReviews`는 `MemberReviewService`가 쓰는 협력 메서드다. 컨트롤러가 이런 메서드를 새로 호출하려면 그 화면 계약이 맞는지 먼저 확인한다.
> - **같은 시그니처 주의**: `ReviewMemberListQueryUseCase#findMemberReviews`와 `ReviewMyListQueryUseCase#findMyReviews`는 둘 다 `(Long, int, int) → PageResult<MyReviewListItemResult>`다. 유스케이스 분리로 별도 포트가 됐으므로 타입으로는 구별되지만, 주입할 포트를 고를 때 아래 차이를 확인한다(분리 전에는 `ReviewQueryUseCase` 한 포트의 두 메서드였다).
>   - `findMemberReviews`: `visibleToCustomer()` 조건이라 고객에게 보이는 리뷰만 나온다. `ownerOnly`는 항상 `false`다.
>   - `findMyReviews`: `hidden = false` 조건만 걸려 점주에게만 공개한 리뷰도 포함한다. `ownerOnly`에 실제 값이 들어간다.
>   - 둘 다 구현은 `infrastructure/persistence/.../review/query/ReviewQueryAdapter`에 있고, 각각 `findReviewsByMemberId`·`findMyReviews`다.
> - **가드**: 위 [봉인·가드 목록](#봉인가드-목록)의 "UseCase 구현 구체 주입 금지"·"UseCase 필드명"·"CQRS 규칙의 `*QueryUseCase` 확장" 항목을 본다.
> - **정정**: 아래 표의 "`findByUsername` 호출부: 시더"는 번복 전에도 틀린 서술이었다. `AdminSeeder`·`CeoSeeder`는 `existsByUsername`만 호출한다.

**대상**: `application/src/main/java/com/tastyhouse/application/**/port/in/*QueryUseCase.java`
와 그 짝인 `**/service/*QueryService.java`

챕터 03 스펙의 문언은 "`QueryService`의 public 메서드 전사"였지만, **그대로 하면 빌드가 깨진다.**
`*QueryService`의 public 메서드에는 컨트롤러가 부르는 것과 **다른 서비스가 부르는 협력용**이 섞여
있는데, 후자는 도메인 모델·infra `*Result`를 그대로 주고받기 때문이다. 이것을 인터페이스로 올리면
`commandRecordsShouldBeBoundaryTyped`(`..port.in..`에서 `com.tastyhouse.domain..`·`infrastructure..`
의존 금지)에 걸린다.

실제로 걸리는 협력용 메서드는 **현재 9개**이며(챕터 03 시점에는 `MemberQueryService#getMember`를
포함해 10개였으나, 그 경로가 `MemberAuthService`의 `memberRepository` 직접 로드로 바뀌어 사라졌다),
**전부 컨트롤러가 아니라 다른 서비스가 호출한다.**

| 메서드 | 반환 | 실제 호출부 |
|---|---|---|
| `AdminQueryService#findByUsername` | `Optional<Admin>` | `TokenService`·`UserDetailsService`·시더 |
| `CeoOwnerQueryService#findByUsername` | `Optional<Ceo>` | 위와 동일 |
| `ProductQueryService`의 `findPopularProducts`·`findShopProductCategories`·`findShopProducts`·`searchByKeyword` | `*Result`·`PageResult` | `ShopQueryService`·`SearchQueryService` |
| `ReviewQueryService`의 `findMyReviews`·`findShopReviewStatistics`·`findShopReviewsByRating` | 위와 동일 | 상동 |

**스펙 §3의 목적이 "컨트롤러 주입 타입 교체"이므로, 인터페이스에는 컨트롤러 표면만 올리고 협력용
메서드는 구체 클래스에 그대로 둔다.** 이러면 조회 로직 diff 0을 유지하면서 두 ArchUnit 규칙을 모두
만족한다. 이것은 규칙을 무르게 하는 것이 아니라, 인바운드 포트가 애초에 겨냥한 표면(컨트롤러 경계)에
대상을 한정하는 것이다.

**판별법 — `port/in` 인터페이스 파일에 `com.tastyhouse.domain.` 또는 `com.tastyhouse.infrastructure.`
import가 생기면 잘못 올린 것이다.** 단 `domain.exception..`(에러 계약)과 `domain.shared.page..`
(페이징 계약) 두 carve-out은 정상이므로 그 둘을 뺀 나머지가 판정 대상이다(실측: `port/in` 전체의
domain import는 `domain.exception` 602건 · `domain.shared.page` 43건이고 **그 밖은 0건**이다).
**(갱신 — 덩어리 01)** 페이징 계약은 `com.tastyhouse.application.shared.port.out.page`로 이동해 domain import가
아니게 됐다. 지금 `port/in`의 domain import는 `domain.exception` 602건뿐이고, 페이징 43건은
`application.shared.port.out.page` import로 바뀌었다. 판정 대상에서 빼는 carve-out은 `domain.exception..` 하나다.

**새 조회를 추가할 때**: 컨트롤러가 부르지 않는 메서드라면 `*QueryUseCase`에 올리지 말고
`*QueryService`에만 둔다. 협력 서비스는 인터페이스가 아니라 구체 클래스를 주입해 쓴다.

### 리스너 배치가 `@SharedApp`으로 4앱 전부가 스캔하는 `application`인 이유 (개별 사유)

> **(앱 마커 제거 후 갱신)** 지금 리스너는 마커 없이 **코어 `application` 모듈**에 있고, 코어가 4앱 전부의 클래스패스에 있으므로 4앱 전부에 뜬다. 아래 표의 리스너별 근거("특정 앱에만 뜨게 두면 다른 앱이 같은 이벤트를 발행할 때 후속 처리가 누락된다")는 이제 "리스너를 앱 모듈에 두지 않는 이유"로 그대로 읽는다(`listenersAndConfigsShouldResideInCore`).

**대상**: `backend/application/src/main/java/com/tastyhouse/application/**/listener/*.java`

리스너 작성 규칙 일반은 이 문서의 [`<ctx>/listener/`](#ctxlistener--도메인-이벤트-리스너) 절에 있다. 여기에는 **"왜 특정 앱 마커(`@WebApp` 등) 하나가 아니라 `@SharedApp`으로 4앱 전부가 스캔하는 application에 두는가"의 리스너별 근거**만 적는다 — 공통 답은 "특정 앱에만 뜨게 두면 다른 앱이 같은 이벤트를 발행할 때 후속 처리가 조용히 누락된다"이고, 각 리스너의 발행 경로가 그 근거다.

과거에는 같은 근거("모든 실행 모듈이 스캔하는 곳")로 `infrastructure:persistence`에 두었다. application도 4앱 전부가 부트스트랩 중첩 `ApplicationLayerScanConfig`(과거 `{App}ApplicationConfig`)로 스캔하지만 `useDefaultFilters = false` + 앱 마커 필터라, 4앱 모두에 뜨게 하는 마커 `@SharedApp`을 신설해 그 근거를 application에서도 성립시켰다(번복됨).

| 리스너 | 발행 경로가 여럿인 근거 |
|---|---|
| `CouponEventListener` | 발급은 admin(수동)·이벤트 경유(가입·추천 보상), 사용은 web(주문 결제) |
| `MemberEventListener` | 가입·탈퇴가 web-api(본인)와 admin-api(관리자 강제 탈퇴) 양쪽 |
| `PointEventListener` | web(주문 결제)·admin(수동 조정)·이벤트 경유(결제 취소·추천 보상) |
| `PaymentEventListener` | 지금은 web-api뿐이지만 admin-api의 환불·관리 경로가 같은 이벤트를 발행하게 되어도 포인트 연동이 누락되면 안 된다 |
| `ReferralRegisteredEventListener` | 추천 등록이 일반 가입과 소셜 가입(4종) 어느 경로에서도 발생한다 |
| `ProductMenuReviewEventListener` | 평가는 web-api에서 등록되지만 admin-api의 숨김·삭제로도 통계가 바뀐다 |
| `ReviewBlindApprovedEventListener` · `ReviewOwnerReplyEventListener` | 지금은 admin/ceo 경로뿐이지만, **알림 적재는 행위 주체가 아니라 "그 일이 일어났다"는 사실에 반응해야 한다** |
| `PolicyActivatedEventListener` | 활성화 자체가 특정 액터에 묶이지 않는 도메인 불변식(`PolicyActivationService`)이다 |
| `MailVerificationEventListener` · `SmsVerificationEventListener` | 도메인별 분리 원칙 — 한 리스너가 여러 도메인 이벤트를 구독하면 한 도메인의 변경이 다른 도메인의 리스너 파일을 건드린다 |

개별 판단으로 따로 남길 것.

- **`ReferralRegisteredEventListener` — 순서가 중요하다. 적립 먼저, 보상 완료 전이는 그 다음이다.** 전이가 먼저 커밋되면 "완료로 표시됐지만 포인트는 없는" 추천 관계가 남아 적립 실패 건을 상태로 식별할 수 없게 된다. 지금 순서라면 적립 실패 시 추천 관계가 `PENDING`에 머물러 재처리 대상으로 남는다. 과거에는 `ReferralRegistrationService`가 point 애그리거트와 리포지토리를 직접 주입해 적립을 재구현했으나, 적립 시맨틱(잔액 증가 + EARNED 이력 + 적립 이벤트)의 단일 원천은 `PointLedgerService`여야 하므로 컨텍스트를 잇는 책임을 리스너로 옮겼다. `AFTER_COMMIT`이라 이 핸들러가 실패해도 추천 등록은 롤백되지 않는데, **추천 등록과 보상 적립의 결합을 끊기 위해 의도적으로 감수한 트레이드오프**다.
- **`PaymentEventListener` — 적립액 계산의 단일 원천은 `PaymentConfirmationService#calculateEarnedPoint`다**(주문에 기록되는 적립 포인트와 실제 적립액이 갈리지 않도록). 리스너는 이벤트 수신과 트랜잭션 경계만 담당한다.
- **`ReviewBlindApprovedEventListener` · `ReviewOwnerReplyEventListener` — `@Async` + `AFTER_COMMIT` + `REQUIRES_NEW` 3종 세트를 반드시 함께 단다.** `AFTER_COMMIT`만 달면 호출 스레드에서 동기 실행되어 알림 실패가 원본 API로 전파된다 — DB에는 반영됐는데 화면은 실패로 뜨는, "알림이 실패해도 원본은 유효하다"는 판단과 정면으로 어긋나는 상태가 된다. `REQUIRES_NEW`가 없으면 커밋될 트랜잭션이 없어 리스너가 조용히 아무것도 남기지 않는다. 동기가 아니라 리스너인 근거는 「크로스 컨텍스트 후처리」 판정표의 "후처리가 실패하면 원본도 없던 일이 되어야 하는가?"에 **아니오**이기 때문이며, 인증코드 발송이 동기인 것과 반대 방향이다.
- **`ReviewBlindApprovedEventListener`에 가게명 조회가 없는 것은 의도적이다** — 게시중단 안내 문구가 가게명을 노출하지 않는다. 고객에게 필요한 정보는 재노출 예정일이지 어느 가게가 요청했는지가 아니며, **요청 주체를 알리면 리뷰 작성자와 점주 사이의 분쟁을 부추길 수 있다.**
- **`ProductMenuReviewEventListener`의 구독 대상은 REVIEW가 아니라 MENU_REVIEW 이벤트다.** `PRODUCT.rating`의 근거가 MENU_REVIEW로 완전히 옮겨갔으므로 구독도 하나만 남는 것이 맞다 — 두 리스너가 같은 `ProductReviewStatsService`를 호출하면 재집계가 두 번 돌고 "어느 쪽이 진짜 근거인가"가 코드에서 사라진다. `ReviewCreatedEvent`/`ReviewDeletedEvent`는 발행만 남고 소비자가 0이다. **`productId == null` 가드는 유지한다** — MENU_REVIEW의 `product_id`는 NOT NULL이지만 이벤트 record가 VO를 담고 있어 향후 발행 경로가 늘 때 null이 실릴 수 있고, 그 예외는 `AFTER_COMMIT` 리스너에서 조용히 유실된다.
- **`MailVerificationEventListener` · `SmsVerificationEventListener`는 발송을 담당하지 않는다.** 이 이벤트는 인증 **완료** 시점이고 발송은 **발급** 시점에 필요하므로 시점이 다르다 — 발송은 `MailVerificationService#issue`·`SmsVerificationService#issue`가 발급과 원자적으로 수행한다.
- **`PolicyActivatedEventListener`는 재동의 요청·개정 고지 발송을 아직 담당하지 않는다** — 발송 대상이 전체 회원이라 요청 스레드에서 처리할 수 없고 배치·큐 설계가 선행돼야 한다. 그때까지는 전이 사실만 남겨, 어떤 정책이 언제 현행이 됐는지가 발행 지점 밖에서도 관측 가능하게 한다.

### `ErrorResponses` — 정적 유틸이고, cause를 따라가지 않으며, AOP로 하지 않는다

**대상**: `backend/application/src/main/java/com/tastyhouse/application/shared/error/ErrorResponses.java` → `resolve(Throwable)`
· `backend/application/src/main/java/com/tastyhouse/application/shared/error/ErrorDescriptor.java`
· 가드: `backend/application/src/test/java/com/tastyhouse/application/shared/error/ErrorResponsesTest.java`(~~`ErrorContractsConsistencyTest`~~ 를 대체)

표현 계층(web-api·api-common-module의 `GlobalExceptionHandler`, security-module의 필터 단계 핸들러)이 `com.tastyhouse.domain.exception`을 보지 않고도 에러 응답을 만들게 하는 번역점이다(덩어리 01). 규칙 본문은 `backend/CLAUDE.md` "예외·에러코드 소유 규칙".

- **최상위 예외만 본다 — `getCause()`를 따라가지 않는다.** `resolve`는 넘겨받은 예외 자체가 `BusinessException`(하위 타입 `ResourceNotFoundException` 등 포함)일 때만 `ErrorDescriptor(status, code, message)`를 돌려주고, 그 밖은 `Optional.empty()`다(`message`는 `getMessage()`). 삭제된 전용 `@ExceptionHandler(BusinessException.class)`도 최상위 타입으로만 매칭했으므로 이것이 기존 동작과 같은 의미다. cause를 따라가면 **오늘 500으로 응답되는, 다른 예외에 감싸인 `BusinessException`이 조용히 4xx로 바뀐다** — wire 계약 변경이다. "더 친절하게" 만들려고 cause 탐색을 넣지 않는다.
- **빈이 아니라 정적 유틸이다.** 빈으로 만들면 이 모듈 규칙상 앱 마커가 필요하고(`beansShouldHaveExactlyOneAppMarker`), 마커 스캔은 앱 부트스트랩의 중첩 `ApplicationLayerScanConfig`가 하므로 api-common의 `ApiCommonAutoConfigurationTest`(imports 제거 후 `ratelimit/ApiCommonRateLimitConfigTest`)처럼 앱 부트스트랩 없이 뜨는 컨텍스트에서는 빈을 찾지 못해 실패한다. 상태 없는 순수 번역이라 빈일 이유도 없다(`ProblemDetails`가 static인 것과 같은 판단).
- **AOP로 예외를 번역하지 않는다.** 서비스 경계에서 `BusinessException`을 다른 타입으로 감싸는 aspect를 두면, 예약 경로(web-application `reservation/service/ReservationCreateService` → `ReservationBookingExecutor`)가 **`OptimisticLockConflictException`을 잡아 재시도**하는 루프가 감싼 예외를 못 알아봐 재시도가 깨진다. 게다가 이 모듈에는 aspectjweaver가 없다. 번역은 응답 직전(핸들러)에서 한 번만 한다.
- ~~**`ErrorContracts`는 `ErrorCode` 3종의 미러다.**~~ **(번복됨 — 에러코드 모듈 분할)** `ErrorContracts`와 `ErrorContractsConsistencyTest`는 삭제됐다. 표현 계층이 직접 내는 코드(rate limit 429·권한 403·인증 401)는 `backend/api-common-module/src/main/java/com/tastyhouse/apicommon/exception/ApiErrorCode.java`의 `RATE_LIMIT_EXCEEDED`·`ACCESS_DENIED`·`AUTH_REQUIRED`가 갖는다(`security-module`이 `api-common-module`을 `implementation`으로 의존한다). `AUTH_REQUIRED`는 `WebErrorCode.AUTH_REQUIRED`의 미러라 같은 값이어야 하며, 전역 유일성 검사의 **봉인 예외 1건**이다(아래 "에러 카탈로그 가드").

### 에러 카탈로그 가드 — 코드는 던지는 가장 안쪽 모듈에 두고, 봉인 집합을 늘리지 않는다 (에러코드 모듈 분할)

**대상**:
- `backend/application/src/test/java/com/tastyhouse/application/shared/exception/ErrorCatalogConventionTest.java`
- `backend/application/src/test/java/com/tastyhouse/application/shared/exception/ErrorCatalogSnapshotTest.java` + `backend/application/src/test/resources/error-catalog-before.tsv`
- `backend/application/src/test/java/com/tastyhouse/application/shared/error/ErrorResponsesTest.java`

| 항목 | before | after |
|---|---|---|
| 카탈로그 | `domain.exception.ErrorCode` 단일 enum 442개 | 7개 — `DomainErrorCode`(153, domain)·`ApplicationErrorCode`(84, 코어)·`WebErrorCode`(86)·`AdminErrorCode`(28)·`CeoErrorCode`(65)·`BatchErrorCode`(1: `ADMIN_DONG_BOUNDARY_FETCH_FAILED`)·`ApiErrorCode`(`api-common-module`) |
| 배치 규칙 | 전부 domain | **그 코드를 던지는 가장 안쪽 모듈.** domain이 던지면 `DomainErrorCode`(application도 함께 던지는 22개 포함), 코어 또는 2개 이상 앱 모듈이 던지면 `ApplicationErrorCode`, 앱 하나만 던지면 그 앱의 `{X}ErrorCode` |
| 예외 | `BusinessException`(구체) · `ResourceNotFoundException`(domain) | `BusinessException`은 abstract. domain은 `DomainException(DomainErrorCode)`, application은 `ApplicationException(ApplicationErrorCodeSpec)` 또는 domain 코드면 `DomainException`. `ResourceNotFoundException`은 `application.shared.exception`으로 이동해 `ApplicationException`을 상속 |
| 가드 | `domain/.../ErrorCodeConventionTest`(`ErrorCode` 하나), `ErrorContractsConsistencyTest` | `ErrorCatalogConventionTest`(7개 카탈로그 전체), `ErrorCatalogSnapshotTest`, `ErrorResponsesTest` |
| Command 가드 | `BusinessException(ErrorCode.INVALID_INPUT)` + carve-out `domain.exception..` | `ApplicationException(ApplicationErrorCode.INVALID_INPUT)`, carve-out 제거 |
| 동작 | — | wire 계약(HTTP 상태·code 문자열·메시지) 불변. 호출부가 없던 23개(`ENTITY_NOT_FOUND`·`ADMIN_NOT_FOUND`·`TAG_NOT_FOUND` 등)만 삭제 |

**규칙**
- 새 코드는 던지는 가장 안쪽 모듈에 둔다. 앱 전용 코드를 **두 번째 앱이 쓰게 되면 코어 `ApplicationErrorCode`로 올린다**(split package라 import 경로는 같다). 앱 모듈의 `{X}ErrorCode`는 같은 앱 안에서만 던진다.
- 코드를 인자로 받는 메서드는 가장 좁은 enum 타입을 쓴다. 앱 코드가 섞이면 `ApplicationErrorCodeSpec`(5개 application 계층 enum이 구현, `ErrorCodeSpec`을 확장), domain 코드와 앱 코드가 섞이면 오버로드한다. **예외 1건**: 코드를 던지지 않고 **싣기만** 하는 값 record는 `ErrorCodeSpec`을 써도 된다 — `backend/ceo-application/src/main/java/com/tastyhouse/application/product/service/ProductAvailabilityFailure.java`뿐이다.
- `ErrorResponses.resolve`는 `instanceof BusinessException`으로 판정하고 cause를 따라가지 않는다(변경 없음). HTTP 상태는 각 enum에서 `int`로 둔다 — api 모듈이 코드 enum을 볼 수 없기 때문이다.

**`ErrorCatalogConventionTest`가 검사하는 것과 봉인 집합** — 전역 code 유일성(봉인: `AUTH_REQUIRED` 미러 1건), `name == code`(봉인: `*_VERIFICATION_CODE_*` 6건 — `SMS_`/`MAIL_` × `NOT_FOUND`·`EXPIRED`·`MISMATCH`), `*_NOT_FOUND → 404`(봉인 4건: `SMS_VERIFICATION_CODE_NOT_FOUND`·`MAIL_VERIFICATION_CODE_NOT_FOUND`·`REFERRAL_REFERRER_NOT_FOUND`·`FOLLOW_NOT_FOUND`), HTTP 상태 400~599, 메시지 비공백, 계층 계약(각 enum이 자기 계층 예외로만 던져지는 구조). 봉인 사유는 프론트가 분기하는 wire 계약이라 지금 고치면 클라이언트가 깨지는 것이다 — `backend/domain/AGENTS.md`의 같은 이름 봉인 항목 참고. **봉인 집합에 새 항목을 추가하지 않는다.** 노후 감지 짝 테스트(`whitelistIsNotStale`)가 고쳐진 봉인을 알려준다.

**`ErrorCatalogSnapshotTest`** — 7개 카탈로그의 `(code, status, message)` 합집합이 분할 전 스냅샷 `error-catalog-before.tsv`(442행)에서 삭제한 23개를 뺀 **419건**과 같음을 단정한다. 분할이 wire 계약을 바꾸지 않았다는 증거다. **스냅샷 파일을 갱신해 테스트를 맞추지 않는다** — 코드·상태·메시지를 바꾸면 이 테스트가 실패해야 정상이다. 의도한 wire 변경이라면 그 사실을 문서에 먼저 적는다. `application`의 build.gradle에 `testImplementation project(':api-common-module')`가 추가됐다(`ApiErrorCode`를 검사하기 위함).

### 03a로 domain에서 옮겨 온 설계 근거 (도메인 서비스)

아래 항목은 원래 `domain/AGENTS.md`의 "코드 주석에서 이관된 설계 근거" 절에 있었다. 대상 서비스가 03a로 이 모듈의 `<ctx>/service/`로 옮겨와 함께 옮겼다. **상대 경로(`shop/service/...`·`order/service/...`)는 `backend/application/src/main/java/com/tastyhouse/application/` 기준**이고, 본문에 등장하는 `<ctx>/model/`·`<ctx>/vo/`·순수 계산기(`*Calculator`·`*Policy`)는 여전히 domain(`backend/domain/src/main/java/com/tastyhouse/domain/`)에 있다. 본문은 그대로이며 "도메인 서비스"라는 말은 "(ServiceConfig 삭제 후에는 `@Service` 없이 앱 마커만 단) 마커 없는 POJO로 이 모듈에 사는, 도메인 불변식을 오케스트레이션하는 서비스"를 뜻한다.

#### 도메인 서비스에 남는 것과 애그리거트에 남는 것의 경계

**대상**: `shop/service/ShopDeliveryTipService.java`, `shop/service/ShopBusinessHourService.java`

도메인 서비스에 두는 것은 **행 하나만 보고는 판정할 수 없는 규칙**뿐이다.

- 집합의 개수·정렬·단조성 (구간별 배달팁의 "3개 이하 + 주문금액 오름차순 + 팁 내림차순")
- 두 리소스에 걸친 상호 배타 (거리별 ↔ 지역별)
- 다른 애그리거트 컬렉션을 읽어야 판정되는 것 (지역별 팁의 행정동이 가게 배달가능지역에 속하는지)
- 집합 관계 (같은 요일 시간대 겹침)

**행 하나의 값 불변식(금액 범위·시각 유효성)은 각 애그리거트의 `of`·`update`가 강제한다.** 서비스에 두면 팩토리를 직접 부르는 경로(배치·마이그레이션)가 규격을 우회할 수 있기 때문이다.

#### replace-all 교체와 변경이력 1행 규칙

**대상**: `shop/service/ShopDeliveryTipService.java` → `replaceTiers`·`replaceRegionTips`·`replaceScheduleTips`

**배달팁 컬렉션은 전부 replace-all로 교체한다.** 위 규칙들이 집합 전체를 봐야 판정되므로, 행 단위 CRUD면 어떤 순서로 조작해도 중간 상태가 규칙을 위반한다. (`ShopBusinessHour`가 개별 CRUD인 것은 요일 간에 이런 관계가 없기 때문이다.)

**변경이력 기록도 이 서비스가 소유한다**(`DELIVERY_TIP_TIER`·`DELIVERY_TIP_DISTANCE`·`DELIVERY_TIP_REGION`·`DELIVERY_TIP_SCHEDULE`·`DELIVERY_TIP_HOLIDAY`). 이 서비스가 replace-all을 하려고 **삭제 전에** 컬렉션을 읽을 수 있는 유일한 지점이고, ceo-api의 `CommandService`는 CQRS 교차 주입 금지로 QueryAdapter를 주입할 수 없어 변경 전 값을 구조적으로 볼 수 없다.

**replace-all은 컬렉션 1행당 이력을 남기지 않고 저장 1회당 1행만 남긴다.** `deleteAll + saveAll`로 교체하므로 PK 기반 diff가 불가능하고, 행 단위로 남기면 이력 목록이 "점주가 저장한 횟수"가 아니라 "바뀐 행 수"로 페이징되어 읽을 수 없게 된다. 그래서 변경 전·후 컬렉션 전체를 `ShopChangeValueFormatter#snapshot`으로 요약해 한 행에 담는다.

이 규칙에서 파생되는 개별 판단들:

- **`tier_order`는 호출부가 보낸 순서가 아니라 정렬 후 재부여한다** — 그래야 저장된 순서와 금액 정렬이 어긋나지 않고, 화면에 보이는 순서와 이력의 순서가 일치한다.
- **`clearDistanceTip`은 설정 헤더가 없으면 이력도 남기지 않는다** — 애초에 거리별을 쓰지 않던 가게에 "해제했다"고 기록하면 일어나지 않은 변경이 이력에 남는다.
- **`clearRegionTips`는 전용 `DELETE` 행을 만들지 않고 빈 컬렉션으로의 교체로 기록한다** — 이 경로가 빈 배열 PUT과 완전히 같은 연산이라, 구분하면 같은 결과가 두 형태로 기록된다.
- **`changeHolidayTip`은 0원(삭제)도 `UPDATE` 한 행으로 남긴다** — 이 엔드포인트는 스칼라 하나를 설정하는 경로이고 0원은 그 스칼라의 유효한 값(미설정)이라, 같은 저장 버튼이 금액에 따라 `UPDATE`/`DELETE`로 갈리면 이력 목록에서 같은 조작이 두 종류로 보인다.
- **지역별 팁 스냅샷에서 마스터에 없는 행정동(폐지 동 등)은 식별자를 그대로 노출한다** — 행을 통째로 빠뜨리면 "그때 무엇이 설정돼 있었는가"가 부정확해진다.
- **`loadOrCreateSetting`은 그 시점에 저장하지 않는다** — 호출부가 전환을 마친 뒤 한 번만 저장해야 "만들었지만 전환에 실패한" 빈 헤더가 남지 않는다.
- **빈 목록으로 지역별을 교체할 때, 거리별을 쓰던 가게의 설정은 건드리지 않는다** — 지역별을 비우는 요청이 거리별 설정을 조용히 지우면 안 된다.

#### 시간 구간 겹침 판정 — 자정 넘김 분할

**대상**: `shop/service/ShopDeliveryTipService.java` → `validateScheduleOverlap`·`toSegments`, `shop/service/ShopBusinessHourService.java`

자정을 넘기는 구간은 `[start, 24:00)`과 `[00:00, end)` **두 조각으로 나눠 판정한다.** 그러지 않으면 22:00~02:00과 01:00~03:00처럼 실제로 겹치는 쌍을 놓친다. 두 서비스가 같은 기법을 쓴다.

**서로 다른 요일 구분끼리는 겹침을 검사하지 않는다** — 겹쳐도 적용 시점에 구체성 우선으로 하나만 선택되므로 이중 부과가 생기지 않고, 오히려 DAILY 기본값 위에 특정 요일을 덧씌우는 정상적인 설정 방식이기 때문이다.

#### 부분실패 판정은 "요청 전체를 반영한 뒤의 최종 상태" 기준

**대상**: `product/service/ProductAvailabilityService.java`

메뉴·옵션의 품절·숨김 전이에서 **하나씩 순차로 검사하면 요청 배열의 순서에 따라 결과가 갈린다.** 노출 메뉴가 2개일 때 둘 다 숨김 요청하면 순차 검사는 첫 건을 통과시키고 두 번째만 실패시키는데, 어느 것이 통과할지가 배열 순서에 좌우된다. **최종 상태 기준이면 "노출 메뉴가 0개가 되므로 마지막 1개는 남긴다"는 판정이 결정적이다.**

이 제약들(노출 메뉴 ≥1 · 추천 메뉴 ≥1 · 옵션 `minSelect` 잔여 개수)이 api 모듈이 아니라 도메인에 있는 이유는 **애그리거트 불변식이고, ceo/admin 두 모듈에 흩어지면 한쪽만 고쳐지기 때문**이다.

**이 서비스는 shop 컨텍스트를 참조하지 않는다.** 품절 기간 기본값("익일 가게 오픈 시간") 산출은 `ShopNextOpenTimeCalculator`(shop 컨텍스트)가 담당하고, ceo-api의 command service가 두 서비스를 각각 주입해 조립한다 — `ShopBusinessHour`를 직접 참조하면 컨텍스트 경계 위반이다.

#### 옵션그룹 합치기 — 기준 그룹 불변·흡수 그룹은 숨김

**대상**: `product/service/ProductOptionGroupMergeService.java`

- **기준 그룹은 손대지 않는다.** "기준 옵션그룹"이 곧 살아남는 정의다. 기준을 덮어쓰면 멱등성이 깨지고, 무엇보다 **과거 주문에 박제된 옵션을 조용히 바꾸게 된다.**
- **흡수 그룹은 행을 남긴 채 감춘다**(`hide()`) — `ORDER_PRODUCT_OPTION`이 `option_group_id`로 이 행을 참조하므로 **하드 삭제는 주문 이력을 끊는다.**
- **흡수 그룹의 옵션을 기준 그룹으로 재부모화(union)하지 않는다.** 합치기 확인 화면은 기준 그룹의 옵션 목록 **하나만** 보여주므로 union이면 중복된 합집합이 나와 화면이 약속한 것과 결과가 달라진다. 또한 추천 합치기는 옵션명·가격이 전부 같은 그룹만 제안하므로 union은 순수 중복 생성이다.
- **링크만 기준 그룹으로 옮긴다**(sort 보존 + 재정규화).

#### 주문 접수 — 타 컨텍스트는 전부 소유 컨텍스트의 서비스를 경유한다

**대상**: `order/service/OrderPlacementService.java`

주문 한 건의 접수는 `Order` 헤더 · 상품 라인(`OrderProduct`) · 라인 옵션(`OrderProductOption`) 세 애그리거트를 한 트랜잭션에서 함께 만들고, 그 과정에서 계산한 금액을 헤더에 되반영해야 하는 **원자 연산**이다. 세 애그리거트 중 하나라도 빠지면 주문이 반쪽으로 저장되고, **금액 되반영이 빠지면 결제 금액이 0원인 주문이 남는다.** 쿠폰 사용·포인트 차감까지 같은 트랜잭션에 묶이는 크로스 애그리거트 불변식 오케스트레이션이라 도메인 계층에 둔다.

**타 컨텍스트는 전부 그 컨텍스트의 서비스를 경유한다** — 상품·옵션 검증은 `OrderProductValidationService`(product 소유), 가게 로드·주문가능·최소주문금액·배달지역·배달팁·예약슬롯은 `ShopOrderContextService`(shop 소유), 주문자 조회는 `OrdererLookupService`, 배달 주소 로드·소유권 검증은 `MemberDeliveryAddressService`(둘 다 member 소유)가 담당한다.

과거 이 서비스는 외부 컨텍스트 6개에서 모델·리포지토리를 직접 주입해 26개를 import했는데, 그러면 **각 컨텍스트의 규칙(판매중지 판정·배달지역 미등록 처리·주소 소유권)이 주문 안에서 재구현되어, 소유 컨텍스트가 정책을 바꿀 때 주문 경로만 낡은 규칙으로 남는다.** 지금 남은 타 컨텍스트 의존은 **서비스와 그 결과 record**뿐이며 모델·리포지토리 직접 import는 0건이다.

**이관해도 트랜잭션 경계는 그대로다** — 전부 같은 트랜잭션 안의 동기 호출이며(이벤트로 바꾸지 않는다), 검증 순서·에러코드·응답 계약도 이관 전과 동일하다.

주요 협력자의 존재 이유(생성자 `@param`에 있던 것):

- **`memberDeliveryAddressService`** — 배달 주소 로드와 소유권 검증. **좌표는 저장된 주소에서만 읽는다 — 위조 방지.**
- **`publicHolidayCalendar`** — 접수 시각의 공휴일 여부 판정. **shop이 이 캘린더를 직접 부르면 컨텍스트 경계를 위반하므로, 판정 결과만 배달팁 산출에 넘긴다.**

**주문 접수는 도메인 이벤트를 발행하지 않는다** — 과거 `OrderCreatedEvent`를 발행했으나 수신 리스너가 없는 no-op이어서 제거했다. 접수 이후 비동기 후처리(알림·집계)가 필요해지면 이 메서드 말미에 발행을 다시 추가하면 된다.

**저장 횟수**: 더티 체킹이 없으므로 헤더는 신규 저장 후 금액 갱신으로 **2회**, 상품 라인은 신규 저장 후 가격 갱신으로 **2회** 저장한다.

**반환은 생성된 주문의 식별자(`OrderId`)만이다** — 응답 조립(가게명·상품 라인·결제 요약)은 커밋 이후 소비 모듈의 조회 서비스(web `OrderDetailQueryService`)가 재조회해 담당한다(CQRS 분리).

#### 리뷰 게시중단 — 상태 전이와 반영은 한 트랜잭션

**대상**: `review/service/ReviewBlindRequestService.java`

"점주 요청 → 관리자 심사 → 승인 시 리뷰 숨김 → 30일 뒤 재노출 또는 고객 동의 시 삭제" 워크플로의 규칙은 요청자(ceo)·심사자(admin)·고객(web)·배치가 서로 다른 액터임에도 동일하게 유지되어야 한다.

**승인은 요청 애그리거트의 상태 전이와 리뷰의 숨김 반영이 한 트랜잭션에서 반드시 함께** 일어나야 하는 원자 연산이다 — 둘 중 하나만 반영되면 **"승인됐는데 리뷰가 계속 노출되는"** 상태가 남는다(`ShopImageApprovalService`가 이미지 교체에 대해 갖는 것과 같은 성질).

**`ShopRequestIndexRecorder`를 생성자 필수 의존으로 받는다** — 요청처리 현황(`SHOP_REQUEST_INDEX`)은 파생 읽기모델이고 기록이 누락되면 그 요청이 통합 목록에서 아예 보이지 않는다. 필수 의존으로 두면 **새 상태 전이 메서드를 추가할 때 동기화 배선이 필요하다는 사실이 컴파일 단계에서 드러난다.**

**취소는 원본 애그리거트의 상태 전이**이며, 취소 후에는 같은 리뷰에 재요청이 가능해진다 — PENDING 중복 차단과 1회 제한이 모두 상태 조회에 기반하므로 **코드 추가 없이 자동으로 풀린다.**

**대상 리뷰가 그 가게의 것인지 역조회로 재검증한다** — 경로의 `shopId`만 믿으면 남의 가게 리뷰에 게시중단을 걸 수 있다.

#### 결제 승인 — PG HTTP 왕복은 트랜잭션 밖

**대상 (이동됨 — 덩어리 02/03a)**: `PaymentConfirmationService`(+`PgConfirmation`·`PgConfirmationTarget`)는 `domain`을 떠나 `backend/application/src/main/java/com/tastyhouse/application/payment/service/PaymentConfirmationService.java`로 이동했다(POJO+마커 등록 패턴, ~~`application/payment/config/PaymentServiceConfig`가 `@SharedApp`로 등록~~ 지금은 클래스에 `@SharedApp` 마커만 — `PaymentServiceConfig`는 삭제됨). `PgPaymentGateway`/`PgProviderGateway`도 같은 이동으로 `application`의 `payment/port/out`에 있다. **아래 설계 근거는 이동 후에도 그대로 유효**하므로 삭제하지 않고 남기며, 대상 파일 경로만 정정한다 — 서비스 자체는 02에서 domain `ContextBoundaryTest`의 봉인 목록을 떠났고, 03a에서 이 모듈의 `ServiceContextBoundaryTest` 봉인 목록으로 다시 들어왔다.

결제 승인은 결제 애그리거트의 상태 전이와 주문 애그리거트의 확정 전이를 한 트랜잭션에서 반드시 함께 수행해야 하는 원자 연산이다. 한쪽만 반영되면 **"결제는 됐지만 주문은 대기"** 이거나 **"주문은 확정인데 결제는 미승인"** 인 정합성 붕괴가 남는다. 승인 경로는 세 가지(PG 콜백 · PG 승인 · 현장결제 완료)인데 규칙은 하나여야 하므로 유스케이스 계층에 둔다.

주문 상태 전이는 직접 `order.confirm()`을 호출하지 않고 `OrderTransitionService`(03a로 이 모듈의 `order/service/`로 이동)에 위임한다 — **전이와 저장을 항상 함께 수행한다는 규칙의 단일 원천을 주문 도메인에 유지하기 위함**이다.

**PG HTTP 왕복은 이 서비스 안에서 하지 않는다.** PG 승인은 (1) 금액·상태·소유권을 검증하는 `preparePgConfirmation`(DB 읽기, 트랜잭션 안)과 (2) PG 응답을 반영하는 `applyPgConfirmation(memberId, pgProvider, pgOrderId, result)`/`failPgConfirmation`(DB 쓰기, 별도 트랜잭션)으로 쪼개져 있고, **그 사이의 PG 호출은 소비 모듈이 트랜잭션 밖에서, 도메인 포트 `PgPaymentGateway`(구현은 라우터 `PgPaymentGatewayRouter`, 이 라우터도 `application`으로 이동)를 통해 수행한다.**

과거에는 PG 왕복 전체가 DB 트랜잭션 안에 있어 커넥션·행 락을 네트워크 지연만큼 점유했고, **PG 승인 성공 후 커밋이 실패하면 "PG는 승인, DB는 미승인"이 되어 보상이 불가능했다.** 그래서 이 서비스는 `PgPaymentGateway`를 주입받지 않는다.

이벤트 발행은 Spring `ApplicationEventPublisher`를 직접 주입하지 않고 포트 `DomainEventPublisher`를 쓴다. **(번복됨 — 03a)** 과거에는 이 포트가 domain에 남았으나 03a에서 이 모듈의 `shared/event/`로 이동했다(구현 `SpringDomainEventPublisher`도 함께 — 아래 "덩어리 03a" 절).

#### 예약 — 정원 차감은 낙관적 락, 재시도는 트랜잭션 밖

**대상**: `reservation/service/ReservationBookingService.java`

예약 생성은 "슬롯 정원 검증 → 정원 차감 → 예약 저장"이, 취소·거절은 "예약 상태 전이 → 슬롯 정원 반납"이 반드시 함께 일어나야 하는 원자 연산이다. 도메인 계층에 두어 **트리거 액터(회원 취소 · 점주 거절)가 달라도 "예약 건수와 슬롯 점유 수는 항상 함께 움직인다"는 규칙이 갈리지 않게** 한다.

**동시성**: 정원 차감은 슬롯의 낙관적 락(`@Version`)으로 보호한다. 이 서비스는 차감 직후 `saveAndFlush`로 **충돌을 트랜잭션 커밋 전에 노출시키기만 하고 재시도는 하지 않는다** — 재시도는 매 시도마다 새 트랜잭션이 필요하므로 트랜잭션 경계 **바깥**(소비 모듈의 command 서비스)에서 수행해야 한다. 충돌 예외는 프레임워크-프리 `OptimisticLockConflictException`으로 번역되어 올라간다(infrastructure 어댑터가 번역).

#### 자주 쓰는 문구 — 5개 상한은 완전하지 않다 (의도된 감수)

**대상**: `ceo/service/CeoReplyPhraseService.java`

**5개 상한은 DB가 아니라 애플리케이션 코드가 강제한다 — 그래서 완전하지 않다.** MySQL에는 "한 점주당 행 5개 이하" 같은 행 수 제약을 걸 수단이 없으므로, 건수 조회와 삽입 사이의 경합을 막을 최종 방어선이 존재하지 않는다. **같은 점주가 동시에 등록 요청을 보내면 6개가 될 수 있다.**

이를 감수하는 이유는 (1) 이 목록이 답변 작성 시 골라 쓰는 **표시용**이라 6개가 되어도 데이터 정합성이나 금전에 피해가 없고, (2) 이를 막으려면 점주 행에 비관적 잠금을 걸어야 하는데 **그 비용이 피해에 비해 과하기** 때문이다. 초과 상태가 발견되면 점주가 하나 지우면 그만이다.

소유권은 `shopId`가 아니라 **문구의 `ceoId`와 요청 점주의 일치**로 검증한다 — 문구는 가게가 아니라 점주 계정에 귀속되므로 `ShopOwnershipValidator`가 개입할 자리가 없다. 불일치는 404가 아니라 `CEO_REPLY_PHRASE_ACCESS_DENIED`(403)다.

**등록·수정 시점에 금칙어를 검수**하므로 실제 답변에 넣을 때 `ReviewOwnerReplyService`가 한 번 더 검수하는 것과 중복되지만 **그것이 의도다** — 문구 등록 후 금칙어 목록이 늘어났을 수 있다.

#### 03a로 `infrastructure:persistence`에서 옮겨 온 설계 근거

아래 항목은 `infrastructure/persistence/AGENTS.md`에 있던 것이다. 대상(`<Ctx>DomainConfig` 18개 → `<Ctx>ServiceConfig`, `CachingProhibitedWordPersistencePort`, `SpringDomainEventPublisher`)이 03a로 이 모듈로 옮겨와 함께 옮겼다. 본문의 "`<Ctx>DomainConfig`"는 ~~지금의 `<Ctx>ServiceConfig`로 읽는다~~ **(번복됨 — application `*ServiceConfig` 삭제)** **같은 이름의 서비스 클래스**(`@Bean` 메서드명 = 클래스명 첫 글자 소문자 = 지금의 빈 이름)로 읽는다. `<Ctx>ServiceConfig`는 전부 삭제됐고, domain 계산기·`prohibitedWordValidator`·`shopDeliveryTipRangePolicy`만 `shared/config/SharedBeanConfig`의 `@Bean`으로 남았다.

##### `<Ctx>ServiceConfig`(구 `<Ctx>DomainConfig`) — 도메인 서비스 빈 등록 근거

**대상**: ~~`backend/application/src/main/java/com/tastyhouse/application/*/config/*ServiceConfig.java`~~ (번복됨 — 삭제) → 지금은 `backend/application/src/main/java/com/tastyhouse/application/*/service/` 의 마커-only 도메인 서비스 클래스들 + `backend/application/src/main/java/com/tastyhouse/application/shared/config/SharedBeanConfig.java` (03a 이전 `backend/infrastructure/persistence/.../*/config/*DomainConfig.java` 18개)

등록 위치 규칙 자체는 이 문서의 "덩어리 03a" 절에 있다. 여기에는 **각 `@Bean`이 왜 도메인 서비스인가**(= 왜 애그리거트나 api 모듈이 아닌가)라는 판단 근거를 모은다. 전 config에 공통으로, 클래스 Javadoc은 "도메인 서비스는 `@Service` 없는 순수 POJO라 Spring이 스캔할 수 없으므로 새 POJO 도메인 서비스를 추가하면 여기에 `@Bean`을 추가한다"는 같은 문장이었다 — 규칙 절과 중복이라 옮기지 않는다.

##### 도메인 서비스로 뺀 판정 기준

주석들이 반복해 든 사유는 아래 네 가지다. 새 서비스를 만들 때도 이 기준으로 판단한다.

| 사유 | 뜻 | 예 |
|---|---|---|
| **집합 차원 불변식** | 행 하나만 보고는 판정할 수 없다 | `ShopNoticeExposureService`(가게당 노출 공지 1건) · `ShopMenuCollectionImageService`(최대 6·최소 1) · `ShopDeliveryAreaAdjustmentService`(진행 중 신청 중복 차단) |
| **크로스 애그리거트 원자성** | 여러 애그리거트가 한 트랜잭션에서 함께 바뀌어야 한다 | `ShopImageApprovalService`(요청 승인 + 이미지 반영) · `ShopPhoneNumberRegistryService`(대표번호 + 가게 애그리거트) · `ProductReviewStatsService` |
| **액터 무관 규칙** | 요청자(ceo)와 검수자(admin), 또는 admin CRUD와 batch 크롤링이 **같은 규칙**을 써야 한다 | `ProductRegistrationService` · `ShopOrderNoticeService` · `ShopRequestCancellationService`(당시 이름 `ShopRequestCancelService` — 지금 그 이름은 이 도메인 서비스를 호출하는 유스케이스 서비스가 쓴다) |
| **컨텍스트 경계 파사드** | 소비 컨텍스트가 남의 모델·리포지토리를 직접 쓰지 않게 한다 | `ShopOrderContextService` · `OrderProductValidationService` |

복제하면 한쪽만 고쳐진다는 것이 공통 위험이다 — `ShopNextOpenTimeCalculator`가 요일별 영업시간 선택 규칙을 새로 짜지 않고 `ShopOperatingStatusCalculator`를 주입해 재사용하는 것도 같은 이유다(복제하면 요일 구분 추가 시 한쪽만 고쳐진다).

##### `ShopDomainConfig` — 개별 판단

**대상**: ~~`backend/application/src/main/java/com/tastyhouse/application/shop/config/ShopServiceConfig.java (구 `ShopDomainConfig`)`~~ (번복됨 — 삭제) → 각 항목의 빈 이름과 같은 이름의 클래스 `backend/application/src/main/java/com/tastyhouse/application/shop/service/{ShopOrderAvailabilityService,ShopDeliveryAreaPolygonService,...}.java`. `prohibitedWordValidator`·`shopNextOpenTimeCalculator`는 `backend/application/src/main/java/com/tastyhouse/application/shared/config/SharedBeanConfig.java`의 `@Bean`

- `prohibitedWordValidator` — **캐싱 데코레이터로 감싼 포트를 주입한다.** 검증기가 텍스트 검증마다 `findAll()`을 호출하므로 전량 로드가 매번 DB로 나가지 않게 한다. 금칙어는 SQL 시드 read-only 데이터라 정합성 리스크가 낮고, **캐싱을 어댑터 쪽에 두어 domain의 순수 POJO 검증기는 그대로 둔다.**
- `shopNextOpenTimeCalculator` — **product가 아니라 shop에 둔다.** 영업시간·휴무일 해석은 shop의 관심사이고, product 도메인 서비스가 `ShopBusinessHour`를 직접 참조하면 **컨텍스트 경계 위반**이다. 두 서비스의 조립은 ceo-api의 command service가 담당한다.
- `shopOrderAvailabilityService` — 주문 접수(`OrderPlacementService`)와 예약 생성(`ReservationBookingService`)이 **같은 규칙**을 쓰도록 검증을 이 서비스 하나에 모았다.
- `shopDeliveryAreaPolygonService` — 도형 원본과 그것을 환산한 행정동 집합이 **같은 트랜잭션에서 항상 일치**해야 한다. 환산을 비동기로 미루면 "저장은 됐는데 주문은 거절되는" 창이 생기고, **그 사이 등록 건수가 0이 되면 주문 접수의 지역 검사가 통째로 비활성된다.**
- `shopDeliveryAreaRadiusService` — 후보 행정동을 **write 포트로 읽는다.** 명령 경로가 infra query DAO를 주입하면 CQRS 교차 주입 금지 규칙에 걸린다. 거리 판정은 원 근사 다각형이 아니라 **하버사인 직선거리**로 한다.
- `shopOriginInfoService` — **금칙어 검수를 하지 않는다.** 원산지 본문은 마케팅 문구가 아니라 **법령이 요구하는 사실 표시**라, 검수로 저장을 막으면 표시 의무를 이행할 수 없게 된다.
- `shopChangeHistoryRecorder` / `shopCeoAssignmentRecorder` / `shopRequestIndexRecorder` — 변경을 수행하는 도메인 서비스가 **같은 트랜잭션에서 동기 호출**한다. **새 배정 경로나 새 요청 성격 애그리거트를 만들면 그 도메인 서비스에 이 Recorder를 배선해야 한다** — 배선 누락은 컴파일에 걸리지 않는다. `ShopChangeValueFormatter`는 상태 없는 static 유틸이라 빈으로 등록하지 않는다.
- `shopOrderNoticeService` — PUT 하나가 기존 행 유무에 따라 insert/update로 갈리므로 단일 애그리거트 연산이 아니고, 그 분기 규칙을 ceo·admin 두 api 모듈이 각자 갖지 않도록 도메인 서비스가 소유한다. 승인 절차가 없어 상태 전이가 `hidden` 하나뿐이라 서비스를 더 쪼갤 이유가 없다.
- `shopCeoAssignmentService` — 재배정을 `REVOKE` + `GRANT` **2행**으로 남긴다.

##### `ProductDomainConfig` — 개별 판단

**대상**: ~~`backend/application/src/main/java/com/tastyhouse/application/product/config/ProductServiceConfig.java (구 `ProductDomainConfig`)`~~ (번복됨 — 삭제) → 각 항목의 빈 이름과 같은 이름의 클래스 `backend/application/src/main/java/com/tastyhouse/application/product/service/{ProductDeletionService,ProductSortService,...}.java`. `cupDepositPolicy`는 `backend/application/src/main/java/com/tastyhouse/application/shared/config/SharedBeanConfig.java`의 `@Bean`(여전히 단일 빈이라 "같은 인스턴스" 근거는 유효하다)

- `cupDepositPolicy` — 순수 계산기인데도 **빈으로 두는 이유는 요율을 단 한 곳에 두기 위함**이다. 점주 설정(ceo)·손님 메뉴판(web)·주문 금액 확정(order) 세 경로가 **같은 인스턴스를 주입받아야** "화면 금액과 결제 금액이 다른" 사고가 구조적으로 불가능해진다.
- `productDeletionService` — 삭제에도 숨김과 **같은 불변식**(노출 메뉴 ≥1 등)을 적용한다. **숨김만 막고 삭제를 열어두면 점주가 삭제로 우회해 빈 메뉴판을 만들 수 있다.**
- `productSortService` — `sort` 값을 클라이언트에서 받지 않고 **순서 있는 id 배열만 받아 서버가 0..N-1로 정규화**한다.
- `productOptionGroupLinkService` — **옵션그룹은 단일 가게에만 속한다**는 불변식을 강제해, 소유권 판정에서 ANY/ALL 구분이 사라지게 한다. 이 불변식이 `ProductQueryAdapter#findLinkedProductsByShop`의 단일 조회를 성립시킨다.
- `productOptionGroupMergeService` — 링크 재배치는 `ProductOptionGroupLinkService#relink`에 위임한다. UNIQUE 충돌 처리와 sort 불변식이 그 클래스 소유로 남아야 `renumber`를 공개하지 않아도 된다.
- `productExposureService` — 요일 묶음과 개별 요일의 **혼용을 금지한다.** 그 조합을 저장할 수 없게 하면 SQL 술어(`ProductQueryAdapter#exposedNow`)와 계산기(`ProductExposureCalculator`)가 갈릴 여지가 없다.
- `productNutritionService` — 영양성분과 알레르기를 **한 서비스가 소유한다.** 나누면 "영양성분만 저장되고 알레르기는 이전 값이 남은" 중간 상태가 손님 화면에 **잘못된 알레르기 표시**로 노출된다. 승인 워크플로가 없는 것은 점주(가맹본사)만이 아는 사실 정보여서 관리자가 검증할 근거가 없기 때문이다.
- `productPriceService` — **전체 교체(PUT) 의미론**이라 정렬·가격명 중복 같은 컬렉션 단위 불변식을 한 번에 판정한다. `sort=0` 행의 배달가를 `PRODUCT.original_price`에 **동기화**해 그 컬럼을 읽는 기존 수십 경로(주문·검색·오늘의할인·목록)의 동작을 그대로 유지한다. `StorePriceVerificationPort`를 받는 이유는 가격 변경으로 배달가 > 매장가가 되면 **그 자리에서** 가게 인증을 내려야 하기 때문이다 — 배치로 미루면 그 사이 손님이 잘못된 뱃지를 본다.

###### 승인 워크플로의 방향 비대칭

→ `productImageApprovalService` · `productRepresentativeApprovalService` · `productVegetarianApprovalService`

**등록·지정은 승인을 거치고, 순서 변경·삭제·해제는 즉시 반영된다.** 검수의 목적이 "부적합한 내용의 노출을 막는 것"이므로 내리는 방향에는 그 위험이 없기 때문이다. 채식 설정만은 신청조차 즉시 반영하지 않는데, **채식 표기가 알레르기·신념과 직결돼 잘못된 표기의 대가가 크기** 때문이다.

사장님 추천은 가게당 최대 6개·이미지 필수·최소 1개 유지 세 제약을 `ProductRepresentativeApprovalService`가 단독으로 소유한다. 세 번째 제약은 일괄 숨김(`ProductAvailabilityService`)이 이미 쓰는 `PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE`를 재사용하므로, **두 경로가 같은 하한을 공유한다.**


##### `CachingProhibitedWordPersistencePort` — 캐싱은 도메인이 아니라 어댑터에 둔다

**대상**: `backend/application/src/main/java/com/tastyhouse/application/shop/service/CachingProhibitedWordPersistencePort.java`
→ `TTL` · `Snapshot`

`ProhibitedWordValidator`는 텍스트 검증 때마다 `ProhibitedWordPersistencePort#findAll()`을 호출하는데, 점주 입력(가게소개·찾아오는길 등) 저장 경로마다 금칙어 테이블을 통째로 다시 읽는 것이 낭비다. **검증기는 마커 없는 순수 POJO(03a 이전에는 domain 소속)라 스프링 `@Cacheable`을 붙일 수 없으므로**, 캐싱을 write 포트를 감싸는 데코레이터로 구현하고 빈 등록 지점(`SharedBeanConfig#prohibitedWordValidator`, 구 `ShopServiceConfig` ← `ShopDomainConfig`)에서 감싼다 — 검증기·도메인 서비스 코드는 그대로다. 03a 이전에는 이 데코레이터가 persistence(`infrastructure/shop/persistence/`)에 있었으나, 감싸는 쪽(설정)이 이 모듈로 오면서 함께 옮겼다 — 순수 자바라 기술 의존이 없다.

금칙어는 SQL 시드로 관리되는 read-only 데이터(Java 계층에 생성·수정 경로가 없다)라 정합성 리스크가 낮다. 그래도 **무기한 캐싱은 시드 갱신이 재기동 전까지 반영되지 않으므로 TTL(10분)을 둬서 자연히 만료시킨다.**

`AtomicReference`에 (적재 시각, 목록) 스냅샷을 통째로 담아 교체하므로 **락이 필요 없다.** 만료 직후 동시 호출이 겹치면 적재가 중복될 수 있으나 결과가 같은 read-only 조회라 무해하다 — 중복 적재를 막는 락이 주는 이득보다 락 경합 비용이 크다.


##### `SpringDomainEventPublisher` — 발행 포트 어댑터

**대상**: `backend/application/src/main/java/com/tastyhouse/application/shared/event/SpringDomainEventPublisher.java`

`DomainEventPublisher` 포트(03a로 domain에서 이동, 같은 패키지)를 Spring `ApplicationEventPublisher`에 위임한다. `@TransactionalEventListener`/`@EventListener` 기반 리스너가 그대로 수신한다. **03a 이전에는 persistence의 마커 없는 `@Component`였고**, 지금은 `@Component`를 떼고 `shared/config/SharedBeanConfig`(`@SharedApp`, 구 `SharedEventConfig`)가 `@Bean domainEventPublisher`로 등록한다 — 포트가 `port.out` 밖(`shared/event`)에 있어 persistence가 볼 수 없게 됐기 때문이다. 리스너도 같은 모듈의 `<ctx>/listener/`에 있다.

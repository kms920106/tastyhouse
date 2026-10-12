# ADR-0036: 앱 마커 애노테이션을 제거하고 앱 소속을 Gradle 모듈(core + {앱}-application 4개)로 표현한다

- 상태: 채택
- 결정 커밋: `d4688a682` (2026-10-04). 후속 `406c34ebe`(같은 날, `application-module.properties` 표식 제거 → `ModuleOrigin` 판정)도 이 결정에 포함합니다.
- 대체한 결정: [ADR-0013](0013-application-module-merge.md), [ADR-0014](0014-package-flattening-app-markers.md), [ADR-0034](0034-service-config-removal.md). [ADR-0024](0024-event-listeners-in-application.md)·[ADR-0025](0025-presentation-domain-cut.md)의 `@SharedApp` 부분도 대체합니다.

## 맥락

이 결정 전까지 application 계층은 **모듈 하나**였습니다. 챕터 01이 앱별 모듈 4개를 합쳤고([ADR-0013](0013-application-module-merge.md)), 챕터 03이 패키지를 `com.tastyhouse.application` 하나로 평탄화했습니다([ADR-0014](0014-package-flattening-app-markers.md)). 이 상태에서는 패키지만 보고 클래스가 어느 앱 것인지 알 수 없었습니다. 그래서 **앱 마커**가 앱 소속을 표현했습니다.

- **앱 마커**: `com.tastyhouse.application.shared.marker`의 `@WebApp`/`@AdminApp`/`@CeoApp`/`@BatchApp`과, "앱 소속 없음 = 4앱 전부"를 뜻하는 `@SharedApp`입니다.
- 각 앱 부트스트랩의 스캔 설정은 `useDefaultFilters = false` + `includeFilters = ANNOTATION {XxxApp, SharedApp}`로 자기 마커만 골라 빈으로 올렸습니다.

마커는 **컴파일러가 모르는 약속**이었습니다. 잘못 달거나 빠뜨려도 빌드는 통과했습니다. 마커 없는 `@Service`는 어느 앱에도 뜨지 않다가, 그 빈이 처음 필요한 기동 시점에 `NoSuchBeanDefinitionException`으로 드러났습니다. 이 구멍을 `AppIsolationTest`(파일 전체), `beansShouldHaveExactlyOneAppMarker`, `adaptersShouldOnlyUseOwnAppUseCases` 같은 ArchUnit 규칙 여러 개로 메웠습니다.

## 이전 규칙

### backend/application-core/AGENTS.md — application(모듈 소개)·챕터 03

- 4개 앱의 application 계층을 담는 단일 모듈이었습니다. 인바운드 포트와 그 구현, 읽기 계약 326개, 도메인 이벤트 리스너 12종이 한 패키지 트리 안에 함께 있었고, 앱 소속은 마커가 표현했습니다.
- `shared/marker/{WebApp,AdminApp,CeoApp,BatchApp}`은 "앱 소속의 유일한 단서"였고, `SharedApp`은 리스너와 공유 `@Configuration`용 5번째 마커였습니다.
- 패키지 트리에서 `port/in/`의 UseCase 인터페이스는 마커를 달았고, Command record는 마커 없이 소속을 testFixtures `AppOwnership`이 유도했습니다. `shared/config/SharedBeanConfig`(구 `SharedEventConfig`)는 `@SharedApp`이었고, `listener/`의 리스너는 `@Component @SharedApp` + `@TransactionalEventListener(AFTER_COMMIT)`였습니다.
- "`com.tastyhouse.application`을 이 모듈이 단독 소유한다 — split package가 끝났다"고 적었습니다. 공유 계약을 `domain`에 두던 시기의 가드 3종(`ReadContractSingleOwnerTest`·`ReadContractPurityTest`·`RuleAnchorTest`의 소유 모듈 필터)이 챕터 04로 사라졌다는 기록입니다. 이 결정으로 5모듈 split package가 다시 생겼습니다.

### backend/application-core/AGENTS.md — ArchUnit 클래스 구성(마커 판정 시기)

- 챕터 03 이후 `AppIsolationTest`는 슬라이스/패키지 술어에서 마커 애노테이션 술어로 전면 재작성된 클래스였습니다. 내용은 `appsShouldNotDependOnEachOther`(마커 5종 5×4=20조합 개별 검사. `@SharedApp`이 `AppOwnership.MARKERS`에 들어가 "공유 리스너는 앱 전용 빈에 의존할 수 없고, 앱 전용 빈도 공유 리스너에 의존할 수 없다"까지 강제), `beansShouldHaveExactlyOneAppMarker`(리스너는 `@SharedApp` 하나로 통과), `useCasesShouldHaveExactlyOneAppMarker`, `commandRecordsShouldBelongToExactlyOneApp`(`AppOwnership` 유도), `markerBeanCounts`·`markerUseCaseCounts`, 그리고 덩어리 02/03a 신설 `sharedBeansShouldNotDependOnWebOnlyServices`(`@SharedApp` 빈은 `MailVerificationService`·`SmsVerificationService`·`PgPaymentGatewayRouter`·`PgPaymentGatewayPort`를 의존하지 않는다)였습니다.
- `LayerRulesTest`의 공유 마커 규칙은 `listenersShouldBeShared`(`@TransactionalEventListener` 메서드를 가진 클래스는 `@SharedApp`이 붙고 `..listener..`에 있어야 한다 — 마커 누락 시 리스너가 어느 앱에도 뜨지 않아 이벤트가 조용히 유실된다), `sharedAppOnlyOnListeners`, `sharedConfigsShouldOnlyDeclareUnmarkedBeans`(`@SharedApp` 설정은 `@Component`/`@Service`를 겸하지 않고, `@Bean` 반환 타입과 그 설정이 생성자를 호출하는 클래스 전부가 앱 마커를 갖지 않아야 한다. 대상이 0개일 때 failOnEmptyShould를 피하려고 `should()`가 아니라 위반을 손으로 모으는 테스트로 썼다)였습니다. 귀결로 앱 전용 `@TransactionalEventListener`는 둘 수 없었습니다.
- 마커별 하한: `markerBeanCounts`는 처음 실측 web 66·admin 62·ceo 101·batch 13보다 낮은 `@WebApp` ≥60·`@AdminApp` ≥55·`@CeoApp` ≥95·`@BatchApp` ≥12와 리스너 12종인 `@SharedApp` ≥12였고, [ADR-0034](0034-service-config-removal.md) 후 `@WebApp` ≥83·`@AdminApp` ≥65·`@CeoApp` ≥122·`@BatchApp` ≥15·`@SharedApp` ≥47(리스너 12 + 공유 도메인 서비스 35)이었습니다. `markerUseCaseCounts`는 `@WebApp` ≥50·`@AdminApp` ≥100·`@CeoApp` ≥95·`@BatchApp` = 7(정확히 일치)이었습니다. 합계 하나로 두면 한 앱이 통째로 사라져도 나머지 세 앱이 하한을 떠받치므로 마커별로 유지했습니다.
- batch 선별은 마커 술어였습니다. `commandRecordsShouldBeBoundaryTyped`는 `.areNotAnnotatedWith(BatchApp.class)`로 batch를 제외했고(batch는 carve-out이 `domain.exception..` 하나뿐인 엄격판을 `BatchSchedulerRulesTest`에서 쓴다), `BatchSchedulerRulesTest`도 마커 술어로 batch만 골랐습니다.

### backend/application-core/AGENTS.md — Dependencies — java-test-fixtures

- testFixtures에는 `ApplicationLayerScanAssertions`와 함께 `AppOwnership`이 있었습니다. `testFixturesApi`로 `archunit-junit5`를 노출한 이유는 `AppOwnership`이 마커 애노테이션(main)과 ArchUnit을 함께 보기 때문이었습니다.

### backend/application-core/AGENTS.md — `DESERIALIZED_COMMANDS` — 고아 Command record 봉인

- `AppOwnership.DESERIALIZED_COMMANDS`는 유도가 닿지 않는 Command record의 봉인 목록이었고, 구성원은 `com.tastyhouse.application.shop.port.in.ShopStorePriceVerificationItemCommand`(`CeoApp`) 1개였습니다. 이 record는 multipart 문자열 파트로 들어와 서비스가 `ObjectMapper`로 역직렬화하므로 어느 UseCase 시그니처에도 등장하지 않습니다. "이 목록에 새 항목을 추가하지 않는다 — 추가 전에 그 record를 어디서 만드는지 먼저 찾는다"가 규칙이었습니다. 목록은 `AppOwnership`과 함께 삭제됐고, 지금은 그 record가 `ceo-application`에 있다는 사실이 소속입니다. 정적 참조가 없다고 죽은 코드로 지우면 안 된다는 점은 그대로입니다.

### backend/application-core/AGENTS.md — 앱 클래스패스가 곧 스캔 포함 기준이다

- 새 빈과 새 UseCase 인터페이스는 반드시 마커 하나를 달고, Command record에는 붙이지 않았습니다(소속은 `AppOwnership` 유도).
- 마커는 `@Component` 메타를 얹지 않은 순수 마커로 유지했습니다. 얹으면 기존 `@Service`의 의미가 흐려지기 때문이었습니다.
- "application은 자기 등록하지 않는다"의 이유는 "4개 앱의 빈이 같은 jar에 있고 마커로만 갈려, 클래스패스 존재만으로 어느 앱인지 결정할 수 없다"였습니다. 이 결정 후 규칙은 유지되고 이유만 "스캔 범위는 앱 부트스트랩이 정한다"로 바뀌었습니다.
- 패키지 기반 include(`..listener..`를 통째로 포함)는 스캔 규칙이 "마커"와 "패키지" 두 가지로 갈리므로 채택하지 않았습니다.

### backend/application-core/AGENTS.md — 리스너 배치·`ErrorResponses`

- 리스너는 과거 "모든 실행 모듈이 스캔하는 곳"이라는 근거로 `infrastructure:persistence`에 있었고, application으로 옮길 때 4앱 모두에 뜨게 하는 마커 `@SharedApp`을 신설해 같은 근거를 성립시켰습니다. 지금은 마커 없이 코어에 두며, 리스너별 근거는 "리스너를 앱 모듈에 두지 않는 이유"로 읽습니다(`listenersAndConfigsShouldResideInCore`).
- `ErrorResponses`를 빈이 아니라 정적 유틸로 둔 근거 중 하나는 "빈으로 만들면 앱 마커가 필요하고(`beansShouldHaveExactlyOneAppMarker`), 마커 스캔은 앱 부트스트랩이 하므로 앱 부트스트랩 없이 뜨는 컨텍스트(`ApiCommonAutoConfigurationTest`, imports 제거 후 `ratelimit/ApiCommonRateLimitConfigTest`)에서 빈을 찾지 못한다"였습니다.

### backend/application-core/AGENTS.md — 빈 배선

- 스캔 필터가 마커 `ANNOTATION`이므로 **마커가 곧 스캔의 유일한 포함 기준**이었습니다. `@Service` 없이 마커만 단 클래스도 빈이 됐고, 도메인 서비스가 이 형태였습니다([ADR-0034](0034-service-config-removal.md)).
- 4개 스캔 설정 전부가 `SharedApp.class`를 포함했으므로, `@SharedApp` 빈(리스너 12종, `SharedBeanConfig`, 공유 도메인 서비스 35개)은 4앱 전부에 떴습니다.

### backend/application-core/AGENTS.md — 도메인 서비스는 클래스에 앱 마커만 단다

- **도메인 서비스에 `@Service`를 달지 않습니다.** `ServiceContextBoundaryTest`가 스테레오타입 붙은 클래스를 대상에서 조용히 빼기 때문입니다. "마커만 = 도메인 서비스, `@Service` + 마커 = 앱 오케스트레이터"로 갈랐습니다.
- 가드는 `markerOnlyClassesShouldBeDomainServices`·`sharedAppOnlyOnListeners`·`sharedConfigsShouldOnlyDeclareUnmarkedBeans`, `AppIsolationTest`의 `constructorDependenciesShouldBeVisibleToApp`·`appRestrictedPortDependentsShouldBelongToThatApp`·`appsShouldNotDependOnEachOther`·`beansShouldHaveExactlyOneAppMarker`·`markerBeanCounts`였습니다.
- 새 도메인 서비스는 `@Bean`이 아니라 클래스에 소비 앱 마커만 달았습니다. 앱 전용 채널 포트를 받는 클래스는 그 앱 마커여야 했습니다(web 전용 `MailSenderPort`·`SmsSenderPort`·`PgProviderGatewayPort`·`SocialOAuthClientPort`는 `@WebApp`, batch 전용 `BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort`는 `@BatchApp`).
- `SharedBeanConfig`에 마커가 붙은 클래스를 `@Bean`으로 추가하면 스캔 빈과 이름이 겹쳐 기동이 실패했고, `sharedConfigsShouldOnlyDeclareUnmarkedBeans`는 이를 `@SharedApp` 설정에 한해 잡았습니다. 그래서 "`@WebApp` 등 앱 마커 설정을 새로 만들어 같은 일을 하면 이 겹침을 어떤 테스트도 잡지 못한다 — 앱 전용 config를 만들지 않는다"고 적었습니다.
- "`markerBeanCounts` 하한과 `markerOnlyClassesShouldBeDomainServices`의 하한 78을 낮추지 않는다 — 이관이 되돌려져 서비스가 마커를 잃으면 하한이 깨지는 것이 의도다"라는 규칙이 있었습니다.

### backend/CLAUDE.md — 모듈 지도·패키지 최상위 지도

- 다이어그램은 "앱 소속은 패키지가 아니라 마커 애노테이션이 표현한다(리스너 전용 `@SharedApp` = 4앱 전부)", "ArchUnit이 막는다 — `AppIsolationTest` · `adaptersShouldOnlyUseOwnAppUseCases`"라고 적었습니다.
- `com.tastyhouse.application..`는 `application` 한 모듈이 단독 소유했습니다(챕터 04로 split package가 끝났다고 기록).
- 4앱의 `implementation`은 `:application` 하나였습니다.
- import 순서 예시(`NoticeCommandService`, 당시 이름)에는 `com.tastyhouse.application.shared.marker.AdminApp` import가 들어 있었습니다. 마커 삭제로 지금 코드에는 없고, 정렬 규칙 예시로서만 유효합니다.

### backend/web-api·admin-api·ceo-api·batch-module AGENTS.md — Purpose / Internal

- 각 앱의 `application` (implementation) 항목은 "코어 + 마커 애노테이션 `XxxApp`·`SharedApp`(부트스트랩 중첩 `ApplicationLayerScanConfig`가 스캔 기준으로 사용. 과거에는 `{App}ApplicationConfig`를 `@Import`)"이었습니다.
- `testFixtures(project(':application-core'))`는 `ApplicationLayerScanAssertions`와 함께, `adaptersShouldOnlyUseOwnAppUseCases`가 Command record의 앱 소속 유도(`AppOwnership`)를 공유하기 위한 것이기도 했습니다(챕터 03).
- Purpose의 챕터 03 콜아웃은 "패키지만으로는 이 모듈이 주입하는 UseCase가 어느 앱 것인지 알 수 없으므로 앱 소속은 마커 애노테이션(`@WebApp`/`@AdminApp`/`@CeoApp`)이 표현한다"였습니다.

### backend/batch-module/AGENTS.md — 패키지 구조 / 규칙 / `adaptersShouldOnlyUseOwnAppUseCases`

- batch 소속은 마커 `@BatchApp`이 표현했습니다(지금은 `batch-application` 모듈에 있다는 사실이 표현합니다).
- 이 모듈에 남은 규칙은 `shouldNotDependOnInfrastructurePersistence`·`schedulersShouldDependOnUseCasesOnly`·`adaptersShouldOnlyUseOwnAppUseCases` 3개였고, 이 결정으로 2개가 됐습니다.
- `adaptersShouldOnlyUseOwnAppUseCases`는 챕터 01이 4개 application 모듈을 합치며 사라진 컴파일 게이트를 대체하는 규칙이었습니다. 챕터 01 원본은 자기를 뺀 3개 앱 패키지를 열거해 금지했고, 챕터 03 평탄화 후에는 마커로 판정하도록 재작성됐습니다. (a) `..port.in..` UseCase 인터페이스 의존은 그것이 `BatchApp`을 달고 있어야 하고, (b) 마커가 없는 Command record는 `AppOwnership#derive`로 소속을 유도해 `BatchApp`인지 보며, (c) 구체 서비스 의존은 기존 `schedulersShouldDependOnUseCasesOnly`가 맡았습니다. 짝 규칙은 `AppIsolationTest#appsShouldNotDependOnEachOther`(application 계층끼리의 수평 의존)였습니다.

### backend/AGENTS.md — Module Dependency Graph / 계층 규칙 봉인

- 그래프는 "4개 api 모듈이 같은 application 모듈을 의존하므로, '자기 앱의 UseCase만 주입'은 빌드가 아니라 각 모듈 `LayerRulesTest`의 `adaptersShouldOnlyUseOwnAppUseCases`가 강제한다(챕터 01 신설)"고 적었습니다.
- "인바운드 어댑터의 앱 격리 — 컴파일 게이트가 지킨다" 절은 인바운드 포트(UseCase 인터페이스)가 자기 앱 마커를 직접 달고 있어야 하고, Command record 등은 소속 앱을 `AppOwnership`으로 유도해 자기 앱 마커인지 보며, 각 앱은 자기 앱 마커가 붙은 application 슬라이스만 의존한다고 적었습니다.

### backend/domain/AGENTS.md · backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md — 빈 등록

- domain에 남은 순수 계산기·정책 7개를 등록하는 `SharedBeanConfig`는 이 결정 전에는 `@SharedApp`이었고, application으로 옮겨 간 포트 주입 서비스는 클래스에 앱 마커만 달아 스캔으로 등록됐습니다.

### backend/infrastructure/{mail,sms,pg}/AGENTS.md

- `MailVerificationService`·`SmsVerificationService`·`PgPaymentGatewayRouter`는 클래스에 `@WebApp` 마커만 달고 web-api의 마커 스캔으로 등록됐습니다. `MailSenderPort` 등을 받는 마커 클래스가 `@WebApp`이 아니면 `AppIsolationTest#appRestrictedPortDependentsShouldBelongToThatApp`이 빌드를 실패시켰습니다.
- `pg/AGENTS.md`의 "포트 2단 구조"는 `PgPaymentGatewayPort`·`PgProviderGatewayPort`를 둘 다 `application` 소유로 적었습니다(지금은 `web-application`). `javamail`·`solapi`의 Dependencies는 포트 소유 모듈로 `:application-core`를 의존했고, 이 결정으로 `:web-application`으로 바뀌었습니다.

### CLAUDE.md — 리포지토리 구조

- "application 계층 1개: `application` — 4개 앱의 유스케이스를 담는 단일 모듈"이라고 적었습니다.

## 결정

앱 마커 5종을 전부 삭제하고 앱 소속을 Gradle 모듈 경계로 옮깁니다. application은 공유 코어(`:application`, 지금의 `:application-core`)와 `{web,admin,ceo,batch}-application` 4모듈로 나뉩니다. 자바 패키지 `com.tastyhouse.application`은 5모듈이 나눠 씁니다(split package). 각 앱은 코어와 자기 앱 모듈만 의존하므로, 다른 앱 코드를 쓰면 **컴파일 에러**가 납니다.

| 항목 | before (앱 마커) | after (모듈 경계) |
|---|---|---|
| application 모듈 | `application` 1개 | 코어 + `{web,admin,ceo,batch}-application` 4개 |
| 앱 소속 표현 | 클래스의 마커 애노테이션 | 클래스가 들어 있는 Gradle 모듈 |
| 앱의 `implementation` | `:application` | `:application` + `:{앱}-application` 1줄 추가 |
| 벤더 `{kakao,naver,apple,facebook}-oauth`·`tosspayments`·`javamail`·`solapi`·`aws-ses`·`aws-sns` | `:application` | `:web-application`(구현하는 SPI 포트가 그리로 이동) |
| 벤더 `bbq`·`admdongkor` | `:application` | `:batch-application` |
| persistence·`firebase`·`aws-s3`·`security-module`·`api-common-module` | `:application` | 변경 없음 |
| 스캔 설정 | `useDefaultFilters = false` + 마커 include 필터 | `@ComponentScan(basePackages = "com.tastyhouse.application")` 기본 필터 |
| 무엇이 뜨는가를 정하는 것 | 마커 | 앱의 클래스패스(코어 + 자기 앱 모듈) — "클래스패스 존재 = 활성화" |
| 도메인 서비스 78개 | 마커만, `@Service` 없음 | `@Service` 부착. 소비 앱이 하나면 그 앱 모듈, 둘 이상이면 코어 |
| 오케스트레이터와 도메인 서비스 구분 | 마커만 = 도메인 서비스, `@Service` + 마커 = 오케스트레이터 | `port.in` UseCase를 구현하면 오케스트레이터, 아니면 도메인 서비스 |
| 리스너·`@Configuration` | `@SharedApp` | 마커 없이 코어에만 둔다 |
| 앱 전용 SPI 포트(메일·SMS·PG·소셜 로그인·BBQ·행정동 경계·답글 문구 검수) | `application` | 소비 앱 모듈 |
| 모듈 수 | 32 | 36(include 31줄 → 35줄 + 암묵 `infrastructure` 컨테이너), bootJar 4개 이름·경로 불변 |

가드도 바꿨습니다.

| 대상 | before | after |
|---|---|---|
| 앱 간 수평 의존·앱 전용 포트 소비·Command 소속 | `AppIsolationTest`, testFixtures `AppOwnership`·`DESERIALIZED_COMMANDS`, api 4모듈 `adaptersShouldOnlyUseOwnAppUseCases` | 삭제 — 컴파일 게이트로 대체 |
| 리스너·마커 위치 | `listenersShouldBeShared`·`sharedAppOnlyOnListeners`·`markerOnlyClassesShouldBeDomainServices`·`sharedConfigsShouldOnlyDeclareUnmarkedBeans` | 삭제. 신설 `listenersAndConfigsShouldResideInCore` |
| 코어에 UseCase가 새는 것 | 없음 | 신설 `coreShouldNotContainUseCasesOrOrchestrators` |
| 빈이 다른 앱에만 있는 구현을 주입 | `constructorDependenciesShouldBeVisibleToApp` | 신설 `coreBeansShouldOnlyDependOnCoreVisibleTypes` |
| `@Configuration`의 이중 등록 | `sharedConfigsShouldOnlyDeclareUnmarkedBeans` | 신설 `configurationsShouldNotRegisterStereotypedClasses` |
| 같은 FQCN이 두 모듈에 생기는 것 | 없음 | 신설 `SplitPackageUniquenessTest` |
| 다른 앱 모듈의 클래스패스 유입 | 마커 필터가 막음 | 신설 4앱 `ApplicationModuleClasspathTest` |
| `commandRecordsShouldBeBoundaryTyped`의 batch 예외 | `.areNotAnnotatedWith(BatchApp.class)` | 클래스의 출처 모듈(`batch-application`)로 판정 |
| `BatchSchedulerRulesTest` 대상 | `@BatchApp` 클래스 | `batch-application` 출처 클래스 |
| 개수 anchor | `markerBeanCounts`·`markerUseCaseCounts` | `moduleBeanCounts`(web ≥83 · admin ≥65 · ceo ≥122 · batch ≥15 · core ≥47)·`moduleUseCaseCounts`(web ≥50 · admin ≥100 · ceo ≥95 · batch =7 · core =0 — 이 결정 시점 값) |
| 스캔 형태 | `assertScansOnlyOwnAppAndSharedMarkers` | `assertScansApplicationLayerWithoutFilters` |

`ApplicationModuleClasspathTest`는 처음에 각 `{앱}-application`이 jar에 싣는 `META-INF/tastyhouse/application-module.properties`(`app=web` 등) 표식의 개수·값으로 판정했습니다. 후속 `406c34ebe`는 이 표식 4개를 삭제하고, 앱 테스트 클래스패스의 `com.tastyhouse.application` 클래스의 출처 모듈 집합이 정확히 `{application, 자기 앱 모듈}`인지를 testFixtures `ModuleOrigin`으로 판정하게 바꿨습니다.

## 근거

- **컴파일러가 지키게 됩니다.** 마커는 컴파일러가 모르는 약속이라 ArchUnit 규칙 여러 개로 구멍을 메웠습니다. 모듈 경계에서는 다른 앱 모듈이 클래스패스에 없으므로 규칙이 아니라 컴파일러가 지킵니다.
- **챕터 01 통합의 전제가 사라졌습니다.** 챕터 01은 "persistence가 4개 앱 모듈 전부를 의존하므로, 모듈이 나뉘어 있어도 persistence를 거쳐 서로가 보여 컴파일 게이트가 실제로는 없다"는 근거로 4모듈을 합쳤습니다. 지금 persistence가 application에서 import하는 것은 `..port.out..`뿐이고 `port.out`은 전부 코어에 있으므로, persistence는 코어 하나만 의존합니다. 따라서 앱 모듈끼리는 어떤 경로로도 서로를 보지 못하고 컴파일 게이트가 진짜로 동작합니다. 모듈명은 같아도 내용은 다릅니다. 과거 4모듈은 읽기 계약까지 나눠 가졌지만, 지금 4모듈은 계약을 코어에 두고 앱 전용 빈만 갖습니다.
- **`@Service`를 달 수 있게 된 이유**: 마커가 없어지면 "스테레오타입 없는 클래스"라는 `ServiceContextBoundaryTest`의 술어를 쓸 수 없습니다. 그래서 술어를 "구조 조건 − 협력 빈 FQN 목록 `EXCLUDED_COLLABORATORS`"로 바꿔 대상 집합(94개)을 이전과 같게 유지했습니다.
- **중첩 스캔 클래스 형태는 유지합니다.** `@SpringBootApplication` 클래스에 `@ComponentScan`을 직접 달면 메타 `@ComponentScan`이 무시되는 Spring 6.1 함정은 그대로이기 때문입니다.
- **표식을 지운 이유(`406c34ebe`)**: 테스트 전용 표식이 운영 jar에 실리는 것을 없애기 위해서입니다.

## 결과·감수한 손실

- 동작 변경은 없습니다. 4앱 jar를 띄워 싱글턴 빈 이름 집합을 비교한 결과 차이가 0이었습니다(web 1131 · admin 1079 · ceo 1155 · batch 791).
- 새 실수 지점이 생겼습니다. 다른 앱 모듈을 의존에 추가하면 그 앱의 빈이 전부 뜹니다("클래스패스 존재 = 활성화"). 각 앱의 `ApplicationModuleClasspathTest`가 이를 막습니다.
- `ServiceContextBoundaryTest`의 술어는 솔직히 "구조 + 이름 목록"입니다. 새 협력 빈을 만들면 `EXCLUDED_COLLABORATORS`에 넣을지 사람이 판단해야 합니다. 짝 테스트 `excludedCollaboratorsShouldNotBeStale`은 더 이상 존재하지 않거나 구조 조건에 맞지 않게 된 항목만 잡고, "빼도 경계 규칙을 통과할 항목"은 잡지 못합니다.
- `coreBeansShouldOnlyDependOnCoreVisibleTypes`는 `@Primary`/`@Qualifier`를 모델링하지 않습니다. infrastructure가 구현하는 포트(후보 0인 의존)는 건너뜁니다.
- 마커 스캔 시절에는 마커 없는 `@Service`가 컴파일은 통과하고 기동 시점 `NoSuchBeanDefinitionException`으로만 드러나 "이 모듈에서 가장 흔한 실수 지점"이었습니다(`beansShouldHaveExactlyOneAppMarker`·`useCasesShouldHaveExactlyOneAppMarker`가 빌드에서 잡았습니다). 지금은 `@Service`가 없으면 안 뜨는 일반 Spring과 같습니다.
- `sharedBeansShouldNotDependOnWebOnlyServices`도 `AppIsolationTest`와 함께 삭제됐습니다. 그 4개 타입이 `web-application`에 있어 코어 빈은 컴파일 단계에서 볼 수 없기 때문입니다.
- 공용 테스트 더블 20개를 코어 testFixtures `com.tastyhouse.testsupport`로 옮기며 package-private이던 9개를 public으로 바꿨습니다. import 4순위에 `testsupport`가 추가됐습니다.
- [ADR-0034](0034-service-config-removal.md)의 "config 삭제"와 "`SharedBeanConfig`에는 애노테이션을 달 수 없는 빈만 둔다"는 유지됐습니다. "소비 앱 집합으로 정한다"는 판단은 마커 선택 기준에서 모듈 선택 기준으로 이어졌습니다.

## 영향받은 문서

- `CLAUDE.md` · `리포지토리 구조` · application 계층은 코어 + 앱 모듈 4개이고, 앱 소속은 Gradle 모듈이 표현한다
- `backend/CLAUDE.md` · `앱 모듈 경계 규칙 (앱 마커 제거 — 앱 소속은 Gradle 모듈이 표현한다)` (하위 `무엇이 어디에 사는가` · `실행 앱·벤더의 의존` · `앱 모듈을 나눠도 컴파일 게이트가 동작하는 이유` · `스캔 설정` · `새 클래스를 추가할 때 (어느 모듈에 두나)` · `가드`) · 현재 규칙의 정본
- `backend/CLAUDE.md` · `앱 마커 규칙 (챕터 03 — 스캔이 패키지에서 애노테이션으로)` · 폐기 표기만 남기고 이 ADR로 연결
- `backend/CLAUDE.md` · `모듈 지도 (모듈 재편 완료 + application 모듈 통합 + external 분리 + 앱 모듈 재분리)` / `패키지 최상위 지도` / `앱별 의존 (전환 후 — \`runtimeOnly\`로 하향)` · 모듈 수, split package, 앱별 `implementation` 열
- `backend/CLAUDE.md` · `application 서비스 CQRS 분리 규칙 (\`{도메인}CommandService\`/\`{도메인}QueryService\`)` · 오케스트레이터 판정 기준
- `backend/application-core/AGENTS.md` · `앱 모듈 재분리 — 앱 소속은 Gradle 모듈이 표현한다` · 마커를 되살리지 않는다
- `backend/application-core/AGENTS.md` · `앱 축 통합 (챕터 01) — 폐기됨` / `패키지 평탄화 (챕터 03)` · 대체된 두 결정의 현행 자리
- `backend/application-core/AGENTS.md` · `빈 배선 — 앱 부트스트랩의 중첩 스캔 설정` · 필터 없는 스캔과 중첩 클래스 형태
- `backend/application-core/AGENTS.md` · `앱 모듈 경계 가드 — 마커를 되살리지 않고, 다른 앱 모듈을 클래스패스에 올리지 않는다 (앱 마커 제거)` · 신설 가드 목록
- `backend/application-core/AGENTS.md` · `application \`*ServiceConfig\`를 두지 않는다 — 도메인 서비스는 \`@Service\`로 스캔 등록` / `도메인 서비스를 둘 모듈은 소비 앱 집합으로 정한다 — 공유 커널 단방향` / `리스너를 4앱 전부가 스캔하는 코어 \`application-core\`에 두는 이유 (개별 사유)` · `@Service` 부착과 모듈 선택 기준, 리스너는 코어
- `backend/application-core/AGENTS.md` · `패키지 구조 (챕터 03으로 평탄화 — 도메인 아래에 앱별 폴더가 없다)` · 5모듈을 합친 패키지 모양
- `backend/web-api/AGENTS.md`, `backend/admin-api/AGENTS.md`, `backend/ceo-api/AGENTS.md` · "Purpose" / "Dependencies" > "Internal" / "`ApplicationModuleClasspathTest` — 다른 앱의 application 모듈이 클래스패스에 섞이지 않게 막는다 (앱 마커 제거)" / "`ApplicationLayerScanConfigTest` — 부트스트랩 스캔 설정의 오기입·직접 선언을 막는다" · 앱이 자기 앱 모듈만 의존한다
- `backend/batch-module/AGENTS.md` · `패키지 구조` / `규칙` / `Internal` / `\`ApplicationModuleClasspathTest\` — 다른 앱의 application 모듈이 클래스패스에 섞이지 않게 막는다 (앱 마커 제거)` · 같은 취지
- `backend/infrastructure/mail/AGENTS.md` · `도메인 서비스 등록은 어디인가 — \`web-application\`의 \`MailVerificationService\`` · 포트가 `web-application`에 있어 다른 앱은 컴파일 단계에서 볼 수 없다
- `backend/infrastructure/sms/AGENTS.md` · `도메인 서비스 등록은 어디인가 — \`web-application\`의 \`SmsVerificationService\`` · 같은 취지
- `backend/infrastructure/pg/AGENTS.md` · `라우터 등록은 어디인가 — \`web-application\`의 \`PgPaymentGatewayRouter\`` / `포트 2단 구조 — 벤더는 공존하고 앱은 하나만 주입한다` · 라우터와 포트 소유 모듈
- `backend/infrastructure/jpa/AGENTS.md` · `규칙` / `컨텍스트 경계를 건너는 것은 얇은 포트 어댑터가 흡수한다` · jpa는 코어만 의존한다
- `backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md` · `Common Patterns` · 리스너는 코어 `application`의 `@Component`

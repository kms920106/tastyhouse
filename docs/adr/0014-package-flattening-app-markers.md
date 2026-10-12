# ADR-0014: application 패키지를 평탄화하고 앱 소속을 마커 애노테이션으로 표현한다(챕터 03)

- 상태: 대체됨(→ [ADR-0036](0036-app-marker-removal.md))
- 결정 커밋: `9b888f1c5` (2026-09-04)
- 대체한 결정: —

## 맥락

[ADR-0013](0013-application-module-merge.md)(챕터 01) 직후에는 Gradle 모듈만 합쳐졌고, 앱별 자바 패키지 `com.tastyhouse.{web|admin|ceo|batch}application` 4개는 그대로였다. 빈 스캔도 이 패키지를 기준으로 했다.

- **마커 애노테이션**: 동작 없이 "이 클래스는 X에 속한다"는 표시만 하는 애노테이션.
- **컴포넌트 스캔 include 필터**: 특정 애노테이션이 붙은 클래스만 빈으로 등록하게 하는 스캔 설정.

## 이전 규칙

앱 소속은 **패키지**가 표현했다. `NoticeQueryService`(당시 이름)가 `com.tastyhouse.adminapplication.notice.service`에 있으면 admin 것이었다. 각 앱은 자기 앱 패키지를 스캔했다. ArchUnit 슬라이스 규칙·import 정렬 규칙은 "접두어가 겹치는 4개 패키지"를 특별 취급했다.

`<ctx>/port/out`은 평탄화 이전에는 읽기 계약(QueryPort·Result·SearchCondition)만의 자리였다.

아래는 이 ADR이 세운 마커 체제가 앱 마커 제거([ADR-0036](0036-app-marker-removal.md)) 전까지 쌓은 규칙의 요지다. 규칙 문서에서 지워졌으므로 여기에 남긴다.

### `backend/application-core/AGENTS.md` — 챕터 03 — 패키지 평탄화 + 앱 마커 애노테이션

- **파일 이동 2건**: `batchapplication/exception/BatchJobException` → `application/shared/exception/`, `batchapplication/crawling/bbq/response/*.java` 4개(`BbqProductResponse`·`BbqProductCategoryResponse`·`BbqProductSubOptionResponse`·`SubOptionItemDetailResponse`) → `application/crawling/bbq/port/out/`.
- 이 시점 `port.out`에 함께 산 아웃바운드 SPI는 `SocialOAuthClientPort`·`BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort`였다.
- **잃어버린 컴파일 게이트의 대체**: 모듈이 하나가 되어 앱 간 수평 의존을 빌드가 막지 못하게 되자, 같은 커밋에 ArchUnit 규칙 두 개를 세웠다(나중에 넣으면 그 사이에 들어온 교차 의존이 정상으로 굳는다는 이유).

  | 잃은 게이트 | 대체 규칙 | 위치 |
  |---|---|---|
  | application → 다른 앱 application | `AppIsolationTest#appsShouldNotDependOnEachOther` | `application` |
  | api 어댑터 → 다른 앱 application | `adaptersShouldOnlyUseOwnAppUseCases` | api 4모듈 각각 |

  앱 마커 제거로 앱 모듈이 다시 나뉘어 컴파일 게이트 자체가 돌아오면서 두 규칙은 삭제됐다.

### `backend/CLAUDE.md` — 앱 마커 규칙 (챕터 03 — 스캔이 패키지에서 애노테이션으로)

- 마커에 `@Component` 메타를 얹지 않은 이유: 얹으면 기존 `@Service`/`@Component`의 의미가 흐려진다. 형태는 `@Service` 바로 옆에 마커를 병기하는 두 줄이었다.
- **Command record 소속 유도**: 300여 개 record에 손으로 마커를 다는 것은 누락이 확실하다는 판단으로 `AppOwnership`(`application-core/src/testFixtures/java/com/tastyhouse/architecture/AppOwnership.java`)이 `apps(R) = R을 시그니처에 쓰는 마커 UseCase의 마커 집합 ∪ R을 컴포넌트로 품는 record의 apps`(전이 폐쇄, 고정점까지)로 계산했다. `AppOwnership.DESERIALIZED_COMMANDS`에는 런타임 역직렬화로만 생성되는 경우만 담고, 새 항목을 넣기 전에 그 record를 실제로 어디서 만드는지부터 확인한다고 했다. `AppOwnership`은 `java-test-fixtures` 플러그인으로 api 4모듈이 `testImplementation(testFixtures(project(':application-core')))`로 재사용했다 — `adaptersShouldOnlyUseOwnAppUseCases`도 같은 유도가 필요해서였다.
- **ArchUnit 4규칙(`AppIsolationTest`)**: `appsShouldNotDependOnEachOther`(마커 5종의 5×4=20조합 금지 — `SharedApp`이 `AppOwnership.MARKERS`에 들어가 공유 리스너 ↔ 앱 전용 빈 의존도 양방향 금지), `beansShouldHaveExactlyOneAppMarker`, `useCasesShouldHaveExactlyOneAppMarker`, `commandRecordsShouldBelongToExactlyOneApp`. 마커별 개수 하한 `markerBeanCounts`(`@SharedApp` 빈 ≥ 12 포함)·`markerUseCaseCounts`가 앱별 anchor를 승계했다.
- 마커 술어는 다른 규칙에도 번졌다 — `commandRecordsShouldBeBoundaryTyped`의 batch 예외는 `.areNotAnnotatedWith(BatchApp.class)`로, api 4모듈의 `adaptersShouldOnlyUseOwnAppUseCases`는 마커 + `AppOwnership` 유도 술어로 판정했다. `commandServicesShouldNotDependOnQueryPorts`를 이름 기준으로 바꾼 직접 원인은 Command 반환 record를 import하는 CommandService 7개였다.
- **`@SharedApp`(5번째 마커)**: 도메인 이벤트 리스너 12종(`com.tastyhouse.application.<ctx>.listener`, `@Component @SharedApp`)을 `infrastructure:persistence`에서 옮기며 신설했다. 이벤트는 어느 앱이 발행하든 후속 처리가 누락되면 안 되므로 4앱 전부가 스캔해야 한다. `LayerRulesTest#sharedAppOnlyOnListeners`(`@SharedApp`은 `..listener..`의 `@TransactionalEventListener` 보유 클래스에만)와 `#listenersShouldBeShared`가 양방향으로 강제했다 — 마커를 빠뜨린 AFTER_COMMIT 리스너는 예외도 로그도 없이 사라지기 때문이다.
- **`@SharedApp` 허용 확대 1 (표현 계층 domain 절단 덩어리 01)**: 리스너 ∨ `..config..`의 `@Configuration`으로 넓혔다. 마커 없는 POJO를 `@SharedApp @Configuration`의 `@Bean`으로만 등록하게 하고, 짝 규칙 `LayerRulesTest#sharedConfigsShouldOnlyDeclareUnmarkedBeans`(설정은 `@Component`/`@Service`를 겸하지 않고, `@Bean` 반환 타입과 그 설정이 생성자를 호출하는 클래스 전부가 앱 마커를 갖지 않는다)를 신설했다. 당시 `@SharedApp` 설정이 0개라 `should()`로 쓰면 failOnEmptyShould에 걸리고 `allowEmptyShould(true)`는 금지라서, 위반을 손으로 모으는 테스트로 썼다. 이후 03b의 Store 빈만 갖던 `@SharedApp` 설정 6개(`Admin`·`Banner`·`Event`·`Notice`·`Partnership`·`Region`의 `*ServiceConfig`)는 persistence domain 재허용으로 Store가 삭제되며 파일째 사라졌다.
- **`@SharedApp` 허용 확대 2 (application `*ServiceConfig` 전면 삭제 — [ADR-0034](0034-service-config-removal.md))**: `<Ctx>ServiceConfig` 22개와 `PgRouterConfig`(23개 config, `@Bean` 88개)를 삭제하고, 도메인 서비스 78개가 `@Service` 없이 클래스에 앱 마커만 달게 했다(분포 `@WebApp` 17 · `@AdminApp` 3 · `@CeoApp` 21 · `@BatchApp` 2 · `@SharedApp` 35). 컨벤션은 "마커만 = 도메인 서비스, `@Service` + 마커 = 앱 오케스트레이터"였다 — `ServiceContextBoundaryTest`가 스테레오타입 붙은 클래스를 검사에서 빼기 때문이다. 잔류 config는 `SharedBeanConfig`(구 `SharedEventConfig`, `@SharedApp`)의 빈 10개였다. 함께 신설·변경된 규칙:
  - `sharedAppOnlyOnListeners`가 `..service..`의 스테레오타입 없는 도메인 서비스까지 허용(규칙명 유지).
  - `LayerRulesTest#markerOnlyClassesShouldBeDomainServices`(하한 78개).
  - `appsShouldNotDependOnEachOther`가 앱 마커 → `..service..`의 `@SharedApp` 단방향 의존을 허용(공유 커널).
  - `AppIsolationTest#constructorDependenciesShouldBeVisibleToApp`(생성자 파라미터 타입이 그 앱에서 보이는 빈인지 — 검사 의존 수 하한 700, 이후 `@Bean` 메서드 파라미터(하한 ≥ 3)·주입 후보 2개 이상 모호·데코레이터 제외로 보강).
  - `AppIsolationTest#appRestrictedPortDependentsShouldBelongToThatApp` — web 전용 `MailSenderPort`·`SmsSenderPort`·`PgProviderGatewayPort`·`SocialOAuthClientPort`는 `@WebApp`, batch 전용 `BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort`는 `@BatchApp`만 받는다(그 전에는 이름 목록 4개 `sharedBeansShouldNotDependOnWebOnlyServices`뿐).
  - `markerBeanCounts` 하한 `@WebApp` ≥83 · `@AdminApp` ≥65 · `@CeoApp` ≥122 · `@BatchApp` ≥15 · `@SharedApp` ≥47.
  - 한 앱 전용 빈이 4앱 전부에 뜨던 것이 소비 앱에서만 뜨게 바뀌었다(빈 이름 불변, 기능·HTTP·DB 차이 없음).
  - 반증 probe 7종으로 각 규칙의 실패를 확인했다. 남은 사각지대는 마커 오배정의 최종 확인이 4앱 `java -jar` 기동뿐이라는 점과, `List<I>` 주입은 구현체 0개여도 기동돼 규칙이 못 잡는다는 점이었다.
- **스캔 위치**: 마커 include 필터 `@ComponentScan`은 처음에 `application` 모듈의 `{Web,Admin,Ceo,Batch}ApplicationConfig`에 있었고 api 4모듈이 `@Import`했다. 이후 각 앱 부트스트랩의 static 중첩 `ApplicationLayerScanConfig`로 옮겨졌고([ADR-0035](0035-application-config-into-bootstrap.md)), `@SharedApp` 추가 후 필터는 `classes = {XxxApp.class, SharedApp.class}`였다.

## 결정

| 항목 | before | after |
|---|---|---|
| 자바 패키지 | `com.tastyhouse.{web\|admin\|ceo\|batch}application` 4개 | `com.tastyhouse.application` 하나 |
| 앱 소속 표현 | 패키지 | 마커 4종 `com.tastyhouse.application.shared.marker.{WebApp,AdminApp,CeoApp,BatchApp}`(`@Component` 메타 없음, `@Target(TYPE)`·`@Retention(RUNTIME)`·`@Documented`) |
| 마커 부착 대상 | — | 빈 242개(`@Service` 220 + `@Component` 22)와 UseCase 인터페이스 257개에 정확히 하나씩 |
| Command record 소속 | 패키지 | 마커 없이 유도 — testFixtures `AppOwnership`이 시그니처·컴포넌트 전이 폐쇄로 계산(0개=고아, 2개 이상=경계 위반). carve-out 1건 `ShopStorePriceVerificationItemCommand`(`DESERIALIZED_COMMANDS`) |
| 빈 스캔 | 앱 패키지 | `@ComponentScan(basePackages = "com.tastyhouse.application", useDefaultFilters = false, includeFilters = @Filter(type = ANNOTATION, classes = XxxApp.class))` |
| 앱 격리 가드 | 패키지·슬라이스 술어 | 마커 술어(`AppIsolationTest` 4규칙, api 4모듈의 `adaptersShouldOnlyUseOwnAppUseCases`) |
| `<ctx>/port/out`의 의미 | 읽기 계약만 | 이 도메인의 모든 아웃바운드 계약(읽기 계약 + 아웃바운드 SPI + 명령 서비스가 반환하는 Result/View record) |

후속으로 5번째 마커 `@SharedApp`(리스너 전용, "4앱 전부에 뜬다")이 추가됐다.

## 근거

- 챕터 01의 "중복이 컸고 이득이 없었다"가 패키지 수준에서도 반복됐다 — 앱별 패키지가 남아 있는 한 ArchUnit·import 정렬 규칙이 4개 패키지를 특별 취급해야 했고, 그 특별 취급이 문서·규칙의 복잡도였다.
- 평탄화로 잃는 "패키지가 말해 주던 앱 소속"은 마커가 대신한다.
- `commandServicesShouldNotDependOnQueryPorts`와 api의 `controllersShouldNotDependOnQueryPorts`는 패키지 술어 → 이름 기준(`QueryPort`/`QueryService` 접미어)으로 바뀌었다 — `port.out`에 Command 반환 record가 함께 살게 되어 패키지 술어로는 정당한 반환 타입까지 잡혔기 때문이다.

## 결과·감수한 손실

- `useDefaultFilters = false`라 마커 없는 `@Service`는 컴파일은 통과하지만 어느 앱에도 뜨지 않는다 — 그 빈이 처음 필요해지는 기동 시점에야 `NoSuchBeanDefinitionException`으로 드러난다.
- jar 이름·경로는 불변이었다.
- **대체**: 앱 마커 제거([ADR-0036](0036-app-marker-removal.md))로 마커 5종·`AppOwnership`·`AppIsolationTest`·`adaptersShouldOnlyUseOwnAppUseCases`·마커 include 필터 스캔이 전부 삭제됐고, 앱 소속은 Gradle 모듈이 표현한다. **패키지 평탄화(`com.tastyhouse.application` 하나)는 유지**되며, 지금은 코어와 앱 모듈 4개가 나눠 쓰는 split package다.
- 지금도 유효한 것: 스캔 설정을 앱 부트스트랩의 static 중첩 클래스로 두는 이유(Spring 6.1 `@ComponentScan` 함정)와 "`<ctx>/port/out`은 모든 아웃바운드 계약"(단 앱 전용 SPI 포트는 앱 모듈로 갔다).

## 영향받은 문서

- `backend/application-core/AGENTS.md` · "패키지 평탄화 (챕터 03)" · 평탄화는 유지, 마커는 삭제
- `backend/application-core/AGENTS.md` · "패키지 구조 (챕터 03으로 평탄화 — 도메인 아래에 앱별 폴더가 없다)" · 현재 패키지 구조
- `backend/application-core/AGENTS.md` · "빈 배선 — 앱 부트스트랩의 중첩 스캔 설정" · 현재 스캔 방식
- `backend/CLAUDE.md` · "앱 마커 규칙 (챕터 03 — 스캔이 패키지에서 애노테이션으로)" · 마커 규칙이 폐기됐다는 포인터
- `backend/CLAUDE.md` · "앱 모듈 경계 규칙 (앱 마커 제거 — 앱 소속은 Gradle 모듈이 표현한다)" > "`<ctx>/port/out`에 사는 것" · "스캔 설정" · 현재 규칙

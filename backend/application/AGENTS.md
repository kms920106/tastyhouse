# application

**4개 앱(web · admin · ceo · batch)의 application 계층을 담는 단일 모듈.** 자바 패키지는 `com.tastyhouse.application` 하나로 평탄화돼 있다(챕터 03) — 구조는 `com.tastyhouse.application.<도메인>.{port.in, port.out, service}`이고 도메인 아래에 앱별 폴더가 없다. 컨텍스트별 인바운드 포트(`<ctx>/port/in/`)와 그 구현인 `*CommandService`/`*QueryService`(batch는 `*SchedulerService`), 그리고 읽기 계약 326개가 이 한 패키지 트리 안에 함께 있다. **앱 소속은 패키지가 아니라 마커 애노테이션**(`@WebApp`/`@AdminApp`/`@CeoApp`/`@BatchApp`)이 표현한다 — 상세는 아래 [챕터 03 — 패키지 평탄화 + 앱 마커](#챕터-03--패키지-평탄화--앱-마커-애노테이션-과거-판단의-번복).

컨트롤러(`<ctx>/adapter/in/web/`)·`request/`·`response/`·config·security 정책·전역 예외 핸들러와 부트스트랩은 각 api 모듈(`web-api`·`admin-api`·`ceo-api`·`batch-module`)에 남아 있다.

## 과거 판단의 번복 — 앱 축을 접은 이유 (챕터 01)

챕터 01~04(각각 batch·web·admin·ceo)로 앱마다 `{app}-application` 모듈을 하나씩 세웠던 것을, **이 챕터가 되돌려 하나로 합쳤다.** 앱 축 분리가 값을 못 했다는 판단이며 근거는 셋이다.

- **컴파일 게이트가 사실상 없었다.** `infrastructure/persistence/build.gradle`이 4개 application 모듈을 전부 `implementation`으로 의존하므로 **모든 실행 jar에 4개 jar가 이미 들어 있었다**(admin-api fat jar `BOOT-INF/lib/` 실측). 앱 분할이 실제로 준 게이트는 application → application 한 방향뿐이었고, 그것은 ArchUnit이 패키지로 이미 막고 있었다.
- **소유권 연쇄가 부채를 낳았다.** 읽기 계약을 앱 모듈이 소유하게 하면서 "한 앱이 소유하면 다른 앱이 그 모듈을 의존해야 한다"를 피하려고, 공유 계약을 `domain-module`로 올리고 `application-common-module`을 해체했다(챕터 05·07·09). 그 결과가 split package 5모듈과 가드 3종(`ReadContractSingleOwnerTest`·`ReadContractPurityTest`·`RuleAnchorTest`의 소유 모듈 필터)이다. **챕터 04에서 그 55개를 이 모듈로 되돌려 이 부채가 통째로 사라졌다.**
- **중복이 컸고 이득이 없었다.** web·admin·ceo의 `LayerRulesTest`는 규칙 16종이 이름·본문까지 동일했다(diff는 carve-out 이름과 `because` 문구뿐). `gradle.properties`가 비어 있어 병렬 빌드 이득도 없었다.

**이 챕터의 범위는 Gradle 모듈만 4 → 1이다.** 자바 패키지는 그대로였다(`com.tastyhouse.{app}application` + `com.tastyhouse.application.<ctx>.port.out`). 뒤 챕터에서 동명 클래스 182건 개명(02) → **패키지 평탄화 + 앱 마커 애노테이션(03, 완료 — 아래 절)** → 공유 읽기 계약 55개 복귀(04, 완료)가 이어진다.

## 챕터 03 — 패키지 평탄화 + 앱 마커 애노테이션 (과거 판단의 번복)

**챕터 01 직후에는 Gradle 모듈만 합쳐졌고 앱별 패키지(`com.tastyhouse.{web|admin|ceo|batch}application`)는 그대로 남아 있었다. 이 챕터가 그 4개 패키지를 `com.tastyhouse.application` 하나로 평탄화했다.**

- **왜 평탄화했나**: 챕터 01의 판단 근거 중 하나였던 "중복이 컸고 이득이 없었다"가 패키지 수준에서도 반복되고 있었다 — 앱별 패키지가 남아 있는 한 `ArchUnit` 슬라이스 규칙·import 정렬 규칙 모두 "접두어가 겹치는 4개 패키지"를 특별 취급해야 했고, 그 특별 취급 자체가 문서·규칙의 복잡도였다. 패키지를 하나로 합치면 그 특별 취급이 사라진다.
- **잃는 것**: 패키지 자체가 앱 소속을 말해주던 유일한 단서가 사라진다. `NoticeQueryService`가 `com.tastyhouse.adminapplication.notice.service`에 있다는 사실만으로 "이건 admin 것"임을 알 수 있었는데, 평탄화 후에는 `com.tastyhouse.application.notice.service`가 되어 그 정보가 없다.
- **대체 수단 — 마커 애노테이션 4종**: `com.tastyhouse.application.shared.marker.{WebApp,AdminApp,CeoApp,BatchApp}`. 순수 마커(`@Component` 메타 없음, `@Target(TYPE)` + `@Retention(RUNTIME)` + `@Documented`)이며, 빈 242개(`@Service` 220 + `@Component` 22)와 UseCase 인터페이스 257개에 정확히 하나씩 붙는다. **Command record에는 붙이지 않는다** — 소속은 유도한다(아래).
- **스캔이 패키지에서 애노테이션으로 바뀌었다**: 4개 `*ApplicationConfig`가 `com.tastyhouse.application` 루트로 이동했고 `@ComponentScan(basePackages = "com.tastyhouse.application", useDefaultFilters = false, includeFilters = @Filter(type = ANNOTATION, classes = XxxApp.class))` 형태다. **`useDefaultFilters = false`이므로 마커 없는 `@Service`는 컴파일은 통과하지만 어느 앱에도 뜨지 않는다** — 그 실패는 그 빈이 처음 필요해지는 기동 시점에야 `NoSuchBeanDefinitionException`으로 드러난다. api 4모듈의 `@Import(XxxApplicationConfig.class)`는 불변이고 jar 이름·경로도 불변이다.
- **파일 이동 2건**: `batchapplication/exception/BatchJobException` → `application/shared/exception/`, `batchapplication/crawling/bbq/response/*.java` 4개(`BbqProductResponse`·`BbqProductCategoryResponse`·`BbqProductSubOptionResponse`·`SubOptionItemDetailResponse`) → `application/crawling/bbq/port/out/`.
- **`<ctx>/port/out`의 의미가 넓어졌다** — 이제 "이 도메인의 **모든 아웃바운드 계약**"이다. 읽기 계약(`QueryPort`·`Result`·`SearchCondition`) + 아웃바운드 SPI(`SocialOAuthClient`·`BbqMenuPort`·`RemoteImagePort`·`AdminDongBoundaryPort`) + **CommandService가 반환하는 Result/View record**가 함께 산다.
- **Command record는 마커 없이 유도한다**: `AppOwnership`(`application/src/testFixtures/java/com/tastyhouse/application/architecture/AppOwnership.java`)이 `apps(R) = R을 시그니처에 쓰는 마커 UseCase의 마커 집합 ∪ R을 컴포넌트로 품는 record의 apps`(전이 폐쇄)로 소속을 계산한다. 0개=고아(죽은 코드), 2개 이상=앱 간 공유(경계 위반) 둘 다 위반. **carve-out 1건**: `ShopStorePriceVerificationItemCommand`는 multipart 문자열 파트를 서비스가 `ObjectMapper`로 역직렬화해 만들어 정적 참조가 없으므로 `AppOwnership.DESERIALIZED_COMMANDS`에 소속(`CeoApp`)을 명시했다 — 유도가 닿을 수 없는 정상 형태다.
- **`AppOwnership`은 `testFixtures`에 있고 api 4모듈이 재사용한다**(`java-test-fixtures` 플러그인, `testImplementation(testFixtures(project(':application')))`) — api 모듈의 `adaptersShouldOnlyUseOwnAppUseCases`도 같은 유도가 필요하기 때문이다.
- **ArchUnit 규칙 전환**: `commandServicesShouldNotDependOnQueryDaos`가 패키지 술어 → **이름 기준**(`haveSimpleNameEndingWith("QueryPort")` / `"QueryService"`)으로 바뀌었다 — `port.out`에 Command 반환 record가 함께 살게 되어, 패키지 술어를 두면 그 record를 import하는 CommandService 7개가 정당한 반환 타입인데도 위반으로 잡히기 때문이다. 같은 이유로 api 3모듈의 `controllersShouldNotDependOnQueryDaos`도 이름 기준이다. `AppIsolationTest`는 슬라이스/패키지 술어에서 **마커 술어**로 전면 재작성됐다(아래 [ArchUnit — 4클래스](#archunit--4클래스-챕터-03으로-importer판별-기준이-패키지에서-마커로-전환) 절 반영). 상세 규칙 목록·근거는 루트 `backend/CLAUDE.md`의 "앱 마커 규칙" 절 참고.

### 잃어버린 컴파일 게이트를 무엇이 대체했나

모듈이 하나가 되면서 **앱 간 수평 의존을 빌드가 막지 못하게 됐다.** 이 챕터는 그 자리에 ArchUnit 규칙 두 개를 같은 커밋에 세웠다 — 나중에 넣으면 그 사이에 들어온 교차 의존이 정상으로 굳는다.

| 잃은 게이트 | 대체 규칙 | 위치 |
|---|---|---|
| application → 다른 앱 application | `AppIsolationTest#appsShouldNotDependOnEachOther` | 이 모듈 |
| api 어댑터 → 다른 앱 application | `adaptersShouldOnlyUseOwnAppUseCases` | api 4모듈 각각 |

## 패키지 구조 (챕터 03으로 평탄화 — 도메인 아래에 앱별 폴더가 없다)

```
com.tastyhouse.application/
  ├── {App}ApplicationConfig.java   @ComponentScan 진입점(마커 기반 필터) — 쓰는 앱이 @Import 한다. 4개(Web/Admin/Ceo/Batch)
  ├── shared/marker/{WebApp,AdminApp,CeoApp,BatchApp}.java   순수 마커 애노테이션 4종 — 앱 소속의 유일한 단서
  ├── shared/exception/BatchJobException.java   (챕터 03 이동 — 과거 batchapplication/exception/)
  └── <ctx>/
      ├── port/in/                UseCase 인터페이스(마커 부착) + Command record(마커 없음 — AppOwnership 유도)
      ├── service/                *CommandService/*QueryService(batch는 *SchedulerService), 마커 부착, implements {Ctx}UseCase
      └── port/out/               이 도메인의 모든 아웃바운드 계약(챕터 03으로 의미 확장) —
                                  읽기 계약({Ctx}QueryPort·*Result·*SearchCondition, 마커 없음) +
                                  아웃바운드 SPI(SocialOAuthClient 등) + Command 경로 반환 Result/View(마커 없음)
```

패키지만 봐서는 어느 앱 것인지 알 수 없다 — 빈·UseCase는 마커 애노테이션이, Command record는 `AppOwnership`의 유도가 소속을 정한다(아래 [챕터 03](#챕터-03--패키지-평탄화--앱-마커-애노테이션-과거-판단의-번복) 참고). 컨텍스트별 규모는 앱마다 다르다.

- **web** 컨텍스트 27종: `auth` · `banner` · `bug` · `coupon` · `event` · `faq` · `follow` · `grade` · `mail` · `member` · `menureview` · `notice` · `notification` · `order` · `partnership` · `payment` · `point` · `policy` · `product` · `rank` · `referral` · `reservation` · `review` · `search` · `shop` · `sms`.
- **admin** 컨텍스트 19종: `admin` · `auth` · `banner` · `bug` · `ceo` · `coupon` · `event` · `faq` · `file` · `member` · `notice` · `order` · `partnership` · `point` · `policy` · `product` · `rank` · `review` · `shop`.
- **ceo** 컨텍스트 6종: `auth` · `ceo` · `product` · `region` · `review` · `shop`. **컨텍스트 수는 가장 적은데 서비스 수는 가장 많다**(`*CommandService` 44 · `*QueryService` 43) — 점주 셀프서비스가 `shop` 하나에 설정 관심사를 대량으로 갖기 때문이다(`ShopBusinessHour*`/`ShopClosedDay*`/`ShopStatus*`/`ShopDeliveryTip*` 등).
- **batch** 잡 슬러그 7종: `grade` · `product` · `productsoldout` · `rank` · `region` · `reviewblind` · `search`. 추가로 `crawling/bbq/`(BBQ 크롤링 동기화 + 응답 record 4종)와 `exception/BatchJobException`이 있다. 잡별 UseCase·트리거 대응표는 `batch-module/AGENTS.md`에 있다.

대형 컨텍스트는 관심사 단위로 서비스를 더 쪼갠다 — 이 관례는 모듈 통합 전과 동일하다.

### 앱 간 동명 클래스는 정상이다

**앱별로 같은 역할의 타입이 따로 존재하는 것은 의도된 중복이다** — 소비자가 다르면 조회 범위·응답 형태가 다르고, 인증은 주체(`Member`·`Admin`·`Ceo`)·ErrorCode·`JWT_SECRET_*`가 앱별로 분리돼 있다. 통합하지 않는다.

**다만 이름까지 같게 두지는 않는다(챕터 02에서 개명 완료).** 챕터 03 평탄화로 세 앱의 타입이 같은 패키지에 공존하므로 simple name이 앱 간에도 유일해야 한다. `NoticeQueryService`(web) / `NoticeManagementQueryService`(admin), `ShopQueryService`(web) / `ShopManagementQueryService`(admin) / `ShopOwnerQueryService`(ceo), `MemberTokenService` / `AdminTokenService` / `CeoTokenService`처럼 **web은 순수명, admin은 `Management`, ceo는 `Owner`**(인증 타입은 주체명 접두)로 구별한다.

공유되는 것은 `domain-module`의 도메인 모델·write 포트·도메인 서비스와, 이 모듈 안에서 여러 앱이 함께 쓰는 `{Ctx}QueryPort` 계약이다. 그 시그니처를 바꿀 때는 소비 앱 전체를 함께 확인한다.

**앱 간 타입명 충돌 시 `Management`/`Owner` 한정어**를 상시 적용한다 — `Result`·`QueryPort`뿐 아니라 `*UseCase`·`*Service`·`*Command`·협력 빈(`*Reader`·`*View`)까지가 대상이다(규칙 전문과 한정어 삽입 위치는 루트 `backend/CLAUDE.md` 참고). 동명 클래스 **182건의 일괄 개명은 챕터 02에서 완료**했다.

### 읽기 계약을 이 모듈이 소유한다

**읽기 계약(`{Ctx}QueryPort`·`*Result`·`*SearchCondition`)은 전부 이 모듈에 있다** — `src/main/java/com/tastyhouse/application/<ctx>/port/out/`이다. 통합 전 4개 모듈이 나눠 갖던 271개를 같은 트리로 합쳤고(챕터 01, 파일명 충돌 0건), 챕터 04에서 `domain-module`이 갖고 있던 다중 앱 공유 계약 55개까지 돌아왔다.

**`com.tastyhouse.application`을 이 모듈이 단독 소유한다 — split package가 끝났다.** 공유 계약 55개를 `domain-module`에 두던 시기에는 한 패키지를 두 모듈이 나눠 가졌고, 그것을 지키는 가드가 3종 필요했다(`ReadContractSingleOwnerTest`·`ReadContractPurityTest`·`RuleAnchorTest`의 소유 모듈 필터). 챕터 04로 셋 다 사라졌다 — 같은 모듈 안의 FQCN 중복은 컴파일 에러이기 때문이다. 이동은 패키지 경로가 같아 `git mv`뿐이었고 소비 측 import는 0건 바뀌었다.

- 구현은 `infrastructure:persistence`의 `<ctx>/query/` DAO다. 그 모듈이 `implementation project(':application')`으로 이 계약들을 본다.
- **새 읽기 계약은 소비 앱 수를 따지지 않고 이 모듈에 둔다.** 소비 앱이 하나든 셋이든 자리가 같다 — 소유 모듈을 판정하던 절차는 챕터 04와 함께 폐기됐다.
- **프레임워크-프리를 `LayerRulesTest#readContractsShouldBeFrameworkFree`가 지킨다**: 이 모듈은 spring starter를 받으므로 `application-common-module` 시절의 컴파일 게이트가 없다. 계약이 참조해도 되는 것은 `java..`·`com.tastyhouse.domain..`과 자기 자신뿐이다.

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
| 도메인 enum `switch` | `ShopReviewSortTypeView` — api 모듈에서 enum을 `switch`하면 바이트코드가 `ordinal()`·`values()`를 호출해 `apiModuleShouldOnlyReadDomainEnums`에 걸린다 |

**중첩 `Status` enum은 Result로 함께 복제하되 상수명을 바꾸지 않는다** — 상수 이름이 그대로 JSON 값이라 이름을 바꾸면 API가 바뀐다(`SocialLoginResult.Status`).

**`@JsonInclude`·`@JsonFormat` 같은 jackson 직렬화 어노테이션은 Response 쪽에만 둔다** — 직렬화는 api 모듈에서 일어나므로 Result로 옮기면 무의미해지고, `@JsonFormat` 소실은 날짜 포맷이 조용히 바뀌어 프론트 파싱을 깬다(챕터 10 실측 8인스턴스/6파일).

**반대로 순수 표현 파생은 Response로 내렸다** — 16자리 리뷰번호 0-pad, 문구 표시명 truncate(`CeoReplyPhraseResponse`), 거리별 배달팁 비움 판정, enum → 문자열 강등.

### Command 경로의 반환 Result는 앱 네임스페이스(`{app}application.<ctx>.port.out`)에 둔다

`ShopDeliveryAreaBulkResult`·`ProductAvailabilityChangeView`·admin `JwtResult`는 **읽기 계약 패키지(`com.tastyhouse.application..port.out`)가 아니라** 앱 네임스페이스에 있다. 읽기 계약 패키지에 두면 `commandServicesShouldNotDependOnQueryDaos`(CQRS 교차 주입 금지)가 CommandService의 **반환 타입**을 위반으로 잡는다.

## 앱별 auth 처리

| 앱 | 인증 방식 | 이 모듈에 있는 것 | api 모듈에 남은 것 |
|---|---|---|---|
| web | 소셜 로그인 SPI + JWT | `JwtTokenProvider` · `TokenService` · `CustomUserDetails(Service)` · `AuthCommandService` | `JwtConfig` · `SecurityConfig` · `PublicPaths` |
| admin | `spring-security-core` + JWT | 위 + `AdminUserDetailsService` | 위 + `RedisRepositoryConfig` |
| ceo | `spring-security-core` + JWT | 위 + `CeoUserDetailsService` | 위 + `RedisRepositoryConfig` |
| batch | 없음 | — | — |

**결합의 실체는 서블릿이 아니라 Spring Security core였다**(챕터 02 판단 기록). auth 컨텍스트 전체에 `jakarta.servlet`·`org.springframework.web` import가 **0건**이었고 — 컨트롤러가 이미 원시값(Bearer 토큰 문자열·인가 코드)만 넘기고 있었다 — 실제 blocker이던 `JwtTokenProvider`·`TokenService`·`CustomUserDetails(Service)`는 `AuthenticationManager`·`SecurityContextHolder`·`UserDetails`·JWT만 쓰는 **서블릿-프리** 타입이라 함께 이동할 수 있었다. 서블릿 결합 타입(필터·EntryPoint·`JwtConfig`·`SecurityConfig`)만 api에 남았고, `applicationMustBeServletFree`가 그 경계를 강제한다.

**소셜 로그인은 web에만 있다** — admin·ceo에는 없다.

## ceo 고유 — 소유권·규격 검증이 이 모듈에 있다

`ShopOwnershipValidator`(`shop.ceoId == 로그인 ceoId` 확인, 불일치 시 `ErrorCode.SHOP_ACCESS_DENIED` 403)와 `ShopImageSpecValidator`/`ProductImageSpecValidator`(이미지 규격)는 **application 계층 협력자**라 이 모듈에 있다. 서블릿 타입을 쓰지 않으므로 `applicationMustBeServletFree`에 걸리지 않으며, 검증기가 쓰는 `javax.imageio.ImageIO`는 java 표준이라 규칙 대상이 아니다.

규격 검증기가 `MultipartFile`을 파라미터로 받는 것은 **업로드 경계 파라미터**로 허용된 형태다(`applicationMustBeServletFree`의 유일한 carve-out). Command record에 담는 것은 `commandRecordsShouldNotHoldMultipartFile`이 별도로 금지한다 — Command에는 업로드 결과 참조(파일 식별자·URL)만 담는다.

## batch 고유 — 왜 `crawling/bbq`가 여기 있나 (챕터 01 §2 판단 기록)

스펙은 "driven 클라이언트면 batch-module 잔류 + 인터페이스 분리"를 원칙으로 했으나, 확인 결과 **`crawling/bbq`는 driven 클라이언트가 아니라 application 계층 코드**였다.

- `BbqProductSyncService`는 `@Service @Transactional`로 **트랜잭션 경계를 소유**하고, 저장 불변식은 도메인 서비스 `ProductRegistrationService`에 위임하며, 동기화 대상 탐색은 `ProductQueryPort`(읽기 포트)로 한다.
- `BbqService`는 오케스트레이션이고, **진짜 driven 클라이언트는 `infrastructure:crawling`에 있다**(`external.crawling.bbq.BbqApiClient`·`external.crawling.RemoteImageDownloader`).
- `BatchJobException`은 `BbqService`만 던지므로 함께 이동했다.

batch는 CQRS 분리를 쓰지 않는다 — `*CommandService`/`*QueryService`가 0개이고 잡 본문이 `*SchedulerService`에 담기며, 스케줄이 유일한 입력이라 Command record가 없고 인바운드 포트가 전부 `void foo()`다.

## ArchUnit — 4클래스 (챕터 03으로 importer·판별 기준이 패키지에서 마커로 전환)

**챕터 01 직후에는 아래 4클래스의 importer가 "4개 앱 패키지"(`com.tastyhouse.{web|admin|ceo|batch}application`)였다.** 챕터 03의 패키지 평탄화로 그 패키지 접두어가 사라지자 이 표현 자체가 성립하지 않게 됐고, 특히 `AppIsolationTest`는 슬라이스/패키지 술어에서 **마커 애노테이션 술어**로 전면 재작성됐다(`application/src/test/.../architecture/AppIsolationTest.java`).

| 클래스 | importer | 내용 |
|---|---|---|
| `LayerRulesTest` | `com.tastyhouse.application`(단일) | **공통 16종.** CQRS 교차 주입 2(이름 기준 — 아래 참고) · UseCase 구현 강제 2 · Command 경계 타입 2 · portIn/request 2 · QueryDSL·infra 차단 2 · servlet-free · adapter 역참조 금지 · 읽기 계약 프레임워크-프리 · swagger·api-common 차단 2 |
| `AppIsolationTest` | `com.tastyhouse.application`(단일, 마커로 앱 구분) | **챕터 03 전면 재작성.** `appsShouldNotDependOnEachOther`(마커 4종 4×3=12조합 개별 검사 — 슬라이스가 아니다) · `beansShouldHaveExactlyOneAppMarker` · `useCasesShouldHaveExactlyOneAppMarker` · `commandRecordsShouldBelongToExactlyOneApp`(`AppOwnership` 유도) · `markerBeanCounts`·`markerUseCaseCounts`(마커별 하한 — 앱별 anchor 승계) |
| `BatchSchedulerRulesTest` | `com.tastyhouse.application`(단일, `.areNotAnnotatedWith(BatchApp.class)` 등 마커 술어로 batch만 선별) | batch 고유 4종 + exact anchor 3종(`*SchedulerService` 7 · `..port.in..` 7 · response record 4) |
| `RuleAnchorTest` | `com.tastyhouse.application`(단일) + 계약 | 공허 통과 자동 검출. 마커별 하한은 `AppIsolationTest`가 승계했으므로 이 클래스는 계약(읽기 계약) 하한만 담당 |

**챕터 01 시점에 통합으로 의미가 달라져 손본 곳 두 군데는(carve-out FQN화, `applicationMustNotDependOnAdapters` 4패키지 확대) 챕터 03 이후에도 그대로 유효하다** — carve-out 대상 클래스와 api 패키지 이름 자체는 이번 평탄화로 바뀌지 않았다.

- `queryServicesShouldNotDependOnWritePorts`의 carve-out은 simple name이 아니라 **FQN**이다. `ShopQueryService`가 web·admin·ceo에 각각 있어 simple name으로 두면 의도한 1개가 아니라 3개 전부가 면제되기 때문이다. 확정 carve-out 3건(web `ShopQueryService` 도메인 계산 입력 / admin `AdminQueryService`·ceo `CeoQueryService` 인증 조회)은 이관 대상이 아니며, **이 목록에 새 항목을 추가하지 않는다.**
- `applicationMustNotDependOnAdapters`의 금지 대상은 **4개 api 패키지 전부**다. 어느 앱의 서비스든 어느 api 모듈도 역참조할 수 없다.

**분리해 둔 이유가 있는 곳도 둘이다.**

- `commandRecordsShouldBeBoundaryTyped`는 (챕터 03 이후) `.areNotAnnotatedWith(BatchApp.class)`로 batch를 제외한다. batch는 carve-out이 `domain.exception..` 하나뿐인 **엄격판**을 `BatchSchedulerRulesTest`에서 쓸 수 있는데, 한 규칙으로 합치면 batch가 느슨한 3-carve-out 규칙에 얹혀 엄격함을 잃는다.
- `AppIsolationTest`의 마커별 anchor(`markerBeanCounts`·`markerUseCaseCounts`)는 **마커별로 유지**한다. 합계 하나로 두면 한 앱의 빈·UseCase가 통째로 사라져도 나머지 세 앱이 하한을 떠받쳐 anchor가 조용히 통과한다.

`allowEmptyShould(true)`는 어느 파일에도 쓰지 않는다 — 규칙이 대상을 잃으면 공허하게 통과시키지 말고 규칙을 지우거나 anchor를 고친다.

### anchor 하한

**마커별 하한(빈·UseCase)은 `AppIsolationTest`가 갖는다** — `markerBeanCounts`(실측 web 66·admin 62·ceo 101·batch 13보다 낮은 하한: `@WebApp` ≥60·`@AdminApp` ≥55·`@CeoApp` ≥95·`@BatchApp` ≥12)와 `markerUseCaseCounts`(`@WebApp` ≥50·`@AdminApp` ≥100·`@CeoApp` ≥95·`@BatchApp` = 7 정확히 일치 — batch는 잡 7개로 규모가 작아 늘거나 줄면 의식적으로 고치는 것이 의도).

읽기 계약은 합계 **≥ 282**(통합 전 4개 앱 합 227 + 챕터 04로 돌아온 공유 계약 55, `RuleAnchorTest` 소유)이다. 소유 모듈을 가리던 소스-URI 필터는 챕터 04에서 제거했다 — 테스트 클래스패스에 남의 모듈 계약이 더는 없다.

하한으로 두는 이유는 컨텍스트가 늘어나는 것이 정상이기 때문이다. 정확히 일치를 요구하면 기능 추가마다 이 파일을 고쳐야 해 anchor가 규칙이 아니라 잡음이 된다(batch UseCase는 규모가 작아 예외적으로 정확히 일치를 쓴다).

## Dependencies

### 빌드 스크립트 형태
- `java-test-fixtures` 플러그인 — `AppOwnership`을 api 4모듈 테스트가 재사용하기 위한 것이다. **같은 파일을 각 모듈에 복제하면 두 벌이 갈라지므로** test fixture로 공유한다(위 [챕터 03](#챕터-03--패키지-평탄화--앱-마커-애노테이션-과거-판단의-번복) 참고). `testFixturesApi`로 `archunit-junit5`를 노출하는 이유는 `AppOwnership`이 마커 애노테이션(main)과 ArchUnit을 함께 보기 때문이다.
- **실행 모듈이 아니므로 `bootJar { enabled = false }` + `jar { enabled = true; archiveClassifier = '' }`** — plain jar만 만든다(`security-module` 선례). 아래 [주의](#주의) 참고.

### Internal
- `domain-module` (implementation) — 도메인 모델·VO·write 포트·도메인 서비스
- `security-core` (implementation) — `JwtTokenProvider`·Redis 토큰 저장소. **web·admin·ceo auth가 쓰는 서블릿-프리 타입 한정**
- **외부 연동 모듈(`infrastructure:{external,firebase,aws,oauth,payment,messaging,crawling}`) 의존은 두지 않는다** — 소셜 로그인 SPI(web)·크롤링 클라이언트(batch) 계약은 이 모듈이 소유하고 어댑터가 그것을 구현한다(**의존 역전**). 실제로 이 모듈의 계약을 구현하는 쪽은 `infrastructure:oauth`(소셜 SPI)와 `infrastructure:crawling`(배치 포트)이며, 이 줄을 되살리면 그 모듈들과 `application` 사이가 순환이 되어 빌드가 깨진다
- **`security-module`·`api-common-module`을 추가하지 않는다** — 서블릿 스택이 유입된다

### External
- `spring-security-core` — admin·ceo `AuthenticationManager`·`SecurityContextHolder`·`PasswordEncoder`·`UserDetails`
- `spring-web` — web·admin·ceo `MultipartFile`(업로드 경계 파라미터). 실사용이 1종뿐이라 starter-web 전체 대신 이 좌표만 선언한다
- `jackson-databind` — ceo `ShopStorePriceVerificationCommandService`의 `ObjectMapper`
- `spring-tx` — `@Transactional`만을 위한 최소 의존
- `spring-boot-autoconfigure`는 **재선언하지 않는다** — batch `AdminDongSyncRunner`의 `@ConditionalOnProperty`가 쓰지만 루트 `build.gradle`의 `subprojects` 블록이 넣는 `spring-boot-starter`로 전이 충족된다

batch 유스케이스가 `spring-web`·`spring-security-core`를 컴파일 클래스패스에서 보게 되지만, **서블릿 스택(`security-module`·`starter-web`)은 여전히 없다.**

### infrastructure 의존 없음 — 이 모듈의 핵심

application 계층이 infra를 모른다는 규칙을 ArchUnit이 아니라 **빌드 그래프가 1차로 강제**한다. `import com.tastyhouse.infrastructure...` 한 줄이 실제 컴파일 에러가 된다. `shouldNotDependOnInfrastructure`는 누군가 build.gradle에 의존을 되돌리는 회귀를 막는 2차 방어선이다.

## 빈 배선 (챕터 03 개정 — 패키지 스캔에서 마커 스캔으로)

**챕터 01 직후에는 앱마다 `{App}ApplicationConfig`가 자기 패키지만 스캔했다**(`@ComponentScan(basePackages = "com.tastyhouse.{app}application")`). 챕터 03의 평탄화로 그 앱별 패키지 자체가 사라졌으므로, 지금은 4개 `*ApplicationConfig` 전부가 **같은 루트 패키지(`com.tastyhouse.application`)를 스캔하되 마커로 걸러낸다**:

```java
@ComponentScan(
    basePackages = "com.tastyhouse.application",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = WebApp.class))
public class WebApplicationConfig { }
```

`useDefaultFilters = false`이므로 **마커가 곧 스캔의 유일한 포함 기준**이다 — `@WebApp` 없는 `@Service`는 컴파일은 통과하지만 `WebApplicationConfig`가 스캔해도 빈으로 뜨지 않는다. 이 실패는 그 빈이 처음 필요해지는 기동 시점에야 `NoSuchBeanDefinitionException`으로 드러나므로, 새 빈·UseCase를 추가할 때 마커를 빠뜨리지 않는 것이 이 모듈에서 가장 흔한 실수 지점이다(ArchUnit `beansShouldHaveExactlyOneAppMarker`·`useCasesShouldHaveExactlyOneAppMarker`가 이를 빌드 시점에 잡는다).

각 부트스트랩의 `@Import` 대상 클래스는 챕터 01 이후 그대로다 — 그래서 이 챕터의 부트스트랩(api 모듈) 소스 변경은 **0건**이다. `scanBasePackages` 문자열 나열이 아니라 타입 세이프 조합을 쓰는 것이 이 저장소의 표준 구성이다(`InfrastructurePersistenceConfig`·`BatchApplicationConfig` 선례).

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar(`security-module` 선례). 앱을 띄우는 것은 각 api 모듈의 fat jar 4개이며, 그 **이름·경로·포트는 통합 후에도 불변**이다. jar 내용만 application jar 4개 → `application-0.0.1-SNAPSHOT.jar` 1개로 바뀐다.
- **빈 배선 실수는 빌드로 드러나지 않는다** — `contextLoads` 테스트가 `@SpringBootTest` 없이 빈 껍데기라 `@Import` 누락 시 빌드는 green이고 jar만 조용히 깨진다. 배선을 건드렸으면 실제로 띄워 `Started {Xxx}Application` 마커를 확인한다.

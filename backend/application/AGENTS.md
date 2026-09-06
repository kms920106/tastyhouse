# application

**4개 앱(web · admin · ceo · batch)의 application 계층을 담는 단일 모듈.** 자바 패키지는 `com.tastyhouse.application` 하나로 평탄화돼 있다(챕터 03) — 구조는 `com.tastyhouse.application.<도메인>.{port.in, port.out, service}`이고 도메인 아래에 앱별 폴더가 없다. 컨텍스트별 인바운드 포트(`<ctx>/port/in/`)와 그 구현인 `*CommandService`/`*QueryService`(batch는 `*SchedulerService`), 그리고 읽기 계약 326개가 이 한 패키지 트리 안에 함께 있다. **앱 소속은 패키지가 아니라 마커 애노테이션**(`@WebApp`/`@AdminApp`/`@CeoApp`/`@BatchApp`)이 표현한다 — 상세는 아래 [챕터 03 — 패키지 평탄화 + 앱 마커](#챕터-03--패키지-평탄화--앱-마커-애노테이션-과거-판단의-번복).

컨트롤러(`<ctx>/adapter/in/web/`)·`request/`·`response/`·config·security 정책·전역 예외 핸들러와 부트스트랩은 각 api 모듈(`web-api`·`admin-api`·`ceo-api`·`batch-module`)에 남아 있다.

## 과거 판단의 번복 — 앱 축을 접은 이유 (챕터 01)

챕터 01~04(각각 batch·web·admin·ceo)로 앱마다 `{app}-application` 모듈을 하나씩 세웠던 것을, **이 챕터가 되돌려 하나로 합쳤다.** 앱 축 분리가 값을 못 했다는 판단이며 근거는 셋이다.

- **컴파일 게이트가 사실상 없었다.** `infrastructure/persistence/build.gradle`이 4개 application 모듈을 전부 `implementation`으로 의존하므로 **모든 실행 jar에 4개 jar가 이미 들어 있었다**(admin-api fat jar `BOOT-INF/lib/` 실측). 앱 분할이 실제로 준 게이트는 application → application 한 방향뿐이었고, 그것은 ArchUnit이 패키지로 이미 막고 있었다.
- **소유권 연쇄가 부채를 낳았다.** 읽기 계약을 앱 모듈이 소유하게 하면서 "한 앱이 소유하면 다른 앱이 그 모듈을 의존해야 한다"를 피하려고, 공유 계약을 `domain`로 올리고 `application-common-module`을 해체했다(챕터 05·07·09). 그 결과가 split package 5모듈과 가드 3종(`ReadContractSingleOwnerTest`·`ReadContractPurityTest`·`RuleAnchorTest`의 소유 모듈 필터)이다. **챕터 04에서 그 55개를 이 모듈로 되돌려 이 부채가 통째로 사라졌다.**
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

공유되는 것은 `domain`의 도메인 모델·write 포트·도메인 서비스와, 이 모듈 안에서 여러 앱이 함께 쓰는 `{Ctx}QueryPort` 계약이다. 그 시그니처를 바꿀 때는 소비 앱 전체를 함께 확인한다.

**앱 간 타입명 충돌 시 `Management`/`Owner` 한정어**를 상시 적용한다 — `Result`·`QueryPort`뿐 아니라 `*UseCase`·`*Service`·`*Command`·협력 빈(`*Reader`·`*View`)까지가 대상이다(규칙 전문과 한정어 삽입 위치는 루트 `backend/CLAUDE.md` 참고). 동명 클래스 **182건의 일괄 개명은 챕터 02에서 완료**했다.

### 읽기 계약을 이 모듈이 소유한다

**읽기 계약(`{Ctx}QueryPort`·`*Result`·`*SearchCondition`)은 전부 이 모듈에 있다** — `src/main/java/com/tastyhouse/application/<ctx>/port/out/`이다. 통합 전 4개 모듈이 나눠 갖던 271개를 같은 트리로 합쳤고(챕터 01, 파일명 충돌 0건), 챕터 04에서 `domain`이 갖고 있던 다중 앱 공유 계약 55개까지 돌아왔다.

**`com.tastyhouse.application`을 이 모듈이 단독 소유한다 — split package가 끝났다.** 공유 계약 55개를 `domain`에 두던 시기에는 한 패키지를 두 모듈이 나눠 가졌고, 그것을 지키는 가드가 3종 필요했다(`ReadContractSingleOwnerTest`·`ReadContractPurityTest`·`RuleAnchorTest`의 소유 모듈 필터). 챕터 04로 셋 다 사라졌다 — 같은 모듈 안의 FQCN 중복은 컴파일 에러이기 때문이다. 이동은 패키지 경로가 같아 `git mv`뿐이었고 소비 측 import는 0건 바뀌었다.

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
| web | 소셜 로그인 SPI + JWT | `JwtTokenProvider` · `TokenService` · `CustomUserDetails(Service)` · `AuthCommandService` | `SecurityConfig` · `PublicPaths` (`JwtConfig`는 챕터 02에서 삭제 — 필터 빈은 `SecurityModuleAutoConfiguration`이 등록) |
| admin | `spring-security-core` + JWT | 위 + `AdminUserDetailsService` | 위 (`RedisRepositoryConfig`는 챕터 01에서 삭제 — 키 접두사는 `security.token-store.key-prefix` 프로퍼티) |
| ceo | `spring-security-core` + JWT | 위 + `CeoUserDetailsService` | 위 (동일) |
| batch | 없음 | — | — |

**결합의 실체는 서블릿이 아니라 Spring Security core였다**(챕터 02 판단 기록). auth 컨텍스트 전체에 `jakarta.servlet`·`org.springframework.web` import가 **0건**이었고 — 컨트롤러가 이미 원시값(Bearer 토큰 문자열·인가 코드)만 넘기고 있었다 — 실제 blocker이던 `JwtTokenProvider`·`TokenService`·`CustomUserDetails(Service)`는 `AuthenticationManager`·`SecurityContextHolder`·`UserDetails`·JWT만 쓰는 **서블릿-프리** 타입이라 함께 이동할 수 있었다. 서블릿 결합 타입만 밖에 남았고 — 필터·EntryPoint는 `security-module`이, `SecurityConfig`·`PublicPaths`는 각 api 모듈이 갖는다 — `applicationMustBeServletFree`가 그 경계를 강제한다.

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
- `domain` (implementation) — 도메인 모델·VO·write 포트·도메인 서비스
- `security-core` (implementation) — `JwtTokenProvider`·토큰 저장소 **포트**. **web·admin·ceo auth가 쓰는 서블릿-프리 타입 한정**. 챕터 01로 `security-core → infrastructure:redis` 간선이 끊겨, 이 모듈의 runtimeClasspath에서 `infrastructure:redis`·`api-common-module`이 사라졌다(전이 수신 0)
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

각 부트스트랩의 `@Import` 대상 클래스는 챕터 01 이후 그대로다 — 그래서 이 챕터의 부트스트랩(api 모듈) 소스 변경은 **0건**이다. `scanBasePackages` 문자열 나열이 아니라 타입 세이프 조합을 쓰는 것이 이 저장소의 표준 구성이었고(`InfrastructurePersistenceConfig`·`BatchApplicationConfig` 선례), **auto-configuration 전환(챕터 02) 이후로는 `scanBasePackages` 나열 자체가 4개 앱에서 사라져 `@Import({App}ApplicationConfig)` 한 줄만 남았다.**

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar(`security-module` 선례). 앱을 띄우는 것은 각 api 모듈의 fat jar 4개이며, 그 **이름·경로·포트는 통합 후에도 불변**이다. jar 내용만 application jar 4개 → `application-0.0.1-SNAPSHOT.jar` 1개로 바뀐다.
- **빈 배선 실수는 빌드로 드러나지 않는다** — `contextLoads` 테스트가 `@SpringBootTest` 없이 빈 껍데기라 `@Import` 누락 시 빌드는 green이고 jar만 조용히 깨진다. 배선을 건드렸으면 실제로 띄워 `Started {Xxx}Application` 마커를 확인한다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

원문 주석은 챕터 04에서 제거되므로, 이 문서가 그 금지 지시의 유일한 소재지다.

### `queryServicesShouldNotDependOnWritePorts` carve-out 3건 — 목록에 새 항목을 추가하지 않는다

**대상**: `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`
→ `queryServicesShouldNotDependOnWritePorts()`

`*QueryService`는 domain의 write 포트를 주입하지 않는다 — 조회 트랜잭션(`readOnly = true`)에서 쓰기 경로가 열리는 것을 구조적으로 막는다.

**carve-out 3건은 각 앱에서 그대로 승계한 확정 판정이며, 이관 대상이 아니다.**

| FQN | 근거 |
|---|---|
| `com.tastyhouse.application.shop.service.ShopQueryService` (web) | write 포트를 배달팁 계산 경로가 도메인 서비스에 넘길 애그리거트 로드에 쓴다. 표현용 투영이 아니라 **도메인 계산 입력**이다 |
| `com.tastyhouse.application.admin.service.AdminQueryService` | 인증(UserDetails 로드)·시드 멱등성 확인에 쓰이며 표현 목적 read model이 없다. 엔티티/원시값 반환 + 불변식 검증 경로다 |
| `com.tastyhouse.application.ceo.service.CeoOwnerQueryService` | 위 admin과 같은 인증 조회 경로다 |

**판정 기준은 simple name이 아니라 FQN이다.** 4개 모듈이 하나로 합쳐지면서 동명 클래스가 한 importer에 들어왔기 때문이다 — 예컨대 `ShopQueryService`는 web·admin·ceo에 각각 존재했으므로 `haveSimpleNameNotEndingWith("ShopQueryService")`를 그대로 두면 **의도한 web 1개가 아니라 3개 전부가 면제**되어 admin·ceo의 위반이 조용히 통과했다. 이후 개명·평탄화로 simple name이 다시 유일해졌지만 **FQN을 유지한다** — 나중에 같은 접미어의 형제가 생겨도 면제 범위가 넓어지지 않기 때문이다.

**이 목록에 새 항목을 추가하지 않는다.**

### `commandRecordsShouldBeBoundaryTyped` carve-out 3건 — 느슨한 판을 batch에 적용하지 않는다

**대상**: `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`
→ `commandRecordsShouldBeBoundaryTyped()`

Command record는 경계 타입만 싣는다. carve-out 3건을 **그대로 유지**한다.

1. `com.tastyhouse.domain.exception..` — `BusinessException`·`ErrorCode`는 애그리거트가 아니라 전 계층이 공유하는 **횡단 관심사(에러 계약)**이고, compact constructor의 구조적 가드가 이를 던져야 응답 코드가 나머지 경로와 같은 형태로 나간다.
2. `org.springframework.web.multipart..`(`MultipartFile`) — 업로드를 받는 연산은 `method(XxxCommand, MultipartFile)`처럼 별도 파라미터로 두는 것이 규정된 형태이고, ArchUnit 의존 그래프는 같은 패키지 UseCase 인터페이스의 메서드 파라미터까지 함께 잡는다. Command **필드**로 실리는 것은 `commandRecordsShouldNotHoldMultipartFile`이 따로 막는다.
3. `com.tastyhouse.domain.shared.page..` — 근거는 `MultipartFile` carve-out과 **동일한 구조**다. 이 규칙이 겨냥하는 것은 Command record가 **필드로** 도메인 모델을 싣는 것인데, ArchUnit은 같은 `..port.in..` 패키지에 사는 **QueryUseCase의 메서드 시그니처**까지 함께 잡는다. 목록 반환 타입이 `PaginationResponse`에서 `PageResult`로 바뀌면서 걸린 건들은 **전부 반환 타입이며 Command 필드는 한 건도 없다**(실측 확인).

즉 이것은 규칙을 무르게 하는 것이 아니라, 규칙이 애초에 겨냥하지 않던 대상을 제외하는 것이다. 도메인 **모델**(`domain.{shop,order,member}.model..` 등)은 그대로 금지이며, Command가 실제로 도메인 타입을 필드로 실으면 여전히 걸린다.

**batch 제외는 importer가 아니라 `@BatchApp` 마커로 표현한다.** batch에는 Command record가 없고 인바운드 포트가 `void foo()`뿐이라 carve-out이 `domain.exception..` 하나인 **엄격판**을 쓸 수 있으며, 그쪽은 `BatchSchedulerRulesTest.inboundPortsShouldBeBoundaryTyped()`가 맡는다. **batch를 이 규칙에 함께 넣으면 느슨한 3-carve-out 판에 얹혀 엄격함을 잃는다.**

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

**대상**: `backend/application/src/testFixtures/java/com/tastyhouse/application/architecture/AppOwnership.java`
→ `DESERIALIZED_COMMANDS`

봉인 구성원 1개 — `com.tastyhouse.application.shop.port.in.ShopStorePriceVerificationItemCommand` (`CeoApp`).

`ShopStorePriceVerificationItemCommand`는 multipart의 **문자열 파트**로 들어온다. 컨트롤러도 Request record도 domain-free여야 해 파싱을 할 수 없으므로, Command가 원문을 `String items`로 담아 넘기고 `ShopStorePriceVerificationCommandService`가 `ObjectMapper`로 이 record 목록으로 역직렬화한다. 그래서 이 record는 어느 UseCase 시그니처에도, 어느 부모 Command의 컴포넌트로도 등장하지 않는다 — **유도가 닿을 수 없는 정상 형태이지 죽은 코드가 아니다.**

**이 목록에 새 항목을 추가하지 않는다.** 고아로 잡히는 record는 대개 진짜 죽은 코드이므로, 추가하기 전에 그 record를 **어디서 만드는지**를 먼저 찾는다. 여기 담을 수 있는 것은 "런타임 역직렬화로만 생성되어 정적 참조가 존재할 수 없는" 경우뿐이다.

### `CeoAuthCommandService` — 기록 실패 정책의 의도적 비대칭 (인증 조회 carve-out)

**대상**: `backend/application/src/main/java/com/tastyhouse/application/auth/service/CeoAuthCommandService.java`

**기록 실패 시 정책은 성공·실패 경로가 의도적으로 비대칭이다.**

- **성공 경로**: 기록 실패를 그대로 전파한다. 접속기록 없이 토큰이 발급되는 상태를 만들지 않는다 — 개인정보처리시스템 접속기록은 법적 요구사항이므로, 남기지 못했다면 접속도 허용하지 않는 편이 옳다.
- **실패 경로**: 기록 실패를 catch·로깅하고 원래 인증 예외를 rethrow한다. **감사 쓰기 실패가 인증 실패 응답 계약(401 `CEO_AUTHENTICATION_FAILED` 등)을 500으로 바꾸면 안 된다.**

이 비대칭은 `AuthCommandServiceTest`가 봉인한다.

### `AuthCommandServiceTest` — 점주 로그인 접속기록 배선 봉인 (소셜 4종 분기 carve-out)

**대상**: `backend/application/src/test/java/com/tastyhouse/application/auth/service/AuthCommandServiceTest.java`

이 테스트가 지키는 것은 네 가지다.

- 성공·실패 양쪽 모두 이력을 남긴다(실패 이력이 인증 예외와 함께 사라지지 않는다).
- 실패 시 **원래 인증 예외가 그대로 rethrow**된다 — 응답 계약이 바뀌지 않는다.
- 존재하지 않는 username은 기록하지 않는다(**계정 존재 여부 탐색 표면 방지**).
- 기록 실패 시 정책이 성공·실패 경로에서 **의도적으로 비대칭**이다.

### 읽기 계약 carve-out 5종 — 도메인 타입을 강등해 나르는 이유

아래 record들은 전부 **api 모듈이 도메인 타입을 알 수 없다는 경계** 때문에 존재한다. "중복 DTO"로 보고 합치거나 도메인 타입을 그대로 실으면 ArchUnit 규칙이 깨진다.

| 대상 (`backend/application/src/main/java/com/tastyhouse/application/...`) | 봉인 취지 |
|---|---|
| `product/port/out/ProductAvailabilityChangeView.java` | **거처는 앱 네임스페이스이고 읽기 계약 패키지(`com.tastyhouse.application..port.out`)가 아니다.** 판매상태 변경은 **Command 경로**의 반환값이라 조회 계약이 아니며, 읽기 계약 패키지에 두면 `commandServicesShouldNotDependOnQueryDaos`(CQRS 교차 주입 금지)가 CommandService의 반환 타입을 위반으로 잡는다. `ErrorCode`는 그대로 담는다 — 에러 계약은 **횡단 관심사**라 api 모듈에서도 참조가 허용된 carve-out(`domain.exception..`)이다 |
| `region/port/out/AdminDongBoundaryViewResult.java` | `AdminDongBoundaryResult`는 DAO가 읽어 온 **인코딩된** `boundary` 문자열을 그대로 들고 있어 그 자체로는 응답을 만들 수 없다. 디코딩은 `GeoRingsPort`가 수행하므로 **application에 남아야 하고**, 표현 계약이 `from(Result)` 한 번으로 끝낼 수 있도록 디코딩을 마친 이 타입을 따로 둔다. 좌표를 `GeoRing`·`GeoPoint`가 아니라 낱개 `BigDecimal` 쌍(`Point`)으로 내리는 이유는 **`controllersShouldBeDomainFree`의 carve-out이 `domain.shared.page..`와 도메인 enum뿐이고 `domain.shared.geo..`는 포함되지 않기** 때문이다. 리포 전체에서 api 모듈이 geo 타입을 참조하는 곳은 한 곳도 없으며, **그 경계를 깨지 않는다** |
| `review/port/out/ReviewBlindReasonView.java` | 카탈로그는 도메인 enum의 `values()`를 훑어 만드는데 그 메서드는 api 모듈에 허용된 accessor가 아니므로(`apiModuleShouldOnlyReadDomainEnums`) 목록 구성이 application에 남는다. **도메인 enum을 그대로 담지 않고 문자열로 강등해 나른다** — 인바운드 포트의 반환 타입에 `com.tastyhouse.domain..`이 실리면 `commandRecordsShouldBeBoundaryTyped`(carve-out은 예외·페이징 계약뿐)에 걸린다. **목록 요소는 제네릭 타입 인자로도 잡힌다** |
| `shop/port/out/GeoPointView.java` | 도형 계산은 도메인 기하 타입으로 수행하는데 api 모듈은 그 타입을 알 수 없다 — `apiModuleShouldBeDomainModelFree`의 carve-out은 `domain.exception..`·`domain.shared.page..`·도메인 enum뿐이고 **`domain.shared.geo..`는 포함되지 않는다.** 추가로 **컴포넌트 선언 순서는 알파벳순(`latitude` → `longitude`)이다** — 둘 다 `BigDecimal`이라 순서가 어긋나면 컴파일은 통과하고 **값만 조용히 뒤바뀐다** |
| `shop/port/out/ShopStorePriceVerificationViewResult.java` | 세 출처를 합친다 — 최신 인증 요청(애그리거트), 인증 여부 플래그, 미충족 메뉴 목록(도메인 서비스). 앞의 둘은 애그리거트에서, 마지막은 도메인 서비스에서 나오므로 표현 계약이 직접 받을 수 없다(`apiModuleShouldBeDomainModelFree`). 미충족 사유는 `domain.product.service`의 `StorePriceUnverifiedItem`을 그대로 넘기지 않고 `UnverifiedItem`으로 옮겨 담는다 — 그 타입은 도메인 **서비스** 패키지에 있어 api 모듈의 carve-out 어디에도 들어가지 않는다. 사유 enum 자체는 carve-out 대상이라 그대로 나르고, 문자열 강등은 표현 계약이 수행한다 |

### `ShopStorePriceVerificationCommandService` — 인덱스 기록이 도메인이 아니라 이 서비스에 있는 이유

**대상**: `backend/application/src/main/java/com/tastyhouse/application/shop/service/ShopStorePriceVerificationCommandService.java`

- **`items`가 JSON 문자열인 것은 요청 형식이 multipart이기 때문이다.** 가격표 이미지와 대상 목록은 한 트랜잭션에 함께 들어와야 한다 — 2단 요청으로 쪼개면 중간에서 끊긴 요청이 첨부만 있고 대상이 없는 고아 상태로 남고, 관리자 검수 큐에 검수할 수 없는 건이 쌓인다. multipart는 JSON 바디를 함께 실을 수 없으므로 목록만 문자열 파트로 받아 여기서 파싱한다.
- **인덱스 기록이 도메인이 아니라 이 서비스에 있는 것은 컨텍스트 경계 때문이다.** 다른 요청 유형(`ShopImageApprovalService`·`ShopDeliveryAreaAdjustmentService`)은 shop 컨텍스트 소유라 도메인 서비스가 직접 `ShopRequestIndexRecorder`를 호출한다. 그러나 인증 요청 애그리거트는 **product** 컨텍스트 소유여서, 그 도메인 서비스가 `shop.service`를 호출하면 `ContextBoundaryTest` 위반이 되고 **봉인 목록은 늘릴 수 없다.** 두 컨텍스트를 한 트랜잭션에서 잇는 일은 표현 계층의 몫이다.
- `MultipartFile`을 파라미터로 받는 것은 **파일 업로드 경계의 문서화된 예외**다 — 규격 검증이 업로드보다 앞서야 하고, 도메인은 통과분의 `fileId`만 받는다.

### QueryDSL 투영 전용 생성자 3건 — "never used" 경고를 근거로 삭제하지 말 것

**대상** (`backend/application/src/main/java/com/tastyhouse/application/review/port/out/`)

| record | 좁은 시그니처가 제외하는 것 | 투영 호출부 |
|---|---|---|
| `ReviewDetailResult` | 1:N인 이미지·태그 | `ReviewQueryDao` |
| `LatestReviewListItemResult` | 1:N인 이미지(`imageUrls`를 빈 목록으로 채운다) | `ReviewQueryDao`(6개 쿼리) |
| `ReviewManagementDetailResult` | 1:N인 이미지·태그 | `ReviewManagementQueryDao#findReviewManagementDetail` |

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

### `//noinspection BusyWait` — 이 억제 마커는 정당하며 제거 대상이 아니다

**대상**: `backend/application/src/main/java/com/tastyhouse/application/crawling/bbq/BbqService.java`
→ 카테고리 루프의 `Thread.sleep(10000)`

**이 모듈에서 유일하게 남아 있는 주석 형태의 코드**다(챕터 04의 주석 전량 제거에서 의도적으로 제외).
정적분석 도구가 읽는 억제 마커이므로 주석이 아니라 **코드로 취급한다.**

억제가 정당한 이유는 **busy-wait가 아니라 외부 BBQ 서버 부하 방지를 위한 의도적인 요청 간
지연**이기 때문이다. 루프 안의 `Thread.sleep`이라는 형태만 보고 "폴링을 이벤트 대기로 바꾸라"는
지적으로 오인해 지연 자체를 없애면, 크롤링이 외부 서버를 연속 타격한다. 마커와 지연 둘 다 유지한다.


## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

챕터 04에서 이 모듈의 java 주석 11,312줄을 전부 제거하며, 코드만 읽어서는 도달할 수 없는
설계 근거를 여기로 옮겼다. 각 절은 **어느 코드 요소에 붙어 있던 서술인지**를 앵커로 밝힌다.

### 트랜잭션 경계를 파사드가 아니라 하위 서비스가 갖는 이유 — read-then-write 판정

**대상**: `application/src/main/java/com/tastyhouse/application/member/service/MemberService.java`,
`auth/service/AuthPasswordResetService.java`, `auth/service/MemberAuthCommandService.java`

화면 단위 흐름을 엮는 **파사드는 `@Transactional`을 갖지 않는다.** 파사드가 트랜잭션을 열면
DB 원자성이 필요 없는 단계(JWT 서명 검증·Redis 접근)까지 DB 커넥션을 네트워크 지연만큼
점유하게 되므로, 원자성이 실제로 필요한 구간만 하위 CommandService가 단일 트랜잭션으로 갖는다.

판정 기준은 하나다 — **"이 단계가 DB에서 읽은 값에 근거해 DB를 쓰는가(read-then-write)?"**
그렇다면 검증과 쓰기가 같은 트랜잭션·같은 로드 안에 있어야 하고(그렇지 않으면 검증 후 쓰기
사이에 상태가 바뀌어 검사를 우회할 수 있다), 아니라면 묶지 않는다.

| 유스케이스 | 판정 | 근거 |
|---|---|---|
| 개인정보 변경 | 묶지 않는다 | 두 토큰 검증이 **JWT 서명·클레임 검증만** 수행하고 DB를 읽지 않는다(토큰이 발급 시점의 인증 사실을 서명으로 담고 있다). read-then-write 경합이 성립하지 않으며, 실제 DB write는 `MemberCommandService#updatePersonalInfo` 한 번뿐이라 이미 단일 트랜잭션이다 |
| 비밀번호 변경 | 묶었다(하강) | "새 비밀번호가 기존과 같은지" 검사가 **DB에서 읽은 현재 비밀번호**에 근거해 DB를 쓰는 read-then-write다. 과거에는 이 검사가 별도 readOnly 트랜잭션에 있어 검사와 변경이 두 트랜잭션·두 번의 회원 로드로 쪼개져 **검사 후 변경 사이에 비밀번호가 바뀌면 우회 가능**했다. `MemberCommandService#updatePassword` 안으로 내려 단일 트랜잭션·단일 로드로 원자화했다 |
| 회원 탈퇴 | 묶지 않는다(묶으면 틀린다) | 탈퇴는 DB 변경이지만 토큰 무효화는 **Redis 블랙리스트 등록**이라 DB 트랜잭션과 무관하다. 오히려 **순서가 중요**하다 — 탈퇴가 커밋된 뒤 무효화해야 하며, 한 트랜잭션에 넣으면 Redis 등록이 커밋 전에 일어나 **탈퇴가 롤백돼도 토큰만 죽는** 불일치가 남는다 |
| 인증코드 발송 | 묶었다 | "기존 미완료 인증 만료 + 새 인증 저장 + 발송"이 함께 성립해야 한다 |

**비밀번호 변경의 검사 순서를 뒤집지 않는다** — 동일 여부(`MEMBER_PASSWORD_SAME_AS_OLD`) →
확인값 불일치(`MEMBER_PASSWORD_CONFIRM_MISMATCH`) 순서를 유지해야 하며, 뒤집으면 두 조건을
동시에 위반한 요청의 **응답 코드가 바뀐다**.

### PG·외부 왕복은 트랜잭션 밖에 둔다 — 3단 구조와 보상 불가 지점

**대상**: `payment/service/PaymentCommandService.java` · `payment/service/PaymentConfirmationExecutor.java`

**클래스 레벨 `@Transactional`이 없는 것은 의도다.** 토스 승인·결제 취소는 PG사와의 HTTP 왕복을
포함하는데, 그 왕복이 DB 트랜잭션 안에 있으면 (1) 커넥션과 결제·주문 행 락을 네트워크 지연만큼
점유하고, (2) PG 처리가 성공한 뒤 커밋이 실패하면 **"PG는 승인/취소, DB는 미반영"이라는 보상 불가
불일치**가 남는다.

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

**대상**: `**/service/*CommandService.java` · `**/service/*QueryService.java`

`*CommandService`는 infra query DAO도 같은 모듈의 `*QueryService`도 주입하지 않고, `*QueryService`는
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
해 순환이 생긴다. 표현 목적 조회는 DAO 계층에서 교차하는 것이 옳다(`ProductQueryService` ↔
`ReviewQueryService`가 실제 사례).

### 도메인 계산 입력은 표현용 투영으로 대체하지 않는다

**대상**: `shop/service/ShopQueryService.java` → `findVisibleShopAggregate`

표현용 단건 조회와 달리 **도메인 서비스에 넘길 도메인 모델이 필요한 조회는 write 포트를 쓴다.**
계산기가 도메인 모델을 받으므로 표현용 Result를 도메인으로 되돌리는 역변환을 두지 않기 위함이며,
이것이 `queryServicesShouldNotDependOnWritePorts` carve-out의 실질적 근거다(위 봉인 목록 참조).
화면 표기용 목록(지역 이름 조립 등)만 infra query DAO에서 받는다.

같은 이유로 **read model을 `reconstitute`로 도메인 모델까지 되짚어 올려** 도메인 정책의 술어를
재사용하는 경로가 있다(`ShopPriceBadgeQueryService`·`ProductQueryService`). 규칙을 복제하면 표시
가격과 결제 금액이 갈리거나, 요일 구분을 추가할 때 한쪽만 고쳐진다. `reconstitute`(검증 미수행)를
쓰는 것은 **기존 데이터가 현행 규격을 위반해도 조회는 되어야 하기 때문**이다.

### 시각·시계에 의존하는 계산은 application에 남는다

**대상**: `coupon/port/out/MyCouponListItemResult.java` · `review/service/ShopReviewQueryService.java`
→ `toReplyWindow` · `review/service/ReviewOwnerReplyCommandService.java` → `register`

"오늘"을 읽어야 하는 판정은 표현 계약이 대신할 수 없다 — 표현 계약이 시계를 읽으면 **응답 조립이
시점에 따라 값이 달라지는 순수하지 않은 함수**가 된다. 마감일 상수는 도메인 모델이 소유하므로
api 모듈이 참조할 수 없다는 것(`apiModuleShouldBeDomainModelFree`)도 함께 작용한다.

반대 방향으로, **domain은 프레임워크-프리라 시계를 주입받을 수 없고** 도메인이 직접 `now()`를
부르면 단위 테스트에서 기한을 고정할 수 없다. 그래서 기준 시각은 **이 계층이 해석해 도메인 서비스에
넘긴다**.

### 도메인 enum에 대한 `switch`를 api 모듈로 내리지 않는다

**대상**: `review/service/ShopReviewQueryService.java` → `describeSortType` ·
`shop/service/ShopRequestQueryService.java` → `toRequestStatus`

도메인 enum에 대한 `switch`는 바이트코드에서 `ordinal()`·`values()` 호출이 되어 api 모듈에서는
`apiModuleShouldOnlyReadDomainEnums`(읽기 accessor 3종만 허용)에 걸린다. 그래서 분기·표시 문구
매핑은 이 계층에 남는다.

**`valueOf`가 아니라 `switch`를 쓰는 것도 의도다** — 어느 한쪽에 상수가 추가되면 컴파일이 깨져
매핑 누락이 드러난다. 값 이름이 그대로 대응하더라도 마찬가지다.

### 표시 문구를 서버가 완성하는 기준

**대상**: `shop/service/ShopQueryService.java` → `toShopDeliveryTipBreakdownItems`·`toTimeSlotLabel`

프론트가 분기·상수를 복제하지 않도록 서버가 문구를 완성한다. **문구 안의 숫자는 천 단위 콤마까지
서버가 넣는다** — 그 값은 응답의 금액 필드가 아니라 **이미 완성된 문장의 일부**라 프론트가 문자열을
뜯어 다시 포맷할 수 없기 때문이다(금액 필드 자체의 표기 포맷은 그대로 프론트 담당이다).

도메인 enum 승격이 필요한 표기(요일 표시명 등)도 여기서 끝낸다 — api 모듈이 호출할 수 없는
도메인 enum 메서드이기 때문이다.

### 컨텍스트 경계를 잇는 조립은 이 계층의 몫이다

**대상**: `product/service/ProductAvailabilityCommandService.java` ·
`product/service/ProductVegetarianCommandService.java` ·
`menureview/service/MenuReviewCommandService.java` ·
`shop/service/ShopStorePriceVerificationCommandService.java`

한 유스케이스가 두 컨텍스트의 값을 함께 필요로 하면, 도메인 서비스가 상대 컨텍스트를 직접 참조하는
대신 **이 계층이 각각 주입해 연결한다** — 도메인에서 참조하면 `ContextBoundaryTest` 위반이 되고
**봉인 목록은 늘릴 수 없다.**

정책과 계산을 가르는 기준도 함께 기록한다 — "오픈 시각을 정할 수 없다"(계산기)와 "그러면 얼마로
할까"(정책)는 서로 다른 판단이므로, **순수 계산기가 정책을 삼키지 않도록** 폴백 정책은 이 계층에 둔다.

### 소유권 역조회를 생략하지 않는다 — 실제 IDOR 사고의 근거

**대상**: `product/service/ProductImageCommandService.java` → `deleteImage` ·
`product/service/ProductOptionGroupOwnershipValidator.java` ·
`shop/service/ShopDeliveryAreaCommandService.java` → `removeDeliveryArea`

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

**대상**: `shop/service/ShopNoticeOwnerCommandService.java` → `saveImages`

파일 단위로 검증·업로드를 교차하면 뒤쪽 파일이 규격 위반일 때 앞쪽은 **이미 외부 스토리지에 올라간**
상태가 된다. 트랜잭션 롤백은 `UPLOADED_FILE` 행만 되돌릴 뿐 **스토리지 바이트는 되돌리지 못해**
실패 시도마다 고아 파일이 누적된다. 그래서 전량 검증을 먼저 끝낸 뒤 업로드한다.

변경 전 요약은 `updateContent` **호출 전에** 확정해야 한다 — 같은 인스턴스를 제자리에서 갱신하므로
나중에 읽으면 이미 변경 후 값이다.

### multipart 문자열 파트의 파싱 위치

**대상**: `shop/service/ShopStorePriceVerificationCommandService.java` → `toItemSpecs`

컨트롤러·Request record는 domain-free라 `BusinessException`을 던질 수 없고, 서비스는 `..request..`를
알 수 없다(`commandServicesShouldNotDependOnRequestRecords`). 세 규칙을 모두 만족하는 유일한 형태는
**Command가 원문을 경계 타입 `String`으로 담아 넘기고 서비스가 파싱하는 것**이다. 파싱 실패와 빈
목록이 같은 `ErrorCode`로 나가던 계약도 이때 그대로 보존된다(둘 다 서비스가 던진다).

실행 순서에도 의도가 있다 — **파싱을 업로드보다 앞에 둬야** 목록이 깨진 요청 때문에 쓸모없는
파일이 업로드되지 않는다.

### 조회 기간 상한을 이 계층에서 강제하는 이유

**대상**: `ceo/service/CeoLoginHistoryQueryService.java`(90일) ·
`ceo/service/CeoShopAccessHistoryQueryService.java`(5년) ·
`shop/service/ShopChangeHistoryQueryService.java`(6개월)

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

**대상**: `auth/service/CeoAuthCommandService.java` · `ceo/service/CeoLoginHistoryCommandService.java`

**이 클래스들에 `@Transactional`을 붙이지 않는다.** 로그인 실패는 Spring Security 예외로 전파되는데,
트랜잭션이 걸려 있으면 **실패 이력이 예외와 함께 롤백되어 영구히 남지 않는다.** 호출부가 비트랜잭션이므로
기록 서비스의 매 호출이 프록시를 거쳐 **독립 트랜잭션으로 즉시 커밋**되고, 따라서 `REQUIRES_NEW`가
필요 없다.

**기록 실패 시 정책은 성공·실패 경로가 의도적으로 비대칭이다**(봉인 목록의 carve-out과 짝).

- 성공 경로는 기록 실패를 **그대로 전파한다** — 접속기록 없이 토큰이 발급되는 상태를 만들지 않으며,
  개인정보처리시스템 접속기록은 법적 요구사항이라 남기지 못했다면 접속도 허용하지 않는 편이 옳다.
- 실패 경로는 기록 실패를 catch·로깅하고 **원래 인증 예외를 rethrow한다** — 감사 쓰기 실패가 인증
  실패 응답 계약(401)을 500으로 바꾸면 안 된다.

`refresh`는 접속기록을 남기지 않는다 — 토큰 갱신은 새로운 개인정보 접속이 아니라 기존 세션의 연장이다.
존재하지 않는 아이디도 기록하지 않는다 — 임의 username을 쌓으면 **계정 존재 여부를 탐색하는 표면**이 된다.

### 인증 타입의 앱별 중복은 의도된 것이다

**대상**: `auth/service/{Member,Admin,Ceo}AuthCommandService.java` · `auth/token/*TokenService.java`

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

**대상**: `shop/service/ShopRequestQueryService.java` → `toStorePriceVerificationDetailResult`

상세는 원칙적으로 원본 애그리거트를 다시 읽는다. 다만 인증 요청 유형은 원본이 **product 컨텍스트
소유**(승인의 본체가 `PRODUCT_PRICE` 갱신)여서 상세를 투영하는 shop 조회 DAO가 없다. 인덱스 상태는
접수·전이 시점마다 `ShopRequestIndexRecorder`가 동기화하므로 목록과 같은 값이고, 화면이 이 유형에서
필요한 것은 진행 상태와 반려 사유뿐이라 인덱스 값을 그대로 쓴다.

### 빈 상태·판정 불가를 예외로 만들지 않는다

**대상**: `shop/service/ShopPriceBadgeQueryService.java` → `getPriceBadges` ·
`shop/service/ShopQueryService.java` → `getShopNotice` ·
`product/service/ProductQueryService.java` → `findProductById` ·
`region/service/AdminDongQueryService.java` → `getAdminDongBoundaries`

부가 표시(뱃지)는 판정 불가가 **가게 화면 전체를 깨서는 안 되므로** 예외 대신 `false`를 준다.
공지가 없는 것은 에러가 아니라 `null`이다 — 대부분의 가게에 공지가 없으므로 404를 쓰면 프론트가
정상 상태를 에러로 처리하게 된다. 가격 행이 없는 이관 이전 메뉴도 예외 대신 빈 목록을 준다(상세가
500으로 막히면 그 메뉴는 아예 팔 수 없다). 지도 축소도 정상 조작이라 **400이 아니라 빈 배열 +
`truncated: true`**로 응답한다.

### N+1을 부르는 반복 조회는 호출부가 한 번에 읽는다

**대상**: `review/service/ReviewQueryService.java` → `findReviewedProductIds` ·
`product/service/ProductQueryService.java` → `findProductsBatch`

주문 상세처럼 항목이 여러 건인 화면이 항목마다 단건 조회를 부르면 항목 수만큼 쿼리가 나간다.
호출부가 **루프 전에 1회 조회**한 뒤 메모리에서 판정하거나, 가격 행을 한 번에 읽어 그룹핑한다.

### 서버가 판정해 가리는 필드는 표현 계층에 맡길 수 없다

**대상**: `review/port/out/ReviewDetailView.java` · `review/service/ReviewQueryService.java`
→ `toReviewDetailView`

배달 평가 3필드는 **뷰어가 작성자 본인일 때만** 채워진다(규격상 다른 고객에게 노출 금지, 본인은
수정 폼 초깃값으로 필요). 판정이 컨트롤러로 새면 **다른 호출부가 그 가림을 빠뜨릴 수 있으므로**
이 계약에 담긴 시점에 세 필드는 이미 "보여도 되는 값"이고 `null`이면 가려진 것이다.

**수정 폼은 받은 값을 그대로 되돌려 보내야 한다** — 수정 API는 PUT(전체 교체) 의미라 받은 값을 조건
없이 덮어쓴다. "값이 없으면 유지"를 서버에 넣지 않은 것은 그 순간 `null`이 "안 보냄"과 "지워줘" 두
뜻을 갖게 되어 **배달 평가를 지울 방법이 사라지기** 때문이다.

### 가시성 가드의 위치가 조회와 등록에서 다른 것은 의도다

**대상**: `review/service/ReviewQueryService.java` → `requireVisibleReview`

조회(GET)는 이 서비스 안에서 직접 가드를 걸지만, 등록(POST)은 컨트롤러가 가드를 호출한 뒤 command
서비스를 부른다 — command 서비스가 query 서비스를 주입받는 것이 **CQRS 교차 주입 금지 위반**이기
때문이다. **한쪽으로 통일하려다 중복 쿼리를 만들지 않는다.**

### 하위 호환을 위한 정규화

**대상**: `review/service/ReviewCommandService.java` → `createReview`·`validateDeliveryRating`

기존 클라이언트가 보내지 않는 필드는 `null`로 오므로 박싱 타입으로 받아 정규화한다(미전송 시 공개).
배달 평가도 **둘 다 null이면 검증 자체를 건너뛴다**. 새 필드를 추가할 때 이 형태를 따른다.

### 앱 마커가 곧 스캔 포함 기준이다

**대상**: `shared/marker/{WebApp,AdminApp,CeoApp,BatchApp}.java` · `{App}ApplicationConfig.java`

`useDefaultFilters = false` 스캔의 **유일한 포함 기준**이자 ArchUnit 앱 격리 규칙의 술어다. 새 빈과
새 UseCase 인터페이스는 **반드시 마커 하나를 단다** — 마커가 없으면 어느 앱에도 뜨지 않고, **컴파일은
통과하므로 실패는 기동 시점 `NoSuchBeanDefinitionException`으로만 드러난다.**

- Command record에는 붙이지 않는다(소속은 `AppOwnership`이 유도한다).
- `@Component` 메타를 얹지 않은 **순수 마커**로 유지한다 — 얹으면 기존 `@Service`의 의미가 흐려진다.
- 라이브러리 모듈 13개는 auto-configuration으로 자기 등록하지만 **이 설정만은 앱이 `@Import` 한다** —
  application 계층은 **앱 정체성 그 자체**라 클래스패스 존재만으로 어느 앱인지 결정할 수 없다
  (4개 앱의 빈이 같은 jar에 있고 마커로만 갈린다).

### 앱마다 얇은 래퍼를 두는 이유 — 트랜잭션 경계는 앱의 관심사다

**대상**: `file/service/FileUpload*CommandService.java`

업로드 규칙 본체(허용 확장자·용량 한도·저장 경로·이벤트 발행)는 도메인 서비스가 단독으로 갖고,
이 클래스들은 `MultipartFile` 어댑팅과 `@Transactional` 경계 선언 둘만 한다. **세 벌은 로직 중복이
아니라 경계 선언 3개다** — 과거 api-common의 `FileService` 한 벌이 겸했는데, 표현 모듈이
`@Transactional` 유스케이스를 갖는 데다 application이 그것을 주입받아 **application → 표현 역방향
의존**이 생겼다.

### 조회 전용 컨텍스트에는 CommandService를 두지 않는다

**대상**: `region/service/AdminDongQueryService.java` · `ceo/service/CeoManagementQueryService.java`

`ADMIN_DONG`은 시드 SQL로만 관리하는 read-only 마스터다. 점주 계정의 생성·수정은 ceo-api가 담당하므로
관리 조회 쪽에는 CommandService를 두지 않는다. **빈 CommandService를 형식으로 만들지 않는다.**

### `QueryUseCase`에는 컨트롤러 표면만 올린다 — 협력용 public 메서드를 전사하지 않는다

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

**새 조회를 추가할 때**: 컨트롤러가 부르지 않는 메서드라면 `*QueryUseCase`에 올리지 말고
`*QueryService`에만 둔다. 협력 서비스는 인터페이스가 아니라 구체 클래스를 주입해 쓴다.

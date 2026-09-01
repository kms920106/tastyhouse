# ceo-application

점주 웹(ceo-api)의 **application 계층**을 소유하는 모듈. 컨텍스트별 인바운드 포트(`<ctx>/port/in/`)와 그 구현인 `*CommandService`/`*QueryService`가 여기 있다. **표현 계약(`<ctx>/response/`)은 챕터 09로 `ceo-api`로 승격됐다** — 이 모듈에는 더 이상 없다.

컨트롤러(`<ctx>/adapter/in/web/`)·`request/`·`response/`·config·security 정책·부트스트랩(`CeoApiApplication`)은 `ceo-api`에 있다(`ceo-api/AGENTS.md`).

## 신설 배경 (챕터 04)

`ceo-api` 하나가 인바운드 어댑터와 application 계층을 함께 담고 있었다. 두 계층이 같은 모듈 안 다른 패키지였을 뿐이라 경계가 **규율로만** 유지됐고, ArchUnit이 사후에 잡을 수는 있어도 "서비스가 서블릿·컨트롤러로 새는 것"을 컴파일 단계에서 막지는 못했다. 모듈을 나누면 그 경계가 빌드 그래프가 된다.

챕터 01~03에서 확정한 레시피(모듈 신설 → `git mv` → 패키지 리네임 → config 배선 → ArchUnit 분할)를 그대로 적용했다. **앱별 application 모듈 분리는 이 챕터로 4개 모두 완결됐다.**

## 패키지 구조

```
com.tastyhouse.ceoapplication/
├── CeoApplicationConfig.java     @ComponentScan 진입점 — 쓰는 앱이 @Import 한다
├── <ctx>/port/in/                UseCase 인터페이스 + Command record
├── <ctx>/service/                *CommandService/*QueryService implements {Ctx}UseCase
└── <ctx>/port/out/               Command 경로 반환 Result (챕터 09 — 읽기 계약 패키지와 구분)

com.tastyhouse.application/            ← split package (챕터 06)
└── <ctx>/port/out/                ceo 단독 읽기 계약 61개 — {Ctx}QueryPort·Result·SearchCondition
```

컨텍스트 6종: `auth` · `ceo` · `product` · `region` · `review` · `shop`.

### 읽기 계약도 이 모듈이 소유한다 (챕터 06)

**ceo만 소비하는 읽기 계약 61개가 `com.tastyhouse.application.<ctx>.port.out`에 있다.** 자바 패키지는 `com.tastyhouse.ceoapplication..`이 아니라 `com.tastyhouse.application..` 그대로인 **split package** 형태이며, 이는 의도된 공통 결정이다 — 패키지를 유지했기 때문에 챕터 06의 이동이 **소비 측 import 무변경**으로 끝났고, ArchUnit 규칙의 패키지 패턴도 손대지 않았다.

컨텍스트는 `ceo` · `product` · `region` · `review` · `shared` · `shop` 6종이다(`port/in`의 6종과 목록이 다르다 — `auth`는 읽기 계약이 없고, 컨텍스트 중립인 `GeoRingsPort`가 `shared`에 있다).

- 구현은 여전히 `infrastructure:persistence`의 `<ctx>/query/` DAO다. 그 모듈이 `implementation project(':ceo-application')`을 선언해 이 계약들을 본다.
- **2개 이상의 앱이 함께 쓰는 계약은 여기 두지 않는다** — `domain-module` 소유다. 예를 들어 `ShopNoticeResult`·`ShopRequestDetailResult`는 ceo와 admin이 함께 쓰므로 챕터 06에서 `domain-module`로 갔다.
- 새 조회를 추가할 때는 **다른 앱도 쓰는지 먼저 확인한다.** 단독이면 이 모듈, 공유면 `domain-module`이다. 판정은 FQN grep만으로는 부족하다 — 같은 패키지 안의 형제 포트가 `import` 없이 참조하면 grep에 잡히지 않기 때문이다(챕터 06에서 실제로 두 건을 오판했고 컴파일 에러로 드러났다).

**컨텍스트 수는 가장 적은데 서비스 수는 가장 많다**(`*CommandService` 44 · `*QueryService` 43 — admin의 30/28보다 많다). 점주 셀프서비스가 `shop` 하나에 설정 관심사를 대량으로 갖기 때문이다(`ShopBusinessHour*`/`ShopClosedDay*`/`ShopPhoneNumber*`/`ShopStatus*`/`ShopIntroduction*`/`ShopConvenienceInfo*`/`ShopTrademark*`/`ShopContentBoard*`/`ShopSuspension*`/`ShopHygieneBadge*`/`ShopDeliveryTip*`/`ShopDeliveryArea*`). 관심사 단위로 쪼개는 이 관례는 분리 전과 동일하다.

## `response/`는 챕터 09로 ceo-api로 갔다

**챕터 04 당시에는 `response/`가 이 모듈에 함께 왔다.** 그때의 확정 규칙은 "`{도메인}QueryService`가 Result → Response 변환을 담당"이었으므로, `response/`를 ceo-api에 남기면 서비스가 `com.tastyhouse.ceoapi..`를 역참조해 `applicationMustNotDependOnAdapters`가 곧바로 위반됐기 때문이다.

**챕터 09가 그 전제를 바꿨다** — 조립 주체를 QueryService에서 **Response record 자신**(`from(XxxResult)`)으로 옮기고 Response를 ceo-api로 승격했다(105개). 유스케이스는 이제 프레임워크-프리 `*Result`·`PageResult`를 반환하므로 역참조가 생기지 않는다. 그 결과 이 모듈의 `io.swagger` import는 **0건**이고 `com.tastyhouse.apicommon` 참조도 **0건**이며, 두 상태를 `LayerRulesTest`의 `applicationShouldNotDependOnSwagger`·`applicationShouldNotDependOnApiCommon`이 고정한다(챕터 09 신설, admin이 챕터 06에서 심은 규칙과 동일).

**`request/`는 원래부터 ceo-api에 있었고 그대로다** — Request → Command 매핑은 인바운드 어댑터의 책임이며(완전 매핑 전략), 컨트롤러가 `request.toCommand(...)`로 조립해 넘긴다. 이제 양방향 매핑이 모두 ceo-api의 책임이다.

### 표현 계약이 만들 수 없는 값은 이 모듈이 `*View`/`*ViewResult`로 넘긴다

승격 후에도 **application에 남아야 하는 조립**이 있다. 표현 계약(api 모듈)은 도메인 모델·도메인 서비스·아웃바운드 포트를 알 수 없고 시계도 읽지 않아야 하므로, 아래는 이 모듈이 계산해 결과만 넘긴다.

| 남는 이유 | 예 |
|---|---|
| 도메인 애그리거트·도메인 서비스에서 나오는 값 | `ShopStatusResult`·`ShopDetailViewResult`·`ProductPriceView`·`ShopVisitGuideValidationResult` |
| 여러 읽기 포트를 합친 결과 | `ShopImageStatusResult`·`ShopClosedDaysResult`·`ShopDeliveryTipViewResult`·`ProductNutritionViewResult` |
| 도메인 enum의 **비-accessor 호출**이 필요한 값 | `ShopRequestListItemViewResult`(`isContractAmending`)·`ShopChangeCategoryResult`·`ShopRequestTypeCatalogResult`·`ProductAllergenTypeView`(`values()`) |
| carve-out이 아닌 도메인 타입 | `GeoPointView`(`domain.shared.geo..`는 carve-out이 아니다) |
| 시계·도메인 상수가 필요한 파생 | `ShopReviewReplyWindow`(답변 마감일 = 작성일 + `ReviewOwnerReply.REPLY_PERIOD_DAYS`) |
| 도메인 enum `switch` | `ShopReviewSortTypeView` — api 모듈에서 enum을 `switch`하면 바이트코드가 `ordinal()`·`values()`를 호출해 `apiModuleShouldOnlyReadDomainEnums`에 걸린다 |

**반대로 순수 표현 파생은 Response로 내렸다** — 16자리 리뷰번호 0-pad, 문구 표시명 truncate(`CeoReplyPhraseResponse`), 거리별 배달팁 비움 판정, 픽업 위치 null 판정, enum → 문자열 강등(accessor 3종).

### Command 경로의 반환 Result는 `ceoapplication.<ctx>.port.out`에 둔다

`ShopDeliveryAreaBulkResult`·`ShopDeliveryAreaBulkDeleteResult`·`ProductAvailabilityChangeView`는 **읽기 계약 패키지(`com.tastyhouse.application..port.out`)가 아니라** 이 모듈의 앱 네임스페이스에 있다. 읽기 계약 패키지에 두면 `commandServicesShouldNotDependOnQueryDaos`(CQRS 교차 주입 금지)가 CommandService의 **반환 타입**을 위반으로 잡는다. admin의 `JwtResult`가 `adminapplication.auth.port.out`에 있는 것과 같은 배치다.

## 소유권 검증이 이 모듈에 있다

`ShopOwnershipValidator`(`shop.ceoId == 로그인 ceoId` 확인, 불일치 시 `ErrorCode.SHOP_ACCESS_DENIED` 403)와 `ShopImageSpecValidator`(상표/콘텐츠 이미지 규격)는 **application 계층 협력자**라 이 모듈에 있다. 둘 다 서블릿 타입을 쓰지 않으므로 `applicationMustBeServletFree`에 걸리지 않는다.

`ShopImageSpecValidator`가 `MultipartFile`을 파라미터로 받는 것은 **업로드 경계 파라미터**로 허용된 형태다(`applicationMustBeServletFree`의 유일한 carve-out). Command record에 담는 것은 `commandRecordsShouldNotHoldMultipartFile`이 별도로 금지한다 — Command에는 업로드 결과 참조(파일 식별자·URL)만 담는다.

## auth 처리 (web/admin-application 선례와 동일)

`AuthCommandService`는 `AuthenticationManager`·`SecurityContextHolder`를, `CeoUserDetailsService`는 `UserDetails` 계약을 쓰는 **서블릿-프리** 타입이라 이 모듈로 함께 왔다. 서블릿 결합 타입(`JwtConfig`·`SecurityConfig`·`PublicPaths`)은 ceo-api에 남았다.

**ceo는 소셜 로그인이 없다** — web-application과 달리 `external-api` 의존이 없다.

## Dependencies

### Internal
- `domain-module` (implementation) — 도메인 모델·VO·write 포트·도메인 서비스
- `security-module` (implementation) — `JwtProperties`·Redis 토큰 저장소. **서블릿-프리 타입 한정**이며, Spring Security core는 이 모듈이 `api`로 노출하는 starter를 타고 들어온다
- `api-common-module` (implementation) — **소스 레벨 참조는 챕터 09로 0건이 됐다**(`applicationShouldNotDependOnApiCommon`이 강제). `build.gradle`에서 이 의존 자체를 제거하는 것은 챕터 11의 몫이라 선언만 남아 있다

### External
- `spring-boot-starter-security` (implementation) — `AuthenticationManager`·`SecurityContextHolder`·`UserDetails`
- `spring-boot-starter-web` (implementation) — **`MultipartFile`(업로드 경계 파라미터) 때문에 필요**하다. 서블릿 결합은 `applicationMustBeServletFree`가 막으므로 이 의존이 계층을 무너뜨리지 않는다(web-application과 같은 판단)
- `spring-tx` (implementation) — `@Transactional`만을 위한 최소 의존 (batch-application 선례)

### infrastructure 의존 없음 — 이 모듈의 핵심

application 계층이 infra를 모른다는 규칙을 ArchUnit이 아니라 **빌드 그래프가 1차로 강제**한다. `import com.tastyhouse.infrastructure...` 한 줄이 실제 컴파일 에러가 된다. ArchUnit `shouldNotDependOnInfrastructure`는 누군가 build.gradle에 의존을 되돌리는 회귀를 막는 2차 방어선이다.

## ArchUnit (`architecture/LayerRulesTest` 12종)

ceo-api에서 **이동한 규칙 10종**과, **물리 분리로 비로소 표현 가능해진 신설 규칙 2종**:

| 규칙 | 내용 | 출처 |
|---|---|---|
| `commandServicesShouldNotDependOnQueryDaos` | CQRS 교차 주입 금지(쓰기→읽기) | 이동 |
| `queryServicesShouldNotDependOnWritePorts` | CQRS 교차 주입 금지(읽기→쓰기) | 이동 |
| `commandServicesShouldImplementUseCase` | `*CommandService`는 `..port.in..` 구현 | 이동 |
| `queryServicesShouldImplementUseCase` | `*QueryService`는 `..port.in..` 구현 | 이동 |
| `commandRecordsShouldBeBoundaryTyped` | Command record ✗ domain(`domain.exception` carve-out)/infra/web | 이동 |
| `commandRecordsShouldNotHoldMultipartFile` | Command 필드에 `MultipartFile` 금지 | 이동 |
| `portInShouldNotDependOnWebPlumbing` | `..port.in..` ✗ web 플럼빙 | 이동 |
| `commandServicesShouldNotDependOnRequestRecords` | 서비스 ✗ `..request..` (완전 매핑) | 이동 |
| `shouldNotDependOnQuerydsl` | ✗ `com.querydsl..` | 이동 |
| `shouldNotDependOnInfrastructure` | ✗ `com.tastyhouse.infrastructure..` | 이동 |
| **`applicationMustBeServletFree`** | 모듈 전체 ✗ `jakarta.servlet..`·`org.springframework.web..` (`MultipartFile`만 예외) | **신설** |
| **`applicationMustNotDependOnAdapters`** | ✗ `com.tastyhouse.ceoapi..` (역참조 금지) | **신설** |

ceo-api에 함께 있을 때는 컨트롤러가 정당하게 서블릿 타입을 쓰므로 모듈 전역 금지를 걸 수 없었다.

`RuleAnchorTest`가 각 규칙의 anchor 개수(`*CommandService` 44 · `*QueryService` 43 · `..port.in..` 222 · 전체 427)를 **하한으로** 검사해, 클래스가 대량 소실되면 빌드가 실패하게 한다.

**`allowEmptyShould(true)`는 쓰지 않는다.** 규칙이 대상을 잃으면 공허 통과를 열지 말고 규칙을 지우거나 anchor를 고친다.

## 빈 배선

`CeoApplicationConfig`(`@ComponentScan("com.tastyhouse.ceoapplication")`)를 `CeoApiApplication`의 `@Import`에 추가한다. `scanBasePackages` 문자열 나열이 아니라 타입 세이프 조합을 쓰는 것이 이 저장소의 표준 구성이다.

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar(`security-module` 선례). 점주 앱을 띄우는 것은 `ceo-api`의 fat jar다.
- **배달팁 파트 5종의 집합 불변식**은 이 모듈의 서비스가 replace-all로 검증한다(구간 "3개 이하 + 금액 오름차순 + 팁 내림차순", 거리별↔지역별 상호 배타). 컨트롤러가 하나로 묶여 있는 이유와 판정 기준은 `ceo-api/AGENTS.md`와 루트 `backend/CLAUDE.md`의 "집합 불변식 설정 컬렉션은 replace-all PUT으로 교체하는 규칙" 참고.
- **web/admin-application과 서비스를 공유하지 않는다** — 같은 이름이 여러 모듈에 공존하는 것은 정상이다. 공유되는 것은 `domain-module`의 write 포트·도메인 서비스와 **여러 앱이 함께 쓰는 읽기 계약**(`domain-module` 소유, 챕터 05~06)이며, 그 시그니처를 바꿀 때는 소비 모듈 전체를 함께 확인한다.

- **챕터 09 갱신 — `application-common-module`이 사라졌다**: 위 [읽기 계약도 이 모듈이 소유한다](#읽기-계약도-이-모듈이-소유한다-챕터-06)의 "admin·web 단독분은 아직 그 모듈에 있다"는 전제가 해소됐다. 이제 앱 단독 계약은 각 `{앱}-application`이, 다중 앱 공유 계약은 `domain-module`이 소유하며 `application-common-module` 의존 선언도 제거됐다.
- **프레임워크-프리는 이제 `LayerRulesTest#readContractsShouldBeFrameworkFree`가 지킨다**: 이 모듈은 spring starter를 받으므로 `application-common-module` 시절의 컴파일 게이트가 없다. 계약이 참조해도 되는 것은 `java..`·`com.tastyhouse.domain..`과 자기 자신뿐이다.

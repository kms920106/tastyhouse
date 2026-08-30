# ceo-application

점주 웹(ceo-api)의 **application 계층**을 소유하는 모듈. 컨텍스트별 인바운드 포트(`<ctx>/port/in/`), 그 구현인 `*CommandService`/`*QueryService`, 그리고 QueryService가 조립하는 표현 계약(`<ctx>/response/`)이 여기 있다.

컨트롤러(`<ctx>/adapter/in/web/`)·`request/`·config·security 정책·부트스트랩(`CeoApiApplication`)은 `ceo-api`에 남아 있다(`ceo-api/AGENTS.md`).

## 신설 배경 (챕터 04)

`ceo-api` 하나가 인바운드 어댑터와 application 계층을 함께 담고 있었다. 두 계층이 같은 모듈 안 다른 패키지였을 뿐이라 경계가 **규율로만** 유지됐고, ArchUnit이 사후에 잡을 수는 있어도 "서비스가 서블릿·컨트롤러로 새는 것"을 컴파일 단계에서 막지는 못했다. 모듈을 나누면 그 경계가 빌드 그래프가 된다.

챕터 01~03에서 확정한 레시피(모듈 신설 → `git mv` → 패키지 리네임 → config 배선 → ArchUnit 분할)를 그대로 적용했다. **앱별 application 모듈 분리는 이 챕터로 4개 모두 완결됐다.**

## 패키지 구조

```
com.tastyhouse.ceoapplication/
├── CeoApplicationConfig.java     @ComponentScan 진입점 — 쓰는 앱이 @Import 한다
├── <ctx>/port/in/                UseCase 인터페이스 + Command record
├── <ctx>/service/                *CommandService/*QueryService implements {Ctx}UseCase
└── <ctx>/response/               QueryService가 조립하는 표현 계약 record
```

컨텍스트 6종: `auth` · `ceo` · `product` · `region` · `review` · `shop`.

**컨텍스트 수는 가장 적은데 서비스 수는 가장 많다**(`*CommandService` 44 · `*QueryService` 43 — admin의 30/28보다 많다). 점주 셀프서비스가 `shop` 하나에 설정 관심사를 대량으로 갖기 때문이다(`ShopBusinessHour*`/`ShopClosedDay*`/`ShopPhoneNumber*`/`ShopStatus*`/`ShopIntroduction*`/`ShopConvenienceInfo*`/`ShopTrademark*`/`ShopContentBoard*`/`ShopSuspension*`/`ShopHygieneBadge*`/`ShopDeliveryTip*`/`ShopDeliveryArea*`). 관심사 단위로 쪼개는 이 관례는 분리 전과 동일하다.

## 왜 `response/`가 함께 왔나

이 저장소의 확정 규칙상 **`{도메인}QueryService`가 Result → Response 변환을 담당**한다(루트 `backend/CLAUDE.md`의 DTO 조립 규칙). 따라서 `response/`를 ceo-api에 남기면 서비스가 `com.tastyhouse.ceoapi..`를 역참조하게 되어 `applicationMustNotDependOnAdapters`가 곧바로 위반되고, 모듈 분리 자체가 성립하지 않는다. `web-application`·`admin-application`과 같은 판단이다.

**`request/`는 반대로 ceo-api에 남는다** — Request → Command 매핑은 인바운드 어댑터의 책임이고(완전 매핑 전략), 컨트롤러가 `request.toCommand(...)`로 조립해 넘긴다. 그 방향(ceo-api → ceo-application)은 정상 의존이다.

**admin과 바이트 동일하던 shop 응답 3종**(`ShopBreakTimeResponse`·`ShopBusinessHourResponse`·`ShopHygieneBadgeResponse`)은 이 모듈이 아니라 `api-common-module` 소유이며, 그 관계는 분리 후에도 그대로다.

## 소유권 검증이 이 모듈에 있다

`ShopOwnershipValidator`(`shop.ceoId == 로그인 ceoId` 확인, 불일치 시 `ErrorCode.SHOP_ACCESS_DENIED` 403)와 `ShopImageSpecValidator`(상표/콘텐츠 이미지 규격)는 **application 계층 협력자**라 이 모듈에 있다. 둘 다 서블릿 타입을 쓰지 않으므로 `applicationMustBeServletFree`에 걸리지 않는다.

`ShopImageSpecValidator`가 `MultipartFile`을 파라미터로 받는 것은 **업로드 경계 파라미터**로 허용된 형태다(`applicationMustBeServletFree`의 유일한 carve-out). Command record에 담는 것은 `commandRecordsShouldNotHoldMultipartFile`이 별도로 금지한다 — Command에는 업로드 결과 참조(파일 식별자·URL)만 담는다.

## auth 처리 (web/admin-application 선례와 동일)

`AuthCommandService`는 `AuthenticationManager`·`SecurityContextHolder`를, `CeoUserDetailsService`는 `UserDetails` 계약을 쓰는 **서블릿-프리** 타입이라 이 모듈로 함께 왔다. 서블릿 결합 타입(`JwtConfig`·`SecurityConfig`·`PublicPaths`)은 ceo-api에 남았다.

**ceo는 소셜 로그인이 없다** — web-application과 달리 `external-api` 의존이 없다.

## Dependencies

### Internal
- `domain-module` (implementation) — 도메인 모델·VO·write 포트·도메인 서비스
- `application-common-module` (implementation) — `{Ctx}QueryPort`·Result DTO·SearchCondition
- `security-module` (implementation) — `JwtProperties`·Redis 토큰 저장소. **서블릿-프리 타입 한정**이며, Spring Security core는 이 모듈이 `api`로 노출하는 starter를 타고 들어온다
- `api-common-module` (implementation) — `PaginationResponse<T>`·공용 shop 응답 3종 등 표현 계약

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
- **web/admin-application과 서비스를 공유하지 않는다** — 같은 이름이 여러 모듈에 공존하는 것은 정상이다. 공유되는 것은 `domain-module`의 write 포트·도메인 서비스와 `application-common-module`의 `{Ctx}QueryPort` 계약이며, 그 시그니처를 바꿀 때는 소비 모듈 전체를 함께 확인한다.

# admin-application

관리자 웹(admin-api)의 **application 계층**을 소유하는 모듈. 컨텍스트별 인바운드 포트(`<ctx>/port/in/`)와 그 구현인 `*CommandService`/`*QueryService`가 여기 있다.

**`<ctx>/response/`는 챕터 06으로 admin-api로 이동했다** — 이 모듈은 `io.swagger`·`com.tastyhouse.apicommon` import가 0건이고, 그 상태를 `applicationShouldNotDependOnSwagger`·`applicationShouldNotDependOnApiCommon`이 고정한다. QueryService는 `*Result`(프레임워크-프리 읽기 계약)를 반환하고 조립은 컨트롤러·Response record가 한다.

컨트롤러(`<ctx>/adapter/in/web/`)·`request/`·config·security 정책·부트스트랩(`AdminApiApplication`)은 `admin-api`에 남아 있다(`admin-api/AGENTS.md`).

## 신설 배경 (챕터 03)

`admin-api` 하나가 인바운드 어댑터와 application 계층을 함께 담고 있었다. 두 계층이 같은 모듈 안 다른 패키지였을 뿐이라 경계가 **규율로만** 유지됐고, ArchUnit이 사후에 잡을 수는 있어도 "서비스가 서블릿·컨트롤러로 새는 것"을 컴파일 단계에서 막지는 못했다. 모듈을 나누면 그 경계가 빌드 그래프가 된다.

챕터 01(`batch-application`)에서 확정하고 챕터 02(`web-application`)가 검증한 레시피(모듈 신설 → `git mv` → 패키지 리네임 → config 배선 → ArchUnit 분할)를 그대로 적용했다.

## 패키지 구조

```
com.tastyhouse.adminapplication/
├── AdminApplicationConfig.java   @ComponentScan 진입점 — 쓰는 앱이 @Import 한다
├── <ctx>/port/in/                UseCase 인터페이스 + Command record
├── <ctx>/service/                *CommandService/*QueryService implements {Ctx}UseCase
└── (<ctx>/response/ 는 챕터 06으로 admin-api로 이동 — 이 모듈에 없다)
```

컨텍스트 19종: `admin` · `auth` · `banner` · `bug` · `ceo` · `coupon` · `event` · `faq` · `file` · `member` · `notice` · `order` · `partnership` · `point` · `policy` · `product` · `rank` · `review` · `shop`.

대형 컨텍스트는 관심사 단위로 서비스를 더 쪼갠다(`shop`의 `ShopContentBoard*`/`ShopHygieneBadge*`/`ShopImageChange*` 등) — 이 관례는 분리 전과 동일하다.

## `response/`는 왜 떠났나 (챕터 06 — 과거 판단의 번복)

**과거 이 절은 "왜 `response/`가 함께 왔나"였고, 근거는 "확정 규칙상 `{도메인}QueryService`가 Result → Response 변환을 담당하므로 `response/`를 admin-api에 남기면 서비스가 `com.tastyhouse.adminapi..`를 역참조해 `applicationMustNotDependOnAdapters`를 위반한다"였다.** 챕터 06이 그 전제를 뒤집었다 — 변환 주체가 QueryService가 아니게 됐기 때문이다.

Response record는 `@Schema`(Swagger)를 달고 `PaginationResponse`(HTTP 래퍼)를 쓴다. 그것이 이 모듈에 있으면 **유스케이스 계층이 API 문서화 도구와 HTTP 표현을 알게 된다**(전환 전 실측: `io.swagger` import 85개 파일). 챕터 06은 그 오염을 걷어내려고 조립 책임을 **컨트롤러·Response record**로 올렸다. 그러면 역참조 문제는 애초에 생기지 않는다 — 서비스가 Response를 모르고 `*Result`만 반환하기 때문이다.

**`request/`는 여전히 admin-api에 있다** — Request → Command 매핑은 인바운드 어댑터의 책임이고(완전 매핑 전략), 컨트롤러가 `request.toCommand(...)`로 조립해 넘긴다. 즉 지금은 `request/`·`response/` 둘 다 api 모듈에 있고, 이 모듈은 포트와 서비스만 갖는다.

**ceo·web은 아직 과거 형태다** — 챕터 06이 admin에만 적용됐으므로 `ceo-application`·`web-application`의 `response/`는 그대로 있고 `io.swagger` import도 각각 105·131개 파일이다. 그 두 모듈의 AGENTS.md 서술은 현재도 유효하다.

## auth 처리 (web-application 선례와 동일)

`AuthService`는 `AuthenticationManager`·`SecurityContextHolder`만 쓰는 **서블릿-프리** 타입이라 이 모듈로 함께 왔고, `AdminCommandService`가 쓰는 `PasswordEncoder`도 같은 이유로 문제되지 않는다. 필요한 것은 `spring-boot-starter-security` 한 줄이며 서블릿 결합은 `applicationMustBeServletFree`가 막는다.

**서블릿 결합 타입은 admin-api에 남았다** — `JwtConfig`(필터 빈 등록)·`SecurityConfig`(필터체인)·`PublicPaths`.

**admin은 소셜 로그인이 없다** — web-application과 달리 `external-api` 의존이 없고, `excludeFilters`로 `com.tastyhouse.external.oauth.*`를 제외하는 관례도 admin-api 쪽에 그대로 유지된다.

## Dependencies

### Internal
- `domain-module` (implementation) — 도메인 모델·VO·write 포트·도메인 서비스
- `security-module` (implementation) — `JwtProperties`·Redis 토큰 저장소(접두사 `admin:rt:`/`admin:bl:`). **서블릿-프리 타입 한정**이며, Spring Security core는 이 모듈이 `api`로 노출하는 starter를 타고 들어온다
- `api-common-module` (implementation) — `PaginationResponse<T>` 등 표현 계약. 이 모듈이 `api`로 노출하는 `spring-boot-starter-web`·springdoc(`@Schema`)을 전이로 받는다

### External
- `spring-boot-starter-security` (implementation) — `AuthenticationManager`·`SecurityContextHolder`·`PasswordEncoder`
- `spring-tx` (implementation) — `@Transactional`만을 위한 최소 의존 (batch-application 선례)

**`spring-boot-starter-web`을 직접 선언하지 않는다** — ceo-application은 `MultipartFile`을 서비스 파라미터로 받아 직접 선언하지만, admin은 그런 업로드 경계 파라미터가 없어 `api-common-module`을 통한 전이로 충분하다.

### infrastructure 의존 없음 — 이 모듈의 핵심

application 계층이 infra를 모른다는 규칙을 ArchUnit이 아니라 **빌드 그래프가 1차로 강제**한다. `import com.tastyhouse.infrastructure...` 한 줄이 실제 컴파일 에러가 된다. ArchUnit `shouldNotDependOnInfrastructure`는 누군가 build.gradle에 의존을 되돌리는 회귀를 막는 2차 방어선이다.

## ArchUnit (`architecture/LayerRulesTest` 12종)

admin-api에서 **이동한 규칙 10종**과, **물리 분리로 비로소 표현 가능해진 신설 규칙 2종**:

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
| **`applicationMustBeServletFree`** | 모듈 전체 ✗ `jakarta.servlet..`·`org.springframework.web..` | **신설** |
| **`applicationMustNotDependOnAdapters`** | ✗ `com.tastyhouse.adminapi..` (역참조 금지) | **신설** |

admin-api에 함께 있을 때는 컨트롤러가 정당하게 서블릿 타입을 쓰므로 모듈 전역 금지를 걸 수 없었다.

`RuleAnchorTest`가 각 규칙의 anchor 개수(`*CommandService` 30 · `*QueryService` 28 · `..port.in..` 228 · 전체 373)를 **하한으로** 검사해, 클래스가 대량 소실되면 빌드가 실패하게 한다. 하한으로 두는 이유는 컨텍스트가 늘어나는 것이 정상이기 때문이다 — 정확히 일치를 요구하면 기능 추가마다 이 파일을 고쳐야 해 anchor가 규칙이 아니라 잡음이 된다(batch-application은 규모가 작아 일치를 썼다).

**`allowEmptyShould(true)`는 쓰지 않는다.** 규칙이 대상을 잃으면 공허 통과를 열지 말고 규칙을 지우거나 anchor를 고친다.

## 빈 배선

`AdminApplicationConfig`(`@ComponentScan("com.tastyhouse.adminapplication")`)를 `AdminApiApplication`의 `@Import`에 추가한다. `scanBasePackages` 문자열 나열이 아니라 타입 세이프 조합을 쓰는 것이 이 저장소의 표준 구성이다(`InfrastructurePersistenceConfig`·`BatchApplicationConfig` 선례).

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar(`security-module` 선례). 관리자 앱을 띄우는 것은 `admin-api`의 fat jar다.
- **web-application과 서비스를 공유하지 않는다** — `NoticeQueryService`처럼 같은 이름이 두 모듈에 공존하는 것은 정상이다(패키지가 달라 충돌하지 않음). 소비자가 다르면 조회 범위·응답 형태가 다르므로 통합하지 않는다. 공유되는 것은 `domain-module`의 도메인 모델·write 포트·도메인 서비스와 그 모듈이 소유하는 다중 앱 공유 `{Ctx}QueryPort` 계약이며, 그 시그니처를 바꿀 때는 소비 모듈 전체를 함께 확인한다.
- **admin/web Result 충돌 시 `Management` 한정어**를 상시 적용한다(루트 `backend/CLAUDE.md` 참고).

- **읽기 계약을 이 모듈이 소유한다 (챕터 09 — `application-common-module` 해체 완료)**: 이 앱만 소비하는 `{Ctx}QueryPort`·`*Result`·`*SearchCondition`은 `src/main/java/com/tastyhouse/application/<ctx>/port/out/`에 있다. 패키지가 모듈명과 어긋나는 것은 의도된 선택이며(`infrastructure:persistence`가 `com.tastyhouse.infrastructure..`를 쓰는 것과 같은 선례), 그 덕분에 계약이 어느 모듈로 가든 소비 측 import와 ArchUnit 패키지 규칙이 바뀌지 않는다.
  - **2개 이상의 앱이 쓰는 공유 계약은 여기 두지 않고 `domain-module`이 소유한다** — 다른 앱이 이 모듈을 의존하게 되는 앱 간 수평 의존을 막기 위해서다. 새 계약을 추가하기 전에 소비 앱이 몇 개인지 먼저 센다.
  - **프레임워크-프리를 `LayerRulesTest#readContractsShouldBeFrameworkFree`가 지킨다**: 이 모듈은 spring starter를 받으므로 `application-common-module` 시절의 컴파일 게이트가 없다. 계약이 참조해도 되는 것은 `java..`·`com.tastyhouse.domain..`과 자기 자신뿐이다.

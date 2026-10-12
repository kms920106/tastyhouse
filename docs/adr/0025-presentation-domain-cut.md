# ADR-0025: 표현 계층(api·api-common·security)이 domain을 모르게 절단한다(엄격 레이어드 덩어리 01)

- 상태: 채택(ErrorContracts 부분 → [ADR-0042](0042-error-code-module-split.md), `@SharedApp` 설정 허용 부분 → [ADR-0036](0036-app-marker-removal.md))
- 결정 커밋: `dfb8f4685` (2026-09-27)
- 대체한 결정: [ADR-0010](0010-api-domain-free-carve-outs.md)

## 맥락

- **표현 계층**: HTTP 요청을 받고 응답을 만드는 바깥쪽 모듈. 여기서는 `web-api`·`admin-api`·`ceo-api`, 공용 플럼빙 `api-common-module`, 보안 `security-core`·`security-module`을 말한다.
- **domain**: 애그리거트·VO·도메인 enum·도메인 예외를 담는 가장 안쪽 모듈.
- **carve-out**: 금지 규칙에 일부러 뚫어 둔 예외 구멍.

[ADR-0010](0010-api-domain-free-carve-outs.md)에서 api 모듈은 domain을 금지하되 carve-out 3종(`domain.exception..`·`domain.shared.page..`·도메인 enum 읽기 accessor)을 열어 두었다. 그 구멍 때문에 표현 계층이 여전히 domain 타입을 컴파일 시점에 볼 수 있었고, `api-common-module`이 `api project(':domain')`으로 domain을 api 3모듈에 전이 노출했다. 두 `GlobalExceptionHandler`의 `@ExceptionHandler(BusinessException.class)` 메서드는 `com.tastyhouse.domain.exception`을 import해야 했고, 이것이 presentation의 domain 의존을 끊지 못하게 한 마지막 고리였다.

## 이전 규칙

### backend/AGENTS.md — `apiModuleShouldBeDomainModelFree` / `ALLOWED_DOMAIN_ENUM_ACCESSORS` / `domainEnum()` / `domainBoundaryPredicatesShouldStillBite`

- `apiModuleShouldBeDomainModelFree`는 모듈 전역에서 `com.tastyhouse.domain..`을 금지하되 carve-out 3종을 두었다.
  1. `domain.exception..`: 계층 칸이 없는 횡단 관심사로 보았다. `api-common-module`이 `api project(':domain')`로 공용 에러 계약을 전 모듈에 노출했다.
  2. `domain.shared.page..`: 페이징 조립을 컨트롤러로 옮기면서 정상 경로가 됐다. application이 `PageResult`를 반환하고 컨트롤러가 `PaginationResponse.from(...)`으로 감쌌다.
  3. 도메인 enum: 타입 성격 술어 `isEnum()`에 `DOMAIN_ROOT` 패키지 조건을 걸어 열었다. 도메인 enum 76개가 애그리거트 루트와 같은 `..model` 패키지에 살아서 패키지 술어로는 `Shop`·`Order`까지 열리기 때문이다.
- 짝 규칙 `apiModuleShouldOnlyReadDomainEnums`가 도메인 enum에 호출할 수 있는 메서드를 `ALLOWED_DOMAIN_ENUM_ACCESSORS`(`name`·`getDescription`·`getDisplayName`)로 제한했다. 바이트코드 실측(admin-api 기준 `name` 57 · `getDescription` 8 · `getDisplayName` 1)에서 도출했고 "항목을 추가하지 않는다"고 봉인했다.
- `domainEnum()` 술어는 `domain.exception..`을 제외했다. `ErrorCode`가 enum이라 짝 규칙이 전역 핸들러의 `getCode()`·`getDefaultMessage()` 호출을 잡았기 때문이다.
- `domainBoundaryPredicatesShouldStillBite`는 carve-out 술어가 무력해지지 않았는지 상시 단정했다(애그리거트 루트 `Shop`은 여전히 금지, `MemberGrade`는 carve-out 대상, enum과 애그리거트 루트가 같은 패키지에 산다는 전제 등 4항목).
- `controllersShouldBeDomainFree`도 같은 carve-out을 공유했다.
- enum carve-out의 근거: 실측 위반 67건이 전부 읽기 전용 accessor였고 도메인 객체 생성·상태 변경·리포지토리 접근은 0건이었다. 규칙의 원래 의도는 승격 방향(String·Long → 도메인 타입)을 막는 것인데 옮겨진 것은 강등 방향이고, ArchUnit 의존 그래프는 두 방향을 구분하지 못한다고 보았다. 다만 도메인 enum 76개 중 13개가 비즈니스 로직을 노출하므로(`MemberGrade#fromReviewCount`·`OrderStatus#canTransitionTo`) 타입 수준 carve-out만으로는 부족해 짝 규칙을 두었다.
- `domainEnum()`이 `domain.exception..`을 제외한 것은 짝 규칙이 web-api 전역 핸들러의 `ErrorCode` 호출을 실측 2건 잡았기 때문이며, 에러 계약은 클래스 수준 규칙에서도 carve-out된 횡단 관심사라 두 규칙이 같은 예외를 공유해야 한다고 보았다.
- 짝 규칙 아래에서는 api 모듈의 도메인 enum `switch`가 금지였다. `switch`는 컴파일 시 `ordinal()`/`values()` 호출로 낮아지는데 그 둘이 `ALLOWED_DOMAIN_ENUM_ACCESSORS` 밖이라 ArchUnit 위반이 됐고, 소스에 위반 메서드명이 없어 원인을 찾기 어려웠다.

### backend/CLAUDE.md — 도메인 enum 경계 규칙 / DTO 조립 규칙

- 아웃바운드(강등) 방향은 챕터 06·07로 정해졌다. 읽기 계약 `*Result`가 품은 도메인 enum을 api 모듈의 Response가 `.name()`으로 읽어 `String`으로 내리는 것이 정상 경로였다. 비즈니스 로직 메서드(`MemberGrade#fromReviewCount`·`OrderStatus#canTransitionTo` 등) 호출은 금지였다.
- Response record는 `com.tastyhouse.domain.*`를 import하지 않지만, `*Result`가 품은 도메인 enum의 읽기 accessor 호출(`result.type().name()`)은 accessor 3종 범위에서 허용했다.

### backend/CLAUDE.md — 앱 마커 규칙

- `@SharedApp`은 리스너 전용이었다. `LayerRulesTest#sharedAppOnlyOnListeners`가 `..listener..`의 `@TransactionalEventListener` 보유 클래스에만 허용했다. "리스너 외에는 달지 않는다"가 규칙이었다.

### backend/api-common-module/AGENTS.md — Key Files / Internal / `GlobalExceptionHandler`

- 내부 의존은 `domain` 하나였고 `api`로 노출했다. 이 노출을 타고 `domain.exception`(`BusinessException`·`ErrorCode`)이 api 3모듈의 공용 에러 계약이 됐다.
- `BusinessException`은 전용 메서드 `handleBusinessException`이 `getErrorCode()`의 상태·code와 `getMessage()`로 처리했다. 권한 거부는 `ErrorCode.ACCESS_DENIED`를 썼다.

### backend/{web,admin,ceo}-api/AGENTS.md — Internal / Working In This Directory

- 각 api 모듈은 `domain`을 선언하지 않았다. domain 타입이 다시 필요해져도 `api-common-module`의 `api project(':domain')` 전이 노출로 해결됐다. web-api `GlobalExceptionHandler`가 쓰는 `domain.exception..`도 이 경로였다.
- web-api는 모듈 전체가 domain-free이되 공용 에러 계약 `domain.exception..`만 carve-out이라고 적었고, 401 처리가 그 사례였다. Response record는 도메인 enum에 carve-out된 accessor(`name`·`getDescription`·`getDisplayName`)만 호출했다.
- admin-api는 carve-out 3종을 명시했다(챕터 06 페이징, 챕터 07 enum accessor).

### backend/batch-module/AGENTS.md — Internal

- batch는 `api-common-module`을 의존하지 않아 domain 전이 경로가 없었다. web/admin/ceo와 달랐다.

### backend/infrastructure/jpa/AGENTS.md — `<ctx>/query/` 도입부

- 반환 페이징 타입은 domain의 `shared/page/PageResult`, 페이징 입력은 `shared/page/PageQuery`였다.

### backend/web-api/src/main/java/com/tastyhouse/webapi/AGENTS.md — Cross-cutting Packages / Common Patterns

- 서비스가 `{Ctx}QueryPort`로부터 받는 `PageResult<T>`는 domain `shared/page` 소속이었고, 이를 `PaginationResponse.from(...)`으로 변환했다.

### backend/application-core/AGENTS.md — 표현 계약이 만들 수 없는 값은 이 모듈이 `*View`/`*ViewResult`로 넘긴다

- 순수 표현 파생(16자리 리뷰번호 0-pad, 문구 표시명 truncate, 거리별 배달팁 비움 판정)과 함께 enum → 문자열 강등도 Response로 내렸다.

### backend/security-core/AGENTS.md — Key Files / Working In This Directory / Internal

- `build.gradle`이 `domain`을 `implementation`으로 의존했고, 그 이유는 토큰 검증 실패를 `com.tastyhouse.domain.exception.ErrorCode`로 표현하기 위해서라고 적혀 있었다. 실측 결과 소스의 `com.tastyhouse.domain..` 참조는 0건이었다.

### backend/security-module/AGENTS.md — Working In This Directory

- `domain` 의존의 유일한 이유는 `JwtAuthenticationEntryPoint`·`JwtAccessDeniedHandler`가 401/403 응답을 조립할 때 쓰는 `com.tastyhouse.domain.exception.ErrorCode` 참조였다(그 밖의 도메인 타입 참조 0건).

## 결정

표현 계층 전체가 domain을 컴파일 클래스패스에 두지 않는다. carve-out은 없다.

| 항목 | before | after |
|---|---|---|
| `api-common-module` 내부 의존 | `api project(':domain')` | `api project(':application-core')`(당시 `application`). `application`은 `domain`을 `implementation`으로만 가진다 |
| `security-module` / `security-core` | `security-module`은 domain 의존, `security-core`는 미사용 domain 의존 | `security-module`은 `application` 의존, `security-core`의 domain 의존 삭제 |
| presentation 컴파일 클래스패스의 `domain` | 있음(api 노출 경유) | 없음 |
| carve-out `domain.exception..` | 허용. 핸들러가 `BusinessException`·`ErrorCode`를 직접 다룸 | 소멸. 판정은 `application.shared.error.ErrorResponses#resolve`(→ `ErrorDescriptor`), 상수는 `ErrorContracts`(이후 [ADR-0042](0042-error-code-module-split.md)로 `ApiErrorCode`) |
| `BusinessException` 처리 메서드 | 전용 `handleBusinessException` | 없음. `@ExceptionHandler(Exception.class)` 폴백이 먼저 `ErrorResponses.resolve(e)`를 호출하고, 값이 있으면 그 status·code·message로 `ProblemDetail`, 없으면 기존 500 |
| carve-out `domain.shared.page..` | 허용. 컨트롤러가 `PageResult`를 감쌈 | 소멸. `PageQuery`/`PageResult`를 `com.tastyhouse.application.shared.port.out.page`로 이동 |
| carve-out 도메인 enum | 허용 + accessor 3종 제한 | 소멸. `*Result`의 enum 필드를 `String`(상수명)으로 바꾸고, 표시 문구는 `{field}Description`/`{field}DisplayName` 컴포넌트로 싣는다. enum 카탈로그는 `CodeLabelResult` |
| 강등 위치 | api 모듈 Response(`result.type().name()`) | DAO 투영(`x.status.stringValue()`) 또는 Result를 만드는 QueryService |
| ArchUnit (api 3모듈) | `apiModuleShouldOnlyReadDomainEnums`·`domainEnum()`·`ALLOWED_DOMAIN_ENUM_ACCESSORS`·`domainBoundaryPredicatesShouldStillBite` | 전부 삭제. `apiModuleShouldBeDomainModelFree`·`controllersShouldBeDomainFree`가 carve-out 없이 전면 금지 |
| ArchUnit (api-common·security-module) | 없음 | `LayerRulesTest#shouldNotDependOnDomain` 신설 |
| `@SharedApp` 허용 대상 | 리스너만 | 리스너 또는 `..config..` + `@Configuration`. 짝 규칙 `sharedConfigsShouldOnlyDeclareUnmarkedBeans` 신설 |
| 기타 이동 | admin 이벤트 상세의 파일 URL 누락 판정이 표현 쪽 | `EventManagementQueryService`로 이동(응답 동일) |

## 근거

- 전용 `BusinessException` 핸들러가 있는 한 표현 계층은 `com.tastyhouse.domain.exception`을 import해야 했다. 판정을 application의 정적 유틸로 옮겨야 마지막 고리가 끊긴다.
- `PageQuery`/`PageResult`는 domain 안에 사용처가 0건이었고, 소비자(persistence DAO 32곳·표현 계층)가 전부 application 쪽이라 domain이 소유할 이유가 없었다.
- enum carve-out은 "api 모듈이 도메인 로직을 수행하지 않는다"를 지키려고 accessor 목록과 짝 규칙, 술어 판별력 단정 테스트까지 필요로 했다. `*Result`가 `String`으로 도착하면 api 모듈이 도메인 enum을 호출할 일 자체가 없어지고, 지킬 술어도 사라진다.
- 컴파일 클래스패스에서 domain을 빼면 회귀는 컴파일부터 실패한다. ArchUnit 규칙은 누군가 의존 한 줄을 되돌리는 회귀를 잡는 2차 방어선으로 남는다.
- `@SharedApp` 확대: persistence `*DomainConfig`(4앱 공통)가 등록하던 빈을 application으로 옮기려면(덩어리 02/03a) 4앱 전부에 등록할 자리가 필요했다. 클래스에 마커를 직접 달면 앱 격리에 걸리고 스캔과 `@Bean`이 겹치므로, 마커 없는 POJO를 `@SharedApp @Configuration`의 `@Bean`으로만 등록하는 형태를 허용했다.

## 결과·감수한 손실

- HTTP wire 계약은 바뀌지 않았다(변경 전/후 jar 응답 diff로 확인).
- `security-core`는 `domain` 의존을 `application`으로 바꾸지 않고 삭제만 했다. `application`이 이미 `security-core`를 의존해 순환이 되기 때문이며, 같은 이유로 `ErrorContracts`도 쓸 수 없었다(필터 단계 응답 조립은 `security-module` 소유). `security-module`은 `implementation project(':application-core')`로 `ErrorContracts`의 `authRequired()`·`accessDenied()`(`ErrorCode.AUTH_REQUIRED`·`ACCESS_DENIED`의 미러)를 썼다. 표현 계층이 쓰던 `ErrorContracts` 상수는 `rateLimit()`·`accessDenied()`·`authRequired()`였다.
- `api-common-module`의 `handleUnexpected`는 `resolve`가 값을 주면 `warn("BusinessException [{}]: {}")` 후 응답을 만든다. `shouldNotDependOnDomain`을 위해 `api-common-module`에 테스트 의존 `archunit-junit5`가 신설됐다.
- `*Result`의 enum 필드를 `String`으로 바꿀 때 record 컴포넌트 정규 순서는 바꾸지 않았고, `{field}Description`/`{field}DisplayName` 컴포넌트는 그 필드 바로 뒤에 추가했다. application이 다시 enum이 필요하면 `DayType.valueOf(...)`처럼 서비스가 승격한다. 덩어리 01 당시 라벨은 DAO가 `EnumLabelProjection.labelOf(enumPath, Enum::getDescription)`로 채웠으나, 이후 [ADR-0028](0028-persistence-state-layer.md)(덩어리 03b)에서 `EnumLabelProjection`이 삭제되고 QueryService가 채우게 됐다.
- 함정: `Map<OrderMethod, ...>.get(dto.orderMethod())`처럼 `Object` 인자 API는 필드가 `String`이 된 뒤에도 컴파일되지만 항상 `null`을 돌려준다(검증 중 당시 `ShopQueryService`의 주문 방식 조회에서 실제 발견). `Map.get`/`containsKey`·`equals`·`Collection.contains`·AssertJ `isEqualTo`가 같은 부류이므로, Result의 문자열을 도메인 enum과 비교할 때는 먼저 `valueOf`로 승격한다.
- 예외 판정은 최상위 예외만 본다. cause를 추적하지 않으므로 기존 500은 그대로 유지된다.
- `domainBoundaryPredicatesShouldStillBite`는 대체 테스트 없이 삭제했다. carve-out 없는 전면 금지는 무력해질 여지가 없다고 보았다.
- api 모듈에서 도메인 enum `switch`는 이제 컴파일부터 불가능하다. "분기 판정은 application에서 한다"는 결론은 그대로다.
- 일부 대체: `ErrorContracts`는 [ADR-0042](0042-error-code-module-split.md)가 api-common의 `ApiErrorCode`로 대체했다. `@SharedApp @Configuration` 허용은 [ADR-0034](0034-service-config-removal.md)와 [ADR-0036](0036-app-marker-removal.md)을 거쳐 사라졌다(마커 자체가 없다). 표현 계층의 domain 절단은 현재도 유효하다.

## 영향받은 문서

- `backend/CLAUDE.md` · "도메인 enum 경계 규칙" · api는 도메인 enum을 만지지 않고 `*Result`가 `String`과 `{field}Description`/`{field}DisplayName`을 싣고 온다.
- `backend/CLAUDE.md` · "DTO 조립 규칙 (`new` 직접 호출 지양)" · Response record는 domain을 import하지 않고 enum도 호출하지 않는다.
- `backend/CLAUDE.md` · "예외·에러코드 소유 규칙 (`ErrorCodeSpec` 공통 계약 + `BusinessException` 단일 계층)" · 표현 계층은 `BusinessException`을 타입으로 잡지 않고 `ErrorResponses`로 판정한다.
- `backend/AGENTS.md` · "`apiModuleShouldBeDomainModelFree` — 모듈 전역 domain 금지" · carve-out 없이 `com.tastyhouse.domain..`을 전면 금지한다.
- `backend/AGENTS.md` · "3층 구조 봉인 — 컨트롤러·Request의 경계 규칙" · `controllersShouldBeDomainFree`도 carve-out이 없다.
- `backend/api-common-module/AGENTS.md` · "Key Files" / "Internal" · 내부 의존은 `application`(api)과 `security-core`이고 domain은 의존하지 않는다. `shouldNotDependOnDomain`이 회귀를 잡는다.
- `backend/api-common-module/AGENTS.md` · "`GlobalExceptionHandler` — web-api가 이 핸들러를 쓰지 않는 이유" · 전용 `BusinessException` 핸들러가 없는 것은 누락이 아니다.
- `backend/web-api/AGENTS.md`·`backend/admin-api/AGENTS.md`·`backend/ceo-api/AGENTS.md` · "Working In This Directory" / "Internal" · 모듈 전체가 domain-free이고 domain을 선언하지 않는다.
- `backend/batch-module/AGENTS.md` · "Internal" · presentation 전체가 domain을 컴파일 클래스패스에 두지 않는다.
- `backend/application-core/AGENTS.md` · "표현 계약이 만들 수 없는 값은 이 모듈이 `*View`/`*ViewResult`로 넘긴다" · enum → 문자열 강등은 DAO 투영 또는 QueryService가 한다.
- `backend/security-core/AGENTS.md` · "Working In This Directory" / "Internal" · 이 모듈은 domain을 의존하지 않는다.
- `backend/security-module/AGENTS.md` · "Key Files" · `build.gradle`이 domain 대신 `application`을 의존한다.
- `backend/infrastructure/jpa/AGENTS.md` · "enum 필드는 문자열로 투영한다 — 라벨은 QueryService가 채운다" · 페이징 타입은 `application.shared.port.out.page`의 `PageResult`/`PageQuery`이다.

# ADR-0010: api 모듈은 domain을 금지하되 domain.exception·domain.shared.page·enum 읽기 accessor 3종만 carve-out한다

- 상태: 대체됨(→ [ADR-0025](0025-presentation-domain-cut.md))
- 결정 커밋: `3bf8f9171` (2026-09-01) — enum accessor carve-out은 `787dd580f` (2026-09-01)
- 대체한 결정: —

## 맥락

컨트롤러는 domain-free였지만(`controllersShouldBeDomainFree`), 이 규칙은 `*ApiController` 접미어로, `requestRecordsShouldBeDomainAndInfraFree`는 `..request..` 패키지로 대상을 좁혔다. 그래서 api 모듈의 `config..`·`security..`·`exception..`이 **무검사 사각지대**였다. 실제로 `admin-api`의 `AdminSeeder`가 도메인 enum `AdminRole`을 import하고 있었다.

- **carve-out**: 금지 규칙에서 명시적으로 빼 준 예외 범위.

## 이전 규칙

api 모듈 전역에 domain 의존을 금지하는 규칙이 없었다. 컨트롤러·Request만 접미어·패키지 기준으로 검사했다.

## 결정

`apiModuleShouldBeDomainModelFree`(web/admin/ceo-api 3곳)를 신설해 모듈 전역에서 `com.tastyhouse.domain..`을 금지하고 아래 3종만 허용했다.

| carve-out | 허용 근거(당시) |
|---|---|
| `domain.exception..` | 계층 칸이 없는 횡단 관심사. `api-common-module`이 `api project(':domain')`로 공용 에러 계약을 전 모듈에 노출한다. 없으면 web-api `GlobalExceptionHandler`가 즉시 걸린다 |
| `domain.shared.page..` | 페이징 조립을 컨트롤러로 옮기며 정상 경로가 됐다(application이 `PageResult`를 반환하고 컨트롤러가 `PaginationResponse.from(...)`으로 감싼다) |
| 도메인 enum(`isEnum()` + `DOMAIN_ROOT`) | Response 조립을 컨트롤러로 올리며 `result.type().name()`처럼 enum을 읽는 것이 설계상 필연이 됐다. 짝 규칙 `apiModuleShouldOnlyReadDomainEnums`가 호출 가능 메서드를 `ALLOWED_DOMAIN_ENUM_ACCESSORS`(`name`·`getDescription`·`getDisplayName`)로 제한 |

| 항목 | before | after |
|---|---|---|
| 검사 범위 | `*ApiController`·`..request..`만 | api 모듈 전역 |
| 허용 | (무검사 영역 전부) | carve-out 3종 |
| `AdminSeeder` | `AdminRole.SUPER_ADMIN.name()` | 리터럴 `"SUPER_ADMIN"`(서비스가 `AdminRole.from`으로 승격·검증) |
| 시더 주입 | 구체 `AdminQueryService`/`CeoQueryService` | 인바운드 포트(`seedersShouldDependOnUseCasesOnly` 신설) |

## 근거

- **enum carve-out을 패키지 술어로 쓸 수 없다**: 도메인 enum 76개는 전부 `com.tastyhouse.domain.<ctx>.model`에 애그리거트 루트와 같은 자리에 있어, `resideInAPackage("..model..")`로 열면 `Shop`·`Order`까지 열린다. 그래서 위치가 아니라 타입 성격(`isEnum()`)으로 좁혔다.
- **타입 carve-out만으로는 이빨이 빠진다**: 도메인 enum 76개 중 13개가 비즈니스 로직을 노출한다(`MemberGrade#fromReviewCount`, `OrderStatus#canTransitionTo`). 짝 규칙이 accessor로 제한했다. accessor 목록은 바이트코드 실측(admin-api 기준 `name` 57·`getDescription` 8·`getDisplayName` 1)에서 도출했다.
- `domainEnum()` 술어는 `domain.exception..`을 제외했다 — `ErrorCode`가 enum이라 짝 규칙이 전역 핸들러의 `getCode()` 호출을 잡기 때문이다.
- `domainBoundaryPredicatesShouldStillBite`가 carve-out을 잘못 넓혀 규칙이 무력해지는 것을 상시 단정했다(애그리거트 루트 금지 유지 등 4항목).

## 결과·감수한 손실

- 위반은 import가 아니라 바이트코드 상수 풀로 잡힌다 — `*Result` 컴포넌트를 통한 전이 의존도 걸리므로 grep으로 검증하면 "위반 0건"으로 오판한다. 이 교훈은 지금도 유효하다.
- **대체**: 표현 계층 domain 절단 덩어리 01([ADR-0025](0025-presentation-domain-cut.md))로 carve-out 3종이 전부 소멸했다. `*Result`가 도메인 enum 대신 `String`(+ `{field}Description`/`{field}DisplayName`)을 싣고, 페이징 타입은 `application.shared.port.out.page`로 옮겨졌고, 에러 처리는 application·api-common의 계약을 쓴다. `ALLOWED_DOMAIN_ENUM_ACCESSORS`·`apiModuleShouldOnlyReadDomainEnums`·`domainEnum()`·`domainBoundaryPredicatesShouldStillBite`는 삭제됐고, `apiModuleShouldBeDomainModelFree`는 이름을 유지한 채 예외 없는 전면 금지가 됐다.

## 영향받은 문서

- `backend/AGENTS.md` · "계층 규칙 봉인 — api 앱 3종 공통" > "`apiModuleShouldBeDomainModelFree` — 모듈 전역 domain 금지" · 지금은 carve-out 없는 전면 금지
- `backend/AGENTS.md` · "계층 규칙 봉인 — api 앱 3종 공통" > "3층 구조 봉인 — 컨트롤러·Request의 경계 규칙" · 컨트롤러·Request는 domain-free
- `backend/AGENTS.md` · "계층 규칙 봉인 — api 앱 3종 공통" > "api 모듈에서 도메인 enum `switch`를 쓰지 않는다" · enum 분기는 application에
- `backend/CLAUDE.md` · "도메인 enum 경계 규칙" · HTTP 경계는 enum을 `String`으로 받고 응답도 `String`으로 받는다
- `backend/CLAUDE.md` · "인바운드 포트 ArchUnit 규칙 6종 (완전 매핑 컴파일 게이트)" > "규칙의 현재 위치 — 모듈 재편 후 인벤토리 (챕터 06 갱신)" · 현재 규칙 인벤토리
- `backend/admin-api/AGENTS.md` · "봉인·가드 목록" > "`seedersShouldDependOnUseCasesOnly` — 이 앱에만 있는 규칙" · "`AdminSeeder.seedSuperAdmin` — 시더는 도메인 enum을 참조하지 않는다 (검출 시점이 늦춰지는 대가 포함)"

# P2. ArchUnit 게이트 강화 — 교차 주입 금지·domain-free·순환 검사

## 배경

루트 `CLAUDE.md`는 "CommandService는 `..query..`를 주입하지 않고, QueryService는 write 포트를 주입하지 않는다 — **이 두 금지가 CQRS 분리를 실제로 지탱하는 지점**"이라고 선언하지만, 현재 ArchUnit(`LayerRulesTest`)은 이 규칙을 전혀 검사하지 않는다. 그 결과 교차 주입 위반 12건이 조용히 누적됐다. 컨트롤러 domain-free 규칙, 모듈 순환 검사도 게이트에 없다.

## 현재 게이트 실태

- `web-api/src/test/java/com/tastyhouse/webapi/architecture/LayerRulesTest.java` (admin/ceo/batch 동명 사본 4개) — 규칙 4개뿐:
  `applicationServicesShouldNotDependOnWebLayer`, `controllersShouldNotDependOnRepositories`, `shouldNotDependOnQuerydsl`, `shouldNotDependOnInfrastructurePersistence`
- domain-module·infrastructure-module·external-api·security-module·logging-module에는 ArchUnit 테스트/의존 자체가 없음.
- infra 접근 제어가 "persistence만 금지" negative rule이라, 신규 infra 서브패키지는 자동 허용됨.

## 현존 위반 (신규 규칙이 잡아내야 할 목록 — red 확인용)

**QueryService → write 포트 주입 (11개 파일)**:
- `web-api/.../shop/ShopQueryService.java:98-105` (ShopRepository, ShopDetailRepository, ShopBookmarkRepository 등 4개)
- `web-api/.../review/ReviewQueryService.java:71-73` (MemberFollowRepository, OrderProductRepository)
- `web-api/.../follow/FollowQueryService.java:34` (MemberFollowRepository)
- `web-api/.../member/service/MemberQueryService.java:38` (MemberRepository)
- `admin-api/.../shop/ShopQueryService.java:64-65`, `admin-api/.../member/MemberQueryService.java:40`, `admin-api/.../admin/AdminQueryService.java:24`
- `ceo-api/.../ceo/CeoQueryService.java:26`, `ShopIntroductionQueryService.java:26`, `ShopBusinessHourQueryService.java:27`, `ShopClosedDayQueryService.java:29`

**CommandService → query 주입 (1건)**: `web-api/.../review/ReviewCommandService.java:27-31,58-60` (MemberQueryDao + ProductQueryService)

**컨트롤러 domain import (1건)**: `web-api/.../payment/PaymentApiController.java:15` (`PaymentCancelCode`)

## 작업 지시

1. 4개 api 모듈의 `LayerRulesTest`에 규칙 추가:
   - `commandServicesShouldNotDependOnQueryDaos`: `*CommandService` → `..infrastructure..query..` 및 `*QueryService` 의존 금지
   - `queryServicesShouldNotDependOnWritePorts`: `*QueryService` → `com.tastyhouse.domain..repository..` 의존 금지
   - `controllersShouldBeDomainFree`: `*ApiController` → `com.tastyhouse.domain..` 의존 금지 (단, `domain.shared.page.PageResult`는 현재 QueryService 반환 관행상 필요 여부 확인 후 예외 결정)
   - `requestResponseRecordsShouldBeDomainAndInfraFree`: `..request..`/`..response..` 패키지 → `com.tastyhouse.domain..`(단 `PaginationResponse`의 `PageResult` 예외는 CLAUDE.md에 문서화된 허용) 및 `com.tastyhouse.infrastructure..` 금지
2. **공허 통과 금지**: `allowEmptyShould(true)`를 쓰지 않는다(batch-module의 기존 CQRS 규칙 1건만 예외 유지 — CLAUDE.md 문서화됨).
3. **red 먼저 확인**: 신규 규칙이 위 현존 위반 13건을 실제로 잡는지 실행해 확인한다. 이것이 규칙이 공허하지 않다는 증명이다.
4. 위반 해소는 P3(ReviewCommandService)·P5(write 포트 유출 이관) 태스크 담당이다. **P3·P5가 완료되지 않은 시점이라면**, 위반 클래스를 ArchUnit `because(...)` + 명시적 예외 목록(FreezingArchRule 또는 클래스명 제외)으로 관리하고 각 예외에 `// TODO(P3)`/`// TODO(P5)` 주석을 남긴다. `@Disabled`로 규칙 전체를 끄지 않는다.
5. domain-module에 ArchUnit 테스트 신설(archunit testImplementation 추가): `com.tastyhouse.domain..`가 `org.springframework..`, `jakarta..`, `com.querydsl..`, `com.tastyhouse.infrastructure..`, `com.tastyhouse.{webapi|adminapi|ceoapi|batch}..`에 의존하지 않음을 강제. (루트 build.gradle의 spring 주입 문제 자체는 P10 담당 — 여기서는 게이트만.)
6. 선택(여유 시): infra 접근을 "query·listener만 허용" positive rule로 전환 검토, `slices().matching("com.tastyhouse.(*)..").should().beFreeOfCycles()` 추가 검토.

## 수용 기준

- [ ] 신규 규칙이 현존 위반 13건을 red로 잡는 것을 실행 로그로 증명 (또는 P3/P5 완료 후라면 green)
- [ ] `allowEmptyShould(true)` 신규 사용 0건
- [ ] domain-module 순수성 ArchUnit 테스트가 존재하고 통과
- [ ] 4개 api 모듈 LayerRulesTest 전량 통과(예외 목록 방식 포함)
- [ ] gradle build 대신 verify-without-gradle 절차로 테스트 실행

## 주의사항

- 프로젝트 메모리 `archunit-vacuous-pass` 참고: 대상 0건 공허 통과가 과거 실제 문제였다.
- 규칙 클래스명·패키지 매칭은 CLAUDE.md의 "계층 판별은 패키지가 아니라 클래스명 접미어" 원칙과 일치시킬 것.
- ceo-api의 `ShopOwnershipValidator`(내부에 ShopRepository 보유)를 QueryService가 주입하는 **간접 위반**은 규칙으로 잡기 어렵다 — 직접 주입만 규칙화하고, 간접 건은 P5 이관 후 재평가한다는 주석을 남긴다.

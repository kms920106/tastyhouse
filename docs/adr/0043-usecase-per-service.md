# ADR-0043: CQRS 쌍 서비스를 유스케이스당 서비스 1개로 분리한다

- 상태: 채택
- 결정 커밋: `d0b32cfe1` (2026-10-08)
- 대체한 결정: —

## 맥락

- **CQRS**: 명령(상태 변경)과 조회를 다른 클래스로 나누는 방식입니다. 이 저장소는 트랜잭션 속성(`readOnly`)과 주입 대상을 클래스 단위로 일관되게 하려고 썼습니다.
- **인바운드 포트(UseCase 인터페이스)**: 컨트롤러가 호출하는 application 계층 인터페이스입니다(`<ctx>/port/in/`).

결정 전에는 `{web,admin,ceo}-application`의 서비스가 도메인마다 `{도메인}CommandService`/`{도메인}QueryService` 한 쌍이었습니다(합계 194개: web 47 · admin 59 · ceo 88). 이 한 쌍이 그 도메인의 연산을 전부 떠안았습니다. 예를 들어 web `ShopQueryService`는 714줄에 public 메서드가 20개였고, admin `ShopManagementCommandService`는 UseCase 37개를 혼자 구현했습니다(`docs/tasks/usecase-service-split/README.md`).

## 이전 규칙

### backend/CLAUDE.md — application 서비스 CQRS 분리 규칙 (`{도메인}CommandService`/`{도메인}QueryService`)

"`{앱}-application` 모듈의 서비스는 도메인당 `{도메인}CommandService`(쓰기)와 `{도메인}QueryService`(읽기) 두 클래스로 분리한다." 근거는 과거 단일 서비스가 명령과 조회를 한 클래스에 담아 트랜잭션 속성이 메서드마다 갈리고, 조회에도 write 포트가 주입돼 의존이 과했다는 것이었습니다. 포트는 애그리거트 단위 `CommandUseCase`/`QueryUseCase` 쌍([ADR-0005](0005-inbound-port-full-mapping.md))이었습니다. 예: `PaymentCommandUseCase`(메서드 6개) / `PaymentCommandService`.

명령/조회 판별은 서비스 클래스명 접미어로 했고, 가드도 규칙마다 `haveSimpleNameEndingWith("CommandService"/"QueryService")`를 썼습니다.

- **클래스명 접미어를 바꾸지 않는다**: "클래스명은 그대로 `{도메인}CommandService`/`{도메인}QueryService`를 유지한다 — 기존 ArchUnit 규칙들이 접미어로 대상을 매칭하므로 이름을 바꾸면 규칙이 조용히 대상을 잃는다"가 규칙이었습니다.
- **"도메인당 2개"는 상한이 아니라 이름 규칙**: 조회만 있는 도메인은 `QueryService`만, 쓰기만 있는 경로는 `CommandService`만 두었습니다.
- **worked example(ceo shop 영업시간)**: 포트는 연산 6개를 담은 `ShopBusinessHourCommandUseCase` 하나, 구현은 `ShopBusinessHourCommandService` 한 클래스, 조회는 `ShopBusinessHourQueryService`였습니다(지금은 `ShopBusinessHourOwnerCreateUseCase`/`ShopBusinessHourOwnerCreateService`·`ShopBusinessHourListQueryService` 등).
- **reference 당시 이름**: web `NoticeQueryService`·`FaqQueryService`(조회 전용), web `ReservationCommandService`(지금 `ReservationCreateService`).
- **가드의 접미어 절**: `coreShouldNotContainUseCasesOrOrchestrators`·`ServiceContextBoundaryTest#isStructuralDomainService`도 `*CommandService`/`*QueryService` 이름을 함께 봤습니다. `commandServicesShouldNotDependOnRequestRecords`의 대상도 `*CommandService`(접미어)였습니다.
- **규칙 수와 anchor**: application `LayerRulesTest`는 26개, `{web,admin,ceo}-api` `LayerRulesTest`는 각 14개였습니다. `RuleAnchorTest`는 `countSuffix` 기반으로 `commandServicesExist` ≥91 · `queryServicesExist` ≥100, `moduleUseCaseCounts` web ≥50 · admin ≥100 · ceo ≥95, `inboundPortsExist` ≥556을 검사했습니다.

### backend/CLAUDE.md — 컨트롤러 분할 기준 (하위 리소스별로 나눈다)

"남은 긴장(후속)"으로 `MemberScreenUseCase`·`MemberAuthCommandUseCase`·`ShopManagementQueryUseCase`가 메서드 7개를 넘고 분할 후 컨트롤러 여러 개가 공유해 per-operation 분해 대상이지만, 컨트롤러 분할을 application 계층을 건드리지 않는 범위로 진행해 남겨 두었다고 적혀 있었습니다.

### backend/AGENTS.md — Purpose / Working In This Directory / Common Patterns

- Purpose: "도메인당 `{도메인}CommandService`/`{도메인}QueryService` CQRS 쌍은 이제 api 모듈이 아니라 `{web|admin|ceo|batch}-application`이 소유"(예: 당시 `com.tastyhouse.application.notice.service.NoticeQueryService`)로 서술했습니다.
- Working In This Directory: "presentation의 도메인 결합 격리 — 도메인당 CQRS 서비스 쌍" 항목이 `{도메인}CommandService`(`@Transactional`, write 포트·도메인 서비스만 주입, 식별자만 반환)와 `{도메인}QueryService`(`@Transactional(readOnly = true)`, `{Ctx}QueryPort`만 주입)를 규정했습니다. 전환 도중에는 "(번복 진행 중) 쌍 서술은 전환이 끝나지 않은 컨텍스트에만 해당한다"는 단서가 붙었습니다.
- Common Patterns: "CQS: 쓰기 `{도메인}CommandService`(`@Transactional`) / 읽기 `{도메인}QueryService`(`@Transactional(readOnly = true)`)".

### backend/AGENTS.md — 계층 규칙 봉인 — api 앱 3종 공통

컨트롤러의 구체 서비스 주입 금지를 두 규칙이 축을 나눠 지켰습니다. `controllersShouldDependOnUseCasesOnly`는 접미어 기준(`*ApiController` ✗ `*CommandService`/`*QueryService`), `webAdaptersShouldNotDependOnApplicationServices`는 위치(`com.tastyhouse.application..service..`) 기준이었습니다. 위치 규칙이 `MemberApiController`/`MemberMeApiController`의 `MemberService` 파사드 직접 주입을 잡아내 `MemberScreenUseCase` 포트를 신설해 해소한 이력이 있었습니다.

### 앱 모듈 AGENTS.md

- `backend/web-api/AGENTS.md`·`backend/web-api/src/main/java/com/tastyhouse/webapi/AGENTS.md`: 컨트롤러는 도메인당 CQRS로 분리된 두 서비스(`{도메인}CommandService`+`{도메인}QueryService`)를 각각 주입했습니다.
- `backend/admin-api/AGENTS.md`·`backend/ceo-api/AGENTS.md`: "도메인당 CQRS 분리(Command/Query 서비스 한 쌍)". admin reference는 `NoticeCommandService`·`NoticeManagementQueryService` 한 쌍이었습니다.
- `backend/ceo-api/AGENTS.md` Testing Requirements: `controllersShouldDependOnUseCasesOnly`가 대상 쪽 접미어 `*CommandService`로 컨트롤러 의존을 검사했습니다.

## 결정

유스케이스 하나 = 인바운드 포트 하나 = 서비스 하나입니다. CQRS(명령/조회 구분, 트랜잭션 속성, 교차 주입 금지, 명령은 식별자만 반환)는 유지합니다.

| 항목 | before | after |
|---|---|---|
| 서비스 입도 | 도메인당 `{도메인}CommandService` + `{도메인}QueryService` 한 쌍 | 유스케이스 하나 = 포트 하나 = 서비스 하나 (194개 → 582개) |
| 명령 포트·서비스 | `PaymentCommandUseCase`(메서드 6개) / `PaymentCommandService` | `{도메인}{동작}UseCase` / `{도메인}{동작}Service` — 예: `PaymentConfirmUseCase` / `PaymentConfirmService` |
| 조회 포트·서비스 | `PaymentQueryUseCase`(메서드 4개) / `PaymentQueryService` | `{도메인}{관점}QueryUseCase` / `{도메인}{관점}QueryService` — 예: `PaymentDetailQueryUseCase` / `PaymentDetailQueryService` |
| 명령/조회 판별 | 서비스 클래스명 접미어 | 구현한 포트 이름이 `QueryUseCase`로 끝나는가 (testFixtures `UseCaseServices#commands()`·`#queries()`) |
| 트랜잭션 | 클래스 레벨(일부 메서드 레벨) | 클래스 레벨. 원본에 클래스 레벨이 없던 12개는 메서드별 원래 속성 유지 |
| 공유 헬퍼 | 서비스의 private 메서드 | 한 연산 전용이면 그 서비스로, 순수 변환이면 대상 record의 `from(...)`, 포트가 필요하면 같은 패키지의 `{도메인}{명사}Reader` |
| 1:1 강제 가드 | 없음("포트를 최소 1개 구현"만) | 신설 4개: `useCaseServicesShouldImplementExactlyOneUseCase` · `useCasesShouldDeclareSingleOperation` · `useCaseServiceNameShouldMatchPort` · `useCaseServicesShouldHaveSinglePublicOperation` |
| api 모듈 가드 | `controllersShouldDependOnUseCasesOnly`(접미어) | 삭제 — 패키지 기반 `webAdaptersShouldNotDependOnApplicationServices`가 실제 가드 |

## 근거

- 『만들면서 배우는 클린 아키텍처』의 헥사고날 형태(`SendMoneyService implements SendMoneyUseCase`)를 따릅니다(`docs/tasks/usecase-service-split/README.md`).
- 한 쌍이 도메인의 연산을 전부 떠안아 클래스가 비대해졌습니다(위 맥락의 714줄·UseCase 37개 사례).
- 접미어는 이름 규칙일 뿐이라 판별 근거로 약합니다. 구현한 포트 종류로 판별하면 이름을 지키지 않아도 분류가 정확합니다.

## 결과·감수한 손실

- 동작 변경은 없습니다(4앱 빈 집합 diff·api-docs 경로 수 동일). 메서드 본문·예외·에러코드·트랜잭션 속성은 그대로 옮겼습니다.
- web `MemberService` 파사드는 트랜잭션 없는 조립 서비스 4개로 해체됐습니다. ceo 도메인 서비스 `ShopRequestCancelService`는 `ShopRequestCancellationService`로 개명됐습니다(도메인 서비스는 명사형, 유스케이스 서비스는 동사형).
- 오버로드는 이름으로 나눕니다(예: `PaymentDetailQueryUseCase`/`PaymentDetailByIdQueryUseCase`).
- 1:1 분리는 인바운드(`port.in`)에만 적용합니다. 아웃바운드 `port.out`과 `*QueryAdapter`는 대상이 아닙니다.
- batch는 대상이 아니었습니다 — `*SchedulerService` 7개가 이미 UseCase 1개 = 서비스 1개였습니다.
- anchor(`RuleAnchorTest`)는 실측값으로 올렸습니다(`commandServicesExist` ≥314 · `queryServicesExist` ≥268, `inboundPortsExist` ≥891).
- 규칙 수는 application `LayerRulesTest` 26 → 28, `{web,admin,ceo}-api` `LayerRulesTest` 각 14 → 13이 됐습니다. 신설 1:1 규칙 4개는 대상 하한 582를 스스로 단정하고 각각 반증 probe로 실패를 확인했습니다.
- `moduleUseCaseCounts`는 web ≥187 · admin ≥207 · ceo ≥188(batch =7 · core =0 불변)이 됐습니다. ceo는 서비스 88개가 유스케이스 서비스 188개(명령 120 · 조회 68)가 됐습니다.
- 컨트롤러 분할 기준의 "남은 긴장"이 해소됐습니다. web `MemberScreenUseCase`·`MemberAuthCommandUseCase`는 사라졌고(예: `MemberLoginUseCase`·`MemberVerifiedPasswordUpdateUseCase`), admin `ShopManagementQueryUseCase`는 per-operation 조회 포트 17개(예: `ShopListManagementQueryUseCase`·`ShopDetailManagementQueryUseCase`)로 나뉘었습니다.
- `backend/CLAUDE.md` import 순서 예시의 `BannerCommandUseCase`는 분리 전 이름이지만 정렬 예시로서 그대로 둡니다(지금은 `BannerCreateUseCase` 등).
- split package(`com.tastyhouse.application.<ctx>.service`를 세 앱이 나눠 씀) 때문에 앱별 작업은 병렬이 아니라 순차로 진행했습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · application 서비스 CQRS 분리 규칙 (`{도메인}CommandService`/`{도메인}QueryService`) · 제목은 앵커 보존용, 정본은 유스케이스당 서비스 1개 표
- `backend/CLAUDE.md` · 인바운드 포트 ArchUnit 규칙 6종 (완전 매핑 컴파일 게이트) · 1:1 규칙 4종과 판별 술어
- `backend/CLAUDE.md` · 컨트롤러 분할 기준 (하위 리소스별로 나눈다)
- `backend/AGENTS.md` · Purpose / Working In This Directory · 유스케이스당 서비스 1개
- `backend/{web,admin,ceo}-api/AGENTS.md` · Purpose / Working In This Directory · 컨트롤러는 연산마다 그 UseCase를 주입
- `backend/web-api/src/main/java/com/tastyhouse/webapi/AGENTS.md` · Working In This Directory
- `backend/admin-application/AGENTS.md` · 무엇이 여기 사는가
- `backend/application-core/AGENTS.md` · 유스케이스 서비스 1:1 규칙 4종 — 포트 하나 = 연산 하나 = 서비스 하나 (유스케이스 분리)

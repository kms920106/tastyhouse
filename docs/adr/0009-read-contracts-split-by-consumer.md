# ADR-0009: 읽기 계약을 소비 앱 수로 나눠 다중 앱 공유분은 domain이, 단독분은 그 앱 모듈이 소유한다

- 상태: 대체됨(→ [ADR-0015](0015-shared-read-contracts-in-application.md))
- 결정 커밋: `2b583d0c2` (2026-08-30)
- 대체한 결정: [ADR-0007](0007-application-common-module-read-ports.md)

## 맥락

[ADR-0008](0008-per-app-application-modules.md)로 application 계층이 앱별 모듈 4개로 나뉘자, 읽기 계약(`{Ctx}QueryPort`·`*Result`·`*SearchCondition`)을 어느 모듈이 소유할지가 문제가 됐다. 한 앱만 쓰는 계약은 그 앱 모듈에 두면 되지만, 여러 앱이 함께 쓰는 계약을 어느 한 앱 모듈에 주면 나머지 앱이 그 모듈을 의존해야 한다.

- **앱 간 수평 의존**: 같은 계층의 web 모듈이 admin 모듈을 의존하는 식의 옆방향 의존. 앱 경계가 무너진다.
- **split package**: 같은 자바 패키지를 여러 Gradle 모듈이 나눠 갖는 상태.

## 이전 규칙

[ADR-0007](0007-application-common-module-read-ports.md) — 읽기 계약 전부를 전용 모듈 `application-common-module`이 소유한다.

## 결정

| 항목 | before | after |
|---|---|---|
| 2개 이상 앱이 쓰는 계약(당시 50개: Result 44·QueryPort 5·SearchCondition 1, 이후 55개) | `application-common-module` | `domain`(git mv) |
| 한 앱만 쓰는 계약 | `application-common-module` | 그 앱의 `{앱}-application`(`a096c5e13`·`99a3d020b`·`45de28c1c`) |
| 패키지 | `com.tastyhouse.application..port.out` | 불변 — split package(5모듈) |
| `application-common-module` | 존재 | 삭제(`45db4cbbd`, 모듈 18 → 17) |
| 가드 | — | `ReadContractPurityTest`(domain)·`ReadContractSingleOwnerTest`(persistence)·`RuleAnchorTest` 소유 모듈 필터 |

## 근거

당시 `backend/CLAUDE.md` 원문: "포트를 소비자별로 쪼개도 여러 앱이 함께 쓰는 메서드와 `*Result`가 남는다. 그것을 어느 한 앱의 application 모듈에 주면 나머지 앱이 그 모듈을 의존해야 한다 — **앱 간 수평 의존**이다. `domain`은 이미 4개 앱이 전부 의존하므로 소유자를 여기로 옮기면 **새 의존 간선이 하나도 생기지 않는다.**" 근거는 이 하나뿐이었고 그 시점에는 타당했다.

공유 계약 50개는 도메인 enum·`Amount` VO만 참조하고 애그리거트 참조가 0건이라, "표현성 투영 184개가 순수 도메인에 섞인다"는 과거 기각 사유에 해당하지 않는다고 판단했다(커밋 메시지).

## 결과·감수한 손실

- 패키지를 바꾸지 않아 소비 측 import와 ArchUnit 패키지 규칙은 무변경이었다. 동작 변경 없음.
- split package 5모듈과 그것을 지키는 가드 3종이라는 부채가 생겼다.
- 공유분 55개에 한해 "표현용 투영이 순수 도메인에 섞인다"는 우려가 되살아났다.
- **대체**: application 모듈 통합([ADR-0013](0013-application-module-merge.md))으로 "어느 한 앱의 모듈"이 사라져 근거가 소멸했고, 공유 계약 55개는 `application`으로 돌아왔다([ADR-0015](0015-shared-read-contracts-in-application.md)). 소비 앱 수를 세던 판정 절차도 함께 폐기됐다.

## 영향받은 문서

- `backend/CLAUDE.md` · "query DAO·QueryPort·Result DTO·SearchCondition 소유 규칙 (읽기 계약은 전부 `application-core`가 소유, 구현은 `infrastructure:jpa`)" · 읽기 계약은 소비 앱 수와 무관하게 전부 `application` 소유
- `backend/application-core/AGENTS.md` · "읽기 계약을 이 모듈이 소유한다" · 현재 소유 규칙
- `backend/domain/AGENTS.md` · "Working In This Directory" · domain에는 모델·VO·이벤트만 둔다
- `CLAUDE.md` · "리포지토리 구조" · 읽기 계약은 `application`이 소유

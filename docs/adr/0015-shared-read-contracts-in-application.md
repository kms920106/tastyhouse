# ADR-0015: 공유 읽기 계약 55개를 domain에서 application으로 되돌린다(챕터 04)

- 상태: 채택
- 결정 커밋: `b5ad5e5be` (2026-09-04)
- 대체한 결정: [ADR-0009](0009-read-contracts-split-by-consumer.md)

## 맥락

[ADR-0009](0009-read-contracts-split-by-consumer.md)는 2개 이상 앱이 쓰는 읽기 계약(`{Ctx}QueryPort`·`*Result`·`*SearchCondition`) 55개를 `domain`에 두었다. 근거는 하나였다 — 공유 계약을 어느 한 앱의 application 모듈에 주면 나머지 앱이 그 모듈을 의존하는 **앱 간 수평 의존**이 생긴다.

그런데 [ADR-0013](0013-application-module-merge.md)(챕터 01)이 앱별 application 모듈 4개를 `application` 하나로 합쳤다.

## 이전 규칙

### `backend/CLAUDE.md` — "다중 앱 공유 계약은 `domain`이 소유한다"

2개 이상의 앱이 함께 쓰는 읽기 계약은 `domain`에, 한 앱만 쓰는 계약은 그 앱의 application 모듈에 둔다. 새 계약은 소비 앱 수를 세어 소유 모듈을 정한다. 그동안 `com.tastyhouse.application.<ctx>.port.out`은 여러 모듈이 나눠 갖는 split package였고, `ReadContractPurityTest`(domain)·`ReadContractSingleOwnerTest`(persistence)·`RuleAnchorTest` 소유 모듈 필터가 지켰다.

당시 문서가 적은 원래 근거는 이것 하나였다 — "포트를 소비자별로 쪼개도 여러 앱이 함께 쓰는 메서드와 `*Result`가 남는다. 그것을 어느 한 앱의 application 모듈에 주면 나머지 앱이 그 모듈을 의존해야 한다(앱 간 수평 의존). `domain`은 이미 4개 앱이 전부 의존하므로 소유자를 여기로 옮기면 새 의존 간선이 하나도 생기지 않는다." 그 시점에는 타당했다.

### `backend/CLAUDE.md` — query DAO·QueryPort·Result DTO·SearchCondition 소유 규칙 (챕터 03/04 경위)

- 챕터 03까지는 `infrastructure:persistence`의 `<ctx>/query/` 패키지 하나가 `{도메인}QueryAdapter` + `@QueryProjection` Result DTO + `{도메인}SearchCondition`을 전부 소유했고, api 모듈이 그 DAO를 직접 주입했다.
- 챕터 04(읽기 경로 포트화)가 이를 역전하면서, infra → api 의존은 만들 수 없고(`LayerRulesTest#shouldNotDependOnApiModules`) domain에 두면 표현용 투영 184개가 순수 도메인에 섞이므로 계약 전용 모듈 `application-common-module`을 신설했다([ADR-0007](0007-application-common-module-read-ports.md)). 동기는 api 모듈이 인프라 구현 상세(DAO 클래스)를 컴파일 타임에 직접 알아야 했던 마지막 결합을 인터페이스 경계로 끊는 것이었다.

### `backend/AGENTS.md` — Purpose (`@QueryProjection` 폐지 항목)

Result가 이관된 계약 모듈을 "현재는 `{앱}-application`·`domain`"으로 적었다 — 공유분이 `domain`에 있던 시기의 표기다.

### `backend/AGENTS.md` — Purpose

조회 계약의 소유 모듈은 소비자 수로 갈린다 — 한 앱만 쓰면 그 앱의 `{앱}-application`, 2개 이상이 쓰면 `domain`이다.

## 결정

| 항목 | before | after |
|---|---|---|
| 공유 읽기 계약 55개 | `domain` | `application`(git mv) |
| 소유 판정 | 소비 앱 수를 센다 | 판정 없음 — 읽기 계약이면 `application`의 `com.tastyhouse.application.<ctx>.port.out` |
| split package | 있음 | 종료(당시 기준) |
| 가드 | `ReadContractPurityTest`·`ReadContractSingleOwnerTest`·`RuleAnchorTest` 소유 모듈 필터 | 삭제(`application` `RuleAnchorTest` 하한 227 → 282) |
| `domain`에 두는 것 | 모델·VO·write 포트·도메인 서비스 + 공유 계약 | 모델·VO·write 포트·도메인 서비스 |

## 근거

- **근거가 소멸했다**: 앱별 모듈이 하나로 합쳐지면서 "어느 한 앱의 모듈"이라는 것이 존재하지 않게 됐다. 공유 계약을 `application`에 두어도 의존할 다른 앱 모듈이 없으니 수평 의존이 생길 자리가 없다(원문 "왜 번복했나").
- 같은 모듈 안의 FQCN 중복은 컴파일 에러라 split package 가드가 필요 없다.
- `domain`이 "표현 투영의 쓰레기통"이 될 위험(계약 전용 모듈을 처음 만든 이유)이 공유분 55개에 한해 되살아나 있던 상태를 해소한다.

## 결과·감수한 손실

- 이동은 `git mv`뿐이었다 — 패키지 경로가 같아 소비자 import 변경 0건(persistence DAO·앱 QueryService·api Response record 전부 무변경), `build.gradle` 변경도 없었다. api 모듈은 그동안 `persistence → domain` 전이로 이 55개를 봤으나 이후 이미 선언돼 있던 `application` 의존(`implementation project(':application-core')`)으로 직접 본다.
- 계약 이름은 기존 명명(`Result` 접미어·`Management` 한정어 등)을 그대로 승계했다 — `View`로의 개명은 184개 개명 + 소비 99파일 연쇄 수정 비용 대비 이득이 없어 하지 않았다.
- 프레임워크-프리는 컴파일 게이트(domain) 대신 ArchUnit `LayerRulesTest#readContractsShouldBeFrameworkFree`가 강제한다 — `application`은 spring starter를 받아 컴파일 게이트가 없다.
- 이후 앱 마커 제거([ADR-0036](0036-app-marker-removal.md))로 `{web,admin,ceo,batch}-application` 4모듈이 다시 생겼지만 **이 결정의 결론은 그대로다** — `port.out`은 전부 코어 `application`에 두므로 앱 모듈끼리 계약을 공유하려고 서로를 의존할 일이 없다. 다만 `com.tastyhouse.application..` 패키지는 다시 5모듈이 나눠 쓰는 split package가 됐다.

## 영향받은 문서

- `backend/CLAUDE.md` · "query DAO·QueryPort·Result DTO·SearchCondition 소유 규칙 (읽기 계약은 전부 `application-core`가 소유, 구현은 `infrastructure:jpa`)" · 읽기 계약은 전부 `application`, 판정 절차 없음
- `backend/application-core/AGENTS.md` · "읽기 계약을 이 모듈이 소유한다" · 현재 소유 규칙
- `backend/AGENTS.md` · "Purpose" · "Module Dependency Graph" · 읽기 계약은 전부 `application`
- `backend/ceo-api/AGENTS.md` · "Purpose" · ceo가 쓰는 읽기 계약의 위치
- `CLAUDE.md` · "리포지토리 구조" · 읽기 계약 소유

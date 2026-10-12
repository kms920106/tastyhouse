# ADR-0007: 읽기 계약을 application-common-module에 모으고 application은 QueryPort로만 읽는다

- 상태: 대체됨(→ [ADR-0009](0009-read-contracts-split-by-consumer.md))
- 결정 커밋: `c50e39780` (2026-08-29)
- 대체한 결정: —

## 맥락

- **읽기 계약**: 화면 표시용 조회의 인터페이스와 입출력 DTO — `{Ctx}QueryPort`(인터페이스)·`*Result`(결과 record)·`*SearchCondition`(검색 조건 record).
- **query DAO**: 그 조회를 QueryDSL로 구현하는 infrastructure 클래스.

챕터 03까지는 `infrastructure:persistence`의 `<ctx>/query/` 패키지 하나가 `{도메인}QueryDao` + `@QueryProjection` Result DTO + SearchCondition을 전부 소유했고, api 모듈의 `{도메인}QueryService`가 그 DAO 구현체를 **컴파일 타임에 직접 주입**했다. api 모듈이 인프라 구현 상세(DAO 클래스)를 알아야 하는 마지막 결합이었다. 계획은 `01d6d7494`에서 먼저 예고됐다.

## 이전 규칙

### `backend/AGENTS.md` — api 모듈이 `infrastructure-module`을 `implementation`으로 의존하는 이유

`{도메인}QueryService`가 infra `<ctx>/query/`의 `{도메인}QueryDao`를 컴파일 타임에 직접 주입하기 때문이다(과거 `runtimeOnly` 은닉에서 변경). 대신 은닉은 의존 스코프가 아니라 ArchUnit이 담당한다 — `..infrastructure..persistence..`(write 어댑터)와 `com.querydsl..`은 금지이고 `..query..`만 허용된다. 의존 그래프에는 "`infrastructure-module (implementation)` ← `<ctx>/query` DAO를 직접 주입하므로 컴파일 타임 필요"로 적혀 있었다.

## 결정

| 항목 | before | after |
|---|---|---|
| Result·SearchCondition 위치 | infra `<ctx>/query/`(Result 189개·SearchCondition 20개) | 신설 프레임워크-프리 모듈 `application-common-module`, 패키지 `com.tastyhouse.application.<ctx>.port.out` |
| 조회 인터페이스 | 없음(DAO 구체 클래스 주입) | `{Ctx}QueryPort` 48개 신설, DAO가 implements |
| QueryService가 주입하는 타입 | DAO 구현체 | `{Ctx}QueryPort` 인터페이스 |
| api 모듈의 infra query import | 있음 | 0건 |
| Result 투영 | `@QueryProjection` | `Projections.constructor(XxxResult.class, ...)`(`@QueryProjection` 선언 0건) |

## 근거

- infra가 구현해야 하는 인터페이스는 api 모듈이 컴파일 타임에 봐야 하는데, infra → api 방향 의존은 만들 수 없고(`LayerRulesTest#shouldNotDependOnApiModules`), domain에 두면 **표현용 투영 184개가 순수 도메인에 섞인다**. 그래서 계약 전용 신규 모듈이 필요했다(원문).
- api 모듈 4개가 공유해야 하는 인터페이스라 어느 api 모듈에도 둘 수 없었다.

## 결과·감수한 손실

- `@QueryProjection` 폐지로 컴파일 게이트를 잃었다. Result record가 `public`이 아니거나 select 인자 개수·타입·순서가 생성자와 다르면 컴파일은 통과하고 호출 시점에 500이 난다. 이를 잡으려고 `ProjectionConstructorMatchingTest`를 신설했고, 전환한 89개 호출은 전환 전 인자 목록과 전수 대조했다(커밋 메시지).
- CQRS 교차 주입 예외가 8건에서 3건으로 줄었다.
- **대체**: "계약을 전용 모듈 하나에 모은다"는 배치는 곧 소비 앱 수로 계약을 나누는 방식([ADR-0009](0009-read-contracts-split-by-consumer.md))으로 바뀌었고, `application-common-module`은 `45db4cbbd`(2026-09-01)에서 삭제됐다(모듈 18 → 17). 지금은 읽기 계약 전부를 코어 `application`이 소유한다([ADR-0015](0015-shared-read-contracts-in-application.md)).
- QueryPort 포트화 자체(QueryService가 인터페이스를 주입하고 api가 infra를 모른다)는 현재 규칙이다.

## 영향받은 문서

- `backend/CLAUDE.md` · "query DAO·QueryPort·Result DTO·SearchCondition 소유 규칙 (읽기 계약은 전부 `application-core`가 소유, 구현은 `infrastructure:jpa`)" · 계약은 `application`의 `port.out`, 구현은 `infrastructure:jpa`
- `backend/CLAUDE.md` · "api 모듈 QueryDSL·infra 전면 금지 규칙 (ArchUnit 강제 — 챕터 04로 완료)" · api는 QueryPort만 주입
- `backend/AGENTS.md` · "Module Dependency Graph" · application이 읽기 계약을 보는 경로
- `backend/infrastructure/jpa/AGENTS.md` · "`<ctx>/query/` — read 어댑터 (CQRS query 측, 개정됨 — 읽기 경로 포트화)" · "읽기 계약 가드 2종은 이 모듈이 소유한다 (챕터 09 — `application-common-module`에서 이관)" · DAO가 QueryPort를 구현, `Projections.constructor` 가드
- `CLAUDE.md` · "리포지토리 구조" · 모듈 수와 읽기 계약 소유

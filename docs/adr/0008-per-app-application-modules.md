# ADR-0008: 앱별 application 계층을 api 모듈에서 떼어 {앱}-application 모듈 4개로 분리한다

- 상태: 대체됨(→ [ADR-0013](0013-application-module-merge.md))
- 결정 커밋: `c55520733` (2026-08-30)
- 대체한 결정: —

## 맥락

- **application 계층**: 유스케이스를 수행하는 서비스(`{도메인}CommandService`/`{도메인}QueryService`)와 UseCase 인터페이스가 사는 계층. 트랜잭션 경계를 소유한다.
- **thin adapter**: 컨트롤러·요청 DTO·설정만 갖고 로직은 갖지 않는 얇은 실행 모듈.

모듈 재편 프로그램 이전에는 각 api 모듈(`web-api`·`admin-api`·`ceo-api`·`batch-module`)이 자기 앱의 application 계층을 함께 담았다. 컨트롤러와 서비스가 같은 모듈에 있으니 계층 위반(서비스가 컨트롤러를 참조, application이 infra를 참조)을 ArchUnit이 사후에만 잡을 수 있었다.

## 이전 규칙

### `backend/admin-api/AGENTS.md` — Purpose

"이 모듈이 관리자 측 application 계층이다."

### `backend/ceo-api/AGENTS.md` — Purpose

"점주 유스케이스의 application 계층은 이 모듈이 직접 소유한다."

### `backend/batch-module/AGENTS.md`

배치 잡의 `<job>/application/{port/in,service}`가 `batch-module` 안에 있었다.

## 결정

batch(파일럿, `c55520733`) → web(`fbc301bd3`) → admin(`9612a64d3`) → ceo(`928178555`) 순으로 application 계층을 앱별 모듈로 물리 분리한다.

| 항목 | before | after |
|---|---|---|
| application 계층 위치 | 각 api 모듈 안 | `{web,admin,ceo,batch}-application` 4모듈(`com.tastyhouse.{앱}application`) |
| api 모듈 | 컨트롤러 + 서비스 | thin adapter(컨트롤러 + `request/` + 설정 + 부트스트랩) |
| application의 infra 의존 | 컴파일 클래스패스에 있음 | 없음 — `import com.tastyhouse.infrastructure...`가 컴파일 에러 |
| 모듈 경계 | 계층 1차원 | "계층 × 앱" 2차원 |
| 빈 배선(파일럿) | 앱 자체 스캔 | `BatchApplicationConfig` `@Import`(`InfrastructureModuleConfig` 선례) |

## 근거

- 계층 위반이 ArchUnit 사후 검출이 아니라 **컴파일 에러**가 된다(원문 "모듈 경계 원칙 (챕터 05 개정 — 2차원 경계)").
- infra를 컴파일 클래스패스에서 빼자 `spring-boot-starter-data-jpa`를 타고 전이로 들어오던 `spring-tx`가 드러났다 — 숨은 의존이 드러나는 것이 분리의 목적이라 infra 의존을 되돌리지 않고 `spring-tx`를 명시 선언했다(커밋 메시지).

## 결과·감수한 손실

- 규칙 8개 중 실제로 실패할 수 있는 것은 2개이고 나머지는 금지 패키지가 클래스패스에 없어 휴면이 됐다 — 계층 강제가 ArchUnit에서 `build.gradle`로 승격된 결과다. 커버리지 과대평가를 막으려고 AGENTS.md에 활성/휴면을 구분하고 `RuleAnchorTest`를 신설했다.
- 읽기 계약을 소비 앱 모듈에 나눠 두는 소유권 연쇄([ADR-0009](0009-read-contracts-split-by-consumer.md))가 뒤따랐다.
- **대체**: 앱 축 분리가 실제 컴파일 게이트를 주지 못한다는 판단으로 4모듈은 `application` 하나로 합쳐졌다([ADR-0013](0013-application-module-merge.md)). 지금 다시 `{앱}-application` 4모듈이 있지만, 그것은 계약을 코어에 두고 앱 전용 빈만 담는 다른 형태이며 [ADR-0036](0036-app-marker-removal.md)의 결과다.

## 영향받은 문서

- `backend/AGENTS.md` · "Purpose" · application 계층은 코어 + 앱 모듈, api 모듈은 thin adapter
- `backend/AGENTS.md` · "Module Dependency Graph" · application 계층 5개
- `backend/admin-api/AGENTS.md` · "Purpose" · 관리자 측 application 계층은 `admin-application`이 소유
- `backend/ceo-api/AGENTS.md` · "Purpose" · 점주 측 application 계층은 `ceo-application`이 소유
- `backend/batch-module/AGENTS.md` · "패키지 구조" · 스케줄러만 남은 batch-module
- `backend/CLAUDE.md` · "인바운드 포트(UseCase 인터페이스)를 도입한다 — 완전 매핑 전략" · 컨텍스트별 분할 금지는 계층별 분할 금지가 아니다

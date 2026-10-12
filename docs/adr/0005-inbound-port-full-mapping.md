# ADR-0005: 인바운드 포트(UseCase 인터페이스)를 도입한다 — 완전 매핑 전략

- 상태: 채택
- 결정 커밋: `01d6d7494` (2026-08-29)
- 대체한 결정: —

## 맥락

- **인바운드 포트(UseCase 인터페이스)**: 컨트롤러가 호출하는 애플리케이션 연산을 인터페이스로 선언한 것. 컨트롤러는 구현 서비스 대신 이 인터페이스를 주입받는다.
- **완전 매핑 전략**: 『만들면서 배우는 클린 아키텍처』 그림 8.3. 계층 경계마다 그 계층 전용 모델로 매핑한다(Request → Command, Result → Response).

이전에는 인터페이스 없이 컨트롤러가 구체 서비스를 직접 주입했다. 경계 위반은 ArchUnit이 사후에 잡았다. 완전 매핑 전환(컨트롤러의 도메인 타입 유입 청산)을 시작하면서 이 결정을 다시 열었다.

## 이전 규칙

### `backend/CLAUDE.md` — "인바운드 포트·컨텍스트별 모듈 분할은 도입하지 않는다 (재론 방지)"

경계를 강제하는 다른 선택지를 검토한 뒤 의도적으로 채택하지 않기로 했다.

- **인바운드 포트(UseCase 인터페이스)를 도입하지 않는다**: 도메인 서비스마다 UseCase 인터페이스를 두면 인터페이스가 약 128개 생기는데, 각각의 구현체가 하나뿐이고 소비자도 모듈당 하나뿐이라 다형성·교체 가능성이라는 인터페이스의 값을 전혀 얻지 못한다. 순수 보일러플레이트이며 "구현으로 점프하려면 한 단계 더 거쳐야 한다"는 탐색 비용만 남는다. (출력 포트는 다르다 — 구현이 인프라에 있고 도메인이 그것을 몰라야 하므로 의존 역전의 실익이 있다.)
- **컨텍스트별 모듈 분할을 하지 않는다**: 경계는 ArchUnit으로 충분하며 25개 모듈로 쪼개면 빌드 그래프·`settings.gradle`·의존 선언이 늘어난다.

### `backend/CLAUDE.md` — 인바운드 포트(UseCase 인터페이스)를 도입한다 — 완전 매핑 전략 채택 (번복 직후 문구)

- **입도 규칙(당시)**: 기본은 애그리거트 단위 `{도메인}CommandUseCase`/`{도메인}QueryUseCase` 쌍이고, 메서드 7개를 초과하거나 컨트롤러 2개 이상이 공유하는 대형 서비스만 per-operation(연산 하나당 인터페이스 하나)으로 분해한다. 처음부터 per-operation으로 쪼개면 과거 우려대로 순수 보일러플레이트가 된다는 이유였다. 이후 [ADR-0043](0043-usecase-per-service.md)으로 바뀌었다.
- **컨텍스트별 모듈 분할 금지의 근거(당시)**: 모듈 분할이 주는 강한 보장이 필요한 것은 모듈 간 방향 의존(domain ← infrastructure ← api)이고 그쪽은 이미 분리돼 있다.
- **단일 예외 `application-common-module`**: 읽기 경로 포트를 소유할 모듈 하나를 신설한 것이 이 금지의 유일한 예외였으나, 챕터 09에서 계약이 전부 소비 앱의 application 모듈로 옮겨가 모듈이 비면서 삭제됐다(공유분 55개는 잠시 `domain`을 거쳐 챕터 04에서 `application`으로 돌아왔다 — [ADR-0007](0007-application-common-module-read-ports.md)·[ADR-0015](0015-shared-read-contracts-in-application.md)).

## 결정

인바운드 포트를 도입한다. 과거 반대 논거(다형성 실익이 0에 가깝다)는 **사실로 인정**하고, 다른 근거로 뒤집는다.

| 항목 | before | after |
|---|---|---|
| 컨트롤러가 주입하는 타입 | 구체 서비스 클래스 | UseCase 인터페이스(`<ctx>/port/in/`) |
| 경계 강제 수단 | ArchUnit(사후 검출) | 컴파일 게이트(구체 서비스에 손대는 코드가 컴파일되지 않음) + ArchUnit |
| 입도(당시) | — | 애그리거트 단위 `{도메인}CommandUseCase`/`{도메인}QueryUseCase` 쌍. 메서드 7개 초과 또는 컨트롤러 2개 이상이 공유하는 대형 서비스만 연산당 인터페이스 1개 |
| 컨텍스트별 모듈 분할 | 하지 않음 | 여전히 하지 않음 |

제목에 "번복"을 남긴 이유는 폐기된 논거가 그럴듯해서 같은 논의가 다시 열리기 쉽기 때문이다(원문).

## 근거

1. **컴파일 게이트** — 컨트롤러가 `application/service/`의 구체 클래스를 아예 모르게 만든다. ArchUnit은 위반을 사후에 잡지만, UseCase 타입만 주입받으면 구체 서비스에 손대는 코드가 애초에 컴파일되지 않는다. 경계 강제 수단의 등급이 한 단계 올라간다.
2. **경계 계약의 문서화** — UseCase 인터페이스 한 파일이 그 애그리거트의 연산 계약(어떤 명령이 있고 무엇을 받아 무엇을 돌려주는가)을 한눈에 고정한다.

## 결과·감수한 손실

- 이후 `935b76bc1`·`da6163964`가 전 컨텍스트에 도입했다.
- 입도 규칙("쌍이 기본")은 이후 유스케이스당 서비스 1개로 바뀌었다([ADR-0043](0043-usecase-per-service.md)). 지금은 web·admin·ceo 전부 연산 하나당 포트 하나다. application `LayerRulesTest#useCasesShouldDeclareSingleOperation`이 포트당 추상 메서드 1개를, `#useCaseServicesShouldImplementExactlyOneUseCase`가 서비스당 포트 1개를 강제한다(batch 출처는 제외 — `*SchedulerService` 7개가 이미 1:1).
- 모듈 재편 프로그램(챕터 01~06)이 앱별 application 모듈 4개와 `infrastructure:persistence`/`infrastructure:redis` 재편을 하면서 모듈 경계가 "계층 × 앱" 2차원이 됐다. 분할 대상이 "컨텍스트"(25개, 수평)가 아니라 "계층 × 앱"(8개, 수직)이어서 모듈 폭증 우려는 현실화되지 않았다 — 모듈은 25개가 아니라 6개 늘었다. 계층 위반이 ArchUnit 사후 검출이 아니라 컴파일 에러가 되어, 컴파일 게이트 논거를 모듈 수준으로 한 단계 더 밀어붙인 셈이다.
- "컨텍스트별 모듈 분할 금지"는 계층별 분할 금지가 아니다 — 이후 앱별 application 모듈 분리([ADR-0008](0008-per-app-application-modules.md))·통합([ADR-0013](0013-application-module-merge.md))·재분리([ADR-0036](0036-app-marker-removal.md))가 이어졌지만 컨텍스트 경계는 여전히 domain의 `ContextBoundaryTest`가 담당한다.
- 서비스 간 협력에도 UseCase 인터페이스를 주입하게 된 것은 [ADR-0033](0033-usecase-interface-injection.md).

## 영향받은 문서

- `backend/CLAUDE.md` · "인바운드 포트(UseCase 인터페이스)를 도입한다 — 완전 매핑 전략" · 도입 근거 2가지와 현재 입도
- `backend/CLAUDE.md` · "인바운드 포트 ArchUnit 규칙 6종 (완전 매핑 컴파일 게이트)" · 컴파일 게이트를 지키는 규칙
- `backend/CLAUDE.md` · "application 서비스 CQRS 분리 규칙 (`{도메인}CommandService`/`{도메인}QueryService`)" · 현재 입도(유스케이스당 서비스 1개)
- `backend/AGENTS.md` · "계층 규칙 봉인 — api 앱 3종 공통" > "3층 구조 봉인 — 컨트롤러·Request의 경계 규칙" · 컨트롤러는 UseCase 인터페이스만 주입

# AI 작업 명령어 가이드

이 저장소에서 AI(Claude Code)에게 일을 시킬 때 **채팅창에 붙여넣는 표준 명령문**을 모아둔 문서입니다. 팀 공유용이며, 명령문을 바꿀 때는 이 파일을 고칩니다.

> **동작 규칙 자체는 여기 없습니다.** AI가 각 단계에서 무엇을 어떻게 해야 하는지는 루트 `CLAUDE.md`와 각 모듈·앱의 `AGENTS.md`에 있습니다. 이 문서는 "그걸 어떻게 시작시키는가"만 다룹니다.

---

## 전체 흐름

요구사항 문서 한 벌에서 배포 가능한 코드까지, 네 단계로 진행합니다.

```
1. 플랜 작성       /omc-plan      → docs/tasks/{backend,frontend,playwright}.md 생성
        ↓
2. backend 구현    /autopilot     → docs/tasks/backend.md 기준으로 구현
        ↓
3. frontend 구현   /autopilot     → docs/tasks/frontend.md 기준으로 구현
        ↓
4. 검증 + 자동수정  (대화형 세션)  → docs/tasks/playwright.md 시나리오 실행
                                   이슈 있으면 수정 세션 자동 호출 → 재검증 반복
```

**단계마다 세션을 새로 여는 것을 권장합니다.** 플랜·구현·검증은 요구되는 컨텍스트가 다르고, 한 세션에 몰아넣으면 앞 단계의 판단이 뒤 단계를 편향시킵니다. 특히 4단계 검증은 MCP Playwright가 필요해 세션 조건 자체가 다릅니다.

**2·3단계 순서는 지킵니다.** frontend가 호출할 API가 없으면 구현 검증이 불가능하므로 backend가 먼저입니다.

---

## 1. 플랜 작성

요구사항 문서(PDF·이미지·명세 등)를 분석해 구현 스펙 문서를 만듭니다.

```
/oh-my-claudecode:omc-plan
docs/tasks/request.pdf 문서를 분석하고 플랜 작성
```

**산출물** — `docs/tasks/` 아래에 고정된 파일명으로 생성됩니다.

| 파일 | 내용 |
|---|---|
| `backend.md` | 엔드포인트·요청·응답·인증·에러코드·스키마 변경 |
| `frontend.md` | 대상 앱·라우트·컴포넌트·폼 스키마·API 대응 |
| `playwright.md` | e2e 검증 시나리오 (검증 대상 화면 / 정상 플로우 / 예외 케이스) |
| `*.sql` | DB 스키마 변경이 있을 때 (직접 실행하지 않고 사람에게 적용 요청) |

해당 영역 변경이 없으면 그 문서는 만들지 않고, 무엇을 왜 생략했는지 보고에 나옵니다.

### 작업이 여러 기능으로 나뉘면 하위 폴더로 갈립니다

플랜 세션이 판단하기에 **독립적으로 구현·검증할 수 있는 기능이 여럿**이면, 덩어리마다 하위 폴더를 만들어 각각 문서 한 벌을 작성합니다.

```
docs/tasks/
  README.md                 ← 덩어리 목록·진행 순서·의존 관계 (먼저 읽으세요)
  {덩어리-slug}/
    backend.md  frontend.md  playwright.md  schema.sql
```

이때 **2~4단계 명령에 폴더 경로를 함께 넣고, 덩어리 하나씩 순서대로 진행합니다.**

```
/oh-my-claudecode:autopilot
docs/tasks/{덩어리-slug}/backend.md backend 구현
```

```
/oh-my-claudecode:autopilot
docs/tasks/{덩어리-slug}/frontend.md frontend 구현
```

```
docs/tasks/{덩어리-slug}/playwright.md 검증해줘.
이슈 있으면 자동 루프로 수정까지 진행해줘.
```

**진행 순서는 `docs/tasks/README.md`가 정합니다.** 뒤 덩어리가 앞 덩어리의 화면 위에 얹히거나, 주문 경로처럼 파급이 큰 변경이 뒤에 배치돼 있어 임의 순서로 착수하면 막힙니다.

검증에서 이슈가 나오면 이슈 파일도 그 덩어리 폴더 안에 만듭니다(`docs/tasks/{덩어리-slug}/playwright-issue-v1.md`).

> **파일명이 고정이라 새 플랜을 쓰면 기존 문서를 덮어씁니다.** `docs/tasks/*`는 `.gitignore` 대상이라 git 히스토리에도 없으니, 이전 작업 문서가 필요하면 미리 백업합니다.

---

## 2. backend 구현

```
/oh-my-claudecode:autopilot
docs/tasks/backend.md backend 구현
```

**스키마 변경이 있으면 이 단계 전에 SQL을 먼저 적용합니다.** 이 저장소에는 마이그레이션 도구가 없고 `ddl-auto: validate`라, 미적용 상태면 backend가 부팅 자체를 거부합니다.

```
Schema-validation: missing table [CEO_LOGIN_HISTORY]
→ APPLICATION FAILED TO START
```

DDL은 AI가 실행하지 않고 `docs/tasks/*.sql`로 작성만 하므로, **사람이 직접 DB에 적용**합니다. 접속 정보는 `backend/.env`에 있습니다.

---

## 3. frontend 구현

```
/oh-my-claudecode:autopilot
docs/tasks/frontend.md frontend 구현
```

**구현 세션은 브라우저를 열지 않습니다.** 화면을 만들어도 개발 서버를 띄우거나 Playwright로 조작하지 않고, `npm run build`도 돌리지 않습니다. 화면 동작 확인은 전부 다음 단계의 몫입니다.

---

## 4. 검증 + 자동 수정 루프

`docs/tasks/playwright.md`의 시나리오를 실제 브라우저로 실행합니다. **이슈가 나오면 AI가 수정 세션을 자동으로 띄우고 재검증까지 스스로 반복**하므로, 사람이 매 라운드 개입할 필요가 없습니다.

### 실행 전 확인

**MCP Playwright가 연결된 대화형 세션에서 실행합니다.** headless(`claude -p`) 세션에는 프로젝트 스코프 MCP 서버의 신뢰 승인이 적용되지 않아 `mcp__playwright__*` 도구가 아예 없고, 그 상태로는 검증이 "도구가 없어 못 함"으로 조용히 끝납니다.

도구가 안 보이면 `claude mcp list`로 `playwright` 상태를 확인합니다. `⏸ Pending approval`이면 최초 신뢰 승인 후 세션을 재시작합니다.

개발 서버·backend 기동은 AI가 알아서 하지만, **DB 스키마 적용만은 사람 몫**입니다(2단계 참조).

### 4-1. 기본 — 검증 + 자동 수정 (권장)

```
docs/tasks/playwright.md 검증해줘.
이슈 있으면 자동 루프로 수정까지 진행해줘.
```

| | 이슈 없을 때 | 이슈 있을 때 |
|---|---|---|
| 결과 | "이슈 없음" 보고 후 종료 | `playwright-issue-v1.md` 생성 → 수정 세션 자동 실행 → 재검증 → 필요하면 `v2`, `v3` |
| 사람 개입 | 없음 | 없음 (최대 3라운드) |

**루프가 멈추는 조건 셋:**

- 이슈가 없어짐 (정상 종료)
- 같은 이슈가 수정 후에도 재현 → 근본 원인 문제로 보고 후 중단
- 3라운드 초과 → 남은 이슈 보고 후 중단

### 4-2. 검증만 — 수정하지 않음

이슈 목록만 받고 수정은 나중에 하고 싶을 때입니다.

```
docs/tasks/playwright.md 검증만 해줘. 이슈 파일만 만들고 수정은 하지 마.
```

### 4-3. 특정 시나리오만

전체를 돌리기엔 오래 걸릴 때, `playwright.md`의 시나리오 번호(`N1`~, `E1`~)를 지정합니다.

```
docs/tasks/playwright.md 의 E1, E5, E10 시나리오만 검증해줘. 이슈 있으면 자동 루프로 수정까지 진행해줘.
```

### 4-4. 기존 이슈 파일만 수정 (검증 없이)

이미 만들어진 이슈 파일을 수정만 시킬 때입니다. 브라우저가 필요 없어 headless 세션에서도 됩니다.

```
/oh-my-claudecode:autopilot docs/tasks/playwright-issue-v1.md 에 기록된 이슈를 모두 수정해줘. 브라우저 검증은 하지 말고 코드 수정과 backend 빌드까지만 수행해줘.
```

### 검증 산출물

| 파일 | 언제 생기나 | 내용 |
|---|---|---|
| `docs/tasks/playwright-issue-v1.md` | 1라운드에서 이슈 발견 시 | 재현 절차 / 기대 동작 / 실제 동작 / 관련 파일·API / 추정 원인 / 조치 상태 |
| `playwright-issue-v2.md`, `-v3.md` | 재검증 라운드에서 또 나올 때 | 위와 동일. **이전 라운드 파일은 지우지 않습니다** — 같은 이슈가 반복되는지 대조하는 근거입니다 |
| `/tmp/playwright-fix-v{N}.log` | 수정 세션 실행 시 | 수정 세션이 무엇을 했는지 |

---

## 자주 겪는 상황

**"수정했다는데 같은 이슈가 또 나온다"**

backend 코드가 바뀌었는데 jar를 재빌드·재기동하지 않으면 수정 전 코드가 그대로 떠 있습니다. 자동 루프가 이걸 처리하지만, 수동으로 확인하려면 기동 시각과 jar 수정 시각을 비교합니다.

```bash
grep 'Started .*ApiApplication' /tmp/ceo-api.log   # 프로세스가 뜬 시각
ls -la backend/ceo-api/build/libs/                 # jar가 만들어진 시각
```

jar mtime이 더 나중이면 실행 중에 덮어쓴 것입니다. `내리기 → 빌드 → 다시 띄우기` 순서로 해소합니다.

**"검증 도중 요청이 타임아웃되는데 어떤 건 정상 응답한다"**

실행 중 재빌드로 톰캣 워커 스레드가 죽은 신호입니다. 루트 `CLAUDE.md`의 "backend가 간헐적으로 응답하지 않을 때" 절을 따릅니다.

**"화면은 뜨는데 목록이 비어 있다"**

신규 기능은 백필이 없어 정상적으로 빈 목록이 나옵니다. 버그가 아니며, `playwright.md`의 "테스트 데이터 준비" 표대로 데이터를 먼저 만들어야 합니다.

**"backend가 부팅되지 않는다"**

`Schema-validation` 오류면 DB 스키마 드리프트입니다. 에러에 찍힌 컬럼 하나만 고치지 말고 `schema.sql`과 실제 테이블을 통째로 대조합니다 — 하나 고치면 다음 컬럼에서 또 실패합니다.

---

## 참고 문서

| 문서 | 내용 |
|---|---|
| 루트 `CLAUDE.md` | 저장소 공통 규칙, 검증→수정 자동 루프의 상세 절차, backend 실행·빌드 규칙 |
| `backend/CLAUDE.md`·`backend/AGENTS.md` | backend 컨벤션 |
| `frontend/{admin,ceo}/CLAUDE.md` | frontend 앱별 컨벤션 |
| `docs/domain/{도메인}.md` | 주문·쿠폰·포인트·등급 등 도메인 비즈니스 규칙 |

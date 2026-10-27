# AI 작업 명령어 가이드

이 저장소에서 AI(Claude Code)에게 일을 시킬 때 **채팅창에 붙여넣는 표준 명령문**을 모아둔 문서입니다. 팀 공유용이며, 명령문을 바꿀 때는 이 파일을 고칩니다.

> **동작 규칙 자체는 여기 없습니다.** AI가 각 단계에서 무엇을 어떻게 해야 하는지는 루트 `CLAUDE.md`와 각 모듈·앱의 `AGENTS.md`에 있습니다. 이 문서는 "그걸 어떻게 시작시키는가"만 다룹니다.

---

## 전체 흐름

요구사항 문서 한 벌에서 배포 가능한 코드까지, 다섯 단계로 진행합니다.

```
1. 플랜 작성       /omc-plan      → docs/tasks/{backend,frontend,playwright}.md 생성
        ↓
2. backend 구현    /autopilot     → docs/tasks/backend.md 기준으로 구현
        ↓
3. frontend 구현   /autopilot     → docs/tasks/frontend.md 기준으로 구현
        ↓
4. 검증 + 자동수정  (대화형 세션)  → docs/tasks/playwright.md 시나리오 실행
                                   이슈 있으면 수정 세션 자동 호출 → 재검증 반복
        ↓
5. 리뷰 + 수정      /review-task   → 커밋 대기 변경을 스펙과 대조해 리뷰
                   /fix-review    → 지적을 판정해 필요한 것만 수정, docs/tasks/review-decisions.md에 기록
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

이때 **2~5단계 명령에 폴더 경로를 함께 넣고, 덩어리 하나씩 순서대로 진행합니다.**

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

## 5. 리뷰 + 수정

커밋하기 전에 대기 중인 변경(`git status` / `git diff`)을 리뷰하고, 지적을 처리합니다. 두 명령은 저장소의 `.claude/commands/`(`review-task.md`·`fix-review.md`)에 있는 프로젝트 슬래시 명령입니다. **리뷰 세션과 수정 세션은 나눕니다** — 지적한 세션이 스스로 고치면 자기 판단을 다시 검토하지 못합니다.

### 5-1. 리뷰

```
/review-task
```

기준 스펙을 지정하려면 인자로 넘깁니다(없으면 `docs/tasks/`에서 이번 변경과 맞는 `backend.md`·`frontend.md`를 찾습니다).

```
/review-task docs/tasks/{덩어리-slug}/backend.md
```

소스는 고치지 않고 심각도 순 지적 목록만 냅니다.

### 5-2. 지적 처리

리뷰 결과의 지적 목록을 그대로 인자로 붙여넣습니다.

```
/fix-review
{리뷰가 낸 지적 목록}
```

지적마다 코드를 직접 읽어 실제 결함인지 판정하고, 필요한 것만 고칩니다. 수정 안 한 지적은 이유를 보고합니다. backend를 고쳤으면 `./gradlew build`까지 돌리고, frontend 빌드·브라우저 검증은 하지 않습니다.

### 판정 기록 — `docs/tasks/review-decisions.md`

`/fix-review`는 지적마다 판정(수정함 / 수정 안 함)·근거·재지적 허용 조건을 이 파일 한 곳에 행으로 추가합니다. 스펙이 하위 폴더에 있어도 위치는 같고, 덩어리는 표의 칸으로 구분합니다. 덩어리 칸에는 `docs/tasks` 바로 아래 **최상위 폴더 이름**만 적습니다(`pg-confirm-recovery/07-review-followup-3`의 스펙이어도 `pg-confirm-recovery`) — 리뷰 후속 스펙이 새 하위 폴더로 생겨도 재지적 차단과 라운드 번호가 이어지게 하기 위해서입니다. 다음 `/review-task`는 이 파일을 먼저 읽어 **이미 판정된 지적을 다시 내지 않습니다**(대상 코드가 다시 바뀌었거나 허용 조건에 해당하면 예외).

> **이 기록은 로컬에만 있습니다.** `docs/tasks/*`가 `.gitignore` 대상이라 커밋되지 않으므로, 재지적을 막는 효과는 그 파일이 있는 PC의 세션에서만 생깁니다. 다른 사람이 같은 변경을 리뷰하면 기록 없이 처음부터 진행합니다. 작업 단위의 휘발성 산출물로 둔 것이 의도입니다.

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

## 결정 경위

이 문서가 소유한 규칙이 **왜 지금 모양이 됐는지**를 결정 단위로 적습니다. 본문은 현재 규칙만 담고, "예전에는 어땠고 왜 바뀌었는가"는 이 절에만 둡니다. 이 절은 항상 문서의 마지막 `##` 절이며, 항목은 번호 순입니다. 작성 규칙은 루트 `CLAUDE.md`의 "규칙 문서 개정 규칙" 절에 있습니다.

<a id="decision-0066"></a>

### 0066. 결정 전용 디렉터리(docs/adr)를 없애고 결정 경위를 소유 문서의 `## 결정 경위` 절로 흡수한다

- 상태: 채택
- 결정 커밋: `6f805ac70` (2026-10-27)
- 대체한 결정: —

#### 맥락

규칙 문서 본문에 지난 규칙과 바뀐 규칙이 취소선·이력 꼬리표로 겹겹이 쌓이자(2026-10-12 기준 43개 문서 663줄), 이력을 결정 단위로 걷어 `docs/adr/NNNN-{slug}.md` 파일 54개로 옮겼고, 이후 65개까지 늘었습니다. 본문은 현재 규칙만 담는다는 원칙은 효과가 있었지만, 결정 파일이 그 규칙을 소유한 문서와 떨어져 있어 문제가 생겼습니다.

- 같은 규칙의 현재 모습(`AGENTS.md`)과 경위(`docs/adr`)가 다른 디렉터리에 있어, 규칙을 고칠 때 두 곳을 함께 갱신해야 했습니다.
- 결정마다 파일·색인 행·상태 문구를 손으로 맞추는 비용이 들었습니다(색인과 파일의 "결정 커밋" 자리표시자가 어긋나는 리뷰 지적이 반복됨).
- `docs/domain`처럼 "주제 하나 = 문서 하나"로 찾는 다른 문서들과 구조가 달랐습니다.

#### 이전 규칙

결정 경위는 `docs/adr/NNNN-{slug}.md`에 결정 1건 = 파일 1개로 적고, `docs/adr/README.md`가 색인·작성 규칙·잔존 이력 검사 명령을 소유했습니다. 대체된 결정은 본문을 지우지 않고 상태만 바꿨습니다.

#### 결정

| 항목 | before | after |
|---|---|---|
| 결정 경위 위치 | `docs/adr/NNNN-{slug}.md` 65개 + `README.md` 색인 | 규칙을 소유한 `AGENTS.md`·`docs/domain/*.md`·`docs/workflow/README.md`의 마지막 `## 결정 경위` 절 |
| 번호·앵커 | 파일명 번호, 파일 경로 링크 | 번호 유지, `<a id="decision-NNNN">` 앵커 링크 |
| 대체된 결정 | 본문 전체 유지 | "결정" 절 요약(before/after 표)만 유지, 원문은 git 이력 |
| 영향받은 문서 절 | 결정마다 있음 | 삭제 — 결정이 소유 문서 안에 있으므로 불필요 |
| 색인 | `docs/adr/README.md` | 없음 — 번호는 `git grep 'id="decision-'`로 찾음 |
| 작성 규칙·잔존 검사 | `docs/adr/README.md` | 루트 `CLAUDE.md`의 "규칙 문서 개정 규칙" 절. 검사는 `## 결정 경위` 절을 제외 |
| 재생성 방지 | 없음 | `.claude/settings.json` PreToolUse 훅이 `docs/adr/` 쓰기를 차단 |

결정 65개의 소유 문서 배치는 다음과 같습니다.

| 소유 문서 | 결정 번호 |
|---|---|
| `backend/AGENTS.md` | 0005·0006·0008·0010·0013·0014·0016·0017·0018·0020·0022·0025·0029·0030·0034·0035·0036·0037·0038·0040·0042·0049 |
| `backend/application-core/AGENTS.md` | 0007·0009·0012·0015·0024·0026·0027·0033·0043·0046·0048·0054·0056·0057 |
| `backend/domain/AGENTS.md` | 0001·0041 |
| `backend/infrastructure/jpa/AGENTS.md` | 0002·0004·0028·0031·0032·0039·0044·0047 |
| `backend/infrastructure/persistence/AGENTS.md` | 0045·0052 |
| `backend/infrastructure/mybatis/AGENTS.md` | 0053 |
| `backend/infrastructure/mysql/AGENTS.md` | 0051 |
| `backend/infrastructure/file-storage/AGENTS.md` | 0021 |
| `backend/infrastructure/oauth/AGENTS.md` | 0050 |
| `backend/api-common-module/AGENTS.md` | 0003 |
| `backend/security-core/AGENTS.md` | 0011·0019·0023 |
| `docs/domain/payment.md` | 0055·0059·0060·0061·0062·0063·0064·0065 |
| `docs/domain/point.md` | 0058 |
| `docs/workflow/README.md` | 0066 |

#### 근거

- 규칙과 그 경위가 한 문서에 있으면 규칙을 고칠 때 경위도 같은 diff에서 갱신됩니다.
- 앵커를 `decision-NNNN`으로 고정하면 제목을 고쳐도 링크가 깨지지 않고, 번호는 기존 결정과 이어지므로 커밋 메시지·리뷰 기록의 번호 언급이 그대로 유효합니다.
- 결정 경위를 `CLAUDE.md`에 두지 않는 이유는 매 세션 자동으로 읽히는 파일이라 이력이 컨텍스트를 잡아먹기 때문입니다.

#### 결과·감수한 손실

- `docs/adr/README.md`의 "원문 표기 → ADR 대응표"(정리 전 이력 꼬리표로 검색하던 사람을 위한 표)와 전체 색인 표는 옮기지 않았습니다. git 이력에 남습니다.
- 대체된 결정 14개(0004·0007·0008·0009·0010·0011·0013·0014·0017·0026·0027·0028·0034·0045)는 맥락·근거·이전 규칙 절이 소유 문서에서 빠졌습니다. 각 항목의 "원문" 줄이 git 이력의 경로를 가리킵니다.
- 결정 경위 절이 붙으면서 소유 문서가 길어졌습니다(`backend/AGENTS.md`·`application-core/AGENTS.md`·`jpa/AGENTS.md`·`docs/domain/payment.md`). 절은 항상 문서 끝에 있으므로 현재 규칙을 읽는 데는 영향이 없습니다.
- `docs/tasks/`(커밋 대상 아님)의 옛 스펙·리뷰 기록에 남은 `docs/adr/...` 경로는 고치지 않았습니다.

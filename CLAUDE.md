# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

이 파일은 **리포지토리 전체에 적용되는 공통 규칙**을 담습니다. 각 영역의 고유 컨벤션은 아래 문서를 참조합니다.

- backend 고유 컨벤션: `backend/CLAUDE.md`, `backend/AGENTS.md` 및 각 모듈의 `AGENTS.md`
- frontend 고유 컨벤션: `frontend/{admin,ceo}/CLAUDE.md`(web은 고유 규칙이 없어 `frontend/web/AGENTS.md`), 각 앱의 `AGENTS.md` 및 `src/` 하위 디렉터리별 `AGENTS.md`

## AI 규칙

이 문서의 모든 경로와 명령 실행 위치는 **리포지토리 루트 기준**으로 표기합니다. 리포지토리 루트에는 `package.json`도 `gradlew`도 없으므로, 빌드·개발 서버·테스트 명령은 반드시 해당 앱/모듈 디렉터리(`frontend/{web,admin,ceo}` 또는 `backend`)로 이동한 뒤 실행합니다. 세션의 현재 작업 디렉터리가 루트가 아닐 수 있으므로, 명령 실행 전 현재 위치를 확인합니다.

명령어에 대한 답변은 한국어로 하도록 합니다.

명령된 로직을 구현 후, frontend 빌드 테스트(해당 앱 디렉터리의 `npm run build`)는 진행하지 않도록 합니다. backend는 실행 파일(jar)을 만들려면 빌드가 필요하므로 이 제한에서 제외하며, `backend` 디렉터리에서 `./gradlew build`를 실행합니다(아래 [개발 서버 포트](#개발-서버-포트) 참조).

나에게 무언가를 되물어 확인해야 할 때는, 자유 서술형으로 답을 요구하지 말고 **항상 선택지를 체크리스트(선택 가능한 항목 목록) 형태로 제시**하여 내가 키보드로 타이핑하지 않고 마우스 클릭만으로 답할 수 있게 합니다. 즉, 질문이 발생하면 가능한 답안들을 명확한 항목으로 나열해 그중에서 고르도록 하고, 여러 개를 동시에 고를 수 있는 질문이면 다중 선택이 가능함을 함께 알려줍니다.

**구현 세션은 브라우저 검증을 수행하지 않습니다.** frontend 화면/폼/플로우를 구현·수정했더라도 개발 서버를 기동하거나 MCP Playwright로 화면을 조작하지 않습니다. 브라우저 검증은 `docs/tasks/playwright.md` 시나리오 문서를 들고 **별도로 요청되는 검증 세션**이 담당합니다.

따라서 구현 작업의 Todo/체크리스트에 다음 항목을 **넣지 않습니다**.

- MCP Playwright 브라우저 검증
- 검증용 개발 서버·backend 앱 기동 및 종료
- 검증 결과 표 작성

플랜 문서나 스펙 문서의 구현 체크리스트에 "구현 후 MCP Playwright 검증 수행" 같은 항목이 남아 있더라도, 구현 세션에서는 이 규칙이 우선합니다. 해당 항목은 검증 세션의 몫이므로 수행하지 말고, 작업 완료 보고에 **"브라우저 검증은 별도 세션 대상"** 이라고만 밝힙니다.

## GIT 규칙

NO_COMMIT_OR_ROLLBACK

명령된 작업(리팩터링/기능 구현 등)이 끝나면, 커밋은 직접 실행하지 않되(NO_COMMIT_OR_ROLLBACK) **추천 커밋 메시지를 항상 함께 제시**합니다. 최근 커밋 로그(`git log`)의 컨벤션(`{type}({scope}): {한글 요약}` 형태, 예: `refactor(naming): ...`, `feat(point): ...`, `style(response): ...`)을 따르고, 본문은 무엇을 왜 바꿨는지(동작 변경 여부 포함) 한국어로 서술합니다. 신설 규칙이 있어 CLAUDE.md/AGENTS.md를 갱신한 경우 그 사실도 본문에 언급합니다.

사용자가 명시적으로 git commit 또는 git push를 요청한 경우에는, 현재 브랜치가 `prod`를 포함한 어떤 브랜치이든 별도로 확인 질문을 하지 않고 바로 진행합니다. 이 저장소는 현재 `prod` 브랜치를 기본으로 사용하며, 사용자가 커밋/푸시를 요청한 것 자체가 대상 브랜치에 대한 승인으로 간주합니다.

## 플랜 작성 규칙

작업 플랜을 작성할 때는, 이번 작업으로 인해 새로운 컨벤션이 생기거나 기존 규칙이 바뀌는지 확인하여 루트 및 각 모듈의 `CLAUDE.md`/`AGENTS.md` 문서도 갱신이 필요한지 함께 검토합니다.

브라우저 동작 확인이 필요한 frontend 작업(화면/폼/플로우 구현 및 수정)의 플랜을 작성할 때는, **e2e 검증 시나리오를 `docs/tasks/playwright.md` 별도 파일로 작성**합니다. 플랜 본문에 섞지 않고 파일을 분리하는 이유는, 이 문서를 나중에 검증 세션에 그대로 전달하기 위함입니다. 순수 백엔드/설정/리팩터링/문서 등 브라우저 조작이 필요 없는 작업은 이 항목이 면제됩니다. 시나리오는 다음을 포함합니다.

- **검증 대상 화면**: 이번 작업으로 신설·변경되는 화면/폼/플로우를 열거합니다.
- **정상 플로우**: 화면별 주요 정상 동작(생성/수정/조회/삭제 등)을 항목으로 구체적으로 기술합니다.
- **주요 예외 케이스**: 유효성 검증 실패, 권한/의존 데이터 부재, 빈 상태, 에러 토스트 등 확인해야 할 예외 상황을 함께 열거합니다.

이 시나리오는 **작성까지가 플랜 세션의 몫**입니다. 실제 수행은 검증 세션이 담당하므로, 플랜·구현 세션은 시나리오를 실행하지 않습니다(위 "AI 규칙" 참조).

### 플랜 산출물 문서 (docs/tasks)

`/plan`, `/oh-my-claudecode:plan`, `/oh-my-claudecode:omc-plan` 등 **plan 계열 명령으로 작업 플랜을 작성할 때는 아래 문서를 `docs/tasks/`에 작성**합니다. 파일명은 접두사 없이 고정입니다.

**이 파일명 규칙에는 예외가 없습니다.** plan 계열 명령을 받았으면 사용자가 파일명을 지정하지 않아도 아래 표의 이름으로 작성하며, `{주제}-분석.md`처럼 임의로 지은 파일명을 쓰지 않습니다. 특히 **입력이 요구사항 명세가 아니라 외부 레퍼런스 자료(타사 정책 안내 PDF·경쟁 서비스 화면 캡처·기사 등)여서 "구현 범위가 확정되지 않았다"고 판단되는 경우에도 이 규칙이 그대로 적용됩니다.** 그 자료를 이 저장소에 도입하는 것을 전제로 API·화면 스펙을 작성하는 것이 plan 세션의 일이며, 원문 분석은 별도 파일로 분리하지 않고 각 문서의 근거·배경 절에 녹입니다.

적용 범위(대상 앱·모듈, 어디까지 만들 것인지)가 불분명하면, 임의 판단으로 다른 형태의 산출물을 내지 말고 **선택지를 제시해 먼저 확인합니다**(위 "AI 규칙"의 체크리스트 질문 규칙).

| 파일 | 내용 | 작성 주체 |
|---|---|---|
| `docs/tasks/backend.md` | 신규 혹은 변경된 **API 스펙을 상세하게** 작성 | 플랜 세션 |
| `docs/tasks/frontend.md` | 화면·컴포넌트·API 연동 등 프론트엔드 구현 스펙 | 플랜 세션 |
| `docs/tasks/playwright.md` | **테스트를 진행할 상세 내용**(위 e2e 시나리오 규칙의 그 파일) | 플랜 세션 |
| `docs/tasks/playwright-issue-v{N}.md` | 검증 중 발견된 오류 (라운드별) | **검증 세션** |

위 문서들을 **어떤 명령으로 만들고 소비하는지**는 `docs/workflow/README.md`에 있습니다. 팀에 공유하는 상시 문서이므로 `docs/tasks/`의 휘발성 산출물과 달리 커밋되며, plan 세션이 덮어쓰지 않습니다. 명령문이나 단계 흐름이 바뀌면 그 파일을 갱신합니다.

**`backend.md`에는 다음을 빠짐없이 담습니다.**

- 엔드포인트별 **HTTP 메서드 · 경로 · 대상 모듈**(`web-api` / `admin-api` / `ceo-api`)
- **요청**: path·query 파라미터, 요청 본문 필드의 이름·타입·필수여부·유효성 제약
- **응답**: 본문 필드의 이름·타입·의미. 공통 래퍼(`ApiResponse<T>`)와 목록 페이징 형태(`content` / `page` / `size` / `totalElements`)를 명시
- **인증·권한**: 필요한 JWT(`th_web_` / `th_admin_` / `th_ceo_` 계열)와 권한
- **에러 케이스**: 상황별 `ErrorCode`와 HTTP 상태
- **신규/변경 구분**: 엔드포인트마다 신규인지 기존 변경인지 표시하고, 변경이면 **변경 전후 차이**를 함께 기술
- 도메인 모델·DB 스키마 변경이 있으면 그 내용도 함께 기술

**`frontend.md`에는 다음을 담습니다.**

- 대상 앱(`web` / `admin` / `ceo`)과 라우트·화면 경로
- 신설·변경되는 컴포넌트와 파일 경로
- 폼 스키마·유효성 규칙·에러 메시지
- 호출하는 API와 `backend.md` 스펙의 대응 관계

**`playwright.md`에는** 위 e2e 시나리오 3요소(**검증 대상 화면 / 정상 플로우 / 주요 예외 케이스**)를 담고, 여기에 더해 다음을 명시합니다.

- **사전 조건**: 기동할 개발 서버와 포트, 사용할 계정, 필요한 테스트 데이터
- 시나리오별 **조작 → 기대 결과 → 확인 방법**(어떤 `mcp__playwright__*` 도구로 확인하는지)

**검증 중 오류를 발견하면 `playwright.md`에 묻어두지 말고 `docs/tasks/playwright-issue-v{N}.md`에 따로 기록합니다.**

- **`{N}`은 검증 라운드 번호**입니다. 첫 검증에서 발견한 오류는 `playwright-issue-v1.md`, 수정 후 재검증에서 또 발견하면 `playwright-issue-v2.md`로 **라운드마다 새 파일**을 만듭니다. 이전 라운드 파일은 지우지 않습니다 — 어떤 이슈가 반복되는지 대조하는 근거이기 때문입니다.
- 한 라운드에서 오류가 여러 건이면 파일을 나누지 않고 그 라운드 파일에 건별 섹션으로 누적합니다.
- 각 항목에는 **재현 절차 / 기대 동작 / 실제 동작 / 관련 파일·API / 추정 원인 / 조치 상태**를 담습니다.
- 이 파일은 **검증 세션의 산출물**입니다. 플랜·구현 세션은 브라우저 검증을 수행하지 않으므로(위 "AI 규칙" 참조) 작성하지 않습니다.
- 작성했으면 작업 완료 보고에 파일 경로를 함께 제시하고, 아래 [검증→수정 자동 루프](#검증수정-자동-루프-검증-세션-전용)를 이어서 수행합니다.

**해당 영역의 변경이 없으면 그 문서는 만들지 않고, 어떤 문서를 왜 생략했는지 보고에 명시합니다**(예: 프론트엔드 전용 작업이면 `backend.md` 생략).

### 작업이 여러 기능 덩어리로 나뉠 때 (하위 폴더 배치)

**한 번의 플랜이 서로 독립적으로 구현·검증될 수 있는 여러 기능을 담는다면, 덩어리마다 하위 폴더를 만들어 그 안에 위 표의 파일들을 각각 작성합니다.**

```
docs/tasks/{덩어리-slug}/{backend,frontend,playwright}.md
docs/tasks/{덩어리-slug}/schema.sql
docs/tasks/README.md          ← 덩어리 목록·진행 순서·의존 관계
```

**파일명 규칙은 그대로이고 폴더 계층만 추가됩니다.** 하위 폴더 안에서도 `backend.md`·`frontend.md`·`playwright.md` 이름을 지키며, 임의로 지은 파일명을 쓰지 않습니다.

이렇게 나누는 이유는 구현 세션(`/autopilot`)에 **덩어리 하나씩 넘기기 위해서**입니다. 독립적인 기능 5개를 한 벌의 `backend.md`에 담으면 문서가 지나치게 길어지고, 한 덩어리의 스키마 변경이 다른 덩어리의 구현을 막습니다.

덩어리로 나눌 때는 `docs/tasks/README.md`를 함께 작성해 **덩어리 목록·권장 진행 순서·의존 관계·위험도**를 밝힙니다. 순서가 중요한 이유(예: 앞 덩어리의 화면에 뒤 덩어리가 얹힘, 주문 경로 파급)를 함께 적어, 구현 세션이 임의 순서로 착수하지 않게 합니다.

**나누지 않아도 되는 경우**: 기능이 하나거나 서로 강하게 얽혀 함께 구현·검증해야 하면 기존대로 `docs/tasks/` 바로 아래에 3개 파일만 만듭니다.

파일명이 고정이므로 **새 작업의 플랜을 작성하면 기존 문서를 덮어씁니다.** 이전 작업 문서를 보존해야 한다면 작성 전에 보존 여부를 확인합니다. `docs/tasks/*`는 `.gitignore`로 커밋 대상이 아니어서 git 히스토리에 남지 않으므로, 덮어쓰거나 지우면 복구할 수 없습니다.

## 네이밍 규칙

파일명, 변수명, 함수명 등 모든 네이밍은 최적의 이름을 선택하도록 합니다. 명확하고 의미 있는 이름을 사용하여 코드의 가독성과 유지보수성을 높입니다.

## E2E 테스트 (MCP Playwright) — 검증 세션 전용

> **이 절은 브라우저 검증을 명시적으로 요청받은 세션에만 적용됩니다.** 구현 세션은 위 "AI 규칙"에 따라 브라우저 검증을 수행하지 않으므로 이 절을 실행 대상으로 삼지 않습니다.
>
> 또한 이 절이 다루는 것은 **MCP Playwright를 통한 AI의 화면 검증**입니다. `frontend/web/e2e/`에 있는 `@playwright/test` 스펙 파일 자산은 별개 트랙이며 각 앱의 `AGENTS.md`가 설명합니다.

브라우저 조작이 필요한 E2E 테스트/검증 작업은 `npx playwright test` 대신 **MCP Playwright 도구**(`mcp__playwright__*`)를 사용합니다. 로그인이 필요한 플로우는 아래 테스트 계정으로 로그인합니다.

- 테스트 계정 정보는 해당 앱 디렉터리 `.env.local`의 `E2E_USERNAME`, `E2E_PASSWORD`를 사용합니다.
- 해당 앱 디렉터리에서 기동한 개발 서버(`npm run dev`)가 실행 중이어야 합니다.
- Playwright MCP 서버는 **리포지토리 루트의 `.mcp.json`**(프로젝트 스코프)에 등록되어 있습니다. 개인 설정(`~/.claude.json`)에 의존하지 않으므로 클론 경로가 바뀌어도 유지됩니다.
- 세션에 `mcp__playwright__*` 도구가 보이지 않으면 서버가 연결되지 않은 상태입니다. 이때는 검증을 생략하지 말고 다음을 확인합니다.
  1. `claude mcp list`로 `playwright` 상태를 확인합니다.
  2. `⏸ Pending approval`이면 프로젝트 스코프 서버의 최초 신뢰 승인이 필요합니다. 대화형 `claude` 세션에서 승인한 뒤 세션을 재시작합니다.
  3. 그래도 연결되지 않으면 `npx -y @playwright/mcp@latest --version`으로 패키지 설치 가능 여부를 확인합니다.
  4. 위 조치로도 연결되지 않으면, 브라우저 검증을 수행하지 못했다는 사실과 원인을 작업 보고에 명시합니다.

### 검증→수정 자동 루프 (검증 세션 전용)

**`docs/tasks/playwright.md` 검증을 요청받은 세션은 이슈를 발견해도 사람에게 넘기고 멈추지 않습니다.** 아래 루프를 스스로 끝까지 돌립니다. 사용자가 매 라운드 수동으로 수정 세션을 띄우던 왕복을 없애기 위한 규칙입니다.

**이 루프를 시작하는 채팅 명령문은 `docs/workflow/README.md`에 있습니다.** 팀에 공유하는 사용법 문서이므로, 명령문을 바꿀 때는 그 파일을 고칩니다.

이 루프에서 **검증(브라우저 조작)은 이 세션이 직접 하고, 수정만 별도 프로세스로 토스합니다.** 수정을 분리하는 이유는 두 가지입니다 — 검증 세션의 컨텍스트에 구현 작업이 섞이지 않아야 다음 라운드 검증이 편향되지 않고, 수정은 autopilot의 QA·검증 파이프라인을 온전히 태울 수 있기 때문입니다.

**반대로 검증은 분리할 수 없습니다.** `claude -p` headless 세션에는 프로젝트 스코프 MCP 서버의 신뢰 승인이 적용되지 않아 `mcp__playwright__*` 도구가 아예 없습니다. 그래서 검증을 하위 프로세스로 토스하면 "도구가 없어 검증 못 함"으로 조용히 끝납니다. **브라우저 조작은 반드시 이 대화형 세션이 수행합니다.**

#### 루프 절차

라운드 `N`을 1부터 시작합니다.

1. **검증 수행** — `docs/tasks/playwright.md`의 시나리오를 `mcp__playwright__*`로 실행합니다.
2. **이슈 없음 → 종료.** "라운드 N 검증 완료, 이슈 없음"을 채팅으로 보고하고 루프를 끝냅니다. 이슈 파일은 만들지 않습니다.
3. **이슈 있음 → `docs/tasks/playwright-issue-v{N}.md` 작성** (위 항목 구성 규칙대로).
4. **직전 라운드와 대조** — `playwright-issue-v{N-1}.md`가 있으면 비교합니다. **같은 이슈가 수정 후에도 재현되면 근본 원인 문제이므로 루프를 즉시 중단**하고, 반복된 이슈와 두 라운드 파일 경로를 함께 보고합니다.
5. **라운드 한도 확인** — `N`이 **3을 초과하면 중단**하고 남은 이슈를 보고합니다. 무한 루프와 토큰 폭주 방지입니다.
6. **수정 세션 토스** — 아래 명령으로 headless 수정 세션을 띄우고 **완료까지 기다립니다**.

   ```bash
   claude -p "/oh-my-claudecode:autopilot docs/tasks/playwright-issue-v${N}.md 에 기록된 이슈를 모두 수정해줘. 브라우저 검증은 하지 말고 코드 수정과 backend 빌드까지만 수행해줘." \
     --permission-mode acceptEdits \
     > /tmp/playwright-fix-v${N}.log 2>&1
   ```

   - 백그라운드(`&`)로 던져놓고 다음 단계로 넘어가지 않습니다. 수정이 끝나기 전에 재검증하면 이전 코드를 검증하게 됩니다.
   - 로그를 파일로 남겨, 수정 세션이 무엇을 했는지 다음 단계에서 확인할 수 있게 합니다.
   - 수정 세션이 실패하거나 아무 변경도 하지 않았으면 루프를 중단하고 `/tmp/playwright-fix-v${N}.log`와 함께 보고합니다.
7. **backend 재기동** — 수정에 backend 코드 변경이 포함됐으면 위 [`java -jar` 규칙](#backend는-java--jar로-실행합니다-bootrun-사용-금지)대로 **`내리기 → 빌드 → 다시 띄우기`** 순서로 재기동합니다. 이 단계를 건너뛰면 수정 전 jar가 그대로 떠 있어 **수정했는데도 같은 이슈가 재현되고**, 4번 규칙에 걸려 "근본 원인"으로 잘못 판정됩니다.
8. `N`을 1 올리고 **1번으로 돌아갑니다.**

#### 보고 형식

루프가 끝나면(이슈 없음·반복 이슈·라운드 한도 중 어느 쪽이든) 다음을 함께 보고합니다.

- 수행한 라운드 수와 종료 사유
- 생성한 `playwright-issue-v*.md` 파일 경로 전부
- 라운드별로 무엇이 수정됐는지 요약
- 남은 미해결 이슈(있으면)

#### 이 루프를 돌리지 않는 경우

- 사용자가 **"검증만"** 또는 **"수정하지 마"** 라고 명시한 경우 → 2·3번까지만 하고 이슈 파일 경로를 보고한 뒤 멈춥니다.
- MCP Playwright가 연결되지 않아 검증 자체를 못 한 경우 → 위 "Playwright MCP가 연결되지 않을 때" 절을 따릅니다. **이슈 파일을 만들지 않고, 수정 세션도 띄우지 않습니다.**

## 리포지토리 구조

```
backend/    Spring Boot Gradle 멀티모듈 (rootProject: tastyhouse-api)
frontend/   Next.js 앱 3개 — web(사용자 모바일 웹) · admin(관리자) · ceo(점주)
docs/       domain(도메인별 비즈니스 지식 문서) · oauth · pg
```

- **backend 모듈** (`backend/settings.gradle`): 총 14개이며 세 갈래입니다.
  - **실행 앱 4개**(bootJar): `web-api`, `admin-api`, `ceo-api`, `batch-module` — 컨트롤러/스케줄러 트리거와 부트스트랩만 갖는 thin adapter입니다.
  - **application 계층 1개**: `application` — 4개 앱의 유스케이스(인바운드 포트 + CQRS 서비스)를 담는 단일 모듈이며 infrastructure를 컴파일 클래스패스에 두지 않습니다. 자바 패키지는 앱별로 남아 있습니다(`com.tastyhouse.{web|admin|ceo|batch}application..`). 앱별 모듈 4개를 여기로 합친 근거는 `backend/application/AGENTS.md`의 "과거 판단의 번복" 절에 있습니다.
  - **공유 모듈**: `domain-module`, `infrastructure:persistence`, `infrastructure:redis`, `external-api`, `security-module`, `logging-module`, `api-common-module`.

  읽기 계약(`com.tastyhouse.application..port.out`의 `{Ctx}QueryPort`·`*Result`·`*SearchCondition`)을 담던 `application-common-module`은 계약이 전부 소유 모듈로 옮겨가며 **삭제됐습니다**(18 → 17개). 이후 앱별 application 모듈 4개를 `application` 하나로 합쳐 **14개**가 됐습니다. 지금은 한 앱만 쓰는 계약 271개를 `application`이, 2개 이상이 쓰는 공유 계약 55개를 `domain-module`이 소유합니다 — 패키지는 그대로라 한 최상위 패키지를 2개 모듈이 나눠 갖습니다.

  `infrastructure`는 기술별로 나뉜 **중첩 프로젝트**입니다(디렉터리 `backend/infrastructure/{persistence,redis}/`, Gradle 좌표 `:infrastructure:persistence`·`:infrastructure:redis`). 자바 패키지는 `com.tastyhouse.infrastructure..`로 불변입니다. 모듈 지도와 배치 기준은 `backend/CLAUDE.md`의 "모듈 지도" 절을 참조합니다.
- **작업 전 해당 영역 문서를 먼저 읽습니다.** backend 각 모듈과 frontend 각 앱의 `src/` 하위 디렉터리에는 그 영역의 최신 상세 가이드가 담긴 `AGENTS.md`가 있으므로, 이 파일의 요약에 의존하지 말고 해당 문서를 직접 확인합니다.
- **도메인 로직 작업 전에는 `docs/domain/{도메인}.md`를 참조합니다.** 주문·쿠폰·포인트·등급 등 도메인별 비즈니스 규칙이 정리돼 있어, 코드만 읽고 추측하는 것보다 정확합니다.

## 개발 서버 포트

아래 경로는 리포지토리 루트 기준이며, 각 명령은 그 디렉터리에서 실행합니다. backend는 Gradle 멀티모듈이라 wrapper가 `backend/`에만 있으므로, 모듈 디렉터리가 아니라 `backend`에서 모듈 태스크를 실행합니다.

| 앱 | 실행 디렉터리 (리포 루트 기준) | 포트 |
|---|---|---|
| web (사용자) | `frontend/web` — `npm run dev` | 3000 (`next dev` 기본값) |
| admin (관리자) | `frontend/admin` — `npm run dev` | 3010 |
| ceo (점주) | `frontend/ceo` — `npm run dev` | 3020 |
| web-api | `backend` — `java -jar web-api/build/libs/web-api-0.0.1-SNAPSHOT.jar` | 8080 |
| admin-api | `backend` — `java -jar admin-api/build/libs/admin-api-0.0.1-SNAPSHOT.jar` | 8090 |
| ceo-api | `backend` — `java -jar ceo-api/build/libs/ceo-api-0.0.1-SNAPSHOT.jar` | 8100 |

**위 표의 backend jar 4개 이름·경로는 모듈 재편(챕터 01~06)과 이후 application 모듈 통합 후에도 불변입니다** — 바뀐 것은 라이브러리 모듈뿐이고 실행 단위는 그대로 4개이기 때문입니다(통합으로 fat jar 안의 application jar가 4개에서 `application-0.0.1-SNAPSHOT.jar` 1개로 줄었을 뿐입니다). 배포 스크립트는 영향받지 않습니다.

backend는 실행 파일(jar)이 있어야 위 명령이 동작합니다. jar가 없으면(클론 직후·`clean` 후) `backend`에서 `./gradlew :{모듈}:build`로 먼저 만듭니다. **코드 변경 시마다 재빌드가 필요합니다** — 자세한 내용은 아래를 참조합니다.

### backend는 `java -jar`로 실행합니다 (`bootRun` 사용 금지)

**backend 앱은 로컬·운영 모두 `./gradlew build`로 만든 산출물을 `java -jar`로 실행합니다.** `bootRun`은 Gradle이 실행 환경을 대신 구성해 주므로 로컬에서만 통하는 경로·클래스패스 전제가 조용히 섞여 들어가고, 그 결과 "로컬에선 되는데 배포하면 안 되는" 차이가 생깁니다. 실행 방식을 하나로 통일하면 로컬에서 검증한 산출물이 곧 배포되는 산출물이 됩니다.

- **jar 이름의 버전은 `backend/build.gradle`의 `version`을 따릅니다**(현재 `0.0.1-SNAPSHOT`). 버전을 올리면 위 표의 파일명도 함께 바뀌므로, 명령을 복사하기 전에 `ls backend/{모듈}/build/libs/`로 실제 파일명을 확인합니다.
- **`-plain.jar`은 실행 대상이 아닙니다.** 같은 디렉터리에 `{모듈}-{버전}-plain.jar`(의존성 없는 클래스 묶음)이 함께 생성되는데, 이것을 실행하면 `no main manifest attribute` 오류가 납니다. 접미어 없는 쪽을 실행합니다.
- **백그라운드로 띄울 때는 로그를 파일로 남깁니다**(예: `nohup java -jar ... > /tmp/ceo-api.log 2>&1 &`). 기동 성공 판정은 포트 LISTEN 여부가 아니라 로그의 `Started {Xxx}ApiApplication` 마커로 합니다 — 포트는 부팅 도중에도 잠깐 열릴 수 있습니다.
- **코드 변경 시마다 `backend`에서 `./gradlew :{모듈}:build`로 재빌드합니다.** jar는 빌드 시점에 고정되므로 재빌드 없이 재실행하면 이전 코드가 그대로 뜨는데, 부팅은 성공하므로 변경이 반영되지 않은 것을 알아채기 어렵습니다.
- **재빌드 전에 그 모듈의 실행 중인 프로세스를 반드시 먼저 내립니다.** 앱이 떠 있는 상태로 빌드하면 Gradle이 jar를 삭제 후 재생성이 아니라 **같은 inode에 덮어쓰기**로 갱신하는데, JVM은 fat jar를 통째로 메모리에 올리지 않고 클래스가 필요한 시점에 파일 오프셋으로 lazy 로딩하므로 **아직 로드되지 않은 클래스의 오프셋이 전부 무효가 됩니다.** 순서는 `내리기 → 빌드 → 다시 띄우기`입니다.

  ```bash
  pgrep -lf 'ceo-api-.*\.jar'          # 1) 실행 중인지 확인
  pkill -f 'ceo-api-.*\.jar'           # 2) 떠 있으면 종료 (없으면 아무 일도 안 함)
  cd backend && ./gradlew :ceo-api:build   # 3) 빌드
  nohup java -jar ceo-api/build/libs/ceo-api-0.0.1-SNAPSHOT.jar > /tmp/ceo-api.log 2>&1 &  # 4) 재기동
  ```

  이 규칙을 어겨도 **그 순간에는 아무 일도 일어나지 않습니다.** 이미 로드된 클래스로 계속 동작하다가, 나중에 처음 필요해지는 클래스에서 `NoClassDefFoundError`가 터집니다. 증상과 원인의 시간 간격이 몇 시간까지 벌어지므로 아래 "간헐적으로 응답하지 않을 때" 절을 함께 참고합니다.
- **`build`는 테스트를 함께 실행하며, 테스트가 실패하면 jar가 만들어지지 않습니다.** 이 저장소에는 ArchUnit 레이어 규칙(`LayerRulesTest`)·`ErrorCodeConventionTest`·`EmbeddedRecordComponentOrderTest` 등 규약 위반을 잡는 가드 테스트가 있으므로, 빌드 실패 시 테스트 출력을 먼저 확인합니다 — 대개 코드가 아니라 규약을 어긴 것입니다.

#### 실행 디렉터리(CWD) 주의

**`java -jar`는 리포 루트와 `backend/` 두 곳에서만 실행합니다.** backend 설정은 `.env`를 `optional:file:.env` / `optional:file:backend/.env` **상대경로 두 벌**로 선언해(각 앱 `application.yml`) 이 두 위치를 지원하는데, 상대경로는 JVM 작업 디렉터리 기준으로 해석되므로 **그 밖의 디렉터리에서 실행하면 `.env`가 로드되지 않습니다.** 이때 DB 접속 정보 같은 필수 환경변수가 비어 부팅이 실패하거나, 더 나쁘게는 기본값으로 엉뚱한 대상에 붙습니다.

```bash
# 권장 — backend 디렉터리에서
cd backend && java -jar ceo-api/build/libs/ceo-api-0.0.1-SNAPSHOT.jar

# 가능 — 리포 루트에서 (backend/.env 경로 선언 덕분)
java -jar backend/ceo-api/build/libs/ceo-api-0.0.1-SNAPSHOT.jar

# 금지 — 그 외 디렉터리 (.env 미로드)
cd /tmp && java -jar ~/.../ceo-api-0.0.1-SNAPSHOT.jar
```

운영 배포처럼 CWD를 보장할 수 없는 환경에서는 `.env` 상대경로에 기대지 말고 환경변수를 직접 주입합니다(자격증명 파일은 `SECRETS_DIR`로 주입 — `backend/CLAUDE.md`의 "시크릿 파일 로딩 규칙(configtree)" 참조).

#### backend가 간헐적으로 응답하지 않을 때 (진단 순서)

요청이 30초 타임아웃되는데 **어떤 요청은 정상 200으로 응답**한다면, 톰캣이나 네트워크를 의심하기 전에 **실행 중 재빌드부터 확인합니다.** 이 저장소에서 실제로 겪은 사고이고, 증상이 원인과 몇 시간 떨어져 있어 추적이 오래 걸립니다.

**1단계 — 기동 시각과 jar 수정 시각을 비교합니다.** 이 한 번으로 판별이 끝납니다.

```bash
grep 'Started .*ApiApplication' /tmp/ceo-api.log   # 프로세스가 뜬 시각
ls -la backend/ceo-api/build/libs/                 # jar가 만들어진 시각
```

**jar mtime이 기동 시각보다 나중이면 확정입니다** — 실행 중에 덮어쓴 것이므로 위 재빌드 규칙대로 `내리기 → 빌드 → 다시 띄우기`로 해소합니다. `lsof -p {PID} | grep '\.jar'`에 `deleted` 표시가 **없는데도** 파일이 바뀐 것이 덮어쓰기의 근거입니다.

**2단계 — 로그에서 아래 두 신호를 확인합니다.**

- `Exception in thread "http-nio-{포트}-exec-N"` — 워커 스레드가 catch되지 않은 예외로 **죽은** 것입니다. 요청마다 하나씩 사망해 스레드 풀이 고갈되고, 그 뒤 새 커넥션은 accept 큐에 쌓인 채 처리되지 않아 타임아웃이 납니다. 아직 살아있는 스레드가 잡은 요청만 200으로 응답하므로 **"간헐적"으로 보입니다.**
- `NoClassDefFoundError` / `ClassNotFoundException` — 특히 `ch.qos.logback.classic.spi.ThrowableProxy`처럼 **예외 로깅 시점에 처음 로드되는 클래스**에서 납니다. 이 예외 자체가 원인이지 부수 현상이 아닙니다.

**3단계 — 앱 로그에 `[REQUEST]` 라인이 있는지 봅니다.** 프론트는 요청을 보냈는데 backend 로그에 해당 `[REQUEST]`가 **아예 없다면**, 요청이 소켓까지는 도착했지만 처리할 스레드가 없어 서블릿 필터(`ApiLoggingFilter`)까지 도달하지 못한 것입니다. 이것도 위 스레드 고갈의 증거이며, **네트워크 문제로 오인하기 쉬운 지점**입니다.

부팅 자체가 실패한다면 다른 문제이므로 아래 DB 스키마 절을 확인합니다.

### 로컬 DB 스키마 반영 (마이그레이션 도구 없음)

**이 저장소에는 Flyway·Liquibase 같은 마이그레이션 도구가 없고 `backend/schema.sql` 한 벌만 있습니다.** 따라서 스키마를 바꾸는 기능을 구현해도 `schema.sql`만 갱신될 뿐 **로컬 DB에는 아무것도 반영되지 않습니다.** 접속 정보는 `backend/.env`(`DB_URL`·`DB_USERNAME`·`DB_PASSWORD`)에 있습니다.

`application-infrastructure.yml`이 `ddl-auto: validate`라서, 엔티티와 실제 테이블이 어긋나면 부팅이 다음과 같이 **정상적으로 거부됩니다**.

```
Schema-validation: missing column [boundary] in table [ADMIN_DONG]
→ Unable to build Hibernate SessionFactory → APPLICATION FAILED TO START
```

- **스키마 변경이 포함된 기능을 구현했거나, 남의 브랜치를 받아 backend 부팅이 실패하면 이 드리프트를 먼저 의심합니다.**
- **에러에 찍힌 컬럼 하나만 고치지 말고 전수 대조합니다.** 하나를 추가하면 다음 컬럼에서 또 실패하므로, `schema.sql`의 `CREATE TABLE` 블록을 `information_schema.columns`와 통째로 비교해 누락 테이블·컬럼을 한 번에 뽑는 편이 빠릅니다.
- **DDL은 직접 실행하지 말고 `docs/tasks/*.sql`로 작성해 사용자에게 적용을 요청합니다.** DB 스키마 변경은 되돌리기 어려운 작업이라 실행 권한이 제한됩니다.
- 컬럼을 추가해 부팅이 되더라도 **시드 데이터가 없으면 기능은 빈 상태로 동작합니다.** 좌표·경계처럼 NULL 허용 컬럼은 부팅을 막지 않으므로, 기능 검증까지 하려면 시드가 별도로 필요한지 확인합니다.

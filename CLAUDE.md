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

| 파일 | 내용 | 작성 주체 |
|---|---|---|
| `docs/tasks/backend.md` | 신규 혹은 변경된 **API 스펙을 상세하게** 작성 | 플랜 세션 |
| `docs/tasks/frontend.md` | 화면·컴포넌트·API 연동 등 프론트엔드 구현 스펙 | 플랜 세션 |
| `docs/tasks/playwright.md` | **테스트를 진행할 상세 내용**(위 e2e 시나리오 규칙의 그 파일) | 플랜 세션 |
| `docs/tasks/playwright-issue.md` | 검증 중 발견된 오류 | **검증 세션** |

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

**검증 중 오류를 발견하면 `playwright.md`에 묻어두지 말고 `docs/tasks/playwright-issue.md`에 따로 기록합니다.**

- 오류가 여러 건이면 파일을 나누지 않고 이 한 파일에 건별 섹션으로 누적합니다.
- 각 항목에는 **재현 절차 / 기대 동작 / 실제 동작 / 관련 파일·API / 추정 원인 / 조치 상태**를 담습니다.
- 이 파일은 **검증 세션의 산출물**입니다. 플랜·구현 세션은 브라우저 검증을 수행하지 않으므로(위 "AI 규칙" 참조) 작성하지 않습니다.
- 작성했으면 작업 완료 보고에 파일 경로를 함께 제시합니다.

**해당 영역의 변경이 없으면 그 문서는 만들지 않고, 어떤 문서를 왜 생략했는지 보고에 명시합니다**(예: 프론트엔드 전용 작업이면 `backend.md` 생략).

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

## 리포지토리 구조

```
backend/    Spring Boot Gradle 멀티모듈 (rootProject: tastyhouse-api)
frontend/   Next.js 앱 3개 — web(사용자 모바일 웹) · admin(관리자) · ceo(점주)
docs/       domain(도메인별 비즈니스 지식 문서) · oauth · pg
```

- **backend 모듈** (`backend/settings.gradle`): 실행 앱 4개(`web-api`, `admin-api`, `ceo-api`, `batch-module`)와 공유 모듈(`domain-module`, `infrastructure-module`, `external-api`, `security-module`, `logging-module`, `api-common-module`)로 구성됩니다.
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

backend는 실행 파일(jar)이 있어야 위 명령이 동작합니다. jar가 없으면(클론 직후·`clean` 후) `backend`에서 `./gradlew :{모듈}:build`로 먼저 만듭니다. **코드 변경 시마다 재빌드가 필요합니다** — 자세한 내용은 아래를 참조합니다.

### backend는 `java -jar`로 실행합니다 (`bootRun` 사용 금지)

**backend 앱은 로컬·운영 모두 `./gradlew build`로 만든 산출물을 `java -jar`로 실행합니다.** `bootRun`은 Gradle이 실행 환경을 대신 구성해 주므로 로컬에서만 통하는 경로·클래스패스 전제가 조용히 섞여 들어가고, 그 결과 "로컬에선 되는데 배포하면 안 되는" 차이가 생깁니다. 실행 방식을 하나로 통일하면 로컬에서 검증한 산출물이 곧 배포되는 산출물이 됩니다.

- **jar 이름의 버전은 `backend/build.gradle`의 `version`을 따릅니다**(현재 `0.0.1-SNAPSHOT`). 버전을 올리면 위 표의 파일명도 함께 바뀌므로, 명령을 복사하기 전에 `ls backend/{모듈}/build/libs/`로 실제 파일명을 확인합니다.
- **`-plain.jar`은 실행 대상이 아닙니다.** 같은 디렉터리에 `{모듈}-{버전}-plain.jar`(의존성 없는 클래스 묶음)이 함께 생성되는데, 이것을 실행하면 `no main manifest attribute` 오류가 납니다. 접미어 없는 쪽을 실행합니다.
- **백그라운드로 띄울 때는 로그를 파일로 남깁니다**(예: `nohup java -jar ... > /tmp/ceo-api.log 2>&1 &`). 기동 성공 판정은 포트 LISTEN 여부가 아니라 로그의 `Started {Xxx}ApiApplication` 마커로 합니다 — 포트는 부팅 도중에도 잠깐 열릴 수 있습니다.
- **코드 변경 시마다 `backend`에서 `./gradlew :{모듈}:build`로 재빌드합니다.** jar는 빌드 시점에 고정되므로 재빌드 없이 재실행하면 이전 코드가 그대로 뜨는데, 부팅은 성공하므로 변경이 반영되지 않은 것을 알아채기 어렵습니다.
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

# 작업지시서 09 — 소스트리 위생 정리 및 문서 정합성 복구

## 배경 (왜)

이 프로젝트는 오랜 기간 여러 AI 세션(OMC 등 도구)을 거치며 작업해온 것으로 보이며, 그 과정에서 도구의 세션 상태 파일과 빌드 산출물이 실수로 소스 트리(`src/main/java`) 내부에 커밋된 흔적이 다수 발견되었다. 또한 일부 문서(AGENTS.md, REFACTORING.md)가 실제 코드 상태와 어긋나 있어(버전 드리프트, 존재하지 않는 모듈 언급), 문서를 신뢰하고 참고하는 향후 작업에 혼란을 줄 수 있다.

## 현재 상태 (근거)

### 1. 소스 트리 내부에 커밋된 도구 세션 상태 디렉터리

`.omc/state/sessions/...` 형태의 디렉터리가 Java 소스 패키지 내부에 다수 커밋되어 있다:

- `core-module` 하위 약 15곳(예: `core-module/.../order/application/dto/command/.omc/...`)
- `infrastructure-module` 하위 약 3곳(예: `infrastructure-module/.../payment/persistence/.omc/...`)
- `web-api` 하위 약 8곳(예: `web-api/src/main/java/com/tastyhouse/webapi/.omc`, `.../product/.omc`, `.../product/response/.omc`, `.../member/response/.omc`, `.../reservation/response/.omc`, `.../shop/response/.omc`, `.../verification/response/.omc`, `.../review/response/.omc`)
- `admin-api` 하위 1곳(`admin-api/src/main/java/com/tastyhouse/adminapi/shop/.omc`)
- `external-api` 하위에도 존재(예: `external-api/src/main/java/com/tastyhouse/external/oauth/.omc/`)

내용물은 `pre-tool-advisory-throttle.json`, `last-tool-error-state.json` 등 도구 내부 상태 파일이다.

### 2. 컴파일 산출물이 커밋된 `bin/` 디렉터리

`core-module/bin/`, `web-api/bin/`, `external-api/bin/`에 컴파일된 클래스/복제 소스 트리가 커밋되어 있다. 이 안에는 오래된 `AGENTS.md` 사본도 포함되어 있어 최신 문서와 혼동을 줄 수 있다.

### 3. 빈 DDL 파일

루트 `alter.sql`이 0바이트다 — 아직 아무 스키마 변경도 기록되지 않은 placeholder로 보이나, 실제로 최근 여러 도메인 리팩터링(테이블 리네이밍, 컬럼 추가 등)에서 alter 작업이 있었다는 기록이 CLAUDE.md에 있다(예: `rank` 도메인의 `is_deleted` 컬럼 신설, `point` 도메인의 `RENAME TABLE`). 이 alter 구문들이 실제로는 `create.sql`에 이미 반영되어 있어서 `alter.sql`이 비어있는 것이 맞는지, 아니면 실행된 마이그레이션 기록이 누락된 것인지 확인이 필요하다.

### 4. 오타 파일명

루트에 `REAME.md`(README.md 오타)가 있으며, 내용은 이미지 경로 메모로 실제 프로젝트 리드미 역할을 하지 않는다.

### 5. 자격증명 파일 존재

루트에 `.env`, `.env-copy`가 있고 `json/` 디렉터리에 Firebase 서비스 계정 자격증명이 있다. `.gitignore`에 이들이 실제로 제외되어 있는지 확인이 필요하다(민감 정보이므로 최우선 확인 대상).

### 6. 문서 정합성 드리프트

- `external-api/AGENTS.md`, `web-api/AGENTS.md`가 JJWT 버전을 `0.12.3`으로 기재하고 있으나, 실제 모든 `build.gradle`은 `0.13.0`을 사용한다.
- `md/REFACTORING.md`가 `file-module`이라는 이름의 모듈을 언급하지만, 이 모듈은 현재 존재하지 않는다(파일 처리 기능은 `core-module/domain/file` + `external-api/file`로 구현되어 있음 — 이미 `MEMORY.md`에도 기록된 사실).
- `md/REFACTORING.md`의 HIGH 심각도 이슈: "Toss 결제 취소 시 PG 상태와 내부 상태 불일치 가능성", 위치는 `web-api/.../payment/PaymentService.java:200-227`로 기록되어 있다. 이 파일이 현재 그 라인 범위에 여전히 같은 문제를 갖고 있는지, 이미 해결되었는지 확인되지 않았다. 참고로 `payment` 도메인은 이후 core-module에서 POJO로 분리되었으므로(CLAUDE.md 기록), 그 리팩터링 과정에서 이 이슈가 우연히 해소되었을 수도 있다.

## 작업 지시

### 9-1. 자격증명 노출 확인 (최우선)

1. `.gitignore`를 열어 `.env`, `.env-copy`, `json/`이 포함되어 있는지 확인한다.
2. `git log --all --full-history -- .env json/`(또는 동등한 방법)으로 이 파일들이 과거에 실수로 커밋된 적이 있는지 확인한다.
3. 노출 이력이 있다면 즉시 사용자에게 알리고(자격증명 회전 필요 여부는 사용자 판단), `.gitignore`에 없다면 추가한다.

### 9-2. 소스트리 오염 제거

1. 모든 `.omc/` 디렉터리를 `src/main/java`, `src/test/java` 하위에서 찾아 삭제한다.
2. 각 모듈의 `bin/` 디렉터리(커밋된 컴파일 산출물)를 삭제한다.
3. 루트 `.gitignore`에 `.omc/`(소스 트리 내부 발생 방지용 패턴 포함), `bin/`, `build/`가 이미 있는지 확인하고 없으면 추가한다.
4. 루트 `REAME.md`를 삭제하거나(내용이 무가치하면) 의도된 문서였다면 `README.md`로 정정한다.

### 9-3. DDL 정합성 확인

1. `alter.sql`이 빈 파일인 이유를 확인한다 — 이 프로젝트가 "새 스키마 변경은 alter.sql에 축적, 초기 스키마는 create.sql"이라는 컨벤션을 쓰는지, 아니면 alter 구문들이 매번 create.sql에 직접 병합되는 방식인지 확인.
2. 컨벤션이 확인되면 그 사실을 `md/DOMAIN-JPA-SEPARATION-GUIDE.md` 또는 신규 `md/DDL-CONVENTION.md`에 짧게 기록한다.

### 9-4. 문서 드리프트 수정

1. `external-api/AGENTS.md`, `web-api/AGENTS.md`의 JJWT 버전을 `0.13.0`으로 정정.
2. `md/REFACTORING.md`의 `file-module` 언급을 실제 구조(`core-module/domain/file` + `external-api/file`)로 정정.
3. `web-api/.../payment/PaymentService.java:200-227`(또는 리팩터링 이후 실제 위치)를 확인해 HIGH 이슈가 여전히 유효한지 판단하고, `md/REFACTORING.md`에 "해결됨" 또는 "여전히 유효함, 별도 작업 필요"로 상태를 갱신한다. 여전히 유효하다면 별도 보안/정합성 작업지시서로 승격할 것을 제안한다.

## 완료 기준

- [ ] 자격증명 노출 이력 확인 완료(있었다면 사용자에게 보고)
- [ ] 모든 `.omc/` 디렉터리가 소스 트리에서 제거됨
- [ ] 모든 `bin/` 디렉터리가 제거됨
- [ ] `.gitignore`가 재발 방지 패턴을 포함함
- [ ] `REAME.md` 처리됨(삭제 또는 정정)
- [ ] `alter.sql` 관련 컨벤션이 확인되고 문서화됨
- [ ] AGENTS.md의 JJWT 버전이 정정됨
- [ ] `md/REFACTORING.md`의 file-module 언급이 정정되고, Toss 결제 HIGH 이슈 상태가 갱신됨

## 주의사항

- `.omc/`, `bin/` 삭제는 되돌리기 어려운 작업이 아니므로(git으로 복구 가능) 비교적 안전하지만, 삭제 전 `git status`로 다른 미커밋 변경사항이 없는지 확인할 것.
- 자격증명 파일(`.env` 등) 자체의 내용은 절대 이 작업 과정에서 로그나 커밋 메시지에 노출하지 않는다.
- Toss 결제 이슈 확인은 신중해야 한다 — 실제 코드를 읽고 판단할 것, 문서만 보고 "해결됨"으로 단정하지 않는다.
- NO_COMMIT_OR_ROLLBACK — 추천 커밋 메시지: `chore(hygiene): 소스트리 내 .omc/bin 오염 제거 및 문서 정합성 복구`. 자격증명 관련 발견사항이 있었다면 별도로 사용자에게 구두 보고(커밋 메시지에 민감정보 관련 세부사항 기재 금지).

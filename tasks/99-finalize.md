# 최종 정리 — 모듈 리네이밍·의존 제거·문서 개정

> 선행: **00~50 전부 완료 필수**(core에 application 패키지 0개 확인 후 시작).

## 작업

### 1. core-module 의존 다이어트
- `core-module/build.gradle`: `spring-tx`·`spring-orm` 제거(도메인 서비스는 전부 POJO이므로 불필요 — 제거 후 LSP로 잔여 참조 0 확인. `@Transactional`/`ObjectOptimisticLockingFailureException` import가 core에 남아 있으면 해당 도메인 작업 미완이므로 반려).
- `@QueryProjection` DTO가 core에 남아 있지 않은지 확인(35개 전부 **infrastructure-module `<ctx>/query/`로** 이관됐어야 함 — 남았다면 해당 도메인 작업 미완으로 반려). 확인 후 `core-module/build.gradle`에서 **`api querydsl-core` 의존 + `querydsl-apt` annotationProcessor + querydsl sourceSets/generated 디렉터리 블록 전부 제거** — 이 시점부터 api 모듈로의 `com.querydsl.*` 전이가 완전 차단된다.
- `infrastructure-module/build.gradle`의 `querydsl-jpa`가 `implementation`으로 강등돼 있는지 확인(10-notice 재작업에서 수행 — 미수행이면 여기서).

### 2. 모듈 리네이밍 core-module → domain-module
- 디렉터리 `core-module/` → `domain-module/` 이동(git mv).
- `settings.gradle`: `include 'core-module'` → `include 'domain-module'`.
- 전 모듈 build.gradle의 `project(':core-module')` → `project(':domain-module')`.

### 3. 패키지 리네이밍 com.tastyhouse.core → com.tastyhouse.domain
- 소스 디렉터리 이동 + 전 모듈 import 일괄 치환(LSP rename 또는 스크립트). `QXxxResult` 등 생성 소스는 재생성이므로 무시. **infrastructure-module `<ctx>/query/` DAO·Result가 참조하는 `com.tastyhouse.core.shared.page.PageQuery`/`PageResult`도 치환 대상에 포함**됨을 유의.
- 4개 앱의 `scanBasePackages` 갱신: `"com.tastyhouse.core"` → 도메인 서비스 빈이 전부 infra `DomainServiceConfig` 소관이면 **core(domain) 스캔 엔트리 자체 제거** 가능 — `@Component`가 domain에 하나도 없는지 grep으로 확인 후 결정.
- `MemberIdConverter`/`OrderIdConverter` 등 core 잔류 컨버터의 새 패키지 경로를 infra `@Convert` 참조와 대조.

### 4. 문서 개정
- 루트 `CLAUDE.md`·`AGENTS.md`, 각 모듈 `AGENTS.md`: 모듈 구조도, "core-module" 명칭, DTO 조립 규칙(Result/Command의 새 위치), import 순서 규칙의 계층 매칭 표(`...application` 매칭이 이제 각 api 모듈 내부를 가리킴), 페이징/response 규칙의 경로 예시 전면 갱신. **신규 컨벤션 명문화**: "query DAO·Result DTO(@QueryProjection)·SearchCondition은 infrastructure-module `<ctx>/query/` 소유", "api 모듈 QueryDSL 금지(ArchUnit 강제)", "api 모듈 application 서비스는 도메인당 `{도메인}CommandService`/`{도메인}QueryService` CQRS 분리", "admin/web Result가 같은 패키지에서 충돌하면 `Management` 한정어 상시 적용".
- `md/CLEAN-ARCHITECTURE.md`에 이번 전환 기록 추가.
- `tasks/` 폴더는 완료 후 삭제 여부를 사용자에게 질문.

### 5. 최종 검증
- 전 모듈 LSP diagnostics 오류 0.
- ArchUnit 테스트(web/admin/ceo/batch 4개 모듈 — QueryDSL·persistence 차단 규칙 포함) 통과 확인.
- `grep -r "com.tastyhouse.core" --include="*.java" .` → 0건.
- api 4모듈(web/admin/ceo/batch) src에서 `grep -r "com.querydsl"` → 0건, `grep -r "QueryProjection"` → 0건, `grep -r "infrastructure.*persistence"`(import) → 0건.
- api 4모듈 build.gradle에 `querydsl-apt` 잔존 0건, core(domain)-module build.gradle에 `querydsl` 잔존 0건.
- gradle build는 실행하지 않음(프로젝트 규칙). 커밋 금지 — Phase별 추천 커밋 메시지 목록을 최종 산출물로 제시.

## 완료 기준
- 위 5개 항목 전부 충족. 추천 커밋 메시지 예: `refactor(module): core-module을 domain-module로 전환 — application 계층을 소비 모듈로 해체하고 도메인 서비스 하강`.

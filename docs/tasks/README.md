# 아키텍처 개선 작업지시서 인덱스

## 배경

`tastyhouse-api`는 Spring Boot 3.2.4 / Java 21 멀티모듈(6개: `web-api`, `admin-api`, `core-module`, `infrastructure-module`, `external-api`, `logging-module`) 프로젝트로, `md/CLEAN-ARCHITECTURE.md`·`md/DOMAIN-JPA-SEPARATION-GUIDE.md`가 정의한 DDD/클린 아키텍처 Strangler Fig 전환이 사실상 완료된 상태다(전 22개 바운디드 컨텍스트가 core-module에서 순수 POJO 도메인 모델로 분리되었고, JPA/QueryDSL-JPA 어댑터는 전부 `infrastructure-module`로 이동해 `core-module`은 100% JPA-free임이 확인됨).

전환 완료 후 전수 조사(core-module/infrastructure-module, admin-api/web-api, external-api·빌드·문서 3개 영역 병렬 탐색)에서 구조적 개선 후보들이 발견되었다. 이 디렉터리는 그 개선 후보들을 **개별 AI 세션에 그대로 명령으로 투입 가능한 자기완결적 작업지시서**로 분할한 것이다. 각 파일은 배경·근거(파일 경로·라인)·작업 지시·완료 기준·주의사항을 자체적으로 포함하므로, 이 README 없이 해당 파일 하나만 열어도 작업을 시작할 수 있다.

## 모듈 의존 그래프 (참고)

```
web-api   ── implementation core-module, external-api, logging-module
          ── runtimeOnly     infrastructure-module
admin-api ── implementation core-module, external-api, logging-module
          ── runtimeOnly     infrastructure-module
external-api ── implementation core-module
infrastructure-module ── api core-module
core-module ── (내부 모듈 의존 없음, JPA-free)
logging-module ── (내부 모듈 의존 없음)
```

## 작업 목록 및 우선순위

사용자가 지정한 최우선 영역은 **① 구조 일관성 정리**, **② 모듈 간 책임·역할 분리**다. 아래 표는 이 순서를 따른다.

| # | 파일 | 우선순위 | 영향 범위 | 선행 작업 | 코드 이해 필요도 |
|---|---|---|---|---|---|
| 01 | [01-common-consolidation.md](01-common-consolidation.md) | 1 (구조 일관성) | web-api, admin-api의 `common/` 전체 | 없음 | 중 |
| 02 | [02-pathbuilder-restore.md](02-pathbuilder-restore.md) | 1 (구조 일관성) | infrastructure-module 4개 파일 | 없음 (02는 01과 무관, 독립 실행 가능) | 중 |
| 03 | [03-naming-consistency.md](03-naming-consistency.md) | 1 (구조 일관성) | web-api Facade 2건, SearchApiController, PathVariable 다수 | 없음 | 중 |
| 04 | [04-dead-code-cleanup.md](04-dead-code-cleanup.md) | 1 (구조 일관성) | infrastructure-module shop, core review | 없음 | 저 (호출부 grep 위주) |
| 05 | [05-file-service-separation.md](05-file-service-separation.md) | 2 (책임 분리) | external-api file 전체, core file 도메인, web/admin-api 24개 호출부 | 02 완료 후 권장(패턴 일관성) | 상 |
| 06 | [06-scheduler-relocation.md](06-scheduler-relocation.md) | 2 (책임 분리) | web-api scheduler 5개 클래스 | 없음 | 중 |
| 07 | [07-cross-cutting-symmetry.md](07-cross-cutting-symmetry.md) | 2 (책임 분리) | web-api ratelimit, admin-api, logging-module | 없음 | 저 |
| 08 | [08-security-todos.md](08-security-todos.md) | 후순위 (보안 실취약점이나 별도 트랙) | core reservation, web-api reservation | 없음 — 독립적으로 언제든 실행 가능 | 상 |
| 09 | [09-hygiene-and-docs.md](09-hygiene-and-docs.md) | 후순위 | 전 모듈 소스트리, md 문서 | 없음 | 저 |

## 의존 순서 참고

- **01(common 통일)과 03(네이밍)**: 03에서 Facade→Service 리네이밍을 어느 방향으로 결정하든 01의 common 통일 방식(모듈별 중복 유지 vs 공유 모듈 신설)에는 영향을 주지 않는다. 순서 무관.
- **02(PathBuilder 복원)**: infrastructure-module 내부closed 작업이라 다른 어떤 작업에도 의존하지 않고 즉시 실행 가능.
- **05(FileService 분리)**: 02에서 정립하는 "정식 Q타입/포트 참조" 패턴 정리가 먼저 끝나면 05의 `application/port` 패턴 적용 논의가 더 매끄럽지만, 강제 선행조건은 아니다.
- **08(보안 TODO)**: 다른 작업과 완전히 독립적인 실취약점 트랙이다. 우선순위 표기와 무관하게 일정이 맞으면 먼저 처리해도 된다.

## 향후 후보 (이번 작업지시서 세트에서 제외)

**테스트·CI 구축**은 사용자가 이번 우선 영역으로 선택하지 않았고 범위가 매우 넓어(신규 테스트 설계 + CI 파이프라인 구축) 독립 작업지시서로 만들지 않았다. 현황만 기록해둔다:

- 실질 테스트 커버리지는 `core-module`의 도메인 모델 단위 테스트 66건뿐(도메인당 `*Test.java` 1개, `domain/model` 하위).
- `web-api`/`admin-api`는 컨텍스트 로드 테스트 1건씩만 존재(`WebApiApplicationTests`, `AdminApiApplicationTests`).
- `infrastructure-module`은 테스트 0건.
- `spring-security-test`, `testcontainers` 등 테스트 의존성은 선언되어 있으나 실제 사용되지 않음.
- `.github/` 디렉터리 부재 — CI 파이프라인 없음.

추후 별도 작업지시서(`10-test-ci-foundation.md` 등)로 다룰 것을 권장한다.

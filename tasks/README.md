# DDD·클린아키텍처 문제점 개선 작업 목록 (P1~P12)

2026-07-31 전수 조사(모듈 의존 그래프 / domain-module DDD 품질 / api·infra 계층 위반)로 도출된 문제점 12건을, 각각 독립 AI가 수행할 수 있는 자기완결형 작업 지시서로 분리한 것이다. 심각도순 번호이며, 각 파일에 배경·문제 상세(파일:라인)·작업 지시·수용 기준·주의사항이 들어 있다.

## 공통 규칙 (모든 작업에 적용)

- 답변·커밋 메시지·주석은 **한국어**.
- **NO_COMMIT_OR_ROLLBACK**: 커밋/롤백을 직접 실행하지 않는다. 작업 완료 시 추천 커밋 메시지만 제시한다(`{type}({scope}): {한글 요약}` 컨벤션).
- **gradle build 실행 금지**. 컴파일 검증이 필요하면 프로젝트 메모리의 `verify-without-gradle` 절차(javac+apt)를 따른다.
- 루트 `CLAUDE.md`(722행)의 네이밍·DTO 조립·CQRS·import 순서 등 30여 개 규칙을 준수한다. 신규 컨벤션이 생기면 CLAUDE.md/AGENTS.md 갱신 여부를 함께 검토한다.
- 되물을 때는 자유 서술형이 아니라 **선택지 체크리스트**로 제시한다.

## 작업 목록과 의존 관계

| 파일 | 제목 | 선행 의존 | 동작 변경 |
|---|---|---|---|
| [p01-order-state-machine.md](p01-order-state-machine.md) | Order 상태 머신 도입 | 없음 | 예 |
| [p02-archunit-cqrs-gate.md](p02-archunit-cqrs-gate.md) | ArchUnit 게이트 강화 (교차 주입·domain-free·순환) | **P3·P5 이후 권장** (기존 위반이 red로 잡히므로) | 아니오 |
| [p03-transaction-boundary.md](p03-transaction-boundary.md) | 트랜잭션 경계 정리 (파사드·PG/메일 호출) | 없음 | 예 |
| [p04-aggregate-invariants.md](p04-aggregate-invariants.md) | of() 팩토리 불변식 추가 | 없음 | 예 |
| [p05-write-port-query-leak.md](p05-write-port-query-leak.md) | write 포트 표현 조회 유출 이관 | 없음 | 아니오 |
| [p06-domain-rules-in-infra.md](p06-domain-rules-in-infra.md) | infra 하드코딩 도메인 규칙 회수 | 없음 | 아니오 |
| [p07-shop-context-logic.md](p07-shop-context-logic.md) | shop 로직 이탈 복구 (Calculator→모델) | 없음 | 아니오 |
| [p08-id-vo-policy.md](p08-id-vo-policy.md) | ID VO 반쪽 적용 해소 (**사용자 정책 결정 필요**) | 없음 | 아니오 |
| [p09-domain-events.md](p09-domain-events.md) | 수신자 없는 이벤트 7종 처리 | 없음 | 결정에 따름 |
| [p10-build-gate-purity.md](p10-build-gate-purity.md) | domain-module 순수성 빌드 강제 + 문서 정합 | 없음 | 아니오 |
| [p11-query-performance.md](p11-query-performance.md) | 페이징 count·N+1 성능 결함 수정 | 없음 | 아니오(성능만) |
| [p12-api-module-duplication.md](p12-api-module-duplication.md) | api 모듈 3중 중복 통합 | 없음 | 아니오 |

### 충돌 주의 (병렬 수행 시)

- **P2 ↔ P3·P5**: P2가 추가하는 ArchUnit 규칙은 P3(ReviewCommandService 정리)·P5(write 포트 유출 이관)가 끝나기 전에는 기존 위반 때문에 red다. P2를 먼저 하면 위반 목록을 `@Disabled` 없이 red로 두지 말고, 해당 태스크 완료 전까지 위반 클래스를 명시적 예외 목록으로 관리하고 주석으로 P3/P5 티켓을 남긴다.
- **P5 ↔ P7**: 둘 다 `ShopDetailRepository`·shop QueryService를 만진다. 동시 수행 시 파일 충돌 — 순차 권장(P5 먼저).
- **P1 ↔ P9**: 둘 다 `Order` 전이 지점을 만진다(P9가 전이 이벤트 발행을 추가할 수 있음). 순차 권장(P1 먼저).
- **P4 ↔ P7**: `ShopBusinessHour.of()` 검증(P4)과 `isOpenAt()` 추가(P7)가 같은 파일 — 순차 또는 담당 통합.

# policy 도메인 전환

> 선행: `tasks/README.md` + `00-phase0`. 그룹 1(병렬 가능).

## 현황
- core: `policy/application/` — `PolicyCommandService`(발행: `ApplicationEventPublisher` 사용 확인됨), QueryService + result. 단일 애그리거트 `PolicyDocument`(enum `PolicyType`).
- 소비자: admin-api(CRUD·활성화), web-api(현행 정책 조회).

## 작업
1. **(C) 판정**: `activatePolicy`는 "새 정책 활성화 + 기존 활성 정책 비활성화"를 한 트랜잭션에서 수행(같은 타입 2개 인스턴스 save) — 액터 무관 규칙이므로 도메인 서비스 `PolicyActivationService`로 하강(패턴 1, `DomainServiceConfig` 등록). 이벤트 발행이 있으면 `DomainEventPublisher` 포트로 교체.
2. **(A)**: 나머지 update/create/delete는 admin `PolicyCommandService`(@Transactional)로 흡수(패턴 2 — 기존 facade `PolicyService`는 Command/Query로 분해, `PolicyActivationService` 호출도 CommandService 소관).
3. **(B)**: infra `infrastructure/policy/query/PolicyQueryDao` 신설(패턴 3 — admin/web 소비자별 메서드 분리), Result·Condition은 infra query 소유(충돌 시 `Management` 한정어). admin/web 각 `PolicyQueryService`(readOnly)가 DAO 주입. `PolicyDocumentRepository` write 순수화(패턴 4).
4. core `policy/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.

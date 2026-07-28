# partnership 도메인 전환

> 선행: `tasks/README.md` + `00-phase0`. 그룹 1(병렬 가능).

## 현황
- core: `partnership/application/` — `PartnershipCommandService`(changeStatus/delete), QueryService. 단일 애그리거트 `PartnershipRequest`(enum `PartnershipStatus`).
- 소비자: web-api(제휴 신청), admin-api(상태 관리).

## 작업
1. **분류**: 전량 (A)+(B). 이벤트·포트 없음.
2. **(A)**: web의 신청 생성은 web `PartnershipCommandService`로, admin의 changeStatus/delete는 admin `PartnershipCommandService`로 흡수(@Transactional, 패턴 2). Command DTO 삭제.
3. **(B)**: infra `infrastructure/partnership/query/PartnershipQueryDao` 신설(패턴 3 — admin 소비 메서드: 목록/상세), Result·Condition은 infra query 소유. admin `PartnershipQueryService`(readOnly)가 DAO 주입. `PartnershipRepository` write 순수화(패턴 4).
4. core `partnership/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.

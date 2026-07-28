# bug 도메인 전환

> 선행: `tasks/README.md` + `00-phase0`. 그룹 1(병렬 가능).

## 현황
- core: `bug/application/` — `BugReportCommandService`(2 repo: BugReport+BugReportImage), QueryService + result. 애그리거트 2개(plain FK 연결).
- 소비자: web-api(제보 등록·내 제보 조회, 이미지 업로드 동반), admin-api(상태변경·분류·담당자 배정).

## 작업
1. **(C) 판정**: 제보 생성이 BugReport save + BugReportImage 복수 save를 한 트랜잭션에서 수행하면 (C) → 도메인 서비스 `BugReportRegistrationService`(가칭)로 하강. `changeStatus`/`classify`/`assign`은 단일 애그리거트 → (A)로 admin 서비스에 흡수.
2. **(B)**: infra `infrastructure/bug/query/BugReportQueryDao` 신설(패턴 3 — web용 내 제보 목록/상세+이미지 서브쿼리, admin용 관리 목록/검색 메서드 분리). `BugReportSearchCondition`·Result는 infra query 패키지 소유(충돌 시 `Management` 한정어). web/admin 각 `BugReportQueryService`(readOnly)가 DAO 주입. Repository 2개 write 순수화(패턴 4).
3. **(A)**: web/admin 각각 `BugReportCommandService`(@Transactional)로 core command 로직 병합(패턴 2 — 기존 facade `BugReportService`는 Command/Query로 분해). Command DTO 삭제.
4. core `bug/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.

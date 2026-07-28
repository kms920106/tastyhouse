# rank 도메인 전환

> 선행: `tasks/README.md` + `00-phase0` + 그룹 1 완료. 그룹 2(병렬 가능). **batch-module 소비자 있음.**

## 현황
- core: `rank/application/` — `RankCommandService`(3 repo: RankPeriod+RankPrize+MemberReviewRank), QueryService + result(`MemberReviewCountResult` 등). `RankInfoRepositoryImpl`은 file/member Q타입 크로스 조인.
- 소비자: admin-api(기간/상품 관리), web-api(랭킹 조회), **batch-module**(`RankCommandService` — 랭킹 집계 적재).

## 작업
1. **(C) 판정**: 랭킹 확정/집계(기간 마감 + MemberReviewRank 일괄 적재 등 다중 애그리거트 원자 연산)는 `RankSettlementService`(가칭) 도메인 서비스로 하강 — batch가 트리거하는 액터 무관 연산. `updatePeriod`/`updatePrize`는 단일 애그리거트 → (A).
2. batch-module에 자기 application 서비스 신설(`@Transactional`)해 도메인 서비스 호출 — core `RankCommandService` import 제거.
3. **(A)**: admin `RankCommandService`(@Transactional)로 기간/상품 CRUD 흡수(패턴 2 — 기존 facade `RankService`는 Command/Query로 분해, 소프트 삭제 규칙 유지).
4. **(B)**: infra `infrastructure/rank/query/RankQueryDao` 신설(패턴 3 — web용 랭킹 목록 file/member 크로스 조인 투영, admin용 관리 조회 메서드 분리). Result는 infra query 소유(충돌 시 `Management` 한정어). web/admin 각 `RankQueryService`(readOnly)가 DAO 주입. Repository write 순수화(`RankInfoRepositoryImpl`의 순수 조회는 통째로 이 DAO로 흡수 가능 — 같은 모듈 내 이동)(패턴 4).
5. core `rank/application/` 삭제.

## 완료 기준
- batch 포함 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.

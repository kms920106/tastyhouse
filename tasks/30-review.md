# review 도메인 전환

> 선행: `tasks/README.md` + `00-phase0` + 그룹 2 완료. 그룹 3(병렬 가능). **최대 불변식 덩어리(7 repo)·batch 소비자.**

## 현황
- core: `review/application/` — `ReviewCommandService`(**7 repo**: Review+Comment+Reply+Image+Like+Tag 등, 이벤트 발행 — `ProductReviewEventListener`가 상품 통계 갱신에 반응), `ReviewQueryService`(**batch-module도 사용**), result 다수. `ReviewRepositoryImpl`은 file/order/product/shop/member Q타입 크로스 조인.
- 소비자: web-api(`webapi/review/ReviewService` — hot path), admin-api(리뷰 숨김/댓글 관리), batch-module(`ReviewQueryService`).

## 작업
1. **(C) 하강**: 리뷰 등록(Review+Image+Tag 원자 save + 이벤트 발행), 리뷰 삭제(연쇄 정리), 숨김 연쇄(`changeReviewHidden` 등)를 `ReviewLifecycleService`(가칭) 도메인 서비스로 하강. 이벤트 발행은 `DomainEventPublisher`. 좋아요 토글(Like+집계)이 원자 연산이면 포함.
2. **(E)**: 리뷰 이벤트에 반응하는 `ProductReviewEventListener`는 **32-product 작업 소관** — 이 작업에서는 발행측만 포트로 교체하고 리스너는 건드리지 말 것(인계 메모 남김).
3. **(A)**: 댓글/답글 작성·수정, 개별 숨김 등 단일 애그리거트 연산은 web/admin 각자 `ReviewCommandService`(@Transactional)로 흡수(패턴 2 — 기존 facade `ReviewService`는 Command/Query로 분해, 명시적 save 유지).
4. **(B)**: infra `infrastructure/review/query/ReviewQueryDao` 신설(패턴 3 — web용 `findBestReviews`·별점별·목록 대형 크로스 조인 투영, admin용 숨김 포함 목록 `findCommentsIncludingHidden` 등, batch용 집계 조회까지 **소비자별 메서드 분리** — batch도 이 infra DAO를 소비). 대형 도메인이므로 400줄 초과 시 용도별 DAO 분리 허용(README 패턴 3). Result는 infra query 소유(충돌 시 `Management` 한정어). web/admin/batch 각 `ReviewQueryService`(readOnly)가 DAO 주입. Repository 6개 write 순수화(패턴 4).
5. core `review/application/` 삭제.

## 완료 기준
- batch 포함 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시. `ReviewLifecycleService` 시그니처 하단 기록(32-product 리스너 개편 시 참조).

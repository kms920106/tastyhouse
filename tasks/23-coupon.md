# coupon 도메인 전환

> 선행: `tasks/README.md` + `00-phase0` + 그룹 1 완료. 그룹 2(병렬 가능). **이벤트 리스너 보유.**

## 현황
- core: `coupon/application/` — `CouponCommandService`(2 repo: Coupon+MemberCoupon, 이벤트 발행), `MemberCouponEventListener`(가입/추천 등 이벤트 반응 발급 추정), QueryService + result(`CouponListItemResult`, `MemberCouponAdminItemResult` 등).
- 소비자: admin-api(쿠폰 CRUD·발급·현황), web-api(내 쿠폰 조회·사용).

## 작업
1. **(C) 하강**: `issueCoupon`(Coupon 발급수량 검증/증가 + MemberCoupon 생성의 원자 연산)과 쿠폰 사용 처리(MemberCoupon 상태전이 + 검증)를 `CouponIssueService`(가칭) 도메인 서비스로 하강. 이벤트 발행은 `DomainEventPublisher`로.
2. **(E)**: `MemberCouponEventListener` → infrastructure `coupon/listener/`, 본문은 `CouponIssueService` 호출로 축소.
3. **(A)**: 쿠폰 CRUD(update/delete — 단일 애그리거트)는 admin `CouponCommandService`(@Transactional)로 흡수(패턴 2 — 기존 facade `CouponService`는 Command/Query로 분해, 명시적 save 유지).
4. **(B)**: infra `infrastructure/coupon/query/CouponQueryDao` 신설(패턴 3 — admin용 쿠폰 목록·발급 현황 조인 조회, web용 내 쿠폰 메서드 분리). Result·Condition은 infra query 소유(충돌 시 `Management` 한정어). admin/web 각 `CouponQueryService`(readOnly)가 DAO 주입. Repository 2개 write 순수화(패턴 4).
5. core `coupon/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시. `CouponIssueService` 시그니처를 파일 하단에 기록(40-order 등에서 쿠폰 사용 연동 시 참조).

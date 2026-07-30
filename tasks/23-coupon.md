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

## 결과 — `CouponIssueService` 시그니처 (40-order/41-payment 연동 시 참조)

위치: `core-module/.../coupon/domain/service/CouponIssueService.java` (순수 POJO, `@Service`/`@Transactional` 없음).
빈 등록: `infrastructure-module`의 `DomainServiceConfig#couponIssueService`.
생성자: `CouponIssueService(CouponRepository, MemberCouponRepository, DomainEventPublisher)`.

```java
// 관리자 수동 발급 — 쿠폰 존재·중복 보유 검증 후 쿠폰의 useEndAt을 만료일로 승계.
// 실패: COUPON_NOT_FOUND(404), COUPON_ALREADY_ISSUED(409)
MemberCouponId issueCoupon(MemberId memberId, CouponId couponId)

// 주문 결제 시 쿠폰 사용 — 소유권·사용가능·최소주문금액 검증 후 할인액 산출 + 사용 상태 전이.
// 실패: MEMBER_COUPON_NOT_FOUND(404), COUPON_ACCESS_DENIED(403), COUPON_INFO_NOT_FOUND(404), COUPON_NOT_AVAILABLE(400)
CouponUseResult useCoupon(MemberCouponId memberCouponId, MemberId memberId, int orderAmountAfterProductDiscount)
```

`CouponUseResult`(같은 패키지): `record CouponUseResult(Long memberCouponId, int couponDiscountAmount)`.

**호출자 주의**: 트랜잭션 경계가 없는 POJO이므로 **반드시 `@Transactional` 안에서 호출**한다. 트랜잭션 밖에서
호출하면 두 애그리거트 저장의 원자성이 깨지고, `@TransactionalEventListener(AFTER_COMMIT)`로 등록된
`CouponEventListener`가 이벤트를 받지 못한다. 현재 호출자는 core `OrderCommandService#createOrder`(주문
결제 시 사용)와 admin `CouponCommandService#issueCoupon`(수동 발급) 둘 다 `@Transactional`이다.

> 40-order 전환 시: `OrderCommandService`는 이번 작업에서 호출부만 복구했다(구 `CouponCommandService.useCoupon(UseCouponCommand)`
> → `couponIssueService.useCoupon(MemberCouponId.of(...), memberId, amount)`). order를 소비 모듈로 하강시킬 때
> 이 도메인 서비스를 그대로 주입하면 된다.

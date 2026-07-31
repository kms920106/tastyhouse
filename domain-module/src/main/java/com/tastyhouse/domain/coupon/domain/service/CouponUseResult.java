package com.tastyhouse.domain.coupon.domain.service;

/**
 * 쿠폰 사용 결과.
 *
 * <p>{@link CouponIssueService#useCoupon}이 반환하는 도메인 서비스 산출물이다. 표현 목적 read model이
 * 아니라 "쿠폰을 사용했다"는 명령의 결과(사용된 회원 쿠폰 식별자 + 산출된 할인액)이므로, infra query
 * 패키지가 아니라 도메인 서비스와 같은 패키지에 둔다. 주문 생성 트랜잭션이 이 할인액을 주문 금액
 * 계산에 그대로 사용한다.
 */
public record CouponUseResult(
    Long memberCouponId,
    int couponDiscountAmount
) {

    public static CouponUseResult of(Long memberCouponId, int couponDiscountAmount) {
        return new CouponUseResult(memberCouponId, couponDiscountAmount);
    }
}

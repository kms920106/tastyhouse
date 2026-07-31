package com.tastyhouse.infrastructure.coupon.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.coupon.domain.model.DiscountType;

/**
 * 내 쿠폰 목록 아이템(web — 회원이 보유한 쿠폰 + 원본 쿠폰 정보 조인).
 *
 * <p>발급 현황({@link MemberCouponItemResult})과 필드 셋이 달라 통합하지 않는다(과잉 노출 방지).
 */
public record MemberCouponResult(
    Long id,
    Long couponId,
    String name,
    String description,
    DiscountType discountType,
    Integer discountAmount,
    Integer maxDiscountAmount,
    Integer minOrderAmount,
    LocalDateTime useStartAt,
    LocalDateTime useEndAt,
    LocalDateTime expiredAt,
    boolean used,
    LocalDateTime usedAt
) {

    @QueryProjection
    public MemberCouponResult {
    }
}

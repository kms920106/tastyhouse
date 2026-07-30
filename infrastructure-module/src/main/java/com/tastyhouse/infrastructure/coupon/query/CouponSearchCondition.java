package com.tastyhouse.infrastructure.coupon.query;

import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;

/**
 * 쿠폰 목록 검색 조건.
 *
 * <p>표현 목적 read의 입력이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다. 세 필드 모두
 * null이면 조건을 적용하지 않는다(전체 조회).
 */
public record CouponSearchCondition(
    String name,
    DiscountType discountType,
    Boolean visible
) {

    public static CouponSearchCondition of(String name, DiscountType discountType, Boolean visible) {
        return new CouponSearchCondition(name, discountType, visible);
    }
}

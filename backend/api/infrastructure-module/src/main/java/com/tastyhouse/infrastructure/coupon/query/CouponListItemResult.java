package com.tastyhouse.infrastructure.coupon.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.coupon.model.DiscountType;

/**
 * 쿠폰 목록 조회 결과(admin 쿠폰 관리 목록).
 *
 * <p>표현 목적 read의 산출물이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다. 비-admin
 * 형제가 없어 {@code Management} 한정어 없이 순수명을 쓴다.
 */
public record CouponListItemResult(
    Long id,
    String name,
    DiscountType discountType,
    Integer discountAmount,
    Integer maxDiscountAmount,
    Integer minOrderAmount,
    Integer maxDiscountCount,
    LocalDateTime issueStartAt,
    LocalDateTime issueEndAt,
    LocalDateTime useStartAt,
    LocalDateTime useEndAt,
    boolean visible
) {

    @QueryProjection
    public CouponListItemResult {
    }
}

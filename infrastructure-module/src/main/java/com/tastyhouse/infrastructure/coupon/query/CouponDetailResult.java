package com.tastyhouse.infrastructure.coupon.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.coupon.domain.model.DiscountType;

/**
 * 쿠폰 상세 조회 결과(admin 쿠폰 상세 화면).
 *
 * <p>이관 전에는 도메인 모델({@code Coupon})을 받아 {@code from(...)}으로 조립했으나, 화면 조립용 read일
 * 뿐 불변식 검증에 쓰이지 않으므로 JPA 엔티티에서 직접 투영하는 방식으로 바꿨다(목록·발급현황 조회와
 * 같은 경로). 덕분에 admin 상세 조회가 write 포트({@code CouponRepository#findById})를 거치지 않는다.
 */
public record CouponDetailResult(
    Long id,
    String name,
    String description,
    DiscountType discountType,
    Integer discountAmount,
    Integer maxDiscountAmount,
    Integer minOrderAmount,
    Integer maxDiscountCount,
    LocalDateTime issueStartAt,
    LocalDateTime issueEndAt,
    LocalDateTime useStartAt,
    LocalDateTime useEndAt,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    @QueryProjection
    public CouponDetailResult {
    }
}

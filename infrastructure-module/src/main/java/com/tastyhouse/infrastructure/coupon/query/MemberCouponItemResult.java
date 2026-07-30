package com.tastyhouse.infrastructure.coupon.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

/**
 * 쿠폰 발급 현황 아이템(admin — 특정 쿠폰을 보유한 회원 목록).
 *
 * <p>회원 관점의 보유 쿠폰 목록({@link MemberCouponResult})과 소비자·필드 셋이 달라 통합하지 않는다.
 * 이쪽은 "누가 받았는지"를, 저쪽은 "어떤 쿠폰인지"를 보여준다.
 */
public record MemberCouponItemResult(
    Long id,
    MemberId memberId,
    boolean used,
    LocalDateTime usedAt,
    LocalDateTime expiredAt,
    LocalDateTime createdAt
) {

    @QueryProjection
    public MemberCouponItemResult {
    }
}

package com.tastyhouse.core.domain.coupon.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.coupon.domain.model.MemberCoupon;
import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.domain.coupon.domain.vo.MemberCouponId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

/**
 * 회원 쿠폰 write 포트.
 *
 * <p>표현 목적 조회(내 쿠폰함·발급 현황)는 infrastructure-module의 {@code CouponQueryDao}가 담당하므로
 * 이 포트에는 command 경로와 도메인 서비스가 트랜잭션 안에서 소비하는 것만 남긴다. 중복 발급 검증용
 * {@code existsByMemberIdAndCouponId}는 이 조회가 없으면 "같은 쿠폰을 두 번 발급하지 않는다"는 불변식을
 * 검증할 수 없으므로 write 포트에 잔류한다.
 */
public interface MemberCouponRepository {

    Optional<MemberCoupon> findById(MemberCouponId id);

    boolean existsByMemberIdAndCouponId(MemberId memberId, CouponId couponId);

    MemberCoupon save(MemberCoupon memberCoupon);
}

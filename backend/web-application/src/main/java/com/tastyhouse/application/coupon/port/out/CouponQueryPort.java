package com.tastyhouse.application.coupon.port.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 회원 보유 쿠폰 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>회원이 자기 쿠폰함을 보는 조회만 다룬다. 쿠폰 자체를 발행·검수하는 관리 화면 조회는
 * {@code CouponManagementQueryPort}가 소유한다 — 두 계약은 공유 메서드가 0개다.
 */
public interface CouponQueryPort {

    List<MemberCouponResult> findMemberCoupons(Long memberId);

    List<MemberCouponResult> findAvailableMemberCoupons(Long memberId, LocalDateTime now);
}

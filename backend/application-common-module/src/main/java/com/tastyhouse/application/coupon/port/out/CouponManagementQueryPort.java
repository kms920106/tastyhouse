package com.tastyhouse.application.coupon.port.out;

import java.util.Optional;

import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 쿠폰 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>쿠폰 정의 목록·상세와 그 쿠폰이 어떤 회원에게 발급됐는지를 조회한다. 회원 쿠폰함 조회는
 * {@link CouponQueryPort}가 소유한다.
 */
public interface CouponManagementQueryPort {

    PageResult<CouponListItemResult> findAllCoupons(CouponSearchCondition condition, PageQuery pageQuery);

    Optional<CouponDetailResult> findCouponDetailById(CouponId couponId);

    PageResult<MemberCouponItemResult> findIssuedMemberCoupons(CouponId couponId, PageQuery pageQuery);
}

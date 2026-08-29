package com.tastyhouse.application.coupon.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * coupon 읽기 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>완전 매핑 전환으로 <b>응용 계층이 읽기 계약을 소유</b>하고 infrastructure-module의
 * {@code CouponQueryDao}가 이를 구현한다. 소비 모듈은 이 인터페이스와 같은 패키지의 반환 DTO
 * ({@code *Result})·검색 조건({@code *SearchCondition})만 알며, QueryDSL도 어댑터의 존재도 알지 않는다.
 *
 * <p>메서드명·시그니처는 DAO의 기존 공개 표면을 그대로 전사한 것이다(챕터 04는 순수 소유권 이동이라
 * 조회 동작·wire 계약을 바꾸지 않는다).
 */
public interface CouponQueryPort {

    PageResult<CouponListItemResult> findAllCoupons(CouponSearchCondition condition, PageQuery pageQuery);

    Optional<CouponDetailResult> findCouponDetailById(CouponId couponId);

    PageResult<MemberCouponItemResult> findIssuedMemberCoupons(CouponId couponId, PageQuery pageQuery);

    List<MemberCouponResult> findMemberCoupons(Long memberId);

    List<MemberCouponResult> findAvailableMemberCoupons(Long memberId, LocalDateTime now);
}

package com.tastyhouse.core.domain.coupon.application;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.coupon.domain.model.Coupon;
import com.tastyhouse.core.domain.coupon.domain.repository.CouponRepository;
import com.tastyhouse.core.domain.coupon.domain.repository.MemberCouponRepository;
import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.coupon.application.dto.CouponAdminListItemDto;
import com.tastyhouse.core.domain.coupon.application.dto.CouponDetailDto;
import com.tastyhouse.core.domain.coupon.application.dto.CouponSearchCondition;
import com.tastyhouse.core.domain.coupon.application.dto.MemberCouponAdminItemDto;
import com.tastyhouse.core.domain.coupon.application.dto.result.MemberCouponResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponQueryService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;

    public List<MemberCouponResult> findMemberCoupons(MemberId memberId) {
        return memberCouponRepository.findWithCouponByMemberId(memberId);
    }

    public List<MemberCouponResult> findAvailableMemberCoupons(MemberId memberId) {
        return memberCouponRepository.findAvailableWithCouponByMemberId(memberId, LocalDateTime.now());
    }

    public Coupon findById(CouponId id) {
        return couponRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }

    public PageResult<CouponAdminListItemDto> findAllCoupons(CouponSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return couponRepository.findAllCoupons(condition, pageQuery);
    }

    public CouponDetailDto findDetailById(CouponId id) {
        return CouponDetailDto.from(findById(id));
    }

    public PageResult<MemberCouponAdminItemDto> findIssuedByCoupon(CouponId couponId, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return memberCouponRepository.findByCouponId(couponId, pageQuery);
    }
}

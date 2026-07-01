package com.tastyhouse.core.domain.coupon.application;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.coupon.application.dto.result.MemberCouponResult;
import com.tastyhouse.core.domain.coupon.domain.model.Coupon;
import com.tastyhouse.core.domain.coupon.domain.model.CouponId;
import com.tastyhouse.core.domain.coupon.domain.repository.CouponRepository;
import com.tastyhouse.core.domain.coupon.domain.repository.MemberCouponRepository;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponQueryService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;

    public List<MemberCouponResult> findMemberCoupons(Long memberId) {
        return memberCouponRepository.findWithCouponByMemberId(memberId);
    }

    public List<MemberCouponResult> findAvailableMemberCoupons(Long memberId) {
        return memberCouponRepository.findAvailableWithCouponByMemberId(memberId, LocalDateTime.now());
    }

    public Coupon findById(CouponId id) {
        return couponRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }
}

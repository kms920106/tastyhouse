package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.coupon.Coupon;
import com.tastyhouse.core.entity.coupon.MemberCoupon;
import com.tastyhouse.core.repository.coupon.CouponJpaRepository;
import com.tastyhouse.core.repository.coupon.CouponRepository;
import com.tastyhouse.core.repository.coupon.MemberCouponJpaRepository;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponCoreService {

    private final CouponRepository couponRepository;
    private final CouponJpaRepository couponJpaRepository;
    private final MemberCouponJpaRepository memberCouponJpaRepository;

    @Transactional(readOnly = true)
    public List<MemberCoupon> findMemberCoupons(Long memberId) {
        return couponRepository.findMemberCouponsByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public List<MemberCoupon> findAvailableMemberCoupons(Long memberId) {
        return couponRepository.findAvailableMemberCouponsByMemberId(memberId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Optional<Coupon> findById(Long couponId) {
        return couponJpaRepository.findById(couponId);
    }

    @Transactional(readOnly = true)
    public MemberCoupon findMemberCouponById(Long id) {
        return memberCouponJpaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_COUPON_NOT_FOUND));
    }
}

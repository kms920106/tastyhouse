package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.coupon.MemberCoupon;
import com.tastyhouse.core.repository.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponCoreService {

    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public List<MemberCoupon> findMemberCoupons(Long memberId) {
        return couponRepository.findMemberCouponsByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public List<MemberCoupon> findAvailableMemberCoupons(Long memberId) {
        return couponRepository.findAvailableMemberCouponsByMemberId(memberId, LocalDateTime.now());
    }
}

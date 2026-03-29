package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.coupon.Coupon;
import com.tastyhouse.core.entity.coupon.MemberCoupon;
import com.tastyhouse.core.repository.coupon.CouponRepository;
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

    @Transactional(readOnly = true)
    public Optional<Coupon> findCouponById(Long couponId) {
        return couponRepository.findCouponById(couponId);
    }

    @Transactional(readOnly = true)
    public List<Coupon> findActiveCoupons() {
        return couponRepository.findActiveCoupons();
    }

    @Transactional(readOnly = true)
    public List<Coupon> findIssuableCoupons(LocalDateTime currentTime) {
        return couponRepository.findIssuableCoupons(currentTime);
    }

    @Transactional(readOnly = true)
    public List<MemberCoupon> findMemberCoupons(Long memberId) {
        return couponRepository.findMemberCouponsByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public List<MemberCoupon> findAvailableMemberCoupons(Long memberId) {
        return couponRepository.findAvailableMemberCouponsByMemberId(memberId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Optional<MemberCoupon> findMemberCouponById(Long memberCouponId) {
        return couponRepository.findMemberCouponById(memberCouponId);
    }

    @Transactional(readOnly = true)
    public Optional<MemberCoupon> findMemberCoupon(Long memberId, Long couponId) {
        return couponRepository.findMemberCouponByMemberIdAndCouponId(memberId, couponId);
    }

    @Transactional(readOnly = true)
    public boolean existsMemberCoupon(Long memberId, Long couponId) {
        return couponRepository.existsMemberCouponByMemberIdAndCouponId(memberId, couponId);
    }

    @Transactional
    public Coupon saveCoupon(Coupon coupon) {
        return couponRepository.saveCoupon(coupon);
    }

    @Transactional
    public MemberCoupon saveMemberCoupon(MemberCoupon memberCoupon) {
        return couponRepository.saveMemberCoupon(memberCoupon);
    }

    @Transactional
    public void deleteMemberCoupon(Long memberCouponId) {
        couponRepository.deleteMemberCouponById(memberCouponId);
    }
}

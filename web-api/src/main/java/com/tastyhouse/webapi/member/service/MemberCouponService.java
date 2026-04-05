package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.entity.coupon.Coupon;
import com.tastyhouse.core.entity.coupon.MemberCoupon;
import com.tastyhouse.core.service.CouponCoreService;
import com.tastyhouse.webapi.member.response.MemberCouponListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberCouponService {

    private final CouponCoreService couponCoreService;

    // 회원이 보유한 전체 쿠폰 목록을 조회
    @Transactional(readOnly = true)
    public List<MemberCouponListItemResponse> getMemberCoupons(Long memberId) {
        List<MemberCoupon> memberCoupons = couponCoreService.findMemberCoupons(memberId);

        if (memberCoupons.isEmpty()) {
            return List.of();
        }

        // 쿠폰 ID 목록 추출
        List<Long> couponIds = memberCoupons.stream().map(MemberCoupon::getCouponId).distinct().toList();

        // 쿠폰 정보 조회
        Map<Long, Coupon> couponMap = couponIds.stream().map(couponCoreService::findById).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toMap(Coupon::getId, coupon -> coupon));

        // 응답 생성
        return memberCoupons.stream().map(memberCoupon -> {
            Coupon coupon = couponMap.get(memberCoupon.getCouponId());
            if (coupon == null) {
                return null;
            }

            return MemberCouponListItemResponse.of(memberCoupon.getId(), coupon.getId(), coupon.getName(), coupon.getDescription(), coupon.getDiscountType(), coupon.getDiscountAmount(), coupon.getMaxDiscountAmount(), coupon.getMinOrderAmount(), coupon.getUseStartAt(), coupon.getUseEndAt(), memberCoupon.getExpiredAt(), memberCoupon.getIsUsed(), memberCoupon.getUsedAt());
        }).filter(Objects::nonNull).toList();
    }

    // 회원이 현재 사용 가능한 쿠폰 목록만 조회
    @Transactional(readOnly = true)
    public List<MemberCouponListItemResponse> getAvailableMemberCoupons(Long memberId) {
        List<MemberCoupon> memberCoupons = couponCoreService.findAvailableMemberCoupons(memberId);

        if (memberCoupons.isEmpty()) {
            return List.of();
        }

        // 쿠폰 ID 목록 추출
        List<Long> couponIds = memberCoupons.stream().map(MemberCoupon::getCouponId).distinct().toList();

        // 쿠폰 정보 조회
        Map<Long, Coupon> couponMap = couponIds.stream().map(couponCoreService::findById).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toMap(Coupon::getId, coupon -> coupon));

        // 응답 생성
        return memberCoupons.stream().map(memberCoupon -> {
            Coupon coupon = couponMap.get(memberCoupon.getCouponId());
            if (coupon == null) {
                return null;
            }

            return MemberCouponListItemResponse.of(memberCoupon.getId(), coupon.getId(), coupon.getName(), coupon.getDescription(), coupon.getDiscountType(), coupon.getDiscountAmount(), coupon.getMaxDiscountAmount(), coupon.getMinOrderAmount(), coupon.getUseStartAt(), coupon.getUseEndAt(), memberCoupon.getExpiredAt(), memberCoupon.getIsUsed(), memberCoupon.getUsedAt());
        }).filter(Objects::nonNull).toList();
    }
}

package com.tastyhouse.webapi.member;

import com.tastyhouse.core.entity.coupon.MemberCoupon;
import com.tastyhouse.core.service.CouponCoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberCouponService {

    private final CouponCoreService couponCoreService;

    public MemberCoupon findById(Long id) {
        return couponCoreService.findMemberCouponById(id);
    }
}

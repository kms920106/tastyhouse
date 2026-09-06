package com.tastyhouse.application.coupon.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.coupon.port.out.CouponQueryPort;
import com.tastyhouse.application.coupon.port.out.MemberCouponResult;
import com.tastyhouse.application.coupon.port.in.CouponQueryUseCase;
import com.tastyhouse.application.coupon.port.out.MyCouponListItemResult;

@Service
@WebApp
@Transactional(readOnly = true)
public class CouponQueryService implements CouponQueryUseCase {

    private final CouponQueryPort couponQueryPort;

    public CouponQueryService(CouponQueryPort couponQueryPort) {
        this.couponQueryPort = couponQueryPort;
    }

    @Override
    public List<MyCouponListItemResult> getMyCoupons(Long memberId) {
        return couponQueryPort.findMemberCoupons(memberId)
            .stream()
            .map(this::toMyCouponListItemResult)
            .toList();
    }

    @Override
    public List<MyCouponListItemResult> getMyAvailableCoupons(Long memberId) {
        return couponQueryPort.findAvailableMemberCoupons(memberId, LocalDateTime.now())
            .stream()
            .map(this::toMyCouponListItemResult)
            .toList();
    }

    private MyCouponListItemResult toMyCouponListItemResult(MemberCouponResult dto) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = dto.expiredAt();
        boolean used = dto.used();

        Long daysRemaining = null;
        if (!used && expiredAt != null && now.isBefore(expiredAt)) {
            daysRemaining = ChronoUnit.DAYS.between(now, expiredAt);
        }

        boolean expired = expiredAt != null && now.isAfter(expiredAt);

        return new MyCouponListItemResult(
            dto.id(),
            dto.couponId(),
            dto.name(),
            dto.description(),
            dto.discountType().name(),
            dto.discountAmount(),
            dto.maxDiscountAmount(),
            dto.minOrderAmount(),
            dto.useStartAt(),
            dto.useEndAt(),
            expiredAt,
            used,
            dto.usedAt(),
            daysRemaining,
            expired
        );
    }
}

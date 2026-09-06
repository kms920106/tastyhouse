package com.tastyhouse.domain.coupon.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class MemberCoupon {
    private final Long id;
    private final MemberId memberId;
    private final CouponId couponId;
    private boolean used;
    private LocalDateTime usedAt;
    private final LocalDateTime expiredAt;

    private MemberCoupon(
        Long id,
        MemberId memberId,
        CouponId couponId,
        boolean used,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.couponId = couponId;
        this.used = used;
        this.usedAt = usedAt;
        this.expiredAt = expiredAt;
    }

    public static MemberCoupon of(
        MemberId memberId,
        CouponId couponId,
        boolean used,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        return new MemberCoupon(null, memberId, couponId, used, usedAt, expiredAt);
    }

    public static MemberCoupon reconstitute(
        Long id,
        MemberId memberId,
        CouponId couponId,
        boolean used,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        return new MemberCoupon(id, memberId, couponId, used, usedAt, expiredAt);
    }

    public MemberCouponId getMemberCouponId() {
        return MemberCouponId.of(this.id);
    }

    public void use() {
        if (!isAvailable()) {
            throw new BusinessException(ErrorCode.COUPON_NOT_AVAILABLE);
        }
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiredAt != null && LocalDateTime.now().isAfter(expiredAt);
    }

    public boolean isAvailable() {
        return !used && !isExpired();
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public CouponId getCouponId() {
        return this.couponId;
    }

    public boolean isUsed() {
        return this.used;
    }

    public LocalDateTime getUsedAt() {
        return this.usedAt;
    }

    public LocalDateTime getExpiredAt() {
        return this.expiredAt;
    }
}

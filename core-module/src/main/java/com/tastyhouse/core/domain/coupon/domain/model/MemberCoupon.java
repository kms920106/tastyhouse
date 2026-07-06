package com.tastyhouse.core.domain.coupon.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.coupon.domain.vo.MemberCouponId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.entity.BaseEntity;

@Getter
@Entity
@Table(
    name = "MEMBER_COUPON",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_member_coupon",
            columnNames = {"member_id", "coupon_id"}
        )
    },
    indexes = {
        @Index(name = "idx_member_coupon_member_id", columnList = "member_id"),
        @Index(name = "idx_member_coupon_coupon_id", columnList = "coupon_id"),
        @Index(name = "idx_member_coupon_used", columnList = "member_id, is_used")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    private MemberCoupon(
        Long memberId,
        Long couponId,
        boolean used,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        this.memberId = memberId;
        this.couponId = couponId;
        this.used = used;
        this.usedAt = usedAt;
        this.expiredAt = expiredAt;
    }

    public static MemberCoupon of(
        Long memberId,
        Long couponId,
        boolean used,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        return new MemberCoupon(memberId, couponId, used, usedAt, expiredAt);
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
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public boolean isAvailable() {
        return !used && !isExpired();
    }
}

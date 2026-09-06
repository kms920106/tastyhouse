package com.tastyhouse.infrastructure.coupon.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

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
public class MemberCouponJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    protected MemberCouponJpaEntity() {
    }

    private MemberCouponJpaEntity(
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

    static MemberCouponJpaEntity create(
        Long memberId,
        Long couponId,
        boolean used,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        return new MemberCouponJpaEntity(memberId, couponId, used, usedAt, expiredAt);
    }

    void applyChanges(boolean used, LocalDateTime usedAt) {
        this.used = used;
        this.usedAt = usedAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public Long getCouponId() {
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

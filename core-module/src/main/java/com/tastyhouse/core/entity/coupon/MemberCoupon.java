package com.tastyhouse.core.entity.coupon;

import com.tastyhouse.core.entity.BaseEntity;
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

import java.time.LocalDateTime;

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
    private Long id; // PK

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 회원 ID (MEMBER.id 참조)

    @Column(name = "coupon_id", nullable = false)
    private Long couponId; // 쿠폰 ID (COUPON.id 참조)

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false; // 사용 여부 (true: 사용)

    @Column(name = "used_at")
    private LocalDateTime usedAt; // 사용 일시

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt; // 만료 일시

    private MemberCoupon(
        Long memberId,
        Long couponId,
        Boolean isUsed,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        this.memberId = memberId;
        this.couponId = couponId;
        this.isUsed = isUsed != null ? isUsed : false;
        this.usedAt = usedAt;
        this.expiredAt = expiredAt;
    }

    public static MemberCoupon of(
        Long memberId,
        Long couponId,
        Boolean isUsed,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
    ) {
        return new MemberCoupon(
            memberId,
            couponId,
            isUsed,
            usedAt,
            expiredAt
        );
    }

    public void use() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public boolean isAvailable() {
        return !isUsed && !isExpired();
    }
}

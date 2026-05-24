package com.tastyhouse.core.domain.referral.domain.model;

import com.tastyhouse.core.domain.referral.domain.vo.ReferralId;
import com.tastyhouse.core.entity.BaseEntity;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "MEMBER_REFERRAL",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_member_referral_referee_id",
        columnNames = {"referee_id"}
    ),
    indexes = {
        @Index(name = "idx_member_referral_referrer_id", columnList = "referrer_id"),
        @Index(name = "idx_member_referral_status", columnList = "status")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberReferral extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referrer_id", nullable = false)
    private Long referrerId;

    @Column(name = "referee_id", nullable = false)
    private Long refereeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ReferralStatus status;

    private MemberReferral(Long referrerId, Long refereeId) {
        this.referrerId = referrerId;
        this.refereeId = refereeId;
        this.status = ReferralStatus.PENDING;
    }

    public static MemberReferral register(Long referrerId, Long refereeId) {
        return new MemberReferral(referrerId, refereeId);
    }

    public ReferralId getReferralId() {
        return new ReferralId(this.id);
    }

    public void reward() {
        if (this.status != ReferralStatus.PENDING) {
            throw new BusinessException(ErrorCode.REFERRAL_INVALID_STATUS);
        }
        this.status = ReferralStatus.REWARDED;
    }

    public void cancel() {
        if (this.status != ReferralStatus.PENDING) {
            throw new BusinessException(ErrorCode.REFERRAL_INVALID_STATUS);
        }
        this.status = ReferralStatus.CANCELLED;
    }
}

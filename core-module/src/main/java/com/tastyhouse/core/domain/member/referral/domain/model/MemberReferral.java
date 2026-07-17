package com.tastyhouse.core.domain.member.referral.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.referral.domain.vo.ReferralId;
import com.tastyhouse.core.domain.member.infrastructure.persistence.converter.MemberIdConverter;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.entity.BaseEntity;

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

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "referrer_id", nullable = false)
    private MemberId referrerId;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "referee_id", nullable = false)
    private MemberId refereeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private MemberReferralStatus status;

    private MemberReferral(MemberId referrerId, MemberId refereeId) {
        this.referrerId = referrerId;
        this.refereeId = refereeId;
        this.status = MemberReferralStatus.PENDING;
    }

    public static MemberReferral register(MemberId referrerId, MemberId refereeId) {
        return new MemberReferral(referrerId, refereeId);
    }

    public ReferralId getReferralId() {
        return new ReferralId(this.id);
    }

    public void reward() {
        if (this.status != MemberReferralStatus.PENDING) {
            throw new BusinessException(ErrorCode.REFERRAL_INVALID_STATUS);
        }
        this.status = MemberReferralStatus.REWARDED;
    }

    public void cancel() {
        if (this.status != MemberReferralStatus.PENDING) {
            throw new BusinessException(ErrorCode.REFERRAL_INVALID_STATUS);
        }
        this.status = MemberReferralStatus.CANCELLED;
    }
}

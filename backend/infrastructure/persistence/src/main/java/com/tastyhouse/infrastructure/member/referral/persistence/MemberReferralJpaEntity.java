package com.tastyhouse.infrastructure.member.referral.persistence;

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

import com.tastyhouse.domain.member.referral.model.MemberReferralStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

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
public class MemberReferralJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referrer_id", nullable = false)
    private Long referrerId;

    @Column(name = "referee_id", nullable = false)
    private Long refereeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private MemberReferralStatus status;

    protected MemberReferralJpaEntity() {
    }

    private MemberReferralJpaEntity(Long referrerId, Long refereeId, MemberReferralStatus status) {
        this.referrerId = referrerId;
        this.refereeId = refereeId;
        this.status = status;
    }

    static MemberReferralJpaEntity create(Long referrerId, Long refereeId, MemberReferralStatus status) {
        return new MemberReferralJpaEntity(referrerId, refereeId, status);
    }

    void applyChanges(MemberReferralStatus status) {
        this.status = status;
    }

    public Long getId() {
        return this.id;
    }

    public Long getReferrerId() {
        return this.referrerId;
    }

    public Long getRefereeId() {
        return this.refereeId;
    }

    public MemberReferralStatus getStatus() {
        return this.status;
    }
}

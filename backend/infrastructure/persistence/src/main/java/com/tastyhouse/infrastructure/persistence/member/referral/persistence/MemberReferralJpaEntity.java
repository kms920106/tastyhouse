package com.tastyhouse.infrastructure.persistence.member.referral.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.infrastructure.persistence.shared.persistence.BaseEntity;

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
class MemberReferralJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referrer_id", nullable = false)
    private Long referrerId;

    @Column(name = "referee_id", nullable = false)
    private Long refereeId;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String status;

    protected MemberReferralJpaEntity() {
    }

    private MemberReferralJpaEntity(Long referrerId, Long refereeId, String status) {
        this.referrerId = referrerId;
        this.refereeId = refereeId;
        this.status = status;
    }

    static MemberReferralJpaEntity create(Long referrerId, Long refereeId, String status) {
        return new MemberReferralJpaEntity(referrerId, refereeId, status);
    }

    void applyChanges(String status) {
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

    public String getStatus() {
        return this.status;
    }
}

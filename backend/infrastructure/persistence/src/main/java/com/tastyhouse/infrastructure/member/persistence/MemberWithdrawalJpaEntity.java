package com.tastyhouse.infrastructure.member.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "MEMBER_WITHDRAWAL")
public class MemberWithdrawalJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private MemberWithdrawalReason reason;

    @Column(name = "reason_detail", length = 500)
    private String reasonDetail;

    protected MemberWithdrawalJpaEntity() {
    }

    private MemberWithdrawalJpaEntity(Long memberId, MemberWithdrawalReason reason, String reasonDetail) {
        this.memberId = memberId;
        this.reason = reason;
        this.reasonDetail = reasonDetail;
    }

    static MemberWithdrawalJpaEntity create(Long memberId, MemberWithdrawalReason reason, String reasonDetail) {
        return new MemberWithdrawalJpaEntity(memberId, reason, reasonDetail);
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public MemberWithdrawalReason getReason() {
        return this.reason;
    }

    public String getReasonDetail() {
        return this.reasonDetail;
    }
}

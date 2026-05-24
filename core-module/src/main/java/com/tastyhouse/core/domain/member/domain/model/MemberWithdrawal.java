package com.tastyhouse.core.domain.member.domain.model;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "MEMBER_WITHDRAWAL")
public class MemberWithdrawal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private WithdrawalReason reason;

    @Column(name = "reason_detail", length = 500)
    private String reasonDetail;

    private MemberWithdrawal(Long memberId, WithdrawalReason reason, String reasonDetail) {
        this.memberId = memberId;
        this.reason = reason;
        this.reasonDetail = reasonDetail;
    }

    public static MemberWithdrawal of(Long memberId, WithdrawalReason reason, String reasonDetail) {
        return new MemberWithdrawal(memberId, reason, reasonDetail);
    }
}

package com.tastyhouse.core.entity.user;

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
    private Long id; // PK

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 탈퇴한 회원 ID (MEMBER.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private WithdrawalReason reason; // 탈퇴 사유 코드 (예: DISSATISFIED, PRIVACY, RARELY_USED, OTHER)

    @Column(name = "reason_detail", length = 500)
    private String reasonDetail; // 탈퇴 사유 상세 내용 (직접 입력)

    private MemberWithdrawal(
        Long memberId,
        WithdrawalReason reason,
        String reasonDetail
    ) {
        this.memberId = memberId;
        this.reason = reason;
        this.reasonDetail = reasonDetail;
    }

    public static MemberWithdrawal of(
        Long memberId,
        WithdrawalReason reason,
        String reasonDetail
    ) {
        return new MemberWithdrawal(
            memberId,
            reason,
            reasonDetail
        );
    }
}

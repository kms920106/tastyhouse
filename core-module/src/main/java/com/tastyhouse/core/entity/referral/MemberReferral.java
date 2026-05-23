package com.tastyhouse.core.entity.referral;

import com.tastyhouse.core.entity.BaseEntity;
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

/**
 * 추천 이력 테이블
 * - referrerId: 추천한 회원 (추천인)
 * - refereeId:  추천받은 회원 (피추천인, 신규 가입자)
 */
@Getter
@Entity
@Table(
    name = "MEMBER_REFERRAL",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_member_referral_referee_id",
        columnNames = {"referee_id"}  // 한 회원은 한 번만 추천받을 수 있음
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
    private Long id; // PK

    @Column(name = "referrer_id", nullable = false)
    private Long referrerId; // 추천인 회원 ID (MEMBER.id 참조)

    @Column(name = "referee_id", nullable = false)
    private Long refereeId; // 피추천인 회원 ID (MEMBER.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ReferralStatus status = ReferralStatus.PENDING; // 추천 처리 상태 (예: PENDING, COMPLETED, CANCELLED)

    private MemberReferral(
        Long referrerId,
        Long refereeId
    ) {
        this.referrerId = referrerId;
        this.refereeId = refereeId;
        this.status = ReferralStatus.PENDING;
    }

    public static MemberReferral of(
        Long referrerId,
        Long refereeId
    ) {
        return new MemberReferral(
            referrerId,
            refereeId
        );
    }
}

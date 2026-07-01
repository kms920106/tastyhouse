package com.tastyhouse.core.domain.referral.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.referral.domain.model.MemberReferral;
import com.tastyhouse.core.domain.referral.domain.model.ReferralStatus;

public record MemberReferralResult(
    Long id,
    Long referrerId,
    Long refereeId,
    ReferralStatus status,
    LocalDateTime createdAt
) {
    public static MemberReferralResult from(MemberReferral referral) {
        return new MemberReferralResult(
            referral.getId(),
            referral.getReferrerId(),
            referral.getRefereeId(),
            referral.getStatus(),
            referral.getCreatedAt()
        );
    }
}

package com.tastyhouse.application.member.referral.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.referral.model.MemberReferralStatus;

public record MemberReferralResult(
    Long id,
    Long referrerId,
    Long refereeId,
    MemberReferralStatus status,
    LocalDateTime createdAt
) {
}

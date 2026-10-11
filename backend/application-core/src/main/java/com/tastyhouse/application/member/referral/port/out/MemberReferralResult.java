package com.tastyhouse.application.member.referral.port.out;

import java.time.LocalDateTime;

public record MemberReferralResult(
    Long id,
    Long referrerId,
    Long refereeId,
    String status,
    LocalDateTime createdAt
) {
}

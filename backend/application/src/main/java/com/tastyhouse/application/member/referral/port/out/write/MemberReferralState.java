package com.tastyhouse.application.member.referral.port.out.write;

import java.time.LocalDateTime;

public record MemberReferralState(
    Long id,
    Long referrerId,
    Long refereeId,
    String status,
    LocalDateTime createdAt
) {
}

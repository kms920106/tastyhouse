package com.tastyhouse.core.domain.referral.application.dto.command;

public record RegisterReferralCommand(
    Long referrerId,
    Long refereeId
) {
}

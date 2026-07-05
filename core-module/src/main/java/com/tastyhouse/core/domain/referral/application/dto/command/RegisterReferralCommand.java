package com.tastyhouse.core.domain.referral.application.dto.command;

public record RegisterReferralCommand(
    Long referrerId,
    Long refereeId
) {

    public static RegisterReferralCommand of(Long referrerId, Long refereeId) {
        return new RegisterReferralCommand(referrerId, refereeId);
    }
}

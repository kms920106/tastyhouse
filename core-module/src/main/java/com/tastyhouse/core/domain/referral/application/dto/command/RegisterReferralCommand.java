package com.tastyhouse.core.domain.referral.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record RegisterReferralCommand(
    MemberId referrerId,
    MemberId refereeId
) {

    public static RegisterReferralCommand of(MemberId referrerId, MemberId refereeId) {
        return new RegisterReferralCommand(referrerId, refereeId);
    }
}

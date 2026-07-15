package com.tastyhouse.core.domain.member.referral.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.referral.domain.vo.ReferralId;

public record ReferralRegisteredEvent(
    ReferralId referralId,
    MemberId referrerId,
    MemberId refereeId,
    LocalDateTime registeredAt
) {
}

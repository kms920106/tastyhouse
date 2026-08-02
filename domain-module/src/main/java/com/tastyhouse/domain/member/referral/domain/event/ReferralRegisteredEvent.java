package com.tastyhouse.domain.member.referral.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.referral.domain.vo.ReferralId;

public record ReferralRegisteredEvent(
    ReferralId referralId,
    MemberId referrerId,
    MemberId refereeId,
    LocalDateTime registeredAt
) {
}

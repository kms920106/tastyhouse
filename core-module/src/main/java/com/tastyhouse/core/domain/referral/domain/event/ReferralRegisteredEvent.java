package com.tastyhouse.core.domain.referral.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.referral.domain.vo.ReferralId;

public record ReferralRegisteredEvent(
    ReferralId referralId,
    Long referrerId,
    Long refereeId,
    LocalDateTime registeredAt
) {
}

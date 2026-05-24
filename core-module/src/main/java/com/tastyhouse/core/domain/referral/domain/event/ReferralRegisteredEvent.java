package com.tastyhouse.core.domain.referral.domain.event;

import com.tastyhouse.core.domain.referral.domain.vo.ReferralId;

import java.time.LocalDateTime;

public record ReferralRegisteredEvent(
    ReferralId referralId,
    Long referrerId,
    Long refereeId,
    LocalDateTime registeredAt
) {
}

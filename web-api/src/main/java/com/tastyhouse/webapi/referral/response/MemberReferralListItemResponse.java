package com.tastyhouse.webapi.referral.response;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.referral.domain.model.ReferralStatus;
import com.tastyhouse.core.domain.referral.application.dto.result.MemberReferralResult;

public record MemberReferralListItemResponse(
    Long id,
    Long refereeId,
    ReferralStatus status,
    LocalDateTime createdAt
) {
    public static MemberReferralListItemResponse from(MemberReferralResult result) {
        return new MemberReferralListItemResponse(
            result.id(),
            result.refereeId(),
            result.status(),
            result.createdAt()
        );
    }
}

package com.tastyhouse.webapi.referral.response;

import com.tastyhouse.core.domain.referral.application.dto.result.MemberReferralResult;
import com.tastyhouse.core.domain.referral.domain.model.ReferralStatus;

import java.time.LocalDateTime;

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

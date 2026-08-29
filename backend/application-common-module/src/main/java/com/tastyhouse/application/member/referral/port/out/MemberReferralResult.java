package com.tastyhouse.application.member.referral.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.referral.model.MemberReferralStatus;

/**
 * 내 추천 목록 항목 read model(web-api 소비).
 */
public record MemberReferralResult(
    Long id,
    Long referrerId,
    Long refereeId,
    MemberReferralStatus status,
    LocalDateTime createdAt
) {
}

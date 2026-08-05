package com.tastyhouse.infrastructure.member.referral.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.member.referral.domain.model.MemberReferralStatus;

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

    @QueryProjection
    public MemberReferralResult {
    }
}

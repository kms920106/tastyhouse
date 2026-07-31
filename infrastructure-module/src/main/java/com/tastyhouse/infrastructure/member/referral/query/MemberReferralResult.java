package com.tastyhouse.infrastructure.member.referral.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.member.referral.domain.model.MemberReferralStatus;

/**
 * 내 추천 목록 항목 read model(web-api 소비).
 *
 * <p>{@code refereeId}는 소비 모듈이 응답에 피추천인 식별자로 내보내므로 도메인 VO {@code MemberId}로
 * 투영한다(JPA 엔티티 필드가 {@code @Convert}로 이미 VO 타입이다).
 */
public record MemberReferralResult(
    Long id,
    MemberId referrerId,
    MemberId refereeId,
    MemberReferralStatus status,
    LocalDateTime createdAt
) {

    @QueryProjection
    public MemberReferralResult {
    }
}

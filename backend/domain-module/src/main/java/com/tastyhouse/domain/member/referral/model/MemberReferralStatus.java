package com.tastyhouse.domain.member.referral.model;

public enum MemberReferralStatus {
    PENDING,    // 가입 완료, 보상 지급 대기
    REWARDED,   // 추천인/피추천인 보상 지급 완료
    CANCELLED   // 취소 (탈퇴 등으로 무효 처리)
}

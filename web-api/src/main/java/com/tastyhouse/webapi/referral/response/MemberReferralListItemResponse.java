package com.tastyhouse.webapi.referral.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.referral.domain.model.ReferralStatus;
import com.tastyhouse.core.domain.referral.application.dto.result.MemberReferralResult;

@Schema(description = "회원 추천(레퍼럴) 목록 아이템 응답")
public record MemberReferralListItemResponse(
    @Schema(description = "추천 ID", example = "1")
    Long id,

    @Schema(description = "피추천인(추천받은 회원) ID", example = "2")
    Long refereeId,

    @Schema(description = "추천 상태", example = "COMPLETED")
    ReferralStatus status,

    @Schema(description = "추천 생성 일시", example = "2026-01-01T00:00:00")
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

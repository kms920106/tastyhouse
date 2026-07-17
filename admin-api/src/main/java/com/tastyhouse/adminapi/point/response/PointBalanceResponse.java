package com.tastyhouse.adminapi.point.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.point.application.dto.result.MemberPointResult;

@Schema(description = "회원 포인트 잔액 응답")
public record PointBalanceResponse(
    @Schema(description = "회원 ID", example = "1")
    Long memberId,

    @Schema(description = "사용 가능 포인트", example = "1500")
    Integer availablePoints,

    @Schema(description = "이번 달 소멸 예정 포인트", example = "200")
    Integer expiredThisMonth
) {
    public static PointBalanceResponse from(MemberPointResult result) {
        return new PointBalanceResponse(
            result.memberId().value(),
            result.availablePoints(),
            result.expiredThisMonth()
        );
    }

    public static PointBalanceResponse zero(Long memberId) {
        return new PointBalanceResponse(memberId, 0, 0);
    }
}

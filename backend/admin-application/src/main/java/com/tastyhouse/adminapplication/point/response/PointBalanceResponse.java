package com.tastyhouse.adminapplication.point.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 포인트 잔액 응답")
public record PointBalanceResponse(
    @Schema(description = "회원 ID", example = "1")
    Long memberId,

    @Schema(description = "사용 가능 포인트", example = "1500")
    Integer availablePoints,

    @Schema(description = "이번 달 소멸 예정 포인트", example = "200")
    Integer expiredThisMonth
) {
    public static PointBalanceResponse from(
        Long memberId,
        Integer availablePoints,
        Integer expiredThisMonth
    ) {
        return new PointBalanceResponse(
            memberId,
            availablePoints,
            expiredThisMonth
        );
    }

    public static PointBalanceResponse zero(Long memberId) {
        return new PointBalanceResponse(
            memberId,
            0,
            0
        );
    }
}

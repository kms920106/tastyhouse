package com.tastyhouse.webapi.point.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.point.port.out.PointBalanceResult;

@Schema(description = "포인트 응답 DTO")
public record PointResponse(
    @Schema(description = "사용 가능 포인트", example = "1000")
    Integer availablePoints,

    @Schema(description = "이번달 소멸 예정 포인트", example = "0")
    Integer expiredThisMonth
) {
    public static PointResponse from(PointBalanceResult result) {
        return new PointResponse(
            result.availablePoints(),
            result.expiredThisMonth()
        );
    }
}

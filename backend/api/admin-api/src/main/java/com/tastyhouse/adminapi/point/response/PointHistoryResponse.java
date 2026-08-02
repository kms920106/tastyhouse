package com.tastyhouse.adminapi.point.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 이력 항목 응답")
public record PointHistoryResponse(
    @Schema(description = "포인트 유형", example = "EARNED", allowableValues = {"EARNED", "USE", "REFUND"})
    String pointType,

    @Schema(description = "포인트 증감량 (적립/환불은 양수, 사용은 음수)", example = "500")
    Integer pointAmount,

    @Schema(description = "사유", example = "리뷰 작성 적립")
    String reason,

    @Schema(description = "발생 일시", example = "2026-07-10T14:30:00")
    LocalDateTime createdAt
) {
    public static PointHistoryResponse from(
        String pointType,
        Integer pointAmount,
        String reason,
        LocalDateTime createdAt
    ) {
        return new PointHistoryResponse(
            pointType,
            pointAmount,
            reason,
            createdAt
        );
    }
}

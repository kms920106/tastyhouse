package com.tastyhouse.webapplication.point.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 내역 항목")
public record PointHistoryItemResponse(
    @Schema(description = "포인트 사용/적립 사유", example = "포토 리뷰 적립금")
    String reason,

    @Schema(description = "내역 일자", example = "2020-10-05")
    LocalDate date,

    @Schema(description = "포인트 변동량 (적립 시 양수, 사용 시 음수)", example = "1000")
    Integer pointAmount,

    @Schema(description = "포인트 유형 (EARNED: 적립, USE: 사용, REFUND: 환불)", example = "EARNED")
    String pointType
) {
    public static PointHistoryItemResponse from(
        String reason,
        LocalDate date,
        Integer pointAmount,
        String pointType
    ) {
        return new PointHistoryItemResponse(
            reason,
            date,
            pointAmount,
            pointType
        );
    }
}

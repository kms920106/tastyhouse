package com.tastyhouse.webapi.point.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.point.port.out.PointHistoryViewResult;

@Schema(description = "포인트 내역 응답 DTO")
public record PointHistoryResponse(
    @Schema(description = "사용 가능 포인트", example = "1000")
    Integer availablePoints,

    @Schema(description = "이번달 소멸 예정 포인트", example = "0")
    Integer expiredThisMonth,

    @Schema(description = "포인트 내역 목록")
    List<PointHistoryItemResponse> histories
) {
    public static PointHistoryResponse from(PointHistoryViewResult result) {
        return new PointHistoryResponse(
            result.availablePoints(),
            result.expiredThisMonth(),
            result.histories().stream().map(PointHistoryItemResponse::from).toList()
        );
    }
}

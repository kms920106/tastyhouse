package com.tastyhouse.webapi.member.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "포인트 내역 응답 DTO")
public record PointHistoryResponse(
    @Schema(description = "사용 가능 포인트", example = "1000")
    Integer availablePoints,

    @Schema(description = "이번달 소멸 예정 포인트", example = "0")
    Integer expiredThisMonth,

    @Schema(description = "포인트 내역 목록")
    List<PointHistoryItemResponse> histories
) {
    public static PointHistoryResponse from(
    Integer availablePoints,
    Integer expiredThisMonth,
    List<PointHistoryItemResponse> histories
    ) {
    return new PointHistoryResponse(
        availablePoints,
        expiredThisMonth,
        histories
    );
    }
}

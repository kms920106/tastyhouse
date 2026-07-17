package com.tastyhouse.webapi.member.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 내역 응답 DTO")
public record MemberPointHistoryResponse(
    @Schema(description = "사용 가능 포인트", example = "1000")
    Integer availablePoints,

    @Schema(description = "이번달 소멸 예정 포인트", example = "0")
    Integer expiredThisMonth,

    @Schema(description = "포인트 내역 목록")
    List<MemberPointHistoryItemResponse> histories
) {
    public static MemberPointHistoryResponse from(
        Integer availablePoints,
        Integer expiredThisMonth,
        List<MemberPointHistoryItemResponse> histories
    ) {
        return new MemberPointHistoryResponse(
            availablePoints,
            expiredThisMonth,
            histories
        );
    }
}

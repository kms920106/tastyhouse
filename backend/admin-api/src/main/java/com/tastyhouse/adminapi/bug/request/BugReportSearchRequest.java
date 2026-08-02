package com.tastyhouse.adminapi.bug.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "버그 제보 검색 요청")
public record BugReportSearchRequest(
    @Schema(description = "제목 (부분 일치 검색)", example = "결제 화면 오류")
    String title,

    @Schema(description = "내용 (부분 일치 검색)", example = "앱이 강제 종료됩니다")
    String content,

    @Schema(description = "제보 회원 ID", example = "1")
    Long memberId,

    @Schema(description = "처리 상태", example = "RECEIVED",
        allowableValues = {"RECEIVED", "IN_PROGRESS", "RESOLVED", "REJECTED", "ON_HOLD"})
    String status,

    @Schema(description = "분류", example = "PAYMENT",
        allowableValues = {"PAYMENT", "LOGIN", "ORDER", "RESERVATION", "UI", "PERFORMANCE", "ETC"})
    String category,

    @Schema(description = "우선순위", example = "HIGH",
        allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    String priority
) {
}

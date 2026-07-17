package com.tastyhouse.adminapi.bug.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.bug.application.dto.result.BugReportListItemResult;

@Schema(description = "버그 제보 목록 항목 응답")
public record BugReportListItemResponse(
    @Schema(description = "버그 제보 ID", example = "1")
    Long id,

    @Schema(description = "제보 회원 정보")
    MemberSummaryResponse member,

    @Schema(description = "신고 기기 정보", example = "iPhone 15 Pro, iOS 17.4")
    String device,

    @Schema(description = "제목", example = "결제 화면 진입 시 앱이 종료됩니다")
    String title,

    @Schema(description = "처리 상태", example = "RECEIVED")
    String status,

    @Schema(description = "분류 (미분류 시 null)", example = "PAYMENT")
    String category,

    @Schema(description = "우선순위 (미지정 시 null)", example = "HIGH")
    String priority,

    @Schema(description = "첨부 이미지 개수", example = "2")
    long imageCount,

    @Schema(description = "제보 등록 일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {

    public static BugReportListItemResponse from(BugReportListItemResult dto, MemberSummaryResponse member) {
        return new BugReportListItemResponse(
            dto.id(),
            member,
            dto.device(),
            dto.title(),
            dto.status() != null ? dto.status().name() : null,
            dto.category() != null ? dto.category().name() : null,
            dto.priority() != null ? dto.priority().name() : null,
            dto.imageCount(),
            dto.createdAt()
        );
    }
}

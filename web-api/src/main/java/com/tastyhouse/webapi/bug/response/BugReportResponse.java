package com.tastyhouse.webapi.bug.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.bug.application.dto.result.BugReportResult;

@Schema(description = "버그 신고 응답")
public record BugReportResponse(
    @Schema(description = "버그 신고 ID", example = "1")
    Long id,

    @Schema(description = "신고 기기 정보", example = "iPhone 15 Pro, iOS 17.4")
    String device,

    @Schema(description = "제목", example = "결제 화면 진입 시 앱이 종료됩니다")
    String title,

    @Schema(description = "내용", example = "결제하기 버튼을 누르면 앱이 강제 종료됩니다.")
    String content,

    @Schema(description = "첨부 파일 ID 목록")
    List<Long> uploadedFileIds,

    @Schema(description = "신고 등록 일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {
    public static BugReportResponse from(BugReportResult result) {
        return new BugReportResponse(
            result.id().value(),
            result.device(),
            result.title(),
            result.content(),
            result.uploadedFileIds(),
            result.createdAt()
        );
    }
}

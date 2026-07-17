package com.tastyhouse.webapi.bug.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

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

    @Schema(description = "앱 버전", example = "3.2.0")
    String appVersion,

    @Schema(description = "플랫폼 (IOS/ANDROID)", example = "IOS")
    String platform,

    @Schema(description = "OS 버전", example = "17.4")
    String osVersion,

    @Schema(description = "처리 상태", example = "RECEIVED")
    String status,

    @Schema(description = "첨부 파일 ID 목록")
    List<Long> uploadedFileIds,

    @Schema(description = "신고 등록 일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {
    public static BugReportResponse from(
        Long id,
        String device,
        String title,
        String content,
        String appVersion,
        String platform,
        String osVersion,
        String status,
        List<Long> uploadedFileIds,
        LocalDateTime createdAt
    ) {
        return new BugReportResponse(
            id,
            device,
            title,
            content,
            appVersion,
            platform,
            osVersion,
            status,
            uploadedFileIds,
            createdAt
        );
    }
}

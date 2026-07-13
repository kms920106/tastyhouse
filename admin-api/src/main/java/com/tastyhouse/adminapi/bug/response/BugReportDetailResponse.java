package com.tastyhouse.adminapi.bug.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.bug.application.dto.BugReportDetailDto;
import com.tastyhouse.adminapi.common.FileResponse;

@Schema(description = "버그 제보 상세 응답")
public record BugReportDetailResponse(
    @Schema(description = "버그 제보 ID", example = "1")
    Long id,

    @Schema(description = "제보 회원 정보")
    MemberSummaryResponse member,

    @Schema(description = "신고 기기 정보", example = "iPhone 15 Pro, iOS 17.4")
    String device,

    @Schema(description = "제목", example = "결제 화면 진입 시 앱이 종료됩니다")
    String title,

    @Schema(description = "내용", example = "결제하기 버튼을 누르면 앱이 강제 종료됩니다.")
    String content,

    @Schema(description = "처리 상태", example = "RECEIVED")
    String status,

    @Schema(description = "분류 (미분류 시 null)", example = "PAYMENT")
    String category,

    @Schema(description = "우선순위 (미지정 시 null)", example = "HIGH")
    String priority,

    @Schema(description = "담당 관리자 ID (미배정 시 null)", example = "1")
    Long assigneeAdminId,

    @Schema(description = "처리 결과/반려 사유 (미처리 시 null)", example = "3.2.1 버전에서 수정 완료했습니다.")
    String adminAnswer,

    @Schema(description = "처리 완료 일시 (미종결 시 null)", example = "2026-01-02T10:00:00")
    LocalDateTime resolvedAt,

    @Schema(description = "앱 버전", example = "3.2.0")
    String appVersion,

    @Schema(description = "플랫폼 (IOS/ANDROID)", example = "IOS")
    String platform,

    @Schema(description = "OS 버전", example = "17.4")
    String osVersion,

    @Schema(description = "첨부 이미지 목록")
    List<FileResponse> images,

    @Schema(description = "제보 등록 일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정 일시", example = "2026-01-01T00:00:00")
    LocalDateTime updatedAt
) {

    public static BugReportDetailResponse from(BugReportDetailDto dto, MemberSummaryResponse member, List<FileResponse> images) {
        return new BugReportDetailResponse(
            dto.id().value(),
            member,
            dto.device(),
            dto.title(),
            dto.content(),
            dto.status() != null ? dto.status().name() : null,
            dto.category() != null ? dto.category().name() : null,
            dto.priority() != null ? dto.priority().name() : null,
            dto.assigneeAdminId(),
            dto.adminAnswer(),
            dto.resolvedAt(),
            dto.appVersion(),
            dto.platform() != null ? dto.platform().name() : null,
            dto.osVersion(),
            images,
            dto.createdAt(),
            dto.updatedAt()
        );
    }
}

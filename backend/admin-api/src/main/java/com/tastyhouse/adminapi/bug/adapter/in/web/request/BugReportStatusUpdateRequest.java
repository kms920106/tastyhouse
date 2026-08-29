package com.tastyhouse.adminapi.bug.adapter.in.web.request;

import com.tastyhouse.adminapi.bug.application.port.in.BugReportStatusChangeCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "버그 제보 처리 상태 변경 요청")
public record BugReportStatusUpdateRequest(
    @NotBlank(message = "처리 상태는 필수입니다.")
    @Schema(description = "변경할 처리 상태 (RECEIVED로의 되돌림은 불가)", example = "IN_PROGRESS",
        allowableValues = {"IN_PROGRESS", "RESOLVED", "REJECTED", "ON_HOLD"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String status,

    @Schema(description = "처리 결과/반려 사유 (RESOLVED·REJECTED 시 기록)", example = "3.2.1 버전에서 수정 완료했습니다.")
    String answer
) {

    public BugReportStatusChangeCommand toCommand(Long bugReportId) {
        return new BugReportStatusChangeCommand(bugReportId, status(), answer());
    }
}

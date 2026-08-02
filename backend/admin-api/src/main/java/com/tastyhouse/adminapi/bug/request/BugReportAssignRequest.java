package com.tastyhouse.adminapi.bug.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "버그 제보 담당자 배정 요청")
public record BugReportAssignRequest(
    @NotNull(message = "담당 관리자 ID는 필수입니다.")
    @Schema(description = "담당 관리자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long assigneeAdminId
) {
}

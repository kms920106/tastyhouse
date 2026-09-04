package com.tastyhouse.adminapi.bug.adapter.in.web.request;

import com.tastyhouse.application.bug.port.in.BugReportClassifyCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "버그 제보 분류/우선순위 지정 요청")
public record BugReportClassifyRequest(
    @NotBlank(message = "분류는 필수입니다.")
    @Schema(description = "분류", example = "PAYMENT",
        allowableValues = {"PAYMENT", "LOGIN", "ORDER", "RESERVATION", "UI", "PERFORMANCE", "ETC"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String category,

    @NotBlank(message = "우선순위는 필수입니다.")
    @Schema(description = "우선순위", example = "HIGH",
        allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String priority
) {

    public BugReportClassifyCommand toCommand(Long bugReportId) {
        return new BugReportClassifyCommand(bugReportId, category(), priority());
    }
}

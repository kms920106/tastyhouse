package com.tastyhouse.adminapi.policy.adapter.in.web.request;

import java.time.LocalDateTime;

import com.tastyhouse.adminapi.policy.application.port.in.PolicyCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "약관 생성 요청")
public record PolicyCreateRequest(
    @NotBlank(message = "정책 타입은 필수입니다.")
    @Schema(description = "정책 타입", example = "TERMS_OF_SERVICE", allowableValues = {"TERMS_OF_SERVICE", "PRIVACY_POLICY", "ELECTRONIC_FINANCIAL_TRANSACTIONS", "AGE_VERIFICATION"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String type,

    @NotBlank(message = "버전은 필수입니다.")
    @Schema(description = "버전", example = "1.0.0", requiredMode = Schema.RequiredMode.REQUIRED)
    String version,

    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "제목", example = "이용약관", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "약관 본문 내용", example = "제1조 (목적) ...", requiredMode = Schema.RequiredMode.REQUIRED)
    String content,

    @NotNull(message = "필수 동의 여부는 필수입니다.")
    @Schema(description = "필수 동의 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean mandatory,

    @NotNull(message = "시행일은 필수입니다.")
    @Schema(description = "시행일", example = "2026-01-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime effectiveDate,

    @Schema(description = "작성자", example = "admin")
    String createdBy
) {

    public PolicyCreateCommand toCommand() {
        return new PolicyCreateCommand(type, version, title, content, mandatory, effectiveDate, createdBy);
    }
}

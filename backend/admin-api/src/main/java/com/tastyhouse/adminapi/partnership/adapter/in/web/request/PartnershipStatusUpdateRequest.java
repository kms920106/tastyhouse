package com.tastyhouse.adminapi.partnership.adapter.in.web.request;

import com.tastyhouse.adminapplication.partnership.port.in.PartnershipStatusChangeCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "제휴 신청 처리 상태 변경 요청")
public record PartnershipStatusUpdateRequest(
    @NotBlank(message = "상태는 필수입니다.")
    @Schema(description = "변경할 처리 상태", example = "IN_PROGRESS", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String status
) {

    public PartnershipStatusChangeCommand toCommand(Long partnershipRequestId) {
        return new PartnershipStatusChangeCommand(partnershipRequestId, status());
    }
}

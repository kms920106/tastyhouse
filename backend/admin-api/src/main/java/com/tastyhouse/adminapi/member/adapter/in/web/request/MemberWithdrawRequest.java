package com.tastyhouse.adminapi.member.adapter.in.web.request;

import com.tastyhouse.adminapplication.member.port.in.MemberManagementWithdrawCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 강제 탈퇴 요청")
public record MemberWithdrawRequest(
    @NotBlank(message = "탈퇴 사유는 필수입니다.")
    @Schema(description = "탈퇴 사유", example = "OTHER",
        allowableValues = {"LOW_USAGE_FREQUENCY", "INSUFFICIENT_CONTENT", "SWITCH_TO_ANOTHER_SERVICE", "PRIVACY_CONCERNS", "OTHER"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String reason,

    @Schema(description = "탈퇴 사유 상세(관리자 메모)", example = "약관 위반으로 인한 관리자 강제 탈퇴")
    String reasonDetail
) {

    public MemberManagementWithdrawCommand toCommand(Long memberId) {
        return new MemberManagementWithdrawCommand(memberId, reason(), reasonDetail());
    }
}

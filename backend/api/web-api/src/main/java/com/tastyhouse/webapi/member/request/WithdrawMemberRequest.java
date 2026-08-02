package com.tastyhouse.webapi.member.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 탈퇴 요청")
public record WithdrawMemberRequest(
    @NotBlank(message = "탈퇴 사유를 선택해주세요.")
    @Schema(description = "탈퇴 사유", example = "LOW_USAGE_FREQUENCY", allowableValues = {"LOW_USAGE_FREQUENCY", "INSUFFICIENT_CONTENT", "SWITCH_TO_ANOTHER_SERVICE", "PRIVACY_CONCERNS", "OTHER"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String reason,

    @Size(max = 500, message = "상세 사유는 최대 500자까지 입력 가능합니다.")
    @Schema(description = "탈퇴 상세 사유 (선택)", example = "서비스를 자주 이용하지 않게 되었습니다.")
    String reasonDetail
) {
}

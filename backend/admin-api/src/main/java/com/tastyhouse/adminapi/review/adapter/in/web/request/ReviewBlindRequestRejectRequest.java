package com.tastyhouse.adminapi.review.adapter.in.web.request;

import com.tastyhouse.adminapi.review.application.port.in.ReviewBlindRequestRejectCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 게시중단 요청 반려 요청")
public record ReviewBlindRequestRejectRequest(
    @NotBlank(message = "반려 사유는 필수입니다.")
    @Size(max = 500, message = "반려 사유는 500자를 초과할 수 없습니다.")
    @Schema(description = "반려 사유", example = "게시 기준 위반 사실이 확인되지 않습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    String rejectReason
) {

    public ReviewBlindRequestRejectCommand toCommand(Long requestId) {
        return new ReviewBlindRequestRejectCommand(requestId, rejectReason);
    }
}

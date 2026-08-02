package com.tastyhouse.adminapi.coupon.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "쿠폰 회원 발급 요청")
public record CouponIssueRequest(
    @NotNull(message = "회원 ID는 필수입니다.")
    @Positive(message = "회원 ID는 양수여야 합니다.")
    @Schema(description = "발급 대상 회원 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long memberId
) {
}

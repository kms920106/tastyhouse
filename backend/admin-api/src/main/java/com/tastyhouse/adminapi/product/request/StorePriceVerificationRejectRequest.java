package com.tastyhouse.adminapi.product.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 매장 가격 인증 요청 반려 요청.
 *
 * <p>사유가 필수인 이유는 점주가 무엇을 고쳐 다시 요청해야 하는지 알아야 하기 때문이다(가격표 이미지가
 * 흐리다·신고 매장가가 가격표와 다르다 등).
 *
 * <p>{@code max = 500}은 {@code SHOP_STORE_PRICE_VERIFICATION.reject_reason} 컬럼 길이와 맞춘 값이다 —
 * 여기서 막지 않으면 DB 단계에서 잘리거나 실패한다.
 */
@Schema(description = "매장 가격 인증 요청 반려 요청")
public record StorePriceVerificationRejectRequest(
    @NotBlank(message = "반려 사유는 필수입니다.")
    @Size(max = 500, message = "반려 사유는 500자 이하여야 합니다.")
    @Schema(description = "반려 사유", example = "가격표 이미지의 금액이 신고된 매장가와 다릅니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String rejectReason
) {
}

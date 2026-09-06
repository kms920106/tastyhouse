package com.tastyhouse.webapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.product.port.in.ProductFeedbackCreateCommand;

@Schema(description = "메뉴 정보 고객 의견 제보 요청")
public record ProductFeedbackCreateRequest(

    @NotBlank(message = "의견 유형은 필수입니다.")
    @Schema(description = "의견 유형", example = "PRICE",
        allowableValues = {"PRICE", "IMAGE", "COMPOSITION", "SOLD_OUT", "ETC"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String feedbackType,

    @Size(max = 500, message = "의견은 500자 이내로 입력해 주세요.")
    @Schema(description = "의견 내용. 유형이 ETC이면 필수입니다", example = "메뉴 사진이 실제와 많이 달라요.")
    String content
) {
    public ProductFeedbackCreateCommand toCommand(Long memberId, Long productId) {
        return new ProductFeedbackCreateCommand(
            memberId,
            productId,
            feedbackType,
            content
        );
    }
}

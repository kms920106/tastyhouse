package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.shop.port.in.ShopRequestCommentOwnerCreateCommand;

/**
 * 요청건 문의 작성 요청(점주).
 */
@Schema(description = "요청건 문의 작성 요청")
public record ShopRequestCommentCreateRequest(

    @NotBlank(message = "문의 내용은 필수입니다.")
    @Size(max = 1000, message = "문의 내용은 1000자 이하여야 합니다.")
    @Schema(
        description = "문의 내용",
        example = "반려 사유를 좀 더 자세히 알려주실 수 있나요?",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String content
) {

    public ShopRequestCommentOwnerCreateCommand toCommand(Long ceoId, Long shopId, Long requestId) {
        return new ShopRequestCommentOwnerCreateCommand(ceoId, shopId, requestId, content());
    }
}

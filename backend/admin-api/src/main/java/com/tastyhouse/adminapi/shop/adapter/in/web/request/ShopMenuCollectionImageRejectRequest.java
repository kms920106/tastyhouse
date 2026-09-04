package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageRejectCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴모음컷 반려 요청.
 *
 * <p>사유가 필수인 이유는 점주가 무엇을 고쳐 다시 올려야 하는지 알아야 하기 때문이다. 메뉴모음컷은
 * 최소 1개 유지 제약이 있어, 반려만 하고 이유를 알려주지 않으면 점주가 교체 자체를 못 한다.
 */
@Schema(description = "메뉴모음컷 반려 요청")
public record ShopMenuCollectionImageRejectRequest(
    @NotBlank(message = "반려 사유는 필수입니다.")
    @Size(max = 500, message = "반려 사유는 500자 이하여야 합니다.")
    @Schema(description = "반려 사유", example = "메뉴가 잘 보이지 않습니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String rejectReason
) {

    public ShopMenuCollectionImageRejectCommand toCommand(Long imageId) {
        return new ShopMenuCollectionImageRejectCommand(imageId, rejectReason);
    }
}

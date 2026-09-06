package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductOptionGroupLinkCommand;
import com.tastyhouse.application.product.port.in.ProductOptionGroupUnlinkCommand;

@Schema(description = "메뉴-옵션그룹 연결·해제 요청")
public record ProductOptionGroupLinkRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId
) {
    public ProductOptionGroupLinkCommand toCommand(Long ceoId, Long productId, Long optionGroupId) {
        return new ProductOptionGroupLinkCommand(ceoId, shopId, productId, optionGroupId);
    }

    public ProductOptionGroupUnlinkCommand toUnlinkCommand(Long ceoId, Long productId, Long optionGroupId) {
        return new ProductOptionGroupUnlinkCommand(ceoId, shopId, productId, optionGroupId);
    }
}

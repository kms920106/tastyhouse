package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductShopLinkCreateCommand;

@Schema(description = "메뉴 불러오기 요청")
public record ProductShopLinkCreateRequest(

    @NotNull(message = "메뉴가 노출될 메뉴그룹을 선택해 주세요.")
    @Schema(description = "대상 가게에서 메뉴가 노출될 메뉴그룹 ID", example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED)
    Long productCategoryId
) {
    public ProductShopLinkCreateCommand toCommand(Long ceoId, Long productId, Long targetShopId) {
        return new ProductShopLinkCreateCommand(ceoId, productId, targetShopId, productCategoryId);
    }
}

package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductShopLinkItemCommand;

@Schema(description = "메뉴-가게 연결 항목")
public record ProductShopLinkItemRequest(

    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "연결할 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotNull(message = "메뉴가 노출될 메뉴그룹을 선택해 주세요.")
    @Schema(description = "이 가게에서 메뉴가 노출될 메뉴그룹 ID", example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED)
    Long productCategoryId
) {
    public ProductShopLinkItemCommand toCommand() {
        return new ProductShopLinkItemCommand(shopId, productCategoryId);
    }
}

package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapplication.product.port.in.ProductShopLinkCreateCommand;

/**
 * 가게 메뉴판으로 메뉴를 불러올 때의 요청 — 그 가게에서 노출될 메뉴그룹을 지정한다.
 *
 * <p>대상 가게는 경로({@code /shops/{targetShopId}})로 받으므로 본문에 담지 않는다.
 */
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

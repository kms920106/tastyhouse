package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopStorePriceVerificationViewResult;

@Schema(description = "매장가격 미인증 메뉴")
public record ShopStorePriceUnverifiedItemResponse(
    @Schema(description = "메뉴 ID", example = "1")
    Long productId,

    @Schema(description = "메뉴명", example = "후라이드 치킨")
    String productName,

    @Schema(description = "미인증 사유 코드", example = "STORE_PRICE_NOT_REGISTERED",
        allowableValues = {"DELIVERY_PRICE_HIGHER_THAN_STORE", "STORE_PRICE_NOT_REGISTERED"})
    String reason
) {
    public static ShopStorePriceUnverifiedItemResponse from(
        ShopStorePriceVerificationViewResult.UnverifiedItem item
    ) {
        return new ShopStorePriceUnverifiedItemResponse(
            item.productId(),
            item.productName(),
            item.reason().name()
        );
    }
}

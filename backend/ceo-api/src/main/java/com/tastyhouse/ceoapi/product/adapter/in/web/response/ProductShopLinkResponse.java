package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductShopLinkResult;

@Schema(description = "메뉴-가게 연결 상태")
public record ProductShopLinkResponse(

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "가게명", example = "맛있는집 강남점")
    String shopName,

    @Schema(description = "이 가게에서의 메뉴그룹 ID. 연결되지 않은 가게면 null입니다", example = "10")
    Long productCategoryId,

    @Schema(description = "이 가게에서의 메뉴그룹명. 연결되지 않은 가게면 null입니다", example = "치킨")
    String productCategoryName,

    @Schema(description = "이 메뉴가 해당 가게에 연결되어 있는지", example = "true")
    boolean linked
) {
    public static ProductShopLinkResponse from(ProductShopLinkResult result) {
        return new ProductShopLinkResponse(
            result.shopId(),
            result.shopName(),
            result.productCategoryId(),
            result.productCategoryName(),
            result.linked()
        );
    }
}

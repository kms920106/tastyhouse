package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductOwnerPriceView;

@Schema(description = "메뉴 가격 행")
public record ProductPriceResponse(
    @Schema(description = "가격 행 ID", example = "10")
    Long id,

    @Schema(description = "가격명(가격 행이 1개면 null일 수 있습니다)", example = "대")
    String priceName,

    @Schema(description = "배달가격(원)", example = "15000")
    Integer deliveryPrice,

    @Schema(description = "매장가격(원). 미인증·미설정이면 null", example = "14000")
    Integer storePrice,

    @Schema(description = "픽업가격(원). 미인증·미설정이면 null", example = "14000")
    Integer pickupPrice,

    @Schema(description = "표시 순서(0부터)", example = "0")
    Integer sort
) {
    public static ProductPriceResponse from(ProductOwnerPriceView view) {
        return new ProductPriceResponse(
            view.id(),
            view.priceName(),
            view.deliveryPrice(),
            view.storePrice(),
            view.pickupPrice(),
            view.sort()
        );
    }
}

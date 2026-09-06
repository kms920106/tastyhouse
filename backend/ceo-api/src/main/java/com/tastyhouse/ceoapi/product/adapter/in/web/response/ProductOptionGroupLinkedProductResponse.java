package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;

@Schema(description = "옵션그룹을 사용하는 메뉴")
public record ProductOptionGroupLinkedProductResponse(
    @Schema(description = "메뉴 ID", example = "100")
    Long id,

    @Schema(description = "메뉴명", example = "매운 등갈비")
    String name
) {
    public static ProductOptionGroupLinkedProductResponse from(ProductOptionGroupLinkedProductResult result) {
        return new ProductOptionGroupLinkedProductResponse(
            result.id(),
            result.name()
        );
    }
}

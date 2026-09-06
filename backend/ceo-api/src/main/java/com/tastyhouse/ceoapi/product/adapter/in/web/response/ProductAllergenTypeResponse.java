package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductAllergenTypeView;

@Schema(description = "알레르기 유발성분 코드·라벨")
public record ProductAllergenTypeResponse(
    @Schema(description = "성분 코드", example = "MILK")
    String code,

    @Schema(description = "성분 한글 라벨", example = "우유")
    String label
) {
    public static ProductAllergenTypeResponse from(ProductAllergenTypeView view) {
        return new ProductAllergenTypeResponse(
            view.code(),
            view.label()
        );
    }
}

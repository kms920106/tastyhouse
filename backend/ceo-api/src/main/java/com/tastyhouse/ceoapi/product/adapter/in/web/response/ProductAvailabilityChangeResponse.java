package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;

@Schema(description = "품절·숨김 일괄 처리 결과")
public record ProductAvailabilityChangeResponse(
    @Schema(description = "적용된 대상 ID 목록")
    List<Long> succeededIds,

    @Schema(description = "적용하지 못한 대상과 그 사유")
    List<ProductAvailabilityFailureResponse> failed
) {
    public static ProductAvailabilityChangeResponse from(ProductAvailabilityChangeView view) {
        return new ProductAvailabilityChangeResponse(
            view.succeeded(),
            view.failed().stream()
                .map(ProductAvailabilityFailureResponse::from)
                .toList()
        );
    }
}

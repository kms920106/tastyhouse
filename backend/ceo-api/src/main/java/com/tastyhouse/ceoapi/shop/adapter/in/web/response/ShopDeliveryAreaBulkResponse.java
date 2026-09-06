package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBulkResult;

@Schema(description = "배달가능지역 일괄 처리 결과")
public record ShopDeliveryAreaBulkResponse(
    @Schema(description = "요청한 개수(중복 제거 후)", example = "20")
    int requestedCount,

    @Schema(description = "실제로 새로 등록된 개수", example = "15")
    int addedCount,

    @Schema(description = "이미 등록돼 있어 건너뛴 개수", example = "5")
    int skippedCount,

    @Schema(description = "반영 후 이 가게의 총 배달가능지역 개수", example = "42")
    int totalCount
) {
    public static ShopDeliveryAreaBulkResponse from(ShopDeliveryAreaBulkResult result) {
        return new ShopDeliveryAreaBulkResponse(
            result.requestedCount(),
            result.addedCount(),
            result.skippedCount(),
            result.totalCount()
        );
    }
}

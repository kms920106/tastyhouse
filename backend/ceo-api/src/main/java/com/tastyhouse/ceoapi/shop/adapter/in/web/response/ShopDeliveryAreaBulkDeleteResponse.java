package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBulkDeleteResult;

@Schema(description = "배달가능지역 일괄 삭제 결과")
public record ShopDeliveryAreaBulkDeleteResponse(
    @Schema(description = "삭제된 개수", example = "12")
    int removedCount,

    @Schema(description = "반영 후 이 가게의 총 배달가능지역 개수", example = "30")
    int totalCount
) {
    public static ShopDeliveryAreaBulkDeleteResponse from(ShopDeliveryAreaBulkDeleteResult result) {
        return new ShopDeliveryAreaBulkDeleteResponse(
            result.removedCount(),
            result.totalCount()
        );
    }
}

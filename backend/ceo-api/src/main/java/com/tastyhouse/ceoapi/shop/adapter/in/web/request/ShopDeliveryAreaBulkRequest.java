package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaBulkCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaBulkDeleteCommand;

@Schema(description = "배달가능지역 행정동 일괄 처리 요청")
public record ShopDeliveryAreaBulkRequest(
    @NotEmpty(message = "행정동 ID 목록은 비어 있을 수 없습니다.")
    @Size(max = 500, message = "한 번에 처리할 수 있는 행정동은 최대 500개입니다.")
    @Schema(description = "행정동 ID 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<@Positive(message = "행정동 ID는 양수여야 합니다.") Long> adminDongIds
) {
    public ShopDeliveryAreaBulkCreateCommand toCreateCommand(Long ceoId, Long shopId) {
        return new ShopDeliveryAreaBulkCreateCommand(ceoId, shopId, adminDongIds());
    }

    public ShopDeliveryAreaBulkDeleteCommand toDeleteCommand(Long ceoId, Long shopId) {
        return new ShopDeliveryAreaBulkDeleteCommand(ceoId, shopId, adminDongIds());
    }
}

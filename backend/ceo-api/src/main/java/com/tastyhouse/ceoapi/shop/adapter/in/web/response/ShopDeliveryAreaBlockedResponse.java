package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBlockedView;

@Schema(description = "닫을 수 없는 행정동 한 건")
public record ShopDeliveryAreaBlockedResponse(
    @Schema(description = "행정동 ID", example = "1101053")
    long adminDongId,

    @Schema(description = "행정동 전체 이름", example = "서울특별시 강남구 역삼1동")
    String regionName,

    @Schema(description = "닫을 수 없는 사유", example = "REGION_TIP", allowableValues = {"REGION_TIP"})
    String reason
) {
    public static ShopDeliveryAreaBlockedResponse from(ShopDeliveryAreaBlockedView blocked) {
        return new ShopDeliveryAreaBlockedResponse(
            blocked.adminDongId(),
            blocked.regionName(),
            blocked.reason()
        );
    }
}

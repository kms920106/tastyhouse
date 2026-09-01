package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaItemResult;

@Schema(description = "가게 배달가능지역 한 건")
public record ShopDeliveryAreaItemResponse(
    @Schema(description = "배달가능지역 ID", example = "10")
    long id,

    @Schema(description = "행정동 ID", example = "1101053")
    long adminDongId,

    @Schema(description = "행정동 전체 이름", example = "서울특별시 강남구 역삼1동")
    String regionName,

    @Schema(
        description = "등록 출처 (MANUAL: 행정동 직접 선택·반경 일괄, POLYGON: 지도 도형 환산)",
        example = "MANUAL",
        allowableValues = {"MANUAL", "POLYGON"}
    )
    String source
) {
    public static ShopDeliveryAreaItemResponse from(ShopDeliveryAreaItemResult result) {
        return new ShopDeliveryAreaItemResponse(
            result.id(),
            result.adminDongId(),
            result.regionName(),
            result.source()
        );
    }
}

package com.tastyhouse.webapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "지도 마커 조회 요청")
public record ShopMapMarkerSearchRequest(
    @NotNull(message = "위도는 필수입니다.")
    @Schema(description = "위도", example = "37.5013", requiredMode = Schema.RequiredMode.REQUIRED)
    Double latitude,

    @NotNull(message = "경도는 필수입니다.")
    @Schema(description = "경도", example = "127.0396", requiredMode = Schema.RequiredMode.REQUIRED)
    Double longitude
) {
}

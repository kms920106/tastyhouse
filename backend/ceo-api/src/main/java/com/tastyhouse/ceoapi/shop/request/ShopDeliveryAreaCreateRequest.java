package com.tastyhouse.ceoapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가게 배달가능지역 등록 요청")
public record ShopDeliveryAreaCreateRequest(
    @NotNull(message = "행정동 ID는 필수입니다.")
    @Schema(description = "행정동 ID", example = "1101053", requiredMode = Schema.RequiredMode.REQUIRED)
    Long adminDongId
) {
}

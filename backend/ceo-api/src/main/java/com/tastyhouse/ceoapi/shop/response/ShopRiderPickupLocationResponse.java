package com.tastyhouse.ceoapi.shop.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "라이더 픽업 위치")
public record ShopRiderPickupLocationResponse(
    @Schema(description = "픽업 도로명주소", example = "서울시 강남구 테헤란로 1")
    String roadAddress,

    @Schema(description = "픽업 지번주소", example = "서울시 강남구 역삼동 1-1")
    String lotAddress,

    @Schema(description = "픽업 상세주소", example = "지하 1층 후문")
    String detailAddress,

    @Schema(description = "픽업 위도", example = "37.497942")
    BigDecimal latitude,

    @Schema(description = "픽업 경도", example = "127.027621")
    BigDecimal longitude
) {

    public static ShopRiderPickupLocationResponse from(
        String roadAddress,
        String lotAddress,
        String detailAddress,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        return new ShopRiderPickupLocationResponse(
            roadAddress,
            lotAddress,
            detailAddress,
            latitude,
            longitude
        );
    }
}

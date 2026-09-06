package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;

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
    public static ShopRiderPickupLocationResponse from(ShopRiderGuideResult result) {
        String roadAddress = result.pickupRoadAddress();
        BigDecimal latitude = result.pickupLatitude();
        BigDecimal longitude = result.pickupLongitude();

        if (roadAddress == null || latitude == null || longitude == null) {
            return null;
        }

        return new ShopRiderPickupLocationResponse(
            roadAddress,
            result.pickupLotAddress(),
            result.pickupDetailAddress(),
            latitude,
            longitude
        );
    }
}

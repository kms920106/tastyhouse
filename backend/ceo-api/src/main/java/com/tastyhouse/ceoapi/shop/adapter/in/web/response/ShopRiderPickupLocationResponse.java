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

    /**
     * 픽업 위치가 미설정이면 {@code null}을 반환해, 프론트가 "가게 실주소로 폴백" 상태임을 한 필드로
     * 판정하게 한다(챕터 09에서 QueryService의 private 매퍼를 이 표현 계약으로 옮겼다 — 세 값이 모두
     * 있어야 위치로 성립한다는 것은 표현 규칙이다).
     */
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

package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 라이더 안내 통합 조회 응답. 화면이 한 탭에서 문구와 픽업 위치를 함께 표시하므로 왕복을 나누지 않는다.
 *
 * <p>가게 실주소·좌표를 함께 내려주는 것은 픽업 위치 미설정 시의 폴백 안내와 "실주소와 동일하게 설정"
 * 버튼의 참고값 용도다.
 */
@Schema(description = "라이더 가게방문 안내 조회 응답")
public record ShopRiderGuideResponse(
    @Schema(description = "라이더 가게방문 안내 문구 (미등록 시 null)",
        example = "대로변에서 분홍색 건물 1층 OO 안경 옆 가게입니다.")
    String visitGuide,

    @Schema(description = "라이더 픽업 위치 (미설정 시 null — 가게 실주소로 폴백)")
    ShopRiderPickupLocationResponse pickupLocation,

    @Schema(description = "가게 실주소(도로명)", example = "서울시 강남구 테헤란로 1")
    String shopRoadAddress,

    @Schema(description = "가게 실주소(지번)", example = "서울시 강남구 역삼동 1-1")
    String shopLotAddress,

    @Schema(description = "가게 실제 위도", example = "37.497942")
    BigDecimal shopLatitude,

    @Schema(description = "가게 실제 경도", example = "127.027621")
    BigDecimal shopLongitude,

    @Schema(description = "라이더 안내 정보(문구·픽업 위치) 최종 수정 일시 (한 번도 등록한 적 없으면 null)",
        example = "2026-08-08T21:02:00")
    LocalDateTime updatedAt
) {

    public static ShopRiderGuideResponse from(
        String visitGuide,
        ShopRiderPickupLocationResponse pickupLocation,
        String shopRoadAddress,
        String shopLotAddress,
        BigDecimal shopLatitude,
        BigDecimal shopLongitude,
        LocalDateTime updatedAt
    ) {
        return new ShopRiderGuideResponse(
            visitGuide,
            pickupLocation,
            shopRoadAddress,
            shopLotAddress,
            shopLatitude,
            shopLongitude,
            updatedAt
        );
    }
}

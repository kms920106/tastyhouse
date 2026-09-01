package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDetailViewResult;

@Schema(description = "내 가게 상세 응답")
public record ShopDetailResponse(
    @Schema(description = "가게 ID", example = "1")
    Long id,

    @Schema(description = "지하철역 ID", example = "1")
    Long stationId,

    @Schema(description = "상호명", example = "맛있는 분식")
    String name,

    @Schema(description = "위도", example = "37.497942")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.027621")
    BigDecimal longitude,

    @Schema(description = "평균 평점", example = "4.5")
    Double rating,

    @Schema(description = "도로명 주소", example = "서울시 강남구 테헤란로 1")
    String roadAddress,

    @Schema(description = "지번 주소", example = "서울시 강남구 역삼동 1")
    String lotAddress,

    @Schema(description = "대표 전화번호", example = "02-1234-5678")
    String phoneNumber,

    @Schema(description = "썸네일 이미지 URL(없으면 null)", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2025%2F02%2F16%2Fthumb.jpg?alt=media")
    String thumbnailImageUrl,

    @Schema(description = "상표 이미지 URL(없으면 null)", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2025%2F02%2F16%2Ftrademark.jpg?alt=media")
    String trademarkImageUrl,

    @Schema(description = "폐업 여부", example = "false")
    boolean permanentlyClosed,

    @Schema(description = "노출정지 여부", example = "false")
    boolean hidden,

    @Schema(description = "공휴일 휴무 여부", example = "false")
    boolean closedOnPublicHolidays,

    @Schema(description = "최소주문금액 (0: 미설정, 설정 시 5000~30000). 배달 주문에만 적용됩니다.", example = "10000")
    int minOrderAmount,

    @Schema(description = "예약주문 운영 여부 (true: 고객이 수령시간을 예약할 수 있음)", example = "true")
    boolean scheduledOrderEnabled,

    @Schema(description = "일회용컵 보증금제 대상 사업자 여부. true인 가게만 보증금 옵션그룹을 만들 수 "
        + "있습니다. 환경부 지정 사실이므로 점주는 변경할 수 없고 관리자만 토글합니다(읽기 전용).",
        example = "false")
    boolean cupDepositEnabled
) {
    public static ShopDetailResponse from(ShopDetailViewResult result) {
        return new ShopDetailResponse(
            result.id(),
            result.stationId(),
            result.name(),
            result.latitude(),
            result.longitude(),
            result.rating(),
            result.roadAddress(),
            result.lotAddress(),
            result.phoneNumber(),
            result.thumbnailImageUrl(),
            result.trademarkImageUrl(),
            result.permanentlyClosed(),
            result.hidden(),
            result.closedOnPublicHolidays(),
            result.minOrderAmount(),
            result.scheduledOrderEnabled(),
            result.cupDepositEnabled()
        );
    }
}

package com.tastyhouse.webapi.shop.response;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 상세 정보 응답")
public record ShopDetailResponse(
    @Schema(description = "가게 ID", example = "1")
    Long id,

    @Schema(description = "상호명", example = "리틀넥 청담")
    String name,

    @Schema(description = "위도", example = "37.5013")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.0396")
    BigDecimal longitude,

    @Schema(description = "총 평점", example = "4.8")
    Double rating,

    @Schema(description = "도로명 주소", example = "서울 강남구 도산대로51길 17")
    String roadAddress,

    @Schema(description = "지번 주소", example = "서울 강남구 신사동 653-7")
    String lotAddress,

    @Schema(description = "대표 전화번호", example = "02-1234-5678")
    String phoneNumber,

    @Schema(description = "전화번호 목록(대표·가상번호 포함)")
    List<ShopPhoneNumberItem> phoneNumbers,

    @Schema(description = "상표 이미지 URL", example = "https://cdn.tastyhouse.com/shop/1/trademark.jpg")
    String trademarkImageUrl,

    @Schema(description = "실시간 영업 상태(OPEN: 영업중, PREPARING: 준비중)", example = "OPEN")
    String operatingStatus,

    @Schema(description = "최소주문금액 (0: 미설정, 제한 없음). 배달 주문에만 적용됩니다.", example = "10000")
    int minOrderAmount,

    @Schema(description = "배달팁 최소 금액(원). 구간별·추가 배달팁을 합산한 하한. 0이면 배달팁 없음", example = "2000")
    int minDeliveryTip,

    @Schema(description = "배달팁 최대 금액(원). 고객 주소가 확정되기 전 상한", example = "4000")
    int maxDeliveryTip
) {
    public static ShopDetailResponse of(
        Long id,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        List<ShopPhoneNumberItem> phoneNumbers,
        String trademarkImageUrl,
        String operatingStatus,
        int minOrderAmount,
        int minDeliveryTip,
        int maxDeliveryTip
    ) {
        return new ShopDetailResponse(
            id,
            name,
            latitude,
            longitude,
            rating,
            roadAddress,
            lotAddress,
            phoneNumber,
            phoneNumbers,
            trademarkImageUrl,
            operatingStatus,
            minOrderAmount,
            minDeliveryTip,
            maxDeliveryTip
        );
    }
}

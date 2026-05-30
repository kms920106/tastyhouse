package com.tastyhouse.webapi.shop.response;

import com.tastyhouse.core.domain.shop.domain.model.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

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

    @Schema(description = "전화번호", example = "02-1234-5678")
    String phoneNumber
) {
    public static ShopDetailResponse from(Shop shop) {
        return new ShopDetailResponse(
            shop.getId(),
            shop.getName(),
            shop.getLatitude(),
            shop.getLongitude(),
            shop.getRating(),
            shop.getRoadAddress(),
            shop.getLotAddress(),
            shop.getPhoneNumber()
        );
    }
}

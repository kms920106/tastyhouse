package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaCandidateView;

@Schema(description = "미리보기 행정동 한 건")
public record ShopDeliveryAreaCandidateResponse(
    @Schema(description = "행정동 ID", example = "1101053")
    long adminDongId,

    @Schema(description = "행정동 전체 이름", example = "서울특별시 강남구 역삼1동")
    String regionName,

    @Schema(description = "대표점 위도", example = "37.500123")
    BigDecimal centerLatitude,

    @Schema(description = "대표점 경도", example = "127.036456")
    BigDecimal centerLongitude,

    @Schema(description = "이미 배달가능지역으로 등록돼 있는지", example = "false")
    boolean alreadyRegistered
) {
    public static ShopDeliveryAreaCandidateResponse from(ShopDeliveryAreaCandidateView candidate) {
        return new ShopDeliveryAreaCandidateResponse(
            candidate.adminDongId(),
            candidate.regionName(),
            candidate.centerLatitude(),
            candidate.centerLongitude(),
            candidate.alreadyRegistered()
        );
    }
}

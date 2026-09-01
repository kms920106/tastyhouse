package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaCandidateView;

/**
 * 미리보기에서 판정된 행정동 한 건.
 *
 * <p>{@code alreadyRegistered}가 있어 화면이 "이번에 새로 열리는 동"과 "이미 열려 있던 동"을 구분해
 * 표시할 수 있다 — 이 구분이 없으면 점주가 반경을 넓혀도 무엇이 늘었는지 알 수 없다.
 */
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

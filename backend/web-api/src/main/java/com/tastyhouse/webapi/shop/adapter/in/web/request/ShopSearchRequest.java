package com.tastyhouse.webapi.shop.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매장 검색 요청")
public record ShopSearchRequest(
    @Schema(description = "인근 지하철역 ID", example = "1")
    Long stationId,

    @Schema(description = "음식 종류 목록", allowableValues = {"KOREAN", "JAPANESE", "WESTERN", "CHINESE", "WORLD", "SNACK", "BAR", "CAFE"})
    List<String> foodTypes,

    @Schema(description = "편의시설 목록", allowableValues = {"PARKING", "RESTROOM", "RESERVATION", "BABY_CHAIR", "PET_FRIENDLY", "OUTLET", "TAKEOUT", "DELIVERY"})
    List<String> amenities
) {
}

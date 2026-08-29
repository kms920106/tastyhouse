package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 검색 요청")
public record ShopSearchRequest(
    @Schema(description = "상호명 (부분 일치 검색)", example = "맛있는")
    String name,

    @Schema(description = "지하철역 ID", example = "1")
    Long stationId,

    @Schema(description = "폐업 여부", example = "false")
    Boolean permanentlyClosed
) {
}

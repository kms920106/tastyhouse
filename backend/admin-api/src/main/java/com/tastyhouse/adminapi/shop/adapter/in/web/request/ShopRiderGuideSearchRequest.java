package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "라이더 안내 등록 가게 목록 검색 요청")
public record ShopRiderGuideSearchRequest(
    @Schema(description = "가게명(부분 일치, 미지정 시 전체)", example = "맛있는 분식")
    String shopName,

    @Schema(description = "안내 문구 등록 여부(true면 문구가 등록된 가게만, false면 문구 없이 픽업 위치만 설정된 가게만, 미지정 시 전체)",
        example = "true")
    Boolean hasVisitGuide
) {
}

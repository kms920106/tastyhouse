package com.tastyhouse.adminapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 콘텐츠보드 검수 목록 검색 요청")
public record ShopContentBoardSearchRequest(
    @Schema(description = "가게 ID(미지정 시 전체)", example = "1")
    Long shopId,

    @Schema(description = "숨김 여부(미지정 시 전체)", example = "false")
    Boolean hidden,

    @Schema(description = "콘텐츠 형태(미지정 시 전체)", example = "IMAGE", allowableValues = {"IMAGE", "GIF", "VIDEO"})
    String contentType
) {
}

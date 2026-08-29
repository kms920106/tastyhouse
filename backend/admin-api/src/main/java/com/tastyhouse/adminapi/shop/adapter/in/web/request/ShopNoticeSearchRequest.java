package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "점주 공지 검수 목록 검색 요청")
public record ShopNoticeSearchRequest(
    @Schema(description = "가게 ID(미지정 시 전체)", example = "3")
    Long shopId,

    @Schema(description = "가게명 부분 일치(미지정 시 전체)", example = "맛있는집")
    String shopName,

    @Schema(description = "게시중단 여부(미지정 시 전체)", example = "false")
    Boolean hidden
) {
}

package com.tastyhouse.webapi.faq.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FAQ 목록 조회 요청")
public record FaqSearchRequest(
    @Schema(description = "FAQ 카테고리 ID (미입력 시 전체 조회)", example = "1")
    Long categoryId
) {
}

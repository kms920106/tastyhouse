package com.tastyhouse.adminapi.faq.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FAQ 항목 목록 검색 조건")
public record FaqSearchRequest(
    @Schema(description = "카테고리 ID 필터 (미지정 시 전체)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Long categoryId,

    @Schema(description = "질문 부분 일치 검색어", example = "환불", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String question,

    @Schema(description = "노출 여부 (미지정 시 전체, true=노출, false=비노출)", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Boolean visible
) {
}

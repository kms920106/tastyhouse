package com.tastyhouse.webapi.search.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "메뉴 검색 요청")
public record SearchKeywordRequest(
    @NotBlank
    @Schema(description = "검색 키워드", example = "치킨", requiredMode = Schema.RequiredMode.REQUIRED)
    String query
) {
}

package com.tastyhouse.webapi.search.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 검색어 응답")
public record RecommendedKeywordResponse(
    @Schema(description = "추천 검색어", example = "마라탕")
    String keyword
) {

    public static RecommendedKeywordResponse of(String keyword) {
        return new RecommendedKeywordResponse(keyword);
    }
}

package com.tastyhouse.webapplication.search.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 검색어 응답")
public record SearchRecommendedKeywordResponse(
    @Schema(description = "추천 검색어", example = "마라탕")
    String keyword
) {

    public static SearchRecommendedKeywordResponse of(String keyword) {
        return new SearchRecommendedKeywordResponse(keyword);
    }
}

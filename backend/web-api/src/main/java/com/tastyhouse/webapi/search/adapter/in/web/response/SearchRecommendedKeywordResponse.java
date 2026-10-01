package com.tastyhouse.webapi.search.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.search.port.out.RecommendedKeywordResult;

@Schema(description = "추천 검색어 응답")
public record SearchRecommendedKeywordResponse(
    @Schema(description = "추천 검색어", example = "마라탕")
    String keyword
) {

    public static SearchRecommendedKeywordResponse from(RecommendedKeywordResult result) {
        return new SearchRecommendedKeywordResponse(result.keyword());
    }
}

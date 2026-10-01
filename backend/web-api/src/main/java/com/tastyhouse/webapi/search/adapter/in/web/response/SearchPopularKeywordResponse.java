package com.tastyhouse.webapi.search.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.search.port.out.PopularKeywordResult;

@Schema(description = "인기 검색어 응답")
public record SearchPopularKeywordResponse(
    @Schema(description = "인기 검색어 순위", example = "1")
    int rank,

    @Schema(description = "검색어", example = "마라탕")
    String keyword,

    @Schema(description = "신규 진입 검색어 여부", example = "true")
    boolean newKeyword
) {

    public static SearchPopularKeywordResponse from(PopularKeywordResult result) {
        return new SearchPopularKeywordResponse(
            result.rank(),
            result.keyword(),
            result.newKeyword()
        );
    }
}

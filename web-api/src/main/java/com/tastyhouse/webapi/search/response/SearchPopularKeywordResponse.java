package com.tastyhouse.webapi.search.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인기 검색어 응답")
public record SearchPopularKeywordResponse(
    @Schema(description = "인기 검색어 순위", example = "1")
    int rank,

    @Schema(description = "검색어", example = "마라탕")
    String keyword,

    @Schema(description = "신규 진입 검색어 여부", example = "true")
    boolean newKeyword
) {

    public static SearchPopularKeywordResponse of(
        int rank,
        String keyword,
        boolean newKeyword
    ) {
        return new SearchPopularKeywordResponse(rank, keyword, newKeyword);
    }
}

package com.tastyhouse.webapi.search.response;

public record RecommendedKeywordResponse(String keyword) {

    public static RecommendedKeywordResponse of(String keyword) {
        return new RecommendedKeywordResponse(keyword);
    }
}

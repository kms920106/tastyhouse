package com.tastyhouse.webapi.search.response;

public record PopularKeywordResponse(int rank, String keyword, boolean newKeyword) {

    public static PopularKeywordResponse of(int rank, String keyword, boolean newKeyword) {
        return new PopularKeywordResponse(rank, keyword, newKeyword);
    }
}

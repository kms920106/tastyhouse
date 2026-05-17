package com.tastyhouse.webapi.search.response;

public record PopularKeywordResponse(int rank, String keyword, boolean isNew) {

    public static PopularKeywordResponse of(int rank, String keyword, boolean isNew) {
        return new PopularKeywordResponse(rank, keyword, isNew);
    }
}

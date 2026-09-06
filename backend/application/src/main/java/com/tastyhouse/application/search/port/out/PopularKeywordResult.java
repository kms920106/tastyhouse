package com.tastyhouse.application.search.port.out;

public record PopularKeywordResult(
    int rank,
    String keyword,
    boolean newKeyword
) {
}

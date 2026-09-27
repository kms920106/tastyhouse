package com.tastyhouse.application.search.port.out.write;

public record PopularKeywordState(
    Long id,
    String keyword,
    int rank,
    boolean newKeyword,
    boolean visible
) {
}

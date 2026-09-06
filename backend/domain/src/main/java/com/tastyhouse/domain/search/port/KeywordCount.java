package com.tastyhouse.domain.search.port;

public record KeywordCount(
    String keyword,
    long count
) {
    public static KeywordCount of(
        String keyword,
        long count
    ) {
        return new KeywordCount(
            keyword,
            count
        );
    }
}

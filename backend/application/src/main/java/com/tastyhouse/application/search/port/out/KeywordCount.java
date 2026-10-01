package com.tastyhouse.application.search.port.out;

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

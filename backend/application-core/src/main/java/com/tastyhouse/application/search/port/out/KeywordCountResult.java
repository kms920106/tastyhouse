package com.tastyhouse.application.search.port.out;

public record KeywordCountResult(
    String keyword,
    long count
) {
}

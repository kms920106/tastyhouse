package com.tastyhouse.infrastructure.shop.query;

/**
 * 태그 목록 항목 결과.
 */
public record TagResult(
    Long id,
    String tagName
) {
}

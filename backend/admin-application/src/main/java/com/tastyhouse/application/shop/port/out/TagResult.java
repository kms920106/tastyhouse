package com.tastyhouse.application.shop.port.out;

/**
 * 태그 목록 항목 결과.
 */
public record TagResult(
    Long id,
    String tagName
) {
}

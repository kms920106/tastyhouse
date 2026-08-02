package com.tastyhouse.infrastructure.product.query;

/**
 * 배치 조회 응답에 포함되는 옵션 read model.
 */
public record BatchOptionResult(
    Long id,
    String name,
    Integer price
) {
}

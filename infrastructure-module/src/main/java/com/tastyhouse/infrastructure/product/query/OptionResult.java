package com.tastyhouse.infrastructure.product.query;

/**
 * 옵션 그룹에 속한 개별 옵션 read model.
 */
public record OptionResult(
    Long id,
    String name,
    Integer additionalPrice,
    boolean soldOut
) {
}

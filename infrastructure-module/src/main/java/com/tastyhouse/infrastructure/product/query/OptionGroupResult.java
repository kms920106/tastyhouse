package com.tastyhouse.infrastructure.product.query;

import java.util.List;

/**
 * 상품 옵션 그룹 read model. {@code common}이 true면 여러 상품이 공유하는 공통 옵션 그룹이다.
 */
public record OptionGroupResult(
    Long id,
    String name,
    String description,
    boolean required,
    boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    boolean common,
    List<OptionResult> options
) {
}

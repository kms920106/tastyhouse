package com.tastyhouse.application.product.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 상세 read model. web(간략 표시)·admin(관리 상세) 양쪽이 필요한 필드의 합집합이며, 각 모듈
 * QueryService가 자기 화면에 필요한 필드만 골라 Response로 조립한다.
 */
public record ProductDetailResult(
    Long id,
    Long shopId,
    Long productCategoryId,
    String name,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Double rating,
    Integer reviewCount,
    boolean representative,
    Integer spiciness,
    boolean soldOut,
    boolean visible,
    Integer sort,
    String weightText,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

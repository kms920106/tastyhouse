package com.tastyhouse.infrastructure.product.query;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.domain.product.model.VegetarianType;

/**
 * 점주 메뉴 상세 관리 read model. {@link ProductDetailResult}(web·admin 공유)와 달리 점주 관리 화면 전용
 * 필드(구성·1인분·평가제외·채식·대표이미지 URL·메뉴그룹명)를 담는다 — 필드 집합이 달라 통합하지 않는다.
 */
public record ProductManagementDetailResult(
    Long id,
    Long shopId,
    Long productCategoryId,
    String productCategoryName,
    String name,
    String composition,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    boolean singleServing,
    Integer spiciness,
    boolean representative,
    boolean ratingExcluded,
    boolean soldOut,
    boolean visible,
    String imageUrl,
    VegetarianType vegetarianType,
    boolean exposureScheduled
) {
    @QueryProjection
    public ProductManagementDetailResult {
    }
}

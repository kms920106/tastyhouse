package com.tastyhouse.infrastructure.product.query;

/**
 * 품절·숨김 관리 화면(메뉴/옵션 탭 공용) 검색 조건. 각 필드가 null이면 해당 조건을 적용하지 않는다.
 */
public record ProductAvailabilitySearchCondition(
    Long shopId,
    String keyword,
    Boolean soldOutOnly,
    Boolean hiddenOnly
) {

    public static ProductAvailabilitySearchCondition of(
        Long shopId,
        String keyword,
        Boolean soldOutOnly,
        Boolean hiddenOnly
    ) {
        return new ProductAvailabilitySearchCondition(shopId, keyword, soldOutOnly, hiddenOnly);
    }
}

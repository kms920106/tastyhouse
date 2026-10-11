package com.tastyhouse.application.product.port.out;

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

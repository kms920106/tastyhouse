package com.tastyhouse.core.domain.shop.application.dto;

public record ShopSearchCondition(
    String name,
    Long stationId,
    Boolean permanentlyClosed
) {

    public static ShopSearchCondition of(
        String name,
        Long stationId,
        Boolean permanentlyClosed
    ) {
        return new ShopSearchCondition(name, stationId, permanentlyClosed);
    }
}

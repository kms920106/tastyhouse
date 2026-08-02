package com.tastyhouse.infrastructure.shop.query;

public record ShopSearchCondition(
    String name,
    Long stationId,
    Boolean permanentlyClosed,
    Long ceoId
) {

    public static ShopSearchCondition of(
        String name,
        Long stationId,
        Boolean permanentlyClosed
    ) {
        return new ShopSearchCondition(name, stationId, permanentlyClosed, null);
    }

    public static ShopSearchCondition of(
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        Long ceoId
    ) {
        return new ShopSearchCondition(name, stationId, permanentlyClosed, ceoId);
    }
}

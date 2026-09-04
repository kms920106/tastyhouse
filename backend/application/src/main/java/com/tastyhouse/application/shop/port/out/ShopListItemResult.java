package com.tastyhouse.application.shop.port.out;

public record ShopListItemResult(
    Long id,
    String name,
    String stationName,
    String roadAddress,
    Double rating,
    boolean permanentlyClosed
) {
}

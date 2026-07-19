package com.tastyhouse.core.domain.shop.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;

public record ShopListItemResult(
    Long id,
    String name,
    String stationName,
    String roadAddress,
    Double rating,
    boolean permanentlyClosed
) {
    @QueryProjection
    public ShopListItemResult {
    }
}

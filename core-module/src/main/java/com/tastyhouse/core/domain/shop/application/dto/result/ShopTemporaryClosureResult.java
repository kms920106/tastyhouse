package com.tastyhouse.core.domain.shop.application.dto.result;

import java.time.LocalDate;

import com.tastyhouse.core.domain.shop.domain.model.ShopTemporaryClosure;

public record ShopTemporaryClosureResult(
    Long id,
    Long shopId,
    LocalDate startDate,
    LocalDate endDate
) {

    public static ShopTemporaryClosureResult from(ShopTemporaryClosure shopTemporaryClosure) {
        return new ShopTemporaryClosureResult(
            shopTemporaryClosure.getId(),
            shopTemporaryClosure.getShopId(),
            shopTemporaryClosure.getStartDate(),
            shopTemporaryClosure.getEndDate()
        );
    }
}

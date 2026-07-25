package com.tastyhouse.core.domain.shop.application.dto.command;

import java.time.LocalDate;

public record ShopTemporaryClosureCreateCommand(
    Long shopId,
    LocalDate startDate,
    LocalDate endDate
) {

    public static ShopTemporaryClosureCreateCommand of(Long shopId, LocalDate startDate, LocalDate endDate) {
        return new ShopTemporaryClosureCreateCommand(shopId, startDate, endDate);
    }
}

package com.tastyhouse.core.domain.shop.application.dto.command;

import java.time.LocalDate;

import com.tastyhouse.core.domain.shop.domain.model.HygieneBadgeType;

public record ShopHygieneBadgeCreateCommand(
    Long shopId,
    HygieneBadgeType badgeType,
    LocalDate certifiedDate,
    String lastInspectionMonth
) {

    public static ShopHygieneBadgeCreateCommand of(
        Long shopId,
        HygieneBadgeType badgeType,
        LocalDate certifiedDate,
        String lastInspectionMonth
    ) {
        return new ShopHygieneBadgeCreateCommand(shopId, badgeType, certifiedDate, lastInspectionMonth);
    }
}

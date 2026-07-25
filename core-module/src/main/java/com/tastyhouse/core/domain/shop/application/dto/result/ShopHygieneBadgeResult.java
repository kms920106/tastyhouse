package com.tastyhouse.core.domain.shop.application.dto.result;

import java.time.LocalDate;

import com.tastyhouse.core.domain.shop.domain.model.HygieneBadgeType;
import com.tastyhouse.core.domain.shop.domain.model.ShopHygieneBadge;

public record ShopHygieneBadgeResult(
    Long id,
    Long shopId,
    HygieneBadgeType badgeType,
    LocalDate certifiedDate,
    String lastInspectionMonth
) {

    public static ShopHygieneBadgeResult from(ShopHygieneBadge shopHygieneBadge) {
        return new ShopHygieneBadgeResult(
            shopHygieneBadge.getId(),
            shopHygieneBadge.getShopId(),
            shopHygieneBadge.getBadgeType(),
            shopHygieneBadge.getCertifiedDate(),
            shopHygieneBadge.getLastInspectionMonth()
        );
    }
}

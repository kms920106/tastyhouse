package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDate;

import com.tastyhouse.core.domain.shop.domain.model.HygieneBadgeType;

public record ShopHygieneBadgeResult(
    Long id,
    Long shopId,
    HygieneBadgeType badgeType,
    LocalDate certifiedDate,
    String lastInspectionMonth
) {

}

package com.tastyhouse.application.shop.port.out;

import java.time.LocalDate;

public record ShopHygieneBadgeResult(
    Long id,
    Long shopId,
    String badgeType,
    LocalDate certifiedDate,
    String lastInspectionMonth
) {
}

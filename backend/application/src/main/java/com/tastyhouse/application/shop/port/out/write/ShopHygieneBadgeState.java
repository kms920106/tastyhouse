package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShopHygieneBadgeState(
    Long id,
    Long shopId,
    String badgeType,
    LocalDate certifiedDate,
    String lastInspectionMonth,
    LocalDateTime createdAt
) {
}

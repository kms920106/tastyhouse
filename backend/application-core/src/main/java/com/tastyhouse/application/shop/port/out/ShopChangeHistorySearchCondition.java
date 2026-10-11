package com.tastyhouse.application.shop.port.out;

import java.time.LocalDate;

public record ShopChangeHistorySearchCondition(
    Long shopId,
    String category,
    String changeType,
    LocalDate changedDate,
    LocalDate retentionFrom
) {
}

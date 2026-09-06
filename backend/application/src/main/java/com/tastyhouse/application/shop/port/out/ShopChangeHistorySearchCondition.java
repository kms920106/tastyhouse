package com.tastyhouse.application.shop.port.out;

import java.time.LocalDate;

import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;

public record ShopChangeHistorySearchCondition(
    Long shopId,
    ShopChangeCategory category,
    ShopChangeType changeType,
    LocalDate changedDate,
    LocalDate retentionFrom
) {
}

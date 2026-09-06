package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;

public record ShopChangeHistoryResult(
    Long id,
    ShopChangeCategory category,
    ShopChangeType changeType,
    ShopChangeActionType actionType,
    String previousValue,
    String newValue,
    LocalDateTime changedAt
) {
}

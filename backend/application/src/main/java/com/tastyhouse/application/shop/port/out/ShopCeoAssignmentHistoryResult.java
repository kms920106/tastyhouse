package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;

public record ShopCeoAssignmentHistoryResult(
    Long id,
    Long shopId,
    String shopName,
    ShopCeoAssignmentActionType actionType,
    LocalDateTime occurredAt
) {
}

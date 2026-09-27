package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopCeoAssignmentHistoryResult(
    Long id,
    Long shopId,
    String shopName,
    String actionType,
    String actionTypeDescription,
    LocalDateTime occurredAt
) {
}

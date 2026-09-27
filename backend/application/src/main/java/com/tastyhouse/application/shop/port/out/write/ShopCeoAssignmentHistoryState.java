package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopCeoAssignmentHistoryState(
    Long id,
    Long shopId,
    Long ceoId,
    String actionType,
    Long actorAdminId,
    LocalDateTime createdAt
) {
}

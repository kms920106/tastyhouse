package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopChangeHistoryState(
    Long id,
    Long shopId,
    String category,
    String changeType,
    String actionType,
    String actorType,
    Long actorId,
    String previousValue,
    String newValue,
    LocalDateTime createdAt
) {
}

package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopRiderGuideHistoryState(
    Long id,
    Long shopId,
    String actorType,
    Long actorId,
    String actionType,
    String previousVisitGuide,
    String newVisitGuide,
    String reason,
    LocalDateTime createdAt
) {
}

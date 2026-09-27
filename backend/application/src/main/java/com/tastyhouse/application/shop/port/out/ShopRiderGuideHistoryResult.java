package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopRiderGuideHistoryResult(
    Long id,
    String actorType,
    Long actorId,
    String actionType,
    String previousVisitGuide,
    String newVisitGuide,
    String reason,
    LocalDateTime createdAt
) {
}

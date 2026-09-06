package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;

public record ShopRiderGuideHistoryResult(
    Long id,
    RiderGuideActorType actorType,
    Long actorId,
    RiderGuideActionType actionType,
    String previousVisitGuide,
    String newVisitGuide,
    String reason,
    LocalDateTime createdAt
) {
}

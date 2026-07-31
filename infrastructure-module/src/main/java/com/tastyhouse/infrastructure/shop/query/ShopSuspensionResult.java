package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.domain.shop.domain.model.SuspensionReason;

public record ShopSuspensionResult(
    Long id,
    Long shopId,
    SuspensionReason reason,
    OrderMethod orderMethod,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime releasedAt
) {

}

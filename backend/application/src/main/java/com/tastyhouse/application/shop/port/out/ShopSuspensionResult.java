package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.SuspensionReason;

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

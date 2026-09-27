package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopSuspensionState(
    Long id,
    Long shopId,
    String reason,
    String orderMethod,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime releasedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

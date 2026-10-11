package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopSuspensionResult(
    Long id,
    Long shopId,
    String reason,
    String orderMethod,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime releasedAt
) {
}

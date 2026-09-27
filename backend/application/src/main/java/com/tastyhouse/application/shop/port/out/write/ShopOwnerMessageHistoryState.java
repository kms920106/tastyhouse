package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopOwnerMessageHistoryState(
    Long id,
    Long shopId,
    String message,
    LocalDateTime createdAt
) {
}

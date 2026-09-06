package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopOwnerMessageResult(
    String message,
    LocalDateTime createdAt
) {
}

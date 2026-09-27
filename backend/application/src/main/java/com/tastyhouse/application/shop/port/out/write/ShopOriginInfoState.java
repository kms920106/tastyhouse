package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopOriginInfoState(
    Long id,
    Long shopId,
    String sourceType,
    String content,
    String url,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

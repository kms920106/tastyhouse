package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopOriginInfoResult(
    Long id,
    Long shopId,
    String sourceType,
    String content,
    String url,
    LocalDateTime updatedAt
) {
}

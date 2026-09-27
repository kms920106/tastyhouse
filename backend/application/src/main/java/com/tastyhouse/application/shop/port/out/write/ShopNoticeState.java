package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopNoticeState(
    Long id,
    Long shopId,
    String content,
    boolean exposed,
    boolean hidden,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

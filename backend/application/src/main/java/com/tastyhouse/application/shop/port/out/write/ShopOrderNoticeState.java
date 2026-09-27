package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopOrderNoticeState(
    Long id,
    Long shopId,
    String content,
    boolean hidden,
    String hiddenReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

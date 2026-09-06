package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopNoticeResult;
import java.time.LocalDateTime;

public record ShopNoticeRow(
    Long id,
    Long shopId,
    String content,
    boolean exposed,
    boolean hidden,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

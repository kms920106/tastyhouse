package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;

import com.tastyhouse.application.shop.port.out.ShopNoticeResult;

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

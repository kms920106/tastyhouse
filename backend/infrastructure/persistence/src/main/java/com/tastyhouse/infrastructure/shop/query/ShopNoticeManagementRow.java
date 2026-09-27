package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;

import com.tastyhouse.application.shop.port.out.ShopNoticeManagementListItemResult;

public record ShopNoticeManagementRow(
    Long id,
    Long shopId,
    String shopName,
    String content,
    boolean exposed,
    boolean hidden,
    LocalDateTime createdAt
) {
}

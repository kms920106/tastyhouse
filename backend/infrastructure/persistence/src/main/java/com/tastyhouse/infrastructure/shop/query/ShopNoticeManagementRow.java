package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopNoticeManagementListItemResult;
import java.time.LocalDateTime;

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

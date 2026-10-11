package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;
import java.util.List;

public record ShopNoticeManagementListItemResult(
    Long id,
    Long shopId,
    String shopName,
    String content,
    List<String> imageUrls,
    boolean exposed,
    boolean hidden,
    LocalDateTime createdAt
) {
}

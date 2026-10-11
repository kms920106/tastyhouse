package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;
import java.util.List;

public record ShopNoticeResult(
    Long id,
    Long shopId,
    String content,
    List<String> imageUrls,
    boolean exposed,
    boolean hidden,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

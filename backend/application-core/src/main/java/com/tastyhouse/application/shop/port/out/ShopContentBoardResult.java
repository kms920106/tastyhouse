package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopContentBoardResult(
    Long id,
    Long shopId,
    String contentType,
    String topic,
    String imageUrl,
    String youtubeUrl,
    String description,
    boolean hidden,
    LocalDateTime createdAt
) {
}

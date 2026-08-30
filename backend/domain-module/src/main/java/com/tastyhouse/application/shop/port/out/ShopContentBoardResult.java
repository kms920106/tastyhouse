package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopContentTopic;
import com.tastyhouse.domain.shop.model.ShopContentType;

public record ShopContentBoardResult(
    Long id,
    Long shopId,
    ShopContentType contentType,
    ShopContentTopic topic,
    String imageUrl,
    String youtubeUrl,
    String description,
    boolean hidden,
    LocalDateTime createdAt
) {
}

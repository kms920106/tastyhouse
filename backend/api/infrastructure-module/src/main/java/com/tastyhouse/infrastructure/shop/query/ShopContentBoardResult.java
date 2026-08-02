package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.model.ShopContentTopic;

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

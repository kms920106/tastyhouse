package com.tastyhouse.core.domain.shop.application.dto.command;

import com.tastyhouse.core.domain.shop.domain.model.ShopContentType;
import com.tastyhouse.core.domain.shop.domain.model.ShopContentTopic;

public record ShopContentBoardCreateCommand(
    Long shopId,
    ShopContentType contentType,
    ShopContentTopic topic,
    Long imageFileId,
    String youtubeUrl,
    String description
) {

    public static ShopContentBoardCreateCommand of(
        Long shopId,
        ShopContentType contentType,
        ShopContentTopic topic,
        Long imageFileId,
        String youtubeUrl,
        String description
    ) {
        return new ShopContentBoardCreateCommand(shopId, contentType, topic, imageFileId, youtubeUrl, description);
    }
}

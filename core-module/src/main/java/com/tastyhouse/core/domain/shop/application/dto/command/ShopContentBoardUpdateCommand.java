package com.tastyhouse.core.domain.shop.application.dto.command;

import com.tastyhouse.core.domain.shop.domain.model.ShopContentTopic;

public record ShopContentBoardUpdateCommand(
    ShopContentTopic topic,
    Long imageFileId,
    String youtubeUrl,
    String description
) {

    public static ShopContentBoardUpdateCommand of(
        ShopContentTopic topic,
        Long imageFileId,
        String youtubeUrl,
        String description
    ) {
        return new ShopContentBoardUpdateCommand(topic, imageFileId, youtubeUrl, description);
    }
}

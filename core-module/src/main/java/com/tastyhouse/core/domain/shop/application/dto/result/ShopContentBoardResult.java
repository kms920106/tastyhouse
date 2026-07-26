package com.tastyhouse.core.domain.shop.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.shop.domain.model.ShopContentBoard;
import com.tastyhouse.core.domain.shop.domain.model.ShopContentType;
import com.tastyhouse.core.domain.shop.domain.model.ShopContentTopic;

public record ShopContentBoardResult(
    Long id,
    Long shopId,
    ShopContentType contentType,
    ShopContentTopic topic,
    Long imageFileId,
    String youtubeUrl,
    String description,
    boolean hidden,
    LocalDateTime createdAt
) {

    public static ShopContentBoardResult from(ShopContentBoard shopContentBoard) {
        return new ShopContentBoardResult(
            shopContentBoard.getId(),
            shopContentBoard.getShopId(),
            shopContentBoard.getContentType(),
            shopContentBoard.getTopic(),
            shopContentBoard.getImageFileId(),
            shopContentBoard.getYoutubeUrl(),
            shopContentBoard.getDescription(),
            shopContentBoard.isHidden(),
            shopContentBoard.getCreatedAt()
        );
    }
}

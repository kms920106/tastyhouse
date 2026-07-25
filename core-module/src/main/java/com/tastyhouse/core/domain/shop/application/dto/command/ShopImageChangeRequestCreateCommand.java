package com.tastyhouse.core.domain.shop.application.dto.command;

import com.tastyhouse.core.domain.shop.domain.model.ShopImageType;

public record ShopImageChangeRequestCreateCommand(
    Long shopId,
    ShopImageType imageType,
    Long imageFileId
) {

    public static ShopImageChangeRequestCreateCommand of(Long shopId, ShopImageType imageType, Long imageFileId) {
        return new ShopImageChangeRequestCreateCommand(shopId, imageType, imageFileId);
    }
}

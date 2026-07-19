package com.tastyhouse.core.domain.shop.application.dto.command;

public record ShopBannerImageSaveCommand(
    Long imageFileId,
    Integer sort
) {

    public static ShopBannerImageSaveCommand of(Long imageFileId, Integer sort) {
        return new ShopBannerImageSaveCommand(imageFileId, sort);
    }
}

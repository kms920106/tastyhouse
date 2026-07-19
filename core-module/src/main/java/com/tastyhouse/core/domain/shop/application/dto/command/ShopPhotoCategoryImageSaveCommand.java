package com.tastyhouse.core.domain.shop.application.dto.command;

public record ShopPhotoCategoryImageSaveCommand(
    Long imageFileId,
    Integer sort,
    boolean visible
) {

    public static ShopPhotoCategoryImageSaveCommand of(
        Long imageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopPhotoCategoryImageSaveCommand(
            imageFileId,
            sort,
            visible
        );
    }
}

package com.tastyhouse.application.shop.port.out.write;

public record ShopPhotoCategoryImageState(
    Long id,
    Long shopPhotoCategoryId,
    Long imageFileId,
    Integer sort,
    boolean visible
) {
}

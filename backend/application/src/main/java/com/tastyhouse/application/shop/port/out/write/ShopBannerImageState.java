package com.tastyhouse.application.shop.port.out.write;

public record ShopBannerImageState(
    Long id,
    Long shopId,
    Long imageFileId,
    Integer sort
) {
}

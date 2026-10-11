package com.tastyhouse.application.shop.port.out;

public record ShopBannerImageResult(
    Long id,
    String imageUrl,
    Integer sort
) {
}

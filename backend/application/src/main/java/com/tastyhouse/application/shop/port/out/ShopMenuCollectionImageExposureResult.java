package com.tastyhouse.application.shop.port.out;

public record ShopMenuCollectionImageExposureResult(
    Long id,
    String imageUrl,
    Integer sort
) {
}

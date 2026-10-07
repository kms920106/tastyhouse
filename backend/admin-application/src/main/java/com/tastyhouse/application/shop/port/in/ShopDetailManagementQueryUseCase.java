package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopManagementDetailResult;

public interface ShopDetailManagementQueryUseCase {

    ShopDetail getShop(Long id);

    record ShopDetail(
        ShopManagementDetailResult shop,
        String thumbnailImageUrl
    ) {
    }
}

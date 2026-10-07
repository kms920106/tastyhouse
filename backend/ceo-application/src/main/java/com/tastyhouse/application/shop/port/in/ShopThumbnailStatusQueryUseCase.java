package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopImageStatusResult;

public interface ShopThumbnailStatusQueryUseCase {

    ShopImageStatusResult getThumbnailStatus(Long ceoId, Long shopId);
}

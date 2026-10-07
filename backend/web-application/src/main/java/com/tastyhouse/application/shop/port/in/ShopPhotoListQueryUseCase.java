package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryViewResult;

public interface ShopPhotoListQueryUseCase {

    List<ShopPhotoCategoryViewResult> getShopPhotos(Long shopId);
}

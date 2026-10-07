package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;

public interface ShopBannerListQueryUseCase {

    List<ShopBannerImageResult> getShopBanners(Long shopId);
}

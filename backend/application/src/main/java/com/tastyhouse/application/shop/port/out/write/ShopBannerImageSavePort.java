package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopBannerImage;

public interface ShopBannerImageSavePort {

    ShopBannerImage saveBannerImage(ShopBannerImage bannerImage);

    void deleteBannerImageById(Long id);
}

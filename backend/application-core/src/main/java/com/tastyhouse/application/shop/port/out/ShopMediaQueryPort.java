package com.tastyhouse.application.shop.port.out;

import java.util.List;

public interface ShopMediaQueryPort {

    List<ShopMenuCollectionImageExposureResult> findMenuCollectionImagesByStatus(Long shopId, String status);

    List<ShopPhotoCategoryImageResult> findAllPhotoCategoryImages();

    List<ShopBannerImageResult> findBannerImages(Long shopId);

    List<ShopPhotoCategoryResult> findPhotoCategories(Long shopId);
}

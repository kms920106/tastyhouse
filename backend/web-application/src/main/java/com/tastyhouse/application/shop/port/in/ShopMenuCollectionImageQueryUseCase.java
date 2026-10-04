package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageExposureResult;

public interface ShopMenuCollectionImageQueryUseCase {

    List<ShopMenuCollectionImageExposureResult> getMenuCollectionImages(Long shopId);
}

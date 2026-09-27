package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageExposureResult;

@WebApp
public interface ShopMenuCollectionImageQueryUseCase {

    List<ShopMenuCollectionImageExposureResult> getMenuCollectionImages(Long shopId);
}

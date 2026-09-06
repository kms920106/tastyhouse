package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageResult;

@CeoApp
public interface ShopMenuCollectionImageOwnerQueryUseCase {

    List<ShopMenuCollectionImageResult> getMenuCollectionImages(Long ceoId, Long shopId);
}
